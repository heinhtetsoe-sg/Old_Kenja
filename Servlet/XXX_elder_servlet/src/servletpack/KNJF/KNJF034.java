/*
 * $Id: d786da88c0bfd1cb9a5527d2f2cb91e7c346de19 $
 *
 * 作成日: 2011/12/07
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJF;


import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

import servletpack.KNJZ.detail.KNJServletUtils;

/**
 * 発育の記録・視力検査の結果 (明治)
 *
 */
public class KNJF034 {

    private static final Log log = LogFactory.getLog(KNJF034.class);

    private boolean _hasData;

    private boolean _errorOccured;

    private Param _param;

    /** グラフイメージファイルの Map&lt;String,File&gt; */
    private Collection _graphFiles;

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

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
            removeImageFiles();
        }

    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        _graphFiles = new ArrayList();

        final List studentList = getStudentList(db2);

        _param._physAvgMap = HexamPhysicalAvgDat.getHexamPhysicalAvgMap(db2, _param);

        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            printPage(db2, svf, student);
        }
    }

    private void printSvfImageFile(final Vrw32alp svf, final String field, final File file) {
        if (null == file || !file.exists()) {
            return;
        }
        svf.VrsOut(field, file.toString());
    }

    private void printPage(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        final String form = "1".equals(_param._useFormKNJF034_2) ? "KNJF034_2.frm" : "KNJF034.frm";
        svf.VrSetForm(form, 1);
        svf.VrsOut("SCHOOL_NAME", _param.getSchoolname(student._schoolKind)); // 学校名
        svf.VrsOut("HRNO", student.getHrnameAttendno(_param)); // 年組番号
        svf.VrsOut("NAME", student._name); // 氏名

        for (final Iterator it = student._medexamDetNoDatList.iterator(); it.hasNext();) {
            final MedexamDetNoDat mdnd = (MedexamDetNoDat) it.next();

            final int g = Integer.parseInt(mdnd._grade);
            final String si = String.valueOf(Integer.parseInt(mdnd._no));
            svf.VrsOutn("MONTH" + si, g, getTuki(mdnd._date)); // 身長
            svf.VrsOutn("HEIGHT" + si, g, mdnd._height); // 身長
            svf.VrsOutn("WEIGHT" + si, g, mdnd._weight); // 体重
            if (_param._isDispHiman) {
                svf.VrsOutn("FAT" + si, g, calcHimando(student, mdnd, _param)); // 肥満度
            }
            if (Integer.parseInt(mdnd._no) == 1) {
                svf.VrsOutn("SIT_HEIGHT" + si, g, mdnd._sitheight); // 座高
            }

            if (!"1".equals(_param._useFormKNJF034_2)) {
                svf.VrsOutn("SIGHTR" + si + "_1", g, mdnd._rBarevisionMark); // 視力
                svf.VrsOutn("SIGHTR" + si + "_2", g, mdnd._rVisionMark); // 視力
                svf.VrsOutn("SIGHTL" + si + "_1", g, mdnd._lBarevisionMark); // 視力
                svf.VrsOutn("SIGHTL" + si + "_2", g, mdnd._lVisionMark); // 視力
            }
        }
        if ("1".equals(_param._useFormKNJF034_2)) {
            printSvfImageFile(svf, "LEGEND1", _param._image2_01);
            printSvfImageFile(svf, "LEGEND2", _param._image2_01);
            if (null != _param._image2_01) {
                svf.VrsOut("LEGEND_TEXT1", "本人の身長");
                svf.VrsOut("LEGEND_TEXT2", "本人の体重");
            }

            final String sexAbbv1 = StringUtils.defaultString((String) _param._sexAbbv1Map.get(student._sex));
            final Map paramMap1 = new HashMap();
            paramMap1.put("YEAR", _param._year);
            paramMap1.put("DATA_DIV", "1");
            paramMap1.put("STUDENT", student);
            paramMap1.put("SEX", student._sex);
            paramMap1.put("UNIT_LABEL", "（cm）");
            paramMap1.put("TICK_UNIT", String.valueOf(10));
            paramMap1.put("Param", _param);

            final File file1 = createPercentileGraphFile(2148, 1684, db2, _param, paramMap1); // 身長
            svf.VrsOut("GRAPH_TITLE1", "身長発育パーセンタイル曲線（" + sexAbbv1 + "子）");
            printSvfImageFile(svf, "GRAPH1", file1);

            final Map paramMap2 = new HashMap();
            paramMap2.put("YEAR", _param._year);
            paramMap2.put("DATA_DIV", "2");
            paramMap2.put("STUDENT", student);
            paramMap2.put("SEX", student._sex);
            paramMap2.put("UNIT_LABEL", "（kg）");
            paramMap2.put("TICK_UNIT", String.valueOf(5));
            paramMap2.put("Param", _param);

            final File file2 = createPercentileGraphFile(2148, 1684, db2, _param, paramMap2); // 体重
            svf.VrsOut("GRAPH_TITLE2", "体重発育パーセンタイル曲線（" + sexAbbv1 + "子）");
            printSvfImageFile(svf, "GRAPH2", file2);

            String usedYear = (String) _param._sessionMap.get("USED_DATA_YEAR");
            if (NumberUtils.isDigits(usedYear)) {
                svf.VrsOut("GRAPH_REMARK", KenjaProperties.gengou(Integer.parseInt(usedYear)) + "年度学校保健統計資料より");
            }
        } else {
            if (!_param._isDispHiman) {
            	printSvfImageFile(svf, "BLANK", _param._imageWSP);
            	printSvfImageFile(svf, "BLANK2", _param._imageWSP);
            } else {
                printSvfImageFile(svf, "PIC1", _param._image01);
                printSvfImageFile(svf, "PIC2", _param._image02);
                printSvfImageFile(svf, "PIC3", _param._image03);
            }
            final File file = createGraphFile(student, _param, 1624, 2456);
            printSvfImageFile(svf, "GRAPH", file);
        }
        printSvfImageFile(svf, "PIC", _param._image04);
        printSvfImageFile(svf, "INTRO", _param._image05); // 凡例
        svf.VrEndPage();
        _hasData = true;
    }

    private String getTuki(final String date) {
        final StringBuffer stb = new StringBuffer();
        if (null != date) {
            try {
                final SimpleDateFormat sdf = new SimpleDateFormat("M");
                final String tuki = sdf.format(java.sql.Date.valueOf(date));
                for (int i = 0; i < tuki.length(); i++) {
                    char ch = tuki.charAt(i);
                    if ('0' <= ch && ch <= '9') {
                        ch = (char) (ch - '0' + '０');
                    }
                    stb.append(ch);
                }
            } catch (Exception e) {
                log.info(" parse failed :", e);
            }
        }
        stb.append("月");
        return stb.toString();
    }

    // 肥満度計算
    //  肥満度（過体重度）= 100 × (測定された体重 - 標準体重) / 標準体重
    public static String calcHimando(final Student student, final MedexamDetNoDat mdnd, final Param param) {
        if (null == mdnd._weight) {
            log.debug(" " + student._schregno + ", " + mdnd._year + " 体重がnull");
            return null;
        }
        BigDecimal weightAvg = null;
        final boolean isUseMethod2 = true;
        if (isUseMethod2) {
            // final BigDecimal weightAvg1 = getWeightAvgMethod1(student, mdnd, param);
            final BigDecimal weightAvg2 = getWeightAvgMethod2(student, mdnd, param._physAvgMap, param);
            // log.fatal(" (schregno, attendno, weight1, weight2) = (" + student._schregno + ", " + student._attendno + ", " + weightAvg1 + ", " + weightAvg2 + ")");
            log.fatal(" (schregno, attendno, weight2) = (" + student._schregno + ", " + student._attendno + ", " + weightAvg2 + ")");
            weightAvg = weightAvg2;
        } else {
            // weightAvg = null; getWeightAvgMethod0(student, mdnd, physAvgMap);
        }
        if (null == weightAvg) {
            return null;
        }
        final BigDecimal himando = new BigDecimal(100).multiply(new BigDecimal(Double.parseDouble(mdnd._weight)).subtract(weightAvg)).divide(weightAvg, 1, BigDecimal.ROUND_HALF_UP);
        log.fatal(" himando = 100 * (" + mdnd._weight + " - " + weightAvg + ") / " + weightAvg + " = " + himando);
        return himando.toString();
    }

    private static BigDecimal getWeightAvgMethod2(final Student student, final MedexamDetNoDat mdnd, final Map physAvgMap, final Param param) {
        if (null == mdnd._height) {
            log.debug(" " + student._schregno + ", " + mdnd._year + " 身長がnull");
            return null;
        }
        // 日本小児内分泌学会 (http://jspe.umin.jp/)
        // http://jspe.umin.jp/ipp_taikaku.htm ２．肥満度 ２）性別・年齢別・身長別標準体重（５歳以降）のデータによる
        // ａ＝ HEXAM_PHYSICAL_AVG_DAT.STD_WEIGHT_KEISU_A
        // ｂ＝ HEXAM_PHYSICAL_AVG_DAT.STD_WEIGHT_KEISU_B　
        // 標準体重＝ａ×身長（cm）- ｂ 　 　
        final BigDecimal height = new BigDecimal(mdnd._height);
        final int iNenrei = (int) getNenrei2(student, param._year, mdnd._year);
        final HexamPhysicalAvgDat hpad = getPhysicalAvgDatNenrei(iNenrei, (List) physAvgMap.get(student._sex));
        if (null == hpad || null == hpad._stdWeightKeisuA || null == hpad._stdWeightKeisuB) {
            return null;
        }
        final BigDecimal a = hpad._stdWeightKeisuA;
        final BigDecimal b = hpad._stdWeightKeisuB;
        final BigDecimal avgWeight = a.multiply(height).subtract(b);
        log.fatal(" method2 avgWeight = " + a + " * " + height + " - " + b + " = " + avgWeight);
        return avgWeight;
    }

    // 年齢の平均データを得る
    private static HexamPhysicalAvgDat getPhysicalAvgDatNenrei(final int nenrei, final List physAvgList) {
        HexamPhysicalAvgDat tgt = null;
        for (final Iterator it = physAvgList.iterator(); it.hasNext();) {
            final HexamPhysicalAvgDat hpad = (HexamPhysicalAvgDat) it.next();
            if (hpad._nenrei <= nenrei) {
                tgt = hpad;
                if (hpad._nenreiYear == nenrei) {
                    break;
                }
            }
        }
        return tgt;
    }
    // 年齢以下の直近の標準体重を得る
    private static HexamPhysicalAvgDat getPhysicalAvgDat(final double nenrei, final List physAvgList) {
        HexamPhysicalAvgDat tgt = null;
        for (final Iterator it = physAvgList.iterator(); it.hasNext();) {
            final HexamPhysicalAvgDat hpad = (HexamPhysicalAvgDat) it.next();
            if (hpad._nenrei <= nenrei && null != hpad._weightAvg) {
                tgt = hpad;
                if (hpad._nenrei == nenrei) {
                    break;
                }
            }
        }
        return tgt;
    }

    // 生年月日と対象日付から年齢を計算する
    public static double getNenrei2(final Student student, final String year1, final String year2) {
        final double startYear;
        if ("J".equals(student._gradeCd)) {
            startYear = 11.0;
        } else if ("H".equals(student._gradeCd)) {
            startYear = 14.0;
        } else {
            startYear = 5.0;
        }
        return startYear + Integer.parseInt(StringUtils.defaultString(student._gradeCd, student._grade)) - (StringUtils.isNumeric(year1) && StringUtils.isNumeric(year2) ? Integer.parseInt(year1) - Integer.parseInt(year2) : 0); // 1年生:6才、2年生:7才、...6年生:11才
    }

    // 生年月日と対象日付から年齢を計算する
    public static final double getNenrei(final Student student, final String date, final String year1, final String year2) {
        if (null == student._birthday) {
            return getNenrei2(student, year1, year2);
        }
        final Calendar calBirthDate = Calendar.getInstance();
        calBirthDate.setTime(Date.valueOf(student._birthday));
        final int birthYear = calBirthDate.get(Calendar.YEAR);
        final int birthDayOfYear = calBirthDate.get(Calendar.DAY_OF_YEAR);

        final Calendar calTestDate = Calendar.getInstance();
        calTestDate.setTime(Date.valueOf(date));
        final int testYear = calTestDate.get(Calendar.YEAR);
        final int testDayOfYear = calTestDate.get(Calendar.DAY_OF_YEAR);

        int nenreiYear = testYear - birthYear + (testDayOfYear - birthDayOfYear < 0 ? -1 : 0);
        final int nenreiDateOfYear = testDayOfYear - birthDayOfYear + (testDayOfYear - birthDayOfYear < 0 ? 365 : 0);
        final double nenrei = nenreiYear + nenreiDateOfYear / 365.0;
        return nenrei;
    }

    private File graphImageFile(final JFreeChart chart, final int width, final int height) {

        final int dotWidth = 1624 * 4 / 3;
        final int dotHeight = height * 4 / 3;

        final String tmpFileName = KNJServletUtils.createTmpFile(".png");
        log.fatal("\ttmp file name=" + tmpFileName);

        final File outputFile = new File(tmpFileName);
        try {
            ChartUtilities.saveChartAsPNG(outputFile, chart, dot2pixel(dotWidth), dot2pixel(dotHeight));
        } catch (final IOException ioEx) {
            log.error("グラフイメージをファイル化できません。", ioEx);
        }

        return outputFile;
    }

    private void removeImageFiles() {
        for (final Iterator it = _graphFiles.iterator(); it.hasNext();) {
            final File imageFile = (File) it.next();
            if (null == imageFile) {
                continue;
            }
            log.fatal("グラフ画像ファイル削除:" + imageFile.getName() + " : " + imageFile.delete());
        }
    }

    private static int dot2pixel(final int dot) {
        final int pixel = dot / 4;

        /*
         * 少し大きめにする事でSVFに貼り付ける際の拡大を防ぐ。
         * 拡大すると粗くなってしまうから。
         */
        return (int) (pixel * 1.0);
    }

    private File createPercentileGraphFile(final int width, final int height, final DB2UDB db2, final Param param, final Map paramMap) {
        File file = null;
        try {
            final Integer paramHashCode = new Integer(paramMap.hashCode());
            if (!param._sessionMap.containsKey(paramHashCode)) {
                File f = graphImageFile(KNJF034Graph.createPercentileChart(db2, paramMap), width, height);
                _graphFiles.add(f);
                param._sessionMap.put(paramHashCode, f);
                param._sessionMap.put("USED_DATA_YEAR", paramMap.get("USED_DATA_YEAR"));
            }
            return (File) param._sessionMap.get(paramHashCode);
        } catch (Exception e) {
            log.fatal("exception!", e);
        } catch (Error err) {
            if (!_errorOccured) {
                log.fatal("error!", err);
                _errorOccured = true;
            }
        }
        return file;
    }

    private File createGraphFile(final Student student, final Param param, final int width, final int height) {
        File file = null;
        try {
            final List list = (List) param._physAvgMap.get(student._sex);
            final String sexName = StringUtils.defaultString((String) _param._sexNameMap.get(student._sex));
            file = graphImageFile(KNJF034Graph.createChart(student, _param, sexName, list), width, height);
            _graphFiles.add(file);
        } catch (Exception e) {
            log.fatal("exception!", e);
        } catch (Error err) {
            if (!_errorOccured) {
                log.fatal("error!", err);
                _errorOccured = true;
            }
        }
        return file;
    }

    private List getStudentList(final DB2UDB db2) {
        final List rtnList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final Map studentMap = new HashMap();
        try {
            final String sql = Student.getStudentSql(_param);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String attendno = rs.getString("ATTENDNO");
                final String name = rs.getString("NAME");
                final String sex = rs.getString("SEX");
                final String birthday = rs.getString("BIRTHDAY");
                final Student student = new Student(schregno, attendno, name, sex, birthday);
                if (_param._isGhr) {
                    student._ghrCd = rs.getString("GHR_CD");
                    student._ghrName = rs.getString("GHR_NAME");
                } else {
                    student._grade = rs.getString("GRADE");
                    student._gradeCd = rs.getString("GRADE_CD");
                    student._schoolKind = rs.getString("SCHOOL_KIND");
                    student._hrClass = rs.getString("HR_CLASS");
                    student._hrName = rs.getString("HR_NAME");
                }
                rtnList.add(student);
                studentMap.put(schregno, student);
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        try {
            final String sql = MedexamDetNoDat.getMedexamDetNoDatSql(_param);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String year = rs.getString("YEAR");
                final String schregno = rs.getString("SCHREGNO");
                final String grade = rs.getString("GRADE");
                final String no = rs.getString("NO");
                final String date = rs.getString("DATE");
                final String height = rs.getString("HEIGHT");
                final String weight = rs.getString("WEIGHT");
                final String sitheight = rs.getString("SITHEIGHT");
                final String rBarevisionMark = rs.getString("R_BAREVISION_MARK");
                final String lBarevisionMark = rs.getString("L_BAREVISION_MARK");
                final String rVisionMark = rs.getString("R_VISION_MARK");
                final String lVisionMark = rs.getString("L_VISION_MARK");
                final MedexamDetNoDat medexamdetnodat = new MedexamDetNoDat(year, schregno, grade, no, date, height, weight, sitheight, rBarevisionMark, lBarevisionMark, rVisionMark, lVisionMark);

                final Student student = (Student) studentMap.get(schregno);
                if (null == student) {
                    continue;
                }
                student._medexamDetNoDatList.add(medexamdetnodat);
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtnList;
    }

    static class Student {
        final String _schregno;
        final String _attendno;
        final String _name;
        final String _sex;
        final String _birthday;
        String _grade;
        String _gradeCd;
        String _schoolKind;
        String _hrClass;
        String _hrName;
        String _ghrCd;
        String _ghrName;

        final List _medexamDetNoDatList = new ArrayList();

        Student(
            final String schregno,
            final String attendno,
            final String name,
            final String sex,
            final String birthday
        ) {
            _schregno = schregno;
            _attendno = attendno;
            _name = name;
            _sex = sex;
            _birthday = birthday;
        }
        public String getHrnameAttendno(final Param param) {
            String rtn = "";
            if (param._isGhr) {
                rtn += StringUtils.defaultString(_ghrName);
            } else {
                rtn += StringUtils.defaultString(_hrName);
            }
            if (NumberUtils.isNumber(_attendno)) {
                rtn += Integer.parseInt(_attendno) + "番";
            } else {
                rtn += StringUtils.defaultString(_attendno);
            }
            return rtn;
        }

        private static String getStudentSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     REGD.SCHREGNO, ");
            if (param._isGhr) {
                stb.append("     REGD.GHR_CD, ");
                stb.append("     REGD.GHR_ATTENDNO AS ATTENDNO, ");
                stb.append("     REGDH.GHR_NAME, ");
            } else {
                stb.append("     REGD.GRADE, ");
                stb.append("     REGDG.GRADE_CD, ");
                stb.append("     REGDG.SCHOOL_KIND, ");
                stb.append("     REGD.HR_CLASS, ");
                stb.append("     REGD.ATTENDNO, ");
                stb.append("     REGDH.HR_NAME, ");
            }
            stb.append("     BASE.NAME, ");
            stb.append("     BASE.SEX, ");
            stb.append("     BASE.BIRTHDAY ");
            stb.append(" FROM ");
            if (param._isGhr) {
                stb.append("     SCHREG_REGD_GHR_DAT REGD ");
                stb.append("     LEFT JOIN SCHREG_REGD_GHR_HDAT REGDH ON REGDH.YEAR = REGD.YEAR ");
                stb.append("         AND REGDH.SEMESTER = REGD.SEMESTER ");
                stb.append("         AND REGDH.GHR_CD = REGD.GHR_CD ");
                stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
            } else if (param._isFi) {
                stb.append("     SCHREG_REGD_FI_DAT REGD ");
                stb.append("     LEFT JOIN SCHREG_REGD_FI_HDAT REGDH ON REGDH.YEAR = REGD.YEAR ");
                stb.append("         AND REGDH.SEMESTER = REGD.SEMESTER ");
                stb.append("         AND REGDH.GRADE = REGD.GRADE ");
                stb.append("         AND REGDH.HR_CLASS = REGD.HR_CLASS ");
                stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
                stb.append("     LEFT JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = REGD.YEAR ");
                stb.append("         AND REGDG.GRADE = REGD.GRADE ");
            } else {
                stb.append("     SCHREG_REGD_DAT REGD ");
                stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR ");
                stb.append("         AND REGDH.SEMESTER = REGD.SEMESTER ");
                stb.append("         AND REGDH.GRADE = REGD.GRADE ");
                stb.append("         AND REGDH.HR_CLASS = REGD.HR_CLASS ");
                stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
                stb.append("     LEFT JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = REGD.YEAR ");
                stb.append("         AND REGDG.GRADE = REGD.GRADE ");
            }
            stb.append(" WHERE ");
            stb.append("     REGD.YEAR = '" + param._year + "' ");
            stb.append("     AND REGD.SEMESTER = '" + param._gakki + "' ");
            if (param._isGhr) {
                if ("2".equals(param._output)) {
                    stb.append("     AND REGD.GHR_CD IN " + SQLUtils.whereIn(true, param._category_selected) + " ");
                } else {
                    stb.append("     AND REGD.GHR_CD = '" + param._gradeHrClass + "' ");
                    stb.append("     AND REGD.SCHREGNO IN " + SQLUtils.whereIn(true, param._category_selected) + " ");
                }
                stb.append(" ORDER BY ");
                stb.append("     REGD.GHR_CD, REGD.GHR_ATTENDNO ");
            } else {
                if ("2".equals(param._output)) {
                    stb.append("     AND REGD.GRADE || REGD.HR_CLASS IN " + SQLUtils.whereIn(true, param._category_selected) + " ");
                } else {
                    stb.append("     AND REGD.GRADE || REGD.HR_CLASS = '" + param._gradeHrClass + "' ");
                    stb.append("     AND REGD.SCHREGNO IN " + SQLUtils.whereIn(true, param._category_selected) + " ");
                }
                stb.append(" ORDER BY ");
                stb.append("     REGD.GRADE, REGD.HR_CLASS, REGD.ATTENDNO ");
            }
            return stb.toString();
        }
    }

    static class MedexamDetNoDat {
        final String _year;
        final String _schregno;
        final String _grade;
        final String _no;
        final String _date;
        final String _height;
        final String _weight;
        final String _sitheight;
        final String _rBarevisionMark;
        final String _lBarevisionMark;
        final String _rVisionMark;
        final String _lVisionMark;

        MedexamDetNoDat(
            final String year,
            final String schregno,
            final String grade,
            final String no,
            final String date,
            final String height,
            final String weight,
            final String sitheight,
            final String rBarevisionMark,
            final String lBarevisionMark,
            final String rVisionMark,
            final String lVisionMark
        ) {
            _year = year;
            _schregno = schregno;
            _grade = grade;
            _no = no;
            _date = date;
            _height = height;
            _weight = weight;
            _sitheight = sitheight;
            _rBarevisionMark = rBarevisionMark;
            _lBarevisionMark = lBarevisionMark;
            _rVisionMark = rVisionMark;
            _lVisionMark = lVisionMark;
        }

        public static String getMedexamDetNoDatSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH ALL_SCHREGNO AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.SCHREGNO ");
            stb.append("     FROM ");
            if (param._isGhr) {
                stb.append("         SCHREG_REGD_GHR_DAT T1 ");
            } else if (param._isFi) {
                stb.append("         SCHREG_REGD_FI_DAT T1 ");
            } else {
                stb.append("         SCHREG_REGD_DAT T1 ");
            }
            stb.append("     WHERE ");
            stb.append("         T1.YEAR = '" + param._year + "' ");
            stb.append("         AND T1.SEMESTER = '" + param._gakki + "' ");
            if (param._isGhr) {
                if ("2".equals(param._output)) {
                    stb.append("         AND T1.GHR_CD IN " + SQLUtils.whereIn(true, param._category_selected) + " ");
                } else {
                    stb.append("         AND T1.GHR_CD = '" + param._gradeHrClass + "' ");
                    stb.append("         AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._category_selected) + " ");
                }
            } else {
                if ("2".equals(param._output)) {
                    stb.append("         AND T1.GRADE || T1.HR_CLASS IN " + SQLUtils.whereIn(true, param._category_selected) + " ");
                } else {
                    stb.append("         AND T1.GRADE || T1.HR_CLASS = '" + param._gradeHrClass + "' ");
                    stb.append("         AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._category_selected) + " ");
                }
            }
            if (!param._isGhr) {
                stb.append(" ), ALL_GRADE AS ( ");
                stb.append("     SELECT ");
                stb.append("         T1.YEAR, ");
                stb.append("         T1.SCHREGNO, ");
                stb.append("         MAX(T3.GRADE_CD) AS GRADE ");
                stb.append("     FROM ");
                stb.append("         SCHREG_REGD_DAT T1 ");
                stb.append("         INNER JOIN ALL_SCHREGNO T2 ON T2.SCHREGNO = T1.SCHREGNO ");

                stb.append("         INNER JOIN SCHREG_REGD_GDAT T3 ON T3.YEAR = T1.YEAR AND T3.GRADE = T1.GRADE ");
                if ("1".equals(param._useSchool_KindField) && !StringUtils.isBlank(param._SCHOOLKIND)) {
                    stb.append("         AND T3.SCHOOL_KIND = '" + param._SCHOOLKIND + "' ");
                }
                stb.append("     WHERE ");
                stb.append("         T1.YEAR <= '" + param._year + "' ");
                stb.append("     GROUP BY ");
                stb.append("         T1.YEAR, ");
                stb.append("         T1.SCHREGNO ");
            }
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.YEAR ");
            stb.append("     ,T1.SCHREGNO ");
            if (param._isGhr) {
                stb.append("     , CAST(NULL AS VARCHAR(1)) AS GRADE ");
            } else {
                stb.append("     ,T2.GRADE ");
            }
            stb.append("     ,T1.NO ");
            stb.append("     ,T1.DATE ");
            stb.append("     ,T1.HEIGHT ");
            stb.append("     ,T1.WEIGHT ");
            stb.append("     ,T1.SITHEIGHT ");
            stb.append("     ,T1.R_BAREVISION_MARK ");
            stb.append("     ,T1.L_BAREVISION_MARK ");
            stb.append("     ,T1.R_VISION_MARK ");
            stb.append("     ,T1.L_VISION_MARK ");
            stb.append(" FROM MEDEXAM_DET_NO_DAT T1 ");
            if (param._isGhr) {
                stb.append(" ORDER BY T1.SCHREGNO, T1.YEAR, T1.NO, T1.DATE ");
            } else {
                stb.append(" INNER JOIN ALL_GRADE T2 ON T2.YEAR = T1.YEAR ");
                stb.append("     AND T2.SCHREGNO = T1.SCHREGNO ");
                stb.append(" ORDER BY T1.SCHREGNO, T1.YEAR, T2.GRADE, T1.NO, T1.DATE ");
            }
            return stb.toString();
        }
    }

    static class HexamPhysicalAvgDat {
        final String _sex;
        final int _nenreiYear;
        final int _nenreiMonth;
        final double _nenrei;
        final BigDecimal _heightAvg;
        final BigDecimal _heightSd;
        final BigDecimal _weightAvg;
        final BigDecimal _weightSd;
        final BigDecimal _stdWeightKeisuA;
        final BigDecimal _stdWeightKeisuB;

        HexamPhysicalAvgDat(
            final String sex,
            final int nenreiYear,
            final int nenreiMonth,
            final BigDecimal heightAvg,
            final BigDecimal heightSd,
            final BigDecimal weightAvg,
            final BigDecimal weightSd,
            final BigDecimal stdWeightKeisuA,
            final BigDecimal stdWeightKeisuB
        ) {
            _sex = sex;
            _nenreiYear = nenreiYear;
            _nenreiMonth = nenreiMonth;
            _nenrei = _nenreiYear + (_nenreiMonth / 12.0);
            _heightAvg = heightAvg;
            _heightSd = heightSd;
            _weightAvg = weightAvg;
            _weightSd = weightSd;
            _stdWeightKeisuA = stdWeightKeisuA;
            _stdWeightKeisuB = stdWeightKeisuB;
        }

        public static Map getHexamPhysicalAvgMap(final DB2UDB db2, final Param param) {
            final Map m = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql(param));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String sex = rs.getString("SEX");
                    final int nenreiYear = rs.getInt("NENREI_YEAR");
                    final int nenreiMonth = rs.getInt("NENREI_MONTH");
                    // if (ageMonth % 3 != 0) { continue; }
                    final BigDecimal heightAvg = rs.getBigDecimal("HEIGHT_AVG");
                    final BigDecimal heightSd = rs.getBigDecimal("HEIGHT_SD");
                    final BigDecimal weightAvg = rs.getBigDecimal("WEIGHT_AVG");
                    final BigDecimal weightSd = rs.getBigDecimal("WEIGHT_SD");
                    final BigDecimal stdWeightKeisuA = rs.getBigDecimal("STD_WEIGHT_KEISU_A");
                    final BigDecimal stdWeightKeisuB = rs.getBigDecimal("STD_WEIGHT_KEISU_B");
                    final HexamPhysicalAvgDat testheightweight = new HexamPhysicalAvgDat(sex, nenreiYear, nenreiMonth, heightAvg, heightSd, weightAvg, weightSd, stdWeightKeisuA, stdWeightKeisuB);
                    if (null == m.get(rs.getString("SEX"))) {
                        m.put(rs.getString("SEX"), new ArrayList());
                    }
                    final List list = (List) m.get(rs.getString("SEX"));
                    list.add(testheightweight);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return m;
        }

        private static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" WITH MAX_YEAR AS ( ");
            stb.append("   SELECT ");
            stb.append("       MAX(YEAR) AS YEAR ");
            stb.append("   FROM ");
            stb.append("       HEXAM_PHYSICAL_AVG_DAT T1 ");
            stb.append("   WHERE ");
            stb.append("       T1.YEAR <= '" + param._year + "' ");
            stb.append(" ), MIN_YEAR AS ( ");
            stb.append("   SELECT ");
            stb.append("       MIN(YEAR) AS YEAR ");
            stb.append("   FROM ");
            stb.append("       HEXAM_PHYSICAL_AVG_DAT T1 ");
            stb.append("   WHERE ");
            stb.append("       T1.YEAR >= '" + param._year + "' ");
            stb.append(" ), MAX_MIN_YEAR AS ( ");
            stb.append("   SELECT ");
            stb.append("       MIN(T1.YEAR) AS YEAR ");
            stb.append("   FROM ( ");
            stb.append("       SELECT YEAR FROM MAX_YEAR T1 ");
            stb.append("       UNION ");
            stb.append("       SELECT YEAR FROM MIN_YEAR T1 ");
            stb.append("   ) T1 ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.SEX, ");
            stb.append("     T1.NENREI_YEAR, ");
            stb.append("     T1.NENREI_MONTH, ");
            stb.append("     T1.HEIGHT_AVG, ");
            stb.append("     T1.HEIGHT_SD, ");
            stb.append("     T1.WEIGHT_AVG, ");
            stb.append("     T1.WEIGHT_SD, ");
            stb.append("     T1.STD_WEIGHT_KEISU_A, ");
            stb.append("     T1.STD_WEIGHT_KEISU_B ");
            stb.append(" FROM ");
            stb.append("    HEXAM_PHYSICAL_AVG_DAT T1 ");
            stb.append("    INNER JOIN MAX_MIN_YEAR T2 ON T2.YEAR = T1.YEAR ");
            stb.append(" ORDER BY ");
            stb.append("     T1.SEX, T1.NENREI_YEAR, T1.NENREI_MONTH ");
            return stb.toString();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 77065 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    static class Param {
        final String _year;
        final String _gakki;
        final String _output;
        final String _gradeHrClass;
        final String[] _category_selected;
        final Map _sexNameMap;
        final Map _sexAbbv1Map;
        final String _docuemntroot;
        final String _imagepath;
        final File _image01;
        final File _image02;
        final File _image03;
        final File _image04;
        final File _image05;
        final File _image2_01;
        final File _imageWSP;
        final String _useSchool_KindField;
        final String _SCHOOLKIND;
        final String _useFormKNJF034_2;
        Map _physAvgMap = new HashMap();
        final Map _sessionMap = new HashMap();
        final String _schoolName1;
        final Map _certifSchool;
        boolean _isFi = false;
        boolean _isGhr = false;
        boolean _isDispHiman;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _gakki = request.getParameter("GAKKI");
            _output = request.getParameter("OUTPUT");
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            _category_selected = request.getParameterValues("category_selected");
            _docuemntroot = request.getParameter("DOCUMENTROOT");
            _imagepath = getImagepath(db2);
            _image01 = checkFile(new File(_docuemntroot + "/" + _imagepath + "KNJF034img01.png"));
            _image02 = checkFile(new File(_docuemntroot + "/" + _imagepath + "KNJF034img02.png"));
            _image03 = checkFile(new File(_docuemntroot + "/" + _imagepath + "KNJF034img03.png"));
            _image04 = checkFile(new File(_docuemntroot + "/" + _imagepath + "KNJF034img04.png"));
            _image05 = checkFile(new File(_docuemntroot + "/" + _imagepath + "KNJF034img05.png"));
            _image2_01 = checkFile(new File(_docuemntroot + "/" + _imagepath + "KNJF034_2img01.png"));
            _imageWSP = checkFile(new File(_docuemntroot + "/" + _imagepath + "whitespace.png"));
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLKIND = request.getParameter("SCHOOLKIND");
            _schoolName1 = getSchoolMstSchoolName1(db2);
            _certifSchool = setCertifSchool(db2);
            _sexNameMap = getSexNameMap(db2, "NAME1");
            _sexAbbv1Map = getSexNameMap(db2, "ABBV1");
            _useFormKNJF034_2 = request.getParameter("useFormKNJF034_2");
            _isDispHiman = !"1".equals(request.getParameter("knjf034HiddenHiman"));

            if ("1".equals(request.getParameter("useSpecial_Support_Hrclass"))) {
                _isGhr = true;
            } else if ("1".equals(request.getParameter("useFi_Hrclass"))) {
                _isFi = true;
            }
        }

        private File checkFile(File file) {
            if (!file.exists()) {
                log.warn(" file not exists: " + file.getAbsolutePath());
                return null;
            }
            return file;
        }

        public String getSchoolname(final String schoolKind) {
            String rtn = null;
            if ("H".equals(schoolKind)) {
                rtn = (String) _certifSchool.get("SCHOOL_NAME");
            } else if ("J".equals(schoolKind)) {
                rtn = (String) _certifSchool.get("REMARK1");
            } else if ("P".equals(schoolKind) || "K".equals(schoolKind)) {
                rtn = (String) _certifSchool.get("REMARK4");
            }
            if (null == rtn) {
                rtn = _schoolName1;
            }
            return rtn;
        }

        public Map setCertifSchool(final DB2UDB db2) {
            final Map _certifSchool = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT * ");
                stb.append(" FROM CERTIF_SCHOOL_DAT T1 ");
                stb.append(" WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '125' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    _certifSchool.put("SCHOOL_NAME", rs.getString("SCHOOL_NAME"));
                    _certifSchool.put("REMARK1", rs.getString("REMARK1"));
                    _certifSchool.put("REMARK4", rs.getString("REMARK4"));
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return _certifSchool;
        }

        private String getImagepath(final DB2UDB db2) {
            final String sql = " SELECT IMAGEPATH FROM CONTROL_MST WHERE CTRL_NO = '01' ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            String rtn = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("IMAGEPATH");
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return (null == rtn) ? rtn : (rtn + "/");
        }

        private String getSchoolMstSchoolName1(final DB2UDB db2) {
            String sql = " SELECT SCHOOLNAME1 FROM SCHOOL_MST WHERE YEAR ='" + _year + "' ";
            if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND)) {
                sql += "   AND SCHOOL_KIND = '" + _SCHOOLKIND + "' ";
            }

            PreparedStatement ps = null;
            ResultSet rs = null;
            String rtn = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("SCHOOLNAME1");
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private Map getSexNameMap(final DB2UDB db2, final String field) {
            final String sql = " SELECT NAMECD2, " + field + " FROM NAME_MST WHERE NAMECD1 ='Z002' ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map m = new HashMap();
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    m.put(rs.getString("NAMECD2"), rs.getString(field));
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return m;
        }
    }
}

// eof

