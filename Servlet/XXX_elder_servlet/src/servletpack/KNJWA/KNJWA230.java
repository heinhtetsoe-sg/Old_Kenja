// kanji=漢字
/*
 * $Id: 9c3cc38870d2f87e64f92912a154d7c52999f6b1 $
 *
 * 作成日: 2008/06/19 11:27:45 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2008-2012 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJWA;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_SchoolinfoSql;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 9c3cc38870d2f87e64f92912a154d7c52999f6b1 $
 */
public class KNJWA230 {

    private static final Log log = LogFactory.getLog(KNJWA230.class);

    Param _param;

    /**
     * KNJD.classから最初に起動されるクラス
     */
    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Vrw32alp svf = new Vrw32alp(); // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null; // Databaseクラスを継承したクラス
        boolean hasData = false; // 該当データなしフラグ
        KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2(); // 帳票におけるＳＶＦおよびＤＢ２の設定

        // ＤＢ接続
        db2 = sd.setDb(request);
        if (sd.openDb(db2)) {
            log.error("db open error");
            return;
        }

        // パラメータの取得
        _param = createParam(db2, request);

        // print svf設定
        sd.setSvfInit(request, response, svf);

        // 印刷処理
        hasData = printSvf(db2, svf);

        // 終了処理
        sd.closeSvf(svf, hasData);
        sd.closeDb(db2);
    }

    /**
     * @param db2
     * @param svf
     * @return
     */
    private boolean printSvf(DB2UDB db2, Vrw32alp svf) throws Exception {
        boolean hasData = false;
        for (final Iterator iter = _param._studentList.iterator(); iter.hasNext();) {

            final Student student = (Student) iter.next();
            log.debug(student);

            if (_param._seito) {
                KNJWA230Youroku objYouroku = new KNJWA230Youroku();
                hasData = objYouroku.printData(db2, svf, _param, student) ? true : hasData;
            }
            if (_param._tani) {
                KNJWA230Tani objTani = new KNJWA230Tani();
                hasData = objTani.printData(db2, svf, _param, student) ? true : hasData;
            }
            if (_param._gakushu) {
                KNJWA230Gakushu objGakushu = new KNJWA230Gakushu();
                hasData = objGakushu.printData(db2, svf, _param, student) ? true : hasData;
            }
            if (_param._katsudo) {
                KNJWA230Katsudo objKatsudo = new KNJWA230Katsudo();
                hasData = objKatsudo.printData(db2, svf, _param, student) ? true : hasData;
            }
        }
        return hasData;
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
    class Param {
        final String _year;
        final String _semester;
        final String _date;
        final String _loginDate;
        final String _grade;
        final List _studentList;
        final boolean _seito;
        final boolean _simei;
        final boolean _tani;
        final boolean _gakushu;
        final boolean _katsudo;
        final boolean _isMirisyuPrint;
        final Map _certifMap;
        final Map _w029Map;
        String _schoolName;
        String _schoolAddr1;
        String _schoolAddr2;

        private boolean _seirekiFlg;
        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _date = request.getParameter("DATE");
            _loginDate = request.getParameter("LOGIN_DATE");
            _grade = request.getParameter("GRADE");
            _seito = getFlg(request.getParameter("SEITO"));
            _simei = getFlg(request.getParameter("SIMEI"));
            _tani = getFlg(request.getParameter("TANI"));
            _gakushu = getFlg(request.getParameter("GAKUSHU"));
            _katsudo = getFlg(request.getParameter("KATSUDO"));
            _isMirisyuPrint = request.getParameter("MIRISYU").equals("1") ? true : false;

            final String[] category_selected = request.getParameterValues("CATEGORY_SELECTED");
            _studentList = getStudentList(db2, _year, _semester, category_selected);

            setSchoolInfo(db2, _year);
            _certifMap = getCertifInfo(db2, _year);
            _w029Map = getW029Map(db2);
            setSeirekiFlg(db2);
        }

        private boolean getFlg(final String parameter) {
            return null != parameter;
        }

        /**
         * @param db2
         * @param category_selected
         * @return
         */
        private List getStudentList(final DB2UDB db2, final String year, final String semester, final String[] category_selected) throws SQLException {
            final List rtnList = new ArrayList();
            ResultSet rs = null;
            PreparedStatement ps = null;
            try {
                ps = db2.prepareStatement(getStudentInfoSql(year, semester));
                for (int i = 0; i < category_selected.length; i++) {
                    int psCnt = 1;
                    ps.setString(psCnt++, category_selected[i]);
                    ps.setString(psCnt++, category_selected[i]);
                    ps.setString(psCnt++, category_selected[i]);
                    rs = ps.executeQuery();
                    if (rs.next()) {
                        Student student = new Student(rs.getString("SCHREGNO"),
                                                      rs.getString("APPLICANTNO"),
                                                      rs.getString("GRADE"),
                                                      rs.getString("HR_CLASS"),
                                                      rs.getString("ATTENDNO"),
                                                      rs.getString("HR_NAME"),
                                                      rs.getString("HR_NAMEABBV"),
                                                      rs.getString("NAME"),
                                                      rs.getString("NAME_KANA"),
                                                      rs.getString("SEX"),
                                                      rs.getString("BIRTHDAY"),
                                                      rs.getString("COURSECD"),
                                                      rs.getString("COURSENAME"),
                                                      rs.getString("MAJORCD"),
                                                      rs.getString("MAJORNAME"),
                                                      rs.getString("COURSECODE"),
                                                      rs.getString("COURSECODENAME"),
                                                      rs.getString("FINISH_DATE"),
                                                      rs.getString("FINSCHOOL"),
                                                      rs.getString("ZIPCD"),
                                                      rs.getString("PREF_CD"),
                                                      rs.getString("PREF_NAME"),
                                                      rs.getString("ADDR1"),
                                                      rs.getString("ADDR2"),
                                                      rs.getString("ADDR3"),
                                                      rs.getString("GNAME"),
                                                      rs.getString("GKANA"),
                                                      rs.getString("GZIP"),
                                                      rs.getString("GPREF"),
                                                      rs.getString("GPREF_NAME"),
                                                      rs.getString("GADDR1"),
                                                      rs.getString("GADDR2"),
                                                      rs.getString("GADDR3"),
                                                      rs.getString("OLD_CURRICULUM"));
                        student.setEntGrd(db2, "ENT");
                        student.setEntGrd(db2, "GRD");
                        student.setAftGrd(db2);
                        student.setRegdHist(db2);
                        student.setTransferHist(db2);
                        rtnList.add(student);
                    }
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtnList;
        }

        private void setSchoolInfo(final DB2UDB db2, final String year) throws SQLException {
            ResultSet rs = null;
            PreparedStatement ps1 = null;
            try {
                KNJ_SchoolinfoSql obj_SchoolinfoSql = new KNJ_SchoolinfoSql("10000");
                ps1 = db2.prepareStatement(obj_SchoolinfoSql.pre_sql());
                int p = 0;
                ps1.setString(++p, year);
                ps1.setString(++p, year);
                rs = ps1.executeQuery();
                String schoolName = "";
                String schoolAddr1 = "";
                String schoolAddr2 = "";
                if (rs.next()) {
                    schoolName = rs.getString("SCHOOLNAME1") == null ? "" : rs.getString("SCHOOLNAME1");
                    schoolAddr1 = rs.getString("SCHOOLADDR1") == null ? "" : rs.getString("SCHOOLADDR1");
                    schoolAddr2 = rs.getString("SCHOOLADDR2") == null ? "" : rs.getString("SCHOOLADDR2");
                }
                _schoolName = schoolName;
                _schoolAddr1 = schoolAddr1;
                _schoolAddr2 = schoolAddr2;
            } finally {
                DbUtils.closeQuietly(null, ps1, rs);
                db2.commit();
            }
        }

        private Map getCertifInfo(final DB2UDB db2, final String year) throws SQLException {
            final Map retMap = new TreeMap();
            ResultSet rsCertif = null;
            String sqlCertif = "SELECT * FROM CERTIF_SCHOOL_DAT " + "WHERE CERTIF_KINDCD = '107' ORDER BY YEAR";
            try {
                db2.query(sqlCertif);
                rsCertif = db2.getResultSet();
                while (rsCertif.next()) {
                    retMap.put(rsCertif.getString("YEAR"), new CertifSchool(rsCertif.getString("SYOSYO_NAME"),
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
                                                     rsCertif.getString("REMARK10")));
                }
            } finally {
                DbUtils.closeQuietly(rsCertif);
                db2.commit();
            }
            return retMap;
        }

        private Map getW029Map(final DB2UDB db2) throws SQLException {
            final Map retMap = new TreeMap();
            ResultSet rs = null;
            String sql = "SELECT * FROM V_NAME_MST WHERE NAMECD1 = 'W029' ORDER BY YEAR, NAMECD1 ";
            try {
                db2.query(sql);
                rs = db2.getResultSet();
                Map setNamecd2Map = new HashMap();
                String befYear = "";
                while (rs.next()) {
                    if (!befYear.equals(rs.getString("YEAR"))) {
                        setNamecd2Map = new HashMap();
                    }
                    setNamecd2Map.put(rs.getString("NAME1"), rs.getString("NAMECD2"));
                    retMap.put(rs.getString("YEAR"), setNamecd2Map);
                    befYear = rs.getString("YEAR");
                }
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
            return retMap;
        }

        private void setSeirekiFlg(final DB2UDB db2) {
            try {
                _seirekiFlg = false;
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                while( rs.next() ){
                    if (rs.getString("NAME1").equals("2")) _seirekiFlg = true; //西暦
                }
                ps.close();
                rs.close();
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
            }
        }

        public String changePrintDate(final String date) {
            if (null != date) {
                if (_seirekiFlg) {
                    return date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date);
                } else {
                    return KNJ_EditDate.h_format_JP(date);
                }
            } else {
                return "";
            }
        }

        public String changePrintYear(final String year, final boolean plusNendo) {
            final String nendo = plusNendo ? "年度" : "";
            if (_seirekiFlg) {
                return year + nendo;
            } else {
                return nao_package.KenjaProperties.gengou(Integer.parseInt(year)) + nendo;
            }
        }

        public boolean isW029(final String year, final String subclassCd) {
            if (!_w029Map.containsKey(year)) {
                return false;
            }
            final Map setNamecd2Map = (Map) _w029Map.get(year);
            return setNamecd2Map.containsKey(subclassCd);
        }
        /**
         * 学習記録データ(全て)の SQL SELECT 文を戻します。
         * 
         * @return
         */
        public String getStudyRecSql(final String selectDiv, final String yearInstate) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" WITH STUDY_REC AS ( ");
            stb.append(" SELECT ");
            stb.append("     'STUDY' AS DATA_DIV, ");
            stb.append("     CASE WHEN VALUE(T1.GET_CREDIT, 0) > 0 ");
            stb.append("          THEN '0' ");
            stb.append("          ELSE '1' ");
            stb.append("     END AS MIRISYU, ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     CASE WHEN T1.CLASSNAME IS NOT NULL ");
            stb.append("          THEN T1.CLASSNAME ");
            stb.append("          ELSE CASE WHEN L1.CLASSORDERNAME1 IS NOT NULL ");
            stb.append("               THEN L1.CLASSORDERNAME1 ");
            stb.append("               ELSE L1.CLASSNAME ");
            stb.append("         END ");
            stb.append("     END AS CLASSNAME, ");
            stb.append("     CASE WHEN L1.SHOWORDER2 IS NOT NULL ");
            stb.append("          THEN L1.SHOWORDER2 ");
            stb.append("          ELSE 999 ");
            stb.append("     END AS CLASSORDER2, ");
            stb.append("     T1.CURRICULUM_CD, ");
            stb.append("     L2.SUBCLASSCD2, ");
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     CASE WHEN T1.SUBCLASSNAME IS NOT NULL ");
            stb.append("          THEN T1.SUBCLASSNAME ");
            stb.append("          ELSE CASE WHEN L2.SUBCLASSORDERNAME1 IS NOT NULL ");
            stb.append("               THEN L2.SUBCLASSORDERNAME1 ");
            stb.append("               ELSE L2.SUBCLASSNAME ");
            stb.append("         END ");
            stb.append("     END AS SUBCLASSNAME, ");
            stb.append("     CASE WHEN L2.SHOWORDER2 IS NOT NULL ");
            stb.append("          THEN L2.SHOWORDER2 ");
            stb.append("          ELSE 999999 ");
            stb.append("     END AS SUBCLASSORDER2, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.SCHOOLCD, ");
            stb.append("     T1.YEAR, ");
            stb.append("     '' AS FORMER_REG_SCHOOLCD, ");
            stb.append("     T1.GET_CREDIT AS GET_CREDIT, ");
            stb.append("     '' AS REMARK, ");
            stb.append("     T1.VALUATION AS VALUATION ");
            stb.append(" FROM ");
            stb.append("     SCHREG_STUDYREC_DAT T1 ");
            stb.append("     LEFT JOIN CLASS_MST L1 ON T1.CLASSCD = L1.CLASSCD ");
            stb.append("     LEFT JOIN SUBCLASS_MST L2 ON T1.CLASSCD = L2.CLASSCD ");
            stb.append("          AND T1.CURRICULUM_CD = L2.CURRICULUM_CD ");
            stb.append("          AND T1.SUBCLASSCD = L2.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     T1.SCHREGNO = ? ");
            if (null != yearInstate) {
                stb.append("     AND T1.YEAR IN (" + yearInstate + ") ");
            }
            if (!_isMirisyuPrint) {
                stb.append("     AND VALUE(T1.GET_CREDIT, 0) > 0 ");
            }
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     'ANOTHER' AS DATA_DIV, ");
            stb.append("     CASE WHEN VALUE(T1.GET_CREDIT, 0) > 0 ");
            stb.append("          THEN '0' ");
            stb.append("          ELSE '1' ");
            stb.append("     END AS MIRISYU, ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     CASE WHEN L1.CLASSORDERNAME1 IS NOT NULL ");
            stb.append("          THEN L1.CLASSORDERNAME1 ");
            stb.append("          ELSE L1.CLASSNAME ");
            stb.append("     END AS CLASSNAME, ");
            stb.append("     CASE WHEN L1.SHOWORDER2 IS NOT NULL ");
            stb.append("          THEN L1.SHOWORDER2 ");
            stb.append("          ELSE 999 ");
            stb.append("     END AS CLASSORDER2, ");
            stb.append("     T1.CURRICULUM_CD, ");
            stb.append("     L2.SUBCLASSCD2, ");
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     CASE WHEN T1.SUBCLASSNAME IS NOT NULL ");
            stb.append("          THEN T1.SUBCLASSNAME ");
            stb.append("          ELSE CASE WHEN L2.SUBCLASSORDERNAME1 IS NOT NULL ");
            stb.append("               THEN L2.SUBCLASSORDERNAME1 ");
            stb.append("               ELSE L2.SUBCLASSNAME ");
            stb.append("         END ");
            stb.append("     END AS SUBCLASSNAME, ");
            stb.append("     CASE WHEN L2.SHOWORDER2 IS NOT NULL ");
            stb.append("          THEN L2.SHOWORDER2 ");
            stb.append("          ELSE 999 ");
            stb.append("     END AS SUBCLASSORDER2, ");
            stb.append("     L3.SCHREGNO, ");
            stb.append("     CAST(CAST(T1.GET_METHOD AS SMALLINT) + 1 AS CHAR(1)) AS SCHOOLCD, ");
            stb.append("     T1.YEAR, ");
            stb.append("     VALUE(L4.NAME, '') AS FORMER_REG_SCHOOLCD, ");
            stb.append("     T1.GET_CREDIT, ");
            stb.append("     T1.REMARK, ");
            stb.append("     T1.VALUATION ");
            stb.append(" FROM ");
            stb.append("     ANOTHER_SCHOOL_GETCREDITS_DAT T1 ");
            stb.append("     LEFT JOIN CLASS_MST L1 ON T1.CLASSCD = L1.CLASSCD ");
            stb.append("     LEFT JOIN SUBCLASS_MST L2 ON T1.CLASSCD = L2.CLASSCD ");
            stb.append("          AND T1.CURRICULUM_CD = L2.CURRICULUM_CD ");
            stb.append("          AND T1.SUBCLASSCD = L2.SUBCLASSCD ");
            stb.append("     LEFT JOIN SCHREG_BASE_MST L3 ON T1.APPLICANTNO = L3.APPLICANTNO ");
            stb.append("     LEFT JOIN FIN_HIGH_SCHOOL_MST L4 ON T1.FORMER_REG_SCHOOLCD = L4.SCHOOL_CD ");
            stb.append(" WHERE ");
            stb.append("     L3.SCHREGNO = ? ");
            if (null != yearInstate) {
                stb.append("     AND T1.YEAR IN (" + yearInstate + ") ");
            }
            if (!_isMirisyuPrint) {
                stb.append("     AND ( ");
                stb.append("          (CAST(CAST(T1.GET_METHOD AS SMALLINT) + 1 AS CHAR(1)) = '2' ");
                stb.append("           OR ");
                stb.append("           CAST(CAST(T1.GET_METHOD AS SMALLINT) + 1 AS CHAR(1)) = '3' ");
                stb.append("          ) ");
                stb.append("          OR ");
                stb.append("          ( ");
                stb.append("           VALUE(T1.GET_CREDIT, 0) > 0 ");
                stb.append("          ) ");
                stb.append("         ) ");
            }
            stb.append(" ), ANNUAL_T AS ( ");
            stb.append(" SELECT ");
            stb.append("     row_number() over (order by T1.YEAR) AS ANNUAL, ");
            stb.append("     T1.YEAR ");
            stb.append(" FROM ");
            stb.append("     STUDY_REC T1 ");
            stb.append(" GROUP BY ");
            stb.append("     T1.YEAR ");

            stb.append(" ), CREDIT_T AS ( ");
            stb.append(" SELECT ");
            stb.append("     CASE WHEN L2.CLASSCD IS NOT NULL ");
            stb.append("          THEN L2.CLASSCD ");
            stb.append("          ELSE T1.CLASSCD ");
            stb.append("     END AS CLASSCD, ");
            stb.append("     CASE WHEN L2.CLASSNAME IS NOT NULL ");
            stb.append("          THEN L2.CLASSNAME ");
            stb.append("          ELSE T1.CLASSNAME ");
            stb.append("     END AS CLASSNAME, ");
            stb.append("     CASE WHEN L2.SHOWORDER2 IS NOT NULL ");
            stb.append("          THEN L2.SHOWORDER2 ");
            stb.append("          ELSE T1.CLASSORDER2 ");
            stb.append("     END AS CLASSORDER2, ");
            stb.append("     MAX(T1.CURRICULUM_CD) AS CURRICULUM_CD, ");
            stb.append("     CASE WHEN L1.SUBCLASSCD IS NOT NULL ");
            stb.append("          THEN L1.SUBCLASSCD ");
            stb.append("          ELSE T1.SUBCLASSCD ");
            stb.append("     END AS SUBCLASSCD, ");
            stb.append("     CASE WHEN L1.SUBCLASSORDERNAME1 IS NOT NULL ");
            stb.append("          THEN L1.SUBCLASSORDERNAME1 ");
            stb.append("          ELSE CASE WHEN L1.SUBCLASSNAME IS NOT NULL ");
            stb.append("               THEN L1.SUBCLASSNAME ");
            stb.append("               ELSE T1.SUBCLASSNAME ");
            stb.append("               END ");
            stb.append("     END AS SUBCLASSNAME, ");
            stb.append("     CASE WHEN L1.SHOWORDER2 IS NOT NULL ");
            stb.append("          THEN L1.SHOWORDER2 ");
            stb.append("          ELSE T1.SUBCLASSORDER2 ");
            stb.append("     END AS SUBCLASSORDER2, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     L3.ANNUAL, ");
            stb.append("     T1.YEAR, ");
            stb.append("     SUM(CASE WHEN T1.MIRISYU = '1' ");
            stb.append("              THEN CASE WHEN T1.DATA_DIV = 'STUDY' AND VALUE(T1.GET_CREDIT, 0) = 0 ");
            stb.append("                        THEN L4.CREDITS ");
            stb.append("                        ELSE T1.GET_CREDIT ");
            stb.append("                   END ");
            stb.append("              ELSE 0 ");
            stb.append("         END ");
            stb.append("     ) AS MIRI_CREDIT, ");
            stb.append("     SUM(CASE WHEN T1.MIRISYU = '0' ");
            stb.append("              THEN T1.GET_CREDIT ");
            stb.append("              ELSE 0 ");
            stb.append("         END ");
            stb.append("     ) AS RISYU_CREDIT, ");
            stb.append("     SUM(CASE WHEN T1.DATA_DIV = 'STUDY' THEN 1 ELSE 0 END) AS STUDY_CNT, ");
            stb.append("     MAX(T1.MIRISYU) AS MAX_MIRI, ");
            stb.append("     MIN(T1.MIRISYU) AS MIN_MIRI ");
            stb.append(" FROM ");
            stb.append("     STUDY_REC T1 ");
            stb.append("     LEFT JOIN SUBCLASS_MST L1 ON T1.CLASSCD = L1.CLASSCD ");
            stb.append("          AND T1.CURRICULUM_CD = L1.CURRICULUM_CD ");
            stb.append("          AND T1.SUBCLASSCD2 = L1.SUBCLASSCD ");
            stb.append("     LEFT JOIN CLASS_MST L2 ON L1.CLASSCD = L2.CLASSCD ");
            stb.append("     LEFT JOIN ANNUAL_T L3 ON T1.YEAR = L3.YEAR ");
            stb.append("     LEFT JOIN SUBCLASS_DETAILS_MST L4 ON T1.YEAR = L4.YEAR ");
            stb.append("          AND T1.CLASSCD = L4.CLASSCD ");
            stb.append("          AND T1.CURRICULUM_CD = L4.CURRICULUM_CD ");
            stb.append("          AND T1.SUBCLASSCD = L4.SUBCLASSCD ");
            stb.append(" GROUP BY ");
            stb.append("     CASE WHEN L2.CLASSCD IS NOT NULL ");
            stb.append("          THEN L2.CLASSCD ");
            stb.append("          ELSE T1.CLASSCD ");
            stb.append("     END, ");
            stb.append("     CASE WHEN L2.CLASSNAME IS NOT NULL ");
            stb.append("          THEN L2.CLASSNAME ");
            stb.append("          ELSE T1.CLASSNAME ");
            stb.append("     END, ");
            stb.append("     CASE WHEN L2.SHOWORDER2 IS NOT NULL ");
            stb.append("          THEN L2.SHOWORDER2 ");
            stb.append("          ELSE T1.CLASSORDER2 ");
            stb.append("     END, ");
            stb.append("     CASE WHEN L1.SUBCLASSCD IS NOT NULL ");
            stb.append("          THEN L1.SUBCLASSCD ");
            stb.append("          ELSE T1.SUBCLASSCD ");
            stb.append("     END, ");
            stb.append("     CASE WHEN L1.SUBCLASSORDERNAME1 IS NOT NULL ");
            stb.append("          THEN L1.SUBCLASSORDERNAME1 ");
            stb.append("          ELSE CASE WHEN L1.SUBCLASSNAME IS NOT NULL ");
            stb.append("               THEN L1.SUBCLASSNAME ");
            stb.append("               ELSE T1.SUBCLASSNAME ");
            stb.append("               END ");
            stb.append("     END, ");
            stb.append("     CASE WHEN L1.SHOWORDER2 IS NOT NULL ");
            stb.append("          THEN L1.SHOWORDER2 ");
            stb.append("          ELSE T1.SUBCLASSORDER2 ");
            stb.append("     END, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     L3.ANNUAL, ");
            stb.append("     T1.YEAR ");
            stb.append(" ) ");
            if (selectDiv.equals("TITLE")) {
                stb.append(" SELECT ");
                stb.append("     ANNUAL, ");
                stb.append("     YEAR ");
                stb.append(" FROM ");
                stb.append("     ANNUAL_T ");
                stb.append(" ORDER BY ");
                stb.append("     YEAR ");
            } else {

                stb.append(" , FUKUSU_T AS ( ");
                stb.append(" SELECT ");
                stb.append("     CASE WHEN L2.CLASSCD IS NOT NULL ");
                stb.append("          THEN L2.CLASSCD ");
                stb.append("          ELSE T1.CLASSCD ");
                stb.append("     END AS CLASSCD, ");
                stb.append("     CASE WHEN L2.CLASSNAME IS NOT NULL ");
                stb.append("          THEN L2.CLASSNAME ");
                stb.append("          ELSE T1.CLASSNAME ");
                stb.append("     END AS CLASSNAME, ");
                stb.append("     CASE WHEN L2.SHOWORDER2 IS NOT NULL ");
                stb.append("          THEN L2.SHOWORDER2 ");
                stb.append("          ELSE T1.CLASSORDER2 ");
                stb.append("     END AS CLASSORDER2, ");
                stb.append("     MAX(T1.CURRICULUM_CD) AS CURRICULUM_CD, ");
                stb.append("     CASE WHEN L1.SUBCLASSCD IS NOT NULL ");
                stb.append("          THEN L1.SUBCLASSCD ");
                stb.append("          ELSE T1.SUBCLASSCD ");
                stb.append("     END AS SUBCLASSCD, ");
                stb.append("     CASE WHEN L1.SUBCLASSORDERNAME1 IS NOT NULL ");
                stb.append("          THEN L1.SUBCLASSORDERNAME1 ");
                stb.append("          ELSE CASE WHEN L1.SUBCLASSNAME IS NOT NULL ");
                stb.append("               THEN L1.SUBCLASSNAME ");
                stb.append("               ELSE T1.SUBCLASSNAME ");
                stb.append("               END ");
                stb.append("     END AS SUBCLASSNAME, ");
                stb.append("     CASE WHEN L1.SHOWORDER2 IS NOT NULL ");
                stb.append("          THEN L1.SHOWORDER2 ");
                stb.append("          ELSE T1.SUBCLASSORDER2 ");
                stb.append("     END AS SUBCLASSORDER2, ");
                stb.append("     T1.SCHREGNO, ");
                stb.append("     MAX(T1.SCHOOLCD) AS SCHOOLCD, ");
                stb.append("     L3.ANNUAL, ");
                stb.append("     T1.YEAR, ");
                stb.append("     CASE WHEN MAX(T1.SCHOOLCD) IN('2', '3', '4') OR MAX(T1.REMARK) IS NOT NULL ");
                stb.append("          THEN MAX(T1.REMARK) ");
                stb.append("          ELSE MAX(T1.FORMER_REG_SCHOOLCD) || 'にて修得' ");
                stb.append("     END AS FORMER_REG_SCHOOLCD, ");
                stb.append("     SUM(T1.GET_CREDIT) AS GET_CREDIT, ");
                stb.append("     ROUND(AVG(FLOAT(T1.VALUATION)) * 10, -1) / 10 AS VALUATION ");
                stb.append(" FROM ");
                stb.append("     STUDY_REC T1 ");
                stb.append("     LEFT JOIN SUBCLASS_MST L1 ON T1.CLASSCD = L1.CLASSCD ");
                stb.append("          AND T1.CURRICULUM_CD = L1.CURRICULUM_CD ");
                stb.append("          AND T1.SUBCLASSCD2 = L1.SUBCLASSCD ");
                stb.append("     LEFT JOIN CLASS_MST L2 ON L1.CLASSCD = L2.CLASSCD ");
                stb.append("     LEFT JOIN ANNUAL_T L3 ON T1.YEAR = L3.YEAR ");
                stb.append(" GROUP BY ");
                stb.append("     CASE WHEN L2.CLASSCD IS NOT NULL ");
                stb.append("          THEN L2.CLASSCD ");
                stb.append("          ELSE T1.CLASSCD ");
                stb.append("     END, ");
                stb.append("     CASE WHEN L2.CLASSNAME IS NOT NULL ");
                stb.append("          THEN L2.CLASSNAME ");
                stb.append("          ELSE T1.CLASSNAME ");
                stb.append("     END, ");
                stb.append("     CASE WHEN L2.SHOWORDER2 IS NOT NULL ");
                stb.append("          THEN L2.SHOWORDER2 ");
                stb.append("          ELSE T1.CLASSORDER2 ");
                stb.append("     END, ");
                stb.append("     CASE WHEN L1.SUBCLASSCD IS NOT NULL ");
                stb.append("          THEN L1.SUBCLASSCD ");
                stb.append("          ELSE T1.SUBCLASSCD ");
                stb.append("     END, ");
                stb.append("     CASE WHEN L1.SUBCLASSORDERNAME1 IS NOT NULL ");
                stb.append("          THEN L1.SUBCLASSORDERNAME1 ");
                stb.append("          ELSE CASE WHEN L1.SUBCLASSNAME IS NOT NULL ");
                stb.append("               THEN L1.SUBCLASSNAME ");
                stb.append("               ELSE T1.SUBCLASSNAME ");
                stb.append("               END ");
                stb.append("     END, ");
                stb.append("     CASE WHEN L1.SHOWORDER2 IS NOT NULL ");
                stb.append("          THEN L1.SHOWORDER2 ");
                stb.append("          ELSE T1.SUBCLASSORDER2 ");
                stb.append("     END, ");
                stb.append("     T1.SCHREGNO, ");
                stb.append("     L3.ANNUAL, ");
                stb.append("     T1.YEAR ");
                stb.append(" HAVING ");
                stb.append("     COUNT(*) > 1 ");
                stb.append("     AND SUM(CASE WHEN VALUE(T1.VALUATION, 0) > 1 THEN 1 ELSE 0 END) > 0 ");
                stb.append(" ) ");

                stb.append(" , MAIN_T AS ( ");
                stb.append(" SELECT ");
                stb.append("     CASE WHEN L2.CLASSCD IS NOT NULL ");
                stb.append("          THEN L2.CLASSCD ");
                stb.append("          ELSE T1.CLASSCD ");
                stb.append("     END AS CLASSCD, ");
                stb.append("     CASE WHEN L2.CLASSNAME IS NOT NULL ");
                stb.append("          THEN L2.CLASSNAME ");
                stb.append("          ELSE T1.CLASSNAME ");
                stb.append("     END AS CLASSNAME, ");
                stb.append("     CASE WHEN L2.SHOWORDER2 IS NOT NULL ");
                stb.append("          THEN L2.SHOWORDER2 ");
                stb.append("          ELSE T1.CLASSORDER2 ");
                stb.append("     END AS CLASSORDER2, ");
                stb.append("     MAX(T1.CURRICULUM_CD) AS CURRICULUM_CD, ");
                stb.append("     CASE WHEN L1.SUBCLASSCD IS NOT NULL ");
                stb.append("          THEN L1.SUBCLASSCD ");
                stb.append("          ELSE T1.SUBCLASSCD ");
                stb.append("     END AS SUBCLASSCD, ");
                stb.append("     CASE WHEN L1.SUBCLASSORDERNAME1 IS NOT NULL ");
                stb.append("          THEN L1.SUBCLASSORDERNAME1 ");
                stb.append("          ELSE CASE WHEN L1.SUBCLASSNAME IS NOT NULL ");
                stb.append("               THEN L1.SUBCLASSNAME ");
                stb.append("               ELSE T1.SUBCLASSNAME ");
                stb.append("               END ");
                stb.append("     END AS SUBCLASSNAME, ");
                stb.append("     CASE WHEN L1.SHOWORDER2 IS NOT NULL ");
                stb.append("          THEN L1.SHOWORDER2 ");
                stb.append("          ELSE T1.SUBCLASSORDER2 ");
                stb.append("     END AS SUBCLASSORDER2, ");
                stb.append("     T1.SCHREGNO, ");
                stb.append("     MAX(T1.SCHOOLCD) AS SCHOOLCD, ");
                stb.append("     L3.ANNUAL, ");
                stb.append("     T1.YEAR, ");
                stb.append("     CASE WHEN MAX(T1.SCHOOLCD) IN('2', '3', '4') OR MAX(T1.REMARK) IS NOT NULL ");
                stb.append("          THEN MAX(T1.REMARK) ");
                stb.append("          ELSE MAX(T1.FORMER_REG_SCHOOLCD) || 'にて修得' ");
                stb.append("     END AS FORMER_REG_SCHOOLCD, ");
                stb.append("     SUM(T1.GET_CREDIT) AS GET_CREDIT, ");
                stb.append("     ROUND(AVG(FLOAT(T1.VALUATION)) * 10, -1) / 10 AS VALUATION ");
                stb.append(" FROM ");
                stb.append("     STUDY_REC T1 ");
                stb.append("     LEFT JOIN SUBCLASS_MST L1 ON T1.CLASSCD = L1.CLASSCD ");
                stb.append("          AND T1.CURRICULUM_CD = L1.CURRICULUM_CD ");
                stb.append("          AND T1.SUBCLASSCD2 = L1.SUBCLASSCD ");
                stb.append("     LEFT JOIN CLASS_MST L2 ON L1.CLASSCD = L2.CLASSCD ");
                stb.append("     LEFT JOIN ANNUAL_T L3 ON T1.YEAR = L3.YEAR, ");
                stb.append("     FUKUSU_T ");
                stb.append(" WHERE ");
                stb.append("     FUKUSU_T.CLASSCD = CASE WHEN L2.CLASSCD IS NOT NULL ");
                stb.append("          THEN L2.CLASSCD ");
                stb.append("          ELSE T1.CLASSCD ");
                stb.append("     END ");
                stb.append("     AND FUKUSU_T.CLASSNAME = CASE WHEN L2.CLASSNAME IS NOT NULL ");
                stb.append("                                 THEN L2.CLASSNAME ");
                stb.append("                                 ELSE T1.CLASSNAME ");
                stb.append("                            END ");
                stb.append("     AND FUKUSU_T.CLASSORDER2 = CASE WHEN L2.SHOWORDER2 IS NOT NULL ");
                stb.append("                                   THEN L2.SHOWORDER2 ");
                stb.append("                                   ELSE T1.CLASSORDER2 ");
                stb.append("                              END ");
                stb.append("     AND FUKUSU_T.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("     AND FUKUSU_T.SUBCLASSCD = CASE WHEN L1.SUBCLASSCD IS NOT NULL ");
                stb.append("                                  THEN L1.SUBCLASSCD ");
                stb.append("                                  ELSE T1.SUBCLASSCD ");
                stb.append("                             END ");
                stb.append("     AND FUKUSU_T.SUBCLASSNAME = CASE WHEN L1.SUBCLASSORDERNAME1 IS NOT NULL ");
                stb.append("                                  THEN L1.SUBCLASSORDERNAME1 ");
                stb.append("                                  ELSE CASE WHEN L1.SUBCLASSNAME IS NOT NULL ");
                stb.append("                                            THEN L1.SUBCLASSNAME ");
                stb.append("                                            ELSE T1.SUBCLASSNAME ");
                stb.append("                                       END ");
                stb.append("                             END ");
                stb.append("     AND FUKUSU_T.SUBCLASSORDER2 = CASE WHEN L1.SHOWORDER2 IS NOT NULL ");
                stb.append("                                      THEN L1.SHOWORDER2 ");
                stb.append("                                      ELSE T1.SUBCLASSORDER2 ");
                stb.append("                                 END ");
                stb.append("     AND FUKUSU_T.SCHREGNO = T1.SCHREGNO ");
                stb.append("     AND FUKUSU_T.ANNUAL = L3.ANNUAL ");
                stb.append("     AND FUKUSU_T.YEAR = T1.YEAR ");
                stb.append("     AND VALUE(T1.VALUATION, 0) > 1 ");
                stb.append(" GROUP BY ");
                stb.append("     CASE WHEN L2.CLASSCD IS NOT NULL ");
                stb.append("          THEN L2.CLASSCD ");
                stb.append("          ELSE T1.CLASSCD ");
                stb.append("     END, ");
                stb.append("     CASE WHEN L2.CLASSNAME IS NOT NULL ");
                stb.append("          THEN L2.CLASSNAME ");
                stb.append("          ELSE T1.CLASSNAME ");
                stb.append("     END, ");
                stb.append("     CASE WHEN L2.SHOWORDER2 IS NOT NULL ");
                stb.append("          THEN L2.SHOWORDER2 ");
                stb.append("          ELSE T1.CLASSORDER2 ");
                stb.append("     END, ");
                stb.append("     CASE WHEN L1.SUBCLASSCD IS NOT NULL ");
                stb.append("          THEN L1.SUBCLASSCD ");
                stb.append("          ELSE T1.SUBCLASSCD ");
                stb.append("     END, ");
                stb.append("     CASE WHEN L1.SUBCLASSORDERNAME1 IS NOT NULL ");
                stb.append("          THEN L1.SUBCLASSORDERNAME1 ");
                stb.append("          ELSE CASE WHEN L1.SUBCLASSNAME IS NOT NULL ");
                stb.append("               THEN L1.SUBCLASSNAME ");
                stb.append("               ELSE T1.SUBCLASSNAME ");
                stb.append("               END ");
                stb.append("     END, ");
                stb.append("     CASE WHEN L1.SHOWORDER2 IS NOT NULL ");
                stb.append("          THEN L1.SHOWORDER2 ");
                stb.append("          ELSE T1.SUBCLASSORDER2 ");
                stb.append("     END, ");
                stb.append("     T1.SCHREGNO, ");
                stb.append("     L3.ANNUAL, ");
                stb.append("     T1.YEAR ");

                stb.append(" UNION ALL ");
                stb.append(" SELECT ");
                stb.append("     CASE WHEN L2.CLASSCD IS NOT NULL ");
                stb.append("          THEN L2.CLASSCD ");
                stb.append("          ELSE T1.CLASSCD ");
                stb.append("     END AS CLASSCD, ");
                stb.append("     CASE WHEN L2.CLASSNAME IS NOT NULL ");
                stb.append("          THEN L2.CLASSNAME ");
                stb.append("          ELSE T1.CLASSNAME ");
                stb.append("     END AS CLASSNAME, ");
                stb.append("     CASE WHEN L2.SHOWORDER2 IS NOT NULL ");
                stb.append("          THEN L2.SHOWORDER2 ");
                stb.append("          ELSE T1.CLASSORDER2 ");
                stb.append("     END AS CLASSORDER2, ");
                stb.append("     MAX(T1.CURRICULUM_CD) AS CURRICULUM_CD, ");
                stb.append("     CASE WHEN L1.SUBCLASSCD IS NOT NULL ");
                stb.append("          THEN L1.SUBCLASSCD ");
                stb.append("          ELSE T1.SUBCLASSCD ");
                stb.append("     END AS SUBCLASSCD, ");
                stb.append("     CASE WHEN L1.SUBCLASSORDERNAME1 IS NOT NULL ");
                stb.append("          THEN L1.SUBCLASSORDERNAME1 ");
                stb.append("          ELSE CASE WHEN L1.SUBCLASSNAME IS NOT NULL ");
                stb.append("               THEN L1.SUBCLASSNAME ");
                stb.append("               ELSE T1.SUBCLASSNAME ");
                stb.append("               END ");
                stb.append("     END AS SUBCLASSNAME, ");
                stb.append("     CASE WHEN L1.SHOWORDER2 IS NOT NULL ");
                stb.append("          THEN L1.SHOWORDER2 ");
                stb.append("          ELSE T1.SUBCLASSORDER2 ");
                stb.append("     END AS SUBCLASSORDER2, ");
                stb.append("     T1.SCHREGNO, ");
                stb.append("     MAX(T1.SCHOOLCD) AS SCHOOLCD, ");
                stb.append("     L3.ANNUAL, ");
                stb.append("     T1.YEAR, ");
                stb.append("     CASE WHEN MAX(T1.SCHOOLCD) IN('2', '3', '4') OR MAX(T1.REMARK) IS NOT NULL ");
                stb.append("          THEN MAX(T1.REMARK) ");
                stb.append("          ELSE MAX(T1.FORMER_REG_SCHOOLCD) || 'にて修得' ");
                stb.append("     END AS FORMER_REG_SCHOOLCD, ");
                stb.append("     SUM(T1.GET_CREDIT) AS GET_CREDIT, ");
                stb.append("     ROUND(AVG(FLOAT(T1.VALUATION)) * 10, -1) / 10 AS VALUATION ");
                stb.append(" FROM ");
                stb.append("     STUDY_REC T1 ");
                stb.append("     LEFT JOIN SUBCLASS_MST L1 ON T1.CLASSCD = L1.CLASSCD ");
                stb.append("          AND T1.CURRICULUM_CD = L1.CURRICULUM_CD ");
                stb.append("          AND T1.SUBCLASSCD2 = L1.SUBCLASSCD ");
                stb.append("     LEFT JOIN CLASS_MST L2 ON L1.CLASSCD = L2.CLASSCD ");
                stb.append("     LEFT JOIN ANNUAL_T L3 ON T1.YEAR = L3.YEAR ");
                stb.append(" GROUP BY ");
                stb.append("     CASE WHEN L2.CLASSCD IS NOT NULL ");
                stb.append("          THEN L2.CLASSCD ");
                stb.append("          ELSE T1.CLASSCD ");
                stb.append("     END, ");
                stb.append("     CASE WHEN L2.CLASSNAME IS NOT NULL ");
                stb.append("          THEN L2.CLASSNAME ");
                stb.append("          ELSE T1.CLASSNAME ");
                stb.append("     END, ");
                stb.append("     CASE WHEN L2.SHOWORDER2 IS NOT NULL ");
                stb.append("          THEN L2.SHOWORDER2 ");
                stb.append("          ELSE T1.CLASSORDER2 ");
                stb.append("     END, ");
                stb.append("     CASE WHEN L1.SUBCLASSCD IS NOT NULL ");
                stb.append("          THEN L1.SUBCLASSCD ");
                stb.append("          ELSE T1.SUBCLASSCD ");
                stb.append("     END, ");
                stb.append("     CASE WHEN L1.SUBCLASSORDERNAME1 IS NOT NULL ");
                stb.append("          THEN L1.SUBCLASSORDERNAME1 ");
                stb.append("          ELSE CASE WHEN L1.SUBCLASSNAME IS NOT NULL ");
                stb.append("               THEN L1.SUBCLASSNAME ");
                stb.append("               ELSE T1.SUBCLASSNAME ");
                stb.append("               END ");
                stb.append("     END, ");
                stb.append("     CASE WHEN L1.SHOWORDER2 IS NOT NULL ");
                stb.append("          THEN L1.SHOWORDER2 ");
                stb.append("          ELSE T1.SUBCLASSORDER2 ");
                stb.append("     END, ");
                stb.append("     T1.SCHREGNO, ");
                stb.append("     L3.ANNUAL, ");
                stb.append("     T1.YEAR ");
                stb.append(" HAVING ");
                stb.append("     COUNT(*) = 1 ");
                stb.append("     OR SUM(CASE WHEN VALUE(T1.VALUATION, 0) > 1 THEN 1 ELSE 0 END) = 0 ");
                stb.append(" ) ");

                stb.append(" , SELECT_T AS ( ");
                stb.append(" SELECT ");
                stb.append("     T1.CLASSCD, ");
                stb.append("     T1.CLASSNAME, ");
                stb.append("     T1.CLASSORDER2, ");
                stb.append("     T1.CURRICULUM_CD, ");
                stb.append("     T1.SUBCLASSCD, ");
                stb.append("     T1.SUBCLASSNAME, ");
                stb.append("     T1.SUBCLASSORDER2, ");
                stb.append("     T1.SCHREGNO, ");
                stb.append("     T1.SCHOOLCD, ");
                stb.append("     T1.ANNUAL, ");
                stb.append("     T1.YEAR, ");
                stb.append("     T1.FORMER_REG_SCHOOLCD, ");
                stb.append("     L1.MIN_MIRI, ");
                stb.append("     L1.STUDY_CNT, ");
                stb.append("     CASE WHEN L1.MIN_MIRI = '0' ");
                stb.append("          THEN L1.RISYU_CREDIT ");
                stb.append("          ELSE L1.MIRI_CREDIT ");
                stb.append("     END AS GET_CREDIT, ");
                stb.append("     T1.VALUATION ");
                stb.append(" FROM ");
                stb.append("     MAIN_T T1 ");
                stb.append("     LEFT JOIN CREDIT_T L1 ON T1.YEAR = L1.YEAR ");
                stb.append("          AND T1.CLASSCD = L1.CLASSCD ");
                stb.append("          AND T1.CURRICULUM_CD = L1.CURRICULUM_CD ");
                stb.append("          AND T1.SUBCLASSCD = L1.SUBCLASSCD ");
                stb.append("          AND T1.SCHREGNO = L1.SCHREGNO ");
                stb.append("          AND T1.ANNUAL = L1.ANNUAL ");
                stb.append(" ) ");

                stb.append(" , CURRICULUM_T AS ( ");
                stb.append(" SELECT ");
                stb.append("     T1.CLASSCD, ");
                stb.append("     MAX(T1.CURRICULUM_CD) AS CURRICULUM_CD, ");
                stb.append("     T1.SUBCLASSCD, ");
                stb.append("     T1.SCHREGNO ");
                stb.append(" FROM ");
                stb.append("     SELECT_T T1 ");
                stb.append(" GROUP BY ");
                stb.append("     T1.CLASSCD, ");
                stb.append("     T1.SUBCLASSCD, ");
                stb.append("     T1.SCHREGNO ");
                stb.append(" ) ");

                stb.append(" SELECT ");
                stb.append("     T1.CLASSCD, ");
                stb.append("     T1.CLASSNAME, ");
                stb.append("     T1.CLASSORDER2, ");
                stb.append("     T1.CURRICULUM_CD, ");
                stb.append("     T1.SUBCLASSCD, ");
                stb.append("     CASE WHEN L2.SUBCLASSORDERNAME1 IS NOT NULL ");
                stb.append("          THEN L2.SUBCLASSORDERNAME1 ");
                stb.append("          ELSE CASE WHEN L2.SUBCLASSNAME IS NOT NULL ");
                stb.append("               THEN L2.SUBCLASSNAME ");
                stb.append("               ELSE T1.SUBCLASSNAME ");
                stb.append("               END ");
                stb.append("     END AS SUBCLASSNAME, ");
                stb.append("     L2.SHOWORDER2, ");
                stb.append("     T1.SCHREGNO, ");
                stb.append("     T1.SCHOOLCD, ");
                stb.append("     T1.ANNUAL, ");
                stb.append("     T1.YEAR, ");
                stb.append("     T1.FORMER_REG_SCHOOLCD, ");
                stb.append("     T1.MIN_MIRI, ");
                stb.append("     T1.STUDY_CNT, ");
                stb.append("     T1.GET_CREDIT, ");
                stb.append("     T1.VALUATION ");
                stb.append(" FROM ");
                stb.append("     SELECT_T T1 ");
                stb.append("     LEFT JOIN CURRICULUM_T L1 ON T1.CLASSCD = L1.CLASSCD ");
                stb.append("          AND T1.SCHREGNO = L1.SCHREGNO ");
                stb.append("          AND T1.SUBCLASSCD = L1.SUBCLASSCD ");
                stb.append("     LEFT JOIN SUBCLASS_MST L2 ON T1.CLASSCD = L2.CLASSCD ");
                stb.append("          AND L1.CURRICULUM_CD = L2.CURRICULUM_CD ");
                stb.append("          AND T1.SUBCLASSCD = L2.SUBCLASSCD ");
                stb.append(" ORDER BY ");
                stb.append("     T1.CLASSORDER2, ");
                stb.append("     T1.CLASSCD, ");
                stb.append("     L2.SHOWORDER2, ");
                stb.append("     T1.SUBCLASSCD, ");
                stb.append("     T1.CURRICULUM_CD, ");
                stb.append("     T1.YEAR ");

            }
            log.debug(stb);
            return stb.toString();
        }

    }

    class Student {
        final String _schregno;
        final String _applicantNo;
        final String _grade;
        final String _hrClass;
        final String _attendNo;
        final String _hrName;
        final String _hrNameAbbv;
        final String _name;
        final String _kana;
        final String _sex;
        final String _birthDay;
        final String _courseCd;
        final String _courseName;
        final String _majorCd;
        final String _majorName;
        final String _courseCode;
        final String _courseCodeName;
        final String _finishDate;
        final String _finSchool;
        final String _zip;
        final String _pref;
        final String _prefName;
        final String _addr1;
        final String _addr2;
        final String _addr3;
        final String _gName;
        final String _gKana;
        final String _gZip;
        final String _gPref;
        final String _gPrefName;
        final String _gAddr1;
        final String _gAddr2;
        final String _gAddr3;
        final List _regdHist;
        final Map _baseHist;
        final List _transferHist;
        final String _oldCurriculum;
        EntGrd _entInfo;
        EntGrd _grdInfo;
        AftGrd _aftGrdInfo;

        public Student(
                final String schregno,
                final String applicantNo,
                final String grade,
                final String hrClass,
                final String attendNo,
                final String hrName,
                final String hrNameAbbv,
                final String name,
                final String nameKana,
                final String sex,
                final String birthDay,
                final String courseCd,
                final String courseName,
                final String majorCd,
                final String majorName,
                final String courseCode,
                final String courseCodeName,
                final String finishDate,
                final String finSchool,
                final String zip,
                final String pref,
                final String prefName,
                final String addr1,
                final String addr2,
                final String addr3,
                final String gName,
                final String gKana,
                final String gZip,
                final String gPref,
                final String gPrefName,
                final String gAddr1,
                final String gAddr2,
                final String gAddr3,
                final String oldCurriculum
                ) {
            _schregno = schregno;
            _applicantNo = applicantNo;
            _grade = grade;
            _hrClass = hrClass;
            _attendNo = attendNo;
            _hrName = hrName;
            _hrNameAbbv = hrNameAbbv;
            _name = name;
            _kana = nameKana;
            _sex = sex;
            _birthDay = birthDay;
            _courseCd = courseCd;
            _courseName = courseName;
            _majorCd = majorCd;
            _majorName = majorName;
            _courseCode = courseCode;
            _courseCodeName = courseCodeName;
            _finishDate = finishDate;
            _finSchool = finSchool;
            _zip = zip;
            _pref = pref;
            _prefName = prefName;
            _addr1 = addr1;
            _addr2 = addr2;
            _addr3 = addr3;
            _gName = gName;
            _gKana = gKana;
            _gZip = gZip;
            _gPref = gPref;
            _gPrefName = gPrefName;
            _gAddr1 = gAddr1;
            _gAddr2 = gAddr2;
            _gAddr3 = gAddr3;
            _regdHist = new ArrayList();
            _baseHist = new HashMap();
            _transferHist = new ArrayList();
            _oldCurriculum = oldCurriculum;
        }

        private void setEntGrd(final DB2UDB db2, final String fieldName) throws SQLException {
            ResultSet rs = null;
            try {
                final String sql = getEntGrdSql(fieldName);
                db2.query(sql);
                rs = db2.getResultSet();
                while (rs.next()) {
                    EntGrd entGrd = new EntGrd(rs.getString("DIV"),
                                               rs.getString("NAME"),
                                               rs.getString("NENDO"),
                                               rs.getString("DATE"),
                                               rs.getString("SCHOOL_NAME"),
                                               rs.getString("ADDR"),
                                               rs.getString("REASON"));

                    log.debug(entGrd);
                    if (fieldName.equals("ENT")) {
                        _entInfo = entGrd;
                    } else {
                        _grdInfo = entGrd;
                    }
                }
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
        }

        private String getEntGrdSql(final String fieldName) {
            final String nameMst = fieldName.equals("ENT") ? "A002" : "A003";
            final StringBuffer stb = new StringBuffer();

            stb.append(" WITH ANO_SCHOOL AS ( ");
            stb.append(" SELECT ");
            stb.append("     APPLICANTNO, ");
            stb.append("     MAX(REGD_S_DATE) AS REGD_S_DATE ");
            stb.append(" FROM ");
            stb.append("     ANOTHER_SCHOOL_HIST_DAT ");
            stb.append(" WHERE ");
            stb.append("     APPLICANTNO = '" + _applicantNo + "' ");
            stb.append(" GROUP BY ");
            stb.append("     APPLICANTNO ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1." + fieldName + "_DIV AS DIV, ");
            stb.append("     CASE WHEN '" + fieldName + "' = 'ENT' AND T1." + fieldName + "_DIV IN ('4', '5') ");
            stb.append("          THEN N1.NAME1 ");
            stb.append("          ELSE CASE WHEN T1." + fieldName + "_DIV IS NULL ");
            stb.append("                    THEN '' ");
            stb.append("                    ELSE CASE WHEN '" + fieldName + "' = 'ENT' ");
            stb.append("                              THEN '入学' ");
            stb.append("                              ELSE N1.NAME1 ");
            stb.append("                         END ");
            stb.append("               END ");
            stb.append("     END AS NAME, ");
            stb.append("     T1." + fieldName + "_DATE AS DATE, ");
            stb.append("     CASE WHEN MONTH(T1." + fieldName + "_DATE) < 4 ");
            stb.append("          THEN YEAR(T1." + fieldName + "_DATE) - 1 ");
            stb.append("          ELSE YEAR(T1." + fieldName + "_DATE) ");
            stb.append("     END AS NENDO, ");
            stb.append("     CASE WHEN '" + fieldName + "' = 'ENT' AND T1." + fieldName + "_DIV IN ('4', '5') ");
            stb.append("          THEN L1.FORMER_REG_SCHOOLCD ");
            stb.append("          ELSE '' ");
            stb.append("     END AS SCHOOL_CD, ");
            stb.append("     CASE WHEN '" + fieldName + "' = 'ENT' AND T1." + fieldName + "_DIV IN ('4', '5') ");
            stb.append("          THEN VALUE(N3.NAME1, '') || L2.NAME ");
            stb.append("          ELSE T1." + fieldName + "_SCHOOL ");
            stb.append("     END AS SCHOOL_NAME, ");
            stb.append("     CASE WHEN '" + fieldName + "' = 'ENT' AND T1." + fieldName + "_DIV IN ('4', '5') ");
            stb.append("          THEN L2.PREF || VALUE(ADDR1, '') || VALUE(ADDR2, '') || VALUE(ADDR3, '') ");
            stb.append("          ELSE T1." + fieldName + "_ADDR ");
            stb.append("     END AS ADDR, ");
            stb.append("     CASE WHEN '" + fieldName + "' = 'ENT' AND T1." + fieldName + "_DIV IN ('4', '5') ");
            stb.append("          THEN VALUE(N2.NAME1, '') || '課程' || VALUE(L1.MAJOR_NAME, '') ");
            stb.append("          ELSE T1." + fieldName + "_REASON ");
            stb.append("     END AS REASON ");
            stb.append(" FROM ");
            stb.append("     SCHREG_BASE_MST T1 ");
            stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = '" + nameMst + "' ");
            stb.append("          AND T1." + fieldName + "_DIV = N1.NAMECD2 ");
            stb.append("     LEFT JOIN (SELECT ");
            stb.append("                    W2.* ");
            stb.append("                FROM ");
            stb.append("                    ANO_SCHOOL W1, ");
            stb.append("                    ANOTHER_SCHOOL_HIST_DAT W2 ");
            stb.append("                WHERE ");
            stb.append("                    W1.APPLICANTNO = W2.APPLICANTNO ");
            stb.append("                    AND W1.REGD_S_DATE = W2.REGD_S_DATE ");
            stb.append("               ) L1 ON T1.APPLICANTNO = L1.APPLICANTNO ");
            stb.append("     LEFT JOIN FIN_HIGH_SCHOOL_MST L2 ON L1.FORMER_REG_SCHOOLCD = L2.SCHOOL_CD ");
            stb.append("     LEFT JOIN NAME_MST N3 ON N3.NAMECD1 = 'L001' ");
            stb.append("          AND L2.FINSCOOL_DISTCD = N3.NAMECD2 ");
            stb.append("     LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'W019' ");
            stb.append("          AND L1.STUDENT_DIV = N2.NAMECD2 ");
            stb.append(" WHERE ");
            stb.append("     T1.SCHREGNO = '" + _schregno + "' ");

            return stb.toString();
        }

        private void setAftGrd(final DB2UDB db2) throws SQLException {
            ResultSet rs = null;
            try {
                final String sql = getAftGrd();
                db2.query(sql);
                rs = db2.getResultSet();
                while (rs.next()) {
                    final String senkou = rs.getString("SENKOU_KIND");
                    final String syugyou = senkou.equals("0") ? rs.getString("AREA_NAME") : rs.getString("SHUSHOKU_ADDR");
                    final String think = senkou.equals("0") ? rs.getString("THINKEXAM") : rs.getString("JOB_THINK");
                    AftGrd aftGrd = new AftGrd(rs.getString("STAT_NAME"),
                                               syugyou,
                                               think);

                    log.debug(aftGrd);
                    _aftGrdInfo = aftGrd;
                }
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
        }

        private String getAftGrd() {
            final StringBuffer stb = new StringBuffer();

            stb.append(" WITH TA AS ( ");
            stb.append(" SELECT ");
            stb.append("     SCHREGNO, ");
            stb.append("     '0' AS SCH_SENKOU_KIND, ");
            stb.append("     MAX(CASE WHEN SENKOU_KIND = '0' THEN YEAR ELSE '-1' END) AS SCH_YEAR, ");
            stb.append("     '1' AS COMP_SENKOU_KIND, ");
            stb.append("     MAX(CASE WHEN SENKOU_KIND = '1' THEN YEAR ELSE '-1' END) AS COMP_YEAR ");
            stb.append(" FROM ");
            stb.append("     AFT_GRAD_COURSE_DAT ");
            stb.append(" WHERE ");
            stb.append("     SCHREGNO = '" + _schregno + "' ");
            stb.append("     AND PLANSTAT = '1' ");
            stb.append(" GROUP BY ");
            stb.append("     SCHREGNO ");
            stb.append(" ), TA2 as( ");
            stb.append(" SELECT ");
            stb.append("     CASE WHEN TA.SCH_YEAR >= TA.COMP_YEAR ");
            stb.append("          THEN TA.SCH_YEAR ");
            stb.append("          ELSE TA.COMP_YEAR ");
            stb.append("     END AS YEAR, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.SENKOU_KIND, ");
            stb.append("     MAX(T1.SEQ) AS SEQ ");
            stb.append(" FROM ");
            stb.append("     AFT_GRAD_COURSE_DAT T1 ");
            stb.append(" INNER JOIN TA ON T1.SCHREGNO = TA.SCHREGNO ");
            stb.append("       AND T1.YEAR = CASE WHEN TA.SCH_YEAR >= TA.COMP_YEAR ");
            stb.append("                          THEN TA.SCH_YEAR ");
            stb.append("                          ELSE TA.COMP_YEAR ");
            stb.append("                     END ");
            stb.append("       AND T1.SENKOU_KIND = CASE WHEN TA.SCH_YEAR >= TA.COMP_YEAR ");
            stb.append("                                 THEN TA.SCH_SENKOU_KIND ");
            stb.append("                                 ELSE TA.COMP_SENKOU_KIND ");
            stb.append("                            END ");
            stb.append(" WHERE ");
            stb.append("     T1.PLANSTAT = '1' ");
            stb.append(" GROUP BY ");
            stb.append("     CASE WHEN TA.SCH_YEAR >= TA.COMP_YEAR ");
            stb.append("          THEN TA.SCH_YEAR ");
            stb.append("          ELSE TA.COMP_YEAR ");
            stb.append("     END, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.SENKOU_KIND ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.* ");
            stb.append(" FROM ");
            stb.append("     AFT_GRAD_COURSE_DAT T1 ");
            stb.append("     INNER JOIN TA2 ON T1.YEAR = TA2.YEAR ");
            stb.append("           AND T1.SCHREGNO = TA2.SCHREGNO ");
            stb.append("           AND T1.SENKOU_KIND = TA2.SENKOU_KIND ");
            stb.append("           AND T1.SEQ = TA2.SEQ ");
            stb.append(" ORDER BY ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SCHREGNO ");

            return stb.toString();
        }

        private void setRegdHist(final DB2UDB db2) throws SQLException {
            ResultSet rsHrClass = null;
            ResultSet rsBaseHist = null;
            try {
                final String sqlHrClass = getHrClassSql();
                db2.query(sqlHrClass);
                rsHrClass = db2.getResultSet();
                while (rsHrClass.next()) {
                    HrClass hrClass = new HrClass(rsHrClass.getString("YEAR"),
                                                  rsHrClass.getString("SORT"),
                                                  rsHrClass.getString("GRADE"),
                                                  rsHrClass.getString("HR_CLASS"),
                                                  rsHrClass.getString("HR_NAME"),
                                                  rsHrClass.getString("HR_NAMEABBV"),
                                                  rsHrClass.getString("STAFFNAME"),
                                                  rsHrClass.getString("STAFFNAME2"),
                                                  rsHrClass.getString("STAFFNAME3"));

                    log.debug(hrClass);
                    _regdHist.add(hrClass);
                }

                final String sqlBaseHist = getSchregBaseHistSql();
                db2.query(sqlBaseHist);
                rsBaseHist = db2.getResultSet();
                while (rsBaseHist.next()) {
                    HrClass hrClass = new HrClass(rsBaseHist.getString("YEAR"),
                                                  rsBaseHist.getString("SORT"),
                                                  rsBaseHist.getString("GRADE"),
                                                  rsBaseHist.getString("HR_CLASS"),
                                                  rsBaseHist.getString("HR_NAME"),
                                                  rsBaseHist.getString("HR_NAMEABBV"),
                                                  rsBaseHist.getString("STAFFNAME"),
                                                  rsBaseHist.getString("STAFFNAME2"),
                                                  rsBaseHist.getString("STAFFNAME3"));

                    log.debug(hrClass);
                    _baseHist.put(rsBaseHist.getString("YEAR"), hrClass);
                }
            } finally {
                DbUtils.closeQuietly(rsHrClass);
                DbUtils.closeQuietly(rsBaseHist);
                db2.commit();
            }
        }

        private String getHrClassSql() {
            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT ");
            stb.append("     T1.YEAR, ");
            stb.append("     row_number() over (order by T1.YEAR) AS SORT, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     L1.HR_NAME, ");
            stb.append("     L1.HR_NAMEABBV, ");
            stb.append("     L2.STAFFNAME, ");
            stb.append("     L3.STAFFNAME AS STAFFNAME2, ");
            stb.append("     L4.STAFFNAME AS STAFFNAME3 ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1 ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT L1 ON T1.YEAR = L1.YEAR ");
            stb.append("          AND T1.SEMESTER = L1.SEMESTER ");
            stb.append("          AND T1.GRADE = L1.GRADE ");
            stb.append("          AND T1.HR_CLASS = L1.HR_CLASS ");
            stb.append("     LEFT JOIN STAFF_MST L2 ON L1.TR_CD1 = L2.STAFFCD ");
            stb.append("     LEFT JOIN STAFF_MST L3 ON L1.TR_CD2 = L3.STAFFCD ");
            stb.append("     LEFT JOIN STAFF_MST L4 ON L1.TR_CD3 = L4.STAFFCD ");
            stb.append(" WHERE ");
            stb.append("     T1.SCHREGNO = '" + _schregno + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.YEAR ");

            return stb.toString();
        }

        private String getSchregBaseHistSql() {
            final StringBuffer stb = new StringBuffer();

            stb.append(" WITH SCH_BASE_HIST AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.YEAR, ");
            stb.append("     MAX(L1.S_APPDATE) AS S_APPDATE ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1 ");
            stb.append("     LEFT JOIN SCHREG_BASE_HIST_DAT L1 ON T1.YEAR = L1.YEAR ");
            stb.append("          AND T1.SEMESTER = L1.SEMESTER ");
            stb.append("          AND T1.SCHREGNO = L1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     T1.SCHREGNO = '" + _schregno + "' ");
            stb.append("     AND (T1.GRADE != L1.GRADE OR T1.HR_CLASS != L1.HR_CLASS) ");
            stb.append(" GROUP BY ");
            stb.append("     T1.YEAR ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.YEAR, ");
            stb.append("     row_number() over (order by T1.YEAR) AS SORT, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     L1.HR_NAME, ");
            stb.append("     L1.HR_NAMEABBV, ");
            stb.append("     L2.STAFFNAME, ");
            stb.append("     L3.STAFFNAME AS STAFFNAME2, ");
            stb.append("     L4.STAFFNAME AS STAFFNAME3 ");
            stb.append(" FROM ");
            stb.append("     SCHREG_BASE_HIST_DAT T1 ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT L1 ON T1.YEAR = L1.YEAR ");
            stb.append("          AND T1.SEMESTER = L1.SEMESTER ");
            stb.append("          AND T1.GRADE = L1.GRADE ");
            stb.append("          AND T1.HR_CLASS = L1.HR_CLASS ");
            stb.append("     LEFT JOIN STAFF_MST L2 ON L1.TR_CD1 = L2.STAFFCD ");
            stb.append("     LEFT JOIN STAFF_MST L3 ON L1.TR_CD2 = L3.STAFFCD ");
            stb.append("     LEFT JOIN STAFF_MST L4 ON L1.TR_CD3 = L4.STAFFCD ");
            stb.append(" WHERE ");
            stb.append("     T1.SCHREGNO = '" + _schregno + "' ");
            stb.append("     AND EXISTS( ");
            stb.append("             SELECT ");
            stb.append("                 'x' ");
            stb.append("             FROM ");
            stb.append("                 SCH_BASE_HIST T2 ");
            stb.append("             WHERE ");
            stb.append("                 T1.S_APPDATE = T2.S_APPDATE ");
            stb.append("         ) ");

            return stb.toString();
        }

        private void setTransferHist(final DB2UDB db2) throws SQLException {
            ResultSet rs = null;
            try {
                final String sql = getTransferSql();
                db2.query(sql);
                rs = db2.getResultSet();
                while (rs.next()) {
                    Transfer transfer = new Transfer(rs.getString("TRANSFERCD"),
                                                     rs.getString("TRANSFERNAME"),
                                                     rs.getString("TRANSFER_SDATE"),
                                                     rs.getString("TRANSFER_EDATE"),
                                                     rs.getString("TRANSFERREASON"),
                                                     rs.getString("TRANSFERPLACE"),
                                                     rs.getString("TRANSFERADDR"),
                                                     rs.getString("ABROAD_CLASSDAYS"),
                                                     rs.getString("ABROAD_CREDITS"));

                    log.debug(transfer);
                    _transferHist.add(transfer);
                }
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
        }

        private String getTransferSql() {
            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT ");
            stb.append("     T1.TRANSFERCD, ");
            stb.append("     N1.NAME1 AS TRANSFERNAME, ");
            stb.append("     T1.TRANSFER_SDATE, ");
            stb.append("     T1.TRANSFER_EDATE, ");
            stb.append("     T1.TRANSFERREASON, ");
            stb.append("     T1.TRANSFERPLACE, ");
            stb.append("     T1.TRANSFERADDR, ");
            stb.append("     T1.ABROAD_CLASSDAYS, ");
            stb.append("     T1.ABROAD_CREDITS ");
            stb.append(" FROM ");
            stb.append("     SCHREG_TRANSFER_DAT T1 ");
            stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'A004' ");
            stb.append("          AND T1.TRANSFERCD = N1.NAMECD2 ");
            stb.append(" WHERE ");
            stb.append("     T1.SCHREGNO = '" + _schregno + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.TRANSFER_SDATE DESC ");

            return stb.toString();
        }

        public Transfer getMaxTransfer() {
            for (final Iterator iter = _transferHist.iterator(); iter.hasNext();) {
                final Transfer transfer = (Transfer) iter.next();
                return transfer;
            }
            return null;
        }

        public String toString() {
            return "学籍：" + _schregno + " 名前：" + _name;
        }
    }

    private String getStudentInfoSql(final String year, final String semester) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     VSCH.SCHREGNO, ");
        stb.append("     BASE.APPLICANTNO, ");
        stb.append("     VSCH.GRADE, ");
        stb.append("     VSCH.HR_CLASS, ");
        stb.append("     VSCH.ATTENDNO, ");
        stb.append("     VSCH.HR_NAME, ");
        stb.append("     VSCH.HR_NAMEABBV, ");
        stb.append("     VSCH.NAME, ");
        stb.append("     BASE.NAME_KANA, ");
        stb.append("     N1.NAME2 AS SEX, ");
        stb.append("     BASE.BIRTHDAY, ");
        stb.append("     VSCH.COURSECD, ");
        stb.append("     L1.COURSENAME, ");
        stb.append("     VSCH.MAJORCD, ");
        stb.append("     L1.MAJORNAME, ");
        stb.append("     VSCH.COURSECODE, ");
        stb.append("     L2.COURSECODENAME, ");
        stb.append("     BASE.FINISH_DATE, ");
        stb.append("     VALUE(L8.NAME1, '') || L7.NAME AS FINSCHOOL, ");
        stb.append("     L4.ZIPCD, ");
        stb.append("     L4.PREF_CD, ");
        stb.append("     L5.PREF_NAME, ");
        stb.append("     L4.ADDR1, ");
        stb.append("     L4.ADDR2, ");
        stb.append("     L4.ADDR3, ");
        stb.append("     L3.GUARD_NAME AS GNAME, ");
        stb.append("     L3.GUARD_KANA AS GKANA, ");
        stb.append("     L3.GUARD_ZIPCD AS GZIP, ");
        stb.append("     L3.GUARD_PREF_CD AS GPREF, ");
        stb.append("     L6.PREF_NAME AS GPREF_NAME, ");
        stb.append("     L3.GUARD_ADDR1 AS GADDR1, ");
        stb.append("     L3.GUARD_ADDR2 AS GADDR2, ");
        stb.append("     L3.GUARD_ADDR3 AS GADDR3, ");
        stb.append("     VALUE(N2.NAMECD2, '0') AS OLD_CURRICULUM ");
        stb.append(" FROM ");
        stb.append("     V_SCHREG_INFO VSCH ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON VSCH.SCHREGNO = BASE.SCHREGNO ");
        stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z002' ");
        stb.append("          AND VSCH.SEX = N1.NAMECD2 ");
        stb.append("     LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'W002' ");
        stb.append("          AND  BASE.CURRICULUM_YEAR BETWEEN N2.NAMESPARE1 AND N2.NAMESPARE2 ");
        stb.append("     LEFT JOIN V_COURSE_MAJOR_MST L1 ON VSCH.YEAR = L1.YEAR ");
        stb.append("          AND VSCH.COURSECD = L1.COURSECD ");
        stb.append("          AND VSCH.MAJORCD = L1.MAJORCD ");
        stb.append("     LEFT JOIN V_COURSECODE_MST L2 ON VSCH.YEAR = L2.YEAR ");
        stb.append("          AND VSCH.COURSECODE = L2.COURSECODE ");
        stb.append("     LEFT JOIN GUARDIAN_DAT L3 ON VSCH.SCHREGNO = L3.SCHREGNO ");
        stb.append("     LEFT JOIN (SELECT ");
        stb.append("                    W1.* ");
        stb.append("                FROM ");
        stb.append("                    SCHREG_ADDRESS_DAT W1 ");
        stb.append("                WHERE ");
        stb.append("                    W1.SCHREGNO = ? ");
        stb.append("                    AND W1.ISSUEDATE = (SELECT ");
        stb.append("                                            MAX(W2.ISSUEDATE) AS ISSUEDATE ");
        stb.append("                                        FROM ");
        stb.append("                                            SCHREG_ADDRESS_DAT W2 ");
        stb.append("                                        WHERE ");
        stb.append("                                            W2.SCHREGNO = ?) ");
        stb.append("               ) L4 ON VSCH.SCHREGNO = L4.SCHREGNO ");
        stb.append("     LEFT JOIN PREF_MST L5 ON L4.PREF_CD = L5.PREF_CD ");
        stb.append("     LEFT JOIN PREF_MST L6 ON L3.GUARD_PREF_CD = L6.PREF_CD ");
        stb.append("     LEFT JOIN FIN_JUNIOR_HIGHSCHOOL_MST L7 ON BASE.FINSCHOOLCD = L7.SCHOOL_CD ");
        stb.append("     LEFT JOIN NAME_MST L8 ON L8.NAMECD1 = 'L001' ");
        stb.append("          AND L7.FINSCOOL_DISTCD = L8.NAMECD2 ");
        stb.append("  ");
        stb.append(" WHERE ");
        stb.append("     VSCH.YEAR = '" + year + "' ");
        stb.append("     AND VSCH.SEMESTER = '" + semester + "' ");
        stb.append("     AND VSCH.SCHREGNO = ? ");

        return stb.toString();
    }

    class EntGrd {
        final String _div;
        final String _name;
        final String _nendo;
        final String _date;
        final String _school;
        final String _addr;
        final String _reason;

        public EntGrd(final String div,
                       final String name,
                       final String nendo,
                       final String date,
                       final String school,
                       final String addr,
                       final String reason
        ) {
            _div = div;
            _name = name;
            _nendo = nendo;
            _date = date;
            _school = school;
            _addr = addr;
            _reason = reason;
        }

        public String toString() {
            return "区分：" + _div + "=" + _name;
        }
    }

    class AftGrd {
        final String _statName;
        final String _areaName;
        final String _thinkExam;

        public AftGrd(final String statName,
                      final String areaName,
                      final String thinkExam
        ) {
            _statName = statName;
            _areaName = areaName;
            _thinkExam = thinkExam;
        }

        public String toString() {
            return _statName;
        }
    }

    class HrClass {
        final String _year;
        final String _sort;
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _hrNameAbbv;
        final String _staffName;
        final String _staffName2;
        final String _staffName3;

        public HrClass(final String year,
                       final String sort,
                       final String grade,
                       final String hrClass,
                       final String hrName,
                       final String hrNameAbbv,
                       final String staffName,
                       final String staffName2,
                       final String staffName3
        ) {
            _year = year;
            _sort = sort;
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _hrNameAbbv = hrNameAbbv;
            _staffName = staffName;
            _staffName2 = staffName2;
            _staffName3 = staffName3;
        }

        public String toString() {
            return "年度：" + _year + " クラス：" + _hrName;
        }
    }

    class Transfer {
        final String _cd;
        final String _name;
        final String _sdate;
        final String _edate;
        final String _reason;
        final String _place;
        final String _addr;
        final String _classdays;
        final String _credits;

        public Transfer(final String cd,
                        final String name,
                        final String sdate,
                        final String edate,
                        final String reason,
                        final String place,
                        final String addr,
                        final String classdays,
                        final String credits
        ) {
            _cd = cd;
            _name = name;
            _sdate = sdate;
            _edate = edate;
            _reason = reason;
            _place = place;
            _addr = addr;
            _classdays = classdays;
            _credits = credits;
        }

        public String toString() {
            return "日付：" + _sdate + " 種別：" + _cd + "=" + _name;
        }
    }

    class CertifSchool {
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

        public CertifSchool(
        ) {
            _syosyoName = "";
            _syosyoName2 = "";
            _schoolName = "";
            _jobName = "";
            _principalName = "";
            _remark1 = "";
            _remark2 = "";
            _remark3 = "";
            _remark4 = "";
            _remark5 = "";
            _remark6 = "";
            _remark7 = "";
            _remark8 = "";
            _remark9 = "";
            _remark10 = "";
        }
        public CertifSchool(
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

}
 // KNJWA230

// eof
