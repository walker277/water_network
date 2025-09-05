# Vizualizace vodovodní sítě

**Popis:**  
Program vizualizuje vodovodní síť a umožňuje sledovat její prvky, průtok vody a zaplnění rezervoárů v čase. Podporuje interaktivní ovládání a změnu parametrů během běhu simulace.

---

## Požadavky
- Java 8 a vyšší
---

## Funkce programu

1. **Základní vizualizace**
   - Výchozí okno 800x600 px
   - Vodovodní síť zabírá maximální prostor s nezkreslenými souřadnicemi
   - Obnovování sítě v čase
   - Dynamické přizpůsobení při změně velikosti okna
   - Možnost měnit velikost grafických prvků (`glyphSize`) během běhu programu
   - Ovládání rychlosti simulace (zrychlení/zpomalení)

2. **Interaktivní vizualizace**
   - Kliknutí na rezervoár zobrazí graf zaplnění rezervoáru v čase
   - Kliknutí na potrubí zobrazí graf rychlosti proudění vody v čase
   - Grafy plně využívají velikost okna a automaticky se přizpůsobují jeho změnám
   - Ventily umožňují ovládání myší (např. táhlo, vlastní „radiobutton“)

3. **Volitelné rozšíření**
   - Velikost rezervoárů odpovídá jejich kapacitě, největší rezervoár má velikost odpovídající parametru `glyphSize`, ostatní jsou proporcionálně menší

## Spuštění

### Linux / MacOS
bash Run.sh

### Windows
.\Run.cmd (Windows Powershell)

***Další informace o projektu jsou dostupne v dokumentaci doc/dokumentace.pdf***
