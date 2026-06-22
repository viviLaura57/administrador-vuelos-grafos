/*
 * Vuelo.java
 * ARISTA DEL GRAFO.
 * Representa un vuelo directo entre dos aeropuertos, con sus pesos
 * (costo, horario, duracion) y datos de capacidad/ocupacion.
 */
public class Vuelo {

    private String lineaAerea;
    private String numeroVuelo;
    private String origen;        // codigo de aeropuerto
    private String destino;       // codigo de aeropuerto
    private int minutoSalida;     // minutos desde el Lunes 00:00 del horizonte
    private int minutoLlegada;
    private double costoNormal;   // en bolivianos
    private int capacidad;        // asientos totales
    private int ocupacion;        // asientos vendidos
    private boolean cancelado;

    public Vuelo(String lineaAerea, String numeroVuelo, String origen, String destino,
                 int minutoSalida, int minutoLlegada, double costoNormal,
                 int capacidad, int ocupacion) {
        this.lineaAerea = lineaAerea;
        this.numeroVuelo = numeroVuelo;
        this.origen = origen;
        this.destino = destino;
        this.minutoSalida = minutoSalida;
        this.minutoLlegada = minutoLlegada;
        this.costoNormal = costoNormal;
        this.capacidad = capacidad;
        this.ocupacion = ocupacion;
        this.cancelado = false;
    }

    public String obtenerLineaAerea() {
        return this.lineaAerea;
    }

    public String obtenerNumeroVuelo() {
        return this.numeroVuelo;
    }

    public String obtenerOrigen() {
        return this.origen;
    }

    public String obtenerDestino() {
        return this.destino;
    }

    public int obtenerMinutoSalida() {
        return this.minutoSalida;
    }

    public int obtenerMinutoLlegada() {
        return this.minutoLlegada;
    }

    public int obtenerDuracion() {
        int duracion = this.minutoLlegada - this.minutoSalida;
        return duracion;
    }

    public int obtenerCapacidad() {
        return this.capacidad;
    }

    public int obtenerOcupacion() {
        return this.ocupacion;
    }

    public boolean estaCancelado() {
        return this.cancelado;
    }

    public void cancelar() {
        this.cancelado = true;
    }

    public void reactivar() {
        this.cancelado = false;
    }

    // Quedan asientos libres? (capacidad vs ocupacion)
    public boolean tieneAsientos() {
        boolean disponible = this.ocupacion < this.capacidad;
        return disponible;
    }

    /*
     * Costo efectivo = costo normal con posible PROMOCION (rebaja).
     * Regla: si la ocupacion es baja (menos del 50% de asientos vendidos),
     * la aerolinea aplica una rebaja por promocion del 15%.
     */
    public double obtenerCostoEfectivo() {
        double costo = this.costoNormal;
        double factorOcupacion = (double) this.ocupacion / this.capacidad;
        if (factorOcupacion < 0.5) {
            costo = costo * 0.85;
        }
        return costo;
    }

    public boolean tienePromocion() {
        double factorOcupacion = (double) this.ocupacion / this.capacidad;
        boolean conPromocion = factorOcupacion < 0.5;
        return conPromocion;
    }
}
