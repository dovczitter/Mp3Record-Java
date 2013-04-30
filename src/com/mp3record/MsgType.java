package com.mp3record;

public enum MsgType {
	 None(-1),
	 RecStarted(0),
	 RecStopped(1),
	 ErrorGetMinBuffersize(2),
	 ErrorCreateFile(3),
	 ErrorRecStart(4),
	 ErrorAudioRecord(5),
	 ErrorAudioEncode(6),
	 ErrorWriteFile(7),
	 ErrorCloseFile(8);
	 
	 private int value;
	 
	 private MsgType (int value) {
		 this.value = value;
	 }
	 public int getValue()
	 {
		 return this.value;
	 }
	 public static MsgType getType(int value)
	 {
		 for (MsgType t : values()) {
			 if (t.getValue() == value)
				 return t;
		 }
		 return None;
	 }
}
