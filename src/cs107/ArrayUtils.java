package cs107;

import java.util.ArrayList;

/**
 * Utility class to manipulate arrays.
 * 
 * @apiNote First Task of the 2022 Mini Project
 * @author Hamza REMMAL (hamza.remmal@epfl.ch)
 * @version 1.2
 * @since 1.0
 */
public final class ArrayUtils {

  /**
   * DO NOT CHANGE THIS, MORE ON THAT IN WEEK 7.
   */
  private ArrayUtils() {
  }

  /**
   * Check if the content of both arrays is the same
   * 
   * @param a1 (byte[]) - First array
   * @param a2 (byte[]) - Second array
   * @return (boolean) - true if both arrays have the same content (or both null),
   *         false otherwise
   * @throws AssertionError if one of the parameters is null
   */
  public static boolean equals(byte[] a1, byte[] a2) {
    if (a1 == null || a2 == null)
      if (a1 == a2)
        return true;
      else
        assert false;

    // If there is any byte that is not the same return false
    for (int i = 0; i < a1.length; ++i)
      if (a1[i] != a2[i])
        return false;

    return true;
  }

  /**
   * Check if the content of both arrays is the same
   * 
   * @param a1 (byte[][]) - First array
   * @param a2 (byte[][]) - Second array
   * @return (boolean) - true if both arrays have the same content (or both null),
   *         false otherwise
   * @throws AssertionError if one of the parameters is null
   */
  public static boolean equals(byte[][] a1, byte[][] a2) {
    if (a1 == null || a2 == null)
      if (a1 == a2)
        return true;
      else
        assert false;

    // If there is any array that is not the same return false
    for (int i = 0; i < a1.length; ++i)
      if (!equals(a1[i], a2[i]))
        return false;

    return true;
  }

  /**
   * Wrap the given value in an array
   * 
   * @param value (byte) - value to wrap
   * @return (byte[]) - array with one element (value)
   */
  public static byte[] wrap(byte value) {
    return new byte[] { value };
  }

  /**
   * Create an Integer using the given array. The input needs to be considered
   * as "Big Endian"
   * (See handout for the definition of "Big Endian")
   * 
   * @param bytes (byte[]) - Array of 4 bytes
   * @return (int) - Integer representation of the array
   * @throws AssertionError if the input is null or the input's length is
   *                        different from 4
   */
  public static int toInt(byte[] bytes) {
    assert bytes != null && bytes.length == 4;

    int value = 0;
    for (int i = 0; i < bytes.length; i++) {
      value = value << 8;
      value = value | bytes[i];
    }

    return value;
  }

  /**
   * Separate the Integer (word) to 4 bytes. The Memory layout of this integer is
   * "Big Endian"
   * (See handout for the definition of "Big Endian")
   * 
   * @param value (int) - The integer
   * @return (byte[]) - Big Endian representation of the integer
   */
  public static byte[] fromInt(int value) {
    byte[] bytes = new byte[4];

    for (int i = 0; i < bytes.length; i++) {
      bytes[3 - i] = (byte) (value >> (8 * i));
    }

    return bytes;
  }

  /**
   * Concatenate a given sequence of bytes and stores them in an array
   * 
   * @param bytes (byte ...) - Sequence of bytes to store in the array
   * @return (byte[]) - Array representation of the sequence
   * @throws AssertionError if the input is null
   */
  public static byte[] concat(byte... bytes) {
    assert bytes != null && bytes.length != 0;

    byte[] bytesTab = new byte[bytes.length];
    for (int i = 0; i < bytes.length; i++) {
      bytesTab[i] = bytes[i];
    }
    return bytesTab;
  }

  /**
   * Concatenate a given sequence of arrays into one array
   * 
   * @param tabs (byte[] ...) - Sequence of arrays
   * @return (byte[]) - Array representation of the sequence
   * @throws AssertionError if the input is null
   *                        or one of the inner arrays of input is null.
   */
  public static byte[] concat(byte[]... tabs) {
    assert tabs != null;
    ArrayList<Byte> bytesList = new ArrayList<Byte>();
    for (int i = 0; i < tabs.length; i++) {
      assert tabs[i] != null;
      for (int j = 0; j < tabs[i].length; j++) {
        bytesList.add(tabs[i][j]);
      }
    }
    byte[] bytesArray = new byte[bytesList.size()];
    for (int i = 0; i < bytesArray.length; i++) {
      bytesArray[i] = bytesList.get(i);
    }

    return bytesArray;
  }

