package com.darkprograms.speech.recognizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import javaFlacEncoder.FLACFileWriter;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import com.darkprograms.speech.util.StringUtil;

/***************************************************************
 * Class that submits FLAC audio and retrieves recognized text
 * 
 * @author Luke Kuza, Duncan Jauncey, Aaron Gokaslan
 **************************************************************/
public class Recognizer {

	public enum Languages {
		AUTO_DETECT("auto"), // tells Google to auto-detect the language
		ARABIC_JORDAN("ar-JO"), ARABIC_LEBANON("ar-LB"), ARABIC_QATAR("ar-QA"), ARABIC_UAE("ar-AE"), ARABIC_MOROCCO("ar-MA"), ARABIC_IRAQ("ar-IQ"), ARABIC_ALGERIA("ar-DZ"), ARABIC_BAHRAIN("ar-BH"), ARABIC_LYBIA("ar-LY"), ARABIC_OMAN("ar-OM"), ARABIC_SAUDI_ARABIA("ar-SA"), ARABIC_TUNISIA("ar-TN"), ARABIC_YEMEN("ar-YE"), BASQUE("eu"), CATALAN("ca"), CZECH("cs"), DUTCH("nl-NL"), ENGLISH_AUSTRALIA("en-AU"), ENGLISH_CANADA("en-CA"), ENGLISH_INDIA("en-IN"), ENGLISH_NEW_ZEALAND("en-NZ"), ENGLISH_SOUTH_AFRICA("en-ZA"), ENGLISH_UK("en-GB"), ENGLISH_US("en-US"), FINNISH("fi"), FRENCH("fr-FR"), GALICIAN("gl"), GERMAN("de-DE"), HEBREW("he"), HUNGARIAN("hu"), ICELANDIC("is"), ITALIAN("it-IT"), INDONESIAN("id"), JAPANESE("ja"), KOREAN("ko"), LATIN("la"), CHINESE_SIMPLIFIED("zh-CN"), CHINESE_TRANDITIONAL("zh-TW"), CHINESE_HONGKONG("zh-HK"), CHINESE_CANTONESE("zh-yue"), MALAYSIAN("ms-MY"), NORWEGIAN("no-NO"), POLISH("pl"), PIG_LATIN("xx-piglatin"), PORTUGUESE("pt-PT"), PORTUGUESE_BRASIL("pt-BR"), ROMANIAN("ro-RO"), RUSSIAN("ru"), SERBIAN("sr-SP"), SLOVAK("sk"), SPANISH_ARGENTINA("es-AR"), SPANISH_BOLIVIA("es-BO"), SPANISH_CHILE("es-CL"), SPANISH_COLOMBIA("es-CO"), SPANISH_COSTA_RICA("es-CR"), SPANISH_DOMINICAN_REPUBLIC("es-DO"), SPANISH_ECUADOR("es-EC"), SPANISH_EL_SALVADOR("es-SV"), SPANISH_GUATEMALA("es-GT"), SPANISH_HONDURAS("es-HN"), SPANISH_MEXICO("es-MX"), SPANISH_NICARAGUA("es-NI"), SPANISH_PANAMA("es-PA"), SPANISH_PARAGUAY("es-PY"), SPANISH_PERU("es-PE"), SPANISH_PUERTO_RICO("es-PR"), SPANISH_SPAIN("es-ES"), SPANISH_US("es-US"), SPANISH_URUGUAY("es-UY"), SPANISH_VENEZUELA("es-VE"), SWEDISH("sv-SE"), TURKISH("tr"), ZULU("zu");

		// TODO Clean Up JavaDoc for Overloaded Methods using @link

		/**
		 * Stores the LanguageCode
		 */
		private final String languageCode;

		/**
		 * Constructor
		 */
		private Languages(final String languageCode) {
			this.languageCode = languageCode;
		}

		public String toString() {
			return languageCode;
		}

	}

