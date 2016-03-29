package org.ustc.scst.dc.battleship;

import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.SwingUtilities;

/**
 * The model of the battleship space
 */
public final class BattleshipModel {

  /** the default field width */
  private static final int DEFAULT_FIELD_WIDTH = 12;

  /** the default field width */
  private static final int DEFAULT_FIELD_HEIGHT = DEFAULT_FIELD_WIDTH;

  /** the default number of ships to place */
  private static final int[] DEFAULT_SHIPS_TO_PLACE = new int[] { 5, 3, 2,
      1 };

  /** the game unintialized game state */
  public static final int GAME_STATE_UNINITIALIZED = 0;

  /** the game is ready to place the ships */
  public static final int GAME_STATE_INITIALIZED = (GAME_STATE_UNINITIALIZED + 1);

  /**
   * the game state denoting that all own ships have been placed and the player
   * is ready
   */
  public static final int GAME_STATE_PLAYER_READY = (GAME_STATE_INITIALIZED << 1);

  /**
   * the game state denoting that both, the player and her enemy, are ready and
   * are playing
   */
  public static final int GAME_STATE_PLAYING = (GAME_STATE_PLAYER_READY << 1);

  /** the game state denoting that the game has ended */
  public static final int GAME_STATE_END = (GAME_STATE_PLAYING << 1);

  /** a cell is empty */
  public static final int CELL_STATE_EMPTY = 0;

  /** a player's ship is in the cell */
  public static final int CELL_STATE_PLAYER_SHIP = (CELL_STATE_EMPTY + 1);

  /** the player has seen the field */
  public static final int CELL_STATE_PLAYER_HAS_SEEN = (CELL_STATE_PLAYER_SHIP << 1);

  /** an enemy's ship is in the cell */
  public static final int CELL_STATE_ENEMY_SHIP = (CELL_STATE_PLAYER_HAS_SEEN << 1);

  /** the enemy has seen the field */
  public static final int CELL_STATE_ENEMY_HAS_SEEN = (CELL_STATE_ENEMY_SHIP << 1);

  /** the player won */
  public static final int WINNER_PLAYER = -1;

  /** the enemy won */
  public static final int WINNER_ENEMY = 1;

  /** nobody won */
  public static final int WINNER_NOBODY = 0;

  /** the model listeners */
  private final ArrayList<IBattleshipModelListener> m_listeners;

  /** the state of the field */
  private final int[][] m_cellStates;

  /** the ships that can be placed */
  private final int[] m_allShipsToPlace;

  /** the ships that can be placed */
  private final ArrayList<ShipPlacement> m_shipsToPlace;

  /** the game state */
  private int m_gameState;

  /** the field width */
  private final int m_width;

  /** the field height */
  private final int m_height;

  /** who won the game? */
  private int m_winner;

  /** the total amount of ship cells */
  private final int m_maxShipCells;

  /** the own ship cells */
  private int m_ownShipCells;

  /** the enemy ship cells */
  private int m_enemyShipCells;

  /** the enemy is ready */
  private boolean m_enemyReady;

  /** the last player */
  private int m_lastPlayer;

  /** Create the default game model */
  public BattleshipModel() {
    super();

    int s, j;

    this.m_width = DEFAULT_FIELD_WIDTH;
    this.m_height = DEFAULT_FIELD_HEIGHT;

    this.m_allShipsToPlace = DEFAULT_SHIPS_TO_PLACE.clone();
    s = 0;
    for (j = this.m_allShipsToPlace.length; (--j) >= 0;) {
      s += ((j + 1) * this.m_allShipsToPlace[j]);
    }
    this.m_maxShipCells = s;

    this.m_cellStates = new int[this.m_height][this.m_width];
    this.m_shipsToPlace = new ArrayList<>();
    this.m_listeners = new ArrayList<>();

    this.m_gameState = GAME_STATE_UNINITIALIZED;
  }

  /**
   * Get the maximum number of ship cells that either the player or the enemy
   * can have
   * 
   * @return the total number of ship cells
   */
  public final int getMaxShipCells() {
    return this.m_maxShipCells;
  }

  /**
   * Get the number of undestroyed ship cells the player has
   * 
   * @return the number of undestroyed ship cells the player has
   */
  public synchronized final int getPlayerShipCells() {
    return this.m_ownShipCells;
  }

