package aimax.osm.reader;

import java.io.File;
import java.io.InputStream;

import aimax.osm.data.MapDataStore;

/**
 * Common interface for reading maps from file.
 * @author R. Lunde
 */
public interface MapReader {
	public void readMap(File file, MapDataStore mapData);
	public void readMap(InputStream is, MapDataStore mapData);
	public String[] fileFormatDescriptions();
	public String[] fileFormatExtensions();
}