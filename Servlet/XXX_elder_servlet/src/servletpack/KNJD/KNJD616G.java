// kanji=漢字
/*
 * $Id: b4ab5d0bf45ab074b4b517da3beacb97bd598172 $
 */
/* 一部の判定条件で入らない所があるが、これは、講座別コース別クラス別の帳票出力を想定して作成したが、その想定から外れて、講座別コース別帳票と講座別クラス別帳票を連続して出力する事になったからである。 */
/* こうなった経緯として、RECORD_AVERAGE_CHAIR_SDIV_DATに、(講座別コース別クラス別の)詳細な情報を持ち合わせていなかった事が判明したためである。  */
/* データの持ち方が変わって、RECORD_AVERAGE_CHAIR_SDIV_DATに、(講座別コース別クラス別の)詳細な情報を持つようになったら、下記の変更をすれば良い。 */
/*   ・データの取得SQL(getTotalScoreInfoSqlのSQL)の変更 */
/*   ・グローバル変数_currentPatternを_param._keyPatternに置き換えて、_currentPatternの宣言を削除 */
/*   ・printMain呼び出し処理を簡素化 */
/*   ・出力処理を取得したデータに合わせて出力する(formPrintを修正する) */
/* 仕様が変わってデータを保持する可能性もあるため、処理を維持して置いている。 */

package servletpack.KNJD;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
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
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 学校教育システム 賢者 [成績管理]  成績通知票
 */
public class KNJD616G {

    private static final Log log = LogFactory.getLog(KNJD616G.class);

    private static final String SEMEALL = "9";
    private static String MIDDLE_TEST_CD = "0101";
	private static String LAST_TEST_CD = "0201";
	private static String TERM_TEST_CD = "9900";

	private static String RAWSCORE_CD = "01";
	private static String NORM_CD = "02";
	private static String EXPECT_CD = "04";
	private static String SCORE_CD = "08";
	private static String GRADING_CD = "09";

    private boolean _hasData;
    Param _param;
    private String _currentPattern;

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {
        Vrw32alp svf = null;
        DB2UDB db2 = null;
        try {
            svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            response.setContentType("application/pdf");

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            // パラメータの取得
            _param = createParam(request, db2);

            if ("3".equals(_param._keyPattern)) {
                _currentPattern = "1";
            } else {
                _currentPattern = _param._keyPattern;
            }
            printMain(db2, svf);
            if ("3".equals(_param._keyPattern)) {
                _currentPattern = "2";
                printMain(db2, svf);
            }

        } catch (final Exception ex) {
            log.error("error! ", ex);
        } finally {
            if (null != db2) {
                try {
                    db2.commit();
                    db2.close();
                } catch (Exception ex) {
                    log.error("db close error!", ex);
                }
            }
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
            svf.VrQuit();
        }
    }


    private void printMain(
            final DB2UDB db2,
            final Vrw32alp svf
    ) {
    	final Map chairMap = getChairInfo(db2);
    	final Map scoreMap = getScoreInfo(db2);
    	final Map attendMap = loadSubclassAttend(db2);
    	final Map totalScoreMap = getTotalScoreInfo(db2);
    	getStudent(db2, chairMap, scoreMap, attendMap);
    	for (Iterator ite = chairMap.keySet().iterator();ite.hasNext();) {
            final String kStr = (String)ite.next();
            final ChairInfo cInfo = (ChairInfo)chairMap.get(kStr);
            //対象の講座は決まったので、後は利用Mapを判別して、下位に渡す。
            final Map useMap = cInfo.getStudentList();
            if ("3".equals(_currentPattern)) {  //※講座別コース別クラス別の出力 -> コース別、クラス別の帳票を別々に出力するよう上位で制限しているので、ここには入らない。理由は先頭に記載。
            	//パターン3はsubMap2重形式なので、別処理経由でmapFilterを呼び出す。
            	subMapFilter(db2, svf, cInfo, useMap, scoreMap, totalScoreMap);
            } else {
                //コース名別の出力についてはそのコース名称を、それ以外は出力するコース名を集約する。
            	mapFilter(db2, svf, cInfo, useMap, null, scoreMap, totalScoreMap);
            }
    	}
    }

    private void subMapFilter(
    		final DB2UDB db2,
            final Vrw32alp svf,
            final ChairInfo cInfo,
            final Map useMap,
            final Map scoreMap,
            final Map totalScoreMap
    ) {
    	//useMapがsumMapにくるまれているので、その単位で処理する。
    	if (useMap == null) return;
    	for (Iterator ite = useMap.keySet().iterator();ite.hasNext();) {
    		final String kStr = (String)ite.next();
    		final Map subMap = (Map)useMap.get(kStr);
    		mapFilter(db2, svf, cInfo, subMap, kStr, scoreMap, totalScoreMap);
    	}
    }

    private void mapFilter(
    		final DB2UDB db2,
            final Vrw32alp svf,
            final ChairInfo cInfo,
            final Map useMap,
            final String kStr,
            final Map scoreMap,
            final Map totalScoreMap
    		) {
    	if (useMap == null) return;
    	if ("3".equals(_currentPattern) || "1".equals(_currentPattern)) {//※講座別コース別クラス別の出力 -> コース別、クラス別の帳票を別々に出力するよう上位で制限しているので、"3"は無い。理由は先頭に記載。
    		for (Iterator ite1 = useMap.keySet().iterator();ite1.hasNext();) {
    			final String grHr = (String)ite1.next();
    			final Map subMap = (Map)useMap.get(grHr);
    			formPrint(db2, svf, cInfo, subMap, kStr, grHr, scoreMap, totalScoreMap);
    		}
    	} else if ("2".equals(_currentPattern)) {
    		for (Iterator ite2 = useMap.keySet().iterator();ite2.hasNext();) {
    			final String course = (String)ite2.next();
    			final Map subMap = (Map)useMap.get(course);
    			formPrint(db2, svf, cInfo, subMap, course, null, scoreMap, totalScoreMap);
    		}
    	} else {
			formPrint(db2, svf, cInfo, useMap, null, null, scoreMap, totalScoreMap);
    	}
    }

