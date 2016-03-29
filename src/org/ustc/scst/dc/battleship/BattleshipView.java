package org.ustc.scst.dc.battleship;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;

/** the ship view */
public class BattleshipView extends JSplitPane implements
    IBattleshipModelListener {

  /** the default serial version uid */
  private static final long serialVersionUID = 1L;

  /** the player panel */
  private final ShipPanel m_player;

  /** the enemy panel */
  private final ShipPanel m_enemy;

  /** the model */
  private final BattleshipModel m_model;

  /**
   * the ship view
   * 
   * @param m
   *          the model
   */
  public BattleshipView(final BattleshipModel m) {
    super();

    this.m_model = m;
    m.addListener(this);

    this.m_player = new ShipPanel(m, true);
    this.m_enemy = new ShipPanel(m, false);

    this.setLeftComponent(this.m_player);
    this.setRightComponent(this.m_enemy);
    this.setDividerLocation(0.5);
    this.setResizeWeight(0.5d);
    this.setContinuousLayout(true);
  }

  /** {@inheritDoc} */
  @Override
  public final void battleshipModelChanged(final BattleshipModelEvent event) {
    final int i;
    int j;
    int state, winner;
    final BattleshipModel model;

    i = event.whatHasChanged();
    model = this.m_model;

    state = -1;
    winner = BattleshipModel.WINNER_NOBODY;
    if ((i & BattleshipModelEvent.CHANGE_FLAG_GAME_STATE) != 0) {
      synchronized (model) {
        state = model.getGameState();
        if ((state & BattleshipModel.GAME_STATE_END) != 0) {
          winner = model.whoWon();
        }
      }
    }

    if (state == -1) {
      return;
    }
    if ((state & BattleshipModel.GAME_STATE_END) != 0) {
      JOptionPane
          .showMessageDialog(
              this,
              ((winner == BattleshipModel.WINNER_ENEMY) ? "You have lost the game." : //$NON-NLS-1$
                  ((winner == BattleshipModel.WINNER_PLAYER) ? "You have won the game." : //$NON-NLS-1$
                      "The game has ended with no winner.")));//$NON-NLS-1$
      System.exit(0);
      return;
    }

    if ((state & BattleshipModel.GAME_STATE_INITIALIZED) != 0) {
      j = this.m_model.getNextShipLengthToPlace();
      JOptionPane
          .showMessageDialog(
              this,
              "Please now place your ships by selecting cells in the left window.\n" + //$NON-NLS-1$
                  "The next ship to place has a length of " + j + ".");//$NON-NLS-1$//$NON-NLS-2$
      return;
    }

    if ((state & BattleshipModel.GAME_STATE_PLAYER_READY) != 0) {
      JOptionPane
          .showMessageDialog(
              this,
              "You have finished placing the ships. We now wait for the enemy to be ready.");//$NON-NLS-1$
      return;
    }

    if ((state & BattleshipModel.GAME_STATE_PLAYING) != 0) {
      JOptionPane
          .showMessageDialog(
              this,//
              "You have placed your ships and the enemy is ready.\nNow play by firing at fields in the right window.");//$NON-NLS-1$
      return;
    }
  }

  /** the player's view schema */
  static final Color[] SCHEME_A = new Color[] {//
  new Color(0, 0, 190, 255),//
      new Color(40, 40, 255, 255),//
      Color.GREEN, Color.RED };

  /** the enemy view schema */
  static final Color[] SCHEME_B = new Color[] { SCHEME_A[0], SCHEME_A[1],
      Color.RED, Color.GREEN };

  /** A ship panel */
  class ShipPanel extends JComponent implements IBattleshipModelListener {

    /** the serial version uid */
    private static final long serialVersionUID = 1L;

    /** the color scheme */
    private final Color[] m_scheme;

    /** the ship flag */
    private final int m_shipFlag;

    /** the seen flag */
    private final int m_seenFlag;

    /** the model */
    final BattleshipModel m_bsModel;

    /** are we a player ? */
    private final boolean m_isPlayer;

    /**
     * Create a ship panel
     * 
     * @param model
     *          the model
     * @param isPlayer
     *          are we the player field?
     */
    public ShipPanel(final BattleshipModel model, final boolean isPlayer) {
      super();
      final int w, h;

      if (isPlayer) {
        this.m_scheme = SCHEME_A;
        this.m_shipFlag = BattleshipModel.CELL_STATE_PLAYER_SHIP;
        this.m_seenFlag = BattleshipModel.CELL_STATE_ENEMY_HAS_SEEN;
      } else {
        this.m_scheme = SCHEME_B;
        this.m_shipFlag = BattleshipModel.CELL_STATE_ENEMY_SHIP;
        this.m_seenFlag = BattleshipModel.CELL_STATE_PLAYER_HAS_SEEN;
      }

      this.m_bsModel = model;
      this.m_isPlayer = isPlayer;

      this.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(final MouseEvent e) {
          ShipPanel.this.onClick(
          //
              ((e.getX() * ShipPanel.this.m_bsModel.getFieldWidth()) / ShipPanel.this
                  .getWidth()), ((e.getY() * ShipPanel.this.m_bsModel
                  .getFieldHeight()) / ShipPanel.this.getHeight()));
        }
      });

      h = model.getFieldHeight();
      w = model.getFieldWidth();

      this.setMinimumSize(new Dimension(8 * w, 8 * h));
      this.setPreferredSize(new Dimension(30 * w, 30 * h));
      model.addListener(this);
    }

    /**
     * A click was detected (ok, i will do this in a bad style, i am tired)
     * 
     * @param x
     *          the x-coordinate
     * @param y
     *          the y-coordinate
     */
    final void onClick(final int x, final int y) {
      int i, length;
      final BattleshipModel m;
      Throwable q;

      if (this.m_isPlayer) {

        m = this.m_bsModel;
        q = null;
        synchronized (m) {
          length = m.getNextShipLengthToPlace();

          if (length <= 0) {
            JOptionPane.showMessageDialog(this, //
                "Currently, there is no ship to place.");//$NON-NLS-1$
            return;
          }

          if (length > 1) {
            i = JOptionPane
                .showConfirmDialog(
                    this,//
                    "Select 'yes' ship with length " + length + //$NON-NLS-1$
                        " horizontally at (" + x + //$NON-NLS-1$
                        ", " + y + //$NON-NLS-1$
                        "), 'no' to placed it vertically, 'Cancel' to cancel.",//$NON-NLS-1$
                    "Place ship!", //$NON-NLS-1$
                    JOptionPane.YES_NO_CANCEL_OPTION);
          } else {
            i = JOptionPane.showConfirmDialog(this,//
                "Place ship with length 1 at (" + x + //$NON-NLS-1$
                    ", " + y + //$NON-NLS-1$
                    ")?",//$NON-NLS-1$
                "Place ship!", //$NON-NLS-1$
                JOptionPane.OK_CANCEL_OPTION);
            if (i == JOptionPane.OK_OPTION) {
              i = JOptionPane.YES_OPTION;
            }
          }

          if (i != JOptionPane.CANCEL_OPTION) {
            try {
              m.placeShip(length, x, y, (i == JOptionPane.YES_OPTION));
            } catch (Throwable t) {
              q = t;
            }
          }

          i = this.m_bsModel.getNextShipLengthToPlace();
          if (i > 0) {
            JOptionPane.showMessageDialog(this,
                "Now place a ship with length " + i + //$NON-NLS-1$
                    ".");//$NON-NLS-1$
          }
        }

        if (q != null) {
          JOptionPane.showMessageDialog(this, q.getMessage());
        }
        return;
      }

      try {
        this.m_bsModel.playerHasSeen(x, y);
      } catch (Throwable t) {
        JOptionPane.showMessageDialog(this, t.getMessage(), "Error!", //$NON-NLS-1$
            JOptionPane.ERROR_MESSAGE);
      }
    }

    /** {@inheritDoc} */
    @Override
    public final void battleshipModelChanged(
        final BattleshipModelEvent event) {
      final int i;

      i = event.whatHasChanged();
      if ((i & BattleshipModelEvent.CHANGE_FLAG_GAME_STATE) != 0) {
        this.repaint();
        return;
      }

      if ((i & BattleshipModelEvent.CHANGE_FLAG_CELL_STATE) != 0) {
        if ((((this.m_bsModel.getCellState(event.getX(), event.getY())) & //
        (~event.getOldState())) & (this.m_seenFlag | this.m_shipFlag)) != 0) {
          this.repaint();
          return;
        }
      }
    }

    /** {@inheritDoc} */
    @Override
    public void paint(final Graphics g) {
      int y, x, sx, sy, ex, ey, state;
      final Color[] colors;
      final int w, h, fieldWidth, fieldHeight, ship, seen;
      final BattleshipModel model;
      Color choose;

      w = this.getWidth();
      h = this.getHeight();

      model = this.m_bsModel;
      fieldWidth = model.getFieldWidth();
      fieldHeight = model.getFieldHeight();
      ship = this.m_shipFlag;
      seen = this.m_seenFlag;

      colors = this.m_scheme;
      g.setColor(colors[0]);
      g.fillRect(0, 0, w, h);

      synchronized (model) {
        for (y = fieldHeight; (--y) >= 0;) {
          ey = ((((y + 1) * h) / fieldHeight) - 2);
          sy = (((y * h) / fieldHeight) + 2);

          for (x = fieldWidth; (--x) >= 0;) {
            ex = ((((x + 1) * w) / fieldWidth) - 2);
            sx = (((x * w) / fieldWidth) + 2);

            state = model.getCellState(x, y);
            choose = (((state & ship) == 0) ? colors[1] : colors[2]);
            g.setColor(choose);
            g.fillRect(sx, sy, ex - sx, ey - sy);

            if ((state & seen) != 0) {
              g.setColor(colors[3]);
              g.drawLine(sx, sy, ex, ey);
              g.drawLine(ex, sy, sx, ey);
            }
          }
        }
      }
    }
  }

}
