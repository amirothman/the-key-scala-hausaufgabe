# Programmieraufgabe

> Aufgabe:
> 
> Erstelle ein Full Stack System Frontend / Backend mit folgender Aufgabestellung:
> 
> Das Backend ruft zyklisch (alle paar Sekunden) die Blogbeiträge von der Seite internate.org (alternativ thekey.academy) ab (über die Wordpress API - https://developer.wordpress.org/rest-api/reference/posts/)
> 
> Das Backend verarbeitet die Blogbeiträge zu einer einfachen Word Count Map ({“und”: 5, “der”: 3, ...})
> 
> Das Backend sendet nach der Verarbeitung die Map per WebSocket an das Frontend
> 
> Das Frontend zeigt die Word Count Map der neuen Beiträge an und aktualisiert sich selbstständig neu bei neuen Daten.

## Bedienungsanleitung

### Voraussetzungen

[Scala, sbt, sc, etc.](https://www.scala-lang.org/download/)


### Starten

```bash
sbt run
```

auf Port 9000 erreichbar.

### Starten mit einer anderen Wordpress-URL

```bash
sbt run -Dwordpress.api.url=https://thekey.academy/wp-json/wp/v2/posts
sbt run -Dwordpress.api.url=http://localhost:1337/index.json # Für Tests auf dem localhost 
```

### Tests

```bash
sbt test
```
