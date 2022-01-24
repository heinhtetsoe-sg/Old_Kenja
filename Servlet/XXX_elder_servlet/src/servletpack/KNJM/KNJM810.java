/*
 * $Id: e03f37d0e5ee3785b9892e5ed62215564f25c61a $
 *
 * 作成日: 2012/10/18
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJM;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;

/**
 * 教科書・学習書注文書
 */
public class KNJM810 {

    private static final Log log = LogFactory.getLog(KNJM810.class);

    private boolean _hasData;

    private Param _param;

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        
        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        try {
            response.setContentType("application/pdf");

            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            printMain(db2, svf);
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }

            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }

    }
    
    private static long toLong(final String s) {
        return null == s ? 0 : Long.parseLong(s);
    }
    
    private AnnualTextbookSubclass getAnnualTextSubclass(final List list, final String annual) {
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final AnnualTextbookSubclass ats = (AnnualTextbookSubclass) it.next();
            if ((null == annual && null == ats._annual || null != annual && annual.equals(ats._annual))) {
                return ats;
            }
        }
        return null;
    }
    
    private TextbookSubclass getTextSubclass(final List list, final String subclasscd) {
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final TextbookSubclass ts = (TextbookSubclass) it.next();
            if (ts._subclasscd.equals(subclasscd)) {
                return ts;
            }
        }
        return null;
    }
    
    private List getAnnualTextbookSubclassList(final DB2UDB db2) throws Exception {
        final List annualTextbookSubclassList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getTextbookSubclassSql();
            log.info(" sql textbooksubclass = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                
                AnnualTextbookSubclass annualTs = getAnnualTextSubclass(annualTextbookSubclassList, rs.getString("ANNUAL"));
                if (null == annualTs) {
                    annualTs = new AnnualTextbookSubclass();
                    annualTs._annual = rs.getString("ANNUAL");
                    annualTextbookSubclassList.add(annualTs);
                }
                
                TextbookSubclass ts = getTextSubclass(annualTs._textbookSubclassList, rs.getString("SUBCLASSCD"));
                if (null == ts) {
                    ts = new TextbookSubclass();
                    ts._subclasscd = rs.getString("SUBCLASSCD");
                    ts._subclassname = rs.getString("SUBCLASSNAME");
                    annualTs._textbookSubclassList.add(ts);
                }

                final Textbook textbook = new Textbook();
                textbook._textbookdiv = rs.getInt("TEXTBOOKDIV");
                textbook._textbookdivNamespare1 = rs.getInt("TEXTBOOKDIV_NAMESPARE1");
                textbook._textbookmk = rs.getString("TEXTBOOKMK");
                textbook._textbookms = rs.getString("TEXTBOOKMS");
                textbook._issuecompanyabbv = rs.getString("ISSUECOMPANYABBV");
                textbook._textbookname = rs.getString("TEXTBOOKNAME");
                textbook._textbookunitprice = rs.getString("TEXTBOOKUNITPRICE");
                textbook._count = rs.getString("COUNT");
                textbook._total = rs.getString("TOTAL");
                
                if (1 == textbook._textbookdivNamespare1) {
                    ts._textbookdivNamespare1List.add(textbook);
                } else if (2 == textbook._textbookdivNamespare1) {
                    ts._textbookdivNamespare2List.add(textbook);
                }
            }

        } catch (final SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return annualTextbookSubclassList;
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws Exception {
        
        final List annualTextbookSubclassList = getAnnualTextbookSubclassList(db2);
        int[] countTotalAll = new int[2 + 1];
        int[] freeCountTotalAll = new int[2 + 1];
        long[] priceTotalAll = new long[2 + 1];

        try {
            boolean setForm = false;
            boolean printShoukei = false;
            int line = 0;
            long[] price = new long[2 + 1];

            // 年次
            for (final Iterator it = annualTextbookSubclassList.iterator(); it.hasNext();) {
                if (!setForm) {
                    svf.VrSetForm("KNJM810.frm", 1);
                    setForm = true;
                }
                if (printShoukei) {
                    svf.VrEndPage();
                    printShoukei = false;
                }
                
                final AnnualTextbookSubclass ats = (AnnualTextbookSubclass) it.next();
                int[] countTotalGrade = new int[2 + 1];
                long[] priceTotalGrade = new long[2 + 1];
                
                line = 0;
                
                // 科目
                for (final Iterator its = ats._textbookSubclassList.iterator(); its.hasNext();) {
                    svf.VrsOut("SCHOOL_NAME", _param._schoolMstSchoolName);
                    if ("1".equals(_param._useAddrField2) && getMS932ByteLength(_param._schoolMstAddr1Addr2) > 90) {
                        svf.VrsOut("ADDRESS4", _param._schoolMstAddr1Addr2);
                    } else if ("1".equals(_param._useAddrField2) && getMS932ByteLength(_param._schoolMstAddr1Addr2) > 80) {
                        svf.VrsOut("ADDRESS3", _param._schoolMstAddr1Addr2);
                    } else if ("1".equals(_param._useAddrField2) && getMS932ByteLength(_param._schoolMstAddr1Addr2) > 60) {
                        svf.VrsOut("ADDRESS2", _param._schoolMstAddr1Addr2);
                    } else {
                        svf.VrsOut("ADDRESS", _param._schoolMstAddr1Addr2);
                    }
                    svf.VrsOut("PREF", _param._nameMstZ010Name3);
                    
                    final TextbookSubclass ts = (TextbookSubclass) its.next();
                    // テキスト行
                    for (int i = 0; i < Math.max(ts._textbookdivNamespare1List.size(), ts._textbookdivNamespare2List.size()); i++) {
                        if (line == 30) {
                            svf.VrEndPage();
                            line = 0;
                        }
                        line += 1;
                        price = new long[2 + 1];

                        svf.VrsOutn("GRADE", line, ats._annual);
                        svf.VrsOutn("SUBJECT" + (getMS932ByteLength(ts._subclassname) > 16 ? "2" : "1"), line, ts._subclassname);

                        if (ts._textbookdivNamespare1List.size() > i) {
                            final Textbook textbook = (Textbook) ts._textbookdivNamespare1List.get(i);
                            svf.VrsOutn("TEST_ISSUE" + ((getMS932ByteLength(textbook._issuecompanyabbv) > 6) ? "2" : "1"), line, textbook._issuecompanyabbv);
                            svf.VrsOutn("TEXT_SIGN", line, textbook._textbookmk);
                            svf.VrsOutn("TEXT_NO", line, textbook._textbookms);
                            svf.VrsOutn("TEXT_NAME" + (getMS932ByteLength(textbook._textbookname) > 30 ? "3" : getMS932ByteLength(textbook._textbookname) > 24 ? "2" : "1"), line, textbook._textbookname);
                            
                            svf.VrsOutn("TEXT_PRICE", line, textbook._textbookunitprice);
                            svf.VrsOutn("TEXT_NUM", line, textbook._count);
                            svf.VrsOutn("TEXT_ALL", line, textbook._total);
                            countTotalGrade[textbook._textbookdivNamespare1] += toLong(textbook._count);
                            price[textbook._textbookdivNamespare1] += toLong(textbook._total);
                            priceTotalGrade[textbook._textbookdivNamespare1] += price[textbook._textbookdivNamespare1];
                        }
                        if (ts._textbookdivNamespare2List.size() > i) {
                            final Textbook textbook = (Textbook) ts._textbookdivNamespare2List.get(i);
                            svf.VrsOutn("STUDY_ISSUE" + (getMS932ByteLength(textbook._issuecompanyabbv) > 6 ? "2" : "1"), line, textbook._issuecompanyabbv);
                            svf.VrsOutn("STUDY_NAME" + (getMS932ByteLength(textbook._textbookname) > 30 ? "3" : getMS932ByteLength(textbook._textbookname) > 24 ? "2" : "1"), line, textbook._textbookname);
                            
                            svf.VrsOutn("STUDY_PRICE", line, textbook._textbookunitprice);
                            svf.VrsOutn("STUDY_NUM", line, textbook._count);
                            svf.VrsOutn("STUDY_ALL", line, textbook._total);
                            countTotalGrade[textbook._textbookdivNamespare1] += toLong(textbook._count);
                            price[textbook._textbookdivNamespare1] += toLong(textbook._total);
                            priceTotalGrade[textbook._textbookdivNamespare1] += price[textbook._textbookdivNamespare1];
                        }
                        _hasData = true;
                        svf.VrsOutn("BOOK_ALL", line, String.valueOf(price[1] + price[2]));
                    }
                }
                
                // 小計
                final int [] freeCountTotalGrade = getFreeCount(db2, ats._annual);
                svf.VrsOutn("SUM_TEXT_NUM", 1, String.valueOf(countTotalGrade[1]));
                svf.VrsOutn("SUM1", 1, String.valueOf(freeCountTotalGrade[1]));
                svf.VrsOutn("SUM_TEXT_ALL", 1, String.valueOf(priceTotalGrade[1]));
                svf.VrsOutn("SUM_STUDY_NUM", 1, String.valueOf(countTotalGrade[2]));
                svf.VrsOutn("SUM2", 1, String.valueOf(freeCountTotalGrade[2]));
                svf.VrsOutn("SUM_STUDY_ALL", 1, String.valueOf(priceTotalGrade[2]));
                svf.VrsOutn("SUM_BOOK_ALL", 1, String.valueOf(priceTotalGrade[1] + priceTotalGrade[2]));
                for (int i = 1; i <= 2; i++) {
                    countTotalAll[i] += countTotalGrade[i];
                    priceTotalAll[i] += priceTotalGrade[i];
                    freeCountTotalAll[i] += freeCountTotalGrade[i];
                }
                printShoukei = true;
            }
            
        } catch (final Exception ex) {
            log.error("Exception:", ex);
        }

        // 計
        svf.VrsOutn("SUM_TEXT_NUM", 2, String.valueOf(countTotalAll[1]));
        svf.VrsOutn("SUM1", 2, String.valueOf(freeCountTotalAll[1]));
        svf.VrsOutn("SUM_TEXT_ALL", 2, String.valueOf(priceTotalAll[1]));
        svf.VrsOutn("SUM_STUDY_NUM", 2, String.valueOf(countTotalAll[2]));
        svf.VrsOutn("SUM2", 2, String.valueOf(freeCountTotalAll[2]));
        svf.VrsOutn("SUM_STUDY_ALL", 2, String.valueOf(priceTotalAll[2]));
        svf.VrsOutn("SUM_BOOK_ALL", 2, String.valueOf(priceTotalAll[1] + priceTotalAll[2]));
        svf.VrEndPage();
    }
    
    private static int getMS932ByteLength(final String s) {
        if (null != s) {
            try {
                return s.getBytes("MS932").length;
            } catch (final Exception e) {
                log.error("exception!", e);
            }
        }
        return 0;
    }

    public String getTextbookSubclassSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCHREG_TEXTBOOK_SUBCLASS AS ( ");
        stb.append(" SELECT DISTINCT ");
        stb.append("     T3.ANNUAL, ");
        stb.append("     T3.SCHREGNO, ");
        stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("     T4.SUBCLASSNAME, ");
        stb.append("     T5.ISSUECOMPANYABBV, ");
        stb.append("     T2.TEXTBOOKCD, ");
        stb.append("     T2.TEXTBOOKDIV, ");
        stb.append("     NMM004.NAMESPARE1 AS TEXTBOOKDIV_NAMESPARE1, ");
        stb.append("     T2.TEXTBOOKMK, ");
        stb.append("     T2.TEXTBOOKMS, ");
        stb.append("     T2.TEXTBOOKNAME, ");
        stb.append("     T2.TEXTBOOKUNITPRICE ");
        stb.append(" FROM  ");
        stb.append("     SCHREG_TEXTBOOK_SUBCLASS_DAT T1 ");
        stb.append("     INNER JOIN TEXTBOOK_MST T2 ON T2.TEXTBOOKCD = T1.TEXTBOOKCD ");
        stb.append("     INNER JOIN NAME_MST NMM004 ON NMM004.NAMECD1 = 'M004' ");
        stb.append("         AND NMM004.NAMECD2 = T2.TEXTBOOKDIV ");
        stb.append("         AND NMM004.NAMESPARE1 IS NOT NULL ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND T3.YEAR = T1.YEAR ");
        stb.append("     LEFT JOIN SUBCLASS_MST T4 ON T4.CLASSCD = T1.CLASSCD ");
        stb.append("         AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("         AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND T4.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("     LEFT JOIN ISSUECOMPANY_MST T5 ON T5.ISSUECOMPANYCD = T2.ISSUECOMPANYCD ");
        stb.append(" WHERE  ");
        stb.append("      T1.YEAR = '" + _param._year + "' ");
        stb.append(" ), KYUYO AS ( ");
        stb.append("     SELECT ANNUAL, SUBCLASSCD, TEXTBOOKCD, COUNT(*) AS COUNT ");
        stb.append("     FROM SCHREG_TEXTBOOK_SUBCLASS ");
        stb.append("     GROUP BY ANNUAL, SUBCLASSCD, TEXTBOOKCD ");
        stb.append(" ) ");
        stb.append(" SELECT DISTINCT ");
        stb.append("     T1.ANNUAL, ");
        stb.append("     VALUE(T1.ANNUAL, '00'), ");
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     T1.SUBCLASSNAME, ");
        stb.append("     T1.ISSUECOMPANYABBV, ");
        stb.append("     T1.TEXTBOOKDIV, ");
        stb.append("     T1.TEXTBOOKDIV_NAMESPARE1, ");
        stb.append("     T1.TEXTBOOKCD, ");
        stb.append("     T1.TEXTBOOKMK, ");
        stb.append("     T1.TEXTBOOKMS, ");
        stb.append("     T1.TEXTBOOKNAME, ");
        stb.append("     T1.TEXTBOOKUNITPRICE, ");
        stb.append("     T2.COUNT, ");
        stb.append("     T1.TEXTBOOKUNITPRICE * T2.COUNT AS TOTAL ");
        stb.append(" FROM SCHREG_TEXTBOOK_SUBCLASS T1 ");
        stb.append(" LEFT JOIN KYUYO T2 ON T2.TEXTBOOKCD = T1.TEXTBOOKCD AND T2.ANNUAL = T1.ANNUAL AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("     (T1.TEXTBOOKDIV_NAMESPARE1 = '1' OR T1.TEXTBOOKDIV_NAMESPARE1 = '2') ");
        stb.append(" ORDER BY  ");
        stb.append("    VALUE(T1.ANNUAL, '00'), T1.SUBCLASSCD, T1.TEXTBOOKDIV_NAMESPARE1, T1.TEXTBOOKCD ");
        return stb.toString();
    }
    
    
    private int[] getFreeCount(final DB2UDB db2, final String annual) throws Exception {
        final int[] freeCount = new int[2 + 1];
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getFreeCountSql(annual);
            log.debug(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                if ("1".equals(rs.getString("TEXTBOOKDIV_NAMESPARE1"))) {
                    freeCount[1] = rs.getInt("COUNT");
                } else if ("2".equals(rs.getString("TEXTBOOKDIV_NAMESPARE1"))) {
                    freeCount[2] = rs.getInt("COUNT");
                }
            }

        } catch (final SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return freeCount;
    }
    
    public String getFreeCountSql(final String annual) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH T_DIVS AS ( ");
        stb.append(" SELECT DISTINCT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("     T2.TEXTBOOKDIV, ");
        stb.append("     NMM004.NAMESPARE1 AS TEXTBOOKDIV_NAMESPARE1, ");
        stb.append("     T1.TEXTBOOKCD ");
        stb.append(" FROM ");
        stb.append("     SCHREG_TEXTBOOK_FREE_DAT T1 ");
        stb.append("     INNER JOIN TEXTBOOK_MST T2 ON T2.TEXTBOOKCD = T1.TEXTBOOKCD ");
        stb.append("     INNER JOIN NAME_MST NMM004 ON NMM004.NAMECD1 = 'M004' ");
        stb.append("         AND NMM004.NAMECD2 = T2.TEXTBOOKDIV ");
        stb.append("         AND NMM004.NAMESPARE1 IS NOT NULL ");
        if (null == annual) {
            stb.append("     LEFT JOIN SCHREG_REGD_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO ");
            stb.append("         AND T3.YEAR = T1.YEAR ");
        } else {
            stb.append("     INNER JOIN SCHREG_REGD_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO ");
            stb.append("         AND T3.YEAR = T1.YEAR ");
            stb.append("         AND T3.ANNUAL = '" + annual + "' ");
        }
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.FREE_FLG = '1' ");
        if (null == annual) {
            stb.append("         AND T3.ANNUAL IS NULL ");
        }
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     TEXTBOOKDIV_NAMESPARE1, ");
        stb.append("     COUNT(*) AS COUNT ");
        stb.append(" FROM ");
        stb.append("     T_DIVS ");
        stb.append(" GROUP BY ");
        stb.append("     TEXTBOOKDIV_NAMESPARE1 ");
        return stb.toString();
    }

    private static class AnnualTextbookSubclass {
        String _annual;
        final List _textbookSubclassList = new ArrayList();
    }

    private static class TextbookSubclass {
        String _subclasscd;
        String _subclassname;
        final List _textbookdivNamespare1List = new ArrayList();
        final List _textbookdivNamespare2List = new ArrayList();
    }
    
    private static class Textbook {
        String _issuecompanyabbv;
        int _textbookdiv;
        int _textbookdivNamespare1;
        String _textbookcd;
        String _textbookmk;
        String _textbookms;
        String _textbookname;
        String _textbookunitprice;
        String _count;
        String _total;
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _useAddrField2;

        private String _schoolMstSchoolName;
        private String _schoolMstAddr1Addr2;
        private String _nameMstZ010Name3;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("YEAR");
            _useAddrField2 = request.getParameter("useAddrField2");
            setSchoolMst(db2);
            setNameMstZ010(db2);
        }
        
        private void setSchoolMst(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT SCHOOLNAME1, VALUE(SCHOOLADDR1, '') || VALUE(SCHOOLADDR2, '') AS SCHOOLADDR FROM SCHOOL_MST WHERE YEAR = '" + _year + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _schoolMstSchoolName = rs.getString("SCHOOLNAME1");
                    _schoolMstAddr1Addr2 = rs.getString("SCHOOLADDR");
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        
        
        private void setNameMstZ010(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT NAME3 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'Z010' AND NAMECD2 = '00' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _nameMstZ010Name3 = rs.getString("NAME3");
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
    }
}

// eof

