package Project;

import Lib.Coordenada;
import Lib.Cultivo;

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

    public static boolean esAreaValida(Cultivo cultivo, Coordenada arribaIzq, Coordenada abajoDerecha, Cultivo[][] cultivos) {
        int maxFila = 0; // Longitud máxima en filas
        int maxColumna = 0; // Longitud máxima en columnas

        // Verificación de filas dentro del área extendida hasta 11 posiciones adicionales
        for (int i = Math.max(0, arribaIzq.getX() - 11); i <= Math.min(cultivos.length - 1, abajoDerecha.getX() + 11); i++) {

            int contadorFila = 0; // Contador para cultivos continuos en fila
            for (int j = arribaIzq.getY(); j <= abajoDerecha.getY(); j++) {
                if (cultivos[i][j] != null && cultivos[i][j].equals(cultivo)) {
                    contadorFila++;
                    maxFila = Math.max(maxFila, contadorFila); // Actualizar longitud máxima de fila continua
                } else {
                    contadorFila = 0; // Reiniciar si encontramos un cultivo diferente
                }
            }
        }

        // Verificación de columnas dentro del área extendida hasta 11 posiciones adicionales
        for (int j = Math.max(0, arribaIzq.getY() - 11); j <= Math.min(cultivos[0].length - 1, abajoDerecha.getY() + 11); j++) {
            int contadorColumna = 0; // Contador para cultivos continuos en columna
            for (int i = arribaIzq.getX(); i <= abajoDerecha.getX(); i++) {
                if (cultivos[i][j] != null && cultivos[i][j].equals(cultivo)) {
                    contadorColumna++;
                    maxColumna = Math.max(maxColumna, contadorColumna); // Actualizar longitud máxima de columna continua
                } else {
                    contadorColumna = 0; // Reiniciar si encontramos un cultivo diferente
                }
                if (maxColumna > 6) { // Poda: Si supera 6 en columna, retorna falso
                    return false;
                }
            }
        }

        // Verificación final para que maxFila + maxColumna <= 11
        return (maxFila + maxColumna) <= 11;
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
