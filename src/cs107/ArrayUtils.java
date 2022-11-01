package cs107;

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

  // ==================================================================================
  // =========================== ARRAY EQUALITY METHODS
  // ===============================
  // ==================================================================================

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
    Boolean sameArray = true;
    //check l'exeption null
    if(a1 == null || a2 == null){
      if(a1 == a2){
        return sameArray;
      }else{
        assert false;
      }
    }
    //check égalité
    for (int i = 0; i < a2.length; i++) {
      if(a1[i] != a2[i]){
        sameArray = false;
      }
    }
    return (sameArray);
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
    Boolean sameArray = true;
    //check null
    if(a1 == null || a2 == null){
      if(a1 == a2){
        return sameArray;
      }else{
        assert false;
      }
    }
    //check égalité
    for(int i=0; i<a2.length;i++){
      if(!equals(a1[i],a2[i])){
        sameArray = false;
      }
    }
    return (sameArray);
  }

  // ==================================================================================
  // ============================ ARRAY WRAPPING METHODS
  // ==============================
  // ==================================================================================

  /**
   * Wrap the given value in an array
   * 
   * @param value (byte) - value to wrap
   * @return (byte[]) - array with one element (value)
   */
  public static byte[] wrap(byte value) {
    byte[] oui = {value};
    return oui;
  }
  // ==================================================================================
  // ========================== INTEGER MANIPULATION METHODS
  // ==========================
  // ==================================================================================

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
    if(bytes.length>4 && bytes != null || bytes ==null){
      assert false;
    }
    int transformedByte = bytes[0];
    for(int i = 0; i<bytes.length-1;i++){
      transformedByte = transformedByte<<8;
      transformedByte = transformedByte | bytes[i+1];
    }
    return transformedByte;
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
    byte[] fromInt = new byte[4];
    for (int i = 0; i<4; i++){
      fromInt[i] = (byte) (value >>> (24-(8*i)));
    }
    return fromInt;
  }

  // ==================================================================================
  // ========================== ARRAY CONCATENATION METHODS
  // ===========================
  // ==================================================================================

  /**
   * Concatenate a given sequence of bytes and stores them in an array
   * 
   * @param bytes (byte ...) - Sequence of bytes to store in the array
   * @return (byte[]) - Array representation of the sequence
   * @throws AssertionError if the input is null
   */
  public static byte[] concat(byte... bytes) {
    assert bytes!=null;
    byte[] ensembleDeByte = new byte[bytes.length];
    for(int i = 0; i<bytes.length; i++){
      ensembleDeByte[i] = bytes[i];
    }
    return ensembleDeByte;
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
    int length = 0;
    assert (tabs!=null);
    for (int i = 0; i < tabs.length; i++){
      if(tabs[i] == null){
        assert false;
      }
      length += tabs[i].length;
    }
    byte[] ensembleDeByte = new byte[length];
    int compter = 0;
    for (int i = 0; i < tabs.length;i++){
      for (int j = 0; j < tabs[i].length ;j++){
        ensembleDeByte[compter] = tabs[i][j];
        compter++;
      }
    }
    return ensembleDeByte;
  }

  // ==================================================================================
  // =========================== ARRAY EXTRACTION METHODS
  // =============================
  // ==================================================================================

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
    assert input!=null;
    assert length>=0;
    int inputLength = input.length;
    assert start<inputLength;
    assert start+length<=inputLength;
    byte[] ensembleExtract = new byte[length];
    for (int i = 0;i < length; i++){
      ensembleExtract[i] = input[start + i];
    }
    return ensembleExtract;
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
    assert (input!=null) && (sizes!=null);
    int sommeSizes = 0;
    for(int i = 0 ; i<sizes.length; i++)
      sommeSizes = sommeSizes + sizes[i];
    assert sommeSizes==input.length;

    byte[][] partitionedByte = new byte[sizes.length][];
    int start = 0;
    for(int j=0; j<sizes.length;j++){
      partitionedByte[j] = extract(input,start,sizes[j]);
      start += sizes[j];
    }

    return partitionedByte;
  }

  // ==================================================================================
  // ============================== ARRAY FORMATTING METHODS
  // ==========================
  // ==================================================================================

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
    //assert
    assert input!=null;
    int tailleLine = input.length;
    int tailleColonne = input[0].length;
    for (int[] line : input) {
      assert line != null;
      assert line.length == tailleColonne;
    }
    //instance variable
    byte[][] byteImage = new byte[tailleLine*tailleColonne][4];
    byte[] argb = new byte[4];
    int countPixel = 0;
    byte byteBleu;
    for (int i = 0;i<tailleLine;i++){
      for (int j = 0;j<tailleColonne;j++){
        argb = fromInt(input[i][j]); // [a,r,g,b] pour each pixel
        byteBleu = argb[3];
        argb[3] = argb[0];
        argb[0] = argb[1];
        argb[1] = argb[2];
        argb[2] = byteBleu; // changement en [r,g,b,a]
        for(int a =0;a<4;a++){
          byteImage[countPixel][a] = argb[a];

        }
        countPixel++;
      }
    }

    return byteImage;
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
    return Helper.fail("Not Implemented");
  }

}