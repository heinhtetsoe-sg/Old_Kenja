/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 *
 * 作成日: 2020/08/07
 * 作成者: matsushima
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJEditString;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;

public class KNJD426M {

    private static final Log log = LogFactory.getLog(KNJD426M.class);

    private boolean _hasData;
    private final String HOUTEI = "1";
    private final String JITSU = "2";
    private static final String SEMEALL = "9";

    private static int OUTPUT_GYO_ALL = 99;

    int _lineCnt = 0; //出力行数
    KNJEditString knjobj = new KNJEditString();

    //フォーム名称
    final String PRINT_FRM1_1 = "KNJD426M_1_1.frm";
    final String PRINT_FRM1_2 = "KNJD426M_1_2.frm";
    final String PRINT_FRM2 = "KNJD426M_2.frm";
    final String PRINT_FRM3 = "KNJD426M_3.frm";
    final String PRINT_FRM4 = "KNJD426M_4.frm";
    final String PRINT_FRM5 = "KNJD426M_5.frm";

    //項目の色付け
    private final String ITEM_PAINT = "PAINT=(0,85,2)";

    //各ページの最大行数
    private final int PAGE_MAX_LINE1_1 = 36;
    private final int PAGE_MAX_LINE1_2 = 54;
    private final int PAGE_MAX_LINE3 = 52;
    private final int PAGE_MAX_LINE4 = 51;
    private final int PAGE_MAX_LINE5 = 54;

    private Param _param;

