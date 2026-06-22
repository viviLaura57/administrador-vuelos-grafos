/*
 * Sistema.java
 * Maneja el MENU por consola, la entrada por teclado, la impresion de
 * resultados y la carga de los datos reales (aeropuertos y vuelos).
 */
import java.util.List;
import java.util.Scanner;

public class Sistema {

    private Grafo grafo;
    private Scanner lector;
    private int minutoActual;   // "ahora" desde el que se planifican los viajes

    private static final String[] DIAS = {"Lun", "Mar", "Mie", "Jue", "Vie", "Sab", "Dom"};
    private static final double PORCENTAJE_PENALIZACION = 0.20; // 20% del vuelo cancelado

    public Sistema() {
        this.grafo = new Grafo();
        this.lector = new Scanner(System.in);
        this.minutoActual = construirMinuto(0, 6, 0); // Lunes 06:00 por defecto
        cargarDatosReales();
    }

    /* -------------------------------------------------------------- */
    /*  Bucle principal del menu                                       */
    /* -------------------------------------------------------------- */
    public void iniciar() {
        System.out.println("==================================================");
        System.out.println(" ADMINISTRACION DE ITINERARIOS DE VUELO - BOLIVIA");
        System.out.println(" (Proyecto de Teoria de Grafos)");
        System.out.println("==================================================");
        boolean salir = false;
        while (!salir) {
            mostrarMenu();
            int opcion = leerEntero("Elija una opcion: ");
            if (opcion == 1) {
                mostrarAeropuertos();
            } else if (opcion == 2) {
                mostrarVuelos();
            } else if (opcion == 3) {
                consultarMinimizarCosto();
            } else if (opcion == 4) {
                consultarConHorario(Grafo.CRITERIO_TIEMPO);
            } else if (opcion == 5) {
                consultarConHorario(Grafo.CRITERIO_ESPERA);
            } else if (opcion == 6) {
                reprogramarVuelo();
            } else if (opcion == 7) {
                cambiarHoraPlanificacion();
            } else if (opcion == 0) {
                salir = true;
                System.out.println("\nFin del programa. Buen viaje!");
            } else {
                System.out.println("Opcion no valida.");
            }
        }
    }

    private void mostrarMenu() {
        System.out.println("\n----------------- MENU -----------------");
        System.out.println(" Hora actual de planificacion: " + formatearMinuto(this.minutoActual));
        System.out.println(" 1. Mostrar aeropuertos (vertices)");
        System.out.println(" 2. Mostrar vuelos (aristas)");
        System.out.println(" 3. Itinerario: MINIMIZAR COSTO");
        System.out.println(" 4. Itinerario: MINIMIZAR TIEMPO");
        System.out.println(" 5. Itinerario: MINIMIZAR TIEMPO DE ESPERA (escalas)");
        System.out.println(" 6. Reprogramar vuelo por problema (interno/externo)");
        System.out.println(" 7. Cambiar hora de planificacion");
        System.out.println(" 0. Salir");
        System.out.println("----------------------------------------");
    }

    /* -------------------------------------------------------------- */
    /*  Opcion 1: listar vertices                                      */
    /* -------------------------------------------------------------- */
    private void mostrarAeropuertos() {
        System.out.println("\n== AEROPUERTOS (vertices del grafo) ==");
        List<Aeropuerto> lista = this.grafo.obtenerAeropuertos();
        for (int i = 0; i < lista.size(); i++) {
            System.out.println("  " + lista.get(i).describir());
        }
    }

    /* -------------------------------------------------------------- */
    /*  Opcion 2: listar aristas                                       */
    /* -------------------------------------------------------------- */
    private void mostrarVuelos() {
        System.out.println("\n== VUELOS DIRECTOS (aristas del grafo) ==");
        List<Vuelo> lista = this.grafo.obtenerVuelos();
        for (int i = 0; i < lista.size(); i++) {
            System.out.println("  " + describirVuelo(lista.get(i)));
        }
    }

    private String describirVuelo(Vuelo vuelo) {
        String estado = vuelo.estaCancelado() ? "  [CANCELADO]" : "";
        String promo = vuelo.tienePromocion() ? " *PROMO" : "";
        String texto = vuelo.obtenerNumeroVuelo() + " " + vuelo.obtenerLineaAerea() + ": "
                + vuelo.obtenerOrigen() + " " + formatearMinuto(vuelo.obtenerMinutoSalida())
                + " -> " + vuelo.obtenerDestino() + " " + formatearMinuto(vuelo.obtenerMinutoLlegada())
                + " | Bs " + redondear(vuelo.obtenerCostoEfectivo()) + promo
                + " | asientos " + vuelo.obtenerOcupacion() + "/" + vuelo.obtenerCapacidad()
                + estado;
        return texto;
    }

