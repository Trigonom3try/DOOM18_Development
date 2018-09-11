
    // I used examples from Linear Algebra to debug this
    // The problem is set up such that we have a point on each line and each slope, which is what we have
    // in the actual ray tracer (vectors and origins). It does not use coefficients to solve the system.

    // See : DOOM18_DEP - Line Intersection with y = mx + b.txt

package DOOM18_Dev_0;     

public class LineIntersectionLogic 
{
    public static void main(String [] args)
    {
        Line l1 = new Line(12, 35, 3);  // a known X coordinate, a known Y coordinate, and a slope
        Line l2 = new Line(-4, -8, 2);  // arbitrarily chosen points on 2 lines from my linear algebra text
        findIntersection(l1,l2);
    }
    
    static class Line
    {
        double X;   double Y;   double M;
        Line(double x, double y, double m)
        {
            X = x; Y = y; M = m;
        }
    }
    
    static void findIntersection(Line l1, Line l2)  // doesn't include all the infinity checking nonsense
    {
        double b1 = l1.Y - (l1.X * l1.M);   // b1 is the y-intercept for the first line
        double b2 = l2.Y - (l2.X * l2.M);   // b2 is the y-intercept for the second line
        
        double X = (b2 - b1) / (l1.M - l2.M);   // X is the x-coordinate of the purported intersection
        
        double Y1 = (l1.M * X) + b1;    // plug in X to the first equation
        double Y2 = (l2.M * X) + b2;    // plug in X to the second equation
        
        if (Y1 == Y2)    // if the y-coordinates you obtain are equal, the point X, Y1 is on both lines
            System.out.println("Intersection found at X : " + X + " , Y : " + Y1);  // or Y2
        else System.out.println("NO intersection found");
    }
}