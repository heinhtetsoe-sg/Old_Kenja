/*
 * $Id: 175c30564ffe5dc94591e2f1b18e9ffc6c70e4f0 $
 *
 * 作成日: 2012/12/06
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJM;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * 学習状況通知
 */
public class KNJM500E {

    private static final Log log = LogFactory.getLog(KNJM500E.class);
    
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
    
    private static String zeroBlank(final int n) {
    	return n == 0 ? "" : String.valueOf(n);
    }
    
    private static String formatDate(final String date) {
        if (null == date) {
            return "";
        }
        final int month = Integer.parseInt(date.substring(5, 7));
        final int day = Integer.parseInt(date.substring(8));
        DecimalFormat zero = new DecimalFormat("00");
        return zero.format(month) + zero.format(day);
    }
    
    private static String add(final String num1, final String num2) {
    	if (!NumberUtils.isNumber(num1)) return num2;
    	if (!NumberUtils.isNumber(num2)) return num1;
    	return new BigDecimal(num1).add(new BigDecimal(num2)).toString();
    }

    private static String getDispNum(final BigDecimal bd) {
    	if (null == bd) {
    		return null;
    	}
        if (bd.setScale(0, BigDecimal.ROUND_UP).equals(bd.setScale(0, BigDecimal.ROUND_DOWN))) {
            // 切り上げでも切り下げでも値が変わらない = 小数点以下が0
            return bd.setScale(0).toString();
        }
        return bd.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }
    
    private <T> List<List<T>> getPageList(final List<T> list, final int maxLine) {
        final List<List<T>> pageList = new ArrayList();
        List<T> current = null;
        for (final T o : list) {
            if (null == current || current.size() >= maxLine) {
                current = new ArrayList();
                pageList.add(current);
            }
            current.add(o);
        }
        return pageList;
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        final String form = "KNJM500E.frm";
        final int maxLine = 16;

        final List<Student> list = Student.getStudentList(db2, _param);
        for (final Student student : list) {
            
            final List<List<Subclass>> pageList = getPageList(student._subclassList, maxLine);
            if (pageList.isEmpty()) {
            	pageList.add(new ArrayList());
            }
            
            for (int pi = 0; pi < pageList.size(); pi++) {

            	svf.VrSetForm(form, 1);

            	printHeader(svf, student);
                
                final List<Subclass> subclassList = pi < pageList.size() ? pageList.get(pi) : Collections.EMPTY_LIST;
                int line = 0;
                for (int subi = 0; subi < subclassList.size(); subi++) {
                    line = subi + 1;
                    
                    final Subclass subclass = subclassList.get(subi);

                    final int ketaSubclass = KNJ_EditEdit.getMS932ByteLength(subclass._subclassname);
                    svf.VrsOutn("SUBCLASS" + (ketaSubclass <= 16 ? "1" : ketaSubclass <= 20 ? "2" : "3"), line, subclass._subclassname); // 科目

                    // レポート提出状況
                    for (int repi = 0; repi < subclass._reportList.size(); repi++) {
                        final Report report = subclass._reportList.get(repi);
                        if (!NumberUtils.isDigits(report._standardSeq)) {
                            continue;
                        }
//                        final String teishutubi = formatDate(report._receiptDate);
//                        final String hensouMark = NumberUtils.isDigits(report._representSeq) && Integer.parseInt(report._representSeq) >= 1 ? "*" : "";
                        
                        String hyouka = null;
                        if (null == report._repStandardSeq) { // レポートが未提出
//                            if (null == report._standardDate) {
//                                // log.debug(" standardSeq = " + report._standardSeq + ":  _ standardDate = " + report._standardDate + "");
//                                hyouka = null;
//                            } else if (report._standardDate.compareTo(_param._kijun) < 0) { // 未提出 && 提出期限を超えた
//                                // log.debug(" standardSeq = " + report._standardSeq + ":  x standardDate = " + report._standardDate + "");
//                                hyouka = "×";
//                            } else { // 未提出 && 提出期限を超えていない
//                                // log.debug(" standardSeq = " + report._standardSeq + ":  n standardDate = " + report._standardDate + "");
//                                hyouka = null;
//                            }
                        } else if (null == report._gradValue ||
                                    null != report._gradDate && _param._kijun.compareTo(report._gradDate) < 0 
                                ) {
//                            // log.debug(" standardSeq = " + report._standardSeq + ":  r receiiptDate = " + report._receiptDate + "");
                        	hyouka = "受";
                        } else {
//                            // log.debug(" standardSeq = " + report._standardSeq + ":  g gradValue = " + report._gradValueName + "");
                        	hyouka = report._gradValueName;
                        }
                        svf.VrsOutn("CONDITIONS" + String.valueOf(Integer.parseInt(report._standardSeq)), line, hyouka); // 評価
                    }

                    
//                    for (int sfi = 0; sfi < Math.min(2, testcdList.size()); sfi++) {
//                        final String testcd = (String) testcdList.get(sfi);
//                        final String score = (String) subclass._testScoreMap.get(testcd);
//                        svf.VrsOut("TEST" + String.valueOf(sfi + 1), score); // テスト
//                    }

                    svf.VrsOutn("REGULATION", line, subclass._repSeqAll); // レポート規定数
                    svf.VrsOutn("TEST1", line, subclass.getScore("1-01-01-01")); // 試験
                    svf.VrsOutn("EVA1", line, subclass.getScore("1-99-00-08")); // 評価
                    svf.VrsOutn("TEST2", line, subclass.getScore("2-01-01-01")); // 試験
                    svf.VrsOutn("EVA2", line, subclass.getScore("9-99-00-08")); // 評価
                    svf.VrsOutn("RATING", line, subclass.getScore("9-99-00-09")); // 評定
                    if (NumberUtils.isDigits(subclass._getCredit) && Integer.parseInt(subclass._getCredit) > 0) {
                    	svf.VrsOutn("JUDGE", line, "合"); // 合否
                    }

                    svf.VrsOutn("GET_CREDIT", line, subclass._getCredit); // 修得単位数
                    svf.VrsOutn("COMP_CREDIT", line, subclass._compCredit); // 履修単位数
                    svf.VrsOutn("SCH_ATTEND", line, subclass.getAttendCount()); // スクーリング・出席数
                    svf.VrsOutn("SCH_REGULATION", line, subclass._schSeqMin); // スクーリング・規定数

                    // スクーリング日付
                    final List<String> printAttendDateList = subclass.getPrintAttendDateList();
                    for (int si = 0; si < printAttendDateList.size(); si++) {
                    	final String d = printAttendDateList.get(si);
                        svf.VrsOutn("SCH_DATE" + String.valueOf(si + 1), line, d);
                    }
                }
                
                // スクーリング日付
                final Map<String, String> specialSubclasscdNameMap = new HashMap();
                final Map<String, List<String>> printSpecialSubclasscdAttendDateListMap = new TreeMap();
                for (final Map<String, String> row : student._hrAttendDateList) {
                	final String executedate = KnjDbUtils.getString(row, "EXECUTEDATE");
                	if (null == executedate) {
                		continue;
                	}
                	final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                	if (null == subclasscd) {
                		continue;
                	}
                	if (!"SPECIAL".equals(KnjDbUtils.getString(row, "TABLEDIV"))) {
                		continue;
                	}
                    if (!printSpecialSubclasscdAttendDateListMap.containsKey(subclasscd)) {
                    	printSpecialSubclasscdAttendDateListMap.put(subclasscd, new ArrayList());
                    }
                    printSpecialSubclasscdAttendDateListMap.get(subclasscd).add(executedate);
                    specialSubclasscdNameMap.put(subclasscd, KnjDbUtils.getString(row, "SUBCLASSNAME"));
                }

                line = line + 2;
                for (final Map.Entry<String, List<String>> e : printSpecialSubclasscdAttendDateListMap.entrySet()) {
                	if (line > maxLine) {
                		break;
                	}
                	final String subclasscd = e.getKey();
                	
                    svf.VrsOutn("SUBCLASS1", line, specialSubclasscdNameMap.get(subclasscd)); // 科目

                    final List<String> printAttendDateList = e.getValue();
                    Collections.sort(printAttendDateList);
                    for (int si = 0; si < printAttendDateList.size(); si++) {
                    	final String d = printAttendDateList.get(si);
                        svf.VrsOutn("SCH_DATE" + String.valueOf(si + 1), line, formatDate(d));
                    }
                    line += 1;
                }
                svf.VrEndPage();
                _hasData = true;
            }
        }
    }

