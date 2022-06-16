import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class Matrix implements Serializable {
    private int height;
    private int width; 
    private double[][] matrix;

    public Matrix(int height, int width, ArrayList<Double> ints) {
        if (ints.size() != height*width) {
            System.out.println("Invalid matrix");
            return;
        }
        this.height = height;
        this.width = width;
        matrix = new double[height][width];
        int intsCounter = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                matrix[i][j] = ints.get(intsCounter);
                intsCounter++;
            }
        }
    }

    public Matrix(double[][] array) {
        for (double[] a: array) {
            if (a.length != array[0].length) {
                System.out.println("Invalid array for matrix");
                return;
            }
        }
        this.height = array.length;
        this.width = array[0].length;
        matrix = array;
    }

    public Matrix encode(Matrix b) { // this*B, this.matrix will always be a 1x3 matrix
        if (this.width != b.getHeight()) {
            System.out.println("Invalid matrices to multiply");
            return null;
        }
        ArrayList<Double> nums = new ArrayList<>();
        double[][] matrixB = b.getMatrix();
        // int thisRow = 0;
        int thisColumn = 0;
        for (int i = 1; i <= b.getWidth(); i++) {
            double sum = 0;
            thisColumn = 0;
            for (int j = 1; j <= b.getHeight(); j++) {
                double element1 = matrixB[j-1][i-1];
                double element2 = this.matrix[0][thisColumn];
                sum+=element1*element2;
                thisColumn++;
            }
            nums.add(sum);
        }
        return new Matrix(1, this.width, nums);
    }

    public double calculateDeterminant() {
        double d = 0;
        int sign = 1;
        if (this.getHeight() == 1) {
            return this.getMatrix()[0][0];
        }
        if (this.getHeight() == 2) {
            return this.getMatrix()[0][0] * this.getMatrix()[1][1]
                    - this.getMatrix()[0][1] * this.getMatrix()[1][0];
        }
        for (int i = 0; i < this.getHeight(); i++) {
            d += sign * this.getMatrix()[0][i] * this.getSubMatrix(0, i).calculateDeterminant();
            sign = -sign;
        }
        return d;
    }

    public void calculateCofactors(ArrayList<Double> cofactors) {
        for (int i = 0; i < this.getHeight(); i++) {
            for (int j = 0; j < this.getWidth(); j++) {
                cofactors.add((Math.pow(-1.0, (double) i + j) * this.getSubMatrix(i, j).calculateDeterminant()));
            }
        }
    }

    public Matrix getTransposeMatrix() {
        ArrayList<Double> transposedList = new ArrayList<>();
        for (int column = 0; column < width; column++) {
            for (int row = 0; row < height; row++) {
                transposedList.add(matrix[row][column]);
            }
        }
        return new Matrix(height, width, transposedList);
    }

    public double[][] getMatrix() {
        return matrix;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Matrix getSubMatrix(int row, int column) {
        ArrayList<Double> newMatrix = new ArrayList<>();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (row != i && column != j) {
                    newMatrix.add(this.matrix[i][j]);
                }
            }
        }
        return new Matrix(height-1, width-1, newMatrix);
    }

    public void scaleMatrix(double x) {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                matrix[i][j] = x*matrix[i][j];
            }
        }
    }

    @Override
    public String toString() {
        String matrixString = "";
        for (double[] row: matrix) {
            matrixString+=Arrays.toString(row);
            matrixString+="\n";
        }
        return matrixString;
    }

    public ArrayList<Double> toList() {
        ArrayList<Double> list = new ArrayList<>();
        for (double[] row: matrix) {
            for (double element: row) {
                list.add(element);
            }
        }
        return list; 
    }
}
