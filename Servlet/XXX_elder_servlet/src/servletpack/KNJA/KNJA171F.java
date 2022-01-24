package servletpack.KNJA;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.CsvUtils;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;


/**
 * 学籍簿
 */
public class KNJA171F {

    private static final Log log = LogFactory.getLog(KNJA171F.class);
    private boolean _hasData;
    private Param _param;

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        DB2UDB db2 = null;
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

        } catch (final Exception e) {
            log.error("Exception:", e);
            return;
        }

        _param = createParam(db2, request);

        Vrw32alp svf = null;
        try {
            if ("csv".equals(_param._cmd)) {
                final List outputLines = new ArrayList();
                String title = "";
                for (int i = 0; i < _param._classSelected.length; i++) {
                    // 生徒データを取得
                    final List studentList = createStudentInfoData(db2, _param, _param._classSelected[i]);
                    if (studentList.size() == 0) {
                        continue;
                    }

                    final Student student0 = (Student) studentList.get(0);

                    title = getTitle(student0._schoolKind, StringUtils.defaultString(student0._hrName), 1);

                    final List staffList = getStaffList(db2, _param._classSelected[i]);
                    if (csvMain(outputLines, studentList, staffList)) { // 生徒出力のメソッド
                        _hasData = true;
                    }
                }
                if (!outputLines.isEmpty()) {
                    final Map csvParam = new HashMap();
                    csvParam.put("HttpServletRequest", request);
                    CsvUtils.outputLines(log, response, title + ".csv", outputLines, csvParam);
                }

            } else {
                svf = new Vrw32alp();

                if (svf.VrInit() < 0) {
                    throw new IllegalStateException("svf初期化失敗");
                }
                svf.VrSetSpoolFileStream(response.getOutputStream());
                response.setContentType("application/pdf");

                for (int i = 0; i < _param._classSelected.length; i++) {
                    // 生徒データを取得
                    final List studentList = createStudentInfoData(db2, _param, _param._classSelected[i]);
                    if (studentList.size() == 0) {
                        continue;
                    }
                    final List staffList = getStaffList(db2, _param._classSelected[i]);
                    if (printMain(svf, studentList, staffList)) { // 生徒出力のメソッド
                        _hasData = true;
                    }
                }
            }

        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != svf) {
                if (!_hasData) {
                    svf.VrSetForm("MES001.frm", 0);
                    svf.VrsOut("note", "note");
                    svf.VrEndPage();
                }

                final int ret = svf.VrQuit();
                log.info("===> VrQuit():" + ret);
            }
            if (null != db2) {
                db2.commit();
                db2.close();
            }
        }
    }

    private List newLine(final List outputList) {
        List line = new ArrayList();
        outputList.add(line);
        return line;
    }

    private boolean csvMain(final List outputList, final List studentList, final List staffList) throws Exception {
        boolean hasData = false;

        final Student student0 = (Student) studentList.get(0);

        final String title = getTitle(student0._schoolKind, StringUtils.defaultString(student0._hrName), 0);

        newLine(outputList).addAll(Arrays.asList(new String[] {null, null, null, title}));

        final List headerLine = new ArrayList();
        if ("1".equals(_param._printRadio)) {
            headerLine.add("学籍番号");
            headerLine.add("番号");
            headerLine.add("氏名");
            headerLine.add("ふりがな");
            if ("H".equals(student0._schoolKind)) {
                headerLine.add("健・自"); // ハンディキャップ
            }
            headerLine.add("性別");
            headerLine.add("保護者名");
            headerLine.add("保護者ふりがな");
            headerLine.add("郵便番号");
            headerLine.add("住所");
            headerLine.add("電話番号");
            headerLine.add("生年月日");
            if ("K".equals(student0._schoolKind)) {
                headerLine.add("バスコース");
                headerLine.add("バス停");
            }
            headerLine.add("備考(転出年月日)");
        } else {
            headerLine.add("番号");
            headerLine.add("氏名");
            headerLine.add("ふりがな");
            headerLine.add("性別");
            headerLine.add("郵便番号");
            headerLine.add("住所");
            headerLine.add("電話番号");
        }

        for (int i = 0; i < staffList.size(); i++) {
            final Map m = (Map) staffList.get(i);
            final String chargediv = (String) m.get("CHARGEDIV");
            final String jobname = "1".equals(chargediv) ? "担任" : !("musashinohigashi".equals(_param._z010Name) && "0".equals(chargediv)) ? "副担任" : null;
            final String staffname = (String) m.get("STAFFNAME");
            final List staffLine = newLine(outputList);
            for (int j = 0; j < headerLine.size() - 2; j++) {
                staffLine.add(null);
            }
            staffLine.addAll(Arrays.asList(new String[] {jobname, staffname}));
        }

        final String setClassType = "2".equals(_param._printRadio) ? "" : "1".equals(_param._useFi_Hrclass) ? ("1".equals(_param._printClass) ? "(法定クラス)" : "2".equals(_param._printClass) ? "(複式クラス)" : "") : "";
        newLine(outputList).addAll(Arrays.asList(new String[] {student0._hrName + setClassType}));

        outputList.add(headerLine);

        for (int i = 0; i < studentList.size(); i++) {
            final Student student = (Student) studentList.get(i);
            csvStudent(outputList, student);
            hasData = true;
        }

        newLine(outputList);
        newLine(outputList);

        return  hasData;
    }

    private boolean printMain(final Vrw32alp svf, final List studentList, final List staffList) throws Exception {
        boolean hasData = false;

        final Student student0 = (Student) studentList.get(0);

        final String title = getTitle(student0._schoolKind, StringUtils.defaultString(student0._hrName), 0);

        final String form;
        int staffMax = 999;
        final int count;
        if ("1".equals(_param._printRadio)) {
            if ("H".equals(student0._schoolKind)) {
                form = "KNJA171F_3.frm";
            } else if ("K".equals(student0._schoolKind)) {
                form = "KNJA171F_4.frm";
            } else {
                form = "KNJA171F_2.frm";
            }
            count = 30;
        } else {
            form = "KNJA171F.frm";
            count = 40;
            if ("K".equals(student0._schoolKind)) {
            } else {
//                staffMax = 1;
            }
        }

        for (final Iterator it = getPageList(studentList, count).iterator(); it.hasNext();) {
            final List pageStudentList = (List) it.next();

            svf.VrSetForm(form, 1);

            svf.VrsOut("TITLE", title);
            svf.VrsOut("YMD", _param._ctrlDateFormat);
            svf.VrsOut("STAFF_NAME1", student0._trname1);
            svf.VrsOut("STAFF_NAME2", student0._subtrname1);
            svf.VrsOut("STAFF_NAME3", student0._subtrname2);
            final String setClassType = "2".equals(_param._printRadio) ? "" : "1".equals(_param._useFi_Hrclass) ? ("1".equals(_param._printClass) ? "(法定クラス)" : "2".equals(_param._printClass) ? "(複式クラス)" : "") : "";
            svf.VrsOut("HR_NAME", student0._hrName + setClassType);

            for (int i = 0; i < pageStudentList.size(); i++) {
                final Student student = (Student) pageStudentList.get(i);
                printStudent(svf, i + 1, student);
                hasData = true;
            }

            for (int i = 0; i < Math.min(staffList.size(), staffMax); i++) {
                final Map m = (Map) staffList.get(i);
                final String chargediv = (String) m.get("CHARGEDIV");
                final String jobname = "1".equals(chargediv) ? "担任" : "0".equals(chargediv) ? "副担任" : null;
                final String staffname = (String) m.get("STAFFNAME");
                //副担任の出力の際、武蔵野東以外は役職名称を出力
                if ( !("musashinohigashi".equals(_param._z010Name) && "0".equals(chargediv)) ) {
                    svf.VrsOutn("JOB_NAME", i + 1, jobname); // 職名
                }
                svf.VrsOutn("TEACHER_NAME" + (30 < getMS932ByteLength(staffname) ? "3" : 20 < getMS932ByteLength(staffname) ? "2" : "1"), i + 1, staffname); // 職員名
//                svf.VrsOutn("TEACHER_ZIP_NO", line, null); // 職員郵便番号
//                svf.VrsOutn("TEACHER_ADDRESS" + (50 < getMS932ByteLength(address) ? "3" : 40 < getMS932ByteLength(address) ? "2" : "1"), line, address); // 職員住所
            }
            svf.VrEndPage();
        }

        return  hasData;
    }

    public String getTitle(final String schoolKind, final String hrName, final int flg) {
        final String setTitle = "1".equals(_param._printRadio) ? "(教師用)" : "";

        String title = "";
        if ("P".equals(schoolKind) || "J".equals(schoolKind) || "H".equals(schoolKind)) {
            title = _param._nendo + "　" + StringUtils.defaultString(_param._certifSchoolDatSchoolName) + "　クラス名簿" + setTitle;
        } else if ("K".equals(schoolKind)) {
            if (flg == 1) {
                // CSV出力ファイル名にHR名称は含まない
                title = _param._nendo + "　クラス名簿";
            } else {
                title = _param._nendo + "　クラス名簿" + (null == hrName ? "" : "　" + hrName);
            }
        }
        return title;
    }

    private static List getPageList(final List list, final int cnt) {
        final List pageList = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= cnt) {
                current = new ArrayList();
                pageList.add(current);
            }
            current.add(o);
        }
        return pageList;
    }

    private static String formatDate(final DB2UDB db2, final Param param, final String date) {
    	if (null == date) {
    		return null;
    	}
    	if (param._isSeireki) {
    		return KNJ_EditDate.h_format_SeirekiJP(StringUtils.replace(date, "/", "-"));
    	}
    	return KNJ_EditDate.h_format_JP(db2, date);
    }

    private void csvStudent(final List outputList, final Student student) {

        final List line = newLine(outputList);
        final String setAddr = StringUtils.defaultString(student._addr1) + StringUtils.defaultString(student._addr2);
        if ("1".equals(_param._printRadio)) {
            line.add(student._schregno);

            line.add((NumberUtils.isDigits(student._attendno)) ? String.valueOf(Integer.parseInt(student._attendno)) : StringUtils.defaultString(student._attendno));
            line.add(student._name);
            line.add(student._namekana);

            if ("H".equals(student._schoolKind)) {
                if (null != student._handicapName && student._handicapName.length() > 0) {
                    line.add(student._handicapName.substring(0, 1)); // ハンディキャップ
                } else {
                    line.add(null);
                }
            }
            line.add(student._sexname); // 性別
            if (!"1".equals(_param._notPrintHogoshaName)) {
                line.add(student._guardname); // 保護者氏名
                line.add(student._guardkana); // 保護者かな
            } else {
                line.add(null);
                line.add(null);
            }
            line.add(student._zipcd); // 郵便番号
            line.add(setAddr);
            line.add(student._telno); // 電話番号
            line.add(student._birthdayFormat); // 生年月日

            if ("K".equals(student._schoolKind)) {
//                final String careCourseCd = NumberUtils.isDigits(student._careCourseCd) ? String.valueOf(Integer.parseInt(student._careCourseCd)) : StringUtils.defaultString(student._careCourseCd);
//                line.add(careCourseCd + StringUtils.defaultString(student._busName)); // バスコース
                line.add(StringUtils.defaultString(student._busName)); // バスコース
                line.add(student._josya_2); // バス停
            }
            if (null != student._grdDiv && !"4".equals(student._grdDiv) && null != student._grdDate) {
                line.add(student._grdDateFormat + StringUtils.defaultString(student._grdDivName)); // 備考
            } else {
                line.add(null);
            }
        } else {
            line.add((NumberUtils.isDigits(student._attendno)) ? String.valueOf(Integer.parseInt(student._attendno)) : StringUtils.defaultString(student._attendno));
            line.add(student._name);
            line.add(student._namekana);

            line.add(student._sexname);
            if (!"1".equals(student._s_no_addr) || "1".equals(_param._printRadio)) {
                line.add(student._zipcd);
                line.add(setAddr);
            } else {
                line.add(null);
                line.add(null);
            }
            if (!"1".equals(student._s_no_tel) || "1".equals(_param._printRadio)) {
                line.add(student._telno);
            } else {
                line.add(null);
            }
        }
    }

    private void printStudent(final Vrw32alp svf, final int gyo, final Student student) {

        final String setAddr = StringUtils.defaultString(student._addr1) + StringUtils.defaultString(student._addr2);
        if ("1".equals(_param._printRadio)) {
            svf.VrsOutn("SCHREG_NO", gyo, student._schregno);

            svf.VrsOutn("NO", gyo, (NumberUtils.isDigits(student._attendno)) ? String.valueOf(Integer.parseInt(student._attendno)) : StringUtils.defaultString(student._attendno));
            svf.VrsOutn("NAME" + (30 < getMS932ByteLength(student._name) ? "3" : 20 < getMS932ByteLength(student._name) ? "2" : "1"), gyo, student._name);
            svf.VrsOutn("KANA" + (30 < getMS932ByteLength(student._namekana) ? "3" : 20 < getMS932ByteLength(student._namekana) ? "2" : "1"), gyo, student._namekana);

            if ("H".equals(student._schoolKind) && null != student._handicapName && student._handicapName.length() > 0) {
                svf.VrsOutn("HANDI", gyo, student._handicapName.substring(0, 1)); // ハンディキャップ
            }
            svf.VrsOutn("SEX", gyo, student._sexname); // 性別
            if (!"1".equals(_param._notPrintHogoshaName)) {
                svf.VrsOutn("GRD_NAME" + (30 < getMS932ByteLength(student._guardname) ? "3" : 20 < getMS932ByteLength(student._guardname) ? "2" : "1"), gyo, student._guardname); // 保護者氏名
                svf.VrsOutn("GRD_KANA" + (30 < getMS932ByteLength(student._guardkana) ? "3" : 20 < getMS932ByteLength(student._guardkana) ? "2" : "1"), gyo, student._guardkana); // 保護者かな
            }
            svf.VrsOutn("ZIP_NO", gyo, student._zipcd); // 郵便番号
            svf.VrsOutn("ADDRESS" + (50 < getMS932ByteLength(setAddr) ? "3" : 40 < getMS932ByteLength(setAddr) ? "2" : "1"), gyo, setAddr);
            svf.VrsOutn("TEL_NO", gyo, student._telno); // 電話番号
            svf.VrsOutn("BIRTHDAY", gyo, student._birthdayFormat); // 生年月日

            if ("K".equals(student._schoolKind)) {
//                final String careCourseCd = NumberUtils.isDigits(student._careCourseCd) ? String.valueOf(Integer.parseInt(student._careCourseCd)) : StringUtils.defaultString(student._careCourseCd);
//                svf.VrsOutn("BUS_COURSE", gyo, careCourseCd + StringUtils.defaultString(student._busName)); // バスコース
                svf.VrsOutn("BUS_COURSE", gyo, StringUtils.defaultString(student._busName)); // バスコース
                svf.VrsOutn("BUS_STOP", gyo, student._josya_2); // バス停
            }
            if (null != student._grdDiv && !"4".equals(student._grdDiv) && null != student._grdDate) {
                svf.VrsOutn("REMARK", gyo, student._grdDateFormat + StringUtils.defaultString(student._grdDivName)); // 備考
            }
        } else {
            svf.VrsOutn("NO", gyo, (NumberUtils.isDigits(student._attendno)) ? String.valueOf(Integer.parseInt(student._attendno)) : StringUtils.defaultString(student._attendno));
            svf.VrsOutn("NAME" + (30 < getMS932ByteLength(student._name) ? "3" : 20 < getMS932ByteLength(student._name) ? "2" : "1"), gyo, student._name);
            svf.VrsOutn("KANA" + (30 < getMS932ByteLength(student._namekana) ? "3" : 20 < getMS932ByteLength(student._namekana) ? "2" : "1"), gyo, student._namekana);

            svf.VrsOutn("SEX", gyo, student._sexname);
            if (!"1".equals(student._s_no_addr)) {
                svf.VrsOutn("ZIP_NO", gyo, student._zipcd);
                svf.VrsOutn("ADDRESS" + (50 < getMS932ByteLength(setAddr) ? "3" : 40 < getMS932ByteLength(setAddr) ? "2" : "1"), gyo, setAddr);
            }
            if (!"1".equals(student._s_no_tel)) {
                svf.VrsOutn("TEL_NO", gyo, student._telno);
            }
        }
    }

    private static int getMS932ByteLength(final String str) {
        return KNJ_EditEdit.getMS932ByteLength(str);
    }

    private List createStudentInfoData(final DB2UDB db2, final Param param, final String selectCd) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List rtnList = new ArrayList();
        try {
            final String sql = getStudentInfoSql(selectCd);
            //log.debug(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final Student studentInfo = new Student(
                        rs.getString("GRADE"),
                        rs.getString("HR_CLASS"),
                        rs.getString("HR_NAME"),
                        rs.getString("PREF_CD"),
                        rs.getString("PREF_NAME"),
                        rs.getString("AREACD"),
                        rs.getString("AREA_NAME"),
                        rs.getString("SCHREGNO"),
                        rs.getString("ATTENDNO"),
                        rs.getString("NAME"),
                        rs.getString("NAME_KANA"),
                        rs.getString("KYODAKU_FLG"),
                        rs.getString("SEX"),
                        rs.getString("SEX_NAME"),
                        rs.getString("HANDICAP"),
                        rs.getString("HANDICAP_NAME"),
                        rs.getString("BIRTHDAY"),
                        rs.getString("GRD_DIV"),
                        rs.getString("GRD_DIV_NAME"),
                        rs.getString("GRD_DATE"),
                        rs.getString("GUARD_NAME"),
                        rs.getString("GUARD_KANA"),
                        rs.getString("ZIPCD"),
                        rs.getString("ADDR1"),
                        rs.getString("ADDR2"),
                        rs.getString("TELNO"),
                        rs.getString("TR_CD1"),
                        rs.getString("TR_NAME1"),
                        rs.getString("SUBTR_CD1"),
                        rs.getString("SUBTR_NAME1"),
                        rs.getString("SUBTR_NAME2"),
                        rs.getString("S_NO_NAME"),
                        rs.getString("S_NO_ADDR"),
                        rs.getString("S_NO_TEL"),
                        rs.getString("S_NO_BIRTH"),
                        rs.getString("H_NO_NAME"),
                        rs.getString("H_NO_ADDR"),
                        rs.getString("H_NO_TEL"),
                        rs.getString("H_NO_BIRTH"),
                        rs.getString("SCHOOL_KIND"),
                        rs.getString("COURSE_CD"),
                        rs.getString("BUS_NAME"),
                        rs.getString("JOSYA_2")
                );
                rtnList.add(studentInfo);
            }
            for (final Iterator it = rtnList.iterator(); it.hasNext();) {
            	final Student student = (Student) it.next();
            	student._birthdayFormat = formatDate(db2, param, student._birthday);
            	student._grdDateFormat = formatDate(db2, param, student._grdDate);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnList;
    }

    private String getStudentInfoSql(final String selectCd) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCHREG_ADDRESS_MAX AS ( ");
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
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.GRADE, ");
        stb.append("     REGDG.SCHOOL_KIND, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T2.HR_NAME, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T3.NAME, ");
        stb.append("     T3.NAME_KANA, ");
        stb.append("     T6.BASE_REMARK1 AS KYODAKU_FLG, ");
        stb.append("     T3.SEX, ");
        stb.append("     N1.ABBV1 AS SEX_NAME, ");
        stb.append("     T3.HANDICAP, ");
        stb.append("     NMA025.NAME1 AS HANDICAP_NAME, ");
        stb.append("     NMA003.NAME1 AS GRD_DIV_NAME, ");
        stb.append("     ENTGRD.GRD_DIV, ");
        stb.append("     ENTGRD.GRD_DATE, ");
        stb.append("     T4.GUARD_NAME, ");
        stb.append("     T4.GUARD_KANA, ");
        stb.append("     T5.ZIPCD, ");
        stb.append("     T5.PREF_CD, ");
        stb.append("     T5.PREF_NAME, ");
        stb.append("     T5.AREACD, ");
        stb.append("     T5.AREA_NAME, ");
        stb.append("     T5.ADDR1, ");
        stb.append("     T5.ADDR2, ");
        stb.append("     T5.TELNO, ");
        stb.append("     T3.BIRTHDAY, ");
        stb.append("     T2.TR_CD1, ");
        stb.append("     S1.STAFFNAME AS TR_NAME1, ");
        stb.append("     T2.SUBTR_CD1, ");
        stb.append("     S2.STAFFNAME AS SUBTR_NAME1, ");
        stb.append("     S3.STAFFNAME AS SUBTR_NAME2, ");
        stb.append("     LICENSE_GS1.REMARK1 AS S_NO_NAME, ");
        stb.append("     LICENSE_GS1.REMARK2 AS S_NO_ADDR, ");
        stb.append("     LICENSE_GS1.REMARK3 AS S_NO_TEL, ");
        stb.append("     LICENSE_GS1.REMARK4 AS S_NO_BIRTH, ");
        stb.append("     LICENSE_GS2.REMARK1 AS H_NO_NAME, ");
        stb.append("     LICENSE_GS2.REMARK2 AS H_NO_ADDR, ");
        stb.append("     LICENSE_GS2.REMARK3 AS H_NO_TEL, ");
        stb.append("     LICENSE_GS2.REMARK4 AS H_NO_BIRTH, ");
        stb.append("     ENVR.ROSEN_2 AS COURSE_CD, ");
        stb.append("     ENVR.JOSYA_2, ");
        stb.append("     BUS_YM.BUS_NAME ");
        stb.append(" FROM ");
        stb.append("     " + _param._tableRegdDat + " T1 ");
        stb.append("     INNER JOIN " + _param._tableRegdHDat + " T2 ");
        stb.append("         ON  T2.YEAR = T1.YEAR ");
        stb.append("         AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("         AND T2.GRADE = T1.GRADE ");
        stb.append("         AND T2.HR_CLASS = T1.HR_CLASS ");
        stb.append("     INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("     INNER JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = T1.YEAR AND REGDG.GRADE = T1.GRADE ");
        stb.append("     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT ENTGRD ON ENTGRD.SCHREGNO = T1.SCHREGNO AND ENTGRD.SCHOOL_KIND = REGDG.SCHOOL_KIND ");
        stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z002' AND N1.NAMECD2 = T3.SEX ");
        stb.append("     LEFT JOIN GUARDIAN_DAT T4 ON T4.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_ADDRESS T5 ON T5.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN STAFF_MST S1 ON S1.STAFFCD = T2.TR_CD1 ");
        stb.append("     LEFT JOIN STAFF_MST S2 ON S2.STAFFCD = T2.SUBTR_CD1 ");
        stb.append("     LEFT JOIN STAFF_MST S3 ON S3.STAFFCD = T2.SUBTR_CD2 ");
        stb.append("     LEFT JOIN SCHREG_BASE_DETAIL_MST T6 ON T6.SCHREGNO = T1.SCHREGNO AND T6.BASE_SEQ = '006' ");
        stb.append("     LEFT JOIN LICENSE_GROUP_PRGID_DAT LICENSE_GM ON LICENSE_GM.PRGID = 'KNJA171F' ");
        stb.append("     LEFT JOIN LICENSE_GROUP_STD_DAT LICENSE_GS1 ON LICENSE_GM.GROUP_DIV = LICENSE_GS1.GROUP_DIV ");
        stb.append("          AND LICENSE_GS1.SCHREGNO = T1.SCHREGNO ");
        stb.append("          AND LICENSE_GS1.SELECT_DIV = '1' ");
        stb.append("     LEFT JOIN LICENSE_GROUP_STD_DAT LICENSE_GS2 ON LICENSE_GM.GROUP_DIV = LICENSE_GS2.GROUP_DIV ");
        stb.append("          AND LICENSE_GS2.SCHREGNO = T1.SCHREGNO ");
        stb.append("          AND LICENSE_GS2.SELECT_DIV = '2' ");
        stb.append("     LEFT JOIN NAME_MST NMA025 ON NMA025.NAMECD1 = 'A025' AND NMA025.NAMECD2 = T3.HANDICAP ");
        stb.append("     LEFT JOIN NAME_MST NMA003 ON NMA003.NAMECD1 = 'A003' AND NMA003.NAMECD2 = ENTGRD.GRD_DIV ");
        stb.append("     LEFT JOIN SCHREG_ENVIR_DAT ENVR ON ENVR.SCHREGNO = T1.SCHREGNO AND ENVR.HOWTOCOMMUTECD = '1' AND ENVR.FLG_2 = '3'");
        stb.append("     LEFT JOIN CHILDCARE_BUS_YMST BUS_YM ON BUS_YM.YEAR = T1.YEAR AND BUS_YM.COURSE_CD = ENVR.ROSEN_2 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + selectCd + "' ");
        if ("1".equals(_param._joseki)) {
            stb.append("     AND NOT (ENTGRD.GRD_DIV IS NOT NULL AND ENTGRD.GRD_DIV <> '4' AND ENTGRD.GRD_DATE < '" + _param._ctrlDate + "') ");
        }
        stb.append(" ORDER BY ");
//        if ("2".equals(_param._printClass)) {
//            stb.append("     T3.NAME_KANA, ");
//        }
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO ");
        return stb.toString();
    }

    /** 生徒 */
    private class Student {
        final String _grade;
        final String _hrclass;
        final String _hrName;
        final String _prefcd;
        final String _prefname;
        final String _areacd;
        final String _areaname;
        final String _schregno;
        final String _attendno;
        final String _name;
        final String _namekana;
        final String _kyodakuFlg;
        final String _sex;
        final String _sexname;
        final String _handicap;
        final String _handicapName;
        final String _birthday;
        final String _grdDiv;
        final String _grdDivName;
        final String _grdDate;
        final String _guardname;
        final String _guardkana;
        final String _zipcd;
        final String _addr1;
        final String _addr2;
        final String _telno;
        final String _trcd1;
        final String _trname1;
        final String _subtrcd1;
        final String _subtrname1;
        final String _subtrname2;
        final String _s_no_name;
        final String _s_no_addr;
        final String _s_no_tel;
        final String _s_no_birth;
        final String _h_no_name;
        final String _h_no_addr;
        final String _h_no_tel;
        final String _h_no_birth;
        final String _schoolKind;
        final String _careCourseCd;
        final String _busName;
        final String _josya_2;
        String _birthdayFormat;
        String _grdDateFormat;

        Student(
                final String grade,
                final String hrclass,
                final String hrName,
                final String prefcd,
                final String prefname,
                final String areacd,
                final String areaname,
                final String schregno,
                final String attendno,
                final String name,
                final String namekana,
                final String kyodakuFlg,
                final String sex,
                final String sexname,
                final String handicap,
                final String handicapName,
                final String birthday,
                final String grdDiv,
                final String grdDivName,
                final String grdDate,
                final String guardname,
                final String guardkana,
                final String zipcd,
                final String addr1,
                final String addr2,
                final String telno,
                final String trcd1,
                final String trname1,
                final String subtrcd1,
                final String subtrname1,
                final String subtrname2,
                final String s_no_name,
                final String s_no_addr,
                final String s_no_tel,
                final String s_no_birth,
                final String h_no_name,
                final String h_no_addr,
                final String h_no_tel,
                final String h_no_birth,
                final String schoolKind,
                final String careCourseCd,
                final String busName,
                final String josya_2
        ) {
            _grade = grade;
            _hrclass = hrclass;
            _hrName = hrName;
            _prefcd = prefcd;
            _prefname = prefname;
            _areacd = areacd;
            _areaname = areaname;
            _schregno = schregno;
            _attendno = attendno;
            _name = name;
            _namekana = namekana;
            _kyodakuFlg = kyodakuFlg;
            _sex = sex;
            _sexname = sexname;
            _handicap = handicap;
            _handicapName = handicapName;
            _birthday = birthday;
            _grdDiv = grdDiv;
            _grdDivName = grdDivName;
            _grdDate = grdDate;
            _guardname = guardname;
            _guardkana = guardkana;
            _zipcd = zipcd;
            _addr1 = addr1;
            _addr2 = addr2;
            _telno = telno;
            _trcd1 = trcd1;
            _trname1 = trname1;
            _subtrcd1 = subtrcd1;
            _subtrname1 = subtrname1;
            _subtrname2 = subtrname2;
            _s_no_name = s_no_name;
            _s_no_addr = s_no_addr;
            _s_no_tel = s_no_tel;
            _s_no_birth = s_no_birth;
            _h_no_name = h_no_name;
            _h_no_addr = h_no_addr;
            _h_no_tel = h_no_tel;
            _h_no_birth = h_no_birth;
            _schoolKind = schoolKind;
            _careCourseCd = careCourseCd;
            _busName = busName;
            _josya_2 = josya_2;
        }
    }

    private List getStaffList(final DB2UDB db2, final String gradeHrclass) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List rtnList = new ArrayList();
        try {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH T_TRCD AS ( ");
            stb.append("   SELECT TR_CD1, TR_CD2, TR_CD3, SUBTR_CD1, SUBTR_CD2, SUBTR_CD3 ");
            stb.append("   FROM " + _param._tableRegdHDat + " T1 ");
            stb.append("   WHERE YEAR = '" + _param._year + "' AND SEMESTER = '" + _param._semester + "' AND GRADE || HR_CLASS = '" + gradeHrclass + "' ");
            stb.append(" ) ");
            stb.append("           SELECT 1, 1 AS CHARGEDIV, STAFFNAME FROM STAFF_MST WHERE STAFFCD IN (SELECT TR_CD1 FROM T_TRCD)  ");
//            stb.append(" UNION ALL SELECT 2, 1 AS CHARGEDIV, STAFFNAME FROM STAFF_MST WHERE STAFFCD IN (SELECT TR_CD2 FROM T_TRCD)  ");
//            stb.append(" UNION ALL SELECT 3, 1 AS CHARGEDIV, STAFFNAME FROM STAFF_MST WHERE STAFFCD IN (SELECT TR_CD3 FROM T_TRCD)  ");
            stb.append(" UNION ALL SELECT 4, 0 AS CHARGEDIV, STAFFNAME FROM STAFF_MST WHERE STAFFCD IN (SELECT SUBTR_CD1 FROM T_TRCD)  ");
            stb.append(" UNION ALL SELECT 5, 0 AS CHARGEDIV, STAFFNAME FROM STAFF_MST WHERE STAFFCD IN (SELECT SUBTR_CD2 FROM T_TRCD)  ");
//            stb.append(" UNION ALL SELECT 6, 0 AS CHARGEDIV, STAFFNAME FROM STAFF_MST WHERE STAFFCD IN (SELECT SUBTR_CD3 FROM T_TRCD)  ");
            stb.append(" ORDER BY 1 ");

            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                final Map m = new HashMap();
                m.put("CHARGEDIV", rs.getString("CHARGEDIV"));
                m.put("STAFFNAME", rs.getString("STAFFNAME"));
                rtnList.add(m);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnList;
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) {
        log.fatal("$Revision: 73004 $ $Date: 2020-03-16 18:22:58 +0900 (月, 16 3 2020) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        return new Param(db2, request);
    }

    private static class Param {

        final String _year;
        final String _semester;
        final String[] _classSelected;
        final String _ctrlDate;
        final String _useFi_Hrclass;
        final String _printClass;
        final String _printRadio; // 1:職員用、2:保護者用
        final String _tableRegdDat;
        final String _tableRegdHDat;
        final String _joseki;
        final String _notPrintHogoshaName;
        final String _cmd;
        final boolean _isSeireki;
        final String _certifSchoolDatSchoolName;
        final String _z010Name;
        final String _nendo;
        final String _ctrlDateFormat;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("CTRL_SEMESTER");
            _classSelected = request.getParameterValues("CATEGORY_SELECTED");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _useFi_Hrclass = request.getParameter("useFi_Hrclass");
            //1:法定クラス 2:複式クラス
            _printClass = request.getParameter("HR_CLASS_TYPE");
            if ("2".equals(_printClass)) {
                _tableRegdDat = "SCHREG_REGD_FI_DAT";
                _tableRegdHDat = "SCHREG_REGD_FI_HDAT";
            } else {
                _tableRegdDat = "SCHREG_REGD_DAT";
                _tableRegdHDat = "SCHREG_REGD_HDAT";
            }
            _printRadio = request.getParameter("PRINT_RADIO");
            _joseki = request.getParameter("JOSEKI");
            _notPrintHogoshaName = request.getParameter("NOT_PRINT_HOGOSHA_NAME");
            _cmd = request.getParameter("cmd");
            _isSeireki = KNJ_EditDate.isSeireki(db2);
            _certifSchoolDatSchoolName = getCertifSchoolDatSchoolName(db2);
            _z010Name = getZ010Name1(db2);
            _nendo = KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_year)) + "年度";
            _ctrlDateFormat = formatDate(db2, this, _ctrlDate);
        }

        private String getZ010Name1(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' "));
        }

        private String getCertifSchoolDatSchoolName(final DB2UDB db2) {
            String certifKindCd = "131";
            String rtn = null;
            final String sql = "SELECT SCHOOL_NAME FROM CERTIF_SCHOOL_DAT"
                    + " WHERE YEAR='" + _year + "'"
                    + " AND CERTIF_KINDCD='" + certifKindCd + "'";
            rtn = KnjDbUtils.getOne(KnjDbUtils.query(db2, sql));
            return rtn;
        }
    }

}// クラスの括り