	private final String API_KEY;

	/**
	 * URL to POST audio data and retrieve results
	 */
	private static final String GOOGLE_RECOGNIZER_URL = "https://www.google.com/speech-api/v2/recognize?";

	private boolean profanityFilter = true;
	private String language = null;

	/**
	 * Constructor
	 */
	public Recognizer(String API_KEY) {
		this.API_KEY = API_KEY;
		this.setLanguage(Languages.AUTO_DETECT);
	}

	// /**
	// * Constructor
	// * @param Language
	// */
	// @Deprecated
	// public Recognizer(String language) {
	// this.language = language;
	// }

	/**
	 * Constructor
	 * 
	 * @param language
	 *            The Languages class for the language you want to designate
	 */
	public Recognizer(Languages language, String API_KEY) {
		this.API_KEY = API_KEY;
		this.language = language.languageCode;
	}

	/**
	 * Constructor
	 * 
	 * @param profanityFilter
	 */
	public Recognizer(boolean profanityFilter, String API_KEY) {
		this.API_KEY = API_KEY;
		this.profanityFilter = profanityFilter;
	}

	/**
	 * Constructor
	 * 
	 * @param language
	 * @param profanityFilter
	 */
	@Deprecated
	public Recognizer(String language, boolean profanityFilter, String API_KEY) {
		this.API_KEY = API_KEY;
		this.language = language;
		this.profanityFilter = profanityFilter;
	}

	/**
	 * Constructor
	 * 
	 * @param language
	 * @param profanityFilter
	 */
	public Recognizer(Languages language, boolean profanityFilter, String API_KEY) {
		this.API_KEY = API_KEY;
		this.language = language.languageCode;
		this.profanityFilter = profanityFilter;
	}

	/**
	 * Language: Contains all supported languages for Google Speech to Text.
	 * Setting this to null will make Google use it's own language detection.
	 * This value is null by default.
	 * 
	 * @param language
	 */
	public void setLanguage(Languages language) {
		this.language = language.languageCode;
	}

	/**
	 * Language code. This language code must match the language of the speech
	 * to be recognized. ex. en-US ru-RU This value is null by default.
	 * 
	 * @param language
	 *            The language code.
	 */
	@Deprecated
	public void setLanguage(String language) {
		this.language = language;
	}

	/**
	 * Returns the state of profanityFilter which enables/disables Google's
	 * profanity filter (on by default).
	 * 
	 * @return profanityFilter
	 */
	public boolean getProfanityFilter() {
		return profanityFilter;
	}

	/**
	 * Language code. This language code must match the language of the speech
	 * to be recognized. ex. en-US ru-RU This value is null by default.
	 * 
	 * @return language the Google language
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * Get recognized data from a Wave file. This method will encode the wave
	 * file to a FLAC file
	 * 
	 * @param waveFile
	 *            Wave file to recognize
	 * @param maxResults
	 *            Maximum number of results to return in response
	 * @return Returns a GoogleResponse, with the response and confidence score
	 * @throws IOException
	 *             Throws exception if something goes wrong
	 */
	public GoogleResponse getRecognizedDataForWave(File waveFile, int maxResults) throws IOException {
		FlacEncoder flacEncoder = new FlacEncoder();
		File flacFile = new File(waveFile + ".flac");

		flacEncoder.convertWaveToFlac(waveFile, flacFile);

		GoogleResponse googleResponse = getRecognizedDataForFlac(flacFile, maxResults, 8000);

		// Delete converted FLAC data
		flacFile.delete();
		return googleResponse;
	}

	/**
	 * Get recognized data from a Wave file. This method will encode the wave
	 * file to a FLAC
	 * 
	 * @param waveFile
	 *            Wave file to recognize
	 * @param maxResults
	 *            the maximum number of results to return in the response NOTE:
	 *            Sample rate of file must be 8000 unless a custom sample rate
	 *            is specified.
	 * @return Returns a GoogleResponse, with the response and confidence score
	 * @throws IOException
	 *             Throws exception if something goes wrong
	 */
	public GoogleResponse getRecognizedDataForWave(String waveFile, int maxResults) throws IOException {
		return getRecognizedDataForWave(new File(waveFile), maxResults);
	}

