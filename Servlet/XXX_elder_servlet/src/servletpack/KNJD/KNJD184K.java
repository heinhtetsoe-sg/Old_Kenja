/*
 * $Id: 0697b7c81945b644ecdf8fe2fecf98e7d1d601a2 $
 *
 * 作成日: 2018/06/14
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.math.BigDecimal;
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

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJD184K {

    private static final Log log = LogFactory.getLog(KNJD184K.class);

    private static final String SEMEALL = "9";
    private static final String SELECT_CLASSCD_UNDER = "90";

    private static final String SDIV1990008 = "1990008"; //1学期
    private static final String SDIV2990008 = "2990008"; //2学期
    private static final String SDIV9990008 = "9990008"; //学年
    private static final String SDIV9990009 = "9990009"; //学年

    private static final String ALL9 = "999999";
    /** 中央寄せ */
    static final String ATTR_CENTERING = "Hensyu=3";

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        final List<Student> studentList = getStudentList(db2);

        for (final Student student : studentList) {

            svf.VrSetForm("KNJD184K.frm", 4);

            final String attendno = NumberUtils.isDigits(student._attendno) ? String.valueOf(Integer.parseInt(student._attendno)) : StringUtils.defaultString(student._attendno);

            //学校ロゴ
            if(_param._schoolLogoPath != null) {
                svf.VrsOut("SCHOOLLOGO", _param._schoolLogoPath);
            }

            //学年
            svf.VrsOut("GRADE", student._gradeName);

            //年度
            svf.VrsOut("NENDO", _param._nendo);

            //年組番号
            svf.VrsOut("HR_NAME", student._hrName + " " + attendno + "番");

            //生徒情報
            final String nameField = getMS932ByteLength(student._name) > 40 ? "3" : getMS932ByteLength(student._name) > 30 ? "2" : "1";
            svf.VrsOut("NAME" + nameField, student._name);

            //学校長
            String princname = _param._schoolInfo != null ? _param._schoolInfo._principalname : "";
            final String princField = getMS932ByteLength(princname) > 40 ? "_3" : getMS932ByteLength(princname) > 30 ? "_2" : "_1";
            svf.VrsOut("STAFFNAME1" + princField, princname);

            //担任
            String teachername = student._staffname != null ? student._staffname : "";
            final String teacherField = getMS932ByteLength(teachername) > 40 ? "_3" : getMS932ByteLength(teachername) > 30 ? "_2" : "_1";
            svf.VrsOut("STAFFNAME2" + teacherField, teachername);

            //学校名
            svf.VrsOut("SCHOOLNAME", (_param._schoolInfo != null ? _param._schoolInfo._schoolname : ""));

            //生徒情報
            final String schInfo = ("H".equals(_param._schoolKind)) ? student._gradeName + "・" + student._majorname + "(" + student._coursecodename + ")" : student._gradeName;
            svf.VrsOut("SCH_INFO", schInfo);

            if("1".equals(_param._descDatePrint)) {
                //出欠コメント
                final String[] summary = KNJ_EditDate.tate_format4(db2, _param._summaryDate);
                final String comment = summary[2] + "月" + summary[3] + "日の出欠状況です。";
                svf.VrsOut("ATTEND_COMMENT", comment);
            }

            //証明文言
            final String cert = ("H".equals(_param._schoolKind)) ? "本校" + student._majorname + student._gradeName : "中学校" + student._gradeName;
            svf.VrsOut("CERT", cert + "の課程を修了したことを証します");

            //証明日付
            final String[] desc = KNJ_EditDate.tate_format4(db2, _param._descDate);
            svf.VrsOut("CERTIF_DATE", desc[0] + desc[1] + "年" + desc[2] + "月" + desc[3] + "日");

            //学校名
            svf.VrsOut("SCHOOLNAME2", (_param._schoolInfo != null ? _param._schoolInfo._schoolname : ""));

            //学校長
            svf.VrsOut("STAFFNAME3" + princField, princname);

            //マスク
            if(!_param._semes3Flg || !"1".equals(_param._printFooter)) {
                if(_param._whiteSpaceImagePath != null) {
                    svf.VrsOut("MASK", _param._whiteSpaceImagePath);
                }
            }

//          //押印
//          final String imgFIleName = getImage(db2,student._stampno);
//          if(imgFIleName != null) {
//              svf.VrsOut("STAMP",imgFIleName);
//          }

            //出欠欄
            printAttendData(svf, student);

            //科目欄
            printTotalScore(svf, student); //合計列
            printSubclassRecordDetail(db2, svf, student); //科目別
            svf.VrEndPage();

            _hasData = true;
        }
    }

    //出欠欄
    private void printAttendData(final Vrw32alp svf, final Student student) {

        for (final String semester : _param._semesterMstMap.keySet()) {

            if ("3".equals(semester)) {
            	continue;
            }

            if (!"9".equals(semester) && Integer.parseInt(semester) > Integer.parseInt(_param._semester)) {
                continue;
            }

            if(!_param._semes3Flg && "9".equals(semester)) return;

            final Map attendMap = (Map) student._attendMap.get( ("9".equals(semester) ? "99" : semester) );
            final int col = "9".equals(semester) ? 3 : Integer.parseInt(semester);
            if (null != attendMap) {
                svf.VrsOutn("LESSON", col, (String) attendMap.get("JYUGYOU")); //授業日数
                svf.VrsOutn("SUSPEND", col, (String) attendMap.get("SHUTTEI")); //出席停止・忌引等日数
                svf.VrsOutn("PRESENT", col, (String) attendMap.get("SHUSSEKISUBEKI")); //出席しなければならない日数
                svf.VrsOutn("SICK", col, (String) attendMap.get("KESSEKI")); //欠席日数
                svf.VrsOutn("ATTEND", col, (String) attendMap.get("SHUSSEKI")); //出席日数
                svf.VrsOutn("LATE", col, (String) attendMap.get("TIKOKU")); //遅刻
                svf.VrsOutn("EARLY", col, (String) attendMap.get("SOUTAI")); //早退
            } else {
                svf.VrsOutn("LESSON", col, "");  //授業日数
                svf.VrsOutn("SUSPEND", col, ""); //出席停止・忌引等日数
                svf.VrsOutn("PRESENT", col, ""); //出席しなければならない日数
                svf.VrsOutn("SICK", col, "");    //欠席日数
                svf.VrsOutn("ATTEND", col, "");  //出席日数
                svf.VrsOutn("LATE", col, "");    //遅刻
                svf.VrsOutn("EARLY", col, "");   //早退
            }
        }
    }

    private void printTotalScore(final Vrw32alp svf, final Student student) {
        if (student._totalscore != null) {
            //1学期
            svf.VrsOut("TOTAL_SCORE1", student._totalscore._score1);
            if (student._totalscore._avg1 != null) {
                svf.VrsOut("SCORE_AVERAGE1", rounddown(student._totalscore._avg1, 1)); //小数1桁で切り捨て(※合計が100点超えるため、5桁表示を意識)
            }

            //2学期
            if(_param._semes2Flg) {
                svf.VrsOut("TOTAL_SCORE2", student._totalscore._score2);
                if (student._totalscore._avg2 != null) {
                    svf.VrsOut("SCORE_AVERAGE2", rounddown(student._totalscore._avg2, 1)); //小数1桁で切り捨て(※合計が100点超えるため、5桁表示を意識)
                }
            }

            //3学期
            if(_param._semes3Flg) {
                svf.VrsOut("TOTAL_SCORE3", student._totalscore._score3);
                if (student._totalscore._avg3 != null) {
                    svf.VrsOut("SCORE_AVERAGE3", rounddown(student._totalscore._avg3, 1)); //小数1桁で切り捨て(※合計が100点超えるため、5桁表示を意識)
                }

                svf.VrsOut("TOTAL_SCORE9", student._totalscore._score9);
                if (student._totalscore._avg9 != null) {
                    svf.VrsOut("SCORE_AVERAGE9", rounddown(student._totalscore._avg9, 1)); //小数1桁で切り捨て(※合計が100点超えるため、5桁表示を意識)
                }
            }
        }
    }

    //指定桁数で切り捨て
    private String rounddown(final String val, final int keta) {
        if (val == null || !StringUtils.isNumeric(val)) {
            return val;
        }
        BigDecimal bd1 = new BigDecimal(val);
        return bd1.setScale(keta - 1, BigDecimal.ROUND_DOWN).toString();
    }

    private void printSubclassRecordDetail(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        final int maxLine = 16;
        int linecnt = 0;

        final Map<String, Subclass> combinedSubclassMap = new HashMap<String, Subclass>();
        final Map<String, List<Subclass>> combinedSubclasscdAttendSubclassListMap = new HashMap<String, List<Subclass>>();
        for (final Iterator<String> itClassMst = student._classCdOrder.iterator(); itClassMst.hasNext();) {
            final String classMstKey = itClassMst.next();
            final ClassMst classMst = student._classMstMap.get(classMstKey);

            for (final Iterator<String> itSubclass = classMst._subclassKeyOrder.iterator(); itSubclass.hasNext();) {
                final String subclassKey = itSubclass.next();
                final Subclass subclass = student._subclassMap.get(subclassKey);

                if (_param._combinedSubclassAttendSubclassListMap.keySet().contains(subclassKey)) {
                	// 合併先科目
                	combinedSubclassMap.put(subclassKey, subclass);
                	//List<Subclass> attends = new ArrayList<Subclass>();
                	for (final String attendSubclasscd : _param._combinedSubclassAttendSubclassListMap.get(subclassKey)) {
                		final Subclass attendSubclass = student._subclassMap.get(attendSubclasscd);
                		if (null != attendSubclass) {
                			Param.getMappedList(combinedSubclasscdAttendSubclassListMap, subclassKey).add(attendSubclass);
                			//attends.add(attendSubclass);
                		}
                	}
//                	if (attends.isEmpty()) {
//                		log.info("合併元の科目がない場合、合併先科目は科目の列には表示する : " + subclass._subclassCd + " : " + subclass._subclassName);
//                	} else {
//                		itSubclass.remove();
//                		log.info(" remove gappeisaki subclass " + subclass._subclassCd + " : " + subclass._subclassName);
//                	}
            		itSubclass.remove();
            		//log.info(" remove gappeisaki subclass " + subclass._subclassCd + " : " + subclass._subclassName);
                }
            }

            if (classMst._subclassKeyOrder.isEmpty()) {
            	//log.info(" remove empty subclasses classMst " + classMst._classCd + " : " + classMst._className);
            	itClassMst.remove();
            }
        }

        int subclassline = 0;
        for (final String classMstKey : student._classCdOrder) {
            final ClassMst classMst = student._classMstMap.get(classMstKey);

            int subClassCnt = 0;
            for (final String subclassKey : classMst._subclassKeyOrder) {
                final Subclass subclass = student._subclassMap.get(subclassKey);

                //log.info(" subclass " + subclass._subclassCd + " : " + subclass._subclassName);
                if (null == subclass._subclassName) {
                    continue;
                }
                final boolean isAttendSubclass = _param._attendSubclassCombinedSubclassMap.keySet().contains(subclassKey);
                if (!"".equals(classMst._className)) {
                	if (getMS932ByteLength(classMst._className) > 6) {
                        svf.VrsOut("CLASSNAME2", (String) classMst._className);
                        svf.VrsOut("CLASSNAME", "1");
                        svf.VrAttribute("CLASSNAME", "X=10000");
                	} else {
                        svf.VrsOut("CLASSNAME", (String) classMst._className);
                        svf.VrsOut("CLASSNAME2", "1");
                        svf.VrAttribute("CLASSNAME2", "X=10000");
                	}
                    final String subclassName;
//					if (isAttendSubclass) {
//                    	// 合併元科目は頭に合併先科目略称を表示
//                    	final String combinedSubclassKey = _param._attendSubclassCombinedSubclassMap.get(subclassKey);
//                    	final String combinedSubclassabbv = _param._combinedSubclassabbvMap.get(combinedSubclassKey);
//                        subclassName = StringUtils.defaultString(combinedSubclassabbv) + "(" + StringUtils.defaultString(subclass._subclassName) + ")";
//                    } else {
//                        subclassName = subclass._subclassName;
//                    }
                    subclassName = subclass._subclassName;
					if (StringUtils.defaultString(subclassName).length() > 10) {
	                    svf.VrsOut("SUBCLASSNAME2_1", subclassName.substring(0, 10));
	                    svf.VrsOut("SUBCLASSNAME2_2", subclassName.substring(10));
					} else {
	                    svf.VrsOut("SUBCLASSNAME", subclassName);
					}
                }

                String scoreGrp = String.valueOf(subclassline);
                String isAttendSubclassPrintCombinedSubclassHyotei = null;
                if (isAttendSubclass) {
                	final String combinedSubclassKey = _param._attendSubclassCombinedSubclassMap.get(subclassKey);
                	log.info(" attend subclass " + subclassKey + ", combined = " + combinedSubclassKey + ", " + _param._combinedSubclassabbvMap.get(combinedSubclassKey) + ", score = " + combinedSubclassMap.get(combinedSubclassKey));
                    final int idx = _param._combinedSubclassList.indexOf(combinedSubclassKey);
                    if (-1 != idx) {
                    	scoreGrp = "C" + String.valueOf(idx + 1);
                    }
                    for (final String subclassKey2 : classMst._subclassKeyOrder) {
                    	final int idx3 = Param.getMappedList(_param._combinedSubclassAttendSubclassListMap, combinedSubclassKey).indexOf(subclassKey2);
                    	if (-1 != idx3) {
                            isAttendSubclassPrintCombinedSubclassHyotei = subclassKey2;
                            break;
                    	}
                    }
                }
                svf.VrsOut("SCORE3_GRP", scoreGrp);
                svf.VrsOut("SCORE9_GRP", scoreGrp);

                final Score score = subclass._score;
                if (null != score) {
                    //1学期
                    final String score1 = StringUtils.defaultString(score._valueDi1, ("".equals(score._d065_name)) ? score._score1 : _param._d001Map.get(score._score1));
                    svf.VrsOut("SCORE1", score1);

                    //2学期
                    if(_param._semes2Flg) {
                        final String score2 = StringUtils.defaultString(score._valueDi2, ("".equals(score._d065_name)) ? score._score2 : _param._d001Map.get(score._score2));
                        svf.VrsOut("SCORE2", score2);
                    }

                    //3学期
                    if(_param._semes3Flg) {
                        String score3;
                        String score9;
                        int attx = -1;
                    	if (isAttendSubclass) {
                    		// 合併元は合併先の成績を出力する
                        	final String combinedSubclassKey = _param._attendSubclassCombinedSubclassMap.get(subclassKey);
                        	final Subclass combinedSubclass = combinedSubclassMap.get(combinedSubclassKey);
                        	if (null == combinedSubclass || null == combinedSubclass._score || !isAttendSubclassPrintCombinedSubclassHyotei.equals(subclassKey)) {
                                score3 = null;
                                score9 = null;
                        	} else {
                                score3 = StringUtils.defaultString(combinedSubclass._score._valueDi3, combinedSubclass._score._score3);
                                score9 = StringUtils.defaultString(combinedSubclass._score._valueDi9, combinedSubclass._score._score9);
                                final int attendSubclassSize = Param.getMappedList(combinedSubclasscdAttendSubclassListMap, combinedSubclassKey).size();
                                final int recordStart = 414;
                                final int recordWidth = 564 - 414;
                                final int fieldStart = 454;
                                attx = recordStart + subclassline * recordWidth + attendSubclassSize * recordWidth / 2 - (fieldStart - recordStart);
                        	}
                    	} else {
                            score3 = StringUtils.defaultString(score._valueDi3, score._score3);
                            score9 = StringUtils.defaultString(score._valueDi9, score._score9);
                        	log.info(subclassKey + " score3 = " + score3 + ", score9 = " + score9);
                    	}
                    	if (!"".equals(score._d065_name)) {
                    		score3 = _param._d001Map.get(score._score3);
                    		log.info(" score3 " + score._score3 + " => " + _param._d001Map.get(score._score3));
                    		score9 = _param._d001Map.get(score._score9);
                    		log.info(" score9 " + score._score9 + " => " + _param._d001Map.get(score._score9));
                    	}
                        svf.VrsOut("SCORE3", score3);
                        svf.VrsOut("SCORE9", score9);
                        if (-1 != attx) {
                            svf.VrAttribute("SCORE3", "X=" + String.valueOf(attx));
                            svf.VrAttribute("SCORE9", "X=" + String.valueOf(attx));
                        }
                    }
                }
                subClassCnt++;
                subclassline++;
                svf.VrEndRecord();
            }
            linecnt += subClassCnt;
        }
        for (;linecnt < maxLine;linecnt++) {
            svf.VrsOut("BLANK", "0");
            svf.VrsOut("SCORE3_GRP", String.valueOf(linecnt));
            svf.VrsOut("SCORE9_GRP", String.valueOf(linecnt));
            svf.VrEndRecord();
        }

        //空行の人の出力のために、空でも１回打っておく。
        if(linecnt == 0) svf.VrEndRecord();
    }

    private static boolean isDigits(final String s) {
        return NumberUtils.isDigits(s);
    }

    private int convval(final String str) {
        return isDigits(str) ? Integer.parseInt(str) : 0;
    }

    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str) {
        return KNJ_EditEdit.getMS932ByteLength(str);
    }

    private List getStudentList(final DB2UDB db2) {
        final List retList = new ArrayList();
        try {
            final String sql = getStudentSql();
            log.debug(" sql =" + sql);

            for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {
                final String schregno = KnjDbUtils.getString(row, "SCHREGNO");
                final String grade = KnjDbUtils.getString(row, "GRADE");
                final String gradeName = KnjDbUtils.getString(row, "GRADE_NAME");
                final String hrClass = KnjDbUtils.getString(row, "HR_CLASS");
                final String attendno = KnjDbUtils.getString(row, "ATTENDNO");
                final String coursecd = KnjDbUtils.getString(row, "COURSECD");
                final String majorcd = KnjDbUtils.getString(row, "MAJORCD");
                final String coursecode = KnjDbUtils.getString(row, "COURSECODE");
                final String name = KnjDbUtils.getString(row, "NAME");
                final String hrName = KnjDbUtils.getString(row, "HR_NAME");
                final String hrAbbv = KnjDbUtils.getString(row, "HR_NAMEABBV");
                final String majorname = KnjDbUtils.getString(row, "MAJORNAME");
                final String coursecodename = KnjDbUtils.getString(row, "COURSECODENAME");
                final String staffname = KnjDbUtils.getString(row, "STAFFNAME");
                final Student student = new Student(schregno, grade, gradeName, hrClass, attendno, coursecd, majorcd, coursecode, name, hrName, hrAbbv, majorname, coursecodename, staffname);
                //評定・欠課情報1(学期別)
                student.setAttendData(db2);
                student.setScoreData(db2);
                //評定(合計)
                student.setTotalScoreData(db2);
                //特別活動・所見・備考
                student.setHreportremark(db2);
                retList.add(student);
            }

        } catch (Exception ex) {
            log.error("Exception:", ex);
        }
        return retList;
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     REGD.SCHREGNO, ");
        stb.append("     REGD.GRADE, ");
        stb.append("     GDAT.GRADE_NAME2 AS GRADE_NAME, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     REGD.COURSECD, ");
        stb.append("     REGD.MAJORCD, ");
        stb.append("     REGD.COURSECODE, ");
        stb.append("     BASE.NAME, ");
        stb.append("     REGH.HR_NAME, ");
        stb.append("     REGH.HR_NAMEABBV, ");
        stb.append("     MM.MAJORNAME, ");
        stb.append("     CM.COURSECODENAME, ");
        stb.append("     SM.STAFFNAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT REGD ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ");
        stb.append("            ON REGD.YEAR  = GDAT.YEAR ");
        stb.append("           AND REGD.GRADE = GDAT.GRADE ");

        //               転学・退学者で、異動日が出欠集計日より小さい日付の場合
        stb.append("     LEFT JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = REGD.SCHREGNO ");
        stb.append("                  AND W3.GRD_DIV IN('2','3') ");
        stb.append("                  AND W3.GRD_DATE < '" + _param._summaryDate + "' ");
        //               転入・編入者で、異動日が出欠集計日より大きい日付の場合
        stb.append("     LEFT JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = REGD.SCHREGNO ");
        stb.append("                  AND W4.ENT_DIV IN('4','5') ");
        stb.append("                  AND W4.ENT_DATE > '" + _param._summaryDate + "' ");
        //               留学・休学者で、出欠集計日が留学・休学期間内にある場合
        stb.append("     LEFT JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = REGD.SCHREGNO ");
        stb.append("                  AND W5.TRANSFERCD IN ('1','2') ");
        stb.append("                  AND '" + _param._summaryDate + "'  BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE ");

        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGH ON REGD.YEAR = REGH.YEAR ");
        stb.append("          AND REGD.SEMESTER = REGH.SEMESTER ");
        stb.append("          AND REGD.GRADE = REGH.GRADE ");
        stb.append("          AND REGD.HR_CLASS = REGH.HR_CLASS ");
        stb.append("     LEFT JOIN STAFF_MST SM ");
        stb.append("       ON REGH.TR_CD1 = SM.STAFFCD ");
        stb.append("     LEFT JOIN MAJOR_MST MM ");
        stb.append("       ON MM.COURSECD = REGD.COURSECD ");
        stb.append("       AND MM.MAJORCD = REGD.MAJORCD ");
        stb.append("     LEFT JOIN COURSECODE_MST CM ");
        stb.append("       ON CM.COURSECODE = REGD.COURSECODE ");
        stb.append(" WHERE ");
        stb.append("     REGD.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND REGD.SEMESTER = '" + _param._ctrlSemester + "' ");
        if ("1".equals(_param._disp)) {
            stb.append("     AND REGD.GRADE || REGD.HR_CLASS IN (" + _param._sqlInstate + ") ");
        } else {
            stb.append("     AND REGD.SCHREGNO IN (" + _param._sqlInstate + ") ");
        }
        stb.append("  AND W3.SCHREGNO IS NULL AND W4.SCHREGNO IS NULL AND W5.SCHREGNO IS NULL"); //転学・退学者、転入・編入者、留学・休学者は除外
        stb.append(" ORDER BY ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGD.ATTENDNO ");
        return stb.toString();
    }

    /** 生徒クラス */
    private class Student {
        final String _schregno;
        final String _grade;
        final String _gradeName;
        final String _hrClass;
        final String _attendno;
        final String _coursecd;
        final String _majorcd;
        final String _coursecode;
        final String _name;
        final String _hrName;
        final String _hrAbbv;
        final String _majorname;
        final String _coursecodename;
        final String _staffname;
        final List<String> _classCdOrder = new ArrayList();
        TotalInfo _totalscore;
        Map _attendMap = new HashMap();
        Map<String, ClassMst> _classMstMap = new TreeMap();
        Map<String, Subclass> _subclassMap = new HashMap();
        Map _hreportremarkMap = new TreeMap();
        public Student(
                final String schregno,
                final String grade,
                final String gradeName,
                final String hrClass,
                final String attendno,
                final String coursecd,
                final String majorcd,
                final String coursecode,
                final String name,
                final String hrName,
                final String hrAbbv,
                final String majorname,
                final String coursecodename,
                final String staffname
        ) {
            _schregno = schregno;
            _grade = grade;
            _gradeName = gradeName;
            _hrClass = hrClass;
            _attendno = attendno;
            _coursecd = coursecd;
            _majorcd = majorcd;
            _coursecode = coursecode;
            _name = name;
            _hrName = hrName;
            _hrAbbv = hrAbbv;
            _majorname = majorname;
            _coursecodename = coursecodename;
            _staffname = staffname;
            _totalscore = null;
        }

        public void setAttendData(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getAttendSql();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                int totalJyugyou = 0;
                int totalShuttei = 0;
                int totalShussekiSubeki = 0;
                int totalKesseki = 0;
                int totalShusseki = 0;
                int totalTochuKekka = 0;
                int totalLate = 0;
                int totalEarly = 0;

                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final int lesson = rs.getInt("LESSON");
                    final int offDays = rs.getInt("OFFDAYS");
                    final int suspend = rs.getInt("SUSPEND");
                    final int mourning = rs.getInt("MOURNING");
                    final int abroad = rs.getInt("ABROAD");
                    final int sick = rs.getInt("SICK");
                    final int notice = rs.getInt("NOTICE");
                    final int nonotice = rs.getInt("NONOTICE");
                    final int late = rs.getInt("LATE");
                    final int early = rs.getInt("EARLY");
                    final int tochuKekka = rs.getInt("TOCHU_KEKKA");

                    if (!"9".equals(semester) && Integer.parseInt(semester) > Integer.parseInt(_param._semester)) {
                        continue;
                    }
                    final Map setMap = new HashMap();
                    final int setJyugyou = "1".equals(_param._schoolMstSemOffDays) ? lesson - abroad : lesson - offDays - abroad;
                    setMap.put("JYUGYOU", String.valueOf(setJyugyou));
                    final int setShuttei = suspend + mourning;
                    setMap.put("SHUTTEI", String.valueOf(setShuttei));
                    final int setShussekiSubeki = setJyugyou - setShuttei;
                    setMap.put("SHUSSEKISUBEKI", String.valueOf(setShussekiSubeki));
                    final int setKesseki = "1".equals(_param._schoolMstSemOffDays) ? sick + notice + nonotice + offDays : sick + notice + nonotice;
                    setMap.put("KESSEKI", String.valueOf(setKesseki));
                    final int setShusseki = setJyugyou - setShuttei - setKesseki;
                    setMap.put("SHUSSEKI", String.valueOf(setShusseki));
                    setMap.put("TOCHUKEKKA", String.valueOf(tochuKekka));
                    setMap.put("TIKOKU", String.valueOf(late));
                    setMap.put("SOUTAI", String.valueOf(early));

                    totalJyugyou += setJyugyou;
                    totalShuttei += setShuttei;
                    totalShussekiSubeki += setShussekiSubeki;
                    totalKesseki += setKesseki;
                    totalShusseki += setShusseki;
                    totalTochuKekka += tochuKekka;
                    totalLate += late;
                    totalEarly += early;

                    _attendMap.put(semester, setMap);
                }
                if (_attendMap.size() > 0) {
                    final Map setMap = new HashMap();
                    setMap.put("JYUGYOU", String.valueOf(totalJyugyou));
                    setMap.put("SHUTTEI", String.valueOf(totalShuttei));
                    setMap.put("SHUSSEKISUBEKI", String.valueOf(totalShussekiSubeki));
                    setMap.put("KESSEKI", String.valueOf(totalKesseki));
                    setMap.put("SHUSSEKI", String.valueOf(totalShusseki));
                    setMap.put("TOCHUKEKKA", String.valueOf(totalTochuKekka));
                    setMap.put("TIKOKU", String.valueOf(totalLate));
                    setMap.put("SOUTAI", String.valueOf(totalEarly));
                    _attendMap.put("99", setMap);
                }

                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private String getAttendSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SEMESTER, ");
            stb.append("     SUM(VALUE(LESSON, 0)) AS LESSON, ");
            stb.append("     SUM(VALUE(OFFDAYS, 0)) AS OFFDAYS, ");
            stb.append("     SUM(VALUE(ABSENT, 0)) AS ABSENT, ");
            stb.append("     SUM(VALUE(SUSPEND, 0)) AS SUSPEND, ");
            stb.append("     SUM(VALUE(MOURNING, 0)) AS MOURNING, ");
            stb.append("     SUM(VALUE(ABROAD, 0)) AS ABROAD, ");
            stb.append("     SUM(VALUE(SICK, 0)) AS SICK, ");
            stb.append("     SUM(VALUE(NOTICE, 0)) AS NOTICE, ");
            stb.append("     SUM(VALUE(NONOTICE, 0)) AS NONOTICE, ");
            stb.append("     SUM(VALUE(LATE, 0)) AS LATE, ");
            stb.append("     SUM(VALUE(EARLY, 0)) AS EARLY, ");
            stb.append("     SUM(VALUE(TOCHU_KEKKA, 0)) AS TOCHU_KEKKA     ");
            stb.append(" FROM ");
            stb.append("     V_ATTEND_SEMES_DAT ");
            stb.append(" WHERE ");
            stb.append("     COPYCD = '0' ");
            stb.append("     AND YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND SCHREGNO = '" + _schregno + "' ");
            stb.append(" GROUP BY ");
            stb.append("     SEMESTER ");

            return stb.toString();
        }

        public void setScoreData(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getScoreSql();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String classCd = rs.getString("CLASSCD");
                    final String schoolKind = rs.getString("SCHOOL_KIND");
                    final String curriculumCd = rs.getString("CURRICULUM_CD");
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final String className = rs.getString("CLASSNAME");
                    final String subclassName = rs.getString("SUBCLASSNAME");
                    final String score1 = rs.getString("SCORE1");
                    final String score2 = rs.getString("SCORE2");
                    final String score3 = rs.getString("SCORE3");
                    final String score9 = rs.getString("SCORE9");
                    final String avg1 = rs.getString("AVG1");
                    final String avg2 = rs.getString("AVG2");
                    final String avg3 = rs.getString("AVG3");
                    final String avg9 = rs.getString("AVG9");
                    final String valueDi1 = rs.getString("VALUE_DI1");
                    final String valueDi2 = rs.getString("VALUE_DI2");
                    final String valueDi3 = rs.getString("VALUE_DI3");
                    final String valueDi9 = rs.getString("VALUE_DI9");
                    final String d065_name = StringUtils.defaultString(rs.getString("D065_NAME"));

                    final String subclassKey = classCd + schoolKind + curriculumCd + subclassCd;

                    ClassMst classMst = null;
                    if (_classMstMap.containsKey(classCd)) {
                        classMst = _classMstMap.get(classCd);
                    } else {
                        classMst = new ClassMst(classCd, className);
                        _classMstMap.put(classCd, classMst);
                        _classCdOrder.add(classCd);
                    }

                    Subclass subclass = null;
                    if (_subclassMap.containsKey(subclassKey)) {
                        subclass = _subclassMap.get(subclassKey);
                    } else {
                        subclass = new Subclass(classCd, curriculumCd, subclassCd, subclassName);
                        _subclassMap.put(subclassKey, subclass);
                        classMst._subclassKeyOrder.add(subclassKey);
                    }
                    subclass._score = new Score(subclassKey, score1, score2, score3, score9, avg1, avg2, avg3, avg9, valueDi1, valueDi2, valueDi3, valueDi9, d065_name);
                }

                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private String getScoreSql() {
            final StringBuffer stb = new StringBuffer();
            //※指定期以下を出力するよう、結合条件でSEMESTERを利用して制限をするようにしているので注意。
            stb.append(" WITH RANK_SDIV AS ( ");
            stb.append(" SELECT DISTINCT ");
            stb.append("  T1.YEAR, ");
            stb.append("  T1.SCHREGNO, ");
            stb.append("  T1.CLASSCD, ");
            stb.append("  T1.SCHOOL_KIND, ");
            stb.append("  T1.SUBCLASSCD, ");
            stb.append("  T1.CURRICULUM_CD ");
            stb.append(" FROM ");
            stb.append("  RECORD_RANK_SDIV_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("  T1.YEAR = '" + _param._ctrlYear + "' ");
            if (!_param._semes9Flg) {
            	stb.append("  AND T1.SEMESTER <= '" + _param._semester + "' ");
            }
            stb.append("  AND T1.SCHREGNO = '" + _schregno + "' ");
            stb.append("  AND T1.CLASSCD <= '" + SELECT_CLASSCD_UNDER + "' ");
            stb.append("  AND T1.SUBCLASSCD NOT LIKE '50%' ");
            stb.append("  AND T1.TESTKINDCD = '99' AND T1.TESTITEMCD = '00' AND T1.SCORE_DIV IN ('08','09') ");
            stb.append(" UNION ");
            stb.append(" SELECT DISTINCT ");
            stb.append("  T1.YEAR, ");
            stb.append("  T1.SCHREGNO, ");
            stb.append("  T1.CLASSCD, ");
            stb.append("  T1.SCHOOL_KIND, ");
            stb.append("  T1.SUBCLASSCD, ");
            stb.append("  T1.CURRICULUM_CD ");
            stb.append(" FROM ");
            stb.append("  RECORD_SCORE_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("  T1.YEAR = '" + _param._ctrlYear + "' ");
            if (!_param._semes9Flg) {
            	stb.append("  AND T1.SEMESTER <= '" + _param._semester + "' ");
            }
            stb.append("  AND T1.SCHREGNO = '" + _schregno + "' ");
            stb.append("  AND T1.CLASSCD <= '" + SELECT_CLASSCD_UNDER + "' ");
            stb.append("  AND T1.SUBCLASSCD NOT LIKE '50%' ");
            stb.append("  AND T1.TESTKINDCD = '99' AND T1.TESTITEMCD = '00' AND T1.SCORE_DIV IN ('08','09') ");
            stb.append("  AND T1.VALUE_DI IS NOT NULL ");
            stb.append(" ORDER BY ");
            stb.append("  CLASSCD, ");
            stb.append("  SCHOOL_KIND, ");
            stb.append("  SUBCLASSCD, ");
            stb.append("  CURRICULUM_CD ");
            stb.append(" ), SUBCLSDAT AS ( ");
            stb.append(" SELECT ");
            stb.append("  T1.YEAR, ");
            stb.append("  T2.GRADE, ");
            stb.append("  T2.SCHREGNO, ");
            stb.append("  T1.CLASSCD, ");
            stb.append("  T1.SCHOOL_KIND, ");
            stb.append("  T1.SUBCLASSCD, ");
            stb.append("  T1.CURRICULUM_CD, ");
            stb.append("  VALUE(T5.CLASSABBV, T5.CLASSORDERNAME2, T5.CLASSNAME) AS CLASSNAME, ");
            stb.append("  VALUE(T4.SUBCLASSORDERNAME2, T4.SUBCLASSNAME) AS SUBCLASSNAME, ");
            stb.append("  T5.SHOWORDER3 AS CLASS_SHOWORDER, ");
            stb.append("  T4.SHOWORDER3 AS SUBCLASS_SHOWORDER, ");
            stb.append("  CASE WHEN SAKI.COMBINED_CLASSCD       IS NOT NULL THEN SAKI.COMBINED_CLASSCD       ELSE T1.CLASSCD       END AS COMBINED_CLASSCD, ");
            stb.append("  CASE WHEN SAKI.COMBINED_SCHOOL_KIND   IS NOT NULL THEN SAKI.COMBINED_SCHOOL_KIND   ELSE T1.SCHOOL_KIND   END AS COMBINED_SCHOOL_KIND, ");
            stb.append("  CASE WHEN SAKI.COMBINED_CURRICULUM_CD IS NOT NULL THEN SAKI.COMBINED_CURRICULUM_CD ELSE T1.CURRICULUM_CD END AS COMBINED_CURRICULUM_CD, ");
            stb.append("  CASE WHEN SAKI.COMBINED_SUBCLASSCD    IS NOT NULL THEN SAKI.COMBINED_SUBCLASSCD    ELSE T1.SUBCLASSCD    END AS COMBINED_SUBCLASSCD ");
            stb.append(" FROM ");
            stb.append("  RANK_SDIV T1 ");
            stb.append("  INNER JOIN SCHREG_REGD_DAT T2 ");
            stb.append("    ON T2.YEAR = T1.YEAR ");
            stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("    AND T2.SEMESTER <= '" + _param._semester + "' ");
            stb.append("  LEFT JOIN SUBCLASS_MST T4 ");
            stb.append("    ON T4.CLASSCD = T1.CLASSCD ");
            stb.append("    AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("    AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("    AND T4.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("  LEFT JOIN CLASS_MST T5 ");
            stb.append("    ON T5.CLASSCD = T1.CLASSCD ");
            stb.append("    AND T5.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("  LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT SAKI ");
            stb.append("     ON SAKI.YEAR = T1.YEAR ");
            stb.append("    AND SAKI.ATTEND_CLASSCD       = T1.CLASSCD ");
            stb.append("    AND SAKI.ATTEND_SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("    AND SAKI.ATTEND_CURRICULUM_CD = T1.SUBCLASSCD ");
            stb.append("    AND SAKI.ATTEND_SUBCLASSCD    = T1.CURRICULUM_CD ");
            stb.append(" WHERE ");
            stb.append("  T1.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("  AND T1.CLASSCD <= '" + SELECT_CLASSCD_UNDER + "' ");
            stb.append("  AND T1.SUBCLASSCD NOT LIKE '50%' ");
            stb.append(" GROUP BY ");
            stb.append("  T1.YEAR, ");
            stb.append("  T2.GRADE, ");
            stb.append("  T2.SCHREGNO, ");
            stb.append("  T1.CLASSCD, ");
            stb.append("  T1.SCHOOL_KIND, ");
            stb.append("  T1.SUBCLASSCD, ");
            stb.append("  T1.CURRICULUM_CD, ");
            stb.append("  VALUE(T5.CLASSABBV, T5.CLASSORDERNAME2, T5.CLASSNAME), ");
            stb.append("  VALUE(T4.SUBCLASSORDERNAME2, T4.SUBCLASSNAME), ");
            stb.append("  T5.SHOWORDER3, ");
            stb.append("  T4.SHOWORDER3, ");
            stb.append("  COMBINED_CLASSCD, ");
            stb.append("  COMBINED_SCHOOL_KIND, ");
            stb.append("  COMBINED_CURRICULUM_CD, ");
            stb.append("  COMBINED_SUBCLASSCD ");
            stb.append(" ORDER BY ");
            stb.append("  T1.YEAR, ");
            stb.append("  T2.SCHREGNO ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("  T1.YEAR, ");
            stb.append("  T1.SCHREGNO, ");
            stb.append("  T1.CLASSCD, ");
            stb.append("  T1.SCHOOL_KIND, ");
            stb.append("  T1.CURRICULUM_CD, ");
            stb.append("  T1.SUBCLASSCD, ");
            stb.append("  T1.CLASSNAME, ");
            stb.append("  T1.SUBCLASSNAME, ");
//            stb.append("  CASE WHEN T6.CALCULATE_CREDIT_FLG = '2' THEN T6.CREDITS ELSE T5.CREDITS END AS CREDITS, ");
            stb.append("  T2_1.SCORE AS SCORE1, ");
            stb.append("  T2_1.AVG   AS AVG1, ");
            stb.append("  T2_1S.VALUE_DI AS VALUE_DI1, ");
            stb.append("  T2_2.SCORE AS SCORE2, ");
            stb.append("  T2_2.AVG   AS AVG2, ");
            stb.append("  T2_2S.VALUE_DI AS VALUE_DI2, ");
            stb.append("  T2_3.SCORE AS SCORE3, ");
            stb.append("  T2_3.AVG   AS AVG3, ");
            stb.append("  T2_3S.VALUE_DI AS VALUE_DI3, ");
            stb.append("  T2_9.SCORE AS SCORE9, ");
            stb.append("  T2_9.AVG   AS AVG9, ");
            stb.append("  T2_9S.VALUE_DI AS VALUE_DI9, ");
            stb.append("  D065.NAME1 AS D065_NAME ");
            stb.append(" FROM ");
            stb.append("  SUBCLSDAT T1 ");
            stb.append("  INNER JOIN SUBCLASS_YDAT SUBY ON SUBY.YEAR = '" + _param._ctrlYear + "' AND SUBY.CLASSCD = T1.CLASSCD AND SUBY.SCHOOL_KIND = T1.SCHOOL_KIND AND SUBY.CURRICULUM_CD = T1.CURRICULUM_CD AND SUBY.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("  LEFT JOIN RECORD_RANK_SDIV_DAT T2_1 ");
            stb.append("    ON T2_1.YEAR = T1.YEAR ");
            stb.append("    AND T2_1.SEMESTER <= '" + _param._semester + "' ");
            stb.append("    AND T2_1.SEMESTER = '1' AND T2_1.TESTKINDCD = '99' AND T2_1.TESTITEMCD = '00' AND T2_1.SCORE_DIV = '08' ");
            stb.append("    AND T2_1.CLASSCD       = T1.CLASSCD ");
            stb.append("    AND T2_1.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("    AND T2_1.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("    AND T2_1.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("    AND T2_1.SCHREGNO      = T1.SCHREGNO ");
            stb.append("  LEFT JOIN RECORD_RANK_SDIV_DAT T2_2 ");
            stb.append("    ON T2_2.YEAR = T1.YEAR ");
            stb.append("    AND T2_2.SEMESTER <= '" + _param._semester + "' ");
            stb.append("    AND T2_2.SEMESTER = '2' AND T2_2.TESTKINDCD = '99' AND T2_2.TESTITEMCD = '00' AND T2_2.SCORE_DIV = '08' ");
            stb.append("    AND T2_2.CLASSCD       = T1.CLASSCD ");
            stb.append("    AND T2_2.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("    AND T2_2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("    AND T2_2.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("    AND T2_2.SCHREGNO      = T1.SCHREGNO ");
            stb.append("  LEFT JOIN RECORD_RANK_SDIV_DAT T2_3 "); //合併先科目を取得
            stb.append("    ON T2_3.YEAR = T1.YEAR ");
            stb.append("    AND T2_3.SEMESTER = '9' AND T2_3.TESTKINDCD = '99' AND T2_3.TESTITEMCD = '00' AND T2_3.SCORE_DIV = '08' ");
            stb.append("    AND T2_3.CLASSCD       = T1.COMBINED_CLASSCD ");
            stb.append("    AND T2_3.SCHOOL_KIND   = T1.COMBINED_SCHOOL_KIND ");
            stb.append("    AND T2_3.CURRICULUM_CD = T1.COMBINED_CURRICULUM_CD ");
            stb.append("    AND T2_3.SUBCLASSCD    = T1.COMBINED_SUBCLASSCD ");
            stb.append("    AND T2_3.SCHREGNO      = T1.SCHREGNO ");
            stb.append("  LEFT JOIN RECORD_RANK_SDIV_DAT T2_9 "); //合併先科目を取得
            stb.append("    ON T2_9.YEAR = T1.YEAR ");
            stb.append("    AND T2_9.SEMESTER = '9' AND T2_9.TESTKINDCD = '99' AND T2_9.TESTITEMCD = '00' AND T2_9.SCORE_DIV = '09' ");
            stb.append("    AND T2_9.CLASSCD       = T1.COMBINED_CLASSCD ");
            stb.append("    AND T2_9.SCHOOL_KIND   = T1.COMBINED_SCHOOL_KIND ");
            stb.append("    AND T2_9.CURRICULUM_CD = T1.COMBINED_CURRICULUM_CD ");
            stb.append("    AND T2_9.SUBCLASSCD    = T1.COMBINED_SUBCLASSCD ");
            stb.append("    AND T2_9.SCHREGNO      = T1.SCHREGNO ");
            stb.append("  LEFT JOIN RECORD_SCORE_DAT T2_1S ");
            stb.append("    ON T2_1S.YEAR = T1.YEAR ");
            stb.append("    AND T2_1S.SEMESTER <= '" + _param._semester + "' ");
            stb.append("    AND T2_1S.SEMESTER = '1' AND T2_1S.TESTKINDCD = '99' AND T2_1S.TESTITEMCD = '00' AND T2_1S.SCORE_DIV = '08' ");
            stb.append("    AND T2_1S.CLASSCD       = T1.CLASSCD ");
            stb.append("    AND T2_1S.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("    AND T2_1S.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("    AND T2_1S.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("    AND T2_1S.SCHREGNO      = T1.SCHREGNO ");
            stb.append("  LEFT JOIN RECORD_SCORE_DAT T2_2S ");
            stb.append("    ON T2_2S.YEAR = T1.YEAR ");
            stb.append("    AND T2_2S.SEMESTER <= '" + _param._semester + "' ");
            stb.append("    AND T2_2S.SEMESTER = '2' AND T2_2S.TESTKINDCD = '99' AND T2_2S.TESTITEMCD = '00' AND T2_2S.SCORE_DIV = '08' ");
            stb.append("    AND T2_2S.CLASSCD       = T1.CLASSCD ");
            stb.append("    AND T2_2S.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("    AND T2_2S.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("    AND T2_2S.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("    AND T2_2S.SCHREGNO      = T1.SCHREGNO ");
            stb.append("  LEFT JOIN RECORD_SCORE_DAT T2_3S "); //合併先科目を取得
            stb.append("    ON T2_3S.YEAR = T1.YEAR ");
            stb.append("    AND T2_3S.SEMESTER = '9' AND T2_3S.TESTKINDCD = '99' AND T2_3S.TESTITEMCD = '00' AND T2_3S.SCORE_DIV = '08' ");
            stb.append("    AND T2_3S.CLASSCD       = T1.COMBINED_CLASSCD ");
            stb.append("    AND T2_3S.SCHOOL_KIND   = T1.COMBINED_SCHOOL_KIND ");
            stb.append("    AND T2_3S.CURRICULUM_CD = T1.COMBINED_CURRICULUM_CD ");
            stb.append("    AND T2_3S.SUBCLASSCD    = T1.COMBINED_SUBCLASSCD ");
            stb.append("    AND T2_3S.SCHREGNO      = T1.SCHREGNO ");
            stb.append("  LEFT JOIN RECORD_SCORE_DAT T2_9S "); //合併先科目を取得
            stb.append("    ON T2_9S.YEAR = T1.YEAR ");
            stb.append("    AND T2_9S.SEMESTER = '9' AND T2_9S.TESTKINDCD = '99' AND T2_9S.TESTITEMCD = '00' AND T2_9S.SCORE_DIV = '09' ");
            stb.append("    AND T2_9S.CLASSCD       = T1.COMBINED_CLASSCD ");
            stb.append("    AND T2_9S.SCHOOL_KIND   = T1.COMBINED_SCHOOL_KIND ");
            stb.append("    AND T2_9S.CURRICULUM_CD = T1.COMBINED_CURRICULUM_CD ");
            stb.append("    AND T2_9S.SUBCLASSCD    = T1.COMBINED_SUBCLASSCD ");
            stb.append("    AND T2_9S.SCHREGNO      = T1.SCHREGNO ");
            stb.append("  LEFT JOIN SCHREG_REGD_DAT COURSE_REGD ");
            stb.append("    ON COURSE_REGD.SCHREGNO = T1.SCHREGNO ");
            stb.append("    AND COURSE_REGD.YEAR = T1.YEAR ");
            stb.append("    AND COURSE_REGD.SEMESTER = '" + _param._semester + "' ");
            stb.append("  LEFT JOIN NAME_MST D065 ");
            stb.append("     ON D065.NAMECD1 = 'D065' ");
            stb.append("    AND D065.NAME1   = T1.CLASSCD ||'-'|| T1.SCHOOL_KIND ||'-'|| T1.CURRICULUM_CD ||'-'|| T1.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("   T1.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("   AND T1.SCHREGNO = '"+ _schregno+ "' ");
            stb.append(" ORDER BY ");
            stb.append("   T1.CLASS_SHOWORDER, CLASSCD, T1.SUBCLASS_SHOWORDER, CURRICULUM_CD, SUBCLASSCD ");

            return stb.toString();
        }

        public void setTotalScoreData(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getTotalScoreSql();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String score1 = rs.getString("SCORE1");
                    final String score2 = rs.getString("SCORE2");
                    final String score3 = rs.getString("SCORE3");
                    final String score9 = rs.getString("SCORE9");
                    final String avg1 = roundHalfUp(rs.getBigDecimal("AVG1"));
                    final String avg2 = roundHalfUp(rs.getBigDecimal("AVG2"));
                    final String avg3 = roundHalfUp(rs.getBigDecimal("AVG3"));
                    final String avg9 = roundHalfUp(rs.getBigDecimal("AVG9"));
                    _totalscore = new TotalInfo(score1, score2, score3, score9, avg1, avg2, avg3, avg9);
                }

               db2.commit();
            } finally {
               DbUtils.closeQuietly(null, ps, rs);
            }
        }
        private String roundHalfUp(final BigDecimal bd) {
            return null == bd ? null : bd.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
        }

        private String getTotalScoreSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" select ");
            stb.append("  T1.YEAR, ");
            stb.append("  T2_1.SCORE      AS SCORE1, ");
            stb.append("  T2_1.AVG        AS AVG1, ");
            stb.append("  T2_2.SCORE      AS SCORE2, ");
            stb.append("  T2_2.AVG        AS AVG2, ");
            stb.append("  T2_3.SCORE      AS SCORE3, ");
            stb.append("  T2_3.AVG        AS AVG3, ");
            stb.append("  T2_9.SCORE      AS SCORE9, ");
            stb.append("  T2_9.AVG        AS AVG9 ");
            stb.append(" from ");
            stb.append(" schreg_regd_dat T1 ");
            stb.append(" LEFT JOIN RECORD_RANK_SDIV_DAT T2_1 ");
            stb.append("   ON T2_1.SCHOOL_KIND = '" + _param._schoolKind + "' ");
            stb.append("   AND T2_1.YEAR = T1.YEAR ");
            stb.append("   AND T2_1.SEMESTER || T2_1.TESTKINDCD || T2_1.TESTITEMCD || T2_1.SCORE_DIV = '" + SDIV1990008 + "' ");
            stb.append("   AND T2_1.CLASSCD = '99' ");
            stb.append("   AND T2_1.CURRICULUM_CD = '99' ");
            stb.append("   AND T2_1.SUBCLASSCD = '" +ALL9+ "' ");
            stb.append("   AND T2_1.SCHREGNO = T1.SCHREGNO ");
            stb.append(" LEFT JOIN RECORD_RANK_SDIV_DAT T2_2 ");
            stb.append("   ON T2_2.SCHOOL_KIND = '" + _param._schoolKind + "' ");
            stb.append("   AND T2_2.YEAR = T1.YEAR ");
            stb.append("   AND T2_2.SEMESTER || T2_2.TESTKINDCD || T2_2.TESTITEMCD || T2_2.SCORE_DIV = '" + SDIV2990008 + "' ");
            stb.append("   AND T2_2.CLASSCD = '99' ");
            stb.append("   AND T2_2.CURRICULUM_CD = '99' ");
            stb.append("   AND T2_2.SUBCLASSCD = '" +ALL9+ "' ");
            stb.append("   AND T2_2.SCHREGNO = T1.SCHREGNO ");
            stb.append(" LEFT JOIN RECORD_RANK_SDIV_DAT T2_3 ");
            stb.append("   ON T2_3.SCHOOL_KIND = '" + _param._schoolKind + "' ");
            stb.append("   AND T2_3.YEAR = T1.YEAR ");
            stb.append("   AND T2_3.SEMESTER || T2_3.TESTKINDCD || T2_3.TESTITEMCD || T2_3.SCORE_DIV = '" + SDIV9990008 + "' ");
            stb.append("   AND T2_3.CLASSCD = '99' ");
            stb.append("   AND T2_3.CURRICULUM_CD = '99' ");
            stb.append("   AND T2_3.SUBCLASSCD = '" +ALL9+ "' ");
            stb.append("   AND T2_3.SCHREGNO = T1.SCHREGNO ");
            stb.append(" LEFT JOIN RECORD_RANK_SDIV_DAT T2_9 ");
            stb.append("   ON T2_9.SCHOOL_KIND = '" + _param._schoolKind + "' ");
            stb.append("   AND T2_9.YEAR = T1.YEAR ");
            stb.append("   AND T2_9.SEMESTER || T2_9.TESTKINDCD || T2_9.TESTITEMCD || T2_9.SCORE_DIV = '" + SDIV9990009 + "' ");
            stb.append("   AND T2_9.CLASSCD = '99' ");
            stb.append("   AND T2_9.CURRICULUM_CD = '99' ");
            stb.append("   AND T2_9.SUBCLASSCD = '" +ALL9+ "' ");
            stb.append("   AND T2_9.SCHREGNO = T1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("  T1.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("  AND T1.SEMESTER = '" + _param._semester + "' ");
            stb.append("  AND T1.SCHREGNO = '" + _schregno + "' ");

            return stb.toString();
        }

        public void setHreportremark(DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = " SELECT SEMESTER, SPECIALACTREMARK, COMMUNICATION FROM HREPORTREMARK_DAT WHERE YEAR = '" + _param._ctrlYear + "' AND SCHREGNO = '" + _schregno + "' ";
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String communication = rs.getString("COMMUNICATION");
                    final String specialactremark = rs.getString("SPECIALACTREMARK");
                    HREPORTREMARKDAT addwk = new HREPORTREMARKDAT(semester, communication, specialactremark);
                    _hreportremarkMap.put(rs.getString("SEMESTER"), addwk);
                }

                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
        }
    }

    private class TotalInfo {
        final String _score1;
        final String _score2;
        final String _score3;
        final String _score9;
        final String _avg1;
        final String _avg2;
        final String _avg3;
        final String _avg9;
        TotalInfo(final String score1, final String score2, final String score3, final String score9, final String avg1, final String avg2, final String avg3, final String avg9) {
            _score1 = score1;
            _score2 = score2;
            _score3 = score3;
            _score9 = score9;
            _avg1 = avg1;
            _avg2 = avg2;
            _avg3 = avg3;
            _avg9 = avg9;
        }
    }
    private class HREPORTREMARKDAT {
        final String _semester;
        final String _communication;
        final String _specialactremark;

        HREPORTREMARKDAT (final String semester, final String communication, final String specialactremark) {
            _semester = semester;
            _communication = communication;
            _specialactremark = specialactremark;
        }
    }

    private class ClassMst {
        final String _classCd;
        final String _className;
        final List<String> _subclassKeyOrder = new ArrayList();
        public ClassMst(
                final String classCd,
                final String className
                ) {
            _classCd = classCd;
            _className = className;
        }

    }

    private class Subclass {
        final String _classCd;
        final String _curriculumCd;
        final String _subclassCd;
        final String _subclassName;
        Score _score;
        Map<String, Score> _valueDiMap = new HashMap();
        public Subclass(
                final String classCd,
                final String curriculumCd,
                final String subclassCd,
                final String subclassName
                ) {
            _classCd = classCd;
            _curriculumCd = curriculumCd;
            _subclassCd = subclassCd;
            _subclassName = subclassName;
        }

        public String toString() {
        	return "Subclass(" + _classCd + _curriculumCd + _subclassCd + ", " + _score + ")";
        }
    }

    private class Score {
        final String _clskeycd;
        final String _score1;
        final String _score2;
        final String _score3;
        final String _score9;
        final String _avg1;
        final String _avg2;
        final String _avg3;
        final String _avg9;
        final String _valueDi1;
        final String _valueDi2;
        final String _valueDi3;
        final String _valueDi9;
        final String _d065_name;
        public Score(
                final String clskeycd,
                final String score1,
                final String score2,
                final String score3,
                final String score9,
                final String avg1,
                final String avg2,
                final String avg3,
                final String avg9,
                final String valueDi1,
                final String valueDi2,
                final String valueDi3,
                final String valueDi9,
                final String d065_name
        ) {
            _clskeycd = clskeycd;
            _score1 = score1;
            _score2 = score2;
            _score3 = score3;
            _score9 = score9;
            _avg1 = avg1;
            _avg2 = avg2;
            _avg3 = avg3;
            _avg9 = avg9;
            _valueDi1 = valueDi1;
            _valueDi2 = valueDi2;
            _valueDi3 = valueDi3;
            _valueDi9 = valueDi9;
            _d065_name = d065_name;
        }
        @Override
        public String toString() {
        	return "Score(1=" + _score1 + ", 2=" + _score2 + ", 3=" + _score3 + ", 9=" + _score9 + ")";
        }
    }

//    private static class TestItemMst {
//        final String _semester;
//        final String _testCd;
//        final String _testName;
//        int _cnt = 0;
//        int _totalScore = 0;
//        String _assessLow = "";
//        String _assessHigh = "";
//        public TestItemMst(
//                final String semester,
//                final String testCd,
//                final String testName
//        ) throws SQLException {
//            _semester = semester;
//            _testCd = testCd;
//            _testName = testName;
//        }
//
//    }

    private static class SemesterMst {
        final String _semester;
        final String _semesterName;
        public SemesterMst(
                final String semester,
                final String semesterName
        ) throws SQLException {
            _semester = semester;
            _semesterName = semesterName;
        }

    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 76061 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    private static class SCHOOLINFO {
        final String _schoolname;
        final String _principalname;
        public SCHOOLINFO(final String schoolname, final String principalname) {
            _schoolname = schoolname;
            _principalname = principalname;
        }
    }

    /** パラメータクラス */
    private static class Param {
        final String _semester;
        final String _disp;
        final String _grade;
        final String _gradeHrClass;
        final String[] _categorySelected;
        final String _sqlInstate;
        final String _summaryDate;
        final String _descDate;
        final String _descDatePrint;
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _prgid;
        final String _usePrgSchoolkind;
        final String _selectschoolkind;
        final String _useschoolKindfield;
        final String _paraSchoolkind;
        final String _schoolcd;
        final String _printLogStaffcd;
        final String _semesterName;
        final String _printFooter;
        final boolean _semes2Flg;
        final boolean _semes3Flg;
        final boolean _semes9Flg;
        final String _nendo;
        final Map<String, SemesterMst> _semesterMstMap;
        final String _schoolMstSemOffDays;
        final String _schoolKind;
        final SCHOOLINFO _schoolInfo;
        final boolean _hasSchoolMstSchoolKind;
        Map<String, List<String>> _combinedSubclassAttendSubclassListMap;
        Map<String, String> _attendSubclassCombinedSubclassMap;
        List<String> _combinedSubclassList;
        Map<String, String> _attendSubclassabbvMap;
        Map<String, String> _combinedSubclassabbvMap;

        final String _documentroot;
        final String _imagepath;
        String _imgFIleName;
        final String _whiteSpaceImagePath;
        final String _schoolLogoPath;

        final String _z010Name1;
        final Map<String, String> _d001Map;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _semester = request.getParameter("SEMESTER");
            _disp = request.getParameter("DISP");
            _grade = request.getParameter("GRADE");
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _summaryDate = request.getParameter("SUMMARY_DATE").replace('/', '-');
            _descDate = request.getParameter("DESC_DATE").replace('/', '-');
            _descDatePrint = request.getParameter("DESC_DATE_PRINT");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _prgid = request.getParameter("PRGID");
            _usePrgSchoolkind = request.getParameter("use_prg_schoolkind");
            _selectschoolkind = request.getParameter("selectSchoolKind");
            _useschoolKindfield = request.getParameter("useSchool_KindField");
            _paraSchoolkind = request.getParameter("SCHOOLKIND");
            _schoolcd = request.getParameter("SCHOOLCD");
            _printLogStaffcd = request.getParameter("PRINT_LOG_STAFFCD");
            _semesterMstMap = getSemesterMstMap(db2);
            _semesterName = _semesterMstMap.get(_semester)._semesterName;
            _printFooter = request.getParameter("PRINT_FOOTER");
            _hasSchoolMstSchoolKind = KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND");
            String grade = _grade;
            if ("2".equals(_disp)) {
                grade = _gradeHrClass.substring(0, 2);
            }
            _schoolKind = getSchoolKind(db2, grade);
            boolean isLastSemester = false;
            try {
            	final Map paramMap = new HashMap();
            	if (KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND")) {
            		paramMap.put("SCHOOL_KIND", _schoolKind);
            	}
            	KNJSchoolMst _knjSchoolMst = new KNJSchoolMst(db2, _ctrlYear, paramMap);
            	isLastSemester = _semester.equals(_knjSchoolMst._semesterDiv);
            } catch (Exception e) {
            	log.error("exception!", e);
            }
            _semes2Flg = "2".equals(_semester) || "3".equals(_semester) || "9".equals(_semester) || isLastSemester ? true : false;
            _semes3Flg = "3".equals(_semester) || "9".equals(_semester) || isLastSemester ? true : false;
            _semes9Flg = "9".equals(_semester) || isLastSemester ? true : false;
            _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_ctrlYear)) + "年度";

            String setInstate = "";
            String sep = "";
            for (int i = 0; i < _categorySelected.length; i++) {
                final String selectVal = _categorySelected[i];
                final String[] setVal = StringUtils.split(selectVal, "-");
                setInstate += sep + "'" + setVal[0] + "'";
                sep = ",";
            }
            _sqlInstate = setInstate;
            _schoolMstSemOffDays = getSemOffDays(db2);
            _schoolInfo = getSchoolName(db2);

            _documentroot = request.getParameter("DOCUMENTROOT");
            final String sqlControlMst = " SELECT IMAGEPATH FROM CONTROL_MST WHERE CTRL_NO = '01' ";
            _imagepath = KnjDbUtils.getOne(KnjDbUtils.query(db2, sqlControlMst));
            _whiteSpaceImagePath = getImageFilePath("whitespace.png");
            _schoolLogoPath = getImageFilePath("SCHOOLLOGO.jpg");

            _z010Name1 = getNameMstZ010(db2);
            _d001Map = getNameMstD001(db2);
            setCombinedSubclassAttendSubclassListMap(db2);
        }

        private void setCombinedSubclassAttendSubclassListMap(final DB2UDB db2) {
        	String sql = "";
        	sql += " SELECT ";
        	sql += "   COMBINED_CLASSCD || COMBINED_SCHOOL_KIND || COMBINED_CURRICULUM_CD || COMBINED_SUBCLASSCD AS COMBINED_SUBCLASS_KEY ";
        	sql += " , ATTEND_CLASSCD || ATTEND_SCHOOL_KIND || ATTEND_CURRICULUM_CD || ATTEND_SUBCLASSCD AS ATTEND_SUBCLASS_KEY ";
        	sql += " , L1.SUBCLASSABBV AS COMBINED_SUBCLASSABBV ";
        	sql += " , L2.SUBCLASSABBV AS ATTEND_SUBCLASSABBV ";
        	sql += " FROM SUBCLASS_REPLACE_COMBINED_DAT T1 ";
        	sql += " LEFT JOIN SUBCLASS_MST L1 ON L1.CLASSCD = COMBINED_CLASSCD AND L1.SCHOOL_KIND = COMBINED_SCHOOL_KIND AND L1.CURRICULUM_CD = COMBINED_CURRICULUM_CD AND L1.SUBCLASSCD = COMBINED_SUBCLASSCD ";
        	sql += " LEFT JOIN SUBCLASS_MST L2 ON L2.CLASSCD = ATTEND_CLASSCD AND L2.SCHOOL_KIND = ATTEND_SCHOOL_KIND AND L2.CURRICULUM_CD = ATTEND_CURRICULUM_CD AND L2.SUBCLASSCD = ATTEND_SUBCLASSCD ";
        	sql += " WHERE YEAR = '" + _ctrlYear + "' ";
        	sql += " ORDER BY ";
        	sql += "     COMBINED_CLASSCD || COMBINED_SCHOOL_KIND || COMBINED_CURRICULUM_CD || COMBINED_SUBCLASSCD ";
        	sql += "   , ATTEND_CLASSCD || ATTEND_SCHOOL_KIND || ATTEND_CURRICULUM_CD || ATTEND_SUBCLASSCD ";

        	_combinedSubclassAttendSubclassListMap = new TreeMap<String, List<String>>();
        	_attendSubclassCombinedSubclassMap = new HashMap<String, String>();
        	_attendSubclassabbvMap = new HashMap<String, String>();
        	_combinedSubclassabbvMap = new HashMap<String, String>();
        	for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {
        		final String combinedSubclassKey = KnjDbUtils.getString(row, "COMBINED_SUBCLASS_KEY");
        		final String attendSubclassKey = KnjDbUtils.getString(row, "ATTEND_SUBCLASS_KEY");
				getMappedList(_combinedSubclassAttendSubclassListMap, combinedSubclassKey).add(attendSubclassKey);
        		_attendSubclassCombinedSubclassMap.put(attendSubclassKey, combinedSubclassKey);
        		_attendSubclassabbvMap.put(attendSubclassKey, KnjDbUtils.getString(row, "ATTEND_SUBCLASSABBV"));
        		_combinedSubclassabbvMap.put(combinedSubclassKey, KnjDbUtils.getString(row, "COMBINED_SUBCLASSABBV"));
        	}
        	_combinedSubclassList = new ArrayList<String>(_attendSubclassCombinedSubclassMap.values());

		}

		private String getNameMstZ010(final DB2UDB db2) throws SQLException {
            String retStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00'";
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    retStr = rs.getString("NAME1");
                }
                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retStr;
        }

		private static <T, K> List<T> getMappedList(final Map<K, List<T>> map, final K key1) {
	        if (!map.containsKey(key1)) {
	            map.put(key1, new ArrayList<T>());
	        }
	        return map.get(key1);
	    }

        private String getSemesterName(final DB2UDB db2) throws SQLException {
            String retStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getSemester();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    retStr = rs.getString("SEMESTERNAME");
                }

                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retStr;
        }

        private String getSemester() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SEMESTERNAME ");
            stb.append(" FROM ");
            stb.append("     SEMESTER_MST ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _ctrlYear + "' ");
            stb.append("     AND SEMESTER = '" + _semester + "' ");
            return stb.toString();
        }

        private Map getSemesterMstMap(final DB2UDB db2) throws SQLException {
            final Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getSemesterMstSql();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String semesterName = rs.getString("SEMESTERNAME");
                    final SemesterMst semesterMst = new SemesterMst(semester, semesterName);
                    retMap.put(semester, semesterMst);
                }

                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retMap;
        }

        private String getSemesterMstSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SEMESTER, ");
            stb.append("     SEMESTERNAME ");
            stb.append(" FROM ");
            stb.append("     SEMESTER_MST ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _ctrlYear + "' ");
//            stb.append("     AND SEMESTER < '9' ");
            return stb.toString();
        }

        private String getSemOffDays(final DB2UDB db2) throws SQLException {
            String retStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getSemOffDays();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    retStr = rs.getString("SEM_OFFDAYS");
                }

                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retStr;
        }

        private String getSemOffDays() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     VALUE(SEM_OFFDAYS, '0') AS SEM_OFFDAYS ");
            stb.append(" FROM ");
            stb.append("     SCHOOL_MST ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _ctrlYear + "' ");
            if (_hasSchoolMstSchoolKind) {
                stb.append("     AND SCHOOL_KIND = '" + _schoolKind + "' ");
            }
            return stb.toString();
        }

        private String getSchoolKind(final DB2UDB db2, final String grade) throws SQLException {
            String retStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getSchoolKind(grade);
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    retStr = rs.getString("SCHOOL_KIND");
                }

                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retStr;
        }

        private String getSchoolKind(final String grade) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     GDAT.SCHOOL_KIND ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_GDAT GDAT ");
            stb.append(" WHERE ");
            stb.append("     GDAT.YEAR = '" + _ctrlYear + "' ");
            stb.append("     AND GDAT.GRADE = '" + grade + "' ");
            return stb.toString();
        }

        private SCHOOLINFO getSchoolName(final DB2UDB db2) throws SQLException {
            SCHOOLINFO retclswk = null;
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getSchoolNameSql();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String schname = rs.getString("SCHOOL_NAME");
                    final String princname = rs.getString("PRINCIPAL_NAME");
                    retclswk = new SCHOOLINFO(schname, princname);
                }

                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retclswk;
        }

        private String getSchoolNameSql() {
            final StringBuffer stb = new StringBuffer();
            final String certifKind = "J".equals(_schoolKind) ? "103" : "104";
            stb.append(" SELECT ");
            stb.append("     SCHOOL_NAME, ");
            stb.append("     PRINCIPAL_NAME ");
            stb.append(" FROM ");
            stb.append("     CERTIF_SCHOOL_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _ctrlYear + "' ");
            stb.append("     AND CERTIF_KINDCD = '" + certifKind + "' ");

            return stb.toString();
        }

        public String getImageFilePath(final String name) {
            final String path = _documentroot + "/" + (null == _imagepath || "".equals(_imagepath) ? "" : _imagepath + "/") + name;
            final boolean exists = new java.io.File(path).exists();
            log.info(" path " + path + " exists: " + exists);
            if (exists) {
                return path;
            }
            return null;
        }

        private Map<String, String> getNameMstD001(final DB2UDB db2) {
        	return KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, "SELECT NAMECD2, NAME1 FROM NAME_MST WHERE NAMECD1 = 'D001' "), "NAMECD2", "NAME1");
        }
    }
}

// eof

