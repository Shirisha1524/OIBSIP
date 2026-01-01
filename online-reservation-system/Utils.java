
package reservation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Utils {

    // Generate unique PNR
    public static String generatePNR(Connection con) throws SQLException {
        String pnr;
        boolean exists;
        do {
            pnr = "PNR" + (int)(Math.random() * 90000 + 10000);
            PreparedStatement pst = con.prepareStatement("SELECT pnr FROM reservations WHERE pnr=?");
            pst.setString(1, pnr);
            ResultSet rs = pst.executeQuery();
            exists = rs.next();
        } while (exists);
        return pnr;
    }

    // Validate non-empty text
    public static boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }
}
