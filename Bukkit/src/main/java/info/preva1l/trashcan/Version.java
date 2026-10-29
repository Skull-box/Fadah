package info.preva1l.trashcan;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.logging.Logger;

/**
 * Remplace {@code info.preva1l.trashcan.Version} des jars Trashcan vendus (même nom de classe, mêmes signatures :
 * le shadowJar garde cette copie, voir {@code Bukkit/build.gradle.kts}).
 * <p>
 * L'original ne lisait que {@code 1.2.3-metadonnees} et rejetait le reste ({@code "Invalid Version ... Using 0.0.0"}).
 * Cette version accepte en plus :
 * <ul>
 *     <li>les suffixes {@code +} et {@code -} : {@code 3.3.2+26.2.762effc}, {@code 3.3.2-SNAPSHOT},
 *     {@code 26.2.build.134-stable} (tout ce qui suit le premier séparateur est la métadonnée) ;</li>
 *     <li>le format {@code 26.2.build.N} (Paper, ASP) : {@code 26.2} puis le numéro de build N, qui départage deux
 *     builds de la même version ;</li>
 *     <li>un préfixe {@code v} et des segments non numériques en fin de numéro ({@code 1.0.0.RELEASE}) : seuls les
 *     segments numériques du début comptent.</li>
 * </ul>
 * Un numéro sans aucun segment numérique reste 0.0.0, avec le même message d'erreur qu'avant.
 */
public class Version implements Comparable<Version> {
    public final String VERSION_DELIMITER = ".";
    public static final String META_DELIMITER = "-";

    private static final String BUILD_SEGMENT = "build";

    private int[] versions = new int[0];
    private String metadata = "";
    private String metaSeparator = "";
    /** Numéro de build ({@code 26.2.build.N}), -1 si absent. */
    private int build = -1;

    protected Version() {
    }

    private Version(String version, String metaSeparator) {
        this.metaSeparator = metaSeparator == null ? META_DELIMITER : metaSeparator;
        parse(version, this.metaSeparator);
    }

    public static Version fromString(String version, String metaSeparator) {
        return new Version(version, metaSeparator);
    }

    public static Version fromString(String version) {
        return new Version(version, META_DELIMITER);
    }

    private void parse(String raw, String separator) {
        String version = raw == null ? "" : raw.trim();
        if (version.length() > 1 && (version.charAt(0) == 'v' || version.charAt(0) == 'V')
                && Character.isDigit(version.charAt(1))) {
            version = version.substring(1);
        }

        // La métadonnée commence au premier séparateur (celui demandé, « + » ou « - »), pas en tête de chaîne.
        int cut = -1;
        String found = separator;
        for (String candidate : candidates(separator)) {
            int index = version.indexOf(candidate);
            if (index > 0 && (cut == -1 || index < cut)) {
                cut = index;
                found = candidate;
            }
        }
        String core = version;
        if (cut > 0) {
            this.metaSeparator = found;
            this.metadata = version.substring(cut + found.length());
            core = version.substring(0, cut);
        }

        List<Integer> numbers = new ArrayList<>();
        String[] segments = core.split(java.util.regex.Pattern.quote(VERSION_DELIMITER));
        for (int i = 0; i < segments.length; i++) {
            String segment = segments[i];
            if (isNumber(segment)) {
                numbers.add(Integer.parseInt(segment));
                continue;
            }
            if (BUILD_SEGMENT.equalsIgnoreCase(segment) && i + 1 < segments.length && isNumber(segments[i + 1])) {
                this.build = Integer.parseInt(segments[i + 1]);
            }
            break;
        }

        if (numbers.isEmpty()) {
            Logger.getLogger(getClass().getName()).severe("Invalid Version \"%s\" Using \"0.0.0\"".formatted(version));
            this.versions = new int[]{0, 0, 0};
            this.build = -1;
            return;
        }
        this.versions = numbers.stream().mapToInt(Integer::intValue).toArray();
    }

    private static List<String> candidates(String separator) {
        List<String> candidates = new ArrayList<>(3);
        if (separator != null && !separator.isEmpty()) {
            candidates.add(separator);
        }
        for (String known : new String[]{"+", "-"}) {
            if (!candidates.contains(known)) {
                candidates.add(known);
            }
        }
        return candidates;
    }

    private static boolean isNumber(String segment) {
        if (segment.isEmpty() || segment.length() > 9) {
            return false;
        }
        for (int i = 0; i < segment.length(); i++) {
            char c = segment.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    @Override
    public int compareTo(Version other) {
        int length = Math.max(versions.length, other.versions.length);
        for (int i = 0; i < length; i++) {
            int mine = i < versions.length ? versions[i] : 0;
            int theirs = i < other.versions.length ? other.versions[i] : 0;
            if (mine < theirs) {
                return -1;
            }
            if (mine > theirs) {
                return 1;
            }
        }
        return Integer.compare(build, other.build);
    }

    @Override
    public String toString() {
        String suffix = metadata.isEmpty() ? "" : metaSeparator + metadata;
        return toStringWithoutMetadata() + suffix;
    }

    public String toStringWithoutMetadata() {
        StringJoiner joiner = new StringJoiner(VERSION_DELIMITER);
        for (int number : versions) {
            joiner.add(String.valueOf(number));
        }
        if (build >= 0) {
            joiner.add(BUILD_SEGMENT).add(String.valueOf(build));
        }
        return joiner.toString();
    }

    public int getMajor() {
        return segment(0);
    }

    public int getMinor() {
        return segment(1);
    }

    public int getPatch() {
        return segment(2);
    }

    public String getMetadata() {
        return metadata;
    }

    private int segment(int index) {
        return index < versions.length ? versions[index] : 0;
    }
}
