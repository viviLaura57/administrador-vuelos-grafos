/*
 * Grafo.java
 * EL GRAFO dirigido y ponderado y sus algoritmos de caminos minimos:
 *   - minimizarCosto      -> Dijkstra (peso = costo del vuelo).
 *   - buscarConHorario    -> minimizar TIEMPO (llegada mas temprana) o
 *                            minimizar TIEMPO DE ESPERA, respetando horarios
 *                            (relajacion tipo Bellman-Ford / label-correcting).
 */
import java.util.ArrayList;
import java.util.List;

public class Grafo {

    private List<Aeropuerto> aeropuertos;  // vertices
    private List<Vuelo> vuelos;            // aristas

    private static final double COSTO_INFINITO = 1.0e18;
    private static final int MINUTO_INFINITO = 1000000000;
    private static final int CONEXION_MINIMA = 45;   // minutos minimos entre escalas

    public static final int CRITERIO_TIEMPO = 1;
    public static final int CRITERIO_ESPERA = 2;

    public Grafo() {
        this.aeropuertos = new ArrayList<Aeropuerto>();
        this.vuelos = new ArrayList<Vuelo>();
    }

    public void agregarAeropuerto(Aeropuerto aeropuerto) {
        this.aeropuertos.add(aeropuerto);
    }

    public void agregarVuelo(Vuelo vuelo) {
        this.vuelos.add(vuelo);
    }

    public List<Aeropuerto> obtenerAeropuertos() {
        return this.aeropuertos;
    }

    public List<Vuelo> obtenerVuelos() {
        return this.vuelos;
    }

    public boolean existeAeropuerto(String codigo) {
        int indice = buscarIndice(codigo);
        boolean existe = indice != -1;
        return existe;
    }

    // Indice del aeropuerto cuyo codigo coincide (-1 si no existe).
    private int buscarIndice(String codigo) {
        int indice = -1;
        for (int i = 0; i < this.aeropuertos.size(); i++) {
            if (this.aeropuertos.get(i).obtenerCodigo().equals(codigo)) {
                indice = i;
            }
        }
        return indice;
    }

    /* -------------------------------------------------------------- */
    /*  ESCENARIO 1: MINIMIZAR COSTO                                   */
    /*  "Quiero el menor costo posible, no me importa el tiempo".      */
    /*  Algoritmo de Dijkstra con peso = costo efectivo del vuelo.     */
    /* -------------------------------------------------------------- */
    public Itinerario minimizarCosto(String origen, String destino) {
        int cantidad = this.aeropuertos.size();
        double[] distancia = new double[cantidad];
        Vuelo[] vueloPrevio = new Vuelo[cantidad];
        boolean[] visitado = new boolean[cantidad];
        for (int i = 0; i < cantidad; i++) {
            distancia[i] = COSTO_INFINITO;
            vueloPrevio[i] = null;
            visitado[i] = false;
        }
        int inicio = buscarIndice(origen);
        int fin = buscarIndice(destino);
        distancia[inicio] = 0.0;

        for (int paso = 0; paso < cantidad; paso++) {
            int actual = seleccionarMenorCosto(distancia, visitado);
            if (actual != -1) {
                visitado[actual] = true;
                for (int i = 0; i < this.vuelos.size(); i++) {
                    Vuelo vuelo = this.vuelos.get(i);
                    int desde = buscarIndice(vuelo.obtenerOrigen());
                    if (!vuelo.estaCancelado() && vuelo.tieneAsientos() && desde == actual) {
                        int hacia = buscarIndice(vuelo.obtenerDestino());
                        double nueva = distancia[actual] + vuelo.obtenerCostoEfectivo();
                        if (nueva < distancia[hacia]) {
                            distancia[hacia] = nueva;
                            vueloPrevio[hacia] = vuelo;
                        }
                    }
                }
            }
        }

        Itinerario resultado = reconstruir(fin, vueloPrevio, distancia[fin] < COSTO_INFINITO, 0);
        return resultado;
    }

