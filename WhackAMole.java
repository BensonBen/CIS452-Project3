package package1;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Random;
import java.util.concurrent.Semaphore;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

public class WhackAMole implements ActionListener {
	
	/*Color for the mole holes*/
	private final static Color DOWN_COLOR = new Color(156, 93, 82);
	
	/*Count is initially 0 it represents the time remaining to whack moles*/
	private static int count = 10;
	
	/*Represents the current score of the player who's playing*/
	private static int score;
	
	/*Button to press to start the game*/
	private static JButton startButton;
	
	/*Array of buttons that represents the mole mounds*/
	private JButton[][] buttons;
	
	/*Label for the amount of time remaining*/
	private JLabel timeLabel;
	
	/*Label for the score of the player*/
	private JLabel scoreLabel;
	
	/*A text area to display the amount of time the player has remaining*/
	private static JTextArea timeArea;
	
	/*A text area to display the score of the player playing*/
	private static JTextArea scoreArea;
	
	/*A randomization object to support random time intervals for moles to appear*/
	private static Random random = new Random(); 
	
	/*Semaphore object to control the amount of active moles*/
	private static Semaphore semaphore;
	
	/*An array of moleThreads one per button*/
	private static Thread[][] moleThreads;
	
	/*The initial size of the game*/
	private static int sizeOfGame = 8;
	
	/*Helps intialize the semaphore set, but getting user input from the GUI*/
	private static int numMoles = 5;
	
	/*Boolean to facilitate if the moles can keep appearing or not*/
	private static boolean time = true;
	
	/*center button panel for the mole buttons to appear on*/
	private JPanel centerPanel;
	
	/*button panel for time, start, and*/
	private JPanel northPanel;
	
	/*Image object to hold the mole image*/
	private static Image moleImage;
	
	/*Image for the mallet object*/
	private static Image mallet;
	
	/*Frame object for when the user wants to start a new game*/
	/*Is a global variable so a thread can listen for its closing*/
	private static JFrame newGameFrame;
	
	/***************************************************************************************
	 * Initializes GUI components
	 **************************************************************************************/
	public WhackAMole() {
		Toolkit toolkit = java.awt.Toolkit.getDefaultToolkit();
		try{
			moleImage = ImageIO.read(getClass().getResource("./rsz_mole.jpg"));
			mallet = toolkit.getImage("./rsz_1rsz_mallet.png");
		}catch(Exception e){
			System.out.println("path to image incorrect");
		}
		
		JFrame frame = new JFrame("Whack-a-Mole");
		frame.setSize(1224, 768);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());

		//get the size of the game from the user...
		centerPanel = new JPanel(new GridLayout(8,8, 5, 5));
		northPanel = new JPanel();

		startButton = new JButton("Start");

		northPanel.add(startButton);
		startButton.addActionListener(this);

		timeLabel = new JLabel("Time Left:");
		northPanel.add(timeLabel);

		timeArea = new JTextArea(1, 5);
		timeArea.setEditable(false);
		northPanel.add(timeArea);
		timeArea.setVisible(true);

		scoreLabel = new JLabel("Score:");
		northPanel.add(scoreLabel);

		scoreArea = new JTextArea(1, 5);
		scoreArea.setEditable(false);
		northPanel.add(scoreArea);
		scoreArea.setVisible(true);

