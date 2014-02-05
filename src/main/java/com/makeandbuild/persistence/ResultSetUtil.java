package com.makeandbuild.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;

public final class ResultSetUtil {

	public static Long getLongOrNull(ResultSet rs, String column) throws SQLException{
	    long lVal = rs.getLong(column);	    
	    if (rs.wasNull()) {
	        return null;
	    }	    
	    return new Long(lVal);
	}
	
    public static Integer getIntOrNull(ResultSet rs, String column) throws SQLException{
        int iVal = rs.getInt(column);     
        if (rs.wasNull()) {
            return null;
        }       
        return new Integer(iVal);
    }

    public static Short getShortOrNull(ResultSet rs, String column) throws SQLException{
        short sVal = rs.getShort(column);     
        if (rs.wasNull()) {
            return null;
        }       
        return new Short(sVal);
    }
    
    public static Boolean getBooleanOrNull(ResultSet rs, String column) throws SQLException{
        boolean bVal = rs.getBoolean(column);
        //hack alert => getBoolean doesn't alway set the wasNull properly http://bugs.mysql.com/bug.php?id=17450
        if (!bVal && rs.wasNull()) {
            return null;
        }       
        return new Boolean(bVal);
    }

    public static Double getDoubleOrNull(ResultSet rs, String columnName) throws SQLException {
        double sVal = rs.getDouble(columnName);     
        if (rs.wasNull()) {
            return null;
        }       
        return new Double(sVal);
    }
    
}
