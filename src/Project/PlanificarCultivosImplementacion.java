package Project;

import Lib.Coordenada;
import Lib.Cultivo;
import Lib.CultivoSeleccionado;
import Lib.PlanificarCultivos;

import java.util.*;
import java.util.stream.Collectors;

public class PlanificarCultivosImplementacion implements PlanificarCultivos {
    private double mejorGananciaGlobal = Double.NEGATIVE_INFINITY;

    @Override
    public List<CultivoSeleccionado> obtenerPlanificacion(List<Cultivo> cultivosDisponibles, double[][] riesgos, String temporada) {
        Cultivo cultivo = seleccionarRepetibleYEliminarPorTemporada(cultivosDisponibles, temporada);
        List <CultivoSeleccionado> cultivoSeleccionados = new ArrayList<>();
        Cultivo [][] matrizCultivos = new Cultivo[riesgos.length][riesgos[0].length];
        Set <Cultivo> cultivoSet = new HashSet<>();
        double ganancia = backtracking(cultivosDisponibles, cultivoSeleccionados, riesgos, mejorGananciaGlobal, 0, matrizCultivos, cultivo, cultivoSet, 0, 0);
        System.out.println(ganancia);
        return cultivoSeleccionados;
    }
    public double obtenerGanancia(List<Cultivo> cultivosDisponibles, double[][] riesgos, String temporada) {
        Cultivo cultivo = seleccionarRepetibleYEliminarPorTemporada(cultivosDisponibles, temporada);
        List <CultivoSeleccionado> cultivoSeleccionados = new ArrayList<>();
        Cultivo [][] matrizCultivos = new Cultivo[riesgos.length][riesgos[0].length];
        Set <Cultivo> cultivoSet = new HashSet<>();
        double ganancia = backtracking(cultivosDisponibles, cultivoSeleccionados, riesgos, mejorGananciaGlobal,0 , matrizCultivos, cultivo, cultivoSet, 0, 0);
        System.out.println(ganancia);
        for (int i = 0; i < matrizCultivos.length; i++) {
            for (int j = 0; j < matrizCultivos[i].length; j++) {
                System.out.println(matrizCultivos[i][j]);
            }
        }
        return ganancia;
    }

    public Cultivo seleccionarRepetibleYEliminarPorTemporada(List<Cultivo> cultivos, String temporada) {
        double gananciaMaxima = Double.NEGATIVE_INFINITY;
        Cultivo cultivoSeleccionado = null;

        // Usar un Iterator para eliminar cultivos fuera de temporada sin errores
        Iterator<Cultivo> iterator = cultivos.iterator();
        while (iterator.hasNext()) {
            Cultivo cultivo = iterator.next();
            if (!cultivo.getTemporadaOptima().equals(temporada)) {
                iterator.remove(); // Elimina cultivos fuera de temporada
            } else {
                // Calcular la ganancia potencial de cada cultivo dejando algunas parcelas libres, habria que ver cuantas puede ocupar y reemplazar el 960
                double ganancia = (cultivo.getPrecioDeVentaPorParcela() - cultivo.getCostoPorParcela()) * 960 - cultivo.getInversionRequerida();
                if (ganancia > gananciaMaxima) {
                    gananciaMaxima = ganancia;
                    cultivoSeleccionado = cultivo;
                }
            }
        }

        return cultivoSeleccionado;
    }


    private double backtracking(
            List<Cultivo> cultivosDisponibles,
            List<CultivoSeleccionado> cultivoSeleccionados,
            double[][] riesgos,
            double gananciaAnterior,
            int indiceCultivo,
            Cultivo[][] matrizCultivos,
            Cultivo cultivoRepetible,
            Set<Cultivo> cultivoSet,
            int indiceY,
            int indiceX) {

// Si llegamos al final, guardamos la configuración si es la mejor hasta ahora
        if (indiceCultivo >= cultivosDisponibles.size()) {
            if (gananciaAnterior > mejorGananciaGlobal) {
                mejorGananciaGlobal = gananciaAnterior;
                //guardarMejorConfiguracion(riesgos, matrizCultivos, cultivoSeleccionados);
            }
            return gananciaAnterior;
        }

        Cultivo cultivoActual = cultivosDisponibles.get(indiceCultivo);
        if (isInSet(cultivoActual, cultivoSet, cultivoRepetible)) { backtracking(cultivosDisponibles, cultivoSeleccionados, riesgos, gananciaAnterior, indiceCultivo+1, matrizCultivos, cultivoRepetible, cultivoSet, indiceY, indiceX); }
        for (int i = 0; i < riesgos.length; i++) {
            for (int j = 0; j < riesgos[i].length; j++) {
                for (int alto = 1; alto <= 11; alto++) {
                    for (int ancho = 1; ancho + alto <= 11; ancho++) {
                        Coordenada arribaIzq = new Coordenada(i, j);
                        Coordenada abajoDerecha = new Coordenada(i + alto - 1, j + ancho - 1);
                            if (esAreaValida(cultivoActual, arribaIzq, abajoDerecha, matrizCultivos)) {
                                if (convienePlantarlo(cultivoActual, gananciaAnterior, riesgos, arribaIzq, abajoDerecha)) {
                                    plantarCultivoEnArea(cultivoActual, arribaIzq, abajoDerecha, matrizCultivos);
                                    cultivoSet.add(cultivoActual);
                                    double nuevaGanancia = gananciaAnterior + calcularGananciaArea(cultivoActual, arribaIzq, abajoDerecha, riesgos);
                                    gananciaAnterior = Math.max(gananciaAnterior, backtracking(
                                            cultivosDisponibles,
                                            cultivoSeleccionados,
                                            riesgos,
                                            nuevaGanancia,
                                            indiceCultivo + 1,
                                            matrizCultivos,
                                            cultivoRepetible,
                                            cultivoSet,
                                            j, i));
                                    removerCultivoDeArea(arribaIzq, abajoDerecha, matrizCultivos);
                                    cultivoSet.remove(cultivoActual);
                                }
                            }
                        }
                    }
                }
            }

        return gananciaAnterior;
    }


