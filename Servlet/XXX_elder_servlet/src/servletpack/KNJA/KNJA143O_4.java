// kanji=漢字
/*
 * 作成日: 2017/08/14 10:24:21 - JST 作成者: nakamoto
 *
 * Copyright(C) 2004-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * <<クラスの説明>>。
 *
 * @author nakamoto
 */
public class KNJA143O_4 {

    private static final Log log = LogFactory.getLog(KNJA143O_4.class);

    private boolean _hasData;

    private Param _param;

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        try {
            log.fatal("$Revision: 71179 $");
            KNJServletUtils.debugParam(request, log);

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final List studentList = Student.getStudentList(db2, _param);

        final String form = "KNJA143O_4.frm";
        svf.VrSetForm(form, 1);

        int cnt = 1;
        for (int j = 0; j < studentList.size(); j++) {

            if ((j != 0) && (j % 4 == 0)) {
                svf.VrEndPage();
                svf.VrSetForm(form, 1);
                cnt = 1;
            }

            final Student student = (Student) studentList.get(j);

            final String addr = StringUtils.defaultString(student._addr1) + StringUtils.defaultString(student._addr2);
            final int ketaAddr = KNJ_EditEdit.getMS932ByteLength(addr);
            svf.VrsOutn("ADDR" + (ketaAddr <= 50 ? "1" : ketaAddr <= 60 ? "2" : ketaAddr <= 70 ? "3" : "4"), cnt, addr); // 住所
            svf.VrsOutn("YEAR", cnt, _param._year); // 住所

            final int ketaName = KNJ_EditEdit.getMS932ByteLength(student._name);
            svf.VrsOutn("NAME" + (ketaName <= 20 ? "1" : ketaName <= 30 ? "2" : "3"), cnt, student._name); // 氏名

            svf.VrsOutn("LIMIT", cnt, KNJ_EditDate.h_format_SeirekiJP(_param._limitDateFrom) + "から" + KNJ_EditDate.h_format_SeirekiJP(_param._limitDate) + "まで有効"); // 発行日
            svf.VrsOutn("SCHREGNO", cnt, student._schregno); // 学籍番号
            svf.VrsOutn("SCHOOL_NAME", cnt, StringUtils.defaultString(_param._schoolname) + StringUtils.defaultString(_param._jobname)); // 学校名

            int chkCnt = 1;
            if (printandchkrosen(svf, student._josya1, student._gesya1, chkCnt, cnt))
                chkCnt++;
            if (printandchkrosen(svf, student._josya2, student._gesya2, chkCnt, cnt))
                chkCnt++;
            if (printandchkrosen(svf, student._josya3, student._gesya3, chkCnt, cnt))
                chkCnt++;
            if (printandchkrosen(svf, student._josya4, student._gesya4, chkCnt, cnt))
                chkCnt++;
            if (printandchkrosen(svf, student._josya5, student._gesya5, chkCnt, cnt))
                chkCnt++;
            if (printandchkrosen(svf, student._josya6, student._gesya6, chkCnt, cnt))
                chkCnt++;
            if (printandchkrosen(svf, student._josya7, student._gesya7, chkCnt, cnt))
                chkCnt++;

            cnt += 1;

            _hasData = true;

        }
        svf.VrEndPage();
    }

    private boolean printandchkrosen(final Vrw32alp svf, final String jyosya, final String gesya, final int chkCnt, final int cnt) {
        if ("".equals(StringUtils.defaultString(jyosya, "")) && "".equals(StringUtils.defaultString(gesya, ""))) {
            return false;
        } else {
            if (!"".equals(StringUtils.defaultString(jyosya, ""))) {
                final int ketaName = KNJ_EditEdit.getMS932ByteLength(jyosya);
                svf.VrsOutn("SECTION" + chkCnt + "_1_" + (ketaName <= 16 ? "1" : ketaName <= 24 ? "2" : "3"), cnt, jyosya); // 乗車
            }
            if (!"".equals(StringUtils.defaultString(gesya, ""))) {
                final int ketaName = KNJ_EditEdit.getMS932ByteLength(gesya);
                svf.VrsOutn("SECTION" + chkCnt + "_2_" + (ketaName <= 16 ? "1" : ketaName <= 24 ? "2" : "3"), cnt, gesya); // 乗車
            }
        }
        return true;
    }

