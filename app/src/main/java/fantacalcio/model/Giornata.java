package fantacalcio.model;

import java.util.Objects;

public class Giornata {
    private int numero; // PK

    public Giornata(int numero) {
        this.numero = numero;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Giornata)) return false;
        Giornata g = (Giornata) o;
        return numero == g.numero;
    }

    @Override
    public int hashCode() {
        return Objects.hash(numero);
    }

    @Override
    public String toString() {
        return "Giornata{numero=" + numero + '}';
    }
}
