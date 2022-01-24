/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: d26aaf5140099c97dd83f1ac2aaa73bf6319ed27 $
 *
 * 作成日: 2019/06/27
 * 作成者: matsushima
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
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
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

public class KNJD429B {

    private static final Log log = LogFactory.getLog(KNJD429B.class);

    private static final String SEMEALL = "9";
    private static final String SELECT_CLASSCD_UNDER = "89";

    private static final String SDIV1990008 = "1990008"; //1学期評定
    private static final String SDIV2990008 = "2990008"; //2学期評定
    private static final String SDIV3990008 = "3990008"; //3学期評定
    private static final String SDIV9990008 = "9990008"; //学年評定

    private static final String ALL3 = "333333";
    private static final String ALL5 = "555555";
    private static final String ALL9 = "999999";

    private static final String HYOTEI_TESTCD = "9990009";

    private static final int JIRITSHU_PAGE_MAX_LINE = 60; //自立活動 1ページ辺りの最大行数

    private static final int GAKUSHU_PAGE_MAX_LINE = 60; //学習の記録 1ページ辺りの最大行数

    //学習の記録 3枠 印字サイズ
    private static final int GAKUSHU_3WAKU_JIRITU_KETA1 = 20; //学習の記録 自立活動 3枠 文字数(左)
    private static final int GAKUSHU_3WAKU_JIRITU_KETA2 = 34; //学習の記録 自立活動 3枠 文字数(中央・右)
    private static final int GAKUSHU_3WAKU_JIRITU_GYO = 20; //学習の記録 自立活動 3枠 行数
    private static final int GAKUSHU_3WAKU_SUBCLASS_KETA = 24; //学習の記録 各教科 3枠 文字数
    private static final int GAKUSHU_3WAKU_SUBCLASS_KETA3 = 30; //学習の記録 各教科 3枠 文字数3列目
       private static final int GAKUSHU_3WAKU_SUBCLASS_GYO = 25; //学習の記録 各教科 3枠 行数

    //学習の記録 2枠 印字サイズ
    private static final int GAKUSHU_2WAKU_JIRITU_KETA = 44; //学習の記録 自立活動 2枠 文字数
    private static final int GAKUSHU_2WAKU_JIRITU_GYO = 16; //学習の記録 自立活動 2枠 行数
    private static final int GAKUSHU_2WAKU_SUBCLASS_KETA = 40; //学習の記録 各教科 2枠 文字数
    private static final int GAKUSHU_2WAKU_SUBCLASS_GYO = 18; //学習の記録 各教科 2枠 行数

    private static final String FORM_A = "KNJD429B_1_A.frm";
    private static final String FORM_B = "KNJD429B_2_B.frm";
    private static final String FORM_C = "KNJD429B_3_C.frm";
    private static final String FORM_D = "KNJD429B_3_D.frm";
    private static final String FORM_E = "KNJD429B_3_E.frm";
    private static final String FORM_F = "KNJD429B_4_F.frm";

