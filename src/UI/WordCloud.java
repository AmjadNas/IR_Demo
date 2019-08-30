package UI;

import Core.Controller;
import Core.Interfaces.I_Controller;
import org.mcavallo.opencloud.Cloud;
import org.mcavallo.opencloud.Tag;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WordCloud extends JFrame implements Runnable{

    private JPanel mainPanel;
    private int num, subNum;

    public WordCloud(int num, int subNum){
        this.num = num;
        this.subNum = subNum;
    }

    @Override
    public void run() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(new Rectangle(800, 600));
        setContentPane(mainPanel);
        new SwingWorker<>(){

            @Override
            protected Object doInBackground() throws Exception {
                try {
                    List<Cloud> clouds = new ArrayList<>();
                    Cloud cloud = new Cloud();
                    I_Controller ctrl = Controller.getInstance();
                    int i = 0;

                    for (Map.Entry<String, Double> set : ctrl.wordFrequencies(num, subNum).entrySet()){
                        System.out.println(set);
                        cloud.addTag(new Tag(set.getKey(), set.getValue()*100));
                        if ((i % 50) == 0){
                            clouds.add(cloud);
                            cloud = new Cloud();
                        }
                        i++;
                    }
                    JPanel panel = new JPanel();
                  //  panel.setLayout(new GridLayout(50, clouds.size()));
                   // JScrollPane scrollPane = new JScrollPane(panel);

                    for (Cloud c : clouds)
                        for (Tag tag : c.tags()) {

                            final JLabel label = new JLabel(tag.getName());
                            label.setOpaque(false);
                            label.setFont(label.getFont().deriveFont((float) tag.getWeight() * 10));
                            panel.add(label);

                        }

                    add(panel);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();

        setVisible(true);
    }
}
