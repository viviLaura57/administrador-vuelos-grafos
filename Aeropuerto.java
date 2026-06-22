/*
 * Aeropuerto.java
 * VERTICE DEL GRAFO.
 * Representa un aeropuerto (un departamento de Bolivia o un pais).
 */
public class Aeropuerto {

    private String codigo;     // ej. CBB
    private String ciudad;     // ej. Cochabamba
    private String lugar;      // departamento o pais
    private boolean nacional;  // true = Bolivia, false = internacional

    public Aeropuerto(String codigo, String ciudad, String lugar, boolean nacional) {
        this.codigo = codigo;
        this.ciudad = ciudad;
        this.lugar = lugar;
        this.nacional = nacional;
    }

    public String obtenerCodigo() {
        return this.codigo;
    }

    public String obtenerCiudad() {
        return this.ciudad;
    }

    public String obtenerLugar() {
        return this.lugar;
    }

    public boolean esNacional() {
        return this.nacional;
    }

    public String describir() {
        String ambito = this.nacional ? "Nacional" : "Internacional";
        String texto = this.codigo + " - " + this.ciudad + " (" + this.lugar + ", " + ambito + ")";
        return texto;
    }
}
