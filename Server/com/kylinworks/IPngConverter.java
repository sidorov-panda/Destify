package com.kylinworks;

import com.jcraft.jzlib.*;

import java.io.*;
import java.util.ArrayList;
import java.util.zip.CRC32;

public class IPngConverter {
	private ArrayList<PNGTrunk> trunks = null;

	private File getTargetFile(File convertedFile) throws IOException {
		String name = convertedFile.getAbsolutePath();
		name = name.substring(0, name.length() -4);
		name += "-new.png";
		return new File(name);
	}

	private PNGTrunk getTrunk(String szName) {
		if (trunks == null) { return null; }
		PNGTrunk trunk;
		for (int n = 0; n < trunks.size(); n++) {
			trunk = trunks.get(n);
			if (trunk.getName().equalsIgnoreCase(szName)) { return trunk; }
		}
		return null;
	}

	private void convertPngFile(File pngFile, File targetFile) throws IOException {
		readTrunks(pngFile);

		if (getTrunk("CgBI") != null) {
			PNGIHDRTrunk ihdrTrunk = (PNGIHDRTrunk) getTrunk("IHDR");

			int nMaxInflateBuffer = 4 *(ihdrTrunk.m_nWidth + 1) *ihdrTrunk.m_nHeight;
			byte[] outputBuffer = new byte[nMaxInflateBuffer];

			convertDataTrunk(ihdrTrunk, outputBuffer, nMaxInflateBuffer);
			writePng(targetFile);
			outputBuffer = null;

		} else {
			// Likely a standard PNG: just copy
			byte[] buffer = new byte[1024];
			int bytesRead;
			InputStream inputStream = new FileInputStream(pngFile);
			try {
				OutputStream outputStream = new FileOutputStream(targetFile);
				try {
					while ((bytesRead = inputStream.read(buffer)) >= 0) {
						outputStream.write(buffer, 0, bytesRead);
					}
					outputStream.flush();

				} finally {
					outputStream.close();
				}
			} finally {
				inputStream.close();
			}
			
			buffer = null;
		}
		
		System.gc();
	}

	@SuppressWarnings("deprecation")
	private long inflate(byte[] conversionBuffer, int nMaxInflateBuffer) throws GZIPException {
		Inflater inflater = new Inflater(-15);

		for (PNGTrunk dataTrunk : trunks) {
			if (!"IDAT".equalsIgnoreCase(dataTrunk.getName())) continue;
			inflater.setInput(dataTrunk.getData(), true);
		}

		inflater.setOutput(conversionBuffer);

		int nResult;
		try {
			nResult = inflater.inflate(JZlib.Z_NO_FLUSH);
			checkResultStatus(nResult);
		} finally {
			inflater.inflateEnd();
		}
		
		return inflater.getTotalOut();
	}

	@SuppressWarnings("deprecation")
	private Deflater deflate(byte[] buffer, int length, int nMaxInflateBuffer) throws GZIPException {
		Deflater deflater = new Deflater();
		deflater.setInput(buffer, 0, length, false);

		int nMaxDeflateBuffer = nMaxInflateBuffer + 1024;
		byte[] deBuffer = new byte[nMaxDeflateBuffer];
		deflater.setOutput(deBuffer);
		deBuffer = null;

		deflater.deflateInit(JZlib.Z_BEST_COMPRESSION);
		int nResult = deflater.deflate(JZlib.Z_FINISH);
		checkResultStatus(nResult);

		if (deflater.getTotalOut() > nMaxDeflateBuffer) {
			throw new GZIPException("deflater output buffer was too small");
		}
		
		System.gc();

		return deflater;
	}

	private void checkResultStatus(int nResult) throws GZIPException {
		switch (nResult) {
		case JZlib.Z_OK:
		case JZlib.Z_STREAM_END:
			break;

		case JZlib.Z_NEED_DICT:
			throw new GZIPException("Z_NEED_DICT - " + nResult);
		case JZlib.Z_DATA_ERROR:
			throw new GZIPException("Z_DATA_ERROR - " + nResult);
		case JZlib.Z_MEM_ERROR:
			throw new GZIPException("Z_MEM_ERROR - " + nResult);
		case JZlib.Z_STREAM_ERROR:
			throw new GZIPException("Z_STREAM_ERROR - " + nResult);
		case JZlib.Z_BUF_ERROR:
			throw new GZIPException("Z_BUF_ERROR - " + nResult);
		default:
			throw new GZIPException("inflater error: " + nResult);
		}
	}