    // recorre el area y planta el cultivo
    private void plantarCultivoEnArea(Cultivo cultivo, Coordenada arribaIzq, Coordenada abajoDerecha, Cultivo[][] matrizCultivos) {
        for (int i = arribaIzq.getX(); i <= abajoDerecha.getX(); i++) {
            for (int j = arribaIzq.getY(); j <= abajoDerecha.getY(); j++) {
                matrizCultivos[i][j] = cultivo;
            }
        }
    }
    //recorre el area y remueve el cultivo de la matriz [][] de cultivos de prueba
    private void removerCultivoDeArea(Coordenada arribaIzq, Coordenada abajoDerecha, Cultivo[][] matrizCultivos) {
        for (int i = arribaIzq.getX(); i <= abajoDerecha.getX(); i++) {
            for (int j = arribaIzq.getY(); j <= abajoDerecha.getY(); j++) {
                matrizCultivos[i][j] = null;
            }
        }
    }


    public boolean convienePlantarlo(Cultivo cultivo, double gananciaActual, double[][] riesgos, Coordenada izqArriba, Coordenada derechaAbajo) {
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

    public boolean isInSet(Cultivo cultivo, Set<Cultivo> cultivoSet, Cultivo cultivoRepetible) {
        return !cultivoSet.contains(cultivo) && !cultivo.equals(cultivoRepetible);
    }

    private boolean esAreaValida(Cultivo cultivo, Coordenada arribaIzq, Coordenada abajoDerecha, Cultivo[][] cultivos) {
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
                if (maxFila > 6) { // Poda: Si supera 6 en fila, retorna falso
                    return false;
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

    private double obtenerPotencialDeCadaParcela (double riesgo, double costo, double precioVenta) {
        return ( 1 - riesgo ) * ( precioVenta - costo );
    }
    private double calcularGananciaArea(Cultivo cultivo, Coordenada arribaIzq, Coordenada abajoDerecha, double[][] riesgos) {
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

    private Coordenada encontrarAreaRectangular(Cultivo[][] matrizCultivos, boolean[][] visitado, int startI, int startJ) {
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

    private void marcarAreaComoVisitada(boolean[][] visitado, Coordenada arribaIzq, Coordenada abajoDerecha) {
        for (int i = arribaIzq.getX(); i <= abajoDerecha.getX(); i++) {
            for (int j = arribaIzq.getY(); j <= abajoDerecha.getY(); j++) {
                visitado[i][j] = true;
            }
        }
    }

    private double calcularMontoInvertido(Cultivo cultivo, Coordenada arribaIzq, Coordenada abajoDerecha) {
        int numeroParcelas = (abajoDerecha.getX() - arribaIzq.getX() + 1) *
                (abajoDerecha.getY() - arribaIzq.getY() + 1);
        return (cultivo.getCostoPorParcela() * numeroParcelas) + cultivo.getInversionRequerida();
    }

    private double calcularRiesgoPromedio(Coordenada arribaIzq, Coordenada abajoDerecha, double[][] riesgos) {
        double sumaRiesgos = 0;
        int numeroParcelas = 0;

        for (int i = arribaIzq.getX(); i <= abajoDerecha.getX(); i++) {
            for (int j = arribaIzq.getY(); j <= abajoDerecha.getY(); j++) {
                sumaRiesgos += riesgos[i][j];
                numeroParcelas++;
            }
        }

        return sumaRiesgos / numeroParcelas;
    }

}