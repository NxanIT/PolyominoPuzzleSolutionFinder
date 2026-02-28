package app;

public class Coordinate {
    private int x;
    private int y;
    private int z;

    Coordinate(int x, int y, int z){
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    boolean z_equals(int c) {
        return z==c;
    }
    
    int getX() {
        return x;
    }
    
    int getY() {
        return y;
    }
    
    int getZ() {
        return z;
    }
}
