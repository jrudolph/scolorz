implicit def float2int(f:float):int = f.intValue
implicit def double2float(d:double):float = d.floatValue

def choice[T](a:Array[T]):T = 
        a(random(0,a.length).floor)

class Turtle (x0:float,y0:float){
        import java.awt._
        import java.awt.geom._

        var pos:Point = new Point(x0,y0);
        var phi:float = 0;
        var parent:Turtle = null
        
        def this(p:Turtle) = {
                this(p.pos.x,p.pos.y)
                phi = p.phi
                parent = p
        }

        def advance(len:float) = 
                new Point(
                        pos.x + len * Math.cos(phi)
                        ,pos.y + len * Math.sin(phi))
        def line(length:float):Unit = {
                val newPos = advance(length)
                g2d.draw(new Line2D.Float(pos,newPos))
                pos = newPos
                this
        }
        def rotate(grad:float) = {
                phi = phi + 2*Math.Pi*grad/360
                this
        }

        def pop:Turtle = parent
        def push:Turtle = new Turtle(this)
}

def kugeln = {
def v(w:int) = (w%360.f)/360.f
val hs:float = random(0,90)
val colors = Array(hs,hs+180,hs+175,hs+185,hs+5,hs-5).map(v(_))
//val colors = Array(hs,hs-10,hs-5,hs+10,hs+5).map(v(_))
List.range(0,30).map (i=>{        
        //val h = choice(Array(0.86,0.6,0.1))

        val h = choice(colors)
        val s = random(0.3,0.9)
        val b = random(0.6,1.)
        
        var x = random(0,width-100)
        var y = random(0,height-100)
        val r = random(15,50)

        val grad = new java.awt.RadialGradientPaint(x+r,y+r,r
                ,Array(0f,1f)
                ,Array(hsb(h,s,b,0.9),hsb(h,s,b,0.7)))

        g2d.setPaint(grad)

        oval(x,y,2*r,2*r)
})}

val t = new Turtle(width/2,height);
t.rotate(-90)

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

//kugeln

baum(arg("Stufen",0f,10f,10f),arg("Stammlaenge",0f,200f,100f),t)
