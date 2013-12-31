package com.xenoage.zong.io.mp3.out;

import static com.xenoage.utils.error.Err.handle;
import static com.xenoage.utils.log.Report.warning;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.xenoage.utils.document.io.FileOutput;
import com.xenoage.utils.io.OutputStream;
import com.xenoage.utils.jse.io.JseFileUtils;
import com.xenoage.utils.jse.io.JseOutputStream;
import com.xenoage.utils.lang.Lang;
import com.xenoage.zong.Voc;
import com.xenoage.zong.Zong;
import com.xenoage.zong.core.Score;
import com.xenoage.zong.io.wav.out.WAVScoreFileOutput;

/**
 * This class writes an MP3 file from a given {@link Score}.
 * 
 * The LAME tool must be installed and in the system path.
 * If lame can not be found, an error is reported.
 * 
 * @author Andreas Wenger
 */
public class MP3ScoreFileOutput
	implements FileOutput<Score> {

	@Override public void write(Score score, OutputStream stream, String filePath)
		throws IOException {
		//look if LAME is installed
		try {
			Runtime.getRuntime().exec("lame");
		} catch (Exception ex) {
			handle(warning(Lang.get(Voc.CouldNotFindLAME, Zong.WEBSITE + "/lame")));
		}
		//save temporary WAVE file first
		File tempWAVFile = File.createTempFile(getClass().getName(), ".wav");
		new WAVScoreFileOutput().write(score, new JseOutputStream(new FileOutputStream(tempWAVFile)),
			tempWAVFile.getAbsolutePath());
		//create temporary MP3 file
		File tempMP3File = File.createTempFile(getClass().getName(), ".mp3");
		//convert to MP3
		try {
			Process process = new ProcessBuilder("lame", tempWAVFile.getAbsolutePath(),
				tempMP3File.getAbsolutePath()).start();
			////forward LAME error output to console
			//BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			//String line;
			//while ((line = br.readLine()) != null) {
			//   System.out.println(line);
			//}
			process.waitFor();
			int exitValue = process.exitValue();
			if (exitValue != 0)
				throw new IOException("LAME process returned: " + exitValue);
			tempWAVFile.delete();
			//copy MP3 file to output stream
			JseFileUtils.copyFile(tempMP3File.getAbsolutePath(), new JseOutputStream(stream));
			tempMP3File.delete();
		} catch (Exception ex) {
			tempWAVFile.delete();
			tempMP3File.delete();
			throw new IOException(ex);
		}
	}

	@Override public boolean isFilePathRequired(Score document) {
		return true;
	}

}