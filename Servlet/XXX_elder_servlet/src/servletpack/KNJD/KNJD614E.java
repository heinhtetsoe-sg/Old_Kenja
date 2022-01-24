/*
 * $Id: 0363e43535c0f3ab26452c0cef6df4e0a055eacd $
 *
 * 作成日: 2020/05/14
 * 作成者: yogi
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
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * 学校教育システム 賢者 [成績管理]  成績判定会議資料
 */

public class KNJD614E {

    private static final Log log = LogFactory.getLog(KNJD614E.class);

    private static final String SEMEALL = "9";
    private static final String SUBCLASSCD999999 = "999999";

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
        Vrw32alp svf = null;
        DB2UDB db2 = null;
        try {
            response.setContentType("application/pdf");

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _hasData = false;

            _param = createParam(db2, request);

            svf = new Vrw32alp();
            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            printMain(db2, svf);

        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
            svf.VrQuit();

            if (null != db2) {
                db2.commit();
                db2.close();
            }
        }
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        //各種データを取っていく。
        final List getAry1;
        final Map getAry2;
        final Map getAry2_1 = new LinkedMap();  //コース/クラスコードのキーテーブル
        final Map getAry2G;  //学年計
        final List getAry3;
        Map getAry4 = null;
        List getAry5 = null;
        final List getAry6;
        final List getAry7;

        //コース名称マスタを保持する
        final Map ccmAry = getCourseCodeMst(db2);

        //クラス名称マスタを保持する。
        final Map hrClsMstAry = getHrClsDat(db2);

        final List printKaikinTypeAry = getKaikinType(db2);
        ////授業日数
        getAry1 = getJyugyouNissuu(db2);

        ////在籍者数/休学者数/復学者数/留学者数を取得
        getAry2 = getSchCntInfo(db2, getAry2_1, hrClsMstAry);

        ////学年計
        getAry2G = getSchCntInfo(db2);

        ////各種出席情報(出席日数不足者判定も含む)を取得
        int[] fusokuFulCnt = {0};
        getAry3 = getNotEnoughAttend(db2, fusokuFulCnt);

        ////成績不良者を取得
        int[] scoreFuryouFulCnt = {0,0};
        if (!"J".equals(_param._schoolKind)) {        
	        getAry4 = notYetStudent(db2, scoreFuryouFulCnt);
	
	        getAry5 = notYetStudent(db2);
        }

        ////成績優秀者を取得(在籍者数の5%)
        getAry6 = getExStudent(db2);

        ////皆勤・精勤者を取得
        int[] kFullCnt = {0,0,0,0};
        getAry7 = getKaikinSeikin(db2, kFullCnt);
        int k6FullCnt = kFullCnt[0];
        int k3FullCnt = kFullCnt[1];
        int k1FullCnt = kFullCnt[2];
        int s1FullCnt = kFullCnt[3];

