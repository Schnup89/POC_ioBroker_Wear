Anmerkung: Der Ansatz per SimpleAdapter war falsch. -> Es wird versucht mit SocketIO die Kommunikation schöner zu lösen

# DEPRECATED
Hier die neue App: https://github.com/Schnup89/ioBroker_WearV2  




# Android WearOS App - ioBroker "VIS"
  
![APP](github/20211202_231742.jpg?raw=true "Optional Title")
![APP](github/20211202_231755.jpg?raw=true "Optional Title")

! Installation aktuell nur per Sideload möglich, keine PlayStore integration
  
Ich stelle euch hier meinen Source-Code und die APK Datei für Android WearOS Smartwatches zur Verfügung.  
Nachdem ich mir die Galaxy Watch4 zugelegt habe hatte ich das Bedürfnis Lampen und sonstige Dinge aus dem ioBroker mit der Uhr zu schalten und zu prüfen.  
  
Ich habe die APP für mich selbst entwickelt, ich bin Hobby-Programmierer und weit davon entfernt ressourcensparend oder schönen Code zu generieren :)  ![20211202_231742](https://user-images.githubusercontent.com/28166743/144513702-75f38e54-e27a-454d-b7ef-3884560a52f9.jpg)

  
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
  
0_userdata.0.wearos  
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

Der fertige JSON-Code hier reinkopieren http://jsonviewer.stack.hu/ und "remove whitespaces" ausführen. Dann den Code in das Objekt 0_userdata.0.wearos schreiben.


Verfügare "Types":  
"type": "text"   - Zeigt nur einen Text an, icon_on icon_off oder readonly wird ignoriert.  
"type": "toggle" - Zeigt den status ON/OFF an und setzt den Status per Klick wenn readonly = false.  

Verfügbare "Icons":
https://github.com/Schnup89/POC_ioBroker_Wear/tree/main/app/src/main/res/drawable - Alle Icons beginnend mit "icon".  

Beim starten der App unter "Einstellungen" kann den Host und den Port vom SimpleAPI-Adapter.  

![APP](github/20211202_231813.jpg?raw=true "Optional Title")

## APK installieren
https://youtu.be/8HsfWPTFGQI

## Schnelltaste
Damit die APP schnell geöffnet werden habe ich dies auf "Doppelklick" der Uhr-Taste gelegt.  
Einstellungen - Erweiterte Funktionen - Anpassen von Tasten > Hier kann die App verlinkt werden
