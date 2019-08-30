package Core.Onjects;

import Core.Utilities.Utility;

import java.io.Serializable;
import java.util.*;

public class Cluster implements Serializable, Comparable<Cluster>{
    private final int num;
    private final double[] centroid;
    private HashMap<Integer, ArrayList<Document>> followers;
    public static final transient int GROUPS_COUNT = 7;
    public static final int CLUSTER_NUM = 4;

    public Cluster(int num, double[] centroid){
        this.num = num;
        this.centroid = centroid;
        followers = new HashMap<>();
    }

    /**
     * add the document to the related sub cluster
     * @param doc pair of the document and the sub cluster number it's related to
     */
    public void addFollower(Pair<Integer, Document> doc){
        if (doc != null) {
            if (followers.containsKey(doc.first))
                followers.get(doc.first).add(doc.second);
            else {
                ArrayList<Document> docs = new ArrayList<>();
                docs.add(doc.second);
                followers.put(doc.first, docs);
            }
        }
    }

    public int getNum() {
        return num;
    }

    public void setfollowers(HashMap<Integer, ArrayList<Document>> tempMap) {
        followers = tempMap;
    }

    /**
     *
     * @return the average distance between the centroid and all it's related documents
     */
    public double getAverageDistamceFromCentroid(){
        List<Document>  documents = new ArrayList<>();
        followers.values().forEach(documents::addAll);
        DoubleSummaryStatistics doubleSummaryStatistics = documents.stream()
                .mapToDouble(c-> {
                    try {
                        return Utility.calcDistance(centroid, c.getVector());
                    } catch (Exception e) {
                        e.printStackTrace();
                        return -1;
                    }
                })
                .summaryStatistics();
        return doubleSummaryStatistics.getAverage();
    }

    /**
     * @param clusters list of clusters
     * @return a list of distances between all pairs of clusters.
     */
    public static ArrayList<Pair<String,Double>> getDistancesForCentroids(List<Cluster> clusters){
        ArrayList<Pair<String,Double>> pairs = new ArrayList<>();
        String phrase;
        for (int i = 0; i < clusters.size()-1; i++){
            for (int j = i+1; j < clusters.size(); j++){
                phrase = "Distance Between Cluster #" + clusters.get(i).num + " and Cluster #" + clusters.get(j).num;
                pairs.add(new Pair<>(phrase, clusters.get(i).getDistanceFromCentroid(clusters.get(j))));
            }
        }
        return pairs;
    }

    /**
     * a helper method used to calculate the distance from one cluster to another.
     * The method is used in getDistancesForCentroids.
     * @param cluster
     * @return
     */
    public double getDistanceFromCentroid(Cluster cluster){
        try {
            return Utility.calcDistance(centroid, cluster.centroid);
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    public HashMap<Integer, ArrayList<Document>> getFollowers() {
        return followers;
    }

    public int size() {
        return followers.size();
    }

    public int getClusterSize(){
        return followers.values().stream().mapToInt(List::size).sum();
    }

    public double[] getCentroid() {
        return centroid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cluster cluster = (Cluster) o;
        return Arrays.equals(centroid, cluster.centroid);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(centroid);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Cluster #")
                .append(num)
                .append("\nSubClusters:");
        followers.forEach((v, k) -> sb.append("\n")
                .append(v).append("=")
                .append(k));
        sb.append("\n" );
        return sb.toString();
    }

    @Override
    public int compareTo(Cluster o) {
        return num - o.num;
    }
}
