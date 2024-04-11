/*
 The contents of this file are subject to the Mozilla Public License
 Version 1.1 (the "License"); you may not use this file except in
 compliance with the License. You may obtain a copy of the License at
 http://www.mozilla.org/MPL/

 Software distributed under the License is distributed on an "AS IS"
 basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 License for the specific language governing rights and limitations
 under the License.

 The Initial Developer of the Original Code is Johannes Rudolph.
 Portions created by the Initial Developer are Copyright (C) 2008
 the Initial Developer. All Rights Reserved.

 Contributor(s):
    Johannes Rudolph <johannes_rudolph@gmx.de>
*/

package net.virtualvoid.graphics;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.virtualvoid.graphics.Painter.IEngine;
import scala.collection.immutable.List;
import scala.collection.immutable.Nil$;
import scala.tools.nsc.Interpreter;
import scala.tools.nsc.Settings;


public class Main{
	private static final String EXAMPLE_SCRIPT = "Tree.script";

	private static void error(String format,Object...args){
		System.err.println(String.format(format,args));
	}

	static interface Model{
		float value();
	}

	public static void main(String[] args) {
		JFrame p=new JFrame();


		p.addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		p.setBackground(Color.white);

		p.setSize(700,700);

		final Settings settings = new Settings();
		settings.usejavacp().tryToSet(null);
		final Interpreter interpreter = new Interpreter(settings);
		final Painter[] painter = new Painter[1];

		interpreter.bind("painter", "Array[net.virtualvoid.graphics.Painter]", painter, List.empty());

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
		text.setFont(new Font(Font.MONOSPACED,Font.PLAIN,20));
		text.setText(readWholeFile(EXAMPLE_SCRIPT));
		text.setBorder(new TitledBorder("Code"));

		JScrollPane scrPane = new JScrollPane(text);
		panel.add(scrPane);

		final JPanel vars = new JPanel();
		vars.setLayout(new GridLayout(5,2));
		vars.setBorder(new TitledBorder("Variables"));
		vars.setMinimumSize(new Dimension(200,300));
		engine[0] = new IEngine() {
			Map<String,Model> sliders = new HashMap<String, Model>();

			NumberFormat nf = NumberFormat.getNumberInstance();

			public Set<Map.Entry<String, Model>> argValues() {
			    return sliders.entrySet();
			}

			public float arg(String name, final float from, final float to, float value) {
				if (sliders.containsKey(name))
					return sliders.get(name).value();
				else{
					final JSlider slider = new JSlider(0,100,(int) ((value-from)/(to-from)*100f));
					vars.add(new JLabel(name));
					vars.add(slider);
					final JLabel text = new JLabel(nf.format(value));
					vars.add(text);
					vars.doLayout();

					final Model model = new Model() {
						@Override
						public float value() {
							return from + slider.getValue() * (to-from)/100;
						}
					};
					sliders.put(name,model);

					slider.addChangeListener(new ChangeListener() {
						@Override
						public void stateChanged(ChangeEvent e) {
							text.setText(nf.format(model.value()));
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
		MouseAdapter executeListener = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				engine[0].clear();

				String code = "object aPainter extends net.virtualvoid.graphics.Painter{\n"
						+ "def paint:Unit = {\n"
						+ text.getText() + "\n}\n"
						+ "}\n";
				interpreter.interpret(code);
				interpreter.interpret("painter(0) = aPainter;");

				c.repaint();
			}
		};
		button.addMouseListener(executeListener);
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
		final JTextField fileName = new JTextField();
		JButton saveButton = new JButton("Save");
		saveButton.addMouseListener(new MouseAdapter() {
		    private File getFile(String ext) {
    		    return new File("out/"+fileName.getText()+"."+ext);
		    }
    		@Override
			public void mouseClicked(MouseEvent e) {
			    try {
			        File f = getFile("png");
			        if (f.exists())
			            JOptionPane.showMessageDialog(null, "Dieser Dateiname ist schon vorhanden, bitte gib einen anderen Dateinamen an.", "Dateiname schon vergeben", JOptionPane.ERROR_MESSAGE);
			        else {
				        BufferedImage i = new BufferedImage(c.getSize().width, c.getSize().height, BufferedImage.TYPE_INT_ARGB);
				        Graphics g = i.getGraphics();
				        c.paint(g);
				        g.dispose();
				        ImageIO.write(i, "png", f);

				        new FileOutputStream(getFile("txt")).write(text.getText().getBytes());

				        PrintWriter pw = new PrintWriter(getFile("vars.txt"));
				        String nameVal = "%s = %s\n";
				        pw.printf(nameVal, "seed", seed[0]);

				        for (Map.Entry<String, Model> kv: engine[0].argValues())
				            pw.printf(nameVal, kv.getKey(), kv.getValue().value());
				        pw.close();
				    }
		        } catch(IOException ex) { throw new RuntimeException(ex); }
			}
		});
		panel.add(fileName);
		panel.add(saveButton);

		panel.add(vars);

		JSplitPane splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,c,panel);

		c.setMinimumSize(new Dimension(50,50));
		c.setSize(400,400);
		c.setBackground(Color.white);

		text.setMinimumSize(new Dimension(200,50));
		splitter.setDividerLocation(400);

		p.add(splitter);

		p.setVisible(true);

		executeListener.mouseClicked(null);

		p.setVisible(true);
	}
	private static String readWholeFile(String filename){
		int length = (int)new File(filename).length();
		char []buffer = new char[length];
		try {
			new InputStreamReader(new FileInputStream(filename)).read(buffer,0,length);
			return new String(buffer);
		} catch (FileNotFoundException e) {
			error("Couldn't find '%s'",filename);
			return "";
		} catch (IOException e) {
			error("Error while reading '%s'",filename);
			e.printStackTrace();
			return "";
		}
	}
}
