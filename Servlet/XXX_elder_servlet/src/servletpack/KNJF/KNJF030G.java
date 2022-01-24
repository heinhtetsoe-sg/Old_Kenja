/*
 * $Id: 1d89d77a108152dfb00d1c85c730c49603ec9bc1 $
 *
 * 作成日: 2018/04/03
 * 作成者: tawada
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJF;

import java.io.File;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJF.detail.MedexamDetDat;
import servletpack.KNJF.detail.MedexamToothDat;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KNJ_Schoolinfo;
import servletpack.KNJZ.detail.SvfField;

/**
 *
 *  学校教育システム 賢者 [保健管理]
 *
 *                  ＜ＫＮＪＦ０３０Ｇ＞  埼玉栄　各種健康診断印刷
 */
public class KNJF030G {

    private static final Log log = LogFactory.getLog(KNJF030G.class);

    private static final String SCHOOL_NAME1      = "SCHOOL_NAME1";
    private static final String SCHOOL_NAME2      = "SCHOOL_NAME2";
    private static final String PRINCIPAL_NAME    = "PRINCIPAL_NAME";
    private static final String PRINCIPAL_JOBNAME = "PRINCIPAL_JOBNAME";

    private static final String IPPAN_1 = "1";
    private static final String IPPAN_2 = "2";
    private static final String HaKouku = "3";

    private static final String NAME1 = "NAME1";
    private Param _param;
    private static String printKenkouSindanIppan = "1";
    private boolean _hasdata;

    /**
     * HTTP Get リクエストの処理
     */
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        final Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス

        //  print設定
        final PrintWriter outstrm = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");

        //  svf設定
        svf.VrInit();                           //クラスの初期化
        svf.VrSetSpoolFileStream(response.getOutputStream());           //PDFファイル名の設定

        log.fatal("$Revision: 68444 $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);

        //  ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch (Exception ex) {
            log.error("DB2 open error!", ex);
            return;
        }

        //  ＳＶＦ作成処理
        try {
            _param = new Param(db2, request);

            printMain(request, svf, db2);
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {

            //  該当データ無し
            if (!_hasdata) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
            //  終了処理
            svf.VrQuit();

            db2.commit();
            db2.close();                //DBを閉じる
            outstrm.close();            //ストリームを閉じる
        }

    }//doGetの括り

    private void printMain(final HttpServletRequest request, final Vrw32alp svf, DB2UDB db2) throws SQLException {
        final List studentList = Student.getStudentList(db2, _param);

        log.info(" check = " + _param._checkList);
        log.info(" studentList size = " + studentList.size());

        //SVF出力
        for (int i = 0; i < _param._checkList.size(); i++) {
            final int n = ((Integer) _param._checkList.get(i)).intValue();
            final String documentCd = 1 == KNJ_EditEdit.getMS932ByteLength(String.valueOf(n)) ? "0"+String.valueOf(n): String.valueOf(n);
            if (n == 1) {
                _hasdata = new KNJF030A().printMain(svf, db2, request, _param, studentList, IPPAN_1) || _hasdata;//健康診断票（一般）
            } else if (n == 2)  {
                _hasdata = new KNJF030A().printMain(svf, db2, request, _param, studentList, IPPAN_2) || _hasdata;//健康診断票（一般）写し
            } else if (n == 3)  { print3(db2, svf, studentList);                //健康診断票（歯・口腔）
            //中学
            } else if (n == 4)  { print4(db2, svf, studentList, documentCd);    //健康診断結果のお知らせ
            } else if (n == 5)  { print5(svf, studentList, documentCd);         //受診報告書
            } else if (n == 6)  { print6(svf, studentList, documentCd);         //検診のおすすめ票（眼科）
            } else if (n == 7)  { print7(svf, studentList, documentCd);         //検診のおすすめ票（歯科）
            } else if (n == 8)  { print8(svf, studentList, documentCd);         //検診のおすすめ票（耳鼻科）
            } else if (n == 9)  { print9(db2, svf, studentList, documentCd);    //検診のおすすめ票（心臓）
            } else if (n == 10) { print10(svf, studentList, documentCd);        //検診のおすすめ票（内科）
            } else if (n == 11) { print11(svf, studentList, documentCd);        //検診のおすすめ票（尿）
            } else if (n == 12) { print12(svf, studentList, documentCd);        //心臓検診結果のお知らせ
            } else if (n == 13) { print13(svf, studentList, documentCd);        //腎臓検診第2次（尿検査）のお知らせ
            } else if (n == 14) { print14(svf, studentList, documentCd);        //尿検査の結果について
            } else if (n == 15) { print15(svf, studentList, documentCd);        //治療継続証明書
            //高校
            } else if (n == 16) { print4(db2, svf, studentList, documentCd);    //健康診断結果のお知らせ
            } else if (n == 17) { print5(svf, studentList, documentCd);         //受診報告書
            } else if (n == 18) { print6(svf, studentList, documentCd);         //検診のおすすめ票（眼科）
            } else if (n == 19) { print7(svf, studentList, documentCd);         //検診のおすすめ票（歯科）
            } else if (n == 20) { print8(svf, studentList, documentCd);         //検診のおすすめ票（耳鼻科）
            } else if (n == 21) { print9(db2, svf, studentList, documentCd);    //検診のおすすめ票（心臓）
            } else if (n == 22) { print10(svf, studentList, documentCd);        //検診のおすすめ票（内科）
            } else if (n == 23) { print11(svf, studentList, documentCd);        //検診のおすすめ票（尿）
            } else if (n == 24) { print24(svf, studentList, documentCd);        //検診のおすすめ票（結核）
            } else if (n == 25) { print12(svf, studentList, documentCd);        //心臓検診結果のお知らせ
            } else if (n == 26) { print13(svf, studentList, documentCd);        //腎臓検診第2次（尿検査）のお知らせ
            } else if (n == 27) { print14(svf, studentList, documentCd);        //尿検査の結果について
            } else if (n == 28) { print28(svf, studentList, documentCd);        //結核検診の結果について
            } else if (n == 29) { print15(svf, studentList, documentCd);        //治療継続証明書
            }
        }
    }

    private static String getString(final Map map, final String field) {
        if (null == field || null == map || map.isEmpty()) {
            return null;
        }
        try {
            if (!map.containsKey(field)) {
                // フィールド名が間違い
                throw new RuntimeException("指定されたフィールドのデータがありません。フィールド名：'" + field + "' :" + map);
            }
        } catch (final Exception e) {
            log.error("exception!", e);
        }
        final String rtn = (String) map.get(field);
//        log.info(" field = " + field + " / rtn = " + rtn);
        return rtn;
    }

    private static class Util {

        private static String sishagonyu(final String s) {
            if (NumberUtils.isNumber(s)) {
                return new BigDecimal(s).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
            }
            return null;
        }

        public static String[] splitByLength(final String data, int count) {
            if (null == data || data.length() == 0) {
                return new String[] {};
            }
            if (data.length() > count) {
                return new String[] {data.substring(0, count), data.substring(count)};
            }
            return new String[] {data};
        }

        private static void svfVrsOutArray(final Vrw32alp svf, final String[] field, final String[] tokens) {
            if (null != tokens) {
                svfVrsOutArrayList(svf, field, Arrays.asList(tokens));
            }
        }

        private static void svfVrsOutArrayList(final Vrw32alp svf, final String[] field, final List tokens) {
            for (int i = 0; i < Math.min(field.length,  tokens.size()); i++) {
                svf.VrsOut(field[i], (String) tokens.get(i));
            }
        }

        private static int getMS932ByteLength(final String str) {
        	return KNJ_EditEdit.getMS932ByteLength(str);
        }

        /**
         * 文字列をMS932にエンコードして指定分割バイト数で分割する
         * @param str 文字列
         * @param splitByte MS932にエンコードした場合の分割バイト数
         * @return 分割された文字列
         */
        private static String[] getMS932ByteToken(final String str, final int[] splitByte) {
            if (null == str) {
                return new String[] {};
            }
            final List tokenList = new ArrayList();
            StringBuffer token = new StringBuffer();
            int splitByteIdx = 0;
            int currentSplitByte = splitByte.length - 1 < splitByteIdx ? 99999999 : splitByte[splitByteIdx];
            for (int i = 0; i < str.length(); i++) {
                final String ch = String.valueOf(str.charAt(i));
                if (getMS932ByteLength(token.toString() + ch) > currentSplitByte) {
                    tokenList.add(token.toString());
                    token = new StringBuffer();
                    splitByteIdx += 1;
                    currentSplitByte = splitByte.length - 1 < splitByteIdx ? 99999999 : splitByte[splitByteIdx];
                }
                token.append(String.valueOf(ch));
            }
            if (token.length() != 0) {
                tokenList.add(token.toString());
            }
            final String[] array = new String[tokenList.size()];
            for (int i = 0; i < tokenList.size(); i++) {
                array[i] = (String) tokenList.get(i);
            }
            return array;
        }

        private static Map getMappedMap(final Map map, final Object key1) {
            if (!map.containsKey(key1)) {
                map.put(key1, new HashMap());
            }
            return (Map) map.get(key1);
        }

        private static List getMappedList(final Map map, final Object key1) {
            if (!map.containsKey(key1)) {
                map.put(key1, new ArrayList());
            }
            return (List) map.get(key1);
        }

        private static String mkString(final String[] array, final String comma) {
            final StringBuffer stb = new StringBuffer();
            String c = "";
            for (int i = 0; i < array.length; i++) {
                if (!StringUtils.isEmpty(array[i])) {
                    stb.append(c).append(array[i]);
                    c = comma;
                }
            }
            return stb.toString();
        }

        // 肥満度計算
        //  肥満度（過体重度）= 100 × (測定された体重 - 標準体重) / 標準体重
        public static String calcHimando(final Student student, final DB2UDB db2, final Param param) {
            if (null == param._physAvgMap) {
                param._physAvgMap = HexamPhysicalAvgDat.getHexamPhysicalAvgMap(db2, param);
            }
            final String weight = student._medexamDetDat._weight;
            if (null == weight) {
                log.debug(" " + student._schregno + ", " + param._year + " 体重がnull");
                return null;
            }
            BigDecimal weightAvg = null;
            final boolean isUseMethod2 = true;
            if (isUseMethod2) {
                // final BigDecimal weightAvg1 = getWeightAvgMethod1(student, mdnd, param);
                final BigDecimal weightAvg2 = getWeightAvgMethod2(student, param._physAvgMap, param);
                // log.fatal(" (schregno, attendno, weight1, weight2) = (" + rs.getString("SCHREGNO") + ", " + rs.getString("ATTENDNO") + ", " + weightAvg1 + ", " + weightAvg2 + ")");
                log.fatal(" (schregno, attendno, weight2) = (" + student._schregno + ", " + student._attendno + ", " + weightAvg2 + ")");
                weightAvg = weightAvg2;
            } else {
                // weightAvg = null; getWeightAvgMethod0(student, mdnd, physAvgMap);
            }
            if (null == weightAvg) {
                return null;
            }
            final BigDecimal himando = new BigDecimal(100).multiply(new BigDecimal(Double.parseDouble(weight)).subtract(weightAvg)).divide(weightAvg, 1, BigDecimal.ROUND_HALF_UP);
            log.fatal(" himando = 100 * (" + weight + " - " + weightAvg + ") / " + weightAvg + " = " + himando);
            return himando.toString();
        }

        private static BigDecimal getWeightAvgMethod2(final Student student, final Map physAvgMap, final Param param) {
            final String schregno = student._schregno;
            if (null == student._medexamDetDat._height) {
                log.debug(" " + schregno + ", " + param._year + " 身長がnull");
                return null;
            }
//            if (null == student._birthDay) {
//                log.debug(" " + schregno + ", " + param._year + " 生年月日がnull");
//                return null;
//            }
            // 日本小児内分泌学会 (http://jspe.umin.jp/)
            // http://jspe.umin.jp/ipp_taikaku.htm ２．肥満度 ２）性別・年齢別・身長別標準体重（５歳以降）のデータによる
            // ａ＝ HEXAM_PHYSICAL_AVG_DAT.STD_WEIGHT_KEISU_A
            // ｂ＝ HEXAM_PHYSICAL_AVG_DAT.STD_WEIGHT_KEISU_B　
            // 標準体重＝ａ×身長（cm）- ｂ 　 　
            final BigDecimal height = new BigDecimal(student._medexamDetDat._height);
            final String kihonDate = param._year + "-04-01";
            final int iNenrei = (int) getNenrei(student, kihonDate, param._year, param._year);
//            final int iNenrei = (int) getNenrei2(rs, param._year, param._year);
            final HexamPhysicalAvgDat hpad = getPhysicalAvgDatNenrei(iNenrei, (List) physAvgMap.get(student._sexCd));
            if (null == hpad || null == hpad._stdWeightKeisuA || null == hpad._stdWeightKeisuB) {
                return null;
            }
            final BigDecimal a = hpad._stdWeightKeisuA;
            final BigDecimal b = hpad._stdWeightKeisuB;
            final BigDecimal avgWeight = a.multiply(height).subtract(b);
            log.fatal(" method2 avgWeight = " + a + " * " + height + " - " + b + " = " + avgWeight);
            return avgWeight;
        }

        // 学年から年齢を計算する
        private static double getNenrei2(final Student student, final String year1, final String year2) throws NumberFormatException {
            return 15.0 + Integer.parseInt(student._grade) - (StringUtils.isNumeric(year1) && StringUtils.isNumeric(year2) ? Integer.parseInt(year1) - Integer.parseInt(year2) : 0);
        }

        // 生年月日と対象日付から年齢を計算する
        private static double getNenrei(final Student student, final String date, final String year1, final String year2) throws NumberFormatException {
            if (null == student._birthDay) {
                return getNenrei2(student, year1, year2);
            }
            final Calendar calBirthDate = Calendar.getInstance();
            calBirthDate.setTime(Date.valueOf(student._birthDay));
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

        // 年齢の平均データを得る
        private static HexamPhysicalAvgDat getPhysicalAvgDatNenrei(final int nenrei, final List physAvgList) {
            HexamPhysicalAvgDat tgt = null;
            boolean findflg = false;
            for (final Iterator it = physAvgList.iterator(); it.hasNext();) {
                final HexamPhysicalAvgDat hpad = (HexamPhysicalAvgDat) it.next();
                if (hpad._nenrei <= nenrei) {
                    tgt = hpad;
                    if (hpad._nenreiYear == nenrei) {
                        findflg = true;
                        break;
                    }
                }
            }
            if (!findflg) {
                return null;
            }
            return tgt;
        }

        public static String month(final String date) {
            if (null != date) {
                try {
                    final Calendar cal = Calendar.getInstance();
                    cal.setTime(Date.valueOf(date));
                    return String.valueOf(cal.get(Calendar.MONTH) + 1);
                } catch (Exception e) {
                }
            }
            return null;
        }

        public static String dayOfMonth(final String date) {
            if (null != date) {
                try {
                    final Calendar cal = Calendar.getInstance();
                    cal.setTime(Date.valueOf(date));
                    return String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
                } catch (Exception e) {
                }
            }
            return null;
        }
    }

    private void svfVrsOutTooth(final Vrw32alp svf, final String field, final String code) {
        if (code == null) {
            return;
        }

        String mark = null;
        if("01".equals(code)) {       mark = "-"; }  // 現在歯
        else if ("02".equals(code)) { mark = "Ｃ"; } // 未処置歯
        else if ("03".equals(code)) { mark = "○"; } // 処置歯
        else if ("04".equals(code)) { mark = "△"; } // 喪失歯（永久歯）
        else if ("05".equals(code)) { mark = "×"; } // 要注意歯
        else if ("06".equals(code)) { mark = "C0"; } // 要観察歯
        else if ("07".equals(code)) { mark = "CS"; } // 要精検歯

        svf.VrsOut(field, mark);
        if ("04".equals(code)) {
            return;
        }
        svf.VrsOut("NOW_"+field, "／");
    }

