package Project;

import Lib.Coordenada;
import Lib.Cultivo;
import Lib.CultivoSeleccionado;
import Lib.PlanificarCultivos;

import java.util.*;
import java.util.stream.Collectors;

import static Project.Utils.*;

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

}