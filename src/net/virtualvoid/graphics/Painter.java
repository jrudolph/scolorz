package net.virtualvoid.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public abstract class Painter {
	static interface IEngine{
		float arg(String name,float from,float to,float value);

		void clear();
	}

	Random rand;

	protected float random(double low,double high){
		return (float) (low + (high - low) * random());
	}
	protected double random(){
		return rand.nextDouble();
	}
	Color fromHSB(float h,float s,float b,float a){
		assert h <= 1.0f : String.format("Hue: %d > 1.0", h);
		assert s <= 1.0f : String.format("Saturation: %d > 1.0", s);
		assert b <= 1.0f : String.format("Brightness: %d > 1.0", b);
		assert a <= 1.0f : String.format("Alpha: %d > 1.0", a);

		int rgb = Color.HSBtoRGB(h,s,b);
		return new Color((rgb&0xffffff) | ((int)(a*255))<<24,true);
	}
	protected Painter(){
		hints = new HashMap<RenderingHints.Key,Object>();
		hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	}

	protected abstract void paint();
	protected Graphics2D g2d;
	private final Map<RenderingHints.Key,Object> hints;

	public int height;
	public int width;

	protected IEngine engine;

	public void paint(Graphics2D g,Random random,int width,int height,IEngine e){
		g2d=g;
		rand=random;
		this.height=height;
		this.width=width;
		this.engine = e;

		g2d.addRenderingHints(hints);

		paint();
	}

	protected void rgb(float r,float g,float b){
		g2d.setColor(new Color(r,g,b));
	}
	protected void rgb(float r,float g,float b,float a){
		g2d.setColor(new Color(r,g,b,a));
	}
	protected Color hsb(float h,float s,float b,float a){
		Color color = fromHSB(h,s,b,a);
		g2d.setColor(color);
		return color;
	}
	protected void color(Color c){
		g2d.setColor(c);
	}
	protected void oval(int x,int y,int width,int height){
		g2d.fillOval(x, y, width, height);
	}
	protected void rect(int x,int y,int width,int height){
		g2d.fillRect(x, y, width, height);
	}
	protected <T> T choice(T[] ar){
		return ar[(int)random(0,ar.length)];
	}
	protected float arg(String name,float from,float to,float value){
		return engine.arg(name, from, to, value);
	}
}
