/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2015 Serge Rieder (serge@jkiss.org)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (version 2)
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.jkiss.dbeaver.utils;

import org.jkiss.dbeaver.core.Log;
import org.jkiss.utils.CommonUtils;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * General non-ui utility methods
 */
public class GeneralUtils {

    static final Log log = Log.getLog(GeneralUtils.class);

    public static final String DEFAULT_FILE_CHARSET_NAME = "UTF-8";
    public static final Charset UTF8_CHARSET = Charset.forName("UTF-8");
    public static final Charset DEFAULT_FILE_CHARSET = UTF8_CHARSET;
    public static final Charset ASCII_CHARSET = Charset.forName("US-ASCII");
    public static final String[] byteToHex = new String[256];
    public static final char[] nibbleToHex = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    static final Map<String, byte[]> BOM_MAP = new HashMap<String, byte[]>();
    static final char[] HEX_CHAR_TABLE = {
      '0', '1', '2', '3',
      '4', '5', '6', '7',
      '8', '9', 'a', 'b',
      'c', 'd', 'e', 'f'
    };

    static {
        // Compose byte to hex map
        for (int i = 0; i < 256; ++i) {
            byteToHex[i] = Character.toString(nibbleToHex[i >>> 4]) + nibbleToHex[i & 0x0f];
        }
    }


    public static String getDefaultFileEncoding()
    {
        return System.getProperty("file.encoding", DEFAULT_FILE_CHARSET_NAME);
    }

    public static String getDefaultConsoleEncoding()
    {
        String consoleEncoding = System.getProperty("console.encoding");
        if (CommonUtils.isEmpty(consoleEncoding)) {
            consoleEncoding = getDefaultFileEncoding();
        }
        return consoleEncoding;
    }

    public static String getDefaultLineSeparator()
    {
        return System.getProperty("line.separator", "\n");
    }

    public static byte[] getCharsetBOM(String charsetName)
    {
        return BOM_MAP.get(charsetName.toUpperCase());
    }

    public static void writeByteAsHex(Writer out, byte b) throws IOException
    {
        int v = b & 0xFF;
        out.write(HEX_CHAR_TABLE[v >>> 4]);
        out.write(HEX_CHAR_TABLE[v & 0xF]);
    }

    public static void writeBytesAsHex(Writer out, byte[] buf, int off, int len) throws IOException
    {
        for (int i = 0; i < len; i++) {
            byte b = buf[off + i];
            int v = b & 0xFF;
            out.write(HEX_CHAR_TABLE[v >>> 4]);
            out.write(HEX_CHAR_TABLE[v & 0xF]);
        }
    }

    public static String convertToString(byte[] bytes, int offset, int length)
    {
        char[] chars = new char[length];
        for (int i = offset; i < offset + length; i++) {
            int b = bytes[i];
            if (b < 0) {
                b = -b + 127;
            }
            chars[i - offset] = (char) b;
        }
        return new String(chars);
    }

    /**
     * Converts string to byte array.
     * This is loosy algorithm because it gets only first byte from each char.
     *
     * @param strValue
     * @return
     */
    public static byte[] convertToBytes(String strValue)
    {
        int length = strValue.length();
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            int c = strValue.charAt(i) & 255;
            if (c > 127) {
                c = -(c - 127);
            }
            bytes[i] = (byte)c;
        }
        return bytes;
    }

    public static Object makeDisplayString(Object object)
    {
        if (object == null) {
            return ""; //$NON-NLS-1$
        }
        if (object instanceof Number) {
            return NumberFormat.getInstance().format(object);
        }
        Class<?> eClass = object.getClass();
        if (eClass.isArray()) {
            if (eClass == byte[].class)
                return Arrays.toString((byte[]) object);
            else if (eClass == short[].class)
                return Arrays.toString((short[]) object);
            else if (eClass == int[].class)
                return Arrays.toString((int[]) object);
            else if (eClass == long[].class)
                return Arrays.toString((long[]) object);
            else if (eClass == char[].class)
                return Arrays.toString((char[]) object);
            else if (eClass == float[].class)
                return Arrays.toString((float[]) object);
            else if (eClass == double[].class)
                return Arrays.toString((double[]) object);
            else if (eClass == boolean[].class)
                return Arrays.toString((boolean[]) object);
            else { // element is an array of object references
                return Arrays.deepToString((Object[]) object);
            }
        }
        return object;
    }

    public static Object convertString(String value, Class<?> valueType)
    {
        try {
            if (CommonUtils.isEmpty(value)) {
                return null;
            }
            if (valueType == null || CharSequence.class.isAssignableFrom(valueType)) {
                return value;
            } else if (valueType == Boolean.class || valueType == Boolean.TYPE) {
                return Boolean.valueOf(value);
            } else if (valueType == Long.class) {
                return Long.valueOf(value);
            } else if (valueType == Long.TYPE) {
                return Long.parseLong(value);
            } else if (valueType == Integer.class) {
                return new Integer(value);
            } else if (valueType == Integer.TYPE) {
                return Integer.parseInt(value);
            } else if (valueType == Short.class) {
                return Short.valueOf(value);
            } else if (valueType == Short.TYPE) {
                return Short.parseShort(value);
            } else if (valueType == Byte.class) {
                return Byte.valueOf(value);
            } else if (valueType == Byte.TYPE) {
                return Byte.parseByte(value);
            } else if (valueType == Double.class) {
                return Double.valueOf(value);
            } else if (valueType == Double.TYPE) {
                return Double.parseDouble(value);
            } else if (valueType == Float.class) {
                return Float.valueOf(value);
            } else if (valueType == Float.TYPE) {
                return Float.parseFloat(value);
            } else if (valueType == BigInteger.class) {
                return new BigInteger(value);
            } else if (valueType == BigDecimal.class) {
                return new BigDecimal(value);
            } else {
                return value;
            }
        } catch (RuntimeException e) {
            log.error(e);
            return value;
        }
    }

    public static Throwable getRootCause(Throwable ex) {
        for (Throwable e = ex; ; e = e.getCause()) {
            if (e.getCause() == null) {
                return e;
            }
        }
    }

}
