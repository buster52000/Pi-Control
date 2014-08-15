package com.cicc.voiceCont;
public class HighPass50 {
	static int NZEROS = 5;
	static int NPOLES = 5;
	static double GAIN = 1.095980088e+00;

	private static double xv[] = new double[NZEROS + 1];
	private static double yv[] = new double[NPOLES + 1];

	public static byte[] process(byte data[]) {
		for (int i = 0; i < data.length; ++i) {
			xv[0] = xv[1];
			xv[1] = xv[2];
			xv[2] = data[i] / GAIN;
			yv[0] = yv[1];
			yv[1] = yv[2];
			yv[2] = (xv[0] + xv[2]) - 2 * xv[1] + (-0.9479259375 * yv[0]) + (1.9469976496 * yv[1]);
			data[i] = (byte) yv[2];
		}
		return data;
	}
}