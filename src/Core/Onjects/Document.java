package Core.Onjects;

import Core.Utilities.Utility;

import java.io.Serializable;

public class Document implements Comparable<Document>, Serializable {

    private final int id;
    private final String name;
    private double[] vector;

    public Document(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public double[] getVector() {
        return vector;
    }

    public void setVector(double[] vector) {
        this.vector = Utility.normalizeVector(vector);
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    @Override
    public int compareTo(Document o) {
        return id-o.id;
    }

    @Override
    public String toString() {
        return "\nDocument #" + id +" -- " + name;
    }
}
