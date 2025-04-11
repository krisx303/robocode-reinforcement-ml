# Projekt: Uczenie Maszynowe w Robocode

## Anna Nowacka, Krzysztof Usnarski

## Cel projektu

Celem projektu jest stworzenie bota do gry **Robocode**, który wykorzystuje **uczenie ze wzmocnieniem (Reinforcement Learning)** do poprawy swoich umiejętności bojowych. Bot powinien nauczyć się optymalnych strategii poruszania się i atakowania poprzez analizę wyników wcześniejszych starć.

## Model uczenia maszynowego

Aby umożliwić efektywne uczenie bota, konieczne jest zdefiniowanie:

### Przestrzeni stanów

Każdy stan będzie reprezentował sytuację na arenie z punktu widzenia bota. W celu uproszczenia i zmniejszenia wymagań obliczeniowych, przestrzeń stanów będzie kwantyzowana do następujących kategorii:

- **Odległość do przeciwnika:**
  - Bliska
  - Średnia
  - Daleka
- **Kąt względny do przeciwnika:**
  - Front
  - Bok
  - Tył
- **Odległość od ściany:**
  - Bliska
  - Średnia
  - Daleka 
- **Prędkość bota:**
  - Stoi w miejscu
  - Porusza się wolno
  - Porusza się szybko
- **Stan energii:**
  - Wysoki (powyżej 50%)
  - Średni (od 20% do 50%)
  - Niski (poniżej 20%)

### Zbioru akcji

Bot może podejmować następujące decyzje:

- **Ruch:**
  - Przesunięcie się do przodu o stałą wartość
  - Przesunięcie się do tyłu
  - Obrót o stały kąt
- **Działania ofensywne:**
  - Strzał o niskiej mocy
  - Strzał o średniej mocy
  - Strzał o dużej mocy
- **Obrót wieżyczki:**
  - Obrót w lewo
  - Obrót w prawo

### Funkcja nagrody

Aby bot mógł efektywnie się uczyć, należy zdefiniować system nagród i kar:

| Działanie                      | Nagroda/Kara |
| ------------------------------ | ------------ |
| Trafienie przeciwnika          | +1           |
| Otrzymanie trafienia           | -1           |
| Strzał, który nie trafił       | -0.2         |
| Wygrana rundy                  | +5           |
| Przegrana rundy                | -5           |
| Kolizja ze ścianą              | -1           |
| Kolizja z przeciwnikiem        | -0.5         |
| Kara za turę                   | -0.001       |

## Wykorzystane technologie

Do implementacji rozwiązania wykorzystamy:

- **Robocode API** – pozwala na kontrolowanie bota i analizę stanu gry.
- **Q-learning** – metoda uczenia ze wzmocnieniem do aktualizacji strategii bota.
- **Tablica wartości Q (Q-Table)** – do przechowywania wartości przypisanych do par stan-akcja.
- **Strategia ε-zachłanna** – zapewnia balans między eksploracją a eksploatacją strategii.
- **Java** – do implementacji bota w Robocode i sieci neuronowej.

## Plan implementacji

1. Stworzenie bota w **Robocode**, który obsługuje podstawowe akcje.
2. Implementacja systemu przechowywania stanów i akcji.
3. Zaimplementowanie algorytmu **Q-learning** do aktualizacji wartości Q.
4. Testowanie bota na różnych przeciwnikach w trybie symulacyjnym.
5. Analiza wyników i dostrojenie parametrów nagród oraz wartości ε.
6. Możliwość zapisu i odczytu modelu (aby bot mógł kontynuować naukę w kolejnych sesjach).

## Możliwe ulepszenia

- Zamiast tablicy Q zastosowanie **sieci neuronowej (Deep Q-Learning)** do uczenia bardziej skomplikowanych strategii.
- Dostosowanie wartości funkcji nagrody w zależności od statystyk bota.
- Analiza skuteczności strategii poprzez zestawienie z różnymi botami przeciwników.
- Możliwość implementacji **policy gradient methods**, które mogą lepiej sprawdzać się w sytuacjach wymagających dynamicznego podejmowania decyzji.

## Podsumowanie

Projekt zakłada stworzenie bota w Robocode, który wykorzystuje uczenie ze wzmocnieniem do poprawy swoich umiejętności. Poprzez odpowiednie modelowanie przestrzeni stanów, akcji oraz systemu nagród, bot powinien być w stanie stopniowo poprawiać swoje wyniki w pojedynkach. W miarę rozwoju projektu można zastosować bardziej zaawansowane metody uczenia, takie jak Deep Q-Learning czy algorytmy policy gradient.