  /**
   * Get the number of undestroyed ship cells the enemy has
   * 
   * @return the number of undestroyed ship cells the enemy has
   */
  public synchronized final int getEnemyShipCells() {
    return this.m_enemyShipCells;
  }

  /**
   * Get the length of the next ship to be placed, or -1 if none is left to be
   * placed
   * 
   * @return the length of the next ship to be placed, or -1 if none is left to
   *         be placed
   */
  public final synchronized int getNextShipLengthToPlace() {
    int s;

    if (this.m_gameState == GAME_STATE_INITIALIZED) {
      s = this.m_shipsToPlace.size();
      if (s > 0) {
        return this.m_shipsToPlace.get(s - 1).m_length;
      }
    }

    return -1;
  }

  /**
   * Get the field width
   * 
   * @return the field width
   */
  public final int getFieldWidth() {
    return this.m_width;
  }

  /**
   * Get the field height
   * 
   * @return the field height
   */
  public final int getFieldHeight() {
    return this.m_height;
  }

  /**
   * Add a model listener
   * 
   * @param l
   *          the model listener
   */
  public synchronized final void addListener(
      final IBattleshipModelListener l) {
    if ((l != null) && (!(this.m_listeners.contains(l)))) {
      this.m_listeners.add(l);
    }
  }

  /**
   * Remove a given listener
   * 
   * @param l
   *          the listener
   */
  public synchronized final void removeListener(
      final IBattleshipModelListener l) {
    this.m_listeners.remove(l);
  }

  /**
   * Propagate a battleship model event
   * 
   * @param event
   *          the event
   */
  final void dispatchEvent(final DispatchableBattleshipModelEvent event) {

    if (event.m_first) {
      event.m_first = false;
      SwingUtilities.invokeLater(event);
      return;
    }

    synchronized (this) {
      for (IBattleshipModelListener m : this.m_listeners) {
        try {
          m.battleshipModelChanged(event);
        } catch (Throwable t) {//
        }
      }
    }
  }

  /**
   * Obtain the game state
   * 
   * @return the game state
   */
  public synchronized final int getGameState() {
    return this.m_gameState;
  }

  /**
   * Return who won this game: -1 for the player, 1 for the enemy, 0 for nobody
   * 
   * @return the indicator for who won the game
   */
  public synchronized final int whoWon() {
    return this.m_winner;
  }

  /**
   * Get the state of the cell at coordinates x and y
   * 
   * @param x
   *          the x-coordinate of the cell
   * @param y
   *          the y-coordinate of the cell
   * @return the state of that cell
   */
  public synchronized final int getCellState(final int x, final int y) {
    return this.m_cellStates[y][x];
  }

  /**
   * Initialized the field
   */
  public synchronized final void initialize() {
    int y, x, i;
    final int[][] cs;
    int[] r;
    final int oldState;

    oldState = this.m_gameState;
    if ((oldState != GAME_STATE_UNINITIALIZED)) {
      throw new IllegalStateException(//
          "Only an uninitalized game can be initialized!"); //$NON-NLS-1$
    }

    cs = this.m_cellStates;
    for (int[] row : cs) {
      Arrays.fill(row, CELL_STATE_EMPTY);
    }

    this.m_enemyReady = false;
    this.m_shipsToPlace.clear();
    for (i = this.m_allShipsToPlace.length; (--i) >= 0;) {
      this.m_shipsToPlace.add(new ShipPlacement(i + 1,
          this.m_allShipsToPlace[i]));
    }
    this.m_gameState = GAME_STATE_INITIALIZED;
    this.m_winner = WINNER_NOBODY;
    this.m_lastPlayer = WINNER_NOBODY;
    this.m_ownShipCells = this.m_maxShipCells;
    this.m_enemyShipCells = this.m_maxShipCells;

    this.dispatchEvent(new DispatchableBattleshipModelEvent(this,//
        BattleshipModelEvent.CHANGE_FLAG_GAME_STATE, oldState, -1, -1));

    for (y = cs.length; (--y) >= 0;) {
      r = cs[y];
      for (x = r.length; (--x) >= 0;) {
        this.dispatchEvent(new DispatchableBattleshipModelEvent(
            this,//
            BattleshipModelEvent.CHANGE_FLAG_CELL_STATE, CELL_STATE_EMPTY,
            x, y));
      }
    }
  }

