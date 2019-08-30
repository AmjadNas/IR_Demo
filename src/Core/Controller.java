package Core;

import Core.Interfaces.I_Controller;
import Core.Interfaces.I_Model;
import Core.Onjects.Cluster;
import Core.Onjects.Document;
import Core.Onjects.Pair;

import java.io.IOException;
import java.util.*;

public class Controller implements I_Controller {
    private static Controller ourInstance;
    private I_Model model;

    public static I_Controller getInstance() throws IOException {
        synchronized (Controller.class){
            if (ourInstance == null)
                ourInstance = new Controller();
        }

        return ourInstance;
    }

    private Controller() throws IOException {
        model = Model.getInstance();
    }

    @Override
    public Vector<Document> fetchResultsBoolean(String query, int top) {
        return model.getBooleanResults(query, top);
    }

    @Override
    public Vector<Document> fetchResultsCosine(String query, int top) {
        return model.getCosineResults(query, top);
    }

    @Override
    public Vector<Document> fetchResultsCluster(String query, int top) {
        return model.getClusterResults(query, top);
    }

    @Override
    public HashMap<String, Double> wordFrequencies(int clusterNum, int subNum) {
        return model.wordFrequencies(clusterNum, subNum);
    }

    @Override
    public String getParserSummery() {
        return model.getParserSummery();
    }

    @Override
    public String getClustersSummery() {
        return model.getClustersSummery();
    }

    @Override
    public String getDistancesBtwnClusters() {
        return model.getDistancesBtwnClusters();
    }

    @Override
    public List<Cluster> getMainClusters() {
        return model.getMainClusters();
    }

    @Override
    public List<Pair<String, Double>> getTopWordsForCluster(int clusterNum, int limit) {
        return model.getTopWordsForCluster(clusterNum, limit);
    }

    @Override
    public int getSubClustersCount(int i) {
        return model.getSubClustersCount(i);
    }

    @Override
    public void printMutualWords(int i) {
        model.mergeSubClusters(i);
    }

}
