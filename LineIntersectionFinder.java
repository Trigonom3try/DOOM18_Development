
/*  It's the damnedest thing (the sort of thing you realize sitting awake at 2am) : you can use the exact same findIntersection logic for finding
    the intersection of a line and plane in 3d, for two lines in 2d, because in 2d the vector forms of the equations of the line and plane both simply 
    become equations for lines. There's might be more code here than for using y = mx + b, but it's easier to understand code and it works handily. I had 
    actually figured out findIntersection for 3d a lot sooner than I worked this out, and I had always assumed I'd be working with y = mx + b for 2d lines.
    Turns out this is even easier than y = mx + b, which I find somewhat ironic.

    There are two different versions of the LineIntersectionFinder app, one that uses a single point for the point buffer, and one that uses an 
    ArrayList of points that gets sorted such that the closest to the "camera" (here, the origin of the space which is at (250,250) on the JPanel )
    gets rendered. Ultimately, the DOOM18 program will need to use an ArrayList, because it will be dealing with both enemy sprites and walls of 
    different heights; however, I used the single point logic because of the simple fact that in this context, that's all you really need, because
    each time you find an intersection that lies within the endpoints of a line segment you can check the distance of that point against what's in the
    point buffer, and if it's closer to the origin, then you simply replace the pointBuffer point. I always want to keep things simple for my programs,
    such that they only have what they need, no more and no less, both for efficiency and for elegance.  

    See : DOOM18 - Line Intersection in 2D.txt, Kermo - The Intersection of a Line and a Plane.txt
    
    Click on two points on the JPanel to instantiate a line segment
*/

