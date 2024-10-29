
import Lib.Cultivo;
import Lib.CultivoSeleccionado;
import Project.PlanificarCultivosImplementacion;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        PlanificarCultivosImplementacion planificador = new PlanificarCultivosImplementacion();

        List<Cultivo> cultivos = new ArrayList<>();

        Cultivo cultivo = new Cultivo();
        cultivo.setNombre("Lechuga");
        cultivo.setCostoPorParcela(100.5);
        cultivo.setInversionRequerida(2000);
        cultivo.setPrecioDeVentaPorParcela(500);
        cultivo.setTemporadaOptima("Otoño");
        cultivos.add(cultivo);

        cultivo = new Cultivo();
        cultivo.setNombre("Remolacha");
        cultivo.setCostoPorParcela(105);
        cultivo.setInversionRequerida(1000);
        cultivo.setPrecioDeVentaPorParcela(450);
        cultivo.setTemporadaOptima("Otoño");
        cultivos.add(cultivo);

        cultivo = new Cultivo();
        cultivo.setNombre("Calabaza");
        cultivo.setCostoPorParcela(97);
        cultivo.setInversionRequerida(1500);
        cultivo.setPrecioDeVentaPorParcela(475);
        cultivo.setTemporadaOptima("Otoño");
        cultivos.add(cultivo);

        cultivo = new Cultivo();
        cultivo.setNombre("Tomate");
        cultivo.setCostoPorParcela(110.0);
        cultivo.setInversionRequerida(2500);
        cultivo.setPrecioDeVentaPorParcela(600);
        cultivo.setTemporadaOptima("Verano");
        cultivos.add(cultivo);

        cultivo = new Cultivo();
        cultivo.setNombre("Zanahoria");
        cultivo.setCostoPorParcela(90.0);
        cultivo.setInversionRequerida(1200);
        cultivo.setPrecioDeVentaPorParcela(430);
        cultivo.setTemporadaOptima("Primavera");
        cultivos.add(cultivo);

        cultivo = new Cultivo();
        cultivo.setNombre("Maíz");
        cultivo.setCostoPorParcela(130.0);
        cultivo.setInversionRequerida(2200);
        cultivo.setPrecioDeVentaPorParcela(580);
        cultivo.setTemporadaOptima("Verano");
        cultivos.add(cultivo);

        cultivo = new Cultivo();
        cultivo.setNombre("Espinaca");
        cultivo.setCostoPorParcela(85.0);
        cultivo.setInversionRequerida(1400);
        cultivo.setPrecioDeVentaPorParcela(460);
        cultivo.setTemporadaOptima("Invierno");
        cultivos.add(cultivo);

        cultivo = new Cultivo();
        cultivo.setNombre("Brócoli");
        cultivo.setCostoPorParcela(115.0);
        cultivo.setInversionRequerida(1800);
        cultivo.setPrecioDeVentaPorParcela(510);
        cultivo.setTemporadaOptima("Otoño");
        cultivos.add(cultivo);


        double[][] riesgos = new double[100][100];
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                riesgos[i][j] = (i + j) / 200.0;
            }
        }
        imprimirMatrizDeRiesgos(riesgos);

        List<CultivoSeleccionado> res = planificador.obtenerPlanificacion(cultivos, riesgos, "Otoño");
        imprimirResultado(res);
    }

    private static void imprimirMatrizDeRiesgos(double[][] riesgos) {
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                System.out.print(riesgos[i][j] + "\t");
            }
            System.out.println();
        }
    }

    private static void imprimirResultado(List<CultivoSeleccionado> res) {
        for (CultivoSeleccionado cultivo : res) {
            System.out.println(cultivo);
        }
    }
}