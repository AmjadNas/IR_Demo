package UI;

import Core.Controller;
import Core.Interfaces.I_Controller;
import Core.Onjects.Cluster;
import Core.Onjects.Document;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;
import java.util.stream.IntStream;

public class MainScreen extends JFrame implements Runnable, WindowListener{

    private JPanel panel1, pp1, pp2;
    private JButton searchBtn;
    private JTextField queryField;
    private JRadioButton clusterRdBtn;
    private JRadioButton vSpaceRdBtn;
    private JRadioButton booleanRbBtn;
    private JList<Document> list1;
    private JPanel resultsPane;
    private JTextArea clusterSummery;
    private JPanel pp3;
    private JTextArea clusters;
    private JPanel pp4;
    private JButton button1;
    private JComboBox<Integer> comboBox1;
    private JTextField topLimit;
    private JComboBox<Integer> comboBox2;
    private JButton topWordsButton;
    private JTextField textField1;
    private JComboBox<Integer> subClusterComboBox;
    //private JPanel nothingPanel;
    private I_Controller controller;
    private SwingWorker<Void, Void> dataLoadWorker  = new SwingWorker<Void, Void>(){
        MyDialog dialog;

        @Override
        protected Void doInBackground()  {
            try {
                dialog = new MyDialog(MainScreen.this, false);
                //   dialog.setModal(true);
                System.out.println("Loading");

                controller = Controller.getInstance();
                System.out.println("Loaded");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void done() {
            super.done();
            dialog.setModal(false);
            dialog.dispose();
            clusterSummery.setText(controller.getParserSummery());
            clusterSummery.append("\n"+controller.getClustersSummery());
            clusterSummery.append(controller.getDistancesBtwnClusters());
            controller.getMainClusters().forEach(c -> clusters.append(c.toString()));
        }
    };

    private void initTabs(){
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add("Search Engine", pp1);
        tabbedPane.add("Data Summery", pp2);
        tabbedPane.add("Clusters", pp3);
        tabbedPane.add("Word Cloud Generator", pp4);
        panel1.add(tabbedPane);
        IntStream.range(1, Cluster.CLUSTER_NUM+1).forEach((i) ->{
            comboBox1.addItem(i);
            comboBox2.addItem(i);
        });
        comboBox1.addItemListener((e) ->{
            if (e.getStateChange() == ItemEvent.SELECTED){
                subClusterComboBox.removeAllItems();
                int i = comboBox1.getItemAt(comboBox1.getSelectedIndex())-1;
                IntStream.range(1, controller.getSubClustersCount(i)+1).forEach((j) ->{
                    subClusterComboBox.addItem(j);
                });
            }
        });
        list1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list1.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting())
                    System.out.println(list1.getModel().getElementAt(list1.getSelectedIndex()));
            }
        });
    }

    private void initBtn(){
        searchBtn.addActionListener(e -> {
            Vector<Document> docs = null;
            try {
                int i = Integer.valueOf(topLimit.getText());
                System.out.println("fetching data");
                if(clusterRdBtn.isSelected()){
                    docs = controller.fetchResultsCluster(queryField.getText(), i);
                }
                else if(vSpaceRdBtn.isSelected()){
                    docs = controller.fetchResultsCosine(queryField.getText(), i);
                }
                else if(booleanRbBtn.isSelected()){
                    docs = controller.fetchResultsBoolean(queryField.getText(), i);
                }
                if (docs != null && !docs.isEmpty())
                    list1.setListData(docs);
                else
                    JOptionPane.showMessageDialog(MainScreen.this, "Couldn't find any results.", "No Results", JOptionPane.INFORMATION_MESSAGE);
            }catch (NumberFormatException ex){
                JOptionPane.showMessageDialog(MainScreen.this, "Illegal Input!", "Error", JOptionPane.ERROR_MESSAGE);
            }

        });
        button1.addActionListener(e -> {
            int i = comboBox1.getItemAt(comboBox1.getSelectedIndex()) -1;
            int j = subClusterComboBox.getItemAt(subClusterComboBox.getSelectedIndex()) -1;
            SwingUtilities.invokeLater(new WordCloud(i, j));
        });
        topWordsButton.addActionListener(e -> {
            try {
                int i = comboBox1.getItemAt(comboBox2.getSelectedIndex()) -1;
                int lim = Integer.valueOf(textField1.getText());
                SwingUtilities.invokeLater(new TopWords(i, lim));
                controller.printMutualWords(i);
            }catch (NumberFormatException ex){
                JOptionPane.showMessageDialog(MainScreen.this, "Illegal Input!", "Error", JOptionPane.ERROR_MESSAGE);
            }

        });
    }

    @Override
    public void run() {
        dataLoadWorker.execute();
        setTitle("Information Retrieval Demo");
        setContentPane(panel1);
        setBounds(new Rectangle(640, 480));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        initTabs();
        initBtn();
        setVisible(true);
    }

    @Override
    public void windowOpened(WindowEvent e) {}

    @Override
    public void windowClosing(WindowEvent e) {
        if (dataLoadWorker != null && dataLoadWorker.getState() == SwingWorker.StateValue.STARTED)
            dataLoadWorker.cancel(true);
    }

    @Override
    public void windowClosed(WindowEvent e) {}

    @Override
    public void windowIconified(WindowEvent e) {}

    @Override
    public void windowDeiconified(WindowEvent e) {}

    @Override
    public void windowActivated(WindowEvent e) {}

    @Override
    public void windowDeactivated(WindowEvent e) {}

}
