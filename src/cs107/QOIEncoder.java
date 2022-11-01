package cs107;

/**
 * "Quite Ok Image" Encoder
 * 
 * @apiNote Second task of the 2022 Mini Project
 * @author Hamza REMMAL (hamza.remmal@epfl.ch)
 * @version 1.2
 * @since 1.0
 */
public final class QOIEncoder {

    /**
     * DO NOT CHANGE THIS, MORE ON THAT IN WEEK 7.
     */
    private QOIEncoder() {
    }

    // ==================================================================================
    // ============================ QUITE OK IMAGE HEADER
    // ===============================
    // ==================================================================================

    /**
     * Generate a "Quite Ok Image" header using the following parameters
     * 
     * @param image (Helper.Image) - Image to use
     * @throws AssertionError if the colorspace or the number of channels is
     *                        corrupted or if the image is null.
     *                        (See the "Quite Ok Image" Specification or the
     *                        handouts of the project for more information)
     * @return (byte[]) - Corresponding "Quite Ok Image" Header
     */
    public static byte[] qoiHeader(Helper.Image image) {
        assert image != null;

        assert image.channels() == QOISpecification.RGB || image.channels() == QOISpecification.RGBA;

        assert image.color_space() == QOISpecification.sRGB || image.color_space() == QOISpecification.ALL;

        byte[] MAGIC_NUMBER = { (byte) 113, (byte) 111, (byte) 105, (byte) 102 };

        int[][] imageData = image.data();

        byte[] width = ArrayUtils.fromInt(imageData[0].length);

        byte[] height = ArrayUtils.fromInt(imageData.length);

        byte[] header = ArrayUtils.concat(MAGIC_NUMBER, width, height, ArrayUtils.wrap(image.channels()),
                ArrayUtils.wrap(image.color_space()));

        return header;

    }

    /**
     * Encode the given pixel using the QOI_OP_RGB schema
     * 
     * @param pixel (byte[]) - The Pixel to encode
     * @throws AssertionError if the pixel's length is not 4
     * @return (byte[]) - Encoding of the pixel using the QOI_OP_RGB schema
     */
    public static byte[] qoiOpRGB(byte[] pixel) {
        assert pixel != null;
        assert pixel.length == 4;

        return ArrayUtils.concat(ArrayUtils.wrap(QOISpecification.QOI_OP_RGB_TAG), ArrayUtils.extract(pixel, 0, 3));
    }

    /**
     * Encode the given pixel using the QOI_OP_RGBA schema
     * 
     * @param pixel (byte[]) - The pixel to encode
     * @throws AssertionError if the pixel's length is not 4
     * @return (byte[]) Encoding of the pixel using the QOI_OP_RGBA schema
     */
    public static byte[] qoiOpRGBA(byte[] pixel) {
        assert pixel != null;
        assert pixel.length == 4;

        return ArrayUtils.concat(ArrayUtils.wrap(QOISpecification.QOI_OP_RGBA_TAG), pixel);
    }

    /**
     * Check if the value given use only n bits (on the right of the left)
     * 
     * @param value   (byte) - The value to check
     * @param nbrBits (int) - The nbr the bits allowed from the left (negative for
     *                from the right)
     * @throws AssertionError if n is more than 8
     * @return (boolean) If the value given use at most n bits (on the right of the
     *         left)
     */
    public static boolean useNBites(byte value, int nbrBits) {
        assert nbrBits >= -8 && nbrBits <= 8 && nbrBits != 0;

        byte sign = (byte) (nbrBits / Math.abs(nbrBits));
        int nbrBitsToRemove = 8 - Math.abs(nbrBits);

        int intValue = sign > 0 ? (value >>> nbrBitsToRemove) << nbrBitsToRemove
                : ((value << nbrBitsToRemove) & 0b11111111) >>> nbrBitsToRemove;

        return intValue == value;
    }