    /* -------------------------------------------------------------- */
    /*  Opcion 3                                                       */
    /* -------------------------------------------------------------- */
    private void consultarMinimizarCosto() {
        System.out.println("\n== MINIMIZAR COSTO ==");
        String origen = leerCodigoAeropuerto("Aeropuerto de ORIGEN (codigo): ");
        String destino = leerCodigoAeropuerto("Aeropuerto de DESTINO (codigo): ");
        Itinerario itinerario = this.grafo.minimizarCosto(origen, destino);
        imprimirItinerario(itinerario, origen, destino, "menor COSTO", false);
    }

    /* -------------------------------------------------------------- */
    /*  Opciones 4 y 5                                                 */
    /* -------------------------------------------------------------- */
    private void consultarConHorario(int criterio) {
        String titulo;
        if (criterio == Grafo.CRITERIO_TIEMPO) {
            titulo = "MINIMIZAR TIEMPO (llegada mas temprana)";
        } else {
            titulo = "MINIMIZAR TIEMPO DE ESPERA (escalas)";
        }
        System.out.println("\n== " + titulo + " ==");
        System.out.println("Planificando desde: " + formatearMinuto(this.minutoActual));
        String origen = leerCodigoAeropuerto("Aeropuerto de ORIGEN (codigo): ");
        String destino = leerCodigoAeropuerto("Aeropuerto de DESTINO (codigo): ");
        Itinerario itinerario = this.grafo.buscarConHorario(origen, destino, this.minutoActual, criterio);
        String objetivo;
        if (criterio == Grafo.CRITERIO_TIEMPO) {
            objetivo = "menor TIEMPO";
        } else {
            objetivo = "menor ESPERA en escalas";
        }
        imprimirItinerario(itinerario, origen, destino, objetivo, true);
    }

    /* -------------------------------------------------------------- */
    /*  Opcion 6: reprogramacion por problema interno o externo        */
    /* -------------------------------------------------------------- */
    private void reprogramarVuelo() {
        System.out.println("\n== REPROGRAMACION DE VUELO POR PROBLEMA ==");
        String origen = leerCodigoAeropuerto("Aeropuerto de ORIGEN (codigo): ");
        String destino = leerCodigoAeropuerto("Aeropuerto de DESTINO (codigo): ");

        Itinerario original = this.grafo.minimizarCosto(origen, destino);
        System.out.println("\n-- Itinerario original (minimo costo) --");
        imprimirItinerario(original, origen, destino, "menor COSTO", false);

        if (original.existeRuta()) {
            List<Vuelo> tramos = original.obtenerTramos();
            System.out.println("\nTramos del itinerario:");
            for (int i = 0; i < tramos.size(); i++) {
                System.out.println("  " + (i + 1) + ") " + describirVuelo(tramos.get(i)));
            }
            int eleccion = leerEntero("Que tramo presenta el problema? (numero): ");
            if (eleccion >= 1 && eleccion <= tramos.size()) {
                Vuelo afectado = tramos.get(eleccion - 1);
                System.out.println("Tipo de problema:");
                System.out.println("  1) INTERNO  (error de la empresa, no atribuido al usuario)");
                System.out.println("  2) EXTERNO  (causa externa, genera costo extra al pasajero)");
                int tipo = leerEntero("Elija el tipo: ");

                afectado.cancelar(); // se quita la arista problematica del grafo
                Itinerario alterno = this.grafo.minimizarCosto(origen, destino);

                if (tipo == 1) {
                    System.out.println("\nProblema INTERNO: la empresa asume el costo. Penalizacion al usuario: Bs 0");
                } else {
                    double base = afectado.obtenerCostoEfectivo();
                    double penalizacion = base * PORCENTAJE_PENALIZACION;
                    alterno.agregarPenalizacion(penalizacion);
                    System.out.println("\nProblema EXTERNO: penalizacion = " + (int) (PORCENTAJE_PENALIZACION * 100)
                            + "% del vuelo cancelado (Bs " + redondear(base) + ") = Bs " + redondear(penalizacion));
                }

                System.out.println("\n-- Itinerario reprogramado (evitando el tramo cancelado) --");
                imprimirItinerario(alterno, origen, destino, "menor COSTO (reprogramado)", false);

                afectado.reactivar(); // se restaura para futuras consultas
                System.out.println("(El vuelo cancelado se reactivo para proximas consultas.)");
            } else {
                System.out.println("Numero de tramo invalido.");
            }
        }
    }