	private void printHeader(final Vrw32alp svf, final Student student) {
		svf.VrsOut("TITLE", "2".equals(_param._titleSelect) ? "通 知 表" : "学 習 状 況 通 知");
		svf.VrAttribute("TITLE", "Hensyu=3"); // センタリング
		svf.VrsOut("NENDO", _param._nendo); // 年度
		svf.VrsOut("COURSE", student._coursecodename); // 課程
		svf.VrsOut("TITLE2", "学習状況通知"); // タイトル
		svf.VrsOut("SCHREGNO", student._schregno); // 学籍番号
		svf.VrsOut("NAME", student._name); // 氏名
		svf.VrsOut("HR_NAME", student._hrname); // クラス
		svf.VrsOut("DATE1", _param._kijunFormatJp); // 日付

		for (int i = 1; i <= 6; i++) {
            final String remarkId = String.valueOf(i);
            final String remark = (String) _param._hreportRemarkTDat.get(remarkId);
		    svf.VrsOut("COMMENT" + remarkId, remark); // コメント
		}
		
		svf.VrsOut("TOTAL_COMP_CREDIT1", student.getTotalCredit(false)); // 履修単位数
		svf.VrsOut("TOTAL_COMP_CREDIT2", add(student._beforeYearCompCredit, student.getTotalCredit(false))); // 履修単位数
		svf.VrsOut("TOTAL_GET_CREDIT1", student.getTotalCredit(true)); // 修得単位数
		svf.VrsOut("TOTAL_GET_CREDIT2", add(student._beforeYearCredit, student.getTotalCredit(true))); // 修得単位数

        svf.VrsOut("DATE2", _param._kijunFormatJp); // 日付
//        svf.VrsOut("DATE3", null); // 日付
		svf.VrsOut("HR_ATTEND1", zeroBlank(Student.shukkouNissuSagaken(student._lastYearHrAttendDateList).size())); // 出校日数
		svf.VrsOut("SP_ACT_ATTEND1", student._beforeYearCreditTime); // 特活時数
		svf.VrsOut("HR_ATTEND2", zeroBlank(Student.shukkouNissuSagaken(student._hrAttendDateList).size())); // 出校日数
		svf.VrsOut("SP_ACT_ATTEND2", student._thisYearCreditTime); // 特活時数
		svf.VrsOut("HR_ATTEND3", add(zeroBlank(Student.shukkouNissuSagaken(student._lastYearHrAttendDateList).size()), zeroBlank(Student.shukkouNissuSagaken(student._hrAttendDateList).size()))); // 出校日数
		svf.VrsOut("SP_ACT_ATTEND3", student._totalYearCreditTime); // 特活時数

		final int ketaPrincipal_name = KNJ_EditEdit.getMS932ByteLength(_param._principalStaffname);
		svf.VrsOut("PRINCIPAL_NAME" + (ketaPrincipal_name <= 20 ? "1" : ketaPrincipal_name <= 30 ? "2" : "3"), _param._principalStaffname); // 校長名
		
		final int ketaStaffname = KNJ_EditEdit.getMS932ByteLength(student._staffname);
		svf.VrsOut("TR_NAME" + (ketaStaffname <= 20 ? "1" : ketaStaffname <= 30 ? "2" : "3"), student._staffname); // 担任名

		svf.VrsOut("SCHOOL_NAME", _param._schoolName1); // 学校名
	}

    private static class Student {
		final String _schregno;
        final String _name;
        final String _hrname;
        final String _staffname;
        final String _coursecodename;
        final String _thisYearCreditTime;
        final String _beforeYearCreditTime;
        final String _totalYearCreditTime;
        final String _beforeYearCompCredit;
        final String _beforeYearCredit;

        final List<Subclass> _subclassList = new ArrayList();
        final Map<String, Subclass> _subclassMap = new HashMap();
        final List<Map<String, String>> _hrAttendDateList = new ArrayList();
        final List<Map<String, String>> _lastYearHrAttendDateList = new ArrayList();

        Student(
                final String schregno,
                final String name,
                final String hrname,
                final String staffname,
                final String coursecodename,
                final String thisYearCreditTime,
                final String beforeYearCreditTime,
                final String totalYearCreditTime,
                final String beforeYearCompCredit,
                final String beforeYearCredit
        ) {
            _schregno = schregno;
            _name = name;
            _hrname = hrname;
            _staffname = staffname;
            _coursecodename = coursecodename;
            _thisYearCreditTime = thisYearCreditTime;
            _beforeYearCreditTime = beforeYearCreditTime;
            _totalYearCreditTime = totalYearCreditTime;
            _beforeYearCompCredit = beforeYearCompCredit;
            _beforeYearCredit = beforeYearCredit;
        }
        
//        public static List shukkouNissu(final List rowList) {
//            final Set set = new TreeSet();
//            for (final Iterator it = rowList.iterator(); it.hasNext();) {
//            	final Map row = (Map) it.next();
//            	
//                if ("SCHOOLING".equals(KnjDbUtils.getString(row, "TABLEDIV"))) {
//                	// スクーリング
//            		// スクーリング種別 = '2' (放送)は出校から除く
//            		if ("1".equals(KnjDbUtils.getString(row, "NAMESPARE1")) && !"1".equals(KnjDbUtils.getString(row, "M026_NAMESPARE1"))) {
//            			set.add(KnjDbUtils.getString(row, "EXECUTEDATE"));
//            		}
//                } else if ("SPECIAL".equals(KnjDbUtils.getString(row, "TABLEDIV"))) {
//                	// 特別活動
//            		if (!"1".equals(KnjDbUtils.getString(row, "M026_NAMESPARE1")) && null != KnjDbUtils.getString(row, "M027_NAME1")) {
//            			set.add(KnjDbUtils.getString(row, "EXECUTEDATE"));
//            		}
//                } else if ("TEST".equals(KnjDbUtils.getString(row, "TABLEDIV"))) {
//                	// テスト
//                    if (!"1".equals(KnjDbUtils.getString(row, "M026_NAMESPARE1"))) {
//                    	set.add(KnjDbUtils.getString(row, "EXECUTEDATE"));
//                    }
//                }
//            }
//            return new ArrayList(set);
//        }