    private boolean _hasData;
    KNJEditString knjobj = new KNJEditString();

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
        final List studentList = getList(db2);
        //下段の出欠
        for (final Iterator rit = _param._attendRanges.values().iterator(); rit.hasNext();) {
            final DateRange range = (DateRange) rit.next();
            Attendance.load(db2, _param, studentList, range);
        }
        for (Iterator iterator = studentList.iterator(); iterator.hasNext();) {
            final Student student = (Student) iterator.next();
            if(_param._printSide1) {
                //表紙
                printHyoshi(db2, svf, student);

                //裏表紙
                printUraByoshi(db2, svf, student);
            }

            if(_param._printSide2) {
                //自立活動
                if("103".equals(_param._printPattern)) {
                    printJiritsukatsudo(db2, svf, student);
                }

                //学習の記録
                printGakushunoKiroku(db2, svf, student);
            }

            if(_param._printSide3) {
                //出欠の記録
                printAttend(svf, student);
            }

//            _hasData = true;
        }
    }

    //表紙
    private void printHyoshi(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        svf.VrSetForm(FORM_A, 1);

        svf.VrsOut("SCHOOL_LOGO", _param._schoolLogoImagePath); //校章

        svf.VrsOut("NENDO", _param._nendo); //年度
        svf.VrsOut("TITLE", _param.getHreportCondition("101", "REMARK10")); //表題
        svf.VrsOut("SCHOOL_NAME", _param._certifSchoolSchoolName); //学校名
        final String classShow = _param.getHreportCondition("104", "REMARK1"); //クラス表示 1:学年 2:年組(法定) 3:年組(実クラス)
        final String hrName = ("1".equals(classShow)) ? student._gradename : ("2".equals(classShow)) ? student._hrname : student._ghrname;
        svf.VrsOut("HR_NAME", student._coursename + "　" + hrName); //クラス
        final String nameField = KNJ_EditEdit.getMS932ByteLength(student._name) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(student._name) > 18 ? "2" : "1";
        svf.VrsOut("NAME" + nameField, student._name); //生徒氏名
        final String pnameField = KNJ_EditEdit.getMS932ByteLength(_param._certifSchoolPrincipalName) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(_param._certifSchoolPrincipalName) > 20 ? "2" : "1";
        svf.VrsOut("PRINCIPAL_NAME" + pnameField, _param._certifSchoolPrincipalName); //校長名

        //担任氏名
        int line = 1;
        final List trNameList = getSchregTrName(db2, student._schregno);
        for (Iterator it = trNameList.iterator(); it.hasNext();) {
            final String trName = (String) it.next();
            final String trNameField = KNJ_EditEdit.getMS932ByteLength(trName) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(trName) > 20 ? "2" : "1";
            svf.VrsOutn("STAFF_NAME" + trNameField, line, trName);
            line++;
        }

        svf.VrEndPage();
        _hasData = true;
    }

    //裏表紙
    private void printUraByoshi(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        svf.VrSetForm(FORM_B, 1);

        //修了証書
        if(_param._output1) {
            svf.VrsOut("TEXT", student._coursename + "　" + student._gradename + "の課程を修了したことを証する。"); //文言
            final String[] date = KNJ_EditDate.tate_format4(db2, _param._ctrlDate.replace('/', '-'));
            svf.VrsOut("DATE", date[0] + "　　年　　月　　日"); //年度
            svf.VrsOut("SCHOOL_NAME2", _param._certifSchoolSchoolName); //学校名
            svf.VrsOut("JOB_NAME", _param._certifSchoolJobName); //役職名
            final String pnameField = KNJ_EditEdit.getMS932ByteLength(_param._certifSchoolPrincipalName) > 26 ? "_3" : KNJ_EditEdit.getMS932ByteLength(_param._certifSchoolPrincipalName) > 20 ? "_2" : "_1";
            svf.VrsOut("PRINCIPAL_NAME2" + pnameField, _param._certifSchoolPrincipalName); //校長名
        } else {
            if (null != _param._whiteSpaceImagePath) {
                svf.VrsOut("MASK", _param._whiteSpaceImagePath);
            }
        }
        svf.VrEndPage();
        _hasData = true;
    }

    //自立活動
    private void printJiritsukatsudo(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        svf.VrSetForm(FORM_E, 4);
        int line = 1;
        final String kindNo = "30";

        svf.VrsOut("TITLE", _param._semester + "学期の記録"); //タイトル
        svf.VrsOut("NAME", student._gradename + "　" + student._name); //学年 + 生徒氏名


        //自立活動Map
        final Map selfrelianceMap = getSchregSelfrelianceRemark(db2, student._schregno);

        //9学期
        if(selfrelianceMap.containsKey(SEMEALL)) {
            final Map map = (Map) selfrelianceMap.get(SEMEALL);
            if(map != null) {
                List content1 = KNJ_EditKinsoku.getTokenList(getMapString(map, "KEY_GOALS"), 40, 20); //内容1-2
                final String con2 = _param.getKindName(kindNo, "004") + "\n" + getMapString(map, "GOALS_REASON"); //項目1-3 + 内容1-3
                List content2 = KNJ_EditKinsoku.getTokenList(con2, 40, 20); //1行目:項目1-3 以降:内容1-3
                final int maxLen = (content1.size() < content2.size()) ? content2.size() : content1.size();

//                //出力
                if(maxLen > 0) {
                    String grp1_2 = "0"; //右辺ヘッダ用
                    for (int i = 0 ; i < maxLen; i++) {
                        //左辺
                        svf.VrsOut("GRP1_1", "1");
                        svf.VrsOut("CLASS_NAME1", _param.getKindName(kindNo, "003")); //項目1-2
                        if(i < content1.size()) svf.VrsOut("CONTENT1_1", (String) content1.get(i)); //内容1-2

                        //右辺
                        svf.VrsOut("GRP1_2", grp1_2);
                        final String field = (i == 0) ? "TITLE1" : "CONTENT1_2";
                        if(i < content2.size()) svf.VrsOut(field, (String) content2.get(i)); //項目1-3 or 内容1-3
                        svf.VrEndRecord();
                        _hasData = true;
                        line++;
                        grp1_2 = "1";
                    }
                }
            }
        }

        //指定学期
        if(selfrelianceMap.containsKey(_param._semester)) {
            final Map map = (Map) selfrelianceMap.get(_param._semester);
            if(map != null) {
                   svf.VrsOut("GRP2_1", "2");
                svf.VrsOut("GRP2_2", "2");
                svf.VrsOut("GRP2_3", "2");
                svf.VrsOut("CLASS_NAME2", "");
                svf.VrsOut("TITLE2_1", _param.getKindName(kindNo, "005")); //項目2-1
                svf.VrsOut("TITLE2_2", _param.getKindName(kindNo, "006")); //項目2-2
                svf.VrsOut("TITLE2_3", _param.getKindName(kindNo, "008")); //項目2-3

                //1ブロック目
                line = printJiritsuMeisai(svf, student, line, kindNo, "009", map, "1");

                //2ブロック目
                line = printJiritsuMeisai(svf, student, line, kindNo, "010", map, "2");

                //3ブロック目
                line = printJiritsuMeisai(svf, student, line, kindNo, "011", map, "3");

                //4ブロック目
                line = printJiritsuMeisai(svf, student, line, kindNo, "012", map, "4");
            }
        }

        svf.VrEndPage();
    }

    private int printJiritsuMeisai(final Vrw32alp svf, final Student student, final int param_line, final String kindNo, final String kindSeq, final Map map, final String column) {

        int line = param_line;
        //出力内容
        List content3_1 = KNJ_EditKinsoku.getTokenList(getMapString(map, "LONG_GOALS"+column), 16, 20);
        List content3_2 = KNJ_EditKinsoku.getTokenList(getMapString(map, "SHORT_GOALS"+column), 32, 20);
        List content3_3 = KNJ_EditKinsoku.getTokenList(getMapString(map, "EVALUATION"+column), 32, 20);

        //出力行数
        int maxLen = content3_1.size();
        if(maxLen < content3_2.size()) maxLen = content3_2.size();
        if(maxLen < content3_3.size()) maxLen = content3_3.size();

        //改ページの判定
        if(JIRITSHU_PAGE_MAX_LINE < line + maxLen) {
            printJiritsuTitle(svf, student, kindNo);
            line = 1;
        }

        //出力
        for (int i = 0 ; i < maxLen; i++) {
            svf.VrsOut("GRP3_1", column);
            svf.VrsOut("GRP3_2", column);
            svf.VrsOut("GRP3_3", column);
            svf.VrsOut("CLASS_NAME3", _param.getKindName(kindNo, kindSeq)); //教科名
            if(i < content3_1.size()) svf.VrsOut("CONTENT3_1", (String) content3_1.get(i)); //項目3-1
            if(i < content3_2.size()) svf.VrsOut("CONTENT3_2", (String) content3_2.get(i)); //項目3-2
            if(i < content3_3.size()) svf.VrsOut("CONTENT3_3", (String) content3_3.get(i)); //項目3-3
            svf.VrEndRecord();
            _hasData = true;
        }
        return line + maxLen;

    }

    private void printJiritsuTitle(final Vrw32alp svf, final Student student, final String kindNo) {
        svf.VrEndPage();
        svf.VrSetForm(FORM_E, 4);

        svf.VrsOut("TITLE", _param._semester + "学期の記録"); //タイトル
        svf.VrsOut("NAME", student._gradename + "　" + student._name); //学年 + 生徒氏名

        svf.VrsOut("TITLE2_1", _param.getKindName(kindNo, "005")); //項目2-1
        svf.VrsOut("TITLE2_2", _param.getKindName(kindNo, "006")); //項目2-2
        svf.VrsOut("TITLE2_3", _param.getKindName(kindNo, "008")); //項目2-23

    }

    //学習の記録
    private void printGakushunoKiroku(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        final boolean is3Waku = (!"102".equals(_param._printPattern)); //帳票パターン 102:2枠 それ以外:3枠
        final String frmName = (is3Waku)? FORM_C : FORM_D;
        svf.VrSetForm(frmName, 4);
        int line = 1;
        int maxLen = 0;
        String kindNo = "";

        svf.VrsOut("TITLE", _param._semester + "学期の記録"); //タイトル
        svf.VrsOut("NAME", student._gradename + "　" + student._name); //学年 + 生徒氏名

        //◆自立活動
        kindNo = "01"; //自立活動の項目名No
        final int jirituKeta = (is3Waku)? GAKUSHU_3WAKU_JIRITU_KETA1 : GAKUSHU_2WAKU_JIRITU_KETA;
        final int jirituKeta2 = (is3Waku)? GAKUSHU_3WAKU_JIRITU_KETA2 : GAKUSHU_2WAKU_JIRITU_KETA;
        final int jirituGyo = (is3Waku)? GAKUSHU_3WAKU_JIRITU_GYO : GAKUSHU_2WAKU_JIRITU_GYO;
        List content1 = KNJ_EditKinsoku.getTokenList(getSchregHreportRemark(db2, student._schregno, SEMEALL, "1", "GOALS"), 90, 4); //内容1
        List content2_1 = KNJ_EditKinsoku.getTokenList(getSchregHreportRemark(db2, student._schregno, _param._semester, "1", "REMARK"), jirituKeta, jirituGyo);
        final String con2Val = (is3Waku) ? getSchregHreportRemark(db2, student._schregno, _param._semester, "2", "REMARK") : "";
        List content2_2 = KNJ_EditKinsoku.getTokenList(con2Val, jirituKeta2, jirituGyo);
        List content2_3 = KNJ_EditKinsoku.getTokenList(getSchregHreportRemark(db2, student._schregno, _param._semester, "3", "REMARK"), jirituKeta2, jirituGyo);
        if(content1.size() != 0 || content2_1.size() != 0 || content2_2.size() != 0 || content2_3.size() != 0) {

            svf.VrsOut("SUBTITLE", "自立活動"); //サブタイトル
            line++; //サブタイトルの行数加算

            //上段
            if(content1.size() != 0) {
                svf.VrsOut("ITEM1", _param.getKindName(kindNo, "001")); //項目1
                line++; //項目名の行数加算

                for (int i = 0 ; i < content1.size(); i++) {
                    svf.VrsOut("GRP1_1", "1");
                    svf.VrsOut("GRP1_2", "1");
                    svf.VrsOut("GRP1_3", "1");
                    svf.VrsOut("CONTENT1", (String) content1.get(i));
                    svf.VrEndRecord();
                    _hasData = true;
                    line++;
                }
            }

            //下段
            maxLen = content2_1.size();
            if(maxLen < content2_2.size()) maxLen = content2_2.size();
            if(maxLen < content2_3.size()) maxLen = content2_3.size();
            if(maxLen != 0) {
                svf.VrsOut("GRP2_1_1", "2");
                svf.VrsOut("GRP2_1_2", "2");
                svf.VrsOut("GRP2_1_3", "2");
                if(is3Waku) {
                    //3枠
                    svf.VrsOut("ITEM2_1", _param.getKindName(kindNo, "003")); //項目2-1
                    svf.VrsOut("ITEM2_2", _param.getKindName(kindNo, "004")); //項目2-2
                    svf.VrsOut("ITEM2_3", _param.getKindName(kindNo, "005")); //項目2-3
                } else {
                    //2枠
                    svf.VrsOut("ITEM2_1", _param.getKindName(kindNo, "003")); //項目2-1
                    svf.VrsOut("ITEM2_2", _param.getKindName(kindNo, "005")); //項目2-2
                }
                line++; //項目名の行数加算

                for (int i = 0 ; i < maxLen; i++) {
                    svf.VrsOut("GRP2_2_1", "3");
                    svf.VrsOut("GRP2_2_2", "3");
                    svf.VrsOut("GRP2_2_3", "3");
                    if(is3Waku) {
                        //3枠
                        if(i < content2_1.size()) svf.VrsOut("CONTENT2_1", (String) content2_1.get(i)); //内容2-1
                        if(i < content2_2.size()) svf.VrsOut("CONTENT2_2", (String) content2_2.get(i)); //内容2-2
                        if(i < content2_3.size()) svf.VrsOut("CONTENT2_3", (String) content2_3.get(i)); //内容2-3
                    } else {
                        //2枠
                        if(i < content2_1.size()) svf.VrsOut("CONTENT2_1", (String) content2_1.get(i)); //内容2-1
                        if(i < content2_3.size()) svf.VrsOut("CONTENT2_2", (String) content2_3.get(i)); //内容2-2
                    }
                    svf.VrEndRecord();
                    _hasData = true;
                    line++;
                }
            }

            svf.VrsOut("BLANK", "BLANK"); //空白行
            svf.VrEndRecord();
            _hasData = true;
            line++;
        }


        //◆各教科の学習の記録
        kindNo = "03"; //学習の記録の項目名No
        final int subclassKeta = (is3Waku)? GAKUSHU_3WAKU_SUBCLASS_KETA : GAKUSHU_2WAKU_SUBCLASS_KETA;
        final int subclassGyo = (is3Waku)? GAKUSHU_3WAKU_SUBCLASS_GYO : GAKUSHU_2WAKU_SUBCLASS_GYO;
        List totalRemark = KNJ_EditKinsoku.getTokenList(getSchregHreportRemark(db2, student._schregno, _param._semester, "1", "TOTALREMARK"), 80, 25); //内容4-1
        if(student._hreportSchregSubclassRemarkDatList.size() != 0 || totalRemark.size() != 0) {

            svf.VrsOut("SUBTITLE", "各教科等"); //サブタイトル
            line++; //サブタイトルの行数加算

            //項目名
            svf.VrsOut("GRP3_1_1", "4");
            svf.VrsOut("GRP3_1_2", "4");
            svf.VrsOut("GRP3_1_3", "4");
            svf.VrsOut("GRP3_1_4", "4");
            svf.VrsOut("GRP3_1_5", "4");
            svf.VrsOut("GRP3_1_6", "4");
            svf.VrsOut("ITEM3_1", "教科等");
            if(is3Waku) {
                //3枠
                svf.VrsOut("ITEM3_2", _param.getKindName(kindNo, "001")); //項目3-1
                svf.VrsOut("ITEM3_3", _param.getKindName(kindNo, "002")); //項目3-2
                svf.VrsOut("ITEM3_4", _param.getKindName(kindNo, "003")); //項目3-3
            } else {
                //2枠
                svf.VrsOut("ITEM3_2", _param.getKindName(kindNo, "002")); //項目3-2
                svf.VrsOut("ITEM3_3", _param.getKindName(kindNo, "003")); //項目3-2
            }
            line++; //項目名の行数加算

            //内容
            int idx = 0;
            for (final Iterator it = student._hreportSchregSubclassRemarkDatList.iterator(); it.hasNext();) {
                final HreportSchregSubclassRemarkDat printData = (HreportSchregSubclassRemarkDat) it.next();

                //出力内容
                List subclassname = knjobj.retDividString(printData._subclassname, 2, subclassGyo);
                List r1List = KNJ_EditKinsoku.getTokenList(printData._remark1, subclassKeta, subclassGyo);
                List r2List = KNJ_EditKinsoku.getTokenList(printData._remark2, subclassKeta, subclassGyo);
                List r3List = KNJ_EditKinsoku.getTokenList(printData._remark3, subclassKeta, subclassGyo);
                if(is3Waku) {
                    r3List = KNJ_EditKinsoku.getTokenList(printData._remark3, GAKUSHU_3WAKU_SUBCLASS_KETA3, subclassGyo);
                }

                //出力行数
                maxLen = subclassname.size();
                if(maxLen < r1List.size()) maxLen = r1List.size();
                if(maxLen < r2List.size()) maxLen = r2List.size();
                if(maxLen < r3List.size()) maxLen = r3List.size();

                //改ページの判定
                if(GAKUSHU_PAGE_MAX_LINE < line + maxLen) {
                    line = printGakushuTitle(svf, student, kindNo, frmName, is3Waku);
                }

                //出力
                for (int i = 0 ; i < maxLen; i++) {
                    svf.VrsOut("GRP3_2_1", String.valueOf(idx));
                    svf.VrsOut("GRP3_2_2", String.valueOf(idx));
                    svf.VrsOut("GRP3_2_3", String.valueOf(idx));
                    svf.VrsOut("GRP3_2_4", String.valueOf(idx));
                    svf.VrsOut("GRP3_2_5", String.valueOf(idx));
                    svf.VrsOut("GRP3_2_6", String.valueOf(idx));
                    if(i < subclassname.size()) svf.VrsOut("CONTENT3_1", (String) subclassname.get(i)); //内容3-1
                    if(is3Waku) {
                        //3枠
                        if(i < r1List.size()) svf.VrsOut("CONTENT3_2", (String) r1List.get(i)); //内容3-2
                        if(i < r2List.size()) svf.VrsOut("CONTENT3_3", (String) r2List.get(i)); //内容3-3
                        if(i < r3List.size()) svf.VrsOut("CONTENT3_4", (String) r3List.get(i)); //内容3-4
                    } else {
                        //2枠
                        if(i < r2List.size()) svf.VrsOut("CONTENT3_2", (String) r2List.get(i)); //内容3-2
                        if(i < r3List.size()) svf.VrsOut("CONTENT3_3", (String) r3List.get(i)); //内容3-3
                    }
                    svf.VrEndRecord();
                    _hasData = true;
                    line++;
                }
                idx++;
            }

            //総合所見
            if(totalRemark.size() != 0) {
                //出力内容
                List subclassname = knjobj.retDividString(_param.getKindName(kindNo, "004"), 2, 25);

                //出力行数
                maxLen = subclassname.size();
                if(maxLen < totalRemark.size()) maxLen = totalRemark.size();

                //改ページの判定
                if(GAKUSHU_PAGE_MAX_LINE < line + maxLen) {
                    line = printGakushuTitle(svf, student, kindNo, frmName, is3Waku);
                }

                //出力
                for (int i = 0 ; i < maxLen; i++) {
                    svf.VrsOut("GRP4_1", "6");
                    svf.VrsOut("GRP4_2", "6");
                    svf.VrsOut("GRP4_3", "6");
                    svf.VrsOut("GRP4_4", "6");
                    if(i < subclassname.size()) svf.VrsOut("CONTENT4_1", (String) subclassname.get(i)); //内容4-1
                    if(i < totalRemark.size()) svf.VrsOut("CONTENT4_2", (String) totalRemark.get(i)); //内容4-2
                    svf.VrEndRecord();
                    _hasData = true;
                    line++;
                }
            }
        }

        svf.VrEndPage();
    }

    private int printGakushuTitle(final Vrw32alp svf, final Student student, final String kindNo, final String frmName, final boolean is3Waku) {
        int line = 1;
        svf.VrEndPage();
        svf.VrSetForm(frmName, 4);


        svf.VrsOut("TITLE", _param._semester + "学期の記録"); //タイトル
        svf.VrsOut("NAME", student._gradename + "　" + student._name); //学年 + 生徒氏名

        svf.VrsOut("SUBTITLE", "各教科等"); //サブタイトル
        line++; //サブタイトルの行数加算

        svf.VrsOut("GRP3_1_1", "4");
        svf.VrsOut("GRP3_1_2", "4");
        svf.VrsOut("GRP3_1_3", "4");
        svf.VrsOut("GRP3_1_4", "4");
        svf.VrsOut("GRP3_1_5", "4");
        svf.VrsOut("GRP3_1_6", "4");
        svf.VrsOut("ITEM3_1", "教科等");
        if(is3Waku) {
            //3枠
            svf.VrsOut("ITEM3_2", _param.getKindName(kindNo, "001")); //項目3-1
            svf.VrsOut("ITEM3_3", _param.getKindName(kindNo, "002")); //項目3-2
            svf.VrsOut("ITEM3_4", _param.getKindName(kindNo, "003")); //項目3-3
        } else {
            //2枠
            svf.VrsOut("ITEM3_2", _param.getKindName(kindNo, "002")); //項目3-2
            svf.VrsOut("ITEM3_3", _param.getKindName(kindNo, "003")); //項目3-2
        }
        line++; //項目名の行数加算

        return line;
    }

    //出欠の記録
    private void printAttend(final Vrw32alp svf, final Student student) {
        svf.VrSetForm(FORM_F, 1);

        svf.VrsOut("TITLE", _param._semester + "学期の記録"); //タイトル
        svf.VrsOut("NAME", student._gradename + "　" + student._name); //学年 + 生徒氏名

        for (final Iterator it = _param._semesterMap.keySet().iterator(); it.hasNext();) {
            final String semester = (String) it.next();
            final Attendance att = (Attendance) student._attendMap.get(semester);
            if("2".equals(semester) && !_param._semes2Flg) continue;
            if("3".equals(semester) && !_param._semes3Flg) continue;

            int line = 1;
            svf.VrsOut("SEMESTER2_"+semester, semester+"学期");   //学期名
            if (null != att) {
                svf.VrsOutn("ATTEND" + semester, line++, String.valueOf(att._lesson));  // 授業日数
                svf.VrsOutn("ATTEND" + semester, line++, String.valueOf(att._suspend + att._mourning)); // 忌引出停日数
                svf.VrsOutn("ATTEND" + semester, line++, String.valueOf(att._mLesson)); // 出席しなければならない日数
                svf.VrsOutn("ATTEND" + semester, line++, String.valueOf(att._absent));  // 欠席日数
                svf.VrsOutn("ATTEND" + semester, line++, String.valueOf(att._present)); // 出席日数
                VrsOutnRenban(svf, "ATTEND_REMARK" + semester, KNJ_EditKinsoku.getTokenList(att._remark, 20, 10)); //備考
                if(!"1".equals(_param.getHreportCondition("111", "REMARK1"))) {
                    if (semester.equals(_param._semester)) {
                        if (null != att._communication) {
                            VrsOutnRenban(svf, "FROM_SCHOOL", KNJ_EditKinsoku.getTokenList(att._communication, 90, 6)); //学校より
                        }
                    }
                } else {
                    if (null != _param._whiteSpaceImagePath) {
                        svf.VrsOut("BLANK1", _param._whiteSpaceImagePath);
                    }
                }
            }
        }
        svf.VrEndPage();
        _hasData = true;
    }

    private String getMapString(final Map map, final String key) {
        String rtnStr = "";
        if(map.containsKey(key)) {
            rtnStr = (String) map.get(key);
        }
        return rtnStr;
    }

    protected void VrsOutnRenban(final Vrw32alp svf, final String field, final List<String> value) {
        if (null != value) {
            for (int i = 0 ; i < value.size(); i++) {
                svf.VrsOutn(field, i + 1, value.get(i));
            }
        }
    }

    /**
 ]    * "[w] * [h]"サイズタイプのパラメータのwもしくはhを整数で返す
     * @param param サイズタイプのパラメータ文字列
     * @param pos split後のインデクス (0:w, 1:h)
     * @return "[w] * [h]"サイズタイプのパラメータのwもしくはhの整数値
     */
    private static int getParamSizeNum(final String param, final int pos) {
        int num = -1;
        String[] nums = StringUtils.split(param, " * ");
        if (StringUtils.isBlank(param) || !(0 <= pos && pos < nums.length)) {
            num = -1;
        } else {
            try {
                num = Integer.valueOf(nums[pos]).intValue();
            } catch (Exception e) {
                log.error("Exception!", e);
            }
        }
        return num;
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getStudentSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final Student student = new Student();
                student._schregno = rs.getString("SCHREGNO");
                student._name = rs.getString("NAME");
                student._schoolKind = rs.getString("SCHOOL_KIND");
                student._gradeCd = rs.getString("GRADE_CD");
                student._hrname = StringUtils.defaultString(rs.getString("HR_NAME"));
                student._ghrname = StringUtils.defaultString(rs.getString("GHR_NAME"));
                student._trCd1 = StringUtils.defaultString(rs.getString("TR_CD1"));
                student._staffname = StringUtils.defaultString(rs.getString("STAFFNAME"));
                student._attendno = NumberUtils.isDigits(rs.getString("ATTENDNO")) ? String.valueOf(Integer.parseInt(rs.getString("ATTENDNO"))) + "番" : rs.getString("ATTENDNO");
                student._grade = rs.getString("GRADE");
                student._gradename = StringUtils.defaultString(rs.getString("GRADE_NAME"));
                student._hrClass = rs.getString("HR_CLASS");
                student._coursecd = rs.getString("COURSECD");
                student._majorcd = rs.getString("MAJORCD");
                student._course = rs.getString("COURSE");
                student._coursename = rs.getString("COURSENAME");
                student._majorname = rs.getString("MAJORNAME");
                student._hrClassName1 = rs.getString("HR_CLASS_NAME1");
                student._entyear = rs.getString("ENT_YEAR");

                student._hreportSchregSubclassRemarkDatList = HreportSchregSubclassRemarkDat.getHreportSchregSubclassRemarkDat(db2, _param, student._schregno);
                retList.add(student);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();
        // 異動者を除外した学籍の表 => 組平均において異動者の除外に使用
        stb.append("WITH SCHNO_A AS(");
        stb.append("    SELECT  T1.YEAR, T1.SEMESTER, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T1.SCHREGNO, T1.COURSECD, T1.MAJORCD, T1.COURSECODE ");
        stb.append("    FROM    SCHREG_REGD_DAT T1,SEMESTER_MST T2 ");
        stb.append("    WHERE   T1.YEAR = '" + _param._loginYear + "' ");
        if (SEMEALL.equals(_param._semester)) {
            stb.append("     AND T1.SEMESTER = '" + _param._loginSemester + "' ");
        } else {
            stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        }
        stb.append("        AND T1.YEAR = T2.YEAR ");
        stb.append("        AND T1.SEMESTER = T2.SEMESTER ");
        stb.append("        AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected));

        //                      在籍チェック:転学(2)・退学(3)者は除外 但し異動日が学期終了日または異動基準日より小さい場合
        //                                   転入(4)・編入(5)者は除外 但し異動日が学期終了日または異動基準日より大きい場合
        stb.append("        AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
        stb.append("                       WHERE   S1.SCHREGNO = T1.SCHREGNO ");
        stb.append("                           AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '" + _param._date + "' THEN T2.EDATE ELSE '" + _param._date + "' END) ");
        stb.append("                             OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '" + _param._date + "' THEN T2.EDATE ELSE '" + _param._date + "' END)) ) ");
//        //                      異動者チェック：留学(1)・休学(2)者は除外 但し学期終了日または基準日が異動開始日と終了日内にある場合
//        stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
//        stb.append(                       "WHERE   S1.SCHREGNO = T1.SCHREGNO ");
//        stb.append(                           "AND S1.TRANSFERCD IN ('1','2') AND CASE WHEN T2.EDATE < '" + parameter._date + "' THEN T2.EDATE ELSE '" + parameter._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
        //                      異動者チェック：休学(2)者は除外 但し学期終了日または基準日が異動開始日と終了日内にある場合
        stb.append("        AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
        stb.append("                       WHERE   S1.SCHREGNO = T1.SCHREGNO ");
        stb.append("                           AND S1.TRANSFERCD IN ('2') AND CASE WHEN T2.EDATE < '" + _param._date + "' THEN T2.EDATE ELSE '" + _param._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
        stb.append("    ) ");

        //メイン表
        stb.append("     SELECT  REGD.SCHREGNO");
        stb.append("            ,REGD.SEMESTER ");
        stb.append("            ,BASE.NAME ");
        stb.append("            ,REGDG.SCHOOL_KIND ");
        stb.append("            ,REGDG.GRADE_CD ");
        stb.append("            ,REGDG.GRADE_NAME1 AS GRADE_NAME ");
        stb.append("            ,REGDH.HR_NAME ");
        stb.append("            ,GHRH.GHR_NAME ");
        stb.append("            ,REGDH.TR_CD1 ");
        stb.append("            ,STF1.STAFFNAME ");
        stb.append("            ,REGD.ATTENDNO ");
        stb.append("            ,REGD.GRADE ");
        stb.append("            ,REGD.HR_CLASS ");
        stb.append("            ,REGD.COURSECD ");
        stb.append("            ,REGD.MAJORCD ");
        stb.append("            ,REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE AS COURSE");
        stb.append("            ,COURSE.COURSENAME ");
        stb.append("            ,MAJOR.MAJORNAME ");
        stb.append("            ,REGDH.HR_CLASS_NAME1 ");
        stb.append("            ,FISCALYEAR(BASE.ENT_DATE) AS ENT_YEAR ");
        stb.append("      FROM    SCHNO_A REGD ");
        stb.append("      LEFT JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = REGD.YEAR ");
        stb.append("                   AND REGDG.GRADE = REGD.GRADE ");
        stb.append("      LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR ");
        stb.append("                   AND REGDH.SEMESTER = REGD.SEMESTER ");
        stb.append("                   AND REGDH.GRADE = REGD.GRADE ");
        stb.append("                   AND REGDH.HR_CLASS = REGD.HR_CLASS ");
        stb.append("      LEFT JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
        stb.append("      LEFT JOIN STAFF_MST STF1 ON STF1.STAFFCD = REGDH.TR_CD1 ");
        stb.append("      LEFT JOIN COURSE_MST COURSE ON COURSE.COURSECD = REGD.COURSECD ");
        stb.append("      LEFT JOIN MAJOR_MST MAJOR ON MAJOR.COURSECD = REGD.COURSECD ");
        stb.append("                   AND MAJOR.MAJORCD = REGD.MAJORCD ");
        stb.append("      LEFT JOIN SCHREG_REGD_GHR_DAT GHRD ");
        stb.append("             ON GHRD.YEAR     = REGD.YEAR ");
        stb.append("            AND GHRD.SEMESTER = REGD.SEMESTER ");
        stb.append("            AND GHRD.SCHREGNO = REGD.SCHREGNO ");
        stb.append("      LEFT JOIN SCHREG_REGD_GHR_HDAT GHRH ");
        stb.append("             ON GHRH.YEAR     = GHRD.YEAR ");
        stb.append("            AND GHRH.SEMESTER = GHRD.SEMESTER ");
        stb.append("            AND GHRH.GHR_CD   = GHRD.GHR_CD ");
        stb.append("     WHERE   REGD.YEAR = '" + _param._loginYear + "' ");
        if (SEMEALL.equals(_param._semester)) {
            stb.append("     AND REGD.SEMESTER = '" + _param._loginSemester + "' ");
        } else {
            stb.append("     AND REGD.SEMESTER = '" + _param._semester + "' ");
        }
        if("1".equals(_param._hukusikiRadio)) {
            stb.append("         AND REGD.GRADE || REGD.HR_CLASS = '"+ _param._gradeHrClass +"' ");
        } else {
            stb.append("         AND GHRH.GHR_CD   = '"+_param._gradeHrClass +"' ");
        }
        stb.append("         AND REGD.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected));

        stb.append("     ORDER BY ");
        stb.append("         REGD.GRADE, ");
        stb.append("         REGD.HR_CLASS, ");
        stb.append("         REGD.ATTENDNO ");
        final String sql = stb.toString();
        log.debug(" student sql = " + sql);

        return stb.toString();
    }

    private static String sishaGonyu(final String val) {
        if (!NumberUtils.isNumber(val)) {
            return null;
        }
        return new BigDecimal(val).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    //対象生徒の担任氏名を取得
    private List getSchregTrName(final DB2UDB db2, final String schregno) {
        List rtnList = new ArrayList();
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT  ");
        stb.append("   T1.STAFFCD, ");
        stb.append("   T2.STAFFNAME ");
        stb.append(" FROM  ");
        stb.append("   HREPORT_STAFF_DAT T1 ");
        stb.append("   LEFT JOIN STAFF_MST T2 ");
        stb.append("          ON T2.STAFFCD = T1.STAFFCD ");
        stb.append(" WHERE T1.YEAR     = '"+ _param._loginYear +"'  ");
        stb.append("   AND T1.SEMESTER = '"+ SEMEALL +"' ");
        stb.append("   AND T1.SCHREGNO    = '"+ schregno +"' ");
        stb.append(" ORDER BY ");
        stb.append("   T1.SEQ ");

        final String sql =  stb.toString();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            log.debug(sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String name = StringUtils.defaultString(rs.getString("STAFFNAME"));
                rtnList.add(name);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtnList;
    }

    //対象生徒の自立活動を取得
    private Map getSchregSelfrelianceRemark(final DB2UDB db2, final String schregno) {
        Map rtnMap = new HashMap();
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT  ");
        stb.append("   * ");
        stb.append(" FROM  ");
        stb.append("   V_HREPORT_SELFRELIANCE_SCHREG_REMARK_DAT ");
        stb.append(" WHERE YEAR     = '"+ _param._loginYear +"'  ");
        stb.append("   AND SCHREGNO    = '"+ schregno +"' ");
        stb.append(" ORDER BY ");
        stb.append("   SEMESTER ");
        final String sql =  stb.toString();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            log.debug(sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                Map map = new HashMap();
                final String semester  = StringUtils.defaultString(rs.getString("SEMESTER"));
                map.put("KEY_GOALS", StringUtils.defaultString(rs.getString("KEY_GOALS")));
                map.put("GOALS_REASON", StringUtils.defaultString(rs.getString("GOALS_REASON")));
                map.put("LONG_GOALS1", StringUtils.defaultString(rs.getString("LONG_GOALS1")));
                map.put("SHORT_GOALS1", StringUtils.defaultString(rs.getString("SHORT_GOALS1")));
                map.put("MEANS1", StringUtils.defaultString(rs.getString("MEANS1")));
                map.put("EVALUATION1", StringUtils.defaultString(rs.getString("EVALUATION1")));
                map.put("LONG_GOALS2", StringUtils.defaultString(rs.getString("LONG_GOALS2")));
                map.put("SHORT_GOALS2", StringUtils.defaultString(rs.getString("SHORT_GOALS2")));
                map.put("MEANS2", StringUtils.defaultString(rs.getString("MEANS2")));
                map.put("EVALUATION2", StringUtils.defaultString(rs.getString("EVALUATION2")));
                map.put("LONG_GOALS3", StringUtils.defaultString(rs.getString("LONG_GOALS3")));
                map.put("SHORT_GOALS3", StringUtils.defaultString(rs.getString("SHORT_GOALS3")));
                map.put("MEANS3", StringUtils.defaultString(rs.getString("MEANS3")));
                map.put("EVALUATION3", StringUtils.defaultString(rs.getString("EVALUATION3")));
                map.put("LONG_GOALS4", StringUtils.defaultString(rs.getString("LONG_GOALS4")));
                map.put("SHORT_GOALS4", StringUtils.defaultString(rs.getString("SHORT_GOALS4")));
                map.put("MEANS4", StringUtils.defaultString(rs.getString("MEANS4")));
                map.put("EVALUATION4", StringUtils.defaultString(rs.getString("EVALUATION4")));
                rtnMap.put(semester, map);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtnMap;
    }

    //対象生徒の学習の記録を取得
    private String getSchregHreportRemark(final DB2UDB db2, final String schregno, final String semester, final String div, final String colum) {
        String rtnStr = "";
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT  ");
        stb.append("   * ");
        stb.append(" FROM  ");
        stb.append("   HREPORT_SCHREG_REMARK_DAT T1 ");
        stb.append(" WHERE T1.YEAR     = '"+ _param._loginYear +"'  ");
        stb.append("   AND T1.SEMESTER = '"+ semester +"' ");
        stb.append("   AND T1.SCHREGNO = '"+ schregno +"' ");
        stb.append("   AND T1.DIV      = '"+ div +"' ");
        final String sql =  stb.toString();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            log.debug(sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                rtnStr = StringUtils.defaultString(rs.getString(colum));
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtnStr;
    }

    private class Student {
        String _schregno;
        String _name;
        String _schoolKind;
        String _gradeCd;
        String _gradename;
        String _hrname;
        String _ghrname;
        String _trCd1;
        String _staffname;
        String _attendno;
        String _grade;
        String _hrClass;
        String _coursecd;
        String _majorcd;
        String _course;
        String _coursename;
        String _majorname;
        String _hrClassName1;
        String _entyear;

        final Map _attendMap = new TreeMap();
        List _hreportSchregSubclassRemarkDatList = Collections.EMPTY_LIST; // 学習の記録

        public Student() {
        }

        private String prestatementSubclass() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCHNO AS( ");
            //学籍の表
            stb.append(" SELECT ");
            stb.append("     T2.SEMESTER, ");
            stb.append("     T2.SCHREGNO, ");
            stb.append("     T2.GRADE, ");
            stb.append("     T2.HR_CLASS, ");
            stb.append("     T2.ATTENDNO, ");
            stb.append("     T2.COURSECD, ");
            stb.append("     T2.MAJORCD, ");
            stb.append("     T2.COURSECODE ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T2 ");
            stb.append(" WHERE ");
            stb.append("         T2.YEAR    = '" + _param._loginYear + "'  ");
            stb.append("     AND T2.GRADE    = '" + _grade + "'  ");
            stb.append("     AND T2.HR_CLASS = '" + _hrClass + "'  ");
            stb.append("     AND T2.SCHREGNO = '" + _schregno + "'  ");
            stb.append("     AND T2.SEMESTER = (SELECT ");
            stb.append("                          MAX(SEMESTER) ");
            stb.append("                        FROM ");
            stb.append("                          SCHREG_REGD_DAT W2 ");
            stb.append("                        WHERE ");
            stb.append("                          W2.YEAR = '" + _param._loginYear + "'  ");
            stb.append("                          AND W2.SEMESTER <= '" + _param._semester + "'  ");
            stb.append("                          AND W2.SCHREGNO = T2.SCHREGNO ");
            stb.append("                     ) ");
            //講座の表
            stb.append(" ) , CHAIR_A AS( ");
            stb.append(" SELECT ");
            stb.append("     S1.SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(" S2.CLASSCD, ");
                stb.append(" S2.SCHOOL_KIND, ");
                stb.append(" S2.CURRICULUM_CD, ");
            }
            stb.append("     S2.SUBCLASSCD ");
            stb.append(" FROM ");
            stb.append("     CHAIR_STD_DAT S1, ");
            stb.append("     CHAIR_DAT S2 ");
            stb.append(" WHERE ");
            stb.append("     S1.YEAR         = '" + _param._loginYear + "' ");
            stb.append("     AND S1.SEMESTER <= '" + _param._semester + "' ");
            stb.append("     AND S2.YEAR     = S1.YEAR          ");
            stb.append("     AND S2.SEMESTER = S1.SEMESTER          ");
            stb.append("     AND S2.CHAIRCD  = S1.CHAIRCD          ");
            stb.append("     AND EXISTS(SELECT ");
            stb.append("                  'X' ");
            stb.append("                FROM ");
            stb.append("                  SCHNO S3 ");
            stb.append("                WHERE ");
            stb.append("                  S3.SCHREGNO = S1.SCHREGNO ");
            stb.append("                GROUP BY ");
            stb.append("                  SCHREGNO ");
            stb.append("             )          ");
            stb.append("     AND SUBCLASSCD <= '" + SELECT_CLASSCD_UNDER + "' ");
            stb.append("     AND SUBCLASSCD NOT LIKE '50%' ");
            stb.append(" GROUP BY ");
            stb.append("     S1.SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(" S2.CLASSCD, ");
                stb.append(" S2.SCHOOL_KIND, ");
                stb.append(" S2.CURRICULUM_CD, ");
            }
            stb.append("     S2.SUBCLASSCD ");
            //成績明細データの表
            stb.append(" ) ,RECORD AS( ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     L2.SCORE AS SCORE1, ");
            stb.append("     L3.SCORE AS SCORE2, ");
            stb.append("     L4.SCORE AS SCORE3, ");
            stb.append("     L5.SCORE AS SCORE9 ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_SDIV_DAT T1 ");
            stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT L2 ");
            stb.append("            ON L2.YEAR          = T1.YEAR ");
            stb.append("           AND L2.SEMESTER || L2.TESTKINDCD || L2.TESTITEMCD || L2.SCORE_DIV = '" + SDIV1990008 + "' ");
            if ("1".equals(_param._useCurriculumcd)) {
            stb.append("           AND L2.CLASSCD       = T1.CLASSCD ");
            stb.append("           AND L2.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("           AND L2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("           AND L2.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("           AND L2.SCHREGNO      = T1.SCHREGNO ");
            stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT L3 ");
            stb.append("            ON L3.YEAR          = T1.YEAR ");
            stb.append("           AND L3.SEMESTER || L3.TESTKINDCD || L3.TESTITEMCD || L3.SCORE_DIV = '" + SDIV2990008 + "' ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("           AND L3.CLASSCD       = T1.CLASSCD ");
                stb.append("           AND L3.SCHOOL_KIND   = T1.SCHOOL_KIND ");
                stb.append("           AND L3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("           AND L3.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("           AND L3.SCHREGNO      = T1.SCHREGNO ");
            stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT L4 ");
            stb.append("            ON L4.YEAR          = T1.YEAR ");
            stb.append("           AND L4.SEMESTER || L4.TESTKINDCD || L4.TESTITEMCD || L4.SCORE_DIV = '" + SDIV3990008 + "' ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("           AND L4.CLASSCD       = T1.CLASSCD ");
                stb.append("           AND L4.SCHOOL_KIND   = T1.SCHOOL_KIND ");
                stb.append("           AND L4.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("           AND L4.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("           AND L4.SCHREGNO      = T1.SCHREGNO ");
            stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT L5 ");
            stb.append("            ON L5.YEAR          = T1.YEAR ");
            stb.append("           AND L5.SEMESTER || L5.TESTKINDCD || L5.TESTITEMCD || L5.SCORE_DIV = '" + SDIV9990008 + "' ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("           AND L5.CLASSCD       = T1.CLASSCD ");
                stb.append("           AND L5.SCHOOL_KIND   = T1.SCHOOL_KIND ");
                stb.append("           AND L5.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("           AND L5.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("           AND L5.SCHREGNO      = T1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._loginYear + "'  ");
            stb.append("     AND EXISTS(SELECT ");
            stb.append("                  'X' ");
            stb.append("                FROM ");
            stb.append("                  SCHNO T2 ");
            stb.append("                WHERE ");
            stb.append("                  T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("                GROUP BY ");
            stb.append("                  SCHREGNO ");
            stb.append("             )          ");
            stb.append("     AND T1.SUBCLASSCD NOT LIKE '50%' ");
            stb.append("     AND T1.SUBCLASSCD NOT IN ('" + ALL3 + "', '" + ALL5 + "') ");
            //メイン表1
            stb.append(" ) ,T_MAIN AS( ");
            stb.append(" SELECT ");
            stb.append("     T3.CLASSCD, ");
            stb.append("     T3.CLASSNAME, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(" T4.SCHOOL_KIND, ");
                stb.append(" T4.CURRICULUM_CD, ");
            }
            stb.append("     T4.SUBCLASSCD, ");
            stb.append("     T4.SUBCLASSNAME, ");
            stb.append("     T1.SCORE1, ");
            stb.append("     T1.SCORE2, ");
            stb.append("     T1.SCORE3, ");
            stb.append("     T1.SCORE9 ");
            stb.append(" FROM ");
            stb.append("     SCHNO T2 ");
            stb.append("     LEFT JOIN RECORD T1 ");
            stb.append("            ON T1.SCHREGNO = T2.SCHREGNO ");
            stb.append("     INNER JOIN CHAIR_A T5 ");
            stb.append("            ON T5.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("           AND T5.SCHREGNO      = T1.SCHREGNO ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("       AND T5.CLASSCD       = T1.CLASSCD ");
                stb.append("       AND T5.SCHOOL_KIND   = T1.SCHOOL_KIND ");
                stb.append("       AND T5.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("     INNER JOIN CLASS_MST T3 ");
            stb.append("            ON T3.CLASSCD = SUBSTR(T1.SUBCLASSCD,1,2) ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("       AND T3.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            }
            stb.append("     INNER JOIN SUBCLASS_MST T4 ");
            stb.append("            ON T4.SUBCLASSCD    = T1.SUBCLASSCD ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("       AND T4.CLASSCD       = T1.CLASSCD ");
                stb.append("       AND T4.SCHOOL_KIND   = T1.SCHOOL_KIND ");
                stb.append("       AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append(" ) ");
            //メイン表2
            stb.append(" SELECT ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.CLASSNAME, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
            }else {
                stb.append("     T1.SUBCLASSCD, ");
            }
            stb.append("     T1.SUBCLASSNAME, ");
            stb.append("     T1.SCORE1, ");
            stb.append("     T1.SCORE2, ");
            stb.append("     T1.SCORE3, ");
            stb.append("     T1.SCORE9 ");
            stb.append(" FROM ");
            stb.append("     T_MAIN T1 ");
            stb.append(" ORDER BY ");
            stb.append("     T1.SUBCLASSCD ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(" ,T1.SCHOOL_KIND ");
                stb.append(" ,T1.CURRICULUM_CD ");
            }

            return stb.toString();
        }
    }

    /**
     * 学習の記録
     */
    private static class HreportSchregSubclassRemarkDat {
        final String _year;
        final String _semester;
        final String _schregno;
        final String _classcd;
        final String _school_kind;
        final String _curriculum_cd;
        final String _subclasscd;
        final String _subclassname;
        final String _remark1;
        final String _remark2;
        final String _remark3;

        public HreportSchregSubclassRemarkDat(
                final String year,
                final String semester,
                final String schregno,
                final String classcd,
                final String school_kind,
                final String curriculum_cd,
                final String subclasscd,
                final String subclassname,
                final String remark1,
                final String remark2,
                final String remark3
                ) {
            _year = year;
            _semester = semester;
            _schregno = schregno;
            _classcd = classcd;
            _school_kind = school_kind;
            _curriculum_cd = curriculum_cd;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _remark1 = remark1;
            _remark2 = remark2;
            _remark3 = remark3;
        }

        public static List getHreportSchregSubclassRemarkDat(final DB2UDB db2, final Param param, final String schregno) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getHreportSchregSubclassRemarkDatSql(param, schregno);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String year = StringUtils.defaultString(rs.getString("YEAR"));
                    final String semester = StringUtils.defaultString(rs.getString("SEMESTER"));
                    final String classcd = StringUtils.defaultString(rs.getString("CLASSCD"));
                    final String school_kind = StringUtils.defaultString(rs.getString("SCHOOL_KIND"));
                    final String curriculum_cd = StringUtils.defaultString(rs.getString("CURRICULUM_CD"));
                    final String subclasscd = StringUtils.defaultString(rs.getString("SUBCLASSCD"));
                    final String subclassname = StringUtils.defaultString(rs.getString("SUBCLASSNAME"));
                    final String remark1 = StringUtils.defaultString(rs.getString("REMARK1"));
                    final String remark2 = StringUtils.defaultString(rs.getString("REMARK2"));
                    final String remark3 = StringUtils.defaultString(rs.getString("REMARK3"));

                    if("".equals(remark1) && "".equals(remark2) && "".equals(remark3)) continue;
                    final HreportSchregSubclassRemarkDat hreportSchregSubclassRemarkDat = new HreportSchregSubclassRemarkDat(year, semester, schregno, classcd, school_kind, curriculum_cd, subclasscd, subclassname, remark1, remark2,remark3);
                    list.add(hreportSchregSubclassRemarkDat);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private static String getHreportSchregSubclassRemarkDatSql(final Param param, final String schregno) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH T_MAIN AS( ");
            stb.append("   SELECT DISTINCT ");
            stb.append("     YEAR, ");
            stb.append("     SEMESTER, ");
            stb.append("     SCHREGNO, ");
            stb.append("     CLASSCD, ");
            stb.append("     SCHOOL_KIND, ");
            stb.append("     CURRICULUM_CD, ");
            stb.append("     SUBCLASSCD, ");
            stb.append("     UNITCD ");
            stb.append("   FROM  ");
            stb.append("     HREPORT_SCHREG_SUBCLASS_REMARK_DAT ");
            stb.append("   WHERE ");
            stb.append("        YEAR     = '"+ param._loginYear +"' ");
            stb.append("    AND SEMESTER = '"+ param._semester +"' ");
            stb.append("    AND SCHREGNO = '"+ schregno +"' ");
            stb.append("    AND SEQ IN ('1','2','3') ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("   T1.YEAR, ");
            stb.append("   T1.SEMESTER, ");
            stb.append("   T1.SCHREGNO, ");
            stb.append("   T1.CLASSCD, ");
            stb.append("   T1.SCHOOL_KIND, ");
            stb.append("   T1.CURRICULUM_CD, ");
            stb.append("   T1.SUBCLASSCD, ");
            stb.append("   T2.SUBCLASSNAME, ");
            stb.append("   HSSR1.REMARK AS REMARK1, ");
            stb.append("   HSSR2.REMARK AS REMARK2, ");
            stb.append("   HSSR3.REMARK AS REMARK3 ");
            stb.append(" FROM  ");
            stb.append("   T_MAIN T1 ");
            stb.append("   LEFT JOIN SUBCLASS_MST T2 ");
            stb.append("          ON T2.CLASSCD       = T1.CLASSCD ");
            stb.append("         AND T2.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("         AND T2.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("   LEFT JOIN HREPORT_SCHREG_SUBCLASS_REMARK_DAT HSSR1 ");
            stb.append("          ON HSSR1.YEAR          = T1.YEAR ");
            stb.append("         AND HSSR1.SEMESTER      = T1.SEMESTER ");
            stb.append("         AND HSSR1.SCHREGNO      = T1.SCHREGNO ");
            stb.append("         AND HSSR1.CLASSCD       = T1.CLASSCD ");
            stb.append("         AND HSSR1.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("         AND HSSR1.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("         AND HSSR1.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("         AND HSSR1.UNITCD        = T1.UNITCD ");
            stb.append("         AND HSSR1.SEQ           = '1' ");
            stb.append("   LEFT JOIN HREPORT_SCHREG_SUBCLASS_REMARK_DAT HSSR2 ");
            stb.append("          ON HSSR2.YEAR          = T1.YEAR ");
            stb.append("         AND HSSR2.SEMESTER      = T1.SEMESTER ");
            stb.append("         AND HSSR2.SCHREGNO      = T1.SCHREGNO ");
            stb.append("         AND HSSR2.CLASSCD       = T1.CLASSCD ");
            stb.append("         AND HSSR2.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("         AND HSSR2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("         AND HSSR2.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("         AND HSSR2.UNITCD        = T1.UNITCD ");
            stb.append("         AND HSSR2.SEQ           = '2' ");
            stb.append("   LEFT JOIN HREPORT_SCHREG_SUBCLASS_REMARK_DAT HSSR3 ");
            stb.append("          ON HSSR3.YEAR          = T1.YEAR ");
            stb.append("         AND HSSR3.SEMESTER      = T1.SEMESTER ");
            stb.append("         AND HSSR3.SCHREGNO      = T1.SCHREGNO ");
            stb.append("         AND HSSR3.CLASSCD       = T1.CLASSCD ");
            stb.append("         AND HSSR3.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("         AND HSSR3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("         AND HSSR3.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("         AND HSSR3.UNITCD        = T1.UNITCD ");
            stb.append("         AND HSSR3.SEQ           = '3' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.SUBCLASSCD ");
            return stb.toString();
        }
    }

    private static class Attendance {
        final int _lesson;
        final int _mLesson;
        final int _suspend;
        final int _mourning;
        final int _absent;
        final int _present;
        final int _late;
        final int _early;
        final int _abroad;
        final String _remark;
        final String _communication;
        Attendance(
                final int lesson,
                final int mLesson,
                final int suspend,
                final int mourning,
                final int absent,
                final int present,
                final int late,
                final int early,
                final int abroad,
                final String remark,
                final String communication
        ) {
            _lesson = lesson;
            _mLesson = mLesson;
            _suspend = suspend;
            _mourning = mourning;
            _absent = absent;
            _present = present;
            _late = late;
            _early = early;
            _abroad = abroad;
            _remark = remark;
            _communication = communication;
        }

        private static void load(
                final DB2UDB db2,
                final Param param,
                final List studentList,
                final DateRange dateRange
        ) {
            log.info(" attendance = " + dateRange);
            if (null == dateRange || null == dateRange._sdate || null == dateRange._edate || dateRange._sdate.compareTo(param._date) > 0) {
                return;
            }
            final String edate = dateRange._edate.compareTo(param._date) > 0 ? param._date : dateRange._edate;
            PreparedStatement psAtSeme = null;
            ResultSet rsAtSeme = null;
            PreparedStatement psHreportRemark = null;
            ResultSet rsHreportRemark = null;
            try {
                param._attendParamMap.put("schregno", "?");

                final String sql = AttendAccumulate.getAttendSemesSql(
                        param._loginYear,
                        param._semester,
                        dateRange._sdate,
                        edate,
                        param._attendParamMap
                );
                log.debug(" attend sql = " + sql);
                psAtSeme = db2.prepareStatement(sql);

                final String hreportRemarkSql = getHreportRemarkSql(param, dateRange);
                psHreportRemark = db2.prepareStatement(hreportRemarkSql);
                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    psHreportRemark.setString(1, student._schregno);
                    rsHreportRemark = psHreportRemark.executeQuery();

                    String setRemark = "";
                    String setCommunication = "";
                    while (rsHreportRemark.next()) {
                        setRemark = rsHreportRemark.getString("ATTENDREC_REMARK");
                        setCommunication = rsHreportRemark.getString("COMMUNICATION");
                    }
                    DbUtils.closeQuietly(rsAtSeme);

                    psAtSeme.setString(1, student._schregno);
                    rsAtSeme = psAtSeme.executeQuery();

                    while (rsAtSeme.next()) {
                        if (!SEMEALL.equals(rsAtSeme.getString("SEMESTER"))) {
                            continue;
                        }

                        final Attendance attendance = new Attendance(
                                rsAtSeme.getInt("LESSON"),
                                rsAtSeme.getInt("MLESSON"),
                                rsAtSeme.getInt("SUSPEND"),
                                rsAtSeme.getInt("MOURNING"),
                                rsAtSeme.getInt("SICK"),
                                rsAtSeme.getInt("PRESENT"),
                                rsAtSeme.getInt("LATE"),
                                rsAtSeme.getInt("EARLY"),
                                rsAtSeme.getInt("TRANSFER_DATE"),
                                setRemark,
                                setCommunication
                        );
                        student._attendMap.put(dateRange._key, attendance);
                    }
                    DbUtils.closeQuietly(rsAtSeme);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(psAtSeme);
                db2.commit();
            }
        }

        private static String getHreportRemarkSql(final Param param, final DateRange dateRange) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     TOTALSTUDYTIME, ");
            stb.append("     SPECIALACTREMARK, ");
            stb.append("     COMMUNICATION, ");
            stb.append("     REMARK1, ");
            stb.append("     REMARK2, ");
            stb.append("     REMARK3, ");
            stb.append("     FOREIGNLANGACT, ");
            stb.append("     ATTENDREC_REMARK ");
            stb.append(" FROM ");
            stb.append("     HREPORTREMARK_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + param._loginYear + "' ");
            stb.append("     AND SEMESTER = '" + dateRange._key + "' ");
            stb.append("     AND SCHREGNO = ? ");

            return stb.toString();
        }

    }

    private static class Semester {
        final String _semester;
        final String _semestername;
        final DateRange _dateRange;
        final List _testItemList;
        final List _semesterDetailList;
        public Semester(final String semester, final String semestername, final String sdate, final String edate) {
            _semester = semester;
            _semestername = semestername;
            _dateRange = new DateRange(_semester, _semestername, sdate, edate);
            _testItemList = new ArrayList();
            _semesterDetailList = new ArrayList();
        }
        public int getTestItemIdx(final TestItem testItem) {
            return _testItemList.indexOf(testItem);
        }
        public int getSemesterDetailIdx(final SemesterDetail semesterDetail) {
//          log.debug(" semesterDetail = " + semesterDetail + " , " + _semesterDetailList.indexOf(semesterDetail));
          return _semesterDetailList.indexOf(semesterDetail);
        }

        public int compareTo(final Object o) {
            if (!(o instanceof Semester)) {
                return 0;
            }
            Semester s = (Semester) o;
            return _semester.compareTo(s._semester);
        }
    }

    private static class SubclassMst implements Comparable {
        final String _specialDiv;
        final String _classcd;
        final String _subclasscd;
        final String _classabbv;
        final String _classname;
        final String _subclassname;
        final Integer _classShoworder3;
        final Integer _subclassShoworder3;
        final boolean _isSaki;
        final boolean _isMoto;
        final String _calculateCreditFlg;
        public SubclassMst(final String specialDiv, final String classcd, final String subclasscd, final String classabbv, final String classname, final String subclassabbv, final String subclassname,
                final Integer classShoworder3,
                final Integer subclassShoworder3,
                final boolean isSaki, final boolean isMoto, final String calculateCreditFlg) {
            _specialDiv = specialDiv;
            _classcd = classcd;
            _subclasscd = subclasscd;
            _classabbv = classabbv;
            _classname = classname;
            _subclassname = subclassname;
            _classShoworder3 = classShoworder3;
            _subclassShoworder3 = subclassShoworder3;
            _isSaki = isSaki;
            _isMoto = isMoto;
            _calculateCreditFlg = calculateCreditFlg;
        }
        public int compareTo(final Object o) {
            final SubclassMst mst = (SubclassMst) o;
            int rtn;
            rtn = _classShoworder3.compareTo(mst._classShoworder3);
            if (0 != rtn) { return rtn; }
            if (null == _classcd && null == mst._classcd) {
                return 0;
            } else if (null == _classcd) {
                return 1;
            } else if (null == mst._classcd) {
                return -1;
            }
            rtn = _classcd.compareTo(mst._classcd);
            if (0 != rtn) { return rtn; }
            rtn = _subclassShoworder3.compareTo(mst._subclassShoworder3);
            if (0 != rtn) { return rtn; }
            if (null == _subclasscd && null == mst._subclasscd) {
                return 0;
            } else if (null == _subclasscd) {
                return 1;
            } else if (null == mst._subclasscd) {
                return -1;
            }
            return _subclasscd.compareTo(mst._subclasscd);
        }
        public String toString() {
            return "SubclassMst(subclasscd = " + _subclasscd + ")";
        }
    }

    private static class DateRange {
        final String _key;
        final String _name;
        final String _sdate;
        final String _edate;
        public DateRange(final String key, final String name, final String sdate, final String edate) {
            _key = key;
            _name = name;
            _sdate = sdate;
            _edate = edate;
        }
        public String toString() {
            return "DateRange(" + _key + ", " + _sdate + ", " + _edate + ")";
        }
    }

    private static class SemesterDetail implements Comparable {
        final Semester _semester;
        final String _cdSemesterDetail;
        final String _semestername;
        final String _sdate;
        final String _edate;
        public SemesterDetail(Semester semester, String cdSemesterDetail, String semestername, final String sdate, final String edate) {
            _semester = semester;
            _cdSemesterDetail = cdSemesterDetail;
            _semestername = StringUtils.defaultString(semestername);
            _sdate = sdate;
            _edate = edate;
        }
        public int compareTo(final Object o) {
            if (!(o instanceof SemesterDetail)) {
                return 0;
            }
            SemesterDetail sd = (SemesterDetail) o;
            int rtn;
            rtn = _semester.compareTo(sd._semester);
            if (rtn != 0) {
                return rtn;
            }
            rtn = _cdSemesterDetail.compareTo(sd._cdSemesterDetail);
            return rtn;
        }
        public String toString() {
            return "SemesterDetail(" + _semester._semester + ", " + _cdSemesterDetail + ", " + _semestername + ")";
        }
    }

    private static class TestItem {
        final String _year;
        final Semester _semester;
        final String _testkindcd;
        final String _testitemcd;
        final String _scoreDiv;
        final String _testitemname;
        final String _testitemabbv1;
        final String _sidouInput;
        final String _sidouInputInf;
        final SemesterDetail _semesterDetail;
        boolean _isGakunenKariHyotei;
        int _printKettenFlg; // -1: 表示しない（仮評定）、1: 値が1をカウント（学年評定）、2:換算した値が1をカウント（評価等）
        public TestItem(final String year, final Semester semester, final String testkindcd, final String testitemcd, final String scoreDiv,
                final String testitemname, final String testitemabbv1, final String sidouInput, final String sidouInputInf,
                final SemesterDetail semesterDetail) {
            _year = year;
            _semester = semester;
            _testkindcd = testkindcd;
            _testitemcd = testitemcd;
            _scoreDiv = scoreDiv;
            _testitemname = testitemname;
            _testitemabbv1 = testitemabbv1;
            _sidouInput = sidouInput;
            _sidouInputInf = sidouInputInf;
            _semesterDetail = semesterDetail;
        }
        public String getTestcd() {
            return _semester._semester +_testkindcd +_testitemcd + _scoreDiv;
        }
        public String toString() {
            return "TestItem(" + _semester._semester + _testkindcd + _testitemcd + "(" + _scoreDiv + "))";
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Id: d26aaf5140099c97dd83f1ac2aaa73bf6319ed27 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _loginYear;
        final String _semester;
        final String _hukusikiRadio;
        final String _schoolKind;
        final String _gradeHrClass;
        final String _grade;
        final String[] _categorySelected;
        final String _date;
        final String _printPattern;
        final boolean _printSide1;
        final boolean _printSide2;
        final boolean _printSide3;
        final boolean _output1;

        final String _loginSemester;
        final String _ctrlDate;
        final String _prgid;
        final String _schoolCd;

        final String _nendo;
        final boolean _semes2Flg;
        final boolean _semes3Flg;
        final boolean _semes9Flg;

        private String _certifSchoolSchoolName;
        private String _certifSchoolJobName;
        private String _certifSchoolPrincipalName;
        private String _certifSchoolRemark3;
        private String _certifSchoolRemark8;

        /** 端数計算共通メソッド引数 */
        private Map _attendParamMap;

        private final List _semesterList;
        private Map _semesterMap;
        private final Map _semesterDetailMap;
        private Map _attendRanges;

        final String _documentroot;
        final String _imagepath;
        final String _schoolLogoImagePath;
        final String _backSlashImagePath;
        final String _whiteSpaceImagePath;
        final String _schoolStampPath;
        private final String _useCurriculumcd;
        private final String _use_school_detail_gcm_dat;
        private final List _attendTestKindItemList;
        private final List _attendSemesterDetailList;
        final Map _stampMap;
        final Map _kindNameMap;
        final Map _hreportConditionMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _loginYear = request.getParameter("YEAR");
            _loginSemester = request.getParameter("LOGIN_SEMESTER");
            _semester = (SEMEALL.equals(request.getParameter("SEMESTER")))? _loginSemester : request.getParameter("SEMESTER");
            _hukusikiRadio = request.getParameter("HUKUSIKI_RADIO");
            _schoolKind = request.getParameter("SCHOOL_KIND");
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            _grade = _gradeHrClass.substring(0, 2);
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _date = request.getParameter("DATE").replace('/', '-');
            _printPattern = request.getParameter("PRINT_PATTERN"); //帳票パターン 101:文言評価（3枠） 102:文言評価（2枠） 103:文言評価+自立活動
            _printSide1 = "1".equals(request.getParameter("PRINT_SIDE1")); //表紙・裏表紙
            _printSide2 = "1".equals(request.getParameter("PRINT_SIDE2")); //学習の記録
            _printSide3 = "1".equals(request.getParameter("PRINT_SIDE3")); //出欠の記録
            _output1 = "1".equals(request.getParameter("OUTPUT1"));
            _ctrlDate = request.getParameter("CTRL_DATE").replace('/', '-');
            _prgid = request.getParameter("PRGID");
            _schoolCd = request.getParameter("SCHOOLCD");

            _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_loginYear)) + "年度";
            _semes2Flg = "2".equals(_semester) || "3".equals(_semester) || "9".equals(_semester) ? true : false;
            _semes3Flg = "3".equals(_semester) || "9".equals(_semester) ? true : false;
            _semes9Flg = SEMEALL.equals(_semester) ? true : false;
            setCertifSchoolDat(db2);

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _grade);
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
            _attendParamMap.put("useCurriculumcd", "1");

            _semesterList = getSemesterList(db2);
            _semesterMap = loadSemester(db2);
            _semesterDetailMap = new HashMap();
            _attendRanges = new HashMap();
            for (final Iterator it = _semesterMap.keySet().iterator(); it.hasNext();) {
                final String semester = (String) it.next();
                final Semester oSemester = (Semester) _semesterMap.get(semester);
                _attendRanges.put(semester, oSemester._dateRange);
            }

            _documentroot = request.getParameter("DOCUMENTROOT");
            final String sqlControlMst = " SELECT IMAGEPATH FROM CONTROL_MST WHERE CTRL_NO = '01' ";
            _imagepath = KnjDbUtils.getOne(KnjDbUtils.query(db2, sqlControlMst));
            _schoolLogoImagePath = getImageFilePath("SCHOOLLOGO.jpg");
            _backSlashImagePath = getImageFilePath("slash_bs.jpg");
            _whiteSpaceImagePath = getImageFilePath("whitespace.png");
            _schoolStampPath = getImageFilePath("SCHOOLSTAMP.bmp");

            _useCurriculumcd = request.getParameter("useCurriculumcd");

            _use_school_detail_gcm_dat = request.getParameter("use_school_detail_gcm_dat");

            _attendTestKindItemList = getTestKindItemList(db2, false, false);
            _attendSemesterDetailList = getAttendSemesterDetailList();

            _stampMap = getStampNoMap(db2);
            _kindNameMap = getKindNameMap(db2);
            _hreportConditionMap = getHreportConditionMap(db2);
        }

        /**
         * 年度の開始日を取得する
         */
        private Map loadSemester(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map map = new TreeMap();
            try {
                final String sql = "select"
                        + "   SEMESTER,"
                        + "   SEMESTERNAME,"
                        + "   SDATE,"
                        + "   EDATE"
                        + " from"
                        + "   V_SEMESTER_GRADE_MST"
                        + " where"
                        + "   YEAR='" + _loginYear + "'"
                        + "   AND GRADE='" + _grade + "'"
                        + " order by SEMESTER"
                    ;

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    map.put(rs.getString("SEMESTER"), new Semester(rs.getString("SEMESTER"), rs.getString("SEMESTERNAME"), rs.getString("SDATE"), rs.getString("EDATE")));
                }
            } catch (final Exception ex) {
                log.error("テスト項目のロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return map;
        }

        private void setCertifSchoolDat(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            final String kindCd = "P".equals(_schoolKind) ? "107" : "J".equals(_schoolKind) ? "103" : "H".equals(_schoolKind) ? "104" : "104";
            sql.append(" SELECT * FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _loginYear + "' AND CERTIF_KINDCD = '" + kindCd + "' ");
            log.debug("certif_school_dat sql = " + sql.toString());

            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql.toString()));

            _certifSchoolSchoolName = StringUtils.defaultString(KnjDbUtils.getString(row, "SCHOOL_NAME"));
            _certifSchoolJobName = StringUtils.defaultString(KnjDbUtils.getString(row, "JOB_NAME"), "校長");
            _certifSchoolPrincipalName = StringUtils.defaultString(KnjDbUtils.getString(row, "PRINCIPAL_NAME"));
            _certifSchoolRemark3 = StringUtils.defaultString(KnjDbUtils.getString(row, "REMARK3"));
            _certifSchoolRemark8 = StringUtils.defaultString(KnjDbUtils.getString(row, "REMARK8"));
        }

        public String getImageFilePath(final String name) {
            final String path = _documentroot + "/" + (null == _imagepath || "".equals(_imagepath) ? "" : _imagepath + "/") + name;
            final boolean exists = new java.io.File(path).exists();
            log.warn(" path " + path + " exists: " + exists);
            if (exists) {
                return path;
            }
            return null;
        }

        private List getSemesterList(DB2UDB db2) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT * FROM SEMESTER_MST WHERE YEAR = '" + _loginYear + "' ORDER BY SEMESTER ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String semestername = rs.getString("SEMESTERNAME");
                    final String sdate = rs.getString("SDATE");
                    final String edate = rs.getString("EDATE");
                    Semester semes = new Semester(semester, semestername, sdate, edate);
                    list.add(semes);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private List getTestKindItemList(DB2UDB db2, final boolean useSubclassControl, final boolean addSemester) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final Map semesterMap = new HashMap();
                for (final Iterator it = _semesterMap.keySet().iterator(); it.hasNext();) {
                    final String seme = (String) it.next();
                    if(!_semesterMap.containsKey(seme)) continue;
                    final Semester semester = (Semester) _semesterMap.get(seme);
                    semesterMap.put(semester._semester, semester);
                }
                final StringBuffer stb = new StringBuffer();
                stb.append(" WITH ADMIN_CONTROL_SDIV_SUBCLASSCD AS (");
                if (useSubclassControl) {
                    stb.append("   SELECT DISTINCT ");
                    stb.append("       T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ");
                    stb.append("   FROM ADMIN_CONTROL_SDIV_DAT T1 ");
                    stb.append("   WHERE T1.YEAR = '" + _loginYear + "' ");
                    stb.append("   UNION ALL ");
                }
                stb.append("   SELECT DISTINCT ");
                stb.append("       T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ");
                stb.append("   FROM ADMIN_CONTROL_SDIV_DAT T1 ");
                stb.append("   WHERE T1.YEAR = '" + _loginYear + "' ");
                stb.append("     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '00-00-00-000000' ");
                stb.append("   UNION ALL ");
                stb.append("   SELECT DISTINCT ");
                stb.append("       T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ");
                stb.append("   FROM ADMIN_CONTROL_SDIV_DAT T1 ");
                stb.append("   WHERE T1.YEAR = '" + _loginYear + "' ");
                stb.append("     AND T1.CLASSCD = '00' ");
                stb.append("     AND T1.CURRICULUM_CD = '00' ");
                stb.append("     AND T1.SUBCLASSCD = '000000' ");
                stb.append(" ) ");
                stb.append(" SELECT ");
                stb.append("     T1.YEAR, ");
                stb.append("     T1.SEMESTER, ");
                stb.append("     T1.TESTKINDCD, ");
                stb.append("     T1.TESTITEMCD, ");
                stb.append("     T1.SCORE_DIV, ");
                stb.append("     T1.TESTITEMNAME, ");
                stb.append("     T1.TESTITEMABBV1, ");
                stb.append("     T1.SIDOU_INPUT, ");
                stb.append("     T1.SIDOU_INPUT_INF, ");
                stb.append("     T11.CLASSCD || '-' || T11.SCHOOL_KIND || '-' || T11.CURRICULUM_CD || '-' || T11.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("     T2.SEMESTER_DETAIL, ");
                stb.append("     T2.SEMESTER AS SEMESTER_DETAIL_SEMESTER, ");
                stb.append("     T2.SEMESTERNAME AS SEMESTERDETAILNAME, ");
                stb.append("     T2.SDATE, ");
                stb.append("     T2.EDATE ");
                if ("1".equals(_use_school_detail_gcm_dat)) {
                    stb.append(" FROM TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV T1 ");
                    stb.append(" INNER JOIN ADMIN_CONTROL_GCM_SDIV_DAT T11 ON T11.YEAR = T1.YEAR ");
                } else {
                    stb.append(" FROM TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ");
                    stb.append(" INNER JOIN ADMIN_CONTROL_SDIV_DAT T11 ON T11.YEAR = T1.YEAR ");
                }
                stb.append("    AND T11.SEMESTER = T1.SEMESTER ");
                stb.append("    AND T11.TESTKINDCD = T1.TESTKINDCD ");
                stb.append("    AND T11.TESTITEMCD = T1.TESTITEMCD ");
                stb.append("    AND T11.SCORE_DIV = T1.SCORE_DIV ");
                if ("1".equals(_use_school_detail_gcm_dat)) {
                    stb.append("    AND T11.GRADE = T1.GRADE ");
                    stb.append("    AND T11.COURSECD = T1.COURSECD ");
                    stb.append("    AND T11.MAJORCD = T1.MAJORCD ");
                }
                stb.append(" LEFT JOIN SEMESTER_DETAIL_MST T2 ON T2.YEAR = T1.YEAR ");
                stb.append("    AND T2.SEMESTER = T1.SEMESTER ");
                stb.append("    AND T2.SEMESTER_DETAIL = T1.SEMESTER_DETAIL ");
                stb.append(" WHERE T1.YEAR = '" + _loginYear + "' ");
                stb.append("   AND T11.CLASSCD || '-' || T11.SCHOOL_KIND || '-' || T11.CURRICULUM_CD || '-' || T11.SUBCLASSCD IN "); // 指定の科目が登録されていれば登録された科目、登録されていなければ00-00-00-000000を使用する
                stb.append("    (SELECT MAX(SUBCLASSCD) FROM ADMIN_CONTROL_SDIV_SUBCLASSCD) ");
                if ("1".equals(_use_school_detail_gcm_dat)) {
//                    stb.append("    AND T1.SCHOOLCD = '" + _PRINT_SCHOOLCD + "' ");
//                    stb.append("    AND T1.SCHOOL_KIND = '" + _PRINT_SCHOOLKIND + "' ");
                    stb.append("    AND T1.GRADE = '00' ");
//                    stb.append("    AND T1.COURSECD || '-' || T1.MAJORCD = '"  + _COURSE_MAJOR + "' ");
                }
                stb.append(" ORDER BY T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV ");

                log.debug(" testitem sql ="  + stb.toString());
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();

                String adminSdivSubclasscd = null;
                while (rs.next()) {
                    adminSdivSubclasscd = rs.getString("SUBCLASSCD");
                    final String year = rs.getString("YEAR");
                    final String testkindcd = rs.getString("TESTKINDCD");
                    final String testitemcd = rs.getString("TESTITEMCD");
                    final String scoreDiv = rs.getString("SCORE_DIV");
                    final String sidouInput = rs.getString("SIDOU_INPUT");
                    final String sidouInputInf = rs.getString("SIDOU_INPUT_INF");
                    Semester semester = (Semester) semesterMap.get(rs.getString("SEMESTER"));
                    if (null == semester) {
                        continue;
                    }
                    final String testitemname = rs.getString("TESTITEMNAME");
                    final String testitemabbv1 = rs.getString("TESTITEMABBV1");
                    SemesterDetail semesterDetail = null;
                    final String cdsemesterDetail = rs.getString("SEMESTER_DETAIL");
                    if (null != cdsemesterDetail) {
                        if (_semesterDetailMap.containsKey(cdsemesterDetail)) {
                            semesterDetail = (SemesterDetail) _semesterDetailMap.get(cdsemesterDetail);
                        } else {
                            final String semesterdetailname = rs.getString("SEMESTERDETAILNAME");
                            final String sdate = rs.getString("SDATE");
                            final String edate = rs.getString("EDATE");
                            semesterDetail = new SemesterDetail(semester, cdsemesterDetail, semesterdetailname, sdate, edate);
                            Semester semesDetailSemester = (Semester) semesterMap.get(rs.getString("SEMESTER_DETAIL_SEMESTER"));
                            if (null != semesDetailSemester) {
                                semesDetailSemester._semesterDetailList.add(semesterDetail);
                            }
                            _semesterDetailMap.put(semesterDetail._cdSemesterDetail, semesterDetail);
                        }
                    }
                    final TestItem testItem = new TestItem(
                            year, semester, testkindcd, testitemcd, scoreDiv, testitemname, testitemabbv1, sidouInput, sidouInputInf,
                            semesterDetail);
                    final boolean isGakunenHyotei = HYOTEI_TESTCD.equals(testItem.getTestcd());
                    if (isGakunenHyotei) {
                        testItem._printKettenFlg = 1;
                    } else if (!SEMEALL.equals(semester._semester) && "09".equals(scoreDiv)) { // 9学期以外の09=9学期以外の仮評定
                        testItem._printKettenFlg = -1;
                    } else {
                        testItem._printKettenFlg = 2;
                    }
                    if (isGakunenHyotei) {
                        final TestItem testItemKari = new TestItem(
                                year, semester, testkindcd, testitemcd, scoreDiv, "仮評定", "仮評定", sidouInput, sidouInputInf,
                                semesterDetail);
                        testItemKari._printKettenFlg = -1;
                        testItemKari._isGakunenKariHyotei = true;
                        if (addSemester) {
                            semester._testItemList.add(testItemKari);
                        }
                        list.add(testItemKari);
                    }
                    if (addSemester) {
                        semester._testItemList.add(testItem);
                    }
                    list.add(testItem);
                }
                log.debug(" testitem admin_control_sdiv_dat subclasscd = " + adminSdivSubclasscd);

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            log.debug(" testcd = " + list);
            return list;
        }

        private List getAttendSemesterDetailList() {
            final Map rtn = new TreeMap();
            for (final Iterator it = _attendTestKindItemList.iterator(); it.hasNext();) {
                final TestItem item = (TestItem) it.next();
                if (null != item._semesterDetail && null != item._semesterDetail._cdSemesterDetail) {
                    rtn.put(item._semesterDetail._cdSemesterDetail, item._semesterDetail);
                }
            }
            Semester semester9 = null;
            for (final Iterator it = _semesterList.iterator(); it.hasNext();) {
                final Semester semester = (Semester) it.next();
                if (semester._semester.equals(SEMEALL)) {
                    semester9 = semester;
                    final SemesterDetail semesterDetail9 = new SemesterDetail(semester9, SEMEALL, "学年", semester9._dateRange._sdate, semester9._dateRange._edate);
                    semester9._semesterDetailList.add(semesterDetail9);
                    rtn.put(SEMEALL, semesterDetail9);
                    break;
                }
            }
            return new ArrayList(rtn.values());
        }

        private Map getStampNoMap(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     STAFFCD, ");
            stb.append("     MAX(STAMP_NO) AS STAMP_NO ");
            stb.append(" FROM ");
            stb.append("     ATTEST_INKAN_DAT ");
            stb.append(" GROUP BY ");
            stb.append("     STAFFCD ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map retMap = new HashMap();
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String staffcd = rs.getString("STAFFCD");
                    final String stampNo = rs.getString("STAMP_NO");
                    retMap.put(staffcd, stampNo);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retMap;
        }

        public String getStaffImageFilePath(final String staffCd) {
            final String stampNo = (String) _stampMap.get(staffCd);
            final String path = _documentroot + "/image/stamp/" + stampNo + ".bmp";
            final boolean exists = new java.io.File(path).exists();
            log.warn(" path " + path + " exists: " + exists);
            if (exists) {
                return path;
            }
            return null;
        }

        private Map getKindNameMap(final DB2UDB db2) {
            Map rtnMap = new HashMap();

            final String sql = getKindNameSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    Map map = new HashMap();
                    final String kindNo = StringUtils.defaultString(rs.getString("KIND_NO"));
                    final String kindSeq = StringUtils.defaultString(rs.getString("KIND_SEQ"));
                    final String kindRemark = StringUtils.defaultString(rs.getString("KIND_REMARK"));
                    if (rtnMap.containsKey(kindNo)) {
                        map = (Map) rtnMap.get(kindNo);
                    }
                    map.put(kindSeq, kindRemark);
                    rtnMap.put(kindNo, map);
                }
            } catch (SQLException ex) {
                log.error("setSubclassList exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtnMap;
        }

        private String getKindNameSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     KIND_NO, ");
            stb.append("     KIND_SEQ, ");
            stb.append("     KIND_REMARK ");
            stb.append(" FROM ");
            stb.append("     HREPORT_GUIDANCE_KIND_NAME_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _loginYear + "' ");
            stb.append(" ORDER BY ");
            stb.append("     KIND_NO, ");
            stb.append("     KIND_SEQ ");

            return stb.toString();
        }

        private String getKindName(final String kindNo, final String kindSeq) {
            String kindName = "";
            if(_kindNameMap.containsKey(kindNo)) {
                final Map map = (Map) _kindNameMap.get(kindNo);
                if(map.containsKey(kindSeq)) {
                    kindName = (String) map.get(kindSeq);
                }
            }
            return kindName;
        }


        private Map getHreportConditionMap(final DB2UDB db2) {
            Map rtnMap = new HashMap();

            final String sql = getHreportConditionSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    Map map = new HashMap();
                    final String seq = StringUtils.defaultString(rs.getString("SEQ"));
                    map.put("REMARK1", StringUtils.defaultString(rs.getString("REMARK1")));
                    map.put("REMARK2", StringUtils.defaultString(rs.getString("REMARK2")));
                    map.put("REMARK3", StringUtils.defaultString(rs.getString("REMARK3")));
                    map.put("REMARK4", StringUtils.defaultString(rs.getString("REMARK4")));
                    map.put("REMARK5", StringUtils.defaultString(rs.getString("REMARK5")));
                    map.put("REMARK6", StringUtils.defaultString(rs.getString("REMARK6")));
                    map.put("REMARK7", StringUtils.defaultString(rs.getString("REMARK7")));
                    map.put("REMARK8", StringUtils.defaultString(rs.getString("REMARK8")));
                    map.put("REMARK9", StringUtils.defaultString(rs.getString("REMARK9")));
                    map.put("REMARK10", StringUtils.defaultString(rs.getString("REMARK10")));
                    rtnMap.put(seq, map);
                }
            } catch (SQLException ex) {
                log.error("setSubclassList exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtnMap;
        }

        private String getHreportConditionSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     HREPORT_CONDITION_DAT ");
            stb.append(" WHERE YEAR        = '" + _loginYear + "' ");
            stb.append("   AND SCHOOLCD    = '" + _schoolCd + "' ");
            stb.append("   AND SCHOOL_KIND = '" + _schoolKind + "' ");
            stb.append("   AND GRADE       = '00' ");
            stb.append("   AND COURSECD    = '0' ");
            stb.append("   AND MAJORCD     = '000' ");
            stb.append("   AND COURSECODE  = '0000' ");
            return stb.toString();
        }

        private String getHreportCondition(final String seq, final String remark) {
            String rtnStr = "";
            if(_hreportConditionMap.containsKey(seq)) {
                final Map map = (Map) _hreportConditionMap.get(seq);
                if(map.containsKey(remark)) {
                    rtnStr = (String) map.get(remark);
                }
            }
            return rtnStr;
        }

    }
}

// eof
