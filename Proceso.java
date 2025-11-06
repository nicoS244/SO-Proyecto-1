public class Proceso {

    private final String id;
    private final String nombre;
    private final int sizeKB;
    private final int cpuTotalMs;
    private final int llegadaMs;

    private int cpuRestanteMs;
    private Integer tPrimeraRespuestaMs;
    private Integer tFinalizacionMs;

    public Proceso(String id, String nombre, int sizeKB, int cpuTotalMs, int llegadaMs) {
        validar(id, nombre, sizeKB, cpuTotalMs, llegadaMs);
        this.id = id.trim();
        this.nombre = nombre.trim();
        this.sizeKB = sizeKB;
        this.cpuTotalMs = cpuTotalMs;
        this.llegadaMs = llegadaMs;
        this.cpuRestanteMs = cpuTotalMs;
        this.tPrimeraRespuestaMs = null;
        this.tFinalizacionMs = null;
    }

    private static void validar(String id, String nombre, int sizeKB, int cpuTotalMs, int llegadaMs) {
        if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("ID vacío");
        if (nombre == null || nombre.trim().isEmpty()) throw new IllegalArgumentException("Nombre vacío");
        if (sizeKB <= 0) throw new IllegalArgumentException("sizeKB debe ser > 0");
        if (cpuTotalMs <= 0) throw new IllegalArgumentException("cpuTotalMs debe ser > 0");
        if (llegadaMs < 0) throw new IllegalArgumentException("llegadaMs debe ser >= 0");
    }

    public int ejecutarPor(int quantumMs, int relojMsActual) {
        if (quantumMs <= 0) throw new IllegalArgumentException("quantumMs debe ser > 0");
        if (relojMsActual < 0) throw new IllegalArgumentException("relojMsActual debe ser >= 0");

        if (tPrimeraRespuestaMs == null) {
            tPrimeraRespuestaMs = relojMsActual;
        }
        int ejecutado = Math.min(quantumMs, cpuRestanteMs);
        cpuRestanteMs -= ejecutado;
        if (cpuRestanteMs == 0) {
            tFinalizacionMs = relojMsActual + ejecutado;
        }
        return ejecutado;
    }

    public boolean terminado() {
        return cpuRestanteMs == 0;
    }

    public int tiempoRespuesta() {
        if (tPrimeraRespuestaMs == null) return -1;
        return tPrimeraRespuestaMs - llegadaMs;
    }

    public int tiempoEjecucion() {
        if (tFinalizacionMs == null) return -1;
        return tFinalizacionMs - llegadaMs;
    }

    public int tiempoEspera() {
        int te = tiempoEjecucion();
        if (te < 0) return -1;
        return te - cpuTotalMs;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public int getSizeKB() { return sizeKB; }
    public int getCpuTotalMs() { return cpuTotalMs; }
    public int getCpuRestanteMs() { return cpuRestanteMs; }
    public int getLlegadaMs() { return llegadaMs; }
    public Integer getTPrimeraRespuestaMs() { return tPrimeraRespuestaMs; }
    public Integer getTFinalizacionMs() { return tFinalizacionMs; }

}