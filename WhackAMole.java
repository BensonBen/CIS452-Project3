package package1;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
import java.util.concurrent.Semaphore;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class WhackAMole implements ActionListener {
	
	private final static Color DOWN_COLOR = new Color(156, 93, 82);
	private static int count = 20;
	private static int score;
	private JButton startButton;
	private JButton[][] buttons;
	private JLabel timeLabel, scoreLabel;
	private JTextArea timeArea;
	private JTextArea scoreArea;
	private static Random random = new Random(); 
	private static Semaphore semaphore = new Semaphore(3);
	private Thread[][] moleThreads;
	private ThreadGroup group;
	private int sizeOfGame = 8;
	private static boolean time = true;
	private JPanel centerPanel;
	private JPanel northPanel;
	private static Image moleImage;

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
		for(int i = 0; i<sizeOfGame; i++){
			for(int j = 0; j<sizeOfGame; j++){
				buttons[i][j].setOpaque(true);
				buttons[i][j].setBackground(DOWN_COLOR);
				buttons[i][j].addActionListener(this);
				centerPanel.add(buttons[i][j]);
			}
		}
	}

	private static class MoleThread implements Runnable {
		JButton button;

		MoleThread(JButton button) {
			this.button = button;
		}

		public void run() {
			while (time) {
				int randomSleepTime = (random.nextInt(4000) + 1000) % 4000;
				if(semaphore.tryAcquire() && count > -1){
					button.setIcon(new ImageIcon(moleImage));
					try {
						Thread.sleep(randomSleepTime);
						button.setIcon(null);
						semaphore.release();
					} catch (InterruptedException e) {
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

	@Override
	public void actionPerformed(ActionEvent e) {
		JButton pressed = (JButton)e.getSource();
		if (pressed == startButton) {
			startButton.setEnabled(false);
			Thread timerThread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					time = true;
					while (count > -1) {
						try {
							timeArea.setText(Integer.toString(count));
							count--;
							Thread.sleep(1000);
						} catch (InterruptedException ex) {
							ex.printStackTrace();
						}
					}

					time = false;
					count = 20;
					score = 0;
					timeArea.setText("" + count);
					scoreArea.setText("" + score);
					startButton.setEnabled(true);
				}
			});
			timerThread.start();
			moleThreads = new Thread[sizeOfGame][sizeOfGame];

			for (int i = 0; i < moleThreads.length; i++) {
				for(int j = 0; j<moleThreads[i].length; j++){
					this.moleThreads[i][j] = new Thread(group, new MoleThread(buttons[i][j]));
					this.moleThreads[i][j].start();
				}
			}
		}
		for (int i = 0; i < buttons.length; i++) {
			for(int j = 0; j < buttons.length; j++){
				if (e.getSource() == buttons[i][j]) {
					if (buttons[i][j].getText().equals("j")) {
						score++;
						scoreArea.setText("" + score);
						buttons[i][j].setText("j");
						moleThreads[i][j].interrupt();
					}
				}
			}
		}
	}

	/*
	 * Runs the game itself
	 * *
	 */
	public static void main(String[] args) {
		new WhackAMole();
	}
}