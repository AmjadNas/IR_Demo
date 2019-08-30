package Core.Interfaces;

import Core.Onjects.Cluster;
import Core.Onjects.Document;
import Core.Onjects.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public interface I_Model {

    Vector<Document> getBooleanResults(String query, int top);

    Vector<Document> getCosineResults(String query, int top);

    Vector<Document> getClusterResults(String query, int top);

    void createTopWords(int limit);

    List<Pair<String, Double>> getTopWordsForCluster(int clusterNum, int limit);

    HashMap<String, Double> wordFrequencies(int clusterNum, int subNum);

    String getParserSummery();

    String getClustersSummery();

    String getDistancesBtwnClusters();

    void mergeSubClusters(int c);

    List<Cluster> getMainClusters();

    int getSubClustersCount(int i);
}
