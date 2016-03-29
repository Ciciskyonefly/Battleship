package org.ustc.scst.dc.battleship;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JOptionPane;

/**
 * The communicator. Here you can implement all the code necessary for
 * performing the communication between the players.
 */
public class Communicator implements IBattleshipModelListener, Runnable {

  /** the model */
  private final BattleshipModel m_model;

  /** are we running? */
  private boolean m_running;

 
  // add your member variables here
  private ServerSocket server ;
  private Socket serverClient,client;
  private int ownPort,enemyPort;
  private String enemyHost;
  private DataOutputStream dos;
  private DataInputStream dis;
 // private InetAddress[] ia;
  // end

  /**
   * Create the communicator
   * 
   * @param m
   *          the model
   */
  public Communicator(final BattleshipModel m) {
    super();
    this.m_model = m;
  }

  /**
   * Setup the communicator
   * 
   * @param ownPort
   *          the own port
   * @param enemyHost
   *          the enemy host
   * @param enemyPort
   *          the enemy port
   * @throws IOException
   *           if something goes wrong
   */
  public synchronized final void start(final int ownPort,
      final String enemyHost, final int enemyPort) throws IOException {

    if (!(this.m_running)) {
      //add
      server = new ServerSocket(ownPort);
      this.ownPort = ownPort;
      this.enemyHost = enemyHost;
      this.enemyPort = enemyPort;
      System.out.println("Server run...");
      //end
      
      this.m_model.addListener(this);
      this.m_running = true;

      new Thread(this).start();
    }
  }

  /**
   * Stop this thread
   */
  public synchronized final void stop() {
    System.out.println("Communicator has been stopped."); //$NON-NLS-1$
    if (this.m_running) {
      this.m_running = false;

      // Add by QiQi
     try{
    	 server.close();
    	 client.close();
    	 dos.close();
    	 dis.close();
     }catch(IOException e){
    	 e.printStackTrace();
     }
      // end

    }
    System.out.println("Server resources released."); //$NON-NLS-1$
  }