package DOOM18_Dev_0;
import java.util.*;
import java.math.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class LineIntersectionFinder
{
    static enum AXIS { Xpos, Xneg, Ypos, Yneg }
    static Vector VictorTheVector = new Vector(AXIS.Xpos);      // Guess who's back and ready to find some intersections
    static ViewPanel vp = new ViewPanel();
    static ButtonPanel bp = new ButtonPanel();
    static ArrayList<LineSegment> LineSegments = new ArrayList<LineSegment>();
    static Point pointBuffer = null;
    static Point origin = new Point(250,250);
    
    static Point L1;    
    static boolean hasL1 = false;
    
    public static void main(String [] args)
    {
        JFrame window = new JFrame();
        window.setSize(500,600);
        window.setResizable(false);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.add(vp);
        window.add(bp, BorderLayout.SOUTH);
        window.setVisible(true);
    }
    
    
    
    static class ViewPanel extends JPanel
    {
        ViewPanel()
        {
            addMouseListener(new LineSegmentCreator());
        }
        public void paintComponent(Graphics g)
        {
            g.setColor(Color.white);
            g.fillRect(0, 0, 500, 500);        
            
            int VictorX = toInt( VictorTheVector.Xcomponent );
            int VictorY = toInt ( VictorTheVector.Ycomponent );
            
            g.setColor(Color.red);
            g.drawLine(250, 250, 250 + VictorX, 250 + VictorY);     
            
            g.setColor(Color.blue);
            for (LineSegment l : LineSegments)
                g.drawLine(l.endpoint1.X, l.endpoint1.Y, l.endpoint2.X, l.endpoint2.Y);
            
            g.setColor(Color.black);
            if (pointBuffer != null)
                g.fillOval(pointBuffer.X, pointBuffer.Y, 5,5);
        }
    }
    
    
    static class ButtonPanel extends JPanel 
    {
        static JButton Left = new JButton("<");
        static JButton Right = new JButton(">");
        
        ButtonPanel()
        {
            Left.addActionListener(new ButtonListener());
            Right.addActionListener(new ButtonListener());
            add(Left);
            add(Right);
        }
    }
    
    static class ButtonListener implements ActionListener
    {
        public void actionPerformed (ActionEvent e)
        {
            pointBuffer = null;
            
            char c = ((JButton)e.getSource()).getText().charAt(0);
            if (c == '<') VictorTheVector.Rotate(-Math.PI / 20);    
            else VictorTheVector.Rotate(Math.PI / 20);          
            
            for (LineSegment l : LineSegments)
                findIntersection(VictorTheVector, l);
            
            vp.repaint();
        }
    }
    
    static class LineSegmentCreator implements MouseListener
    {
        public void mouseClicked(MouseEvent e)
        {
            int X = e.getX();   int Y = e.getY();
            if (!hasL1) 
            {
                L1 = new Point(X, Y);
                hasL1 = true;
            }
            else 
            {
                LineSegments.add(new LineSegment(L1, new Point(X,Y), Color.black));
                hasL1 = false;
                vp.repaint();
            }
            
        }
        public void mousePressed(MouseEvent e)  {}
        public void mouseReleased(MouseEvent e) {}
        public void mouseEntered(MouseEvent e)  {}
        public void mouseExited(MouseEvent e)   {}
    }
    
    
    
    
    
    static class Point
    {
        int X; int Y;
        Point(int x, int y)
        {
            X = x; Y = y;
        }
        Point(Vector v)
        {
            X = toInt(v.Xcomponent);
            Y = toInt(v.Ycomponent);
        }
        void add(int x, int y)
        {
            X += x; Y += y;
        }
        public String toString()
        {
            return "\tPrinting point ...\n\t\tX : " + X + " , Y : " + Y;
        }
    }
    
    
    static class LineSegment
    {
        Vector slope;
        Vector normal;      // the normal is a vector orthogonal to the slope of the line segment in either direction, necessary for findIntersection
        Point endpoint1;
        Point endpoint2;
        Color color;
        
        LineSegment(Point p0, Point p1, Color c)
        {
            endpoint1 = p0; endpoint2 = p1; slope = new Vector(p0, p1);
            normal = new Vector(-slope.Ycomponent, slope.Xcomponent);   // negative reciprocal slope *
            normal.changeMagnitude(1);
            color = c;
        }
    }
    
    static class Vector
    {
        double Xcomponent;       
        double Ycomponent;
        double magnitude;        
        
        Vector(AXIS startingAxis)     
                                     
        {
            magnitude = 1;
            switch (startingAxis) {
                case Xpos : { Xcomponent = 1; Ycomponent = 0; break; }
                case Xneg : { Xcomponent = -1; Ycomponent = 0; break; } 
                case Ypos : { Xcomponent = 0; Ycomponent = 1; break; }
                case Yneg : { Xcomponent = 0; Ycomponent = -1; break; }
                default : break;
            }
        }
        Vector(double Xcomp, double Ycomp)
        {
            Xcomponent = Xcomp;
            Ycomponent = Ycomp;
            magnitude = distance(Xcomponent, Ycomponent);
        }
        Vector(Point p)
        {
            Xcomponent = toDouble(p.X);  Ycomponent = toDouble(p.Y);
            magnitude = distance(Xcomponent, Ycomponent);
        }
        Vector(Point p0, Point p1)
        {
            Xcomponent = toDouble(p1.X - p0.X);
            Ycomponent = toDouble(p1.Y - p0.Y);
            magnitude = distance(Xcomponent, Ycomponent);
        }
        Vector(Vector v)
        {
            Xcomponent = v.Xcomponent; Ycomponent = v.Ycomponent; magnitude = v.magnitude;
        }
        void Rotate(double rotation)
        {
            double Xtemp = Xcomponent;
            Xcomponent = Xcomponent*Math.cos(rotation) - Ycomponent*Math.sin(rotation);  
            Ycomponent = Xtemp*Math.sin(rotation) + Ycomponent*Math.cos(rotation);  
            double newMag = distance(Xcomponent, Ycomponent);
            
            Xcomponent = Xcomponent * (magnitude/newMag);   
            Ycomponent = Ycomponent * (magnitude/newMag);   
        }
        
        void changeMagnitude(double newMag)
        {
            Xcomponent = Xcomponent * (newMag / magnitude);
            Ycomponent = Ycomponent * (newMag / magnitude);
            magnitude = newMag;
        }
        
        static double dotProduct(Vector v1, Vector v2)
        {
            return v1.Xcomponent*v2.Xcomponent + v1.Ycomponent*v2.Ycomponent;
        }
        
        static Vector scalarMultiple(Vector v, double scalar)
        {
            return new Vector(v.Xcomponent * scalar, v.Ycomponent * scalar);
        }
        static Vector sum (Vector v1, Vector v2)
        {
            return new Vector(v1.Xcomponent + v2.Xcomponent, v1.Ycomponent + v2.Ycomponent);
        }
        
        public String toString()
        {
            String s = "Printing Vector ...";
            s += "\n Xcomponent : " + Xcomponent;
            s += "\n Ycomponent : " + Ycomponent;
            s += "\n Magnitude : " + magnitude;
            s += "\n";
            return s;
        }
    }
    
    
    static void findIntersection(Vector v, LineSegment l)   // Yep, it's the exact same logic for 2d and 3d **
    {
        Vector l0 = new Vector(l.endpoint1);
        Vector v0 = new Vector(origin);    
        
        double denominator = Vector.dotProduct(v, l.normal);
        if (denominator == 0) return;       // special logic for reporting colors of parallel lines could go here if we want
        
        double numerator = Vector.dotProduct(l0, l.normal) - Vector.dotProduct(v0, l.normal);
        
        double t = numerator / denominator;
        if (t < 0) return;
        
        Point intersection = new Point( Vector.sum(v0, Vector.scalarMultiple(v, t)) );
        if (withinEndpoints(intersection, l)) 
        {
            if (pointBuffer == null) pointBuffer = intersection;
            else if ( distance(intersection, origin) < distance(pointBuffer, origin) ) pointBuffer = intersection;
        }
    }
    
    static boolean withinEndpoints(Point p, LineSegment l)
    {
        if (p.X <= Math.max(l.endpoint1.X, l.endpoint2.X) && p.X >= Math.min(l.endpoint1.X, l.endpoint2.X))
           if (p.Y <= Math.max(l.endpoint1.Y, l.endpoint2.Y) && p.Y >= Math.min(l.endpoint1.Y, l.endpoint2.Y))
               return true;
        return false;
    }
    
    
    static double distance(Point p0, Point p1)
    {
        double deltaX = toDouble(p1.X - p0.X);
        double deltaY = toDouble(p1.Y - p0.Y);
        return distance(deltaX, deltaY);
    }
    
    static double distance(double deltaX, double deltaY)  
    {
        return Math.sqrt(deltaX*deltaX + deltaY*deltaY);   
    }
    static double toDouble(int a)
    {
        return ( (double) a ) / 100;
    }
    static int toInt (double d)
    {
        return (int)( d * 100 );
    }
}