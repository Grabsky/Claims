# ClaimsGUI
Manage your ProtectionStone claim using in-game GUI.

## Commands & Permissions
- `/teren - skydistrict.teren`
- `/teren <player> - skydistrict.teren.others`

## Building

To build, run `mvn install` or `mvn clean install`.  
Jar file named `ClaimsGUI-[VERSION].jar` will be placed in `../build/`

## TO-DO
- [x] Make GUIs work with sections (in OOP friendly way)
- [ ] Add `/teren <player>` for administrators (?)
- [ ] Add support for multiple regions (?)
- [x] Section: MAIN
- [x] Section: ADD (paginated)
- [x] Section: REMOVE
- [ ] Section: FLAGS (with working click-to-next feature)
- [ ] Section: UPGRADE
- [x] Subsections: MEMBERS, SETTINGS
- [x] Rewrite of MEMBERS subsection (integration with REMOVE)
