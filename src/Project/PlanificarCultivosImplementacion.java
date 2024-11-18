package Project;

import Lib.Coordenada;
import Lib.Cultivo;
import Lib.CultivoSeleccionado;
import Lib.PlanificarCultivos;

import java.util.*;
import java.util.stream.Collectors;

import static Project.Utils.*;

public class PlanificarCultivosImplementacion implements PlanificarCultivos {

    @Override
    public List<CultivoSeleccionado> obtenerPlanificacion(List<Cultivo> cultivosDisponibles, double[][] riesgos, String temporada) {

        //filtramos por temporada
        for (int i = 0; i < cultivosDisponibles.size(); i++) {
            Cultivo cultivo = cultivosDisponibles.get(i);
            if (!cultivo.getTemporadaOptima().equals(temporada)) {
                cultivosDisponibles.remove(cultivo);
            }
        }
        double mejorGananciaGlobal = Double.NEGATIVE_INFINITY;
        double gananciaParcial = 0;
        List <CultivoSeleccionado> cultivoSeleccionados = new ArrayList<>();
        String [][] matrizCultivos = new String[riesgos.length][riesgos[0].length]; //copiamos la matriz de riesgo para tener el mismo tamañp
        Set <Cultivo> cultivoSet = new HashSet<>();

        List<CultivoSeleccionado> resultado = backtracking(cultivosDisponibles, cultivoSeleccionados, riesgos,gananciaParcial ,mejorGananciaGlobal, 0, matrizCultivos, cultivoSet);
        System.out.println(resultado);
        return cultivoSeleccionados;
    }



    private List<CultivoSeleccionado> backtracking(
            List<Cultivo> cultivosDisponibles,
            List<CultivoSeleccionado> cultivoSeleccionados,
            double[][] riesgos,
            double gananciaParcial,
            double mejorGanancia,
            int indiceCultivo, //nivel
            String[][] matrizCultivos,
            Set<Cultivo> cultivoSet) {

        // Si llegamos al final, guardamos la configuración si es la mejor hasta ahora
        if (indiceCultivo >= cultivosDisponibles.size()) {
            if (gananciaParcial > mejorGanancia) {
                mejorGanancia = gananciaParcial;
                //guardarMejorConfiguracion(riesgos, matrizCultivos, cultivoSeleccionados);
            }
            return cultivoSeleccionados;  // Retornamos la configuración con la mejor ganancia
        }

        Cultivo cultivoActual = cultivosDisponibles.get(indiceCultivo);

        for (int i = 0; i < riesgos.length; i++) {
            for (int j = 0; j < riesgos[i].length; j++) {
                for (int alto = 1; alto <= Math.min(11, riesgos.length - i); alto++) {
                    for (int ancho = 1; ancho + alto <= Math.min(11, riesgos[0].length - j); ancho++) {
                        Coordenada arribaIzq = new Coordenada(i, j);
                        Coordenada abajoDerecha = new Coordenada(i + alto - 1, j + ancho - 1);

                        // Validamos que las coordenadas no excedan los límites de la matriz
                        if (abajoDerecha.getX() < riesgos.length && abajoDerecha.getY() < riesgos[0].length) {
                            if (Utils.esAreaValida(arribaIzq, abajoDerecha, matrizCultivos, cultivoActual)) {

                                // Se agrega el cultivo a la lista de cultivos seleccionados
                                CultivoSeleccionado cultivoSeleccionado = new CultivoSeleccionado(cultivoActual.getNombre(), arribaIzq,
                                        abajoDerecha, calcularMontoInvertido(cultivoActual, arribaIzq, abajoDerecha),
                                        RiesgoAsociado(arribaIzq, abajoDerecha, riesgos),
                                        calcularGananciaArea(cultivoActual, arribaIzq, abajoDerecha, riesgos));
                                cultivoSeleccionados.add(cultivoSeleccionado);
                                // Marcar la matriz de cultivos
                                marcarMatrizCultivos(cultivoActual, arribaIzq, abajoDerecha, matrizCultivos);
                                System.out.println("Cultivo seleccionado: " + cultivoSeleccionado);
                                System.out.println(gananciaParcial);
                                cultivoSet.add(cultivoActual);

                                // Llamada recursiva con la nueva ganancia
                                double nuevaGanancia = gananciaParcial + calcularGananciaArea(cultivoActual, arribaIzq, abajoDerecha, riesgos);
                                List<CultivoSeleccionado> resultadoRecursivo = backtracking(cultivosDisponibles, cultivoSeleccionados, riesgos, nuevaGanancia, mejorGanancia, indiceCultivo + 1, matrizCultivos, cultivoSet);

                                // Si el resultado recursivo tiene una mejor ganancia, actualizamos
                                if (calcularGananciaArea(cultivoActual, arribaIzq, abajoDerecha, riesgos) > mejorGanancia) {
                                    cultivoSeleccionados = resultadoRecursivo;  // Asignamos el mejor resultado
                                }

                                // Retroceder: desmarcar matriz y remover el cultivo
                                Utils.desmarcarMatrizCultivos(arribaIzq, abajoDerecha, matrizCultivos);
                                cultivoSeleccionados.remove(cultivoSeleccionado);
                                cultivoSet.remove(cultivoActual);
                            }
                        }
                    }
                }
            }
        }

        return cultivoSeleccionados;  // Retorna la mejor selección de cultivos
    }

}