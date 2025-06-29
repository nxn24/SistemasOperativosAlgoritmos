package com.informeSO.disco;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

// Clase que representa un archivo en la simulacion del disco
class EntradaArchivo {
    private String nombre;
    private int tamanoEnBloques;
    private List<Integer> bloquesAsignados; // Bloques que ocupa en el disco
    private boolean estaBorrado; // true si está lógicamente borrado
    private String firma; // Firma simple (ej. "JPG", "TXT")
    private String contenidoOriginal; // Contenido original del archivo

    public EntradaArchivo(String nombre, int tamanoEnBloques, List<Integer> bloquesAsignados, String firma, String contenido) {
        this.nombre = nombre;
        this.tamanoEnBloques = tamanoEnBloques;
        this.bloquesAsignados = new ArrayList<>(bloquesAsignados); // Copia para evitar referencias
        this.estaBorrado = false;
        this.firma = firma;
        this.contenidoOriginal = contenido;
    }

    // Getters y Setters
    public String obtenerNombre() { return nombre; }
    public int obtenerTamanoEnBloques() { return tamanoEnBloques; }
    public List<Integer> obtenerBloquesAsignados() { return bloquesAsignados; }
    public boolean estaBorrado() { return estaBorrado; }
    public void establecerBorrado(boolean borrado) { estaBorrado = borrado; }
    public String obtenerFirma() { return firma; }
    public String obtenerContenidoOriginal() { return contenidoOriginal; }

    @Override
    public String toString() {
        return "Archivo[Nombre='" + nombre + "', Tamaño=" + tamanoEnBloques + " bloques, Bloques=" + bloquesAsignados +
               ", Borrado=" + estaBorrado + ", Firma='" + firma + "']";
    }
}

// Clase que simula el disco y las operaciones de archivos
public class SimuladorDisco {
    private int totalBloques;
    private String[] bloquesDisco; // Representa el contenido de cada bloque (ej. "LIBRE", "ARCHIVO_A_BLK1", "SOBRESCRITO")
    private List<EntradaArchivo> entradasSistemaArchivos; // Simula la tabla de asignación de archivos/directorio

    public SimuladorDisco(int totalBloques) {
        this.totalBloques = totalBloques;
        this.bloquesDisco = new String[totalBloques];
        Arrays.fill(bloquesDisco, "LIBRE"); // Todos los bloques inicializados como libres
        this.entradasSistemaArchivos = new ArrayList<>();
        System.out.println("Simulador de disco inicializado con " + totalBloques + " bloques.");
    }

    /**
     * Crea un archivo asignando bloques y actualizando las entradas del sistema de archivos.
     * @param nombre Nombre del archivo.
     * @param tamanoEnBloques Tamaño en bloques.
     * @param firma Firma del archivo.
     * @param contenido Contenido simulado del archivo (para verificar recuperación).
     * @return true si se creó, false si no hay espacio.
     */
    public boolean crearArchivo(String nombre, int tamanoEnBloques, String firma, String contenido) {
        if (tamanoEnBloques <= 0) {
            System.out.println("Error: El tamaño del archivo debe ser mayor a 0.");
            return false;
        }
        // Verificar si el archivo ya existe (sin importar si está borrado lógicamente)
        if (entradasSistemaArchivos.stream().anyMatch(f -> f.obtenerNombre().equals(nombre))) {
            System.out.println("Error: Ya existe un archivo con el nombre '" + nombre + "'.");
            return false;
        }

        List<Integer> bloquesLibres = new ArrayList<>();
        for (int i = 0; i < totalBloques; i++) {
            if (bloquesDisco[i].equals("LIBRE")) {
                bloquesLibres.add(i);
                if (bloquesLibres.size() == tamanoEnBloques) {
                    break; // Encontró suficientes bloques contiguos (simplificación)
                }
            } else {
                bloquesLibres.clear(); // Reiniciar si no son contiguos para este ejemplo
            }
        }

        if (bloquesLibres.size() < tamanoEnBloques) {
            System.out.println("No hay suficiente espacio libre en el disco para crear '" + nombre + "'.");
            return false;
        }

        // Asignar bloques y escribir contenido simulado
        for (int i = 0; i < tamanoEnBloques; i++) {
            int idBloque = bloquesLibres.get(i);
            // Dividir el contenido simulado para cada bloque
            int inicioContenido = i * (contenido.length() / tamanoEnBloques);
            int finContenido = (i + 1) * (contenido.length() / tamanoEnBloques);
            String fragmentoContenido = contenido.substring(Math.min(inicioContenido, contenido.length()), Math.min(finContenido, contenido.length()));
            
            bloquesDisco[idBloque] = "ARCHIVO_" + nombre + "_BLK" + (i + 1) + " (" + fragmentoContenido + ")";
        }

        EntradaArchivo nuevoArchivo = new EntradaArchivo(nombre, tamanoEnBloques, bloquesLibres, firma, contenido);
        entradasSistemaArchivos.add(nuevoArchivo);
        System.out.println("Archivo '" + nombre + "' creado exitosamente. Ocupa bloques: " + bloquesLibres);
        return true;
    }

