package cs107;

import static cs107.Helper.Image;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * "Quite Ok Image" Decoder
 * 
 * @apiNote Third task of the 2022 Mini Project
 * @author Hamza REMMAL (hamza.remmal@epfl.ch)
 * @version 1.2
 * @since 1.0
 */
public final class QOIDecoder {

    /**
     * DO NOT CHANGE THIS, MORE ON THAT IN WEEK 7.
     */
    private QOIDecoder() {
    }

    // ==================================================================================
    // =========================== QUITE OK IMAGE HEADER
    // ================================
    // =========================== QUITE OK IMAGE HEADER
    // ================================
    // ==================================================================================

    /**
     * Extract useful information from the "Quite Ok Image" header
     * 
     * @param header (byte[]) - A "Quite Ok Image" header
     * @return (int[]) - Array such as its content is {width, height, channels,
     *         color space}
     * @throws AssertionError See handouts section 6.1
     */
    public static int[] decodeHeader(byte[] header) {
        assert header != null && header.length == QOISpecification.HEADER_SIZE;

        byte[][] partitionedHeader = ArrayUtils.partition(header, 4, 4, 4, 1, 1);

        int width = ArrayUtils.toInt(partitionedHeader[1]);
        int height = ArrayUtils.toInt(partitionedHeader[2]);
        byte channels = partitionedHeader[3][0];
        byte colorSpace = partitionedHeader[4][0];

        assert ArrayUtils.equals(partitionedHeader[0], QOISpecification.QOI_MAGIC);
        assert channels == QOISpecification.RGB || channels == QOISpecification.RGBA;
        assert colorSpace == QOISpecification.ALL || colorSpace == QOISpecification.sRGB;

        return ArrayUtils.concat(width, height, (int) channels, (int) colorSpace);
    }

    // ==================================================================================
    // =========================== ATOMIC DECODING METHODS
    // ==============================
    // ==================================================================================

    /**
     * Check if two byte have the same tag on the two first bits
     * 
     * @param chunk (byte) - A data chunk
     * @param tag   (byte) - A tag to check
     * @return (boolean) - Whether the chunk has the right tag
     * @throws AssertionError If the chunk or the tag is null
     */
    public static boolean hasTag(byte chunk, byte tag) {
        byte chunkTag = (byte) (chunk & 0b11000000);

        return chunkTag == tag;
    }

    /**
     * Store the pixel in the buffer and return the number of consumed bytes
     * 
     * @param buffer   (byte[][]) - Buffer where to store the pixel
     * @param input    (byte[]) - Stream of bytes to read from
     * @param alpha    (byte) - Alpha component of the pixel
     * @param position (int) - Index in the buffer
     * @param idx      (int) - Index in the input
     * @return (int) - The number of consumed bytes
     * @throws AssertionError See handouts section 6.2.1
     */
    public static int decodeQoiOpRGB(byte[][] buffer, byte[] input, byte alpha, int position, int idx) {
        assert buffer != null && input != null;
        assert position >= 0 && position < buffer.length;
        assert idx >= 0 && idx + 3 <= input.length;

        byte[] decodedPixel = ArrayUtils.concat(ArrayUtils.extract(input, idx, 3), ArrayUtils.wrap(alpha));
        buffer[position] = decodedPixel;

        return 3;
    }

    /**
     * Store the pixel in the buffer and return the number of consumed bytes
     * 
     * @param buffer   (byte[][]) - Buffer where to store the pixel
     * @param input    (byte[]) - Stream of bytes to read from
     * @param position (int) - Index in the buffer
     * @param idx      (int) - Index in the input
     * @return (int) - The number of consumed bytes
     * @throws AssertionError See handouts section 6.2.2
     */
    public static int decodeQoiOpRGBA(byte[][] buffer, byte[] input, int position, int idx) {
        assert idx >= 0 && idx + 4 <= input.length;

        return decodeQoiOpRGB(buffer, input, input[idx + 3], position, idx) + 1;
    }

    /**
     * Create a new pixel following the "QOI_OP_DIFF" schema.
     * 
     * @param previousPixel (byte[]) - The previous pixel
     * @param chunk         (byte) - A "QOI_OP_DIFF" data chunk
     * @return (byte[]) - The newly created pixel
     * @throws AssertionError See handouts section 6.2.4
     */
    public static byte[] decodeQoiOpDiff(byte[] previousPixel, byte chunk) {
        assert ArrayUtils.isPixel(previousPixel);
        assert hasTag(chunk, QOISpecification.QOI_OP_DIFF_TAG);

        byte selectionByte = (byte) (0b00000011);

        byte rDiff = (byte) (((chunk >> 4) & selectionByte) - 2);
        byte gDiff = (byte) (((chunk >> 2) & selectionByte) - 2);
        byte bDiff = (byte) (((chunk) & selectionByte) - 2);

        return new byte[] { (byte) (previousPixel[0] + rDiff), (byte) (previousPixel[1] + gDiff),
                (byte) (previousPixel[2] + bDiff), previousPixel[3] };
    }

