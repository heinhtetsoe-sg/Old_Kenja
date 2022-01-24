// kanji=漢字
/*
 * $Id: cef1711016f6da1e929a8422aa3e8865fd906a6c $
 *
 * 作成日: 2007/07/02 17:19:09 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2007-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.IOException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJEditString;
import servletpack.KNJZ.detail.KNJObjectAbs;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * クラス別履修・修得単位／評定平均チェックリスト。
 * @author nakamoto
 * @version $Id: cef1711016f6da1e929a8422aa3e8865fd906a6c $
 */
public class KNJD236C {

    private static final String FORM_NAME = "KNJD236.frm";

    private static final Log log = LogFactory.getLog(KNJD236C.class);

    private KNJObjectAbs knjobj;        //編集用クラス

    Param _param;
    /**
     * KNJD.classから呼ばれる処理
     * @param request リクエスト
     * @param response レスポンス
     * @throws Exception I/O例外
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

            _param = createParam(request);
            
            _param.load(db2);
            
            knjobj = new KNJEditString();

            boolean hasData = false;

            svf.VrSetForm(FORM_NAME, 1);
            log.debug("印刷するフォーム:" + FORM_NAME);

            for (int i = 0; i < _param._groupList.length; i++) {
                if (printMain(db2, svf, _param._groupList[i])) hasData = true;
            }
            if (!hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } finally {
            close(db2, svf);
        }
    }
    
    private List groupList(final List list, final int max) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= max) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }

    /** 印刷処理メイン */
    private boolean printMain(
            final DB2UDB db2,
            final Vrw32alp svf,
            final String typeGroupCd
    ) throws Exception {
        boolean rtnflg = false;

        final List listSubClassAll = createSubClass(db2, typeGroupCd);
        log.debug("科目名=" + listSubClassAll);
        final int max = 18;
        final List groupList = groupList(listSubClassAll, max); // max行ごとに改ページ
        if (groupList.isEmpty()) {
            // 科目行数が0の場合、全て表示されないのでダミーのリストを追加
            groupList.add(new ArrayList());
        }

        for (final Iterator it = groupList.iterator(); it.hasNext();) {
            final List listSubClass = (List) it.next();
            
            //ヘッダ部
            printHeader(db2, svf, typeGroupCd);
            //[0]クラス毎の生徒数
            print0StudentCount(db2, svf, typeGroupCd);
            //[1]成績完備・成績不完備生徒数
            print1KanbiCount(db2, svf, typeGroupCd);
            print1FukanbiCount(db2, svf, typeGroupCd);
            print1Fukanbi(db2, svf, typeGroupCd);

            //[2]科目別平均点・欠点人員
            print2(db2, svf, typeGroupCd, listSubClass);
            //[3]成績下位者
            print3(db2, svf, typeGroupCd, listSubClass);
            //[4]生活全般
            print4(db2, svf, typeGroupCd);
            
            svf.VrEndPage();
            rtnflg = true;
        }

        return rtnflg;
    }

    private void printHeader(final DB2UDB db2, final Vrw32alp svf, final String typeGroupCd) throws SQLException {
        svf.VrsOut("NENDO"      , _param._gengou);
        svf.VrsOut("SEMESTER"   , _param._semesterName);
        svf.VrsOut("DATE"       , _param._loginDate);

        final List typeGroups = createTypeGroup(db2, typeGroupCd);
        log.debug("グループ名=" + typeGroups);
        for (final Iterator it = typeGroups.iterator(); it.hasNext();) {
            final TypeGroups typeGroup = (TypeGroups) it.next();
            svf.VrsOut("SUBTITLE"   , typeGroup._groupName);
        }
    }