  // /**
  // * Force game initialization
  // */
  // public synchronized final void forceInitialize() {
  // final int oldState;
  //
  // oldState = this.m_gameState;
  // if ((oldState != GAME_STATE_UNINITIALIZED)
  // && (oldState != GAME_STATE_END)) {
  // this.endGame(WINNER_ENEMY);
  // }
  // this.initialize();
  // }

  /**
   * Terminate and set a winner
   * 
   * @param winner
   *          the winner
   */
  public final synchronized void endGame(final int winner) {
    final int state;

    if ((winner != WINNER_NOBODY) && (winner != WINNER_PLAYER)
        && (winner != WINNER_ENEMY)) {
      throw new IllegalStateException(
          "Winner must be either " + WINNER_NOBODY + //$NON-NLS-1$
              ", " + WINNER_PLAYER + //$NON-NLS-1$
              ", or " + WINNER_ENEMY + //$NON-NLS-1$
              ".");//$NON-NLS-1$
    }

    state = this.m_gameState;
    if (state != GAME_STATE_END) {
      this.m_gameState = GAME_STATE_END;
      this.m_winner = winner;
      this.m_shipsToPlace.clear();
      this.dispatchEvent(new DispatchableBattleshipModelEvent(this,
          BattleshipModelEvent.CHANGE_FLAG_GAME_STATE, state, -1, -1));
    } else {
      if (this.m_winner != winner) {
        throw new IllegalStateException(//
            "The game has already finished and was won by someone else."); //$NON-NLS-1$
      }
    }
  }

  /**
   * The enemy is ready to play
   */
  public synchronized final void enemyIsReady() {
    final int oldState;

    oldState = this.m_gameState;
    if ((oldState != GAME_STATE_INITIALIZED)
        && (oldState != GAME_STATE_PLAYER_READY)) {
      throw new IllegalStateException(//
          "The enemy player said that she is ready, but we are in the wrong state."); //$NON-NLS-1$
    }

    this.m_enemyReady = true;
    if (this.m_gameState == GAME_STATE_PLAYER_READY) {
      this.m_gameState = GAME_STATE_PLAYING;
      this.dispatchEvent(//
      new DispatchableBattleshipModelEvent(this,//
          BattleshipModelEvent.CHANGE_FLAG_GAME_STATE, oldState, -1, -1));
    }
  }

  /**
   * The enemy has seen a given cell
   * 
   * @param x
   *          the x-coordinate of the cell
   * @param y
   *          the y-coordinate of the cell
   * @throws IllegalStateException
   *           if something goes wrong
   */
  public final void enemyHasSeen(final int x, final int y)
      throws IllegalStateException {
    this.cellStateChange(x, y, CELL_STATE_ENEMY_HAS_SEEN);
  }

  /**
   * The player has seen a given cell
   * 
   * @param x
   *          the x-coordinate of the cell
   * @param y
   *          the y-coordinate of the cell
   * @throws IllegalStateException
   *           if something goes wrong
   */
  public final void playerHasSeen(final int x, final int y)
      throws IllegalStateException {
    this.cellStateChange(x, y, CELL_STATE_PLAYER_HAS_SEEN);
  }

  /**
   * The enemy has revealed a ship that has been hit at the given position
   * 
   * @param x
   *          the x-coordinate of the cell
   * @param y
   *          the y-coordinate of the cell
   * @throws IllegalStateException
   *           if something goes wrong
   */
  public final void enemyHasShip(final int x, final int y)
      throws IllegalStateException {
    this.cellStateChange(x, y, CELL_STATE_ENEMY_SHIP);
  }

