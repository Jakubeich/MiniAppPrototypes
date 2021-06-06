package com.jakubeich.videorecorder;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.util.Vector;

import javax.media.Buffer;
import javax.media.ConfigureCompleteEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.DataSink;
import javax.media.EndOfMediaEvent;
import javax.media.Format;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.PrefetchCompleteEvent;
import javax.media.Processor;
import javax.media.RealizeCompleteEvent;
import javax.media.ResourceUnavailableEvent;
import javax.media.Time;
import javax.media.control.TrackControl;
import javax.media.datasink.DataSinkErrorEvent;
import javax.media.datasink.DataSinkEvent;
import javax.media.datasink.DataSinkListener;
import javax.media.datasink.EndOfStreamEvent;
import javax.media.format.VideoFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.FileTypeDescriptor;
import javax.media.protocol.PullBufferDataSource;
import javax.media.protocol.PullBufferStream;

public class JpegImagesToMovie implements ControllerListener, DataSinkListener {

	public boolean doIt(int width, int height, int frameRate, Vector inFiles, MediaLocator outML)
			throws MalformedURLException {
		ImageDataSource ids = new ImageDataSource(width, height, frameRate, inFiles);

		Processor p;

		try {
			p = Manager.createProcessor(ids);
		} catch (Exception e) {
			System.err.println("Yikes!  Cannot create a processor from the data source.");
			return false;
		}

		p.addControllerListener(this);

		p.configure();
		if (!waitForState(p, p.Configured)) {
			System.err.println("Failed to configure the processor.");
			return false;
		}

		p.setContentDescriptor(new ContentDescriptor(FileTypeDescriptor.QUICKTIME));

		TrackControl tcs[] = p.getTrackControls();
		Format f[] = tcs[0].getSupportedFormats();
		if (f == null || f.length <= 0) {
			System.err.println("The mux does not support the input format: " + tcs[0].getFormat());
			return false;
		}

		tcs[0].setFormat(f[0]);

		p.realize();
		if (!waitForState(p, p.Realized)) {
			System.err.println("Failed to realize the processor.");
			return false;
		}

		DataSink dsink;
		if ((dsink = createDataSink(p, outML)) == null) {
			System.err.println("Failed to create a DataSink for the given output MediaLocator: " + outML);
			return false;
		}

		dsink.addDataSinkListener(this);
		fileDone = false;

		System.out.println("Generating the video : " + outML.getURL().toString());

		try {
			p.start();
			dsink.start();
		} catch (IOException e) {
			System.err.println("IO error during processing");
			return false;
		}

		waitForFileDone();

		try {
			dsink.close();
		} catch (Exception e) {
		}
		p.removeControllerListener(this);

		System.out.println("Video creation completed!!!!!");
		return true;
	}

	DataSink createDataSink(Processor p, MediaLocator outML) {

		DataSource ds;

		if ((ds = p.getDataOutput()) == null) {
			System.err.println("Something is really wrong: the processor does not have an output DataSource");
			return null;
		}

		DataSink dsink;

		try {
			// System.err.println("- create DataSink for: " + outML);
			dsink = Manager.createDataSink(ds, outML);
			dsink.open();
		} catch (Exception e) {
			System.err.println("Cannot create the DataSink: " + e);
			return null;
		}

		return dsink;
	}

	Object waitSync = new Object();
	boolean stateTransitionOK = true;

	boolean waitForState(Processor p, int state) {
		synchronized (waitSync) {
			try {
				while (p.getState() < state && stateTransitionOK)
					waitSync.wait();
			} catch (Exception e) {
			}
		}
		return stateTransitionOK;
	}

	public void controllerUpdate(ControllerEvent evt) {

		if (evt instanceof ConfigureCompleteEvent || evt instanceof RealizeCompleteEvent
				|| evt instanceof PrefetchCompleteEvent) {
			synchronized (waitSync) {
				stateTransitionOK = true;
				waitSync.notifyAll();
			}
		} else if (evt instanceof ResourceUnavailableEvent) {
			synchronized (waitSync) {
				stateTransitionOK = false;
				waitSync.notifyAll();
			}
		} else if (evt instanceof EndOfMediaEvent) {
			evt.getSourceController().stop();
			evt.getSourceController().close();
		}
	}

