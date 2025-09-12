package packets;


import packetUtils.ObfuscatedNames;

import java.lang.reflect.Field;

public class BufferMethods {

	public static void setOffset(Object bufferInstance, int offset) {
		try {
			Field offsetField = bufferInstance.getClass().getField(ObfuscatedNames.bufferOffsetField);
			offsetField.setAccessible(true);
			offsetField.setInt(bufferInstance, offset);
			offsetField.setAccessible(false);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public static int getOffset(Object bufferInstance) {
		try {
			Field offsetField = bufferInstance.getClass().getField(ObfuscatedNames.bufferOffsetField);
			offsetField.setAccessible(true);
			int offset = offsetField.getInt(bufferInstance);
			offsetField.setAccessible(false);
			return offset;
		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public static void setArray(Object bufferInstance, byte[] array) {
		try {
			Field arrayField = bufferInstance.getClass().getField(ObfuscatedNames.bufferArrayField);
			arrayField.setAccessible(true);
			arrayField.set(bufferInstance, array);
			arrayField.setAccessible(false);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public static byte[] getArray(Object bufferInstance) {
		try {
			Field arrayField = bufferInstance.getClass().getField(ObfuscatedNames.bufferArrayField);
			arrayField.setAccessible(true);
			byte[] array = (byte[]) arrayField.get(bufferInstance);
			arrayField.setAccessible(false);
			return array;
		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void writeValue(String writeDescription, int value, Object bufferInstance) {
		int writeTypeMagnitude = writeDescription.contains("v") ? 0 : Integer.parseInt(writeDescription.substring(1).trim());
		byte[] arr = getArray(bufferInstance);
		int index = nextIndex(getOffset(bufferInstance));
		setOffset(bufferInstance, index);
		index = index * Integer.parseInt(ObfuscatedNames.indexMultiplier) - 1;
		//System.out.println("Index: " + index);
		switch (writeDescription.charAt(0)) {
			case 's':
				setArray(bufferInstance, writeSub(writeTypeMagnitude, value, arr, index));
				break;
			case 'a':
				setArray(bufferInstance, writeAdd(writeTypeMagnitude, value, arr, index));
				break;
			case 'r':
				setArray(bufferInstance, writeRightShifted(writeTypeMagnitude, value, arr, index));
				break;
			case 'v':
				setArray(bufferInstance, writeVar(value, arr, index));
				break;
		}
	}

	public static void writeStringCp1252NullTerminated(String val, Object bufferInstance) {
		byte[] arr = getArray(bufferInstance);

		int offset = getOffset(bufferInstance);
		int indexMultiplier = Integer.parseInt(ObfuscatedNames.indexMultiplier);
		int offsetMultiplier = (int) Long.parseLong(ObfuscatedNames.offsetMultiplier);

		int realIndex = offset * indexMultiplier;

		int bytesWritten = encodeStringCp1252(val, 0, val.length(), arr, realIndex);

		offset += bytesWritten * offsetMultiplier;
		offset += offsetMultiplier;

		int nullTerminatorIndex = offset * indexMultiplier - 1;

		arr[nullTerminatorIndex] = 0;

		setOffset(bufferInstance, offset);
		setArray(bufferInstance, arr);
	}

	public static void writeStringCp1252NullCircumfixed(String val, Object bufferInstance) {
		byte[] arr = getArray(bufferInstance);

		int offset = getOffset(bufferInstance);
		int indexMultiplier = Integer.parseInt(ObfuscatedNames.indexMultiplier);
		int offsetMultiplier = (int) Long.parseLong(ObfuscatedNames.offsetMultiplier);

		offset += offsetMultiplier;
		int leadingNullIndex = offset * indexMultiplier - 1;
		arr[leadingNullIndex] = 0;

		int stringWriteIndex = offset * indexMultiplier;
		int bytesWritten = encodeStringCp1252(val, 0, val.length(), arr, stringWriteIndex);
		offset += bytesWritten * offsetMultiplier;

		offset += offsetMultiplier;
		int trailingNullIndex = offset * indexMultiplier - 1;
		arr[trailingNullIndex] = 0;

		setOffset(bufferInstance, offset);
		setArray(bufferInstance, arr);
	}

	static byte[] writeSub(int subValue, int value, byte[] arr, int index) {
		arr[index] = (byte) (subValue - value);
		return arr;
	}

	static byte[] writeAdd(int addValue, int value, byte[] arr, int index) {
		arr[index] = (byte) (addValue + value);
		return arr;
	}

	static byte[] writeRightShifted(int shiftAmount, int value, byte[] arr, int index) {
		arr[index] = (byte) (value >> shiftAmount);
		return arr;
	}

	static byte[] writeVar(int value, byte[] arr, int index) {
		arr[index] = (byte) (value);
		return arr;
	}

	static public int nextIndex(int offset) {
		offset += (int) Long.parseLong(ObfuscatedNames.offsetMultiplier);
		return offset;
	}

	public static int encodeStringCp1252(CharSequence data, int startIndex, int endIndex, byte[] output, int outputStartIndex) {
		int var5 = endIndex - startIndex;

		for(int var6 = 0; var6 < var5; ++var6) {
			char var7 = data.charAt(var6 + startIndex);
			if((var7 <= 0 || var7 >= 128) && (var7 < 160 || var7 > 255)) {
				if(var7 == 8364) {
					output[var6 + outputStartIndex] = -128;
				} else if(var7 == 8218) {
					output[var6 + outputStartIndex] = -126;
				} else if(var7 == 402) {
					output[var6 + outputStartIndex] = -125;
				} else if(var7 == 8222) {
					output[var6 + outputStartIndex] = -124;
				} else if(var7 == 8230) {
					output[var6 + outputStartIndex] = -123;
				} else if(var7 == 8224) {
					output[var6 + outputStartIndex] = -122;
				} else if(var7 == 8225) {
					output[var6 + outputStartIndex] = -121;
				} else if(var7 == 710) {
					output[var6 + outputStartIndex] = -120;
				} else if(var7 == 8240) {
					output[var6 + outputStartIndex] = -119;
				} else if(var7 == 352) {
					output[var6 + outputStartIndex] = -118;
				} else if(var7 == 8249) {
					output[var6 + outputStartIndex] = -117;
				} else if(var7 == 338) {
					output[var6 + outputStartIndex] = -116;
				} else if(var7 == 381) {
					output[var6 + outputStartIndex] = -114;
				} else if(var7 == 8216) {
					output[var6 + outputStartIndex] = -111;
				} else if(var7 == 8217) {
					output[var6 + outputStartIndex] = -110;
				} else if(var7 == 8220) {
					output[var6 + outputStartIndex] = -109;
				} else if(var7 == 8221) {
					output[var6 + outputStartIndex] = -108;
				} else if(var7 == 8226) {
					output[var6 + outputStartIndex] = -107;
				} else if(var7 == 8211) {
					output[var6 + outputStartIndex] = -106;
				} else if(var7 == 8212) {
					output[var6 + outputStartIndex] = -105;
				} else if(var7 == 732) {
					output[var6 + outputStartIndex] = -104;
				} else if(var7 == 8482) {
					output[var6 + outputStartIndex] = -103;
				} else if(var7 == 353) {
					output[var6 + outputStartIndex] = -102;
				} else if(var7 == 8250) {
					output[var6 + outputStartIndex] = -101;
				} else if(var7 == 339) {
					output[var6 + outputStartIndex] = -100;
				} else if(var7 == 382) {
					output[var6 + outputStartIndex] = -98;
				} else if(var7 == 376) {
					output[var6 + outputStartIndex] = -97;
				} else {
					output[var6 + outputStartIndex] = 63;
				}
			} else {
				output[var6 + outputStartIndex] = (byte)var7;
			}
		}

		return var5;
	}
}