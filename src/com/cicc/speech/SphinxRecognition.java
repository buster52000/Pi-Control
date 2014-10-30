package com.cicc.speech;


public abstract class SphinxRecognition {

//	public static void main(String args[]) {
//		SphinxRecognition rec = new SphinxRecognition() {
//			
//			@Override
//			public void acceptableResponseHeard(String response) {
//				System.out.println(response);
//			}
//		};
//		rec.startSphinxRecognition();
//	}
//
//	private static final String ACOUSTIC_MODEL = "resource:/WSJ_8gau_13dCep_16k_40mel_130Hz_6800Hz";
////	private static final String DICTIONARY_PATH = "resource:/WSJ_8gau_13dCep_16k_40mel_130Hz_6800Hz/dict/cmudict.0.6d";
//	private static final String DICTIONARY_PATH = "resource:/com/cicc/speech/9917.dic";
//	private static final String LANG_MODEL_PATH = "resource:/com/cicc/speech/9917.lm";
////	private static final String GRAMMAR_PATH = "resource:/com/cicc/speech/";
//
//	private LiveSpeechRecognizer recognizer;
//	private Configuration config;
//	private boolean started;
//
//	public SphinxRecognition() {
//		config = new Configuration();
//		config.setAcousticModelPath(ACOUSTIC_MODEL);
//		config.setDictionaryPath(DICTIONARY_PATH);
////		config.setGrammarPath(GRAMMAR_PATH);
//		config.setUseGrammar(false);
//		config.setLanguageModelPath(LANG_MODEL_PATH);
////		config.setGrammarName("speech");
//		try {
//			recognizer = new LiveSpeechRecognizer(config);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		started = false;
//	}
//
//	public void startSphinxRecognition() {
//		recognizer.startRecognition(true);
//		started = true;
//		Thread t = new Thread(new Runnable() {
//			
//			@Override
//			public void run() {
//				while(started) {
//					String utterance = recognizer.getResult().getHypothesis();
//					if(utterance != null && utterance.length() != 0) {
//						acceptableResponseHeard(utterance);
//					}
//				}
//			}
//		});
//		t.start();
//	}
//
//	public void stopSphinxRecognition() {
//		started = false;
//		recognizer.stopRecognition();
//	}
//
//	public abstract void acceptableResponseHeard(String response);

}