    private void formPrint(
    		final DB2UDB db2,
            final Vrw32alp svf,
            final ChairInfo cInfo,
            final Map useMap,
            final String courseCode,
            final String grHrcls,
            final Map scoreMap,
            final Map totalScoreMap
    ) {
        svf.VrSetForm("KNJD616G.frm", 1);
        final Map lessonMap = new LinkedMap();
		final int rowMax = 45;
		List subList = rebuildMap(useMap, rowMax, lessonMap);  //1ページ分で再構成する。
		if (subList == null) return;
		for (Iterator ite = subList.iterator();ite.hasNext();) {  //ページ単位のループ
			final Map putInfoMap = (Map)ite.next();
			setTitle(svf, cInfo, useMap, courseCode, grHrcls);
			String studentgrade = "";
			if (!"".equals(StringUtils.defaultString(grHrcls, ""))) {
				studentgrade = grHrcls.substring(2);
			}
			for (Iterator itr = _param._semesterMap.keySet().iterator();itr.hasNext();) {  //学期単位のループ
				final String semester = (String)itr.next();
				final List lessonList = (List)lessonMap.get(semester);
			    if (lessonList != null && lessonList.size() > 0) {
				    Collections.sort(lessonList);
			    }
				int schCnt = 0;
				for (Iterator its = putInfoMap.keySet().iterator();its.hasNext();) {  //生徒単位のループ
					final String kStr = (String)its.next();
					Student student = (Student)putInfoMap.get(kStr);
					schCnt++;
					if ("".equals(StringUtils.defaultString(grHrcls, ""))) {
						if (studentgrade.compareTo(student._grade) < 0) {
						    studentgrade = student._grade;
						}
					}
					Attendance attwk = null;
					if (student._attendSubclsMap != null && student._attendSubclsMap.containsKey(semester + ":" + _param._subclasscd)) {
					    attwk = (Attendance)student._attendSubclsMap.get(semester + ":" + _param._subclasscd);
					}
					if (schCnt == 1 && lessonList != null && lessonList.size() > 0) {  //先頭の生徒の時だけ
						//問題：講座内の出席すべき日数は、コース毎に変わらない？まとめて出力する時にどれ出すの？->MAXで。
						final Integer maxval = (Integer)lessonList.get(lessonList.size()-1);
                        svf.VrsOut("LESSON" + semester, String.valueOf(maxval));  //(科目別)授業日数
					}
					if ("1".equals(semester)) {
	                    //固定か所を最初だけ(1学期処理時に)出力
					    svf.VrsOutn("HR_NAME", schCnt, student._hr_Name);
					    svf.VrsOutn("NUMBER", schCnt, String.valueOf(Integer.parseInt(student._attendno)));
					    svf.VrsOutn("COURSE_ABBV", schCnt, student._coursecodeabbv1);
					    final int nlen = KNJ_EditEdit.getMS932ByteLength(student._name);
					    final String nfield = nlen > 30 ? "2" : "1";
					    svf.VrsOutn("NAME"+nfield, schCnt, student._name);
					}
					if (student._scoreMap != null && student._scoreMap.size() > 0) {
						if (!"9".equals(semester)) {
							if ("3".equals(semester)) {
								//※S見込点はSQL取得時にEXPECT_CDに強制変更しているので注意。
								//期末(考査/見込み)
								printScore(svf, student, semester, LAST_TEST_CD + RAWSCORE_CD, "SCORE" + semester + "_1", schCnt);  //考査
								printScore(svf, student, semester, LAST_TEST_CD + EXPECT_CD, "SCORE" + semester + "_2", schCnt);  //見込
								//他(平常点/学期得点/学期評価/欠課時数)
								printScore(svf, student, semester, TERM_TEST_CD + NORM_CD, "SCORE" + semester + "_3", schCnt);  //平常点
								printScore(svf, student, semester, TERM_TEST_CD + SCORE_CD, "SCORE" + semester + "_4", schCnt);  //学期得点
								printScore(svf, student, semester, TERM_TEST_CD + GRADING_CD, "GRADING" + semester, schCnt);       //学期評価
							} else {
								//中間(考査/見込)
								printScore(svf, student, semester, MIDDLE_TEST_CD + RAWSCORE_CD, "SCORE" + semester + "_1", schCnt);  //考査
								printScore(svf, student, semester, MIDDLE_TEST_CD + EXPECT_CD, "SCORE" + semester + "_2", schCnt);  //見込
								//期末(考査/見込み)
								printScore(svf, student, semester, LAST_TEST_CD + RAWSCORE_CD, "SCORE" + semester + "_3", schCnt);  //考査
								printScore(svf, student, semester, LAST_TEST_CD + EXPECT_CD, "SCORE" + semester + "_4", schCnt);  //見込
								//他(平常点/学期得点/学期評価/欠課時数)
								printScore(svf, student, semester, TERM_TEST_CD + NORM_CD, "SCORE" + semester + "_5", schCnt);  //平常点
								printScore(svf, student, semester, TERM_TEST_CD + SCORE_CD, "SCORE" + semester + "_6", schCnt);  //学期得点
								printScore(svf, student, semester, TERM_TEST_CD + GRADING_CD, "GRADING" + semester, schCnt);       //学期評価
							}
						} else {
							printScore(svf, student, semester, TERM_TEST_CD + SCORE_CD, "SCORE" + semester, schCnt);  //学年得点
							printScore(svf, student, semester, TERM_TEST_CD + GRADING_CD, "GRADING" + semester, schCnt);       //評定

						}
					}
					if (attwk != null) {
					    svf.VrsOutn("ABSENT" + semester, schCnt, String.valueOf(attwk._kekkaJisu));   //欠課時数
					}
				}
				if (totalScoreMap.size() > 0) {
					if (!"9".equals(semester)) {
						if ("3".equals(semester)) {
							//期末
							printTotalScore(svf, totalScoreMap, semester, LAST_TEST_CD + RAWSCORE_CD, "SCORE", "TOTAL_SCORE" + semester + "_3", 1, courseCode, grHrcls, studentgrade);
							printTotalScore(svf, totalScoreMap, semester, LAST_TEST_CD + RAWSCORE_CD, "COUNT", "TOTAL_SCORE" + semester + "_3", 2, courseCode, grHrcls, studentgrade);
							printTotalScore(svf, totalScoreMap, semester, LAST_TEST_CD + RAWSCORE_CD, "AVG", "TOTAL_SCORE" + semester + "_3", 3, courseCode, grHrcls, studentgrade);
							//平常点
							printTotalScore(svf, totalScoreMap, semester, TERM_TEST_CD + NORM_CD, "SCORE", "TOTAL_SCORE" + semester + "_5", 1, courseCode, grHrcls, studentgrade);
							printTotalScore(svf, totalScoreMap, semester, TERM_TEST_CD + NORM_CD, "COUNT", "TOTAL_SCORE" + semester + "_5", 2, courseCode, grHrcls, studentgrade);
							printTotalScore(svf, totalScoreMap, semester, TERM_TEST_CD + NORM_CD, "AVG", "TOTAL_SCORE" + semester + "_5", 3, courseCode, grHrcls, studentgrade);
							//学期得点
							printTotalScore(svf, totalScoreMap, semester, TERM_TEST_CD + SCORE_CD, "SCORE", "TOTAL_SCORE" + semester + "_6", 1, courseCode, grHrcls, studentgrade);
							printTotalScore(svf, totalScoreMap, semester, TERM_TEST_CD + SCORE_CD, "COUNT", "TOTAL_SCORE" + semester + "_6", 2, courseCode, grHrcls, studentgrade);
							printTotalScore(svf, totalScoreMap, semester, TERM_TEST_CD + SCORE_CD, "AVG", "TOTAL_SCORE" + semester + "_6", 3, courseCode, grHrcls, studentgrade);
							//学期評価
							printTotalScore(svf, totalScoreMap, semester, TERM_TEST_CD + GRADING_CD, "SCORE", "TOTAL_GRADING" + semester, 1, courseCode, grHrcls, studentgrade);
							printTotalScore(svf, totalScoreMap, semester, TERM_TEST_CD + GRADING_CD, "COUNT", "TOTAL_GRADING" + semester, 2, courseCode, grHrcls, studentgrade);
							printTotalScore(svf, totalScoreMap, semester, TERM_TEST_CD + GRADING_CD, "AVG", "TOTAL_GRADING" + semester, 3, courseCode, grHrcls, studentgrade);
						} else {
							//中間
							printTotalScore(svf, totalScoreMap, semester, MIDDLE_TEST_CD + RAWSCORE_CD, "SCORE", "TOTAL_SCORE" + semester + "_1", 1, courseCode, grHrcls, studentgrade);
							printTotalScore(svf, totalScoreMap, semester, MIDDLE_TEST_CD + RAWSCORE_CD, "COUNT", "TOTAL_SCORE" + semester + "_1", 2, courseCode, grHrcls, studentgrade);
							printTotalScore(svf, totalScoreMap, semester, MIDDLE_TEST_CD + RAWSCORE_CD, "AVG", "TOTAL_SCORE" + semester + "_1", 3, courseCode, grHrcls, studentgrade);
							//期末
							printTotalScore(svf, totalScoreMap, semester, LAST_TEST_CD + RAWSCORE_CD, "SCORE", "TOTAL_SCORE" + semester + "_2", 1, courseCode, grHrcls, studentgrade);
							printTotalScore(svf, totalScoreMap, semester, LAST_TEST_CD + RAWSCORE_CD, "COUNT", "TOTAL_SCORE" + semester + "_2", 2, courseCode, grHrcls, studentgrade);
							printTotalScore(svf, totalScoreMap, semester, LAST_TEST_CD + RAWSCORE_CD, "AVG", "TOTAL_SCORE" + semester + "_2", 3, courseCode, grHrcls, studentgrade);
							//平常点
							printTotalScore(svf, totalScoreMap, semester, TERM_TEST_CD + NORM_CD, "SCORE", "TOTAL_SCORE" + semester + "_3", 1, courseCode, grHrcls, studentgrade);
							printTotalScore(svf, totalScoreMap, semester, TERM_TEST_CD + NORM_CD, "COUNT", "TOTAL_SCORE" + semester + "_3", 2, courseCode, grHrcls, studentgrade);
							printTotalScore(svf, totalScoreMap, semester, TERM_TEST_CD + NORM_CD, "AVG", "TOTAL_SCORE" + semester + "_3", 3, courseCode, grHrcls, studentgrade);
							//学期得点
							printTotalScore(svf, totalScoreMap, semester, TERM_TEST_CD + SCORE_CD, "SCORE", "TOTAL_SCORE" + semester + "_4", 1, courseCode, grHrcls, studentgrade);
							printTotalScore(svf, totalScoreMap, semester, TERM_TEST_CD + SCORE_CD, "COUNT", "TOTAL_SCORE" + semester + "_4", 2, courseCode, grHrcls, studentgrade);
							printTotalScore(svf, totalScoreMap, semester, TERM_TEST_CD + SCORE_CD, "AVG", "TOTAL_SCORE" + semester + "_4", 3, courseCode, grHrcls, studentgrade);
							//学期評価
							printTotalScore(svf, totalScoreMap, semester, TERM_TEST_CD + GRADING_CD, "SCORE", "TOTAL_GRADING" + semester, 1, courseCode, grHrcls, studentgrade);
							printTotalScore(svf, totalScoreMap, semester, TERM_TEST_CD + GRADING_CD, "COUNT", "TOTAL_GRADING" + semester, 2, courseCode, grHrcls, studentgrade);
							printTotalScore(svf, totalScoreMap, semester, TERM_TEST_CD + GRADING_CD, "AVG", "TOTAL_GRADING" + semester, 3, courseCode, grHrcls, studentgrade);
						}
					} else {
						//学年得点
						printTotalScore(svf, totalScoreMap, semester, TERM_TEST_CD + SCORE_CD, "SCORE", "TOTAL_SCORE" + semester + "_6", 1, courseCode, grHrcls, studentgrade);
						printTotalScore(svf, totalScoreMap, semester, TERM_TEST_CD + SCORE_CD, "COUNT", "TOTAL_SCORE" + semester + "_6", 2, courseCode, grHrcls, studentgrade);
						printTotalScore(svf, totalScoreMap, semester, TERM_TEST_CD + SCORE_CD, "AVG", "TOTAL_SCORE" + semester + "_6", 3, courseCode, grHrcls, studentgrade);
						//評価
						printTotalScore(svf, totalScoreMap, semester, TERM_TEST_CD + GRADING_CD, "SCORE", "TOTAL_GRADING" + semester, 1, courseCode, grHrcls, studentgrade);
						printTotalScore(svf, totalScoreMap, semester, TERM_TEST_CD + GRADING_CD, "COUNT", "TOTAL_GRADING" + semester, 2, courseCode, grHrcls, studentgrade);
						printTotalScore(svf, totalScoreMap, semester, TERM_TEST_CD + GRADING_CD, "AVG", "TOTAL_GRADING" + semester, 3, courseCode, grHrcls, studentgrade);
					}
				}
			}
			_hasData = true;
			svf.VrEndPage();
		}
    }

