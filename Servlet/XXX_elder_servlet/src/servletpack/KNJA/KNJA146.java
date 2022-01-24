// kanji=漢字
/*
 * $Id: 0535acb9182d484a03fadd78bb6d0e0c1cf0d306 $
 *
 * 作成日: 2008/04/24 18:16:28 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2008-2012 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;

/**
 * 学校教育システム 賢者 [学籍管理] ＜ＫＮＪＡ１４６＞ 身分証明書（自修館）
 */

public class KNJA146 {

    private static final Log log = LogFactory.getLog(KNJA146.class);

    Param _param;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private static final String KINDCD101 = "101";
    private static final String KINDCD102 = "102";

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Vrw32alp svf = new Vrw32alp(); //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null; //Databaseクラスを継承したクラス
        try {
            init(response, svf);

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            //  ＳＶＦ作成処理
            boolean hasData = false; //該当データなしフラグ

            //SVF出力
            hasData = printMain(db2, svf);

            log.debug("hasData=" + hasData);

            //  該当データ無し
            if (!hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } finally {
            svf.VrQuit();
            db2.commit();
            db2.close();
        }

    }// doGetの括り

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        dumpParam(request, param);
        return param;
    }

    /** パラメータダンプ */
    private void dumpParam(final HttpServletRequest request, final Param param) {
        log.fatal("$Revision: 56595 $"); // CVSキーワードの取り扱いに注意
        if (log.isDebugEnabled()) {
            final Enumeration enums = request.getParameterNames();
            while (enums.hasMoreElements()) {
                final String name = (String) enums.nextElement();
                final String[] values = request.getParameterValues(name);
                log.debug("parameter:name=" + name + ", value=[" + StringUtils.join(values, ',') + "]");
            }
        }
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _sdate;
        private final String _edate;
        private final String _documentRoot;
        private final String _output;
        private final String _disp;
        private final String _inState;
        private final String _imageFolder;
        private final String _fileExtension;
        private final Map _certifSchool = new HashMap();
        private String _z010 = "";
        private String _z012 = "";
        private final boolean _isSeireki;

        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            ResultSet rsZ010 = null;
            ResultSet rsZ012 = null;
            ResultSet rsCertif = null;

            try {
                _year = request.getParameter("YEAR");
                _semester = request.getParameter("SEMESTER");
                _documentRoot = request.getParameter("DOCUMENTROOT");
                _output = request.getParameter("OUTPUT");
                _disp = request.getParameter("DISP");

                final String classcd[] = request.getParameterValues("category_selected");
                String sep = "";
                final StringBuffer stb = new StringBuffer();
                stb.append("(");
                for( int ia=0 ; ia<classcd.length ; ia++ ){
                    stb.append(getInstate(classcd[ia], sep));
                    sep = ",";
                }
                stb.append(")");
                _inState = stb.toString();

                _z010 = setNameMst(db2, "Z010", "00");
                _z012 = setNameMst(db2, "Z012", "01");
                _isSeireki = _z012.equals("2") ? true : false;

                _sdate = request.getParameter("TERM_SDATE");
                _edate = request.getParameter("TERM_EDATE");

                KNJ_Get_Info getinfo = new KNJ_Get_Info();
                KNJ_Get_Info.ReturnVal returnval = null;
                // 写真データ
                returnval = getinfo.Control(db2);
                _imageFolder = returnval.val4;      // 格納フォルダ
                _fileExtension = returnval.val5;    // 拡張子

                getinfo = null;
                returnval = null;

                // 証明書学校データ
                String sql = "SELECT * FROM CERTIF_SCHOOL_DAT " + "WHERE YEAR='" + _year + "' AND CERTIF_KINDCD IN('101','102')";
                db2.query(sql);
                rsCertif = db2.getResultSet();
                while (rsCertif.next()) {
                    final CertifSchool certifSchool = new CertifSchool(rsCertif.getString("SYOSYO_NAME"),
                                                                       rsCertif.getString("SYOSYO_NAME2"),
                                                                       rsCertif.getString("SCHOOL_NAME"),
                                                                       rsCertif.getString("JOB_NAME"),
                                                                       rsCertif.getString("PRINCIPAL_NAME"),
                                                                       rsCertif.getString("REMARK1"),
                                                                       rsCertif.getString("REMARK2"),
                                                                       rsCertif.getString("REMARK3"),
                                                                       rsCertif.getString("REMARK4"),
                                                                       rsCertif.getString("REMARK5"),
                                                                       rsCertif.getString("REMARK6"),
                                                                       rsCertif.getString("REMARK7"),
                                                                       rsCertif.getString("REMARK8"),
                                                                       rsCertif.getString("REMARK9"),
                                                                       rsCertif.getString("REMARK10")
                                                                      );
                    _certifSchool.put(rsCertif.getString("CERTIF_KINDCD"), certifSchool);
                }
                rsCertif.close();

            } finally {
                DbUtils.closeQuietly(rsZ010);
                DbUtils.closeQuietly(rsZ012);
                DbUtils.closeQuietly(rsCertif);
                db2.commit();
            }
        }

        private String setNameMst(final DB2UDB db2, final String namecd1, final String namecd2) throws SQLException {
            String rtnSt = "";
            db2.query(getNameMst(_year, namecd1, namecd2));
            ResultSet rs = db2.getResultSet();
            while (rs.next()) {
                rtnSt = rs.getString("NAME1");
            }
            return rtnSt;
        }

        private String getInstate(final String classcd, String sep) {
            String rtnSt = "";
            if (_disp.equals("2")) {
                rtnSt = sep + "'" + classcd + "'";
            }
            if (_disp.equals("1")) {
                rtnSt = sep + "'" + (classcd).substring(0,(classcd).indexOf("-")) + "'";
            }
            return rtnSt;
        }

        private String getNameMst(final String year, final String namecd1, final String namecd2) {
            final String rtnSql = " SELECT "
                                + "     * "
                                + " FROM "
                                + "     V_NAME_MST "
                                + " WHERE "
                                + "     YEAR = '" + year + "' "
                                + "     AND NAMECD1 = '" + namecd1 + "' "
                                + "     AND NAMECD2 = '" + namecd2 + "'";
            return rtnSql;
        }

        private String changePrintDate(final String date) {
            if (_isSeireki) {
                return date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date);
            } else {
                return KNJ_EditDate.h_format_JP(date);
            }
        }
    }

    private class CertifSchool {
        final String _syosyoName;
        final String _syosyoName2;
        final String _schoolName;
        final String _jobName;
        final String _principalName;
        final String _remark1;
        final String _remark2;
        final String _remark3;
        final String _remark4;
        final String _remark5;
        final String _remark6;
        final String _remark7;
        final String _remark8;
        final String _remark9;
        final String _remark10;

        CertifSchool(
                final String syosyoName,
                final String syosyoName2,
                final String schoolName,
                final String jobName,
                final String principalName,
                final String remark1,
                final String remark2,
                final String remark3,
                final String remark4,
                final String remark5,
                final String remark6,
                final String remark7,
                final String remark8,
                final String remark9,
                final String remark10
        ) {
            _syosyoName = syosyoName;
            _syosyoName2 = syosyoName2;
            _schoolName = schoolName;
            _jobName = jobName;
            _principalName = principalName;
            _remark1 = remark1;
            _remark2 = remark2;
            _remark3 = remark3;
            _remark4 = remark4;
            _remark5 = remark5;
            _remark6 = remark6;
            _remark7 = remark7;
            _remark8 = remark8;
            _remark9 = remark9;
            _remark10 = remark10;
        }
    }

    private boolean printMain(DB2UDB db2, Vrw32alp svf) throws ParseException, SQLException {
        svf.VrSetForm("KNJA146.frm", 1);
        final List printData = getPrintData(db2);
        boolean hasData = printOut(svf, printData);
        return hasData;
    }

    private List getPrintData(final DB2UDB db2) throws ParseException, SQLException {
        final List rtnList = new ArrayList();
        final String studenSql = getStudentInfoSql();
        log.debug(studenSql);
        PreparedStatement psStudent = null;
        ResultSet rsStudent = null;

        try {
            psStudent = db2.prepareStatement(studenSql);
            rsStudent = psStudent.executeQuery();
            while (rsStudent.next()) {
                final Student student = new Student(rsStudent.getString("SCHREGNO"),
                                                      rsStudent.getString("GRADE"),
                                                      rsStudent.getString("HR_CLASS"),
                                                      rsStudent.getString("ATTENDNO"),
                                                      rsStudent.getString("NAME"),
                                                      rsStudent.getString("BIRTHDAY"),
                                                      rsStudent.getString("ENT_YEAR"),
                                                      rsStudent.getString("COURSENAME"),
                                                      rsStudent.getString("ADDR1"),
                                                      rsStudent.getString("ADDR2"));
                rtnList.add(student);
                log.debug(student);
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, psStudent, rsStudent);
        }
        
        return rtnList;
    }

    private boolean printOut(final Vrw32alp svf, final List printData) {
        CertifSchool certif = (CertifSchool) _param._certifSchool.get(KINDCD101);
        boolean hasData = false;
        for (final Iterator iter = printData.iterator(); iter.hasNext();) {
            final Student student = (Student) iter.next();

            svf.VrsOut("SCHREGNO", student._schregno.substring(2));
            svf.VrsOut("ENT_YEAR", student._entYear + "年度生");
            svf.VrsOut("NAME", student._name);
            svf.VrsOut("BIRTHDAY", _param.changePrintDate(student._birthDay) + "生");
            svf.VrsOut("ADDR1", student._addr1);
            svf.VrsOut("ADDR2", student._addr2);
            svf.VrsOut("SDATE", _param.changePrintDate(_param._sdate));
            svf.VrsOut("EDATE", _param.changePrintDate(_param._edate));
            svf.VrsOut("SCHOOL_ZIP", certif != null ? certif._remark2 : "");
            svf.VrsOut("SCHOOL_ADDR", certif != null ? certif._remark1 : "");
            svf.VrsOut("SCHOOL_TEL", certif != null ? certif._remark3 : "");
            svf.VrsOut("SCHOOL_NAME", certif != null ? certif._schoolName : "");
            svf.VrsOut("JOB_NAME", certif != null ? certif._jobName : "");
            svf.VrsOut("PRINCIPAL_NAME", certif != null ? certif._principalName : "");
            svf.VrsOut("BARCODE", student._schregno.substring(2));

            final String image_pass = _param._documentRoot + "/" + _param._imageFolder + "/";   //イメージパス
            String photo_check = image_pass + "P" + student._schregno + "." + _param._fileExtension;    //顔写真
            setImage(svf, photo_check, "PHOTO");

            final String stamp_check = image_pass + "SCHOOLSTAMP.jpg";  //学校長印
            setImage(svf, stamp_check, "STAMP");
            svf.VrEndPage();
            hasData = true;
        }
        return hasData;
    }

    private void setImage(final Vrw32alp svf, final String file_check, final String fieldName) {
        File file = new File(file_check);
        if (file.exists()) {
            svf.VrsOut(fieldName, file_check );
        }
    }

    private class Student {
        final String _schregno;
        final String _grade;
        final String _hrClass;
        final String _attendNo;
        final String _name;
        final String _birthDay;
        final String _entYear;
        final String _courseName;
        final String _addr1;
        final String _addr2;

        public Student(
                final String schregno,
                final String grade,
                final String hrClass,
                final String attendNo,
                final String name,
                final String birthDay,
                final String entYear,
                final String courseName,
                final String addr1,
                final String addr2
        ) {
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _attendNo = attendNo;
            _name = name;
            _birthDay = birthDay;
            _entYear = entYear;
            _courseName = courseName;
            _addr1 = addr1;
            _addr2 = addr2;
        }

        public String toString() {
            return "学籍番号:" + _schregno
            + " 年組番:" + _grade + _hrClass + _attendNo
            + " 名前:" + _name;
        }

    }
    /** 生徒情報* */
    private String getStudentInfoSql() {
        StringBuffer stb = new StringBuffer();
        // 生徒情報
        stb.append("WITH SCHNO AS ( ");
        stb.append(" SELECT ");
        stb.append("    * ");
        stb.append(" FROM ");
        // 1:新入生データ,2:在籍データ
        if (_param._output.equals("1")) {
            stb.append("    CLASS_FORMATION_DAT ");
        } else {
            stb.append("    SCHREG_REGD_DAT ");
        }
        stb.append(" WHERE ");
        stb.append("    YEAR='" + _param._year + "' ");
        stb.append("    AND SEMESTER='" + _param._semester + "' ");
        // 1:個人,2:クラス
        if (_param._disp.equals("1")) {
            stb.append("    AND SCHREGNO IN " + _param._inState + " ");
        }
        if (_param._disp.equals("2")) {
            stb.append("    AND GRADE || HR_CLASS IN " + _param._inState + " ");
        }
        stb.append(" ) ");
        
        // 住所
        stb.append(", SCH_ADDR1 AS ( ");
        stb.append(" SELECT ");
        stb.append("    SCHREGNO, ");
        stb.append("    MAX(ISSUEDATE) AS ISSUEDATE ");
        stb.append(" FROM ");
        stb.append("    SCHREG_ADDRESS_DAT ");
        stb.append(" WHERE ");
        stb.append("    SCHREGNO IN (SELECT SCHREGNO FROM SCHNO) ");
        stb.append(" GROUP BY ");
        stb.append("    SCHREGNO ");
        stb.append(" ) ");
        stb.append(", SCH_ADDR2 AS ( ");
        stb.append(" SELECT ");
        stb.append("    SCHREGNO, ");
        stb.append("    ISSUEDATE, ");
        stb.append("    ADDR1, ");
        stb.append("    ADDR2 ");
        stb.append(" FROM ");
        stb.append("    SCHREG_ADDRESS_DAT ");
        stb.append(" WHERE ");
        stb.append("    SCHREGNO IN (SELECT SCHREGNO FROM SCHNO) ");
        stb.append(" ) ");
        stb.append(", SCH_ADDR AS ( ");
        stb.append(" SELECT ");
        stb.append("    W2.SCHREGNO, ");
        stb.append("    W2.ADDR1, ");
        stb.append("    W2.ADDR2 ");
        stb.append(" FROM ");
        stb.append("    SCH_ADDR1 W1, ");
        stb.append("    SCH_ADDR2 W2 ");
        stb.append(" WHERE ");
        stb.append("    W1.SCHREGNO = W2.SCHREGNO ");
        stb.append("    AND W1.ISSUEDATE = W2.ISSUEDATE ");
        stb.append(" ) ");
        
        // メイン
        stb.append(" SELECT ");
        stb.append("    T1.SCHREGNO, ");
        stb.append("    T1.GRADE, ");
        stb.append("    T1.HR_CLASS, ");
        stb.append("    VALUE(T1.ATTENDNO,'0') AS ATTENDNO, ");
        stb.append("    T5.COURSENAME, ");
        if (_param._output.equals("1")) {
            stb.append("    T0.ENTERYEAR AS ENT_YEAR, ");
            stb.append("    CASE WHEN T1.GRADE = '01' AND VALUE(T1.REMAINGRADE_FLG,'0') = '0' ");
            stb.append("         THEN T0.NAME ");
            stb.append("         ELSE T2.NAME ");
            stb.append("    END AS NAME, "); // 氏名
            stb.append("    CASE WHEN T1.GRADE = '01' AND VALUE(T1.REMAINGRADE_FLG,'0') = '0' ");
            stb.append("         THEN T0.BIRTHDAY ");
            stb.append("         ELSE T2.BIRTHDAY ");
            stb.append("    END AS BIRTHDAY, "); // 生年月日
            stb.append("    CASE WHEN T1.GRADE = '01' AND VALUE(T1.REMAINGRADE_FLG,'0') = '0' ");
            stb.append("         THEN T0.ADDR1 ");
            stb.append("         ELSE T3.ADDR1 ");
            stb.append("    END AS ADDR1, "); // 住所１
            stb.append("    CASE WHEN T1.GRADE = '01' AND VALUE(T1.REMAINGRADE_FLG,'0') = '0' ");
            stb.append("         THEN T0.ADDR2 ");
            stb.append("         ELSE T3.ADDR2 ");
            stb.append("    END AS ADDR2 "); // 住所２
        } else {
            stb.append("    CASE WHEN MONTH(T2.ENT_DATE) < 3 ");
            stb.append("         THEN YEAR(T2.ENT_DATE) - 1 ");
            stb.append("         ELSE YEAR(T2.ENT_DATE) ");
            stb.append("    END AS ENT_YEAR, "); // 入学年度
            stb.append("    T2.NAME, T2.BIRTHDAY, T3.ADDR1, T3.ADDR2 ");
        }
        stb.append(" FROM ");
        stb.append("    SCHNO T1 ");
        stb.append("    LEFT JOIN FRESHMAN_DAT T0 ON T0.ENTERYEAR = T1.YEAR ");
        stb.append("         AND T0.SCHREGNO = T1.SCHREGNO ");
        stb.append("    LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("    LEFT JOIN SCH_ADDR T3 ON T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("    LEFT JOIN COURSE_MST T5 ON T5.COURSECD = T1.COURSECD ");
        stb.append(" ORDER BY ");
        stb.append("    T1.GRADE, ");
        stb.append("    T1.HR_CLASS, ");
        stb.append("    T1.ATTENDNO ");

        return stb.toString();
    }

}// クラスの括り
