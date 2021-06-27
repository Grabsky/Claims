#### DISCLAIMER: Resource has been archived due to lack of interest in Minecraft-related development.

# Claims
Create protected region and manage it using in-game GUI. Limited to 1 claim per user with no exceptions due to (intentional) plugin design.

*Due to light-weight checks, by default players can override any region that is not a claim. Please increase priority of non-claim regions to protect them from being griefed.*

*You shouldn't modify claim regions manually unless you know what you're doing. Severe issues may appear.*

## Building
To build, run `mvn install` or `mvn clean install`.

## Permissions
#### Safe to be given to players:
Permission | Description
--- | ---
`skydistrict.command.claims` | Use `/claim` command to manage own claim.
`skydistrict.plugin.claims.place` | Create a protected claim.
`skydistrict.plugin.claims.destroy` | Destroy (own) claim.

#### Recommended only for admins:
Permission | Description
--- | ---
`skydistrict.command.claims.others` | Use `/claim <player>` command to manage their claims.
`skydistrict.command.claims.fix` | Use `/claim fix` command to place hidden claim block.
`skydistrict.command.claims.reload` | Use `/claim reload` command to reload plugin configuration.
`skydistrict.bypass.claims.ownercheck` | Bypass owner check. (eg. when destroying not owned region)
`skydistrict.bypass.claims.teleportdelay` | Bypass teleport delay.
`skydistrict.bypass.claims.upgradecost` | Bypass upgrade cost.
