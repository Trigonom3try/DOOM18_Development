

/*  This version of LineIntersectionFinder uses an ArrayList of multiple Points for the pointBuffer. This will be necessary for the final version of 
    DOOM18, because in the final version there will be walls (which are line segments) of different heights, meaning that a wall that is lower than the
    current Z-position of the ray cast during the camera update loop will not be rendered, and a higher wall that is farther away would have to be 
    stored such that it is rendered instead (think of looking over a wall that is waist-high to one that is storey-high); also there will be null-valued 
    Colorpoints in Sprites (think of the area under a demon's shoulder between its elbow and torso, or between its legs) where whatever is behind the 
    sprite will have to be rendered instead (and thus stored in an ArrayList buffer for that purpose). The purpose of this app is to demonstrate that 
    simple logic in a straight-forward ray-tracer (even though it is not strictly necessary, See : LineIntersectionFinder.java).
    Points in this ArrayList are sorted by distance from origin using a Bubblesort algorithm
*/

package DOOM18_Dev_0;
import java.util.*;
import java.math.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class LineIntersectionFinder_sortPB
{
    static enum AXIS { Xpos, Xneg, Ypos, Yneg }
    static Vector VictorTheVector = new Vector(AXIS.Xpos);      
    static ViewPanel vp = new ViewPanel();
    static ButtonPanel bp = new ButtonPanel();
    static ArrayList<LineSegment> LineSegments = new ArrayList<LineSegment>();
    static ArrayList<Point> pointBuffer = new ArrayList<Point>();
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
    
    
    
    static class ViewPanel extends JPanel   // the part of the window that draws the vector
    {
        ViewPanel()
        {
            addMouseListener(new LineSegmentCreator());
        }
        public void paintComponent(Graphics g)
        {
            g.setColor(Color.white);
            g.fillRect(0, 0, 500, 500);         // this is the background
            
            int VictorX = toInt( VictorTheVector.Xcomponent );
            int VictorY = toInt ( VictorTheVector.Ycomponent );
            
            g.setColor(Color.red);
            g.drawLine(250, 250, 250 + VictorX, 250 + VictorY);     
            
            g.setColor(Color.blue);
            for (LineSegment l : LineSegments)
                g.drawLine(l.endpoint1.X, l.endpoint1.Y, l.endpoint2.X, l.endpoint2.Y);
            
            g.setColor(Color.black);
            if (pointBuffer.size() != 0)
                g.fillOval(pointBuffer.get(0).X, pointBuffer.get(0).Y, 5,5);
        }
    }
    
    
    static class ButtonPanel extends JPanel // here and in Vector_Rotator_3D, the rotator buttons get attached directly to the JFrame
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
            pointBuffer = new ArrayList<Point>();
            
            char c = ((JButton)e.getSource()).getText().charAt(0);
            if (c == '<') VictorTheVector.Rotate(-Math.PI / 20);     // a Radians value. Press the button 20 times to rotate 180 degrees
            else VictorTheVector.Rotate(Math.PI / 20);          // negative for left, positive for right
            
            for (int i = 0; i < LineSegments.size(); ++i) 
                findIntersection(VictorTheVector, LineSegments.get(i));
            
            sortPointBuffer();
            
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
        Vector normal;
        Point endpoint1;
        Point endpoint2;
        Color color;
        
        LineSegment(Point p0, Point p1, Color c)
        {
            endpoint1 = p0; endpoint2 = p1; slope = new Vector(p0, p1);
            normal = new Vector(-slope.Ycomponent, slope.Xcomponent);   // negative reciprocal slope 
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
    
    
    static void findIntersection(Vector v, LineSegment l) 
    {
        Vector p0 = new Vector(l.endpoint1);
        Vector l0 = new Vector(origin);    
        
        double denominator = Vector.dotProduct(v, l.normal);
        if (denominator == 0) return;       // special logic for reporting colors of parallel lines could go here if we want
        
        double numerator = Vector.dotProduct(p0, l.normal) - Vector.dotProduct(l0, l.normal);
        
        double t = numerator / denominator;
        if (t < 0) return;
        
        
        Point intersection = new Point( Vector.sum(l0, Vector.scalarMultiple(v, t)) );
        if (withinEndpoints(intersection, l)) 
            pointBuffer.add(intersection); 
    }
    
    static boolean withinEndpoints(Point p, LineSegment l)
    {
        if (p.X <= Math.max(l.endpoint1.X, l.endpoint2.X) && p.X >= Math.min(l.endpoint1.X, l.endpoint2.X))
           if (p.Y <= Math.max(l.endpoint1.Y, l.endpoint2.Y) && p.Y >= Math.min(l.endpoint1.Y, l.endpoint2.Y))
               return true;
        return false;
    }
    
    
    static void sortPointBuffer()       // This is Bubblesort
    {
        for (int i = 0; i < pointBuffer.size(); ++i)
            for (int j = pointBuffer.size()-1; j > i; --j)
            {
                Point p0 = pointBuffer.get(j);
                Point p1 = pointBuffer.get(j-1);
                double d0 = distance(p0, origin);
                double d1 = distance(p1, origin);
                
                if (d0 < d1) 
                {
                    pointBuffer.set(j, p1);
                    pointBuffer.set(j-1, p0);
                }  
            }
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