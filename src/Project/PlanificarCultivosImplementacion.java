package Project;

import Lib.Coordenada;
import Lib.Cultivo;
import Lib.CultivoSeleccionado;
import Lib.PlanificarCultivos;

import java.util.*;
import java.util.stream.Collectors;

import static Project.Utils.*;

public class PlanificarCultivosImplementacion implements PlanificarCultivos {
    private static double mejorGananciaGlobal = Double.NEGATIVE_INFINITY;

    @Override
    public List<CultivoSeleccionado> obtenerPlanificacion(List<Cultivo> cultivosDisponibles, double[][] riesgos, String temporada) {

        //filtramos por temporada
        for (Cultivo cultivo: cultivosDisponibles) {
            if (!cultivo.getTemporadaOptima().equals(temporada)) {
                cultivosDisponibles.remove(cultivo);
            }
        }

        List <CultivoSeleccionado> cultivoSeleccionados = new ArrayList<>();
        Cultivo [][] matrizCultivos = new Cultivo[riesgos.length][riesgos[0].length]; //copiamos la matriz de riesgo para tener el mismo tamañp
        Set <Cultivo> cultivoSet = new HashSet<>();

        double ganancia = backtracking(cultivosDisponibles, cultivoSeleccionados, riesgos, mejorGananciaGlobal, 0, matrizCultivos, cultivo, cultivoSet, 0, 0);
        System.out.println(ganancia);
        return cultivoSeleccionados;
    }



    private double backtracking(
            List<Cultivo> cultivosDisponibles,
            List<CultivoSeleccionado> cultivoSeleccionados,
            double[][] riesgos,
            double gananciaAnterior,
            int indiceCultivo, //nivel
            Cultivo cultivoRepetible,
            Set<Cultivo> cultivoSet) {

        // Si llegamos al final, guardamos la configuración si es la mejor hasta ahora
        if (indiceCultivo >= cultivosDisponibles.size()) {
            if (gananciaAnterior > mejorGananciaGlobal) {
                mejorGananciaGlobal = gananciaAnterior;
                //guardarMejorConfiguracion(riesgos, matrizCultivos, cultivoSeleccionados);
            }
            return gananciaAnterior;
        }

        Cultivo cultivoActual = cultivosDisponibles.get(indiceCultivo);

        for (int i = 0; i < riesgos.length; i++) {
            for (int j = 0; j < riesgos[i].length; j++) {
                for (int alto = 1; alto <= 11; alto++) {
                    for (int ancho = 1; ancho + alto <= 11; ancho++) {

                        Coordenada arribaIzq = new Coordenada(i, j);
                        Coordenada abajoDerecha = new Coordenada(i + alto - 1, j + ancho - 1);

                        if (esAreaValida(cultivoActual, arribaIzq, abajoDerecha, matrizCultivos)) { //arreglar funcion
                            if (convienePlantarlo(cultivoActual, gananciaAnterior, riesgos, arribaIzq, abajoDerecha)) {

                                //se agrega el cultivo a la lista de cultivos seleccionados
                                cultivoSeleccionados.add(new CultivoSeleccionado(cultivoActual.getNombre(), arribaIzq,
                                        abajoDerecha, calcularMontoInvertido(cultivoActual, arribaIzq, abajoDerecha),
                                        RiesgoAsociado(arribaIzq, abajoDerecha, riesgos),
                                        calcularGananciaArea(cultivoActual,arribaIzq, abajoDerecha, riesgos)));

                                cultivoSet.add(cultivoActual);
                                double nuevaGanancia = gananciaAnterior + calcularGananciaArea(cultivoActual, arribaIzq, abajoDerecha, riesgos);

                                //LLAMAR AL BACKTRACKING
                                backtracking(cultivosDisponibles, cultivoSeleccionados, riesgos, gananciaAnterior, indiceCultivo + 1, )

                                removerCultivoDeArea(arribaIzq, abajoDerecha, matrizCultivos);
                                cultivoSet.remove(cultivoActual);
                            }
                        }
                        }
                    }
                }
            }

        return gananciaAnterior;
        //return CultivosSeleccionados
    }

}