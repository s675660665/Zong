package com.xenoage.zong.musiclayout.stamper;

import static com.xenoage.utils.kernel.Range.range;
import static com.xenoage.zong.core.music.format.SP.sp;
import static com.xenoage.zong.musiclayout.stamper.LegerLinesStamper.legerLinesStamper;
import static com.xenoage.zong.musiclayout.stamper.StemStamper.stemStamper;

import com.xenoage.utils.color.Color;
import com.xenoage.zong.core.music.beam.Beam;
import com.xenoage.zong.core.music.chord.Chord;
import com.xenoage.zong.core.music.chord.StemDirection;
import com.xenoage.zong.core.music.util.DurationInfo;
import com.xenoage.zong.musiclayout.layouter.cache.util.OpenBeamMiddleStem;
import com.xenoage.zong.musiclayout.layouter.scoreframelayout.util.ChordStampings;
import com.xenoage.zong.musiclayout.notations.ChordNotation;
import com.xenoage.zong.musiclayout.notations.chord.AccidentalAlignment;
import com.xenoage.zong.musiclayout.notations.chord.AccidentalsAlignment;
import com.xenoage.zong.musiclayout.notations.chord.ArticulationAlignment;
import com.xenoage.zong.musiclayout.notations.chord.ArticulationsAlignment;
import com.xenoage.zong.musiclayout.notations.chord.NoteAlignment;
import com.xenoage.zong.musiclayout.notations.chord.NotesAlignment;
import com.xenoage.zong.musiclayout.settings.ChordWidths;
import com.xenoage.zong.musiclayout.settings.LayoutSettings;
import com.xenoage.zong.musiclayout.stamper.StemStamper.Type;
import com.xenoage.zong.musiclayout.stampings.AccidentalStamping;
import com.xenoage.zong.musiclayout.stampings.ArticulationStamping;
import com.xenoage.zong.musiclayout.stampings.FlagsStamping;
import com.xenoage.zong.musiclayout.stampings.LegerLineStamping;
import com.xenoage.zong.musiclayout.stampings.NoteheadStamping;
import com.xenoage.zong.musiclayout.stampings.ProlongationDotStamping;
import com.xenoage.zong.musiclayout.stampings.StaffStamping;
import com.xenoage.zong.musiclayout.stampings.StemStamping;
import com.xenoage.zong.symbols.SymbolPool;

/**
 * Creates stampings for chords.
 * 
 * Beams, ties, slurs and lyrics are not handled here,
 * those must be handled by other classes.
 * 
 * @author Andreas Wenger
 */
@SuppressWarnings("Tests needed")
public class ChordStamper {
	