		addButtons();
		frame.add(northPanel,BorderLayout.NORTH);
		frame.add(centerPanel, BorderLayout.CENTER);
		frame.setVisible(true);
	}
	
	/***********************************************************************
	 * Repaints the center panel with the appropriate amount of buttons
	 * appropriately paints the button array
	 * adds action listeners to the buttons
	 * then marks the center panel for changes in the GUI tree structure
	 ***********************************************************************/
	private void addButtons(){
		buttons = new JButton[sizeOfGame][sizeOfGame];
		centerPanel.removeAll();
		centerPanel.setLayout(new GridLayout(sizeOfGame, sizeOfGame, 5, 5));
		for(int i = 0; i<sizeOfGame; i++){
			for(int j = 0; j<sizeOfGame; j++){
				buttons[i][j] = new JButton();
				buttons[i][j].setOpaque(true);
				buttons[i][j].setBackground(DOWN_COLOR);
				buttons[i][j].addActionListener(this);
				centerPanel.add(buttons[i][j]);
			}
		}
		centerPanel.revalidate();
		centerPanel.repaint();
	}
	
	/************************************************************************
	 * creates spinners
	 * A frame
	 * And appropriate action listeners
	 * 
	 ************************************************************************/
	private static void promptUserInput(){
		//------------------------------------------------------
		//Create initial GUI components
		//------------------------------------------------------
		newGameFrame = new JFrame();
		newGameFrame.setTitle("Game Setup");
		newGameFrame.setLayout(new BorderLayout());
		newGameFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		JPanel panel = new JPanel(new GridLayout(4,2));
		JButton closeButton = new JButton("done");
		closeButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				newGameFrame.dispose();
			}
		});
		//------------------------------------------------------
		//Set up the spinners 
		//------------------------------------------------------
		SpinnerModel modelTimer =
				new SpinnerNumberModel(count,
						5,
						100,
						1);        
		JLabel timeLabel = new JLabel("Time: ");
		
		JSpinner timeSpinner = new JSpinner(modelTimer);
		JSpinner.DefaultEditor editor1 = ( JSpinner.DefaultEditor )timeSpinner.getEditor();
		editor1.getTextField().setEditable( false );
		
		//number of moles selector
		SpinnerModel numMoles = new SpinnerNumberModel( WhackAMole.numMoles,
				0,
				Integer.MAX_VALUE,
				1);
		JLabel numMolesLabel = new JLabel("# moles: ");
		panel.add(numMolesLabel);
		JSpinner moleSpinner = new JSpinner(numMoles);
		JSpinner.DefaultEditor editor2 = ( JSpinner.DefaultEditor )moleSpinner.getEditor();
		editor2.getTextField().setEditable( false );
		
		//size of the board selector
		SpinnerModel boardIncrementer = new SpinnerNumberModel( WhackAMole.sizeOfGame,
				0,
				10,
				1);
		JLabel sizeOfBoardLabel = new JLabel("size x size");
		
		JSpinner boardIncrementerSpinner = new JSpinner(boardIncrementer);
		boardIncrementerSpinner.getEditor();
		JSpinner.DefaultEditor editor3 = ( JSpinner.DefaultEditor ) boardIncrementerSpinner.getEditor();
		editor3.getTextField().setEditable( false );
		//-------------------------------------------------------
		//Add the listeners to the frame closing
		//-------------------------------------------------------
		panel.add(timeLabel,0,0);
		panel.add(timeSpinner, 0, 1);
		panel.add(sizeOfBoardLabel, 1, 0);
		panel.add(boardIncrementerSpinner, 1, 1);
		panel.add(numMolesLabel, 2, 1);
		panel.add(moleSpinner, 2, 1);
		newGameFrame.add(panel, BorderLayout.CENTER);
		newGameFrame.add(closeButton, BorderLayout.SOUTH);
		//-------------------------------------------------------
		//Listens for the window closing
		//Then intializes variables in the MAIN gui prior
		//to repainting according to user input
		//-------------------------------------------------------
		newGameFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e){
				Integer numberOfMoles = (Integer)moleSpinner.getValue();
				Integer time = (Integer)timeSpinner.getValue();
				Integer size = (Integer)boardIncrementerSpinner.getValue();
				if(numberOfMoles > (sizeOfGame * sizeOfGame)){
					JOptionPane.showMessageDialog(newGameFrame, "decrease number of moles");
				}else{
					WhackAMole.count = time;
					WhackAMole.numMoles = size;
					WhackAMole.sizeOfGame = numberOfMoles;
					e.getWindow().dispose();
				}
			}
		});
		
		newGameFrame.pack();
		newGameFrame.setVisible(true);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 * Listens for the start button to be pressed and creates a thread to listen for
	 * when the users options panel is closed, then repaints the main GUI
	 * 
	 * Second, listens for any other button press
	 * Adds to the score if you pressed a mole
	 * Finally interrupts a thread which will release a semaphore because you
	 * "whacked a mole"
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		JButton pressed = (JButton)e.getSource();
		//get user input
		if (pressed == startButton) {
			startButton.setEnabled(false);
			promptUserInput();
			Thread waitForUserInput = new Thread(new WaitForWindow());
			waitForUserInput.start();
		}
		//did the user whack a mole?
		for (int i = 0; i < buttons.length; i++) {
			for(int j = 0; j < buttons[i].length; j++){
				if (pressed == buttons[i][j]) {
					//an up mole has an empty space.
					//a down mole has two empty spaces.
					//this is so that is doesn't interfere with the visual aspect
					if (buttons[i][j].getText().equals(" ")) {
						score++;
						scoreArea.setText("" + score);
						buttons[i][j].setText("  ");
						moleThreads[i][j].interrupt();
					}
				}
			}
		}
	}
	
	/*********************************************************************
	 * 
	 * @author Ben Benson
	 * When the user has completed their input
	 * The thread initialized the center Panel according to user input
	 * Along with other semaphore for moles, and the timer
	 * Once time has run out the thread sets time = false
	 * which will cause the mole's (Threads) to quit
	 ********************************************************************/
	private class TimerThread implements Runnable{
			@Override
			public void run() {
				WhackAMole.time = true;
				addButtons();
				moleThreads = new Thread[sizeOfGame][sizeOfGame];
				semaphore = new Semaphore(numMoles);
				for (int i = 0; i < moleThreads.length; i++) {
					for(int j = 0; j<moleThreads[i].length; j++){
						WhackAMole.moleThreads[i][j] = new Thread( new MoleThread(buttons[i][j]));
						WhackAMole.moleThreads[i][j].start();
					}
				}
				while (count > -1) {
					try {
						timeArea.setText(Integer.toString(count));
						count--;
						Thread.sleep(1000);
					} catch (InterruptedException ex) {
						ex.printStackTrace();
					}
				}
				JOptionPane.showMessageDialog(newGameFrame, "Times Up!");
				WhackAMole.time = false;
				WhackAMole.count = 20;
				WhackAMole.score = 0;
				WhackAMole.timeArea.setText("" + count);
				WhackAMole.scoreArea.setText("" + score);
				WhackAMole.startButton.setEnabled(true);
			}
	}
	
	/*****************************************************************
	 * @author Ben
	 * Waits for the user's input then creates another thread 
	 * called TimerThread that sets up the GUI components
	 *****************************************************************/
	private class WaitForWindow implements Runnable{
		@Override
		public void run() {
			while(true){
				//is the window closed?
				if(!newGameFrame.isVisible()){
					Thread timerThread = new Thread(new TimerThread());
					timerThread.start();
					return;
				//it's not closed sleep 300ms
				}else{
					try {
						//wait for the user
						Thread.sleep(300);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
	/*****************************************************************
	 * @author Ben
	 * Takes a reference to a button, so a Thread = Button and can
	 * control that button
	 * 
	 * A mole pops up every a random 4seconds - 1.5 seconds.
	 * If a user hits a mole, the thread releases a semaphore resource
	 * If the user has not and the mole "goes back down" then it releases
	 * a semaphore resource as well.
	 * 
	 * If there's not availible resources (semaphore) then sleep
	 * for a random amount of time. Wake up an check again.
	 *****************************************************************/
	private static class MoleThread implements Runnable {
		JButton button;

		MoleThread(JButton button) {
			this.button = button;
			button.setText("  ");
		}

		public void run() {
			while (time) {
				int randomSleepTime = (random.nextInt(4000) + 1500) % 4000;
				if(semaphore.tryAcquire() && count > -1){
					button.setIcon(new ImageIcon(moleImage));
					button.setText(" ");
					try {
						Thread.sleep(randomSleepTime);
						button.setText("  ");
						button.setIcon(null);
						semaphore.release();
					} catch (InterruptedException e) {
						button.setText("  ");
						button.setIcon(null);
						semaphore.release();
					}
				}
				try {
					Thread.sleep(randomSleepTime);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	//Run the game
	public static void main(String[] args) {
		new WhackAMole();
	}
}
