/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: 20c7c06032b6e2873c821aaaf43abe5fef991faf $
 *
 * 作成日: 2020/04/07
 * 作成者: matsushima
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;

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

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
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


public class KNJE390N_sien {

    private static final Log log = LogFactory.getLog(KNJE390N_sien.class);

    private boolean _hasData;

    final int PAGE_MAXLINE1 = 45;
    final int PAGE_MAXLINE2 = 38;

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

        svf.VrSetForm("KNJE390N_B_1.frm", 4);

        for (int i = 0; i <= 60; i++) {
        	svf.VrAttribute("HEAD_" + String.valueOf(i), ATTR_PAINT_GRAY_FILL);
        }

        //各生徒毎の出力処理
        //ページ先頭の情報を出力
        printTopInfo(db2, svf, student);

        //1ページ目
        printPage1(db2, svf, student);

        //2ページ目
        printPage2(db2, svf, student);

        _hasData = true;
    }


    private void printTopInfo(final DB2UDB db2, final Vrw32alp svf, final StudentLoc student) {

    	//枠外
    	svf.VrsOut("SCHOOL_NAME", _param._knjSchoolMst._schoolName1);
        final StringBuffer sql4 = new StringBuffer();
        sql4.append(" SELECT ");
        sql4.append("   REMARK AS RECORD_STAFFNAMES ");
        sql4.append(" FROM SCHREG_CHALLENGED_SUPPORTPLAN_DAT ");
        sql4.append(" WHERE ");
        sql4.append("   YEAR = '" + _param._ctrlYear + "'");
        sql4.append("   AND SCHREGNO = '" + student._schregno + "' ");
        sql4.append("   AND SPRT_DIV = '01' ");
        sql4.append("   AND SPRT_SEQ = '03' ");
        final List resultList4 = getRowList(db2, sql4.toString());
        String recStaff = null;
        if (resultList4.size() > 0) {
            final Map row = (Map) resultList4.get(0);
            recStaff = getString("RECORD_STAFFNAMES", row);
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
        svf.VrsOut("GRADE_NAME", student._grade_Name1 + ("2".equals(_param._printHrClassType) ? student._ghr_Nameabbv : student._hrNameAbbv));

        final int entryMaxLine = 1;
        final Map updateMap = getUpdateHistMap(db2, "C", null, student._schoolkind_Name, entryMaxLine, student._writingDate);
        svf.VrsOut("MAKE_DATE", formatDate(db2, getString("CREATE_DATE", updateMap))); // 作成日
        svf.VrsOut("UPDATE", formatDate(db2, getString("UPDATE_DATE1", updateMap))); // 更新日
        final String mkfield = KNJ_EditEdit.getMS932ByteLength(student._recordStaffName) > 10 ? "2" : "1";
        svf.VrsOut("MAKER" + mkfield, student._recordStaffName);
        svf.VrsOut("ENTRY_PERSON", getString("UPDATE_STAFFNAME1", updateMap));

        final int nklen = KNJ_EditEdit.getMS932ByteLength(student._name_Kana);
        final String nkfield = nklen > 30 ? "3" : "2";
        svf.VrsOut("KANA" + nkfield, student._name_Kana);
        final int nlen = KNJ_EditEdit.getMS932ByteLength(student._name);
        final String nfield = nlen > 26 ? "3" : "2";
        svf.VrsOut("NAME" + nfield, student._name);
        svf.VrsOut("SEX", student._sex);
        svf.VrsOut("BIRTHDAY", formatDate(db2, student._birthday));
//        svf.VrsOut("NOTEBOOK1", "身体障害者手帳");
//        svf.VrsOut("CLASS1", append(student._card_Class, "種") + " " + append(student._card_Rank, "級"));
//        svf.VrsOut("NOTEBOOK2", "療育手帳");
//        svf.VrsOut("CLASS2", student._card_Name);
//        svf.VrsOut("NOTEBOOK3", "精神障害者保健福祉手帳");
//        svf.VrsOut("CLASS3", append(student._card_Remark, "級"));
//        svf.VrsOut("DIAG_NAME", append(student._card_Remark, "級"));
        List challengedNames = knjobj.retDividString(student._challengedNames, 50, 6); //障害名等
    	for (int i = 0 ; i < challengedNames.size() ; i++) {
        	svf.VrsOutn("DIAG_NAME", i+1, (String) challengedNames.get(i));
    	}
    }

    //1ページ目
    private void printPage1(final DB2UDB db2, final Vrw32alp svf, final StudentLoc student) {
    	//変数宣言
    	int maxLen = 0;
		List onesHopePresent = new ArrayList(); //現在の願い 本人
		List guardianHopePresent = new ArrayList(); //現在の願い 保護者
		List onesHopeFuture = new ArrayList(); //将来の希望 本人
		List guardianHopeFuture = new ArrayList(); //将来の希望 保護者
		List reasonableAccommodation = new ArrayList(); //合理的配慮等
		List selfrelianceGoal = new ArrayList(); //目指したい自立の姿
		List supportPlan = new ArrayList(); //今年度の支援方針

		//値の設定
        for (Iterator iterator = student._schregChallengedSupportplanMain.iterator(); iterator.hasNext();) {
            final SchregChallengedSupportplanMain printData = (SchregChallengedSupportplanMain) iterator.next();
    		onesHopePresent = knjobj.retDividString(printData._ones_hope_present, 40, 3);
    		guardianHopePresent = knjobj.retDividString(printData._guardian_hope_present, 40, 3);
    		onesHopeFuture = knjobj.retDividString(printData._ones_hope_future, 40, 3);
    		guardianHopeFuture = knjobj.retDividString(printData._guardian_hope_future, 40, 3);
    		reasonableAccommodation = knjobj.retDividString(printData._reasonable_accommodation, 104, 20);
    		selfrelianceGoal = knjobj.retDividString(printData._selfreliance_goal, 104, 10);
    		supportPlan = knjobj.retDividString(printData._support_plan, 90, 10);
        }

        //2段目
        svf.VrsOut("TITLE", "現在の願い・将来の希望"); //項目名称
        svf.VrAttribute("TITLE", ATTR_PAINT_GRAY_FILL);
        svf.VrsOut("TITLE_WHOLE_DIV", "1");
        maxLen = onesHopePresent.size() > onesHopeFuture.size() ? onesHopePresent.size() : onesHopeFuture.size();
        svf.VrsOut("DIVIDEHOPE_2", "(現在)");
        svf.VrsOut("DIVIDEHOPE_3", "(将来)");
        for (int i = 0 ; i < maxLen; i++) {
        	if((maxLen/2) == i) svf.VrsOut("DIVIDEHOPE_1", "本人");
            svf.VrAttribute("DIVIDEHOPE_1", ATTR_PAINT_GRAY_FILL);
            svf.VrsOut("GRPHOPE_1", "11"); //グループ
            svf.VrsOut("GRPHOPE_2", "11"); //グループ
            svf.VrsOut("GRPHOPE_3", "11"); //グループ
            svf.VrsOut("GRPHOPE_4", "11"); //グループ
            svf.VrsOut("GRPHOPE_5", "11"); //グループ
            if(i < onesHopePresent.size()) svf.VrsOut("LEAD1", (String) onesHopePresent.get(i));
            if(i < onesHopeFuture.size()) svf.VrsOut("LEAD2", (String) onesHopeFuture.get(i));
            svf.VrEndRecord();
        }
        if(maxLen == 0) {
        	svf.VrsOut("DIVIDEHOPE_1", "本人");
        	svf.VrAttribute("DIVIDEHOPE_1", ATTR_PAINT_GRAY_FILL);
        	svf.VrsOut("GRPHOPE_1", "11"); //グループ
            svf.VrsOut("GRPHOPE_2", "11"); //グループ
            svf.VrsOut("GRPHOPE_3", "11"); //グループ
            svf.VrsOut("GRPHOPE_4", "11"); //グループ
            svf.VrsOut("GRPHOPE_5", "11"); //グループ
        	svf.VrEndRecord();
        }
        maxLen = guardianHopePresent.size() > guardianHopeFuture.size() ? guardianHopePresent.size() : guardianHopeFuture.size();
        svf.VrsOut("DIVIDEHOPE_2", "(現在)");
        svf.VrsOut("DIVIDEHOPE_3", "(将来)");
        for (int i = 0 ; i < maxLen; i++) {
        	if((maxLen/2) == i) svf.VrsOut("DIVIDEHOPE_1", "保護者");
            svf.VrAttribute("DIVIDEHOPE_1", ATTR_PAINT_GRAY_FILL);
            svf.VrsOut("GRPHOPE_1", "12"); //グループ
            svf.VrsOut("GRPHOPE_2", "12"); //グループ
            svf.VrsOut("GRPHOPE_3", "12"); //グループ
            svf.VrsOut("GRPHOPE_4", "12"); //グループ
            svf.VrsOut("GRPHOPE_5", "12"); //グループ
            if(i < guardianHopePresent.size()) svf.VrsOut("LEAD1", (String) guardianHopePresent.get(i));
            if(i < guardianHopeFuture.size()) svf.VrsOut("LEAD2", (String) guardianHopeFuture.get(i));
            svf.VrEndRecord();
        }
        if(maxLen == 0) {
        	svf.VrsOut("DIVIDEHOPE_1", "保護者");
        	svf.VrAttribute("DIVIDEHOPE_1", ATTR_PAINT_GRAY_FILL);
        	 svf.VrsOut("GRPHOPE_1", "12"); //グループ
             svf.VrsOut("GRPHOPE_2", "12"); //グループ
             svf.VrsOut("GRPHOPE_3", "12"); //グループ
             svf.VrsOut("GRPHOPE_4", "12"); //グループ
             svf.VrsOut("GRPHOPE_5", "12"); //グループ
        	svf.VrEndRecord();
        }

        //3段目
    	svf.VrsOut("TITLE", "合理的配慮等");//項目名称
    	svf.VrAttribute("TITLE", ATTR_PAINT_GRAY_FILL);
        svf.VrsOut("TITLE_WHOLE_DIV", "2");
        maxLen = reasonableAccommodation.size();
        for (int i = 0 ; i < maxLen; i++) {
            svf.VrsOut("GRP_COMMENT", "2"); //グループ
            svf.VrsOut("COMMENT", (String) reasonableAccommodation.get(i));
            svf.VrEndRecord();
        }
        if(maxLen == 0) {
        	svf.VrsOut("GRP_COMMENT", "2"); //グループ
        	svf.VrEndRecord();
        }

        //4段目
    	svf.VrsOut("TITLE", "目指したい自立の姿");//項目名称
    	svf.VrAttribute("TITLE", ATTR_PAINT_GRAY_FILL);
        svf.VrsOut("TITLE_WHOLE_DIV", "3");
        maxLen = selfrelianceGoal.size();
        for (int i = 0 ; i < maxLen; i++) {
            svf.VrsOut("GRP_COMMENT", "3"); //グループ
            svf.VrsOut("COMMENT", (String) selfrelianceGoal.get(i));
            svf.VrEndRecord();
        }
        if(maxLen == 0) {
        	 svf.VrsOut("GRP_COMMENT", "3"); //グループ
        	svf.VrEndRecord();
        }

        //5段目
        maxLen = supportPlan.size();
        for (int i = 0 ; i < maxLen; i++) {
            if((maxLen/2) == i) svf.VrsOut("DIVI_LEAD", "今年度の支援方針");//項目名称
            svf.VrAttribute("DIVI_LEAD", ATTR_PAINT_GRAY_FILL);
            svf.VrsOut("GRPSEIIKU_1", "4"); //グループ
            svf.VrsOut("GRPSEIIKU_2", "4"); //グループ
            svf.VrsOut("LEAD", (String) supportPlan.get(i));
            svf.VrEndRecord();
        }
        if(maxLen == 0) {
        	svf.VrsOut("DIVI_LEAD", "今年度の支援方針");//項目名称
        	svf.VrAttribute("DIVI_LEAD", ATTR_PAINT_GRAY_FILL);
            svf.VrsOut("GRPSEIIKU_1", "4"); //グループ
            svf.VrsOut("GRPSEIIKU_2", "4"); //グループ
        	svf.VrEndRecord();
        }
        _hasData = true;
    }

    //2ページ目
    private void printPage2(final DB2UDB db2, final Vrw32alp svf, final StudentLoc student) {
    	final String form1 = "KNJE390N_B_2_1.frm";
        final String form2= "KNJE390N_B_2_2.frm";
        boolean lastPageFlg = false;
        final String[] date = KNJ_EditDate.tate_format4(db2, _param._ctrlDate);

        int outLine = getPage2OutLine(db2, student, 0); //出力行数
        int outLine1 = getPage2OutLine(db2, student, 1); //出力行数(具体的な支援)
        int outLine2 = getPage2OutLine(db2, student, 2); //出力行数(連携の記録)
        if(outLine < 2 ) return;

        //フォームの判定
        if(PAGE_MAXLINE2 < outLine) {
        	svf.VrSetForm(form1, 4);
        } else {
        	svf.VrSetForm(form2, 4);
        	svf.VrsOut("ERA_NAME", date[0]); //元号
        	lastPageFlg = true;
        }

        int line = 1;
        //具体的な支援
        svf.VrsOut("GRP1_1", "--"); //グループ
        svf.VrsOut("GRP1_2", "00"); //グループ
        svf.VrsOut("GRP1_3", "00"); //グループ
        svf.VrsOut("GRP1_4", "00"); //グループ
        svf.VrsOut("GRP1_5", "00"); //グループ
        svf.VrsOut("DIVIDE1_1", ""); //表タイトル
        svf.VrsOut("DIVIDE1_2", ""); //項目名
        svf.VrsOut("FACILITY1", "支援内容");//支援内容
        svf.VrsOut("SUPPRT_ORG", "支援機関・支援者");//支援機関・支援者
        svf.VrsOut("SUPPORT_DIV", "支援内容に関する評価");//支援内容に関する評価
        svf.VrAttribute("DIVIDE1_1", ATTR_PAINT_GRAY_FILL);
        svf.VrAttribute("DIVIDE1_2", ATTR_PAINT_GRAY_FILL);
        svf.VrAttribute("FACILITY1", ATTR_PAINT_GRAY_FILL);
        svf.VrAttribute("SUPPRT_ORG", ATTR_PAINT_GRAY_FILL);
        svf.VrAttribute("SUPPORT_DIV", ATTR_PAINT_GRAY_FILL);
        svf.VrEndRecord();

        if(outLine1 > 2 ) {
            line += 1; //ヘッダ行

            int hyouIdx = 0; //表題用
            boolean firstFlg = true;
            List hyouName = knjobj.retDividString("具体的な支援", 2, OUTPUT_FULL_GYO);
            for (Iterator iterator = student._schregChallengedSupportplanStatus.iterator(); iterator.hasNext();) {
                final SchregChallengedSupportplanStatus printData = (SchregChallengedSupportplanStatus) iterator.next();

                final String target = ("01".equals(printData._data_div)) ? "学校生活" : ("02".equals(printData._data_div)) ? "家庭生活"
                        : ("03".equals(printData._data_div)) ? "余暇・地域生活・福祉" : ("04".equals(printData._data_div)) ? "進路・労働"
                        : ("05".equals(printData._data_div)) ? "医療" : ("06".equals(printData._data_div)) ? "その他(カウンセリング等)" : "";
                final int targetKeta = ("03".equals(printData._data_div) || "06".equals(printData._data_div)) ? 6 : 4;

                if("".equals(target.toString())) continue;
                List columName = knjobj.retDividString(target, targetKeta, OUTPUT_FULL_GYO);
                List status = knjobj.retDividString(printData._status, 30, 20);
                List status2 = knjobj.retDividString(printData._status2, 24, 20);
                List status3 = knjobj.retDividString(printData._status3, 24, 20);
                int maxLen = columName.size();
                if(maxLen < status.size()) maxLen = status.size();
                if(maxLen < status2.size()) maxLen = status2.size();
                if(maxLen < status3.size()) maxLen = status3.size();
                line += maxLen; //行数加算

                //改ページ判定
                if(!lastPageFlg) {
                    if(PAGE_MAXLINE1 < line) {
                        outLine  = outLine - line;
                        line = 1;
                        if(PAGE_MAXLINE2 < outLine) {
                        	//途中ページ
                            svf.VrSetForm(form1, 4);
                        } else {
                        	//最終ページ
                            svf.VrSetForm(form2, 4);
                            svf.VrsOut("ERA_NAME", date[0]); //元号
                            lastPageFlg = true;
                        }
                    }
                }

                for (int i = 0 ; i < maxLen; i++) {
                    svf.VrsOut("GRP1_1", "--"); //グループ
                    svf.VrsOut("GRP1_2", printData._data_div); //グループ
                    svf.VrsOut("GRP1_3", printData._data_div); //グループ
                    svf.VrsOut("GRP1_4", printData._data_div); //グループ
                    svf.VrsOut("GRP1_5", printData._data_div); //グループ
                    if(firstFlg && hyouIdx < hyouName.size()) svf.VrsOut("DIVIDE1_1", (String) hyouName.get(hyouIdx)); //表タイトル
                    if(i < columName.size()) svf.VrsOut("DIVIDE1_2", (String) columName.get(i)); //項目名
                    if(i < status.size()) svf.VrsOut("FACILITY1", (String) status.get(i)); //支援内容
                    if(i < status2.size()) svf.VrsOut("SUPPRT_ORG", (String) status2.get(i)); //支援機関・支援者
                    if(i < status3.size()) svf.VrsOut("SUPPORT_DIV", (String) status3.get(i)); //支援内容に関する評価
                    svf.VrAttribute("DIVIDE1_1", ATTR_PAINT_GRAY_FILL);
                    svf.VrAttribute("DIVIDE1_2", ATTR_PAINT_GRAY_FILL);
                    svf.VrEndRecord();
                    hyouIdx++;
                }
                if(hyouIdx >= hyouName.size()) firstFlg = false;
            }

            //最終ページ判定
            if(!lastPageFlg) {
                //最終ページ以外の場合、改ページ
                svf.VrSetForm(form2, 4);
                svf.VrsOut("ERA_NAME", date[0]); //元号
                lastPageFlg = true;
            }
        }else {
        	String[] gcd = {"11","11","12","12","13","13","13","13","14","14","14","15","16","16","16","16","16"};
        	String[] cname = {"学校","生活","家庭","生活","余暇・","地域生","活・福","祉","進路","・労","働","医療","その他","(カウ","ンセリ","ング等",")"};
        	List hyouName = knjobj.retDividString("具体的な支援", 2, OUTPUT_FULL_GYO);
        	for (int i = 0 ; i < cname.length; i++) {
            	svf.VrsOut("GRP1_1", "--"); //グループ
                svf.VrsOut("GRP1_2", gcd[i]); //グループ
                svf.VrsOut("GRP1_3", gcd[i]); //グループ
                svf.VrsOut("GRP1_4", gcd[i]); //グループ
                svf.VrsOut("GRP1_5", gcd[i]); //グループ
                if(i < hyouName.size())svf.VrsOut("DIVIDE1_1", (String) hyouName.get(i)); //表タイトル
                if(i < cname.length) svf.VrsOut("DIVIDE1_2", cname[i]); //項目名
                svf.VrAttribute("DIVIDE1_1", ATTR_PAINT_GRAY_FILL);
                svf.VrAttribute("DIVIDE1_2", ATTR_PAINT_GRAY_FILL);
                svf.VrEndRecord();
            }
        }

        //連携の記録
        if(outLine2 > 2 ) {
            svf.VrsOut("TITLE", "連携の記録"); //連携の記録 タイトル
            svf.VrAttribute("TITLE", ATTR_PAINT_GRAY_FILL);
            line += 1; //ヘッダ行
            for (Iterator iterator = student._schregChallengedSupportplanMain.iterator(); iterator.hasNext();) {
                final SchregChallengedSupportplanMain printData = (SchregChallengedSupportplanMain) iterator.next();
                List record = knjobj.retDividString(printData._record, 90, 10);
                for (int i = 0 ; i < record.size(); i++) {
                	svf.VrsOut("GRP3_1", "3"); //グループ
                    svf.VrsOut("COOPERATE", (String) record.get(i)); //連携の記録
                    svf.VrEndRecord();
                }
                line += record.size();
            }
        }else {
        	svf.VrsOut("TITLE", "連携の記録"); //連携の記録 タイトル
            svf.VrAttribute("TITLE", ATTR_PAINT_GRAY_FILL);
            svf.VrsOut("GRP3_1", "3"); //グループ
            svf.VrEndRecord();
        }
        svf.VrEndPage();
        _hasData = true;
    }

    //2ページ目の行数取得
    private int getPage2OutLine(final DB2UDB db2, final StudentLoc student, final int countKbn) {
    	//countKbn == 0：全行 1：具体的な支援の行数　2：連携の記録の行数
        int totalLine = 1;

        if(countKbn == 0 || countKbn == 1) {
            //具体的な支援
            totalLine += 1; //ヘッダ行
            for (Iterator iterator = student._schregChallengedSupportplanStatus.iterator(); iterator.hasNext();) {
                final SchregChallengedSupportplanStatus printData = (SchregChallengedSupportplanStatus) iterator.next();

                final String target = ("01".equals(printData._data_div)) ? "学校生活" : ("02".equals(printData._data_div)) ? "家庭生活"
                        : ("03".equals(printData._data_div)) ? "余暇・地域生活・福祉" : ("04".equals(printData._data_div)) ? "進路・労働"
                        : ("05".equals(printData._data_div)) ? "医療" : ("06".equals(printData._data_div)) ? "その他(カウンセリング等)" : "";
                final int targetKeta = ("03".equals(printData._data_div) || "06".equals(printData._data_div)) ? 6 : 4;

                if("".equals(target.toString())) continue;
                List columName = knjobj.retDividString(target, targetKeta, OUTPUT_FULL_GYO);
                List status = knjobj.retDividString(printData._status, 30, 20);
                List status2 = knjobj.retDividString(printData._status2, 24, 20);
                List status3 = knjobj.retDividString(printData._status3, 24, 20);
                int maxLen = columName.size() > status.size() ? columName.size() : status.size();
                if(maxLen < status2.size()) maxLen = status2.size();
                if(maxLen < status3.size()) maxLen = status3.size();
                totalLine += maxLen;
            }
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

    private String append(final String a, final String b) {
        if (StringUtils.isBlank(a)) {
            return "";
        }
        return a + StringUtils.defaultString(b);
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
                final String schregno = StringUtils.defaultString(rs.getString("SCHREGNO"));
                final String name = StringUtils.defaultString(rs.getString("NAME"));
                final String name_Kana = StringUtils.defaultString(rs.getString("NAME_KANA"));
                final String sex = StringUtils.defaultString(rs.getString("SEX"));
                final String birthday = StringUtils.defaultString(rs.getString("BIRTHDAY"));
                final String addr = StringUtils.defaultString(rs.getString("ADDR"));
                final String telno = StringUtils.defaultString(rs.getString("TELNO"));
                final String coursecd = StringUtils.defaultString(rs.getString("COURSECD"));
                final String coursename = StringUtils.defaultString(rs.getString("COURSENAME"));
                final String hrNameAbbv = StringUtils.defaultString(rs.getString("HR_NAMEABBV"));
                final String ghr_Cd = StringUtils.defaultString(rs.getString("GHR_CD"), "");
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
                final String hope_Honnin = StringUtils.defaultString(rs.getString("HOPE_HONNIN"));
                final String hope_Hogosya = StringUtils.defaultString(rs.getString("HOPE_HOGOSYA"));
                final String base_Info = StringUtils.defaultString(rs.getString("BASE_INFO"));
                final String hairyo = StringUtils.defaultString(rs.getString("HAIRYO"));
                final String guardName = StringUtils.defaultString(rs.getString("GUARD_NAME"));
                final String hikitugi = StringUtils.defaultString(rs.getString("HIKITUGI"));
                final String writingDate = StringUtils.defaultString(rs.getString("WRITING_DATE"));
                final String challengedNames = StringUtils.defaultString(rs.getString("CHALLENGED_NAMES"));
                final String recordStaffName = StringUtils.defaultString(rs.getString("RECORD_STAFFNAME"));


                student = new StudentLoc(grade, schoolkind_Name, grade_Cd, grade_Name1,
                                                     schregno, name, name_Kana, sex, birthday, addr, telno, coursecd, coursename, hrNameAbbv, ghr_Cd, ghr_Nameabbv, ghr_Attendno, challenged_Card_Class, card_Class, challenged_Card_Rank,
                                                     card_Rank, challenged_Card_Name, card_Name, challenged_Card_Remark, card_Remark,
                                                     hope_Honnin, hope_Hogosya, base_Info, hairyo, guardName, hikitugi, writingDate, challengedNames, recordStaffName
                                                    );

                SchregChallengedSupportplanMain.setSchregChallengedSupportplanMain(db2, _param, student);
                SchregChallengedSupportplanStatus.setSchregChallengedSupportplanStatus(db2, _param, student);
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
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("   A023.ABBV1 AS SCHOOLKIND_NAME, ");
        stb.append("   T2.GRADE, ");
        stb.append("   T5.GRADE_CD, ");
        stb.append("   T5.GRADE_NAME1, ");
        stb.append("   T2WK.SCHREGNO, ");
        stb.append("   T3.NAME, ");
        stb.append("   T3.NAME_KANA, ");
        stb.append("   Z002.ABBV1 AS SEX, ");
        stb.append("   T3.BIRTHDAY, ");
        stb.append("   VALUE(T6.ADDR1, '') || VALUE(T6.ADDR2, '') AS ADDR, ");
        stb.append("   T6.TELNO, ");
        stb.append("   T2.COURSECD, ");
        stb.append("   T14.COURSENAME, ");
        stb.append("   T4.HR_NAMEABBV, ");
        stb.append("   T15.GHR_CD, ");
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
        stb.append("   T8_01.REMARK AS HOPE_HONNIN, ");
        stb.append("   T8_02.REMARK AS HOPE_HOGOSYA, ");
        stb.append("   T9.REMARK AS BASE_INFO, ");
        stb.append("   T10.REMARK AS HAIRYO, ");
        stb.append("   T11.GUARD_NAME, ");
        stb.append("   T12_01.REMARK AS HIKITUGI, ");
        stb.append("   T7.WRITING_DATE, ");
        stb.append("   T7.CHALLENGED_NAMES, ");
        stb.append("   T13.RECORD_STAFFNAME ");
        stb.append(" FROM ");
        stb.append("   MXSCHREG T2WK ");
        stb.append("   LEFT JOIN SCHREG_CHALLENGED_SUPPORT_FACILITY_GRP_DAT T1 ");
        stb.append("     ON T1.YEAR = T2WK.YEAR ");
        stb.append("    AND T1.SCHREGNO = T2WK.SCHREGNO ");
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
        stb.append("    AND T7.RECORD_DATE = 'NEW' ");
        stb.append("   LEFT JOIN SCHREG_CHALLENGED_SUPPORTPLAN_DAT T8_01 ");
        stb.append("     ON T8_01.YEAR = T2WK.YEAR ");
        stb.append("    AND T8_01.SCHREGNO = T2WK.SCHREGNO ");
        stb.append("    AND T8_01.SPRT_DIV = '01' ");
        stb.append("    AND T8_01.SPRT_SEQ = '01' ");
        stb.append("   LEFT JOIN SCHREG_CHALLENGED_SUPPORTPLAN_DAT T8_02 ");
        stb.append("     ON T8_02.YEAR = T2WK.YEAR ");
        stb.append("    AND T8_02.SCHREGNO = T2WK.SCHREGNO ");
        stb.append("    AND T8_02.SPRT_DIV = '01' ");
        stb.append("    AND T8_02.SPRT_SEQ = '02' ");
        stb.append("   LEFT JOIN SCHREG_CHALLENGED_SUPPORTPLAN_DAT T9 ");
        stb.append("     ON T9.YEAR = T2WK.YEAR ");
        stb.append("    AND T9.SCHREGNO = T2WK.SCHREGNO ");
        stb.append("    AND T9.SPRT_DIV = '02' ");
        stb.append("    AND T9.SPRT_SEQ = '01' ");
        stb.append("   LEFT JOIN SCHREG_CHALLENGED_SUPPORTPLAN_DAT T10 ");
        stb.append("     ON T10.YEAR = T2WK.YEAR ");
        stb.append("    AND T10.SCHREGNO = T2WK.SCHREGNO ");
        stb.append("    AND T10.SPRT_DIV = '03' ");
        stb.append("    AND T10.SPRT_SEQ = '01' ");
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
        stb.append("   LEFT JOIN SCHREG_CHALLENGED_SUPPORTPLAN_DAT T12_01 ");
        stb.append("     ON T12_01.YEAR = T2WK.YEAR ");
        stb.append("    AND T12_01.SCHREGNO = T2WK.SCHREGNO ");
        stb.append("    AND T12_01.SPRT_DIV = '05' ");
        stb.append("    AND T12_01.SPRT_SEQ = '01' ");
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
        stb.append(" WHERE ");
        stb.append("    T2WK.YEAR = '" + _param._ctrlYear + "' ");
//        stb.append("    AND T2WK.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
        stb.append("    AND T2WK.SCHREGNO = ? ");

        return stb.toString();
    }

    private class StudentLoc {
        final String _grade;
        final String _schoolkind_Name;
        final String _grade_Cd;
        final String _grade_Name1;
        final String _schregno;
        final String _name;
        final String _name_Kana;
        final String _sex;
        final String _birthday;
        final String _addr;
        final String _telno;
        final String _coursecd;
        final String _coursename;
        final String _hrNameAbbv;
        final String _ghr_Cd;
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
        final String _hope_Honnin;
        final String _hope_Hogosya;
        final String _base_Info;
        final String _hairyo;
        final String _guardName;
        final String _hikitugi;
        final String _writingDate;
        final String _challengedNames;
        final String _recordStaffName;
        List _schregChallengedSupportplanMain = new ArrayList();
        List _schregChallengedSupportplanStatus = new ArrayList();


        public StudentLoc(
                final String grade, final String schoolkind_Name, final String grade_Cd, final String grade_Name1, final String schregno, final String name, final String name_Kana, final String sex, final String birthday, final String addr, final String telno, final String coursecd, final String coursename, final String hrNameAbbv, final String ghr_Cd, final String ghr_Nameabbv, final String ghr_Attendno, final String challenged_Card_Class, final String card_Class, final String challenged_Card_Rank, final String card_Rank, final String challenged_Card_Name, final String card_Name, final String challenged_Card_Remark, final String card_Remark, final String hope_Honnin, final String hope_Hogosya, final String base_Info, final String hairyo, final String guardName, final String hikitugi, final String writingDate, final String challengedNames, final String recordStaffName
                ) {
            _grade = grade;
            _schoolkind_Name = schoolkind_Name;
            _grade_Cd = grade_Cd;
            _grade_Name1 = grade_Name1;
            _schregno = schregno;
            _name = name;
            _name_Kana = name_Kana;
            _sex = sex;
            _birthday = birthday;
            _addr = addr;
            _telno = telno;
            _coursecd = coursecd;
            _coursename = coursename;
            _hrNameAbbv = hrNameAbbv;
            _ghr_Cd = ghr_Cd;
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
            _hope_Honnin = hope_Honnin;
            _hope_Hogosya = hope_Hogosya;
            _base_Info = base_Info;
            _hairyo = hairyo;
            _guardName = guardName;
            _hikitugi = hikitugi;
            _writingDate = writingDate;
            _challengedNames = challengedNames;
            _recordStaffName = recordStaffName;
        }
    }

    private static class SchregChallengedSupportplanMain {
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

        public SchregChallengedSupportplanMain(
                final String ones_hope_present,
                final String guardian_hope_present,
                final String ones_hope_future,
                final String guardian_hope_future,
                final String reasonable_accommodation,
                final String selfreliance_goal,
                final String support_goal,
                final String support_plan,
                final String record,
                final String record_staffname
        ) {
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
                    final String ones_hope_future = StringUtils.defaultString(rs.getString("ONES_HOPE_FUTURE"));
                    final String guardian_hope_future = StringUtils.defaultString(rs.getString("GUARDIAN_HOPE_FUTURE"));
                    final String reasonable_accommodation = StringUtils.defaultString(rs.getString("REASONABLE_ACCOMMODATION"));
                    final String selfreliance_goal = StringUtils.defaultString(rs.getString("SELFRELIANCE_GOAL"));
                    final String support_goal = StringUtils.defaultString(rs.getString("SUPPORT_GOAL"));
                    final String support_plan = StringUtils.defaultString(rs.getString("SUPPORT_PLAN"));
                    final String record = StringUtils.defaultString(rs.getString("RECORD"));
                    final String record_staffname = StringUtils.defaultString(rs.getString("RECORD_STAFFNAME"));
                    final SchregChallengedSupportplanMain schregChallengedSupportplanMain = new SchregChallengedSupportplanMain(ones_hope_present, guardian_hope_present, ones_hope_future, guardian_hope_future,
                                                                                                                                        reasonable_accommodation, selfreliance_goal, support_goal, support_plan, record, record_staffname);

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
        sql.append("SELECT ");
        sql.append("   DATE(T1.UPDATED) AS UPDATE_DATE ");
        sql.append("   , T2.STAFFNAME AS UPDATE_STAFFNAME ");
        sql.append(" FROM CHALLENGED_TABLE_UPDATE_LOG T1 ");
        sql.append(" INNER JOIN (SELECT MIN(UPDATED) AS UPDATED FROM CHALLENGED_TABLE_UPDATE_LOG T0 ");
        sql.append("   WHERE ");
        sql.append("     T0.DATA_TYPE = '" + dataType + "' ");
        sql.append("     AND T0.SCHREGNO = '" + schregno + "' ");
        if (null != year) {
            sql.append("   AND T0.YEAR = '" + year + "' ");
        }
        sql.append("     AND T0.RECORD_DATE = '" + _param._recordDate + "' ");
        sql.append(" ) T3 ON T3.UPDATED = T1.UPDATED ");
        sql.append(" LEFT JOIN STAFF_MST T2 ON T2.STAFFCD = T1.STAFFCD ");
        sql.append(" ORDER BY 1 ");

        //log.debug(" update time sql = " + sql.toString());
        final List resultList = getRowList(db2, sql.toString());
        final Map rtn = new HashMap();

        String createName = null;
        if (resultList.size() > 0) {
            final Map row = (Map) resultList.get(0);
            createName = getString("UPDATE_STAFFNAME", row);
        }
        rtn.put("CREATE_STAFFNAME", createName);
        rtn.put("CREATE_DATE",  writingDate);

        final StringBuffer sql3 = new StringBuffer();
        sql3.append(" SELECT ");
        sql3.append("     RECORD_DATE, ");
        sql3.append("     RECORD_STAFFNAME ");
        sql3.append(" FROM ");
        sql3.append("     SCHREG_CHALLENGED_ASSESSMENT_MAIN_DAT ");
        sql3.append(" WHERE ");
        sql3.append("     SCHREGNO ='" + schregno + "' AND");
        sql3.append("     WRITING_DATE ='" + writingDate + "'");

        //log.debug(" update time sql = " + sql.toString());
        final List resultList3 = getRowList(db2, sql.toString());

        String recordStaffName = null;
        String recordDate = null;
        if (resultList3.size() > 0) {
            final Map row = (Map) resultList3.get(0);
            recordStaffName = getString("RECORD_STAFFNAME", row);
            recordDate = getString("RECORD_DATE", row);
        }
        rtn.put("RECORD_STAFFNAME", recordStaffName);
        rtn.put("RECORD_DATE",  recordDate);

        final StringBuffer sql2 = new StringBuffer();
        sql2.append("SELECT ");
        sql2.append("   DATE(T1.RECORD_DATE) AS UPDATE_DATE ");
        sql2.append("   , T2.STAFFNAME AS UPDATE_STAFFNAME ");
        sql2.append(" FROM CHALLENGED_TABLE_UPDATE_LOG T1 ");
        sql2.append(" INNER JOIN (SELECT RECORD_DATE, MIN(UPDATED) AS UPDATED FROM CHALLENGED_TABLE_UPDATE_LOG T0 ");
        sql2.append("   WHERE ");
        sql2.append("     T0.DATA_TYPE = '" + dataType + "' ");
        sql2.append("     AND T0.SCHREGNO = '" + schregno + "' ");
        if (null != year) {
            sql2.append("   AND T0.YEAR = '" + year + "' ");
        }
        sql2.append("   AND T0.RECORD_DATE <> 'NEW' ");
        sql2.append("   GROUP BY RECORD_DATE ");
        sql2.append(" ) T3 ON T3.UPDATED = T1.UPDATED ");
        sql2.append(" LEFT JOIN STAFF_MST T2 ON T2.STAFFCD = T1.STAFFCD ");
        sql2.append(" ORDER BY 1 ");

        //log.debug(" update time2 sql = " + sql2.toString());
        final List resultList2 = getRowList(db2, sql2.toString()); // 更新日付昇順

        final List list = new ArrayList(); // resultList2の最後から最大max件日付昇順
        for (int start = Math.max(resultList2.size() - max, 0), i = start; i < start + max; i++) {
            if (i < resultList2.size()) {
                list.add(resultList2.get(i));
            }
        }

        for (int n = 1; n <= max; n++) {
            String updateNameN = null, updateDateN = null;
            if (n - 1 < list.size()) {
                final Map row = (Map) list.get(n - 1);
                updateNameN = getString("UPDATE_STAFFNAME", row);
                updateDateN = getString("UPDATE_DATE", row);
            }
            rtn.put("UPDATE_STAFFNAME" + String.valueOf(n), updateNameN);
            rtn.put("UPDATE_DATE" + String.valueOf(n),  updateDateN);
        }
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
        log.fatal("$Revision: 77170 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

     /** パラメータクラス */

    private static class Param {
         final String[] _categorySelected;
         final String _ctrlYear;        //_year
         final String _ctrlSemester;    //_gakki
         final String _ctrlDate;
         final String _recordDate;
         KNJSchoolMst _knjSchoolMst;
         final Map _sqlQueryMap;
         final boolean _isOutputDebug;
         final String _printHrClassType;

 //      final String _schoolName;

         Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
             _categorySelected = request.getParameterValues("category_selected");
             _ctrlYear = request.getParameter("YEAR");
             _ctrlSemester = request.getParameter("GAKKI");
             _ctrlDate = request.getParameter("CTRL_DATE");
             _recordDate = "NEW";
             _knjSchoolMst = new KNJSchoolMst(db2, _ctrlYear);
             _sqlQueryMap = new HashMap();
             _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));
             _printHrClassType = request.getParameter("PRINT_HR_CLASS_TYPE");
         }

         private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
             return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJE390' AND NAME = '" + propName + "' "));
         }


    }
}

// eof
