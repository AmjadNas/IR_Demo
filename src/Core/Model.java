package Core;

import Core.Interfaces.I_Model;
import Core.Onjects.Cluster;
import Core.Onjects.Document;
import Core.Onjects.Pair;
import Core.Onjects.Term;
import Core.Utilities.KMeans;
import Core.Utilities.Parser;
import Core.Utilities.Porter;
import Core.Utilities.Utility;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Model implements I_Model {

    private static Model instance;
    private Parser parser;
    private List<Cluster> mainClusters;

    private Model() throws IOException {
         if ((parser = Parser.deSerializeData()) == null) {
             parser = new Parser();

             new Thread(() -> {
                     System.out.println("Saving data to disk!");
                     serializeData("parser.bin", parser);
             }).start();
         }
         mainClusters = getClusters();

    }

    public static Model getInstance() throws IOException {
        if (instance == null)
            instance = new Model();

        return instance;
    }

    private List<Cluster> getClusters(){
        List<Cluster> clusters;
        if((clusters = deserializeClusters()) == null) {
            TreeMap<Integer, ArrayList<Document>> map = new TreeMap<>();
            List<Document> documents = parser.getDocs().stream()
                    .map(val -> val.first)
                    .collect(Collectors.toList());

            KMeans kMeans = new KMeans(documents, Cluster.CLUSTER_NUM);
            kMeans.clustering(1);
            List<Pair<Integer, Document>> pairs = kMeans.getPairs();
            clusters = kMeans.getClusters();

            for (Pair<Integer, Document> pair : pairs) {
                if (map.containsKey(pair.first))
                    map.get(pair.first).add(pair.second);
                else {
                    ArrayList<Document> docs = new ArrayList<>();
                    docs.add(pair.second);
                    map.put(pair.first, docs);
                }
            }

            int i = 0;
            for (Map.Entry<Integer, ArrayList<Document>> entry : map.entrySet()) {
                KMeans subMeans = new KMeans(entry.getValue(), Cluster.GROUPS_COUNT);
                subMeans.clustering(2);
                for (Pair<Integer, Document> pair : subMeans.getPairs())
                    clusters.get(i).addFollower(pair);
                i++;
            }
            serializeData("clusters.bin", clusters);
        }
        return clusters;
    }

    private List<Cluster> deserializeClusters() {
        try {
            List<Cluster> clusters;
            File dataFolder = new File("data");

            if (!dataFolder.exists())
                return null;

            File file = new File(dataFolder, "clusters.bin");
            FileInputStream fileInputStream = new FileInputStream(file);
            ObjectInputStream in = new ObjectInputStream(fileInputStream);

            clusters = (List<Cluster>) in.readObject();

            in.close();
            fileInputStream.close();

            return clusters;
        }catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Vector<Document> getBooleanResults(String query, int top) {
         Set<Term> terms = processQuery(query);
         Set<Document> documents = new TreeSet<>();

         terms.stream()
                 .map(parser.getInvertedIndex()::get)
                 .sorted(Comparator.comparingInt(TreeSet::size))
                 .collect(Collectors.toList())
                 .forEach(documents::addAll);

         return documents.stream()
                 .limit(top)
                 .collect(Collectors.toCollection(Vector::new));
    }

    @Override
    public Vector<Document> getCosineResults(String query, int top) {
        Set<Term> terms = processQuery(query);
        Set<Document> documents  = new TreeSet<>();
        TreeMap<Term, TreeSet<Document>>   map = parser.getInvertedIndex();
        double[] vec = new double[map.keySet().size()];

        int i = 0;
        for (Map.Entry<Term, TreeSet<Document>>  keys : map.entrySet()) {
            if (terms.contains(keys.getKey())) {
                double d = keys.getKey().getDf();
                vec[i] = keys.getKey().getDf();
                documents.addAll(keys.getValue());
            } else
                vec[i] = 0;
            i++;
        }

        vec = Utility.normalizeVector(vec);

        return documents.stream()
                .sorted(new CosineComparator(vec))
                .limit(top)
                .collect(Collectors.toCollection(Vector::new));
    }

    @Override
    public Vector<Document> getClusterResults(String query, int top) {
        Set<Term> terms = processQuery(query);
        TreeMap<Term, TreeSet<Document>>  map = parser.getInvertedIndex();
        double[] vec = new double[map.keySet().size()];

        int i = 0;
        for (Map.Entry<Term, TreeSet<Document>>  keys : map.entrySet()) {
            if (terms.contains(keys.getKey())) {
                vec[i] = keys.getKey().getDf();
            } else
                vec[i] = 0;
            i++;
        }

        vec = Utility.normalizeVector(vec);
        Cluster max = mainClusters.stream()
                .max(new ClusterDistanceComparator(vec))
                .orElse(mainClusters.get(mainClusters.size()-1));

        List<Document> documents = new ArrayList<>();
        max.getFollowers().values().forEach(documents::addAll);

        return documents.stream()
                .sorted(new CosineComparator(vec))
                .limit(top)
                .collect(Collectors.toCollection(Vector::new));
    }

    @Override
    public String getParserSummery() {
        return parser.toString();
    }

    @Override
    public String getClustersSummery() {
        StringBuilder stringBuilder = new StringBuilder();
        mainClusters.forEach(c->{
            stringBuilder.append(String.format("Cluster #%d:" +
                    "\n Size: %d" +
                    "\n Average distance from centroid: %.2f" +
                    "\n number of sub-clusters: %d \n", c.getNum(), c.getClusterSize(), c.getAverageDistamceFromCentroid(), c.size()));
        });
        return stringBuilder.toString();
    }

    @Override
    public String getDistancesBtwnClusters() {
        StringBuilder sb = new StringBuilder();
        Cluster.getDistancesForCentroids(mainClusters)
                .forEach(sb::append);
        return sb.toString();
    }

    @Override
    public void createTopWords(int limit){
        FileWriter fileWriter = null;
        try {
            fileWriter =  new FileWriter(new File("top words.txt"));
            BufferedWriter bw = new BufferedWriter(fileWriter);
            parser.top30TermsByIDF(limit).forEach(x -> {
                try {
                    bw.write(x+"\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            bw.flush();
            fileWriter.close();
            bw.close();
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    /**
     *
     * @param clusterNum
     * @param limit
     * @return top N words in a cluster ordered by TF-IDF values
     */
    @Override
    public List<Pair<String, Double>> getTopWordsForCluster(int clusterNum, int limit){
        int wordCount = parser.getInvertedIndex().size();

        Cluster c = mainClusters.get(clusterNum);
        List<Pair<String, Double>> list = new ArrayList<>();
        double[] k = new double[wordCount];
        List<Document> documents = new ArrayList<>();
        c.getFollowers().values().forEach(documents::addAll);

        for (Document d : documents) {
            for (int i = 0; i < wordCount; i++) {
                k[i] += d.getVector()[i];
            }
        }

        int i = 0;
        for (Map.Entry<Term, TreeSet<Document>> set : parser.getInvertedIndex().entrySet()){
            if (k[i] > 0){
                list.add(new Pair<>(set.getKey().getTerm(), k[i]));
            }
            i++;
        }

        return list.stream()
                .sorted((c1, c2) -> Double.compare(c2.second, c1.second))
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public HashMap<String, Double> wordFrequencies(int clusterNum, int subNum){
        List<Document> documents = new ArrayList<>(mainClusters.get(clusterNum).getFollowers().get(subNum));
        HashMap<String, Double> pairs = new HashMap<>();
        // sum the tf-idf values for every word in the documents set
        for (Document d : documents){
            int  i = 0;
            for (Term t : parser.getInvertedIndex().keySet()){
                double tf_idf = d.getVector()[i];
                if (tf_idf > 0) {
                    if (pairs.containsKey(t.getTerm())) {
                        double newVal = pairs.get(t.getTerm()) + tf_idf;
                        pairs.replace(t.getTerm(), newVal);
                    } else {
                        pairs.put(t.getTerm(), tf_idf);
                    }
                }
                i++;
            }
        }
        return pairs;

    }

    private Set<Term> processQuery(String query) {
        final Porter stemmer = new Porter();
        return Arrays.stream(Utility.stripText(query).split(" "))
                .filter((val) -> !parser.isStopWord(val))
                .map((val) -> new Term(stemmer.stripAffixes(val.toLowerCase())))
                .collect(Collectors.toSet());
    }

    private void serializeData(String filename, Object obj){
        try {
            File dataFolder = new File("data");

            if (!dataFolder.exists())
                dataFolder.mkdir();

            File file = new File(dataFolder, filename);
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream out = new ObjectOutputStream(fos);

            out.writeObject(obj);

            out.close();
            fos.close();
        }catch (IOException  e){
            e.printStackTrace();

        }
    }

    /**
     * prints out every mutual word between the sub cluster for a cluster
     * @param c cluster number
     */
    @Override
    public void mergeSubClusters(int c){
        Cluster cluster = mainClusters.get(c);
        int wordCount = parser.getInvertedIndex().size();
        double[][] k = new double[Cluster.GROUPS_COUNT][wordCount];

        for (int i = 0; i < Cluster.GROUPS_COUNT; i++){
            for (Document d : cluster.getFollowers().get(i)) {
                for (int j = 0; j < wordCount; j++) {
                    k[i][j] += d.getVector()[j];
                }
            }
        }
        // find the mutual words
        ArrayList<Pair<String, int[]>> pairs = new ArrayList<>();
        for (int i = 0; i < Cluster.GROUPS_COUNT; i++){
            for (int j = i+1; j < Cluster.GROUPS_COUNT; j++){
                int y = 0;
                int[] iner = intersect(k[i], k[j]);
                pairs.add(new Pair<>("Subcluster " + i + " " + j + " intersectopn " + iner[wordCount], iner));
            }

        }
        // filter out the non important words
        for (int i = 0; i < pairs.size(); i++){
            for (int j = i+1; j < pairs.size(); j++){
                for (int y = 0; y < wordCount; y++){
                    if (pairs.get(i).second[y] > 0 && pairs.get(j).second[y] > 0){
                        pairs.get(i).second[y] = 0;
                        //pairs.get(j).second[y] = 0;
                    }
                }
            }
        }

        for (Pair<String, int[]> p: pairs){
            int i = 0;
            System.out.println(p.first);
            for (Term t : parser.getInvertedIndex().keySet()){
                if (p.second[i] > 0)
                    System.out.print(", " + t.getTerm());
                i++;
            }
            System.out.println();
        }
    }

    private int[] intersect(double[] doubles, double[] doubles1) {
        int i = 0;
        int[] arr = new int [doubles.length+1];
        for (int j = 0; j < doubles.length; j++){
            if (doubles[j] > 0 && doubles1[j] > 0){
                arr[j] = 1;
                i++;
            }
        }
        arr[doubles.length] = i;
        return arr;
    }

    @Override
    public List<Cluster> getMainClusters() {
        return mainClusters;
    }

    @Override
    public int getSubClustersCount(int i) {
        return mainClusters.get(i).getFollowers().keySet().size();
    }

    public Parser getParser() {
        return parser;
    }

    private static class ClusterDistanceComparator implements Comparator<Cluster>{
        private double[] arr;

        private ClusterDistanceComparator(double[] arr){
            this.arr = arr;
        }

        @Override
        public int compare(Cluster o1, Cluster o2) {
            try {
                return Double.compare(Utility.cosineSimilarity(arr, o1.getCentroid()), Utility.cosineSimilarity(arr, o2.getCentroid()));

            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }
    }

    private static class CosineComparator implements Comparator<Document>{
        private double[] arr;
        
        private CosineComparator(double[] arr){
            this.arr = arr;
        }
         
        @Override
        public int compare(Document o1, Document o2) {
            try {
                return Double.compare(Utility.cosineSimilarity(arr, o2.getVector()),
                        Utility.cosineSimilarity(arr, o1.getVector()));

            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }
    }
    
    // comparator class to help determine the order of the priority queue
    // by measuring the distance between two documents
    // and how much they're far from the leader document
    private static class DistanceComparator implements Comparator<Document>{
        private double[] arr;

        private DistanceComparator(double[] c){
            arr = c;
        }

        @Override
        public int compare(Document o1, Document o2) {
            try {
                return Double.compare(Utility.cosineSimilarity(arr, o1.getVector())
                        , Utility.calcDistance(arr, o2.getVector()));
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }
    }

}