    /**
     * Simula la eliminación lógica de un archivo.
     * Marca la EntradaArchivo como borrada y sus bloques como libres, pero no sobrescribe el contenido.
     * @param nombre Nombre del archivo a eliminar.
     * @return true si se eliminó lógicamente, false si no se encontró o ya estaba borrado.
     */
    public boolean eliminarArchivo(String nombre) {
        EntradaArchivo archivoAEliminar = entradasSistemaArchivos.stream()
                .filter(f -> f.obtenerNombre().equals(nombre) && !f.estaBorrado())
                .findFirst()
                .orElse(null);

        if (archivoAEliminar != null) {
            archivoAEliminar.establecerBorrado(true);
            for (int idBloque : archivoAEliminar.obtenerBloquesAsignados()) {
                // Marcar el bloque como lógicamente libre, pero el contenido aún puede estar allí.
                // En una implementación real, aquí solo se actualiza la FAT/Inode, no el contenido del bloque.
                bloquesDisco[idBloque] = "LIBRE_LOGICO"; // Indica que el espacio está disponible para nueva escritura
            }
            System.out.println("Archivo '" + nombre + "' marcado como borrado lógicamente. Sus bloques están ahora marcados como LIBRE_LOGICO.");
            return true;
        } else {
            System.out.println("Archivo '" + nombre + "' no encontrado o ya está borrado lógicamente.");
            return false;
        }
    }

    /**
     * Simula la recuperación de un archivo lógicamente borrado.
     * Intenta encontrar una EntradaArchivo borrada y verifica si sus bloques no han sido sobrescritos.
     * @param nombre Nombre del archivo a recuperar.
     * @return true si se recuperó, false si no se pudo o no se encontró.
     */
    public boolean recuperarArchivo(String nombre) {
        EntradaArchivo archivoARecuperar = entradasSistemaArchivos.stream()
                .filter(f -> f.obtenerNombre().equals(nombre) && f.estaBorrado())
                .findFirst()
                .orElse(null);

        if (archivoARecuperar != null) {
            boolean estaSobrescrito = false;
            for (int idBloque : archivoARecuperar.obtenerBloquesAsignados()) {
                // Simulación de comprobación de sobrescritura: si el bloque ya no tiene el patrón original
                // o si ha sido marcado como "SOBRESCRITO", se considera irrecuperable.
                // En un escenario real, esto implicaría escanear el contenido del bloque para ver si coincide con la firma o partes del archivo.
                if (!bloquesDisco[idBloque].startsWith("LIBRE_LOGICO") && !bloquesDisco[idBloque].contains(archivoARecuperar.obtenerContenidoOriginal().substring(0, Math.min(archivoARecuperar.obtenerContenidoOriginal().length(), 5)))) {
                     // Heurística simple: si ya no es "LIBRE_LOGICO" y no contiene las primeras 5 letras del contenido original
                    estaSobrescrito = true;
                    break;
                }
            }

            if (!estaSobrescrito) {
                archivoARecuperar.establecerBorrado(false); // Marcar como no borrado
                for (int idBloque : archivoARecuperar.obtenerBloquesAsignados()) {
                    // Restaurar la apariencia del bloque como si estuviera en uso por el archivo
                    bloquesDisco[idBloque] = "ARCHIVO_" + nombre + "_BLK" + (archivoARecuperar.obtenerBloquesAsignados().indexOf(idBloque) + 1) + " (RECUPERADO)";
                }
                System.out.println("Archivo '" + nombre + "' recuperado exitosamente.");
                return true;
            } else {
                System.out.println("Error: El archivo '" + nombre + "' no se puede recuperar, sus bloques han sido sobrescritos.");
                return false;
            }
        } else {
            System.out.println("Archivo '" + nombre + "' no encontrado o no está en estado de borrado lógico.");
            return false;
        }
    }
    
