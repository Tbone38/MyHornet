package com.treshna.hornet;

public class UpdateDatabase {
	
	public static class Ninety {
		public static final String SQL = "ALTER TABLE "+ContentDescriptor.Image.NAME+" ADD COLUMN "
				+ContentDescriptor.Image.Cols.IID+" INTEGER ;";
	}
	
}