        public static List shukkouNissuSagaken(final List rowList) {
            final Set set = new TreeSet();
            for (final Iterator it = rowList.iterator(); it.hasNext();) {
            	final Map row = (Map) it.next();
            	
                if ("SPECIAL".equals(KnjDbUtils.getString(row, "TABLEDIV"))) {
                	// 特別活動
                	if (!"1".equals(KnjDbUtils.getString(row, "M026_NAMESPARE1")) && null != KnjDbUtils.getString(row, "M027_NAME1")) {
            			set.add(KnjDbUtils.getString(row, "EXECUTEDATE"));
            		}
                }
            }
            return new ArrayList(set);
        }
        
//        public List getAttendDateList() {
//        	final List dateSet = new ArrayList();
//        	for (final Subclass subclass : _subclassList) {
//				for (final Attend at : subclass._attendList) {
//	                if ("1".equals(at._namespare1)) {
//	                	dateSet.add(at._executedate);
//	                }
//				}
//        	}
//			return dateSet;
//		}

		public String getTotalCredit(boolean isShutoku) {
        	Integer total = null;
        	for (final Subclass subclass : _subclassList) {
				String credit;
				if (isShutoku) {
					// 修得単位
					credit = subclass._getCredit;
				} else {
					// 履修単位
					credit = subclass._compCredit;
				}
				if (NumberUtils.isDigits(credit)) {
					if (null == total) {
						total = Integer.valueOf(credit);
					} else {
						total = new Integer(total.intValue() + Integer.parseInt(credit));
					}
				}
			}
        	
			return null == total ? null : total.toString();
		}
        
		private static List<Student> getStudentList(final DB2UDB db2, final Param param) {
        	final List<Student> list = new ArrayList();
            final Map<String, Student> studentMap = new HashMap();

            final String studentSql = getStudentSql(param);
            if (param._isOutputDebug) {
            	log.info("     studentSql = " + studentSql);
            }
            for (final Map row : KnjDbUtils.query(db2, studentSql)) {
            	final String schregno = KnjDbUtils.getString(row, "SCHREGNO");
            	if (null == studentMap.get(schregno)) {
            		final String name = KnjDbUtils.getString(row, "NAME");
            		final String hrname = KnjDbUtils.getString(row, "HR_NAME");
            		final String staffname = KnjDbUtils.getString(row, "STAFFNAME");
            		final String coursecodename = KnjDbUtils.getString(row, "COURSECODENAME");
            		
            		final String thisYearCreditTime = getDispNum(KnjDbUtils.getBigDecimal(row, "THIS_YEAR_SPECIALJISUU", null));
            		final String beforeYearCreditTime = getDispNum(KnjDbUtils.getBigDecimal(row, "BEFORE_YEAR_SPECIALJISUU", null));
            		final String totalYearCreditTime = getDispNum(KnjDbUtils.getBigDecimal(row, "TOTAL_YEAR_SPECIALJISUU", null));
            		final String beforeYearCompCredit = KnjDbUtils.getString(row, "BEFORE_YEAR_COMP_CREDIT");
            		final String beforeYearCredit = KnjDbUtils.getString(row, "BEFORE_YEAR_CREDIT");
            		
            		final Student student = new Student(schregno, name, hrname, staffname, coursecodename, thisYearCreditTime, beforeYearCreditTime, totalYearCreditTime, beforeYearCompCredit, beforeYearCredit);
            		list.add(student);
            		studentMap.put(schregno, student);
            	}
            	
            	if (null != KnjDbUtils.getString(row, "SUBCLASSCD")) {
            		final Student student = studentMap.get(schregno);
            		
            		final String year = KnjDbUtils.getString(row, "YEAR");
            		final String chaircd = KnjDbUtils.getString(row, "CHAIRCD");
            		final String subclassname = KnjDbUtils.getString(row, "SUBCLASSNAME");
            		final String repSeqAll = KnjDbUtils.getString(row, "REP_SEQ_ALL");
            		final String schSeqAll = KnjDbUtils.getString(row, "SCH_SEQ_ALL");
            		final String schSeqMin = KnjDbUtils.getString(row, "SCH_SEQ_MIN");
            		
            		final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD"); 
            		final Subclass subclass = new Subclass(year, chaircd, subclasscd, subclassname, repSeqAll, schSeqAll, schSeqMin);
            		student._subclassList.add(subclass);
            		student._subclassMap.put(subclass._subclasscd, subclass);
            	}
            }

            final String schAttendSql = getSchAttendSql(param, param._year);
            if (param._isOutputDebug) {
            	log.info("     schAttendSql = " + schAttendSql);
            }
            for (final Map row : KnjDbUtils.query(db2, schAttendSql)) {
                final Student student = studentMap.get(KnjDbUtils.getString(row, "SCHREGNO"));
                if (null == student) {
                    continue;
                }
                final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD"); 
                final Subclass subclass = student._subclassMap.get(subclasscd);
                if (null == subclass) {
                    continue;
                }
                Attend attend = new Attend(KnjDbUtils.getString(row, "SCHOOLINGKINDCD"), KnjDbUtils.getString(row, "NAMESPARE1"), KnjDbUtils.getString(row, "EXECUTEDATE"), KnjDbUtils.getString(row, "PERIODCD"), KnjDbUtils.getBigDecimal(row, "CREDIT_TIME", null));
                if (param._isOutputDebug) {
                	log.info(" student " + student._schregno + ", subclasscd = " + subclasscd + ", attend = " + attend._schoolingkindcd + ", " + attend._namespare1 + ", " + attend._executedate);
                }
				subclass._attendList.add(attend);
            }


            final String hrAttendSql = getHrAttendSql(param, false);
            if (param._isOutputDebug) {
            	log.info("     hrAttendSql = " + hrAttendSql);
            }
            for (final Map row : KnjDbUtils.query(db2, hrAttendSql)) {
                final Student student = studentMap.get(KnjDbUtils.getString(row, "SCHREGNO"));
                if (null == student) {
                    continue;
                }
                student._hrAttendDateList.add(row);
            }

            final String hrAttendLastyearSql = getHrAttendSql(param, true);
            for (final Map row : KnjDbUtils.query(db2, hrAttendLastyearSql)) {
                final Student student = studentMap.get(KnjDbUtils.getString(row, "SCHREGNO"));
                if (null == student) {
                    continue;
                }
                student._lastYearHrAttendDateList.add(row);
            }

            final String reportSql = getReportSql(param);
            if (param._isOutputDebug) {
            	log.info("     reportSql = " + reportSql);
            }
            for (final Map row : KnjDbUtils.query(db2, reportSql)) {
                final Student student = studentMap.get(KnjDbUtils.getString(row, "SCHREGNO"));
                if (null == student) {
                    continue;
                }
                final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD"); 
                final Subclass subclass = student._subclassMap.get(subclasscd);
                if (null == subclass) {
                    continue;
                }
                Report rep = new Report(KnjDbUtils.getString(row, "STANDARD_SEQ"), KnjDbUtils.getString(row, "STANDARD_DATE"), KnjDbUtils.getString(row, "TESTCD"), KnjDbUtils.getString(row, "REP_STANDARD_SEQ"), KnjDbUtils.getString(row, "NAMESPARE1"), KnjDbUtils.getString(row, "REPRESENT_SEQ"), KnjDbUtils.getString(row, "RECEIPT_DATE"), KnjDbUtils.getString(row, "GRAD_DATE"), KnjDbUtils.getString(row, "GRAD_VALUE"), KnjDbUtils.getString(row, "GRAD_VALUE_NAME"));
                if (param._isOutputDebug) {
                	log.info(" student " + student._schregno + ", subclasscd = " + subclasscd + ", report = " + rep._standardSeq + ", " + rep._repStandardSeq);
                }
				subclass._reportList.add(rep);
            }

            final String recordHistDatSql = getRecordScoreHistDatSql(param);
            if (param._isOutputDebug) {
            	log.info(" recordHistDatSql = " + recordHistDatSql);
            }
            for (final Map row : KnjDbUtils.query(db2, recordHistDatSql)) {

         	   final Student student = studentMap.get(KnjDbUtils.getString(row, "SCHREGNO"));
                if (null == student) {
                    continue;
                }
                final String subclasscd = KnjDbUtils.getString(row, "CLASSCD") + "-" + KnjDbUtils.getString(row, "SCHOOL_KIND") + "-"  + KnjDbUtils.getString(row, "CURRICULUM_CD") + "-" + KnjDbUtils.getString(row, "SUBCLASSCD"); 
                final Subclass subclass = student._subclassMap.get(subclasscd);
                if (null == subclass) {
                	log.info(" null subclass : " + subclasscd + " / " + student._schregno);
                    continue;
                }
                final String testcd = KnjDbUtils.getString(row, "SEMESTER") + "-" + KnjDbUtils.getString(row, "TESTKINDCD") + "-" + KnjDbUtils.getString(row, "TESTITEMCD") + "-" + KnjDbUtils.getString(row, "SCORE_DIV");
                if ("9-99-00-09".equals(testcd)) {
                    subclass._getCredit = KnjDbUtils.getString(row, "GET_CREDIT");
                    subclass._compCredit = KnjDbUtils.getString(row, "COMP_CREDIT");
                }
                if (StringUtils.defaultString(KnjDbUtils.getString(row, "TESTKINDCD")).startsWith("99")) {
                	subclass._testScoreMap.put(testcd, KnjDbUtils.getString(row, "VALUE"));
                } else {
                	subclass._testScoreMap.put(testcd, KnjDbUtils.getString(row, "SCORE"));
                }
                subclass._testSeqMap.put(testcd, KnjDbUtils.getString(row, "SEQ"));
            }

            return list;
        }
        
