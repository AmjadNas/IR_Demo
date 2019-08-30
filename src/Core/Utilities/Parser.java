package Core.Utilities;

import Core.Onjects.Document;
import Core.Onjects.Pair;
import Core.Onjects.Term;

import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class Parser implements Serializable {

    private TreeMap<Term, TreeSet<Document>> invertedIndex;
    private HashSet<String> stopWords;
    private int filesLength;
    private ArrayList<Pair<Document, Integer>> docs;

    public Parser() throws IOException {
        System.out.println("Initializing Parser");
        loadStopWords();
        loadData();
    }

    /**
     * Loads the stop words list into memory.
     *
     * @throws IOException
     */
    private void loadStopWords() throws IOException {
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {
            System.out.println("Loading Stop Words");
            stopWords = new HashSet<>();
            String line;
            File file = new File("stopwords.txt");
            fileReader = new FileReader(file);
            bufferedReader = new BufferedReader(fileReader);
            while ((line = bufferedReader.readLine()) != null) {
                stopWords.add(line);
            }
            System.out.println("Done loading stop words");
        } finally {
            if (fileReader != null)
                fileReader.close();
            if (bufferedReader != null)
                bufferedReader.close();
        }
    }

    /**
     * loads all the documents, parses them
     *
     * @throws IOException
     */
    private void loadData() throws IOException {
        int i = 0;
        File folder = new File("docs/");
        final String EXTENSION = ".txt";

        if (folder.isDirectory()) {
            File[] listOfFiles;
            invertedIndex = new TreeMap<>();
            

            if ((listOfFiles = folder.listFiles()) != null) {
                System.out.println("Parsing Data");
                filesLength = listOfFiles.length;
                docs = new ArrayList<>( filesLength);
                
                processAsync(listOfFiles);
               /* Porter stemmer = new Porter();
                TreeMap<Term, Pair<Double, double[]>> tree = new TreeMap<>();
                
                for (File file : listOfFiles) {
                    if (file.isFile() && file.getName().endsWith(EXTENSION)) {
                        System.out.println(file.getName());
                        parseDocument(file, i, stemmer, tree);
                        i++;
                    }
                }
                invertedIndex.remove(new Term(""));
                tree.remove(new Term(""));

                calcTermsIDF(tree);
                setDocsVectors(tree);*/
            }
        }
    }

    /**
     * loads files concurrently
     * @param listOfFiles list of files in directory
     */
    private void processAsync(File[] listOfFiles) {
        int begin = 0;
        int numCores = Runtime.getRuntime().availableProcessors();
        int chunks =  filesLength / numCores;
        List<CompletableFuture<Void>> list = new ArrayList<>();
        Porter stemmer = new Porter();
        TreeMap<Term, Pair<Double, double[]>> tree = new TreeMap<>();
        
        if ( filesLength % chunks > 0) {
            numCores += 1;
        }

        for (int i = 1; i <= numCores; i++) {
            int end = i * chunks - 1;
            int dist = end - begin;

            if (dist + begin >  filesLength) {
                dist =  filesLength - begin - 1;
            }

            list.add(createTask(listOfFiles, begin, dist, stemmer, tree));
            begin = end + 1;
        }

        // wait for all chunks to finish
        CompletableFuture.allOf(list.toArray(new CompletableFuture[numCores]));
        docs.sort((e1, e2) -> e1.first.compareTo(e2.first));
        invertedIndex.remove(new Term(""));
        tree.remove(new Term(""));

        calcTermsIDF(tree);
        setDocsVectors(tree);
    }

    private CompletableFuture<Void> createTask(File[] listOfFiles, int begin, int curr, Porter stemmer, TreeMap<Term, Pair<Double, double[]>> tree) {
        return CompletableFuture.runAsync(() -> {
            for (int i = begin; i <= curr; i++) {
                try {
                    parseDocument(listOfFiles[i], i, stemmer, tree);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * static method that reads saved from disk
     *
     * @return Parses instance in case it was found else returns a null
     */
    public static Parser deSerializeData() {
        try {
            Parser parser;
            File dataFolder = new File("data");

            if (!dataFolder.exists())
                return null;

            File file = new File(dataFolder, "parser.bin");
            FileInputStream fileInputStream = new FileInputStream(file);
            ObjectInputStream in = new ObjectInputStream(fileInputStream);

            parser = (Parser) in.readObject();

            in.close();
            fileInputStream.close();

            return parser;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Calculates the TF-IDF for every term and saves them aggregates them in a vector that represents
     * the document the method takes a Tree map that in the key has the term and in the
     * value has a pair of df and an existence vector that describes the tf in all documents.
     *
     * @param tree sorted terms
     */
    private void setDocsVectors(TreeMap<Term, Pair<Double, double[]>> tree) {
        double[] arr;
        int j;
        double val;
        Term t;

        for (int i = 0; i < docs.size(); i++) {
            arr = new double[invertedIndex.size()];
            j = 0;
            for (Map.Entry<Term, Pair<Double, double[]>> set : tree.entrySet()) {
                t = set.getKey();

                if ((val = set.getValue().second[i]) == 0)
                    arr[j] = 0;
                else
                    arr[j] = (1 + Math.log(val)) * t.getDf();

                j++;
            }
            docs.get(i).first.setVector(arr);
        }
    }

    /**
     * Calculates the idf for every term and saves it.
     *
     * @param tree sorted terms
     */
    private void calcTermsIDF(TreeMap<Term, Pair<Double, double[]>> tree) {
        double idf;
        for (Map.Entry<Term, Pair<Double, double[]>> set : tree.entrySet()) {
            //if (set.getKey())
            double division = filesLength / set.getValue().first;
            idf = Math.log(division);
            set.getKey().setIdf(idf);
        }
    }

    /**
     * A helper method that takes a file that points to the document on disk and parses it.
     *
     * @param file
     * @param i
     * @param stemmer
     * @param tree
     * @throws IOException
     */
    private void parseDocument(File file, int i, Porter stemmer, TreeMap<Term, Pair<Double, double[]>> tree) throws IOException {
        BufferedReader reader = null;
        FileReader fileReader = null;

        String str;
        try {
            HashSet<String> scannedTerms = new HashSet<>();
            fileReader = new FileReader(file);
            reader = new BufferedReader(fileReader);
            Pair<Document, Integer> dTemp = new Pair<>(new Document(i, file.getName()), 0);
            while ((str = reader.readLine()) != null) {
                parseText(dTemp, str, i, scannedTerms, stemmer, tree);
            }
            docs.add(dTemp);
        } finally {
            if (fileReader != null)
                fileReader.close();

            if (reader != null)
                reader.close();
        }
    }

    /**
     * A helper method used in parseDocument method that
     * parses the text and filters it out from s
     * stop words  and stems the terms before storing them.
     *
     * @param dTemp
     * @param text
     * @param i
     * @param scannedTerms
     * @param stemmer
     * @param tree
     */
    private void parseText(Pair<Document, Integer> dTemp, String text, int i, HashSet<String> scannedTerms, Porter stemmer, TreeMap<Term, Pair<Double, double[]>> tree) {
        Term t;
        Pair<Double, double[]> pTemp;

        text = Utility.stripText(text);

        for (String word : text.split("\\W+")) {
            if (!stopWords.contains(word) && !word.contains("_")) {
                word = stemmer.stripAffixes(word);
                t = new Term(word);
                if ((pTemp = tree.get(t)) != null) {
                    if (!scannedTerms.contains(word)) {
                        pTemp.first++;
                        invertedIndex.get(t).add(dTemp.first);
                        scannedTerms.add(word);
                    }
                    pTemp.second[i]++;
                } else {
                    double[] arr = new double[filesLength];
                    arr[i]++;
                    TreeSet<Document> tempDocs = new TreeSet<>();
                    tempDocs.add(dTemp.first);
                    invertedIndex.put(t, tempDocs);
                    tree.put(t, new Pair<>(1.0, arr));
                    scannedTerms.add(word);

                }
            }
            dTemp.second++;
        }
    }

    /**
     * Calculates the average length of all documents
     *
     * @return average for document length
     */
    public double getAverageDocumentLength() {
        return docs.stream()
                .mapToDouble(val -> val.second)
                .summaryStatistics()
                .getAverage();
    }

    /**
     * Returns the top limit terms sorted by their IDF
     *
     * @param limit
     * @return top limit words
     */
    public List<String> top30TermsByIDF(int limit) {
        return invertedIndex.keySet().stream()
                .sorted((c, c2) -> Double.compare(c2.getDf(), c.getDf()))
                .map(Term::getTerm)
                .limit(limit)
                .collect(Collectors.toList());
    }

    public HashSet<String> getStopWords() {
        return stopWords;
    }

    public int getFilesLength() {
        return filesLength;
    }

    public int getVocabularySize() {
        return invertedIndex.size();
    }

    public ArrayList<Pair<Document, Integer>> getDocs() {
        return docs;
    }

    public TreeMap<Term, TreeSet<Document>> getInvertedIndex() {
        return invertedIndex;
    }

    public boolean isStopWord(String val) {
        return stopWords.contains(val);
    }

    @Override
    public String toString() {
        return String.format("Collection size: %d" +
                "\nVocabulary size: %d" +
                "\nAverage document size: %.3f", getFilesLength(), invertedIndex.size(), getAverageDocumentLength());
    }

}
