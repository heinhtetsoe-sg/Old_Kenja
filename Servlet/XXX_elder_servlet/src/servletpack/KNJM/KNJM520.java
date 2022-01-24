// kanji=漢字
/*
 * $Id: 77b98793e3ee95dfbe52d96404933e84849fcbe6 $
 *
 * 作成日: 2008/09/16 13:15:49 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2008-2012 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJM;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 77b98793e3ee95dfbe52d96404933e84849fcbe6 $
 */
public class KNJM520 {

    private static final Log log = LogFactory.getLog(KNJM520.class);

    Param _param;

    /**
     * @param args
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意

        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;

        try {
            init(response, svf);

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            boolean hasData = false;
            hasData = _param._form.formPrintOut(svf, db2);
            if (!hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }

        } finally {
            close(db2, svf);
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

    private void close(final DB2UDB db2, final Vrw32alp svf) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
        if (null != svf) {
            svf.VrQuit();
        }
    }

    abstract class Form {
        private final String _name;
        private final int _type;
        private final String _title;

        /**
         * コンストラクタ。
         */
        public Form(final String name, final int type, final String title) {
            _name = name;
            _type = type;
            _title = title;
        }

        /**
         * 印刷処理
         * @param svf
         * @param db2
         * @return
         */
        abstract boolean formPrintOut(final Vrw32alp svf, final DB2UDB db2) throws SQLException ;

