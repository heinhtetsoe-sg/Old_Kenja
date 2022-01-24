package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
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


/**
 * 一覧
 */
public class KNJL431I {

    private static final Log log = LogFactory.getLog(KNJL431I.class);
    private boolean _hasData;
    private Param _param;

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        DB2UDB db2 = null;
        try {
            final String dbName = request.getParameter("DBNAME");
            db2 = new DB2UDB(dbName, "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

        } catch (final Exception e) {
            log.error("Exception:", e);
            return;
        }

        _param = createParam(db2, request);

        Vrw32alp svf = null;
        try {
            svf = new Vrw32alp();

            if (svf.VrInit() < 0) {
                throw new IllegalStateException("svf初期化失敗");
            }
            svf.VrSetSpoolFileStream(response.getOutputStream());
            response.setContentType("application/pdf");

            printMain(db2, svf);

        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }

            final int ret = svf.VrQuit();
            log.info("===> VrQuit():" + ret);

            if (null != db2) {
                db2.commit();
                db2.close();
            }
        }
    }

    private static List getPageList(final List studentlist, final int count) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = studentlist.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            if (null == current || current.size() >= count) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(student);
        }
        return rtn;
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws Exception {

        //生徒単位で回す
        final int maxLine = 20;
        final Map<String, List> intGroupSchListMap = getIntGroupSchListMap(db2);

        //面接班単位ループ
        int pageNum = 1;
        for (final String curIntGrpCd : _param._intGrpNameMap.keySet()) {

            final List<Student> studentList = intGroupSchListMap.get(curIntGrpCd);

            if (null == studentList) {
                continue;
            }

            svf.VrSetForm("KNJL431I.frm", 4);

            //ヘッダ印字
            printPageHeader(db2, svf, String.valueOf(pageNum), curIntGrpCd);

            //受験番号単位ループ
            int schCnt = 1;
            for (Student student : studentList) {

                if (schCnt >= maxLine) {
                    svf.VrEndPage();
                    pageNum++;
                    svf.VrSetForm("KNJL431I.frm", 4);
                    printPageHeader(db2, svf, String.valueOf(pageNum), curIntGrpCd);
                    schCnt = 1;
                }

                svf.VrAttribute("RECORD1", "Print=1");
                //受験番号
                svf.VrsOut("EXAM_NO", student._examno);
                //性別
                final String sexName = ("1".equals(student._sex)) ? "男" : "女";
                svf.VrsOut("SEX", sexName);

                schCnt++;
                _hasData = true;
                svf.VrEndRecord();
            }
            svf.VrEndPage();
            pageNum++;
        }

    }

    private void printPageHeader(final DB2UDB db2, final Vrw32alp svf, final String pageNum, final String intGrpCd) {
        //日付時刻
        final String dateStr = _param.convertDateFormat(_param._ctrlDate);
        svf.VrsOut("DATE", dateStr + " " + _param._time);

        //ページ数
        svf.VrsOut("PAGE", String.valueOf(pageNum) + "頁");

        //タイトル
        svf.VrsOut("TITLE", _param._examYear + "年度  入学試験 " + _param._testDivname + " 面接チェックリスト　(個人控え用)");

        //試験会場
        String outputDivName = "";
        if ("1".equals(_param._outputDiv)) {
            outputDivName = "(男女共)";
        } else if("2".equals(_param._outputDiv)) {
            outputDivName = "(男子のみ)";
        } else {
            outputDivName = "(女子のみ)";
        }
        final String intGrpName = (_param._intGrpNameMap.containsKey(intGrpCd)) ? _param._intGrpNameMap.get(intGrpCd) : "" ;
        svf.VrsOut("PLACE", "第 " + StringUtils.defaultString(intGrpName) + " 面接試験会場　　" + outputDivName);

    }

    private Map getIntGroupSchListMap(final DB2UDB db2)  throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final Map intGroupSchListMap = new LinkedHashMap();
        try {
            final String sql = getStudentSql();

            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            Map schMap = new TreeMap(); //通過した生徒を記録するマップ

            while (rs.next()) {
                final String examno 		= rs.getString("EXAMNO");
                final String sex   			= rs.getString("SEX");
                final String recGrpCd 		= rs.getString("RECEPT_GROUP_CD");
                final String intGrpCd  		= rs.getString("INTERVIEW_GROUP_CD");

                if (!schMap.containsKey(examno)) {
                    final Student student = new Student(examno, sex, recGrpCd, intGrpCd);
                    schMap.put(examno, student);
                }

                if (!intGroupSchListMap.containsKey(intGrpCd)) {
                    intGroupSchListMap.put(intGrpCd, new ArrayList());
                }

                //追加
                final Student student = (Student)schMap.get(examno);
                final List intGroupSchList = (List)intGroupSchListMap.get(intGrpCd);
                intGroupSchList.add(student);

            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return intGroupSchListMap;
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append("    WITH GROUP_DATA AS ( ");
        stb.append("      SELECT ");
        stb.append("        ENTEXAMYEAR, ");
        stb.append("        APPLICANTDIV, ");
        stb.append("        TESTDIV, ");
        stb.append("        SUBSTR(EXAMHALLCD, 1, 1) AS GROUP_DIV, ");
        stb.append("        SUBSTR(EXAMHALLCD, 3, 2) AS GROUP_CD, ");
        stb.append("        S_RECEPTNO, ");
        stb.append("        E_RECEPTNO ");
        stb.append("      FROM ");
        stb.append("        ENTEXAM_HALL_YDAT ");
        stb.append("      WHERE ");
        stb.append("        ENTEXAMYEAR 		= '" + _param._examYear + "' ");
        stb.append("        AND APPLICANTDIV 	= '" + _param._applicantDiv + "' ");
        stb.append("        AND TESTDIV 		= '" + _param._testDiv + "' ");
        stb.append("    ) ");
        stb.append("    SELECT ");
        stb.append("      T1.EXAMNO, ");
        stb.append("      T1.SEX, ");
        stb.append("      T2.GROUP_CD AS RECEPT_GROUP_CD, ");
        stb.append("      T3.GROUP_CD AS INTERVIEW_GROUP_CD ");
        stb.append("    FROM ");
        stb.append("      ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("      INNER JOIN GROUP_DATA T2 ");
        stb.append("        ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("        AND T2.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("        AND T2.TESTDIV = T1.TESTDIV ");
        stb.append("        AND T2.GROUP_DIV = '1' ");
        stb.append("        AND T1.EXAMNO BETWEEN T2.S_RECEPTNO AND T2.E_RECEPTNO ");
        stb.append("      INNER JOIN GROUP_DATA T3 ");
        stb.append("        ON T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("        AND T3.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("        AND T3.TESTDIV = T1.TESTDIV ");
        stb.append("        AND T3.GROUP_DIV = '2' ");
        stb.append("        AND T1.EXAMNO BETWEEN T3.S_RECEPTNO AND T3.E_RECEPTNO ");
        stb.append("    WHERE ");
        stb.append("    	VALUE(T1.JUDGEMENT, '') <> '5' "); //未受験以外
        if (!"1".equals(_param._outputDiv)) {
            if("2".equals(_param._outputDiv)) {
                stb.append("    	AND T1.SEX = '1' "); //男性
            } else {
                stb.append("    	AND T1.SEX = '2' "); //女性
            }
        }
        stb.append("    ORDER BY ");
        stb.append("      RECEPT_GROUP_CD, ");
        stb.append("      INTERVIEW_GROUP_CD, ");
        stb.append("      EXAMNO ");

        return stb.toString();
    }


    private class Student {
        final String _examno;
        final String _sex;
        final String _recGrpCd;
        final String _intGrpCd;

        Student(
                final String examno,
                final String sex,
                final String recGrpCd,
                final String intGrpCd
        ) {
            _examno   	=  examno;
            _sex   		=  sex;
            _recGrpCd   =  recGrpCd;
            _intGrpCd   =  intGrpCd;
        }
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) {
        log.fatal("$Revision: 76110 $ $Date: 2020-08-20 16:29:51 +0900 (木, 20 8 2020) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        return new Param(db2, request);
    }

    private static class Param {
        final String _ctrlDate;
        final String _ctrlYear;
        final String _ctrlSemester;

        final String _examYear;
        final String _applicantDiv;
        final String _testDiv;
        final String _outputDiv;
        final String _testDivname;
        final String _time;

        final Map<String, String> _intGrpNameMap;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _ctrlDate = request.getParameter("CTRL_DATE");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _examYear = request.getParameter("ENTEXAMYEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _outputDiv = request.getParameter("OUTPUT_DIV");
            _time = request.getParameter("TIME");

            _testDivname = getTestDivName(db2);
            _intGrpNameMap = getIntGrpNameMap(db2);
        }

        //yyyy-MM-dd → yyyy年MM月dd日
        private String convertDateFormat(final String dateStr) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String rtnDateStr = "";
            try {
                Date date = sdf.parse(dateStr);
                sdf.applyPattern("yyyy年MM月dd日");
                rtnDateStr = sdf.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            return rtnDateStr;
        }

        private String getTestDivName(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String testDivName = "";

            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT TESTDIV_ABBV FROM ENTEXAM_TESTDIV_MST WHERE ENTEXAMYEAR = '" + _examYear + "' AND APPLICANTDIV = '" + _applicantDiv + "' AND TESTDIV = '" + _testDiv + "'  ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    testDivName = rs.getString("TESTDIV_ABBV");
                }
            } catch (SQLException ex) {
                log.debug(" exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            return testDivName;
        }

        private Map getIntGrpNameMap(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            Map rtnMap = new LinkedHashMap();

            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT SUBSTR(EXAMHALLCD, 3, 2) AS INTGROUPCD, EXAMHALL_ABBV AS INTGROUPNAME_ABBV FROM ENTEXAM_HALL_YDAT ");
                stb.append(" WHERE ENTEXAMYEAR = '" + _examYear + "' AND APPLICANTDIV = '" + _applicantDiv + "' AND TESTDIV = '" + _testDiv + "' AND SUBSTR(EXAMHALLCD, 1, 1) = '2'  ");
                stb.append(" ORDER BY INTGROUPCD  ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtnMap.put(rs.getString("INTGROUPCD"), rs.getString("INTGROUPNAME_ABBV"));
                }
            } catch (SQLException ex) {
                log.debug(" exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            return rtnMap;
        }

    }

}// クラスの括り