    /**
     * Create a new pixel following the "QOI_OP_LUMA" schema
     * 
     * @param previousPixel (byte[]) - The previous pixel
     * @param data          (byte[]) - A "QOI_OP_LUMA" data chunk
     * @return (byte[]) - The newly created pixel
     * @throws AssertionError See handouts section 6.2.5
     */
    public static byte[] decodeQoiOpLuma(byte[] previousPixel, byte[] data) {
        assert ArrayUtils.isPixel(previousPixel);
        assert data != null;
        assert hasTag(data[0], QOISpecification.QOI_OP_LUMA_TAG);

        byte gDiff = (byte) ((data[0] & 0b00111111) - 32);
        byte rDiff = (byte) (((data[1] >> 4) & 0b00001111) + gDiff - 8);
        byte bDiff = (byte) ((data[1] & 0b00001111) + gDiff - 8);

        return new byte[] { (byte) (previousPixel[0] + rDiff), (byte) (previousPixel[1] + gDiff),
                (byte) (previousPixel[2] + bDiff), previousPixel[3] };
    }

    /**
     * Store the given pixel in the buffer multiple times
     * 
     * @param buffer   (byte[][]) - Buffer where to store the pixel
     * @param pixel    (byte[]) - The pixel to store
     * @param chunk    (byte) - a QOI_OP_RUN data chunk
     * @param position (int) - Index in buffer to start writing from
     * @return (int) - number of written pixels in buffer
     * @throws AssertionError See handouts section 6.2.6
     */
    public static int decodeQoiOpRun(byte[][] buffer, byte[] pixel, byte chunk, int position) {
        assert buffer != null;
        assert position >= 0 && position < buffer.length;
        assert ArrayUtils.isPixel(pixel);
        assert buffer[position].length == 4;
        assert hasTag(chunk, QOISpecification.QOI_OP_RUN_TAG);

        int nbrOfReproduction = (chunk & 0b00111111) + 1;

        for (int i = position; i < position + nbrOfReproduction; ++i) {
            buffer[i] = pixel;
        }

        return nbrOfReproduction - 1;
    }

    // =====================================================================
    // ====================== GLOBAL DECODING METHODS ======================
    // =====================================================================

    /**
     * Return the byte at the given index
     * 
     * @param encodedData (byte[]) - input where the bytes are stored
     * @param wrappedIdx  (int[]) - Index in the input wrapped
     * @return (byte) - The tag
     * @throws AssertionError If encodedData or wrappedIdx are null or if wrappedIdx
     *                        is not valid
     */
    public static byte getByte(byte[] encodedData, int[] wrappedIdx) {
        assert encodedData != null;
        assert wrappedIdx != null;
        assert wrappedIdx[0] >= 0 && wrappedIdx[0] < encodedData.length;

        return encodedData[wrappedIdx[0]];
    }

    /**
     * Add the pixel to the hashtable and add 1 to the position
     * 
     * @param hashTable (byte[][]) - the hashtable where the pixels are stored
     * @param pixel     (byte[]) - Index in the input wrapped
     * @return (byte) - The tag
     * @throws AssertionError If hashTable is null or if the pixel does not match
     *                        the required format or is null
     */
    public static void addToHashTable(byte[][] hashTable, byte[] pixel) {
        assert hashTable != null;
        assert ArrayUtils.isPixel(pixel);

        byte pixelIndex = QOISpecification.hash(pixel);
        hashTable[pixelIndex] = pixel;
    }

    /**
     * Check if there's an rgba tag if yes add the pixel to the buffer and increment
     * the wrappedIdx accordingly
     * 
     * @param input           (byte[]) - The data to decode
     * @param buffer          (byte[][]) - The decoded data
     * @param wrappedPosition (int[]) - Index in the buffer wrapped
     * @param wrappedIdx      (int) - Index in the input wrapped
     * @return (boolean) - Whether the tag corresponded
     */
    public static boolean addDecodedQoiOpRGBA(byte[] input, byte[][] buffer,
            int[] wrappedPosition, int[] wrappedIdx) {
        assert input != null;
        assert wrappedIdx != null;
        assert wrappedIdx[0] >= 0 && wrappedIdx[0] < input.length;

        if (getByte(input, wrappedIdx) == QOISpecification.QOI_OP_RGBA_TAG) {
            ++wrappedIdx[0];
            wrappedIdx[0] += decodeQoiOpRGBA(buffer, input, wrappedPosition[0], wrappedIdx[0]);
            return true;
        } else
            return false;
    }

