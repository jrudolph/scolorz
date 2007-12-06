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

def kugeln = 
List.range(0,30).map (i=>{
        
        hsb(choice(Array(0.86,0.6,0.1))
                ,random(0.3,0.9)
                ,random(0.6,1.)
                ,0.6)
        
        var x = random(0,300)
        var y = random(0,300)
        val d = random(30,100)

        oval(x,y,d,d)
})

val t = new Turtle(200,400);
t.rotate(-90)

def baum(depth:int,length:float,t:Turtle):Unit = 
        if (depth > 0 ){
                hsb(0.6,0.7,0.4,depth.floatValue/8);
                t.line(length)
                t.rotate(-30)
                baum(depth-1,length * 0.8,t.rotate(random(10,30)).push)
                baum(depth-1,length * 0.8,t.rotate(random(14,30)).push)
        }        

baum(10,70,t)

//kugeln