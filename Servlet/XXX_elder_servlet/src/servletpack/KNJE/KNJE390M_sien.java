/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 *
 * 作成日: 2020/04/07
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJEditString;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.SvfField;


public class KNJE390M_sien {

    private static final Log log = LogFactory.getLog(KNJE390M_sien.class);

    private boolean _hasData;

    int _line = 1;
    final int PAGE_MAXLINE  = 39; //KNJE390M_B_1.frm の行数（学部～障害名等を除いた行数）
    final int PAGE_MAXLINE1 = 44; //KNJE390M_B_2_1.frm の行数
    final int PAGE_MAXLINE2 = 39; //KNJE390M_B_2_2.frm の行数
    int CURRENT_PAGE_MAXLINE = PAGE_MAXLINE; //現在出力フォームの行数（初期：KNJE390M_B_1.frm）

    //出力フォーム
    final String form1 = "KNJE390M_B_2_1.frm";
    final String form2= "KNJE390M_B_2_2.frm";
//    boolean _lastPageFlg = false;

    /** 中央寄せ */
    private static final String ATTR_CENTERING = "Hensyu=3";

    String ATTR_PAINT_GRAY_FILL = "PAINT=(0,85,2)";

    final int OUTPUT_FULL_GYO = 999; //登録された全行出力
    KNJEditString knjobj = new KNJEditString();

    private Param _param;

