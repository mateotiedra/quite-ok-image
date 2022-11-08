package cs107;

import java.util.ArrayList;
import java.util.Arrays;

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

        int[][] imageData = image.data();

        byte[] width = ArrayUtils.fromInt(imageData[0].length);

        byte[] height = ArrayUtils.fromInt(imageData.length);

        byte[] header = ArrayUtils.concat(QOISpecification.QOI_MAGIC, width, height, ArrayUtils.wrap(image.channels()),
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
        assert ArrayUtils.isPixel(pixel);

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
        assert ArrayUtils.isPixel(pixel);

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
     * @param value (byte[]) - The byte to encode
     * @throws AssertionError if the byte's value use more than 6 bits
     *                        if the tag's value use more than 2 bits
     * @return (byte[]) Encoding of the value by adding the 2 bits tag
     */
    public static byte addTagToValue(byte tag, byte value) {
        assert useNBites(tag, 2);

        // set the two first bits of the value to 0
        assert useNBites(value, -6);

        return (byte) (tag | value);
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
        assert diff != null;
        assert diff.length == 3;

        int diffEncoded = 0b00000000;
        for (byte colorDiff : diff) {
            colorDiff += 2;
            assert useNBites(colorDiff, -2);

            diffEncoded = (diffEncoded << 2) | colorDiff;
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
        assert diff != null;
        assert diff.length == 3;

        int[] diffEncoded = { (byte) 0b00000000, (byte) 0b00000000 };

        for (int i = 0; i < 3; i++) {
            if (i == 1) {
                // The green
                byte colorDiff = (byte) (diff[i] + 32);
                assert useNBites(colorDiff, -6);

                diffEncoded[0] = colorDiff;
            } else {
                // The red and blue
                byte colorDiff = (byte) ((diff[i] - diff[1]) + 8);
                assert useNBites(colorDiff, -4);

                diffEncoded[1] = (diffEncoded[1] << 4) | colorDiff;
            }
        }

        byte[] encoding = ArrayUtils.concat(addTagToValue(QOISpecification.QOI_OP_LUMA_TAG, (byte) (diffEncoded[0])),
                (byte) (diffEncoded[1]));
        return encoding;
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
        assert count >= 1 && count <= 62;

        return ArrayUtils.wrap(addTagToValue(QOISpecification.QOI_OP_RUN_TAG, (byte) (count - 1)));
    }

    // ==========================================================================
    // ============================== GLOBAL ENCODING METHODS ===================
    // ==========================================================================

    /**
     * If pixel == previous pixel, increase the counter and test if a QOI_OP_RUN can
     * be built (counter reaching the limit of 62 or last pixel reached), then go to
     * the next pixel otherwise test if a QOI_OP_RUN can be built and go to the next
     * step
     * 
     * @param previousPixel (byte[]) - previous pixel
     * @param pixel         (byte[]) - pixel to compare
     * @param qoiOps        (ArrayList<byte[]>) - ArrayList to which the OP bytes
     *                      will be added
     * @param wrappedCount  (int[]) - the counter wrapped
     * @return (boolean) - must stop to add other bytes for this pixel after that
     */
    public static boolean addQoiOpRun(byte[] previousPixel, byte[] pixel, ArrayList<byte[]> qoiOps, int[] wrappedCount,
            boolean lastIteration) {
        assert ArrayUtils.isPixel(previousPixel) && ArrayUtils.isPixel(pixel);
        assert qoiOps != null && wrappedCount != null;

        if (ArrayUtils.equals(pixel, previousPixel)) {
            if (++wrappedCount[0] == 62) {
                qoiOps.add(qoiOpRun((byte) 62));
                wrappedCount[0] = 0;
            }
            if (!lastIteration)
                return true;
        } else if (wrappedCount[0] > 0) {
            qoiOps.add(qoiOpRun((byte) wrappedCount[0]));
            wrappedCount[0] = 0;
        }

        if (lastIteration && wrappedCount[0] > 0) {
            qoiOps.add(qoiOpRun((byte) wrappedCount[0]));
            return true;
        }

        return false;
    }

    /**
     * If pixel is in the hash table, create a QOI_OP_INDEX block and go to the next
     * pixel, otherwise add the pixel to the hash table and go to the next step
     * 
     * @param pixel     (byte[]) - pixel to compare
     * @param qoiOps    (ArrayList<byte[]>) - ArrayList to which the OP bytes
     * @param hashTable (byte[][]) - pixel to compare
     * @return (boolean) - must stop to add other bytes for this pixel after that
     */
    public static boolean addQoiOpIndex(byte[] pixel, ArrayList<byte[]> qoiOps, byte[][] hashTable) {
        assert ArrayUtils.isPixel(pixel);
        assert qoiOps != null && hashTable != null;

        byte pixelIndex = QOISpecification.hash(pixel);

        if (ArrayUtils.equals(hashTable[pixelIndex], pixel)) {
            qoiOps.add(qoiOpIndex(pixelIndex));
            return true;
        } else {
            hashTable[pixelIndex] = pixel;
        }

        return false;
    }

    /**
     * If the alpha channel is the same between the current and the previous pixel,
     * and the difference between these pixels is small (according to the criteria
     * specific to QOI_OP_DIFFa blocks) then create a QOI_OP_DIFF block and go to
     * the next pixel, otherwise go to the next step.
     * 
     * @param previousPixel (byte[]) - previous pixel
     * @param pixel         (byte[]) - pixel to compare
     * @param qoiOps        (ArrayList<byte[]>) - ArrayList to which the OP bytes
     *                      will be added
     * @return (boolean) - must stop to add other bytes for this pixel after that
     */
    public static boolean addQoiOpDiff(byte[] previousPixel, byte[] pixel, ArrayList<byte[]> qoiOps) {
        assert ArrayUtils.isPixel(previousPixel) && ArrayUtils.isPixel(pixel);
        assert qoiOps != null;

        // Not the same Alpha
        if (pixel[3] != previousPixel[3])
            return false;

        byte[] rgbDiff = new byte[3];

        for (int i = 0; i < pixel.length - 1; ++i) {
            byte diff = (byte) (pixel[i] - previousPixel[i]);

            // A too great difference
            if (diff <= -3 || diff >= 2)
                return false;

            rgbDiff[i] = (byte) (diff);
        }

        qoiOps.add(qoiOpDiff(rgbDiff));

        return true;
    }

    /**
     * If the alpha channel is the same between the current and the previous pixel,
     * and the difference between these pixels matches the criteria of the
     * QOI_OP_LUMA blocks, then create a block of this type and
     * go to the next pixel, otherwise go to the next step
     * 
     * @param previousPixel (byte[]) - previous pixel
     * @param pixel         (byte[]) - pixel to compare
     * @param qoiOps        (ArrayList<byte[]>) - ArrayList to which the OP bytes
     *                      will be added
     * @return (boolean) - must stop to add other bytes for this pixel after that
     */
    public static boolean addQoiOpLuma(byte[] previousPixel, byte[] pixel, ArrayList<byte[]> qoiOps) {
        assert ArrayUtils.isPixel(previousPixel) && ArrayUtils.isPixel(pixel);
        assert qoiOps != null;

        // Not the same Alpha
        if (pixel[3] != previousPixel[3])
            return false;

        // Check for the green
        byte gDiff = (byte) (pixel[1] - previousPixel[1]);
        if (gDiff <= -33 || gDiff >= 32)
            return false;

        // Check for the red
        byte rDiff = (byte) (pixel[0] - previousPixel[0]);
        if (rDiff - gDiff <= -9 || rDiff - gDiff >= 8)
            return false;

        // Check for the blue
        byte bDiff = (byte) (pixel[2] - previousPixel[2]);
        if (bDiff - gDiff <= -9 || bDiff - gDiff >= 8)
            return false;

        qoiOps.add(qoiOpLuma(new byte[] { rDiff, gDiff, bDiff }));

        return true;
    }

    /**
     * If the alpha channel is the same between the current pixel and the previous
     * one, create a QOI_OP_RGB block (the alpha channel value being the one common
     * to both pixels) and go to the next pixel, otherwise
     * go to the next step
     * 
     * @param previousPixel (byte[]) - previous pixel
     * @param pixel         (byte[]) - pixel to compare
     * @param qoiOps        (ArrayList<byte[]>) - ArrayList to which the OP bytes
     *                      will be added
     * @return (boolean) - must stop to add other bytes for this pixel after that
     */
    public static boolean addQoiOpRGB(byte[] previousPixel, byte[] pixel, ArrayList<byte[]> qoiOps) {
        assert ArrayUtils.isPixel(previousPixel) && ArrayUtils.isPixel(pixel);
        assert qoiOps != null;

        // Not the same Alpha
        if (pixel[3] != previousPixel[3])
            return false;

        qoiOps.add(qoiOpRGB(pixel));

        return true;
    }

    /**
     * Create a QOI_OP_RGBA block
     * 
     * @param previousPixel (byte[]) - previous pixel
     * @param pixel         (byte[]) - pixel to compare
     * @param qoiOps        (ArrayList<byte[]>) - ArrayList to which the OP bytes
     *                      will be added
     * @return (boolean) - must stop to add other bytes for this pixel after that
     */
    public static boolean addQoiOpRGBA(byte[] previousPixel, byte[] pixel, ArrayList<byte[]> qoiOps) {
        assert ArrayUtils.isPixel(previousPixel) && ArrayUtils.isPixel(pixel);
        assert qoiOps != null;

        qoiOps.add(qoiOpRGBA(pixel));

        return true;
    }

    /**
     * Encode the given image using the "Quite Ok Image" Protocol
     * (See handout for more information about the "Quite Ok Image" protocol)
     * 
     * @param image (byte[][]) - Formatted image to encode
     * @return (byte[]) - "Quite Ok Image" representation of the image
     */
    public static byte[] encodeData(byte[][] image) {
        assert image != null;
        byte[] previousPixel = QOISpecification.START_PIXEL;

        // We wrap those two variable to be able to modify them in functions
        byte[][] hashTable = new byte[64][4];
        int[] wrappedCount = { 0 };

        ArrayList<byte[]> qoiOps = new ArrayList<byte[]>();

        for (int i = 0; i < image.length; ++i) {
            byte[] pixel = image[i];

            if (addQoiOpRun(previousPixel, pixel, qoiOps, wrappedCount, i == image.length - 1)) {
                // System.out.println("same pixel as the previous one (if last iteration of it :
                // op run added)");
            } else if (addQoiOpIndex(pixel, qoiOps, hashTable)) {
                // System.out.println("op index added");
            } else if (addQoiOpDiff(previousPixel, pixel, qoiOps)) {
                // System.out.println("op diff added");
            } else if (addQoiOpLuma(previousPixel, pixel, qoiOps)) {
                // System.out.println("op luma added");
            } else if (addQoiOpRGB(previousPixel, pixel, qoiOps)) {
                // System.out.println("op rgb added");
            } else if (addQoiOpRGBA(previousPixel, pixel, qoiOps)) {
                // System.out.println("op rgba added");
            }

            previousPixel = pixel;
        }

        return ArrayUtils.concat(qoiOps.toArray(new byte[0][]));
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
        assert image != null;

        byte[] header = qoiHeader(image);
        byte[] imageData = encodeData(ArrayUtils.imageToChannels(image.data()));

        return ArrayUtils.concat(header, imageData, QOISpecification.QOI_EOF);
    }

}