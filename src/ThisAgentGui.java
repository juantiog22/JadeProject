package JadeProject;

import jade.core.AID;
import jade.domain.FIPAException;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class ThisAgentGui  extends JFrame {

    private ThisAgent myAgent;

    ThisAgentGui( ThisAgent a){
        super(a.getLocalName());

        myAgent= a;

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 2));

        JButton b1 = new JButton("Have a Meeting");

        b1.addActionListener(new ActionListener () {
            public void actionPerformed(ActionEvent ev) {
                try{
                    myAgent.haveMeeting();
                }
                catch(Exception e) {
                    JOptionPane.showMessageDialog(ThisAgentGui.this, e.getMessage());
                }
            }
        });

        b1.setPreferredSize(new Dimension(300,30));

        panel.add(b1);

        getContentPane().add(panel, BorderLayout.SOUTH);

        setResizable(false);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                myAgent.doDelete();
            }
        });
    }

    public void display(){
        pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int centX = (int) screenSize.getWidth() / 2;
        int centY = (int) screenSize.getHeight() / 2;
        setLocation(centX - getWidth() / 2, centY - getHeight() / 2);
        setVisible(true);

    }
}