     /**
      * @param request
      *            リクエスト
      * @param response
      *            レスポンス
      */
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response, final DB2UDB db2) throws Exception {
         try {
             _param = createParam(db2, request);

             _hasData = false;
         } catch (final Exception e) {
             log.error("Exception:", e);
         }
     }

    public void printMain(final DB2UDB db2, final Vrw32alp svf, final String schregNo) {
        final StudentLoc student = getInfo(db2, schregNo);
        if (student == null) return;

        svf.VrSetForm("KNJE390M_B_1.frm", 4);

        for (int i = 0; i <= 60; i++) {
            svf.VrAttribute("HEAD_" + String.valueOf(i), ATTR_PAINT_GRAY_FILL);
        }

        //各生徒毎の出力処理
        //ページ先頭の情報を出力
        printTopInfo(db2, svf, student);

        //1ページ目
        _line = 1;
        printPage1(db2, svf, student);

        //2ページ目
        printPage2(db2, svf, student);

//        //最終ページの出力
//        if(_lastPageFlg == false) {
//            final String[] date = KNJ_EditDate.tate_format4(db2, _param._ctrlDate);
//            svf.VrSetForm(form2, 4);
//            svf.VrsOut("ERA_NAME", date[0]); //元号
//            svf.VrsOut("BLANK", "aa");
//            svf.VrEndRecord();
//        }

        _hasData = true;
    }


    private void printTopInfo(final DB2UDB db2, final Vrw32alp svf, final StudentLoc student) {
        //枠外
        svf.VrsOut("SCHOOL_NAME", _param._knjSchoolMst._schoolName1);
        final StringBuffer sql4 = new StringBuffer();
        sql4.append(" SELECT ");
        sql4.append("   RECORD_STAFFNAME ");
        sql4.append(" FROM SCHREG_CHALLENGED_PROFILE_MAIN_DAT ");
        sql4.append(" WHERE ");
        sql4.append("   SCHREGNO = '" + student._schregno + "' ");
        sql4.append("   AND RECORD_DATE = (SELECT MAX(RECORD_DATE) FROM SCHREG_CHALLENGED_PROFILE_MAIN_DAT WHERE SCHREGNO = '" + student._schregno + "') ");
        final List resultList4 = getRowList(db2, sql4.toString());
        String recStaff = null;
        if (resultList4.size() > 0) {
            final Map row = (Map) resultList4.get(0);
            recStaff = getString("RECORD_STAFFNAME", row);
        }
        if (recStaff != null) {
            final int mlen = KNJ_EditEdit.getMS932ByteLength(recStaff);
            if (mlen > 30) {
                final String[] cutStr = KNJ_EditEdit.get_token(recStaff, 40, 2);
                svf.VrsOut("MAKER3_2", cutStr[0]);
                if (mlen > 40) {
                    svf.VrsOut("MAKER3_3", cutStr[1]);
                }
            } else {
                svf.VrsOut("MAKER3", recStaff); // 作成者
            }
        }

        //生徒情報
        svf.VrsOut("DEPARTMENT_NAME", student._coursename);
        svf.VrsOut("GRADE_NAME", getPrintStudentGrHrname(student));


        final int entryMaxLine = 1;
        final Map updateMap = getUpdateHistMap(db2, "C", _param._ctrlYear, student._schregno, entryMaxLine, student._writingDate);
        if (student._schregChallengedSupportplanMain.size() > 0) {
            final SchregChallengedSupportplanMain printData = (SchregChallengedSupportplanMain)student._schregChallengedSupportplanMain.get(0);
            final String updStr = printData._record_date;
            svf.VrsOut("MAKE_DATE", ("".equals(StringUtils.defaultString(updStr, "")) || (!NumberUtils.isNumber(updStr) && updStr.length() < 8)) ? "" : formatDate(db2, updStr)); // 作成日
            final String mkfield = KNJ_EditEdit.getMS932ByteLength(student._recordStaffName) > 10 ? "2" : "1";
            svf.VrsOut("MAKER" + mkfield, printData._record_staffname);
        }

        final int nklen = KNJ_EditEdit.getMS932ByteLength(student._name_Kana);
        final String nkfield = nklen > 30 ? "3" : "2";
        svf.VrsOut("KANA" + nkfield, student._name_Kana);
        final int nlen = KNJ_EditEdit.getMS932ByteLength(student._name);
        final String nfield = nlen > 26 ? "3" : "2";
        svf.VrsOut("NAME" + nfield, student._name);
        svf.VrsOut("SEX", student._sex);
        svf.VrsOut("BIRTHDAY", formatDate(db2, student._birthday));

        svf.VrsOut("ZIP1", student._zipCd);  //本人住所(郵便番号)
        final int ad1len = KNJ_EditEdit.getMS932ByteLength(student._addr1);
        final int ad2len = KNJ_EditEdit.getMS932ByteLength(student._addr2);
        final String adrfield = (ad1len > 40 || ad2len > 40) ? "_2" : "";
        svf.VrsOut("ADDR1_1" + adrfield, student._addr1);  //本人住所(住所)
        svf.VrsOut("ADDR1_2" + adrfield, student._addr2);  //本人住所(住所)

        final int gnlen = KNJ_EditEdit.getMS932ByteLength(student._guardName);
        final String gnfield = gnlen > 26 ? "3" : "2";
        svf.VrsOut("GUARD_NAME" + gnfield, student._guardName);
        final int gklen = KNJ_EditEdit.getMS932ByteLength(student._guardKana);
        final String gkfield = gklen > 30 ? "3" : "2";
        svf.VrsOut("GUARD_KANA" + gkfield, student._guardKana);

        svf.VrsOut("GUARD_RELATION", student._relationship);  //続柄

        svf.VrsOut("ZIP2", student._guardZipCd);  //本人住所(郵便番号)
        final int ad21len = KNJ_EditEdit.getMS932ByteLength(student._guardAddr1);
        final int ad22len = KNJ_EditEdit.getMS932ByteLength(student._guardAddr2);
        final String ad2field = (ad21len > 40 || ad22len > 40) ? "_2" : "";
        svf.VrsOut("ADDR2_1" + ad2field, student._guardAddr1);  //本人住所(住所)
        svf.VrsOut("ADDR2_2" + ad2field, student._guardAddr2);  //本人住所(住所)

        svf.VrsOut("TELNO", student._telno);

        List challengedNames = knjobj.retDividString(getString("CHALLENGED_NAMES", updateMap), 90, 3); //障害名等
        for (int i = 0 ; i < Math.min(3,challengedNames.size()) ; i++) {
            svf.VrsOut("HANDI_CONDITION" + (i+1), (String) challengedNames.get(i));
        }
        svf.VrsOut("BLANK", "aa");
    }

    private String getPrintStudentGrHrname(final StudentLoc student) {
        return student._grade_Name2 + ("2".equals(_param._printHrClassType) ? student._ghr_Nameabbv : student._hrClassName2);
    }

    //1ページ目
    private void printPage1(final DB2UDB db2, final Vrw32alp svf, final StudentLoc student) {
        //変数宣言
        int maxLen = 0;
        List reasonableAccommodation = new ArrayList(); //合理的配慮等

        //値の設定
        for (Iterator iterator = student._schregChallengedSupportplanMain.iterator(); iterator.hasNext();) {
            final SchregChallengedSupportplanMain printData = (SchregChallengedSupportplanMain) iterator.next();
            reasonableAccommodation = knjobj.retDividString(printData._reasonable_accommodation, 110, 20);
        }

        //2段目
        svf.VrsOut("TITLE", "願い"); //項目名称
        svf.VrAttribute("TITLE", ATTR_PAINT_GRAY_FILL);
        svf.VrEndRecord();
        _line += 1; //ヘッダ行
        if (_param._challengedTitle.containsKey("01")) {
            final ChallengedSupportPlanKindName prtWk = (ChallengedSupportPlanKindName)_param._challengedTitle.get("01");
            svf.VrsOut("LEAD1", prtWk._status_Name1);  //願い(左)項目名
            svf.VrAttribute("LEAD1", ATTR_PAINT_GRAY_FILL + " " + ATTR_CENTERING);
            svf.VrsOut("LEAD2", prtWk._status_Name2);  //願い(右)項目名
            svf.VrAttribute("LEAD2", ATTR_PAINT_GRAY_FILL + " " + ATTR_CENTERING);
            _line += 1; //ヘッダ行
        }
        svf.VrsOut("GRPHOPE_1", "B1");
        svf.VrAttribute("GRPHOPE_1", ATTR_PAINT_GRAY_FILL);
        svf.VrsOut("GRPHOPE_2", "B2");
        svf.VrAttribute("GRPHOPE_2", ATTR_PAINT_GRAY_FILL);
        svf.VrsOut("GRPHOPE_3", "B3");
        svf.VrAttribute("GRPHOPE_3", ATTR_PAINT_GRAY_FILL);
        svf.VrsOut("GRPHOPE_4", "B4");
        svf.VrAttribute("GRPHOPE_4", ATTR_PAINT_GRAY_FILL);
        svf.VrsOut("GRPHOPE_5", "B5");
        svf.VrAttribute("GRPHOPE_5", ATTR_PAINT_GRAY_FILL);
        svf.VrEndRecord();

        boolean prtChk1 = false;
        int recCnt = 1;
        for (Iterator itr = student._schregChallengedSupportplanRecord.iterator();itr.hasNext();) {
            final SchregChallengedSupportPlanRecordDat prtWk = (SchregChallengedSupportPlanRecordDat)itr.next();
            if (!"01".equals(prtWk._kind_No)) {
                continue;
            }
            svf.VrAttribute("GRPHOPE_1", ATTR_PAINT_GRAY_FILL);
            svf.VrsOut("GRPHOPE_1", recCnt + "1");
            List prtLst = createPrtList("LEAD1", prtWk._descript1, "LEAD2", prtWk._descript2, 50, 8);  //数値は、最大入力バイト、行数
            final List ttlLst = KNJ_EditKinsoku.getTokenList(prtWk._kind_Name, 10);
            final int maxidx = Math.max(ttlLst.size(), prtLst.size());  // データ、タイトルのどちらか大きい方をこの項目の出力行数とする。
            int prtIdx = new BigDecimal((maxidx - (ttlLst.size() - 1)) * 1.0 / 2.0).setScale(0,BigDecimal.ROUND_CEILING).intValue() - 1 ;  // 端数切り上げ( (最大出力行数 - (タイトルサイズ - 1)) / 2) - 1 = タイトル出力位置(※最後の-1は0ベースにするため)
            if (prtIdx < 0) prtIdx = 0;

            if (prtLst.size() == 0) {
                for (int cnt = 0;cnt < ttlLst.size();cnt++) {
                    svf.VrsOut("DIVIDEHOPE_1", (String)ttlLst.get(cnt));  //願い(縦項目)項目名 (※空レコード対策)
                    svf.VrAttribute("GRPHOPE_1", ATTR_PAINT_GRAY_FILL);
                    svf.VrsOut("GRPHOPE_1", recCnt + "1");
                    svf.VrsOut("GRPHOPE_2", recCnt + "2");
                    svf.VrsOut("GRPHOPE_3", recCnt + "3");
                    svf.VrsOut("GRPHOPE_4", recCnt + "4");
                    svf.VrsOut("GRPHOPE_5", recCnt + "5");
                    svf.VrEndRecord();
                    _line++;
                }
            } else {
                Iterator itt = prtLst.iterator();  //元々prtLstベースのループだったが、maxidx分回さないといけなくなったので、lCntベースのループに変更
                for (int lCnt = 0;lCnt < maxidx;lCnt++) {
                    if (prtIdx <= lCnt && lCnt - prtIdx < ttlLst.size()) {
                        svf.VrsOut("DIVIDEHOPE_1", (String)ttlLst.get(lCnt - prtIdx));  //願い(縦項目)項目名(※1レコードに複数行入るための対策)
                    }
                    if (itt.hasNext()) {
                        final Map cutMap = (Map)itt.next();
                        svf.VrsOut("LEAD1", cutMap.containsKey("LEAD1") && !"".equals(StringUtils.defaultString((String)cutMap.get("LEAD1"), "")) ? (String)cutMap.get("LEAD1"): "");  //願い縦項目(左)
                        svf.VrsOut("LEAD2", cutMap.containsKey("LEAD2") && !"".equals(StringUtils.defaultString((String)cutMap.get("LEAD2"), "")) ? (String)cutMap.get("LEAD2"): "");  //願い縦項目(右)
                    }
                    svf.VrAttribute("GRPHOPE_1", ATTR_PAINT_GRAY_FILL);
                    svf.VrsOut("GRPHOPE_1", recCnt + "1");
                    svf.VrsOut("GRPHOPE_2", recCnt + "2");
                    svf.VrsOut("GRPHOPE_3", recCnt + "3");
                    svf.VrsOut("GRPHOPE_4", recCnt + "4");
                    svf.VrsOut("GRPHOPE_5", recCnt + "5");
                    svf.VrEndRecord();
                    _line++;
                }
            }
            prtChk1 = true;
            recCnt++;
        }
        //もし、データ出力が無いなら、空行を1つ出す。
        if (!prtChk1) {
            svf.VrsOut("DIVIDEHOPE_1", "");  //願い(縦項目)項目名
            svf.VrsOut("GRPHOPE_1", recCnt + "1");
            svf.VrAttribute("GRPHOPE_1", ATTR_PAINT_GRAY_FILL);
            svf.VrsOut("GRPHOPE_2", recCnt + "2");
            svf.VrsOut("GRPHOPE_3", recCnt + "3");
            svf.VrsOut("GRPHOPE_4", recCnt + "4");
            svf.VrsOut("GRPHOPE_5", recCnt + "5");
            svf.VrEndRecord();
            _line++;
        }
        svf.VrsOut("BLANK", "aa");
        svf.VrEndRecord();
        _line += 1; //ヘッダ行

        svf.VrsOut("TITLE", "目標"); //項目名称
        svf.VrAttribute("TITLE", ATTR_PAINT_GRAY_FILL);
        svf.VrEndRecord();
        _line += 1; //ヘッダ行

        recCnt = 1;
        for (Iterator itr = student._schregChallengedSupportplanRecord.iterator();itr.hasNext();) {
            final SchregChallengedSupportPlanRecordDat prtWk = (SchregChallengedSupportPlanRecordDat)itr.next();
            if (!"02".equals(prtWk._kind_No)) {
                continue;
            }
            if ("".equals(StringUtils.defaultString(prtWk._descript1, ""))) {
                final List ttlLst = KNJ_EditKinsoku.getTokenList(prtWk._kind_Name, 10);
                for (int cnt = 0;cnt < ttlLst.size();cnt++) {
                    svf.VrsOut("DIVI_HOPE", (String)ttlLst.get(cnt)); //目標(縦項目)項目名 (※空レコード対策)
                    svf.VrsOut("GRPSEIIKU_1", recCnt + "1");
                    svf.VrsOut("GRPSEIIKU_2", recCnt + "1");
                    svf.VrAttribute("GRPSEIIKU_1", ATTR_PAINT_GRAY_FILL);
                    svf.VrEndRecord();
                    _line++;
                }
            } else {
                final String[] s1Str = KNJ_EditEdit.get_token(prtWk._descript1, 100, 3);  //数値は、最大入力バイト、行数
                final int lastidx = getLastStrArryIdx(s1Str) + 1;
                final List ttlLst = KNJ_EditKinsoku.getTokenList(prtWk._kind_Name, 10);
                final int maxidx = Math.max(ttlLst.size(), lastidx);  // データ、タイトルのどちらか大きい方をこの項目の出力行数とする。
                int prtIdx = new BigDecimal((maxidx - (ttlLst.size() - 1)) * 1.0 / 2.0).setScale(0,BigDecimal.ROUND_CEILING).intValue() - 1 ;  // 端数切り上げ( (最大出力行数 - (タイトルサイズ - 1)) / 2) - 1 = タイトル出力位置
                if (prtIdx < 0) prtIdx = 0;
                for (int cnt = 0;cnt < maxidx;cnt++) {
                    if (s1Str == null) continue;
                    if (prtIdx <= cnt && cnt - prtIdx < ttlLst.size()) {
                        svf.VrsOut("DIVI_HOPE", (String)ttlLst.get(cnt - prtIdx));  //目標(縦項目)項目名(※1レコードに複数行入るための対策)
                    }
                    if (cnt < lastidx) {
                        svf.VrsOut("HOPE", s1Str[cnt]);  //目標縦項目
                    }
                    svf.VrsOut("GRPSEIIKU_1", recCnt + "1");
                    svf.VrsOut("GRPSEIIKU_2", recCnt + "1");
                    svf.VrAttribute("GRPSEIIKU_1", ATTR_PAINT_GRAY_FILL);
                    svf.VrEndRecord();
                    _line++;
                }
            }
            recCnt++;
        }
        svf.VrsOut("BLANK", "aa");
        svf.VrEndRecord();
        _line += 1; //ヘッダ行

        //次ページで出力する行数
        int outLine = getPage2OutLine(db2, student, 0);
        final String[] date = KNJ_EditDate.tate_format4(db2, _param._ctrlDate);

        //3段目
        String field = "COMMENT";
        svf.VrsOut("TITLE", "合理的配慮");//項目名称
        svf.VrAttribute("TITLE", ATTR_PAINT_GRAY_FILL);
        svf.VrEndRecord();
        _line += 1; //ヘッダ行
        maxLen = reasonableAccommodation.size();
        for (int i = 0 ; i < maxLen; i++) {
            svf.VrsOut("GRP_COMMENT", "2"); //グループ
            svf.VrsOut(field, (String) reasonableAccommodation.get(i));
            svf.VrEndRecord();
            _line++;
            //改ページ判定
            if(PAGE_MAXLINE < _line) {
                _line = 1;
                if(outLine < 2 ) {
                    //次ページで出力する内容が存在しない場合、最終ページ
                    svf.VrSetForm(form2, 4);
                    svf.VrsOut("ERA_NAME", date[0]); //元号
//                    _lastPageFlg = true;
                } else {
                    //次ページで出力する内容が存在する場合、出力する行数から、次ページが最終ページか否かを判定
                    if(PAGE_MAXLINE2 < (outLine + (maxLen - (i+1)))) {
                        //途中ページ
                        svf.VrSetForm(form1, 4);
                        CURRENT_PAGE_MAXLINE = PAGE_MAXLINE1;
                    } else {
                        //最終ページ
                        svf.VrSetForm(form2, 4);
                        svf.VrsOut("ERA_NAME", date[0]); //元号
                        CURRENT_PAGE_MAXLINE = PAGE_MAXLINE2;
//                        _lastPageFlg = true;
                    }
                }
                field = "COOPERATE"; //改ページ後のフィールド切り替え
            }
        }
        if(maxLen == 0) {
            svf.VrsOut("GRP_COMMENT", "2"); //グループ
            svf.VrEndRecord();
            _line += 1; //ヘッダ行
        }
        _hasData = true;
    }

    private int getLastStrArryIdx(final String[] sStr) {
        int retIdx = sStr == null ? 0 : sStr.length;
        for (int cnt = sStr.length;cnt > 0;cnt--) {
            if (!"".equals(StringUtils.defaultString(sStr[cnt-1], ""))) {
                retIdx = cnt-1;
                break;
            }
        }
        return retIdx;
    }

    private List createPrtList(final String field1, final String sliceStr1, final String field2, final String sliceStr2, final int cutByte, final int maxRow) {
        List retList = new ArrayList();
        Map subMap = new LinkedMap();
        final String[] s1Str = KNJ_EditEdit.get_token(sliceStr1, cutByte, maxRow);
        final String[] s2Str = KNJ_EditEdit.get_token(sliceStr2, cutByte, maxRow);
        final int s1max = s1Str == null ? 0 : s1Str.length;
        final int s2max = s2Str == null ? 0 : s2Str.length;

        boolean bchkS1;
        boolean bchkS2;
        for (int cnt = 0;cnt < Math.max(s1max, s2max);cnt++) {
            subMap = new LinkedMap();
            bchkS1 = true;
            if (s1Str != null && cnt < s1Str.length) {
                subMap.put(field1, s1Str[cnt]);
                bchkS1 = false;
            }
            bchkS2 = true;
            if (s2Str != null && cnt < s2Str.length) {
                subMap.put(field2, s2Str[cnt]);
                bchkS2 = false;
            }
            if ((!bchkS1 && !"".equals(StringUtils.defaultString(s1Str[cnt], ""))) || (!bchkS2 && !"".equals(StringUtils.defaultString(s2Str[cnt], "")))) {
                retList.add(subMap);
            }
        }
        return retList;
    }

    //2ページ目
    private void printPage2(final DB2UDB db2, final Vrw32alp svf, final StudentLoc student) {
        //項目ごとの出力行数
        final int outLine = getPage2OutLine(db2, student, 0); //出力行数
        final int outLine1 = getPage2OutLine(db2, student, 1); //出力行数(具体的な支援)
        final int outLine2 = getPage2OutLine(db2, student, 2); //出力行数(連携の記録)
        if(outLine < 2 ) return;

        //残行数（本ページの出力行数）
        int remainingLine = outLine;

        if(CURRENT_PAGE_MAXLINE == PAGE_MAXLINE) {
            //前ページで改ページが行われていない場合
            _line = 1;
            //フォームの判定
            if(PAGE_MAXLINE2 < remainingLine) {
                //途中ページ
                svf.VrSetForm(form1, 4);
                CURRENT_PAGE_MAXLINE = PAGE_MAXLINE1;
            } else {
                //最終ページ
                final String[] date = KNJ_EditDate.tate_format4(db2, _param._ctrlDate);
                svf.VrSetForm(form2, 4);
                svf.VrsOut("ERA_NAME", date[0]); //元号
                CURRENT_PAGE_MAXLINE = PAGE_MAXLINE2;
            }
        } else {
            //前ページで改ページが行われている場合、空行を挿入
            svf.VrsOut("BLANK", "aa");
            svf.VrEndRecord();
            _line += 1; //ヘッダ行
            //残行数 = 本ページの出力行数 + 前ページからの繰り越し行数
            remainingLine = outLine + _line;
        }


        //具体的な支援
        svf.VrsOut("GRP1_1", "--"); //グループ
        svf.VrsOut("GRP1_2", "00"); //グループ
        svf.VrsOut("GRP1_3", "00"); //グループ
        svf.VrsOut("GRP1_4", "00"); //グループ
        svf.VrsOut("GRP1_5", "00"); //グループ
        svf.VrsOut("DIVIDE1_1", ""); //表タイトル
        svf.VrsOut("DIVIDE1_2", ""); //項目名
        if (_param._challengedTitle.containsKey("03")) {
            final ChallengedSupportPlanKindName prtWk = (ChallengedSupportPlanKindName)_param._challengedTitle.get("03");
            svf.VrsOut("FACILITY1", prtWk._status_Name1);  //具体的な支援(左)項目名
            svf.VrAttribute("FACILITY1", ATTR_PAINT_GRAY_FILL + " " + ATTR_CENTERING);
            svf.VrsOut("SUPPRT_ORG", prtWk._status_Name2);  //具体的な支援(右)項目名
            svf.VrAttribute("SUPPRT_ORG", ATTR_PAINT_GRAY_FILL + " " + ATTR_CENTERING);
            svf.VrsOut("SUPPORT_DIV", prtWk._status_Name3);  //具体的な支援(右)項目名
            svf.VrAttribute("SUPPORT_DIV", ATTR_PAINT_GRAY_FILL + " " + ATTR_CENTERING);
            _line += 1; //ヘッダ行
        }
        svf.VrAttribute("GRP1_1", ATTR_PAINT_GRAY_FILL);
        svf.VrAttribute("GRP1_2", ATTR_PAINT_GRAY_FILL);
        svf.VrAttribute("GRP1_3", ATTR_PAINT_GRAY_FILL);
        svf.VrAttribute("GRP1_4", ATTR_PAINT_GRAY_FILL);
        svf.VrAttribute("GRP1_5", ATTR_PAINT_GRAY_FILL);
        svf.VrEndRecord();

        List hyouName = knjobj.retDividString("具体的な支援", 2, OUTPUT_FULL_GYO);
        int hyouIdx = 0; //表題用
        boolean firstFlg = true;
        if(outLine1 > 2 ) {
            for (Iterator itr = student._schregChallengedSupportplanRecord.iterator();itr.hasNext();) {
                final SchregChallengedSupportPlanRecordDat prtWk = (SchregChallengedSupportPlanRecordDat)itr.next();
                if (!"03".equals(prtWk._kind_No)) {
                    continue;
                }
                if("".equals(StringUtils.defaultString(prtWk._kind_Name))) continue;
                List columName = knjobj.retDividString(prtWk._kind_Name, 6, OUTPUT_FULL_GYO);  //具体的な支援(縦項目)項目名
                List status = knjobj.retDividString(prtWk._descript1, 30, 20);   //具体的な支援(左)
                List status2 = knjobj.retDividString(prtWk._descript2, 24, 20);  //具体的な支援(中)
                List status3 = knjobj.retDividString(prtWk._descript3, 24, 20);  //具体的な支援(右)
                int maxLen = columName.size();
                if(status != null && maxLen < status.size()) maxLen = status.size();
                if(status2 != null && maxLen < status2.size()) maxLen = status2.size();
                if(status3 != null && maxLen < status3.size()) maxLen = status3.size();

                for (int i = 0 ; i < maxLen; i++) {
                    svf.VrsOut("GRP1_1", "--"); //グループ
                    svf.VrsOut("GRP1_2", prtWk.getKindSeqNo()); //グループ
                    svf.VrsOut("GRP1_3", prtWk.getKindSeqNo()); //グループ
                    svf.VrsOut("GRP1_4", prtWk.getKindSeqNo()); //グループ
                    svf.VrsOut("GRP1_5", prtWk.getKindSeqNo()); //グループ
                    if(firstFlg && hyouIdx < hyouName.size()) svf.VrsOut("DIVIDE1_1", (String) hyouName.get(hyouIdx)); //表タイトル
                    if(columName != null &&i < columName.size()) svf.VrsOut("DIVIDE1_2", (String) columName.get(i)); //項目名
                    if(status != null && i < status.size()) svf.VrsOut("FACILITY1", (String) status.get(i)); //支援内容
                    if(status2 != null && i < status2.size()) svf.VrsOut("SUPPRT_ORG", (String) status2.get(i)); //支援機関・支援者
                    if(status3 != null && i < status3.size()) svf.VrsOut("SUPPORT_DIV", (String) status3.get(i)); //支援内容に関する評価
                    svf.VrAttribute("GRP1_1", ATTR_PAINT_GRAY_FILL);
                    svf.VrAttribute("GRP1_2", ATTR_PAINT_GRAY_FILL);
                    svf.VrEndRecord();
                    hyouIdx++;
                    _line++;
                    //改ページ判定
                    remainingLine = newPage(db2, svf, remainingLine);
                }
                if(hyouIdx >= hyouName.size()) firstFlg = false;
            }

        }else {
            int recCnt = 0;
            int putCnt = 0;
            for (Iterator itr = student._schregChallengedSupportplanRecord.iterator();itr.hasNext();) {
                final SchregChallengedSupportPlanRecordDat prtWk = (SchregChallengedSupportPlanRecordDat)itr.next();
                if (!"03".equals(prtWk._kind_No)) {
                    continue;
                }
                recCnt++;
                final String[] columName = KNJ_EditEdit.get_token(prtWk._kind_Name, 10, 20);  //最大行数は20行
                putCnt = 0;
                for (int cnt = 0;cnt < columName.length;cnt++) {
                    if (columName[cnt] == null) continue;
                    svf.VrsOut("DIVIDE1_2", columName[cnt]); //項目名
                    putCnt++;
                    svf.VrsOut("GRP1_1", "--"); //グループ
                    svf.VrsOut("GRP1_2", String.valueOf(recCnt) + (putCnt)); //グループ
                    svf.VrsOut("GRP1_3", String.valueOf(recCnt) + (putCnt)); //グループ
                    svf.VrsOut("GRP1_4", String.valueOf(recCnt) + (putCnt)); //グループ
                    svf.VrsOut("GRP1_5", String.valueOf(recCnt) + (putCnt)); //グループ
                    if(firstFlg && cnt < hyouName.size())svf.VrsOut("DIVIDE1_1", (String) hyouName.get(hyouIdx)); //表タイトル
                    svf.VrAttribute("DIVIDE1_1", ATTR_PAINT_GRAY_FILL);
                    svf.VrAttribute("DIVIDE1_2", ATTR_PAINT_GRAY_FILL);
                    svf.VrEndRecord();
                    hyouIdx++;
                }
                if(hyouIdx >= hyouName.size()) firstFlg = false;
            }
            if (firstFlg) {  //まだ出し切れていない
                for (int cnt = hyouIdx;cnt < hyouName.size();cnt++) {
                    //putCnt++;
                    svf.VrsOut("GRP1_1", "--"); //グループ
                    svf.VrsOut("GRP1_2", String.valueOf(recCnt) + (putCnt)); //グループ
                    svf.VrsOut("GRP1_3", String.valueOf(recCnt) + (putCnt)); //グループ
                    svf.VrsOut("GRP1_4", String.valueOf(recCnt) + (putCnt)); //グループ
                    svf.VrsOut("GRP1_5", String.valueOf(recCnt) + (putCnt)); //グループ
                    if(firstFlg && cnt < hyouName.size())svf.VrsOut("DIVIDE1_1", (String) hyouName.get(hyouIdx)); //表タイトル
                    svf.VrAttribute("DIVIDE1_1", ATTR_PAINT_GRAY_FILL);
                    svf.VrAttribute("DIVIDE1_2", ATTR_PAINT_GRAY_FILL);
                    svf.VrEndRecord();
                }
            }
        }

        svf.VrsOut("BLANK", "aa");
        svf.VrEndRecord();
        _line += 1; //ヘッダ行

        //連携の記録
        if(outLine2 > 2 ) {
            String title = "連携の記録";
            if (_param._challengedTitle.containsKey("04")) {
                final ChallengedSupportPlanKindName prtWk = (ChallengedSupportPlanKindName)_param._challengedTitle.get("04");
                title = prtWk._status_Name1;
            }
            svf.VrsOut("TITLE", title); //連携の記録 タイトル
            svf.VrAttribute("TITLE", ATTR_PAINT_GRAY_FILL);
            _line += 1; //ヘッダ行
            for (Iterator iterator = student._schregChallengedSupportplanMain.iterator(); iterator.hasNext();) {
                final SchregChallengedSupportplanMain printData = (SchregChallengedSupportplanMain) iterator.next();
                List record = knjobj.retDividString(printData._record, 110, 15);
                for (int i = 0 ; i < record.size(); i++) {
                    svf.VrsOut("GRP3_1", "3"); //グループ
                    svf.VrsOut("COOPERATE", (String) record.get(i)); //連携の記録
                    svf.VrEndRecord();
                    _line++;
                    //改ページ判定
                    remainingLine = newPage(db2, svf, remainingLine);
                }
            }
        }else {
            String title = "連携の記録";
            if (_param._challengedTitle.containsKey("04")) {
                final ChallengedSupportPlanKindName prtWk = (ChallengedSupportPlanKindName)_param._challengedTitle.get("04");
                title = prtWk._status_Name1;
            }
            svf.VrsOut("TITLE", title); //連携の記録 タイトル
            svf.VrAttribute("TITLE", ATTR_PAINT_GRAY_FILL);
            svf.VrsOut("GRP3_1", "3"); //グループ
            svf.VrEndRecord();
        }
        svf.VrEndPage();
        _hasData = true;
    }

    //改ページ判定
    private int newPage(final DB2UDB db2, final Vrw32alp svf, int remainingLine) {
        if(CURRENT_PAGE_MAXLINE < _line) {
            remainingLine = remainingLine - _line; //出力行を除いた残行数
            _line = 1;
            //残行数から、次ページが最終ページか否かを判定
            if(PAGE_MAXLINE2 < remainingLine) {
                //途中ページ
                svf.VrSetForm(form1, 4);
//                CURRENT_PAGE_MAXLINE = PAGE_MAXLINE1; //残行数が、途中ページの行数より大きい

                //次ページの行数設定
                if(remainingLine <= PAGE_MAXLINE1) {
                    CURRENT_PAGE_MAXLINE = PAGE_MAXLINE2-1; //残行数が、途中ページの行数以下(最終ページ出力用に1行以上繰り越し)
                } else {
                    CURRENT_PAGE_MAXLINE = PAGE_MAXLINE1; //残行数が、途中ページの行数より大きい
                }
            } else {
                //最終ページ
                final String[] date = KNJ_EditDate.tate_format4(db2, _param._ctrlDate);
                svf.VrSetForm(form2, 4);
                svf.VrsOut("ERA_NAME", date[0]); //元号
                CURRENT_PAGE_MAXLINE = PAGE_MAXLINE2;
//                _lastPageFlg = true;
            }
        }
        return remainingLine;
    }

    //2ページ目の行数取得
    private int getPage2OutLine(final DB2UDB db2, final StudentLoc student, final int countKbn) {
        //countKbn == 0：全行 1：具体的な支援の行数　2：連携の記録の行数
        int totalLine = 1;

        if(countKbn == 0 || countKbn == 1) {
            //具体的な支援
            totalLine += 1; //ヘッダ行
            for (Iterator itr = student._schregChallengedSupportplanRecord.iterator();itr.hasNext();) {
                final SchregChallengedSupportPlanRecordDat prtWk = (SchregChallengedSupportPlanRecordDat)itr.next();

                if (!"03".equals(prtWk._kind_No)) {
                    continue;
                }

                if("".equals(StringUtils.defaultString(prtWk._kind_Name))) continue;
                List columName = knjobj.retDividString(prtWk._kind_Name, 6, OUTPUT_FULL_GYO);  //具体的な支援(縦項目)項目名
                List status = knjobj.retDividString(prtWk._descript1, 30, 20);   //具体的な支援(左)
                List status2 = knjobj.retDividString(prtWk._descript2, 24, 20);  //具体的な支援(中)
                List status3 = knjobj.retDividString(prtWk._descript3, 24, 20);  //具体的な支援(右)
                int maxLen = columName.size();
                if(status != null && maxLen < status.size()) maxLen = status.size();
                if(status2 != null && maxLen < status2.size()) maxLen = status2.size();
                if(status3 != null && maxLen < status3.size()) maxLen = status3.size();
                totalLine += maxLen; //行数加算
            }
        }
        if(countKbn == 0) {
            totalLine += 1; //「具体的な支援」と「連携の記録」の間にあるブランク
        }

        if(countKbn == 0 || countKbn == 2) {
            //連携の記録
            totalLine += 1; //ヘッダ行
            for (Iterator iterator = student._schregChallengedSupportplanMain.iterator(); iterator.hasNext();) {
                final SchregChallengedSupportplanMain printData = (SchregChallengedSupportplanMain) iterator.next();
                List record = knjobj.retDividString(printData._record, 90, 10);
                totalLine += record.size();
            }
        }

        return totalLine;
    }


    protected static List getTokenList(final String source0, final int bytePerLine, final int gyo) {
        if (source0 == null || source0.length() == 0) {
            return new ArrayList();
        }
        return KNJ_EditKinsoku.getTokenList(source0, bytePerLine, gyo);
    }

    protected static String formatDate(final DB2UDB db2, final String date) {
        if (null == date) {
            return null;
        }
        if ("".equals(date)) {
            return "";
        }
        final String[] tateFormat = KNJ_EditDate.tate_format(KNJ_EditDate.h_format_JP(db2, date));
        final String nengo = KNJ_EditDate.gengouAlphabetMarkOfDate(db2, StringUtils.replace(date, "-", "/"));
        return nengo + ("元".equals(tateFormat[1]) ? "1" : tateFormat[1]) + "." + tateFormat[2] + "." + tateFormat[3];
    }

    private StudentLoc getInfo(final DB2UDB db2, final String schregNo) {
        StudentLoc student = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {

            final String studentSql = getStudentSql();
            ps = db2.prepareStatement(studentSql);
            ps.setString(1, schregNo);
            rs = ps.executeQuery();

            if (rs.next()) {
                final String grade = StringUtils.defaultString(rs.getString("GRADE"));
                final String schoolkind_Name = StringUtils.defaultString(rs.getString("SCHOOLKIND_NAME"));
                final String grade_Cd = StringUtils.defaultString(rs.getString("GRADE_CD"));
                final String grade_Name1 = StringUtils.defaultString(rs.getString("GRADE_NAME1"));
                final String grade_Name2 = StringUtils.defaultString(rs.getString("GRADE_NAME2"));
                final String schregno = StringUtils.defaultString(rs.getString("SCHREGNO"));
                final String name = StringUtils.defaultString(rs.getString("NAME"));
                final String name_Kana = StringUtils.defaultString(rs.getString("NAME_KANA"));
                final String sex = StringUtils.defaultString(rs.getString("SEX"));
                final String birthday = StringUtils.defaultString(rs.getString("BIRTHDAY"));
                final String zipCd = StringUtils.defaultString(rs.getString("ZIPCD"));
                final String addr1 = StringUtils.defaultString(rs.getString("ADDR1"));
                final String addr2 = StringUtils.defaultString(rs.getString("ADDR2"));
                final String telno = StringUtils.defaultString(rs.getString("TELNO"));
                final String coursecd = StringUtils.defaultString(rs.getString("COURSECD"));
                final String coursename = StringUtils.defaultString(rs.getString("COURSENAME"));
                final String hrClassName2 = StringUtils.defaultString(rs.getString("HR_CLASS_NAME2"));
                final String hrNameAbbv = StringUtils.defaultString(rs.getString("HR_NAMEABBV"));
                final String ghr_Cd = StringUtils.defaultString(rs.getString("GHR_CD"), "");
                final String ghr_Name = StringUtils.defaultString(rs.getString("GHR_NAME"));
                final String ghr_Nameabbv = StringUtils.defaultString(rs.getString("GHR_NAMEABBV"));
                final String ghr_Attendno = StringUtils.defaultString(rs.getString("GHR_ATTENDNO"));
                final String challenged_Card_Class = StringUtils.defaultString(rs.getString("CHALLENGED_CARD_CLASS"));
                final String card_Class = StringUtils.defaultString(rs.getString("CARD_CLASS"));
                final String challenged_Card_Rank = StringUtils.defaultString(rs.getString("CHALLENGED_CARD_RANK"));
                final String card_Rank = StringUtils.defaultString(rs.getString("CARD_RANK"));
                final String challenged_Card_Name = StringUtils.defaultString(rs.getString("CHALLENGED_CARD_NAME"));
                final String card_Name = StringUtils.defaultString(rs.getString("CARD_NAME"));
                final String challenged_Card_Remark = StringUtils.defaultString(rs.getString("CHALLENGED_CARD_REMARK"));
                final String card_Remark = StringUtils.defaultString(rs.getString("CARD_REMARK"));
                final String guardName = StringUtils.defaultString(rs.getString("GUARD_NAME"));
                final String guardKana = StringUtils.defaultString(rs.getString("GUARD_KANA"));
                final String relationship = StringUtils.defaultString(rs.getString("GUARD_RELATIONSHIP_NAME"));
                final String guardZipCd = StringUtils.defaultString(rs.getString("GUARD_ZIPCD"));
                final String guardAddr1 = StringUtils.defaultString(rs.getString("GUARD_ADDR1"));
                final String guardAddr2 = StringUtils.defaultString(rs.getString("GUARD_ADDR2"));
                final String writingDate = StringUtils.defaultString(rs.getString("WRITING_DATE"));
                final String challengedNames = StringUtils.defaultString(rs.getString("CHALLENGED_NAMES"));
                final String recordStaffName = StringUtils.defaultString(rs.getString("RECORD_STAFFNAME"));


                student = new StudentLoc(grade, schoolkind_Name, grade_Cd, grade_Name1, grade_Name2,
                                                     schregno, name, name_Kana, sex, birthday, zipCd, addr1, addr2, telno, coursecd, coursename, hrClassName2, hrNameAbbv, ghr_Cd, ghr_Name, ghr_Nameabbv, ghr_Attendno, challenged_Card_Class, card_Class, challenged_Card_Rank,
                                                     card_Rank, challenged_Card_Name, card_Name, challenged_Card_Remark, card_Remark,
                                                     guardName, guardKana, relationship, guardZipCd, guardAddr1, guardAddr2, writingDate, challengedNames, recordStaffName
                                                    );

                SchregChallengedSupportplanMain.setSchregChallengedSupportplanMain(db2, _param, student);
                SchregChallengedSupportplanStatus.setSchregChallengedSupportplanStatus(db2, _param, student);
                SchregChallengedSupportPlanRecordDat.setSchregChallengedSupportPlanRecordDat(db2, _param, student);
            }

        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return student;
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH MXSCHREG AS ( ");
        stb.append(" SELECT ");
        stb.append("     YEAR, ");
        stb.append("     MAX(SEMESTER) AS SEMESTER, ");
        stb.append("     SCHREGNO ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + _param._ctrlYear + "' ");
        stb.append(" GROUP BY ");
        stb.append("     YEAR, ");
        stb.append("     SCHREGNO ");
        stb.append(" ), MXSCHADDR AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     MAX(T1.ISSUEDATE) AS ISSUEDATE ");
        stb.append(" FROM ");
        stb.append("     SCHREG_ADDRESS_DAT T1 ");
        stb.append("     LEFT JOIN (SELECT SCHREGNO, MAX(GRADE) AS GRADE FROM SCHREG_REGD_DAT WHERE YEAR = '" + _param._ctrlYear + "' GROUP BY SCHREGNO) T3 ");
        stb.append("       ON T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("      AND GDAT.GRADE = T3.GRADE ");
        stb.append("     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("      AND T2.SCHOOL_KIND = GDAT.SCHOOL_KIND ");
        stb.append(" WHERE ");
        stb.append("     T1.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
        stb.append("     AND (T1.EXPIREDATE IS NULL OR T2.ENT_DATE < T1.EXPIREDATE) AND T1.ISSUEDATE <= '" + _param._ctrlDate.replace('/', '-') + "' ");
        stb.append(" GROUP BY ");
        stb.append("     T1.SCHREGNO ");
        stb.append(" ), GUARD_ADDRESS AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.GUARD_ZIPCD, ");
        stb.append("     T1.GUARD_ADDR1, ");
        stb.append("     T1.GUARD_ADDR2, ");
        stb.append("     T1.GUARD_TELNO, ");
        stb.append("     T1.GUARD_ADDR_FLG ");
        stb.append(" FROM  GUARDIAN_ADDRESS_DAT T1 ");
        stb.append(" INNER JOIN (SELECT T1.SCHREGNO, MAX(T1.ISSUEDATE) AS ISSUEDATE ");
        stb.append("             FROM GUARDIAN_ADDRESS_DAT T1 ");
        stb.append("             WHERE FISCALYEAR(T1.ISSUEDATE) <= '" + _param._ctrlYear + "' ");
        stb.append("             GROUP BY T1.SCHREGNO ");
        stb.append("            ) T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.ISSUEDATE = T1.ISSUEDATE ");
        stb.append(" WHERE T1.SCHREGNO IN (SELECT SCHREGNO FROM MXSCHREG) ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("   A023.ABBV1 AS SCHOOLKIND_NAME, ");
        stb.append("   T2.GRADE, ");
        stb.append("   T5.GRADE_CD, ");
        stb.append("   T5.GRADE_NAME1, ");
        stb.append("   T5.GRADE_NAME2, ");
        stb.append("   T2WK.SCHREGNO, ");
        stb.append("   T3.NAME, ");
        stb.append("   T3.NAME_KANA, ");
        stb.append("   Z002.ABBV1 AS SEX, ");
        stb.append("   T3.BIRTHDAY, ");
        stb.append("   VALUE(T6.ZIPCD, '') AS ZIPCD, ");  //本人住所(郵便番号)
        stb.append("   VALUE(T6.ADDR1, '') AS ADDR1, ");  //本人住所(住所)
        stb.append("   CASE WHEN T6.ADDR_FLG = '1' THEN VALUE(T6.ADDR2, '') ELSE '' END AS ADDR2, ");  //本人住所(方書き)
        stb.append("   T6.TELNO, ");
        stb.append("   T2.COURSECD, ");
        stb.append("   T14.COURSENAME, ");
        stb.append("   T4.HR_CLASS_NAME2, ");
        stb.append("   T4.HR_NAMEABBV, ");
        stb.append("   T15.GHR_CD, ");
        stb.append("   T16.GHR_NAME, ");
        stb.append("   T16.GHR_NAMEABBV, ");
        stb.append("   T15.GHR_ATTENDNO, ");
        stb.append("   T7.CHALLENGED_CARD_CLASS, ");
        stb.append("   E031.NAME1 AS CARD_CLASS, ");
        stb.append("   T7.CHALLENGED_CARD_RANK, ");
        stb.append("   E032.NAME1 AS CARD_RANK, ");
        stb.append("   T7.CHALLENGED_CARD_NAME, ");
        stb.append("   E061.NAME1 AS CARD_NAME, ");
        stb.append("   T7.CHALLENGED_CARD_REMARK, ");
        stb.append("   E063.NAME1 AS CARD_REMARK, ");
        stb.append("   T11.GUARD_NAME, ");
        stb.append("   T11.GUARD_KANA, ");   //保護者氏名ふりがな
        stb.append("   T11.RELATIONSHIP, ");
        stb.append("   H201.NAME1 AS GUARD_RELATIONSHIP_NAME, ");  //続柄
        stb.append("   T17.GUARD_ZIPCD, ");  //保護者住所(郵便番号)
        stb.append("   T17.GUARD_ADDR1, ");  //保護者住所(住所)
        stb.append("   CASE WHEN T17.GUARD_ADDR_FLG = '1' THEN VALUE(T17.GUARD_ADDR2, '') ELSE '' END AS GUARD_ADDR2, ");  //保護者住所(方書き)
        stb.append("   T7.WRITING_DATE, ");
        stb.append("   T7.CHALLENGED_NAMES, ");
        stb.append("   T13.RECORD_STAFFNAME ");
        stb.append(" FROM ");
        stb.append("   MXSCHREG T2WK ");
        stb.append("   LEFT JOIN SCHREG_REGD_DAT T2 ");
        stb.append("     ON T2.YEAR = T2WK.YEAR ");
        stb.append("    AND T2.SEMESTER = T2WK.SEMESTER ");
        stb.append("    AND T2.SCHREGNO = T2WK.SCHREGNO ");
        stb.append("   LEFT JOIN SCHREG_BASE_MST T3 ");
        stb.append("     ON T3.SCHREGNO = T2.SCHREGNO ");
        stb.append("   LEFT JOIN SCHREG_REGD_HDAT T4 ");
        stb.append("     ON T4.YEAR = T2.YEAR ");
        stb.append("    AND T4.SEMESTER = T2.SEMESTER ");
        stb.append("    AND T4.GRADE = T2.GRADE ");
        stb.append("    AND T4.HR_CLASS = T2.HR_CLASS ");
        stb.append("   LEFT JOIN SCHREG_REGD_GDAT T5 ");
        stb.append("     ON T5.YEAR = T2.YEAR ");
        stb.append("    AND T5.GRADE = T2.GRADE ");
        stb.append("   LEFT JOIN SCHREG_ADDRESS_DAT T6 ");
        stb.append("     ON T6.SCHREGNO = T2WK.SCHREGNO ");
        stb.append("    AND T6.ISSUEDATE = (SELECT TW.ISSUEDATE FROM MXSCHADDR TW WHERE TW.SCHREGNO = T6.SCHREGNO) ");
        stb.append("   LEFT JOIN SCHREG_CHALLENGED_PROFILE_MAIN_DAT T7 ");
        stb.append("     ON T7.SCHREGNO = T2WK.SCHREGNO ");
        stb.append("    AND T7.RECORD_DATE = (SELECT MAX(T7WK.RECORD_DATE) FROM SCHREG_CHALLENGED_PROFILE_MAIN_DAT T7WK WHERE T7WK.SCHREGNO = T2WK.SCHREGNO) ");
        stb.append("   LEFT JOIN NAME_MST A023 ");
        stb.append("     ON A023.NAMECD1 = 'A023' ");
        stb.append("    AND A023.NAME1 = T5.SCHOOL_KIND ");
        stb.append("   LEFT JOIN NAME_MST Z002 ");
        stb.append("     ON Z002.NAMECD1 = 'Z002' ");
        stb.append("    AND Z002.NAMECD2 = T3.SEX ");
        stb.append("   LEFT JOIN NAME_MST E031 ");
        stb.append("     ON E031.NAMECD1 = 'E031' ");
        stb.append("    AND E031.NAMECD2 = T7.CHALLENGED_CARD_CLASS ");
        stb.append("   LEFT JOIN NAME_MST E032 ");
        stb.append("     ON E032.NAMECD1 = 'E032' ");
        stb.append("    AND E032.NAMECD2 = T7.CHALLENGED_CARD_RANK ");
        stb.append("   LEFT JOIN NAME_MST E061 ");
        stb.append("     ON E061.NAMECD1 = 'E061' ");
        stb.append("    AND E061.NAMECD2 = T7.CHALLENGED_CARD_NAME ");
        stb.append("   LEFT JOIN NAME_MST E063 ");
        stb.append("     ON E063.NAMECD1 = 'E063' ");
        stb.append("    AND E063.NAMECD2 = T7.CHALLENGED_CARD_REMARK ");
        stb.append("   LEFT JOIN GUARDIAN_DAT T11 ");
        stb.append("     ON T11.SCHREGNO = T2WK.SCHREGNO ");
        stb.append("   LEFT JOIN SCHREG_CHALLENGED_ASSESSMENT_MAIN_DAT T13 ");
        stb.append("     ON T13.SCHREGNO     = T2WK.SCHREGNO ");
        stb.append("    AND T13.WRITING_DATE = T7.WRITING_DATE ");
        stb.append("   LEFT JOIN COURSE_MST T14 ON T14.COURSECD = T2.COURSECD ");
        stb.append("   LEFT JOIN SCHREG_REGD_GHR_DAT T15 ");
        stb.append("     ON T15.SCHREGNO = T2.SCHREGNO ");
        stb.append("    AND T15.YEAR = T2.YEAR ");
        stb.append("    AND T15.SEMESTER = T2.SEMESTER ");
        stb.append("   LEFT JOIN SCHREG_REGD_GHR_HDAT T16 ");
        stb.append("     ON T16.YEAR = T15.YEAR ");
        stb.append("    AND T16.SEMESTER = T15.SEMESTER ");
        stb.append("    AND T16.GHR_CD = T15.GHR_CD ");
        stb.append("   LEFT JOIN GUARD_ADDRESS T17 ");
        stb.append("     ON T17.SCHREGNO = T2WK.SCHREGNO ");
        stb.append("   LEFT JOIN NAME_MST H201 ");
        stb.append("     ON H201.NAMECD1 = 'H201' AND H201.NAMECD2 = T11.RELATIONSHIP ");
        stb.append(" WHERE ");
        stb.append("    T2WK.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("    AND T2WK.SCHREGNO = ? ");

        return stb.toString();
    }

    private class StudentLoc {
        final String _grade;
        final String _schoolkind_Name;
        final String _grade_Cd;
        final String _grade_Name1;
        final String _grade_Name2;
        final String _schregno;
        final String _name;
        final String _name_Kana;
        final String _sex;
        final String _birthday;
        final String _zipCd;
        final String _addr1;
        final String _addr2;
        final String _telno;
        final String _coursecd;
        final String _coursename;
        final String _hrClassName2;
        final String _hrNameAbbv;
        final String _ghr_Cd;
        final String _ghr_Name;
        final String _ghr_Nameabbv;
        final String _ghr_Attendno;
        final String _challenged_Card_Class;
        final String _card_Class;
        final String _challenged_Card_Rank;
        final String _card_Rank;
        final String _challenged_Card_Name;
        final String _card_Name;
        final String _challenged_Card_Remark;
        final String _card_Remark;
        final String _guardName;
        final String _guardKana;
        final String _relationship;
        final String _guardZipCd;
        final String _guardAddr1;
        final String _guardAddr2;
        final String _writingDate;
        final String _challengedNames;
        final String _recordStaffName;
        List _schregChallengedSupportplanRecord = new ArrayList();
        List _schregChallengedSupportplanMain = new ArrayList();
        List _schregChallengedSupportplanStatus = new ArrayList();


        public StudentLoc(
                final String grade, final String schoolkind_Name, final String grade_Cd, final String grade_Name1, final String grade_Name2, final String schregno, final String name, final String name_Kana, final String sex,
                final String birthday, final String zipCd, final String addr1, final String addr2, final String telno, final String coursecd, final String coursename, final String hrClassName2, final String hrNameAbbv, final String ghr_Cd, final String ghr_Name,
                final String ghr_Nameabbv, final String ghr_Attendno, final String challenged_Card_Class, final String card_Class, final String challenged_Card_Rank, final String card_Rank, final String challenged_Card_Name,
                final String card_Name, final String challenged_Card_Remark, final String card_Remark, final String guardName,final String guardKana, final String relationship, final String guardZipCd, final String guardAddr1,
                final String guardAddr2, final String writingDate, final String challengedNames, final String recordStaffName
                ) {
            _grade = grade;
            _schoolkind_Name = schoolkind_Name;
            _grade_Cd = grade_Cd;
            _grade_Name1 = grade_Name1;
            _grade_Name2 = grade_Name2;
            _schregno = schregno;
            _name = name;
            _name_Kana = name_Kana;
            _sex = sex;
            _birthday = birthday;
            _zipCd=zipCd;
            _addr1=addr1;
            _addr2=addr2;
            _telno = telno;
            _coursecd = coursecd;
            _coursename = coursename;
            _hrClassName2 = hrClassName2;
            _hrNameAbbv = hrNameAbbv;
            _ghr_Cd = ghr_Cd;
            _ghr_Name = ghr_Name;
            _ghr_Nameabbv = ghr_Nameabbv;
            _ghr_Attendno = ghr_Attendno;
            _challenged_Card_Class = challenged_Card_Class;
            _card_Class = card_Class;
            _challenged_Card_Rank = challenged_Card_Rank;
            _card_Rank = card_Rank;
            _challenged_Card_Name = challenged_Card_Name;
            _card_Name = card_Name;
            _challenged_Card_Remark = challenged_Card_Remark;
            _card_Remark = card_Remark;
            _guardName = guardName;
            _guardKana = guardKana;
               _relationship = relationship;
              _guardZipCd = guardZipCd;
               _guardAddr1 = guardAddr1;
               _guardAddr2 = guardAddr2;
            _writingDate = writingDate;
            _challengedNames = challengedNames;
            _recordStaffName = recordStaffName;
        }
    }

    private static class ChallengedSupportPlanKindName {
        final String _kind_No;
        final String _kind_Seq;
        final String _status_Name1;
        final String _status_Name2;
        final String _status_Name3;
        public ChallengedSupportPlanKindName (final String kind_No, final String kind_Seq, final String status_Name1, final String status_Name2, final String status_Name3)
        {
            _kind_No = kind_No;
            _kind_Seq = kind_Seq;
            _status_Name1 = status_Name1;
            _status_Name2 = status_Name2;
            _status_Name3 = status_Name3;
        }
        public static void setChallengedSupportPlanKindName(final DB2UDB db2, final Param param) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getChallengedSupportPlanKindNameSql(param);
                log.debug("getSchregChallengedSupportplanMainSql = "+sql);
                ps = db2.prepareStatement(sql);

                param._challengedTitle = new LinkedMap();
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String kind_No = rs.getString("KIND_NO");
                    final String kind_Seq = rs.getString("KIND_SEQ");
                    final String status_Name1 = rs.getString("STATUS_NAME1");
                    final String status_Name2 = rs.getString("STATUS_NAME2");
                    final String status_Name3 = rs.getString("STATUS_NAME3");
                    final ChallengedSupportPlanKindName addKk = new ChallengedSupportPlanKindName(kind_No, kind_Seq, status_Name1, status_Name2, status_Name3);

                    param._challengedTitle.put(kind_No, addKk);
                }
                DbUtils.closeQuietly(rs);

            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        private static String getChallengedSupportPlanKindNameSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   T1.KIND_NO, ");
            stb.append("   T1.KIND_SEQ, ");
            stb.append("   T1.STATUS_NAME1, ");
            stb.append("   T1.STATUS_NAME2, ");
            stb.append("   T1.STATUS_NAME3 ");
            stb.append(" FROM ");
            stb.append("   CHALLENGED_SUPPORTPLAN_KIND_NAME_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("   T1.YEAR = '" + param._ctrlYear + "' ");
            //stb.append("   AND (T1.KIND_NO = '01' OR T1.KIND_NO = '03') ");
            stb.append("   AND T1.KIND_SEQ = '000' ");
            stb.append(" ORDER BY T1.KIND_NO, T1.KIND_SEQ ");
            return stb.toString();
        }
    }
    private static class SchregChallengedSupportPlanRecordDat {
        final String _kind_No;
        final String _kind_Seq;
        final String _kind_Name;
        final String _descript1;
        final String _descript2;
        final String _descript3;
        public SchregChallengedSupportPlanRecordDat (final String kind_No, final String kind_Seq, final String kind_Name, final String descript1, final String descript2, final String descript3)
        {
            _kind_No = kind_No;
            _kind_Seq = kind_Seq;
            _kind_Name = kind_Name;
            _descript1 = descript1;
            _descript2 = descript2;
            _descript3 = descript3;
        }
        public String getKindSeqNo() {
            return _kind_Seq.substring(1,3);
        }
        public static void setSchregChallengedSupportPlanRecordDat(final DB2UDB db2, final Param param, final StudentLoc student) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getSchregChallengedSupportPlanRecordDatSql(param, student._schregno);
                log.debug("getSchregChallengedSupportplanMainSql = "+sql);
                ps = db2.prepareStatement(sql);

                student._schregChallengedSupportplanRecord = new ArrayList();

                rs = ps.executeQuery();
                while (rs.next()) {
                    final String kind_No = rs.getString("KIND_NO");
                    final String kind_Seq = rs.getString("KIND_SEQ");
                    final String kind_Name = rs.getString("KIND_NAME");
                    final String descript1 = rs.getString("DESCRIPT1");
                    final String descript2 = rs.getString("DESCRIPT2");
                    final String descript3 = rs.getString("DESCRIPT3");
                    final SchregChallengedSupportPlanRecordDat addKk = new SchregChallengedSupportPlanRecordDat(kind_No, kind_Seq, kind_Name, descript1, descript2, descript3);

                    student._schregChallengedSupportplanRecord.add(addKk);
                }
                DbUtils.closeQuietly(rs);

            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        private static String getSchregChallengedSupportPlanRecordDatSql(final Param param, final String schregno) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCSS_DAT AS ( ");
            stb.append(" SELECT ");
            stb.append("   * ");
            stb.append(" FROM ");
            stb.append("   SCHREG_CHALLENGED_SUPPORTPLAN_STATUS_DAT ");
            stb.append(" WHERE ");
            stb.append("   YEAR = '" + param._ctrlYear + "' ");
            stb.append("   AND SCHREGNO = '" + schregno + "' ");
            stb.append("   AND RECORD_DATE = (SELECT MAX(RECORD_DATE) FROM SCHREG_CHALLENGED_SUPPORTPLAN_STATUS_DAT WHERE YEAR = '" + param._ctrlYear + "' AND SCHREGNO = '" + schregno + "') ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.KIND_NO, ");
            stb.append("     T1.KIND_SEQ, ");
            stb.append("     T1.KIND_NAME, ");
            stb.append("     T2.STATUS AS DESCRIPT1, ");
            stb.append("     T2.STATUS2 AS DESCRIPT2, ");
            stb.append("     T2.STATUS3 AS DESCRIPT3 ");
            stb.append(" FROM ");
            stb.append("     CHALLENGED_SUPPORTPLAN_KIND_NAME_DAT T1 ");
            stb.append("     LEFT JOIN SCSS_DAT T2 ");
            stb.append("       ON T2.YEAR = T1.YEAR  AND T2.SCHREGNO = '" + schregno + "' AND T2.DATA_DIV = SUBSTR(T1.KIND_SEQ, 2,2) ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._ctrlYear + "'   AND ");
            stb.append("     T1.KIND_NO = '03'   AND ");
            stb.append("     T1.KIND_SEQ <> '000' ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("  T1.KIND_NO, ");
            stb.append("  T1.KIND_SEQ, ");
            stb.append("  T1.KIND_NAME, ");
            stb.append("  T2.HOPE1 AS DESCRIPT1, ");
            stb.append("  T2.HOPE2 AS DESCRIPT2, ");
            stb.append("  NULL AS DESCRIPT3 ");
            stb.append(" FROM ");
            stb.append("  CHALLENGED_SUPPORTPLAN_KIND_NAME_DAT T1 ");
            stb.append("  LEFT JOIN SCHREG_CHALLENGED_SUPPORTPLAN_RECORD_DAT T2 ");
            stb.append("    ON T2.YEAR = T1.YEAR ");
            stb.append("   AND T2.SCHREGNO = '" + schregno + "' ");
            stb.append("   AND T2.DIV = SUBSTR(T1.KIND_SEQ, 2,2) ");
            stb.append("   AND T2.RECORD_DATE = (SELECT MAX(RECORD_DATE) FROM SCHREG_CHALLENGED_SUPPORTPLAN_RECORD_DAT L2 ");
            stb.append("                          WHERE L2.YEAR = T2.YEAR AND L2.SCHREGNO = T2.SCHREGNO AND L2.DIV = T2.DIV ) ");
            stb.append(" WHERE ");
            stb.append("  T1.YEAR = '" + param._ctrlYear + "' ");
            stb.append("  AND T1.KIND_NO = '01' ");
            stb.append("  AND T1.KIND_SEQ <> '000' ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("  T1.KIND_NO, ");
            stb.append("  T1.KIND_SEQ, ");
            stb.append("  T1.KIND_NAME, ");
            stb.append("  T2.GOALS AS DESCRIPT1, ");
            stb.append("  NULL AS DESCRIPT2, ");
            stb.append("  NULL AS DESCRIPT3 ");
            stb.append(" FROM ");
            stb.append("  CHALLENGED_SUPPORTPLAN_KIND_NAME_DAT T1 ");
            stb.append("  LEFT JOIN SCHREG_CHALLENGED_SUPPORTPLAN_RECORD_DAT T2 ");
            stb.append("    ON T2.YEAR = T1.YEAR ");
            stb.append("   AND T2.SCHREGNO = '" + schregno + "' ");
            stb.append("   AND T2.DIV = SUBSTR(T1.KIND_SEQ, 2,2) ");
            stb.append("   AND T2.RECORD_DATE = (SELECT MAX(RECORD_DATE) FROM SCHREG_CHALLENGED_SUPPORTPLAN_RECORD_DAT L2 ");
            stb.append("                          WHERE L2.YEAR = T2.YEAR AND L2.SCHREGNO = T2.SCHREGNO AND L2.DIV = T2.DIV ) ");
            stb.append(" WHERE ");
            stb.append("  T1.YEAR = '" + param._ctrlYear + "' ");
            stb.append("  AND T1.KIND_NO = '02' ");
            stb.append("  AND T1.KIND_SEQ <> '000' ");
            stb.append(" ORDER BY KIND_NO, KIND_SEQ ");
            return stb.toString();
        }
    }
    private static class SchregChallengedSupportplanMain {
        final String _ones_hope_present;
        final String _guardian_hope_present;
        final String _reasonable_accommodation;
        final String _support_goal;
        final String _record;
        final String _record_staffname;
        final String _record_date;

        public SchregChallengedSupportplanMain(
                final String ones_hope_present,
                final String guardian_hope_present,
                final String reasonable_accommodation,
                final String support_goal,
                final String record,
                final String record_staffname,
                final String record_date
        ) {
            _ones_hope_present = ones_hope_present;
            _guardian_hope_present = guardian_hope_present;
            _reasonable_accommodation = reasonable_accommodation;
            _support_goal = support_goal;
            _record = record;
            _record_staffname = record_staffname;
            _record_date = record_date;
        }

        public static void setSchregChallengedSupportplanMain(final DB2UDB db2, final Param param, final StudentLoc student) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getSchregChallengedSupportplanMainSql(param);
                log.debug("getSchregChallengedSupportplanMainSql = "+sql);
                ps = db2.prepareStatement(sql);

                student._schregChallengedSupportplanMain = new ArrayList();

                ps.setString(1, student._schregno);

                rs = ps.executeQuery();
                while (rs.next()) {
                    final String ones_hope_present = StringUtils.defaultString(rs.getString("ONES_HOPE_PRESENT"));
                    final String guardian_hope_present = StringUtils.defaultString(rs.getString("GUARDIAN_HOPE_PRESENT"));
                    final String reasonable_accommodation = StringUtils.defaultString(rs.getString("REASONABLE_ACCOMMODATION"));
                    final String support_goal = StringUtils.defaultString(rs.getString("SUPPORT_GOAL"));
                    final String record = StringUtils.defaultString(rs.getString("RECORD"));
                    final String record_staffname = StringUtils.defaultString(rs.getString("RECORD_STAFFNAME"));
                    final String record_date = StringUtils.defaultString(rs.getString("RECORD_DATE"));
                    final SchregChallengedSupportplanMain schregChallengedSupportplanMain = new SchregChallengedSupportplanMain(ones_hope_present, guardian_hope_present,
                                                                                                                                        reasonable_accommodation, support_goal, record, record_staffname, record_date);

                    student._schregChallengedSupportplanMain.add(schregChallengedSupportplanMain);
                }
                DbUtils.closeQuietly(rs);

            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private static String getSchregChallengedSupportplanMainSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   T1.* ");
            stb.append(" FROM ");
            stb.append("   SCHREG_CHALLENGED_SUPPORTPLAN_MAIN_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("       T1.YEAR        = '"+ param._ctrlYear +"' ");
            stb.append("   AND T1.SCHREGNO    = ? ");
            stb.append("   AND T1.RECORD_DATE = (SELECT MAX(RECORD_DATE) ");
            stb.append("                               FROM SCHREG_CHALLENGED_SUPPORTPLAN_MAIN_DAT ");
            stb.append("                              WHERE YEAR        = T1.YEAR ");
            stb.append("                                AND SCHREGNO    = T1.SCHREGNO ");
            stb.append("                                AND RECORD_DATE <> 'NEW' ");
            stb.append("                        ) ");
            return stb.toString();
        }
    }

    private static class SchregChallengedSupportplanStatus {
        final String _data_div;
        final String _status;
        final String _status2;
        final String _status3;

        public SchregChallengedSupportplanStatus(
                final String data_div,
                final String status,
                final String status2,
                final String status3
        ) {
            _data_div = data_div;
            _status = status;
            _status2 = status2;
            _status3 = status3;
        }

        public static void setSchregChallengedSupportplanStatus(final DB2UDB db2, final Param param, final StudentLoc student) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getSchregChallengedSupportplanStatusSql(param);
                log.debug("hreport sql = "+sql);
                ps = db2.prepareStatement(sql);

                student._schregChallengedSupportplanStatus = new ArrayList();

                ps.setString(1, student._schregno);

                rs = ps.executeQuery();
                while (rs.next()) {
                    final String data_div = StringUtils.defaultString(rs.getString("DATA_DIV"));
                    final String status = StringUtils.defaultString(rs.getString("STATUS"));
                    final String status2 = StringUtils.defaultString(rs.getString("STATUS2"));
                    final String status3 = StringUtils.defaultString(rs.getString("STATUS3"));
                    final SchregChallengedSupportplanStatus schregChallengedSupportplanStatus = new SchregChallengedSupportplanStatus(data_div, status, status2, status3);
                    student._schregChallengedSupportplanStatus.add(schregChallengedSupportplanStatus);
                }
                DbUtils.closeQuietly(rs);

            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private static String getSchregChallengedSupportplanStatusSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   T1.* ");
            stb.append(" FROM ");
            stb.append("   SCHREG_CHALLENGED_SUPPORTPLAN_STATUS_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("       T1.YEAR        = '"+ param._ctrlYear +"' ");
            stb.append("   AND T1.SCHREGNO    = ? ");
            stb.append("   AND T1.RECORD_DATE = (SELECT MAX(RECORD_DATE) ");
            stb.append("                               FROM SCHREG_CHALLENGED_SUPPORTPLAN_STATUS_DAT ");
            stb.append("                              WHERE YEAR     = T1.YEAR ");
            stb.append("                                AND SCHREGNO = T1.SCHREGNO ");
            stb.append("                                AND DATA_DIV = T1.DATA_DIV ");
            stb.append("                        ) ");
            stb.append(" ORDER BY T1.DATA_DIV ");
            return stb.toString();
        }
    }

    private static class Form {
        final Param _param;
        final Vrw32alp _svf;
        String _form1;
        String _form2;
        int _form2n;
        int _recMax2;
        int _recMax1;
        int _recMax;
        int recLine;
        boolean _isForm1;
        String _currentform;
        Map _fieldInfoMap;

        private void VrAttributen(final String field, final int gyo, final String data) {
            if (_param._isOutputDebug) {
                log.info(" VrAttribute(\"" + field + "\", 「" + data + "」)");
            }
            _svf.VrAttributen(field, gyo, data);
        }

        private void VrAttribute(final String field, final String data) {
            if (_param._isOutputDebug) {
                log.info(" VrAttribute(\"" + field + "\", 「" + data + "」)");
            }
            _svf.VrAttribute(field, data);
        }

        private void VrsOut(final String field, final String data) {
            if (_param._isOutputDebug) {
                log.info(" VrsOut(\"" + field + "\", 「" + data + "」)");
            }
            _svf.VrsOut(field, data);
        }

        private void VrsOutn(final String field, final int gyo, final String data) {
            _svf.VrsOutn(field, gyo, data);
        }

        private Form(final Param param, final Vrw32alp svf) {
            _param = param;
            _svf = svf;
        }

        protected void setForm(final String form, int div) {
            _svf.VrSetForm(form, div);
            log.info(" form " + form);
            if (null == _currentform || !_currentform.equals(form)) {
                _currentform = form;
                _fieldInfoMap = SvfField.getSvfFormFieldInfoMapGroupByName(_svf);
            }
        }

        private SvfField getSvfField(final String fieldname) {
            return (SvfField) _fieldInfoMap.get(fieldname);
        }

        protected void VrsOutSelect(final String[][] fieldLists, final String data) {
            final int datasize = KNJ_EditEdit.getMS932ByteLength(data);
            String[] fieldFound = null;
            boolean output = false;
            searchField:
            for (int i = 0; i < fieldLists.length; i++) {
                final String[] fieldnameList = fieldLists[i];
                int totalKeta = 0;
                int ketaMin = -1;
                for (int j = 0; j < fieldnameList.length; j++) {
                    final String fieldname = fieldnameList[j];
                    final SvfField svfField = getSvfField(fieldname);
                    if (null == svfField) {
                        continue searchField;
                    }
                    totalKeta += svfField._fieldLength;
                    if (ketaMin == -1) {
                        ketaMin = svfField._fieldLength;
                    } else {
                        ketaMin = Math.min(ketaMin, svfField._fieldLength);
                    }
                    fieldFound = fieldnameList;
                }
                if (datasize <= totalKeta) {
                    final List tokenList = KNJ_EditKinsoku.getTokenList(data, ketaMin); // fieldListの桁数はすべて同じ前提
                    if (tokenList.size() <= fieldnameList.length) {
                        for (int j = 0; j < Math.min(tokenList.size(), fieldnameList.length); j++) {
                            VrsOut(fieldnameList[j], (String) tokenList.get(j));
                        }
                        output = true;
                        break searchField;
                    }
                }
            }
            if (!output && null != fieldFound) {
                final String[] fieldnameList = fieldFound;
                int ketaMin = -1;
                for (int j = 0; j < fieldnameList.length; j++) {
                    final String fieldname = fieldnameList[j];
                    final SvfField svfField = getSvfField(fieldname);
                    if (ketaMin == -1) {
                        ketaMin = svfField._fieldLength;
                    } else {
                        ketaMin = Math.min(ketaMin, svfField._fieldLength);
                    }
                    fieldFound = fieldnameList;
                }
                final List tokenList = KNJ_EditKinsoku.getTokenList(data, ketaMin);
                for (int j = 0; j < Math.min(tokenList.size(), fieldnameList.length); j++) {
                    VrsOut(fieldnameList[j], (String) tokenList.get(j));
                }
                output = true;
            }
        }

        private int fieldKeta(final String fieldname) {
            SvfField field = (SvfField) _fieldInfoMap.get(fieldname);
            if (null == field) {
                if (_param._isOutputDebug) {
                    log.warn("no such field : " + fieldname);
                }
                return -1;
            }
            return field._fieldLength;
        }

        private void setForm2() {
            setForm(_form2, _form2n);
            _recMax = _recMax2;
            recLine = 0;
            _isForm1 = false;
        }

        private void VrEndRecord() {
            if (_param._isOutputDebug) {
                log.info(" VrEndRecord.");
            }
            _svf.VrEndRecord();
            recLine += 1;
            if (_recMax != -1 && recLine >= _recMax && null != _form2) {
                setForm2();
            }
        }
    }


    protected Map getUpdateHistMap(final DB2UDB db2, final String dataType, final String year, final String schregno, final int max, final String writingDate) {

        final StringBuffer sql = new StringBuffer();
        sql.append(" SELECT ");
        sql.append("   T1.RECORD_DATE AS UPDATE_DATE ");
        sql.append("   , T1.RECORD_STAFFNAME AS UPDATE_STAFFNAME ");
        sql.append("   , T1.CHALLENGED_NAMES ");
        sql.append(" FROM ");
        sql.append("   SCHREG_CHALLENGED_SUPPORTPLAN_MAIN_DAT T1 ");
        sql.append(" WHERE ");
        sql.append("   T1.YEAR = '" + year + "' ");
        sql.append("   AND T1.SCHREGNO = '" + schregno + "' ");
        sql.append("   AND T1.RECORD_DATE = ( ");
        sql.append("                         SELECT ");
        sql.append("                           MAX(TW.RECORD_DATE)");
        sql.append("                         FROM ");
        sql.append("                           SCHREG_CHALLENGED_SUPPORTPLAN_MAIN_DAT TW ");
        sql.append("                         WHERE ");
        sql.append("                           TW.YEAR = T1.YEAR ");
        sql.append("                           AND TW.SCHREGNO = T1.SCHREGNO ");
        sql.append("                        ) ");

        //log.debug(" update time sql = " + sql.toString());
        final List resultList = getRowList(db2, sql.toString());
        final Map rtn = new HashMap();

        String updDate = null;
        String updStaffName = null;
        String calName = null;
        if (resultList.size() > 0) {
            final Map row = (Map) resultList.get(0);
            updDate = getString("UPDATE_DATE", row);
            updStaffName = getString("UPDATE_STAFFNAME", row);
            calName = getString("CHALLENGED_NAMES", row);
        }
        rtn.put("UPDATE_STAFFNAME", updStaffName);
        rtn.put("UPDATE_DATE",  updDate);
        rtn.put("CHALLENGED_NAMES", calName);

        return rtn;
    }

    protected static List getRowList(final DB2UDB db2, final String sql) {
        final List rtn = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            final Integer[] idxs = new Integer[meta.getColumnCount() + 1];
            for (int col = 1; col <= meta.getColumnCount(); col++) {
                idxs[col] = new Integer(col);
            }
            while (rs.next()) {
                final Map m = new HashMap();
                for (int col = 1; col <= meta.getColumnCount(); col++) {
                    final String val = rs.getString(col);
                    m.put(meta.getColumnLabel(col), val);
                    m.put(idxs[col], val);
                }
                rtn.add(m);
            }
        } catch (SQLException e) {
            log.error("exception! sql = " + sql, e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtn;
    }


    protected static String getString(final String field, final Map m) {
        if (null == m) {
            log.info("unimplemented? " + field);
            return null;
        }
        if (m.isEmpty()) {
            return null;
        }
        if (!m.containsKey(field)) {
            try {
                throw new IllegalStateException("フィールドなし:" + field + ", " + m);
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        final String retStr = StringUtils.defaultString((String) m.get(field));
        return retStr;
    }



    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 77083 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

     /** パラメータクラス */

    private static class Param {
         final String[] _categorySelected;
         final String _ctrlYear;        //_year
         final String _ctrlSemester;    //_gakki
         final String _ctrlDate;
         KNJSchoolMst _knjSchoolMst;
         final boolean _isOutputDebug;
         final String _printHrClassType;

         Map _challengedTitle = new LinkedMap();

         Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
             _categorySelected = request.getParameterValues("category_selected");
             _ctrlYear = request.getParameter("YEAR");
             _ctrlSemester = request.getParameter("GAKKI");
             _ctrlDate = request.getParameter("CTRL_DATE");
             _knjSchoolMst = new KNJSchoolMst(db2, _ctrlYear);
             _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));
             _printHrClassType = request.getParameter("PRINT_HR_CLASS_TYPE");
             ChallengedSupportPlanKindName.setChallengedSupportPlanKindName(db2, this);
         }

         private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
             return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJE390' AND NAME = '" + propName + "' "));
         }


    }
}

// eof
