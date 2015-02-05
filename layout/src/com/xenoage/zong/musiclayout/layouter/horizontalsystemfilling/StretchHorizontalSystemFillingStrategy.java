package com.xenoage.zong.musiclayout.layouter.horizontalsystemfilling;

import static com.xenoage.utils.collections.CList.clist;
import static com.xenoage.utils.collections.CollectionUtils.alist;
import static com.xenoage.utils.kernel.Range.range;
import static com.xenoage.utils.kernel.Range.rangeReverse;

import java.util.List;

import com.xenoage.utils.collections.CList;
import com.xenoage.zong.musiclayout.BeatOffset;
import com.xenoage.zong.musiclayout.SystemArrangement;
import com.xenoage.zong.musiclayout.spacing.ColumnSpacing;
import com.xenoage.zong.musiclayout.spacing.horizontal.MeasureElementsSpacing;
import com.xenoage.zong.musiclayout.spacing.horizontal.MeasureSpacing;
import com.xenoage.zong.musiclayout.spacing.horizontal.ElementSpacing;
import com.xenoage.zong.musiclayout.spacing.horizontal.VoiceSpacing;

/**
 * This horizontal system filling strategy
 * stretches all measures within the given system,
 * so that they use the whole useable width
 * of the system.
 * 
 * @author Andreas Wenger
 */
public class StretchHorizontalSystemFillingStrategy
	implements HorizontalSystemFillingStrategy {

	public static final StretchHorizontalSystemFillingStrategy instance =
		new StretchHorizontalSystemFillingStrategy();


	/**
	 * Stretches the measures of the given system, so that
	 * it uses the whole usable width of the system.
	 */
	@Override public SystemArrangement computeSystemArrangement(SystemArrangement systemArrangement,
		float usableWidth) {
		//compute width of all voice spacings
		//(leading spacings are not stretched)
		float voicesWidth = 0;
		float leadingsWidth = 0;
		for (ColumnSpacing mcs : systemArrangement.getColumnSpacings()) {
			voicesWidth += mcs.getVoicesWidth();
			leadingsWidth += mcs.getLeadingWidth();
		}

		//compute the stretching factor for the voice spacings
		if (voicesWidth == 0)
			return systemArrangement;
		float stretch = (usableWidth - leadingsWidth) / voicesWidth;

		//stretch the voice spacings
		//measure columns
		CList<ColumnSpacing> newMCSpacings = clist(systemArrangement.getColumnSpacings().size());
		for (ColumnSpacing column : systemArrangement.getColumnSpacings()) {
			//beat offsets
			CList<BeatOffset> newBeatOffsets = clist(column.getBeatOffsets().length);
			for (BeatOffset oldBeatOffset : column.getBeatOffsets()) {
				//stretch the offset
				newBeatOffsets.add(oldBeatOffset.withOffsetMm(oldBeatOffset.getOffsetMm() * stretch));
			}
			CList<BeatOffset> newBarlineOffsets = clist(column.getBarlineOffsets().length);
			for (BeatOffset oldBarlineOffset : column.getBarlineOffsets()) {
				//stretch the offset
				newBarlineOffsets.add(oldBarlineOffset.withOffsetMm(oldBarlineOffset.getOffsetMm() *
					stretch));
			}
			//measures
			CList<MeasureSpacing> newMeasureSpacings = clist(column.getMeasureSpacings().length);
			for (MeasureSpacing oldMS : column.getMeasureSpacings()) {
				//measure elements
				List<ElementSpacing> me = oldMS.getMeasureElementsSpacings().elements;
				List<ElementSpacing> newMESElements = alist(me.size());
				for (int i : range(me)) {
					//stretch the offset
					newMESElements.add(me.get(i).withOffset(me.get(i).offsetIs * stretch));
				}
				MeasureElementsSpacing newMES = new MeasureElementsSpacing(newMESElements);
				//voices
				CList<VoiceSpacing> newVSs = clist(oldMS.getVoiceSpacings().size());
				for (VoiceSpacing oldVS : oldMS.getVoiceSpacings()) {
					//spacing elements
					//traverse in reverse order, so we can align grace elements correctly
					//grace elements are not stretched, but the distance to their following full element
					//stays the same
					List<ElementSpacing> newSEs = alist(oldVS.spacingElements.size());
					float lastOldOffset = Float.NaN;
					float lastNewOffset = Float.NaN;
					for (int i : rangeReverse(oldVS.spacingElements)) {
						ElementSpacing oldSE = oldVS.spacingElements.get(i);
						if (oldSE.grace && !Float.isNaN(lastOldOffset)) {
							//grace element: keep distance to following element
							float oldDistance = lastOldOffset - oldSE.offsetIs;
							lastNewOffset = lastNewOffset - oldDistance;
						}
						else {
							//normal element: stretch the offset
							lastNewOffset = oldSE.offsetIs * stretch;
						}
						lastOldOffset = oldSE.offsetIs;
						newSEs.add(0, oldSE.withOffset(lastNewOffset));
					}
					newVSs.add(new VoiceSpacing(oldVS.voice, oldVS.interlineSpace, newSEs));
				}
				newMeasureSpacings.add(new MeasureSpacing(oldMS.getMp(), newVSs.close(), newMES,
					oldMS.getLeadingSpacing()));
			}

			newMCSpacings.add(new ColumnSpacing(column.getScore(), newMeasureSpacings.toArray(MeasureSpacing.empty),
				newBeatOffsets.toArray(BeatOffset.empty), newBarlineOffsets.toArray(BeatOffset.empty)));

		}

		//create and return the new system
		return systemArrangement.withSpacings(newMCSpacings.close(), usableWidth);
	}

}
