/*
 * $Id: 68d310e28cd5fbe41af1a569f2668fd4b580ded1 $
 *
 * 作成日: 2018/05/09
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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL190D {

    private static final Log log = LogFactory.getLog(KNJL190D.class);

    //志願者用
    private static final String PASS          = "1"; // 1:合格通知書
    private static final String UN_PASS      = "2"; // 2:合否通知書（不合格通知）
    private static final String TR_PERMIT    = "3"; // 3:転籍許可通知書
    private static final String TR_NO_PERMIT = "4"; // 4:転籍不許可通知書

    //出身校用
    private static final String SELECTION_RESULT = "5"; // 5:入学試験（通信制課程）の選考結果について
    private static final String TR_PASS            = "6"; // 6:生徒転入学について（合格者）
    private static final String TR2_PASS           = "9"; // 9:生徒転籍について（合格者）
    private static final String TR2_UNPASS        = "7"; // 7:転入学試験の選考結果について（不合格者）
    private static final String TR_UNPASS         = "8"; // 8:転籍試験の選考結果について（不合格者）

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

            //db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2kenja", DB2UDB.TYPE2);
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

        //合格者を取得し、各合格者に対して、3つの処理を行う(転籍対象者は振込金額の紙だけ別フォームを利用)。
        List gouakulist = getList(db2, true);

        List unPassList = getList(db2, false);

        if (PASS.equals(_param._formKind)) { //合格通知書(1)
            for (Iterator iterator = gouakulist.iterator(); iterator.hasNext();) {
                PrintData goukakuInfo = (PrintData)iterator.next();

                if ("1".equals(goukakuInfo._exchange)) continue; // 転籍は出力しない

                //合格通知書
                printPass(db2, svf, goukakuInfo);

                //納入金案内
                printMoneyPaid(db2, svf, goukakuInfo);

                //納入金案内(転籍以外)
                printMoneyPaid1(db2, svf, goukakuInfo);

                _hasData = true;
            }
        } else if (UN_PASS.equals(_param._formKind)) { //合否通知書（不合格通知）(2)
            for (Iterator iterator = unPassList.iterator(); iterator.hasNext();) {
                PrintData unPassInfo = (PrintData)iterator.next();

                if ("1".equals(unPassInfo._exchange)) continue; // 転籍は出力しない

                //合否通知書（不合格通知）
                printUnPass(db2, svf, unPassInfo);

                _hasData = true;
            }
        } else if(TR_PERMIT.equals(_param._formKind)) { // 転籍許可通知書(3)
            for (Iterator iterator = gouakulist.iterator(); iterator.hasNext();) {
                PrintData goukakuInfo = (PrintData)iterator.next();

                if (!"1".equals(goukakuInfo._exchange)) continue; // 転籍のみ出力

                //転籍許可通知書
                printTrPermit(db2, svf, goukakuInfo);

                // //納入金案内
                // printMoneyPaid(db2, svf, goukakuInfo);

                // //納入金案内(転籍用)
                // printMoneyPaid3(db2, svf, goukakuInfo);

                _hasData = true;
            }
        } else if (TR_NO_PERMIT.equals(_param._formKind)) { // 転籍不許可通知書(4)
            for (Iterator iterator = unPassList.iterator(); iterator.hasNext();) {
                PrintData unPassInfo = (PrintData)iterator.next();

                if (!"1".equals(unPassInfo._exchange)) continue; // 転籍のみ出力

                //転籍不許可通知書
                printTrNoPermit(db2, svf, unPassInfo);

                _hasData = true;
            }
        } else if (SELECTION_RESULT.equals(_param._formKind)) { // 入学試験（通信制課程）の選考結果について(5)
            //入学試験（通信制課程）の選考結果について
            printSelectionResultt(db2, svf);
        } else if(TR_PASS.equals(_param._formKind) || TR2_PASS.equals(_param._formKind)) { // 生徒転入学について（合格者）(6) or 生徒転籍について（合格者）(9)
            for (Iterator iterator = gouakulist.iterator(); iterator.hasNext();) {
                PrintData goukakuInfo = (PrintData)iterator.next();

                if (TR_PASS.equals(_param._formKind)) {
                    if (!Arrays.asList("2", "3").contains(goukakuInfo._desireDiv)) continue; // 転入生・編入生
                } else if (TR2_PASS.equals(_param._formKind)) {
                    if (!Arrays.asList("4").contains(goukakuInfo._desireDiv)) continue; // 転籍
                }

                // 生徒転入学について
                printTrPass(db2, svf, goukakuInfo);

                _hasData = true;
            }
        } else if (TR2_UNPASS.equals(_param._formKind)) { // 転入学試験の選考結果について（不合格者）(7)
            for (Iterator iterator = unPassList.iterator(); iterator.hasNext();) {
                PrintData unPassInfo = (PrintData)iterator.next();

                if ("1".equals(unPassInfo._exchange) || "1".equals(unPassInfo._desireDiv)) continue; // 転籍、新入学以外出力

                //転入学試験の選考結果について（不合格者）
                printTrUnPass(db2, svf, unPassInfo);

                _hasData = true;
            }
        } else if (TR_UNPASS.equals(_param._formKind)) { // 転籍試験の選考結果について（不合格者）(8)
            for (Iterator iterator = unPassList.iterator(); iterator.hasNext();) {
                PrintData unPassInfo = (PrintData)iterator.next();

                if (!"1".equals(unPassInfo._exchange)) continue; // 転籍のみ出力

                //転籍試験の選考結果について（不合格者）
                printTrUnPass(db2, svf, unPassInfo);

                _hasData = true;
            }
        }
    }

    /** (1) 合格通知書 */
    private void printPass(final DB2UDB db2, final Vrw32alp svf, PrintData goukakuInfo) {
        // 1:新入学用　それ以外転入学、編入学用
        final String setForm = ("1".equals(goukakuInfo._desireDiv)) ? "KNJL190D_1_1.frm": "KNJL190D_1_2.frm";
        svf.VrSetForm(setForm, 1);

        svf.VrsOut("EXAMNO", goukakuInfo._examNo);
        String nameIdx = "";
        int namelen = KNJ_EditEdit.getMS932ByteLength(goukakuInfo._name);
        if (namelen <= 30) {
            nameIdx = "1";
        } else if (namelen <= 40) {
            nameIdx = "2";
        } else if (namelen <= 50) {
            nameIdx = "3";
        } else {
            nameIdx = "4";
        }
        svf.VrsOut("NAME" + nameIdx, goukakuInfo._name);

        // 転入学日
        if ("2".equals(goukakuInfo._desireDiv) || "3".equals(goukakuInfo._desireDiv)) {
            svf.VrsOut("ENT_DIV", "2".equals(goukakuInfo._desireDiv) ? "転入学" : "編入学");
        }
        svf.VrsOut("ENT_DATE", KNJ_EditDate.h_format_JP(db2, _param._tengakuDate));

        // 学校情報
        schoolInfoPrint(db2, svf, true);

        svf.VrEndPage();
    }

    /** 納入金案内 */
    private void printMoneyPaid(final DB2UDB db2, final Vrw32alp svf, PrintData goukakuInfo) {
        svf.VrSetForm("KNJL190D_1_3.frm", 1);
        svf.VrsOut("DUMMY", "prt");

        // 学校情報
        schoolInfoPrint(db2, svf, true);

        svf.VrEndPage();
    }

    /** 納入金案内(転籍以外) */
    private void printMoneyPaid1(final DB2UDB db2, final Vrw32alp svf, PrintData goukakuInfo) {
        //新入生の出力でなぎさ中なら納入金のフォームを変える
        //転入生の出力でなぎさ高校(転編入照会日アリ)なら納入金のフォームを変える
        //※中学/高校共に入力された場合を考慮して、高校から先にチェックする。
        if (("2".equals(goukakuInfo._desireDiv) && "3430004".equals(goukakuInfo._finHschoolCd) && !"".equals(goukakuInfo._tenhenInq_date))
             ||  ("1".equals(goukakuInfo._desireDiv) && "3400011".equals(goukakuInfo._finJschoolCd))) {
                svf.VrSetForm("KNJL190D_3_2.frm", 1);
        } else {
            svf.VrSetForm("KNJL190D_1_4.frm", 1);
        }
        final String prttransferyear = KNJ_EditDate.h_format_JP_N(db2, _param._transferDate1);
        final String cuttransferdate[] = StringUtils.split(_param._transferDate1, "/");
        svf.VrsOut("LIMIT", prttransferyear + cuttransferdate[1] + "月" + cuttransferdate[2] + "日");
        svf.VrEndPage();
    }

    /** (2) 合否通知書（不合格通知） */
    private void printUnPass(final DB2UDB db2, final Vrw32alp svf, PrintData goukakuInfo) {
        svf.VrSetForm("KNJL190D_2.frm", 1);

        svf.VrsOut("EXAMNO", goukakuInfo._examNo);
        final String nameIdx = KNJ_EditEdit.getMS932ByteLength(goukakuInfo._name) > 54 ? "2": "1";
        svf.VrsOut("NAME" + nameIdx, goukakuInfo._name + "　様");

        // 学校情報
        schoolInfoPrint(db2, svf, true);

        svf.VrEndPage();
    }

    /** (3) 転籍許可通知書 */
    private void printTrPermit(final DB2UDB db2, final Vrw32alp svf, PrintData goukakuInfo) {
        svf.VrSetForm("KNJL190D_3_1.frm", 1);

        svf.VrsOut("EXAMNO", goukakuInfo._examNo);
        String nameIdx = "";
        int namelen = KNJ_EditEdit.getMS932ByteLength(goukakuInfo._name);
        if (namelen <= 30) {
            nameIdx = "1";
        } else if (namelen <= 40) {
            nameIdx = "2";
        } else if (namelen <= 50) {
            nameIdx = "3";
        } else {
            nameIdx = "4";
        }
        svf.VrsOut("NAME" + nameIdx, goukakuInfo._name);

        // 転籍許可日
        svf.VrsOut("ENT_DATE", KNJ_EditDate.h_format_JP(db2, _param._tensekiDate));

        // 学校情報
        schoolInfoPrint(db2, svf, true);

        svf.VrEndPage();
    }

    /** 納入金案内(転籍用) */
    private void printMoneyPaid3(final DB2UDB db2, final Vrw32alp svf, PrintData goukakuInfo) {
        svf.VrSetForm("KNJL190D_3_2.frm", 1);

        final String prttransferyear = KNJ_EditDate.h_format_JP_N(db2, _param._transferDate1);
        final String cuttransferdate[] = StringUtils.split(_param._transferDate1, "/");
        svf.VrsOut("LIMIT", prttransferyear + cuttransferdate[1] + "月" + cuttransferdate[2] + "日");

        svf.VrEndPage();
    }

    /** (4) 転籍不許可通知書 */
    private void printTrNoPermit(final DB2UDB db2, final Vrw32alp svf, PrintData goukakuInfo) {
        svf.VrSetForm("KNJL190D_4.frm", 1);

        svf.VrsOut("EXAMNO", goukakuInfo._examNo);

        String nameIdx = "";
        int namelen = KNJ_EditEdit.getMS932ByteLength(goukakuInfo._name);
        if (namelen <= 30) {
            nameIdx = "1";
        } else if (namelen <= 40) {
            nameIdx = "2";
        } else if (namelen <= 50) {
            nameIdx = "3";
        } else {
            nameIdx = "4";
        }
        svf.VrsOut("NAME" + nameIdx, goukakuInfo._name);

        // 学校情報
        schoolInfoPrint(db2, svf, true);

        svf.VrEndPage();
    }

    /** (5) 入学試験（通信制課程）の選考結果について */
    private void printSelectionResultt(final DB2UDB db2, final Vrw32alp svf) {
        final List schList = getList(db2, true);
        String befSchoolCd = "";

        int schCnt = 0;

        for (Iterator it = schList.iterator(); it.hasNext();) {
            final PrintData priData = (PrintData) it.next();

            if (!"1".equals(priData._desireDiv)) continue; // 新入学のみ

            if (!befSchoolCd.equals(priData._finJschoolCd)) {
                svf.VrSetForm("KNJL190D_5.frm", 4);
                schCnt = 0;
            }

            // 出身学校
            final String setFinschName = priData._finJschoolName + "　　校長　様";
            svf.VrsOut("FINSCHOOL_NAME" , setFinschName);

            // 時候の挨拶
            final String setGreet = StringUtils.defaultString(_param._greetName) + "、";
            svf.VrsOut("TEXT" , setGreet);

            // 生徒情報
            svf.VrsOut("RESULT" , priData._judgeName);  // 結果
            svf.VrsOut("EXAM_NO" , priData._examNo);    // 受験番号
            int namelen = KNJ_EditEdit.getMS932ByteLength(priData._name);
            final String nameIdx = namelen <= 30 ? "1": namelen <= 40 ? "2": namelen <= 50 ? "3": "4";
            svf.VrsOut("NAME" + nameIdx, priData._name); // 氏名
            svf.VrsOut("HOPE" , "普通科");               // 志望学科

            schCnt++;
            if (_param._finSchCntMap.get(priData._finJschoolCd).equals(String.valueOf(schCnt))) {
                svf.VrsOut("END" , "以上"); // 以上
            }

            // 学校情報
            schoolInfoPrint(db2, svf, false);

            svf.VrEndRecord();
            befSchoolCd = priData._finJschoolCd;
            _hasData = true;
        }

        svf.VrEndPage();
    }

    /** (6) 生徒転入学について */
    private void printTrPass(final DB2UDB db2, final Vrw32alp svf, PrintData goukakuInfo) {
        if (TR_PASS.equals(_param._formKind)) {
            svf.VrSetForm("KNJL190D_6.frm", 1);
        } else if (TR2_PASS.equals(_param._formKind)) {
            svf.VrSetForm("KNJL190D_9.frm", 1);
        }

        // 出身学校
        final String setFinschName = goukakuInfo._finHschoolName + "　　校長　様";
        svf.VrsOut("FINSCHOOL_NAME" , setFinschName);

        // 時候の挨拶
        final String setGreet = StringUtils.defaultString(_param._greetName) + "、";
        svf.VrsOut("TEXT" , setGreet);

        // 学校情報
        schoolInfoPrint(db2, svf, false);

        // 照会日
        svf.VrsOut("QUERY_DATE", KNJ_EditDate.h_format_JP(db2, goukakuInfo._inquiryDate));

        final String nameIdx = KNJ_EditEdit.getMS932ByteLength(goukakuInfo._name) > 30 ? "3": KNJ_EditEdit.getMS932ByteLength(goukakuInfo._name) > 16 ? "2": "1";
        svf.VrsOut("NAME" + nameIdx, goukakuInfo._name);

        // 転入学許可日
        svf.VrsOut("ENT_DATE", KNJ_EditDate.h_format_JP(db2, _param._tengakuDate));

        // 日本スポーツ振興センター加入証明書
        if (_param._isSportCertif && ArrayUtils.contains(_param._examNos, goukakuInfo._examNo)) {
            svf.VrsOut("SPORTS", "　　⑤日本スポーツ振興センター加入証明書");
        }

        //学校名
        svf.VrsOut("SCHOOL_NAME2", _param._certifSchool._schoolName + "　通信制課程　校長　宛");

        svf.VrEndPage();
    }

    /** (7) 転入学試験の選考結果について（不合格者）<br>
     *  (8) 転籍試験の選考結果について（不合格者）
     */
    private void printTrUnPass(final DB2UDB db2, final Vrw32alp svf, PrintData goukakuInfo) {
        final String setForm = (TR2_UNPASS.equals(_param._formKind)) ? "KNJL190D_7.frm": "KNJL190D_8.frm";
        svf.VrSetForm(setForm, 1);

        // 出身学校
        final String setFinschName = goukakuInfo._finHschoolName + "　　校長　様";
        svf.VrsOut("FINSCHOOL_NAME" , setFinschName);

        // 学校情報
        schoolInfoPrint(db2, svf, false);

        // 時候の挨拶
        final String setGreet = StringUtils.defaultString(_param._greetName) + "、";
        svf.VrsOut("TEXT" , setGreet);

        svf.VrsOut("EXAMNO", goukakuInfo._examNo);

        final String nameIdx = KNJ_EditEdit.getMS932ByteLength(goukakuInfo._name) > 26 ? "2": "1";
        svf.VrsOut("NAME" + nameIdx, goukakuInfo._name);

        svf.VrEndPage();
    }

    /**
     * 学校情報セット
     * @param db2
     * @param svf
     * @param isLogoPrint 「true：校長名の空白をカット　false：しない」
     */
    private void schoolInfoPrint(final DB2UDB db2, final Vrw32alp svf, final boolean isLogoPrint) {
        //校証
        if (isLogoPrint && null != _param._schoollogoFilePath) {
            svf.VrsOut("SCHOOL_LOGO", _param._schoollogoFilePath);
        }

        //印刷日
        final String prtnoticeyear = KNJ_EditDate.h_format_JP_N(db2, _param._noticeDate);
        final String cutnoticedate[] = StringUtils.split(_param._noticeDate, "/");
        svf.VrsOut("DATE", prtnoticeyear + cutnoticedate[1] + "月" + cutnoticedate[2] + "日");

        //学校名
        svf.VrsOut("SCHOOL_NAME", _param._certifSchool._schoolName);

        //郵便番号
        svf.VrsOut("ZIP_NO", _param._certifSchool._schoolZipCd);

        //学校住所
        final String addr = StringUtils.defaultString(_param._certifSchool._schoolAddr1) + StringUtils.defaultString(_param._certifSchool._schoolAddr2);
        svf.VrsOut("SCHOOL_ADDR", addr);

        //電話番号
        svf.VrsOut("TEL1", "TEL " + _param._certifSchool._schoolTel);

        //FAX番号
        svf.VrsOut("TEL2", "FAX " + _param._certifSchool._schoolFax);

        //職名
        svf.VrsOut("JOB_NAME", _param._certifSchool._jobName);

        //校長名
        if (isLogoPrint) {
            svf.VrsOut("STAFF_NAME", StringUtils.deleteWhitespace(_param._certifSchool._principalName));
        } else {
            svf.VrsOut("STAFF_NAME", _param._certifSchool._principalName);
        }
    }

    private List getList(final DB2UDB db2, final boolean pass) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = getSql(pass);
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            String befSchoolCd = "";
            int schCnt = 0;

            while (rs.next()) {
                if (!befSchoolCd.equals(rs.getString("FS_JCD"))) schCnt = 0;

                final String desireDiv      = rs.getString("DESIREDIV");
                final String examNo         = rs.getString("EXAMNO");
                final String name           = rs.getString("NAME");
                final String exchange       = rs.getString("REMARK1");
                final String finJschoolCd   = rs.getString("FS_JCD");
                final String finJschoolName = rs.getString("FS_JNAME");
                final String finHschoolCd   = rs.getString("FS_HCD");
                final String finHschoolName = rs.getString("FS_HNAME");
                final String judgeName      = rs.getString("JUDGE_NAME");
                final String tenhenInq_date = rs.getString("TENHEN_INQ_DATE");
                final String inquiryDate    = rs.getString("INQUIRY_DATE");

                schCnt++;
                _param._finSchCntMap.put(finJschoolCd, String.valueOf(schCnt));
                befSchoolCd = finJschoolCd;

                final PrintData printData = new PrintData(desireDiv, examNo, name, exchange, finJschoolCd, finJschoolName, finHschoolCd, finHschoolName, judgeName, tenhenInq_date, inquiryDate);
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

    private String getSql(final boolean pass) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.DESIREDIV, ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     T2.REMARK1, ");// 転籍フラグ
        stb.append("     value(T1.FS_CD, '') as FS_JCD, ");
        stb.append("     value(FINJ.FINSCHOOL_NAME, '') as FS_JNAME, ");
        stb.append("     value(T2.REMARK2, '') as FS_HCD, ");
        stb.append("     value(FINH.FINSCHOOL_NAME, '') as FS_HNAME, ");
        stb.append("     L013.NAME1 as JUDGE_NAME, ");
        stb.append("     T2.REMARK8 as TENHEN_INQ_DATE, ");  //転編入学照会日
        stb.append("     T2.REMARK9 as INQUIRY_DATE "); // 調査書照会日
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T2 ");
        stb.append("        ON T1.EXAMNO       = T2.EXAMNO ");
        stb.append("       AND T1.ENTEXAMYEAR  = T2.ENTEXAMYEAR ");
        stb.append("       AND T1.APPLICANTDIV = T2.APPLICANTDIV ");
        stb.append("       AND T2.SEQ          = '033' ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FINJ ON FINJ.FINSCHOOLCD = T1.FS_CD ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FINH ON FINH.FINSCHOOLCD = T2.REMARK2 ");
        stb.append("     LEFT JOIN NAME_MST L013 ON L013.NAMECD1 = 'L013' ");
        stb.append("                            AND L013.NAMECD2 = T1.JUDGEMENT ");
        stb.append(" WHERE ");
        stb.append("         T1.ENTEXAMYEAR  = '"+ _param._entExamYear +"' ");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND T1.TESTDIV      = '" + _param._testDiv + "' ");
        stb.append("     AND T1.DESIREDIV    = '" + _param._desireDiv +"' ");
        if (!SELECTION_RESULT.equals(_param._formKind)) {
            if (pass) {
                stb.append("     AND T1.JUDGEMENT    = '1' "); // 合格
            } else {
                stb.append("     AND T1.JUDGEMENT    = '0' "); // 不合格
            }
        }
        stb.append(" ORDER BY ");
        if (SELECTION_RESULT.equals(_param._formKind)) {
            stb.append("     T1.FS_CD, ");
        }
        stb.append("     T1.DESIREDIV, ");
        stb.append("     T1.EXAMNO ");

        return stb.toString();
    }

    private class PrintData {
        final String _desireDiv;
        final String _examNo;
        final String _name;
        final String _exchange;
        final String _finJschoolCd;
        final String _finJschoolName;
        final String _finHschoolCd;
        final String _finHschoolName;
        final String _judgeName;
        final String _tenhenInq_date;
        /** 調査書照会日 */
        final String _inquiryDate;
        public PrintData(
                final String desireDiv,
                final String examNo,
                final String name,
                final String exchange,
                final String finJschoolCd,
                final String finJschoolName,
                final String finHschoolCd,
                final String finHschoolName,
                final String judgeName,
                final String tenhenInq_date,
                final String inquiryDate
        ) {
            _desireDiv      = desireDiv;
            _examNo         = examNo;
            _name           = name;
            _exchange       = exchange;
            _finJschoolCd   = finJschoolCd;
            _finJschoolName = finJschoolName;
            _finHschoolCd   = finHschoolCd;
            _finHschoolName = finHschoolName;
            _judgeName      = judgeName;
            _tenhenInq_date = tenhenInq_date;
            _inquiryDate    = inquiryDate;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 71281 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    //証明書学校データ
    private class CertifSchool {
        final String _schoolName;
        final String _jobName;
        final String _principalName;
        final String _schoolZipCd;
        final String _schoolAddr1;
        final String _schoolAddr2;
        final String _schoolTel;
        final String _schoolFax;
        public CertifSchool(
                final String schoolName,
                final String jobName,
                final String principalName,
                final String schoolZipCd,
                final String schoolAddr1,
                final String schoolAddr2,
                final String schoolTel,
                final String schoolFax
        ) {
            _schoolName     = schoolName;
            _jobName        = jobName;
            _principalName  = principalName;
            _schoolZipCd    = schoolZipCd;
            _schoolAddr1    = schoolAddr1;
            _schoolAddr2    = schoolAddr2;
            _schoolTel      = schoolTel;
            _schoolFax      = schoolFax;
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
        private final String _desireDiv;
        private final String _sendTo;
        private final String _formKind;
        private final String _noticeDate;
        private final String _transferDate1;
        private final String _tengakuDate;
        private final String _tensekiDate;
        private final String _greetMonth;
        private final String _greet;
        private final String _greetName;
        private final boolean _isSportCertif;
        private final String[] _examNos;

        private final String _documentroot;
        private final String _imagepath;
        final String _schoollogoFilePath;
        final CertifSchool _certifSchool;

        final Map _finSchCntMap = new TreeMap();

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _loginYear      = request.getParameter("LOGIN_YEAR");
            _loginSemester  = request.getParameter("LOGIN_SEMESTER");
            _loginDate      = request.getParameter("LOGIN_DATE");
            _entExamYear    = request.getParameter("ENTEXAMYEAR");
            _applicantDiv   = request.getParameter("APPLICANTDIV");
            _testDiv        = request.getParameter("TESTDIV");          //入試区分
            _desireDiv      = request.getParameter("DESIREDIV");        //志望区分
            _sendTo         = request.getParameter("SEND_TO");          //送付先(1:志願者 2:出身校)
            _formKind       = request.getParameter("FORM_KIND");        //
            _noticeDate     = request.getParameter("NOTICE_DATE");      //通知書発行日（1～8）
            _transferDate1  = request.getParameter("TRANSFER_DATE1");   //納入締切日（1）
            _tengakuDate    = request.getParameter("TENNYUGAKU_DATE");  //転入学日（1、6）
            _tensekiDate    = request.getParameter("TENSEKI_DATE");     //転籍許可日（3）
            _greetMonth     = request.getParameter("MONTH");            //時候の挨拶 月（5～8）
            _greet          = request.getParameter("GREET");            //時候の挨拶（5～8）
            _greetName      = getGreet(db2, _greetMonth, _greet);

            _isSportCertif  = "1".equals(request.getParameter("SPORT_CERTIF")); //日本スポーツ振興センター加入証明書checkBox(6)
            _examNos        = request.getParameterValues("SPORT_SELECTED");     //日本スポーツ振興センター加入証明書出力する人(6)

            _documentroot       = request.getParameter("DOCUMENTROOT");
            _imagepath          = request.getParameter("IMAGEPATH");
            _schoollogoFilePath = getImageFilePath("SCHOOLLOGO.jpg");
            _certifSchool       = getCertifSchool(db2);

        }

        /** 時候の挨拶取得 */
        private String getGreet(final DB2UDB db2, final String month, final String greet) {
            String retGreetName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT GREETING FROM SEASON_GREETINGS_MST WHERE MONTH = '" + month + "' AND SEQ = '" + greet + "' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    retGreetName = rs.getString("GREETING");
                }
            } catch (SQLException ex) {
                log.debug("getGreet exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retGreetName;
        }

        /** 証明書学校データ */
        private CertifSchool getCertifSchool(final DB2UDB db2) {
            CertifSchool certifSchool = new CertifSchool(null, null, null, null, null, null, null, null);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {

                ps = db2.prepareStatement("SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _entExamYear + "' AND CERTIF_KINDCD = '106' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    final String schoolName     = rs.getString("SCHOOL_NAME");
                    final String jobName        = rs.getString("JOB_NAME");
                    final String principalName  = rs.getString("PRINCIPAL_NAME");
                    final String schoolZipCd    = rs.getString("REMARK2");
                    final String schoolAddr1    = rs.getString("REMARK4");
                    final String schoolAddr2    = rs.getString("REMARK5");
                    final String schoolTel      = rs.getString("REMARK1");
                    final String schoolFax      = rs.getString("REMARK10");
                    certifSchool = new CertifSchool(schoolName, jobName, principalName, schoolZipCd, schoolAddr1, schoolAddr2, schoolTel, schoolFax);
                }
            } catch (SQLException ex) {
                log.debug("getCertif exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return certifSchool;
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
