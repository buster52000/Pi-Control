package com.cicc.alarm;
import java.awt.AWTException;
import java.awt.Font;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.cicc.tts.Speak;
import com.cicc.weather.Weather;

@SuppressWarnings("serial")
public class Controller extends JFrame implements KeyListener {

	private int hour, min;
	private JLabel label;
	private boolean stop;
	private Timer t;
	private Clip clip;
	private AudioInputStream in;
	public static final int secondAlmDelay = 15;	
	
	public Controller(int hour, int min) {
		t = new Timer();
		this.hour = hour;
		this.min = min;
		label = new JLabel("Alarm Clock");
		label.setFont(new Font(Font.SERIF, Font.CENTER_BASELINE, 42));
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		addKeyListener(this);
		add(label);
		pack();
	}

	public void start() {
		String hTemp = Integer.toString(hour == 0 ? 12 : hour > 12 ? hour - 12 : hour);
		String mTemp = Integer.toString(min);
		String aTemp = hour > 11 ? "P.M." : "A.M.";
		say("Timer set for " + hTemp + " " + mTemp + " " + aTemp);
		int year;
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, min);
		cal.set(Calendar.SECOND, 0);
		if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) > this.hour || (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) == this.hour && Calendar.getInstance().get(Calendar.MINUTE) > this.min)) {
			if ((Calendar.getInstance().get(Calendar.DAY_OF_YEAR) == 365 && Calendar.getInstance().get(Calendar.YEAR) % 4 != 0) || (Calendar.getInstance().get(Calendar.DAY_OF_YEAR) == 366 && Calendar.getInstance().get(Calendar.YEAR) % 4 == 0)) {
				year = Calendar.getInstance().get(Calendar.YEAR) + 1;
				cal.set(Calendar.YEAR, year);
				cal.set(Calendar.DAY_OF_YEAR, 1);
			} else {
				year = Calendar.getInstance().get(Calendar.YEAR);
				cal.set(Calendar.DAY_OF_YEAR, Calendar.getInstance().get(Calendar.DAY_OF_YEAR) + 1);
			}
		} else {
			year = Calendar.getInstance().get(Calendar.YEAR);
		}
		long time = cal.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
		System.out.println(time);
		if (time > 0) {
			sleepComp(cal);
			t.schedule(new TimerTask() {

				@Override
				public void run() {
					alarm();
				}
			}, time);
		} else
			alarm();
	}

	public void sleepComp(Calendar cal) {
		File wake = new File("wakeup.xml");
		if (!wake.exists()) {
			try {
				InputStream in = AlarmMain.class.getResourceAsStream("wakeup.xml");
				wake.createNewFile();
				FileOutputStream out = new FileOutputStream(wake);
				int c;
				while ((c = in.read()) != -1) {
					out.write(c);
				}
				in.close();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		cal.setTimeInMillis(cal.getTimeInMillis() - 60000);
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(wake);
			NodeList nList = doc.getElementsByTagName("StartBoundary");
			Node node = nList.item(0);
			String str = cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) < 9 ? "0" : "") + (cal.get(Calendar.MONTH) + 1) + "-" + (cal.get(Calendar.DAY_OF_MONTH) < 10 ? "0" : "") + cal.get(Calendar.DAY_OF_MONTH) + "T" + (cal.get(Calendar.HOUR_OF_DAY) < 10 ? "0" : "") + cal.get(Calendar.HOUR_OF_DAY) + ":" + (cal.get(Calendar.MINUTE) < 10 ? "0" : "") + cal.get(Calendar.MINUTE) + ":" + (cal.get(Calendar.SECOND) < 10 ? "0" : "") + cal.get(Calendar.SECOND);
			System.out.println(str);
			node.setTextContent(str);

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			File tmp = new File("output.xml");
			if (tmp.exists())
				tmp.delete();
			StreamResult result = new StreamResult("output.xml");
			transformer.transform(source, result);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}

		runCmd("schtasks /delete /TN wakeup /F", true);
		runCmd("schtasks /create /TN wakeup /XML output.xml", true);
		
		runCmd("rundll32.exe powrprof.dll,SetSuspendState Standby", true);
		try {
			Robot bot = new Robot();
			bot.mouseMove(1, 1);
			bot.mouseMove(100, 100);
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}

	public void alarm() {
		setVisible(true);
		in = null;
		clip = null;
		try {
			in = AudioSystem.getAudioInputStream(Controller.class.getResource("alarm.wav"));
			clip = AudioSystem.getClip();
			clip.open(in);
		} catch (IOException e2) {
			e2.printStackTrace();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		}
		clip.start();
		t.schedule(new TimerTask() {

			@Override
			public void run() {

				clip.loop(Clip.LOOP_CONTINUOUSLY);

			}
		}, secondAlmDelay * 60 * 1000);
	}

	public void talk() {
		int hour = Calendar.getInstance().get(Calendar.HOUR);
		int min = Calendar.getInstance().get(Calendar.MINUTE);
		int amPm = Calendar.getInstance().get(Calendar.AM_PM);
		int dow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
		int month = Calendar.getInstance().get(Calendar.MONTH);
		int date = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
		int year = Calendar.getInstance().get(Calendar.YEAR);
		String sMonth = "";
		String sDow = "";
		switch (month) {
		case Calendar.JANUARY:
			sMonth = "January";
			break;
		case Calendar.FEBRUARY:
			sMonth = "February";
			break;
		case Calendar.MARCH:
			sMonth = "March";
			break;
		case Calendar.APRIL:
			sMonth = "April";
			break;
		case Calendar.MAY:
			sMonth = "May";
			break;
		case Calendar.JUNE:
			sMonth = "June";
			break;
		case Calendar.JULY:
			sMonth = "July";
			break;
		case Calendar.AUGUST:
			sMonth = "August";
			break;
		case Calendar.SEPTEMBER:
			sMonth = "September";
			break;
		case Calendar.OCTOBER:
			sMonth = "October";
			break;
		case Calendar.NOVEMBER:
			sMonth = "November";
			break;
		case Calendar.DECEMBER:
			sMonth = "December";
			break;
		default:
			sMonth = "Unknown Month";
			break;
		}
		switch (dow) {
		case Calendar.SUNDAY:
			sDow = "Sunday";
			break;
		case Calendar.MONDAY:
			sDow = "Monday";
			break;
		case Calendar.TUESDAY:
			sDow = "Tuesday";
			break;
		case Calendar.WEDNESDAY:
			sDow = "Wednesday";
			break;
		case Calendar.THURSDAY:
			sDow = "Thursday";
			break;
		case Calendar.FRIDAY:
			sDow = "Friday";
			break;
		case Calendar.SATURDAY:
			sDow = "Saturday";
			break;
		default:
			sDow = "Unknown day of week";
			break;
		}
		String am = "";
		if (amPm == Calendar.AM)
			am = "A.M.";
		else
			am = "P.M.";
		say("the time is " + hour + " " + min + " " + am);
		say("the date is " + sDow + " " + sMonth + " " + date + ", " + year);
		for (String s : getRSSTitles("http://www.pburgsd.net/site/RSS.aspx?DomainID=152&ModuleInstanceID=2054&PageID=1207")) {
			if (includes(s, month + "/" + date + "/" + year)) {
				if (includes(s, "Day 1")) {
					say("Today is a day 1");
				} else if (includes(s, "Day 2")) {
					say("Today is a day 2");
				} else if (includes(s, "Day 3")) {
					say("Today is a day 3");
				} else if (includes(s, "Day 4")) {
					say("Today is a day 4");
				}
				if ((includes(s, "1/2") && includes(s, "Dismissal")) || (includes(s, "Half") && includes(s, "Dismissal"))) {
					say("Today is a half day");
				}
				if (includes(s, "School Closed")) {
					say("School is closed today");
				}
			}
		}
		Weather weather = new Weather();
		weather.start();
		setVisible(false);
		dispose();
		System.exit(0);
	}

	public boolean includes(String str1, String str2) {
		return str1.contains(str2);
	}

	public String[] getRSSTitles(String url) {
		RSSFeedParser parser = new RSSFeedParser(url);
		Feed feed = parser.readFeed();
		int size = feed.getMessages().size();
		String[] msgs = new String[size];
		for (int i = 0; i < size; i++) {
			msgs[i] = feed.getMessages().get(i).getTitle();
		}
		return msgs;
	}

	public void runCmd(String cmd, boolean wait) {
		Runtime rt = Runtime.getRuntime();
		Process pr;
		try {
			pr = rt.exec(cmd);
			if (wait)
				pr.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void say(String str) {
		Speak.say(str);
	}

	@Override
	public void keyPressed(KeyEvent arg0) {

	}

	@Override
	public void keyReleased(KeyEvent arg0) {

	}

	@Override
	public void keyTyped(KeyEvent e) {

		char key = e.getKeyChar();
		if (key == ' ') {
			if (!stop) {
				t.cancel();
				stop = true;
				clip.stop();
				clip.close();
				try {
					in.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				talk();
			}
		}
	}
}
