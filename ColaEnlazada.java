// ColaEnlazada.java
// Cola propia SIN usar colecciones de Java para la estructura interna.
// Operaciones: encolar, desencolar, frente, estaVacia, tamano, limpiar, imprimir.

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class ColaEnlazada<T> implements Iterable<T> {
    // Nodo interno (lista simplemente enlazada)
    private static final class Nodo<E> {
        E dato;
        Nodo<E> sig;
        Nodo(E dato) { this.dato = dato; }
    }

    private Nodo<T> cabeza; // frente (desencolar)
    private Nodo<T> cola;   // final (encolar)
    private int tam;

    public ColaEnlazada() {
        cabeza = null;
        cola = null;
        tam = 0;
    }

    // Encolar al final O(1)
    public void encolar(T x) {
        Nodo<T> n = new Nodo<>(x);
        if (cola == null) {
            cabeza = cola = n;
        } else {
            cola.sig = n;
            cola = n;
        }
        tam++;
    }

    // Desencolar del frente O(1)
    public T desencolar() {
        if (cabeza == null) throw new NoSuchElementException("Cola vacía");
        T val = cabeza.dato;
        cabeza = cabeza.sig;
        if (cabeza == null) cola = null; // quedó vacía
        tam--;
        return val;
    }

    // Ver el frente sin quitar
    public T frente() {
        if (cabeza == null) throw new NoSuchElementException("Cola vacía");
        return cabeza.dato;
    }

    // ¿Está vacía?
    public boolean estaVacia() { return tam == 0; }

    // Tamaño actual
    public int tamano() { return tam; }

    // Vaciar completamente
    public void limpiar() {
        Nodo<T> cur = cabeza;
        while (cur != null) {
            Nodo<T> nxt = cur.sig;
            cur.dato = null; // ayuda al GC (opcional)
            cur.sig = null;
            cur = nxt;
        }
        cabeza = cola = null;
        tam = 0;
    }

    // Iterador para recorrer sin modificar
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Nodo<T> it = cabeza;
            @Override public boolean hasNext() { return it != null; }
            @Override public T next() {
                if (it == null) throw new NoSuchElementException();
                T v = it.dato;
                it = it.sig;
                return v;
            }
        };
    }

    // Impresión formateada del contenido sin consumir la cola
    public void imprimir(String titulo, Function<T, String> toText) {
        StringBuilder sb = new StringBuilder();
        if (titulo != null && !titulo.isEmpty()) sb.append(titulo).append("=");
        sb.append("[");
        Nodo<T> cur = cabeza;
        while (cur != null) {
            sb.append(toText != null ? toText.apply(cur.dato) : String.valueOf(cur.dato));
            cur = cur.sig;
            if (cur != null) sb.append(", ");
        }
        sb.append("]");
        System.out.println(sb.toString());
    }

    // Atajo: imprime IDs si T tiene getId()
    public void imprimirIdsSiExisten(String titulo) {
        imprimir(titulo, e -> {
            try { return String.valueOf(e.getClass().getMethod("getId").invoke(e)); }
            catch (Exception ex) { return String.valueOf(e); }
        });
    }
}