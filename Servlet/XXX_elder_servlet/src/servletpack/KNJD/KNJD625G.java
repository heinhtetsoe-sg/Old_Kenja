/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: 5f340da379415859d8d2a115cbde15c3f22d6ea9 $
 *
 * 作成日: 2020/04/14
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJEditString;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

public class KNJD625G {

    private static final Log log = LogFactory.getLog(KNJD625G.class);

    private static final String SEME1 = "1";
    private static final String SEME2 = "2";
    private static final String SEME3 = "3";
    private static final String SEMEALL = "9";
    private static final String SELECT_CLASSCD_UNDER = "89";

    private static final String SDIV1010108 = "1010108"; //1学期中間考査
    private static final String SDIV1990008 = "1990008"; //1学期期末考査
    private static final String SDIV2010108 = "2010108"; //2学期中間考査
    private static final String SDIV2990008 = "2990008"; //2学期期末考査
    private static final String SDIV3990008 = "3990008"; //3学期期末考査
    private static final String SDIV9990008 = "9990008"; //学年評価
    private static final String SDIV9990009 = "9990009"; //学年評定

    private static final String ALL3 = "333333";
    private static final String ALL5 = "555555";
    private static final String ALL9 = "999999";

    private static final String HYOTEI_TESTCD = "9990009";

