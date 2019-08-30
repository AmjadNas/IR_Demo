package UI;

import javax.swing.*;
import java.awt.*;

public class MyDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;

    public MyDialog(Frame frame, boolean b) {
        super(frame, b);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setBounds(new Rectangle(320, 290));
        setContentPane(contentPane);
       // setModal(true);
        setVisible(true);
    }


}
