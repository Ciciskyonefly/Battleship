package org.ustc.scst.dc.battleship;

/** The model listener */
public interface IBattleshipModelListener {

  /**
   * The model has changed.
   * 
   * @param event
   *          the model change event
   */
  public abstract void battleshipModelChanged(final BattleshipModelEvent event);
}
