# Talking Skin (Fabric, Minecraft 26.3)

Dicke Cartoon-Lippen mit Zähnen und leichte Körper-, Kopf- und Armbewegung an Spielermodellen,
sobald jemand im **Simple Voice Chat** spricht. Der Mund öffnet sich nach der Lautstärke.

* Rein clientseitig: Jeder, der die Mod installiert hat, sieht die Animation bei allen Spielern,
  die er im Voicechat hört. Die anderen brauchen die Mod nicht.
* Läuft auch ohne Simple Voice Chat, tut dann aber nichts.

## Fertige .jar bauen

**Variante A: GitHub (kein lokales Setup)**
1. Neues GitHub-Repo anlegen, den Inhalt dieses Ordners hochladen.
2. Tab *Actions* → Workflow *build* → Artefakt `talkingskin-jar` herunterladen.

**Variante B: lokal**
1. JDK 25 und Gradle (aktuell, 9.x) installieren.
2. Im Projektordner `gradle build` ausführen. (Mit installiertem Gradle einmalig `gradle wrapper`, dann geht auch `./gradlew build`.)
3. Die Datei `build/libs/talkingskin-1.0.0+mc26.3.jar` ist die Mod. **Nicht** die `-sources.jar`.

Passt eine Version nicht (Loom, Loader, Fabric API), die aktuellen Werte stehen auf
https://fabricmc.net/develop/ und kommen in `gradle.properties`.

## Auf Modrinth hochladen
1. modrinth.com → *Create a project* → Typ *Mod*. Beschreibung aus `modrinth/DESCRIPTION.md`, Icon `modrinth/icon.png`.
2. Environment: *Client*. Lizenz: MIT (vorher in `LICENSE` und `fabric.mod.json` „DEIN NAME“ ersetzen).
3. *Versions* → *Create version*: die `.jar` hochladen, Loader **Fabric**, Spielversion **26.3**.
4. Abhängigkeiten: *Fabric API* (required), *Simple Voice Chat* (required oder optional).

## Einstellungen
Datei `.minecraft/config/talkingskin.json` (wird beim ersten Start angelegt):
`lips`, `teeth`, `gestures`, `bodyMovement`, `alwaysShowLips`, `showOnSelf`,
`lipThickness` (0.3–3), `gestureStrength` (0–3) und die Farben `lipColor`, `lipOutlineColor`, `mouthColor`, `teethColor`.

## Aufbau
| Datei | Aufgabe |
|---|---|
| `TalkingSkinVoicechatPlugin` | Simple-Voice-Chat-Plugin: liest Mikrofon und empfangene Stimmen |
| `TalkTracker` | Lautstärke pro Spieler, glättet Öffnen/Schließen |
| `mixin/AvatarRendererMixin` | kopiert die Daten beim „Extract“ in den RenderState |
| `LipsLayer` | zeichnet Lippen, Mundinneres und Zähne am Kopf |
| `mixin/PlayerModelMixin` | Kopf-/Körper-/Armbewegung |
