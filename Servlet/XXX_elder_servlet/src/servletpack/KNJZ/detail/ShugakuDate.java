package servletpack.KNJZ.detail;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;

public class ShugakuDate {

    private static final Log log = LogFactory.getLog(ShugakuDate.class);
    
    private final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    public final List _nameMstT001;
    public boolean _useLog = false;
    public int _nenDefault = 1989;
    public boolean _printBlank = true;
    
    public ShugakuDate(final DB2UDB db2) {
        _nameMstT001 = getNameMstT001(db2);
    }
    
    public String getChijiName(DB2UDB db2) {
    	return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT CHIJI_NAME FROM CHIJI_MST WHERE S_DATE = (SELECT MAX(S_DATE) FROM CHIJI_MST ) "));
    }
    
    public String getChijiName2(DB2UDB db2) {
        String name = null;
        final String sql = " SELECT VALUE(CHIJI_YAKUSHOKU_NAME, '')  AS CHIJI_YAKUSHOKU_NAME, VALUE(CHIJI_NAME, '') AS CHIJI_NAME FROM CHIJI_MST WHERE S_DATE = (SELECT MAX(S_DATE) FROM CHIJI_MST ) ";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                final String chijiYakushokuName = rs.getString("CHIJI_YAKUSHOKU_NAME");
                final String chijiName = rs.getString("CHIJI_NAME");
                name =  chijiYakushokuName + "　 " + ltrim(chijiName);
            }
        } catch (SQLException e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return name;
    }
    
    public String getChijiName3(DB2UDB db2) {
        String name = null;
        final String sql = " SELECT VALUE(CHIJI_YAKUSHOKU_NAME, '') || '　　' || VALUE(CHIJI_NAME, '') AS CHIJI_NAME FROM CHIJI_MST WHERE S_DATE = (SELECT MAX(S_DATE) FROM CHIJI_MST) ";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                name = rs.getString("CHIJI_NAME");
            }
        } catch (SQLException e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return name;
    }

    private String ltrim(final String s) {
        if (null != s) {
            for (int i = 0; i < s.length(); i++) {
                if (s.charAt(i) != ' ' && s.charAt(i) != '　') {
                    return s.substring(i);
                }
            }
        }
        return s;
    }

    private List getNameMstT001(final DB2UDB db2) {
    	return KnjDbUtils.query(db2, " SELECT * FROM NAME_MST WHERE NAMECD1 = 'T001' ORDER BY NAMECD2 DESC ");
    }

    public String d5toYmStr(final String d5) {
    	if (null == d5 || d5.length() < 5) {
    		return null;
    	}
        final DecimalFormat decf = new DecimalFormat("00");
        final String nengoFlg = d5.substring(0, 1);
        final int nengoNen = Integer.parseInt(d5.substring(1, 3));
        final String tuki = decf.format(Integer.parseInt(d5.substring(3, 5)));
        
        int nen = _nenDefault; // default
        try {
            for (final Iterator it = _nameMstT001.iterator(); it.hasNext();) {
                final Map m = (Map) it.next();
                final String namecd2 = (String) m.get("NAMECD2");
                if (namecd2.equals(nengoFlg)) {
                    final String abbv1 = (String) m.get("ABBV1");
                    if (null != abbv1) {
                        final Calendar dcal = Calendar.getInstance();
                        dcal.setTime(df.parse(abbv1.replace('/', '-')));
                        nen = dcal.get(Calendar.YEAR);
                    }
                    break;
                }
            }
            return (nen + nengoNen - 1) + "-" + tuki;
        } catch (Exception e) {
            log.error("format exception! date = " + d5, e);
        }
        return null;
    }
    
    public String d7toDateStr(final String d7) {
    	if (null == d7 || d7.length() < 7) {
    		return null;
    	}
        final DecimalFormat decf = new DecimalFormat("00");
        final String nengoFlg = d7.substring(0, 1);
        final int nengoNen = Integer.parseInt(d7.substring(1, 3));
        final String tuki = decf.format(Integer.parseInt(d7.substring(3, 5)));
        final String hi = decf.format(Integer.parseInt(d7.substring(5, 7)));
        
        int nen = _nenDefault; // default
        try {
            for (final Iterator it = _nameMstT001.iterator(); it.hasNext();) {
                final Map m = (Map) it.next();
                final String namecd2 = (String) m.get("NAMECD2");
                if (namecd2.equals(nengoFlg)) {
                    final String abbv1 = (String) m.get("ABBV1");
                    if (null != abbv1) {
                        final Calendar dcal = Calendar.getInstance();
                        dcal.setTime(df.parse(abbv1.replace('/', '-')));
                        nen = dcal.get(Calendar.YEAR);
                    }
                    break;
                }
            }
            return (nen + nengoNen - 1) + "-" + tuki + "-" + hi;
        } catch (Exception e) {
            log.error("format exception! date = " + d7, e);
        }
        return null;
    }
    
    public String getUkeYearNum(final String ukeYear) {
    	if (!NumberUtils.isDigits(ukeYear)) {
    		return "";
    	}
        final int[] ints = getNenTukiHiInt(ukeYear + "-12-31");
        final String wa = null == ints ? "" : String.valueOf(ints[0] % 10);
        return wa;
    }

    public String gengou(final String year) {
        if (!NumberUtils.isDigits(year)) {
            return "　　　";
        }
        final String[] rtn = nengoNenTukiHi(year + "-12-31", false);
        if (null == rtn) {
            return "　　　";
        }
        return rtn[0] + rtn[1];
    }

    public String nengo(final String date) {
        if (null != date) {
            final String[] nengoNenTukiHi = nengoNenTukiHi(date);
            return nengoNenTukiHi[0];
        }
        return "　　";
    }

    public String getNen(final String year) {
    	if (!NumberUtils.isDigits(year)) {
    		return "";
    	}
        final int[] ints = getNenTukiHiInt(year + "-12-31");
        final String wa = null == ints ? "" : String.valueOf(ints[0] % 10);
        return wa;
    }

    public String formatNen(final String year) {
        final String[] rtn = nengoNenTuki(year + "-04", true);
        if (null == rtn) {
            return "　　　　"; // 全角8文字
        }
        return rtn[0] + rtn[1] + "年";
    }

    public String[] nengoNenTuki(final String nentuki, final boolean withSpace) {
        if (null != nentuki) {
            final String[] nengoNenTukiHi = nengoNenTukiHi(nentuki + "-01", withSpace);
            if (null == nengoNenTukiHi) {
                return null;
            }
            final String[] rtn = {nengoNenTukiHi[0], nengoNenTukiHi[1], nengoNenTukiHi[2]};
            if (_useLog) {
            	log.info(" nentuki = " + nentuki + ", rtn = " + ArrayUtils.toString(rtn));
            }
			return rtn;
        }
        return null;
    }
    
    public String formatNentuki(final String nentuki, final boolean withSpace) {
        final String[] rtn = nengoNenTuki(nentuki, withSpace);
        if (null == rtn) {
            return "　　　　　　"; // 全角8文字
        }
        return rtn[0] + rtn[1] + "年" + rtn[2] + "月";
    }
    
    public String formatNentuki(final String nentuki) {
    	return formatNentuki(nentuki, false);
    }
    
    public String nengoNenFromDate(final String date, final boolean isKeta2) {
        final String[] rtn = formatDateToNen(date, isKeta2);
        if (null == rtn) {
            return "　　　　　　"; // 全角8文字
        }
        return rtn[0] + "" + rtn[1] + "年";
    }
    
    public String[] formatDateToNen(final String date, final boolean isKeta2) {
        if (null != date) {
            final String[] nengoNenTukiHi = nengoNenTukiHi(date, isKeta2);
            if (null == nengoNenTukiHi) {
                return null;
            }
            String[] rtn = {nengoNenTukiHi[0], nengoNenTukiHi[1]};
            if (_useLog) {
            	log.info(" date = " + date + ", rtn = " + rtn);
            }
			return rtn;
        }
        if (_printBlank) {
        	return new String[] {"　　", "  "};
        }
        return null;
    }
    
    public String nengoNenTukiFromDate(final String date) {
        final String[] rtn = formatDateToNentuki(date);
        if (null == rtn) {
            return "　　　　　　"; // 全角8文字
        }
        return rtn[0] + "" + rtn[1] + "年" + rtn[2] + "月";
    }
    
    public String[] formatDateToNentuki(final String date) {
    	return formatDateToNentuki(date, true);
    }
    
    public String[] formatDateToNentuki(final String date, final boolean isKeta2) {
        if (null != date) {
            final String[] nengoNenTukiHi = nengoNenTukiHi(date, isKeta2);
            if (null == nengoNenTukiHi) {
                return null;
            }
            String[] rtn = {nengoNenTukiHi[0], nengoNenTukiHi[1], nengoNenTukiHi[2]};
            if (_useLog) {
            	log.info(" date = " + date + ", rtn = " + rtn);
            }
			return rtn;
        }
        if (_printBlank) {
        	return new String[] {"　　", "  ", "  "};
        }
        return null;
    }
    
    public String[] nengoNendo(String date, final boolean isKeta2) {
        if (null != date) {
        	try {
                final Date d = java.sql.Date.valueOf(date);
                final Calendar cal = Calendar.getInstance();
                cal.setTime(d);
                final int tuki = cal.get(Calendar.MONTH) + 1;
                if (1 <= tuki && tuki <= 3) {
                	date = String.valueOf(cal.get(Calendar.YEAR) - 1) + "-12-31";
                }
                final String[] nengoNenTukiHi = nengoNenTukiHi(date, isKeta2);
        		return new String[] {nengoNenTukiHi[0], nengoNenTukiHi[1]};
        	} catch (Exception e) {
        		log.error("format exception! date = " + date, e);
        	}
        }
        if (_printBlank) {
        	return new String[] {"　　", "  "};
        }
        return null;
    }
    
    public String[] nengoNenTukiHi(final String date) {
    	return nengoNenTukiHi(date, true);
    }
    
    public String[] nengoNenTukiHi(final String date, final boolean isKeta2) {
        if (null != date) {
            try {
                final Date d = java.sql.Date.valueOf(date);
                final Calendar cal = Calendar.getInstance();
                cal.setTime(d);
                int nen = -1;
                final int tuki = cal.get(Calendar.MONTH) + 1;
                final int hi = cal.get(Calendar.DAY_OF_MONTH);
                
                String nengo = "";
                for (final Iterator it = _nameMstT001.iterator(); it.hasNext();) {
                    final Map m = (Map) it.next();
                    final String abbv1 = (String) m.get("ABBV1");
                    if (null != abbv1) {
                        final Calendar dcal = Calendar.getInstance();
                        dcal.setTime(df.parse(abbv1.replace('/', '-')));
                        if (dcal.before(cal) || dcal.equals(cal)) {
                            nengo = StringUtils.defaultString((String) m.get("NAME1"), "　　");
                            nen = cal.get(Calendar.YEAR) - dcal.get(Calendar.YEAR) + 1;
                            break;
                        }
                    }
                }
                final int keta = isKeta2 ? 2 : 1;
                String[] rtn = {nengo, (nen == -1) ? "  " : (nen == 1) ? "元" : keta(nen, keta), keta(tuki, keta), keta(hi, keta)};
                if (_useLog) {
                	log.info(" date = " + date + ", rtn = " + ArrayUtils.toString(rtn));
                }
				return rtn;
            } catch (Exception e) {
                log.error("format exception! date = " + date, e);
            }
        }
        if (_printBlank) {
        	return new String[] {"　　", "  ", "  ", " "};
        }
        return null;
    }
    
    /**
     * 年号の年度、月、日を配列で返す<br />
     * ※前提条件 年号が名称マスタ「T001」に登録されている
     * @param date 日付
     * @return 年号の年度、月、日<br />例：2016-01-01 (平成28年) -> {28, 1, 1}
     */
    public int[] getNenTukiHiInt(final String date) {
        if (null != date) {
            try {
                final Date d = java.sql.Date.valueOf(date);
                final Calendar cal = Calendar.getInstance();
                cal.setTime(d);
                int nen = -1;
                final int tuki = cal.get(Calendar.MONTH) + 1;
                final int hi = cal.get(Calendar.DAY_OF_MONTH);
                
                for (final Iterator it = _nameMstT001.iterator(); it.hasNext();) {
                    final Map m = (Map) it.next();
                    final String abbv1 = (String) m.get("ABBV1");
                    if (null != abbv1) {
                        final Calendar dcal = Calendar.getInstance();
                        dcal.setTime(df.parse(abbv1.replace('/', '-')));
                        if (dcal.before(cal)) {
                            nen = cal.get(Calendar.YEAR) - dcal.get(Calendar.YEAR) + 1;
                            break;
                        }
                    }
                }
                return new int[] {nen, tuki, hi};
            } catch (Exception e) {
                log.error("format exception! date = " + date, e);
            }
        }
        return null;
    }
    
    public String formatDateMarkDot(final String date) {
        if (null != date) {
            try {
                final Date d = java.sql.Date.valueOf(date);
                final Calendar cal = Calendar.getInstance();
                cal.setTime(d);
                int nen = -1;
                final int tuki = cal.get(Calendar.MONTH) + 1;
                final int hi = cal.get(Calendar.DAY_OF_MONTH);
                
                String mark = " ";
                for (final Iterator it = _nameMstT001.iterator(); it.hasNext();) {
                    final Map m = (Map) it.next();
                    final String abbv1 = (String) m.get("ABBV1");
                    if (null != abbv1) {
                        final Calendar dcal = Calendar.getInstance();
                        dcal.setTime(df.parse(abbv1.replace('/', '-')));
                        if (dcal.before(cal) || dcal.equals(cal)) {
                            mark = StringUtils.defaultString((String) m.get("NAME3"), " ");
                            nen = cal.get(Calendar.YEAR) - dcal.get(Calendar.YEAR) + 1;
                            break;
                        }
                    }
                }
                String rtn = mark + "." + keta(nen, 2) + "." + keta(tuki, 2) + "." + keta(hi, 2);
                if (_useLog) {
                	log.info(" date = " + date + ", rtn = " + rtn);
                }
				return rtn;
            } catch (Exception e) {
                log.error("format exception! date = " + date, e);
            }
        }
        return null;
    }
    
    public String formatDate(final String date, final boolean isKeta2) {
        final String[] rtn = nengoNenTukiHi(date, isKeta2);
        if (null == rtn) {
            return "　　　　　　"; // 全角8文字
        }
        return rtn[0] + rtn[1] + "年" + rtn[2] + "月" + rtn[3] + "日";
    }
    
    public String formatDate(final String date) {
    	return formatDate(date, true);
    }
    
    public String keta(final int n, final int keta) {
        return StringUtils.repeat(" ", keta - String.valueOf(n).length()) + String.valueOf(n);
    }
}