        svf.VrSetForm("KNJD614E_1.frm", 4);
        //タイトル
        final String nendo = KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_param._year));
        svf.VrsOut("TITLE", nendo + "度進級・卒業判定資料(" + _param._gradeName + ")");
        svf.VrsOut("DATE", StringUtils.replaceChars(_param._ctrlDate, "-", "/"));

        //1.授業日数
        svf.VrsOut("LESSON", (getAry1.size() > 0 ? (String)getAry1.get(0) : "0"));  ////フォームの設定上、空だとタイトルが出力されないので、取得できなかったときは"0日"とする。

        //2.各種資料該当者一覧表
    	final int nColMax = 10;
        int reptCnt = 0;
        for(Iterator ite = getAry2_1.keySet().iterator();ite.hasNext();) {
        	_hasData = true;
        	final String kStr = (String)ite.next();
        	final Map subMap = (Map)getAry2_1.get(kStr);
        	String prtCourseName = "";
        	if (ccmAry.containsKey(kStr.substring(4))) {
        		courseInfo prtwk = (courseInfo)ccmAry.get(kStr.substring(4));
        		prtCourseName = prtwk._coursecodename;
        	}
            svf.VrsOut("COURSE_NAME", prtCourseName);
            int sum2_1 = 0;
            int sum2_2 = 0;
            int sum2_3 = 0;
            int sum2_4 = 0;
            int sum2_5 = 0;
            int sum2_6_1 = 0;
            int sum2_6_2 = 0;
            int sum2_6_3 = 0;
            int sum2_8 = 0;
            int sum2_9_1 = 0;
            int sum2_9_2 = 0;
            int sum2_9_3 = 0;
            int sum2_9_4 = 0;
    		int nCol = 0;
        	for (Iterator itr = subMap.keySet().iterator();itr.hasNext();) {
        		final String sStr = (String)itr.next();
        		hrInfo hrWk = (hrInfo)subMap.get(sStr);
        		schCntInfo prtWk = (schCntInfo)getAry2.get(kStr + "-" + sStr);
        		if (prtWk != null) {
        			nCol++;
        			if (nCol <= 10) {
                        svf.VrsOutn("HR_NAME", nCol, hrWk._hr_Nameabbv); //クラス
                        svf.VrsOutn("NUM1", nCol, prtWk._zaiseki_Cnt);  //在籍者数
                        svf.VrsOutn("NUM2", nCol, prtWk._kyuugaku_Cnt);  //休学者数
                        svf.VrsOutn("NUM3", nCol, prtWk._fukugaku_Cnt);  //復学者数
                        svf.VrsOutn("NUM4", nCol, prtWk._ryuugaku_Cnt);  //留学者数
        			}
        			sum2_1 += Integer.parseInt(prtWk._zaiseki_Cnt);
        			sum2_2 += Integer.parseInt(prtWk._kyuugaku_Cnt);
        			sum2_3 += Integer.parseInt(prtWk._fukugaku_Cnt);
        			sum2_4 += Integer.parseInt(prtWk._ryuugaku_Cnt);

                    int hCnt = 0;
                    for (Iterator itt = getAry3.iterator();itt.hasNext();) {
                    	final neAttInfo attwk = (neAttInfo)itt.next();
                        if (kStr.equals(attwk.getMargeCourseCds()) && sStr.equals(attwk._hr_Class)) {
                            if (!"0".equals(attwk._fusoku_Flg)) {
                                hCnt++;
                            }
                        }
                    }
                    sum2_5 += hCnt;
        			if (nCol <= nColMax) {
                        svf.VrsOutn("NUM5", nCol, String.valueOf(hCnt));  //出席日数不足者数
        			}

        			if (!"J".equals(_param._schoolKind)) {
	                    nySchInfo nyWk = (nySchInfo)getAry4.get(kStr + "-" + sStr);
	                	if (nyWk != null) {
	            			if (nCol <= nColMax) {
	                            svf.VrsOutn("NUM6", nCol, nyWk._pat1);  //成績不良者(基準以内)
	                            svf.VrsOutn("NUM7", nCol, nyWk._pat2);  //成績不良者(基準以上)
	                            svf.VrsOutn("NUM8", nCol, String.valueOf(Integer.parseInt(nyWk._pat1) + Integer.parseInt(nyWk._pat2)));  //成績不良者(計)
	            			}
	                        sum2_6_1 += Integer.parseInt(nyWk._pat1);
	                        sum2_6_2 += Integer.parseInt(nyWk._pat2);
	                        sum2_6_3 += sum2_6_1 + sum2_6_2;
	                    }
        			}
        			if (nCol <= nColMax) {
                        svf.VrsOutn("NUM9", nCol, "");  //素行不良者は空でOK
        			}

                    int exCnt = 0;
                    for (Iterator itu = getAry6.iterator();itu.hasNext();) {
                        exSchInfo exWk = (exSchInfo)itu.next();
                        if (kStr.equals(exWk.getMargeCourseCds()) && sStr.equals(exWk._hr_Class)) {
                        	exCnt++;
                        }
                    }
                    sum2_8 += exCnt;
        			if (nCol <= nColMax) {
                        svf.VrsOutn("NUM10", nCol, String.valueOf(exCnt)); //成績優良者
        			}

                    int kCnt_1 = 0;
                    int kCnt_2 = 0;
                    int kCnt_3 = 0;
                    int kCnt_4 = 0;
                    for (Iterator ito = getAry7.iterator();ito.hasNext();) {
                    	kaikinInfo kWk = (kaikinInfo)ito.next();
                        if (kStr.equals(kWk.getMargeCourseCds()) && sStr.equals(kWk._hr_Class)) {
                            if (_param._print63KaikinFlg) {
                                if ("1".equals(kWk._kaikin_Div) && "6".equals(kWk._ref_Year)) {
                                    kCnt_1++;
                                }
                                if ("1".equals(kWk._kaikin_Div) && "3".equals(kWk._ref_Year)) {
                                    kCnt_2++;
                                }
                            }
                            if ("1".equals(kWk._kaikin_Div) && "1".equals(kWk._ref_Year)) {
                                kCnt_3++;
                            }
                            if ("2".equals(kWk._kaikin_Div) && "1".equals(kWk._ref_Year)) {
                                kCnt_4++;
                            }
                        }
                    }
        			if (nCol <= nColMax) {
                        svf.VrsOutn("NUM11", nCol, String.valueOf(kCnt_1)); //皆勤者(6ヵ年)
                        svf.VrsOutn("NUM12", nCol, String.valueOf(kCnt_2)); //皆勤者(3ヵ年)
                        svf.VrsOutn("NUM13", nCol, String.valueOf(kCnt_3)); //皆勤者(1ヵ年)
                        svf.VrsOutn("NUM14", nCol, String.valueOf(kCnt_4)); //精勤者
        			}
        			sum2_9_1 += kCnt_1;
        			sum2_9_2 += kCnt_2;
        			sum2_9_3 += kCnt_3;
        			sum2_9_4 += kCnt_4;
        		}
        	}
            //コース合計
        	final int ttlCol = 11;
            svf.VrsOutn("HR_NAME", ttlCol, "計"); //クラス
            svf.VrsOutn("NUM1", ttlCol, String.valueOf(sum2_1));  //在籍者数
            svf.VrsOutn("NUM2", ttlCol, String.valueOf(sum2_2));  //休学者数
            svf.VrsOutn("NUM3", ttlCol, String.valueOf(sum2_3));  //復学者数
            svf.VrsOutn("NUM4", ttlCol, String.valueOf(sum2_4));  //留学者数
            svf.VrsOutn("NUM5", ttlCol, String.valueOf(sum2_5));  //出席日数不足者数
            svf.VrsOutn("NUM6", ttlCol, String.valueOf(sum2_6_1));  //成績不良者(基準以内)
            svf.VrsOutn("NUM7", ttlCol, String.valueOf(sum2_6_2));  //成績不良者(基準以上)
            svf.VrsOutn("NUM8", ttlCol, String.valueOf(sum2_6_3));  //成績不良者(計)
            svf.VrsOutn("NUM9", ttlCol, "");  //素行不良者は空でOK
            svf.VrsOutn("NUM10", ttlCol, String.valueOf(sum2_8)); //成績優良者
            svf.VrsOutn("NUM11", ttlCol, String.valueOf(sum2_9_1)); //皆勤者(6ヵ年)
            svf.VrsOutn("NUM12", ttlCol, String.valueOf(sum2_9_2)); //皆勤者(3ヵ年)
            svf.VrsOutn("NUM13", ttlCol, String.valueOf(sum2_9_3)); //皆勤者(1ヵ年)
            svf.VrsOutn("NUM14", ttlCol, String.valueOf(sum2_9_4)); //精勤者
            //学年計
            if (getAry2G.size() > 0) {
            	schCntInfo ttlwk = (schCntInfo)getAry2G.get(_param._grade);
                svf.VrsOut("TOTAL_NUM1", ttlwk._zaiseki_Cnt);  //在籍者数
                svf.VrsOut("TOTAL_NUM2", ttlwk._kyuugaku_Cnt);  //休学者数
                svf.VrsOut("TOTAL_NUM3", ttlwk._fukugaku_Cnt);  //復学者数
                svf.VrsOut("TOTAL_NUM4", ttlwk._ryuugaku_Cnt);  //留学者数
            } else {
                svf.VrsOut("TOTAL_NUM1", "0");  //在籍者数
                svf.VrsOut("TOTAL_NUM2", "0");  //休学者数
                svf.VrsOut("TOTAL_NUM3", "0");  //復学者数
                svf.VrsOut("TOTAL_NUM4", "0");  //留学者数
            }
            svf.VrsOut("TOTAL_NUM5", String.valueOf(fusokuFulCnt[0]));  //出席日数不足者数
            if (!"J".equals(_param._schoolKind)) {
	            svf.VrsOut("TOTAL_NUM6", String.valueOf(scoreFuryouFulCnt[0]));  //成績不良者(基準以内)
	            svf.VrsOut("TOTAL_NUM7", String.valueOf(scoreFuryouFulCnt[1]));  //成績不良者(基準以上)
	            svf.VrsOut("TOTAL_NUM8", String.valueOf(scoreFuryouFulCnt[0]+scoreFuryouFulCnt[1]));  //成績不良者(計)
            }
            svf.VrsOut("TOTAL_NUM9", "");  //素行不良者は空でOK
            svf.VrsOut("TOTAL_NUM10", String.valueOf(getAry6.size())); //成績優良者
            svf.VrsOut("TOTAL_NUM11", String.valueOf(k6FullCnt)); //皆勤者(6ヵ年)
            svf.VrsOut("TOTAL_NUM12", String.valueOf(k3FullCnt)); //皆勤者(3ヵ年)
            svf.VrsOut("TOTAL_NUM13", String.valueOf(k1FullCnt)); //皆勤者(1ヵ年)
            svf.VrsOut("TOTAL_NUM14", String.valueOf(s1FullCnt)); //精勤者
            svf.VrEndRecord();
            reptCnt++;
            if (reptCnt % 3 == 0) {
            	if (reptCnt <= 3) {
            		//3.のタイトルが残って改ページするので、余白を付ける。
                    svf.VrsOut("BLANK", "A");
                    svf.VrEndRecord();
            	} else {
            		//上の条件に加えて先頭3行分が出なくなるので、つまり計4行分余白を付ける。
                    svf.VrsOut("BLANK", "A");
                    svf.VrEndRecord();
                    svf.VrsOut("BLANK", "A");
                    svf.VrEndRecord();
                    svf.VrsOut("BLANK", "A");
                    svf.VrEndRecord();
                    svf.VrsOut("BLANK", "A");
                    svf.VrEndRecord();
            	}
            }
        }
        if (reptCnt % 3 != 0) {
            svf.VrsOut("BLANK", "A");
            svf.VrEndRecord();
        }

        final int pageLineMax = 57;
        int filledLine = (3 + reptCnt * 16 + (reptCnt >= 3 ? 1 : 0) + ((int)(reptCnt / 3) > 1 ? ((int)(reptCnt / 3) - 1) * 4 : 0)) % pageLineMax;

        //3.出席日数不足者
        int prtLine = printTbl3(svf, false, getAry3);
        //1.ページに2表以上(1表ならタイトル3行+表16行+余白1行)印刷した状態で、2.埋まった状態に埋めると次ページになるなら、余白を入れて改ページ
        if (filledLine > 20 && filledLine + prtLine > pageLineMax) {
        	final int filedLMax = pageLineMax - filledLine;
            for (int fCnt = 0;fCnt < filedLMax;fCnt++) {
                svf.VrsOut("BLANK", "A");
                svf.VrEndRecord();
            }
            filledLine = 0;
        }
        printTbl3(svf, true, getAry3);
        filledLine += prtLine;
        filledLine = filledLine % pageLineMax;

        //4.成績優良者
        prtLine = printTbl4(svf, false, getAry2_1, ccmAry, hrClsMstAry, getAry6, getAry3);
        if (filledLine != 0 && filledLine + prtLine > pageLineMax) {
        	final int filedLMax = pageLineMax - filledLine;
            for (int fCnt = 0;fCnt < filedLMax;fCnt++) {
                svf.VrsOut("BLANK", "A");
                svf.VrEndRecord();
            }
            filledLine = 0;
        }
        printTbl4(svf, true, getAry2_1, ccmAry, hrClsMstAry, getAry6, getAry3);
        filledLine += prtLine;
        filledLine = filledLine % pageLineMax;

        //5.単位未修得者
        if (!"J".equals(_param._schoolKind)) {
	        prtLine = printTbl5(svf, false, getAry2_1, ccmAry, hrClsMstAry, getAry3, getAry5);
	        if (filledLine != 0 && filledLine + prtLine > pageLineMax) {
	        	final int filedLMax = pageLineMax - filledLine;
	            for (int fCnt = 0;fCnt < filedLMax;fCnt++) {
	                svf.VrsOut("BLANK", "A");
	                svf.VrEndRecord();
	            }
	            filledLine = 0;
	        }
	        printTbl5(svf, true, getAry2_1, ccmAry, hrClsMstAry, getAry3, getAry5);
        }

        svf.VrEndPage();

        //皆勤・精勤
        svf.VrSetForm("KNJD614E_2.frm", 1);

        for (Iterator itc = printKaikinTypeAry.iterator();itc.hasNext();) {
        	kaikinMst kmWk = (kaikinMst)itc.next();
            if (!_param._print63KaikinFlg && !"1".equals(kmWk._ref_Year)) {
                continue;
            }

            setTitle2frm(svf, nendo, kmWk, getAry7);

            int schFulCnt = 0;
            int schCnt = 0;
            final int colMax = 4;
            final int rowMax = 50;
    		for (Iterator ite = getAry7.iterator();ite.hasNext();) {
    			kaikinInfo kWk = (kaikinInfo)ite.next();
    			if (kmWk._kaikin_Cd.equals(kWk._kaikin_Cd)) {
    				int calcChkWk = (int)(schCnt / colMax);
    				if (calcChkWk >= rowMax) {
    					//改ページ
    					svf.VrEndPage();
    					schCnt = 0;
    		            setTitle2frm(svf, nendo, kmWk, getAry7);
    		            calcChkWk = (int)(schCnt / colMax);
    				}
    				final int putCol = (schCnt % colMax) + 1;
    				final int putRow = calcChkWk + 1;
    				if (putRow <= rowMax) {
    					svf.VrsOutn("HR_NAME" + putCol, putRow, kWk._hr_Nameabbv);
    					svf.VrsOutn("NAME" + putCol, putRow, kWk._name);
        				schFulCnt++;
    				}
    				schCnt++;
    			}
    		}
    		if (schFulCnt > 0) {
    			svf.VrEndPage();
    		}
        }
    }
    private void setTitle2frm(final Vrw32alp svf, final String nendo, final kaikinMst kmWk, final List getAry7) {
        svf.VrsOut("TITLE", nendo + "度 " + ("2".equals(kmWk._kaikin_Div) ? "精勤" : "皆勤") +"受賞候補者");
        svf.VrsOut("GRADE", _param._gradeName);
        svf.VrsOut("SUBTITLE", kmWk._kaikin_Name);
        int numCnt = countKaikinType(getAry7, kmWk._kaikin_Cd);
        svf.VrsOut("TOTAL_NUM", "以下"+numCnt+"名");
    }
    private int printTbl3(final Vrw32alp svf, boolean printFlg, final List getAry3) {
    	int lineCnt = 0;

        //3.出席日数不足者
    	if (printFlg) {
            svf.VrsOut("ITEM_NAME", "3.出席日数不足者");
            svf.VrEndRecord();
    	}
    	lineCnt++;
    	if (printFlg) {
            printTblHead(svf, "HTYPE1");
            svf.VrEndRecord();
        }
    	lineCnt++;

        for (Iterator ite = getAry3.iterator();ite.hasNext();) {
        	neAttInfo neWk = (neAttInfo)ite.next();
        	if (!"0".equals(neWk._fusoku_Flg)) {
            	if (printFlg) {
                    svf.VrsOut("HR_ABBV1", StringUtils.defaultString(neWk._hr_Nameabbv));
                    svf.VrsOut("NO1", StringUtils.defaultString(neWk._attendno));
                    svf.VrsOut("NAME1", StringUtils.defaultString(neWk._name));
                    svf.VrsOut("SUM1_1", StringUtils.defaultString(neWk._absent));
                    svf.VrsOut("SUM1_2", StringUtils.defaultString(neWk._late));
                    svf.VrsOut("SUM1_3", StringUtils.defaultString(neWk._early));
                    if (neWk._absent != null || neWk._kansan_Val != null) {
                        svf.VrsOut("SUM1_4", String.valueOf(Integer.parseInt(StringUtils.defaultString(neWk._absent, "0")) + Integer.parseInt(StringUtils.defaultString(neWk._kansan_Val, "0"))));
                    }
                    svf.VrEndRecord();
            	}
            	lineCnt++;
        	}
        }

    	if (printFlg) {
            svf.VrsOut("BLANK", "A");
            svf.VrEndRecord();
    	}
    	lineCnt++;

        return lineCnt;
    }

    private int printTbl4(final Vrw32alp svf, boolean printFlg, final Map getAry2_1, final Map ccmAry, final Map hrClsMstAry, final List getAry6, final List getAry3) {
    	int lineCnt = 0;
        //4.成績優良者
    	if (printFlg) {
            svf.VrsOut("ITEM_NAME", "4.成績優良者");
            svf.VrEndRecord();
    	}
    	lineCnt++;
        for(Iterator ite = getAry2_1.keySet().iterator();ite.hasNext();) {
        	final String kStr = (String)ite.next();
        	final Map subMap = (Map)getAry2_1.get(kStr);
        	String prtCourseName = "";
        	if (ccmAry.containsKey(kStr.substring(4))) {
        		courseInfo prtwk = (courseInfo)ccmAry.get(kStr.substring(4));
        		prtCourseName = prtwk._coursecodename;
        	}
        	if (printFlg) {
                svf.VrsOut("ITEM_NAME", "  コース:" + prtCourseName);
                svf.VrEndRecord();
        	}
        	lineCnt++;
        	if (printFlg) {
                printTblHead(svf, "HTYPE2");
                svf.VrEndRecord();
        	}
        	lineCnt++;
        	for (Iterator itr = getAry6.iterator();itr.hasNext();) {
        		final exSchInfo prtwk = (exSchInfo)itr.next();
        		final neAttInfo attwk = findAttInfo(getAry3, prtwk._schregno, kStr, prtwk._hr_Class);
        		if (kStr.equals(prtwk.getMargeCourseCds())) {
        			hrInfo putHr = (hrInfo)hrClsMstAry.get(prtwk._hr_Class);
                	if (printFlg) {
            			svf.VrsOut("HR_ABBV2", putHr._hr_Nameabbv);
            	        svf.VrsOut("NO2", String.valueOf(Integer.parseInt(prtwk._attendno)));
            	        svf.VrsOut("NAME2", prtwk._name);
            	        svf.VrsOut("SUM2_1", prtwk._score);
            	        svf.VrsOut("SUM2_2", prtwk._avg);
            	        if (attwk != null) {
            	            svf.VrsOut("SUM2_3", (attwk._absent != null ? attwk._absent : ""));
            	            svf.VrsOut("SUM2_4", (attwk._late != null ? attwk._late : ""));
            	            svf.VrsOut("SUM2_5", (attwk._early != null ? attwk._early : ""));
            	        }
            	        svf.VrEndRecord();
                	}
                	lineCnt++;
        		}
        	}

        	if (printFlg) {
                svf.VrsOut("BLANK", "A");
                svf.VrEndRecord();
        	}
        	lineCnt++;
        }
        return lineCnt;
    }
    private int printTbl5(final Vrw32alp svf, boolean printFlg, final Map getAry2_1, final Map ccmAry, final Map hrClsMstAry, final List getAry3, final List getAry5) {
    	int lineCnt = 0;
        //5.単位未修得者
    	if (printFlg) {
            svf.VrsOut("ITEM_NAME", "5.単位未修得者");
            svf.VrEndRecord();
    	}
    	lineCnt++;

        for(Iterator ite = getAry2_1.keySet().iterator();ite.hasNext();) {
        	final String kStr = (String)ite.next();
        	final Map subMap = (Map)getAry2_1.get(kStr);
        	String prtCourseName = "";
        	if (ccmAry.containsKey(kStr.substring(4))) {
        		courseInfo prtwk = (courseInfo)ccmAry.get(kStr.substring(4));
        		prtCourseName = prtwk._coursecodename;
        	}
        	if (printFlg) {
                svf.VrsOut("ITEM_NAME", "  修得単位数" + _param._creditLine + "以上 コース:" + prtCourseName);
                svf.VrEndRecord();
        	}
        	lineCnt++;
        	if (printFlg) {
                printTblHead(svf, "HTYPE2");
                svf.VrEndRecord();
        	}
        	lineCnt++;

        	lineCnt += nyTblPrintDetail(svf, printFlg, hrClsMstAry, kStr, getAry3, getAry5, "CHK1");

        	if (printFlg) {
                svf.VrsOut("BLANK", "A");
                svf.VrEndRecord();
        	}
        	lineCnt++;

        	if (printFlg) {
                svf.VrsOut("ITEM_NAME", "  修得単位数" + _param._creditLine + "未満 コース:" + prtCourseName);
                svf.VrEndRecord();
        	}
        	lineCnt++;

        	if (printFlg) {
                printTblHead(svf, "HTYPE2");
                svf.VrEndRecord();
        	}
        	lineCnt++;

        	lineCnt += nyTblPrintDetail(svf, printFlg, hrClsMstAry, kStr, getAry3, getAry5, "CHK2");

        	if (printFlg) {
                svf.VrsOut("BLANK", "A");
                svf.VrEndRecord();
        	}
        	lineCnt++;
        }
        return lineCnt;
    }

	private neAttInfo findAttInfo(final List getAry3, final String schregno, final String margeCourseCode, final String hrClass) {
		neAttInfo retwk = null;
		for (Iterator ite = getAry3.iterator();ite.hasNext();) {
		    final neAttInfo chkWk = (neAttInfo)ite.next();
		    if (schregno.equals(chkWk._schregno) && margeCourseCode.equals(chkWk.getMargeCourseCds()) && hrClass.equals(chkWk._hr_Class)) {
		    	retwk = chkWk;
		    	break;
		    }
		}
		return retwk;
	}
	private void printTblHead(final Vrw32alp svf, final String headType) {
		if ("HTYPE1".equals(headType)) {
	        svf.VrsOut("ITEM1_1", "欠席");
	        svf.VrsOut("ITEM1_2", "遅刻");
	        svf.VrsOut("ITEM1_3", "早退");
	        svf.VrsOut("ITEM1_4", "換算合計");
		}
		if ("HTYPE2".equals(headType)) {
            svf.VrsOut("ITEM2_1", "合計");
            svf.VrsOut("ITEM2_2", "平均");
            svf.VrsOut("ITEM2_3", "欠席");
            svf.VrsOut("ITEM2_4", "遅刻");
            svf.VrsOut("ITEM2_5", "早退");
            svf.VrEndRecord();
		}
	}
	private int nyTblPrintDetail(final Vrw32alp svf, final boolean printFlg, final Map hrClsMstAry, final String kStr, final List getAry3, final List getAry5, final String chkPattern) {
		int lineCnt = 0;
        for (Iterator its = getAry5.iterator();its.hasNext();) {
            final nySchInfo prtwk = (nySchInfo)its.next();
    		final neAttInfo attwk = findAttInfo(getAry3, prtwk._schregno, kStr, prtwk._hr_Class);
    		final String chkPat = "CHK2".equals(chkPattern) ? prtwk._pat2 : prtwk._pat1;
            if (kStr.equals(prtwk.getMargeCourseCds()) && "1".equals(chkPat)) {
    			hrInfo putHr = (hrInfo)hrClsMstAry.get(prtwk._hr_Class);
    			if (printFlg) {
                    svf.VrsOut("HR_ABBV2", putHr._hr_Nameabbv);
                    svf.VrsOut("NO2", prtwk._attendno);
                    svf.VrsOut("NAME2", prtwk._name);
                    svf.VrsOut("SUM2_1", prtwk._score);
                    svf.VrsOut("SUM2_2", prtwk._avg);
                    if (attwk != null) {
                        svf.VrsOut("SUM2_3", (attwk._absent != null ? attwk._absent : ""));
                        svf.VrsOut("SUM2_4", (attwk._late != null ? attwk._late : ""));
                        svf.VrsOut("SUM2_5", (attwk._early != null ? attwk._early : ""));
                    }
                    svf.VrEndRecord();
    			}
    			lineCnt++;
            }
        }
        return lineCnt;
	}
	private int countKaikinType(final List getAry7, final String kaikin_Cd) {
		int retCnt = 0;
		for (Iterator ite = getAry7.iterator();ite.hasNext();) {
			kaikinInfo kWk = (kaikinInfo)ite.next();
			if (kaikin_Cd.equals(kWk._kaikin_Cd)) {
				retCnt++;
			}
		}
		return retCnt;
	}

    final Map getHrClsDat(final DB2UDB db2) {
        final String query = getHrClsDatSql();
    	log.debug("getHrClsDat : "+ query);
        final Map hrClsMstAry = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(query);
            rs = ps.executeQuery();
            while (rs.next()) {
            	final String year = rs.getString("YEAR");
            	final String grade = rs.getString("GRADE");
            	final String hr_Class = rs.getString("HR_CLASS");
            	final String hr_Name = rs.getString("HR_NAME");
            	final String hr_Nameabbv = rs.getString("HR_NAMEABBV");
            	final String grade_Name = rs.getString("GRADE_NAME");
            	final String hr_Class_Name1 = rs.getString("HR_CLASS_NAME1");
            	final String hr_Class_Name2 = rs.getString("HR_CLASS_NAME2");
            	final String hr_Faccd = rs.getString("HR_FACCD");
            	final String tr_Cd1 = rs.getString("TR_CD1");
            	final String tr_Cd2 = rs.getString("TR_CD2");
            	final String tr_Cd3 = rs.getString("TR_CD3");
            	final String subtr_Cd1 = rs.getString("SUBTR_CD1");
            	final String subtr_Cd2 = rs.getString("SUBTR_CD2");
            	final String subtr_Cd3 = rs.getString("SUBTR_CD3");
            	final String classweeks = rs.getString("CLASSWEEKS");
            	final String classdays = rs.getString("CLASSDAYS");
            	hrInfo addwk = new hrInfo(year, grade, hr_Class, hr_Name, hr_Nameabbv, grade_Name, hr_Class_Name1, hr_Class_Name2, hr_Faccd, tr_Cd1, tr_Cd2, tr_Cd3, subtr_Cd1, subtr_Cd2, subtr_Cd3, classweeks, classdays);
            	hrClsMstAry.put(hr_Class, addwk);
            }
        } catch (Exception e) {
            log.error("sql exception!", e);
        } finally {
            DbUtils.closeQuietly(ps);
        }
        return hrClsMstAry;
    }
    private String  getHrClsDatSql() {
    	final StringBuffer stb = new StringBuffer();
    	stb.append(" SELECT DISTINCT ");
    	stb.append("   T1.YEAR, ");
    	stb.append("   T1.GRADE, ");
    	stb.append("   T1.HR_CLASS, ");
    	stb.append("   T1.HR_NAME, ");
    	stb.append("   T1.HR_NAMEABBV, ");
    	stb.append("   T1.GRADE_NAME, ");
    	stb.append("   T1.HR_CLASS_NAME1, ");
    	stb.append("   T1.HR_CLASS_NAME2, ");
    	stb.append("   T1.HR_FACCD, ");
    	stb.append("   T1.TR_CD1, ");
    	stb.append("   T1.TR_CD2, ");
    	stb.append("   T1.TR_CD3, ");
    	stb.append("   T1.SUBTR_CD1, ");
    	stb.append("   T1.SUBTR_CD2, ");
    	stb.append("   T1.SUBTR_CD3, ");
    	stb.append("   T1.CLASSWEEKS, ");
    	stb.append("   T1.CLASSDAYS ");
    	stb.append(" FROM ");
    	stb.append("   SCHREG_REGD_HDAT T1 ");
    	stb.append(" WHERE ");
    	stb.append("   T1.YEAR = '" + _param._year + "' ");
    	stb.append("   AND T1.GRADE = '" + _param._grade + "' ");
    	stb.append("   AND SEMESTER = (SELECT MAX(TH.SEMESTER) FROM SCHREG_REGD_HDAT TH WHERE TH.YEAR = T1.YEAR AND TH.GRADE = T1.GRADE AND TH.HR_CLASS = T1.HR_CLASS) ");  //通年が想定されるので、全クラスの最終学期で登録されている名称を引っ張る
        return stb.toString();
    }

    private List getKaikinType(final DB2UDB db2) {
        final List printKaikinTypeAry = new ArrayList();
        final String query = getKaikinTypeSql();
    	log.debug("getKaikinType : "+ query);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(query);
            rs = ps.executeQuery();
            while (rs.next()) {
            	final String kaikin_Cd = rs.getString("KAIKIN_CD");
            	final String kaikin_Name = rs.getString("KAIKIN_NAME");
            	final String kaikin_Div = rs.getString("KAIKIN_DIV");
            	final String ref_Year = rs.getString("REF_YEAR");
            	final String kesseki_Condition = rs.getString("KESSEKI_CONDITION");
            	final String tikoku_Condition = rs.getString("TIKOKU_CONDITION");
            	final String soutai_Condition = rs.getString("SOUTAI_CONDITION");
            	final String kesseki_Kansan = rs.getString("KESSEKI_KANSAN");
            	final String kekka_Jisu_Condition = rs.getString("KEKKA_JISU_CONDITION");
            	final String priority = rs.getString("PRIORITY");
            	final String kaikin_Flg = rs.getString("KAIKIN_FLG");
            	kaikinMst addwk = new kaikinMst(kaikin_Cd, kaikin_Name, kaikin_Div, ref_Year, kesseki_Condition, tikoku_Condition, soutai_Condition, kesseki_Kansan, kekka_Jisu_Condition, priority, kaikin_Flg);
            	printKaikinTypeAry.add(addwk);
            }
        } catch (Exception e) {
            log.error("sql exception!", e);
        } finally {
            DbUtils.closeQuietly(ps);
        }
        return printKaikinTypeAry;
    }
    private String getKaikinTypeSql() {
    	final StringBuffer stb = new StringBuffer();
    	stb.append(" SELECT ");
    	stb.append("   * ");
    	stb.append(" FROM ");
    	stb.append("   KAIKIN_MST ");
    	stb.append(" ORDER BY ");
    	stb.append("   REF_YEAR DESC, ");
    	stb.append("   KAIKIN_DIV ");
        return stb.toString();
    }

    //授業日数を取得
    final List getJyugyouNissuu(final DB2UDB db2) {
    	final String query = getJyugyouNissuuSql();
    	log.debug("getJyugyouNissuu : "+ query);
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List getAry1 = new ArrayList();
        try {
            ps = db2.prepareStatement(query);
            rs = ps.executeQuery();
            while (rs.next()) {
            	getAry1.add(rs.getString("LESSON"));
            }
        } catch (Exception e) {
            log.error("sql exception!", e);
        } finally {
            DbUtils.closeQuietly(ps);
        }
        return getAry1;
    }
    private String  getJyugyouNissuuSql() {
    	final StringBuffer stb = new StringBuffer();
        final String sDateYM = StringUtils.substring(StringUtils.replaceChars(_param._sdate, "-", ""), 0, 6);
        final String eDateYM = StringUtils.substring(StringUtils.replaceChars(_param._edate, "-", ""), 0, 6);
        stb.append(" SELECT ");
        stb.append("     GRADE, ");
        stb.append("     SUM(LESSON) AS LESSON ");
        stb.append(" FROM ");
        stb.append("   ATTEND_LESSON_MST ");
        stb.append(" WHERE ");
        stb.append("   '" + sDateYM + "' <= YEAR || MONTH AND YEAR || MONTH <= '" + eDateYM + "' ");
        stb.append("   AND COURSECD = '0' ");
        stb.append("   AND MAJORCD = '000' ");
        stb.append("   AND GRADE = '" + _param._grade + "' ");
        stb.append(" GROUP BY ");
        stb.append("   GRADE ");
        stb.append(" ORDER BY ");
        stb.append("   GRADE ");
        return stb.toString();
    }

    private Map getCourseCodeMst(final DB2UDB db2) {
        //コース名称マスタを保持する。
        final Map ccmAry = new LinkedMap();
        final String query = getCourseCodeMstSql();
    	log.debug("getCourseCodeMst : "+ query);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(query);
            rs = ps.executeQuery();
            while (rs.next()) {
            	final String coursecode = rs.getString("COURSECODE");
            	final String coursecodename = rs.getString("COURSECODENAME");
            	final String coursecodeabbv1 = rs.getString("COURSECODEABBV1");
            	final String coursecodeabbv2 = rs.getString("COURSECODEABBV2");
            	final String coursecodeabbv3 = rs.getString("COURSECODEABBV3");
            	courseInfo addwk = new courseInfo(coursecode, coursecodename, coursecodeabbv1, coursecodeabbv2, coursecodeabbv3);
            	ccmAry.put(coursecode, addwk);
            }
        } catch (Exception e) {
            log.error("sql exception!", e);
        } finally {
            DbUtils.closeQuietly(ps);
        }
        return ccmAry;
    }
    private String  getCourseCodeMstSql() {
        return " SELECT * FROM COURSECODE_MST ";
    }

    //在籍者数/休学者数/復学者数/留学者数を取得
    final Map getSchCntInfo(final DB2UDB db2, final Map getAry2_1, final Map hrClsMstAry) {
    	final String query = getSchCntInfoSql("");
    	log.debug("getSchCntInfo : "+ query);
        PreparedStatement ps = null;
        ResultSet rs = null;
        final Map getAry2 = new LinkedMap();
        Map subMap = null;
        try {
            ps = db2.prepareStatement(query);
            rs = ps.executeQuery();
            while (rs.next()) {
            	final String coursecd = rs.getString("COURSECD");
            	final String majorcd = rs.getString("MAJORCD");
            	final String coursecode = rs.getString("COURSECODE");
            	final String hr_Class = rs.getString("HR_CLASS");
            	final String grade = rs.getString("GRADE");
            	final String zaiseki_Cnt = rs.getString("ZAISEKI_CNT");
            	final String kyuugaku_Cnt = rs.getString("KYUUGAKU_CNT");
            	final String fukugaku_Cnt = rs.getString("FUKUGAKU_CNT");
            	final String ryuugaku_Cnt = rs.getString("RYUUGAKU_CNT");
            	schCntInfo addwk = new schCntInfo(coursecd, majorcd, coursecode, hr_Class, grade, zaiseki_Cnt, kyuugaku_Cnt, fukugaku_Cnt, ryuugaku_Cnt);
                final String std_key1 = StringUtils.defaultString(coursecd) + StringUtils.defaultString(majorcd) + StringUtils.defaultString(coursecode);
                final String std_key2 = StringUtils.defaultString(hr_Class);
            	getAry2.put(std_key1 + "-" + std_key2, addwk);
                if (!getAry2_1.containsKey(std_key1)) {
                	subMap = new LinkedMap();
                	getAry2_1.put(std_key1, subMap);
                }
                subMap = (Map)getAry2_1.get(std_key1);
                if (!subMap.containsKey(std_key2)) {
                	hrInfo objWk = (hrInfo)hrClsMstAry.get(std_key2);
                	subMap.put(std_key2, objWk);
                }
            }
        } catch (Exception e) {
            log.error("sql exception!", e);
        } finally {
            DbUtils.closeQuietly(ps);
        }
        return getAry2;
    }
    final Map getSchCntInfo(final DB2UDB db2) {
    	final String query = getSchCntInfoSql("GRADE_SUM");
    	log.debug("getSchCntInfo_ : "+ query);
    	final Map getAry2G = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(query);
            rs = ps.executeQuery();
            while (rs.next()) {
            	final String grade = rs.getString("GRADE");
            	final String zaiseki_Cnt = rs.getString("ZAISEKI_CNT");
            	final String kyuugaku_Cnt = rs.getString("KYUUGAKU_CNT");
            	final String fukugaku_Cnt = rs.getString("FUKUGAKU_CNT");
            	final String ryuugaku_Cnt = rs.getString("RYUUGAKU_CNT");
            	schCntInfo addwk = new schCntInfo("", "", "", "", grade, zaiseki_Cnt, kyuugaku_Cnt, fukugaku_Cnt, ryuugaku_Cnt);
            	getAry2G.put(grade, addwk);
            }
        } catch (Exception e) {
            log.error("sql exception!", e);
        } finally {
            DbUtils.closeQuietly(ps);
        }
        return getAry2G;  //学年集計結果なので、1レコードしか取れないはず。
    }

    private String  getSchCntInfoSql(final String sum_param) {
    	final StringBuffer stb = new StringBuffer();
        final String sDate = StringUtils.replaceChars(_param._sdate, "/", "-");
        final String eDate = StringUtils.replaceChars(_param._edate, "/", "-");

        stb.append(" WITH SCHNO_A AS( ");  // 在籍者数
        stb.append(" SELECT DISTINCT ");
        stb.append("   T1.SCHREGNO, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T1.COURSECD, T1.MAJORCD, T1.COURSECODE  ");
        stb.append(" FROM ");
        stb.append("   SCHREG_REGD_DAT T1 ");
        stb.append("   INNER JOIN SCHREG_REGD_HDAT TH ");
        stb.append("     ON TH.YEAR = T1.YEAR ");
        stb.append("    AND TH.SEMESTER = T1.SEMESTER ");
        stb.append("    AND TH.GRADE = T1.GRADE ");
        stb.append("    AND TH.HR_CLASS = T1.HR_CLASS ");
        stb.append("   LEFT JOIN SEMESTER_MST T2  ");
        stb.append("     ON T1.YEAR = T2.YEAR  ");
        stb.append("    AND T1.SEMESTER = T2.SEMESTER  ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR = '" + _param._year + "' ");
        stb.append("   AND T1.GRADE = '" + _param._grade + "' ");
        stb.append("   AND '" + eDate + "' BETWEEN T2.SDATE AND T2.EDATE ");
        stb.append("   AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1  ");
        stb.append("                  WHERE S1.SCHREGNO = T1.SCHREGNO  ");
        stb.append("                    AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '" + eDate + "' THEN T2.EDATE ELSE '" + sDate + "' END)  ");
        stb.append("                    OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '" + sDate + "' THEN T2.EDATE ELSE '" + sDate + "' END)) )  ");
        stb.append("   AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1  ");
        stb.append("                  WHERE S1.SCHREGNO = T1.SCHREGNO  ");
        stb.append("                    AND S1.TRANSFERCD IN ('1','2') ");
        stb.append("                    AND CASE WHEN T2.EDATE < '" + sDate + "' THEN T2.EDATE ELSE '" + sDate + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE)  ");
        stb.append(" ), SCHNO_B AS( ");  // 休学者数
        stb.append(" SELECT DISTINCT ");
        stb.append("   T1.SCHREGNO, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T1.COURSECD, T1.MAJORCD, T1.COURSECODE  ");
        stb.append(" FROM ");
        stb.append("   SCHREG_REGD_DAT T1 ");
        stb.append("   INNER JOIN SCHREG_REGD_HDAT TH ");
        stb.append("     ON TH.YEAR = T1.YEAR ");
        stb.append("    AND TH.SEMESTER = T1.SEMESTER ");
        stb.append("    AND TH.GRADE = T1.GRADE ");
        stb.append("    AND TH.HR_CLASS = T1.HR_CLASS ");
        stb.append("   LEFT JOIN SEMESTER_MST T2  ");
        stb.append("     ON T1.YEAR = T2.YEAR  ");
        stb.append("    AND T1.SEMESTER = T2.SEMESTER  ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR = '" + _param._year + "' ");
        stb.append("   AND T1.GRADE = '" + _param._grade + "' ");
        stb.append("   AND '" + eDate + "' BETWEEN T2.SDATE AND T2.EDATE ");
        stb.append("   AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1  ");
        stb.append("                  WHERE S1.SCHREGNO = T1.SCHREGNO  ");
        stb.append("                    AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '" + eDate + "' THEN T2.EDATE ELSE '" + sDate + "' END)  ");
        stb.append("                    OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '" + sDate + "' THEN T2.EDATE ELSE '" + sDate + "' END)) )  ");
        stb.append("   AND EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1  ");
        stb.append("                  WHERE S1.SCHREGNO = T1.SCHREGNO  ");
        stb.append("                    AND S1.TRANSFERCD IN ('1') ");
        stb.append("                    AND CASE WHEN T2.EDATE < '" + sDate + "' THEN T2.EDATE ELSE '" + sDate + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE)  ");
        stb.append(" ), SCHNO_C AS( ");  // 復学者数
        stb.append(" SELECT DISTINCT ");
        stb.append("   T1.SCHREGNO, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T1.COURSECD, T1.MAJORCD, T1.COURSECODE  ");
        stb.append(" FROM ");
        stb.append("   SCHREG_REGD_DAT T1 ");
        stb.append("   INNER JOIN SCHREG_REGD_HDAT TH ");
        stb.append("     ON TH.YEAR = T1.YEAR ");
        stb.append("    AND TH.SEMESTER = T1.SEMESTER ");
        stb.append("    AND TH.GRADE = T1.GRADE ");
        stb.append("    AND TH.HR_CLASS = T1.HR_CLASS ");
        stb.append("   LEFT JOIN SEMESTER_MST T2  ");
        stb.append("     ON T1.YEAR = T2.YEAR  ");
        stb.append("    AND T1.SEMESTER = T2.SEMESTER  ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR = '" + _param._year + "' ");
        stb.append("   AND T1.GRADE = '" + _param._grade + "' ");
        stb.append("   AND '" + eDate + "' BETWEEN T2.SDATE AND T2.EDATE ");
        stb.append("   AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1  ");
        stb.append("                  WHERE S1.SCHREGNO = T1.SCHREGNO  ");
        stb.append("                    AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '" + eDate + "' THEN T2.EDATE ELSE '" + sDate + "' END)  ");
        stb.append("                    OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '" + sDate + "' THEN T2.EDATE ELSE '" + sDate + "' END)) )  ");
        stb.append("   AND EXISTS(SELECT  'X' FROM ( ");
        stb.append("                                SELECT T1.SCHREGNO, MIN(T1.COMEBACK_DATE) AS COMEBACK_DATE ");
        stb.append("                                FROM SCHREG_ENT_GRD_HIST_COMEBACK_DAT T1 ");
        stb.append("                                     INNER JOIN SCHREG_REGD_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("                                      AND T2.YEAR = '" + _param._year + "' ");
        stb.append("                                      AND T2.SEMESTER = (SELECT TW.SEMESTER FROM SEMESTER_MST TW WHERE T1.COMEBACK_DATE BETWEEN TW.SDATE AND TW.EDATE) ");
        stb.append("                                      AND T2.GRADE = '" + _param._grade + "' ");
        stb.append("                                WHERE ");
        stb.append("                                  T1.COMEBACK_DATE < '" + eDate + "' ");
        stb.append("                                GROUP BY ");
        stb.append("                                  T1.SCHREGNO ");
        stb.append("                               ) S1 ");
        stb.append("              WHERE S1.SCHREGNO = T1.SCHREGNO ");
        stb.append("              ) ");
        stb.append(" ), SCHNO_D AS( ");  // 留学者数
        stb.append(" SELECT DISTINCT ");
        stb.append("   T1.SCHREGNO, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T1.COURSECD, T1.MAJORCD, T1.COURSECODE  ");
        stb.append(" FROM ");
        stb.append("   SCHREG_REGD_DAT T1 ");
        stb.append("   INNER JOIN SCHREG_REGD_HDAT TH ");
        stb.append("     ON TH.YEAR = T1.YEAR ");
        stb.append("    AND TH.SEMESTER = T1.SEMESTER ");
        stb.append("    AND TH.GRADE = T1.GRADE ");
        stb.append("    AND TH.HR_CLASS = T1.HR_CLASS ");
        stb.append("   LEFT JOIN SEMESTER_MST T2  ");
        stb.append("     ON T1.YEAR = T2.YEAR  ");
        stb.append("    AND T1.SEMESTER = T2.SEMESTER  ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR = '" + _param._year + "' ");
        stb.append("   AND T1.GRADE = '" + _param._grade + "' ");
        stb.append("   AND '" + eDate + "' BETWEEN T2.SDATE AND T2.EDATE ");
        stb.append("   AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1  ");
        stb.append("                  WHERE S1.SCHREGNO = T1.SCHREGNO  ");
        stb.append("                    AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '" + eDate + "' THEN T2.EDATE ELSE '" + sDate + "' END)  ");
        stb.append("                    OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '" + sDate + "' THEN T2.EDATE ELSE '" + sDate + "' END)) )  ");
        stb.append("   AND EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1  ");
        stb.append("                  WHERE S1.SCHREGNO = T1.SCHREGNO  ");
        stb.append("                    AND S1.TRANSFERCD IN ('2') ");
        stb.append("                    AND CASE WHEN T2.EDATE < '" + sDate + "' THEN T2.EDATE ELSE '" + sDate + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE)  ");
        stb.append(" ),KEY_MARGE_WK1 AS ( ");
        stb.append(" SELECT * FROM SCHNO_A ");
        stb.append(" UNION ");
        stb.append(" SELECT * FROM SCHNO_B ");
        stb.append(" UNION ");
        stb.append(" SELECT * FROM SCHNO_C ");
        stb.append(" UNION ");
        stb.append(" SELECT * FROM SCHNO_D ");
        stb.append(" ),KEY_MARGE_WK2 AS ( ");
        stb.append(" SELECT DISTINCT ");
        stb.append(" T0.GRADE, T0.HR_CLASS, T0.ATTENDNO, T0.COURSECD, T0.MAJORCD, T0.COURSECODE, ");
        stb.append(" (CASE WHEN VALUE(T1.ATTENDNO, '') = '' THEN 0 ELSE 1 END) AS T1ATTNO, ");
        stb.append(" (CASE WHEN VALUE(T2.ATTENDNO, '') = '' THEN 0 ELSE 1 END) AS T2ATTNO, ");
        stb.append(" (CASE WHEN VALUE(T3.ATTENDNO, '') = '' THEN 0 ELSE 1 END) AS T3ATTNO, ");
        stb.append(" (CASE WHEN VALUE(T4.ATTENDNO, '') = '' THEN 0 ELSE 1 END) AS T4ATTNO ");
        stb.append(" FROM KEY_MARGE_WK1 T0 ");
        stb.append("   LEFT JOIN SCHNO_A T1 ");
        stb.append("     ON T1.GRADE = T0.GRADE ");
        stb.append("    AND T1.HR_CLASS = T0.HR_CLASS ");
        stb.append("    AND T1.ATTENDNO = T0.ATTENDNO ");
        stb.append("    AND T1.COURSECD = T0.COURSECD ");
        stb.append("    AND T1.MAJORCD = T0.MAJORCD ");
        stb.append("    AND T1.COURSECODE = T0.COURSECODE ");
        stb.append("   LEFT JOIN SCHNO_B T2 ");
        stb.append("     ON T2.GRADE = T0.GRADE ");
        stb.append("    AND T2.HR_CLASS = T0.HR_CLASS ");
        stb.append("    AND T2.ATTENDNO = T0.ATTENDNO ");
        stb.append("    AND T2.COURSECD = T0.COURSECD ");
        stb.append("    AND T2.MAJORCD = T0.MAJORCD ");
        stb.append("    AND T2.COURSECODE = T0.COURSECODE ");
        stb.append("   LEFT JOIN SCHNO_C T3 ");
        stb.append("     ON T3.GRADE = T0.GRADE ");
        stb.append("    AND T3.HR_CLASS = T0.HR_CLASS ");
        stb.append("    AND T3.ATTENDNO = T0.ATTENDNO ");
        stb.append("    AND T3.COURSECD = T0.COURSECD ");
        stb.append("    AND T3.MAJORCD = T0.MAJORCD ");
        stb.append("    AND T3.COURSECODE = T0.COURSECODE ");
        stb.append("   LEFT JOIN SCHNO_D T4 ");
        stb.append("     ON T4.GRADE = T0.GRADE ");
        stb.append("    AND T4.HR_CLASS = T0.HR_CLASS ");
        stb.append("    AND T4.ATTENDNO = T0.ATTENDNO ");
        stb.append("    AND T4.COURSECD = T0.COURSECD ");
        stb.append("    AND T4.MAJORCD = T0.MAJORCD ");
        stb.append("    AND T4.COURSECODE = T0.COURSECODE ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        if (!"GRADE_SUM".equals(sum_param)) {
            stb.append("   T0.COURSECD, ");
            stb.append("   T0.MAJORCD, ");
            stb.append("   T0.COURSECODE, ");
            stb.append("   T0.HR_CLASS, ");
        }
        stb.append("   T0.GRADE, ");
        stb.append("   SUM(T0.T1ATTNO) AS ZAISEKI_CNT, ");
        stb.append("   SUM(T0.T2ATTNO) AS KYUUGAKU_CNT, ");
        stb.append("   SUM(T0.T3ATTNO) AS FUKUGAKU_CNT, ");
        stb.append("   SUM(T0.T4ATTNO) AS RYUUGAKU_CNT ");
        stb.append(" FROM ");
        stb.append("   KEY_MARGE_WK2 T0 ");
        stb.append(" GROUP BY ");
        if (!"GRADE_SUM".equals(sum_param)) {
            stb.append("   T0.COURSECD, ");
            stb.append("   T0.MAJORCD, ");
            stb.append("   T0.COURSECODE, ");
            stb.append("   T0.HR_CLASS, ");
        }
        stb.append("   T0.GRADE ");
        stb.append(" ORDER BY ");
        if (!"GRADE_SUM".equals(sum_param)) {
            stb.append("   T0.COURSECD, ");
            stb.append("   T0.MAJORCD, ");
            stb.append("   T0.COURSECODE, ");
        }
        stb.append("   T0.GRADE ");
        if (!"GRADE_SUM".equals(sum_param)) {
            stb.append("   ,T0.HR_CLASS ");
        }
        return stb.toString();
    }

    //出席日数不足者を取得
    final List getNotEnoughAttend(final DB2UDB db2, int[] fusokuFulCnt) {
    	final String query = getNotEnoughAttendSql();
    	log.debug("getNotEnoughAttend : " + query);
        final List getAry3 = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        fusokuFulCnt[0] = 0;
        try {
            ps = db2.prepareStatement(query);
            rs = ps.executeQuery();
            while (rs.next()) {
            	final String coursecd = rs.getString("COURSECD");
            	final String majorcd = rs.getString("MAJORCD");
            	final String coursecode = rs.getString("COURSECODE");
            	final String grade = rs.getString("GRADE");
            	final String hr_Class = rs.getString("HR_CLASS");
            	final String hr_Nameabbv = rs.getString("HR_NAMEABBV");
            	final String attendno = rs.getString("ATTENDNO");
            	final String schregno = rs.getString("SCHREGNO");
            	final String name = rs.getString("NAME");
            	final String fusoku_Flg = rs.getString("FUSOKU_FLG");
            	final String classdays = rs.getString("CLASSDAYS");
            	final String suspend = rs.getString("SUSPEND");
            	final String mourning = rs.getString("MOURNING");
            	final String present1 = rs.getString("PRESENT1");
            	final String absent = rs.getString("ABSENT");
            	final String present2 = rs.getString("PRESENT2");
            	final String late = rs.getString("LATE");
            	final String early = rs.getString("EARLY");
            	final String kansan_Val = rs.getString("KANSAN_VAL");
            	neAttInfo addwk = new neAttInfo(coursecd, majorcd, coursecode, grade, hr_Class, hr_Nameabbv, attendno, schregno, name, fusoku_Flg, classdays, suspend, mourning, present1, absent, present2, late, early, kansan_Val);

            	getAry3.add(addwk);
                if (!"0".equals(StringUtils.defaultString(fusoku_Flg,"0"))) {
                	fusokuFulCnt[0]++;
                }
            }
        } catch (Exception e) {
            log.error("sql exception!", e);
        } finally {
            DbUtils.closeQuietly(ps);
        }
        return getAry3;
    }
    private String  getNotEnoughAttendSql() {
    	final StringBuffer stb = new StringBuffer();
        //SQLが長いので、何をやっているかを記載。
        //1.換算で利用するデータがKAIKIN_MSTの1ヵ年皆勤の条件で計算するため、それを取得。
        //2.KAIKIN_MSTに1ヵ年皆勤の複数データ登録可能なので、優先度の高いもので、コードの若い物の最初の1件を採用する。
        //3.生徒の出欠情報と、遅刻早退の換算値を算出
        //4.クラス毎に出席すべき日数を算出
        //5.出席日数不足か、判定(生徒毎に出す処理もあるので、出席日数不足の集計はPRG側)。
        stb.append(" WITH KANSAN_PATTERN AS ( ");
        stb.append(" SELECT ");
        stb.append("   ROW_NUMBER() OVER(ORDER BY PRIORITY,KAIKIN_CD) AS ROWNUMBER, ");
        stb.append("   CASE WHEN TIKOKU_CONDITION IS NOT NULL THEN 2 ELSE 1 END AS CALC_PATTERN, ");
        stb.append("   KESSEKI_KANSAN, ");
        stb.append("   TIKOKU_CONDITION, ");
        stb.append("   SOUTAI_CONDITION ");
        stb.append(" FROM ");
        stb.append("   KAIKIN_MST ");
        stb.append(" WHERE ");
        stb.append("   KAIKIN_DIV = '1' ");
        stb.append("   AND REF_YEAR = '1' ");
        stb.append(" ), KANSAN_1PATTERN AS ( ");  // 複数レコードある可能性を排除する(先頭だけ取得)。
        stb.append(" SELECT ");
        stb.append("   * ");
        stb.append(" FROM ");
        stb.append("   KANSAN_PATTERN  ");
        stb.append(" WHERE ");
        stb.append("   ROWNUMBER = 1 ");
        stb.append(" ), SCH_RESULT AS ( ");  // 生徒の出欠情報と、遅刻早退の換算値を算出
        stb.append(" SELECT ");
        stb.append("     T1.COURSECD, ");
        stb.append("     T1.MAJORCD, ");
        stb.append("     T1.COURSECODE, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.CLASSDAYS, ");
        stb.append("     T1.SUSPEND, ");
        stb.append("     T1.MOURNING, ");
        stb.append("     VALUE(T1.CLASSDAYS,0)-VALUE(T1.SUSPEND,0)-VALUE(T1.MOURNING,0) AS PRESENT1, ");
        stb.append("     VALUE(T1.SICK,0)+VALUE(T1.NOTICE,0)+VALUE(T1.NONOTICE,0) AS ABSENT, ");
        stb.append("     VALUE(T1.CLASSDAYS,0)-VALUE(T1.SUSPEND,0)-VALUE(T1.MOURNING,0) ");
        stb.append("           -VALUE(T1.SICK,0)-VALUE(T1.NOTICE,0)-VALUE(T1.NONOTICE,0) AS PRESENT2, ");
        stb.append("     T1.LATE, ");
        stb.append("     T1.EARLY, ");
        stb.append("     CASE WHEN T2.CALC_PATTERN IS NULL ");
        stb.append("               OR (T2.CALC_PATTERN <> 2 AND VALUE(T2.KESSEKI_KANSAN, 0) = 0) ");
        stb.append("               OR (T2.CALC_PATTERN = 2 AND (VALUE(T2.TIKOKU_CONDITION, 0) = 0 OR VALUE(T2.SOUTAI_CONDITION, 0) = 0)) ");
        stb.append("               THEN 0 ");  // 0割り対策
        stb.append("          WHEN T2.CALC_PATTERN = 2 THEN INT(T1.LATE / T2.TIKOKU_CONDITION) + INT(T1.EARLY / T2.SOUTAI_CONDITION) ");
        stb.append("          ELSE INT((T1.LATE + T1.EARLY) / T2.KESSEKI_KANSAN) ");
        stb.append("          END AS KANSAN_VAL ");
        stb.append(" FROM ");
        stb.append(" ( ");
        stb.append("  SELECT ");
        stb.append("      T3.COURSECD, ");
        stb.append("      T3.MAJORCD, ");
        stb.append("      T3.COURSECODE, ");
        stb.append("      T3.GRADE, ");
        stb.append("      T3.HR_CLASS, ");
        stb.append("      T3.ATTENDNO, ");
        stb.append("      T1.SCHREGNO, ");
        stb.append("      T4.NAME, ");
        stb.append("      SUM(VALUE(T1.LESSON, 0) - VALUE(OFFDAYS, 0) - VALUE(ABROAD, 0)) AS CLASSDAYS, ");
        stb.append("      SUM(T1.SUSPEND) AS SUSPEND, ");
        stb.append("      SUM(T1.MOURNING) AS MOURNING, ");
        stb.append("      SUM(T1.SICK) AS SICK, ");
        stb.append("      SUM(T1.NOTICE) AS NOTICE, ");
        stb.append("      SUM(T1.NONOTICE) AS NONOTICE, ");
        stb.append("      SUM(T1.LATE) AS LATE, ");
        stb.append("      SUM(T1.EARLY) AS EARLY ");
        stb.append("  FROM ");
        stb.append("      ATTEND_SEMES_DAT T1 ");
        stb.append("      INNER JOIN SCHREG_REGD_DAT T3 ");
        stb.append("        ON T3.YEAR = T1.YEAR ");
        stb.append("       AND T3.SEMESTER = T1.SEMESTER ");
        stb.append("       AND T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("      INNER JOIN SCHREG_BASE_MST T4 ");
        stb.append("        ON T4.SCHREGNO = T1.SCHREGNO ");
        stb.append("  WHERE ");
        stb.append("      T1.YEAR = '" + _param._year + "' ");
        stb.append("      AND T1.SEMESTER <= (SELECT MAX(SEMESTER) FROM SEMESTER_MST WHERE YEAR = '" + _param._year + "' AND SEMESTER <> '9') ");
        stb.append("      AND T3.GRADE = '" + _param._grade + "' ");
        stb.append("  GROUP BY ");
        stb.append("      T3.COURSECD, ");
        stb.append("      T3.MAJORCD, ");
        stb.append("      T3.COURSECODE, ");
        stb.append("      T3.GRADE, ");
        stb.append("      T3.HR_CLASS, ");
        stb.append("      T3.ATTENDNO, ");
        stb.append("      T4.NAME, ");
        stb.append("      T1.SCHREGNO ");
        stb.append(" )T1,KANSAN_1PATTERN T2 ");
        stb.append(" ORDER BY ");
        stb.append("   HR_CLASS ");
        stb.append("   ,T1.ATTENDNO ");
        stb.append(" ), CLS_MUSTDAY AS ( ");  // クラス内での出席すべき日数を算出
        stb.append(" SELECT ");
        stb.append("     T1.COURSECD, ");
        stb.append("     T1.MAJORCD, ");
        stb.append("     T1.COURSECODE, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     MAX(T1.PRESENT1) AS MX_PRESENT ");
        stb.append(" FROM ");
        stb.append("   SCH_RESULT T1 ");
        stb.append(" GROUP BY ");
        stb.append("     T1.COURSECD, ");
        stb.append("     T1.MAJORCD, ");
        stb.append("     T1.COURSECODE, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.COURSECD, ");
        stb.append("     T1.MAJORCD, ");
        stb.append("     T1.COURSECODE, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T3.HR_NAMEABBV, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     CASE WHEN T1.ABSENT + T1.KANSAN_VAL > INT(T2.MX_PRESENT / 4) THEN 1 ELSE 0 END AS FUSOKU_FLG, ");
        stb.append("     T1.CLASSDAYS, ");
        stb.append("     T1.SUSPEND, ");
        stb.append("     T1.MOURNING, ");
        stb.append("     T1.PRESENT1, ");
        stb.append("     T1.ABSENT, ");
        stb.append("     T1.PRESENT2, ");
        stb.append("     T1.LATE, ");
        stb.append("     T1.EARLY, ");
        stb.append("     T1.KANSAN_VAL ");
        stb.append(" FROM ");
        stb.append("   SCH_RESULT T1 ");
        stb.append("   LEFT JOIN CLS_MUSTDAY T2 ");
        stb.append("     ON T2.GRADE = T1.GRADE ");
        stb.append("    AND T2.HR_CLASS = T1.HR_CLASS ");
        stb.append("    AND T2.COURSECD = T1.COURSECD ");
        stb.append("    AND T2.MAJORCD = T1.MAJORCD ");
        stb.append("    AND T2.COURSECODE = T1.COURSECODE ");
        stb.append("   LEFT JOIN SCHREG_REGD_HDAT T3 ");
        stb.append("     ON T3.YEAR = '" + _param._year + "' ");
        stb.append("    AND T3.SEMESTER = (SELECT MAX(SEMESTER) FROM SEMESTER_MST WHERE YEAR = '" + _param._year + "' AND SEMESTER <> '9') ");
        stb.append("    AND T3.GRADE = T1.GRADE ");
        stb.append("    AND T3.HR_CLASS = T1.HR_CLASS ");
        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T3.HR_NAMEABBV, ");
        stb.append("     T1.ATTENDNO ");

        return stb.toString();
    }

    //成績不良者を取得
    final Map notYetStudent(final DB2UDB db2, int[] scoreFuryouFulCnt) {
    	final String query = notYetStudentSql(false);
    	log.debug("notYetStudent : "+ query);
    	final Map getAry4 = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(query);
            rs = ps.executeQuery();
            while (rs.next()) {
            	final String coursecd = rs.getString("COURSECD");
            	final String majorcd = rs.getString("MAJORCD");
            	final String coursecode = rs.getString("COURSECODE");
            	final String grade = rs.getString("GRADE");
            	final String hr_Class = rs.getString("HR_CLASS");
            	final String pat1 = rs.getString("PAT1");
            	final String pat2 = rs.getString("PAT2");
            	nySchInfo addwk = new nySchInfo(coursecd, majorcd, coursecode, grade, hr_Class, "", "", "", "", "", pat1, pat2);
                final String std_key1 = StringUtils.defaultString(coursecd) + StringUtils.defaultString(majorcd) + StringUtils.defaultString(coursecode);
                final String std_key2 = StringUtils.defaultString(hr_Class);
            	getAry4.put(std_key1 + "-" + std_key2, addwk);
                if (!"".equals(pat1)) {
                	scoreFuryouFulCnt[0] += Integer.parseInt(pat1);
                }
                if (!"".equals(pat2)) {
                	scoreFuryouFulCnt[1] += Integer.parseInt(pat2);
                }
            }
        } catch (Exception e) {
            log.error("sql exception!", e);
        } finally {
            DbUtils.closeQuietly(ps);
        }
        return getAry4;
    }
    final List notYetStudent(final DB2UDB db2) {
    	final String query = notYetStudentSql(true);
    	log.debug("notYetStudent_ : "+ query);
    	final List getAry5 = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(query);
            rs = ps.executeQuery();
            while (rs.next()) {
            	final String coursecd = rs.getString("COURSECD");
            	final String majorcd = rs.getString("MAJORCD");
            	final String coursecode = rs.getString("COURSECODE");
            	final String grade = rs.getString("GRADE");
            	final String hr_Class = rs.getString("HR_CLASS");
            	final String attendno = rs.getString("ATTENDNO");
            	final String schregno = rs.getString("SCHREGNO");
            	final String name = rs.getString("NAME");
            	final String score = rs.getString("SCORE");
            	final String avg = rs.getString("AVG");
            	final String pat1 = rs.getString("PAT1");
            	final String pat2 = rs.getString("PAT2");
            	nySchInfo addwk = new nySchInfo(coursecd, majorcd, coursecode, grade, hr_Class, attendno, schregno, name, score, avg, pat1, pat2);
            	getAry5.add(addwk);
            }
        } catch (Exception e) {
            log.error("sql exception!", e);
        } finally {
            DbUtils.closeQuietly(ps);
        }
        return getAry5;
    }
    private String  notYetStudentSql(final boolean detailFlg) {  //default=false
    	final StringBuffer stb = new StringBuffer();
        stb.append(" WITH CREDIT_BASE AS ( ");
        stb.append(" SELECT ");
        stb.append("   T1.YEAR, ");
        stb.append("   T1.SEMESTER, ");
        stb.append("   T1.CLASSCD, ");
        stb.append("   T1.SCHOOL_KIND, ");
        stb.append("   T1.CURRICULUM_CD, ");
        stb.append("   T1.SUBCLASSCD, ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T1.COMP_CREDIT, ");
        stb.append("   CASE WHEN T1.COMP_CREDIT IS NULL OR T1.COMP_CREDIT = 0 THEN 1 ELSE 0 END AS CHK_NL ");
        stb.append(" FROM ");
        stb.append("   RECORD_SCORE_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR = '" + _param._year + "' ");
        stb.append("   AND T1.SEMESTER = '9' ");
        stb.append("   AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '990008' ");
        stb.append(" ), CREDIT_SUMMARY AS ( ");
        stb.append(" SELECT ");
        stb.append("   T1.YEAR, ");
        stb.append("   T1.SEMESTER, ");
        stb.append("   T2.COURSECD, ");
        stb.append("   T2.MAJORCD, ");
        stb.append("   T2.COURSECODE, ");
        stb.append("   T2.GRADE, ");
        stb.append("   T2.HR_CLASS, ");
        stb.append("   T2.ATTENDNO, ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T3.NAME, ");
        stb.append("   SUM(T1.COMP_CREDIT) AS COMP_CREDITS, ");
        stb.append("   SUM(T1.CHK_NL) AS CHK_NLS ");
        stb.append(" FROM ");
        stb.append("   CREDIT_BASE T1 ");
        stb.append("   LEFT JOIN SCHREG_REGD_DAT T2 ");
        stb.append("     ON T2.YEAR = T1.YEAR ");
        stb.append("    AND T2.SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("   LEFT JOIN SCHREG_BASE_MST T3 ");
        stb.append("     ON T3.SCHREGNO = T1.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("    T2.GRADE = '" + _param._grade + "' ");
        stb.append(" GROUP BY ");
        stb.append("   T1.YEAR, ");
        stb.append("   T1.SEMESTER, ");
        stb.append("   T2.COURSECD, ");
        stb.append("   T2.MAJORCD, ");
        stb.append("   T2.COURSECODE, ");
        stb.append("   T2.GRADE, ");
        stb.append("   T2.HR_CLASS, ");
        stb.append("   T2.ATTENDNO, ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T3.NAME ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("   T1.COURSECD, ");
        stb.append("   T1.MAJORCD, ");
        stb.append("   T1.COURSECODE, ");
        stb.append("   T1.GRADE, ");
        stb.append("   T1.HR_CLASS, ");
        if (detailFlg) {
            stb.append("   T1.ATTENDNO, ");
            stb.append("   T1.SCHREGNO, ");
            stb.append("   T1.NAME, ");
            stb.append("   T2.SCORE, ");
            stb.append("   T2.AVG, ");
            stb.append("   CASE WHEN T1.COMP_CREDITS >= " + _param._creditLine + " AND T1.CHK_NLS > 0 THEN 1 ELSE 0 END AS PAT1, ");
            stb.append("   CASE WHEN T1.COMP_CREDITS < " + _param._creditLine + " THEN 1 ELSE 0 END AS PAT2 ");
        } else {
            stb.append("   SUM(CASE WHEN T1.COMP_CREDITS >= " + _param._creditLine + " AND T1.CHK_NLS > 0 THEN 1 ELSE 0 END) AS PAT1, ");
            stb.append("   SUM(CASE WHEN T1.COMP_CREDITS < " + _param._creditLine + " THEN 1 ELSE 0 END) AS PAT2 ");
        }
        stb.append(" FROM ");
        stb.append("   CREDIT_SUMMARY T1 ");
        if (detailFlg) {
            stb.append("   LEFT JOIN RECORD_RANK_SDIV_DAT T2 ");
            stb.append("     ON T2.YEAR = T1.YEAR ");
            stb.append("    AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("    AND T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = '990008' ");
            stb.append("    AND T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD = '99-" + _param._schoolKind + "-99-" + SUBCLASSCD999999 + "' ");
            stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
        } else {
            stb.append(" GROUP BY ");
            stb.append("   T1.COURSECD, ");
            stb.append("   T1.MAJORCD, ");
            stb.append("   T1.COURSECODE, ");
            stb.append("   T1.GRADE, ");
            stb.append("   T1.HR_CLASS ");
        }
        stb.append("   ORDER BY ");
        stb.append("   T1.COURSECD, ");
        stb.append("   T1.MAJORCD, ");
        stb.append("   T1.COURSECODE, ");
        stb.append("   T1.GRADE, ");
        stb.append("   T1.HR_CLASS ");
        if (detailFlg) {
            stb.append("   ,T1.ATTENDNO, ");
            stb.append("   T1.SCHREGNO, ");
            stb.append("   T1.NAME ");
        }

        return stb.toString();
    }

    //成績優秀者を取得(在籍者数の5%)
    final List getExStudent(final DB2UDB db2) {
    	final String query = getExStudentSql();
    	log.debug("getExStudent : "+ query);
    	final List getAry6 = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(query);
            rs = ps.executeQuery();
            while (rs.next()) {
            	final String year = rs.getString("YEAR");
            	final String semester = rs.getString("SEMESTER");
            	final String testkindcd = rs.getString("TESTKINDCD");
            	final String testitemcd = rs.getString("TESTITEMCD");
            	final String score_Div = rs.getString("SCORE_DIV");
            	final String classcd = rs.getString("CLASSCD");
            	final String school_Kind = rs.getString("SCHOOL_KIND");
            	final String curriculum_Cd = rs.getString("CURRICULUM_CD");
            	final String subclasscd = rs.getString("SUBCLASSCD");
            	final String grade = rs.getString("GRADE");
            	final String hr_Class = rs.getString("HR_CLASS");
            	final String attendno = rs.getString("ATTENDNO");
            	final String coursecd = rs.getString("COURSECD");
            	final String majorcd = rs.getString("MAJORCD");
            	final String coursecode = rs.getString("COURSECODE");
            	final String name = rs.getString("NAME");
            	final String schregno = rs.getString("SCHREGNO");
            	final String score = rs.getString("SCORE");
            	final String avg = rs.getString("AVG");
            	final String grade_Deviation_Rank = rs.getString("GRADE_DEVIATION_RANK");
            	exSchInfo addwk = new exSchInfo(year, semester, testkindcd, testitemcd, score_Div, classcd, school_Kind, curriculum_Cd, subclasscd, grade, hr_Class, attendno, coursecd, majorcd, coursecode, name, schregno, score, avg, grade_Deviation_Rank);
            	getAry6.add(addwk);
            }
        } catch (Exception e) {
            log.error("sql exception!", e);
        } finally {
            DbUtils.closeQuietly(ps);
        }
        return getAry6;
    }
    private String  getExStudentSql() {
    	final StringBuffer stb = new StringBuffer();
        final String sDate = StringUtils.replaceChars(_param._sdate, "/", "-");
        final String eDate = StringUtils.replaceChars(_param._edate, "/", "-");
        stb.append(" WITH SCHNO_A AS( ");  // 在籍者数
        stb.append(" SELECT DISTINCT ");
        stb.append("   T1.SCHREGNO, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T1.COURSECD, T1.MAJORCD, T1.COURSECODE  ");
        stb.append(" FROM ");
        stb.append("   SCHREG_REGD_DAT T1 ");
        stb.append("   LEFT JOIN SEMESTER_MST T2  ");
        stb.append("     ON T1.YEAR = T2.YEAR  ");
        stb.append("    AND T1.SEMESTER = T2.SEMESTER  ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR = '" +  _param._year + "' ");
        stb.append("   AND T1.GRADE = '" + _param._grade + "' ");
        stb.append("   AND '" + eDate + "' BETWEEN T2.SDATE AND T2.EDATE ");
        stb.append("   AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1  ");
        stb.append("                  WHERE S1.SCHREGNO = T1.SCHREGNO  ");
        stb.append("                    AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '" + eDate + "' THEN T2.EDATE ELSE '" + sDate + "' END)  ");
        stb.append("                    OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '" + sDate + "' THEN T2.EDATE ELSE '" + sDate + "' END)) )  ");
        stb.append("   AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1  ");
        stb.append("                  WHERE S1.SCHREGNO = T1.SCHREGNO  ");
        stb.append("                    AND S1.TRANSFERCD IN ('1','2') ");
        stb.append("                    AND CASE WHEN T2.EDATE < '" + sDate + "' THEN T2.EDATE ELSE '" + sDate + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE)  ");
        stb.append(" ), Z_CNT AS ( ");
        stb.append(" SELECT ");
        stb.append("   T1.COURSECD, ");
        stb.append("   T1.MAJORCD, ");
        stb.append("   T5.MAJORNAME, ");
        stb.append("   T1.COURSECODE, ");
        stb.append("   T6.COURSECODENAME, ");
        stb.append("   T1.GRADE, ");
        stb.append("   count(T1.schregno) AS ZAISEKI_CNT ");
        stb.append(" FROM ");
        stb.append("   SCHNO_A T1 ");
        stb.append("   LEFT JOIN MAJOR_MST T5 ");
        stb.append("     ON T5.COURSECD = T1.COURSECD ");
        stb.append("    AND T5.MAJORCD = T1.MAJORCD ");
        stb.append("   LEFT JOIN COURSECODE_MST T6 ");
        stb.append("     ON T6.COURSECODE = T1.COURSECODE ");
        stb.append(" GROUP BY ");
        stb.append("   T1.COURSECD, ");
        stb.append("   T1.MAJORCD, ");
        stb.append("   T5.MAJORNAME, ");
        stb.append("   T1.COURSECODE, ");
        stb.append("   T6.COURSECODENAME, ");
        stb.append("   T1.GRADE ");
        stb.append(" ORDER BY ");
        stb.append("   T1.COURSECD, ");
        stb.append("   T1.MAJORCD, ");
        stb.append("   T1.COURSECODE, ");
        stb.append("   T1.GRADE ");
        stb.append(" ), DEV_RANK AS ( ");
        stb.append(" SELECT ");
        stb.append("   row_number() over(partition by (T2.GRADE) order by GRADE_DEVIATION_RANK ) as R_NUM, ");
        stb.append("   T1.YEAR, ");
        stb.append("   T1.SEMESTER, ");
        stb.append("   T1.TESTKINDCD, ");
        stb.append("   T1.TESTITEMCD, ");
        stb.append("   T1.SCORE_DIV, ");
        stb.append("   T1.CLASSCD, ");
        stb.append("   T1.SCHOOL_KIND, ");
        stb.append("   T1.CURRICULUM_CD, ");
        stb.append("   T1.SUBCLASSCD, ");
        stb.append("   T2.GRADE, ");
        stb.append("   T2.HR_CLASS, ");
        stb.append("   T2.ATTENDNO, ");
        stb.append("   T2.COURSECD, ");
        stb.append("   T2.MAJORCD, ");
        stb.append("   T2.COURSECODE, ");
        stb.append("   T3.NAME, ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T1.SCORE, ");
        stb.append("   T1.AVG, ");
        stb.append("   T1.GRADE_DEVIATION_RANK ");
        stb.append(" FROM ");
        stb.append("   RECORD_RANK_SDIV_DAT T1 ");
        stb.append("   LEFT JOIN SCHREG_REGD_DAT T2 ");
        stb.append("     ON T2.YEAR = T1.YEAR ");
        stb.append("    AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("   LEFT JOIN SCHREG_BASE_MST T3 ");
        stb.append("     ON T3.SCHREGNO = T1.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR = '" + _param._year + "' ");
        stb.append("   AND T1.SEMESTER = '1' ");
        stb.append("   AND T1.TESTKINDCD = '99' ");
        stb.append("   AND T1.TESTITEMCD = '00' ");
        stb.append("   AND T1.SCORE_DIV = '08' ");
        stb.append("   AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '99-" + _param._schoolKind + "-99-" + SUBCLASSCD999999 + "' ");
        stb.append("   AND T2.GRADE = '" + _param._grade + "' ");
        stb.append(" ORDER BY ");
        stb.append("   T2.GRADE, ");
        stb.append("   T1.GRADE_DEVIATION_RANK, ");
        stb.append("   T2.HR_CLASS, ");
        stb.append("   T2.ATTENDNO ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("   T1.* ");
        stb.append(" FROM ");
        stb.append("   DEV_RANK T1 ");
        stb.append("   LEFT JOIN Z_CNT T2 ");
        stb.append("     ON T2.COURSECD = T1.COURSECD ");
        stb.append("    AND T2.MAJORCD = T1.MAJORCD ");
        stb.append("    AND T2.COURSECODE = T1.COURSECODE ");
        stb.append("    AND T2.GRADE = T1.GRADE ");
        stb.append(" WHERE ");
        stb.append("   T1.GRADE_DEVIATION_RANK <= (SELECT GRADE_DEVIATION_RANK FROM DEV_RANK WHERE R_NUM = CEIL(T2.ZAISEKI_CNT * 5 / 100.0)) ");
        return stb.toString();
    }

    //皆勤・精勤者を取得
    final List getKaikinSeikin(final DB2UDB db2, int[] kFullCnt) {
    	final String query = getKaikinSeikinSql();
    	log.debug("getKaikinSeikin : "+ query);
    	final List getAry7 = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(query);
            rs = ps.executeQuery();
            while (rs.next()) {
            	final String schregno = rs.getString("SCHREGNO");
            	final String kaikin_Cd = rs.getString("KAIKIN_CD");
            	final String kaikin_Name = rs.getString("KAIKIN_NAME");
            	final String kaikin_Div = rs.getString("KAIKIN_DIV");
            	final String ref_Year = rs.getString("REF_YEAR");
            	final String coursecd = rs.getString("COURSECD");
            	final String majorcd = rs.getString("MAJORCD");
            	final String coursecode = rs.getString("COURSECODE");
            	final String grade = rs.getString("GRADE");
            	final String grade_Cd = rs.getString("GRADE_CD");
            	final String grade_Name1 = rs.getString("GRADE_NAME1");
            	final String hr_Class = rs.getString("HR_CLASS");
            	final String hr_Name = rs.getString("HR_NAME");
            	final String hr_Nameabbv = rs.getString("HR_NAMEABBV");
            	final String attendno = rs.getString("ATTENDNO");
            	final String name = rs.getString("NAME");
            	kaikinInfo addwk = new kaikinInfo(schregno, kaikin_Cd, kaikin_Name, kaikin_Div, ref_Year, coursecd, majorcd, coursecode, grade, grade_Cd, grade_Name1, hr_Class, hr_Name, hr_Nameabbv, attendno, name);
                getAry7.add(addwk);
                if (_param._print63KaikinFlg) {
                    if ("1".equals(kaikin_Div) && "6".equals(ref_Year)) {
                    	kFullCnt[0]++;
                    }
                    if ("1".equals(kaikin_Div) && "3".equals(ref_Year)) {
                    	kFullCnt[1]++;
                    }
                }
                if ("1".equals(kaikin_Div) && "1".equals(ref_Year)) {
                	kFullCnt[2]++;
                }
                if ("2".equals(kaikin_Div) && "1".equals(ref_Year)) {
                	kFullCnt[3]++;
                }
            }
        } catch (Exception e) {
            log.error("sql exception!", e);
        } finally {
            DbUtils.closeQuietly(ps);
        }
        return getAry7;
    }
    private String  getKaikinSeikinSql() {
    	final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T1.KAIKIN_CD, ");
        stb.append("   T2.KAIKIN_NAME, ");
        stb.append("   T2.KAIKIN_DIV, ");
        stb.append("   T2.REF_YEAR, ");
        stb.append("   T3.COURSECD, ");
        stb.append("   T3.MAJORCD, ");
        stb.append("   T3.COURSECODE, ");
        stb.append("   T3.GRADE, ");
        stb.append("   T4.GRADE_CD, ");
        stb.append("   T4.GRADE_NAME1, ");
        stb.append("   T3.HR_CLASS, ");
        stb.append("   T5.HR_NAME, ");
        stb.append("   T5.HR_NAMEABBV, ");
        stb.append("   T3.ATTENDNO, ");
        stb.append("   T6.NAME ");
        stb.append(" FROM ");
        stb.append("   KAIKIN_DAT T1 ");
        stb.append("   INNER JOIN KAIKIN_MST T2 ");
        stb.append("     ON T2.KAIKIN_CD = T1.KAIKIN_CD ");
        stb.append("   LEFT JOIN SCHREG_REGD_DAT T3 ");
        stb.append("     ON T3.YEAR = T1.YEAR ");
        stb.append("    AND T3.SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append("    AND T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("   LEFT JOIN SCHREG_REGD_GDAT T4 ");
        stb.append("     ON T4.YEAR = T3.YEAR ");
        stb.append("    AND T4.GRADE = T3.GRADE ");
        stb.append("   LEFT JOIN SCHREG_REGD_HDAT T5 ");
        stb.append("     ON T5.YEAR = T3.YEAR ");
        stb.append("    AND T5.SEMESTER = T3.SEMESTER ");
        stb.append("    AND T5.GRADE = T3.GRADE ");
        stb.append("    AND T5.HR_CLASS = T3.HR_CLASS ");
        stb.append("   LEFT JOIN SCHREG_BASE_MST T6 ");
        stb.append("     ON T6.SCHREGNO = T3.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR = '" + _param._year + "' ");
        stb.append("   AND T3.GRADE = '" + _param._grade + "' ");
        stb.append("   AND T1.KAIKIN_FLG = '1' ");
        stb.append("   AND VALUE(T1.INVALID_FLG, '0') = '0' ");
        stb.append(" ORDER BY ");
        stb.append("   HR_CLASS, ");
        stb.append("   ATTENDNO ");
        return stb.toString();
    }



    private class hrInfo {
        final String _year;
        final String _grade;
        final String _hr_Class;
        final String _hr_Name;
        final String _hr_Nameabbv;
        final String _grade_Name;
        final String _hr_Class_Name1;
        final String _hr_Class_Name2;
        final String _hr_Faccd;
        final String _tr_Cd1;
        final String _tr_Cd2;
        final String _tr_Cd3;
        final String _subtr_Cd1;
        final String _subtr_Cd2;
        final String _subtr_Cd3;
        final String _classweeks;
        final String _classdays;
        public hrInfo (final String year, final String grade, final String hr_Class, final String hr_Name, final String hr_Nameabbv, final String grade_Name, final String hr_Class_Name1, final String hr_Class_Name2, final String hr_Faccd, final String tr_Cd1, final String tr_Cd2, final String tr_Cd3, final String subtr_Cd1, final String subtr_Cd2, final String subtr_Cd3, final String classweeks, final String classdays)
        {
            _year = year;
            _grade = grade;
            _hr_Class = hr_Class;
            _hr_Name = hr_Name;
            _hr_Nameabbv = hr_Nameabbv;
            _grade_Name = grade_Name;
            _hr_Class_Name1 = hr_Class_Name1;
            _hr_Class_Name2 = hr_Class_Name2;
            _hr_Faccd = hr_Faccd;
            _tr_Cd1 = tr_Cd1;
            _tr_Cd2 = tr_Cd2;
            _tr_Cd3 = tr_Cd3;
            _subtr_Cd1 = subtr_Cd1;
            _subtr_Cd2 = subtr_Cd2;
            _subtr_Cd3 = subtr_Cd3;
            _classweeks = classweeks;
            _classdays = classdays;
        }
    }

    private class courseInfo {
        final String _coursecode;
        final String _coursecodename;
        final String _coursecodeabbv1;
        final String _coursecodeabbv2;
        final String _coursecodeabbv3;
        public courseInfo (final String coursecode, final String coursecodename, final String coursecodeabbv1, final String coursecodeabbv2, final String coursecodeabbv3)
        {
            _coursecode = coursecode;
            _coursecodename = coursecodename;
            _coursecodeabbv1 = coursecodeabbv1;
            _coursecodeabbv2 = coursecodeabbv2;
            _coursecodeabbv3 = coursecodeabbv3;
        }
    }

    private class schCntInfo {
        final String _coursecd;
        final String _majorcd;
        final String _coursecode;
        final String _hr_Class;
        final String _grade;
        final String _zaiseki_Cnt;
        final String _kyuugaku_Cnt;
        final String _fukugaku_Cnt;
        final String _ryuugaku_Cnt;
        public schCntInfo (final String coursecd, final String majorcd, final String coursecode, final String hr_Class, final String grade, final String zaiseki_Cnt, final String kyuugaku_Cnt, final String fukugaku_Cnt, final String ryuugaku_Cnt)
        {
            _coursecd = coursecd;
            _majorcd = majorcd;
            _coursecode = coursecode;
            _hr_Class = hr_Class;
            _grade = grade;
            _zaiseki_Cnt = zaiseki_Cnt;
            _kyuugaku_Cnt = kyuugaku_Cnt;
            _fukugaku_Cnt = fukugaku_Cnt;
            _ryuugaku_Cnt = ryuugaku_Cnt;
        }
        private String getMargeCourseCds() {
        	return StringUtils.defaultString(_coursecd) + StringUtils.defaultString(_majorcd) + StringUtils.defaultString(_coursecode);
        }
        private String getMargeKeyCds() {
        	return getMargeCourseCds() + "-" + StringUtils.defaultString(_hr_Class);
        }
    }

    private class neAttInfo {
        final String _coursecd;
        final String _majorcd;
        final String _coursecode;
        final String _grade;
        final String _hr_Class;
        final String _hr_Nameabbv;
        final String _attendno;
        final String _schregno;
        final String _name;
        final String _fusoku_Flg;
        final String _classdays;
        final String _suspend;
        final String _mourning;
        final String _present1;
        final String _absent;
        final String _present2;
        final String _late;
        final String _early;
        final String _kansan_Val;
        public neAttInfo (final String coursecd, final String majorcd, final String coursecode, final String grade, final String hr_Class, final String hr_Nameabbv, final String attendno, final String schregno, final String name, final String fusoku_Flg, final String classdays, final String suspend, final String mourning, final String present1, final String absent, final String present2, final String late, final String early, final String kansan_Val)
        {
            _coursecd = coursecd;
            _majorcd = majorcd;
            _coursecode = coursecode;
            _grade = grade;
            _hr_Class = hr_Class;
            _hr_Nameabbv = hr_Nameabbv;
            _attendno = attendno;
            _schregno = schregno;
            _name = name;
            _fusoku_Flg = fusoku_Flg;
            _classdays = classdays;
            _suspend = suspend;
            _mourning = mourning;
            _present1 = present1;
            _absent = absent;
            _present2 = present2;
            _late = late;
            _early = early;
            _kansan_Val = kansan_Val;
        }
        private String getMargeCourseCds() {
        	return StringUtils.defaultString(_coursecd) + StringUtils.defaultString(_majorcd) + StringUtils.defaultString(_coursecode);
        }
    }

    private class nySchInfo {
        final String _coursecd;
        final String _majorcd;
        final String _coursecode;
        final String _grade;
        final String _hr_Class;
        final String _attendno;
        final String _schregno;
        final String _name;
        final String _score;
        final String _avg;
        final String _pat1;
        final String _pat2;
        public nySchInfo (final String coursecd, final String majorcd, final String coursecode, final String grade, final String hr_Class, final String attendno, final String schregno, final String name, final String score, final String avg, final String pat1, final String pat2)
        {
            _coursecd = coursecd;
            _majorcd = majorcd;
            _coursecode = coursecode;
            _grade = grade;
            _hr_Class = hr_Class;
            _attendno = attendno;
            _schregno = schregno;
            _name = name;
            _score = score;
            _avg = avg;
            _pat1 = pat1;
            _pat2 = pat2;
        }
        private String getMargeCourseCds() {
        	return StringUtils.defaultString(_coursecd) + StringUtils.defaultString(_majorcd) + StringUtils.defaultString(_coursecode);
        }
    }

    private class exSchInfo {
        final String _year;
        final String _semester;
        final String _testkindcd;
        final String _testitemcd;
        final String _score_Div;
        final String _classcd;
        final String _school_Kind;
        final String _curriculum_Cd;
        final String _subclasscd;
        final String _grade;
        final String _hr_Class;
        final String _attendno;
        final String _coursecd;
        final String _majorcd;
        final String _coursecode;
        final String _name;
        final String _schregno;
        final String _score;
        final String _avg;
        final String _grade_Deviation_Rank;
        public exSchInfo (final String year, final String semester, final String testkindcd, final String testitemcd, final String score_Div, final String classcd, final String school_Kind, final String curriculum_Cd, final String subclasscd, final String grade, final String hr_Class, final String attendno, final String coursecd, final String majorcd, final String coursecode, final String name, final String schregno, final String score, final String avg, final String grade_Deviation_Rank)
        {
            _year = year;
            _semester = semester;
            _testkindcd = testkindcd;
            _testitemcd = testitemcd;
            _score_Div = score_Div;
            _classcd = classcd;
            _school_Kind = school_Kind;
            _curriculum_Cd = curriculum_Cd;
            _subclasscd = subclasscd;
            _grade = grade;
            _hr_Class = hr_Class;
            _attendno = attendno;
            _coursecd = coursecd;
            _majorcd = majorcd;
            _coursecode = coursecode;
            _name = name;
            _schregno = schregno;
            _score = score;
            _avg = avg;
            _grade_Deviation_Rank = grade_Deviation_Rank;
        }
        private String getMargeCourseCds() {
        	return StringUtils.defaultString(_coursecd) + StringUtils.defaultString(_majorcd) + StringUtils.defaultString(_coursecode);
        }
    }

    private class kaikinInfo {
        final String _schregno;
        final String _kaikin_Cd;
        final String _kaikin_Name;
        final String _kaikin_Div;
        final String _ref_Year;
        final String _coursecd;
        final String _majorcd;
        final String _coursecode;
        final String _grade;
        final String _grade_Cd;
        final String _grade_Name1;
        final String _hr_Class;
        final String _hr_Name;
        final String _hr_Nameabbv;
        final String _attendno;
        final String _name;
        public kaikinInfo (final String schregno, final String kaikin_Cd, final String kaikin_Name, final String kaikin_Div, final String ref_Year, final String coursecd, final String majorcd, final String coursecode, final String grade, final String grade_Cd, final String grade_Name1, final String hr_Class, final String hr_Name, final String hr_Nameabbv, final String attendno, final String name)
        {
            _schregno = schregno;
            _kaikin_Cd = kaikin_Cd;
            _kaikin_Name = kaikin_Name;
            _kaikin_Div = kaikin_Div;
            _ref_Year = ref_Year;
            _coursecd = coursecd;
            _majorcd = majorcd;
            _coursecode = coursecode;
            _grade = grade;
            _grade_Cd = grade_Cd;
            _grade_Name1 = grade_Name1;
            _hr_Class = hr_Class;
            _hr_Name = hr_Name;
            _hr_Nameabbv = hr_Nameabbv;
            _attendno = attendno;
            _name = name;
        }
        private String getMargeCourseCds() {
        	return StringUtils.defaultString(_coursecd) + StringUtils.defaultString(_majorcd) + StringUtils.defaultString(_coursecode);
        }
    }

    private class kaikinMst {
        final String _kaikin_Cd;
        final String _kaikin_Name;
        final String _kaikin_Div;
        final String _ref_Year;
        final String _kesseki_Condition;
        final String _tikoku_Condition;
        final String _soutai_Condition;
        final String _kesseki_Kansan;
        final String _kekka_Jisu_Condition;
        final String _priority;
        final String _kaikin_Flg;
        public kaikinMst (final String kaikin_Cd, final String kaikin_Name, final String kaikin_Div, final String ref_Year, final String kesseki_Condition, final String tikoku_Condition, final String soutai_Condition, final String kesseki_Kansan, final String kekka_Jisu_Condition, final String priority, final String kaikin_Flg)
        {
            _kaikin_Cd = kaikin_Cd;
            _kaikin_Name = kaikin_Name;
            _kaikin_Div = kaikin_Div;
            _ref_Year = ref_Year;
            _kesseki_Condition = kesseki_Condition;
            _tikoku_Condition = tikoku_Condition;
            _soutai_Condition = soutai_Condition;
            _kesseki_Kansan = kesseki_Kansan;
            _kekka_Jisu_Condition = kekka_Jisu_Condition;
            _priority = priority;
            _kaikin_Flg = kaikin_Flg;
        }
    }


    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 75058 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {

    	final String _ctrlYear;
    	final String _ctrlSemester;
    	final String _ctrlDate;
    	final String _schoolKind;
    	final String _grade;
    	final String _sdate;
    	final String _edate;
    	final String _creditLine;
    	final String _documentRoot;
    	final String _useCurriculumcd;
    	final String _semeDate;
    	final String _semeSdate;
    	final String _semeEdate;
    	final String _semeFlg;
        final boolean _print63KaikinFlg;

        final String _year;
        final String _semester;  //通常のsemesterではなく、日付から逆算したsemesterなので注意。
        final String _gradeCd;
        final String _gradeName;
        final String _a023Abbv1;

        private Map _semesterMap;

//        private Map _averageDatMap = Collections.EMPTY_MAP;
        final Map _attendParamMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
        	_ctrlYear = request.getParameter("CTRL_YEAR");
            _year = request.getParameter("CTRL_YEAR");
            _ctrlDate = StringUtils.replace(request.getParameter("CTRL_DATE"), "/", "-");
            _grade = request.getParameter("GRADE");

            _edate = request.getParameter("EDATE").replace('/', '-');
            _sdate = request.getParameter("SDATE").replace('/', '-');
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _creditLine = request.getParameter("CREDIT_LINE");

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");

            _schoolKind = getGdat(db2, "SCHOOL_KIND");
            _gradeCd = getGdat(db2, "GRADE_CD");
            _a023Abbv1 = getA023Abbv1(db2, _schoolKind);
            _gradeName = _a023Abbv1 + " " + String.valueOf(Integer.parseInt(_gradeCd)) + "年";

            _print63KaikinFlg = "03".equals(_gradeCd) ? true : false;

            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _semeSdate = request.getParameter("SEME_SDATE");
            _semeEdate = request.getParameter("SEME_EDATE");
            _semeFlg = request.getParameter("SEME_FLG");
            _semeDate = request.getParameter("SEME_DATE");
            _documentRoot = request.getParameter("DOCUMENTROOT");

            _semesterMap = getSemesterMap(db2, _year);
            String decideSemester = "";
            String lastSemester = "0";
            for (Iterator ite = _semesterMap.keySet().iterator();ite.hasNext();) {
            	final String kStr = (String)ite.next();
            	if (SEMEALL.equals(kStr)) continue;
            	final Map info = (Map)_semesterMap.get(kStr);
            	final String sdate = (String)info.get("SDATE");
            	final String edate = (String)info.get("EDATE");
            	if (sdate.compareTo(_edate) <= 0 && edate.compareTo(_edate) >= 0) {
            		decideSemester = kStr;
            	}
            	if (kStr.compareTo(lastSemester) > 0) {
            		lastSemester = kStr;
            	}
            }
            if (!"".equals(decideSemester)) {
                _semester = decideSemester;
            } else {
                _semester = _ctrlSemester;  //決まらなかったときはCTRL_SEMESTER
            }

        }

        public Map getSemesterMap(final DB2UDB db2, final String year) {

            final String sql = " SELECT * FROM SEMESTER_MST WHERE YEAR = '" + year + "' ";

            return KnjDbUtils.getKeyMap(KnjDbUtils.query(db2, sql), "SEMESTER");
        }


        private String getA023Abbv1(final DB2UDB db2, final String schoolKind) {

            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     ABBV1 ");
            stb.append(" FROM NAME_MST T1 ");
            stb.append(" WHERE T1.NAMECD1 = 'A023' ");
            stb.append("   AND T1.NAME1 = '" + schoolKind + "' ");

            String rtn = KnjDbUtils.getOne(KnjDbUtils.query(db2, stb.toString()));

            return rtn;
        }

        private String getGdat(final DB2UDB db2, final String field) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     " + field + " ");
            stb.append(" FROM SCHREG_REGD_GDAT T1 ");
            stb.append(" WHERE T1.YEAR = '" + _year + "' ");
            stb.append("   AND T1.GRADE = '" + _grade + "' ");

            String rtn = KnjDbUtils.getOne(KnjDbUtils.query(db2, stb.toString()));

            return rtn;
        }
    }
}

// eof

