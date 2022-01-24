package servletpack.KNJA;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_Semester;
import servletpack.KNJZ.detail.StaffInfo;
import servletpack.KNJZ.detail.SvfField;

/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*
 *
 *  学校教育システム 賢者 [学籍管理]
 *                                                               　                *
 *                  ＜ＫＮＪＡ２００＞    クラス名簿印刷　　　　　　　　           *
 *                                                                                 *
 * 2003/11/12 nakamoto 和暦変換に対応                                              *
 * 2005/05/23 yamasiro 電話番号出力指定                                            *
 *<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/

public class KNJA200A {

    private static final Log log = LogFactory.getLog("KNJA200A.class");

    private boolean _hasData;

    private Param _param;
    private Map _formFieldInfoMap = new HashMap();

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {
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

    }    //doGetの括り

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {

        Integer field1_1_2Keta = null;
        final int maxRowCnt = 5;
        final int maxLine = 5;
        final String form = "KNJA200A.frm";
        final String nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度";
        final String today = KNJ_EditDate.h_format_JP(db2, _param._date);
        final List printMainList = getPrintMainList(db2);
        for (final Iterator iter = printMainList.iterator(); iter.hasNext();) {
            svf.VrSetForm(form, 1);

            if (null == field1_1_2Keta) {
                try {
                    _formFieldInfoMap.put(form, SvfField.getSvfFormFieldInfoMapGroupByName(svf));
                } catch (Throwable t) {
                    log.error(t);
                }
                final String fieldname = "field1_1_2";
                field1_1_2Keta = new Integer(getFieldLength(form, fieldname, 0));
                log.info(" field = " + fieldname + ", keta = " + field1_1_2Keta);
            }

            _hasData = true;
            int rowCnt = 1;
            int line = 1;
            final MainClass mainClass = (MainClass) iter.next();
            String staffstr = "";
            for (final Iterator staff_it = mainClass._staffList.iterator(); staff_it.hasNext();) {
                final String addstaffname =(String)staff_it.next();
                if (addstaffname == null) break;
            	staffstr += (!"".equals(staffstr) ? "、" : "");
            	staffstr += addstaffname;
            }

            if ("1".equals(_param._dataDiv)) {
                svf.VrsOut("TITLE", "部活動名簿");
                final String buName = "部活名：" + mainClass._name;
                final String staff = " 顧問名：" + StringUtils.defaultString(staffstr);
                svf.VrsOut("GRADE_HR_CLASS" ,  buName + staff);
            } else if ("2".equals(_param._dataDiv)) {
                svf.VrsOut("TITLE", "生徒会活動等名簿");
                final String buName = "委員会名：" + mainClass._name;
                final String staff = " 顧問名：" + StringUtils.defaultString(staffstr);
                svf.VrsOut("GRADE_HR_CLASS" ,  buName + staff);
            } else {
                svf.VrsOut("TITLE", "学級活動名簿");
                final String buName = "クラス名：" + mainClass._name;
                final String staff = " 担任名：" + StringUtils.defaultString(staffstr);
                svf.VrsOut("GRADE_HR_CLASS" ,  buName + staff);
            }
            svf.VrsOut("NENDO", nendo);
            svf.VrsOut("SEMESTER", _param._semesterName);
            svf.VrsOut("TODAY", today);
            for (final Iterator itStudent = mainClass._studentList.iterator(); itStudent.hasNext();) {

                if (rowCnt > maxRowCnt) {
                    rowCnt = 1;
                    line++;
                }
                if (line > maxLine) {
                    rowCnt = 1;
                    line = 1;
                    svf.VrEndPage();
                }
                final Student student = (Student) itStudent.next();
                final String attendNo = String.valueOf(Integer.parseInt(student._attendNo));
                String hrnameAttendno = "";
                if (_param._isSundaikoufu) {
                    hrnameAttendno = StringUtils.defaultString(student._hrClassName2) + "-" + attendNo;
                } else {
                    hrnameAttendno = student._hrName + "-" + attendNo;
                }
                String field1;
                if (getMS932ByteLength(hrnameAttendno) > 9 && field1_1_2Keta.intValue() > 9) {
                    field1 = "field1_" + rowCnt + "_2";
                } else {
                    field1 = "field1_" + rowCnt;
                }
                svf.VrsOutn(field1, line, hrnameAttendno);
                svf.VrsOutn("field2_" + rowCnt, line, "(" + student._schregNo + ")");
                String studentName = _param._staffInfo.getStrEngOrJp(student._name, student._nameEng);
				final String setStudentName = null != student._executive ? student._executive + ":" + studentName : studentName;
                final String nameField = getMS932ByteLength(setStudentName) > 26 ? "_3" : getMS932ByteLength(setStudentName) > 20 ? "_2" : "";
                svf.VrsOutn("NAME_SHOW_" + rowCnt + nameField, line, setStudentName);

                if ("1".equals(_param._tel)) {
                    svf.VrsOutn("PHONE" + rowCnt, line, student._tel);
                }
                String strdir1 = "P" + student._schregNo + "." + _param._extention;
                String strdir2 = _param._documentroot + "/" + _param._folder + "/" + strdir1;
                File f1 = new File(strdir2);   //写真データ存在チェック用

                //写真
                if (f1.exists()) {
                    svf.VrsOutn("Bitmap_Field" + rowCnt, line, strdir2);
                    svf.VrsOutn("NO_DATA"      + rowCnt, line, "");
                    svf.VrsOutn("ATTENDNO"     + rowCnt, line, "");
                } else{
                    svf.VrsOutn("Bitmap_Field" + rowCnt, line, "");
                    svf.VrsOutn("NO_DATA"      + rowCnt, line, "イメージデータなし");
                    svf.VrsOutn("ATTENDNO"     + rowCnt, line, strdir1);
                }
                rowCnt++;
            }
            svf.VrEndPage();
        }
    }

