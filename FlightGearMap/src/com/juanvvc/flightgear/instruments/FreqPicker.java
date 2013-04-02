/**
 * 
 */
package com.juanvvc.flightgear.instruments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.juanvvc.flightgear.MyLog;
import com.juanvvc.flightgear.R;

/**
 * @author juanvi
 *
 */
public class FreqPicker extends LinearLayout implements OnClickListener {
	private boolean isDirty = false;
	private boolean showingSelected = true;
	
	public FreqPicker(Context context) {
		super(context);
		this.initComponent(context);
	}
	public FreqPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.initComponent(context);
	}

	private void initComponent(final Context context) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(R.layout.freqpicker, null, false);
		this.addView(v);
		
		showSelected(true);
		setSelectedFreq(123.45, 123.45);
		
		((Button) this.findViewById(R.id.swapfreq)).setOnClickListener(this);
		((TextView) this.findViewById(R.id.freq2)).setOnClickListener(this);
	}
	
	public void setSelectedFreq(final double f1, final double f2) {
		if ( f1 > -1 ) {
			((TextView) this.findViewById(R.id.freq1)).setText(Double.valueOf(f1).toString());
		}
		if ( f2 > -1 )  {
			((TextView) this.findViewById(R.id.freq2)).setText(Double.valueOf(f2).toString());
		}
		isDirty = true;
	}
	
	public double getSelectedFreq() {
		TextView src;
		if (showingSelected) {
			src = (TextView) this.findViewById(R.id.freq1);
		} else {
			src = (TextView) this.findViewById(R.id.freq2);
		}
		isDirty = false;
		try {
			return Double.parseDouble(src.getText().toString());
		} catch (Exception e) {
			return 0;
		}
	}
	
	public void setLabel(final String l) {
		((TextView) this.findViewById(R.id.label)).setText(l);
	}

	@Override
	public void onClick(final View v) {
		final TextView t1 = (TextView) this.findViewById(R.id.freq1);
		final TextView t2 = (TextView) this.findViewById(R.id.freq2);
		String s;
		switch(v.getId()){
		case R.id.swapfreq:
			s = t1.getText().toString();
			t1.setText(t2.getText());
			t2.setText(s);
			this.setSelectedFreq(-1, -1);
			break;
		default:
			final EditText input = new EditText(this.getContext());
			input.setInputType(Activity.DEFAULT_KEYS_DIALER);
			input.setKeyListener(new DigitsKeyListener(false, true));

			new AlertDialog.Builder(this.getContext())
					.setTitle(getContext().getText(R.string.sel_freq))
					.setView(input)
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(
										final DialogInterface dialog,
										final int which) {
									try {
										double f = Double.parseDouble(input.getText().toString());
										FreqPicker.this.setSelectedFreq(-1,  f);
									} catch (NumberFormatException e) {
										MyLog.e(FreqPicker.this, e.toString());
									}
								}
							}).setNegativeButton(R.string.cancel, null).show();
		}
	}
	
	public void showSelected(final boolean show) {
		if (show) {
			this.findViewById(R.id.freq1layout).setVisibility(VISIBLE);
			this.findViewById(R.id.swapfreq).setVisibility(VISIBLE);			
		} else {
			this.findViewById(R.id.freq1layout).setVisibility(GONE);
			this.findViewById(R.id.swapfreq).setVisibility(GONE);
		}
		showingSelected = show;
		
	}
			
}
