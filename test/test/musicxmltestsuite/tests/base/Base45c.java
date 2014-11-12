package musicxmltestsuite.tests.base;

import com.xenoage.zong.core.music.barline.Barline;
import com.xenoage.zong.core.music.barline.BarlineStyle;

public interface Base45c
	extends Base {

	@Override default String getFileName() {
		return "45c-RepeatMultipleTimes.xml";
	}
	
	Barline[] expectedStartBarlines = {
		null,
		Barline.barlineForwardRepeat(BarlineStyle.HeavyLight),
		null,
		null,
		null,
		null,
		null,
		null,
	};

	Barline[] expectedEndBarlines = {
		null,
		null,
		Barline.barlineBackwardRepeat(BarlineStyle.LightHeavy, 5),
		null,
		null,
		null,
		Barline.barlineBackwardRepeat(BarlineStyle.LightHeavy, 3),
		Barline.barline(BarlineStyle.LightHeavy),
	};
	
}
