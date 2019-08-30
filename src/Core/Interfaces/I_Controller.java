package Core.Interfaces;

import Core.Onjects.Cluster;
import Core.Onjects.Document;
import Core.Onjects.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public interface I_Controller {

     Vector<Document> fetchResultsBoolean(String query, int top);
     Vector<Document> fetchResultsCosine(String query, int top);
     Vector<Document> fetchResultsCluster(String query, int top);
     HashMap<String, Double> wordFrequencies(int clusterNum, int subNum);
     String getParserSummery();
     String getClustersSummery();
     String getDistancesBtwnClusters();
     List<Cluster> getMainClusters();
     List<Pair<String, Double>> getTopWordsForCluster(int clusterNum, int limit);
     int getSubClustersCount(int i);
     void printMutualWords(int i);
}
