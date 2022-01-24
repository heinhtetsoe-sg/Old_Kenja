// kanji=漢字
/*
 * $Id: c1da3bb777c174a181fe2c201da968272f1e95d0 $
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJC;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
import org.apache.commons.lang.ArrayUtils;
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
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/*
 *  学校教育システム 賢者 [出欠管理]  欠時記入表
 */

public class KNJC153A {

    private static final Log log = LogFactory.getLog(KNJC153A.class);
    private static String FROM_TO_MARK = "\uFF5E";

//    private static final int KANSAN0_NASI = 0;                       // 換算0：無し
//    private static final int KANSAN1_SEISU_GAKKI = 1;                // 換算1：学期ごと整数
//    private static final int KANSAN2_SEISU_NENKAN = 2;               // 換算2：年間で整数
    private static final int KANSAN3_SYOSU_GAKKI = 3;                // 換算3：学期ごと小数
    private static final int KANSAN4_SYOSU_NENKAN = 4;               // 換算4：年間で小数
//    private static final int KANSAN5_SEISU_NENKAN_AMARIKURIAGE = 5;  // 換算5：年間で整数（余り繰り上げ）

    private static final int SEMES_ALL = 9;
    
    private static final String AMIKAKE_ATTR1 = "Paint=(1,80,1),Bold=1";

    private boolean _hasdata;

    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;              // Databaseクラスを継承したクラス

        try {
            // print svf設定
            response.setContentType("application/pdf");
            svf.VrInit();                                         //クラスの初期化
            svf.VrSetSpoolFileStream(response.getOutputStream());     //PDFファイル名の設定

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);    //Databaseクラスを継承したクラス
            db2.open();

            KNJServletUtils.debugParam(request, log);
            // パラメータの取得
            final Param param = new Param(request, db2);