  /**
   * Modify the given cell's state
   * 
   * @param x
   *          the x-coordinate
   * @param y
   *          the y-coordinate
   * @param modification
   *          the modification
   * @throws IllegalStateException
   *           the illegal state exception
   */
  private final void cellStateChange(final int x, final int y,
      final int modification) throws IllegalStateException {
    final int oldCellState, nu, oldState;
    final boolean shipLost, shipWon, gameLost, gameWon;

    if ((y < 0) || (y >= this.m_height)) {
      throw new IllegalStateException(//
          "Y coordinate out of bounds."); //$NON-NLS-1$
    }

    if ((x < 0) || (x >= this.m_width)) {
      throw new IllegalStateException(//
          "X coordinate out of bounds."); //$NON-NLS-1$
    }

    if (modification == 0) {
      throw new IllegalStateException(//
          "Empty modification not allowed."); //$NON-NLS-1$
    }

    synchronized (this) {
      oldState = this.m_gameState;
      if (oldState != GAME_STATE_PLAYING) {
        throw new IllegalStateException(//
            "Modifications are only possible during the game."); //$NON-NLS-1$        
      }

      oldCellState = this.m_cellStates[y][x];
      if ((oldCellState & modification) != 0) {
        throw new IllegalStateException(//
            "The modification cannot be applied to this cell."); //$NON-NLS-1$
      }

      if ((modification & CELL_STATE_PLAYER_HAS_SEEN) != 0) {
        if (this.m_lastPlayer == WINNER_PLAYER) {
          throw new IllegalStateException(//
              "You need to wait for the enemy before being allowed to view another field."); //$NON-NLS-1$
        }
      }

      if ((modification & CELL_STATE_ENEMY_HAS_SEEN) != 0) {
        if (this.m_lastPlayer == WINNER_ENEMY) {
          throw new IllegalStateException(//
              "The enemy needs to wait for you before being allowed to view another field."); //$NON-NLS-1$
        }
      }

      nu = (oldCellState | modification);

      shipLost = (((modification & CELL_STATE_ENEMY_HAS_SEEN) != 0) && //
      ((nu & CELL_STATE_PLAYER_SHIP) != 0));
      if (shipLost) {
        gameLost = ((--this.m_ownShipCells) <= 0);
      } else {
        gameLost = false;
      }

      if (((modification & CELL_STATE_ENEMY_SHIP) != 0) && //
          (nu & CELL_STATE_PLAYER_HAS_SEEN) == 0) {
        throw new IllegalArgumentException(//
            "An enemy ship cannot become visible without being seen first." //$NON-NLS-1$
        );
      }

      shipWon = ((((nu & CELL_STATE_PLAYER_HAS_SEEN) != 0) && //
      ((modification & CELL_STATE_ENEMY_SHIP) != 0)));

      if (shipWon) {
        gameWon = ((--this.m_enemyShipCells) <= 0);
      } else {
        gameWon = false;
      }

      if (shipLost && shipWon) {
        throw new IllegalArgumentException(//
            "Disallowed batch operation." //$NON-NLS-1$
        );
      }

      if (gameWon || gameLost) {
        this.m_gameState = GAME_STATE_END;
        if (gameWon) {
          this.m_winner = WINNER_PLAYER;
        } else {
          this.m_winner = WINNER_ENEMY;
        }
      }

      this.m_cellStates[y][x] = nu;

      if ((modification & CELL_STATE_PLAYER_HAS_SEEN) != 0) {
        this.m_lastPlayer = WINNER_PLAYER;
      }
      if ((modification & CELL_STATE_ENEMY_HAS_SEEN) != 0) {
        this.m_lastPlayer = WINNER_ENEMY;
      }

      this.dispatchEvent(new DispatchableBattleshipModelEvent(this,
          BattleshipModelEvent.CHANGE_FLAG_CELL_STATE, oldCellState, x, y));
      if (this.m_gameState != oldState) {
        this.dispatchEvent(new DispatchableBattleshipModelEvent(this,
            BattleshipModelEvent.CHANGE_FLAG_GAME_STATE, oldState, -1, -1));
      }
    }
  }

