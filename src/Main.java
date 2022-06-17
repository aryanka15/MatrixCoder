import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Scanner;

// A program to encode and decode alphabetical messages using any size matrix 
public class Main {

    public static final String SEP = File.separator;
    public static final String[] VALUE_TABLE = new String[] { " ", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
            "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };
    public static final String MATRIX_FILE = System.getProperty("user.dir") + SEP + "database" + SEP + "Matrix.dat";
    public static final String VALUES_FILE = System.getProperty("user.dir") + SEP + "database" + SEP + "Values.dat";

    public static void main(String[] args) throws ClassNotFoundException, IOException {
        System.out.println(MATRIX_FILE);
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

    public static void decode(Scanner scan) throws IOException, ClassNotFoundException { // Decoding a message with
                                                                                         // matrix
        scan.nextLine();
        ArrayList<Double> encodedValues = new ArrayList<>(); // the array containing the encoded values, user entered
        System.out.print("Enter values (spaces in between each) or type L to load from file: ");
        String values = scan.nextLine();
        if (values.equalsIgnoreCase("L")) {
            File file = new File(VALUES_FILE);
            if (file.exists()) {
                try {
                    FileInputStream fis = new FileInputStream(VALUES_FILE);
                    ObjectInputStream in = new ObjectInputStream(fis);

                    encodedValues = (ArrayList<Double>) in.readObject();

                    in.close();
                    fis.close();
                } catch (IOException ex) {
                    System.out.println("File invalid");
                }
            }
        } else {
            Scanner valueScan = new Scanner(values); // go through the space-separated values in order to put them in
                                                     // the encoded values array
            while (valueScan.hasNext()) {
                encodedValues.add(Double.parseDouble(valueScan.next())); // converts strings to doubles
            }
            valueScan.close();
        }
        System.out.print(
                "What height is your key matrix? (The matrix is always square for this purpose) or press L to load matrix from file: ");
        String next = scan.next(); // width and height of the key matrix (must always be square)
        Matrix keyMatrix = null;
        int height = 0;
        int width = 0;
        if (next.equalsIgnoreCase("l")) {
            File file = new File(MATRIX_FILE);
            if (file.exists()) {
                try {
                    FileInputStream fis = new FileInputStream(file);
                    ObjectInputStream in = new ObjectInputStream(fis);

                    keyMatrix = (Matrix) in.readObject();
                    height = keyMatrix.getHeight();
                    width = keyMatrix.getWidth();
                    in.close();
                    fis.close();
                } catch (IOException ex) {
                    System.out.println("File invalid");
                }
            }
        } else {
            height = Integer.parseInt(next);
            width = height;
            if (encodedValues.size() % height != 0) {
                System.out.println(
                        "Invalid input, make sure that the values can be split evenly into groups of " + height);
                return;
            }

            ArrayList<Double> matrixValues = new ArrayList<>();
            for (int i = 1; i <= height; i++) {
                for (int j = 1; j <= width; j++) {
                    System.out.print("Enter value at position " + i + j + " on the key matrix: ");
                    matrixValues.add(scan.nextDouble());
                }
            }
            keyMatrix = new Matrix(height, width, matrixValues);
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

        double determinant = keyMatrix.calculateDeterminant();
        if (determinant == 0) {
            System.out.println("This matrix is not invertible");
            return;
        }
        ArrayList<Double> cofactors = new ArrayList<>();
        keyMatrix.calculateCofactors(cofactors);
        Matrix cofactorMatrix = new Matrix(height, width, cofactors);
        Matrix inverseMatrix = cofactorMatrix.getTransposeMatrix();
        inverseMatrix.scaleMatrix(1.0 / determinant);
        ArrayList<Double> valuesToConvert = new ArrayList<>();
        for (Matrix m : encodedMatrices) {
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
        if (keyMatrix.calculateDeterminant() == 0) {
            System.out.println("This matrix is not invertible and is invalid.");
            return;
        } 
        System.out.println("The encoding matrix: " + keyMatrix);
        ArrayList<Matrix> messageMatrices = new ArrayList<>();
        convertTextToInt(message, messageMatrices, height);
        ArrayList<Matrix> encodedMessage = new ArrayList<>();
        for (Matrix temMatrix : messageMatrices) {
            encodedMessage.add(temMatrix.encode(keyMatrix));
        }
        ArrayList<Double> encodedDoubles = new ArrayList<>();
        System.out.println("Your encoded message: ");
        for (Matrix temMatrix : encodedMessage) {
            for (double x : temMatrix.toList()) {
                System.out.print(x + " ");
                encodedDoubles.add(x);
            }
        }
        File file = new File(VALUES_FILE);
        File matrixFile = new File(MATRIX_FILE);
        System.out.println();
        try {
            if (matrixFile.createNewFile()) {
                System.out.println("New matrix file instantiated");
            }
            FileOutputStream fos = new FileOutputStream(matrixFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(keyMatrix);

            oos.close();
            fos.close();
        } catch (IOException e) {
            System.out.println("Could not save file");
        }
        try {
            if (file.createNewFile()) {
                System.out.println("New file instantiated");
            }
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(encodedDoubles);

            oos.close();
            fos.close();
        } catch (IOException ex) {
            System.out.println("File saving failed");
        }
        System.out.println();
    }

    public static String convertIntToText(ArrayList<Double> values) {
        String message = "";
        for (double value : values) {
            message += VALUE_TABLE[(int) Math.round(value)];
        }
        return message;
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
