package com.xenoage.zong.io.musicxml.in;

import com.xenoage.utils.filter.AllFilter;
import com.xenoage.zong.io.musicxml.opus.Opus;
import org.junit.Test;

import java.util.List;

import static com.xenoage.utils.jse.JsePlatformUtils.jsePlatformUtils;
import static com.xenoage.utils.jse.async.Sync.sync;
import static org.junit.Assert.*;

/**
 * Tests for {@link OpusFileInput}.
 * 
 * @author Andreas Wenger
 */
public class OpusFileInputTest {

	@Test public void test()
		throws Exception {
		OpusFileInput opusInput = new OpusFileInput();
		String dir = "data/test/scores/MxlOpusFileInputTest";
		Opus opus = opusInput.readOpusFile(jsePlatformUtils().openFile(dir + "/SomeOpus.xml"));
		//must contain one score, one opus and one opus-link
		assertTrue(opus.getItems().get(0) instanceof com.xenoage.zong.io.musicxml.opus.Score);
		assertTrue(opus.getItems().get(1) instanceof com.xenoage.zong.io.musicxml.opus.Opus);
		assertTrue(opus.getItems().get(2) instanceof com.xenoage.zong.io.musicxml.opus.OpusLink);
		//resolve links
		opus = sync(new OpusLinkResolver(opus, null, dir));
		//check flattened list of scores and load the files
		List<String> scores = opus.getScoreFilenames();
		assertEquals(4, scores.size());
		String[] scoresExpected = new String[] { "BeetAnGeSample.xml", "BrahWiMeSample.mxl",
			"DebuMandSample.xml", "SchbAvMaSample.xml" };
		for (String scoreExpected : scoresExpected) {
			try {
				assertTrue(scoreExpected + " not found", scores.contains(scoreExpected));
				String filePath = dir + "/" + scoreExpected;
				sync(new MusicXmlFileReader(jsePlatformUtils().openFile(filePath),
					filePath, new AllFilter<>()));
			} catch (Exception ex) {
				throw new Exception("Failed to load " + scoreExpected, ex);
			}
		}
	}

}