  /**
   * Place a ship of the given length on the field
   * 
   * @param length
   *          the length of the ship
   * @param x
   *          the x-coordinate of the upper-left corner of the ship
   * @param y
   *          the y-coordinate of the upper-left corner of the ship
   * @param hor
   *          true if the ship has been placed horizontally, false if it has
   *          been placed vertically
   * @throws IllegalStateException
   *           the illegal state exception
   */
  public synchronized final void placeShip(final int length, final int x,
      final int y, final boolean hor) throws IllegalStateException {
    final int width, height, shipWidth, shipHeight;
    int placementIndex, i, j, k;
    final int[][] state;
    final int oldState;
    ShipPlacement p;
    DispatchableBattleshipModelEvent[] events;
    DispatchableBattleshipModelEvent v;

    oldState = this.m_gameState;
    if (oldState != GAME_STATE_INITIALIZED) {
      throw new IllegalStateException(//
          "Ships can only be placed during initialization."); //$NON-NLS-1$
    }

    placementIndex = -1;
    p = null;
    test: {
      for (i = this.m_shipsToPlace.size(); (--i) >= 0;) {
        p = this.m_shipsToPlace.get(i);
        if (p.m_length == length) {
          placementIndex = i;
          break test;
        }
      }

      throw new IllegalStateException(//
          "No ship of length " + length + //$NON-NLS-1$ 
              " can be placed now."); //$NON-NLS-1$
    }

    shipWidth = (hor ? length : 1);
    shipHeight = (hor ? 1 : length);

    state = this.m_cellStates;
    height = this.m_height;
    width = this.m_width;

    if ((y < 0) || ((y + shipHeight) > height)) {
      throw new IllegalStateException(//
          "The ship exceeds the vertical size of the field."); //$NON-NLS-1$
    }

    if ((x < 0) || ((x + shipWidth) > width)) {
      throw new IllegalStateException(//
          "The ship exceeds the horizontal size of the field."); //$NON-NLS-1$
    }

    for (i = (y + shipHeight); (--i) >= y;) {
      if ((i < 0) || (i >= height)) {
        throw new IllegalStateException(//
            "One of the ships vertical coordinates is invalid."); //$NON-NLS-1$
      }
      for (j = (x + shipWidth); (--j) >= x;) {
        if ((j < 0) || (j >= width)) {
          throw new IllegalStateException(//
              "One of the ships horizontal coordinates is invalid."); //$NON-NLS-1$
        }
        if ((state[i][j] & CELL_STATE_PLAYER_SHIP) != 0) {
          throw new IllegalStateException(//
              "The ship intersects with another ship of the player."); //$NON-NLS-1$
        }
      }
    }

    if (p != null) {
      if ((--p.m_count) <= 0) {
        this.m_shipsToPlace.remove(placementIndex);
      }
    }
    events = new DispatchableBattleshipModelEvent[length];

    k = length;
    for (i = (y + shipHeight); (--i) >= y;) {
      for (j = (x + shipWidth); (--j) >= x;) {
        events[--k] = new DispatchableBattleshipModelEvent(this,
            BattleshipModelEvent.CHANGE_FLAG_CELL_STATE, state[i][j], j, i);
        state[i][j] |= CELL_STATE_PLAYER_SHIP;
      }
    }

    v = null;
    if (this.m_shipsToPlace.size() <= 0) {
      if (this.m_gameState == GAME_STATE_INITIALIZED) {
        if (this.m_enemyReady) {
          this.m_gameState = GAME_STATE_PLAYING;
        } else {
          this.m_gameState = GAME_STATE_PLAYER_READY;
        }
        v = new DispatchableBattleshipModelEvent(this,
            BattleshipModelEvent.CHANGE_FLAG_GAME_STATE, oldState, -1, -1);
      }
    }

    for (DispatchableBattleshipModelEvent e : events) {
      this.dispatchEvent(e);
    }
    if (v != null) {
      this.dispatchEvent(v);
    }
  }

  /** a model event */
  private static final class DispatchableBattleshipModelEvent extends
      BattleshipModelEvent implements Runnable {

    /** first */
    boolean m_first;

    /**
     * The model event
     * 
     * @param model
     *          the model
     * @param change
     *          the change flags
     * @param oldstate
     *          the old state
     * @param x
     *          the x-coordinate of the changed cell
     * @param y
     *          the y-coordinate of the changed cell
     */
    public DispatchableBattleshipModelEvent(final BattleshipModel model,
        final int change, final int oldstate, final int x, final int y) {
      super(model, change, oldstate, x, y);
      this.m_first = true;
    }

    /** run */
    @Override
    public final void run() {
      this.getModel().dispatchEvent(this);
    }
  }

  /** the ship placement */
  private static final class ShipPlacement {
    /** the ship length */
    final int m_length;
    /** the ship count */
    int m_count;

    /**
     * Create a new ship placement
     * 
     * @param length
     *          the ship length
     * @param count
     *          the number of ships available
     */
    ShipPlacement(final int length, final int count) {
      super();
      this.m_length = length;
      this.m_count = count;
    }
  }

	
}
