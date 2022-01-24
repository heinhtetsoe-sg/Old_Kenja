/*
 * $Id: 030a522bdcdc79ae6b9aaff95a5de88e7df6680d $
 *
 * 作成日: 2017/11/01
 * 作成者: tawada
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJL322D {

    private static final Log log = LogFactory.getLog(KNJL322D.class);

    private boolean _hasData;

    private Param _param;

    private String _sub_outputdiv;
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
        final List targetlist = getAllTargetSchool(db2);
        for (Iterator iterator = targetlist.iterator(); iterator.hasNext();) {
            _sub_outputdiv = (String) iterator.next();
            if ("1".equals(_param._noticeType)){
                printMain2(db2, svf);
            } else if ("2".equals(_param._noticeType)) {
                printMain3(db2, svf);
            } else {
                return;
            }
        }
    }

    private List getAllTargetSchool(final DB2UDB db2) {
        List retList = new ArrayList();

        if (!"999999999999".equals(_param._outputdiv)) {
            retList.add(_param._outputdiv);
        } else {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getAllTargetSchoolSql();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String schoolcd = rs.getString("VALUE");
                    retList.add(schoolcd);
                }
            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        return retList;
    }

    private void setTitle(final DB2UDB db2, final Vrw32alp svf, final List schoolinfo, final List targetschoolinfo, final int linecnt1, final int linecnt2) {
        final String prtdate = KNJ_EditDate.h_format_JP_N(db2, _param._noticeDate);
        final String cutnoticedate[] = StringUtils.split(_param._noticeDate, "/");
        final String examyear = KNJ_EditDate.h_format_JP_N(db2, _param._entExamYear + "/4/1");

        for (Iterator iterator = targetschoolinfo.iterator(); iterator.hasNext();) {
        	//帳票本文のタイトル部に受験種別を表示(param内部で固定文字列を指定)
        	svf.VrsOut("TESTDIVNAME", _param._testDivName);
            final TargetSchoolInfo printData = (TargetSchoolInfo) iterator.next();
            //(左)和暦日付
            svf.VrsOut("DATE", prtdate + Integer.parseInt(cutnoticedate[1]) + "月" + Integer.parseInt(cutnoticedate[2]) + "日");
            //(左)郵便番号
            svf.VrsOut("ZIPNO", printData._zipNo);
            //(左)住所1
            if (32 >= KNJ_EditEdit.getMS932ByteLength(printData._addr1)) {
                svf.VrsOut("ADDR1_1", printData._addr1);
            } else if (50 >= KNJ_EditEdit.getMS932ByteLength(printData._addr1)) {
                svf.VrsOut("ADDR1_2", printData._addr1);
            } else {
                svf.VrsOut("ADDR1_3", printData._addr1);
            }
            //(左)住所2
            if (null != printData._addr2) {
                if (32 >= KNJ_EditEdit.getMS932ByteLength(printData._addr2)) {
                    svf.VrsOut("ADDR2_1", printData._addr2);
                } else if (50 >= KNJ_EditEdit.getMS932ByteLength(printData._addr2)) {
                    svf.VrsOut("ADDR2_2", printData._addr2);
                } else {
                    svf.VrsOut("ADDR2_3", printData._addr2);
                }
            }
            //(左)対象学校+'長　様'
            final String fsnamestr = printData._name + "長 様";
            if (22 >= KNJ_EditEdit.getMS932ByteLength(fsnamestr)) {
                svf.VrsOut("FINSCHOOL_NAME1", fsnamestr);
            } else if (34 >= KNJ_EditEdit.getMS932ByteLength(fsnamestr)) {
                svf.VrsOut("FINSCHOOL_NAME2", fsnamestr);
            } else if (44 >= KNJ_EditEdit.getMS932ByteLength(fsnamestr)) {
                svf.VrsOut("FINSCHOOL_NAME3", fsnamestr);
            }
            //(左)対象学校コード
            svf.VrsOut("FINSCHOOL_CD", printData._fsCd);

            SchoolInfo printData2 = null;
            for (Iterator iterator2 = schoolinfo.iterator(); iterator2.hasNext();) {
                printData2 = (SchoolInfo) iterator2.next();
                //(左)自学校名
                svf.VrsOut("SCHOOL_NAME", printData2._schoolName);
                //(左)役職
                svf.VrsOut("JOB_NAME", printData2._jobName);
                //(左)自学校長名前
                svf.VrsOut("STAFF_NAME", printData2._principalName);
            }

            //(右)和暦年度
            svf.VrsOut("NENDO1", examyear + "度");
            //(右)西暦年度
            svf.VrsOut("NENDO2", _param._entExamYear + "年度");
            //(右)自学校名
            if (null != printData2) {
                svf.VrsOut("SCHOOL_NAME2", printData2._schoolName);
            }
            //(右)対象学校名(略称)
            svf.VrsOut("FINSCHOOL_NAME_ABBV2", printData._name_abbv);
            //(右)合計1
            svf.VrsOut("PASS_NUM", String.valueOf(linecnt1));
            //(右)合計2
            svf.VrsOut("NOTPASS_NUM", String.valueOf(linecnt2));
            final String transferdate[] = StringUtils.split(_param._decisiondate, "/");
            svf.VrsOut("DECISION_DATE", Integer.parseInt(transferdate[1]) + "月" + Integer.parseInt(transferdate[2]) + "日");
        }
    }

    private void printMain2(final DB2UDB db2, final Vrw32alp svf) {

        final ArrayList printList3 = getList1(db2, 3);
        final ArrayList printList4 = getList1(db2, 4);
        if (printList4.size() < 1) {
        	log.warn("minyuryoku student existed in fs_cd:"+_sub_outputdiv);
            return;
        }

        final ArrayList printList1 = getList1(db2, 1);
        final ArrayList printList2 = getList1(db2, 2);
        final List schoolinfo = getSchoolInfo(db2);
        final List targetschoolinfo = getTargetSchoolInfo(db2);
        final int maxLine = 50;
        final int maxUndecPageLine = 24;
        final int halfmaxUndecLine = maxUndecPageLine / 2;
        final int lineCnt1 = printList1.size();
        final int lineCnt2 = printList2.size();
        final BigDecimal undecpagecnt = new BigDecimal(printList3.size()).divide(new BigDecimal(maxUndecPageLine), 0, BigDecimal.ROUND_UP);
        final BigDecimal maxpagenum1 = new BigDecimal(lineCnt1).divide(new BigDecimal(maxLine), 0, BigDecimal.ROUND_HALF_UP);
        final BigDecimal maxpagenum2 = new BigDecimal(lineCnt2).divide(new BigDecimal(maxLine), 0, BigDecimal.ROUND_HALF_UP);
        //(int)Math.ceil(printList3.size() / maxUndecPageLine);
        final BigDecimal pagecnt = lineCnt1 < lineCnt2 ? maxpagenum2 :maxpagenum1;
        int listmax = lineCnt1 < lineCnt2 ? lineCnt2 : lineCnt1;
        if (pagecnt.compareTo(undecpagecnt) < 0) {
            final int calcwk = (undecpagecnt.intValue() - 1) * (maxLine - maxUndecPageLine) + printList3.size();
            listmax = calcwk < listmax ? listmax : calcwk;
        }
        int printline1 = 1;
        int printline2 = 1;
        int undecLine = 1;
        int undecCol = 0;
        int undecDataCnt = 0;

        if (printList3.size() >= 1) {
            svf.VrSetForm("KNJL322D_2.frm", 1);
        } else {
            svf.VrSetForm("KNJL322D_1.frm", 1);
        }
        if (listmax > 0) {
            setTitle(db2, svf, schoolinfo, targetschoolinfo, lineCnt1, lineCnt2);
            for (int idx = 0; idx < listmax; idx++) {
                if (printline1 > maxLine || printline2 > maxLine || (idx >= printList1.size() && idx >= printList2.size() && undecDataCnt < printList3.size() && ((undecCol+1) * undecLine) > maxUndecPageLine)) {
                    svf.VrEndPage();
                    setTitle(db2, svf, schoolinfo, targetschoolinfo, lineCnt1, lineCnt2);//ヘッダ
                    printline1 = 1;
                    printline2 = 1;
                    undecLine = 1;
                    undecCol = 0;
                }
                if (undecLine > halfmaxUndecLine) {
                    if (undecCol == 0) {
                        undecCol++;
                        undecLine = 1;
                    }
                }

                if (undecDataCnt < printList3.size() && undecLine * (undecCol+1) <= maxUndecPageLine) {
                    final PrintData1 printData = (PrintData1)printList3.get(undecDataCnt);
                    if (undecCol == 1) {
                        svf.VrsOutn("UNCERTAIN_EXAM_NO2", undecLine, printData._examNo);
                        if (18 >= KNJ_EditEdit.getMS932ByteLength(printData._name)) {
                            svf.VrsOutn("UNCERTAIN_NAME2_1", undecLine, printData._name);
                        } else if (30 >= KNJ_EditEdit.getMS932ByteLength(printData._name)) {
                            svf.VrsOutn("UNCERTAIN_NAME2_2", undecLine, printData._name);
                        } else if (40 >= KNJ_EditEdit.getMS932ByteLength(printData._name)) {
                            svf.VrsOutn("UNCERTAIN_NAME2_3", undecLine, printData._name);
                        }
                    } else {
                        svf.VrsOutn("UNCERTAIN_EXAM_NO1", undecLine, printData._examNo);
                        if (18 >= KNJ_EditEdit.getMS932ByteLength(printData._name)) {
                            svf.VrsOutn("UNCERTAIN_NAME1_1", undecLine, printData._name);
                        } else if (30 >= KNJ_EditEdit.getMS932ByteLength(printData._name)) {
                            svf.VrsOutn("UNCERTAIN_NAME1_2", undecLine, printData._name);
                        } else if (40 >= KNJ_EditEdit.getMS932ByteLength(printData._name)) {
                            svf.VrsOutn("UNCERTAIN_NAME1_3", undecLine, printData._name);
                        }
                    }
                    undecLine++;
                    undecDataCnt++;
                }

                if (idx < printList1.size()) {
                    //(右)合格者(左表:合格)
                    PrintData1 printData = (PrintData1)printList1.get(idx);
                    svf.VrsOutn("EXAM_NO1", printline1, printData._examNo);
                    if (18 >= KNJ_EditEdit.getMS932ByteLength(printData._name)) {
                        svf.VrsOutn("NAME1_1", printline1, printData._name);
                    } else if (50 >= KNJ_EditEdit.getMS932ByteLength(printData._name)) {
                        svf.VrsOutn("NAME1_2", printline1, printData._name);
                    } else {
                        svf.VrsOutn("NAME1_3", printline1, printData._name);
                    }
                    svf.VrsOutn("COURSE_NAME", printline1, printData._jdgname);
                    printline1++;
                }
                if (idx < printList2.size()) {
                    //(右)合格者(右表:不合格)
                    PrintData1 printData = (PrintData1)printList2.get(idx);
                    svf.VrsOutn("EXAM_NO2", printline2, printData._examNo);
                    if (18 >= KNJ_EditEdit.getMS932ByteLength(printData._name)) {
                        svf.VrsOutn("NAME2_1", printline2, printData._name);
                    } else if (50 >= KNJ_EditEdit.getMS932ByteLength(printData._name)) {
                        svf.VrsOutn("NAME2_2", printline2, printData._name);
                    } else {
                        svf.VrsOutn("NAME2_3", printline2, printData._name);
                    }
                    svf.VrsOutn("REMARK", printline2, printData._jdgname);
                    printline2++;
                }

                _hasData = true;
            }
            if (_hasData) {
                svf.VrEndPage();
            }
        }
    }

    private void printMain3(final DB2UDB db2, final Vrw32alp svf) {

        final List targetschoolinfo = getTargetSchoolInfo(db2);
        final List printList = getList1(db2, 3);
        final List schoolinfo = getSchoolInfo(db2);
        final int maxLine = 50;
        int lineCnt = 1;

        if (printList.size() > 0) {
            svf.VrSetForm("KNJL322D_3.frm", 1);
            setTitle2(db2, svf, targetschoolinfo, schoolinfo);
            for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
                final PrintData1 printData = (PrintData1) iterator.next();
                if (lineCnt > maxLine) {
                    svf.VrEndPage();
                    setTitle2(db2, svf, targetschoolinfo, schoolinfo);
                    lineCnt = 1;
                }
                svf.VrsOutn("EXAM_NO1", lineCnt, printData._examNo);
                if (18 >= KNJ_EditEdit.getMS932ByteLength(printData._name)) {
                    svf.VrsOutn("NAME1_1", lineCnt, printData._name);
                } else if (50 >= KNJ_EditEdit.getMS932ByteLength(printData._name)) {
                    svf.VrsOutn("NAME1_2", lineCnt, printData._name);
                } else {
                    svf.VrsOutn("NAME1_3", lineCnt, printData._name);
                }
                svf.VrsOutn("COURSE_NAME", lineCnt, printData._jdgname);
                lineCnt++;
                _hasData = true;
            }
            if (_hasData) {
                svf.VrEndPage();
            }
        }
    }

    private void setTitle2(final DB2UDB db2, final Vrw32alp svf, final List targetschoolinfo, final List schoolinfo) {
        final String prtdate = KNJ_EditDate.h_format_JP_N(db2, _param._noticeDate);
        final String cutnoticedate[] = StringUtils.split(_param._noticeDate, "/");
        final String examyear = KNJ_EditDate.h_format_JP_N(db2, _param._entExamYear + "/4/1");

        for (Iterator iterator = targetschoolinfo.iterator(); iterator.hasNext();) {
            final TargetSchoolInfo printData = (TargetSchoolInfo) iterator.next();

            //和暦日付
            svf.VrsOut("DATE", prtdate + Integer.parseInt(cutnoticedate[1]) + "月" + Integer.parseInt(cutnoticedate[2]) + "日");
            //郵便番号
            svf.VrsOut("ZIPNO", printData._zipNo);
            //(左)住所1
            if (32 >= KNJ_EditEdit.getMS932ByteLength(printData._addr1)) {
                svf.VrsOut("ADDR1_1", printData._addr1);
            } else if (50 >= KNJ_EditEdit.getMS932ByteLength(printData._addr1)) {
                svf.VrsOut("ADDR1_2", printData._addr1);
            } else {
                svf.VrsOut("ADDR1_3", printData._addr1);
            }
            //(左)住所2
            if (32 >= KNJ_EditEdit.getMS932ByteLength(printData._addr2)) {
                svf.VrsOut("ADDR2_1", printData._addr2);
            } else if (50 >= KNJ_EditEdit.getMS932ByteLength(printData._addr2)) {
                svf.VrsOut("ADDR2_2", printData._addr2);
            } else {
                svf.VrsOut("ADDR2_3", printData._addr2);
            }
            //(左)対象学校+'長　様'
            final String fsnamestr = printData._name + "長 様";
            if (22 >= KNJ_EditEdit.getMS932ByteLength(fsnamestr)) {
                svf.VrsOut("NAME1", fsnamestr);
            } else if (34 >= KNJ_EditEdit.getMS932ByteLength(fsnamestr)) {
                svf.VrsOut("NAME2", fsnamestr);
            } else if (44 >= KNJ_EditEdit.getMS932ByteLength(fsnamestr)) {
                svf.VrsOut("NAME3", fsnamestr);
            }

            for (Iterator iterator2 = schoolinfo.iterator(); iterator2.hasNext();) {
                final SchoolInfo printData2 = (SchoolInfo) iterator2.next();
                svf.VrsOut("SCHOOL_NAME", printData2._schoolName);
                svf.VrsOut("JOB_NAME", printData2._jobName);
                svf.VrsOut("STAFF_NAME", printData2._principalName);
            }
            //年度
            svf.VrsOut("NENDO", examyear + "度");
        }

    }

    private List getSchoolInfo(final DB2UDB db2) {
        List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = getSchoolInfoSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schoolname = rs.getString("SCHOOL_NAME");
                final String jobname = rs.getString("JOB_NAME");
                final String principalname = rs.getString("PRINCIPAL_NAME");

                SchoolInfo addInfo = new SchoolInfo(schoolname, jobname, principalname);
                retList.add(addInfo);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private ArrayList getList1(final DB2UDB db2, final int datatype) {
        final ArrayList retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = getSql1(datatype);
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String examNo = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String nameKana = rs.getString("JUDGEMENT");
                final String judgement = rs.getString("JUDGENAME");
                final String remark = rs.getString("REMARK");

                final PrintData1 printData = new PrintData1(examNo, name, nameKana, judgement, remark);
                retList.add(printData);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private List getTargetSchoolInfo(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = getTargetSchoolInfoSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String fscd = rs.getString("FSCD");
                final String zipcd = rs.getString("ZIPCD");
                final String addr1 = rs.getString("ADDR1");
                final String addr2 = rs.getString("ADDR2");
                final String name = rs.getString("NAME");
                final String name_abbv = rs.getString("NAME_ABBV");

                final TargetSchoolInfo printData = new TargetSchoolInfo(fscd, zipcd, addr1, addr2, name, name_abbv);
                retList.add(printData);
                }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getSql1(final int datatype) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     T1.JUDGEMENT, ");
        stb.append("     JDG_NAME.NAME1 AS JUDGENAME, ");
        stb.append("     '' AS REMARK ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
        if ("2".equals(_param._noticeType)) {
            stb.append("     LEFT JOIN NAME_MST JDG_NAME ON JDG_NAME.NAMECD1 = 'L012' ");
            stb.append("       AND JDG_NAME.NAMECD2 = T1.ENTDIV ");
        } else {
            stb.append("     LEFT JOIN NAME_MST JDG_NAME ON JDG_NAME.NAMECD1 = 'L013' ");
            stb.append("       AND JDG_NAME.NAMECD2 = T1.JUDGEMENT ");
        }
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._entExamYear + "'");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "'");
        stb.append("     AND T1.TESTDIV = '" + _param._testDiv + "'");
        stb.append("     AND T1.FS_CD = '" + _sub_outputdiv + "'");
        if ("1".equals(_param._noticeType)) {
            if (datatype == 1) {
                stb.append("     AND T1.JUDGEMENT IN ('1', '2', '3') ");
            } else if (datatype == 2) {
                stb.append("     AND NOT(T1.JUDGEMENT IN ('1', '2', '3')) ");
            } else if (datatype == 3) {
                stb.append("     AND T1.JUDGEMENT IN ('3') ");
            } else if (datatype == 4) {
                stb.append("     AND NOT(T1.JUDGEMENT IS NULL OR T1.JUDGEMENT = '') ");
            }
        } else if ("2".equals(_param._noticeType)) {
            stb.append("     AND T1.JUDGEMENT = '3' ");
            stb.append("     AND T1.ENTDIV IN ('1', '2') ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.EXAMNO ASC");

        return stb.toString();
    }

    private String getTargetSchoolInfoSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     FINSCHOOLCD AS FSCD, ");
        stb.append("     FINSCHOOL_ZIPCD AS ZIPCD, ");
        stb.append("     FINSCHOOL_ADDR1 AS ADDR1, ");
        stb.append("     FINSCHOOL_ADDR2 AS ADDR2, ");
        stb.append("     FINSCHOOL_NAME  AS NAME, ");
        stb.append("     FINSCHOOL_NAME_ABBV  AS NAME_ABBV");
        stb.append(" FROM ");
        stb.append("     FINSCHOOL_MST ");
        stb.append(" WHERE ");
        stb.append("     FINSCHOOLCD = '" + _sub_outputdiv + "' ");

        return stb.toString();
    }

    private String getSchoolInfoSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     SCHOOL_NAME, ");
        stb.append("     JOB_NAME, ");
        stb.append("     PRINCIPAL_NAME ");
        stb.append(" FROM ");
        stb.append("     DB2INST1.CERTIF_SCHOOL_DAT ");
        stb.append(" WHERE ");
        stb.append("     CERTIF_KINDCD = '106' ");
        stb.append("     AND YEAR = '" + _param._entExamYear + "' ");

        return stb.toString();
    }

    private String getAllTargetSchoolSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     BASE.FS_CD AS VALUE ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FINS ON BASE.FS_CD = FINS.FINSCHOOLCD ");
        stb.append(" WHERE ");
        stb.append("     BASE.ENTEXAMYEAR  = '" + _param._entExamYear + "' ");
        stb.append("     AND BASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append(" GROUP BY ");
        stb.append("     BASE.FS_CD, ");
        stb.append("     FINS.FINSCHOOL_NAME ");
        stb.append(" ORDER BY ");
        stb.append("     VALUE ");

         return stb.toString();
    }

    private class PrintData1 {
        final String _examNo;
        final String _name;
        final String _judgement;
        final String _jdgname;
        final String _remark;
        public PrintData1(
                final String examNo,
                final String name,
                final String judgement,
                final String jdgname,
                final String remark
        ) {
            _examNo = examNo;
            _name = name;
            _judgement = judgement;
            _jdgname = jdgname;
            _remark = remark;
        }
    }

    private class TargetSchoolInfo {
        final String _fsCd;
        final String _zipNo;
        final String _addr1;
        final String _addr2;
        final String _name;
        final String _name_abbv;

        public TargetSchoolInfo(
                final String fscd,
                final String zipno,
                final String addr1,
                final String addr2,
                final String name,
                final String name_abbv
        ) {
            _fsCd = fscd;
            _zipNo = zipno;
            _addr1 = addr1;
            _addr2 = addr2;
            _name = name;
            _name_abbv = name_abbv;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 72246 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    private class SchoolInfo {
        private final String _schoolName;
        private final String _jobName;
        private final String _principalName;

        SchoolInfo(final String schoolname, final String jobname, final String principalname) {
            _schoolName = schoolname;
            _jobName = jobname;
            _principalName = principalname;
        }
    }

    /** パラメータクラス */
    private class Param {
        private final String _loginYear;
        private final String _loginSemester;
        private final String _loginDate;
        private final String _entExamYear;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _testDivName;
        private final String _noticeType;
        private final String _noticeDate;
        private final String _outputdiv;
        private final String _decisiondate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _loginYear     = request.getParameter("LOGIN_YEAR");
            _loginSemester = request.getParameter("LOGIN_SEMESTER");
            _loginDate     = request.getParameter("LOGIN_DATE");

            _entExamYear   = request.getParameter("ENTEXAMYEAR");
            _applicantDiv  = request.getParameter("APPLICANTDIV");
            _testDiv       = request.getParameter("TESTDIV");

            _outputdiv     = request.getParameter("OUTPUT_DIV");
            _noticeType    = request.getParameter("NOTICE_TYPE");
            _noticeDate    = request.getParameter("NOTICE_DATE");
            _decisiondate  = request.getParameter("DECISION_DATE");
            _testDivName   = getTestDivName(db2);
        }

        private String getTestDivName(final DB2UDB db2) {
        	// final String sql = " SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'L004' AND NAMECD2 = '" + _testDiv + "' ";
        	// return KnjDbUtils.getOne(KnjDbUtils.query(db2, sql));
        	return "1".equals(_testDiv) ? "推薦" : "一般";
        }
    }
}

// eof
