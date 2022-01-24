// kanji=漢字
/*
 * $Id: de14b4547ca2d2d4362e7f38e27b4016756f422d $
 *
 * 作成日: 2009/02/13 14:42:08 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2014 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJM;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.KnjDbUtils;

/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*
 *
 *  学校教育システム 賢者 [通信制]
 *
 *                  ＜ＫＮＪＭ４００＞  スクーリングチェックリスト
 *
 *  2005/03/14 m-yama 新規作成
 *  2005/06/07 m-yama 回数欄追加に伴う修正 NO001
 *<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/

public class KNJM400 {

    private static final Log log = LogFactory.getLog(KNJM400.class);
    private boolean hasdata = false;                               //該当データなしフラグ

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws IOException
    {
    	log.fatal("$Revision: 74432 $");
    	KNJServletUtils.debugParam(request, log);

    	Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）

        //  print設定
        response.setContentType("application/pdf");

        //  svf設定
        svf.VrInit();                         //クラスの初期化
        svf.VrSetSpoolFileStream(response.getOutputStream());        //PDFファイル名の設定

        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        //  ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
            
            //  パラメータの取得
            final String[] kinname = request.getParameterValues("KINNAME");                     //スクーリング種別
            Param param = new Param(db2, request);
            log.info("kouza"+param._chrname);
            log.info("kouji"+param._schltime);

            svf.VrSetForm("KNJM400.frm", 1);
            //SVF出力
            for (final String kincd : kinname) {
                Set_Detail_1(db2, svf, param, kincd);
                log.info("syutu"+hasdata);
            }

        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
        	//  該当データ無し
            if (!hasdata) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "");
                svf.VrEndPage();
            }
            //  終了処理
            svf.VrQuit();

            if (null != db2) {
            	db2.commit();
            	db2.close();                //DBを閉じる
            }
        }

    }//doGetの括り

    /** SVF-FORM **/
    private void Set_Detail_1(DB2UDB db2,Vrw32alp svf,Param param,String kincd) {
    	
        final List<Map<String, String>> ps1List = KnjDbUtils.query(db2, Pre_Stat1(param), new Object[] {kincd});
        // 講座コード、職員コード、講座コード、SCHOOLING_SEQ毎に改ページ
        final List<List<Map<String, String>>> groupList = groupByChaircdStaffcdPeriodcdSchoolingSeq(ps1List);
        
        for (final List<Map<String, String>> group : groupList) {

        	final int maxLine = 50;
        	final int maxColumn = 2;
        	int num = 0;
        	
        	final List<List<Map<String, String>>> splitByPage = splitByCount(group, maxLine * maxColumn);
        	final Map<String, String> row0 = group.get(0);
        	log.info(" chaircd = " + KnjDbUtils.getString(row0, "CHAIRCD") + ", staffcd = " + KnjDbUtils.getString(row0, "STAFFCD") + ", periodcd = " + KnjDbUtils.getString(row0, "PERIODCD") + ", schoolingSeq = " + KnjDbUtils.getString(row0, "SCHOOLING_SEQ") + ", count = " + group.size() + ", page = " + splitByPage.size());
        	if (null == KnjDbUtils.getString(row0, "SCHOOLING_SEQ")) {
				svf.VrAttribute("KAI", "X=10000");
        	}

        	for (final List<Map<String, String>> pageRowList : splitByPage) {

				svf.VrsOut("TITLE", param._semestername);

        		final List<List<Map<String, String>>> colRowList = splitByCount(pageRowList, maxLine);
        		
        		for (int ci = 0; ci < colRowList.size(); ci++) {
        			final int column = ci + 1;
        			
        			for (int gi = 0; gi < colRowList.get(ci).size(); gi++) {
        				
        				final Map<String, String> row = colRowList.get(ci).get(gi);
        				
        				final int gyo = gi + 1;
        				num += 1;
        				
        				//  組略称・担任名出力
        				svf.VrsOut("DATE"         , param._date.replace('-','/') );
        				svf.VrsOut("KINNAME"      , KnjDbUtils.getString(row, "KINNAME") );
        				svf.VrsOut("CHRNAME"      , KnjDbUtils.getString(row, "CHAIRCD") +":"+ KnjDbUtils.getString(row, "CHAIRNAME") );
        				svf.VrsOut("MAKEDAY"      , param._ctrlDate );
        				if ("4".equals(kincd)){
        					svf.VrsOut("TECHNAME"     , "");
        					svf.VrsOut("SCHOOLTIME"   , "");
        				} else {
        					svf.VrsOut("TECHNAME"     , KnjDbUtils.getString(row, "STAFFNAME_SHOW") );
        					svf.VrsOut("SCHOOLTIME"   , KnjDbUtils.getString(row, "PERIODNAME") );
        					svf.VrsOut("KAI"          , KnjDbUtils.getString(row, "SCHOOLING_SEQ") );
        				}
        				
        				//  出席番号・かな出力
        				svf.VrsOutn("NUMBER" + String.valueOf(column)    ,gyo, String.valueOf(num) );
        				svf.VrsOutn("SCHNO" + String.valueOf(column)     ,gyo, KnjDbUtils.getString(row, "SCHREGNO") );
        				svf.VrsOutn("NAME" + String.valueOf(column)  ,gyo, KnjDbUtils.getString(row, "NAME_SHOW") );
        			}
        		}

                svf.VrEndPage();
                
                hasdata = true;
        	}
        }

    }//Set_Detail_1()の括り
    
    /**PrepareStatement作成**/
    private String Pre_Stat1(final Param param) {
    	final StringBuffer stb = new StringBuffer();
        stb.append("SELECT ");
        stb.append("    W1.SCHREGNO, ");
        stb.append("    W1.CHAIRCD, ");
        stb.append("    W1.PERIODCD, ");
        stb.append("    W1.SCHOOLING_SEQ, ");
        stb.append("    W1.STAFFCD, ");
        stb.append("    W1.SCHOOLINGKINDCD, ");
        stb.append("    W2.NAME_SHOW ");
        stb.append("  , W3.CHAIRNAME ");
        stb.append("  , W4.NAME1 AS PERIODNAME ");
        stb.append("  , W5.NAME1 AS KINNAME ");
        stb.append("  , W6.STAFFNAME_SHOW ");
        stb.append("FROM ");
        stb.append("    SCH_ATTEND_DAT W1 ");
        stb.append(" LEFT JOIN SCHREG_BASE_MST W2 ON W1.SCHREGNO = W2.SCHREGNO ");
        stb.append(" LEFT JOIN CHAIR_DAT W3 ON W1.CHAIRCD = W3.CHAIRCD AND W3.YEAR = W1.YEAR AND W3.SEMESTER = '"+param._semester+"' ");
        stb.append(" LEFT JOIN V_NAME_MST W4 ON W1.PERIODCD = W4.NAMECD2 AND W4.YEAR = W1.YEAR AND W4.NAMECD1 = 'B001' ");
        stb.append(" LEFT JOIN V_NAME_MST W5 ON W1.SCHOOLINGKINDCD = W5.NAMECD2 AND W5.YEAR = W1.YEAR AND W5.NAMECD1 = 'M001' ");
        stb.append(" LEFT JOIN STAFF_MST W6 ON W1.STAFFCD = W6.STAFFCD ");
        stb.append("WHERE ");
        stb.append("    W1.YEAR = '"+param._year+"' ");
        stb.append("    AND W1.EXECUTEDATE = '"+param._date+"' ");
        if (param._schltime != null){
            stb.append("    AND W1.PERIODCD = '"+param._schltime+"' ");
        }
        stb.append("    AND W1.SCHOOLINGKINDCD = ? ");
        if (!param._chrname.equals("0")){
            stb.append("    AND W1.CHAIRCD = '"+param._chrname+"' ");
        }
        stb.append("ORDER BY W1.CHAIRCD, W1.STAFFCD, W1.PERIODCD, W1.SCHOOLING_SEQ, W1.SCHREGNO ");
log.info(stb);
        return stb.toString();

    }//Pre_Stat1()の括り

    private static <T> List<List<T>> splitByCount(final List<T> list, final int count) {
    	final List<List<T>> rtn = new ArrayList<List<T>>();
    	List<T> current = null;
    	for (final T t : list) {
    		if (null == current || current.size() >= count) {
    			current = new ArrayList<T>();
    			rtn.add(current);
    		}
    		current.add(t);
    	}
    	return rtn;
    }

    private static List<List<Map<String, String>>> groupByChaircdStaffcdPeriodcdSchoolingSeq(final List<Map<String, String>> rowList) {
    	final List<List<Map<String, String>>> rtn = new ArrayList<List<Map<String, String>>>();
    	List<Map<String, String>> current = null;
    	String befchaircd = null;
    	String befstaffcd = null;
    	String befperiodcd = null;
    	String befschoolingSeq = null;
    	for (final Map<String, String> row : rowList) {
    		boolean isNew = false;
    		final String chaircd = KnjDbUtils.getString(row, "CHAIRCD");
    		final String staffcd = KnjDbUtils.getString(row, "STAFFCD");
    		final String periodcd = KnjDbUtils.getString(row, "PERIODCD");
    		final String schoolingSeq = KnjDbUtils.getString(row, "SCHOOLING_SEQ");
    		if (null == current) {
    			isNew = true;
    		} else {
    			if (!(null == befchaircd && null == chaircd || null != befchaircd && befchaircd.equals(chaircd))) {
//    				log.info(" diff chaircd : " + befchaircd + " / " + chaircd);
    				isNew = true;
    			} else {
        			if (!(null == befstaffcd && null == staffcd || null != befstaffcd && befstaffcd.equals(staffcd))) {
//        				log.info(" diff staffcd : " + befstaffcd + " / " + staffcd);
        				isNew = true;
        			} else {
            			if (!(null == befperiodcd && null == periodcd || null != befperiodcd && befperiodcd.equals(periodcd))) {
//            				log.info(" diff periodcd : " + befperiodcd + " / " + periodcd);
            				isNew = true;
            			} else {
                			if (!(null == befschoolingSeq && null == schoolingSeq || null != befschoolingSeq && befschoolingSeq.equals(schoolingSeq))) {
//                				log.info(" diff schoolingSeq : " + befschoolingSeq + " / " + schoolingSeq);
                				isNew = true;
                			}
            			}
        			}
    			}
    		}
			if (isNew) {
				current = new ArrayList<Map<String, String>>();
				rtn.add(current);
			}
			current.add(row);
    		befchaircd = chaircd;
    		befstaffcd = staffcd;
    		befperiodcd = periodcd;
    		befschoolingSeq = schoolingSeq;
    	}
    	return rtn;
    }

    private static class Param {
        final String _year;
        final String _semester;
        final String _chrname;
        final String _date;
        final String _schltime;
        final String _semestername;
        String _ctrlDate;
        private Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("YEAR");                                //年度
            if(request.getParameter("SELECT_SEMESTER") == null){
                _semester = request.getParameter("SEMESTER");                        //学期
            } else{
                _semester = request.getParameter("SELECT_SEMESTER");                 //学期
            }
            _chrname = request.getParameter("CHRNAME");                             //講座
            _date = request.getParameter("DATE");                                //日付
            _schltime = request.getParameter("SCHLTIME");                            //校時

        //  作成日(現在処理日)の取得
            KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = null;
            try {
                returnval = getinfo.Control(db2);
                _ctrlDate = KNJ_EditDate.h_format_thi(returnval.val3,0);
            } catch( Exception ex ){
                log.error("setHeader set error!", ex);
            }
            _semestername = getSemesterName(db2);
        }

        private String getSemesterName(final DB2UDB db2){
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SEMESTERNAME ");
            stb.append(" FROM ");
            stb.append("     SEMESTER_MST ");
            stb.append(" WHERE  ");
            stb.append("     YEAR='"+_year+"' ");
            stb.append("     AND SEMESTER='"+_semester+"' ");

            return KnjDbUtils.getOne(KnjDbUtils.query(db2, stb.toString()));
        }
    }
}//クラスの括り