	/**
	 * Get recognized data from a FLAC file.
	 * 
	 * @param flacFile
	 *            FLAC file to recognize
	 * @param maxResults
	 *            the maximum number of results to return in the response NOTE:
	 *            Sample rate of file must be 8000 unless a custom sample rate
	 *            is specified.
	 * @return Returns a GoogleResponse, with the response and confidence score
	 * @throws IOException
	 *             Throws exception if something goes wrong
	 */
	public GoogleResponse getRecognizedDataForFlac(File flacFile, int maxResults) throws IOException {
		return getRecognizedDataForFlac(flacFile, maxResults, 8000);
	}

	/**
	 * Get recognized data from a FLAC file.
	 * 
	 * @param flacFile
	 *            FLAC file to recognize
	 * @param maxResults
	 *            the maximum number of results to return in the response
	 * @param samepleRate
	 *            The sampleRate of the file. Default is 8000.
	 * @return Returns a GoogleResponse, with the response and confidence score
	 * @throws IOException
	 *             Throws exception if something goes wrong
	 */
	public GoogleResponse getRecognizedDataForFlac(File flacFile, int maxResults, int sampleRate) throws IOException {
		String response = rawRequest(flacFile, maxResults, sampleRate);
		GoogleResponse googleResponse = new GoogleResponse();
		parseResponse(response, googleResponse);
		return googleResponse;
	}

	/**
	 * Get recognized data from a FLAC file.
	 * 
	 * @param flacFile
	 *            FLAC file to recognize
	 * @param maxResults
	 *            the maximum number of results to return in the response
	 * @param samepleRate
	 *            The sampleRate of the file. Default is 8000.
	 * @return Returns a GoogleResponse, with the response and confidence score
	 * @throws IOException
	 *             Throws exception if something goes wrong
	 */
	public GoogleResponse getRecognizedDataForFlac(String flacFile, int maxResults, int sampleRate) throws IOException {
		return getRecognizedDataForFlac(new File(flacFile), maxResults, sampleRate);
	}

	/**
	 * Get recognized data from a FLAC file.
	 * 
	 * @param flacFile
	 *            FLAC file to recognize
	 * @param maxResults
	 *            the maximum number of results to return in the response
	 * @return Returns a GoogleResponse, with the response and confidence score
	 * @throws IOException
	 *             Throws exception if something goes wrong
	 */
	public GoogleResponse getRecognizedDataForFlac(String flacFile, int maxResults) throws IOException {
		return getRecognizedDataForFlac(new File(flacFile), maxResults);
	}

	/**
	 * Get recognized data from a Wave file. This method will encode the wave
	 * file to a FLAC. This method will automatically set the language to en-US,
	 * or English
	 * 
	 * @param waveFile
	 *            Wave file to recognize
	 * @return Returns a GoogleResponse, with the response and confidence score
	 * @throws IOException
	 *             Throws exception if something goes wrong
	 */
	public GoogleResponse getRecognizedDataForWave(File waveFile) throws IOException {
		return getRecognizedDataForWave(waveFile, 1);
	}

	/**
	 * Get recognized data from a Wave file. This method will encode the wave
	 * file to a FLAC. This method will automatically set the language to en-US,
	 * or English
	 * 
	 * @param waveFile
	 *            Wave file to recognize
	 * @return Returns a GoogleResponse, with the response and confidence score
	 * @throws IOException
	 *             Throws exception if something goes wrong
	 */
	public GoogleResponse getRecognizedDataForWave(String waveFile) throws IOException {
		return getRecognizedDataForWave(waveFile, 1);
	}

