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


    //CHEQUEARRRRRR
    public static boolean convienePlantarlo(Cultivo cultivo, double gananciaActual, double[][] riesgos, Coordenada izqArriba, Coordenada derechaAbajo) {
        double ganancia = 0;

        for (int i = izqArriba.getX(); i <= derechaAbajo.getX(); i++) {
            for (int j = izqArriba.getY(); j <= derechaAbajo.getY(); j++) {
                // Suma el potencial de cada parcela en el área
                double potencial = obtenerPotencialDeCadaParcela(riesgos[i][j], cultivo.getCostoPorParcela(), cultivo.getPrecioDeVentaPorParcela());
                ganancia += potencial;
            }
        }

        // Resta el costo de inversión única del cultivo
        ganancia -= cultivo.getInversionRequerida();

        // Retorna true si la ganancia es mejor que la ganancia actual
        return ganancia > gananciaActual;
    }

    public static boolean isInSet(Cultivo cultivo, Set<Cultivo> cultivoSet, Cultivo cultivoRepetible) {
        return !cultivoSet.contains(cultivo) && !cultivo.equals(cultivoRepetible);
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
                    if (!visitados[nuevoX][nuevoY] && matrizCultivos[nuevoX][nuevoY].equals(cultivo)) {
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

    public static boolean esAreaValida(Coordenada arribaIzq, Coordenada abajoDerecha, String[][] matrizCultivos, Cultivo cultivo) {
        // Verificar que el área principal esté libre
        if (!areaLibre(arribaIzq, abajoDerecha, matrizCultivos)) {
            return false;
        }

        int maxFila = matrizCultivos.length;
        int maxColumna = matrizCultivos[0].length;
        int maxFilaContinuo = 0;
        int maxColumnaContinuo = 0;

        // Verificar filas continuas en los límites
        for (int i = Math.max(0, arribaIzq.getX() - 11); i <= Math.min(maxFila - 1, abajoDerecha.getX() + 11); i++) {
            int contadorFila = 0;
            for (int j = Math.max(0, arribaIzq.getY() - 11); j <= Math.min(maxColumna - 1, abajoDerecha.getY() + 11); j++) {
                // Salteamos las posiciones del área
                if (i >= arribaIzq.getX() && i <= abajoDerecha.getX() && j >= arribaIzq.getY() && j <= abajoDerecha.getY()) {
                    continue;
                }
                // Validamos índices antes de acceder a la matriz
                if (i >= 0 && i < maxFila && j >= 0 && j < maxColumna) {
                    if (matrizCultivos[i][j] != null && matrizCultivos[i][j].equals(cultivo.getNombre())) {
                        contadorFila++;
                        maxFilaContinuo = Math.max(maxFilaContinuo, contadorFila);
                    } else {
                        contadorFila = 0;
                    }
                }
            }
        }

        // Verificar columnas continuas en los límites
        for (int j = Math.max(0, arribaIzq.getY() - 11); j <= Math.min(maxColumna - 1, abajoDerecha.getY() + 11); j++) {
            int contadorColumna = 0;
            for (int i = Math.max(0, arribaIzq.getX() - 11); i <= Math.min(maxFila - 1, abajoDerecha.getX() + 11); i++) {
                // Salteamos las posiciones del área
                if (i >= arribaIzq.getX() && i <= abajoDerecha.getX() && j >= arribaIzq.getY() && j <= abajoDerecha.getY()) {
                    continue;
                }
                // Validamos índices antes de acceder a la matriz
                if (i >= 0 && i < maxFila && j >= 0 && j < maxColumna) {
                    if (matrizCultivos[i][j] != null && matrizCultivos[i][j].equals(cultivo.getNombre())) {
                        contadorColumna++;
                        maxColumnaContinuo = Math.max(maxColumnaContinuo, contadorColumna);
                    } else {
                        contadorColumna = 0;
                    }
                }
            }
        }

        // Verificar que la suma de las dimensiones continuas no exceda 11
        return (maxFilaContinuo + maxColumnaContinuo) <= 11;
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

    /*private void guardarMejorConfiguracion(double [][] riesgos,Cultivo[][] matrizCultivos, List<CultivoSeleccionado> cultivoSeleccionados) {
        cultivoSeleccionados.clear();
        boolean[][] visitado = new boolean[matrizCultivos.length][matrizCultivos[0].length];

        // Recorrer la matriz buscando cultivos no visitados
        for (int i = 0; i < matrizCultivos.length; i++) {
            for (int j = 0; j < matrizCultivos[0].length; j++) {
                if (!visitado[i][j] && matrizCultivos[i][j] != null) {
                    // Encontrar el área rectangular del cultivo actual
                    Coordenada arribaIzq = new Coordenada(i, j);
                    Coordenada abajoDerecha = encontrarAreaRectangular(matrizCultivos, visitado, i, j);

                    // Marcar toda el área como visitada
                    marcarAreaComoVisitada(visitado, arribaIzq, abajoDerecha);

                    // Calcular estadísticas del área
                    double montoInvertido = calcularMontoInvertido(matrizCultivos[i][j], arribaIzq, abajoDerecha);
                    double riesgoPromedio = calcularRiesgoPromedio(arribaIzq, abajoDerecha, riesgos);
                    double gananciaArea = calcularGananciaArea(matrizCultivos[i][j], arribaIzq, abajoDerecha, riesgos);

                    // Crear y agregar el CultivoSeleccionado
                    CultivoSeleccionado cultivoSeleccionado = new CultivoSeleccionado(
                            matrizCultivos[i][j].getNombre(),
                            arribaIzq,
                            abajoDerecha,
                            montoInvertido,
                            (int)riesgoPromedio,
                            gananciaArea
                    );
                    cultivoSeleccionados.add(cultivoSeleccionado);
                }
            }
        }
    }*/

    public static Coordenada encontrarAreaRectangular(Cultivo[][] matrizCultivos, boolean[][] visitado, int startI, int startJ) {
        Cultivo cultivoActual = matrizCultivos[startI][startJ];
        int maxI = startI;
        int maxJ = startJ;

        // Encontrar el límite inferior derecho del rectángulo
        while (maxI + 1 < matrizCultivos.length &&
                !visitado[maxI + 1][startJ] &&
                matrizCultivos[maxI + 1][startJ] == cultivoActual) {
            maxI++;
        }

        while (maxJ + 1 < matrizCultivos[0].length &&
                !visitado[startI][maxJ + 1] &&
                matrizCultivos[startI][maxJ + 1] == cultivoActual) {
            maxJ++;
        }

        // Verificar que toda el área rectangular contiene el mismo cultivo
        for (int i = startI; i <= maxI; i++) {
            for (int j = startJ; j <= maxJ; j++) {
                if (matrizCultivos[i][j] != cultivoActual) {
                    // Si encontramos un cultivo diferente, ajustamos los límites
                    return new Coordenada(i - 1, j - 1);
                }
            }
        }

        return new Coordenada(maxI, maxJ);
    }

    public static void marcarAreaComoVisitada(boolean[][] visitado, Coordenada arribaIzq, Coordenada abajoDerecha) {
        for (int i = arribaIzq.getX(); i <= abajoDerecha.getX(); i++) {
            for (int j = arribaIzq.getY(); j <= abajoDerecha.getY(); j++) {
                visitado[i][j] = true;
            }
        }
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
