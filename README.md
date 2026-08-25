# ArmorAbilities

A Paper plugin that grants unique abilities to trimmed armor sets in Minecraft.

Each armor type (leather, iron, gold, chainmail, diamond, netherite, etc.) can be assigned an ability via anvil renaming. When a player wears a matching set, they gain special powers.

## Abilities

| Ability | Effect | Full Set Bonus |
|---------|--------|----------------|
| **Moon** | Jump Boost | No fall damage at level 4 |
| **Speed** | Speed + Haste | Full haste at level 4 |
| **Scuba** | Water Breathing, Night Vision, Haste (underwater) | Full underwater kit at level 4 |
| **Miner** | Haste + Night Vision | — |
| **Lava** | Fire Resistance (in lava) | No fire damage at level 4 |
| **Peace** | Mobs won't target you | — |
| **Creeper** | Explode on death | — |
| **Rage** | Lightning retaliation when hit | — |
| **Vampire** | Heal on hit | — |
| **Assassin** | Extra damage while sneaking | — |
| **Spider** | Climb walls | — |

## Requirements

- **Minecraft**: 26.2+ (Paper server)
- **Java**: 25 or newer
- **Paper API**: 26.2

## Building from Source

```bash
# Clone the repository
git clone https://github.com/monsterwhat/ArmorAbilities.git
cd ArmorAbilities

# Build with Gradle (Java 25 is auto-provisioned via Foojay)
./gradlew clean build
```

The built jar will be in `build/libs/armorabilities-6.1.0.jar`.

## Installation

1. Download the latest release jar from [Releases](https://github.com/monsterwhat/ArmorAbilities/releases)
2. Place the jar in your server's `plugins/` directory
3. Restart or reload the server

## Configuration

Abilities are configured via `config.yml` in the plugin's data folder. Each ability can be enabled/disabled and its parameters tuned.

## Permissions

| Permission | Description | Default |
|-----------|-------------|---------|
| `armorabilities.*` | All abilities | op |
| `armorabilities.jump` | Moon ability | true |
| `armorabilities.speed` | Speed ability | true |
| `armorabilities.scuba` | Scuba ability | true |
| `armorabilities.miner` | Miner ability | true |
| `armorabilities.lavaswim` | Lava ability | true |
| `armorabilities.peace` | Peace ability | true |
| `armorabilities.creeper` | Creeper ability | op |
| `armorabilities.rage` | Rage ability | op |
| `armorabilities.vampire` | Vampire ability | true |
| `armorabilities.spider` | Spider ability | true |
| `armorabilities.assassin` | Assassin ability | true |

## License

[MIT](LICENSE)
