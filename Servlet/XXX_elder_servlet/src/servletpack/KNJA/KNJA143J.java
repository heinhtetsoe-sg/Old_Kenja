// kanji=漢字
/*
 * $Id: b6e1ec3bf7e948e62d3f113251e70a8cb9798f27 $
 *
 * 作成日: 2010/03/24 10:24:21 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: b6e1ec3bf7e948e62d3f113251e70a8cb9798f27 $
 */
public class KNJA143J {

    private static final Log log = LogFactory.getLog("KNJA143J.class");

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

    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str) {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
    }

    private static Map getMappedMap(final Map map, final String key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap());
        }
        return (Map) map.get(key1);
    }

    private List getPageList(final List list, final int cnt) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= cnt) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final List printStudents = getPrintStudent(db2);

        final List pageList = getPageList(printStudents, 5);
        for (final Iterator pit = pageList.iterator(); pit.hasNext();) {
            final List studentList = (List) pit.next();

            if ("1".equals(_param._output)) {
                svf.VrSetForm("KNJA143J_2.frm", 4);
            } else {
                svf.VrSetForm("KNJA143J_1.frm", 4);
            }

            for (int i = 0; i < studentList.size(); i++) {
                final Student student = (Student) studentList.get(i);

                //画像--------------
                String photo = "";                      //顔写真
                String stamp = "SCHOOLSTAMP" + (null == student._schoolKind ? "" : ("_" + student._schoolKind)) + _param._extensionStamp;  //学校印
                String photo_check = "";
                String stamp_check = _param._documentroot + "/" + _param._imagePass + "/" + stamp;
                File f2 = new File(stamp_check);        //学校長印データ存在チェック用

                //顔写真
                photo = "P" + student._schregno + "." + _param._extensionPhoto;
                photo_check = _param._documentroot + "/" + _param._imagePass + "/" + photo;
                File f1 = new File(photo_check);//写真データ存在チェック用
                if (f1.exists()) {
                    svf.VrsOut("PHOTO_BMP", photo_check );//顔写真
                }
                //学校印
                if (_param._printSchoolStamp && f2.exists()) {
                    svf.VrsOut("STAMP_BMP", stamp_check );//学校印
                }
                svf.VrsOut("BARCODE", student._schregno); // 学籍番号

                svf.VrsOut("TITLE", "P".equals(student._schoolKind) ? "児童証明書" : "生徒証明書");
                svf.VrsOut("SCHREGNO", student._schregno);
                svf.VrsOut("SENTENCE", "P".equals(student._schoolKind) ? "児童" : "生徒");
                svf.VrsOut("SEITO", "P".equals(student._schoolKind) ? "児童" : "生徒");
                final String setKind;
                if (_param.isCollege()) {
                    setKind = "P".equals(student._schoolKind) ? "小学部" : "J".equals(student._schoolKind) ? "中学部" : "高等部";
                } else {
                    setKind = "P".equals(student._schoolKind) ? "小学校" : "J".equals(student._schoolKind) ? "中学校" : "高等学校";
                }
                final String coursecodename;
                if ("P".equals(student._schoolKind)) {
                    coursecodename = student._gradeName1 != null && student._gradeName1.length() > 0 ? "(" + student._gradeName1 + ")" : "";
                } else {
                    coursecodename = (!_param.isCollege() && student._courseCodeName.length() > 0 ? "(" + student._courseCodeName + ")" : "");
                }
                svf.VrsOut("ENT_SCHOOL", setKind + " " + student._gradeName2 + " " + StringUtils.replace(student._hrName, "組", "")
                        + "組 " + Integer.parseInt(student._attendNo) + "番" + coursecodename);

                svf.VrsOut("KANA", student._kana);
                final int nameKeta = getMS932ByteLength(student._name);
                svf.VrsOut("NAME" + (nameKeta > 30 ? "3" : nameKeta > 20 ? "2" : ""), student._name); // 生徒氏名

                //生年月日
                if (student._birthday != null) {
                    String birth = KNJ_EditDate.h_format_JP(student._birthday);
                    String arr_birth[] = KNJ_EditDate.tate_format(birth);
                    for (int birthI = 1; birthI < 5; birthI++) {
                        if (arr_birth[1] == null) arr_birth[1] = (arr_birth[0]).substring(2);
                        svf.VrsOut("BIRTHDAY"+String.valueOf(birthI), arr_birth[birthI - 1] );
                    }
                    final int birthYMD = Integer.parseInt(StringUtils.replace(student._birthday, "-", ""));
                    final int kijunYMD = Integer.parseInt(StringUtils.replace(_param._sDate, "-", ""));
                    final int nenrei = (kijunYMD - birthYMD) / 10000;
                    svf.VrsOut("AGE", String.valueOf(nenrei));
                }

                final String setAddr = student._addr1 + student._addr2;
                final int addrKeta = getMS932ByteLength(setAddr);
                svf.VrsOut("ADDRESS1" + (addrKeta > 50 ? "_4" : addrKeta > 40 ? "_3" : addrKeta > 30 ? "_2" : ""), setAddr);

                //発行日
                if (_param._sDate != null) {
                    String sday = KNJ_EditDate.h_format_JP(_param._sDate);
                    String arr_sday[] = KNJ_EditDate.tate_format(sday);
                    for (int sdayI = 1; sdayI < 5; sdayI++) {
                        if (arr_sday[1] == null) arr_sday[1] = (arr_sday[0]).substring(2);
                        svf.VrsOut("SDATE"+String.valueOf(sdayI), arr_sday[sdayI - 1] );
                    }
                }

                String certifSchoolCd = "";
                if ("H".equals(student._schoolKind)) {
                    certifSchoolCd = "101";
                } else if ("J".equals(student._schoolKind)) {
                    certifSchoolCd = "102";
                } else if ("P".equals(student._schoolKind)) {
                    certifSchoolCd = "140";
                }
                final Map certifSchoolDat = getMappedMap(_param._certifSchoolDat, certifSchoolCd);
                final String setSchoolAddr = (String) certifSchoolDat.get("REMARK1");
                final int schoolAddrKeta = getMS932ByteLength(setSchoolAddr);
                svf.VrsOut("SCHOOLADDRESS1" + (schoolAddrKeta > 50 ? "_4" : schoolAddrKeta > 40 ? "_3" : schoolAddrKeta > 32 ? "_2" : ""), setSchoolAddr);
                svf.VrsOut("TELNO", "電話" + (String) certifSchoolDat.get("REMARK3"));

                svf.VrsOut("SCHOOLNAME1", (String) certifSchoolDat.get("SCHOOL_NAME"));
                svf.VrsOut("STAFFNAME", (String) certifSchoolDat.get("PRINCIPAL_NAME"));
                svf.VrsOut("JOBNAME", (String) certifSchoolDat.get("JOB_NAME"));

                svf.VrsOut("LIMIT", KNJ_EditDate.h_format_JP(_param._eDate) + "まで有効");

                if (_param._printStation) {
                    final int gesyaKeta = getMS932ByteLength(student._gesyaALL);
                    svf.VrsOut("SECTION" + (gesyaKeta > 20 ? "3" : gesyaKeta > 14 ? "2" : "1"), student._gesyaALL);
                }
                _hasData = true;
                svf.VrEndRecord();
            }

        }
    }

    private String getGesya(final String gesyaALL, final String gesya) {
        if (gesyaALL != "") return gesyaALL;
        if (gesya == null) return gesyaALL;
        return gesya;
    }

    private List getPrintStudent(final DB2UDB db2) throws SQLException {
        final List rtnList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = getStudentSql();
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String name = "1".equals(rs.getString("USE_REAL_NAME")) ? rs.getString("REAL_NAME") : rs.getString("NAME");
                final String kana = "1".equals(rs.getString("USE_REAL_NAME")) ? rs.getString("REAL_NAME_KANA") : rs.getString("NAME_KANA");
                final String birthday = rs.getString("BIRTHDAY");
                final String courseName = rs.getString("COURSENAME");
                final String majorName = rs.getString("MAJORNAME");
                final String courseCodeName = rs.getString("COURSECODEABBV1");
                final String addr1 = rs.getString("ADDR1");
                final String addr2 = rs.getString("ADDR2");
                final String grade = rs.getString("GRADE");
                final String gradeName1 = rs.getString("GRADE_NAME1");
                final String gradeName2 = rs.getString("GRADE_NAME2");
                final String attendNo = rs.getString("ATTENDNO");
                final String schoolKind = rs.getString("SCHOOL_KIND");
                final String hrName = rs.getString("HR_CLASS_NAME1");
                String gesyaALL = "";
                gesyaALL = getGesya(gesyaALL, rs.getString("GESYA_7"));
                gesyaALL = getGesya(gesyaALL, rs.getString("GESYA_6"));
                gesyaALL = getGesya(gesyaALL, rs.getString("GESYA_5"));
                gesyaALL = getGesya(gesyaALL, rs.getString("GESYA_4"));
                gesyaALL = getGesya(gesyaALL, rs.getString("GESYA_3"));
                gesyaALL = getGesya(gesyaALL, rs.getString("GESYA_2"));
                gesyaALL = getGesya(gesyaALL, rs.getString("GESYA_1"));
                if (StringUtils.isBlank(gesyaALL) && StringUtils.isBlank(rs.getString("JOSYA_1"))) {
                    gesyaALL = "";
                } else {
                    gesyaALL = gesyaALL + "・" + StringUtils.defaultString(rs.getString("JOSYA_1"));
                }
                final Student student = new Student(
                        schregno,
                        name,
                        kana,
                        birthday,
                        courseName,
                        majorName,
                        courseCodeName,
                        addr1,
                        addr2,
                        grade,
                        gradeName1,
                        gradeName2,
                        attendNo,
                        schoolKind,
                        gesyaALL,
                        hrName);
                rtnList.add(student);
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtnList;
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append("WITH SCHNO AS ( ");
        stb.append(" SELECT ");
        stb.append("    T1.YEAR, ");
        stb.append("    T1.SEMESTER, ");
        stb.append("    T1.GRADE, ");
        stb.append("    T1.HR_CLASS, ");
        stb.append("    T1.ATTENDNO, ");
        stb.append("    T1.SCHREGNO, ");
        stb.append("    T1.COURSECD, ");
        stb.append("    T1.MAJORCD, ");
        stb.append("    T1.COURSECODE, ");
        stb.append("    T2.NAME, ");
        stb.append("    T2.REAL_NAME, ");
        stb.append("    T2.NAME_KANA, ");
        stb.append("    T2.REAL_NAME_KANA, ");
        stb.append("    T2.BIRTHDAY ");
        stb.append(" FROM ");
        stb.append("    SCHREG_REGD_DAT T1 ");
        stb.append("    INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO=T1.SCHREGNO ");
        stb.append("    LEFT JOIN SCHREG_BASE_DETAIL_MST T3 ON T3.SCHREGNO=T1.SCHREGNO AND T3.BASE_SEQ = '003' ");
        stb.append(" WHERE ");
        stb.append("    T1.YEAR='" + _param._year + "' ");
        stb.append("    AND T1.SEMESTER='" + _param._semester + "' ");
        stb.append("    AND T1.SCHREGNO IN " + _param._inState + " ");
        stb.append(" ), SCHREG_ADDRESS_MAX AS ( ");
        stb.append("     SELECT ");
        stb.append("         SCHREGNO, ");
        stb.append("         MAX(ISSUEDATE) AS ISSUEDATE ");
        stb.append("     FROM ");
        stb.append("         SCHREG_ADDRESS_DAT ");
        stb.append("     GROUP BY ");
        stb.append("         SCHREGNO ");
        stb.append("     ) ");
        stb.append(" , SCHREG_ADDRESS AS ( ");
        stb.append("     SELECT DISTINCT ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T1.ZIPCD, ");
        stb.append("         P1.PREF_CD, ");
        stb.append("         P1.PREF_NAME, ");
        stb.append("         T1.AREACD, ");
        stb.append("         N1.NAME1 AS AREA_NAME, ");
        stb.append("         T1.ADDR1, ");
        stb.append("         T1.ADDR2, ");
        stb.append("         T1.TELNO ");
        stb.append("     FROM ");
        stb.append("         SCHREG_ADDRESS_DAT T1 ");
        stb.append("         INNER JOIN SCHREG_ADDRESS_MAX T2 ");
        stb.append("             ON  T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("             AND T2.ISSUEDATE = T1.ISSUEDATE ");
        stb.append("         LEFT JOIN ZIPCD_MST Z1 ON Z1.NEW_ZIPCD = T1.ZIPCD ");
        stb.append("         LEFT JOIN PREF_MST P1 ON P1.PREF_CD = SUBSTR(Z1.CITYCD,1,2) ");
        stb.append("         LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'A020' AND N1.NAMECD2 = T1.AREACD ");
        stb.append("     ) ");

        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO ");
        stb.append("     , T1.GRADE ");
        stb.append("     , T1.ATTENDNO ");
        stb.append("     , REGDG.SCHOOL_KIND ");
        stb.append("     , REGDG.GRADE_NAME1 ");
        stb.append("     , REGDG.GRADE_NAME2 ");
        stb.append("     , T3.HR_CLASS_NAME1 AS HR_CLASS_NAME1 ");
        stb.append("     , T1.NAME ");
        stb.append("     , T1.REAL_NAME ");
        stb.append("     , T1.NAME_KANA ");
        stb.append("     , T1.REAL_NAME_KANA ");
        stb.append("     , T1.NAME ");
        stb.append("     , T1.REAL_NAME ");
        stb.append("     , (CASE WHEN L4.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END) AS USE_REAL_NAME ");
        stb.append("     , T1.BIRTHDAY ");
        stb.append("     , L3.COURSENAME ");
        stb.append("     , L1.MAJORNAME ");
        stb.append("     , VALUE(COURSECODE.COURSECODEABBV1, '') AS COURSECODEABBV1 ");
        stb.append("     , CASE WHEN L5.FLG_1 = '1' THEN G1.STATION_NAME ELSE L5.GESYA_1 END AS JOSYA_1 ");
        stb.append("     , CASE WHEN L5.FLG_1 = '1' THEN G1.STATION_NAME ELSE L5.GESYA_1 END AS GESYA_1 ");
        stb.append("     , CASE WHEN L5.FLG_2 = '1' THEN G2.STATION_NAME ELSE L5.GESYA_2 END AS GESYA_2 ");
        stb.append("     , CASE WHEN L5.FLG_3 = '1' THEN G3.STATION_NAME ELSE L5.GESYA_3 END AS GESYA_3 ");
        stb.append("     , CASE WHEN L5.FLG_4 = '1' THEN G4.STATION_NAME ELSE L5.GESYA_4 END AS GESYA_4 ");
        stb.append("     , CASE WHEN L5.FLG_5 = '1' THEN G5.STATION_NAME ELSE L5.GESYA_5 END AS GESYA_5 ");
        stb.append("     , CASE WHEN L5.FLG_6 = '1' THEN G6.STATION_NAME ELSE L5.GESYA_6 END AS GESYA_6 ");
        stb.append("     , CASE WHEN L5.FLG_7 = '1' THEN G7.STATION_NAME ELSE L5.GESYA_7 END AS GESYA_7 ");
        stb.append("     , VALUE(ADDR.ADDR1, '') AS ADDR1 ");
        stb.append("     , VALUE(ADDR.ADDR2, '') AS ADDR2 ");
        stb.append(" FROM ");
        stb.append("     SCHNO T1 ");
        stb.append("     INNER JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR=T1.YEAR AND T3.SEMESTER=T1.SEMESTER AND T3.GRADE=T1.GRADE AND T3.HR_CLASS=T1.HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = T1.YEAR AND REGDG.GRADE = T1.GRADE ");
        stb.append("     LEFT JOIN SCHREG_ADDRESS ADDR ON ADDR.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN COURSE_MST L3 ON L3.COURSECD=T1.COURSECD ");
        stb.append("     LEFT JOIN MAJOR_MST L1 ON L1.COURSECD = T1.COURSECD AND L1.MAJORCD = T1.MAJORCD ");
        stb.append("     LEFT JOIN COURSECODE_MST COURSECODE ON COURSECODE.COURSECODE = T1.COURSECODE ");
        stb.append("     LEFT JOIN SCHREG_NAME_SETUP_DAT L4 ON L4.SCHREGNO = T1.SCHREGNO AND L4.DIV = '05' ");
        stb.append("     LEFT JOIN SCHREG_ENVIR_DAT L5 ON L5.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN STATION_NETMST G1 ON G1.STATION_CD = L5.GESYA_1 ");
        stb.append("     LEFT JOIN STATION_NETMST G2 ON G2.STATION_CD = L5.GESYA_2 ");
        stb.append("     LEFT JOIN STATION_NETMST G3 ON G3.STATION_CD = L5.GESYA_3 ");
        stb.append("     LEFT JOIN STATION_NETMST G4 ON G4.STATION_CD = L5.GESYA_4 ");
        stb.append("     LEFT JOIN STATION_NETMST G5 ON G5.STATION_CD = L5.GESYA_5 ");
        stb.append("     LEFT JOIN STATION_NETMST G6 ON G6.STATION_CD = L5.GESYA_6 ");
        stb.append("     LEFT JOIN STATION_NETMST G7 ON G7.STATION_CD = L5.GESYA_7 ");
        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO ");
        return stb.toString();
    }

    private class Student {
        final String _schregno;
        final String _name;
        final String _kana;
        final String _birthday;
        final String _courseName;
        final String _majorName;
        final String _courseCodeName;
        final String _addr1;
        final String _addr2;
        final String _grade;
        final String _gradeName1;
        final String _gradeName2;
        final String _attendNo;
        final String _schoolKind;
        final String _gesyaALL;
        final String _hrName;

        Student(final String schregno,
                final String name,
                final String kana,
                final String birthday,
                final String courseName,
                final String majorName,
                final String courseCodeName,
                final String addr1,
                final String addr2,
                final String grade,
                final String gradeName1,
                final String gradeName2,
                final String attendNo,
                final String schoolKind,
                final String gesyaALL,
                final String hrName
        ) {
            _schregno = schregno;
            _name = name;
            _kana = kana;
            _birthday = birthday;
            _courseName = courseName;
            _majorName = majorName;
            _courseCodeName = courseCodeName;
            _addr1 = addr1;
            _addr2 = addr2;
            _grade = grade;
            _gradeName1 = gradeName1;
            _gradeName2 = gradeName2;
            _attendNo = attendNo;
            _schoolKind = schoolKind;
            _gesyaALL = gesyaALL;
            _hrName = hrName;
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
        final String _year;
        final String _semester;
        final String _output; //1:A4 2:カード
        final boolean _printSchoolStamp;
        final boolean _printStation;
        final String _inState;
        final String _sDate;
        final String _eDate;
        final String _extensionStamp;
        private String _imagePass;
        private String _extensionPhoto;
        final String _documentroot;
        private boolean _isSeireki;
        private Map _certifSchoolDat = Collections.EMPTY_MAP;
        private String _z010SchoolCode;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _output = request.getParameter("OUTPUT");
            final String schoolStamp = request.getParameter("PRINT_SCHOOL_STAMP");
            _printSchoolStamp = "2".equals(_output) || "1".equals(schoolStamp);
            final String station = request.getParameter("PRINT_STATION");
            _printStation = "1".equals(station);

            final String[] category_selected = request.getParameterValues("category_selected");
            String sep = "";
            final StringBuffer stb = new StringBuffer();
            stb.append("(");
            for (int i = 0; i < category_selected.length; i++) {
                String rtnSt = "";
                rtnSt = "'" + StringUtils.split(category_selected[i], "-")[0] + "'";
                stb.append(sep).append(rtnSt);
                sep = ",";
            }
            stb.append(")");
            _inState = stb.toString();

            _sDate = request.getParameter("TERM_SDATE").replace('/', '-');
            _eDate = String.valueOf(Integer.parseInt(_year) + 1) + "-04-30";
            _documentroot = request.getParameter("DOCUMENTROOT");

            _extensionStamp = ".bmp";
            KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = null;

            //  写真データ
            try {
                returnval = getinfo.Control(db2);
                _imagePass = returnval.val4;        //格納フォルダ
                _extensionPhoto = returnval.val5;   //拡張子
            } catch( Exception e ){
                log.error("setHeader set error!");
            }

            setSeirekiFlg(db2);
            setSchoolInfo(db2);
            _z010SchoolCode = getSchoolCode(db2);
        }

        private void setSeirekiFlg(final DB2UDB db2) {
            try {
                _isSeireki = false;
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    final String str = rs.getString("NAME1");
                    if ("2".equals(str)) _isSeireki = true; //西暦
                }
                ps.close();
                rs.close();
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
            }
        }

        private String printDate(final String date) {
            if (null == date) {
                return "";
            }
            if (_isSeireki) {
                return date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date);
            } else {
                return KNJ_EditDate.h_format_JP(date);
            }
        }

        private String printDateFormat(final String date) {
            if (_isSeireki) {
                final String wdate = (null == date) ? date : date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date);
                return KNJ_EditDate.setDateFormat2(wdate);
            } else {
                final String wdate = (null == date) ? date : KNJ_EditDate.h_format_JP(date);
                return KNJ_EditDate.setDateFormat(wdate, _year);
            }
        }

        private void setSchoolInfo(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _certifSchoolDat = new HashMap();
            try {
                String sql = "SELECT CERTIF_KINDCD, SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK1, REMARK3 " +
                             "FROM CERTIF_SCHOOL_DAT " +
                             "WHERE YEAR = '" + _year + "' AND (CERTIF_KINDCD = '101' OR CERTIF_KINDCD = '102' OR CERTIF_KINDCD = '140') ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Map certifKindMap = getMappedMap(_certifSchoolDat, rs.getString("CERTIF_KINDCD"));
                    certifKindMap.put("SCHOOL_NAME", null != rs.getString("SCHOOL_NAME") ? rs.getString("SCHOOL_NAME") : "");
                    certifKindMap.put("JOB_NAME", null != rs.getString("JOB_NAME") ? rs.getString("JOB_NAME") : "");
                    certifKindMap.put("PRINCIPAL_NAME", null != rs.getString("PRINCIPAL_NAME") ? rs.getString("PRINCIPAL_NAME") : "");
                    certifKindMap.put("REMARK1", null != rs.getString("REMARK1") ? rs.getString("REMARK1") : "");
                    certifKindMap.put("REMARK3", null != rs.getString("REMARK3") ? rs.getString("REMARK3") : "");
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        
        private String getSchoolCode(DB2UDB db2) {
            String schoolCode = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("   CASE WHEN NAME1 = 'CHIBEN' THEN NAME2 ELSE NULL END AS SCHOOLCODE ");
                stb.append(" FROM ");
                stb.append("   NAME_MST T1 ");
                stb.append(" WHERE ");
                stb.append("   T1.NAMECD1 = 'Z010' ");
                stb.append("   AND T1.NAMECD2 = '00' ");
                
                PreparedStatement ps = db2.prepareStatement(stb.toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    schoolCode = rs.getString("SCHOOLCODE");
                }
                ps.close();
                rs.close();
            } catch (Exception e) {
                log.error("getSchoolCode Exception", e);
            }
            return schoolCode;
        }

        boolean isCollege() {
            return "30290086001".equals(_z010SchoolCode);
        }
    }
}

// eof
