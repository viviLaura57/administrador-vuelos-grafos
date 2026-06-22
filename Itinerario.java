/*
 * Itinerario.java
 * RESULTADO de una busqueda: el camino encontrado en el grafo,
 * con sus metricas (costo, tiempo, espera y penalizacion).
 */
import java.util.ArrayList;
import java.util.List;

public class Itinerario {

    private List<Vuelo> tramos;
    private boolean existe;
    private int minutoPartida;   // momento desde el que se busco
    private double costoTotal;
    private int tiempoTotal;     // minutos transcurridos hasta la llegada final
    private int esperaTotal;     // minutos de espera acumulados en escalas
    private double penalizacion; // costo extra por reprogramacion

    public Itinerario() {
        this.tramos = new ArrayList<Vuelo>();
        this.existe = false;
        this.minutoPartida = 0;
        this.costoTotal = 0.0;
        this.tiempoTotal = 0;
        this.esperaTotal = 0;
        this.penalizacion = 0.0;
    }

    public void marcarInexistente() {
        this.existe = false;
    }

    public boolean existeRuta() {
        return this.existe;
    }

    public List<Vuelo> obtenerTramos() {
        return this.tramos;
    }

    public int cantidadEscalas() {
        int escalas = this.tramos.size() - 1;
        if (escalas < 0) {
            escalas = 0;
        }
        return escalas;
    }

    public double obtenerCostoTotal() {
        return this.costoTotal;
    }

    public int obtenerTiempoTotal() {
        return this.tiempoTotal;
    }

    public int obtenerEsperaTotal() {
        return this.esperaTotal;
    }

    public double obtenerPenalizacion() {
        return this.penalizacion;
    }

    public void agregarPenalizacion(double monto) {
        this.penalizacion = this.penalizacion + monto;
        this.costoTotal = this.costoTotal + monto;
    }

    // Carga los tramos del camino y calcula sus metricas.
    public void establecerCamino(List<Vuelo> camino, int minutoPartida) {
        this.tramos = camino;
        this.minutoPartida = minutoPartida;
        this.existe = !camino.isEmpty();
        double sumaCosto = 0.0;
        int sumaEspera = 0;
        for (int i = 0; i < this.tramos.size(); i++) {
            Vuelo actual = this.tramos.get(i);
            sumaCosto = sumaCosto + actual.obtenerCostoEfectivo();
            if (i > 0) {
                Vuelo anterior = this.tramos.get(i - 1);
                int espera = actual.obtenerMinutoSalida() - anterior.obtenerMinutoLlegada();
                sumaEspera = sumaEspera + espera;
            }
        }
        this.costoTotal = sumaCosto;
        this.esperaTotal = sumaEspera;
        if (this.tramos.isEmpty()) {
            this.tiempoTotal = 0;
        } else {
            Vuelo ultimo = this.tramos.get(this.tramos.size() - 1);
            this.tiempoTotal = ultimo.obtenerMinutoLlegada() - minutoPartida;
        }
    }
}
