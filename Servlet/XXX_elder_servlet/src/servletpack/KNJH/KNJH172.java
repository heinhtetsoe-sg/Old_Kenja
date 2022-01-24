// kanji=漢字
/*
 * $Id: 2c3d4a54d0bc2c454ed45283eba390be53dfb93d $
 *
 * 作成日: 2010/04/12 14:04:38 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2010 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJH;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJSvfFieldModify;

public class KNJH172 {

    private static final Log log = LogFactory.getLog("KNJH172.class");

    private boolean _hasData;

    Param _param;

    private KNJSvfFieldModify svfobj;   //フォームのフィールド属性変更

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final List hrclassList = getHrClassList(db2);
        svf.VrSetForm("KNJH172.frm", 4);

        for (final Iterator itg = hrclassList.iterator(); itg.hasNext();) {
            final HrClass hrclass = (HrClass) itg.next();
            svf.VrAttribute("HR_NAME", "FF=1");
            svf.VrsOut("HR_NAME", hrclass._hrName);

            int cnt = 1;
            final List studentList = getStudentList(db2, hrclass._hrClass);
            final List studentUnAddrList = new ArrayList();
            for (final Iterator its = studentList.iterator(); its.hasNext();) {
                final Student student = (Student) its.next();

                if (student._isGrd) {
//                    log.debug(hrclass._hrName + " student " + student._attendno + " " +student._schregno + " : " + student._name + " is skipped.");
                    continue;
//                    svf.VrsOut("NOTICE", "");
//                    svf.VrsOut("ATTENDNO2", student._attendno);//ダミー(空行表示)
//                    svf.VrEndRecord();
//                    svf.VrsOut("NOTICE", "");
//                    svf.VrsOut("ATTENDNO2", student._attendno);//ダミー(空行表示)
//                    svf.VrEndRecord();
                } else {
                    svf.VrsOut("GROUP_NO", student._groupNo);
                    svf.VrsOut("ATTENDNO", student._attendno);
                    String len1 = (null != student._name && 10 < student._name.length()) ? "_2" : "_1";
                    svf.VrsOut("NAME1" + len1, student._name);
                    String len2 = (null != student._guardName && 10 < student._guardName.length()) ? "2" : "1";
                    svf.VrsOut("GUARD_NAME" + len2, student._guardName);
                    svf.VrsOut("GUARD_ZIPCD", student._guardZipcd);
                    svf.VrsOut("GUARD_TELNO", student._guardTelno);
//                    svf.VrsOut("GUARD_ADDR", student._guardAddr);
                    svfFieldAttribute_GUARD_ADDR(svf, student._guardAddr1, cnt, 50);
                    svf.VrEndRecord();//１行目
                    svfFieldAttribute_ADDR(svf, student._guardAddr2, cnt, 50);
                    svf.VrsOut("ATTENDNO2", student._attendno);//ダミー
                    svf.VrEndRecord();//２行目
                    // 保護者住所と生徒住所が違っている時
                    if (student.isUnAddr()) {
                        studentUnAddrList.add(student);
                    }
                }

                cnt++;
                cnt++;
                _hasData = true;
            }
            
            // 保護者住所と違っている生徒住所を表記
            if (!studentUnAddrList.isEmpty()) {
                // ３行空行
                svf.VrsOut("NOTICE", "");
                svf.VrsOut("ATTENDNO2", "＊");//ダミー
                svf.VrEndRecord();
                svf.VrsOut("NOTICE", "");
                svf.VrsOut("ATTENDNO2", "＊");//ダミー
                svf.VrEndRecord();
                cnt++;
                cnt++;
                svf.VrsOut("NOTICE", "");
                svf.VrsOut("ATTENDNO2", "＊");//ダミー
                svf.VrEndRecord();
                svf.VrsOut("NOTICE", "＊　保護者と住所を異にする生徒の住所");
                svf.VrsOut("ATTENDNO2", "＊");//ダミー
                svf.VrEndRecord();
                cnt++;
                cnt++;
                for (final Iterator it = studentUnAddrList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    
                    String len1 = (null != student._name && 10 < student._name.length()) ? "2" : "1";
                    svf.VrsOut("GUARD_NAME" + len1, student._name);
                    svf.VrsOut("GUARD_ZIPCD", student._zipcd);
                    svf.VrsOut("GUARD_TELNO", student._telno);
                    svfFieldAttribute_GUARD_ADDR(svf, student._addr1, cnt, 50);
                    svf.VrEndRecord();//１行目
                    svfFieldAttribute_ADDR(svf, student._addr2, cnt, 50);
                    svf.VrsOut("ATTENDNO2", student._attendno);//ダミー
                    svf.VrEndRecord();//２行目
                    cnt++;
                    cnt++;
                }
            }
        }
    }

    /*
     * ＳＶＦ−ＦＯＲＭフィールド属性変更(RECORD) => 文字数により文字ピッチ及びＹ軸を変更する
     */
    private void svfFieldAttribute_GUARD_ADDR(
            final Vrw32alp svf,
            final String name,
            int ln,
            final int lineMax
    ) {
        ln = (ln % lineMax == 0)? lineMax: ln % lineMax;  // 出力する行位置を再設定
        try {
            if( svfobj == null )svfobj = new KNJSvfFieldModify();
            svfobj.width = 1000;     //フィールドの幅(ドット)
            svfobj.height = 80;      //フィールドの高さ(ドット)
            svfobj.ystart = 672 - 80;    //開始位置(ドット)
            svfobj.minnum = 36;      //最小設定文字数
            svfobj.maxnum = 100;     //最大設定文字数
            svfobj.setRetvalue( name, ln );

            svf.VrAttribute("GUARD_ADDR" , "X="+ ( 1901 ) );  //開始Ｘ軸
            svf.VrAttribute("GUARD_ADDR" , "Y="+ svfobj.jiku );  //開始Ｙ軸
            svf.VrAttribute("GUARD_ADDR" , "Size=" + svfobj.size );  //文字サイズ
            svf.VrsOut("GUARD_ADDR",  name );
        } catch( Exception e ){
            log.error("svf.VrAttribute error! ", e);
        }
    }

    /*
     * ＳＶＦ−ＦＯＲＭフィールド属性変更(RECORD) => 文字数により文字ピッチ及びＹ軸を変更する
     */
    private void svfFieldAttribute_ADDR(
            final Vrw32alp svf,
            final String name,
            int ln,
            final int lineMax
    ) {
        ln = (ln % lineMax == 0)? lineMax: ln % lineMax;  // 出力する行位置を再設定
        try {
            if( svfobj == null )svfobj = new KNJSvfFieldModify();
            svfobj.width = 1000;     //フィールドの幅(ドット)
            svfobj.height = 80;      //フィールドの高さ(ドット)
            svfobj.ystart = 752 - 80;    //開始位置(ドット)
            svfobj.minnum = 36;      //最小設定文字数
            svfobj.maxnum = 100;     //最大設定文字数
            svfobj.setRetvalue( name, ln );

            svf.VrAttribute("ADDR" , "X="+ ( 1901 ) );  //開始Ｘ軸
            svf.VrAttribute("ADDR" , "Y="+ svfobj.jiku );  //開始Ｙ軸
            svf.VrAttribute("ADDR" , "Size=" + svfobj.size );  //文字サイズ
            svf.VrsOut("ADDR",  name );
        } catch( Exception e ){
            log.error("svf.VrAttribute error! ", e);
        }
    }

    private List getHrClassList(final DB2UDB db2) throws SQLException {
        final List rtnList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = getHrClassSql();
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String hrClass = rs.getString("GRADE_HR_CLASS");
                final String hrName = rs.getString("HR_NAME");
                final HrClass hrclass = new HrClass(
                        hrClass,
                        hrName);
                rtnList.add(hrclass);
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtnList;
    }

    private String getHrClassSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT DISTINCT ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.GRADE || T1.HR_CLASS AS GRADE_HR_CLASS, ");
        stb.append("     T2.HR_NAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT T2 ON ");
        stb.append("             T1.YEAR = T2.YEAR AND T1.SEMESTER = T2.SEMESTER AND  ");
        stb.append("             T1.GRADE = T2.GRADE AND T1.HR_CLASS = T2.HR_CLASS ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._ctrlYear + "' AND ");
        stb.append("     T1.SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE DESC, ");
        stb.append("     T1.HR_CLASS ");
        return stb.toString();
    }

    private class HrClass {
        final String _hrClass;
        final String _hrName;

        HrClass(final String hrClass,
                final String hrName
        ) {
            _hrClass = hrClass;
            _hrName = hrName;
        }
    }

    private List getStudentList(final DB2UDB db2, final String hrClass) throws SQLException {
        final List rtnList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = getStudentSql(hrClass);
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String attendno = rs.getString("ATTENDNO");
                final String name = rs.getString("NAME");
                final String zipcd = rs.getString("ZIPCD");
                final String addr1 = rs.getString("ADDR1");
                final String addr2 = rs.getString("ADDR2");
                final String telno = rs.getString("TELNO");
                final String guardName = rs.getString("GUARD_NAME");
                final String guardZipcd = rs.getString("GUARD_ZIPCD");
                final String guardAddr1 = rs.getString("GUARD_ADDR1");
                final String guardAddr2 = rs.getString("GUARD_ADDR2");
                final String guardTelno = rs.getString("GUARD_TELNO");
                final String groupNo = rs.getString("GO_HOME_GROUP_NO");
                final boolean isGrd = "1".equals(rs.getString("IS_GRD"));
                final Student student = new Student(
                        schregno,
                        attendno,
                        name,
                        zipcd,
                        addr1,
                        addr2,
                        telno,
                        guardName,
                        guardZipcd,
                        guardAddr1,
                        guardAddr2,
                        guardTelno,
                        groupNo,
                        isGrd);
                rtnList.add(student);
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtnList;
    }

    private String getStudentSql(final String hrClass) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH ADDR_INFO AS( ");
        stb.append("     SELECT ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T1.ZIPCD, ");
        stb.append("         CASE WHEN T1.ADDR1 IS NULL THEN '' ELSE T1.ADDR1 END AS ADDR1, ");
        stb.append("         CASE WHEN T1.ADDR2 IS NULL THEN '' ELSE T1.ADDR2 END AS ADDR2, ");
        stb.append("         T1.TELNO ");
        stb.append("     FROM ");
        stb.append("         SCHREG_ADDRESS_DAT T1 ");
        stb.append("     WHERE ");
        stb.append("         T1.ISSUEDATE IN(SELECT ");
        stb.append("                             MAX(T2.ISSUEDATE) AS ISSUEDATE ");
        stb.append("                         FROM ");
        stb.append("                             SCHREG_ADDRESS_DAT T2 ");
        stb.append("                         WHERE ");
        stb.append("                             T1.SCHREGNO = T2.SCHREGNO AND ");
        stb.append("                             '" + _param._ctrlDate + "' BETWEEN T2.ISSUEDATE AND ");
        stb.append("                             CASE WHEN T2.EXPIREDATE IS NULL THEN '9999-12-31' ELSE T2.EXPIREDATE END ) ");
        stb.append(" ) ");

        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     '(' || T2.GO_HOME_GROUP_NO || ')' AS GO_HOME_GROUP_NO, ");
        stb.append("     INT(T1.ATTENDNO) AS ATTENDNO, ");
        stb.append("     T3.NAME, ");
        stb.append("     T4.GUARD_NAME, ");
        stb.append("     T4.GUARD_ZIPCD, ");
        stb.append("     CASE WHEN T4.GUARD_ADDR1 IS NULL THEN '' ELSE T4.GUARD_ADDR1 END AS GUARD_ADDR1, ");
        stb.append("     CASE WHEN T4.GUARD_ADDR2 IS NULL THEN '' ELSE T4.GUARD_ADDR2 END AS GUARD_ADDR2, ");
        stb.append("     T4.GUARD_TELNO, ");
        stb.append("     T5.ZIPCD, ");
        stb.append("     value(T5.ADDR1,'') AS ADDR1, ");
        stb.append("     value(T5.ADDR2,'') AS ADDR2, ");
        stb.append("     T5.TELNO, ");
        stb.append("     CASE WHEN T3.GRD_DIV IS NOT NULL AND T3.GRD_DIV <> '4' AND VALUE(GRD_DATE, '9999-12-31') < '" + _param._ctrlDate + "' THEN '1' END AS IS_GRD");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     LEFT JOIN SCHREG_ENVIR_DAT T2 ON T1.SCHREGNO = T2.SCHREGNO AND T2.GO_HOME_GROUP_NO <> '00' ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST T3 ON T1.SCHREGNO = T3.SCHREGNO ");
        stb.append("     LEFT JOIN GUARDIAN_DAT T4 ON T1.SCHREGNO = T4.SCHREGNO ");
        stb.append("     LEFT JOIN ADDR_INFO T5 ON T1.SCHREGNO = T5.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._ctrlYear + "' AND ");
        stb.append("     T1.SEMESTER = '" + _param._ctrlSemester + "' AND ");
        stb.append("     T1.GRADE || T1.HR_CLASS = '" + hrClass + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.ATTENDNO ");
        return stb.toString();
    }

    private class Student {
        final String _schregno;
        final String _attendno;
        final String _name;
        final String _zipcd;
        final String _addr1;
        final String _addr2;
        final String _telno;
        final String _guardName;
        final String _guardZipcd;
        final String _guardAddr1;
        final String _guardAddr2;
        final String _guardTelno;
        final String _groupNo;
        final boolean _isGrd;

        Student(final String schregno,
                final String attendno,
                final String name,
                final String zipcd,
                final String addr1,
                final String addr2,
                final String telno,
                final String guardName,
                final String guardZipcd,
                final String guardAddr1,
                final String guardAddr2,
                final String guardTelno,
                final String groupNo,
                final boolean isGrd
        ) {
            _schregno = schregno;
            _attendno = attendno;
            _name = name;
            _zipcd = zipcd;
            _addr1 = addr1;
            _addr2 = addr2;
            _telno = telno;
            _guardName = guardName;
            _guardZipcd = guardZipcd;
            _guardAddr1 = guardAddr1;
            _guardAddr2 = guardAddr2;
            _guardTelno = guardTelno;
            _groupNo = groupNo;
            _isGrd = isGrd;
        }

        private boolean isUnAddr() {
            if ((_guardAddr1 + _guardAddr2).equals(_addr1 + _addr2)) return false;
            return true;
        }
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
        }

    }
}

// eof
