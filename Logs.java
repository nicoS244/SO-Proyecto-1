public final class Logs {
    private Logs() {}

    public static boolean ENABLE_COLOR = true;
    public static boolean SPACER_AFTER_EACH_LINE = true;

    private static final String R  = "\u001B[0m";
    private static final String B  = "\u001B[1m";
    private static final String DIM= "\u001B[2m";

    private static final String K  = "\u001B[30m";
    private static final String R1 = "\u001B[31m";
    private static final String G1 = "\u001B[32m";
    private static final String Y1 = "\u001B[33m";
    private static final String B1 = "\u001B[34m";
    private static final String M1 = "\u001B[35m";
    private static final String C1 = "\u001B[36m";
    private static final String W1 = "\u001B[37m";

    private static String c(String color, String s) {
        if (!ENABLE_COLOR) return s;
        return color + s + R;
    }

    private static void out(String line) {
        System.out.println(line);
        if (SPACER_AFTER_EACH_LINE) System.out.println();
    }

    public static void heading(String text) {
        out(c(B + C1, text));
    }

    public static void cola(String titulo, ColaEnlazada<Proceso> cola) {
        StringBuilder sb = new StringBuilder();
        sb.append(c(B1, titulo)).append(" = ").append(c(B1, "["));
        boolean first = true;
        for (Proceso p : cola) {
            if (!first) sb.append(c(B1, ", "));
            sb.append(c(B, p.getId()));
            first = false;
        }
        sb.append(c(B1, "]"));
        out(sb.toString());
    }

    public static void cpuTick(int t, Proceso p, int restanteAntes, int ejecutadoHastaAhora) {
        int restanteAhora = restanteAntes - ejecutadoHastaAhora;
        out(c(W1, String.format("t=%d: CPU %-6s -> restante=%dms", t, p.getId(), restanteAhora)));
    }

    public static void quantumExpira(int t, Proceso p) {
        out(c(R1 + B, String.format("t=%d: Expira quantum %s (restante=%dms) -> regresa a 'Procesos Listos'",
                t, p.getId(), p.getCpuRestanteMs())));
    }

    public static void termina(int t, Proceso p) {
        out(c(G1 + B, String.format("t=%d: TERMINA %s", t, p.getId())));
    }

    public static void memCarga(int t, Proceso p, int memLibreDespuesKB) {
        out(c(Y1 + B, String.format("t=%d: CARGA %s (size=%dKB) -> memLibre=%dKB",
                t, p.getId(), p.getSizeKB(), memLibreDespuesKB)));
    }

    public static void memLibera(int t, Proceso p, int memLibreDespuesKB) {
        out(c(M1 + B, String.format("t=%d: LIBERA %s (size=%dKB) -> memLibre=%dKB",
                t, p.getId(), p.getSizeKB(), memLibreDespuesKB)));
    }
}