        private static String getStudentSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SPECIAL_ATTEND AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.SCHREGNO,  ");
            stb.append("         VALUE(CASE WHEN T1.YEAR = '" + param._year + "' THEN 'THIS' ELSE 'BEFORE' END, 'TOTAL') AS DIV, ");
            stb.append("         SUM(CAST(T1.CREDIT_TIME AS DECIMAL(5, 1))) AS SPECIALJISUU ");
            stb.append("     FROM ");
            stb.append("         SPECIALACT_ATTEND_DAT T1 ");
            stb.append("     INNER JOIN V_NAME_MST M027 ");
            stb.append("          ON M027.NAME1   = T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ");
            stb.append("         AND M027.NAMECD1 = 'M027' ");
            stb.append("         AND M027.YEAR = T1.YEAR ");
            stb.append("     WHERE ");
            stb.append("         (T1.YEAR < '" + param._year + "' OR T1.YEAR = '" + param._year + "' AND T1.ATTENDDATE <= '" + param._kijun + "') ");
            stb.append("          AND NOT EXISTS(SELECT 'X' FROM V_NAME_MST M026 ");
            stb.append("                         WHERE M026.NAME1      = T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ");
            stb.append("                           AND M026.YEAR       = T1.YEAR ");
            stb.append("                           AND M026.NAMECD1    = 'M026' ");
            stb.append("                           AND M026.NAMESPARE2 = '1' ) ");
            stb.append("     GROUP BY ");
            stb.append("         GROUPING SETS ( ");
            stb.append("             (T1.SCHREGNO), ");
            stb.append("             (T1.SCHREGNO, ");
            stb.append("              CASE WHEN T1.YEAR = '" + param._year + "' THEN 'THIS' ELSE 'BEFORE' END)) ");
            stb.append(" ) ");
            stb.append("     SELECT DISTINCT ");
            stb.append("         CHAIR.YEAR, ");
            stb.append("         CHAIR.CHAIRCD, ");
            stb.append("         CHAIR.CLASSCD || '-' || CHAIR.SCHOOL_KIND || '-' || CHAIR.CURRICULUM_CD || '-' || CHAIR.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("         SUBM.SUBCLASSNAME, ");
            stb.append("         CORR.REP_SEQ_ALL, ");
            stb.append("         CORR.SCH_SEQ_ALL, ");
            stb.append("         CORR.SCH_SEQ_MIN, ");
            stb.append("         BASE.SCHREGNO, ");
            stb.append("         BASE.NAME, ");
            stb.append("         REGDH.HR_NAME, ");
            stb.append("         STF.STAFFNAME , ");
            stb.append("         CRCM.COURSECODENAME, ");
            stb.append("         SPAT0.SPECIALJISUU AS THIS_YEAR_SPECIALJISUU, ");
            stb.append("         SPAT1.SPECIALJISUU AS BEFORE_YEAR_SPECIALJISUU, ");
            stb.append("         SPAT2.SPECIALJISUU AS TOTAL_YEAR_SPECIALJISUU, ");
            stb.append("         STBEF.COMP_CREDIT AS BEFORE_YEAR_COMP_CREDIT, ");
            stb.append("         CASE WHEN STBEF.ADD_CREDIT IS NULL THEN STBEF.GET_CREDIT ELSE VALUE(STBEF.GET_CREDIT, 0) + VALUE(STBEF.ADD_CREDIT, 0) END AS BEFORE_YEAR_CREDIT ");
            stb.append("     FROM ");
            stb.append("         SCHREG_BASE_MST BASE ");
            stb.append("     INNER JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = BASE.SCHREGNO ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR ");
            stb.append("         AND REGDH.SEMESTER = REGD.SEMESTER ");
            stb.append("         AND REGDH.GRADE = REGD.GRADE ");
            stb.append("         AND REGDH.HR_CLASS = REGD.HR_CLASS ");
            stb.append("     LEFT JOIN STAFF_MST STF ON STF.STAFFCD = REGDH.TR_CD1 ");
            stb.append("     LEFT JOIN COURSECODE_MST CRCM ON CRCM.COURSECODE = REGD.COURSECODE ");
            stb.append("     LEFT JOIN (SELECT DISTINCT YEAR, SEMESTER, CHAIRCD, SCHREGNO ");
            stb.append("                 FROM CHAIR_STD_DAT ");
            stb.append("                 WHERE YEAR = '" + param._year + "' ");
            stb.append("                ) STD ON STD.YEAR = REGD.YEAR ");
            stb.append("         AND STD.SEMESTER <= REGD.SEMESTER ");
            stb.append("         AND STD.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     LEFT JOIN CHAIR_DAT CHAIR ON STD.YEAR = CHAIR.YEAR ");
            stb.append("         AND STD.SEMESTER = CHAIR.SEMESTER ");
            stb.append("         AND STD.CHAIRCD = CHAIR.CHAIRCD ");
            stb.append("     LEFT JOIN CHAIR_CORRES_DAT CORR ON CORR.YEAR = CHAIR.YEAR ");
            stb.append("         AND CORR.CHAIRCD = CHAIR.CHAIRCD ");
            stb.append("     LEFT JOIN SUBCLASS_MST SUBM ON SUBM.CLASSCD = CHAIR.CLASSCD ");
            stb.append("         AND SUBM.SCHOOL_KIND = CHAIR.SCHOOL_KIND ");
            stb.append("         AND SUBM.CURRICULUM_CD = CHAIR.CURRICULUM_CD ");
            stb.append("         AND SUBM.SUBCLASSCD = CHAIR.SUBCLASSCD ");
            stb.append("     LEFT JOIN SPECIAL_ATTEND SPAT0 ON SPAT0.SCHREGNO = STD.SCHREGNO AND SPAT0.DIV = 'THIS' ");
            stb.append("     LEFT JOIN SPECIAL_ATTEND SPAT1 ON SPAT1.SCHREGNO = STD.SCHREGNO AND SPAT1.DIV = 'BEFORE' ");
            stb.append("     LEFT JOIN SPECIAL_ATTEND SPAT2 ON SPAT2.SCHREGNO = STD.SCHREGNO AND SPAT2.DIV = 'TOTAL' ");
            stb.append("     LEFT JOIN (SELECT SCHREGNO, SUM(COMP_CREDIT) AS COMP_CREDIT, SUM(GET_CREDIT) AS GET_CREDIT, SUM(ADD_CREDIT) AS ADD_CREDIT ");
            stb.append("                FROM SCHREG_STUDYREC_DAT ");
            stb.append("                WHERE YEAR < '" + param._year + "' ");
            stb.append("                GROUP BY SCHREGNO) STBEF ON STBEF.SCHREGNO = STD.SCHREGNO ");
            stb.append("     WHERE ");
            stb.append("         REGD.YEAR = '" + param._year + "' ");
            stb.append("         AND REGD.SEMESTER = '" + param._semester + "' ");
            stb.append("         AND (CHAIR.YEAR IS NULL OR CHAIR.CLASSCD <= '90') ");
            stb.append("         AND REGD.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            stb.append(" ORDER BY ");
            stb.append("     BASE.SCHREGNO, CHAIR.CLASSCD || '-' || CHAIR.SCHOOL_KIND || '-' || CHAIR.CURRICULUM_CD || '-' || CHAIR.SUBCLASSCD ");
            return stb.toString();
        }
        
        private static String getReportSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH MAX_REPRESENT_SEQ AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.YEAR, ");
            stb.append("         T1.CLASSCD, ");
            stb.append("         T1.SCHOOL_KIND, ");
            stb.append("         T1.CURRICULUM_CD, ");
            stb.append("         T1.SUBCLASSCD, ");
            stb.append("         T1.STANDARD_SEQ, ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         MAX(T1.REPRESENT_SEQ) AS REPRESENT_SEQ ");
            stb.append("     FROM ");
            stb.append("         REP_PRESENT_DAT T1 ");
            stb.append("     WHERE ");
            stb.append("         T1.YEAR = '" + param._year + "' ");
            stb.append("         AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            stb.append("     GROUP BY ");
            stb.append("         T1.YEAR, ");
            stb.append("         T1.CLASSCD, ");
            stb.append("         T1.SCHOOL_KIND, ");
            stb.append("         T1.CURRICULUM_CD, ");
            stb.append("         T1.SUBCLASSCD, ");
            stb.append("         T1.STANDARD_SEQ, ");
            stb.append("         T1.SCHREGNO ");
            stb.append(" ), MAX_RECEIPT_DATE AS ( ");
            stb.append("     SELECT  ");
            stb.append("         T1.YEAR, ");
            stb.append("         T1.CLASSCD, ");
            stb.append("         T1.SCHOOL_KIND, ");
            stb.append("         T1.CURRICULUM_CD, ");
            stb.append("         T1.SUBCLASSCD, ");
            stb.append("         T1.STANDARD_SEQ, ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.REPRESENT_SEQ, ");
            stb.append("         MAX(T1.RECEIPT_DATE) AS RECEIPT_DATE ");
            stb.append("     FROM ");
            stb.append("         REP_PRESENT_DAT T1 ");
            stb.append("     INNER JOIN MAX_REPRESENT_SEQ T2 ON T2.YEAR = T1.YEAR ");
            stb.append("         AND T2.CLASSCD = T1.CLASSCD ");
            stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("         AND T2.REPRESENT_SEQ = T1.REPRESENT_SEQ ");
            stb.append("     GROUP BY ");
            stb.append("         T1.YEAR, ");
            stb.append("         T1.CLASSCD, ");
            stb.append("         T1.SCHOOL_KIND, ");
            stb.append("         T1.CURRICULUM_CD, ");
            stb.append("         T1.SUBCLASSCD, ");
            stb.append("         T1.STANDARD_SEQ, ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.REPRESENT_SEQ ");
            stb.append(" ), PRINT_DATA AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.YEAR, ");
            stb.append("         T1.CLASSCD, ");
            stb.append("         T1.SCHOOL_KIND, ");
            stb.append("         T1.CURRICULUM_CD, ");
            stb.append("         T1.SUBCLASSCD, ");
            stb.append("         T1.STANDARD_SEQ, ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.REPRESENT_SEQ, ");
            stb.append("         T1.RECEIPT_DATE, ");
            stb.append("         T3.NAMESPARE1, ");
            stb.append("         T1.GRAD_DATE, ");
            stb.append("         T1.GRAD_VALUE, ");
            stb.append("         T3.ABBV1 AS GRAD_VALUE_NAME ");
            stb.append("     FROM ");
            stb.append("         REP_PRESENT_DAT T1 ");
            stb.append("     INNER JOIN MAX_RECEIPT_DATE T2 ON T2.YEAR = T1.YEAR ");
            stb.append("         AND T2.CLASSCD = T1.CLASSCD ");
            stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND T2.STANDARD_SEQ = T1.STANDARD_SEQ ");
            stb.append("         AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("         AND T2.REPRESENT_SEQ = T1.REPRESENT_SEQ ");
            stb.append("         AND T2.RECEIPT_DATE = T1.RECEIPT_DATE ");
            stb.append("     LEFT JOIN NAME_MST T3 ON T3.NAMECD1 = 'M003' ");
            stb.append("         AND T3.NAMECD2 = T1.GRAD_VALUE ");
            stb.append("     WHERE ");
            stb.append("         T2.RECEIPT_DATE <= '" + param._kijun + "' ");
            stb.append(" ), SUBCLASS_SCHREGNO AS ( ");
            stb.append("     SELECT DISTINCT ");
            stb.append("         T1.YEAR, ");
            stb.append("         T1.CLASSCD, ");
            stb.append("         T1.SCHOOL_KIND, ");
            stb.append("         T1.CURRICULUM_CD, ");
            stb.append("         T1.SUBCLASSCD, ");
            stb.append("         T1.SCHREGNO ");
            if ("1".equals(param._useRepStandarddateCourseDat)) {
                stb.append("       , T3.COURSECD ");
                stb.append("       , T3.MAJORCD ");
                stb.append("       , T3.COURSECODE ");
            }
            stb.append("     FROM ");
            stb.append("         SUBCLASS_STD_SELECT_DAT T1 ");
            stb.append("         INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            if ("1".equals(param._useRepStandarddateCourseDat)) {
                stb.append("         INNER JOIN SCHREG_REGD_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO ");
                stb.append("             AND T3.YEAR = T1.YEAR ");
                stb.append("             AND T3.SEMESTER = T1.SEMESTER ");
            }

            stb.append("     WHERE ");
            stb.append("         T1.YEAR = '" + param._year + "' ");
            stb.append("         AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            stb.append(" ), MAIN AS ( ");
            stb.append("     SELECT DISTINCT ");
            stb.append("         T1.YEAR, ");
            stb.append("         T1.CLASSCD, ");
            stb.append("         T1.SCHOOL_KIND, ");
            stb.append("         T1.CURRICULUM_CD, ");
            stb.append("         T1.SUBCLASSCD, ");
            stb.append("         T1.STANDARD_SEQ, ");
            stb.append("         T1.STANDARD_DATE, ");
            stb.append("         NMM002.NAMESPARE1 AS TESTCD, ");
            stb.append("         T2.SCHREGNO ");
            stb.append("     FROM ");
            if ("1".equals(param._useRepStandarddateCourseDat)) {
                stb.append("         REP_STANDARDDATE_COURSE_DAT T1 ");
            } else {
                stb.append("         REP_STANDARDDATE_DAT T1 ");
            }

            stb.append("     INNER JOIN SUBCLASS_SCHREGNO T2 ON T2.YEAR = T1.YEAR ");
            stb.append("         AND T2.CLASSCD = T1.CLASSCD ");
            stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useRepStandarddateCourseDat)) {
                stb.append("         AND T2.COURSECD = T1.COURSECD ");
                stb.append("         AND T2.MAJORCD = T1.MAJORCD ");
                stb.append("         AND T2.COURSECODE = T1.COURSECODE ");
            }

            stb.append("     LEFT JOIN NAME_MST NMM002 ON NMM002.NAMECD1 = 'M002' AND NMM002.NAMECD2 = T1.REPORTDIV ");
            stb.append(" ) ");
            stb.append("     SELECT ");
            stb.append("         T0.YEAR, ");
            stb.append("         T0.CLASSCD || '-' || T0.SCHOOL_KIND || '-' || T0.CURRICULUM_CD || '-' || T0.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("         T0.STANDARD_SEQ, ");
            stb.append("         T0.STANDARD_DATE, ");
            stb.append("         T0.TESTCD, ");
            stb.append("         T0.SCHREGNO, ");
            stb.append("         T1.STANDARD_SEQ AS REP_STANDARD_SEQ, ");
            stb.append("         T1.REPRESENT_SEQ, ");
            stb.append("         T1.RECEIPT_DATE, ");
            stb.append("         T1.NAMESPARE1, ");
            stb.append("         T1.GRAD_DATE, ");
            stb.append("         T1.GRAD_VALUE, ");
            stb.append("         T1.GRAD_VALUE_NAME ");
            stb.append("     FROM ");
            stb.append("         MAIN T0 ");
            stb.append("     LEFT JOIN PRINT_DATA T1 ON T1.YEAR = T0.YEAR ");
            stb.append("         AND T1.CLASSCD = T0.CLASSCD ");
            stb.append("         AND T1.SCHOOL_KIND = T0.SCHOOL_KIND ");
            stb.append("         AND T1.CURRICULUM_CD = T0.CURRICULUM_CD ");
            stb.append("         AND T1.SUBCLASSCD = T0.SUBCLASSCD ");
            stb.append("         AND T1.STANDARD_SEQ = T0.STANDARD_SEQ ");
            stb.append("         AND T1.SCHREGNO = T0.SCHREGNO ");
            stb.append("     ORDER BY ");
            stb.append("         T0.CLASSCD, ");
            stb.append("         T0.SCHOOL_KIND, ");
            stb.append("         T0.CURRICULUM_CD, ");
            stb.append("         T0.SUBCLASSCD, ");
            stb.append("         T0.STANDARD_SEQ, ");
            stb.append("         T1.REPRESENT_SEQ ");
            return stb.toString();
        }
        
        private static String getSchAttendSql(final Param param, final String year) {
            final StringBuffer stb = new StringBuffer();
            stb.append("     SELECT ");
            stb.append("         T1.YEAR, ");
            stb.append("         T2.SEMESTER, ");
            stb.append("         T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.SCHOOLINGKINDCD, ");
            stb.append("         T4.NAMESPARE1, ");
            stb.append("         T1.EXECUTEDATE, ");
            stb.append("         T1.PERIODCD, ");
            stb.append("         T1.CREDIT_TIME ");
            stb.append("     FROM ");
            stb.append("         SCH_ATTEND_DAT T1 ");
            stb.append("         INNER JOIN SEMESTER_MST T2 ON T2.YEAR = T1.YEAR ");
            stb.append("             AND T2.SEMESTER <> '9' ");
            stb.append("             AND T1.EXECUTEDATE BETWEEN T2.SDATE AND T2.EDATE ");
            stb.append("         INNER JOIN CHAIR_DAT T3 ON T3.YEAR = T1.YEAR ");
            stb.append("             AND T3.SEMESTER = T2.SEMESTER ");
            stb.append("             AND T3.CHAIRCD = T1.CHAIRCD ");
            stb.append("         LEFT JOIN NAME_MST T4 ON T4.NAMECD1 = 'M001' ");
            stb.append("             AND T4.NAMECD2 = T1.SCHOOLINGKINDCD ");
            stb.append("     WHERE ");
            stb.append("         T1.YEAR = '" + year + "' ");
            stb.append("         AND T1.EXECUTEDATE <= '" + param._kijun + "' ");
            stb.append("         AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            stb.append("         AND NOT EXISTS(SELECT 'X' FROM V_NAME_MST M026 ");
            stb.append("                         WHERE M026.NAME1      = T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD ");
            stb.append("                           AND M026.YEAR       = T1.YEAR ");
            stb.append("                           AND M026.NAMECD1    = 'M026' ");
            stb.append("                           AND M026.NAMESPARE2 = '1' ) ");
            stb.append("     ORDER BY ");
            stb.append("         T1.SCHREGNO ");
            stb.append("       , T3.SUBCLASSCD ");
            stb.append("       , T1.EXECUTEDATE ");
            stb.append("       , T1.PERIODCD ");
            return stb.toString();
        }
        
        private static String getHrAttendSql(final Param param, final boolean isLastYear) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCHREGNOS (SCHREGNO) AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.SCHREGNO ");
            stb.append("     FROM SCHREG_BASE_MST T1 ");
            stb.append("     WHERE T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     BASE.SCHREGNO, ");
            stb.append("     CASE WHEN T2.SCHREGNO IS NOT NULL THEN 'SCHOOLING' END AS TABLEDIV, ");
            stb.append("     T2.EXECUTEDATE, ");
            stb.append("     T5.SEMESTER ");
            stb.append("   , CHAIR.CLASSCD ");
            stb.append("   , CHAIR.CLASSCD || '-' || CHAIR.SCHOOL_KIND || '-' || CHAIR.CURRICULUM_CD || '-' || CHAIR.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("   , SUBM.SUBCLASSNAME ");
            stb.append("   , T2.CREDIT_TIME ");
            stb.append("   , T9.NAMESPARE1 ");
            stb.append("   , NM_M026.NAMESPARE1 AS M026_NAMESPARE1 ");
            stb.append("   , NM_M026.NAMESPARE2 AS M026_NAMESPARE2 ");
            stb.append("   , NM_M027.NAME1 AS M027_NAME1");
            stb.append(" FROM SCHREG_BASE_MST BASE ");
            stb.append(" INNER JOIN SCHREGNOS S1 ON S1.SCHREGNO = BASE.SCHREGNO ");
            stb.append(" LEFT JOIN SCH_ATTEND_DAT T2 ON T2.SCHREGNO = BASE.SCHREGNO ");
            stb.append(" LEFT JOIN SEMESTER_MST T5 ON T5.YEAR = T2.YEAR ");
            stb.append("     AND T5.SEMESTER <> '9' ");
            stb.append("     AND T2.EXECUTEDATE BETWEEN T5.SDATE AND T5.EDATE ");
            stb.append(" LEFT JOIN CHAIR_DAT CHAIR ON CHAIR.CHAIRCD = T2.CHAIRCD ");
            stb.append("     AND CHAIR.YEAR = T2.YEAR ");
            stb.append("     AND CHAIR.SEMESTER = T5.SEMESTER ");
            stb.append(" LEFT JOIN SUBCLASS_MST SUBM ON SUBM.CLASSCD = CHAIR.CLASSCD ");
            stb.append("     AND SUBM.SCHOOL_KIND = CHAIR.SCHOOL_KIND ");
            stb.append("     AND SUBM.CURRICULUM_CD = CHAIR.CURRICULUM_CD ");
            stb.append("     AND SUBM.SUBCLASSCD = CHAIR.SUBCLASSCD ");
            stb.append(" LEFT JOIN NAME_MST T9 ON T9.NAMECD1 = 'M001' ");
            stb.append("     AND T9.NAMECD2 = T2.SCHOOLINGKINDCD ");
            stb.append(" LEFT JOIN V_NAME_MST NM_M026 ON NM_M026.YEAR = T2.YEAR ");
            stb.append("     AND NM_M026.NAMECD1 = 'M026' ");
            stb.append("     AND NM_M026.NAME1 = CHAIR.CLASSCD || '-' || CHAIR.SCHOOL_KIND || '-' || CHAIR.CURRICULUM_CD || '-' || CHAIR.SUBCLASSCD ");
            stb.append(" LEFT JOIN V_NAME_MST NM_M027 ON NM_M027.YEAR = T2.YEAR ");
            stb.append("     AND NM_M027.NAMECD1 = 'M027' ");
            stb.append("     AND NM_M027.NAME1 = CHAIR.CLASSCD || '-' || CHAIR.SCHOOL_KIND || '-' || CHAIR.CURRICULUM_CD || '-' || CHAIR.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     T2.EXECUTEDATE <= '" + param._kijun + "' ");
            if (isLastYear) {
                stb.append("     AND T2.YEAR < '" + param._year + "' ");
            } else {
                stb.append("     AND T2.YEAR = '" + param._year + "' ");
            }
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     BASE.SCHREGNO, ");
            stb.append("     CASE WHEN T2.SCHREGNO IS NOT NULL THEN 'SPECIAL' END AS TABLEDIV, ");
            stb.append("     T2.ATTENDDATE AS EXECUTEDATE, ");
            stb.append("     T2.SEMESTER ");
            stb.append("   , T2.CLASSCD ");
            stb.append("   , CHAIR.CLASSCD || '-' || CHAIR.SCHOOL_KIND || '-' || CHAIR.CURRICULUM_CD || '-' || CHAIR.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("   , SUBM.SUBCLASSNAME ");
            stb.append("   , T2.CREDIT_TIME ");
            stb.append("   , CAST(NULL AS VARCHAR(1)) AS NAMESPARE1 ");
            stb.append("   , NM_M026.NAMESPARE1 AS M026_NAMESPARE1 ");
            stb.append("   , NM_M026.NAMESPARE2 AS M026_NAMESPARE2 ");
            stb.append("   , NM_M027.NAME1 AS M027_NAME1");
            stb.append(" FROM SCHREG_BASE_MST BASE ");
            stb.append(" INNER JOIN SCHREGNOS S1 ON S1.SCHREGNO = BASE.SCHREGNO ");
            stb.append(" LEFT JOIN SPECIALACT_ATTEND_DAT T2 ON T2.SCHREGNO = BASE.SCHREGNO ");
            stb.append(" LEFT JOIN CHAIR_DAT CHAIR ON CHAIR.CHAIRCD = T2.CHAIRCD ");
            stb.append("     AND CHAIR.YEAR = T2.YEAR ");
            stb.append("     AND CHAIR.SEMESTER = T2.SEMESTER ");
            stb.append(" LEFT JOIN SUBCLASS_MST SUBM ON SUBM.CLASSCD = CHAIR.CLASSCD ");
            stb.append("     AND SUBM.SCHOOL_KIND = CHAIR.SCHOOL_KIND ");
            stb.append("     AND SUBM.CURRICULUM_CD = CHAIR.CURRICULUM_CD ");
            stb.append("     AND SUBM.SUBCLASSCD = CHAIR.SUBCLASSCD ");
            stb.append(" LEFT JOIN V_NAME_MST NM_M026 ON NM_M026.YEAR = T2.YEAR ");
            stb.append("     AND NM_M026.NAMECD1 = 'M026' ");
            stb.append("     AND NM_M026.NAME1 = T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD ");
            stb.append(" LEFT JOIN V_NAME_MST NM_M027 ON NM_M027.YEAR = T2.YEAR ");
            stb.append("     AND NM_M027.NAMECD1 = 'M027' ");
            stb.append("     AND NM_M027.NAME1 = T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     T2.ATTENDDATE <= '" + param._kijun + "' ");
            if (isLastYear) {
                stb.append("     AND T2.YEAR < '" + param._year + "' ");
            } else {
                stb.append("     AND T2.YEAR = '" + param._year + "' ");
            }
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     BASE.SCHREGNO, ");
            stb.append("     CASE WHEN T2.SCHREGNO IS NOT NULL THEN 'TEST' END AS TABLEDIV, ");
            stb.append("     T2.INPUT_DATE AS EXECUTEDATE, ");
            stb.append("     T2.SEMESTER ");
            stb.append("   , T2.CLASSCD ");
            stb.append("   , T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("   , SUBM.SUBCLASSNAME ");
            stb.append("   , CAST(NULL AS DECIMAL(5, 1)) AS CREDIT_TIME ");
            stb.append("   , CAST(NULL AS VARCHAR(1)) AS NAMESPARE1 ");
            stb.append("   , NM_M026.NAMESPARE1 AS M026_NAMESPARE1 ");
            stb.append("   , CAST(NULL AS VARCHAR(1)) AS M026_NAMESPARE2 ");
            stb.append("   , CAST(NULL AS VARCHAR(1)) AS M027_NAME1");
            stb.append(" FROM SCHREG_BASE_MST BASE ");
            stb.append(" INNER JOIN SCHREGNOS S1 ON S1.SCHREGNO = BASE.SCHREGNO ");
            stb.append(" LEFT JOIN TEST_ATTEND_DAT T2 ON T2.SCHREGNO = BASE.SCHREGNO ");
            stb.append(" LEFT JOIN SUBCLASS_MST SUBM ON SUBM.CLASSCD = T2.CLASSCD ");
            stb.append("     AND SUBM.SCHOOL_KIND = T2.SCHOOL_KIND ");
            stb.append("     AND SUBM.CURRICULUM_CD = T2.CURRICULUM_CD ");
            stb.append("     AND SUBM.SUBCLASSCD = T2.SUBCLASSCD ");
            stb.append(" LEFT JOIN V_NAME_MST NM_M026 ON NM_M026.YEAR = T2.YEAR ");
            stb.append("     AND NM_M026.NAMECD1 = 'M026' ");
            stb.append("     AND NM_M026.NAME1 = T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     T2.INPUT_DATE <= '" + param._kijun + "' ");
            if (isLastYear) {
                stb.append("     AND T2.YEAR < '" + param._year + "' ");
            } else {
                stb.append("     AND T2.YEAR = '" + param._year + "' ");
            }
            return stb.toString();
        }
        
        
        private static String getRecordScoreHistDatSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append("     SELECT ");
            stb.append("         T1.* ");
            stb.append("     FROM ");
            stb.append("         V_RECORD_SCORE_HIST_DAT T1 ");
            stb.append("     WHERE ");
            stb.append("         T1.YEAR = '" + param._year + "' ");
            stb.append("         AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            return stb.toString();
        }
    }
    
    private static class Subclass {
        final String _year;
        final String _chaircd;
        final String _subclasscd;
        final String _subclassname;
        final String _repSeqAll;
        final String _schSeqAll;
        final String _schSeqMin;
        final List<Report> _reportList = new ArrayList();
        final List<Attend> _attendList = new ArrayList();
        final Map<String, String> _testScoreMap = new HashMap();
        final Map<String, String> _testSeqMap = new HashMap();
        String _getCredit;
        String _compCredit;

        Subclass(
                final String year,
                final String chaircd,
                final String subclasscd,
                final String subclassname,
                final String repSeqAll,
                final String schSeqAll,
                final String schSeqMin
        ) {
            _year = year;
            _chaircd = chaircd;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _repSeqAll = repSeqAll;
            _schSeqAll = schSeqAll;
            _schSeqMin = schSeqMin;
        }
        
        public boolean isTsuishi(final String testcd) {
        	final String seq = _testSeqMap.get(testcd);
        	return NumberUtils.isDigits(seq) && Integer.parseInt(seq) > 1;
        }
        
        public String getScore(final String testcd) {
        	final String score = _testScoreMap.get(testcd);
        	return (isTsuishi(testcd) ? "*" : "") + StringUtils.defaultString(score);
        }

        public String getAttendCount() {
        	final List list = new ArrayList();
        	boolean addval = false;
            final BigDecimal _1 = BigDecimal.ONE;
            BigDecimal n = new BigDecimal(0);
            for (final Attend at : _attendList) {
            	addval = true;
            	n = n.add(null == at._creditTime ? _1 : at._creditTime);
            }
            if (!list.isEmpty()) {
                addval = true;
                n = n.add(new BigDecimal(list.size()));
            }
            return addval ? getDispNum(n) : "0";
        }
        
        public List<String> getPrintAttendDateList() {
            final List<String> list = new ArrayList();
            for (final Attend at : _attendList) {
                if (null == at._executedate) {
                	continue;
                }
                if ("6".equals(at._schoolingkindcd)) { // 放送
                    list.add("放送");
                } else { // スクーリング
                    list.add(formatDate(at._executedate));
                }
            }
            return list;
        }
        
        public List getTestcdList() {
            final List list = new ArrayList();
            for (final Report rep : _reportList) {
                if (null != rep._testcd && !list.contains(rep._testcd)) {
                    list.add(rep._testcd);
                }
            }
            return list;
        }
    }
    
    private static class Report {
        final String _standardSeq; // REP_STANDARDDATE_DAT.STANDARD_SEQ
        final String _standardDate;
        final String _testcd;
        final String _repStandardSeq; // REP_REPORT_DAT.STANDARD_SEQ
        final String _namespare1;
        final String _representSeq;
        final String _receiptDate;
        final String _gradDate;
        final String _gradValue;
        final String _gradValueName;
        public Report(final String standardSeq, final String standardDate, final String testcd, final String repStandardSeq, final String namespare1, final String representSeq, final String receiptDate, final String gradDate, final String gradValue, final String gradValueName) {
            _standardSeq = standardSeq;
            _standardDate = standardDate;
            _testcd = testcd;
            _repStandardSeq = repStandardSeq;
            _namespare1 = namespare1;
            _representSeq = representSeq;
            _receiptDate = receiptDate;
            _gradDate = gradDate;
            _gradValue = gradValue;
            _gradValueName = null == gradValue ? "受" : gradValueName;
        }
    }
    
    private static class Attend {
        final String _schoolingkindcd;
        final String _namespare1;
        final String _executedate;
        final String _periodcd;
        final BigDecimal _creditTime;
        public Attend(final String schoolingkindcd, final String namespare1, final String executedate, final String periodcd, final BigDecimal creditTime) {
            _schoolingkindcd = schoolingkindcd;
            _namespare1 = namespare1;
            _executedate = executedate;
            _periodcd = periodcd;
            _creditTime = creditTime;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 77235 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _year;
        final String _semester;
        final String _gradeHrclass;
        final String[] _categorySelected;
        final String _kijun;
        final String _loginDate;
        final String _titleSelect;
        final String _useRepStandarddateCourseDat;

        final String _schoolKind;
        final boolean _hasSCHOOLMST_SCHOOL_KIND;
        final String _schoolName1;
        final String _principalStaffname;
        final Map _hreportRemarkTDat;
        final String _kijunFormatJp;
		final String _nendo;
		final boolean _isOutputDebug;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("GAKKI");
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _categorySelected = request.getParameterValues("category_selected");
            _kijun = request.getParameter("KIJUN").replace('/', '-');
            _loginDate = request.getParameter("LOGIN_DATE");
            _titleSelect = request.getParameter("TITLE_SELECT");
            _useRepStandarddateCourseDat = request.getParameter("useRepStandarddateCourseDat");

            _kijunFormatJp = KNJ_EditDate.h_format_JP(db2, _kijun);
            _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_year)) + "年度";
            _schoolKind = getSchoolKind(db2);
            _hasSCHOOLMST_SCHOOL_KIND = KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND");
            _schoolName1 = getSchoolName1(db2);
            _principalStaffname = getPrincipalname(db2);
            _hreportRemarkTDat = getHreportRemarkTDatMap(db2);
            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));
        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJM500E' AND NAME = '" + propName + "' "));
        }

        private String getSchoolKind(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _gradeHrclass.substring(0, 2) + "' "));
        }
        
