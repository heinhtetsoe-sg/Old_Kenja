// kanji=漢字
/*
 * $Id: 358633efd17cf9944f6370b2f9830ecf0a5310fa $
 *
 * 作成日:
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJC;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_ClassCode;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KnjDbUtils;

/*
 *  学校教育システム 賢者 [出欠管理]     勤怠集計表 [個人資料]  科目別勤怠集計表
 *
 *  2004/03/29 yamashiro・教科コード仕様の変更に伴う修正
 *  2004/07/10 yamashiro・１日欠席のときも欠課とする（取り敢えず元の仕様にココだけ変更を加える）
 *  2004/08/24 yamashiro・組名称に"組"がない場合に対処-->組名称と出席番号の間に"-"を挿入
 *  2004/11/17 yamashiro・遅刻コードに'15'を追加
 *                      ・遅刻、欠課、出欠を単独で処理した場合の不具合を修正
 *  2004/11/18 nakamoto ・出欠以外で処理した場合、回数が２倍で表示される不具合を修正
 *  2004/11/19 yamashiro・
 *  2004/12/03 yamashiro・出席簿に出欠仕様を合わせる
 *  2005/04/14 yamashiro・最初の校時の忌引は遅刻にカウントしない、最後の校時の忌引は早退にカウントしない。
 *  2005/04/20 yamashiro・１６校時対応
 *                      ・欠課時数換算の有無および換算基底数をテーブルにもつことによる変更
 *                      ・欠課時数換算の年間単位での処理を追加 => 元は学期ごとの換算のみ
 *                      ・欠課時数換算により欠課が発生し且つ明細(日付)行がない場合は、明細なしで出力するように修正（新仕様に合わせて）
 *                      ・欠課時数換算が年間の場合、年度始めの日付を印刷範囲日付の開始日とする（新仕様に合わせて）
 *                        また、欠課時数換算なしの場合、印刷範囲日付の再設定は行わない（新仕様に合わせて）
 *  2005/06/18 yamashiro・欠課時数関連フラグの取得先をKNJ_Get_InfoからKNJDefineCodeへ変更
 */

public class KNJC110 implements KNJ_ClassCode {

    private static final Log log = LogFactory.getLog(KNJC110.class);
    
    private static final String DI_DIV_ATTEND = "0";
    private static final String DI_DIV_LATE = "1";
    private static final String DI_DIV_EARLY = "2";
    private static final String DI_DIV_SICK = "3";
    private static final String DI_DIV_SUSPEND_MOURNING = "4";
    
    private static final int DI_CD_ATTEND = 0;
    private static final int DI_CD_ABSENCE = 1;
    private static final int DI_CD_SUSPEND = 2;
    private static final int DI_CD_MOURNING = 3;
    private static final int DI_CD_SICK = 4;
    private static final int DI_CD_NOTICE = 5;
    private static final int DI_CD_NONOTICE = 6;
    private static final int DI_CD_ABSENCE_ALL = 8;
    private static final int DI_CD_SUSPEND_ALL = 9;
    private static final int DI_CD_MOURNING_ALL = 10;
    private static final int DI_CD_SICK_ALL = 11;
    private static final int DI_CD_NOTICE_ALL = 12;
    private static final int DI_CD_NONOTICE_ALL = 13;
    private static final int DI_CD_NURSEOFF = 14;
    private static final int DI_CD_LATE = 15;
    private static final int DI_CD_EARLY = 16;
    private static final int DI_CD_VIRUS = 19;
    private static final int DI_CD_VIRUS_ALL = 20;
    private static final int DI_CD_LATE2 = 23;
    private static final int DI_CD_LATE3 = 24;
    private static final int DI_CD_KOUDOME = 25;
    private static final int DI_CD_KOUDOME_ALL = 26;
    private static final int DI_CD_29_KEKKA_CHIKOKU = 29;
    private static final int DI_CD_30_KEKKA_SOUTAI = 30;
    private static final int DI_CD_31_KEKKA_CHIKOKU_SOUTAI = 31;
    private static final int DI_CD_32_SHUSSEKI_CHIKOKU_SOUTAI = 32;
    
    private static final int[] suspendOnlyCds = new int[] {DI_CD_SUSPEND, DI_CD_SUSPEND_ALL}; // 出停のコード
    private static final int[] virusCds = {DI_CD_VIRUS, DI_CD_VIRUS_ALL}; // 伝染病のコード
    private static final int[] koudomeCds = {DI_CD_KOUDOME, DI_CD_KOUDOME_ALL}; // 交止のコード
    private static final int[] mourningCds = new int[]{DI_CD_MOURNING, DI_CD_MOURNING_ALL}; // 忌引のコード
    private static final int[] sickCds = new int[]{DI_CD_SICK, DI_CD_NOTICE, DI_CD_NONOTICE, DI_CD_SICK_ALL, DI_CD_NOTICE_ALL, DI_CD_NONOTICE_ALL}; // 欠課のコード
    
    private static final int[] inClassCds = new int[]{DI_CD_ATTEND, DI_CD_NURSEOFF, DI_CD_LATE, DI_CD_EARLY, DI_CD_LATE2, DI_CD_LATE3}; // 1日出欠での出席扱いコード
    
    private static final String KEY_SUSPEND = "SUSPEND";
    private static final String KEY_MOURNING = "MOURNING";
    private static final String KEY_LATE = "LATE";
    private static final String KEY_EARLY = "EARLY";
    private static final String KEY_SICK = "SICK";
    
    private static final BigDecimal zero = new BigDecimal(0);
    
    private boolean nonedata;               //該当データなしフラグ
    
    /**
     * HTTP Get リクエストの処理
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {
        final KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();  //帳票におけるＳＶＦおよびＤＢ２の設定

        final Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;              // Databaseクラスを継承したクラス

        // print svf設定
        sd.setSvfInit(request, response, svf);

        // ＤＢ接続
        db2 = sd.setDb(request);
        if (sd.openDb(db2)) {
            log.error("db open error");
            return;
        }
        Param param = getParam(db2, request);
        setSuspendCd(param);
        
        // 印刷処理
        printSvf(db2, param, svf);
        
        // 終了処理
        sd.closeSvf(svf, nonedata);
        sd.closeDb(db2);
    }

    private void setSuspendCd(final Param param) {
        final List list = new ArrayList();
        for (int i = 0; i < suspendOnlyCds.length; i++) {
            list.add(new Integer(suspendOnlyCds[i]));
        }
        if ("true".equals(param._useVirus)) {
            for (int i = 0; i < virusCds.length; i++) {
                list.add(new Integer(virusCds[i]));
            }
        }
        if ("true".equals(param._useKoudome)) {
            for (int i = 0; i < koudomeCds.length; i++) {
                list.add(new Integer(koudomeCds[i]));
            }
        }
        final int[] array = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = ((Integer) list.get(i)).intValue();
        }
        // log.debug(" suspendcds = " + ArrayUtils.toString(array));
        param.suspendCds = array;
    }

    /*
     *  印刷処理
     */
    private void printSvf(final DB2UDB db2,final Param param, final Vrw32alp svf) {
        printSvfHead(db2, param, svf);              //見出し出力のメソッド
        
        final List studentList = Student.getStudentList(db2, param);

        setSubclassList(db2, param, studentList);
        setKindMap(db2, param, studentList);

        for(int i = 0 ; i < studentList.size(); i++) {
            //遅刻・早退・欠課のSQL

            final Student student = (Student) studentList.get(i);             //学籍データ出力のメソッド

            //学籍のSQL
            //組名称の編集
            if (StringUtils.defaultString(student._hrname).lastIndexOf("組") > -1) {
                svf.VrsOut("HR_NAME", student._hrname + StringUtils.defaultString(student._attendno) + "番");        //組名称&出席番号
            } else {
                svf.VrsOut("HR_NAME", StringUtils.defaultString(student._hrname) + "-" + StringUtils.defaultString(student._attendno) + "番");  //組名称&出席番号
            }
            if ("1".equals(param._use_SchregNo_hyoji)) {
                svf.VrsOut("SCHREGNO", student._schregno);     //学籍番号
                svf.VrsOut("name2", student._name);     //氏名
            } else  {
                svf.VrsOut("name", student._name);     //氏名
            }

            printSvfAttendSubclass(param, svf, student);   //校時出欠データ出力のメソッド svf.VrEndRecord()
            printSvfAttendDay2(param, svf, student);        //１日出欠データ出力のメソッド svf.VrEndRecord()
        }
    }