    private void printScore(final Vrw32alp svf, final Student student, final String semester, final String searchCd, final String outField, final int gyo) {
		final String kStr = semester + "-" + searchCd;
		if (student._scoreMap.containsKey(kStr)) {
			ScoreInfo putwk = (ScoreInfo)student._scoreMap.get(kStr);
			if ("*".equals(putwk._value_Di)) {
				svf.VrsOutn(outField, gyo, "欠");
			} else {
				if (putwk._score != null) {
				    svf.VrsOutn(outField, gyo, putwk._score);
				}
			}
		}
    }

    private void printTotalScore(final Vrw32alp svf, final Map totalScoreMap, final String semester, final String searchCd, final String outType, final String outField, final int gyo, final String courseCode, final String grHrcls, final String studentGrade) {
		//下部の集計値
		final String kStr = semester + "-" + searchCd;
		if (totalScoreMap.containsKey(kStr)) {
			final Map subMap = (Map)totalScoreMap.get(kStr);
			String ssKey = "";
			if ("3".equals(_currentPattern)) {
				//※講座別コース別クラス別の出力 -> コース別、クラス別の帳票を別々に出力するよう上位で制限しているので、"3"は無い。理由は先頭に記載。
				//※処理を作成する際、他と違って2重マップになっている事に注意。
			} else if ("2".equals(_currentPattern)) {
			    ssKey = courseCode;
			} else if ("1".equals(_currentPattern)) {
			    ssKey = grHrcls;
			} else {
				ssKey = studentGrade;
			}
			if (!"".equals(ssKey) && subMap.containsKey(ssKey)) {
				DownTotalInfo putwk = (DownTotalInfo)subMap.get(ssKey);
				if ("SCORE".equals(outType) && putwk._score != null) {
				    svf.VrsOutn(outField, gyo, putwk._score);
				} else if ("COUNT".equals(outType) && putwk._count != null) {
				    svf.VrsOutn(outField, gyo, putwk._count);
				} else if ("AVG".equals(outType) && putwk._avg != null) {
				    svf.VrsOutn(outField, gyo, putwk._avg);
				}
			}
		}
    }

	private void setTitle(final Vrw32alp svf, final ChairInfo cInfo, final Map useMap, final String courseCode, final String grHrcls) {
		svf.VrsOut("year2", _param._year + "年度");
		svf.VrsOut("TITLE", "科目別成績一覧表");
		svf.VrsOut("CHAIR_NAME", cInfo._chairname);
		String majorName = "";
		String delim = "";
		List chkMajorCdList = new ArrayList();
		for (Iterator ite = useMap.keySet().iterator();ite.hasNext();) {
			final String kStr = (String)ite.next();
			Student student = (Student)useMap.get(kStr);
			if (!chkMajorCdList.contains(student._majorcd)) {
				chkMajorCdList.add(student._majorcd);
				majorName += delim + student._majorname;
				delim = "、";
			}
		}
		svf.VrsOut("COURSE_NAME", StringUtils.defaultString(_param._schKindName, "") + "　" + StringUtils.defaultString(majorName));
		svf.VrsOut("SUBCLASS_NAME", _param._subclsName);
		String staffNames = cInfo._staffname;
        delim = "、";
		for (Iterator itr = cInfo._subStaffList.iterator();itr.hasNext();) {
			final String sname = (String)itr.next();
			if (!"".equals(StringUtils.defaultString(sname, ""))) {
			    staffNames += delim + sname;
			}
		}
		final int trlen = KNJ_EditEdit.getMS932ByteLength(staffNames);
		final String trfield = trlen > 50 ? "3" : trlen > 40 ? "2" : "";
		svf.VrsOut("HR_TEACHER" + trfield, staffNames);
		svf.VrsOut("SCHOOL_NAME", _param._cerifSchoolName);
	}