            printSvf(db2, svf, param);

        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            // 終了処理
            if (!_hasdata) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
            svf.VrQuit();
            try {
                db2.close();
            } catch (Exception ex) {
                log.error("db close error!", ex);
            }//try-cathの括り
        }

    }

    /**
     *  svf print 印刷処理
     */
    private void printSvf(final DB2UDB db2, final Vrw32alp svf, final Param param) {
    	
        final String form = "KNJC153A.frm";   //２学期制用 50行
        
        final int LINE = 50;
        
        for (int chi = 0; chi < param._chairList.size(); chi++) {
        	final Chair chair = (Chair) param._chairList.get(chi);

        	chair.setStudent(db2, param, LINE);

            // １ページに入る範囲の生徒単位で処理を行う
			for (final Iterator pit = chair._studentPageMap.entrySet().iterator(); pit.hasNext();) {
            	
                final Map.Entry e = (Map.Entry) pit.next();
                final Map hm1 = (Map) e.getValue();
                
                svf.VrSetForm(form, 1);

                printHeader(svf, param, chair);

                //  総ページ数
                final Integer creditMax = chair.getCredit(db2, svf, param);
                if (null != creditMax && 0 < creditMax.intValue()) {
                    svf.VrsOut("CREDIT", String.valueOf(creditMax.intValue()));   //単位
                }

                printAtetnd(param, svf, chair, hm1, LINE);      //出欠集計の取得と出力のメソッド
                svf.VrEndPage();
                _hasdata = true;
            }
        }
    }

	private void printHeader(final Vrw32alp svf, final Param param, final Chair chair) {
		svf.VrsOut("TITLE",      StringUtils.defaultString(param._nendo) + "　欠時記入表");
		svf.VrsOut("scope_day",  param._dateFromTo); //印刷範囲
		svf.VrsOut("TODAY",      param._ctrlDateString);
		svf.VrsOut("STAFFNAME",  chair._staffName);
		svf.VrsOut("class",      chair._chairName);
		svf.VrsOut("CLASSNAME",  chair._subclassName);

		for (int i = 1; i <= param._semesdiv; i++) {
		    final Semester semester = param.getSemester(String.valueOf(i));
		    svf.VrsOut("SEMESTER" + i, semester._semestername);
		    for (int j = 0; j < semester._testItemList.size(); j++) {
		    	final TestItem testItem = (TestItem) semester._testItemList.get(j);
		    	if (null != testItem) {
		            svf.VrsOut("TESTNAME" + i + "_" + String.valueOf(j + 1), testItem._testitemname);
		    	}
		    }
		}
		svf.VrsOut("SEMESTER9", "学年累計");
		svf.VrsOut("RANGE", param._dateFromTo);

		svf.VrsOut("NOTE5", "　");
		svf.VrAttribute("NOTE5", AMIKAKE_ATTR1);
		svf.VrsOut("NOTE6", "：1/3で欠時超過");

		final List note34 = new ArrayList();
		if ("1".equals(param._knjSchoolMst._subOffDays)) {
			note34.add("※備考欄の休学の（）内の数字は、休学欠課数を表す。");
		}
		final String footerText = param.getFooterText();
		if (null != footerText) {
			note34.add(footerText);
		}
		for (int ni = 0; ni < note34.size(); ni++) {
			svf.VrsOut("NOTE" + String.valueOf(3 + ni), (String) note34.get(ni));
		}
	}

    private static String notZeroInt(final double sum) {
        if (sum <= 0.0) {
            return "";
        }
        return String.valueOf((int) sum);
    }

    /**
     *  svf print 出欠集計取得と印刷
     */
    private void printAtetnd(final Param param, final Vrw32alp svf, final Chair chair, final Map hm1, final int maxLine) {

        for (int line = 1; line <= maxLine; line++) {
            final Student student = chair.getStudent(hm1, line);
            if (null != student) {
                //生徒名等出力のメソッド
                if (null != student._rsMap) {
                    svf.VrsOutn("HR_NAME",   line, (String) student._rsMap.get("ATTENDNO")); //学年組出席番号
                    svf.VrsOutn("NAME", line, (String) student._rsMap.get("NAME"));     //生徒名

                    //備考の編集＆出力
                    final String str = student.getSchTransferInfo(param);
                    if (str != null) {
                        svf.VrsOutn("REMARK", line, str);   //備考
                    }
                }
            }
        }

        final Map studentSumMap = new TreeMap();                //出欠の累計用配列 [生徒の列インデクス] 学期累計用配列 4項目づつ
        final Map chairSum = new TreeMap();                 //クラス合計用配列 [学期] 学期集計および累計用配列

        final int sem9 = SEMES_ALL;
        final Attendance hrSum9 = Attendance.get(chairSum, String.valueOf(SEMES_ALL));
        for (final Iterator stit = hm1.keySet().iterator(); stit.hasNext();) {
            final String schregno = (String) stit.next();
            final Student student = (Student) hm1.get(schregno);
            if (null == student) {
                continue;
            }

            for (final Iterator it = param._testitemMap.values().iterator(); it.hasNext();) {
            	final TestItem testItem = (TestItem) it.next();
                final Attendance ate = (Attendance) student._attendance.get(testItem._dateRange._key);
                final Semester semester = testItem._semester;
                final int testItemidx = semester._testItemList.indexOf(testItem);
                
                //log.info(" testItem = " + testItem + ", idx = " + testItemidx);
                
                if (null != ate && -1 != testItemidx) {
                	final String idx = String.valueOf(testItemidx + 1);
                	final int sem = Integer.parseInt((String) semester._key);
                	//学期集計の出力
                	printKesseki(param, svf, "TOTAL_ABSENCE" + sem + "_" + idx, student, ate._kesseki, ate._lesson);
                	svf.VrsOutn("TOTAL_ABSENCE" + sem + "_" + idx, student._line, notZeroInt(ate._kesseki));    //欠席
                	svf.VrsOutn("TOTAL_SUSPEND" + sem + "_" + idx, student._line, notZeroInt(ate._suspend));    //出停
					final String kekkaStr = Attendance.getSumstr(param, ate._srcKekka);
                	svf.VrsOutn("TOTAL_TIME" + sem + "_" + idx + (KNJ_EditEdit.getMS932ByteLength(kekkaStr) > 4 ? "_2" : ""), student._line, kekkaStr);
                	svf.VrsOutn("TOTAL_JUJITIME" + sem + "_" + idx, student._line, notZeroInt(ate._lesson));
                	
                	//生徒別累計処理
                	Attendance.get(studentSumMap, new Integer(student._line)).add(ate);
                	
                	//学期別累計処理
                	Attendance.get(chairSum, testItem._dateRange._key).add(ate);
                }
            }
            
        	final Attendance studentSum = Attendance.get(studentSumMap, new Integer(student._line));
        	printKesseki(param, svf, "TOTAL_ABSENCE" + sem9, student, studentSum._kesseki, studentSum._lesson);
			hrSum9._kesseki += studentSum._kesseki;

			final String sumSuspendStr = Attendance.getSumstr(param, studentSum._suspend);
			svf.VrsOutn("TOTAL_SUSPEND" + sem9 + (KNJ_EditEdit.getMS932ByteLength(sumSuspendStr) > 4 ? "_2" : ""), student._line, sumSuspendStr);  //欠席
			hrSum9._suspend += studentSum._suspend;

//            final double late;
//            final double early;
//            if (param.isConvOnSemester() || param.isPrintRawLateEarly()) {
//                late = studentSum._srcLate;
//                early = studentSum._srcEarly;
//            } else{
//                late = Attendance.lateonyear(param, studentSum._srcLate, studentSum._srcEarly);
//                early = Attendance.earlyonyear(param, studentSum._srcLate, studentSum._srcEarly);
//            }
//            hrSum9._srcLate += late;
//            hrSum9._srcEarly += early;

			final String kekkaStr = Attendance.getSumstr(param, studentSum._srcKekka);
        	svf.VrsOutn("TOTAL_TIME" + sem9 + (KNJ_EditEdit.getMS932ByteLength(kekkaStr) > 4 ? "_2" : ""), student._line, kekkaStr);
        	hrSum9._srcKekka += studentSum._srcKekka;

        	//時数
            svf.VrsOutn("TOTAL_JUJITIME" + sem9, student._line, Attendance.getSumstr(param, studentSum._lesson));
            hrSum9._lesson += studentSum._lesson;
        }

        // 最下段累計出力処理
        final int lastLine = 51;
        for (final Iterator it = param._testitemMap.values().iterator(); it.hasNext();) {
        	final TestItem testItem = (TestItem) it.next();
            final Attendance hrSumi = (Attendance) Attendance.get(chairSum, testItem._dateRange._key);
            final int testItemidx = testItem._semester._testItemList.indexOf(testItem);
            if (-1 != testItemidx) {
            	final int sem = Integer.parseInt((String) testItem._semester._key);
            	final String idx = String.valueOf(testItemidx + 1);
            	svf.VrsOutn("TOTAL_ABSENCE" + sem + "_" + idx, lastLine, notZeroInt(hrSumi._kesseki)); //欠席
            	svf.VrsOutn("TOTAL_SUSPEND" + sem + "_" + idx, lastLine, notZeroInt(hrSumi._suspend)); //出停
            	final String sumStr = notZeroInt(hrSumi._srcKekka); //出停
            	svf.VrsOutn("TOTAL_TIME" + sem + "_" + idx + (KNJ_EditEdit.getMS932ByteLength(sumStr) > 4 ? "_2" : ""), lastLine, sumStr); //欠課
            	svf.VrsOutn("TOTAL_JUJITIME" + sem + "_" + idx, lastLine, notZeroInt(hrSumi._lesson)); //時数
            }
        }

        svf.VrsOutn("TOTAL_ABSENCE" + sem9, lastLine, notZeroInt(hrSum9._kesseki)); //欠席
        svf.VrsOutn("TOTAL_SUSPEND" + sem9, lastLine, notZeroInt(hrSum9._suspend)); //出停
		final String sumStr = Attendance.getSumstr(param, hrSum9._srcKekka); //出停
        svf.VrsOutn("TOTAL_TIME" + sem9 + (KNJ_EditEdit.getMS932ByteLength(sumStr) > 4 ? "_2" : ""), lastLine, sumStr); //欠課
        svf.VrsOutn("TOTAL_JUJITIME" + sem9, lastLine, notZeroInt(hrSum9._lesson)); //時数
    }

	private void printKesseki(final Param param, final Vrw32alp svf, final String field, final Student student, final double kesseki, final double lesson) {
		if (kesseki != 0) {
			final String sumKessekiStr = Attendance.getSumstr(param, kesseki);
			final double absencehigh = new BigDecimal(lesson).divide(new BigDecimal(3), 1, BigDecimal.ROUND_HALF_UP).doubleValue(); // 1/3
			final String field1 = field + (KNJ_EditEdit.getMS932ByteLength(sumKessekiStr) > 4 ? "_2" : "");
		    if (0 < lesson && 0 < absencehigh && absencehigh < kesseki) {
		        svf.VrAttributen(field1, student._line, AMIKAKE_ATTR1);
		    }
		    svf.VrsOutn(field1, student._line, sumKessekiStr);  //欠席
		}
	}
    
    private static class Student {
        final String _schregno;
        final int _line;
        final Map _attendance = new TreeMap();
        final String _grade;
        final String _hrClass;
        int _gradeInt;
        Map _rsMap;
        Student(final String schregno, final int line, final String grade, final String hrClass) {
            _schregno = schregno;
            _line = line;
            _grade = grade;
            _hrClass = hrClass;
        }
        
        /**
         *   備考編集
         */
        public String getSchTransferInfo(final Param param) {
            String str = null;
            try {
                // 異動情報の優先順位は 「留学・休学 > 退学・転学 > 転入・編入」 とする
                final String trainsfercd = (String) _rsMap.get("TRANSFERCD");
                final String kbnDate2 = (String) _rsMap.get("KBN_DATE2");
                final String kbnDate2e = (String) _rsMap.get("KBN_DATE2E");
                final String kbnName2 = StringUtils.defaultString((String) _rsMap.get("KBN_NAME2"));

                final String kbnName1 = StringUtils.defaultString((String) _rsMap.get("KBN_NAME1"));
                final String kbnDate1 = (String) _rsMap.get("KBN_DATE1");

                final String kbnDate2sSemester = (String) _rsMap.get("KBN_DATE2_SSEMESTER");
                final String kbnDate2eSemester = (String) _rsMap.get("KBN_DATE2_ESEMESTER");

                if (kbnDate2 != null) {
                    final Calendar cala = Param.toCal(param._dateTo);  //印刷範囲TO
                    final Calendar calb = Param.toCal(kbnDate2);
                    final Calendar calc = Param.toCal(kbnDate2e);
                    if (!calb.after(cala)) {
                        str = param.sdf2.format(calb.getTime()) + FROM_TO_MARK + param.sdf2.format(calc.getTime()) + kbnName2;
                        if ("1".equals(param._knjSchoolMst._subOffDays) && "2".equals(trainsfercd)) {
                            final String offdaysCount = getSchregOffdays(param, kbnDate2sSemester, kbnDate2eSemester);
                            str += "（" + offdaysCount + "）"; // 休学が欠課に含まれるとき備考に休学時数を表示する
                        }
                    }
                } else if (kbnDate1 != null) {
                    final Calendar cala = Param.toCal(param._dateTo);  //印刷範囲TO
                    final Calendar calb = Param.toCal(kbnDate1);
                    if (!calb.after(cala)) {
                        str = param.sdf3.format(calb.getTime()) + kbnName1;
                    }
                }
            } catch (Exception ex) {
                log.error("setSchTransferInfo error!", ex);
            }
            return str;
        }
        

        /**
         *  svf print 出欠集計取得
         */
        private String getSchregOffdays(final Param param, final String kbnDate2sSemester, final String kbnDate2eSemester) {

            String offdays = "";
            for (final Iterator it = param._semesterList.iterator(); it.hasNext();) {
                final Semester semester = (Semester) it.next();
                                                 //学期コード
                if (String.valueOf(SEMES_ALL).equals(semester._key)) {
                    break;
                }
                if (Param.toCal(param._dateTo).before(Param.toCal(semester._sdate))) {
                    break;   //印刷終了日が学期開始日の前なら以降の学期は出力しない！
                }
                if (null != kbnDate2sSemester && Integer.parseInt(kbnDate2sSemester) > Integer.parseInt(semester._key)) {
             	   continue;
                }
                if (null != kbnDate2eSemester && Integer.parseInt(kbnDate2eSemester) < Integer.parseInt(semester._key)) {
             	   continue;
                }
                final Attendance ate = (Attendance) _attendance.get(semester._key);
                if (null != ate) {
                    if (ate._offdays > 0) {
                 	   offdays = String.valueOf((NumberUtils.isDigits(offdays) ? Integer.parseInt(offdays) : 0) + (int) ate._offdays);
                    }
                }
            }
            return offdays;
        }

    }

    private static class Attendance {
        // [0:欠席 1:遅刻 2:早退 3:欠課時数 4:時数]
        double _kesseki;
//        double _srcLate;
//        double _srcEarly;
        double _srcKekka;
        double _lesson;
        double _suspend;
        double _offdays;
        public void add(final Attendance att) {
            _kesseki += att._kesseki;
//            _srcLate += att._srcLate;
//            _srcEarly += att._srcEarly;
            _srcKekka += att._srcKekka;
            _lesson += att._lesson;
            _suspend += att._suspend;
            _offdays += att._offdays;
        }
        
        public static Attendance get(final Map m, final Object key) {
            if (null == m.get(key)) {
            	m.put(key, new Attendance());
            }
            final Attendance src = (Attendance) m.get(key);
            return src;
        }
        
//        /**
//         *  ペナルティー欠課換算後の遅刻算出（換算数を差し引く）
//         */
//        private static double lateonyear(final Param param, final double srcLate, final double srcEarly) {
//            double late = 0;
//            try {
//                final int lateearly = (int) (srcLate + srcEarly);
//                if (param._absentConv == KANSAN5_SEISU_NENKAN_AMARIKURIAGE) {
//                    final int amari = 0 != getKuriage(param, lateearly) ? Integer.parseInt(param._knjSchoolMst._amariKuriage) : 0;
//                    if( srcLate - (lateearly / param._absentConvLate * param._absentConvLate + amari) < 0) {
//                        late = 0;
//                    } else {
//                        late = srcLate - (lateearly / param._absentConvLate * param._absentConvLate + amari);
//                    }
//                } else if (param._absentConv == KANSAN4_SYOSU_NENKAN) {
//                    late = 0; // すべて欠課へ換算
//                } else if( srcLate - lateearly / param._absentConvLate * param._absentConvLate < 0) {
//                    late = 0;
//                } else {
//                    late = srcLate - lateearly / param._absentConvLate * param._absentConvLate;
//                }
//            } catch (Exception ex) {
//                log.error("error! ", ex);
//            }
//            return late;
//        }

//        /**
//         *  ペナルティー欠課換算後の早退算出（換算数を差し引く）
//         */
//        private static double earlyonyear(final Param param, final double srcLate, final double srcEarly) {
//            double early = 0;
//            try {
//                final int lateearly = (int) (srcLate + srcEarly);
//                if (param._absentConv == KANSAN5_SEISU_NENKAN_AMARIKURIAGE) {
//                    final int amari = 0 != getKuriage(param, lateearly) ? Integer.parseInt(param._knjSchoolMst._amariKuriage) : 0;
//                    if( srcLate - (lateearly / param._absentConvLate * param._absentConvLate  + amari) < 0) {
//                        early = srcEarly + ( srcLate - (lateearly / param._absentConvLate * param._absentConvLate  + amari));
//                    } else {
//                        early = srcEarly;
//                    }
//                } else if (param._absentConv == KANSAN4_SYOSU_NENKAN) {
//                    early = 0; // すべて欠課へ換算
//                } else if( srcLate - lateearly / param._absentConvLate * param._absentConvLate < 0) {
//                    early = srcEarly + ( srcLate - lateearly / param._absentConvLate * param._absentConvLate );
//                } else {
//                    early = srcEarly;
//                }
//            } catch (Exception ex) {
//                log.error("error! ", ex);
//            }
//            return early;
//        }

//        /**
//         *  ペナルティー欠課換算後の遅刻算出（換算数を差し引く）
//         */
//        private static int getKuriage(final Param param, final int lateEarly) {
//            try {
//                final int _absentCovLate = Integer.parseInt(param._knjSchoolMst._absentCovLate);
//                final int _amariKuriage = Integer.parseInt(param._knjSchoolMst._amariKuriage);
//                return lateEarly % _absentCovLate >= _amariKuriage ? 1 : 0;
//            } catch (Exception e) {
//                log.error("Exception:", e);
//            }
//            return 0;
//        }
        
        /**
         *  出欠累計の編集 ゼロは否出力
         */
        private static String getSumstr(final Param param, final double sum) {
            if (sum <= 0.0) {
                return "";
            }

            final String rtn;
            if (param.isAttendPrintInt()) {
                rtn = String.valueOf((int) sum);
            } else {
                rtn = new BigDecimal(sum).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
            }
            return rtn;

        }
    }
    
    private static class Chair {
    	final String _chaircd;
        String _chairName;
        String _subclassName;
        String _staffName;
        String _subclassAbbv;
        String _schoolKind;
        String _curriculumCd;
        final List _studentList = new ArrayList();
        Map _studentPageMap;
        
        Chair(final String chaircd) {
        	_chaircd = chaircd;
        }

        /**
         * 指定行の生徒を得る
         * @param schregnoLineMap 生徒と行のマップ
         * @param line 行
         * @return 指定行の生徒
         */
        private Student getStudent(final Map schregnoLineMap, final int line) {
            for (final Iterator it = schregnoLineMap.keySet().iterator(); it.hasNext();) {
                final String schregno = (String) it.next();
                final Student student = (Student) schregnoLineMap.get(schregno);
                if (student._line == line) {
                    return student;
                }
            }
            return null;
        }

        /**
         *  講座名、科目名を取得するメソッド
         */
        private void setChairInfo(final DB2UDB db2, final Param param) {
            
            final StringBuffer stb = new StringBuffer();
            stb.append("SELECT W1.CHAIRNAME, W2.SUBCLASSNAME, W2.SUBCLASSABBV ");
            stb.append(       ",W1.SCHOOL_KIND ");
            stb.append(       ",W1.CURRICULUM_CD ");
            stb.append("FROM CHAIR_DAT W1 ");
            stb.append("INNER JOIN SUBCLASS_MST W2 ON ");
            stb.append(       "    W1.CLASSCD = W2.CLASSCD ");
            stb.append(       "AND W1.SCHOOL_KIND = W2.SCHOOL_KIND ");
            stb.append(       "AND W1.CURRICULUM_CD = W2.CURRICULUM_CD ");
            stb.append(       "AND W1.SUBCLASSCD = W2.SUBCLASSCD ");
            stb.append("WHERE  W1.YEAR='" + param._year + "' AND W1.SEMESTER='" + param._semester + "' AND ");
            stb.append(       "W1.CHAIRCD='" + _chaircd + "' ");

            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, stb.toString()));
            _chairName = KnjDbUtils.getString(row, "CHAIRNAME");
            _subclassName = KnjDbUtils.getString(row, "SUBCLASSNAME");
            _subclassAbbv = KnjDbUtils.getString(row, "SUBCLASSABBV");
            _schoolKind = KnjDbUtils.getString(row, "SCHOOL_KIND");
            _curriculumCd = KnjDbUtils.getString(row, "CURRICULUM_CD");
        }

        /**
         *  講座担任名を取得するメソッド
         */
        private void setChairStaff(final DB2UDB db2, final Param param) {

            final StringBuffer stb = new StringBuffer();
            stb.append("SELECT  STAFFNAME ");
            stb.append("FROM    CHAIR_STF_DAT W1 ");
            stb.append(" INNER JOIN STAFF_MST W2 ON W2.STAFFCD = W1.STAFFCD ");
            stb.append("WHERE   W1.YEAR = '" + param._year + "' ");
            stb.append(    "AND W1.SEMESTER = '" + param._semester + "' ");
            stb.append(    "AND W1.CHAIRCD = '" + _chaircd + "' ");
            stb.append(    "AND W1.CHARGEDIV = 1 ");

            _staffName = KnjDbUtils.getOne(KnjDbUtils.query(db2, stb.toString()));
        }
        
        private void setStudent(final DB2UDB db2, final Param param, final int maxLine) {
            final Map pageMap = new TreeMap();
            //  生徒名等ResultSet作成
            final String sql = getStudentSql(param);
            
            if (param._isOutputDebug) {
            	log.info(" sql = " + sql);
            }
            
            _studentList.clear();
            
            //  生徒名等SVF出力
            Integer page = new Integer(1);
            int line = 0;
            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
            	final Map row = (Map) it.next();
                line += 1;
                final String schregno = KnjDbUtils.getString(row, "SCHREGNO");
                final String grade = KnjDbUtils.getString(row, "GRADE");
				final Student student = new Student(schregno, line, grade, KnjDbUtils.getString(row, "HR_CLASS"));
                if (NumberUtils.isDigits(grade)) {
                    student._gradeInt = Integer.parseInt(grade);
                }
                student._rsMap = row;
                if (null == pageMap.get(page)) {
                    pageMap.put(page, new HashMap());
                }
                final Map hm1 = (Map) pageMap.get(page);                                        //学籍番号と行番号の保管
                hm1.put(schregno, student);     //行番号に学籍番号を付ける
                if (line == maxLine) {
                    page = new Integer(page.intValue() + 1);
                    line = 0;
                }
                _studentList.add(student);
            }
            _studentPageMap = pageMap;
            
            setAttendance(db2, param);
        }
        
        /**
         *  PrepareStatement作成  生徒情報
         **/
        private String getStudentSql(final Param param) {
            final StringBuffer stb = new StringBuffer();

            stb.append("WITH SCHNO_A AS(");
            stb.append("    SELECT  SCHREGNO, MAX(SEMESTER) AS SEMESTER ");
            stb.append("    FROM    CHAIR_STD_DAT ");
            stb.append("    WHERE   YEAR = '" + param._year + "' ");
            stb.append("        AND SEMESTER <= '" + param._semester + "' ");
            stb.append("        AND CHAIRCD = '" + _chaircd + "' ");
            stb.append("        AND '" + param._ctrldate + "' BETWEEN APPDATE AND APPENDDATE ");
            stb.append("        GROUP BY SCHREGNO ");
            stb.append(") ");

            stb.append(",TRANSFER_A AS(");
            stb.append("    SELECT  SCHREGNO,MAX(TRANSFER_SDATE) AS TRANSFER_SDATE ");
            stb.append("    FROM    SCHREG_TRANSFER_DAT S1,SEMESTER_MST S3 ");
            stb.append("    WHERE   TRANSFERCD IN('1','2') ");
            stb.append("        AND EXISTS(SELECT 'X' FROM SCHNO_A S2 WHERE S2.SCHREGNO = S1.SCHREGNO) ");
            stb.append("        AND FISCALYEAR(S1.TRANSFER_SDATE) = '" + param._year + "' ");
            stb.append("        AND S1.TRANSFER_SDATE < S3.EDATE ");
            stb.append("        AND S3.YEAR = '" + param._year + "' ");
            stb.append("        AND S3.SEMESTER <= '" + param._semester + "' ");
            stb.append("    GROUP BY SCHREGNO ");
            stb.append(") ");

            stb.append(",TRANSFER_B AS(");
            stb.append("    SELECT  SCHREGNO, TRANSFER_SDATE, TRANSFER_EDATE, TRANSFERCD ");
            stb.append("    FROM    SCHREG_TRANSFER_DAT S1 ");
            stb.append("    WHERE   EXISTS(SELECT 'X' FROM TRANSFER_A S2 WHERE S1.SCHREGNO = S2.SCHREGNO ");
            stb.append("                                                   AND S1.TRANSFER_SDATE = S2.TRANSFER_SDATE) ");
            stb.append(") ");

            stb.append("SELECT  REGD.SCHREGNO, ");
            stb.append("        BASE.NAME, ");
            stb.append("        REGDH.HR_NAMEABBV || '-' || CHAR(INT(REGD.ATTENDNO)) AS ATTENDNO, ");
            stb.append("        REGD.GRADE, ");
            stb.append("        REGD.HR_CLASS, ");
            stb.append("        CASE WHEN ENTGRD.GRD_DATE IS NOT NULL THEN '1' ELSE '0' END AS KBN_DIV1, ");
            stb.append("        CASE WHEN ENTGRD.GRD_DATE IS NOT NULL THEN ENTGRD.GRD_DATE ELSE ENTGRD.ENT_DATE END AS KBN_DATE1, ");
            stb.append("        CASE WHEN ENTGRD.GRD_DATE IS NOT NULL THEN A003.NAME1 ELSE A002.NAME1 END AS KBN_NAME1, ");
            stb.append("        TRB.TRANSFERCD, ");
            stb.append("        TRSEMES.SEMESTER AS KBN_DATE2_SSEMESTER, ");
            stb.append("        TRSEMEE.SEMESTER AS KBN_DATE2_ESEMESTER, ");
            stb.append("        TRB.TRANSFER_SDATE AS KBN_DATE2, ");
            stb.append("        TRB.TRANSFER_EDATE AS KBN_DATE2E,");
            stb.append("        A004.NAME1 AS KBN_NAME2 ");
            stb.append("FROM    SCHNO_A W6 ");
            stb.append("INNER JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = '" + param._year + "' AND REGD.SEMESTER = W6.SEMESTER AND REGD.SCHREGNO = W6.SCHREGNO ");
            stb.append("INNER JOIN V_SEMESTER_GRADE_MST W2 ON W2.YEAR = '" + param._year + "' AND W2.SEMESTER = REGD.SEMESTER AND W2.GRADE = REGD.GRADE ");
            stb.append("INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
            stb.append("INNER JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR AND REGDH.SEMESTER = REGD.SEMESTER ");
            stb.append("                              AND REGDH.GRADE = REGD.GRADE AND REGDH.HR_CLASS = REGD.HR_CLASS ");
            stb.append("INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGD.YEAR AND GDAT.GRADE = REGD.GRADE ");
            stb.append("LEFT JOIN SCHREG_ENT_GRD_HIST_DAT ENTGRD ON ENTGRD.SCHREGNO = REGD.SCHREGNO AND ENTGRD.SCHOOL_KIND = GDAT.SCHOOL_KIND AND ((ENTGRD.GRD_DIV IN('2','3')) OR (ENTGRD.ENT_DIV IN('4','5'))) ");
            stb.append("LEFT JOIN NAME_MST A003 ON A003.NAMECD1 = 'A003' AND A003.NAMECD2 = ENTGRD.GRD_DIV ");
            stb.append("LEFT JOIN NAME_MST A002 ON A002.NAMECD1 = 'A002' AND A002.NAMECD2 = ENTGRD.ENT_DIV ");
            stb.append("LEFT JOIN TRANSFER_B TRB ON TRB.SCHREGNO = REGD.SCHREGNO ");
            stb.append("LEFT JOIN NAME_MST A004 ON A004.NAMECD1 = 'A004' AND A004.NAMECD2 = TRB.TRANSFERCD ");
            stb.append("LEFT JOIN SEMESTER_MST TRSEMES ON TRSEMES.YEAR = '" + param._year + "' AND TRSEMES.SEMESTER <> '9' AND TRB.TRANSFER_SDATE BETWEEN TRSEMES.SDATE AND TRSEMES.EDATE ");
            stb.append("LEFT JOIN SEMESTER_MST TRSEMEE ON TRSEMEE.YEAR = '" + param._year + "' AND TRSEMEE.SEMESTER <> '9' AND TRB.TRANSFER_EDATE BETWEEN TRSEMEE.SDATE AND TRSEMEE.EDATE ");
            stb.append("ORDER BY REGD.GRADE, REGD.HR_CLASS, REGD.ATTENDNO ");

            return stb.toString();
        }

        private Integer getCredit(final DB2UDB db2, final Vrw32alp svf, final Param param) {

            Integer creditMax = null;
            //生徒数取得
            final String sql = prestateSchnumAndCredits(param);

            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
            	final Map row = (Map) it.next();
                if (KnjDbUtils.getString(row, "CREDITS") != null) {
                    if (null == creditMax || creditMax.intValue() < KnjDbUtils.getInt(row, "CREDITS", new Integer(-1)).intValue()) {
                        creditMax = Integer.valueOf(KnjDbUtils.getString(row, "CREDITS"));
                    }
                }
            }
            return creditMax;
        }

        /**
         *  PrepareStatement作成 該当講座の生徒数・単位の表
         */
        private String prestateSchnumAndCredits(final Param param) {

            final StringBuffer stb = new StringBuffer();

            //対象講座を抽出
            stb.append("WITH CHAIR_A AS(");
            stb.append("   SELECT  CHAIRCD, SCHREGNO ");
            stb.append("   FROM    CHAIR_STD_DAT T1 ");
            stb.append("   WHERE   YEAR = '" + param._year + "' AND ");

                                   //指定講座の生徒を対象とする
            stb.append("           EXISTS(SELECT  SCHREGNO ");
            stb.append("                  FROM    CHAIR_STD_DAT T2 ");
            stb.append("                  WHERE   YEAR = T1.YEAR AND ");
            stb.append("                          t2.semester = t1.semester and ");
            stb.append("                          CHAIRCD = '" + _chaircd + "' AND ");
            stb.append("                          (( APPDATE    BETWEEN '" + param._dateFrom + "' AND '" + param._dateTo + "' )OR ");
            stb.append("                           ( APPENDDATE BETWEEN '" + param._dateFrom + "' AND '" + param._dateTo + "' )OR ");
            stb.append("                           ( '" + param._dateFrom + "' BETWEEN APPDATE AND APPENDDATE )OR ");
            stb.append("                           ( '" + param._dateTo + "' BETWEEN APPDATE AND APPENDDATE )) AND ");
            stb.append("                          T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("                  GROUP BY SCHREGNO) AND ");

                                   //指定講座の科目を対象とする
            stb.append("           EXISTS(SELECT  'X' ");
            stb.append("                  FROM    CHAIR_DAT T2 ");
            stb.append("                  WHERE   T2.YEAR = T1.YEAR AND ");
            stb.append("                          T2.CHAIRCD = T1.CHAIRCD AND ");
            stb.append("                          EXISTS(SELECT  'X' ");
            stb.append("                                 FROM    CHAIR_DAT T3 ");
            stb.append("                                 WHERE   T3.YEAR = T2.YEAR AND ");
            stb.append("                                         t3.semester = t2.semester and ");
            stb.append("                                         T3.CHAIRCD = '" + _chaircd + "' ");
            stb.append("       AND T3.CLASSCD = T2.CLASSCD ");
            stb.append("       AND T3.SCHOOL_KIND = T2.SCHOOL_KIND ");
            stb.append("       AND T3.CURRICULUM_CD = T2.CURRICULUM_CD ");
            stb.append("       AND T3.SUBCLASSCD = T2.SUBCLASSCD )) ");
            stb.append("   GROUP BY CHAIRCD, SCHREGNO) ");

            //メイン表
            stb.append("SELECT  (SELECT COUNT(DISTINCT SCHREGNO) FROM CHAIR_A) AS COUNT, ");
            stb.append("        (SELECT COUNT(DISTINCT CHAIRCD)  FROM CHAIR_A) AS CHAIR_COUNT, ");
            stb.append("        T3.CREDITS, ");
            stb.append("        T1.SCHREGNO, ");
            stb.append("        T1.CHAIRCD ");
            stb.append("FROM CHAIR_A T1 ");
            stb.append("        INNER JOIN SCHREG_REGD_DAT T2 ON T2.YEAR = '" + param._year + "' AND ");
            stb.append("                                         T2.SEMESTER = '" + param._semester + "' AND ");
            stb.append("                                         T1.SCHREGNO = T2.SCHREGNO ");
            stb.append("        LEFT JOIN V_CREDIT_MST T3 ON T3.YEAR = '" + param._year + "' AND ");
            stb.append("                                   T3.GRADE = T2.GRADE AND ");
            stb.append("                                   T3.COURSECD = T2.COURSECD AND ");
            stb.append("                                   T3.MAJORCD = T2.MAJORCD AND ");
            stb.append("                                   T3.COURSECODE = T2.COURSECODE AND ");
            stb.append("                                   T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD = '" + param._subclassCd + "' ");
            return stb.toString();
        }
        
        private void setAttendance(final DB2UDB db2, final Param param) {

            final Set gradeSet = new HashSet();
            final Set hrClassSet = new HashSet();
            for (final Iterator stit = _studentList.iterator(); stit.hasNext();) {
                final Student student = (Student) stit.next();
                if (null == student) {
                    continue;
                }
                if (null != student._grade) {
                    gradeSet.add(student._grade);
                }
                if (null != student._hrClass) {
                    hrClassSet.add(student._hrClass);
                }
            }
            log.info(" grade = " + gradeSet + ", hrClass = " + hrClassSet);
            
            for (final Iterator it = param._testitemMap.values().iterator(); it.hasNext();) {

            	final TestItem testItem = (TestItem) it.next();
            	if (null == testItem._dateRange) {
            		continue;
            	}
            	if (null == testItem._dateRange._sdate || null == testItem._dateRange._edate) {
            		log.warn(" null date " + testItem._dateRange._key + ", sdate = " + testItem._dateRange._sdate + ", edate = " + testItem._dateRange._edate);
            		continue;
            	}
                if (Param.toCal(param._dateFrom).after(Param.toCal(testItem._dateRange._edate))) {
                    continue;   //印刷終了日が学期開始日の前なら以降の学期は出力しない！
                }
                final String edate;      //学期終了日
                if (Param.toCal(param._dateTo).before(Param.toCal(testItem._dateRange._edate))) {         //印刷終了日が学期終了日の前なら集計期間は印刷終了日までとする！
                    edate = param._dateTo;
                } else {
                    edate = testItem._dateRange._edate;
                }
                final DateRange range = new DateRange(testItem._dateRange._key, testItem._testitemname, testItem._dateRange._sdate, edate);

                loadAttendance(db2, param, gradeSet, hrClassSet, testItem._semester._key, range);
            }
        }

		private void loadAttendance(final DB2UDB db2, final Param param, final Set gradeSet, final Set hrClassSet, final String semester, final DateRange range) {
			param._attendParamMap.put("subclasscd", param._subclassCd);
			param._attendParamMap.put("sSemester", semester);
			if (gradeSet.size() == 1) {
			    param._attendParamMap.put("grade", gradeSet.iterator().next());
			} else {
			    param._attendParamMap.remove("grade");
			}
			if (hrClassSet.size() == 1) {
			    param._attendParamMap.put("hrClass", hrClassSet.iterator().next());
			} else {
			    param._attendParamMap.remove("hrClass");
			}
			boolean exeStudent = false;
			if (gradeSet.size() > 1 || hrClassSet.size() > 1) {
				exeStudent = true;
			    param._attendParamMap.put("schregno", "?");
			} else {
			    param._attendParamMap.remove("schregno");
			}
			final String sql = AttendAccumulate.getAttendSubclassSql(param._year,
					semester,
			        range._sdate,
			        range._edate,
			        param._attendParamMap);

			if (param._isOutputDebug) {
			    for (final Iterator pit = param._attendParamMap.entrySet().iterator(); pit.hasNext();) {
			        final Map.Entry e = (Map.Entry) pit.next();
			        log.info(" attend param " + e.getKey() + " = " + e.getValue());
			    }
			}
			if (param._isOutputDebugAll) {
			    log.info(" range = " + range + " calc " + range._key + " sql = " + sql);
			}

			if (exeStudent) {
				PreparedStatement ps = null;
				try {
			    	ps = db2.prepareStatement(sql);  //出欠集計
			        for (final Iterator stit = _studentList.iterator(); stit.hasNext();) {
			            final Student student = (Student) stit.next();
			            if (null == student) {
			                continue;
			            }

			            for (final Iterator dit = KnjDbUtils.query(db2, ps, new Object[] { student._schregno }).iterator(); dit.hasNext();) {
			            	final Map row = (Map) dit.next();
			            	
			                if (KnjDbUtils.getString(row, "SEMESTER").equals(String.valueOf(SEMES_ALL))) {
			                	setStudentAttendance(row, param, range);
			                }
			            }
			        }
				} catch (Exception e) {
			        DbUtils.closeQuietly(ps);
				}
			} else {

			    for (final Iterator dit = KnjDbUtils.query(db2, sql).iterator(); dit.hasNext();) {
			    	final Map row = (Map) dit.next();
			        setStudentAttendance(row, param, range);
			    }
			}
		}

        private void setStudentAttendance(final Map row, final Param param, final DateRange range) {
            final Student student = (Student) getStudent(KnjDbUtils.getString(row, "SCHREGNO"));
            if (null == student) {
                return;
            }

            final int sick1 = Integer.parseInt(KnjDbUtils.getString(row, "SICK1"));
            final int suspend = Integer.parseInt(KnjDbUtils.getString(row, "SUSPEND"));
//            final double late = param.isPrintRawLateEarly() ? Double.parseDouble(KnjDbUtils.getString(row, "LATE")) : Double.parseDouble(KnjDbUtils.getString(row, "LATE2"));
//            final double early = param.isPrintRawLateEarly() ? Double.parseDouble(KnjDbUtils.getString(row, "EARLY")) : Double.parseDouble(KnjDbUtils.getString(row, "EARLY2"));
            final String sick2Str = KnjDbUtils.getString(row, "SICK2");
            final int jisu = Integer.parseInt(KnjDbUtils.getString(row, "MLESSON"));
            final int offdays = Integer.parseInt(KnjDbUtils.getString(row, "OFFDAYS"));
            final Attendance ate = new Attendance();
            ate._kesseki = sick1;
            ate._suspend = suspend;
//            ate._srcLate = late;
//            ate._srcEarly = early;
            ate._srcKekka = Double.parseDouble(sick2Str);    //欠課
            ate._lesson = jisu;
            ate._offdays = offdays;
            student._attendance.put(range._key, ate);
        }

		private Student getStudent(final String schregno) {
			for (final Iterator it = _studentList.iterator(); it.hasNext();) {
				final Student student = (Student) it.next();
				if (null != schregno && schregno.equals(student._schregno)) {
					return student;
				}
			}
			return null;
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

    private static class Semester extends DateRange {
        final String _semestername;
        final List _testItemList = new ArrayList();
        public Semester(final String semester, final String semestername, final String sdate, final String edate) {
            super(semester, semestername, sdate, edate);
            _semestername = semestername;
        }
        public String toString() {
        	return "Semester(" + _key + ", " + _sdate + ", " + _edate + ")";
        }
    }
    
    private static class TestItem {
        public String _testcd;
        public Semester _semester = new Semester(null, null, null, null);
        public String _testitemname;
        public String _sidouinputinf;
        public String _sidouinput;
        public String _scoreDivName;
        public String _semesterDetail;
        public DateRange _dateRange;
        public boolean _printScore;
        public String semester() {
            return _testcd.substring(0, 1);
        }
        public String scorediv() {
            return _testcd.substring(_testcd.length() - 2);
        }
        public String toString() {
            return "TestItem(" + _testcd + ":" + _testitemname + ", sidouInput=" + _sidouinput + ", sidouInputInf=" + _sidouinputinf + ")";
        }
    }

    private static class Param {

        final String _year;
        final String _semester;
        final String _classCd;
        final String _subclassCd;
        final String _nendo;
        final String _dateFromTo;
        final Map _testitemMap;

        /** 印刷範囲開始 */
        final String _dateFrom;
        /** 印刷範囲終了 */
        final String _dateTo;

        final String[] _category_selected;
        String _ctrlDateString;

        /** 適用開始日付 */
        final String _ctrldate;

        /** 遅刻・早退は欠課換算前の値を表示するか */
        final String _chikokuHyoujiFlg;

        /** テスト項目マスタテーブル */
        final String _useTestCountflg;

        final List _chairList = new ArrayList();

        final KNJSchoolMst _knjSchoolMst;

        final int _semesdiv;
        final int _absentConv;
        final int _absentConvLate;
        final Map _attendParamMap;
        final List _semesterList;

        final boolean _isOutputDebugAll;
        final boolean _isOutputDebug;

        final SimpleDateFormat sdf2 = new SimpleDateFormat("MM/dd");
        final SimpleDateFormat sdf3 = new SimpleDateFormat("yy/MM/dd");

        public Param(final HttpServletRequest request, final DB2UDB db2) {
            _year = request.getParameter("YEAR");            //年度
            _semester = request.getParameter("SEMESTER");        //学期
            _category_selected = request.getParameterValues("category_selected");
            _classCd = request.getParameter("CLASSCD");         //教科コード
            _subclassCd = request.getParameter("SUBCLASSCD");      //科目コード
            _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_year)) + "年度";
            //  日付型を変換
            _dateFrom = KNJ_EditDate.H_Format_Haifun(request.getParameter("SDATE"));    //印刷範囲開始
            _dateTo = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE"));   //印刷範囲終了
            _dateFromTo = KNJ_EditDate.h_format_JP(db2, _dateFrom) + " " + FROM_TO_MARK + " " + KNJ_EditDate.h_format_JP(db2, _dateTo);
            _ctrldate = request.getParameter("CTRL_DATE");
            _chikokuHyoujiFlg = request.getParameter("chikokuHyoujiFlg"); // 遅刻・早退は欠課換算前の値を表示するか
            _useTestCountflg = request.getParameter("useTestCountflg"); // テスト項目マスタテーブル

            KNJSchoolMst knjSchoolMst = null;
            try {
            	final Map paramMap = new HashMap();
            	paramMap.put("SCHOOL_KIND", StringUtils.split(_subclassCd, "-")[1]);
            	
                knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (Exception e) {
                log.warn("学校マスタ取得でエラー", e);
            }
            _knjSchoolMst = knjSchoolMst;
            _absentConv = NumberUtils.isDigits(_knjSchoolMst._absentCov) ? Integer.parseInt(_knjSchoolMst._absentCov) : 0;
            _absentConvLate = NumberUtils.isDigits(_knjSchoolMst._absentCovLate) ? Integer.parseInt(_knjSchoolMst._absentCovLate) : 0;
            _semesdiv = null != _knjSchoolMst && NumberUtils.isDigits(_knjSchoolMst._semesterDiv) ? Integer.parseInt(_knjSchoolMst._semesterDiv) : -1;

            //  作成日(現在処理日)
            _ctrlDateString = KNJ_EditDate.h_format_JP(db2, KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT CTRL_DATE FROM CONTROL_MST WHERE CTRL_NO = '01' ")));

            for (int i = 0; i < _category_selected.length; i++) {
            	final Chair chair = new Chair(_category_selected[i]);
            	_chairList.add(chair);
            	
                //  講座名、科目名
            	chair.setChairInfo(db2, this);

                //  担任名
                chair.setChairStaff(db2, this);

            }
            
            _attendParamMap = new HashMap();
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("DB2UDB", db2);

            _semesterList = getSemesterList(db2);
            _testitemMap = getTestItemMap(db2);

            final String[] outputDebug = StringUtils.split(getDbPrginfoProperties(db2, "outputDebug"));
            _isOutputDebugAll = ArrayUtils.contains(outputDebug, "all");
            _isOutputDebug = _isOutputDebugAll || ArrayUtils.contains(outputDebug, "1");
        }

        private static Calendar toCal(final String date) {
            final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            final Calendar cal = Calendar.getInstance();
            try {
                cal.setTime(sdf.parse(date));
            } catch (Exception ex) {
                log.error("toCalendar error!", ex);
            }
            return cal;
        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJC153A' AND NAME = '" + propName + "' "));
        }

//        /**
//         * 遅刻・早退は欠課換算前の値を表示するか
//         * @return 遅刻・早退は欠課換算前の値を表示するならtrue、そうでなければfalse
//         */
//        private boolean isPrintRawLateEarly() {
//            return "1".equals(_chikokuHyoujiFlg);
//        }

        /**
         * 出欠は整数で表示するか
         * @return 出欠は整数で表示するならtrue、そうでなければfalse
         */
        private boolean isAttendPrintInt() {
            return _absentConv != KANSAN3_SYOSU_GAKKI && _absentConv != KANSAN4_SYOSU_NENKAN;
        }

//        /**
//         * 出欠換算は学期ごとか
//         * @return 出欠換算は学期ごとならtrue、そうでなければfalse
//         */
//        private boolean isConvOnSemester() {
//            return _absentConv != KANSAN2_SEISU_NENKAN && _absentConv != KANSAN4_SYOSU_NENKAN && _absentConv != KANSAN5_SEISU_NENKAN_AMARIKURIAGE;
//        }

//        /**
//         * 欠課を表示するか
//         * @param semester 学期
//         * @return 欠課を表示するか
//         */
//        private boolean isPrintSemesKekka(int semester) {
//            final boolean isPrintKekka;
//            if (SEMES_ALL == semester) {
//                isPrintKekka = _absentConv != KANSAN0_NASI; // 総欠課数は換算無し以外で表示する
//            } else {
//                isPrintKekka = !(_absentConv == KANSAN0_NASI || _absentConv == KANSAN2_SEISU_NENKAN || _absentConv == KANSAN4_SYOSU_NENKAN || _absentConv == KANSAN5_SEISU_NENKAN_AMARIKURIAGE);
//            }
//            return isPrintKekka;
//        }

        private Semester getSemester(final String semester) {
            for (final Iterator it = _semesterList.iterator(); it.hasNext();) {
                final Semester seme = (Semester) it.next();
                if (semester.equals(seme._key)) {
                    return seme;
                }
            }
            return null;
        }
        
        private String getFooterText() {
        	final StringBuffer text = new StringBuffer("※欠課時数に");
            String comma = "";
            if ("1".equals(_knjSchoolMst._subOffDays)) {
                text.append(comma + "休学");
                comma = "、";
            }
            if ("1".equals(_knjSchoolMst._subAbsent)) {
                text.append(comma + "公欠");
                comma = "、";
            }
            if ("1".equals(_knjSchoolMst._subSuspend)) {
                text.append(comma + "出停");
                comma = "、";
            }
            if ("1".equals(_knjSchoolMst._subMourning)) {
                text.append(comma + "忌引");
                comma = "、";
            }
            if ("1".equals(_knjSchoolMst._subVirus)) {
                text.append(comma + "出停（伝染病）");
                comma = "、";
            }
            text.append("を含む。");

            if ("".equals(comma)) {
                return null;
            }
            return text.toString();
        }

        /** ＤＢより該当年度全学期情報を取得するメソッド **/
        private List getSemesterList(final DB2UDB db2) {

            final List semesterList = new ArrayList();
            final String sql = "SELECT * FROM SEMESTER_MST WHERE YEAR = '" + _year + "' ORDER BY SEMESTER";
            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
            	final Map row = (Map) it.next();
                final Semester semester = new Semester(KnjDbUtils.getString(row, "SEMESTER"), KnjDbUtils.getString(row, "SEMESTERNAME"), KnjDbUtils.getString(row, "SDATE"), KnjDbUtils.getString(row, "EDATE"));
                semesterList.add(semester);
            }
            return semesterList;
        }

        protected Map getTestItemMap(
                final DB2UDB db2
        ) {
            final Map testitemMap = new HashMap();
            final String sql = "SELECT T1.SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV AS TESTCD "
                    +  " ,T1.SEMESTER "
                    +  " ,TESTITEMNAME "
                    + "  ,SIDOU_INPUT "
                    + "  ,SIDOU_INPUT_INF "
                    + "  ,T1.SEMESTER "
                    + "  ,T1.SEMESTER_DETAIL "
                    + "  ,T2.SDATE "
                    + "  ,T2.EDATE "
                    +  " ,CASE WHEN T1.SEMESTER <= '" + _semester + "' THEN 1 ELSE 0 END AS PRINT "
                    +  " ,NMD053.NAME1 AS SCORE_DIV_NAME "
                    +  "FROM TESTITEM_MST_COUNTFLG_NEW_SDIV T1 "
                    +  "LEFT JOIN SEMESTER_DETAIL_MST T2 ON T2.YEAR = T1.YEAR "
                    +  " AND T2.SEMESTER = T1.SEMESTER "
                    +  " AND T2.SEMESTER_DETAIL = T1.SEMESTER_DETAIL "
                    +  "LEFT JOIN NAME_MST NMD053 ON NMD053.NAMECD1 = 'D053' AND NMD053.NAMECD2 = T1.SCORE_DIV AND T1.SEMESTER <> '9' AND T1.TESTKINDCD <> '99' "
                    +  "WHERE T1.YEAR = '" + _year + "' "
                    +  "  AND T1.SEMESTER <> '" + SEMES_ALL + "' "
                    +  "  AND T1.SCORE_DIV = '01' "
                    +  " ORDER BY T1.SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV ";
            log.debug(" sql = " + sql);
            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
            	final Map row = (Map) it.next();
            	final String semester = KnjDbUtils.getString(row, "SEMESTER"); 
                final String testcd = KnjDbUtils.getString(row, "TESTCD");
                final TestItem testitem = new TestItem();
                testitem._testcd = testcd;
                testitem._testitemname = KnjDbUtils.getString(row, "TESTITEMNAME");
                testitem._sidouinput = KnjDbUtils.getString(row, "SIDOU_INPUT");
                testitem._sidouinputinf = KnjDbUtils.getString(row, "SIDOU_INPUT_INF");
                for (final Iterator sit = _semesterList.iterator(); sit.hasNext();) {
                	final Semester seme = (Semester) sit.next();
                	if (null != seme._key && seme._key.equals(semester)) {
                		seme._testItemList.add(testitem);
                		testitem._semester = seme;
                	}
                }
                if (null == testitem._semester) {
                	log.info(" null semester (semester = " + semester + ")");
                }
                testitem._semesterDetail = KnjDbUtils.getString(row, "SEMESTER_DETAIL");
                testitem._dateRange = new DateRange(testitem._testcd, testitem._testitemname, KnjDbUtils.getString(row, "SDATE"), KnjDbUtils.getString(row, "EDATE"));
                testitem._printScore = "1".equals(KnjDbUtils.getString(row, "PRINT"));
                testitem._scoreDivName = KnjDbUtils.getString(row, "SCORE_DIV_NAME");
                testitemMap.put(testcd, testitem);
            }
            return testitemMap;
        }
    }
}
