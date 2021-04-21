# Claims
Create protected region and manage it using in-game GUI. Limited to 1 claim per user with no exceptions due to intentional design.

*You shouldn't modify claim regions manually unless you know what you're doing. Weird issues may appear.*

## Building
To build, run `mvn install` or `mvn clean install`.  
Jar file named `Claims-[VERSION].jar` will be placed in `../build/`

## Permissions
#### Safe to be given to players:
Permission | Description
--- | ---
`skydistrict.claims.panel` | Using `/claim` command.
`skydistrict.claims.place` | Creating a claim by placing claim block.
`skydistrict.claims.destroy` | Removing (own) claim by removing claim block.
`skydistrict.claims.flags` | Managing claim flags.
`skydistrict.claims.members` | Managing claim members.
`skydistrict.claims.upgrade` | Upgrading the claim.

#### Recommended only for admins:
Permission | Description
--- | ---
`skydistrict.claims.panel.others` | Using `/claim <player>` command.
`skydistrict.claims.destroy.others` | Removing protected region of other players.
`skydistrict.claims.bypass.teleportdelay` | Bypass teleport delay.
`skydistrict.claims.bypass.upgradecost` | Bypass upgrade cost.


## TO-DO
- [ ] Test in search of bugs and exploits
  - [ ] Cache (upgrading/adding/removing)
  - [ ] Claim (creating/removing/upgrading/protection)
  - [ ] Claim flags (including default flags)
  - [ ] Command (functionality and permissions)
  - [ ] Claim upgrades (block type)
  - [ ] ... and everything I forgot to mention
- [ ] (Optional) Add (basic) config.yml
- [ ] (Optional) Add (chat) messages.yml
- [x] Re-implement core features (creating/removing claims)
- [x] Re-implement panel (using new API)
- [x] Add `*` character to the title when editing someone's claim
- [x] Code cleanup and missing comments
- [x] Teleport actions (with region browser)
- [x] Make GUIs work with sections (in OOP friendly way)
- [x] Section: MAIN
- [x] Section: ADD (paginated)
- [x] Section: REMOVE
- [x] Section: FLAGS (with working click-to-next feature)
- [x] Section: UPGRADE
- [x] Subsections: SETTINGS
- [x] Rewrite of MEMBERS subsection (integration with REMOVE)
- [x] (Optional) Add `/claim <player>` for administrators