	Object waitFileSync = new Object();
	boolean fileDone = false;
	boolean fileSuccess = true;

	boolean waitForFileDone() {
		synchronized (waitFileSync) {
			try {
				while (!fileDone)
					waitFileSync.wait();
			} catch (Exception e) {
			}
		}
		return fileSuccess;
	}

	public void dataSinkUpdate(DataSinkEvent evt) {

		if (evt instanceof EndOfStreamEvent) {
			synchronized (waitFileSync) {
				fileDone = true;
				waitFileSync.notifyAll();
			}
		} else if (evt instanceof DataSinkErrorEvent) {
			synchronized (waitFileSync) {
				fileDone = true;
				fileSuccess = false;
				waitFileSync.notifyAll();
			}
		}
	}

	static void prUsage() {
		System.err.println(
				"Usage: java JpegImagesToMovie -w <width> -h <height> -f <frame rate> -o <output URL> <input JPEG file 1> <input JPEG file 2> ...");
		System.exit(-1);
	}

	static MediaLocator createMediaLocator(String url) {

		MediaLocator ml;

		if (url.indexOf(":") > 0 && (ml = new MediaLocator(url)) != null)
			return ml;

		if (url.startsWith(File.separator)) {
			if ((ml = new MediaLocator("file:" + url)) != null)
				return ml;
		} else {
			String file = "file:" + System.getProperty("user.dir") + File.separator + url;
			if ((ml = new MediaLocator(file)) != null)
				return ml;
		}

		return null;
	}

	class ImageDataSource extends PullBufferDataSource {

		ImageSourceStream streams[];

		ImageDataSource(int width, int height, int frameRate, Vector images) {
			streams = new ImageSourceStream[1];
			streams[0] = new ImageSourceStream(width, height, frameRate, images);
		}

		public void setLocator(MediaLocator source) {
		}

		public MediaLocator getLocator() {
			return null;
		}

		public String getContentType() {
			return ContentDescriptor.RAW;
		}

		public void connect() {
		}

		public void disconnect() {
		}

		public void start() {
		}

		public void stop() {
		}

		public PullBufferStream[] getStreams() {
			return streams;
		}

		public Time getDuration() {
			return DURATION_UNKNOWN;
		}

		public Object[] getControls() {
			return new Object[0];
		}

		public Object getControl(String type) {
			return null;
		}
	}

	class ImageSourceStream implements PullBufferStream {

		Vector images;
		int width, height;
		VideoFormat format;

		int nextImage = 0;
		boolean ended = false;

		public ImageSourceStream(int width, int height, int frameRate, Vector images) {
			this.width = width;
			this.height = height;
			this.images = images;

			format = new VideoFormat(VideoFormat.JPEG, new Dimension(width, height), Format.NOT_SPECIFIED,
					Format.byteArray, (float) frameRate);
		}

		public boolean willReadBlock() {
			return false;
		}

		public void read(Buffer buf) throws IOException {

			if (nextImage >= images.size()) {
				buf.setEOM(true);
				buf.setOffset(0);
				buf.setLength(0);
				ended = true;
				return;
			}

			String imageFile = (String) images.elementAt(nextImage);
			nextImage++;

			RandomAccessFile raFile;
			raFile = new RandomAccessFile(imageFile, "r");

			byte data[] = null;

			if (buf.getData() instanceof byte[])
				data = (byte[]) buf.getData();

			if (data == null || data.length < raFile.length()) {
				data = new byte[(int) raFile.length()];
				buf.setData(data);
			}

			raFile.readFully(data, 0, (int) raFile.length());

			buf.setOffset(0);
			buf.setLength((int) raFile.length());
			buf.setFormat(format);
			buf.setFlags(buf.getFlags() | buf.FLAG_KEY_FRAME);

			raFile.close();
		}

		public Format getFormat() {
			return format;
		}

		public ContentDescriptor getContentDescriptor() {
			return new ContentDescriptor(ContentDescriptor.RAW);
		}

		public long getContentLength() {
			return 0;
		}

		public boolean endOfStream() {
			return ended;
		}

		public Object[] getControls() {
			return new Object[0];
		}

		public Object getControl(String type) {
			return null;
		}
	}
}
