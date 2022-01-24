// kanji=漢字
/*
 * $Id: 900968cef47ef2ad01220e6d767adc5a6968b76f $
 *
 * 作成日: 2003/11/11
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJC;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KnjDbUtils;

/*
 *
 *  学校教育システム 賢者 [出欠管理]
 *
 *                  ＜ＫＮＪＣ１２０＞  個人出欠状況（月別）
 *
 *  2003/11/11・KNJC070の廃止に伴いKNJC120を新規作成
 *  2004/03/29 yamashiro・教科コード仕様の変更に伴う修正
 *  2004/07/16 yamashiro・月別集計を月に２つの学期に対応->２学期制の場合
 *                      ・ATTEND_SEMES_DAT,ATTEND_SUBCLASS_DATの'A_'を変更
 *  2004/08/24 yamashiro・組名称に"組"がない場合に対処-->組名称と出席番号の間に"-"を挿入
 *  2004/11/01 yamashiro・出欠集計テーブルATTEND_SEMES_DAT,_DATの変更に伴う修正
 *  2005/02/17 yamashiro・出力されない月がある不具合を修正
 *  2006/01/24 yamashiro・月別集計において教科遅刻が出力されない不具合を修正
 */

public class KNJC120 {

    private static final Log log = LogFactory.getLog(KNJC120.class);
    
