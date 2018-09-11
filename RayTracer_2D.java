

// I completed this app some time after writing the findIntersection method in 3D for the Kermo, and after doing much of the work on 3D ray-tracing,
// but before successfully debugging Kermo_Foundation.java. I figured it would be important to get 2D ray-tracing to work before finishing up with 3D


package DOOM18_Dev_0;

import java.util.*;
import java.math.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.*;

public class RayTracer_2D 
{
    static enum AXIS { Xpos, Xneg, Ypos, Yneg }
    
    static final int FieldOfVision = 150;
    static final int FrameWidth = 5;
    static final int colorDistance = 500 / FrameWidth;  // 500 is the pixel width of the JPanel that will be the display  
    static final Color defaultColor = Color.WHITE;   // the default color for frame pixels
    
    static Color [] Frame = new Color[FrameWidth];
    static ArrayList<LineSegment> LineSegments = new ArrayList<LineSegment>();
    static ArrayList<Point> Intersections = new ArrayList<Point>();
    static Camera camera = new Camera();
    
    
    static TD_ViewPanel td_view = new TD_ViewPanel();
    static FP_ViewPanel fp_view = new FP_ViewPanel();
    static CameraTranslator camTranslator = new CameraTranslator();
    static CameraRotator camRotator = new CameraRotator();
    
    static Point L1 = null;   // this is for line segment creation
    
    public static void main(String [] args)
    {
        LineSegments.add(new LineSegment(new Point(10,10), new Point(490,10), Color.black));
        ViewWindow td_viewWindow = new ViewWindow("Top Down View", 500);
        ViewWindow fp_viewWindow = new ViewWindow("First Person View", 50);
        td_viewWindow.add(td_view);
        fp_viewWindow.add(fp_view);     
    }
    
    
    static class ViewWindow extends JFrame
    {
        ViewWindow(String title, int height)
        {
            setTitle(title);
            setSize(500,height);
            setResizable(false);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setVisible(true);
        }
    }
    
    
    static class TD_ViewPanel extends JPanel  
    {
        TD_ViewPanel()
        {
            addMouseListener(new LineSegmentCreator());
        }
        public void paintComponent(Graphics g)
        {
            g.setColor(Color.white);
            g.fillRect(0, 0, 500, 500);
            
            g.setColor(Color.blue);
            int rayX1 = camera.origin.X;
            int rayY1 = camera.origin.Y;
            for (int i = 0; i < camera.Rays.length; ++i)
            {
                int rayX2 = toInt( camera.Rays[i].Xcomponent ) + rayX1;
                int rayY2 = toInt( camera.Rays[i].Ycomponent ) + rayY1;
                g.drawLine(rayX1, rayY1, rayX2, rayY2);
            }
            g.setColor(Color.black);
            for (LineSegment ls : LineSegments)
                g.drawLine(ls.endpoint1.X, ls.endpoint1.Y, ls.endpoint2.X, ls.endpoint2.Y);
            
            g.setColor(Color.red);
            for (Point i : Intersections)
                g.fillOval(i.X, i.Y, 5, 5);
        }
    }
    
    static class FP_ViewPanel extends JPanel
    {   
        public void paintComponent(Graphics g)
        {
            for (int i = 0; i < Frame.length; ++i)
            {
                g.setColor(Frame[i]);
                g.fillRect(i * colorDistance, 0, colorDistance, 50); // 50 is the pixel height of the panel
            }
        }
    }
    
    static class CameraTranslator extends JFrame
    {
        Button left = new Button("<", "cam_move_left");
        Button right = new Button(">", "cam_move_right");
        Button up = new Button("^", "cam_move_up");
        Button down = new Button("v", "cam_move_down");
        
        CameraTranslator()
        {
            setTitle("Camera Translator");
            setSize(150,150);
            setResizable(false);
            add(left, BorderLayout.WEST);
            add(right, BorderLayout.EAST);
            add(up, BorderLayout.CENTER);
            add(down, BorderLayout.SOUTH);
            setVisible(true);
        }
    }
    
    static class CameraRotator extends JFrame
    {
        Button left = new Button("<", "cam_rotate_left");
        Button right = new Button(">", "cam_rotate_right");
        
        CameraRotator()
        {
            setTitle("Camera Rotator");
            setSize(100,100);
            setResizable(false);
            setLayout(new GridLayout(1,2));
            add(left);      add(right);
            setVisible(true);
        }
    }
    
    static class Button extends JButton
    {
        String tag;
        Button(String label, String t)
        {
            setText(label);
            tag = t;
            addActionListener(new ButtonListener());
        }
    }
    
