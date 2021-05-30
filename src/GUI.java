import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class GUI extends JFrame implements ActionListener, MouseListener {
	public static final String FILE = "input/words.txt";
	private static String[] words;

	// Jframe height, weight
	private static final int WIDTH = 600;
	private static final int HEIGHT = 600;

	// File menu
	private static final String PLAY_START = "Play";
	public static final String PLAY_AGAIN = "Play Again";
	private static final String PLAY_EXIT = "Exit";

	// Initial : 초기 실행
	// Started : 게임이 시작됨
	// Exited : 사용자에 의해 종료됨
	private String state = "Initial";

	private int defeatedCount = 0;
	private int winnerCount = 0;

	// Random genator for word array
	public Random rGen = new Random();

	public static String wordAnswerStr;
	private static char[] wordAnswer;
	private static char[] wordGuess;

	public static int failedCount = 0;
	private static String guessList = "";

	// all my panels - mainpanel holds left/right/bottom(keyboard)
	public static JPanel mainPanel, leftPanel, rightPanel, bottomPanel, belowPanel;

	public GUI() {
		// 게임 제목
		super("Hang Man");

		// set size of the jframe
		setSize(WIDTH, HEIGHT);
		// populate word array
		words = getWordsFromFile();
		// close Jframe on exit
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// main panel houses three panels - left, right and bottom(keyboard).
		mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(3, 0));
		mainPanel.setBackground(Color.WHITE);

		rightPanel = new JPanel();
		leftPanel = new JPanel();
		leftPanel.setBackground(Color.WHITE);
		rightPanel.setBackground(Color.WHITE);
		// add the left/right panel
		mainPanel.add(leftPanel);
		mainPanel.add(rightPanel);
		bottomPanel = new JPanel();
		bottomPanel.setLayout(new GridLayout(4, 4));
		bottomPanel.setBackground(Color.GRAY);
		// add the bottom panel which contains Jbuttons
		mainPanel.add(bottomPanel, BorderLayout.SOUTH);

		belowPanel = new JPanel();
		belowPanel.setBackground(Color.GREEN);
		// add last panel which houses replay/exit button
		add(mainPanel);
		add(belowPanel, BorderLayout.AFTER_LAST_LINE);

		// set visibility to false until game is over
		belowPanel.setVisible(false);

		createMenuBar();
		createButtons(bottomPanel);
		// create replay/exit buttons
		replayButtons(belowPanel);
		addMouseListener(this);
	}

	// method creates two jbutton for replay/exit and adds actionlisteners
	public void replayButtons(JPanel belowPanel) {
		JButton playAgain = new JButton(PLAY_AGAIN);
		playAgain.setSize(80, 80);
		playAgain.setActionCommand(PLAY_AGAIN);
		playAgain.addActionListener(this);
		JButton exit = new JButton(PLAY_EXIT);
		exit.setActionCommand(PLAY_EXIT);
		exit.addActionListener(this);
		exit.setSize(80, 80);
		belowPanel.add(playAgain);
		belowPanel.add(exit);
	}

	// method creates an array of jbuttons with actionlisteners to use as a
	// keyboard
	public void createButtons(JPanel bottomPanel) {

		JButton[] buttons = new JButton[26];
		String[] inputLetter = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q",
				"R", "S", "T", "U", "V", "W", "X", "Y", "Z" };

		for (int i = 0; i < buttons.length; i++) {
			buttons[i] = new JButton(inputLetter[i]);
			buttons[i].setSize(40, 40);
			buttons[i].setActionCommand(inputLetter[i]);
			buttons[i].addActionListener(this);

			bottomPanel.add(buttons[i]);
		}

	}

	// method creates menu and menuitems
	public void createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		// create file menu
		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);

		// add menu items
		createMenuItem(fileMenu, PLAY_START);
		createMenuItem(fileMenu, PLAY_EXIT);

	}

	// method creates menu items with action listeners
	public void createMenuItem(JMenu menu, String itemName) {
		JMenuItem menuItem = new JMenuItem(itemName);
		menuItem.addActionListener(this);
		menu.add(menuItem);
	}

	private void hangman(Graphics g) {

		if (failedCount < 5) {
			// draw face
			g.setColor(Color.YELLOW);
			g.fillOval(35, 120, 70, 60);
			// hat
			g.setColor(Color.RED);

			g.fillRect(48, 90, 48, 30);
			g.fillRect(30, 120, 80, 15);

			// draw eyes
			g.setColor(Color.GREEN);
			g.fillOval(55, 140, 10, 10);
			g.fillOval(75, 140, 10, 10);

			// smile
			g.setColor(Color.RED);
			g.drawArc(50, 155, 40, 10, -10, -180);
		}

		if (failedCount < 4) {
			// body
			g.setColor(Color.GREEN);
			g.fillRect(60, 180, 20, 80);

			// left arm
			g.setColor(Color.GREEN);
			g.fillRect(25, 200, 45, 15);
			g.setColor(Color.YELLOW);
			g.fillRect(15, 200, 10, 15);
		}

		if (failedCount < 3) {
			// right arm
			g.setColor(Color.GREEN);
			g.fillRect(80, 200, 45, 15);
			g.setColor(Color.YELLOW);
			g.fillRect(120, 200, 10, 15);
		}

		if (failedCount < 2) {
			// left foot
			g.setColor(Color.BLACK);
			g.fillRect(35, 260, 30, 15);
		}

		if (failedCount < 1) {
			// right foot
			g.setColor(Color.BLACK);
			g.fillRect(70, 260, 30, 15);
		}

		g.setColor(Color.BLACK);
	}

	private void gameMessages(Graphics g) {
		if (!isWinner()) {
			g.drawString("Hang Man!!!", 25, 80);
			g.drawString("승리 횟수 : " + winnerCount + " / 10", 300, 80);
			g.drawString("패배 횟수 : " + defeatedCount + " / 3", 300, 100);
			return;
		}
	}

	public boolean isWinner() {
		return Arrays.equals(wordGuess, wordAnswer);
	}

	public String getword() {
		words = getWordsFromFile();

		int n = words.length;
		int r = rGen.nextInt(n);
		String word = words[r];

		return word;
	}

	public String[] getWordsFromFile() {
		BufferedReader reader = null;
		List<String> wordList = new ArrayList<String>();

		try {
			reader = new BufferedReader(new FileReader(FILE));
			String s = null;
			while ((s = reader.readLine()) != null) {
				if (s.length() > 2 && Pattern.matches("^[a-zA-Z]*$", s)) {
					wordList.add(s.toLowerCase());
				}
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
			System.exit(-1);
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				System.out.println(e.getMessage());
				System.exit(-1);
			}
		}
		return wordList.toArray(new String[wordList.size()]);
	}

	public void inputLetter(String letter) {
		System.out.println("Input Letter : " + letter);
		letter = letter.toLowerCase();
		char letterChar = letter.charAt(0);

		if (!guessList.contains(letter)) {
			if (wordAnswerStr.contains(letter)) {
				for (int i = 0; i < wordAnswer.length; i++) {
					if (letterChar == wordAnswer[i]) {
						wordGuess[i] = letterChar;
					}
				}
			}

			if (!wordAnswerStr.contains(letter)) {
				JOptionPane.showMessageDialog(null, letter + " is not exist");
				failedCount++;
			}

			guessList += letter;
			if (failedCount < 6 && !isWinner()) {
				guessList += ",";
			}

			repaint();

			result();
		}
	}

	private void play() {
		wordAnswerStr = getword();
		wordAnswer = wordAnswerStr.toCharArray();
		wordGuess = new char[wordAnswer.length];

		int wordShowCount = 0;
		int wordShowMax = (int) (Math.floor(wordAnswer.length * 3 / 10));

		for (int i = 0; i < wordGuess.length; i++) {
			wordGuess[i] = '_';
			if (Math.random() < 0.3 && wordShowCount < wordShowMax) {
				wordGuess[i] = wordAnswer[i];
				wordShowCount++;
			}
		}
	}

	private void exit() {
		state = "Exited";
		System.exit(-1);
	}

	private void playReset() {
		failedCount = 0;
		guessList = "";
		bottomPanel.setVisible(true);
		state = "Started";
		play();
		repaint();
	}

	private void result() {
		if (isWinner()) {
			JOptionPane.showMessageDialog(null, "Correct!!");
			winnerCount++;
			playReset();
		}

		if (failedCount >= 5) {
			JOptionPane.showMessageDialog(null, "Fail!!");
			defeatedCount++;
			playReset();
		}

		if (defeatedCount >= 3) {
			JOptionPane.showMessageDialog(null, "You Lose");
			exit();
		}

		if (winnerCount >= 10) {
			JOptionPane.showMessageDialog(null, "You Win");
			exit();
		}
	}

	public void paint(Graphics g) {
		super.paint(g);

		// set the font
		Font font = new Font("Serif", Font.BOLD | Font.ITALIC, 24);
		g.setFont(font);
		g.setColor(Color.RED);

		// if user has selected play from menu - start game
		if (state == "Started") {
			String result = "";
			for (int i = 0; i < wordGuess.length; i++) {
				result += wordGuess[i] + " ";
			}

			g.drawString(result, 300, 175);
			g.drawString("GUESSES", 300, 300);
			g.drawString(guessList, 300, 350);

			System.out.println("Answer Word : " + wordAnswerStr);

			hangman(g);
			gameMessages(g);
		}
	}

	// 액션이 발생할 때 호출됩니다.
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();

		if (command.equals(PLAY_START)) {
			state = "Started";
			play();
			repaint();
			return;
		}

		Boolean isClickedLetter = command.length() == 1 && state == "Started";
		if (isClickedLetter) {
			inputLetter(command);
			repaint();
			return;
		}

		if (command.equals(PLAY_AGAIN)) {
			failedCount = 0;
			guessList = "";
			bottomPanel.setVisible(true);
			state = "Started";
			play();
			repaint();
			return;
		}

		if (command.equals(PLAY_EXIT)) {
			state = "Exited";
			System.exit(-1);
			return;
		}

	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	public static void main(String[] args) {
		GUI hangman = new GUI();
		hangman.setVisible(true);
		hangman.state = "Started";
		hangman.play();
		hangman.repaint();
	}
}
