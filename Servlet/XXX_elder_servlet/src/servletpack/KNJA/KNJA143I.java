// kanji=漢字
/*
 * $Id: 0abc39f46827d918a86fbb7131e543b4c888e70b $
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
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_Get_Info;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: 0abc39f46827d918a86fbb7131e543b4c888e70b $
 */
public class KNJA143I {

    private static final Log log = LogFactory.getLog("KNJA143I.class");

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
        return KNJ_EditEdit.getMS932ByteLength(str);
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

        final List pageList = getPageList(printStudents, 4);
        for (final Iterator pit = pageList.iterator(); pit.hasNext();) {
            final List studentList = (List) pit.next();

            final String form = _param._useFormFlg ? _param._useFormName + ".frm" : "KNJA143I.frm";
            svf.VrSetForm(form, 1);

            for (int i = 0; i < studentList.size(); i++) {
                final Student student = (Student) studentList.get(i);
                final int line = i + 1;

                if(!_param._useFormFlg) {

                    svf.VrsOutn("SCHREGNO", line, student._schregno); // 学籍番号
                    svf.VrsOutn("COURSE", line, student._majorName); // 所属
                    final int nameKeta = getMS932ByteLength(student._name);
                    svf.VrsOutn("NAME" + (nameKeta > 20 ? "3" : nameKeta > 14 ? "2" : "1"), line, student._name); // 生徒氏名
                    final int gesyaKeta = getMS932ByteLength(student._gesyaALL);
                    svf.VrsOutn("SECTION" + (gesyaKeta > 30 ? "_2" : ""), line, student._gesyaALL); // 通学区間
//                    svf.VrsOutn("SCHREGNO2", line, student._schregno.substring(Math.max(0, student._schregno.length() - 4), student._schregno.length())); // 学籍番号下4桁
                    svf.VrsOutn("SCHREGNO2", line, student._examno); // 受験番号
                    printDate(db2, svf, "BIRTHDAY", line, student._birthday); // 生年月日
                    printDate(db2, svf, "LDATE", line, _param._eDate);
                    printDate(db2, svf, "SDATE", line, _param._sDate);

                    String certifSchoolCd = "";
                    if ("H".equals(student._schoolKind)) {
                        certifSchoolCd = "101";
                    } else if ("J".equals(student._schoolKind)) {
                        certifSchoolCd = "102";
                    }

                    final Map certifSchoolDat = getMappedMap(_param._certifSchoolDat, certifSchoolCd);
                    svf.VrsOutn("JOBNAME", line, (String) certifSchoolDat.get("JOB_NAME")); // 役職・氏名
                    svf.VrsOutn("STAFFNAME", line, (String) certifSchoolDat.get("PRINCIPAL_NAME")); // 役職・氏名

                }else {

                    //生徒情報
                    svf.VrsOutn("TITLE", line, "生徒証"); // タイトル
                    // 顔写真
                    final String photoPath = _param._documentroot + "/" + _param._ctrlMstImageDir + "/" + "P" + student._schregno + "." + _param._ctrlMstExtension;
                    if (new File(photoPath).exists()) {
                        svf.VrsOutn("PHOTO_BMP", line, photoPath);//顔写真
                    }
                    svf.VrsOutn("SCHREGNO", line, student._schregno); // 学籍番号
                    svf.VrsOutn("HR_NAME", line, student._hrName); // 学年
                    final String nameField = getMS932ByteLength(student._name) > 24 ? "3" : getMS932ByteLength(student._name) > 18 ? "2" : "";
                    svf.VrsOutn("NAME" + nameField, line, student._name); // 生徒氏名
                    svf.VrsOutn("AGE", line, student._age); // 年齢
                    final String birthDay = KNJ_EditDate.h_format_JP_Bth(db2, student._birthday);
                    svf.VrsOutn("BIRTHDAY", line, birthDay);  // 生年月日
                    final String addr1Field = getMS932ByteLength(student._addr1) > 50 ? "_4" : getMS932ByteLength(student._addr1) > 40 ? "_3" : getMS932ByteLength(student._addr1) > 30 ? "_2" : "_1" ;
                    final String addr2Field = getMS932ByteLength(student._addr2) > 50 ? "_4" : getMS932ByteLength(student._addr2) > 40 ? "_3" : getMS932ByteLength(student._addr2) > 30 ? "_2" : "_1" ;
                    svf.VrsOutn("ADDRESS1" + addr1Field, line, student._addr1); // 住所1
                    svf.VrsOutn("ADDRESS2" + addr2Field, line, student._addr2); // 住所2
                    final String[] sDate = KNJ_EditDate.tate_format4(db2, _param._sDate);

                    svf.VrsOutn("SDATE", line, sDate[0] + sDate[1] + "年" + sDate[2] + "月" + sDate[3] + "日" ); // 発行日

                    //通学区間
                    final String[] fDate = KNJ_EditDate.tate_format4(db2, _param._eDate);
                    svf.VrsOutn("FDATE", line, fDate[0] + fDate[1] + "年" + fDate[2] + "月" + fDate[3] + "日" ); // 有効期限日
                    final String[] gesya = StringUtils.split(student._gesyaALL,"-");
                    final String[] josya = StringUtils.split(student._josyaALL,"-");
                    if (null != gesya) {
                        switch (gesya.length) {
                        case 7:
                        case 6:
                            setSectionDatt(svf, "SECTION6_1", line, josya[5]); // 通学区間6 From
                            setSectionDatt(svf, "SECTION6_2", line, gesya[5]); // 通学区間6 To
                        case 5:
                            setSectionDatt(svf, "SECTION5_1", line, josya[4]); // 通学区間5 From
                            setSectionDatt(svf, "SECTION5_2", line, gesya[4]); // 通学区間5 To
                        case 4:
                            setSectionDatt(svf, "SECTION4_1", line, josya[3]); // 通学区間4 From
                            setSectionDatt(svf, "SECTION4_2", line, gesya[3]); // 通学区間4 To
                        case 3:
                            setSectionDatt(svf, "SECTION3_1", line, josya[2]); // 通学区間3 From
                            setSectionDatt(svf, "SECTION3_2", line, gesya[2]); // 通学区間3 To
                        case 2:
                            setSectionDatt(svf, "SECTION2_1", line, josya[1]); // 通学区間2 From
                            setSectionDatt(svf, "SECTION2_2", line, gesya[1]); // 通学区間2 To
                        case 1:
                            setSectionDatt(svf, "SECTION1_1", line, josya[0]); // 通学区間1 From
                            setSectionDatt(svf, "SECTION1_2", line, gesya[0]); // 通学区間1 To
                        }
                    }
                    String certifSchoolCd = "";
                    if ("H".equals(student._schoolKind)) {
                        certifSchoolCd = "101";
                        svf.VrsOutn("SENTENCE1", line, "高等学校"); // 証明文言
                    } else if ("J".equals(student._schoolKind)) {
                        certifSchoolCd = "102";
                        svf.VrsOutn("SENTENCE1", line, "中学校"); // 証明文言
                    }

                    //学校情報
                    final Map certifSchoolDat = getMappedMap(_param._certifSchoolDat, certifSchoolCd);
                    svf.VrsOutn("JOBNAME", line, (String) certifSchoolDat.get("JOB_NAME")); // 役職・氏名
                    svf.VrsOutn("STAFFNAME", line, (String) certifSchoolDat.get("PRINCIPAL_NAME")); // 役職・氏名
                    final String schoolAddr = (String) certifSchoolDat.get("REMARK1");
                    final String schoolAddrField = getMS932ByteLength(schoolAddr) > 50 ? "4" : getMS932ByteLength(schoolAddr) > 40 ? "3" : getMS932ByteLength(schoolAddr) > 34 ? "2" : "";
                    svf.VrsOutn("SCHOOLADDRESS" + schoolAddrField, line, schoolAddr); // 学校所在地
                    svf.VrsOutn("SCHOOLNAME", line, (String) certifSchoolDat.get("SCHOOL_NAME")); // 学校名
                }

                _hasData = true;
            }

            svf.VrEndPage();
        }
    }

    public void printDate(final DB2UDB db2, final Vrw32alp svf, final String head, final int line, final String date) {
        if (null == date) {
            return;
        }
        if (_param._isBunkyo) {
        	// 西暦出力
        	final String[] splited = StringUtils.split(date, StringUtils.split(date, "-").length == 3 ? "-" : "/");
        	if (splited.length == 3) {
        		svf.VrsOutn(head + "2", line, String.valueOf(Integer.parseInt(splited[0]))); // 発行日
        		svf.VrsOutn(head + "3", line, String.valueOf(Integer.parseInt(splited[1]))); // 発行日
        		svf.VrsOutn(head + "4", line, String.valueOf(Integer.parseInt(splited[2]))); // 発行日
        	}
        } else {
            final String[] splited = KNJ_EditDate.tate_format4(db2, date);
            if (splited.length == 4) {
                //log.debug(" date = " + ArrayUtils.toString(splited));
                svf.VrsOutn(head + "1", line, splited[0]); // 発行日
                svf.VrsOutn(head + "2", line, splited[1]); // 発行日
                svf.VrsOutn(head + "3", line, splited[2]); // 発行日
                svf.VrsOutn(head + "4", line, splited[3]); // 発行日
            }
        }
    }

    public void setSectionDatt(final Vrw32alp svf, final String field, final int line, final String section) {
        final String sectionField = getMS932ByteLength(section) > 16 ? "_3" : getMS932ByteLength(section) > 10 ? "_2" : "_1";
        svf.VrsOutn(field + sectionField, line, section); // 通学区間
    }

    private String getGesya(final String gesyaALL, final String gesya) {
        if (gesya == null) return gesyaALL;
        if (gesyaALL == null) return gesya;
        return gesyaALL + "-" + gesya;
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
                final String birthday = rs.getString("BIRTHDAY");
                final String examno = rs.getString("EXAMNO");
                final String courseName = rs.getString("COURSENAME");
                final String majorName = rs.getString("MAJORNAME");
//                final String addr1 = rs.getString("ADDR1");
//                final String addr2 = rs.getString("ADDR2");
                final String grade = rs.getString("GRADE");
                final String schoolKind = rs.getString("SCHOOL_KIND");
                final String hrName = rs.getString("HR_CLASS_NAME1");
                String gesyaALL = null;
                if(!_param._useFormFlg) {
                    gesyaALL = getGesya(gesyaALL, rs.getString("GESYA_7"));
                    gesyaALL = getGesya(gesyaALL, rs.getString("GESYA_6"));
                    gesyaALL = getGesya(gesyaALL, rs.getString("GESYA_5"));
                    gesyaALL = getGesya(gesyaALL, rs.getString("GESYA_4"));
                    gesyaALL = getGesya(gesyaALL, rs.getString("GESYA_3"));
                    gesyaALL = getGesya(gesyaALL, rs.getString("GESYA_2"));
                    gesyaALL = getGesya(gesyaALL, rs.getString("GESYA_1"));
                }else {
                    gesyaALL = getGesya(gesyaALL, rs.getString("GESYA_1"));
                    gesyaALL = getGesya(gesyaALL, rs.getString("GESYA_2"));
                    gesyaALL = getGesya(gesyaALL, rs.getString("GESYA_3"));
                    gesyaALL = getGesya(gesyaALL, rs.getString("GESYA_4"));
                    gesyaALL = getGesya(gesyaALL, rs.getString("GESYA_5"));
                    gesyaALL = getGesya(gesyaALL, rs.getString("GESYA_6"));
                    gesyaALL = getGesya(gesyaALL, rs.getString("GESYA_7"));
                }
                final String age = rs.getString("AGE");
                final String addr1 = StringUtils.defaultString(rs.getString("ADDR1"));
                final String addr2 = StringUtils.defaultString(rs.getString("ADDR2"));
                String josyaALL = null;
                josyaALL = getGesya(josyaALL, rs.getString("JOSYA_1"));
                josyaALL = getGesya(josyaALL, rs.getString("JOSYA_2"));
                josyaALL = getGesya(josyaALL, rs.getString("JOSYA_3"));
                josyaALL = getGesya(josyaALL, rs.getString("JOSYA_4"));
                josyaALL = getGesya(josyaALL, rs.getString("JOSYA_5"));
                josyaALL = getGesya(josyaALL, rs.getString("JOSYA_6"));
                josyaALL = getGesya(josyaALL, rs.getString("JOSYA_7"));
                final Student student = new Student(
                        schregno,
                        name,
                        birthday,
                        examno,
                        courseName,
                        majorName,
//                        addr1,
//                        addr2,
                        grade,
                        schoolKind,
                        gesyaALL,
                        hrName,
                        age,
                        addr1,
                        addr2,
                        josyaALL);
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
        if ("1".equals(_param._output)) {
            stb.append("    T20.EXAMNO, ");
            stb.append("    VALUE(T2.NAME, T20.NAME) AS NAME, ");
            stb.append("    CAST(NULL AS CHAR(1)) AS REAL_NAME, ");
            stb.append("    VALUE(T2.BIRTHDAY, T20.BIRTHDAY) AS BIRTHDAY ");
        } else {
            stb.append("    T3.BASE_REMARK1 AS EXAMNO, ");
            stb.append("    T2.NAME, ");
            stb.append("    T2.REAL_NAME, ");
            stb.append("    T2.BIRTHDAY ");
        }
        stb.append(" FROM ");
        if ("1".equals(_param._output)) {
            stb.append("    CLASS_FORMATION_DAT T1 ");
            stb.append("    LEFT JOIN FRESHMAN_DAT T20 ON T20.ENTERYEAR = T1.YEAR AND T20.SCHREGNO=T1.SCHREGNO ");
            stb.append("    LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO=T1.SCHREGNO ");
        } else {
            stb.append("    SCHREG_REGD_DAT T1 ");
            stb.append("    INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO=T1.SCHREGNO ");
            stb.append("    LEFT JOIN SCHREG_BASE_DETAIL_MST T3 ON T3.SCHREGNO=T1.SCHREGNO AND T3.BASE_SEQ = '003' ");
        }
        stb.append(" WHERE ");
        stb.append("    T1.YEAR='" + _param._year + "' ");
        stb.append("    AND T1.SEMESTER='" + _param._semester + "' ");
        if ("1".equals(_param._disp)) {
            stb.append("    AND T1.SCHREGNO IN " + _param._inState + " ");
        }
        if ("2".equals(_param._disp)) {
            stb.append("    AND T1.GRADE || T1.HR_CLASS IN " + _param._inState + " ");
        }
        stb.append(" ) ");
        if(_param._useFormFlg) {
            stb.append(" , SCHREG_ADDRESS_MAX AS ( ");
            stb.append(" SELECT ");
            stb.append("     SCHREGNO, ");
            stb.append("     MAX(ISSUEDATE) AS ISSUEDATE ");
            stb.append(" FROM ");
            stb.append("     SCHREG_ADDRESS_DAT ");
            stb.append(" WHERE ");
            stb.append("     SCHREGNO IN (SELECT SCHREGNO FROM SCHNO) ");
            stb.append(" GROUP BY ");
            stb.append("     SCHREGNO ");
            stb.append("     ) ");
            stb.append(" , SCHREG_ADDRESS AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.ISSUEDATE, ");
            stb.append("     T1.ADDR1, ");
            stb.append("     T1.ADDR2 ");
            stb.append(" FROM ");
            stb.append("     SCHREG_ADDRESS_DAT T1, ");
            stb.append("     SCHREG_ADDRESS_MAX T2 ");
            stb.append(" WHERE ");
            stb.append("     T2.SCHREGNO = T1.SCHREGNO AND ");
            stb.append("     T2.ISSUEDATE = T1.ISSUEDATE ");
            stb.append("     ) ");
        }

        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO ");
        stb.append("     , T1.GRADE ");
        stb.append("     , REGDG.SCHOOL_KIND ");
        stb.append("     , T1.NAME ");
        stb.append("     , T1.REAL_NAME ");
        stb.append("     , (CASE WHEN L4.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END) AS USE_REAL_NAME ");
        stb.append("     , T1.BIRTHDAY ");
        stb.append("     , T1.EXAMNO ");
        stb.append("     , L3.COURSENAME ");
        stb.append("     , L1.MAJORNAME ");
        stb.append("     , CASE WHEN L5.FLG_1 = '1' THEN G1.STATION_NAME ELSE L5.GESYA_1 END AS GESYA_1 ");
        stb.append("     , CASE WHEN L5.FLG_2 = '1' THEN G2.STATION_NAME ELSE L5.GESYA_2 END AS GESYA_2 ");
        stb.append("     , CASE WHEN L5.FLG_3 = '1' THEN G3.STATION_NAME ELSE L5.GESYA_3 END AS GESYA_3 ");
        stb.append("     , CASE WHEN L5.FLG_4 = '1' THEN G4.STATION_NAME ELSE L5.GESYA_4 END AS GESYA_4 ");
        stb.append("     , CASE WHEN L5.FLG_5 = '1' THEN G5.STATION_NAME ELSE L5.GESYA_5 END AS GESYA_5 ");
        stb.append("     , CASE WHEN L5.FLG_6 = '1' THEN G6.STATION_NAME ELSE L5.GESYA_6 END AS GESYA_6 ");
        stb.append("     , CASE WHEN L5.FLG_7 = '1' THEN G7.STATION_NAME ELSE L5.GESYA_7 END AS GESYA_7 ");
//        stb.append("     , L2.ADDR1 ");
//        stb.append("     , L2.ADDR2 ");
        if(_param._useFormFlg) {
            stb.append("     , T3.HR_NAME AS HR_CLASS_NAME1 ");
            stb.append("     , CASE WHEN T1.BIRTHDAY IS NOT NULL THEN YEAR('" + _param._sDate + "' - T1.BIRTHDAY) END AS AGE ");
            stb.append("     , L2.ADDR1 ");
            stb.append("     , L2.ADDR2 ");
            stb.append("     , CASE WHEN L5.FLG_1 = '1' THEN J1.STATION_NAME ELSE L5.JOSYA_1 END AS JOSYA_1 ");
            stb.append("     , CASE WHEN L5.FLG_2 = '1' THEN J2.STATION_NAME ELSE L5.JOSYA_2 END AS JOSYA_2 ");
            stb.append("     , CASE WHEN L5.FLG_3 = '1' THEN J3.STATION_NAME ELSE L5.JOSYA_3 END AS JOSYA_3 ");
            stb.append("     , CASE WHEN L5.FLG_4 = '1' THEN J4.STATION_NAME ELSE L5.JOSYA_4 END AS JOSYA_4 ");
            stb.append("     , CASE WHEN L5.FLG_5 = '1' THEN J5.STATION_NAME ELSE L5.JOSYA_5 END AS JOSYA_5 ");
            stb.append("     , CASE WHEN L5.FLG_6 = '1' THEN J6.STATION_NAME ELSE L5.JOSYA_6 END AS JOSYA_6 ");
            stb.append("     , CASE WHEN L5.FLG_7 = '1' THEN J7.STATION_NAME ELSE L5.JOSYA_7 END AS JOSYA_7 ");
        }else {
            stb.append("     , T3.HR_CLASS_NAME1 ");
            stb.append("     , '' AS AGE ");
            stb.append("     , '' AS ADDR1 ");
            stb.append("     , '' AS ADDR2 ");
            stb.append("     , '' AS JOSYA_1 ");
            stb.append("     , '' AS JOSYA_2 ");
            stb.append("     , '' AS JOSYA_3 ");
            stb.append("     , '' AS JOSYA_4 ");
            stb.append("     , '' AS JOSYA_5 ");
            stb.append("     , '' AS JOSYA_6 ");
            stb.append("     , '' AS JOSYA_7 ");
        }
        stb.append(" FROM ");
        stb.append("     SCHNO T1 ");
        stb.append("     INNER JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR=T1.YEAR AND T3.SEMESTER=T1.SEMESTER AND T3.GRADE=T1.GRADE AND T3.HR_CLASS=T1.HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = T1.YEAR AND REGDG.GRADE = T1.GRADE ");
        if(_param._useFormFlg) stb.append("     LEFT JOIN SCHREG_ADDRESS L2 ON L2.SCHREGNO=T1.SCHREGNO ");
        stb.append("     LEFT JOIN COURSE_MST L3 ON L3.COURSECD=T1.COURSECD ");
        stb.append("     LEFT JOIN MAJOR_MST L1 ON L1.COURSECD = T1.COURSECD AND L1.MAJORCD = T1.MAJORCD ");
        stb.append("     LEFT JOIN SCHREG_NAME_SETUP_DAT L4 ON L4.SCHREGNO = T1.SCHREGNO AND L4.DIV = '05' ");
        stb.append("     LEFT JOIN SCHREG_ENVIR_DAT L5 ON L5.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN STATION_NETMST G1 ON G1.STATION_CD = L5.GESYA_1 ");
        stb.append("     LEFT JOIN STATION_NETMST G2 ON G2.STATION_CD = L5.GESYA_2 ");
        stb.append("     LEFT JOIN STATION_NETMST G3 ON G3.STATION_CD = L5.GESYA_3 ");
        stb.append("     LEFT JOIN STATION_NETMST G4 ON G4.STATION_CD = L5.GESYA_4 ");
        stb.append("     LEFT JOIN STATION_NETMST G5 ON G5.STATION_CD = L5.GESYA_5 ");
        stb.append("     LEFT JOIN STATION_NETMST G6 ON G6.STATION_CD = L5.GESYA_6 ");
        stb.append("     LEFT JOIN STATION_NETMST G7 ON G7.STATION_CD = L5.GESYA_7 ");
        stb.append("     LEFT JOIN STATION_NETMST J1 ON J1.STATION_CD = L5.JOSYA_1 ");
        stb.append("     LEFT JOIN STATION_NETMST J2 ON J2.STATION_CD = L5.JOSYA_2 ");
        stb.append("     LEFT JOIN STATION_NETMST J3 ON J3.STATION_CD = L5.JOSYA_3 ");
        stb.append("     LEFT JOIN STATION_NETMST J4 ON J4.STATION_CD = L5.JOSYA_4 ");
        stb.append("     LEFT JOIN STATION_NETMST J5 ON J5.STATION_CD = L5.JOSYA_5 ");
        stb.append("     LEFT JOIN STATION_NETMST J6 ON J6.STATION_CD = L5.JOSYA_6 ");
        stb.append("     LEFT JOIN STATION_NETMST J7 ON J7.STATION_CD = L5.JOSYA_7 ");
        stb.append(" ORDER BY ");
        if ("1".equals(_param._sort)) { // 年組番
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.ATTENDNO ");
        } else { // 学籍番号（受験番号）
            stb.append("     T1.SCHREGNO ");
        }
        return stb.toString();
    }

    private class Student {
        final String _schregno;
        final String _name;
        final String _birthday;
        final String _examno;
        final String _courseName;
        final String _majorName;
//        final String _addr1;
//        final String _addr2;
        final String _grade;
        final String _schoolKind;
        final String _gesyaALL;
        final String _hrName;
        final String _age;
        final String _addr1;
        final String _addr2;
        final String _josyaALL;

        Student(final String schregno,
                final String name,
                final String birthday,
                final String examno,
                final String courseName,
                final String majorName,
//                final String addr1,
//                final String addr2,
                final String grade,
                final String schoolKind,
                final String gesyaALL,
                final String hrName,
                final String age,
                final String addr1,
                final String addr2,
                final String josyaALL
        ) {
            _schregno = schregno;
            _name = name;
            _birthday = birthday;
            _examno = examno;
            _courseName = courseName;
            _majorName = majorName;
//            _addr1 = addr1;
//            _addr2 = addr2;
            _grade = grade;
            _schoolKind = schoolKind;
            _gesyaALL = gesyaALL;
            _hrName = hrName;
            _age = age;
            _addr1 = addr1;
            _addr2 = addr2;
            _josyaALL = josyaALL;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 66747 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _year;
        final String _semester;
        final String _output; // 1:新入生 2:在籍
        final String _disp; // 1:個人,2:クラス
        final String _sort; // 1:年組番,2:学籍番号（受験番号）
        final String _inState;
        final String _sDate;
        final String _eDate;
        private boolean _isSeireki;
        private boolean _isBunkyo;
        private Map _certifSchoolDat = Collections.EMPTY_MAP;
        private final String _useFormName;
        private final boolean _useFormFlg;
        final String _documentroot;
	    private String _ctrlMstImageDir;
	    private String _ctrlMstExtension;

//        final String _useAddrField2;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _output = request.getParameter("OUTPUT");
            _disp = request.getParameter("DISP");
            _sort = request.getParameter("SORT_DIV");

            final String[] category_selected = request.getParameterValues("category_selected");
            String sep = "";
            final StringBuffer stb = new StringBuffer();
            stb.append("(");
            for (int i = 0; i < category_selected.length; i++) {
                String rtnSt = "";
                if ("2".equals(_disp)) {
                    rtnSt = "'" + category_selected[i] + "'";
                } else if ("1".equals(_disp)) {
                    rtnSt = "'" + StringUtils.split(category_selected[i], "-")[0] + "'";
                }
                stb.append(sep).append(rtnSt);
                sep = ",";
            }
            stb.append(")");
            _inState = stb.toString();

            _sDate = request.getParameter("TERM_SDATE").replace('/', '-');
            _eDate = request.getParameter("TERM_EDATE").replace('/', '-');

            setSeirekiFlg(db2);
            setSchoolInfo(db2);
            final String z010Name1 = getZ010Name1(db2);
            _isBunkyo = "bunkyo".equals(z010Name1);
//            _useAddrField2 = request.getParameter("useAddrField2");
            _useFormName = StringUtils.defaultString(request.getParameter("useFormNameA143I"));
            _useFormFlg = !"".equals(_useFormName) ? true : false;
            _documentroot = request.getParameter("DOCUMENTROOT");

            KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = null;

            //  写真データ
            try {
                returnval = getinfo.Control(db2);
                _ctrlMstImageDir = returnval.val4;      //格納フォルダ
                _ctrlMstExtension = returnval.val5;      //拡張子
            } catch (Exception e) {
                log.error("setHeader set error!", e);
            }

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

        private String getZ010Name1(final DB2UDB db2) {
        	String z010Name1 = null;
            try {
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    z010Name1 = rs.getString("NAME1");
                }
                ps.close();
                rs.close();
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
            }
            return z010Name1;
        }

        private String printDate(final DB2UDB db2, final String date) {
            if (null == date) {
                return "";
            }
            if (_isSeireki) {
                return date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date);
            } else {
                return KNJ_EditDate.h_format_JP(db2, date);
            }
        }

        private String printDateFormat(final DB2UDB db2, final String date) {
            if (_isSeireki) {
                final String wdate = (null == date) ? date : date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date);
                return KNJ_EditDate.setDateFormat2(wdate);
            } else {
                final String wdate = (null == date) ? date : KNJ_EditDate.h_format_JP(db2, date);
                return KNJ_EditDate.setDateFormat(db2, wdate, _year);
            }
        }

        private void setSchoolInfo(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _certifSchoolDat = new HashMap();
            try {
                String sql = "SELECT CERTIF_KINDCD, SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK1 " +
                             "FROM CERTIF_SCHOOL_DAT " +
                             "WHERE YEAR = '" + _year + "' AND (CERTIF_KINDCD = '101' OR CERTIF_KINDCD = '102') ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Map certifKindMap = getMappedMap(_certifSchoolDat, rs.getString("CERTIF_KINDCD"));
                    certifKindMap.put("SCHOOL_NAME", rs.getString("SCHOOL_NAME"));
                    certifKindMap.put("JOB_NAME", rs.getString("JOB_NAME"));
                    certifKindMap.put("PRINCIPAL_NAME", rs.getString("PRINCIPAL_NAME"));
                    certifKindMap.put("REMARK1", rs.getString("REMARK1"));
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
    }
}

// eof
