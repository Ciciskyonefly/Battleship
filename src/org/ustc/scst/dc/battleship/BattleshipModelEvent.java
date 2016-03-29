package org.ustc.scst.dc.battleship;

/**
 * The model event
 */
public class BattleshipModelEvent {

  /** the game state has changed */
  public static final int CHANGE_FLAG_GAME_STATE = 1;

  /** a cell state has changed */
  public static final int CHANGE_FLAG_CELL_STATE = (CHANGE_FLAG_GAME_STATE << 1);

  /** all change flags */
  private static final int ALL_CHANGE_FLAGS = (CHANGE_FLAG_GAME_STATE | CHANGE_FLAG_CELL_STATE);

  /** the change */
  private final int m_whatHasChanged;

  /** the model */
  private final BattleshipModel m_model;

  /** the x-coordinate of the changed field */
  private final int m_x;

  /** the y-coordinate of the changed field */
  private final int m_y;

  /** the old state bit mask */
  private final int m_oldState;

  /**
   * The model event
   * 
   * @param model
   *          the model
   * @param what
   *          the what has changed flags
   * @param oldstate
   *          the old state
   * @param x
   *          the x-coordinate of the changed cell
   * @param y
   *          the y-coordinate of the changed cell
   */
  public BattleshipModelEvent(final BattleshipModel model, final int what,
      final int oldstate, final int x, final int y) {
    super();

    final int ch;

    ch = (what & ALL_CHANGE_FLAGS);
    if (ch != what) {
      throw new IllegalArgumentException("Illegal change flags!"); //$NON-NLS-1$
    }

    this.m_model = model;
    this.m_whatHasChanged = ch;
    this.m_oldState = oldstate;

    if ((what & CHANGE_FLAG_CELL_STATE) != 0) {
      this.m_x = x;
      this.m_y = y;
    } else {
      this.m_x = (-1);
      this.m_y = (-1);
    }
  }

  /**
   * Get the change flags
   * 
   * @return the change flags
   */
  public final int whatHasChanged() {
    return this.m_whatHasChanged;
  }

  /**
   * get the old state of the game or cell
   * 
   * @return the old state of the game or cell
   */
  public final int getOldState() {
    return this.m_oldState;
  }

  /**
   * Get the model
   * 
   * @return the model
   */
  public final BattleshipModel getModel() {
    return this.m_model;
  }

  /**
   * Get the x-coordinate of the cell
   * 
   * @return the x-coordinate of the cell
   */
  public final int getX() {
    return this.m_x;
  }

  /**
   * Get the y-coordinate of the cell
   * 
   * @return the y-coordinate of the cell
   */
  public final int getY() {
    return this.m_y;
  }

}