    // ３）健康診断票（歯・口腔）
    private void print3(final DB2UDB db2, final Vrw32alp svf, final List studentList) throws SQLException {
        final int maxLine = 5;

        final String form = "KNJF030G_3.frm";
        log.info(" form = " + form);

        for (int sti = 0; sti < studentList.size(); sti++) {
            final Student student = (Student) studentList.get(sti);

            if (null == student._medexamToothDat) {
                continue;
            }

            svf.VrSetForm(form, 4);

            final String[] printSchoolKind = {"J", "H"};

            for (int ki = 0; ki < printSchoolKind.length; ki++) {
                final String schoolKind = printSchoolKind[ki];
                if (!schoolKind.equals(student._schoolKind)) continue;

                svf.VrsOut("SCHREGNO", student._schregno); // 学籍番号（マスク）
                svf.VrsOut("NAME_SHOW" + (Util.getMS932ByteLength(student._name) > 24 ? "_2" : ""), student._name); // 氏名
                if ("1".equals(_param._defineSchool.schooldiv)) {
                    svf.VrsOut("GRADENAME_TITLE", "年度");
                } else {
                    svf.VrsOut("GRADENAME_TITLE", "学年");
                }
                svf.VrsOut("NAME_HEADER", "名前"); // 名前ヘッダ
                svf.VrsOut("SEX", student._sex); // 性別
                svf.VrsOut("BIRTHDAY", KNJ_EditDate.h_format_JP(student._birthDay)); // 生年月日

                final List regdList = RegdDat.getRegdList(db2, _param, student);
                for (int i = 0; i < Math.min(maxLine, regdList.size()); i++) {
                    final int line = i + 1;
                    final RegdDat regd = (RegdDat) regdList.get(i);
                    if ("1".equals(_param._defineSchool.schooldiv)) {
                        if (NumberUtils.isDigits(regd._year)) {
                            svf.VrsOutn("GRADE_2", line, KenjaProperties.gengou(Integer.parseInt(regd._year)) + "年度");
                        }
                    } else {
                        svf.VrsOutn("GRADE", line, regd._gradeName);
                    }
                    svf.VrsOutn(Util.getMS932ByteLength(regd._hrClassName) > 8 ? "HR_NAME2_1" : "HR_NAME1", line, regd._hrClassName); // クラス
                    svf.VrsOutn("ATTENDNO", line, regd._attendNo); // 出席番号
                }

                for (int i = 0; i < Math.min(maxLine, regdList.size()); i++) {

                    final RegdDat regd = (RegdDat) regdList.get(i);

                    svf.VrsOut("NENDO1", KenjaProperties.gengou(Integer.parseInt(regd._year)).substring(0,2)); // 年度
                    svf.VrsOut("NENDO2", KenjaProperties.gengou(Integer.parseInt(regd._year)).substring(2)); // 年度
                    svf.VrsOut("AGE", regd._age); // 年齢

                    if (null != regd._medexamToothDat) {

                        final MedexamToothDat mtd = regd._medexamToothDat;

                        if ("01".equals(mtd._jawsJointcd)) {
                            svf.VrsOut("JAWSCD0", "○"); // 歯列
                        } else if ("02".equals(mtd._jawsJointcd)) {
                            svf.VrsOut("JAWSCD1", "○"); // 歯列
                        } else if ("03".equals(mtd._jawsJointcd)) {
                            svf.VrsOut("JAWSCD2", "○"); // 歯列
                        }

                        if ("01".equals(mtd._jawsJointcd3)) {
                            svf.VrsOut("JOINTCD0", "○"); // 咬合
                        } else if ("02".equals(mtd._jawsJointcd3)) {
                            svf.VrsOut("JOINTCD1", "○"); // 咬合
                        } else if ("03".equals(mtd._jawsJointcd3)) {
                            svf.VrsOut("JOINTCD2", "○"); // 咬合
                        }

                        if ("01".equals(mtd._jawsJointcd2)) {
                            svf.VrsOut("JAWS_JOINTCD20", "○"); // 歯列・咬合
                        } else if ("02".equals(mtd._jawsJointcd2)) {
                            svf.VrsOut("JAWS_JOINTCD21", "○"); // 歯列・咬合
                        } else if ("03".equals(mtd._jawsJointcd2)) {
                            svf.VrsOut("JAWS_JOINTCD22", "○"); // 歯列・咬合
                        }

                        if ("01".equals(mtd._plaquecd)) {
                            svf.VrsOut("PLAQUECD0", "○"); // 歯垢の状態
                        } else if ("02".equals(mtd._plaquecd)) {
                            svf.VrsOut("PLAQUECD1", "○"); // 歯垢の状態
                        } else if ("03".equals(mtd._plaquecd)) {
                            svf.VrsOut("PLAQUECD2", "○"); // 歯垢の状態
                        }

                        if ("01".equals(mtd._gumcd)) {
                            svf.VrsOut("GUMCD0", "○"); // 歯肉の状態
                        } else if ("02".equals(mtd._gumcd)) {
                            svf.VrsOut("GUMCD1", "○"); // 歯肉の状態
                        } else if ("03".equals(mtd._gumcd)) {
                            svf.VrsOut("GUMCD2", "○"); // 歯肉の状態
                        }

                        // 永久歯上
                        svfVrsOutTooth(svf, "UP_L_ADULT8", mtd._upLAdult8);
                        svfVrsOutTooth(svf, "UP_L_ADULT7", mtd._upLAdult7);
                        svfVrsOutTooth(svf, "UP_L_ADULT6", mtd._upLAdult6);
                        svfVrsOutTooth(svf, "UP_L_ADULT5", mtd._upLAdult5);
                        svfVrsOutTooth(svf, "UP_L_ADULT4", mtd._upLAdult4);
                        svfVrsOutTooth(svf, "UP_L_ADULT3", mtd._upLAdult3);
                        svfVrsOutTooth(svf, "UP_L_ADULT2", mtd._upLAdult2);
                        svfVrsOutTooth(svf, "UP_L_ADULT1", mtd._upLAdult1);
                        svfVrsOutTooth(svf, "UP_R_ADULT1", mtd._upRAdult1);
                        svfVrsOutTooth(svf, "UP_R_ADULT2", mtd._upRAdult2);
                        svfVrsOutTooth(svf, "UP_R_ADULT3", mtd._upRAdult3);
                        svfVrsOutTooth(svf, "UP_R_ADULT4", mtd._upRAdult4);
                        svfVrsOutTooth(svf, "UP_R_ADULT5", mtd._upRAdult5);
                        svfVrsOutTooth(svf, "UP_R_ADULT6", mtd._upRAdult6);
                        svfVrsOutTooth(svf, "UP_R_ADULT7", mtd._upRAdult7);
                        svfVrsOutTooth(svf, "UP_R_ADULT8", mtd._upRAdult8);
                        // 乳歯上
                        svfVrsOutTooth(svf, "UP_L_BABY5", mtd._upLBaby5);
                        svfVrsOutTooth(svf, "UP_L_BABY4", mtd._upLBaby4);
                        svfVrsOutTooth(svf, "UP_L_BABY3", mtd._upLBaby3);
                        svfVrsOutTooth(svf, "UP_L_BABY2", mtd._upLBaby2);
                        svfVrsOutTooth(svf, "UP_L_BABY1", mtd._upLBaby1);
                        svfVrsOutTooth(svf, "UP_R_BABY1", mtd._upRBaby1);
                        svfVrsOutTooth(svf, "UP_R_BABY2", mtd._upRBaby2);
                        svfVrsOutTooth(svf, "UP_R_BABY3", mtd._upRBaby3);
                        svfVrsOutTooth(svf, "UP_R_BABY4", mtd._upRBaby4);
                        svfVrsOutTooth(svf, "UP_R_BABY5", mtd._upRBaby5);
                        // 乳歯下
                        svfVrsOutTooth(svf, "LW_L_BABY5", mtd._lwLBaby5);
                        svfVrsOutTooth(svf, "LW_L_BABY4", mtd._lwLBaby4);
                        svfVrsOutTooth(svf, "LW_L_BABY3", mtd._lwLBaby3);
                        svfVrsOutTooth(svf, "LW_L_BABY2", mtd._lwLBaby2);
                        svfVrsOutTooth(svf, "LW_L_BABY1", mtd._lwLBaby1);
                        svfVrsOutTooth(svf, "LW_R_BABY1", mtd._lwRBaby1);
                        svfVrsOutTooth(svf, "LW_R_BABY2", mtd._lwRBaby2);
                        svfVrsOutTooth(svf, "LW_R_BABY3", mtd._lwRBaby3);
                        svfVrsOutTooth(svf, "LW_R_BABY4", mtd._lwRBaby4);
                        svfVrsOutTooth(svf, "LW_R_BABY5", mtd._lwRBaby5);
                        // 永久歯下
                        svfVrsOutTooth(svf, "LW_L_ADULT8", mtd._lwLAdult8);
                        svfVrsOutTooth(svf, "LW_L_ADULT7", mtd._lwLAdult7);
                        svfVrsOutTooth(svf, "LW_L_ADULT6", mtd._lwLAdult6);
                        svfVrsOutTooth(svf, "LW_L_ADULT5", mtd._lwLAdult5);
                        svfVrsOutTooth(svf, "LW_L_ADULT4", mtd._lwLAdult4);
                        svfVrsOutTooth(svf, "LW_L_ADULT3", mtd._lwLAdult3);
                        svfVrsOutTooth(svf, "LW_L_ADULT2", mtd._lwLAdult2);
                        svfVrsOutTooth(svf, "LW_L_ADULT1", mtd._lwLAdult1);
                        svfVrsOutTooth(svf, "LW_R_ADULT1", mtd._lwRAdult1);
                        svfVrsOutTooth(svf, "LW_R_ADULT2", mtd._lwRAdult2);
                        svfVrsOutTooth(svf, "LW_R_ADULT3", mtd._lwRAdult3);
                        svfVrsOutTooth(svf, "LW_R_ADULT4", mtd._lwRAdult4);
                        svfVrsOutTooth(svf, "LW_R_ADULT5", mtd._lwRAdult5);
                        svfVrsOutTooth(svf, "LW_R_ADULT6", mtd._lwRAdult6);
                        svfVrsOutTooth(svf, "LW_R_ADULT7", mtd._lwRAdult7);
                        svfVrsOutTooth(svf, "LW_R_ADULT8", mtd._lwRAdult8);

                        svf.VrsOut("BABYTOOTH"          , mtd._babytooth); // 乳歯・現在歯数
                        svf.VrsOut("REMAINBABYTOOTH"    , mtd._remainbabytooth); // 乳歯・未処置数
                        svf.VrsOut("TREATEDBABYTOOTH"   , mtd._treatedbabytooth); // 乳歯・処置数
                        svf.VrsOut("ADULTTOOTH"         , mtd._adulttooth); // 永久歯・現在歯数
                        svf.VrsOut("REMAINADULTTOOTH"   , mtd._remainadulttooth); // 永久歯・未処置数
                        svf.VrsOut("TREATEDADULTTOOTH"  , mtd._treatedadulttooth); // 永久歯・処置数
                        svf.VrsOut("LOSTADULTTOOTH"     , mtd._lostadulttooth); // 永久歯・喪失歯数
                        final String toothotherdisease = _param.getNameMstName1("F530", mtd._otherdiseasecd);
                        svf.VrsOut(Util.getMS932ByteLength(toothotherdisease) > 10 ? "TOOTHOTHERDISEASE2" : "TOOTHOTHERDISEASE", toothotherdisease); // その他の疾病及び異常
                        final String[] split = Util.splitByLength(_param.getNameMstName1("F540", mtd._dentistremarkcd), 10);
                        final String[] dentistremarkField = {"DENTISTREMARK", "rDENTISTREMARK"};
                        for (int di = 0; di < Math.min(split.length, dentistremarkField.length); di++) {
                            svf.VrsOut(dentistremarkField[di], split[di]); // 事後措置
                        }
                        svf.VrsOut("month", Util.month(mtd._dentistremarkdate)); // 学校歯科医・月
                        svf.VrsOut("day", Util.dayOfMonth(mtd._dentistremarkdate)); // 学校歯科医・日
                        svf.VrsOut("DENTISTTREAT", _param.getNameMstName1("F541", mtd._dentisttreatcd)); // 学校歯科医所見
                        // 印
                        if (null != mtd._date && null != _param._doctorPrint) {
                            svf.VrsOut("STAFFBTMC", _param.getStampImageFile(regd._year, HaKouku));
                        }

                    }

                    svf.VrEndRecord();
                    _hasdata = true;
                }
            }
        }
    }

    private void printDocumentMstTitleText(final Vrw32alp svf, final String title, final List textList, final String certifNo) {
        svf.VrsOut("CERT_NO", certifNo); //発行番号
        svf.VrsOut("TITLE", title);
        for (int i = 0; i < textList.size(); i++) {
            svf.VrsOut("TEXT" + String.valueOf(i + 1), (String) textList.get(i));
        }
    }

    /**
     * ヘッダーセット
     */
    private void printHeaderData(final Vrw32alp svf, final Student student, final String documentCd) {
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._date)); //作成日付

        if ("15".equals(documentCd) || "29".equals(documentCd)) {
            svf.VrsOut("NAME1_1", "保 護 者　様"); //保護者
        } else {
            final String setHrName    = student._majorname+" "+student._hrName+student._attendno+"番";
            final String hrNameField = KNJ_EditEdit.getMS932ByteLength(setHrName) > 30 ? "2": "1";
            svf.VrsOut("HR_NAME1_" + hrNameField, setHrName); //学科　年組番

            final String setName   = ("15".equals(documentCd)) ? "保護者 様": student._name+" 保護者 様";
            final String nameField = KNJ_EditEdit.getMS932ByteLength(setName) <= 30 ? "_1": KNJ_EditEdit.getMS932ByteLength(setName) <= 38 ? "_2": "_3";
            svf.VrsOut("NAME1" + nameField, setName); //生徒名＋"保護者 様"
        }

