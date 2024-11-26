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

        List<CultivoSeleccionado> resultado = backtracking(cultivosDisponibles, cultivoSeleccionados, riesgos,gananciaParcial ,mejorGananciaGlobal, 0, matrizCultivos);
        return cultivoSeleccionados;
    }

    private List<CultivoSeleccionado> backtracking(
            List<Cultivo> cultivosDisponibles,
            List<CultivoSeleccionado> cultivoSeleccionados,
            double[][] riesgos,
            double gananciaParcial,
            double mejorGanancia,
            int indiceCultivo, //nivel
            String[][] matrizCultivos) {


        // Si llegamos al final, actualizamos la ganancia global si es necesario
        if (indiceCultivo >= cultivosDisponibles.size()) {
            if (gananciaParcial > mejorGanancia) {
                mejorGanancia = gananciaParcial;  // Actualizar mejor ganancia
                return new ArrayList<>(cultivoSeleccionados); // Guardar la configuración
            }
            double gananciaParametro = 0;
            return backtrackingRepetidos(
                    cultivosDisponibles, cultivoSeleccionados, riesgos,
                    gananciaParcial, gananciaParametro, 0, matrizCultivos
            );
        }

        Cultivo cultivoActual = cultivosDisponibles.get(indiceCultivo);

        List<CultivoSeleccionado> mejorSeleccion = new ArrayList<>(cultivoSeleccionados); // Copia la configuración actual

        for (int i = 0; i < riesgos.length; i++) {
            for (int j = 0; j < riesgos[i].length; j++) {
                if (matrizCultivos[i][j] != null) {
                    continue;
                }
                for (int alto = 1; alto <= Math.min(11, riesgos.length - i); alto++) {
                    for (int ancho = 1; ancho + alto <= Math.min(11, riesgos[0].length - j); ancho++) {

                        Coordenada arribaIzq = new Coordenada(i, j);
                        Coordenada abajoDerecha = new Coordenada(i + alto - 1, j + ancho - 1);

                        // Validación del área para evitar cultivos solapados
                        // Verificamos que la colocación no se salga del campo ni solape con otros cultivos
                        if (abajoDerecha.getX() < riesgos.length && abajoDerecha.getY() < riesgos[0].length) {
                            if (Utils.areaLibre(arribaIzq, abajoDerecha, matrizCultivos)) {

                                //Calcular la ganancia y agregar el cultivo
                                double montoInvertido = calcularMontoInvertido(cultivoActual, arribaIzq, abajoDerecha);
                                double riesgoAsociado = RiesgoAsociado(arribaIzq, abajoDerecha, riesgos);
                                double gananciaArea = calcularGananciaArea(cultivoActual, arribaIzq, abajoDerecha, riesgos);

                                CultivoSeleccionado cultivoSeleccionado = new CultivoSeleccionado(
                                        cultivoActual.getNombre(), arribaIzq, abajoDerecha,
                                        montoInvertido, (int) riesgoAsociado, gananciaArea
                                );

                                // Se agrega el cultivo a la lista de cultivos seleccionados
                                cultivoSeleccionados.add(cultivoSeleccionado);

                                // Marcar la matriz de cultivos con el área ocupada
                                Utils.marcarMatrizCultivos(cultivoActual, arribaIzq, abajoDerecha, matrizCultivos);


                                // Llamada recursiva para explorar el siguiente cultivo
                                double nuevaGanancia = gananciaParcial + gananciaArea;

                                List<CultivoSeleccionado> resultadoRecursivo = backtracking(
                                        cultivosDisponibles, cultivoSeleccionados, riesgos,
                                        nuevaGanancia, mejorGanancia, indiceCultivo + 1,
                                        matrizCultivos
                                );

                                // Actualizamos el mejor resultado si es necesario
                                if (nuevaGanancia > mejorGanancia) {
                                    mejorGanancia = nuevaGanancia;
                                    mejorSeleccion = resultadoRecursivo;
                                }

                                // desmarcar matriz y remover el cultivo
                                Utils.desmarcarMatrizCultivos(arribaIzq, abajoDerecha, matrizCultivos);
                                cultivoSeleccionados.remove(cultivoSeleccionado);
                            }
                        }
                    }
                }
            }
        }

        // Intentamos avanzar al siguiente cultivo sin colocar el actual
        List<CultivoSeleccionado> resultadoSinColocar = backtracking(
                cultivosDisponibles, cultivoSeleccionados, riesgos,
                gananciaParcial, mejorGanancia, indiceCultivo + 1,
                matrizCultivos
        );

        // Retornamos el mejor resultado encontrado entre colocar o no colocar el cultivo
        return resultadoSinColocar.size() > mejorSeleccion.size() ? resultadoSinColocar : mejorSeleccion;
    }


    private List<CultivoSeleccionado> backtrackingRepetidos(
            List<Cultivo> cultivosDisponibles,
            List<CultivoSeleccionado> cultivoSeleccionados,
            double[][] riesgos,
            double gananciaParcial,
            double mejorGananciaAnterior,
            int indiceCultivo, //nivel
            String[][] matrizCultivos) {

        // Si hemos explorado todos los cultivos
        if (indiceCultivo >= cultivosDisponibles.size()) {
            return new ArrayList<>(cultivoSeleccionados);
        }

        Cultivo cultivoActual = cultivosDisponibles.get(indiceCultivo);

        List<CultivoSeleccionado> mejorSeleccion = new ArrayList<>(cultivoSeleccionados);

        // Evaluar todos los posibles rectángulos en la matriz para repetir el cultivo
        for (int i = 0; i < riesgos.length; i++) {
            for (int j = 0; j < riesgos[i].length; j++) {
                if (matrizCultivos[i][j] != null) continue; // Saltar si el espacio ya está ocupado

                for (int alto = 1; alto <= Math.min(11, riesgos.length - i); alto++) {
                    for (int ancho = 1; ancho + alto <= Math.min(11, riesgos[0].length - j); ancho++) {

                        Coordenada arribaIzq = new Coordenada(i, j);
                        Coordenada abajoDerecha = new Coordenada(i + alto - 1, j + ancho - 1);

                        // Verificar si el área es válida para plantar el cultivo actual
                        if (abajoDerecha.getX() < riesgos.length && abajoDerecha.getY() < riesgos[0].length) {
                            if (Utils.sePuedePlantar(arribaIzq, abajoDerecha, matrizCultivos, cultivoActual.getNombre())) {

                                // Calcular ganancia, monto invertido y riesgo asociado para el área
                                double gananciaArea = calcularGananciaArea(cultivoActual, arribaIzq, abajoDerecha, riesgos);
                                double montoInvertido = calcularMontoInvertido(cultivoActual, arribaIzq, abajoDerecha);
                                double riesgoAsociado = RiesgoAsociado(arribaIzq, abajoDerecha, riesgos);

                                // Si la ganancia de esta área es mejor, actualizamos la selección
                                if (gananciaArea > mejorGananciaAnterior) {
                                    mejorGananciaAnterior = gananciaArea;

                                    CultivoSeleccionado cultivoSeleccionado = new CultivoSeleccionado(
                                            cultivoActual.getNombre(), arribaIzq, abajoDerecha,
                                            montoInvertido, (int) riesgoAsociado, mejorGananciaAnterior
                                    );

                                    // Marcar el área en la matriz y agregar el cultivo a la selección
                                    Utils.marcarMatrizCultivos(cultivoActual, arribaIzq, abajoDerecha, matrizCultivos);
                                    cultivoSeleccionados.add(cultivoSeleccionado);


                                    // Llamada recursiva
                                    List<CultivoSeleccionado> resultadoRecursivo = backtrackingRepetidos(
                                            cultivosDisponibles, cultivoSeleccionados, riesgos,
                                            gananciaParcial + gananciaArea, mejorGananciaAnterior,
                                            indiceCultivo + 1, matrizCultivos
                                    );

                                    // Actualizar la mejor selección si corresponde
                                    if (resultadoRecursivo.size() > mejorSeleccion.size()) {
                                        mejorSeleccion = resultadoRecursivo;
                                    }

                                    // Desmarcar la matriz y remover el cultivo para backtracking
                                    Utils.desmarcarMatrizCultivos(arribaIzq, abajoDerecha, matrizCultivos);
                                    cultivoSeleccionados.remove(cultivoSeleccionado);
                                }
                            }
                        }
                    }
                }
            }
        }

        // Retornar la mejor selección después de explorar todas las opciones
        return mejorSeleccion;
    }


}