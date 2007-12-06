package net.virtualvoid.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public abstract class Painter {
	Random rand;
	public void setRandom(Random r) {
		rand = r;
	}
	protected float random(double low,double high){
		return (float) (low + (high - low) * random());
	}
	protected double random(){
		return rand.nextDouble();
	}
	Color fromHSB(float h,float s,float b,float a){
		int rgb = Color.HSBtoRGB(h,s,b);
		Color c = new Color(rgb);
		float[] comps = c.getColorComponents(null);
		return new Color(comps[0],comps[1],comps[2],a);
	}
	protected Painter(){
		hints = new HashMap<RenderingHints.Key,Object>();
		hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	}

	protected abstract void paint();
	protected Graphics2D g2d;
	private Map<RenderingHints.Key,Object> hints;

	public void paint(Graphics2D g){
		g2d=g;

		g2d.addRenderingHints(hints);

		paint();
	}

	protected void rgb(float r,float g,float b){
		g2d.setColor(new Color(r,g,b));
	}
	protected void rgb(float r,float g,float b,float a){
		g2d.setColor(new Color(r,g,b,a));
	}
	protected void hsb(float h,float s,float b,float a){
		g2d.setColor(fromHSB(h,s,b,a));
	}
	protected void oval(int x,int y,int width,int height){
		g2d.fillOval(x, y, width, height);
	}
	protected <T> T choice(T[] ar){
		return ar[(int)random(0,ar.length)];
	}
}