    //実力テスト　教科列番号
    private static final int COL_ENG = 1; //英語
    private static final int COL_MATH = 2; //数学
    private static final int COL_LANG = 3; //国語
    private static final int COL_SCI_SOC = 4; //理科・社会
    private static final int COL_ALL = 5; //総合
    
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
        for (Iterator iterator = studentList.iterator(); iterator.hasNext();) {
            final Student student = (Student) iterator.next();

            //1ページ目
            printPaga1(db2, svf, student);

            //2ページ目
            printPaga2(db2, svf, student);

            _hasData = true;
        }
    }

    private String add(final String num1, final String num2) {
    	if (!NumberUtils.isDigits(num1)) { return num2; }
    	if (!NumberUtils.isDigits(num2)) { return num1; }
    	return String.valueOf(Integer.parseInt(num1) + Integer.parseInt(num2));
    }

    private void printPaga1(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        final String form = "KNJD625G_1.frm";
        svf.VrSetForm(form , 1);

        //明細部以外を印字
        printTitlePage1(db2, svf, student);

        //明細
        for (Iterator it = student._studentHistList.iterator(); it.hasNext();) {
            final StudentHist studentHist = (StudentHist) it.next();
            int line = 1; //出力行


            //科目リスト
            _param.setSubclassMst(db2, studentHist._year);
            final List subclassList = subclassListRemoveD026();
            Collections.sort(subclassList);

        	//学年による印字位置の設定
            int grade = Integer.parseInt(_param._grade);
            int gradeHist = Integer.parseInt(studentHist._grade); //比較用学年
            int gradeLine = 1; //印字位置 1or2or3
            //学年の判定
            if(grade != gradeHist) {
            	//現在学年でない場合
            	grade = grade - 1; //現在学年 - 1
            	if(grade == gradeHist) {
                	gradeLine = gradeLine + 1; //1年前
            	} else {
            		//1年前でない場合
            		grade = grade - 1; //現在学年 - 2
            		if(grade == gradeHist) {
                    	gradeLine = gradeLine + 2; //2年前
            		} else {
            			//現在含め過去3学年以外の場合
            			continue;
            		}
            	}
            }
            final String gradeField = String.valueOf(gradeLine);
            svf.VrsOut("GRADE"+gradeField, studentHist._grade_name); //学年

            //合計単位数の算出
            String totalCredit = null;
            for (Iterator itSubclass = subclassList.iterator(); itSubclass.hasNext();) {
            	final SubclassMst subclassMst = (SubclassMst) itSubclass.next();
                final String subclassCd = subclassMst._subclasscd;

                final boolean isPrint = studentHist._printSubclassMap.containsKey(subclassCd) || studentHist._attendSubClassMap.containsKey(subclassCd);
                if (!isPrint) {
                	itSubclass.remove();
                } else {
                	final String credit = _param.getCredits(studentHist, subclassCd);
            		totalCredit = add(totalCredit, credit);
                }
            }
    		//成績一覧
            Map<String, String> classCdMap = new LinkedHashMap(); 
            for (Iterator itSubclass = subclassList.iterator(); itSubclass.hasNext();) {

            	final SubclassMst subclassMst = (SubclassMst) itSubclass.next();
                final String subclassCd = subclassMst._subclasscd;
                
                classCdMap.put(subclassMst._classcd, subclassMst._classabbv);
                
//                svf.VrsOut("CLASS_NAME", subclassMst._classname); //教科名
                String subclassField = KNJ_EditEdit.getMS932ByteLength(subclassMst._subclassname) > 6 ? "_2" : "_1";
            	svf.VrsOutn("SUBCLASS_NAME" + gradeField + subclassField, line, subclassMst._subclassname); //科目名

        		//単位数
        		svf.VrsOutn("CREDIT", line, _param.getCredits(studentHist, subclassCd)); //単位数

                if (studentHist._printSubclassMap.containsKey(subclassCd)) {
                	final ScoreData scoreData = (ScoreData) studentHist._printSubclassMap.get(subclassCd);
                	if (_param._isOutputDebug) {
                		log.info(" score = " + scoreData);
                	}

                	//1学期
                	svf.VrsOutn("SCORE" + gradeField + "_1_1", line, getRoundValue(scoreData.score(SDIV1010108), 1)); //中間
                	svf.VrsOutn("SCORE" + gradeField + "_1_2", line, getRoundValue(scoreData.score(SDIV1990008), 1)); //期末

                	//2学期
                	if(!studentHist._currentFlg || _param._semes2Flg) {
                    	svf.VrsOutn("SCORE" + gradeField + "_2_1", line, getRoundValue(scoreData.score(SDIV2010108), 1)); //中間
                    	svf.VrsOutn("SCORE" + gradeField + "_2_2", line, getRoundValue(scoreData.score(SDIV2990008), 1)); //期末
                	}

                	//3学期
                	if(!studentHist._currentFlg || _param._semes3Flg) {
                    	svf.VrsOutn("SCORE" + gradeField + "_3_1", line, getRoundValue(scoreData.score(SDIV3990008), 1)); //期末
                	}

                	//学年末
                	if(!studentHist._currentFlg || _param._semes9Flg) {
                    	svf.VrsOutn("SCORE" + gradeField + "_9_1", line, getRoundValue(scoreData.score(SDIV9990008), 1)); //評価
                    	svf.VrsOutn("SCORE" + gradeField + "_9_2", line, getRoundValue(scoreData.score(SDIV9990009), 1)); //評定
                	}

                	//単位数
            		final String credit = _param.getCredits(studentHist, subclassCd);
            		svf.VrsOutn("CREDIT"+gradeField, line, credit);
                }

                //欠課
                if (studentHist._attendSubClassMap.containsKey(subclassCd)) {
                    final Map<String, SubclassAttendance> attendSubMap = studentHist._attendSubClassMap.get(subclassCd);
                    for (final Iterator itSeme = _param._semesterMap.keySet().iterator(); itSeme.hasNext();) {
                    	BigDecimal sick = BigDecimal.ZERO;
                        final String semester = (String) itSeme.next();
                        if(SEME2.equals(semester) && !_param._semes2Flg) continue;
                        if(SEME3.equals(semester) && !_param._semes3Flg) continue;
                        if(SEMEALL.equals(semester) && !_param._semes9Flg) continue;
                        if (attendSubMap.containsKey(semester)) {
                            final SubclassAttendance attendance= attendSubMap.get(semester);
                            if(attendance._sick != null) {
                                sick = attendance._sick;
                            }
                        }
                        svf.VrsOutn("KEKKA" + gradeField + "_" + semester, line, sick.toString()); //欠課

                        if (isAddKekkaTotal(subclassMst, studentHist._attendSubClassMap.keySet())) {
                        	//合計を設定
                        	if (!studentHist._attendSubClassMap.containsKey(ALL9)) {
                        		//ALL9無し
                        		studentHist._attendSubClassMap.put(ALL9, new TreeMap());
                        	}
                        	Map<String, SubclassAttendance> setSubAttendMap = studentHist._attendSubClassMap.get(ALL9);
                        	if (setSubAttendMap.containsKey(semester)) {
                        		//学期一致
                        		final SubclassAttendance attendance = setSubAttendMap.get(semester);
                        		if(sick != null) sick = sick.add(attendance._sick);
                        	}
                        	setSubAttendMap.put(semester, new SubclassAttendance(null, null, sick, null, null));
                        }
                    }
                } else {
                    //科目が存在しない場合、'0'を印字
                	svf.VrsOutn("KEKKA"+ gradeField +"_1", line, "0"); //欠課
                    if(_param._semes2Flg) svf.VrsOutn("KEKKA"+ gradeField +"_2", line, "0"); //欠課
                    if(_param._semes3Flg) svf.VrsOutn("KEKKA"+ gradeField +"_3", line, "0"); //欠課
                    if(_param._semes9Flg) svf.VrsOutn("KEKKA"+ gradeField +"_9", line, "0"); //欠課
                }
                line++;
            }
            
            //成績一覧 合計の印字
            printTotal(svf, studentHist, totalCredit, gradeField);
        }
        svf.VrEndPage();
    }

    private String getRoundValue(final String value, int roundNum) {
        if (value == null || "".equals(value)) return null;
        BigDecimal bd = new BigDecimal(value);
        
        return bd.setScale(roundNum, BigDecimal.ROUND_HALF_UP).toString();
    }

	private void printTitlePage1(final DB2UDB db2, final Vrw32alp svf, final Student student) {
    	//明細部以外を印字

        //生徒情報
        svf.VrsOut("SCHREGNO", student._schregno); //学籍番号
        String nameField = KNJ_EditEdit.getMS932ByteLength(student._name) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(student._name) > 20 ? "2" : "1";
        svf.VrsOut("NAME" + nameField, student._name); //氏名
        svf.VrsOut("ZIPNO", student._zipcd); //郵便番号
        String addr1Field = KNJ_EditEdit.getMS932ByteLength(student._addr1) > 50 ? "_3" : KNJ_EditEdit.getMS932ByteLength(student._addr1) > 40 ? "_2" : "_1";
        svf.VrsOut("ADDR1" + addr1Field, student._addr1); //住所1
        String addr2Field = KNJ_EditEdit.getMS932ByteLength(student._addr2) > 50 ? "_3" : KNJ_EditEdit.getMS932ByteLength(student._addr2) > 40 ? "_2" : "_1";
        svf.VrsOut("ADDR2" + addr2Field, student._addr2); //住所2
        
        //保護者情報
        String gnameField = KNJ_EditEdit.getMS932ByteLength(student._name) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(student._name) > 20 ? "2" : "1";
        svf.VrsOut("GUARD_NAME" + gnameField, student._guard_name); //保護者氏名
        svf.VrsOut("GUARD_ZIPNO", student._guard_zipcd); //保護者郵便番号
        String gaddr1Field = KNJ_EditEdit.getMS932ByteLength(student._addr1) > 50 ? "_3" : KNJ_EditEdit.getMS932ByteLength(student._addr1) > 40 ? "_2" : "_1";
        svf.VrsOut("GUARD_ADDR1" + gaddr1Field, student._guard_addr1); //保護者住所1
        String gaddr2Field = KNJ_EditEdit.getMS932ByteLength(student._addr2) > 50 ? "_3" : KNJ_EditEdit.getMS932ByteLength(student._addr2) > 40 ? "_2" : "_1";
        svf.VrsOut("GUARD_ADDR2" + gaddr2Field, student._guard_addr2); //保護者住所2

        //科目リスト
        _param.setSubclassMst(db2, _param._loginYear);
        final List subclassList = subclassListRemoveD026();
        Collections.sort(subclassList);

    	//学年による印字位置の設定
        int grade = Integer.parseInt(_param._grade);
        int gradeHist = grade; //比較用学年

        for (Iterator it = student._studentHistList.iterator(); it.hasNext();) {
            int line = 1;
            final StudentHist studentHist = (StudentHist) it.next();
            grade = Integer.parseInt(_param._grade); //現在学年
            gradeHist = Integer.parseInt(studentHist._grade); //比較用学年

            int gradeLine = student._studentHistList.size(); //印字位置 3or2or1            
            //学年の判定
            if(grade != gradeHist) {
            	//現在学年でない場合
            	grade = grade - 1; //現在学年 - 1
            	if(grade == gradeHist) {
                	gradeLine = gradeLine - 1; //1年前
            	} else {
            		//1年前でない場合
            		grade = grade - 1; //現在学年 - 2
            		if(grade == gradeHist) {
                    	gradeLine = gradeLine - 2; //2年前
            		} else {
            			//現在含め過去3学年以外の場合
            			continue;
            		}
            	}
            }

            //特別活動の記録
            final String field = "SP_ACT" + String.valueOf(gradeLine);
            final String club = getSchregClub(db2, studentHist._year, student._schregno);
            List clubList = knjobj.retDividString(club, 44, 3);
            for (int i = 0 ; i < clubList.size(); i++) {
                svf.VrsOutn(field, line, (String) clubList.get(i)); //部活動
                line++;
            }
            final String committee = getSchregCommittee(db2, studentHist._year, student._schregno);
            List committeeList = knjobj.retDividString(committee, 44, 3);
            for (int i = 0 ; i < committeeList.size(); i++) {
            	svf.VrsOutn(field, line, (String) committeeList.get(i)); //委員会・生徒会
                line++;
            }

            //出欠の記録
            printAttend(svf, studentHist, gradeLine);
            
            //各教科評定平均
            printHyouteiAvg(db2, svf , studentHist, gradeLine);

        }

        //資格情報
        final String qualified = getSchregQualified(db2, student._schregno);
        List qualifiedList = knjobj.retDividString(qualified, 36, 12);
        for (int i = 0 ; i < qualifiedList.size(); i++) {
        	svf.VrsOutn("QUALIFY", i, (String) qualifiedList.get(i));
        }
    }

    private boolean isAddKekkaTotal(final SubclassMst subclassMst, final Collection<String> subclasscdSet) {
    	if (subclassMst.isMoto()) {
    		// 元科目なら、先科目が表示対象でない場合に加算する
    		return subclasscdSet.contains(subclassMst._combined._subclasscd);
    	}
    	return true;
	}

    private void printTotal(final Vrw32alp svf, final StudentHist studentHist, final String totalCredit, final String gradeField) {
        //■成績一覧 合計の印字
        final String subclassCd = ALL9;
        final ScoreData scoreData = (ScoreData) studentHist._printSubclassMap.get(subclassCd);
        if(scoreData != null) {
        	//1学期
        	printTotalScore(svf, scoreData, gradeField, "1", SDIV1010108, SDIV1990008);


        	//2学期
        	if(!studentHist._currentFlg || _param._semes2Flg) {
            	printTotalScore(svf, scoreData, gradeField, "2", SDIV2010108, SDIV2990008);
        	}

        	//3学期
        	if(!studentHist._currentFlg || _param._semes3Flg) {
        		printTotalScore(svf, scoreData, gradeField, "3", SDIV3990008, "");
        	}

        	//学年末
        	if(!studentHist._currentFlg || _param._semes9Flg) {
        		printTotalScore(svf, scoreData, gradeField, "9", SDIV9990008, SDIV9990009);
        	}

        	//単位数
    		final String credit = _param.getCredits(studentHist, subclassCd);
    		svf.VrsOutn("CREDIT"+gradeField, 19, credit);
        }


        if("H".equals(_param._schoolKind)) {
            //欠課
            BigDecimal sick = BigDecimal.ZERO;
            if (studentHist._attendSubClassMap.containsKey(subclassCd)) {
                final Map attendSubMap = (Map) studentHist._attendSubClassMap.get(subclassCd);
                for (final Iterator it = _param._semesterMap.keySet().iterator(); it.hasNext();) {
                	sick = BigDecimal.ZERO;
                    final String semester = (String) it.next();
                    if(SEME2.equals(semester) && !_param._semes2Flg) continue;
                    if(SEME3.equals(semester) && !_param._semes3Flg) continue;
                    if(SEMEALL.equals(semester) && !_param._semes9Flg) continue;
                    if (attendSubMap.containsKey(semester)) {
                        final SubclassAttendance attendance= (SubclassAttendance) attendSubMap.get(semester);
                        if(attendance._sick != null) {
                            sick = attendance._sick;
                        }
                    }
                    svf.VrsOutn("KEKKA"+ gradeField +"_" + semester, 19, sick.toString()); //欠課
                }
            }
        }
    }

    //成績一覧 合計欄 「合計」「平均」「組順位」「年順位」
    private void printTotalScore(final Vrw32alp svf, final ScoreData scoreData, final String gradeField, final String semesField, final String sdiv1, final String sdiv2) {
    	final String field1 = "SCORE" + gradeField + "_" + semesField + "_1"; //中間
    	final String field2 = "SCORE" + gradeField + "_" + semesField + "_2"; //期末
    	//合計・平均
    	svf.VrsOutn(field1, 19, scoreData.score(sdiv1)); //中間 合計
    	svf.VrsOutn(field2, 19, scoreData.score(sdiv2)); //期末 合計
    	svf.VrsOutn(field1, 20, getRoundValue(scoreData.avg(sdiv1), 1)); //中間 平均
    	svf.VrsOutn(field2, 20, getRoundValue(scoreData.avg(sdiv2), 1)); //期末 平均

    	//組順位
        final Rank hrRank1_1 = (Rank) scoreData._hrRankMap.get(sdiv1);
        if(hrRank1_1 != null) {
        	svf.VrsOutn(field1, 21, hrRank1_1._rank); //中間 組順位
        }
        final Rank hrRank1_2 = (Rank) scoreData._hrRankMap.get(sdiv2);
        if(hrRank1_2 != null) {
        	svf.VrsOutn(field2, 21, hrRank1_2._rank); //期末 組順位
        }

        //年順位
        final Rank gradeRank1_1 = (Rank) scoreData._gradeRankMap.get(sdiv1);
        if(gradeRank1_1 != null) {
        	svf.VrsOutn(field1, 22, gradeRank1_1._rank); //中間 年順位
        }
        final Rank gradeRank1_2 = (Rank) scoreData._gradeRankMap.get(sdiv2);
        if(gradeRank1_2 != null) {
        	svf.VrsOutn(field2, 22, gradeRank1_2._rank); //中間 年順位
        }
    }

    private void printPaga2(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        final String form = "KNJD625G_2.frm";
        svf.VrSetForm(form , 1);

        //明細部以外を印字
        printTitlePage2(db2, svf, student);

		//校外実力テスト
		Map<String, String>mockClassMap = getMockClassMap(db2, _param, student._schregno);
		Map<String, List<String>> mockRankRangeMap = getMockRankRangeMap(db2, _param, student);
		
		List<String> mockCdList = new ArrayList(student._mockCdMap.keySet());
		Collections.sort(mockCdList);
		Collections.reverse(mockCdList);

		List<String> classCdList = new ArrayList(student._classCdMap.keySet());
		Collections.sort(classCdList);

		//年度+模試CD単位ループ(降順)
		int mockRow = 1;
		for (String yearAndMockCd : mockCdList) {
			final String mockname = (String)student._mockCdMap.get(yearAndMockCd);
			final String mockNameSuf = (KNJ_EditEdit.getMS932ByteLength(mockname) <= 24) ? "1" : "2";
			svf.VrsOutn("MOCK_NAME1_" + mockNameSuf, mockRow, mockname);

			//各教科列の模試科目を表示するフィールド番号(4列目は複数科目なので科目ループの外側で定義)
       		int mockSubCol1 = 0;
    		int mockSubCol2 = 0;
    		int mockSubCol3 = 0;
    		int mockSubCol4 = 0;
    		int mockSubCol5 = 0;
			
			for (String classCd : classCdList) {
				//模試科目単位ループ

				int mockSubCol   = 0;
				int mockClassCol = 0;

        		if (!mockRankRangeMap.containsKey(yearAndMockCd + "-" + classCd)) continue;
        		
				List<String> mockSubNameAndDevList = mockRankRangeMap.get(yearAndMockCd + "-" + classCd); //教科ごとの模試科目リスト(値は科目名_成績)
				for (String mockSubNameAndDev : mockSubNameAndDevList) {
					final String[] tmp = StringUtils.split(mockSubNameAndDev, "_"); //0:模試科目名, 1:模試科目DEVIATION
					if (tmp.length != 2) continue;
					if ("".equals(tmp[1])) continue;

	        		final int classCdorder = getClassCdOrder(classCd);
	        		String className = "";
	        		if (classCdorder == -1) continue; //指定の教科以外は無視
					if (classCdorder == COL_ENG) { //1.英語
	        			mockClassCol = 1;
	        			mockSubCol1++;
	        			mockSubCol = mockSubCol1;
	        			className = "英語";
	        		} else if (classCdorder == COL_MATH) { //2.数学
	        			mockClassCol = 2;
	        			mockSubCol2++;
	        			className = "数学";
	        			mockSubCol = mockSubCol2;
	        		} else if (classCdorder == COL_LANG) { //3.国語
	        			mockClassCol = 3;
	        			mockSubCol3++;
	        			mockSubCol = mockSubCol3;
	        			className = "国語";
	        		} else if (classCdorder == COL_SCI_SOC) { //4.理科・社会
	        			mockClassCol = 4;
	        			mockSubCol4++;
	        			mockSubCol = mockSubCol4;
	        			className = "理科・社会";
	        		} else if (classCdorder == COL_ALL) { //5.総合
	        			mockClassCol = 5;
	        			mockSubCol5++;
	        			mockSubCol = mockSubCol5;
	        			className = "総合";
	        		}

        			svf.VrsOut("MOCK_CLASS_NAME" + mockClassCol, className);
					svf.VrsOutn("MOCK_SUBCLASS_NAME" + mockClassCol + "_" + mockSubCol, mockRow, tmp[0]);
					svf.VrsOutn("MOCK_SUBCLASS_DEVI" + mockClassCol + "_" + mockSubCol, mockRow, tmp[1]);
					
				}
				_hasData = true;
			}
			mockRow++; 
		}
        svf.VrEndPage();
    }

    private Integer getClassCdOrder (final String classcd) {
    	if ("J".equals(_param._schoolKind) && "19".equals(classcd) || "H".equals(_param._schoolKind) && "38".equals(classcd)) {
    		return COL_ENG;
    	} else if ("J".equals(_param._schoolKind) && "13".equals(classcd) || "H".equals(_param._schoolKind) && "34".equals(classcd)) {
    		return COL_MATH;
    	} else if ("J".equals(_param._schoolKind) && "11".equals(classcd) || "H".equals(_param._schoolKind) && "31".equals(classcd)) {
    		return COL_LANG;
    	} else if ("H".equals(_param._schoolKind) && "32".equals(classcd)
    			 || "H".equals(_param._schoolKind) && "33".equals(classcd)
    			 || "H".equals(_param._schoolKind) && "35".equals(classcd)) {
    		return COL_SCI_SOC;
    	} else if ("99".equals(classcd)) {
    		return COL_ALL;
    	}
    	return -1;    	
    }

	private void printTitlePage2(final DB2UDB db2, final Vrw32alp svf, final Student student) {
    	//明細部以外を印字

        //生徒情報
        svf.VrsOut("SCHREGNO", student._schregno); //学籍番号
        String nameField = KNJ_EditEdit.getMS932ByteLength(student._name) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(student._name) > 20 ? "2" : "1";
        svf.VrsOut("NAME" + nameField, student._name); //氏名
        svf.VrsOut("ZIPNO", student._zipcd); //郵便番号
        String addr1Field = KNJ_EditEdit.getMS932ByteLength(student._addr1) > 50 ? "_3" : KNJ_EditEdit.getMS932ByteLength(student._addr1) > 40 ? "_2" : "_1";
        svf.VrsOut("ADDR1" + addr1Field, student._addr1); //住所1
        String addr2Field = KNJ_EditEdit.getMS932ByteLength(student._addr2) > 50 ? "_3" : KNJ_EditEdit.getMS932ByteLength(student._addr2) > 40 ? "_2" : "_1";
        svf.VrsOut("ADDR2" + addr2Field, student._addr2); //住所2

        //保護者情報
        String gnameField = KNJ_EditEdit.getMS932ByteLength(student._name) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(student._name) > 20 ? "2" : "1";
        svf.VrsOut("GUARD_NAME" + gnameField, student._guard_name); //保護者氏名
        svf.VrsOut("GUARD_ZIPNO", student._guard_zipcd); //保護者郵便番号
        String gaddr1Field = KNJ_EditEdit.getMS932ByteLength(student._addr1) > 50 ? "_3" : KNJ_EditEdit.getMS932ByteLength(student._addr1) > 40 ? "_2" : "_1";
        svf.VrsOut("GUARD_ADDR1" + gaddr1Field, student._guard_addr1); //保護者住所1
        String gaddr2Field = KNJ_EditEdit.getMS932ByteLength(student._addr2) > 50 ? "_3" : KNJ_EditEdit.getMS932ByteLength(student._addr2) > 40 ? "_2" : "_1";
        svf.VrsOut("GUARD_ADDR2" + gaddr2Field, student._guard_addr2); //保護者住所2

    	//3学年分繰り返し
        int examLine = 1; //受験校の行
        final Map courseHopeHistMap = new TreeMap(); //進路希望調査
        for (Iterator it = student._studentHistList.iterator(); it.hasNext();) {
            final StudentHist studentHist = (StudentHist) it.next();

        	//各学年の進路希望調査を保持
            if(studentHist._courseHopeMap != null) {
                int grade = Integer.parseInt(_param._grade);
                int gradeHist = Integer.parseInt(studentHist._grade); //比較用学年
                //学年の判定
                if(grade == gradeHist) {
                	//現在学年の場合
                	courseHopeHistMap.put("3", studentHist._courseHopeMap);
                } else {
                	//現在学年でない場合
                	grade = grade - 1; //現在学年 - 1
                	if(grade == gradeHist) {
                    	courseHopeHistMap.put("2", studentHist._courseHopeMap);
                	} else {
                		//1年前でない場合
                		grade = grade - 1; //現在学年 - 2
                		if(grade == gradeHist) {
                			courseHopeHistMap.put("1", studentHist._courseHopeMap);
                		} else {
                			//現在含め過去3学年以外の場合
                			continue;
                		}
                	}
                }
            }

        	//受験校
            for (final Iterator itAft = studentHist._aftGradCourseMap.keySet().iterator(); itAft.hasNext();) {
                final String key = (String) itAft.next();
                final AftGradCourse aftGradCourse = (AftGradCourse) studentHist._aftGradCourseMap.get(key);
                if(aftGradCourse == null) continue;
                svf.VrsOutn("EXAM_HOPE_NAME", examLine, ""); //志望
                svf.VrsOutn("EXAM_COLLEGE_NAME", examLine, aftGradCourse._exam_college_name); //大学名
                svf.VrsOutn("EXAM_FACULTY_NAME", examLine, aftGradCourse._exam_faculty_name); //学部名
                svf.VrsOutn("EXAM_DEPARTMENT_NAME", examLine, aftGradCourse._exam_department_name); //学科名
                svf.VrsOutn("EXAM_FORM", examLine, aftGradCourse._exam_form); //形態
                svf.VrsOutn("JUDGE", examLine, aftGradCourse._judge); //合否
                svf.VrsOutn("ENT", examLine, aftGradCourse._ent); //入学
                examLine++;
            }
        }

        //進路希望調査
        int hopeLine = 1; //進路希望の行
        int maxLine = 3; //学年ごとの最大行
        for (final Iterator itHope = courseHopeHistMap.keySet().iterator(); itHope.hasNext();) {
            final String key = (String) itHope.next();
        	final Map map = (Map)courseHopeHistMap.get(key);
        	if(map == null) continue;
        	//進路希望調査
        	if(printCourseHope(svf, map, hopeLine, maxLine)) {
                //印字位置 1～3, 4～6, 7～13
                hopeLine = (hopeLine == 1) ? 4 : 7;
                maxLine = (hopeLine == 1) ? 6 : 13;
        	}
        }
    }

    //進路希望調査の印字
    private boolean printCourseHope(final Vrw32alp svf, final Map courseHopeMap, final int hopeLine, final int maxLine) {
    	boolean rtnFlg = false;
    	int line = hopeLine;
        for (final Iterator itAft = courseHopeMap.keySet().iterator(); itAft.hasNext();) {
        	if(line > maxLine) break;
            final String key = (String) itAft.next();
            final CourseHope courseHope = (CourseHope) courseHopeMap.get(key);
            final String hopeName = "第"+courseHope._hope_num;
            svf.VrsOutn("HOPE_NAME", line, hopeName); //志望
            svf.VrsOutn("HOPE_COLLEGE_NAME", line, courseHope._school_name); //大学名
            svf.VrsOutn("HOPE_FACULTY_NAME", line, courseHope._facultyname); //学部名
            svf.VrsOutn("HOPE_DEPARTMENT_NAME", line, courseHope._departmentname); //学科名
            svf.VrsOutn("HOPE_FORM", line, courseHope._howtoexam); //形態
            svf.VrsOutn("HOPE_BLINE", line, ""); //Bライン
            line++;
            rtnFlg = true;
        }
        return rtnFlg;
    }

    private List subclassListRemoveD026() {
        final List retList = new ArrayList(_param._subclassMstMap.values());
        for (final Iterator it = retList.iterator(); it.hasNext();) {
            final SubclassMst subclassMst = (SubclassMst) it.next();
            if (_param._d026List.contains(subclassMst._subclasscd)) {
                it.remove();
            }
            if (_param._isNoPrintMoto &&  subclassMst.isMoto() || !_param._isPrintSakiKamoku &&  subclassMst.isSaki()) {
                it.remove();
            }
        }
        return retList;
    }

    protected void VrsOutnRenban(final Vrw32alp svf, final String field, final String[] value) {
        if (null != value) {
            for (int i = 0 ; i < value.length; i++) {
                svf.VrsOutn(field, i + 1, value[i]);
            }
        }
    }

    // 出欠記録
    private void printAttend(final Vrw32alp svf, final StudentHist studentHist, final int gradeLine) {
    	//9学期の出欠記録
        final Attendance att = (Attendance) studentHist._attendMap.get(SEMEALL);
        String lesson  = "";
        String mlesson = "";
        String suspend = "";
        String absent  = "";
        String present = "";
        String late    = "";
        String early   = "";
        String abroad  = "";
        if (null != att) {
            lesson  = String.valueOf(att._lesson);
            suspend = String.valueOf(att._suspend + att._mourning);
            mlesson = String.valueOf(att._mLesson);
            absent  = String.valueOf(att._sick);
            present = String.valueOf(att._present);
            late    = String.valueOf(att._late);
            early   = String.valueOf(att._early);
            abroad  = String.valueOf(att._abroad);
        }
        svf.VrsOutn("LESSON", gradeLine, lesson);   // 授業日数
        svf.VrsOutn("SUSPEND", gradeLine, suspend); // 忌引出停日数
        svf.VrsOutn("ABROAD", gradeLine, abroad);   // 留学中の授業日数
        svf.VrsOutn("SICK", gradeLine, absent);     // 欠席日数
        svf.VrsOutn("ATTEND", gradeLine, present);  // 出席日数
        svf.VrsOutn("MUST", gradeLine, mlesson);    // 要出席日数
        svf.VrsOutn("LATE", gradeLine, late);       // 遅刻
        svf.VrsOutn("EARLY", gradeLine, early);     // 早退
    }

    // 出欠記録
    private void printHyouteiAvg(final DB2UDB db2, final Vrw32alp svf, final StudentHist studentHist, final int gradeLine) {
        //科目リスト
        _param.setSubclassMst(db2, studentHist._year);
        final List subclassList = subclassListRemoveD026();

        Map<String, String> classCdMap = new LinkedHashMap(); 
        for (Iterator itSubclass = subclassList.iterator(); itSubclass.hasNext();) {
        	final SubclassMst subclassMst = (SubclassMst) itSubclass.next();
            classCdMap.put(subclassMst._classcd, subclassMst._classabbv);
        }

        List<String> sortClassCdList = new ArrayList(classCdMap.keySet());
        Collections.sort(sortClassCdList);
        int idx = 1;
        for (String classCd : sortClassCdList) {
        	if (!studentHist._hyouteiAvgMap.containsKey(classCd)) continue;
        	final String hyouteiAvg = (String)studentHist._hyouteiAvgMap.get(classCd);
        	svf.VrsOutn("CLASS_AVE_NAME", idx, (String)classCdMap.get(classCd)); //教科名
            svf.VrsOutn("CLASS_AVE" + gradeLine, idx, hyouteiAvg);
            idx++;
        }

    }

    private int getSemeLine(final String semester) {
        final int line;
        if (SEMEALL.equals(semester)) {
            line = 4;
        } else {
            line = Integer.parseInt(semester);
        }
        return line;
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getStudentSql();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final Student student = new Student();
                student._schregno = rs.getString("SCHREGNO");
                student._name = rs.getString("NAME");
                student._schoolKind = rs.getString("SCHOOL_KIND");
                student._attendno = NumberUtils.isDigits(rs.getString("ATTENDNO")) ? String.valueOf(Integer.parseInt(rs.getString("ATTENDNO"))) + "番" : rs.getString("ATTENDNO");
                student._grade = rs.getString("GRADE");
                student._hrClass = rs.getString("HR_CLASS");
                student._zipcd = StringUtils.defaultString(rs.getString("ZIPCD"));
                student._addr1 = StringUtils.defaultString(rs.getString("ADDR1"));
                student._addr2 = StringUtils.defaultString(rs.getString("ADDR2"));
                student._guard_zipcd = StringUtils.defaultString(rs.getString("GUARD_ZIPCD"));
                student._guard_addr1 = StringUtils.defaultString(rs.getString("GUARD_ADDR1"));
                student._guard_addr2 = StringUtils.defaultString(rs.getString("GUARD_ADDR2"));
                student._guard_name = StringUtils.defaultString(rs.getString("GUARD_NAME"));
                student._studentHistList = student.setHistList(db2);
                retList.add(student);
            }
            
            //出欠情報をセット
            Attendance.load(db2, _param, retList);
            
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
        if ("1".equals(_param._disp)) {
            stb.append("    AND T1.GRADE || T1.HR_CLASS IN " + SQLUtils.whereIn(true, _param._categorySelected));
        } else {
            stb.append("    AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected));
        }

        stb.append("    ) ");
        //メイン表
        stb.append("     SELECT  REGD.SCHREGNO");
        stb.append("            ,REGD.SEMESTER ");
        stb.append("            ,BASE.NAME ");
        stb.append("            ,REGDG.SCHOOL_KIND ");
        stb.append("            ,REGD.ATTENDNO ");
        stb.append("            ,REGDG.GRADE ");
        stb.append("            ,REGD.HR_CLASS ");
        stb.append("            ,GADDR.GUARD_ZIPCD ");
        stb.append("            ,ADDR.ZIPCD ");
        stb.append("            ,ADDR.ADDR1 ");
        stb.append("            ,ADDR.ADDR2 ");
        stb.append("            ,GADDR.GUARD_ZIPCD ");
        stb.append("            ,GADDR.GUARD_ADDR1 ");
        stb.append("            ,GADDR.GUARD_ADDR2 ");
        stb.append("            ,GUARDIAN.GUARD_NAME ");
        stb.append("     FROM    SCHNO_A REGD ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = REGD.YEAR ");
        stb.append("                  AND REGDG.GRADE = REGD.GRADE ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR ");
        stb.append("                  AND REGDH.SEMESTER = REGD.SEMESTER ");
        stb.append("                  AND REGDH.GRADE = REGD.GRADE ");
        stb.append("                  AND REGDH.HR_CLASS = REGD.HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     LEFT JOIN (SELECT SCHREGNO, MAX(ISSUEDATE) AS ISSUEDATE FROM SCHREG_ADDRESS_DAT GROUP BY SCHREGNO) L_ADDR ");
        stb.append("            ON L_ADDR.SCHREGNO = REGD.SCHREGNO  ");
        stb.append("     LEFT JOIN SCHREG_ADDRESS_DAT ADDR ");
        stb.append("            ON ADDR.SCHREGNO  = L_ADDR.SCHREGNO ");
        stb.append("           AND ADDR.ISSUEDATE = L_ADDR.ISSUEDATE ");
        stb.append("     LEFT JOIN GUARDIAN_DAT GUARDIAN ");
        stb.append("            ON GUARDIAN.SCHREGNO = REGD.SCHREGNO  ");
        stb.append("     LEFT JOIN (SELECT SCHREGNO, MAX(ISSUEDATE) AS ISSUEDATE FROM GUARDIAN_ADDRESS_DAT GROUP BY SCHREGNO) L_GADDR ");
        stb.append("            ON L_GADDR.SCHREGNO = GUARDIAN.SCHREGNO  ");
        stb.append("     LEFT JOIN GUARDIAN_ADDRESS_DAT GADDR ");
        stb.append("            ON GADDR.SCHREGNO  = L_GADDR.SCHREGNO ");
        stb.append("           AND GADDR.ISSUEDATE = L_GADDR.ISSUEDATE ");
        stb.append("     WHERE   REGD.YEAR = '" + _param._loginYear + "' ");
        if (SEMEALL.equals(_param._semester)) {
            stb.append("     AND REGD.SEMESTER = '" + _param._loginSemester + "' ");
        } else {
            stb.append("     AND REGD.SEMESTER = '" + _param._semester + "' ");
        }
        if ("1".equals(_param._disp)) {
            stb.append("         AND REGD.GRADE || REGD.HR_CLASS IN " + SQLUtils.whereIn(true, _param._categorySelected));
        } else {
            stb.append("         AND REGD.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected));
        }
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

    //対象生徒の部活動を取得
    private String getSchregClub(final DB2UDB db2, final String year, final String schregno) {
    	String rtnStr = "";
    	String conect = "";

    	final StringBuffer stb = new StringBuffer();
        stb.append("  SELECT ");
        stb.append("    T1.SDATE, ");
        stb.append("    T1.CLUBCD, ");
        stb.append("    T2.CLUBNAME, ");
        stb.append("    CASE WHEN T1.SDATE BETWEEN TSEM.SDATE AND TSEM.EDATE OR "); 						 //1.入部日付が年度内
        stb.append("              T1.EDATE IS NOT NULL AND T1.EDATE BETWEEN TSEM.SDATE AND TSEM.EDATE OR "); //2.退部日付が年度内
        stb.append("              T1.EDATE IS NULL ");														 //3.退部日付がNULL(継続中)
        stb.append("              THEN 1 ELSE NULL END AS FLG ");
        stb.append("  FROM ");
        stb.append("    SCHREG_CLUB_HIST_DAT T1 ");
        stb.append("    INNER JOIN CLUB_MST T2 ");
        stb.append("      ON T2.SCHOOLCD = T1.SCHOOLCD ");
        stb.append("      AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("      AND T2.CLUBCD = T1.CLUBCD ");
        stb.append("    INNER JOIN SEMESTER_MST TSEM ");
        stb.append("      ON TSEM.YEAR = '" + year + "' ");
        stb.append("  	AND TSEM.SEMESTER = '9' ");
        stb.append("  WHERE ");
        stb.append("    T1.SCHREGNO = '" + schregno + "' ");
        stb.append("  ORDER BY ");
        stb.append("    T1.SDATE, ");
        stb.append("    T1.CLUBCD ");
        final String sql =  stb.toString();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
            	final String club = StringUtils.defaultString(rs.getString("CLUBNAME"));
            	final String syozokuFlg = StringUtils.defaultString(rs.getString("FLG"));
            	if(!"".equals(club) && "1".equals(syozokuFlg)) {
            		rtnStr = rtnStr + conect + club;
            		conect = ",";
            	}
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtnStr.toString();
    }

    //対象生徒の委員会・生徒会を取得
    private String getSchregCommittee(final DB2UDB db2, final String year, final String schregno) {
    	String rtnStr = "";
    	String conect = "";
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("   T1.SEMESTER, ");
        stb.append("   SEMES.SEMESTERNAME, ");
        stb.append("   T1.COMMITTEECD, ");
        stb.append("   T2.COMMITTEENAME, ");
        stb.append("   J002.NAME1 AS EXECUTIVE_NAME ");
        stb.append(" FROM ");
        stb.append("   SCHREG_COMMITTEE_HIST_DAT T1 ");
        stb.append("   INNER JOIN COMMITTEE_MST T2 ");
        stb.append("          ON T2.SCHOOLCD      = T1.SCHOOLCD ");
        stb.append("         AND T2.SCHOOL_KIND   = T1.SCHOOL_KIND ");
        stb.append("         AND T2.COMMITTEE_FLG = T1.COMMITTEE_FLG ");
        stb.append("         AND T2.COMMITTEECD   = T1.COMMITTEECD ");
        stb.append("   LEFT JOIN NAME_MST J002 ");
        stb.append("          ON J002.NAMECD1 = 'J002' ");
        stb.append("         AND J002.NAMECD2 = T1.EXECUTIVECD ");
        stb.append("   LEFT JOIN SEMESTER_MST SEMES ");
        stb.append("          ON SEMES.YEAR     = T1.YEAR ");
        stb.append("         AND SEMES.SEMESTER = T1.SEMESTER ");
        stb.append(" WHERE ");
        stb.append("       T1.YEAR     = '"+ year +"' ");
        stb.append("   AND T1.SCHREGNO = '"+ schregno +"' ");
        final String sql =  stb.toString();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
            	final String semester = StringUtils.defaultString(rs.getString("SEMESTER"));
            	final String semestername = StringUtils.defaultString(rs.getString("SEMESTERNAME"));
            	final String committee = StringUtils.defaultString(rs.getString("COMMITTEENAME"));
            	final String executive = StringUtils.defaultString(rs.getString("EXECUTIVE_NAME"));
            	if(!"".equals(committee)) {
            		//委員会 役職(学期)
            		final String addSemes = !"".equals(semestername) ? "("+ semestername + ")" : "";
            		final String val = ("".equals(executive)) ? committee  : committee + "　" + executive + addSemes;
            		rtnStr = rtnStr + conect + val;
            		conect = ",";
            	}
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtnStr.toString();
    }

    //対象生徒の資格情報を取得
    private String getSchregQualified(final DB2UDB db2, final String schregno) {
    	String rtnStr = "";
    	String conect = "";
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("   T1.REGDDATE, ");
        stb.append("   T1.SEQ, ");
        stb.append("   T1.QUALIFIED_CD, ");
        stb.append("   L1.QUALIFIED_NAME, ");
        stb.append("   T1.RANK, ");
        stb.append("   L3.NAME1 AS RANK_NAME ");
        stb.append(" FROM  ");
        stb.append("   SCHREG_QUALIFIED_HOBBY_DAT T1 ");
        stb.append("   LEFT JOIN QUALIFIED_MST L1 ");
        stb.append("          ON L1.QUALIFIED_CD = T1.QUALIFIED_CD ");
        stb.append("   LEFT JOIN NAME_MST L3  ");
        stb.append("          ON L3.NAMECD2 = T1.RANK ");
        stb.append("         AND L3.NAMECD1 = 'H312' ");
        stb.append(" WHERE  ");
        stb.append("   T1.SCHREGNO = '"+ schregno +"' ");
        stb.append(" ORDER BY T1.REGDDATE,T1.SUBCLASSCD,T1.SEQ ");
        final String sql =  stb.toString();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
            	final String qualified_name = StringUtils.defaultString(rs.getString("QUALIFIED_NAME"));
            	final String rank_name = StringUtils.defaultString(rs.getString("RANK_NAME"));
            	if(!"".equals(qualified_name)) {
            		//資格 級
            		final String val = ("".equals(rank_name)) ? qualified_name  : qualified_name + "　" + rank_name;
            		rtnStr = rtnStr + conect + val;
            		conect = ",";
            	}
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtnStr.toString();
    }

    private class Student {
        String _schregno;
        String _name;
        String _schoolKind;
        String _attendno;
        String _grade;
        String _hrClass;
        String _zipcd;
        String _addr1;
        String _addr2;
        String _guard_zipcd;
        String _guard_addr1;
        String _guard_addr2;
        String _guard_name;
        List _studentHistList;
        final Map _printMockMap = new TreeMap();
    	private Map _mockCdMap = new LinkedHashMap();
    	private Map _classCdMap = new LinkedHashMap();

        private List setHistList(final DB2UDB db2) {
            final List retList = new ArrayList();
            final String sql = studentHistSql();
            log.debug("hist sql" + sql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String year = StringUtils.defaultString(rs.getString("YEAR"));
                    final String semester = StringUtils.defaultString(rs.getString("SEMESTER"));
                    final String grade = StringUtils.defaultString(rs.getString("GRADE"));
                    final String hr_class = StringUtils.defaultString(rs.getString("HR_CLASS"));
                    final String attendno = String.valueOf(Integer.parseInt(rs.getString("ATTENDNO")));
                    final String grade_name= StringUtils.defaultString(rs.getString("GRADE_NAME2"));
                    final String schregno = StringUtils.defaultString(rs.getString("SCHREGNO"));
                    final String coursecd = StringUtils.defaultString(rs.getString("COURSECD"));
                    final String majorcd = StringUtils.defaultString(rs.getString("MAJORCD"));
                    final String coursecode = StringUtils.defaultString(rs.getString("COURSECODE"));
                    final StudentHist studentHist = new StudentHist(year, semester, grade, hr_class, attendno, grade_name, schregno, coursecd, majorcd, coursecode);
                    studentHist.setSubclass(db2);
                    studentHist.setCourseHopeMap(db2); //進路希望
                    studentHist.setAftGradCourseMap(db2); //受験校
                    retList.add(studentHist);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retList;
        }

        private String studentHistSql() {
            final StringBuffer stb = new StringBuffer();
            //現在学年は指示学期、過去学年は年度最終学期
            stb.append(" WITH YEAR_MAXSEMES AS ( ");
            stb.append(" SELECT ");
            stb.append("   YEAR, ");
            stb.append("   CASE WHEN YEAR = '"+ _param._loginYear +"' ");
            if (SEMEALL.equals(_param._semester)) {
                stb.append("        THEN '"+ _param._loginSemester +"'  ");
            } else {
                stb.append("        THEN '"+ _param._semester +"'  ");
            }
            stb.append("        ELSE MAX(SEMESTER)  ");
            stb.append("   END AS SEMESTER ");
            stb.append(" FROM  ");
            stb.append("   SEMESTER_MST ");
            stb.append(" WHERE  ");
            stb.append("   SEMESTER != '9' ");
            stb.append(" GROUP BY  ");
            stb.append("   YEAR ");
            //留年を考慮し、学年毎の最大年度
            stb.append(" ), SCH_MAXYEAR AS ( ");
            stb.append(" SELECT ");
            stb.append("   T1.SCHREGNO, ");
            stb.append("   T1.GRADE, ");
            stb.append("   MAX(T1.YEAR) AS YEAR ");
            stb.append(" FROM ");
            stb.append("   SCHREG_REGD_DAT T1 ");
            stb.append("   INNER JOIN SCHREG_REGD_GDAT T2 ");
            stb.append("           ON T2.YEAR        = T1.YEAR ");
            stb.append("          AND T2.GRADE       = T1.GRADE ");
            stb.append("          AND T2.SCHOOL_KIND = '" + _param._schoolKind + "' ");
            stb.append(" WHERE ");
            stb.append("   T1.SCHREGNO = '"+ _schregno +"' ");
            stb.append(" GROUP BY ");
            stb.append("   T1.GRADE, ");
            stb.append("   T1.SCHREGNO ");
            stb.append(" ORDER BY ");
            stb.append("   T1.SCHREGNO, ");
            stb.append("   T1.GRADE ");
            stb.append(" ) ");
            //メイン
            stb.append(" SELECT DISTINCT  ");
            stb.append("   T1.YEAR, ");
            stb.append("   T1.SEMESTER, ");
            stb.append("   T1.GRADE, ");
            stb.append("   T1.HR_CLASS, ");
            stb.append("   T1.ATTENDNO, ");
            stb.append("   GDAT.GRADE_NAME2, ");
            stb.append("   T1.SCHREGNO, ");
            stb.append("   T1.COURSECD, ");
            stb.append("   T1.MAJORCD, ");
            stb.append("   T1.COURSECODE ");
            stb.append(" FROM ");
            stb.append("   SCHREG_REGD_DAT T1 ");
            stb.append("   INNER JOIN YEAR_MAXSEMES T2 ");
            stb.append("           ON T2.YEAR     = T1.YEAR ");
            stb.append("          AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("   INNER JOIN SCH_MAXYEAR T3 ");
            stb.append("           ON T3.SCHREGNO = T1.SCHREGNO ");
            stb.append("          AND T3.YEAR     = T1.YEAR ");
            stb.append("          AND T3.GRADE    = T1.GRADE ");
            stb.append("   INNER JOIN SCHREG_REGD_GDAT GDAT ");
            stb.append("           ON GDAT.YEAR        = T1.YEAR ");
            stb.append("          AND GDAT.GRADE       = T1.GRADE ");
            stb.append("          AND GDAT.SCHOOL_KIND = '" + _param._schoolKind + "' ");
            stb.append(" ORDER BY ");
            stb.append("   YEAR DESC ");

            return stb.toString();
        }
    }

    private class StudentHist {
    	final String _year;
    	final String _semester;
        final String _grade;
        final String _hrClass;
        final String _attendno;
        final String _grade_name;
        final String _schregno;
        final String _coursecd;
        final String _majorcd;
        final String _coursecode;

        boolean _currentFlg = false;
        
        final Map _courseHopeMap = new TreeMap();
        final Map _aftGradCourseMap = new TreeMap();
        final Map _attendMap = new TreeMap();
        final Map _hyouteiAvgMap = new TreeMap();
        final Map _printSubclassMap = new TreeMap();
        final Map<String, Map<String, SubclassAttendance>> _attendSubClassMap = new HashMap();

        private StudentHist(
            	final String year,
            	final String semester,
                final String grade,
                final String hrClass,
                final String attendno,
                final String grade_name,
                final String schregno,
                final String coursecd,
                final String majorcd,
                final String coursecode
        ) {
        	_year = year;
        	_semester = semester;
            _grade = grade;
            _hrClass = hrClass;
            _attendno = attendno;
            _grade_name = grade_name;
            _schregno = schregno;
            _coursecd = coursecd;
            _majorcd = majorcd;
            _coursecode = coursecode;
            _currentFlg = (_year.equals(_param._loginYear)) ? true : false ;
        }

        private void setSubclass(final DB2UDB db2) {
            final String scoreSql = prestatementSubclass();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(scoreSql);
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String classcd = rs.getString("CLASSCD");
                    final String classname = rs.getString("CLASSNAME");
                    final String subclassname = rs.getString("SUBCLASSNAME");
                    final String credits = rs.getString("CREDITS");

                    String subclasscd = rs.getString("SUBCLASSCD");
                    if (!"999999".equals(subclasscd)) {
                        subclasscd = rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-" + rs.getString("CURRICULUM_CD") + "-" + rs.getString("SUBCLASSCD");
                    }

                    final Rank gradeRank = new Rank(rs.getString("GRADE_RANK"), rs.getString("GRADE_AVG_RANK"), rs.getString("GRADE_COUNT"), rs.getString("GRADE_AVG"), rs.getString("GRADE_HIGHSCORE"));
                    final Rank hrRank = new Rank(rs.getString("CLASS_RANK"), rs.getString("CLASS_AVG_RANK"), rs.getString("HR_COUNT"), rs.getString("HR_AVG"), rs.getString("HR_HIGHSCORE"));
                    final Rank courseRank = new Rank(rs.getString("COURSE_RANK"), rs.getString("COURSE_AVG_RANK"), rs.getString("COURSE_COUNT"), rs.getString("COURSE_AVG"), rs.getString("COURSE_HIGHSCORE"));
                    final Rank majorRank = new Rank(rs.getString("MAJOR_RANK"), rs.getString("MAJOR_AVG_RANK"), rs.getString("MAJOR_COUNT"), rs.getString("MAJOR_AVG"), rs.getString("MAJOR_HIGHSCORE"));

                    final String key = subclasscd;
                    if (!_printSubclassMap.containsKey(key)) {
                    	_printSubclassMap.put(key, new ScoreData(classcd, classname, subclasscd, subclassname, credits));
                    }
                    if (null == rs.getString("SEMESTER")) {
                    	continue;
                    }

                    final ScoreData scoreData = (ScoreData) _printSubclassMap.get(key);
                    final String testcd = rs.getString("SEMESTER") + rs.getString("TESTKINDCD") + rs.getString("TESTITEMCD") + rs.getString("SCORE_DIV");
                    scoreData._scoreMap.put(testcd, StringUtils.defaultString(rs.getString("SCORE")));
                	scoreData._avgMap.put(testcd, StringUtils.defaultString(rs.getString("AVG")));
                	scoreData._gradeRankMap.put(testcd, gradeRank);
                	scoreData._hrRankMap.put(testcd, hrRank);
                	scoreData._courceRankMap.put(testcd, courseRank);
                	scoreData._majorRankMap.put(testcd, majorRank);

                	//各教科評定平均
                	final String hyouteiAvg = rs.getString("HYOUTEI_HEIKIN");
                	_hyouteiAvgMap.put(classcd, hyouteiAvg);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
            	DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String prestatementSubclass() {
            final StringBuffer stb = new StringBuffer();

            final String sdiv1_1 = SDIV1010108; //1学期 中間
            final String sdiv1_2 = SDIV1990008; //1学期 期末
            final String sdiv2_1 = !_currentFlg || _param._semes2Flg ? SDIV2010108 : ""; //2学期 中間
            final String sdiv2_2 = !_currentFlg || _param._semes2Flg ? SDIV2990008 : ""; //2学期 期末
            final String sdiv3_2 = !_currentFlg || _param._semes3Flg ? SDIV3990008 : ""; //3学期 期末
            final String sdiv9_1 = !_currentFlg || _param._semes9Flg ? SDIV9990008 : ""; //3学期 期末
            final String sdiv9_2 = !_currentFlg || _param._semes9Flg ? SDIV9990009 : ""; //3学期 期末
            final String[] sdivs = {sdiv1_1, sdiv1_2, sdiv2_1, sdiv2_2, sdiv3_2, sdiv9_1, sdiv9_2};
            final StringBuffer divStr = divStr("", sdivs);
//            final StringBuffer divStrT5 = divStr("T5.", sdivs);

            stb.append(" WITH SCHNO AS( ");
            //学籍の表
            stb.append(" SELECT ");
            stb.append("     T2.YEAR, ");
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
            stb.append("         T2.YEAR    = '" + _year + "'  ");
            stb.append("     AND T2.GRADE    = '" + _grade + "'  ");
            stb.append("     AND T2.HR_CLASS = '" + _hrClass + "'  ");
            stb.append("     AND T2.SCHREGNO = '" + _schregno + "'  ");
            stb.append("     AND T2.SEMESTER = (SELECT ");
            stb.append("                          MAX(SEMESTER) ");
            stb.append("                        FROM ");
            stb.append("                          SCHREG_REGD_DAT W2 ");
            stb.append("                        WHERE ");
            stb.append("                          W2.YEAR = '" + _year + "'  ");
            stb.append("                          AND W2.SEMESTER <= '" + _semester + "'  ");
            stb.append("                          AND W2.SCHREGNO = T2.SCHREGNO ");
            stb.append("                     ) ");
            //成績明細データの表
            stb.append(" ) ,RECORD00 AS( ");
            stb.append("     SELECT DISTINCT ");
            stb.append("         T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHREGNO ");
            stb.append("     FROM RECORD_RANK_SDIV_DAT T1 ");
            stb.append("     INNER JOIN (SELECT ");
            stb.append("                  SCHREGNO ");
            stb.append("                FROM ");
            stb.append("                  SCHNO T2 ");
            stb.append("                GROUP BY ");
            stb.append("                  SCHREGNO ");
            stb.append("             ) T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _year + "'  ");
            stb.append("     AND T1.SUBCLASSCD NOT LIKE '50%' ");
            stb.append("     AND T1.SUBCLASSCD NOT IN ('" + ALL3 + "', '" + ALL5 + "') ");
            stb.append(" ) ,RECORD0 AS( ");
            stb.append("     SELECT ");
            stb.append("              T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHREGNO ");
            stb.append("            , L1.CREDITS ");
            stb.append("            , L2.SCORE ");
            stb.append("            , L2.AVG ");
            stb.append("            , L2.GRADE_RANK ");
            stb.append("            , L2.GRADE_AVG_RANK ");
            stb.append("            , L2.CLASS_RANK ");
            stb.append("            , L2.CLASS_AVG_RANK ");
            stb.append("            , L2.COURSE_RANK ");
            stb.append("            , L2.COURSE_AVG_RANK ");
            stb.append("            , L2.MAJOR_RANK ");
            stb.append("            , L2.MAJOR_AVG_RANK ");
            stb.append("            , T_AVG1.AVG AS GRADE_AVG ");
            stb.append("            , T_AVG1.COUNT AS GRADE_COUNT ");
            stb.append("            , T_AVG1.HIGHSCORE AS GRADE_HIGHSCORE ");
            stb.append("            , T_AVG2.AVG AS HR_AVG ");
            stb.append("            , T_AVG2.COUNT AS HR_COUNT ");
            stb.append("            , T_AVG2.HIGHSCORE AS HR_HIGHSCORE ");
            stb.append("            , T_AVG3.AVG AS COURSE_AVG ");
            stb.append("            , T_AVG3.COUNT AS COURSE_COUNT ");
            stb.append("            , T_AVG3.HIGHSCORE AS COURSE_HIGHSCORE ");
            stb.append("            , T_AVG4.AVG AS MAJOR_AVG ");
            stb.append("            , T_AVG4.COUNT AS MAJOR_COUNT ");
            stb.append("            , T_AVG4.HIGHSCORE AS MAJOR_HIGHSCORE ");
            stb.append("     FROM RECORD00 T1 ");
            stb.append("     INNER JOIN SCHNO T2 ");
            stb.append("             ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("     LEFT JOIN CREDIT_MST L1 ");
            stb.append("            ON L1.YEAR          = T2.YEAR ");
            stb.append("           AND L1.COURSECD      = T2.COURSECD ");
            stb.append("           AND L1.MAJORCD       = T2.MAJORCD ");
            stb.append("           AND L1.COURSECODE    = T2.COURSECODE ");
            stb.append("           AND L1.GRADE         = T2.GRADE ");
            stb.append("           AND L1.CLASSCD       = T1.CLASSCD ");
            stb.append("           AND L1.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("           AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("           AND L1.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT L2 ");
            stb.append("            ON L2.YEAR          = T1.YEAR ");
            stb.append("           AND L2.SEMESTER      = T1.SEMESTER ");
            stb.append("           AND L2.TESTKINDCD    = T1.TESTKINDCD ");
            stb.append("           AND L2.TESTITEMCD    = T1.TESTITEMCD ");
            stb.append("           AND L2.SCORE_DIV     = T1.SCORE_DIV ");
            stb.append("           AND L2.CLASSCD       = T1.CLASSCD ");
            stb.append("           AND L2.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("           AND L2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("           AND L2.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("           AND L2.SCHREGNO      = T1.SCHREGNO ");
            stb.append("     LEFT JOIN RECORD_AVERAGE_SDIV_DAT T_AVG1 ON T_AVG1.YEAR = T1.YEAR AND T_AVG1.SEMESTER = T1.SEMESTER AND T_AVG1.TESTKINDCD = T1.TESTKINDCD AND T_AVG1.TESTITEMCD = T1.TESTITEMCD AND T_AVG1.SCORE_DIV = T1.SCORE_DIV AND T_AVG1.GRADE = '" + _grade + "' AND T_AVG1.CLASSCD = T1.CLASSCD AND T_AVG1.SCHOOL_KIND = T1.SCHOOL_KIND AND T_AVG1.CURRICULUM_CD = T1.CURRICULUM_CD AND T_AVG1.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("           AND T_AVG1.AVG_DIV    = '1' "); //学年
            stb.append("     LEFT JOIN RECORD_AVERAGE_SDIV_DAT T_AVG2 ON T_AVG2.YEAR = T1.YEAR AND T_AVG2.SEMESTER = T1.SEMESTER AND T_AVG2.TESTKINDCD = T1.TESTKINDCD AND T_AVG2.TESTITEMCD = T1.TESTITEMCD AND T_AVG2.SCORE_DIV = T1.SCORE_DIV AND T_AVG2.GRADE = '" + _grade + "' AND T_AVG2.CLASSCD = T1.CLASSCD AND T_AVG2.SCHOOL_KIND = T1.SCHOOL_KIND AND T_AVG2.CURRICULUM_CD = T1.CURRICULUM_CD AND T_AVG2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("           AND T_AVG2.AVG_DIV    = '2' "); //クラス
            stb.append("           AND T_AVG2.HR_CLASS   = T2.HR_CLASS ");
            stb.append("     LEFT JOIN RECORD_AVERAGE_SDIV_DAT T_AVG3 ON T_AVG3.YEAR = T1.YEAR AND T_AVG3.SEMESTER = T1.SEMESTER AND T_AVG3.TESTKINDCD = T1.TESTKINDCD AND T_AVG3.TESTITEMCD = T1.TESTITEMCD AND T_AVG3.SCORE_DIV = T1.SCORE_DIV AND T_AVG3.GRADE = '" + _grade + "' AND T_AVG3.CLASSCD = T1.CLASSCD AND T_AVG3.SCHOOL_KIND = T1.SCHOOL_KIND AND T_AVG3.CURRICULUM_CD = T1.CURRICULUM_CD AND T_AVG3.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("           AND T_AVG3.AVG_DIV    = '3' ");
            stb.append("           AND T_AVG3.COURSECD   = T2.COURSECD ");
            stb.append("           AND T_AVG3.MAJORCD    = T2.MAJORCD ");
            stb.append("           AND T_AVG3.COURSECODE = T2.COURSECODE "); //コース
            stb.append("     LEFT JOIN RECORD_AVERAGE_SDIV_DAT T_AVG4 ON T_AVG4.YEAR = T1.YEAR AND T_AVG4.SEMESTER = T1.SEMESTER AND T_AVG4.TESTKINDCD = T1.TESTKINDCD AND T_AVG4.TESTITEMCD = T1.TESTITEMCD AND T_AVG4.SCORE_DIV = T1.SCORE_DIV AND T_AVG4.GRADE = '" + _grade + "' AND T_AVG4.CLASSCD = T1.CLASSCD AND T_AVG4.SCHOOL_KIND = T1.SCHOOL_KIND AND T_AVG4.CURRICULUM_CD = T1.CURRICULUM_CD AND T_AVG4.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("           AND T_AVG4.AVG_DIV    = '4' ");
            stb.append("           AND T_AVG4.COURSECD   = T2.COURSECD ");
            stb.append("           AND T_AVG4.MAJORCD    = T2.MAJORCD ");
            stb.append("           AND T_AVG4.COURSECODE = '0000' "); //専攻
            stb.append(" ) ,RECORD AS( ");
            stb.append("     SELECT ");
            stb.append("       T3.CLASSNAME, ");
            stb.append("       T4.SUBCLASSNAME, ");
            stb.append("       T1.* ");
            stb.append("     FROM RECORD0 T1 ");
            stb.append("     INNER JOIN CLASS_MST T3 ");
            stb.append("        ON T3.CLASSCD = SUBSTR(T1.SUBCLASSCD,1,2) ");
            stb.append("       AND T3.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("     INNER JOIN SUBCLASS_MST T4 ");
            stb.append("        ON T4.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("       AND T4.CLASSCD       = T1.CLASSCD ");
            stb.append("       AND T4.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("       AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("     INNER JOIN SUBCLASS_YDAT SUBY ");
            stb.append("        ON SUBY.YEAR          = '" + _year + "' ");
            stb.append("       AND SUBY.SUBCLASSCD    = T4.SUBCLASSCD ");
            stb.append("       AND SUBY.CLASSCD       = T4.CLASSCD ");
            stb.append("       AND SUBY.SCHOOL_KIND   = T4.SCHOOL_KIND ");
            stb.append("       AND SUBY.CURRICULUM_CD = T4.CURRICULUM_CD ");
            stb.append("   UNION ALL ");
            stb.append("     SELECT ");
            stb.append("       CAST(NULL AS VARCHAR(1)) AS CLASSNAME, ");
            stb.append("       CAST(NULL AS VARCHAR(1)) AS SUBCLASSNAME, ");
            stb.append("       T1.* ");
            stb.append("      FROM RECORD0 T1 ");
            stb.append("           LEFT JOIN ( SELECT T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV ");
            stb.append("                         FROM RECORD_RANK_SDIV_DAT T1 ");
            stb.append("                              INNER JOIN SCHREG_REGD_DAT REGD ");
            stb.append("                                      ON REGD.SCHREGNO = T1.SCHREGNO ");
            stb.append("                                     AND REGD.YEAR     = T1.YEAR ");
            stb.append("                                     AND REGD.SEMESTER = '" + _semester + "' ");
            stb.append("                        WHERE ");
            stb.append("                              T1.YEAR       = '" + _year + "' ");
            stb.append("                          AND T1.SUBCLASSCD = '" + ALL9 + "' ");
            stb.append("                        GROUP BY T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV  ");
            stb.append("                     ) T2");
            stb.append("                  ON T2.YEAR       = T1.YEAR ");
            stb.append("                 AND T2.SEMESTER   = T1.SEMESTER ");
            stb.append("                 AND T2.TESTKINDCD = T1.TESTKINDCD ");
            stb.append("                 AND T2.TESTITEMCD = T1.TESTITEMCD ");
            stb.append("                 AND T2.SCORE_DIV  = T1.SCORE_DIV ");
            stb.append("      WHERE T1.SUBCLASSCD = '" + ALL9 + "' ");
            stb.append(" ), HYOUTEI_AVG AS ( ");
            //各教科評定平均
            stb.append("  SELECT ");
            stb.append("    CLASSCD, DECIMAL(ROUND(FLOAT(SUM(VALUATION)) / COUNT(SUBCLASSCD), 1), 2, 1) AS HYOUTEI_HEIKIN ");
            stb.append("  FROM ");
            stb.append("    SCHREG_STUDYREC_DAT ");
            stb.append("  WHERE ");
            stb.append("    YEAR = '" + _year + "' ");
            stb.append("    AND SCHREGNO = '" + _schregno + "' ");
            stb.append("  GROUP BY ");
            stb.append("    CLASSCD ");
            stb.append(" )");

            stb.append(" SELECT ");
            stb.append("       T1.*, ");
            stb.append("       T2.HYOUTEI_HEIKIN ");
            stb.append(" FROM RECORD T1 ");
            stb.append(" LEFT JOIN HYOUTEI_AVG T2 ON T2.CLASSCD = T1.CLASSCD ");
            stb.append(" ORDER BY ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
            stb.append("     T1.SUBCLASSCD,  ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.TESTKINDCD, ");
            stb.append("     T1.TESTITEMCD, ");
            stb.append("     T1.SCORE_DIV  ");

            return stb.toString();
        }

        /**
         * 学期+テスト種別のWHERE句を作成
         * @param tab テーブル別名
         * @param sdivs 学期+テスト種別
         * @return 作成した文字列
         */
		private StringBuffer divStr(final String tab, final String[] sdivs) {
			final StringBuffer divStr = new StringBuffer();
            divStr.append(" ( ");
            String or = "";
            for (int i = 0; i < sdivs.length; i++) {
            	if("".equals(sdivs[i].toString())) continue;
            	final String semester = sdivs[i].substring(0, 1);
            	final String testkindcd = sdivs[i].substring(1, 3);
            	final String testitemcd = sdivs[i].substring(3, 5);
            	final String scorediv = sdivs[i].substring(5);
            	divStr.append(or).append(" " + tab + "SEMESTER = '" + semester + "' AND " + tab + "TESTKINDCD = '" + testkindcd + "' AND " + tab + "TESTITEMCD = '" + testitemcd + "' AND " + tab + "SCORE_DIV = '" + scorediv + "' ");
            	or = " OR ";
            }
            divStr.append(" ) ");
			return divStr;
		}

        private void setCourseHopeMap(final DB2UDB db2) {
            final String getCourseHopeSql = getCourseHopeSql(_year, _schregno);
            if (_param._isOutputDebug) {
            	log.fatal(" getCourseHopeSql = " + getCourseHopeSql);
            }
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(getCourseHopeSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                	final String entrydate = StringUtils.defaultString(rs.getString("ENTRYDATE"));
                	final String seq = StringUtils.defaultString(rs.getString("SEQ"));
                	final String schregno = StringUtils.defaultString(rs.getString("SCHREGNO"));
                	final String hope_num = StringUtils.defaultString(rs.getString("HOPE_NUM"));
                	final String school_cd = StringUtils.defaultString(rs.getString("SCHOOL_CD"));
                	final String school_name = StringUtils.defaultString(rs.getString("SCHOOL_NAME"));
                	final String facultycd = StringUtils.defaultString(rs.getString("FACULTYCD"));
                	final String facultyname = StringUtils.defaultString(rs.getString("FACULTYNAME"));
                	final String departmentcd = StringUtils.defaultString(rs.getString("DEPARTMENTCD"));
                	final String departmentname = StringUtils.defaultString(rs.getString("DEPARTMENTNAME"));
                	final String howtoexam = StringUtils.defaultString(rs.getString("HOWTOEXAM"));
                	final String howtoexamname = StringUtils.defaultString(rs.getString("HOWTOEXAMNAME"));
                	final CourseHope courseHope = new CourseHope(entrydate, seq, schregno, hope_num, school_cd, school_name, facultycd, facultyname, departmentcd, departmentname, howtoexam, howtoexamname);
                	_courseHopeMap.put(hope_num, courseHope);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
            	DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        //対象生徒の進路希望を取得
        private String getCourseHopeSql(final String year, final String schregno) {
            final StringBuffer stb = new StringBuffer();
            //年度の日付
            stb.append(" WITH SEMES AS ( ");
            stb.append(" SELECT ");
            stb.append("   YEAR, ");
            stb.append("   SDATE, ");
            stb.append("   EDATE ");
            stb.append(" FROM  ");
            stb.append("   SEMESTER_MST ");
            stb.append(" WHERE  ");
            stb.append("       YEAR     = '"+ year +"'  ");
            stb.append("   AND SEMESTER = '9' ");
            //年度内の最新日付
            stb.append(" ), SEMES2 AS( ");
            stb.append(" SELECT ");
            stb.append("   T1.SCHREGNO, ");
            stb.append("   MAX(T1.ENTRYDATE) AS ENTRYDATE, ");
            stb.append("   T1.COURSE_KIND ");
            stb.append(" FROM  ");
            stb.append("   COURSE_HOPE_DAT T1 ");
            stb.append("   INNER JOIN SEMES T2 ");
            stb.append("           ON T2.YEAR = T1.YEAR ");
            stb.append(" WHERE  ");
            stb.append("       T1.YEAR        = '"+ year +"' ");
            stb.append("   AND T1.SCHREGNO    = '"+ schregno +"' ");
            stb.append("   AND T1.COURSE_KIND = '1' "); //進路種別：1(進学)
            stb.append("   AND T1.ENTRYDATE BETWEEN T2.SDATE AND T2.EDATE ");
            stb.append(" GROUP BY T1.SCHREGNO, T1.COURSE_KIND ");
            //年度内の最新日付の最新シーケンス
            stb.append(" ), SEMES3 AS( ");
            stb.append(" SELECT ");
            stb.append("   T1.SCHREGNO, ");
            stb.append("   T1.ENTRYDATE, ");
            stb.append("   MAX(T1.SEQ) AS SEQ, ");
            stb.append("   T1.COURSE_KIND ");
            stb.append(" FROM  ");
            stb.append("   COURSE_HOPE_DAT T1 ");
            stb.append("   INNER JOIN SEMES2 T2 ");
            stb.append("           ON T2.SCHREGNO    = T1.SCHREGNO ");
            stb.append("          AND T2.ENTRYDATE   = T1.ENTRYDATE ");
            stb.append("          AND T2.COURSE_KIND = T1.COURSE_KIND ");
            stb.append(" GROUP BY T1.SCHREGNO, T1.ENTRYDATE, T1.COURSE_KIND ");
            //第1志望
            stb.append(" ), HOPE1 AS ( ");
            stb.append(" SELECT  ");
            stb.append("   T1.ENTRYDATE, ");
            stb.append("   T1.SEQ, ");
            stb.append("   T1.SCHREGNO, ");
            stb.append("   '1' AS HOPE_NUM, ");
            stb.append("   T1.SCHOOL_CD1 AS SCHOOL_CD, ");
            stb.append("   T1.FACULTYCD1 AS FACULTYCD, ");
            stb.append("   T1.DEPARTMENTCD1 AS DEPARTMENTCD, ");
            stb.append("   T1.HOWTOEXAM1 AS HOWTOEXAM ");
            stb.append(" FROM  ");
            stb.append("   COURSE_HOPE_DAT T1 ");
            stb.append("   INNER JOIN SEMES3 T2 ");
            stb.append("           ON T2.ENTRYDATE   = T1.ENTRYDATE ");
            stb.append("          AND T2.SEQ         = T1.SEQ ");
            stb.append("          AND T2.SCHREGNO    = T1.SCHREGNO ");
            stb.append("          AND T2.COURSE_KIND = T1.COURSE_KIND ");
            //第2志望
            stb.append(" ),  HOPE2 AS ( ");
            stb.append(" SELECT  ");
            stb.append("   T1.ENTRYDATE, ");
            stb.append("   T1.SEQ, ");
            stb.append("   T1.SCHREGNO, ");
            stb.append("   '2' AS HOPE_NUM, ");
            stb.append("   T1.SCHOOL_CD2 AS SCHOOL_CD, ");
            stb.append("   T1.FACULTYCD2 AS FACULTYCD, ");
            stb.append("   T1.DEPARTMENTCD2 AS DEPARTMENTCD, ");
            stb.append("   T1.HOWTOEXAM2 AS HOWTOEXAM ");
            stb.append(" FROM  ");
            stb.append("   COURSE_HOPE_DAT T1 ");
            stb.append("   INNER JOIN SEMES3 T2    ");
            stb.append("           ON T2.ENTRYDATE   = T1.ENTRYDATE ");
            stb.append("          AND T2.SEQ         = T1.SEQ ");
            stb.append("          AND T2.SCHREGNO    = T1.SCHREGNO ");
            stb.append("          AND T2.COURSE_KIND = T1.COURSE_KIND ");
            //第3志望から第6志望
            stb.append(" ),  HOPE3 AS ( ");
            stb.append(" SELECT  ");
            stb.append("   T2.ENTRYDATE, ");
            stb.append("   T2.SEQ, ");
            stb.append("   T2.SCHREGNO, ");
            stb.append("   T2.HOPE_NUM, ");
            stb.append("   T2.SCHOOL_CD AS SCHOOL_CD, ");
            stb.append("   T2.FACULTYCD AS FACULTYCD, ");
            stb.append("   T2.DEPARTMENTCD AS DEPARTMENTCD, ");
            stb.append("   T2.HOWTOEXAM AS HOWTOEXAM ");
            stb.append(" FROM  ");
            stb.append("   COURSE_HOPE_DAT T1 ");
            stb.append("   INNER JOIN COURSE_HOPE_DETAIL_DAT T2 ");
            stb.append("           ON T2.ENTRYDATE = T1.ENTRYDATE ");
            stb.append("          AND T2.SEQ       = T1.SEQ ");
            stb.append("          AND T2.SCHREGNO  = T1.SCHREGNO ");
            stb.append("   INNER JOIN SEMES3 T3 ");
            stb.append("           ON T3.ENTRYDATE   = T1.ENTRYDATE ");
            stb.append("          AND T3.SEQ         = T1.SEQ ");
            stb.append("          AND T3.SCHREGNO    = T1.SCHREGNO ");
            stb.append("          AND T3.COURSE_KIND = T1.COURSE_KIND ");
            //第1志望から第6志望
            stb.append(" ), COURSE_HOPE AS( ");
            stb.append(" SELECT * FROM HOPE1 ");
            stb.append(" UNION ");
            stb.append(" SELECT * FROM HOPE2 ");
            stb.append(" UNION ");
            stb.append(" SELECT * FROM HOPE3 ");
            stb.append(" ) ");
            //メイン
            stb.append(" SELECT  ");
            stb.append("   T1.ENTRYDATE, ");
            stb.append("   T1.SEQ, ");
            stb.append("   T1.SCHREGNO, ");
            stb.append("   HOPE_NUM, ");
            stb.append("   T1.SCHOOL_CD, ");
            stb.append("   T2.SCHOOL_NAME, ");
            stb.append("   T1.FACULTYCD, ");
            stb.append("   T3.FACULTYNAME, ");
            stb.append("   T1.DEPARTMENTCD, ");
            stb.append("   T4.DEPARTMENTNAME, ");
            stb.append("   T1.HOWTOEXAM, ");
            stb.append("   T5.NAME1 AS HOWTOEXAMNAME ");
            stb.append(" FROM  ");
            stb.append("   COURSE_HOPE T1 ");
            stb.append("   LEFT JOIN COLLEGE_MST T2  ");
            stb.append("          ON T2.SCHOOL_CD = T1.SCHOOL_CD ");
            stb.append("   LEFT JOIN COLLEGE_FACULTY_MST T3  ");
            stb.append("          ON T3.SCHOOL_CD = T1.SCHOOL_CD  ");
            stb.append("         AND T3.FACULTYCD = T1.FACULTYCD ");
            stb.append("   LEFT JOIN COLLEGE_DEPARTMENT_MST T4  ");
            stb.append("          ON T4.SCHOOL_CD    = T1.SCHOOL_CD  ");
            stb.append("         AND T4.FACULTYCD    = T1.FACULTYCD  ");
            stb.append("         AND T4.DEPARTMENTCD = T1.DEPARTMENTCD ");
            stb.append("   LEFT JOIN V_NAME_MST T5  ");
            stb.append("          ON T5.YEAR    = '"+ year +"' ");
            stb.append("         AND T5.NAMECD1 = 'E002' ");
            stb.append("         AND T5.NAMECD2 = T1.HOWTOEXAM ");
            stb.append(" ORDER BY HOPE_NUM ");
            return stb.toString();
        }

        private void setAftGradCourseMap(final DB2UDB db2) {
            final String getAftGradCourseSql = getAftGradCourseSql(_year, _schregno);
            if (_param._isOutputDebug) {
            	log.fatal(" getAftGradCourseSql = " + getAftGradCourseSql);
            }
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(getAftGradCourseSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                	final String schregno = StringUtils.defaultString(rs.getString("SCHREGNO"));
                	final String toroku_date = StringUtils.defaultString(rs.getString("TOROKU_DATE"));
                	final String seq = StringUtils.defaultString(rs.getString("SEQ"));
                	final String kindname = StringUtils.defaultString(rs.getString("KINDNAME"));
                	final String course_kind_name = StringUtils.defaultString(rs.getString("COURSE_KIND_NAME"));
                	final String exam_hope_name = StringUtils.defaultString(rs.getString("EXAM_HOPE_NAME"));
                	final String exam_college_namecd = StringUtils.defaultString(rs.getString("EXAM_COLLEGE_NAMECD"));
                	final String exam_college_name = StringUtils.defaultString(rs.getString("EXAM_COLLEGE_NAME"));
                	final String exam_faculty_namecd = StringUtils.defaultString(rs.getString("EXAM_FACULTY_NAMECD"));
                	final String exam_faculty_name = StringUtils.defaultString(rs.getString("EXAM_FACULTY_NAME"));
                	final String exam_department_namecd = StringUtils.defaultString(rs.getString("EXAM_DEPARTMENT_NAMECD"));
                	final String exam_department_name = StringUtils.defaultString(rs.getString("EXAM_DEPARTMENT_NAME"));
                	final String exam_formcd = StringUtils.defaultString(rs.getString("EXAM_FORMCD"));
                	final String exam_form = StringUtils.defaultString(rs.getString("EXAM_FORM"));
                	final String judgecd = StringUtils.defaultString(rs.getString("JUDGECD"));
                	final String judge = StringUtils.defaultString(rs.getString("JUDGE"));
                	final String entcd = StringUtils.defaultString(rs.getString("ENTCD"));
                	final String ent = StringUtils.defaultString(rs.getString("ENT"));
                	final AftGradCourse aftGradCourse = new AftGradCourse(schregno, toroku_date, seq, kindname, course_kind_name, exam_hope_name, exam_college_namecd, exam_college_name, exam_faculty_namecd, exam_faculty_name, exam_department_namecd, exam_department_name, exam_formcd, exam_form, judgecd, judge, entcd, ent);
                	final String key =toroku_date + seq;
                	_aftGradCourseMap.put(key, aftGradCourse);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
            	DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        //対象生徒の受験校を取得
        private String getAftGradCourseSql(final String year, final String schregno) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.TOROKU_DATE, ");
            stb.append("     T1.SEQ, ");
            stb.append("     '受験報告' AS KINDNAME, ");
            stb.append("     '進学' AS COURSE_KIND_NAME, ");
            stb.append("     '' AS EXAM_HOPE_NAME, ");
            stb.append("     T2.SCHOOL_CD AS EXAM_COLLEGE_NAMECD, ");
            stb.append("     T2.SCHOOL_NAME AS EXAM_COLLEGE_NAME, ");
            stb.append("     T3.FACULTYCD AS EXAM_FACULTY_NAMECD, ");
            stb.append("     T3.FACULTYNAME AS EXAM_FACULTY_NAME, ");
            stb.append("     T4.DEPARTMENTCD AS EXAM_DEPARTMENT_NAMECD, ");
            stb.append("     T4.DEPARTMENTNAME AS EXAM_DEPARTMENT_NAME, ");
            stb.append("     N1.NAMECD2 AS EXAM_FORMCD, ");
            stb.append("     N1.NAME1 AS EXAM_FORM, ");
            stb.append("     N2.NAMECD2 AS JUDGECD, ");
            stb.append("     N2.NAME1 AS JUDGE, ");
            stb.append("     N3.NAMECD2 AS ENTCD, ");
            stb.append("     N3.NAME1 AS ENT, ");
            stb.append("     T1.STAT_CD || '-' || T1.FACULTYCD || '-' || T1.DEPARTMENTCD AS SORT ");
            stb.append(" FROM ");
            stb.append("     AFT_GRAD_COURSE_DAT T1 ");
            stb.append("     LEFT JOIN COLLEGE_MST T2  ");
            stb.append("            ON T1.STAT_CD = T2.SCHOOL_CD ");
            stb.append("     LEFT JOIN COLLEGE_FACULTY_MST T3  ");
            stb.append("            ON T1.STAT_CD   = T3.SCHOOL_CD ");
            stb.append("           AND T1.FACULTYCD = T3.FACULTYCD ");
            stb.append("     LEFT JOIN COLLEGE_DEPARTMENT_MST T4  ");
            stb.append("            ON T1.STAT_CD       = T4.SCHOOL_CD ");
            stb.append("           AND T1.FACULTYCD     = T4.FACULTYCD ");
            stb.append("           AND T1.DEPARTMENTCD  = T4.DEPARTMENTCD ");
            stb.append("     LEFT JOIN NAME_MST N1  ");
            stb.append("            ON N1.NAMECD1 = 'E002' ");
            stb.append("           AND N1.NAMECD2 = T1.HOWTOEXAM ");
            stb.append("     LEFT JOIN NAME_MST N2  ");
            stb.append("            ON N2.NAMECD1 = 'E005' ");
            stb.append("           AND N2.NAMECD2 = T1.DECISION ");
            stb.append("     LEFT JOIN NAME_MST N3 ");
            stb.append("            ON N3.NAMECD1 = 'E006' ");
            stb.append("           AND N3.NAMECD2 = T1.PLANSTAT ");
            stb.append(" WHERE ");
            stb.append("       T1.YEAR     = '"+ year +"' ");
            stb.append("   AND T1.SCHREGNO = '"+ schregno +"' ");
            stb.append("   AND T1.TOROKU_DATE IS NOT NULL ");
            stb.append("   AND T1.SENKOU_KIND = '0' ");
            stb.append(" ORDER BY ");
            stb.append("     TOROKU_DATE DESC, ");
            stb.append("     SORT DESC, ");
            stb.append("     SEQ  DESC ");
            return stb.toString();
        }
    }

    private static class ScoreData {
        final String _classcd;
        final String _classname;
        final String _subclasscd;
        final String _subclassname;
        final String _credits;
        final Map _scoreMap = new HashMap(); // 得点
        final Map _avgMap = new HashMap(); // 平均点
        final Map _gradeRankMap = new HashMap(); // 学年順位
        final Map _hrRankMap = new HashMap(); // クラス順位
        final Map _courceRankMap = new HashMap(); // コース順位
        final Map _majorRankMap = new HashMap(); // 専攻順位
        final Map _hyouteiAvgMap = new HashMap(); // 評定平均

        private ScoreData(
                final String classcd,
                final String classname,
                final String subclasscd,
                final String subclassname,
                final String credits
        ) {
            _classcd = classcd;
            _classname = classname;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _credits = credits;
        }

		public String score(final String sdiv) {
			return StringUtils.defaultString((String) _scoreMap.get(sdiv), "");
		}

		public String avg(final String sdiv) {
			return StringUtils.defaultString((String) _avgMap.get(sdiv), "");
		}

		public String toString() {
			return "ScoreData(" + _subclasscd + ":" + _subclassname + ", scoreMap = " + _scoreMap + ")";
		}
    }

    private static class Rank {
        final String _rank;
        final String _avgRank;
        final String _count;
        final String _avg;
        final String _highscore;
        public Rank(final String rank, final String avgRank, final String count, final String avg, final String highscore) {
            _rank = rank;
            _avgRank = avgRank;
            _count = count;
            _avg = avg;
            _highscore = highscore;
        }
    }

    private static class CourseHope {
    	final String _entrydate;
    	final String _seq;
    	final String _schregno;
    	final String _hope_num;
    	final String _school_cd;
    	final String _school_name;
    	final String _facultycd;
    	final String _facultyname;
    	final String _departmentcd;
    	final String _departmentname;
    	final String _howtoexam;
    	final String _howtoexamname;

        private CourseHope(
        		final String entrydate,
        		final String seq,
        		final String schregno,
        		final String hope_num,
        		final String school_cd,
        		final String school_name,
        		final String facultycd,
        		final String facultyname,
        		final String departmentcd,
        		final String departmentname,
        		final String howtoexam,
        		final String howtoexamname
        ) {
        	_entrydate = entrydate;
        	_seq = seq;
        	_schregno = schregno;
        	_hope_num = hope_num;
        	_school_cd = school_cd;
        	_school_name = school_name;
        	_facultycd = facultycd;
        	_facultyname = facultyname;
        	_departmentcd = departmentcd;
        	_departmentname = departmentname;
        	_howtoexam = howtoexam;
        	_howtoexamname = howtoexamname;
        }
    }

    private static class AftGradCourse{
    	final String _schregno;
    	final String _toroku_date;
    	final String _seq;
    	final String _kindname;
    	final String _course_kind_name;
    	final String _exam_hope_name;
    	final String _exam_college_namecd;
    	final String _exam_college_name;
    	final String _exam_faculty_namecd;
    	final String _exam_faculty_name;
    	final String _exam_department_namecd;
    	final String _exam_department_name;
    	final String _exam_formcd;
    	final String _exam_form;
    	final String _judgecd;
    	final String _judge;
    	final String _entcd;
    	final String _ent;

        private AftGradCourse(
        		final String schregno,
        		final String toroku_date,
        		final String seq,
        		final String kindname,
        		final String course_kind_name,
        		final String exam_hope_name,
        		final String exam_college_namecd,
        		final String exam_college_name,
        		final String exam_faculty_namecd,
        		final String exam_faculty_name,
        		final String exam_department_namecd,
        		final String exam_department_name,
        		final String exam_formcd,
        		final String exam_form,
        		final String judgecd,
        		final String judge,
        		final String entcd,
        		final String ent
        ) {
        	_schregno = schregno;
        	_toroku_date = toroku_date;
        	_seq = seq;
        	_kindname = kindname;
        	_course_kind_name = course_kind_name;
        	_exam_hope_name = exam_hope_name;
        	_exam_college_namecd = exam_college_namecd;
        	_exam_college_name = exam_college_name;
        	_exam_faculty_namecd = exam_faculty_namecd;
        	_exam_faculty_name = exam_faculty_name;
        	_exam_department_namecd = exam_department_namecd;
        	_exam_department_name = exam_department_name;
        	_exam_formcd = exam_formcd;
        	_exam_form = exam_form;
        	_judgecd = judgecd;
        	_judge = judge;
        	_entcd = entcd;
        	_ent = ent;
        }
    }

    private static class Attendance {
        final int _lesson;
        final int _mLesson;
        final int _suspend;
        final int _mourning;
        final int _sick;
        final int _present;
        final int _late;
        final int _early;
        final int _abroad;
        Attendance(
                final int lesson,
                final int mLesson,
                final int suspend,
                final int mourning,
                final int sick,
                final int present,
                final int late,
                final int early,
                final int abroad
        ) {
            _lesson = lesson;
            _mLesson = mLesson;
            _suspend = suspend;
            _mourning = mourning;
            _sick = sick;
            _present = present;
            _late = late;
            _early = early;
            _abroad = abroad;
        }

        
        private static void load(
                final DB2UDB db2,
                final Param param,
                final List studentList
        ) {

        	PreparedStatement psAtSeme = null;
            ResultSet rsAtSeme = null;
            try {
                param._attendParamMap.put("grade", "?");

                //今年度学年
                String currentYear = param._loginYear;
                String currentGrade = param._grade;                
                //前年度学年
                String lastYear = String.valueOf(Integer.parseInt(param._loginYear) - 1);
                String lastYearGrade = String.format("%02d", Integer.parseInt(param._grade) - 1);                
                //全前年度学年
                String lastYear2 = String.valueOf(Integer.parseInt(param._loginYear) - 2);
                String lastYearGrade2 = String.format("%02d", Integer.parseInt(param._grade) - 2);
                
                Map<String, String> yearGradeMap = new LinkedHashMap();
                yearGradeMap.put(currentYear, currentGrade);
                yearGradeMap.put(lastYear, lastYearGrade);
                yearGradeMap.put(lastYear2, lastYearGrade2);
                
                Map<String, Attendance> yearSchregAttendanceMap = new LinkedHashMap();

                //年度・学年毎にSQLを実行(生徒単位だと遅くなるため)
                for (final String year : yearGradeMap.keySet()) {
                	final String grade = yearGradeMap.get(year);
                	
                	if ("00".equals(grade)) break; //前年度学年が存在しない場合 

	                final String sdate = param.getSemesterMst(db2, year, SEMEALL, "SDATE");
	                final String edate = param.getSemesterMst(db2, year, SEMEALL, "EDATE");
                    
                    final String sql = AttendAccumulate.getAttendSemesSql(
                            year,
                            SEMEALL,
                            sdate,
                            edate,
                            param._attendParamMap
                    );
                    psAtSeme = db2.prepareStatement(sql);
                    psAtSeme.setString(1, grade);
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
                                rsAtSeme.getInt("TRANSFER_DATE")
                        );
                        final String schregno = rsAtSeme.getString("SCHREGNO");
                        yearSchregAttendanceMap.put(schregno + "-" + year, attendance);
                    }
                }
                DbUtils.closeQuietly(rsAtSeme);

                //生徒の過去学年毎の出欠情報を取得
                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    //■出欠の記録
                    for (Iterator itHist = student._studentHistList.iterator(); itHist.hasNext();) {
                        final StudentHist studentHist = (StudentHist) itHist.next();
                            Attendance attendance = yearSchregAttendanceMap.get(studentHist._schregno + "-" + studentHist._year);
                            studentHist._attendMap.put("9", attendance);
                    }
                }

            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, psAtSeme, rsAtSeme);
                db2.commit();
            }
        }
    }

    private static class SubclassAttendance {
        final BigDecimal _lesson;
        final BigDecimal _attend;
        final BigDecimal _sick;
        final BigDecimal _late;
        final BigDecimal _early;
        boolean _isOver;

        public SubclassAttendance(final BigDecimal lesson, final BigDecimal attend, final BigDecimal sick, final BigDecimal late, final BigDecimal early) {
            _lesson = lesson;
            _attend = attend;
            _sick = sick;
            _late = late;
            _early = early;
        }

        public String toString() {
            return "SubclassAttendance(" + _sick == null ? null : sishaGonyu(_sick.toString())  + "/" + _lesson + ")";
        }

        private static void load(final DB2UDB db2,
                final Param param,
                final List studentList,
                final DateRange dateRange) {
//            log.info(" subclass attendance dateRange = " + dateRange);
//            if (null == dateRange || null == dateRange._sdate || null == dateRange._edate || dateRange._sdate.compareTo(param._date) > 0) {
//                return;
//            }

            for (final Iterator it2 = param._attendSemesterDetailList.iterator(); it2.hasNext();) {
            	final SemesterDetail semesDetail = (SemesterDetail) it2.next();
                if (null == semesDetail) {
                    continue;
                }

                PreparedStatement ps = null;
                ResultSet rs = null;
                try {
                    param._attendParamMap.put("schregno", "?");

                    for (final Iterator it3 = studentList.iterator(); it3.hasNext();) {
                        final Student student = (Student) it3.next();
                        for (Iterator itHist = student._studentHistList.iterator(); itHist.hasNext();) {
                            final StudentHist studentHist = (StudentHist) itHist.next();
                            Map setSubAttendMap = null;

                            for (final Iterator itSemes = param._semesterMap.keySet().iterator(); itSemes.hasNext();) {
                                final String semester = (String) itSemes.next();

                                final String year = studentHist._year;
                                final String sdate = param.getSemesterMst(db2, year, semester, "SDATE");
                                final String edate = param.getSemesterMst(db2, year, semester, "EDATE");
                                if("".equals(year) || "".equals(semester) || "".equals(sdate) || "".equals(edate)) continue;
                                final String sql = AttendAccumulate.getAttendSubclassSql(
                                        year,
                                        semester,
                                        sdate,
                                        edate,
                                        param._attendParamMap
                                );

                                ps = db2.prepareStatement(sql);
                                ps.setString(1, student._schregno);
                                rs = ps.executeQuery();

                                while (rs.next()) {
                                    if (!semester.equals(rs.getString("SEMESTER"))) {
                                        continue;
                                    }
                                    final String subclasscd = rs.getString("SUBCLASSCD");

                                    param.setSubclassMst(db2, studentHist._year);
                                    final SubclassMst mst = (SubclassMst) param._subclassMstMap.get(subclasscd);
                                    if (null == mst) {
                                        log.warn("no subclass : " + subclasscd);
                                        continue;
                                    }
                                    setSubAttendMap = new TreeMap();
                                    final int iclasscd = Integer.parseInt(subclasscd.substring(0, 2));
                                    //if ((Integer.parseInt(KNJDefineSchool.subject_D) <= iclasscd && iclasscd <= Integer.parseInt(KNJDefineSchool.subject_U) || iclasscd == Integer.parseInt(KNJDefineSchool.subject_T))) {
                                    if (Integer.parseInt(KNJDefineSchool.subject_D) <= iclasscd && rs.getBigDecimal("MLESSON").intValue() > 0) {

                                        final BigDecimal lesson = rs.getBigDecimal("MLESSON");
                                        final BigDecimal rawSick = rs.getBigDecimal("SICK1");
                                        final BigDecimal sick = rs.getBigDecimal("SICK2");
                                        final BigDecimal rawReplacedSick = rs.getBigDecimal("RAW_REPLACED_SICK");
                                        final BigDecimal replacedSick = rs.getBigDecimal("REPLACED_SICK");
                                        final BigDecimal late = rs.getBigDecimal("LATE");
                                        final BigDecimal early = rs.getBigDecimal("EARLY");

                                        final BigDecimal sick1 = mst.isSaki() ? rawReplacedSick : rawSick;
                                        final BigDecimal attend = lesson.subtract(null == sick1 ? BigDecimal.valueOf(0) : sick1);
                                        final BigDecimal sick2 = mst.isSaki() ? replacedSick : sick;

                                        final BigDecimal absenceHigh = rs.getBigDecimal("ABSENCE_HIGH");

                                        final SubclassAttendance subclassAttendance = new SubclassAttendance(lesson, attend, sick2, late, early);

                                        //欠課時数上限
                                        final Double absent = Double.valueOf(mst.isSaki() ? rs.getString("REPLACED_SICK"): rs.getString("SICK2"));
                                        subclassAttendance._isOver = subclassAttendance.judgeOver(absent, absenceHigh);

                                        if (studentHist._attendSubClassMap.containsKey(subclasscd)) {
                                            setSubAttendMap = (Map) studentHist._attendSubClassMap.get(subclasscd);
                                        } else {
                                            setSubAttendMap = new TreeMap();
                                        }
                                        setSubAttendMap.put(semester, subclassAttendance);
                                    }
                                    studentHist._attendSubClassMap.put(subclasscd, setSubAttendMap);
                                }

                                DbUtils.closeQuietly(rs);
                            }
                        }

                    }

                } catch (Exception e) {
                    log.fatal("exception!", e);
                } finally {
                    DbUtils.closeQuietly(ps);
                    db2.commit();
                }
            }
        }

        /**
         * 欠課時数超過ならTrueを戻します。
         * @param absent 欠課時数
         * @param absenceHigh 超過対象欠課時数（CREDIT_MST）
         * @return
         */
        private boolean judgeOver(final Double absent, final BigDecimal absenceHigh) {
            if (null == absent || null == absenceHigh) {
                return false;
            }
            if (0.1 > absent.floatValue() || 0.0 == absenceHigh.doubleValue()) {
                return false;
            }
            if (absenceHigh.doubleValue() < absent.doubleValue()) {
                return true;
            }
            return false;
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

    private static class SubclassMst implements Comparable<SubclassMst> {
        final String _specialDiv;
        final String _classcd;
        final String _subclasscd;
        final String _classabbv;
        final String _classname;
        final String _subclassname;
        final Integer _classShoworder3;
        final Integer _subclassShoworder3;
        final String _calculateCreditFlg;
        SubclassMst _combined = null;
        List<SubclassMst> _attendSubclassList = new ArrayList();
        public SubclassMst(final String specialDiv, final String classcd, final String subclasscd, final String classabbv, final String classname, final String subclassabbv, final String subclassname,
                final Integer classShoworder3,
                final Integer subclassShoworder3,
                final String calculateCreditFlg) {
            _specialDiv = specialDiv;
            _classcd = classcd;
            _subclasscd = subclasscd;
            _classabbv = classabbv;
            _classname = classname;
            _subclassname = subclassname;
            _classShoworder3 = classShoworder3;
            _subclassShoworder3 = subclassShoworder3;
            _calculateCreditFlg = calculateCreditFlg;
        }
        public boolean isMoto() {
        	return null != _combined;
        }
        public boolean isSaki() {
        	return !_attendSubclassList.isEmpty();
        }
        public int compareTo(final SubclassMst mst) {
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

    private static class SemesterDetail implements Comparable<SemesterDetail> {
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
        public int compareTo(final SemesterDetail sd) {
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

    /*
     * 模試用
     */
    private Map getMockClassMap(final DB2UDB db2, final Param param, final String schregno) {
    	final StringBuffer stb = new StringBuffer();
        stb.append(" WITH RANK_SUBCLASS AS ( ");
        stb.append("   SELECT ");
        stb.append("     MRANGE.YEAR, ");
        stb.append("     MRANGE.SCHREGNO, ");
        stb.append("     MRANGE.MOCK_SUBCLASS_CD ");
        stb.append("   FROM ");
        stb.append("     MOCK_RANK_RANGE_DAT MRANGE ");
        stb.append("   WHERE ");
//        stb.append("     MRANGE.YEAR = '" + param._loginYear + "' "); //過去に受けた模試も含める
        stb.append("     MRANGE.SCHREGNO = '" + schregno + "' ");
        stb.append("     AND MRANGE.RANK_RANGE = '1' ");
        stb.append("     AND MRANGE.RANK_DIV = '02' ");
        stb.append("     AND MRANGE.MOCKDIV = '1' ");
        stb.append("     AND MRANGE.MOCK_SUBCLASS_CD <> '999999' ");
        stb.append("   GROUP BY ");
        stb.append("     MRANGE.YEAR, ");
        stb.append("     MRANGE.SCHREGNO, ");
        stb.append("     MRANGE.MOCK_SUBCLASS_CD ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("   T1.YEAR, ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T3.CLASSCD, ");
        stb.append("   T3.CLASSABBV ");
        stb.append(" FROM ");
        stb.append("   RANK_SUBCLASS T1 ");
        stb.append("   LEFT JOIN MOCK_SUBCLASS_MST T2 ");
        stb.append("     ON T2.MOCK_SUBCLASS_CD = T1.MOCK_SUBCLASS_CD ");
        stb.append("   LEFT JOIN CLASS_MST T3 ");
        stb.append("     ON T3.CLASSCD = T2.CLASSCD ");
        stb.append("     AND T3.SCHOOL_KIND = '" + param._schoolKind + "' ");
        stb.append(" GROUP BY ");
        stb.append("   T1.YEAR, ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T3.CLASSCD, ");
        stb.append("   T3.CLASSABBV ");
        stb.append(" ORDER BY ");
        stb.append("   T1.YEAR, ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T3.CLASSCD ");

        PreparedStatement ps = null;
        ResultSet rs = null;

        Map mockSubclassMap = new LinkedHashMap();

        try {
        	log.debug(stb.toString());
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();

            while (rs.next()) {
            	final String classcd  	 	= rs.getString("CLASSCD");
            	final String classAbbv     = rs.getString("CLASSABBV");

            	mockSubclassMap.put(classcd, classAbbv);
            }
        } catch (SQLException e) {
            log.fatal("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        return mockSubclassMap;
    }

    private static class MockClass {
    	final String _classcd;
    	final String _classAbbv;

    	MockClass(String classcd, final String classAbbv) {
    		_classcd = classcd;
    		_classAbbv = classAbbv;
    	}
    }

    private Map getMockRankRangeMap(final DB2UDB db2, final Param param, final Student student) {
    	final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("   T1.YEAR, ");
        stb.append("   T1.MOCKCD, ");
        stb.append("   CASE WHEN T5.MOCK_SUBCLASS_CD IS NOT NULL THEN '99' ELSE T2.CLASSCD END AS CLASSCD, ");
        stb.append("   T1.MOCK_SUBCLASS_CD, ");
        stb.append("   T2.SUBCLASS_ABBV, ");
        stb.append("   T1.DEVIATION, ");
        stb.append("   T3.MOCKNAME1 ");
        stb.append(" FROM ");
        stb.append("   MOCK_RANK_RANGE_DAT T1 ");
        stb.append("   LEFT JOIN MOCK_SUBCLASS_MST T2 ");
        stb.append("     ON T2.MOCK_SUBCLASS_CD = T1.MOCK_SUBCLASS_CD ");
        stb.append("   LEFT JOIN MOCK_MST T3 ON ");
        stb.append("     T3.MOCKCD = T1.MOCKCD ");
        stb.append("   INNER JOIN ( SELECT DISTINCT YEAR,SCHREGNO, COURSECD, MAJORCD, COURSECODE FROM SCHREG_REGD_DAT WHERE SCHREGNO = '"+ student._schregno +"' ) T4");
        stb.append("           ON T4.YEAR     = T1.YEAR ");
        stb.append("          AND T4.SCHREGNO = T1.SCHREGNO ");
        stb.append("   LEFT JOIN MOCK_TOTAL_SUBCLASS_DAT T5");
        stb.append("           ON T5.YEAR       = T1.YEAR ");
        stb.append("          AND T5.MOCKCD     = T1.MOCKCD ");
        stb.append("          AND T5.COURSECD   = T4.COURSECD ");
        stb.append("          AND T5.MAJORCD    = T4.MAJORCD ");
        stb.append("          AND T5.COURSECODE = T4.COURSECODE ");
        stb.append("          AND T5.MOCK_SUBCLASS_CD = T1.MOCK_SUBCLASS_CD ");
        stb.append(" WHERE ");
        stb.append("       T1.SCHREGNO = '" + student._schregno + "' ");
        stb.append("   AND T1.RANK_RANGE = '1' ");
        stb.append("   AND T1.RANK_DIV = '02' ");
        stb.append("   AND T1.MOCKDIV = '1' ");
        stb.append("   AND T1.DEVIATION IS NOT NULL ");
        stb.append(" ORDER BY ");
        stb.append("   T3.MOSI_DATE DESC, ");
        stb.append("   MOCKCD DESC, ");
        stb.append("   CLASSCD, ");
        stb.append("   MOCK_SUBCLASS_CD ");

    	log.debug(stb.toString());
        PreparedStatement ps = null;
        ResultSet rs = null;

        Map mockRankRangeMap = new LinkedHashMap();

        try {
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();

            while (rs.next()) {
            	final String year = rs.getString("YEAR");
            	final String mockcd = rs.getString("MOCKCD");
            	final String classcd = rs.getString("CLASSCD");
            	final String mockSubcd = rs.getString("MOCK_SUBCLASS_CD");
            	final String subAbbv = rs.getString("SUBCLASS_ABBV");
            	final String deviation = rs.getString("DEVIATION");
            	final String mockname = rs.getString("MOCKNAME1");

            	if (classcd == null) continue; //MOCK_SUBCLASS_MSTでCLASSCDを未設定の場合は帳票に出さない
            	
            	final String mapkey = year + "-" + mockcd + "-" + classcd;

            	//生徒ごとの校外実力テストの縦横項目を取得
            	student._mockCdMap.put(year + "-" + mockcd, mockname);
            	student._classCdMap.put(classcd, "");
            	
            	if (!mockRankRangeMap.containsKey(mapkey)) {
            		mockRankRangeMap.put(mapkey, new ArrayList());
            	}
        		List list = (List)mockRankRangeMap.get(mapkey);
        		list.add(subAbbv + "_" + deviation);
            }
        } catch (SQLException e) {
            log.fatal("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        return mockRankRangeMap;

    }

    private static class MockRankRange {
    	final String _mockcd;
    	final String _mockSubcd;
    	final String _subclassAbbv;
    	final String _deviation;
    	final String _mockname;

    	MockRankRange(final String mockcd, final String mockSubcd, final String subclassAbbv, final String deviation, final String mockname) {
    		_mockcd = mockcd;
    		_mockSubcd = mockSubcd;
    		_subclassAbbv = subclassAbbv;
    		_deviation = deviation;
    		_mockname = mockname;
    	}
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
    	log.fatal("$Revision: 75748 $");
    	KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _loginYear;
        final String _loginSemester;
        final String _semester;
        final String _disp;
        final String _gradeHrClass;
        final String _grade;
        final String[] _categorySelected;
        final String _testCd;
//        final String _date;
        final String _prgid;

        final String _nendo;
        final boolean _semes2Flg;
        final boolean _semes3Flg;
        final boolean _semes9Flg;
        final String _schoolKind;
        final String _schoolKindName;
        private final Map _testItemMap;
        final String _semesSdate;
        final String _semesEdate;

        final Map _stampMap;

        private String _certifSchoolSchoolName;
        private String _certifSchoolJobName;
        private String _certifSchoolPrincipalName;

        /** 端数計算共通メソッド引数 */
        private final Map _attendParamMap;

        private final List _semesterList;
        private Map _semesterMap;
        private final Map _semesterDetailMap;
        private Map<String, SubclassMst> _subclassMstMap;
        private Map<String, Map<String, String>> _creditMstMap;
        private List _d026List = new ArrayList();
        private Map _attendRanges;

        final String _documentroot;
        final String _imagepath;
        final String _schoolLogoImagePath;
        final String _backSlashImagePath;
        final String _whiteSpaceImagePath;
        final String _schoolStampPath;

        private boolean _isNoPrintMoto;
        private boolean _isPrintSakiKamoku;

        private boolean _isOutputDebug;

        private final List _attendTestKindItemList;
        private final List _attendSemesterDetailList;


        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _loginYear = request.getParameter("YEAR");
            _loginSemester = request.getParameter("LOGIN_SEMESTER");
            _semester = request.getParameter("SEMESTER");

        	_disp = request.getParameter("DISP");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            if ("1".equals(_disp)) {
                _grade = request.getParameter("GRADE");
            } else {
                _grade = _gradeHrClass.substring(0, 2);
            }
            _testCd = request.getParameter("TESTCD");
//            _date = request.getParameter("DATE").replace('/', '-');
            _prgid = request.getParameter("PRGID");

            _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_loginYear)) + "年度";
            _semes2Flg = SEME2.equals(_semester) || SEME3.equals(_semester) || SEMEALL.equals(_semester) ? true : false;
            _semes3Flg = SEME3.equals(_semester) || SEMEALL.equals(_semester) ? true : false;
            _semes9Flg = SEMEALL.equals(_semester) ? true : false;
//            loadNameMstD026(db2);
//            loadNameMstD016(db2);
//            setPrintSakiKamoku(db2);
            _schoolKind = getSchoolKind(db2);
            _schoolKindName = getSchoolKindName(db2);
            _testItemMap = settestItemMap(db2);
            _semesSdate = getSemesterMst(db2, _loginYear, SEMEALL, "SDATE");
            _semesEdate = getSemesterMst(db2, _loginYear, _loginSemester, "EDATE");
            _stampMap = getStampNoMap(db2);

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
            // setSubclassMst(db2);
            setCreditMst(db2);

            _documentroot = request.getParameter("DOCUMENTROOT");
            final String sqlControlMst = " SELECT IMAGEPATH FROM CONTROL_MST WHERE CTRL_NO = '01' ";
            _imagepath = KnjDbUtils.getOne(KnjDbUtils.query(db2, sqlControlMst));
            _schoolLogoImagePath = getImageFilePath("SCHOOLLOGO.jpg");
            _backSlashImagePath = getImageFilePath("slash_bs.jpg");
            _whiteSpaceImagePath = getImageFilePath("whitespace.png");
            _schoolStampPath = getImageFilePath("SCHOOLSTAMP.bmp");

            _attendTestKindItemList = getTestKindItemList(db2, false, false);
            _attendSemesterDetailList = getAttendSemesterDetailList();

            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));
        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJD187N' AND NAME = '" + propName + "' "));
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

        private void setSubclassMst(
                final DB2UDB db2,
                final String year
        ) {
        	if("".equals(year)) return;
            loadNameMstD026(db2, year);
            loadNameMstD016(db2, year);
            setPrintSakiKamoku(db2, year);

            PreparedStatement ps = null;
            ResultSet rs = null;
            _subclassMstMap = new HashMap();
            try {
                String sql = "";
                sql += " SELECT ";
                sql += " VALUE(T2.SPECIALDIV, '0') AS SPECIALDIV, ";
                sql += " T1.CLASSCD, ";
                sql += " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
                sql += " T2.CLASSABBV, VALUE(T2.CLASSORDERNAME2, T2.CLASSNAME) AS CLASSNAME, T1.SUBCLASSABBV, VALUE(T1.SUBCLASSORDERNAME2, T1.SUBCLASSNAME) AS SUBCLASSNAME, ";
                sql += " COMB1.CALCULATE_CREDIT_FLG, ";
                sql += " VALUE(T2.SHOWORDER3, 999) AS CLASS_SHOWORDER3, ";
                sql += " VALUE(T1.SHOWORDER3, 999) AS SUBCLASS_SHOWORDER3, ";
                sql += " ATT1.COMBINED_CLASSCD || '-' || ATT1.COMBINED_SCHOOL_KIND || '-' || ATT1.COMBINED_CURRICULUM_CD || '-' || ATT1.COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD, ";
                sql += " COMB1.ATTEND_CLASSCD || '-' || COMB1.ATTEND_SCHOOL_KIND || '-' || COMB1.ATTEND_CURRICULUM_CD || '-' || COMB1.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD ";
                sql += " FROM SUBCLASS_MST T1 ";
                sql += " LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
                sql += " LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT ATT1 ON ATT1.YEAR = '" + year + "' AND ATT1.ATTEND_CLASSCD = T1.CLASSCD AND ATT1.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND AND ATT1.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD AND ATT1.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ";
                sql += " LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT COMB1 ON COMB1.YEAR = '" + year + "' AND COMB1.COMBINED_CLASSCD = T1.CLASSCD AND COMB1.COMBINED_SCHOOL_KIND = T1.SCHOOL_KIND AND COMB1.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD AND COMB1.COMBINED_SUBCLASSCD = T1.SUBCLASSCD ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                	if (!_subclassMstMap.containsKey(rs.getString("SUBCLASSCD"))) {
                		final SubclassMst mst = new SubclassMst(rs.getString("SPECIALDIV"), rs.getString("CLASSCD"), rs.getString("SUBCLASSCD"), rs.getString("CLASSABBV"), rs.getString("CLASSNAME"), rs.getString("SUBCLASSABBV"), rs.getString("SUBCLASSNAME"), new Integer(rs.getInt("CLASS_SHOWORDER3")), new Integer(rs.getInt("SUBCLASS_SHOWORDER3")), rs.getString("CALCULATE_CREDIT_FLG"));
                		_subclassMstMap.put(rs.getString("SUBCLASSCD"), mst);
                	}
                	final SubclassMst combined = _subclassMstMap.get(rs.getString("COMBINED_SUBCLASSCD"));
                	if (null != combined) {
                		final SubclassMst mst = _subclassMstMap.get(rs.getString("SUBCLASSCD"));
                		mst._combined = combined;
                	}
                	final SubclassMst attend = _subclassMstMap.get(rs.getString("ATTEND_SUBCLASSCD"));
                	if (null != attend) {
                		final SubclassMst mst = _subclassMstMap.get(rs.getString("SUBCLASSCD"));
                		mst._attendSubclassList.add(attend);
                	}
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getCredits(final StudentHist studentHist, final String subclasscd) {
        	final String regdKey = studentHist._coursecd + studentHist._majorcd + studentHist._grade + studentHist._coursecode;
        	final Map<String, String> subclasscdCreditMap = _creditMstMap.get(regdKey);
			if (null == subclasscdCreditMap) {
        		return null;
        	}
        	final String credits = subclasscdCreditMap.get(subclasscd);
        	if (!subclasscdCreditMap.containsKey(subclasscd)) {
        		log.info(" no credit_mst : " + subclasscd);
        	}
			return credits;
        }

        private void setCreditMst(
                final DB2UDB db2
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _creditMstMap = new HashMap();
            try {
                String sql = "";
                sql += " SELECT ";
                sql += " T1.COURSECD || T1.MAJORCD || T1.GRADE || T1.COURSECODE AS REGD_KEY, ";
                sql += " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
                sql += " T1.CREDITS ";
                sql += " FROM CREDIT_MST T1 ";
                sql += " WHERE YEAR = '" + _loginYear + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                	final String regdKey = rs.getString("REGD_KEY");
					if (!_creditMstMap.containsKey(regdKey)) {
                		_creditMstMap.put(regdKey, new TreeMap());
                	}
                	_creditMstMap.get(regdKey).put(rs.getString("SUBCLASSCD"), rs.getString("CREDITS"));
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getSchoolKind(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _loginYear + "' AND GRADE = '" + _grade + "' ");

            return KnjDbUtils.getOne(KnjDbUtils.query(db2, sql.toString()));
        }

        private String getSchoolKindName(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT ABBV1 FROM NAME_MST WHERE NAMECD1 = 'A023' AND NAME1 = '" + _schoolKind + "' ");
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, sql.toString()));
        }

        private Map settestItemMap(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map map = new TreeMap();
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV AS TESTITEM, TESTITEMNAME ");
                sql.append(" FROM TESTITEM_MST_COUNTFLG_NEW_SDIV ");
                sql.append(" WHERE YEAR = '" + _loginYear + "' ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String testitem = rs.getString("TESTITEM");
                    final String testitemname = StringUtils.defaultString(rs.getString("TESTITEMNAME"));
                    if (!map.containsKey(testitem)) {
                        map.put(testitem, testitemname);
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
            	DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return map;
        }

        private void setCertifSchoolDat(final DB2UDB db2) {
        	final String certifKindcd = "J".equals(_schoolKind) ? "103" : "104";
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK2, REMARK4, REMARK5 FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _loginYear + "' AND CERTIF_KINDCD = '"+ certifKindcd +"' ");
            log.debug("certif_school_dat sql = " + sql.toString());

            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql.toString()));

            _certifSchoolSchoolName = StringUtils.defaultString(KnjDbUtils.getString(row, "SCHOOL_NAME"));
            _certifSchoolJobName = StringUtils.defaultString(KnjDbUtils.getString(row, "JOB_NAME"), "校長");
            _certifSchoolPrincipalName = StringUtils.defaultString(KnjDbUtils.getString(row, "PRINCIPAL_NAME"));
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

        private void loadNameMstD016(final DB2UDB db2, final String year) {
            _isNoPrintMoto = false;
            final String sql = "SELECT NAMECD2, NAMESPARE1, NAMESPARE2 FROM V_NAME_MST WHERE YEAR = '" + year + "' AND NAMECD1 = 'D016' AND NAMECD2 = '01' ";
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql));
            if ("Y".equals(KnjDbUtils.getString(row, "NAMESPARE1"))) _isNoPrintMoto = true;
//            log.info("(名称マスタD016):元科目を表示しない = " + _isNoPrintMoto); TODO 元科目ログ
        }

        /**
         * 合併先科目を印刷するか
         */
        private void setPrintSakiKamoku(final DB2UDB db2, final String year) {
            // 初期値：印刷する
            _isPrintSakiKamoku = true;
            // 名称マスタ「D021」「01」から取得する
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, "SELECT NAMESPARE3 FROM V_NAME_MST WHERE YEAR='" + year + "' AND NAMECD1 = 'D021' AND NAMECD2 = '01' "));
            if ("Y".equals(KnjDbUtils.getString(row, "NAMESPARE3"))) {
                _isPrintSakiKamoku = false;
            }
//            log.debug("合併先科目を印刷するか：" + _isPrintSakiKamoku); TODO 合併先科目ログ
        }

        private void loadNameMstD026(final DB2UDB db2, final String year) {

            final StringBuffer sql = new StringBuffer();
                final String field = SEMEALL.equals(_semester) ? "NAMESPARE1" : "ABBV" + _semester;
                sql.append(" SELECT NAME1 AS SUBCLASSCD FROM V_NAME_MST ");
                sql.append(" WHERE YEAR = '" + year + "' AND NAMECD1 = 'D026' AND " + field + " = '1'  ");

            _d026List.clear();
            _d026List.addAll(KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, sql.toString()), "SUBCLASSCD"));
//            log.info("非表示科目:" + _d026List); TODO 非表示科目ログ
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
                stb.append(" FROM TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ");
                stb.append(" INNER JOIN ADMIN_CONTROL_SDIV_DAT T11 ON T11.YEAR = T1.YEAR ");
                stb.append("    AND T11.SEMESTER = T1.SEMESTER ");
                stb.append("    AND T11.TESTKINDCD = T1.TESTKINDCD ");
                stb.append("    AND T11.TESTITEMCD = T1.TESTITEMCD ");
                stb.append("    AND T11.SCORE_DIV = T1.SCORE_DIV ");
                stb.append(" LEFT JOIN SEMESTER_DETAIL_MST T2 ON T2.YEAR = T1.YEAR ");
                stb.append("    AND T2.SEMESTER = T1.SEMESTER ");
                stb.append("    AND T2.SEMESTER_DETAIL = T1.SEMESTER_DETAIL ");
                stb.append(" WHERE T1.YEAR = '" + _loginYear + "' ");
                stb.append("   AND T11.CLASSCD || '-' || T11.SCHOOL_KIND || '-' || T11.CURRICULUM_CD || '-' || T11.SUBCLASSCD IN "); // 指定の科目が登録されていれば登録された科目、登録されていなければ00-00-00-000000を使用する
                stb.append("    (SELECT MAX(SUBCLASSCD) FROM ADMIN_CONTROL_SDIV_SUBCLASSCD) ");
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

        //学期情報の取得
        private String getSemesterMst(DB2UDB db2, final String year, final String semester, final String column) {
        	if("".equals(year) || "".equals(semester)) return "";
            String rtnStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
            	final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("   * ");
                stb.append(" FROM ");
                stb.append("   SEMESTER_MST ");
                stb.append(" WHERE ");
                stb.append("       YEAR     = '"+ year +"' ");
                stb.append("   AND SEMESTER = '"+ semester +"' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtnStr = StringUtils.defaultString(rs.getString(column));
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtnStr;
        }

        public String getKindCd() {
            return null == _testCd ? null : _testCd.substring(0, 2);
        }

        public String getItemCd() {
            return null == _testCd ? null : _testCd.substring(2, 4);
        }

        public String getScoreDiv() {
            return null == _testCd ? null : _testCd.substring(4);
        }
    }
}

// eof