    private static class Student {
        String _schregno;
        String _grade;
        String _hrClass;
        String _attendno;
        String _schoolKind;
        String _gradeCd;
        String _gradeName1;
        String _name;
        String _zipcd;
        String _addr1;
        String _addr2;
        String _josya1;
        String _josya2;
        String _josya3;
        String _josya4;
        String _josya5;
        String _josya6;
        String _josya7;
        String _gesya1;
        String _gesya2;
        String _gesya3;
        String _gesya4;
        String _gesya5;
        String _gesya6;
        String _gesya7;

        public static List getStudentList(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            final String sql = sql(param);
            log.debug(" sql = " + sql);
            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
                final Student student = new Student();
                final Map map = (Map) it.next();
                student._schregno = KnjDbUtils.getString(map, "SCHREGNO");
                student._grade = KnjDbUtils.getString(map, "GRADE");
                student._hrClass = KnjDbUtils.getString(map, "HR_CLASS");
                student._attendno = KnjDbUtils.getString(map, "ATTENDNO");
                student._schoolKind = KnjDbUtils.getString(map, "SCHOOL_KIND");
                student._gradeCd = KnjDbUtils.getString(map, "GRADE_CD");
                student._gradeName1 = KnjDbUtils.getString(map, "GRADE_NAME1");
                student._name = KnjDbUtils.getString(map, "NAME");
                student._zipcd = KnjDbUtils.getString(map, "ZIPCD");
                student._addr1 = KnjDbUtils.getString(map, "ADDR1");
                student._addr2 = KnjDbUtils.getString(map, "ADDR2");
                student._josya1 = KnjDbUtils.getString(map, "JOSYA_1");
                student._josya2 = KnjDbUtils.getString(map, "JOSYA_2");
                student._josya3 = KnjDbUtils.getString(map, "JOSYA_3");
                student._josya4 = KnjDbUtils.getString(map, "JOSYA_4");
                student._josya5 = KnjDbUtils.getString(map, "JOSYA_5");
                student._josya6 = KnjDbUtils.getString(map, "JOSYA_6");
                student._josya7 = KnjDbUtils.getString(map, "JOSYA_7");
                student._gesya1 = KnjDbUtils.getString(map, "GESYA_1");
                student._gesya2 = KnjDbUtils.getString(map, "GESYA_2");
                student._gesya3 = KnjDbUtils.getString(map, "GESYA_3");
                student._gesya4 = KnjDbUtils.getString(map, "GESYA_4");
                student._gesya5 = KnjDbUtils.getString(map, "GESYA_5");
                student._gesya6 = KnjDbUtils.getString(map, "GESYA_6");
                student._gesya7 = KnjDbUtils.getString(map, "GESYA_7");

                list.add(student);
            }
            return list;
        }

