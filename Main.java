package MatrixCoder;

import java.util.ArrayList;
import java.util.Scanner;

// A program to encode and decode alphabetical messages using any size matrix 
public class Main {

    public static final String[] VALUE_TABLE = new String[]{" ", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        while (true) { // menu to encode, decode, or quit program
            int decision = 0;
            while (decision != 1 && decision != 2 && decision != 3) {
                System.out.print("Do you want to (1) encode or (2) decode or (3) quit? ");
                decision = scan.nextInt();
            }
            if (decision == 1) {
                encode(scan);
            }

            else if (decision == 2) {
                decode(scan);
            }

            else {
                break;
            }

        }
    }












    public static void decode(Scanner scan) { // Decoding a message with matrix
        scan.nextLine();
        ArrayList<Double> encodedValues = new ArrayList<>(); // the array containing the encoded values, user entered
        System.out.print("Enter values (spaces in between each): ");
        String values = scan.nextLine();
        Scanner valueScan = new Scanner(values); // go through the space-separated values in order to put them in the encoded values array
        while (valueScan.hasNext()) {
            encodedValues.add(Double.parseDouble(valueScan.next())); // converts strings to doubles
        }
        valueScan.close();
        System.out.print("What height is your key matrix? (The matrix is always square for this purpose) ");
        int height = scan.nextInt(); // width and height of the key matrix (must always be square)
        int width = height;
        if (encodedValues.size() % height != 0) {
            System.out.println("Invalid input, make sure that the values can be split evenly into groups of " + height);
            return;
        }
        ArrayList<Matrix> encodedMatrices = new ArrayList<>();
        ArrayList<Double> tempTriples = new ArrayList<>();
        for (int i = 0; i < encodedValues.size(); i++) {
            tempTriples.add(encodedValues.get(i));
            if ((i + 1) % height == 0) {
                encodedMatrices.add(new Matrix(1, width, tempTriples));
                tempTriples.clear();
            }
        }
        
        ArrayList<Double> matrixValues = new ArrayList<>();
        for (int i = 1; i <= height; i++) {
            for (int j = 1; j <= width; j++) {
                System.out.print("Enter value at position " + i + j + " on the key matrix: ");
                matrixValues.add(scan.nextDouble());
            }
        }
        Matrix keyMatrix = new Matrix(height, width, matrixValues);
        double determinant = calculateDeterminant(keyMatrix);
        if (determinant == 0) {
            System.out.println("This matrix is not invertible");
            return;
        }
        ArrayList<Double> cofactors = new ArrayList<>();
        calculateCofactors(keyMatrix, cofactors);
        Matrix cofactorMatrix = new Matrix(height, width, cofactors);
        Matrix inverseMatrix = cofactorMatrix.getTransposeMatrix();
        inverseMatrix.scaleMatrix(1.0 / determinant);
        ArrayList<Double> valuesToConvert = new ArrayList<>();
        for (Matrix m: encodedMatrices) {
            Matrix encoded = m.encode(inverseMatrix);
            for (int i = 0; i < encoded.getHeight(); i++) {
                for (int j = 0; j < encoded.getWidth(); j++) {
                    valuesToConvert.add(encoded.getMatrix()[i][j]);
                }
            }
        }
        String message = convertIntToText(valuesToConvert);   
        System.out.println("Your message is: " + message); 
    }

    public static String convertIntToText(ArrayList<Double> values) {
        String message = "";
        for (double value: values) {
            message += VALUE_TABLE[(int) Math.round(value)];
        }
        return message;
    }

    public static void encode(Scanner scan) {
        System.out.print("What height is your key matrix? (The key matrix is square) ");
        int height = scan.nextInt();
        scan.nextLine();
        int width = height;
        System.out.print("What is the message you would like to encode (Only alphabets and spaced please)? ");
        String message = scan.nextLine();
        message = message.toUpperCase();
        System.out.println("Your message to encode: " + message);
        ArrayList<Double> values = new ArrayList<>();
        for (int i = 1; i <= height; i++) {
            for (int j = 1; j <= width; j++) {
                System.out.print("Enter value at position " + i + j + " on the key matrix: ");
                values.add(scan.nextDouble());
            }
        }
        Matrix keyMatrix = new Matrix(height, width, values);
        System.out.println("The encoding matrix: " + keyMatrix);
        ArrayList<Matrix> messageMatrices = new ArrayList<>();
        convertTextToInt(message, messageMatrices, height);
        ArrayList<Matrix> encodedMessage = new ArrayList<>();
        for (Matrix temMatrix : messageMatrices) {
            encodedMessage.add(temMatrix.encode(keyMatrix));
        }
        System.out.println("Your encoded message: ");
        for (Matrix temMatrix : encodedMessage) {
            for (double x : temMatrix.toList()) {
                System.out.print(x + " ");
            }
        }
        System.out.println();
    }

    public static double calculateDeterminant(Matrix matrix) {
        double d = 0;
        int sign = 1;
        if (matrix.getHeight() == 1) {
            return matrix.getMatrix()[0][0];
        }
        if (matrix.getHeight() == 2) {
            return matrix.getMatrix()[0][0] * matrix.getMatrix()[1][1]
                    - matrix.getMatrix()[0][1] * matrix.getMatrix()[1][0];
        }
        for (int i = 0; i < matrix.getHeight(); i++) {
            d += sign * matrix.getMatrix()[0][i] * calculateDeterminant(matrix.getSubMatrix(0, i));
            sign = -sign;
        }
        return d;
    }

    public static void calculateCofactors(Matrix matrix, ArrayList<Double> cofactors) {
        for (int i = 0; i < matrix.getHeight(); i++) {
            for (int j = 0; j < matrix.getWidth(); j++) {
                cofactors.add((Math.pow(-1.0, (double) i + j) * calculateDeterminant(matrix.getSubMatrix(i, j))));
            }
        }
    }

    public static void convertTextToInt(String message, ArrayList<Matrix> matrices, int keyMatrixSize) {
        if (message.length() % keyMatrixSize != 0) {
            int length = message.length();
            while (length % keyMatrixSize != 0) {
                message += " ";
                length++;
            }
        }
        ArrayList<Double> numToMakeMatrix = new ArrayList<>();
        for (int i = 0; i < message.length(); i++) {
            char temp = message.charAt(i);
            double numToInsert = 0;
            if (temp != ' ') {
                int ascii = temp;
                numToInsert = ascii - 64.0; // Converts to space = 0, a = 1, b = 2, etc.
            }
            numToMakeMatrix.add(numToInsert);
            if ((i + 1) % keyMatrixSize == 0) {
                matrices.add(new Matrix(1, keyMatrixSize, numToMakeMatrix));
                numToMakeMatrix.clear();
            }
        }
    }

}
