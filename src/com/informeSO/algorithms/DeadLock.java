package com.informeSO.algorithms;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class DeadLock {
    private int numProcesos;
    private int numRecursos;
    private int[] disponibles;
    private int[][] asignados;
    private int[][] request; // Para la detección de interbloqueos, 'request' es lo que están esperando.

    
    public DeadLock(int numProcesos, int numRecursos, int[]disponibles, int[][] asignados, int[][] request) {
        this.numProcesos = numProcesos;
        this.numRecursos = numRecursos;
        this.disponibles = Arrays.copyOf(disponibles, disponibles.length);
        this.asignados = asignados;
        this.request = request;
    }

    //Metodo para hacer una copia profunda de una matriz 2D
    /* 
    private int[][] clonarMatriz(int[][] original) {
        int[][] copia = new int[original.length][];
        for (int i = 0; i < original.length; i++) {
            copia[i] = Arrays.copyOf(original[i], original[i].length);
        }
        return copia;
    }
    */

    //imprimir el estado actual del sistema
    public void imprimirEstado(){
        System.out.println("\n--- Estado Actual del Sistema ---");
        System.out.println("Recursos disponibles: " + Arrays.toString(disponibles));
        
        System.out.println("\nMatriz Asignados:");
        for (int i = 0; i < numProcesos; i++) {
            System.out.println("Proceso " + i + ": " + Arrays.toString(asignados[i]));
        }
        
        System.out.println("\nMatriz Request (Solicitudes de recursos):");
        for (int i = 0; i < numProcesos; i++) {
            System.out.println("Proceso " + i + ": " + Arrays.toString(request[i]));
        }
        System.out.println("\n-------------------------------\n");
    }

    
    public boolean detectarInterbloqueo(){
        int work[] = Arrays.copyOf(disponibles, numProcesos);
        boolean finish[] = new boolean[numProcesos];
        List<Integer> procesosInterbloqueados = new ArrayList<>();

        System.out.println("\n--- Ejecutando Detección de Interbloqueos --- \n");
        System.out.println("Estado inicial de 'work': " + Arrays.toString(work));

        //Detectar si hay un cambio en cada iteración
        int procesosFinalizadosEnEstaPasada;
        do {
            procesosFinalizadosEnEstaPasada = 0;
            //boolean procesoFinalizadoEncontrado = false;

            for (int i = 0; i < numProcesos; i++) {
                if (!finish[i]) {  // Si el proceso aún no ha terminado
                    boolean puedeAsignar = true;

                    //Verificar si el proceso puede ser asignado
                    for (int j = 0; j < numRecursos; j++) {
                        if (request[i][j] > work[j]) {  //Si la solicitud es mayor que los recursos disponibles
                            puedeAsignar = false;
                            break;
                        }
                    }

                    //Si puede asignar, actualizar 'work' y marcar como finalizado
                    if (puedeAsignar) {
                        //simular que el proceso i puede ejecutarse y liberar sus recursos
                        for (int j = 0; j < numRecursos; j++) {
                            work[j] += asignados[i][j];
                        }
                        finish[i] = true;  // Marcar el proceso como finalizado
                        procesosFinalizadosEnEstaPasada++;
                        System.out.println("  Proceso P" + i + " puede ejecutar y liberar recursos. Work actual: " + Arrays.toString(work));
                    }
                }
            }
            //Si no se encontró ningún proceso que pudiera ejecutarse en esta pasada, significa que hay un interbloqueo
        } while (procesosFinalizadosEnEstaPasada > 0);  //Evitar bucles infinitos

        //Verificar si todos los procesos han finalizado
        boolean interbloqueoDetectado = false;
        for (int i = 0; i < numProcesos; i++) {
            if (!finish[i]) {  // Si hay algún proceso que no ha terminado
                procesosInterbloqueados.add(i);
                interbloqueoDetectado = true;
            }
        }

        if(interbloqueoDetectado) {
            System.out.println("Interbloqueo detectado!! Procesos interbloqueado son: " + procesosInterbloqueados);
            return true; 
        } else {
            System.out.println("No se detectó interbloqueo. Todos los procesos pueden finalizar.");
            return false;
        }
    }

    //Terminar procesos interbloqueados hasta que se resuelva el interbloqueo
    public void recuperarInterbloqueo(){
        if(!detectarInterbloqueo()) {
            System.out.println("No hay interbloqueo, no es necesario recuperar.");
            return;
        }

        System.out.println("\n--- Recuperando de Interbloqueo ---");
        while(detectarInterbloqueo()){
            //Obtener la lista de procesos interbloqueados
            List<Integer> procesosInterbloqueadosActuales = new ArrayList<>();
            int tempWork[] = Arrays.copyOf(disponibles, numRecursos);
            boolean tempFinish[] = new boolean[numProcesos];

            
            int procesosTerminadosTemp;
            do{
                procesosTerminadosTemp = 0;
                boolean procesoFinalizadoEncontradoTemp = false;

                for (int i = 0; i < numProcesos; i++) {
                    if (!tempFinish[i]) {  // Si el proceso aún no ha terminado
                        boolean puedeAsignar = true;

                        //Verificar si el proceso puede ser asignado
                        for (int j = 0; j < numRecursos; j++) {
                            if (request[i][j] > tempWork[j]) {  //Si la solicitud es mayor que los recursos disponibles
                                puedeAsignar = false;
                                break;
                            }
                        }

                        //Si puede asignar, actualizar 'work' y marcar como finalizado
                        if (puedeAsignar) {
                            //simular que el proceso i puede ejecutarse y liberar sus recursos
                            for (int j = 0; j < numRecursos; j++) {
                                tempWork[j] += asignados[i][j];
                            }
                            tempFinish[i] = true;  // Marcar el proceso como finalizado
                            procesoFinalizadoEncontradoTemp = true;
                            procesosTerminadosTemp++;
                        }
                    }
                }
            }while (procesosTerminadosTemp > 0);  // Evitar bucles infinitos
            for (int i = 0; i < numProcesos; i++) {
                if (!tempFinish[i]) {  // Si hay algún proceso que no ha terminado
                    procesosInterbloqueadosActuales.add(i);
                }
            }

            if(procesosInterbloqueadosActuales.isEmpty()) {
                System.out.println("Todos los procesos han finalizado, no hay interbloqueo.");
                break;
            }

            //Primer proceso interbloqueado encontrado
            int victimaId = procesosInterbloqueadosActuales.get(0);
            System.out.println("Proceso P" + victimaId + " seleccionado como proceso víctima para terminar.");

            // Liberar recursos del proceso víctima
            for (int j = 0; j < numRecursos; j++) {
                disponibles[j] += asignados[victimaId][j];
                asignados[victimaId][j] = 0;  // El proceso víctima ya no tiene recursos asignados
                request[victimaId][j] = 0;  // El proceso víctima ya no está solicitando recursos
            }

            //Asegurar que el proceso victima ya no se considere en futuras detecciones de interbloqueo
            imprimirEstado();
            System.out.println("  Recursos de P" + victimaId + " liberados. Verificando nuevamente el interbloqueo...");
        }
        System.out.println("\n--- Recuperación de interbloqueo completada ---");
    }

    public static void main(String[] args) {
        int numProcesos = 3;
        int numRecursos = 3;

        System.out.println("\n--- Escenario 1: Sin Interbloqueo ---");
        int[] disponible1 = {1, 5, 2}; // R0, R1, R2
        //int[] disponible1 = {1, 5, 2}; // R0, R1, R2

        int[][] asignado1 = {
                {0, 1, 0}, // P0
                {2, 0, 0}, // P1
                {3, 0, 3}  // P2
        };

        /* 
        int[][] asigando1 = {
                {1, 0, 0}, // P0 tiene R0, espera R1
                {0, 1, 0}, // P1 tiene R1, espera R0
                {0, 0, 1}  // P2 tiene R2, no involucrado
        };
        */
        int[][] request1 = {
                {0, 0, 0}, // P0
                {0, 2, 0}, // P1
                {0, 0, 0}  // P2
        };

        /*
        int[][] request1 = { // Piden lo que el otro tiene
                {0, 1, 0}, // P0 solicita R1 (retenido por P1)
                {1, 0, 0}, // P1 solicita R0 (retenido por P0)
                {0, 0, 0}  // P2 no solicita nada
        */


        DeadLock deadlock1 = new DeadLock(numProcesos, numRecursos, disponible1, asignado1, request1);
        deadlock1.imprimirEstado();
        deadlock1.detectarInterbloqueo();   // detectar interbloqueo
        deadlock1.recuperarInterbloqueo(); // si detecta interbloqueo, recuperar y resolverlo
        //deadlock1.imprimirEstado();  //Imprimir estado final después de recuperación
        //deadlock1.detectarInterbloqueo();   verificar si se resolvio el interbloqueo
    }
}
