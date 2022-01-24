// kanji=漢字
/*
 * $Id: c93ed3358e1cba59b38e8add1a147eaebfa8dede $
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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/*
 *  学校教育システム 賢者 [出欠管理]     欠席等状況 / 講座出席状況表
 */

public class KNJC110B {

    private static final Log log = LogFactory.getLog(KNJC110B.class);
    
    private static final String DI_DIV_ATTEND = "0";
    private static final String DI_DIV_LATE = "1";
    private static final String DI_DIV_EARLY = "2";
    private static final String DI_DIV_SICK = "3";
    private static final String DI_DIV_SUSPEND_MOURNING = "4";
    
    private static final Integer DI_CD_ATTEND = new Integer(0);
    private static final Integer DI_CD_ABSENCE = new Integer(1);
    private static final Integer DI_CD_SUSPEND = new Integer(2);
    private static final Integer DI_CD_MOURNING = new Integer(3);
    private static final Integer DI_CD_SICK = new Integer(4);
    private static final Integer DI_CD_NOTICE = new Integer(5);
    private static final Integer DI_CD_NONOTICE = new Integer(6);
    private static final Integer DI_CD_ABSENCE_ALL = new Integer(8);
    private static final Integer DI_CD_SUSPEND_ALL = new Integer(9);
    private static final Integer DI_CD_MOURNING_ALL = new Integer(10);
    private static final Integer DI_CD_SICK_ALL = new Integer(11);
    private static final Integer DI_CD_NOTICE_ALL = new Integer(12);
    private static final Integer DI_CD_NONOTICE_ALL = new Integer(13);
    private static final Integer DI_CD_NURSEOFF = new Integer(14);
    private static final Integer DI_CD_LATE = new Integer(15);
    private static final Integer DI_CD_EARLY = new Integer(16);
    private static final Integer DI_CD_VIRUS = new Integer(19);
    private static final Integer DI_CD_VIRUS_ALL = new Integer(20);
    private static final Integer DI_CD_LATE2 = new Integer(23);
    private static final Integer DI_CD_LATE3 = new Integer(24);
    private static final Integer DI_CD_KOUDOME = new Integer(25);
    private static final Integer DI_CD_KOUDOME_ALL = new Integer(26);
    private static final Integer DI_CD_29_KEKKA_CHIKOKU = new Integer(29);
    private static final Integer DI_CD_30_KEKKA_SOUTAI = new Integer(30);
    private static final Integer DI_CD_31_KEKKA_CHIKOKU_SOUTAI = new Integer(31);
    private static final Integer DI_CD_32_SHUSSEKI_CHIKOKU_SOUTAI = new Integer(32);
    
    private static final Integer[] suspendOnlyCds = {DI_CD_SUSPEND, DI_CD_SUSPEND_ALL}; // 出停のコード
    private static final Integer[] virusCds = {DI_CD_VIRUS, DI_CD_VIRUS_ALL}; // 伝染病のコード
    private static final Integer[] koudomeCds = {DI_CD_KOUDOME, DI_CD_KOUDOME_ALL}; // 交止のコード
    private static final Integer[] mourningCds = {DI_CD_MOURNING, DI_CD_MOURNING_ALL}; // 忌引のコード
    private static final Integer[] sickCds = {DI_CD_SICK, DI_CD_NOTICE, DI_CD_NONOTICE, DI_CD_SICK_ALL, DI_CD_NOTICE_ALL, DI_CD_NONOTICE_ALL}; // 欠課のコード
    private static final Integer[] absenceCds = {DI_CD_ABSENCE, DI_CD_ABSENCE_ALL}; // 公欠のコード
    
    private static final Integer[] inClassCds = {DI_CD_ATTEND, DI_CD_NURSEOFF, DI_CD_LATE, DI_CD_EARLY, DI_CD_LATE2, DI_CD_LATE3}; // 1日出欠での出席扱いコード
    
    private static final String KEY_SUSPEND = "SUSPEND";
    private static final String KEY_MOURNING = "MOURNING";
    private static final String KEY_LATE = "LATE";
    private static final String KEY_EARLY = "EARLY";
    private static final String KEY_SICK = "SICK";
    private static final String KEY_ABSENCE = "ABSENCE";
    
    private static final BigDecimal zero = new BigDecimal(0);
    
    private boolean hasdata;               //該当データなしフラグ
    
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
        final Param param = getParam(db2, request);
        setSuspendCd(param);
        
        // 印刷処理
        printSvf(db2, param, svf);
        