    static class ButtonListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            String tag = ((Button)e.getSource()).tag;
            if (tag.equals("cam_move_left")) camera.move(-5,0);     
            if (tag.equals("cam_move_right")) camera.move(5,0);
            if (tag.equals("cam_move_up")) camera.move(0,-5);   
            if (tag.equals("cam_move_down")) camera.move(0,5);
            if (tag.equals("cam_rotate_left")) camera.rotate(-Math.PI / 20);
            if (tag.equals("cam_rotate_right")) camera.rotate(Math.PI / 20);
            
            camera.updateFrame();
            
            td_view.repaint();
            fp_view.repaint();
        }
    }
    
    static class LineSegmentCreator implements MouseListener
    {
        public void mouseClicked(MouseEvent e)
        {
            int X = e.getX();   int Y = e.getY();
            if (L1 == null) L1 = new Point(X, Y);
            else 
            {
                LineSegments.add(new LineSegment(L1, new Point(X,Y), Color.black));
                L1 = null;
                td_view.repaint();
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
            color = c;
        }
    }
    
    static class Camera
    {
        Vector normal = new Vector(AXIS.Xpos);   // not strictly necessary in this context
        Vector [] Rays = new Vector[FrameWidth]; // we only keep a bunch of rays in memory so that we can draw the camera to the Top Down view panel
        Point origin = new Point(250,250);
        Point pointBuffer = null;       
        
        Camera()
        {
            Vector ray = new Vector(normal);
            
            double initialRotation = toRadians( (double) (  FieldOfVision / 2 ) );
            double incrementalRotation = toRadians( (double)(FieldOfVision / (FrameWidth-1)) );
            
            ray.Rotate(-initialRotation);
            for (int i = 0; i < FrameWidth; ++i)
            {
                Rays[i] = new Vector(ray);
                ray.Rotate(incrementalRotation);
            }    
            updateFrame();
        }
        
        void move(int x, int y)
        {
            origin.add(x, y);
        }
        void rotate(double rotation)
        {
            normal.Rotate(rotation);
            for (int i = 0; i < Rays.length; ++i)
                Rays[i].Rotate(rotation);
        }
        
        void updateFrame()
        {
            Intersections = new ArrayList<Point>();
            for (int i = 0; i < Rays.length; ++i)
            {
                pointBuffer = null;
                for (LineSegment l : LineSegments)
                {
                    Point p = findIntersection(Rays[i], l);
                    if ( p == null ) continue;
                    if ( withinEndpoints(p, l) )
                    {
                        if ( pointBuffer == null) pointBuffer = p;
                        else if (distance(p, origin) < distance(pointBuffer, origin)) pointBuffer = p;
                    }
                }
                if (pointBuffer == null) Frame[i] = defaultColor; 
                else 
                {   
                    Intersections.add(pointBuffer);
                    Frame[i] = Color.black;
                }
            }
        }
    }
    
    
    
    static class Vector
    {
        double Xcomponent;       
        double Ycomponent;
        double magnitude;      
        
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
        
        void Rotate(double rotation)
        {
            double Xtemp = Xcomponent;
            Xcomponent = Xcomponent*Math.cos(rotation) - Ycomponent*Math.sin(rotation);  
            Ycomponent = Xtemp*Math.sin(rotation) + Ycomponent*Math.cos(rotation);   
            magnitude = distance(Xcomponent, Ycomponent);
            changeMagnitude(1); 
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
    
    static Point findIntersection(Vector v, LineSegment l)    
    {
        Vector l0 = new Vector(l.endpoint1);
        Vector v0 = new Vector(camera.origin);    
        double denominator = Vector.dotProduct(v, l.normal);
        if (denominator == 0) return null;       // special logic for reporting colors of parallel lines could go here if we want
        
        double numerator = Vector.dotProduct(l0, l.normal) - Vector.dotProduct(v0, l.normal);
        double t = numerator / denominator;
        if (t < 0) return null;
        
        Point intersection = new Point( Vector.sum(v0, Vector.scalarMultiple(v, t)) );
        
        return intersection;
    }
    
    static boolean withinEndpoints(Point p, LineSegment l)
    {
        if (p.X <= Math.max(l.endpoint1.X,l.endpoint2.X) && p.X >= Math.min(l.endpoint1.X,l.endpoint2.X))     // nested conditionals for readability
                if (p.Y <= Math.max(l.endpoint1.Y,l.endpoint2.Y) && p.Y >= Math.min(l.endpoint1.Y,l.endpoint2.Y))
                    return true;
        return false;
    }
    
    static double distance(Point p0, Point p1)
    {
        double deltaX = toDouble(p1.X - p0.X);
        double deltaY = toDouble(p1.Y - p0.Y);
        return distance(deltaX, deltaY);        // recursive call to save some typing
    }
    
    static double distance(double deltaX, double deltaY)  // finds a 2 dimensional length or distance from 2 deltas
    {
        return Math.sqrt(deltaX*deltaX + deltaY*deltaY);    // Pythagoras in 2 variables
    }
    
    static double toRadians(double d)
    {
        return (Math.PI * d) / 180;
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