    private void print0StudentCount(final DB2UDB db2, final Vrw32alp svf, final String typeGroupCd) throws SQLException {
        final String sql = sql0StudentCount(typeGroupCd);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            String tmpHrClass = "";
            int hrClassCnt = 0;
            while (rs.next()) {
                if (!tmpHrClass.equals(rs.getString("HR_CLASS"))) {
                    tmpHrClass = rs.getString("HR_CLASS");
                    hrClassCnt++;
                }

                int gyo = (3 < hrClassCnt) ? hrClassCnt - 3 : hrClassCnt;
                int len = (3 < hrClassCnt) ? 2 : 1;
//                log.debug("gyo="+gyo+", len="+len+", hrClassCnt="+hrClassCnt);

                svf.VrsOutn("HR_NAME1_"     + len, gyo, rs.getString("HR_NAME"));
                svf.VrsOutn("STAFFNAME1_"   + len, gyo, rs.getString("STAFFNAME"));
                if ("0".equals(rs.getString("SEX"))) {
                    svf.VrsOutn("STD_COUNT1_"   + len, gyo, rs.getString("CNT"));
                }
                if ("1".equals(rs.getString("SEX"))) {
                    svf.VrsOutn("BOY_COUNT1_"   + len, gyo, rs.getString("CNT"));
                }
                if ("2".equals(rs.getString("SEX"))) {
                    svf.VrsOutn("GIRL_COUNT1_"  + len, gyo, rs.getString("CNT"));
                }
            }
        } catch (final Exception ex) {
            log.error("クラス毎生徒数の取得でエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
    }
    
    private String sql0StudentCount(final String typeGroupCd) {
        final String sql;
        sql = ""
            + " WITH SCHNO_CNT AS ( "
            + "     SELECT "
            + "         T3.HR_CLASS, "
            + "         value(T4.SEX,'0') as SEX, "
            + "         count(*) as CNT "
            + "     FROM "
            + "         TYPE_GROUP_COURSE_DAT T2 "
            + "         INNER JOIN SCHREG_REGD_DAT T3 "
            + "                 ON T3.YEAR=T2.YEAR "
            + "                AND T3.SEMESTER='" + _param.getSchregSemester() + "' "
            + "                AND T3.GRADE=T2.GRADE "
            + "                AND T3.COURSECD=T2.COURSECD "
            + "                AND T3.MAJORCD=T2.MAJORCD "
            + "                AND T3.COURSECODE=T2.COURSECODE "
            + "         INNER JOIN SCHREG_BASE_MST T4 "
            + "                 ON T4.SCHREGNO=T3.SCHREGNO "
            + "     WHERE "
            + "         T2.YEAR='" + _param._year + "' AND "
            + "         T2.TYPE_GROUP_CD='" + typeGroupCd + "' AND "
            + "         T2.GRADE='" + _param._grade + "' "
            + "     GROUP BY "
            + "         GROUPING SETS((T3.HR_CLASS,T4.SEX),T3.HR_CLASS) "
            + "     ) "

            + " SELECT "
            + "     T1.HR_CLASS, "
            + "     T5.HR_NAME, "
            + "     T1.SEX, "
            + "     T1.CNT, "
            + "     T6.STAFFNAME "
            + " FROM "
            + "     SCHNO_CNT T1 "
            + "     INNER JOIN SCHREG_REGD_HDAT T5 "
            + "             ON T5.YEAR='" + _param._year + "' "
            + "            AND T5.SEMESTER='" + _param.getSchregSemester() + "' "
            + "            AND T5.GRADE='" + _param._grade + "' "
            + "            AND T5.HR_CLASS=T1.HR_CLASS "
            + "     LEFT JOIN STAFF_MST T6 "
            + "            ON T6.STAFFCD=T5.TR_CD1 "
            + " ORDER BY "
            + "     T1.HR_CLASS, "
            + "     T1.SEX "
            ;
        return sql;
    }

    private void print1Fukanbi(final DB2UDB db2, final Vrw32alp svf, final String typeGroupCd) throws SQLException {
        final String sql = sql1Fukanbi(typeGroupCd);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            String subAbbv = "";
            String tmpSubAbbv = "";
            String tmpSubcd = "";
            String tmpSchno = "";
            int gyo = 0;
            int subCnt = 0;
            while (rs.next()) {
                if (!tmpSchno.equals(rs.getString("SCHREGNO"))) {
                    tmpSchno = rs.getString("SCHREGNO");
                    gyo++;
                    if (15 < gyo) break;

                    tmpSubcd = "";
                    subCnt = 0;

                    subAbbv = "";
                }
                if (!tmpSubcd.equals(rs.getString("SUBCLASSCD"))) {
                    tmpSubcd = rs.getString("SUBCLASSCD");
                    subCnt++;

                    String seq = (subCnt <= 1) ? "" : ", ";
                    if (subCnt <= 10) {
                        subAbbv = subAbbv + seq + rs.getString("SUBCLASSABBV");
                    } else {
                        int hokaCnt = subCnt - 10;
                        tmpSubAbbv = subAbbv + seq + "他" + hokaCnt + "科目";
                    }
                }

                svf.VrsOutn("HR_NAME2"  , gyo, rs.getString("HR_NAME"));
                svf.VrsOutn("ATTENDNO2" , gyo, rs.getString("ATTENDNO"));
                svf.VrsOutn("NAME2"     , gyo, rs.getString("NAME"));

                int len=2;

                svf.VrsOutn("REMARK2_"           + len, gyo, rs.getString("REMARK1"));
                svf.VrsOutn("REMARK_SUBCLASS2_"  + len, gyo, (subCnt <= 10) ? subAbbv : tmpSubAbbv);
            }
        } catch (final Exception ex) {
            log.error("不完備生徒明細の取得でエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
    }
    
    private String sql1Fukanbi(final String typeGroupCd) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" WITH T_SCHNO AS ( ");
        stb.append("     SELECT ");
        stb.append("         T3.SCHREGNO, ");
        stb.append("         T3.HR_CLASS, ");
        stb.append("         T5.HR_NAME, ");
        stb.append("         T3.ATTENDNO, ");
        stb.append("         T4.NAME ");
        stb.append("     FROM ");
        stb.append("         TYPE_GROUP_COURSE_DAT T2 ");
        stb.append("         INNER JOIN SCHREG_REGD_DAT T3 ");
        stb.append("                 ON T3.YEAR=T2.YEAR ");
        stb.append("                AND T3.SEMESTER='" + _param.getSchregSemester() + "' ");
        stb.append("                AND T3.GRADE=T2.GRADE ");
        stb.append("                AND T3.COURSECD=T2.COURSECD ");
        stb.append("                AND T3.MAJORCD=T2.MAJORCD ");
        stb.append("                AND T3.COURSECODE=T2.COURSECODE ");
        stb.append("         INNER JOIN SCHREG_BASE_MST T4 ");
        stb.append("                 ON T4.SCHREGNO=T3.SCHREGNO ");
        stb.append("         INNER JOIN SCHREG_REGD_HDAT T5 ");
        stb.append("                 ON T3.YEAR=T5.YEAR ");
        stb.append("                AND T3.SEMESTER=T5.SEMESTER ");
        stb.append("                AND T3.GRADE=T5.GRADE ");
        stb.append("                AND T3.HR_CLASS=T5.HR_CLASS ");
        stb.append("     WHERE ");
        stb.append("         T2.YEAR='" + _param._year + "' AND ");
        stb.append("         T2.TYPE_GROUP_CD='" + typeGroupCd + "' AND ");
        stb.append("         T2.GRADE='" + _param._grade + "' ");
        stb.append("     ) ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.HR_NAME, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.NAME, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T2.CLASSCD || T2.SCHOOL_KIND || T2.CURRICULUM_CD || T2.SUBCLASSCD AS SUBCLASSCD, ");
        } else {
            stb.append("     T2.SUBCLASSCD, ");
        }
        stb.append("     T3.SUBCLASSABBV, ");
        stb.append("     T4.REMARK1 ");
        stb.append(" FROM ");
        stb.append("     T_SCHNO T1 ");
        stb.append("     INNER JOIN RECORD_SCORE_DAT T2 ");
        stb.append("             ON T2.YEAR='" + _param._year + "' ");
        stb.append("            AND T2.SEMESTER='" + _param._semester + "' ");
        stb.append("            AND T2.TESTKINDCD='99' ");
        stb.append("            AND T2.TESTITEMCD='00' ");
        stb.append("            AND T2.SCORE_DIV='00' ");
        stb.append("            AND SUBSTR(T2.SUBCLASSCD, 1, 2) <> '90' ");
        stb.append("            AND T2.SCHREGNO=T1.SCHREGNO ");
        stb.append("            AND T2.VALUE IS NULL ");
        stb.append("     INNER JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD=T2.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     AND T3.CLASSCD = T2.CLASSCD ");
            stb.append("     AND T3.SCHOOL_KIND = T2.SCHOOL_KIND ");
            stb.append("     AND T3.CURRICULUM_CD = T2.CURRICULUM_CD ");
        }
        stb.append("      LEFT JOIN HEXAM_RECORD_REMARK_DAT T4 ");
        stb.append("             ON T4.YEAR='" + _param._year + "' ");
        stb.append("            AND T4.SEMESTER='" + _param._semester + "' ");
        stb.append("            AND T4.TESTKINDCD='99' ");
        stb.append("            AND T4.TESTITEMCD='00' ");
        stb.append("            AND T4.REMARK_DIV='3' ");
        stb.append("            AND T4.SCHREGNO=T1.SCHREGNO ");
        stb.append(" ORDER BY ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T2.CLASSCD || T2.SCHOOL_KIND || T2.CURRICULUM_CD || T2.SUBCLASSCD ");
        } else {
            stb.append("     T2.SUBCLASSCD ");
        }
        return stb.toString();
    }

    private void print1KanbiCount(final DB2UDB db2, final Vrw32alp svf, final String typeGroupCd) throws SQLException {
        final String sql = sql1KanbiCount(typeGroupCd);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                svf.VrsOut("GOOD_COUNT2" , rs.getString("KANBI_CNT"));
            }
        } catch (final Exception ex) {
            log.error("完備生徒数の取得でエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
    }
    
    private String sql1KanbiCount(final String typeGroupCd) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" WITH T_SCHNO AS ( ");
        stb.append("     SELECT ");
        stb.append("         T3.SCHREGNO, ");
        stb.append("         T3.HR_CLASS, ");
        stb.append("         T5.HR_NAME, ");
        stb.append("         T3.ATTENDNO, ");
        stb.append("         T4.NAME ");
        stb.append("     FROM ");
        stb.append("         TYPE_GROUP_COURSE_DAT T2 ");
        stb.append("         INNER JOIN SCHREG_REGD_DAT T3 ");
        stb.append("                 ON T3.YEAR=T2.YEAR ");
        stb.append("                AND T3.SEMESTER='" + _param.getSchregSemester() + "' ");
        stb.append("                AND T3.GRADE=T2.GRADE ");
        stb.append("                AND T3.COURSECD=T2.COURSECD ");
        stb.append("                AND T3.MAJORCD=T2.MAJORCD ");
        stb.append("                AND T3.COURSECODE=T2.COURSECODE ");
        stb.append("         INNER JOIN SCHREG_BASE_MST T4 ");
        stb.append("                 ON T4.SCHREGNO=T3.SCHREGNO ");
        stb.append("         INNER JOIN SCHREG_REGD_HDAT T5 ");
        stb.append("                 ON T3.YEAR=T5.YEAR ");
        stb.append("                AND T3.SEMESTER=T5.SEMESTER ");
        stb.append("                AND T3.GRADE=T5.GRADE ");
        stb.append("                AND T3.HR_CLASS=T5.HR_CLASS ");
        stb.append("     WHERE ");
        stb.append("         T2.YEAR='" + _param._year + "' AND ");
        stb.append("         T2.TYPE_GROUP_CD='" + typeGroupCd + "' AND ");
        stb.append("         T2.GRADE='" + _param._grade + "' ");
        stb.append("     ) ");
        stb.append(" , T_FUKANBI AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.SCHREGNO ");
        stb.append("     FROM ");
        stb.append("         T_SCHNO T1 ");
        stb.append("         INNER JOIN RECORD_SCORE_DAT T2 ");
        stb.append("                 ON T2.YEAR='" + _param._year + "' ");
        stb.append("                AND T2.SEMESTER='" + _param._semester + "' ");
        stb.append("                AND T2.TESTKINDCD='99' ");
        stb.append("                AND T2.TESTITEMCD='00' ");
        stb.append("                AND T2.SCORE_DIV='00' ");
        stb.append("                AND SUBSTR(T2.SUBCLASSCD, 1, 2) <> '90' ");
        stb.append("                AND T2.SCHREGNO=T1.SCHREGNO ");
        stb.append("                AND T2.VALUE IS NULL ");
        stb.append("         INNER JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD=T2.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     AND T3.CLASSCD = T2.CLASSCD ");
            stb.append("     AND T3.SCHOOL_KIND = T2.SCHOOL_KIND ");
            stb.append("     AND T3.CURRICULUM_CD = T2.CURRICULUM_CD ");
        }
        stb.append("     GROUP BY ");
        stb.append("         T1.SCHREGNO ");
        stb.append("     ) ");
        stb.append(" SELECT ");
        stb.append("     COUNT(DISTINCT T1.SCHREGNO) AS KANBI_CNT ");
        stb.append(" FROM ");
        stb.append("     T_SCHNO T1 ");
        stb.append("     INNER JOIN RECORD_SCORE_DAT T2 ");
        stb.append("             ON T2.YEAR='" + _param._year + "' ");
        stb.append("            AND T2.SEMESTER='" + _param._semester + "' ");
        stb.append("            AND T2.TESTKINDCD='99' ");
        stb.append("            AND T2.TESTITEMCD='00' ");
        stb.append("            AND T2.SCORE_DIV='00' ");
        stb.append("            AND T2.SCHREGNO=T1.SCHREGNO ");
        stb.append("            AND T2.VALUE IS NOT NULL ");
        stb.append(" WHERE ");
        stb.append("     T1.SCHREGNO NOT IN (SELECT W1.SCHREGNO FROM T_FUKANBI W1) ");
        return stb.toString();
    }

    private void print1FukanbiCount(final DB2UDB db2, final Vrw32alp svf, final String typeGroupCd) throws SQLException {
        final String sql = sql1FukanbiCount(typeGroupCd);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                svf.VrsOut("BAD_COUNT2" , rs.getString("FUKANBI_CNT"));
            }
        } catch (final Exception ex) {
            log.error("不完備生徒数の取得でエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
    }
    
    private String sql1FukanbiCount(final String typeGroupCd) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" WITH T_SCHNO AS ( ");
        stb.append("     SELECT ");
        stb.append("         T3.SCHREGNO, ");
        stb.append("         T3.HR_CLASS, ");
        stb.append("         T5.HR_NAME, ");
        stb.append("         T3.ATTENDNO, ");
        stb.append("         T4.NAME ");
        stb.append("     FROM ");
        stb.append("         TYPE_GROUP_COURSE_DAT T2 ");
        stb.append("         INNER JOIN SCHREG_REGD_DAT T3 ");
        stb.append("                 ON T3.YEAR=T2.YEAR ");
        stb.append("                AND T3.SEMESTER='" + _param.getSchregSemester() + "' ");
        stb.append("                AND T3.GRADE=T2.GRADE ");
        stb.append("                AND T3.COURSECD=T2.COURSECD ");
        stb.append("                AND T3.MAJORCD=T2.MAJORCD ");
        stb.append("                AND T3.COURSECODE=T2.COURSECODE ");
        stb.append("         INNER JOIN SCHREG_BASE_MST T4 ");
        stb.append("                 ON T4.SCHREGNO=T3.SCHREGNO ");
        stb.append("         INNER JOIN SCHREG_REGD_HDAT T5 ");
        stb.append("                 ON T3.YEAR=T5.YEAR ");
        stb.append("                AND T3.SEMESTER=T5.SEMESTER ");
        stb.append("                AND T3.GRADE=T5.GRADE ");
        stb.append("                AND T3.HR_CLASS=T5.HR_CLASS ");
        stb.append("     WHERE ");
        stb.append("         T2.YEAR='" + _param._year + "' AND ");
        stb.append("         T2.TYPE_GROUP_CD='" + typeGroupCd + "' AND ");
        stb.append("         T2.GRADE='" + _param._grade + "' ");
        stb.append("     ) ");

        stb.append(" SELECT ");
        stb.append("     COUNT(DISTINCT T1.SCHREGNO) AS FUKANBI_CNT ");
        stb.append(" FROM ");
        stb.append("     T_SCHNO T1 ");
        stb.append("     INNER JOIN RECORD_SCORE_DAT T2 ");
        stb.append("             ON T2.YEAR='" + _param._year + "' ");
        stb.append("            AND T2.SEMESTER='" + _param._semester + "' ");
        stb.append("            AND T2.TESTKINDCD='99' ");
        stb.append("            AND T2.TESTITEMCD='00' ");
        stb.append("            AND T2.SCORE_DIV='00' ");
        stb.append("            AND SUBSTR(T2.SUBCLASSCD, 1, 2) <> '90' ");
        stb.append("            AND T2.SCHREGNO=T1.SCHREGNO ");
        stb.append("            AND T2.VALUE IS NULL ");
        stb.append("     INNER JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD=T2.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("           AND T3.CLASSCD = T2.CLASSCD ");
            stb.append("           AND T3.SCHOOL_KIND = T2.SCHOOL_KIND ");
            stb.append("           AND T3.CURRICULUM_CD = T2.CURRICULUM_CD ");
        }
        return stb.toString();
    }

    private void print2(final DB2UDB db2, final Vrw32alp svf, final String typeGroupCd, final List listSubClass) throws SQLException {
        final List listHrClass = createHrClass(db2, typeGroupCd);
        log.debug("クラス名=" + listHrClass);

        int gyo = 0;
        for (final Iterator ith = listHrClass.iterator(); ith.hasNext();) {
            final HrClass rtnHrClass = (HrClass) ith.next();
            gyo++;
            svf.VrsOutn("HR_NAME3", gyo, rtnHrClass._hrName);
            print2HrAvg(db2, svf, rtnHrClass, gyo);

            int len = 0;
            for (final Iterator its = listSubClass.iterator(); its.hasNext();) {
                final SubClass rtnSubClass = (SubClass) its.next();
                len++;
                svf.VrsOutn("SUBCLASS3", len, rtnSubClass._subclassAbbv);

                print2Avg(db2, svf, rtnHrClass, rtnSubClass, gyo, len);
                print2BadCount(db2, svf, typeGroupCd, rtnHrClass, rtnSubClass, gyo, len);
            }
        }
    }

    private void print2HrAvg(final DB2UDB db2, final Vrw32alp svf, final HrClass rtnHrClass, int gyo) {
        final String sql = sql2HrAvg(rtnHrClass._hrCd);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                String average = rs.getString("HR_AVG");
                svf.VrsOutn("TOTAL_AVERAGE3", gyo, average);
            }
        } catch (final Exception ex) {
            log.error("学級平均点の取得でエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
    }
    
    private String sql2HrAvg(final String hrCd) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     sum(T2.SCORE) AS SCORE, ");
        stb.append("     sum(T2.COUNT) AS COUNT, ");
        stb.append("     decimal(round((float(sum(T2.SCORE))/sum(T2.COUNT))*10,0)/10,5,1) AS HR_AVG ");
        stb.append(" FROM ");
        stb.append("     RECORD_AVERAGE_DAT T2 ");
        stb.append("     INNER JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD=T2.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("           AND T3.CLASSCD = T2.CLASSCD ");
            stb.append("           AND T3.SCHOOL_KIND = T2.SCHOOL_KIND ");
            stb.append("           AND T3.CURRICULUM_CD = T2.CURRICULUM_CD ");
        }
        stb.append(" WHERE ");
        stb.append("         T2.YEAR='" + _param._year + "' ");
        stb.append("     AND T2.SEMESTER='" + _param._semester + "' ");
        stb.append("     AND T2.TESTKINDCD='99' ");
        stb.append("     AND T2.TESTITEMCD='00' ");
        stb.append("     AND T2.SUBCLASSCD not like '%0000' ");
        stb.append("     AND T2.AVG_DIV='2' ");
        stb.append("     AND T2.GRADE='" + _param._grade + "' ");
        stb.append("     AND T2.HR_CLASS='" + hrCd + "' ");
        return stb.toString();
    }

    private void print2Avg(final DB2UDB db2, final Vrw32alp svf, final HrClass rtnHrClass, final SubClass rtnSubClass, int gyo, int len) {
        final String sql = sql2Avg(rtnHrClass._hrCd, rtnSubClass._subclassCd);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                String average = rs.getString("AVERAGE");
                svf.VrsOutn("AVERAGE3_"+gyo, len, average);
            }
        } catch (final Exception ex) {
            log.error("平均点の取得でエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
    }
    
    private String sql2Avg(final String hrCd, final String subclassCd) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T2.CLASSCD || T2.SCHOOL_KIND || T2.CURRICULUM_CD || T2.SUBCLASSCD AS SUBCLASSCD, ");
        } else {
            stb.append("     T2.SUBCLASSCD, ");
        }
        stb.append("     T3.SUBCLASSABBV, ");
        stb.append("     T2.SCORE, ");
        stb.append("     T2.COUNT, ");
        stb.append("     decimal(round(float(T2.AVG)*10,0)/10,5,1) as AVERAGE ");
        stb.append(" FROM ");
        stb.append("     RECORD_AVERAGE_DAT T2 ");
        stb.append("     INNER JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD=T2.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("           AND T3.CLASSCD = T2.CLASSCD ");
            stb.append("           AND T3.SCHOOL_KIND = T2.SCHOOL_KIND ");
            stb.append("           AND T3.CURRICULUM_CD = T2.CURRICULUM_CD ");
        }
        stb.append(" WHERE ");
        stb.append("         T2.YEAR='" + _param._year + "' ");
        stb.append("     AND T2.SEMESTER='" + _param._semester + "' ");
        stb.append("     AND T2.TESTKINDCD='99' ");
        stb.append("     AND T2.TESTITEMCD='00' ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     AND T2.CLASSCD || T2.SCHOOL_KIND || T2.CURRICULUM_CD || T2.SUBCLASSCD='" + subclassCd + "' ");
        } else {
            stb.append("     AND T2.SUBCLASSCD='" + subclassCd + "' ");
        }
        stb.append("     AND T2.AVG_DIV='2' ");
        stb.append("     AND T2.GRADE='" + _param._grade + "' ");
        stb.append("     AND T2.HR_CLASS='" + hrCd + "' ");
        stb.append(" ORDER BY ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T2.CLASSCD || T2.SCHOOL_KIND || T2.CURRICULUM_CD || T2.SUBCLASSCD ");
        } else {
            stb.append("     T2.SUBCLASSCD ");
        }
        return stb.toString();
    }

    private void print2BadCount(final DB2UDB db2, final Vrw32alp svf, final String typeGroupCd, final HrClass rtnHrClass, final SubClass rtnSubClass, int gyo, int len) {
        final String sql = sql2BadCount(typeGroupCd, rtnHrClass._hrCd, rtnSubClass._subclassCd);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                String badCount = rs.getString("BAD_CNT");
                svf.VrsOutn("BAD_COUNT3_"+gyo, len, badCount);
            }
        } catch (final Exception ex) {
            log.error("欠点人員の取得でエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
    }
    
    private String sql2BadCount(final String typeGroupCd, final String hrCd, final String subclassCd) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH T_SCHNO AS ( ");
        stb.append("     SELECT ");
        stb.append("         T3.SCHREGNO, ");
        stb.append("         T3.HR_CLASS, ");
        stb.append("         T5.HR_NAME, ");
        stb.append("         T3.ATTENDNO, ");
        stb.append("         T4.NAME ");
        stb.append("     FROM ");
        stb.append("         TYPE_GROUP_COURSE_DAT T2 ");
        stb.append("         INNER JOIN SCHREG_REGD_DAT T3 ");
        stb.append("                 ON T3.YEAR=T2.YEAR ");
        stb.append("                AND T3.SEMESTER='" + _param.getSchregSemester() + "' ");
        stb.append("                AND T3.GRADE=T2.GRADE ");
        stb.append("                AND T3.HR_CLASS='" + hrCd + "' ");
        stb.append("                AND T3.COURSECD=T2.COURSECD ");
        stb.append("                AND T3.MAJORCD=T2.MAJORCD ");
        stb.append("                AND T3.COURSECODE=T2.COURSECODE ");
        stb.append("         INNER JOIN SCHREG_BASE_MST T4 ");
        stb.append("                 ON T4.SCHREGNO=T3.SCHREGNO ");
        stb.append("         INNER JOIN SCHREG_REGD_HDAT T5 ");
        stb.append("                 ON T3.YEAR=T5.YEAR ");
        stb.append("                AND T3.SEMESTER=T5.SEMESTER ");
        stb.append("                AND T3.GRADE=T5.GRADE ");
        stb.append("                AND T3.HR_CLASS=T5.HR_CLASS ");
        stb.append("     WHERE ");
        stb.append("         T2.YEAR='" + _param._year + "' AND ");
        stb.append("         T2.TYPE_GROUP_CD='" + typeGroupCd + "' AND ");
        stb.append("         T2.GRADE='" + _param._grade + "' ");
        stb.append("     ) ");

        stb.append(" SELECT ");
        stb.append("     COUNT(*) AS BAD_CNT ");
        stb.append(" FROM ");
        stb.append("     T_SCHNO T1 ");
        stb.append("     INNER JOIN RECORD_RANK_DAT T2 ");
        stb.append("             ON T2.YEAR='" + _param._year + "' ");
        stb.append("            AND T2.SEMESTER='" + _param._semester + "' ");
        stb.append("            AND T2.TESTKINDCD='99' ");
        stb.append("            AND T2.TESTITEMCD='00' ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("            AND T2.CLASSCD || T2.SCHOOL_KIND || T2.CURRICULUM_CD || T2.SUBCLASSCD='" + subclassCd + "' ");
        } else {
            stb.append("            AND T2.SUBCLASSCD='" + subclassCd + "' ");
        }
        stb.append("            AND T2.SCHREGNO=T1.SCHREGNO ");
        stb.append("            AND T2.SCORE < " + _param._badScore + " ");
        stb.append("     INNER JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD=T2.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("           AND T3.CLASSCD = T2.CLASSCD ");
            stb.append("           AND T3.SCHOOL_KIND = T2.SCHOOL_KIND ");
            stb.append("           AND T3.CURRICULUM_CD = T2.CURRICULUM_CD ");
        }
        return stb.toString();
    }

    private void print3(final DB2UDB db2, final Vrw32alp svf, final String typeGroupCd, final List listSubClass) throws SQLException {
        final String sql = sql3Rank(typeGroupCd);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            int gyo = 0;
            while (rs.next()) {
                gyo++;
                int kaiRank = rs.getInt("KAI_RANK");
                if ((15 < gyo) | _param._rankNo < kaiRank) break;

                svf.VrsOutn("HR_NAME4"  , gyo, rs.getString("HR_NAME"));
                svf.VrsOutn("RANK4"     , gyo, rs.getString("RANK"));
                svf.VrsOutn("NAME4"     , gyo, rs.getString("NAME"));
                svf.VrsOutn("TOTAL4"    , gyo, rs.getString("SCORE"));
                svf.VrsOutn("AVERAGE4"  , gyo, rs.getString("AVERAGE"));

                String schregno = rs.getString("SCHREGNO");
                int len = 0;
                for (final Iterator its = listSubClass.iterator(); its.hasNext();) {
                    final SubClass rtnSubClass = (SubClass) its.next();
                    len++;
                    svf.VrsOutn("SUBCLASS4", len, rtnSubClass._subclassAbbv);

                    print3Score(db2, svf, schregno, rtnSubClass._subclassCd, gyo, len);
                }
            }
        } catch (final Exception ex) {
            log.error("順位の取得でエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
    }
    
    private String sql3Rank(final String typeGroupCd) {
        final String sql;
        sql = ""
            + " WITH T_SCHNO AS ( "
            + "     SELECT "
            + "         T3.SCHREGNO, "
            + "         T3.HR_CLASS, "
            + "         T5.HR_NAME, "
            + "         T3.ATTENDNO, "
            + "         T4.NAME "
            + "     FROM "
            + "         TYPE_GROUP_COURSE_DAT T2 "
            + "         INNER JOIN SCHREG_REGD_DAT T3 "
            + "                 ON T3.YEAR=T2.YEAR "
            + "                AND T3.SEMESTER='" + _param.getSchregSemester() + "' "
            + "                AND T3.GRADE=T2.GRADE "
            + "                AND T3.COURSECD=T2.COURSECD "
            + "                AND T3.MAJORCD=T2.MAJORCD "
            + "                AND T3.COURSECODE=T2.COURSECODE "
            + "         INNER JOIN SCHREG_BASE_MST T4 "
            + "                 ON T4.SCHREGNO=T3.SCHREGNO "
            + "         INNER JOIN SCHREG_REGD_HDAT T5 "
            + "                 ON T3.YEAR=T5.YEAR "
            + "                AND T3.SEMESTER=T5.SEMESTER "
            + "                AND T3.GRADE=T5.GRADE "
            + "                AND T3.HR_CLASS=T5.HR_CLASS "
            + "     WHERE "
            + "         T2.YEAR='" + _param._year + "' AND "
            + "         T2.TYPE_GROUP_CD='" + typeGroupCd + "' AND "
            + "         T2.GRADE='" + _param._grade + "' "
            + "     ) "

            + " SELECT "
            + "     T1.SCHREGNO, "
            + "     T1.NAME, "
            + "     T1.HR_CLASS, "
            + "     T1.HR_NAME, "
            + "     T2.SCORE, "
            + "     decimal(round(float(T2.AVG)*10,0)/10,5,1) as AVERAGE, "
            + "     RANK() OVER(ORDER BY T2.AVG DESC) as RANK, "
            + "     RANK() OVER(ORDER BY T2.AVG) as KAI_RANK "
            + " FROM "
            + "     T_SCHNO T1 "
            + "     INNER JOIN RECORD_RANK_DAT T2 "
            + "             ON T2.YEAR='" + _param._year + "' "
            + "            AND T2.SEMESTER='" + _param._semester + "' "
            + "            AND T2.TESTKINDCD='99' "
            + "            AND T2.TESTITEMCD='00' "
            + "            AND T2.SUBCLASSCD= '999999' "
            + "            AND T2.SCHREGNO=T1.SCHREGNO "
            + "            AND T2.COURSE_RANK IS NOT NULL "
            + " ORDER BY "
            + "     T2.AVG, "
            + "     T1.HR_CLASS, "
            + "     T1.ATTENDNO "
            ;
        return sql;
    }

    private void print3Score(final DB2UDB db2, final Vrw32alp svf, final String schregno, final String subclassCd, int gyo, int len) {
        final String sql = sql3Score(schregno, subclassCd);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                String score = rs.getString("SCORE");
                if (score != null) {
                    if (Integer.parseInt(score) < _param._badScore) score = "(" + score + ")";
                }
                svf.VrsOutn("SCORE4_"+gyo, len, score);
            }
        } catch (final Exception ex) {
            log.error("得点の取得でエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
    }
    
    private String sql3Score(final String schregno, final String subclassCd) {

        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T2.SCHREGNO, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T2.CLASSCD || T2.SCHOOL_KIND || T2.CURRICULUM_CD || T2.SUBCLASSCD AS SUBCLASSCD, ");
        } else {
            stb.append("     T2.SUBCLASSCD, ");
        }
        stb.append("     T2.SCORE ");
        stb.append(" FROM ");
        stb.append("     RECORD_RANK_DAT T2 ");
        stb.append(" WHERE ");
        stb.append("         T2.YEAR='" + _param._year + "' ");
        stb.append("     AND T2.SEMESTER='" + _param._semester + "' ");
        stb.append("     AND T2.TESTKINDCD='99' ");
        stb.append("     AND T2.TESTITEMCD='00' ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     AND T2.CLASSCD || T2.SCHOOL_KIND || T2.CURRICULUM_CD || T2.SUBCLASSCD='" + subclassCd + "' ");
        } else {
            stb.append("     AND T2.SUBCLASSCD='" + subclassCd + "' ");
        }
        stb.append("     AND T2.SCHREGNO='" + schregno + "' ");
        stb.append(" ORDER BY ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T2.CLASSCD || T2.SCHOOL_KIND || T2.CURRICULUM_CD || T2.SUBCLASSCD ");
        } else {
            stb.append("     T2.SUBCLASSCD ");
        }
        return stb.toString();
    }

    private void print4(final DB2UDB db2, final Vrw32alp svf, final String typeGroupCd) throws SQLException {
        final String sql = sql4(typeGroupCd);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                ArrayList arrlist = knjobj.retDividString( rs.getString("REMARK1"), 120, 4 );
                if (arrlist != null) {
                    for (int i = 0; i < arrlist.size(); i++) {
                        svf.VrsOutn("REMARK", i+1,  (String)arrlist.get(i) );
                    }
                }
            }
        } catch (final Exception ex) {
            log.error("生活全般の取得でエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
    }
    
    private String sql4(final String typeGroupCd) {
        final String sql;
        sql = ""
            + " SELECT "
            + "     GRADE, "
            + "     TYPE_GROUP_CD, "
            + "     REMARK1 "
            + " FROM "
            + "     HEXAM_RECORD_DOCUMENT_DAT "
            + " WHERE "
            + "     YEAR='" + _param._year + "' AND "
            + "     SEMESTER='" + _param._semester + "' AND "
            + "     GRADE='" + _param._grade + "' AND "
            + "     TYPE_GROUP_CD='" + typeGroupCd + "' "
            ;
        return sql;
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

    private Param createParam(final HttpServletRequest request) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);

        final String year = request.getParameter("YEAR");
        final String semester = request.getParameter("SEMESTER");
        final String grade = request.getParameter("GRADE");
        final String groupList[] = request.getParameterValues("CLASS_SELECTED");
        final String loginDate = request.getParameter("LOGIN_DATE");
        final String badScore = request.getParameter("BAD_SCORE");
        final String rankNo = request.getParameter("RANK_NO");
        final String useCurriculumcd = request.getParameter("useCurriculumcd");

        final Param param = new Param(
                year,
                semester,
                grade,
                groupList,
                loginDate,
                Integer.parseInt(badScore),
                Integer.parseInt(rankNo),
                useCurriculumcd
        );
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _gengou;
        private final String _semester;
        private final String _grade;
        private final String _loginDate;
        private final String[] _groupList;
        private final int _badScore;
        private final int _rankNo;
        private final String _useCurriculumcd;

        private String _semesterName;
        private String _semesterSdate;
        private String _semesterEdate;
        private String _semesterMax;

        public Param(
                final String year,
                final String semester,
                final String grade,
                final String[] groupList,
                final String loginDate,
                final int badScore,
                final int rankNo,
                final String useCurriculumcd
        ) {
            _year = year;
            _semester = semester;
            _grade = grade;
            _groupList = groupList;
            _loginDate = KNJ_EditDate.h_format_JP(loginDate);
            final String gengou = nao_package.KenjaProperties.gengou(Integer.parseInt(year));
            _gengou = gengou + "年度";
            _badScore = badScore;
            _rankNo = rankNo;
            _useCurriculumcd = useCurriculumcd;
        }

        public void load(final DB2UDB db2) {
            createSemesterName(db2);
            createSchregSeme(db2);
        }

        public void createSemesterName(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String sql = "SELECT SEMESTER,SEMESTERNAME,SDATE,EDATE FROM SEMESTER_MST " +
                               "WHERE YEAR='" + _year + "' AND SEMESTER='" + _semester + "'";
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _semesterName = rs.getString("SEMESTERNAME");
                    _semesterSdate = rs.getString("SDATE");
                    _semesterEdate = rs.getString("EDATE");
                }
            } catch (final Exception ex) {
                log.error("学期名の取得でエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.debug("学期名:" + _semesterName);
        }

        public void createSchregSeme(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String sql = "SELECT max(SEMESTER) as SEMESTER FROM SCHREG_REGD_HDAT " +
                               "WHERE YEAR='" + _year + "' AND GRADE='" + _grade + "'";
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _semesterMax = rs.getString("SEMESTER");
                }
            } catch (final Exception ex) {
                log.error("MAX学期の取得でエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.debug("MAX学期:" + _semesterMax);
        }

        public String getSchregSemester() {
            return ("9".equals(_semester)) ? _semesterMax : _semester;
        }
    }

    private List createTypeGroup(final DB2UDB db2, final String typeGroupCd) throws SQLException {
        final List rtn = new ArrayList();
        final String sql = sqlTypeGroupCourseMst(typeGroupCd);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String groupCd = rs.getString("TYPE_GROUP_CD");
                final String groupName = rs.getString("TYPE_GROUP_NAME");

                final TypeGroups typeGroups = new TypeGroups(groupCd, groupName);
                rtn.add(typeGroups);
            }
        } catch (final Exception ex) {
            log.error("グループ名の取得でエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return rtn;
    }
    
    private String sqlTypeGroupCourseMst(final String typeGroupCd) {
        final String sql;
        sql = " SELECT "
            + "     TYPE_GROUP_CD, "
            + "     TYPE_GROUP_NAME "
            + " FROM "
            + "     TYPE_GROUP_COURSE_MST "
            + " WHERE "
            + "     YEAR='" + _param._year + "' AND "
            + "     TYPE_GROUP_CD='" + typeGroupCd + "' AND "
            + "     GRADE='" + _param._grade + "' "
            ;
        return sql;
    }

    private class TypeGroups {
        private final String _groupCd;
        private final String _groupName;

        public TypeGroups(
                final String groupCd,
                final String groupName
        ) {
            _groupCd = groupCd;
            _groupName = groupName;
        }

        public String toString() {
            return _groupCd + ":" + _groupName;
        }
    }

    private List createHrClass(final DB2UDB db2, final String typeGroupCd) throws SQLException {
        final List rtn = new ArrayList();
        final String sql = sqlHrClass(typeGroupCd);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String hrCd = rs.getString("HR_CLASS");
                final String hrName = rs.getString("HR_NAME");

                final HrClass rtnHrClass = new HrClass(hrCd, hrName);
                rtn.add(rtnHrClass);
            }
        } catch (final Exception ex) {
            log.error("クラス名の取得でエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return rtn;
    }

    private String sqlHrClass(final String typeGroupCd) {
        final String sql;
        sql = ""
            + " SELECT DISTINCT "
            + "     T3.HR_CLASS, "
            + "     T5.HR_NAME "
            + " FROM "
            + "     TYPE_GROUP_COURSE_DAT T2 "
            + "     INNER JOIN SCHREG_REGD_DAT T3 "
            + "             ON T3.YEAR=T2.YEAR "
            + "            AND T3.SEMESTER='" + _param.getSchregSemester() + "' "
            + "            AND T3.GRADE=T2.GRADE "
            + "            AND T3.COURSECD=T2.COURSECD "
            + "            AND T3.MAJORCD=T2.MAJORCD "
            + "            AND T3.COURSECODE=T2.COURSECODE "
            + "     INNER JOIN SCHREG_BASE_MST T4 "
            + "             ON T4.SCHREGNO=T3.SCHREGNO "
            + "     INNER JOIN SCHREG_REGD_HDAT T5 "
            + "             ON T3.YEAR=T5.YEAR "
            + "            AND T3.SEMESTER=T5.SEMESTER "
            + "            AND T3.GRADE=T5.GRADE "
            + "            AND T3.HR_CLASS=T5.HR_CLASS "
            + " WHERE "
            + "     T2.YEAR='" + _param._year + "' AND "
            + "     T2.TYPE_GROUP_CD='" + typeGroupCd + "' AND "
            + "     T2.GRADE='" + _param._grade + "' "
            ;
        return sql;
    }

    private class HrClass {
        private final String _hrCd;
        private final String _hrName;

        public HrClass(
                final String hrCd,
                final String hrName
        ) {
            _hrCd = hrCd;
            _hrName = hrName;
        }

        public String toString() {
            return _hrCd + ":" + _hrName;
        }
    }

    private List createSubClass(final DB2UDB db2, final String typeGroupCd) throws SQLException {
        final List rtn = new ArrayList();
        final String sql = sqlSubClass(typeGroupCd);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String subclassCd = rs.getString("SUBCLASSCD");
                final String subclassAbbv = rs.getString("SUBCLASSABBV");

                final SubClass rtnSubClass = new SubClass(subclassCd, subclassAbbv);
                rtn.add(rtnSubClass);
            }
        } catch (final Exception ex) {
            log.error("科目名の取得でエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return rtn;
    }

    private String sqlSubClass(final String typeGroupCd) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH T_SCHNO AS ( ");
        stb.append("     SELECT ");
        stb.append("         T3.HR_CLASS ");
        stb.append("     FROM ");
        stb.append("         TYPE_GROUP_COURSE_DAT T2 ");
        stb.append("         INNER JOIN SCHREG_REGD_DAT T3 ");
        stb.append("                 ON T3.YEAR=T2.YEAR ");
        stb.append("                AND T3.SEMESTER='" + _param.getSchregSemester() + "' ");
        stb.append("                AND T3.GRADE=T2.GRADE ");
        stb.append("                AND T3.COURSECD=T2.COURSECD ");
        stb.append("                AND T3.MAJORCD=T2.MAJORCD ");
        stb.append("                AND T3.COURSECODE=T2.COURSECODE ");
        stb.append("         INNER JOIN SCHREG_BASE_MST T4 ");
        stb.append("                 ON T4.SCHREGNO=T3.SCHREGNO ");
        stb.append("         INNER JOIN SCHREG_REGD_HDAT T5 ");
        stb.append("                 ON T3.YEAR=T5.YEAR ");
        stb.append("                AND T3.SEMESTER=T5.SEMESTER ");
        stb.append("                AND T3.GRADE=T5.GRADE ");
        stb.append("                AND T3.HR_CLASS=T5.HR_CLASS ");
        stb.append("     WHERE ");
        stb.append("         T2.YEAR='" + _param._year + "' AND ");
        stb.append("         T2.TYPE_GROUP_CD='" + typeGroupCd + "' AND ");
        stb.append("         T2.GRADE='" + _param._grade + "' ");
        stb.append("     GROUP BY ");
        stb.append("         T3.HR_CLASS ");
        stb.append("     ) ");

        stb.append(" SELECT DISTINCT ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T2.CLASSCD || T2.SCHOOL_KIND || T2.CURRICULUM_CD || T2.SUBCLASSCD AS SUBCLASSCD, ");
        } else {
            stb.append("     T2.SUBCLASSCD, ");
        }
        stb.append("     T3.SUBCLASSABBV ");
        stb.append(" FROM ");
        stb.append("     T_SCHNO T1 ");
        stb.append("     INNER JOIN RECORD_AVERAGE_DAT T2 ");
        stb.append("             ON T2.YEAR='" + _param._year + "' ");
        stb.append("            AND T2.SEMESTER='" + _param._semester + "' ");
        stb.append("            AND T2.TESTKINDCD='99' ");
        stb.append("            AND T2.TESTITEMCD='00' ");
        stb.append("            AND T2.SUBCLASSCD not like '%0000' ");
        stb.append("            AND T2.AVG_DIV='2' ");
        stb.append("            AND T2.GRADE='" + _param._grade + "' ");
        stb.append("            AND T2.HR_CLASS=T1.HR_CLASS ");
        stb.append("     INNER JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD=T2.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("           AND T3.CLASSCD = T2.CLASSCD ");
            stb.append("           AND T3.SCHOOL_KIND = T2.SCHOOL_KIND ");
            stb.append("           AND T3.CURRICULUM_CD = T2.CURRICULUM_CD ");
        }
        stb.append(" ORDER BY ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T2.CLASSCD || T2.SCHOOL_KIND || T2.CURRICULUM_CD || T2.SUBCLASSCD ");
        } else {
            stb.append("     T2.SUBCLASSCD ");
        }
        return stb.toString();
    }

    private class SubClass {
        private final String _subclassCd;
        private final String _subclassAbbv;

        public SubClass(
                final String subclassCd,
                final String subclassAbbv
        ) {
            _subclassCd = subclassCd;
            _subclassAbbv = subclassAbbv;
        }

        public String toString() {
            return _subclassCd + ":" + _subclassAbbv;
        }
    }
}
