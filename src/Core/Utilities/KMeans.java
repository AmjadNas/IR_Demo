package Core.Utilities;

import Core.Onjects.Cluster;
import Core.Onjects.Document;
import Core.Onjects.Pair;

import java.util.ArrayList;
import java.util.List;

public class KMeans {
    // Data members
    private final double[][] data; // Array of all records in dataset
    private final List<Document> docs;
    private int []label;  // generated cluster labels
    private double[][] centroids; // centroids: the center of clusters
    private int nrows, ndims; // the number of rows and dimensions
    private int numClusters; // the number of clusters;
    private final List<Pair<Integer, Document>> pairs;

    // Constructor; loads records from file <fileName>.
    // if labels do not exist, set labelname to null
    public KMeans(List<Document> documents, int numClusters) {
        docs = documents;
        this.numClusters = numClusters;
        centroids = new double[numClusters][];
        nrows = documents.size();
        ndims = documents.get(0).getVector().length;
        data = new double[nrows][];
        pairs = new ArrayList<>(nrows);

        for (int i = 0; i < nrows; i++)
            data[i] = documents.get(i).getVector();

    }

    // Perform k-means clustering with the specified number of clusters and
    // Eucliden distance metric.
    // niter is the maximum number of iterations. If it is set to -1, the kmeans iteration is only terminated by the convergence condition.
    // centroids are the initial centroids. It is optional. If set to null, the initial centroids will be generated randomly.
    public void clustering(int niter) {
        double[][] c1 = centroids;
        double threshold = 0.005f*0.005f;
        int round = 0;
        label = new int[nrows];

        if (numClusters <= nrows){

            ArrayList<Integer> idx = new ArrayList<>();
            for (int i = 0; i < numClusters; i++){
                int c;
                do{
                    c = (int) (Math.random()* nrows);
                }while(idx.contains(c)); // avoid duplicates
                idx.add(c);

                // copy the value from data[c]
                centroids[i] = new double[ndims];
                for (int j = 0; j < ndims; j++)
                    centroids[i][j] = data[c][j];
            }

            for (; round < niter; round++){
                // update _centroids with the last round results
                centroids = c1;

                //assign record to the closest centroid
                for (int i = 0; i < nrows; i++){
                    label[i] = closest(data[i]);
                }

                // recompute centroids based on the assignments
                c1 = updateCentroids();
                round++;
                if (((niter - round) == 0) || converge(centroids, c1, threshold))
                    break;
            }
        }else {
            centroids[0] = new double[ndims];
            int r = (int) (Math.random() * nrows);
            for (int j = 0; j < ndims; j++)
                centroids[0][j] = data[r][j];
        }

        System.out.printf("Converges at %d\n", round);
        for (int i = 0; i < nrows; i++) {
            Pair<Integer, Document> p = new Pair<>(label[i], docs.get(i));
            pairs.add(p);
        }

    }

    // find the closest centroid for the record v
    private int closest(double [] v){
        try {
            double mindist = Utility.calcDistance(v, centroids[0]);
            int label = 0;
            for (int i = 1; i < numClusters; i++){
                double t = Utility.calcDistance(v, centroids[i]);
                if (mindist > t){
                    mindist = t;
                    label = i;
                }
            }
            return label;
        }catch (Exception e){
            e.printStackTrace();
            return -1;
        }

    }

    // according to the cluster labels, recompute the centroids
    // the centroid is updated by averaging its members in the cluster.
    // this only applies to Euclidean distance as the similarity measure.

    private double[][] updateCentroids(){
        // initialize centroids and set to 0
        double [][] newc = new double [numClusters][]; //new centroids
        int [] counts = new int[numClusters]; // sizes of the clusters

        // intialize
        for (int i = 0; i< numClusters; i++){
            newc[i] = new double [ndims];
        }


        for (int i = 0; i < nrows; i++){
            int cn = label[i]; // the cluster membership id for record i
            for (int j = 0; j < ndims; j++){
                newc[cn][j] += data[i][j]; // update that centroid by adding the member data record
            }
            counts[cn]++;
        }

        // finally get the average
        for (int i = 0; i< numClusters; i++){
            for (int j = 0; j< ndims; j++){
                newc[i][j]/= counts[i];
            }
        }

        return newc;
    }

    // check convergence condition
    // max{dist(c1[i], c2[i]), i=1..numClusters < threshold
    private boolean converge(double [][] c1, double [][] c2, double threshold){
        // c1 and c2 are two sets of centroids
        double maxv = 0;
        for (int i = 0; i < numClusters; i++){
            double d = 0;
            try {
                d = Utility.calcDistance(c1[i], c2[i]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (maxv < d)
                maxv = d;
        }
        return maxv < threshold;

    }

    public List<Pair<Integer, Document>> getPairs() {
        return pairs;
    }

    public double[][] getCentroids() {
        return centroids;
    }

    public int[] getLabel() {
        return label;
    }

    public int nrows(){
        return nrows;
    }

    /**
     *
     * @return the clustering results
     */
    public List<Cluster> getClusters(){
        List<Cluster> clusters = new ArrayList<>();
        int i = 0;
        for (double[] d : centroids){
            int j = i ;
            Cluster cTemp = new Cluster(i, d);

            /*pairs.stream()
                    .filter(val -> val.first == j)
                    .forEach(val -> cTemp.addFollower(val.second));*/

            clusters.add(cTemp);
            i++;
        }

        return clusters;
    }



}
