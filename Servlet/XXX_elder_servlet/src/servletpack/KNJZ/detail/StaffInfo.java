package servletpack.KNJZ.detail;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.DbUtils;

import nao_package.db.DB2UDB;

public class StaffInfo {
	
    protected final DB2UDB _db2;

	
	public String _staffCd;
	public String _name;
	public String _nameShow;
	public String _nameKana;
	public String _nameEng;
	/** SEQ001 FIELD1 */
	public String _color;
	/** SEQ001 FIELD2 */
	public String _fontSize;
	/** SEQ001 FIELD3 */
	public String _IME;
	/** SEQ001 FIELD4 */
	public String _ATOK;
	/** SEQ001 FIELD5 */
	public String _language;

	public StaffInfo(final DB2UDB db2, final String staffCd) {
		_staffCd = staffCd;
        _db2 = db2;
        final String staffSql = getStaffSql(staffCd);
        PreparedStatement psStaff = null;
        ResultSet rsStaff = null;
        try {
            psStaff = _db2.prepareStatement(staffSql);
            rsStaff = psStaff.executeQuery();
            if (rsStaff.next()) {
            	_name = rsStaff.getString("STAFFNAME");
            	_nameShow = rsStaff.getString("STAFFNAME_SHOW");
            	_nameKana = rsStaff.getString("STAFFNAME_KANA");
            	_nameEng = rsStaff.getString("STAFFNAME_ENG");
            	_color = rsStaff.getString("FIELD1");
            	_fontSize = rsStaff.getString("FIELD2");
            	_IME = rsStaff.getString("FIELD3");
            	_ATOK = rsStaff.getString("FIELD4");
            	_language = rsStaff.getString("FIELD5");
            }
        } catch (SQLException e) {
			e.printStackTrace();
		} finally {
            DbUtils.closeQuietly(null, psStaff, rsStaff);
            _db2.commit();
        }
    }

    private String getStaffSql(final String staffCd) {
        final StringBuffer stb = new StringBuffer();
        stb.append("  SELECT ");
        stb.append("      STAFFNAME, ");
        stb.append("      STAFFNAME_SHOW, ");
        stb.append("      STAFFNAME_KANA, ");
        stb.append("      STAFFNAME_ENG, ");
        stb.append("      STAFFD.FIELD1, ");
        stb.append("      STAFFD.FIELD2, ");
        stb.append("      STAFFD.FIELD3, ");
        stb.append("      STAFFD.FIELD4, ");
        stb.append("      STAFFD.FIELD5 ");
        stb.append("  FROM ");
        stb.append("      STAFF_MST STAFF ");
        stb.append("      LEFT JOIN ");
        stb.append("      STAFF_DETAIL_SEQ_MST AS STAFFD ");
        stb.append("      ON STAFF.STAFFCD = STAFFD.STAFFCD ");
        stb.append("      AND STAFFD.STAFF_SEQ = '001' ");
        stb.append("  WHERE ");
        stb.append("      STAFF.STAFFCD = '" + staffCd + "' ");
        return stb.toString();
    }

	public String getStrEngOrJp(final String name, final String nameEng) {
		return "1".equals(_language) ? nameEng : name;
	}
	
	
}
