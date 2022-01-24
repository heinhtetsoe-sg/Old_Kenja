/*
 * $Id: 158d9cb19614fb616dd4e08b49cfd8b6eb7d5110 $
 *
 * 作成日: 2018/07/31
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
import java.util.Collections;
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
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJL327A {

    private static final Log log = LogFactory.getLog(KNJL327A.class);

    private boolean _hasData;

    private Param _param;

    private final String PRINT_FS = "1";
    private final String PRINT_PS = "2";

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
        String formname = PRINT_PS.equals(_param._formtype) ? "KNJL327A_1.frm" : "KNJL327A_2.frm";
        final List groupList = getList(db2);
        int printCnt = 0;

        for (Iterator itelist = groupList.iterator(); itelist.hasNext();) {
            List printList = (List)itelist.next();
            if (printList == null || printList.size() == 0) {
                continue;
            }
            svf.VrSetForm(formname, 4);
            setTitle(db2, svf, (PrintData)printList.get(0));//ヘッダ
            printCnt = 0;
            for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
                final PrintData printData = (PrintData) iterator.next();
                printCnt++;
                svf.VrsOut("NO", String.valueOf(printCnt));
                svf.VrsOut("ELECT_DIV", printData._shdivname);
                svf.VrsOut("COURSE", printData._coursename);

                svf.VrsOut("EXAM_NO", printData._receptno);
                if (PRINT_FS.equals(_param._formtype)) {
                    int namelen = KNJ_EditEdit.getMS932ByteLength(printData._name);
                    String namefield = namelen > 30 ? "NAME3" : (namelen > 20 ? "NAME2" : "NAME1");
                    svf.VrsOut(namefield, printData._name);
                }
                if ("1".equals(printData._coursecode) && ("〇".equals(printData._sfflg[1]) || "〇".equals(printData._sfflg[2]))) {
                    svf.VrsOut("PASS_COURSE1", "×");
                } else {
                    svf.VrsOut("PASS_COURSE1", printData._sfflg[0]);
                }
                if (("1".equals(printData._coursecode) || "2".equals(printData._coursecode)) && "〇".equals(printData._sfflg[2])) {
                    svf.VrsOut("PASS_COURSE2", "×");
                } else {
                    svf.VrsOut("PASS_COURSE2", printData._sfflg[1]);
                }
                svf.VrsOut("PASS_COURSE3", printData._sfflg[2]);
                if (!"".equals(printData._chguppercourse)) {
                    svf.VrsOut("CHANGE_COURSE", printData._chguppercourse);
                } else {
                    svf.VrsOut("CHANGE_COURSE", "―");
                }
                if (!printData._kessekiFlg) {
                    svf.VrsOut("REMARK", StringUtils.defaultString(printData._tokutai, ""));
                }

                svf.VrEndRecord();

                _hasData = true;
            }
            svf.VrsOut("TOTAL_SUM", "以上" + String.valueOf(printCnt) + "名");
            svf.VrEndRecord();
        }
    }

    private void setTitle(final DB2UDB db2, final Vrw32alp svf, final PrintData pinfo) {
        String setYear = KNJ_EditDate.h_format_JP_N(db2, _param._entExamYear + "/04/01");
        if (PRINT_PS.equals(_param._formtype)) {
            svf.VrsOut("FINSCHOOL_CD", pinfo._prischcd + StringUtils.defaultString(pinfo._prischclscd, ""));   //塾ID+教室ID
            svf.VrsOut("FINSCHOOL_NAME", pinfo._prischname + StringUtils.defaultString(pinfo._prischclsname, "")+"様"); //塾名+教室名+"様"
            svf.VrsOut("TITLE", setYear+"度　"+ _param._schoolKindNameList.get(_param._applicantDiv) + "塾宛通知書");
        } else {
            svf.VrsOut("FINSCHOOL_CD", pinfo._fscd);   //出身中学ID
            final String type =  "3".equals(pinfo._finschooltype) ? "中学校" : "2".equals(pinfo._finschooltype) ? "小学校" : "";
            svf.VrsOut("FINSCHOOL_NAME", pinfo._finschoolname + type + "御中"); //出身学校名 + "小学校" or "中学校" + "御中"
            svf.VrsOut("TITLE", setYear+"度　"+ _param._schoolKindNameList.get(_param._applicantDiv) + "学校宛通知書");
        }
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._loginDate));
        svf.VrsOut("SCHOOL_NAME", (String)_param._ourSchoolInfo.get("SCHOOL_NAME"));
        svf.VrsOut("STAFF_NAME", (String)_param._ourSchoolInfo.get("JOB_NAME") + (String)_param._ourSchoolInfo.get("PRINCIPAL_NAME"));
        if (null != _param._stampFilePath) {
            svf.VrsOut("SCHOOLSTAMP", _param._stampFilePath);
        }
        int ii = 1;
        for (Iterator ite = _param._courseNameList.keySet().iterator();ite.hasNext();) {
            String gwk = (String)ite.next();
            if (ii > 3) {
                break;
            }
            svf.VrsOut("COURSE_NAME" + ii, (String)_param._courseNameList.get(gwk)+"コース");
            ii++;
        }
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        List onegrplist = new ArrayList();
        retList.add(onegrplist);
        String beforegrpcd = "";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {

                final String prischcd = rs.getString("PRISCHCD");
                final String prischname = rs.getString("PRISCHNAME");

                final String fscd = rs.getString("FS_CD");
                final String finschoolname = rs.getString("FINSCHOOL_NAME");
                final String finschooltype = rs.getString("FINSCHOOL_TYPE");

                final String prischclscd = rs.getString("PRISCHCLSCD");
                final String prischclsname = rs.getString("PRISCHCLSNAME");

                final String shdiv = rs.getString("SHDIV");
                final String shdivname = rs.getString("SHDIVNAME");

                final String coursecode = rs.getString("COURSECODE");
                final String coursename = rs.getString("COURSENAME");

                final String coursename1_2 = rs.getString("COURSENAME1_2");
                final String coursename2_2 = rs.getString("COURSENAME2_2");

                final String receptno = rs.getString("RECEPTNO");
                final String name = rs.getString("NAME");
                final String tokutai = rs.getString("KAKUTEI_TOKUTAI_NAME");
                final String chguppercourse = rs.getString("CHG_UPPER_COURSE");


                final String[] sfflg = new String[3];
                int ii = 1;
                boolean kessekiFlg = false;
                for (Iterator iterator = _param._courseNameList.keySet().iterator(); iterator.hasNext();) {
                    if (ii > 3) {
                        break;
                    }
                    final String setGouhi = rs.getString("SFFLG_" + ii);
                    if (!kessekiFlg && "欠席".equals(setGouhi)) {
                        kessekiFlg = true;
                    }
                    sfflg[ii-1] = setGouhi;
                    ii++;
                }
                if (ii <= 3) {
                    for (;ii > 3;ii++) {
                        sfflg[ii-1] = "";
                    }
                }


                final PrintData printData = new PrintData(prischcd, prischname, fscd, finschoolname, finschooltype, prischclscd, prischclsname,
                                                           shdiv, shdivname, coursecode, coursename,coursename1_2,
                                                           coursename2_2, receptno, name, tokutai, chguppercourse, sfflg, kessekiFlg);
                //塾/出身中学で、判定するコードが変化。
                final String kstr;
                if (PRINT_PS.equals(_param._formtype)) {
                    kstr = prischcd + prischclscd;
                } else {
                    kstr = fscd;
                }
                if (!"".equals(beforegrpcd) && !beforegrpcd.equals(kstr)) {
                    onegrplist = new ArrayList();
                    retList.add(onegrplist);
                }
                if (PRINT_PS.equals(_param._formtype)) {
                    beforegrpcd = prischcd + prischclscd;
                } else {
                    beforegrpcd = fscd;
                }
                onegrplist.add(printData);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getSql() {
        final StringBuffer stb = new StringBuffer();
        int lpmax = _param._courseNameList.size() < 3 ? _param._courseNameList.size() : 3;
        //塾コード1～3を分割して、個別にデータを用意。
        //このデータを利用して作成する。
        stb.append("WITH DIVIDE_PRISCHCD AS (");
        stb.append(" SELECT ");
        stb.append("   EADD1.ENTEXAMYEAR,");
        stb.append("   EADD1.APPLICANTDIV, ");
        stb.append("   EADD1.EXAMNO, ");
        stb.append("   EADD1.SEQ, ");
        stb.append("   EADD1.REMARK1 AS PRISCHCD, ");
        stb.append("   PM1.PRISCHOOL_NAME AS PRISCHNAME, ");
        stb.append("   EADD1.REMARK3 AS PRISCHCLSCD, ");
        stb.append("   PSCM1.PRISCHOOL_NAME AS PRISCHCLSNAME ");
        stb.append(" FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT EADD1");
        stb.append("   LEFT JOIN PRISCHOOL_MST PM1 ");
        stb.append("      ON PM1.PRISCHOOLCD = EADD1.REMARK1 ");
        stb.append("   LEFT JOIN PRISCHOOL_CLASS_MST PSCM1 ");
        stb.append("      ON PSCM1.PRISCHOOLCD = EADD1.REMARK1 ");
        stb.append("     AND PSCM1.PRISCHOOL_CLASS_CD = EADD1.REMARK3 ");
        stb.append(" WHERE  ");
        stb.append("   EADD1.ENTEXAMYEAR = '" + _param._entExamYear + "' ");
        stb.append("   AND EADD1.APPLICANTDIV   = '" + _param._applicantDiv + "' ");
        stb.append("   AND EADD1.REMARK1 IS NOT NULL AND EADD1.REMARK1 <> '' ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("   EADD2.ENTEXAMYEAR,");
        stb.append("   EADD2.APPLICANTDIV, ");
        stb.append("   EADD2.EXAMNO, ");
        stb.append("   EADD2.SEQ, ");
        stb.append("   EADD2.REMARK4 AS PRISCHCD, ");
        stb.append("   PM2.PRISCHOOL_NAME AS PRISCHNAME, ");
        stb.append("   EADD2.REMARK5 AS PRISCHCLSCD, ");
        stb.append("   PSCM2.PRISCHOOL_NAME AS PRISCHCLSNAME ");
        stb.append(" FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT EADD2 ");
        stb.append("   LEFT JOIN PRISCHOOL_MST PM2 ");
        stb.append("      ON PM2.PRISCHOOLCD = EADD2.REMARK4 ");
        stb.append("   LEFT JOIN PRISCHOOL_CLASS_MST PSCM2 ");
        stb.append("      ON PSCM2.PRISCHOOLCD = EADD2.REMARK4 ");
        stb.append("     AND PSCM2.PRISCHOOL_CLASS_CD = EADD2.REMARK5");
        stb.append(" WHERE  ");
        stb.append("   EADD2.ENTEXAMYEAR = '" + _param._entExamYear + "' ");
        stb.append("   AND EADD2.APPLICANTDIV   = '" + _param._applicantDiv + "' ");
        stb.append("   AND EADD2.REMARK4 IS NOT NULL AND EADD2.REMARK4 <> '' ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("   EADD3.ENTEXAMYEAR,");
        stb.append("   EADD3.APPLICANTDIV, ");
        stb.append("   EADD3.EXAMNO, ");
        stb.append("   EADD3.SEQ, ");
        stb.append("   EADD3.REMARK6 AS PRISCHCD, ");
        stb.append("   PM3.PRISCHOOL_NAME AS PRISCHNAME, ");
        stb.append("   EADD3.REMARK7 AS PRISCHCLSCD, ");
        stb.append("   PSCM3.PRISCHOOL_NAME AS PRISCHCLSNAME ");
        stb.append(" FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT EADD3 ");
        stb.append("   LEFT JOIN PRISCHOOL_MST PM3 ");
        stb.append("      ON PM3.PRISCHOOLCD = EADD3.REMARK6 ");
        stb.append("   LEFT JOIN PRISCHOOL_CLASS_MST PSCM3 ");
        stb.append("      ON PSCM3.PRISCHOOLCD = EADD3.REMARK6 ");
        stb.append("     AND PSCM3.PRISCHOOL_CLASS_CD = EADD3.REMARK7 ");
        stb.append(" WHERE  ");
        stb.append("   EADD3.ENTEXAMYEAR = '" + _param._entExamYear + "' ");
        stb.append("   AND EADD3.APPLICANTDIV   = '" + _param._applicantDiv + "' ");
        stb.append("   AND EADD3.REMARK6 IS NOT NULL AND EADD3.REMARK6 <> '' ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        //塾
        stb.append("   EADD.PRISCHCD, ");
        stb.append("   EADD.PRISCHNAME, ");
        //出身学校
        stb.append("   BASE.FS_CD, ");
        stb.append("   FINS.FINSCHOOL_NAME, ");
        stb.append("   FINS.FINSCHOOL_TYPE, ");
        //(塾の)教室
        stb.append("   EADD.PRISCHCLSCD, ");
        stb.append("   EADD.PRISCHCLSNAME, ");
        //専願併願区分
        stb.append("   ERDD.REMARK1 AS SHDIV, ");
        stb.append("   L006.NAME1 AS SHDIVNAME, ");
        //コースコード、名
        stb.append("   ERDD.REMARK2 AS COURSECODE, ");
        stb.append("     L058.ABBV2 AS COURSENAME, ");
        //専願コード、名
        stb.append("     L013_8.NAME1 AS COURSENAME1_2, ");
        //併願コード、名
        stb.append("     L013_9.NAME1 AS COURSENAME2_2, ");
        stb.append("     RCPT.RECEPTNO, ");
        stb.append("     BASE.NAME, ");
        //確定特待
        stb.append("     HNR7.HONORDIV_NAME AS KAKUTEI_TOKUTAI_NAME, ");
        //各コース合否
        int ii = 1;
        for (Iterator iterator = _param._courseNameList.keySet().iterator(); iterator.hasNext();) {
            if (ii > lpmax) break;
            String kstr = (String)iterator.next();
            stb.append("     CASE WHEN ERDD.REMARK1 = '1' AND ERDD.REMARK8 = '" + kstr + "' THEN '〇' ");
            stb.append("          WHEN ERDD.REMARK1 = '2' AND ERDD.REMARK9 = '" + kstr + "' THEN '〇' ");
            stb.append("          WHEN ERDD.REMARK1 = '1' AND ERDD.REMARK2 = '" + kstr + "' AND ERDD.REMARK8 = '0' THEN '×' ");
            stb.append("          WHEN ERDD.REMARK1 = '2' AND ERDD.REMARK2 = '" + kstr + "' AND ERDD.REMARK9 = '0' THEN '×' ");
            stb.append("          WHEN ERDD.REMARK2 = '" + kstr + "' AND ERDD.REMARK1 = '1' AND ERDD.REMARK8 = '4' THEN '欠席' ");
            stb.append("          WHEN ERDD.REMARK2 = '" + kstr + "' AND ERDD.REMARK1 = '2' AND ERDD.REMARK9 = '4' THEN '欠席' ");
            stb.append("          ELSE '' END AS SFFLG_" + ii + ", ");
            ii++;
        }
        //専願切替時合格コース名
        stb.append("   CASE WHEN ERDD.REMARK1 = '2' AND L013_8.NAMESPARE1 = '1' THEN L013_8.NAME1 ELSE '' END AS CHG_UPPER_COURSE ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT RCPT ");
        stb.append("     INNER JOIN V_ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("          ON BASE.ENTEXAMYEAR    = RCPT.ENTEXAMYEAR ");
        stb.append("         AND BASE.APPLICANTDIV   = RCPT.APPLICANTDIV ");
        stb.append("         AND BASE.EXAMNO         = RCPT.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT B029 "); //確定特待
        stb.append("          ON B029.ENTEXAMYEAR    = RCPT.ENTEXAMYEAR ");
        stb.append("         AND B029.APPLICANTDIV   = RCPT.APPLICANTDIV ");
        stb.append("         AND B029.EXAMNO         = RCPT.EXAMNO ");
        stb.append("         AND B029.SEQ='029' ");
        stb.append("     LEFT JOIN ENTEXAM_HONORDIV_MST HNR7 "); //確定特待
        stb.append("          ON HNR7.ENTEXAMYEAR    = B029.ENTEXAMYEAR ");
        stb.append("         AND HNR7.APPLICANTDIV   = B029.APPLICANTDIV ");
        stb.append("         AND HNR7.HONORDIV       = B029.REMARK7 ");
        stb.append("         AND HNR7.HONOR_TYPE    <> '3' ");
        stb.append("     LEFT JOIN DIVIDE_PRISCHCD EADD ");  //塾コード、教室コード
        stb.append("          ON EADD.ENTEXAMYEAR  = RCPT.ENTEXAMYEAR");
        stb.append("         AND EADD.APPLICANTDIV = RCPT.APPLICANTDIV ");
        stb.append("         AND EADD.EXAMNO       = RCPT.EXAMNO ");
        stb.append("         AND EADD.SEQ          = '008' ");
        stb.append("     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT ERDD "); //専願/併願コースコード(REMARK1/8/9)
        stb.append("          ON ERDD.ENTEXAMYEAR    = RCPT.ENTEXAMYEAR ");
        stb.append("         AND ERDD.APPLICANTDIV   = RCPT.APPLICANTDIV ");
        stb.append("         AND ERDD.TESTDIV        = RCPT.TESTDIV ");
        stb.append("         AND ERDD.EXAM_TYPE      = RCPT.EXAM_TYPE ");
        stb.append("         AND ERDD.RECEPTNO       = RCPT.RECEPTNO ");
        stb.append("         AND ERDD.SEQ='006' ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FINS ");          //学校名称
        stb.append("         ON FINS.FINSCHOOLCD = BASE.FS_CD ");
        stb.append("     LEFT JOIN ENTEXAM_TESTDIV_MST TDIV ");   //試験名称
        stb.append("          ON TDIV.ENTEXAMYEAR = RCPT.ENTEXAMYEAR ");
        stb.append("         AND TDIV.APPLICANTDIV = RCPT.APPLICANTDIV ");
        stb.append("         AND TDIV.TESTDIV = RCPT.TESTDIV ");
        stb.append("     LEFT JOIN V_NAME_MST Z002 ");            //性別
        stb.append("          ON Z002.YEAR       = BASE.ENTEXAMYEAR ");
        stb.append("         AND Z002.NAMECD1    = 'Z002' ");
        stb.append("         AND Z002.NAMECD2    = BASE.SEX ");
        stb.append("     LEFT JOIN V_NAME_MST L003 ");            //中学/高校
        stb.append("          ON L003.YEAR       = BASE.ENTEXAMYEAR ");
        stb.append("         AND L003.NAMECD1    = 'L003' ");
        stb.append("         AND L003.NAMECD2    = BASE.APPLICANTDIV ");
        stb.append("     LEFT JOIN V_NAME_MST L006 ");
        stb.append("          ON L006.YEAR       = ERDD.ENTEXAMYEAR ");
        stb.append("         AND L006.NAMECD1    = 'L006'  ");
        stb.append("         AND L006.NAMECD2    = ERDD.REMARK1 ");
        stb.append("     LEFT JOIN V_NAME_MST L013_8 ");
        stb.append("          ON L013_8.YEAR     = RCPT.ENTEXAMYEAR ");
        stb.append("         AND L013_8.NAMECD1  = 'L' || VALUE(L003.NAMESPARE3,'H') || '13' ");
        stb.append("         AND L013_8.NAMECD2  = ERDD.REMARK8 ");
        stb.append("     LEFT JOIN V_NAME_MST L013_9 ");
        stb.append("          ON L013_9.YEAR     = RCPT.ENTEXAMYEAR ");
        stb.append("         AND L013_9.NAMECD1  = 'L' || VALUE(L003.NAMESPARE3,'H') || '13' ");
        stb.append("         AND L013_9.NAMECD2  = ERDD.REMARK9 ");
        stb.append("     LEFT JOIN V_NAME_MST L058 ");
        stb.append("          ON L058.YEAR     = RCPT.ENTEXAMYEAR ");
        stb.append("         AND L058.NAMECD1  = 'L' || VALUE(L003.NAMESPARE3,'H') || '58' ");
        stb.append("         AND L058.NAMECD2  = ERDD.REMARK2 ");

        stb.append(" WHERE ");
        stb.append("         RCPT.ENTEXAMYEAR    = '" + _param._entExamYear + "' ");
        stb.append("     AND RCPT.APPLICANTDIV   = '" + _param._applicantDiv + "' ");
        if (!_param._testDiv.equals("ALL")) {
            stb.append("     AND RCPT.TESTDIV        = '" + _param._testDiv + "' ");
        }
        stb.append("     AND RCPT.EXAM_TYPE      = '1' "); //EXAMTYPEは1固定
        if (PRINT_PS.equals(_param._formtype)) {
            stb.append("     AND EADD.PRISCHCD IS NOT NULL ");
        } else {
            stb.append("     AND BASE.FS_CD IS NOT NULL ");
        }
        stb.append(" ORDER BY ");
        if (PRINT_PS.equals(_param._formtype)) {
            stb.append("     EADD.PRISCHCD, EADD.PRISCHCLSCD, ");
        } else {
            stb.append("     BASE.FS_CD, ");
        }
        stb.append("     RCPT.TESTDIV, RCPT.RECEPTNO ");

        return stb.toString();
    }

    private class PrintData {
        final String _prischcd;
        final String _prischname;

        final String _fscd;
        final String _finschoolname;
        final String _finschooltype;

        final String _prischclscd;
        final String _prischclsname;

        final String _shdiv;
        final String _shdivname;

        final String _coursecode;
        final String _coursename;

        final String _coursename1_2;
        final String _coursename2_2;

        final String _receptno;
        final String _name;
        final String _tokutai;
        final String _chguppercourse;

        final String[] _sfflg;
        final boolean _kessekiFlg;

        public PrintData(
                final String prischcd,
                final String prischname,

                final String fscd,
                final String finschoolname,
                final String finschooltype,

                final String prischclscd,
                final String prischclsname,

                final String shdiv,
                final String shdivname,

                final String coursecode,
                final String coursename,

                final String coursename1_2,
                final String coursename2_2,

                final String receptno,
                final String name,
                final String tokutai,
                final String chguppercourse,
                final String[] sfflg,
                final boolean kessekiFlg
        ) {
            _prischcd = prischcd;
            _prischname = prischname;

            _fscd = fscd;
            _finschoolname = finschoolname;
            _finschooltype = finschooltype;

            _prischclscd = prischclscd;
            _prischclsname = prischclsname;

            _shdiv = shdiv;
            _shdivname = shdivname;

            _coursecode = coursecode;
            _coursename = coursename;

            _coursename1_2 = coursename1_2;
            _coursename2_2 = coursename2_2;

            _receptno = receptno;
            _name = name;
            _tokutai = tokutai;
            _chguppercourse = chguppercourse;
            _sfflg = sfflg;
            _kessekiFlg = kessekiFlg;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 71546 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _loginYear;
        private final String _loginSemester;
        private final String _loginDate;
        private final String _entExamYear;
        private final String _applicantDiv;
        private final String _appDivName;
        private final String _testDiv;
        private final String _formtype;
        private final String _appdivstr;
        private final String _documentroot;
        private final String _imagepath;
        private final String _stampFilePath;

        private Map _schoolKindNameList  = Collections.EMPTY_MAP;
        private Map _courseNameList      = Collections.EMPTY_MAP;

        private Map _ourSchoolInfo;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _loginYear      = request.getParameter("LOGIN_YEAR");
            _loginSemester  = request.getParameter("LOGIN_SEMESTER");
            _loginDate      = request.getParameter("LOGIN_DATE");
            _entExamYear    = request.getParameter("ENTEXAMYEAR");
            _applicantDiv   = request.getParameter("APPLICANTDIV");
            _appDivName     = StringUtils.defaultString(getNameMst(db2, "NAME1", "L003", _applicantDiv));
            _testDiv        = request.getParameter("TESTDIV");
            _formtype       = request.getParameter("FORM");
            _appdivstr = _applicantDiv.equals("1") ? "J" : "H";
            _documentroot = request.getParameter("DOCUMENTROOT");
            KNJ_Control imagepath_extension = new KNJ_Control();                //取得クラスのインスタンス作成
            KNJ_Control.ReturnVal returnval = imagepath_extension.Control(db2);
            _imagepath = returnval.val4;                                        //写真データ格納フォルダ
            if ("J".equals(_appdivstr)) {
                _stampFilePath = getImageFilePath("SCHOOLSTAMP_J.bmp");//校長印
            } else {
                _stampFilePath = getImageFilePath("SCHOOLSTAMP_H.bmp");//校長印
            }

            _schoolKindNameList = setSchoolKindNameMap(db2);
            _courseNameList = setCourseNameMap(db2);
            setCertifSchoolDat(db2);
        }
        private Map setSchoolKindNameMap(final DB2UDB db2) {
            return KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, "SELECT NAMECD2, NAME1 FROM NAME_MST WHERE NAMECD1 = 'L003' "), "NAMECD2", "NAME1");
        }
        private Map setCourseNameMap(final DB2UDB db2) {
            final List recordList = KnjDbUtils.query(db2, "SELECT NAMECD2, ABBV2 FROM NAME_MST WHERE NAMECD1 = 'L"+ _appdivstr +"58' ORDER BY NAMECD2 ");
            final String keyColumn = "NAMECD2";
            final String valueColumn = "ABBV2";

            final Map rtn = new LinkedMap();
            for (final Iterator it = recordList.iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                rtn.put(KnjDbUtils.getString(row, keyColumn), KnjDbUtils.getString(row, valueColumn));
            }
            return rtn;

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

        private String getTestdivMst(final DB2UDB db2, final String field, final String testdiv) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT " + field + " FROM ENTEXAM_TESTDIV_MST WHERE ENTEXAMYEAR = '" + _entExamYear + "' AND APPLICANTDIV = '" + _applicantDiv + "' AND TESTDIV = '" + testdiv + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }

        private void setCertifSchoolDat(final DB2UDB db2) {
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK2, REMARK4 FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _entExamYear + "' ");
            if ("J".equals(_appdivstr)) {
                sql.append("   AND CERTIF_KINDCD = '105' ");
            } else {
                sql.append("   AND CERTIF_KINDCD = '106' ");
            }
            log.debug("certif_school_dat sql = " + sql.toString());

            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql.toString()));

            _ourSchoolInfo = new LinkedMap();
            _ourSchoolInfo.put("SCHOOL_NAME", StringUtils.defaultString(KnjDbUtils.getString(row, "SCHOOL_NAME"), ""));
            _ourSchoolInfo.put("JOB_NAME", StringUtils.defaultString(KnjDbUtils.getString(row, "JOB_NAME"), ""));
            _ourSchoolInfo.put("PRINCIPAL_NAME",  StringUtils.defaultString(KnjDbUtils.getString(row, "PRINCIPAL_NAME"), ""));
            _ourSchoolInfo.put("REMARK2", StringUtils.defaultString(KnjDbUtils.getString(row, "REMARK2"), "担任"));
            _ourSchoolInfo.put("REMARK4", KnjDbUtils.getString(row, "REMARK4"));
        }

        /**
         * 写真データファイルの取得
         */
        private String getImageFilePath(final String filename) {
            if (null == _documentroot || null == _imagepath || null == filename) {
                return null;
            } // DOCUMENTROOT
            final StringBuffer path = new StringBuffer();
            path.append(_documentroot).append("/").append(_imagepath).append("/").append(filename);
            final File file = new File(path.toString());
            if (!file.exists()) {
                log.warn("画像ファイル無し:" + path);
                return null;
            } // 写真データ存在チェック用
            return path.toString();
        }
    }
}

// eof
