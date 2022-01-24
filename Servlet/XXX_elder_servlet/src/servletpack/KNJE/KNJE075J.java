/*
 * $Id: 4902b841dcd8318cc0d5185b54109350a89a9257 $
 *
 * 作成日: 2013/02/15
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KNJ_PersonalinfoSql;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * 学校教育システム 賢者 [進路情報管理] 熊本用中学調査書
 */
public class KNJE075J {

    private static final Log log = LogFactory.getLog(KNJE075J.class);

    private boolean _hasData;

    private static final KNJ_PersonalinfoSql personalinfoSql = new KNJ_PersonalinfoSql();

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

            for (int i = 0; i < _param._categorySelected.length; i++) {
                final String schregno = (String) _param._categorySelected[i];
                final Student student = new Student(schregno);
                log.debug(" schregno = " + student._schregno);
                student.load(db2, _param);
                
                if (_param._isSundaikoufu) {
                    printSundaiKoufu(svf, student);
                    
                } else {
                    printKumamotoPage1(svf, student);
                    
                    printKumamotoPage2(svf, student);
                }
            }
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
    
    /**
     *  日付の編集（ブランク挿入）
     *  ○引数について >> １番目は編集対象日付「平成18年1月1日」、２番目は元号取得用年度
     *  ○戻り値について >> 「平成3年1月1日」-> 「平成 3年 1月 1日」
     */
    private static String setDateFormat(
            final String hdate,
            final String nendo
            ) {
        StringBuffer stb = new StringBuffer();
        try {
            //日付が無い場合は「平成　年  月  日」の様式とする
            if (hdate == null) {
                stb.append(KenjaProperties.gengou(Integer.parseInt(nendo), 4, 1));
                if (2 < stb.length()) {
                    stb.delete(2, stb.length());
                }
                stb.append("    年    月    日");
                return stb.toString();
            } else {
                //「平成18年 1月 1日」の様式とする => 数値は２桁
                stb = setFormatInsertBlank("", stb.append( hdate ) );
            }
        } catch (NumberFormatException e) {
            log.error("NumberFormatException", e);
        }
        return stb.toString();
    }
    
    private static String setYearMonthFormat(
            final String hdate,
            final String nendo
            ) {
        final String head = "　　";
        final StringBuffer stb = new StringBuffer();
        try {
            //日付が無い場合は「平成　年  月  日」の様式とする
            if (hdate == null) {
                stb.append(KenjaProperties.gengou(Integer.parseInt(nendo), 4, 1));
                if (2 < stb.length()) {
                    stb.delete(2, stb.length());
                }
                stb.insert(0, head);
                stb.append("    年    月");
                return stb.toString();
            } else {
                //「平成18年 1月」の様式とする => 数値は２桁
                setFormatInsertBlank(head, stb.append(hdate));
            }
        } catch (NumberFormatException e) {
            log.error("NumberFormatException", e);
        }
        return stb.toString();
    }
    
    /**
     *  文字編集（ブランク挿入）
     */
    private static StringBuffer setFormatInsertBlank(final String head, final StringBuffer stb) {
        int n = 0;
        for (int i = 0; i < stb.length(); i++) {
            final char ch = (stb.toString()).charAt(i);
            if (Character.isDigit(ch)) {
                n++;
            } else {
                if (0 < n) {
                    if (1 == n) {
                        stb.insert(i - n, "　");
                        i++;
                    } else if (2 == n) {
                        stb.insert(i - n, " ");
                        i++;
                    }
                    stb.insert(i, " ");
                    i++;
                    n = 0;
                } else if (ch == '元') {
                    stb.insert(i, "　");
                    i++;
                }
            }
        }
        stb.insert(0, StringUtils.defaultString(head));
        return stb;
    }
    
    private void printSundaiKoufu(final Vrw32alp svf, final Student student) {
        final String form = "KNJE075J_SUNDAI.frm";
        svf.VrSetForm(form, 1);

        
        svf.VrsOut("ADMISSION_DATE", setYearMonthFormat(null, _param._year)); // 編入学
        svf.VrsOut("TRANSFER_DATE", setYearMonthFormat(null, _param._year)); // 転入学
        svf.VrsOut("TRANSFER_SCHOOL", append("中学校", "より") + "転入学"); // 転入学学校
        svf.VrsOut("GRAD_DATE", setYearMonthFormat(null, _param._year)); // 卒業

        if ("1".equals(student._entdiv) || "2".equals(student._entdiv) || "3".equals(student._entdiv)) {
            svf.VrsOut("ADMISSION_DATE", setYearMonthFormat(KNJ_EditDate.h_format_JP_M(student._entdate), _param._year)); // 編入学
            svf.VrsOut("ADMISSION_CIRCLE1", "〇"); // 入学〇
        } else if ("4".equals(student._entdiv)) {
            svf.VrsOut("TRANSFER_DATE", setYearMonthFormat(KNJ_EditDate.h_format_JP_M(student._entdate), _param._year)); // 転入学
            svf.VrsOut("TRANSFER_SCHOOL", append(student._entSchool, "より") + "転入学"); // 転入学学校
        } else if ("5".equals(student._entdiv)) {
            svf.VrsOut("ADMISSION_DATE", setYearMonthFormat(KNJ_EditDate.h_format_JP_M(student._entdate), _param._year)); // 編入学
            svf.VrsOut("ADMISSION_CIRCLE2", "〇"); // 編入学〇
        }
        if (null == student._grddiv || "4".equals(student._grddiv)) {
            svf.VrsOut("GRAD_CIRCLE1", "〇"); // 卒業見込
            svf.VrsOut("GRAD_DATE", setYearMonthFormat(KNJ_EditDate.h_format_JP_M(student._gradudate), _param._year)); // 卒業
        } else if ("1".equals(student._grddiv)) {
            svf.VrsOut("GRAD_CIRCLE2", "〇"); // 卒業
            svf.VrsOut("GRAD_DATE", setYearMonthFormat(KNJ_EditDate.h_format_JP_M(student._grddate), _param._year)); // 卒業
        }
        if (null != student._sexName) {
            if (-1 != student._sexName.indexOf("男")) {
                svf.VrsOut("SEX_CIRCLE1", "〇"); // 性別〇
            } else if (-1 != student._sexName.indexOf("女")) {
                svf.VrsOut("SEX_CIRCLE2", "〇"); // 性別〇
            }
        }

        final int ketaKana = KNJ_EditKinsoku.getMS932ByteCount(student._kana);
        svf.VrsOut("KANA" + (ketaKana <= 40 ? "1" : "2"), student._kana); // ふりがな

        final int ketaName = KNJ_EditKinsoku.getMS932ByteCount(student._name);
        svf.VrsOut("NAME" + (ketaName <= 24 ? "1" : ketaName <= 30 ? "2" : "3"), student._name); // 氏名

        if (null == student._birthday) {
            svf.VrsOut("BIRTHDAY", "　　" + append("　　 年    月    日", "生")); // 生年月日
        } else {
            svf.VrsOut("BIRTHDAY", "　　" + append(setDateFormat(KNJ_EditDate.h_format_SeirekiJP(student._birthday), _param._year), "生")); // 生年月日
        }

        svf.VrsOut("DATE", setDateFormat(KNJ_EditDate.h_format_JP(_param._date), _param._year)); // 日付
        svf.VrsOut("SCHOOL_NAME", _param._schoolName);
        if (!"2".equals(_param._kotyo)) {
            svf.VrsOut("PRINCIPAL_NAME", _param._principalName);
        }
        svf.VrsOut("CHARGE_NAME", _param._teacherName);

        final int maxGrade = 3;
        for (int j = 0; j < maxGrade; j++) {
            final int line = j + 1;
            final String gradename = new String[] {"１年", "２年", "３年"}[j];
            svf.VrsOutn("GRADE1", line, gradename); // 学年
            svf.VrsOutn("GRADE2", line, gradename); // 学年
        }
        
        for (int vi = 0; vi < student._valueRecordList.size(); vi++) {
            final String svi = String.valueOf(vi + 1);
            final ValueRecord valueRecord = (ValueRecord) student._valueRecordList.get(vi);
            final String classname;
            if ("1".equals(valueRecord._electDiv)) {
                classname = valueRecord._subClassName;
            } else {
                classname = valueRecord._className;
            }
            if (null != classname) {
                if (KNJ_EditEdit.getMS932ByteLength(classname) <= 6) {
                    svf.VrsOut("CLASS" + svi + "_2", classname);
                    
                } else if (classname.length() == 4) {
                    svf.VrsOut("CLASS" + svi + "_1", classname.substring(0, 2));
                    svf.VrsOut("CLASS" + svi + "_2", classname.substring(2));
                } else {
                    final String[] token = KNJ_EditEdit.get_token(classname, 6, 2);
                    if (null != token) {
                        for (int i = 0; i < token.length; i++) {
                            svf.VrsOut("CLASS" + svi + "_" + String.valueOf(i + 1), token[i]); // 教科名出力
                        }
                    }
                }
            }
            
            for (final Iterator itv = valueRecord._values.iterator(); itv.hasNext();) {
                final Value v = (Value) itv.next();
                if (v._value != null) {
                    if ("1".equals(valueRecord._electDiv)) { // 選択科目は固定で読み替え 11 -> A, 22 -> B, 33 -> C
                        final String value;
                        if ("11".equals(v._value)) {
                            value = "A";
                        } else if ("22".equals(v._value)) {
                            value = "B";
                        } else if ("33".equals(v._value)) {
                            value = "C";
                        } else {
                            value = v._value;
                        }
                        svf.VrsOutn("VALUE" + svi, v._grade, value); // 評定
                    } else {
                        svf.VrsOutn("VALUE" + svi, v._grade, v._value); // 評定
                    }
                }
            }
        }
        
        for (final Iterator it = student._attendanceMap.keySet().iterator(); it.hasNext();) {
            final Integer grade = (Integer) it.next();
            final Attendance attendance = (Attendance) student._attendanceMap.get(grade);
            svf.VrsOutn("ATTEND", grade.intValue() , attendance._absent); // 欠席
            final HexamentremarkDat d = (HexamentremarkDat) student._hexamentremarkDatMap.get(grade);
            if (null != d) {
                final int lines = 5;
                final List tokenList = KNJ_EditKinsoku.getTokenList(d._attendrecRemark, 8 * 2, lines);
                for (int i = 0; i < Math.min(tokenList.size(), lines); i++) {
                    svf.VrsOutn("ATTEND_REMARK" + String.valueOf(i + 1), grade.intValue(), (String) tokenList.get(i)); 
                }
            }
        }

        VrsOut(svf, "NOTE", "", student._hexamentremarkHDat._remark, 45, 5); // 特記事項
        
        svf.VrEndPage();
        _hasData = true;
    }
    