        private String getSchoolName1(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOLNAME1 ");
            sql.append(" FROM SCHOOL_MST ");
            sql.append(" WHERE YEAR = '" + _year + "' ");
            if (_hasSCHOOLMST_SCHOOL_KIND) {
                sql.append("     AND SCHOOL_KIND = '" + _schoolKind + "' ");
            }
            return StringUtils.defaultString(KnjDbUtils.getOne(KnjDbUtils.query(db2, sql.toString())));
        }
        
        private String getPrincipalname(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT T1.STAFFCD, T2.STAFFNAME ");
            sql.append(" FROM STAFF_PRINCIPAL_HIST_DAT T1 ");
            sql.append(" INNER JOIN STAFF_MST T2 ON T2.STAFFCD = T1.STAFFCD ");
            sql.append(" WHERE T1.SCHOOL_KIND = '" + _schoolKind + "' ");
            sql.append("   AND T1.FROM_DATE = (SELECT MAX(FROM_DATE) FROM STAFF_PRINCIPAL_HIST_DAT WHERE SCHOOL_KIND = '" + _schoolKind + "' AND FROM_DATE <= '" + _kijun + "' ) ");
            return StringUtils.defaultString(KnjDbUtils.getString(KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql.toString())), "STAFFNAME"));
        }

        private Map getHreportRemarkTDatMap(final DB2UDB db2) {
            final String sql = " SELECT REMARKID, REMARK FROM HREPORTREMARK_T_DAT WHERE REMARKID IN ('1', '2', '3', '4', '5', '6') ";
            return KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, sql), "REMARKID", "REMARK");
        }
    }
}

// eof

