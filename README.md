# xSkill (Paper 1.21.4)

Config-driven item definitions (IDs under `swords:` in YAML), abilities, cooldowns, and global level/XP.

**Held item:** `/xskill give <id>` and `/xskill set <id>` work on **any non-empty item** in your main hand (not limited to swords). Definitions can still set display name, lore, and model from config.

## Build

```bash
mvn -DskipTests package
```

Jar: `target/xSkill-1.0.0.jar`

## Commands

- `/xskill give <id>` — apply full definition to **held item** (any item)
- `/xskill set <id>` — set definition ID only on **held item** (keeps look)
- `/xskill give <player> <id> [amount]` — give configured items to a player
- `/xskill reload`
- `/xskill stats [player]`
- `/xskill level [player]`

## Config

`src/main/resources/config.yml`
