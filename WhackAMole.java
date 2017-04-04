package package1;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Image;
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
	private final static Color DOWN_COLOR = new Color(156, 93, 82);
	private static int count = 20;
	private static int score;
	private static JButton startButton;
	private JButton[][] buttons;
	private JLabel timeLabel, scoreLabel;
	private static JTextArea timeArea;
	private static JTextArea scoreArea;
	private static Random random = new Random(); 
	private static Semaphore semaphore;
	private static Thread[][] moleThreads;
	private ThreadGroup group;
	private static int sizeOfGame = 8;
	private static int numMoles;
	private static boolean time = true;
	private JPanel centerPanel;
	private JPanel northPanel;
	private static Image moleImage;
	private static JFrame newGameFrame;

	public WhackAMole() {
		try{
			moleImage = ImageIO.read(getClass().getResource("./rsz_mole.jpg"));
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

	private static void promptUserInput(){
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
		//the amount of time selector
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
		SpinnerModel boardIncrementer = new SpinnerNumberModel( WhackAMole.numMoles,
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

	@Override
	public void actionPerformed(ActionEvent e) {
		JButton pressed = (JButton)e.getSource();
		if (pressed == startButton) {
			startButton.setEnabled(false);
			promptUserInput();
			Thread waitForUserInput = new Thread(new WaitForWindow());
			waitForUserInput.start();
		}
		for (int i = 0; i < buttons.length; i++) {
			for(int j = 0; j < buttons[i].length; j++){
				if (pressed == buttons[i][j]) {
					//an up mole has an empty space.
					//a down mole has two empty spaces.
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
	
	private class TimerThread implements Runnable{
			@Override
			public void run() {
				WhackAMole.time = true;
				addButtons();
				moleThreads = new Thread[sizeOfGame][sizeOfGame];
				semaphore = new Semaphore(numMoles);
				for (int i = 0; i < moleThreads.length; i++) {
					for(int j = 0; j<moleThreads[i].length; j++){
						WhackAMole.moleThreads[i][j] = new Thread(group, new MoleThread(buttons[i][j]));
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
	
	private class WaitForWindow implements Runnable{
		@Override
		public void run() {
			while(true){
				if(!newGameFrame.isVisible()){
					System.out.println("window closed");
					Thread timerThread = new Thread(new TimerThread());
					timerThread.start();
					return;
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
