package us.ilite.common.lib.util;

/**
 * @author jmshapiro
 * RangeScale will scale an input value in range a to range b or the reverse.
 */
public class RangeScale {

    private double rangeAMin;
    private double rangeAMax;
    private double rangeBMin;
    private double rangeBMax;

    public RangeScale(double rangeAMin, double rangeAMax, double rangeBMin, double rangeBMax ) {
        this.rangeAMin = rangeAMin;
        this.rangeAMax = rangeAMax;
        this.rangeBMin = rangeBMin;
        this.rangeBMax = rangeBMax;
    }

    public double scaleAtoB(double aInput) {
        return (aInput - this.rangeAMin) / (this.rangeAMax - this.rangeAMin) * (this.rangeBMax - this.rangeBMin) + this.rangeBMin;
    }

    public double scaleBtoA(double bInput) {
        return (bInput - this.rangeBMin) / (this.rangeBMax - this.rangeBMin) * (this.rangeAMax - this.rangeAMin) + this.rangeAMin;
    }

    
    
	/**
	 * Unit test
	 * @param args
	 */
	public static void main(String[] args) {

		RangeScale rScale = new RangeScale(-1, 1, 0, 135);
		double[] inputA = {-2.0, -1.0, 0.0, 1.0, 2.0};
		
		for (double i :inputA) {
			System.out.println("Test a to b input = " + i + " scaled output = " + rScale.scaleAtoB(i));
		}
		
		double[] inputB = {-45.0, 0.0, 45.0, 67.5, 90.0, 135.0, 180.0};
		
		for (double i :inputB) {
			System.out.println("Test b to a input = " + i + " scaled output = " + rScale.scaleBtoA(i));
		}
		
		// Now test a reversed range
		RangeScale rScaleRev = new RangeScale(1, -1, 0, 135);
		
		for (double i :inputA) {
			System.out.println("Test reversed a to b input = " + i + " scaled output = " + rScaleRev.scaleAtoB(i));
		}
		
		for (double i :inputB) {
			System.out.println("Test b to reversed a input = " + i + " scaled output = " + rScaleRev.scaleBtoA(i));
		}
		
		
	}
    
}
