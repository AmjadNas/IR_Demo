package UI;

import Core.Controller;
import Core.Interfaces.I_Controller;
import Core.Onjects.Pair;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class TopWords extends JFrame implements Runnable{
    private JPanel panel1;
    private JTextArea textArea1;
    private int clusterNum, limit;

    public TopWords(int clusterNum,int limit){
        this.clusterNum = clusterNum;
        this.limit = limit;
    }

    @Override
    public void run() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(new Rectangle(800, 600));
        setContentPane(panel1);
        new SwingWorker<>() {
            @Override
            protected Object doInBackground() throws Exception {
                I_Controller controller = Controller.getInstance();
                List<Pair<String, Double>> list = controller.getTopWordsForCluster(clusterNum, limit);
                textArea1.setText("Cluster #" + clusterNum
                        +"\nTop "+ limit + " Words");
                list.forEach(w -> textArea1.append("\n" + w.first));
                return null;
            }
        }.execute();

        setVisible(true);
    }
}
