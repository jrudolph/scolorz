package net.virtualvoid.graphics;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Random;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import scala.tools.nsc.Interpreter;
import scala.tools.nsc.Settings;


public class Main {
	static interface Paintable{
		void paint(Graphics2D g);
		void setRandom(Random r);
	}

	public static void main(String[] args) {
		JFrame p=new JFrame();

		p.setVisible(true);
		p.addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		p.setBackground(Color.white);

		p.setSize(600,400);

		final Interpreter interpreter = new Interpreter(new Settings());
		final Painter[] painter = new Painter[1];

		interpreter.bind("painter", "Array[net.virtualvoid.graphics.Painter]", painter);
		//Object res = interpreter.interpret("val i = blub;");

		/*final Painter paintable = new Painter() {
			@Override
			public void paint(Graphics2D g) {
				Graphics2D g2d=g;
				Map<RenderingHints.Key,Object> hints=new HashMap<RenderingHints.Key,Object>();
				hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				g2d.addRenderingHints(hints);

				final int COUNT = 20;

				for (int i=0;i<COUNT;i++){
					float s,b;
					Color c2 = fromHSB(0.4f, s = random(0.3,1), b = random(0.5,1),0.9f);

					g.setColor(c2);

					int d=(int)random(10,60);

					int x = (int) (200 + Math.cos(2.*Math.PI*i/COUNT)*100);
					int y = (int) (200 + Math.sin(2.*Math.PI*i/COUNT)*100);

					g.fillOval(x-d/2,y-d/2
							, d,d);
				}
			}
		};*/

		final long []seed = new long[1];
		seed[0]=500;
		final Canvas c = new Canvas(){

			Random rand = new Random(seed[0]);
			@Override
			public void paint(Graphics g) {
				rand.setSeed(seed[0]);

				if (painter[0]!=null){
					painter[0].setRandom(rand);
					painter[0].paint((Graphics2D) g);
				}
			}
		};
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));

		final JTextArea text = new JTextArea();
		text.setFont(new Font(Font.MONOSPACED,Font.PLAIN,10));

		JScrollPane scrPane = new JScrollPane(text);
		panel.add(scrPane);

		JButton button = new JButton("Execute");
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				//seed[0] = System.nanoTime();
				String code = "object aPainter extends net.virtualvoid.graphics.Painter{\n"
						+ "def paint:Unit = {\n"
						+ text.getText() + "\n}\n"
						+ "}\n";
				interpreter.interpret(code);
				interpreter.interpret("painter(0) = aPainter;");

				c.repaint();
			}
		});
		panel.add(button);
		JButton button2 = new JButton("New Seed");
		button2.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				seed[0] = System.nanoTime();
				c.repaint();
			}
		});
		panel.add(button2);

		JSplitPane splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,c,panel);

		c.setMinimumSize(new Dimension(50,50));
		c.setSize(400,400);
		c.setBackground(Color.white);

		text.setMinimumSize(new Dimension(200,50));
		splitter.setDividerLocation(400);

		p.add(splitter);
		p.setVisible(true);
	}
}