    /**
     * @return
     */
    private List getPrintMainList(final DB2UDB db2) throws SQLException {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String getHrSql = getMainSql();

        try {
            ps = db2.prepareStatement(getHrSql);
            rs = ps.executeQuery();
            MainClass mainClass = null;
            while (rs.next()) {
                final String cd = rs.getString("CD");
                final String hrName = rs.getString("NAME");
                final String hrNameAbbv = rs.getString("ABBV");
                final String staffName = rs.getString("STAFFNAME");
            	if (mainClass == null) {
                    mainClass = new MainClass(cd, hrName, hrNameAbbv, staffName);
            	} else {
                    if (staffName != null && (!"".equals(staffName) && !mainClass._staffList.contains(staffName))) {
            		    mainClass._staffList.add(staffName);
                    }
            	}
            }
            if (mainClass != null) {
                mainClass.setStudent(db2);
                retList.add(mainClass);
            }
        } catch (Exception e) {
            log.fatal("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    /**
     * @return
     */
    private String getMainSql() {
        if ("1".equals(_param._dataDiv)) {
            return getClubSql();
        } else if ("2".equals(_param._dataDiv)) {
            return getCommitteeSql();
        } else {
            return getClassSql();
        }
    }

    /**
     * @return
     */
    private String getClubSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.CLUBCD AS CD, ");
        stb.append("     CLUBNAME AS NAME, ");
        stb.append("     CLUBNAME AS ABBV, ");
        stb.append("     L2.STAFFNAME ");
        stb.append(" FROM ");
        stb.append("     CLUB_MST T1 ");
        stb.append("     LEFT JOIN (SELECT ");
        if ("1".equals(_param._useSchool_KindField)) {
            stb.append("   SCHOOLCD, ");
            stb.append("   SCHOOL_KIND, ");
        }
        stb.append("                    CLUBCD, ");
        stb.append("                    ADVISER AS ADVISER ");
        stb.append("                FROM ");
        stb.append("                    CLUB_ADVISER_DAT ");
        stb.append("                WHERE ");
        stb.append("                    YEAR = '" + _param._year + "' ");
        stb.append("                    AND CLUBCD = '" + _param._dataCmb + "' ");
        stb.append("                ORDER BY ADVISER ");
        stb.append("                ) L1 ON T1.CLUBCD = L1.CLUBCD ");
        if ("1".equals(_param._useSchool_KindField)) {
        	stb.append("        AND L1.SCHOOLCD = T1.SCHOOLCD ");
        	stb.append("        AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ");
        }
        stb.append("     LEFT JOIN STAFF_MST L2 ON L1.ADVISER = L2.STAFFCD ");
        stb.append(" WHERE ");
        stb.append("     T1.CLUBCD = '" + _param._dataCmb + "' ");
        if ("1".equals(_param._useClubMultiSchoolKind) && !StringUtils.isBlank(_param._SCHOOLKIND) && !StringUtils.isBlank(_param._SCHOOLCD)) {
            stb.append("   AND T1.SCHOOLCD = '" + _param._SCHOOLCD + "' ");
            stb.append("   AND T1.SCHOOL_KIND = '" + _param._SCHOOLKIND + "' ");
        } else {
            if (!StringUtils.isBlank(_param._schkind)) {
                stb.append("   AND T1.SCHOOL_KIND = '" + _param._schkind + "' ");
            } else if ("1".equals(_param._use_prg_schoolkind)) {
                if (!StringUtils.isBlank(_param._selectSchoolKind)) {
                    stb.append("        AND T1.SCHOOLCD = '" + _param._SCHOOLCD + "' ");
                    stb.append("        AND T1.SCHOOL_KIND IN " + _param.selectSchoolKindSql + "  ");
                }
            } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOLKIND) && !StringUtils.isBlank(_param._SCHOOLCD)) {
                stb.append("   AND T1.SCHOOLCD = '" + _param._SCHOOLCD + "' ");
                stb.append("   AND T1.SCHOOL_KIND = '" + _param._SCHOOLKIND + "' ");
            }
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.CLUBCD ");

        return stb.toString();
    }

    /**
     * @return
     */
    private String getCommitteeSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.COMMITTEE_FLG || '-' || T1.COMMITTEECD AS CD, ");
        stb.append("     COMMITTEENAME AS NAME, ");
        stb.append("     COMMITTEENAME AS ABBV, ");
        stb.append("     T3.STAFFNAME ");
        stb.append(" FROM ");
        stb.append("     COMMITTEE_MST T1 ");
        stb.append("     LEFT JOIN (SELECT COMMITTEE_FLG || '-' || COMMITTEECD AS CD, MIN(ADVISER) AS ADVISER ");
        stb.append("                FROM COMMITTEE_ADVISER_DAT  ");
        stb.append("                WHERE ");
        stb.append("                  YEAR = '" + _param._year + "' ");
        if ("1".equals(_param._useClubMultiSchoolKind) && !StringUtils.isBlank(_param._SCHOOLKIND) && !StringUtils.isBlank(_param._SCHOOLCD)) {
            stb.append("   AND SCHOOLCD = '" + _param._SCHOOLCD + "' ");
            stb.append("   AND SCHOOL_KIND = '" + _param._SCHOOLKIND + "' ");
        } else {
            if ("1".equals(_param._use_prg_schoolkind)) {
                if (!StringUtils.isBlank(_param._selectSchoolKind)) {
                    stb.append("        AND SCHOOLCD = '" + _param._SCHOOLCD + "' ");
                    stb.append("        AND SCHOOL_KIND IN " + _param.selectSchoolKindSql + "  ");
                }
            } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOLKIND) && !StringUtils.isBlank(_param._SCHOOLCD)) {
                stb.append("   AND SCHOOLCD = '" + _param._SCHOOLCD + "' ");
                stb.append("   AND SCHOOL_KIND = '" + _param._SCHOOLKIND + "' ");
            }
        }

        stb.append("                GROUP BY COMMITTEE_FLG || '-' || COMMITTEECD ");
        stb.append("               ) T2 ON T2.CD = T1.COMMITTEE_FLG || '-' || T1.COMMITTEECD ");
        stb.append("     LEFT JOIN STAFF_MST T3 ON T3.STAFFCD = T2.ADVISER ");
        stb.append(" WHERE ");
        stb.append("     T1.COMMITTEE_FLG || '-' || T1.COMMITTEECD = '" + _param._dataCmb + "' ");
        if ("1".equals(_param._useClubMultiSchoolKind) && !StringUtils.isBlank(_param._SCHOOLKIND) && !StringUtils.isBlank(_param._SCHOOLCD)) {
            stb.append("   AND T1.SCHOOLCD = '" + _param._SCHOOLCD + "' ");
            stb.append("   AND T1.SCHOOL_KIND = '" + _param._SCHOOLKIND + "' ");
        } else {
            if (!StringUtils.isBlank(_param._schkind)) {
                stb.append("   AND T1.SCHOOL_KIND = '" + _param._schkind + "' ");
            } else if ("1".equals(_param._use_prg_schoolkind)) {
                if (!StringUtils.isBlank(_param._selectSchoolKind)) {
                    stb.append("        AND T1.SCHOOLCD = '" + _param._SCHOOLCD + "' ");
                    stb.append("        AND T1.SCHOOL_KIND IN " + _param.selectSchoolKindSql + "  ");
                }
            } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOLKIND) && !StringUtils.isBlank(_param._SCHOOLCD)) {
                stb.append("   AND T1.SCHOOLCD = '" + _param._SCHOOLCD + "' ");
                stb.append("   AND T1.SCHOOL_KIND = '" + _param._SCHOOLKIND + "' ");
            }
        }

        stb.append(" ORDER BY ");
        stb.append("     T1.COMMITTEE_FLG || '-' || T1.COMMITTEECD ");

        return stb.toString();
    }

    /**
     * @return
     */
    private String getClassSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.GRADE || T1.HR_CLASS AS CD, ");
        stb.append("     T1.HR_NAME AS NAME, ");
        stb.append("     T1.HR_NAMEABBV AS ABBV, ");
        stb.append("     T3.STAFFNAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_HDAT T1 ");
        stb.append("     LEFT JOIN STAFF_MST T3 ON T3.STAFFCD = T1.TR_CD1 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + _param._dataCmb + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE || T1.HR_CLASS ");

        return stb.toString();
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    private static int getMS932ByteLength(final String s) {
    	return KNJ_EditEdit.getMS932ByteLength(s);
    }

    private class MainClass {
        final String _cd;
        final String _name;
        final String _abbv;
        final List _staffList;
        final List _studentList;

        /**
         * コンストラクタ。
         */
        MainClass(
                final String cd,
                final String name,
                final String abbv,
                final String staffName
        ) {
            _cd = cd;
            _name = name;
            _abbv = abbv;
            _staffList = new ArrayList();
            _staffList.add(staffName);
            _studentList = new ArrayList();
        }
        /**
         * @param db2
         */
        public void setStudent(final DB2UDB db2) throws SQLException {
            final String studentSql = getStudentSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(studentSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregNo = rs.getString("SCHREGNO");
                    final String hrName = rs.getString("HR_NAME");
                    final String hrClassName2 = rs.getString("HR_CLASS_NAME2");
                    final String attendNo = rs.getString("ATTENDNO");
                    final String name = rs.getString("NAME");
                    final String nameEng = rs.getString("NAME_ENG");
                    final String nameKana = rs.getString("NAME_KANA");
                    final String tel = rs.getString("TELNO");
                    final String executive = rs.getString("NAME1");
                    final Student student = new Student(schregNo, hrName, hrClassName2, attendNo, name, nameEng, nameKana, tel, executive);
                    _studentList.add(student);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
        }
        /**
         * @return
         */
        private String getStudentSql() {
            if ("1".equals(_param._dataDiv)) {
                return getClubStdSql();
            } else if ("2".equals(_param._dataDiv)) {
                return getCommitteeStdSql();
            } else {
                return getClassStdSql();
            }
        }

        /**
         * @return
         */
        private String getClubStdSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     REGD.GRADE, ");
            stb.append("     REGD.HR_CLASS, ");
            stb.append("     REGH.HR_NAME, ");
            stb.append("     REGH.HR_NAMEABBV, ");
            stb.append("     REGH.HR_CLASS_NAME2, ");
            stb.append("     REGD.ATTENDNO, ");
            stb.append("     T1.EXECUTIVECD, ");
            stb.append("     L1.NAME1, ");
            stb.append("     BASE.NAME, ");
            stb.append("     BASE.NAME_ENG, ");
            stb.append("     BASE.NAME_KANA, ");
            stb.append("     ADDR.TELNO ");
            stb.append(" FROM ");
            stb.append("     SCHREG_CLUB_HIST_DAT T1 ");
            stb.append("     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'J001' ");
            stb.append("          AND T1.EXECUTIVECD = L1.NAMECD2 ");
            stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON T1.SCHREGNO = BASE.SCHREGNO ");
            stb.append("     INNER JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = '" + _param._year + "' ");
            stb.append("          AND REGD.SEMESTER = '" + _param._semester + "' ");
            stb.append("          AND T1.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGH ON REGD.YEAR = REGH.YEAR ");
            stb.append("          AND REGD.SEMESTER = REGH.SEMESTER ");
            stb.append("          AND REGD.GRADE = REGH.GRADE ");
            stb.append("          AND REGD.HR_CLASS = REGH.HR_CLASS ");
            if ("1".equals(_param._useClubMultiSchoolKind) && !StringUtils.isBlank(_param._SCHOOLKIND) && !StringUtils.isBlank(_param._SCHOOLCD)) {
                stb.append("   LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGH.YEAR AND GDAT.GRADE = REGH.GRADE ");
            } else {
                if (!StringUtils.isBlank(_param._schkind)) {
                    stb.append("   INNER JOIN SCHREG_REGD_GDAT GDAT ON REGD.YEAR = GDAT.YEAR AND REGD.GRADE = GDAT.GRADE AND GDAT.SCHOOL_KIND = '" + _param._schkind + "' ");
                } else if ("1".equals(_param._use_prg_schoolkind)) {
                    stb.append("   LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGH.YEAR AND GDAT.GRADE = REGH.GRADE ");
                } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOLKIND) && !StringUtils.isBlank(_param._SCHOOLCD)) {
                    stb.append("   LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGH.YEAR AND GDAT.GRADE = REGH.GRADE ");
                }
            }

            stb.append("     LEFT JOIN ( ");
            stb.append("         SELECT  ");
            stb.append("              SCHREGNO, ");
            stb.append("              TELNO  ");
            stb.append("         FROM  ");
            stb.append("              SCHREG_ADDRESS_DAT  ");
            stb.append("         WHERE  ");
            stb.append("             (SCHREGNO,ISSUEDATE) IN  ");
            stb.append("             ( SELECT  ");
            stb.append("                   SCHREGNO,MAX(ISSUEDATE)  ");
            stb.append("               FROM  ");
            stb.append("                   SCHREG_ADDRESS_DAT  ");
            stb.append("               GROUP BY ");
            stb.append("                   SCHREGNO  ");
            stb.append("               HAVING  ");
            stb.append("                   SCHREGNO IN ( ");
            stb.append("                        SELECT  ");
            stb.append("                            SCHREGNO  ");
            stb.append("                        FROM  ");
            stb.append("                            SCHREG_REGD_DAT  ");
            stb.append("                        WHERE  ");
            stb.append("                            YEAR = '" + _param._year + "'  ");
            stb.append("                            AND SEMESTER = '" + _param._semester + "'  ");
            stb.append("                   ) ");
            stb.append("             )  ");
            stb.append("     ) ADDR ON (T1.SCHREGNO = ADDR.SCHREGNO)  ");
            stb.append(" WHERE ");
            stb.append("     T1.CLUBCD = '" + _cd + "' ");
            stb.append("     AND '" + _param._date.replace('/', '-') + "' BETWEEN T1.SDATE AND VALUE(T1.EDATE, '9999-12-31') ");
            if ("1".equals(_param._useClubMultiSchoolKind) && !StringUtils.isBlank(_param._SCHOOLKIND) && !StringUtils.isBlank(_param._SCHOOLCD)) {
                stb.append("   AND T1.SCHOOLCD = '" + _param._SCHOOLCD + "' ");
                stb.append("   AND T1.SCHOOL_KIND = '" + _param._SCHOOLKIND + "' ");
                stb.append("   AND GDAT.SCHOOL_KIND = '" + _param._SCHOOLKIND + "' ");
            } else {
                if ("1".equals(_param._use_prg_schoolkind)) {
                    if (!StringUtils.isBlank(_param._selectSchoolKind)) {
                        stb.append("        AND T1.SCHOOLCD = '" + _param._SCHOOLCD + "' ");
                        stb.append("        AND T1.SCHOOL_KIND IN " + _param.selectSchoolKindSql + "  ");
                    }
                } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOLKIND) && !StringUtils.isBlank(_param._SCHOOLCD)) {
                    stb.append("   AND T1.SCHOOLCD = '" + _param._SCHOOLCD + "' ");
                    stb.append("   AND T1.SCHOOL_KIND = '" + _param._SCHOOLKIND + "' ");
                }
                if ("1".equals(_param._use_prg_schoolkind)) {
                    if (!StringUtils.isBlank(_param._selectSchoolKind)) {
                        stb.append("        AND GDAT.SCHOOL_KIND IN " + _param.selectSchoolKindSql + "  ");
                    }
                } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOLKIND) && !StringUtils.isBlank(_param._SCHOOLCD)) {
                    stb.append("   AND GDAT.SCHOOL_KIND = '" + _param._SCHOOLKIND + "' ");
                }
            }
            stb.append(" ORDER BY ");
            stb.append("     REGD.GRADE, ");
            stb.append("     REGD.HR_CLASS, ");
            stb.append("     REGD.ATTENDNO ");

            return stb.toString();
        }

        /**
         * @return
         */
        private String getCommitteeStdSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     REGD.GRADE, ");
            stb.append("     REGD.HR_CLASS, ");
            stb.append("     REGH.HR_NAME, ");
            stb.append("     REGH.HR_NAMEABBV, ");
            stb.append("     REGH.HR_CLASS_NAME2, ");
            stb.append("     REGD.ATTENDNO, ");
            stb.append("     T1.EXECUTIVECD, ");
            stb.append("     L1.NAME1, ");
            stb.append("     BASE.NAME, ");
            stb.append("     BASE.NAME_ENG, ");
            stb.append("     BASE.NAME_KANA, ");
            stb.append("     ADDR.TELNO ");
            stb.append(" FROM ");
            stb.append("     SCHREG_COMMITTEE_HIST_DAT T1 ");
            stb.append("     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'J002' ");
            stb.append("          AND T1.EXECUTIVECD = L1.NAMECD2 ");
            stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON T1.SCHREGNO = BASE.SCHREGNO ");
            stb.append("     INNER JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = '" + _param._year + "' ");
            stb.append("          AND REGD.SEMESTER = '" + _param._semester + "' ");
            stb.append("          AND T1.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGH ON REGD.YEAR = REGH.YEAR ");
            stb.append("          AND REGD.SEMESTER = REGH.SEMESTER ");
            stb.append("          AND REGD.GRADE = REGH.GRADE ");
            stb.append("          AND REGD.HR_CLASS = REGH.HR_CLASS ");
            if ("1".equals(_param._useClubMultiSchoolKind) && !StringUtils.isBlank(_param._SCHOOLKIND) && !StringUtils.isBlank(_param._SCHOOLCD)) {
                stb.append("   LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGH.YEAR AND GDAT.GRADE = REGH.GRADE ");
            } else {
                if (!StringUtils.isBlank(_param._schkind)) {
                    stb.append("   INNER JOIN SCHREG_REGD_GDAT GDAT ON REGD.YEAR = GDAT.YEAR AND REGD.GRADE = GDAT.GRADE AND GDAT.SCHOOL_KIND = '" + _param._schkind + "' ");
                } else if ("1".equals(_param._use_prg_schoolkind)) {
                    stb.append("   LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGH.YEAR AND GDAT.GRADE = REGH.GRADE ");
                } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOLKIND) && !StringUtils.isBlank(_param._SCHOOLCD)) {
                    stb.append("   LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGH.YEAR AND GDAT.GRADE = REGH.GRADE ");
                }
            }
            stb.append("     LEFT JOIN ( ");
            stb.append("         SELECT  ");
            stb.append("              SCHREGNO, ");
            stb.append("              TELNO  ");
            stb.append("         FROM  ");
            stb.append("              SCHREG_ADDRESS_DAT  ");
            stb.append("         WHERE  ");
            stb.append("             (SCHREGNO,ISSUEDATE) IN  ");
            stb.append("             ( SELECT  ");
            stb.append("                   SCHREGNO,MAX(ISSUEDATE)  ");
            stb.append("               FROM  ");
            stb.append("                   SCHREG_ADDRESS_DAT  ");
            stb.append("               GROUP BY ");
            stb.append("                   SCHREGNO  ");
            stb.append("               HAVING  ");
            stb.append("                   SCHREGNO IN ( ");
            stb.append("                        SELECT  ");
            stb.append("                            SCHREGNO  ");
            stb.append("                        FROM  ");
            stb.append("                            SCHREG_REGD_DAT  ");
            stb.append("                        WHERE  ");
            stb.append("                            YEAR = '" + _param._year + "'  ");
            stb.append("                            AND SEMESTER = '" + _param._semester + "'  ");
            stb.append("                   ) ");
            stb.append("             )  ");
            stb.append("     ) ADDR ON (T1.SCHREGNO = ADDR.SCHREGNO)  ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._year + "' ");
            stb.append("     AND T1.SEMESTER IN ('" + _param._semester + "', '9') ");
            stb.append("     AND T1.COMMITTEE_FLG || '-' || T1.COMMITTEECD = '" + _cd + "' ");
            if ("1".equals(_param._useClubMultiSchoolKind) && !StringUtils.isBlank(_param._SCHOOLKIND) && !StringUtils.isBlank(_param._SCHOOLCD)) {
                stb.append("   AND T1.SCHOOLCD = '" + _param._SCHOOLCD + "' ");
                stb.append("   AND T1.SCHOOL_KIND = '" + _param._SCHOOLKIND + "' ");
                stb.append("   AND GDAT.SCHOOL_KIND = '" + _param._SCHOOLKIND + "' ");
            } else {
                if ("1".equals(_param._use_prg_schoolkind)) {
                    if (!StringUtils.isBlank(_param._selectSchoolKind)) {
                        stb.append("        AND T1.SCHOOLCD = '" + _param._SCHOOLCD + "' ");
                        stb.append("        AND T1.SCHOOL_KIND IN " + _param.selectSchoolKindSql + "  ");
                    }
                } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOLKIND) && !StringUtils.isBlank(_param._SCHOOLCD)) {
                    stb.append("   AND T1.SCHOOLCD = '" + _param._SCHOOLCD + "' ");
                    stb.append("   AND T1.SCHOOL_KIND = '" + _param._SCHOOLKIND + "' ");
                }
                if ("1".equals(_param._use_prg_schoolkind)) {
                    if (!StringUtils.isBlank(_param._selectSchoolKind)) {
                        stb.append("        AND GDAT.SCHOOL_KIND IN " + _param.selectSchoolKindSql + "  ");
                    }
                } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOLKIND) && !StringUtils.isBlank(_param._SCHOOLCD)) {
                    stb.append("   AND GDAT.SCHOOL_KIND = '" + _param._SCHOOLKIND + "' ");
                }
            }
            stb.append(" ORDER BY ");
            stb.append("     REGD.GRADE, ");
            stb.append("     REGD.HR_CLASS, ");
            stb.append("     REGD.ATTENDNO ");

            return stb.toString();
        }

        /**
         * @return
         */
        private String getClassStdSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     REGD.GRADE, ");
            stb.append("     REGD.HR_CLASS, ");
            stb.append("     REGH.HR_NAME, ");
            stb.append("     REGH.HR_NAMEABBV, ");
            stb.append("     REGH.HR_CLASS_NAME2, ");
            stb.append("     REGD.ATTENDNO, ");
            stb.append("     T1.COMMITTEE_FLG || T1.COMMITTEECD AS CD, ");
            stb.append("     COMMST.COMMITTEENAME AS NAME1, ");
            stb.append("     BASE.NAME, ");
            stb.append("     BASE.NAME_ENG, ");
            stb.append("     BASE.NAME_KANA, ");
            stb.append("     ADDR.TELNO ");
            stb.append(" FROM ");
            stb.append("     SCHREG_COMMITTEE_HIST_DAT T1 ");
            stb.append("     INNER JOIN COMMITTEE_MST COMMST ON COMMST.COMMITTEE_FLG = T1.COMMITTEE_FLG ");
            stb.append("          AND COMMST.COMMITTEECD = T1.COMMITTEECD ");
            if ("1".equals(_param._useClubMultiSchoolKind) && !StringUtils.isBlank(_param._SCHOOLKIND) && !StringUtils.isBlank(_param._SCHOOLCD)) {
                stb.append("   AND COMMST.SCHOOLCD = '" + _param._SCHOOLCD + "' ");
                stb.append("   AND COMMST.SCHOOL_KIND = '" + _param._SCHOOLKIND + "' ");
            } else {
                if ("1".equals(_param._use_prg_schoolkind)) {
                    if (!StringUtils.isBlank(_param._selectSchoolKind)) {
                        stb.append("        AND COMMST.SCHOOLCD = '" + _param._SCHOOLCD + "' ");
                        stb.append("        AND COMMST.SCHOOL_KIND IN " + _param.selectSchoolKindSql + "  ");
                    }
                } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOLKIND) && !StringUtils.isBlank(_param._SCHOOLCD)) {
                    stb.append("   AND COMMST.SCHOOLCD = '" + _param._SCHOOLCD + "' ");
                    stb.append("   AND COMMST.SCHOOL_KIND = '" + _param._SCHOOLKIND + "' ");
                }
            }
            stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON T1.SCHREGNO = BASE.SCHREGNO ");
            stb.append("     INNER JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = '" + _param._year + "' ");
            stb.append("          AND REGD.SEMESTER = '" + _param._semester + "' ");
            stb.append("          AND REGD.GRADE || REGD.HR_CLASS = '" + _cd + "' ");
            stb.append("          AND T1.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGH ON REGD.YEAR = REGH.YEAR ");
            stb.append("          AND REGD.SEMESTER = REGH.SEMESTER ");
            stb.append("          AND REGD.GRADE = REGH.GRADE ");
            stb.append("          AND REGD.HR_CLASS = REGH.HR_CLASS ");
            if (!StringUtils.isBlank(_param._schkind)) {
                stb.append("     INNER JOIN SCHREG_REGD_GDAT GDAT ON REGD.YEAR = GDAT.YEAR ");
                stb.append("          AND REGD.GRADE = GDAT.GRADE ");
                stb.append("          AND GDAT.SCHOOL_KIND = '" + _param._schkind + "' ");
            }
            stb.append("     LEFT JOIN ( ");
            stb.append("         SELECT  ");
            stb.append("              SCHREGNO, ");
            stb.append("              TELNO  ");
            stb.append("         FROM  ");
            stb.append("              SCHREG_ADDRESS_DAT  ");
            stb.append("         WHERE  ");
            stb.append("             (SCHREGNO,ISSUEDATE) IN  ");
            stb.append("             ( SELECT  ");
            stb.append("                   SCHREGNO,MAX(ISSUEDATE)  ");
            stb.append("               FROM  ");
            stb.append("                   SCHREG_ADDRESS_DAT  ");
            stb.append("               GROUP BY ");
            stb.append("                   SCHREGNO  ");
            stb.append("               HAVING  ");
            stb.append("                   SCHREGNO IN ( ");
            stb.append("                        SELECT  ");
            stb.append("                            SCHREGNO  ");
            stb.append("                        FROM  ");
            stb.append("                            SCHREG_REGD_DAT  ");
            stb.append("                        WHERE  ");
            stb.append("                            YEAR = '" + _param._year + "'  ");
            stb.append("                            AND SEMESTER = '" + _param._semester + "'  ");
            stb.append("                   ) ");
            stb.append("             )  ");
            stb.append("     ) ADDR ON (T1.SCHREGNO = ADDR.SCHREGNO)  ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._year + "' ");
            stb.append("     AND T1.SEMESTER IN ('" + _param._semester + "', '9') ");
            stb.append("     AND T1.COMMITTEE_FLG = '2' ");
            if ("1".equals(_param._useClubMultiSchoolKind) && !StringUtils.isBlank(_param._SCHOOLKIND) && !StringUtils.isBlank(_param._SCHOOLCD)) {
                stb.append("   AND T1.SCHOOLCD = '" + _param._SCHOOLCD + "' ");
                stb.append("   AND T1.SCHOOL_KIND = '" + _param._SCHOOLKIND + "' ");
            } else {
                if ("1".equals(_param._use_prg_schoolkind)) {
                    if (!StringUtils.isBlank(_param._selectSchoolKind)) {
                        stb.append("        AND T1.SCHOOLCD = '" + _param._SCHOOLCD + "' ");
                        stb.append("        AND T1.SCHOOL_KIND IN " + _param.selectSchoolKindSql + "  ");
                    }
                } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOLKIND) && !StringUtils.isBlank(_param._SCHOOLCD)) {
                    stb.append("   AND T1.SCHOOLCD = '" + _param._SCHOOLCD + "' ");
                    stb.append("   AND T1.SCHOOL_KIND = '" + _param._SCHOOLKIND + "' ");
                }
            }
            stb.append(" ORDER BY ");
            stb.append("     REGD.GRADE, ");
            stb.append("     REGD.HR_CLASS, ");
            stb.append("     REGD.ATTENDNO, ");
            stb.append("     T1.COMMITTEE_FLG || T1.COMMITTEECD ");

            return stb.toString();
        }

    }

    private class Student {
        final String _schregNo;
        final String _hrName;
        final String _hrClassName2;
        final String _attendNo;
        final String _name;
        final String _nameEng;
        final String _nameKana;
        final String _tel;
        final String _executive;
        /**
         * コンストラクタ。
         */
        Student(
                final String schregNo,
                final String hrName,
                final String hrClassName2,
                final String attendNo,
                final String name,
                final String nameEng,
                final String nameKana,
                final String tel,
                final String executive
        ) {
            _schregNo = schregNo;
            _hrName = hrName;
            _hrClassName2 = hrClassName2;
            _attendNo = attendNo;
            _name = name;
            _nameEng = nameEng;
            _nameKana = nameKana;
            _tel = tel;
            _executive = executive;
        }
    }

    private int getFieldLength(final String form, final String fieldname, final int defval) {
        int length = defval;
        try {
            length = Integer.parseInt((String) getFieldStatusMap(form, fieldname).get("Keta"));
        } catch (Throwable t) {
            log.error(t);
        }
        return length;
    }

    private Map getFieldStatusMap(final String form, final String fieldname) {
        final Map m = new HashMap();
        try {
            SvfField f = (SvfField) getMappedMap(_formFieldInfoMap, form).get(fieldname);
            m.put("X", String.valueOf(f.x()));
            m.put("Y", String.valueOf(f.y()));
            m.put("Size", String.valueOf(f.size()));
            m.put("Keta", String.valueOf(f._fieldLength));
        } catch (Throwable t) {
            final String key = form + "." + fieldname;
            if (null == getMappedMap(_formFieldInfoMap, "ERROR").get(key)) {
                log.warn(" svf field not found:" + key);
                if (null == form) {
                    log.error(" form not set!");
                }
                getMappedMap(_formFieldInfoMap, "ERROR").put(key, "ERROR");
            }

        }
        return m;
    }

    private static Map getMappedMap(final Map map, final Object key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap());
        }
        return (Map) map.get(key1);
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 71440 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _semesterName;
        private final String _date;
        private final String _dataDiv;
        private final String _dataCmb;
        private final String _tel;
        private final String _documentroot;
        private final String _folder;
        private final String _extention;
        private final String _useSchool_KindField;
        private final String _SCHOOLCD;
        private final String _SCHOOLKIND;
        private final String _use_prg_schoolkind;
        private final String _selectSchoolKind;
        private final String _schkind;
        private String selectSchoolKindSql;
        private final String _z010;
        private final boolean _isSundaikoufu;
        private final String _staffCd;
        private final StaffInfo _staffInfo;
        private final String _useClubMultiSchoolKind;


        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");

            KNJ_Semester semester = new KNJ_Semester();
            KNJ_Semester.ReturnVal semreturnval = semester.Semester(db2, _year, _semester);
            _semesterName = semreturnval.val1;

            _dataDiv = request.getParameter("DATA_DIV");
            _dataCmb = request.getParameter("DATA_CMB");
            _date = request.getParameter("DATE");
            _tel = request.getParameter("TEL");
            _documentroot = request.getParameter("DOCUMENTROOT");
            KNJ_Control imagepath_extension = new KNJ_Control();            //取得クラスのインスタンス作成
            KNJ_Control.ReturnVal returnval = imagepath_extension.Control(db2);
            _folder = returnval.val4;                                           //写真データ格納フォルダ
            _extention = returnval.val5;                                          //写真データの拡張子
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLCD= request.getParameter("SCHOOLCD");
            _SCHOOLKIND = request.getParameter("SCHOOLKIND");
            _use_prg_schoolkind = request.getParameter("use_prg_schoolkind");
            _selectSchoolKind = request.getParameter("selectSchoolKind");
            if (!StringUtils.isBlank(_selectSchoolKind)) {
                StringBuffer stb = new StringBuffer("('");
                final String[] split = StringUtils.split(_selectSchoolKind, ":");
                if (null != split) {
                    for (int i = 0; i < split.length; i++) {
                        stb.append(split[i] + "', '");
                    }
                }
                selectSchoolKindSql = stb.append("')").toString();
            }
            _schkind = request.getParameter("SCHKIND");
            _z010 = getSchoolName(db2);
            _isSundaikoufu = "sundaikoufu".equals(_z010);
            _staffCd = request.getParameter("PRINT_LOG_STAFFCD");
            _staffInfo = new StaffInfo(db2, _staffCd);
            _useClubMultiSchoolKind = request.getParameter("useClubMultiSchoolKind");
        }


        private String getSchoolName(final DB2UDB db2) {
            String schoolName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    schoolName = rs.getString("NAME1");
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return schoolName;
        }
    }
}  //クラスの括り