    private Map getChairInfo(final DB2UDB db2) {
    	final Map retMap = new LinkedMap();
    	final String query = getChairInfoSql();
        PreparedStatement ps = null;
        try {
            ps = db2.prepareStatement(query);

            for (final Iterator rit = KnjDbUtils.query(db2, ps, null).iterator();rit.hasNext();) {
           	    final Map row = (Map) rit.next();

           	    final String chaircd = KnjDbUtils.getString(row, "CHAIRCD");
           	    final String chairname = KnjDbUtils.getString(row, "CHAIRNAME");
           	    final String chargediv = KnjDbUtils.getString(row, "CHARGEDIV");
           	    final String staffcd = KnjDbUtils.getString(row, "STAFFCD");
           	    final String staffname = KnjDbUtils.getString(row, "STAFFNAME");

           	    if (retMap.containsKey(chaircd)) {
           	    	final ChairInfo cinfo = (ChairInfo)retMap.get(chaircd);
           	    	cinfo._subStaffList.add(staffname);
           	    } else {
                    final ChairInfo cinfo = new ChairInfo(chaircd, chairname, chargediv, staffcd, staffname);

                  	//データの登録
                    retMap.put(chaircd, cinfo);
           	    }
            }
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            DbUtils.closeQuietly(ps);
        }

    	return retMap;
    }

    private String getChairInfoSql() {
    	final StringBuffer stb = new StringBuffer();
    	stb.append(" select DISTINCT ");
    	stb.append("   T1.CHAIRCD, ");
    	stb.append("   T1.CHAIRNAME, ");
    	stb.append("   T3.CHARGEDIV, ");
    	stb.append("   T3.STAFFCD, ");
    	stb.append("   T4.STAFFNAME ");
    	stb.append(" FROM ");
    	stb.append("   CHAIR_DAT T1 ");
    	stb.append("   LEFT JOIN CHAIR_STF_DAT T3 ");
    	stb.append("     ON T3.YEAR = T1.YEAR ");
    	stb.append("    AND T3.SEMESTER = T1.SEMESTER ");
    	stb.append("    AND T3.CHAIRCD = T1.CHAIRCD ");
    	stb.append("   LEFT JOIN STAFF_MST T4 ");
    	stb.append("     ON T4.STAFFCD = T3.STAFFCD ");
    	stb.append(" WHERE ");
    	stb.append("   T1.YEAR = '" + _param._year + "' ");
    	stb.append("   AND T1.SEMESTER = '" + _param._semester + "' ");
    	stb.append("   AND T1.CHAIRCD IN " + SQLUtils.whereIn(false, _param._categorySelected));
    	stb.append(" ORDER BY ");
    	stb.append("   CHAIRCD, ");
    	stb.append("   CHARGEDIV, ");
    	stb.append("   STAFFCD ");
    	return stb.toString();
    }

    private void getStudent(final DB2UDB db2, final Map chairMap, final Map scoreMap, final Map attendMap) {
    	ChairInfo ctlCls = null;
    	final String query = getStudentSql();
        PreparedStatement ps = null;
        try {
            ps = db2.prepareStatement(query);

            for (final Iterator rit = KnjDbUtils.query(db2, ps, null).iterator();rit.hasNext();) {
           	    final Map row = (Map) rit.next();

                final String chaircd = KnjDbUtils.getString(row, "CHAIRCD");
                final String coursecd = KnjDbUtils.getString(row, "COURSECD");
                final String majorcd = KnjDbUtils.getString(row, "MAJORCD");
                final String majorname = KnjDbUtils.getString(row, "MAJORNAME");
                final String coursecode = KnjDbUtils.getString(row, "COURSECODE");
                final String coursecodeabbv1 = KnjDbUtils.getString(row, "COURSECODEABBV1");
                final String grade = KnjDbUtils.getString(row, "GRADE");
                final String grade_Name1 = KnjDbUtils.getString(row, "GRADE_NAME1");
                final String hr_Class = KnjDbUtils.getString(row, "HR_CLASS");
                final String hr_Name = KnjDbUtils.getString(row, "HR_NAME");
                final String attendno = KnjDbUtils.getString(row, "ATTENDNO");
                final String schregno = KnjDbUtils.getString(row, "SCHREGNO");
                final String name = KnjDbUtils.getString(row, "NAME");

                final Student student = new Student(chaircd, coursecd, majorcd, majorname, coursecode, coursecodeabbv1, grade, grade_Name1, hr_Class, hr_Name, attendno, schregno, name);

                //成績データを紐づけ
                if (scoreMap.containsKey(schregno)) {
                	final Map ctlMap = (Map)scoreMap.get(schregno);
                	student._scoreMap = ctlMap;
                }
                if (attendMap.containsKey(schregno)) {
                	final Map ctlMap = (Map)attendMap.get(schregno);
                	student._attendSubclsMap = ctlMap;
                }
              	//講座クラスに紐づけ(ページ出力パターンに応じて設定)
                if (chairMap.containsKey(chaircd)) {
                	ctlCls = (ChairInfo)chairMap.get(chaircd);
                	ctlCls.addStudentList(coursecd, majorcd, coursecode, grade, hr_Class, student);
                }
            }
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            DbUtils.closeQuietly(ps);
        }

    	return;
    }

