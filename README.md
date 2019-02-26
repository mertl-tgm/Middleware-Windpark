# Middleware Windpark

## Dokumentation

Eine genauere Dokumentation zu den einzelnen Projekten ist in dem entsprechenden PDF Dokumenten zu entnehmen.

## Überblick

Das Projekt besteht aus 3 Teilprojekte, welche aufeinander aufbauen. Es handelt sich dabei um eine vereinfachte Steuerungssoftware für Windparks.

**DEZSYS-L04:** Ein Windpark besteht aus mehreren Windräder, alle Informationen dieser wird bei einem zentralen Parkrechner in einer XML Datei gespeichert. Als Übertragungsart wird XML-RPC verwendet.

**DEZSYS-L05:** Mehrere Parkrechner senden mittels Message Oriented Middleware die Informationen an eine zentrale Stelle.

**DEZSYS-L06:** Von der zentralen Stelle aus soll mittels Remote Method Invocation die einzelnen Windräder der Windparks gesteuert werden können.

**Skizze:**
![Test](https://raw.githubusercontent.com/mertl-tgm/Middleware_Windpark/master/windpark_grafik.jpg)