    /**
     * Check if there's an rgb tag if yes add the pixel to the buffer and increment
     * the wrappedIdx accordingly
     * 
     * @param previousPixel   (byte[]) - Previous pixel
     * @param input           (byte[]) - The data to decode
     * @param buffer          (byte[][]) - The decoded data
     * @param wrappedPosition (int[]) - Index in the buffer wrapped
     * @param wrappedIdx      (int) - Index in the input wrapped
     * @return (boolean) - Whether the tag corresponded
     */
    public static boolean addDecodedQoiOpRGB(byte[] previousPixel, byte[] input, byte[][] buffer,
            int[] wrappedPosition, int[] wrappedIdx) {
        assert ArrayUtils.isPixel(previousPixel);
        assert input != null;
        assert wrappedIdx != null;
        assert wrappedIdx[0] >= 0 && wrappedIdx[0] < input.length;

        if (getByte(input, wrappedIdx) == QOISpecification.QOI_OP_RGB_TAG) {
            ++wrappedIdx[0];
            wrappedIdx[0] += decodeQoiOpRGB(buffer, input, previousPixel[3], wrappedPosition[0], wrappedIdx[0]);
            return true;
        } else
            return false;
    }

    /**
     * Check if there's an luma tag if yes add the pixel to the buffer and increment
     * the wrappedIdx accordingly
     * 
     * @param previousPixel   (byte[]) - Previous pixel
     * @param input           (byte[]) - The data to decode
     * @param buffer          (byte[][]) - The decoded data
     * @param wrappedPosition (int[]) - Index in the buffer wrapped
     * @param wrappedIdx      (int) - Index in the input wrapped
     * @return (boolean) - Whether the tag corresponded
     */
    public static boolean addDecodedQoiOpLuma(byte[] previousPixel, byte[] input, byte[][] buffer,
            int[] wrappedPosition, int[] wrappedIdx) {
        assert ArrayUtils.isPixel(previousPixel);
        assert input != null;
        assert wrappedIdx != null;
        assert wrappedIdx[0] >= 0 && wrappedIdx[0] < input.length;

        if (hasTag(getByte(input, wrappedIdx), QOISpecification.QOI_OP_LUMA_TAG)) {
            buffer[wrappedPosition[0]] = decodeQoiOpLuma(previousPixel, ArrayUtils.extract(input, wrappedIdx[0], 2));
            wrappedIdx[0] += 2;
            return true;
        } else
            return false;
    }

    /**
     * Check if there's an diff tag if yes add the pixel to the buffer and increment
     * the wrappedIdx accordingly
     * 
     * @param previousPixel   (byte[]) - Previous pixel
     * @param input           (byte[]) - The data to decode
     * @param buffer          (byte[][]) - The decoded data
     * @param wrappedPosition (int[]) - Index in the buffer wrapped
     * @param wrappedIdx      (int) - Index in the input wrapped
     * @return (boolean) - Whether the tag corresponded
     */
    public static boolean addDecodedQoiOpDiff(byte[] previousPixel, byte[] input, byte[][] buffer,
            int[] wrappedPosition, int[] wrappedIdx) {
        assert ArrayUtils.isPixel(previousPixel);
        assert input != null;
        assert wrappedIdx != null;
        assert wrappedIdx[0] >= 0 && wrappedIdx[0] < input.length;

        byte chunk = getByte(input, wrappedIdx);

        if (hasTag(chunk, QOISpecification.QOI_OP_DIFF_TAG)) {
            buffer[wrappedPosition[0]] = decodeQoiOpDiff(previousPixel, chunk);
            ++wrappedIdx[0];
            return true;
        } else
            return false;
    }

