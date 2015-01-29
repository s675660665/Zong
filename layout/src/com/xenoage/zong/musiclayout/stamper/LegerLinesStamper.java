package com.xenoage.zong.musiclayout.stamper;

import static com.xenoage.utils.kernel.Range.range;
import static com.xenoage.zong.musiclayout.notations.chord.NoteSuspension.None;
import static com.xenoage.zong.musiclayout.notations.chord.NoteSuspension.Right;

import com.xenoage.zong.core.music.chord.Chord;
import com.xenoage.zong.musiclayout.notations.ChordNotation;
import com.xenoage.zong.musiclayout.notations.chord.NoteAlignment;
import com.xenoage.zong.musiclayout.notations.chord.NoteSuspension;
import com.xenoage.zong.musiclayout.notations.chord.NotesAlignment;
import com.xenoage.zong.musiclayout.stampings.LegerLineStamping;
import com.xenoage.zong.musiclayout.stampings.StaffStamping;

/**
 * Creates {@link LegerLineStamping}s from a {@link ChordNotation}.
 *  
 * @author Andreas Wenger
 */
public class LegerLinesStamper {
	
	public static final LegerLinesStamper legerLinesStamper = new LegerLinesStamper();
	
	public static final LegerLineStamping[] empty = new LegerLineStamping[0];
	
	
	public LegerLineStamping[] stamp(ChordNotation chordNotation, float chordXMm, StaffStamping staffStamping) {
		Chord chord = chordNotation.getElement();
		NotesAlignment cna = chordNotation.getNotesAlignment();
		int bottomCount = getBottomCount(cna.getBottomNoteAlignment().lp);
		int topCount = getTopCount(cna.getTopNoteAlignment().lp, staffStamping.linesCount);
		if (bottomCount > 0 || topCount > 0) {
			//horizontal position and width (may differ above and below staff, dependent on suspended notes)
			NoteSuspension bottomSuspension = getBottomSuspension(cna.noteAlignments);
			float xTopMm = getXMm(chordXMm, cna.noteheadWidthIs, bottomSuspension, staffStamping.is);
			float widthBottomIs = getWidthIs(bottomSuspension != None);
			NoteSuspension topSuspension = getTopSuspension(cna.noteAlignments, staffStamping.linesCount);
			float xBottomMm = getXMm(chordXMm, cna.noteheadWidthIs, topSuspension, staffStamping.is);
			float widthTopIs = getWidthIs(bottomSuspension != None);
			//vertical positions
			int[] bottomLps = getBottomLps(bottomCount);
			int[] topLps = getTopLps(topCount, staffStamping.linesCount);
			//create stampings
			LegerLineStamping[] ret = new LegerLineStamping[bottomCount + topCount];
			for (int i : range(bottomCount))
				ret[i] = new LegerLineStamping(staffStamping, chord, xBottomMm, bottomLps[i], widthBottomIs);
			for (int i : range(topCount))
				ret[bottomCount + i] = new LegerLineStamping(staffStamping, chord, xTopMm, topLps[i], widthTopIs);
			return ret;
		}
		else {
			return empty;
		}
	}
	
	float getXMm(float chordXMm, float noteheadWidthIs, NoteSuspension suspension, float staffIs) {
		float leftNoteXMm = chordXMm;
		//center x on middle of chord notes
		if (suspension == None)
			leftNoteXMm += noteheadWidthIs * staffIs / 2;
		else if (suspension == Right)
			leftNoteXMm += noteheadWidthIs * staffIs;
		return leftNoteXMm;
	}
	
	NoteSuspension getBottomSuspension(NoteAlignment[] noteAlignments) {
		//find a suspended note which needs a leger line on the bottom side
		for (NoteAlignment note : noteAlignments)
			if (note.suspension != None && note.lp <= -2)
				return note.suspension;
		return None;
	}
	
	NoteSuspension getTopSuspension(NoteAlignment[] noteAlignments, int staffLinesCount) {
		//find a suspended note which needs a leger line on the top side
		for (NoteAlignment note : noteAlignments)
			if (note.suspension != None && note.lp >= staffLinesCount * 2)
				return note.suspension;
		return None;
	}
	
	float getWidthIs(boolean suspended) {
		return suspended ? LegerLineStamping.lengthSuspendedIs : LegerLineStamping.lengthNormalIs;
	}
	
	int getTopCount(int topNoteLp, int staffLinesCount) {
		int staffTopLp = staffLinesCount * 2 - 2;
		if (topNoteLp > staffTopLp + 1)
			return (topNoteLp - staffTopLp) / 2;
		else
			return 0;
	}
	
	int getBottomCount(int bottomNoteLp) {
		if (bottomNoteLp < -1)
			return bottomNoteLp / -2;
		else
			return 0;
	}
	
	int[] getBottomLps(int legerLinesBottomCount) {
		int[] ret = new int[legerLinesBottomCount];
		//in ascending order
		for (int i : range(legerLinesBottomCount)) {
			int lp = -2 - (legerLinesBottomCount - i - 1) * 2;
			ret[i] = lp;
		}
		return ret;
	}
	
	int[] getTopLps(int legerLinesTopCount, int staffLinesCount) {
		int[] ret = new int[legerLinesTopCount];
		//in ascending order
		for (int i : range(legerLinesTopCount)) {
			int lp = (staffLinesCount + i) * 2;
			ret[i] = lp;
		}
		return ret;
	}

}