	/**
	 * Get recognized data from a FLAC file. This method will automatically set
	 * the language to en-US, or English
	 * 
	 * @param flacFile
	 *            FLAC file to recognize
	 * @return Returns a GoogleResponse, with the response and confidence score
	 * @throws IOException
	 *             Throws exception if something goes wrong
	 */
	public GoogleResponse getRecognizedDataForFlac(File flacFile) throws IOException {
		return getRecognizedDataForFlac(flacFile, 1);
	}

	/**
	 * Get recognized data from a FLAC file. This method will automatically set
	 * the language to en-US, or English
	 * 
	 * @param flacFile
	 *            FLAC file to recognize
	 * @return Returns a GoogleResponse, with the response and confidence score
	 * @throws IOException
	 *             Throws exception if something goes wrong
	 */
	public GoogleResponse getRecognizedDataForFlac(String flacFile) throws IOException {
		return getRecognizedDataForFlac(flacFile, 1);
	}

	/**
	 * Parses the raw response from Google
	 * 
	 * @param rawResponse
	 *            The raw, unparsed response from Google
	 * @return Returns the parsed response in the form of a Google Response.
	 */
	private void parseResponse(String rawResponce, GoogleResponse googleResponce) {
		if (rawResponce == null || googleResponce == null)
			return;
		String responce1 = StringUtil.substringBetween(rawResponce, "[", "]");
		boolean confident = true;
		if (responce1 == null || responce1.equals("")) {
			confident = false;
		} else {
			googleResponce.setResponse(responce1);
			rawResponce.replaceFirst(responce1, "");
		}
		rawResponce = rawResponce.replaceFirst("\\{\"result\":\\[\\]\\}", "");
		if (rawResponce.equals(""))
			return;
		String unsplitResponses = StringUtil.substringBetween(rawResponce, "\"alternative\":[", "]");
		String[] splitResponses = unsplitResponses.split("\\},\\{");
		for (int i = 0; i < splitResponses.length; i++) {
			splitResponses[i] = splitResponses[i].replaceAll("\\{", "");
			splitResponses[i] = splitResponses[i].replaceAll("\\}", "");
			String response = splitResponses[i];
			if (splitResponses[i].contains(","))
				response = splitResponses[i].split(",")[0];
			response = response.replaceAll("\"transcript\":", "");
			response = StringUtil.stripQuotes(response);
			if (i == 0 && !confident)
				googleResponce.setResponse(response);
			else
				googleResponce.getOtherPossibleResponses().add(response);
		}

	}

	// private void parseResponse(String rawResponse, GoogleResponse
	// googleResponse) {
	// if (rawResponse == null)
	// return;
	//
	// String array = StringUtil.substringBetween(rawResponse, "[", "]");
	// String[] parts = array.split("}");
	//
	// boolean first = true;
	// for (String s : parts) {
	// if (first) {
	// first = false;
	// String utterancePart = s.split(",")[0];
	// String confidencePart = s.split(",")[1];
	//
	// String utterance = utterancePart.split(":")[1];
	// String confidence = confidencePart.split(":")[1];
	//
	// utterance = StringUtil.stripQuotes(utterance);
	// confidence = StringUtil.stripQuotes(confidence);
	//
	// if (utterance.equals("null")) {
	// utterance = null;
	// }
	// if (confidence.equals("null")) {
	// confidence = null;
	// }
	//
	// googleResponse.setResponse(utterance);
	// googleResponse.setConfidence(confidence);
	// } else {
	// String utterance = s.split(":")[1];
	// utterance = StringUtil.stripQuotes(utterance);
	// if (utterance.equals("null")) {
	// utterance = null;
	// }
	// googleResponse.getOtherPossibleResponses().add(utterance);
	// }
	// }
	// }

