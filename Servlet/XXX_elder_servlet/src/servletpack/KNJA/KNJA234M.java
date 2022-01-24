// kanji=漢字
/*
 * $Id: 2720dee1b24d35324831709d87c03c3a81b23f9f $
 *
 * 作成日: 2013/01/18 14:48:39 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2013 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 2720dee1b24d35324831709d87c03c3a81b23f9f $
 */
public class KNJA234M {

    private static final Log log = LogFactory.getLog("KNJA234M.class");

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
            init(response, svf);

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            printMain(db2, svf);

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            closeDb(db2);
            svf.VrQuit();
        }

    }

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJA234M.frm", 4);
        final List printStdList = getPrintStdList(db2);
        final int totalcnt = printStdList.size();
        final int pageMax = (totalcnt / 100) + (totalcnt % 100 == 0 ? 0 : 1);
        int cnt = 1;
        int recordDiv = 1;
        String befKanaSentou = "";
        for (final Iterator iter = printStdList.iterator(); iter.hasNext();) {
            if (!_hasData) {
                setHead(db2, svf, pageMax);
            }
            _hasData = true;
            final Student student = (Student) iter.next();
            if (!befKanaSentou.equals(student._kanaSentou)) {
                recordDiv = 1;
                svf.VrsOut("SAKUIN", student._kanaSentou);
            } else {
                recordDiv = 2;
            }
            svf.VrsOut("KANA" + recordDiv + "_1", student._nameKana);
            svf.VrsOut("SCHREGNO" + recordDiv, student._schregNo);
            svf.VrsOut("NAME" + recordDiv + "_1", student._name);
            svf.VrsOut("COMP" + recordDiv + "_1", student._comp1);
            svf.VrsOut("COMP" + recordDiv + "_2", student._comp2);
            svf.VrsOut("COMP" + recordDiv + "_3", student._comp3);
            cnt++;
            befKanaSentou = student._kanaSentou;
            svf.VrEndRecord();
            if (cnt > 100) {
                cnt = 1;
                setHead(db2, svf, pageMax);
                befKanaSentou = "";
            }
            svf.VrEndPage();
        }
        if (cnt > 1) {
            for (int i = cnt; i <= 100; i++) {
                svf.VrsOut("DUMMY", "1");
                svf.VrEndRecord();
            }
        }
    }

    private void setHead(final DB2UDB db2, final Vrw32alp svf, final int totalPage) {
        svf.VrsOut("P2", String.valueOf(totalPage));
        svf.VrsOut("NENDO", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._ctrlYear)) + "年度");
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._ctrlDate));
        final String comp1[] = _param.getCompTitle(db2, 2);
        final String comp2[] = _param.getCompTitle(db2, 1);
        final String comp3[] = _param.getCompTitle(db2, 0);
        svf.VrsOut("COMP_NAME1_1", comp1[0]);
        svf.VrsOut("COMP_NAME2_1", comp2[0]);
        svf.VrsOut("COMP_NAME3_1", comp3[0]);
        svf.VrsOut("COMP_NAME1_2", comp1[1]);
        svf.VrsOut("COMP_NAME2_2", comp2[1]);
        svf.VrsOut("COMP_NAME3_2", comp3[1]);
    }

    /**
     * @param db2
     * @return
     */
    private List getPrintStdList(final DB2UDB db2) {
        final List retList = new ArrayList();

        PreparedStatement ps = null;
        ResultSet rs = null;
        final String getStudentSql = getStudentSql();
        try {
            ps = db2.prepareStatement(getStudentSql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregNo = rs.getString("SCHREGNO");
                final String name = rs.getString("NAME");
                final String nameKana = rs.getString("NAME_KANA");
                final String nameSort = rs.getString("SORT");
                final Student student = new Student(schregNo, name, nameKana, nameSort);
                student.setCompData(db2);
                retList.add(student);
            }
        } catch (SQLException e) {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return retList;
    }

    /**
     * @return
     */
    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     BASE.SCHREGNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     TRANSLATE_K_H(BASE.NAME_KANA) AS NAME_KANA, ");
        stb.append("     TRANSLATE_KANA(BASE.NAME_KANA) AS SORT ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT REGD ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     REGD.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND REGD.SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append(" ORDER BY ");
        stb.append("     SORT, ");
        stb.append("     TRANSLATE_K_H(BASE.NAME_KANA), ");
        stb.append("     BASE.SCHREGNO ");

        return stb.toString();
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    private class Student {
        final String _schregNo;
        final String _name;
        final String _nameKana;
        final String _kanaSentou;
        String _comp1 = "";
        String _comp2 = "";
        String _comp3 = "";
        /**
         * コンストラクタ。
         */
        Student(
                final String schregNo,
                final String name,
                final String nameKana,
                final String nameSort
        ) {
            _schregNo = schregNo;
            _name = name;
            _nameKana = nameKana;
            _kanaSentou = nameSort.substring(0, 1);
        }
        /**
         *
         */
        public void setCompData(final DB2UDB db2) throws SQLException {
            final Map yearCompMap = getComp(db2);
            final String setYear1 = String.valueOf(Integer.parseInt(_param._ctrlYear) - 2);
            if (yearCompMap.containsKey(setYear1)) {
                _comp1 = (String) yearCompMap.get(setYear1);
            }
            final String setYear2 = String.valueOf(Integer.parseInt(_param._ctrlYear) - 1);
            if (yearCompMap.containsKey(setYear2)) {
                _comp2 = (String) yearCompMap.get(setYear2);
            }
            final String setYear3 = String.valueOf(Integer.parseInt(_param._ctrlYear) - 0);
            if (yearCompMap.containsKey(setYear3)) {
                _comp3 = (String) yearCompMap.get(setYear3);
            }
        }

        private Map getComp(final DB2UDB db2) {
            final Map m = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String compData = getCompDataSql();
                ps = db2.prepareStatement(compData);
                rs = ps.executeQuery();
                while (rs.next()) {
                    String retStr = null != rs.getString("CNT") && rs.getInt("CNT") != 0 ? "1" : "0";
                    m.put(rs.getString("YEAR"), retStr);
                }
            } catch (final Exception e) {
            	log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return m;
        }
        /**
         * @param i
         * @return
         */
        private String getCompDataSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.YEAR, ");
            stb.append("     COUNT(T2.SCHREGNO) AS CNT ");
            stb.append(" FROM ");
            stb.append("     (SELECT DISTINCT SCHREGNO, YEAR FROM SCHREG_REGD_DAT) T1 ");
            stb.append("     LEFT JOIN SUBCLASS_STD_SELECT_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("         AND T2.YEAR = T1.YEAR ");
            stb.append(" WHERE ");
            stb.append("     T1.SCHREGNO = '" + _schregNo + "' ");
            stb.append(" GROUP BY ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SCHREGNO ");

            return stb.toString();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 71745 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final Map _titleMap = new HashMap();

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
        }

        /**
         * @param i
         * @return
         */
        public String[] getCompTitle(final DB2UDB db2, int i) {
            final String year = String.valueOf(Integer.parseInt(_param._ctrlYear) - i);
 
            if (!_titleMap.containsKey(year)) {
            	String retVal[] = new String[2];
            	final String gengouData = KNJ_EditDate.gengou(db2, Integer.parseInt(year));
 
            	if (gengouData.indexOf("明治") != -1) {
            		retVal[0] = "M";
            		if ("明治元".equals(gengouData)) {
            			retVal[1] = "1";
            		} else {
            			retVal[1] = gengouData.substring(2);
            		}
            	} else if (gengouData.indexOf("大正") != -1) {
            		retVal[0] = "T";
            		if ("大正元".equals(gengouData)) {
            			retVal[1] = "1";
            		} else {
            			retVal[1] = gengouData.substring(2);
            		}
            	} else if (gengouData.indexOf("昭和") != -1) {
            		retVal[0] = "S";
            		if ("昭和元".equals(gengouData)) {
            			retVal[1] = "1";
            		} else {
            			retVal[1] = gengouData.substring(2);
            		}
            	} else if (gengouData.indexOf("平成") != -1) {
            		retVal[0] = "H";
            		if ("平成元".equals(gengouData)) {
            			retVal[1] = "1";
            		} else {
            			retVal[1] = gengouData.substring(2);
            		}
            	} else if (gengouData.indexOf("令和") != -1) {
            		retVal[0] = "R";
            		if ("令和元".equals(gengouData)) {
            			retVal[1] = "1";
            		} else {
            			retVal[1] = gengouData.substring(2);
            		}
            	}
            	_titleMap.put(year,  retVal);
            }
            return (String[]) _titleMap.get(year);
        }

    }
}

// eof
