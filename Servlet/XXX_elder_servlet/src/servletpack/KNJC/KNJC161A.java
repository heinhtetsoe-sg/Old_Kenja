// kanji=漢字
/*
 * $Id: ead2c3d19217b975521c45b8721f690728090438 $
 *
 */
package servletpack.KNJC;

import java.io.IOException;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.CsvUtils;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/*
 *  学校教育システム 賢者 [出欠管理] 出欠集計表
 *
 *  注意事項：csvBufCtlクラスに、CSV出力位置とfrmの名称をMAPに登録する処理があるため、
 *            フォーム変更の際はそちらの変更も必要となるので、注意すること。
 */

public class KNJC161A {

    private static final Log log = LogFactory.getLog(KNJC161A.class);

    private static final String csv = "csv";

    private static final int _useKindCnt = 2;          //(月別表)扱う校種の数(H/J)
    private static final int _monthcolsize = 5;        //(月別表)4ケ月分+TOTAL
    private static final int _yearcolsize = 12;        //(年度表)11ケ月分+TOTAL
    private static final int _grade_h_classsize = 13;   //(月別表/年度表)1学年の行数+TOTAL行の1行
    private static final int _grade_j_classsize = 3;    //(月別表/年度表)1学年の行数+TOTAL行の1行

    private boolean _hasdata = false;
    private Map _gradefieldmap = new HashMap(); //学年毎の帳票フィールドID保持マップ

    public void svf_out (
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {

        Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）

        try {
            response.setContentType("application/pdf");
            svf.VrInit();                             //クラスの初期化
            svf.VrSetSpoolFileStream(response.getOutputStream());         //PDFファイル名の設定
        } catch (java.io.IOException ex) {
            log.error("svf instancing exception! ", ex);
        }

        // ＤＢ接続
        DB2UDB db2 = null;
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);    //Databaseクラスを継承したクラス
            db2.open();
        } catch (Exception ex) {
            log.error("db2 instancing exception! ", ex);
            return;
        }

        _hasdata = false;
        Param param = null;
        _gradefieldmap.put("06", "1"); //高3
        _gradefieldmap.put("05", "2"); //高2
        _gradefieldmap.put("04", "3"); //高1
        _gradefieldmap.put("03", "4"); //中3
        _gradefieldmap.put("02", "5"); //中2
        _gradefieldmap.put("01", "6"); //中1

        try {
            log.debug(" $Revision: 72316 $ $Date: 2020-02-10 11:41:34 +0900 (月, 10 2 2020) $ ");
            KNJServletUtils.debugParam(request, log);
            param = new Param(request, db2);
            if (param._monthList.size() > 0) {
                if (csv.equals(param._cmd)) {
                    final List outputLines = new ArrayList();
                    svfPrint(db2, param, svf, outputLines);
                    CsvUtils.outputLines(log, response, param._fName + ".csv", outputLines);
                } else {
                    // 印刷処理
                    svfPrint(db2, param, svf, null);
                }
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            if (null != param) {
                DbUtils.closeQuietly(param._sqlAttendance);
            }
            // 終了処理
            if (!_hasdata) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
            svf.VrQuit();
        }
    }

