//kanji=漢字
/*
 *
 * 作成日: 2010/02/22 22:22:22 - JST
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJC;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 *
 *   ＜ＫＮＪＣ１６６Ｃ 皆勤・精勤者一覧出力＞
 */

public class KNJC166C {

    private static final Log log = LogFactory.getLog(KNJC166C.class);

    private static final int MAXROW = 30;
    private static final int MAXCOL = 8;
    private boolean _hasData = false;

    Param _param;

    /**
     * @param request
     * @param response
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) {

        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        try {
            response.setContentType("application/pdf");

            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            printMain(db2, svf);

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }

    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJC166C.frm", 1);
        Map schregMap = getSchregMap(db2);

          int clsCnt = 0;
           for (Iterator itr = schregMap.keySet().iterator();itr.hasNext();) {
               String hrClass = (String)itr.next();
               List getList = (List)schregMap.get(hrClass);
               clsCnt++;
            if (clsCnt > MAXCOL) {
                svf.VrEndPage();
                clsCnt = 1;
            }
               int studentCnt = 0;
               for (Iterator its = getList.iterator();its.hasNext();) {
                   PrintData obj = (PrintData)its.next();
                   studentCnt++;
                   if (studentCnt > MAXROW) {
                       continue;
                   }
                   if (studentCnt == 1 && clsCnt == 1) {
                       setTitle(db2, svf, obj);
                   }
                   if (studentCnt == 1) {
                       //組名称を出力
                       svf.VrsOut("HR_NAME" + clsCnt, obj._hr_Name);
                   }
                   svf.VrsOutn("NO" + clsCnt, studentCnt, obj._attendNo);
                   svf.VrsOutn("KANA" + clsCnt, studentCnt, obj._name_Kana);
                   svf.VrsOutn("NAME" + clsCnt, studentCnt, obj._name);
               }
               svf.VrsOut("TOTAL" + clsCnt, String.valueOf(studentCnt));
               _hasData = true;
           }
           svf.VrEndPage();
    }

    private void setTitle(final DB2UDB db2, final Vrw32alp svf, final PrintData obj) {
        final String nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "度";
        svf.VrsOut("TITLE", nendo + " 皆勤者・精勤者一覧表" + "(" + _param._kaikinName + ")");
        svf.VrsOut("GRADE", obj._grade_Name1);
        Calendar cl = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd(E) HH:mm");
        svf.VrsOut("FOOTER", sdf.format(cl.getTime()) + " " + _param._staffName);
    }

    private Map getSchregMap(final DB2UDB db2) {
        Map retMap = new LinkedMap();
        List addList = new ArrayList();

        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = getKaikinSchregSql();
        log.debug(" sql =" + sql);

        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String grade = rs.getString("GRADE");
                final String grade_Name1 = rs.getString("GRADE_NAME1");
                final String hr_Class = rs.getString("HR_CLASS");
                final String hr_Name = rs.getString("HR_NAME");
                final String attendNo = rs.getString("ATTENDNO");
                final String schregno = rs.getString("SCHREGNO");
                final String name = rs.getString("NAME");
                final String name_Kana = rs.getString("NAME_KANA");
                PrintData addwk = new PrintData(grade, grade_Name1, hr_Class, hr_Name, attendNo, schregno, name, name_Kana);
                if (!retMap.containsKey(hr_Class)) {
                    addList = new ArrayList();
                    retMap.put(hr_Class, addList);
                } else {
                    addList = (List)retMap.get(hr_Class);
                }
                addList.add(addwk);

            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }

    /**
     * @return
     */
    private String getKaikinSchregSql() {

        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("   T2.GRADE, ");
        stb.append("   T3.GRADE_NAME1, ");
        stb.append("   T2.HR_CLASS, ");
        stb.append("   T4.HR_NAME, ");
        stb.append("   T2.ATTENDNO, ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T5.NAME, ");
        stb.append("   T5.NAME_KANA ");
        stb.append(" FROM ");
        stb.append("   KAIKIN_DAT T1 ");
        stb.append("   LEFT JOIN SCHREG_REGD_DAT T2 ");
        stb.append("     ON T2.YEAR = T1.YEAR ");
        stb.append("    AND T2.SEMESTER = '" + _param._ctrlSemester + "'");
        stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("   LEFT JOIN SCHREG_REGD_GDAT T3 ");
        stb.append("     ON T3.YEAR = T2.YEAR ");
        stb.append("    AND T3.GRADE = T2.GRADE ");
        stb.append("   LEFT JOIN SCHREG_REGD_HDAT T4 ");
        stb.append("     ON T4.YEAR = T2.YEAR ");
        stb.append("    AND T4.SEMESTER = T2.SEMESTER ");
        stb.append("    AND T4.GRADE = T2.GRADE ");
        stb.append("    AND T4.HR_CLASS = T2.HR_CLASS ");
        stb.append("   LEFT JOIN SCHREG_BASE_MST T5 ");
        stb.append("     ON T5.SCHREGNO = T1.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T2.GRADE = '" + _param._grade + "' ");
        stb.append("     AND T1.KAIKIN_CD = '" + _param._kaikinCd + "' ");
        stb.append("     AND KAIKIN_FLG = '1' ");
        stb.append("     AND VALUE(INVALID_FLG, '0') = '0' ");
        stb.append(" ORDER BY ");
        stb.append("   T2.GRADE, ");
        stb.append("   T2.HR_CLASS, ");
        stb.append("   T2.ATTENDNO ");

        return stb.toString();
    }

    private class PrintData {
        final String _grade;
        final String _grade_Name1;
        final String _hr_Class;
        final String _hr_Name;
        final String _attendNo;
        final String _schregno;
        final String _name;
        final String _name_Kana;
        public PrintData (final String grade, final String grade_Name1, final String hr_Class, final String hr_Name, final String attendNo, final String schregno, final String name, final String name_Kana)
        {
            _grade = grade;
            _grade_Name1 = grade_Name1;
            _hr_Class = hr_Class;
            _hr_Name = hr_Name;
            _attendNo = attendNo;
            _schregno = schregno;
            _name = name;
            _name_Kana = name_Kana;
        }

    }

    /** パラメータ取得処?*/
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _grade;
        private final String _ctrlSemester;
        private final String _kaikinCd;
        private final String _kaikinName;
        private final String _ctrlDate;
        private final String _staffCd;
        private final String _staffName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _grade = request.getParameter("GRADE");
            _kaikinCd = request.getParameter("KAIKIN_CD");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _staffCd = request.getParameter("STAFFCD");
            _staffName = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT STAFFNAME FROM STAFF_MST WHERE STAFFCD = '" + _staffCd + "'"));
            if (!"".equals(_kaikinCd)) {
                _kaikinName = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT KAIKIN_NAME FROM KAIKIN_MST WHERE KAIKIN_CD = '" + _kaikinCd + "'"));
            } else {
                _kaikinName = "";
            }
        }
    }
}

// eof