    private String getStudentSql() {
    	final StringBuffer stb = new StringBuffer();
    	stb.append(" select DISTINCT ");
    	stb.append("   T1.CHAIRCD, ");
    	stb.append("   T3.COURSECD, ");
    	stb.append("   T3.MAJORCD, ");
    	stb.append("   T7.MAJORNAME, ");
    	stb.append("   T3.COURSECODE, ");
    	stb.append("   T8.COURSECODEABBV1, ");
    	stb.append("   T3.GRADE, ");
    	stb.append("   T5.GRADE_NAME1, ");
    	stb.append("   T3.HR_CLASS, ");
    	stb.append("   T6.HR_NAME, ");
    	stb.append("   T3.ATTENDNO, ");
    	stb.append("   T2.SCHREGNO, ");
    	stb.append("   T4.NAME ");
    	stb.append(" FROM ");
    	stb.append("   CHAIR_DAT T1 ");
    	stb.append("   LEFT JOIN CHAIR_STD_DAT T2 ");
    	stb.append("     ON T2.YEAR = T1.YEAR ");
    	stb.append("    AND T2.SEMESTER = T1.SEMESTER ");
    	stb.append("    AND T2.CHAIRCD = T1.CHAIRCD ");
    	stb.append("   LEFT JOIN SCHREG_REGD_DAT T3 ");
    	stb.append("     ON T3.YEAR = T2.YEAR ");
    	stb.append("    AND T3.SEMESTER = T2.SEMESTER ");
    	stb.append("    AND T3.SCHREGNO = T2.SCHREGNO ");
    	stb.append("   INNER JOIN SCHREG_BASE_MST T4 ");
    	stb.append("     ON T4.SCHREGNO = T3.SCHREGNO ");
    	stb.append("   INNER JOIN SCHREG_REGD_GDAT T5 ");
    	stb.append("     ON T5.YEAR = T3.YEAR ");
    	stb.append("    AND T5.GRADE = T3.GRADE ");
    	stb.append("    AND T5.SCHOOL_KIND = '" + _param._schoolKind + "' ");
    	stb.append("   LEFT JOIN SCHREG_REGD_HDAT T6 ");
    	stb.append("     ON T6.YEAR = T3.YEAR ");
    	stb.append("    AND T6.SEMESTER = T3.SEMESTER ");
    	stb.append("    AND T6.GRADE = T3.GRADE ");
    	stb.append("    AND T6.HR_CLASS = T3.HR_CLASS ");
    	stb.append("   LEFT JOIN MAJOR_MST T7 ");
    	stb.append("     ON T7.COURSECD = T3.COURSECD ");
    	stb.append("    AND T7.MAJORCD = T3.MAJORCD ");
    	stb.append("   LEFT JOIN COURSECODE_MST T8 ");
    	stb.append("     ON T8.COURSECODE = T3.COURSECODE ");
    	stb.append(" WHERE ");
    	stb.append("   T1.YEAR = '" + _param._year + "' ");
    	stb.append("   AND T1.SEMESTER = '" + _param._semester + "' ");
    	stb.append("   AND T1.CHAIRCD IN " + SQLUtils.whereIn(false, _param._categorySelected));
    	stb.append("   AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '" + _param._subclasscd + "' ");
    	stb.append(" ORDER BY ");
    	stb.append("   T1.CHAIRCD, ");
    	if (!"1".equals(_param._keyTypeCourse) && "1".equals(_param._keyTypeClass)) {  //コースOFF、クラスON時のみ、ソートの優先順を変える。
    	    stb.append("   T3.GRADE, ");
    	    stb.append("   T3.HR_CLASS, ");
    	} else {
    	    stb.append("   T3.COURSECD, ");
    	    stb.append("   T3.MAJORCD, ");
    	    stb.append("   T3.COURSECODE, ");
    	    stb.append("   T3.GRADE, ");
    	    stb.append("   T3.HR_CLASS, ");
    	}
    	stb.append("   T3.ATTENDNO, ");
    	stb.append("   T2.SCHREGNO ");
    	return stb.toString();
    }

    private Map getScoreInfo(final DB2UDB db2) {
    	final Map retMap = new LinkedMap();
    	Map subMap = null;
    	final String query = getScoreInfoSql();
        PreparedStatement ps = null;
        try {
            ps = db2.prepareStatement(query);

            for (final Iterator rit = KnjDbUtils.query(db2, ps, null).iterator();rit.hasNext();) {
           	    final Map row = (Map) rit.next();

                final String year = KnjDbUtils.getString(row, "YEAR");
                final String semester = KnjDbUtils.getString(row, "SEMESTER");
                final String testkindcd = KnjDbUtils.getString(row, "TESTKINDCD");
                final String testitemcd = KnjDbUtils.getString(row, "TESTITEMCD");
                final String score_Div = KnjDbUtils.getString(row, "SCORE_DIV");
                final String classcd = KnjDbUtils.getString(row, "CLASSCD");
                final String school_Kind = KnjDbUtils.getString(row, "SCHOOL_KIND");
                final String curriculum_Cd = KnjDbUtils.getString(row, "CURRICULUM_CD");
                final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                final String schregno = KnjDbUtils.getString(row, "SCHREGNO");
                final String score = KnjDbUtils.getString(row, "SCORE");
                final String value_Di = KnjDbUtils.getString(row, "VALUE_DI");

                final ScoreInfo sinfo = new ScoreInfo(year, semester, testkindcd, testitemcd, score_Div, classcd, school_Kind, curriculum_Cd, subclasscd, schregno, score, value_Di);

              	//データの登録(学期:考査種別:科目コード:学籍番号)
                //※教科/科目を指示画面で指定しているので、教科/科目をkeyとしては利用しない。
                if (retMap.containsKey(schregno)) {
                	subMap = (Map)retMap.get(schregno);
                } else {
                	subMap = new LinkedMap();
                	retMap.put(schregno, subMap);
                }
                final String kStr = semester + "-" + testkindcd + testitemcd + score_Div;
                subMap.put(kStr, sinfo);
            }
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            DbUtils.closeQuietly(ps);
        }

    	return retMap;
    }

    private String getScoreInfoSql() {
    	final StringBuffer stb = new StringBuffer();
    	stb.append(" SELECT ");
    	stb.append("   YEAR, SEMESTER, TESTKINDCD, TESTITEMCD, SCORE_DIV, ");
    	stb.append("   CLASSCD,SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SCHREGNO, ");
    	stb.append("   0 AS SCORE, ");
    	stb.append("   VALUE_DI ");
    	stb.append(" FROM ");
    	stb.append("   RECORD_SCORE_DAT ");
    	stb.append(" WHERE ");
    	stb.append("   YEAR = '" + _param._year + "' ");
    	stb.append("   AND TESTKINDCD || TESTITEMCD || SCORE_DIV IN ('" + MIDDLE_TEST_CD + RAWSCORE_CD + "', '" + LAST_TEST_CD + RAWSCORE_CD + "', '" + TERM_TEST_CD + NORM_CD + "', '" + TERM_TEST_CD + SCORE_CD + "', '" + TERM_TEST_CD + GRADING_CD + "') ");
    	stb.append("   AND CHAIRCD IN " + SQLUtils.whereIn(false, _param._categorySelected));
    	stb.append("   AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '" + _param._subclasscd + "' ");
    	stb.append("   AND VALUE_DI = '*' ");
    	stb.append(" UNION ALL ");
    	stb.append(" SELECT ");
    	stb.append("   YEAR, SEMESTER, TESTKINDCD, TESTITEMCD, '" + EXPECT_CD + "' AS SCORE_DIV, ");    //※注意：元仕様が、見込みはRECORD_SCORE_DATの0101'04'、0201'04'で取得となっていたが、RECORD_SLUMP_SDIV_DATに変わったので、そちらに沿う形にここで合わせている。
    	stb.append("   CLASSCD,SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SCHREGNO, ");
    	stb.append("   SCORE, ");
    	stb.append("   '' AS VALUE_DI ");
    	stb.append(" FROM ");
    	stb.append("   RECORD_SLUMP_SDIV_DAT ");
    	stb.append(" WHERE ");
    	stb.append("   YEAR = '" + _param._year + "' ");
    	stb.append("   AND TESTKINDCD || TESTITEMCD || SCORE_DIV IN ('" + MIDDLE_TEST_CD + RAWSCORE_CD + "', '" + LAST_TEST_CD + RAWSCORE_CD + "') ");  //※010101, 020101で取得。
    	stb.append("   AND CHAIRCD IN " + SQLUtils.whereIn(false, _param._categorySelected));
    	stb.append("   AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '" + _param._subclasscd + "' ");
    	stb.append("   AND SCORE IS NOT NULL ");
    	stb.append(" UNION ALL ");
    	stb.append(" SELECT ");
    	stb.append("   YEAR, SEMESTER, TESTKINDCD, TESTITEMCD, SCORE_DIV, ");
    	stb.append("   CLASSCD,SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SCHREGNO, ");
    	stb.append("   SCORE, ");
    	stb.append("   NULL AS VALUE_DI ");
    	stb.append(" FROM ");
    	stb.append("   RECORD_RANK_SDIV_DAT ");
    	stb.append(" WHERE ");
    	stb.append("   YEAR = '" + _param._year + "' ");
    	stb.append("   AND TESTKINDCD || TESTITEMCD || SCORE_DIV IN ('" + MIDDLE_TEST_CD + RAWSCORE_CD + "', '" + MIDDLE_TEST_CD + EXPECT_CD + "','" + LAST_TEST_CD + RAWSCORE_CD + "', '" + LAST_TEST_CD + EXPECT_CD + "', '" + TERM_TEST_CD + NORM_CD + "', '" + TERM_TEST_CD + SCORE_CD + "', '" + TERM_TEST_CD + GRADING_CD + "') ");
    	stb.append("   AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '" + _param._subclasscd + "' ");
    	stb.append(" ORDER BY ");
    	stb.append(" SEMESTER,TESTKINDCD,TESTITEMCD,SCORE_DIV,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,SCHREGNO ");
    	return stb.toString();
    }


