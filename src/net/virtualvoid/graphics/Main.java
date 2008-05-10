package net.virtualvoid.graphics;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.virtualvoid.graphics.Painter.IEngine;
import scala.tools.nsc.Interpreter;
import scala.tools.nsc.Settings;


public class Main{
	/*static interface Paintable{
		void paint(Graphics2D g);
		void setRandom(Random r);
	}*/

	static interface Model{
		float value();
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

		final long []seed = new long[1];
		final IEngine[] engine=new IEngine[1];
		seed[0]=500;
		final Canvas c = new Canvas(){

			Random rand = new Random(seed[0]);

		    private int bufferWidth;
		    private int bufferHeight;
		    private Image bufferImage;
		    private Graphics bufferGraphics;
			@Override
			public void paint(Graphics g) {
				rand.setSeed(seed[0]);

				if (painter[0]!=null){

			        if(bufferWidth!=getSize().width ||
			        		bufferHeight!=getSize().height ||
			        		bufferImage==null || bufferGraphics==null)
			        	resetBuffer();

		            bufferGraphics.clearRect(0,0,bufferWidth,bufferHeight);

					painter[0].paint((Graphics2D) bufferGraphics,rand,bufferWidth,bufferHeight,engine[0]);

					g.drawImage(bufferImage,0,0,this);
				}
			}
			@Override
			public void update(Graphics g) {
				paint(g);
			}
		    private void resetBuffer(){
		        // always keep track of the image size

		        bufferWidth=getSize().width;
		        bufferHeight=getSize().height;

		        //    clean up the previous image

		        if(bufferGraphics!=null){
		            bufferGraphics.dispose();
		            bufferGraphics=null;
		        }
		        if(bufferImage!=null){
		            bufferImage.flush();
		            bufferImage=null;
		        }
		        System.gc();

		        //    create the new image with the size of the panel

		        bufferImage=createImage(bufferWidth,bufferHeight);
		        bufferGraphics=bufferImage.getGraphics();
		    }
		};
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));

		final JTextArea text = new JTextArea();
		text.setFont(new Font(Font.MONOSPACED,Font.PLAIN,10));

		JScrollPane scrPane = new JScrollPane(text);
		panel.add(scrPane);

		final JPanel vars = new JPanel();
		vars.setLayout(new GridLayout(5,2));
		engine[0] = new IEngine() {
			Map<String,Model> sliders = new HashMap<String, Model>();

			public float arg(String name, final float from, final float to, float value) {
				if (sliders.containsKey(name))
					return sliders.get(name).value();
				else{
					final JSlider slider = new JSlider(0,100,(int) ((value-from)/(to-from)*100f));
					vars.add(new JLabel(name));
					vars.add(slider);
					vars.doLayout();

					sliders.put(name,new Model() {
						@Override
						public float value() {
							return from + slider.getValue() * (to-from)/100;
						}
					});

					slider.addChangeListener(new ChangeListener() {
						@Override
						public void stateChanged(ChangeEvent e) {
							c.repaint();
						}
					});

					return value;
				}
			}

			public void clear(){
				sliders.clear();
				vars.removeAll();
			}
		};

		JButton button = new JButton("Execute");
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				engine[0].clear();

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

		panel.add(vars);

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
