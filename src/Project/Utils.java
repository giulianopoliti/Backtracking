package Project;

import Lib.Coordenada;
import Lib.Cultivo;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class Utils {

    // recorre el area y planta el cultivo
    public static void plantarCultivoEnArea(Cultivo cultivo, Coordenada arribaIzq, Coordenada abajoDerecha, Cultivo[][] matrizCultivos) {
        for (int i = arribaIzq.getX(); i <= abajoDerecha.getX(); i++) {
            for (int j = arribaIzq.getY(); j <= abajoDerecha.getY(); j++) {
                matrizCultivos[i][j] = cultivo;
            }
        }
    }
    //recorre el area y remueve el cultivo de la matriz [][] de cultivos de prueba
    public static void removerCultivoDeArea(Coordenada arribaIzq, Coordenada abajoDerecha, Cultivo[][] matrizCultivos) {
        for (int i = arribaIzq.getX(); i <= abajoDerecha.getX(); i++) {
            for (int j = arribaIzq.getY(); j <= abajoDerecha.getY(); j++) {
                matrizCultivos[i][j] = null;
            }
        }
    }


    public static boolean areaLibre(Coordenada arribaIzq, Coordenada abajoDerecha, String[][] cultivos) {
        for (int i = arribaIzq.getX(); i <= abajoDerecha.getX(); i++) {
            for (int j = arribaIzq.getY(); j <= abajoDerecha.getY(); j++) {
                if (cultivos[i][j] != null) {
                    return false;
                }
            }
        }
        return true;
    }


    public static boolean sePuedePlantar(Coordenada arribaIzq, Coordenada abajoDerecha, String[][] matrizCultivos, String cultivo) {
        // Dimensiones de la matriz
        int filas = matrizCultivos.length;
        int columnas = matrizCultivos[0].length;

        // Límites iniciales del área especificada
        int minX = arribaIzq.getX();
        int maxX = abajoDerecha.getX();
        int minY = arribaIzq.getY();
        int maxY = abajoDerecha.getY();

        // Cola para realizar la búsqueda en anchura (BFS)
        Queue<Coordenada> cola = new LinkedList<>();

        // Matriz de visitados para evitar procesar la misma celda más de una vez
        boolean[][] visitados = new boolean[filas][columnas];

        // Agregar todas las celdas del área inicial a la cola y marcarlas como visitadas
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                cola.add(new Coordenada(x, y));
                visitados[x][y] = true;
            }
        }

        // Direcciones para moverse: arriba, abajo, izquierda, derecha
        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};

        // Realizar BFS
        while (!cola.isEmpty()) {
            Coordenada actual = cola.poll();
            int x = actual.getX();
            int y = actual.getY();

            // Explora las celdas adyacentes
            for (int i = 0; i < 4; i++) {
                int nuevoX = x + dx[i];
                int nuevoY = y + dy[i];

                // Verificar si la celda está dentro de los límites de la matriz
                if (nuevoX >= 0 && nuevoX < filas && nuevoY >= 0 && nuevoY < columnas) {
                    // Verificar si no ha sido visitada y tiene el mismo cultivo
                    if (!visitados[nuevoX][nuevoY] && matrizCultivos[nuevoX][nuevoY] != null && matrizCultivos[nuevoX][nuevoY].equals(cultivo)) {
                        // Marcar como visitada y agregarla a la cola
                        visitados[nuevoX][nuevoY] = true;
                        cola.add(new Coordenada(nuevoX, nuevoY));

                        // Expandir los límites del área efectiva
                        minX = Math.min(minX, nuevoX);
                        maxX = Math.max(maxX, nuevoX);
                        minY = Math.min(minY, nuevoY);
                        maxY = Math.max(maxY, nuevoY);
                    }
                }
            }
        }

        // Calcular ancho (N) y alto (M) del área efectiva
        int ancho = maxX - minX + 1;
        int alto = maxY - minY + 1;

        // Verificar la restricción N + M <= 11
        return (ancho + alto) <= 11;
    }

    public static void marcarMatrizCultivos(Cultivo cultivo, Coordenada arribaIzq, Coordenada abajoDerecha, String[][] cultivos) {
        for (int i = arribaIzq.getX(); i <= abajoDerecha.getX(); i++) {
            for (int j = arribaIzq.getY(); j <= abajoDerecha.getY(); j++) {
                cultivos[i][j] = cultivo.getNombre();
            }
        }
    }


    public static void desmarcarMatrizCultivos (Coordenada arribaIzq, Coordenada abajoDerecha, String[][] cultivos) {
        for (int i = arribaIzq.getX(); i <= abajoDerecha.getX(); i++) {
            for (int j = arribaIzq.getY(); j <= abajoDerecha.getY(); j++) {
                cultivos[i][j] = null;
            }
        }
    }

    public static double obtenerPotencialDeCadaParcela (double riesgo, double costo, double precioVenta) {
        return ( 1 - riesgo ) * ( precioVenta - costo );
    }
    public static double calcularGananciaArea(Cultivo cultivo, Coordenada arribaIzq, Coordenada abajoDerecha, double[][] riesgos) {
        double ganancia = 0;

        for (int i = arribaIzq.getX(); i <= abajoDerecha.getX(); i++) {
            for (int j = arribaIzq.getY(); j <= abajoDerecha.getY(); j++) {
                double potencial = obtenerPotencialDeCadaParcela(riesgos[i][j], cultivo.getCostoPorParcela(), cultivo.getPrecioDeVentaPorParcela());
                ganancia += potencial;
            }
        }

        // Resta el costo de inversión única del cultivo
        return ganancia - cultivo.getInversionRequerida();
    }

    //chequear
    public static double calcularMontoInvertido(Cultivo cultivo, Coordenada arribaIzq, Coordenada abajoDerecha) {
        int numeroParcelas = (abajoDerecha.getX() + 1 - arribaIzq.getX()) * (abajoDerecha.getY() + 1 - arribaIzq.getY());
        return (cultivo.getCostoPorParcela() * numeroParcelas) + cultivo.getInversionRequerida();
    }

    public static int RiesgoAsociado(Coordenada arribaIzq, Coordenada abajoDerecha, double[][] riesgos) {
        double sumaRiesgos = 0;

        for (int i = arribaIzq.getX(); i <= abajoDerecha.getX(); i++) {
            for (int j = arribaIzq.getY(); j <= abajoDerecha.getY(); j++) {
                sumaRiesgos += riesgos[i][j];
            }
        }

        return (int) sumaRiesgos * 100; //PREGUNTARRRRR A NICOOOOOOO
    }

}