    private Map getTotalScoreInfo(final DB2UDB db2) {
    	final Map retMap = new LinkedMap();
    	Map subMap = null;
    	final String query = getTotalScoreInfoSql();
        PreparedStatement ps = null;
        try {
            ps = db2.prepareStatement(query);

            for (final Iterator rit = KnjDbUtils.query(db2, ps, null).iterator();rit.hasNext();) {
           	    final Map row = (Map) rit.next();
              	final String semester = KnjDbUtils.getString(row, "SEMESTER");
                final String testkindcd = KnjDbUtils.getString(row, "TESTKINDCD");
                final String testitemcd = KnjDbUtils.getString(row, "TESTITEMCD");
                final String score_Div = KnjDbUtils.getString(row, "SCORE_DIV");
                final String grade = KnjDbUtils.getString(row, "GRADE");
                final String hr_Class = KnjDbUtils.getString(row, "HR_CLASS");
                final String coursecd = KnjDbUtils.getString(row, "COURSECD");
                final String majorcd = KnjDbUtils.getString(row, "MAJORCD");
                final String coursecode = KnjDbUtils.getString(row, "COURSECODE");
                final String score = KnjDbUtils.getString(row, "SCORE");
                final String count = KnjDbUtils.getString(row, "COUNT");
                final String avg = KnjDbUtils.getString(row, "AVG");

                final DownTotalInfo sinfo = new DownTotalInfo(semester, testkindcd, testitemcd, score_Div, grade, hr_Class, coursecd, majorcd, coursecode, score, count, avg);

              	//データの登録(学期:考査種別:科目コード:学籍番号)
                //※教科/科目を指示画面で指定しているので、教科/科目をkeyとしては利用しない。
                final String sKey = semester + "-" +  testkindcd + testitemcd + score_Div;
                if (retMap.containsKey(sKey)) {
                	subMap = (Map)retMap.get(sKey);
                } else {
                	subMap = new LinkedMap();
                	retMap.put(sKey, subMap);
                }
                String kStr = "";
                if ("3".equals(_currentPattern)) {
            		//※講座別コース別クラス別の出力 -> コース別、クラス別の帳票を別々に出力するよう上位で制限しているので、ここには入らない。理由は先頭に記載。
                } else if ("2".equals(_currentPattern)) {
                	kStr = coursecd + majorcd + coursecode;
                } else if ("1".equals(_currentPattern)) {
                	kStr = grade + hr_Class;
                } else {
                	kStr = grade;
                }
                subMap.put(kStr, sinfo);
            }
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            DbUtils.closeQuietly(ps);
        }

    	return retMap;
    }

    private String getTotalScoreInfoSql() {
    	final StringBuffer stb = new StringBuffer();
    	stb.append(" SELECT ");
    	stb.append("   SEMESTER, ");
    	stb.append("   TESTKINDCD, ");
    	stb.append("   TESTITEMCD, ");
    	stb.append("   SCORE_DIV, ");
    	stb.append("   GRADE, ");
    	stb.append("   HR_CLASS, ");
    	stb.append("   COURSECD, ");
    	stb.append("   MAJORCD, ");
    	stb.append("   COURSECODE, ");
    	stb.append("   SCORE, ");
    	stb.append("   COUNT, ");
    	stb.append("   DECIMAL(INT(AVG*10.0+0.5)/10.0, 4, 1) AS AVG ");
    	stb.append(" FROM ");
    	stb.append("   RECORD_AVERAGE_CHAIR_SDIV_DAT ");
    	stb.append(" WHERE ");
    	stb.append("   YEAR = '" + _param._year + "' ");
    	stb.append("   AND CHAIRCD IN " + SQLUtils.whereIn(false, _param._categorySelected));
    	if ("3".equals(_currentPattern)) {
    		//※講座別コース別クラス別の出力 -> コース別、クラス別の帳票を別々に出力するよう上位で制限しているので、ここには入らない。理由は先頭に記載。
    	} else if ("2".equals(_currentPattern)) {
        	stb.append("   AND AVG_DIV = '3' ");
        	stb.append("   AND HR_CLASS = '000' ");
    	} else if ("1".equals(_currentPattern)) {
        	stb.append("   AND AVG_DIV = '2' ");
        	stb.append("   AND COURSECD = '0' ");
        	stb.append("   AND MAJORCD = '000' ");
        	stb.append("   AND COURSECODE = '0000' ");
    	} else {
        	stb.append("   AND AVG_DIV = '1' ");
        	stb.append("   AND HR_CLASS = '000' ");
        	stb.append("   AND COURSECD = '0' ");
        	stb.append("   AND MAJORCD = '000' ");
        	stb.append("   AND COURSECODE = '0000' ");
    	}
    	stb.append("   AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '" + _param._subclasscd + "' ");
    	stb.append("  ORDER BY GRADE, HR_CLASS, COURSECD, MAJORCD, COURSECODE ");
    	return stb.toString();
    }

	private List rebuildMap(final Map studentMap, final int cntMax, final Map lessonMap) {
		if (studentMap.size() == 0) return null;
		List retList = new ArrayList();
		Map addwk = new LinkedMap();
		List lessonWkList = null;
		retList.add(addwk);
		for (final Iterator ite = studentMap.keySet().iterator();ite.hasNext();) {
			final String kStr = (String)ite.next();
			if (addwk.size() >= cntMax) {
				addwk = new LinkedMap();
				retList.add(addwk);
			}
			Student student = (Student)studentMap.get(kStr);
			if (student._attendSubclsMap != null) {
				for (Iterator its = student._attendSubclsMap.keySet().iterator();its.hasNext();) {
					final String sStr = (String)its.next();
					final Attendance att = (Attendance)student._attendSubclsMap.get(sStr);

					if (att._lesson > 0) {
						if (lessonMap.containsKey(att._semester)) {
							lessonWkList = (List)lessonMap.get(att._semester);
						} else {
							lessonWkList = new ArrayList();
							lessonMap.put(att._semester, lessonWkList);
						}
						Integer addValWk = new Integer(att._lesson);
						if (!lessonWkList.contains(addValWk)) {
						    lessonWkList.add(addValWk);
						}
					}
				}
			}
			addwk.put(kStr, studentMap.get(kStr));
		}
		return retList;
	}

    private class ChairInfo {
        final String _chaircd;
        final String _chairname;
        final String _chargediv;
        final String _staffcd;
        final String _staffname;

        final List _subStaffList;

