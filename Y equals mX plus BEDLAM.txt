

/*  Using y = mx + b to find the intersection of two lines seems at first glance to be the way to go, as it saves a bit of code elsewhere in the program -
    you don't have to worry about the dot product, sum, or scalar multiple of vectors, or stipulate a normal for line segments (even though that's easy
    to do) - but it creates a lot of aggravation largely due to the fact that you have to deal with positive and negative infinity and division by zero,
,   and tailor your code to special cases. God, I hate slopes... when I was initially working on the Kermo, I dealt with slopes because they are 
    equivalent to angles and I thought they could be useful, but now I see that neither angles nor slopes are of any use whatsoever and will only drive 
    you crazy (See : Kermo_DEP - Angles and Slopes.txt). Anyways the algebra here is messy to read, there are double-int conversions that are hard to 
    understand, and 
    debugging was a pain. When I found myself having to make an additionalIntersectionLogic method in addition to findIntersection and taking more 
    parameters, I literally started laughing out loud because of how disgusting the code was starting to look. It was bedlam, man. 

    I initially wanted to use y = mx + b in the final version of DOOM18 if only to shake things up from the Kermo (which has the exact same code to the 
    letter for finding ray intersections) and show that there are often different ways of doing things in code that can still work (which, believe it or
    not, Y_equals_m_x_plus_BEDLAM.java does; go ahead and test it), but now I see that vector forms of lines in 2D are the way to go, in terms of 
    efficiency, elegance, and readability. Also, other aspects of the complete game's ultimate structure, such as hit detection, are going to require
    things like vector normals to line segments so I would have to include those things in the code anyway, so there's really no point in using this
    findIntersection logic if it doesn't ultimately save you any code elsewhere and is unnecessarily messy and sloppy.
    
    See : LineIntersectionLogic.java, DOOM18_DEP - Line Intersection with y = mx + b.txt
    See : LineIntersectionFinder.java, LineIntersectionFinder_sortPB.java (for the more elegant method)

    This app finds the intersection of a line subtended from the origin (250,250) of the JPanel having a slope that is a vector, colored red, that you can
    rotate using two buttons on the JFrame, and line segments which are colored blue. The intersection is drawn to the screen with a black dot.

    Click on two points on the JPanel to instantiate a line segment
*/

package DOOM18_Dev_0;
import java.util.*;
import java.math.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class Y_equals_m_x_plus_BEDLAM
{
    static enum AXIS { Xpos, Xneg, Ypos, Yneg }
    static Vector VictorTheVector = new Vector(AXIS.Xpos);      // Guess who's back and ready to find some intersections
    static ViewPanel vp = new ViewPanel();
    static ButtonPanel bp = new ButtonPanel();
    static ArrayList<LineSegment> Lines = new ArrayList<LineSegment>();
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
            for (LineSegment l : Lines)
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
            
            for (LineSegment l : Lines)
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
                Lines.add(new LineSegment(L1, new Point(X,Y), Color.black));
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
        Point endpoint1;
        Point endpoint2;
        Color color;
        
        LineSegment(Point p0, Point p1, Color c)
        {
            endpoint1 = p0; endpoint2 = p1; slope = new Vector(p0, p1);
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
    
    
    static void findIntersection(Vector v, LineSegment l)   //  This was a battle, man. Love is a Battlefied *
                                                            //  But it works
    {
        double x1 = toDouble(origin.X);
        double y1 = toDouble(origin.Y);
        double m1 = v.Ycomponent / v.Xcomponent;
        
        double x2 = toDouble(l.endpoint1.X);
        double y2 = toDouble(l.endpoint1.Y);
        double m2 = toDouble(l.endpoint2.Y - l.endpoint1.Y) / toDouble(l.endpoint2.X - l.endpoint1.X);
        
        if ( (m1 == Double.POSITIVE_INFINITY || m1 == Double.NEGATIVE_INFINITY) && (m2 == Double.POSITIVE_INFINITY || m2 == Double.NEGATIVE_INFINITY)) 
        {
            if ( toInt(x1) == toInt(x2) ) 
            {
                additionalIntersectionLogic(new Point(toInt(x1), toInt(y2)), v, l);
            }   else return;
        }   
        else if (m1 == Double.POSITIVE_INFINITY || m1 == Double.NEGATIVE_INFINITY)  
        { 
            double b2 = y2 - (m2 * x2);
            int Y = toInt( m2*x1 + b2 );
            additionalIntersectionLogic(new Point(toInt(x1), Y), v, l);
        }
        else if (m2 == Double.POSITIVE_INFINITY || m2 == Double.NEGATIVE_INFINITY) 
        {
            double b1 = y1 - (m1 * x1);
            int Y = toInt( m1*x2 + b1 );
            additionalIntersectionLogic(new Point(toInt(x2), Y), v, l);
        }
        else if (m1 == 0 && m2 == 0)
            additionalIntersectionLogic(new Point(toInt(x2), toInt(y2)), v, l);
        else
        {
            double b1 = y1 - (m1 * x1);
            double b2 = y2 - (m2 * x2);
            double X = (b2 - b1) / (m1 - m2);
            int Y1 = toInt ( m1*X + b1 );
            int Y2 = toInt ( m2*X + b2 );
            
            if (Y1 == Y2) additionalIntersectionLogic(new Point(toInt(X), Y1), v, l);
        }
    }
    
    static void additionalIntersectionLogic(Point p, Vector v, LineSegment l)     // this is bedlam
    {
        Point intersection = p;
            if ( withinEndpoints(intersection, l) )     // point is within the endpoints of the line segment
                if ( checkVectorSigns(v, new Vector(origin, intersection)) )    // point is not behind the camera
                    if ( pointBuffer == null ) pointBuffer = intersection;      // nothing in the point buffer yet, set pB to the point
                    else if ( distance(intersection, origin) < distance(pointBuffer, origin) ) pointBuffer = intersection;  
                                                        // point is closer to the camera than the pB, set pB to the point
    }
    
    static boolean withinEndpoints(Point p, LineSegment l)
    {
        if (p.X <= Math.max(l.endpoint1.X, l.endpoint2.X) && p.X >= Math.min(l.endpoint1.X, l.endpoint2.X))
           if (p.Y <= Math.max(l.endpoint1.Y, l.endpoint2.Y) && p.Y >= Math.min(l.endpoint1.Y, l.endpoint2.Y))
               return true;
        return false;
    }
    
    static boolean checkVectorSigns(Vector v1, Vector v2)   // two vectors are antiparallel iff all of their components differ in sign
                // this logic has the same function as the if (t < 0) line in the findIntersection method for the vector forms version of the app: it 
                // checks whether the point of intersection is behind the camera
    {
        if ( (v1.Xcomponent <= 0 && v2.Xcomponent >= 0) || (v1.Xcomponent >= 0 && v2.Xcomponent <= 0) )
            if ( (v1.Ycomponent <= 0 && v2.Ycomponent >= 0) || (v1.Ycomponent >= 0 && v2.Ycomponent <= 0) )
                return false; 
        return true;
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