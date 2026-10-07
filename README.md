[![CodeFactor](https://www.codefactor.io/repository/github/finally-a-decent/fadah/badge)](https://www.codefactor.io/repository/github/finally-a-decent/fadah)
## Fadah (Finally a Decent Auction House) is the fast, modern and advanced auction house plugin that you have been looking for!

- Modrinth/Download: https://modrinth.com/plugin/fadah/
- Docs: https://docs.preva1l.info/fadah/

#### Features:
- Item Searching
- Item Filtering
- Infinite & Configurable Categories
- Folia & ShreddedPaper Support
- Multi Server Support
- Multiple Databases Supported
- Fast Bug Fixes
- Used & battle tested in large production environments
- Configurable Max Listings
- Highly Configurable
- Transaction Logs

#### Commands
----------------------------------------
/ah | fadah.use
/ah help | fadah.help
/ah sell <amount> | fadah.use
/ah toggle | fadah.toggle-status
/ah reload | fadah.reload
/ah profile | fadah.profile
/ah expired | fadah.expired-items
/ah redeem | fadah.collection-box

Permissions:
- `fadah.max-listings.<amount>` - How many items the user can list on the auction house at any one time
- `fadah.listing-tax.<percent>` - The percent a user should be taxed from the products price
- `fadah.advert-price.<amount>` - How much an advert should cost
- `fadah.manage.<command>` - Allows the user to manage other peoples profiles etc

Support: https://discord.gg/4KcF7S94HF

Requirements:
- Vault & Compatible Economy Plugin
- (Optional) A database (MySQL, MariaDB, MongoDB)
- (Optional) Redis

## Building from source

The original developer's Maven repository (`https://repo.preva1l.info/`) is offline,
so several artifacts that aren't published anywhere else are vendored into this repo:

- `libs/maven-repo/` — file-based Maven repository containing:
  - `info.preva1l.hooker:Hooker:1.0.2` (built from `Finally-A-Decent/Hooker` master)
  - `info.preva1l.trashcan:Trashcan-Tooling:1.0.3` (Gradle plugin)
  - `info.preva1l.trashcan:{common, common-kotlin, paper, paper-kotlin}:1.2.3`
  - `com.github.puregero:multilib:1.2.5` (built from `PureGero/multilib` HEAD — no public release exists)
- `libs/libreforge-4.58.1.jar` — the `shadow` classifier from auxilor (the regular `com.willfp:libreforge:4.58.1` artifact is a 302-byte stub)
- `libs/{AkarianAuctionHouse, AuctionHouse, CoinsEngine}-*.jar` — pre-existing migrator/integration jars

The Gradle build is wired to read `libs/maven-repo/` as a local Maven repository
(see `settings.gradle.kts`, `buildSrc/build.gradle.kts`, `buildSrc/settings.gradle.kts`,
and `buildSrc/src/main/kotlin/fadah.common.gradle.kts`), so a normal `./gradlew build`
works out of the box without installing anything to your local Maven cache.

If you'd rather install these to your own `~/.m2/repository` (e.g. for use by another
project on your machine), copy `libs/maven-repo/*` into `~/.m2/repository/`, or use
`mvn install:install-file` per artifact.


## Compiler en local

Fork du réseau Skullbox. Prérequis : JDK 21 (le wrapper Gradle 8.14 est dans le dépôt, rien d'autre à installer).
Aucun jeton n'est nécessaire : Fadah ne lit aucun paquet privé (les dépendances viennent de dépôts publics, dont
Clojars pour `multilib`, et de `libs/maven-repo/`).

```bash
./gradlew clean build
```

Le jar du plugin déployé est `target/Fadah-Bukkit-3.2.2.jar` (`Fadah-API-3.2.2.jar` est l'API seule). La version vient
de `gradle.properties` ; `-Pversion=x.y.z` la remplace, et le jar comme `paper-plugin.yml` la portent. Toujours
lancer `clean` : le descripteur n'est pas régénéré quand seule la version change.

## CI/CD (Skull-box)

Fichiers : `.github/workflows/ci.yml`, `.github/scripts/`, `.releaserc.json`. Runner `blacksmith-2vcpu-ubuntu-2404`, JDK 21.
Ils remplacent les workflows de l'amont (`test.yml`, `publish.yml`).

- **Chaque push et PR** : `./gradlew clean build` (tests compris). Une PR ne publie rien.
- **Push sur `master`** : release GitHub par [semantic-release](https://github.com/semantic-release/semantic-release)
  (`feat:` = mineure, `fix:` = correctif, `feat!:` ou `BREAKING CHANGE` = majeure ; `ci:`, `build:`, `docs:`, `chore:`
  ne produisent aucune release). La Release porte `Fadah-Bukkit-<version>.jar`. La première Release (`v3.2.2`) a été
  créée d'après la version de `gradle.properties`. Les commits de l'amont ne suivent pas forcément les conventional
  commits : peu de versions sortent seules après une synchronisation.
- **Push sur `master`, aussi** : le jar du plugin est publié en SNAPSHOT mobile `fr.skullbox:fadah-bukkit:3.2.2-SNAPSHOT`
  sur `https://maven.pkg.github.com/Skull-box/Fadah` (pom sans dépendances transitives). Élagage : au 10e dépôt du
  SNAPSHOT le paquet est supprimé puis republié (~0,63 Mo par publication, 6,3 Mo au plus).
- **Toute autre branche** : prérelease GitHub glissante `build-<branche>` (titre = nom de la branche), écrasée à
  chaque push, supprimée avec la branche.
