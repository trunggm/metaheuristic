/**
 * En este paquete se encuentra los algoritmos para realizar el clustering.
 */
package es.ujaen.metaheuristicas.pr1.algorithms;

import es.ujaen.metaheuristicas.pr1.clustering.Cluster;
import es.ujaen.metaheuristicas.pr1.clustering.Functions;
import es.ujaen.metaheuristicas.pr1.clustering.Pattern;
import java.util.List;
import java.util.Random;

/**
 * Algoritmo GRASP
 *
 * @author Raúl Moya Reyes <www.raulmoya.es>
 */
public class GRASP {

    /**
     * Método para lanzar el algorimo de búsqueda tabú.
     *
     * @param fileName Nombre del archivo para realizar la carga de datos.
     * @param seed Semilla a utilizar para la generación de la solución inicial.
     * @param numberClusters Número de clusters a generar.
     * @param threshold Umbral de los candidatos de la lista restringida para la
     * construcción de la solución greedy.
     * @return Float con el coste de la solución.
     */
    public static float init(String fileName, Integer seed, Integer numberClusters, Double threshold) {

        /* Conjunto de todos los patrones */
        List<Pattern> patterns = Functions.readData(fileName);
        /* Números aleatorios a partir de una semilla */
        Random rand = new Random(seed);

        /* Declaración de variables */
        List<Cluster> clusters = null;
        List<Pattern> centroids = null;
        float greedyCost = 0;
        float initialCost = 0;
        float cost = 0;

        /* Inicio de la cuenta del tiempo de ejecución */
        Long time = System.currentTimeMillis();

        /* Se ejecuta un mínimo de 25 veces */
        int execution = 25;

        /* Se ejecuta 25 veces */
        for (int i = 0; i < execution; i++) {
            /* Asignación de los patrones a cada cluster */
            clusters = Functions.setGreedy(patterns, rand, numberClusters, threshold);
            /* Cálculo del centroide de cada cluster */
            centroids = Functions.calculateCentroids(clusters);
            /* Coste de la solución greedy */
            greedyCost = Functions.objectiveFunction(clusters, centroids);
            /* Inicialización del coste final y su coste inicial con la solución greedy */
            cost = greedyCost;
            initialCost = greedyCost;

            /* Coste de la solición actual usando BL */
            float actualCost = run(clusters, centroids, greedyCost);

            if (cost > actualCost) {
                cost = actualCost;
                initialCost = greedyCost;
            }
        }

        /* Fin de la cuenta del tiempo de ejecución */
        time = System.currentTimeMillis() - time;
        /* Imprimir contenido en consola */
        Functions.print(fileName, patterns, clusters, centroids, seed,
                initialCost, cost, time);

        return cost;
    }

    /**
     * Método con el algoritmo de búsqueda local sin utilizar ningún método de
     * generación de solución inicial.
     *
     * @param clusters Clusters con los patrones asignados.
     * @param centroids Centroides de cada cluster.
     * @param initialCost Coste de la solución inicial.
     * @return Float con el coste de la solución generada mediante BL.
     */
    public static Float run(List<Cluster> clusters, List<Pattern> centroids, float initialCost) {

        /* Coste de la solución actual */
        float cost = initialCost;

        /* Itera si hay mejora */
        boolean improvement = true;
        /* Itera como máximo 10000 veces */
        int iterations = 10000;

        /* Repite hasta que supere el núemro de iteraciones o no mejore */
        for (int count = 0; improvement && count < iterations;) {
            improvement = false;
            /* Recorre los cluster */
            for (int i = 0; i < clusters.size(); i++) {
                /* Recorre los patrones */
                Cluster cluster = clusters.get(i);
                for (int j = 0; j < cluster.size(); j++) {
                    /* Repite comprobando el patrón en todos los clusters */
                    for (int assigned = 0; assigned < clusters.size(); assigned++) {
                        if (assigned != i && cluster.size() > 3) {
                            /* Obtiene el valor de mejora de la solución */
                            Float objective = Functions.factorize(clusters, centroids, assigned, i, j);
                            count++;
                            /* Si es negativo es porque mejora, entonces recalcula */
                            if (objective < 0) {
                                improvement = true;
                                cost += objective;
                                /* Recalcular los centroides */
                                centroids = Functions.calculateCentroids(clusters);
                                /* Cambio del patrón al nuevo cluster */
                                clusters.get(assigned).add(cluster.remove(j));
                                /* Pasa al siguiente cluster */
                                j--;
                                assigned = clusters.size();
                            }
                        }
                    }
                }
            }
        }

        return cost;
    }
}