	public static final ChordStamper chordStamper = new ChordStamper();
	
	
	public ChordStampings create(ChordNotation chordNotation, float chordXMm, StaffStamping staffStamping,
		SymbolPool symbolPool, LayoutSettings layoutSettings) {
		Chord chord = chordNotation.element;
		boolean grace = chord.isGrace();
		float scaling = (grace ? layoutSettings.scalingGrace : 1);
		ChordWidths chordWidths = (grace ? layoutSettings.graceChordWidths : layoutSettings.chordWidths);

		FlagsStamping flags = null;

		float leftNoteXMm = getLeftNoteXMm(chordXMm,
			chordNotation.notesAlignment, staffStamping.is);
		
		//stem
		StemStamping stem = null;
		OpenBeamMiddleStem openStem = null;
		StemStamper.Type stemType = stemStamper.getStemType(chordNotation);
		if (stemType == Type.Stem)
			stem = stemStamper.stamp(chordNotation, leftNoteXMm, staffStamping);
		else if (stemType == Type.MiddleBeamStem)
			openStem = stemStamper.openBeamMiddleStem(chordNotation, leftNoteXMm, staffStamping);
		
		//type of notehead
		int noteheadType = NoteheadStamping.NOTEHEAD_WHOLE;
		DurationInfo.Type symbolType = DurationInfo.getNoteheadSymbolType(chord.getDisplayedDuration());
		if (symbolType == DurationInfo.Type.Half)
			noteheadType = NoteheadStamping.NOTEHEAD_HALF;
		else if (symbolType == DurationInfo.Type.Quarter)
			noteheadType = NoteheadStamping.NOTEHEAD_QUARTER;

		//noteheads
		NotesAlignment nas = chordNotation.notesAlignment;
		NoteheadStamping[] noteheads = new NoteheadStamping[nas.getNotesCount()];
		for (int iNote : range(noteheads)) {
			NoteAlignment na = nas.getNoteAlignment(iNote);
			Color color = Color.black;
			NoteheadStamping noteSt = new NoteheadStamping(chord, noteheadType, color, staffStamping, sp(
				leftNoteXMm + na.offsetIs * staffStamping.is, na.lp), scaling, symbolPool);
			noteheads[iNote] = noteSt;
		}

		//flags (only drawn if there is no beam)
		int flagsCount = DurationInfo.getFlagsCount(chord.getDisplayedDuration());
		Beam beam = chord.getBeam();
		StemDirection stemDir = chordNotation.stemDirection;
		if (beam == null && flagsCount > 0) {
			FlagsStamping.FlagsDirection flag = (stemDir == StemDirection.Up ? FlagsStamping.FlagsDirection.Down
				: FlagsStamping.FlagsDirection.Up);
			flags = new FlagsStamping(flag, flagsCount, scaling, staffStamping, chord, sp(
				leftNoteXMm + nas.stemOffsetIs * staffStamping.is, chordNotation.stemAlignment.endLp), symbolPool);
		}

		//accidentals
		AccidentalsAlignment caa = chordNotation.getAccidentalsAlignment();
		AccidentalStamping[] accidentals = new AccidentalStamping[0];
		if (caa != null) {
			accidentals = new AccidentalStamping[caa.accidentals.length];
			for (int iAcc : range(accidentals)) {
				AccidentalAlignment aa = caa.accidentals[iAcc];
				AccidentalStamping accSt = new AccidentalStamping(chord, aa.type, staffStamping,
					sp(chordXMm +
							(aa.offset - chordNotation.getWidth().getFrontGap() + 0.5f /* 0.5f: half accidental width - TODO */) *
							staffStamping.is, aa.linePosition), 1, symbolPool);
				accidentals[iAcc] = accSt;
			}
		}

		//dots
		int[] dotPositions = nas.getDotsLp();
		int dotsPerNote = nas.getDotsPerNoteCount();
		ProlongationDotStamping[] dots = new ProlongationDotStamping[dotPositions.length * dotsPerNote];
		for (int iNote : range(dotPositions)) {
			for (int iDot : range(dotsPerNote)) {
				ProlongationDotStamping dotSt = new ProlongationDotStamping(staffStamping, chord, sp(
					leftNoteXMm + nas.getDotsOffsetIs(iDot) * staffStamping.is, dotPositions[iNote]), symbolPool);
				dots[iNote * dotsPerNote + iDot] = dotSt;
			}
		}

		//articulations
		ArticulationsAlignment cara = chordNotation.getArticulationsAlignment();
		ArticulationStamping[] articulations = new ArticulationStamping[0];
		if (cara != null) {
			articulations = new ArticulationStamping[cara.articulations.length];
			float noteheadWidth = chordWidths.get(chord.getDuration());
			for (int iArt : range(articulations)) {
				ArticulationAlignment ara = cara.getArticulations()[iArt];
				ArticulationStamping araSt = new ArticulationStamping(chord,
					ara.articulationType, staffStamping, sp(leftNoteXMm + (ara.xOffsetIS + (noteheadWidth / 2)) *
						staffStamping.is, ara.yLP), 1, symbolPool);
				articulations[iArt] = araSt;
			}
		}
		
		//leger lines
		LegerLineStamping[] legerLines = legerLinesStamper.stamp(chordNotation, chordXMm, staffStamping);

		return new ChordStampings(chord, chordXMm, staffStamping, noteheads,
			dots, accidentals, legerLines, articulations, flags, stem, openStem);
	}
	
	float getLeftNoteXMm(float chordXMm, NotesAlignment notesAlignment, float staffIs) {
		//left-suspended chord? then move chord to the left by the width of a notehead
		float leftNoteXMm = chordXMm;
		if (notesAlignment.leftSuspended)
			leftNoteXMm -= notesAlignment.noteheadWidthIs * staffIs;
		return leftNoteXMm;
	}

}
