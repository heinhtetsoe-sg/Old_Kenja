/*
 * $Id: 790110ebdba653958208cb6f1deae35e0a281be5 $
 *
 * 作成日: 2015/09/08
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

/**
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３００Ｎ＞  座席ラベル
 **/
public class KNJL507J_1 {

    private static final Log log = LogFactory.getLog(KNJL507J_1.class);
    private static final int KEKKAMAXCNT = 9;
    private static final int SCOREMAXCNT = 4;
    private static final int MAXLINECNT = 40;

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

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SRCHBASE_TBL AS ( ");
        //検索データを定義する。
        stb.append(" SELECT DISTINCT ");
        stb.append("  T1.ENTEXAMYEAR, ");
        stb.append("  T1.APPLICANTDIV, ");
        stb.append("  T1.TESTDIV, ");
        stb.append("  T2.RECOM_EXAMNO, ");
        stb.append("  T1.JUDGEDIV ");
        stb.append(" FROM ");
        stb.append("  ENTEXAM_RECEPT_DAT T1 ");
        stb.append("  INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ");
        stb.append("    ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("   AND T2.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("   AND T2.EXAMNO = T1.EXAMNO ");
        stb.append(" WHERE ");
        stb.append("  T1.ENTEXAMYEAR = '"+_param._entexamyear+"' ");
        stb.append("  AND T1.APPLICANTDIV = '"+_param._applicantdiv+"' ");
        stb.append(" ), PASTTEST_TBL AS ( ");
        //選択した試験種別よりも過去の試験を抽出
        stb.append(" SELECT DISTINCT ");
        stb.append("   T1.ENTEXAMYEAR, ");
        stb.append("   T1.APPLICANTDIV, ");
        stb.append("   T1.RECOM_EXAMNO ");
        stb.append(" FROM ");
        stb.append("    SRCHBASE_TBL T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.TESTDIV IN ( ");
        stb.append("         SELECT ");
        stb.append("             T4.TESTDIV ");
        stb.append("         FROM ");
        stb.append("             ENTEXAM_TESTDIV_MST T3 ");
        stb.append("             LEFT JOIN ENTEXAM_TESTDIV_MST T4 ");
        stb.append("                  ON T4.ENTEXAMYEAR = T3.ENTEXAMYEAR ");
        stb.append("                 AND T4.APPLICANTDIV = T3.APPLICANTDIV ");
        stb.append("                 AND T4.TESTDIV <> T3.TESTDIV ");
        stb.append("                 AND T4.TEST_DATE || '-' || T4.TESTDIV < T3.TEST_DATE || '-' || T3.TESTDIV ");
        stb.append("                 AND T4.INTERVIEW_DIV = T3.INTERVIEW_DIV ");
        stb.append("                 AND T4.INTERVIEW_DIV = '1' ");
        stb.append("         WHERE ");
        stb.append("             T3.ENTEXAMYEAR = '"+_param._entexamyear+"' ");
        stb.append("             AND T3.APPLICANTDIV = '"+_param._applicantdiv+"' ");
        if (!"".equals(_param._testdiv)) {
            stb.append("             AND T3.TESTDIV = '"+_param._testdiv+"' ");
        }
        stb.append("     ) ");
        stb.append("     AND VALUE(T1.JUDGEDIV,'') <> '4' ");

        stb.append(" ) ");
        // 以下、表示用データ。
        stb.append(" SELECT ");
        stb.append("  T1.RECEPTNO, ");
        stb.append("  T3_009.REMARK1 AS ORDERNO, ");
        stb.append("  T11_L013.ABBV1 AS PASS_UNPASS, ");
        stb.append("  CASE WHEN T4.RECOM_EXAMNO IS NOT NULL OR TM_TEKI.INTERVIEW_DIV IS NULL THEN '' ELSE '面' END AS INTERVIEW, ");
        stb.append("  CASE WHEN T1.JUDGEDIV = '4' THEN '欠' ELSE '' END AS ATTEND, ");
        stb.append("  T9.EXAMTYPE_NAME_ABBV AS EXAMTYPE, ");
        int datcnt = 1;
        for (Iterator itr = _param._testdivMap.keySet().iterator();itr.hasNext();) {
        	String keystr = (String)itr.next();
            if (keystr == null) continue;
            stb.append("  CASE WHEN T5_00"+datcnt+".JUDGEDIV = '1' THEN '合'  ");
            stb.append("       WHEN T5_00"+datcnt+".JUDGEDIV = '2' THEN '×' ");
            stb.append("       WHEN T5_00"+datcnt+".JUDGEDIV = '4' THEN '欠' ");
            stb.append("       WHEN T5_00"+datcnt+".RECOM_EXAMNO IS NOT NULL THEN '〇' ");
            stb.append("       ELSE '' END AS KEKKA"+datcnt+", ");
            datcnt++;
        }
        if ("2".equals(_param._applicantdiv)) {
        	stb.append("     T6_003.REMARK1 AS NAITEI, ");
        }
        stb.append("  CASE WHEN T6_004.REMARK1 = '1' THEN 'A' END AS CHK_A, ");
        stb.append("  CASE WHEN T6_004.REMARK2 = '1' THEN 'F' END AS CHK_F, ");
        stb.append("  CASE WHEN T6_004.REMARK3 = '1' THEN 'T' END AS CHK_T, ");
        stb.append("  CASE WHEN T6_004.REMARK4 = '1' THEN 'B' END AS CHK_B, ");
        stb.append("  CASE WHEN T6_004.REMARK5 = '1' THEN 'J' END AS CHK_J, ");
        if ("2".equals(_param._applicantdiv)) {
        	stb.append(" T6.TOTAL_ALL AS NAISINTEN, ");
        	stb.append(" T6.ABSENCE_DAYS3 AS KESSEKI, ");
        }
        stb.append("  T2.NAME, ");
        stb.append("  T2.NAME_KANA, ");

    	//各科目(対象科目は_testSubclassMapで保持しているので、中高共通)
        for (Iterator ite = _param._testSubclassMap.keySet().iterator();ite.hasNext();) {
        	String getSubClsCd = (String)ite.next();
        	String subClsTblName = "T12_"+getSubClsCd;
            stb.append("  " + subClsTblName+".SCORE AS SCORE"+getSubClsCd+", ");
        }
        if ("1".equals(_param._applicantdiv)) {
        	//小計(TOTAL2)
        	stb.append(" T1.TOTAL2, ");
        	//小計順位(TOTAL_RANK2)
        	stb.append(" T1.TOTAL_RANK2, ");
        }
    	//合計(TOTAL4)
    	stb.append(" T1.TOTAL4, ");
    	//合計順位(TOTAL_RANK4)
    	stb.append(" T1.TOTAL_RANK4, ");

        stb.append("  T6.REMARK1 AS REMARK, ");
        stb.append("  T7.FINSCHOOL_NAME ");
        stb.append(" FROM ");
        stb.append("  ENTEXAM_RECEPT_DAT T1 ");
        stb.append("  INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ");
        stb.append("    ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("   AND T2.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("   AND T2.EXAMNO = T1.EXAMNO ");
        stb.append("  LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT T3_009 ");
        stb.append("    ON T3_009.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("   AND T3_009.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("   AND T3_009.TESTDIV = T1.TESTDIV ");
        stb.append("   AND T3_009.EXAM_TYPE = T1.EXAM_TYPE ");
        stb.append("   AND T3_009.RECEPTNO = T1.RECEPTNO ");
        stb.append("   AND T3_009.SEQ = '009' ");
        stb.append("  LEFT JOIN ENTEXAM_TESTDIV_MST TM_TEKI ");
        stb.append("    ON TM_TEKI.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("   AND TM_TEKI.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("   AND TM_TEKI.TESTDIV = T1.TESTDIV ");
        stb.append("  LEFT JOIN PASTTEST_TBL T4 ");
        stb.append("    ON T4.ENTEXAMYEAR = T2.ENTEXAMYEAR ");
        stb.append("   AND T4.APPLICANTDIV = T2.APPLICANTDIV ");
        stb.append("   AND T4.RECOM_EXAMNO = T2.RECOM_EXAMNO ");
        int srchdatcnt = 1;
        for (Iterator ite = _param._testdivMap.keySet().iterator();ite.hasNext();) {
        	String getTestDiv = (String)ite.next();
            stb.append("  LEFT JOIN SRCHBASE_TBL T5_00"+srchdatcnt+" ");
            stb.append("    ON T5_00"+srchdatcnt+".ENTEXAMYEAR = T2.ENTEXAMYEAR ");
            stb.append("   AND T5_00"+srchdatcnt+".APPLICANTDIV = T2.APPLICANTDIV ");
            stb.append("   AND T5_00"+srchdatcnt+".TESTDIV = '"+getTestDiv+"' ");
            stb.append("   AND T5_00"+srchdatcnt+".RECOM_EXAMNO = T2.RECOM_EXAMNO ");
            srchdatcnt++;
        }
        stb.append("  LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT T6_004 ");
        stb.append("    ON T6_004.ENTEXAMYEAR = T2.ENTEXAMYEAR ");
        stb.append("   AND T6_004.APPLICANTDIV = T2.APPLICANTDIV ");
        stb.append("   AND T6_004.EXAMNO = T2.EXAMNO ");
        stb.append("   AND T6_004.SEQ = '004' ");
        stb.append("  LEFT JOIN FINSCHOOL_MST T7 ");
        stb.append("    ON T7.FINSCHOOLCD = T2.FS_CD ");
        stb.append("  LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT T6 ");
        stb.append("    ON T6.ENTEXAMYEAR = T2.ENTEXAMYEAR ");
        stb.append("   AND T6.APPLICANTDIV = T2.APPLICANTDIV ");
        stb.append("   AND T6.EXAMNO = T2.EXAMNO ");
        stb.append("   LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT T6_003 ");
        stb.append("    ON T6_003.ENTEXAMYEAR = T2.ENTEXAMYEAR ");
        stb.append("   AND T6_003.APPLICANTDIV = T2.APPLICANTDIV ");
        stb.append("   AND T6_003.EXAMNO = T2.EXAMNO ");
        stb.append("   AND T6_003.SEQ = '003' ");
        stb.append("  LEFT JOIN ENTEXAM_EXAMTYPE_MST T9 ");
        stb.append("    ON T9.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("   AND T9.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("   AND T9.EXAM_TYPE = T1.EXAM_TYPE ");
        stb.append("  LEFT JOIN NAME_MST T11_L013 ");
        stb.append("    ON T11_L013.NAMECD1 = 'L013' ");
        stb.append("   AND T11_L013.NAMECD2 = T1.JUDGEDIV ");
        stb.append("  LEFT JOIN NAME_MST T15_L013 ");
        stb.append("    ON T15_L013.NAMECD1 = 'L013' ");
        stb.append("   AND T15_L013.NAMECD2 = T1.JUDGEDIV ");

        for (Iterator ite = _param._testSubclassMap.keySet().iterator();ite.hasNext();) {
        	String getSubClsCd = (String)ite.next();
        	String subClsTblName = "T12_"+getSubClsCd;
            stb.append("  LEFT JOIN ENTEXAM_SCORE_DAT "+subClsTblName+" ");
        	stb.append("    ON "+subClsTblName+".ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        	stb.append("   AND "+subClsTblName+".APPLICANTDIV = T1.APPLICANTDIV ");
        	stb.append("   AND "+subClsTblName+".TESTDIV = T1.TESTDIV ");
        	stb.append("   AND "+subClsTblName+".EXAM_TYPE = T1.EXAM_TYPE ");
        	stb.append("   AND "+subClsTblName+".RECEPTNO = T1.RECEPTNO ");
        	stb.append("   AND "+subClsTblName+".TESTSUBCLASSCD = '"+getSubClsCd+"' ");
        }
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '"+_param._entexamyear+"' ");
        stb.append("     AND T1.APPLICANTDIV = '"+_param._applicantdiv+"' ");
        if (!"".equals(_param._testdiv)) {
            stb.append("     AND T1.TESTDIV = '"+_param._testdiv+"' ");
        }
        stb.append("     AND T15_L013.NAMESPARE1 = '1' ");
        stb.append(" ORDER BY ");
        stb.append("     RECEPTNO ");
        return stb.toString();
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final List receptNoList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = sql();
            log.debug(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
            	final String receptno = rs.getString("RECEPTNO");    //受験番号
            	final String passunpass = rs.getString("PASS_UNPASS"); //合否
            	final String interview = rs.getString("INTERVIEW");     //面接
            	final String attend = rs.getString("ATTEND");        //出欠
                final String examTypeName = rs.getString("EXAMTYPE"); //入試方式
            	final String chkA = rs.getString("CHK_A");          //A
            	final String chkF = rs.getString("CHK_F");          //F
            	final String chkT = rs.getString("CHK_T");          //T
            	final String chkB = rs.getString("CHK_B");          //B
            	final String chkJ = rs.getString("CHK_J");          //J
            	final String name = rs.getString("NAME");
            	final String kana = rs.getString("NAME_KANA");
            	final String remark = rs.getString("REMARK");        //備考
            	final String finSchoolName = rs.getString("FINSCHOOL_NAME"); //出身校名
            	receptInfo addwk = new receptInfo(receptno, interview, attend, examTypeName, chkA
            			                           , chkF, chkT, chkB, chkJ, name, kana, remark, finSchoolName, passunpass);

            	int subClsidx = 0;
                for (Iterator ite = _param._testSubclassMap.keySet().iterator();ite.hasNext();) {
                	String getSubClsCd = (String)ite.next();
                	addwk.setScore(subClsidx, rs.getString("SCORE"+getSubClsCd));
                	subClsidx++;
                }

                if ("1".equals(_param._applicantdiv)) {
            		addwk.setappdiv1data(rs.getString("ORDERNO"));
            		addwk.setTotal2(rs.getString("TOTAL2"), rs.getString("TOTAL_RANK2"));
            	} else {
            		addwk.setappdiv2data(rs.getString("NAITEI"), rs.getString("NAISINTEN"), rs.getString("KESSEKI"));
                }
        		addwk.setTotal4(rs.getString("TOTAL4"), rs.getString("TOTAL_RANK4"));
                int datcnt = 0;
                for (Iterator itr = _param._testdivMap.keySet().iterator();itr.hasNext();) {
                	String keystr = (String)itr.next();
                    if (keystr == null) continue;
                    addwk.setkekkadata(datcnt, rs.getString("KEKKA"+(datcnt+1)));
                    datcnt++;
                }
            	receptNoList.add(addwk);
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        if (receptNoList.isEmpty()) {
            return;
        }

        if ("1".equals(_param._applicantdiv)) {
            svf.VrSetForm("KNJL507J_1_1.frm", 1);
        } else {
            svf.VrSetForm("KNJL507J_1_2.frm", 1);
        }

        final int maxRow = MAXLINECNT;
        int row = 1;

        //ヘッダ、タイトル
        setTitle(db2, svf);
        for (final Iterator it = receptNoList.iterator(); it.hasNext();){
        	receptInfo putwk = (receptInfo)it.next();

            if (row > maxRow) {
                svf.VrEndPage();
                //ヘッダ、タイトル
                setTitle(db2, svf);
                row = 1;
            }

            svf.VrsOutn("EXAM_NO", row, putwk._receptNo); // 受験番号
            svf.VrsOutn("JUDGE", row, putwk._passunpass);  //合否
            svf.VrsOutn("INTERVIEW", row, putwk._interview); // 面接
            svf.VrsOutn("ATTEND", row, putwk._attend); // 出欠
            svf.VrsOutn("METHOD", row, putwk._examTypeName);  //入試方式
            int kdatcnt = 0;
            for (Iterator itr = _param._testdivMap.keySet().iterator();itr.hasNext();) {
                String keystr = (String)itr.next();
                if (keystr == null) continue;
                svf.VrsOutn("SCORE"+(kdatcnt+1), row, putwk.getkekkadata(kdatcnt));
                kdatcnt++;
            }
            svf.VrsOutn("DIV1", row, putwk._chkA); // A
            svf.VrsOutn("DIV2", row, putwk._chkF); // F
            svf.VrsOutn("DIV3", row, putwk._chkT); // T
            svf.VrsOutn("DIV4", row, putwk._chkB); // B
            svf.VrsOutn("DIV5", row, putwk._chkJ); // J
            int nlen = KNJ_EditEdit.getMS932ByteLength(putwk._name);
            String nfield = nlen > 30 ? "3" : nlen > 20 ? "2" : "1";
            svf.VrsOutn("NAME"+nfield, row, putwk._name); // 氏名
            int klen = KNJ_EditEdit.getMS932ByteLength(putwk._kana);
            String kfield = klen > 60 ? "4" : klen > 40 ? "3" : klen > 30 ? "2" : "1";
            svf.VrsOutn("KANA"+kfield, row, putwk._kana); // 氏名カナ
            svf.VrsOutn("FINSCHOOL_NAME", row, putwk._finSchoolName); // 出身校名
            svf.VrsOutn("REMARK", row, putwk._remark); // 備考

            //各教科の出力
            int datcnt = 0;
            for (Iterator itr = _param._testSubclassMap.keySet().iterator();itr.hasNext();) {
            	String keystr = (String)itr.next();
                if (keystr == null) continue;
            	svf.VrsOutn("CLASS_SCORE"+(datcnt+1), row, putwk.getScore(datcnt));
                datcnt++;
            }
            if ("1".equals(_param._applicantdiv)) {
            	//中学のみ
                svf.VrsOutn("REF_NO", row, putwk._orderNo); // 整理番号
                //2教科合計/RANK
                svf.VrsOutn("SUBTOTAL", row, putwk._total2);    // 小計
                svf.VrsOutn("CLASS_RANK1", row, putwk._rank2);  // 小計順位
            } else {
            	//高校(共通処理として)
                svf.VrsOutn("OFFER", row, putwk._naitei);       // 内定
                svf.VrsOutn("CONF_REPORT", row, putwk._naisin); //内申点
                svf.VrsOutn("NOTICE", row, putwk._absence);     //欠席
            }
            //4教科合計/RANK
            svf.VrsOutn("TOTAL", row, putwk._total4);           // 合計
            svf.VrsOutn("CLASS_RANK2", row, putwk._rank4);      // 合計順位
            row++;
        }
        svf.VrEndPage();
        _hasData = true;
    }

    private void setTitle(final DB2UDB db2, final Vrw32alp svf) {
        //ヘッダ、タイトル
        String setYear = KNJ_EditDate.h_format_Seireki_N(_param._entexamyear + "-04-01");
        String setTestDivName = "".equals(_param._testdivName) ? "" : " " + _param._testdivName;
        svf.VrsOut("TITLE", setYear + "度 " + _param._applicantdivName + setTestDivName + " 合格者一覧表");
        //DATE
        Calendar cl = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm");
        svf.VrsOut("DATE", sdf.format(cl.getTime()));

        int srchdatcnt = 1;
        for (Iterator ite = _param._testdivMap.keySet().iterator();ite.hasNext();) {
        	String idxstr = (String)ite.next();
            svf.VrsOut("EXAM_NUM"+srchdatcnt, (String)_param._testdivMap.get(idxstr));
            srchdatcnt++;
        }
        int clsnamecnt = 1;
        for (Iterator its = _param._testSubclassMap.keySet().iterator(); its.hasNext();) {
        	String idxstr = (String)its.next();
            svf.VrsOut("CLASS_NAME"+clsnamecnt, (String)_param._testSubclassMap.get(idxstr));
            clsnamecnt++;
        }
    }

    private class receptInfo{
    	final String _receptNo;      //受験番号
    	final String _interview;     //面接
    	final String _attend;        //出欠
        final String _examTypeName;  //入試方式略称
    	final String _chkA;          //A
    	final String _chkF;          //F
    	final String _chkT;          //T
    	final String _chkB;          //B
    	final String _chkJ;          //J
    	final String _name;          //氏名
    	final String _kana;          //氏名カナ
    	final String _remark;        //備考
    	final String _finSchoolName; //出身校名

    	String[] _kekka;        //入試種別1

    	String[] _score;        //科目Xの点数

    	String _total2;        //2科目合計
    	String _rank2;         //2科目ランク
    	String _total4;        //4科目合計
    	String _rank4;         //4科目ランク

    	String _orderNo;      //整理番号(applicantdiv==1のみ)

    	String _naitei;  //内定
    	String _naisin;  //内申
    	String _absence; //欠席

    	String _passunpass; //合否

    	receptInfo(
    	    	final String receptNo,
    	    	final String interview,     //面接
    	    	final String attend,        //出欠
                final String examTypeName,  //入試方式略称
    	    	final String chkA,          //A
    	    	final String chkF,          //F
    	    	final String chkT,          //T
    	    	final String chkB,          //B
    	    	final String chkJ,          //J
    	    	final String name,          //氏名
    	    	final String kana,          //氏名カナ
    	    	final String remark,        //備考
    	    	final String finSchoolName, //出身校名
    	    	final String passunpass     //合否
    			) {
    		_receptNo = receptNo;
	    	_interview = interview;
	    	_attend = attend;
            _examTypeName = examTypeName;
	    	_chkA = chkA;
	    	_chkF = chkF;
	    	_chkT = chkT;
	    	_chkB = chkB;
	    	_chkJ = chkJ;
        	_name = name;
        	_kana = kana;
	    	_remark = remark;
	    	_finSchoolName = finSchoolName;
	    	_passunpass = passunpass;
	    	_kekka = new String[KEKKAMAXCNT];
	    	_score = new String[SCOREMAXCNT];
    	}

    	private void setappdiv1data(final String orderNo) {
    		_orderNo = orderNo;

    		_naitei = "";
    		_naisin = "";
    		_absence = "";
    	}
    	private void setappdiv2data(final String naitei, final String naisin, final String absence) {
    		_naitei = naitei;
    		_naisin = naisin;
    		_absence = absence;

    		_orderNo = "";
    	}
    	private String getkekkadata(final int idx) {
    		if (idx >= 0 && idx < KEKKAMAXCNT) {
    		    return _kekka[idx];
    		}
    		return "";
    	}
    	private void setkekkadata(final int idx, final String kekka) {
    		if (idx >= 0 && idx < KEKKAMAXCNT) {
    		    _kekka[idx] = kekka;
    		}
    	}
    	private String getScore(final int idx) {
    		if (idx >= 0 && idx < SCOREMAXCNT) {
                return _score[idx];
    		}
    		return "";
    	}
    	private void setScore(final int idx, final String score) {
    		if (idx >= 0 && idx < SCOREMAXCNT) {
                _score[idx] = score;
    		}
    	}
    	private void setTotal2(final String total, final String rank) {
    		_total2 = total;
    		_rank2 = rank;
    	}
    	private void setTotal4(final String total, final String rank) {
    		_total4 = total;
    		_rank4 = rank;
    	}
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 70978 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _applicantdivName;
        final String _testdiv;
        final String _testdivName;
        final String _loginDate;
        final Map _testSubclassMap;
        final Map _testdivMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");

            _applicantdivName = getNameMst(db2, "L003", _applicantdiv);
            _testdivName = getTestdivNameMst(db2);
            _loginDate = request.getParameter("LOGIN_DATE");

            _testSubclassMap = getSortTestSubclassMap(db2);
            _testdivMap = getSortTestDiv(db2);
        }

        private Map getSortTestDiv(final DB2UDB db2) {
        	final Map retMap = new LinkedMap();
        	StringBuffer stb = new StringBuffer();
        	stb.append(" SELECT ");
        	stb.append("     TESTDIV, ");
        	stb.append("     TESTDIV_NAME, ");
        	stb.append("     TESTDIV_ABBV ");
        	stb.append(" FROM ");
        	stb.append("     ENTEXAM_TESTDIV_MST ");
        	stb.append(" WHERE ");
        	stb.append("     ENTEXAMYEAR = '"+_entexamyear+"' ");
        	stb.append("     AND APPLICANTDIV = '"+_applicantdiv+"' ");
        	stb.append(" ORDER BY ");
        	stb.append("     TEST_DATE, ");
        	stb.append("     TESTDIV ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = stb.toString();
                // log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                	retMap.put(rs.getString("TESTDIV"), rs.getString("TESTDIV_ABBV"));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            return retMap;
        }

        private Map getSortTestSubclassMap(final DB2UDB db2) {
        	final Map retMap = new LinkedMap();
        	StringBuffer stb = new StringBuffer();
            stb.append(" WITH GROUPING_EXAMTYPE AS ( ");
            stb.append("     SELECT ");
            stb.append("       EXAM_TYPE ");
            stb.append("     FROM ");
            stb.append("       ENTEXAM_RECEPT_DAT ");
            stb.append("     WHERE ");
            stb.append("       ENTEXAMYEAR = '"+_entexamyear+"' ");
            stb.append("       AND APPLICANTDIV = '"+_applicantdiv+"' ");
            if (!"".equals(_testdiv)) {
                stb.append("       AND TESTDIV = '"+_testdiv+"' ");
            }
            stb.append("     GROUP BY ");
            stb.append("       EXAM_TYPE ");
            stb.append(" ) ");
        	stb.append(" , GROUPING_MERGE_EXAMTYPE_SUBCLASS_MST AS ( ");
        	stb.append("     SELECT ");
        	stb.append("       SUBCLASSCD, ");
        	stb.append("       SUM(CASE WHEN JUDGE_SUMMARY = '1' THEN 1 ELSE 0 END) AS CNTFLG ");
        	stb.append("     FROM ");
        	stb.append("       ENTEXAM_EXAMTYPE_SUBCLASS_MST ");
        	stb.append("     WHERE ");
        	stb.append("       ENTEXAMYEAR = '"+_entexamyear+"' ");
        	stb.append("       AND APPLICANTDIV = '"+_applicantdiv+"' ");
            stb.append("       AND EXAM_TYPE IN (SELECT EXAM_TYPE FROM GROUPING_EXAMTYPE) ");
        	stb.append("     GROUP BY ");
        	stb.append("       SUBCLASSCD ");
        	stb.append(" ) ");
        	stb.append(" SELECT ");
        	stb.append("   T1.SUBCLASSCD, ");
        	stb.append("   T2_L009.NAME1, ");
        	stb.append("   CASE WHEN T1.CNTFLG > 0 THEN 1 ELSE 0 END AS GRPFLG ");
        	stb.append(" FROM ");
        	stb.append("   GROUPING_MERGE_EXAMTYPE_SUBCLASS_MST T1 ");
        	stb.append("   LEFT JOIN NAME_MST T2_L009 ");
        	stb.append("     ON T2_L009.NAMECD1 = 'L009' ");
        	stb.append("    AND T2_L009.NAMECD2 = T1.SUBCLASSCD ");
        	stb.append(" ORDER BY ");
//        	stb.append("   GRPFLG DESC, ");
        	stb.append("   T1.SUBCLASSCD ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = stb.toString();
                // log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                	retMap.put(rs.getString("SUBCLASSCD"), rs.getString("NAME1"));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            return retMap;
        }

        private String getNameMst(final DB2UDB db2, final String namecd1, final String namecd2) {
        	String retStr = "";
        	StringBuffer stb = new StringBuffer();

        	stb.append(" SELECT DISTINCT ");
        	stb.append("     NAME1 ");
        	stb.append(" FROM ");
        	stb.append("     V_NAME_MST ");
        	stb.append(" WHERE ");
        	stb.append("     YEAR    = '" + _entexamyear + "' AND ");
        	stb.append("     NAMECD1 = '" + namecd1 + "' ");
            if (!"".equals(namecd2)) {
            	stb.append(" AND NAMECD2 = '" + namecd2 + "' ");
            }

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = stb.toString();
                // log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                	retStr = rs.getString("NAME1");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            return retStr;
        }

        private String getTestdivNameMst(final DB2UDB db2) {
        	String retStr = "";
        	StringBuffer stb = new StringBuffer();

        	stb.append(" SELECT DISTINCT ");
        	stb.append("     TESTDIV_NAME ");
        	stb.append(" FROM ");
        	stb.append("     ENTEXAM_TESTDIV_MST ");
        	stb.append(" WHERE ");
        	stb.append("     ENTEXAMYEAR    = '" + _entexamyear + "' AND ");
        	stb.append("     APPLICANTDIV = '" + _applicantdiv + "' ");
            stb.append(" AND TESTDIV = '" + _testdiv + "' ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = stb.toString();
                // log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    retStr = StringUtils.defaultString(rs.getString("TESTDIV_NAME"));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            return retStr;
        }
    }
}

// eof

