package Project;

import Lib.Coordenada;
import Lib.Cultivo;
import Lib.CultivoSeleccionado;
import Lib.PlanificarCultivos;

import javax.swing.*;
import java.util.*;
import java.util.stream.Collectors;

import static Project.Utils.*;

public class PlanificarCultivosImplementacion implements PlanificarCultivos {

    @Override
    public List<CultivoSeleccionado> obtenerPlanificacion(List<Cultivo> cultivosDisponibles, double[][] riesgos, String temporada) {

        cultivosDisponibles.removeIf(cultivo -> !cultivo.getTemporadaOptima().equals(temporada));

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
        if (indiceCultivo >= cultivosDisponibles.size()-1) {
            if (gananciaParcial > mejorGanancia) {
                mejorGanancia = gananciaParcial;
                return new ArrayList<>(cultivoSeleccionados); // Guardar la mejor configuración
            }
            return cultivoSeleccionados; // Retornamos la configuración actual
        }

        Cultivo cultivoActual = cultivosDisponibles.get(indiceCultivo);
        List<CultivoSeleccionado> mejorSeleccion = new ArrayList<>(cultivoSeleccionados); // Copia la configuración actual

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
                                CultivoSeleccionado cultivoSeleccionado = new CultivoSeleccionado(
                                        cultivoActual.getNombre(), arribaIzq, abajoDerecha,
                                        calcularMontoInvertido(cultivoActual, arribaIzq, abajoDerecha),
                                        RiesgoAsociado(arribaIzq, abajoDerecha, riesgos),
                                        calcularGananciaArea(cultivoActual, arribaIzq, abajoDerecha, riesgos)
                                );
                                cultivoSeleccionados.add(cultivoSeleccionado);

                                // Marcar la matriz de cultivos
                                Utils.marcarMatrizCultivos(cultivoActual, arribaIzq, abajoDerecha, matrizCultivos);

                                // Llamada recursiva con la nueva ganancia
                                double nuevaGanancia = gananciaParcial + calcularGananciaArea(cultivoActual, arribaIzq, abajoDerecha, riesgos);
                                List<CultivoSeleccionado> resultadoRecursivo = backtracking(
                                        cultivosDisponibles, cultivoSeleccionados, riesgos,
                                        nuevaGanancia, mejorGanancia, indiceCultivo + 1,
                                        matrizCultivos, cultivoSet
                                );

                                // Actualizamos el mejor resultado si es necesario
                                if (nuevaGanancia > mejorGanancia) {
                                    mejorGanancia = nuevaGanancia;
                                    System.out.println(mejorGanancia);
                                    mejorSeleccion = resultadoRecursivo;
                                }
                                System.out.println(cultivoActual.getNombre() + " " + nuevaGanancia);
                                // Retroceder: desmarcar matriz y remover el cultivo
                                Utils.desmarcarMatrizCultivos(arribaIzq, abajoDerecha, matrizCultivos);
                                cultivoSeleccionados.remove(cultivoSeleccionado);
                            }
                        }
                    }
                }
            }
        }

        // Intentamos avanzar al siguiente cultivo aunque no se haya colocado el actual
        List<CultivoSeleccionado> resultadoSinColocar = backtracking(
                cultivosDisponibles, cultivoSeleccionados, riesgos,
                gananciaParcial, mejorGanancia, indiceCultivo + 1,
                matrizCultivos, cultivoSet
        );

        // Si la configuración sin el cultivo actual es mejor, usamos esa
        return resultadoSinColocar.size() > mejorSeleccion.size() ? resultadoSinColocar : mejorSeleccion;

}

}