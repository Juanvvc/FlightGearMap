package com.juanvvc.flightgear.maps;

import java.io.InputStream;

import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase.LowMemoryException;
import org.osmdroid.tileprovider.tilesource.ITileSource;

import com.juanvvc.flightgear.MyLog;
import com.juanvvc.flightgear.R;

import android.content.Context;
import android.graphics.drawable.Drawable;

public class SolidTileSource implements ITileSource {
	private Context context;

	public SolidTileSource(Context c) {
		this.context = c;
	}

	@Override
	public Drawable getDrawable(String arg0) throws LowMemoryException {
		MyLog.i(this, "Loading from file");
		return context.getResources().getDrawable(R.drawable.solidtile);
	}

	@Override
	public Drawable getDrawable(InputStream arg0) throws LowMemoryException {
		MyLog.i(this, "Loading from stream");
		return context.getResources().getDrawable(R.drawable.solidtile);
	}

	@Override
	public int getMaximumZoomLevel() {
		return 10;
	}

	@Override
	public int getMinimumZoomLevel() {
		return 2;
	}

	@Override
	public String getTileRelativeFilenameString(MapTile arg0) {
		MyLog.i(this, "Loading from tile");
		return "";
	}

	@Override
	public int getTileSizePixels() {
		return 256;
	}

	@Override
	public String localizedName(ResourceProxy arg0) {
		return "SolidTileSource";
	}

	@Override
	public String name() {
		return "SolidTileSource";
	}

	@Override
	public int ordinal() {
		return 1;
	}

}