        private static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     BASE.SCHREGNO, ");
            stb.append("     REGD.GRADE, ");
            stb.append("     REGD.HR_CLASS, ");
            stb.append("     REGD.ATTENDNO, ");
            stb.append("     REGDG.SCHOOL_KIND, ");
            stb.append("     REGDG.GRADE_CD, ");
            stb.append("     REGDG.GRADE_NAME1, ");
            stb.append("     BASE.NAME, ");
            stb.append("     ADDR.ZIPCD, ");
            stb.append("     ADDR.ADDR1, ");
            stb.append("     ADDR.ADDR2, ");
            stb.append("     CASE WHEN ENVR.FLG_1 = '1' THEN J1.STATION_NAME ELSE ENVR.JOSYA_1 END AS JOSYA_1, ");
            stb.append("     CASE WHEN ENVR.FLG_2 = '1' THEN J2.STATION_NAME ELSE ENVR.JOSYA_2 END AS JOSYA_2, ");
            stb.append("     CASE WHEN ENVR.FLG_3 = '1' THEN J3.STATION_NAME ELSE ENVR.JOSYA_3 END AS JOSYA_3, ");
            stb.append("     CASE WHEN ENVR.FLG_4 = '1' THEN J4.STATION_NAME ELSE ENVR.JOSYA_4 END AS JOSYA_4, ");
            stb.append("     CASE WHEN ENVR.FLG_5 = '1' THEN J5.STATION_NAME ELSE ENVR.JOSYA_5 END AS JOSYA_5, ");
            stb.append("     CASE WHEN ENVR.FLG_6 = '1' THEN J6.STATION_NAME ELSE ENVR.JOSYA_6 END AS JOSYA_6, ");
            stb.append("     CASE WHEN ENVR.FLG_7 = '1' THEN J7.STATION_NAME ELSE ENVR.JOSYA_7 END AS JOSYA_7, ");
            stb.append("     CASE WHEN ENVR.FLG_1 = '1' THEN J1.LINE_NAME    ELSE ENVR.ROSEN_1 END AS ROSEN_1, ");
            stb.append("     CASE WHEN ENVR.FLG_2 = '1' THEN J2.LINE_NAME    ELSE ENVR.ROSEN_2 END AS ROSEN_2, ");
            stb.append("     CASE WHEN ENVR.FLG_3 = '1' THEN J3.LINE_NAME    ELSE ENVR.ROSEN_3 END AS ROSEN_3, ");
            stb.append("     CASE WHEN ENVR.FLG_4 = '1' THEN J4.LINE_NAME    ELSE ENVR.ROSEN_4 END AS ROSEN_4, ");
            stb.append("     CASE WHEN ENVR.FLG_5 = '1' THEN J5.LINE_NAME    ELSE ENVR.ROSEN_5 END AS ROSEN_5, ");
            stb.append("     CASE WHEN ENVR.FLG_6 = '1' THEN J6.LINE_NAME    ELSE ENVR.ROSEN_6 END AS ROSEN_6, ");
            stb.append("     CASE WHEN ENVR.FLG_7 = '1' THEN J7.LINE_NAME    ELSE ENVR.ROSEN_7 END AS ROSEN_7, ");
            stb.append("     CASE WHEN ENVR.FLG_1 = '1' THEN G1.STATION_NAME ELSE ENVR.GESYA_1 END AS GESYA_1, ");
            stb.append("     CASE WHEN ENVR.FLG_2 = '1' THEN G2.STATION_NAME ELSE ENVR.GESYA_2 END AS GESYA_2, ");
            stb.append("     CASE WHEN ENVR.FLG_3 = '1' THEN G3.STATION_NAME ELSE ENVR.GESYA_3 END AS GESYA_3, ");
            stb.append("     CASE WHEN ENVR.FLG_4 = '1' THEN G4.STATION_NAME ELSE ENVR.GESYA_4 END AS GESYA_4, ");
            stb.append("     CASE WHEN ENVR.FLG_5 = '1' THEN G5.STATION_NAME ELSE ENVR.GESYA_5 END AS GESYA_5, ");
            stb.append("     CASE WHEN ENVR.FLG_6 = '1' THEN G6.STATION_NAME ELSE ENVR.GESYA_6 END AS GESYA_6, ");
            stb.append("     CASE WHEN ENVR.FLG_7 = '1' THEN G7.STATION_NAME ELSE ENVR.GESYA_7 END AS GESYA_7 ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT REGD ");
            stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     LEFT JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = REGD.YEAR ");
            stb.append("         AND REGDG.GRADE = REGD.GRADE ");
            stb.append("     LEFT JOIN MAJOR_MST MAJ ON MAJ.COURSECD = REGD.COURSECD AND MAJ.MAJORCD = REGD.MAJORCD ");
            stb.append("     LEFT JOIN ( ");
            stb.append("         SELECT ");
            stb.append("             SCHREGNO, ");
            stb.append("             MAX(ISSUEDATE) AS ISSUEDATE ");
            stb.append("         FROM ");
            stb.append("             SCHREG_ADDRESS_DAT ");
            stb.append("         GROUP BY ");
            stb.append("             SCHREGNO ");
            stb.append("     ) ADDR_MAX ON ADDR_MAX.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     LEFT JOIN SCHREG_ADDRESS_DAT ADDR ON ADDR.SCHREGNO = ADDR_MAX.SCHREGNO ");
            stb.append("         AND ADDR.ISSUEDATE = ADDR_MAX.ISSUEDATE ");
            stb.append("     LEFT JOIN SCHREG_ENVIR_DAT ENVR ON ENVR.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     LEFT JOIN STATION_NETMST J1 ON J1.STATION_CD = ENVR.JOSYA_1 ");
            stb.append("     LEFT JOIN STATION_NETMST J2 ON J2.STATION_CD = ENVR.JOSYA_2 ");
            stb.append("     LEFT JOIN STATION_NETMST J3 ON J3.STATION_CD = ENVR.JOSYA_3 ");
            stb.append("     LEFT JOIN STATION_NETMST J4 ON J4.STATION_CD = ENVR.JOSYA_4 ");
            stb.append("     LEFT JOIN STATION_NETMST J5 ON J5.STATION_CD = ENVR.JOSYA_5 ");
            stb.append("     LEFT JOIN STATION_NETMST J6 ON J6.STATION_CD = ENVR.JOSYA_6 ");
            stb.append("     LEFT JOIN STATION_NETMST J7 ON J7.STATION_CD = ENVR.JOSYA_7 ");
            stb.append("     LEFT JOIN STATION_NETMST G1 ON G1.STATION_CD = ENVR.GESYA_1 ");
            stb.append("     LEFT JOIN STATION_NETMST G2 ON G2.STATION_CD = ENVR.GESYA_2 ");
            stb.append("     LEFT JOIN STATION_NETMST G3 ON G3.STATION_CD = ENVR.GESYA_3 ");
            stb.append("     LEFT JOIN STATION_NETMST G4 ON G4.STATION_CD = ENVR.GESYA_4 ");
            stb.append("     LEFT JOIN STATION_NETMST G5 ON G5.STATION_CD = ENVR.GESYA_5 ");
            stb.append("     LEFT JOIN STATION_NETMST G6 ON G6.STATION_CD = ENVR.GESYA_6 ");
            stb.append("     LEFT JOIN STATION_NETMST G7 ON G7.STATION_CD = ENVR.GESYA_7 ");
            stb.append(" WHERE ");
            stb.append("     REGD.YEAR = '" + param._year + "' ");
            stb.append("     AND REGD.SEMESTER = '" + param._semester + "' ");
            if ("1".equals(param._disp)) {
                stb.append("     AND REGD.GRADE || REGD.HR_CLASS IN " + SQLUtils.whereIn(true, param._category_selected) + " ");
            } else if ("2".equals(param._disp)) {
                stb.append("     AND REGD.SCHREGNO IN " + SQLUtils.whereIn(true, param._category_selected) + " ");
            }
            stb.append(" ORDER BY ");
            stb.append("     REGD.GRADE, ");
            stb.append("     REGD.HR_CLASS, ");
            stb.append("     REGD.ATTENDNO ");
            return stb.toString();
        }

    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _year;
        final String _semester;
        final String _disp; // 1:クラス 2:個人
        final String[] _category_selected;
        String _limitDateFrom;
        String _limitDate;

        private String _jobname;
        private String _principalName;
        private String _schoolname;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("SEMESTER");
            _disp = request.getParameter("DISP");
            _category_selected = request.getParameterValues("CATEGORY_SELECTED");

            if (null != _category_selected) {
                for (int i = 0; i < _category_selected.length; i++) {
                    _category_selected[i] = StringUtils.split(_category_selected[i], "-")[0];
                }
            }
            if (null != request.getParameter("LIMIT_DATE_FROM")) {
                _limitDateFrom = StringUtils.defaultString(request.getParameter("LIMIT_DATE_FROM")).replace('/', '-');
            }
            if (null != request.getParameter("LIMIT_DATE")) {
                _limitDate = StringUtils.defaultString(request.getParameter("LIMIT_DATE")).replace('/', '-');
            }
            loadCertifSchoolDat(db2);
        }

        public void loadCertifSchoolDat(final DB2UDB db2) {

            final Map map = KnjDbUtils.firstRow(KnjDbUtils.query(db2, "SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '101'"));
            _jobname = KnjDbUtils.getString(map, "JOB_NAME");
            _principalName = KnjDbUtils.getString(map, "PRINCIPAL_NAME");
            _schoolname = KnjDbUtils.getString(map, "SCHOOL_NAME");
        }
    }
}

// eof
