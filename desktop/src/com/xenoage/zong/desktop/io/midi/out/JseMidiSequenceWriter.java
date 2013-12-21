package com.xenoage.zong.desktop.io.midi.out;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import com.xenoage.zong.io.midi.out.MidiSequenceWriter;

/**
 * Java SE implementation of a {@link MidiSequenceWriter}.
 * 
 * @author Andreas Wenger
 */
public class JseMidiSequenceWriter
	extends MidiSequenceWriter {

	private Sequence sequence = null;
	private Track[] tracks;


	@Override public void init(int tracksCount, int resolutionPpq) {
		//create sequence
		try {
			sequence = new Sequence(Sequence.PPQ, resolutionPpq);
		} catch (InvalidMidiDataException e) {
			throw new RuntimeException(); //TODO
		}
		//add tracks
		for (int i = 0; i < tracksCount; i++) {
			tracks[i] = sequence.createTrack();
		}
	}
	
	@Override public void writeEvent(int track, int channel, long tick, int command, int data1, int data2) {
		try {
			ShortMessage message = new ShortMessage();
			message.setMessage(command, channel, data1, data2);
			tracks[track].add(new MidiEvent(message, tick));
		} catch (InvalidMidiDataException e) {
			//ignore - TODO
		}
	}

	@Override public long getLength() {
		return sequence.getTickLength();
	}
	
}
