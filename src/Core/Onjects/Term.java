package Core.Onjects;

import java.io.Serializable;
import java.util.Objects;

public class Term implements Comparable<Term>, Serializable {

    private final String term;
    private double idf;

    public Term(String s){
        term = s;
    }

    public double getDf() {
        return idf;
    }

    public String getTerm() {
        return term;
    }

    public void setIdf(double idf) {
        this.idf = idf;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Term term1 = (Term) o;
        return term.equals(term1.term);
    }

    @Override
    public int hashCode() {
        return Objects.hash(term);
    }

    @Override
    public int compareTo(Term o) {
        if (o != null)
            return term.compareTo(o.term);

        return 1;
    }

    @Override
    public String toString() {
        return term + " IDF: " + idf;
    }
}
