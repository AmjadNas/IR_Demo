package Core.Utilities;

public final class Utility {

    private Utility(){}

    public static double getTF(double tfidf, double idf){
        if (idf > 0){
            double tf = tfidf/idf;
            tf = Math.pow(2, tf-1);
            //if (tf > 0 && tf < 1)
             //   return 1;
            return Math.round(tf);
        }
        return -1;
    }

    public static double calcSqrDistance(double[] v1, double[] v2) throws Exception {
        double diff, sum = 0;

        if (v1.length != v2.length)
            throw new Exception("Vector length mismatch!");

        for (int i = 0; i < v1.length; i++){
            diff = v1[i] - v2[i];
            sum += Math.pow(diff, 2);
        }

        return sum;
    }

    /**
     * calculates the euclidean distance between vectors
     * @param v1
     * @param v2
     * @return
     * @throws Exception
     */
    public static double calcDistance(double[] v1, double[] v2) throws Exception {
        double diff, sum = 0;

        if (v1.length != v2.length)
            throw new Exception("Vector length mismatch!");

        for (int i = 0; i < v1.length; i++){
            diff = v1[i] - v2[i];
            sum += Math.pow(diff, 2);
        }

        return Math.sqrt(sum);
    }

    /**
     * returns the unit vector for a given vector
     * @param v1
     * @return
     */
    public static double[] normalizeVector(double[] v1){
        double dist = calcVectorLength(v1);
        double[] normalized = new double[v1.length];
        
        for (int i = 0; i < v1.length; i++){
            normalized[i] = v1[i]/dist;
        }

        return normalized;
    }

    /**
     * calculates a given vector's length
     * @param v1
     * @return
     */
    public static double calcVectorLength(double[] v1)  {
        double sum = 0;
        for (double v : v1){
            sum += Math.pow(v, 2);
        }
        
        return Math.sqrt(sum);
    }

    /**
     * Strips the text from numbers, emails, links and returns the words only
     * @param text
     * @return filtered text
     */
    public static String stripText(String text) {

        String lower_case = text.toLowerCase()
                .replaceAll("-"+System.lineSeparator(), "");

        return lower_case.replaceAll("((https?|ftp|gopher|telnet|file|Unsure|http):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)","")
                .replaceAll("([^.@\\s]+)(\\.[^.@\\s]+)*@([^.@\\s]+\\.)+([^.@\\s]+)", "")
                .replaceAll("http.*", "")
                .replaceAll("\\W+", " ")
                .replaceAll("[\\d]", "" );
    }

    /**
     * calculates the cosine similarity between two vectors
     * @param v1
     * @param v2
     * @return
     * @throws Exception
     */
    public static double cosineSimilarity(double[] v1, double[] v2) throws Exception {
        if (v1.length != v2.length)
            throw new Exception("Vector length mismatch!");
        double sum = 0;

        for (int i = 0; i < v1.length; i++)
            sum += v1[i] * v2[i];

        return sum;
    }
}
