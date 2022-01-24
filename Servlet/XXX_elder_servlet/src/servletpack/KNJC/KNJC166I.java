// kanji=漢字
/*
 * $Id: 3168a2dbd26944b5984683330e31d56adb34cf8e $
 *
 */
package servletpack.KNJC;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import servletpack.KNJZ.detail.CsvUtils;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/*
 *  学校教育システム 賢者 [事務管理] 皆勤・精勤者一覧
 */

public class KNJC166I {

    private static final Log log = LogFactory.getLog(KNJC166I.class);
//    private static final String TARGET_GRADE_ATTRIBUTE = "Paint=(1,90,2),Bold=1";
    private Param _param;
    private boolean _hasdata = false;
    private int MAX_LINE;

    private String OUT_CSVFLG = "1";

    public void svf_out (
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {

        final Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）

        log.info(" $Id: 3168a2dbd26944b5984683330e31d56adb34cf8e $ ");
        KNJServletUtils.debugParam(request, log);

        try {
            // ＤＢ接続
            DB2UDB db2 = null;
            try {
                db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);    //Databaseクラスを継承したクラス
                db2.open();
            } catch (Exception ex) {
                log.error("db2 open error!", ex);
                return;
            }
            _param = new Param(request, db2);

            if (!OUT_CSVFLG.equals(_param._csvoutflg)) {
                response.setContentType("application/pdf");
                svf.VrInit();                             //クラスの初期化
                svf.VrSetSpoolFileStream(response.getOutputStream());         //PDFファイル名の設定
            }


            // 印刷処理
            final Map[] schoolKinds = getSchoolKinds(db2);
            for (int i = 0; i < schoolKinds.length; i++) {
                printGradeList(svf, db2, schoolKinds[i]);
            }

            if (OUT_CSVFLG.equals(_param._csvoutflg)) {
                final Map csvParam = new HashMap();
                csvParam.put("HttpServletRequest", request);
                CsvUtils.outputLines(log, response, _param._fName, _param._outBuf.createCsvBuf(), csvParam);
            }
            for (final Iterator it = _param._psMap.values().iterator(); it.hasNext();) {
                PreparedStatement ps = (PreparedStatement) it.next();
                DbUtils.closeQuietly(ps);
            }

        } catch (Exception ex) {
            log.error("exception!", ex);
        } finally {
            // 終了処理
            if (!_hasdata) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
            svf.VrQuit();
        }
    }

