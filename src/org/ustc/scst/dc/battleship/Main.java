package org.ustc.scst.dc.battleship;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/** the main window */
public class Main extends JFrame {

  /** serial version uid */
  private static final long serialVersionUID = 1L;

  /** the own port */
  private JTextField m_ownPort;

  /** the other port */
  private JTextField m_otherPort;

  /** the other address */
  private JTextField m_otherAddress;

  /** the model */
  private final BattleshipModel m_model;

  /** the communicator */
  private final Communicator m_com;

  /** the view */
  private BattleshipView m_view;

  /**
   * The main method
   * 
   * @param ownPort
   *          the own port
   * @param otherPort
   *          the other port
   * @param otherAddress
   *          the other address
   */
  public Main(final String ownPort, final String otherPort,
      final String otherAddress) {
    super("Battleship"); //$NON-NLS-1$

    this.m_model = new BattleshipModel();

    this.m_com = new Communicator(this.m_model);

    this.makeConnectionPane(ownPort, otherPort, otherAddress);

    this.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent e) {
        Main.this.exit();
      }
    });

    this.setDefaultCloseOperation(EXIT_ON_CLOSE);
  }

  /**
   * make the connection pane
   * 
   * @param ownPort
   *          the own port
   * @param otherPort
   *          the other port
   * @param otherAddress
   *          the other address
   */
  private final void makeConnectionPane(final String ownPort,
      final String otherPort, final String otherAddress) {
    GridBagLayout layout;
    GridBagConstraints gc;
    JButton v;
    JComponent c;
    int x, y;
    Insets ins;
    final JPanel p;
    Dimension d, cur;

    this.setVisible(false);

    p = new JPanel();
    layout = new GridBagLayout();
    p.setLayout(layout);

    ins = new Insets(2, 2, 2, 2);

    y = 0;
    x = 0;
    gc = new GridBagConstraints(
        //
        x, y, 1, 1, 0d, 0d, GridBagConstraints.EAST,
        GridBagConstraints.NONE, ins, 1, 1);
    c = new JLabel("Own port:"); //$NON-NLS-1$
    p.add(c);
    layout.addLayoutComponent(c, gc);

    x++;
    gc = new GridBagConstraints(
        //
        x, y, 1, 1, 1d, 0d, GridBagConstraints.WEST,
        GridBagConstraints.HORIZONTAL, ins, 1, 1);
    if (this.m_ownPort == null) {
      this.m_ownPort = new JTextField((ownPort != null) ? ownPort
          : "45000"); //$NON-NLS-1$        
    }
    p.add(this.m_ownPort);
    layout.addLayoutComponent(this.m_ownPort, gc);

    y++;
    x = 0;
    gc = new GridBagConstraints(
        //
        x, y, 1, 1, 0d, 0d, GridBagConstraints.EAST,
        GridBagConstraints.NONE, ins, 1, 1);
    c = new JLabel("Enemy address:"); //$NON-NLS-1$
    p.add(c);
    layout.addLayoutComponent(c, gc);

    x++;
    gc = new GridBagConstraints(
        //
        x, y, 1, 1, 1d, 0d, GridBagConstraints.WEST,
        GridBagConstraints.HORIZONTAL, ins, 1, 1);
    if (this.m_otherAddress == null) {
      this.m_otherAddress = new JTextField(
          (otherAddress != null) ? otherAddress : "localhost"); //$NON-NLS-1$        
    }
    p.add(this.m_otherAddress);
    layout.addLayoutComponent(this.m_otherAddress, gc);

    y++;
    x = 0;
    gc = new GridBagConstraints(
        //
        x, y, 1, 1, 0d, 0d, GridBagConstraints.EAST,
        GridBagConstraints.NONE, ins, 1, 1);
    c = new JLabel("Enemy port:"); //$NON-NLS-1$
    p.add(c);
    layout.addLayoutComponent(c, gc);

    x++;
    gc = new GridBagConstraints(
        //
        x, y, 1, 1, 1d, 0d, GridBagConstraints.WEST,
        GridBagConstraints.HORIZONTAL, ins, 1, 1);
    if (this.m_otherPort == null) {
      this.m_otherPort = new JTextField((otherPort != null) ? otherPort
          : "45000"); //$NON-NLS-1$        
    }
    p.add(this.m_otherPort);
    layout.addLayoutComponent(this.m_otherPort, gc);

    y++;
    x = 0;
    gc = new GridBagConstraints(
        //
        x, y, 2, 1, 1d, 0d, GridBagConstraints.CENTER,
        GridBagConstraints.HORIZONTAL, ins, 1, 1);

    v = new JButton("Connect!"); //$NON-NLS-1$
    v.addActionListener(new ActionListener() {
      @Override
      public final void actionPerformed(ActionEvent e) {
        Main.this.connect();
      }
    });

    p.add(v);
    layout.addLayoutComponent(v, gc);

    this.setContentPane(p);
    this.pack();

    d = Toolkit.getDefaultToolkit().getScreenSize();
    cur = this.getSize();
    this.setLocation(((d.width - cur.width) >>> 1),
        ((d.height - cur.width) >>> 1));
    this.setVisible(true);
  }

  /**
   * Connect!
   */
  final void connect() {
    try {
      this.m_com.start(Integer.parseInt(this.m_ownPort.getText()), //
          this.m_otherAddress.getText(),//
          Integer.parseInt(this.m_otherPort.getText()));
    } catch (Throwable t) {
      JOptionPane.showMessageDialog(this, t.getMessage());
      return;
    }
    this.makeGamePane();
  }

  /**
   * The game pane
   */
  private final void makeGamePane() {
    Dimension d, e;
    Point p;

    if (this.m_view == null) {
      this.m_view = new BattleshipView(this.m_model);
    }

    d = this.getSize();
    this.setContentPane(this.m_view);
    this.pack();
    e = this.getSize();
    p = this.getLocation();
    this.setLocation((p.x + ((d.width - e.width) / 2)),//
        (p.y + ((d.height- e.height) / 2)));

    this.setVisible(true);
    this.m_model.initialize();
  }

  /**
   * Disconnet
   */
  final void disconnect() {
    this.m_model.endGame(BattleshipModel.WINNER_ENEMY);
    this.makeConnectionPane(null, null, null);
  }

  /**
   * Stop the communication
   */
  final void exit() {
    this.m_model.endGame(BattleshipModel.WINNER_ENEMY);
    this.m_com.stop();
  }

  /**
   * The main method
   * 
   * @param args
   *          the arguments
   * @throws Throwable
   *           the throwable
   */
  public static final void main(final String[] args) throws Throwable {
    String ownPort, otherPort, otherAddress;

    ownPort = null;
    otherPort = null;
    otherAddress = null;

    if (args != null) {
      if (args.length > 0) {
        ownPort = args[0];
        if ((ownPort != null) && (ownPort.length() <= 0)) {
          ownPort = null;
        }

        if (args.length > 1) {
          otherAddress = args[1];
          if ((otherAddress != null) && (otherAddress.length() <= 0)) {
            otherAddress = null;
          }

          if (args.length > 2) {
            otherPort = args[2];
            if ((otherPort != null) && (otherPort.length() <= 0)) {
              otherPort = null;
            }
          }
        }
      }
    }

    new Main(ownPort, otherPort, otherAddress).isActive();
  }

}