    /**
     * Add to the given valur of 6 bits the prefix tag of 2 bits
     * 
     * @param tag   (byte[]) - The tag to add
     * @param pixel (byte[]) - The byte to encode
     * @throws AssertionError if the byte's value use more than 6 bits
     *                        if the tag's value use more than 2 bits
     * @return (byte[]) Encoding of the value by adding the 2 bits tag
     */
    public static byte addTagToValue(byte tag, byte value) {
        Integer intTag = 0;
        assert useNBites(tag, 2);

        // set the two first bits of the value to 0
        assert useNBites(value, -6);

        return (byte) (intTag | value);
    }

    /**
     * Encode the index using the QOI_OP_INDEX schema
     * 
     * @param index (byte) - Index of the pixel
     * @throws AssertionError if the index is outside the range of all possible
     *                        indices
     * @return (byte[]) - Encoding of the index using the QOI_OP_INDEX schema
     */
    public static byte[] qoiOpIndex(byte index) {
        /*
         * In our the function addTagValue is not needed but I put there so the program
         * would handle the change of QOI_OP_INDEX_TAG
         */
        return ArrayUtils.wrap(addTagToValue(QOISpecification.QOI_OP_INDEX_TAG, index));
    }

    /**
     * Encode the difference between 2 pixels using the QOI_OP_DIFF schema
     * 
     * @param diff (byte[]) - The difference between 2 pixels
     * @throws AssertionError if diff doesn't respect the constraints or diff's
     *                        length is not 3
     *                        (See the handout for the constraints)
     * @return (byte[]) - Encoding of the given difference
     */
    public static byte[] qoiOpDiff(byte[] diff) {
        int diffEncoded = 0b00000000;
        for (byte colorDiff : diff) {
            colorDiff += 2;

            assert useNBites(colorDiff, -2);
            diffEncoded = (diffEncoded << 2) & colorDiff;
        }

        return ArrayUtils.wrap(addTagToValue(QOISpecification.QOI_OP_DIFF_TAG, (byte) (diffEncoded)));
    }

    /**
     * Encode the difference between 2 pixels using the QOI_OP_LUMA schema
     * 
     * @param diff (byte[]) - The difference between 2 pixels
     * @throws AssertionError if diff doesn't respect the constraints
     *                        or diff's length is not 3
     *                        (See the handout for the constraints)
     * @return (byte[]) - Encoding of the given difference
     */
    public static byte[] qoiOpLuma(byte[] diff) {
        return Helper.fail("Not Implemented");
    }

    /**
     * Encode the number of similar pixels using the QOI_OP_RUN schema
     * 
     * @param count (byte) - Number of similar pixels
     * @throws AssertionError if count is not between 0 (exclusive) and 63
     *                        (exclusive)
     * @return (byte[]) - Encoding of count
     */
    public static byte[] qoiOpRun(byte count) {
        return Helper.fail("Not Implemented");
    }

    // ==================================================================================
    // ============================== GLOBAL ENCODING METHODS
    // ==========================
    // ==================================================================================

    /**
     * Encode the given image using the "Quite Ok Image" Protocol
     * (See handout for more information about the "Quite Ok Image" protocol)
     * 
     * @param image (byte[][]) - Formatted image to encode
     * @return (byte[]) - "Quite Ok Image" representation of the image
     */
    public static byte[] encodeData(byte[][] image) {
        return Helper.fail("Not Implemented");
    }

    /**
     * Creates the representation in memory of the "Quite Ok Image" file.
     * 
     * @apiNote THE FILE IS NOT CREATED YET, THIS IS JUST ITS REPRESENTATION.
     *          TO CREATE THE FILE, YOU'LL NEED TO CALL Helper::write
     * @param image (Helper.Image) - Image to encode
     * @return (byte[]) - Binary representation of the "Quite Ok File" of the image
     * @throws AssertionError if the image is null
     */
    public static byte[] qoiFile(Helper.Image image) {
        return Helper.fail("Not Implemented");
    }

}