    private boolean _hasData;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out (
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {
        final KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();  //帳票におけるＳＶＦおよびＤＢ２の設定
        Vrw32alp svf = new Vrw32alp();  //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;              //Databaseクラスを継承したクラス
        // print svf設定
        sd.setSvfInit(request, response, svf);
        // ＤＢ接続
        db2 = sd.setDb(request);
        if (sd.openDb(db2)) {
            log.error("db open error");
            return;
        }
        // 印刷処理
        final Param param = getParam(db2, request);
        _hasData = false;
        printMain(db2, svf, param);
        // 終了処理
        sd.closeSvf(svf, _hasData);
        sd.closeDb(db2);
    }

    //  ＳＶＦ作成処理
    private void printMain(final DB2UDB db2, final Vrw32alp svf, final Param param) {
        //SVF出力
        final String form = param._isRisshisha ? "KNJC120_2.frm" : "KNJC120.frm";

        for (int i = 0; i < param._category_name.length; i++) {
        	final String schregno = param._category_name[i];
            svf.VrSetForm(form, 4);
            final Student student = new Student(schregno);
			printHeader(db2, svf, student, param); //生徒名等出力
            printMonth(db2, svf, student, param); //月別出欠表出力
            printYear(db2, svf, student, param); //全体出欠表出力
            if (param._isRisshisha) {
            	if ("1024".equals(student._coursecode)) { // 特進コース
            		printHokou(db2, svf, student, param); //全体出欠表出力
            	}
            }
        }
 
        DbUtils.closeQuietly(param.ps1);
        DbUtils.closeQuietly(param.ps2);
        DbUtils.closeQuietly(param.ps3);
        db2.commit();
    }

    private Param getParam(final DB2UDB db2, final HttpServletRequest request) {
        log.fatal("$Revision: 75265 $");
    	KNJServletUtils.debugParam(request, log);
        final Param paramap = new Param(db2, request);
        return paramap;
    }

    /*----------------*
     * 見出し等の出力
     *----------------*/
    private void printHeader(final DB2UDB db2, final Vrw32alp svf, final Student student, final Param param) {
        try{
        	if (null == param.ps1) {
        		final String sql = sqlStudentInfo(param);
				param.ps1 = db2.prepareStatement(sql);
        	}
        } catch (Exception ex) {
            log.error("[KNJC120]Set_Head read error!", ex);
        }
        
        //  個人情報の出力
        for (final Map<String, String> row : KnjDbUtils.query(db2, param.ps1, new Object[] {java.sql.Date.valueOf(param._date), student._schregno})) {
            svf.VrsOut("TODAY"        , param._ctrlDateString);                        //処理日
            svf.VrsOut("DATE"         , param._dateString);                        //処理日
            svf.VrsOut("SCHREGNO"     , KnjDbUtils.getString(row, "SCHREGNO"));        //学籍番号
            svf.VrsOut("NAME"         , KnjDbUtils.getString(row, "NAME"));            //氏名
            student._coursecode = KnjDbUtils.getString(row, "COURSECODE");
        	final String grdDate = KnjDbUtils.getString(row, "GRD_DATE");
			if (grdDate == null) {
                //組名称の編集
                String strx = "";
                final String attendno = NumberUtils.isDigits(KnjDbUtils.getString(row, "ATTENDNO")) ? String.valueOf(Integer.parseInt(KnjDbUtils.getString(row, "ATTENDNO"))) : StringUtils.defaultString(KnjDbUtils.getString(row, "ATTENDNO"));
                final String hrname = KnjDbUtils.getString(row, "HR_NAME");
                if (null != hrname) {
                	if (hrname.lastIndexOf("組") > -1) {
                		strx = hrname + attendno + "番";
                	} else {
                		strx = hrname + "-" + attendno + "番";
                	}
                }
                svf.VrsOut("HR_NAME"  , strx);                        //組名称&出席番号
                svf.VrsOut("zaiseki2" , "在学");
            } else{
                svf.VrsOut("GRAD_YMD" , KNJ_EditDate.h_format_JP(db2, grdDate));  //卒業日
                svf.VrsOut("zaiseki2" , KnjDbUtils.getString(row, "IN_OUT"));      //卒業
            }
        }
    }

    /*------------------*
     * 月別集計表の出力
     *------------------*/
    private void printMonth(final DB2UDB db2, final Vrw32alp svf, final Student student, final Param param) {
        try {
        	if (null == param.ps2) {
        		final String monthSql = getMonthSql(param);
        		param.ps2 = db2.prepareStatement(monthSql);
        		log.debug(" month sql = " + monthSql);
        	}
        } catch (Exception ex) {
            log.error("[KNJC120]Set_Detail_1 read error!", ex);
        }
        
        int h_month = 0;    //出欠データより取得した月の保管用
        int c_month = 1;    //出力行の月
        int i = 0;         //出力の繰返し数
        for (final Map<String, String> row : KnjDbUtils.query(db2, param.ps2, new Object[] {student._schregno, student._schregno})) {
            final int month = Integer.parseInt(KnjDbUtils.getString(row, "MONTH"));
            final int r_month = (month > 3) ? month - 3 : month + 9;       //出欠データより取得した月 ４月を１行目に換算する
            if (r_month == h_month) {
            	c_month--;                //月が重複する場合の処理
            }
            for (; c_month <= r_month; c_month++) {          //月項目名を出力
                svf.VrsOutn("MONTH", ++i, param._zenkakuMap.get(new Integer((c_month < 10) ? c_month + 3 : c_month - 9)) + "月");

            }
            svfVrsOutnNotNull(svf, "MONTHLY_LESSON"          ,i ,KnjDbUtils.getString(row, "J_NISU")       );            //授業日数
			svfVrsOutnNotNull(svf, "MONTHLY_SUSPEND"         ,i ,KnjDbUtils.getString(row, "SUSPEND")      );            //出停
			svfVrsOutnNotNull(svf, "MONTHLY_KIBIKI"          ,i ,KnjDbUtils.getString(row, "MOURNING")     );            //忌引
			svfVrsOutnNotNull(svf, "MONTHLY_LEAVE"           ,i ,KnjDbUtils.getString(row, "EARLY")        );            //早退
			svfVrsOutnNotNull(svf, "MONTHLY_LATE"            ,i ,KnjDbUtils.getString(row, "LATE")         );                //遅刻
			svfVrsOutnNotNull(svf, "MONTHLY_ABSENCE"         ,i ,KnjDbUtils.getString(row, "ABSENCE")      );            //病欠
            if (param._isRisshisha) {
                final String classMlessonRisshisha = KnjDbUtils.getString(row, "CLASS_MLESSON_RISSI");
                final String classAttendRisshisha = KnjDbUtils.getString(row, "CLASS_ATTEND_RISSI");
            	svfVrsOutnNotNull(svf, "MONTHLY_SUBCLASS_ATTEND"   ,i ,classAttendRisshisha);    //総授業出席数
				svfVrsOutnNotNull(svf, "MONTHLY_SUBCLASS_LESSON"   ,i ,classMlessonRisshisha);    //総授業時間数
				svfVrsOutnNotNull(svf, "MONTHLY_SUBCLASS_PERCENTAGE"   ,i , percentage(classAttendRisshisha, classMlessonRisshisha));    //出席率
            } else {
            	svfVrsOutnNotNull(svf, "MONTHLY_LATE_SHR"        ,i ,KnjDbUtils.getString(row, "SHR_LATE")     );            //遅刻 ＳＨＲ
				svfVrsOutnNotNull(svf, "MONTHLY_LATE_SUBJECT"    ,i ,KnjDbUtils.getString(row, "CLASS_LATE")   );        //遅刻 教科
				svfVrsOutnNotNull(svf, "MONTHLY_LATE_LHR"        ,i ,KnjDbUtils.getString(row, "LHR_LATE")     );            //遅刻 ＬＨＲ
				svfVrsOutnNotNull(svf, "MONTHLY_KEKKA_SUBJECT"   ,i ,KnjDbUtils.getString(row, "CLASS_ABSENCE"));    //欠課 教科
            }
            h_month = r_month;
        }
    }
    
    private void svfVrsOutnNotNull(final Vrw32alp svf, final String field, final int gyo, final String data) {
    	if (null == data) {
    		return;
    	}
    	svf.VrsOutn(field, gyo, data);
    }

    private void svfVrsOutNotNull(final Vrw32alp svf, final String field, final String data) {
    	if (null == data) {
    		return;
    	}
    	svf.VrsOut(field, data);
    }

    private String percentage(final String bunshi, final String bunbo) {
    	if (!NumberUtils.isNumber(bunshi) || !NumberUtils.isNumber(bunbo)) {
    		return null;
    	}
    	if (new BigDecimal(bunbo).doubleValue() == 0.0) {
    		return "0.0%";
    	}
		return new BigDecimal(bunshi).multiply(new BigDecimal(100)).divide(new BigDecimal(bunbo), 1, BigDecimal.ROUND_HALF_UP).toString() + "%";
	}

	/*----------------*
     * 全体表の出力
     *----------------*/
    private void printYear(final DB2UDB db2, final Vrw32alp svf, final Student student, final Param param) {
        try {
        	if (null == param.ps3) {
        		final String yearSql = getYearSql(param);
        		log.debug(" year sql = " + yearSql);
        		param.ps3 = db2.prepareStatement(yearSql);
        	}
        } catch (Exception ex) {
            log.error("[KNJC120]printYear error!", ex);
        }
        
        int annual = 0;
        for (final Map<String, String> row : KnjDbUtils.query(db2, param.ps3, new Object[] {student._schregno, student._schregno, student._schregno, student._schregno})) {
        	//  学年別出力
    		if (annual != Integer.parseInt(KnjDbUtils.getString(row, "ANNUAL"))) {
    			if (annual > 0) {
    				svf.VrEndRecord();
    			}
    			annual = Integer.parseInt(KnjDbUtils.getString(row, "ANNUAL"));
    			if (annual < 99) {
    				final String grade;
    				if ("1".equals(param._knjc120PrintGradeCd)) {
    					grade = String.valueOf(Integer.parseInt(KnjDbUtils.getString(row, "GRADE_CD"))) + "年";
    				} else {
    					grade = String.valueOf(annual) + "年";
    				}
    				svf.VrsOut("GRADE", grade);  //学年
    			}
    		}
        	if (param._year.equals(KnjDbUtils.getString(row, "YEAR"))) {
        		student._classAttendRisshisha96 = KnjDbUtils.getString(row, "CLASS_ATTEND_RISSI96");
        		student._classMlessonRisshisha96 = KnjDbUtils.getString(row, "CLASS_MLESSON_RISSI96");
        	}
        	
        	//  明細出力
        	final String jNisu = KnjDbUtils.getString(row, "J_NISU");
        	final String suspend = KnjDbUtils.getString(row, "SUSPEND");
        	final String mourning = KnjDbUtils.getString(row, "MOURNING");
        	final String early = KnjDbUtils.getString(row, "EARLY");
        	final String late = KnjDbUtils.getString(row, "LATE");
        	final String absence = KnjDbUtils.getString(row, "ABSENCE");
        	final String shrLate = KnjDbUtils.getString(row, "SHR_LATE");
        	final String classLate = KnjDbUtils.getString(row, "CLASS_LATE");
        	final String lhrLate = KnjDbUtils.getString(row, "LHR_LATE");
        	final String classAbsence = KnjDbUtils.getString(row, "CLASS_ABSENCE");
        	final String classmLessonRisshisha = KnjDbUtils.getString(row, "CLASS_MLESSON_RISSI");
        	final String classAttendRisshisha = KnjDbUtils.getString(row, "CLASS_ATTEND_RISSI");
        	if (annual == 99) {       //----->全体
        		svfVrsOutNotNull(svf, "TOTAL_LESSON"          ,jNisu      ) ; //授業日数
        		svfVrsOutNotNull(svf, "TOTAL_SUSPEND"         ,suspend     ) ; //出停
        		svfVrsOutNotNull(svf, "TOTAL_KIBIKI"          ,mourning    ) ; //忌引
        		svfVrsOutNotNull(svf, "TOTAL_LEAVE"           ,early       ) ; //早退
        		svfVrsOutNotNull(svf, "TOTAL_LATE"            ,late        ) ; //遅刻
        		svfVrsOutNotNull(svf, "TOTAL_ABSENCE"         ,absence     ) ; //病欠
        		if (param._isRisshisha) {
        			svfVrsOutNotNull(svf, "TOTAL_SUBCLASS_ATTEND"   , classAttendRisshisha);    //欠課 教科
        			svfVrsOutNotNull(svf, "TOTAL_SUBCLASS_LESSON"   , classmLessonRisshisha);    //欠課 教科
        			svfVrsOutNotNull(svf, "TOTAL_SUBCLASS_PERCENTAGE"   , percentage(classAttendRisshisha, classmLessonRisshisha));    //出席率
        		} else {
        			svfVrsOutNotNull(svf, "TOTAL_LATE_SHR"        ,shrLate    ) ; //遅刻 ＳＨＲ
        			svfVrsOutNotNull(svf, "TOTAL_LATE_SUBJECT"    ,classLate  ) ; //遅刻 教科
        			svfVrsOutNotNull(svf, "TOTAL_LATE_LHR"        ,lhrLate    ) ; //遅刻 ＬＨＲ
        			svfVrsOutNotNull(svf, "TOTAL_KEKKA_SUBJECT"   ,classAbsence); //欠課 教科
        		}
        	} else {                       //----->学年別学期別
        		int i = Integer.parseInt(KnjDbUtils.getString(row, "SEMESTER"));
        		if (i == 9) {
        			i = 4;                                                           //学年末
        		}
        		String semestername = KnjDbUtils.getString(row, "SEMESTERNAME");
        		svfVrsOutnNotNull(svf, "term1"            ,i ,semestername) ; //学期名称
        		svfVrsOutnNotNull(svf, "LESSON"           ,i ,jNisu      ) ; //授業日数
        		svfVrsOutnNotNull(svf, "SUSPEND"          ,i ,suspend     ) ; //出停
        		svfVrsOutnNotNull(svf, "KIBIKI"           ,i ,mourning    ) ; //忌引
        		svfVrsOutnNotNull(svf, "LEAVE"            ,i ,early       ) ; //早退
        		svfVrsOutnNotNull(svf, "LATE"             ,i ,late        ) ; //遅刻
        		svfVrsOutnNotNull(svf, "ABSENCE"          ,i ,absence     ) ; //病欠
        		if (param._isRisshisha) {
        			svfVrsOutnNotNull(svf, "SUBCLASS_ATTEND"   ,i ,classAttendRisshisha);    //総授業出席数
        			svfVrsOutnNotNull(svf, "SUBCLASS_LESSON"   ,i ,classmLessonRisshisha);    //総授業時間数
        			svfVrsOutnNotNull(svf, "SUBCLASS_PERCENTAGE"   ,i , percentage(classAttendRisshisha, classmLessonRisshisha));    //出席率
        		} else {
        			svfVrsOutnNotNull(svf, "LATE_SHR"         ,i ,shrLate    ) ; //遅刻 ＳＨＲ
        			svfVrsOutnNotNull(svf, "LATE_SUBJECT"     ,i ,classLate  ) ; //遅刻 教科
        			svfVrsOutnNotNull(svf, "LATE_LHR"         ,i ,lhrLate    ) ; //遅刻 ＬＨＲ
        			svfVrsOutnNotNull(svf, "KEKKA_SUBJECT"    ,i ,classAbsence); //欠課 教科
        		}
        	}
        }
        if (annual > 0) {
        	svf.VrEndRecord();
        	_hasData = true;
        	//Svf_Int(svf);
        }
    }
    
    /**
     * (立志舎のみ)
     * 特別補講の時数と出席率を印字する
     */
    private void printHokou(final DB2UDB db2, final Vrw32alp svf, final Student student, final Param param) {
    	svf.VrsOut("BLANK", "1") ; // 空行
        svf.VrEndRecord();

        svf.VrsOut("SPHOKO_LESSON", student._classMlessonRisshisha96); // 総授業時間数
    	svf.VrsOut("SPHOKO_ATTEND", student._classAttendRisshisha96); // 総出席時間数
    	svf.VrsOut("SPHOKO_PER", percentage(student._classAttendRisshisha96, student._classMlessonRisshisha96)); // 出席率
        svf.VrEndRecord();
    }

    /*
     * PrepareStatement SQL
     */
    private String sqlStudentInfo(final Param param) {

        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ST1.SCHREGNO, ");
        stb.append("        ST1.GRADE, ");
        stb.append("        ST1.HR_CLASS, ");
        stb.append("        ST1.ATTENDNO, ");
        stb.append("        ST1.COURSECODE, ");
        stb.append("        ST2.NAME, ");
        stb.append("        ST3.HR_NAME,");
        stb.append("        CASE WHEN EGHIST.GRD_DATE <= ? THEN EGHIST.GRD_DATE ELSE NULL END AS GRD_DATE,");
        stb.append("        Meisyou_Get(EGHIST.GRD_DIV,'A003',1) AS IN_OUT");
        stb.append(" FROM SCHREG_REGD_DAT ST1");
        stb.append(" INNER JOIN SCHREG_BASE_MST ST2 ON ST1.SCHREGNO = ST2.SCHREGNO");
        stb.append(" INNER JOIN SCHREG_REGD_HDAT ST3 ON ST1.YEAR = ST3.YEAR AND ST1.SEMESTER = ST3.SEMESTER");
        stb.append("                                AND ST1.GRADE = ST3.GRADE AND ST1.HR_CLASS = ST3.HR_CLASS");
        stb.append(" LEFT JOIN SCHREG_REGD_GDAT GDAT ON ST1.YEAR = GDAT.YEAR AND ST1.GRADE = GDAT.GRADE ");
        stb.append(" LEFT JOIN SCHREG_ENT_GRD_HIST_DAT EGHIST ON ST1.SCHREGNO = EGHIST.SCHREGNO AND GDAT.SCHOOL_KIND = EGHIST.SCHOOL_KIND ");
        stb.append(" WHERE ST1.YEAR = '" + param._year + "'");
        stb.append("   AND ST1.SEMESTER = '" + param._semester + "'");
        stb.append("   AND ST1.SCHREGNO = ?");
        return stb.toString();
    }

    /*------------------------*
     * PrepareStatement SQL
     *------------------------*/
    private String getMonthSql(final Param param) {

        //  月別集計
        final StringBuffer stb = new StringBuffer();
        stb.append(    "SELECT  ");
        stb.append("        VALUE(T1.SEMESTER, T2.SEMESTER) AS SEMESTER, ");
        stb.append("        VALUE(T1.MONTH,T2.MONTH) AS MONTH,");
        stb.append(        "VALUE(T1.J_NISU,0) AS J_NISU, VALUE(T1.SUSPEND,0) AS SUSPEND, VALUE(T1.MOURNING,0) AS MOURNING, VALUE(T1.LATE,0) AS LATE, VALUE(T1.EARLY,0) AS EARLY, VALUE(T1.ABSENCE,0) AS ABSENCE, ");  
        stb.append(        "VALUE(T2.SHR_LATE,0) AS SHR_LATE, ");
        stb.append(        "VALUE(T2.CLASS_LATE,0) AS CLASS_LATE, ");
        stb.append(        "VALUE(T2.CLASS_MLESSON_RISSI,0) AS CLASS_MLESSON_RISSI, ");
        stb.append(        "VALUE(T2.CLASS_ATTEND_RISSI,0) AS CLASS_ATTEND_RISSI, ");
        stb.append(        "VALUE(T2.CLASS_ABSENCE,0) AS CLASS_ABSENCE, ");
        stb.append(        "VALUE(T2.LHR_LATE,0) AS LHR_LATE ");  
        stb.append("FROM (   SELECT  SEMESTER,MONTH, ");
        stb.append(                 "SUM(LESSON)AS J_NISU, ");
        stb.append(                 "VALUE(SUM(SUSPEND), 0) ");
        if ("true".equals(param._useVirus)) {
            stb.append(                 " + VALUE(SUM(VIRUS), 0) ");
        }
        if ("true".equals(param._useKoudome)) {
            stb.append(                 " + VALUE(SUM(KOUDOME), 0) ");
        }
        stb.append(                 " AS SUSPEND,  ");
        stb.append(                 "SUM(MOURNING)AS MOURNING, ");
        stb.append(                 "SUM(LATE)AS LATE, ");
        stb.append(                 "SUM(EARLY)AS EARLY, ");
        stb.append(                 "VALUE(SUM(SICK), 0) + VALUE(SUM(NOTICE), 0) + VALUE(SUM(NONOTICE), 0)");
        if ("1".equals(param._knjSchoolMst._semOffDays)) {
        stb.append(                    "+ VALUE(SUM(OFFDAYS), 0) ");
        }
        stb.append(                " AS ABSENCE ");
        //授業日数・出停忌引・病欠・事故欠（届／無届）・ＳＨＲ遅刻
        stb.append(         "FROM    ATTEND_SEMES_DAT ");
        stb.append(         "WHERE   SCHREGNO = ? AND YEAR = '" + param._year + "' AND SEMESTER <= '" + param._semester + "' ");
        stb.append(         "GROUP BY SEMESTER, MONTH ");
        stb.append(    ") T1 ");

        stb.append(    "FULL JOIN(");
        stb.append(            "SELECT ");
        stb.append(                "SEMESTER,MONTH, ");
        stb.append(                "SUM(CASE WHEN CLASSCD = '" + KNJDefineSchool.subject_S + "' THEN VALUE(LATE,0) ELSE NULL END) AS SHR_LATE,"); // 92
        stb.append(                "SUM(CASE WHEN CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' THEN VALUE(LATE,0) "); // 01 ~ 89
        stb.append(                                                            "ELSE NULL END) AS CLASS_LATE,");
        stb.append(                "SUM(CASE WHEN CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_T + "' OR CLASSCD IN ('91', '93', '" + KNJDefineSchool.subject_L + "') THEN VALUE(LESSON,0) "); // 01 ~ 90, 91, 93, 94 (立志舎)
        stb.append(                "    - VALUE(ABROAD,0) - VALUE(SUSPEND,0) - VALUE(VIRUS,0) - VALUE(KOUDOME,0) - VALUE(MOURNING,0) ");
        stb.append(                "       ELSE NULL END) AS CLASS_MLESSON_RISSI,");
        stb.append(                "SUM(CASE WHEN CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_T + "' OR CLASSCD IN ('91', '93', '" + KNJDefineSchool.subject_L + "') THEN VALUE(LESSON,0) "); // 01 ~ 90, 91, 93, 94 (立志舎)
        stb.append(                "    - VALUE(ABROAD,0) - VALUE(SUSPEND,0) - VALUE(VIRUS,0) - VALUE(KOUDOME,0) - VALUE(MOURNING,0) ");
        stb.append(                "    - VALUE(SICK,0) - VALUE(NOTICE,0) - VALUE(NONOTICE,0) - VALUE(NURSEOFF,0) ");
        stb.append(                "       ELSE NULL END) AS CLASS_ATTEND_RISSI,");
        stb.append(                "SUM(CASE WHEN CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' THEN VALUE(SICK,0) ");
        stb.append(                        "+VALUE(NOTICE,0)+VALUE(NONOTICE,0)+VALUE(NURSEOFF,0) ");
        if ("1".equals(param._knjSchoolMst._subOffDays)) {
            stb.append(                "+ VALUE(OFFDAYS,0) ");
        }
        if ("1".equals(param._knjSchoolMst._subSuspend)) {
            stb.append(                "+ VALUE(SUSPEND, 0) ");
        }
        if ("1".equals(param._knjSchoolMst._subVirus)) {
            stb.append(                "+ VALUE(VIRUS, 0) ");
        }
        if ("1".equals(param._knjSchoolMst._subKoudome)) {
            stb.append(                "+ VALUE(KOUDOME, 0) ");
        }
        if ("1".equals(param._knjSchoolMst._subMourning)) {
            stb.append(                "+ VALUE(MOURNING, 0) ");
        }
        if ("1".equals(param._knjSchoolMst._subAbsent)) {
            stb.append(                "+ VALUE(ABSENT, 0) ");
        }
        stb.append(                " ELSE NULL END) AS CLASS_ABSENCE,");
        stb.append(                "SUM(CASE WHEN CLASSCD = '" + KNJDefineSchool.subject_L + "'THEN VALUE(LATE,0) ELSE NULL END) AS LHR_LATE "); // 94
        stb.append(            "FROM     ATTEND_SUBCLASS_DAT W1 ");
        stb.append(            "WHERE    W1.SCHREGNO = ? AND W1.YEAR='" + param._year + "'AND W1.SEMESTER <= '" + param._semester + "'");
        stb.append(                "AND (W1.CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' OR W1.CLASSCD IN('" + KNJDefineSchool.subject_T + "', '91', '" + KNJDefineSchool.subject_E + "','" + KNJDefineSchool.subject_L + "') ");
        stb.append(                                                        "OR W1.SUBCLASSCD = '" + KNJDefineSchool.subject_S_A + "') ");
        stb.append(            "GROUP BY SEMESTER,MONTH ");
        stb.append(    ") T2 ON T2.SEMESTER = T1.SEMESTER AND T2.MONTH = T1.MONTH ");

        stb.append("ORDER BY SEMESTER, INT(MONTH) + CASE WHEN MONTH < '04' THEN 12 ELSE 0 END");
        return stb.toString();
    }

    /*------------------------*
     * PrepareStatement SQL
     *------------------------*/
    private String getYearSql(final Param param) {

    	//  全体集計
        final StringBuffer stb = new StringBuffer();
        stb.append("SELECT ");
        stb.append(     "T1.YEAR, T1.ANNUAL, T1.GRADE_CD, T1.SEMESTER, T2.SEMESTERNAME, ");
        stb.append(     "VALUE(T3.J_NISU,0) AS J_NISU, ");
        stb.append(     "VALUE(T3.SUSPEND,0) AS SUSPEND, ");
        stb.append(     "VALUE(T3.MOURNING,0) AS MOURNING, ");
        stb.append(     "VALUE(T3.LATE,0) AS LATE, ");
        stb.append(     "VALUE(T3.EARLY,0) AS EARLY, ");
        stb.append(     "VALUE(T3.ABSENCE,0) AS ABSENCE, ");
        stb.append(     "VALUE(T4.SHR_LATE,0) AS SHR_LATE, ");
        stb.append(     "VALUE(T4.CLASS_MLESSON_RISSI,0) AS CLASS_MLESSON_RISSI, ");
        stb.append(     "VALUE(T4.CLASS_ATTEND_RISSI,0) AS CLASS_ATTEND_RISSI, ");
        stb.append(     "VALUE(T4.CLASS_MLESSON_RISSI96,0) AS CLASS_MLESSON_RISSI96, ");
        stb.append(     "VALUE(T4.CLASS_ATTEND_RISSI96,0) AS CLASS_ATTEND_RISSI96, ");
        stb.append(     "VALUE(T4.CLASS_LATE,0) AS CLASS_LATE, ");
        stb.append(     "VALUE(T4.CLASS_ABSENCE,0) AS CLASS_ABSENCE, ");
        stb.append(     "VALUE(T4.LHR_LATE,0) AS LHR_LATE ");
        stb.append( "FROM ( SELECT    VALUE(REGD.YEAR, '9999') AS YEAR,");
        stb.append(                  "VALUE(REGD.SEMESTER, '9') AS SEMESTER,");
        stb.append(                  "VALUE(REGD.ANNUAL, '99') AS ANNUAL, ");
        stb.append(                  "VALUE(GDAT.GRADE_CD, '99') AS GRADE_CD ");
        stb.append(         "FROM     SCHREG_REGD_DAT REGD ");
        stb.append(         "LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGD.YEAR AND GDAT.GRADE = REGD.GRADE ");
        stb.append(         "WHERE    REGD.SCHREGNO = ? AND REGD.YEAR IN (");
        stb.append(                         "SELECT   MAX(YEAR) AS YEAR ");
        stb.append(                         "FROM     SCHREG_REGD_DAT ");
        stb.append(                         "WHERE    SCHREGNO = ? AND (YEAR < '" + param._year + "' OR YEAR = '" + param._year + "' AND SEMESTER <= '" + param._semester + "') ");
        stb.append(                         "GROUP BY ANNUAL) ");
        stb.append(         "GROUP BY GROUPING SETS ");
        stb.append(             "((REGD.ANNUAL, GDAT.GRADE_CD, REGD.YEAR, REGD.SEMESTER), (REGD.ANNUAL, GDAT.GRADE_CD, REGD.YEAR), ()) ");
        stb.append(     ") T1 ");
        stb.append(     "LEFT JOIN SEMESTER_MST T2 ON T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER ");

        stb.append(     "LEFT JOIN (SELECT  ");
        stb.append(                     "VALUE(YEAR,'9999') AS YEAR, ");
        stb.append(                     "VALUE(SEMESTER,'9') AS SEMESTER,");
        stb.append(                     "SUM(LESSON)  AS J_NISU, ");
        stb.append(                     "SUM(VALUE(SUSPEND, 0) ");
        if ("true".equals(param._useVirus)) {
            stb.append(                 " + VALUE(VIRUS, 0) ");
        }
        if ("true".equals(param._useKoudome)) {
            stb.append(                 " + VALUE(KOUDOME, 0) ");
        }
        stb.append(                     " ) AS SUSPEND,  ");
        stb.append(                     "SUM(MOURNING) AS MOURNING, ");
        stb.append(                     "SUM(LATE) AS LATE, ");
        stb.append(                     "SUM(EARLY) AS EARLY, ");
        stb.append(                     "SUM(VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) ");
        if ("1".equals(param._knjSchoolMst._semOffDays)) {
            stb.append(                    "+ VALUE(OFFDAYS,0) ");
        }
        stb.append(                    ") AS ABSENCE ");
        //授業日数・出停忌引・病欠・事故欠（届／無届）・ＳＨＲ遅刻
        stb.append(             "FROM     ATTEND_SEMES_DAT ");
        stb.append(             "WHERE    SCHREGNO = ? AND (YEAR < '" + param._year + "' OR YEAR = '" + param._year + "' AND SEMESTER <= '" + param._semester + "') ");
        stb.append(             "GROUP BY GROUPING SETS((YEAR,SEMESTER),(YEAR),())");
        stb.append(     ") T3 ON T3.YEAR = T1.YEAR AND T3.SEMESTER = T1.SEMESTER ");
                 
        stb.append(     "LEFT JOIN (SELECT ");
        stb.append(                 "VALUE(YEAR,'9999') AS YEAR, ");
        stb.append(                 "VALUE(SEMESTER,'9') AS SEMESTER,");
        stb.append(                 "SUM(CASE WHEN CLASSCD = '" + KNJDefineSchool.subject_S + "' THEN VALUE(LATE,0) ELSE NULL END) AS SHR_LATE,");
        stb.append(                 "SUM(CASE WHEN CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' THEN VALUE(LATE,0) ELSE NULL END) AS CLASS_LATE,");
        stb.append(                 "SUM(CASE WHEN CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_T + "' OR CLASSCD IN ('91', '93', '" + KNJDefineSchool.subject_L + "') THEN VALUE(LESSON,0) ");
        stb.append(                 "    - VALUE(ABROAD,0) - VALUE(SUSPEND,0) - VALUE(VIRUS,0) - VALUE(KOUDOME,0) - VALUE(MOURNING,0) ");
        stb.append(                 "       ELSE NULL END) AS CLASS_MLESSON_RISSI,"); // 01 ~ 90, 91, 93, 94 (立志舎)
        stb.append(                 "SUM(CASE WHEN CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_T + "' OR CLASSCD IN ('91', '93', '" + KNJDefineSchool.subject_L + "') THEN VALUE(LESSON,0) ");
        stb.append(                 "    - VALUE(ABROAD,0) - VALUE(SUSPEND,0) - VALUE(VIRUS,0) - VALUE(KOUDOME,0) - VALUE(MOURNING,0) ");
        stb.append(                 "    - VALUE(SICK,0) - VALUE(NOTICE,0) - VALUE(NONOTICE,0) - VALUE(NURSEOFF,0) ");
        stb.append(                 "       ELSE NULL END) AS CLASS_ATTEND_RISSI,"); // 01 ~ 90, 91, 93, 94 (立志舎)

        stb.append(                 "SUM(CASE WHEN CLASSCD = '96' THEN VALUE(LESSON,0) "); // 96 (立志舎)
        stb.append(                 "    - VALUE(ABROAD,0) - VALUE(SUSPEND,0) - VALUE(VIRUS,0) - VALUE(KOUDOME,0) - VALUE(MOURNING,0) ");
        stb.append(                 "       ELSE NULL END) AS CLASS_MLESSON_RISSI96,"); // 96 (立志舎)
        stb.append(                 "SUM(CASE WHEN CLASSCD = '96' THEN VALUE(LESSON,0) ");
        stb.append(                 "    - VALUE(ABROAD,0) - VALUE(SUSPEND,0) - VALUE(VIRUS,0) - VALUE(KOUDOME,0) - VALUE(MOURNING,0) ");
        stb.append(                 "    - VALUE(SICK,0) - VALUE(NOTICE,0) - VALUE(NONOTICE,0) - VALUE(NURSEOFF,0) ");
        stb.append(                 "       ELSE NULL END) AS CLASS_ATTEND_RISSI96,"); // 96 (立志舎)

        stb.append(                 "SUM(CASE WHEN CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' THEN VALUE(SICK,0) ");
        stb.append(                         "+ VALUE(NOTICE,0) + VALUE(NONOTICE,0) + VALUE(NURSEOFF,0) ");
        if ("1".equals(param._knjSchoolMst._subOffDays)) {
            stb.append(                        "+ VALUE(OFFDAYS,0) ");
        }
        if ("1".equals(param._knjSchoolMst._subSuspend)) {
            stb.append(                        "+ VALUE(SUSPEND, 0) ");
        }
        if ("1".equals(param._knjSchoolMst._subVirus)) {
            stb.append(                        "+ VALUE(VIRUS, 0) ");
        }
        if ("1".equals(param._knjSchoolMst._subKoudome)) {
            stb.append(                        "+ VALUE(KOUDOME, 0) ");
        }
        if ("1".equals(param._knjSchoolMst._subMourning)) {
            stb.append(                        "+ VALUE(MOURNING, 0) ");
        }
        if ("1".equals(param._knjSchoolMst._subAbsent)) {
            stb.append(                        "+ VALUE(ABSENT, 0) ");
        }
        stb.append(                        "ELSE NULL END) AS CLASS_ABSENCE,");
        stb.append(                 "SUM(CASE WHEN CLASSCD = '" + KNJDefineSchool.subject_L + "' THEN VALUE(LATE,0) ELSE NULL END) AS LHR_LATE ");
        stb.append(             "FROM     ATTEND_SUBCLASS_DAT ");
        stb.append(             "WHERE    SCHREGNO = ? AND (YEAR < '" + param._year + "' OR YEAR = '" + param._year + "' AND SEMESTER <= '" + param._semester + "')");
        stb.append(                         "AND(CLASSCD BETWEEN'" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' OR ");
        stb.append(                             "CLASSCD IN ('" + KNJDefineSchool.subject_T + "', '91', '" + KNJDefineSchool.subject_E + "', '" + KNJDefineSchool.subject_L + "', '96') OR SUBCLASSCD = '" + KNJDefineSchool.subject_S + "0100') ");
        stb.append(             "GROUP BY GROUPING SETS((YEAR,SEMESTER),(YEAR),())");
        stb.append(     ") T4  ON T4.YEAR = T1.YEAR AND T4.SEMESTER = T1.SEMESTER ");
        if (param._isRisshisha) {
        	// データがない年度は印字しない
            stb.append(" WHERE    (T3.YEAR IS NOT NULL OR T4.YEAR IS NOT NULL) ");
        }

        stb.append( "ORDER BY T1.ANNUAL,T1.SEMESTER");
        return stb.toString();
    }

    private static class Student {
    	final String _schregno;
    	String _coursecode;
    	String _classMlessonRisshisha96;
    	String _classAttendRisshisha96;
    	Student(final String schregno) {
    		_schregno = schregno;
    	}
    }

    private static class Param {
        final String _year;
        final String _semester;
        final String _semeflg;
        final String _date;
        final String _ctrlDate;
        final String _gradeHrclass;
        final String _useCurriculumcd;
        final String _useVirus;
        final String _useKoudome;
        final String[] _category_name;
        final String _z010name;
        final boolean _isRisshisha;
        final String _knjc120PrintGradeCd;
        final String _ctrlDateString;
        final String _dateString;

        private KNJSchoolMst _knjSchoolMst;
        
        private final Map<Integer, String> _zenkakuMap = Map_Month(); //月項目名用のMAP作成
        
        //SQL作成
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;
        PreparedStatement ps3 = null;
        PreparedStatement ps4 = null;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("YEAR");  // 年度
            _semester = request.getParameter("SEMESTER");  // 学期
            _semeflg = request.getParameter("SEME_FLG");  // LOG-IN時の学期（現在学期）
            _date = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE"));  // 出欠集計日付
            _ctrlDate = KNJ_EditDate.H_Format_Haifun(request.getParameter("CTRL_DATE"));  // 学籍処理日
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");  // 学年・組
            _category_name = request.getParameterValues("category_name");   //対象学籍番号;
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
            _z010name = getZ010(db2);
            _isRisshisha = "risshisha".equals(_z010name);
            _knjc120PrintGradeCd = request.getParameter("knjc120PrintGradeCd");
            try {
                final Map smParamMap = new HashMap();
                if (KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND")) {
                	final String schoolKind = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _gradeHrclass.substring(0, 2) + "' "));
					smParamMap.put("SCHOOL_KIND", schoolKind);
                }
                _knjSchoolMst = new KNJSchoolMst(db2, _year, smParamMap);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }
            _ctrlDateString = KNJ_EditDate.h_format_JP(db2, _ctrlDate);
            _dateString = KNJ_EditDate.h_format_JP(db2, _date);
        }
        
        /*------------------------*
         * 月項目名用のMAP作成
         *------------------------*/
        private Map<Integer, String> Map_Month() {
            final Map<Integer, String> m = new HashMap();
            final int han[] = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
            final String zen[] = {"１", "２", "３", "４", "５", "６", "７", "８", "９", "１０", "１１", "１２"};
            for (int i = 0; i < han.length; i++) {
            	m.put(new Integer(han[i]), zen[i]);
            }
            return m;
        }
        
        private String getZ010(final DB2UDB db2) {
        	return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' "));
        }

    }

}