  /**
   * The server loop
   */
  @Override
  public final void run() {
    while (this.m_running) {
      // Add by QiQi
    	try {
			serverClient = server.accept();
			dis = new DataInputStream(serverClient.getInputStream());
			String s = dis.readUTF();
			
			if("The enemy has discovered one of our ships".equals(s)){
				System.out.println("Running enemyShipDiscovered method...");
				int x = dis.readInt();
				int y = dis.readInt();
				this.enemyShipDiscovered(x,y);
			}else if("Player Has Seen".equals(s)){
				System.out.println("Running enemyHasSeen method...");
				int x = dis.readInt();
				int y = dis.readInt();
				this.enemyHasSeen(x, y);
			}else if("We are ready!!!".equals(s)){
				this.enemyIsReady();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
     //End
    }
  }

  /** <p>
   * The player has clicked an enemy cell and wants to see whats
   * behind it. We need to tell "the other side", i.e., the enemy,
   * about that. In other words, we need to send a message with the
   * two coordinates x and y to the enemy. The "other side" will
   * then call the corresponding method {@link #enemyHasSeen(int, int)}.
   * </p><p>
   * If we have revealed a ship, the enemy's game engine will invoke
   * the method "playerShipDiscovered" on his side, which will then
   * send a message back to us.</p>
   * 
   * @param x
   *          the x-coordinate of the cell
   * @param y
   *          the y-coordinate of the cell
   */
  public synchronized void playerHasSeen(final int x, final int y) {
	  //Add by QiQi
	  try{
    	client = new Socket(enemyHost,enemyPort);
		dos =  new DataOutputStream(client.getOutputStream());
		dos.writeUTF("Player Has Seen");
		dos.writeInt(x);
		dos.writeInt(y);
		dos.flush();
    }catch (IOException e) {
		e.printStackTrace();
	}
	  //End
  }

  /**
   * The enemy has discovered one of our ships. We should tell him that. The
   * other side will then call the method {@link #enemyShipDiscovered(int, int)}
   * .
   * 
   * @param x
   *          the x-coordinate of the cell
   * @param y
   *          the y-coordinate of the cell
   */
//TODO
  public synchronized void playerShipDiscovered(final int x, final int y) {
	  //Add by QiQi
	 try {
		client = new Socket(enemyHost,enemyPort);
		dos =  new DataOutputStream(client.getOutputStream());
		dos.writeUTF("The enemy has discovered one of our ships");
		dos.writeInt(x);
		dos.writeInt(y);
		dos.flush();
	 } catch (IOException e) {
		e.printStackTrace();
	}
	 //End
  }

  /**
   * The player is ready. We need to tell that to the other side. The other side
   * will then call the method {@link #enemyIsReady()}
   */
  //TODO
  public synchronized void playerIsReady() {
	  //Add by QiQi
	  try {
		client = new Socket(enemyHost,enemyPort);
		dos =  new DataOutputStream(client.getOutputStream());
		dos.writeUTF("We are ready!!!");
		dos.flush();
	} catch (IOException e) {
		e.printStackTrace();
	}
	  //End
  }

  /**
   * The enemy has seen a cell. The other side must have told us that.
   * 
   * @param x
   *          the x-coordinate of the cell
   * @param y
   *          the y-coordinate of the cell
   */
  public synchronized final void enemyHasSeen(final int x, final int y) {
    try {
      this.m_model.enemyHasSeen(x, y);
    } catch (Throwable t) {
      this.onError(t);
    }
  }

  /**
   * We have discovered an enemy ship. The other side must have told us that.
   * 
   * @param x
   *          the x-coordinate of the cell
   * @param y
   *          the y-coordinate of the cell
   */
  public synchronized final void enemyShipDiscovered(final int x,
      final int y) {
    try {
      this.m_model.enemyHasShip(x, y);
    } catch (Throwable t) {
      this.onError(t);
    }
  }

  /**
   * The enemy is ready. The other side must have told us that
   */
  public synchronized final void enemyIsReady() {
    try {
      this.m_model.enemyIsReady();
    } catch (Throwable t) {
      this.onError(t);
    }
  }

  //
  // better not touch stuff below that line
  //

  /**
   * An error has been caused. Maybe the game state was inconsistent. We should
   * terminate the game and maybe send an error message to the other side.
   * 
   * @param t
   *          the error
   */
  public synchronized final void onError(final Throwable t) {
    this.stop();
    JOptionPane
        .showMessageDialog(
            null,
            "An error has occured that lead to game termination. The error message is '" //$NON-NLS-1$
                + t.getMessage() + "'.");//$NON-NLS-1$
    this.m_model.endGame(BattleshipModel.WINNER_NOBODY);
  }

  /** {@inheritDoc} */
  @Override
  public synchronized final void battleshipModelChanged(
      final BattleshipModelEvent event) {
    int whatHasChanged, oldState, change, state, /* winner, */x, y;
    final BattleshipModel model;

    whatHasChanged = event.whatHasChanged();
    oldState = event.getOldState();
    model = this.m_model;
    if ((whatHasChanged & BattleshipModelEvent.CHANGE_FLAG_GAME_STATE) != 0) {
      synchronized (model) {
        change = ((model.getGameState()) & (~oldState));
      }

      if ((change & BattleshipModel.GAME_STATE_PLAYER_READY) != 0) {
        this.playerIsReady();
      } else {
        if (((change & BattleshipModel.GAME_STATE_PLAYING) != 0)
            && ((oldState & BattleshipModel.GAME_STATE_PLAYER_READY) == 0)) {
          this.playerIsReady();
        }
      }

      return;
    }

    if ((whatHasChanged & BattleshipModelEvent.CHANGE_FLAG_CELL_STATE) != 0) {
      x = event.getX();
      y = event.getY();

      state = model.getCellState(x, y);
      change = (state & (~oldState));

      if ((change & BattleshipModel.CELL_STATE_PLAYER_HAS_SEEN) != 0) {
        this.playerHasSeen(x, y);
      }

      if (((change & BattleshipModel.CELL_STATE_ENEMY_HAS_SEEN) != 0)
          && ((state & BattleshipModel.CELL_STATE_PLAYER_SHIP) != 0)) {
        this.playerShipDiscovered(x, y);
      }
    }
  }
}