    // Vertice no visitado con menor distancia (-1 si no queda ninguno).
    private int seleccionarMenorCosto(double[] distancia, boolean[] visitado) {
        int elegido = -1;
        double menor = COSTO_INFINITO;
        for (int i = 0; i < distancia.length; i++) {
            if (!visitado[i] && distancia[i] < menor) {
                menor = distancia[i];
                elegido = i;
            }
        }
        return elegido;
    }

    /* -------------------------------------------------------------- */
    /*  ESCENARIO 2 y 3: MINIMIZAR TIEMPO / TIEMPO DE ESPERA           */
    /*  Relajacion tipo Bellman-Ford (label-correcting): se repasan    */
    /*  todas las aristas varias veces hasta estabilizar. Como el      */
    /*  tiempo solo avanza, no hay ciclos y el proceso converge.       */
    /* -------------------------------------------------------------- */
    public Itinerario buscarConHorario(String origen, String destino, int minutoActual, int criterio) {
        int cantidad = this.aeropuertos.size();
        double[] metrica = new double[cantidad];   // tiempo de llegada o espera acumulada
        int[] llegada = new int[cantidad];         // minuto de llegada al vertice
        Vuelo[] vueloPrevio = new Vuelo[cantidad];
        for (int i = 0; i < cantidad; i++) {
            metrica[i] = COSTO_INFINITO;
            llegada[i] = MINUTO_INFINITO;
            vueloPrevio[i] = null;
        }
        int inicio = buscarIndice(origen);
        int fin = buscarIndice(destino);
        llegada[inicio] = minutoActual;
        if (criterio == CRITERIO_TIEMPO) {
            metrica[inicio] = minutoActual;
        } else {
            metrica[inicio] = 0.0;
        }

        for (int pasada = 0; pasada < cantidad - 1; pasada++) {
            for (int i = 0; i < this.vuelos.size(); i++) {
                Vuelo vuelo = this.vuelos.get(i);
                int desde = buscarIndice(vuelo.obtenerOrigen());
                int hacia = buscarIndice(vuelo.obtenerDestino());
                boolean utilizable = !vuelo.estaCancelado() && vuelo.tieneAsientos()
                        && llegada[desde] < MINUTO_INFINITO;
                if (utilizable) {
                    int listoEn;
                    if (desde == inicio) {
                        listoEn = minutoActual;
                    } else {
                        listoEn = llegada[desde] + CONEXION_MINIMA;
                    }
                    if (vuelo.obtenerMinutoSalida() >= listoEn) {
                        double nuevaMetrica;
                        if (criterio == CRITERIO_TIEMPO) {
                            nuevaMetrica = vuelo.obtenerMinutoLlegada();
                        } else {
                            int espera;
                            if (desde == inicio) {
                                espera = 0;
                            } else {
                                espera = vuelo.obtenerMinutoSalida() - llegada[desde];
                            }
                            nuevaMetrica = metrica[desde] + espera;
                        }
                        if (nuevaMetrica < metrica[hacia]) {
                            metrica[hacia] = nuevaMetrica;
                            llegada[hacia] = vuelo.obtenerMinutoLlegada();
                            vueloPrevio[hacia] = vuelo;
                        }
                    }
                }
            }
        }

        boolean hayRuta = llegada[fin] < MINUTO_INFINITO;
        Itinerario resultado = reconstruir(fin, vueloPrevio, hayRuta, minutoActual);
        return resultado;
    }

    /* -------------------------------------------------------------- */
    /*  Reconstruye el camino caminando hacia atras por vueloPrevio.   */
    /* -------------------------------------------------------------- */
    private Itinerario reconstruir(int fin, Vuelo[] vueloPrevio, boolean hayRuta, int minutoPartida) {
        Itinerario itinerario = new Itinerario();
        if (!hayRuta) {
            itinerario.marcarInexistente();
        } else {
            List<Vuelo> camino = new ArrayList<Vuelo>();
            int actual = fin;
            boolean terminado = false;
            while (!terminado) {
                Vuelo vuelo = vueloPrevio[actual];
                if (vuelo == null) {
                    terminado = true;
                } else {
                    camino.add(0, vuelo);
                    actual = buscarIndice(vuelo.obtenerOrigen());
                }
            }
            itinerario.establecerCamino(camino, minutoPartida);
        }
        return itinerario;
    }
}
