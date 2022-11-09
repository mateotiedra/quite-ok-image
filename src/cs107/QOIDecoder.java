package cs107;

import static cs107.Helper.Image;

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
        assert idx >= 0 && idx + 3 < input.length;
        assert input.length >= 3;

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
        return Helper.fail("Not Implemented");
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

    // ==================================================================================
    // ========================= GLOBAL DECODING METHODS
    // ================================
    // ==================================================================================

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
        return Helper.fail("Not Implemented");
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