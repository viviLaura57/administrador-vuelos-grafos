/*
 * ============================================================================
 *  PROYECTO 2do PARCIAL - TEORIA DE GRAFOS
 *  Aplicacion para Administracion de Itinerarios de Vuelo (Bolivia)
 * ----------------------------------------------------------------------------
 *  CLASE PRINCIPAL: punto de entrada del programa.
 *
 *  Estructura del proyecto (un archivo por clase):
 *    - Aeropuerto.java          Vertice del grafo.
 *    - Vuelo.java               Arista del grafo (vuelo directo).
 *    - Itinerario.java          Resultado: camino encontrado + metricas.
 *    - Grafo.java               El grafo y los algoritmos de caminos minimos.
 *    - Sistema.java             Menu por consola, teclado y datos.
 *    - AdministradorVuelos.java Metodo main (este archivo).
 *
 *  Compilar:  javac *.java
 *  Ejecutar:  java AdministradorVuelos
 * ============================================================================
 */
public class AdministradorVuelos {

    public static void main(String[] argumentos) {
        Sistema sistema = new Sistema();
        sistema.iniciar();
    }
}