    private static String append(final String a, final String b) {
        if (StringUtils.isBlank(a)) {
            return "";
        }
        return a + b;
    }

    private static String prepend(final String a, final String b) {
        if (StringUtils.isBlank(b)) {
            return "";
        }
        return a + b;
    }

    private void printKumamotoPage1(final Vrw32alp svf, final Student student) {
        
        final String form = student.getTotalViewLines() > 37 ? "KNJE075KUMA_1_2.frm" : "KNJE075KUMA_1_1.frm";
        svf.VrSetForm(form, 4);

        if (30 < KNJ_EditEdit.getMS932ByteLength(student._kana)) {
            svf.VrsOut("KANA2", student._kana); 
        } else {
            svf.VrsOut("KANA1", student._kana); 
        }

        if (24 < KNJ_EditEdit.getMS932ByteLength(student._name)) {
            final String[] data = KNJ_EditEdit.get_token(student._name, 24, 2);
            if (null != data) {
                for (int i = 0; i < Math.min(data.length, 2); i++) {
                   svf.VrsOut("NAME2_" + (i + 1), data[i]); 
                }
            }
        } else {
            svf.VrsOut("NAME1", student._name); 
        }
        svf.VrsOut("SEX", student._sexName);
        
        int idouline = 0;
        if (("4".equals(student._entdiv) || "5".equals(student._entdiv)) && null != student._entdate) { // 転入学 or 編入学
            idouline += 1;
            svf.VrsOutn("GRD_REASON", idouline, StringUtils.defaultString(KNJ_EditDate.h_format_JP(student._entdate), "") + " " + StringUtils.defaultString(student._entname, ""));
        }
        if (("2".equals(student._grddiv) || "3".equals(student._grddiv)) && null != student._grddate) { // 退学 or 転学
            idouline += 1;
            svf.VrsOutn("GRD_REASON", idouline, StringUtils.defaultString(KNJ_EditDate.h_format_JP(student._grddate), "") + " " + StringUtils.defaultString(student._grdname, ""));
        }
        
        if (null != student._birthday) {
            final String[] nengetuhi = getGengouNenGetsuHi(student._birthday);
            svf.VrsOut("ERA_NAME", nengetuhi[0]);
            svf.VrsOut("BIRTH_Y", nengetuhi[1]);
            svf.VrsOut("BIRTH_M", nengetuhi[2]);
            svf.VrsOut("BIRTH_D", nengetuhi[3]);
        }
        if (null != student._grddate && ("1".equals(student._grddiv) || "4".equals(student._grddiv))) {
            final String[] nengetuhi = getGengouNenGetsuHi(student._grddate);
            svf.VrsOut("ERA_NAME2", nengetuhi[0]);
            svf.VrsOut("GRAD_Y", nengetuhi[1]);
            svf.VrsOut("GRAD_M", nengetuhi[2]);
            svf.VrsOut("GRAD_D", nengetuhi[3]);
        }
        
        // 評定出力処理
        int line = 0;  //欄の出力行数
        int lineSel = 0;  //欄の出力行数
        for (final Iterator it = student._valueRecordList.iterator(); it.hasNext();) {
            final ValueRecord valueRecord = (ValueRecord) it.next();
            if ("1".equals(valueRecord._electDiv)) {
                lineSel += 1;
                svf.VrsOutn("CLASS3", lineSel, valueRecord._subClassName); // 科目名出力
            } else {
                line += 1;
                svf.VrsOutn("CLASS2", line, valueRecord._className); // 教科名出力
            }
            
            for (final Iterator itv = valueRecord._values.iterator(); itv.hasNext();) {
                final Value v = (Value) itv.next();
                if (v._value != null) {
                    if ("1".equals(valueRecord._electDiv)) { // 選択科目は固定で読み替え 11 -> A, 22 -> B, 33 -> C
                        final String value;
                        if ("11".equals(v._value)) {
                            value = "A";
                        } else if ("22".equals(v._value)) {
                            value = "B";
                        } else if ("33".equals(v._value)) {
                            value = "C";
                        } else {
                            value = v._value;
                        }
                        svf.VrsOutn("SEL_VAL" + v._grade, lineSel, value);  //評定
                    } else {
                        svf.VrsOutn("VAL" + v._grade, line, v._value);  //評定
                    }
                }
            }
        }
        boolean nonedata = false;
        
        // 観点出力処理
        int line1 = 0;
        String oldclasscd = "";
        for (final Iterator it = student._classViewList.iterator(); it.hasNext();) {
            final ClassViewSub classview = (ClassViewSub) it.next();
            if ("1".equals(classview._electdiv)) {
                continue;
            }
            final String[] classname = classview.getClassnameArray(classview._classname);  // 教科名のセット
            
            String classcd = classview._classcd;
            if (oldclasscd.equals(classview._classcd)) {
                if (StringUtils.isNumeric(classview._classcd)) {
                    classcd = new DecimalFormat("00").format(Integer.parseInt(classview._classcd) + 1);
                } else {
                    classcd = "00";
                }
            }
            
            int i = 0;  //教科名称カウント用変数を初期化
            for (final Iterator it2 = classview._views.iterator(); it2.hasNext();) {
                final ViewSub view = (ViewSub) it2.next();
                
                if (i < classname.length) {
                    svf.VrsOut("CLASS1", String.valueOf(classname[i]));  //教科名称
                }
                i++;
                for (final Iterator itv = view._views.iterator(); itv.hasNext();) {
                    final View v = (View) itv.next();
                    svf.VrsOut("VIEW" + v._g, v._view);  //観点
                }
                svf.VrsOut("CLASS_GRP", classcd);  //教科コード
                svf.VrsOut("VIEWNAME" + (KNJ_EditEdit.getMS932ByteLength(view._viewname) > 36 ? "1" : "2"), view._viewname);  //観点名称
                if (view.year1IsSlash()) svf.VrsOut("VIEWSLASH1", "／");
                if (view.year2IsSlash()) svf.VrsOut("VIEWSLASH2", "／");
                if (view.year3IsSlash()) svf.VrsOut("VIEWSLASH3", "／");
                svf.VrEndRecord();
                line1++;
            }
            //観点別学習状況の教科名出力処理（教科の変わり目）
            if (i < classname.length) {
                for (int j = i; j < classname.length; j += 1) {
                    line1++;
                    svf.VrsOut("CLASS1", String.valueOf(classname[j]));  //教科名称
                    svf.VrsOut("CLASS_GRP", classcd);  //教科コード
                    svf.VrEndRecord();
                }
            }
            nonedata = true;
            oldclasscd = classcd;
        }
        if (!nonedata) {  // --> データが１件も無い場合の処理
            svf.VrsOut("CLASS_GRP", oldclasscd);  //教科コード
            svf.VrEndRecord();
            nonedata = true;
        }
        _hasData = true;
    }
    
