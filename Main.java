// Main.java
// Simulador Round Robin monohilo con memoria simple y cola propia.
// Reglas del documento:
// - Llegadas diferidas (por tiempo de llegada)
// - Dos colas: "Procesos Listos" (fuera de memoria) y "Listos para Ejecución" (en memoria)
// - FIFO estricta para cargar a memoria (si el primero no cabe, no se salta)
// - Al expirar quantum y NO terminar: sale de memoria, regresa a listos y libera memoria
// - Imprimir cada operación en colas y cada cambio de memoria
// - Calcular promedios: respuesta, ejecución (turnaround), espera

import java.util.*;

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // Config de formato: colores y salto extra entre mensajes
        Logs.ENABLE_COLOR = true;              // ponlo en false si tu terminal no soporta ANSI
        Logs.SPACER_AFTER_EACH_LINE = true;    // salto de línea extra tras cada print

        System.out.println("====== SIMULADOR DE PROCESOS (RR) ======");
        int n = leerEnteroPositivo(sc, "Cantidad de procesos: ");
        int memoriaTotalKB = leerEnteroPositivo(sc, "Memoria total (KB): ");
        int quantumMs = leerEnteroPositivo(sc, "Quantum (ms): ");

        // Captura con unicidad de ID
        List<Proceso> todos = new ArrayList<>(n);
        Set<String> usados = new HashSet<>();
        for (int i = 1; i <= n; i++) {
            System.out.println("\n=== Proceso " + i + " ===");
            String id;
            while (true) {
                System.out.print("ID: "); id = sc.next().trim();
                if (!usados.contains(id)) break;
                System.out.println("ID repetido, ingresa otro.");
            }
            System.out.print("Nombre: "); String nombre = sc.next().trim();
            int sizeKB = leerEnteroPositivo(sc, "Tamaño (KB): ");
            int cpuTotal = leerEnteroPositivo(sc, "Tiempo de CPU total (ms): ");
            int llegada = leerEnteroNoNegativo(sc, "Tiempo de llegada (ms): ");

            Proceso p = new Proceso(id, nombre, sizeKB, cpuTotal, llegada);
            todos.add(p);
            usados.add(id);
        }

        // Ordenar llegadas (estable)
        todos.sort(Comparator.comparingInt(Proceso::getLlegadaMs));

        // Mostrar resumen
        System.out.println("\n====== PROCESOS INGRESADOS ======");
        for (Proceso p : todos) {
            System.out.printf("ID: %-8s  Nom: %-10s  Size=%4dKB  CPU=%5dms  Llegada=%5dms  ",
                    p.getId(), p.getNombre(), p.getSizeKB(), p.getCpuTotalMs(), p.getLlegadaMs());
        }

        Memoria mem = new Memoria(memoriaTotalKB);
        ColaEnlazada<Proceso> colaListos  = new ColaEnlazada<>(); // "Procesos Listos" (fuera de memoria)
        ColaEnlazada<Proceso> colaMemoria = new ColaEnlazada<>(); // "Listos para Ejecución" (en memoria)

        int reloj = 0;
        int idxLleg = 0;

        Logs.heading("\n====== INICIA SIMULACIÓN ======");

        while (true) {

            idxLleg = procesarLlegadas(todos, idxLleg, reloj, colaListos, colaMemoria);

            cargarMemoriaFIFO(mem, reloj, colaListos, colaMemoria);

            if (!colaMemoria.estaVacia()) {
                Proceso actual = colaMemoria.desencolar();
                Logs.cola(String.format("t=%d: Desencola de 'Listos para Ejecución' %s -> Listos para Ejecución", reloj, actual.getId()), colaMemoria);

                int ticks = Math.min(quantumMs, actual.getCpuRestanteMs());
                int restanteAntes = actual.getCpuRestanteMs();

                for (int i = 1; i <= ticks; i++) {
                    // Ejecuta exactamente 1 ms
                    actual.ejecutarPor(1, reloj);
                    reloj += 1;

                    // Log del tick de CPU (con salto extra)
                    Logs.cpuTick(reloj, actual, restanteAntes, i);

                    // Llegadas en este ms (imprime "Procesos Listos" y "Listos para Ejecución")
                    idxLleg = procesarLlegadas(todos, idxLleg, reloj, colaListos, colaMemoria);

                    // Intentar cargar a memoria en cuanto se pueda (FIFO estricta)
                    cargarMemoriaFIFO(mem, reloj, colaListos, colaMemoria);

                    // Si terminó antes de consumir todo el quantum, cortar
                    if (actual.terminado()) break;
                }

                if (actual.terminado()) {
                    mem.liberar(actual, reloj);
                    Logs.termina(reloj, actual);
                } else {
                    mem.liberar(actual, reloj);
                    Logs.quantumExpira(reloj, actual);
                    colaListos.encolar(actual);
                    Logs.cola("Procesos Listos", colaListos);
                }
                continue;
            }

            // (D) Si no hay nada para ejecutar y tampoco listos, pero faltan llegadas: saltar reloj
            if (colaMemoria.estaVacia() && colaListos.estaVacia() && idxLleg < todos.size()) {
                reloj = Math.max(reloj, todos.get(idxLleg).getLlegadaMs());
                continue;
            }

            // (E) Condición de término
            if (colaMemoria.estaVacia() && colaListos.estaVacia() && idxLleg >= todos.size()) break;
        }

        // Métricas finales
        double promResp   = promedio(todos, Proceso::tiempoRespuesta);
        double promTurn   = promedio(todos, Proceso::tiempoEjecucion);
        double promEspera = promedio(todos, Proceso::tiempoEspera);

        Logs.heading("====== FIN ======");
        System.out.printf("Promedio RESPUESTA : %.2f ms%n%n", promResp);
        System.out.printf("Promedio EJECUCIÓN : %.2f ms%n%n", promTurn);
        System.out.printf("Promedio ESPERA    : %.2f ms%n%n", promEspera);
    }

    // ========= helpers de simulación =========

    // Inserta llegadas cuyo tiempo <= reloj; imprime "Procesos Listos" y "Listos para Ejecución"
    private static int procesarLlegadas(List<Proceso> todos, int idxLleg, int reloj,
                                        ColaEnlazada<Proceso> colaListos,
                                        ColaEnlazada<Proceso> colaMemoria) {
        while (idxLleg < todos.size() && todos.get(idxLleg).getLlegadaMs() <= reloj) {
            Proceso p = todos.get(idxLleg++);
            colaListos.encolar(p);
            Logs.cola(String.format("t=%d: Llega %s -> Procesos Listos", reloj, p.getId()), colaListos);
            Logs.cola("Listos para Ejecución", colaMemoria);
        }
        return idxLleg;
    }

    private static void cargarMemoriaFIFO(Memoria mem, int reloj,
                                          ColaEnlazada<Proceso> colaListos,
                                          ColaEnlazada<Proceso> colaMemoria) {
        boolean cargo = true;
        while (cargo && !colaListos.estaVacia()) {
            Proceso f = colaListos.frente();
            if (mem.cabe(f)) {
                colaListos.desencolar();
                Logs.cola(String.format("t=%d: Sale de 'Procesos Listos' %s -> Procesos Listos", reloj, f.getId()), colaListos);
                mem.cargar(f, reloj);
                colaMemoria.encolar(f);
                Logs.cola("Listos para Ejecución", colaMemoria);
            } else {
                cargo = false;
            }
        }
    }

    // ========= utilidades de lectura =========
    private static int leerEnteroPositivo(Scanner sc, String prompt) {
        int x = -1;
        while (x <= 0) {
            System.out.print(prompt);
            while (!sc.hasNextInt()) { sc.next(); System.out.print(prompt); }
            x = sc.nextInt();
        }
        return x;
    }
    private static int leerEnteroNoNegativo(Scanner sc, String prompt) {
        int x = -1;
        while (x < 0) {
            System.out.print(prompt);
            while (!sc.hasNextInt()) { sc.next(); System.out.print(prompt); }
            x = sc.nextInt();
        }
        return x;
    }

    // ========= utilidades de métricas =========
    private interface IntGetter { int get(Proceso p); }

    private static double promedio(List<Proceso> ps, IntGetter g) {
        long suma = 0;
        int cnt = 0;
        for (Proceso p : ps) {
            int v = g.get(p);
            if (v >= 0) { suma += v; cnt++; }
        }
        return cnt == 0 ? 0.0 : (suma * 1.0) / cnt;
    }
}