        public void setHead(final Vrw32alp svf) {
            svf.VrSetForm(_name, _type);
            svf.VrsOut("DATE", _param._makeDate);
        }

    }

    private class Form520_1 extends Form {

        public Form520_1() {
            super("KNJM520_1.frm", 1, "日付別出欠状況リスト");
        }


        /**
         * {@inheritDoc}
         */
        boolean formPrintOut(final Vrw32alp svf, final DB2UDB db2) throws SQLException {
            boolean hasData = false;
            db2.query(getExeDateSql("ALL"));
            ResultSet rs = null;
            rs = db2.getResultSet();
            final List printList = new ArrayList();
            while (rs.next()) {
                PrintData printData = new PrintData(rs.getString("SEMESTER"), rs.getString("EXECUTEDATE"), rs.getString("CNT"));
                printList.add(printData);
            }

            setHead(svf);
            int gyo = 1;
            int i = 1;
            String semCheck = "kakuninn";
            boolean firstFlag = true;
            for (final Iterator iter = printList.iterator(); iter.hasNext();) {
                if (gyo > 30) {
                    i = changeRetsu(svf, i);
                    gyo = 1;
                }

                final PrintData printData = (PrintData) iter.next();
                if (!semCheck.equals(printData._semester) && !firstFlag) {
                    i = changeRetsu(svf, i);
                    gyo = 1;
                }
                svf.VrsOutn("EXECUTEDATE" + i  ,gyo, printData._exeDate);
                svf.VrsOutn("ATTEND" + i  ,gyo, printData._cnt);
                svf.VrsOut("SEMESTER" + i, _param.getSemesterName(printData._semester));


                log.debug(printData);
                hasData = true;
                gyo++;
                semCheck = printData._semester;
                firstFlag = false;
            }
            svf.VrEndPage();
            return hasData;
        }


        private int changeRetsu(final Vrw32alp svf, int i) {
            if (i == 1) {
                i = 2;
            } else {
                svf.VrEndPage();
                i = 1;
            }
            return i;
        }

        private class PrintData {
            final String _semester;
            final String _exeDate;
            final String _cnt;
            /**
             * コンストラクタ。
             */
            public PrintData(final String semester, final String exeDate, final String cnt) {
                _semester = semester;
                _exeDate = exeDate.replace('-', '/');
                _cnt = cnt;
            }

            /**
             * {@inheritDoc}
             */
            public String toString() {
                return _semester + "学期：" + _exeDate + "：" + _cnt;
            }
        }
    }

    private String getExeDateSql(final String selectDiv) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH EXE_DATE_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     YEAR, ");
        stb.append("     SEMESTER, ");
        stb.append("     EXECUTEDATE ");
        stb.append(" FROM ");
        stb.append("     SCH_CHR_T_DAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + _param._year + "' ");
        stb.append("     AND VALUE(EXECUTED, '0') = '1' ");
        if (selectDiv.equals("DATE")) {
            stb.append("     AND EXECUTEDATE = '" + _param._exeData + "' ");
        }
        stb.append(" GROUP BY ");
        stb.append("     YEAR, ");
        stb.append("     SEMESTER, ");
        stb.append("     EXECUTEDATE ");
        stb.append(" ), SCHREG_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.EXECUTEDATE, ");
        stb.append("     T1.SCHREGNO ");
        stb.append(" FROM ");
        stb.append("     SCH_ATTEND_DAT T1, ");
        stb.append("     EXE_DATE_T T2 ");
        stb.append(" WHERE ");
        stb.append("     T1.EXECUTEDATE = T2.EXECUTEDATE ");
        stb.append("     AND T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND VALUE(T1.SCHOOLINGKINDCD, '0') = '1' ");
        stb.append(" GROUP BY ");
        stb.append("     T1.EXECUTEDATE, ");
        stb.append("     T1.SCHREGNO ");
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     T1.EXECUTEDATE, ");
        stb.append("     T1.SCHREGNO ");
        stb.append(" FROM ");
        stb.append("     HR_ATTEND_DAT T1, ");
        stb.append("     EXE_DATE_T T2 ");
        stb.append(" WHERE ");
        stb.append("     T1.EXECUTEDATE = T2.EXECUTEDATE ");
        stb.append("     AND T1.YEAR = '" + _param._year + "' ");
        stb.append(" GROUP BY ");
        stb.append("     T1.EXECUTEDATE, ");
        stb.append("     T1.SCHREGNO ");
        stb.append(" ), CNT_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     EXECUTEDATE, ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     SCHREG_T ");
        stb.append(" GROUP BY ");
        stb.append("     EXECUTEDATE ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     T1.EXECUTEDATE, ");
        stb.append("     VALUE(L1.CNT, 0) AS CNT ");
        stb.append(" FROM ");
        stb.append("     EXE_DATE_T T1 ");
        stb.append("     LEFT JOIN CNT_T L1 ON T1.EXECUTEDATE = L1.EXECUTEDATE ");
        stb.append(" ORDER BY ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     T1.EXECUTEDATE ");

        return stb.toString();
    }

    private class Form520_2 extends Form {

        public Form520_2() {
            super("KNJM520_2.frm", 1, "日付・科目・校時別出欠状況リスト");
        }

        /**
         * {@inheritDoc}
         */
        boolean formPrintOut(final Vrw32alp svf, final DB2UDB db2) throws SQLException {
            boolean hasData = false;
            final List subclassList = getSubclasses(db2);

            setHead(svf);
            final String exeDate = KNJ_EditDate.h_format_JP(_param._exeData);
            svf.VrsOut("EXECUTEDATE", exeDate);

            db2.query(getExeDateSql("DATE"));
            ResultSet rs = null;
            rs = db2.getResultSet();
            if (rs.next()) {
                svf.VrsOut("TOTAL_ATTEND", rs.getString("CNT"));
            }
            db2.commit();

            for (final Iterator iter = _param._nameMstB001.iterator(); iter.hasNext();) {
                final Period period = (Period) iter.next();
                svf.VrsOut("PERIOD" + period._cd, period._abbv);
            }

            int gyo = 1;
            for (final Iterator iter = subclassList.iterator(); iter.hasNext();) {
                final Subclass subclass = (Subclass) iter.next();
                log.debug(subclass);
                if (gyo > 50) {
                    svf.VrEndPage();
                    gyo = 1;
                }
                svf.VrsOutn("SUBCLASS", gyo, subclass._subclassAbbv);
                for (final Iterator iterator = subclass._printData.iterator(); iterator.hasNext();) {
                    final PrintData printData = (PrintData) iterator.next();
                    log.debug(printData._periodCd + "校時：" + printData._cnt);
                    svf.VrsOutn("ATTEND" + printData._periodCd, gyo, printData._cnt);
                }
                gyo++;
                hasData = true;
            }
            if (gyo > 1) {
                svf.VrEndPage();
            }
            return hasData;
        }

        /**
         * @param db2
         * @return
         */
        private List getSubclasses(final DB2UDB db2) throws SQLException {
            final List retList = new ArrayList();
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("     CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD AS SUBCLASSCD, ");
            } else {
                stb.append("     SUBCLASSCD, ");
            }
            stb.append("     SUBCLASSABBV ");
            stb.append(" FROM ");
            stb.append("     V_SUBCLASS_MST ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _param._year + "'");
            stb.append(" ORDER BY ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("     CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD ");
            } else {
                stb.append("     SUBCLASSCD ");
            }
            PreparedStatement psSubclass = null;
            psSubclass = db2.prepareStatement(stb.toString());
            ResultSet rs = psSubclass.executeQuery();
            db2.commit();
            while (rs.next()) {
                final String subclassCd = rs.getString("SUBCLASSCD");
                final String subclassAbbv = rs.getString("SUBCLASSABBV");
                final Subclass subclass = new Subclass(subclassCd, subclassAbbv);
                subclass.setPrintData(db2);
                retList.add(subclass);
            }
            DbUtils.closeQuietly(null, psSubclass, rs);
            return retList;
        }

        private class Subclass {
            final String _subclassCd;
            final String _subclassAbbv;
            final List _printData = new ArrayList();

            public Subclass(final String subclassCd, final String subclassAbbv) {
                _subclassCd = subclassCd;
                _subclassAbbv = subclassAbbv;
            }

            private void setPrintData(final DB2UDB db2) throws SQLException {
                PreparedStatement psPrint = null;
                final String sql = getPrintDataSql();
                psPrint = db2.prepareStatement(sql);
                db2.commit();
                for (final Iterator iter = _param._nameMstB001.iterator(); iter.hasNext();) {
                    final Period period = (Period) iter.next();
                    int p = 1;
                    psPrint.setString(p++, period._cd);
                    psPrint.setString(p++, _subclassCd);
                    psPrint.setString(p++, period._cd);
                    psPrint.setString(p++, period._cd);
                    final String classCd = "1".equals(_param._useCurriculumcd) ? _subclassCd.substring(4,6) : _subclassCd.substring(0, 2);
                    psPrint.setString(p++, classCd);
                    ResultSet rs = psPrint.executeQuery();
                    while (rs.next()) {
                        final String cnt = rs.getString("CNT");
                        PrintData printData = new PrintData(period._cd, cnt);
                        _printData.add(printData);
                    }
                }
            }

            /**
             * @return
             */
            private String getPrintDataSql() {
                // TODO:HR_ATTENDの処理もいれる。
                final StringBuffer stb = new StringBuffer();
                stb.append(" WITH SCH_CHR_T AS ( ");
                stb.append(" SELECT ");
                stb.append("     T1.EXECUTEDATE, ");
                stb.append("     T1.CHAIRCD, ");
                stb.append("     T1.PERIODCD, ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("     L1.CLASSCD, ");
                    stb.append("     L1.SCHOOL_KIND, ");
                    stb.append("     L1.CURRICULUM_CD, ");
                }
                stb.append("     L1.SUBCLASSCD, ");
                stb.append("     VALUE(T1.EXECUTED, '0') AS EXECUTED ");
                stb.append(" FROM ");
                stb.append("     SCH_CHR_T_DAT T1 ");
                stb.append("     LEFT JOIN CHAIR_DAT L1 ON T1.YEAR = L1.YEAR ");
                stb.append("          AND T1.SEMESTER = L1.SEMESTER ");
                stb.append("          AND T1.CHAIRCD = L1.CHAIRCD ");
                stb.append(" WHERE ");
                stb.append("     T1.EXECUTEDATE = '" + _param._exeData + "' ");
                stb.append("     AND T1.YEAR = '" + _param._year + "' ");
                stb.append("     AND T1.PERIODCD = ? ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("     AND L1.CLASSCD || L1.SCHOOL_KIND || L1.CURRICULUM_CD || L1.SUBCLASSCD = ? ");
                } else {
                    stb.append("     AND L1.SUBCLASSCD = ? ");
                }
                stb.append(" ), SCHREG_T AS ( ");
                stb.append(" SELECT ");
                stb.append("     T1.EXECUTEDATE, ");
                stb.append("     T1.SCHREGNO, ");
                stb.append("     T1.CHAIRCD, ");
                stb.append("     T1.PERIODCD, ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("     L1.CLASSCD, ");
                    stb.append("     L1.SCHOOL_KIND, ");
                    stb.append("     L1.CURRICULUM_CD, ");
                }
                stb.append("     L1.SUBCLASSCD, ");
                stb.append("     L1.EXECUTED ");
                stb.append(" FROM ");
                stb.append("     SCH_ATTEND_DAT T1 ");
                stb.append("     INNER JOIN SCH_CHR_T L1 ON T1.EXECUTEDATE = L1.EXECUTEDATE ");
                stb.append("           AND T1.PERIODCD = L1.PERIODCD ");
                stb.append("           AND T1.CHAIRCD = L1.CHAIRCD ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + _param._year + "' ");
                stb.append("     AND T1.EXECUTEDATE = '" + _param._exeData + "' ");
                stb.append("     AND T1.PERIODCD = ? ");
                stb.append("     AND VALUE(T1.SCHOOLINGKINDCD, '0') = '1' ");
                stb.append(" GROUP BY ");
                stb.append("     T1.EXECUTEDATE, ");
                stb.append("     T1.SCHREGNO, ");
                stb.append("     T1.CHAIRCD, ");
                stb.append("     T1.PERIODCD, ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("     L1.CLASSCD, ");
                    stb.append("     L1.SCHOOL_KIND, ");
                    stb.append("     L1.CURRICULUM_CD, ");
                }
                stb.append("     L1.SUBCLASSCD, ");
                stb.append("     L1.EXECUTED ");

                stb.append(" ), SEME_T AS ( ");
                stb.append(" SELECT ");
                stb.append("     MAX(SEMESTER) AS SEMESTER ");
                stb.append(" FROM ");
                stb.append("     SEMESTER_MST ");
                stb.append(" WHERE ");
                stb.append("     YEAR = '" + _param._year + "' ");
                stb.append("     AND SEMESTER < '9' ");
                stb.append("     AND '" + _param._exeData + "' BETWEEN SDATE AND EDATE ");

                stb.append(" ), CHAIR_T AS ( ");
                stb.append(" SELECT ");
                stb.append("     T1.YEAR, ");
                stb.append("     T1.CHAIRCD, ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("     T1.CLASSCD, ");
                    stb.append("     T1.SCHOOL_KIND, ");
                    stb.append("     T1.CURRICULUM_CD, ");
                }
                stb.append("     T1.SUBCLASSCD ");
                stb.append(" FROM ");
                stb.append("     CHAIR_DAT T1 ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + _param._year + "' ");
                stb.append("     AND T1.SEMESTER IN (SELECT T2.SEMESTER FROM SEME_T T2) ");
                stb.append(" GROUP BY ");
                stb.append("     T1.YEAR, ");
                stb.append("     T1.CHAIRCD, ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("     T1.CLASSCD, ");
                    stb.append("     T1.SCHOOL_KIND, ");
                    stb.append("     T1.CURRICULUM_CD, ");
                }
                stb.append("     T1.SUBCLASSCD ");

                stb.append(" ), HR_ATTEND AS ( ");
                stb.append(" SELECT ");
                stb.append("     T1.SCHREGNO, ");
                stb.append("     T1.CHAIRCD, ");
                stb.append("     T1.PERIODCD, ");
                stb.append("     SUBSTR(L1.SUBCLASSCD, 1, 2) AS CLASSCD ");
                stb.append(" FROM ");
                stb.append("     HR_ATTEND_DAT T1 ");
                stb.append("     LEFT JOIN CHAIR_T L1 ON T1.YEAR = L1.YEAR ");
                stb.append("          AND T1.CHAIRCD = L1.CHAIRCD ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + _param._year + "' ");
                stb.append("     AND T1.EXECUTEDATE = '" + _param._exeData + "' ");
                stb.append("     AND T1.PERIODCD = ? ");
                stb.append("     AND SUBSTR(L1.SUBCLASSCD, 1, 2) = ? ");
                stb.append(" ), MAIN_T AS ( ");
                stb.append(" SELECT ");
                stb.append("     SUBSTR(SUBCLASSCD, 1, 2) AS CLASSCD, ");
                stb.append("     SUM(CASE WHEN EXECUTED = '1' ");
                stb.append("              THEN 1 ");
                stb.append("              ELSE 0 ");
                stb.append("         END ");
                stb.append("     ) AS CNT ");
                stb.append(" FROM ");
                stb.append("     SCHREG_T ");
                stb.append(" GROUP BY ");
                stb.append("     SUBCLASSCD ");
                stb.append(" UNION ALL ");
                stb.append(" SELECT ");
                stb.append("     CLASSCD, ");
                stb.append("     COUNT(*) AS CNT ");
                stb.append(" FROM ");
                stb.append("     HR_ATTEND ");
                stb.append(" GROUP BY ");
                stb.append("     CLASSCD ");
                stb.append(" ) ");
                stb.append(" SELECT ");
                stb.append("     CLASSCD, ");
                stb.append("     SUM(CNT) AS CNT ");
                stb.append(" FROM ");
                stb.append("     MAIN_T ");
                stb.append(" GROUP BY ");
                stb.append("     CLASSCD ");

                return stb.toString();
            }

            /**
             * {@inheritDoc}
             */
            public String toString() {
                return _subclassCd + " : " + _subclassAbbv;
            }
        }

        private class PrintData {
            final String _periodCd;
            final String _cnt;
            /**
             * コンストラクタ。
             */
            public PrintData(final String periodCd, final String cnt) {
                _periodCd = periodCd;
                _cnt = cnt;
            }

            /**
             * {@inheritDoc}
             */
            public String toString() {
                return _periodCd + "校時：" + _cnt;
            }
        }
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
        private final String _printDiv;
        private final String _exeData;
        private final String _makeDate;
        private final List _nameMstB001 = new ArrayList();
        private final Form _form;
        private final Map _semesterMst = new HashMap();
        private final String _useCurriculumcd;

        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _year = request.getParameter("YEAR");
            final String exeDate = request.getParameter("DATE");
            _exeData = null != exeDate ? exeDate.replace('/', '-') : "";
            _printDiv = request.getParameter("PRINT_DIV");
            _form = _printDiv.equals("1") ? (Form) new Form520_1() : (Form) new Form520_2();
            _useCurriculumcd = request.getParameter("useCurriculumcd");

            final StringBuffer stb = new StringBuffer();
            final Date date = new Date();
            final SimpleDateFormat sdfY = new SimpleDateFormat("yyyy");
            stb.append(nao_package.KenjaProperties.gengou(Integer.parseInt(sdfY.format(date))));
            final SimpleDateFormat sdf = new SimpleDateFormat("年M月d日");
            stb.append(sdf.format(date));
            _makeDate = stb.toString();

            setNameMst(db2);
            setSemesterMst(db2);
        }
        private void setSemesterMst(final DB2UDB db2) throws SQLException {
            final String sql = " SELECT "
            + "     SEMESTER, "
            + "     SEMESTERNAME "
            + " FROM "
            + "     SEMESTER_MST "
            + " WHERE "
            + "     YEAR = '" + _year + "'  ";
            db2.query(sql);
            ResultSet rs = db2.getResultSet();
            while (rs.next()) {
               final String sem = rs.getString("SEMESTER");
               final String semName = rs.getString("SEMESTERNAME");
               _semesterMst.put(sem,semName);
            }
        }
        private void setNameMst(final DB2UDB db2) throws SQLException {
            final String sql = " SELECT "
                             + "     NAMECD2, "
                             + "     ABBV1 "
                             + " FROM "
                             + "     V_NAME_MST "
                             + " WHERE "
                             + "     YEAR = '" + _year + "' "
                             + "     AND NAMECD1 = 'B001' "
                             + " ORDER BY "
                             + "     NAMECD2 ";
            db2.query(sql);
            ResultSet rs = db2.getResultSet();
            while (rs.next()) {
                final String cd = rs.getString("NAMECD2");
                final String abbv = rs.getString("ABBV1");
                _nameMstB001.add(new Period(cd, abbv));
            }
        }
        private String getSemesterName(final String semester) {
            return (String) _param._semesterMst.get(semester);
        }

    }

    private class Period {
        final String _cd;
        final String _abbv;

        public Period(final String cd, final String abbv) {
            _cd = cd;
            _abbv = abbv;
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return _cd + " : " + _abbv;
        }
    }

}
 // KNJM520

// eof