    private static String[] getGengouNenGetsuHi(final String date) {
        return KNJ_EditDate.tate_format(KNJ_EditDate.h_format_JP(date));
    }
    
    private static void VrsOut(final Vrw32alp svf, final String fieldHead, final String fieldEnd, final String s, final int keta, final int lines) {
        final String[] data = KNJ_EditEdit.get_token(s, keta * 2, lines);
        if (null != data) {
            for (int i = 0; i < Math.min(data.length, lines); i++) {
               svf.VrsOut(fieldHead + String.valueOf(i + 1) + fieldEnd, data[i]); 
            }
        }
    }
    
    private static void VrsOutn(final Vrw32alp svf, final String field, final String s, final int keta, final int lines) {
        final String[] data = KNJ_EditEdit.get_token(s, keta * 2, lines);
        if (null != data) {
            for (int i = 0; i < Math.min(data.length, lines); i++) {
               svf.VrsOutn(field, (i + 1), data[i]); 
            }
        }
    }
    
    private void printKumamotoPage2(final Vrw32alp svf, final Student student) {
        
        svf.VrSetForm("KNJE075KUMA_2.frm", 1);

        svf.VrsOut("KANA", student._kana); 
        if (24 < KNJ_EditEdit.getMS932ByteLength(student._name)) {
            final String[] data = KNJ_EditEdit.get_token(student._name, 24, 2);
            if (null != data) {
                for (int i = 0; i < Math.min(data.length, 2); i++) {
                   svf.VrsOut("NAME2_" + (i + 1), data[i]); 
                }
            }
        } else {
            svf.VrsOut("NAME1", student._name); 
        }
        
        if (null != student._hexamentremarkJHDat) {
            // 総合的な学習の時間の記録
            VrsOutn(svf, "TOTAL_REC", student._hexamentremarkJHDat._totalstudyval, 25, 11);
            
            // 特別活動の記録
            VrsOutn(svf, "SPECIAL_REC", student._hexamentremarkJHDat._specialactrec, 25, 11);

            // 行動の記録
            VrsOutn(svf, "ACTION_REC", student._hexamentremarkJHDat._behaverecRemark, 25, 8);

            // 総合所見及び指導上参考となる諸事項
            VrsOutn(svf, "TOTAL_VIEW", student._hexamentremarkJHDat._trinRef, 25, 8);

            // 健康の記録
            VrsOutn(svf, "HEALTH_REC", student._hexamentremarkJHDat._healthrec, 25, 8);
        }

        // 出欠の記録
        for (final Iterator it = student._attendanceMap.keySet().iterator(); it.hasNext();) {
            final Integer grade = (Integer) it.next();
            final Attendance attendance = (Attendance) student._attendanceMap.get(grade);
            svf.VrsOut("MUSTDAY" + grade, attendance._requirePresent);
            svf.VrsOut("ABSENTDAY" + grade, attendance._absent);
            if (NumberUtils.isDigits(attendance._absent) && 0 == Integer.parseInt(attendance._absent)) {
            } else {
                final HexamentremarkDat d = (HexamentremarkDat) student._hexamentremarkDatMap.get(grade);
                if (null != d) {
                    VrsOutn(svf, "REMARK" + grade, d._attendrecRemark, 6, 6);
                }
            }
        }
        if (null != _param._date) {
            final String[] nengetuhi = getGengouNenGetsuHi(_param._date);
            svf.VrsOut("ERA_NAME", nengetuhi[0]);
            svf.VrsOut("YEAR", nengetuhi[1]);
            svf.VrsOut("MONTH", nengetuhi[2]);
            svf.VrsOut("DAY", nengetuhi[3]);
        }
        svf.VrsOut("ADDR1", _param._schoolLoc);
        svf.VrsOut("SCHOOL_NAME1", _param._schoolName);
        if (!"2".equals(_param._kotyo)) {
            svf.VrsOut("PRINCIPAL_NAME", _param._principalName);
        }
        svf.VrsOut("TEACHER_NAME", _param._teacherName);
        _hasData = true;
        
        svf.VrEndPage();
    }
    
    private static class Student {
        final String _schregno;
        private String _name;
        private String _kana;
        private String _sexName;
        private String _birthday;
        private String _entdate;
        private String _entdiv;
        private String _entname;
        private String _entSchool;
        private String _grddate;
        private String _gradudate;
        private String _grddiv;
        private String _grdname;
        private String _birthdayFlg;
        private String _finschoolname;
        private String _schoolKind;
        private Map _idouRirekiMap = Collections.EMPTY_MAP;
        private Map _attendanceMap = Collections.EMPTY_MAP;
        private Map _hexamentremarkDatMap = Collections.EMPTY_MAP;
        private List _classViewList = Collections.EMPTY_LIST;
        private List _valueRecordList = Collections.EMPTY_LIST;
        private HexamentremarkJHDat _hexamentremarkJHDat;
        private HexamentremarkHDat _hexamentremarkHDat;

        public Student(final String schregno) {
            _schregno = schregno;
        }
        
        public void load(final DB2UDB db2, final Param param) {
            loadName(db2, param);
            _entSchool = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT ENT_SCHOOL FROM SCHREG_ENT_GRD_HIST_DAT WHERE SCHREGNO = '" + _schregno + "' AND SCHOOL_KIND = '" + _schoolKind + "' "));
            _attendanceMap = Attendance.load(db2, param, _schregno);
            _classViewList = ClassViewSub.load(db2, param, _schregno);
            _valueRecordList = ValueRecord.load(db2, param, _schregno);
            _hexamentremarkDatMap = HexamentremarkDat.load(db2, param, _schregno);
            _hexamentremarkJHDat = HexamentremarkJHDat.load(db2, param, _schregno);
            _hexamentremarkHDat = HexamentremarkHDat.load(db2, param, _schregno);
        }
        