    /* -------------------------------------------------------------- */
    /*  Opcion 7: cambiar la hora actual de planificacion              */
    /*  (el "ahora" desde el que se planifican los viajes)             */
    /* -------------------------------------------------------------- */
    private void cambiarHoraPlanificacion() {
        System.out.println("\n== CAMBIAR HORA DE PLANIFICACION ==");
        System.out.println("Hora actual: " + formatearMinuto(this.minutoActual));
        int dia = leerEnteroEnRango("Dia (0=Lun, 1=Mar, 2=Mie, 3=Jue, 4=Vie, 5=Sab, 6=Dom): ", 0, 6);
        int hora = leerEnteroEnRango("Hora (0-23): ", 0, 23);
        int minuto = leerEnteroEnRango("Minuto (0-59): ", 0, 59);
        this.minutoActual = construirMinuto(dia, hora, minuto);
        System.out.println("Nueva hora de planificacion: " + formatearMinuto(this.minutoActual));
    }

    /* -------------------------------------------------------------- */
    /*  Impresion de un itinerario resultante                          */
    /* -------------------------------------------------------------- */
    private void imprimirItinerario(Itinerario itinerario, String origen, String destino,
                                    String objetivo, boolean usaHorario) {
        if (!itinerario.existeRuta()) {
            System.out.println("No existe ruta de " + origen + " a " + destino + " con asientos disponibles.");
        } else {
            List<Vuelo> tramos = itinerario.obtenerTramos();
            String tipoRuta;
            if (tramos.size() == 1) {
                tipoRuta = "VUELO DIRECTO";
            } else {
                tipoRuta = "VUELO CON ESCALA (" + itinerario.cantidadEscalas() + " escala/s)";
            }
            System.out.println("Ruta optima por " + objetivo + ": " + tipoRuta);
            for (int i = 0; i < tramos.size(); i++) {
                System.out.println("   Tramo " + (i + 1) + ": " + describirVuelo(tramos.get(i)));
            }
            System.out.println("   ---------------------------------------------");
            System.out.println("   Costo total:  Bs " + redondear(itinerario.obtenerCostoTotal()));
            if (itinerario.obtenerPenalizacion() > 0) {
                System.out.println("   (incluye penalizacion: Bs " + redondear(itinerario.obtenerPenalizacion()) + ")");
            }
            if (usaHorario) {
                System.out.println("   Tiempo total: " + formatearDuracion(itinerario.obtenerTiempoTotal())
                        + " (hasta la llegada final)");
                System.out.println("   Espera total en escalas: " + formatearDuracion(itinerario.obtenerEsperaTotal()));
            }
        }
    }

    /* -------------------------------------------------------------- */
    /*  Lectura por teclado (validada)                                 */
    /* -------------------------------------------------------------- */
    private int leerEntero(String mensaje) {
        int valor = 0;
        boolean valido = false;
        while (!valido) {
            System.out.print(mensaje);
            String linea = this.lector.nextLine().trim();
            try {
                valor = Integer.parseInt(linea);
                valido = true;
            } catch (NumberFormatException error) {
                System.out.println("  Entrada invalida, ingrese un numero entero.");
            }
        }
        return valor;
    }

    private int leerEnteroEnRango(String mensaje, int minimo, int maximo) {
        int valor = minimo;
        boolean valido = false;
        while (!valido) {
            valor = leerEntero(mensaje);
            if (valor >= minimo && valor <= maximo) {
                valido = true;
            } else {
                System.out.println("  Debe estar entre " + minimo + " y " + maximo + ".");
            }
        }
        return valor;
    }

    private String leerCodigoAeropuerto(String mensaje) {
        String codigo = "";
        boolean valido = false;
        while (!valido) {
            System.out.print(mensaje);
            codigo = this.lector.nextLine().trim().toUpperCase();
            if (this.grafo.existeAeropuerto(codigo)) {
                valido = true;
            } else {
                System.out.println("  Codigo inexistente. Use la opcion 1 para ver los codigos.");
            }
        }
        return codigo;
    }

    /* -------------------------------------------------------------- */
    /*  Utilidades de tiempo y formato                                 */
    /* -------------------------------------------------------------- */
    private int construirMinuto(int dia, int hora, int minuto) {
        int total = dia * 1440 + hora * 60 + minuto;
        return total;
    }