	/**
	 * Performs the request to Google with a file <br>
	 * Request is buffered
	 * 
	 * @param inputFile
	 *            Input files to recognize
	 * @return Returns the raw, unparsed response from Google
	 * @throws IOException
	 *             Throws exception if something went wrong
	 */
	private String rawRequest(File inputFile, int maxResults, int sampleRate) throws IOException {
		URL url;
		URLConnection urlConn;
		OutputStream outputStream;
		BufferedReader br;

		StringBuilder sb = new StringBuilder(GOOGLE_RECOGNIZER_URL);
		sb.append("key=" + API_KEY);
		if (language != null) {
			sb.append("&lang=");
			sb.append(language);
		} else {
			sb.append("&lang=auto");
		}
		if (!profanityFilter) {
			sb.append("&pfilter=0");
		}
		sb.append("&maxresults=");
		sb.append(maxResults);

		// URL of Remote Script.
		url = new URL(sb.toString());

		// Open New URL connection channel.
		urlConn = url.openConnection();

		// we want to do output.
		urlConn.setDoOutput(true);

		// No caching
		urlConn.setUseCaches(false);

		// Specify the header content type.
		urlConn.setRequestProperty("Content-Type", "audio/x-flac; rate=" + sampleRate);

		// Send POST output.
		outputStream = urlConn.getOutputStream();

		FileInputStream fileInputStream = new FileInputStream(inputFile);

		byte[] buffer = new byte[256];

		while ((fileInputStream.read(buffer, 0, 256)) != -1) {
			outputStream.write(buffer, 0, 256);
		}

		fileInputStream.close();
		outputStream.close();

		// Get response data.
		br = new BufferedReader(new InputStreamReader(urlConn.getInputStream(), Charset.forName("UTF-8")));

		String response = br.readLine();
		System.out.println(response);
		boolean done = false;
		while (response != null && !done) {
			String tmp = br.readLine();
			System.out.println(tmp);
			if (tmp != null)
				response += tmp;
			else
				done = true;
		}
		System.out.println("Unparsed Responce = " + response);
		br.close();

		return response;

	}
	
	public GoogleResponse getRecognizedDataForAudioStream(AudioInputStream in, int maxResults, int sampleRate) throws IOException {
		String response = rawRequest(in, maxResults, sampleRate);
		GoogleResponse googleResponse = new GoogleResponse();
		parseResponse(response, googleResponse);
		return googleResponse;
	}
	
	private String rawRequest(AudioInputStream in, int maxResults, int sampleRate) throws IOException {
		URL url;
		URLConnection urlConn;
		OutputStream outputStream;
		BufferedReader br;

		StringBuilder sb = new StringBuilder(GOOGLE_RECOGNIZER_URL);
		sb.append("key=" + API_KEY);
		if (language != null) {
			sb.append("&lang=");
			sb.append(language);
		} else {
			sb.append("&lang=auto");
		}
		if (!profanityFilter) {
			sb.append("&pfilter=0");
		}
		sb.append("&maxresults=");
		sb.append(maxResults);

		// URL of Remote Script.
		url = new URL(sb.toString());

		// Open New URL connection channel.
		urlConn = url.openConnection();

		// we want to do output.
		urlConn.setDoOutput(true);

		// No caching
		urlConn.setUseCaches(false);

		// Specify the header content type.
		urlConn.setRequestProperty("Content-Type", "audio/x-flac; rate=" + sampleRate);

		// Send POST output.
		outputStream = urlConn.getOutputStream();

		AudioSystem.write(in, FLACFileWriter.FLAC, outputStream);

		// Get response data.
		br = new BufferedReader(new InputStreamReader(urlConn.getInputStream(), Charset.forName("UTF-8")));

		String response = br.readLine();
		System.out.println(response);
		boolean done = false;
		while (response != null && !done) {
			String tmp = br.readLine();
			System.out.println(tmp);
			if (tmp != null)
				response += tmp;
			else
				done = true;
		}
		System.out.println("Unparsed Responce = " + response);
		br.close();

		return response;
	}

}
