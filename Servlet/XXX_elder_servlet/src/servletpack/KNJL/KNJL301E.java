/*
 * $Id: 3f8c78b1d2225ba18b12731a16afb751488305dd $
 *
 * 作成日: 2018/11/07
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL301E {

    private static final Log log = LogFactory.getLog(KNJL301E.class);

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJL301E.frm", 4);
        final Map printMap = getMapDat(db2);
        if (printMap.isEmpty()) {
        	return;
        }
        //final int maxPageLine = 42;
        int printLine = 1;

        setPageHeader(db2, svf);//ヘッダ
        
        String setYear = KNJ_EditDate.getAutoFormatYearNen(db2, _param._entExamYear + "/04/01");
        svf.VrsOut("TITLE", setYear + "度　" + _param._testdivName + "　推薦資格");
        svf.VrEndRecord();

        for (Iterator iterator = printMap.keySet().iterator(); iterator.hasNext();) {
            final String kstr = (String)iterator.next();
            List prtList = (List) printMap.get(kstr);
            //表毎にタイトルを出力
            setTitle(db2, svf, prtList);//ヘッダ
            int recordcnt = 1;
            for (Iterator itr = prtList.iterator(); itr.hasNext();) {
                final PrintData printData = (PrintData) itr.next();
                //データ
                //NO
                svf.VrsOut("NO",  String.valueOf(recordcnt));
                //合否判定(出力は空白なので、処理はなし)
                //受験番号
                svf.VrsOut("EXAM_NO" , printData._examNo);
                //氏名
                svf.VrsOut("NAME1" , printData._name);
                //氏名かな
                svf.VrsOut("KANA" , printData._nameKana);
                //出身校
                int fsnamelen = KNJ_EditEdit.getMS932ByteLength(printData._fsNameAbbv);
                if (fsnamelen > 14) {
                    svf.VrsOut("FINSCHOOL_NAME2" , printData._fsNameAbbv);
                } else {
                    svf.VrsOut("FINSCHOOL_NAME" , printData._fsNameAbbv);
                }
                //5教科平均
                if (printData._confAvg5 != null && !"".equals(printData._confAvg5)) {
                    //SQLで小数1位で四捨五入しているので、小数1位より下の余計な文字列を切り取る。
                    int confavg5len = KNJ_EditEdit.getMS932ByteLength(printData._confAvg5);
                    String confAvg5OutputStr = printData._confAvg5;
                    int dotidx = printData._confAvg5.indexOf('.');
                    if (dotidx >= 0 && confavg5len - dotidx + 1 > 1) {
                        confAvg5OutputStr = confAvg5OutputStr.substring(0, dotidx+2);
                    } else {
                        //ドットが無いので、先頭5文字を出力(残りは見切れ扱い)
                        if (confavg5len > 5) {
                            confAvg5OutputStr = confAvg5OutputStr.substring(0, 5);
                        }
                    }
                    svf.VrsOut("AVERAGE" , confAvg5OutputStr);
                }
                if (KNJ_EditEdit.getMS932ByteLength(printData._qualifyDetail) <= 80) {
        			svf.VrsOut("QUALIFY_CONTENT2", printData._qualifyDetail); // 中央
                } else {
                	//資格内容
                	String[] qdstr = KNJ_EditEdit.get_token(printData._qualifyDetail, 80, 3);
                	if (qdstr != null) {
                		for (int ii = 0;ii < qdstr.length;ii++) {
                			svf.VrsOut("QUALIFY_CONTENT" + (ii + 1), qdstr[ii]);
                		}
                	}
                }
                //資格
                if ("1".equals(printData._qualify)) {  //実績
                    svf.VrsOut("QUALIFY1", "○");
                } else if ("2".equals(printData._qualify)) { //推薦
                    svf.VrsOut("QUALIFY2", "○");
                }
                //実技
                svf.VrsOut("PRACTICE", printData._skill);
                //判定
                svf.VrsOut("JUDGE", printData._judge);
                //面接
                svf.VrsOut("INTERVIEW_A", printData._interview1);
                svf.VrsOut("INTERVIEW_B", printData._interview2);

                printLine++;
                recordcnt++;
                svf.VrEndRecord();
                _hasData = true;
            }
            if (iterator.hasNext()) {
                //終了と判断して、次表との間に余白行を挿入(最終行に余白は入れない。)
                svf.VrsOut("BLANK", "dummy");
                svf.VrEndRecord();
            }
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }


    private void setPageHeader(final DB2UDB db2, final Vrw32alp svf) {
        final String printDateTime = KNJ_EditDate.getAutoFormatDate(db2, _param._loginDate);// + "　" + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE);
        svf.VrsOut("DATE", printDateTime);
    }

    private void setTitle(final DB2UDB db2, final Vrw32alp svf, final List prtList) {
        String desireDivTestDiv1Name = "";
        if (null != prtList && prtList.size() > 0) {
            final PrintData printData = (PrintData) prtList.get(0);
            //受験区分 コース
            desireDivTestDiv1Name = StringUtils.defaultString(printData._tDivName) + " " + StringUtils.defaultString(printData._dDivName);
        }
        svf.VrsOut("SUBTITLE", desireDivTestDiv1Name);
        svf.VrEndRecord();
        svf.VrsOut("HEADER", "DUMMY");
        svf.VrEndRecord();
    }

    private Map getMapDat(final DB2UDB db2) {
        final Map retMap = new LinkedMap();
        List addList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String kstr = "";
            final String sql = getSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String testDiv1 = StringUtils.defaultString(rs.getString("TESTDIV1"), "");
                final String desireDiv = StringUtils.defaultString(rs.getString("DESIREDIV"), "");
                final String examNo = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String nameKana = rs.getString("NAME_KANA");
                final String tDivName = rs.getString("TDIVNAME");
                final String dDivName = rs.getString("DDIVNAME");
                final String qualifyDetail = rs.getString("QUALIFYDETAIL");
                final String qualify = rs.getString("QUALIFY");
                final String skill = rs.getString("SKILL");
                final String judge = rs.getString("JUDGE");
                final String confAvg5 = rs.getString("CONF_AVG5");
                final String fsNameAbbv = rs.getString("FINSCHOOL_NAME_ABBV");
                final String interview1 = rs.getString("INTERVIEW1");
                final String interview2 = rs.getString("INTERVIEW2");

                if (!"".equals(kstr) && !kstr.equals(desireDiv + "-" + testDiv1)) {
                    addList = new ArrayList();
                    retMap.put(desireDiv + "-" + testDiv1, addList);
                } else if ("".equals(kstr)) {
                    retMap.put(desireDiv + "-" + testDiv1, addList);
                }
                final PrintData printData = new PrintData(desireDiv, testDiv1, examNo, name, nameKana, tDivName, dDivName, qualifyDetail, qualify, skill, judge, confAvg5, fsNameAbbv, interview1, interview2);
                addList.add(printData);
                kstr = desireDiv + "-" + testDiv1;
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }

    private String getSql() {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("  T1.EXAMNO, ");
        stb.append("  T1.NAME, ");
        stb.append("  T1.NAME_KANA, ");
        stb.append("  T1.TESTDIV, ");
        stb.append("  T1.TESTDIV1, ");
        stb.append("  L045.NAME1 AS TDIVNAME, ");
        stb.append("  T1.DESIREDIV, ");
        stb.append("  L058.NAME1 AS DDIVNAME, ");
        //5教科の平均
        stb.append("  CASE WHEN T3.TOTAL5 IS NULL AND T3_001.REMARK10 IS NULL AND T3_002.REMARK10 IS NULL THEN NULL ");
        stb.append("       ELSE ROUND((VALUE(T3.TOTAL5, 0) + INT(VALUE(T3_001.REMARK10, '0')) + INT(VALUE(T3_002.REMARK10, '0'))) / 15.0 , 1) END AS CONF_AVG5, ");
        stb.append("  FM.FINSCHOOL_NAME_ABBV, ");
        stb.append("  L027_1.NAME1 AS INTERVIEW1, ");
        stb.append("  L027_2.NAME1 AS INTERVIEW2, ");
        stb.append("  T2.REMARK10 AS QUALIFYDETAIL, ");
        stb.append("  T2.REMARK2 AS QUALIFY, ");
        stb.append("  CASE WHEN T2.REMARK3 = '1' THEN '―' WHEN T2.REMARK3 = '2' THEN '有り' WHEN T2.REMARK3 = '3' THEN '無し' ELSE '' END AS SKILL, ");
        stb.append("  CASE WHEN T2.REMARK4 = '1' THEN '―' WHEN T2.REMARK4 = '2' THEN '可' WHEN T2.REMARK4 = '3' THEN '否' ELSE '' END AS JUDGE ");
        stb.append(" FROM ");
        stb.append("  ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("  LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T2 ");
        stb.append("    ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("   AND T2.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("   AND T2.EXAMNO = T1.EXAMNO ");
        stb.append("   AND T2.SEQ = '034' ");
        stb.append("  LEFT JOIN NAME_MST L045 ");
        stb.append("    ON L045.NAMECD1 = 'L045' ");
        stb.append("   AND L045.NAMECD2 = T1.TESTDIV1 ");
        stb.append("  LEFT JOIN NAME_MST L058 ");
        stb.append("    ON L058.NAMECD1 = 'L058' ");
        stb.append("   AND L058.NAMECD2 = T1.DESIREDIV ");
        stb.append("    LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT T3 ");
        stb.append("      ON T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("     AND T3.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("     AND T3.EXAMNO = T1.EXAMNO ");
        stb.append("    LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT T3_001 ");
        stb.append("      ON T3_001.ENTEXAMYEAR = T3.ENTEXAMYEAR ");
        stb.append("     AND T3_001.APPLICANTDIV = T3.APPLICANTDIV ");
        stb.append("     AND T3_001.EXAMNO = T3.EXAMNO ");
        stb.append("     AND T3_001.SEQ = '001' ");
        stb.append("    LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT T3_002 ");
        stb.append("      ON T3_002.ENTEXAMYEAR = T3.ENTEXAMYEAR ");
        stb.append("     AND T3_002.APPLICANTDIV = T3.APPLICANTDIV ");
        stb.append("     AND T3_002.EXAMNO = T3.EXAMNO ");
        stb.append("     AND T3_002.SEQ = '002' ");
        stb.append("    LEFT JOIN FINSCHOOL_MST FM ");
        stb.append("      ON FM.FINSCHOOLCD = T1.FS_CD ");
        stb.append("    LEFT JOIN ENTEXAM_INTERVIEW_DAT M1 ");
        stb.append("      ON M1.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("     AND M1.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("     AND M1.TESTDIV = T1.TESTDIV ");
        stb.append("     AND M1.EXAMNO = T1.EXAMNO ");
        stb.append("    LEFT JOIN NAME_MST L027_1 ");
        stb.append("      ON L027_1.NAMECD1 = 'L027' ");
        stb.append("     AND L027_1.NAMECD2 = M1.INTERVIEW_A ");
        stb.append("    LEFT JOIN NAME_MST L027_2 ");
        stb.append("      ON L027_2.NAMECD1 = 'L027' ");
        stb.append("     AND L027_2.NAMECD2 = M1.INTERVIEW_B ");
        stb.append(" WHERE ");
        stb.append("   T1.ENTEXAMYEAR = '" + _param._entExamYear + "' ");
        stb.append("   AND T1.APPLICANTDIV = '" +  _param._applicantDiv + "' ");
        stb.append("   AND T1.TESTDIV = '" + _param._testDiv + "' ");
        stb.append(" ORDER BY ");
        stb.append("  T1.DESIREDIV, ");
        stb.append("  T1.TESTDIV1, ");
        stb.append("  T1.ENTEXAMYEAR, ");
        stb.append("  T1.TESTDIV, ");
        stb.append("  T1.EXAMNO ");

        return stb.toString();
    }

    private class PrintData {
        final String _desireDiv;
        final String _testDiv1;
        final String _examNo;
        final String _name;
        final String _nameKana;
        final String _tDivName;
        final String _dDivName;
        final String _qualifyDetail;
        final String _qualify;
        final String _skill;
        final String _judge;
        final String _confAvg5;
        final String _fsNameAbbv;
        final String _interview1;
        final String _interview2;

        public PrintData(
                final String desireDiv,
                final String testDiv1,
                final String examNo,
                final String name,
                final String nameKana,
                final String tDivName,
                final String dDivName,
                final String qualifyDetail,
                final String qualify,
                final String skill,
                final String judge,
                final String confAvg5,
                final String fsNameAbbv,
                final String interview1,
                final String interview2
        ) {
        	_desireDiv = desireDiv;
        	_testDiv1 = testDiv1;
        	_examNo = examNo;
        	_name = name;
        	_nameKana = nameKana;
        	_tDivName = tDivName;
        	_dDivName = dDivName;
        	_qualifyDetail = qualifyDetail;
        	_qualify = qualify;
        	_skill = skill;
        	_judge = judge;
            _confAvg5 = confAvg5;
            _fsNameAbbv = fsNameAbbv;
            _interview1 = interview1;
            _interview2 = interview2;
        }

        private boolean chkFileExist() {
            return checkFilePath(_param._documentRoot + "/" + _param._imagePath + "/ENTEXAM/KNJL011E_" + _param._entExamYear + "_"+ _param._applicantDiv + "_"+_examNo +".pdf");
        }

        private boolean checkFilePath(final String path) {
            final boolean exists = new File(path).exists();
            if (!exists) {
                log.info("file not found:" + path);
                return false;
            }
            log.info("exists:" + path);
            return true;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 64507 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _loginYear;
        private final String _loginDate;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _entExamYear;
        private final String _testdivName;
        private final String _documentRoot;
        private String _imagePath;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _loginDate     = request.getParameter("LOGIN_DATE");
            _loginYear     = request.getParameter("LOGIN_YEAR");
            _entExamYear   = request.getParameter("ENTEXAMYEAR");
            _applicantDiv  = request.getParameter("APPLICANTDIV");
            _testDiv       = request.getParameter("TESTDIV");
            _testdivName = StringUtils.defaultString(getNameMst(db2, "NAME1", "L004", _testDiv));
            _documentRoot = request.getParameter("DOCUMENTROOT");
            loadControlMst(db2);
        }

        private String getNameMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + namecd2 + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }
        private void loadControlMst(final DB2UDB db2) {
            final String sql = "SELECT IMAGEPATH, EXTENSION FROM CONTROL_MST WHERE CTRL_NO = '01' ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _imagePath = rs.getString("IMAGEPATH");
                }
            } catch (SQLException e) {
                log.error("Exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }
    }
}

// eof
