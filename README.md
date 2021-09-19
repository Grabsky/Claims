# Claims
Create protected region and manage it using in-game GUI. Limited to 1 claim per user with no exceptions due to (intentional) plugin design. Requires [WorldEdit](https://github.com/EngineHub/WorldEdit) and [WorldGuard](https://github.com/EngineHub/WorldGuard) to work properly.

*Due to light-weight checks, by default players can override any region that is not a claim. Please increase priority of non-claim regions to protect them from being griefed.*

*You shouldn't modify claim regions manually unless you know what you're doing. Severe issues may appear.*

## Building
To build, run `mvn install` or `mvn clean install`.

## Permissions
#### Safe to be given to players:
Permission | Description
--- | ---
`claims.command.claims` | Use `/claims` command to manage own claim.
`claims.plugin.place` | Create a protected claim.
`claims.plugin.destroy` | Destroy (own) claim.

#### Recommended only for admins:
Permission | Description
--- | ---
`claims.command.claims.edit` | Use `/claims edit <player>` command to manage their claims.
`claims.command.claims.get` | Use `/claims get` command to get claim blocks and upgrade crystal.
`claims.command.claims.fix` | Use `/claims fix` command to place hidden claim block.
`claims.command.claims.reload` | Use `/claims reload` command to reload plugin configuration.
`claims.bypass.ownercheck` | Bypass owner check. (eg. when destroying not owned region)
`claims.bypass.teleportdelay` | Bypass teleport delay.
`claims.bypass.upgradecost` | Bypass upgrade cost.
`claims.plugin.displayallclaims` | Plugin will show all claims in "accessible claim list" view.
