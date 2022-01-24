/*
 * $Id: 202a1c5be8340a45d2252114d49f6ddad92a9d36 $
 *
 * 作成日: 2011/09/22
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJS;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;

/**
 *  学校教育システム 賢者 [小学校プログラム] 出席簿
 *
 */
public class KNJS341 {

    private static final Log log = LogFactory.getLog(KNJS341.class);

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        
        for (int gi = 0; gi < _param._gradeMapList.size(); gi++) {
            final Map gradeMap = (Map) _param._gradeMapList.get(gi);
            final String grade = (String) gradeMap.get("GRADE");
            final String gradename1 = (String) gradeMap.get("GRADE_NAME1");

            svf.VrSetForm("KNJS341MUSAHIGA.frm", 4);
            svf.VrsOut("GRADE_NAME", gradename1);

            printHrclass(db2, svf, "1", new String[] {grade + "001"}); // 1組の生徒
            printHrclass(db2, svf, "2", new String[] {grade + "002"}); // 2組の生徒
            printHrclass(db2, svf, "3", new String[] {grade + "003", grade + "004", grade + "005"}); // 3、4、5組の生徒
            
            _hasData = true;
        }
    }

    public void printHrclass(final DB2UDB db2, final Vrw32alp svf, final String col, final String[] hrClasses) {
        
        final StringBuffer printHrName = new StringBuffer();
        final List[] studentLists = new List[hrClasses.length];
        int studentCount = 0;
        for (int j = 0; j < hrClasses.length; j++) {
            final String hrName = getHrName(hrClasses[j]);
            printHrName.append(hrName);
            studentLists[j] = getSchregRegdDatList(db2, hrClasses[j]);
            studentCount += studentLists[j].size();
        }
        int line = 0;
        boolean printFlg = false;
        if (!StringUtils.isBlank(printHrName.toString()) || studentCount > 0) {
            svf.VrsOut("HR_NAME" + col, printHrName + "組");
            svf.VrsOut("HR_NUM" + col, String.valueOf(studentCount));
            
            linePrint:
            for (int j = 0; j < studentLists.length; j++) {
                final List studentList1 = studentLists[j];
                if (studentList1.size() == 0) {
                    continue;
                }
                if (printFlg) {
                    for (int i = 0; i < 2; i++) {
                        svf.VrsOut("NAME1", "\n"); // ブランク行
                        svf.VrEndRecord();
                        line += 1;
                    }
                }
                
                svf.VrsOut("HEADER1", hrClasses.length == 1 ? "名前" : getHrName(hrClasses[j]) + "組");
                svf.VrsOut("HEADER2", "欠");
                svf.VrsOut("HEADER3", "遅");
                svf.VrsOut("HEADER4", "早");
                svf.VrsOut("HEADER5", "給");
                svf.VrEndRecord();
                line += 1;

                for (int i = 0; i < studentList1.size(); i++) {
                    final Map m = (Map) studentList1.get(i);
                    final String name = (String) m.get("NAME");
                    svf.VrsOut("NAME" + (getMS932ByteLength(name) > 20 ? "3" : getMS932ByteLength(name) > 16 ? "2" : "1"), name);
                    svf.VrEndRecord();
                    line += 1;
                    printFlg = true;
                    if (line > 40) {
                        break linePrint;
                    }
                }
            }
        }
        if (!printFlg) {
            svf.VrsOut("HEADER1", "名前");
            svf.VrsOut("HEADER2", "欠");
            svf.VrsOut("HEADER3", "遅");
            svf.VrsOut("HEADER4", "早");
            svf.VrsOut("HEADER5", "給");
            svf.VrEndRecord();
            line += 1;
        }
        
        for (;line <= 40;) {
            svf.VrsOut("NAME1", "\n"); // ブランク行
            svf.VrEndRecord();
            line += 1;
        }
        
        svf.VrsOut("NAME1", "合計");
        svf.VrEndRecord();
        line += 1;
    }

    private String getHrName(final String gradeHrClass) {
        return StringUtils.defaultString((String) _param._hrClassName1Map.get(gradeHrClass));
    }

    private List getSchregRegdDatList(final DB2UDB db2, final String gradeHrClass) {
        final List schregRegdDatList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT T1.ATTENDNO, T2.NAME ");
            if ("2".equals(_param._hrClassType)) {
                sql.append(" FROM SCHREG_REGD_FI_DAT T1 ");
            } else {
                sql.append(" FROM SCHREG_REGD_DAT T1 ");
            }
            sql.append(" INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            sql.append(" WHERE T1.YEAR = '" + _param._ctrlYear + "' ");
            sql.append("   AND T1.SEMESTER = '" + _param._ctrlSemester + "' ");
            sql.append("   AND T1.GRADE || T1.HR_CLASS = '" + gradeHrClass + "' ");
            sql.append("  ORDER BY T1.ATTENDNO ");
            
            ps = db2.prepareStatement(sql.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                final Map map = new HashMap();
                map.put("NAME", rs.getString("NAME"));
                schregRegdDatList.add(map);
            }
            
        } catch (Exception e) {
            log.fatal("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return schregRegdDatList;
    }
    
    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str) {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;                      //byte数を取得
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }
    
    /** パラメータクラス */
    private static class Param {
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _grade;
        final String _hrClassType;
        final String _ctrlDate;
        final List _gradeMapList;
        final Map _hrClassName1Map;
        private String _useSchool_KindField;
        private String _SCHOOLKIND;
        
        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _grade = request.getParameter("GRADE");
            _hrClassType = request.getParameter("HR_CLASS_TYPE");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLKIND = request.getParameter("SCHOOLKIND");
            _gradeMapList = getGradeMapList(db2);
            //log.debug(" print grade = " + _gradeMapList);
            _hrClassName1Map = getHrClassName1Map(db2);
        }

        private List getGradeMapList(final DB2UDB db2) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT T1.GRADE, T1.GRADE_NAME1 ");
                sql.append(" FROM SCHREG_REGD_GDAT T1 ");
                if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND)) {
                    sql.append(" INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ");
                    sql.append("   AND T2.SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
                }
                sql.append(" WHERE T1.YEAR = '" + _ctrlYear + "' ");
                if (!"00".equals(_grade)) {
                    sql.append("   AND T1.GRADE = '" + _grade + "' ");
                }
                sql.append(" ORDER BY T1.GRADE ");
                
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Map m = new HashMap();
                    m.put("GRADE", rs.getString("GRADE"));
                    m.put("GRADE_NAME1", rs.getString("GRADE_NAME1"));
                    list.add(m);
                }
                
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private Map getHrClassName1Map(final DB2UDB db2) {
            final Map map = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT GRADE || HR_CLASS AS GRADE_HR_CLASS, HR_CLASS_NAME1 ");
                if ("2".equals(_hrClassType)) {
                    sql.append(" FROM SCHREG_REGD_FI_HDAT ");
                } else {
                    sql.append(" FROM SCHREG_REGD_HDAT ");
                }
                sql.append(" WHERE YEAR = '" + _ctrlYear + "' ");
                sql.append("   AND SEMESTER = '" + _ctrlSemester + "' ");
                
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    map.put(rs.getString("GRADE_HR_CLASS"), rs.getString("HR_CLASS_NAME1"));
                }
                
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return map;
        }
    }
}

// eof