    /**
     * Check if there's an index tag if yes add the pixel to the buffer and
     * increment the wrappedIdx accordingly
     * 
     * @param previousPixel   (byte[]) - Previous pixel
     * @param input           (byte[]) - The data to decode
     * @param buffer          (byte[][]) - The decoded data
     * @param wrappedPosition (int[]) - Index in the buffer wrapped
     * @param wrappedIdx      (int) - Index in the input wrapped
     * @param hashTable       (byte[][]) - The hash table data
     * 
     * @return (boolean) - Whether the tag corresponded
     */
    public static boolean addDecodedQoiOpIndex(byte[] input, byte[][] buffer,
            int[] wrappedPosition, int[] wrappedIdx, byte[][] hashTable) {
        assert input != null;
        assert wrappedIdx != null;
        assert wrappedIdx[0] >= 0 && wrappedIdx[0] < input.length;
        assert hashTable != null;

        byte chunk = getByte(input, wrappedIdx);

        if (hasTag(chunk, QOISpecification.QOI_OP_INDEX_TAG)) {
            int pixelIndex = chunk & 0b00111111;

            byte[] pixel = hashTable[pixelIndex];
            assert ArrayUtils.isPixel(pixel);

            buffer[wrappedPosition[0]] = pixel;
            return true;
        }

        return false;
    }

    /**
     * Check if there's an run tag if yes add the pixels to the buffer and increment
     * the wrappedIdx accordingly
     * 
     * @param previousPixel   (byte[]) - Previous pixel
     * @param input           (byte[]) - The data to decode
     * @param buffer          (byte[][]) - The decoded data
     * @param wrappedPosition (int[]) - Index in the buffer wrapped
     * @param wrappedIdx      (int) - Index in the input wrapped
     * 
     * @return (boolean) - Whether the tag corresponded
     */
    public static boolean addDecodedQoiOpRun(byte[] previousPixel, byte[] input, byte[][] buffer,
            int[] wrappedPosition, int[] wrappedIdx) {
        assert ArrayUtils.isPixel(previousPixel);
        assert input != null;
        assert wrappedIdx != null;
        assert wrappedIdx[0] >= 0 && wrappedIdx[0] < input.length;

        byte chunk = getByte(input, wrappedIdx);

        if (hasTag(chunk, QOISpecification.QOI_OP_RUN_TAG)) {
            wrappedPosition[0] += decodeQoiOpRun(buffer, previousPixel, chunk, wrappedPosition[0]);
            ++wrappedIdx[0];
            return true;
        }

        return false;
    }

    /**
     * Decode the given data using the "Quite Ok Image" Protocol
     * 
     * @param data   (byte[]) - Data to decode
     * @param width  (int) - The width of the expected output
     * @param height (int) - The height of the expected output
     * @return (byte[][]) - Decoded "Quite Ok Image"
     * @throws AssertionError See handouts section 6.3
     */
    public static byte[][] decodeData(byte[] data, int width, int height) {
        assert data != null;
        assert width > 0 && height > 0;

        int nbrOfPixels = width * height;
        byte[] previousPixel = QOISpecification.START_PIXEL;

        // We wrap those two variable to be able to modify them in functions
        byte[][] hashTable = new byte[64][4];

        byte[][] buffer = new byte[nbrOfPixels][4];

        // Wrapped counter
        int[] wrappedIdx = { 0 };
        int[] wrappedPosition = { 0 };

        while (wrappedIdx[0] < data.length && wrappedPosition[0] < nbrOfPixels) {
            if (addDecodedQoiOpRGBA(data, buffer, wrappedPosition, wrappedIdx)) {
            } else if (addDecodedQoiOpRGB(previousPixel, data, buffer, wrappedPosition, wrappedIdx)) {
            } else if (addDecodedQoiOpLuma(previousPixel, data, buffer, wrappedPosition, wrappedIdx)) {
            } else if (addDecodedQoiOpDiff(previousPixel, data, buffer, wrappedPosition, wrappedIdx)) {
            } else if (addDecodedQoiOpIndex(data, buffer, wrappedPosition, wrappedIdx, hashTable)) {
            } else if (addDecodedQoiOpRun(previousPixel, data, buffer, wrappedPosition, wrappedIdx)) {
            } else {
                System.out.println("Problemos");
            }

            byte[] pixel = buffer[wrappedPosition[0]];
            addToHashTable(hashTable, pixel);
            previousPixel = pixel;

            ++wrappedPosition[0];
        }

        assert wrappedIdx[0] == data.length && wrappedPosition[0] == nbrOfPixels;

        return buffer;
    }

    /**
     * Decode a file using the "Quite Ok Image" Protocol
     * 
     * @param content (byte[]) - Content of the file to decode
     * @return (Image) - Decoded image
     * @throws AssertionError if content is null
     */
    public static Image decodeQoiFile(byte[] content) {
        return Helper.fail("Not Implemented");
    }

}