# Claims
Manage your ProtectionStone claim using in-game GUI.

## Building
To build, run `mvn install` or `mvn clean install`.  
Jar file named `Claims-[VERSION].jar` will be placed in `../build/`


## Permissions
#### Safe to be given to players:
Permission | Description
--- | ---
`skydistrict.claims.panel` | Using `/claim` command.
`skydistrict.claims.place` | Creating a protected region by placing claim block.
`skydistrict.claims.destroy` | Removing a protected region by destroying claim block. (only owner)
`skydistrict.claims.flags` | Managing claim flags.
`skydistrict.claims.members` | Managing claim members.
`skydistrict.claims.upgrade` | Upgrading the claim.

#### Recommended only for admins:
Permission | Description
--- | ---
`skydistrict.claims.panel.others` | Using `/claim <player>` command.
`skydistrict.claims.bypass.teleportdelay` | Bypass teleport delay.
`skydistrict.claims.bypass.upgradecost` | Bypass upgrade cost.

## TO-DO
- [ ] Re-implement core features (creating/removing claims)
- [ ] Re-implement panel (using new API)
- [ ] Test in search of bugs and exploits
- [ ] Add messages.yml
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
- [x] (Optional) Add `/teren <player>` for administrators