    /*
     *  印刷処理 見出し出力
     */
    private void printSvfHead(final DB2UDB db2, final Param param, final Vrw32alp svf) {
        ////  ペナルティー欠課換算処理をする場合、集計開始日は学期開始日とする
        //if (definecode.absent_cov != 0) setScopeDate(db2);

        svf.VrSetForm("KNJC110.frm", 4);
        svf.VrAttribute("HR_NAME","FF=1");

        svf.VrsOut("PERIOD", KNJ_EditDate.getAutoFormatDate(db2, param._date1) + " \uFF5E " + KNJ_EditDate.getAutoFormatDate(db2, param._date2));  //集計期間

		if (!param._isPrintKetsuji && param._absent_cov != 0 ) {
            svf.VrsOut("NOTE1", "同一科目を" + param._absent_cov_late + "回遅刻・早退すると1回欠課の扱いとする");
            svf.VrsOut("NOTE2", "集計期間は" + ((param._absent_cov == 1 || param._absent_cov == 3) ? "学期" : "年度") + "の始まりを開始日とする");
        }

        //  作成日(現在処理日)の取得
        KNJ_Control control = new KNJ_Control();
        KNJ_Control.ReturnVal returnval = control.Control(db2);
        svf.VrsOut("ymd"  ,KNJ_EditDate.getAutoFormatDate(db2, returnval.val3));             //作成日
    }

    private static class Student {
        final String _schregno;
        final String _name;
        final String _hrname;
        final String _attendno;
        private List _subclassList = Collections.EMPTY_LIST;
        private Map _kindMap = Collections.EMPTY_MAP;
        public Student(final String schregno, final String name, final String hrname, final String attendno) {
            _schregno = schregno;
            _name = name;
            _hrname = hrname;
            _attendno = attendno;
        }
        
        /*
         *  印刷処理 生徒名出力
         */
        public static List getStudentList(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            //SQL作成
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                //学籍のSQL
                final String sql = prestatementRegd(param);
//                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                for (int i = 0; i < param._categorySelected.length; i++) {
                    ps.setString(1, param._categorySelected[i]);
                    rs = ps.executeQuery();
                    if (rs.next()) {
                        Student student = new Student(rs.getString("SCHREGNO"), rs.getString("NAME"), rs.getString("HR_NAME"), rs.getString("ATTENDNO"));
                        list.add(student);
                    }
                }
            } catch (Exception ex) {
                log.error("[KNJC110]printSvfRegd error!", ex);
            } finally{
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        /*
         *  preparedStatement作成  学籍データ
         *    該当生徒の前学期のデータを降順で取得する => 最初のレコードだけ使用する
         */
        private static String prestatementRegd(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append("SELECT ");
            stb.append("    W1.SCHREGNO, W1.NAME, W3.HR_NAME, INT(W2.ATTENDNO)AS ATTENDNO ");
            stb.append("FROM ");
            stb.append("    SCHREG_BASE_MST W1");
            stb.append("    INNER JOIN SCHREG_REGD_DAT W2 ON W1.SCHREGNO = W2.SCHREGNO ");
            stb.append("    INNER JOIN SCHREG_REGD_HDAT W3 ON W2.YEAR = W3.YEAR AND W2.SEMESTER = W3.SEMESTER AND W2.GRADE = W3.GRADE AND W2.HR_CLASS = W3.HR_CLASS ");
            stb.append("WHERE ");
            stb.append("       W1.SCHREGNO = ? AND ");
            stb.append(       "W2.YEAR = '" + param._year + "' ");
            stb.append("ORDER BY W2.SEMESTER DESC");
            return stb.toString();
        }
    }

    /*
     *  印刷処理 出欠データ出力
     *    校時（科目）の出欠状況を出力
     */
    private void printSvfAttendSubclass(final Param param, final Vrw32alp svf, final Student student) {
        final Set diDivSet = getDiDivSet(student._subclassList);
        diDivSet.remove(DI_DIV_ATTEND);
        diDivSet.remove(DI_DIV_SUSPEND_MOURNING);

        try {
            for (final Iterator diit = diDivSet.iterator(); diit.hasNext();) {
                final String diDiv = (String) diit.next();
                
                svf.VrsOut("KINTAI", kintaiName(param, diDiv));
                
                final DiRecord diRec = new DiRecord(diDiv);
                diRec._subclassList = getDiCdSubclassList(diDiv, student._subclassList, param);
                
                svf.VrsOut("total",  String.valueOf(diRec.getTotalCount(param)));
                svf.VrEndRecord();
                
                for (final Iterator sit = diRec._subclassList.iterator(); sit.hasNext();) {
                    final Subclass subclass = (Subclass) sit.next();
                     
                    svf.VrsOut("subject1", subclass._name);

                    final BigDecimal count = DiRecord.getSubclassCountWithKansan(diDiv, subclass, param);

                    final int scale = (param._absent_cov == 3 || param._absent_cov == 4) ? 1 : 0;
                    final String countStr = count.setScale(scale, BigDecimal.ROUND_HALF_UP).toString();
                    svf.VrsOut("late_no",  countStr);
                    final int totalCount =  ("2".equals(param._outputjisuu)) ? subclass.getMLesson(param) : subclass.getLesson();
                    svf.VrsOut("lesson", String.valueOf(totalCount));

                    log.debug(diRec._diDiv + "  (" + subclass + ") " + " = " + countStr + "時間 (" + count + "), " + totalCount + "回");

                    boolean hasData = false;
                    int scount = 0;
                    for (final Iterator dit = diRec.getDateSet(subclass).iterator(); dit.hasNext();) {
                        final String date = (String) dit.next();
                        if (date != null) {
                            scount += 1;
                            svf.VrsOut("la" + scount, formatDate(date)); //月日
                            if (5 <= scount) {
                                svf.VrEndRecord();
                                scount = 0;
                            }
                        }
                        hasData = true;
                    }
                    if (!hasData || 0 < scount) {
                        svf.VrEndRecord();
                        nonedata = true;
                    }
                }
            }

        } catch (Exception ex) {
            log.error("[KNJC110]printSvfAttendSubclass error!", ex);
        }
    }
    
    private List getDiCdSubclassList(final String diDiv, final List subclassList, final Param param) {
        final Set col = new TreeSet();
        for (final Iterator it = subclassList.iterator(); it.hasNext();) {
            final Subclass subclass = (Subclass) it.next();
            for (final Iterator itd = subclass._subclassRec.iterator(); itd.hasNext();) {
                final SubclassRec subclassRec = (SubclassRec) itd.next();
                if (diDiv.equals(subclassRec._diDiv)) {
                    col.add(subclass);
                }
            }
            if (subclass._penalty.doubleValue() != 0.0) {
                boolean add = false;
                if ((DI_DIV_EARLY.equals(diDiv) || DI_DIV_LATE.equals(diDiv))) {
                    final BigDecimal kansan = DiRecord.getSubclassCountWithKansan(diDiv, subclass, param);
                    if (null != kansan && kansan.doubleValue() > 0.0) {
                        add = true;
                    }
                } else if (DI_DIV_SICK.equals(diDiv)) {
                    add = true;
                }
                if (add) {
                    col.add(subclass);
                }
            }
        }
        return new ArrayList(col);
    }

    private Set getDiDivSet(final List subclassList) {
        final Set rtn = new TreeSet();
        for (final Iterator it = subclassList.iterator(); it.hasNext();) {
            final Subclass subclass = (Subclass) it.next();
            for (final Iterator itd = subclass._subclassRec.iterator(); itd.hasNext();) {
                final SubclassRec subclassRec = (SubclassRec) itd.next();
                rtn.add(subclassRec._diDiv);
            }
            if (subclass._penalty.doubleValue() != 0.0) {
                rtn.add(DI_DIV_SICK);
            }
        }
        return rtn;
    }

    private void setSubclassList(final DB2UDB db2, final Param param, final List studentList) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql = prestatementDetail(param);
            log.debug(" sql = " + sql);
            
            ps = db2.prepareStatement(sql);

            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                
                ps.setString(1, student._schregno);
                rs = ps.executeQuery();
                
                student._subclassList = new ArrayList();