    private String formatearMinuto(int minutoTotal) {
        int dia = (minutoTotal / 1440) % 7;
        int restante = minutoTotal % 1440;
        int hora = restante / 60;
        int minuto = restante % 60;
        String texto = DIAS[dia] + " " + dosDigitos(hora) + ":" + dosDigitos(minuto);
        return texto;
    }

    private String formatearDuracion(int minutos) {
        int horas = minutos / 60;
        int resto = minutos % 60;
        String texto = horas + "h " + dosDigitos(resto) + "m";
        return texto;
    }

    private String dosDigitos(int numero) {
        String texto;
        if (numero < 10) {
            texto = "0" + numero;
        } else {
            texto = "" + numero;
        }
        return texto;
    }

    private String redondear(double valor) {
        long entero = Math.round(valor);
        String texto = "" + entero;
        return texto;
    }

    /* -------------------------------------------------------------- */
    /*  CARGA DE DATOS REALES (aeropuertos y rutas de Bolivia)         */
    /* -------------------------------------------------------------- */
    private void cargarDatosReales() {
        // ---- Vertices nacionales (un aeropuerto por departamento) ----
        this.grafo.agregarAeropuerto(new Aeropuerto("LPB", "La Paz", "La Paz", true));
        this.grafo.agregarAeropuerto(new Aeropuerto("CBB", "Cochabamba", "Cochabamba", true));
        this.grafo.agregarAeropuerto(new Aeropuerto("VVI", "Santa Cruz", "Santa Cruz", true));
        this.grafo.agregarAeropuerto(new Aeropuerto("SRE", "Sucre", "Chuquisaca", true));
        this.grafo.agregarAeropuerto(new Aeropuerto("TJA", "Tarija", "Tarija", true));
        this.grafo.agregarAeropuerto(new Aeropuerto("TDD", "Trinidad", "Beni", true));
        this.grafo.agregarAeropuerto(new Aeropuerto("CIJ", "Cobija", "Pando", true));
        this.grafo.agregarAeropuerto(new Aeropuerto("ORU", "Oruro", "Oruro", true));
        this.grafo.agregarAeropuerto(new Aeropuerto("POI", "Potosi", "Potosi", true));
        this.grafo.agregarAeropuerto(new Aeropuerto("UYU", "Uyuni", "Potosi", true));
        // ---- Vertices internacionales ----
        this.grafo.agregarAeropuerto(new Aeropuerto("EZE", "Buenos Aires", "Argentina", false));
        this.grafo.agregarAeropuerto(new Aeropuerto("GRU", "Sao Paulo", "Brasil", false));
        this.grafo.agregarAeropuerto(new Aeropuerto("LIM", "Lima", "Peru", false));
        this.grafo.agregarAeropuerto(new Aeropuerto("MIA", "Miami", "EE.UU.", false));
        this.grafo.agregarAeropuerto(new Aeropuerto("MAD", "Madrid", "Espana", false));

        // ---- Aristas: eje troncal LPB-CBB-VVI (vuelos directos) ----
        this.grafo.agregarVuelo(new Vuelo("BoA", "OB101", "CBB", "VVI", construirMinuto(0, 7, 0),  construirMinuto(0, 7, 45),  520, 150, 90));
        this.grafo.agregarVuelo(new Vuelo("BoA", "OB102", "VVI", "CBB", construirMinuto(0, 9, 0),  construirMinuto(0, 9, 45),  520, 150, 120));
        this.grafo.agregarVuelo(new Vuelo("BoA", "OB110", "CBB", "LPB", construirMinuto(0, 6, 30), construirMinuto(0, 7, 5),   480, 150, 70));
        this.grafo.agregarVuelo(new Vuelo("BoA", "OB111", "LPB", "CBB", construirMinuto(0, 8, 0),  construirMinuto(0, 8, 35),  480, 150, 100));
        this.grafo.agregarVuelo(new Vuelo("BoA", "OB120", "LPB", "VVI", construirMinuto(0, 7, 0),  construirMinuto(0, 7, 55),  690, 150, 130));
        this.grafo.agregarVuelo(new Vuelo("BoA", "OB121", "VVI", "LPB", construirMinuto(0, 10, 0), construirMinuto(0, 10, 55), 690, 150, 60));

        // ---- Aristas: ciudades secundarias hacia el hub ----
        this.grafo.agregarVuelo(new Vuelo("EcoJet", "8J201", "CBB", "SRE", construirMinuto(0, 12, 0), construirMinuto(0, 12, 40), 560, 100, 40));
        this.grafo.agregarVuelo(new Vuelo("EcoJet", "8J202", "SRE", "VVI", construirMinuto(0, 14, 0), construirMinuto(0, 14, 45), 600, 100, 55));
        this.grafo.agregarVuelo(new Vuelo("EcoJet", "8J203", "VVI", "SRE", construirMinuto(0, 16, 0), construirMinuto(0, 16, 45), 600, 100, 30));
        this.grafo.agregarVuelo(new Vuelo("BoA", "OB130", "CBB", "TJA", construirMinuto(0, 11, 0), construirMinuto(0, 12, 0), 720, 130, 100));
        this.grafo.agregarVuelo(new Vuelo("BoA", "OB131", "TJA", "VVI", construirMinuto(0, 13, 30), construirMinuto(0, 14, 25), 700, 130, 80));
        this.grafo.agregarVuelo(new Vuelo("BoA", "OB132", "VVI", "TJA", construirMinuto(0, 9, 30), construirMinuto(0, 10, 25), 700, 130, 90));
        this.grafo.agregarVuelo(new Vuelo("EcoJet", "8J210", "CBB", "TDD", construirMinuto(0, 8, 0), construirMinuto(0, 8, 50), 650, 100, 70));
        this.grafo.agregarVuelo(new Vuelo("EcoJet", "8J211", "TDD", "VVI", construirMinuto(0, 10, 30), construirMinuto(0, 11, 10), 600, 100, 50));
        this.grafo.agregarVuelo(new Vuelo("EcoJet", "8J220", "VVI", "CIJ", construirMinuto(0, 7, 30), construirMinuto(0, 8, 50), 900, 100, 60));
        this.grafo.agregarVuelo(new Vuelo("EcoJet", "8J221", "CBB", "CIJ", construirMinuto(0, 10, 0), construirMinuto(0, 11, 30), 950, 100, 40));
        this.grafo.agregarVuelo(new Vuelo("EcoJet", "8J222", "LPB", "CIJ", construirMinuto(0, 9, 0), construirMinuto(0, 10, 15), 880, 100, 55));
        this.grafo.agregarVuelo(new Vuelo("BoA", "OB140", "CBB", "ORU", construirMinuto(0, 6, 0), construirMinuto(0, 6, 30), 400, 120, 30));
        this.grafo.agregarVuelo(new Vuelo("BoA", "OB150", "CBB", "UYU", construirMinuto(0, 13, 0), construirMinuto(0, 13, 50), 700, 100, 45));
        this.grafo.agregarVuelo(new Vuelo("BoA", "OB160", "VVI", "POI", construirMinuto(0, 15, 0), construirMinuto(0, 15, 55), 650, 100, 35));

        // ---- Aristas internacionales (salen sobre todo de VVI) ----
        this.grafo.agregarVuelo(new Vuelo("BoA", "OB300", "VVI", "EZE", construirMinuto(0, 22, 0), construirMinuto(1, 0, 40), 2500, 200, 150));
        this.grafo.agregarVuelo(new Vuelo("BoA", "OB310", "VVI", "GRU", construirMinuto(0, 21, 0), construirMinuto(0, 23, 30), 2800, 200, 160));
        this.grafo.agregarVuelo(new Vuelo("BoA", "OB320", "VVI", "LIM", construirMinuto(0, 20, 0), construirMinuto(0, 22, 30), 2200, 200, 140));
        this.grafo.agregarVuelo(new Vuelo("BoA", "OB330", "VVI", "MIA", construirMinuto(0, 23, 30), construirMinuto(1, 6, 30), 5500, 220, 180));
        this.grafo.agregarVuelo(new Vuelo("BoA", "OB340", "VVI", "MAD", construirMinuto(0, 23, 0), construirMinuto(1, 14, 0), 7200, 220, 200));
        this.grafo.agregarVuelo(new Vuelo("BoA", "OB341", "VVI", "MAD", construirMinuto(2, 23, 0), construirMinuto(3, 14, 0), 7200, 220, 110));
        // Un directo internacional desde CBB, pero MAS TARDE (sale el martes):
        this.grafo.agregarVuelo(new Vuelo("BoA", "OB301", "CBB", "EZE", construirMinuto(1, 8, 0), construirMinuto(1, 10, 45), 2600, 150, 100));
        this.grafo.agregarVuelo(new Vuelo("BoA", "OB321", "LPB", "LIM", construirMinuto(0, 19, 0), construirMinuto(0, 21, 0), 2300, 150, 90));
    }
}