        Map _studentCCourseClassMap;
        Map _studentCCourseMap;
        Map _studentCClassMap;
        final Map _studentChairMap;
        public ChairInfo (final String chaircd, final String chairname, final String chargediv, final String staffcd, final String staffname)
        {
            _chaircd = chaircd;
            _chairname = chairname;
            _chargediv = chargediv;
            _staffcd = staffcd;
            _staffname = staffname;
            _studentCCourseClassMap = null;
            _studentCCourseMap = null;
            _studentCClassMap = null;
            _studentChairMap = new TreeMap();

            _subStaffList = new ArrayList();
        }
        private void addStudentList(final String coursecd, final String majorcd, final String coursecode, final String grade, final String hr_Class, final Student student) {
			final String key1Str = coursecd + majorcd + coursecode;
			final String key2Str = grade + hr_Class;
			Map subMap = null;
			Map detMap = null;
        	if ("3".equals(_currentPattern)) {  //※講座別コース別クラス別の出力 -> コース別、クラス別の帳票を別々に出力するよう上位で制限しているので、ここには入らない。理由は先頭に記載。
                //(講座別)コース別年組別:_studentCCourseClassMap
    			if (_studentCCourseClassMap == null) {
    				_studentCCourseClassMap = new LinkedMap();
    			}
    			if (_studentCCourseClassMap.containsKey(key1Str)) {
    				subMap = (Map)_studentCCourseClassMap.get(key1Str);
    			} else {
    				subMap = new LinkedMap();
    				_studentCCourseClassMap.put(key1Str, subMap);
    			}
				if (subMap.containsKey(key2Str)) {
					detMap = (Map)subMap.get(key2Str);
				} else {
					detMap = new LinkedMap();
					subMap.put(key2Str, detMap);
				}
				detMap.put(student._schregno, student);
        	} else if ("2".equals(_currentPattern)) {
                //(講座別)コース別:_studentCCourseMap
    			if (_studentCCourseMap == null) {
    				_studentCCourseMap = new LinkedMap();
    			}
    			if (_studentCCourseMap.containsKey(key1Str)) {
    				subMap = (Map)_studentCCourseMap.get(key1Str);
    			} else {
    				subMap = new LinkedMap();
    				_studentCCourseMap.put(key1Str, subMap);
    			}
    			subMap.put(student._schregno, student);
        	} else if ("1".equals(_currentPattern)) {
                //(講座別)年組別:_studentCClassMap
        		if (_studentCClassMap == null) {
        			_studentCClassMap = new LinkedMap();
        		}
    			if (_studentCClassMap.containsKey(key2Str)) {
    				subMap = (Map)_studentCClassMap.get(key2Str);
    			} else {
    				subMap = new LinkedMap();
    				_studentCClassMap.put(key2Str, subMap);
    			}
    			subMap.put(student._schregno, student);
        	}
            //講座別:_studentChairMap
        	_studentChairMap.put(student._schregno, student);
        }

        private Map getStudentList() {
			Map retMap = null;
        	if ("3".equals(_currentPattern)) {
                //(講座別)コース別年組別:_studentCCourseClassMap
                retMap = _studentCCourseClassMap;
        	} else if ("2".equals(_currentPattern)) {
                //(講座別)コース別:_studentCCourseMap
    			retMap = _studentCCourseMap;
        	} else if ("1".equals(_currentPattern)) {
                //(講座別)年組別:_studentCClassMap
   				retMap = _studentCClassMap;
        	} else {
            //講座別:_studentChairMap
        		retMap = _studentChairMap;
        	}
        	return retMap;
        }
    }

    /**
     * 生徒
     */
    private class Student {
        final String _chaircd;
        final String _coursecd;
        final String _majorcd;
        final String _majorname;
        final String _coursecode;
        final String _coursecodeabbv1;
        final String _grade;
        final String _grade_Name1;
        final String _hr_Class;
        final String _hr_Name;
        final String _attendno;
        final String _schregno;
        final String _name;

        final Map _attendMap;
        Map _attendSubclsMap;
        Map _scoreMap;

        public Student (final String chaircd, final String coursecd, final String majorcd, final String majorname, final String coursecode, final String coursecodeabbv1, final String grade, final String grade_Name1, final String hr_Class, final String hr_Name, final String attendno, final String schregno, final String name)
        {
            _chaircd = chaircd;
            _coursecd = coursecd;
            _majorcd = majorcd;
            _majorname = majorname;
            _coursecode = coursecode;
            _coursecodeabbv1 = coursecodeabbv1;
            _grade = grade;
            _grade_Name1 = grade_Name1;
            _hr_Class = hr_Class;
            _hr_Name = hr_Name;
            _attendno = attendno;
            _schregno = schregno;
            _name = name;

            _attendMap = new LinkedMap();
            _scoreMap = null;
        }
    }