        private void loadName(final DB2UDB db2, final Param param) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                // 生徒名取得および印刷
                int p = 0;
                final String sql_info_reg = personalinfoSql.sql_info_reg("1111111000");
                ps = db2.prepareStatement(sql_info_reg);
                ps.setString(++p, _schregno); // 学籍番号
                ps.setString(++p, param._year); // 年度
                ps.setString(++p, param._gakki); // 学期
                ps.setString(++p, _schregno); // 学籍番号
                ps.setString(++p, param._year); // 年度
                rs = ps.executeQuery();
                if (rs.next()) {
                    _name = rs.getString("NAME");
                    _kana = rs.getString("NAME_KANA");
                    _sexName = rs.getString("SEX");
                    _birthday = rs.getString("BIRTHDAY");
                    _entdate = rs.getString("ENT_DATE");
                    _entdiv = rs.getString("ENT_DIV");
                    _entname = rs.getString("ENTER_NAME");
                    _grddate = rs.getString("GRD_DATE");
                    _gradudate = rs.getString("GRADU_DATE");
                    _grddiv = rs.getString("GRD_DIV");
                    _grdname = rs.getString("GRADU_NAME");
                    _birthdayFlg = rs.getString("BIRTHDAY_FLG");
                    _finschoolname = rs.getString("J_NAME");
                    _schoolKind = rs.getString("SCHOOL_KIND");
                }
            } catch (Exception ex) {
                log.error("printSvfDetail_5 SCHREG_INFO error!", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }
        
        public int getTotalViewLines() {
            int line = 0;
            for (final Iterator it = _classViewList.iterator(); it.hasNext();) {
                final ClassViewSub classview = (ClassViewSub) it.next();
                line += classview._views.size();
            }
            return line;
        }
    }

    private static class Attendance {
        
        final String _lesson;
        final String _suspendMourning;
        final String _abroad;
        final String _requirePresent;
        final String _present;
        final String _absent;

        public Attendance(final String lesson, final String suspendMourning, final String abroad, final String requirePresent, final String present, final String absent) {
            _lesson = lesson;
            _suspendMourning = suspendMourning;
            _abroad = abroad;
            _requirePresent = requirePresent;
            _present = present;
            _absent = absent;
        }

        public static Map load(final DB2UDB db2, Param param, final String schregno) {
            final Map dataMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                int p = 0;
                final String sql = Attendance.sql(param);
                ps = db2.prepareStatement(sql);
                ps.setString(++p, param._year); // 年度
                ps.setString(++p, schregno); // 学籍番号
                rs = ps.executeQuery();

                while (rs.next()) {
                    final int gint = param.getGradeCd(rs.getString("YEAR"), rs.getString("ANNUAL")); // 学年
                    if (-1 == gint) {
                        continue;
                    }
                    final Integer g = new Integer(gint);
                    final String lesson = rs.getString("LESSON"); // 授業日数
                    final String suspendMourning = rs.getString("SUSPEND_MOURNING"); // 出停・忌引
                    final String abroad = rs.getString("ABROAD"); // 留学
                    final String requirePresent = rs.getString("REQUIREPRESENT"); // 要出席
                    final String present = rs.getString("PRESENT"); // 出席
                    final String absent = rs.getString("ABSENT"); // 欠席
                    final Attendance a = new Attendance(lesson, suspendMourning, abroad, requirePresent, present, absent);
                    //log.info(" attend " + schregno + " " + g + " = " + absent);
                    dataMap.put(g, a);
                }
            } catch (Exception ex) {
                log.error("printSvfDetail_4 error!", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return dataMap;
        }

        private static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append("SELECT  T1.YEAR, ANNUAL, ");
            stb.append("  VALUE(CLASSDAYS,0) AS CLASSDAYS, ");
            stb.append("  CASE WHEN S1.SEM_OFFDAYS = '1'THEN VALUE(CLASSDAYS,0) - VALUE(ABROAD,0) ");
            stb.append("    ELSE VALUE(CLASSDAYS,0) - VALUE(OFFDAYS,0) - VALUE(ABROAD,0) ");
            stb.append("  END AS LESSON, ");
            stb.append("  VALUE(SUSPEND,0) + VALUE(MOURNING,0) AS SUSPEND_MOURNING, ");
            stb.append("  VALUE(SUSPEND,0) AS SUSPEND, ");
            stb.append("  VALUE(MOURNING,0) AS MOURNING, ");
            stb.append("  VALUE(ABROAD,0) AS ABROAD, ");
            stb.append("  CASE WHEN S1.SEM_OFFDAYS = '1' ");
            stb.append("    THEN VALUE(REQUIREPRESENT,0) + VALUE(OFFDAYS,0) ");
            stb.append("    ELSE VALUE(REQUIREPRESENT,0) ");
            stb.append("  END AS REQUIREPRESENT, ");
            stb.append("  VALUE(PRESENT,0) AS PRESENT, ");
            stb.append("  CASE WHEN S1.SEM_OFFDAYS = '1' ");
            stb.append("    THEN VALUE(SICK,0) + VALUE(ACCIDENTNOTICE,0) + VALUE(NOACCIDENTNOTICE,0) + VALUE(OFFDAYS,0) ");
            stb.append("    ELSE VALUE(SICK,0) + VALUE(ACCIDENTNOTICE,0) + VALUE(NOACCIDENTNOTICE,0) ");
            stb.append("  END AS ABSENT ");
            stb.append("FROM    SCHREG_ATTENDREC_DAT T1 ");
            stb.append("LEFT JOIN SCHOOL_MST S1 ON S1.YEAR = T1.YEAR ");
            if (param._isSundaikoufu) {
                stb.append(" AND S1.SCHOOL_KIND = 'J' ");
            }
            stb.append("WHERE   T1.YEAR <= ? ");
            stb.append("  AND SCHREGNO = ? ");
            // NO001 stb.append("ORDER BY ANNUAL ");
            return stb.toString();
        }
    }
    
    private static class HexamentremarkDat {

        final Integer _annual;
        final String _attendrecRemark;
//        final String _totalstudyval;
//        final String _calssact;
//        final String _studentact;
//        final String _clubact;
//        final String _schoolevent;

        public HexamentremarkDat(final Integer annual, final String attendrecRemark, final String totalstudyval, final String calssact, final String studentact, final String clubact, final String schoolevent) {
            _annual = annual;
            _attendrecRemark = attendrecRemark;
//            _totalstudyval = totalstudyval;
//            _calssact = calssact;
//            _studentact = studentact;
//            _clubact = clubact;
//            _schoolevent = schoolevent;
        }
        
        public static Map load(final DB2UDB db2, final Param param, final String schregno) {
            final Map dataMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                int p = 0;
                ps = db2.prepareStatement(sql());
                ps.setString(++p, param._year); // 年度
                ps.setString(++p, schregno); // 学籍番号
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Integer annual = new Integer(param.getGradeCd(rs.getString("YEAR"), rs.getString("ANNUAL"))); // 学年
                    final String attendrecRemark = rs.getString("ATTENDREC_REMARK");
                    final String totalstudyval = rs.getString("TOTALSTUDYVAL");
                    final String calssact = rs.getString("CALSSACT");
                    final String studentact = rs.getString("STUDENTACT");
                    final String clubact = rs.getString("CLUBACT");
                    final String schoolevent = rs.getString("SCHOOLEVENT");
                    
                    final HexamentremarkDat hd = new HexamentremarkDat(annual, attendrecRemark, totalstudyval, calssact, studentact, clubact, schoolevent);
                    dataMap.put(annual, hd);
                }
            } catch (Exception ex) {
                log.error("printSvfDetail_1 error!", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return dataMap;
        }

        private static String sql() {
            final StringBuffer stb = new StringBuffer();
            stb.append("SELECT ");
            stb.append("  YEAR, ANNUAL, ATTENDREC_REMARK, TOTALSTUDYVAL, CALSSACT, STUDENTACT, CLUBACT, SCHOOLEVENT ");
            stb.append("FROM    HEXAM_ENTREMARK_DAT T1 ");
            stb.append("WHERE   YEAR <= ? ");
            stb.append("  AND SCHREGNO = ? ");
            stb.append("ORDER BY ANNUAL, YEAR ");
            return stb.toString();
        }
    }
    
    private static class HexamentremarkHDat {

        String _remark;

