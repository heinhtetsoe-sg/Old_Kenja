// kanji=漢字
/*
 * $Id: 9d676459fbe207d813b6247841b4b842666d5b69 $
 *
 * 作成日: 2005/06/24
 * 作成者: nakamoto
 *
 * Copyright(C) 2005-2008 ALP Okinawa Co.,Ltd. All rights reserved.
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
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.SvfField;

/**
 *
 *  学校教育システム 賢者 [保健管理]
 *
 *                  ＜ＫＮＪＦ０３０Ｄ＞  三重県　健診のお知らせ一覧
 */
public class KNJF030D {

    private static final Log log = LogFactory.getLog(KNJF030D.class);

    private static final String SCHOOL_NAME1 = "SCHOOL_NAME1";
    private static final String SCHOOL_NAME2 = "SCHOOL_NAME2";
    private static final String PRINCIPAL_NAME = "PRINCIPAL_NAME";
    private static final String PRINCIPAL_JOBNAME = "PRINCIPAL_JOBNAME";

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

        log.fatal("$Revision: 77285 $"); // CVSキーワードの取り扱いに注意
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
            if (n == 1) {
                _hasdata = new KNJF030A().printMain(svf, db2, request, _param, studentList) || _hasdata;
            } else if (n == 2) { print2(db2, svf, studentList);
            } else if (n == 3) { print3(db2, svf, studentList);
            } else if (n == 4) { print4(db2, svf, studentList);
            } else if (n == 5) { print5(db2, svf, studentList);
            } else if (n == 6) { print6(db2, svf, studentList);
            } else if (n == 7) { print7(db2, svf, studentList);
            } else if (n == 8) { print8(db2, svf, studentList);
            } else if (n == 9) { print9(db2, svf, studentList);
            } else if (n == 10) { print10(db2, svf, studentList);
            } else if (n == 11) { print11(db2, svf, studentList);
            } else if (n == 12) { print12(db2, svf, studentList);
            } else if (n == 13) { print13(svf, studentList);
            } else if (n == 14) { print14(svf, studentList);
            } else if (n == 15 || n == 16 || n == 17) { print15_16_17(n, db2, svf, studentList);
            } else if (n == 18) { print18(db2, svf, studentList);
            } else if (n == 19) { print19(db2, svf, studentList);
            } else if (n == 20) { print20(db2, svf, studentList);
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
            int len = 0;
            if (null != str) {
                try {
                    len = str.getBytes("MS932").length;
                } catch (Exception e) {
                    log.error("exception!", e);
                }
            }
            return len;
        }

