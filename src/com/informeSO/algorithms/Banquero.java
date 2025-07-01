package com.informeSO.algorithms;


import java.util.Arrays;


public class Banquero {
    private int numProcesos;
    private int numRecursos;
    // Vector de recursos disponibles
    private int[] disponibles;
    // Matriz de máxima demanda de recursos por proceso
    private int[][] maxima;
    // Matriz de asignación de recursos a procesos
    private int[][] asignacion;
    // Matriz de recursos necesarios por proceso
    private int[][] necesario;

    // Constructor de la clase
    public Banquero(int numProcesos, int numRecursos, int[] disponibles, int[][] maxima, int[][] asignacion) {
        this.numProcesos = numProcesos;
        this.numRecursos = numRecursos;
        this.disponibles = new int[numRecursos];
        this.maxima = new int[numProcesos][numRecursos];
        this.asignacion = new int[numProcesos][numRecursos];
        this.necesario = new int[numProcesos][numRecursos];

        // Copiar matrices y vectores
        for(int i = 0; i < numProcesos; i++) {
            this.maxima[i] = Arrays.copyOf(maxima[i], numRecursos);
            this.asignacion[i] = Arrays.copyOf(asignacion[i], numRecursos);
        }
        this.disponibles = Arrays.copyOf(disponibles, numRecursos);
        
        // Llamar a un método para calcular la matriz Need inicialmente
        calcularNecesario();
    }

    // calcular la matriz Need
    // need[i][j] = max[i][j] - allocation[i][j]
    private void calcularNecesario() {
        for (int i = 0; i < numProcesos; i++) {
            for (int j = 0; j < numRecursos; j++) {
                necesario[i][j] = maxima[i][j] - asignacion[i][j];
            }
        }
    }

    // imprimir el estado actual del sistema 
    public void printState() {
        // Implementación para mostrar las matrices y vectores
        System.out.println("\n--- Estado Actual del Sistema ---");
        System.out.println("Recursos Disponibles: " + Arrays.toString(disponibles));
        
        System.out.println();

        System.out.println("Matriz Máxima:");
        for (int i = 0; i < numProcesos; i++) {
            System.out.println("P" + i + ": " + Arrays.toString(maxima[i]));
        }

        System.out.println();
        System.out.println("Matriz Asignación:");
        for (int i = 0; i < numProcesos; i++) {
            System.out.println("P" + i + ": " + Arrays.toString(asignacion[i]));
        }

        System.out.println();
        System.out.println("Matriz Necesaria:");
        for (int i = 0; i < numProcesos; i++) {
            System.out.println("P" + i + ": " + Arrays.toString(necesario[i]));
        }

        System.out.println("---------------------------------");
    }

    // Algoritmo de Seguridad: Verifica si el sistema está en un estado seguro
    // Retorna true si es seguro, false de lo contrario.
    public boolean esSeguro() {
        int[] work = Arrays.copyOf(disponibles, numRecursos);
        boolean[] finish = new boolean[numProcesos];
        // Para almacenar la secuencia segura
        int[] secuenciaSegura = new int[numProcesos];
        int contador = 0;

        System.out.println("\n--- Ejecutando Algoritmo de Seguridad ---");
        System.out.println("Work inicial: " + Arrays.toString(work));

        // Bucle principal para encontrar la secuencia segura
        while (contador < numProcesos) {
            boolean found = false;
            for (int i = 0; i < numProcesos; i++) {
                if (!finish[i]) {
                    // Verificar si la necesidad del proceso i puede ser satisfecha con work
                    boolean puedeAsignar = true;
                    for (int j = 0; j < numRecursos; j++) {
                        if (necesario[i][j] > work[j]) {
                            puedeAsignar = false;
                            break; // No puede satisfacerse, pasar al siguiente proceso
                        }
                    }
                    // Si el proceso i puede ejecutar
                    if (puedeAsignar) {
                        // Simular la asignación de recursos
                        for (int j = 0; j < numRecursos; j++) {
                            work[j] += asignacion[i][j]; // Liberar recursos asignados
                        }
                        finish[i] = true; // Marcar como terminado
                        secuenciaSegura[contador] = i; //Añadir a la secuencia segura
                        contador++;
                        found = true; // Se encontró un proceso seguro
                        System.out.println("Proceso P" + i + " puede ejecutarse. Work actualizado: " + Arrays.toString(work));
                        break; // Salir del bucle para reiniciar la búsqueda
                    }
                }
            }
            // Si no se encontró ningún proceso que pueda ejecutarse
            if (!found) {
                System.out.println("\n No se encontró un proceso que pueda ejecutarse. El sistema está en estado inseguro.");
                return false; // No se puede encontrar una secuencia segura
            }
        }
        System.out.println("\\n El sistema está en estado seguro. Secuencia segura: " + Arrays.toString(secuenciaSegura));
        return true; // Se encontró una secuencia segura
    }