    private class ScoreInfo {
        final String _year;
        final String _semester;
        final String _testkindcd;
        final String _testitemcd;
        final String _score_Div;
        final String _classcd;
        final String _school_Kind;
        final String _curriculum_Cd;
        final String _subclasscd;
        final String _schregno;
        final String _score;
        final String _value_Di;
        public ScoreInfo (final String year, final String semester, final String testkindcd, final String testitemcd, final String score_Div, final String classcd, final String school_Kind, final String curriculum_Cd, final String subclasscd, final String schregno, final String score, final String value_Di)
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
            _schregno = schregno;
            _score = score;
            _value_Di = value_Di;
        }
    }

    private class DownTotalInfo {
        final String _semester;
        final String _testkindcd;
        final String _testitemcd;
        final String _score_Div;
        final String _grade;
        final String _hr_Class;
        final String _coursecd;
        final String _majorcd;
        final String _coursecode;
        final String _score;
        final String _count;
        final String _avg;
        public DownTotalInfo (final String semester, final String testkindcd, final String testitemcd, final String score_Div, final String grade, final String hr_Class, final String coursecd, final String majorcd, final String coursecode, final String score, final String count, final String avg)
        {
            _semester = semester;
            _testkindcd = testkindcd;
            _testitemcd = testitemcd;
            _score_Div = score_Div;
            _grade = grade;
            _hr_Class = hr_Class;
            _coursecd = coursecd;
            _majorcd = majorcd;
            _coursecode = coursecode;
            _score = score;
            _count = count;
            _avg = avg;
        }
    }

    private class Attendance {
        final String _semester;
        final int _lesson;
        final int _mLesson;
        final int _suspend;
        final int _mourning;
        final int _sick;
        final int _sickOnly;
        final int _noticeOnly;
        final int _nonoticeOnly;
        final int _present;
        final int _late;
        final int _early;
        final int _kekkaJisu;
        final int _transDays;
        Attendance(
                final String semester,
                final int lesson,
                final int mLesson,
                final int suspend,
                final int mourning,
                final int sick,
                final int sickOnly,
                final int noticeOnly,
                final int nonoticeOnly,
                final int present,
                final int late,
                final int early,
                final int kekkaJisu,
                final int transDays
        ) {
            _semester = semester;
            _lesson = lesson;
            _mLesson = mLesson;
            _suspend = suspend;
            _mourning = mourning;
            _sick = sick;
            _sickOnly = sickOnly;
            _noticeOnly = noticeOnly;
            _nonoticeOnly = nonoticeOnly;
            _present = present;
            _late = late;
            _early = early;
            _kekkaJisu = kekkaJisu;
            _transDays = transDays;
        }
    }

    private Map loadSubclassAttend(
            final DB2UDB db2
    ) {
    	Map retMap = new TreeMap();
    	Map subMap = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String SSEMESTER = "1";
        _param._attendSubclsParamMap.put("subclasscd", _param._subclasscd);
        _param._attendSubclsParamMap.put("sSemester", SSEMESTER);
        String edate = _param._date;
        String sdate = (String)_param._sdate;
        if (sdate.compareTo(edate) < 0) {
            final String sqlAttendSubclass = AttendAccumulate.getAttendSubclassSql(
            		_param._year,
            		SEMEALL,
                    (String)sdate,
                    (String)edate,
                    _param._attendSubclsParamMap
                    );
            try {
                ps = db2.prepareStatement(sqlAttendSubclass);
            	for (int clsCnt = 0;clsCnt < _param._categorySelected.length;clsCnt++) {
                    rs = ps.executeQuery();
                    while (rs.next()) {
                	    final String schregno = rs.getString("SCHREGNO");
                	    final String semester = rs.getString("SEMESTER");
                        final String subclasscd = rs.getString("SUBCLASSCD");
                        final Attendance attendance = new Attendance(
                        		semester,
                        		rs.getInt("LESSON"),
                        		rs.getInt("MLESSON"),
                        		rs.getInt("SUSPEND"),
                        		rs.getInt("MOURNING"),
                        		rs.getInt("SICK1"),
                        		0,
                        		0,
                        		0,
                        		0,
                        		rs.getInt("LATE"),
                        		rs.getInt("EARLY"),
                        		rs.getInt("SICK2"),
                        		0
                        );
                        if (retMap.containsKey(schregno)) {
                        	subMap = (Map)retMap.get(schregno);
                        } else {
                        	subMap = new LinkedMap();
                        	retMap.put(schregno, subMap);
                        }
                        final String sKey = semester + ":" + subclasscd;
                        subMap.put(sKey, attendance);
                    }
                    if (clsCnt+1 < _param._categorySelected.length) {  //最終じゃなければ、psを再利用するので、rsのみ閉じる。最後はfinallyで。
                        DbUtils.closeQuietly(rs);
                    }
            	}
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        return retMap;
    }

    protected Param createParam(final HttpServletRequest request, final DB2UDB db2) {
        log.fatal("$Revision: 74640 $ $Date: 2020-06-01 17:53:57 +0900 (月, 01 6 2020) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(request, db2);
        return param;
    }

    private class Param {
        final String _year;
        final String _semester;
        final String[] _categorySelected;
        final String _sdate;
        final String _date;
        final Map _semesterMap;

        final String _useCurriculumcd;

        final String _schoolKind;
        final String _schoolCd;


        private String _cerifSchoolName;

        private boolean _isNoPrintMoto;
        final Map _attendSubclsParamMap;
        final boolean _isOutputDebug;

        final String _classcd;
        final String _subclasscd;
        final String _ctrlDate;

        final List _d026List = new ArrayList();

        final String _keyTypeCourse;
        final String _keyTypeClass;
        final String _schKindName;
        final String _subclsName;

        final String _keyPattern;

        Param(final HttpServletRequest request, final DB2UDB db2) {
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("SEMESTER");

            _classcd = request.getParameter("CLASSCD");
            _subclasscd = request.getParameter("SUBCLASSCD");
            final String[] cutStr = StringUtils.split(_subclasscd, '-');
            if (cutStr.length > 2) {
            	_schoolKind = cutStr[1];
            } else {
                _schoolKind = request.getParameter("SCHOOL_KIND");
            }
            _schKindName = getSchKindName(db2);
            _ctrlDate = null == request.getParameter("CTRL_DATE") ? request.getParameter("CTRL_DATE") : request.getParameter("CTRL_DATE").replace('/', '-');
            _sdate = null == request.getParameter("DATE1") ? request.getParameter("DATE1") : request.getParameter("DATE1").replace('/', '-');
            _date = null == request.getParameter("DATE2") ? request.getParameter("DATE2") : request.getParameter("DATE2").replace('/', '-');

            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");  //対象講座(一覧から選択)


            _schoolCd = request.getParameter("SCHOOLCD");

            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _keyTypeCourse = request.getParameter("DIV_COURSE");
            _keyTypeClass = request.getParameter("DIV_CLASS");

            setCertifSchoolDat(db2);

            loadNameMstD026(db2);
            loadNameMstD016(db2);

            _attendSubclsParamMap = new HashMap();
            _attendSubclsParamMap.put("DB2UDB", db2);
            _attendSubclsParamMap.put("HttpServletRequest", request);
            _attendSubclsParamMap.put("useCurriculumcd", _useCurriculumcd);

            _isOutputDebug = "1".equals(KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJD616G' AND NAME = 'outputDebug' ")));

            _subclsName = getSublcsName(db2);
            _semesterMap = getSemesterMap(db2);
            _keyPattern = getKeyPattern();
        }

        private String getSublcsName(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append("SELECT  ");
            stb.append("  SUBCLASSNAME ");
            stb.append("FROM    SUBCLASS_MST T1 ");
            stb.append("WHERE   T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '" + _subclasscd + "' ");
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, stb.toString()));
        }

        private String getSchKindName(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   ABBV1 ");
            stb.append(" FROM ");
            stb.append("   NAME_MST ");
            stb.append(" WHERE ");
            stb.append("   NAMECD1 = 'A023' ");
            stb.append("   AND NAME1 = '" + _schoolKind + "' ");
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, stb.toString()));
        }
        private void loadNameMstD016(final DB2UDB db2) {
            final String sql = "SELECT NAMESPARE1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D016' AND NAMECD2 = '01' ";
            final String namespare1 = KnjDbUtils.getOne(KnjDbUtils.query(db2, sql));
            _isNoPrintMoto = "Y".equals(namespare1);
            log.info("(名称マスタD016):元科目を表示しない = " + _isNoPrintMoto);
        }

        private void setCertifSchoolDat(final DB2UDB db2) {
            StringBuffer sql = new StringBuffer();
            sql.append(" select SCHOOLNAME1 FROM SCHOOL_MST ");
            sql.append(" WHERE YEAR = '" + _year + "' AND SCHOOL_KIND = '" + _schoolKind + "' AND SCHOOLCD = '" + _schoolCd + "' ");
            log.debug("certif_school_dat sql = " + sql.toString());

        	final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql.toString()));
            _cerifSchoolName = StringUtils.defaultString(KnjDbUtils.getString(row, "SCHOOLNAME1"));
        }

        private Map getSemesterMap(final DB2UDB db2) {
            final Map retMap = new LinkedMap();
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT * FROM SEMESTER_MST ");
            sql.append(" WHERE YEAR = '" + _year + "' ORDER BY SEMESTER ");
            log.debug("getSemesterMap sql = " + sql.toString());

            PreparedStatement ps = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                for (final Iterator rit = KnjDbUtils.query(db2, ps, null).iterator();rit.hasNext();) {
               	    final Map row = (Map) rit.next();
               	    final String semester = KnjDbUtils.getString(row, "SEMESTER");
               	    retMap.put(semester, row);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
            }
            return retMap;
        }

        private void loadNameMstD026(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            final String field;
            if ("1".equals(_semester)) {
                field = "ABBV1";
            } else if ("2".equals(_semester)) {
                field = "ABBV2";
            } else {
                field = "ABBV3";
            }
            sql.append(" SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D026' AND " + field + " = '1' OR NAMESPARE1 = '1' ");

            _d026List.clear();
            _d026List.addAll(KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, sql.toString()), "NAME1"));
        }
        private String getKeyPattern() {
			String keyPattern = null;
        	if ("1".equals(_keyTypeCourse)) {
        		if ("1".equals(_keyTypeClass)) {
                    //(講座別)コース別年組別:_studentCCourseClassMap
            		keyPattern = "3";
        		} else {
                    //(講座別)コース別:_studentCCourseMap
            		keyPattern = "2";
        		}
        	} else if ("1".equals(_keyTypeClass)) {
                //(講座別)年組別:_studentCClassMap
        		keyPattern = "1";
        	} else {
            //講座別:_studentChairMap
        		keyPattern = "0";
        	}
        	return keyPattern;
        }
    }
}
