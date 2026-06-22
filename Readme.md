# Administrador de Itinerarios de Vuelo — Bolivia

Aplicación de consola en **Java** que administra itinerarios de vuelos **nacionales** (entre departamentos de Bolivia) e **internacionales** (de Bolivia a otros países), modelando la red aérea como un **grafo dirigido y ponderado** y resolviendo consultas con algoritmos de **caminos mínimos**.

> Proyecto académico para la materia de **Teoría de Grafos**.

---

## 📑 Tabla de contenidos

- [Descripción](#-descripción)
- [Conceptos de grafos aplicados](#-conceptos-de-grafos-aplicados)
- [Estructura del proyecto](#-estructura-del-proyecto)
- [Requisitos](#-requisitos)
- [Compilar y ejecutar](#-compilar-y-ejecutar)
- [Menú y uso](#-menú-y-uso)
- [Reglas de negocio](#-reglas-de-negocio)
- [Datos del grafo](#-datos-del-grafo)
- [Ejemplos de prueba](#-ejemplos-de-prueba)
---

## 📝 Descripción

El sistema permite consultar la mejor ruta entre dos aeropuertos según distintos objetivos (costo, tiempo o espera), y reprogramar un vuelo cuando ocurre un problema. Todo funciona **por consola**, mediante un menú y entrada por teclado, sin interfaz gráfica.

La red se representa así:

| Elemento del grafo | En el dominio del problema |
|--------------------|----------------------------|
| **Vértice** | Aeropuerto (un departamento de Bolivia o un país) |
| **Arista** | Vuelo directo entre dos aeropuertos |
| **Camino** | Un itinerario completo |
| **Arista única** | Vuelo **directo** |
| **Camino de varias aristas** | Vuelo **con escala**  |

---

## 🔗 Conceptos de grafos aplicados

| Escenario | Algoritmo | Peso / criterio |
|-----------|-----------|-----------------|
| **Minimizar costo** | Dijkstra | Costo efectivo del vuelo (Bs) |
| **Minimizar tiempo** | Relajación tipo Bellman-Ford (*label-correcting*) | Llegada más temprana, respetando horarios |
| **Minimizar tiempo de espera** | Relajación tipo Bellman-Ford | Espera acumulada en las escalas |
| **Reprogramación** | Recálculo del camino tras eliminar una arista | Costo + penalización |

Las búsquedas que consideran horarios solo toman vuelos que **salen después** del momento de planificación (no se puede abordar un vuelo que ya despegó). Por eso una ruta con escala que sale hoy puede ganarle a un vuelo directo que recién sale días después.

---

## 📁 Estructura del proyecto

Un archivo por clase (paquete por defecto):

```
.
├── AdministradorVuelos.java   # Clase principal: método main
├── Aeropuerto.java            # Vértice del grafo
├── Vuelo.java                 # Arista del grafo (vuelo directo)
├── Itinerario.java            # Resultado: camino encontrado + métricas
├── Grafo.java                 # El grafo y los algoritmos de caminos mínimos
├── Sistema.java               # Menú, entrada por teclado y carga de datos
└── README.md
```

---

## ⚙️ Requisitos

- **JDK 8 o superior** (probado con OpenJDK 21).
- No usa librerías externas.

---

## ▶️ Compilar y ejecutar

Con todos los archivos `.java` en la misma carpeta:

```bash
javac *.java
java AdministradorVuelos
```

---

## 🖥️ Menú y uso

```
1. Mostrar aeropuertos (vertices)
2. Mostrar vuelos (aristas)
3. Itinerario: MINIMIZAR COSTO
4. Itinerario: MINIMIZAR TIEMPO
5. Itinerario: MINIMIZAR TIEMPO DE ESPERA (escalas)
6. Reprogramar vuelo por problema (interno/externo)
7. Cambiar hora de planificacion
0. Salir
```

- Los aeropuertos se eligen por su **código** (ej. `CBB`, `VVI`, `MAD`). Usa la opción **1** para verlos.
- La **hora de planificación** (por defecto *Lunes 06:00*) es el "ahora" desde el cual se buscan los vuelos; afecta a las opciones **4** y **5**. Se cambia con la opción **7**.

---

## 💼 Reglas de negocio

| Regla | Detalle |
|-------|---------|
| **Promoción (rebaja)** | Si la ocupación del vuelo es menor al 50 %, se aplica un descuento del **15 %** sobre el costo. |
| **Capacidad / ocupación** | Un vuelo lleno (ocupación = capacidad) no se ofrece. |
| **Conexión mínima** | Entre escalas deben pasar al menos **45 minutos**. |
| **Penalización interna** | Problema por error de la empresa (no atribuido al usuario) → **Bs 0** (la empresa asume). |
| **Penalización externa** | Causa externa → **20 % del costo del vuelo cancelado**, sumado al itinerario reprogramado. |

---

## 🌎 Datos del grafo

**Aerolíneas:** BoA (Boliviana de Aviación) y EcoJet. **Hub doméstico:** Cochabamba (CBB). Los vuelos internacionales salen principalmente de Santa Cruz (VVI), por lo que ir, por ejemplo, de Sucre a Madrid exige una escala.

**Aeropuertos nacionales**

| Código | Ciudad | Departamento |
|--------|--------|--------------|
| LPB | La Paz | La Paz |
| CBB | Cochabamba | Cochabamba |
| VVI | Santa Cruz | Santa Cruz |
| SRE | Sucre | Chuquisaca |
| TJA | Tarija | Tarija |
| TDD | Trinidad | Beni |
| CIJ | Cobija | Pando |
| ORU | Oruro | Oruro |
| POI | Potosí | Potosí |
| UYU | Uyuni | Potosí |

**Destinos internacionales**

| Código | Ciudad | País |
|--------|--------|------|
| EZE | Buenos Aires | Argentina |
| GRU | São Paulo | Brasil |
| LIM | Lima | Perú |
| MIA | Miami | EE.UU. |
| MAD | Madrid | España |

Los datos se cargan en el método `cargarDatosReales()` de `Sistema.java`. Los horarios, precios y ocupaciones son **representativos** (no son el calendario en tiempo real de las aerolíneas), elegidos para ilustrar con claridad cada escenario.

*Fuentes consultadas para rutas y destinos reales:* páginas de Boliviana de Aviación y EcoJet, y agregadores de rutas aéreas.

---

## 🧪 Ejemplos de prueba

> Cada línea representa una entrada seguida de **Enter**.

**1. Vuelo con escala** — no hay directo internacional desde Sucre:
```
3
SRE
MAD
```
➡️ Escala `SRE → VVI → MAD`, costo Bs 7800.

**2. Minimizar tiempo** — la escala que sale antes le gana al directo posterior:
```
4
CBB
EZE
```
➡️ `CBB → VVI → EZE`, llegada más temprana.

**3. Minimizar espera** — mismo par, resultado distinto:
```
5
CBB
EZE
```
➡️ Directo `CBB → EZE` (espera 0). Comparar con el ejemplo 2 muestra que cada criterio puede dar un camino diferente.

**4. Reprogramación externa** — cancelas el directo y te reencamina con penalización:
```
6
CBB
VVI
1
2
```
➡️ Reprograma por `CBB → SRE → VVI` y suma la penalización (20 % de Bs 520 = Bs 104).

**5. Cambiar hora de planificación** — la hora altera la ruta óptima:
```
7
0
7
30
4
CBB
EZE
```
➡️ Como el vuelo `CBB → VVI` de las 07:00 ya salió, el algoritmo reencamina por Trinidad (`CBB → TDD → VVI → EZE`).

---


## 👤 Autor

- *Bermudez Torrico Viviana Laura*

Materia: **Teoría de Grafos** — Universidad Mayor de San Simón (UMSS), Cochabamba, Bolivia.