        public static HexamentremarkHDat load(final DB2UDB db2, final Param param, final String schregno) {
            HexamentremarkHDat hexamjhdat = new HexamentremarkHDat();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                int p = 0;
                ps = db2.prepareStatement(sql());
                ps.setString(++p, schregno); // 学籍番号
                rs = ps.executeQuery();
                while (rs.next()) {
                    
                    hexamjhdat._remark = rs.getString("REMARK");
                }
            } catch (Exception ex) {
                log.error("printSvfDetail_1 error!", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(rs);
            }
            return hexamjhdat;
        }

        private static String sql() {
            StringBuffer stb = new StringBuffer();
            stb.append("SELECT ");
            stb.append("  * ");
            stb.append("FROM ");
            stb.append("  HEXAM_ENTREMARK_HDAT ");
            stb.append("WHERE ");
            stb.append("  SCHREGNO = ? ");
            return stb.toString();
        }
    }
    
    private static class HexamentremarkJHDat {

        final String _totalstudyval;
        final String _behaverecRemark;
        final String _healthrec;
        final String _specialactrec;
        final String _trinRef;

        public HexamentremarkJHDat(final String totalstudyval, final String behaverecRemark, final String healthrec, final String specialactrec, final String trinRef) {
            _totalstudyval = totalstudyval;
            _behaverecRemark = behaverecRemark;
            _healthrec = healthrec;
            _specialactrec = specialactrec;
            _trinRef = trinRef;
        }
        
        public static HexamentremarkJHDat load(final DB2UDB db2, final Param param, final String schregno) {
            HexamentremarkJHDat hexamjhdat = new HexamentremarkJHDat("", "", "", "", "");
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                int p = 0;
                ps = db2.prepareStatement(sql());
                ps.setString(++p, schregno); // 学籍番号
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String totalstudyval = rs.getString("TOTALSTUDYVAL");
                    final String behaverecRemark = rs.getString("BEHAVEREC_REMARK");
                    final String healthrec = rs.getString("HEALTHREC");
                    final String specialactrec = rs.getString("SPECIALACTREC");
                    final String trinRef = rs.getString("TRIN_REF");
                    
                    hexamjhdat = new HexamentremarkJHDat(totalstudyval, behaverecRemark, healthrec, specialactrec, trinRef);
                }
            } catch (Exception ex) {
                log.error("printSvfDetail_1 error!", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(rs);
            }
            return hexamjhdat;
        }