        private static int getMaxMS932ByteLength(final String[] strs) {
            int max = 0;
            for (int i = 0; i < strs.length; i++) {
                max = Math.max(max, getMS932ByteLength(strs[i]));
            }
            return max;
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

        private static int toInt(final String s, final int def) {
            if (!NumberUtils.isDigits(s)) {
                return def;
            }
            return Integer.parseInt(s);
        }

        private static double toDouble(final String s, final double def) {
            if (!NumberUtils.isNumber(s)) {
                return def;
            }
            return Double.parseDouble(s);
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

        private static String kakko(final String s) {
            if (null == s) {
                return s;
            }
            return "(" + s + ")";
        }
    }

    private void svfVrsOut(final Vrw32alp svf, final String form, final String[] fields, final String data) {
    	final int dataKeta = KNJ_EditEdit.getMS932ByteLength(data);
    	String lastField = null;
    	for (int i = 0; i < fields.length; i++) {
    		final int fieldKeta = Param.getFieldKeta(_param.getFieldInfoMap(svf, form), fields[i], 0);
    		if (fieldKeta > 0) {
    			lastField = fields[i];
    			if (dataKeta <= fieldKeta) {
    				break;
    			}
    		}
    	}
    	if (null != lastField) {
    		svf.VrsOut(lastField, data);
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

    private void print2(final DB2UDB db2, final Vrw32alp svf, final List studentList) throws SQLException {
        final int maxLine = 5;
        final String form;
        if (_param._isRisshisha) {
        	// 「歯列・咬合」
        	form = "KNJF030D_2_2.frm";
        } else if (_param._isMie) {
        	form = "KNJF030D_2_3.frm";
        } else {
        	// 「歯列」、「咬合」
        	form = "KNJF030D_2.frm";
        }
        log.info(" form = " + form);

        for (int sti = 0; sti < studentList.size(); sti++) {
            final Student student = (Student) studentList.get(sti);

            if (null == student._medexamToothDat) {
                continue;
            }

            svf.VrSetForm(form, 4);

            svf.VrsOut("SCHREGNO", student._schregno); // 学籍番号（マスク）
            svf.VrsOut("NAME_SHOW" + (Util.getMS932ByteLength(student._name) > 24 ? "_2" : ""), student._name); // 氏名
            if ("1".equals(_param._defineSchool.schooldiv)) {
                svf.VrsOut("GRADENAME_TITLE", "年度");
            } else {
                svf.VrsOut("GRADENAME_TITLE", "学年");
            }
            svf.VrsOut("NAME_HEADER", "名前"); // 名前ヘッダ
            svf.VrsOut("SEX", student._sex); // 性別
            svf.VrsOut("BIRTHDAY", KNJ_EditDate.h_format_JP(db2, student._birthDay)); // 生年月日

            final List regdList = RegdDat.getRegdList(db2, _param, student);
            for (int i = 0; i < Math.min(maxLine, regdList.size()); i++) {
                final int line = i + 1;
                final RegdDat regd = (RegdDat) regdList.get(i);
                if ("1".equals(_param._defineSchool.schooldiv)) {
                	if (NumberUtils.isDigits(regd._year)) {
                		svf.VrsOutn("GRADE_2", line, KNJ_EditDate.gengou(db2, Integer.parseInt(regd._year)) + "年度");
                	}
                } else {
                    svf.VrsOutn("GRADE", line, regd._gradeName);
                }
                svf.VrsOutn(Util.getMS932ByteLength(regd._hrClassName) > 8 ? "HR_NAME2_1" : "HR_NAME1", line, regd._hrClassName); // クラス
                svf.VrsOutn("ATTENDNO", line, regd._attendNo); // 出席番号
            }

            for (int i = 0; i < Math.min(maxLine, regdList.size()); i++) {

                final RegdDat regd = (RegdDat) regdList.get(i);

                svf.VrsOut("NENDO1", KNJ_EditDate.gengou(db2, Integer.parseInt(regd._year)).substring(0,2)); // 年度
                svf.VrsOut("NENDO2", KNJ_EditDate.gengou(db2, Integer.parseInt(regd._year)).substring(2)); // 年度
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
                    if (_param._isMie) {
                        if (split.length > 0) {
                            svf.VrsOut("DENTISTREMARK", split[0]); // 事後措置
                        }
                    	final String docName = KNJF030A.getDocName(db2, _param._year, student._schregno);
                        svf.VrsOut("rDENTISTREMARK", StringUtils.defaultString(docName, ""));
                    } else {
                        final String[] dentistremarkField = {"DENTISTREMARK", "rDENTISTREMARK"};
                        for (int di = 0; di < Math.min(split.length, dentistremarkField.length); di++) {
                            svf.VrsOut(dentistremarkField[di], split[di]); // 事後措置
                        }
                    }
                    svf.VrsOut("month", Util.month(mtd._dentistremarkdate)); // 学校歯科医・月
                    svf.VrsOut("day", Util.dayOfMonth(mtd._dentistremarkdate)); // 学校歯科医・日
                    svf.VrsOut("DENTISTTREAT", _param.getNameMstName1("F541", mtd._dentisttreatcd)); // 学校歯科医所見
                }

                svf.VrEndRecord();
                _hasdata = true;
            }
        }
    }

    private void printDocumentMstTitleText(final Vrw32alp svf, final String title, final List textList) {
        svf.VrsOut("TITLE", title);
        for (int i = 0; i < textList.size(); i++) {
            svf.VrsOut("TEXT" + String.valueOf(i + 1), (String) textList.get(i));
        }
    }

    private void printPrincipalStaffname(final Vrw32alp svf) {
        if ("on".equals(_param._hyoji1)) {
            svf.VrsOut(_param._staffnameKeta <= 26 ? "STAFF_NAME1" : _param._staffnameKeta <= 30 ? "STAFF_NAME2" : "STAFF_NAME3", _param._staffname);
        }
    }

    private void printAttention(final Vrw32alp svf) {
        if ("on".equals(_param._hyoji2)) {
            svf.VrsOut("ATTENTION", "【水泳（プール指導）の可否】　 　可　　　・　　　否");
        }
    }

    private void printInn(final Vrw32alp svf) {
        if ("on".equals(_param._hyoji3)) {
            svf.VrsOut("STAMP_NAME", "印");
        }
    }

    private void printGuardName(final Vrw32alp svf) {
        if ("on".equals(_param._hyoji4)) {
            svf.VrsOut("GUARD_NAME", "保護者名");
            svf.VrAttribute("GUARD_NAME", "UnderLine=(0,3,1),Keta=50");
        }
    }

    private boolean isNotPrint(final String div, final MedexamDetDat mdd, final MedexamToothDat mtd) {
        if (ArrayUtils.contains(new String[] {"03", "04", "05", "06", "10", "11", "12", "13", "15", "16", "17", "20_1", "20_2", "20_3"}, div)) {
            // データ無しを表示しない
            if (null == mdd) {
                return true;
            }
        }
        if (ArrayUtils.contains(new String[] {"07", "08"}, div)) {
            // データ無しを表示しない
            if (null == mtd) {
                return true;
            }
        }
        if (ArrayUtils.contains(new String[] {"09"}, div)) {
            // データ無しを表示しない
            if (null == mdd && null == mtd) {
                return true;
            }
        }
//        if (debug) { return false; }

        if ("03".equals(div)) {
            return (null == mdd._eyediseasecd || "01".equals(mdd._eyediseasecd));
        } else if ("04".equals(div)) {
            final String[] a = {"A", "a", "Ａ", "ａ"};
            if (
                (null == mdd._rBarevisionMark || ArrayUtils.contains(a, mdd._rBarevisionMark))
             && (null == mdd._rVisionMark || ArrayUtils.contains(a, mdd._rVisionMark))
             && (null == mdd._lBarevisionMark || ArrayUtils.contains(a, mdd._lBarevisionMark))
             && (null == mdd._lVisionMark || ArrayUtils.contains(a, mdd._lVisionMark))
                    ) { // 入力無しか全てAなら対象外
                return true;
            }
        } else if ("05".equals(div)) {
            if ((null == mdd._rEar || "01".equals(mdd._rEar)) && (null == mdd._lEar || "01".equals(mdd._lEar))) {
                return true;
            }
        } else if ("06".equals(div)) {
            if (
               (null == mdd._nosediseasecd || "01".equals(mdd._nosediseasecd))
            && (null == mdd._nosediseasecd5 || "01".equals(mdd._nosediseasecd5))
            && (null == mdd._nosediseasecd6 || "01".equals(mdd._nosediseasecd6))
            && (null == mdd._nosediseasecd7 || "01".equals(mdd._nosediseasecd7))) {
                return true;
            }
        } else if ("07".equals(div)) {
            if (       (null == mtd._jawsJointcd)
                    && (null == mtd._jawsJointcd2)
                    && (null == mtd._jawsJointcd3)
                    && (null == mtd._plaquecd)
                    && (null == mtd._gumcd)
                    && (null == mtd._remainadulttooth)
                    && (null == mtd._brackAdulttooth)
                    && (null == mtd._brackBabytooth)) {
                return true;
            }
        } else if ("08".equals(div)) {
            if (       (null == mtd._jawsJointcd)
                    && (null == mtd._jawsJointcd2)
                    && (null == mtd._jawsJointcd3)
                    && (null == mtd._plaquecd)
                    && (null == mtd._gumcd)
                    && (null == mtd._remainadulttooth)
                    && (null == mtd._brackAdulttooth)
                    && (null == mtd._brackBabytooth)) {
                return true;
            }
        } else if ("10".equals(div)) {
        	if (_param._isRisshisha) {
				final boolean isTanpaku = 4 <= Util.toInt(mdd._albuminuria1cd, 0); // 蛋白04以上
				final boolean isTou = 3 <= Util.toInt(mdd._uricsugar1cd, 0); // 糖03以上
				final boolean isSenketsu = 4 <= Util.toInt(mdd._uricbleed1cd, 0); // 潜血04以上
        		if ((null == mdd._nutritioncd || "01".equals(mdd._nutritioncd))
        		 && (null == mdd._skindiseasecd || "01".equals(mdd._skindiseasecd))
        		 && (null == mdd._otherdiseasecd || "01".equals(mdd._otherdiseasecd))
        		 && isTanpaku == false
        		 && isTou == false
        		 && isSenketsu == false
        		 && (null == mdd._heartMedexam || "01".equals(mdd._heartMedexam))
        				) {
        			return true;
        		} else {
        			if (_param._isOutputDebug) {
        				log.info(" " + div + " isPrint= tan: " + isTanpaku + ", tou: " + isTou + ", sen: " + isSenketsu + ", nut:" + mdd._nutritioncd + ", skindi:" + mdd._skindiseasecd + ", otherdis:" + mdd._otherdiseasecd + ", heartmed:" + mdd._heartMedexam);
        			}
        			return false;
        		}
        	} else {
        		if ((null == mdd._nutritioncd || "01".equals(mdd._nutritioncd))
        		 && (null == mdd._spineribcd || "01".equals(mdd._spineribcd))
        		 && (null == mdd._skindiseasecd || "01".equals(mdd._skindiseasecd))
        		 && (null == mdd._otherdiseasecd || "01".equals(mdd._otherdiseasecd))
        				) {
        			return true;
        		}
        	}
        } else if ("11".equals(div)) {
            if (
                    (null == mdd._spineribcd || "01".equals(mdd._spineribcd))
                ) {
                return true;
            }
        } else if ("12".equals(div)) {
            if (null == mdd._eyediseasecd5) {
                return true;
            }
        } else if ("13".equals(div)) {
            if (
                    (null == mdd._heartMedexam || "01".equals(mdd._heartMedexam)) && null == mdd._heartMedexamRemark
               ) {
                return true;
            }
        } else if ("14".equals(div)) {
            if (
                    (null == mdd._heartMedexam || "01".equals(mdd._heartMedexam)) && null == mdd._heartMedexamRemark
               ) {
                return true;
            }
        } else if ("15".equals(div)) {
            if (
                    (!"02".equals(mdd._tbRemarkcd))
                    ) {
                return true;
            }
        } else if ("16".equals(div)) {
            if (
                    (!"03".equals(mdd._tbRemarkcd))
               ) {
                return true;
            }
        } else if ("17".equals(div)) {
            if (
                    (!"04".equals(mdd._tbRemarkcd))
               ) {
                return true;
            }
        } else if ("18".equals(div)) {
            if (null == mtd
                    || (null == mtd._jawsJointcd)
                    && (null == mtd._jawsJointcd2)
                    && (null == mtd._jawsJointcd3)
                    && (null == mtd._plaquecd)
                    && (null == mtd._gumcd)
                    && (null == mtd._remainadulttooth)
                    && (null == mtd._brackAdulttooth)
                    && (null == mtd._brackBabytooth)
                       ) {
                // 未受験者を印刷
                return false;
            } else {
                return true;
            }
        } else if ("19_1".equals(div)) {
            // 未受験者を印刷
            if (null == mdd
            || null == mdd._nutritioncd
            && null == mdd._spineribcd
            && null == mdd._skindiseasecd
            && null == mdd._otherdiseasecd) {
                return false;
            } else {
                return true;
            }
        } else if ("19_2".equals(div)) {
            // 未受験者を印刷
            if (null == mdd
            || null == mdd._eyediseasecd) {
                return false;
            } else {
                return true;
            }
        } else if ("19_3".equals(div)) {
            // 未受験者を印刷
            if (null == mdd
            || null == mdd._nosediseasecd
            && null == mdd._nosediseasecdRemark1
            && null == mdd._nosediseasecdRemark2
            && null == mdd._nosediseasecdRemark3) {
                return false;
            } else {
                return true;
            }
        } else if ("20_1".equals(div)) {
            // １次尿検査のお知らせ
            // 全員出力
        } else if ("20_2".equals(div)) {
            // １次尿検査結果のお知らせ
            if (null == mdd._albuminuria1cd
             || null == mdd._uricbleed1cd
             || null == mdd._uricsugar1cd) {
                return true;
            } else {
                return false;
            }
        } else if ("20_3".equals(div)) {
            // ２次尿検査結果のお知らせ
            if (null == mdd._albuminuria2cd
             || null == mdd._uricbleed2cd
             || null == mdd._uricsugar2cd) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    // 3 眼科検診結果のお知らせ
    private void print3(final DB2UDB db2, final Vrw32alp svf, final List studentList) {
        final String documentCd = "03";
        final String form = "KNJF030D_3.frm";
        log.info(" form = " + form);

        final List documentMstTextTokenList = KNJ_EditKinsoku.getTokenList(_param.getDocumentMstText(documentCd), 80);

        for (int sti = 0; sti < studentList.size(); sti++) {
            final Student student = (Student) studentList.get(sti);
            final MedexamDetDat mdd = student._medexamDetDat;
            if (isNotPrint(documentCd, mdd, null)) {
                continue;
            }

            svf.VrSetForm(form, 1);

            printDocumentMstTitleText(svf, _param.getDocumentMstTitle(documentCd), documentMstTextTokenList);

            svf.VrsOut("DATE", _param._hiduke); // 日付
            svf.VrsOut("SCHOOL_NAME", _param._certifSchoolDatSchoolName); // 学校名

            svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME1_1" : student._nenkuminamaeLen <= 60 ? "NAME1_2" : "NAME1_3", student._nenkuminamae);
            svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME2_1" : student._nenkuminamaeLen <= 60 ? "NAME2_2" : "NAME2_3", student._nenkuminamae);

            final String result = Util.mkString(new String[] {_param.getNameMstName1("F050", mdd._eyediseasecd), mdd._eyeTestResult}, " ");
            svf.VrsOut(Util.getMS932ByteLength(result) <= 36 ? "RESULT1" : Util.getMS932ByteLength(result) <= 50 ? "RESULT2" : "RESULT3", result);

            putGengou1(db2, svf, "ERA_NAME");

            printPrincipalStaffname(svf);
            printAttention(svf);
            printInn(svf);
            printGuardName(svf);

            svf.VrEndPage();
            _hasdata = true;
        }
    }

    // 4 視力検査結果のお知らせ
    private void print4(final DB2UDB db2, final Vrw32alp svf, final List studentList) {
        final String documentCd = "04";
        final String form = "KNJF030D_4.frm";
        log.info(" form = " + form);

        final List documentMstTextTokenList = KNJ_EditKinsoku.getTokenList(_param.getDocumentMstText(documentCd), 80);

        for (int sti = 0; sti < studentList.size(); sti++) {
            final Student student = (Student) studentList.get(sti);
            final MedexamDetDat mdd = student._medexamDetDat;
            if (isNotPrint(documentCd, mdd, null)) {
                continue;
            }

            svf.VrSetForm(form, 1);

            printDocumentMstTitleText(svf, _param.getDocumentMstTitle(documentCd), documentMstTextTokenList);

            svf.VrsOut("DATE", _param._hiduke); // 日付
            svf.VrsOut("SCHOOL_NAME", _param._certifSchoolDatSchoolName); // 学校名
            svf.VrsOut("EYE_R1", mdd._rBarevisionMark); // 視力
            svf.VrsOut("EYE_R2", mdd._rVisionMark); // 視力
            svf.VrsOut("EYE_L1", mdd._lBarevisionMark); // 視力
            svf.VrsOut("EYE_L2", mdd._lVisionMark); // 視力

            svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME1_1" : student._nenkuminamaeLen <= 60 ? "NAME1_2" : "NAME1_3", student._nenkuminamae);
            svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME2_1" : student._nenkuminamaeLen <= 60 ? "NAME2_2" : "NAME2_3", student._nenkuminamae);

            putGengou1(db2, svf, "ERA_NAME");

            printPrincipalStaffname(svf);
            printInn(svf);
            printGuardName(svf);

            svf.VrEndPage();
            _hasdata = true;
        }
    }

    // 5 聴力検査結果のお知らせ
    private void print5(final DB2UDB db2, final Vrw32alp svf, final List studentList) {
        final String documentCd = "05";
        final String form = "KNJF030D_5.frm";
        log.info(" form = " + form);

        final List documentMstTextTokenList = KNJ_EditKinsoku.getTokenList(_param.getDocumentMstText(documentCd), 80);

        for (int sti = 0; sti < studentList.size(); sti++) {
            final Student student = (Student) studentList.get(sti);
            final MedexamDetDat mdd = student._medexamDetDat;
            if (isNotPrint(documentCd, mdd, null)) {
                continue;
            }

            svf.VrSetForm(form, 1);

            printDocumentMstTitleText(svf, _param.getDocumentMstTitle(documentCd), documentMstTextTokenList);

            svf.VrsOut("R_EAR_DB", mdd._rEarDb); // 聴力・右
            svf.VrsOut("R_EAR_DB2", mdd._rEarDb4000); // 聴力・右
            svf.VrsOut("R_EAR", _param.getNameMstName1("F010", mdd._rEar)); //
            svf.VrsOut("L_EAR_DB", mdd._lEarDb); // 聴力・左
            svf.VrsOut("L_EAR_DB2", mdd._lEarDb4000); // 聴力・左
            svf.VrsOut("L_EAR", _param.getNameMstName1("F010", mdd._lEar)); //

            svf.VrsOut("DATE", _param._hiduke); // 日付
            svf.VrsOut("SCHOOL_NAME", _param._certifSchoolDatSchoolName); // 学校名

            svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME1_1" : student._nenkuminamaeLen <= 60 ? "NAME1_2" : "NAME1_3", student._nenkuminamae);
            svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME2_1" : student._nenkuminamaeLen <= 60 ? "NAME2_2" : "NAME2_3", student._nenkuminamae);

            putGengou1(db2, svf, "ERA_NAME");

            printPrincipalStaffname(svf);
            printInn(svf);
            printGuardName(svf);

            svf.VrEndPage();
            _hasdata = true;
        }
    }

    // 6 耳鼻咽頭検診結果のお知らせ
    private void print6(final DB2UDB db2, final Vrw32alp svf, final List studentList) {
        final String documentCd = "06";
        final String form = "KNJF030D_6.frm";
        log.info(" form = " + form);
        final List documentMstTextTokenList = KNJ_EditKinsoku.getTokenList(_param.getDocumentMstText(documentCd), 80);

        for (int sti = 0; sti < studentList.size(); sti++) {
            final Student student = (Student) studentList.get(sti);
            final MedexamDetDat mdd = student._medexamDetDat;
            if (isNotPrint(documentCd, mdd, null)) {
                continue;
            }

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

            svf.VrSetForm(form, 1);

            printDocumentMstTitleText(svf, _param.getDocumentMstTitle(documentCd), documentMstTextTokenList);

            svf.VrsOut("DATE", _param._hiduke); // 日付
            svf.VrsOut("SCHOOL_NAME", _param._certifSchoolDatSchoolName); // 学校名
            svf.VrsOut("RESULT1", noseDisease); // 結果備考

            svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME1_1" : student._nenkuminamaeLen <= 60 ? "NAME1_2" : "NAME1_3", student._nenkuminamae);
            svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME2_1" : student._nenkuminamaeLen <= 60 ? "NAME2_2" : "NAME2_3", student._nenkuminamae);

            putGengou1(db2, svf, "ERA_NAME");

            printPrincipalStaffname(svf);
            printAttention(svf);
            printInn(svf);
            printGuardName(svf);

            svf.VrEndPage();
            _hasdata = true;
        }
    }

    // 7 歯科検診結果のお知らせ
    private void print7(final DB2UDB db2, final Vrw32alp svf, final List studentList) {
        final String documentCd = "07";
        final String form = "KNJF030D_7.frm";
        log.info(" form = " + form);
        final List documentMstTextTokenList = KNJ_EditKinsoku.getTokenList(_param.getDocumentMstText(documentCd), 80);

        for (int sti = 0; sti < studentList.size(); sti++) {
            final Student student = (Student) studentList.get(sti);
            final MedexamToothDat mtd = student._medexamToothDat;
            if (isNotPrint(documentCd, null, mtd)) {
                continue;
            }

            svf.VrSetForm(form, 1);

            printDocumentMstTitleText(svf, _param.getDocumentMstTitle(documentCd), documentMstTextTokenList);

            svf.VrsOut("DATE", _param._hiduke); // 日付
            svf.VrsOut("SCHOOL_NAME", _param._certifSchoolDatSchoolName); // 学校名
            svf.VrsOut("RESULT1", _param.getNameMstName1("F510", mtd._jawsJointcd)); // 結果
            svf.VrsOut("RESULT2", _param.getNameMstName1("F512", mtd._jawsJointcd3)); // 結果
            svf.VrsOut("RESULT3", _param.getNameMstName1("F511", mtd._jawsJointcd2)); // 結果
            svf.VrsOut("RESULT4", _param.getNameMstName1("F520", mtd._plaquecd)); // 結果
            svf.VrsOut("RESULT5", _param.getNameMstName1("F513", mtd._gumcd)); // 結果
            final String remain = StringUtils.defaultString(mtd._remainadulttooth) + StringUtils.defaultString(Util.kakko(mtd._remainbabytooth));
            svf.VrsOut("RESULT6", remain); // 結果
            svf.VrsOut("RESULT7", mtd._brackBabytooth); // 結果
            svf.VrsOut("RESULT8", mtd._brackAdulttooth); // 結果
            svf.VrsOut("RESULT9", Util.mkString(new String[] {_param.getNameMstName1("F530", mtd._otherdiseasecd), mtd._otherdisease}, " ")); // 結果

            svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME1_1" : student._nenkuminamaeLen <= 60 ? "NAME1_2" : "NAME1_3", student._nenkuminamae);
            svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME2_1" : student._nenkuminamaeLen <= 60 ? "NAME2_2" : "NAME2_3", student._nenkuminamae);

            putGengou1(db2, svf, "ERA_NAME");

            printPrincipalStaffname(svf);
            printInn(svf);
            printGuardName(svf);

            svf.VrEndPage();
            _hasdata = true;
        }
    }

    // 8 歯科検診結果のお知らせ（全員配付)
    private void print8(final DB2UDB db2, final Vrw32alp svf, final List studentList) {
        final String documentCd = "08";
        final String form = "KNJF030D_8.frm";
        log.info(" form = " + form);
        final List documentMstTextTokenList = KNJ_EditKinsoku.getTokenList(_param.getDocumentMstText(documentCd), 80);

        for (int sti = 0; sti < studentList.size(); sti++) {
            final Student student = (Student) studentList.get(sti);
            final MedexamToothDat mtd = student._medexamToothDat;
            if (isNotPrint(documentCd, null, mtd)) {
                continue;
            }

            svf.VrSetForm(form, 1);
            svf.VrsOut("DATE", _param._hiduke); // 日付
            svf.VrsOut("SCHOOL_NAME", _param._certifSchoolDatSchoolName); // 学校名

            printDocumentMstTitleText(svf, _param.getDocumentMstTitle(documentCd), documentMstTextTokenList);

            if ((null == mtd._jawsJointcd || "01".equals(mtd._jawsJointcd))
             && (null == mtd._jawsJointcd2 || "01".equals(mtd._jawsJointcd2))
             && (null == mtd._jawsJointcd3 || "01".equals(mtd._jawsJointcd3))
             && (null == mtd._plaquecd || "01".equals(mtd._plaquecd))
             && (null == mtd._gumcd || "01".equals(mtd._gumcd))
             && (null == mtd._remainbabytooth || "0".equals(mtd._remainbabytooth))
             && (null == mtd._remainadulttooth || "0".equals(mtd._remainadulttooth))
             && (null == mtd._brackAdulttooth || "0".equals(mtd._brackAdulttooth))
             && (null == mtd._brackBabytooth || "0".equals(mtd._brackBabytooth)
             && (null == mtd._otherdiseasecd || "01".equals(mtd._otherdiseasecd)))) {

                svf.VrsOut("RESULT1", "○"); // 結果
            } else {

                if ("02".equals(mtd._jawsJointcd)) {
                    svf.VrsOut("RESULT2_1", "○"); // 結果
                } else if ("03".equals(mtd._jawsJointcd)) {
                    svf.VrsOut("RESULT3_1", "○"); // 結果
                }
                if ("02".equals(mtd._jawsJointcd2)) {
                    svf.VrsOut("RESULT2_3", "○"); // 結果
                } else if ("03".equals(mtd._jawsJointcd2)) {
                    svf.VrsOut("RESULT3_3", "○"); // 結果
                }
                if ("02".equals(mtd._jawsJointcd3)) {
                    svf.VrsOut("RESULT2_2", "○"); // 結果
                } else if ("03".equals(mtd._jawsJointcd3)) {
                    svf.VrsOut("RESULT3_2", "○"); // 結果
                }

                if ("02".equals(mtd._plaquecd)) {
                    svf.VrsOut("RESULT2_4", "○"); // 結果
                } else if ("03".equals(mtd._plaquecd)) {
                    svf.VrsOut("RESULT3_4", "○"); // 結果
                }
                if ("02".equals(mtd._gumcd)) {
                    svf.VrsOut("RESULT2_5", "○"); // 結果
                } else if ("03".equals(mtd._gumcd)) {
                    svf.VrsOut("RESULT3_5", "○"); // 結果
                }

                if (Util.toInt(mtd._brackAdulttooth, 0) > 0) {
                    svf.VrsOut("RESULT2_6", "○"); // 結果 CO（ｼｰｵｰ）
                }
                if (Util.toInt(mtd._remainadulttooth, 0) + Util.toInt(mtd._remainbabytooth, 0) > 0) {
                    svf.VrsOut("RESULT3_6", "○"); // 結果 未処置歯
                }
                if (Util.toInt(mtd._brackBabytooth, 0) > 0) {
                    svf.VrsOut("RESULT3_7", "○"); // 結果 要注意乳歯
                }

                if (!(null == mtd._otherdiseasecd || "01".equals(mtd._otherdiseasecd))) {
                    if (null != mtd._otherdisease) {
                        svf.VrsOut("RESULT3_8", "○"); // 結果 その他の疾病
                    } else {
                        svf.VrsOut("RESULT2_7", "○"); // 結果 その他
                    }
                }
            }

            svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME1_1" : student._nenkuminamaeLen <= 60 ? "NAME1_2" : "NAME1_3", student._nenkuminamae);
            svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME2_1" : student._nenkuminamaeLen <= 60 ? "NAME2_2" : "NAME2_3", student._nenkuminamae);

            putGengou1(db2, svf, "ERA_NAME");

            printPrincipalStaffname(svf);
            printInn(svf);
            printGuardName(svf);

            svf.VrEndPage();
            _hasdata = true;
        }
    }

    // 9 定期健康診断結果のお知らせ
    private void print9(final DB2UDB db2, final Vrw32alp svf, final List studentList) {
        final String documentCd = "09";
        final String form = "KNJF030D_9.frm";
        log.info(" form = " + form);
        final List documentMstTextTokenList = KNJ_EditKinsoku.getTokenList(_param.getDocumentMstText(documentCd), 80);

        for (int sti = 0; sti < studentList.size(); sti++) {
            final Student student = (Student) studentList.get(sti);

            svf.VrSetForm(form, 1);

            printDocumentMstTitleText(svf, _param.getDocumentMstTitle(documentCd), documentMstTextTokenList);

            svf.VrsOut("DATE", _param._hiduke); // 日付
            svf.VrsOut("SCHOOL_NAME", _param._certifSchoolDatSchoolName); // 学校名

            svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME1_1" : student._nenkuminamaeLen <= 60 ? "NAME1_2" : "NAME1_3", student._nenkuminamae);

            printPrincipalStaffname(svf);

            boolean printpermark = true;//"計算不能"を出力する特殊パターン以外は%記号を出力
            if (null != student._medexamDetDat) {
                final MedexamDetDat mdd = student._medexamDetDat;
                svf.VrsOut("HEIGHT", Util.sishagonyu(mdd._height)); // 身長
                svf.VrsOut("WEIGHT", Util.sishagonyu(mdd._weight)); // 体重
                final String outStr = Util.calcHimando(student, db2, _param);
                if (outStr == null) {
                    svf.VrsOut("OBESITY_IMP", "計算不能");
                    printpermark = false;
                } else {
                    svf.VrsOut("OBESITY", outStr); // 肥満度
                }

                svf.VrsOut("R_BAREVISION", mdd._rBarevisionMark); // 視力・右
                svf.VrsOut("R_VISION", mdd._rVisionMark); // 視力・右・矯正
                svf.VrsOut("L_BAREVISION", mdd._lBarevisionMark); // 視力・左
                svf.VrsOut("L_VISION", mdd._lVisionMark); // 視力・左・矯正

                svfVrsOut(svf, form, new String[] {"EYEDISEASE", "EYEDISEASE2"}, Util.mkString(new String[] {_param.getNameMstName1("F050", mdd._eyediseasecd), mdd._eyeTestResult}, " ")); // 目の疾病

                svfVrsOut(svf, form, new String[] {"NUTRITION", "NUTRITION2"}, Util.mkString(new String[] {_param.getNameMstName1("F030", mdd._nutritioncd), mdd._nutritioncdRemark}, " ")); // 栄養状態
                svfVrsOut(svf, form, new String[] {"SPINERIB", "SPINERIB2"}, Util.mkString(new String[] {_param.getNameMstName1("F040", mdd._spineribcd), mdd._spineribcdRemark}, " ")); // 脊柱・胸郭
                svfVrsOut(svf, form, new String[] {"SKINDISEASE", "SKINDISEASE2"}, Util.mkString(new String[] {_param.getNameMstName1("F070", mdd._skindiseasecd), mdd._skindiseasecdRemark}, " ")); // 皮膚疾患

				final String otherdisease;
				if (_param._isRisshisha) {
					otherdisease = Util.mkString(new String[] {_param.getNameMstName1("F140", mdd._otherdiseasecd), mdd._otherRemark2}, " ");  //★
				} else {
					otherdisease = Util.mkString(new String[] {_param.getNameMstName1("F140", mdd._otherdiseasecd), mdd._otherRemark}, " ");
				}
                svfVrsOut(svf, form, new String[] {"OTHERDISEASE", "OTHERDISEASE2"}, otherdisease); // その他の疾病および異常

                svf.VrsOut("ALBUMINURIA", _param.getNameMstName1("F020", mdd._albuminuria1cd)); // 尿・蛋白
                svf.VrsOut("URICBLEED", _param.getNameMstName1("F018", mdd._uricbleed1cd)); // 尿・潜血
                svf.VrsOut("URICSUGAR", _param.getNameMstName1("F019", mdd._uricsugar1cd)); // 尿・糖
                svf.VrsOut("ALBUMINURIA2", _param.getNameMstName1("F020", mdd._albuminuria2cd)); // 尿・蛋白
                svf.VrsOut("URICBLEED2", _param.getNameMstName1("F018", mdd._uricbleed2cd)); // 尿・潜血

                svf.VrsOut("R_EAR_DB", mdd._rEarDb); // 聴力・右
                svf.VrsOut("R_EAR", _param.getNameMstName1("F010", mdd._rEar)); //
                svf.VrsOut("L_EAR_DB", mdd._lEarDb); // 聴力・左
                svf.VrsOut("L_EAR", _param.getNameMstName1("F010", mdd._lEar)); //

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

                final String setView1 = Util.mkString(new String[] {_param.getNameMstName1("F100", mdd._tbRemarkcd), mdd._tbXRay}, " ");
                final String fieldView1 = Util.getMS932ByteLength(setView1) > 40 ? "_3" : Util.getMS932ByteLength(setView1) > 20 ? "_2" : "";
                svf.VrsOut("VIEWS1" + fieldView1, setView1); // 間接撮影・所見

                final String setView2 = Util.mkString(new String[] {
                        _param.getNameMstName1("F100", mdd._tbOthertestcd),
                        _param.getNameMstName1("F120", mdd._tbNamecd),
                        _param.getNameMstName1("F130", mdd._tbOthertestRemark1)}, " ");
                final String fieldView2 = Util.getMS932ByteLength(setView2) > 40 ? "_3" : Util.getMS932ByteLength(setView2) > 20 ? "_2" : "";
                svf.VrsOut("VIEWS2" + fieldView2, setView2); // 間接撮影・所見

                final String setHeartMedexam = Util.mkString(new String[] {_param.getNameMstName1("F080", mdd._heartMedexam), mdd._heartMedexamRemark}, " ");
                final String fieldHeartMedexam = Util.getMS932ByteLength(setHeartMedexam) > 40 ? "3" : Util.getMS932ByteLength(setHeartMedexam) > 20 ? "2" : "";
                svf.VrsOut("HEART_MEDEXAM" + fieldHeartMedexam, setHeartMedexam); // 心電図

                final String setHeartDisease = Util.mkString(new String[] {_param.getNameMstName1("F090", mdd._heartdiseasecd), mdd._heartdiseasecdRemark}, " ");
                final String fieldHeartDisease = Util.getMS932ByteLength(setHeartDisease) > 40 ? "3" : Util.getMS932ByteLength(setHeartDisease) > 20 ? "2" : "";
                svf.VrsOut("HEARTDISEASE" + fieldHeartDisease, setHeartDisease); // 心臓・疾病
            }

            if (null != student._medexamToothDat) {
                final MedexamToothDat mtd = student._medexamToothDat;

                svf.VrsOut("REMAINBABYTOOTH", mtd._remainbabytooth); // 乳歯・未処置数
                svf.VrsOut("REMAINADULTTOOTH", mtd._remainadulttooth); // 永久歯・未処置数
                svf.VrsOut("BRACKADULTTOOTH", mtd._brackAdulttooth); // 永久歯・要観察歯
                svf.VrsOut("BRACKBABYTOOTH", mtd._brackBabytooth); // 乳歯・要注意乳歯数

                final String gum = _param.getNameMstName1("F513", mtd._gumcd);
                svf.VrsOut(Util.getMS932ByteLength(gum) <= 20 ? "GUM1" : Util.getMS932ByteLength(gum) <= 30 ? "GUM2" : "GUM3", gum);

                final String jaws_joint1_ = _param.getNameMstName1("F510", mtd._jawsJointcd);
                svf.VrsOut(Util.getMS932ByteLength(jaws_joint1_) <= 20 ? "JAWS_JOINT1_1" : Util.getMS932ByteLength(jaws_joint1_) <= 30 ? "JAWS_JOINT1_2" : "JAWS_JOINT1_3", jaws_joint1_);

                final String jaws_joint2_ = _param.getNameMstName1("F512", mtd._jawsJointcd3);
                svf.VrsOut(Util.getMS932ByteLength(jaws_joint2_) <= 20 ? "JAWS_JOINT2_1" : Util.getMS932ByteLength(jaws_joint2_) <= 30 ? "JAWS_JOINT2_2" : "JAWS_JOINT2_3", jaws_joint2_);

                final String jaws_joint3_ = _param.getNameMstName1("F511", mtd._jawsJointcd2);
                svf.VrsOut(Util.getMS932ByteLength(jaws_joint3_) <= 20 ? "JAWS_JOINT3_1" : Util.getMS932ByteLength(jaws_joint3_) <= 30 ? "JAWS_JOINT3_2" : "JAWS_JOINT3_3", jaws_joint3_);

                final String plaque = _param.getNameMstName1("F520", mtd._plaquecd);
                svf.VrsOut(Util.getMS932ByteLength(plaque) <= 20 ? "PLAQUE1" : Util.getMS932ByteLength(plaque) <= 30 ? "PLAQUE2" : "PLAQUE3", plaque);

                final String toothotherdisease = Util.mkString(new String[] {_param.getNameMstName1("F530", mtd._otherdiseasecd), mtd._otherdisease}, " ");
                svf.VrsOut(Util.getMS932ByteLength(toothotherdisease) <= 20 ? "TOOTHOTHERDISEASE1" : Util.getMS932ByteLength(toothotherdisease) <= 30 ? "TOOTHOTHERDISEASE2" : "TOOTHOTHERDISEASE3", toothotherdisease);
            }
            if (printpermark) {
                //"計算不能"を出力する特殊パターン以外は%記号を「計算結果が出力されていなくても」出力
                svf.VrsOut("PER", "%");
            }

            svf.VrEndPage();
            _hasdata = true;
        }
    }

    // 10 内科検診結果のお知らせ
    private void print10(final DB2UDB db2, final Vrw32alp svf, final List studentList) {
        final String documentCd = "10";
		final String form = _param._isRisshisha ? "KNJF030D_10_2.frm" : "KNJF030D_10.frm";
        log.info(" form = " + form);
        final List documentMstTextTokenList = KNJ_EditKinsoku.getTokenList(_param.getDocumentMstText(documentCd), 80);

        for (int sti = 0; sti < studentList.size(); sti++) {
            final Student student = (Student) studentList.get(sti);
            final MedexamDetDat mdd = student._medexamDetDat;
            if (isNotPrint(documentCd, mdd, null)) {
            	if (_param._isOutputDebug) {
            		log.info(" not print " + student._schregno);
            	}
                continue;
            }

            svf.VrSetForm(form, 1);

            printDocumentMstTitleText(svf, _param.getDocumentMstTitle(documentCd), documentMstTextTokenList);

            svf.VrsOut("DATE", _param._hiduke); // 日付
            svf.VrsOut("SCHOOL_NAME", _param._certifSchoolDatSchoolName); // 学校名

            final String nutrition = Util.mkString(new String[] {_param.getNameMstName1("F030", mdd._nutritioncd), mdd._nutritioncdRemark}, " ");
            svf.VrsOut(Util.getMS932ByteLength(nutrition) <= 56 ? "NUTRITION1" : "NUTRITION2", nutrition); // 栄養状態

            if (!_param._isRisshisha) {
            	final String spinerib = Util.mkString(new String[] {_param.getNameMstName1("F040", mdd._spineribcd), mdd._spineribcdRemark}, " ");
            	svf.VrsOut(Util.getMS932ByteLength(spinerib) <= 56 ? "SPINERIB1" : "SPINERIB2", spinerib); // 脊柱・胸郭
            }

            final String skindisease = Util.mkString(new String[] {_param.getNameMstName1("F070", mdd._skindiseasecd), mdd._skindiseasecdRemark}, " ");
            svf.VrsOut(Util.getMS932ByteLength(skindisease) <= 56 ? "SKINDISEASE1" : "SKINDISEASE2", skindisease); // 皮膚疾患

            final String otherdisease;
            if (_param._isRisshisha) {
            	otherdisease = Util.mkString(new String[] {_param.getNameMstName1("F140", mdd._otherdiseasecd), mdd._otherRemark2}, " ");
            } else {
            	otherdisease = Util.mkString(new String[] {_param.getNameMstName1("F140", mdd._otherdiseasecd), mdd._otherRemark}, " ");
            }
            svf.VrsOut(Util.getMS932ByteLength(otherdisease) <= 56 ? "OTHERDISEASE1" : "OTHERDISEASE2", otherdisease); // その他の疾病および異常

            svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME1_1" : student._nenkuminamaeLen <= 60 ? "NAME1_2" : "NAME1_3", student._nenkuminamae);
            svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME2_1" : student._nenkuminamaeLen <= 60 ? "NAME2_2" : "NAME2_3", student._nenkuminamae);

            final String albuminuria1cd = _param.getNameMstName1("F020", mdd._albuminuria1cd);
            svf.VrsOut("ALBUMINURIA1CD", albuminuria1cd); // 蛋白

            final String uricsugar1cd = _param.getNameMstName1("F019", mdd._uricsugar1cd);
            svf.VrsOut("URICSUGAR1CD", uricsugar1cd); // 糖

            final String uricbleed1cd = _param.getNameMstName1("F018", mdd._uricbleed1cd);
            svf.VrsOut("URICBLEED1CD", uricbleed1cd); // 潜血

            String heartRemark = "99".equals(mdd._heartMedexam) ? Util.mkString(new String[] {_param.getNameMstName1("F080", mdd._heartMedexam), mdd._heartMedexamRemark}, " ") : _param.getNameMstName1("F080", mdd._heartMedexam);
            final String setHeartMedRemarkField = KNJ_EditEdit.getMS932ByteLength(heartRemark) > 64 ? "3" : KNJ_EditEdit.getMS932ByteLength(heartRemark) > 56 ? "2" : "1";
            svf.VrsOut("HEART_MEDEXAM_REMARK" + setHeartMedRemarkField, heartRemark); // 心電図

            putGengou1(db2, svf, "ERA_NAME");

            printPrincipalStaffname(svf);
            printAttention(svf);
            printInn(svf);
            printGuardName(svf);

            svf.VrEndPage();
            _hasdata = true;
        }
    }

    // 11 運動器検診結果のお知らせ
    private void print11(final DB2UDB db2, final Vrw32alp svf, final List studentList) {
        final String documentCd = "11";
        final String form = "KNJF030D_11.frm";
        log.info(" form = " + form);
        final List documentMstTextTokenList = KNJ_EditKinsoku.getTokenList(_param.getDocumentMstText(documentCd), 80);

        for (int sti = 0; sti < studentList.size(); sti++) {
            final Student student = (Student) studentList.get(sti);
            final MedexamDetDat mdd = student._medexamDetDat;
            if (isNotPrint(documentCd, mdd, null)) {
                continue;
            }

            svf.VrSetForm(form, 1);

            printDocumentMstTitleText(svf, _param.getDocumentMstTitle(documentCd), documentMstTextTokenList);

            svf.VrsOut("DATE", _param._hiduke); // 日付
            svf.VrsOut("SCHOOL_NAME", _param._certifSchoolDatSchoolName); // 学校名
            svf.VrsOut("SPINERIB", Util.mkString(new String[] {_param.getNameMstName1("F040", mdd._spineribcd), mdd._spineribcdRemark}, " ")); // 脊柱・胸郭

            svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME1_1" : student._nenkuminamaeLen <= 60 ? "NAME1_2" : "NAME1_3", student._nenkuminamae);
            svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME2_1" : student._nenkuminamaeLen <= 60 ? "NAME2_2" : "NAME2_3", student._nenkuminamae);

            putGengou1(db2, svf, "ERA_NAME");

            printPrincipalStaffname(svf);
            printAttention(svf);
            printInn(svf);
            printGuardName(svf);

            svf.VrEndPage();

            _hasdata = true;
        }
    }

    // 12 色覚検査結果のお知らせ
    private void print12(final DB2UDB db2, final Vrw32alp svf, final List studentList) {
        final String documentCd = "12";
        final String form = "KNJF030D_12.frm";
        log.info(" form = " + form);
        final List documentMstTextTokenList = KNJ_EditKinsoku.getTokenList(_param.getDocumentMstText(documentCd), 80);

        for (int sti = 0; sti < studentList.size(); sti++) {
            final Student student = (Student) studentList.get(sti);
            final MedexamDetDat mdd = student._medexamDetDat;
            if (isNotPrint(documentCd, mdd, null)) {
                continue;
            }

            svf.VrSetForm(form, 1);

            printDocumentMstTitleText(svf, _param.getDocumentMstTitle(documentCd), documentMstTextTokenList);

            svf.VrsOut("DATE", _param._hiduke); // 日付
            svf.VrsOut("SCHOOL_NAME", _param._certifSchoolDatSchoolName); // 学校名
            if ("01".equals(mdd._eyediseasecd5)) {
                svf.VrsOut("COLOR_VISION1", "○"); // 色覚検査
            } else if (null != mdd._eyediseasecd5) {
                svf.VrsOut("COLOR_VISION2", "○"); // 色覚検査
            }

            svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME1_1" : student._nenkuminamaeLen <= 60 ? "NAME1_2" : "NAME1_3", student._nenkuminamae);
            svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME2_1" : student._nenkuminamaeLen <= 60 ? "NAME2_2" : "NAME2_3", student._nenkuminamae);

            putGengou1(db2, svf, "ERA_NAME");

            printPrincipalStaffname(svf);
            printInn(svf);
            printGuardName(svf);

            svf.VrEndPage();
            _hasdata = true;
        }
    }

    // 13 心臓検診結果のお知らせ
    private void print13(final Vrw32alp svf, final List studentList) {
        final String documentCd = "13";
        final String form = "KNJF030D_13.frm";
        log.info(" form = " + form);
        final List documentMstTextTokenList = KNJ_EditKinsoku.getTokenList(_param.getDocumentMstText(documentCd), 80);

        for (int sti = 0; sti < studentList.size(); sti++) {
            final Student student = (Student) studentList.get(sti);
            final MedexamDetDat mdd = student._medexamDetDat;
            if (isNotPrint(documentCd, mdd, null)) {
                continue;
            }

            svf.VrSetForm(form, 1);

            printDocumentMstTitleText(svf, _param.getDocumentMstTitle(documentCd), documentMstTextTokenList);

            svf.VrsOut("DATE", _param._hiduke); // 日付
            svf.VrsOut("SCHOOL_NAME", _param._certifSchoolDatSchoolName); // 学校名
            final String heartMedexam = Util.mkString(new String[] {_param.getNameMstName1("F080", mdd._heartMedexam), mdd._heartMedexamRemark}, " ");
            if (Util.getMS932ByteLength(heartMedexam) <= 68) {
                svf.VrsOut("HEART_MEDEXAM1", heartMedexam); // 心電図
            } else {
                final List tokenList = KNJ_EditKinsoku.getTokenList(heartMedexam, 68);
                for (int i = 0; i < tokenList.size(); i++) {
                    svf.VrsOut("HEART_MEDEXAM2_" + String.valueOf(i + 1), (String) tokenList.get(i)); // 心電図
                }
            }
//            svf.VrsOut("HEART_EXAM1", null); // 心臓精密検査
//            svf.VrsOut("HEART_EXAM2", null); // 心臓精密検査
//            svf.VrsOut("HEART_EXAM3", null); // 心臓精密検査
//            svf.VrsOut("HEART_EXAM4", null); // 心臓精密検査
//            svf.VrsOut("HEART_EXAM5", null); // 心臓精密検査
            svf.VrsOut("CHARGE", _param._charge); // 担当者情報

            svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME1_1" : student._nenkuminamaeLen <= 60 ? "NAME1_2" : "NAME1_3", student._nenkuminamae);

            printPrincipalStaffname(svf);

            svf.VrEndPage();
            _hasdata = true;
        }
    }

    // 14 心臓検診精密検査のお願い（主治医）
    private void print14(final Vrw32alp svf, final List studentList) {
        final String documentCd = "14";
        final String form = "KNJF030D_14.frm";
        log.info(" form = " + form);
        final List documentMstTextTokenList = KNJ_EditKinsoku.getTokenList(_param.getDocumentMstText(documentCd), 80);

        for (int sti = 0; sti < studentList.size(); sti++) {
            final Student student = (Student) studentList.get(sti);
            final MedexamDetDat mdd = student._medexamDetDat;
            if (isNotPrint(documentCd, mdd, null)) {
                continue;
            }

            svf.VrSetForm(form, 1);

            printDocumentMstTitleText(svf, _param.getDocumentMstTitle(documentCd), documentMstTextTokenList);

            svf.VrsOut("DATE", _param._hiduke); // 日付
            svf.VrsOut("SCHOOL_NAME", _param._certifSchoolDatSchoolName); // 学校名
            svf.VrsOut("SCHOOL_NAME2", _param._certifSchoolDatSchoolName); // 学校名

            final String heartMedexam = Util.mkString(new String[] {_param.getNameMstName1("F080", mdd._heartMedexam), mdd._heartMedexamRemark}, " ");
            if (Util.getMS932ByteLength(heartMedexam) <= 68) {
                svf.VrsOut("HEART_MEDEXAM1", heartMedexam); // 心電図
            } else {
                final List tokenList = KNJ_EditKinsoku.getTokenList(heartMedexam, 68);
                for (int i = 0; i < tokenList.size(); i++) {
                    svf.VrsOut("HEART_MEDEXAM2_" + String.valueOf(i + 1), (String) tokenList.get(i)); // 心電図
                }
            }
//            svf.VrsOut("HEART_EXAM1", null); // 心臓精密検査
//            svf.VrsOut("HEART_EXAM2", null); // 心臓精密検査
//            svf.VrsOut("HEART_EXAM3", null); // 心臓精密検査
//            svf.VrsOut("HEART_EXAM4", null); // 心臓精密検査
//            svf.VrsOut("HEART_EXAM5", null); // 心臓精密検査

            svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME1_1" : student._nenkuminamaeLen <= 60 ? "NAME1_2" : "NAME1_3", student._nenkuminamae);

            printPrincipalStaffname(svf);

            svf.VrEndPage();
            _hasdata = true;
        }
    }

    // 15 胸部エックス線検診結果のお知らせ（要経過観察）
    // 16 胸部エックス線検診結果のお知らせ（要精密検査）
    // 17 胸部エックス線検診結果のお知らせ（要精密検査_主治医）
    private void print15_16_17(final int n, final DB2UDB db2, final Vrw32alp svf, final List studentList) {
        final String documentCd;
        String form = null;
        if (n == 15) {
            form = "KNJF030D_15.frm";
            documentCd = "15";
        } else if (n == 16) {
            form = "KNJF030D_16.frm";
            documentCd = "16";
        } else if (n == 17 ) {
            form = "KNJF030D_17_1.frm";
            documentCd = "17";
        } else {
            return;
        }
        log.info(" form = " + form);
        final List documentMstTextTokenList = KNJ_EditKinsoku.getTokenList(_param.getDocumentMstText(documentCd), 80);

        for (int sti = 0; sti < studentList.size(); sti++) {
            final Student student = (Student) studentList.get(sti);
            final MedexamDetDat mdd = student._medexamDetDat;
            if (isNotPrint(documentCd, mdd, null)) {
                continue;
            }
            svf.VrSetForm(form, 1);

            printDocumentMstTitleText(svf, _param.getDocumentMstTitle(documentCd), documentMstTextTokenList);

            svf.VrsOut("DATE", _param._hiduke); // 日付
            svf.VrsOut("SCHOOL_NAME", _param._certifSchoolDatSchoolName); // 学校名
            svf.VrsOut("VIEWS1", Util.mkString(new String[] {_param.getNameMstName1("F100", mdd._tbRemarkcd), mdd._tbXRay}, " ")); // 間接撮影・所見
            svf.VrsOut("VIEWS2", Util.mkString(new String[] {_param.getNameMstName1("F100", mdd._tbOthertestcd),
                                                             _param.getNameMstName1("F120", mdd._tbNamecd),
                                                             _param.getNameMstName1("F130", mdd._tbOthertestRemark1)}, " ")); // 間接撮影・所見
            svf.VrsOut("CHARGE", _param._charge); // 担当者情報

            svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME1_1" : student._nenkuminamaeLen <= 60 ? "NAME1_2" : "NAME1_3", student._nenkuminamae);
            svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME2_1" : student._nenkuminamaeLen <= 60 ? "NAME2_2" : "NAME2_3", student._nenkuminamae);

            if(n != 17) {
            	putGengou1(db2, svf, "ERA_NAME");
            }

            printPrincipalStaffname(svf);
            printAttention(svf);
            printInn(svf);
            printGuardName(svf);

            svf.VrEndPage();

            if (n == 17) {
                final String form2 = "KNJF030D_17_2.frm";
                svf.VrSetForm(form2, 1);

                svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME1_1" : student._nenkuminamaeLen <= 60 ? "NAME1_2" : "NAME1_3", student._nenkuminamae);

                putGengou1(db2, svf, "ERA_NAME");

                printAttention(svf);
                printInn(svf);
                printGuardName(svf);

                svf.VrEndPage();
            }

            _hasdata = true;
        }
    }

    // 18 未検診のお知らせ（歯科）
    private void print18(final DB2UDB db2, final Vrw32alp svf, final List studentList) {
        final String documentCd = "18";
        final String form = "KNJF030D_18.frm";
        log.info(" form = " + form);
        final List documentMstTextTokenList = KNJ_EditKinsoku.getTokenList(_param.getDocumentMstText(documentCd), 80);

        for (int sti = 0; sti < studentList.size(); sti++) {
            final Student student = (Student) studentList.get(sti);
            final MedexamToothDat mtd = student._medexamToothDat;
            if (isNotPrint(documentCd, null, mtd)) {
                continue;
            }

            svf.VrSetForm(form, 1);

            printDocumentMstTitleText(svf, _param.getDocumentMstTitle(documentCd), documentMstTextTokenList);

            svf.VrsOut("DATE", _param._hiduke); // 日付
            svf.VrsOut("SCHOOL_NAME", _param._certifSchoolDatSchoolName); // 学校名

            svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME1_1" : student._nenkuminamaeLen <= 60 ? "NAME1_2" : "NAME1_3", student._nenkuminamae);
            svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME2_1" : student._nenkuminamaeLen <= 60 ? "NAME2_2" : "NAME2_3", student._nenkuminamae);

            List flist = new ArrayList();
            flist.add("ERA_NAME");
            flist.add("ERA_NAME2");
            putGengou2(db2, svf, flist);

            printPrincipalStaffname(svf);
            printInn(svf);

            svf.VrEndPage();
            _hasdata = true;
        }
    }

    // 19 未検診のお知らせ（内科_眼科_耳鼻科）
    private void print19(final DB2UDB db2, final Vrw32alp svf, final List studentList) {
        final String documentCd = "19";
        final String form = "KNJF030D_19.frm";
        log.info(" form = " + form);
        final List documentMstTextTokenList = KNJ_EditKinsoku.getTokenList(_param.getDocumentMstText(documentCd), 80);

        for (int sti = 0; sti < studentList.size(); sti++) {
            final Student student = (Student) studentList.get(sti);
            final MedexamDetDat mdd = student._medexamDetDat;

            for (int i = 0; i < 3; i++) {
                String mijukenName = "";
                boolean isNotPrint = false;
                switch (i) {
                case 0:
                    isNotPrint = isNotPrint("19_1", mdd, null);
                    mijukenName = "内科検診";
                    break;
                case 1:
                    isNotPrint = isNotPrint("19_2", mdd, null);
                    mijukenName = "眼科検診";
                    break;
                case 2:
                    isNotPrint = isNotPrint("19_3", mdd, null);
                    mijukenName = "耳鼻科検診";
                    break;
                }
                if (isNotPrint) {
                    continue;
                }

                svf.VrSetForm(form, 1);

                printDocumentMstTitleText(svf, _param.getDocumentMstTitle(documentCd), documentMstTextTokenList);

                svf.VrsOut("DATE", _param._hiduke); // 日付
                svf.VrsOut("SCHOOL_NAME", _param._certifSchoolDatSchoolName); // 学校名
                svf.VrsOut("MEDICAL_EXAM_NAME2", mijukenName); // 検診名

                svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME1_1" : student._nenkuminamaeLen <= 60 ? "NAME1_2" : "NAME1_3", student._nenkuminamae);
                svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME2_1" : student._nenkuminamaeLen <= 60 ? "NAME2_2" : "NAME2_3", student._nenkuminamae);

                putGengou1(db2, svf, "ERA_NAME");

                printPrincipalStaffname(svf);
                printInn(svf);

                svf.VrEndPage();
                _hasdata = true;
            }
        }
    }

    // 20 尿検査結果のお知らせ
    private void print20(final DB2UDB db2, final Vrw32alp svf, final List studentList) {

        if ("on".equals(_param._nyocheck1)) {
            // １次尿検査のお知らせ
            final String form = "KNJF030D_20_1.frm";

            log.info(" form = " + form);

            for (int sti = 0; sti < studentList.size(); sti++) {
                final Student student = (Student) studentList.get(sti);
                final MedexamDetDat mdd = student._medexamDetDat;

                if (isNotPrint("20_1", mdd, null)) {
                    continue;
                }

                svf.VrSetForm(form, 1);

                svf.VrsOut("DUMMY", "1"); // ダミーデータ

                svf.VrEndPage();
                _hasdata = true;
            }
        }

        if ("on".equals(_param._nyocheck2)) {
            // １次尿検査結果のお知らせ
            final String form2 = "KNJF030D_20_2.frm";
            final String form3 = "KNJF030D_20_3.frm";
            log.info(" form2 = " + form2 + ", form3 = " + form3);

            for (int sti = 0; sti < studentList.size(); sti++) {
                final Student student = (Student) studentList.get(sti);
                final MedexamDetDat mdd = student._medexamDetDat;

                if (isNotPrint("20_2", mdd, null)) {
                    continue;
                }

                final boolean ijounasi =
                            (null == mdd._albuminuria1cd || "01".equals(mdd._albuminuria1cd))
                         && (null == mdd._uricbleed1cd || "01".equals(mdd._uricbleed1cd))
                         && (null == mdd._uricsugar1cd || "01".equals(mdd._uricsugar1cd));

                if (ijounasi) {
                    // １次尿検査結果のお知らせ
                    svf.VrSetForm(form2, 1);

                    svf.VrsOut("DATE", _param._hiduke); // 日付
                    svf.VrsOut("SCHOOL_NAME", _param._certifSchoolDatSchoolName); // 学校名
                    printPrincipalStaffname(svf);

                    svf.VrEndPage();
                    _hasdata = true;
                } else {
                    // ２次尿検査のお知らせ（蛋白・潜血用）
                    svf.VrSetForm(form3, 1);

                    svf.VrsOut("DUMMY", "1"); // ダミーデータ

                    svf.VrEndPage();
                    _hasdata = true;
                }
            }
        }

        if ("on".equals(_param._nyocheck3)) {
            // ２次尿検査結果のお知らせ
            final String form4 = "KNJF030D_20_4.frm";
            final String form51 = "KNJF030D_20_5_1.frm";
            final String form52 = "KNJF030D_20_5_2.frm";
            final String form53 = "KNJF030D_20_5_3.frm";

            for (int sti = 0; sti < studentList.size(); sti++) {
                final Student student = (Student) studentList.get(sti);
                final MedexamDetDat mdd = student._medexamDetDat;

                if (isNotPrint("20_3", mdd, null)) {
                    continue;
                }

                final boolean ijounasiTanpaku = null == mdd._albuminuria2cd || "01".equals(mdd._albuminuria2cd);
                final boolean ijounasiBleed = null == mdd._uricbleed2cd || "01".equals(mdd._uricbleed2cd);
                final boolean ijounasiSugar = null == mdd._uricsugar2cd || "01".equals(mdd._uricsugar2cd);
                final boolean ijounasi = ijounasiTanpaku && ijounasiBleed && ijounasiSugar;

                if (ijounasi) {
                    svf.VrSetForm(form4, 1);

                    svf.VrsOut("DATE", _param._hiduke); // 日付
                    svf.VrsOut("SCHOOL_NAME", _param._certifSchoolDatSchoolName); // 学校名
                    printPrincipalStaffname(svf);

                    svf.VrEndPage();
                    _hasdata = true;
                } else {
                    if (!ijounasiTanpaku || !ijounasiBleed) {
                        // ２次尿検査結果のお知らせ（蛋白・潜血用）
                        svf.VrSetForm(form51, 1);

                        svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME1_1" : student._nenkuminamaeLen <= 60 ? "NAME1_2" : "NAME1_3", student._nenkuminamae);
                        svf.VrsOut("DATE", _param._hiduke); // 日付
                        svf.VrsOut("SCHOOL_NAME", _param._certifSchoolDatSchoolName); // 学校名
                        printPrincipalStaffname(svf);

                        svf.VrEndPage();
                        _hasdata = true;
                    }

                    if (!ijounasiSugar) {
                        // ２次尿検査結果のお知らせ（糖用）
                        svf.VrSetForm(form52, 1);

                        svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME1_1" : student._nenkuminamaeLen <= 60 ? "NAME1_2" : "NAME1_3", student._nenkuminamae);
                        svf.VrsOut("DATE", _param._hiduke); // 日付
                        svf.VrsOut("SCHOOL_NAME", _param._certifSchoolDatSchoolName); // 学校名
                        printPrincipalStaffname(svf);

                        svf.VrEndPage();
                        _hasdata = true;
                    }

                    // 尿検査結果のお知らせ
                    svf.VrSetForm(form53, 1);

                    svf.VrsOut(Util.getMS932ByteLength(student._name) <= 28 ? "NAME1_1" : "NAME1_2", student._name); // 氏名他

                    svf.VrsOut(Util.getMS932ByteLength(mdd._uricothertest) <= 28 ? "URINE_OTHERS1" : Util.getMS932ByteLength(mdd._uricothertest) <= 30 ? "URINE_OTHERS2" : "URINE_OTHERS3", mdd._uricothertest); // 尿・その他の検査

                    svf.VrsOut("HEIGHT", Util.sishagonyu(mdd._height)); // 身長
                    svf.VrsOut("WEIGHT", Util.sishagonyu(mdd._weight)); // 体重
                    svf.VrsOut("OBESITY", Util.calcHimando(student, db2, _param)); // 肥満度

                    svf.VrsOut("DATE", _param._hiduke); // 日付
                    svf.VrsOut("SCHOOL_NAME", _param._certifSchoolDatSchoolName); // 学校名
                    printPrincipalStaffname(svf);

                    svf.VrEndPage();
                    _hasdata = true;
                }
            }
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
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _attendno = attendno;
            _annual = annual;
            _name = name;
            _sexCd = sexCd;
            _sex = sex;
            _birthDay = birthDay;
            _coursecd = coursecd;
            _majorcd = majorcd;
            _coursecode = coursecode;
            _coursename = coursename;
            _majorname = majorname;
            _coursecodename = coursecodename;
            _schoolKind = schoolKind;
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
                    final String schregno = rs.getString("SCHREGNO");
                    final String grade = rs.getString("GRADE");
                    final String hrClass = rs.getString("HR_CLASS");
                    final String hrName = rs.getString("HR_NAME");
                    final String attendno = rs.getString("ATTENDNO");
                    final String annual = rs.getString("ANNUAL");
                    final String name = rs.getString("NAME");
                    final String sexCd = rs.getString("SEX_CD");
                    final String sex = rs.getString("SEX");
                    final String birthDay = rs.getString("BIRTHDAY");
                    final String coursecd = rs.getString("COURSECD");
                    final String majorcd = rs.getString("MAJORCD");
                    final String coursecode = rs.getString("COURSECODE");
                    final String coursename = rs.getString("COURSENAME");
                    final String majorname = rs.getString("MAJORNAME");
                    final String coursecodename = rs.getString("COURSECODENAME");
                    final String schoolKind = rs.getString("SCHOOL_KIND");
                    final Student student = new Student(schregno,
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
                    final String seki = NumberUtils.isDigits(student._attendno) ? String.valueOf(Integer.parseInt(student._attendno)) : StringUtils.defaultString(student._attendno);
                    student._nenkuminamae = StringUtils.defaultString(student._hrName) + " " + seki + "席　名前　" + StringUtils.defaultString(student._name);
                    student._nenkuminamaeLen = Util.getMS932ByteLength(student._nenkuminamae);
                    rtnList.add(student);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            for (final Iterator it = rtnList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();

                student._medexamDetDat = new MedexamDetDat(db2, param._year, student._schregno, printKenkouSindanIppan);
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
            stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON T1.YEAR = GDAT.YEAR ");
            stb.append("          AND T1.GRADE = GDAT.GRADE ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT AS HR ON T1.YEAR = HR.YEAR ");
            stb.append("          AND T1.SEMESTER = HR.SEMESTER ");
            stb.append("          AND T1.GRADE = HR.GRADE ");
            stb.append("          AND T1.HR_CLASS = HR.HR_CLASS ");
            stb.append("     LEFT JOIN SCHREG_BASE_MST AS BASE ON T1.SCHREGNO = BASE.SCHREGNO ");
            stb.append("     LEFT JOIN NAME_MST AS N1 ON N1.NAMECD1 = 'Z002' ");
            stb.append("          AND BASE.SEX = N1.NAMECD2 ");
            stb.append("     LEFT JOIN COURSECODE_MST AS COURSEC ON T1.COURSECODE = COURSEC.COURSECODE ");
            stb.append("     LEFT JOIN MAJOR_MST AS MAJOR ON T1.COURSECD = MAJOR.COURSECD ");
            stb.append("          AND T1.MAJORCD = MAJOR.MAJORCD ");
            stb.append("     LEFT JOIN COURSE_MST AS COURSE ON T1.COURSECD = COURSE.COURSECD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
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
            _schregNo = schregNo;
            _year = year;
            _semester = semester;
            _grade = grade;
            _hrClass = hrClass;
            _attendNo = attendNo;
            _hrName = hrName;
            _hrClassName = hrClassName;
            _gradeName = gradeName;
            _gradeCd = gradeCd;
            _age = age;
            _hasData = hasData;
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
                    final String schregNo = rs.getString("SCHREGNO");
                    final String year = rs.getString("YEAR");
                    final String semester = rs.getString("SEMESTER");
                    final String grade = rs.getString("GRADE");
                    final String hrClass = rs.getString("HR_CLASS");
                    final String attendNo = rs.getString("ATTENDNO");
                    final String hrName = rs.getString("HR_NAME");
                    final String hrClassName = rs.getString("HR_CLASS_NAME1");
                    final String gradeName = rs.getString("GRADE_NAME1");
                    final String gradeCd = rs.getString("GRADE_CD");
                    final String age = rs.getString("AGE");
                    final String hasData = rs.getString("HAS_DATA");
                    final String entYearGradeCd = rs.getString("ENT_YEAR_GRADE_CD");
                    final String schoolKind = rs.getString("SCHOOL_KIND");
                    final RegdDat regdDat = new RegdDat(schregNo, year, semester, grade, hrClass, attendNo, hrName, hrClassName, gradeName, gradeCd, age, hasData, entYearGradeCd, schoolKind);
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
            stb.append("        TT1.YEAR = '" + param._year + "' ");
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
            stb.append("                W2.SCHREGNO = W1.SCHREGNO ");
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
            stb.append("                W2.SCHREGNO = W1.SCHREGNO ");
            stb.append("                AND W2.YEAR = W1.YEAR ");
            stb.append("                AND W2.SEMESTER=W1.SEMESTER ");
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
            stb.append("     INNER JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("           AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("           AND T2.GRADE = T1.GRADE ");
            stb.append("           AND T2.HR_CLASS = T1.HR_CLASS ");
            stb.append("     LEFT JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO ");
            stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR ");
            stb.append("           AND GDAT.GRADE = T1.GRADE ");
            stb.append("     LEFT JOIN NAME_MST VN ON VN.NAMECD1 = 'A023' ");
            stb.append("          AND VN.NAME1 = GDAT.SCHOOL_KIND ");
            stb.append("     LEFT JOIN MEDEXAM_DET_DAT MED ON MED.YEAR = T1.YEAR ");
            stb.append("           AND MED.SCHREGNO = T1.SCHREGNO ");
            stb.append("     LEFT JOIN (SELECT I1.SCHREGNO, MAX(I2.GRADE_CD) AS GRADE_CD  ");
            stb.append("                FROM SCHREG_REGD_DAT I1 ");
            stb.append("                INNER JOIN SCHREG_REGD_GDAT I2 ON I2.YEAR = I1.YEAR AND I2.GRADE = I1.GRADE ");
            stb.append("                INNER JOIN SCHREG_ENT_GRD_HIST_DAT I3 ON I3.SCHREGNO = I1.SCHREGNO ");
            stb.append("                    AND I3.SCHOOL_KIND = I2.SCHOOL_KIND ");
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
     * @version $Id: 9d676459fbe207d813b6247841b4b842666d5b69 $
     */
    private static class KNJF030A {

        protected Param _param;
        protected DB2UDB _db2;

        private String FORM_KNJF030A_1_5 = "KNJF030A_1_5.frm";
        private String FORM_KNJF030A_1P_5 = "KNJF030A_1P_5.frm";

        private int hrnameKeta = 0;
        private int hrnameC2_1Keta = 0;
        private int nosedisease2Keta = 0;
        private int HR_NAME1_Keta = 0;

        private boolean _hasData;

        /**
         * {@inheritDoc}
         */
        protected boolean printMain(final Vrw32alp svf, final DB2UDB db2, final HttpServletRequest request, final Param param, final List students) throws SQLException {

            _param = param;
            _db2 = db2;

            _hasData = false;
            for (final Iterator itStudent = students.iterator(); itStudent.hasNext();) {
                final Student student = (Student) itStudent.next();
                log.debug(" schregno = " + student._schregno);
//                hasDataP = false;
                int dataCnt = 0;
//                if ("1".equals(param._useSchool_KindField) && !StringUtils.isBlank(param._SCHOOLKIND)) {
                    dataCnt = printOut(svf, _db2, student, _param._SCHOOLKIND, dataCnt);
//                } else {
//                    dataCnt = printOut(svf, _db2, student, "K", dataCnt);
//                    dataCnt = printOut(svf, _db2, student, "P", dataCnt);
//                    dataCnt = printOut(svf, _db2, student, "J", dataCnt);
//                    dataCnt = printOut(svf, _db2, student, "H", dataCnt);
//                }
            }
            return _hasData;
        }

        private int printOut(final Vrw32alp svf, final DB2UDB db2, final Student student, final String schoolKind, final int prevCount) throws SQLException {
            final List printData = RegdDat.getRegdList(db2, _param, student);
            if (printData.size() == 0) {
                return 0;
            }
            final String form = _param._isMie ? "KNJF030D_1_2.frm" : "KNJF030D_1.frm";
            log.fatal(" form = " + form);
            svf.VrSetForm(form, 4);

            try {
                final Map fieldnameMap = _param.getFieldInfoMap(svf, form);
                hrnameKeta = Param.getFieldKeta(fieldnameMap, "HR_NAME", 0);
                hrnameC2_1Keta = Param.getFieldKeta(fieldnameMap, "HR_NAMEC2_1", 0);
                nosedisease2Keta = Param.getFieldKeta(fieldnameMap, "NOSEDISEASE2", 0);
                HR_NAME1_Keta = Param.getFieldKeta(fieldnameMap, "HR_NAME1", 9999);
            } catch (Exception e) {
                log.warn("exception!", e);
            }

            int dataCnt = ("J".equals(schoolKind) ? prevCount : 0) + 1;
//            if ("2".equals(printKenkouSindanIppan)) {
//                // 熊本は入学学年の列から表示
//                final RegdDat regdDat0 = (RegdDat) printData.get(0);
//                if (NumberUtils.isDigits(regdDat0._entYearGradeCd)) {
//                    for (int cd = 1; cd < Integer.parseInt(regdDat0._entYearGradeCd); cd++) {
//                        svf.VrsOut("SCHREGNO", regdDat0._schregNo);   //改ページ用
//                        svf.VrEndRecord();
//                        dataCnt += 1;
//                    }
//                }
//            }
            for (final Iterator itPrint = printData.iterator(); itPrint.hasNext();) {
                final RegdDat regdDat = (RegdDat) itPrint.next();
//                if ("1".equals(_otherInjiParam.get("OUTPUTA")) && null == regdDat._hasData) {
//                    continue;
//                }
                if (dataCnt > 5) {
                    svf.VrSetForm(form, 4);
                    dataCnt = ("J".equals(schoolKind) ? prevCount : 0) + 1;
                }
                if ("1".equals(_param._defineSchool.schooldiv)) {
                    svf.VrsOut("GRADENAME_TITLE", "年度");
                } else {
                    svf.VrsOut("GRADENAME_TITLE", "学年");
                }
                svf.VrsOut("NAME_HEADER", "名前"); // 名前ヘッダ
                svf.VrsOut("SCHREGNO", regdDat._schregNo);   //改ページ用
                svf.VrsOut("NAME_SHOW" + (Util.getMS932ByteLength(student._name) > 24 ? "_2" : ""), student._name);
                svf.VrsOut("SEX", student._sex);
                svf.VrsOut("BIRTHDAY", KNJ_EditDate.h_format_JP(db2, student._birthDay));
                svf.VrsOut("SCHOOL_NAME", _param.getSchoolInfo(schoolKind, SCHOOL_NAME2));
                final String title = "生徒健康診断票";
                svf.VrsOut("TITLE",  title);
                if ("1".equals(printKenkouSindanIppan)) {
                    // 病名のタイトル
                    svf.VrsOut("DISEASE_NAME_TITLE", "疾病及び異常");
                }

                if ("1".equals(_param._defineSchool.schooldiv)) {
                	if (NumberUtils.isDigits(regdDat._year)) {
                		svf.VrsOutn("GRADE_2", dataCnt, KNJ_EditDate.gengou(db2, Integer.parseInt(regdDat._year)) + "年度");
                	}
                } else {
                    svf.VrsOutn("GRADE", dataCnt, regdDat._gradeName);
                }
                svf.VrsOutn(Util.getMS932ByteLength(regdDat._hrClassName) > HR_NAME1_Keta ? "HR_NAME2_1" : "HR_NAME1", dataCnt, regdDat._hrClassName);
                svf.VrsOutn("ATTENDNO", dataCnt, regdDat._attendNo);
                if (hrnameKeta > 0 && Util.getMS932ByteLength(regdDat._hrName) > hrnameKeta && hrnameC2_1Keta > hrnameKeta) {
                    svf.VrsOutn("HR_NAMEC2_1", dataCnt, regdDat._hrName);
                } else {
                    svf.VrsOutn("HR_NAME", dataCnt, regdDat._hrName);
                }
                svf.VrsOutn("YEAR", dataCnt, KNJ_EditDate.gengou(db2, Integer.parseInt(regdDat._year)) + "年度");
                svf.VrsOut("M_DATE", KNJ_EditDate.gengou(db2, Integer.parseInt(regdDat._year)) + "年度");
                svf.VrsOut("AGE", regdDat._age);        //４月１日現在の年齢

                if (null != regdDat._hasData) {

//                if ("1".equals(_otherInjiParam.get("OUTPUTA"))) {
                    final MedexamDetDat mdd = regdDat._medexamDetDat;
                    svf.VrsOut("HEIGHT", mdd._height);
                    svf.VrsOut("WEIGHT", mdd._weight);
                    if (!"J".equals(schoolKind)) {
                        svf.VrsOut("SIT_HEIGHT", mdd._sitheight);
                    }
                    svf.VrsOut("BUST", mdd._chest);
                    if ("01".equals(mdd._nutritioncd)) {
                        printSlash(svf, "SLASH_NUTRITION");
                    } else if ("98".equals(mdd._nutritioncd)) {
                        svf.VrsOut("NUTRITION", "未受診");
                    } else if (null != mdd._nutritioncd) {
                        svf.VrsOut("NUTRITION", "要注意");
                    }
//                    setName1WithSlash(svf, "NUTRITION", "F030", mdd._nutritioncd, "SLASH_NUTRITION");
//                    if ("2".equals(printKenkouSindanIppan)) {
//                        if (isSonota("F040", mdd._spineribcd)) {
//                            // その他はコード名称を表示しない
//                            svf.VrsOut("SPINERIBCD_REMARK2_1", mdd._spineribcdRemark);
//                        } else {
//                            setName1WithSlash(svf, "SPINERIB", "F040", mdd._spineribcd, "SLASH_SPINERIB");
//                        }
//                    } else {
                        setName1WithSlash(svf, "SPINERIB", "F040", mdd._spineribcd, "SLASH_SPINERIB");
                        svf.VrsOut("SPINERIBCD_REMARK", mdd._spineribcdRemark);
//                    }
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
//                    if ("2".equals(printKenkouSindanIppan)) {
//                        if (isSonota("F050", mdd._eyediseasecd)) {
//                            // その他はコード名称を表示しない
//                            setSonotaText3(svf,
//                                    "EYE_TEST_RESULT1_2", "EYE_TEST_RESULT2_2", "EYE_TEST_RESULT3_2",
//                                    30, "EYE_TEST_RESULT1", "EYE_TEST_RESULT2", "EYE_TEST_RESULT3",
//                                    mdd._eyeTestResult, mdd._eyeTestResult2, mdd._eyeTestResult3);
//                        } else {
//                            setName1WithSlash(svf, "EYEDISEASE", "F050", mdd._eyediseasecd, "SLASH_EYEDISEASE");
//                            svf.VrsOut("EYE_TEST_RESULT", getKakko(mdd._eyeTestResult));
//                        }
//                    } else {
                        setName1WithSlash(svf, "EYEDISEASE", "F050", mdd._eyediseasecd, "SLASH_EYEDISEASE");
                        svf.VrsOut("EYE_TEST_RESULT", mdd._eyeTestResult);
//                    }
//                    if ("2".equals(printKenkouSindanIppan)) {
//                        final String setRyear = "04".equals(mdd._rEar) ? "(" + mdd._rEarDb + ")" : "";
//                        final String setLyear = "04".equals(mdd._lEar) ? "(" + mdd._lEarDb + ")" : "";
//                        setNameWithSlash(svf, "R_EAR", "F010", mdd._rEar, 0, setRyear, "", "SLASH_R_EAR");
//                        setNameWithSlash(svf, "L_EAR", "F010", mdd._lEar, 0, setLyear, "", "SLASH_L_EAR");
//                    } else {
                        if ("02".equals(mdd._rEar)) {
                            svf.VrsOut("R_EAR_DB", "○" + StringUtils.defaultString(mdd._rEarDb, "  ") + "db");
                            svf.VrsOut("R_EAR", "（" + StringUtils.defaultString(mdd._rEarDb4000, "  ") + "db）");
                        } else if ("01".equals(mdd._rEar)) {
                            printSlash(svf, "SLASH_R_EAR");
                        } else {
                            svf.VrsOut("R_EAR_DB", mdd._rEarDb);
                            svf.VrsOut("R_EAR", mdd._rEar);
                        }
                        if ("02".equals(mdd._lEar)) {
                            svf.VrsOut("L_EAR_DB", "○" + StringUtils.defaultString(mdd._lEarDb, "  ") + "db");
                            svf.VrsOut("L_EAR", "（" + StringUtils.defaultString(mdd._lEarDb4000, "  ") + "db）");
                        } else if ("01".equals(mdd._lEar)) {
                            printSlash(svf, "SLASH_L_EAR");
                        } else {
                            svf.VrsOut("L_EAR_DB", mdd._lEarDb);
                            svf.VrsOut("L_EAR", mdd._lEar);
                        }
//                    }
                    // 耳鼻咽喉疾患
//                    if ("2".equals(printKenkouSindanIppan)) {
//                        if (isSonota("F060", mdd._nosediseasecd)) {
//                            // その他はコード名称を表示しない
//                            setSonotaText3(svf,
//                                    "NOSEDISEASECD_REMARK1_2", "NOSEDISEASECD_REMARK2_2", "NOSEDISEASECD_REMARK3_2",
//                                    30, "NOSEDISEASECD_REMARK1", "NOSEDISEASECD_REMARK2", "NOSEDISEASECD_REMARK3",
//                                    mdd._nosediseasecdRemark, mdd._nosediseasecdRemark2, mdd._nosediseasecdRemark3);
//                        } else {
//                            setName1WithSlash(svf, "NOSEDISEASE", "F060", mdd._nosediseasecd, "SLASH_NOSEDISEASE");
//                            svf.VrsOut("NOSEDISEASECD_REMARK", getKakko(mdd._nosediseasecdRemark));
//                        }
//                    } else {
//                        setName1WithSlash("NOSEDISEASE", "F060", regdDat._medexamDetDat._nosediseasecd, "SLASH_NOSEDISEASE");
//                        _svf.VrsOut("NOSEDISEASECD_REMARK", getKakko(regdDat._medexamDetDat._nosediseasecdRemark));

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
//                    }
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

//                    } else {
//                        svf.VrsOut("SKINDISEASE", mdd._skindiseasecdRemark);
//                        setName1WithSlash(svf, "SKINDISEASE", "F070", mdd._skindiseasecd, "SLASH_SKINDISEASE");
//
//                    }
                    // 結核
                    // 撮影日
                    svf.VrsOut("PHOTO_DATE", KNJ_EditDate.h_format_JP(db2, mdd._tbFilmdate));
                    // 画像番号
                    svf.VrsOut("FILMNO", mdd._tbFilmno);
                    // 所見
                    if (null == mdd._tbXRay) {
                        setName1WithSlash(svf, "VIEWS1_1", "F100", mdd._tbRemarkcd, "SLASH_VIEWS1_1");
                    } else {
                    	final int idx = indexOfKetaLimit(mdd._tbXRay, 20);
                    	if (-1 == idx) {
                    		svf.VrsOut("VIEWS1_1", mdd._tbXRay);
                    	} else {
                    		svf.VrsOut("VIEWS1_1", mdd._tbXRay.substring(0, idx));
                    		if (idx < mdd._tbXRay.length()) {
                        		svf.VrsOut("TB_X_RAY", mdd._tbXRay.substring(idx));
                    		}
                    	}
                    }
//                    if ("2".equals(printKenkouSindanIppan)) {
//                        // その他の検査
//                        if (isSonota("F110", mdd._tbOthertestcd)) {
//                            // その他はコード名称を表示しない
//                            svf.VrsOut("OTHERS2_1", mdd._tbOthertestRemark1);
//                        } else {
//                            setName1WithSlash(svf, "OTHERS", "F110", mdd._tbOthertestcd, null);
//                        }
//                        // 病名
//                        if (isSonota("F120", mdd._tbNamecd)) {
//                            // その他はコード名称を表示しない
//                            svf.VrsOut("DISEASE_NAME2_1", mdd._tbNameRemark1);  //★
//                        } else {
//                            setName1WithSlash(svf, "DISEASE_NAME", "F120", mdd._tbNamecd, "SLASH_DISEASE_NAME");
//                        }
//                        // 指導区分
//                        if (isSonota("F130", mdd._tbAdvisecd)) {
//                            // その他はコード名称を表示しない
//                            svf.VrsOut("GUIDANCE2_1", mdd._tbAdviseRemark1);
//                        } else {
//                            setName1WithSlash(svf, "GUIDANCE", "F130", mdd._tbAdvisecd, null);
//                        }
//                    } else {
                        // その他の検査
                        setName1WithSlash(svf, "OTHERS", "F110", mdd._tbOthertestcd, "SLASH_OTHERS");
                        // 病名
                        setName1WithSlash(svf, "DISEASE_NAME", "F120", mdd._tbNamecd, "SLASH_DISEASE_NAME");
                        // 指導区分
                        setName1WithSlash(svf, "GUIDANCE", "F130", mdd._tbAdvisecd, null);
//                    }
                    // 臨床医学検査(心電図)
                    setHeartMedExam(svf, mdd._heartMedexamRemark, mdd._heartMedexam, form);
                    // 疾病及び異常
                    setHeartdiseasecd(svf, mdd._heartdiseasecdRemark, mdd._heartdiseasecd, form);
                    svf.VrsOut("MANAGEMENT", mdd._managementDivName);
                    svf.VrsOut("MANAGEMENT2", mdd._managementRemark);
                    svf.VrsOut("BMI", mdd._bmi);
                    setName1WithSlash(svf, "ALBUMINURIA", "F020", mdd._albuminuria1cd, null);
                    setName1WithSlash(svf, "URICSUGAR", "F019", mdd._uricsugar1cd, null);
                    setName1WithSlash(svf, "URICBLEED", "F018", mdd._uricbleed1cd, null);
                    setName1WithSlash(svf, "ALBUMINURIA2", "F020", mdd._albuminuria2cd, null);
                    setName1WithSlash(svf, "URICSUGAR2", "F019", mdd._uricsugar2cd, null);
                    setName1WithSlash(svf, "URICBLEED2", "F018", mdd._uricbleed2cd, null);
//                    if ("2".equals(printKenkouSindanIppan)) {
//                        setNameWithSlash(svf, "URINE_OTHERS", "F022", mdd._uricothertestCd, 10, "", "1", null);
//                    } else {
                        svf.VrsOut("URINE_OTHERS" + (Util.getMS932ByteLength(mdd._uricothertest) > 20 ? "1" : ""), mdd._uricothertest);
//                    }
                    setName1WithSlash(svf, "URI_ADVISECD", "F021", mdd._uriAdvisecd, null);
                    svf.VrsOut("ANEMIA", mdd._anemiaRemark);
                    svf.VrsOut("HEMOGLOBIN", mdd._hemoglobin);
                    setName(svf, "PARASITE", "F023", mdd._parasite, NAME1, 0, "", "");
//                    // その他の疾病及び異常
//                    if ("2".equals(printKenkouSindanIppan)) {
//                        if (isSonota("F140", mdd._otherdiseasecd)) {
//                            setSonotaText3(svf,
//                                    "OTHERDISEASE_REMARK1_2", "OTHERDISEASE_REMARK2_2", "OTHERDISEASE_REMARK3_2",
//                                    30, "OTHERDISEASE_REMARK1", "OTHERDISEASE_REMARK2", "OTHERDISEASE_REMARK3",
//                                    mdd._otherRemark, mdd._otherRemark2, mdd._otherRemark3);
//                        } else {
//                            setName1WithSlash(svf, "OTHERDISEASE", "F140", mdd._otherdiseasecd, "SLASH_OTHERDISEASE");
//                        }
//                    } else {
                        setName1WithSlash(svf, "OTHERDISEASE", "F140", mdd._otherdiseasecd, "SLASH_OTHERDISEASE");
//                    }
                    if ("1".equals(printKenkouSindanIppan)) {
                        setName1WithSlash(svf, "OTHER_ADVISE", "F145", mdd._otherAdvisecd, "SLASH_OTHERDISEASE");
                    }
                    svf.VrsOut("OTHER_ADVISECD2", mdd._otherRemark);
                    // 学校医
                    if (_param._isMie) {
                    	if (mdd._docCd != null) {
                            if (!"1".equals(_param._kenkouSindanIppanNotPrintNameMstComboNamespare2Is1) || !"1".equals(_param.getNameMst("F144", mdd._docCd, "NAMESPARE2"))) {
                                svf.VrsOut("VIEWS2_1", getName("F144", mdd._docCd, NAME1, ""));
                            }
                    	}
                    	final String docName = getDocName(_db2, _param._year, regdDat._schregNo);
                        svf.VrsOut("VIEWS2_2", StringUtils.defaultString(docName, ""));
                    } else {
                        svf.VrsOut("VIEWS2_1", StringUtils.defaultString(mdd._docRemark, ""));
                    }
                    // 月日
                    svf.VrsOut("DOC_DATE", KNJ_EditDate.h_format_JP(db2, mdd._docDate));
//                    if (null != regdDat._medexamDetDat._docRemark && null != regdDat._medexamDetDat._docDate && null != _printStamp) {
//                        svf.VrsOut("STAFFBTMC", getStampImageFile(regdDat._year));
//                    }
                    // 事後措置
//                    if ("2".equals(printKenkouSindanIppan)) {
//                        if (isSonota("F150", mdd._treatcd)) {
//                            setSonotaText3(svf,
//                                    "DOC_TREAT_REMARK1_2", "DOC_TREAT_REMARK2_2", "DOC_TREAT_REMARK3_2",
//                                    30, "DOC_TREAT_REMARK1", "DOC_TREAT_REMARK2", "DOC_TREAT_REMARK3",
//                                    mdd._treatRemark1, mdd._treatRemark2, mdd._treatRemark3);
//                        } else {
//                            setName1WithSlash(svf, "DOC_TREAT1", "F150", mdd._treatcd, "SLASH_DOC_TREAT");
//                        }
//                    } else {
                        setName1WithSlash(svf, "DOC_TREAT1", "F150", mdd._treatcd, "SLASH_DOC_TREAT");
//                    }
                    // 備考
                    if (null != mdd._sitheight) {
                        final int maxKeta = FORM_KNJF030A_1_5.equals(form) || FORM_KNJF030A_1P_5.equals(form) ? 14 : 20;
                        String remark1 = "座高（" + mdd._sitheight + "cm）";
                        remark1 += StringUtils.repeat(" ", maxKeta - Util.getMS932ByteLength(remark1));
                        remark1 += StringUtils.defaultString(mdd._remark);
                        svf.VrsOut("NOTE1"     ,  remark1);
                    } else {
                        svf.VrsOut("NOTE1"     ,  mdd._remark);
                    }

//                }
                }
                dataCnt++;

                svf.VrEndRecord();
                _hasData = true;
            }
            return dataCnt;
        }

//        private void print1(final DB2UDB db2, final Vrw32alp svf, final List studentList) {
//            final int maxLine = 5;
//            final String form = "KNJF030D_1.frm";
//            log.info(" form = " + form);
//
//            for (int sti = 0; sti < studentList.size(); sti++) {
//                final Student student = (Student) studentList.get(sti);
//                if (null == student._medexamToothDat) {
//                    continue;
//                }
//
//                svf.VrSetForm(form, 4);
//
//                svf.VrsOut("SCHREGNO", student._schregno); //
//                svf.VrsOut("SEX", null); // 性別
//                svf.VrsOut("NAME_SHOW", null); // 氏名
//                svf.VrsOut("BIRTHDAY", null); // 生年月日
//                svf.VrsOut("NAME_HEADER", null); // 名前ヘッダ
//                svf.VrsOut("NAME_SHOW_2", null); // 氏名
//                svf.VrsOut("SCHOOL_NAME", _param._certifSchoolDatSchoolName); // 学校名
//                svf.VrsOut("DISEASE_NAME_TITLE", null); // 結核・病名名称
//
//                for (int j = 0; j < maxLine; j++) {
//                    final int line = j + 1;
//                    svf.VrsOutn("GRADE", line, null); // 学年
//                    svf.VrsOutn("HR_NAME2_1", line, null); // クラス
//                    svf.VrsOutn("HR_NAME1", line, null); // クラス
//                    svf.VrsOutn("HR_NAME2_2", line, null); // クラス
//                    svf.VrsOutn("ATTENDNO", line, null); // 出席番号
//                }
//
//                // record RECORD
//                for (int i = 0; i < maxLine; i++) {
//                    svf.VrsOut("AGE", null); // 年齢
//                    svf.VrsOut("M_DATE", null); // 健康診断年月日
//                    svf.VrsOut("HEIGHT", null); // 身長
//                    svf.VrsOut("WEIGHT", null); // 体重
//                    svf.VrsOut("NUTRITION", null); // 栄養状態
//                    svf.VrsOut("SPINERIB", null); // 脊柱・胸郭
//                    svf.VrsOut("SPINERIBCD_REMARK", null); // 脊柱・胸郭備考
//                    svf.VrsOut("R_BAREVISION", null); // 視力・右
//                    svf.VrsOut("R_VISION", null); // 視力・右・矯正
//                    svf.VrsOut("L_BAREVISION", null); // 視力・左
//                    svf.VrsOut("L_VISION", null); // 視力・左・矯正
//                    svf.VrsOut("EYEDISEASE", null); // 目の疾病
//                    svf.VrsOut("EYE_TEST_RESULT", null); // 眼備考
//                    svf.VrsOut("R_EAR_DB", null); // 聴力・右
//                    svf.VrsOut("R_EAR", null); //
//                    svf.VrsOut("L_EAR_DB", null); // 聴力・左
//                    svf.VrsOut("L_EAR", null); //
//                    svf.VrsOut("NOSEDISEASE", null); // 耳鼻咽頭疾患
//                    svf.VrsOut("NOSEDISEASE2", null); // 耳鼻咽頭疾患2
//                    svf.VrsOut("NOSEDISEASECD_REMARK", null); // 耳鼻咽頭備考
//                    svf.VrsOut("SKINDISEASE", null); // 皮膚疾患
//                    svf.VrsOut("PHOTO_DATE", null); // 撮影日
//                    svf.VrsOut("FILMNO", null); // フィルム番号
//                    svf.VrsOut("VIEWS1_1", null); // 間接撮影・所見
//                    svf.VrsOut("TB_X_RAY", null); // 結核備考
//                    svf.VrsOut("OTHERS", null); // 結核・その他の検査
//                    svf.VrsOut("DISEASE_NAME", null); // 結核・病名
//                    svf.VrsOut("GUIDANCE", null); // 指導区分
//                    svf.VrsOut("HEART_MEDEXAM2_1", null); // 心電図
//                    svf.VrsOut("HEART_MEDEXAM", null); // 心電図
//                    svf.VrsOut("HEART_MEDEXAM2_2", null); // 心電図
//                    svf.VrsOut("HEART_MEDEXAM_2", null); // 心電図
//                    svf.VrsOut("HEARTDISEASECD_REMARK", null); // 心臓備考
//                    svf.VrsOut("HEART_MEDEXAM2_3", null); // 心電図
//                    svf.VrsOut("HEARTDISEASECD_REMARK1", null); // 心臓備考
//                    svf.VrsOut("HEARTDISEASE2_1", null); // 心臓・疾病
//                    svf.VrsOut("HEARTDISEASE1", null); // 心臓・疾病
//                    svf.VrsOut("HEARTDISEASE2_2", null); // 心臓・疾病
//                    svf.VrsOut("HEARTDISEASE1_2", null); // 心臓・疾病
//                    svf.VrsOut("HEARTDISEASE2", null); // 心臓・疾病
//                    svf.VrsOut("HEARTDISEASE2_3", null); // 心臓・疾病
//                    svf.VrsOut("ALBUMINURIA", null); // 尿・蛋白
//                    svf.VrsOut("URICSUGAR", null); // 尿・糖
//                    svf.VrsOut("URICBLEED", null); // 尿・潜血
//                    svf.VrsOut("ALBUMINURIA2", null); // 尿・蛋白
//                    svf.VrsOut("URICSUGAR2", null); // 尿・糖
//                    svf.VrsOut("URICBLEED2", null); // 尿・潜血
//                    svf.VrsOut("URINE_OTHERS1", null); // 尿・その他の検査
//                    svf.VrsOut("URINE_OTHERS", null); // 尿・その他の検査
//                    svf.VrsOut("URINE_OTHERS2", null); // 尿・その他の検査
//                    svf.VrsOut("OTHERDISEASE", null); // その他の疾病および異常
//                    svf.VrsOut("OTHER_ADVISE", null); // 指導区分
//                    svf.VrsOut("OTHER_ADVISECD2", null); // 指導区分
//                    svf.VrsOut("STAFFBTM", null); //
//                    svf.VrsOut("STAFFBTMC", null); //
//                    svf.VrsOut("VIEWS2_1", null); // 所見
//                    svf.VrsOut("DOC_DATE", null); // 学校医・月日
//                    svf.VrsOut("DOC_TREAT1", null); // 事後措置
//                    svf.VrsOut("DOC_TREAT2", null); // 事後措置
//                    svf.VrsOut("NOTE1", null); // 備考
//                    svf.VrsOut("NOTE2", null); // 備考
//                    svf.VrsOut("NOTE3", null); // 備考
//                    svf.VrsOut("NOTE4", null); // 備考
//                    svf.VrsOut("NOTE5", null); // 備考
//                    svf.VrsOut("NOTE6", null); // 備考
//
//                    svf.VrEndRecord();
//                }
//            }
//        }

        private static String getDocName(final DB2UDB db2, final String yStr, final String schregno) {
        	final String query =" SELECT T1.DET_REMARK1 FROM MEDEXAM_DET_DETAIL_DAT T1  WHERE T1.YEAR = '" + yStr + "' AND T1.SCHREGNO = '" + schregno + "' AND T1.DET_SEQ = '013' ";
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, query));

        }
        private int indexOfKetaLimit(final String data, final int limit) {
        	if (null == data) {
        		return -1;
        	}
        	int ketaTotal = 0;
        	for (int i = 0; i < data.length(); i++) {
        		final String ch = String.valueOf(data.charAt(i));
        		final int keta = Util.getMS932ByteLength(ch);
        		if (ketaTotal + keta >= limit) {
        			return i + 1;
        		}
        		ketaTotal += keta;
        	}
			return -1;
		}

		private void setSonotaText3(final Vrw32alp svf, final String field1_1, final String field2_1, final String field3_1, final int keta,
                final String field1_2, final String field2_2, final String field3_2,
                final String data1, final String data2, final String data3) {
            final String[] field_1 = new String[] {field1_1, field2_1, field3_1};
            final String[] field_2 = new String[] {field1_2, field2_2, field3_2};
            final String[] data = new String[] {data1, data2, data3};
            setSonotaTextArray(svf, field_1, keta, field_2, data);
        }

        private void setSonotaTextArray(final Vrw32alp svf, final String[] field_1, final int keta, final String[] field_2, final String[] data) {
            if (0 < keta && keta < Util.getMaxMS932ByteLength(data)) {
                Util.svfVrsOutArray(svf, field_2, data);
            } else {
                Util.svfVrsOutArray(svf, field_1, data);
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
                    final int ketaHEART_MEDEXAM;
                    final int ketaHEART_MEDEXAM_2;
                    if (FORM_KNJF030A_1_5.equals(form) || FORM_KNJF030A_1P_5.equals(form)) {
                        ketaHEART_MEDEXAM = 12 + 12; // リンクフィールド
                        ketaHEART_MEDEXAM_2 = 11 + 11; // リンクフィールド
                        final int ketaHEART_MEDEXAM2_2 = 21 * 2; // リンクフィールド
                        final int ketaHEART_MEDEXAM2_3 = 21 * 2; // リンクフィールド
                        if (remarklen <= ketaHEART_MEDEXAM + ketaHEART_MEDEXAM_2) {
                            fieldlen = new int[] {ketaHEART_MEDEXAM, ketaHEART_MEDEXAM_2};
                            fieldname = new String[] {"HEART_MEDEXAM", "HEART_MEDEXAM_2"};
                        } else {
                            fieldlen = new int[] {ketaHEART_MEDEXAM2_2, ketaHEART_MEDEXAM2_3};
                            fieldname = new String[] {"HEART_MEDEXAM2_2", "HEART_MEDEXAM2_3"};
                        }
                        Util.svfVrsOutArray(svf, fieldname, Util.getMS932ByteToken(heartMedexamRemark, fieldlen));
                    } else {
                        ketaHEART_MEDEXAM = 20;
                        ketaHEART_MEDEXAM_2 = 22;
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
                }
            } else {
                final String remark = heartMedexamRemark;
                final int remarklen = Util.getMS932ByteLength(remark);
                // log.debug(" heartmedexam remarklen = " + remarklen);
                final int[] fieldlen;
                final String[] fieldname;
                final int ketaHEART_MEDEXAM_2;
                if (FORM_KNJF030A_1_5.equals(form) || FORM_KNJF030A_1P_5.equals(form)) {
                    setName1WithSlash(svf, "HEART_MEDEXAM", "F080", heartMedexam, "SLASH_HEART_MEDEXAM");
                    ketaHEART_MEDEXAM_2 = 11 + 11; // リンクフィールド
                    final int ketaHEART_MEDEXAM2_2 = 21 * 2; // リンクフィールド
                    final int ketaHEART_MEDEXAM2_3 = 21 * 2; // リンクフィールド
                    if (remarklen <= ketaHEART_MEDEXAM_2) {
                        fieldlen = new int[] {ketaHEART_MEDEXAM_2};
                        fieldname = new String[] {"HEART_MEDEXAM_2"};
                    } else {
                        fieldlen = new int[] {ketaHEART_MEDEXAM2_2, ketaHEART_MEDEXAM2_3};
                        fieldname = new String[] {"HEART_MEDEXAM2_2", "HEART_MEDEXAM2_3"};
                    }
                    Util.svfVrsOutArray(svf, fieldname, Util.getMS932ByteToken(heartMedexamRemark, fieldlen));
                } else {
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
        }

        private void setHeartdiseasecd(final Vrw32alp svf, final String heartdiseasecdRemark, final String heartdiseasecd, final String form) throws SQLException {
//            log.debug(" heartdiseasecdRemark = [" + heartdiseasecdRemark + "] keta = [" + getMS932ByteLength(heartdiseasecdRemark) + "], heartdiseasecd = " + heartdiseasecd);
            if ("2".equals(printKenkouSindanIppan)) {
                if (FORM_KNJF030A_1_5.equals(form) || FORM_KNJF030A_1P_5.equals(form)) {
                    if (isSonota("F090", heartdiseasecd)) {
                        final int ketaHEARTDISEASE1 = 12 + 12; // リンクフィールド
                        final int ketaHEARTDISEASE1_2 = 11 + 11; // リンクフィールド
                        final int[] fieldlen = new int[] {ketaHEARTDISEASE1, ketaHEARTDISEASE1_2};
                        final String[] fieldname = new String[] {"HEARTDISEASE1", "HEARTDISEASE1_2"};
                        Util.svfVrsOutArray(svf, fieldname, Util.getMS932ByteToken(heartdiseasecdRemark, fieldlen));
                    } else {
                        setName1WithSlash(svf, "HEARTDISEASE1", "F090", heartdiseasecd, "SLASH_HEARTDISEASE");
                    }
                } else {
                    if (isSonota("F090", heartdiseasecd)) {
                        svf.VrsOut("HEARTDISEASE_REMARK1", heartdiseasecdRemark);
                    } else {
                        setName1WithSlash(svf, "HEARTDISEASE1", "F090", heartdiseasecd, "SLASH_HEARTDISEASE");
                    }
                }
            } else if ("1".equals(_param._useKnjf030AHeartBiko)) {
                if (null == heartdiseasecdRemark) {
                    setName1WithSlash(svf, "HEARTDISEASE1", "F090", heartdiseasecd, "SLASH_HEARTDISEASE");
                } else {
                    final int remarklen = Util.getMS932ByteLength(heartdiseasecdRemark);
                    final int ketaHEARTDISEASE1;
                    final int ketaHEARTDISEASE1_2;
                    if (FORM_KNJF030A_1_5.equals(form) || FORM_KNJF030A_1P_5.equals(form)) {
                        ketaHEARTDISEASE1 = 12 + 12; // リンクフィールド
                        ketaHEARTDISEASE1_2 = 11 + 11; // リンクフィールド
                        final int[] fieldlen = new int[] {ketaHEARTDISEASE1, ketaHEARTDISEASE1_2};
                        final String[] fieldname = new String[] {"HEARTDISEASE1", "HEARTDISEASE1_2"};
                        Util.svfVrsOutArray(svf, fieldname, Util.getMS932ByteToken(heartdiseasecdRemark, fieldlen));
                    } else {
                        ketaHEARTDISEASE1 = 20;
                        ketaHEARTDISEASE1_2 = 22;
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
                }
            } else {
                final String kakkoHeartdiseasecdRemark = heartdiseasecdRemark;
                final int remarklen = Util.getMS932ByteLength(kakkoHeartdiseasecdRemark);
                final int ketaHEARTDISEASE1;
                final int ketaHEARTDISEASE1_2;
                final int[] fieldlen;
                if (FORM_KNJF030A_1_5.equals(form) || FORM_KNJF030A_1P_5.equals(form)) {
                    ketaHEARTDISEASE1 = 12 + 12; // リンクフィールド
                    ketaHEARTDISEASE1_2 = 11 + 11; // リンクフィールド
                    final int ketaHEARTDISEASE2_2 = 21 + 21; // リンクフィールド
                    setName1WithSlash(svf, "HEARTDISEASE1", "F090", heartdiseasecd, "SLASH_HEARTDISEASE");
                    if (remarklen <= ketaHEARTDISEASE1_2) {
                        fieldlen = new int[] {ketaHEARTDISEASE1_2};
                        Util.svfVrsOutArray(svf, new String[] {"HEARTDISEASE1_2"}, Util.getMS932ByteToken(kakkoHeartdiseasecdRemark, fieldlen));
                    } else {
                        fieldlen = new int[] {ketaHEARTDISEASE2_2};
                        Util.svfVrsOutArray(svf, new String[] {"HEARTDISEASE2_2"}, Util.getMS932ByteToken(kakkoHeartdiseasecdRemark, fieldlen));
                    }
                } else {
                    ketaHEARTDISEASE1 = 22;
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

        protected String setName(
                final Vrw32alp svf,
                final String fieldName,
                final String nameCd1,
                final String nameCd2,
                final String useFieldName,
                final int lineLength,
                final String plusString,
                final String fieldNameOver
        ) throws SQLException {
            final int checkNameCd2 = NumberUtils.isDigits(nameCd2) ? Integer.parseInt(nameCd2) : -1;
            final String sql = "SELECT VALUE(" + useFieldName + ", '') AS LABEL FROM NAME_MST WHERE NAMECD1 = '" + nameCd1 + "' AND NAMECD2 = '" + nameCd2 + "'";
            ResultSet rs = null;
            PreparedStatement ps = null;
            String retStr = "";
            try {
                ps = _db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String setName = checkNameCd2 == -1 ? "" + plusString : rs.getString("LABEL") + plusString;
                    if (lineLength > 0 && setName.length() > lineLength) {
                        svf.VrsOut(fieldName + fieldNameOver, setName);
                    } else {
                        svf.VrsOut(fieldName, setName);
                    }
                    retStr = setName;
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                _db2.commit();
            }
            return retStr;
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
//        final String _schoolJudge;
        final String _date;
        final List _checkList = new ArrayList();
        final String _nyocheck1;
        final String _nyocheck2;
        final String _nyocheck3;
        final String _hyoji1; // 校長名を表示
        final String _hyoji2; // プール指導を表示
        final String _hyoji3; // 医師「印」を表示
        final String _hyoji4; // 文言「保護者名」を表示
        private String _textDoctorStamp;

//        final String _ctrlDate;
        final String _namemstZ010Name1;
        final boolean _isRisshisha;
        final boolean _isMie;
//        final String _printStamp;
//        private Map _yearKouiStampNo; // 学校医印鑑
//        private Map _yearRemark6Staffname; // 校長名
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
        private String _staffname;
        private int _staffnameKeta;
        private String _hiduke;
        private String _charge; // 担当情報
        final String _useSchool_KindField;
        final String _SCHOOLKIND;
        final Map _formFieldInfoMapMap = new HashMap();

        KNJDefineSchool _defineSchool;

        /** 写真データ格納フォルダ */
        private final String _imageDir;
//        /** 写真データの拡張子 */
//        private final String _imageExt;
        /** 陰影保管場所(陰影出力に関係する) */
        private final String _documentRoot;

        private final Map _nameMstFXX;
        private final Map _documentMstMap;
        private Map _physAvgMap = null;

        final String _useKnjf030AHeartBiko;
        final String _useParasite_P;
        final String _useParasite_J;
        final String _useParasite_H;
//        final String _useForm5_H_Ippan;
        /** 名称マスタのコンボで名称予備2=1は表示しない */
        final String _kenkouSindanIppanNotPrintNameMstComboNamespare2Is1;
        final String _slashJpgFilePath;

        final boolean _isOutputDebug;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            //  パラメータの取得
            _year = request.getParameter("YEAR");                        //年度
            _gakki = request.getParameter("GAKKI");                       //学期 1,2,3
            _kubun = request.getParameter("KUBUN");                       //1:クラス,2:個人
//            _schoolJudge = request.getParameter("SCHOOL_JUDGE");                //H:高校、J:中学
            _classSelected = request.getParameterValues("CLASS_SELECTED");
            if ("2".equals(_kubun)) {
                for (int i = 0; i < _classSelected.length; i++) {
                    _classSelected[i] = StringUtils.split(_classSelected[i], "-")[0];
                }
            }
            for (int checknoi = 1; checknoi <= 20; checknoi++) {
                if ("on".equals(request.getParameter("CHECK" + String.valueOf(checknoi)))) {
                    _checkList.add(new Integer(checknoi));
                }
            }
            _nyocheck1 = request.getParameter("NYOCHECK1");
            _nyocheck2 = request.getParameter("NYOCHECK2");
            _nyocheck3 = request.getParameter("NYOCHECK3");

            _hyoji1 = request.getParameter("HYOJI1");
            _hyoji2 = request.getParameter("HYOJI2");
            _hyoji3 = request.getParameter("HYOJI3");
            _hyoji4 = request.getParameter("HYOJI4");

            _date = request.getParameter("DATE");        // 学校への提出日

            _useKnjf030AHeartBiko = request.getParameter("useKnjf030AHeartBiko");
            _useParasite_P = request.getParameter("useParasite_P");
            _useParasite_J = request.getParameter("useParasite_J"); // 中学校で寄生虫卵を使用するか
            _useParasite_H = request.getParameter("useParasite_H"); // 高校で寄生虫卵を使用するか
//            _useForm5_H_Ippan = request.getParameter("useForm5_H_Ippan");
            _kenkouSindanIppanNotPrintNameMstComboNamespare2Is1 = request.getParameter("kenkouSindanIppanNotPrintNameMstComboNamespare2Is1");

            if (null != _date) {
                _hiduke = KNJ_EditDate.h_format_JP(db2, _date);
            }

            _defineSchool = new KNJDefineSchool();
            _defineSchool.defineCode(db2, _year);

            //  学校名・学校住所・校長名の取得
            _returnval2 = new KNJ_Schoolinfo(_year).get_info(db2);

//            _printStamp = request.getParameter("PRINT_STAMP");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLKIND = request.getParameter("SCHOOLKIND");
            _namemstZ010Name1 = getNameMstZ010(db2);
            log.info(" _namemstZ010Name1 = " + _namemstZ010Name1);
            _isRisshisha = "risshisha".equals(_namemstZ010Name1);
            _isMie = "mieken".equals(_namemstZ010Name1);

            _documentRoot = request.getParameter("DOCUMENTROOT"); // 陰影保管場所 NO001
            _imageDir = "image";
            _slashJpgFilePath = getImageFile("slash.jpg");
//            _imageExt = "bmp";
            setCertifSchoolDat(db2);

            _nameMstFXX = getNameMstFXX(db2);
//            for (final Iterator it = _nameMstFXX.keySet().iterator(); it.hasNext();) {
//                final String namecd1 = (String) it.next();
//                final Map map2 = Util.getMappedMap(_nameMstFXX, namecd1);
//                log.info(" ----:namecd1 = " + namecd1);
//                for (final Iterator it2 = map2.keySet().iterator(); it2.hasNext();) {
//                    final String namecd2 = (String) it2.next();
//                    log.info("   :namecd2 = " + namecd2 + ", map = " + map2.get(namecd2));
//                }
//            }
            _documentMstMap = getDocumentMstMap(db2);

            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));
        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJF030D' AND NAME = '" + propName + "' "));
        }

        private Map getFieldInfoMap(final Vrw32alp svf, final String form) {
            if (null == _formFieldInfoMapMap.get(form)) {
                _formFieldInfoMapMap.put(form, SvfField.getSvfFormFieldInfoMapGroupByName(svf));
            }
            return Util.getMappedMap(_formFieldInfoMapMap, form);
        }

        private static int getFieldKeta(final Map fieldnameMap, final String fieldname, final int defaultKeta) {
            int rtn = 0;
            final SvfField fieldHrName = (SvfField) fieldnameMap.get(fieldname);
            if (null == fieldHrName) {
                log.info("not found svf field: " + fieldname);
                rtn = defaultKeta;
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
                map.put(SCHOOL_NAME1, _certifSchoolDatSchoolName); //学校名１
                map.put(SCHOOL_NAME2, _certifSchoolDatSchoolName); //学校名２
                map.put(PRINCIPAL_NAME, _certifSchoolDatPrincipalName); //校長名
                map.put(PRINCIPAL_JOBNAME, _certifSchoolDatJobName);
            } else if ("J".equals(schoolKind)) {
                map.put(SCHOOL_NAME1, _certifSchoolDatRemark1); //学校名１
                map.put(SCHOOL_NAME2, _certifSchoolDatRemark1); //学校名２
                map.put(PRINCIPAL_NAME, _certifSchoolDatRemark2); //校長名
                map.put(PRINCIPAL_JOBNAME, _certifSchoolDatRemark3);
            } else if ("P".equals(schoolKind) || "K".equals(schoolKind)) {
                map.put(SCHOOL_NAME1, _certifSchoolDatRemark4); //学校名１
                map.put(SCHOOL_NAME2, _certifSchoolDatRemark4); //学校名２
                map.put(PRINCIPAL_NAME, _certifSchoolDatRemark5); //校長名
                map.put(PRINCIPAL_JOBNAME, _certifSchoolDatRemark6);
            }
            if (null == map.get(SCHOOL_NAME1)) map.put(SCHOOL_NAME1, _returnval2.SCHOOL_NAME1); //学校名１
            if (null == map.get(SCHOOL_NAME2)) map.put(SCHOOL_NAME2, _returnval2.SCHOOL_NAME2); //学校名２
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

        private void setCertifSchoolDat(final DB2UDB db2) {
//            _yearKouiStampNo = new HashMap();
//            _yearRemark6Staffname = new HashMap();
//            try {
//                final StringBuffer stb = new StringBuffer();
//                stb.append(" WITH T_INKAN AS ( ");
//                stb.append("     SELECT ");
//                stb.append("         MAX(STAMP_NO) AS STAMP_NO, ");
//                stb.append("         STAFFCD ");
//                stb.append("     FROM ");
//                stb.append("         ATTEST_INKAN_DAT ");
//                stb.append("     GROUP BY ");
//                stb.append("         STAFFCD ");
//                stb.append(" ) ");
//                stb.append(" SELECT T1.YEAR, T1.REMARK5, T2.STAMP_NO, T1.REMARK6, T3.STAFFNAME AS REMARK6_STAFFNAME ");
//                stb.append(" FROM CERTIF_SCHOOL_DAT T1 ");
//                stb.append(" LEFT JOIN T_INKAN T2 ON T2.STAFFCD = T1.REMARK5 ");
//                stb.append(" LEFT JOIN STAFF_MST T3 ON T3.STAFFCD = T1.REMARK6 ");
//                stb.append(" WHERE CERTIF_KINDCD = '124' ");
//                ps = db2.prepareStatement(stb.toString());
//                rs = ps.executeQuery();
//                while (rs.next()) {
//                    _yearKouiStampNo.put(rs.getString("YEAR"), rs.getString("STAMP_NO"));
//                    _yearRemark6Staffname.put(rs.getString("YEAR"), rs.getString("REMARK6_STAFFNAME"));
//                }
//            } catch (Exception e) {
//                log.error("Exception", e);
//            } finally {
//                DbUtils.closeQuietly(null, ps, rs);
//                db2.commit();
//            }

            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT * ");
            stb.append(" FROM CERTIF_SCHOOL_DAT T1 ");
            stb.append(" WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '125' ");

            final Map row = DB.firstRow(DB.query(db2, stb.toString()));

            _certifSchoolDatSchoolName = getString(row, "SCHOOL_NAME");
            _certifSchoolDatJobName = getString(row, "JOB_NAME");
            _certifSchoolDatPrincipalName = getString(row, "PRINCIPAL_NAME");
            _certifSchoolDatRemark1 = getString(row, "REMARK1");
            _certifSchoolDatRemark2 = getString(row, "REMARK2");
            _certifSchoolDatRemark3 = getString(row, "REMARK3");
            _certifSchoolDatRemark4 = getString(row, "REMARK4");
            _certifSchoolDatRemark5 = getString(row, "REMARK5");
            _certifSchoolDatRemark6 = getString(row, "REMARK6");
            _certifSchoolDatRemark7 = getString(row, "REMARK7");
            _staffname = StringUtils.defaultString(_certifSchoolDatJobName) + StringUtils.defaultString(_certifSchoolDatPrincipalName);
            _staffnameKeta = Util.getMS932ByteLength(_staffname);
            _charge = StringUtils.defaultString(_certifSchoolDatRemark7);
        }

        private String getDocumentMstTitle(final String docuemntcd) {
            return getString(DB.firstRow(Util.getMappedList(_documentMstMap, docuemntcd)), "TITLE");
        }
        private String getDocumentMstText(final String docuemntcd) {
            return getString(DB.firstRow(Util.getMappedList(_documentMstMap, docuemntcd)), "TEXT");
        }
        private Map getDocumentMstMap(final DB2UDB db2) {
            final String sql = " SELECT DOCUMENTCD, TITLE, TEXT FROM DOCUMENT_MST ";
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

//        /**
//         * 写真データファイルの取得
//         */
//        private String getStampImageFile(final String year) {
//            if (null == year) {
//                return null;
//            }
//            if (null == _documentRoot) {
//                return null;
//            } // DOCUMENTROOT
//            if (null == _imageDir) {
//                return null;
//            }
//            if (null == _imageExt) {
//                return null;
//            }
//            final String filename = (String) _yearKouiStampNo.get(year);
//            if (null == filename) {
//                return null;
//            }
//            final StringBuffer stb = new StringBuffer();
//            stb.append(_documentRoot);
//            stb.append("/");
//            stb.append(_imageDir);
//            stb.append("/");
//            stb.append(filename);
//            stb.append(".");
//            stb.append(_imageExt);
//            File file1 = new File(stb.toString());
//            if (!file1.exists()) {
//                return null;
//            } // 写真データ存在チェック用
//            return stb.toString();
//        }
    }

    private void putGengou1(final DB2UDB db2, final Vrw32alp svf, final String field) {
        //元号(記入項目用)
        String[] dwk;
        if (_param._date.indexOf('/') >= 0) {
            dwk = StringUtils.split(_param._date, '/');
        } else if (_param._date.indexOf('-') >= 0) {
            dwk = StringUtils.split(_param._date, '-');
        } else {
            //ありえないので、固定値で設定。
            dwk = new String[1];
        }
        if (dwk.length >= 3) {
            final String gengou = KNJ_EditDate.gengou(db2, Integer.parseInt(dwk[0]), Integer.parseInt(dwk[1]), Integer.parseInt(dwk[2]));
            svf.VrsOut(field, gengou);
        }
    }

    private void putGengou2(final DB2UDB db2, final Vrw32alp svf, final List fieldList) {
        //元号(記入項目用)
        String[] dwk;
        if (_param._date.indexOf('/') >= 0) {
            dwk = StringUtils.split(_param._date, '/');
        } else if (_param._date.indexOf('-') >= 0) {
            dwk = StringUtils.split(_param._date, '-');
        } else {
            //ありえないので、固定値で設定。
            dwk = new String[1];
        }
        if (dwk.length >= 3) {
            final String gengou = KNJ_EditDate.gengou(db2, Integer.parseInt(dwk[0]), Integer.parseInt(dwk[1]), Integer.parseInt(dwk[2]));
            for (final Iterator it = fieldList.iterator(); it.hasNext();) {
                final String setFieldStr = (String) it.next();
                svf.VrsOut(setFieldStr, gengou);
            }
        }
    }

}//クラスの括り
