implicit def float2int(f:float):int = f.intValue
implicit def double2float(d:double):float = d.floatValue

def choice[T](a:Array[T]):T = 
        a(random(0,a.length).floor)

import java.awt._
import java.awt.geom._
class Turtle(x0:float,y0:float) {
        var pos:Point = new Point(x0,y0);
        var phi:float = 0;
        var parent:Turtle = null
        
        def this(p:Turtle) = {
                this(p.pos.x,p.pos.y)
                phi = p.phi
                parent = p
        }
	def shape(s: Shape) {
		g2d.draw(s)
	}

        def advance(len:float) = 
                new Point(
                        pos.x + len * Math.cos(phi)
                        ,pos.y + len * Math.sin(phi))
        def line(length:float):Unit = {
                val newPos = advance(length)
                shape(new Line2D.Float(pos,newPos))
                pos = newPos
                this
        }
	def move(length: Float): Unit = {
		pos = advance(length)
		this
	}
	def quad(length: Float)(ctrl: Turtle => Turtle): Unit = {
		val t2 = push
		val end = advance(length)
		val ctrlPos = ctrl(t2).pos
		shape(new QuadCurve2D.Float(pos.x, pos.y, ctrlPos.x, ctrlPos.y, end.x, end.y))
		pos = end
        }
        def rotate(grad:float) = {
                phi = phi + 2*Math.Pi*grad/360
                this
        }

        def pop:Turtle = parent
        def push:Turtle = new Turtle(this)
	def asPath: PathTurtle = new PathTurtle(this, new Path2D.Float)
}
class PathTurtle(t: Turtle, val path: Path2D.Float) extends Turtle(t.pos.x, t.pos.y) {
	phi = t.phi
	
	def this(p: PathTurtle) = {
                this(p, p.path)
        }
	
	override def shape(s: Shape) {
		path.append(s, false)
	}
	def stroke {
		g2d.draw(path)
	}
	def fill {
		g2d.fill(path)
	}
	override def push: Turtle = new PathTurtle(this)
}

def times(i: Int)(x: => Unit) {
	for (a <- 0 until i)
		x
}

def baum(depth:int,length:float,t:Turtle):Unit = 
        if (depth > 0 ){
                g2d.setStroke(new java.awt.BasicStroke(depth,1,1));

                hsb(0.6,0.7,0.4,depth.floatValue/10);
                t.line(length)
                //t.rotate(-15)
                val r = (10-depth).floatValue/10f*arg("winkel",0f,360f,70)
                baum(depth-1,length * arg("krumm",0f,1f,0.9f),t.rotate(random(-r,r)).push)
                baum(depth-1,length * arg("gerade",0f,1f,0.8f),t.push)
        }        

g2d.setPaint(new java.awt.RadialGradientPaint(width/2,height/2,width/2
        ,Array(0f,1f)
        ,Array(hsb(0.33,0.1,1,0.2),hsb(0.33,0.1,1,0.8))))

rect(0,0,width,height)

val w = arg("winkel", 0f, 180f, 45f)
val l = arg("length", 0f, 300f, 100f)
val l2 = arg("length2", 0f, 300f, 200f)
val staengel = arg("staengel", 0f, 100f, 0f)

val colorDev = arg("colorDev", 0.05f, 0.4f, 0.1)

val baseColor = arg("color", 0f, 1f, 0.33f)
val saturation = arg("saturation", 0f, 1f, 0.8)
val brightness = arg("brightness", 0f, 1f, 0.8)
def painter(color: Float) = Array(hsb(color,saturation,brightness,0.8+random(-0.25,0.25)),hsb(color,saturation*0.8,brightness*0.8,0.5))

def blatt(w: Float, color: Float, inputT: Turtle) = {
	hsb(0.33,0.8,0.5, 0.7)
	g2d.setStroke(new java.awt.BasicStroke(1f,1,1));
	inputT.line(staengel)
	val t = inputT.asPath
	val other = t.push
	val rl = l + random(-l/3f, + l/3f)
	val rl2 = l2 + random(-l2/8f, + l2/8f)
	t.quad(rl2) { t =>
		t.rotate(-w)
		t.move(rl)
		t
	}
	t.rotate(180f)
	t.quad(rl2) { t =>
		other.rotate(w)
		other.move(rl)
		other
	}

	val half = inputT.push
	half.move(l2/2)
	g2d.setPaint(new RadialGradientPaint(half.pos.x,half.pos.y,l2/2
	        ,Array(0f,1f)
        	,painter(color)))
	t.fill
	
	hsb(0.33,0.8,0.5, 0.7)
	g2d.setStroke(new java.awt.BasicStroke(1f,1,1));
	t.stroke
}

def blume(x: Float, colorAdjust: Float) {
val t = new Turtle(x,height);

t.rotate(-90)

times(20) {
  val bl = random(0,1) < 0.12
  if (bl) {
	val blT = t.push
	blT.rotate(-30)
	blatt(w, 0.33, blT.push)
	blT.rotate(60)
	blatt(w, 0.33, blT.push)
  }  
  hsb(0.33,0.7,0.4, 1);
  g2d.setStroke(new java.awt.BasicStroke(5,1,1));

  t.line(20f + random(0f,5f))
  t.rotate(random(-10, 10))
}

val count = arg("count", 1f, 100f, 10)
val winki = 360f / count
times(count) {
blatt(w, colorAdjust + baseColor + random(-colorDev,colorDev), t.push)
t.rotate(winki)
}

}

val blumen = arg("blumen", 0, 10, 3)

for (x <- 0 until width by (width/blumen))
  blume(x+width/blumen/2, random(0,0.3))

//baum(arg("Stufen",0f,10f,10f),arg("Stammlaenge",0f,200f,100f),t)

