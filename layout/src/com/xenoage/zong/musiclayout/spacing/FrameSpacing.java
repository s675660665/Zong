package com.xenoage.zong.musiclayout.spacing;

import com.xenoage.utils.math.geom.Size2f;
import com.xenoage.zong.layout.frames.ScoreFrame;
import com.xenoage.zong.utils.exceptions.IllegalMPException;
import lombok.Getter;
import lombok.val;

import java.util.Collections;
import java.util.List;

import static com.xenoage.utils.collections.CollectionUtils.getFirst;
import static com.xenoage.utils.collections.CollectionUtils.getLast;
import static com.xenoage.zong.core.position.MP.atMeasure;

/**
 * The spacing information of the musical layout of a {@link ScoreFrame}.
 * 
 * It contains the usable frame size and the {@link SystemSpacing}s, which contain the
 * staves, measures and elements of the {@link ScoreFrame}.
 *
 * @author Andreas Wenger
 */
@Getter
public class FrameSpacing {

	/** The systems in this frame. */
	public List<SystemSpacing> systems;
	/** The size in mm this frame may use. */
	public Size2f usableSizeMm;
	
	
	public FrameSpacing(List<SystemSpacing> systems, Size2f usableSizeMm) {
		this.systems = systems;
		this.usableSizeMm = usableSizeMm;
		//set backward references
		for (SystemSpacing system : systems)
			system.parentFrame = this;
	}
	
	/**
	 * Returns an empty {@link FrameSpacing} with the given size.
	 */
	public static FrameSpacing empty(Size2f usableSizeMm) {
		return new FrameSpacing(Collections.<SystemSpacing>emptyList(), usableSizeMm);
	}

	/**
	 * Gets the index of the first measure, or -1 if there are no measures.
	 */
	public int getStartMeasure() {
		if (systems.size() == 0)
			return -1;
		else
			return getFirst(systems).getStartMeasure();
	}

	/**
	 * Gets the index of the last measure, or -1 if there are no measures.
	 */
	public int getEndMeasure() {
		if (systems.size() == 0)
			return -1;
		else
			return getLast(systems).getEndMeasure();
	}
	
	public ColumnSpacing getColumn(int scoreMeasure) {
		for (SystemSpacing system : systems) {
			if (scoreMeasure <= system.getEndMeasure())
				return system.getColumn(scoreMeasure);
		}
		throw new IllegalMPException(atMeasure(scoreMeasure));
	}

	public SystemSpacing getSystem(int scoreMeasure) {
		for (val system : systems) {
			if (system.containsMeasure(scoreMeasure))
				return system;
		}
		throw new IllegalMPException(atMeasure(scoreMeasure));
	}

}
