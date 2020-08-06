package br.com.finalcraft.forgerestrictor;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;

public class Utils {
	public static UUID toUUID(byte[] bytes) {
	    if (bytes.length != 16) {
	        throw new IllegalArgumentException();
	    }
	    int i = 0;
	    long msl = 0;
	    for (; i < 8; i++) {
	        msl = (msl << 8) | (bytes[i] & 0xFF);
	    }
	    long lsl = 0;
	    for (; i < 16; i++) {
	        lsl = (lsl << 8) | (bytes[i] & 0xFF);
	    }
	    return new UUID(msl, lsl);
	}
	
	public static String UUIDtoHexString(UUID uuid) {
		if (uuid==null) return "0x0";
		return "0x"+org.apache.commons.lang.StringUtils.leftPad(Long.toHexString(uuid.getMostSignificantBits()), 16, "0")+org.apache.commons.lang.StringUtils.leftPad(Long.toHexString(uuid.getLeastSignificantBits()), 16, "0");
	}
	
	public static boolean isFakePlayer(Player player) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			if(player==p) {
				return false;
			}
		}
		return true;
	}
	
	public static String sanitizeSql(String string) {
		return string.replace("\\", "\\\\").replace("\"", "\\\"");
	}
	
	public static String stripSqlString(String string) {
		return string.replace("\\", "").replace("\"", "");
	}
	
	public static String locationToString(Location location) {
		return "[" + location.getWorld().getName() + ", " + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() + "]";
	}
	
	public static String mergeStringArrayFromIndex(String[] arrayString, int i) {
		StringBuilder sb = new StringBuilder();
		
		for(;i<arrayString.length;i++){
			sb.append(arrayString[i]);
			sb.append(' ');
		}
		
		if (sb.length()!=0) {
			sb.deleteCharAt(sb.length()-1);
		}
		return sb.toString();
	}
	
	public static short getUtcYear() {
		return (short) Calendar.getInstance(TimeZone.getTimeZone("GMT")).get(Calendar.YEAR);
	}
	
	public static int epoch() {
		return (int) (System.currentTimeMillis()/1000);
	}
}
