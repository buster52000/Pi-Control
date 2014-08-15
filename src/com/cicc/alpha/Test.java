package com.cicc.alpha;

public class Test {

	public static void main(String[] args) {
		Search s = new Search("7R65AK-2YLWYGXEL4");
		String answer = s.getPlaintextForQuery("What is the square root of 3424");
		if (answer == null) {
			System.out.println("Error");
		} else {
			System.out.println(answer);
		}
	}

}