        // 終了処理
        sd.closeSvf(svf, hasdata);
        sd.closeDb(db2);
    }

    private synchronized void setSuspendCd(final Param param) {
        final List<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < suspendOnlyCds.length; i++) {
            list.add(suspendOnlyCds[i]);
        }
        if ("true".equals(param._useVirus)) {
            for (int i = 0; i < virusCds.length; i++) {
                list.add(virusCds[i]);
            }
        }
        if ("true".equals(param._useKoudome)) {
            for (int i = 0; i < koudomeCds.length; i++) {
                list.add(koudomeCds[i]);
            }
        }
        final Integer[] array = new Integer[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        // log.debug(" suspendcds = " + ArrayUtils.toString(array));
        param.suspendCds = array;
    }

    /*
     *  印刷処理
     */
    private void printSvf(
            final DB2UDB db2,
            final Param param,
            final Vrw32alp svf
    ) {
        final List<Student> studentList = Student.getStudentList(db2, param);

        final String nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(param._year)) + "年度";

        if ("2".equals(param._output)) {
            OnedayKintai.setKindMap(db2, param, studentList);

            final String form;
            if ("1".equals(param._knjc110bUsePeriod12Form)) {
            	form = "KNJC110B_2_2.frm";
            } else {
            	form = "KNJC110B_2.frm";
            }
			svf.VrSetForm(form, 1);
            
            for(int i = 0 ; i < studentList.size(); i++) {
                
                final Student student = studentList.get(i);             //学籍データ出力のメソッド
                
                if (student._printList.isEmpty()) {
                    continue;
                }
                log.info(" schregno = " + student._schregno);
                
                final List<List<OnedayKintai>> pageList = getPageList(student._printList, 50);
                
                for (int pi = 0; pi < pageList.size(); pi++) {
                    final List<OnedayKintai> onedayKintaiList = pageList.get(pi);
                    
					svf.VrsOut("TITLE", nendo + " 講座出席状況表"); // タイトル
                    svf.VrsOut("PRINT_DATE", param._now); // 印刷日
                    svf.VrsOut("HR_NAME", StringUtils.defaultString(student._hrname) + " " + (NumberUtils.isDigits(student._attendno) ? String.valueOf(Integer.parseInt(student._attendno)) : StringUtils.defaultString(student._attendno)) + "番"); // 年組番
                    svf.VrsOut("NAME", student._name); // 氏名
                    
                    for (final Map.Entry<String, Integer> e : getMappedMap(param._courseCdperiodCdMap, student._coursecd).entrySet()) {
                        final String periodCd = e.getKey();
                        final Integer pdi = e.getValue();
                        final String name = getMappedMap(param._courseCdperiodNameMap, student._coursecd).get(periodCd);
                        svf.VrsOut("PERIOD_NAME" + pdi.toString(), name); // 校時名称
                    }
                    
                    for (int j = 0; j < onedayKintaiList.size(); j++) {
                        final int line = j + 1;
                        final OnedayKintai onedayKintai = onedayKintaiList.get(j);
                        
                        svf.VrsOutn("DATE", line, formatDate(db2, false, onedayKintai._date)); // 日付
                        
                        for (final ScheduleAttendance a : onedayKintai._attendances) {
                            final Integer pdi = getMappedMap(param._courseCdperiodCdMap, student._coursecd).get(a._periodcd);
                            if (null == pdi) {
                                continue;
                            }
                            final int keta = getMS932ByteLength(a._chairname);
                            svf.VrsOutn("CHAIR_NAME" + pdi.toString() + "_" + (keta > 12 ? "3" : keta > 8 ? "2" : "1"), line, a._chairname); // 出欠名称
                        }
                    }
                    svf.VrEndPage();
                    hasdata = true;
                }
            }

        } else {
            OnedayKintai.setKindMap(db2, param, studentList);
            
            String form;
            if ("1".equals(param._knjc110bUsePeriod12Form)) {
            	form = "KNJC110B_1_2.frm";
            } else {
            	form = "KNJC110B_1.frm";
            }
			svf.VrSetForm(form, 1);
            
            //int page = 0;
            for(int i = 0 ; i < studentList.size(); i++) {
                
                final Student student = studentList.get(i);             //学籍データ出力のメソッド
                
                log.info(" schregno = " + student._schregno + " printList size = " + student._printList.size());
                if (student._printList.isEmpty()) {
                    continue;
                }
                
                final List<List<OnedayKintai>> pageList = getPageList(student._printList, 50);
                
                for (int pi = 0; pi < pageList.size(); pi++) {
                    final List<OnedayKintai> onedayKintaiList = pageList.get(pi);
                    
                    svf.VrsOut("TITLE", nendo + " 欠席等状況"); // タイトル
                    //svf.VrsOut("PAGE", String.valueOf(page + pi + 1)); // ページ
                    svf.VrsOut("PRINT_DATE", param._now); // 印刷日
                    svf.VrsOut("HR_NAME", StringUtils.defaultString(student._hrname) + " " + (NumberUtils.isDigits(student._attendno) ? String.valueOf(Integer.parseInt(student._attendno)) : StringUtils.defaultString(student._attendno)) + "番"); // 年組番
                    svf.VrsOut("NAME", student._name); // 氏名
                    
                    for (final Map.Entry<String, Integer> e : getMappedMap(param._courseCdperiodCdMap, student._coursecd).entrySet()) {
                        final String periodCd = e.getKey();
                        final Integer pdi = e.getValue();
                        final String name = getMappedMap(param._courseCdperiodNameMap, student._coursecd).get(periodCd);
                        svf.VrsOut("PERIOD_NAME" + pdi.toString(), name); // 校時名称
                    }
                    
                    for (int j = 0; j < onedayKintaiList.size(); j++) {
                        final int line = j + 1;
                        final OnedayKintai onedayKintai = onedayKintaiList.get(j);
                        
                        svf.VrsOutn("DATE", line, formatDate(db2, true, onedayKintai._date)); // 日付
                        svf.VrsOutn("ATTEND_DIV", line, getMS932ByteLength(onedayKintai._onedayKintaiName) > 4 ? onedayKintai._onedayKintaiName.substring(0, 2) : onedayKintai._onedayKintaiName); // 1日出欠
                        
                        for (final ScheduleAttendance a : onedayKintai._attendances) {
                            final Integer pdi = getMappedMap(param._courseCdperiodCdMap, student._coursecd).get(a._periodcd);
                            if (null == pdi) {
                                continue;
                            }
                            
                            String diName = "";
                            if (onedayKintai._isAbroad) {
                                diName = "留学";
                            } else if (onedayKintai._isOffdays) {
                                diName = "休学";
                            } else if (null != a._diCd && a._diCd.intValue() != 0) {
                                diName = param._kintaiNameMap.get(a._diCd);
                            }
                            svf.VrsOutn("DI_NAME" + pdi.toString(), line, getMS932ByteLength(diName) >= 4 ? diName.substring(0, 1) : diName); // 出欠名称
                        }
                        
                        if (null != onedayKintai._remarkList) {
                            final StringBuffer remark = new StringBuffer();
                            for (int ri = 0; ri < onedayKintai._remarkList.size(); ri++) {
                                final String iremark = onedayKintai._remarkList.get(ri);
                                if (!StringUtils.isBlank(iremark)) {
                                    if (remark.length() != 0) {
                                        remark.append(" ");
                                    }
                                    remark.append(iremark);
                                }
                            }
                            svf.VrsOutn("REMARK", line, remark.toString()); // 備考
                        }
                    }
                    svf.VrEndPage();
                    hasdata = true;
                }
                //page += pageList.size();
            }
        }
    }

    private static <T> List<List<T>> getPageList(final List<T> list, final int max) {
        final List<List<T>> rtn = new ArrayList<List<T>>();
        List<T> current = null;
        for (final T o : list) {
            if (null == current || current.size() >= max) {
                current = new ArrayList<T>();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }

    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str) {
        return KNJ_EditEdit.getMS932ByteLength(str);
    }
    
    private static <A, B> List<B> getMappedList(final Map<A, List<B>> map, final A key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList());
        }
        return map.get(key1);
    }
    
    private static <A, B, C> Map<B, C> getMappedMap(final Map<A, Map<B, C>> map, final A key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap());
        }
        return map.get(key1);
    }

    private static String formatDate(final DB2UDB db2, final boolean printNen, final String date) {
        final Calendar cal1 = Calendar.getInstance();
        cal1.setTime(Date.valueOf(date));
        String nen = "";
        if (printNen) {
            nen = KNJ_EditDate.gengou(db2, cal1.get(Calendar.YEAR)) + "年";
        }
        String tuki = (cal1.get(Calendar.MONTH) + 1 < 10 ? " " : "") + String.valueOf(cal1.get(Calendar.MONTH) + 1) + "月";
        String hi = (cal1.get(Calendar.DATE) < 10 ? " " : "") + String.valueOf(cal1.get(Calendar.DATE)) + "日";
        String youbi = "(" + KNJ_EditDate.h_format_W(date) + ")";
        return nen + tuki + hi + youbi;
    }
    
    private static class Student {
        final String _schregno;
        final String _name;
        final String _hrname;
        final String _attendno;
        final String _coursecd;
        Map<String, OnedayKintai> _onedayKintaiMap = Collections.EMPTY_MAP;
        List<OnedayKintai> _printList = Collections.EMPTY_LIST;

        public Student(final String schregno, final String name, final String hrname, final String attendno, final String coursecd) {
            _schregno = schregno;
            _name = name;
            _hrname = hrname;
            _attendno = attendno;
            _coursecd = coursecd;
        }
        
        /*
         *  印刷処理 生徒名出力
         */
        private static List<Student> getStudentList(
                final DB2UDB db2,
                final Param param
        ) {
            final List<Student> list = new ArrayList<Student>();
            //SQL作成
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                //学籍のSQL

                final StringBuffer stb = new StringBuffer();
                stb.append("SELECT ");
                stb.append("    W1.SCHREGNO, W1.NAME, W3.HR_NAME, INT(W2.ATTENDNO)AS ATTENDNO, W2.COURSECD ");
                stb.append("FROM ");
                stb.append("    SCHREG_BASE_MST W1");
                stb.append("    INNER JOIN SCHREG_REGD_DAT W2 ON W1.SCHREGNO = W2.SCHREGNO ");
                stb.append("    INNER JOIN SCHREG_REGD_HDAT W3 ON W2.YEAR = W3.YEAR AND W2.SEMESTER = W3.SEMESTER AND W2.GRADE = W3.GRADE AND W2.HR_CLASS = W3.HR_CLASS ");
                stb.append("WHERE ");
                stb.append("       W1.SCHREGNO = ? AND ");
                stb.append(       "W2.YEAR = '" + param._year + "' ");
                stb.append("ORDER BY W2.SEMESTER DESC ");
                final String sql = stb.toString();

//                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                for (int i = 0; i < param._categorySelected.length; i++) {
                    ps.setString(1, param._categorySelected[i]);
                    rs = ps.executeQuery();
                    if (rs.next()) {
                        final Student student = new Student(rs.getString("SCHREGNO"), rs.getString("NAME"), rs.getString("HR_NAME"), rs.getString("ATTENDNO"), rs.getString("COURSECD"));
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
    }

    private static class OnedayKintai {
        final String _date;
        final ArrayList<ScheduleAttendance> _attendances;
        List<String> _remarkList;
        boolean _isAbroad;
        boolean _isOffdays;
        private String _onedayKintaiName;
        
        OnedayKintai(final String date) {
            _date = date;
            _attendances = new ArrayList<ScheduleAttendance>();
        }
        
        private static List<Integer> createList(Integer[] diCds) {
            return Arrays.asList(diCds);
        }
        
        private static Integer[] createArray(final List<Integer> integerList) {
            final Integer[] array = new Integer[integerList.size()];
            integerList.toArray(array);
            return array;
        }
        
        public Integer[] kekkaCds() {
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
            return sickCds;
        }
        
        public Integer[] kouketsuCds() {
            return absenceCds;
        }

        public static boolean contains(final Integer diCd, final Integer[] diCds) {
            for (int i = 0; i < diCds.length; i++) {
                if (diCd == null && null == diCds[i] || diCd != null && diCd.equals(diCds[i])) {
                    return true;
                }
            }
            return false;
        }
        
        /** diCds が diCdList の diCd をひとつでも含んでいるか */
        public static boolean containsOne(final List<ScheduleAttendance> attendancesList, final Integer[] diCds) {
            for (int i = 0; i < attendancesList.size(); i++) {
                if (contains(attendancesList.get(i)._repDiCd, diCds)) {
                    return true;
                }
            }
            return false;
        }
        
        /** diCds が diCdList の diCd を全て含んでいるか */
        public static boolean containsAll(final List<ScheduleAttendance> attendancesList, final Integer[] diCds) {
            for (int i = 0; i < attendancesList.size(); i++) {
                if (!contains(attendancesList.get(i)._repDiCd, diCds)) {
                    return false;
                }
            }
            return true;
        }
        
        public static Integer[] plus(final Integer[] cds1, final Integer[] cds2) {
            final Integer[] ret = new Integer[cds1.length + cds2.length];
            for (int i = 0; i < cds1.length; i++) {
                ret[i] = cds1[i];
            }
            for (int i = 0; i < cds2.length; i++) {
                ret[cds1.length + i] = cds2[i];
            }
            return ret;
        }
        
        /** 1日公欠か */
        public boolean isOnedayKouketsu(final Param param) {
            if (_isAbroad || _isOffdays) {
                return false;
            }
            if (_attendances.size() == 0) {
                return false;
            }
            if ("1".equals(param._knjSchoolMst._syukessekiHanteiHou)) {
                if (isOnedaySuspend(param) || isOnedayMourning(param)) {
                    return false;
                }
                if (isOnedayAll(plus(mourningCds, kekkaCds())) || isOnedayAll(plus(param.suspendCds, kekkaCds()))) {
                    return false;
                }
            }
            return containsOne(_attendances, kouketsuCds());
        }

        /** 1日欠席か */
        public boolean isOnedaySick(final Param param) {
            if ("1".equals(param._knjSchoolMst._semOffDays) && _isOffdays) {
                return true;
            }
            if (_isAbroad || _isOffdays) {
                return false;
            }
            if (_attendances.size() == 0) {
                return false;
            }
            final List cds = new ArrayList();
            cds.add(DI_CD_29_KEKKA_CHIKOKU);
            cds.add(DI_CD_30_KEKKA_SOUTAI);
            cds.add(DI_CD_31_KEKKA_CHIKOKU_SOUTAI);
            cds.add(DI_CD_32_SHUSSEKI_CHIKOKU_SOUTAI);
            if (containsAll(_attendances, createArray(cds))) {
                return false;
            }
            if ("1".equals(param._knjSchoolMst._syukessekiHanteiHou)) {
                if (isOnedaySuspend(param) || isOnedayMourning(param)) {
                    return false;
                }
                if (isOnedayAll(plus(mourningCds, kekkaCds())) || isOnedayAll(plus(param.suspendCds, kekkaCds()))) {
                    return true;
                }
            }
            return containsAll(_attendances, kekkaCds());
        }

        /** 1日出停か */
        public boolean isOnedaySuspend(final Param param) {
            if (_attendances.size() == 0 || _isAbroad || _isOffdays) { // 留学・休学の判定を優先する
                return false;
            }
            if ("1".equals(param._knjSchoolMst._syukessekiHanteiHou)) {
                if (!isOnedayAll(mourningCds) && isOnedayAll(plus(mourningCds, param.suspendCds))) {
                    return true;
                }
                // すべて出停なら出停
                return containsAll(_attendances, param.suspendCds);
            } else {
                // ひとつでも出停があれば出停
                return containsOne(_attendances, param.suspendCds);
            }
        }

        /** 1日忌引か */
        public boolean isOnedayMourning(final Param param) {
            if (_attendances.size() == 0 || _isAbroad || _isOffdays) { // 留学・休学の判定を優先する
                return false;
            }
            if ("1".equals(param._knjSchoolMst._syukessekiHanteiHou)) {
                if (!isOnedayAll(param.suspendCds) && isOnedayAll(plus(mourningCds, param.suspendCds))) {
                    return true;
                }
                // すべて忌引なら忌引
                return containsAll(_attendances, mourningCds);
            } else {
                // ひとつでも忌引があれば忌引
                return containsOne(_attendances, mourningCds);
            }
        }
        
        /** すべてのコマがcdsに含まれるか */
        public boolean isOnedayAll(final Integer[] cds) {
            return containsAll(_attendances, cds);
        }
        
        /** 1日遅刻か */
        public boolean isOnedayLate(final Param param) {
            if (_attendances.size() == 0 || _isAbroad || _isOffdays) {
                return false;
            }
            if (isOnedaySuspend(param) || isOnedayMourning(param) || isOnedaySick(param)) {
                return false;
            }
            final Integer firstDiCd = ((ScheduleAttendance)_attendances.get(0))._repDiCd; 
            if (firstDiCd == DI_CD_LATE || firstDiCd == DI_CD_LATE2 || firstDiCd == DI_CD_LATE3) {
                return true;
            }
            final List onedayLate = new ArrayList();
            onedayLate.add(DI_CD_29_KEKKA_CHIKOKU);
            onedayLate.add(DI_CD_31_KEKKA_CHIKOKU_SOUTAI);
            onedayLate.add(DI_CD_32_SHUSSEKI_CHIKOKU_SOUTAI);
            if (_attendances.size() == 1) {
                if (containsAll(_attendances, createArray(onedayLate))) {
                    return true;
                }
                return false;
            }
            final List onedayLateWithKekka = new ArrayList();
            onedayLateWithKekka.addAll(onedayLate);
            onedayLateWithKekka.addAll(createList(kekkaCds()));
            if (containsOne(_attendances, createArray(onedayLate)) && containsAll(_attendances, createArray(onedayLateWithKekka))) {
                return true;
            }
            if (contains(firstDiCd, kekkaCds())) { // 最初の授業を欠席
                return containsOne(_attendances.subList(1, _attendances.size()), inClassCds);
            } 
            return false;
        }
        
        /** 1日早退か */
        public boolean isOnedayEarly(final Param param) {
            if (_attendances.size() == 0 || _isAbroad || _isOffdays) {
                return false;
            }
            if (isOnedaySuspend(param) || isOnedayMourning(param) || isOnedaySick(param)) {
                return false;
            }
            final Integer lastDiCd = ((ScheduleAttendance)_attendances.get(_attendances.size() - 1))._repDiCd;
            if (lastDiCd == DI_CD_EARLY) {
                return true;
            }
            final List onedayEarly = new ArrayList();
            onedayEarly.add(DI_CD_30_KEKKA_SOUTAI);
            onedayEarly.add(DI_CD_31_KEKKA_CHIKOKU_SOUTAI);
            onedayEarly.add(DI_CD_32_SHUSSEKI_CHIKOKU_SOUTAI);
            if (_attendances.size() == 1) {
                if (containsAll(_attendances, createArray(onedayEarly))) {
                    return true;
                }
                return false;
            }
            final List onedayEarlyWithKekka = new ArrayList();
            onedayEarlyWithKekka.addAll(onedayEarly);
            onedayEarlyWithKekka.addAll(createList(kekkaCds()));
            if (containsOne(_attendances, createArray(onedayEarly)) && containsAll(_attendances, createArray(onedayEarlyWithKekka))) {
                return true;
            }
            if (contains(lastDiCd, kekkaCds())) { // 最後の授業を欠席
                return containsOne(_attendances.subList(0, _attendances.size() - 1), inClassCds);
            } 
            return false;
        }
        
        public String getOnedayStatus(final Param param) {
            final StringBuffer stb = new StringBuffer();
            if (_isAbroad) {
                stb.append("[abroad]"); 
            }
            if (_isOffdays) {
                stb.append("[offdays]"); 
            }
            if (isOnedaySick(param)) {
                stb.append("[sick]"); 
            }
            if (isOnedaySuspend(param)) {
                stb.append("[suspend]"); 
            }
            if (isOnedayMourning(param)) {
                stb.append("[mourning]"); 
            }
            if (isOnedayLate(param)) {
                stb.append("[late]"); 
            }
            if (isOnedayEarly(param)) {
                stb.append("[early]"); 
            }
            return stb.toString();
        }
        
        public String toString(final Param param) {
            final StringBuffer stb = new StringBuffer("(");
            String comma = "";
            for (final ScheduleAttendance a : _attendances) {
                stb.append(comma).append(a._periodcd).append("=").append(a._diCd).append("(").append(a._repDiCd).append(")");
                comma = ",";
            }
            return _date + "," + stb.toString()+ ")" + getOnedayStatus(param);
        }
        
        private static void setKindMap(final DB2UDB db2, final Param param, final List<Student> studentList) {

            final String sql = getDateAttendSql(param);
            if (param._isOutputDebug) {
            	log.info("sql dateAttend " + sql);
            }
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                ps = db2.prepareStatement(sql);

                for (final Student student : studentList) {
                    
                    student._onedayKintaiMap = new TreeMap();
                
                    ps.setString(1, student._schregno);
                    ps.setString(2, student._schregno);
                    rs = ps.executeQuery();

                    while(rs.next()) {
                        final String date = rs.getString("EXECUTEDATE");
                        final Integer repDiCd = Integer.valueOf(rs.getString("REP_DI_CD"));
                        final Integer diCd = Integer.valueOf(rs.getString("DI_CD"));
                        final boolean isAbroad = "1".equals(rs.getString("IS_ABROAD"));
                        final boolean isOffdays = "1".equals(rs.getString("IS_OFFDAYS"));
                        
                        if (null == student._onedayKintaiMap.get(date)) {
                            student._onedayKintaiMap.put(date, new OnedayKintai(date));
                        }
                        final OnedayKintai onedayKintai = student._onedayKintaiMap.get(date);
                        final ScheduleAttendance a = new ScheduleAttendance(rs.getString("PERIODCD"), rs.getString("CHAIRCD"), rs.getString("CHAIRNAME"), repDiCd, diCd);
                        onedayKintai._attendances.add(a);
                        onedayKintai._isAbroad = onedayKintai._isAbroad || isAbroad;
                        onedayKintai._isOffdays = onedayKintai._isOffdays || isOffdays;
                        final String diRemark = rs.getString("DI_REMARK");
                        if (null != diRemark) {
                            if (null == onedayKintai._remarkList) {
                                onedayKintai._remarkList = new ArrayList();
                            }
                            if (!onedayKintai._remarkList.contains(diRemark)) {
                                onedayKintai._remarkList.add(diRemark);
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
            
            for (final Student student : studentList) {
                
                student._printList = new ArrayList();
                for (final String date : student._onedayKintaiMap.keySet()) {
                    final OnedayKintai onedayKintai = student._onedayKintaiMap.get(date);
                    
                    if ("2".equals(param._output)) {
                        // 出席のある日付
                        if (onedayKintai._isAbroad || onedayKintai._isOffdays) {
                            continue;
                        }
                        boolean addflg = false;
                        for (final ScheduleAttendance a : onedayKintai._attendances) {
                            if (null == a._diCd || a._diCd.intValue() == 0) {
                                addflg = true;
                                break;
                            }
                        }
                        if (addflg) {
                            student._printList.add(onedayKintai);
                        }
                    } else {

                        // 留学・休学・出停・忌引・公欠・欠席・遅刻・早退のいずれか
                        onedayKintai._onedayKintaiName = "";
                        boolean addflg = false;
                        if (onedayKintai._isAbroad) {
                            onedayKintai._onedayKintaiName += "留学";
                            addflg = true;
                        }
                        if (onedayKintai._isOffdays) {
                            onedayKintai._onedayKintaiName += "休学";
                            addflg = true;
                        }
                        final boolean isOnedaySuspend = onedayKintai.isOnedaySuspend(param);
                        if (isOnedaySuspend) {
                            onedayKintai._onedayKintaiName += "出停";
                            addflg = true;
                        }
                        final boolean isOnedayMourning = onedayKintai.isOnedayMourning(param);
                        if (isOnedayMourning) {
                            onedayKintai._onedayKintaiName += "忌引";
                            addflg = true;
                        }
                        if (!(isOnedaySuspend || isOnedayMourning)) {
                            final boolean isOnedayKouketsu = onedayKintai.isOnedayKouketsu(param);
                            if (isOnedayKouketsu) {
                                onedayKintai._onedayKintaiName += "公欠";
                                addflg = true;
                            }
                            final boolean isOnedaySick = onedayKintai.isOnedaySick(param);
                            if (isOnedaySick) {
                                onedayKintai._onedayKintaiName += "欠席";
                                addflg = true;
                            }
                            final boolean isOnedayLate = onedayKintai.isOnedayLate(param);
                            if (isOnedayLate) {
                                onedayKintai._onedayKintaiName += "遅刻";
                                addflg = true;
                            }
                            final boolean isOnedayEarly = onedayKintai.isOnedayEarly(param);
                            if (isOnedayEarly) {
                                onedayKintai._onedayKintaiName += "早退";
                                addflg = true;
                            }
                        }
                        if (addflg) {
                            student._printList.add(onedayKintai);
                            log.info(" student = " + student._schregno + ", date = " + date + ", onedayKintai = " + onedayKintai.getOnedayStatus(param));
                        }
                        if (param._isOutputDebug) {
                            log.info(" student = " + student._schregno + ", date = " + date + ", onedayKintai = " + onedayKintai.getOnedayStatus(param) + " / " + onedayKintai._attendances);
                        }
                    }
                }
            }
        }
        
        /*
         *  preparedStatement作成  １日の出欠表
         */
        private static String getDateAttendSql(final Param param) {

            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH ");
            stb.append("  PERIOD_TERM AS( ");
            stb.append("   SELECT S_PERIODCD, E_PERIODCD ");
            stb.append("   FROM   COURSE_MST T1 ");
            stb.append("   WHERE  EXISTS (SELECT 'X' FROM SCHREG_REGD_DAT T2 ");
            stb.append("                 WHERE  T2.YEAR = '" + param._year + "' ");
            stb.append("                    AND T2.SEMESTER = '" + param._semester + "' ");
            stb.append("                    AND T2.SCHREGNO = ? ");
            stb.append("                    AND T2.COURSECD = T1.COURSECD) ");
            stb.append("), SCH_PERIOD_DATA AS ( ");
            stb.append("SELECT ");
            stb.append("    T2.SCHREGNO, ");
            stb.append("    T1.YEAR, ");
            stb.append("    T1.EXECUTEDATE, ");
            stb.append("    T1.PERIODCD, ");
            stb.append("    T1.CHAIRCD, ");
            stb.append("    T3.CHAIRNAME, ");
            stb.append("    CASE WHEN T_TRANT1.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS IS_ABROAD, ");
            stb.append("    CASE WHEN T_TRANT2.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS IS_OFFDAYS ");
            stb.append("FROM ");
            stb.append("    SCH_CHR_DAT T1 ");
            stb.append("    INNER JOIN CHAIR_STD_DAT T2 ON T1.YEAR = T2.YEAR ");
            stb.append("        AND T1.SEMESTER = T2.SEMESTER ");
            stb.append("        AND T1.CHAIRCD = T2.CHAIRCD ");
            stb.append("        AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ");
            stb.append("    INNER JOIN CHAIR_DAT T3 ON T1.YEAR = T3.YEAR ");
            stb.append("        AND T1.SEMESTER = T3.SEMESTER ");
            stb.append("        AND T1.CHAIRCD = T3.CHAIRCD ");
            stb.append("    LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = T2.SCHREGNO ");
            stb.append("        AND REGD.YEAR = T2.YEAR ");
            stb.append("        AND REGD.SEMESTER = T2.SEMESTER ");
            // 留学
            stb.append("    LEFT JOIN SCHREG_TRANSFER_DAT T_TRANT1 ON T_TRANT1.SCHREGNO = T2.SCHREGNO ");
            stb.append("        AND T_TRANT1.TRANSFERCD = '1' ");
            stb.append("        AND T1.EXECUTEDATE BETWEEN T_TRANT1.TRANSFER_SDATE AND T_TRANT1.TRANSFER_EDATE ");
            // 休学
            stb.append("    LEFT JOIN SCHREG_TRANSFER_DAT T_TRANT2 ON T_TRANT2.SCHREGNO = T2.SCHREGNO ");
            stb.append("        AND T_TRANT2.TRANSFERCD = '2' ");
            stb.append("        AND T1.EXECUTEDATE BETWEEN T_TRANT2.TRANSFER_SDATE AND T_TRANT2.TRANSFER_EDATE ");
            stb.append("    WHERE ");
            stb.append("        T1.YEAR = '" + param._year+ "' ");
            stb.append("        AND T1.EXECUTEDATE BETWEEN DATE('" + param._date1 + "') AND DATE('" + param._date2 + "') ");
            stb.append("        AND T2.SCHREGNO = ? ");
            if (param.defineSchool.usefromtoperiod) {
                stb.append("        AND EXISTS (SELECT 'X' FROM PERIOD_TERM T3 WHERE T1.PERIODCD BETWEEN T3.S_PERIODCD AND T3.E_PERIODCD) ");
            }
            stb.append("        AND NOT EXISTS(SELECT 'X' FROM SCHREG_BASE_MST BASE ");
            stb.append("                     WHERE  BASE.SCHREGNO = T2.SCHREGNO ");
            stb.append("                        AND ((ENT_DIV IN('4','5') AND T1.EXECUTEDATE < ENT_DATE) ");
            stb.append("                          OR (GRD_DIV IN('2','3') AND T1.EXECUTEDATE > GRD_DATE))) ");
            stb.append("    AND NOT EXISTS(SELECT ");
            stb.append("                       'X' ");
            stb.append("                   FROM ");
            stb.append("                       ATTEND_DAT T_ATE ");
            stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT ATDD ON ATDD.YEAR = '" + param._year + "' AND ATDD.DI_CD = T_ATE.DI_CD ");
            stb.append("                   WHERE ");
            stb.append("                       T_ATE.SCHREGNO = T2.SCHREGNO ");
            stb.append("                       AND T_ATE.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append("                       AND ATDD.REP_DI_CD = '27' "); // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
            stb.append("                  ) ");
            // 勤怠コード'28'は時間割にカウントしない
            stb.append("    AND NOT EXISTS(SELECT ");
            stb.append("                       'X' ");
            stb.append("                   FROM ");
            stb.append("                       ATTEND_DAT T_ATE ");
            stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT ATDD ON ATDD.YEAR = '" + param._year + "' AND ATDD.DI_CD = T_ATE.DI_CD ");
            stb.append("                   WHERE ");
            stb.append("                       T_ATE.SCHREGNO = T2.SCHREGNO ");
            stb.append("                       AND T_ATE.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append("                       AND T_ATE.PERIODCD = T1.PERIODCD ");
            stb.append("                       AND ATDD.REP_DI_CD = '28' ");
            stb.append("                  ) ");
            if (param._hasSchChrDatExecutediv) {
                stb.append("        AND VALUE(T1.EXECUTEDIV, '0') <> '2' "); // 休講は対象外
            }
            stb.append(") ");
            stb.append("SELECT ");
            stb.append("    T1.SCHREGNO, ");
            stb.append("    T1.EXECUTEDATE, ");
            stb.append("    T1.PERIODCD, ");
            stb.append("    T1.CHAIRCD, ");
            stb.append("    T1.CHAIRNAME, ");
            stb.append("    T1.IS_OFFDAYS, ");
            stb.append("    T1.IS_ABROAD, ");
            stb.append("    VALUE(ATDD.REP_DI_CD,'0') AS REP_DI_CD, ");
            stb.append("    VALUE(L1.DI_CD, '0') AS DI_CD, ");
            stb.append("    L1.DI_REMARK ");
            stb.append("FROM ");
            stb.append("    SCH_PERIOD_DATA T1 ");
            stb.append("    LEFT JOIN ATTEND_DAT L1 ON L1.SCHREGNO = T1.SCHREGNO ");
            stb.append("        AND L1.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append("        AND L1.PERIODCD = T1.PERIODCD ");
            stb.append("        AND L1.YEAR = T1.YEAR ");
            stb.append("    LEFT JOIN ATTEND_DI_CD_DAT ATDD ON ATDD.YEAR = '" + param._year + "' AND ATDD.DI_CD = L1.DI_CD ");
            stb.append(" ORDER BY ");
            stb.append("    T1.EXECUTEDATE, T1.PERIODCD ");
            
            return stb.toString();
        }
    }
    
    private static class ScheduleAttendance {
        final String _periodcd;
        final String _chaircd;
        final String _chairname;
        final Integer _repDiCd;
        final Integer _diCd;
        public ScheduleAttendance(final String periodcd, final String chaircd, final String chairname, final Integer repdicd, final Integer dicd) {
            _periodcd = periodcd;
            _chaircd = chaircd;
            _chairname = chairname;
            _repDiCd = repdicd;
            _diCd = dicd;
        }
        public String toString() {
        	return "(repDiCd=" + _repDiCd + ", diCd=" + _diCd + ")";
        }
    }

    /*
     * get parameter doGet()パラメータ受け取り
     */
    private Param getParam(final DB2UDB db2, final HttpServletRequest request) {
        KNJServletUtils.debugParam(request, log);
        log.fatal(" $Revision: 76375 $ $Date: 2020-09-03 13:15:08 +0900 (木, 03 9 2020) $");
        return new Param(db2, request);
    }

    private static class Param {
        final String _year;
        final String _semester;
        final String _ctrlDate;
        final String[] _categorySelected;
        final String _date1;
        final String _date2;
        final String _gradehrclass;
        final String _output;
        final String _useCurriculumcd;
        final String _useTestCountflg;
        final String _useVirus;
        final String _useKoudome;
        final boolean _hasSchChrDatExecutediv;
        final String _knjc110bUsePeriod12Form;
        final String _now;
        private Integer[] suspendCds = {}; // 出停のコード
        
        final KNJDefineSchool defineSchool;       //各学校における定数等設定
        final KNJSchoolMst _knjSchoolMst;
        final Map<String, Map<String, Integer>> _courseCdperiodCdMap = new TreeMap();
        final Map<String, Map<String, String>> _courseCdperiodNameMap = new TreeMap();
        final Map<Integer, String> _kintaiNameMap = new HashMap();

        final boolean _isOutputDebug;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year  = request.getParameter("YEAR");           //年度
            _semester  = request.getParameter("SEMESTER");       //学期
            _ctrlDate = request.getParameter("CTRL_DATE");
            _categorySelected = request.getParameterValues("category_selected");  // 学籍番号;
            //日付型を変換
            _date1 = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE1"));            //印刷範囲開始
            _date2 = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE2"));            //印刷範囲開始
            _gradehrclass = request.getParameter("GRADE_HR_CLASS");  // 学年・組
            _output = request.getParameter("OUTPUT");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
            
            final Calendar cal = Calendar.getInstance();
            final DecimalFormat df2 = new DecimalFormat("00");
            _now = KNJ_EditDate.h_format_JP(db2, _ctrlDate) + " " + df2.format(cal.get(Calendar.HOUR_OF_DAY)) + ":" + df2.format(cal.get(Calendar.MINUTE));
            
            KNJSchoolMst knjSchoolMst = null;
            try {
                knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (SQLException e) {
                log.debug("exception!", e);
            }
            _knjSchoolMst = knjSchoolMst;
            
            //  欠課数換算定数取得 => KNJDefineCodeImpを実装したオブジェクトを作成  05/06/18Build
            defineSchool = new KNJDefineSchool();
            defineSchool.defineCode (db2, _year);         //各学校における定数等設定
            log.debug("semesdiv = " + defineSchool.semesdiv + "   absent_cov = " + defineSchool.absent_cov + "   absent_cov_late = " + defineSchool.absent_cov_late);
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useTestCountflg = request.getParameter("useTestCountflg"); // テスト項目マスタテーブル
            _knjc110bUsePeriod12Form = request.getParameter("knjc110bUsePeriod12Form");
            _hasSchChrDatExecutediv = KnjDbUtils.setTableColumnCheck(db2, "SCH_CHR_DAT", "EXECUTEDIV");
            setPeriodMap(db2);
            setKintaiMap(db2);
            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));
        }
        
        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
        	return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJC110B' AND NAME = '" + propName + "' "));
        }
        
        private void setPeriodMap(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT T1.COURSECD, NAMECD2, NAME1, ROW_NUMBER() OVER(PARTITION BY T1.COURSECD ORDER BY NAMECD2) AS ROW ");
            stb.append(" FROM COURSE_MST T1 ");
            stb.append("      INNER JOIN V_NAME_MST T2 ON T2.YEAR = '" + _year + "' AND T2.NAMECD1 = 'B001' AND T2.NAMECD2 BETWEEN T1.S_PERIODCD AND T1.E_PERIODCD ");
            stb.append(" ORDER BY T1.COURSECD, T2.NAMECD2 ");
            
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    getMappedMap(_courseCdperiodCdMap, rs.getString("COURSECD")).put(rs.getString("NAMECD2"), new Integer(rs.getString("ROW")));
                    getMappedMap(_courseCdperiodNameMap, rs.getString("COURSECD")).put(rs.getString("NAMECD2"), rs.getString("NAME1"));
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        
        
        private void setKintaiMap(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DI_CD, DI_MARK FROM ATTEND_DI_CD_DAT WHERE YEAR = '" + _year + "' ORDER BY DI_CD ");
            
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    if (NumberUtils.isDigits(rs.getString("DI_CD"))) {
                        _kintaiNameMap.put(Integer.valueOf(rs.getString("DI_CD")), rs.getString("DI_MARK"));
                    }
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
    }
}