    /**
     * @param request
     *            リクエスト
     * @param response
     *            レスポンス
     */
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws Exception {

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
        final List printList = getList(db2);
        final List subTitleList = new ArrayList();
        subTitleList.add("学部");
        subTitleList.add("年・組");
        subTitleList.add("障害種別");
        subTitleList.add("作成日");
        subTitleList.add("作成者");
        subTitleList.add("氏名");
        subTitleList.add("性別");
        subTitleList.add("生年月日");
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final Student student = (Student) iterator.next();

            //1ページ目
            printPage1(db2, svf, subTitleList, student);

            //自立活動実態シート
            if(_param._printBlock1) {
                printPage2(db2, svf, subTitleList, student);
            }

            //各教科等における1年間の目標
            if(_param._printBlock3) {
                printPage3(db2, svf, student);
            }

            //重点目標, 各教科等の記録
            if(_param._printBlock2 || _param._printBlock4) {
                final Block block8 = (Block) student._blockMap.get("08");
                if (null != block8) {
                    block8.printOut(svf, student);
                }
            }

            //次年度の目標
            if(_param._printBlock5) {
                printPage5(db2, svf, student);
            }
        }
    }

    private void printPage1(final DB2UDB db2, final Vrw32alp svf, final List subTitleList, final Student student) {
        svf.VrSetForm(PRINT_FRM1_1, 4);
        svf.VrsOut("SCHOOL_NAME", _param._schoolName);

        int titleCnt = 1;
        for (Iterator itTitle = subTitleList.iterator(); itTitle.hasNext();) {
            final String subTitleName = (String) itTitle.next();
            final String titleField = "SUB_TITLE" + titleCnt;
            svf.VrAttribute(titleField, ITEM_PAINT);
            svf.VrsOut(titleField, subTitleName);
            titleCnt++;
        }
        //学部
        svf.VrsOut("DEPARTMENT_NAME", student._gakubuName);
        //作成日
        svf.VrsOut("MAKE_DATE", getEditDate(db2, student._createDate));
        //作成者
        svf.VrsOut("MAKER1", student._createUser);
        //年組
        svf.VrsOut("GRADE_NAME", student.getHrName());
        //氏名
        int nameLen = KNJ_EditEdit.getMS932ByteLength(student._name);
        final String nameField = nameLen > 30 ? "3" : nameLen > 20 ? "2" : "1";
        svf.VrsOut("NAME" + nameField, student._name);
        //かな
        int kanaLen = KNJ_EditEdit.getMS932ByteLength(student._nameKana);
        final String kanaField = kanaLen > 30 ? "3" : kanaLen > 20 ? "2" : "1";
        svf.VrsOut("KANA" + kanaField, student._nameKana);

        //障害情報
        final Assessment assessment = student._assessment;
        if(assessment != null) {
            //障害名等
            if(_param._printColumn1) {
                svf.VrAttribute("SUB_TITLE9", ITEM_PAINT);
                svf.VrsOut("SUB_TITLE9", "障害名等");
                final String[] challengeNameArray = KNJ_EditEdit.get_token(assessment._challengedNames, 100, 2);
                if (null != challengeNameArray) {
                    for (int i = 0; i < challengeNameArray.length; i++) {
                        svf.VrsOutn("DIAG_NAME", i + 1, challengeNameArray[i]);
                    }
                }
            }
            //実態概要・障害の特性
            if(_param._printColumn2) {
                svf.VrAttribute("SUB_TITLE10", ITEM_PAINT);
                svf.VrsOut("SUB_TITLE10", "実態概要・障害の特性");
                final String[] challengeStatusArray = KNJ_EditEdit.get_token(assessment._challengedStatus, 100, 3);
                if (null != challengeStatusArray) {
                    for (int i = 0; i < challengeStatusArray.length; i++) {
                        svf.VrsOutn("DIAG_NAME1", i + 1, challengeStatusArray[i]);
                    }
                }
            }
            //検査
            if(_param._printColumn3) {
                //タイトル
                final String[] kensaTitle = {"検査","検査日","検査名","検査機関","検査日","検査名","検査機関"};
                titleCnt = 11;
                for (int i = 0 ; i < kensaTitle.length; i++) {
                    final String field = "SUB_TITLE" + titleCnt;
                    svf.VrAttribute(field, ITEM_PAINT);
                    svf.VrsOut(field, kensaTitle[i]);
                    titleCnt++;
                }

                //検査日1
                svf.VrsOut("INSPECT_DATE1", getEditDate(db2, assessment._checkDate1));
                //検査機関1
                svf.VrsOut("INSPECT_AGENT1", assessment._checkCenterText1);
                //検査名1
                svf.VrsOut("INSPECT_ITEM1", assessment._checkName1);
                //検査日2
                svf.VrsOut("INSPECT_DATE2", getEditDate(db2, assessment._checkDate2));
                //検査機関2
                svf.VrsOut("INSPECT_AGENT2", assessment._checkCenterText2);
                //検査名2
                svf.VrsOut("INSPECT_ITEM2", assessment._checkName2);
            }
        }

        if((_param._printColumn4 && student._challengedSupportplanRecordMap.size() > 0)
            || (_param._printColumn5 && student._challengedSupportplanMainDat != null)) {
            svf.VrsOut("SEPARATE", "SEPARATE");//空行挿入
        }

        //願い
        int pageLine = 1;
        int PageMaxLine = PAGE_MAX_LINE1_1;
        int grp = 1;
        if(_param._printColumn4 && student._challengedSupportplanRecordMap.size() > 0) {
            final int keta = 44;
            final String kindNo = "01";
            if(_param._kindNameMap.containsKey(kindNo)) {
                svf.VrAttribute("TITLE1", ITEM_PAINT);
                svf.VrsOut("TITLE1", "願い");
                svf.VrAttribute("DIV1", ITEM_PAINT);
                svf.VrAttribute("CONTENT1_1", ITEM_PAINT);
                svf.VrAttribute("CONTENT1_2", ITEM_PAINT);
                svf.VrsOut("GRP1_1", String.valueOf(grp));
                svf.VrsOut("GRP1_2", String.valueOf(grp));
                svf.VrsOut("GRP1_3", String.valueOf(grp));
                svf.VrsOut("CONTENT1_1", centering(_param.getKindNameMapValue(kindNo, "000", "STATUS_NAME1"), 22)); //左列 項目名
                svf.VrsOut("CONTENT1_2", centering(_param.getKindNameMapValue(kindNo, "000", "STATUS_NAME2"), 22)); //右列 項目名
                svf.VrEndRecord();
                grp++;
                pageLine++;

                final Map kindNameMap = (Map) _param._kindNameMap.get(kindNo);
                for (Iterator it = kindNameMap.keySet().iterator(); it.hasNext();) {
                    final String kindSeq = (String) it.next();
                    if("000".equals(kindSeq)) continue;
                    if(Integer.parseInt(kindSeq) > 8) break;
                    final Map map = (Map) kindNameMap.get(kindSeq);
                    final String div = String.format("%02d", Integer.parseInt(kindSeq));
                    if(student._challengedSupportplanRecordMap.containsKey(div)){
                        final ChallengedSupportplanRecordDat outData = (ChallengedSupportplanRecordDat) student._challengedSupportplanRecordMap.get(div);
                        final List kindName = knjobj.retDividString((String) map.get("KIND_NAME"), 10, 3);
                        final List hope1 = knjobj.retDividString(outData._hope1, keta, OUTPUT_GYO_ALL);
                        final List hope2 = knjobj.retDividString(outData._hope2, keta, OUTPUT_GYO_ALL);
                        int maxLen = hope1.size() > hope2.size() ? hope1.size() : hope2.size();
                        if(kindName.size() > maxLen ) maxLen = kindName.size();

                        //改ページ
                        pageLine += maxLen;
                        if(pageLine > PageMaxLine) {
                            svf.VrEndPage();
                            svf.VrSetForm(PRINT_FRM1_2, 4);
                            pageLine = maxLen;
                            PageMaxLine = PAGE_MAX_LINE1_2;

                            svf.VrAttribute("TITLE1", ITEM_PAINT);
                            svf.VrsOut("TITLE1", "願い");
                            pageLine++;

                            svf.VrAttribute("DIV1", ITEM_PAINT);
                            svf.VrAttribute("CONTENT1_1", ITEM_PAINT);
                            svf.VrAttribute("CONTENT1_2", ITEM_PAINT);
                            svf.VrsOut("GRP1_1", String.valueOf(grp));
                            svf.VrsOut("GRP1_2", String.valueOf(grp));
                            svf.VrsOut("GRP1_3", String.valueOf(grp));

                            svf.VrsOut("CONTENT1_1", centering(_param.getKindNameMapValue(kindNo, "000", "STATUS_NAME1"), 22)); //左列 項目名
                            svf.VrsOut("CONTENT1_2", centering(_param.getKindNameMapValue(kindNo, "000", "STATUS_NAME2"), 22)); //右列 項目名

                            svf.VrEndRecord();
                            pageLine++;
                            grp++;
                        }

                        for (int i = 0 ; i < maxLen; i++) {
                            svf.VrAttribute("DIV1", ITEM_PAINT);
                            svf.VrsOut("GRP1_1", String.valueOf(grp));
                            svf.VrsOut("GRP1_2", String.valueOf(grp));
                            svf.VrsOut("GRP1_3", String.valueOf(grp));
                            if(i < kindName.size()) svf.VrsOut("DIV1", (String) kindName.get(i)); //縦列 項目名
                            if(i < hope1.size()) svf.VrsOut("CONTENT1_1", (String) hope1.get(i));
                            if(i < hope2.size()) svf.VrsOut("CONTENT1_2", (String) hope2.get(i));
                            svf.VrEndRecord();
                        }
                        grp++;
                    }
                }
            }
        }


        //合理的配慮
        if(_param._printColumn5) {
            if(student._challengedSupportplanMainDat != null) {
                final List content = knjobj.retDividString(student._challengedSupportplanMainDat._reasonable_accommodation, 110, 20);
                pageLine += 1 + content.size(); //タイトル + 合理的配慮
                String str = "";
                //改ページ
                if(pageLine > PageMaxLine) {
                    svf.VrEndPage();
                    svf.VrSetForm(PRINT_FRM1_2, 4);
                }
                svf.VrAttribute("TITLE1", ITEM_PAINT);
                svf.VrsOut("TITLE1", str + "合理的配慮");
                for (int i = 0 ; i < content.size(); i++) {
                    svf.VrsOut("GRP2", String.valueOf(grp));
                    svf.VrsOut("CONTENT2", (String) content.get(i));
                    svf.VrEndRecord();
                }
                if (content.size() == 0) {
                    svf.VrsOut("GRP2", "aa");
                    svf.VrEndRecord();
                }
            }
        }

        //空行１ページ目のレコードがない時の対策
        svf.VrsOut("BLANK", "1");
        svf.VrEndRecord();
        _hasData = true;
    }

    /*-----------------------------------------------------------------------------------------------
     *	日付の編集
     *		2006-07-24 --> H.18.7.24
     *		2006/07/24 --> H.18.7.24
     *-----------------------------------------------------------------------------------------------*/
    private String getEditDate(final DB2UDB db2, final String date) {
        if("".equals(date)) {
            return "";
        }
        final String[] tateFormat = KNJ_EditDate.tate_format(KNJ_EditDate.h_format_JP(db2, date));
        final String nengo = KNJ_EditDate.gengouAlphabetMarkOfDate(db2, StringUtils.replace(date, "-", "/"));
        return nengo + ("元".equals(tateFormat[1]) ? "1" : tateFormat[1]) + "." + tateFormat[2] + "." + tateFormat[3];
    }

    private void printPage2(final DB2UDB db2, final Vrw32alp svf, final List subTitleList, final Student student) {
        svf.VrSetForm(PRINT_FRM2, 1);
        svf.VrsOut("SCHOOL_NAME", _param._schoolName);

        String title = "自立活動実態シート";
        if(_param._hreportGuidanceKindNameHDatMap.containsKey("01")) {
            title = (String) _param._hreportGuidanceKindNameHDatMap.get("01");
        }
        svf.VrsOut("TITLE", title); //頁タイトル

        int titleCnt = 1;
        for (Iterator itTitle = subTitleList.iterator(); itTitle.hasNext();) {
            final String subTitleName = (String) itTitle.next();
            final String titleField = "SUB_TITLE" + titleCnt;
            svf.VrAttribute(titleField, ITEM_PAINT);
            svf.VrsOut(titleField, subTitleName);
            titleCnt++;
        }
        //学部
        svf.VrsOut("DEPARTMENT_NAME", student._gakubuName);
        //作成日
        svf.VrsOut("MAKE_DATE", getEditDate(db2, student._createDate));
        //作成者
        svf.VrsOut("MAKER1", student._createUser);
        //年組
        svf.VrsOut("GRADE_NAME", student.getHrName());
        //氏名
        svf.VrsOut("NAME1", student._name);
        //かな
        svf.VrsOut("KANA1", student._nameKana);

        //タイトル
        final String[] subsTitle = {"児童・生徒の実態","健康の保持","心理的な安定","人間関係の形成","環境の把握","身体の動き","コミュニケーション","指導目標","具体的な指導内容"};
        final String[] subsTitleFIeld = {"7","8_1","8_2","8_3","8_4","8_5","8_6","9","11"};
        for (int i = 0 ; i < subsTitle.length; i++) {
            final String field = "SUB_TITLE"+subsTitleFIeld[i];
            svf.VrAttribute(field, ITEM_PAINT);
            svf.VrsOut(field, subsTitle[i]);
        }

        final String div = "01";
        if(student._hreportGuidanceSchregRemarkMap.containsKey(SEMEALL)) {
            final Map divMap = (Map) student._hreportGuidanceSchregRemarkMap.get(SEMEALL);
            if(divMap.containsKey(div)) {

                //内容
                final Map seqMap = (Map) divMap.get(div);
                for (Iterator it = seqMap.keySet().iterator(); it.hasNext();) {
                    final String seq = (String) it.next();
                    final HreportGuidanceSchregRemarkDat outData = (HreportGuidanceSchregRemarkDat) seqMap.get(seq);

                    final int intSeq = Integer.parseInt(seq);
                    if(intSeq <= 8) {
                        //SEQ 1～8
                        //児童・生徒の実態 , 収集した情報を自立活動の区分に即して整理 , いくつかの指導目標の中で優先する目標
                        final String field = ("1".equals(seq)) ? "CONTENT7" : ("8".equals(seq)) ? "CONTENT9" : "CONTENT8_" + (intSeq-1);
                        final int keta = ("1".equals(seq)) ? 90 : ("8".equals(seq)) ? 90 : 14;
                        final int gyo = ("1".equals(seq)) ? 7 : ("8".equals(seq)) ? 3 : 7;
                        VrsOutnRenban(svf, field, KNJ_EditEdit.get_token(outData._remark, keta, gyo));
                    } else if(intSeq <= 11) {
                        //SEQ 9～11
                        //選定された項目を関連付け具体的な指導内容を設定
                        final String field = ("9".equals(seq)) ? "CONTENT11_1" : ("10".equals(seq)) ? "CONTENT11_2" : "CONTENT11_3";
                        final int keta = 30;
                        final int gyo = 5;
                        VrsOutnRenban(svf, field, KNJ_EditEdit.get_token(outData._remark, keta, gyo));
                    }
                }
            }
        }

        //指導目標を達成するために必要な項目
        //タイトル
        final String[] subTitle = {"健康の保持","心理的な安定","人間関係の形成","環境の把握","身体の動き","コミュニケーション"};
        final String[][] subTitle2 = {{"①","②","③","④","⑤"},{"①","②","③"},{"①","②","③","④"},{"①","②","③","④","⑤"},{"①","②","③","④","⑤"},{"①","②","③","④","⑤"}};
        for (int i = 0 ; i < subTitle.length; i++) {
            //上段
            final String field = "SUB_TITLE10_"+(i+1);
            svf.VrAttribute(field, ITEM_PAINT);
            svf.VrsOut(field, subTitle[i]);
            //中段
            final String[] subTitle2_2 = subTitle2[i];
            for (int j = 0 ; j < subTitle2_2.length; j++) {
                final String field2 = "SUB_TITLE11_"+(i+1)+"_"+(j+1);
                svf.VrAttribute(field2, ITEM_PAINT);
                svf.VrsOut(field2, subTitle2_2[j]);
            }
        }
        //内容(下段)
        for (Iterator it = student._hreportGuidanceSchregSelfrelianceList.iterator(); it.hasNext();) {
            final HreportGuidanceSchregSelfrelianceDat outData = (HreportGuidanceSchregSelfrelianceDat) it.next();
            final String field = "CONTENT10_" + outData._self_div + "_" + outData._self_seq;
            svf.VrsOut(field, "○");
        }
        svf.VrEndPage();
        _hasData = true;
    }

    private void printPage3(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        svf.VrSetForm(PRINT_FRM3, 4);
        svf.VrAttribute("NAME", ITEM_PAINT);
        svf.VrsOut("NAME", "氏名 (" + student._name + ")");
        svf.VrsOut("TITLE", "目標"); //頁タイトル

        int pageLine = 1;

        //各教科等における1年間の目標
        if(_param._printBlock3) {
            int grp = 1;
            final String subclassDiv = "03";
            if(student._hreportGuidanceSchregSubclassRemarkMap.containsKey(subclassDiv)) {
                String title = "";
                if(_param._hreportGuidanceKindNameHDatMap.containsKey("03")) {
                    title = (String) _param._hreportGuidanceKindNameHDatMap.get("03");
                }
                svf.VrAttribute("TITLE1", ITEM_PAINT);
                svf.VrsOut("TITLE1", title); //表名称

                final Map subclassMap = (Map) student._hreportGuidanceSchregSubclassRemarkMap.get(subclassDiv);
                for (Iterator it = subclassMap.keySet().iterator(); it.hasNext();) {
                    final String subclasscd = (String) it.next();
                    final HreportGuidanceSchregSubclassRemark outDate = (HreportGuidanceSchregSubclassRemark) subclassMap.get(subclasscd);
                    List subclassName = knjobj.retDividString(outDate._subclassname, 2, OUTPUT_GYO_ALL);
                    List remak = knjobj.retDividString(outDate._remark9, 90, OUTPUT_GYO_ALL);
                    final int maxLen = subclassName.size() > remak.size() ? subclassName.size() : remak.size();

                    //改ページ
                    pageLine += maxLen;
                    if(pageLine > PAGE_MAX_LINE3) {
                        svf.VrEndPage();
                        svf.VrSetForm(PRINT_FRM3, 4);
                        pageLine = maxLen;
                        svf.VrAttribute("NAME", ITEM_PAINT);
                        svf.VrsOut("NAME", "氏名 (" + student._name + ")");
                        svf.VrsOut("TITLE", "目標"); //頁タイトル
                        svf.VrAttribute("TITLE1", ITEM_PAINT);
                        svf.VrsOut("TITLE1", title); //表名称
                        pageLine++;
                        grp++;
                    }

                    //出力
                    for (int i = 0 ; i < maxLen; i++) {
                        svf.VrAttribute("CLASS_TITLE", ITEM_PAINT);
                        svf.VrsOut("GRP2_1", String.valueOf(grp)); //グループ
                        svf.VrsOut("GRP2_2", String.valueOf(grp)); //グループ
                        svf.VrsOut("GRP2_3", String.valueOf(grp)); //グループ
                        if(i < subclassName.size()) svf.VrsOut("CLASS_TITLE", (String) subclassName.get(i));
                        if(i < remak.size()) svf.VrsOut("HOPE1", (String) remak.get(i));
                        svf.VrEndRecord();
                    }
                    grp++;
                }
            }

        }
        _hasData = true;
    }

    private void printPage5(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        svf.VrSetForm(PRINT_FRM5, 4);
        svf.VrAttribute("NAME", ITEM_PAINT);
        svf.VrsOut("NAME", "氏名 (" + student._name + ")");
        svf.VrsOut("TITLE", "次年度の目標"); //頁タイトル

        //次年度の目標
        int pageLine = 1;
        int grp = 1;
        final String div = "05";
        if(student._hreportGuidanceSchregSubclassRemarkMap.containsKey(div)) {
            String title = "";
            if(_param._hreportGuidanceKindNameHDatMap.containsKey("05")) {
                title = (String) _param._hreportGuidanceKindNameHDatMap.get("05");
            }
            svf.VrAttribute("TITLE1", ITEM_PAINT);
            svf.VrsOut("TITLE1", title); //表名称

            final Map subclassMap = (Map) student._hreportGuidanceSchregSubclassRemarkMap.get(div);
            for (Iterator it = subclassMap.keySet().iterator(); it.hasNext();) {
                final String subclasscd = (String) it.next();
                final HreportGuidanceSchregSubclassRemark outDate = (HreportGuidanceSchregSubclassRemark) subclassMap.get(subclasscd);
                List subclassName = knjobj.retDividString(outDate._subclassname, 2, OUTPUT_GYO_ALL);
                List remak = knjobj.retDividString(outDate._remark9, 90, OUTPUT_GYO_ALL);
                final int maxLen = subclassName.size() > remak.size() ? subclassName.size() : remak.size();

                //改ページ
                pageLine += maxLen;
                if(pageLine > PAGE_MAX_LINE5) {
                    svf.VrEndPage();
                    svf.VrSetForm(PRINT_FRM5, 4);
                    pageLine = maxLen;
                    svf.VrAttribute("NAME", ITEM_PAINT);
                    svf.VrsOut("NAME", "氏名 (" + student._name + ")");
                    svf.VrsOut("TITLE", "次年度の目標"); //頁タイトル
                    svf.VrAttribute("TITLE1", ITEM_PAINT);
                    svf.VrsOut("TITLE1", title); //表名称
                    pageLine++;
                    grp++;
                }

                //出力
                for (int i = 0 ; i < maxLen; i++) {
                    svf.VrAttribute("CLASS_TITLE", ITEM_PAINT);
                    svf.VrsOut("GRP2_1", String.valueOf(grp)); //グループ
                    svf.VrsOut("GRP2_2", String.valueOf(grp)); //グループ
                    svf.VrsOut("GRP2_3", String.valueOf(grp)); //グループ
                    if(i < subclassName.size()) svf.VrsOut("CLASS_TITLE", (String) subclassName.get(i));
                    if(i < remak.size()) svf.VrsOut("HOPE1", (String) remak.get(i));
                    svf.VrEndRecord();
                }
                grp++;
            }
        }
        _hasData = true;
    }

    protected void VrsOutnRenban(final Vrw32alp svf, final String field, final String[] value) {
        if (null != value) {
            for (int i = 0 ; i < value.length; i++) {
                svf.VrsOutn(field, i + 1, value[i]);
            }
        }
    }

    /**
     * 表示文字数幅分にセンタリングした文字列
     * @param str 元文字列
     * @param keta 表示文字数幅
     * @return センタリングした文字列
     */
    private static String centering(final String str, final int width) {
        if (null == str) {
            return StringUtils.repeat("　", width);
        }
        final String sps = StringUtils.repeat("　", (width - str.length()) / 2);
        final StringBuffer stb = new StringBuffer();
        stb.append(sps);
        for (int i = 0; i < str.length(); i++) {
            stb.append(str.charAt(i));
        }
        stb.append(StringUtils.repeat("　", width - stb.length()));
        return stb.toString();
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String studentSql = getStudentSql();
        log.debug(" sql =" + studentSql);

        try {
            ps = db2.prepareStatement(studentSql);
            rs = ps.executeQuery();

            while (rs.next()) {

                final String schregNo = StringUtils.defaultString(rs.getString("SCHREGNO"));
                final String schoolKind = StringUtils.defaultString(rs.getString("SCHOOL_KIND"));
                final String ghrCd = StringUtils.defaultString(rs.getString("GHR_CD"));
                final String grade = StringUtils.defaultString(rs.getString("GRADE"));
                final String gradeName = StringUtils.defaultString(rs.getString("GRADE_NAME2"));
                final String hrClass = StringUtils.defaultString(rs.getString("HR_CLASS"));
                final String gakubuName = StringUtils.defaultString(rs.getString("GAKUBU_NAME"));
                final String hrName = StringUtils.defaultString(rs.getString("HR_NAME"));
                final String ghrName = StringUtils.defaultString(rs.getString("GHR_NAME"));
                final String name = StringUtils.defaultString(rs.getString("NAME"));
                final String nameKana = StringUtils.defaultString(rs.getString("NAME_KANA"));
                final String birthDay = StringUtils.defaultString(rs.getString("BIRTHDAY"));
                final String sexName = StringUtils.defaultString(rs.getString("SEX_NAME"));
                final String createDate = StringUtils.defaultString(rs.getString("CREATEDATE"));
                final String createUser = StringUtils.defaultString(rs.getString("CREATEUSER"));

                final Student student = new Student(schregNo, schoolKind, ghrCd, grade, gradeName, hrClass, gakubuName, hrName,
                        ghrName, name, nameKana, birthDay, sexName, createDate, createUser);

                student.setAssessment(db2);
                student.setBlock8(db2);
                student.setChallengedSupportplanRecord(db2);
                student.setChallengedSupportplanMainDat(db2);
                student.setHreportGuidanceSchregRemarkDat(db2);
                student.setHreportGuidanceSchregSelfrelianceDat(db2);
                student.setHreportGuidanceSchregSubclassRemark(db2);
                student.setHreportGuidanceSchregSubclass(db2);
                retList.add(student);
            }

        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH MAX_RECORD AS ( ");
        stb.append(" SELECT ");
        stb.append("   YEAR, SEMESTER, SCHREGNO, DIV, MAX(RECORD_DATE) AS RECORD_DATE ");
        stb.append(" FROM  ");
        stb.append("   HREPORT_GUIDANCE_SCHREG_REMARK_DAT ");
        stb.append(" WHERE YEAR     = '" + _param._ctrlYear + "' ");
        stb.append("   AND SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
        stb.append(" GROUP BY  ");
        stb.append("   YEAR, SEMESTER, SCHREGNO, DIV ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     BASE.SCHREGNO, ");
        stb.append("     GDAT.SCHOOL_KIND, ");
        stb.append("     REGD.GRADE, ");
        stb.append("     GDAT.GRADE_NAME2, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGDH.HR_CLASS_NAME2 AS HR_NAME, ");
        stb.append("     GHR.GHR_CD, ");
        stb.append("     GHRH.GHR_NAMEABBV AS GHR_NAME, ");
        stb.append("     COURSE.COURSENAME AS GAKUBU_NAME, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.NAME_KANA, ");
        stb.append("     BASE.BIRTHDAY, ");
        stb.append("     Z002.NAME2 AS SEX_NAME, ");
//        stb.append("     CASE WHEN D091.NAME1 IS NULL THEN GUID_R1.REMARK ELSE D091.NAME1 END AS GUID_COURSE, ");
        stb.append("     GUID_R2.REMARK AS CREATEDATE, ");
        stb.append("     STF.STAFFNAME AS CREATEUSER ");
        stb.append(" FROM ");
        stb.append("     V_SCHREG_BASE_MST BASE ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD ON BASE.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON REGD.YEAR = GDAT.YEAR ");
        stb.append("                                  AND REGD.GRADE = GDAT.GRADE ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGD.YEAR = REGDH.YEAR ");
        stb.append("                                  AND REGD.SEMESTER = REGDH.SEMESTER  ");
        stb.append("                                  AND REGD.GRADE || REGD.HR_CLASS = REGDH.GRADE || REGDH.HR_CLASS  ");
        stb.append("     LEFT JOIN SCHREG_REGD_GHR_DAT GHR ON GHR.SCHREGNO = REGD.SCHREGNO ");
        stb.append("                                     AND GHR.YEAR =REGD.YEAR ");
        stb.append("                                     AND GHR.SEMESTER = REGD.SEMESTER ");
        stb.append("     LEFT JOIN SCHREG_REGD_GHR_HDAT GHRH ON GHRH.YEAR = GHR.YEAR ");
        stb.append("                                      AND GHRH.SEMESTER = GHR.SEMESTER ");
        stb.append("                                      AND GHRH.GHR_CD = GHR.GHR_CD ");
        stb.append("     LEFT JOIN MAX_RECORD GUID ON REGD.YEAR = GUID.YEAR ");
        stb.append("                                      AND GUID.SEMESTER = '9' ");
        stb.append("                                      AND REGD.SCHREGNO = GUID.SCHREGNO ");
        stb.append("                                      AND GUID.DIV      = '00' ");
//        stb.append("     LEFT JOIN HREPORT_GUIDANCE_SCHREG_REMARK_DAT GUID_R1 ON REGD.YEAR = GUID_R1.YEAR "); //障害種別コード
//        stb.append("                                      AND GUID_R1.SEMESTER    = GUID.SEMESTER ");
//        stb.append("                                      AND GUID_R1.RECORD_DATE = GUID.RECORD_DATE ");
//        stb.append("                                      AND GUID_R1.SCHREGNO    = GUID.SCHREGNO ");
//        stb.append("                                      AND GUID_R1.DIV         = GUID.DIV ");
//        stb.append("                                      AND GUID_R1.SEQ         = 1 ");
        stb.append("     LEFT JOIN HREPORT_GUIDANCE_SCHREG_REMARK_DAT GUID_R2 ON REGD.YEAR = GUID_R2.YEAR "); //作成日
        stb.append("                                      AND GUID_R2.SEMESTER    = GUID.SEMESTER ");
        stb.append("                                      AND GUID_R2.RECORD_DATE = GUID.RECORD_DATE ");
        stb.append("                                      AND GUID_R2.SCHREGNO    = GUID.SCHREGNO ");
        stb.append("                                      AND GUID_R2.DIV         = GUID.DIV ");
        stb.append("                                      AND GUID_R2.SEQ         = 1 ");
        stb.append("     LEFT JOIN HREPORT_GUIDANCE_SCHREG_REMARK_DAT GUID_R3 ON REGD.YEAR = GUID_R3.YEAR "); //作成者
        stb.append("                                      AND GUID_R3.SEMESTER    = GUID.SEMESTER ");
        stb.append("                                      AND GUID_R3.RECORD_DATE = GUID.RECORD_DATE ");
        stb.append("                                      AND GUID_R3.SCHREGNO    = GUID.SCHREGNO ");
        stb.append("                                      AND GUID_R3.DIV         = GUID.DIV ");
        stb.append("                                      AND GUID_R3.SEQ         = 2 ");
        stb.append("     LEFT JOIN STAFF_MST STF ON STF.STAFFCD = GUID_R3.REMARK ");
        stb.append("     LEFT JOIN COURSE_MST COURSE ON COURSE.COURSECD = REGD.COURSECD ");
        stb.append("     LEFT JOIN V_NAME_MST Z002 ON REGD.YEAR = Z002.YEAR ");
        stb.append("                            AND Z002.NAMECD1 = 'Z002' ");
        stb.append("                            AND BASE.SEX = Z002.NAMECD2 ");
        stb.append(" WHERE ");
        stb.append("     BASE.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
        stb.append("     AND REGD.YEAR     = '" + _param._ctrlYear + "' ");
        stb.append("     AND REGD.SEMESTER = '" + _param._semester + "' ");
        stb.append(" ORDER BY ");
        stb.append("     GHR.GHR_CD, ");
        stb.append("     GHR.GHR_ATTENDNO, ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGD.ATTENDNO ");

        return stb.toString();
    }

    private class Student {
        final String _schregNo;
        final String _schoolKind;
        final String _ghrCd;
        final String _grade;
        final String _gradeName;
        final String _hrClass;
        final String _gakubuName;
        final String _hrName;
        final String _ghrName;
        final String _name;
        final String _nameKana;
        final String _birthDay;
        final String _sexName;
        final String _createDate;
        final String _createUser;
        final Map _blockMap;
        final Map _challengedSupportplanRecordMap;
        Assessment _assessment = null;
        ChallengedSupportplanMainDat _challengedSupportplanMainDat = null;
        final Map _hreportGuidanceSchregRemarkMap;
        final List _hreportGuidanceSchregSelfrelianceList;
        final Map _hreportGuidanceSchregSubclassRemarkMap;
        final Map _hreportGuidanceSchregSubclassMap;


        public Student(final String schregNo, final String schoolKind, final String ghrCd, final String grade, final String gradeName,
                final String hrClass, final String gakubuName, final String hrName, final String ghrName,
                final String name, final String nameKana, final String birthDay, final String sexName,
                final String createDate, final String createUser) {
            _schregNo = schregNo;
            _schoolKind = schoolKind;
            _ghrCd = StringUtils.isEmpty(ghrCd) ? "00" : ghrCd;
            _grade = grade;
            _gradeName = gradeName;
            _hrClass = hrClass;
            _gakubuName = gakubuName;
            _hrName = hrName;
            _ghrName = ghrName;
            _name = name;
            _nameKana = nameKana;
            _birthDay = birthDay;
            _sexName = sexName;
            _createDate = createDate;
            _createUser = createUser;

            _blockMap = new HashMap();
            _challengedSupportplanRecordMap = new TreeMap();
            _hreportGuidanceSchregRemarkMap = new TreeMap();
            _hreportGuidanceSchregSelfrelianceList = new ArrayList();
            _hreportGuidanceSchregSubclassRemarkMap = new TreeMap();
            _hreportGuidanceSchregSubclassMap = new TreeMap();
        }

        private String getHrName() {
            if (HOUTEI.equals(_param._outputClass)) {
                return _gradeName + _hrName;
            } else {
                return _gradeName + _ghrName;
            }
        }

        private void setAssessment(final DB2UDB db2) {

            final String aPaternSql = getAssessmentSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(aPaternSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String challengedNames = StringUtils.defaultString(rs.getString("CHALLENGED_NAMES"));
                    final String challengedStatus = StringUtils.defaultString(rs.getString("CHALLENGED_STATUS"));
                    final String checkDate1 = StringUtils.defaultString(rs.getString("CHECK_DATE1"));
                    final String checkName1 = "".equals(StringUtils.defaultString(rs.getString("CHECK_NAME1"))) ? "" : StringUtils.replace(StringUtils.defaultString(rs.getString("CHECK_NAME1")),"\r\n", " ");
                    final String checkCenterText1 = StringUtils.defaultString(rs.getString("CHECK_CENTER_TEXT1"));
                    final String checkDate2 = StringUtils.defaultString(rs.getString("CHECK_DATE2"));
                    final String checkName2 = "".equals(StringUtils.defaultString(rs.getString("CHECK_NAME2"))) ? "" : StringUtils.replace(StringUtils.defaultString(rs.getString("CHECK_NAME2")), "\r\n", " ");
                    final String checkCenterText2 = StringUtils.defaultString(rs.getString("CHECK_CENTER_TEXT2"));
                    _assessment = new Assessment(challengedNames, challengedStatus, checkDate1, checkName1, checkCenterText1,
                            checkDate2, checkName2, checkCenterText2);
                }
            } catch (SQLException ex) {
                log.error("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }


        private String getAssessmentSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH MAX_DAY AS ( ");
            stb.append(" SELECT ");
            stb.append("     YEAR, ");
            stb.append("     SCHREGNO, ");
            stb.append("     MAX(RECORD_DATE) AS RECORD_DATE ");
            stb.append(" FROM ");
            stb.append("     SCHREG_CHALLENGED_ASSESSMENT_MAIN_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND SCHREGNO = '" + _schregNo + "' ");
            stb.append(" GROUP BY ");
            stb.append("     YEAR, ");
            stb.append("     SCHREGNO ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     ASSESS_MAIN.CHALLENGED_NAMES, ");
            stb.append("     ASSESS_MAIN.CHALLENGED_STATUS, ");
            stb.append("     ASSESS_CHECK1.CHECK_DATE AS CHECK_DATE1, ");
            stb.append("     ASSESS_CHECK1.CHECK_NAME AS CHECK_NAME1, ");
            stb.append("     ASSESS_CHECK1.CHECK_CENTER_TEXT AS CHECK_CENTER_TEXT1, ");
            stb.append("     ASSESS_CHECK2.CHECK_DATE AS CHECK_DATE2, ");
            stb.append("     ASSESS_CHECK2.CHECK_NAME AS CHECK_NAME2, ");
            stb.append("     ASSESS_CHECK2.CHECK_CENTER_TEXT AS CHECK_CENTER_TEXT2 ");
            stb.append(" FROM ");
            stb.append("     MAX_DAY, ");
            stb.append("     SCHREG_CHALLENGED_ASSESSMENT_MAIN_DAT ASSESS_MAIN ");
            stb.append("     LEFT JOIN SCHREG_CHALLENGED_ASSESSMENT_CHECK_RECORD_DAT ASSESS_CHECK1 ");
            stb.append("           ON ASSESS_MAIN.YEAR = ASSESS_CHECK1.YEAR ");
            stb.append("          AND ASSESS_MAIN.SCHREGNO = ASSESS_CHECK1.SCHREGNO ");
            stb.append("          AND ASSESS_MAIN.RECORD_DATE = ASSESS_CHECK1.RECORD_DATE ");
            stb.append("          AND ASSESS_CHECK1.RECORD_SEQ = 1 ");
            stb.append("     LEFT JOIN SCHREG_CHALLENGED_ASSESSMENT_CHECK_RECORD_DAT ASSESS_CHECK2 ");
            stb.append("           ON ASSESS_MAIN.YEAR = ASSESS_CHECK2.YEAR ");
            stb.append("          AND ASSESS_MAIN.SCHREGNO = ASSESS_CHECK2.SCHREGNO ");
            stb.append("          AND ASSESS_MAIN.RECORD_DATE = ASSESS_CHECK2.RECORD_DATE ");
            stb.append("          AND ASSESS_CHECK2.RECORD_SEQ = 2 ");
            stb.append(" WHERE ");
            stb.append("     MAX_DAY.YEAR = ASSESS_MAIN.YEAR ");
            stb.append("     AND MAX_DAY.SCHREGNO = ASSESS_MAIN.SCHREGNO ");
            stb.append("     AND MAX_DAY.RECORD_DATE = ASSESS_MAIN.RECORD_DATE ");

            return stb.toString();
        }

        private void setBlock8(final DB2UDB db2) {
            Block8 block8 = null;
            final String aPaternSql = getBlock8PaternSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(aPaternSql);
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String guidancePattern = StringUtils.defaultString(rs.getString("GUIDANCE_PATTERN"));
                    final String gakubuSchoolKind = StringUtils.defaultString(rs.getString("GAKUBU_SCHOOL_KIND"));
                    final String condition = StringUtils.defaultString(rs.getString("CONDITION"));
                    final String groupCd = StringUtils.defaultString(rs.getString("GROUPCD"));
                    final String ghrCd = StringUtils.defaultString(rs.getString("GHR_CD"));
                    final String grade = StringUtils.defaultString(rs.getString("GRADE"));
                    final String hrClass = StringUtils.defaultString(rs.getString("HR_CLASS"));
                    block8 = new Block8(guidancePattern, gakubuSchoolKind, condition, groupCd, ghrCd, grade, hrClass);
                }
            } catch (SQLException ex) {
                log.error("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            if (null != block8) {
                block8.setTitle(db2);
            } else {
                block8 = new Block8(null,null,null,null,null,null,null);
            }
            block8.setSubclassMap(db2, _schregNo, _grade, _hrClass);
            _blockMap.put("08", block8);
        }

        private String getBlock8PaternSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("     '3' AS GUIDANCE_PATTERN, ");
            stb.append("     T1.GAKUBU_SCHOOL_KIND, ");
            stb.append("     T1.CONDITION, ");
            stb.append("     T1.GROUPCD, ");
            stb.append("     T1.GHR_CD, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS ");
            stb.append(" FROM ");
            stb.append("     GRADE_KIND_SCHREG_GROUP_DAT T1 ");
            stb.append("     LEFT JOIN GRADE_KIND_COMP_GROUP_YMST L1 ON L1.YEAR = T1.YEAR ");
            stb.append("          AND T1.SEMESTER    = L1.SEMESTER ");
            stb.append("          AND T1.GAKUBU_SCHOOL_KIND = L1.GAKUBU_SCHOOL_KIND ");
            stb.append("          AND T1.GHR_CD      = L1.GHR_CD ");
            stb.append("          AND T1.GRADE       = L1.GRADE ");
            stb.append("          AND T1.HR_CLASS    = L1.HR_CLASS ");
            stb.append("          AND T1.CONDITION   = L1.CONDITION ");
            stb.append("          AND T1.GROUPCD     = L1.GROUPCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND T1.SEMESTER = '9' ");
            stb.append("     AND T1.SCHREGNO = '" + _schregNo + "' ");

            return stb.toString();
        }

        public void setChallengedSupportplanRecord(final DB2UDB db2) {
            final String sql = getChallengedSupportplanRecordSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String div = StringUtils.defaultString(rs.getString("DIV"));
                    final String hope1 = StringUtils.defaultString(rs.getString("HOPE1"));
                    final String hope2 = StringUtils.defaultString(rs.getString("HOPE2"));
                    final String goals = StringUtils.defaultString(rs.getString("GOALS"));
                    final ChallengedSupportplanRecordDat challengedSupportplanRecordDat = new ChallengedSupportplanRecordDat(div, hope1, hope2, goals);
                    _challengedSupportplanRecordMap.put(div, challengedSupportplanRecordDat);
                }
            } catch (SQLException ex) {
                log.error("setSubclassList exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getChallengedSupportplanRecordSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.DIV, ");
            stb.append("     T1.HOPE1, ");
            stb.append("     T1.HOPE2, ");
            stb.append("     T1.GOALS ");
            stb.append(" FROM ");
            stb.append("     SCHREG_CHALLENGED_SUPPORTPLAN_RECORD_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR     = '" + _param._ctrlYear + "' ");
            stb.append(" AND T1.SCHREGNO = '" + _schregNo + "' ");
            stb.append(" AND T1.RECORD_DATE = (SELECT MAX(T2.RECORD_DATE) FROM SCHREG_CHALLENGED_SUPPORTPLAN_RECORD_DAT T2 ");
            stb.append("                        WHERE T2.YEAR = T1.YEAR AND T2.SCHREGNO = T1.SCHREGNO AND T2.DIV = T1.DIV )");

            return stb.toString();
        }

        public void setChallengedSupportplanMainDat(final DB2UDB db2) {
            final String sql = getChallengedSupportplanMainDatSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String record_date = StringUtils.defaultString(rs.getString("RECORD_DATE"));
                    final String writing_date = StringUtils.defaultString(rs.getString("WRITING_DATE"));
                    final String ones_hope_present = StringUtils.defaultString(rs.getString("ONES_HOPE_PRESENT"));
                    final String guardian_hope_present = StringUtils.defaultString(rs.getString("GUARDIAN_HOPE_PRESENT"));
                    final String ones_hope_future = StringUtils.defaultString(rs.getString("ONES_HOPE_FUTURE"));
                    final String guardian_hope_future = StringUtils.defaultString(rs.getString("GUARDIAN_HOPE_FUTURE"));
                    final String reasonable_accommodation = StringUtils.defaultString(rs.getString("REASONABLE_ACCOMMODATION"));
                    final String selfreliance_goal = StringUtils.defaultString(rs.getString("SELFRELIANCE_GOAL"));
                    final String support_goal = StringUtils.defaultString(rs.getString("SUPPORT_GOAL"));
                    final String support_plan = StringUtils.defaultString(rs.getString("SUPPORT_PLAN"));
                    final String record = StringUtils.defaultString(rs.getString("RECORD"));
                    final String record_staffname = StringUtils.defaultString(rs.getString("RECORD_STAFFNAME"));
                    final String challenged_names = StringUtils.defaultString(rs.getString("CHALLENGED_NAMES"));
                    _challengedSupportplanMainDat = new ChallengedSupportplanMainDat(record_date, writing_date, ones_hope_present, guardian_hope_present, ones_hope_future, guardian_hope_future, reasonable_accommodation, selfreliance_goal, support_goal, support_plan, record, record_staffname,challenged_names);
                }
            } catch (SQLException ex) {
                log.error("setSubclassList exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getChallengedSupportplanMainDatSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   T1.RECORD_DATE, ");
            stb.append("   T1.WRITING_DATE, ");
            stb.append("   T1.ONES_HOPE_PRESENT, ");
            stb.append("   T1.GUARDIAN_HOPE_PRESENT, ");
            stb.append("   T1.ONES_HOPE_FUTURE, ");
            stb.append("   T1.GUARDIAN_HOPE_FUTURE, ");
            stb.append("   T1.REASONABLE_ACCOMMODATION, ");
            stb.append("   T1.SELFRELIANCE_GOAL, ");
            stb.append("   T1.SUPPORT_GOAL, ");
            stb.append("   T1.SUPPORT_PLAN, ");
            stb.append("   T1.RECORD, ");
            stb.append("   T1.RECORD_STAFFNAME, ");
            stb.append("   T1.CHALLENGED_NAMES ");
            stb.append(" FROM  ");
            stb.append("   SCHREG_CHALLENGED_SUPPORTPLAN_MAIN_DAT T1 ");
            stb.append(" WHERE T1.YEAR     = '" + _param._ctrlYear + "' ");
            stb.append("   AND T1.SCHREGNO = '" + _schregNo + "' ");
            stb.append("   AND T1.RECORD_DATE = (SELECT MAX(T2.RECORD_DATE) ");
            stb.append("                           FROM SCHREG_CHALLENGED_SUPPORTPLAN_MAIN_DAT T2 ");
            stb.append("                          WHERE T2.YEAR     = T1.YEAR  ");
            stb.append("                            AND T2.SCHREGNO = T1.SCHREGNO ) ");


            return stb.toString();
        }

        public void setHreportGuidanceSchregRemarkDat(final DB2UDB db2) {
            final String sql = setHreportGuidanceSchregRemarkDatSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = StringUtils.defaultString(rs.getString("SEMESTER"));
                    final String record_date = StringUtils.defaultString(rs.getString("RECORD_DATE"));
                    final String div = StringUtils.defaultString(rs.getString("DIV"));
                    final String seq = StringUtils.defaultString(rs.getString("SEQ"));
                    final String remark = StringUtils.defaultString(rs.getString("REMARK"));
                    final HreportGuidanceSchregRemarkDat hreportGuidanceSchregRemarkDat = new HreportGuidanceSchregRemarkDat(semester, record_date, div, seq, remark);
                    final Map divMap;
                    if(_hreportGuidanceSchregRemarkMap.containsKey(semester)) {
                        divMap = (Map) _hreportGuidanceSchregRemarkMap.get(semester);
                    } else {
                        divMap = new TreeMap();
                    }

                    final Map seqMap;
                    if(divMap.containsKey(div)) {
                        seqMap = (Map) divMap.get(div);
                    } else {
                        seqMap = new TreeMap();
                    }
                    seqMap.put(seq, hreportGuidanceSchregRemarkDat);
                    divMap.put(div, seqMap);
                    _hreportGuidanceSchregRemarkMap.put(semester, divMap); //SEMESTER ⇒ DIV ⇒ SEQ ⇒ HreportGuidanceSchregRemarkDat
                }
            } catch (SQLException ex) {
                log.error("setSubclassList exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String setHreportGuidanceSchregRemarkDatSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH MAX_RECORD AS ( ");
            stb.append(" SELECT  ");
            stb.append("   YEAR, SEMESTER, SCHREGNO, MAX(RECORD_DATE) AS RECORD_DATE ");
            stb.append(" FROM  ");
            stb.append("   HREPORT_GUIDANCE_SCHREG_REMARK_DAT ");
            stb.append(" WHERE YEAR     = '" + _param._ctrlYear + "' ");
            stb.append("   AND SCHREGNO = '" + _schregNo + "' ");
            stb.append(" GROUP BY  ");
            stb.append("   YEAR, SEMESTER, SCHREGNO ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("   T1.SEMESTER, ");
            stb.append("   T1.RECORD_DATE,  ");
            stb.append("   T1.DIV, ");
            stb.append("   T1.SEQ, ");
            stb.append("   T1.REMARK ");
            stb.append(" FROM ");
            stb.append("   HREPORT_GUIDANCE_SCHREG_REMARK_DAT T1 ");
            stb.append("   INNER JOIN MAX_RECORD T2 ");
            stb.append("           ON T2.YEAR        = T1.YEAR ");
            stb.append("          AND T2.SEMESTER    = T1.SEMESTER ");
            stb.append("          AND T2.RECORD_DATE = T1.RECORD_DATE ");
            stb.append("          AND T2.SCHREGNO    = T1.SCHREGNO ");
            stb.append(" ORDER BY ");
            stb.append("   T1.SEMESTER, ");
            stb.append("   T1.DIV, ");
            stb.append("   T1.SEQ ");
            return stb.toString();
        }

        private String getHreportGuidanceSchregRemark(final String semester, final String div, final String seq) {
            String rtnStr = "";
            if(_hreportGuidanceSchregRemarkMap.containsKey(semester)) {
                final Map divMap = (Map) _hreportGuidanceSchregRemarkMap.get(semester);
                if(divMap.containsKey(div)) {
                    final Map seqMap = (Map) divMap.get(div);
                    if(seqMap.containsKey(seq)) {
                        final HreportGuidanceSchregRemarkDat outDate = (HreportGuidanceSchregRemarkDat) seqMap.get(seq);
                        rtnStr = outDate._remark;
                    }
                }
            }
            return rtnStr;
        }

        public void setHreportGuidanceSchregSelfrelianceDat(final DB2UDB db2) {
            final String sql = setHreportGuidanceSchregSelfrelianceDatSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String self_target = StringUtils.defaultString(rs.getString("SELF_TARGET"));
                    final String self_div = StringUtils.defaultString(rs.getString("SELF_DIV"));
                    final String self_seq = StringUtils.defaultString(rs.getString("SELF_SEQ"));
                    final HreportGuidanceSchregSelfrelianceDat hreportGuidanceSchregSelfrelianceDat = new HreportGuidanceSchregSelfrelianceDat(self_target, self_div, self_seq);
                    _hreportGuidanceSchregSelfrelianceList.add(hreportGuidanceSchregSelfrelianceDat);
                }
            } catch (SQLException ex) {
                log.error("setSubclassList exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String setHreportGuidanceSchregSelfrelianceDatSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT  ");
            stb.append("   SELF_TARGET, ");
            stb.append("   SELF_DIV, ");
            stb.append("   SELF_SEQ ");
            stb.append(" FROM  ");
            stb.append("   HREPORT_GUIDANCE_SCHREG_SELFRELIANCE_DAT ");
            stb.append(" WHERE YEAR     = '" + _param._ctrlYear + "' ");
            stb.append("   AND SCHREGNO = '" + _schregNo + "' ");
            stb.append("   AND SELF_TARGET = '00' ");
            stb.append(" ORDER BY ");
            stb.append("   SELF_DIV, ");
            stb.append("   SELF_SEQ ");

            return stb.toString();
        }


        public void setHreportGuidanceSchregSubclassRemark(final DB2UDB db2) {
            final String sql = setHreportGuidanceSchregSubclassRemarkSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String div = StringUtils.defaultString(rs.getString("DIV"));
                    final String subclasscd = StringUtils.defaultString(rs.getString("SUBCLASSCD"));
                    final String subclassname = StringUtils.defaultString(rs.getString("SUBCLASSNAME"));
                    final String remark1 = StringUtils.defaultString(rs.getString("REMARK1"));
                    final String remark2 = StringUtils.defaultString(rs.getString("REMARK2"));
                    final String remark9 = StringUtils.defaultString(rs.getString("REMARK9"));
                    final HreportGuidanceSchregSubclassRemark hreportGuidanceSchregSubclassRemark = new HreportGuidanceSchregSubclassRemark(div, subclasscd, subclassname, remark1, remark2, remark9);
                    final Map map;
                    if(_hreportGuidanceSchregSubclassRemarkMap.containsKey(div)) {
                        map = (Map) _hreportGuidanceSchregSubclassRemarkMap.get(div);
                    } else {
                        map = new TreeMap();
                    }
                    map.put(subclasscd, hreportGuidanceSchregSubclassRemark);
                    _hreportGuidanceSchregSubclassRemarkMap.put(div, map);

                }
            } catch (SQLException ex) {
                log.error("setSubclassList exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String setHreportGuidanceSchregSubclassRemarkSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH MAX_RECORD AS( ");
            stb.append("   SELECT YEAR, SEMESTER, SCHREGNO, MAX(RECORD_DATE) AS RECORD_DATE ");
            stb.append("   FROM HREPORT_GUIDANCE_SCHREG_SUBCLASS_REMARK_DAT ");
            stb.append("   WHERE YEAR     = '" + _param._ctrlYear + "' ");
            stb.append("     AND SCHREGNO = '" + _schregNo + "' ");
            stb.append("   GROUP BY YEAR, SEMESTER, SCHREGNO ");
            stb.append(" ), EXIST_SUBCLASS AS( ");
            stb.append(" SELECT ");
            stb.append("   COMP.YEAR ");
            stb.append("   , COMP.SEMESTER ");
            stb.append("   , COMP.CLASSCD ");
            stb.append("   , COMP.SCHOOL_KIND ");
            stb.append("   , COMP.CURRICULUM_CD ");
            stb.append("   , COMP.SUBCLASSCD ");
            stb.append("   , REMARK.DIV ");
            stb.append(" FROM ");
            stb.append("   GRADE_KIND_SCHREG_GROUP_DAT SCH ");
            stb.append("   INNER JOIN MAX_RECORD T2 ");
            stb.append("           ON T2.YEAR       = SCH.YEAR ");
            stb.append("          AND T2.SEMESTER   = SCH.SEMESTER ");
            stb.append("          AND T2.SCHREGNO   = SCH.SCHREGNO ");
            stb.append("   INNER JOIN GRADE_KIND_COMP_GROUP_DAT COMP ");
            stb.append("     ON SCH.YEAR = COMP.YEAR ");
            stb.append("     AND SCH.SEMESTER = COMP.SEMESTER ");
            stb.append("     AND SCH.GAKUBU_SCHOOL_KIND = COMP.GAKUBU_SCHOOL_KIND ");
            stb.append("     AND SCH.GHR_CD = COMP.GHR_CD ");
            stb.append("     AND SCH.GRADE = COMP.GRADE ");
            stb.append("     AND SCH.HR_CLASS = COMP.HR_CLASS ");
            stb.append("     AND SCH.CONDITION = COMP.CONDITION ");
            stb.append("     AND SCH.GROUPCD = COMP.GROUPCD ");
            stb.append("   INNER JOIN V_SUBCLASS_MST SUB ");
            stb.append("     ON SCH.YEAR = SUB.YEAR ");
            stb.append("     AND COMP.CLASSCD = SUB.CLASSCD ");
            stb.append("     AND COMP.SCHOOL_KIND = SUB.SCHOOL_KIND ");
            stb.append("     AND COMP.CURRICULUM_CD = SUB.CURRICULUM_CD ");
            stb.append("     AND COMP.SUBCLASSCD = SUB.SUBCLASSCD ");
            stb.append("   LEFT JOIN HREPORT_GUIDANCE_SCHREG_SUBCLASS_REMARK_DAT REMARK ");
            stb.append("     ON SCH.YEAR = REMARK.YEAR ");
            stb.append("     AND SCH.SEMESTER = REMARK.SEMESTER ");
            stb.append("     AND REMARK.RECORD_DATE= T2.RECORD_DATE ");
            stb.append("     AND SCH.SCHREGNO = REMARK.SCHREGNO ");
            stb.append("     AND COMP.CLASSCD = REMARK.CLASSCD ");
            stb.append("     AND COMP.SCHOOL_KIND = REMARK.SCHOOL_KIND ");
            stb.append("     AND COMP.CURRICULUM_CD = REMARK.CURRICULUM_CD ");
            stb.append("     AND COMP.SUBCLASSCD = REMARK.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("   SCH.YEAR     = '" + _param._ctrlYear + "' ");
            stb.append("   AND SCH.SEMESTER = '9' ");
            stb.append("   AND SCH.SCHREGNO = '" + _schregNo + "' ");
            stb.append(" ), T_MAIN AS( ");
            stb.append("   SELECT DISTINCT ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.DIV, ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     SEME1.REMARK AS REMARK1, ");
            stb.append("     SEME2.REMARK AS REMARK2, ");
            stb.append("     SEME9.REMARK AS REMARK9 ");
            stb.append("   FROM  ");
            stb.append("     HREPORT_GUIDANCE_SCHREG_SUBCLASS_REMARK_DAT T1 ");
            stb.append("     INNER JOIN MAX_RECORD T2 ");
            stb.append("       ON T2.YEAR       = T1.YEAR ");
            stb.append("      AND T2.SEMESTER   = T1.SEMESTER ");
            stb.append("      AND T2.SCHREGNO   = T1.SCHREGNO ");
            stb.append("      AND T2.RECORD_DATE= T1.RECORD_DATE ");
            stb.append("     LEFT JOIN HREPORT_GUIDANCE_SCHREG_SUBCLASS_REMARK_DAT SEME1 ");
            stb.append("       ON SEME1.YEAR          = T1.YEAR ");
            stb.append("      AND SEME1.SEMESTER      = '1' ");
            stb.append("      AND SEME1.SCHREGNO      = T1.SCHREGNO ");
            stb.append("      AND SEME1.RECORD_DATE   = T1.RECORD_DATE ");
            stb.append("      AND SEME1.DIV           = T1.DIV ");
            stb.append("      AND SEME1.CLASSCD       = T1.CLASSCD  ");
            stb.append("      AND SEME1.SCHOOL_KIND   = T1.SCHOOL_KIND  ");
            stb.append("      AND SEME1.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("      AND SEME1.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("     LEFT JOIN HREPORT_GUIDANCE_SCHREG_SUBCLASS_REMARK_DAT SEME2 ");
            stb.append("       ON SEME2.YEAR          = T1.YEAR ");
            stb.append("      AND SEME2.SEMESTER      = '2' ");
            stb.append("      AND SEME2.SCHREGNO      = T1.SCHREGNO ");
            stb.append("      AND SEME2.RECORD_DATE   = T1.RECORD_DATE ");
            stb.append("      AND SEME2.DIV           = T1.DIV ");
            stb.append("      AND SEME2.CLASSCD       = T1.CLASSCD  ");
            stb.append("      AND SEME2.SCHOOL_KIND   = T1.SCHOOL_KIND  ");
            stb.append("      AND SEME2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("      AND SEME2.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("     LEFT JOIN HREPORT_GUIDANCE_SCHREG_SUBCLASS_REMARK_DAT SEME9 ");
            stb.append("       ON SEME9.YEAR          = T1.YEAR ");
            stb.append("      AND SEME9.SEMESTER      = '9' ");
            stb.append("      AND SEME9.SCHREGNO      = T1.SCHREGNO ");
            stb.append("      AND SEME9.RECORD_DATE   = T1.RECORD_DATE ");
            stb.append("      AND SEME9.DIV           = T1.DIV ");
            stb.append("      AND SEME9.CLASSCD       = T1.CLASSCD  ");
            stb.append("      AND SEME9.SCHOOL_KIND   = T1.SCHOOL_KIND  ");
            stb.append("      AND SEME9.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("      AND SEME9.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     EXISTS ( ");
            stb.append("         SELECT ");
            stb.append("             'x' ");
            stb.append("         FROM ");
            stb.append("             EXIST_SUBCLASS ES ");
            stb.append("         WHERE ");
            stb.append("                 T1.YEAR = ES.YEAR ");
            stb.append("             AND T1.DIV = ES.DIV ");
            stb.append("             AND T1.CLASSCD = ES.CLASSCD ");
            stb.append("             AND T1.SCHOOL_KIND = ES.SCHOOL_KIND ");
            stb.append("             AND T1.CURRICULUM_CD = ES.CURRICULUM_CD ");
            stb.append("             AND T1.SUBCLASSCD = ES.SUBCLASSCD ");
            stb.append("     ) ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("   T1.DIV, ");
            stb.append("   T1.CLASSCD ||'-'|| T1.SCHOOL_KIND ||'-'|| T1.CURRICULUM_CD ||'-'|| T1.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("   T3.SUBCLASSNAME, ");
            stb.append("   T1.REMARK1, ");
            stb.append("   T1.REMARK2, ");
            stb.append("   T1.REMARK9 ");
            stb.append(" FROM ");
            stb.append("   T_MAIN T1 ");
            stb.append("   INNER JOIN V_SUBCLASS_MST T3 ");
            stb.append("     ON T3.YEAR = T1.YEAR ");
            stb.append("    AND T3.CLASSCD       = T1.CLASSCD  ");
            stb.append("    AND T3.SCHOOL_KIND   = T1.SCHOOL_KIND  ");
            stb.append("    AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("    AND T3.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append(" ORDER BY ");
            stb.append("   DIV, ");
            stb.append("   VALUE(T3.SHOWORDER3, '999'), ");
            stb.append("   SUBCLASSCD ");
            return stb.toString();
        }


        public void setHreportGuidanceSchregSubclass(final DB2UDB db2) {
            final String sql = setHreportGuidanceSchregSubclassSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String year = StringUtils.defaultString(rs.getString("YEAR"));
                    final String semester = StringUtils.defaultString(rs.getString("SEMESTER"));
                    final String schregno = StringUtils.defaultString(rs.getString("SCHREGNO"));
                    final String subclasscd = StringUtils.defaultString(rs.getString("SUBCLASSCD"));
                    final String subclassname = StringUtils.defaultString(rs.getString("SUBCLASSNAME"));
                    final String unitcd = StringUtils.defaultString(rs.getString("UNITCD"));
                    final String guidance_pattern = StringUtils.defaultString(rs.getString("GUIDANCE_PATTERN"));
                    final String remark1 = StringUtils.defaultString(rs.getString("REMARK1"));
                    final String remark2 = StringUtils.defaultString(rs.getString("REMARK2"));
                    final String remark3 = StringUtils.defaultString(rs.getString("REMARK3"));
                    final HreportGuidanceSchregSubclass hreportGuidanceSchregSubclass = new HreportGuidanceSchregSubclass(year, semester, schregno, subclasscd, subclassname, unitcd, guidance_pattern, remark1, remark2, remark3);

                    final Map map;
                    if(_hreportGuidanceSchregSubclassMap.containsKey(subclasscd)) {
                        map = (Map) _hreportGuidanceSchregSubclassMap.get(subclasscd);
                    } else {
                        map = new TreeMap();
                    }
                    map.put(semester, hreportGuidanceSchregSubclass);
                    _hreportGuidanceSchregSubclassMap.put(subclasscd, map);

                }
            } catch (SQLException ex) {
                log.error("setSubclassList exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String setHreportGuidanceSchregSubclassSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH MAIN AS( ");
            stb.append("   SELECT DISTINCT ");
            stb.append("     YEAR, ");
            stb.append("     SEMESTER, ");
            stb.append("     SCHREGNO, ");
            stb.append("     CLASSCD, ");
            stb.append("     SCHOOL_KIND, ");
            stb.append("     CURRICULUM_CD, ");
            stb.append("     SUBCLASSCD, ");
            stb.append("     UNITCD, ");
            stb.append("     GUIDANCE_PATTERN ");
            stb.append("   FROM  ");
            stb.append("     HREPORT_GUIDANCE_SCHREG_SUBCLASS_DAT ");
            stb.append("   WHERE YEAR     = '" + _param._ctrlYear + "' ");
            stb.append("     AND SCHREGNO = '" + _schregNo + "' ");
            stb.append("     AND GUIDANCE_PATTERN = '3' ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("   T1.YEAR, ");
            stb.append("   T1.SEMESTER, ");
            stb.append("   T1.SCHREGNO, ");
            stb.append("   T1.CLASSCD ||'-'|| T1.SCHOOL_KIND ||'-'|| T1.CURRICULUM_CD ||'-'|| T1.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("   T5.SUBCLASSNAME, ");
            stb.append("   T1.UNITCD, ");
            stb.append("   T1.GUIDANCE_PATTERN, ");
            stb.append("   T2.REMARK AS REMARK1, ");
            stb.append("   T3.REMARK AS REMARK2, ");
            stb.append("   T4.REMARK AS REMARK3 ");
            stb.append(" FROM  ");
            stb.append("   MAIN T1 ");
            stb.append("   LEFT JOIN HREPORT_GUIDANCE_SCHREG_SUBCLASS_DAT T2 ");
            stb.append("          ON T2.YEAR             = T1.YEAR ");
            stb.append("         AND T2.SEMESTER         = T1.SEMESTER ");
            stb.append("         AND T2.SCHREGNO         = T1.SCHREGNO ");
            stb.append("         AND T2.CLASSCD          = T1.CLASSCD ");
            stb.append("         AND T2.SCHOOL_KIND      = T1.SCHOOL_KIND ");
            stb.append("         AND T2.CURRICULUM_CD    = T1.CURRICULUM_CD ");
            stb.append("         AND T2.SUBCLASSCD       = T1.SUBCLASSCD ");
            stb.append("         AND T2.GUIDANCE_PATTERN = T1.GUIDANCE_PATTERN ");
            stb.append("         AND T2.UNITCD           = T1.UNITCD ");
            stb.append("         AND T2.SEQ              = '1' ");
            stb.append("   LEFT JOIN HREPORT_GUIDANCE_SCHREG_SUBCLASS_DAT T3 ");
            stb.append("          ON T3.YEAR             = T1.YEAR ");
            stb.append("         AND T3.SEMESTER         = T1.SEMESTER ");
            stb.append("         AND T3.SCHREGNO         = T1.SCHREGNO ");
            stb.append("         AND T3.CLASSCD          = T1.CLASSCD ");
            stb.append("         AND T3.SCHOOL_KIND      = T1.SCHOOL_KIND ");
            stb.append("         AND T3.CURRICULUM_CD    = T1.CURRICULUM_CD ");
            stb.append("         AND T3.SUBCLASSCD       = T1.SUBCLASSCD ");
            stb.append("         AND T3.GUIDANCE_PATTERN = T1.GUIDANCE_PATTERN ");
            stb.append("         AND T3.UNITCD           = T1.UNITCD ");
            stb.append("         AND T3.SEQ              = '2' ");
            stb.append("   LEFT JOIN HREPORT_GUIDANCE_SCHREG_SUBCLASS_DAT T4 ");
            stb.append("          ON T4.YEAR             = T1.YEAR ");
            stb.append("         AND T4.SEMESTER         = T1.SEMESTER ");
            stb.append("         AND T4.SCHREGNO         = T1.SCHREGNO ");
            stb.append("         AND T4.CLASSCD          = T1.CLASSCD ");
            stb.append("         AND T4.SCHOOL_KIND      = T1.SCHOOL_KIND ");
            stb.append("         AND T4.CURRICULUM_CD    = T1.CURRICULUM_CD ");
            stb.append("         AND T4.SUBCLASSCD       = T1.SUBCLASSCD ");
            stb.append("         AND T4.GUIDANCE_PATTERN = T1.GUIDANCE_PATTERN ");
            stb.append("         AND T4.UNITCD           = T1.UNITCD ");
            stb.append("         AND T4.SEQ              = '3' ");
            stb.append("   LEFT JOIN SUBCLASS_MST T5 ");
            stb.append("          ON T5.CLASSCD          = T1.CLASSCD ");
            stb.append("         AND T5.SCHOOL_KIND      = T1.SCHOOL_KIND ");
            stb.append("         AND T5.CURRICULUM_CD    = T1.CURRICULUM_CD ");
            stb.append("         AND T5.SUBCLASSCD       = T1.SUBCLASSCD ");
            stb.append(" ORDER BY ");
            stb.append("   T5.SHOWORDER3, ");
            stb.append("   SUBCLASSCD, ");
            stb.append("   SEMESTER ");

            return stb.toString();
        }


    }

    abstract private class Block {
        protected boolean _pageChange;
        protected String _kindNo;
        protected String _formId;
        protected int _maxLen;
        protected int _maxLine;
        protected int _pageMaxLine;

        abstract void printOut(final Vrw32alp svf, final Student student);

        /** 改ページブロックなら改ページ */
        final void pageChange(final Vrw32alp svf) {
            if (_pageChange) {
                svf.VrSetForm(_formId, 4);
                _lineCnt = 0;
            }
        }

        /** チェックして改ページ */
        final void checkLineAndPageChangeBlock8(final Vrw32alp svf, final int cnt) {
            if (_lineCnt + cnt > _pageMaxLine) {
                svf.VrEndPage();
                svf.VrSetForm(_formId, 4);
                _lineCnt = 0;
            }
        }
    }

    private class Assessment {
        final String _challengedNames;
        final String _challengedStatus;
        final String _checkDate1;
        final String _checkName1;
        final String _checkCenterText1;
        final String _checkDate2;
        final String _checkName2;
        final String _checkCenterText2;

        public Assessment(final String challengedNames, final String challengedStatus, final String checkDate1,
                final String checkName1, final String checkCenterText1, final String checkDate2,
                final String checkName2, final String checkCenterText2) {
            _challengedNames = StringUtils.defaultString(challengedNames);
            _challengedStatus = StringUtils.defaultString(challengedStatus);
            _checkDate1 = StringUtils.defaultString(checkDate1);
            _checkName1 = StringUtils.defaultString(checkName1);
            _checkCenterText1 = StringUtils.defaultString(checkCenterText1);
            _checkDate2 = StringUtils.defaultString(checkDate2);
            _checkName2 = StringUtils.defaultString(checkName2);
            _checkCenterText2 = StringUtils.defaultString(checkCenterText2);
        }
    }

    private class Block8 extends Block {
        final String _guidancePattern;
        final String _gakubuSchoolKind;
        final String _condition;
        final String _groupCd;
        final String _ghrCd;
        String _itemRemark1;
        String _itemRemark2;
        String _itemRemark3;
        String _itemRemark4;
        String _itemRemark5;
        Map _subclassMap;
        final String _grade;
        final String _hrClass;

        public Block8(final String guidancePattern,
                final String gakubuSchoolKind, final String condition, final String groupCd,
                final String ghrCd, final String grade, final String hrClass) {
            _guidancePattern = guidancePattern;
            _gakubuSchoolKind = gakubuSchoolKind;
            _condition = condition;
            _groupCd = groupCd;
            _ghrCd = ghrCd;
            _subclassMap = new LinkedMap();
            _pageChange = true;
            _kindNo = "08";
            _formId = PRINT_FRM4;
            _pageMaxLine = PAGE_MAX_LINE4;
            _grade = grade;
            _hrClass = hrClass;
        }

        public void setTitle(final DB2UDB db2) {
            final String titleSql = getBlock8PaternSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(titleSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _itemRemark1 = StringUtils.defaultString(rs.getString("ITEM_REMARK1"));
                    _itemRemark2 = StringUtils.defaultString(rs.getString("ITEM_REMARK2"));
                    _itemRemark3 = StringUtils.defaultString(rs.getString("ITEM_REMARK3"));
                    _itemRemark4 = StringUtils.defaultString(rs.getString("ITEM_REMARK4"));
                    _itemRemark5 = StringUtils.defaultString(rs.getString("ITEM_REMARK5"));
                }
            } catch (SQLException ex) {
                log.error("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getBlock8PaternSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("     GUID_ITEM.* ");
            stb.append(" FROM ");
            stb.append("     HREPORT_GUIDANCE_ITEM_NAME_DAT GUID_ITEM ");
            stb.append(" WHERE ");
            stb.append("     GUID_ITEM.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND GUID_ITEM.SEMESTER = '9' ");
            stb.append("     AND GUID_ITEM.GUIDANCE_PATTERN = '" + _guidancePattern + "' ");
            stb.append("     AND GUID_ITEM.GAKUBU_SCHOOL_KIND = '" + _gakubuSchoolKind + "' ");
            stb.append("     AND GUID_ITEM.CONDITION = '" + _condition + "' ");

            return stb.toString();
        }

        public void setSubclassMap(final DB2UDB db2, final String schregNo, final String grade,
                final String hrClass) {
            final String subclassSql = getBlock8SubclassSql(schregNo, grade, hrClass);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(subclassSql);
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String semester = StringUtils.defaultString(rs.getString("SEMESTER"));
                    final String classCd = StringUtils.defaultString(rs.getString("CLASSCD"));
                    final String schoolKind = StringUtils.defaultString(rs.getString("SCHOOL_KIND"));
                    final String curriculumCd = StringUtils.defaultString(rs.getString("CURRICULUM_CD"));
                    final String subclassCd = StringUtils.defaultString(rs.getString("SUBCLASSCD"));
                    final String subclassName = StringUtils.defaultString(rs.getString("SUBCLASSNAME"));
                    final String unitCd = StringUtils.defaultString(rs.getString("UNITCD"));
                    final String unitName = StringUtils.defaultString(rs.getString("UNITNAME"));
                    final String guidancePattern = StringUtils.defaultString(rs.getString("GUIDANCE_PATTERN"));
                    final String seq = StringUtils.defaultString(rs.getString("SEQ"));
                    final String remark = StringUtils.defaultString(rs.getString("REMARK"));
                    final Subclass subclass;
                    final String key = classCd + schoolKind + curriculumCd + subclassCd;
                    if (_subclassMap.containsKey(key)) {
                        subclass = (Subclass) _subclassMap.get(key);
                    } else {
                        subclass = new Subclass(classCd, schoolKind, curriculumCd, subclassCd, subclassName, unitCd);
                    }
                    subclass.setSemeUnitMap(semester, unitCd, unitName, guidancePattern, seq, remark);
                    _subclassMap.put(key, subclass);
                }
            } catch (SQLException ex) {
                log.error("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getBlock8SubclassSql(final String schregNo, final String grade,
                final String hrClass) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     GUID_SUB.CLASSCD, ");
            stb.append("     GUID_SUB.SCHOOL_KIND, ");
            stb.append("     GUID_SUB.CURRICULUM_CD, ");
            stb.append("     GUID_SUB.SUBCLASSCD, ");
            stb.append("     GUID_SUB.SEMESTER, ");
            stb.append("     GUID_SUB.UNITCD, ");
            stb.append("     UNIT_M.UNITNAME, ");
            stb.append("     GUID_SUB.GUIDANCE_PATTERN, ");
            stb.append("     GUID_SUB.SEQ, ");
            stb.append("     GUID_SUB.REMARK, ");
            stb.append("     SUBM.SUBCLASSNAME ");
            stb.append(" FROM ");
            stb.append("     HREPORT_GUIDANCE_SCHREG_SUBCLASS_DAT GUID_SUB ");
            stb.append("     LEFT JOIN SUBCLASS_MST SUBM ON GUID_SUB.CLASSCD = SUBM.CLASSCD ");
            stb.append("          AND GUID_SUB.SCHOOL_KIND = SUBM.SCHOOL_KIND ");
            stb.append("          AND GUID_SUB.CURRICULUM_CD = SUBM.CURRICULUM_CD ");
            stb.append("          AND GUID_SUB.SUBCLASSCD = SUBM.SUBCLASSCD ");
            stb.append("     LEFT JOIN GRADE_KIND_UNIT_GROUP_YMST UNIT_M ON GUID_SUB.YEAR = UNIT_M.YEAR ");
            stb.append("          AND UNIT_M.SEMESTER = '9' ");
            stb.append("          AND UNIT_M.GAKUBU_SCHOOL_KIND = '" + _gakubuSchoolKind + "' ");
            stb.append("          AND UNIT_M.GHR_CD = '" + _ghrCd + "' ");
            stb.append("          AND UNIT_M.GRADE = '" + _grade + "' ");
            stb.append("          AND UNIT_M.HR_CLASS = '" + _hrClass + "' ");
            stb.append("          AND UNIT_M.CONDITION = '" + _condition + "' ");
            stb.append("          AND UNIT_M.GROUPCD = '" + _groupCd + "' ");
            stb.append("          AND GUID_SUB.UNITCD = UNIT_M.UNITCD ");
            stb.append("          AND GUID_SUB.CLASSCD = UNIT_M.CLASSCD ");
            stb.append("          AND GUID_SUB.SCHOOL_KIND = UNIT_M.SCHOOL_KIND ");
            stb.append("          AND GUID_SUB.CURRICULUM_CD = UNIT_M.CURRICULUM_CD ");
            stb.append("          AND GUID_SUB.SUBCLASSCD = UNIT_M.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     GUID_SUB.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND GUID_SUB.SCHREGNO = '" + schregNo + "' ");
//            stb.append("     AND GUID_SUB.GUIDANCE_PATTERN = '" + _guidancePattern + "' ");
            stb.append("     AND GUID_SUB.GUIDANCE_PATTERN = '3' ");
            stb.append(" ORDER BY ");
            stb.append("     GUID_SUB.CLASSCD, ");
            stb.append("     GUID_SUB.SCHOOL_KIND, ");
            stb.append("     GUID_SUB.CURRICULUM_CD, ");
            stb.append("     GUID_SUB.SUBCLASSCD, ");
            stb.append("     GUID_SUB.SEMESTER, ");
            stb.append("     GUID_SUB.UNITCD, ");
            stb.append("     GUID_SUB.SEQ ");
            return stb.toString();
        }

        void printOut(final Vrw32alp svf, final Student student) {
            //各教科毎の記録
            printOutB(svf, student);
        }

        public void printOutB(final Vrw32alp svf, final Student student) {
            pageChange(svf);

            //重点目標
            if (_param._printBlock2) {

                String blockTitle = "各教科等の記録";
                if(_param._hreportGuidanceKindNameHDatMap.containsKey("04")) {
                    blockTitle = (String) _param._hreportGuidanceKindNameHDatMap.get("04");
                }
                svf.VrsOut("TITLE", blockTitle); //頁タイトル

                final Map divMap;
                if (student._hreportGuidanceSchregRemarkMap.containsKey(SEMEALL)) {
                    divMap = (Map) student._hreportGuidanceSchregRemarkMap.get(SEMEALL);
                } else {
                    divMap = null;
                }

                final String kindNo = "02";
                final String div = "02";
                int grp = 1;
                if (_param._hreportGuidanceKindNameDatMap.containsKey(kindNo)) {
                    String title = "";
                    if (_param._hreportGuidanceKindNameHDatMap.containsKey("02")) {
                        title = (String) _param._hreportGuidanceKindNameHDatMap.get("02");
                    }
                    svf.VrAttribute("TITLE1", ITEM_PAINT);
                    svf.VrsOut("TITLE1", title); //表名称

                    final Map kindNameMap = (Map) _param._hreportGuidanceKindNameDatMap.get(kindNo);
                    for (Iterator it = kindNameMap.keySet().iterator(); it.hasNext();) {
                        final String kindSeq = (String) it.next();
                        if ("000".equals(kindSeq)) {
                            continue;
                        }
                        if (Integer.parseInt(kindSeq) > 6) {
                            break;
                        }
                        final Map map = (Map) kindNameMap.get(kindSeq);

                        String remark = "";
                        if (divMap != null) {
                            //内容
                            if (divMap.containsKey(div)) {
                                final String seq = String.valueOf(Integer.parseInt(kindSeq));
                                final Map seqMap = (Map) divMap.get(div);
                                final HreportGuidanceSchregRemarkDat outData = (HreportGuidanceSchregRemarkDat) seqMap
                                        .get(seq);
                                remark = outData._remark;
                            }
                        }

                        final List kindRemark = knjobj.retDividString((String) map.get("KIND_REMARK"), 10, 3);
                        final List content = knjobj.retDividString(remark, 80, OUTPUT_GYO_ALL);
                        final int maxLen = content.size() > kindRemark.size() ? content.size() : kindRemark.size();

                        //改ページ
                        _lineCnt += maxLen;
                        if (_lineCnt > PAGE_MAX_LINE4) {
                            svf.VrEndPage();
                            svf.VrSetForm(PRINT_FRM4, 4);
                            _lineCnt = maxLen;
                            svf.VrAttribute("TITLE1", ITEM_PAINT);
                            svf.VrsOut("TITLE1", title); //表名称
                            _lineCnt++;
                            grp++;
                        }

                        //出力
                        for (int i = 0; i < maxLen; i++) {
                            svf.VrAttribute("DIV4", ITEM_PAINT);
                            svf.VrsOut("GRP4_1", String.valueOf(grp));
                            svf.VrsOut("GRP4_2", String.valueOf(grp));

                            if (i < kindRemark.size()) {
                                svf.VrsOut("DIV4", (String) kindRemark.get(i));
                            }
                            if (i < content.size()) {
                                svf.VrsOut("CONTENT4", (String) content.get(i));
                            }

                            svf.VrEndRecord();
                        }
                        grp++;
                    }
                }
            }

            //各教科等の記録
            if (_param._printBlock4) {
                setHeadB(svf, student);
                int grp1 = 1;
                int grp2 = 1;
                int grp3 = 1;
                int grp4 = 1;
                final Map printSubclass = new HashMap();
                //科目
                for (Iterator itRemark = _subclassMap.keySet().iterator(); itRemark.hasNext();) {
                    final String subclassCd = (String) itRemark.next();
                    final Subclass subclass = (Subclass) _subclassMap.get(subclassCd);
                    final int subclassLen = 2;
                    final List setSubclassList = KNJ_EditKinsoku.getTokenList(subclass._subclassName, subclassLen);

                    final Map printSemester = new HashMap();
                    //学期
                    for (Iterator itSemeUnit = subclass._semeUnitMap.keySet().iterator(); itSemeUnit.hasNext();) {
                        final String semester = (String) itSemeUnit.next();
                        final Map unitMap = (Map) subclass._semeUnitMap.get(semester);

                        //単元
                        for (Iterator itUnit = unitMap.keySet().iterator(); itUnit.hasNext();) {
                            final String unitCd = (String) itUnit.next();
                            final UnitData unitData = (UnitData) unitMap.get(unitCd);

                            //単元名
                            final List setUnitList = KNJ_EditKinsoku.getTokenList(unitData._unitName, 2);
                            //学期名
                            final List setSemeList = KNJ_EditKinsoku
                                    .getTokenList((String) _param._semesterMap.get(semester), 2);
                            //登録データ1
                            final String remark1 = (String) unitData._unitSeqMap.get("1");
                            final List setRemarkList1 = KNJ_EditKinsoku.getTokenList(remark1, 30);
                            //登録データ2
                            final String remark2 = (String) unitData._unitSeqMap.get("2");
                            final List setRemarkList2 = KNJ_EditKinsoku.getTokenList(remark2, 30);
                            //登録データ3
                            final String remark3 = (String) unitData._unitSeqMap.get("3");
                            final List setRemarkList3 = KNJ_EditKinsoku.getTokenList(remark3, 40);

                            //未登録は出力しない
                            if ("".equals(remark1) && "".equals(remark2) && "".equals(remark3)) {
                                continue;
                            }

                            int maxLine = setRemarkList1.size();
                            if (setRemarkList2.size() > maxLine) {
                                maxLine = setRemarkList2.size();
                            }
                            if (setRemarkList3.size() > maxLine) {
                                maxLine = setRemarkList3.size();
                            }
                            if (setSubclassList.size() > maxLine) {
                                maxLine = setSubclassList.size();
                            }
                            if (setUnitList.size() > maxLine) {
                                maxLine = setUnitList.size();
                            }

                            checkLineAndPageChangeBlock8(svf, maxLine);
                            for (int i = 0; i < maxLine; i++) {
                                final String fieldName;
                                if (subclass._isUnit) {
                                    //単元あり
                                    fieldName = "1";
                                    svf.VrsOut("GRP" + fieldName + "_2", String.valueOf(grp2));
                                    grp4 = grp2;
                                    final String unitField = "UNIT" + fieldName;
                                    svf.VrAttribute(unitField, ITEM_PAINT);
                                    if (i < setUnitList.size()) {
                                        svf.VrsOut(unitField, (String) setUnitList.get(i));
                                    }
                                } else {
                                    //単元なし
                                    fieldName = "2";
                                    grp4 = grp3;
                                }
                                final String classNameField = "CLASS_NAME" + fieldName;
                                svf.VrAttribute(classNameField, ITEM_PAINT);
                                if (!printSubclass.containsKey(subclassCd) && i < setSubclassList.size()) {
                                    svf.VrsOut(classNameField, (String) setSubclassList.get(i));
                                }
                                final String semeField = "SEMESTER" + fieldName;
                                svf.VrAttribute(semeField, ITEM_PAINT);
                                if (!printSemester.containsKey(semester) && setSemeList.size() > i) {
                                    svf.VrsOut(semeField, (String) setSemeList.get(i));
                                }
                                svf.VrsOut("GRP" + fieldName + "_1", String.valueOf(grp1));
                                svf.VrsOut("GRP" + fieldName + "_3", String.valueOf(grp3));
                                svf.VrsOut("GRP" + fieldName + "_4", String.valueOf(grp4));
                                svf.VrsOut("GRP" + fieldName + "_5", String.valueOf(grp4));
                                svf.VrsOut("GRP" + fieldName + "_6", String.valueOf(grp4));
                                if (i < setRemarkList1.size()) {
                                    svf.VrsOut("HOPE" + fieldName, (String) setRemarkList1.get(i));
                                }
                                if (i < setRemarkList2.size()) {
                                    svf.VrsOut("METHOD" + fieldName, (String) setRemarkList2.get(i));
                                }
                                if (i < setRemarkList3.size()) {
                                    svf.VrsOut("VAL" + fieldName, (String) setRemarkList3.get(i));
                                }
                                svf.VrEndRecord();
                                _lineCnt++;
                                _hasData = true;
                            }
                            printSemester.put(semester, semester);
                            printSubclass.put(subclassCd, subclassCd);
                            grp2++;
                        }
                        grp3++;
                    }
                    grp1++;
                }

                if (_subclassMap.size() == 0) {
                    svf.VrsOut("GRP1_1", "n");
                }

                //総合所見
                int idx = 0;
                grp1 = 1;
                grp2 = 1;
                final List semester = new ArrayList<String>();

                for (Iterator ite = _param._semesterMap.keySet().iterator(); ite.hasNext();) {
                    final String key = (String)ite.next();
                    if (SEMEALL.equals(key)) {
                        continue;
                    }
                    semester.add(_param._semesterMap.get(key));
                }
                final String content1 = student.getHreportGuidanceSchregRemark("1", "04", "1");
                final String content2 = student.getHreportGuidanceSchregRemark("2", "04", "1");
                final String[] contents = { content1, content2 };
                if (!"".equals(content1) || !"".equals(content2)) {
                    final List item = KNJ_EditKinsoku
                            .getTokenList(_param.getHreportGuidanceKindNameDatMapValue("04", "001"), 2);
                    for (int i = 0; i < semester.size(); i++) {
                        final List semes = KNJ_EditKinsoku.getTokenList(semester.get(i).toString(), 2);
                        final List content = KNJ_EditKinsoku.getTokenList(contents[i], 100);
                        final int maxLen = semes.size() > content.size() ? semes.size() : content.size();
                        checkLineAndPageChangeBlock8(svf, maxLen);
                        for (int j = 0; j < maxLen; j++) {
                            svf.VrAttribute("CLASS_NAME3", ITEM_PAINT);
                            svf.VrAttribute("SEMESTER3", ITEM_PAINT);
                            svf.VrsOut("GRP3_1", String.valueOf(grp1));
                            svf.VrsOut("GRP3_3", String.valueOf(grp2));
                            svf.VrsOut("GRP3_4", String.valueOf(grp2));
                            svf.VrsOut("GRP3_5", String.valueOf(grp2));
                            svf.VrsOut("GRP3_6", String.valueOf(grp2));
                            if (idx < item.size()) {
                                svf.VrsOut("CLASS_NAME3", (String) item.get(idx)); //教科等
                            }
                            if (j < semes.size()) {
                                svf.VrsOut("SEMESTER3", (String) semes.get(j)); //学期
                            }
                            if (j < content.size()) {
                                svf.VrsOut("REMARK", (String) content.get(j)); //内容
                            }
                            svf.VrEndRecord();
                            idx++;
                        }
                        grp2++;
                    }
                }
            }
        }

        private void setHeadB(final Vrw32alp svf, final Student student) {
            String title = "各教科等の記録";
            if(_param._hreportGuidanceKindNameHDatMap.containsKey("04")) {
                title = (String) _param._hreportGuidanceKindNameHDatMap.get("04");
            }
            svf.VrsOut("TITLE", title); //頁タイトル

            final String fiedl1 = KNJ_EditEdit.getMS932ByteLength(_itemRemark1) > 26 ? "_2" : "";
            final String fiedl2 = KNJ_EditEdit.getMS932ByteLength(_itemRemark2) > 26 ? "_2" : "";
            svf.VrAttribute("HEADER_NAME1" + fiedl1, ITEM_PAINT);
            svf.VrsOut("HEADER_NAME1" + fiedl1, _itemRemark1);
            svf.VrAttribute("HEADER_NAME2" + fiedl2, ITEM_PAINT);
            svf.VrsOut("HEADER_NAME2" + fiedl2, _itemRemark2);
            svf.VrAttribute("HEADER_NAME3", ITEM_PAINT);
            svf.VrsOut("HEADER_NAME3", _itemRemark3);
            svf.VrsOut("NAME", "氏名 (" + student._name + ")");
            final List subTitleList = new ArrayList();
            subTitleList.add("教科等");
            subTitleList.add("学期");
            int titleCnt = 1;
            for (Iterator itTitle = subTitleList.iterator(); itTitle.hasNext();) {
                final String subTitleName = (String) itTitle.next();
                final String titleField = "SUB_TITLE" + titleCnt;
                svf.VrAttribute(titleField, ITEM_PAINT);
                svf.VrsOut(titleField, subTitleName);
                titleCnt++;
            }
        }

    }

    private class Subclass {
        final String _classCd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclassCd;
        final String _subclassName;
        final boolean _isUnit;
        final Map _semeUnitMap;

        public Subclass(final String classCd, final String schoolKind, final String curriculumCd,
                final String subclassCd, final String subclassName, final String unitCd) {
            _classCd = classCd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclassCd = subclassCd;
            _subclassName = subclassName;
            _isUnit = !"00".equals(unitCd);
            _semeUnitMap = new LinkedMap();
        }

        public void setSemeUnitMap(final String semester, final String unitCd, final String unitName,
                final String guidancePattern, final String seq, final String remark
        ) {
            UnitData unitData;
            final Map unitDataMap;
            if (_semeUnitMap.containsKey(semester)) {
                unitDataMap = (Map) _semeUnitMap.get(semester);
            } else {
                unitDataMap = new LinkedMap();
                _semeUnitMap.put(semester, unitDataMap);
            }
            if (unitDataMap.containsKey(unitCd)) {
                unitData = (UnitData) unitDataMap.get(unitCd);
            } else {
                unitData = new UnitData(unitCd, unitName, guidancePattern);
                unitDataMap.put(unitCd, unitData);
            }
            unitData._unitSeqMap.put(seq, remark);
        }

    }

    private class UnitData {
        final String _unitCd;
        final String _unitName;
        final String _guidancePattern;
        final Map _unitSeqMap;

        public UnitData(final String unitCd, final String unitName, final String guidancePattern) {
            _unitCd = unitCd;
            _unitName = unitName;
            _guidancePattern = guidancePattern;
            _unitSeqMap = new HashMap();
        }
    }

    private class ChallengedSupportplanRecordDat {
        final String _div;
        final String _hope1;
        final String _hope2;
        final String _goals;

        public ChallengedSupportplanRecordDat(
                final String div,
                final String hope1,
                final String hope2,
                final String goals
        ) {
            _div = div;
            _hope1 = hope1;
            _hope2 = hope2;
            _goals = goals;
        }
    }

    private class ChallengedSupportplanMainDat {
        final String _record_date;
        final String _writing_date;
        final String _ones_hope_present;
        final String _guardian_hope_present;
        final String _ones_hope_future;
        final String _guardian_hope_future;
        final String _reasonable_accommodation;
        final String _selfreliance_goal;
        final String _support_goal;
        final String _support_plan;
        final String _record;
        final String _record_staffname;
        final String _challenged_names;

        public ChallengedSupportplanMainDat(
                final String record_date,
                final String writing_date,
                final String ones_hope_present,
                final String guardian_hope_present,
                final String ones_hope_future,
                final String guardian_hope_future,
                final String reasonable_accommodation,
                final String selfreliance_goal,
                final String support_goal,
                final String support_plan,
                final String record,
                final String record_staffname,
                final String challenged_names
        ) {
            _record_date = record_date;
            _writing_date = writing_date;
            _ones_hope_present = ones_hope_present;
            _guardian_hope_present = guardian_hope_present;
            _ones_hope_future = ones_hope_future;
            _guardian_hope_future = guardian_hope_future;
            _reasonable_accommodation = reasonable_accommodation;
            _selfreliance_goal = selfreliance_goal;
            _support_goal = support_goal;
            _support_plan = support_plan;
            _record = record;
            _record_staffname = record_staffname;
            _challenged_names = challenged_names;
        }
    }

    private class HreportGuidanceSchregRemarkDat {
        final String _semester;
        final String _record_date;
        final String _div;
        final String _seq;
        final String _remark;

        public HreportGuidanceSchregRemarkDat(
                final String semester,
                final String record_date,
                final String div,
                final String seq,
                final String remark
        ) {
            _semester = semester;
            _record_date = record_date;
            _div = div;
            _seq = seq;
            _remark = remark;
        }
    }

    private class HreportGuidanceSchregSelfrelianceDat {
        final String _self_target;
        final String _self_div;
        final String _self_seq;

        public HreportGuidanceSchregSelfrelianceDat(
                final String self_target,
                final String self_div,
                final String self_seq
        ) {
            _self_target = self_target;
            _self_div = self_div;
            _self_seq = self_seq;

        }
    }

    private class HreportGuidanceSchregSubclassRemark{
        final String _div;
        final String _subclasscd;
        final String _subclassname;
        final String _remark1;
        final String _remark2;
        final String _remark9;

        public HreportGuidanceSchregSubclassRemark(
                final String div,
                final String subclasscd,
                final String subclassname,
                final String remark1,
                final String remark2,
                final String remark9
        ) {
            _div = div;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _remark1 = remark1;
            _remark2 = remark2;
            _remark9 = remark9;
        }
    }


    private class HreportGuidanceSchregSubclass{
        final String _year;
        final String _semester;
        final String _schregno;
        final String _subclasscd;
        final String _subclassname;
        final String _unitcd;
        final String _guidance_pattern;
        final String _remark1;
        final String _remark2;
        final String _remark3;


        public HreportGuidanceSchregSubclass(
                final String year,
                final String semester,
                final String schregno,
                final String subclasscd,
                final String subclassname,
                final String unitcd,
                final String guidance_pattern,
                final String remark1,
                final String remark2,
                final String remark3
        ) {
            _year = year;
            _semester = semester;
            _schregno = schregno;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _unitcd = unitcd;
            _guidance_pattern = guidance_pattern;
            _remark1 = remark1;
            _remark2 = remark2;
            _remark3 = remark3;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _semester;
        final String _hukusikiRadio;
        final String _schoolKind;
        final String _ghrCd;
        final String[] _categorySelected;
        final String _outputClass; //出力クラス
        final boolean _printColumn1; //出力する項目（右列）
        final boolean _printColumn2; //出力する項目（右列）
        final boolean _printColumn3; //出力する項目（右列）
        final boolean _printColumn4; //出力する項目（右列）
        final boolean _printColumn5; //出力する項目（右列）
        final boolean _printBlock1; //出力する項目（左列）
        final boolean _printBlock2; //出力する項目（左列）
        final boolean _printBlock3; //出力する項目（左列）
        final boolean _printBlock4; //出力する項目（左列）
        final boolean _printBlock5; //出力する項目（左列）
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _prgid;
        final String _documentroot;
        final String _selectGhr;
        final String _printLogStaffcd;
        final String _printLogRemoteIdent;
        final String _schoolName;
        final Map _semesterMap;
        final Map _kindNameMap;
        final Map _hreportGuidanceKindNameDatMap;
        final Map _hreportGuidanceKindNameHDatMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _semester = request.getParameter("SEMESTER");
            _hukusikiRadio = request.getParameter("HUKUSIKI_RADIO");
            _outputClass = request.getParameter("OUTPUT_CLASS");
            _schoolKind = request.getParameter("SCHOOL_KIND");
            _ghrCd = request.getParameter("GHR_CD");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _printColumn1 = !StringUtils.isEmpty(request.getParameter("CHK_COLUMNBLOCK1"));
            _printColumn2 = !StringUtils.isEmpty(request.getParameter("CHK_COLUMNBLOCK2"));
            _printColumn3 = !StringUtils.isEmpty(request.getParameter("CHK_COLUMNBLOCK3"));
            _printColumn4 = !StringUtils.isEmpty(request.getParameter("CHK_COLUMNBLOCK4"));
            _printColumn5 = !StringUtils.isEmpty(request.getParameter("CHK_COLUMNBLOCK5"));
            _printBlock1 = !StringUtils.isEmpty(request.getParameter("CHK_PRINTBLOCK1"));
            _printBlock2 = !StringUtils.isEmpty(request.getParameter("CHK_PRINTBLOCK2"));
            _printBlock3 = !StringUtils.isEmpty(request.getParameter("CHK_PRINTBLOCK3"));
            _printBlock4 = !StringUtils.isEmpty(request.getParameter("CHK_PRINTBLOCK4"));
            _printBlock5 = !StringUtils.isEmpty(request.getParameter("CHK_PRINTBLOCK5"));
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _prgid = request.getParameter("PRGID");
            _documentroot = request.getParameter("DOCUMENTROOT");
            _selectGhr = request.getParameter("SELECT_GHR");
            _printLogStaffcd = request.getParameter("PRINT_LOG_STAFFCD");
            _printLogRemoteIdent = request.getParameter("PRINT_LOG_REMOTE_IDENT");
            _schoolName = getSchoolName(db2);
            _semesterMap = getSemesterMap(db2);
            _kindNameMap = getKindNameMap(db2);
            _hreportGuidanceKindNameDatMap = getHreportGuidanceKindNameDatMap(db2);
            _hreportGuidanceKindNameHDatMap = getHreportGuidanceKindNameHDatMap(db2);
        }

        private String getSchoolName(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
//            sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK2, REMARK4, REMARK5 FROM CERTIF_SCHOOL_DAT ");
//            sql.append(" WHERE YEAR = '" + _ctrlYear + "' AND CERTIF_KINDCD = '103' ");
            sql.append(" SELECT * ");
            sql.append("   FROM SCHOOL_MST ");
            sql.append("  WHERE YEAR = '" + _ctrlYear + "' ");
            sql.append("    AND SCHOOL_KIND = '" + _schoolKind + "' ");
            log.debug("certif_school_dat sql = " + sql.toString());

            String retSchoolName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    retSchoolName = StringUtils.defaultString(rs.getString("SCHOOLNAME1"));
                }
            } catch (SQLException ex) {
                log.error("certif_school_dat exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retSchoolName;
        }

        private Map getSemesterMap(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SEMESTER, SEMESTERNAME FROM SEMESTER_MST ");
            sql.append(" WHERE YEAR = '" + _ctrlYear + "' ");
            sql.append(" ORDER BY SEMESTER ");

            final Map retSemesterMap = new LinkedMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    retSemesterMap.put(StringUtils.defaultString(rs.getString("SEMESTER")), StringUtils.defaultString(rs.getString("SEMESTERNAME")));
                }
            } catch (SQLException ex) {
                log.error("certif_school_dat exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retSemesterMap;
        }

        private Map getKindNameMap(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT * ");
            sql.append(" FROM CHALLENGED_SUPPORTPLAN_KIND_NAME_DAT ");
            sql.append(" WHERE YEAR = '" + _ctrlYear + "' ");
            sql.append(" ORDER BY KIND_NO, KIND_SEQ ");

            final Map rtnMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String kind_no  = StringUtils.defaultString(rs.getString("KIND_NO"));
                    final String kind_seq = StringUtils.defaultString(rs.getString("KIND_SEQ"));
                    final String kind_name= StringUtils.defaultString(rs.getString("KIND_NAME"));
                    final String status_name1 = StringUtils.defaultString(rs.getString("STATUS_NAME1"));
                    final String status_name2 = StringUtils.defaultString(rs.getString("STATUS_NAME2"));
                    final String status_name3 = StringUtils.defaultString(rs.getString("STATUS_NAME3"));
                    final String status_name4  = StringUtils.defaultString(rs.getString("STATUS_NAME4"));
                    final Map map1 = new TreeMap();
                    map1.put("KIND_NO", kind_no);
                    map1.put("KIND_SEQ", kind_seq);
                    map1.put("KIND_NAME", kind_name);
                    map1.put("STATUS_NAME1", status_name1);
                    map1.put("STATUS_NAME2", status_name2);
                    map1.put("STATUS_NAME3", status_name3);
                    map1.put("STATUS_NAME4", status_name4);

                    Map map2 = new TreeMap();
                    if(rtnMap.containsKey(kind_no)) {
                        map2 = (Map) rtnMap.get(kind_no);
                    }
                    map2.put(kind_seq, map1);

                    rtnMap.put(kind_no, map2);
                }

            } catch (SQLException ex) {
                log.error("certif_school_dat exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtnMap;
        }

        private String getKindNameMapValue(final String kindNo, final String kindSeq, final String field) {
            String rtnStr = "";
            if(_kindNameMap.containsKey(kindNo)) {
                final Map map1 = (Map) _kindNameMap.get(kindNo);
                if(map1.containsKey(kindSeq)) {
                    final Map map2 = (Map) map1.get(kindSeq);
                    rtnStr = (String) map2.get(field);
                }
            }
            return rtnStr;
        }

        private Map getHreportGuidanceKindNameDatMap(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT * ");
            sql.append(" FROM HREPORT_GUIDANCE_KIND_NAME_DAT ");
            sql.append(" WHERE YEAR = '" + _ctrlYear + "' ");
            sql.append(" ORDER BY KIND_NO, KIND_SEQ");

            final Map rtnMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String kind_no  = StringUtils.defaultString(rs.getString("KIND_NO"));
                    final String kind_seq = StringUtils.defaultString(rs.getString("KIND_SEQ"));
                    final String kind_remark = StringUtils.defaultString(rs.getString("KIND_REMARK"));
                    final Map map1 = new TreeMap();
                    map1.put("KIND_NO", kind_no);
                    map1.put("KIND_SEQ", kind_seq);
                    map1.put("KIND_REMARK", kind_remark);

                    Map map2 = new TreeMap();
                    if(rtnMap.containsKey(kind_no)) {
                        map2 = (Map) rtnMap.get(kind_no);
                    }
                    map2.put(kind_seq, map1);

                    rtnMap.put(kind_no, map2);
                }

            } catch (SQLException ex) {
                log.error("certif_school_dat exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtnMap;
        }

        private String getHreportGuidanceKindNameDatMapValue(final String kindNo, final String kindSeq) {
            String rtnStr = "";
            if(_hreportGuidanceKindNameDatMap.containsKey(kindNo)) {
                final Map map1 = (Map) _hreportGuidanceKindNameDatMap.get(kindNo);
                if(map1.containsKey(kindSeq)) {
                    final Map map2 = (Map) map1.get(kindSeq);
                    rtnStr = (String) map2.get("KIND_REMARK");
                }
            }
            return rtnStr;
        }


        private Map getHreportGuidanceKindNameHDatMap(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT * ");
            sql.append(" FROM HREPORT_GUIDANCE_KIND_NAME_HDAT ");
            sql.append(" WHERE YEAR = '" + _ctrlYear + "' ");

            final Map rtnMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String kind_no  = StringUtils.defaultString(rs.getString("KIND_NO"));
                    final String kind_name = StringUtils.defaultString(rs.getString("KIND_NAME"));
                    rtnMap.put(kind_no, kind_name);
                }

            } catch (SQLException ex) {
                log.error("certif_school_dat exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtnMap;
        }
    }
}

// eof
