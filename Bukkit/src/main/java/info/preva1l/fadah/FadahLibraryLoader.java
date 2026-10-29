package info.preva1l.fadah;

import com.google.gson.Gson;
import info.preva1l.trashcan.extension.libloader.Dependency;
import info.preva1l.trashcan.extension.libloader.Libraries;
import info.preva1l.trashcan.extension.libloader.Repository;
import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.LibraryStore;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Chargeur de bibliothèques de Fadah, à la place de {@code BaseLibraryLoader} de Trashcan.
 * <p>
 * Même principe que celui de Trashcan (lit {@code libraries.json} puis {@code trashcan-libraries.json}, un
 * {@link MavenLibraryResolver} par dépôt, jar remappé pour les dépendances {@code remap}), avec deux différences :
 * <ul>
 *     <li>Paper 26 refuse Maven Central comme CDN ({@code Plugin used Maven Central for library resolution}) :
 *     tout dépôt Maven Central est remplacé par {@link MavenLibraryResolver#MAVEN_CENTRAL_DEFAULT_MIRROR} ;</li>
 *     <li>une dépendance déclarée par Fadah ({@code libraries.json}) l'emporte sur la même dépendance
 *     (groupe:artefact) déclarée par Trashcan ({@code trashcan-libraries.json}), quelle que soit sa version : sans
 *     cela les deux versions seraient chargées ensemble.</li>
 * </ul>
 * Chaque dépôt a son propre résolveur : une bibliothèque n'est cherchée que dans le dépôt où le build l'a déclarée
 * (multilib vient de Clojars, jamais d'un dépôt {@code file:}).
 * <p>
 * Cette classe est chargée par Paper avant le plugin : elle ne doit dépendre que de l'API Paper, de Gson, d'Aether et
 * des classes {@code libloader} de Trashcan (relocalisées dans le jar).
 */
public class FadahLibraryLoader implements PluginLoader {
    private static final Logger LOGGER = Logger.getLogger("Fadah");

    private final Gson gson = new Gson();

    @Override
    public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
        Libraries libraries = load();

        for (Repository repository : libraries.repositories().values()) {
            if (repository.dependencies().isEmpty()) {
                continue;
            }

            RemoteRepository remote = new RemoteRepository.Builder(
                    repository.name(), "default", resolveUrl(repository.url())
            ).build();

            MavenLibraryResolver resolver = new MavenLibraryResolver();
            resolver.addRepository(remote);

            for (Dependency dependency : repository.dependencies()) {
                org.eclipse.aether.graph.Dependency aether = new org.eclipse.aether.graph.Dependency(
                        new DefaultArtifact(dependency.asGavCoordinate()), null
                );

                if (dependency.remap()) {
                    // Résolveur dédié : seuls les jars à remapper passent par le manifeste « spigot ».
                    MavenLibraryResolver remapped = new MavenLibraryResolver();
                    remapped.addDependency(aether);
                    remapped.addRepository(remote);
                    register(classpathBuilder, remapped, true);
                    continue;
                }
                resolver.addDependency(aether);
            }
            register(classpathBuilder, resolver, false);
        }
    }

    /**
     * Maven Central est interdit comme CDN par Paper 26 : on passe par le miroir qu'il fournit.
     */
    static String resolveUrl(String url) {
        if (url == null) {
            return null;
        }
        String normalized = url.trim().toLowerCase(Locale.ROOT);
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return switch (normalized) {
            case "https://repo.maven.apache.org/maven2",
                 "http://repo.maven.apache.org/maven2",
                 "https://repo1.maven.org/maven2",
                 "http://repo1.maven.org/maven2" -> MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR;
            default -> url;
        };
    }

    private void register(PluginClasspathBuilder builder, MavenLibraryResolver resolver, boolean remap) {
        if (!remap) {
            builder.addLibrary(resolver);
            return;
        }
        builder.addLibrary(store -> resolver.register(remappingStore(store)));
    }

    private LibraryStore remappingStore(LibraryStore store) {
        return jar -> {
            Path toAdd = jar;
            try {
                Path remapped = Files.createTempFile("remapped-", ".jar");
                modifyManifest(jar, remapped);
                toAdd = remapped;
            } catch (IOException ignored) {
                // Repli : le jar d'origine, non remappé.
            }
            store.addLibrary(toAdd);
        };
    }

    /**
     * Copie {@code source} vers {@code target} en déclarant le jar comme mappé Spigot ({@code paperweight-mappings-namespace}).
     */
    private void modifyManifest(Path source, Path target) throws IOException {
        Path temp = Files.createTempFile("temp-", ".jar");
        try (JarFile jar = new JarFile(source.toFile());
             JarOutputStream out = new JarOutputStream(Files.newOutputStream(temp))) {
            var entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if ("META-INF/MANIFEST.MF".equals(entry.getName())) {
                    continue;
                }
                out.putNextEntry(new JarEntry(entry.getName()));
                try (InputStream in = jar.getInputStream(entry)) {
                    in.transferTo(out);
                }
                out.closeEntry();
            }

            Manifest manifest = jar.getManifest();
            if (manifest == null) {
                manifest = new Manifest();
            }
            Attributes main = manifest.getMainAttributes();
            if (main.getValue(Attributes.Name.MANIFEST_VERSION) == null) {
                main.put(Attributes.Name.MANIFEST_VERSION, "1.0");
            }
            main.putValue("paperweight-mappings-namespace", "spigot");
            out.putNextEntry(new JarEntry("META-INF/MANIFEST.MF"));
            manifest.write(out);
            out.closeEntry();
        }
        Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Dépendances de Fadah ({@code libraries.json}) + celles de Trashcan ({@code trashcan-libraries.json}), les
     * premières remplaçant les secondes à groupe:artefact égal.
     */
    private Libraries load() {
        Libraries fadah = read("libraries.json");
        Libraries trashcan = read("trashcan-libraries.json");

        Set<String> declared = new HashSet<>();
        for (Repository repository : fadah.repositories().values()) {
            for (Dependency dependency : repository.dependencies()) {
                declared.add(dependency.group() + ":" + dependency.artifact());
            }
        }

        Map<String, Repository> merged = new HashMap<>();
        fadah.repositories().forEach((key, repository) ->
                merged.put(key, new Repository(repository.name(), repository.url(), new ArrayList<>(repository.dependencies()))));

        trashcan.repositories().forEach((key, repository) -> {
            List<Dependency> kept = repository.dependencies().stream()
                    .filter(dependency -> !declared.contains(dependency.group() + ":" + dependency.artifact()))
                    .toList();
            merged.merge(key, new Repository(repository.name(), repository.url(), new ArrayList<>(kept)), (existing, extra) -> {
                List<Dependency> all = new ArrayList<>(existing.dependencies());
                all.addAll(extra.dependencies());
                return new Repository(existing.name(), existing.url(), all);
            });
        });
        return new Libraries(merged);
    }

    private Libraries read(String name) {
        try (InputStream in = FadahLibraryLoader.class.getResourceAsStream("/" + name)) {
            if (in != null) {
                Libraries libraries = gson.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), Libraries.class);
                if (libraries != null && libraries.repositories() != null) {
                    return libraries;
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return new Libraries(new HashMap<>());
    }
}
