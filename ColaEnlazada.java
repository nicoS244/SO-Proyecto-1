import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class ColaEnlazada<T> implements Iterable<T> {
    private static final class Nodo<E> {
        E dato;
        Nodo<E> sig;
        Nodo(E dato) { this.dato = dato; }
    }

    private Nodo<T> cabeza;
    private Nodo<T> cola;
    private int tam;

    public ColaEnlazada() {
        cabeza = null;
        cola = null;
        tam = 0;
    }

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

    public T desencolar() {
        if (cabeza == null) throw new NoSuchElementException("Cola vacía");
        T val = cabeza.dato;
        cabeza = cabeza.sig;
        if (cabeza == null) cola = null;
        tam--;
        return val;
    }

    public T frente() {
        if (cabeza == null) throw new NoSuchElementException("Cola vacía");
        return cabeza.dato;
    }

    public boolean estaVacia() { return tam == 0; }

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
}