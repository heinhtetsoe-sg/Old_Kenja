/*
 * $Id: 8864f1c1953194cd6b0b5cfc51fc91f3d597904a $
 *
 * 作成日: 2017/11/01
 * 作成者: tawada
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
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

public class KNJL321D {

    private static final Log log = LogFactory.getLog(KNJL321D.class);

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
        if ("1".equals(_param._noticeType)){
            printMain1(db2, svf);
        } else if ("2".equals(_param._noticeType)) {
            printMain2(db2, svf);
        } else if ("3".equals(_param._noticeType)) {
            printMain3(db2, svf);
        } else if ("4".equals(_param._noticeType)) {
            printMain4(db2, svf);
        } else {
            return;
        }
    }

    private void printMain1(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJL321D_1.frm", 1);

        final List printList = getList1(db2);
        final String setYear = KNJ_EditDate.h_format_JP_N(db2, _param._entExamYear + "/04/01");
        final String testdivstr = getNameMst(db2, "L004", _param._testDiv, "ABBV1");
        final Calendar cal = Calendar.getInstance();
        final String prtnoticeyear = KNJ_EditDate.h_format_JP_N(db2, _param._noticeDate);
        final String cutnoticedate[] = StringUtils.split(_param._noticeDate, "/");
        final String prttrans1year = KNJ_EditDate.h_format_JP_N(db2, _param._transferDate1);
        final String prttrans1week = KNJ_EditDate.h_format_W(_param._transferDate1);
        final String cuttrans1date[] = StringUtils.split(_param._transferDate1, "/");
        final String prttrans1Str = prttrans1year + Integer.parseInt(cuttrans1date[1]) + "月" + Integer.parseInt(cuttrans1date[2]) + "日(" + prttrans1week + ")";
        final String prttrans2year = KNJ_EditDate.h_format_JP_N(db2, _param._transferDate2);
        final String cuttrans2date[] = StringUtils.split(_param._transferDate2, "/");
        final String prttrans2week = KNJ_EditDate.h_format_W(_param._transferDate2);
        final String prttrans2Str = prttrans2year + Integer.parseInt(cuttrans2date[1]) + "月" + Integer.parseInt(cuttrans2date[2]) + "日(" + prttrans2week + ")";
        String money1 = "";
        String money2 = "";
        String ttlstr1 = "";
        String ttlstr2 = "";
        String typestr = "";

        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final PrintData1 printData = (PrintData1) iterator.next();

            money1 = "";
            money2 = "";
            ttlstr1 = "";
            ttlstr2 = "";
            if ("1".equals(_param._testDiv)) {
                if ("1".equals(printData._judgekind) || "4".equals(printData._judgekind)) {  //クラブ1種
                    money1 = "*******";
                    money2 = "*******";
                    ttlstr1 = "＊＊＊＊＊";
                    ttlstr2 = "＊＊＊＊";
                } else if ("2".equals(printData._judgekind)) {  //クラブ2種
                    money1 = "\\110000";
                    money2 = "*******";
                    ttlstr1 += "入学手続金・入学金";
                    ttlstr2 = "＊＊＊＊";
                } else {
                    money1 = "\\220000";
                    money2 = "*******";
                    ttlstr1 += "入学手続金・入学金";
                    ttlstr2 = "＊＊＊＊";
                }
            } else {
            	if ("4".equals(printData._judgekind)) {
                    money1 = "\\50000";
                    money2 = "*******";
                    ttlstr1 += "入学手続金";
                    ttlstr2 = "＊＊＊＊";
            	} else {
            		money1 = "\\50000";
            		money2 = "\\170000";
            		ttlstr1 += "入学手続金";
            		ttlstr2 = "入学金";
            	}
            }
            typestr = "普通科";
            if (null != printData._judgementPattern) {
            	typestr += " (" + printData._judgementPattern + ")";
            }
            //(右)年度
            svf.VrsOut("TITLE", setYear + "度 " + testdivstr + "入学試験");
            //(右)受験番号
            svf.VrsOut("EXAM_NO", printData._examNo);
            //(右)名前
            if (24 >= KNJ_EditEdit.getMS932ByteLength(printData._name)) {
                svf.VrsOut("NAME2_1", printData._name);
            } else if (30 >= KNJ_EditEdit.getMS932ByteLength(printData._name)) {
                svf.VrsOut("NAME2_2", printData._name);
            } else {
                svf.VrsOut("NAME2_3", printData._name);
            }
            //(右)固定文字列＋類型
            svf.VrsOut("COURSE_NAME", typestr);
            //(右)和暦日付(画面指定)
            svf.VrsOut("DATE", prtnoticeyear + Integer.parseInt(cutnoticedate[1]) + "月" + Integer.parseInt(cutnoticedate[2]) + "日");

            //(左上)郵便番号
            svf.VrsOut("ZIPNO", printData._gzipcd);
            //(左上)住所1
            if (40 >= KNJ_EditEdit.getMS932ByteLength(printData._gaddress1)) {
                svf.VrsOut("ADDR1_1", printData._gaddress1);
            } else if (50 >= KNJ_EditEdit.getMS932ByteLength(printData._gaddress1)) {
                svf.VrsOut("ADDR1_2", printData._gaddress1);
            } else {
                svf.VrsOut("ADDR1_3", printData._gaddress1);
            }
            //(左上)住所2
            if (40 >= KNJ_EditEdit.getMS932ByteLength(printData._gaddress2)) {
                svf.VrsOut("ADDR2_1", printData._gaddress2);
            } else if (50 >= KNJ_EditEdit.getMS932ByteLength(printData._gaddress2)) {
                svf.VrsOut("ADDR2_2", printData._gaddress2);
            } else {
                svf.VrsOut("ADDR2_3", printData._gaddress2);
            }
            //(左上)氏名
            final String name = StringUtils.isEmpty(printData._name) ? "" : printData._name + " 様";
            if (34 >= KNJ_EditEdit.getMS932ByteLength(name)) {
                svf.VrsOut("NAME1_1", name);
            } else {
                svf.VrsOut("NAME1_2", name);
            }

            //(左中)タイトル1
            svf.VrsOutn("TRANS_NAME1", 1, ttlstr1);
            //(左中)金額1
            svf.VrsOutn("TRANS_MONEY1", 1, money1);
            //(左中)タイトル2
            svf.VrsOutn("TRANS_NAME2", 1, ttlstr1);
            //(左中)金額2
            svf.VrsOutn("TRANS_MONEY2", 1, money1);
            //(左中)和暦年月日(曜日)
            svf.VrsOutn("LIMIT_DATE", 1, chkMaskStr1(prttrans1Str, printData._judgement, printData._judgekind, 10, "*"));
            //(左中)受験番号1
            svf.VrsOutn("TRANS_EXAM_NO1", 1, chkMaskStr1(printData._examNo, printData._judgement, printData._judgekind, 4, "*"));
            //(左中)固定文字列＋類型
            svf.VrsOutn("TRANS_COURSE_NAME1", 1, chkMaskStr1(typestr, printData._judgement, printData._judgekind, 11, "*"));
            //(左中)氏名1
            if (24 >= KNJ_EditEdit.getMS932ByteLength(printData._name)) {
                svf.VrsOutn("TRANS_NAME1_1", 1, chkMaskStr1(printData._name, printData._judgement, printData._judgekind, 24, "*"));
            } else if (30 >= KNJ_EditEdit.getMS932ByteLength(printData._name)) {
                svf.VrsOutn("TRANS_NAME1_2", 1, chkMaskStr1(printData._name, printData._judgement, printData._judgekind, 30, "*"));
            } else {
                svf.VrsOutn("TRANS_NAME1_3", 1, chkMaskStr1(printData._name, printData._judgement, printData._judgekind, 40, "*"));
            }
            //(左中)属性-受験番号2
            final String attrval = judgeAttribute(printData._judgement, printData._judgekind);
            svf.VrsOutn("TRANS_EXAM_NO2", 1, chkMaskStr1(attrval + printData._examNo, printData._judgement, printData._judgekind, 6, "*"));
            //(左中)氏名かな
            if (22 >= KNJ_EditEdit.getMS932ByteLength(printData._nameKana)) {
                svf.VrsOutn("TRANS_KANA1", 1, chkMaskStr1(printData._nameKana, printData._judgement, printData._judgekind, 22, "*"));
            } else if (40 >= KNJ_EditEdit.getMS932ByteLength(printData._nameKana)) {
                svf.VrsOutn("TRANS_KANA2", 1, chkMaskStr1(printData._nameKana, printData._judgement, printData._judgekind, 40, "*"));
            } else if (60 >= KNJ_EditEdit.getMS932ByteLength(printData._nameKana)) {
                svf.VrsOutn("TRANS_KANA3", 1, chkMaskStr1(printData._nameKana, printData._judgement, printData._judgekind, 60, "*"));
            } else {
                svf.VrsOutn("TRANS_KANA4", 1, chkMaskStr1(printData._nameKana, printData._judgement, printData._judgekind, 80, "*"));
            }
            //(左中)氏名2
            if (34 >= KNJ_EditEdit.getMS932ByteLength(printData._nameKana)) {
                svf.VrsOutn("TRANS_NAME2_1", 1, chkMaskStr1(printData._name, printData._judgement, printData._judgekind, 34, "*"));
            } else {
                svf.VrsOutn("TRANS_NAME2_2", 1, chkMaskStr1(printData._name, printData._judgement, printData._judgekind, 40, "*"));
            }

            //(左下)タイトル1
            svf.VrsOutn("TRANS_NAME1", 2, ttlstr2);
            //(左下)金額1
            svf.VrsOutn("TRANS_MONEY1", 2, money2);
            //(左下)タイトル2
            svf.VrsOutn("TRANS_NAME1", 2, ttlstr2);
            //(左下)金額2
            svf.VrsOutn("TRANS_MONEY2", 2, money2);
            //(左下)和暦年月日(曜日)
            svf.VrsOutn("LIMIT_DATE", 2, chkMaskStr2(prttrans2Str, printData._judgement, printData._judgekind, 10, "*"));
            //(左下)受験番号1
            svf.VrsOutn("TRANS_EXAM_NO1", 2, chkMaskStr2(printData._examNo, printData._judgement, printData._judgekind, 4, "*"));
            //(左下)固定文字列＋類型
            svf.VrsOutn("TRANS_COURSE_NAME1", 2, chkMaskStr2(typestr, printData._judgement, printData._judgekind, 11, "*"));
            //(左下)氏名1
            if (24 >= KNJ_EditEdit.getMS932ByteLength(printData._name)) {
                svf.VrsOutn("TRANS_NAME1_1", 2, chkMaskStr2(printData._name, printData._judgement, printData._judgekind, 24, "*"));
            } else if (30 >= KNJ_EditEdit.getMS932ByteLength(printData._name)) {
                svf.VrsOutn("TRANS_NAME1_2", 2, chkMaskStr2(printData._name, printData._judgement, printData._judgekind, 30, "*"));
            } else {
                svf.VrsOutn("TRANS_NAME1_3", 2, chkMaskStr2(printData._name, printData._judgement, printData._judgekind, 40, "*"));
            }
            //(左下)属性-受験番号2
            svf.VrsOutn("TRANS_EXAM_NO2", 2, chkMaskStr2(attrval + printData._examNo, printData._judgement, printData._judgekind, 6, "*"));
            //(左下)氏名かな
            if (22 >= KNJ_EditEdit.getMS932ByteLength(printData._nameKana)) {
                svf.VrsOutn("TRANS_KANA1", 2, chkMaskStr2(printData._nameKana, printData._judgement, printData._judgekind, 22, "*"));
            } else if (40 >= KNJ_EditEdit.getMS932ByteLength(printData._nameKana)) {
                svf.VrsOutn("TRANS_KANA2", 2, chkMaskStr2(printData._nameKana, printData._judgement, printData._judgekind, 40, "*"));
            } else if (60 >= KNJ_EditEdit.getMS932ByteLength(printData._nameKana)) {
                svf.VrsOutn("TRANS_KANA3", 2, chkMaskStr2(printData._nameKana, printData._judgement, printData._judgekind, 60, "*"));
            } else {
                svf.VrsOutn("TRANS_KANA4", 2, chkMaskStr2(printData._nameKana, printData._judgement, printData._judgekind, 80, "*"));
            }
            //(左下)氏名2
            if (34 >= KNJ_EditEdit.getMS932ByteLength(printData._nameKana)) {
                svf.VrsOutn("TRANS_NAME2_1", 2, chkMaskStr2(printData._name, printData._judgement, printData._judgekind, 34, "*"));
            } else {
                svf.VrsOutn("TRANS_NAME2_1", 2, chkMaskStr2(printData._name, printData._judgement, printData._judgekind, 40, "*"));
            }

            svf.VrEndPage();
            _hasData = true;
        }
    }

    private void printMain2(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJL321D_2.frm", 1);

        final List printList = getList2(db2);
        final List schoolinfo = getSchoolInfo(db2);
        final String cuttypedate[] = StringUtils.split(_param._decisionDate, "/");
        final String prtnoticeyear = KNJ_EditDate.h_format_JP_N(db2, _param._noticeDate);
        final String cutnoticedate[] = StringUtils.split(_param._noticeDate, "/");

        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final PrintData2 printData = (PrintData2) iterator.next();
            //DATE1
            svf.VrsOut("SEND_DATE", Integer.parseInt(cuttypedate[1]) + "月" + Integer.parseInt(cuttypedate[2]) + "日");
            //DATE2
            svf.VrsOut("DATE", prtnoticeyear + Integer.parseInt(cutnoticedate[1]) + "月" + Integer.parseInt(cutnoticedate[2]) + "日");

            for (Iterator iterator2 = schoolinfo.iterator(); iterator2.hasNext();) {
                final SchoolInfo printData2 = (SchoolInfo) iterator2.next();
                svf.VrsOut("SCHOOL_NAME", printData2._schoolName);
                svf.VrsOut("JOB_NAME", printData2._jobName);
                svf.VrsOut("STAFF_NAME", printData2._principalName);
            }

            svf.VrEndPage();
            _hasData = true;
        }
    }

    private void printMain3(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJL321D_3.frm", 1);

        final List printList = getList1(db2);
        final List schoolinfo = getSchoolInfo(db2);
        final String prtdate = KNJ_EditDate.h_format_JP_N(db2, _param._noticeDate);
        final String cutnoticedate[] = StringUtils.split(_param._noticeDate, "/");

        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final PrintData1 printData = (PrintData1) iterator.next();

            final String typestr = "普通科 (" + printData._pattern + ")";
            //和暦日付
            svf.VrsOut("DATE", prtdate + Integer.parseInt(cutnoticedate[1]) + "月" + Integer.parseInt(cutnoticedate[2]) + "日");
            //郵便番号
            svf.VrsOut("ZIPNO", printData._gzipcd);
            //住所1
            svf.VrsOut("ADDR1_1", printData._gaddress1);
            //住所2
            svf.VrsOut("ADDR2_1", printData._gaddress2);
            //氏名
            svf.VrsOut("NAME1", printData._name + " 様");
            //受験番号
            svf.VrsOut("EXAM_NO", printData._examNo);
            //(右)固定文字列＋類型
            svf.VrsOut("COURSE_NAME", typestr);

            for (Iterator iterator2 = schoolinfo.iterator(); iterator2.hasNext();) {
                final SchoolInfo printData2 = (SchoolInfo) iterator2.next();
                svf.VrsOut("SCHOOL_NAME", printData2._schoolName);
                svf.VrsOut("JOB_NAME", printData2._jobName);
                svf.VrsOut("STAFF_NAME", printData2._principalName);
            }

            svf.VrEndPage();
            _hasData = true;
        }
    }

    private void printMain4(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJL321D_4.frm", 1);

        final List printList = getList1(db2);
        final String prttestdivyear = KNJ_EditDate.h_format_JP_N(db2, _param._entExamYear + "/04/01");
        final String testdivstr = getNameMst(db2, "L004", _param._testDiv, "ABBV1");
        final String prtnoticeyear = KNJ_EditDate.h_format_JP_N(db2, _param._noticeDate);
        final String cutnoticedate[] = StringUtils.split(_param._noticeDate, "/");

        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final PrintData1 printData = (PrintData1) iterator.next();
            //(左)郵便番号
            svf.VrsOut("ZIPNO", printData._gzipcd);
            //(左)住所1
            svf.VrsOut("ADDR1_1", printData._gaddress1);
            //(左)住所2
            svf.VrsOut("ADDR2_1", printData._gaddress2);
            //(左)氏名1
            final int n1len = KNJ_EditEdit.getMS932ByteLength(printData._name);
            final String n1field = n1len > 34 ? "_2" : "_1";
            svf.VrsOut("NAME1" + n1field, printData._name + " 様");
            //(右)和暦年度 + 受験種別 + "入学試験"
            svf.VrsOut("TITLE", prttestdivyear + "度　" + testdivstr + "入学試験");
            //(右)受験番号
            svf.VrsOut("EXAM_NO", printData._examNo);
            //(右)氏名
            final int n2len = KNJ_EditEdit.getMS932ByteLength(printData._name);
            final String n2field = n2len > 30 ? "_4" : (n2len > 24 ? "_3" : (n2len > 14 ? "_2" : "_1"));
            svf.VrsOut("NAME2" + n2field, printData._name);
            //(右)和暦年月日
            svf.VrsOut("DATE", prtnoticeyear + Integer.parseInt(cutnoticedate[1]) + "月" + Integer.parseInt(cutnoticedate[2]) + "日");

            svf.VrEndPage();
            _hasData = true;
        }
    }

    private String chkMaskStr1(final String basestr, final String jdgval, final String jdgkind, final int length, final String setstr) {
        if ("1".equals(_param._testDiv) && "1".equals(jdgkind) || "1".equals(_param._testDiv) && "4".equals(jdgkind)) {
        	return StringUtils.repeat(setstr, length);
        }
        return basestr;
    }

    private String chkMaskStr2(final String basestr, final String jdgval, final String jdgkind, final int length, final String setstr) {
        if ("1".equals(_param._testDiv) || "2".equals(_param._testDiv) && "4".equals(jdgkind)) {
        	return StringUtils.repeat(setstr, length);
        }
        return basestr;
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

    private String getNameMst(final DB2UDB db2, final String findcode, final String code2, final String getElement) {
        String retStr = "";
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = getNameMstSql("L004", _param._testDiv, "");
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                retStr = rs.getString(getElement);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retStr;
    }

    private List getList1(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = getSql1();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String examNo = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String nameKana = rs.getString("NAME_KANA");
                final String judgement = rs.getString("JUDGEMENT");
                final String judgementPattern = rs.getString("JUDGEMENT_PATTERN");
                final String judgekind = rs.getString("JUDGE_KIND");
                final String pattern = rs.getString("PATTERN");
                final String gzipcd = rs.getString("GZIPCD");
                final String gaddress1 = rs.getString("GADDRESS1");
                final String gaddress2 = rs.getString("GADDRESS2");
                final String gzipno = rs.getString("GZIPCD");

                final PrintData1 printData = new PrintData1(examNo, name, nameKana, judgement, judgementPattern, judgekind, pattern, gzipcd, gaddress1, gaddress2, gzipno);
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

    private List getList2(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = getSql2();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String examNo = rs.getString("EXAMNO");

                final PrintData2 printData = new PrintData2(examNo);
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

    private String judgeAttribute(final String jdgval, final String jdgkind) {
        String retstr = "";
    	retstr = judgeAttributeSub(jdgval, retstr) ;
        if ("1".equals(_param._testDiv)) {
            //推薦
        	if ("1".equals(jdgkind)) {
        		retstr = "6-";
        	} else if ("2".equals(jdgkind)) {
        		retstr = "8-";
        	} else if ("3".equals(jdgkind)) {
        		retstr = "7-";
        	} else if ("4".equals(jdgkind)) {
        		retstr = "5-";
        	}
        } else {
            //一般
        	if ("4".equals(jdgkind)) {
        		retstr = "4-";
        	}
        }
        log.debug("testdiv:" + _param._testDiv + " jdgval:" + jdgval + " jdgkind:" + jdgkind + " retstr:" + retstr);
        return retstr;
    }

    private String judgeAttributeSub(final String jdgval, final String defval) {
    	String retstr = defval;
    	if ("1".equals(jdgval)) {
        	retstr = "1-";
    	} else if ("2".equals(jdgval)) {
        	retstr = "2-";
    	} else if ("3".equals(jdgval)) {
        	retstr = "3-";
    	}
        return retstr;
    }

    private String getNameMstSql(final String findcode, final String code2, final String namespare) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     NAMECD2, ");
        stb.append("     NAME1, ");
        stb.append("     ABBV1 ");
        stb.append(" FROM ");
        stb.append("     NAME_MST ");
        stb.append(" WHERE ");
        stb.append("     NAMECD1 = '" + findcode + "'");
        if (!"".equals(code2)) {
            stb.append("    AND NAMECD2 = '" + code2 + "'");
        }
        if (!"".equals(namespare)) {
            stb.append("     AND NAMESPARE2 = '" + namespare + "'");
        }
        stb.append(" ORDER BY ");
        stb.append("     NAMECD2 ASC ");

        return stb.toString();
    }

    private String getSql1() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     T1.NAME_KANA, ");
        stb.append("     T1.JUDGEMENT, ");
        stb.append("     NM2.NAMESPARE3 AS JUDGEMENT_PATTERN, ");
        stb.append("     T1.JUDGE_KIND, ");
        stb.append("     NM1.NAMESPARE3 AS PATTERN, ");
        stb.append("     AD1.GZIPCD, ");
        stb.append("     AD1.GADDRESS1, ");
        stb.append("     AD1.GADDRESS2 ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT AD1 ON T1.EXAMNO = AD1.EXAMNO AND ");
        stb.append("       T1.ENTEXAMYEAR = AD1.ENTEXAMYEAR AND ");
        stb.append("       T1.APPLICANTDIV = AD1.APPLICANTDIV");
        stb.append("     LEFT JOIN NAME_MST NM1 ON NM1.NAMECD1 = 'L012' ");
        stb.append("       AND NM1.NAMECD2 = T1.ENTDIV ");
        stb.append("     LEFT JOIN NAME_MST NM2 ON NM2.NAMECD1 = 'L013' ");
        stb.append("       AND NM2.NAMECD2 = T1.JUDGEMENT ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._entExamYear + "'");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "'");
        stb.append("     AND T1.TESTDIV = '" + _param._testDiv + "'");
        if ("1".equals(_param._noticeType)) {
            if ("2".equals(_param._outputDiv)) {
                stb.append("     AND T1.JUDGEMENT IN ('3')");
            } else {
                stb.append("     AND T1.JUDGEMENT IN ('1', '2')");
            }
        } else if ("3".equals(_param._noticeType)) {
            stb.append("     AND T1.JUDGEMENT = '3' ");
            stb.append("     AND T1.ENTDIV IN ('1', '2') ");
        } else if ("4".equals(_param._noticeType)) {
            stb.append("     AND T1.JUDGEMENT = '0' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.EXAMNO ASC");

        return stb.toString();
    }

    private String getSql2() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.EXAMNO ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._entExamYear + "'");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "'");
        stb.append("     AND T1.TESTDIV = '" + _param._testDiv + "'");
        stb.append("     AND T1.JUDGEMENT = '3' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.EXAMNO ASC");

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

    private class PrintData1 {
        final String _examNo;
        final String _name;
        final String _nameKana;
        final String _judgement;
        final String _judgementPattern;
        final String _judgekind;
        final String _pattern;
        final String _gzipcd;
        final String _gaddress1;
        final String _gaddress2;
        final String _gtelno;
        public PrintData1(
                final String examNo,
                final String name,
                final String nameKana,
                final String judgement,
                final String judgementPattern,
                final String judgekind,
                final String pattern,
                final String gzipcd,
                final String gaddress1,
                final String gaddress2,
                final String gtelno
        ) {
            _examNo = examNo;
            _name = name;
            _nameKana = nameKana;
            _judgement = judgement;
            _judgementPattern = judgementPattern;
            _judgekind = judgekind;
            _pattern = pattern;
            _gzipcd = gzipcd;
            _gaddress1 = gaddress1;
            _gaddress2 = gaddress2;
            _gtelno = gtelno;
        }
    }

    private class PrintData2 {
		final String _examNo;

        public PrintData2(
                final String examNo
        ) {
            _examNo = examNo;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 72460 $");
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
        private final String _noticeType;
        private final String _outputDiv;
        private final String _noticeDate;
        private final String _transferDate1;
        private final String _transferDate2;
        private final String _decisionDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _loginYear     = request.getParameter("LOGIN_YEAR");
            _loginSemester = request.getParameter("LOGIN_SEMESTER");
            _loginDate     = request.getParameter("LOGIN_DATE");
            _entExamYear   = request.getParameter("ENTEXAMYEAR");
            _applicantDiv  = request.getParameter("APPLICANTDIV");
            _testDiv       = request.getParameter("TESTDIV");
            _noticeType    = request.getParameter("NOTICE_TYPE");
            if ("1".equals(_noticeType)) {
                _outputDiv    = request.getParameter("OUTPUT_DIV");
                _noticeDate    = request.getParameter("NOTICE_DATE");
                _transferDate1    = request.getParameter("TRANSFER_DATE1");
                _transferDate2    = request.getParameter("TRANSFER_DATE2");
                _decisionDate = "";
            } else if ("2".equals(_noticeType)) {
                _outputDiv    = "";
                _noticeDate    = request.getParameter("NOTICE_DATE");
                _transferDate1    = "";
                _transferDate2    = "";
                _decisionDate = request.getParameter("DECISION_DATE");
            } else if ("3".equals(_noticeType)) {
                _outputDiv    = "";
                _noticeDate    = request.getParameter("NOTICE_DATE");
                _transferDate1    = "";
                _transferDate2    = "";
                _decisionDate = "";
            } else if ("4".equals(_noticeType)) {
                _outputDiv    = "";
                _noticeDate    = request.getParameter("NOTICE_DATE");
                _transferDate1    = "";
                _transferDate2    = "";
                _decisionDate = "";
            } else {
                _outputDiv    = "";
                _noticeDate    = "";
                _transferDate1    = "";
                _transferDate2    = "";
                _decisionDate = "";
            }
        }

    }
}

// eof