        private static String sql() {
            StringBuffer stb = new StringBuffer();
            stb.append("SELECT ");
            stb.append("  * ");
            stb.append("FROM ");
            stb.append("  HEXAM_ENTREMARK_J_HDAT ");
            stb.append("WHERE ");
            stb.append("  SCHREGNO = ? ");
            return stb.toString();
        }
    }

    /**
     * 評定データ
     */
    private static class ValueRecord {
        final String _classCd;
        final String _subClassCd;
        final String _electDiv;
        final String _className;
        final String _subClassName;
        final List _values;
        public ValueRecord(
                final String classCd, 
                final String subClassCd, 
                final String electDiv, 
                final String className, 
                final String subClassName) {
            _classCd = classCd;
            _subClassCd = subClassCd;
            _electDiv = electDiv;
            _className = className;
            _subClassName = subClassName;
            _values = new ArrayList();
        }
        
        public static List load(final DB2UDB db2, final Param param, final String schregno) {
            final List valueRecordList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getValueRecordSql(param);
                ps = db2.prepareStatement(sql);
                int p = 0;
                ps.setString(++p, schregno);
                ps.setString(++p, schregno);
                ps.setString(++p, schregno);
                rs = ps.executeQuery();
                
                while (rs.next()) {
                    //教科コードの変わり目
                    final String year = rs.getString("YEAR");
                    if (null == year) {
                        continue;
                    }
                    final int grade = param.getGradeCd(year, rs.getString("GRADE")); // 学年
                    final String electDiv = rs.getString("ELECTDIV");
                    final String classCd;
                    final String subClassCd;
                    if ("1".equals(param._useCurriculumcd)) {
                        classCd = rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND");
                    } else {
                        classCd = rs.getString("CLASSCD");
                    }
                    if ("1".equals(param._useCurriculumcd)) {
                        subClassCd = rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-" + rs.getString("CURRICULUM_CD") + "-" + rs.getString("SUBCLASSCD");
                    } else {
                        subClassCd = rs.getString("SUBCLASSCD");
                    }
                    final String className = rs.getString("CLASSNAME");
                    final String subClassName = rs.getString("SUBCLASSNAME");
                    //評定出力
                    final String value = rs.getString("VALUE");
                    
                    ValueRecord valueRecord = null;
                    
                    for (final Iterator it = valueRecordList.iterator(); it.hasNext();) {
                        ValueRecord vr = (ValueRecord) it.next();
                        if (vr._classCd != null && vr._classCd.equals(classCd)) {
                            valueRecord = vr;
                            break;
                        }
                    }
                    
                    if (null == valueRecord) {
                        valueRecord = new ValueRecord(classCd, subClassCd, electDiv, className, subClassName);
                        valueRecordList.add(valueRecord);
                    }
                    valueRecord._values.add(new Value(year, grade, value));
                }
            } catch (Exception ex) {
                log.error("printSvfDetail_1 error!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return valueRecordList;
        }
        
        /**
         *  priparedstatement作成  成績データ（評定）
         */
        private static String getValueRecordSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append("WITH ");
            //評定の表
            stb.append(" VALUE_DATA AS( ");
            stb.append("   SELECT ");
            stb.append("        ANNUAL ");
            stb.append("       ,CLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("       ,SCHOOL_KIND ");
                stb.append("       ,CURRICULUM_CD ");
            }
            stb.append("       ,SUBCLASSCD ");
            stb.append("       ,YEAR ");
            stb.append("       ,MAX(VALUATION) AS VALUE ");
            stb.append("   FROM ");
            stb.append("       SCHREG_STUDYREC_DAT T1 ");
            stb.append("   WHERE ");
            stb.append("       T1.SCHREGNO = ? ");
            stb.append("       AND T1.YEAR <= '" + param._year + "' ");
            stb.append("   GROUP BY ");
            stb.append("        ANNUAL ");
            stb.append("       ,CLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("       ,SCHOOL_KIND ");
                stb.append("       ,CURRICULUM_CD ");
            }
            stb.append("       ,SUBCLASSCD ");
            stb.append("       ,YEAR ");
            stb.append(" ) ");
            //学籍の表
            stb.append(",SCHREG_DATA AS( ");
            stb.append("  SELECT ");
            stb.append("      YEAR ");
            stb.append("     ,ANNUAL  ");
            stb.append("     ,GRADE  ");
            stb.append("  FROM ");
            stb.append("     SCHREG_REGD_DAT ");
            stb.append("  WHERE ");
            stb.append("      SCHREGNO = ? ");
            stb.append("      AND YEAR IN (SELECT  MAX(YEAR)  ");
            stb.append("                 FROM    SCHREG_REGD_DAT  ");
            stb.append("                 WHERE   SCHREGNO = ? ");
            stb.append("                     AND YEAR <= '" + param._year + "' ");
            stb.append("                 GROUP BY GRADE) ");
            stb.append("  GROUP BY ");
            stb.append("      YEAR ");
            stb.append("      ,ANNUAL ");
            stb.append("      ,GRADE ");
            stb.append(") ");
            //メイン表
            stb.append("SELECT ");
            stb.append("     T2.YEAR ");
            stb.append("    ,T2.GRADE ");
            stb.append("    ,T3.ELECTDIV ");
            stb.append("    ,T3.CLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("       ,T5.SCHOOL_KIND ");
                stb.append("       ,T5.CURRICULUM_CD ");
            }
            stb.append("    ,T6.SUBCLASSCD ");
            stb.append("    ,CASE WHEN T3.CLASSORDERNAME1 IS NOT NULL THEN T3.CLASSORDERNAME1 ELSE T3.CLASSNAME END AS CLASSNAME ");
            stb.append("    ,CASE WHEN T3.SHOWORDER IS NOT NULL THEN T3.SHOWORDER ELSE -1 END AS SHOWORDERCLASS ");
            stb.append("    ,CASE WHEN T6.SUBCLASSORDERNAME1 IS NOT NULL THEN T6.SUBCLASSORDERNAME1 ELSE T6.SUBCLASSNAME END AS SUBCLASSNAME ");
            stb.append("    ,T5.VALUE ");
            stb.append("FROM  SCHREG_DATA T2 ");
            stb.append("INNER JOIN VALUE_DATA T5 ON T5.YEAR = T2.YEAR ");
            stb.append("       AND T5.ANNUAL = T2.ANNUAL ");
            stb.append("INNER JOIN CLASS_MST T3 ON T3.CLASSCD = T5.CLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("       AND T3.SCHOOL_KIND = T5.SCHOOL_KIND ");
            }
            stb.append("INNER JOIN SUBCLASS_MST T6 ON T6.CLASSCD = T5.CLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("       AND T6.SCHOOL_KIND = T5.SCHOOL_KIND ");
                stb.append("       AND T6.CURRICULUM_CD = T5.CURRICULUM_CD ");
            }
            stb.append("       AND T6.SUBCLASSCD = T5.SUBCLASSCD ");
            stb.append("ORDER BY ");
            stb.append("    SHOWORDERCLASS, ");
            stb.append("    T5.CLASSCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("       T5.SCHOOL_KIND, ");
                stb.append("       T5.CURRICULUM_CD, ");
            }
            stb.append("    T5.SUBCLASSCD, ");
            stb.append("    T3.ELECTDIV, ");
            stb.append("    T2.GRADE ");
            return stb.toString();
        }
        
        public String toString() {
            return "Value[" + _classCd + ", " + _electDiv + ", " + _className + "]";
        }
    }
    
    private static class Value {
        final String _year;
        final int _grade;
        final String _value; //評定
        Value(
                final String year,
                final int grade,
                final String value) {
            _year = year;
            _grade = grade;
            _value = value;
        }
    }
    
    /**
     * 観点の教科（JVIEWNAME_SUB_MST、JVIEWSTAT_SUB_DAT）
     */
    private static class ClassViewSub {
        final String _classcd;  //教科コード
        final String _classname;  //教科名称
        final String _electdiv;
        final List _views;
        
        public ClassViewSub(
                final String classcd, 
                final String classname, 
                final String electdiv
        ) {
            _classcd = classcd;
            _classname = classname;
            _electdiv = electdiv;
            _views = new ArrayList();
        }
        
        public void addView(final ViewSub view) {
            _views.add(view);
        }
        
        public int getViewNum() {
            int c = 0;
            String viewcdOld = "";
            for (final Iterator it = _views.iterator(); it.hasNext();) {
                final ViewSub view = (ViewSub) it.next();
                if (view._viewcd != null && !viewcdOld.equals(view._viewcd)) {
                    c += 1;
                    viewcdOld = view._viewcd;
                }
            }
            return c;
        }
        
        // 教科名のセット
        private String[] getClassnameArray(final String classname) {
            if (null == classname) {
                return new String[] {""};
            }
            String[] divclassname = null;
            if (classname.length() <= 3) {
                divclassname = new String[1];
                if (classname.length() == 3) {
                    divclassname[0] = classname;
                } else if (classname.length() == 2) {
                    divclassname[0] = classname.substring(0, 1) + "　" + classname.substring(1);
                } else if (classname.length() == 1) {
                    divclassname[0] = "　" + classname + "　";
                }
            } else if (classname.length() <= 4) {
                divclassname = new String[2];
                divclassname[0] = classname.substring(0, 2);
                divclassname[1] = classname.substring(2);
            } else if (classname.indexOf("・") != -1 && classname.substring(0, classname.indexOf("・")).length() <= 3 && classname.substring(classname.indexOf("・") + 1).length() <= 3) {
                divclassname = new String[3];
                divclassname[0] = classname.substring(0, classname.indexOf("・"));
                divclassname[1] = "・";
                divclassname[2] = classname.substring(classname.indexOf("・") + 1);
            } else {
                final List bufferList = new ArrayList();
                int currentByte = 0;
                StringBuffer currentBuffer = null;
                for (int i = 0; i < classname.length(); i++) {
                    final char ch = classname.charAt(i);
                    final int chByte = KNJ_EditEdit.getMS932ByteLength(String.valueOf(ch));
                    if (currentBuffer == null || currentByte + chByte > 6) {
                        currentByte = 0;
                        currentBuffer = new StringBuffer();
                        bufferList.add(currentBuffer);
                    }
                    currentBuffer.append(ch);
                    currentByte += chByte;
                }
                divclassname = new String[bufferList.size()];
                for (int i = 0; i < divclassname.length; i++) {
                    divclassname[i] = ((StringBuffer) bufferList.get(i)).toString();
                }
            }
            final String[] rtn = new String[Math.max(_views.size(), divclassname.length)];
            for (int i = 0; i < rtn.length; i++) {
                rtn[i] = "";
            }
            for (int st = (rtn.length / 2 + (rtn.length % 2)) - (divclassname.length / 2 + (divclassname.length % 2)), i = st; i < st + divclassname.length; i++) {
                rtn[i] = divclassname[i - st];
            }
            return rtn;
        }
        
        public String toString() {
            return "[" + _classcd + ":" + _classname + " e = " + _electdiv + "]";
        }
        
        private static ClassViewSub getClassViewSub(final List classViewList, final String classcd, final String classname, final String electdiv) {
            ClassViewSub classView = null;
            for (final Iterator it = classViewList.iterator(); it.hasNext();) {
                final ClassViewSub classView0 = (ClassViewSub) it.next();
                if (classView0._classcd.equals(classcd) && classView0._classname.equals(classname) && classView0._electdiv.equals(electdiv)) {
                    classView = classView0;
                    break;
                }
            }
            return classView;
        }

        private static ViewSub getViewSub(final List viewSubList, final String subclasscd, final String viewcd) {
            ViewSub viewSub = null;
            for (final Iterator it = viewSubList.iterator(); it.hasNext();) {
                final ViewSub viewSub0 = (ViewSub) it.next();
                if (viewSub0._classcd.equals(subclasscd) && viewSub0._viewcd.equals(viewcd)) {
                    viewSub = viewSub0;
                    break;
                }
            }
            return viewSub;
        }

        /**
         * 観点のリストを得る
         * @param db2
         * @param param
         * @param schregno
         * @return
         */
        public static List load(final DB2UDB db2, final Param param, final String schregno) {
            final List classViewList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getViewRecordSql(param);
                ps = db2.prepareStatement(sql);
                int p = 0;
                ps.setString(++p, schregno);
                ps.setString(++p, schregno);
                ps.setString(++p, schregno);
                rs = ps.executeQuery();
                
                while (rs.next()) {
                    //教科コードの変わり目
                    final String classcd;
                    if ("1".equals(param._useCurriculumcd)) {
                        classcd = rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND");
                    } else {
                        classcd = rs.getString("CLASSCD");
                    }
                    final String classname = rs.getString("CLASSNAME");
//                    final String subclasscd;
//                    if ("1".equals(param._useCurriculumcd)) {
//                        subclasscd = rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-" + rs.getString("CURRICULUM_CD") + "-" + rs.getString("SUBCLASSCD");
//                    } else {
//                        subclasscd = rs.getString("SUBCLASSCD");
//                    }
                    final String viewcd = rs.getString("VIEWCD");
                    final String viewname = rs.getString("VIEWNAME");
                    final String hasYear1 = rs.getString("HAS_YEAR1");
                    final String hasYear2 = rs.getString("HAS_YEAR2");
                    final String hasYear3 = rs.getString("HAS_YEAR3");
                    final String status1 = rs.getString("STATUS1");
                    final String status2 = rs.getString("STATUS2");
                    final String status3 = rs.getString("STATUS3");
                    final String electdiv = rs.getString("ELECTDIV");  //必修:null or 0, 選択:1
                    
                    ClassViewSub classViewSub = getClassViewSub(classViewList, classcd, classname, electdiv);
                    if (null == classViewSub) {
                        classViewSub = new ClassViewSub(classcd, classname, electdiv);
                        classViewList.add(classViewSub);
                    }
                    ViewSub viewSub = getViewSub(classViewSub._views, classcd, viewcd);
                    if (null == viewSub) {
                        viewSub = new ViewSub(classcd, viewcd, viewname);
                        classViewSub.addView(viewSub);
                    }
                    if ("1".equals(param._useCurriculumcd)) {
                        viewSub.setCurriculumYear(rs.getString("CURRICULUM_CD"), hasYear1, hasYear2, hasYear3);
                    } else {
                        viewSub.setCurriculumYear("2", hasYear1, hasYear2, hasYear3);
                    }
                    if (null != status1) {
                        final int g = param.getGradeCd(rs.getString("YEAR1"), rs.getString("GRADE1")); // 学年
                        View view = new View(rs.getString("YEAR1"), g, status1);
                        viewSub._views.add(view);
                    }
                    if (null != status2) {
                        final int g = param.getGradeCd(rs.getString("YEAR2"), rs.getString("GRADE2")); // 学年
                        View view = new View(rs.getString("YEAR2"), g, status2);
                        viewSub._views.add(view);
                    }
                    if (null != status3) {
                        final int g = param.getGradeCd(rs.getString("YEAR3"), rs.getString("GRADE3")); // 学年
                        View view = new View(rs.getString("YEAR3"), g, status3);
                        viewSub._views.add(view);
                    }
                }
            } catch (Exception ex) {
                log.error("printSvfDetail_1 error!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return classViewList;
        }
        
        /**
         *  priparedstatement作成  成績データ（観点）
         */
        private static String getViewRecordSql(final Param param) {
            
            final StringBuffer stb = new StringBuffer();
            stb.append("WITH ");
            //観点の表
            stb.append("VIEW_DATA AS( ");
            stb.append("  SELECT ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("      CLASSCD, ");
                stb.append("      SCHOOL_KIND, ");
                stb.append("      CURRICULUM_CD, ");
            }
            stb.append("      SUBCLASSCD ");
            stb.append("     ,VIEWCD ");
            stb.append("     ,YEAR ");
            stb.append("     ,STATUS ");
            stb.append("  FROM ");
            stb.append("     JVIEWSTAT_SUB_DAT T1 ");
            stb.append("  WHERE ");
            stb.append("     T1.SCHREGNO = ? ");
            stb.append("    AND T1.YEAR <= '" + param._year + "' ");
            stb.append("    AND T1.SEMESTER = '9' ");
            stb.append("    AND SUBSTR(T1.VIEWCD,3,2) <> '99' ");
            stb.append(") ");
            
            //学籍の表
            stb.append(",SCHREG_DATA AS( ");
            stb.append("  SELECT  YEAR ");
            stb.append("         ,GRADE  ");
            stb.append("  FROM    SCHREG_REGD_DAT  ");
            stb.append("  WHERE   SCHREGNO = ?  ");
            stb.append("      AND YEAR IN (SELECT  MAX(YEAR)  ");
            stb.append("                 FROM    SCHREG_REGD_DAT  ");
            stb.append("                 WHERE   SCHREGNO = ? ");
            stb.append("                     AND YEAR <= '" + param._year + "' ");
            stb.append("                 GROUP BY GRADE)  ");
            stb.append("  GROUP BY YEAR,GRADE  ");
            stb.append(") ");
            
            //メイン表
            stb.append("SELECT ");
            stb.append("    VALUE(T3.ELECTDIV, '0') AS ELECTDIV ");
            stb.append("   ,T3.CLASSCD");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("  ,T2.SCHOOL_KIND ");
                stb.append("  ,T2.CURRICULUM_CD ");
            }
            stb.append("   ,T2.SUBCLASSCD");
            stb.append("   ,CASE WHEN T3.CLASSORDERNAME1 IS NOT NULL THEN T3.CLASSORDERNAME1 ELSE T3.CLASSNAME END AS CLASSNAME ");
            stb.append("   ,CASE WHEN T3.SHOWORDER IS NOT NULL THEN T3.SHOWORDER ELSE -1 END AS SHOWORDERCLASS ");
            stb.append("   ,T2.VIEWCD ");
            stb.append("   ,T2.VIEWNAME ");
            stb.append("   ,T2.YEAR1 ");
            stb.append("   ,T2.YEAR2 ");
            stb.append("   ,T2.YEAR3 ");
            stb.append("   ,T2.HAS_YEAR1 ");
            stb.append("   ,T2.HAS_YEAR2 ");
            stb.append("   ,T2.HAS_YEAR3 ");
            stb.append("   ,VT1.STATUS AS STATUS1 ");
            stb.append("   ,VT2.STATUS AS STATUS2 ");
            stb.append("   ,VT3.STATUS AS STATUS3 ");
            stb.append("   ,RT1.GRADE AS GRADE1 ");
            stb.append("   ,RT2.GRADE AS GRADE2 ");
            stb.append("   ,RT3.GRADE AS GRADE3 ");
            stb.append("FROM  ( SELECT ");
            stb.append("          W1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("          , W1.CLASSCD ");
                stb.append("          , W1.SCHOOL_KIND ");
                stb.append("          , W1.CURRICULUM_CD ");
            }
            stb.append("          , W1.VIEWCD ");
            stb.append("          , VIEWNAME ");
            stb.append("          , '" + param._jviewYear[0] + "' AS YEAR1 ");
            stb.append("          , '" + param._jviewYear[1] + "' AS YEAR2 ");
            stb.append("          , '" + param._jviewYear[2] + "' AS YEAR3 ");
            stb.append("          , CASE WHEN L1.YEAR IS NOT NULL THEN 1 END AS HAS_YEAR1 ");
            stb.append("          , CASE WHEN L2.YEAR IS NOT NULL THEN 1 END AS HAS_YEAR2 ");
            stb.append("          , CASE WHEN L3.YEAR IS NOT NULL THEN 1 END AS HAS_YEAR3 ");
            stb.append("          , CASE WHEN W1.SHOWORDER IS NOT NULL THEN W1.SHOWORDER ELSE -1 END AS SHOWORDERVIEW ");
            stb.append("        FROM    JVIEWNAME_SUB_MST W1 ");
            stb.append("               LEFT JOIN JVIEWNAME_SUB_YDAT L1 ON L1.YEAR = '" + param._jviewYear[0] + "' AND L1.CLASSCD = W1.CLASSCD AND L1.SCHOOL_KIND = W1.SCHOOL_KIND AND L1.CURRICULUM_CD = W1.CURRICULUM_CD AND L1.SUBCLASSCD = W1.SUBCLASSCD AND L1.VIEWCD = W1.VIEWCD ");
            stb.append("               LEFT JOIN JVIEWNAME_SUB_YDAT L2 ON L2.YEAR = '" + param._jviewYear[1] + "' AND L2.CLASSCD = W1.CLASSCD AND L2.SCHOOL_KIND = W1.SCHOOL_KIND AND L2.CURRICULUM_CD = W1.CURRICULUM_CD AND L2.SUBCLASSCD = W1.SUBCLASSCD AND L2.VIEWCD = W1.VIEWCD ");
            stb.append("               LEFT JOIN JVIEWNAME_SUB_YDAT L3 ON L3.YEAR = '" + param._jviewYear[2] + "' AND L3.CLASSCD = W1.CLASSCD AND L3.SCHOOL_KIND = W1.SCHOOL_KIND AND L3.CURRICULUM_CD = W1.CURRICULUM_CD AND L3.SUBCLASSCD = W1.SUBCLASSCD AND L3.VIEWCD = W1.VIEWCD ");
            stb.append("        WHERE W1.SCHOOL_KIND = 'J' ");
            stb.append("              AND (L1.YEAR IS NOT NULL OR L2.YEAR IS NOT NULL OR L3.YEAR IS NOT NULL) ");
            stb.append("      ) T2 ");
            stb.append("INNER JOIN CLASS_MST T3 ON ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    T3.CLASSCD = T2.CLASSCD AND T3.SCHOOL_KIND = T2.SCHOOL_KIND ");
            } else {
                stb.append("    T3.CLASSCD = SUBSTR(T2.VIEWCD,1,2)  ");
            }
            stb.append("LEFT JOIN VIEW_DATA VT1 ON VT1.YEAR = T2.YEAR1 ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    AND VT1.CLASSCD = T2.CLASSCD  ");
                stb.append("    AND VT1.SCHOOL_KIND = T2.SCHOOL_KIND  ");
                stb.append("    AND VT1.CURRICULUM_CD = T2.CURRICULUM_CD  ");
            }
            stb.append("    AND VT1.SUBCLASSCD = T2.SUBCLASSCD  ");
            stb.append("    AND VT1.VIEWCD = T2.VIEWCD  ");
            stb.append("LEFT JOIN SCHREG_DATA RT1 ON RT1.YEAR = T2.YEAR1 ");
            stb.append("LEFT JOIN VIEW_DATA VT2 ON VT2.YEAR = T2.YEAR2 ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    AND VT2.CLASSCD = T2.CLASSCD  ");
                stb.append("    AND VT2.SCHOOL_KIND = T2.SCHOOL_KIND  ");
                stb.append("    AND VT2.CURRICULUM_CD = T2.CURRICULUM_CD  ");
            }
            stb.append("    AND VT2.SUBCLASSCD = T2.SUBCLASSCD  ");
            stb.append("    AND VT2.VIEWCD = T2.VIEWCD  ");
            stb.append("LEFT JOIN SCHREG_DATA RT2 ON RT2.YEAR = T2.YEAR2 ");
            stb.append("LEFT JOIN VIEW_DATA VT3 ON VT3.YEAR = T2.YEAR3 ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    AND VT3.CLASSCD = T2.CLASSCD  ");
                stb.append("    AND VT3.SCHOOL_KIND = T2.SCHOOL_KIND  ");
                stb.append("    AND VT3.CURRICULUM_CD = T2.CURRICULUM_CD  ");
            }
            stb.append("    AND VT3.SUBCLASSCD = T2.SUBCLASSCD  ");
            stb.append("    AND VT3.VIEWCD = T2.VIEWCD  ");
            stb.append("LEFT JOIN SCHREG_DATA RT3 ON RT3.YEAR = T2.YEAR3 ");
            stb.append("ORDER BY ");
            stb.append("    VALUE(SHOWORDERCLASS, -1), ");
            stb.append("    T3.CLASSCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("  T2.SCHOOL_KIND, ");
            }
            stb.append("    VALUE(T3.ELECTDIV, '0'), ");
            stb.append("    VALUE(T2.SHOWORDERVIEW, -1), ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("  T2.CURRICULUM_CD, ");
            }
            stb.append("    T2.VIEWCD ");
            return stb.toString();
        }
    }
    
    /**
     * 観点データ
     */
    private static class ViewSub {
        final String _classcd; // 科目コード
        final String _viewcd;  //観点コード
        final String _viewname;  //観点コード
        final List _views;
        final Map _slashYear1Map;
        final Map _slashYear2Map;
        final Map _slashYear3Map;

        public ViewSub(
                final String classcd,
                final String viewcd, 
                final String viewname
        ) {
            _classcd = classcd;
            _viewcd = viewcd;
            _viewname = viewname;
            _views = new ArrayList();
            _slashYear1Map = new HashMap();
            _slashYear2Map = new HashMap();
            _slashYear3Map = new HashMap();
        }
        public void setCurriculumYear(final String curriculumCd, final String year1, final String year2, final String year3) {
            _slashYear1Map.put(curriculumCd, year1);
            _slashYear2Map.put(curriculumCd, year2);
            _slashYear3Map.put(curriculumCd, year3);
        }
        public boolean year1IsSlash() {
            return valueAllNull(_slashYear1Map);
        }
        public boolean year2IsSlash() {
            return valueAllNull(_slashYear2Map);
        }
        public boolean year3IsSlash() {
            return valueAllNull(_slashYear3Map);
        }
        private boolean valueAllNull(final Map slashYear) {
            for (final Iterator it = slashYear.values().iterator(); it.hasNext();) {
                final String yearn = (String) it.next();
                if (null != yearn) {
                    return false;
                }
            }
            return true;
        }
    }
    
    private static class View {
        final String _year;
        final int _g;
        final String _view; // 観点
        public View(
                final String year,
                final int g,
                final String view) {
            _year = year;
            _g = g;
            _view = view;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 67010 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _year;
        final String _gakki;
        final String[] _categorySelected;
        final String _useCurriculumcd;
        final String _seki;
        final String _date;
        final String _grade;
        final String _gradeHrClass;
        final String _kotyo;
        final Map _gradeCdMap;
        final String _z010Name1;
        final boolean _isSundaikoufu; // 駿台甲府はTrue
        
        private int[] _jviewYear;
        private String _teacherName;
        private String _schoolLoc;
        private String _schoolName;
        private String _principalName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _gakki = request.getParameter("GAKKI");
            _categorySelected = request.getParameterValues("category_selected");
            _useCurriculumcd = "1"; // request.getParameter("useCurriculumcd");
            _gradeCdMap = getGradeCdMap(db2);
            _seki = request.getParameter("SEKI");
            _date = request.getParameter("DATE");
            _grade = request.getParameter("GRADE");
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            _kotyo = request.getParameter("KOTYO");
            _z010Name1 = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' "));
            _isSundaikoufu = "sundaikoufu".equals(_z010Name1);
            setTeacherName(db2);
            setCertifKind(db2);
            
            final int grade = Integer.parseInt(request.getParameter("GRADE"));
            final int iYear = Integer.parseInt(_year);
            if (grade >= 3) {
                _jviewYear = new int[] { iYear - 2, iYear - 1, iYear };
            } else if (grade == 2) {
                _jviewYear = new int[] { iYear - 1, iYear, iYear + 1 };
            } else if (grade <= 1) {
                _jviewYear = new int[] { iYear, iYear + 1, iYear + 2 };
            }
        }
        
        public int getGradeCd(final String year, final String grade) {
            final String gradeCd = (String) _gradeCdMap.get(year + grade);
            int n = -1;
            try {
                n = Integer.parseInt(gradeCd);
            } catch (Exception e) {
                log.error("SCHREG_REGD_GDAT.GRADE_CD IS NOT NUMBER. value = '" + gradeCd + "'");
            }
            return n;
        }

        private Map getGradeCdMap(final DB2UDB db2) {
            final Map gdatMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     * ");
                stb.append(" FROM ");
                stb.append("     SCHREG_REGD_GDAT T1 ");
                stb.append(" WHERE ");
                stb.append("     T1.SCHOOL_KIND = 'J' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String year = rs.getString("YEAR");
                    final String grade = rs.getString("GRADE");
                    gdatMap.put(year + grade, rs.getString("GRADE_CD"));
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return gdatMap;
        }
        
        private void setTeacherName(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     * ");
                stb.append(" FROM ");
                stb.append("     STAFF_MST ");
                stb.append(" WHERE ");
                stb.append("     STAFFCD = '" + _seki + "' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    _teacherName = rs.getString("STAFFNAME");
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        
        private void setCertifKind(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     * ");
                stb.append(" FROM ");
                stb.append("     CERTIF_SCHOOL_DAT ");
                stb.append(" WHERE ");
                stb.append("     YEAR = '" + _year + "' ");
                stb.append("     AND CERTIF_KINDCD = '115' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    _schoolLoc = rs.getString("REMARK4");
                    _schoolName = rs.getString("SCHOOL_NAME");
                    _principalName = rs.getString("PRINCIPAL_NAME");
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
    }
}

// eof