                while (rs.next()) {
                    
                    String keyCd;
                    if ("1".equals(param._useCurriculumcd)) {
                        keyCd = rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-" + rs.getString("CURRICULUM_CD") + "-" + rs.getString("SUBCLASSCD");
                    } else {
                        keyCd = rs.getString("SUBCLASSCD");
                    }
                    if ("2".equals(param._outputname)) {
                        keyCd = keyCd + "-" + rs.getString("CHAIRCD");
                    }

                    if (null == Subclass.getSubclass(keyCd, student._subclassList)) {
                        final String keyname;
                        if ("2".equals(param._outputname)) {
                            keyname = rs.getString("CHAIRNAME");
                        } else {
                            keyname = rs.getString("SUBCLASSNAME");
                        }
                        student._subclassList.add(new Subclass(keyCd, keyname));
                    }
                    
                    final Subclass subclass = Subclass.getSubclass(keyCd, student._subclassList);
                    
                    final String semester = rs.getString("SEMESTER");
                    final String chaircd = rs.getString("CHAIRCD");
                    final String chairname = rs.getString("CHAIRNAME");
                    final String executedate = rs.getString("EXECUTEDATE");
                    final String periodcd = rs.getString("PERIODCD");
                    final String diDiv = rs.getString("DI_CD");
                    final String diCd = rs.getString("DI_CD0");
                    final int count = rs.getInt("COUNT");

                    final SubclassRec subclassRec = new SubclassRec(semester, chaircd, chairname, executedate, periodcd, diDiv, diCd, count);
                    subclass._subclassRec.add(subclassRec);
                    
                }
            }

        } catch (Exception ex) {
            log.error("[KNJC110]printSvfAttendSubclass error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        
        // 欠課換算
        if (!param._isPrintKetsuji && 0 != param._absent_cov && 0 != param._absent_cov_late) {
            
            for (final Iterator itst = studentList.iterator(); itst.hasNext();) {
                final Student student = (Student) itst.next();

                for (final Iterator it = student._subclassList.iterator(); it.hasNext();) {
                    final Subclass subclass = (Subclass) it.next();
                    
                    final Map termMap = new HashMap(); // <semester, List<SubclassRec>>
                    
                    for (final Iterator itr = subclass._subclassRec.iterator(); itr.hasNext();) {
                        final SubclassRec subRec = (SubclassRec) itr.next();
                        
                        String key = "";
                        if (param._absent_cov == 1 || param._absent_cov == 3) {
                            // 学期ごとに換算
                            key = subRec._semester;
                        } else if (param._absent_cov == 2 || param._absent_cov == 4) { 
                            // 年間で換算
                            key = "9";
                        } else {
                            key = "N";
                        }
                        if (null == termMap.get(key)) {
                            termMap.put(key, new ArrayList()); 
                        }
                        final List termSubclassRecList = (List) termMap.get(key);
                        termSubclassRecList.add(subRec);
                    }
                    
                    for (final Iterator its = termMap.keySet().iterator(); its.hasNext();) {
                        final String key = (String) its.next();
                        final List termSubclassList = (List) termMap.get(key);

                        int late = 0;
                        int early = 0;
                        
                        for (final Iterator itr = termSubclassList.iterator(); itr.hasNext();) {
                            final SubclassRec subRec = (SubclassRec) itr.next();
                            if (DI_DIV_LATE.equals(subRec._diDiv)) {
                                late += subRec._count;
                            } else if (DI_DIV_EARLY.equals(subRec._diDiv)) {
                                early += subRec._count;
                            }
                        }

                        BigDecimal penalty = null;
                        if (param._absent_cov == 1 || param._absent_cov == 2) {
                            // 整数
                            final int p = (late + early) / param._absent_cov_late;
                            penalty = new BigDecimal(p);
                            
                        } else if (param._absent_cov == 3 || param._absent_cov == 4) {
                            // 実数
                            penalty = new BigDecimal(late + early).divide(new BigDecimal(param._absent_cov_late), 0, BigDecimal.ROUND_DOWN);
                        }
                        if (null != penalty) {
                            subclass._penalty = subclass._penalty.add(penalty);

                            final BigDecimal lateearlyKansan = penalty.multiply(new BigDecimal(param._absent_cov_late));
                            subclass._lateEarlyKansan = subclass._lateEarlyKansan.add(lateearlyKansan);
                        }
                    }
                }
            }
        }
    }

    private String kintaiName(final Param param, final String diDiv) {
        String name = "";
        if (DI_DIV_LATE.equals(diDiv)) {
            name = "遅刻";
        } else if (DI_DIV_EARLY.equals(diDiv)) {
            name = "早退";
        } else if (DI_DIV_SICK.equals(diDiv)) {
            if (param._isPrintKetsuji) {
                name = "欠時";
            } else {
                name = "欠課";
            }
        }
        return name;
    }
    
    private static String formatDate(final String date) {
        final Calendar cal1 = Calendar.getInstance();
        cal1.setTime(Date.valueOf(date));
        return String.valueOf(cal1.get(Calendar.MONTH) + 1 ) + "/" + String.valueOf(cal1.get(Calendar.DATE)) + "(" + KNJ_EditDate.h_format_W(date) + ")";
    }
    
    private void printKindLine(final Vrw32alp svf, final String title, final List kindList) {
        svf.VrsOut("subject1", title);
        log.debug("title = " + title);
        svf.VrsOut("late_no", String.valueOf(kindList.size()));
        int scount = 0;
        for (final Iterator it = kindList.iterator(); it.hasNext();) {
            final String date = (String) it.next();
            
            if (5 <= scount) {
                svf.VrEndRecord();
                nonedata = true;
                scount = 0;
            }
            scount++;

            // log.debug("     date >>> " + date);
            final String outputString = KNJ_EditDate.h_format_S(date, "M") + "/" + KNJ_EditDate.h_format_S(date,"d") + "(" + KNJ_EditDate.h_format_W(date) +")";
            // log.debug("outputString = " + outputString);
            svf.VrsOut("la" + scount  ,outputString); //月日
        }
        if (0 < scount) {
            svf.VrEndRecord();
            nonedata = true;
            scount = 0;
        }
    }

    /*
     *  印刷処理 出欠データ出力
     *    １日の出欠状況を出力
     */
    private void printSvfAttendDay2(final Param param, final Vrw32alp svf, final Student student) {
        try {
            svf.VrsOut("kintai",  "１日");

            final List suspends = (List) student._kindMap.get(KEY_SUSPEND);
            if (suspends.size() != 0) {
                printKindLine(svf, "出停", suspends);
            }
            final List mournings = (List) student._kindMap.get(KEY_MOURNING);
            if (mournings.size() != 0) {
                printKindLine(svf, "忌引", mournings);
            }
            final List sicks = (List) student._kindMap.get(KEY_SICK);
            if (sicks.size() != 0) {
                printKindLine(svf, "欠席", sicks);
            }
            final List lates = (List) student._kindMap.get(KEY_LATE);
            if (lates.size() != 0) {
                printKindLine(svf, "遅刻", lates);
            }
            final List earlys = (List) student._kindMap.get(KEY_EARLY);
            if (earlys.size() != 0) {
                printKindLine(svf, "早退", earlys);
            }

        } catch (Exception ex) {
            log.error("[KNJC110]printSvfAttendDay2 error!", ex);
        }
    }

    private void setKindMap(final DB2UDB db2, final Param param, final List studentList) {

        final String sql = prestatementDateAttend(param);
        log.debug("sql dateAttend " + sql);
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(sql);

            for (final Iterator itst = studentList.iterator(); itst.hasNext();) {
                final Student student = (Student) itst.next();
                
                student._kindMap = new HashMap();
                final TreeMap onedayKintaiMap = new TreeMap();
            
                ps.setString(1, student._schregno);
                ps.setString(2, student._schregno);
                ps.setString(3, student._schregno);
                rs = ps.executeQuery();
                student._kindMap.put(KEY_SUSPEND, new ArrayList());
                student._kindMap.put(KEY_MOURNING, new ArrayList());
                student._kindMap.put(KEY_SICK, new ArrayList());
                student._kindMap.put(KEY_LATE, new ArrayList());
                student._kindMap.put(KEY_EARLY, new ArrayList());

                while (rs.next()) {
                    final String date = rs.getString("EXECUTEDATE");
                    final Integer diCd = Integer.valueOf(rs.getString("DI_CD"));
                    final boolean isAbroad = "1".equals(rs.getString("IS_ABROAD"));
                    final boolean isOffdays = "1".equals(rs.getString("IS_OFFDAYS"));
                    final OnedayKintai onedayKintai = getOnedayKintai(date, param, onedayKintaiMap);
                    onedayKintai._diCds.add(diCd);
                    onedayKintai._isAbroad = onedayKintai._isAbroad || isAbroad;
                    onedayKintai._isOffdays = onedayKintai._isOffdays || isOffdays;
                }

                for (final Iterator it = onedayKintaiMap.keySet().iterator(); it.hasNext();) {
                    final String date = (String) it.next();
                    final OnedayKintai onedayKintai = (OnedayKintai) onedayKintaiMap.get(date);
                    //log.info(onedayKintai.toString());
                    
                    final boolean isOnedaySuspend = onedayKintai.isOnedaySuspend();
                    if (isOnedaySuspend) {
                        ((List) student._kindMap.get(KEY_SUSPEND)).add(date);
                    }
                    final boolean isOnedayMourning = onedayKintai.isOnedayMourning();
                    if (isOnedayMourning) {
                        ((List) student._kindMap.get(KEY_MOURNING)).add(date);
                    }
                    if (!(isOnedaySuspend || isOnedayMourning)) {
                        if (onedayKintai.isOnedaySick()) {
                            ((List) student._kindMap.get(KEY_SICK)).add(date);
                        }
                        if (onedayKintai.isOnedayLate()) {
                            ((List) student._kindMap.get(KEY_LATE)).add(date);
                        }
                        if (onedayKintai.isOnedayEarly()) {
                            ((List) student._kindMap.get(KEY_EARLY)).add(date);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.fatal("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    /*
     *  preparedStatement作成  遅刻・早退・欠課
     *    時間割データにリンクした出欠データの表
     */
    private String prestatementDetail(final Param param) {
        final StringBuffer stb = new StringBuffer();
        // 時間割 (休学・留学を含む)
        stb.append(" WITH ");
        stb.append("  TEST_COUNTFLG AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.EXECUTEDATE, ");
        stb.append("         T1.PERIODCD, ");
        stb.append("         T1.CHAIRCD, ");
        stb.append("         '2' AS DATADIV ");
        stb.append("     FROM ");
        stb.append("         SCH_CHR_TEST T1, ");
        if ("TESTITEM_MST_COUNTFLG".equals(param._useTestCountflg)) {
            stb.append("         TESTITEM_MST_COUNTFLG T2 ");
            stb.append("     WHERE ");
            stb.append("         T2.YEAR       = T1.YEAR ");
        } else if ("TESTITEM_MST_COUNTFLG_NEW_SDIV".equals(param._useTestCountflg)) {
            stb.append("         TESTITEM_MST_COUNTFLG_NEW_SDIV T2 ");
            stb.append("     WHERE ");
            stb.append("         T2.YEAR       = T1.YEAR ");
            stb.append("         AND T2.SEMESTER   = T1.SEMESTER ");
            stb.append("         AND T2.SCORE_DIV  = '01' ");
        } else {
            stb.append("         TESTITEM_MST_COUNTFLG_NEW T2 ");
            stb.append("     WHERE ");
            stb.append("         T2.YEAR       = T1.YEAR ");
            stb.append("         AND T2.SEMESTER   = T1.SEMESTER ");
        }
        stb.append("         AND T2.TESTKINDCD = T1.TESTKINDCD ");
        stb.append("         AND T2.TESTITEMCD = T1.TESTITEMCD ");
        stb.append("         AND T2.COUNTFLG   = '0' ");
        stb.append(" ) ,SCHREG_SCHEDULE_R AS(");
        stb.append("   SELECT T2.SCHREGNO, T1.EXECUTEDATE, T1.PERIODCD");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("       ,T3.CLASSCD ");
            stb.append("       ,T3.SCHOOL_KIND ");
            stb.append("       ,T3.CURRICULUM_CD ");
        }
        stb.append("          ,T3.SUBCLASSCD, S3.SUBCLASSNAME, T3.CHAIRCD, T3.CHAIRNAME, S2.DI_CD, T1.SEMESTER");
        stb.append("    FROM   SCH_CHR_DAT T1");
        stb.append("    INNER JOIN CHAIR_STD_DAT T2 ON T1.YEAR = T2.YEAR");
        stb.append("       AND T1.SEMESTER = T2.SEMESTER");
        stb.append("       AND T1.CHAIRCD = T2.CHAIRCD");
        stb.append("       AND T2.SCHREGNO = ? ");
        stb.append("       AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE");
        stb.append("    INNER JOIN CHAIR_DAT T3 ON T1.YEAR = T3.YEAR");
        stb.append("       AND T1.SEMESTER = T3.SEMESTER");
        stb.append("       AND T1.CHAIRCD = T3.CHAIRCD");
        stb.append("    LEFT JOIN ATTEND_DAT S2 ON T1.YEAR = S2.YEAR ");
        stb.append("       AND S2.ATTENDDATE = T1.EXECUTEDATE");
        stb.append("       AND S2.PERIODCD = T1.PERIODCD");
        stb.append("       AND S2.SCHREGNO = T2.SCHREGNO");
        stb.append("    LEFT JOIN SUBCLASS_MST S3 ON ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("       S3.CLASSCD = T3.CLASSCD AND ");
            stb.append("       S3.SCHOOL_KIND = T3.SCHOOL_KIND AND ");
            stb.append("       S3.CURRICULUM_CD = T3.CURRICULUM_CD AND ");
        }
        stb.append("       S3.SUBCLASSCD = T3.SUBCLASSCD ");
        stb.append("    WHERE  T1.YEAR = '" + param._year + "'");
        stb.append("       AND T1.EXECUTEDATE BETWEEN DATE('" + param._date1 + "') AND DATE('" + param._date2 + "')");
        if (param.definecode.useschchrcountflg) {
            //                       COUNTFLGによる制限
            stb.append("       AND NOT EXISTS(SELECT 'X' FROM SCH_CHR_COUNTFLG T4");
            stb.append("                      WHERE  T4.EXECUTEDATE = T1.EXECUTEDATE");
            stb.append("                         AND T4.PERIODCD =T1.PERIODCD");
            stb.append("                         AND T4.CHAIRCD = T1.CHAIRCD");
            stb.append("                         AND T1.DATADIV IN ('0', '1') ");
            stb.append("                         AND T4.GRADE = '" + param._gradehrclass.substring(0, 2) + "'");
            stb.append("                         AND T4.HR_CLASS = '" + param._gradehrclass.substring(2) + "'");
            stb.append("                         AND T4.COUNTFLG = '0')");
            stb.append("       AND NOT EXISTS(SELECT 'X' FROM TEST_COUNTFLG TEST ");
            stb.append("                       WHERE ");
            stb.append("                           TEST.EXECUTEDATE  = T1.EXECUTEDATE ");
            stb.append("                           AND TEST.PERIODCD = T1.PERIODCD ");
            stb.append("                           AND TEST.CHAIRCD  = T1.CHAIRCD ");
            stb.append("                           AND TEST.DATADIV  = T1.DATADIV) ");
        }
        //                           学籍異動による制限
        stb.append("       AND NOT EXISTS(SELECT 'X' FROM SCHREG_BASE_MST T4");
        stb.append("                     WHERE  T4.SCHREGNO = T2.SCHREGNO ");
        stb.append("                        AND ((ENT_DIV IN('4','5') AND EXECUTEDATE < ENT_DATE)");
        stb.append("                          OR (GRD_DIV IN('2','3') AND EXECUTEDATE > GRD_DATE)))");
        stb.append("    AND NOT EXISTS(SELECT ");
        stb.append("                       'X' ");
        stb.append("                   FROM ");
        stb.append("                       ATTEND_DAT T4 ");
        stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT ADCD ON ADCD.YEAR = T4.YEAR AND ADCD.DI_CD = T4.DI_CD ");
        stb.append("                   WHERE ");
        stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
        stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
        stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
        stb.append("                       AND ADCD.REP_DI_CD = '27' "); // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
        stb.append("                  ) ");
        // 勤怠コード'28'は時間割にカウントしない
        stb.append("    AND NOT EXISTS(SELECT ");
        stb.append("                       'X' ");
        stb.append("                   FROM ");
        stb.append("                       ATTEND_DAT T4 ");
        stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT ADCD ON ADCD.YEAR = T4.YEAR AND ADCD.DI_CD = T4.DI_CD ");
        stb.append("                   WHERE ");
        stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
        stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
        stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
        stb.append("                       AND ADCD.REP_DI_CD = '28' ");
        stb.append("                  ) ");
        if (param._hasSchChrDatExecutediv) {
            stb.append("        AND VALUE(T1.EXECUTEDIV, '0') <> '2' "); // 休講は対象外
        }
        stb.append("   GROUP BY T2.SCHREGNO, T1.EXECUTEDATE, T1.PERIODCD ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("       ,T3.CLASSCD ");
            stb.append("       ,T3.SCHOOL_KIND ");
            stb.append("       ,T3.CURRICULUM_CD ");
        }
        stb.append("           ,T3.SUBCLASSCD, S3.SUBCLASSNAME, T3.CHAIRCD, T3.CHAIRNAME, T1.SEMESTER, S2.DI_CD ");
        stb.append(" ) ");
        // 時間割 (休学・留学を含まない)
        stb.append(" , SCHREG_SCHEDULE AS ( ");
        stb.append("   SELECT T1.SCHREGNO, T1.EXECUTEDATE, T1.PERIODCD ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("       ,T1.CLASSCD ");
            stb.append("       ,T1.SCHOOL_KIND ");
            stb.append("       ,T1.CURRICULUM_CD ");
        }
        stb.append("        , T1.SUBCLASSCD, T1.DI_CD, T1.SEMESTER");
        stb.append("    FROM SCHREG_SCHEDULE_R T1 ");
        stb.append("    WHERE NOT EXISTS(SELECT 'X' FROM SCHREG_TRANSFER_DAT T4");
        stb.append("                WHERE  T4.SCHREGNO = T1.SCHREGNO ");
        stb.append("                AND (TRANSFERCD IN('1','2') AND EXECUTEDATE BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE ))");
        stb.append(" ) ");
        // 時間割 (休学数)
        stb.append(" , SCHREG_OFFDAYS AS ( ");
        stb.append("   SELECT T1.SCHREGNO, T1.EXECUTEDATE, T1.PERIODCD ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("       ,T1.CLASSCD ");
            stb.append("       ,T1.SCHOOL_KIND ");
            stb.append("       ,T1.CURRICULUM_CD ");
        }
        stb.append("        , T1.SUBCLASSCD, '3' AS DI_CD, T1.SEMESTER");
        stb.append("    FROM SCHREG_SCHEDULE_R T1 ");
        stb.append("    WHERE EXISTS(SELECT 'X' FROM SCHREG_TRANSFER_DAT T4");
        stb.append("                WHERE  T4.SCHREGNO = T1.SCHREGNO ");
        stb.append("                AND (TRANSFERCD = '2' AND EXECUTEDATE BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE ))");
        stb.append(" ) ");

        stb.append(" , ATTEND_DETAIL AS(");
        stb.append("   SELECT T1.SCHREGNO, T1.SEMESTER ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("       ,T1.CLASSCD ");
            stb.append("       ,T1.SCHOOL_KIND ");
            stb.append("       ,T1.CURRICULUM_CD ");
        }
        stb.append("         ,T1.SUBCLASSCD, T1.SUBCLASSNAME, T1.CHAIRCD, T1.CHAIRNAME, T1.EXECUTEDATE, T1.PERIODCD,");
        stb.append("          CASE WHEN ADCD.REP_DI_CD in ('7','15','23','24') THEN '" + DI_DIV_LATE + "' ");
        stb.append("               WHEN ADCD.REP_DI_CD = '16' THEN '" + DI_DIV_EARLY + "' ");
        stb.append("               WHEN ADCD.REP_DI_CD in ('ARIENAI' ");
        if (!"1".equals(param.knjSchoolMst._subSuspend)) {
            stb.append(          ",'2','9'");
        }
        if (!"1".equals(param.knjSchoolMst._subMourning)) {
            stb.append(          ",'3','10'");
        }
        if (!"1".equals(param.knjSchoolMst._subVirus)) {
            stb.append(          ",'19','20'");
        }
        if (!"1".equals(param.knjSchoolMst._subKoudome)) {
            stb.append(          ",'25','26'");
        }
        stb.append("                                )");
        stb.append("                    THEN '" + DI_DIV_SUSPEND_MOURNING + "' ");
        stb.append("               WHEN (CASE WHEN ADCD.REP_DI_CD IN ('29','30','31') THEN VALUE(ADCD.ATSUB_REPL_DI_CD, ADCD.REP_DI_CD) ELSE ADCD.REP_DI_CD END) in ('4','5','6','14','11','12','13'");
        if ("1".equals(param.knjSchoolMst._subAbsent)) {
            stb.append(          ",'1','8'");
        }
        if ("1".equals(param.knjSchoolMst._subSuspend)) {
            stb.append(          ",'2','9'");
        }
        if ("1".equals(param.knjSchoolMst._subMourning)) {
            stb.append(          ",'3','10'");
        }
        if ("1".equals(param.knjSchoolMst._subVirus)) {
            stb.append(          ",'19','20'");
        }
        if ("1".equals(param.knjSchoolMst._subKoudome)) {
            stb.append(          ",'25','26'");
        }
        stb.append("                                )");
        if ("1".equals(param.knjSchoolMst._subOffDays)) {
            stb.append("                    OR T3.DI_CD IS NOT NULL ");
        }
        stb.append("                    THEN '" + DI_DIV_SICK + "' ");
        stb.append("               ELSE '" + DI_DIV_ATTEND + "' END AS DI_CD,");
        stb.append("               T1.DI_CD AS DI_CD0 ");
        stb.append("   FROM SCHREG_SCHEDULE_R T1 ");
        stb.append("   LEFT JOIN SCHREG_SCHEDULE T2 ON T1.SCHREGNO = T2.SCHREGNO ");
        stb.append("       AND T1.SEMESTER = T2.SEMESTER ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("       AND T1.CLASSCD = T2.CLASSCD ");
            stb.append("       AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ");
            stb.append("       AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ");
        }
        stb.append("       AND T1.SUBCLASSCD = T2.SUBCLASSCD ");
        stb.append("       AND T1.EXECUTEDATE = T2.EXECUTEDATE ");
        stb.append("       AND T1.PERIODCD = T2.PERIODCD ");
        stb.append("       AND T1.DI_CD = T2.DI_CD ");
        stb.append("   LEFT JOIN SCHREG_OFFDAYS T3 ON T1.SCHREGNO = T3.SCHREGNO ");
        stb.append("       AND T1.SEMESTER = T3.SEMESTER ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("       AND T1.CLASSCD = T2.CLASSCD ");
            stb.append("       AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ");
            stb.append("       AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ");
        }
        stb.append("       AND T1.SUBCLASSCD = T3.SUBCLASSCD ");
        stb.append("       AND T1.EXECUTEDATE = T3.EXECUTEDATE ");
        stb.append("       AND T1.PERIODCD = T3.PERIODCD ");
        stb.append("   LEFT JOIN ATTEND_DI_CD_DAT ADCD ON ADCD.YEAR = '" + param._year + "' AND ADCD.DI_CD = T1.DI_CD ");
        stb.append(" ) ");
        stb.append("   SELECT T1.SCHREGNO, T1.SEMESTER ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("       ,T1.CLASSCD ");
            stb.append("       ,T1.SCHOOL_KIND ");
            stb.append("       ,T1.CURRICULUM_CD ");
        }
        stb.append("         ,T1.SUBCLASSCD, T1.SUBCLASSNAME, T1.CHAIRCD, T1.CHAIRNAME, T1.EXECUTEDATE, T1.PERIODCD,");
        stb.append("          T1.DI_CD,");
        stb.append("          T1.DI_CD0, ");
        stb.append("          INT(CASE WHEN T1.DI_CD = '" + DI_DIV_LATE + "' THEN VALUE(ADCD.MULTIPLY, '1') ELSE '1' END) AS COUNT ");
        stb.append("   FROM ATTEND_DETAIL T1 ");
        stb.append("   LEFT JOIN ATTEND_DI_CD_DAT ADCD ON ADCD.YEAR = '" + param._year + "' AND ADCD.DI_CD = T1.DI_CD0 ");
        stb.append("   ORDER BY T1.SCHREGNO ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("       ,T1.CLASSCD ");
            stb.append("       ,T1.SCHOOL_KIND ");
            stb.append("       ,T1.CURRICULUM_CD ");
        }
        stb.append("       ,T1.SUBCLASSCD, T1.SEMESTER, T1.EXECUTEDATE, T1.PERIODCD ");
        return stb.toString();
    }

    /*
     *  preparedStatement作成  １日の出欠表
     */
    private String prestatementDateAttend(final Param param) {

        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH ");
        stb.append("  PERIOD_TERM AS( ");
        stb.append("   SELECT S_PERIODCD, E_PERIODCD ");
        stb.append("   FROM   COURSE_MST T1 ");
        stb.append("   WHERE  EXISTS(SELECT 'X' FROM SCHREG_REGD_DAT T2 ");
        stb.append("                 WHERE  T2.YEAR = '" + param._year+ "' ");
        stb.append("                    AND T2.SEMESTER = '" + param._semester+ "' ");
        stb.append("                    AND T2.SCHREGNO = ? ");
        stb.append("                    AND T2.COURSECD = T1.COURSECD) ");
        stb.append("), SCH_PERIOD_DATA AS ( ");
        stb.append("SELECT ");
        stb.append("    S2.SCHREGNO, ");
        stb.append("    S1.YEAR, ");
        stb.append("    S1.EXECUTEDATE, ");
        stb.append("    S1.PERIODCD, ");
        stb.append("    (CASE WHEN S4.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END) AS IS_OFFDAYS, ");
        stb.append("    (CASE WHEN S5.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END) AS IS_ABROAD ");
        stb.append("FROM ");
        stb.append("    SCH_CHR_DAT S1 ");
        stb.append("    INNER JOIN CHAIR_STD_DAT S2 ON S1.EXECUTEDATE BETWEEN S2.APPDATE AND S2.APPENDDATE ");
        stb.append("        AND S1.YEAR = S2.YEAR ");
        stb.append("        AND S1.SEMESTER = S2.SEMESTER ");
        stb.append("        AND S1.CHAIRCD = S2.CHAIRCD ");
        stb.append("    LEFT JOIN SCHREG_REGD_DAT S6 ON S6.YEAR = S2.YEAR ");
        stb.append("        AND S6.SEMESTER = S2.SEMESTER ");
        stb.append("        AND S6.SCHREGNO = S2.SCHREGNO ");
        // 留学
        stb.append("    LEFT JOIN SCHREG_TRANSFER_DAT S5 ON S1.EXECUTEDATE BETWEEN S5.TRANSFER_SDATE AND S5.TRANSFER_EDATE ");
        stb.append("        AND S5.TRANSFERCD = '1' ");
        stb.append("        AND S5.SCHREGNO = S2.SCHREGNO ");
        // 休学
        stb.append("    LEFT JOIN SCHREG_TRANSFER_DAT S4 ON S1.EXECUTEDATE BETWEEN S4.TRANSFER_SDATE AND S4.TRANSFER_EDATE ");
        stb.append("        AND S4.TRANSFERCD = '2' ");
        stb.append("        AND S4.SCHREGNO = S2.SCHREGNO ");
        stb.append("    WHERE ");
        stb.append("        S1.YEAR = '" + param._year+ "' ");
        stb.append("        AND S1.EXECUTEDATE BETWEEN DATE('" + param._date1+ "') AND DATE('" + param._date2+ "') ");
        stb.append("        AND S2.SCHREGNO = ? ");
        if (param.definecode.usefromtoperiod) {
            stb.append("        AND EXISTS (SELECT 'X' FROM PERIOD_TERM T3 WHERE S1.PERIODCD BETWEEN T3.S_PERIODCD AND T3.E_PERIODCD) ");
        }
        stb.append("        AND NOT EXISTS(SELECT 'X' FROM SCHREG_BASE_MST T3 ");
        stb.append("                     WHERE  T3.SCHREGNO = ? ");
        stb.append("                        AND ((ENT_DIV IN('4','5') AND S1.EXECUTEDATE < ENT_DATE) ");
        stb.append("                          OR (GRD_DIV IN('2','3') AND S1.EXECUTEDATE > GRD_DATE))) ");
        stb.append("    AND NOT EXISTS(SELECT ");
        stb.append("                       'X' ");
        stb.append("                   FROM ");
        stb.append("                       ATTEND_DAT T4 ");
        stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT ADCD ON ADCD.YEAR = T4.YEAR AND ADCD.DI_CD = T4.DI_CD ");
        stb.append("                   WHERE ");
        stb.append("                       T4.SCHREGNO = S2.SCHREGNO ");
        stb.append("                       AND T4.ATTENDDATE = S1.EXECUTEDATE ");
        stb.append("                       AND ADCD.REP_DI_CD = '27' "); // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
        stb.append("                  ) ");
        // 勤怠コード'28'は時間割にカウントしない
        stb.append("    AND NOT EXISTS(SELECT ");
        stb.append("                       'X' ");
        stb.append("                   FROM ");
        stb.append("                       ATTEND_DAT T4 ");
        stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT ADCD ON ADCD.YEAR = T4.YEAR AND ADCD.DI_CD = T4.DI_CD ");
        stb.append("                   WHERE ");
        stb.append("                       T4.SCHREGNO = S2.SCHREGNO ");
        stb.append("                       AND T4.ATTENDDATE = S1.EXECUTEDATE ");
        stb.append("                       AND T4.PERIODCD = S1.PERIODCD ");
        stb.append("                       AND ADCD.REP_DI_CD = '28' ");
        stb.append("                  ) ");
        if (param._hasChairDetailDat) {
            stb.append("    AND NOT EXISTS(SELECT 'X' FROM CHAIR_DETAIL_DAT L4 ");
            stb.append("                   WHERE L4.YEAR = '" + param._year + "' ");
            stb.append("                         AND L4.SEMESTER = '" + param._semester + "' ");
            stb.append("                         AND L4.CHAIRCD = S1.CHAIRCD ");
            stb.append("                         AND L4.SEQ = '001' ");
            stb.append("                         AND L4.REMARK2 = '1' "); // 1日出欠の対象外
            stb.append("                  ) ");
        }
        if (param._hasSchChrDatExecutediv) {
            stb.append("        AND VALUE(S1.EXECUTEDIV, '0') <> '2' "); // 休講は対象外
        }
        stb.append(") ");
        stb.append("SELECT ");
        stb.append("    S1.SCHREGNO, ");
        stb.append("    S1.EXECUTEDATE, ");
        stb.append("    S1.PERIODCD, ");
        stb.append("    VALUE(ADCD.REP_DI_CD,'0') AS DI_CD, ");
        stb.append("    S1.IS_OFFDAYS, ");
        stb.append("    S1.IS_ABROAD ");
        stb.append("FROM ");
        stb.append("    SCH_PERIOD_DATA S1 ");
        stb.append("    LEFT JOIN ATTEND_DAT S3 ON S3.YEAR = S1.YEAR ");
        stb.append("        AND S3.ATTENDDATE = S1.EXECUTEDATE ");
        stb.append("        AND S3.PERIODCD = S1.PERIODCD         ");
        stb.append("        AND S3.SCHREGNO = S1.SCHREGNO ");
        stb.append("    LEFT JOIN ATTEND_DI_CD_DAT ADCD ON ADCD.YEAR = S1.YEAR AND ADCD.DI_CD = S3.DI_CD ");
        stb.append(" ORDER BY ");
        stb.append("    S1.EXECUTEDATE, S1.PERIODCD ");
        
        return stb.toString();
    }

    /*
     * get parameter doGet()パラメータ受け取り
     */
    private Param getParam(final DB2UDB db2, final HttpServletRequest request) {
    	log.fatal(" $Revision: 67923 $ $Date: 2019-06-10 15:22:44 +0900 (月, 10 6 2019) $");
        KNJServletUtils.debugParam(request, log);
        return new Param(db2, request);
    }

    private OnedayKintai getOnedayKintai(final String date, final Param param, final TreeMap onedayKintaiMap) {
        if (null == onedayKintaiMap.get(date)) {
            onedayKintaiMap.put(date, new OnedayKintai(date, param));
        }
        return (OnedayKintai) onedayKintaiMap.get(date);
    }
    
    
    private static class OnedayKintai {
        final String _date;
        final ArrayList _diCds;
        final Param _param;
        boolean _isAbroad;
        boolean _isOffdays;
        
        OnedayKintai(final String date, final Param param) {
            _date = date;
            _param = param;
            _diCds = new ArrayList();
        }
        
        private static List createList(int[] diCds) {
            final List list = new ArrayList();
            for (int i = 0; i < diCds.length; i++) {
                list.add(new Integer(diCds[i]));
            }
            return list;
        }
        
        private static int[] createArray(final List integerList) {
            final int[] array = new int[integerList.size()];
            for (int i = 0; i < integerList.size(); i++) {
                array[i] = ((Integer) integerList.get(i)).intValue();
            }
            return array;
        }
        
        public int[] kekkaCds() {
            final List kekka = new ArrayList();
            
            kekka.addAll(createList(sickCds));
            
//            if ("1".equals(_param.knjSchoolMst._subAbsent)) {
//                kekka.addAll(createList(new int[]{DI_CD_ABSENCE, DI_CD_ABSENCE_ALL}));
//            }
//            if (!"1".equals(_param.knjSchoolMst._syukessekiHanteiHou)) {
//                if ("1".equals(_param.knjSchoolMst._subSuspend)) {
//                    kekka.addAll(createList(suspendCds));
//                }
//                if ("1".equals(_param.knjSchoolMst._subMourning)) {
//                    kekka.addAll(createList(mourningCds));
//                }
//            }
//            if ("1".equals(_param.knjSchoolMst._subVirus)) {
//                kekka.addAll(createList(virusCds));
//            }
            return createArray(kekka);
        }

        public static boolean contains(final int diCd, final int[] diCds) {
            return ArrayUtils.contains(diCds, diCd);
        }
        
        /** diCds が diCdList の diCd をひとつでも含んでいるか */
        public static boolean containsOne(final List diCdList, final int[] diCds) {
            final int[] refDiCds = createArray(diCdList);
            for (int i = 0; i < refDiCds.length; i++) {
                if (contains(refDiCds[i], diCds)) {
                    return true;
                }
            }
            return false;
        }
        
        /** diCds が diCdList の diCd を全て含んでいるか */
        public static boolean containsAll(final List diCdList, final int[] diCds) {
            final int[] refDiCds = createArray(diCdList);
            for (int i = 0; i < refDiCds.length; i++) {
                if (!contains(refDiCds[i], diCds)) {
                    return false;
                }
            }
            return true;
        }
        
        public static int[] plus(final int[] cds1, final int[] cds2) {
            final int[] ret = new int[cds1.length + cds2.length];
            for (int i = 0; i < cds1.length; i++) {
                ret[i] = cds1[i];
            }
            for (int i = 0; i < cds2.length; i++) {
                ret[cds1.length + i] = cds2[i];
            }
            return ret;
        }
        
        /** 1日欠席か */
        public boolean isOnedaySick() {
            if ("1".equals(_param.knjSchoolMst._semOffDays) && _isOffdays) {
                return true;
            }
            if (_isAbroad || _isOffdays) {
                return false;
            }
            if (_diCds.size() == 0) {
                return false;
            }
            final List cds = new ArrayList();
            cds.add(new Integer(DI_CD_29_KEKKA_CHIKOKU));
            cds.add(new Integer(DI_CD_30_KEKKA_SOUTAI));
            cds.add(new Integer(DI_CD_31_KEKKA_CHIKOKU_SOUTAI));
            cds.add(new Integer(DI_CD_32_SHUSSEKI_CHIKOKU_SOUTAI));
            if (containsAll(_diCds, createArray(cds))) {
                return false;
            }
            if ("1".equals(_param.knjSchoolMst._syukessekiHanteiHou)) {
                if (isOnedaySuspend() || isOnedayMourning()) {
                    return false;
                }
                if (containsAll(_diCds, plus(mourningCds, kekkaCds())) || containsAll(_diCds, plus(_param.suspendCds, kekkaCds()))) {
                    return true;
                }
            }
            return containsAll(_diCds, kekkaCds());
        }

        /** 1日出停か */
        public boolean isOnedaySuspend() {
            if (_diCds.size() == 0 || _isAbroad || _isOffdays) { // 留学・休学の判定を優先する
                return false;
            }
            if ("1".equals(_param.knjSchoolMst._syukessekiHanteiHou)) {
                if (!containsAll(_diCds, mourningCds) && containsAll(_diCds, plus(mourningCds, _param.suspendCds))) {
                    return true;
                }
                // すべて出停なら出停
                return containsAll(_diCds, _param.suspendCds);
            } else {
                // ひとつでも出停があれば出停
                return containsOne(_diCds, _param.suspendCds);
            }
        }

        /** 1日忌引か */
        public boolean isOnedayMourning() {
            if (_diCds.size() == 0 || _isAbroad || _isOffdays) { // 留学・休学の判定を優先する
                return false;
            }
            if ("1".equals(_param.knjSchoolMst._syukessekiHanteiHou)) {
                if (!containsAll(_diCds, _param.suspendCds) && containsAll(_diCds, plus(mourningCds, _param.suspendCds))) {
                    return true;
                }
                // すべて忌引なら忌引
                return containsAll(_diCds, mourningCds);
            } else {
                // ひとつでも忌引があれば忌引
                return containsOne(_diCds, mourningCds);
            }
        }
        
        /** 1日遅刻か */
        public boolean isOnedayLate() {
            if (_diCds.size() == 0 || _isAbroad || _isOffdays) {
                return false;
            }
            if (isOnedaySuspend() || isOnedayMourning()) {
                return false;
            }
            final int firstDiCd = ((Integer)_diCds.get(0)).intValue(); 
            if (firstDiCd == DI_CD_LATE || firstDiCd == DI_CD_LATE2 || firstDiCd == DI_CD_LATE3) {
                return true;
            }
            final List onedayLate = new ArrayList();
            onedayLate.add(new Integer(DI_CD_29_KEKKA_CHIKOKU));
            onedayLate.add(new Integer(DI_CD_31_KEKKA_CHIKOKU_SOUTAI));
            onedayLate.add(new Integer(DI_CD_32_SHUSSEKI_CHIKOKU_SOUTAI));
            if (_diCds.size() == 1) {
                if (containsAll(_diCds, createArray(onedayLate))) {
                    return true;
                }
                return false;
            }
            final List onedayLateWithKekka = new ArrayList();
            onedayLateWithKekka.addAll(onedayLate);
            onedayLateWithKekka.addAll(createList(kekkaCds()));
            if (containsOne(_diCds, createArray(onedayLate)) && containsAll(_diCds, createArray(onedayLateWithKekka))) {
                return true;
            }
            if (contains(firstDiCd, kekkaCds())) { // 最初の授業を欠席
                return containsOne(_diCds.subList(1, _diCds.size()), inClassCds);
            } 
            return false;
        }
        
        /** 1日早退か */
        public boolean isOnedayEarly() {
            if (_diCds.size() == 0 || _isAbroad || _isOffdays) {
                return false;
            }
            if (isOnedaySuspend() || isOnedayMourning()) {
                return false;
            }
            final int lastDiCd = ((Integer)_diCds.get(_diCds.size() - 1)).intValue();
            if (lastDiCd == DI_CD_EARLY) {
                return true;
            }
            final List onedayEarly = new ArrayList();
            onedayEarly.add(new Integer(DI_CD_30_KEKKA_SOUTAI));
            onedayEarly.add(new Integer(DI_CD_31_KEKKA_CHIKOKU_SOUTAI));
            onedayEarly.add(new Integer(DI_CD_32_SHUSSEKI_CHIKOKU_SOUTAI));
            if (_diCds.size() == 1) {
                if (containsAll(_diCds, createArray(onedayEarly))) {
                    return true;
                }
                return false;
            }
            final List onedayEarlyWithKekka = new ArrayList();
            onedayEarlyWithKekka.addAll(onedayEarly);
            onedayEarlyWithKekka.addAll(createList(kekkaCds()));
            if (containsOne(_diCds, createArray(onedayEarly)) && containsAll(_diCds, createArray(onedayEarlyWithKekka))) {
                return true;
            }
            if (contains(lastDiCd, kekkaCds())) { // 最後の授業を欠席
                return containsOne(_diCds.subList(0, _diCds.size() - 1), inClassCds);
            } 
            return false;
        }
        
        public String getOnedayStatus() {
            final StringBuffer stb = new StringBuffer();
            if (_isAbroad) {
                stb.append("[abroad]"); 
            }
            if (_isOffdays) {
                stb.append("[offdays]"); 
            }
            if (isOnedaySick()) {
                stb.append("[sick]"); 
            }
            if (isOnedaySuspend()) {
                stb.append("[suspend]"); 
            }
            if (isOnedayMourning()) {
                stb.append("[mourning]"); 
            }
            if (isOnedayLate()) {
                stb.append("[late]"); 
            }
            if (isOnedayEarly()) {
                stb.append("[early]"); 
            }
            return stb.toString();
        }
        
        public String toString() {
            final StringBuffer stb = new StringBuffer("(");
            String comma = "";
            for (final Iterator it = _diCds.iterator(); it.hasNext();) {
                final Integer diCd = (Integer ) it.next();
                stb.append(comma + diCd);
                comma = ",";
            }
            return _date + "," + stb.toString()+ ")" + getOnedayStatus();
        }
    }
    
    private static class DiRecord {
        final String _diDiv;
        List _subclassList = Collections.EMPTY_LIST;

        DiRecord(final String diDiv) {
            _diDiv = diDiv;
        }
        
        public Set getDateSet(final Subclass subclass) {
            final Set rtn = new TreeSet();
            for (final Iterator its = getSubclassRecList(_diDiv, subclass).iterator(); its.hasNext();) {
                final SubclassRec rec = (SubclassRec) its.next();
                rtn.add(rec._executedate);
            }
            return rtn;
        }

        private static List getSubclassRecList(final String diDiv, final Subclass subclass) {
            final List list = new ArrayList();
            for (Iterator its = subclass._subclassRec.iterator(); its.hasNext();) {
                final SubclassRec rec = (SubclassRec) its.next();
                if (diDiv.equals(rec._diDiv)) {
                    list.add(rec);
                }
            }
            return list;
        }
        
        static BigDecimal getShowKansanLate(final BigDecimal late, final Subclass subclass) {
            if (null == late) {
                return zero;
            }
            BigDecimal count = late.subtract(subclass._lateEarlyKansan);
            if (count.doubleValue() < 0) {
                count = zero;
            }
            return count;
        }
        
        static BigDecimal getSubclassCountWithKansan(final String diDiv, final Subclass subclass, final Param param) {
            BigDecimal count = null;
            if (DI_DIV_LATE.equals(diDiv)) {
                final BigDecimal late = DiRecord.getSubclassCount(DI_DIV_LATE, subclass);
                if ("1".equals(param._chikokuHyoujiFlg)) {
                    // 遅刻数
                    count = late;
                } else if (null != late) {
                    // 換算後のあまり
                    count = getShowKansanLate(late, subclass);
                }
            } else if (DI_DIV_EARLY.endsWith(diDiv)) {
                final BigDecimal early = DiRecord.getSubclassCount(DI_DIV_EARLY, subclass);
                if ("1".equals(param._chikokuHyoujiFlg)) {
                    // 早退数
                    count = early;
                } else if (null != early) {
                    // 換算後のあまり
                    final BigDecimal late = DiRecord.getSubclassCount(DI_DIV_LATE, subclass);
                    count = early.add(null == late ? zero : late).subtract(subclass._lateEarlyKansan).subtract(getShowKansanLate(late, subclass));
                }
                
            } else if (DI_DIV_SICK.equals(diDiv)) {
                final BigDecimal count0 = getSubclassCount(DI_DIV_SICK, subclass);
                count = (null == count0 ? zero : count0).add(subclass._penalty);
            } else {
                count = DiRecord.getSubclassCount(diDiv, subclass);
            }
            return count;
        }

        private static BigDecimal getSubclassCount(final String diDiv, final Subclass subclass) {
            BigDecimal total = null;
            for (Iterator its = getSubclassRecList(diDiv, subclass).iterator(); its.hasNext();) {
                final SubclassRec rec = (SubclassRec) its.next();
                total = (null == total ? zero : total).add(new BigDecimal(rec._count));
            }
            return total;
        }

        public BigDecimal getTotalCount(final Param param) {
            BigDecimal total = null;
            for (final Iterator it = _subclassList.iterator(); it.hasNext();) {
                final Subclass subclass = (Subclass) it.next();
                final BigDecimal kansan = getSubclassCountWithKansan(_diDiv, subclass, param);
                if (null != kansan) {
                    total = (null == total ? zero : total).add(kansan);
                }
            }
            return total;
        }
    }

    private static class SubclassRec {
        final String _semester;
        final String _chaircd;
        final String _chairname;
        final String _executedate;
        final String _periodcd;
        final String _diDiv;
        final String _diCd;
        final int _count;
        public SubclassRec(
                final String semester, final String chaircd, final String chairname, final String executedate,
                final String periodcd, final String diDiv, final String diCd,
                final int count) {
            _semester = semester;
            _chaircd = chaircd;
            _chairname = chairname;
            _executedate = executedate;
            _periodcd = periodcd;
            _diDiv = diDiv;
            _diCd = diCd;
            _count = count;
        }
    }

    private static class Subclass implements Comparable {
        final String _keyCd;
        final String _name;
        final List _subclassRec = new ArrayList();
        BigDecimal _penalty = zero;
        BigDecimal _lateEarlyKansan = zero;
        
        public static Subclass getSubclass(final String keyCd, final List list) {
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final Subclass subclass = (Subclass) it.next();
                if (keyCd.equals(subclass._keyCd)) {
                    return subclass;
                }
            }
            return null;
        }

        Subclass(final String keyCd, final String name) {
            _keyCd = keyCd;
            _name = name;
        }

        int getLesson() {
            final int lesson = _subclassRec.size();
            return lesson;
        }

        int getMLesson(final Param param) {
            final BigDecimal subclassCountWithKansan = DiRecord.getSubclassCountWithKansan(DI_DIV_SUSPEND_MOURNING, this, param);
            final int suspendMourning;
            if (null != subclassCountWithKansan) {
                suspendMourning = subclassCountWithKansan.intValue();
            } else {
                suspendMourning = 0;
            }
            return getLesson() - suspendMourning; 
        }
        
        public int compareTo(Object o) {
            if (null == o || !(o instanceof Subclass)) {
                return -1;
            }
            return _keyCd.compareTo(((Subclass) o)._keyCd); 
        }
        public String toString() {
            return "Subclass(" + _keyCd + ", " + _name + ")";
        }
    }
    
    private static class Param {
        final String _year;
        final String _semester;
        final String[] _categorySelected;
        final String _date1;
        final String _date2;
        final String _gradehrclass;
        final String _useCurriculumcd;
        final String _useTestCountflg;
        final String _chikokuHyoujiFlg;
        final String _useVirus;
        final String _useKoudome;
        final boolean _hasSchChrDatExecutediv;
        final boolean _hasChairDetailDat;
        final int _absent_cov;
        final int _absent_cov_late;
        final KNJDefineSchool definecode;
        
        final KNJSchoolMst knjSchoolMst;
        final String _outputname;
        final String _outputjisuu;
        final boolean _isPrintKetsuji;
        final String _use_SchregNo_hyoji;
        private int[] suspendCds = {}; // 出停のコード

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year  = request.getParameter("YEAR");           //年度
            _semester  = request.getParameter("SEMESTER");       //学期
            _categorySelected = request.getParameterValues("category_selected");  // 学籍番号;
            //日付型を変換
            _date1 = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE1"));            //印刷範囲開始
            _date2 = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE2"));            //印刷範囲開始
            _gradehrclass = request.getParameter("GRADE_HR_CLASS");  // 学年・組
            _outputname = request.getParameter("OUTPUTNAME");  // 1:科目名, 2:講座名
            _outputjisuu = request.getParameter("OUTPUTJISUU");
            _isPrintKetsuji = "2".equals(_outputname);
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
            _use_SchregNo_hyoji = request.getParameter("use_SchregNo_hyoji");
            
            KNJSchoolMst knjSchoolMst_ = null;
            try {
            	final Map paramMap = new HashMap();
            	if (KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND")) {
            		final String schoolKind = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE ='" + _gradehrclass.substring(0, 2) + "' "));
					paramMap.put("SCHOOL_KIND", schoolKind);
            	}
                knjSchoolMst_ = new KNJSchoolMst(db2, _year, paramMap);
            } catch (SQLException e) {
                log.debug("exception!", e);
            }
            knjSchoolMst = knjSchoolMst_;
            definecode = new KNJDefineSchool();
            definecode.defineCode(db2, _year);
            if ("KIN".equals(definecode.schoolmark)) {
                _absent_cov = definecode.absent_cov;
                _absent_cov_late = definecode.absent_cov_late;
            } else {
                _absent_cov = Integer.parseInt(StringUtils.defaultString(knjSchoolMst._absentCov, "0"));
                _absent_cov_late = Integer.parseInt(StringUtils.defaultString(knjSchoolMst._absentCovLate, "0"));
            }
            
            //  欠課数換算定数取得 => KNJDefineCodeImpを実装したオブジェクトを作成
            log.debug("semesdiv = " + knjSchoolMst._semesterDiv + "   absent_cov = " + knjSchoolMst._absentCov + "   absent_cov_late = " + knjSchoolMst._absentCovLate);
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useTestCountflg = request.getParameter("useTestCountflg"); // テスト項目マスタテーブル
            _chikokuHyoujiFlg = request.getParameter("chikokuHyoujiFlg"); // 遅刻表示フラグ
            _hasSchChrDatExecutediv = KnjDbUtils.setTableColumnCheck(db2, "SCH_CHR_DAT", "EXECUTEDIV");
            _hasChairDetailDat = KnjDbUtils.setTableColumnCheck(db2, "CHAIR_DETAIL_DAT", null);
        }
    }
}