    /**
     *  印刷処理
     */
    private void svfPrint(
            final DB2UDB db2,
            final Param param,
            final Vrw32alp svf,
            final List csvlist
    ) {
        final List grades = new ArrayList(); // 学年のリスト
        final Map gradeClassMap = new TreeMap(); // 学年ごとのHRクラスのリストマップ

//        //画面指定が8月のみの場合、内部処理で8月のデータを9月に集約しているため、結果として8月のデータが取れないので、対象外とする。
//        //->8月のみ指定でも「取得データは8月のみ。出力は"8・9月"列」で出力できるよう、修正。
//        if (param._smonth == param._emonth && param._smonth == 8) {
//        	return;
//        }
        //学籍のSQL
        setHrClasses(db2, param, grades, gradeClassMap);
        //学級毎の出欠データを取得
        setHrInfo(db2, param, grades, gradeClassMap);
        //各学級ごとの欠席者情報を取得
        if (!"2".equals(param._outputSelect)) {
            setAttendInfo(db2, param, grades, gradeClassMap);
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(Date.valueOf(StringUtils.replace(param._edate2, "/", "-")));
        int setmonth = cal.get(Calendar.MONTH) + 1;

        if ("2".equals(param._outputSelect)) {
        	//通年
            final String form = "KNJC161A_2.frm";
            log.debug("frm:"+form);
            svf.VrSetForm(form, 1);

            csvBufCtl linebuf = new csvBufCtl(false);
            List outbuf = null;

            if (csvlist == null) {
                svf.VrsOut("TITLE", param._year + "年度" + "　職会用出欠統計表　年間");
                svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, param._loginDate));
            } else {
            	param._fName = param._year + "年度" + "　職会用出欠統計表　年間";
            	csvlist.add(Arrays.asList(new String[]{param._year + "年度" + "　職会用出欠統計表　年間", KNJ_EditDate.h_format_JP(db2, param._loginDate)}));
            	setCsvTitle3(linebuf._csvbuf);
            	outbuf = linebuf.convCsvBuf();
            	csvlist.add(outbuf);
            	linebuf.clrCsvBuf();
            	setCsvTitle4(linebuf._csvbuf);
            }

            int titlecnt = _yearcolsize - 1;
            for (int cnt = 1; cnt <= param._monthList.size();cnt++) {
            	if (cnt > titlecnt) continue;
            	String setttlmonth = String.valueOf(Integer.parseInt((String)param._monthList.get(cnt - 1)));
            	if (!"9".equals(setttlmonth)) {
            		setttlmonth += "月";
                	bufOut(svf, "MONTH1_" + cnt, setttlmonth, linebuf, csvlist);
                	bufOut(svf, "MONTH2_" + cnt, setttlmonth, linebuf, csvlist);
                	bufOut(svf, "MONTH3_" + cnt, setttlmonth, linebuf, csvlist);
            	} else {
            		setttlmonth = "8・9月";
                	bufOut(svf, "MONTH1_" + cnt + "_2", setttlmonth, linebuf, csvlist);
                	bufOut(svf, "MONTH2_" + cnt + "_2", setttlmonth, linebuf, csvlist);
                	bufOut(svf, "MONTH3_" + cnt + "_2", setttlmonth, linebuf, csvlist);
            	}
            }

            if (csvlist != null) {
            	outbuf = linebuf.convCsvBuf();
            	csvlist.add(outbuf);
            	linebuf.clrCsvBuf();
            }

            final int[] ttlstudentnumcnt = new int[2];  //"H"と"J"2ケ分
            final AttendanceCount[][] totalAttend = new AttendanceCount[_useKindCnt][_yearcolsize];  //"H"と"J"2ケ分 * 月別データ数
            for (int i = 0;i < _useKindCnt;i++) {
            	for (int j = 0;j < _yearcolsize;j++ ) {
            		totalAttend[i][j] = new AttendanceCount();
            	}
            }

            int hcnt = 0;
            int jcnt = 0;
            for(final Iterator it = grades.iterator(); it.hasNext();) {
                final GradeCls grade = (GradeCls) it.next();
                final List hrClasses = (List) gradeClassMap.get(grade._grade);
                if (hcnt > 0 && "J".equals(grade._schkind)) {
                	//H->J切り替わりのタイミングで出力
                    printTotalAttendanceInfo(svf, param, ttlstudentnumcnt, totalAttend, "H", csvlist);
                }
                svfPrintGrade2(svf, param, grade, hrClasses, ttlstudentnumcnt, totalAttend, csvlist);
                _hasdata = true;
                if ("J".equals(grade._schkind)) {
                	jcnt++;
                } else {
                	hcnt++;
                }
            }

            if (_hasdata && jcnt > 0) {
                printTotalAttendanceInfo(svf, param, ttlstudentnumcnt, totalAttend, "J", csvlist);
            }
        } else {
        	//学期
            final String form = "KNJC161A_1.frm";
            log.debug("frm:"+form);
            svf.VrSetForm(form, 1);

            csvBufCtl linebuf = new csvBufCtl(true);
            List outbuf = null;

            if (csvlist == null) {
                svf.VrsOut("TITLE", param._year + "年度" + setmonth + "月" + "　職会用出欠統計表　");
                final String subttlstr = param._smonth == param._emonth ? "" : " ～ " + setmonth + "月";
                svf.VrsOut("SUBTITLE", "(" + param._smonth + "月" + subttlstr + ")" );
                svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, param._loginDate));
                svf.VrsOut("ATTEND_MONTH", setmonth + "月");
                svf.VrsOut("NOTICE_NAME1", "朝礼欠" + param._earlyCnt + "回以上");
                svf.VrsOut("NOTICE_NAME2", "欠席" + param._sickCnt + "日以上");
            } else {
            	param._fName = param._year + "年度" + setmonth + "月" + "　職会用出欠統計表　";
                final String subttlstr = param._smonth == param._emonth ? "" : " ～ " + setmonth + "月";
            	csvlist.add(Arrays.asList(new String[] {param._year + "年度" + setmonth + "月" + "　職会用出欠統計表　", "(" + param._smonth + "月" + subttlstr + ")", KNJ_EditDate.h_format_JP(db2, param._loginDate)}));
            	setCsvTitle1(linebuf._csvbuf, setmonth);
            	outbuf = linebuf.convCsvBuf();
            	csvlist.add(outbuf);
            	linebuf.clrCsvBuf();
            	setCsvTitle2(linebuf._csvbuf, param);
            }

            int titlecnt = _monthcolsize - 1;
            for (int cnt = 1; cnt <= param._monthList.size();cnt++) {
            	if (cnt > titlecnt) continue;
            	String setttlmonth = String.valueOf(Integer.valueOf((String)param._monthList.get(cnt - 1)));
            	if ("8".equals(setttlmonth) || "9".equals(setttlmonth)) {
            		setttlmonth = "8・9月";
                	bufOut(svf, "MONTH1_" + cnt + "_2", setttlmonth, linebuf, csvlist);
                	bufOut(svf, "MONTH2_" + cnt + "_2", setttlmonth, linebuf, csvlist);
                	bufOut(svf, "MONTH3_" + cnt + "_2", setttlmonth, linebuf, csvlist);
            	} else {
            		setttlmonth += "月";
                	bufOut(svf, "MONTH1_" + cnt, setttlmonth, linebuf, csvlist);
                	bufOut(svf, "MONTH2_" + cnt, setttlmonth, linebuf, csvlist);
                	bufOut(svf, "MONTH3_" + cnt, setttlmonth, linebuf, csvlist);
            	}
            }

            if (csvlist != null) {
            	outbuf = linebuf.convCsvBuf();
            	csvlist.add(outbuf);
            	linebuf.clrCsvBuf();
            }

            final int[] ttlstudentnumcnt = new int[2];  //"H"と"J"2ケ分
            final AttendanceCount[][] totalAttend = new AttendanceCount[_useKindCnt][_monthcolsize];  //"H"と"J"2ケ分 * 月別データ数
            for (int i = 0;i < _useKindCnt;i++) {
            	for (int j = 0;j < _monthcolsize;j++ ) {
            		totalAttend[i][j] = new AttendanceCount();
            	}
            }


            int hcnt = 0;
            int jcnt = 0;
            boolean h_totalprintflg = false;
            for(final Iterator it = grades.iterator(); it.hasNext();) {
                final GradeCls grade = (GradeCls) it.next();
                final List hrClasses = (List) gradeClassMap.get(grade._grade);

                if (hcnt > 0 && "J".equals(grade._schkind) && !h_totalprintflg) {
                	//H->J切り替わりのタイミングで出力
                    printTotalAttendanceInfo(svf, param, ttlstudentnumcnt, totalAttend, "H", csvlist);
                    h_totalprintflg = true;
                }
                svfPrintGrade(svf, param, grade, hrClasses, ttlstudentnumcnt, totalAttend, csvlist);
                _hasdata = true;
                if ("J".equals(grade._schkind)) {
                	jcnt++;
                } else {
                	hcnt++;
                }
            }

            if (_hasdata && jcnt > 0) {
            	//最後に出力
                printTotalAttendanceInfo(svf, param, ttlstudentnumcnt, totalAttend, "J", csvlist);
            }
        }
        svf.VrEndPage();
    }

    private void setCsvTitle1(final String[] linebuf, final int setmonth) {
    	//0-2 空行
    	linebuf[3] = "欠席日数";
    	//4-7 空行
    	linebuf[8] = "朝礼欠回数";
    	//9-12 空行
    	linebuf[13] = "退礼欠回数";
    	//14-18 空行
    	linebuf[19] = String.valueOf(setmonth)+"月";
    }

    private void setCsvTitle2(final String[] linebuf, final Param param) {
    	linebuf[0] = "学年";
    	linebuf[1] = "組";
    	linebuf[2] = "生徒数";
    	//3-6 ※空いてる箇所は呼び出し元のループ処理で設定
    	linebuf[7] = "計";
    	//8-11 ※空いてる箇所は呼び出し元のループ処理で設定
    	linebuf[12] = "計";
    	//13-16 ※空いてる箇所は呼び出し元のループ処理で設定
    	linebuf[17] = "計";
    	linebuf[18] = "朝礼欠" + param._earlyCnt + "回以上";  //A
    	linebuf[19] = "欠席" + param._sickCnt + "日以上";  //B
    }

    private void setCsvTitle3(final String[] linebuf) {
    	//0-2 空行
    	linebuf[3] = "欠席日数";
    	//4-14 空行
    	linebuf[15] = "朝礼欠回数";
    	//16-26 空行
    	linebuf[27] = "退礼欠回数";
    }

    private void setCsvTitle4(final String[] linebuf) {
    	linebuf[0] = "学年";
    	linebuf[1] = "組";
    	linebuf[2] = "生徒数";
    	//3-13 ※空いてる箇所は呼び出し元のループ処理で設定
    	linebuf[14] = "計";
    	//15-25 ※空いてる箇所は呼び出し元のループ処理で設定
    	linebuf[26] = "計";
    	//27-37 ※空いてる箇所は呼び出し元のループ処理で設定
    	linebuf[38] = "計";
    	//3-38 ※空いてる箇所は呼び出し元のループ処理で設定
    }

    private static void setHrClasses(final DB2UDB db2, final Param param, final List grades, final Map gradeClassMap) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        final Map gradeHrClassMap = new HashMap();
        try {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     GD.SCHOOL_KIND, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.HR_NAME ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_HDAT T1 ");
            stb.append("     INNER JOIN SCHREG_REGD_GDAT GD ON GD.YEAR = T1.YEAR ");
            stb.append("           AND GD.GRADE = T1.GRADE ");
            stb.append("           AND GD.SCHOOL_KIND IN " + param._schKindinState + " ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND GD.GRADE_CD <= '03' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.GRADE DESC, ");
            stb.append("     T1.HR_CLASS ");
            final String sqlRegdH = stb.toString();
            //log.debug("sqlRegdH:"+sqlRegdH);
            ps = db2.prepareStatement(sqlRegdH);
            rs = ps.executeQuery();

            while(rs.next()) {
                final String schkind = rs.getString("SCHOOL_KIND");
                final String grade = rs.getString("GRADE");
            	GradeCls gcls = new GradeCls(grade, schkind);
                List hrClasses = null;
                for(final Iterator it = grades.iterator(); it.hasNext(); ) {
                    final GradeCls grade1 = (GradeCls) it.next();
                    if (grade1._grade.equals(grade)) {
                        hrClasses = (List) gradeClassMap.get(grade1._grade);
                        break;
                    }
                }
                if (hrClasses == null) {
                    grades.add(gcls);
                    hrClasses = new ArrayList();
                    gradeClassMap.put(grade, hrClasses);
                }

                final HrClass hrClass = new HrClass(rs.getString("GRADE"), rs.getString("HR_CLASS"), rs.getString("HR_NAME"));
                hrClasses.add(hrClass);
                gradeHrClassMap.put(rs.getString("GRADE") + rs.getString("HR_CLASS"), hrClass);
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        }

        for(final Iterator it = grades.iterator(); it.hasNext();) {
            final GradeCls grade = (GradeCls) it.next();
            final List hrClasses = (List) gradeClassMap.get(grade._grade);
            load(db2, param, grade._grade, hrClasses);
        }
    }

    private static void setHrInfo(final DB2UDB db2, final Param param, final List grades, final Map gradeClassMap) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean getoneyearflg = false;
        if ("2".equals(param._outputSelect)) {
        	getoneyearflg = true;
        }

        try {
            String sql = sqlHrInfo(param, getoneyearflg);
            log.debug("hrinfosql:" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while(rs.next()) {
            	final String schkind = rs.getString("SCHOOL_KIND");
            	final String grade = rs.getString("GRADE");
            	final String hrclass = rs.getString("HR_CLASS");
            	final String month = rs.getString("MONTH");
            	final String stucnt = StringUtils.defaultIfEmpty(rs.getString("STUCNT"), "0");
            	final String sick = StringUtils.defaultIfEmpty(rs.getString("SICK"), "0");
            	final String late = StringUtils.defaultIfEmpty(rs.getString("LATE"), "0");
            	final String early = StringUtils.defaultIfEmpty(rs.getString("EARLY"), "0");
                List hrClasses = null;
                for(final Iterator it = grades.iterator(); it.hasNext(); ) {
                    final GradeCls grade1 = (GradeCls) it.next();
                    if (grade1._grade.equals(grade)) {
                        hrClasses = (List) gradeClassMap.get(grade1._grade);
                        for(final Iterator itc = hrClasses.iterator(); itc.hasNext(); ) {
                            final HrClass classwk = (HrClass) itc.next();
                            if (classwk._hrClass.equals(hrclass)) {
                                AttendanceCount acwk = new AttendanceCount();
                                acwk._sick = Integer.parseInt(sick);
                                acwk._late = Integer.parseInt(late);
                                acwk._early = Integer.parseInt(early);
                                classwk._hrClassAttendMonthMap.put(month, acwk);
                                classwk._studentcnt = Integer.parseInt(stucnt);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(ps);
            db2.commit();
        }

    }

    private static void setAttendInfo(final DB2UDB db2, final Param param, final List grades, final Map gradeclassmap) {
        for(final Iterator it = grades.iterator(); it.hasNext(); ) {
            final GradeCls gradec = (GradeCls) it.next();
            final List hrclasses = (List) gradeclassmap.get(gradec._grade);
            for(final Iterator itc = hrclasses.iterator(); itc.hasNext(); ) {
                final HrClass classwk = (HrClass) itc.next();
                classwk._AttendStudentList = getAttendStudentList(db2, param, classwk);
            }
        }
    }

    private static Map getAttendStudentList(final DB2UDB db2, final Param param, final HrClass classwk) {
    	Map retMap = null;

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            String sql = sqlStudentAttend(param, classwk);
            //log.debug("sqlStudentAttend:" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while(rs.next()) {
            	final String name = rs.getString("NAME");
            	final String sick = rs.getString("SICK");
            	final String early = rs.getString("EARLY");
            	final String late = rs.getString("LATE");
            	if (retMap == null) {
            		retMap = new HashMap();
            		List sicklist = new ArrayList();
            		List earlylist = new ArrayList();
            		retMap.put("SICK", sicklist);
            		retMap.put("EARLY", earlylist);
            	}
            	if (sick != null && !"0".equals(sick) && Integer.parseInt(sick) >= param._sickCnt) {
            		((List)retMap.get("SICK")).add(name + "(" + sick + ")");
            	}
            	if (early != null && !"0".equals(early) && Integer.parseInt(early) >= param._earlyCnt) {
            		((List)retMap.get("EARLY")).add(name + "(" + early + ")");
            	}
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(ps);
            db2.commit();
        }

    	return retMap;
    }


    private static String sqlHrInfo(final Param param, boolean getoneyearflg) {
        final StringBuffer stb = new StringBuffer();
        //1.一度生徒単位で出欠を月単位で集計(SUMBASEDAT)。
        // ※年度の場合は8月のデータを9月に変換して集計対象とする。
        //2.そのうえで、抽出対象条件に合う生徒を抽出(FILTERDAT)。-> 18/05/09 対象者の抽出条件であり、集計には抽出条件を利用しないのでWHERE条件は削除。
        //3.対象生徒をクラス単位にまとめる(FINDSUMDAT)。
        //4.このままでは抽出が「対象の生徒が居るクラスのみ」のため、出力ベースとなるデータを作成(TBLBASEDAT)
        //5.クラス単位の生徒数(該当学期でのMAX人数)を取得(STUCNTDAT)。
        //3.4.5.をまとめて出力。

        int smonthwk = Integer.parseInt(StringUtils.substring(param._sdate, 4, 6));
        int emonthwk = Integer.parseInt(StringUtils.substring(param._edate, 4, 6));
        //8月が含まれていた場合、8月分を9月にまとめるため別口で取る。データの取得範囲はきちんと切り分ける。
        //処理としては「年間」でない場合は4ヶ月までしか出力しないため、12月以上(つまり1～3月)指定しても除外対象だが、範囲チェックとして残しておく。
        boolean incluingeightflg = false;
        if (smonthwk <= 8 && (8 <= emonthwk || emonthwk <= 3)) {
        	incluingeightflg = true;
        }

        stb.append(" WITH SUMBASEDAT AS ( ");
        stb.append(" SELECT ");
        stb.append("   T4.SCHOOL_KIND, ");
        stb.append("   T2.GRADE, ");
        stb.append("   T2.HR_CLASS, ");
        stb.append("   T1.MONTH, ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T6.NAME, ");
        stb.append("   SUM(VALUE(T1.SICK, 0) + VALUE(T1.NOTICE, 0) + VALUE(T1.NONOTICE, 0)) AS SICK, ");
        stb.append("   SUM(T31.CNT) AS LATE, ");
        stb.append("   SUM(T32.CNT) AS EARLY ");
        stb.append("  FROM  ATTEND_SEMES_DAT T1 ");
        stb.append("      LEFT JOIN ATTEND_SEMES_DETAIL_DAT T31 ");
        stb.append("        ON T1.YEAR = T31.YEAR ");
        stb.append("        AND T1.SEMESTER = T31.SEMESTER ");
        stb.append("        AND T1.SCHREGNO = T31.SCHREGNO ");
        stb.append("        AND T1.COPYCD = T31.COPYCD ");
        stb.append("        AND T1.MONTH = T31.MONTH ");
        stb.append("        AND T31.SEQ = '002' ");
        stb.append("      LEFT JOIN ATTEND_SEMES_DETAIL_DAT T32 ");
        stb.append("        ON T1.YEAR = T32.YEAR ");
        stb.append("        AND T1.SEMESTER = T32.SEMESTER ");
        stb.append("        AND T1.SCHREGNO = T32.SCHREGNO ");
        stb.append("        AND T1.COPYCD = T32.COPYCD ");
        stb.append("        AND T1.MONTH = T32.MONTH ");
        stb.append("        AND T32.SEQ = '001' ");
        stb.append("      LEFT JOIN SCHREG_REGD_DAT T2 ");
        stb.append("          ON T1.YEAR = T2.YEAR ");
        stb.append("          AND T1.SEMESTER = T2.SEMESTER ");
        stb.append("          AND T1.SCHREGNO = T2.SCHREGNO ");
        stb.append("      LEFT JOIN SCHREG_REGD_GDAT T4 ");
        stb.append("          ON T2.YEAR = T4.YEAR ");
        stb.append("          AND T2.GRADE = T4.GRADE ");
        stb.append("      LEFT JOIN SCHREG_BASE_MST T6 ");
        stb.append("          ON T1.SCHREGNO = T6.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     T2.GRADE IS NOT NULL ");
        if (!getoneyearflg) {
            stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND INT('" + param._sdate + "') <= (INT(T1.YEAR) + CASE WHEN T1.MONTH IN ('01', '02', '03') THEN 1 ELSE 0 END) * 10000 + INT(T1.MONTH || T1.APPOINTED_DAY) ");
            stb.append("     AND (INT(T1.YEAR) + CASE WHEN T1.MONTH IN ('01', '02', '03') THEN 1 ELSE 0 END) * 10000 + INT(T1.MONTH || T1.APPOINTED_DAY) <= INT('" + param._edate + "') ");
            if (incluingeightflg) {
            	//8月が含まれていた場合、8月分を9月にまとめるため別口で取る。
                stb.append("     AND T1.MONTH <> '08' ");
            }
        } else {
            stb.append("     AND INT('" + param._year + "0401" + "') <= INT(T1.YEAR || T1.MONTH || T1.APPOINTED_DAY) ");
            stb.append("     AND INT(T1.YEAR || T1.MONTH || T1.APPOINTED_DAY) <= INT('" + (Integer.parseInt(param._year) + 1) + "0331" + "') ");
            stb.append("     AND T1.MONTH <> '08' ");
        }
        stb.append(" GROUP BY ");
        stb.append("   T4.SCHOOL_KIND, ");
        stb.append("   T2.GRADE, ");
        stb.append("   T2.HR_CLASS, ");
        stb.append("   T1.MONTH, ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T6.NAME ");
        if (getoneyearflg || incluingeightflg) {
        	stb.append(" UNION ");
            stb.append(" SELECT ");
            stb.append("   T4.SCHOOL_KIND, ");
            stb.append("   T2.GRADE, ");
            stb.append("   T2.HR_CLASS, ");
            stb.append("   '09' AS MONTH, ");
            stb.append("   T1.SCHREGNO, ");
            stb.append("   T6.NAME, ");
            stb.append("   SUM(VALUE(T1.SICK, 0) + VALUE(T1.NOTICE, 0) + VALUE(T1.NONOTICE, 0)) AS SICK, ");
            stb.append("   SUM(T31.CNT) AS LATE, ");
            stb.append("   SUM(T32.CNT) AS EARLY ");
            stb.append("  FROM  ATTEND_SEMES_DAT T1 ");
            stb.append("      LEFT JOIN ATTEND_SEMES_DETAIL_DAT T31 ");
            stb.append("        ON T1.YEAR = T31.YEAR ");
            stb.append("        AND T1.SEMESTER = T31.SEMESTER ");
            stb.append("        AND T1.SCHREGNO = T31.SCHREGNO ");
            stb.append("        AND T1.COPYCD = T31.COPYCD ");
            stb.append("        AND T1.MONTH = T31.MONTH ");
            stb.append("        AND T31.SEQ = '002' ");
            stb.append("      LEFT JOIN ATTEND_SEMES_DETAIL_DAT T32 ");
            stb.append("        ON T1.YEAR = T32.YEAR ");
            stb.append("        AND T1.SEMESTER = T32.SEMESTER ");
            stb.append("        AND T1.SCHREGNO = T32.SCHREGNO ");
            stb.append("        AND T1.COPYCD = T32.COPYCD ");
            stb.append("        AND T1.MONTH = T32.MONTH ");
            stb.append("        AND T32.SEQ = '001' ");
            stb.append("      LEFT JOIN SCHREG_REGD_DAT T2 ");
            stb.append("          ON T1.YEAR = T2.YEAR ");
            stb.append("          AND T1.SEMESTER = T2.SEMESTER ");
            stb.append("          AND T1.SCHREGNO = T2.SCHREGNO ");
            stb.append("      LEFT JOIN SCHREG_REGD_GDAT T4 ");
            stb.append("          ON T2.YEAR = T4.YEAR ");
            stb.append("          AND T2.GRADE = T4.GRADE ");
            stb.append("      LEFT JOIN SCHREG_BASE_MST T6 ");
            stb.append("          ON T1.SCHREGNO = T6.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     T2.GRADE IS NOT NULL ");
            if (!getoneyearflg) {
                stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
                stb.append("     AND '" + param._year + "' = T1.YEAR ");
            } else {
                stb.append("     AND INT('" + param._year + "0401" + "') <= INT(T1.YEAR || T1.MONTH || T1.APPOINTED_DAY) ");
                stb.append("     AND INT(T1.YEAR || T1.MONTH ||  T1.APPOINTED_DAY) <= INT('" + (Integer.parseInt(param._year) + 1) + "0331" + "') ");
            }
            stb.append("     AND T1.MONTH = '08' ");
            stb.append(" GROUP BY ");
            stb.append("   T4.SCHOOL_KIND, ");
            stb.append("   T2.GRADE, ");
            stb.append("   T2.HR_CLASS, ");
            stb.append("   T1.MONTH, ");
            stb.append("   T1.SCHREGNO, ");
            stb.append("   T6.NAME ");
        }
        stb.append(" ),FILTERDAT AS ( ");
        stb.append("  SELECT ");
        stb.append("   SBD1.SCHOOL_KIND, ");
        stb.append("   SBD1.GRADE, ");
        stb.append("   SBD1.HR_CLASS, ");
        stb.append("   SBD1.MONTH, ");
        stb.append("   SBD1.SCHREGNO, ");
        stb.append("   SBD1.NAME, ");
        stb.append("   SBD1.SICK, ");
        stb.append("   SBD1.LATE, ");
        stb.append("   SBD1.EARLY ");
        //人数をカウントする場合、下記コメント項目をSUMBASEDATで集計対象にする(例:SICK->SICKCNT)こと。
//        stb.append("   CASE WHEN SBD1.SICK >= 1 THEN 1 ELSE 0 END AS SICKCNT, ");
//        stb.append("   CASE WHEN SBD1.LATE >= 0 THEN 1 ELSE 0 END AS LATECNT, ");
//        stb.append("   CASE WHEN SBD1.EARLY >= 0 THEN 1 ELSE 0 END AS EARLYCNT ");
        stb.append("  FROM  SUMBASEDAT SBD1 ");
// 抽出条件を削除。(18/05/09)
//        stb.append("  WHERE ");
//        stb.append("         SBD1.SICK >= " + param._sickCnt + " ");
//        stb.append("         OR SBD1.EARLY >= " + param._earlyCnt + " ");
        stb.append(" ), FINDSUMDAT AS ( ");
        stb.append(" SELECT ");
        stb.append("   FD1.SCHOOL_KIND, ");
        stb.append("   FD1.GRADE, ");
        stb.append("   FD1.HR_CLASS, ");
        stb.append("   FD1.MONTH, ");
        stb.append("   SUM(FD1.SICK) AS SICK, ");
        stb.append("   SUM(FD1.LATE) AS LATE, ");
        stb.append("   SUM(FD1.EARLY) AS EARLY ");
        stb.append(" FROM  FILTERDAT FD1 ");
        stb.append(" GROUP BY ");
        stb.append("   FD1.SCHOOL_KIND, ");
        stb.append("   FD1.GRADE, ");
        stb.append("   FD1.HR_CLASS, ");
        stb.append("   FD1.MONTH ");
        stb.append(" ORDER BY ");
        stb.append("   FD1.SCHOOL_KIND, ");
        stb.append("   FD1.GRADE, ");
        stb.append("   FD1.HR_CLASS, ");
        stb.append("   FD1.MONTH ");
        stb.append(" ), TBLBASEDAT AS ( ");
        stb.append(" SELECT ");
        stb.append("   T4.SCHOOL_KIND, ");
        stb.append("   T2.GRADE, ");
        stb.append("   T2.HR_CLASS, ");
        stb.append("   T1.MONTH, ");
        stb.append("   0 AS SICK, ");
        stb.append("   0 AS LATE, ");
        stb.append("   0 AS EARLY ");
        stb.append(" FROM SCHREG_REGD_DAT T2 ");
        stb.append("      LEFT JOIN ATTEND_SEMES_DAT T1 ");
        stb.append("          ON T1.YEAR = T2.YEAR ");
        stb.append("          AND T1.SEMESTER = T2.SEMESTER ");
        stb.append("          AND T1.SCHREGNO = T2.SCHREGNO ");
        stb.append("      LEFT JOIN SCHREG_REGD_GDAT T4 ");
        stb.append("          ON T2.YEAR = T4.YEAR ");
        stb.append("          AND T2.GRADE = T4.GRADE ");
        stb.append(" WHERE ");
        stb.append("    T2.YEAR = '" + param._year + "' ");
        stb.append("    AND T2.SEMESTER = '" + param._semester + "' ");
        stb.append("    AND MONTH IS NOT NULL ");
        stb.append("    AND MONTH <> '08' ");
        stb.append(" GROUP BY ");
        stb.append("   T4.SCHOOL_KIND, ");
        stb.append("   T2.GRADE, ");
        stb.append("   T2.HR_CLASS, ");
        stb.append("   T1.MONTH ");
        if (getoneyearflg || incluingeightflg) {
            stb.append(" UNION ");
            stb.append(" SELECT ");
            stb.append("   T4.SCHOOL_KIND, ");
            stb.append("   T2.GRADE, ");
            stb.append("   T2.HR_CLASS, ");
            stb.append("   '09' AS MONTH, ");
            stb.append("   0 AS SICK, ");
            stb.append("   0 AS LATE, ");
            stb.append("   0 AS EARLY ");
            stb.append(" FROM SCHREG_REGD_DAT T2 ");
            stb.append("      LEFT JOIN ATTEND_SEMES_DAT T1 ");
            stb.append("          ON T1.YEAR = T2.YEAR ");
            stb.append("          AND T1.SEMESTER = T2.SEMESTER ");
            stb.append("          AND T1.SCHREGNO = T2.SCHREGNO ");
            stb.append("      LEFT JOIN SCHREG_REGD_GDAT T4 ");
            stb.append("          ON T2.YEAR = T4.YEAR ");
            stb.append("          AND T2.GRADE = T4.GRADE ");
            stb.append(" WHERE ");
            stb.append("    T2.YEAR = '" + param._year + "' ");
            stb.append("    AND T2.SEMESTER = '" + param._semester + "' ");
            stb.append("    AND MONTH IS NOT NULL ");
            stb.append("    AND MONTH = '08' ");
            stb.append(" GROUP BY ");
            stb.append("   T4.SCHOOL_KIND, ");
            stb.append("   T2.GRADE, ");
            stb.append("   T2.HR_CLASS, ");
            stb.append("   T1.MONTH ");
        }
        stb.append(" ), STUCNTDAT AS ( ");
        stb.append(" SELECT  ");
        stb.append("   T1.GRADE, ");
        stb.append("   T1.HR_CLASS, ");
        stb.append("   COUNT(T1.SCHREGNO) AS STUCNT ");
        stb.append(" FROM SCHREG_REGD_DAT T1 ");
        stb.append("      LEFT JOIN SCHREG_BASE_MST T2 ");
        stb.append("        ON T1.SCHREGNO = T2.SCHREGNO ");
        stb.append("      LEFT JOIN SEMESTER_MST T3 ");
        stb.append("        ON T3.YEAR = T1.YEAR ");
        stb.append("        AND T3.SEMESTER = T1.SEMESTER ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR = '" + param._year + "' ");
        if (!getoneyearflg) {
            stb.append("   AND T1.SEMESTER = '" + param._semester + "' ");
        }
        stb.append("   AND (T2.GRD_DIV IS NULL OR (T2.GRD_DIV IS NOT NULL AND T3.SDATE < T2.GRD_DATE )) ");
        stb.append(" GROUP BY ");
        stb.append("   T1.GRADE, ");
        stb.append("   T1.HR_CLASS ");
        stb.append(" ORDER BY ");
        stb.append("   T1.GRADE, ");
        stb.append("   T1.HR_CLASS ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("   TB1.SCHOOL_KIND, ");
        stb.append("   TB1.GRADE, ");
        stb.append("   TB1.HR_CLASS, ");
        stb.append("   TB1.MONTH, ");
        stb.append("   T8.STUCNT, ");
        stb.append("   SUM(TB1.SICK + FS1.SICK) AS SICK, ");
        stb.append("   SUM(TB1.LATE + FS1.LATE) AS LATE, ");
        stb.append("   SUM(TB1.EARLY + FS1.EARLY) AS EARLY ");
        stb.append(" FROM ");
        stb.append("   TBLBASEDAT TB1 ");
        stb.append("   LEFT JOIN FINDSUMDAT FS1 ");
        stb.append("     ON FS1.SCHOOL_KIND = TB1.SCHOOL_KIND ");
        stb.append("     AND FS1.GRADE = TB1.GRADE ");
        stb.append("     AND FS1.HR_CLASS = TB1.HR_CLASS ");
        stb.append("     AND FS1.MONTH = TB1.MONTH ");
        stb.append("   LEFT JOIN STUCNTDAT T8 ");
        stb.append("     ON T8.GRADE = TB1.GRADE ");
        stb.append("     AND T8.HR_CLASS = TB1.HR_CLASS ");
        stb.append(" WHERE ");
        stb.append("   TB1.SCHOOL_KIND IS NOT NULL ");
        stb.append(" GROUP BY ");
        stb.append("   TB1.SCHOOL_KIND, ");
        stb.append("   TB1.MONTH, ");
        stb.append("   TB1.GRADE, ");
        stb.append("   TB1.HR_CLASS, ");
        stb.append("   T8.STUCNT ");
        stb.append(" ORDER BY ");
        stb.append("   TB1.SCHOOL_KIND, ");
        stb.append("   TB1.GRADE DESC, ");
        stb.append("   TB1.HR_CLASS, ");
        stb.append("   TB1.MONTH ");
        return stb.toString();
    }

    /**
     *  印刷処理 メイン出力
     *
     */
    private void printTotalAttendanceInfo(final Vrw32alp svf, final Param param, final int[] ttlstudentnumcnt, final AttendanceCount[][] totalAttend, final String hjflg, final List csvlist) {
        final int kindttlid = "H".equals(hjflg) ? 1 : 2;
        csvBufCtl linebuf = new csvBufCtl("2".equals(param._outputSelect) ? false : true);
        int lpsize = "2".equals(param._outputSelect) ? _yearcolsize : _monthcolsize;

        //学年TOTAL
    	bufOut(svf, "TOTAL_HR_NUM" + kindttlid, String.valueOf(ttlstudentnumcnt[kindttlid - 1]), linebuf, csvlist);
        for (int tcnt = 1; tcnt <= lpsize;tcnt++) {
        	bufOut(svf, "TOTAL_NOTICE" + kindttlid + "_" + tcnt, String.valueOf(totalAttend[kindttlid - 1][tcnt - 1]._sick), linebuf, csvlist);
        	bufOut(svf, "TOTAL_MORNING" + kindttlid + "_" + tcnt, String.valueOf(totalAttend[kindttlid - 1][tcnt - 1]._early), linebuf, csvlist);
        	bufOut(svf, "TOTAL_LATE" + kindttlid + "_" + tcnt, String.valueOf(totalAttend[kindttlid - 1][tcnt - 1]._late), linebuf, csvlist);
        }
        if (csvlist != null) {
        	linebuf.setTotalGradeStr(param, hjflg);
        	csvlist.add(linebuf.convCsvBuf());
        	linebuf.clrCsvBuf();
        }
    }

    /**
     *  印刷処理 メイン出力
     *
     */
    private void svfPrintGrade(final Vrw32alp svf, final Param param, final GradeCls grade, final List hrClasses, final int[] ttlstudentnumcnt, final AttendanceCount[][] totalAttend, final List csvlist) {

        final DecimalFormat df2 = new DecimalFormat("00");
        csvBufCtl linebuf = new csvBufCtl(true);

        int clscnt = 0;
        AttendanceCount[] totalAttendMonth = new AttendanceCount[_monthcolsize];
        for (int i = 0;i < _monthcolsize;i++) {
        	totalAttendMonth[i] = new AttendanceCount();
        }
        int numtotal = 0;
        final int kindttlid = "H".equals(grade._schkind) ? 0 : 1;
        int monthrowsize = "H".equals(grade._schkind) ? _grade_h_classsize : _grade_j_classsize;
        for (final Iterator it = hrClasses.iterator(); it.hasNext();) {
            final HrClass hrClass = (HrClass) it.next();
            if (clscnt >= monthrowsize - 1) continue;

            Map outputdata = hrClass._hrClassAttendMonthMap;
            Set keyobj = outputdata.keySet();
        	bufOutn(svf, "HR_NAME" + (String)_gradefieldmap.get(grade._grade), clscnt + 1, hrClass._hrname, linebuf, csvlist);
        	bufOutn(svf, "HR_NUM" + (String)_gradefieldmap.get(grade._grade), clscnt + 1, String.valueOf(hrClass._studentcnt), linebuf, csvlist);
            numtotal += hrClass._studentcnt;
            ttlstudentnumcnt[kindttlid] += hrClass._studentcnt;

            AttendanceCount settotalwk = new AttendanceCount();
            int titlecnt = _monthcolsize - 1;
            for (int cnt = 1; cnt <= param._monthList.size();cnt++) {
            	if (cnt > titlecnt) continue;
            	String setttlmonth = (String)param._monthList.get(cnt - 1);
                if (keyobj.contains(setttlmonth)) {
                	AttendanceCount setwk = (AttendanceCount)outputdata.get(setttlmonth);
                	bufOutn(svf, "NOTICE" + (String)_gradefieldmap.get(grade._grade) + "_" + cnt, clscnt + 1, String.valueOf(setwk._sick), linebuf, csvlist);
                	bufOutn(svf, "MORNING" + (String)_gradefieldmap.get(grade._grade) + "_" + cnt, clscnt + 1, String.valueOf(setwk._early), linebuf, csvlist);
                	bufOutn(svf, "LATE" + (String)_gradefieldmap.get(grade._grade) + "_" + cnt, clscnt + 1, String.valueOf(setwk._late), linebuf, csvlist);
                    settotalwk.addAttend(setwk);
                    totalAttendMonth[cnt - 1].addAttend(setwk);
                    totalAttend[kindttlid][cnt - 1].addAttend(setwk);
                } else {
                	bufOutn(svf, "NOTICE" + (String)_gradefieldmap.get(grade._grade) + "_" + cnt, clscnt + 1, "0", linebuf, csvlist);
                	bufOutn(svf, "MORNING" + (String)_gradefieldmap.get(grade._grade) + "_" + cnt, clscnt + 1, "0", linebuf, csvlist);
                	bufOutn(svf, "LATE" + (String)_gradefieldmap.get(grade._grade) + "_" + cnt, clscnt + 1, "0", linebuf, csvlist);
                }
            }

            //合計
            int summid = _monthcolsize;
        	bufOutn(svf, "NOTICE" + (String)_gradefieldmap.get(grade._grade) + "_" + summid, clscnt + 1, String.valueOf(settotalwk._sick), linebuf, csvlist);
        	bufOutn(svf, "MORNING" + (String)_gradefieldmap.get(grade._grade) + "_" + summid, clscnt + 1, String.valueOf(settotalwk._early), linebuf, csvlist);
        	bufOutn(svf, "LATE" + (String)_gradefieldmap.get(grade._grade) + "_" + summid, clscnt + 1, String.valueOf(settotalwk._late), linebuf, csvlist);
            totalAttendMonth[summid - 1].addAttend(settotalwk);
            totalAttend[kindttlid][summid - 1].addAttend(settotalwk);

            if (hrClass._AttendStudentList != null) {
                List earlylst = (List)hrClass._AttendStudentList.get("EARLY");
                if (earlylst.size() > 0) {
            	    String setstr = "";
            	    for (int lcnt = 0;lcnt < earlylst.size();lcnt++) {
            		    setstr += ("".equals(setstr) ? "" : "、") + (String)earlylst.get(lcnt);
            	    }
                    String NNfieldname = KNJ_EditEdit.getMS932ByteLength(setstr) <= 70 ? "1" : "2";
                	bufOutn(svf, "NOTICE_NAME" + (String)_gradefieldmap.get(grade._grade) + "_" + NNfieldname, clscnt + 1, setstr, linebuf, csvlist);
                }

                List sicklst = (List)hrClass._AttendStudentList.get("SICK");
                if (sicklst.size() > 0) {
            	    String setstr = "";
            	    for (int lcnt = 0;lcnt < sicklst.size();lcnt++) {
            		    setstr += ("".equals(setstr) ? "" : "、") + (String)sicklst.get(lcnt);
            	    }
                    String MNfieldname = KNJ_EditEdit.getMS932ByteLength(setstr) <= 70 ? "1" : "2";
                	bufOutn(svf, "MORNING_NAME" + (String)_gradefieldmap.get(grade._grade) + "_" + MNfieldname, clscnt + 1, setstr, linebuf, csvlist);
                }
            }

            // 改行
            if (csvlist != null) {
            	//先頭に学年文字列を設定する。
            	linebuf.setGradeStr(param, grade._schkind, grade._grade);
            	csvlist.add(linebuf.convCsvBuf());
            	linebuf.clrCsvBuf();
            }
            clscnt++;
        }

        //学年TOTAL
    	int out_jh_line = "H".equals(grade._schkind) ? _grade_h_classsize : _grade_j_classsize;
        int ttlid = monthrowsize;
    	bufOutn(svf, "HR_NAME" + (String)_gradefieldmap.get(grade._grade), ttlid, "計", linebuf, csvlist);
    	bufOutn(svf, "HR_NUM" + (String)_gradefieldmap.get(grade._grade), ttlid, String.valueOf(numtotal), linebuf, csvlist);
        for (int tcnt = 1; tcnt <= _monthcolsize;tcnt++) {
        	bufOutn(svf, "NOTICE" + (String)_gradefieldmap.get(grade._grade) + "_" + tcnt, out_jh_line, String.valueOf(totalAttendMonth[tcnt - 1]._sick), linebuf, csvlist);
        	bufOutn(svf, "MORNING" + (String)_gradefieldmap.get(grade._grade) + "_" + tcnt, out_jh_line, String.valueOf(totalAttendMonth[tcnt - 1]._early), linebuf, csvlist);
        	bufOutn(svf, "LATE" + (String)_gradefieldmap.get(grade._grade) + "_" + tcnt, out_jh_line, String.valueOf(totalAttendMonth[tcnt - 1]._late), linebuf, csvlist);
        }
        if (csvlist != null) {
        	linebuf.setGradeStr(param, grade._schkind, grade._grade);
        	csvlist.add(linebuf.convCsvBuf());
        	linebuf.clrCsvBuf();
        }
    }

    private void bufOut(final Vrw32alp svf, final String field, final String val, final csvBufCtl linebuf, final List csvlist) {
    	if (csvlist == null) {
            svf.VrsOut(field, val);
    	} else {
    		linebuf.setCsvBuf(field, val);
    	}
    }

    private void bufOutn(final Vrw32alp svf, final String field, final int line, final String val, final csvBufCtl linebuf, final List csvlist) {
    	if (csvlist == null) {
            svf.VrsOutn(field, line, val);
    	} else {
    		linebuf.setCsvBuf(field, val);
    	}
    }

    /**
     *  印刷処理 メイン出力
     *
     */
    private void svfPrintGrade2(final Vrw32alp svf, final Param param, final GradeCls grade, final List hrClasses, final int[] ttlstudentnumcnt, final AttendanceCount[][] totalAttend, final List csvlist) {

        final DecimalFormat df2 = new DecimalFormat("00");
        csvBufCtl linebuf = new csvBufCtl(false);

        int clscnt = 0;
        AttendanceCount[] totalAttendMonth = new AttendanceCount[_yearcolsize];
        for (int i = 0;i < _yearcolsize;i++) {
        	totalAttendMonth[i] = new AttendanceCount();
        }
        int numtotal = 0;
        final int kindttlid = "H".equals(grade._schkind) ? 0 : 1;
        int monthrowsize = "H".equals(grade._schkind) ? _grade_h_classsize : _grade_j_classsize;

        for (final Iterator it = hrClasses.iterator(); it.hasNext();) {
            final HrClass hrClass = (HrClass) it.next();
            if (clscnt >= monthrowsize - 1) continue;

            Map outputdata = hrClass._hrClassAttendMonthMap;
            Set keyobj = outputdata.keySet();

        	bufOutn(svf, "HR_NAME" + (String)_gradefieldmap.get(grade._grade), clscnt, hrClass._hrname, linebuf, csvlist);
        	bufOutn(svf, "HR_NUM" + (String)_gradefieldmap.get(grade._grade), clscnt, String.valueOf(hrClass._studentcnt), linebuf, csvlist);
            numtotal += hrClass._studentcnt;
            ttlstudentnumcnt[kindttlid] += hrClass._studentcnt;

            AttendanceCount settotalwk = new AttendanceCount();
            int titlecnt = _yearcolsize - 1;
            for (int cnt = 1; cnt <= param._monthList.size();cnt++) {
            	if (cnt > titlecnt) continue;
            	String setttlmonth = (String)param._monthList.get(cnt - 1);
                if (keyobj.contains(setttlmonth)) {
                	AttendanceCount setwk = (AttendanceCount)outputdata.get(setttlmonth);
                    bufOutn(svf, "NOTICE" + (String)_gradefieldmap.get(grade._grade) + "_" + cnt, clscnt, String.valueOf(setwk._sick), linebuf, csvlist);
                    bufOutn(svf, "MORNING" + (String)_gradefieldmap.get(grade._grade) + "_" + cnt, clscnt, String.valueOf(setwk._early), linebuf, csvlist);
                    bufOutn(svf, "LATE" + (String)_gradefieldmap.get(grade._grade) + "_" + cnt, clscnt, String.valueOf(setwk._late), linebuf, csvlist);
                    settotalwk.addAttend(setwk);
                    totalAttendMonth[cnt - 1].addAttend(setwk);
                    totalAttend[kindttlid][cnt - 1].addAttend(setwk);
                } else {
                    bufOutn(svf, "NOTICE" + (String)_gradefieldmap.get(grade._grade) + "_" + cnt, clscnt, "0", linebuf, csvlist);
                    bufOutn(svf, "MORNING" + (String)_gradefieldmap.get(grade._grade) + "_" + cnt, clscnt, "0", linebuf, csvlist);
                    bufOutn(svf, "LATE" + (String)_gradefieldmap.get(grade._grade) + "_" + cnt, clscnt, "0", linebuf, csvlist);
                }
            }

            //合計
            int summid = _yearcolsize;
            bufOutn(svf, "NOTICE" + (String)_gradefieldmap.get(grade._grade) + "_" + summid, clscnt, String.valueOf(settotalwk._sick), linebuf, csvlist);
            bufOutn(svf, "MORNING" + (String)_gradefieldmap.get(grade._grade) + "_" + summid, clscnt, String.valueOf(settotalwk._early), linebuf, csvlist);
            bufOutn(svf, "LATE" + (String)_gradefieldmap.get(grade._grade) + "_" + summid, clscnt, String.valueOf(settotalwk._late), linebuf, csvlist);
            totalAttendMonth[summid - 1].addAttend(settotalwk);
            totalAttend[kindttlid][summid - 1].addAttend(settotalwk);

            // 改行
            if (csvlist != null) {
            	//先頭に学年文字列を設定する。
            	linebuf.setGradeStr(param, grade._schkind, grade._grade);
            	csvlist.add(linebuf.convCsvBuf());
            	linebuf.clrCsvBuf();
            }
            clscnt++;
        }

        //学年TOTAL
    	int out_jh_line = "H".equals(grade._schkind) ? _grade_h_classsize : _grade_j_classsize;
        int ttlid = monthrowsize;
    	bufOutn(svf, "HR_NAME" + (String)_gradefieldmap.get(grade._grade), ttlid, "計", linebuf, csvlist);
    	bufOutn(svf, "HR_NUM" + (String)_gradefieldmap.get(grade._grade), ttlid, String.valueOf(numtotal), linebuf, csvlist);
        for (int tcnt = 1; tcnt <= _yearcolsize;tcnt++) {
        	bufOutn(svf, "NOTICE" + (String)_gradefieldmap.get(grade._grade) + "_" + tcnt, out_jh_line, String.valueOf(totalAttendMonth[tcnt - 1]._sick), linebuf, csvlist);
        	bufOutn(svf, "MORNING" + (String)_gradefieldmap.get(grade._grade) + "_" + tcnt, out_jh_line, String.valueOf(totalAttendMonth[tcnt - 1]._early), linebuf, csvlist);
        	bufOutn(svf, "LATE" + (String)_gradefieldmap.get(grade._grade) + "_" + tcnt, out_jh_line, String.valueOf(totalAttendMonth[tcnt - 1]._late), linebuf, csvlist);
        }
        if (csvlist != null) {
        	linebuf.setGradeStr(param, grade._schkind, grade._grade);
        	csvlist.add(linebuf.convCsvBuf());
        	linebuf.clrCsvBuf();
        }
    }

    private static void load(final DB2UDB db2, final Param param, final String grade, final List hrClasses) {

        PreparedStatement ps = null;
        try {
        	//★欠席該当生徒情報を取得

            final String gradeSdate = (String) param._sDateMap.get(grade);
            param._attendParamMap2.put("grade", grade);
            final String sql = AttendAccumulate.getAttendSemesSql(
                    param._year,
                    param._semester,
                    null != gradeSdate && param._sDateMonth.compareTo(gradeSdate) < 0 ? gradeSdate : param._sDateMonth,
                    param._sdate2,
                    param._attendParamMap2
            );
        	//★欠席該当生徒情報を取得
            ps = db2.prepareStatement(sql);

            for (final Iterator it = hrClasses.iterator(); it.hasNext();) {
                final HrClass hrClass = (HrClass) it.next();

                int pi;
                pi = 0;
                param._sqlAttendance.setString(++pi, grade);
                param._sqlAttendance.setString(++pi, hrClass._hrClass);
//                hrClass._hrClassAttend = getHrClassAttend(param, param._sqlAttendance, hrClass, "HR_CLASS_ATTEND");
                pi = 0;
                ps.setString(++pi, hrClass._hrClass);
//                hrClass._hrClassAttendMonth = getHrClassAttend(param, ps, hrClass, "HR_CLASS_ATTEND_MONTH");
            }

        } catch (Exception ex) {
            log.error("svfPrintGrade exception!", ex);
        } finally {
            DbUtils.closeQuietly(ps);
            db2.commit();
        }
    }


//    private static void loadMonths(final DB2UDB db2, final Param param, final List grades, final Map gradeClassMap) {
//        PreparedStatement ps = null;
//        try {
//            final Calendar dcal = Calendar.getInstance();
//            dcal.setTime(Date.valueOf(param._date));
//            final DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//            final boolean isKindai = "KINDAI".equals(param._z010);
//
//            for (final Iterator itm = param._monthList.iterator(); itm.hasNext();) {
//                final String month = (String) itm.next();
//
//                final String nen = String.valueOf(Integer.parseInt(param._year) + (Integer.parseInt(month) <= 3 ? 1 : 0));
//                final String monthStartDate = nen + "-" + month + (isKindai ? "-02" : "-01");
//
//                final Calendar endCal = Calendar.getInstance();
//                endCal.setTime(Date.valueOf(monthStartDate));
//                endCal.add(Calendar.MONTH, 1);
//                endCal.add(Calendar.DAY_OF_MONTH, - 1);
//
//                final String endDate = sdf.format((dcal.before(endCal) ? dcal : endCal).getTime());
//                log.debug(" month = " + month + ", " + monthStartDate + " - " + endDate);
//
//                final String sql = AttendAccumulate.getAttendSemesSql(
//                        param._year,
//                        param._semester,
//                        monthStartDate,
//                        endDate,
//                        param._attendParamMapMonth
//                        );
//                ps = db2.prepareStatement(sql);
//
//                for(final Iterator it = grades.iterator(); it.hasNext();) {
//                    final String grade = (String) it.next();
//                    final List hrClasses = (List) gradeClassMap.get(grade);
//
//                    for (final Iterator hrit = hrClasses.iterator(); hrit.hasNext();) {
//                        final HrClass hrClass = (HrClass) hrit.next();
//
//                        int pi = 0;
//                        ps.setString(++pi, grade);
//                        ps.setString(++pi, hrClass._hrClass);
//                        hrClass._hrClassAttendMonthMap.put(month, getHrClassAttend(param, ps, hrClass, "HR_CLASS_ATTEND_MONTH"));
//                    }
//                }
//            }
//
//        } catch (Exception ex) {
//            log.error("svfPrintGrade exception!", ex);
//        } finally {
//            DbUtils.closeQuietly(ps);
//            db2.commit();
//        }
//    }

    private static String sqlStudentAttend(final Param param, HrClass classwk) {
        final StringBuffer stb = new StringBuffer();
        //1.一度指定期間内の生徒単位で出欠を月単位で集計(SUMBASEDAT)。
        //2.そのうえで、抽出対象条件に合う生徒を抽出して名前を紐づけ(FILTERDAT)。
        //3.対象生徒を月単位で集計。
    	stb.append(" WITH SUMBASEDAT AS ( ");
    	stb.append(" SELECT ");
    	stb.append("   T4.SCHOOL_KIND, ");
    	stb.append("   T2.GRADE, ");
    	stb.append("   T2.HR_CLASS, ");
    	stb.append("   T1.MONTH, ");
    	stb.append("   T1.SCHREGNO, ");
    	stb.append("   T6.NAME, ");
    	stb.append("   SUM(VALUE(T1.SICK, 0) + VALUE(T1.NOTICE,0) + VALUE(T1.NONOTICE, 0)) AS SICK, ");
    	stb.append("   SUM(T31.CNT) AS LATE, ");
    	stb.append("   SUM(T32.CNT) AS EARLY ");
    	stb.append("  FROM  ATTEND_SEMES_DAT T1 ");
    	stb.append("      LEFT JOIN ATTEND_SEMES_DETAIL_DAT T31 ");
    	stb.append("        ON T1.YEAR = T31.YEAR ");
    	stb.append("        AND T1.SEMESTER = T31.SEMESTER ");
    	stb.append("        AND T1.SCHREGNO = T31.SCHREGNO ");
    	stb.append("        AND T1.COPYCD = T31.COPYCD ");
    	stb.append("        AND T1.MONTH = T31.MONTH ");
    	stb.append("        AND T31.SEQ = '002' ");
    	stb.append("      LEFT JOIN ATTEND_SEMES_DETAIL_DAT T32 ");
    	stb.append("        ON T1.YEAR = T32.YEAR ");
    	stb.append("        AND T1.SEMESTER = T32.SEMESTER ");
    	stb.append("        AND T1.SCHREGNO = T32.SCHREGNO ");
    	stb.append("        AND T1.COPYCD = T32.COPYCD ");
    	stb.append("        AND T1.MONTH = T32.MONTH ");
    	stb.append("        AND T32.SEQ = '001' ");
    	stb.append("      LEFT JOIN SCHREG_REGD_DAT T2 ");
    	stb.append("          ON T1.YEAR = T2.YEAR ");
    	stb.append("          AND T1.SEMESTER = T2.SEMESTER ");
    	stb.append("          AND T1.SCHREGNO = T2.SCHREGNO ");
    	stb.append("      LEFT JOIN SCHREG_REGD_GDAT T4 ");
    	stb.append("          ON T2.YEAR = T4.YEAR ");
    	stb.append("          AND T2.GRADE = T4.GRADE ");
    	stb.append("      LEFT JOIN SCHREG_BASE_MST T6 ");
    	stb.append("          ON T1.SCHREGNO = T6.SCHREGNO ");
    	stb.append(" WHERE ");
    	stb.append("     T1.YEAR = '" + param._year + "' ");
    	stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
    	stb.append("     AND '" + param._sdate + "' <= (CASE WHEN T1.MONTH IN ('01', '02', '03') THEN '" + String.valueOf(Integer.parseInt(param._year) + 1) + "' ELSE T1.YEAR END || T1.MONTH || T1.APPOINTED_DAY) ");
    	stb.append("     AND (CASE WHEN T1.MONTH IN ('01', '02', '03') THEN '" + String.valueOf(Integer.parseInt(param._year) + 1) + "' ELSE T1.YEAR END || T1.MONTH || T1.APPOINTED_DAY) <= '" + param._edate + "' ");
    	
    	stb.append("     AND T2.GRADE IS NOT NULL ");
    	stb.append("     AND T2.GRADE = '" + classwk._grade + "' ");
    	stb.append("     AND T2.HR_CLASS = '" + classwk._hrClass + "' ");
    	stb.append(" GROUP BY ");
    	stb.append("   T4.SCHOOL_KIND, ");
    	stb.append("   T2.GRADE, ");
    	stb.append("   T2.HR_CLASS, ");
    	stb.append("   T1.MONTH, ");
    	stb.append("   T1.SCHREGNO, ");
    	stb.append("   T6.NAME ");
    	stb.append(" ),FILTERDAT AS ( ");
    	stb.append("  SELECT ");
    	stb.append("   SBD1.SCHOOL_KIND, ");
    	stb.append("   SBD1.GRADE, ");
    	stb.append("   SBD1.HR_CLASS, ");
    	stb.append("   SBD1.MONTH, ");
    	stb.append("   SBD1.SCHREGNO, ");
    	stb.append("   SBD1.NAME, ");
    	stb.append("   SBD1.SICK, ");
    	stb.append("   SBD1.LATE, ");
    	stb.append("   SBD1.EARLY, ");
    	stb.append("   CASE WHEN SBD1.SICK > 0 THEN 1 ELSE 0 END AS SICKCNT, ");
    	stb.append("   CASE WHEN SBD1.LATE > 0 THEN 1 ELSE 0 END AS LATECNT, ");
    	stb.append("   CASE WHEN SBD1.EARLY > 0 THEN 1 ELSE 0 END AS EARLYCNT ");
    	stb.append("  FROM  SUMBASEDAT SBD1 ");
    	stb.append("  WHERE ");
    	stb.append("         SBD1.SICK >= " + param._sickCnt + " ");
    	stb.append("         OR SBD1.EARLY >= " + param._earlyCnt + " ");
    	stb.append(" ) ");
    	stb.append(" SELECT ");
    	stb.append("   FD1.SCHOOL_KIND, ");
    	stb.append("   FD1.GRADE, ");
    	stb.append("   FD1.HR_CLASS, ");
    	stb.append("   FD1.SCHREGNO, ");
    	stb.append("   FD1.NAME, ");
    	stb.append("   SUM(FD1.SICK) AS SICK, ");
    	stb.append("   SUM(FD1.LATE) AS LATE, ");
    	stb.append("   SUM(FD1.EARLY) AS EARLY ");
    	stb.append(" FROM  FILTERDAT FD1 ");
    	stb.append(" GROUP BY ");
    	stb.append("   FD1.SCHOOL_KIND, ");
    	stb.append("   FD1.GRADE, ");
    	stb.append("   FD1.HR_CLASS, ");
    	stb.append("   FD1.SCHREGNO, ");
    	stb.append("   FD1.NAME ");
    	stb.append(" ORDER BY ");
    	stb.append("   FD1.SCHOOL_KIND, ");
    	stb.append("   FD1.GRADE DESC, ");
    	stb.append("   FD1.HR_CLASS ");
        return stb.toString();
    }

//    private static AttendanceCount getHrClassAttend(final Param param, PreparedStatement ps, final HrClass hrClass, final String key) throws SQLException {
//        ResultSet rs = null;
//        final AttendanceCount total = new AttendanceCount(false);
//        try {
//            rs = ps.executeQuery();
//            while(rs.next()) {
//                if (!"9".equals(rs.getString("SEMESTER"))) {
//                    continue;
//                }
//                if ("2".equals(param._outputSelect)) {
//                    final Map schregMap = (Map) hrClass._studentMap.get(rs.getString("SCHREGNO"));
//                    if (null == schregMap) {
//                        log.info(" not in " + hrClass._hrClass + ", schregno = " + rs.getString("SCHREGNO"));
//                        continue;
//                    }
//                    final AttendanceCount attendanceStudent = new AttendanceCount(false);
//                    attendanceStudent.addAttend(rs);
//                    schregMap.put(key, attendanceStudent);
//                }
//                total.addAttend(rs);
//            }
//        } finally {
//            DbUtils.closeQuietly(rs);
//        }
//        return total;
//    }

//    /** svfへ出力する
//     * @param svf svf
//     * @param isYear 年計か
//     */
//    private void printAttendanceCount(final Vrw32alp svf, final Param param, final boolean isYear, final AttendanceCount a, final int line) {
//        // 出席率 = 100.0 * 出席日数 / 授業日数
//        if (null == a) {
//            return;
//        }
//        final String percentage;
//        if (a._mlesson == 0) {
//            percentage = "0.0";
//        } else {
//            percentage = new BigDecimal(100 * a._attend).divide(new BigDecimal(a._mlesson), 1, BigDecimal.ROUND_HALF_UP).toString();
//        }
//        a._attendancePercentage = percentage;
//
//        String totalStr = isYear ? "TOTAL_" : "";
//        if ("2".equals(param._outputSelect)) {
//            if (!a._isTotal) {
//                svf.VrsOutn(totalStr + "LESSON", line, String.valueOf(a._displayLesson));
//            }
//            svf.VrsOutn(totalStr + "SUSPEND", line, String.valueOf(a._suspend + a._mourning));
//            svf.VrsOutn(totalStr + "ABROAD", line, String.valueOf(a._abroad));
//            svf.VrsOutn(totalStr + "REQ_ATTEND", line, String.valueOf(a._mlesson));
//            svf.VrsOutn(totalStr + "SICK", line, String.valueOf(a._sick));
//            svf.VrsOutn(totalStr + "ATTEND", line, String.valueOf(a._attend));
//            svf.VrsOutn(totalStr + "LATE", line, String.valueOf(a._late));
//            svf.VrsOutn(totalStr + "EARLY", line, String.valueOf(a._early));
//        } else {
//            if (!a._isTotal) {
//                svf.VrsOut(totalStr + "LESSON", String.valueOf(a._displayLesson));
//            }
//            svf.VrsOut(totalStr + "MOURNING", String.valueOf(a._mourning));
//            svf.VrsOut(totalStr + "SUSPEND", String.valueOf(a._suspend));
//            svf.VrsOut(totalStr + "ATTEND", String.valueOf(a._attend));
//            svf.VrsOut(totalStr + "SICK", String.valueOf(a._sick));
//            svf.VrsOut(totalStr + "LATE", String.valueOf(a._late));
//            svf.VrsOut(totalStr + "EARLY", String.valueOf(a._early));
//            svf.VrsOut(totalStr + "ABSENT", String.valueOf(a._absent));
//            svf.VrsOut(totalStr + "PERCENTAGE", a._attendancePercentage);
//        }
//    }

    private static class GradeCls {
    	final String _grade;
    	final String _schkind;
    	GradeCls (final String grade, final String schkind) {
    		_grade = grade;
    		_schkind = schkind;
    	}
    }

    private class Param {
        final String _year;
        final String _semester;
        String _sDateMonth;
        final String _loginDate;
//        final String _schoolKind;
        final String _outputSelect;
        final List _monthList = new ArrayList();
//        final String _z010;

        final String _cmd;
        final int _sickCnt;
        final int _earlyCnt;
        final int _smonth;
        final String _sdate;
        final String _sdate2;
        final int _emonth;
        final String _edate;
        final String _edate2;

        private Map _sDateMap;

        private PreparedStatement _sqlAttendance;

        private Map _attendParamMap;
        private Map _attendParamMap2;
//        private Map _attendParamMapMonth;

        private String _schKindinState;

        private String _fName;

        private String[][] _schKindStr;
        private Map _gradecdMap;


        Param(final HttpServletRequest request, final DB2UDB db2) {
            _year = request.getParameter("YEAR");
            _semester  = request.getParameter("SETSEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
//            _schoolKind = request.getParameter("SCHOOL_KIND");

            _schKindinState = "('H', 'J')";
            _cmd = request.getParameter("cmd");
            final String sickstr = StringUtils.defaultString(request.getParameter("SICK"), "0");
            _sickCnt = Integer.parseInt("".equals(sickstr) ? "0" : sickstr);
            final String earlyCnt = StringUtils.defaultString(request.getParameter("CHOREIKETSU"), "0");
            _earlyCnt = Integer.parseInt("".equals(earlyCnt) ? "0" : earlyCnt);
            _fName = "outcsv";

            _smonth = Integer.parseInt(request.getParameter("SMONTH"));
            _emonth = Integer.parseInt(request.getParameter("EMONTH"));

            _schKindStr = new String[2][2];

            final DecimalFormat df2 = new DecimalFormat("00");
            Calendar cal = Calendar.getInstance();
//            cal.setTime(Date.valueOf(_date));
//            month = cal.get(Calendar.MONTH) + 1;

            final String setYear = _smonth < 4 ? String.valueOf(Integer.parseInt(_year) + 1) : _year;
            cal.clear();
			cal.set(Integer.parseInt(setYear), _emonth - 1, 1);
            _sdate = setYear + df2.format(_smonth) + "01";
            _sdate2 = setYear + "/" + df2.format(_smonth) + "/" + "01";
            _edate = setYear + df2.format(_emonth) + cal.getActualMaximum(Calendar.DATE);
            _edate2 = setYear + "/" + df2.format(_emonth) + "/" + cal.getActualMaximum(Calendar.DATE);

            _schKindStr[0][0] = getA023(db2, _year, "1");
            _schKindStr[0][1] = StringUtils.substring(_schKindStr[0][0], 0, 1);
            _schKindStr[1][0] = getA023(db2, _year, "2");
            _schKindStr[1][1] = StringUtils.substring(_schKindStr[1][0], 0, 1);
            _gradecdMap = new HashMap();
            setGradeCdMap(db2);
            //★

            _outputSelect = request.getParameter("OUTPUT_SELECT");
//            _z010 = getZ010(db2, _year);

            _sDateMap = getYearStartDateMap(db2);

            final int year = cal.get(Calendar.YEAR);
            DecimalFormat df4 = new DecimalFormat("0000");
            _sDateMonth = df4.format(year) + "-" + df2.format(_smonth) + "-01";

            if ("2".equals(_outputSelect)) {
                for (int m = 4; m <= 15; m++) {
                    //8月は除外(9月とセットで1列として扱う)。
                    if (m != 8) {
                        _monthList.add(df2.format(m - (m > 12 ? 12 : 0)));
                    }
                }
            } else {
                if (_smonth + (_smonth <= 3 ? 12 : 0) <= _emonth + (_emonth <= 3 ? 12 : 0)) {
                    for (int m = _smonth + (_smonth <= 3 ? 12 : 0); m <= _emonth + (_emonth <= 3 ? 12 : 0); m++) {
                        //8月は例外(9月とセットで1列として扱う)。
                        if (m != 8) {
                            _monthList.add(df2.format(m - (m > 12 ? 12 : 0)));
                        } else if (m == 8 && _emonth == 8) { //8月まで指定の場合、8月分を「9月を出力予定」として登録
                            _monthList.add(df2.format(9));
                        }
                    }
                }
            }
            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", "?");
            _attendParamMap.put("hrClass", "?");

            _attendParamMap2 = new HashMap();
            _attendParamMap2.put("DB2UDB", db2);
            _attendParamMap2.put("HttpServletRequest", request);
            _attendParamMap2.put("hrClass", "?");

            try {
                String sql;

                // 年間
                sql = AttendAccumulate.getAttendSemesSql(
                        _year,
                        _semester,
                        null,
                        _sdate2,
                        _attendParamMap
                        );
                _sqlAttendance = db2.prepareStatement(sql);

            } catch (SQLException ex) {
                log.error("parameter load exception!", ex);
            }
        }

        private Map getYearStartDateMap(DB2UDB db2) {
            final Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            String sql = "SELECT GRADE, SDATE FROM V_SEMESTER_GRADE_MST WHERE SEMESTER = '9' AND YEAR = '" + _year + "' ";
            //log.debug("datamapsql;" + sql);
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn.put(rs.getString("GRADE"), rs.getString("SDATE"));
                }
            } catch (SQLException ex) {
                log.error("getYearStartDate exception!" , ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }


//        private String getZ010(final DB2UDB db2, final String year) {
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            String val = null;
//            try {
//                ps = db2.prepareStatement(sqlNameMst(year, "Z010", "00"));
//                rs = ps.executeQuery();
//                if (rs.next()) {
//                    val = rs.getString("NAME1");
//                }
//            } catch (Exception e) {
//                log.fatal("exception!", e);
//            } finally {
//                db2.commit();
//                DbUtils.closeQuietly(null, ps, rs);
//            }
//            return val;
//        }

        private String getA023(final DB2UDB db2, final String year, final String namecd2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String val = null;
            try {
                ps = db2.prepareStatement(sqlNameMst(year, "A023", namecd2));
                rs = ps.executeQuery();
                if (rs.next()) {
                    val = rs.getString("ABBV1");
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return val;
        }

        private void setGradeCdMap(final DB2UDB db2) {
        	String sql = "select DISTINCT GRADE, GRADE_CD from SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND SCHOOL_KIND IN ('H', 'J')";
            PreparedStatement ps = null;
            ResultSet rs = null;
            String keyval = null;
            String val = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                   keyval = rs.getString("GRADE");
                   val = rs.getString("GRADE_CD");
                    _gradecdMap.put(keyval, val);
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }
        private String sqlNameMst(final String year, final String namecd1, final String namecd2) {
            final String sql = " SELECT "
                + "     * "
                + " FROM "
                + "     V_NAME_MST "
                + " WHERE "
                + "         YEAR = '" + year + "' "
                + "     AND NAMECD1 = '" + namecd1 + "' "
                + "     AND NAMECD2 = '" + namecd2 + "'";
            return sql;
        }
    }

    /** 出欠カウント */
    private static class AttendanceCount {
        /** 欠席日数 */
        private int _sick;
        /** 遅刻日数 */
        private int _late;
        /** 早退日数 */
        private int _early;

    	/** 表示する授業日数(各クラスごとの生徒の授業日数の最大値) */
//        private int _displayLesson;
        /** 授業日数 */
//        private int _lesson;
        /** 出席すべき日数 */
//        private int _mlesson;
        /** 出席日数 */
//        private int _attend;
        /** 忌引日数 */
//        private int _mourning;
        /** 出停日数 */
//        private int _suspend;
        /** 公欠日数 */
//        private int _absent;
        /** 留学日数 */
//        private int _abroad;
        /** 休学日数 */
//        private int _offdays;
        /** 出欠率 */
//        private String _attendancePercentage;

        public AttendanceCount() {
        	_sick = 0;
        	_early = 0;
        	_late = 0;
        }

        public String toString() {
            DecimalFormat df5 = new DecimalFormat("00000");
            return
//            "LESSON=" + df5.format(_lesson)
//            + ", MLESSON=" + df5.format(_mlesson)
//            + ", ATTEND=" + df5.format(_attend)
            ", SICK=" + df5.format(_sick)
            + ", LATE=" + df5.format(_late )
            + ", EARLY=" + df5.format(_early);
//            + ", MOURNING=" + df5.format(_mourning)
//            + ", SUSPEND=" + df5.format(_suspend)
//            + ", ABSENT=" + df5.format(_absent)
//            + ", ABROAD=" + df5.format(_abroad)
//            + ", OFFDAYS=" + df5.format(_offdays)
//            + ", percentage=" + _attendancePercentage;
        }

        public void addAttend(ResultSet rs) throws SQLException {
            int lesson = rs.getInt("LESSON"); // 授業日数
            int offdays = rs.getInt("OFFDAYS"); // 休学日数
            int sick = rs.getInt("SICK"); // 欠席日数
            int suspend = rs.getInt("SUSPEND");
            suspend += rs.getInt("VIRUS");
            suspend += rs.getInt("KOUDOME");
            int special = rs.getInt("MOURNING") + suspend; // 特別欠席
            int mlesson = lesson - special; // 出席すべき日数
//            _displayLesson = Math.max(lesson, _displayLesson);
//            _lesson += lesson;
//            _mlesson += mlesson;
            _sick += sick;
//            _attend += mlesson - sick; // 出席日数 = 出席すべき日数 - 欠席日数
            _late += rs.getInt("LATE");
            _early += rs.getInt("EARLY");
//            _mourning += rs.getInt("MOURNING");
//            _suspend += suspend;
//            _absent += rs.getInt("ABSENT");
//            _abroad += rs.getInt("TRANSFER_DATE");
//            _offdays += offdays;
        }

        /** 出欠カウントを追加する */
        public void addAttend(AttendanceCount ac) {
            if (null == ac) {
                return;
            }
//            _lesson += ac._lesson;
//            _mlesson += ac._mlesson;
//            _attend += ac._attend;
            _sick += ac._sick;
            _late += ac._late;
            _early += ac._early;
//            _mourning += ac._mourning;
//            _suspend += ac._suspend;
//            _absent += ac._absent;
//            _offdays += ac._offdays;
        }
    }

    private static class csvBufCtl {
    	private static final int _outputgradecnt = 6;
    	private static final int _monthcsvbuflen = 20;
    	private static final int _yearcsvbuflen = 39;
    	private String[] _csvbuf;
    	private int _len;
    	Map _csvplaceidx;

    	csvBufCtl(boolean monthflg) {
    		_csvplaceidx = new HashMap();
    		//CSV出力位置を登録する。
    		//!!! 注意 !!! フォームの修正を行った場合、こちらの修正も合わせて行う事。
    		if (monthflg) {
    			_len = _monthcsvbuflen;
    			_csvbuf = new String[_monthcsvbuflen];
    			int cnt = 0;

        		//タイトル部分の登録
    			for (cnt = 1;cnt <= 3;cnt++) { //横(情報の種類)
                    for (int wkcnt = 1;wkcnt <= _monthcolsize - 1;wkcnt++) { //横(1情報の設定列数-1 ※"計"にはフィールド名なし。)
                		_csvplaceidx.put("MONTH"+cnt+"_" + wkcnt, String.valueOf(5 * (cnt - 1) + wkcnt + 2));
                		//「8・9月」用フィールド名
              		    _csvplaceidx.put("MONTH"+cnt+"_" + wkcnt + "_2", String.valueOf(5 * (cnt - 1) + wkcnt + 2));
                    }
    			}
        		//データ部分の登録(データ部)
        		for (cnt = 1; cnt <= _outputgradecnt;cnt++) { //縦(学年数)
                    _csvplaceidx.put("HR_NAME" + cnt, "1");
                    _csvplaceidx.put("HR_NUM" + cnt, "2");
                    for (int wkcnt = 1;wkcnt <= _monthcolsize;wkcnt++) { //横(1情報の設定列数)
                		_csvplaceidx.put("NOTICE" + cnt + "_" + wkcnt, String.valueOf(wkcnt + 2));
                		_csvplaceidx.put("MORNING" + cnt + "_" + wkcnt, String.valueOf(wkcnt + 7));
                		_csvplaceidx.put("LATE" + cnt + "_" + wkcnt, String.valueOf(wkcnt + 12));
                    }
            		_csvplaceidx.put("NOTICE_NAME" + cnt + "_1", "18");
            		_csvplaceidx.put("NOTICE_NAME" + cnt + "_2", "18");
            		_csvplaceidx.put("MORNING_NAME" + cnt + "_1", "19");
            		_csvplaceidx.put("MORNING_NAME" + cnt + "_2", "19");
        		}
        		//データ部分の登録(合計部)
        		for (cnt = 1; cnt <= 2;cnt++) { //縦(中学/高校で1つづつ)
                    _csvplaceidx.put("TOTAL_HR_NUM" + cnt, "2");
                    for (int wkcnt = 1;wkcnt <= _monthcolsize;wkcnt++) { //横(1情報の設定列数)
                		_csvplaceidx.put("TOTAL_NOTICE" + cnt + "_" + wkcnt, String.valueOf(wkcnt + 2));
                		_csvplaceidx.put("TOTAL_MORNING" + cnt + "_" + wkcnt, String.valueOf(wkcnt + 7));
                		_csvplaceidx.put("TOTAL_LATE" + cnt + "_" + wkcnt, String.valueOf(wkcnt + 12));
                    }
        		}
    		} else {
    			_len = _yearcsvbuflen;
    			_csvbuf = new String[_yearcsvbuflen];
    			int cnt = 0;

        		//タイトル部分の登録
        		for (cnt = 1; cnt <= 3;cnt++) { //横(情報の種類)
                    for (int wkcnt = 1;wkcnt <= _yearcolsize - 1;wkcnt++) {  //横(1情報の設定列数-1 ※"計"にはフィールド名なし。)
                        _csvplaceidx.put("MONTH" + cnt + "_" + wkcnt, String.valueOf(12 * (cnt - 1) + wkcnt + 2));
                		//「8・9月」用フィールド名
                        _csvplaceidx.put("MONTH" + cnt + "_" + wkcnt + "_2", String.valueOf(12 * (cnt - 1) + wkcnt + 2));
                    }
        		}
        		//データ部分の登録(データ部)
        		for (cnt = 1; cnt <= _outputgradecnt;cnt++) { //縦(学年数)
                    _csvplaceidx.put("HR_NAME" + cnt, "1");
                    _csvplaceidx.put("HR_NUM" + cnt, "2");
            		//データ部分の登録(データ部)
                    for (int wkcnt = 1;wkcnt <= _yearcolsize;wkcnt++) {  //横
                		_csvplaceidx.put("NOTICE" + cnt + "_" + wkcnt, String.valueOf(2 + wkcnt));
                		_csvplaceidx.put("MORNING" + cnt + "_" + wkcnt, String.valueOf(14 + wkcnt));
                		_csvplaceidx.put("LATE" + cnt + "_" + wkcnt, String.valueOf(26 + wkcnt));
                    }
        		}
        		//データ部分の登録(合計部)
        		for (cnt = 1; cnt <= 2;cnt++) { //縦(中学/高校で1つづつ)
                    _csvplaceidx.put("TOTAL_HR_NUM" + cnt, "2");
                    for (int wkcnt = 1;wkcnt <= _yearcolsize;wkcnt++) { //横
                		_csvplaceidx.put("TOTAL_NOTICE" + cnt + "_" + wkcnt, String.valueOf(2 + wkcnt));
                		_csvplaceidx.put("TOTAL_MORNING" + cnt + "_" + wkcnt, String.valueOf(14 + wkcnt));
                		_csvplaceidx.put("TOTAL_LATE" + cnt + "_" + wkcnt, String.valueOf(26 + wkcnt));
                    }
        		}
    		}
    		clrCsvBuf();
    	}
        void clrCsvBuf() {
        	for(int cnt = 0;cnt < _len;cnt++) {
        		_csvbuf[cnt] = "";
        	}

        }

        void setGradeStr(final Param param, final String schkind, final String grade) {
        	if ("H".equals(schkind)) {
        		_csvbuf[0] = param._schKindStr[1][1] + Integer.parseInt((String)param._gradecdMap.get(grade));
        	} else {
        		_csvbuf[0] = param._schKindStr[0][1] + Integer.parseInt((String)param._gradecdMap.get(grade));
        	}
        }

        void setTotalGradeStr(final Param param, final String schkind) {
        	if ("H".equals(schkind)) {
        		_csvbuf[0] = param._schKindStr[1][0] + " 計";
        	} else {
        		_csvbuf[0] = param._schKindStr[0][0] + " 計";
        	}
        }

        List convCsvBuf() {
        	List retlst = new ArrayList();
        	retlst.addAll(Arrays.asList(_csvbuf));
        	return retlst;
        }
        void setCsvBuf(final String idxstr, final String setval) {
log.debug("idx:" + idxstr + "_val:" + setval + "_subidx:" + (String)_csvplaceidx.get(idxstr));
        	_csvbuf[Integer.parseInt((String)_csvplaceidx.get(idxstr))] = setval;
        }

    }
    private static class HrClass {
        final String _grade;
        final String _hrClass;
        final String _hrname;

        /** 対象者 */
        private Map _AttendStudentList;
        Map _hrClassAttendMonthMap = new HashMap();
    	/** 生徒数 */
        private int _studentcnt;
        HrClass(final String grade,
                final String hrClass,
                final String hrname
                ) {
            _grade = grade;
            _hrClass = hrClass;
            _hrname = hrname;
            _studentcnt = 0;
        }
    }
}