    // Algoritmo de Solicitud de Recursos
    public boolean solicitarRecursos(int procesoId, int[] request) {
        System.out.println("\n--- Solicitud de Recursos ---");
        System.out.println("Proceso P" + procesoId + " solicita: " + Arrays.toString(request));

        // 1. Verificar Request <= Need
        for (int j = 0; j < numRecursos; j++) {
            if (request[j] > necesario[procesoId][j]) {
                System.out.println("  Error: La solicitud excede la necesidad máxima declarada de P" + procesoId);
                return false;
            }
        }

        // 2. Verificar Request <= Disponibles
        for (int j = 0; j < numRecursos; j++) {
            if (request[j] > disponibles[j]) {
                System.out.println("  Proceso P" + procesoId + " debe esperar. Recursos no disponibles actualmente.\n");
                return false; // No hay suficientes recursos disponibles
            }
        }

        // 3. Asignación hipotética
        // Guardar el estado actual para una posible reversión
        int[][] originalAsignacion = new int[numProcesos][numRecursos];
        int[][] originalNecesario = new int[numProcesos][numRecursos];
        int[] originalDisponibles = Arrays.copyOf(disponibles, numRecursos);
        for (int i = 0; i < numProcesos; i++) {
            originalAsignacion[i] = Arrays.copyOf(asignacion[i], numRecursos);
            originalNecesario[i] = Arrays.copyOf(necesario[i], numRecursos);
        }

        // Simular la asignación de recursos
        for (int j = 0; j < numRecursos; j++) {
            disponibles[j] -= request[j];
            asignacion[procesoId][j] += request[j];
            necesario[procesoId][j] -= request[j];
        }

        // 4. Comprobar el estado de seguridad con la asignación hipotética
        if (esSeguro()) {
            System.out.println("  Solicitud de P" + procesoId + " CONCEDIDA. El sistema permanece en estado seguro.\n");
            return true;
        } else {
            System.out.println("  Solicitud de P" + procesoId + " DENEGADA. La concesión resultaría en un estado inseguro.\n");
            // Revertir los cambios
            asignacion = originalAsignacion;
            disponibles = originalDisponibles;
            necesario = originalNecesario;
            calcularNecesario();
            return false;
        }
    }

    // Método principal para probar la simulación
    public static void main(String[] args) {
        /// Ejemplo de inicialización
        int numProcesos = 5;
        int numRecursos = 3;

        int[] disponibles = {3, 3, 2}; // R0, R1, R2

        int[][] maxima = {
                {7, 5, 3}, // P0
                {3, 2, 2}, // P1
                {9, 0, 2}, // P2
                {2, 2, 2}, // P3
                {4, 3, 3}  // P4
        };

        int[][] asignacion = {
                {0, 1, 0}, // P0
                {2, 0, 0}, // P1
                {3, 0, 2}, // P2
                {2, 1, 1}, // P3
                {0, 0, 2}  // P4
        };
        // Crear una instancia del Algoritmo Banquero
        Banquero banquero = new Banquero(numProcesos, numRecursos, disponibles, maxima, asignacion);
        // Imprimir el estado inicial del sistema
        banquero.printState();

        // 1. Probar el estado inicial

        System.out.println("\n--- Verificando estado inicial ---");
        banquero.esSeguro(); // Verificar si el sistema está en un estado seguro

        // 2. Probar solicitudes de recursos

        System.out.println("\n--- Escenario 1: Solicitud Segura (P1 solicita (1,0,2)) ---");
        //System.out.println("\n--- Escenario 2: Solicitud Insegura (P4 solicita (3,3,0)) ---");
        
        
        int[] solicitud1 = {1, 0, 2}; // P0 solicita 1 R0, 0 R1, 2 R2
        //int[] solicitud1 = {3, 3, 0}; // P4 solicita 3 R0, 3 R1, 0 R2


        boolean resultado1 = banquero.solicitarRecursos(1, solicitud1);
        /*
        boolean resultado1 = banquero.solicitarRecursos(4, solicitud1);
        if (resultado1) {
           banquero.printState(); // Imprimir el estado después de la solicitud
        }
        */
    }
}
