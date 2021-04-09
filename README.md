# ClaimsGUI
Manage your ProtectionStone claim using in-game GUI.

## Commands & Permissions
- `/teren - skydistrict.teren`
- `/teren <player> - skydistrict.teren.others`

## Building

To build, run `mvn install` or `mvn clean install`.  
Jar file named `ClaimsGUI-[VERSION].jar` will be placed in `../build/`

## TO-DO
- [ ] Code cleanup and missing comments 
- [ ] Test in search of bugs and exploits
- [ ] (Optional) Add configuration file and reload command
- [ ] (Optional) Add `/teren <player>` for administrators
- [ ] (Optional) Add support for multiple regions
- [x] Teleport actions (with region browser)
- [x] Make GUIs work with sections (in OOP friendly way)
- [x] Section: MAIN
- [x] Section: ADD (paginated)
- [x] Section: REMOVE
- [x] Section: FLAGS (with working click-to-next feature)
- [x] Section: UPGRADE
- [x] Subsections: SETTINGS
- [x] Rewrite of MEMBERS subsection (integration with REMOVE)


