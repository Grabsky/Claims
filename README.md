# ClaimsGUI
Manage your ProtectionStone claim using in-game GUI.

## Building
To build, run `mvn install` or `mvn clean install`.  
Jar file named `ClaimsGUI-[VERSION].jar` will be placed in `../build/`

## Commands
- `/claim` - `skydistrict.claims`
- `/teren <player>` - `skydistrict.claims.others`
## Permissions
- `skydistrict.claims.bypass.upgradecost`

## TO-DO
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

