package com.juanvvc.flightgear;

import java.util.Date;

/** Models the data that FlilghtGear sent.
 * @author juanvi
 *
 */
public class PlaneData {

	private float speed = 0;
	private int heading = 0;
	private float altitude = 0;
	private float rate = 0 ;
	private float pitch = 0;
	private float roll = 0;
	private float latitude = 0;
	private float longitude = 0;
	private float seconds = 0;
	private Date date = new Date();
	private int rpm = 0;
	private float slip = 0;
	private float turn = 0;
	
	public void parse(final String input) {
		// strip string with new line
		String realInput = input.substring(0, input.indexOf("\n"));
		String[] data = realInput.split(":");
		
		speed = new Float(data[0]).floatValue(); // speed, in knots
		rpm = new Integer(data[1]).intValue(); // RPM
		heading = new Integer(data[2]).intValue(); // heading, in degrees
		altitude = new Float(data[3]).floatValue(); // altitude, in feet
		rate = new Float(data[4]).floatValue(); // rate of climb, in feet per second
		pitch = new Float(data[5]).floatValue(); // pitch, in degrees
		roll = new Float(data[6]).floatValue(); // roll, in degrees
		latitude = new Float(data[7]).floatValue(); // latitude, in degrees
		longitude = new Float(data[8]).floatValue(); // longitude, in degrees
		seconds = new Integer(data[9]).intValue(); // seconds from GMT midnight
		turn = new Float(data[10]).floatValue(); // turn rate, in turns per 2min
		slip = new Float(data[11]).floatValue(); // slip skid, in ??
		
		date = new Date();
	}
	
	public float getSpeed() {
		return speed;
	}
	public int getHeading() {
		return heading;
	}
	public float getAltitude() {
		return altitude;
	}
	public float getRate() {
		return rate;
	}
	public float getPitch() {
		return pitch;
	}
	public float getRoll() {
		return roll;
	}
	public float getLatitude() {
		return latitude;
	}
	public float getLongitude() {
		return longitude;
	}
	public float getSeconds() {
		return seconds;
	}
	public Date getDate() {
		return date;
	}
	public int getRPM() {
		return rpm;
	}
	public float getSlipSkid() {
		return slip;
	}
	public float getTurnRate() {
		return turn;
	}
}


// NEXT; Parse NMEA
/*				BufferedReader reader = new BufferedReader(new CharArrayReader(new String(p.getData()).toCharArray()));

// using magic and a bit of analysis, we know that FlighGear sends three strings:
// GPRMC, GPGGA, PATLA
// The first two are standard NMEA, the third one seems to be a internal line with additional information
// of the radio system

String st1 = reader.readLine();
String st2 = reader.readLine();
String st3 = reader.readLine();
myLog.i(TAG, "Receiving: " + st1);
myLog.i(TAG, "Receiving: " + st2);
myLog.i(TAG, "Receiving: " + st3);

// parse GPRMC
String[] sts = st1.split(",");
if (!sts[0].equals("$GPRMC")) {
	myLog.w(TAG, "GPRMC expected: " + st1);
	continue;
}
if (!sts[2].equals("A")) {
	myLog.w(TAG, "Not active: " + st1);
	continue;
}
double lat = new Double(sts[3]).doubleValue();
// convert from HHMM.M to HH.hh
lat = Math.floor(lat / 100) + (lat % 100) / 60;
if (sts[4].equals("S")) {
	lat = -lat;
}
double lng = new Double(sts[5]).doubleValue();
lng = Math.floor(lng / 100) + (lng % 100) / 60;
if (sts[6].equals("W")) {
	lng = -lng;
}
float s = new Float(sts[7]).floatValue(); // speed in knots
float b = new Float(sts[8]).floatValue(); // bearing

// TODO: this is not working
//SimpleDateFormat df = new SimpleDateFormat("hhmmss ddMM'1'yy");
//Date date = df.parse(sts[1] + " " + sts[9]);
Date date = new Date();

// parse GPGGA
sts = st2.split(",");
if (!sts[0].equals("$GPGGA")) {
	myLog.w(TAG, "GPGGA expected: " + st2);
	continue;
}
int a = new Integer(sts[9]).intValue(); // in feet */