    /**
     * Simula la sobrescritura de un bloque de disco.
     * Esto ocurre cuando un nuevo archivo se escribe en espacio "libre" (incluido el lógico).
     * @param idBloque ID del bloque a sobrescribir.
     * @param nuevoContenido Contenido nuevo a escribir.
     */
    public void sobrescribirBloque(int idBloque, String nuevoContenido) {
        if (idBloque >= 0 && idBloque < totalBloques) {
            bloquesDisco[idBloque] = "SOBRESCRITO_CON_" + nuevoContenido;
            System.out.println("Bloque " + idBloque + " sobrescrito con nuevo contenido: " + nuevoContenido);
        } else {
            System.out.println("Error: ID de bloque inválido.");
        }
    }

    /**
     * Muestra una representación del disco (estado de los bloques) y las entradas del sistema de archivos.
     */
    public void mostrarEstadoDisco() {
        System.out.println("\n--- Estado del Disco ---");
        System.out.println("Bloques del Disco: " + Arrays.toString(bloquesDisco));
        
        System.out.println("\n--- Entradas del Sistema de Archivos ---");
        if (entradasSistemaArchivos.isEmpty()) {
            System.out.println("No hay archivos registrados.");
        } else {
            entradasSistemaArchivos.forEach(System.out::println);
        }
        System.out.println("----------------------------------------\n");
    }

    /**
     * Simula el escaneo de firmas de archivos (file carving) en el disco.
     * En esta simulación, solo busca bloques que contengan la firma en su contenido simulado.
     * @param firma La firma a buscar (ej. "JPG", "PDF").
     */
    public void escanearPorFirmas(String firma) {
        System.out.println("\n--- Escaneo de firmas para '" + firma + "' ---");
        List<Integer> bloquesEncontrados = new ArrayList<>();
        for (int i = 0; i < totalBloques; i++) {
            if (bloquesDisco[i].contains(firma)) { // Búsqueda simple de la firma en el contenido del bloque
                bloquesEncontrados.add(i);
            }
        }
        if (bloquesEncontrados.isEmpty()) {
            System.out.println("No se encontraron bloques con la firma '" + firma + "'.");
        } else {
            System.out.println("Firma '" + firma + "' encontrada en bloques: " + bloquesEncontrados);
            // En un software real, aquí se intentarían reconstruir los archivos a partir de estos bloques.
        }
        System.out.println("----------------------------------------\n");
    }

    public static void main(String[] args) {
    SimuladorDisco simulador = new SimuladorDisco(5); // Disco pequeño de 5 bloques

    // Crear un archivo
    System.out.println("\n--- Creando archivo 'foto.jpg' ---");
    simulador.crearArchivo("foto.jpg", 2, "JPG", "IMAGEN_JPEG");
    simulador.mostrarEstadoDisco();

    // Eliminar lógicamente el archivo
    System.out.println("--- Eliminando lógicamente 'foto.jpg' ---");
    simulador.eliminarArchivo("foto.jpg");
    simulador.mostrarEstadoDisco();

    // Intentar recuperar el archivo (debería funcionar)
    System.out.println("--- Intentando recuperar 'foto.jpg' (debería funcionar) ---");
    simulador.recuperarArchivo("foto.jpg");
    simulador.mostrarEstadoDisco();

    // Eliminar y sobrescribir un bloque
    System.out.println("--- Eliminando y sobrescribiendo un bloque de 'foto.jpg' ---");
    simulador.eliminarArchivo("foto.jpg");
    simulador.sobrescribirBloque(0, "DATOS_NUEVOS");
    simulador.mostrarEstadoDisco();

    // Intentar recuperar el archivo (debería fallar)
    System.out.println("--- Intentando recuperar 'foto.jpg' (debería fallar) ---");
    simulador.recuperarArchivo("foto.jpg");
    simulador.mostrarEstadoDisco();
    }
}