	@SuppressWarnings("deprecation")
	private boolean convertDataTrunk(PNGIHDRTrunk ihdrTrunk, byte[] conversionBuffer, int nMaxInflateBuffer) throws IOException {
		long inflatedSize = inflate(conversionBuffer, nMaxInflateBuffer);

		// Switch the color
		int nIndex = 0;
		byte nTemp;
		for (int y = 0; y < ihdrTrunk.m_nHeight; y++) {
			nIndex++;
			for (int x = 0; x < ihdrTrunk.m_nWidth; x++) {
				nTemp = conversionBuffer[nIndex];
				conversionBuffer[nIndex] = conversionBuffer[nIndex + 2];
				conversionBuffer[nIndex + 2] = nTemp;
				nIndex += 4;
			}
		}

		Deflater deflater = deflate(conversionBuffer, (int) inflatedSize, nMaxInflateBuffer);

		// Put the result in the first IDAT chunk (the only one to be written out)
		PNGTrunk firstDataTrunk = getTrunk("IDAT");

		CRC32 crc32 = new CRC32();
		crc32.update(firstDataTrunk.getName().getBytes());
		crc32.update(deflater.getNextOut(), 0, (int) deflater.getTotalOut());
		long lCRCValue = crc32.getValue();

		firstDataTrunk.m_nData = deflater.getNextOut();
		firstDataTrunk.m_nCRC[0] = (byte) ((lCRCValue & 0xFF000000) >> 24);
		firstDataTrunk.m_nCRC[1] = (byte) ((lCRCValue & 0xFF0000) >> 16);
		firstDataTrunk.m_nCRC[2] = (byte) ((lCRCValue & 0xFF00) >> 8);
		firstDataTrunk.m_nCRC[3] = (byte) (lCRCValue & 0xFF);
		firstDataTrunk.m_nSize = (int) deflater.getTotalOut();

		return false;
	}

	private void writePng(File newFileName) throws IOException {
		FileOutputStream outStream = new FileOutputStream(newFileName);
		try {
			byte[] pngHeader = { -119, 80, 78, 71, 13, 10, 26, 10 };
			outStream.write(pngHeader);
			boolean dataWritten = false;
			for (PNGTrunk trunk : trunks) {
				if (trunk.getName().equalsIgnoreCase("CgBI")) { continue; }
				if ("IDAT".equalsIgnoreCase(trunk.getName())) {
					if (dataWritten) { continue; } else { dataWritten = true; }
				}

				trunk.writeToStream(outStream);
			}
			outStream.flush();
			pngHeader = null;
			System.gc();

		} finally {
			outStream.close();
			outStream = null;
		}
	}

	private void readTrunks(File pngFile) throws IOException {
		DataInputStream input = new DataInputStream(new FileInputStream(pngFile));
		try {
			byte[] nPNGHeader = new byte[8];
			input.readFully(nPNGHeader);

			trunks = new ArrayList<PNGTrunk>();
			if ((nPNGHeader[0] == -119) && (nPNGHeader[1] == 0x50)
					&& (nPNGHeader[2] == 0x4e) && (nPNGHeader[3] == 0x47)
					&& (nPNGHeader[4] == 0x0d) && (nPNGHeader[5] == 0x0a)
					&& (nPNGHeader[6] == 0x1a) && (nPNGHeader[7] == 0x0a)) {

				PNGTrunk trunk;
				do {
					trunk = PNGTrunk.generateTrunk(input);
					trunks.add(trunk);
				} while (!trunk.getName().equalsIgnoreCase("IEND"));
			}
			nPNGHeader = null;
			System.gc();
			
		} finally {
			input.close();
			input = null;
		}
	}

	public void convert(File sourceFile) throws IOException {
		File targetFile = getTargetFile(sourceFile);
		convertPngFile(sourceFile, targetFile);
		targetFile = null;
	}
}
