/*
 * $Id: e606a4503484948725f067b6eacb6cac84680f09 $
 *
 * 作成日: 2011/05/16
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;


import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;

/**
 * 学校教育システム 賢者 [成績管理]  共愛学園・中学成績通知票
 */

public class KNJD183J {

    private static final Log log = LogFactory.getLog(KNJD183J.class);
    
    private static String SEMES9 = "9";
    
    private static String SEMES3 = "3";

    private boolean _hasData;

    Param _param;
    
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
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
            svf.VrQuit();
        }

    }
    
    private int getMS932ByteLength(final String str) {
        int len = 0;
        if (null != str) {
            try {
                len = str.getBytes("MS932").length;
            } catch (final UnsupportedEncodingException e) {
                log.debug("exception!", e);
            }
        }
        return len;
    }
    
    public static List getToken(final String str, final int len) {
        return KNJ_EditKinsoku.getTokenList(str, len, 30);
    }
    
    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        
        final List viewClassList = ViewClass.getViewClassList(db2, _param);

        final List studentList = Student.getStudentList(db2, _param);

        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            
            final Student student = (Student) it.next();
            
            load(db2, student);
            
            printHyosi(svf, student);
            
            printMain(svf, student, viewClassList);
            
            _hasData = true;
        }
    }

    private void load(final DB2UDB db2, final Student student) {
        student._viewRecordList = ViewRecord.getViewRecordList(db2, _param, student._schregno);
        student._viewValuationList = ViewValuation.getViewValuationList(db2, _param, student._schregno);
        student._monthAttendances = Attendance.load(db2, _param, student._schregno);
        student._behaviorSemesDatList = BehaviorSemesDat.load(db2, _param, student._schregno);
        student._hreportremarkDatList = HreportremarkDat.load(db2, _param, student._schregno);
        student._hreportremarkDatDetailMap = HreportremarkDetailDat.load(db2, _param, student._schregno);
        student._hreportremarkDatDetailMap3 = HreportremarkDetailDat3.load(db2, _param, student._schregno);
        student._medexamDetDat = MedexamDat.getMedexamDetDat(db2, _param, student._schregno);
        student._sportsScoreDatMap = SportsScoreDat.getSportsScoreDatMap(db2, _param, student._schregno);
    }
    
    private void printHyosi(final Vrw32alp svf, final Student student) {
        svf.VrSetForm("KNJD183J_1.frm", 1);
        
        printHyosiHeader(svf, student);
        
        printTokubetsuKatsudou(svf, student);
        
        printAttendances(svf, student);
        
        printKenkou(svf, student);
        
        printUndou(svf, student);
        
        svf.VrEndPage();
    }

    private void printMain(final Vrw32alp svf, final Student student, final List viewClassList) {
        svf.VrSetForm("KNJD183J_2.frm", 4);
        
        svf.VrsOut(getMS932ByteLength(student._name) < 20 ? "NAME" : "NAME2", student._name);
        svf.VrsOut("ATTENDNO", student._attendno);
        svf.VrsOut("ATTENDNO2", student._attendno);
        
        svf.VrsOut(getMS932ByteLength(student._staffname) > 20 ? "STAFFNAME2_2" : "STAFFNAME2_1", student._staffname);
        
        printTsushinran(svf, student);
        
        printSougou(svf, student);
        
        printKoudou(svf, student);
        
        printGakushu(svf, student, viewClassList);
    }
    
    private static String trimLeft(final String s) {
        if (null == s) {
            return null;
        }
        String rtn = s;
        for (int i = 0; i < s.length(); i++) {
            final char ch = s.charAt(i);
            if (ch != ' ' && ch != '　') {
                rtn = s.substring(i);
                break;
            }
        }
        return rtn;
    }

    private void printHyosiHeader(final Vrw32alp svf, final Student student) {
        svf.VrsOut(getMS932ByteLength(student._name) < 20 ? "NAME" : "NAME2", student._name);
        svf.VrsOut(getMS932ByteLength(student._name) < 20 ? "NAME3" : "NAME4", student._name);
        svf.VrsOut(getMS932ByteLength(student._staffname) < 20 ? "STAFFNAME2_1" : "STAFFNAME2_2", student._staffname);
        svf.VrsOut("STAFFNAME1_1", _param._principalname);
        svf.VrsOut("SCHOOLNAME", _param._schoolname);
        
        svf.VrsOut("HR_NAME", _param._hyosiHrname);
        svf.VrsOut("ATTENDNO", student._attendno);
        
        if (null != _param.getFilePath()) {
            svf.VrsOut("LOGO", _param.getFilePath());
        }
        
        if (SEMES9.equals(_param._semester)) {
            svf.VrsOut("JOB", _param._jobname);
            svf.VrsOut("STAFFNAME1_3", trimLeft(_param._principalname));
            svf.VrsOut("GRADE", _param._gradeCd);
            svf.VrsOut("DATE", _param.getDateString(_param._ctrlDate));
        }
    }
    
    private void printUndou(final Vrw32alp svf, final Student student) {
        
//      "060"; // 50m走
        final SportsScoreDat ss1 = (SportsScoreDat) student._sportsScoreDatMap.get("060");
        if (null != ss1) {
            svf.VrsOut("RUN", toBigDecimalSetScale(ss1._record, 1));
        }
//      "070"; // 立ち幅とび
        final SportsScoreDat ss2 = (SportsScoreDat) student._sportsScoreDatMap.get("070");
        if (null != ss2) {
            svf.VrsOut("JUMP", toBigDecimalSetScale(ss2._record, 0));
        }
//      "080"; // ハンドボール投げ
        final SportsScoreDat ss3 = (SportsScoreDat) student._sportsScoreDatMap.get("080");
        if (null != ss3) {
            svf.VrsOut("THROW", toBigDecimalSetScale(ss3._record, 0));
        }
//      "040"; // 反復横とび
        final SportsScoreDat ss4 = (SportsScoreDat) student._sportsScoreDatMap.get("040");
        if (null != ss4) {
            svf.VrsOut("SIDE_STEP", toBigDecimalSetScale(ss4._record, 0));
        }
//      "090"; // 持久走
        final SportsScoreDat ss5 = (SportsScoreDat) student._sportsScoreDatMap.get("090");
        if (null != ss5) {
            svf.VrsOut("ENDURANCE_RUN", toBigDecimalSetScale(ss5._record, 2));
        }
//      "010"、"011"; // 握力左、握力右 => 握力は左と右の平均を出力する。片方しか値が無い場合はその値をそのまま出力する。
        final SportsScoreDat ss6l = (SportsScoreDat) student._sportsScoreDatMap.get("010");
        final SportsScoreDat ss6r = (SportsScoreDat) student._sportsScoreDatMap.get("011");
        if (null != ss6l || null != ss6r) {
            final BigDecimal v1 = null == ss6l || null == ss6l._record || !NumberUtils.isNumber(ss6l._record) ? null : new BigDecimal(ss6l._record);
            final BigDecimal v2 = null == ss6r || null == ss6r._record || !NumberUtils.isNumber(ss6r._record) ? null : new BigDecimal(ss6r._record);
            int denom = 0;
            if (null != v1) { denom += 1; }
            if (null != v2) { denom += 1; }
            final BigDecimal zero = new BigDecimal(0);
            final BigDecimal sum = (null == v1 ? zero : v1).add(null == v2 ? zero : v2);
            if (0 != denom) {
                final BigDecimal avg = sum.divide(new BigDecimal(denom), 0, BigDecimal.ROUND_HALF_UP);
                svf.VrsOut("GRIP", avg.toString());
            }
        }
//      "020"; // 上体おこし
        final SportsScoreDat ss7 = (SportsScoreDat) student._sportsScoreDatMap.get("020");
        if (null != ss7) {
            svf.VrsOut("UPPER", toBigDecimalSetScale(ss7._record, 0));
        }
//      "030"; // 長座体前屈
        final SportsScoreDat ss8 = (SportsScoreDat) student._sportsScoreDatMap.get("030");
        if (null != ss8) {
            svf.VrsOut("BEND", toBigDecimalSetScale(ss8._record, 0));
        }
//      "999"; // 総合判定
        final SportsScoreDat ss9 = (SportsScoreDat) student._sportsScoreDatMap.get("999");
        if (null != ss9) {
            svf.VrsOut("TOTAL_JUDGE", ss9._value);
        }
    }
    
    private String toBigDecimalSetScale(final String str, int scale) {
        return NumberUtils.isNumber(str) ? new BigDecimal(str).setScale(scale, BigDecimal.ROUND_HALF_UP).toString() : str;
    }

    private void printKenkou(final Vrw32alp svf, final Student student) {
        if (null != student._medexamDetDat) {
            final MedexamDat mdd = student._medexamDetDat;
            svf.VrsOut("HEIGHT", mdd._height);
            svf.VrsOut("WEIGHT", mdd._weight);
            svf.VrsOut("SEATED_HEIGHT", mdd._sitheight);
            svf.VrsOut("RIGHT_EYE", mdd._rBarevisionMark);
            svf.VrsOut("LEFT_EYE", mdd._lBarevisionMark);
            svf.VrsOut("TOOTH", String.valueOf(mdd._misyochiSu));
        }
    }

    private void printTokubetsuKatsudou(final Vrw32alp svf, final Student student) {
        final String codeSeitokai = "01";
        final String codeGakkyu   = "02";
        final String codeClub     = "03";
        final String codeSonota   = "04";
        final String[] codes = new String[]{codeSeitokai, codeGakkyu, codeClub, codeSonota};
        for (int i = 0; i < codes.length; i++) {
            svf.VrsOutn("SPECIAL_ACT", i + 1, (String) student._hreportremarkDatDetailMap.get(codes[i]));
        }
    }

    private void printAttendances(final Vrw32alp svf, final Student student) {
        final Attendance dummy = new Attendance(null, null, null);
        final Attendance total = new Attendance(null, null, null);
        
        final DecimalFormat df = new DecimalFormat("00");
        final List monthList = new ArrayList();
        final String emonth = _param._date.substring(5, 7);
        if (Integer.parseInt(emonth) < 4) {
            for (int m = 4; m <= 12; m++) {
                monthList.add(df.format(m));
            }
            for (int m = 1; m <= Integer.parseInt(emonth); m++) {
                monthList.add(df.format(m));
            }
        } else {
            for (int m = 4; m <= Integer.parseInt(emonth); m++) {
                monthList.add(df.format(m));
            }
        }
        
        for (final Iterator it = monthList.iterator(); it.hasNext();) {
            final String month = (String) it.next();
            
            final Attendance att = (null == student._monthAttendances.get(month)) ? dummy : (Attendance) student._monthAttendances.get(month);
            
            // log.debug(" attendance month = " + att._month);
            
            final int i = Integer.parseInt(month) + (_param.isNewYear(month) ? 9 : -3);
            
            svf.VrsOutn("LESSON", i,String.valueOf(att._lesson));
            svf.VrsOutn("ATTEND", i,String.valueOf(att._kesseki));
            svf.VrsOutn("ABSENT1", i,String.valueOf(att._kekkaChohai));
            svf.VrsOutn("ABSENT2", i,String.valueOf(att._kekkaJisu));
            svf.VrsOutn("LATE1", i,String.valueOf(att._lateChohai));
            svf.VrsOutn("LATE2", i,String.valueOf(att._lateJisu));
            svf.VrsOutn("EARLY", i,String.valueOf(att._early));
            svf.VrsOutn("MOURNING", i,String.valueOf(att._suspendMourning));
            total.add(att);
        }
        
        svf.VrsOut("TOTAL_LESSON", String.valueOf(total._lesson));
        svf.VrsOut("TOTAL_ATTEND", String.valueOf(total._kesseki));
        svf.VrsOut("TOTAL_ABSENT1", String.valueOf(total._kekkaChohai));
        svf.VrsOut("TOTAL_ABSENT2", String.valueOf(total._kekkaJisu));
        svf.VrsOut("TOTAL_LATE1", String.valueOf(total._lateChohai));
        svf.VrsOut("TOTAL_LATE2", String.valueOf(total._lateJisu));
        svf.VrsOut("TOTAL_EARLY", String.valueOf(total._early));
        svf.VrsOut("TOTAL_MOURNING", String.valueOf(total._suspendMourning));
        
        printAttendrecremark(svf, student);
    }

    /**
     * 学習の記録を印字する
     * @param svf
     * @param student
     */
    private void printGakushu(final Vrw32alp svf, final Student student, final List viewClassList) {
        final List hisshuClassList = new ArrayList();
        final List sentakuClassList = new ArrayList();
        for (final Iterator it = viewClassList.iterator(); it.hasNext();) {
            final ViewClass viewClass = (ViewClass) it.next();
            if ("1".equals(viewClass._electDiv)) {
                sentakuClassList.add(viewClass);
            } else {
                hisshuClassList.add(viewClass);
            }
        }
        
        // 選択科目表示
        int classc = 1;
        for (final Iterator it = sentakuClassList.iterator(); it.hasNext();) {
            final ViewClass vc = (ViewClass) it.next();
            
            final String jj = classc > 3 ? "4" : "3";
            final int ji = classc > 3 ? classc - 3 : classc;
            
            for (int i = 0; i < vc.getViewSize(); i++) {
                
                svf.VrsOutn("SUBJECTNAME" + jj, ji, vc._classname);
                
                svf.VrsOutn("VIEWNAME" + jj + "_" + (i + 1), ji, vc.getViewName(i)); // 観点名称
                
                final ViewRecord vr = (ViewRecord) student.getViewMap(vc.getViewCd(i)).get(SEMES9);
                if (null != vr) {
                    svf.VrsOutn("VIEW" + jj + "_" + (i + 1), ji, vr._status); // 観点
                }
                
                final ViewValuation vv = (ViewValuation) student.getValueMap(vc._keycd).get(SEMES9);
                if (null != vv) {
                    svf.VrsOutn("RATE" + jj + "_2",  ji, vv.getSentakuValue()); // 評定
                }
            }
            classc += 1;
        }
        
        // 評定平均表示
        final Map hyoteiMap999999 = student.getValueMap("99");
        final String[] hyoteiSemesters = {"1", "2", SEMES9};
        for (int si = 0; si < hyoteiSemesters.length; si++) {
            final String semester = hyoteiSemesters[si];
            final String field = "AVE_RATE" + (SEMES9.equals(semester) ? "3" : semester);
            final ViewValuation vv = (ViewValuation) hyoteiMap999999.get(semester);
            if (null == vv) {
                svf.VrsOut(field, "99");
                svf.VrAttribute(field, "X=10000");
            } else {
                svf.VrsOut(field, vv._avg); // 評定平均
            }
        }
        
        // 必修教科表示
        final int LINES = 2;
        int line = 0; // 観点の行数
        for (final Iterator it = hisshuClassList.iterator(); it.hasNext();) {
            final ViewClass vc = (ViewClass) it.next();
            
            int count = 0;
            final List classname = vc.getClassnameCharacterList();
            
            for (int i = 0; i < vc.getViewSize(); i++) {
                svf.VrsOut("SUBJECTGRP", vc._classcd);
                
                final String viewname = vc.getViewName(i);
                final int k = i % LINES + 1;
                svf.VrsOut("SUBJECTNAME" + k, (String) classname.get(i));
                svf.VrsOut("VIEWNAME" + k, viewname); // 観点名称
                
                final ViewRecord vr = (ViewRecord) student.getViewMap(vc.getViewCd(i)).get(SEMES9);
                if (null != vr) {
                    svf.VrsOut("VIEW" + k, vr._status); // 観点
                }
                
                final Map hyoteiMap = student.getValueMap(vc._keycd);
                for (int si = 0; si < hyoteiSemesters.length; si++) {
                    final String semester = hyoteiSemesters[si];
                    final String field = "RATE" + (SEMES9.equals(semester) ? "3" : semester);
                    final ViewValuation vv = (ViewValuation) hyoteiMap.get(semester);
                    if (null == vv) {
                        svf.VrsOut(field, vc._keycd);
                        svf.VrAttribute(field, "X=10000");
                    } else {
                        svf.VrsOut(field, vv._value); // 評定
                    }
                }
                
                if (k == LINES) {
                    line += 1;
                    svf.VrEndRecord();
                }
                count = i % LINES;
            }
            for (int i = vc.getViewSize(); i < classname.size(); i++) {
                final int k = i % LINES + 1;
                svf.VrsOut("SUBJECTGRP", vc._classcd);
                svf.VrsOut("SUBJECTNAME" + k, (String) classname.get(i));
                final Map hyoteiMap = student.getValueMap(vc._keycd);
                for (int si = 0; si < hyoteiSemesters.length; si++) {
                    final String semester = hyoteiSemesters[si];
                    final String field = "RATE" + (SEMES9.equals(semester) ? "3" : semester);
                    final ViewValuation vv = (ViewValuation) hyoteiMap.get(semester);
                    if (null == vv) {
                        svf.VrsOut(field, vc._keycd);
                        svf.VrAttribute(field, "X=10000");
                    } else {
                        svf.VrsOut(field, vv._value); // 評定
                    }
                }
                if (k == LINES) {
                    line += 1;
                    svf.VrEndRecord();
                }
                count = i % LINES;
            }
            if (0 == count) {
                line += 1;
                svf.VrEndRecord();
            }
        }
        
        // 空行挿入
        final int maxLine = 22;
        for (int i = line == maxLine ? maxLine : line % maxLine; i < maxLine; i++) {
            svf.VrsOut("SUBJECTGRP", String.valueOf(i));
            svf.VrEndRecord();
        }
    }

    private void printKoudou(final Vrw32alp svf, final Student student) {
        for (final Iterator it = student._behaviorSemesDatList.iterator(); it.hasNext();) {
            final BehaviorSemesDat bsd = (BehaviorSemesDat) it.next();
            if (NumberUtils.isDigits(bsd._semester) && NumberUtils.isDigits(bsd._code)) {
                svf.VrsOutn("ACTION" + Integer.parseInt(bsd._semester), Integer.parseInt(bsd._code), bsd._mark);
            }
        }
    }

    private void printSougou(final Vrw32alp svf, final Student student) {
        final List gakusyukatsudo = new ArrayList();
        final List kanten = new ArrayList();
        final List hyouka = new ArrayList();

        for (final Iterator it = student._hreportremarkDatList.iterator(); it.hasNext();) {
            final HreportremarkDat hr = (HreportremarkDat) it.next();
            if (!SEMES9.equals(hr._semester)) { // 9学期のみ表示
                continue;
            }
            if (null != hr._totalstudytime) gakusyukatsudo.addAll(getToken(hr._totalstudytime, 26));
        }
        final String remark1Kanten = (String) student._hreportremarkDatDetailMap3.get("03");
        final String remark1Hyouka = (String) student._hreportremarkDatDetailMap3.get("04");
        if (null != remark1Hyouka) hyouka.addAll(getToken(remark1Hyouka, 32));
        if (null != remark1Kanten) kanten.addAll(getToken(remark1Kanten, 26));
        int i;
        i = 1;
        for (final Iterator it = gakusyukatsudo.iterator(); it.hasNext();) {
            final String text = (String) it.next();
            svf.VrsOutn("TOTAL_ACT1", i, text);
            i += 1;
        }
        i = 1;
        for (final Iterator it = kanten.iterator(); it.hasNext();) {
            final String text = (String) it.next();
            svf.VrsOutn("TOTAL_ACT2", i, text);
            i += 1;
        }
        i = 1;
        for (final Iterator it = hyouka.iterator(); it.hasNext();) {
            final String text = (String) it.next();
            svf.VrsOutn("TOTAL_ACT3", i, text);
            i += 1;
        }
    }

    private void printTsushinran(final Vrw32alp svf, final Student student) {
        for (final Iterator it = student._hreportremarkDatList.iterator(); it.hasNext();) {
            final HreportremarkDat hr = (HreportremarkDat) it.next();
            if (SEMES9.equals(hr._semester)) { // 9学期以外を連結して表示
                continue;
            }
            final List communicationList = getToken(hr._communication, 50);
            int i = 1;
            for (final Iterator itc = communicationList.iterator(); itc.hasNext();) {
                final String text = (String) itc.next();
                svf.VrsOutn("COMMUNICATION" + Integer.parseInt(hr._semester), i, text);
                i += 1;
            }
        }
    }

    private void printAttendrecremark(final Vrw32alp svf, final Student student) {
        final StringBuffer stb = new StringBuffer();
        for (final Iterator it = student._hreportremarkDatList.iterator(); it.hasNext();) {
            final HreportremarkDat hr = (HreportremarkDat) it.next();
            if (SEMES9.equals(hr._semester)) { // 9学期以外を連結して表示
                continue;
            }
            if (null != hr._attendrecRemark) {
                stb.append(hr._attendrecRemark);
            }
        }
        svf.VrsOut("REMARK", stb.toString());
    }

    private static String getKeycd(final ResultSet rs, final Param param) throws SQLException {
        final String classCd = rs.getString("CLASSCD");
        if ("99".equals(classCd) || "33".equals(classCd) || "55".equals(classCd)) {
            return classCd;
        }
        if ("1".equals(param._useCurriculumcd)) {
            return rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-" + rs.getString("CURRICULUM_CD");
        }
        return classCd;
    }
    
    /**
     * 観点の教科
     */
    private static class ViewClass {
        final String _classcd;
        final String _keycd;
        final String _classname;
        final String _electDiv;
        final List _viewList;
        final List _valuationList;
        ViewClass(
                final String classcd,
                final String keycd,
                final String classname,
                final String electDiv) {
            _classcd = classcd;
            _keycd = keycd;
            _classname = classname;
            _electDiv = electDiv;
            _viewList = new ArrayList();
            _valuationList = new ArrayList();
        }
        
        public void addView(final String viewcd, final String viewname) {
            _viewList.add(new String[]{viewcd, viewname});
        }
        
        public String getViewCd(final int i) {
            return ((String[]) _viewList.get(i))[0];
        }
        
        public String getViewName(final int i) {
            return ((String[]) _viewList.get(i))[1];
        }
        
        public int getViewSize() {
            return _viewList.size();
        }
        
        public List getClassnameCharacterList() {
            if (null == _classname || "".equals(_classname)) {
                return Collections.EMPTY_LIST;
            }
            List rtn = new ArrayList();
            if (_classname.length() >= getViewSize()) {
                for (int i = 0; i < _classname.length(); i++) {
                    rtn.add(String.valueOf(_classname.charAt(i)));
                }
            } else {
                final int st = (getViewSize() - _classname.length()) / 2; // センタリング
                int count = 0;
                for (int i = 0; i < st; i++) {
                    rtn.add("");
                    count += 1;
                }
                for (int i = st, ci = 0; i < st + _classname.length(); i++, ci++) {
                    rtn.add(String.valueOf(_classname.charAt(ci)));
                    count += 1;
                }
                for (int i = count; i < getViewSize(); i++) {
                    rtn.add("");
                }
            }
            return rtn;
        }
        
        public static List getViewClassList(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getViewClassSql(param);
                // log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    
                    final String classcd = rs.getString("CLASSCD");
                    final String keycd = getKeycd(rs, param);
                    final String classname = rs.getString("CLASSNAME");
                    final String electDiv = rs.getString("ELECTDIV");
                    final String viewcd = rs.getString("VIEWCD");
                    final String viewname = rs.getString("VIEWNAME");
                    
                    ViewClass viewClass = null;
                    for (final Iterator it = list.iterator(); it.hasNext();) {
                        final ViewClass viewClass0 = (ViewClass) it.next();
                        if (viewClass0._keycd.equals(keycd)) {
                            viewClass = viewClass0;
                            break;
                        }
                    }
                    
                    if (null == viewClass) {
                        viewClass = new ViewClass(classcd, keycd, classname, electDiv);
                        list.add(viewClass);
                    }
                    
                    viewClass.addView(viewcd, viewname);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }
        
        private static String getViewClassSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.GRADE, ");
            stb.append("     T3.CLASSCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         T3.SCHOOL_KIND, ");
                stb.append("         T1.CURRICULUM_CD, ");
            }
            stb.append("     T3.CLASSNAME, ");
            stb.append("     VALUE(T3.ELECTDIV, '0') AS ELECTDIV, ");
            stb.append("     T1.VIEWCD, ");
            stb.append("     T1.VIEWNAME ");
            stb.append(" FROM ");
            stb.append("     JVIEWNAME_GRADE_MST T1 ");
            stb.append("     INNER JOIN JVIEWNAME_GRADE_YDAT T2 ON T2.YEAR = '" + param._year + "' ");
            stb.append("         AND T2.GRADE = T1.GRADE ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T2.CLASSCD = T1.CLASSCD  ");
                stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND  ");
                stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD  ");
            }
            stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD  ");
            stb.append("         AND T2.VIEWCD = T1.VIEWCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     INNER JOIN CLASS_MST T3 ON T3.CLASSCD || '-' || T3.SCHOOL_KIND = ");
                stb.append("                                T1.CLASSCD || '-' || T1.SCHOOL_KIND ");
            } else {
                stb.append("     INNER JOIN CLASS_MST T3 ON T3.CLASSCD = SUBSTR(T1.VIEWCD, 1, 2) ");
            }
            stb.append(" WHERE ");
            stb.append("     T1.GRADE = '" + param._grade + "' ");
            stb.append(" ORDER BY ");
            stb.append("     VALUE(T3.ELECTDIV, '0'), ");
            stb.append("     VALUE(T3.SHOWORDER3, -1), ");
            stb.append("     T3.CLASSCD, ");
            stb.append("     VALUE(T1.SHOWORDER, -1), ");
            stb.append("     T1.VIEWCD ");
            return stb.toString();
        }
    }
    
    /**
     * 学習の記録（観点）
     */
    private static class ViewRecord {
        
        final String _semester;
        final String _viewcd;
        final String _status;
        final String _grade;
        final String _viewname;
        final String _classcd;
        final String _classMstShoworder;
        final String _showorder;
        ViewRecord(
                final String semester,
                final String viewcd,
                final String status,
                final String grade,
                final String viewname,
                final String classcd,
                final String classMstShoworder,
                final String showorder) {
            _semester = semester;
            _viewcd = viewcd;
            _status = status;
            _grade = grade;
            _viewname = viewname;
            _classcd = classcd;
            _classMstShoworder = classMstShoworder;
            _showorder = showorder;
        }
        
        public static List getViewRecordList(final DB2UDB db2, final Param param, final String schregno) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getViewRecordSql(param, schregno);
//                log.debug(" viewrecord sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    
                    final String semester = rs.getString("SEMESTER");
                    final String viewcd = rs.getString("VIEWCD");
                    final String status = rs.getString("STATUS");
                    final String grade = rs.getString("GRADE");
                    final String viewname = rs.getString("VIEWNAME");
                    final String classcd = rs.getString("CLASSCD");
                    final String classMstShoworder = rs.getString("CLASS_MST_SHOWORDER");
                    final String showorder = rs.getString("SHOWORDER");
                    
                    final ViewRecord viewRecord = new ViewRecord(semester, viewcd, status, grade, viewname, classcd, classMstShoworder, showorder);
                    
                    list.add(viewRecord);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }
        
        private static String getViewRecordSql(final Param param, final String schregno) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.GRADE   ");
            stb.append("     , T1.VIEWNAME ");
            stb.append("     , T1.VIEWCD ");
            stb.append("     , T3.YEAR ");
            stb.append("     , T3.SEMESTER ");
            stb.append("     , T3.SCHREGNO ");
            stb.append("     , T3.STATUS ");
            stb.append("     , T4.CLASSCD ");
            stb.append("     , T4.SHOWORDER AS CLASS_MST_SHOWORDER ");
            stb.append("     , T1.SHOWORDER ");
            stb.append(" FROM JVIEWNAME_GRADE_MST T1 ");
            stb.append("     INNER JOIN JVIEWNAME_GRADE_YDAT T2 ON T2.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        AND T2.CLASSCD = T1.CLASSCD ");
                stb.append("        AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("        AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("         AND T2.VIEWCD = T1.VIEWCD ");
            stb.append("     LEFT JOIN JVIEWSTAT_RECORD_DAT T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        AND T3.CLASSCD = T1.CLASSCD ");
                stb.append("        AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("        AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("         AND T3.VIEWCD = T1.VIEWCD ");
            stb.append("         AND T3.YEAR = T2.YEAR "); 
            stb.append("         AND T3.SEMESTER <= '" + param._semester + "' ");
            stb.append("         AND T3.SCHREGNO = '" + schregno + "' ");
            stb.append("     LEFT JOIN CLASS_MST T4 ON T4.CLASSCD = SUBSTR(T1.VIEWCD, 1, 2) ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ");
            }
            stb.append(" WHERE ");
            stb.append("     T2.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.GRADE = '" + param._grade + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T3.YEAR ");
            stb.append("     , T3.SEMESTER ");
            stb.append("     , VALUE(T4.SHOWORDER, 0) ");
            stb.append("     , T4.CLASSCD ");
            stb.append("     , VALUE(T1.SHOWORDER, 0) ");
            stb.append("     , T1.VIEWCD ");
            return stb.toString();
        }
    }
    
    /**
     * 学習の記録（評定）
     */
    private static class ViewValuation {
        final String _semester;
        final String _classcd;
        final String _keycd;
        final String _subclasscd;
        final String _subclassname;
        final String _value;
        final String _avg;
        ViewValuation(
                final String semester,
                final String classcd,
                final String keycd,
                final String subclasscd,
                final String subclassname,
                final String value,
                final String avg) {
            _semester = semester;
            _classcd = classcd;
            _keycd = keycd;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _value = value;
            _avg = avg;
        }
        
        /**
         * 選択科目の場合、固定で変換する
         */
        public String getSentakuValue() {
            final String value;
            if ("11".equals(_value)) {
                value = "A";
            } else if ("22".equals(_value)) {
                value = "B";
            } else if ("33".equals(_value)) {
                value = "C";
            } else {
                value = null;
            }
            return value;
        }
        
        public static List getViewValuationList(final DB2UDB db2, final Param param, final String schregno) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getViewValuationSql(param, schregno);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    
                    final String semester = rs.getString("SEMESTER");
                    final String classcd = rs.getString("CLASSCD");
                    final String keycd = getKeycd(rs, param);
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String subclassname = rs.getString("SUBCLASSNAME");
                    final String value = rs.getString("VALUE");
                    final String avg = NumberUtils.isNumber(rs.getString("AVG")) ? new BigDecimal(rs.getString("AVG")).setScale(1, BigDecimal.ROUND_HALF_UP).toString() : null;
                    final ViewValuation viewValuation = new ViewValuation(semester, classcd, keycd, subclasscd, subclassname, value, avg);
                    
                    list.add(viewValuation);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }
        
        private static String getViewValuationSql(final Param param, final String schregno) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T2.SEMESTER, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T2.CLASSCD, ");
                stb.append("     T2.SCHOOL_KIND, ");
                stb.append("     T2.CURRICULUM_CD, ");
                stb.append("     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD AS SUBCLASSCD, ");
            } else {
                stb.append("     SUBSTR(T2.SUBCLASSCD, 1, 2) AS CLASSCD, ");
                stb.append("     T2.SUBCLASSCD, ");
            }
            stb.append("     T4.SUBCLASSNAME, ");
            stb.append("     T2.SCORE AS VALUE, ");
            stb.append("     T2.AVG ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_DAT T2 ");
            stb.append("     LEFT JOIN SUBCLASS_MST T4 ON ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        T4.CLASSCD = T2.CLASSCD AND ");
                stb.append("        T4.SCHOOL_KIND = T2.SCHOOL_KIND AND ");
                stb.append("        T4.CURRICULUM_CD = T2.CURRICULUM_CD AND ");
            }
            stb.append("        T4.SUBCLASSCD = T2.SUBCLASSCD ");
            stb.append("     LEFT JOIN CLASS_MST T5 ON ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        T5.CLASSCD = T2.CLASSCD ");
                stb.append("        AND T5.SCHOOL_KIND = T2.SCHOOL_KIND ");
            } else {
                stb.append("        T5.CLASSCD = SUBSTR(T2.SUBCLASSCD, 1, 2) ");
            }
            stb.append(" WHERE ");
            stb.append("     T2.YEAR = '" + param._year + "' ");
            stb.append("     AND T2.SEMESTER <= '" + param._semester + "' ");
            stb.append("     AND T2.TESTKINDCD = '99' ");
            stb.append("     AND T2.TESTITEMCD = '00' ");
            stb.append("     AND T2.SCHREGNO = '" + schregno + "' ");
            if ("Y".equals(param._d016Namespare1)) {
                stb.append("     AND NOT EXISTS ( ");
                stb.append("         SELECT 'X' ");
                stb.append("         FROM ");
                stb.append("             SUBCLASS_REPLACE_COMBINED_DAT L1 ");
                stb.append("         WHERE ");
                stb.append("             L1.YEAR = T2.YEAR ");
                stb.append("             AND ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("                 L1.ATTEND_CLASSCD || '-' || L1.ATTEND_SCHOOL_KIND || '-' || L1.ATTEND_CURRICULUM_CD || '-' || ");
                }
                stb.append("                 L1.ATTEND_SUBCLASSCD = ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("                 T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
                }
                stb.append("                 T2.SUBCLASSCD ");
                
                stb.append("     ) ");
            }
            stb.append(" ORDER BY ");
            stb.append("     T5.SHOWORDER3, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
            }
            stb.append("     T2.SUBCLASSCD ");
            return stb.toString();
        }
    }
    
    /**
     * 身体の記録
     */
    private static class MedexamDat {
        
        final String _height;
        final String _weight;
        final String _sitheight;
        final String _rBarevisionMark;
        final String _lBarevisionMark;
        final String _rVisionMark;
        final String _lVisionMark;
        final String _toothOtherdiseasecd;
        final int _misyochiSu;
        
        public MedexamDat(
                final String height,
                final String weight,
                final String sitheight,
                final String rBarevisionMark,
                final String lBarevisionMark,
                final String rVisionMark,
                final String lVisionMark,
                final String toothOtherdiseasecd,
                final int misyochiSu) {
            _height = height;
            _weight = weight;
            _sitheight = sitheight;
            _rBarevisionMark = rBarevisionMark;
            _lBarevisionMark = lBarevisionMark;
            _rVisionMark = rVisionMark;
            _lVisionMark = lVisionMark;
            _toothOtherdiseasecd = toothOtherdiseasecd;
            _misyochiSu = misyochiSu;
        }

        public static MedexamDat getMedexamDetDat(final DB2UDB db2, final Param param, final String schregno) {
            MedexamDat medexamDetDat = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getMedexamDetDatSql(param, schregno);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    
                    final String height = rs.getString("HEIGHT");
                    final String weight = rs.getString("WEIGHT");
                    final String sitheight = rs.getString("SITHEIGHT");
                    final String rBarevisionMark = rs.getString("R_BAREVISION_MARK");
                    final String lBarevisionMark = rs.getString("L_BAREVISION_MARK");
                    final String rVisionMark = rs.getString("R_VISION_MARK");
                    final String lVisionMark = rs.getString("L_VISION_MARK");
                    final String toolthOtherdiseasecd = rs.getString("OTHERDISEASECD");
                    final int misyochiSu = NumberUtils.isDigits(rs.getString("MISYOCHI_SU")) ? Integer.parseInt(rs.getString("MISYOCHI_SU")) : 0;
                    
                    medexamDetDat = new MedexamDat(height, weight, sitheight,
                            rBarevisionMark, lBarevisionMark, rVisionMark, lVisionMark, toolthOtherdiseasecd, misyochiSu);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return medexamDetDat;
        }
        
        private static String getMedexamDetDatSql(final Param param, final String schregno) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH MEDEXAM_DET AS ( ");
            stb.append("   SELECT * FROM MEDEXAM_DET_DAT WHERE YEAR = '" + param._year + "' AND SCHREGNO = '" + schregno + "' ");
            stb.append(" ), MEDEXAM_TOOTH AS ( ");
            stb.append("   SELECT * FROM MEDEXAM_TOOTH_DAT WHERE YEAR = '" + param._year + "' AND SCHREGNO = '" + schregno + "' ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.HEIGHT, ");
            stb.append("     T1.WEIGHT, ");
            stb.append("     T1.SITHEIGHT, ");
            stb.append("     T1.R_BAREVISION_MARK, ");
            stb.append("     T1.L_BAREVISION_MARK, ");
            stb.append("     T1.R_VISION_MARK, ");
            stb.append("     T1.L_VISION_MARK, ");
            stb.append("     T2.OTHERDISEASECD, ");
            stb.append("     VALUE(T2.REMAINADULTTOOTH, 0) + VALUE(REMAINBABYTOOTH, 0) AS MISYOCHI_SU ");
            stb.append(" FROM ");
            stb.append("     MEDEXAM_DET T1 ");
            stb.append("     FULL OUTER JOIN MEDEXAM_TOOTH T2 ON T2.YEAR = T1.YEAR ");
            stb.append("        AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     VALUE(T1.YEAR, T2.YEAR) = '" + param._year + "' ");
            stb.append("     AND VALUE(T1.SCHREGNO, T2.SCHREGNO) = '" + schregno + "' ");
            return stb.toString();
        }
    }
    
    /**
     * 身体・運動のようす　運動
     */
    private static class SportsScoreDat {
        
        final String _itemcd;
        final String _date;
        final String _record;
        final String _score;
        final String _value;
        
        SportsScoreDat(
                final String itemcd,
                final String date,
                final String record,
                final String score,
                final String value) {
            _itemcd = itemcd;
            _date = date;
            _record = record;
            _score = score;
            _value = value;
        }
        
        public static Map getSportsScoreDatMap(final DB2UDB db2, final Param param, final String schregno) {
            final Map map = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getSportsScoreDatSql(param, schregno);
                
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    
                    final String itemcd = rs.getString("ITEMCD");
                    final String date = rs.getString("DATE");
                    final String record = rs.getString("RECORD");
                    final String score = rs.getString("SCORE");
                    final String value = rs.getString("VALUE");
                    
                    final SportsScoreDat sportsScoreDat = new SportsScoreDat(itemcd, date, record, score, value);
                    
                    map.put(itemcd, sportsScoreDat);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return map;
        }
        
        private static String getSportsScoreDatSql(final Param param, final String schregno) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.ITEMCD, ");
            stb.append("     T1.DATE, ");
            stb.append("     T1.RECORD, ");
            stb.append("     T1.SCORE, ");
            stb.append("     T1.VALUE ");
            stb.append(" FROM ");
            stb.append("     SPORTS_SCORE_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
            return stb.toString();
        }
    }
    
    /**
     * 生徒
     */
    private static class Student {
        final String _schregno;
        Map _monthAttendances;
        List _behaviorSemesDatList;
        List _hreportremarkDatList;
        Map _hreportremarkDatDetailMap;
        Map _hreportremarkDatDetailMap3;
        MedexamDat _medexamDetDat;
        Map _sportsScoreDatMap;
        List _viewRecordList;
        List _viewValuationList;
        
        String _name;
        String _attendno;
        String _hrname;
        String _staffname;
        
        public Student(
                final String schregno
        ) {
            _schregno = schregno;
            _monthAttendances = Collections.EMPTY_MAP;
            _behaviorSemesDatList = Collections.EMPTY_LIST;
            _hreportremarkDatList = Collections.EMPTY_LIST;
            _hreportremarkDatDetailMap = Collections.EMPTY_MAP;
            _hreportremarkDatDetailMap3 = Collections.EMPTY_MAP;
            _medexamDetDat = null;
            _sportsScoreDatMap = Collections.EMPTY_MAP;
            _viewRecordList = Collections.EMPTY_LIST;
            _viewValuationList = Collections.EMPTY_LIST;
        }
        
        /**
         * 学期と観点コードの観点のマップを得る
         * @param viewcd 観点コード 
         * @return 学期と観点コードの観点のマップ
         */
        public Map getViewMap(final String viewcd) {
            final Map rtn = new HashMap();
            if (null != viewcd) {
                for (Iterator it = _viewRecordList.iterator(); it.hasNext();) {
                    final ViewRecord viewRecord = (ViewRecord) it.next();
                    if (viewcd.equals(viewRecord._viewcd)) {
                        rtn.put(viewRecord._semester, viewRecord);
                    }
                }
            }
            return rtn;
        }
        
        /**
         * 学期と評定のマップを得る
         * @param classcd 評定の教科コード
         * @return 学期と評定のマップ
         */
        public Map getValueMap(final String keycd) {
            final Map rtn = new HashMap();
            if (null != keycd) {
                for (Iterator it = _viewValuationList.iterator(); it.hasNext();) {
                    final ViewValuation vv = (ViewValuation) it.next();
                    if (keycd.equals(vv._keycd)) {
                        rtn.put(vv._semester, vv);
                    }
                }
            }
            return rtn;
        }
        
        public static List getStudentList(final DB2UDB db2, final Param param) {
            final List students = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            String sql = null;
            try {
                sql = prestatementRegd(param);
                // log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                
                while (rs.next()) {
                    final Student student = new Student(rs.getString("SCHREGNO"));
                    student._name = "1".equals(rs.getString("USE_REAL_NAME")) ? rs.getString("REAL_NAME") : rs.getString("NAME");
                    student._attendno = NumberUtils.isNumber(rs.getString("ATTENDNO")) ? String.valueOf(Integer.parseInt(rs.getString("ATTENDNO"))) : rs.getString("ATTENDNO");
                    student._hrname = rs.getString("HR_NAME");
                    student._staffname = rs.getString("STAFFNAME");
                    students.add(student);
                }
                
            } catch (SQLException ex) {
                log.debug("Exception: sql = " + sql, ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            
            return students;
        }
        /** 
         * <pre>
         * 生徒の学籍等の情報および総合的な学習の時間の所見・通信欄を取得するＳＱＬ文を戻します。
         * ・指定された生徒全員を対象とします。
         * </pre>
         */
        public static String prestatementRegd(final Param param) {
            final StringBuffer stb = new StringBuffer();
            // 異動者を除外した学籍の表 => 組平均において異動者の除外に使用
            stb.append("WITH SCHNO_A AS(");
            stb.append(    "SELECT  T1.SCHREGNO, T1.GRADE, T1.ATTENDNO, T1.SEMESTER, T1.COURSECD, T1.MAJORCD, T1.COURSECODE ");
            stb.append(    "FROM    SCHREG_REGD_DAT T1,V_SEMESTER_GRADE_MST T2 ");
            stb.append(    "WHERE   T1.YEAR = '" + param._year + "' ");
            if (SEMES9.equals(param._semester)) {
                stb.append(        "AND T1.SEMESTER = '"+ param._ctrlSemester +"' ");
            } else {
                stb.append(        "AND T1.SEMESTER = '"+ param._semester +"' ");
            }
            stb.append(        "AND T1.YEAR = T2.YEAR ");
            stb.append(        "AND T1.SEMESTER = T2.SEMESTER ");
            stb.append(        "AND T1.GRADE = T2.GRADE ");
            stb.append(        "AND T1.GRADE||T1.HR_CLASS = '" + param._gradehrclass + "' ");
            stb.append(        "AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            //                      在籍チェック:転学(2)・退学(3)者は除外 但し異動日が学期終了日または異動基準日より小さい場合
            //                                   転入(4)・編入(5)者は除外 但し異動日が学期終了日または異動基準日より大きい場合 
            stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
            stb.append(                       "WHERE   S1.SCHREGNO = T1.SCHREGNO ");
            stb.append(                           "AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END) ");
            stb.append(                             "OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END)) ) ");
            //                      異動者チェック：留学(1)・休学(2)者は除外 但し学期終了日または基準日が異動開始日と終了日内にある場合
            stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
            stb.append(                       "WHERE   S1.SCHREGNO = T1.SCHREGNO ");
            stb.append(                           "AND S1.TRANSFERCD IN ('1','2') AND CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
            stb.append(    ") ");
            
            //メイン表
            stb.append("SELECT  T1.SCHREGNO, T1.ATTENDNO, T2.HR_NAME, ");
            stb.append(        "T5.NAME, T5.REAL_NAME, CASE WHEN T6.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS USE_REAL_NAME, ");
            stb.append(        "T3.COURSENAME, T4.MAJORNAME, ");
            stb.append(        "T1.GRADE, T1.COURSECD, T1.MAJORCD, T1.COURSECODE, ");
            stb.append(        "VALUE(T7.STAFFNAME, VALUE(T8.STAFFNAME, T9.STAFFNAME)) AS STAFFNAME ");
            stb.append("FROM    SCHNO_A T1 ");
            stb.append(        "INNER JOIN SCHREG_BASE_MST T5 ON T1.SCHREGNO = T5.SCHREGNO ");
            stb.append(        "INNER JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR = '" + param._year + "' AND ");
            stb.append(                                          "T2.SEMESTER = T1.SEMESTER AND ");
            stb.append(                                          "T2.GRADE || T2.HR_CLASS = '" + param._gradehrclass + "' ");
            stb.append(        "LEFT JOIN COURSE_MST T3 ON T3.COURSECD = T1.COURSECD ");
            stb.append(        "LEFT JOIN MAJOR_MST T4 ON T4.COURSECD = T1.COURSECD AND T4.MAJORCD = T1.MAJORCD ");
            stb.append(        "LEFT JOIN SCHREG_NAME_SETUP_DAT T6 ON T6.SCHREGNO = T1.SCHREGNO AND T6.DIV = '03' ");
            stb.append(        "LEFT JOIN STAFF_MST T7 ON T7.STAFFCD = T2.TR_CD1 ");
            stb.append(        "LEFT JOIN STAFF_MST T8 ON T8.STAFFCD = T2.TR_CD2 ");
            stb.append(        "LEFT JOIN STAFF_MST T9 ON T9.STAFFCD = T2.TR_CD3 ");
            stb.append("ORDER BY ATTENDNO");
            return stb.toString();
        }
    }

    /**
     * 出欠
     */
    private static class Attendance {
        final String _year;
        final String _semester;
        final String _month;
        int _lesson;
        int _kesseki;
        int _kekkaChohai;
        int _kekkaJisu;
        int _lateJisu;
        int _lateChohai;
        int _early;
        int _suspendMourning;
        public Attendance(
                final String year,
                final String semester,
                final String month
        ) {
            _year = year;
            _semester = semester;
            _month = month;
        }
        public void add(final Attendance att) {
            _lesson += att._lesson;
            _kesseki += att._kesseki;
            _kekkaChohai += att._kekkaChohai;
            _kekkaJisu += att._kekkaJisu;
            _lateChohai += att._lateChohai;
            _lateJisu += att._lateJisu;
            _early += att._early;
            _suspendMourning += att._suspendMourning;
        }
        public static Map load(final DB2UDB db2, final Param param, final String schregno) {
            Map attendances = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final KNJSchoolMst knjSchoolmst = param._knjSchoolMst;
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     YEAR, ");
                stb.append("     MONTH, ");
                stb.append("     SEMESTER, ");
                stb.append("     SCHREGNO, ");
                stb.append("     LESSON - ABROAD ");
                if (!"1".equals(knjSchoolmst._semOffDays)) {
                    stb.append("     - OFFDAYS ");
                }
                stb.append("     AS LESSON, ");
                stb.append("     ABSENT, ");
                stb.append("     SUSPEND, ");
                if ("true".equals(param._useVirus)) {
                    stb.append("     VIRUS AS VIRUS, ");
                } else {
                    stb.append("     0 AS VIRUS, ");
                }
                if ("true".equals(param._useKoudome)) {
                    stb.append("     KOUDOME AS KOUDOME, ");
                } else {
                    stb.append("     0 AS KOUDOME, ");
                }
                stb.append("     MOURNING, ");
                stb.append("     SICK + NOTICE + NONOTICE AS SICK, ");
                stb.append("     LATE, ");
                stb.append("     EARLY, ");
                stb.append("     VALUE(KEKKA_JISU, 0) AS KEKKA_JISU, ");
                stb.append("     VALUE(REIHAI_KEKKA, 0) AS REIHAI_KEKKA, ");
                stb.append("     VALUE(REIHAI_TIKOKU, 0) AS REIHAI_TIKOKU, ");
                stb.append("     VALUE(JYUGYOU_TIKOKU, 0) AS JYUGYOU_TIKOKU ");
                stb.append(" FROM ");
                stb.append("     V_ATTEND_SEMES_DAT T1 ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + param._year + "' ");
                stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
                final String emonth = param._date.substring(5, 7);
                if (Integer.parseInt(emonth) < 4) {
                    stb.append("     AND (T1.MONTH BETWEEN '04' AND '12' OR T1.MONTH BETWEEN '01' AND '" + emonth + "') ");
                } else {
                    stb.append("     AND (T1.MONTH BETWEEN '04' AND '" + emonth + "') ");
                }
                
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    
                    final String year = rs.getString("YEAR");
                    final String semester = rs.getString("SEMESTER");
                    final String month = rs.getString("MONTH");
                    
                    if (!attendances.containsKey(month)) {
                        attendances.put(month, new Attendance(year, semester, month));
                    }
                    final Attendance att = (Attendance) attendances.get(month);
                    att._lesson += rs.getInt("LESSON");
                    att._suspendMourning += rs.getInt("SUSPEND") + rs.getInt("MOURNING") + ("true".equals(param._useVirus) ? rs.getInt("VIRUS") : 0) + ("true".equals(param._useKoudome) ? rs.getInt("KOUDOME") : 0);
                    att._kesseki += rs.getInt("SICK");
                    att._early += rs.getInt("EARLY");
                    att._kekkaJisu += rs.getInt("KEKKA_JISU");
                    att._lateJisu += rs.getInt("JYUGYOU_TIKOKU");
                    att._kekkaChohai += rs.getInt("REIHAI_KEKKA");
                    att._lateChohai += rs.getInt("REIHAI_TIKOKU");
                }
                
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return attendances;
        }
    }
    
    /**
     * 通知票所見詳細(DIV='01')
     */
    private static class HreportremarkDetailDat {
        public static Map load(final DB2UDB db2, final Param param, final String schregno) {
            final Map map = new HashMap();
            final String sql = sql(param, schregno);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String code = rs.getString("CODE");
                    final String remark1 = rs.getString("REMARK1");
                    map.put(code, remark1);
                }
                
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return map;
        }
        private static String sql(final Param param, final String schregno) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.CODE, ");
            stb.append("     T1.REMARK1 ");
            stb.append(" FROM ");
            stb.append("     HREPORTREMARK_DETAIL_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + SEMES9 + "' ");
            stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
            stb.append("     AND T1.DIV = '01' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.CODE, ");
            stb.append("     T1.SEMESTER ");
            return stb.toString();
        }
    }
    
    /**
     * 通知票所見詳細(DIV='03')
     */
    private static class HreportremarkDetailDat3 {
        public static Map load(final DB2UDB db2, final Param param, final String schregno) {
            final Map map = new HashMap();
            final String sql = sql(param, schregno);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String code = rs.getString("CODE");
                    final String remark1 = rs.getString("REMARK1");
                    map.put(code, remark1);
                }
                
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return map;
        }
        private static String sql(final Param param, final String schregno) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.CODE, ");
            stb.append("     T1.REMARK1 ");
            stb.append(" FROM ");
            stb.append("     HREPORTREMARK_DETAIL_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + SEMES9 + "' ");
            stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
            stb.append("     AND T1.DIV = '03' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.CODE, ");
            stb.append("     T1.SEMESTER ");
            return stb.toString();
        }
    }
    
    /**
     * 通知票所見
     */
    private static class HreportremarkDat implements Comparable {
        final String _semester;
        final String _totalstudytime;
        final String _communication;
        final String _attendrecRemark;
        final String _remark1;
        final String _remark2;
        public HreportremarkDat(
                final String semester,
                final String totalstudytime,
                final String communication,
                final String attendrecRemark,
                final String remark1,
                final String remark2
        ) {
            _semester = semester;
            _totalstudytime = totalstudytime;
            _communication = communication;
            _attendrecRemark = attendrecRemark;
            _remark1 = remark1;
            _remark2 = remark2;
        }
        public int compareTo(final Object o) {
            if (o == null || !(o instanceof HreportremarkDat)) {
                return -1;
            }
            final HreportremarkDat other = (HreportremarkDat) o;
            return _semester.compareTo(other._semester);
        }
        public static List load(final DB2UDB db2, final Param param, final String schregno) {
            final List list = new ArrayList();
            final String sql = sql(param, schregno);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String totalstudytime = rs.getString("TOTALSTUDYTIME");
                    final String communication = rs.getString("COMMUNICATION");
                    final String attendrecRemark = rs.getString("ATTENDREC_REMARK");
                    final String remark1 = rs.getString("REMARK1");
                    final String remark2 = rs.getString("REMARK2");
                    final HreportremarkDat hd = new HreportremarkDat(semester, totalstudytime, communication, attendrecRemark, remark1, remark2);
                    list.add(hd);
                }
                
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            Collections.sort(list);
            return list;
        }
        private static String sql(final Param param, final String schregno) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.TOTALSTUDYTIME, ");
            stb.append("     T1.COMMUNICATION, ");
            stb.append("     T1.ATTENDREC_REMARK, ");
            stb.append("     T1.REMARK1, ");
            stb.append("     T1.REMARK2 ");
            stb.append(" FROM ");
            stb.append("     HREPORTREMARK_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND (T1.SEMESTER <= '" + param._semester + "' OR T1.SEMESTER = '" + SEMES9 + "') ");
            stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.SEMESTER ");
            return stb.toString();
        }
    }
    
    /**
     * 行動の状況
     */
    private static class BehaviorSemesDat {

        final String _semester;
        final String _code;
        final String _record;
        final String _mark;
        
        public BehaviorSemesDat(
                final String semester,
                final String code,
                final String record,
                final String mark) {
            _semester = semester;
            _code = code;
            _record = record;
            _mark = mark;
        }
        
        public static List load(final DB2UDB db2, final Param param, final String schregno) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getBehaviorSemesDatSql(param, schregno);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String code = rs.getString("CODE");
                    final String record = rs.getString("RECORD");
                    final String mark = rs.getString("MARK");
                    final BehaviorSemesDat behaviorSemesDat = new BehaviorSemesDat(semester, code, record, mark);
                    list.add(behaviorSemesDat);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }
        
        private static String getBehaviorSemesDatSql(final Param param, final String schregno) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.* ");
            stb.append("     , T2.NAMESPARE1 AS MARK ");
            stb.append(" FROM ");
            stb.append("     BEHAVIOR_SEMES_DAT T1 ");
            stb.append("     LEFT JOIN NAME_MST T2 ON T2.NAMECD1 = 'D036' ");
            stb.append("         AND T2.NAME1 = T1.RECORD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER <= '" + param._semester + "' ");
            stb.append("     AND T1.SEMESTER <> '" + SEMES9 + "' ");
            stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
            return stb.toString();
        }
    }
    
    /** 月 */
    private static class Month implements Comparable {
        final String _year;
        final String _monthString;
        final String _sdate;
        final String _edate;
        private Month(final String year, final String monthString, final String sdate, final String edate) {
            _year = year;
            _monthString = monthString;
            _sdate = sdate;
            _edate = edate;
        }
        public int compareTo(Object o) {
            if (o != null && !(o instanceof Month)) {
                return -1;
            }
            final Month another = (Month) o;
            int ret = _year.compareTo(another._year);
            if (0 == ret) {
                ret = _monthString.compareTo(another._monthString);
            }
            return ret;
        }
        public int hashCode() {
            return _year.hashCode() + _monthString.hashCode();
        }
        public String toString() {
            return "Month(" + _year + "-" + _monthString + "," + _sdate + ":" + _edate + ")"; 
        }
    }
    
    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _year;
        final String _semester;
        final String _ctrlSemester;
        final String _date;
        final String _ctrlDate;
        final String _documentroot;
        String _imagepath;
        String _extension;
        final String _gradehrclass;
        final String _grade;
        final String _gradeCd;
        final String[] _categorySelected;
        final String _hyosiHrname;
        
        final String _principalname;
        final String _schoolname;
        final String _jobname;
        
        final List _semesterList;
        
        final KNJSchoolMst _knjSchoolMst;
        
        final String _d016Namespare1;
        
        private final DecimalFormat df = new DecimalFormat("00");
        
        private final String _z012;
        
        private final String _useCurriculumcd;
        private final String _useVirus;
        private final String _useKoudome;

        private Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _date = request.getParameter("DATE").replace('/', '-');
            _ctrlDate = request.getParameter("CTRL_DATE").replace('/', '-');
            _documentroot = request.getParameter("DOCUMENTROOT");
            _gradehrclass = request.getParameter("GRADE_HR_CLASS");
            _grade = _gradehrclass.substring(0, 2);
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _gradeCd = getGradeCd(db2, _grade);
            final String hrname = getHrClassName1(db2, _year, SEMES9.equals(_semester) ? _ctrlSemester : _semester, _gradehrclass);
            _hyosiHrname = "第" + _gradeCd + "学年 " + hrname + "組 ";
            
            _semesterList = getSemesterList(db2);
            // log.debug(" semesterList = " + _semesterList);
            
            _d016Namespare1 = getNameMst(db2, _year, "D016", "01", "NAMESPARE1");

            KNJSchoolMst knjSchoolMst = null;
            try {
                knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }
            _knjSchoolMst = knjSchoolMst;
            
            _z012 = getZ012(db2);
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
            
            final Map certifSchoolDat = getCertifSchoolDat(db2, _year);
            _principalname = (String) certifSchoolDat.get("PRINCIPAL_NAME");
            _schoolname = (String) certifSchoolDat.get("SCHOOL_NAME");
            _jobname = (String) certifSchoolDat.get("JOB_NAME");
            
            setImagePath(db2);
        }
        
        private void setImagePath(DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT IMAGEPATH, EXTENSION FROM CONTROL_MST ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    _imagepath = rs.getString("IMAGEPATH");
                    _extension = rs.getString("EXTENSION");
                }
            } catch (SQLException ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        
        public String getFilePath() {
            final String path = _documentroot + "/" + (null == _imagepath ? "" : (_imagepath + "/")) + "SCHOOLLOGO." + _extension;
            if (!new java.io.File(path).exists()) {
                return null;
            }
            return path;
        }
        
        private List getMonthList(final String nendo, final String date) {
            
            final Calendar cal = Calendar.getInstance();
            final Date lastDate = java.sql.Date.valueOf(date);
            cal.setTime(java.sql.Date.valueOf(nendo + "-04-01"));
            final Map map = new HashMap();
            while (nendo.equals(KNJ_EditDate.b_year(toSqldate(cal))) && cal.getTime().compareTo(lastDate) <= 0) {
                final String monthStr = df.format(cal.get(Calendar.MONTH) + 1);
                if (!map.containsKey(monthStr)) {
                    map.put(monthStr, new TreeSet());
                }
                final Collection dates = (Collection) map.get(monthStr);
                dates.add(toSqldate(cal));
                cal.add(Calendar.DATE, 1);
            }
            
            final List monthList = new ArrayList();
            for (final Iterator it = map.keySet().iterator(); it.hasNext();) {
                final String monthStr = (String) it.next();
                final List dates = new ArrayList((Collection) map.get(monthStr));
                if (null != dates && 0 != dates.size()) {
                    final String sdate = (String) dates.get(0);
                    final String edate = 1 == dates.size() ? sdate : (String) dates.get(dates.size() - 1);
                    final String year = String.valueOf(Integer.parseInt(nendo) + (isNewYear(monthStr) ? 1 : 0)); 
                    final Month month = new Month(year, monthStr, sdate, edate);
                    monthList.add(month);
                }
            }
            
            Collections.sort(monthList);
            return monthList;
        }
        
        private boolean isNewYear(final String monthStr) {
            return Integer.parseInt(monthStr) < 4;
        }
        
        private String toSqldate(final Calendar cal) {
            return new StringBuffer().append(cal.get(Calendar.YEAR)).append("-").append(df.format(cal.get(Calendar.MONTH) + 1)).append("-").append(df.format(cal.get(Calendar.DATE))).toString();
        }
        
        private List getSemesterList(final DB2UDB db2) {
            final List semesters = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT SEMESTER, SDATE, EDATE FROM V_SEMESTER_GRADE_MST WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ORDER BY SEMESTER ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Map map = new HashMap();
                    map.put("SEMESTER", rs.getString("SEMESTER"));
                    map.put("SDATE", rs.getString("SDATE"));
                    map.put("EDATE", rs.getString("EDATE"));
                    semesters.add(map);
                }
                
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return semesters;
        }
        
        private String getNameMst(final DB2UDB db2, final String year, final String namecd1, final String namecd2, final String field) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT ");
                sql.append("    " + field + " ");
                sql.append(" FROM NAME_MST T1 ");
                sql.append("    INNER JOIN NAME_YDAT T2 ON T2.YEAR = '" + year + "' AND T2.NAMECD1 = T1.NAMECD1 AND T2.NAMECD2 = T1.NAMECD2 ");
                sql.append(" WHERE ");
                sql.append("    T1.NAMECD1 = '" + namecd1 + "' ");
                sql.append("    AND T1.NAMECD2 = '" + namecd2 + "' ");
                sql.append("   ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (null != rs.getString(field)) {
                        rtn = rs.getString(field);
                    }
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }
        
        private Map getCertifSchoolDat(final DB2UDB db2, final String year) {
            final Map certifSchoolDat = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + year + "' AND CERTIF_KINDCD = '103' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    final ResultSetMetaData meta = rs.getMetaData();
                    for (int i = 1; i <= meta.getColumnCount(); i++) {
                        final String column = meta.getColumnName(i);
                        final String data = rs.getString(column);
                        certifSchoolDat.put(column, data);
                    }
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return certifSchoolDat;
        }
        
        private String getHrClassName1(final DB2UDB db2, final String year, final String semester, final String gradeHrclass) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT HR_CLASS_NAME1 FROM SCHREG_REGD_HDAT WHERE YEAR = '" + year + "' AND SEMESTER = '" + semester + "' AND GRADE || HR_CLASS = '" + gradeHrclass + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next() && null != rs.getString("HR_CLASS_NAME1")) {
                    rtn = rs.getString("HR_CLASS_NAME1");
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            if (null == rtn) {
                try {
                    final String sql = " SELECT HR_CLASS FROM SCHREG_REGD_DAT WHERE YEAR = '" + year + "' AND SEMESTER = '" + semester + "' AND GRADE || HR_CLASS = '" + gradeHrclass + "' ";
                    ps = db2.prepareStatement(sql);
                    rs = ps.executeQuery();
                    if (rs.next()) {
                        if (null == rtn && null != rs.getString("HR_CLASS")) {
                            rtn = NumberUtils.isDigits(rs.getString("HR_CLASS")) ? String.valueOf(Integer.parseInt(rs.getString("HR_CLASS"))) : rs.getString("HR_CLASS");
                        }
                    }
                } catch (SQLException e) {
                    log.error("exception!", e);
                } finally {
                    DbUtils.closeQuietly(null, ps, rs);
                    db2.commit();
                }
            }
            if (null == rtn) {
                rtn = "";
            }
            return rtn;
        }
        
        private String getZ012(DB2UDB db2) throws SQLException {
            PreparedStatement ps = db2.prepareStatement("SELECT T1.NAME1 FROM NAME_MST T1 WHERE T1.NAMECD1 = 'Z012' ");
            ResultSet rs = ps.executeQuery();
            String rtn = null;
            while (rs.next()) {
                rtn = rs.getString("NAME1");
            }
            DbUtils.closeQuietly(null, ps, rs);
            return rtn;
        }
        
        private String getDateString(String date) {
            if ("2".equals(_z012)) {
                return date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date);
            } else {
                return KNJ_EditDate.h_format_JP(date); // デフォルトは和暦
            }
        }
        
        private String getGradeCd(final DB2UDB db2, final String grade) {
            String gradeCd = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT * FROM SCHREG_REGD_GDAT T1 WHERE T1.SCHOOL_KIND = 'J' AND T1.YEAR = '" + _year + "' AND T1.GRADE = '" + grade + "' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    final String tmp = rs.getString("GRADE_CD");
                    if (NumberUtils.isDigits(tmp)) {
                        gradeCd = String.valueOf(Integer.parseInt(tmp));
                    }
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return gradeCd;
        }
    }
}

// eof
