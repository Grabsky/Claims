# Claims
Create protected region and manage it using in-game GUI. Limited to 1 claim per user with no exceptions due to (intentional) plugin design. Requires [WorldEdit](https://github.com/EngineHub/WorldEdit) and [WorldGuard](https://github.com/EngineHub/WorldGuard) to work properly.

*Due to light-weight checks, by default players can override any region that is not a claim. Please increase priority of non-claim regions to protect them from being griefed.*

*You shouldn't modify claim regions manually unless you know what you're doing. Severe issues may appear.*

## Running
### Supported server software
Server | Version
--- | ---
[Paper](https://github.com/PaperMC/Paper) (and forks...) | 1.18 | https://papermc.io/downloads

### External dependencies
Dependency | Required
--- | ---
[Indigo](https://github.com/Grabsky/Indigo) | Yes
[Vanish](https://github.com/Grabsky/Vanish) | No

## Building
To build, run `gradle build` or `gradle clean build`.

## Permissions
#### Safe to be given to players:
Permission | Description | Admin*
--- | --- | ---
`claims.command.claims` | Use `/claims` command to manage own claim. | No
`claims.plugin.place` | Create a protected claim. | No
`claims.plugin.destroy` | Destroy (own) claim. | No
`claims.command.claims.edit` | Use `/claims edit <player>` command. | Yes
`claims.command.claims.get` | Use `/claims get` command. | Yes
`claims.command.claims.fix` | Use `/claims fix` command. | Yes
`claims.command.claims.reload` | Use `/claims reload` command. | Yes
`claims.bypass.ownercheck` | Bypass owner checks. | Yes
`claims.bypass.teleportdelay` | Bypass teleport delays. | Yes
`claims.bypass.upgradecost` | Bypass upgrade costs. | Yes
`claims.plugin.displayallclaims` | View all claims in "accessible claims" list. | Yes

\* Recommended only for players with admin rights