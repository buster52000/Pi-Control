package com.cicc.gpio;

import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.bind.PropertyException;

import com.cicc.gpio.RealLCD.Direction;
import com.cicc.texttospeech.Speak;
import com.cicc.voiceCont.Main;

public class LCDController implements ILCD {

	private ILCD lcd;
	private int lcdMode = 0;
	private ReentrantLock lock;

	private boolean propInternet, propAlm, propSilent;

	public static final byte[] BELL_CHAR = { 0x0, 0x4, 0xe, 0xe, 0xe, 0x1f, 0x4, 0x0 };

	public static final int LCD_MODE_MAIN = 0;
	public static final int LCD_MODE_ALARM = 1;
	public static final int LCD_MODE_WRITE = 2;

	public static final int LCD_PROP_ALM = 0;
	public static final int LCD_PROP_SILENT = 1;
	public static final int LCD_PROP_INTERNET = 2;

	public LCDController(int mode) {
		System.out.println("\""+System.getProperty("user.name")+"\"");
		System.out.println("\""+System.getProperty("os.name")+"\"");
		System.out.println("\""+System.getProperty("os.arch")+"\"");
		if (System.getProperty("os.arch").equals("arm") && System.getProperty("os.name").equals("Linux")) {
			if (!System.getProperty("user.name").equals("root"))
				try {
					throw new Exception("Must be root user");
				} catch (Exception e1) {
					e1.printStackTrace();
					System.exit(0);
				}
			try {
				lcd = new RealLCD();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else
			lcd = new MockupLCD();
		lcdMode = 0;
		lock = new ReentrantLock(true);
		setMode(mode);
	}

	public void setMode(int mode) {
		lcdMode = mode;
		if(mode == LCD_MODE_MAIN)
			showMain();
	}

	private void showMain() {
		Runnable run = new Runnable() {

			@Override
			public void run() {
				aquireLock();
				clear();
				aquireLock();
				while (lcdMode == LCD_MODE_MAIN) {
					Calendar cal = Calendar.getInstance();
					int hour = cal.get(Calendar.HOUR);
					if (hour == 0)
						hour = 12;
					int minute = cal.get(Calendar.MINUTE);
					int second = cal.get(Calendar.SECOND);
					int month = cal.get(Calendar.MONTH) + 1;
					int day = cal.get(Calendar.DAY_OF_MONTH);
					int year = cal.get(Calendar.YEAR);
					String time = (hour < 10 ? "0" : "") + hour + ":" + (minute < 10 ? "0" : "") + minute + ":" + (second < 10 ? "0" : "") + second;
					String date = (month < 10 ? "0" : "") + month + "/" + (day < 10 ? "0" : "") + day + "/" + year;
					aquireLock();
					setCursorPosition(0, (16 - time.length()) / 2);
					write(time);
					setCursorPosition(1, (16 - date.length()) / 2);
					write(date);
					releaseLock();
					long sleepTime = 1000 - System.currentTimeMillis() % 1000;
					if (sleepTime >= 10) {
						try {
							Thread.sleep(sleepTime);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					aquireLock();
					if ((cal.get(Calendar.HOUR_OF_DAY) > 22 || cal.get(Calendar.HOUR_OF_DAY) < 6)) {
						if (getBacklight() != Color.OFF)
							setBacklight(Color.OFF);
					} else {
						if (getBacklight() != Color.RED)
							setBacklight(Color.RED);
					}
					releaseLock();
				}
			}
		};
		Thread clock = new Thread(run, "Clock");
		clock.start();
	}

	public void setProperty(int property, Object value) {

		switch (property) {
		case LCD_PROP_ALM:
			if (value instanceof Boolean)
				propAlm = ((Boolean) value).booleanValue();
			break;
		case LCD_PROP_SILENT:
			if (value instanceof Boolean)
				propSilent = ((Boolean) value).booleanValue();
			break;
		case LCD_PROP_INTERNET:
			if (value instanceof Boolean)
				propInternet = ((Boolean) value).booleanValue();
			break;
		default:
			try {
				throw new PropertyException("Unknown LCD Property");
			} catch (PropertyException e) {
				e.printStackTrace();
			}
		}
		// updateProperties();

	}

	public int getMode() {
		return lcdMode;
	}

	@Override
	public void setText(String s) {
		if (!heldByCurrentThread())
			return;
		try {
			lcd.setText(s);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void write(int value) {
		if (!heldByCurrentThread())
			return;
		try {
			lcd.write(value);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void write(String str) {
		if (!heldByCurrentThread())
			return;
		try {
			lcd.write(str);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setText(int row, String string) {
		if (!heldByCurrentThread())
			return;
		try {
			lcd.setText(row, string);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setCursorPosition(int row, int column) {
		if (!heldByCurrentThread())
			return;
		try {
			lcd.setCursorPosition(row, column);
			// Thread.sleep(10);
		} catch (IOException/* | InterruptedException */e) {
			e.printStackTrace();
		}
	}

	@Override
	public void stop() {
		if (!heldByCurrentThread())
			return;
		try {
			lcd.stop();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void clear() {
		if (!heldByCurrentThread())
			return;
		try {
			lcd.clear();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void home() {
		if (!heldByCurrentThread())
			return;
		try {
			lcd.home();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setCursorEnabled(boolean enable) {
		if (!heldByCurrentThread())
			return;
		try {
			lcd.setCursorEnabled(enable);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean isCursorEnabled() {
		if (!heldByCurrentThread())
			return false;
		boolean enabled = lcd.isCursorEnabled();
		return enabled;
	}

	@Override
	public void setDisplayEnabled(boolean enable) {
		if (!heldByCurrentThread())
			return;
		try {
			lcd.setDisplayEnabled(enable);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean isDisplayEnabled() {
		if (!heldByCurrentThread())
			return false;
		boolean enabled = lcd.isDisplayEnabled();
		return enabled;
	}

	@Override
	public void setBlinkEnabled(boolean enable) {
		if (!heldByCurrentThread())
			return;
		try {
			lcd.setBlinkEnabled(enable);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean isBlinkEnabled() {
		if (!heldByCurrentThread())
			return false;
		boolean enabled = lcd.isBlinkEnabled();
		return enabled;
	}

	@Override
	public void setBacklight(Color color) {
		if (!heldByCurrentThread())
			return;
		try {
			lcd.setBacklight(color);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Color getBacklight() {
		if (!heldByCurrentThread())
			return null;
		Color light = null;
		try {
			light = lcd.getBacklight();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return light;
	}

	@Override
	public void scrollDisplay(Direction direction) {
		if (!heldByCurrentThread())
			return;
		try {
			lcd.scrollDisplay(direction);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setTextFlowDirection(Direction direction) {
		if (!heldByCurrentThread())
			return;
		try {
			lcd.setTextFlowDirection(direction);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setAutoScrollEnabled(boolean enable) {
		if (!heldByCurrentThread())
			return;
		try {
			lcd.setAutoScrollEnabled(enable);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean isAutoScrollEnabled() {
		if (!heldByCurrentThread())
			return false;
		boolean enabled = lcd.isAutoScrollEnabled();
		return enabled;
	}

	@Override
	public boolean isButtonPressed(Button button) {
		if (!heldByCurrentThread())
			return false;
		boolean pressed = false;
		try {
			pressed = lcd.isButtonPressed(button);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return pressed;
	}

	@Override
	public int buttonsPressedBitmask() {
		if (!heldByCurrentThread())
			return -1;
		int bitMask = -1;
		try {
			bitMask = lcd.buttonsPressedBitmask();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bitMask;
	}

	public void createChar(byte location, byte[] charmap, int charID) {
		if (!heldByCurrentThread())
			return;
		if (lcd instanceof RealLCD) {
			((RealLCD) lcd).createChar(location, charmap, charID);
		}
	}

	public int getCharIDAt(int location) {
		if (!heldByCurrentThread())
			return -1;
		if (lcd instanceof RealLCD) {
			int charID = ((RealLCD) lcd).getCharIDAt(location);
			return charID;
		}
		return -1;
	}

	// private void autoAquireLock() {
	// if (!lock.isHeldByCurrentThread())
	// lock.lock();
	// }
	//
	// private void autoReleaseLock() {
	// if (lock.isHeldByCurrentThread() && !manuelLock)
	// while (lock.getHoldCount() > 0)
	// lock.unlock();
	// }

	public void aquireLock() {
		lock.lock();
	}

	public void releaseLock() {
		if (lock.isHeldByCurrentThread())
			while (lock.getHoldCount() > 0)
				lock.unlock();
	}

	private boolean heldByCurrentThread() {
		if (lock.isHeldByCurrentThread())
			return true;
		try {
			throw new Exception("Lock must be aquired before using the lcd");
		} catch (Exception e) {
			e.printStackTrace();
			Speak.say("the lock was not aquired before writing to the L.C.D.");
		}
		return false;
	}

}