    private void printGradeList(final Vrw32alp svf, final DB2UDB db2, final Map schoolKindMap) {
        final List gradeList = (List) schoolKindMap.get("GRADE_LIST");
        List allList = new ArrayList();
        for (final Iterator git = gradeList.iterator(); git.hasNext();) {
            final Map gradeMap = (Map) git.next();
            final String grade = (String) gradeMap.get("GRADE");
            final String gradeCd = (String) gradeMap.get("GRADE_CD");
            final String schoolKind = (String) gradeMap.get("SCHOOL_KIND");
            final String schoolKindMaxGrade = (String) gradeMap.get("SCHOOL_KIND_MAX_GRADE");

            final List studentList = Student.loadStudentList(db2, _param, grade);
            // 生徒がいなければ処理をスキップ
            if (studentList.size() == 0) {
                continue;
            }
            final Map regdMap = Regd.getRegdMap(studentList);
            log.debug(" grade = " + grade + " (" + gradeMap.get("SCHOOL_KIND") + ") regd key = " + regdMap.keySet());
            for (final Iterator it = regdMap.entrySet().iterator(); it.hasNext();) {
                final Map.Entry e = (Map.Entry) it.next();
                final String key = (String) e.getKey();
                final Collection regdList = (Collection) e.getValue();
                //log.debug(" set attend " + grade + " : " + hrClass);

                final String[] split = StringUtils.split(key, "-");
                DayAttendance.setAttendData(db2, _param, regdList, split[0], split[1], split[2], split[3]);
            }
            //getTargetList内でクラス単位にlist分割する。※画面で学年指定するので、学年は1つしかないが、仕様変更を見据えてそのままとする。
            //listにlistをaddしているので、ループで個別に取得してm.put -> allList.addする。
            final List targetListList = getTargetList(_param, studentList);
            for (int ii = 0;ii < targetListList.size();ii++) {
                Map m = new HashMap();
                m.put("GRADE", grade);
                m.put("GRADE_CD", gradeCd);
                m.put("SCHOOL_KIND", schoolKind);
                m.put("SCHOOL_KIND_MAX_GRADE", schoolKindMaxGrade);
                List addwklst = (List)targetListList.get(ii);
                if (addwklst.size() > 0) {
                    m.put("HR_CLASS", ((Student)addwklst.get(0))._hrclass);
                }
                m.put("LIST", (List)targetListList.get(ii));
                allList.add(m);
            }

        }
        int allPage = 0;
        for (final Iterator it = allList.iterator(); it.hasNext();) {
            final Map m = (Map) it.next();
            final List list = (List) m.get("LIST");
            final String schoolKind = (String) m.get("SCHOOL_KIND");
            if ("J".equals(schoolKind)) {
            	MAX_LINE = 40;
            } else {
            	MAX_LINE = 40;
            }
            allPage += getPageList(list, MAX_LINE).size();
        }
        int page = 1;
        final String title = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._ctrlYear)) + "年度　累計皆勤者一覧表";
        _param._fName = title + ".csv";
        for (final Iterator it = allList.iterator(); it.hasNext();) {
            final Map m = (Map) it.next();
            final List list = (List) m.get("LIST");
            //クラス名は、生徒の情報から取得する。
            final String hrclass = list.size() > 0 ? ((Student)list.get(0)).getCtrlRegdHrName(_param) : "";
            final String grade = (String) m.get("GRADE");
            final String schoolKindMaxGrade = (String) m.get("SCHOOL_KIND_MAX_GRADE");
            final boolean isPrint3Kaikin = null != schoolKindMaxGrade && schoolKindMaxGrade.equals(grade);
            final String sdate = Param.getSemester1Sdate(db2, _param._ctrlYear, grade);
            final String dateRange = "出欠集計範囲 " + StringUtils.defaultString(KNJ_EditDate.h_format_JP(db2, sdate)) + "～" + StringUtils.defaultString(KNJ_EditDate.h_format_JP(db2, _param._date));
            final String gradeCd = (String) m.get("GRADE_CD");
            final String schoolKind = (String) m.get("SCHOOL_KIND");
            if ("J".equals(schoolKind)) {
            	MAX_LINE = 40;
            } else {
            	MAX_LINE = 40;
            }
            final List pageList = getPageList(list, MAX_LINE);
            for (int pi = 0; pi < pageList.size(); pi++) {
                final List studentList = (List) pageList.get(pi);
                if (_param._isOutputDebug) {
                    log.info(" page : " + (pi + 1) + "/" + pageList.size() + ", studentList size = " + studentList.size());
                }
                printPageMain(svf, title, schoolKind, isPrint3Kaikin, gradeCd, hrclass, studentList, page + pi, allPage, dateRange);
            }
            page += pageList.size();
        }
    }

    private Map getKaikinMap(final Student student, final String loginSchoolKind, final List regdJList, final DayAttendance total, final boolean isPrint3Kaikin, final boolean noDataFlg, final boolean isAllKaikin, final int dataCnt, final boolean isCtrlYearKaikin) {
    	final Map kaikinMap = new HashMap();

        boolean noDataFlgJ = false;
        boolean isAllKaikinJ = true;
        if ("H".equals(loginSchoolKind)) {
        	// 中等部の判定
        	if (_param._isOutputDebug) {
        		log.info(" student " + student._schregno + " regdJList size = " + regdJList.size());
        	}
            DayAttendance totalJ = null;
            int dataJCnt = 0;  //2年からの転入者に対する3皆勤チェック用
            for (final Iterator rit = student._regdList.iterator(); rit.hasNext();) {
            //for (final Iterator rit = regdJList.iterator(); rit.hasNext();) {
                final Regd regd = (Regd) rit.next();
                if (!"J".equals(regd._schoolKind)) {
                	continue;
                }
                final DayAttendance da = regd._dayAttendance;
                if (null == da) {
                	noDataFlgJ = true;
                    continue;
                }
                dataJCnt++;
                if (!da._isKaikin) {
                	isAllKaikinJ = false;
                }
                if (null == totalJ) {
                	totalJ = new DayAttendance();
                }
                totalJ = totalJ.add(da);
            }
            //データなしで上記for処理を素通りして後の処理で変にならないよう、中学3年分のデータがない、totalJが作成されてない、daがnullになっている箇所があれば、皆勤フラグをfalseにする。
            if (dataJCnt < 3 || totalJ == null || noDataFlgJ) {
            	isAllKaikinJ = false;
            }
            if (null != totalJ) {
                if (!noDataFlgJ && dataJCnt >= 3) {
                	if (isAllKaikinJ) {
                		kaikinMap.put("3KAIKIN_SEIKINJ_KAIKIN", "1");
                	} else {
//                		if (isSeikin(student._schregno, "ALL_J", totalJ, _param)) {
//                		    kaikinMap.put("3KAIKIN_SEIKINJ_SEIKIN", "1");
//                		}
                	}
                }
            }
        }

        if (null != total) {
            boolean setAnyKaikin = false;
            if (!setAnyKaikin && isPrint3Kaikin) {
            	if (!noDataFlg && dataCnt >= 3) {
            		if (isAllKaikin) {
                		kaikinMap.put("3KAIKIN", "1");
            			setAnyKaikin = true;
            		} else if (isSeikin(student._schregno, "ALL", total, _param)) {
                		kaikinMap.put("3SEIKIN", "1");
                        //setAnyKaikin = true;
            		}
            	}
            }
            if (!setAnyKaikin && isCtrlYearKaikin) {
        		kaikinMap.put("1KAIKIN", "1");
    			setAnyKaikin = true;
            }
        }

    	return kaikinMap;
    }

    private void printPageMain(final Vrw32alp svf, final String title, final String schoolKind, final boolean isPrint3Kaikin, final String gradeCd, final String hrName, final List studentList, final int page, final int allPage, final String dateRange) {
        if (!OUT_CSVFLG.equals(_param._csvoutflg)) {
            printPage(svf, title, schoolKind, isPrint3Kaikin, gradeCd, hrName, studentList, page, allPage, dateRange);
        } else {
            printCsv(svf, title, schoolKind, isPrint3Kaikin, gradeCd, hrName, studentList, page, allPage, dateRange);
        }

    }

    private void printPage(final Vrw32alp svf, final String title, final String schoolKind, final boolean isPrint3Kaikin, final String gradeCd, final String hrName, final List studentList, final int page, final int allPage, final String dateRange) {

        final String form;
        final String[] suffx;
        final String GRADE_ALL = "99";
        if ("J".equals(schoolKind)) {
            // 中等部の列なし
            form = "KNJC166I_2.frm";
        } else {
            form = "KNJC166I_1.frm";
        }
        suffx = new String[] {"1", "2", "3", GRADE_ALL};
        svf.VrSetForm(form, 4);

        svf.VrsOut("nendo", title); // 年度
        svf.VrsOut("PAGE1", String.valueOf(page)); // ページ(分子)
        svf.VrsOut("PAGE2", String.valueOf(allPage)); // ページ(分母)
//        final int iGradeCd = Integer.parseInt(gradeCd);
//        svf.VrsOut("GRADE_NAME", String.valueOf(iGradeCd) + "年"); // 学年名
        svf.VrsOut("GRADE_NAME", hrName); // 年組名
        svf.VrsOut("ymd1", _param._ctrlDateStr); // 作成日
        for (int sfi = 0; sfi < suffx.length; sfi++) {
            final String field = "SEMESTER" + suffx[sfi];
            if (GRADE_ALL.equals(suffx[sfi])) {
                svf.VrsOut(field, "累計"); // 学期
//                if ("2".equals(_param._output)) {
//                    svf.VrAttribute(field, TARGET_GRADE_ATTRIBUTE);
//                }
            } else {
                svf.VrsOut(field, String.valueOf(suffx[sfi]) + "年生"); // 学期
//                if ("1".equals(_param._output) && (sfi + 1 == iGradeCd)) {
//                    svf.VrAttribute(field, TARGET_GRADE_ATTRIBUTE);
//                }
            }
        }
        svf.VrsOut("DATE_RANGE", dateRange); // 当年度出欠集計範囲

        for (int i = 0; i < studentList.size(); i++) {
            final Student student = (Student) studentList.get(i);
            final Regd ctrlRegd = student.getCtrlRegd(_param);
            if (null != ctrlRegd) {
                final String attendno = (NumberUtils.isDigits(ctrlRegd._attendNo) ? keta(3, String.valueOf(Integer.parseInt(ctrlRegd._attendNo))) : StringUtils.defaultString(ctrlRegd._attendNo)) + "番";
                svf.VrsOut("NUMBER", StringUtils.defaultString(ctrlRegd._hrNameAbbv) + " " + attendno); // 番号
            }
            svf.VrsOut("SCHREGNO", student._schregno); // 番号
            svf.VrsOut("name", student._name); // 生徒氏名
            final String loginSchoolKind = student.getCtrlRegd(_param)._schoolKind;
            final List regdJList = new ArrayList();

            DayAttendance total = null;

            boolean noDataFlg = false;
            boolean isAllKaikin = true;
            boolean isCtrlYearKaikin = false;
            int dataCnt = 0;    //3学年分の出席データが登録されているかをチェックするためにカウント
            for (final Iterator rit = student._regdList.iterator(); rit.hasNext();) {
                final Regd regd = (Regd) rit.next();
                if ("H".equals(loginSchoolKind) && "J".equals(regd._schoolKind)) {
                    regdJList.add(regd);
                    continue;
                }

                final String regdGrade = String.valueOf(Integer.parseInt(regd._gradeCd));
                final DayAttendance da = regd._dayAttendance;
                if (null == da) {
                    noDataFlg = true;
                    continue;
                }
                dataCnt++;
                svf.VrsOut("TOTAL_ABSENCE" + regdGrade, String.valueOf(da._sick)); // 欠席計
                svf.VrsOut("TOTAL_LATE" + regdGrade, String.valueOf(da._late)); // 遅刻数
                svf.VrsOut("TOTAL_EARLY" + regdGrade, String.valueOf(da._early)); // 早退数
                svf.VrsOut("TOTAL_SUSPEND" + regdGrade, String.valueOf(da._suspend + da._mourning)); // 停止・忌引数
                svf.VrsOut("TOTAL_KEKKA" + regdGrade, kekka(da)); // 欠課数
                if (da._isKaikin) {
                    svf.VrsOut("KAIKIN" + regdGrade, "皆勤");
                    if (regd._year.equals(_param._ctrlYear)) {
                        isCtrlYearKaikin = true;
                    }
                } else {
                    isAllKaikin = false;
                }
                if (null == total) {
                    total = new DayAttendance();
                }
                total = total.add(da);
            }

            final Map kaikinMap = getKaikinMap(student, loginSchoolKind, regdJList, total, isPrint3Kaikin, noDataFlg, isAllKaikin, dataCnt, isCtrlYearKaikin);
            if (null != total) {
                svf.VrsOut("TOTAL_ABSENCE" + GRADE_ALL, String.valueOf(total._sick)); // 欠席計
                svf.VrsOut("TOTAL_LATE" + GRADE_ALL, String.valueOf(total._late)); // 遅刻数
                svf.VrsOut("TOTAL_EARLY" + GRADE_ALL, String.valueOf(total._early)); // 早退数
                svf.VrsOut("TOTAL_SUSPEND" + GRADE_ALL, String.valueOf(total._suspend + total._mourning)); // 停止・忌引数
                svf.VrsOut("TOTAL_KEKKA" + GRADE_ALL, kekka(total)); // 欠課数
            }

            svf.VrsOut("1KAIKIN", "1".equals(kaikinMap.get("1KAIKIN")) ? "1皆勤" : "");
            svf.VrsOut("3KAIKIN", "1".equals(kaikinMap.get("3KAIKIN")) ? "3皆勤" : "");
            // svf.VrsOut("6KAIKIN", "1".equals(kaikinMap.get("6KAIKIN")) ? "6皆勤" : "");
            svf.VrsOut("3SEIKIN", "1".equals(kaikinMap.get("3SEIKIN")) ? "3精勤" : "");
            if ("H".equals(loginSchoolKind)) {
            	svf.VrsOut("3KAIKIN_SEIKINJ", "1".equals(kaikinMap.get("3KAIKIN_SEIKINJ_KAIKIN")) ? "3皆勤" : "");
            }
            svf.VrEndRecord();
        }
        for (int i = studentList.size(); i < MAX_LINE; i++) {
            svf.VrsOut("name", "\n"); // 生徒氏名
            svf.VrEndRecord();
        }
        _hasdata = true;
    }

    private void printCsv(final Vrw32alp svf, final String title, final String schoolKind, final boolean isPrint3Kaikin, final String gradeCd, final String hrName, final List studentList, final int page, final int allPage, final String dateRange) {
        //タイトル/項目名/データでくくって出力位置制御はしているが、詳細な処理では出力順番でCSV出力も変わるので、注意。
        final String[] suffx;
        final String GRADE_ALL = "99";
        suffx = new String[] {"1", "2", "3", GRADE_ALL};

//  --- 下はタイトル ---
        if (_param._outBuf._clsDataSetPtr._Head.size() == 0) {
            csvOutBuf(svf, title, 1);   // タイトル
            csvOutBuf(svf, hrName, 1);  // 年組名
            csvOutBuf(svf,  _param._ctrlDateStr, 1);  // 作成日
            csvOutBuf(svf, dateRange, 1); //出力集計範囲
        }
//  --- 上はタイトル、下は項目名 ---
        if (_param._outBuf._TableElmName.size() == 0) {
            csvOutBuf(svf, "年組番", 2);
            csvOutBuf(svf, "学籍番号", 2);
            csvOutBuf(svf, "氏名", 2);
            for (int sfi = 0; sfi < suffx.length; sfi++) {
                final String field = "SEMESTER" + suffx[sfi];
    			if (GRADE_ALL.equals(suffx[sfi])) {
    				final String totalstr = "累計";
      	            csvOutBuf(svf, totalstr + "欠席数", 2);
      	            csvOutBuf(svf, totalstr + "遅刻", 2);
      	            csvOutBuf(svf, totalstr + "早退", 2);
      	            csvOutBuf(svf, totalstr + "出停・忌引", 2);
      	            csvOutBuf(svf, totalstr + "欠課時数", 2);
      	            // csvOutBuf(svf, totalstr + "皆勤", 2);
                } else {
                    final String gradestr = String.valueOf(suffx[sfi]) + "年生";
                    csvOutBuf(svf, gradestr + "欠席数", 2);
      	            csvOutBuf(svf, gradestr + "遅刻", 2);
      	            csvOutBuf(svf, gradestr + "早退", 2);
      	            csvOutBuf(svf, gradestr + "出停・忌引", 2);
      	            csvOutBuf(svf, gradestr + "欠課時数", 2);
      	            csvOutBuf(svf, gradestr + "皆勤", 2);
                }
            }
            csvOutBuf(svf, "1皆勤", 2);
            csvOutBuf(svf, "3皆勤", 2);
            // csvOutBuf(svf, "6皆勤", 2);
            csvOutBuf(svf, "3精勤", 2);
            if ("H".equals(schoolKind)) {
                csvOutBuf(svf, "中等部3皆勤／3精勤", 2);
            }
            csvOutBuf(svf, "DUMMY", 2);
        }

//  --- 上は項目名、下はデータ ---
        for (int i = 0; i < studentList.size(); i++) {
            final Student student = (Student) studentList.get(i);
            final Regd ctrlRegd = student.getCtrlRegd(_param);
            if (null != ctrlRegd) {
                final String attendno = (NumberUtils.isDigits(ctrlRegd._attendNo) ? keta(3, String.valueOf(Integer.parseInt(ctrlRegd._attendNo))) : StringUtils.defaultString(ctrlRegd._attendNo)) + "番";
                csvOutBuf(svf, StringUtils.defaultString(ctrlRegd._hrNameAbbv) + " " + attendno, 3); // 年組番
            } else {
                csvOutBuf(svf, "", 3); // 年組番
            }
            csvOutBuf(svf, student._schregno, 3); // 学籍番号
            csvOutBuf(svf, student._name, 3); // 生徒氏名
            final String loginSchoolKind = student.getCtrlRegd(_param)._schoolKind;
            final List regdJList = new ArrayList();

            DayAttendance total = null;

            boolean noDataFlg = false;
            boolean isAllKaikin = true;
            boolean isCtrlYearKaikin = false;
            int dataCnt = 0;    //3学年分の出席データが登録されているかをチェックするためにカウント
            int regdoutmax = 3; //3学年分+累計で4だが、累計を出力する直前に使うため、-1している。
            int regdcnt = 0;
            for (final Iterator rit = student._regdList.iterator(); rit.hasNext();) {
                final Regd regd = (Regd) rit.next();
                if ("H".equals(loginSchoolKind) && "J".equals(regd._schoolKind)) {
                    regdJList.add(regd);
                    continue;
                }

                final String regdGrade = String.valueOf(Integer.parseInt(regd._gradeCd));
                final DayAttendance da = regd._dayAttendance;
                if (null == da) {
                    csvOutBuf(svf, "", 3); // 欠席計
                    csvOutBuf(svf, "", 3); // 遅刻数
                    csvOutBuf(svf, "", 3); // 早退数
                    csvOutBuf(svf, "", 3); // 停止・忌引数
                    csvOutBuf(svf, "", 3); // 欠課数
                    csvOutBuf(svf, "", 3); // 皆勤
                    regdcnt++;
                    noDataFlg = true;
                    continue;
                }
                dataCnt++;
                csvOutBuf(svf, (da._sick == 0 ? "" : String.valueOf(da._sick)), 3); // 欠席数
                csvOutBuf(svf, (da._late == 0 ? "" : String.valueOf(da._late)), 3); // 遅刻数
                csvOutBuf(svf, (da._early == 0 ? "" : String.valueOf(da._early)), 3); // 早退数
                csvOutBuf(svf, (da._suspend + da._mourning == 0 ? "" : String.valueOf(da._suspend + da._mourning)), 3); // 停止・忌引数
                String kekkastr = kekka(da);
                csvOutBuf(svf, (("0".equals(kekkastr) || "0.0".equals(kekkastr) || ".0".equals(kekkastr)) ? "" : kekka(da)), 3); // 欠課数
                if (da._isKaikin) {
                    csvOutBuf(svf, "皆勤", 3); // 皆勤
                    if (regd._year.equals(_param._ctrlYear)) {
                        isCtrlYearKaikin = true;
                    }
                } else {
                    csvOutBuf(svf, "", 3); // 皆勤
                    isAllKaikin = false;
                }
                if (null == total) {
                    total = new DayAttendance();
                }
                total = total.add(da);
                regdcnt++;
            }
            if (regdcnt < regdoutmax) {
                for (int ii = regdcnt;ii < regdoutmax;ii++) {
                    csvOutBuf(svf, "", 3); // 欠席計
                    csvOutBuf(svf, "", 3); // 遅刻数
                    csvOutBuf(svf, "", 3); // 早退数
                    csvOutBuf(svf, "", 3); // 停止・忌引数
                    csvOutBuf(svf, "", 3); // 欠課数
                    csvOutBuf(svf, "", 3); // 皆勤
                    regdcnt++;
                }
            }

            if (null != total) {
                csvOutBuf(svf, (total._sick == 0 ? "" : String.valueOf(total._sick)),3); // 欠席計
                csvOutBuf(svf, (total._late == 0 ? "" : String.valueOf(total._late)),3); // 遅刻数
                csvOutBuf(svf, (total._early == 0 ? "" : String.valueOf(total._early)),3); // 早退数
                csvOutBuf(svf, (total._suspend + total._mourning == 0 ? "" : String.valueOf(total._suspend + total._mourning)),3); // 停止・忌引数
                String kekkastr = kekka(total);
                csvOutBuf(svf, (("0".equals(kekkastr) || "0.0".equals(kekkastr) || ".0".equals(kekkastr)) ? "" : kekka(total)),3); // 欠課数
            } else {
            	csvOutBuf(svf, "",3); // 欠席計
            	csvOutBuf(svf, "",3); // 遅刻数
            	csvOutBuf(svf, "",3); // 早退数
            	csvOutBuf(svf, "",3); // 停止・忌引数
            	csvOutBuf(svf, "",3); // 欠課数
            }
            final Map kaikinMap = getKaikinMap(student, loginSchoolKind, regdJList, total, isPrint3Kaikin, noDataFlg, isAllKaikin, dataCnt, isCtrlYearKaikin);
        	csvOutBuf(svf, "1".equals(kaikinMap.get("1KAIKIN")) ? "1皆勤" : "",3); // 1皆勤
        	csvOutBuf(svf, "1".equals(kaikinMap.get("3KAIKIN")) ? "3皆勤" : "",3); // 3皆勤
        	// csvOutBuf(svf, "1".equals(kaikinMap.get("6KAIKIN")) ? "6皆勤" : "",3); // 6皆勤
        	csvOutBuf(svf, "1".equals(kaikinMap.get("3SEIKIN")) ? "3精勤" : "",3); // 3精勤
            if ("H".equals(loginSchoolKind)) {
            	csvOutBuf(svf, "1".equals(kaikinMap.get("3KAIKIN_SEIKINJ_KAIKIN")) ? "3皆勤" : "",3); //  3皆勤/3精勤J
            }
            csvOutBuf(svf, "DUMMY", 3); // DUMMY

            _param._outBuf._clsDataSetPtr.feedLine();
        }
        _param._outBuf.feedClsDataSet();
        _hasdata = true;
    }

    private String keta(final int keta, final String v) {
        return StringUtils.repeat(" ", keta - StringUtils.defaultString(v).length()) + StringUtils.defaultString(v);
    }

    private String kekka(final DayAttendance da) {
        final int scale = "3".equals(_param._knjSchoolMst._absentCov) || "4".equals(_param._knjSchoolMst._absentCov) ? 1 : 0;
        final String k = null == da._kekka ? "" : da._kekka.setScale(scale, BigDecimal.ROUND_HALF_UP).toString(); // 欠課時数総合計
        return k;
    }

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
     *
     * @param param
     * @param studentList
     * @return
     */
    private List getTargetList(final Param param, final List studentList) {
        //クラス毎にリスト作成する。
        //学年の変化も、念のためチェックする。
        String beforegradehrclass = "";
        List targetList = new ArrayList();
        final List retList = new ArrayList();
        retList.add(targetList);
        //int noTargetCount = 0;
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            if (student._regdList.size() == 0) {
                log.warn(" 在籍が無い:" + student._schregno);
                continue;
            }
            boolean isKaikin = true; // いずれの年度かが皆勤なら対象
            //クラスが変わるタイミングでNULLデータを入れる。
            if (!"".equals(beforegradehrclass) && !beforegradehrclass.equals(student._nowgrade_hrclass)) {
                targetList = new ArrayList();
                retList.add(targetList);
            }
            beforegradehrclass = student._nowgrade_hrclass;
            for (final Iterator rit = student._regdList.iterator(); rit.hasNext();) {
                final Regd regd = (Regd) rit.next();
                final boolean targetKaikin = isKaikin(student._schregno, regd._year, regd._dayAttendance, param);
                if (targetKaikin) {
                    regd._dayAttendance._isKaikin = true;
                    isKaikin = true;
                }
            }
            boolean isTarget = isKaikin;
            if (isTarget) {
                targetList.add(student);
            }
        }
        return retList;
    }

    private boolean isKaikin(final String schregno, final String year, final DayAttendance da, final Param param) {
        if (null == da) {
            log.info(" no attend : schregno = " + schregno + ", year = " + year + ",  att = " + da);
            return false; // 出欠データのない生徒は対象外
        }
        if (da._lesson == 0) { // 対象外
            //log.info(" lesson 0 : schregno = " + schregno + ", year = " + year + ",  att = " + da);
            return false;
        }

        boolean isTarget = da._sick == 0 && da._late == 0 && da._early == 0 && (null == da._kekka || da._kekka.doubleValue() == 0.0);
        return isTarget;
    }

    private boolean isSeikin(final String schregno, final String year, final DayAttendance da, final Param param) {
        if (null == da) {
            log.info(" no attend : schregno = " + schregno + ", year = " + year + ",  att = " + da);
            return false; // 出欠データのない生徒は対象外
        }
        if (da._lesson == 0) { // 対象外
            //log.info(" lesson 0 : schregno = " + schregno + ", year = " + year + ",  att = " + da);
            return false;
        }

        //遅刻/早退/欠課が9以下(遅刻/早退/欠課の合計が3で1欠席なので、都合9となる)かつ、欠席が3以下かをチェック(判定式の右辺が負なら、左辺が0でも精勤対象外)
        //ざっくり書くと、欠席分を除いた精勤許容残数と遅刻/早退/欠課の合計をチェックしている。
    	boolean isTarget = (da._late + da._early + (null == da._kekka ? 0.0 : da._kekka.doubleValue()) <= 9 - (da._sick * 3));
        return isTarget;
    }

    private Map[] getSchoolKinds(final DB2UDB db2) {
        List list = new ArrayList();

        String sql = "";
        sql += " SELECT T1.GRADE, T1.GRADE_CD, T1.SCHOOL_KIND, T2.GRADE AS SCHOOL_KIND_MAX_GRADE ";
        sql += " FROM SCHREG_REGD_GDAT T1 ";
        sql += " LEFT JOIN (SELECT YEAR, SCHOOL_KIND, MAX(GRADE) AS GRADE ";
        sql += "            FROM SCHREG_REGD_GDAT ";
        sql += "            GROUP BY YEAR, SCHOOL_KIND) T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
        sql += " WHERE T1.YEAR = '" + _param._ctrlYear + "' AND T1.GRADE = '" + _param._grade + "' ";
        sql += " ORDER BY T1.GRADE ";

        for (final Iterator qit = KnjDbUtils.query(db2, sql).iterator(); qit.hasNext();) {
            final Map row = (Map) qit.next();
            Map tm = null;
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final Map m = (Map) it.next();
                if (KnjDbUtils.getString(row, "SCHOOL_KIND").equals(m.get("SCHOOL_KIND"))) {
                    tm = m;
                    break;
                }
            }
            if (null == tm) {
                tm = new HashMap();
                tm.put("SCHOOL_KIND", KnjDbUtils.getString(row, "SCHOOL_KIND"));
                list.add(tm);
            }
            if (null == tm.get("GRADE_LIST")) {
                tm.put("GRADE_LIST", new ArrayList());
            }
            final List gradeList = (List) tm.get("GRADE_LIST");
            final Map gradeMap = new HashMap();
            gradeList.add(gradeMap);
            gradeMap.put("GRADE", KnjDbUtils.getString(row, "GRADE"));
            gradeMap.put("GRADE_CD", KnjDbUtils.getString(row, "GRADE_CD"));
            gradeMap.put("SCHOOL_KIND", KnjDbUtils.getString(row, "SCHOOL_KIND"));
            gradeMap.put("SCHOOL_KIND_MAX_GRADE", KnjDbUtils.getString(row, "SCHOOL_KIND_MAX_GRADE"));
        }

        final Map[] arr = new Map[list.size()];
        for (int i = 0; i < list.size(); i++) {
            arr[i] = (Map) list.get(i);
        }
        return arr;
    }

    private static class Regd {
        final Student _student;
        final String _year;
        final String _semester;
        final String _grade;
        final String _gradeCd;
        final String _schoolKind;
        final String _hrClass;
        final String _hrName;
        final String _hrNameAbbv;
        final String _attendNo;

        DayAttendance _dayAttendance = null;
        public Regd(
                final Student student,
                final String year,
                final String semester,
                final String grade,
                final String gradeCd,
                final String schoolKind,
                final String hrClass,
                final String hrName,
                final String hrNameAbbv,
                final String attendNo) {
            _student = student;
            _year = year;
            _semester = semester;
            _grade = grade;
            _gradeCd = gradeCd;
            _schoolKind = schoolKind;
            _hrClass = hrClass;
            _hrName = hrName;
            _hrNameAbbv = hrNameAbbv;
            _attendNo = attendNo;
        }

        private static Map getRegdMap(final List studentList) {
            final Map map = new HashMap();
            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                for (final Iterator git = student._regdList.iterator(); git.hasNext();) {
                    final Regd regd = (Regd) git.next();
                    final String key = regd._year + "-" + regd._semester + "-" + regd._grade + "-" + regd._hrClass;
                    if (null == map.get(key)) {
                        map.put(key, new ArrayList());
                    }
                    ((List) map.get(key)).add(regd);
                }
            }
            return map;
        }
    }

    private static class Student {
        final String _grade;
        final String _hrclass;
        final String _nowgrade_hrclass;
        final String _schregno;
        final String _name;
        final String _sex;

        final List _regdList = new ArrayList();

        public Student(
                final String grade,
                final String hrclass,
                final String nowgrade_hrclass,
                final String schregno,
                final String name,
                final String sex) {
            _grade = grade;
            _hrclass = hrclass;
            _nowgrade_hrclass = nowgrade_hrclass;
            _schregno = schregno;
            _name = name;
            _sex = sex;
        }

        public String getCtrlRegdHrName(final Param param) {
            for (final Iterator it = _regdList.iterator(); it.hasNext();) {
                final Regd regd = (Regd) it.next();
                if (regd._year.equals(param._ctrlYear) && regd._semester.equals(param._ctrlSemester)) {
                    return regd._hrName;
                }
            }
            return "";
        }
        public Regd getCtrlRegd(final Param param) {
            for (final Iterator it = _regdList.iterator(); it.hasNext();) {
                final Regd regd = (Regd) it.next();
                if (regd._year.equals(param._ctrlYear) && regd._semester.equals(param._ctrlSemester)) {
                    return regd;
                }
            }
            return null;
        }

        public static List loadStudentList(final DB2UDB db2, final Param param, final String grade) {
            final List studentList = new ArrayList();
            final Map schregMap = new HashMap();

            // HRの生徒を取得
            final String psKey = "REGD_SQL";
            if (null == param._psMap.get(psKey)) {
                final String sql = sqlSchregRegdDat(param);
                log.debug("sql = " + sql);
                if (param._isOutputDebug) {
                    log.info("schreg_regd_dat sql = " + sql);
                }
                try {
                    param._psMap.put(psKey, db2.prepareStatement(sql));
                } catch (Exception e) {
                    log.error("exception!", e);
                }
            }
            PreparedStatement ps = (PreparedStatement) param._psMap.get(psKey);
            for (final Iterator it = KnjDbUtils.query(db2, ps, new Object[] {grade}).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                final String schregno = KnjDbUtils.getString(row, "SCHREGNO");
                if (null == schregMap.get(schregno)) {
                    final Student st = new Student(
                            KnjDbUtils.getString(row, "GRADE"),
                            KnjDbUtils.getString(row, "HR_CLASS"),
                            KnjDbUtils.getString(row, "NOW_GRADE_HR_CLASS"),
                            schregno,
                            KnjDbUtils.getString(row, "NAME"),
                            KnjDbUtils.getString(row, "SEX"));
                    schregMap.put(schregno, st);
                    studentList.add(st);
                }
                final Student student = (Student) schregMap.get(schregno);
                student._regdList.add(new Regd(student, KnjDbUtils.getString(row, "YEAR"), KnjDbUtils.getString(row, "SEMESTER"), KnjDbUtils.getString(row, "GRADE"), KnjDbUtils.getString(row, "GRADE_CD"), KnjDbUtils.getString(row, "SCHOOL_KIND"), KnjDbUtils.getString(row, "HR_CLASS"), KnjDbUtils.getString(row, "HR_NAME"), KnjDbUtils.getString(row, "HR_NAMEABBV"), KnjDbUtils.getString(row, "ATTENDNO")));
            }
            return studentList;
        }

        /** 学生を得るSQL */
        private static String sqlSchregRegdDat(final Param param) {
            StringBuffer stb = new StringBuffer();

            stb.append(" WITH T_REGD0 AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.ATTENDNO ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._ctrlYear + "' ");
            stb.append("     AND T1.SEMESTER = '" + param._ctrlSemester + "' ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.ATTENDNO ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1");
            stb.append("     INNER JOIN (SELECT SCHREGNO, YEAR, MAX(SEMESTER) AS SEMESTER FROM SCHREG_REGD_DAT GROUP BY SCHREGNO, YEAR) T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR < '" + param._ctrlYear + "' ");
            stb.append(" ), T_REGD AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.ATTENDNO ");
            stb.append(" FROM ");
            stb.append("     T_REGD0 T1");
            stb.append("     INNER JOIN (SELECT SCHREGNO, GRADE, MAX(YEAR) AS YEAR FROM T_REGD0 GROUP BY SCHREGNO, GRADE) T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ");
            stb.append("     INNER JOIN SCHREG_REGD_GDAT T4 ON T4.YEAR = T1.YEAR AND T4.GRADE = T1.GRADE ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     REGD.SCHREGNO, ");
            stb.append("     BASE.NAME, ");
            stb.append("     BASE.SEX, ");
            stb.append("     T5.YEAR, ");
            stb.append("     T5.SEMESTER, ");
            stb.append("     T5.GRADE, ");
            stb.append("     REGD.GRADE || REGD.HR_CLASS AS NOW_GRADE_HR_CLASS, ");
            stb.append("     REGDG.GRADE_CD, ");
            stb.append("     REGDG.SCHOOL_KIND, ");
            stb.append("     T5.HR_CLASS, ");
            stb.append("     T5.ATTENDNO, ");
            stb.append("     T3.HR_NAME, ");
            stb.append("     T3.HR_NAMEABBV ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT REGD");
            stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ");
            stb.append("     LEFT JOIN T_REGD T5 ON T5.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     LEFT JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = T5.YEAR AND REGDG.GRADE = T5.GRADE ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT T3 ON ");
            stb.append("         T5.YEAR = T3.YEAR ");
            stb.append("         AND T5.SEMESTER = T3.SEMESTER ");
            stb.append("         AND T5.GRADE = T3.GRADE ");
            stb.append("         AND T5.HR_CLASS = T3.HR_CLASS ");
//            stb.append("     INNER JOIN SCHREG_ENT_GRD_HIST_DAT ENTGRD ON ENTGRD.SCHREGNO = T5.SCHREGNO ");
//            stb.append("         AND ENTGRD.SCHOOL_KIND = REGDG.SCHOOL_KIND ");
            stb.append(" WHERE ");
            stb.append("     REGD.YEAR = '" + param._ctrlYear + "' ");
            stb.append("     AND REGD.SEMESTER = '" + param._ctrlSemester + "' ");
            stb.append("     AND REGD.GRADE = ? ");
            stb.append("     AND REGD.GRADE || REGD.HR_CLASS IN " + SQLUtils.whereIn(true, param._classSelected));
//            stb.append("     AND ENTGRD.ENT_DIV IN ('1', '2', '3') ");
            stb.append(" ORDER BY ");
            stb.append("     REGD.GRADE, REGD.HR_CLASS, REGD.ATTENDNO, T5.YEAR, T5.SEMESTER, T5.GRADE, T5.HR_CLASS, T5.ATTENDNO ");
            return stb.toString();
        }
    }

    /** 1日出欠カウント */
    private static class DayAttendance {
        /** 授業日数 */
        private int _lesson;
        /** 忌引日数 */
        private int _mourning;
        /** 出停日数 */
        private int _suspend;
        private int _virus;
        private int _koudome;
        /** 出席すべき日数 */
        private int _mlesson;
        /** 欠席日数 */
        private int _sick;
        /** 遅刻日数 */
        private int _late;
        /** 早退日数 */
        private int _early;
        /** 欠課時数 */
        private BigDecimal _kekka;

        private boolean _isKaikin = false;

        public DayAttendance add(final DayAttendance a) {
            final DayAttendance n = new DayAttendance();
            n._lesson = _lesson + a._lesson;
            n._mourning = _mourning + a._mourning;
            n._suspend = _suspend + a._suspend;
            n._virus = _virus + a._virus;
            n._koudome = _koudome + a._koudome;
            n._mlesson = _mlesson + a._mlesson;
            n._sick = _sick + a._sick;
            n._late = _late + a._late;
            n._early = _early + a._early;
            if (null != _kekka || null != a._kekka) {
                n._kekka = (null == _kekka ? new BigDecimal(0) : _kekka).add(null == a._kekka ? new BigDecimal(0) : a._kekka);
            }
            return n;
        }

        public String toString() {
            DecimalFormat df5 = new DecimalFormat("000");
            return
            "LESSON=" + df5.format(_lesson)
            + ", MOR=" + df5.format(_mourning)
            + ", SSP=" + df5.format(_suspend)
            + ", MLS=" + df5.format(_mlesson)
            + ", SCK=" + df5.format(_sick)
            + ", LAT=" + df5.format(_late)
            + ", EAL=" + df5.format(_early);
        }

        private static void setAttendData(final DB2UDB db2, final Param param, final Collection regdList, final String year, final String semester, final String grade, final String hrClass) {
            String sql = null;
            try {
                String psKey = "ATTEND" + year + grade;
                if (null == param._psMap.get(psKey)) {
                    // 出欠の情報
                    String date;
                    if (year.equals(param._ctrlYear)) {
                        date = param._date;
                    } else {
                        date = String.valueOf(Integer.parseInt(year) + 1) + "-03-31";
                    }
                    param._attendParamMap.put("attendSemesGrade", grade);

                    sql = AttendAccumulate.getAttendSemesSql(year, semester, null, date, param._attendParamMap);
                    //log.debug(" sql = " + sql);
                    param._psMap.put(psKey, db2.prepareStatement(sql));
                    log.debug(" prepared.");
                }

                PreparedStatement ps = (PreparedStatement) param._psMap.get(psKey);

                final Map regdMap = new HashMap();
                for (final Iterator it = regdList.iterator(); it.hasNext();) {
                    final Regd regd = (Regd) it.next();
                    regdMap.put(regd._student._schregno, regd);
                }
                final Integer zero = new Integer(0);
                for (final Iterator it = KnjDbUtils.query(db2, ps, new Object[] {grade, hrClass}).iterator(); it.hasNext();) {
                    final Map row = (Map) it.next();
                    if (!"9".equals(KnjDbUtils.getString(row, "SEMESTER"))) {
                        continue;
                    }

                    final Regd regd = (Regd) regdMap.get(KnjDbUtils.getString(row, "SCHREGNO"));
                    if (null == regd) {
                        continue;
                    }
                    if (null == regd._dayAttendance) {
                        regd._dayAttendance = new DayAttendance();
                    }

                    final int lesson   = KnjDbUtils.getInt(row, "LESSON", zero).intValue(); // 授業日数
                    final int sick     = KnjDbUtils.getInt(row, "SICK", zero).intValue(); // 病欠日数
                    final int special  = KnjDbUtils.getInt(row, "MOURNING", zero).intValue() + KnjDbUtils.getInt(row, "SUSPEND", zero).intValue() + KnjDbUtils.getInt(row, "VIRUS", zero).intValue() + KnjDbUtils.getInt(row, "KOUDOME", zero).intValue(); // 特別欠席
                    final int mlesson  = lesson - special; // 出席すべき日数
                    regd._dayAttendance._lesson   += lesson;
                    regd._dayAttendance._mourning += KnjDbUtils.getInt(row, "MOURNING", zero).intValue();
                    regd._dayAttendance._suspend  += KnjDbUtils.getInt(row, "SUSPEND", zero).intValue();
                    regd._dayAttendance._virus  += KnjDbUtils.getInt(row, "VIRUS", zero).intValue();
                    regd._dayAttendance._koudome  += KnjDbUtils.getInt(row, "KOUDOME", zero).intValue();
                    regd._dayAttendance._mlesson  += mlesson;
                    regd._dayAttendance._sick     += sick;
                    regd._dayAttendance._late     += KnjDbUtils.getInt(row, "LATE", zero).intValue();
                    regd._dayAttendance._early    += KnjDbUtils.getInt(row, "EARLY", zero).intValue();
                }

            } catch (Exception ex) {
                log.error("exception!", ex);
            }

            try {

                final StringBuffer stb = new StringBuffer();
                final String[] dateArray;
                if (year.equals(param._ctrlYear)) {
                    dateArray = StringUtils.split(param._date, "-");
                } else {
                    dateArray = StringUtils.split(String.valueOf(Integer.parseInt(year) + 1) + "-03-31", "-");
                }
                final String setYM = dateArray[0] + dateArray[1];
                stb.append(" SELECT ");
                stb.append("     SCHREGNO, ");
                stb.append("     SUM(VALUE(CNT, 0)) AS SICK ");
                stb.append(" FROM ");
                stb.append("     ATTEND_SEMES_DETAIL_DAT ");
                stb.append(" WHERE ");
                stb.append("     YEAR = '" + year + "' ");
                stb.append("     AND YEAR || MONTH <= '" + setYM + "' ");
                stb.append("     AND SEQ = '102' ");
                stb.append(" GROUP BY ");
                stb.append("     SCHREGNO ");
                PreparedStatement psDetail102 = null;
                ResultSet rsDetail102 = null;
                psDetail102 = db2.prepareStatement(stb.toString());
                rsDetail102 = psDetail102.executeQuery();

                final Map regdMap = new HashMap();
                for (final Iterator it = regdList.iterator(); it.hasNext();) {
                    final Regd regd = (Regd) it.next();
                    regdMap.put(regd._student._schregno, regd);
                }
                while (rsDetail102.next()) {

                    String schregNo = rsDetail102.getString("SCHREGNO");
                    final Regd regd = (Regd) regdMap.get(schregNo);
                    if (null == regd) {
                        continue;
                    }
                    if (null == regd._dayAttendance) {
                        regd._dayAttendance = new DayAttendance();
                    }

                    regd._dayAttendance._kekka = rsDetail102.getBigDecimal("SICK");
                }

            } catch (Exception ex) {
                log.error("exception!", ex);
            }
        }
    }

    private void csvOutBuf(final Vrw32alp svf, final String dat, final int outLine) {
        if (OUT_CSVFLG.equals(_param._csvoutflg)) {
            if (outLine == 1) {
                _param._outBuf._clsDataSetPtr._Head.add(dat);
            } else if (outLine == 2) {
                _param._outBuf._TableElmName.add(dat);
            } else {
                _param._outBuf._clsDataSetPtr._DetailPtr.add(dat);
            }
        }
        return;
    }

    private static class CsvOutDataSet {
        private final List _Head;
        private final List _Detail;

        private List _DetailPtr;

        CsvOutDataSet() {
            _Head = new ArrayList();
            _Detail = new ArrayList();
            feedLine();
        }
        void feedLine() {
            _Detail.add(new ArrayList());
            _DetailPtr = getDetailLine();
        }
        List getDetailLine() {
            return (List)_Detail.get(_Detail.size()-1);
        }
    }
    private static class CsvOutLine {
        private final List _TableElmName;    //StringのList
        private final List _clsDataSet;      //CsvOutDataSetのList

        private CsvOutDataSet _clsDataSetPtr;

        CsvOutLine() {
            _TableElmName = new ArrayList();
            _clsDataSet = new ArrayList();
            feedClsDataSet();
        }
        void feedClsDataSet() {
            _clsDataSet.add(new CsvOutDataSet());
            _clsDataSetPtr = getClsDataSet();
        }
        CsvOutDataSet getClsDataSet() {
            return (CsvOutDataSet)_clsDataSet.get(_clsDataSet.size()-1);
        }
        List createCsvBuf() {
            int ii = 0;
            List totalOutputList = new ArrayList();

            totalOutputList.add(_TableElmName);

            for(ii = 0;ii < _clsDataSet.size();ii++) {
                CsvOutDataSet wkcls = (CsvOutDataSet)_clsDataSet.get(ii);
                totalOutputList.add(wkcls._Head);
                int jj = 0;
                for(jj = 0;jj < wkcls._Detail.size();jj++) {
                    List wklist = (List)wkcls._Detail.get(jj);
                    if (wklist.size() > 0) {
                        totalOutputList.add(wklist);
                    }
                }
            }

            return totalOutputList;
        }
    }

    private static class Param {
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _date;
        final String _ctrlDate;
        final String _ctrlDateStr;
        final boolean _isOutputDebug;

        final String _grade;
        final String[] _classSelected;
        final String _useSchool_KindField;
        final String _SCHOOLKIND;
        final String _schoolKind; //校種（学年より取得）

        final String _csvoutflg;
        final CsvOutLine _outBuf;
        private String _fName;

        private KNJSchoolMst _knjSchoolMst;

        final Map _psMap;
        final Map _attendParamMap;

        Param(final HttpServletRequest request, final DB2UDB db2) {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester  = request.getParameter("CTRL_SEMESTER");
            _date = request.getParameter("DATE").replace('/', '-');
            _ctrlDate = null == request.getParameter("CTRL_DATE") ? null : request.getParameter("CTRL_DATE").replace('/', '-');
            _ctrlDateStr = KNJ_EditDate.h_format_JP(db2, _ctrlDate);

            _grade = request.getParameter("GRADE");
            _classSelected = request.getParameterValues("CLASS_SELECTED");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLKIND = request.getParameter("SCHOOLKIND");
            _schoolKind = getSchoolKind(db2);

            final String chkcsv = request.getParameter("cmd");
            if (chkcsv != null && "csv".equals(chkcsv)) {
                _csvoutflg = "1";
            } else {
                _csvoutflg = "0";
            }
            _outBuf = new CsvOutLine();
            _fName = "";

            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _ctrlYear);
            } catch (Exception ex) {
                log.error("Param load exception!", ex);
            }
            final String z010 = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' "));
            log.info(" z010 = " + z010);
            _psMap = new HashMap();

            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));
            // 出欠の情報
            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("absenceDiv", "2");
            _attendParamMap.put("grade", "?");
            _attendParamMap.put("hrClass", "?");
            _attendParamMap.put("knjSchoolMst", _knjSchoolMst);
        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJC166I' AND NAME = '" + propName + "' "));
        }

        private static int defval(final String val, final int def) {
            return NumberUtils.isDigits(val) ? Integer.parseInt(val) : def;
        }

        private static String getSemester1Sdate(final DB2UDB db2, final String year, final String grade) {
            final String[] semes = {"1", "9"};
            boolean hasRecord = false;
            String rtn = null;
            try {
                hasRecord = false;
                for (int i = 0; i < semes.length; i++) {
                    final String sql = "SELECT SDATE FROM SEMESTER_MST WHERE YEAR = '" + year + "' AND SEMESTER = '" + semes[i] + "' ORDER BY SEMESTER";
                    for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
                        final Map row = (Map) it.next();
                        final String sdate = KnjDbUtils.getString(row, "SDATE");
                        rtn = sdate;
                        log.debug("set " + year + "-" + semes[i] + " sdate = " + sdate);
                        if (null != sdate) {
                            hasRecord = true;
                        }
                    }
                    if (hasRecord) {
                        break;
                    }
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            }
            if (null != grade) {
                try {
                    hasRecord = false;
                    for (int i = 0; i < semes.length; i++) {
                        final String sql = "SELECT GRADE, SDATE FROM V_SEMESTER_GRADE_MST WHERE YEAR = '" + year + "' AND SEMESTER = '" + semes[i] + "' AND GRADE = '" + grade + "' ORDER BY SEMESTER";
                        for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
                            final Map row = (Map) it.next();
                            final String sdate = KnjDbUtils.getString(row, "SDATE");
                            rtn = sdate;
                            if (null != sdate) {
                                hasRecord = true;
                            }
                        }
                        if (hasRecord) {
                            break;
                        }
                    }
                } catch (Exception ex) {
                    log.error("V_SEMESTER_GRADE_MST取得エラー:", ex);
                }
            }
            return rtn;
        }

        private String getSchoolKind(final DB2UDB db2) {
            String schoolKind = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = " SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _ctrlYear + "' AND GRADE = '" + _grade + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    schoolKind = rs.getString("SCHOOL_KIND");
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return schoolKind;
        }
    }
}
