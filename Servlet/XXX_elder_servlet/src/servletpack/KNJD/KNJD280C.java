package servletpack.KNJD;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.ServletException;
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
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 学校教育システム 賢者 [成績管理]  立志舎 成績詳細（生徒別）
 */
public class KNJD280C {

    private static final Log log = LogFactory.getLog(KNJD280C.class);

    private static final String SEMEALL = "9";
    private static final String SUBCLASSCD999999 = "999999";
    private static final String FROM_TO_MARK = "\uFF5E";
    private static final String TESTCD_HYOKA = "990008";
    private static final String TESTCD_GAKUNEN_HYOKA = "9" + TESTCD_HYOKA;
    private static final String TESTCD_GAKUNEN_HYOTEI = "9990009";

    private boolean _hasData;

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {
        Vrw32alp svf = null;
        try {
            svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            response.setContentType("application/pdf");

            outputPdf(svf, request);

        } catch (final Exception ex) {
            log.error("error! ", ex);
        } finally {
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
            svf.VrQuit();
        }
    }
    
    public void outputPdf(final Vrw32alp svf, final HttpServletRequest request) throws Exception {
        DB2UDB db2 = null;
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            // パラメータの取得
            Param param = createParam(request, db2);

            printMain(db2, svf, param);

        } catch (final Exception ex) {
            log.error("error! ", ex);
        } finally {
            try {
                db2.commit();
                db2.close();
            } catch (Exception ex) {
                log.error("db close error!", ex);
            }
        }
    }
    
    protected void printMain(final DB2UDB db2, final Vrw32alp svf, final Param param) {
        final List studentList = Student.getStudentList(db2, param);

		final String form = "KNJD280C.frm";
		final String ZENKI_CHUKAN   = "1010101";
		final String ZENKI_KIMATSU  = "1020101";
		final String KOUKI_CHUKAN   = "2010101";
		final String KOUKI_KIMATSU  = "2020101";
		final String[] semTestcds = new String[] {ZENKI_CHUKAN, ZENKI_KIMATSU, KOUKI_CHUKAN, KOUKI_KIMATSU};
		
		final Map subclasscdStudentListMap = new HashMap();
		for (final Iterator it = studentList.iterator(); it.hasNext();) {
			final Student student = (Student) it.next();
			for (final Iterator subit = student._subclassMap.keySet().iterator(); subit.hasNext();) {
				final String subclasscd = (String) subit.next();
				getMappedList(subclasscdStudentListMap, subclasscd).add(student);
			}
		}
		final int maxLine = 35;
		
		for (int i = 0; i < param._categorySelected.length; i++) {
			final String subclasscd = param._categorySelected[i];
			final List subclassAllStudentList = getMappedList(subclasscdStudentListMap, subclasscd);
			
			final SubclassMst mst = param.getSubclassMst(subclasscd);
			if (null == mst) {
				log.info(" not found subclass : " + subclasscd);
				continue;
			}
			
			final List pageList = getPageList(subclassAllStudentList, maxLine);
			for (int pi = 0; pi < pageList.size(); pi++) {
				final List subclasStudentList = (List) pageList.get(pi);

	    		svf.VrSetForm(form, 4);
				svf.VrsOut("TEST_NAME1", "中間"); // 試験名称 前期中間
				svf.VrsOut("TEST_NAME2", "期末"); // 試験名称 前期期末
				svf.VrsOut("TEST_NAME3", "中間"); // 試験名称 後期中間
				svf.VrsOut("TEST_NAME4", "期末"); // 試験名称 後期期末
	    		
	    		svf.VrsOut("TITLE", param._nendo + "　成績詳細　" + mst._subclassname); // タイトル

		    	for (int sti = 0; sti < maxLine; sti++) {
	    			if (sti < subclasStudentList.size()) {
			    		final Student student = (Student) subclasStudentList.get(sti);
			    		
			    		final Subclass subclass = student.getSubclass(subclasscd);

			    		svf.VrsOut("NAME" + (KNJ_EditEdit.getMS932ByteLength(student._name) <= 20 ? "1" : "2"), student._name); // 科目名

	    				if (param._isOutputDebug) {
	    					log.info(" student " + student._schregno + " (" + student._grade + student._hrClass + "-" + student._attendno + ") (zenki,kouki) = (" + (subclass._isZenki ? "1" : "0") + "," + (subclass._isKouki ? "1" : "0") + ") ");
	    				}
	    				
	    				svf.VrsOut("REPORT1", subclass._repoLimitCnt); // レポート規定数
	    				svf.VrsOut("REPORT2", subclass._rVal1); // レポート合格数
	    				
	    				svf.VrsOut("ATTEND1", subclass._schoolingLimitCnt); // 出席規定数
	    				if (null != subclass._subclassAttendance && null != subclass._subclassAttendance._attend) {
	    					svf.VrsOut("ATTEND2", subclass._subclassAttendance._attend.toString()); // 出席出席数
	    				}

	    				if (null != subclass._subclassAttendance && null != subclass._subclassAttendance._lesson) {
	    					svf.VrsOut("ATTEND3", subclass._subclassAttendance._lesson.toString()); // 出席時数
	    				}
	    				
	    				final List scoreList = new ArrayList();
						for (int si = 0; si < semTestcds.length; si++) {
							final String field = "TEST" + String.valueOf(si + 1);
							final String semtestcd = semTestcds[si];
							final String semester = semtestcd.substring(0, 1);
							if (subclass._isZenki && !"1".equals(semester)) {
								svf.VrAttribute(field, "Paint=(1,80,2),Bold=1");
							} else if (subclass._isKouki && "1".equals(semester)) {
								svf.VrAttribute(field, "Paint=(1,80,2),Bold=1");
							}
							if (semester.compareTo(param._semester) <= 0) {
								final String score = subclass.getScore(semtestcd);
								svf.VrsOut(field, score);
								if (null != score) {
									scoreList.add(score);
								}
							}
						}

						final String subclassRecordSemester = subclass._isZenki ? "1" : subclass._isKouki ? "2" : param._semester;
						final String SLUMP = subclassRecordSemester + "020101_SLUMP";
						final String GAKKI_ATTEND_TESTCD = subclassRecordSemester + "990007"; // 出席点
						final String GAKKI_HEIJOTEN = subclassRecordSemester + "990002"; // 平常点
						final String GAKKI_HYOKA_TESTCD = subclassRecordSemester + "990008"; // 学期評価
						final String HYOTEI_TESTCD = "9990009"; // 評定

	    				svf.VrsOut("RETEST", subclass.getScore(SLUMP)); // 再試験
	    				svf.VrsOut("AE", subclass._tVal1Name1); // AE
	    				svf.VrsOut("ATTEND_POINT", subclass.getScore(GAKKI_ATTEND_TESTCD)); // 出席点
	    				svf.VrsOut("TEST_POINT", calcTestScore(scoreList, subclass._m022Abbv3)); // 試験点
	    				svf.VrsOut("NORMAL_POINT", subclass.getScore(GAKKI_HEIJOTEN)); // 平常点
	    				svf.VrsOut("TOTAL_POINT", subclass.getScore(GAKKI_HYOKA_TESTCD)); // 合計点
	    				svf.VrsOut("VALUE", subclass.getScore(HYOTEI_TESTCD)); // 評定

	    			} else {
	    				svf.VrsOut("SUBCLASS_NAME1", "DUMMY"); // 科目名
	    				svf.VrAttribute("SUBCLASS_NAME1", "X=10000"); // 科目名
	    			}
	    			svf.VrEndRecord();
	    			_hasData = true;
		    	}
			}
		}
    }
    
    private static String calcTestScore(final List scoreList, final String m022Abbv3) {
    	BigDecimal sum = null;
    	int count = 0;
    	for (final Iterator it = scoreList.iterator(); it.hasNext();) {
    		final String score = (String) it.next();
    		if (NumberUtils.isNumber(score)) {
    			if (null == sum) {
    				sum = new BigDecimal(score);
    			} else {
    				sum = sum.add(new BigDecimal(score));
    			}
    		}
    		if (NumberUtils.isNumber(score) || "*".equals(score)) {
    			count += 1;
    		}
    	}
    	if (null == sum) {
    		return null;
    	}
    	if (NumberUtils.isNumber(m022Abbv3)) {
    		sum = sum.multiply(new BigDecimal(m022Abbv3));
    	}
		return sum.divide(new BigDecimal(count), 0, BigDecimal.ROUND_HALF_UP).toString();
	}
    
    private static List getMappedList(final Map map, final Object key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList());
        }
        return (List) map.get(key1);
    }
    
    /**
     * listを最大数ごとにグループ化したリストを得る
     * @param list
     * @param max 最大数
     * @return listを最大数ごとにグループ化したリスト
     */
    private static List getPageList(final List list, final int max) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= max) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }

    /**
     * 生徒
     */
    private static class Student {
        String _schregno;
        String _name;
        String _hrname;
        String _staffname;
        String _staffname2;
        String _grade;
        String _hrClass;
        String _gradeCd;
        String _coursecd;
        String _majorcd;
        String _course;
        String _majorname;
        String _majorabbv;
        String _coursecodename;
        String _attendno;
        String _attendnoStr;
		String _attendnoZeroSuprpess;
        String _hrClassName1;
        final Map _attendMap = new TreeMap();
        final Map _subclassMap = new TreeMap();
        String _entyear;

        Subclass getSubclass(final String subclasscd) {
            return (Subclass) _subclassMap.get(subclasscd);
        }

        /**
         * 生徒を取得
         */
        private static List getStudentList(final DB2UDB db2, final Param param) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" WITH CHAIR_STD AS ( ");
            stb.append(" SELECT T1.SCHREGNO ");
            stb.append(" FROM CHAIR_STD_DAT T1 ");
            stb.append(" INNER JOIN CHAIR_DAT T2 ON T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER AND T2.CHAIRCD = T1.CHAIRCD ");
            stb.append(" WHERE T1.YEAR = '" + param._year + "' AND T1.SEMESTER <= '" + param._semester + "' ");
            stb.append("   AND T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD IN " + SQLUtils.whereIn(true, param._categorySelected));
            stb.append(" ) ");
            stb.append("     SELECT  REGD.SCHREGNO");
            stb.append("            ,REGD.SEMESTER ");
            stb.append("            ,BASE.NAME ");
            stb.append("            ,HDAT.HR_NAME ");
            stb.append("            ,STFM.STAFFNAME ");
            stb.append("            ,STFM2.STAFFNAME AS STAFFNAME2 ");
            stb.append("            ,REGD.ATTENDNO ");
            stb.append("            ,REGD.GRADE ");
            stb.append("            ,REGD.HR_CLASS ");
            stb.append("            ,GDAT.GRADE_CD ");
            stb.append("            ,REGD.COURSECD ");
            stb.append("            ,REGD.MAJORCD ");
            stb.append("            ,REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE AS COURSE");
            stb.append("            ,MAJ.MAJORNAME ");
            stb.append("            ,MAJ.MAJORABBV ");
            stb.append("            ,CCM.COURSECODENAME ");
            stb.append("            ,HDAT.HR_CLASS_NAME1 ");
            stb.append("            ,FISCALYEAR(BASE.ENT_DATE) AS ENT_YEAR ");
            stb.append("            ,CASE WHEN W3.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("                  WHEN W4.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("                  WHEN W5.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("                  ELSE 0 END AS LEAVE ");
            stb.append("     FROM    SCHREG_REGD_DAT REGD ");
            stb.append("     INNER JOIN CHAIR_STD ON CHAIR_STD.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     INNER JOIN V_SEMESTER_GRADE_MST SEMEG ON SEMEG.YEAR = '" + param._year + "' AND SEMEG.SEMESTER = REGD.SEMESTER AND SEMEG.GRADE = REGD.GRADE ");
            //               転学・退学者で、異動日が[学期終了日または出欠集計日の小さい日付]より小さい日付の場合
            stb.append("     LEFT JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = REGD.SCHREGNO ");
            stb.append("                  AND W3.GRD_DIV IN('2','3') ");
            stb.append("                  AND W3.GRD_DATE < CASE WHEN SEMEG.EDATE < '" + param._date + "' THEN SEMEG.EDATE ELSE '" + param._date + "' END ");
            //               転入・編入者で、異動日が[学期終了日または出欠集計日の小さい日付]より大きい日付の場合
            stb.append("     LEFT JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = REGD.SCHREGNO ");
            stb.append("                  AND W4.ENT_DIV IN('4','5') ");
            stb.append("                  AND W4.ENT_DATE > CASE WHEN SEMEG.EDATE < '" + param._date + "' THEN SEMEG.EDATE ELSE '" + param._date + "' END ");
            //               留学・休学者で、[学期終了日または出欠集計日の小さい日付]が留学・休学期間内にある場合
            stb.append("     LEFT JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = REGD.SCHREGNO ");
            stb.append("                  AND W5.TRANSFERCD IN ('1','2') ");
            stb.append("                  AND CASE WHEN SEMEG.EDATE < '" + param._date + "' THEN SEMEG.EDATE ELSE '" + param._date + "' END BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR = REGD.YEAR ");
            stb.append("                  AND HDAT.SEMESTER = REGD.SEMESTER ");
            stb.append("                  AND HDAT.GRADE = REGD.GRADE ");
            stb.append("                  AND HDAT.HR_CLASS = REGD.HR_CLASS ");
            stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGD.YEAR ");
            stb.append("                  AND GDAT.GRADE = REGD.GRADE ");
            stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     LEFT JOIN STAFF_MST STFM ON STFM.STAFFCD = HDAT.TR_CD1 ");
            stb.append("     LEFT JOIN STAFF_MST STFM2 ON STFM2.STAFFCD = HDAT.TR_CD2 ");
            stb.append("     LEFT JOIN MAJOR_MST MAJ ON MAJ.COURSECD = REGD.COURSECD ");
            stb.append("                  AND MAJ.MAJORCD = REGD.MAJORCD ");
            stb.append("     LEFT JOIN COURSECODE_MST CCM ON CCM.COURSECODE = REGD.COURSECODE ");
            stb.append("     WHERE   REGD.YEAR = '" + param._year + "' ");
            stb.append("     AND REGD.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND REGD.COURSECD = '" + param._major.substring(0, 1) + "' ");
            stb.append("     AND REGD.MAJORCD = '" + param._major.substring(1) + "' ");
            stb.append("     ORDER BY ");
            stb.append("         REGD.GRADE, ");
            stb.append("         REGD.HR_CLASS, ");
            stb.append("         REGD.ATTENDNO ");
            final String sql = stb.toString();
            log.debug(" student sql = " + sql);

            final List studentList = new ArrayList();

            for (final Iterator it = KnjDbUtils.query(db2, stb.toString()).iterator(); it.hasNext();) {
            	final Map row = (Map) it.next();
                final Student student = new Student();
                student._schregno = KnjDbUtils.getString(row, "SCHREGNO");
                student._name = KnjDbUtils.getString(row, "NAME");
                student._hrname = KnjDbUtils.getString(row, "HR_NAME");
                student._staffname = StringUtils.defaultString(KnjDbUtils.getString(row, "STAFFNAME"));
                student._staffname2 = StringUtils.defaultString(KnjDbUtils.getString(row, "STAFFNAME2"));
                student._attendno = KnjDbUtils.getString(row, "ATTENDNO");
                student._attendnoStr = NumberUtils.isDigits(KnjDbUtils.getString(row, "ATTENDNO")) ? String.valueOf(Integer.parseInt(KnjDbUtils.getString(row, "ATTENDNO"))) + "番" : KnjDbUtils.getString(row, "ATTENDNO");
                student._attendnoZeroSuprpess = NumberUtils.isDigits(KnjDbUtils.getString(row, "ATTENDNO")) ? String.valueOf(Integer.parseInt(KnjDbUtils.getString(row, "ATTENDNO"))) : KnjDbUtils.getString(row, "ATTENDNO");
                student._grade = KnjDbUtils.getString(row, "GRADE");
                student._hrClass = KnjDbUtils.getString(row, "HR_CLASS");
                student._gradeCd = KnjDbUtils.getString(row, "GRADE_CD");
                student._coursecd = KnjDbUtils.getString(row, "COURSECD");
                student._majorcd = KnjDbUtils.getString(row, "MAJORCD");
                student._course = KnjDbUtils.getString(row, "COURSE");
                student._majorname = KnjDbUtils.getString(row, "MAJORNAME");
                student._majorabbv = KnjDbUtils.getString(row, "MAJORABBV");
                student._coursecodename = KnjDbUtils.getString(row, "COURSECODENAME");
                student._hrClassName1 = KnjDbUtils.getString(row, "HR_CLASS_NAME1");
                student._entyear = KnjDbUtils.getString(row, "ENT_YEAR");
                studentList.add(student);
            }
            
        	final Map studentMap = new HashMap();
        	for (final Iterator it = studentList.iterator(); it.hasNext();) {
        		final Student student = (Student) it.next();
        		studentMap.put(student._schregno, student);
        	}

            Subclass.setSubclassList(db2, param, studentMap);

            SubclassAttendance.load(db2, param, studentList);

            return studentList;
        }
    }
    
    private static class Subclass {
        final SubclassMst _mst;
        final String _subclasscd;
        final String _repoLimitCnt;
        final String _schoolingLimitCnt;
        final String _useMedia1;
        final String _m022Abbv3;
        final String _rVal1;
        final String _tVal1;
        final String _sVal1;
        final String _tVal1Name1;
        
        final Map _scoreMap = new HashMap();
        SubclassAttendance _subclassAttendance;
        boolean _isZenki;
        boolean _isKouki;

        Subclass(
        	final SubclassMst mst,
            final String subclasscd,
            final String repoLimitCnt,
            final String schoolingLimitCnt,
            final String useMedia1,
            final String m022Abbv3,
            final String rVal1,
            final String tVal1,
            final String tVal1Name1,
            final String sVal1
        ) {
        	_mst = mst;
            _subclasscd = subclasscd;
            _repoLimitCnt = repoLimitCnt;
            _schoolingLimitCnt = schoolingLimitCnt;
            _useMedia1 = useMedia1;
            _m022Abbv3 = m022Abbv3;
            _rVal1 = rVal1;
            _tVal1 = tVal1;
            _tVal1Name1 = tVal1Name1;
            _sVal1 = sVal1;
        }

        public String getScore(final String semTestcd) {
        	return (String) _scoreMap.get(semTestcd);
        }
        
        public static void setSubclassList(final DB2UDB db2, final Param param, final Map studentMap) {
            final String sql = sql(param);
            if (param._isOutputDebug) {
            	log.info(" subclass sql = " + sql);
            }
            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
            	final Map row = (Map) it.next();
                final String schregno = KnjDbUtils.getString(row, "SCHREGNO");
                final Student student = (Student) studentMap.get(schregno);
                if (null == student) {
                	continue;
                }
                final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                if (null == student.getSubclass(subclasscd)) {
                    final SubclassMst mst = param.getSubclassMst(subclasscd);
                    
                    final String repoLimitCnt = KnjDbUtils.getString(row, "REPO_LIMIT_CNT");
                    final String schoolingLimitCnt = KnjDbUtils.getString(row, "SCHOOLING_LIMIT_CNT");
                    final String useMedia1 = KnjDbUtils.getString(row, "USE_MEDIA1");
                    final String m022Abbv3 = KnjDbUtils.getString(row, "M022_ABBV3");
                    final String rVal1 = KnjDbUtils.getString(row, "R_VAL1");
                    final String tVal1 = KnjDbUtils.getString(row, "T_VAL1");
                    final String tVal1Name1 = KnjDbUtils.getString(row, "T_VAL1_NAME1");
                    final String sVal1 = KnjDbUtils.getString(row, "S_VAL1");
                    final Subclass subclass = new Subclass(mst, subclasscd, repoLimitCnt, schoolingLimitCnt, useMedia1, m022Abbv3, rVal1, tVal1, tVal1Name1, sVal1);
                    student._subclassMap.put(subclasscd, subclass);
                    
                    final String takeSemes = KnjDbUtils.getString(row, "TAKESEMES");
                    if ("1".equals(takeSemes)) {
                    	subclass._isZenki = true;
                    } else if ("2".equals(takeSemes)) {
                    	subclass._isKouki = true;
                    }
                }
                
                final Subclass subclass = (Subclass) student._subclassMap.get(subclasscd);
                
                final String semTestcd = KnjDbUtils.getString(row, "SEM_TESTCD");
                final String score = null != KnjDbUtils.getString(row, "VALUE_DI") ? KnjDbUtils.getString(row, "VALUE_DI") : KnjDbUtils.getString(row, "SCORE");
                subclass._scoreMap.put(semTestcd, score);
                subclass._scoreMap.put(semTestcd + "_SLUMP", KnjDbUtils.getString(row, "SLUMP_SCORE"));
            }
        }

        public static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH CHAIR_SUBCLASS AS ( ");
            stb.append(" SELECT ");
            stb.append("    T1.SCHREGNO ");
            stb.append("    , T1.YEAR ");
            stb.append("    , T1.SEMESTER ");
            stb.append("    , T1.GRADE ");
            stb.append("    , T1.HR_CLASS ");
            stb.append("    , T1.ATTENDNO ");
            stb.append("    , L2.CLASSCD ");
            stb.append("    , L2.SCHOOL_KIND ");
            stb.append("    , L2.CURRICULUM_CD ");
            stb.append("    , L2.SUBCLASSCD ");
            stb.append("    , MIN(VALUE(L2.TAKESEMES, '0')) AS TAKESEMES ");
            stb.append("    , MAX(L3.REPO_LIMIT_CNT) AS REPO_LIMIT_CNT ");
            stb.append("    , MAX(L3.SCHOOLING_LIMIT_CNT) AS SCHOOLING_LIMIT_CNT ");
            stb.append("    , MAX(L3.USE_MEDIA1) AS USE_MEDIA1 ");
            stb.append("    , MAX(VALUE(M022.ABBV3, M022_2.ABBV3)) AS M022_ABBV3 ");
            stb.append(" FROM SCHREG_REGD_DAT T1 ");
            stb.append(" INNER JOIN CHAIR_STD_DAT L1 ON ");
            stb.append("      L1.YEAR = T1.YEAR ");
            stb.append("    AND L1.SEMESTER = T1.SEMESTER ");
            stb.append("    AND L1.SCHREGNO = T1.SCHREGNO ");
            stb.append(" INNER JOIN CHAIR_DAT L2 ON ");
            stb.append("      L2.YEAR = T1.YEAR ");
            stb.append("    AND L2.SEMESTER = T1.SEMESTER ");
            stb.append("    AND L2.CHAIRCD = L1.CHAIRCD ");
            if ("1".equals(param._printSubclassLastChairStd)) {
                stb.append("     INNER JOIN SEMESTER_MST STDSEME ON STDSEME.YEAR = L1.YEAR ");
                stb.append("       AND STDSEME.SEMESTER = L1.SEMESTER ");
                stb.append("       AND STDSEME.EDATE = L1.APPENDDATE ");
            }
            stb.append(" LEFT JOIN CHAIR_CORRES_SEMES_DAT L3 ON ");
            stb.append("      L3.YEAR = T1.YEAR ");
            stb.append("    AND L3.SEMESTER = T1.SEMESTER ");
            stb.append("    AND L3.CHAIRCD = L1.CHAIRCD ");
            stb.append("    AND L3.CLASSCD = L2.CLASSCD ");
            stb.append("    AND L3.SCHOOL_KIND = L2.SCHOOL_KIND ");
            stb.append("    AND L3.CURRICULUM_CD = L2.CURRICULUM_CD ");
            stb.append("    AND L3.SUBCLASSCD = L2.SUBCLASSCD ");
            stb.append(" LEFT JOIN NAME_MST M021 ON M021.NAMECD1 = 'M021' ");
            stb.append("    AND M021.NAME1 = T1.COURSECD ");
            stb.append("    AND M021.NAME2 = T1.MAJORCD ");
            stb.append("    AND M021.ABBV1 = L2.CLASSCD || '-' || L2.SCHOOL_KIND || '-' || L2.CURRICULUM_CD || '-' || L2.SUBCLASSCD ");
            stb.append(" LEFT JOIN NAME_MST M022 ON M022.NAMECD1 = 'M022' AND M022.NAMECD2 = M021.ABBV2 ");
            stb.append(" LEFT JOIN NAME_MST M021_2 ON M021_2.NAMECD1 = 'M021' ");
            stb.append("    AND M021_2.NAME1 = T1.COURSECD ");
            stb.append("    AND M021_2.NAME2 = T1.MAJORCD ");
            stb.append("    AND M021_2.ABBV1 IS NULL ");
            stb.append(" LEFT JOIN NAME_MST M022_2 ON M022_2.NAMECD1 = 'M022' AND M022_2.NAMECD2 = M021_2.ABBV2 ");
            stb.append(" WHERE ");
            stb.append("    T1.YEAR = '" + param._year + "' ");
            stb.append("    AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append("    AND L2.CLASSCD || '-' || L2.SCHOOL_KIND || '-' || L2.CURRICULUM_CD || '-' || L2.SUBCLASSCD IN " + SQLUtils.whereIn(true, param._categorySelected));
            stb.append(" GROUP BY ");
            stb.append("    T1.SCHREGNO ");
            stb.append("    , T1.YEAR ");
            stb.append("    , T1.SEMESTER ");
            stb.append("    , T1.GRADE ");
            stb.append("    , T1.HR_CLASS ");
            stb.append("    , T1.ATTENDNO ");
            stb.append("    , L2.CLASSCD ");
            stb.append("    , L2.SCHOOL_KIND ");
            stb.append("    , L2.CURRICULUM_CD ");
            stb.append("    , L2.SUBCLASSCD ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("    T1.SCHREGNO ");
            stb.append("    , T1.YEAR ");
            stb.append("    , T1.SEMESTER ");
            stb.append("    , T1.GRADE ");
            stb.append("    , T1.HR_CLASS ");
            stb.append("    , T1.ATTENDNO ");
            stb.append("    , T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("    , T1.TAKESEMES ");
            stb.append("    , T1.REPO_LIMIT_CNT ");
            stb.append("    , T1.SCHOOLING_LIMIT_CNT ");
            stb.append("    , T1.USE_MEDIA1 ");
            stb.append("    , T1.M022_ABBV3 ");
            stb.append("    , L5R.VAL_NUMERIC AS R_VAL1 ");
            stb.append("    , L5T.VAL_NUMERIC AS T_VAL1 ");
            stb.append("    , L5S.VAL_NUMERIC AS S_VAL1 ");
            stb.append("    , M006.NAME1 AS T_VAL1_NAME1 ");
            stb.append("    , REC.SEMESTER || REC.TESTKINDCD || REC.TESTITEMCD || REC.SCORE_DIV AS SEM_TESTCD ");
            stb.append("    , REC.SCORE ");
            stb.append("    , REC.VALUE_DI ");
            stb.append("    , SLUMP.SCORE AS SLUMP_SCORE ");
            stb.append(" FROM CHAIR_SUBCLASS T1 ");
            stb.append(" LEFT JOIN SUBCLASS_CORRES_RST_SEMES_DAT L5R ON ");
            stb.append("      L5R.YEAR = T1.YEAR ");
            stb.append("    AND L5R.SEMESTER = T1.SEMESTER ");
            stb.append("    AND L5R.CLASSCD = T1.CLASSCD ");
            stb.append("    AND L5R.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("    AND L5R.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("    AND L5R.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("    AND L5R.RST_DIV = 'R' ");
            stb.append("    AND L5R.SEQ = 1 ");
            stb.append("    AND L5R.SCHREGNO = T1.SCHREGNO ");
            stb.append(" LEFT JOIN SUBCLASS_CORRES_RST_SEMES_DAT L5T ON ");
            stb.append("      L5T.YEAR = T1.YEAR ");
            stb.append("    AND L5T.SEMESTER = T1.SEMESTER ");
            stb.append("    AND L5T.CLASSCD = T1.CLASSCD ");
            stb.append("    AND L5T.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("    AND L5T.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("    AND L5T.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("    AND L5T.RST_DIV = 'T' ");
            stb.append("    AND L5T.SEQ = 1 ");
            stb.append("    AND L5T.SCHREGNO = T1.SCHREGNO ");
            stb.append(" LEFT JOIN SUBCLASS_CORRES_RST_SEMES_DAT L5S ON ");
            stb.append("      L5S.YEAR = T1.YEAR ");
            stb.append("    AND L5S.SEMESTER = T1.SEMESTER ");
            stb.append("    AND L5S.CLASSCD = T1.CLASSCD ");
            stb.append("    AND L5S.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("    AND L5S.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("    AND L5S.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("    AND L5S.RST_DIV = 'S' ");
            stb.append("    AND L5S.SEQ = 1 ");
            stb.append("    AND L5S.SCHREGNO = T1.SCHREGNO ");
            stb.append(" LEFT JOIN NAME_MST M006 ON ");
            stb.append("      M006.NAMECD1 = 'M006' ");
            stb.append("    AND M006.NAMECD2 = CAST(L5T.VAL_NUMERIC AS VARCHAR(1)) ");
            stb.append(" LEFT JOIN RECORD_SCORE_DAT REC ON ");
            stb.append("      REC.YEAR = T1.YEAR ");
            stb.append("    AND REC.CLASSCD = T1.CLASSCD ");
            stb.append("    AND REC.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("    AND REC.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("    AND REC.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("    AND REC.SCHREGNO = T1.SCHREGNO ");
            stb.append(" LEFT JOIN RECORD_SLUMP_SDIV_DAT SLUMP ON ");
            stb.append("      SLUMP.YEAR = REC.YEAR ");
            stb.append("    AND SLUMP.SEMESTER = REC.SEMESTER ");
            stb.append("    AND SLUMP.TESTKINDCD = REC.TESTKINDCD ");
            stb.append("    AND SLUMP.TESTITEMCD = REC.TESTITEMCD ");
            stb.append("    AND SLUMP.SCORE_DIV = REC.SCORE_DIV ");
            stb.append("    AND SLUMP.CLASSCD = REC.CLASSCD ");
            stb.append("    AND SLUMP.SCHOOL_KIND = REC.SCHOOL_KIND ");
            stb.append("    AND SLUMP.CURRICULUM_CD = REC.CURRICULUM_CD ");
            stb.append("    AND SLUMP.SUBCLASSCD = REC.SUBCLASSCD ");
            stb.append("    AND SLUMP.SCHREGNO = REC.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("   T1.CLASSCD <= '90' ");
            stb.append(" ORDER BY ");
            stb.append("    T1.GRADE ");
            stb.append("    , T1.HR_CLASS ");
            stb.append("    , T1.ATTENDNO ");
            stb.append("    , T1.SUBCLASSCD ");
            return stb.toString();
        }
    }

    private static class SubclassAttendance {
        BigDecimal _lesson;
        BigDecimal _attend;
        BigDecimal _sick;

        public SubclassAttendance(final BigDecimal lesson, final BigDecimal attend, final BigDecimal sick) {
            _lesson = lesson;
            _attend = attend;
            _sick = sick;
        }

        private static void load(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            PreparedStatement ps2 = null;
            try {
                param._attendParamMap.put("schregno", "?");
                param._attendParamMap.put("subclasscdArray", param._categorySelected);

                final String sql = AttendAccumulate.getAttendSubclassSql(
                        param._year,
                        SEMEALL,
                        null,
                        param._date,
                        param._attendParamMap
                );

                ps = db2.prepareStatement(sql);
                
                for (final Iterator stit = studentList.iterator(); stit.hasNext();) {
                    final Student student = (Student) stit.next();

                    final Set logged = new HashSet();

                    for (final Iterator it = KnjDbUtils.query(db2, ps, new Object[] {student._schregno}).iterator(); it.hasNext();) {
                    	final Map row = (Map) it.next();
                        if (!SEMEALL.equals(KnjDbUtils.getString(row, "SEMESTER"))) {
                            continue;
                        }
                        final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");

                        final SubclassMst mst = (SubclassMst) param._subclassMstMap.get(subclasscd);
                        if (null == mst) {
                            log.warn("no subclass : " + subclasscd);
                            continue;
                        }
                        final int iclasscd = Integer.parseInt(subclasscd.substring(0, 2));
                        if ((Integer.parseInt(KNJDefineSchool.subject_D) <= iclasscd && iclasscd <= Integer.parseInt(KNJDefineSchool.subject_U) || iclasscd == Integer.parseInt(KNJDefineSchool.subject_T))) {
                            if (null == student._subclassMap.get(subclasscd)) {
                            	final String message = " null chair subclass = " + subclasscd;
                            	if (logged.contains(message)) {
                            		log.info(message);
                            		logged.add(message);
                            	}
                                continue;
                            }
                            final Subclass subclass = student.getSubclass(subclasscd);

                            final BigDecimal lesson = KnjDbUtils.getBigDecimal(row, "MLESSON", null);
                            final BigDecimal rawSick = KnjDbUtils.getBigDecimal(row, "SICK1", null);
                            final BigDecimal sick = KnjDbUtils.getBigDecimal(row, "SICK2", null);
                            final BigDecimal rawReplacedSick = KnjDbUtils.getBigDecimal(row, "RAW_REPLACED_SICK", null);
                            final BigDecimal replacedSick = KnjDbUtils.getBigDecimal(row, "REPLACED_SICK", null);

                            final BigDecimal sick1 = mst._isSaki ? rawReplacedSick : rawSick;
                            final BigDecimal attend = lesson.subtract(null == sick1 ? BigDecimal.valueOf(0) : sick1);
                            final BigDecimal sick2 = mst._isSaki ? replacedSick : sick;

                            final SubclassAttendance sa = new SubclassAttendance(lesson, attend, sick2);

//                            log.debug(" schregno = " + student._schregno + ", sa = " + subclassAttendance);
                            subclass._subclassAttendance = sa;
                        }
                    }
                }
                
                final String sql2 = getSchAttendDatSql(param);
                ps2 = db2.prepareStatement(sql2);

                if (param._isOutputDebug) {
                	log.info(" attend sql2  = " + sql2);
                }
                
                for (final Iterator stit = studentList.iterator(); stit.hasNext();) {
                    final Student student = (Student) stit.next();
                    
                    for (final Iterator it = KnjDbUtils.query(db2, ps2, new Object[] {student._schregno}).iterator(); it.hasNext();) {
                    	final Map row = (Map) it.next();
                        final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");

                        if (null == student._subclassMap.get(subclasscd)) {
                        	continue;
                        }
                        final Subclass subclass = student.getSubclass(subclasscd);

                        BigDecimal creditTime = KnjDbUtils.getBigDecimal(row, "CREDIT_TIME", null);
                        if (null != creditTime && creditTime.scale() > 0 && creditTime.subtract(creditTime.setScale(0, BigDecimal.ROUND_DOWN)).doubleValue() == 0.0) {
                        	creditTime = creditTime.setScale(0);
                        }
						SubclassAttendance sa = new SubclassAttendance(null, creditTime, null);
                        if (null != subclass._subclassAttendance) {
                        	sa = subclass._subclassAttendance.add(sa);
                        }
                        subclass._subclassAttendance = sa;
                    }
                }

            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(ps);
                DbUtils.closeQuietly(ps2);
                db2.commit();
            }
        }
        
        private SubclassAttendance add(final SubclassAttendance sa) {
        	if (null == sa) {
        		return this;
        	}
        	final BigDecimal lesson = null == _lesson ? sa._lesson : null == sa._lesson ? _lesson : _lesson.add(sa._lesson);
        	final BigDecimal attend = null == _attend ? sa._attend : null == sa._attend ? _attend : _attend.add(sa._attend);
        	final BigDecimal sick = null == _sick ? sa._sick : null == sa._sick ? _sick : _sick.add(sa._sick);
			return new SubclassAttendance(lesson, attend, sick);
		}

		private static String getSchAttendDatSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("       T1.SCHREGNO ");
            stb.append("     , CHR.CLASSCD || '-' || CHR.SCHOOL_KIND || '-' || CHR.CURRICULUM_CD || '-' || CHR.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("     , T1.SCHOOLINGKINDCD ");
            stb.append("     , SUM(T1.CREDIT_TIME) AS CREDIT_TIME ");
            stb.append(" FROM SCH_ATTEND_DAT T1 ");
            stb.append(" INNER JOIN SEMESTER_MST SEME ON SEME.YEAR = T1.YEAR ");
            stb.append("                             AND SEME.SEMESTER <> '9' ");
            stb.append("                             AND T1.EXECUTEDATE BETWEEN SEME.SDATE AND SEME.EDATE ");
            stb.append(" INNER JOIN CHAIR_DAT CHR ON CHR.YEAR = T1.YEAR ");
            stb.append("                         AND CHR.SEMESTER = SEME.SEMESTER ");
            stb.append("                         AND CHR.CHAIRCD = T1.CHAIRCD ");
            stb.append(" INNER JOIN SUBCLASS_MST SUB_M ON SUB_M.CLASSCD = CHR.CLASSCD ");
            stb.append("                              AND SUB_M.SCHOOL_KIND = CHR.SCHOOL_KIND ");
            stb.append("                              AND SUB_M.CURRICULUM_CD = CHR.CURRICULUM_CD ");
            stb.append("                              AND SUB_M.SUBCLASSCD = CHR.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND SEME.SEMESTER <= '" + param._semester + "' ");
            stb.append("     AND CHR.CLASSCD <= '90' ");
            stb.append("     AND T1.SCHREGNO = ? ");
            stb.append(" GROUP BY ");
            stb.append("       T1.SCHREGNO ");
            stb.append("     , CHR.CLASSCD || '-' || CHR.SCHOOL_KIND || '-' || CHR.CURRICULUM_CD || '-' || CHR.SUBCLASSCD ");
            stb.append("     , T1.SCHOOLINGKINDCD ");
            stb.append(" HAVING ");
            stb.append("     SUM(T1.CREDIT_TIME) IS NOT NULL ");

            return stb.toString();
        }
    }
    
    private static class SubclassMst implements Comparable {
    	final Param _param;
        final String _specialDiv;
        final String _classcd;
        final String _subclasscd;
        final String _classabbv;
        final String _classname;
//        final String _subclassabbv;
        final String _subclassname;
        final Integer _electdiv;
        boolean _isSaki;
        boolean _isMoto;
        String _calculateCreditFlg;
        SubclassMst _sakikamoku;
        public SubclassMst(final Param param, final String specialDiv, final String classcd, final String subclasscd, final String classabbv, final String classname, final String subclassabbv, final String subclassname,
                final Integer electdiv) {
        	_param = param;
            _specialDiv = specialDiv;
            _classcd = classcd;
            _subclasscd = subclasscd;
            _classabbv = classabbv;
            _classname = classname;
//            _subclassabbv = subclassabbv;
            _subclassname = subclassname;
            _electdiv = electdiv;
        }
        public int compareTo(final Object o) {
            final SubclassMst os = (SubclassMst) o;
            int rtn;
            rtn = _subclasscd.compareTo(os._subclasscd);
            return rtn;
        }
        public String toString() {
            return "SubclassMst(subclasscd = " + _subclasscd + ")";
        }
        
        private static Map getSubclassMst(
                final DB2UDB db2,
                final Param param,
                final String year
        ) {
        	Map subclassMstMap = new HashMap();
            try {
                String sql = "";
                sql += " SELECT ";
                sql += "   VALUE(T2.SPECIALDIV, '0') AS SPECIALDIV, ";
                sql += "   T1.CLASSCD, ";
                sql += "   T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
                sql += "   T2.CLASSABBV, T2.CLASSNAME, T1.SUBCLASSABBV, T1.SUBCLASSNAME, ";
                sql += "   VALUE(T1.ELECTDIV, '0') AS ELECTDIV ";
                sql += " FROM SUBCLASS_MST T1 ";
                sql += " LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
                for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
                	final Map row = (Map) it.next();
                    final SubclassMst mst = new SubclassMst(param, KnjDbUtils.getString(row, "SPECIALDIV"), KnjDbUtils.getString(row, "CLASSCD"), KnjDbUtils.getString(row, "SUBCLASSCD"), KnjDbUtils.getString(row, "CLASSABBV"), KnjDbUtils.getString(row, "CLASSNAME")
                    		, KnjDbUtils.getString(row, "SUBCLASSABBV")
                    		, KnjDbUtils.getString(row, "SUBCLASSNAME")
                    		, KnjDbUtils.getInt(row, "ELECTDIV", new Integer(999)));
                    subclassMstMap.put(KnjDbUtils.getString(row, "SUBCLASSCD"), mst);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            }
            try {
                String sql = "";
                sql += " SELECT ";
                sql += " COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD, CALCULATE_CREDIT_FLG,  ";
                sql += " ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD ";
                sql += " FROM SUBCLASS_REPLACE_COMBINED_DAT ";
                sql += " WHERE YEAR = '" + year + "' ";
                if (param._isOutputDebug) {
                	log.info(" repl sub sql = " + sql);
                }
                for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
                	final Map row = (Map) it.next();
                    
                    final SubclassMst combined = (SubclassMst) subclassMstMap.get(KnjDbUtils.getString(row, "COMBINED_SUBCLASSCD"));
                    final SubclassMst attend = (SubclassMst) subclassMstMap.get(KnjDbUtils.getString(row, "ATTEND_SUBCLASSCD"));
                    if (null != combined && null != attend) {
                    	combined._isSaki = true;
                    	attend._isMoto = true;
                    	combined._calculateCreditFlg = KnjDbUtils.getString(row, "CALCULATE_CREDIT_FLG");
                    	attend._sakikamoku = combined;
                    } else {
                    	log.warn(" combined = " + combined + ", attend = " + attend + " in " + row);
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            }
            return subclassMstMap;
        }
    }
    
    private Param createParam(final HttpServletRequest request, final DB2UDB db2) {
        log.fatal("$Revision: 69207 $ $Date: 2019-08-13 19:04:08 +0900 (火, 13 8 2019) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(request, db2);
        return param;
    }

    private static class Param {
        final String _year;
        final String _semester;
        final String _major;
        final String _loginDate;

        final String[] _categorySelected;
        /** 出欠集計日付 */
        final String _date;
        
        final String _documentroot;
        final String _printSubclassLastChairStd;
        final boolean _isOutputDebug;

        final boolean _isSeireki;
        private Map _subclassMstMap;

        /** 端数計算共通メソッド引数 */
        final Map _attendParamMap;

        private KNJSchoolMst _knjSchoolMst;

        private final DecimalFormat _df;
        
        final String _nendo;
        
        Param(final HttpServletRequest request, final DB2UDB db2) {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("GAKKI");
            _major = request.getParameter("MAJOR");
            _loginDate = request.getParameter("LOGIN_DATE");
            _categorySelected = request.getParameterValues("category_selected");
            _date = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE"));
            
            _documentroot = request.getParameter("DOCUMENTROOT");
            _printSubclassLastChairStd = request.getParameter("printSubclassLastChairStd");

            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));
            _nendo = nendo(db2);

            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }
            
            _subclassMstMap = SubclassMst.getSubclassMst(db2, this, _year);

            _df = null != _knjSchoolMst && ("3".equals(_knjSchoolMst._absentCov) || "4".equals(_knjSchoolMst._absentCov)) ? new DecimalFormat("0.0") : new DecimalFormat("0");

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
            _attendParamMap.put("useCurriculumcd", "1");

            _isSeireki = "2".equals(KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z012' AND NAMECD2 = '00' ")));
        }
        
        private String nendo(final DB2UDB db2) {
			return _isSeireki ? StringUtils.defaultString(_year) + "年度" : KNJ_EditDate.gengou(db2, Integer.parseInt(_year)) + "年度";
		}

		private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJD280C' AND NAME = '" + propName + "' "));
        }

		private SubclassMst getSubclassMst(final String subclasscd) {
            if (null == _subclassMstMap.get(subclasscd)) {
                //log.info("科目マスタなし:" + subclasscd);
                return null;
            }
            return (SubclassMst) _subclassMstMap.get(subclasscd);
        }
    }
}
