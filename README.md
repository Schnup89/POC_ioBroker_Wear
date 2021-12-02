# Android WearOS App - ioBroker "VIS"
  
Ich stelle euch hier meinen Source-Code und die APK Datei für Android WearOS Smartwatches zur Verfügung.  
Nachdem ich mir die Galaxy Watch4 zugelegt habe hatte ich das Bedürfnis Lampen und sonstige Dinge aus dem ioBroker mit der Uhr zu schalten und zu prüfen.  
  
Ich habe die APP für mich selbst entwickelt, ich bin Hobby-Programmierer und weit davon entfernt ressourcensparend oder schönen Code zu generieren :)  
  
## Installation / Voraussetzung
  
Simple-API Adapter  
https://github.com/ioBroker/ioBroker.simple-api  
Dort unter "Einstellungen" "Alle Datenpunkte auflisten" aktivieren. 

JSON-Datenpunkt anlegen: 0_userdata.0.wearos
```
{
  "common": {
    "name": "wearos",
    "desc": "Manuell erzeugt",
    "role": "state",
    "type": "json",
    "read": true,
    "write": true
  },
  "type": "state",
  "from": "system.adapter.admin.0",
  "user": "system.user.admin",
  "ts": 1637865967360,
  "_id": "0_userdata.0.wearos"
}
```

Dieser Datenpunkt wird von der App abgefragt und die Liste daraus erstellt.  
  
## Konfiguration
  
```
{
  "states": [
    {
      "name": "Wzm Lampe",
      "type": "toggle",
      "icon_on": "bulb_on",
      "icon_off": "bulb_off",
      "readonly": false,
      "id": "tuya.0.bf9f6e2bf53e96bbc2tkpc.1"
    },
    {
      "name": "Wzm Stehlampe",
      "type": "toggle",
      "icon_on": "bulb_on",
      "icon_off": "bulb_off",
      "readonly": false,
      "id": "admin.0.HAB_btn_so20_Wohnz_Lampe"
    },
    {
      "name": "Bowl",
      "type": "toggle",
      "icon_on": "bulb_on",
      "icon_off": "bulb_off",
      "readonly": false,
      "id": "mqtt.0.cmnd.soBW_PC.POWER"
    },
    {
      "name": "Party EG",
      "type": "toggle",
      "icon_on": "fire_on",
      "icon_off": "fire_off",
      "readonly": false,
      "id": "admin.0.HAB_btn_heiz_party_eg"
    },
    {
      "name": "Party OG",
      "type": "toggle",
      "icon_on": "fire_on",
      "icon_off": "fire_off",
      "readonly": false,
      "id": "admin.0.HAB_btn_heiz_party_og"
    },
    {
      "name": "Temp-Außen",
      "type": "text",
      "icon_on": "bulb_on",
      "icon_off": "bulb_off",
      "readonly": true,
      "id": "mqtt.0.gettempa"
    }
  ]
}
```
  
Verfügare "Types":  
"type": "text"   - Zeigt nur einen Text an, icon_on icon_off oder readonly wird ignoriert.  
"type": "toggle" - Zeigt den status ON/OFF an und setzt den Status per Klick wenn readonly = false.  

Verfügbare "Icons":