  /**
   * Extract an array from another array
   * 
   * @param input  (byte[]) - Array to extract from
   * @param start  (int) - Index in the input array to start the extract from
   * @param length (int) - The number of bytes to extract
   * @return (byte[]) - The extracted array
   * @throws AssertionError if the input is null or start and length are invalid.
   *                        start + length should also be smaller than the input's
   *                        length
   */
  public static byte[] extract(byte[] input, int start, int length) {
    assert input != null;
    assert start >= 0 && start < input.length;
    assert length >= 0;
    assert start + length <= input.length;

    byte[] subTab = new byte[length];

    for (int i = 0; i < length; i++) {
      subTab[i] = input[start + i];
    }
    return subTab;
  }

  /**
   * Create a partition of the input array.
   * (See handout for more information on how this method works)
   * 
   * @param input (byte[]) - The original array
   * @param sizes (int ...) - Sizes of the partitions
   * @return (byte[][]) - Array of input's partitions.
   *         The order of the partition is the same as the order in sizes
   * @throws AssertionError if one of the parameters is null
   *                        or the sum of the elements in sizes is different from
   *                        the input's length
   */
  public static byte[][] partition(byte[] input, int... sizes) {
    assert input != null;
    assert sizes != null;
    assert input.length == sumOf(sizes);

    byte[][] partitionedBytesTabs = new byte[sizes.length][];
    int start = 0;

    for (int i = 0; i < sizes.length; i++) {
      partitionedBytesTabs[i] = extract(input, start, sizes[i]);
      start += sizes[i];
    }
    return partitionedBytesTabs;
  }

  /**
   * Return the sum of every elements of the table
   * 
   * @param input (int[]) - The table to sum
   * @return (int) - Sum
   * 
   * @throws AssertionError if the table is null of if the
   */
  public static int sumOf(int[] input) {
    int sum = 0;
    for (int i : input) {
      sum += i;
    }
    return sum;
  }

  /**
   * Format a 2-dim integer array
   * where each dimension is a direction in the image to
   * a 2-dim byte array where the first dimension is the pixel
   * and the second dimension is the channel.
   * See handouts for more information on the format.
   * 
   * @param input (int[][]) - image data
   * @return (byte [][]) - formatted image data
   * @throws AssertionError if the input is null
   *                        or one of the inner arrays of input is null
   */
  public static byte[][] imageToChannels(int[][] input) {
    assert input != null;

    ArrayList<byte[]> pixelsList = new ArrayList<byte[]>();

    for (int[] line : input) {
      assert line != null;

      for (int pixelIntFormatted : line) {
        byte[] pixelBytesFormatted = fromInt(pixelIntFormatted);
        // Put the alpha channel at the end to match the required format
        pixelBytesFormatted = concat(extract(pixelBytesFormatted, 1, 3), wrap(pixelBytesFormatted[0]));

        pixelsList.add(pixelBytesFormatted);
      }
    }

    byte[][] pixelsTab = new byte[pixelsList.size()][4];

    for (int i = 0; i < pixelsTab.length; i++) {
      pixelsTab[i] = pixelsList.get(i);
    }

    return pixelsTab;
  }

  /**
   * Format a 2-dim byte array where the first dimension is the pixel
   * and the second is the channel to a 2-dim int array where the first
   * dimension is the height and the second is the width
   * 
   * @param input  (byte[][]) : linear representation of the image
   * @param height (int) - Height of the resulting image
   * @param width  (int) - Width of the resulting image
   * @return (int[][]) - the image data
   * @throws AssertionError if the input is null
   *                        or one of the inner arrays of input is null
   *                        or input's length differs from width * height
   *                        or height is invalid
   *                        or width is invalid
   */
  public static int[][] channelsToImage(byte[][] input, int height, int width) {
    assert input != null;
    assert input.length == width * height;
    assert width > 0 && height > 0;

    int[][] pixelsTabImageFormatted = new int[height][width];

    for (int i = 0; i < height; ++i) {
      assert input != null && input[i].length == 4;

      for (int j = 0; j < width; ++j) {
        byte[] pixelBytesFormatted = input[j + i * width];
        // Put the alpha back on the first place
        pixelBytesFormatted = concat(wrap(pixelBytesFormatted[3]), extract(pixelBytesFormatted, 0, 3));
        pixelsTabImageFormatted[i][j] = toInt(pixelBytesFormatted);
      }

    }

    return pixelsTabImageFormatted;
  }

}