        svf.VrsOut("SCHOOL_NAME1_1", _param.getSchoolInfo(student._schoolKind, SCHOOL_NAME1));             //学校名１
        svf.VrsOut("SCHOOL_NAME1_2", _param.getSchoolInfo(student._schoolKind, SCHOOL_NAME2));             //学校名２
        final String setStaffName = _param.getSchoolInfo(student._schoolKind, PRINCIPAL_JOBNAME) +_param.getSchoolInfo(student._schoolKind, PRINCIPAL_NAME);
        final String sNameField   = KNJ_EditEdit.getMS932ByteLength(setStaffName) <= 26 ? "1": KNJ_EditEdit.getMS932ByteLength(setStaffName) <= 30 ? "2": "3";
        svf.VrsOut("STAFF_NAME" + sNameField, setStaffName); //職名+校長名
    }

    /**
     * フッターセット
     */
    private void printFooterData(final Vrw32alp svf, final Student student, final String title, final String documentCd) {
        if ("15".equals(documentCd) || "29".equals(documentCd)) {
            svf.VrsOut("CERT_NAME", "治 療 継 続 証 明 書"); //フッタータイトル
        } else {
            svf.VrsOut("CERT_NAME", "診 断 証 明 書"+title); //フッタータイトル
        }

        final String setHrName    = student._majorname+" "+student._hrName+student._attendno+"番";
        final String hrNameField = KNJ_EditEdit.getMS932ByteLength(setHrName) > 30 ? "2": "1";
        svf.VrsOut("HR_NAME2_" + hrNameField, setHrName); //学科　年組番

        final String setName   = student._name;
        final String nameField = KNJ_EditEdit.getMS932ByteLength(setName) <= 24 ? "1": KNJ_EditEdit.getMS932ByteLength(setName) <= 30 ? "2": KNJ_EditEdit.getMS932ByteLength(setName) <= 36 ? "3": "4";
        svf.VrsOut("NAME2_" + nameField, setName); //生徒名

        svf.VrsOut("SCHOOL_NAME2", _param.getSchoolInfo(student._schoolKind, SCHOOL_NAME2)+ " 様");             //学校名
        svf.VrsOut("ERA_NAME", KNJ_EditDate.h_format_JP_N(_param._ctrlDate).substring(0, 2)); //元号
        svf.VrsOut("STAMP_NAME", "印");        //印
    }

    private void printTextData(final Vrw32alp svf, final List textList, final String fieldName) {
        if (null == textList) return;
        for (int i = 0; i < textList.size(); i++) {
            svf.VrsOut(fieldName + String.valueOf(i + 1), (String) textList.get(i));
        }
    }

    private boolean isNotPrint(final String div, final MedexamDetDat mdd, final MedexamToothDat mtd) {
        if (ArrayUtils.contains(new String[] {"13", "14", "26", "27", "28"}, div)) {
            // データ無しを表示しない
            if (null == mdd) {
                return true;
            }
        }
//        if (debug) { return false; }

        // 検診のおすすめ票（眼科）
        if ("06".equals(div) || "18".equals(div)) {
            // 未受診者を印刷
            if (null == mdd
                || null == mdd._rBarevisionMark
                && null == mdd._rVisionMark
                && null == mdd._lBarevisionMark
                && null == mdd._lVisionMark
                && (null == mdd._eyediseasecd || "00".equals(mdd._eyediseasecd))) {
                return false;
            } else {
                return true;
            }

         // 検診のおすすめ票（歯科）
        } else if ("07".equals(div) || "19".equals(div)) {
            // 未受験者を印刷
            if (null == mtd
                || (null == mtd._jawsJointcd  || "00".equals(mtd._jawsJointcd))
                && (null == mtd._jawsJointcd2 || "00".equals(mtd._jawsJointcd2))
                && (null == mtd._plaquecd     || "00".equals(mtd._plaquecd))
                && (null == mtd._gumcd        || "00".equals(mtd._gumcd))
                && (null == mtd._remainadulttooth)
                && (null == mtd._brackAdulttooth)
                && (null == mtd._brackBabytooth)) {
                return false;
            } else {
                return true;
            }

         // 検診のおすすめ票（耳鼻科）
        } else if ("08".equals(div) || "20".equals(div)) {
            // 未受験者を印刷
            if (null == mdd
                || (null == mdd._nosediseasecd || "00".equals(mdd._nosediseasecd))
                && null == mdd._rEarDb
                && null == mdd._lEarDb) {
                return false;
            } else {
                return true;
            }

         // 検診のおすすめ票（心臓）
        } else if ("09".equals(div) || "21".equals(div)) {
            // 未受験者を印刷
            if (null == mdd || (null == mdd._heartMedexam || "00".equals(mdd._heartMedexam))) {
                return false;
            } else {
                return true;
            }

         // 検診のおすすめ票（内科）
        } else if ("10".equals(div) || "22".equals(div)) {
            // 未受験者を印刷
            if (null == mdd
                || (null == mdd._spineribcd     || "00".equals(mdd._spineribcd))
                && (null == mdd._skindiseasecd  || "00".equals(mdd._skindiseasecd))
                && (null == mdd._otherdiseasecd || "00".equals(mdd._otherdiseasecd))) {
                return false;
            } else {
                return true;
            }

        // 検診のおすすめ票（尿）
        } else if ("11".equals(div) || "23".equals(div)) {
            // 未受験者を印刷
            if (null == mdd
                || (null == mdd._albuminuria1cd || "00".equals(mdd._albuminuria1cd))
                && (null == mdd._uricbleed1cd   || "00".equals(mdd._uricbleed1cd))
                && (null == mdd._uricsugar1cd   || "00".equals(mdd._uricsugar1cd))) {
                return false;
            } else {
                return true;
            }

        // 腎臓検診第２次（尿検査）のお知らせ
        } else if ("13".equals(div) || "26".equals(div)) {
            if (   null == mdd._albuminuria1cd
                || null == mdd._uricbleed1cd
                || null == mdd._uricsugar1cd) {
                return true;
            } else {
                return false;
            }

        // 腎臓検診第２次（尿検査）結果報告
        } else if ("14".equals(div) || "27".equals(div)) {
            if (   null == mdd._albuminuria2cd
                || null == mdd._uricbleed2cd
                || null == mdd._uricsugar2cd) {
                return true;
            } else {
                return false;
            }

        // 検診のおすすめ票（結核）
        } else if ("24".equals(div)) {
            // 未受験者を印刷
            if (null == mdd || (null == mdd._tbNamecd || "00".equals(mdd._tbNamecd))) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    // ４）１６）健康診断結果のお知らせ
    private void print4(final DB2UDB db2, final Vrw32alp svf, final List studentList, final String documentCd) {
        final String form = "KNJF030G_4.frm";
        log.info(" form = " + form);

        final List documentMstTextTokenList = KNJ_EditKinsoku.getTokenList(_param.getDocumentMstText(documentCd), 80);

        for (int sti = 0; sti < studentList.size(); sti++) {
            final Student student = (Student) studentList.get(sti);

            //対象校種じゃない時は、印刷しない
            final String chkSchoolKind = ("04".equals(documentCd)) ? "J": "H";
            if(!student._schoolKind.equals(chkSchoolKind)) continue;

            svf.VrSetForm(form, 1);

            //ヘッダーセット
            printHeaderData(svf, student, documentCd);
            printDocumentMstTitleText(svf, _param.getDocumentMstTitle(documentCd), documentMstTextTokenList, _param.getDocumentMstCertifNo(documentCd));

//            printPrincipalStaffname(svf);

            boolean printpermark = true;//"計算不能"を出力する特殊パターン以外は%記号を出力
            //データセット
            if (null != student._medexamDetDat) {
                final MedexamDetDat mdd = student._medexamDetDat;
                //身体測定
                svf.VrsOut("HEIGHT", Util.sishagonyu(mdd._height)); // 身長
                svf.VrsOut("WEIGHT", Util.sishagonyu(mdd._weight)); // 体重
                final String outStr = Util.calcHimando(student, db2, _param);
                if (outStr == null) {
                    svf.VrsOut("OBESITY_IMP", "計算不能");
                    printpermark = false;
                } else {
                    svf.VrsOut("OBESITY", outStr); // 肥満度
                }
                svf.VrsOut("NUTRITION", _param.getNameMstName1("F030", mdd._nutritioncd)); // 栄養状態

                //眼
                svf.VrsOut("R_BAREVISION", mdd._rBarevisionMark);   // 視力・右
                svf.VrsOut("R_VISION", mdd._rVisionMark);           // 視力・右・矯正
                svf.VrsOut("L_BAREVISION", mdd._lBarevisionMark);   // 視力・左
                svf.VrsOut("L_VISION", mdd._lVisionMark);           // 視力・左・矯正
                svf.VrsOut("EYEDISEASE", Util.mkString(new String[] {_param.getNameMstName1("F050", mdd._eyediseasecd), mdd._eyeTestResult}, " ")); // 目の疾病

                //耳・鼻
                svf.VrsOut("R_EAR", _param.getNameMstName1("F010", mdd._rEar)); //聴力・右
                svf.VrsOut("L_EAR", _param.getNameMstName1("F010", mdd._lEar)); //聴力・左

                final String noseDisease = Util.mkString(new String[] {
                        _param.getNameMstName1("F060", mdd._nosediseasecd)
                        , mdd._nosediseasecdRemark
                        , _param.getNameMstName1("F061", mdd._nosediseasecd5)
                        , mdd._nosediseasecdRemark1
                        , _param.getNameMstName1("F062", mdd._nosediseasecd6)
                        , mdd._nosediseasecdRemark2
                        , _param.getNameMstName1("F063", mdd._nosediseasecd7)
                        , mdd._nosediseasecdRemark3
                  }, " ");
                final String fieldNose = Util.getMS932ByteLength(noseDisease) > 40 ? "3" : Util.getMS932ByteLength(noseDisease) > 20 ? "2" : "";
                svf.VrsOut("NOSEDISEASE" + fieldNose, noseDisease); // 耳鼻咽頭疾患

                //内科検診
                svf.VrsOut("SPINERIB", Util.mkString(new String[] {_param.getNameMstName1("F040", mdd._spineribcd), mdd._spineribcdRemark}, " ")); // 脊柱・胸郭
                svf.VrsOut("MOVEMENT", _param.getNameMstName1("F141", mdd._guideDiv));  //運動器
                svf.VrsOut("SKINDISEASE", Util.mkString(new String[] {_param.getNameMstName1("F070", mdd._skindiseasecd), mdd._skindiseasecdRemark}, " ")); // 皮膚疾患
                svf.VrsOut("OTHERDISEASE", Util.mkString(new String[] {_param.getNameMstName1("F140", mdd._otherdiseasecd), mdd._otherRemark}, " ")); // その他の疾病および異常
                if ((null == mdd) ||
                    ((null == mdd._spineribcd     || "00".equals(mdd._spineribcd)) &&
                     (null == mdd._guideDiv       || "00".equals(mdd._guideDiv)) &&
                     (null == mdd._skindiseasecd  || "00".equals(mdd._skindiseasecd)) &&
                     (null == mdd._otherdiseasecd || "00".equals(mdd._otherdiseasecd)))) {
                    svf.VrsOut("INTERNAL_CHECK", "未受診");
                }

                //結核
                final String setTbName = Util.mkString(new String[] {_param.getNameMstName1("F120", mdd._tbNamecd), mdd._tbNameRemark1}, " ");
                final String viesField = KNJ_EditEdit.getMS932ByteLength(setTbName) > 40 ? "_3": KNJ_EditEdit.getMS932ByteLength(setTbName) > 20 ? "_2": "";
                svf.VrsOut("VIEWS1" + viesField, setTbName); // 間接撮影結果

                //心臓
                final String setHeartMedexam   = Util.mkString(new String[] {_param.getNameMstName1("F080", mdd._heartMedexam), mdd._heartMedexamRemark}, " ");
                final String fieldHeartMedexam = Util.getMS932ByteLength(setHeartMedexam) > 40 ? "3" : Util.getMS932ByteLength(setHeartMedexam) > 20 ? "2" : "";
                svf.VrsOut("HEART_MEDEXAM" + fieldHeartMedexam, setHeartMedexam); // 心電図

                //尿
                svf.VrsOut("ALBUMINURIA", _param.getNameMstName1("F020", mdd._albuminuria1cd)); // 蛋白
                svf.VrsOut("URICBLEED", _param.getNameMstName1("F018", mdd._uricbleed1cd));     // 潜血
                svf.VrsOut("URICSUGAR", _param.getNameMstName1("F019", mdd._uricsugar1cd));     // 糖
                if (null == mdd._albuminuria2cd && null == mdd._uricbleed2cd && null == mdd._uricsugar2cd) {
                    svf.VrsOut("URINE2", "異常なし");
                } else {
                    String setAuu = "";
                    setAuu  = (null != mdd._albuminuria2cd) ? "蛋白:"+_param.getNameMstName1("F020", mdd._albuminuria2cd)+" ": ""; // 尿二次・検査結果
                    setAuu += (null != mdd._uricsugar2cd)   ? "糖:"  +_param.getNameMstName1("F019", mdd._uricsugar2cd)  +" ": ""; // 尿二次・検査結果
                    setAuu += (null != mdd._uricbleed2cd)   ? "潜血:"+_param.getNameMstName1("F018", mdd._uricbleed2cd)  +" ": ""; // 尿二次・検査結果
                    svf.VrsOut("URINE2", setAuu); // 尿二次・検査結果
                }
                final List uricothertestList = KNJ_EditKinsoku.getTokenList(mdd._uricothertest, 30);
                printTextData(svf, uricothertestList, "URINE_DETAIL");     // 結果詳細
                svf.VrsOut("URICOTHERTEST", mdd._uricothertest);     // 結果詳細
            }

            //歯
            if (null != student._medexamToothDat) {
                final MedexamToothDat mtd = student._medexamToothDat;

                final String brackAdultTooth = null == mtd._brackAdulttooth ? "　": Integer.parseInt(mtd._brackAdulttooth) > 0 ? "有": "0";
                svf.VrsOut("BRACKADULTTOOTH", brackAdultTooth);   // 虫歯・要観察の歯の有無

                String setJointCd = "";
                if ("01".equals(mtd._jawsJointcd) && "01".equals(mtd._jawsJointcd2)) {
                    setJointCd = _param.getNameMstName1("F510", mtd._jawsJointcd);
                } else if ((null == mtd._jawsJointcd || "00".equals(mtd._jawsJointcd)) && (null == mtd._jawsJointcd2 || "00".equals(mtd._jawsJointcd2))) {
                    setJointCd = "未受診";
                } else {
                    setJointCd = _param.getNameMstName1("F510", mtd._jawsJointcd) +" "+ _param.getNameMstName1("F511", mtd._jawsJointcd2);
                }
                final String jointField = KNJ_EditEdit.getMS932ByteLength(setJointCd) > 30 ? "3": KNJ_EditEdit.getMS932ByteLength(setJointCd) > 20 ? "2": "1";
                svf.VrsOut("JAWS_JOINT1_" + jointField, setJointCd);//歯列・咬合, 顎関節

                final String plaque = _param.getNameMstName1("F520", mtd._plaquecd); //歯垢の状態
                svf.VrsOut(Util.getMS932ByteLength(plaque) <= 20 ? "PLAQUE1" : Util.getMS932ByteLength(plaque) <= 30 ? "PLAQUE2" : "PLAQUE3", plaque);

                final String gum = _param.getNameMstName1("F513", mtd._gumcd); //歯肉の状態
                svf.VrsOut(Util.getMS932ByteLength(gum) <= 20 ? "GUM1" : Util.getMS932ByteLength(gum) <= 30 ? "GUM2" : "GUM3", gum);

                final String toothotherdisease = Util.mkString(new String[] {_param.getNameMstName1("F530", mtd._otherdiseasecd), mtd._otherdisease}, " ");//歯その他
                svf.VrsOut(Util.getMS932ByteLength(toothotherdisease) <= 20 ? "TOOTHOTHERDISEASE1" : Util.getMS932ByteLength(toothotherdisease) <= 30 ? "TOOTHOTHERDISEASE2" : "TOOTHOTHERDISEASE3", toothotherdisease);

                if ((null == mtd) ||
                    ((null == mtd._brackAdulttooth) &&
                     (null == mtd._jawsJointcd    || "00".equals(mtd._jawsJointcd)) &&
                     (null == mtd._jawsJointcd2   || "00".equals(mtd._jawsJointcd2)) &&
                     (null == mtd._plaquecd       || "00".equals(mtd._plaquecd)) &&
                     (null == mtd._gumcd          || "00".equals(mtd._gumcd)) &&
                     (null == mtd._otherdiseasecd || "00".equals(mtd._otherdiseasecd)))) {
                    svf.VrsOut("TOOTH_CHECK", "未受診");
                }
            }
            if (printpermark) {
                //"計算不能"を出力する特殊パターン以外は%記号を「計算結果が出力されていなくても」出力
                svf.VrsOut("PER", "％");
            }

            svf.VrEndPage();
            _hasdata = true;
        }
    }

    // ５）１７）受診報告書
    private void print5(final Vrw32alp svf, final List studentList, final String documentCd) {
        final String form = "KNJF030G_5.frm";
        log.info(" form = " + form);

        for (int sti = 0; sti < studentList.size(); sti++) {
            final Student student = (Student) studentList.get(sti);

            //対象校種じゃない時は、印刷しない
            final String chkSchoolKind = ("05".equals(documentCd)) ? "J": "H";
            if(!student._schoolKind.equals(chkSchoolKind)) continue;

            svf.VrSetForm(form, 1);

            svf.VrsOut("DEPARTMENT", student._majorname);//学科
            svf.VrsOut("HR_NAME", student._hrName+student._attendno+"番");//年組番
            svf.VrsOut("NAME", student._name);//生徒名

            svf.VrEndPage();

            _hasdata = true;
        }
    }

    // ６）１８）検診のおすすめ票（眼科）
    private void print6(final Vrw32alp svf, final List studentList, final String documentCd) {
        final String form = "KNJF030G_6.frm";
        log.info(" form = " + form);
        final List documentMstTextTokenList = KNJ_EditKinsoku.getTokenList(_param.getDocumentMstText(documentCd), 80);

        for (int sti = 0; sti < studentList.size(); sti++) {
            final Student student = (Student) studentList.get(sti);

            //対象校種じゃない時は、印刷しない
            final String chkSchoolKind = ("06".equals(documentCd)) ? "J": "H";
            if(!student._schoolKind.equals(chkSchoolKind)) continue;

            final MedexamDetDat mdd = student._medexamDetDat;
            if (isNotPrint(documentCd, mdd, null)) {
                continue;
            }

            svf.VrSetForm(form, 1);

            //ヘッダーセット
            printHeaderData(svf, student, documentCd);
            printDocumentMstTitleText(svf, _param.getDocumentMstTitle(documentCd), documentMstTextTokenList, _param.getDocumentMstCertifNo(documentCd));

            //フッター情報
            printFooterData(svf, student, "（眼科検診未受診者）", documentCd);

            svf.VrEndPage();
            _hasdata = true;
        }
    }

    // ７）１９）検診のおすすめ票（歯科）
    private void print7(final Vrw32alp svf, final List studentList, final String documentCd) {
        final String form = "KNJF030G_7.frm";
        log.info(" form = " + form);
        final List documentMstTextTokenList = KNJ_EditKinsoku.getTokenList(_param.getDocumentMstText(documentCd), 80);

        for (int sti = 0; sti < studentList.size(); sti++) {
            final Student student = (Student) studentList.get(sti);

            //対象校種じゃない時は、印刷しない
            final String chkSchoolKind = ("07".equals(documentCd)) ? "J": "H";
            if(!student._schoolKind.equals(chkSchoolKind)) continue;

            final MedexamToothDat mtd = student._medexamToothDat;
            if (isNotPrint(documentCd, null, mtd)) {
                continue;
            }

            svf.VrSetForm(form, 1);

            //ヘッダーセット
            printHeaderData(svf, student, documentCd);
            printDocumentMstTitleText(svf, _param.getDocumentMstTitle(documentCd), documentMstTextTokenList, _param.getDocumentMstCertifNo(documentCd));

            //フッター情報
            printFooterData(svf, student, "（歯科未受診者）", documentCd);

            svf.VrEndPage();
            _hasdata = true;
        }
    }

    // ８）２０）検診のおすすめ票（耳鼻科）
    private void print8(final Vrw32alp svf, final List studentList, final String documentCd) {
        final String form = "KNJF030G_6.frm";
        log.info(" form = " + form);
        final List documentMstTextTokenList = KNJ_EditKinsoku.getTokenList(_param.getDocumentMstText(documentCd), 80);

        for (int sti = 0; sti < studentList.size(); sti++) {
            final Student student = (Student) studentList.get(sti);

            //対象校種じゃない時は、印刷しない
            final String chkSchoolKind = ("08".equals(documentCd)) ? "J": "H";
            if(!student._schoolKind.equals(chkSchoolKind)) continue;

            final MedexamDetDat mdd = student._medexamDetDat;
            if (isNotPrint(documentCd, mdd, null)) {
                continue;
            }

            svf.VrSetForm(form, 1);

            //ヘッダーセット
            printHeaderData(svf, student, documentCd);
            printDocumentMstTitleText(svf, _param.getDocumentMstTitle(documentCd), documentMstTextTokenList, _param.getDocumentMstCertifNo(documentCd));

            //フッター情報
            printFooterData(svf, student, "（耳鼻科検診未受診者）", documentCd);

            svf.VrEndPage();
            _hasdata = true;
        }
    }

    // ９）２１）検診のおすすめ票（心臓）
    private void print9(final DB2UDB db2, final Vrw32alp svf, final List studentList, final String documentCd) {
        final String form = "KNJF030G_6.frm";
        log.info(" form = " + form);
        final List documentMstTextTokenList = KNJ_EditKinsoku.getTokenList(_param.getDocumentMstText(documentCd), 80);

        for (int sti = 0; sti < studentList.size(); sti++) {
            final Student student = (Student) studentList.get(sti);

            //対象校種じゃない時は、印刷しない
            final String chkSchoolKind = ("09".equals(documentCd)) ? "J": "H";
            if(!student._schoolKind.equals(chkSchoolKind)) continue;

            final MedexamDetDat mdd = student._medexamDetDat;
            if (isNotPrint(documentCd, mdd, null)) {
                continue;
            }

            svf.VrSetForm(form, 1);

            //ヘッダーセット
            printHeaderData(svf, student, documentCd);
            printDocumentMstTitleText(svf, _param.getDocumentMstTitle(documentCd), documentMstTextTokenList, _param.getDocumentMstCertifNo(documentCd));

            //フッター情報
            printFooterData(svf, student, "（心臓検診未受診者）", documentCd);

            svf.VrEndPage();
            _hasdata = true;
        }
    }

    // １０）２２）検診のおすすめ票（内科）
    private void print10(final Vrw32alp svf, final List studentList, final String documentCd) {
        final String form = "KNJF030G_6.frm";
        log.info(" form = " + form);
        final List documentMstTextTokenList = KNJ_EditKinsoku.getTokenList(_param.getDocumentMstText(documentCd), 80);

        for (int sti = 0; sti < studentList.size(); sti++) {
            final Student student = (Student) studentList.get(sti);

            //対象校種じゃない時は、印刷しない
            final String chkSchoolKind = ("10".equals(documentCd)) ? "J": "H";
            if(!student._schoolKind.equals(chkSchoolKind)) continue;

            final MedexamDetDat mdd = student._medexamDetDat;
            if (isNotPrint(documentCd, mdd, null)) {
                continue;
            }

            svf.VrSetForm(form, 1);

            //ヘッダーセット
            printHeaderData(svf, student, documentCd);
            printDocumentMstTitleText(svf, _param.getDocumentMstTitle(documentCd), documentMstTextTokenList, _param.getDocumentMstCertifNo(documentCd));

            //フッター情報
            printFooterData(svf, student, "（内科検診未受診者）", documentCd);

            svf.VrEndPage();
            _hasdata = true;
        }
    }

    // １１）２３）検診のおすすめ票（尿）
    private void print11(final Vrw32alp svf, final List studentList, final String documentCd) {
        final String form = "KNJF030G_11.frm";
        log.info(" form = " + form);
        final List documentMstTextTokenList = KNJ_EditKinsoku.getTokenList(_param.getDocumentMstText(documentCd), 80);

        for (int sti = 0; sti < studentList.size(); sti++) {
            final Student student = (Student) studentList.get(sti);

            //対象校種じゃない時は、印刷しない
            final String chkSchoolKind = ("11".equals(documentCd)) ? "J": "H";
            if(!student._schoolKind.equals(chkSchoolKind)) continue;

            final MedexamDetDat mdd = student._medexamDetDat;
            if (isNotPrint(documentCd, mdd, null)) {
                continue;
            }

            svf.VrSetForm(form, 1);

            //ヘッダーセット
            printHeaderData(svf, student, documentCd);
            printDocumentMstTitleText(svf, _param.getDocumentMstTitle(documentCd), documentMstTextTokenList, _param.getDocumentMstCertifNo(documentCd));

            //フッター情報
            printFooterData(svf, student, "（尿検査未実施者）", documentCd);

            svf.VrEndPage();

            _hasdata = true;
        }
    }

    // １２）２５）心臓健診結果のお知らせ
    private void print12(final Vrw32alp svf, final List studentList, final String documentCd) {
        final String form = "KNJF030G_12.frm";
        log.info(" form = " + form);
        final List documentMstTextTokenList = KNJ_EditKinsoku.getTokenList(_param.getDocumentMstText(documentCd), 80);

        for (int sti = 0; sti < studentList.size(); sti++) {
            final Student student = (Student) studentList.get(sti);

            //対象校種じゃない時は、印刷しない
            final String chkSchoolKind = ("12".equals(documentCd)) ? "J": "H";
            if(!student._schoolKind.equals(chkSchoolKind)) continue;

            final MedexamDetDat mdd = student._medexamDetDat;

            svf.VrSetForm(form, 1);

            //ヘッダーセット
            printHeaderData(svf, student, documentCd);
            printDocumentMstTitleText(svf, _param.getDocumentMstTitle(documentCd), documentMstTextTokenList, _param.getDocumentMstCertifNo(documentCd));

            //データ情報
            final String heartMedexam = Util.mkString(new String[] {_param.getNameMstName1("F080", mdd._heartMedexam), mdd._heartMedexamRemark}, " ");
            svf.VrsOut("HEART", heartMedexam); // 心電図所見
/*
            if (Util.getMS932ByteLength(heartMedexam) <= 46) {
                svf.VrsOut("HEART1", heartMedexam); // 心電図所見
            } else {
                final List tokenList = KNJ_EditKinsoku.getTokenList(heartMedexam, 46);
                for (int i = 0; i < tokenList.size(); i++) {
                    if (i == 0) {
                        svf.VrsOut("HEART", (String) tokenList.get(i)); // 心電図所見
                    } else {
                        svf.VrsOut("HEART" + String.valueOf(i + 1), (String) tokenList.get(i)); // 心電図所見
                    }
                }
            }
*/

            svf.VrEndPage();
            _hasdata = true;
        }
    }

    // １３）２６）腎臓検診第2次（尿検査）のお知らせ
    private void print13(final Vrw32alp svf, final List studentList, final String documentCd) {
        final String form = "KNJF030G_13.frm";
        log.info(" form = " + form);
        final List documentMstTextTokenList = KNJ_EditKinsoku.getTokenList(_param.getDocumentMstText(documentCd), 80);

        for (int sti = 0; sti < studentList.size(); sti++) {
            final Student student = (Student) studentList.get(sti);

            //対象校種じゃない時は、印刷しない
            final String chkSchoolKind = ("13".equals(documentCd)) ? "J": "H";
            if(!student._schoolKind.equals(chkSchoolKind)) continue;

            final MedexamDetDat mdd = student._medexamDetDat;
            if (isNotPrint(documentCd, mdd, null)) {
                continue;
            }

            svf.VrSetForm(form, 1);

            //ヘッダーセット
            printHeaderData(svf, student, documentCd);
            printDocumentMstTitleText(svf, _param.getDocumentMstTitle(documentCd), documentMstTextTokenList, _param.getDocumentMstCertifNo(documentCd));

            //データセット
            final String setDate = KNJ_EditDate.h_format_JP(_param._presentDate)+"（"+KNJ_EditDate.h_format_W(_param._presentDate)+"）";
            svf.VrsOut("SUBMIT_DATE", setDate); // 提出日
            svf.VrsOut("SUBMIT_TIME", "（朝９:００まで）"); // 提出日（時間）
            svf.VrsOut("ALBUMINURIA", _param.getNameMstName1("F020", mdd._albuminuria1cd)); // 蛋白
            svf.VrsOut("URICSUGAR", _param.getNameMstName1("F019", mdd._uricsugar1cd));     // 糖
            svf.VrsOut("URICBLEED", _param.getNameMstName1("F018", mdd._uricbleed1cd));     // 潜血

            svf.VrEndPage();
            _hasdata = true;
        }
    }

    //１４）２７）尿検査の結果について
    private void print14(final Vrw32alp svf, final List studentList, final String documentCd) {
        final List documentMstTextTokenList = KNJ_EditKinsoku.getTokenList(_param.getDocumentMstText(documentCd), 80);

        for (int sti = 0; sti < studentList.size(); sti++) {
            final Student student = (Student) studentList.get(sti);

            //対象校種じゃない時は、印刷しない
            final String chkSchoolKind = ("14".equals(documentCd)) ? "J": "H";
            if(!student._schoolKind.equals(chkSchoolKind)) continue;

            final MedexamDetDat mdd = student._medexamDetDat;
            if (isNotPrint(documentCd, mdd, null)) {
                continue;
            }

            //１枚目
            svf.VrSetForm("KNJF030G_14_1.frm", 1);

            //ヘッダーセット
            printHeaderData(svf, student, documentCd);
            printDocumentMstTitleText(svf, _param.getDocumentMstTitle(documentCd), documentMstTextTokenList, _param.getDocumentMstCertifNo(documentCd));

            //データセット
            final String setDate = KNJ_EditDate.h_format_JP(_param._inspectionFrom)+"～"+KNJ_EditDate.h_format_S(_param._inspectionTo, "d")+"日";
            svf.VrsOut("CHECK_DATE", setDate); // 検査日時
            svf.VrsOut("CHECK_PLACE", _param._inspectionBody); // 検査機関
            svf.VrsOut("ALBUMINURIA", _param.getNameMstName1("F020", mdd._albuminuria2cd)); // 蛋白
            svf.VrsOut("URICSUGAR", _param.getNameMstName1("F019", mdd._uricsugar2cd));     // 糖
            svf.VrsOut("URICBLEED", _param.getNameMstName1("F018", mdd._uricbleed2cd));     // 潜血

            svf.VrEndPage();

            //２枚目
            svf.VrSetForm("KNJF030G_14_2.frm", 1);

            svf.VrsOut("HR_NAME1_1", "担 当 医　様"); //担当医

            svf.VrsOut("SCHOOL_NAME1_1", _param.getSchoolInfo(student._schoolKind, SCHOOL_NAME1));             //学校名１
            svf.VrsOut("SCHOOL_NAME1_2", _param.getSchoolInfo(student._schoolKind, SCHOOL_NAME2));             //学校名２
            final String setStaffName = _param.getSchoolInfo(student._schoolKind, PRINCIPAL_JOBNAME) +_param.getSchoolInfo(student._schoolKind, PRINCIPAL_NAME);
            final String sNameField   = KNJ_EditEdit.getMS932ByteLength(setStaffName) <= 26 ? "1": KNJ_EditEdit.getMS932ByteLength(setStaffName) <= 30 ? "2": "3";
            svf.VrsOut("STAFF_NAME" + sNameField, setStaffName); //職名+校長名

            final String setDate2 = KNJ_EditDate.h_format_JP_N(_param._ctrlDate).substring(0, 2)+"　　年　　月　　日";
            svf.VrsOut("DATE2", setDate2);

            for (int i = 0; i < documentMstTextTokenList.size(); i++) {
                svf.VrsOut("TEXT" + String.valueOf(i + 1), (String) documentMstTextTokenList.get(i));//文面
            }

            svf.VrEndPage();

            _hasdata = true;
        }
    }

    // １５）２９）治療継続証明書
    private void print15(final Vrw32alp svf, final List studentList, final String documentCd) {
        final String form = "KNJF030G_15.frm";
        log.info(" form = " + form);

        final List documentMstTextTokenList = KNJ_EditKinsoku.getTokenList(_param.getDocumentMstText(documentCd), 80);

        for (int sti = 0; sti < studentList.size(); sti++) {
            final Student student = (Student) studentList.get(sti);

            //対象校種じゃない時は、印刷しない
            final String chkSchoolKind = ("15".equals(documentCd)) ? "J": "H";
            if(!student._schoolKind.equals(chkSchoolKind)) continue;

            svf.VrSetForm(form, 1);

            //ヘッダーセット
            printHeaderData(svf, student, documentCd);
            printDocumentMstTitleText(svf, _param.getDocumentMstTitle(documentCd), documentMstTextTokenList, _param.getDocumentMstCertifNo(documentCd));

            //フッター情報
            printFooterData(svf, student, "", documentCd);

            svf.VrEndPage();

            _hasdata = true;
        }
    }

    // ２４）検診のおすすめ票（結核）
    private void print24(final Vrw32alp svf, final List studentList, final String documentCd) {
        final String form = "KNJF030G_6.frm";
        log.info(" form = " + form);
        final List documentMstTextTokenList = KNJ_EditKinsoku.getTokenList(_param.getDocumentMstText(documentCd), 80);

        for (int sti = 0; sti < studentList.size(); sti++) {
            final Student student = (Student) studentList.get(sti);

            //対象校種じゃない時は、印刷しない
            final String chkSchoolKind = "H";
            if(!student._schoolKind.equals(chkSchoolKind)) continue;

            final MedexamDetDat mdd = student._medexamDetDat;
            if (isNotPrint(documentCd, mdd, null)) {
                continue;
            }

            svf.VrSetForm(form, 1);

            //ヘッダーセット
            printHeaderData(svf, student, documentCd);
            printDocumentMstTitleText(svf, _param.getDocumentMstTitle(documentCd), documentMstTextTokenList, _param.getDocumentMstCertifNo(documentCd));

            //フッター情報
            printFooterData(svf, student, "（結核検診未受診者）", documentCd);

            svf.VrEndPage();
            _hasdata = true;
        }
    }

    // ２８）結核検診の結果について
    private void print28(final Vrw32alp svf, final List studentList, final String documentCd) {
        final List documentMstTextTokenList = KNJ_EditKinsoku.getTokenList(_param.getDocumentMstText(documentCd), 80);

        for (int sti = 0; sti < studentList.size(); sti++) {
            final Student student = (Student) studentList.get(sti);

            //対象校種じゃない時は、印刷しない
            final String chkSchoolKind = "H";
            if(!student._schoolKind.equals(chkSchoolKind)) continue;

            final MedexamDetDat mdd = student._medexamDetDat;
            if (isNotPrint(documentCd, mdd, null)) {
                continue;
            }

            //１枚目
            svf.VrSetForm("KNJF030G_28_1.frm", 1);

            //ヘッダーセット
            printHeaderData(svf, student, documentCd);
            printDocumentMstTitleText(svf, _param.getDocumentMstTitle(documentCd), documentMstTextTokenList, _param.getDocumentMstCertifNo(documentCd));

            //データセット
            final String tbRemarkcd = Util.mkString(new String[] {_param.getNameMstName1("F100", mdd._tbRemarkcd), mdd._tbXRay}, " ");
            if (Util.getMS932ByteLength(tbRemarkcd) <= 40) {
                svf.VrsOut("VIEW1", tbRemarkcd); // 所見
            } else {
                final List tokenList = KNJ_EditKinsoku.getTokenList(tbRemarkcd, 40);
                for (int i = 0; i < tokenList.size(); i++) {
                    svf.VrsOut("VIEW" + String.valueOf(i + 1), (String) tokenList.get(i)); // 所見
                }
            }

            svf.VrEndPage();

            //２枚目
            svf.VrSetForm("KNJF030G_28_2.frm", 1);

            final String setDate = KNJ_EditDate.h_format_JP_N(_param._ctrlDate).substring(0, 2)+"　　年　　月　　日";
            svf.VrsOut("DATE", setDate);

            final String photpDate = "撮影日　"+KNJ_EditDate.h_format_JP_N(_param._ctrlDate).substring(0, 2)+"　　年　　月　　日";
            svf.VrsOut("PHOTO_DATE", photpDate);//撮影日付

            //フッター情報
            printFooterData(svf, student, "", documentCd);

            svf.VrEndPage();
            _hasdata = true;
        }
    }

    public static class Student {
        final String _schregno;
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _attendno;
        final String _annual;
        final String _name;
        final String _sexCd;
        final String _sex;
        final String _birthDay;
        final String _coursecd;
        final String _majorcd;
        final String _coursecode;
        final String _coursename;
        final String _majorname;
        final String _coursecodename;
        final String _schoolKind;
        MedexamDetDat _medexamDetDat = null;
        MedexamToothDat _medexamToothDat = null;
        String _nenkuminamae;
        int _nenkuminamaeLen;

        Student(final String schregno,
                final String grade,
                final String hrClass,
                final String hrName,
                final String attendno,
                final String annual,
                final String name,
                final String sexCd,
                final String sex,
                final String birthDay,
                final String coursecd,
                final String majorcd,
                final String coursecode,
                final String coursename,
                final String majorname,
                final String coursecodename,
                final String schoolKind
        ) {
            _schregno       = schregno;
            _grade          = grade;
            _hrClass        = hrClass;
            _hrName         = hrName;
            _attendno       = attendno;
            _annual         = annual;
            _name           = name;
            _sexCd          = sexCd;
            _sex            = sex;
            _birthDay       = birthDay;
            _coursecd       = coursecd;
            _majorcd        = majorcd;
            _coursecode     = coursecode;
            _coursename     = coursename;
            _majorname      = majorname;
            _coursecodename = coursecodename;
            _schoolKind     = schoolKind;
        }

        static List getStudentList(final DB2UDB db2, final Param param) throws SQLException {
            final List rtnList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String studentSql = Student.getStudentSql(param);
            log.debug(" student sql = " + studentSql);
            try {
                ps = db2.prepareStatement(studentSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno       = rs.getString("SCHREGNO");
                    final String grade          = rs.getString("GRADE");
                    final String hrClass        = rs.getString("HR_CLASS");
                    final String hrName         = rs.getString("HR_NAME");
                    final String attendno       = rs.getString("ATTENDNO");
                    final String annual         = rs.getString("ANNUAL");
                    final String name           = rs.getString("NAME");
                    final String sexCd          = rs.getString("SEX_CD");
                    final String sex            = rs.getString("SEX");
                    final String birthDay       = rs.getString("BIRTHDAY");
                    final String coursecd       = rs.getString("COURSECD");
                    final String majorcd        = rs.getString("MAJORCD");
                    final String coursecode     = rs.getString("COURSECODE");
                    final String coursename     = rs.getString("COURSENAME");
                    final String majorname      = rs.getString("MAJORNAME");
                    final String coursecodename = rs.getString("COURSECODENAME");
                    final String schoolKind     = rs.getString("SCHOOL_KIND");
                    final Student student       = new Student(schregno,
                                                               grade,
                                                               hrClass,
                                                               hrName,
                                                               attendno,
                                                               annual,
                                                               name,
                                                               sexCd,
                                                               sex,
                                                               birthDay,
                                                               coursecd,
                                                               majorcd,
                                                               coursecode,
                                                               coursename,
                                                               majorname,
                                                               coursecodename,
                                                               schoolKind);
                    final String ban = NumberUtils.isDigits(student._attendno) ? String.valueOf(Integer.parseInt(student._attendno)) : StringUtils.defaultString(student._attendno);
                    student._nenkuminamae = StringUtils.defaultString(student._hrName) + " " + ban + "番　名前　" + StringUtils.defaultString(student._name);
                    student._nenkuminamaeLen = Util.getMS932ByteLength(student._nenkuminamae);
                    rtnList.add(student);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            for (final Iterator it = rtnList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();

                student._medexamDetDat   = new MedexamDetDat(db2, param._year, student._schregno, printKenkouSindanIppan);
                student._medexamToothDat = new MedexamToothDat(db2, param._year, student._schregno);
            }
            return rtnList;
        }

        private static String getStudentSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     HR.HR_NAME, ");
            stb.append("     T1.ATTENDNO, ");
            stb.append("     T1.ANNUAL, ");
            stb.append("     BASE.NAME, ");
            stb.append("     BASE.SEX AS SEX_CD, ");
            stb.append("     N1.NAME2 AS SEX, ");
            stb.append("     BASE.BIRTHDAY, ");
            stb.append("     T1.COURSECD, ");
            stb.append("     T1.MAJORCD, ");
            stb.append("     T1.COURSECODE, ");
            stb.append("     COURSE.COURSENAME, ");
            stb.append("     MAJOR.MAJORNAME, ");
            stb.append("     COURSEC.COURSECODENAME, ");
            stb.append("     GDAT.SCHOOL_KIND ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1 ");
            stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON T1.YEAR  = GDAT.YEAR ");
            stb.append("                                    AND T1.GRADE = GDAT.GRADE ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT AS HR ON T1.YEAR = HR.YEAR ");
            stb.append("                                     AND T1.SEMESTER = HR.SEMESTER ");
            stb.append("                                     AND T1.GRADE    = HR.GRADE ");
            stb.append("                                     AND T1.HR_CLASS = HR.HR_CLASS ");
            stb.append("     LEFT JOIN SCHREG_BASE_MST AS BASE ON T1.SCHREGNO = BASE.SCHREGNO ");
            stb.append("     LEFT JOIN NAME_MST AS N1 ON N1.NAMECD1 = 'Z002' ");
            stb.append("                             AND BASE.SEX   = N1.NAMECD2 ");
            stb.append("     LEFT JOIN COURSECODE_MST AS COURSEC ON T1.COURSECODE = COURSEC.COURSECODE ");
            stb.append("     LEFT JOIN MAJOR_MST AS MAJOR ON T1.COURSECD = MAJOR.COURSECD ");
            stb.append("                                 AND T1.MAJORCD  = MAJOR.MAJORCD ");
            stb.append("     LEFT JOIN COURSE_MST AS COURSE ON T1.COURSECD = COURSE.COURSECD ");
            stb.append(" WHERE ");
            stb.append("         T1.YEAR     = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + param._gakki + "' ");
            if ("1".equals(param._kubun)) { //1:クラス
                stb.append("       AND T1.GRADE || T1.HR_CLASS IN " + SQLUtils.whereIn(true, param._classSelected) + " ");
            } else if ("2".equals(param._kubun)) { //2:個人
                stb.append("       AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._classSelected) + " ");
            }
            stb.append(" ORDER BY ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.ATTENDNO ");
            return stb.toString();
        }
    }

    private static class RegdDat {
        final String _schregNo;
        final String _year;
        final String _semester;
        final String _grade;
        final String _hrClass;
        final String _attendNo;
        final String _hrName;
        final String _hrClassName;
        final String _gradeName;
        final String _gradeCd;
        final String _age;
        final String _hasData;
        final String _entYearGradeCd;
        final String _schoolKind;
        MedexamDetDat _medexamDetDat = null;
        MedexamToothDat _medexamToothDat = null;

        /**
         * コンストラクタ。
         */
        public RegdDat(
                final String schregNo,
                final String year,
                final String semester,
                final String grade,
                final String hrClass,
                final String attendNo,
                final String hrName,
                final String hrClassName,
                final String gradeName,
                final String gradeCd,
                final String age,
                final String hasData,
                final String entYearGradeCd,
                final String schoolKind
        ) {
            _schregNo       = schregNo;
            _year           = year;
            _semester       = semester;
            _grade          = grade;
            _hrClass        = hrClass;
            _attendNo       = attendNo;
            _hrName         = hrName;
            _hrClassName    = hrClassName;
            _gradeName      = gradeName;
            _gradeCd        = gradeCd;
            _age            = age;
            _hasData        = hasData;
            _entYearGradeCd = entYearGradeCd;
            _schoolKind = schoolKind;
        }

        private static List getRegdList(final DB2UDB db2, final Param param, final Student student) throws SQLException {
            final List regdList = new ArrayList();
            final String regdSql = getRegdSql(param, student);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(regdSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregNo       = rs.getString("SCHREGNO");
                    final String year           = rs.getString("YEAR");
                    final String semester       = rs.getString("SEMESTER");
                    final String grade          = rs.getString("GRADE");
                    final String hrClass        = rs.getString("HR_CLASS");
                    final String attendNo       = rs.getString("ATTENDNO");
                    final String hrName         = rs.getString("HR_NAME");
                    final String hrClassName    = rs.getString("HR_CLASS_NAME1");
                    final String gradeName      = rs.getString("GRADE_NAME1");
                    final String gradeCd        = rs.getString("GRADE_CD");
                    final String age            = rs.getString("AGE");
                    final String hasData        = rs.getString("HAS_DATA");
                    final String entYearGradeCd = rs.getString("ENT_YEAR_GRADE_CD");
                    final String schoolKind = rs.getString("SCHOOL_KIND");
                    final RegdDat regdDat       = new RegdDat(schregNo, year, semester, grade, hrClass, attendNo, hrName, hrClassName, gradeName, gradeCd, age, hasData, entYearGradeCd, schoolKind);
                    regdList.add(regdDat);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            for (final Iterator it = regdList.iterator(); it.hasNext();) {
                final RegdDat regd = (RegdDat) it.next();
                regd._medexamDetDat = new MedexamDetDat(db2, regd._year, regd._schregNo, printKenkouSindanIppan);
                regd._medexamToothDat = new MedexamToothDat(db2, regd._year, regd._schregNo);
            }
            return regdList;
        }

        private static String getRegdSql(final Param param, final Student student) {

            final StringBuffer stb = new StringBuffer();
            //在籍（現在年度）
            stb.append("WITH SCHNO AS ( ");
            stb.append("    SELECT ");
            stb.append("        TT1.SCHREGNO, ");
            stb.append("        TT1.YEAR, ");
            stb.append("        TT1.SEMESTER, ");
            stb.append("        TT2.SCHOOL_KIND, ");
            stb.append("        TT1.GRADE, ");
            stb.append("        TT1.HR_CLASS, ");
            stb.append("        TT1.ATTENDNO ");
            stb.append("    FROM ");
            stb.append("        SCHREG_REGD_DAT TT1 ");
            stb.append("        LEFT JOIN SCHREG_REGD_GDAT TT2 ");
            stb.append("          ON TT2.YEAR = TT1.YEAR ");
            stb.append("         AND TT2.GRADE = TT1.GRADE ");
            stb.append("    WHERE ");
            stb.append("            TT1.YEAR     = '" + param._year + "' ");
            stb.append("        AND TT1.SEMESTER = '" + param._gakki + "' ");
            stb.append("        AND TT1.SCHREGNO = '" + student._schregno + "' ");
            stb.append("    ) ");
            //現在年度以前の学期を取得
            stb.append(",SCHNO_MIN AS ( ");
            stb.append("    SELECT ");
            stb.append("        W1.SCHREGNO, ");
            stb.append("        W1.YEAR, ");
            stb.append("        MIN(W1.SEMESTER) AS SEMESTER ");
            stb.append("    FROM ");
            stb.append("        SCHREG_REGD_DAT W1 ");
            stb.append("        LEFT JOIN SCHREG_REGD_GDAT W3 ");
            stb.append("          ON W3.YEAR = W1.YEAR ");
            stb.append("         AND W3.GRADE = W1.GRADE ");
            stb.append("    WHERE ");
            stb.append("        EXISTS( ");
            stb.append("            SELECT ");
            stb.append("                'X' ");
            stb.append("            FROM ");
            stb.append("                SCHNO W2 ");
            stb.append("            WHERE ");
            stb.append("                    W2.SCHREGNO = W1.SCHREGNO ");
            stb.append("                AND W2.YEAR > W1.YEAR ");
            stb.append("                AND W2.SCHOOL_KIND = W3.SCHOOL_KIND ");
            stb.append("        ) ");
            stb.append("    GROUP BY ");
            stb.append("        W1.SCHREGNO, ");
            stb.append("        W1.YEAR ");
            stb.append("    ) ");
            //在籍（現在年度以外）
            stb.append(",SCHNO_ALL AS ( ");
            stb.append("    SELECT ");
            stb.append("        SCHREGNO, ");
            stb.append("        YEAR, ");
            stb.append("        SEMESTER, ");
            stb.append("        GRADE, ");
            stb.append("        HR_CLASS, ");
            stb.append("        ATTENDNO ");
            stb.append("    FROM ");
            stb.append("        SCHREG_REGD_DAT W1 ");
            stb.append("    WHERE ");
            stb.append("        EXISTS( ");
            stb.append("            SELECT ");
            stb.append("                'X' ");
            stb.append("            FROM ");
            stb.append("                SCHNO_MIN W2 ");
            stb.append("            WHERE ");
            stb.append("                    W2.SCHREGNO = W1.SCHREGNO ");
            stb.append("                AND W2.YEAR     = W1.YEAR ");
            stb.append("                AND W2.SEMESTER = W1.SEMESTER ");
            stb.append("        ) ");
            stb.append("    UNION ");
            stb.append("    SELECT ");
            stb.append("        SCHREGNO, ");
            stb.append("        YEAR, ");
            stb.append("        SEMESTER, ");
            stb.append("        GRADE, ");
            stb.append("        HR_CLASS, ");
            stb.append("        ATTENDNO ");
            stb.append("    FROM ");
            stb.append("        SCHNO W1 ");
            stb.append("    ) ");

            //メイン
            stb.append(" SELECT ");
            stb.append("     T1.*, ");
            stb.append("     T2.HR_NAME, ");
            stb.append("     T2.HR_CLASS_NAME1, ");
            stb.append("     GDAT.GRADE_NAME1, ");
            stb.append("     GDAT.GRADE_CD, ");
            stb.append("     CASE WHEN T3.BIRTHDAY IS NOT NULL ");
            stb.append("          THEN YEAR(T2.YEAR || '-04-01' - BIRTHDAY) ");
            stb.append("     END AS AGE, ");
            stb.append("     CASE WHEN MED.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("     END AS HAS_DATA, ");
            stb.append("     GDAT.SCHOOL_KIND, ");
            stb.append("     T_ENT_YEAR_GRADE_CD.GRADE_CD AS ENT_YEAR_GRADE_CD ");
            stb.append(" FROM ");
            stb.append("     SCHNO_ALL T1 ");
            stb.append("     INNER JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR     = T1.YEAR ");
            stb.append("                                   AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("                                   AND T2.GRADE    = T1.GRADE ");
            stb.append("                                   AND T2.HR_CLASS = T1.HR_CLASS ");
            stb.append("     LEFT JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO ");
            stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR  = T1.YEAR ");
            stb.append("                                    AND GDAT.GRADE = T1.GRADE ");
            stb.append("     LEFT JOIN NAME_MST VN ON VN.NAMECD1 = 'A023' ");
            stb.append("                          AND VN.NAME1   = GDAT.SCHOOL_KIND ");
            stb.append("     LEFT JOIN MEDEXAM_DET_DAT MED ON MED.YEAR     = T1.YEAR ");
            stb.append("                                  AND MED.SCHREGNO = T1.SCHREGNO ");
            stb.append("     LEFT JOIN (SELECT I1.SCHREGNO, MAX(I2.GRADE_CD) AS GRADE_CD  ");
            stb.append("                FROM SCHREG_REGD_DAT I1 ");
            stb.append("                INNER JOIN SCHREG_REGD_GDAT I2 ON I2.YEAR = I1.YEAR AND I2.GRADE = I1.GRADE ");
            stb.append("                INNER JOIN SCHREG_ENT_GRD_HIST_DAT I3 ON I3.SCHREGNO    = I1.SCHREGNO ");
            stb.append("                                                     AND I3.SCHOOL_KIND = I2.SCHOOL_KIND ");
            stb.append("                WHERE FISCALYEAR(I3.ENT_DATE) = I1.YEAR ");
            stb.append("                  AND I3.SCHOOL_KIND = I2.SCHOOL_KIND ");
            stb.append("                GROUP BY I1.SCHREGNO ");
            stb.append("               ) T_ENT_YEAR_GRADE_CD ON T_ENT_YEAR_GRADE_CD.SCHREGNO = T1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("    T1.GRADE BETWEEN VN.NAME2 AND VN.NAME3 ");
            stb.append(" ORDER BY ");
            stb.append("     T1.YEAR ");

            return stb.toString();
        }
    }

    /**
     * <<クラスの説明>>。
     * @author m-yama
     * @version $Id: 1d89d77a108152dfb00d1c85c730c49603ec9bc1 $
     */
    private static class KNJF030A {

        protected Param _param;
        protected DB2UDB _db2;

        private int hrnameKeta = 0;
        private int hrnameC2_1Keta = 0;
        private int nosedisease2Keta = 0;

        private boolean _hasData;

        /**
         * {@inheritDoc}
         */
        protected boolean printMain(final Vrw32alp svf, final DB2UDB db2, final HttpServletRequest request, final Param param, final List students, final String ippanDiv) throws SQLException {

            _param = param;
            _db2 = db2;

            _hasData = false;
            for (final Iterator itStudent = students.iterator(); itStudent.hasNext();) {
                final Student student = (Student) itStudent.next();
                log.debug(" schregno = " + student._schregno);

                printOut(svf, _db2, student, ippanDiv);
            }
            return _hasData;
        }

        private void printOut(final Vrw32alp svf, final DB2UDB db2, final Student student, final String ippanDiv) throws SQLException {
            final String[] printSchoolKind = {"J", "H"};

            for (int ki = 0; ki < printSchoolKind.length; ki++) {
                final String schoolKind = printSchoolKind[ki];
                if (!schoolKind.equals(student._schoolKind)) continue;

                final List printData = RegdDat.getRegdList(db2, _param, student);
                if (printData.size() == 0) {
                    return;
                }
                final String form = "1".equals(ippanDiv) ? "KNJF030G_1.frm": "KNJF030G_2.frm";
                log.fatal(" form = " + form);
                svf.VrSetForm(form, 4);

                try {
                    final Map fieldnameMap = _param.getFieldInfoMap(svf, form);
                    hrnameKeta       = Param.getFieldKeta(fieldnameMap, "HR_NAME");
                    hrnameC2_1Keta   = Param.getFieldKeta(fieldnameMap, "HR_NAMEC2_1");
                    nosedisease2Keta = Param.getFieldKeta(fieldnameMap, "NOSEDISEASE2");
                } catch (Exception e) {
                    log.warn("exception!", e);
                }
                if ("1".equals(_param._defineSchool.schooldiv)) {
                    svf.VrsOut("GRADENAME_TITLE", "年度");
                } else {
                    svf.VrsOut("GRADENAME_TITLE", "学年");
                }
                svf.VrsOut("NAME_HEADER", "名前"); // 名前ヘッダ

                //健康診断票の写し
                if ("2".equals(ippanDiv)) {
                    svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._date)); // 日付
                    final String setSchName = _param.getSchoolInfo(student._schoolKind, SCHOOL_NAME1) + "　" + _param.getSchoolInfo(student._schoolKind, SCHOOL_NAME2);
                    svf.VrsOut("CERTIF_SCHOOL_NAME", setSchName); // 学校名
                    final String filePath = _param.getSchoolStampImageFilePath(student._schoolKind);
                    if (null != filePath) {
                        svf.VrsOut("SCHOOL_STAMP", filePath);// 学校印
                    }
                }

                int dataCnt = 1;
                for (final Iterator itPrint = printData.iterator(); itPrint.hasNext();) {
                    final RegdDat regdDat = (RegdDat) itPrint.next();
                    svf.VrsOut("SCHREGNO", regdDat._schregNo);   //改ページ用
                    svf.VrsOut("NAME_SHOW" + (Util.getMS932ByteLength(student._name) > 24 ? "_2" : ""), student._name);//氏名
                    svf.VrsOut("SEX", student._sex);//性別
                    svf.VrsOut("BIRTHDAY", KNJ_EditDate.h_format_JP(student._birthDay));//生年月日
                    svf.VrsOut("SCHOOL_NAME", _param.getSchoolInfo(schoolKind, SCHOOL_NAME2));//学校名
                    final String title = "生徒健康診断票";
                    svf.VrsOut("TITLE",  title);
                    if ("1".equals(printKenkouSindanIppan)) {
                        // 病名のタイトル
                        svf.VrsOut("DISEASE_NAME_TITLE", "疾病及び異常");
                    }

                    if ("1".equals(_param._defineSchool.schooldiv)) {
                        if (NumberUtils.isDigits(regdDat._year)) {
                            svf.VrsOutn("GRADE_2", dataCnt, KenjaProperties.gengou(Integer.parseInt(regdDat._year)) + "年度");
                        }
                    } else {
                        svf.VrsOutn("GRADE", dataCnt, regdDat._gradeName);
                    }
                    svf.VrsOutn(null != regdDat._hrClassName && regdDat._hrClassName.length() > 4 ? "HR_NAME2_1" : "HR_NAME1", dataCnt, regdDat._hrClassName);
                    svf.VrsOutn("ATTENDNO", dataCnt, regdDat._attendNo);
                    if (hrnameKeta > 0 && Util.getMS932ByteLength(regdDat._hrName) > hrnameKeta && hrnameC2_1Keta > hrnameKeta) {
                        svf.VrsOutn("HR_NAMEC2_1", dataCnt, regdDat._hrName);
                    } else {
                        svf.VrsOutn("HR_NAME", dataCnt, regdDat._hrName);
                    }
                    svf.VrsOutn("YEAR", dataCnt, KenjaProperties.gengou(Integer.parseInt(regdDat._year)) + "年度");
                    svf.VrsOut("M_DATE", KenjaProperties.gengou(Integer.parseInt(regdDat._year)) + "年度");
                    svf.VrsOut("AGE", regdDat._age);        //４月１日現在の年齢

                    if (null != regdDat._hasData) {
                        final MedexamDetDat mdd = regdDat._medexamDetDat;
                        svf.VrsOut("HEIGHT", mdd._height);//身長
                        svf.VrsOut("WEIGHT", mdd._weight);//体重

                        //栄養状態
                        if ("01".equals(mdd._nutritioncd)) {
                            printSlash(svf, "SLASH_NUTRITION");
                        } else if ("98".equals(mdd._nutritioncd)) {
                            svf.VrsOut("NUTRITION", "未受診");
                        } else if (null != mdd._nutritioncd) {
                            svf.VrsOut("NUTRITION", _param.getNameMstName1("F030", mdd._nutritioncd));
                        }

                        //脊柱・胸郭・四肢
                        setName1WithSlash(svf, "SPINERIB", "F040", mdd._spineribcd, "SLASH_SPINERIB");
                        svf.VrsOut("SPINERIBCD_REMARK", mdd._spineribcdRemark);

                        //視力
                        String str2 = "";
                        String rBarevisionMark = mdd._rBarevisionMark;
                        String lBarevisionMark = mdd._lBarevisionMark;
                        if ("1".equals(mdd._visionCantMeasure)) {
                            str2 = "_2";
                            rBarevisionMark = "測定不能";
                            lBarevisionMark = "測定不能";
                        }
                        svf.VrsOut("R_BAREVISION" + str2, rBarevisionMark);
                        svf.VrsOut("R_VISION", mdd._rVisionMark);
                        svf.VrsOut("L_BAREVISION" + str2, lBarevisionMark);
                        svf.VrsOut("L_VISION", mdd._lVisionMark);

                        // 眼の疾病及び異常
                        setName1WithSlash(svf, "EYEDISEASE", "F050", mdd._eyediseasecd, "SLASH_EYEDISEASE");
                        svf.VrsOut("EYE_TEST_RESULT", mdd._eyeTestResult);

                        //聴力
                        if ("02".equals(mdd._rEar)) {
                            svf.VrsOut("R_EAR_DB", "○" + StringUtils.defaultString(mdd._rEarDb, "  ") + "dB");
                            svf.VrsOut("R_EAR", "（" + StringUtils.defaultString(mdd._rEarDb4000, "  ") + "dB）");
                        } else if ("01".equals(mdd._rEar)) {
                            printSlash(svf, "SLASH_R_EAR");
                        } else {
                            svf.VrsOut("R_EAR_DB", mdd._rEarDb);
                            svf.VrsOut("R_EAR", _param.getNameMstName1("F010", mdd._rEar));
                        }
                        if ("02".equals(mdd._lEar)) {
                            svf.VrsOut("L_EAR_DB", "○" + StringUtils.defaultString(mdd._lEarDb, "  ") + "dB");
                            svf.VrsOut("L_EAR", "（" + StringUtils.defaultString(mdd._lEarDb4000, "  ") + "dB）");
                        } else if ("01".equals(mdd._lEar)) {
                            printSlash(svf, "SLASH_L_EAR");
                        } else {
                            svf.VrsOut("L_EAR_DB", mdd._lEarDb);
                            svf.VrsOut("L_EAR", _param.getNameMstName1("F010", mdd._lEar));
                        }

                        // 耳鼻咽喉疾患
                        if ("01".equals(mdd._nosediseasecd)) {
                            printSlash(svf, "SLASH_NOSEDISEASE");
                        } else {
                            final String nosedisease = getNoseDisease(mdd);
                            final int fieldSize = Util.getMS932ByteLength(nosedisease) > 40 ? 32 : 20;
                            final String fieldSoeji = Util.getMS932ByteLength(nosedisease) > 40 ? "2_" : "1_";
                            final List token = KNJ_EditKinsoku.getTokenList(nosedisease, fieldSize);
                            for (int j = 0; j < token.size(); j++) {
                                svf.VrsOut("NOSEDISEASE" + fieldSoeji + (j + 1), (String) token.get(j));
                            }
                        }

                        // 皮膚疾患
                        if ("01".equals(mdd._skindiseasecd)) {
                            printSlash(svf, "SLASH_SKINDISEASE");
                        } else {
                            final String text;
                            if ("99".equals(mdd._skindiseasecd)) {
                                // その他はコード名称を表示しない
                                text = mdd._skindiseasecdRemark;
                            } else {
                                text = Util.mkString(new String[] {_param.getNameMstName1("F070", mdd._skindiseasecd), mdd._skindiseasecdRemark}, " ");
                            }
                            if (Util.getMS932ByteLength(text) >= 20) {
                                svf.VrsOut("SKINDISEASE2_1", text);
                            } else {
                                svf.VrsOut("SKINDISEASE", text);
                            }
                        }

                        // 結核
                        // 撮影日
                        svf.VrsOut("PHOTO_DATE", KNJ_EditDate.h_format_JP(mdd._tbFilmdate));
                        // 画像番号
                        svf.VrsOut("FILMNO", mdd._tbFilmno);
                        // 所見
                        setName1WithSlash(svf, "VIEWS1_1", "F100", mdd._tbRemarkcd, "SLASH_VIEWS1_1");
                        svf.VrsOut("TB_X_RAY1", mdd._tbXRay);
                        // その他の検査
                        setName1WithSlash(svf, "OTHERS", "F110", mdd._tbOthertestcd, "SLASH_OTHERS");
                        // 病名
                        setName1WithSlash(svf, "DISEASE_NAME", "F120", mdd._tbNamecd, "SLASH_DISEASE_NAME");
                        // 指導区分
                        setName1WithSlash(svf, "GUIDANCE", "F130", mdd._tbAdvisecd, null);

                        //心臓
                        // 臨床医学検査(心電図)
                        setHeartMedExam(svf, mdd._heartMedexamRemark, mdd._heartMedexam, form);
                        // 疾病及び異常
                        setHeartdiseasecd(svf, mdd._heartdiseasecdRemark, mdd._heartdiseasecd, form);

                        //尿
                        setName1WithSlash(svf, "ALBUMINURIA", "F020", mdd._albuminuria1cd, null);
                        setName1WithSlash(svf, "URICSUGAR", "F019", mdd._uricsugar1cd, null);
                        setName1WithSlash(svf, "URICBLEED", "F018", mdd._uricbleed1cd, null);
                        setName1WithSlash(svf, "ALBUMINURIA2", "F020", mdd._albuminuria2cd, null);
                        setName1WithSlash(svf, "URICSUGAR2", "F019", mdd._uricsugar2cd, null);
                        setName1WithSlash(svf, "URICBLEED2", "F018", mdd._uricbleed2cd, null);

                        svf.VrsOut("URINE_OTHERS" + (Util.getMS932ByteLength(mdd._uricothertest) > 20 ? "1" : ""), mdd._uricothertest);
                        setName1WithSlash(svf, "URI_ADVISECD", "F021", mdd._uriAdvisecd, null);

                        // その他の疾病及び異常
                        setName1WithSlash(svf, "OTHERDISEASE", "F140", mdd._otherdiseasecd, "SLASH_OTHERDISEASE");
                        if ("1".equals(printKenkouSindanIppan)) {
                            setName1WithSlash(svf, "OTHER_ADVISE", "F145", mdd._otherAdvisecd, "SLASH_OTHERDISEASE");
                        }
                        svf.VrsOut("OTHER_ADVISECD2", mdd._otherRemark);

                        // 学校医
                        // 所見
                        svf.VrsOut("VIEWS2_1", StringUtils.defaultString(mdd._docRemark, ""));
                        // 月日
                        svf.VrsOut("DOC_DATE", KNJ_EditDate.h_format_JP(mdd._docDate));
                        // 印
                        if (null != regdDat._medexamDetDat._docRemark && null != regdDat._medexamDetDat._docDate && null != _param._doctorPrint) {
                            svf.VrsOut("STAFFBTMC", _param.getStampImageFile(regdDat._year, IPPAN_1));
                        }

                        // 事後措置
                        setName1WithSlash(svf, "DOC_TREAT1", "F150", mdd._treatcd, "SLASH_DOC_TREAT");

                        // 備考
                        if (null != mdd._sitheight) {
                            final int maxKeta = 20;
                            String remark1 = "座高（" + mdd._sitheight + "cm）";
                            remark1 += StringUtils.repeat(" ", maxKeta - Util.getMS932ByteLength(remark1));
                            remark1 += StringUtils.defaultString(mdd._remark);
                            svf.VrsOut("NOTE1"     ,  remark1);
                        } else {
                            svf.VrsOut("NOTE1"     ,  mdd._remark);
                        }
                    }
                    dataCnt++;

                    svf.VrEndRecord();
                    _hasData = true;
                }
            }
        }

        private void printSlash(final Vrw32alp svf, final String field) {
            final String slashFile = _param._slashJpgFilePath;
            if (null != slashFile) {
                svf.VrsOut(field, slashFile);
            }
        }

        private void setHeartMedExam(final Vrw32alp svf, final String heartMedexamRemark, final String heartMedexam, final String form) throws SQLException {
//            log.debug(" heartMedexamRemark = [" + heartMedexamRemark + "] keta = [" + getMS932ByteLength(heartMedexamRemark) + "], heartMedexam = " + heartMedexam);
            if ("1".equals(_param._useKnjf030AHeartBiko)) {
                if (null == heartMedexamRemark) {
                    setName1WithSlash(svf, "HEART_MEDEXAM", "F080", heartMedexam, "SLASH_HEART_MEDEXAM");
                } else {
                    final int remarklen = Util.getMS932ByteLength(heartMedexamRemark);
                    final int[] fieldlen;
                    final String[] fieldname;
                    final int ketaHEART_MEDEXAM    = 20;
                    final int ketaHEART_MEDEXAM_2  = 22;
                    final int ketaHEART_MEDEXAM2_1 = 26;
                    final int ketaHEART_MEDEXAM2_2 = 26;
                    final int ketaHEART_MEDEXAM2_3 = 26;
                    if (remarklen <= ketaHEART_MEDEXAM + ketaHEART_MEDEXAM_2) {
                        fieldlen = new int[] {ketaHEART_MEDEXAM, ketaHEART_MEDEXAM_2};
                        fieldname = new String[] {"HEART_MEDEXAM", "HEART_MEDEXAM_2"};
                    } else if (remarklen <= ketaHEART_MEDEXAM2_1 + ketaHEART_MEDEXAM2_2 + ketaHEART_MEDEXAM2_3) {
                        fieldlen = new int[] {ketaHEART_MEDEXAM2_1, ketaHEART_MEDEXAM2_2, ketaHEART_MEDEXAM2_3};
                        fieldname = new String[] {"HEART_MEDEXAM2_1", "HEART_MEDEXAM2_2", "HEART_MEDEXAM2_3"};
                    } else {
                        fieldlen = null;
                        fieldname = null;
                    }
                    if (null != fieldlen && null != fieldname) {
                        Util.svfVrsOutArray(svf, fieldname, Util.getMS932ByteToken(heartMedexamRemark, fieldlen));
                    } else {
                        svf.VrsOut("HEARTDISEASECD_REMARK", heartMedexamRemark); // 42 * 2
                    }
                }
            } else {
                final String remark = heartMedexamRemark;
                final int remarklen = Util.getMS932ByteLength(remark);
                // log.debug(" heartmedexam remarklen = " + remarklen);
                final int[] fieldlen;
                final String[] fieldname;
                final String cdfield;
                if (remarklen <= 22) {
                    cdfield = "HEART_MEDEXAM";
                    fieldlen = new int[] {22};
                    fieldname = new String[] {"HEART_MEDEXAM_2"};
                } else if (remarklen <= 26 * 2) {
                    cdfield = "HEART_MEDEXAM2_1";
                    fieldlen = new int[] {26, 26};
                    fieldname = new String[] {"HEART_MEDEXAM2_2", "HEART_MEDEXAM2_3"};
                } else {
                    cdfield = "HEART_MEDEXAM2_1";
                    fieldlen = null;
                    fieldname = null;
                }
                setName1WithSlash(svf, cdfield, "F080", heartMedexam, "SLASH_HEART_MEDEXAM");
                if (null != fieldlen && null != fieldname) {
                    Util.svfVrsOutArray(svf, fieldname, Util.getMS932ByteToken(remark, fieldlen));
                } else {
                    svf.VrsOut("HEARTDISEASECD_REMARK", remark); // 42 * 2
                }
            }
        }

        private void setHeartdiseasecd(final Vrw32alp svf, final String heartdiseasecdRemark, final String heartdiseasecd, final String form) throws SQLException {
//            log.debug(" heartdiseasecdRemark = [" + heartdiseasecdRemark + "] keta = [" + getMS932ByteLength(heartdiseasecdRemark) + "], heartdiseasecd = " + heartdiseasecd);
            if ("2".equals(printKenkouSindanIppan)) {
                if (isSonota("F090", heartdiseasecd)) {
                    svf.VrsOut("HEARTDISEASE_REMARK1", heartdiseasecdRemark);
                } else {
                    setName1WithSlash(svf, "HEARTDISEASE1", "F090", heartdiseasecd, "SLASH_HEARTDISEASE");
                }
            } else if ("1".equals(_param._useKnjf030AHeartBiko)) {
                if (null == heartdiseasecdRemark) {
                    setName1WithSlash(svf, "HEARTDISEASE1", "F090", heartdiseasecd, "SLASH_HEARTDISEASE");
                } else {
                    final int remarklen = Util.getMS932ByteLength(heartdiseasecdRemark);
                    final int ketaHEARTDISEASE1   = 20;
                    final int ketaHEARTDISEASE1_2 = 22;
                    final int ketaHEARTDISEASE2_1 = 26;
                    final int ketaHEARTDISEASE2_2 = 26;
                    final int ketaHEARTDISEASE2_3 = 26;
                    final int[] fieldlen;
                    final String[] fieldname;
                    if (remarklen <= ketaHEARTDISEASE1 + ketaHEARTDISEASE1_2) {
                        fieldlen = new int[] {ketaHEARTDISEASE1, ketaHEARTDISEASE1_2};
                        fieldname = new String[] {"HEARTDISEASE1", "HEARTDISEASE1_2"};
                    } else if (remarklen <= ketaHEARTDISEASE2_1 + ketaHEARTDISEASE2_2 + ketaHEARTDISEASE2_3) {
                        fieldlen = new int[] {ketaHEARTDISEASE2_1, ketaHEARTDISEASE2_2, ketaHEARTDISEASE2_3};
                        fieldname = new String[] {"HEARTDISEASE2_1", "HEARTDISEASE2_2", "HEARTDISEASE2_3"};
                    } else {
                        fieldlen = null;
                        fieldname = null;
                    }
                    if (null != fieldlen && null != fieldname) {
                        Util.svfVrsOutArray(svf, fieldname, Util.getMS932ByteToken(heartdiseasecdRemark, fieldlen));
                    } else {
                        svf.VrsOut("HEARTDISEASE2", heartdiseasecdRemark); // 42
                    }
                }
            } else {
                final String kakkoHeartdiseasecdRemark = heartdiseasecdRemark;
                final int remarklen = Util.getMS932ByteLength(kakkoHeartdiseasecdRemark);
                final int ketaHEARTDISEASE1 = 22;
                final int[] fieldlen;
                final int ketaHEARTDISEASE2_2 = 26;
                final int ketaHEARTDISEASE2_3 = 26;
                if (remarklen <= ketaHEARTDISEASE1) {
                    setName1WithSlash(svf, "HEARTDISEASE1", "F090", heartdiseasecd, "SLASH_HEARTDISEASE");
                    fieldlen = new int[] {ketaHEARTDISEASE1};
                    Util.svfVrsOutArray(svf, new String[] {"HEARTDISEASE1_2"}, Util.getMS932ByteToken(kakkoHeartdiseasecdRemark, fieldlen));
                } else {
                    setName1WithSlash(svf, "HEARTDISEASE2_1", "F090", heartdiseasecd, "SLASH_HEARTDISEASE");
                    if (remarklen <= ketaHEARTDISEASE2_2 + ketaHEARTDISEASE2_3) {
                        fieldlen = new int[] {ketaHEARTDISEASE2_2, ketaHEARTDISEASE2_3};
                        Util.svfVrsOutArray(svf, new String[] {"HEARTDISEASE2_2", "HEARTDISEASE2_3"}, Util.getMS932ByteToken(kakkoHeartdiseasecdRemark, fieldlen));
                    } else {
                        svf.VrsOut("HEARTDISEASE2", kakkoHeartdiseasecdRemark); // 42
                    }
                }
            }
        }

        /**
         * コードが「その他」（名称マスタの予備2が'1'）ならtrue、それ以外はfalse
         */
        private boolean isSonota(
                final String nameCd1,
                final String nameCd2
        ) throws SQLException {
            if ("1".equals(_param.getNameMst(nameCd1, nameCd2, "NAMESPARE2"))) {
                return true;
            }
            return false;
        }

        private void setName1WithSlash(
                final Vrw32alp svf,
                final String fieldName,
                final String nameCd1,
                final String nameCd2,
                final String slashField
        ) throws SQLException {
//            if (null != slashField && "1".equals(_param.getNameMst(nameCd1, nameCd2, "NAMESPARE1"))) {
//                printSlash(slashField);
//                return;
//            }
            if (null != slashField && "01".equals(nameCd2)) {
                printSlash(svf, slashField);
                return;
            }
            if (null == nameCd2) {
                return;
            }
            if ("1".equals(_param._kenkouSindanIppanNotPrintNameMstComboNamespare2Is1) && "1".equals(_param.getNameMst(nameCd1, nameCd2, "NAMESPARE2"))) {
                return;
            }
            svf.VrsOut(fieldName, getName(nameCd1, nameCd2, NAME1, ""));
        }

        private String getName(final String nameCd1, final String nameCd2, final String useFieldName, final String plusString) throws SQLException {
            if ("1".equals(_param._kenkouSindanIppanNotPrintNameMstComboNamespare2Is1) && "1".equals(_param.getNameMst(nameCd1, nameCd2, "NAMESPARE2"))) {
                return "";
            }
            final String sql = "SELECT VALUE(" + useFieldName + ", '') AS LABEL, NAMESPARE2 FROM NAME_MST WHERE NAMECD1 = '" + nameCd1 + "' AND NAMECD2 = '" + nameCd2 + "'";
            ResultSet rs = null;
            PreparedStatement ps = null;
            String retStr = "";
            try {
                ps = _db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    retStr = (!NumberUtils.isDigits(nameCd2) ? "" : rs.getString("LABEL")) + plusString;
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                _db2.commit();
            }
            return retStr;
        }

        private String getNoseDisease(final MedexamDetDat medexamDetDat) throws SQLException {
            String noseDisease = concatWith(new String[] {
                    getTextOrName(medexamDetDat._nosediseasecdRemark1, "F061", medexamDetDat._nosediseasecd5, NAME1, "")
                  , getTextOrName(medexamDetDat._nosediseasecdRemark2, "F062", medexamDetDat._nosediseasecd6, NAME1, "")
                  , getTextOrName(medexamDetDat._nosediseasecdRemark3, "F063", medexamDetDat._nosediseasecd7, NAME1, "")
                  , medexamDetDat._nosediseasecdRemark
            }, "、");
            if (StringUtils.isEmpty(noseDisease)) {
                noseDisease = getName("F060", medexamDetDat._nosediseasecd, NAME1, "");
            }
            return noseDisease;
        }

        private String getTextOrName(final String text, final String nameCd1, final String nameCd2, final String useFieldName, final String plusString) throws SQLException {
            if (null != text && text.length() > 0) {
                return text;
            }
            return getName(nameCd1, nameCd2, useFieldName, plusString);
        }

        /**
         * 配列内のブランクでない文字列をコンマで連結する
         * @param array 配列
         * @param comma コンマ
         * @return 連結した文字列
         */
        private String concatWith(final String[] array, final String comma) {
            final StringBuffer stb = new StringBuffer();
            if (null != array) {
                for (int i = 0; i < array.length; i++) {
                    if (null == array[i]) {
                        continue;
                    }
                    if (stb.length() > 0 && array[i].length() > 0) {
                        stb.append(comma);
                    }
                    stb.append(array[i]);
                }
            }
            return stb.toString();
        }
    }

    private static class HexamPhysicalAvgDat {
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
            _nenreiYear      = nenreiYear;
            _nenreiMonth     = nenreiMonth;
            _nenrei          = _nenreiYear + (_nenreiMonth / 12.0);
            _heightAvg       = heightAvg;
            _heightSd        = heightSd;
            _weightAvg       = weightAvg;
            _weightSd        = weightSd;
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
                    final String sex                 = rs.getString("SEX");
                    final int nenreiYear            = rs.getInt("NENREI_YEAR");
                    final int nenreiMonth           = rs.getInt("NENREI_MONTH");
                    // if (ageMonth % 3 != 0) { continue; }
                    final BigDecimal heightAvg       = rs.getBigDecimal("HEIGHT_AVG");
                    final BigDecimal heightSd        = rs.getBigDecimal("HEIGHT_SD");
                    final BigDecimal weightAvg       = rs.getBigDecimal("WEIGHT_AVG");
                    final BigDecimal weightSd        = rs.getBigDecimal("WEIGHT_SD");
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

    /**
     * <<ローダー>>。
     */
    private static class DB {

        /**
         * queryしたリストの最初のレコード（Map）のカラム1列目を返す。Listが空の場合、nullを返す。
         * @param list queryしたリスト
         * @return queryしたリストの最初のレコード（Map）のカラム1列目。Listが空の場合、null。
         */
        public static String getOne(final List list) {
            return (String) firstRow(list).get(new Integer(1));
        }

        /**
         * キーカラム値と値カラム値のマップを得る
         * @param recordList レコードのリスト
         * @param keyColumn キーカラム
         * @param valueColumn 値カラム
         * @return　キーカラム値と値カラム値のマップ
         */
        public static Map getColumnValMap(final List recordList, final String keyColumn, final String valueColumn) {
            final Map rtn = new HashMap();
            for (final Iterator it = recordList.iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                rtn.put(getString(row, keyColumn), getString(row, valueColumn));
            }
            return rtn;
        }

        /**
         * 指定カラムのデータのリストを得る
         * @param rowList レコードのリスト
         * @param column 指定カラム
         * @return　指定カラムのデータのリスト
         */
        public static List getColumnDataList(final List rowList, final String column) {
            final List rtn = new ArrayList();
            for (final Iterator it = rowList.iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                rtn.add(row.get(column));
            }
            return rtn;
        }

        /**
         * 指定カラムのデータのリストを得る
         * @param idx インデクス
         * @param columnList レコードのリスト
         * @param rowList 行リスト
         * @return　指定カラムのデータのリスト
         */
        public static Map getColumnGroupByMap(final String[] columns, final List rowList) {
            final Map rtn = new TreeMap();
            for (final Iterator it = rowList.iterator(); it.hasNext();) {
                final Map row = (Map) it.next();

                Map parent = rtn;
                for (int i = 0; i < columns.length; i++) {
                    final String column = columns[i];

                    if (i == columns.length - 1) {
                        Util.getMappedList(parent, getString(row, column)).add(row);
                    } else {
                        parent = Util.getMappedMap(parent, getString(row, column));
                    }
                }
            }
            return rtn;
        }

        public static Map neRow() {
            return new HashMap();
        }

        public static Map resultSetToMap(final ResultSetMetaData meta, final ResultSet rs) throws SQLException {
            final Map map = neRow();
            for (int i = 0; i < meta.getColumnCount(); i++) {
                final String columnName = meta.getColumnName(i + 1);
                final String val = rs.getString(columnName);
                map.put(columnName, val);
                map.put(new Integer(i + 1), val);
            }
            return map;
        }

        /**
         * sqlを発行した結果のレコード（Map）のリストを得る
         * @param db2 DB2
         * @param ps statement
         * @param parameter パラメータ。ない場合はnull
         * @return レコードのリスト
         */
        public static List query(final DB2UDB db2, final PreparedStatement ps, final String[] parameter) {
            final List rowList = new ArrayList();
            ResultSet rs = null;
            try {
                if (null != parameter) {
                    for (int i = 0; i < parameter.length; i++) {
                        ps.setString(i + 1, parameter[i]);
                    }
                }
                rs = ps.executeQuery();
                final ResultSetMetaData meta = rs.getMetaData();
                while (rs.next()) {
                    rowList.add(resultSetToMap(meta, rs));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(rs);
            }
            return rowList;
        }

        /**
         * sqlを発行した結果のレコード（Map）のリストを得る
         * @param db2 DB2
         * @param sql SQL
         * @param parameter パラメータ。ない場合はnull
         * @return レコードのリスト
         */
        public static List query(final DB2UDB db2, final String sql, final String[] parameter) {
            final List rowList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                if (null != parameter && parameter.length > 0) {
                    for (int i = 0; i < parameter.length; i++) {
                        ps.setString(i + 1, parameter[i]);
                    }
                }
                rs = ps.executeQuery();
                final ResultSetMetaData meta = rs.getMetaData();
                while (rs.next()) {
                    rowList.add(resultSetToMap(meta, rs));
                }
            } catch (Exception e) {
                log.error("exception! sql=" + sql, e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rowList;
        }

        /**
         * sqlを発行した結果のレコード（Map）のリストを得る
         * @param db2 DB2
         * @param sql SQL
         * @return レコードのリスト
         */
        public static List query(final DB2UDB db2, final String sql) {
            return query(db2, sql, null);
        }

        public static Map firstRow(final List rowList) {
            if (rowList.isEmpty()) {
                return neRow();
            }
            return (Map) rowList.get(0);
        }

        public static Map LastRow(final List rowList) {
            if (rowList.isEmpty()) {
                return neRow();
            }
            return (Map) rowList.get(rowList.size() - 1);
        }
    }

    private static class Param {
        final String _year;
        final String _gakki;
        final String _kubun;
        final String[] _classSelected;
        final List _checkList = new ArrayList();
        final String _date;             // 作成日付
        final String _presentDate;      // 提出日
        final String _inspectionFrom;   // 検査日時from
        final String _inspectionTo;     // 検査日時to
        String _inspectionBody = "";     // 検査機関
        final String _doctorPrint;      // 医師印を表示する

        final String _ctrlDate;
        private String _namemstZ010Name1;
        private Map _yearKouiStampNo;       // 学校医印鑑
        private Map _yearToothKouiStampNo;  // 学校歯科医印鑑
        private KNJ_Schoolinfo.ReturnVal _returnval2;
        private String _certifSchoolDatSchoolName;
        private String _certifSchoolDatJobName;
        private String _certifSchoolDatPrincipalName;
        private String _certifSchoolDatRemark1;
        private String _certifSchoolDatRemark2;
        private String _certifSchoolDatRemark3;
        private String _certifSchoolDatRemark4;
        private String _certifSchoolDatRemark5;
        private String _certifSchoolDatRemark6;
        private String _certifSchoolDatRemark7;
        private String _certifSchoolDatRemark8;
        private String _staffname;
        private int _staffnameKeta;
        private String _charge; // 担当情報
        final String _useSchool_KindField;
        final String _SCHOOLKIND;
        final Map _formFieldInfoMapMap = new HashMap();

        KNJDefineSchool _defineSchool;

        /** 写真データ格納フォルダ */
        private final String _imageDir;
        /** 写真データの拡張子 */
        private final String _imageExt;
        /** 陰影保管場所(陰影出力に関係する) */
        private final String _documentRoot;
        final String _imagepath;
        final Map _schoolKindImagePathMap = new HashMap();

        private final Map _nameMstFXX;
        private final Map _documentMstMap;
        private Map _physAvgMap = null;

        final String _useKnjf030AHeartBiko;
        /** 名称マスタのコンボで名称予備2=1は表示しない */
        final String _kenkouSindanIppanNotPrintNameMstComboNamespare2Is1;
        final String _slashJpgFilePath;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            //  パラメータの取得
            _year     = request.getParameter("YEAR");                        //年度
            _gakki    = request.getParameter("GAKKI");                       //学期 1,2,3
            _ctrlDate = request.getParameter("CTRL_DATE");                   //ログイン日付
            _kubun    = request.getParameter("KUBUN");                       //1:クラス,2:個人
            _classSelected = request.getParameterValues("CLASS_SELECTED");
            if ("2".equals(_kubun)) {
                for (int i = 0; i < _classSelected.length; i++) {
                    _classSelected[i] = StringUtils.split(_classSelected[i], "-")[0];
                }
            }
            for (int checknoi = 1; checknoi <= 29; checknoi++) {
                if ("on".equals(request.getParameter("CHECK" + String.valueOf(checknoi)))) {
                    _checkList.add(new Integer(checknoi));
                }
            }

            _date           = request.getParameter("DATE");
            _presentDate    = request.getParameter("PRESENT_DATE");
            _inspectionFrom = request.getParameter("INSPECTION_FROM");
            _inspectionTo   = request.getParameter("INSPECTION_TO");
            try {
                _inspectionBody = new String(request.getParameter("INSPECTION_BODY").getBytes("ISO8859-1"));
            } catch (Exception e) {
                log.error("exception!", e);
            }
            _doctorPrint    = request.getParameter("DOCTOR_PRINT");

            _useKnjf030AHeartBiko = request.getParameter("useKnjf030AHeartBiko");
            _kenkouSindanIppanNotPrintNameMstComboNamespare2Is1 = request.getParameter("kenkouSindanIppanNotPrintNameMstComboNamespare2Is1");

            _defineSchool = new KNJDefineSchool();
            _defineSchool.defineCode(db2, _year);

            //  学校名・学校住所・校長名の取得
            _returnval2 = new KNJ_Schoolinfo(_year).get_info(db2);

            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLKIND          = request.getParameter("SCHOOLKIND");
            _namemstZ010Name1    = getNameMstZ010(db2);
            log.info(" _namemstZ010Name1 = " + _namemstZ010Name1);

            _documentRoot     = request.getParameter("DOCUMENTROOT"); // 陰影保管場所 NO001
            _imagepath        = request.getParameter("IMAGEPATH");
            _imageDir         = "image";
            _slashJpgFilePath = getImageFile("slash.jpg");
            _imageExt = "bmp";
            setCertifSchoolDat(db2);
            _yearToothKouiStampNo = getToothKouiStampMap(db2);

            _nameMstFXX = getNameMstFXX(db2);
            _documentMstMap = getDocumentMstMap(db2);
        }

        public String getSchoolStampImageFilePath(final String schoolKind) {
        	if (!_schoolKindImagePathMap.containsKey(schoolKind)) {
        		_schoolKindImagePathMap.put(schoolKind, getImageFile(_imagepath, "SCHOOLSTAMP_" + schoolKind + ".bmp"));
        	}
			return (String) _schoolKindImagePathMap.get(schoolKind);
		}

		private Map getFieldInfoMap(final Vrw32alp svf, final String form) {
            if (null == _formFieldInfoMapMap.get(form)) {
                _formFieldInfoMapMap.put(form, SvfField.getSvfFormFieldInfoMapGroupByName(svf));
            }
            return Util.getMappedMap(_formFieldInfoMapMap, form);
        }

        private static int getFieldKeta(final Map fieldnameMap, final String fieldname) {
            int rtn = 0;
            final SvfField fieldHrName = (SvfField) fieldnameMap.get(fieldname);
            if (null == fieldHrName) {
                log.info("not found svf field: " + fieldname);
            } else {
                 rtn = fieldHrName._fieldLength;
            }
            return rtn;
        }

        private String getNameMstName1(final String namecd1, final String namecd2) {
            return getNameMst(namecd1, namecd2, "NAME1");
        }

        private String getNameMst(final String namecd1, final String namecd2, final String field) {
            return getString(DB.firstRow(Util.getMappedList(Util.getMappedMap(_nameMstFXX, namecd1), namecd2)), field);
        }

        private String getSchoolInfo(final String schoolKind, final String field) {
            final Map map = new HashMap();
            if ("H".equals(schoolKind)) {
                map.put(SCHOOL_NAME1, _certifSchoolDatRemark8);         //学校名１
                map.put(SCHOOL_NAME2, _certifSchoolDatSchoolName);      //学校名２
                map.put(PRINCIPAL_NAME, _certifSchoolDatPrincipalName); //校長名
                map.put(PRINCIPAL_JOBNAME, _certifSchoolDatJobName);
            } else if ("J".equals(schoolKind)) {
                map.put(SCHOOL_NAME1, _certifSchoolDatRemark8);   //学校名１
                map.put(SCHOOL_NAME2, _certifSchoolDatRemark1);   //学校名２
                map.put(PRINCIPAL_NAME, _certifSchoolDatRemark2); //校長名
                map.put(PRINCIPAL_JOBNAME, _certifSchoolDatRemark3);
            } else if ("P".equals(schoolKind) || "K".equals(schoolKind)) {
                map.put(SCHOOL_NAME1, _certifSchoolDatRemark8);   //学校名１
                map.put(SCHOOL_NAME2, _certifSchoolDatRemark4);   //学校名２
                map.put(PRINCIPAL_NAME, _certifSchoolDatRemark5); //校長名
                map.put(PRINCIPAL_JOBNAME, _certifSchoolDatRemark6);
            }
            if (null == map.get(SCHOOL_NAME1)) map.put(SCHOOL_NAME1, _returnval2.SCHOOL_NAME1);       //学校名１
            if (null == map.get(SCHOOL_NAME2)) map.put(SCHOOL_NAME2, _returnval2.SCHOOL_NAME2);       //学校名２
            if (null == map.get(PRINCIPAL_NAME)) map.put(PRINCIPAL_NAME, _returnval2.PRINCIPAL_NAME); //校長名
            if (null == map.get(PRINCIPAL_JOBNAME)) map.put(PRINCIPAL_JOBNAME, _returnval2.PRINCIPAL_JOBNAME);
            return (String) map.get(field);
        }

        private Map getNameMstFXX(final DB2UDB db2) {
            final String sql = "SELECT NAMECD1, NAMECD2, NAME1, NAME2, NAME3, NAMESPARE1, NAMESPARE2, NAMESPARE3 FROM NAME_MST WHERE NAMECD1 LIKE 'F%' ";
            return DB.getColumnGroupByMap(new String[] {"NAMECD1", "NAMECD2"}, DB.query(db2, sql));
        }

        private String getNameMstZ010(final DB2UDB db2) {
            return DB.getOne(DB.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' AND NAME1 IS NOT NULL "));
        }

        private Map getToothKouiStampMap(final DB2UDB db2) {
            Map retMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" WITH T_INKAN AS ( ");
                stb.append("     SELECT ");
                stb.append("         MAX(STAMP_NO) AS STAMP_NO, ");
                stb.append("         STAFFCD ");
                stb.append("     FROM ");
                stb.append("         ATTEST_INKAN_DAT ");
                stb.append("     GROUP BY ");
                stb.append("         STAFFCD ");
                stb.append(" ) ");
                stb.append(" SELECT T1.YEAR, T2.STAMP_NO ");
                stb.append(" FROM CERTIF_SCHOOL_DAT T1 ");
                stb.append(" LEFT JOIN T_INKAN T2 ON T2.STAFFCD = T1.REMARK10 ");
                stb.append(" WHERE CERTIF_KINDCD = '124' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                  retMap.put(rs.getString("YEAR"), rs.getString("STAMP_NO"));
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        private void setCertifSchoolDat(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            _yearKouiStampNo = new HashMap();
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" WITH T_INKAN AS ( ");
                stb.append("     SELECT ");
                stb.append("         MAX(STAMP_NO) AS STAMP_NO, ");
                stb.append("         STAFFCD ");
                stb.append("     FROM ");
                stb.append("         ATTEST_INKAN_DAT ");
                stb.append("     GROUP BY ");
                stb.append("         STAFFCD ");
                stb.append(" ) ");
                stb.append(" SELECT T1.YEAR, T2.STAMP_NO ");
                stb.append(" FROM CERTIF_SCHOOL_DAT T1 ");
                stb.append(" LEFT JOIN T_INKAN T2 ON T2.STAFFCD = T1.REMARK5 ");
                stb.append(" WHERE CERTIF_KINDCD = '124' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    _yearKouiStampNo.put(rs.getString("YEAR"), rs.getString("STAMP_NO"));
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT * ");
            stb.append(" FROM CERTIF_SCHOOL_DAT T1 ");
            stb.append(" WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '125' ");

            final Map row = DB.firstRow(DB.query(db2, stb.toString()));

            _certifSchoolDatSchoolName      = getString(row, "SCHOOL_NAME");
            _certifSchoolDatJobName         = getString(row, "JOB_NAME");
            _certifSchoolDatPrincipalName   = getString(row, "PRINCIPAL_NAME");
            _certifSchoolDatRemark1         = getString(row, "REMARK1");
            _certifSchoolDatRemark2         = getString(row, "REMARK2");
            _certifSchoolDatRemark3         = getString(row, "REMARK3");
            _certifSchoolDatRemark4         = getString(row, "REMARK4");
            _certifSchoolDatRemark5         = getString(row, "REMARK5");
            _certifSchoolDatRemark6         = getString(row, "REMARK6");
            _certifSchoolDatRemark7         = getString(row, "REMARK7");
            _certifSchoolDatRemark8         = getString(row, "REMARK8");
            _staffname                      = StringUtils.defaultString(_certifSchoolDatJobName) + StringUtils.defaultString(_certifSchoolDatPrincipalName);
            _staffnameKeta                  = Util.getMS932ByteLength(_staffname);
            _charge                         = StringUtils.defaultString(_certifSchoolDatRemark7);
        }

        private String getDocumentMstTitle(final String docuemntcd) {
            return getString(DB.firstRow(Util.getMappedList(_documentMstMap, docuemntcd)), "TITLE");
        }
        private String getDocumentMstText(final String docuemntcd) {
            return getString(DB.firstRow(Util.getMappedList(_documentMstMap, docuemntcd)), "TEXT");
        }
        private String getDocumentMstCertifNo(final String docuemntcd) {
            return getString(DB.firstRow(Util.getMappedList(_documentMstMap, docuemntcd)), "CERTIF_NO");
        }
        private Map getDocumentMstMap(final DB2UDB db2) {
            final String sql = " SELECT DOCUMENTCD, TITLE, CERTIF_NO, TEXT FROM DOCUMENT_MST ";
            return DB.getColumnGroupByMap(new String[] {"DOCUMENTCD"}, DB.query(db2, sql));
        }

        /**
         * 写真データファイルの取得
         */
        public String getImageFile(final String filename) {
            return getImageFile(_imageDir, filename);
        }

        /**
         * 写真データファイルの取得
         */
        private String getImageFile(final String imageDir, final String filename) {
            if (null == _documentRoot) {
                return null;
            } // DOCUMENTROOT
            final StringBuffer stb = new StringBuffer();
            stb.append(_documentRoot);
            stb.append("/");
            stb.append(imageDir);
            stb.append("/");
            stb.append(filename);
            File file1 = new File(stb.toString());
            log.info(" filename = " + file1.toString() + ", exists = " + file1.exists());
            if (!file1.exists()) {
                return null;
            } // 写真データ存在チェック用
            return stb.toString();
        }

        /**
         * 写真データファイルの取得
         */
        private String getStampImageFile(final String year, final String div) {
            if (null == year) {
                return null;
            }
            if (null == _documentRoot) {
                return null;
            } // DOCUMENTROOT
            if (null == _imageDir) {
                return null;
            }
            if (null == _imageExt) {
                return null;
            }
            String filename = "";
            if (IPPAN_1.equals(div)) {
                filename = (String) _yearKouiStampNo.get(year);
            } else {
                filename = (String) _yearToothKouiStampNo.get(year);
            }
            if (null == filename) {
                return null;
            }
            final StringBuffer stb = new StringBuffer();
            stb.append(_documentRoot);
            stb.append("/");
            stb.append(_imageDir);
            stb.append("/");
            stb.append(filename);
            stb.append(".");
            stb.append(_imageExt);
            File file1 = new File(stb.toString());
            if (!file1.exists()) {
                return null;
            } // 写真データ存在チェック用
            return stb.toString();
        }
    }

}//クラスの括り
