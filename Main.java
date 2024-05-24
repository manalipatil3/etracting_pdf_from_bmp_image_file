import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static void reverseArray(int[] arr)
    {
        int n = arr.length;
        for (int i = 0; i < n / 2; i++)
        {
            int temp = arr[i];
            arr[i] = arr[n - 1 - i];
            arr[n - 1 - i] = temp;
        }
    }
    private static boolean checkIndicatorBits(int[] lsbArray)
    {
        boolean indicatorFound = false;
        for (int i = 0; i < 64; i += 8)
        {
            int value = 0;
            for (int j = 0; j < 8; j++)
            {
                value = (value << 1) | lsbArray[i + j];
            }
            String hexValue = String.format("%02X", value);
            if (hexValue.equals("A5"))
            {
                indicatorFound = true;
                break;
            }
        }
        return indicatorFound;
    }
    private static byte[] convertListToByteArray(List<Integer> lowerBitsList)
    {
        int listSize = lowerBitsList.size();
        int byteArraySize = (listSize + 7) / 8;
        byte[] byteArray = new byte[byteArraySize];
        int bitIndex = 0;
        int byteIndex = 0;
        int byteValue=0;
        List<Integer> reversedBits = new ArrayList<>();

        for (int i = 0; i < listSize; i += 8) {
            reversedBits.clear();
            int endIndex = Math.min(i + 8, listSize);
            for (int j = endIndex - 1; j >= i; j--) {
                reversedBits.add(lowerBitsList.get(j));
            }
            for (int bit : reversedBits) {
                byteValue = (byteValue << 1) | bit;
                bitIndex++;
                if (bitIndex == 8) {
                    byteArray[byteIndex++] = (byte) byteValue;
                    byteValue = 0;
                    bitIndex = 0;
                }
            }
        }
        if (bitIndex > 0) {
            byteArray[byteIndex] = (byte) (byteValue << (8 - bitIndex));
        }
        return byteArray;
    }

    public static void main(String[] args) {
        try {
            if (args.length != 2) {
                System.out.println("Please provide input_file and output_file");
                System.exit(1);
            }
            File bmpFile = new File(args[0]);
            File outputFile = new File(args[1]);

            if (!args[0].toLowerCase().endsWith(".bmp")) {
                throw new IllegalArgumentException();
            }
            if (!args[1].toLowerCase().endsWith(".pdf")) {
                throw new IllegalArgumentException();
            }
            FileInputStream fileInputStream = new FileInputStream(bmpFile);
            long skipped = fileInputStream.skip(100);
            if (skipped != 100)
            {
                System.err.println("Error skipping the initial 100 bytes");
                return;
            }
            int[] lsbArray = new int[64];
            byte[] buffer = new byte[64];
            int bytesRead = fileInputStream.read(buffer);
            if (bytesRead != 64)
            {
                System.err.println("Error reading the next 64 bytes");
                return;
            }
            for (int byteIndex = 0; byteIndex < 64; byteIndex++)
            {
                byte b = buffer[byteIndex];
                lsbArray[byteIndex] = (b & 0x01);
            }
            boolean indicatorFound = checkIndicatorBits(lsbArray);
            if (indicatorFound)
            {
                //System.out.println("Indicator bits sequence found");
                buffer = new byte[27];
                bytesRead = fileInputStream.read(buffer);
                if (bytesRead != 27)
                {
                    System.err.println("Error reading the next 27 bytes");
                    return;
                }
                int[] lowerBitsArray = new int[27];
                for (int i = 0; i < 27; i++)
                {
                    lowerBitsArray[i] = buffer[i] & 0x01;
                }
                reverseArray(lowerBitsArray);

                int value = 0;
                for (int bit : lowerBitsArray) {
                    value = (value << 1) | bit;
                }

                //System.out.println("Integer value of the reversed lower bits: " + value);
                List<Integer> lowerBitsList = new ArrayList<>();
                int bytesConsumed = 0;
                while (bytesConsumed < value*8)
                {
                    int b = fileInputStream.read();
                    if (b == -1)
                    {
                        System.err.println("Error: Reached end of file before reading the expected number of bytes");
                        return;
                    }
                    lowerBitsList.add(b & 0x01);
                    bytesConsumed++;
                }
                //System.out.print("Byte array formed from the lower bits: ");
                byte[] byteArray = convertListToByteArray(lowerBitsList);
//                String stringFromBytes = new String(byteArray);
//                //System.out.println("String formed from the byte array: " + stringFromBytes);
                FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
                fileOutputStream.write(byteArray);
                fileOutputStream.close();
                //System.out.println("String written to output.txt file.");
            } else
            {
                System.out.println("Indicator bits sequence not found");
            }

            fileInputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (IllegalArgumentException e){
            System.err.println("Please provide the correct bmp file as input and provide correct output file name");
        }
    }


}