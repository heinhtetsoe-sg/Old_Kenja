/*
 * $Id: 9e81f6d3989ed46d64f05ea796138f34c12bb2ac $
 *
 * 作成日: 2019/01/31
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

public class KNJF030H {

    private static final Log log = LogFactory.getLog(KNJF030H.class);

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

        //log.fatal("$Revision: 77185 $"); // CVSキーワードの取り扱いに注意
        log.fatal("$Id: 9e81f6d3989ed46d64f05ea796138f34c12bb2ac $");
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

            printMain(svf, db2);
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

    private void printMain(final Vrw32alp svf, DB2UDB db2) throws SQLException {
        final List studentList = Student.getStudentList(db2, _param);

        log.info(" check = " + _param._checkList);
        log.info(" studentList size = " + studentList.size());

        //SVF出力
        for (int i = 0; i < _param._checkList.size(); i++) {
            final int n = ((Integer) _param._checkList.get(i)).intValue();
            if (n == 1) {
                _hasdata = new KNJF030A().printMain(svf, db2, _param, studentList) || _hasdata;
            } else if (n == 2) { print2(db2, svf, studentList);
            } else if (n == 3) { print3(svf, studentList);
            } else if (n == 4) {
                if (_param._isSagaken) {
                    print4saga(db2, svf, studentList);
                } else {
                    print4(svf, studentList);
                }
            } else if (n == 5) {
                if (_param._isSagaken) {
                    print5saga(db2, svf, studentList);
                } else {
                    print5(svf, studentList);
                }
            } else if (n == 6) {
                if (_param._isSagaken) {
                    print6saga(db2, svf, studentList);
                } else {
                    print6(svf, studentList);
                }
            } else if (n == 7) {
                if (_param._isSagaken) {
                    print7saga(db2, svf, studentList);
                } else {
                    print7(svf, studentList);
                }
            } else if (n == 8) { print8(db2, svf, studentList);
            } else if (n == 9) { print9(db2, svf, studentList);
            } else if (n == 10) {
                if (_param._isSagaken) {
                    print10saga(db2, svf, studentList);
                } else {
                    print10(svf, studentList);
                }
            } else if (n == 11) { print11(svf, studentList);
            } else if (n == 12) { print12(svf, studentList);
            } else if (n == 13) { print13(db2, svf, studentList);
            } else if (n == 14) { print14(svf, studentList);
            } else if (n == 15 || n == 16 || n == 17) { print15_16_17(n, svf, studentList);
            } else if (n == 18) { print18(svf, studentList);
            } else if (n == 19) {
            	if ("2".equals(_param._n19Sort)) {
                    for (int cnt = 0; cnt < 3; cnt++) {
                    	String prtType = "";
                    	switch (cnt) {
                    	case 0:
                    		prtType = "on".equals(_param._n19Check1) ? "1" : "";
                    		break;
                    	case 1:
                    		prtType = "on".equals(_param._n19Check2) ? "2" : "";
                    		break;
                    	case 2:
                    		prtType = "on".equals(_param._n19Check3) ? "3" : "";
                    		break;
                    	default:
                    		break;
                    	}
                    	if (!"".equals(prtType)) {
                	        print19(svf, studentList, prtType);
                    	}
            		}
            	} else {
            	    print19(svf, studentList, "ALL");
            	}
            } else if (n == 20) { print20(db2, svf, studentList);
            }
        }
    }

    private static String getString(final Map map, final String field) {
    	return KnjDbUtils.getString(map, field);
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
            final int cnt = data.length() / count;
            final int cntAmari = data.length() % count;
            final int forCnt = cntAmari > 0 ? cnt + 1 : cnt;
            final String[] retStr = new String[forCnt];
            String dataHoge = data;
            for (int i = 0; i < forCnt; i++) {
                if (dataHoge.length() < count) {
                    retStr[i] = dataHoge.substring(0, dataHoge.length());
                    dataHoge = dataHoge.substring(dataHoge.length());
                } else {
                    retStr[i] = dataHoge.substring(0, count);
                    dataHoge = dataHoge.substring(count);
                }
            }
            return retStr;
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

        private static int toInt(final String s, final int def) {
            if (!NumberUtils.isDigits(s)) {
                return def;
            }
            return Integer.parseInt(s);
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
            // 日本小児内分泌学会 (http://jspe.umin.jp/)
            // http://jspe.umin.jp/ipp_taikaku.htm ２．肥満度 ２）性別・年齢別・身長別標準体重（５歳以降）のデータによる
            // ａ＝ HEXAM_PHYSICAL_AVG_DAT.STD_WEIGHT_KEISU_A
            // ｂ＝ HEXAM_PHYSICAL_AVG_DAT.STD_WEIGHT_KEISU_B　
            // 標準体重＝ａ×身長（cm）- ｂ 　 　
            final BigDecimal height = new BigDecimal(student._medexamDetDat._height);
            final String kihonDate = param._year + "-04-01";
            final int iNenrei = (int) getNenrei(student, kihonDate, param._year, param._year);
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
            final int fieldKeta = Param.getFieldKeta(_param.getFieldInfoMap(svf, form), fields[i]);
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


        for (int sti = 0; sti < studentList.size(); sti++) {
            final Student student = (Student) studentList.get(sti);

            if (null == student._medexamToothDat) {
                continue;
            }
            final String form;
            final int maxLine;
            if(_param._isRitsumeikan && "P".equals(student._schoolKind)) {
            	form = "KNJF030H_2_3.frm";
            	maxLine = 6;
            } else {
            	form = "KNJF030H_2.frm";
            	maxLine = 5;
            }

            log.info(" form = " + form);


            svf.VrSetForm(form, 4);

            final String notPrintSubTitle = (String) _param._notPrintSubTitleSchoolkindMap.get(student._schoolKind);
            final String title = "KNJF030H_2_3.frm".equals(form) ? "児童生徒健康診断票（歯・口腔）" :"生徒学生健康診断票（歯・口腔）";
            svf.VrsOut("TITLE",  title);
            svf.VrsOut("SUBTITLE", "1".equals(notPrintSubTitle) ? "" : "高等学校等用"); // サブタイトル
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
                    String dentistRemark = StringUtils.defaultString(_param.getNameMstName1("F540", mtd._dentistremarkcd));
                    if (_param._isFukuiken) {
                    	final String[] split = Util.splitByLength(dentistRemark, 13);
                        final String[] dentistremarkField = {"DENTISTREMARK2_1", "DENTISTREMARK2_2"};
                        for (int di = 0; di < Math.min(split.length, dentistremarkField.length); di++) {
                            svf.VrsOut(dentistremarkField[di], split[di]); // 事後措置
                        }
                    	if (null != mtd._date && null != _param._certifSchoolDat124Remark10) {
                        	svf.VrsOut("DENTISTREMARK2_3", _param._certifSchoolDat124Remark10);
                        }
                    } else {
                        final String[] split = Util.splitByLength(dentistRemark, 10);
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

    /** 元号名セット */
    private void printEraName(DB2UDB db2, final Vrw32alp svf) {
        final String[] gengou = KNJ_EditDate.tate_format4(db2, StringUtils.replace(_param._date, "/", "-"));
        svf.VrsOut("ERA_NAME", gengou[0]); // 元号
    }

    /** タイトル、本文セット */
    private void printDocumentMstTitleText(final Vrw32alp svf, final String title, final List textList) {
        svf.VrsOut("TITLE", title);
        for (int i = 0; i < textList.size(); i++) {
            svf.VrsOut("TEXT" + String.valueOf(i + 1), (String) textList.get(i));
        }
    }

    /** 職員名セット */
    private void printPrincipalStaffname(final Vrw32alp svf) {
        if ("on".equals(_param._hyoji1)) {
            svf.VrsOut(_param._staffnameKeta <= 26 ? "STAFF_NAME1" : _param._staffnameKeta <= 30 ? "STAFF_NAME2" : "STAFF_NAME3", _param._staffname);
            svf.VrsOut("STAFF_NAME", _param._staffname2);
        }
    }

    private void printAttention(final Vrw32alp svf) {
        if ("on".equals(_param._hyoji2)) {
            svf.VrsOut("ATTENTION", "【水泳（プール指導）の可否】　 　可　　　・　　　否");
        }
    }

    /** 印セット */
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
            if (
                (null == mdd._rBarevisionMark)
             && (null == mdd._rVisionMark)
             && (null == mdd._rBarevision)
             && (null == mdd._rVision)
             && (null == mdd._rVisionCantMeasure)
             && (null == mdd._lBarevisionMark)
             && (null == mdd._lVisionMark)
             && (null == mdd._lBarevision)
             && (null == mdd._lVision)
             && (null == mdd._lVisionCantMeasure)
                    ) { // 入力無しなら対象外
                return true;
            }
        } else if ("05".equals(div)) {
            if (
                (null == mdd._rEar || "01".equals(mdd._rEar))
             && (null == mdd._rEarCantMeasure)
             && (null == mdd._lEar || "01".equals(mdd._lEar))
             && (null == mdd._lEarCantMeasure)
                ) {
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
            // データ有は全員出力
        } else if ("10".equals(div)) {
            if ((null == mdd._nutritioncd || "01".equals(mdd._nutritioncd))
             && (null == mdd._spineribcd || "01".equals(mdd._spineribcd))
             && (null == mdd._skindiseasecd || "01".equals(mdd._skindiseasecd))
             && (null == mdd._otherdiseasecd || "01".equals(mdd._otherdiseasecd))
                    ) {
                return true;
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
    private void print3(final Vrw32alp svf, final List studentList) {
        final String documentCd = "03";
        final String form = "KNJF030H_3.frm";
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
            svf.VrsOut("SCHOOL_NAME", _param._certifSchoolDat125SchoolName); // 学校名

            svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME1_1" : student._nenkuminamaeLen <= 60 ? "NAME1_2" : "NAME1_3", student._nenkuminamae);
            svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME2_1" : student._nenkuminamaeLen <= 60 ? "NAME2_2" : "NAME2_3", student._nenkuminamae);

            final String result = Util.mkString(new String[] {_param.getNameMstName1("F050", mdd._eyediseasecd), mdd._eyeTestResult}, " ");
            svf.VrsOut(Util.getMS932ByteLength(result) <= 36 ? "RESULT1" : Util.getMS932ByteLength(result) <= 50 ? "RESULT2" : "RESULT3", result);

            printPrincipalStaffname(svf);
            printAttention(svf);
            printInn(svf);
            printGuardName(svf);

            svf.VrEndPage();
            _hasdata = true;
        }
    }

    // 4 視力検査結果のお知らせ
    private void print4(final Vrw32alp svf, final List studentList) {
        final String documentCd = "04";
        final String form = "KNJF030H_4.frm";
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
            svf.VrsOut("SCHOOL_NAME", _param._certifSchoolDat125SchoolName); // 学校名

            // 視力
            String rBareVision = "";
            String lBareVision = "";
            String rVision = "";
            String lVision = "";
            // 数字、文字、両方データ有は文字を優先
            // 裸眼（右）セット
            if (!"".equals(mdd._rBarevisionMark) && null != mdd._rBarevisionMark && !"".equals(mdd._rBarevision) && null != mdd._rBarevision ) {
                rBareVision = mdd._rBarevisionMark;
            } else if (!"".equals(mdd._rBarevisionMark) && null != mdd._rBarevisionMark) {
                rBareVision = mdd._rBarevisionMark;
            } else if (!"".equals(mdd._rBarevision) && null != mdd._rBarevision) {
                rBareVision = mdd._rBarevision;
            }
            // 裸眼（左）セット
            if (!"".equals(mdd._lBarevisionMark) && null != mdd._lBarevisionMark && !"".equals(mdd._lBarevision) && null != mdd._lBarevision) {
                lBareVision = mdd._lBarevisionMark;
            } else if (!"".equals(mdd._lBarevisionMark) && null != mdd._lBarevisionMark) {
                lBareVision = mdd._lBarevisionMark;
            } else if (!"".equals(mdd._lBarevision) && null != mdd._lBarevision) {
                lBareVision = mdd._lBarevision;
            }
            // 矯正（右）セット
            if (!"".equals(mdd._rVisionMark) && null != mdd._rVisionMark && !"".equals(mdd._rVision) && null != mdd._rVision) {
                rVision = mdd._rVisionMark;
            } else if (!"".equals(mdd._rVisionMark) && null != mdd._rVisionMark) {
                rVision = mdd._rVisionMark;
            } else if (!"".equals(mdd._rVision) && null != mdd._rVision) {
                rVision = mdd._rVision;
            }
            // 矯正（左）セット
            if (!"".equals(mdd._lVisionMark) && null != mdd._lVisionMark && !"".equals(mdd._lVision) && null != mdd._lVision) {
                lVision = mdd._lVisionMark;
            } else if (!"".equals(mdd._lVisionMark) && null != mdd._lVisionMark) {
                lVision = mdd._lVisionMark;
            } else if (!"".equals(mdd._lVision) && null != mdd._lVision) {
                lVision = mdd._lVision;
            }

            if ("1".equals(mdd._rVisionCantMeasure)) {// 測定困難は何も印字しない
            } else {
                svf.VrsOut("EYE_R1", rBareVision); // 視力
                svf.VrsOut("EYE_R2", rVision); // 視力
            }
            if ("1".equals(mdd._lVisionCantMeasure)) {// 測定困難は何も印字しない
            } else {
                svf.VrsOut("EYE_L1", lBareVision); // 視力
                svf.VrsOut("EYE_L2", lVision); // 視力
            }

            svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME1_1" : student._nenkuminamaeLen <= 60 ? "NAME1_2" : "NAME1_3", student._nenkuminamae);
            svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME2_1" : student._nenkuminamaeLen <= 60 ? "NAME2_2" : "NAME2_3", student._nenkuminamae);

            printPrincipalStaffname(svf);
            printInn(svf);
            printGuardName(svf);

            svf.VrEndPage();
            _hasdata = true;
        }
    }

    // 4 視力検査結果のお知らせ
    private void print4saga(DB2UDB db2, final Vrw32alp svf, final List studentList) {
        final String documentCd = "04";
        final String form = "KNJF030H_4_2.frm";
        log.info(" form = " + form);

        final List documentMstTextTokenList = KNJ_EditKinsoku.getTokenList(_param.getDocumentMstText(documentCd), 80);

        for (int sti = 0; sti < studentList.size(); sti++) {
            final Student student = (Student) studentList.get(sti);
            final MedexamDetMonthDat mdd = (MedexamDetMonthDat) student._medexamDetMonthDatMap.get("104"); // 4月（1学期）
            final MedexamDetMonthDat mdd2 = (MedexamDetMonthDat) student._medexamDetMonthDatMap.get("210"); // 10月（2学期）
            if (null == mdd && null == mdd2) {
                continue;
            }
            // 全て入力無しなら対象外
            boolean nodataflg = false;
            boolean nodataflg2 = false;
            if (null != mdd) {
                if (null == mdd._rBarevisionMark && null == mdd._rBarevision &&
                    null == mdd._lBarevisionMark && null == mdd._lBarevision &&
                    null == mdd._rVisionMark && null == mdd._rVision &&
                    null == mdd._lVisionMark && null == mdd._lVision) {
                    nodataflg = true;
                }
            }
            if (null != mdd2) {
                if (null == mdd2._rBarevisionMark && null == mdd2._rBarevision &&
                    null == mdd2._lBarevisionMark && null == mdd2._lBarevision &&
                    null == mdd2._rVisionMark && null == mdd2._rVision &&
                    null == mdd2._lVisionMark && null == mdd2._lVision) {
                    nodataflg2 = true;
                }
            }
            if (nodataflg && nodataflg2) {
                continue;
            }

            svf.VrSetForm(form, 1);

            printDocumentMstTitleText(svf, _param.getDocumentMstTitle(documentCd), documentMstTextTokenList);

            svf.VrsOut("DATE", _param._hiduke); // 日付
            svf.VrsOut("SCHOOL_NAME", _param._certifSchoolDat125SchoolName); // 学校名

            // 4月
            if (null != mdd) {
                final String rBareVision = getVision(mdd._rBarevisionMark, mdd._rBarevision);
                final String lBareVision = getVision(mdd._lBarevisionMark, mdd._lBarevision);
                final String rVision = getVision(mdd._rVisionMark, mdd._rVision);
                final String lVision = getVision(mdd._lVisionMark, mdd._lVision);

                svf.VrsOut("EYE_R1", rBareVision); // 裸眼（右）
                svf.VrsOut("EYE_R2", rVision); // 矯正（右）
                svf.VrsOut("EYE_L1", lBareVision); // 裸眼（左）
                svf.VrsOut("EYE_L2", lVision); // 矯正（左）
            }
            // 10月
            if (null != mdd2) {
                final String rBareVision2 = getVision(mdd2._rBarevisionMark, mdd2._rBarevision);
                final String lBareVision2 = getVision(mdd2._lBarevisionMark, mdd2._lBarevision);
                final String rVision2 = getVision(mdd2._rVisionMark, mdd2._rVision);
                final String lVision2 = getVision(mdd2._lVisionMark, mdd2._lVision);
                if(!"1".equals(_param._gakki)){
                    svf.VrsOut("EYE_R1_2", rBareVision2); // 裸眼（右）
                    svf.VrsOut("EYE_R2_2", rVision2); // 矯正（右）
                    svf.VrsOut("EYE_L1_2", lBareVision2); // 裸眼（左）
                    svf.VrsOut("EYE_L2_2", lVision2); // 矯正（左）
                }
            }

            svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME1_1" : student._nenkuminamaeLen <= 60 ? "NAME1_2" : "NAME1_3", student._nenkuminamae);

            printPrincipalStaffname(svf);
            printInn(svf);
            printEraName(db2, svf); // 元号名

            svf.VrEndPage();
            _hasdata = true;
        }
    }

    // 数字、文字、両方データ有は文字を優先
    private String getVision(final String visionMark, final String vision) {
        String rtnStr = "";
        if (!"".equals(visionMark) && null != visionMark && !"".equals(vision) && null != vision ) {
            rtnStr = visionMark;
        } else if (!"".equals(visionMark) && null != visionMark) {
            rtnStr = visionMark;
        } else if (!"".equals(vision) && null != vision) {
            rtnStr = vision;
        }
        return rtnStr;
    }

    // 5 聴力検査結果のお知らせ
    private void print5(final Vrw32alp svf, final List studentList) {
        final String documentCd = "05";
        final String form = "KNJF030H_5.frm";
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

            if ("1".equals(mdd._rEarCantMeasure)) { // 測定困難は何も印字しない
            } else {
                svf.VrsOut("R_EAR", _param.getNameMstName1("F010", mdd._rEar)); // 状態コンボ
            }
            if ("1".equals(mdd._lEarCantMeasure)) { // 測定困難は何も印字しない
            } else {
                svf.VrsOut("L_EAR", _param.getNameMstName1("F010", mdd._lEar)); // 状態コンボ
            }

            svf.VrsOut("DATE", _param._hiduke); // 日付
            svf.VrsOut("SCHOOL_NAME", _param._certifSchoolDat125SchoolName); // 学校名

            svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME1_1" : student._nenkuminamaeLen <= 60 ? "NAME1_2" : "NAME1_3", student._nenkuminamae);
            svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME2_1" : student._nenkuminamaeLen <= 60 ? "NAME2_2" : "NAME2_3", student._nenkuminamae);

            printPrincipalStaffname(svf);
            printInn(svf);
            printGuardName(svf);

            svf.VrEndPage();
            _hasdata = true;
        }
    }

    // 5 聴力検査結果のお知らせ
    private void print5saga(DB2UDB db2, final Vrw32alp svf, final List studentList) {
        final String documentCd = "05";
        final String form = "KNJF030H_5_2.frm";
        log.info(" form = " + form);

        final List documentMstTextTokenList = KNJ_EditKinsoku.getTokenList(_param.getDocumentMstText(documentCd), 80);

        for (int sti = 0; sti < studentList.size(); sti++) {
            final Student student = (Student) studentList.get(sti);
            final MedexamDetDat mdd = student._medexamDetDat;
            if (null == mdd) {
                continue;
            }
            // 全て入力無しなら対象外
            boolean nodataflg = false;
            if (null != mdd) {
                if (null == mdd._rEar &&
                    null == mdd._lEar) {
                    nodataflg = true;
                }
            }
            if (nodataflg) {
                continue;
            }

            svf.VrSetForm(form, 1);

            printDocumentMstTitleText(svf, _param.getDocumentMstTitle(documentCd), documentMstTextTokenList);

            // 右 状態コンボ
            if ("1".equals(mdd._rVisionCantMeasure)) {
                svf.VrsOut("R_EAR", "難聴の疑い");
            } else {
                svf.VrsOut("R_EAR", _param.getNameMstName1("F010", mdd._rEar));
            }
            // 左 状態コンボ
            if ("1".equals(mdd._lVisionCantMeasure)) {
                svf.VrsOut("R_EAR", "難聴の疑い");
            } else {
                svf.VrsOut("L_EAR", _param.getNameMstName1("F010", mdd._lEar));
            }
            // その他
            String rEtc = "";
            String lEtc = "";
            if ("".equals(StringUtils.defaultString(mdd._rEarDb, ""))) {
                rEtc = "右 平均dB(    )";
            } else {
                rEtc = "右 平均dB(" + mdd._rEarDb + ")";
            }
            if ("".equals(StringUtils.defaultString(mdd._lEarDb, ""))) {
                lEtc = "左 平均dB(    )";
            } else {
                lEtc = "左 平均dB(" + mdd._lEarDb + ")";
            }
            if (!"".equals(rEtc) && !"".equals(lEtc)) {
                svf.VrsOut("ETC", rEtc + "  " + lEtc);
            } else if (!"".equals(rEtc)) {
                svf.VrsOut("ETC", rEtc);
            } else if (!"".equals(lEtc)) {
                svf.VrsOut("ETC", lEtc);
            }

            svf.VrsOut("DATE", _param._hiduke); // 日付
            svf.VrsOut("SCHOOL_NAME", _param._certifSchoolDat125SchoolName); // 学校名

            svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME1_1" : student._nenkuminamaeLen <= 60 ? "NAME1_2" : "NAME1_3", student._nenkuminamae);

            printPrincipalStaffname(svf);
            printInn(svf);
            printEraName(db2, svf); // 元号名

            svf.VrEndPage();
            _hasdata = true;
        }
    }

    // 6 耳鼻咽頭検診結果のお知らせ
    private void print6(final Vrw32alp svf, final List studentList) {
        final String documentCd = "06";
        final String form = "KNJF030H_6.frm";
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
            svf.VrsOut("SCHOOL_NAME", _param._certifSchoolDat125SchoolName); // 学校名
            svf.VrsOut("RESULT1", noseDisease); // 結果備考

            svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME1_1" : student._nenkuminamaeLen <= 60 ? "NAME1_2" : "NAME1_3", student._nenkuminamae);
            svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME2_1" : student._nenkuminamaeLen <= 60 ? "NAME2_2" : "NAME2_3", student._nenkuminamae);

            printPrincipalStaffname(svf);
            printAttention(svf);
            printInn(svf);
            printGuardName(svf);

            svf.VrEndPage();
            _hasdata = true;
        }
    }

    // 6 耳鼻咽頭検診結果のお知らせ
    private void print6saga(DB2UDB db2, final Vrw32alp svf, final List studentList) {
        final String documentCd = "06";
        final String form = "KNJF030H_6_2.frm";
        log.info(" form = " + form);
        final List documentMstTextTokenList = KNJ_EditKinsoku.getTokenList(_param.getDocumentMstText(documentCd), 80);

        for (int sti = 0; sti < studentList.size(); sti++) {
            final Student student = (Student) studentList.get(sti);
            final MedexamDetDat mdd = student._medexamDetDat;
            if (null == mdd) {
                continue;
            }
            // 全て入力無しなら対象外
            boolean nodataflg = false;
            if (null != mdd) {
                if (null == mdd._nosediseasecd5 &&
                    null == mdd._nosediseasecd6 &&
                    null == mdd._nosediseasecd7) {
                    nodataflg = true;
                }
            }
            if (nodataflg) {
                continue;
            }

            svf.VrSetForm(form, 1);

            printDocumentMstTitleText(svf, _param.getDocumentMstTitle(documentCd), documentMstTextTokenList);

            svf.VrsOut("DATE", _param._hiduke); // 日付
            svf.VrsOut("SCHOOL_NAME", _param._certifSchoolDat125SchoolName); // 学校名

            final String nosedisease1 = "99".equals(mdd._nosediseasecd5) ? mdd._nosediseasecdRemark1 : _param.getNameMstName1("F061", mdd._nosediseasecd5);
            svf.VrsOut("RESULT1", nosedisease1); // 耳疾

            final String nosedisease2 = "99".equals(mdd._nosediseasecd6) ? mdd._nosediseasecdRemark2 : _param.getNameMstName1("F062", mdd._nosediseasecd6);
            svf.VrsOut("RESULT2", nosedisease2); // 鼻疾

            final String nosedisease3 = "99".equals(mdd._nosediseasecd7) ? mdd._nosediseasecdRemark3 : _param.getNameMstName1("F063", mdd._nosediseasecd7);
            svf.VrsOut("RESULT3", nosedisease3); // 咽頭

            svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME1_1" : student._nenkuminamaeLen <= 60 ? "NAME1_2" : "NAME1_3", student._nenkuminamae);

            printPrincipalStaffname(svf);
            printInn(svf);
            printEraName(db2, svf); // 元号名

            svf.VrEndPage();
            _hasdata = true;
        }
    }

    // 7 歯科検診結果のお知らせ
    private void print7(final Vrw32alp svf, final List studentList) {
        final String documentCd = "07";
        final String form = "KNJF030H_7.frm";
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
            svf.VrsOut("SCHOOL_NAME", _param._certifSchoolDat125SchoolName); // 学校名
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

            printPrincipalStaffname(svf);
            printInn(svf);
            printGuardName(svf);

            svf.VrEndPage();
            _hasdata = true;
        }
    }

    // 7 歯科検診結果のお知らせ
    private void print7saga(DB2UDB db2, final Vrw32alp svf, final List studentList) {
        final String documentCd = "07";
        final String form = "KNJF030H_7_2.frm";
        log.info(" form = " + form);
        final List documentMstTextTokenList = KNJ_EditKinsoku.getTokenList(_param.getDocumentMstText(documentCd), 80);

        for (int sti = 0; sti < studentList.size(); sti++) {
            final Student student = (Student) studentList.get(sti);
            final MedexamToothDat mtd = student._medexamToothDat;
            if (null == mtd) {
                continue;
            }
            // 全て入力無しなら対象外
            boolean nodataflg = false;
            if (null != mtd) {
                if (null == mtd._jawsJointcd &&
                    null == mtd._jawsJointcd2 &&
                    null == mtd._plaquecd &&
                    null == mtd._gumcd &&
                    null == mtd._calculuscd &&
                    null == mtd._remainbabytooth &&
                    null == mtd._remainadulttooth &&
                    null == mtd._brackBabytooth &&
                    null == mtd._brackAdulttooth &&
                    null == mtd._otherdiseasecd) {
                    nodataflg = true;
                }
            }
            if (nodataflg) {
                continue;
            }

            svf.VrSetForm(form, 1);

            printDocumentMstTitleText(svf, _param.getDocumentMstTitle(documentCd), documentMstTextTokenList);

            svf.VrsOut("DATE", _param._hiduke); // 日付
            svf.VrsOut("SCHOOL_NAME", _param._certifSchoolDat125SchoolName); // 学校名

            final String maru = "〇";
            boolean abnormalFlg = false;

            if (!"01".equals(mtd._jawsJointcd) && null != mtd._jawsJointcd) {
                svf.VrsOut("RESULT2", maru); // B)歯列・咬合
                abnormalFlg = true;
            }
            if (!"01".equals(mtd._jawsJointcd2) && null != mtd._jawsJointcd2) {
                svf.VrsOut("RESULT3", maru); // C)顎関節
                abnormalFlg = true;
            }
            if ("02".equals(mtd._plaquecd)) {
                svf.VrsOut("RESULT4_1", maru); // Da)歯垢
                abnormalFlg = true;
            }
            if ("03".equals(mtd._plaquecd)) {
                svf.VrsOut("RESULT4_2", maru); // Db)歯垢
                abnormalFlg = true;
            }
            if ("02".equals(mtd._gumcd)) {
                svf.VrsOut("RESULT5_1", maru); // Ea)歯肉
                abnormalFlg = true;
            }
            if ("03".equals(mtd._gumcd) || "03".equals(mtd._calculuscd)) {
                svf.VrsOut("RESULT5_2", maru); // Eb)歯肉or歯石
                abnormalFlg = true;
            }
            if (1 <= toIntTooth(mtd._brackAdulttooth)) {
                svf.VrsOut("RESULT6_1", maru); // Fa)要観察歯
                abnormalFlg = true;
            }
            if (1 <= toIntTooth(mtd._remainbabytooth) || 1 <= toIntTooth(mtd._remainadulttooth)) {
                svf.VrsOut("RESULT6_2", maru); // Fb)乳歯、永久歯の未処置
                abnormalFlg = true;
            }
            if (1 <= toIntTooth(mtd._brackBabytooth)) {
                svf.VrsOut("RESULT7", maru); // G)要注意乳歯
                abnormalFlg = true;
            }
            boolean abnormal8Flg = false;
            final String fixcolStr1 = "その他異常：";
            final String fixcolStr2 = "口腔異常　：";
            if (!"01".equals(mtd._otherdiseasecd) && null != mtd._otherdiseasecd) {
	            if (!"".equals(mtd._otherdiseasecd)) {
	                if (_param.isSonota("F530", mtd._otherdiseasecd)) {
	                    svf.VrsOut("RESULT8_2", fixcolStr1 + mtd._otherdisease); // テキストデータ
	                } else {
	                    svf.VrsOut("RESULT8_2", fixcolStr1 + StringUtils.defaultString(_param.getNameMstName1("F530", mtd._otherdiseasecd), "")); // テキストデータ
	                }
	                abnormal8Flg = true;
	                abnormalFlg = true;
	            }
            } else {
                svf.VrsOut("RESULT8_2", fixcolStr1);
            }
            if (!"01".equals(mtd._otherdiseasecd2) && null != mtd._otherdiseasecd2) {
	            if (!"".equals(mtd._otherdiseasecd2)) {
	                if (_param.isSonota("F531", mtd._otherdiseasecd2)) {
	                    svf.VrsOut("RESULT8_3", fixcolStr2 + mtd._otherdisease2); // テキストデータ
	                } else {
	                    svf.VrsOut("RESULT8_3", fixcolStr2 + StringUtils.defaultString(_param.getNameMstName1("F531", mtd._otherdiseasecd2), "")); // テキストデータ
	                }
	                abnormal8Flg = true;
	                abnormalFlg = true;
	            }
            } else {
                svf.VrsOut("RESULT8_3", fixcolStr2);
            }
            if (abnormal8Flg) {
                svf.VrsOut("RESULT8", maru); // H)その他
            }
            if (!abnormalFlg) {
                svf.VrsOut("RESULT1", maru); // A)全て異常なし
            }

            svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME1_1" : student._nenkuminamaeLen <= 60 ? "NAME1_2" : "NAME1_3", student._nenkuminamae);

            printPrincipalStaffname(svf);
            printInn(svf);
            printEraName(db2, svf); // 元号名

            svf.VrEndPage();
            _hasdata = true;
        }
    }

    private int toIntTooth(final String tooth) {
        return NumberUtils.isNumber(tooth) ? Integer.parseInt(tooth) : 0;
    }

    // 8 歯科検診結果のお知らせ（全員配付)
    private void print8(DB2UDB db2, final Vrw32alp svf, final List studentList) {
        final String documentCd = "08";
        final String form = "KNJF030H_8.frm";
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
            svf.VrsOut("SCHOOL_NAME", _param._certifSchoolDat125SchoolName); // 学校名

            printEraName(db2, svf); // 元号名

            printDocumentMstTitleText(svf, _param.getDocumentMstTitle(documentCd), documentMstTextTokenList);

            // 異常なし
            if ("01".equals(mtd._dentistremarkcd)) {
                svf.VrsOut("RESULT1", "○");
            }

            /** 経過観察 */
            // CO
            if (0 < Util.toInt(mtd._brackAdulttooth, 0)) {
                svf.VrsOut("RESULT2_1", "○");
            }

            // GO
            if ("02".equals(mtd._gumcd)) {
                svf.VrsOut("RESULT2_2", "○");
            }

            // 歯垢
            if ("02".equals(mtd._plaquecd) || "03".equals(mtd._plaquecd)) {
                svf.VrsOut("RESULT2_3", "○");
            }

            // 顎関節・かみ合わせ・歯並び
            if ("02".equals(mtd._jawsJointcd) || "02".equals(mtd._jawsJointcd2) || "02".equals(mtd._jawsJointcd3)) {
                svf.VrsOut("RESULT2_4", "○");
            }

            /** 受診をお勧めします */
            // むし歯
            if (0 < Util.toInt(mtd._remainbabytooth, 0) || 0 < Util.toInt(mtd._remainadulttooth, 0)) {
                svf.VrsOut("RESULT3_1", "○");
            }

            // 歯肉の病気
            if ("03".equals(mtd._gumcd)) {
                svf.VrsOut("RESULT3_2", "○");
            }

            // CO要相談、要注意乳歯
            if (0 < Util.toInt(mtd._brackBabytooth, 0)) {
                svf.VrsOut("RESULT3_3", "○");
            }

            // 顎関節・噛み合わせ・歯並びの相談
            if ("03".equals(mtd._jawsJointcd) || "03".equals(mtd._jawsJointcd2) || "03".equals(mtd._jawsJointcd3)) {
                svf.VrsOut("RESULT3_4", "○");
            }

            // 歯石の沈着
            if ("02".equals(mtd._calculuscd) || "03".equals(mtd._calculuscd)) {
                svf.VrsOut("RESULT3_5", "○");
            }

            // その他
            if (null == mtd._otherdiseasecd || "01".equals(mtd._otherdiseasecd)) {
            } else if (_param.isSonota("F530", mtd._otherdiseasecd)) {
                svf.VrsOut("RESULT3_6", "○");
                svf.VrsOut("ETC", mtd._otherdisease);
            } else {
                svf.VrsOut("RESULT3_6", "○");
                svf.VrsOut("ETC", _param.getNameMstName1("F530", mtd._otherdiseasecd));
            }

            svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME1_1" : student._nenkuminamaeLen <= 60 ? "NAME1_2" : "NAME1_3", student._nenkuminamae);
            svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME2_1" : student._nenkuminamaeLen <= 60 ? "NAME2_2" : "NAME2_3", student._nenkuminamae);

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
        final String form = "KNJF030H_9.frm";
        log.info(" form = " + form);
        final List documentMstTextTokenList = KNJ_EditKinsoku.getTokenList(_param.getDocumentMstText(documentCd), 80);

        for (int sti = 0; sti < studentList.size(); sti++) {
            final Student student = (Student) studentList.get(sti);

            svf.VrSetForm(form, 1);

            printDocumentMstTitleText(svf, _param.getDocumentMstTitle(documentCd), documentMstTextTokenList);

            svf.VrsOut("DATE", _param._hiduke); // 日付
            svf.VrsOut("SCHOOL_NAME", _param._certifSchoolDat125SchoolName); // 学校名

            svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME1_1" : student._nenkuminamaeLen <= 60 ? "NAME1_2" : "NAME1_3", student._nenkuminamae);

            printPrincipalStaffname(svf);

            boolean printPercent = true;
            boolean printpermark = true;//"計算不能"を出力する特殊パターン以外は%記号を出力
            if (null != student._medexamDetDat) {
                final MedexamDetDat mdd = student._medexamDetDat;
                svf.VrsOut("HEIGHT", Util.sishagonyu(mdd._height)); // 身長
                svf.VrsOut("WEIGHT", Util.sishagonyu(mdd._weight)); // 体重
                if ("1".equals(mdd._noPrintObesityIndex)) { // 帳票印字無し
                    printPercent = false;
                } else {
                    final String outStr = Util.calcHimando(student, db2, _param);
                    if (outStr == null) {
                        svf.VrsOut("OBESITY_IMP", "計算不能");
                        printpermark = false;
                    } else {
                        svf.VrsOut("OBESITY", outStr); // 肥満度
                    }
                }

                // 視力
                String rBareVision = "";
                String lBareVision = "";
                String rVision = "";
                String lVision = "";
                // 数字、文字、両方データ有は文字を優先
                // 裸眼（右）セット
                if (null != mdd._rBarevisionMark && null != mdd._rBarevision) {
                    rBareVision = mdd._rBarevisionMark;
                } else if (null != mdd._rBarevisionMark) {
                    rBareVision = mdd._rBarevisionMark;
                } else if (null != mdd._rBarevision) {
                    rBareVision = mdd._rBarevision;
                }
                // 裸眼（左）セット
                if (null != mdd._lBarevisionMark && null != mdd._lBarevision) {
                    lBareVision = mdd._lBarevisionMark;
                } else if (null != mdd._lBarevisionMark) {
                    lBareVision = mdd._lBarevisionMark;
                } else if (null != mdd._lBarevision) {
                    lBareVision = mdd._lBarevision;
                }
                // 矯正（右）セット
                if (null != mdd._rVisionMark && null != mdd._rVision) {
                    rVision = mdd._rVisionMark;
                } else if (null != mdd._rVisionMark) {
                    rVision = mdd._rVisionMark;
                } else if (null != mdd._rVision) {
                    rVision = mdd._rVision;
                }
                // 矯正（左）セット
                if (null != mdd._lVisionMark && null != mdd._lVision) {
                    lVision = mdd._lVisionMark;
                } else if (null != mdd._lVisionMark) {
                    lVision = mdd._lVisionMark;
                } else if (null != mdd._lVision) {
                    lVision = mdd._lVision;
                }

                if ("1".equals(mdd._rVisionCantMeasure)) {// 測定困難は何も印字しない
                } else {
                    svf.VrsOut("R_BAREVISION", rBareVision); // 視力・右
                    svf.VrsOut("R_VISION",     rVision); // 視力・右・矯正
                }
                if ("1".equals(mdd._lVisionCantMeasure)) {// 測定困難は何も印字しない
                } else {
                    svf.VrsOut("L_BAREVISION", lBareVision); // 視力・左
                    svf.VrsOut("L_VISION",     lVision); // 視力・左・矯正
                }

                // 目の疾病
                svfVrsOut(svf, form, new String[] {"EYEDISEASE", "EYEDISEASE2"}, Util.mkString(new String[] {_param.getNameMstName1("F050", mdd._eyediseasecd), mdd._eyeTestResult}, " "));

                // 栄養状態
                svfVrsOut(svf, form, new String[] {"NUTRITION", "NUTRITION2"}, Util.mkString(new String[] {_param.getNameMstName1("F030", mdd._nutritioncd), mdd._nutritioncdRemark}, " "));

                // 脊柱・胸郭
                svfVrsOut(svf, form, new String[] {"SPINERIB", "SPINERIB2"}, Util.mkString(new String[] {_param.getNameMstName1("F040", mdd._spineribcd), mdd._spineribcdRemark}, " "));

                // 皮膚疾患
                svfVrsOut(svf, form, new String[] {"SKINDISEASE", "SKINDISEASE2"}, Util.mkString(new String[] {_param.getNameMstName1("F070", mdd._skindiseasecd), mdd._skindiseasecdRemark}, " "));

                final String otherdisease;
                otherdisease = Util.mkString(new String[] {_param.getNameMstName1("F140", mdd._otherdiseasecd), mdd._otherRemark}, " ");
                svfVrsOut(svf, form, new String[] {"OTHERDISEASE", "OTHERDISEASE2"}, otherdisease); // その他の疾病および異常

                svf.VrsOut("ALBUMINURIA", _param.getNameMstName1("F020", mdd._albuminuria1cd)); // 尿・蛋白
                svf.VrsOut("URICBLEED", _param.getNameMstName1("F018", mdd._uricbleed1cd)); // 尿・潜血
                svf.VrsOut("URICSUGAR", _param.getNameMstName1("F019", mdd._uricsugar1cd)); // 尿・糖
                svf.VrsOut("ALBUMINURIA2", _param.getNameMstName1("F020", mdd._albuminuria2cd)); // 尿・蛋白
                svf.VrsOut("URICBLEED2", _param.getNameMstName1("F018", mdd._uricbleed2cd)); // 尿・潜血
                svf.VrsOut("URICSUGAR2", _param.getNameMstName1("F019", mdd._uricsugar2cd)); // 尿・糖

                // 聴力
                if ("1".equals(mdd._rEarCantMeasure)) { // 測定困難は何も印字しない
                } else {
                    svf.VrsOut("R_EAR_DB", mdd._rEarDb); // 聴力・右
                    svf.VrsOut("R_EAR", _param.getNameMstName1("F010", mdd._rEar)); //
                }
                if ("1".equals(mdd._lEarCantMeasure)) { // 測定困難は何も印字しない
                } else {
                    svf.VrsOut("L_EAR_DB", mdd._lEarDb); // 聴力・左
                    svf.VrsOut("L_EAR", _param.getNameMstName1("F010", mdd._lEar)); //
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
            if (printpermark && printPercent) {
                //"計算不能"を出力する特殊パターン以外は%記号を「計算結果が出力されていなくても」出力
                svf.VrsOut("PER", "%");
            }

            svf.VrEndPage();
            _hasdata = true;
        }
    }

    // 10 内科検診結果のお知らせ
    private void print10(final Vrw32alp svf, final List studentList) {
        final String documentCd = "10";
        final String form = "KNJF030H_10.frm";
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
            svf.VrsOut("SCHOOL_NAME", _param._certifSchoolDat125SchoolName); // 学校名

            final String nutrition = Util.mkString(new String[] {_param.getNameMstName1("F030", mdd._nutritioncd), mdd._nutritioncdRemark}, " ");
            svf.VrsOut(Util.getMS932ByteLength(nutrition) <= 56 ? "NUTRITION1" : "NUTRITION2", nutrition); // 栄養状態

            final String spinerib = Util.mkString(new String[] {_param.getNameMstName1("F040", mdd._spineribcd), mdd._spineribcdRemark}, " ");
            svf.VrsOut(Util.getMS932ByteLength(spinerib) <= 56 ? "SPINERIB1" : "SPINERIB2", spinerib); // 脊柱・胸郭

            final String skindisease = Util.mkString(new String[] {_param.getNameMstName1("F070", mdd._skindiseasecd), mdd._skindiseasecdRemark}, " ");
            svf.VrsOut(Util.getMS932ByteLength(skindisease) <= 56 ? "SKINDISEASE1" : "SKINDISEASE2", skindisease); // 皮膚疾患

            final String otherdisease;
            otherdisease = Util.mkString(new String[] {_param.getNameMstName1("F140", mdd._otherdiseasecd), mdd._otherRemark}, " ");
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

            printPrincipalStaffname(svf);
            printAttention(svf);
            printInn(svf);
            printGuardName(svf);

            svf.VrEndPage();
            _hasdata = true;
        }
    }

    // 10 内科検診結果のお知らせ
    private void print10saga(DB2UDB db2, final Vrw32alp svf, final List studentList) {
        final String documentCd = "10";
        final String form = "KNJF030H_10_3.frm";
        log.info(" form = " + form);
        final List documentMstTextTokenList = KNJ_EditKinsoku.getTokenList(_param.getDocumentMstText(documentCd), 80);

        for (int sti = 0; sti < studentList.size(); sti++) {
            final Student student = (Student) studentList.get(sti);
            final MedexamDetDat mdd = student._medexamDetDat;
            if (null == mdd) {
                continue;
            }
            // 全て入力無しなら対象外
            boolean nodataflg = false;
            if (null != mdd) {
                if (null == mdd._nutritioncd &&
                    null == mdd._spineribcd &&
                    null == mdd._skindiseasecd &&
                    null == mdd._heartdiseasecd &&
                    null == mdd._otherAdvisecd) {
                    nodataflg = true;
                }
            }
            if (nodataflg) {
                continue;
            }

            svf.VrSetForm(form, 1);

            printDocumentMstTitleText(svf, _param.getDocumentMstTitle(documentCd), documentMstTextTokenList);

            svf.VrsOut("DATE", _param._hiduke); // 日付
            svf.VrsOut("SCHOOL_NAME", _param._certifSchoolDat125SchoolName); // 学校名

            svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME1_1" : student._nenkuminamaeLen <= 60 ? "NAME1_2" : "NAME1_3", student._nenkuminamae);
            svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME2_1" : student._nenkuminamaeLen <= 60 ? "NAME2_2" : "NAME2_3", student._nenkuminamae);

            final String nutrition = "99".equals(mdd._nutritioncd) ? mdd._nutritioncdRemark : _param.getNameMstName1("F030", mdd._nutritioncd);
            svf.VrsOut(Util.getMS932ByteLength(nutrition) <= 56 ? "NUTRITION1" : "NUTRITION2", nutrition); // 栄養状態
            final String chkOutputLine = _param.getNameMst("F040", mdd._spineribcd, "ABBV1");
            final String spinerib = "99".equals(mdd._spineribcd) ? mdd._spineribcdRemark : _param.getNameMstName1("F040", mdd._spineribcd);
            if ("1".equals(chkOutputLine)) {
                //コードによる制御はせず、DB側で設定
                //if ("01".equals(mdd._spineribcd) || "02".equals(mdd._spineribcd)) {
                    svf.VrsOut(Util.getMS932ByteLength(spinerib) <= 56 ? "SPINERIB1_1" : "SPINERIB1_2", spinerib); // 脊柱
                //}
            } else {
            	svf.VrsOut("SPINERIB1_1", "異常なし");
            }
            if ("2".equals(chkOutputLine)) {
                //コードによる制御はせず、DB側で設定
                //if ("01".equals(mdd._spineribcd) || "04".equals(mdd._spineribcd)) {
                    svf.VrsOut(Util.getMS932ByteLength(spinerib) <= 56 ? "SPINERIB2_1" : "SPINERIB2_2", spinerib); // 胸郭
                //}
            } else {
            	svf.VrsOut("SPINERIB2_1", "異常なし");
            }
            if ("3".equals(chkOutputLine)) {
                //コードによる制御はせず、DB側で設定
	            //if ("01".equals(mdd._spineribcd) || "05".equals(mdd._spineribcd) || "98".equals(mdd._spineribcd) || "99".equals(mdd._spineribcd)) {
	                svf.VrsOut(Util.getMS932ByteLength(spinerib) <= 56 ? "SPINERIB3_1" : "SPINERIB3_2", spinerib); // 四肢状態
	            //}
            } else {
            	svf.VrsOut("SPINERIB3_1", "異常なし");
            }

            final String skindisease = "99".equals(mdd._skindiseasecd) ? mdd._skindiseasecdRemark : _param.getNameMstName1("F070", mdd._skindiseasecd);
            svf.VrsOut(Util.getMS932ByteLength(skindisease) <= 56 ? "SKINDISEASE1" : "SKINDISEASE2", skindisease); // 皮膚疾患

            final String heartdisease = "99".equals(mdd._heartdiseasecd) ? mdd._heartdiseasecdRemark : _param.getNameMstName1("F090", mdd._heartdiseasecd);
            svf.VrsOut(Util.getMS932ByteLength(heartdisease) <= 56 ? "HEARTDISEASE1" : "HEARTDISEASE2", heartdisease); // 心臓疾患

            final String otherAdvise = "".equals(StringUtils.defaultString(mdd._uriAdvisecd, "")) ? "" : StringUtils.defaultString(_param.getNameMstName1("F021", mdd._uriAdvisecd), "");
            final String otherAdvise2 = otherAdvise + ("".equals(otherAdvise) ? "" : " ") + StringUtils.defaultString(mdd._uricothertest, "");
            svf.VrsOut(Util.getMS932ByteLength(otherAdvise2) <= 56 ? "KIDNEYDISEASE1" : "KIDNEYDISEASE2", otherAdvise2); // 腎臓疾患

            printPrincipalStaffname(svf);
            printInn(svf);
            printEraName(db2, svf); // 元号名

            svf.VrEndPage();
            _hasdata = true;
        }
    }

    // 11 運動器検診結果のお知らせ
    private void print11(final Vrw32alp svf, final List studentList) {
        final String documentCd = "11";
        final String form = "KNJF030H_11.frm";
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
            svf.VrsOut("SCHOOL_NAME", _param._certifSchoolDat125SchoolName); // 学校名
            svf.VrsOut("SPINERIB", Util.mkString(new String[] {_param.getNameMstName1("F040", mdd._spineribcd), mdd._spineribcdRemark}, " ")); // 脊柱・胸郭

            svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME1_1" : student._nenkuminamaeLen <= 60 ? "NAME1_2" : "NAME1_3", student._nenkuminamae);
            svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME2_1" : student._nenkuminamaeLen <= 60 ? "NAME2_2" : "NAME2_3", student._nenkuminamae);

            printPrincipalStaffname(svf);
            printAttention(svf);
            printInn(svf);
            printGuardName(svf);

            svf.VrEndPage();

            _hasdata = true;
        }
    }

    // 12 色覚検査結果のお知らせ
    private void print12(final Vrw32alp svf, final List studentList) {
        final String documentCd = "12";
        final String form = "KNJF030H_12.frm";
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
            svf.VrsOut("SCHOOL_NAME", _param._certifSchoolDat125SchoolName); // 学校名
            if ("01".equals(mdd._eyediseasecd5)) {
                svf.VrsOut("COLOR_VISION1", "○"); // 色覚検査
            } else if (null != mdd._eyediseasecd5) {
                svf.VrsOut("COLOR_VISION2", "○"); // 色覚検査
            }

            svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME1_1" : student._nenkuminamaeLen <= 60 ? "NAME1_2" : "NAME1_3", student._nenkuminamae);
            svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME2_1" : student._nenkuminamaeLen <= 60 ? "NAME2_2" : "NAME2_3", student._nenkuminamae);

            printPrincipalStaffname(svf);
            printInn(svf);
            printGuardName(svf);

            svf.VrEndPage();
            _hasdata = true;
        }
    }

    // 13 心臓検診結果のお知らせ
    private void print13(final DB2UDB db2, final Vrw32alp svf, final List studentList) {
        final String documentCd = "13";
        final String form = _param._isKoma ? "KNJF030H_13_2.frm" : "KNJF030H_13.frm";
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

            String heartMedexam = "";
            if (_param._isKoma) {
                svf.VrsOut("DATE", _param._date == null ? "" : KNJ_EditDate.getAutoFormatDate(db2, _param._date)); // 日付
                heartMedexam = Util.mkString(new String[] {_param.getNameMstName1("F080", mdd._heartMedexam), mdd._heartMedexamRemark}, "(") + ")";
                final String[] cutwkGengou = KNJ_EditDate.tate_format4(db2, _param._date.replace('/', '-'));
                if (cutwkGengou.length > 0) {
                    svf.VrsOut("ERA_NAME", cutwkGengou[0]);
                }
                svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME2_1" : student._nenkuminamaeLen <= 60 ? "NAME2_2" : "NAME2_3", student._nenkuminamae);
            } else {
                svf.VrsOut("DATE", _param._hiduke); // 日付
                heartMedexam = Util.mkString(new String[] {_param.getNameMstName1("F080", mdd._heartMedexam), mdd._heartMedexamRemark}, " ");
            }
            svf.VrsOut("SCHOOL_NAME", _param._certifSchoolDat125SchoolName); // 学校名
            final int heartStrLineLen = _param._isKoma ? 60 : 68;
            if (Util.getMS932ByteLength(heartMedexam) <= heartStrLineLen) {
                svf.VrsOut("HEART_MEDEXAM1", heartMedexam); // 心電図
            } else {
                final List tokenList = KNJ_EditKinsoku.getTokenList(heartMedexam, heartStrLineLen);
                for (int i = 0; i < tokenList.size(); i++) {
                    svf.VrsOut("HEART_MEDEXAM2_" + String.valueOf(i + 1), (String) tokenList.get(i)); // 心電図
                }
            }
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
        final String form = "KNJF030H_14.frm";
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
            svf.VrsOut("SCHOOL_NAME", _param._certifSchoolDat125SchoolName); // 学校名
            svf.VrsOut("SCHOOL_NAME2", _param._certifSchoolDat125SchoolName); // 学校名

            final String heartMedexam = Util.mkString(new String[] {_param.getNameMstName1("F080", mdd._heartMedexam), mdd._heartMedexamRemark}, " ");
            if (Util.getMS932ByteLength(heartMedexam) <= 68) {
                svf.VrsOut("HEART_MEDEXAM1", heartMedexam); // 心電図
            } else {
                final List tokenList = KNJ_EditKinsoku.getTokenList(heartMedexam, 68);
                for (int i = 0; i < tokenList.size(); i++) {
                    svf.VrsOut("HEART_MEDEXAM2_" + String.valueOf(i + 1), (String) tokenList.get(i)); // 心電図
                }
            }

            svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME1_1" : student._nenkuminamaeLen <= 60 ? "NAME1_2" : "NAME1_3", student._nenkuminamae);

            printPrincipalStaffname(svf);

            svf.VrEndPage();
            _hasdata = true;
        }
    }

    // 15 胸部エックス線検診結果のお知らせ（要経過観察）
    // 16 胸部エックス線検診結果のお知らせ（要精密検査）
    // 17 胸部エックス線検診結果のお知らせ（要精密検査_主治医）
    private void print15_16_17(final int n, final Vrw32alp svf, final List studentList) {
        final String documentCd;
        String form = null;
        if (n == 15) {
            form = "KNJF030H_15.frm";
            documentCd = "15";
        } else if (n == 16) {
            form = "KNJF030H_16.frm";
            documentCd = "16";
        } else if (n == 17 ) {
            form = "KNJF030H_17_1.frm";
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
            svf.VrsOut("SCHOOL_NAME", _param._certifSchoolDat125SchoolName); // 学校名
            svf.VrsOut("VIEWS1", Util.mkString(new String[] {_param.getNameMstName1("F100", mdd._tbRemarkcd), mdd._tbXRay}, " ")); // 間接撮影・所見
            svf.VrsOut("VIEWS2", Util.mkString(new String[] {_param.getNameMstName1("F100", mdd._tbOthertestcd),
                                                             _param.getNameMstName1("F120", mdd._tbNamecd),
                                                             _param.getNameMstName1("F130", mdd._tbOthertestRemark1)}, " ")); // 間接撮影・所見
            svf.VrsOut("CHARGE", _param._charge); // 担当者情報

            svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME1_1" : student._nenkuminamaeLen <= 60 ? "NAME1_2" : "NAME1_3", student._nenkuminamae);
            svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME2_1" : student._nenkuminamaeLen <= 60 ? "NAME2_2" : "NAME2_3", student._nenkuminamae);

            printPrincipalStaffname(svf);
            printAttention(svf);
            printInn(svf);
            printGuardName(svf);

            svf.VrEndPage();

            _hasdata = true;
        }
    }

    // 18 未検診のお知らせ（歯科）
    private void print18(final Vrw32alp svf, final List studentList) {
        final String documentCd = "18";
        final String form = "KNJF030H_18.frm";
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
            svf.VrsOut("SCHOOL_NAME", _param._certifSchoolDat125SchoolName); // 学校名

            svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME1_1" : student._nenkuminamaeLen <= 60 ? "NAME1_2" : "NAME1_3", student._nenkuminamae);
            svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME2_1" : student._nenkuminamaeLen <= 60 ? "NAME2_2" : "NAME2_3", student._nenkuminamae);

            printPrincipalStaffname(svf);
            printInn(svf);

            svf.VrEndPage();
            _hasdata = true;
        }
    }

    // 19 未検診のお知らせ（内科_眼科_耳鼻科）
    private void print19(final Vrw32alp svf, final List studentList, final String prtType) {
        final String documentCd = "19";
        final String form = "KNJF030H_19.frm";
        log.info(" form = " + form);

        for (int sti = 0; sti < studentList.size(); sti++) {
            final Student student = (Student) studentList.get(sti);
            final MedexamDetDat mdd = student._medexamDetDat;

            for (int i = 0; i < 3; i++) {
                String mijukenName = "";
                boolean isNotPrint = true;
                switch (i) {
                case 0:
                	if ("ALL".equals(prtType) || "1".equals(prtType)) {
                        isNotPrint = isNotPrint("19_1", mdd, null);
                        mijukenName = "内科検診";
                	}
                    break;
                case 1:
                	if ("ALL".equals(prtType) || "2".equals(prtType)) {
                        isNotPrint = isNotPrint("19_2", mdd, null);
                        mijukenName = "眼科検診";
                	}
                    break;
                case 2:
                	if ("ALL".equals(prtType) || "3".equals(prtType)) {
                        isNotPrint = isNotPrint("19_3", mdd, null);
                        mijukenName = "耳鼻科検診";
                	}
                    break;
                }
                if (isNotPrint) {
                    continue;
                }

                svf.VrSetForm(form, 1);

                final List documentMstTextTokenList = KNJ_EditKinsoku.getTokenList(_param.getDocumentMstText(documentCd), 80);
                printDocumentMstTitleText(svf, mijukenName + _param.getDocumentMstTitle(documentCd), documentMstTextTokenList);
                svf.VrsOut("TITLE2", mijukenName + "受診結果報告書");

                svf.VrsOut("DATE", _param._hiduke); // 日付
                svf.VrsOut("SCHOOL_NAME", _param._certifSchoolDat125SchoolName); // 学校名

                svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME1_1" : student._nenkuminamaeLen <= 60 ? "NAME1_2" : "NAME1_3", student._nenkuminamae);
                svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME2_1" : student._nenkuminamaeLen <= 60 ? "NAME2_2" : "NAME2_3", student._nenkuminamae);

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
            final String form = "KNJF030H_20_1.frm";

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
            final String form2 = "KNJF030H_20_2.frm";
            final String form3 = "KNJF030H_20_3.frm";
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
                    svf.VrsOut("SCHOOL_NAME", _param._certifSchoolDat125SchoolName); // 学校名
                    printPrincipalStaffname(svf);

                    svf.VrEndPage();
                    _hasdata = true;
                } else {
                    // ２次尿検査のお知らせ（蛋白・潜血用）
                    svf.VrSetForm(form3, 1);
                    final String snStr = student._hrName + " " + student._attendno + "番    名前  " + student._name;
                    final int snlen = KNJ_EditEdit.getMS932ByteLength(snStr);
                    final String snfield = snlen > 60 ? "3" : (snlen > 48 ? "2" : "1");
                    svf.VrsOut("NAME1_" + snfield, snStr);
                    final String prtDate = KNJ_EditDate.h_format_JP(db2, _param._date.replace('/', '-'));
                    svf.VrsOut("DATE", prtDate);

                    svf.VrsOut("ALBUMINURIA", _param.getNameMstName1("F020", mdd._albuminuria1cd)); // 尿・蛋白
                    svf.VrsOut("URICBLEED", _param.getNameMstName1("F018", mdd._uricbleed1cd)); // 尿・潜血
                    svf.VrsOut("URICSUGAR", _param.getNameMstName1("F019", mdd._uricsugar1cd)); // 尿・糖
                    final String otherAdvise2 = StringUtils.defaultString(mdd._uricothertest, "");
                    svf.VrsOut(Util.getMS932ByteLength(otherAdvise2) > 30 ? "URINE_OTHERS3" : (Util.getMS932ByteLength(otherAdvise2) > 26 ? "URINE_OTHERS2" : "URINE_OTHERS1"), otherAdvise2); // 腎臓疾患

                    svf.VrsOut("DUMMY", "1"); // ダミーデータ

                    svf.VrEndPage();
                    _hasdata = true;
                }
            }
        }

        if ("on".equals(_param._nyocheck3)) {
            // ２次尿検査結果のお知らせ
            final String form4 = "KNJF030H_20_4.frm";
            final String form51 = "KNJF030H_20_5_1.frm";
            final String form53 = "KNJF030H_20_5_3.frm";
            final String form6 = "KNJF030H_20_6.frm";

            final String documentCd = "20";  //駒澤用として利用している文書マスタ
            final List documentMstTextTokenList = KNJ_EditKinsoku.getTokenList(_param.getDocumentMstText(documentCd), 80);

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

                final String otherAdvise2 = StringUtils.defaultString(mdd._uricothertest, "");
                if (ijounasi) {
                    svf.VrSetForm(form4, 1);

                    svf.VrsOut("DATE", _param._hiduke); // 日付
                    svf.VrsOut("SCHOOL_NAME", _param._certifSchoolDat125SchoolName); // 学校名
                    printPrincipalStaffname(svf);

                    svf.VrEndPage();
                    _hasdata = true;
                } else {
                	if (_param._isKoma) {
                        // ２次尿検査結果のお知らせ（駒澤用）
                        svf.VrSetForm(form6, 1);
                        svf.VrsOut("DATE", _param._date == null ? "" : KNJ_EditDate.getAutoFormatDate(db2, _param._date)); // 日付
                        svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME1_1" : student._nenkuminamaeLen <= 60 ? "NAME1_2" : "NAME1_3", student._nenkuminamae);
                        svf.VrsOut("SCHOOL_NAME", _param._certifSchoolDat125SchoolName); // 学校名
                        printPrincipalStaffname(svf); // 職員名
                        printDocumentMstTitleText(svf, _param.getDocumentMstTitle(documentCd), documentMstTextTokenList);
                        svf.VrsOut("ALBUMINURIA1", "蛋白：" + StringUtils.defaultString(_param.getNameMstName1("F020", mdd._albuminuria1cd), "")); // 尿・蛋白
                        svf.VrsOut("URICSUGAR1", "糖　：" + StringUtils.defaultString(_param.getNameMstName1("F019", mdd._uricsugar1cd), "")); // 尿・糖
                        svf.VrsOut("URICBLEED1", "潜血：" + StringUtils.defaultString(_param.getNameMstName1("F018", mdd._uricbleed1cd), "")); // 尿・潜血
                        //svf.VrsOut("URINE_OTHERS1_" + (Util.getMS932ByteLength(otherAdvise2) > 30 ? "3" : (Util.getMS932ByteLength(otherAdvise2) > 20 ? "2" : "1")), otherAdvise2); // 腎臓疾患
                        svf.VrsOut("ALBUMINURIA2", "蛋白：" + StringUtils.defaultString(_param.getNameMstName1("F020", mdd._albuminuria2cd), "")); // 尿・蛋白
                        svf.VrsOut("URICSUGAR2", "糖　：" + StringUtils.defaultString(_param.getNameMstName1("F019", mdd._uricsugar2cd), "")); // 尿・糖
                        svf.VrsOut("URICBLEED2", "潜血：" + StringUtils.defaultString(_param.getNameMstName1("F018", mdd._uricbleed2cd), "")); // 尿・潜血
                        //svf.VrsOut("URINE_OTHERS2_" + (Util.getMS932ByteLength(otherAdvise2) > 30 ? "3" : (Util.getMS932ByteLength(otherAdvise2) > 20 ? "2" : "1")), otherAdvise2); // 腎臓疾患
                        svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME2_1" : student._nenkuminamaeLen <= 60 ? "NAME2_2" : "NAME2_3", student._nenkuminamae);
                        final String[] cutwkGengou = KNJ_EditDate.tate_format4(db2, _param._date.replace('/', '-'));
                        if (cutwkGengou.length > 0) {
                            svf.VrsOut("ERA_NAME", cutwkGengou[0]);
                        }
                	} else {
                        if (!ijounasiTanpaku || !ijounasiBleed) {
                            // ２次尿検査結果のお知らせ（蛋白・潜血用）
                            svf.VrSetForm(form51, 1);

                            svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME1_1" : student._nenkuminamaeLen <= 60 ? "NAME1_2" : "NAME1_3", student._nenkuminamae);
                            svf.VrsOut("DATE", _param._hiduke); // 日付
                            svf.VrsOut("SCHOOL_NAME", _param._certifSchoolDat125SchoolName); // 学校名
                            svf.VrsOut("ALBUMINURIA", _param.getNameMstName1("F020", mdd._albuminuria2cd)); // 尿・蛋白
                            svf.VrsOut("URICBLEED", _param.getNameMstName1("F018", mdd._uricbleed2cd)); // 尿・潜血
                            svf.VrsOut("URICSUGAR", _param.getNameMstName1("F019", mdd._uricsugar2cd)); // 尿・糖
                            svf.VrsOut(Util.getMS932ByteLength(otherAdvise2) > 30 ? "URINE_OTHERS3" : (Util.getMS932ByteLength(otherAdvise2) > 26 ? "URINE_OTHERS2" : "URINE_OTHERS1"), otherAdvise2); // 腎臓疾患
                            printPrincipalStaffname(svf);

                            svf.VrEndPage();
                            _hasdata = true;
                        }

                        // 精密検査の依頼
                        svf.VrSetForm(form53, 1);

                        svf.VrsOut(Util.getMS932ByteLength(student._name) <= 28 ? "NAME1_1" : "NAME1_2", student._name); // 氏名他

                        svf.VrsOut("ALBUMINURIA", _param.getNameMstName1("F020", mdd._albuminuria2cd)); // 尿・蛋白
                        svf.VrsOut("URICBLEED", _param.getNameMstName1("F018", mdd._uricbleed2cd)); // 尿・潜血
                        svf.VrsOut("URICSUGAR", _param.getNameMstName1("F019", mdd._uricsugar2cd)); // 尿・糖
                        svf.VrsOut(Util.getMS932ByteLength(otherAdvise2) > 30 ? "URINE_OTHERS3" : (Util.getMS932ByteLength(otherAdvise2) > 26 ? "URINE_OTHERS2" : "URINE_OTHERS1"), otherAdvise2); // 腎臓疾患

                        svf.VrsOut("HEIGHT", Util.sishagonyu(mdd._height)); // 身長
                        svf.VrsOut("WEIGHT", Util.sishagonyu(mdd._weight)); // 体重
                        svf.VrsOut("OBESITY", Util.calcHimando(student, db2, _param)); // 肥満度

                        svf.VrsOut("DATE", _param._hiduke); // 日付
                        svf.VrsOut("SCHOOL_NAME", _param._certifSchoolDat125SchoolName); // 学校名

                        svf.VrsOut(student._nenkuminamaeLen <= 48 ? "NAME2_1" : student._nenkuminamaeLen <= 60 ? "NAME2_2" : "NAME2_3", student._nenkuminamae);

                        printEraName(db2, svf); // 元号名
                        printInn(svf); // 印
                        printPrincipalStaffname(svf); // 職員名
                	}

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
        Map _medexamDetMonthDatMap = new HashMap();

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
                    student._nenkuminamae = StringUtils.defaultString(student._hrName) + " " + seki + "番　名前　" + StringUtils.defaultString(student._name);
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
                student._medexamDetMonthDatMap = MedexamDetMonthDat.getMdmdMap(db2, param._year, student._schregno);
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

    public static class MedexamDetMonthDat {
        final String _semester;
        final String _month;
        final String _rBarevision;
        final String _rBarevisionMark;
        final String _lBarevision;
        final String _lBarevisionMark;
        final String _rVision;
        final String _rVisionMark;
        final String _lVision;
        final String _lVisionMark;

        MedexamDetMonthDat(final String semester,
                           final String month,
                           final String rBarevision,
                           final String rBarevisionMark,
                           final String lBarevision,
                           final String lBarevisionMark,
                           final String rVision,
                           final String rVisionMark,
                           final String lVision,
                           final String lVisionMark
        ) {
            _semester = semester;
            _month = month;
            _rBarevision = rBarevision;
            _rBarevisionMark = rBarevisionMark;
            _lBarevision = lBarevision;
            _lBarevisionMark = lBarevisionMark;
            _rVision = rVision;
            _rVisionMark = rVisionMark;
            _lVision = lVision;
            _lVisionMark = lVisionMark;
        }

        static Map getMdmdMap(final DB2UDB db2, final String year, final String schregno) throws SQLException {
            final Map rtnMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String mdmdSql = getMdmdSql(year, schregno);
            log.debug(" mdmd sql = " + mdmdSql);
            try {
                ps = db2.prepareStatement(mdmdSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String month = rs.getString("MONTH");
                    final String rBarevision = rs.getString("R_BAREVISION");
                    final String rBarevisionMark = rs.getString("R_BAREVISION_MARK");
                    final String lBarevision = rs.getString("L_BAREVISION");
                    final String lBarevisionMark = rs.getString("L_BAREVISION_MARK");
                    final String rVision = rs.getString("R_VISION");
                    final String rVisionMark = rs.getString("R_VISION_MARK");
                    final String lVision = rs.getString("L_VISION");
                    final String lVisionMark = rs.getString("L_VISION_MARK");

                    final MedexamDetMonthDat mdmd = new MedexamDetMonthDat(semester, month, rBarevision, rBarevisionMark, lBarevision, lBarevisionMark, rVision, rVisionMark, lVision, lVisionMark);
                    rtnMap.put(semester + month, mdmd);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtnMap;
        }

        private static String getMdmdSql(final String year, final String schregno) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.MONTH, ");
            stb.append("     T1.R_BAREVISION, ");
            stb.append("     T1.R_BAREVISION_MARK, ");
            stb.append("     T1.L_BAREVISION, ");
            stb.append("     T1.L_BAREVISION_MARK, ");
            stb.append("     T1.R_VISION, ");
            stb.append("     T1.R_VISION_MARK, ");
            stb.append("     T1.L_VISION, ");
            stb.append("     T1.L_VISION_MARK ");
            stb.append(" FROM ");
            stb.append("     MEDEXAM_DET_MONTH_DAT T1");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + year + "' ");
            stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
            // 4月は別テーブルにあるので、そちらから取得。
            //stb.append("     AND T1.MONTH IN ('04','10') ");
            stb.append("     AND T1.MONTH IN ('10') ");
            stb.append(" UNION ");
            stb.append(" SELECT ");
            stb.append("     '1' AS SEMESTER, ");
            stb.append("     '04' AS MONTH, ");
            stb.append("     T2.R_BAREVISION, ");
            stb.append("     T2.R_BAREVISION_MARK, ");
            stb.append("     T2.L_BAREVISION, ");
            stb.append("     T2.L_BAREVISION_MARK, ");
            stb.append("     T2.R_VISION, ");
            stb.append("     T2.R_VISION_MARK, ");
            stb.append("     T2.L_VISION, ");
            stb.append("     T2.L_VISION_MARK ");
            stb.append(" FROM ");
            stb.append("     MEDEXAM_DET_DAT T2 ");
            stb.append(" WHERE ");
            stb.append("     T2.YEAR = '" + year + "' ");
            stb.append("     AND T2.SCHREGNO = '" + schregno + "' ");
            stb.append(" ORDER BY ");
            stb.append("     SEMESTER, ");
            stb.append("     MONTH ");
            return stb.toString();
        }
    }

    /**
     * <<クラスの説明>>。
     * @author m-yama
     * @version $Id: 9e81f6d3989ed46d64f05ea796138f34c12bb2ac $
     */
    private static class KNJF030A {

        protected Param _param;
        protected DB2UDB _db2;

        private String FORM_KNJF030A_1_5 = "KNJF030A_1_5.frm";
        private String FORM_KNJF030A_1P_5 = "KNJF030A_1P_5.frm";

        private int hrnameKeta = 0;
        private int hrnameC2_1Keta = 0;

        private boolean _hasData;

        /**
         * {@inheritDoc}
         */
        protected boolean printMain(final Vrw32alp svf, final DB2UDB db2, final Param param, final List students) throws SQLException {

            _param = param;
            _db2 = db2;

            _hasData = false;
            for (final Iterator itStudent = students.iterator(); itStudent.hasNext();) {
                final Student student = (Student) itStudent.next();
                log.debug(" schregno = " + student._schregno);
                int dataCnt = 0;
                    dataCnt = printOut(svf, _db2, student, _param._SCHOOLKIND, dataCnt);
            }
            return _hasData;
        }

        private int printOut(final Vrw32alp svf, final DB2UDB db2, final Student student, final String schoolKind, final int prevCount) throws SQLException {
            final List printData = RegdDat.getRegdList(db2, _param, student);
            if (printData.size() == 0) {
                return 0;
            }

			final String form = _param._isSpecialSupport ? "KNJF030H_1_2.frm"
					: _param._isFukuiken ? "KNJF030H_1_4.frm"
							: _param._isKoma ? "KNJF030H_1_5.frm"
									: (_param._isRitsumeikan && "P".equals(student._schoolKind)) ? "KNJF030H_1_6.frm"
											: "KNJF030H_1.frm";

            log.fatal(" form = " + form);
            svf.VrSetForm(form, 4);

            try {
                final Map fieldnameMap = _param.getFieldInfoMap(svf, form);
                hrnameKeta = Param.getFieldKeta(fieldnameMap, "HR_NAME");
                hrnameC2_1Keta = Param.getFieldKeta(fieldnameMap, "HR_NAMEC2_1");
            } catch (Exception e) {
                log.warn("exception!", e);
            }
            if ("1".equals(_param._defineSchool.schooldiv)) {
                svf.VrsOut("GRADENAME_TITLE", "年度");
            } else {
                svf.VrsOut("GRADENAME_TITLE", "学年");
            }
            svf.VrsOut("NAME_HEADER", "名前"); // 名前ヘッダ

            //特別支援の時、2枚目を出力する用
            final List remarkList = new ArrayList();

            int dataCnt = ("J".equals(schoolKind) ? prevCount : 0) + 1;
            for (final Iterator itPrint = printData.iterator(); itPrint.hasNext();) {
                final RegdDat regdDat = (RegdDat) itPrint.next();

                svf.VrsOut("SCHREGNO", regdDat._schregNo);   //改ページ用
                svf.VrsOut("NAME_SHOW" + (Util.getMS932ByteLength(student._name) > 24 ? "_2" : ""), student._name);
                svf.VrsOut("SEX", student._sex);
                svf.VrsOut("BIRTHDAY", _param._isKoma ? KNJ_EditDate.getAutoFormatDate(db2, student._birthDay) : KNJ_EditDate.h_format_JP(db2, student._birthDay));
                svf.VrsOut("SCHOOL_NAME", _param.getSchoolInfo(schoolKind, SCHOOL_NAME2));

                final String title = "KNJF030H_1_6.frm".equals(form) ? "児童生徒健康診断票（一般）" :"生徒学生健康診断票（一般）";
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
                svf.VrsOutn(null != regdDat._hrClassName && regdDat._hrClassName.length() > 4 ? "HR_NAME2_1" : "HR_NAME1", dataCnt, regdDat._hrClassName);
                svf.VrsOutn("ATTENDNO", dataCnt, regdDat._attendNo);
                if (hrnameKeta > 0 && Util.getMS932ByteLength(regdDat._hrName) > hrnameKeta && hrnameC2_1Keta > hrnameKeta) {
                    svf.VrsOutn("HR_NAMEC2_1", dataCnt, regdDat._hrName);
                } else {
                    svf.VrsOutn("HR_NAME", dataCnt, regdDat._hrName);
                }
                svf.VrsOutn("YEAR", dataCnt, KNJ_EditDate.gengou(db2, Integer.parseInt(regdDat._year)) + "年度");
                svf.VrsOut("M_DATE", KNJ_EditDate.gengou(db2, Integer.parseInt(regdDat._year)) + "年度");
                svf.VrsOut("AGE", regdDat._age);        //４月１日現在の年齢

                if (null == regdDat._hasData) {
                    final RemarkData setRemarkNull = (RemarkData) new RemarkData(regdDat._age, null, null);
                    remarkList.add(setRemarkNull);
                }

                if (null != regdDat._hasData) {

                    final MedexamDetDat mdd = regdDat._medexamDetDat;

                    // 身長
                    svf.VrsOut("HEIGHT", mdd._height);

                    // 体重
                    svf.VrsOut("WEIGHT", mdd._weight);

                    // 栄養状態
                    if (student._medexamDetDat._height != null) {
//                        final BigDecimal height = new BigDecimal(student._medexamDetDat._height);
                        final String kihonDate = _param._year + "-04-01";
                        final int iNenrei = (int) _param.getNenrei(student, kihonDate, _param._year, _param._year);
                        setNameHiman(svf, "NUTRITION", "F030", mdd, student._sexCd, iNenrei);
                    }

                    // 脊柱・胸郭・四肢
                    setName1WithSlash(svf, "SPINERIB", "F040", mdd._spineribcd, "SLASH_SPINERIB");
                    svf.VrsOut("SPINERIBCD_REMARK", mdd._spineribcdRemark);

                    // 視力
                    final String cantMeasure = "測定困難";
                    String rBareVision = "";
                    String lBareVision = "";
                    String rVision = "";
                    String lVision = "";
                    // 数字、文字、両方データ有は文字を優先
                    // 裸眼（右）セット
                    if (null != mdd._rBarevisionMark && null != mdd._rBarevision) {
                        rBareVision = mdd._rBarevisionMark;
                    } else if (null != mdd._rBarevisionMark) {
                        rBareVision = mdd._rBarevisionMark;
                    } else if (null != mdd._rBarevision) {
                        rBareVision = mdd._rBarevision;
                    }
                    // 裸眼（左）セット
                    if (null != mdd._lBarevisionMark && null != mdd._lBarevision) {
                        lBareVision = mdd._lBarevisionMark;
                    } else if (null != mdd._lBarevisionMark) {
                        lBareVision = mdd._lBarevisionMark;
                    } else if (null != mdd._lBarevision) {
                        lBareVision = mdd._lBarevision;
                    }
                    // 矯正（右）セット
                    if (null != mdd._rVisionMark && null != mdd._rVision) {
                        rVision = mdd._rVisionMark;
                    } else if (null != mdd._rVisionMark) {
                        rVision = mdd._rVisionMark;
                    } else if (null != mdd._rVision) {
                        rVision = mdd._rVision;
                    }
                    // 矯正（左）セット
                    if (null != mdd._lVisionMark && null != mdd._lVision) {
                        lVision = mdd._lVisionMark;
                    } else if (null != mdd._lVisionMark) {
                        lVision = mdd._lVisionMark;
                    } else if (null != mdd._lVision) {
                        lVision = mdd._lVision;
                    }
                    // 視力（右）
                    if ("1".equals(mdd._rVisionCantMeasure)) {
                        svf.VrsOut("R_BAREVISION_2", cantMeasure); // 測定困難
                    } else {
                        svf.VrsOut("R_BAREVISION", rBareVision);
                        svf.VrsOut("R_VISION",     rVision);
                    }
                    // 視力（左）
                    if ("1".equals(mdd._lVisionCantMeasure)) {
                        svf.VrsOut("L_BAREVISION_2", cantMeasure); // 測定困難
                    } else {
                        svf.VrsOut("L_BAREVISION", lBareVision);
                        svf.VrsOut("L_VISION",     lVision);
                    }

                    // 目の疾病及び異常
                    setName1WithSlash(svf, "EYEDISEASE", "F050", mdd._eyediseasecd, "SLASH_EYEDISEASE");
                    svf.VrsOut("EYE_TEST_RESULT", mdd._eyeTestResult);

                    // 聴力
                    if ("1".equals(mdd._rEarCantMeasure)) {
                        svf.VrsOut("R_EAR", cantMeasure); // 測定困難
                    } else if (_param._isKoma) {
                    	if (!"".equals(StringUtils.defaultString(mdd._rEarDb1000, ""))) {
                    		final String gStr = getName("F010", mdd._rEarDb1000, NAME1, "");
                            svf.VrsOut("R_EAR_1", !"".equals(gStr) ? gStr : "  ");
                    	} else {
                            svf.VrsOut("R_EAR_1", StringUtils.defaultString(mdd._rEarDb1000, "  "));
                    	}
                    	if (!"".equals(StringUtils.defaultString(mdd._rEarDb4000, ""))) {
                    		final String gStr = getName("F010", mdd._rEarDb4000, NAME1, "");
                            svf.VrsOut("R_EAR_2", !"".equals(gStr) ? gStr : "  ");
                    	} else {
                            svf.VrsOut("R_EAR_2", StringUtils.defaultString(mdd._rEarDb4000, "  "));
                    	}
                    } else if (!_param._isFukuiken && "02".equals(mdd._rEar)) {
                        svf.VrsOut("R_EAR_DB", "○" + StringUtils.defaultString(mdd._rEarDb, "  ") + "db");
                        svf.VrsOut("R_EAR", "（" + StringUtils.defaultString(mdd._rEarDb4000, "  ") + "db）");
                    } else if ("01".equals(mdd._rEar)) {
                        printSlash(svf, "SLASH_R_EAR");
                    } else {
                        svf.VrsOut("R_EAR_DB", mdd._rEarDb);
                        svf.VrsOut("R_EAR", _param.getNameMstName1("F010", mdd._rEar));
                    }
                    if ("1".equals(mdd._lEarCantMeasure)) {
                        svf.VrsOut("L_EAR", cantMeasure); // 測定困難
                    } else if (_param._isKoma) {
                    	if (!"".equals(StringUtils.defaultString(mdd._rEarDb1000, ""))) {
                    		final String gStr = getName("F010", mdd._lEarDb1000, NAME1, "");
                            svf.VrsOut("L_EAR_1", !"".equals(gStr) ? gStr : "  ");
                    	} else {
                            svf.VrsOut("L_EAR_1", StringUtils.defaultString(mdd._lEarDb1000, "  "));
                    	}
                    	if (!"".equals(StringUtils.defaultString(mdd._rEarDb1000, ""))) {
                    		final String gStr = getName("F010", mdd._lEarDb4000, NAME1, "");
                            svf.VrsOut("L_EAR_2", !"".equals(gStr) ? gStr : "  ");
                    	} else {
                            svf.VrsOut("L_EAR_2", StringUtils.defaultString(mdd._lEarDb4000, "  "));
                    	}
                    } else if (!_param._isFukuiken && "02".equals(mdd._lEar)) {
                        svf.VrsOut("L_EAR_DB", "○" + StringUtils.defaultString(mdd._lEarDb, "  ") + "db");
                        svf.VrsOut("L_EAR", "（" + StringUtils.defaultString(mdd._lEarDb4000, "  ") + "db）");
                    } else if ("01".equals(mdd._lEar)) {
                        printSlash(svf, "SLASH_L_EAR");
                    } else {
                        svf.VrsOut("L_EAR_DB", mdd._lEarDb);
                        svf.VrsOut("L_EAR", _param.getNameMstName1("F010",mdd._lEar));
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
//                        svf.VrsOut("SKINDISEASE", mdd._skindiseasecdRemark);
//                        setName1WithSlash(svf, "SKINDISEASE", "F070", mdd._skindiseasecd, "SLASH_SKINDISEASE");

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
                    // その他の検査
                    setName1WithSlash(svf, "OTHERS", "F110", mdd._tbOthertestcd, "SLASH_OTHERS");
                    // 病名
                    setName1WithSlash(svf, "DISEASE_NAME", "F120", mdd._tbNamecd, "SLASH_DISEASE_NAME");
                    // 指導区分
                    setName1WithSlash(svf, "GUIDANCE", "F130", mdd._tbAdvisecd, null);


                    if (_param._isKoma) {
                    	// 心電図No
                    	svf.VrsOut("HEART_FILMNO", mdd._heartGraphNo);
                    }
                    // 臨床医学検査(心電図)
                    setHeartMedExam(svf, mdd._heartMedexamRemark, mdd._heartMedexam, form);
                    // 疾病及び異常
                    setHeartdiseasecd(svf, mdd._heartdiseasecdRemark, mdd._heartdiseasecd, form);

                    // 尿
                    setName1WithSlash(svf, "ALBUMINURIA",  "F020", mdd._albuminuria1cd, null);
                    setName1WithSlash(svf, "URICSUGAR",    "F019", mdd._uricsugar1cd, null);
                    setName1WithSlash(svf, "URICBLEED",    "F018", mdd._uricbleed1cd, null);
                    setName1WithSlash(svf, "ALBUMINURIA2", "F020", mdd._albuminuria2cd, null);
                    setName1WithSlash(svf, "URICSUGAR2",   "F019", mdd._uricsugar2cd, null);
                    setName1WithSlash(svf, "URICBLEED2",   "F018", mdd._uricbleed2cd, null);
                    svf.VrsOut("URINE_OTHERS" + (Util.getMS932ByteLength(mdd._uricothertest) > 20 ? "1" : ""), mdd._uricothertest);

                    // その他の疾病及び異常
                    setName1WithSlash(svf, "OTHERDISEASE", "F140", mdd._otherdiseasecd, "SLASH_OTHERDISEASE");
                    if ("1".equals(printKenkouSindanIppan)) {
                        setName1WithSlash(svf, "OTHER_ADVISE", "F145", mdd._otherAdvisecd, "SLASH_OTHERDISEASE");
                    }
                    svf.VrsOut("OTHER_ADVISECD2", mdd._otherRemark2);
                    // 学校医
                    String views = StringUtils.defaultString(mdd._docRemark, "");
                	final String[] split = Util.splitByLength(views, 10);
                    List dentistremarkField = new ArrayList();
                	if (_param._isFukuiken) {
                		if (null != mdd._date) {
                			final String certifSchoolDat124Remark5 = getCertif124(db2,regdDat._year);
                		    svf.VrsOut("VIEWS2_3", certifSchoolDat124Remark5);
                		}
                		dentistremarkField.add("VIEWS2_1");
                		dentistremarkField.add("VIEWS2_2");
                	} else {
                		dentistremarkField.add("VIEWS2_1");
                		dentistremarkField.add("VIEWS2_2");
                		dentistremarkField.add("VIEWS2_3");
                	}
                    for (int di = 0; di < Math.min(split.length, dentistremarkField.size()); di++) {
                        svf.VrsOut((String)dentistremarkField.get(di), split[di]); // 事後措置
                    }
                    // 月日
                    svf.VrsOut("DOC_DATE", KNJ_EditDate.h_format_JP(db2, mdd._docDate));

                    // 事後措置
                    setName1WithSlash(svf, "DOC_TREAT1", "F150", mdd._treatcd, "SLASH_DOC_TREAT");
                    setName1WithSlash(svf, "DOC_TREAT2", "F151", mdd._treatcd2, "SLASH_DOC_TREAT");

                    // 備考
                    svf.VrsOut("NOTE1", mdd._remark);

                    // その他の疾病及び異常 + 備考セット
                    final RemarkData setRemark = (RemarkData) new RemarkData(regdDat._age, mdd._otherRemark2, mdd._remark);
                    remarkList.add(setRemark);
                }
                dataCnt++;

                svf.VrEndRecord();
                _hasData = true;
            }

            // 生徒の2枚目を出力する。(特別支援専用フォーム)
            if (_param._isSpecialSupport) {
                setRemark(svf, remarkList);
                remarkList.clear();
            }

            return dataCnt;
        }

        /** 健康診断（一般）2枚目 */
        private void setRemark(final Vrw32alp svf, final List remarkList) {
            final String form = "KNJF030H_1_3.frm";
            log.fatal(" form = " + form);
            svf.VrSetForm(form, 1);

            int setRow = 1;

            // その他の疾病及び異常
            for (Iterator it = remarkList.iterator(); it.hasNext();) {
                final RemarkData remarkDate = (RemarkData) it.next();

                // 年齢
                svf.VrsOutn("AGE", setRow, remarkDate._age);

                // その他の疾病及び異常
                final String reIdx = 100 < KNJ_EditEdit.getMS932ByteLength(remarkDate._otherRemark) ? "2_1": "1";
                svf.VrsOutn("REMARK" + reIdx, setRow, remarkDate._otherRemark);

                setRow++;
            }

            for (int i = setRow; i <= 5; i++) {
                setRow++;
            }

            // 備考
            for (Iterator it = remarkList.iterator(); it.hasNext();) {
                final RemarkData remarkDate = (RemarkData) it.next();

                // 年齢
                svf.VrsOutn("AGE", setRow, remarkDate._age);

                // その他の疾病及び異常
                final String reIdx = 100 < KNJ_EditEdit.getMS932ByteLength(remarkDate._remark) ? "2_1": "1";
                svf.VrsOutn("REMARK" + reIdx, setRow, remarkDate._remark);

                setRow++;
            }

            svf.VrEndPage();
        }

        private static class RemarkData {
            final String _age;
            final String _otherRemark;
            final String _remark;

            RemarkData(
                final String age,
                final String otherRemark,
                final String remark
            ) {
                _age            = age;
                _otherRemark    = otherRemark;
                _remark         = remark;
            }
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

            final String setName = getName(nameCd1, nameCd2, NAME1, "");

            // 脊柱・胸郭・四肢
            if ("SPINERIB".equals(fieldName) && 20 < KNJ_EditEdit.getMS932ByteLength(setName)) {
                svf.VrsOut(fieldName + "2", setName);
            } else {
                svf.VrsOut(fieldName, setName);
            }
        }

        private void setNameHiman(
                final Vrw32alp svf,
                final String fieldName,
                final String nameCd1,
                final MedexamDetDat mdd,
                final String sex,
                final int iNenrei

        ) throws SQLException {
        	final Map getPsyMap = (Map)_param._phys_avg.get(sex + "-" + iNenrei);
        	 if (_param._isFukuiken) {  //福井県のみ
          	    if ("1".equals(_param.getNameMst(nameCd1, mdd._nutritioncd, "NAMESPARE2"))) {
          	    	//出力しない。
          	    } else {
             	    if ("1".equals(_param.getNameMst(nameCd1, mdd._nutritioncd, "NAMESPARE1"))) {
                     	//栄養状態のコンボボックスで選択した値について、名称Mの予備1が"1"だった場合、斜線を出力する。
                         printSlash(svf, "SLASH_" + fieldName);
             	    } else {
             	    	final String putStr = mdd._nutritioncdRemark;
             	    	if (20 < KNJ_EditEdit.getMS932ByteLength(putStr)) {
                 	    	svf.VrsOut(fieldName+"2_1", StringUtils.defaultString(putStr, ""));
             	    	} else {
             	    	    svf.VrsOut(fieldName, putStr);
             	    	}
             	    }
          	    }
         	} else if (getPsyMap == null || getPsyMap.size() == 0) {
            	//肥満度を取得できなかった場合、BMI値を出力
            	final BigDecimal normWeight = new BigDecimal( ((("1".equals(sex) ? 0.733 : 0.56) * Double.parseDouble(mdd._height)) - ("1".equals(sex) ? 70.989 : 37.002)) );
            	final BigDecimal himanLev = (new BigDecimal(mdd._weight)).subtract(normWeight).divide(normWeight, 1, java.math.BigDecimal.ROUND_DOWN);
            	final String himanStr = himanLev.toString();
            	String setHimanStr = "";
            	if (himanLev.doubleValue() < -0.3) {
                	setHimanStr = "高度痩せ";
            	} else if ("-0.2".equals(himanStr)) {
                	setHimanStr = "やせ";
            	} else if ("-0.1".equals(himanStr) || "0.0".equals(himanStr) || "0".equals(himanStr) || "0.1".equals(himanStr)) {
                	setHimanStr = "普通";
            	} else if ("0.2".equals(himanStr)) {
                	setHimanStr = "軽度肥満";
            	} else if ("0.3".equals(himanStr) || "0.4".equals(himanStr)) {
                	setHimanStr = "中等度肥満";
            	} else if (himanLev.doubleValue() > 0.4) {
                	setHimanStr = "高度肥満";
            	}
                svf.VrsOut(fieldName, setHimanStr);
            } else {
                if ("1".equals(_param._kenkouSindanIppanNotPrintNameMstComboNamespare2Is1) && "1".equals(_param.getNameMst(nameCd1, mdd._nutritioncd, "NAMESPARE2"))) {
                    return;
                }

                final String std_Weight_Keisu_str_a = (String)getPsyMap.get("STD_WEIGHT_KEISU_A");
                final String std_Weight_Keisu_str_b = (String)getPsyMap.get("STD_WEIGHT_KEISU_B");
                final BigDecimal std_Weight_Keisu_a = new BigDecimal(std_Weight_Keisu_str_a);
                final BigDecimal std_Weight_Keisu_b = new BigDecimal(std_Weight_Keisu_str_b);

                final String setName = (!"".equals(mdd._height)) ? (std_Weight_Keisu_a.multiply(new BigDecimal(mdd._height)).subtract(std_Weight_Keisu_b).setScale(1, BigDecimal.ROUND_HALF_DOWN).toString()) + "%" : "";

                // 脊柱・胸郭・四肢
                if ("SPINERIB".equals(fieldName) && 20 < KNJ_EditEdit.getMS932ByteLength(setName)) {
                    svf.VrsOut(fieldName + "2", setName);
                } else {
                    svf.VrsOut(fieldName, setName);
                }
            }
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

    private static class Param {
        final String _year;
        final String _gakki;
        final String _kubun;
        final String[] _classSelected;
        final String _date;
        final List _checkList = new ArrayList();
        final String _nyocheck1;
        final String _nyocheck2;
        final String _nyocheck3;
        final String _hyoji1; // 校長名を表示
        final String _hyoji2; // プール指導を表示
        final String _hyoji3; // 医師「印」を表示
        final String _hyoji4; // 文言「保護者名」を表示

        final String _namemstZ010Name1;
        final boolean _isSagaken;
        final boolean _isFukuiken;
        final boolean _isKoma;
        final boolean _isRitsumeikan;
        private KNJ_Schoolinfo.ReturnVal _returnval2;
        private String _certifSchoolDat124Remark5;
        private String _certifSchoolDat124Remark10;
        private String _certifSchoolDat125SchoolName;
        private String _certifSchoolDat125JobName;
        private String _certifSchoolDat125PrincipalName;
        private String _certifSchoolDat125Remark1;
        private String _certifSchoolDat125Remark2;
        private String _certifSchoolDat125Remark3;
        private String _certifSchoolDat125Remark4;
        private String _certifSchoolDat125Remark5;
        private String _certifSchoolDat125Remark6;
        private String _certifSchoolDat125Remark7;
        private String _staffname;
        private String _staffname2;
        private int _staffnameKeta;
        private String _hiduke;
        private String _charge; // 担当情報
        private final boolean _isSpecialSupport; // 特別支援学校
        final String _SCHOOLKIND;
        final Map _formFieldInfoMapMap = new HashMap();

        KNJDefineSchool _defineSchool;

        /** 写真データ格納フォルダ */
        private final String _imageDir;
        /** 陰影保管場所(陰影出力に関係する) */
        private final String _documentRoot;

        private final Map _nameMstFXX;
        private final Map _documentMstMap;
        private Map _physAvgMap = null;

        final String _useKnjf030AHeartBiko;
//        final String _useForm5_H_Ippan;
        /** 名称マスタのコンボで名称予備2=1は表示しない */
        final String _kenkouSindanIppanNotPrintNameMstComboNamespare2Is1;
        final String _slashJpgFilePath;

        final boolean _isOutputDebug;

        final String _n19Sort;
        final String _n19Check1;
        final String _n19Check2;
        final String _n19Check3;

        final Map _phys_avg;
        final Map _notPrintSubTitleSchoolkindMap;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            //  パラメータの取得
            _year = request.getParameter("YEAR");                        //年度
            _gakki = request.getParameter("GAKKI");                       //学期 1,2,3
            _kubun = request.getParameter("KUBUN");                       //1:クラス,2:個人
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

            _n19Sort = request.getParameter("N19SORT");
            _n19Check1 = request.getParameter("N19CHECK1");
            _n19Check2 = request.getParameter("N19CHECK2");
            _n19Check3 = request.getParameter("N19CHECK3");

            _useKnjf030AHeartBiko = request.getParameter("useKnjf030AHeartBiko");
            _kenkouSindanIppanNotPrintNameMstComboNamespare2Is1 = request.getParameter("kenkouSindanIppanNotPrintNameMstComboNamespare2Is1");

            if (null != _date) {
                _hiduke = KNJ_EditDate.h_format_JP(db2, _date);
            }

            _defineSchool = new KNJDefineSchool();
            _defineSchool.defineCode(db2, _year);

            //  学校名・学校住所・校長名の取得
            _returnval2 = new KNJ_Schoolinfo(_year).get_info(db2);

            _isSpecialSupport = "1".equals(request.getParameter("useSpecial_Support_School"));
            _SCHOOLKIND = request.getParameter("SCHOOLKIND");
            _namemstZ010Name1 = getNameMstZ010(db2);
            log.info(" _namemstZ010Name1 = " + _namemstZ010Name1);
            _isSagaken = "sagaken".equals(_namemstZ010Name1);
            _isFukuiken = "fukuiken".equals(_namemstZ010Name1);
            _isKoma = "koma".equals(_namemstZ010Name1);
            _isRitsumeikan = "Ritsumeikan".equals(_namemstZ010Name1);

            _documentRoot = request.getParameter("DOCUMENTROOT"); // 陰影保管場所 NO001
            _imageDir = "image";
            _slashJpgFilePath = getImageFile("slash.jpg");
            setCertifSchoolDat(db2);

            _nameMstFXX = getNameMstFXX(db2);
            _documentMstMap = getDocumentMstMap(db2);
            _phys_avg = getHexamPhysicalAvgDat(db2);
            _notPrintSubTitleSchoolkindMap = getNotPrintSubTitleSchoolkindMap(db2, request);

            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));
        }

        private Map getNotPrintSubTitleSchoolkindMap(final DB2UDB db2, final HttpServletRequest request) {
            Map retMap = new TreeMap();
            final String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'A023' ";
            final List rowList = KnjDbUtils.query(db2, sql);
            for (final Iterator it = rowList.iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                final String schoolkind = getString(row, "NAME1");
                final String notPrintSubTitle = request.getParameter("KNJF030H_notPrint_SubTitle_" + schoolkind);
                retMap.put(schoolkind, notPrintSubTitle);
            }
            return retMap;
        }

        private Map getHexamPhysicalAvgDat(final DB2UDB db2) {
            Map retMap = new TreeMap();
        	final String sql = "SELECT * FROM HEXAM_PHYSICAL_AVG_DAT WHERE YEAR <= '"+_year+"' ";
            final List rowList = KnjDbUtils.query(db2, sql);
            final String[] kStrList = new String[] {"SEX", "NENREI_YEAR"};
            String kStr = "";
            String sep = "";
            for (final Iterator it = rowList.iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                kStr = "";
                sep = "";
                for (int cnt = 0; cnt < kStrList.length;cnt++) {
                	kStr += sep + row.get(kStrList[cnt]);
                	sep = "-";
                }
                retMap.put(kStr, row);
            }

            return retMap;
            // return getColumnGroupByMap(, );
        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJF030H' AND NAME = '" + propName + "' "));
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
            return getString(KnjDbUtils.firstRow(Util.getMappedList(Util.getMappedMap(_nameMstFXX, namecd1), namecd2)), field);
        }

        private String getSchoolInfo(final String schoolKind, final String field) {
            final Map map = new HashMap();
            if ("H".equals(schoolKind)) {
                map.put(SCHOOL_NAME1, _certifSchoolDat125SchoolName); //学校名１
                map.put(SCHOOL_NAME2, _certifSchoolDat125SchoolName); //学校名２
                map.put(PRINCIPAL_NAME, _certifSchoolDat125PrincipalName); //校長名
                map.put(PRINCIPAL_JOBNAME, _certifSchoolDat125JobName);
            } else if ("J".equals(schoolKind)) {
                map.put(SCHOOL_NAME1, _certifSchoolDat125Remark1); //学校名１
                map.put(SCHOOL_NAME2, _certifSchoolDat125Remark1); //学校名２
                map.put(PRINCIPAL_NAME, _certifSchoolDat125Remark2); //校長名
                map.put(PRINCIPAL_JOBNAME, _certifSchoolDat125Remark3);
            } else if ("P".equals(schoolKind) || "K".equals(schoolKind)) {
                map.put(SCHOOL_NAME1, _certifSchoolDat125Remark4); //学校名１
                map.put(SCHOOL_NAME2, _certifSchoolDat125Remark4); //学校名２
                map.put(PRINCIPAL_NAME, _certifSchoolDat125Remark5); //校長名
                map.put(PRINCIPAL_JOBNAME, _certifSchoolDat125Remark6);
            }
            if (null == map.get(SCHOOL_NAME1)) map.put(SCHOOL_NAME1, _returnval2.SCHOOL_NAME1); //学校名１
            if (null == map.get(SCHOOL_NAME2)) map.put(SCHOOL_NAME2, _returnval2.SCHOOL_NAME2); //学校名２
            if (null == map.get(PRINCIPAL_NAME)) map.put(PRINCIPAL_NAME, _returnval2.PRINCIPAL_NAME); //校長名
            if (null == map.get(PRINCIPAL_JOBNAME)) map.put(PRINCIPAL_JOBNAME, _returnval2.PRINCIPAL_JOBNAME);
            return (String) map.get(field);
        }

        private Map getNameMstFXX(final DB2UDB db2) {
            final String sql = "SELECT NAMECD1, NAMECD2, NAME1, NAME2, NAME3, NAMESPARE1, NAMESPARE2, NAMESPARE3 ,ABBV1 FROM NAME_MST WHERE NAMECD1 LIKE 'F%' ";
            return getColumnGroupByMap(new String[] {"NAMECD1", "NAMECD2"}, KnjDbUtils.query(db2, sql));
        }

        private String getNameMstZ010(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' AND NAME1 IS NOT NULL "));
        }

        private void setCertifSchoolDat(final DB2UDB db2) {

            final StringBuffer stb124 = new StringBuffer();
            stb124.append(" SELECT ");
            stb124.append("     VALUE(T2.STAFFNAME, T1.REMARK5) AS REMARK5 ");
            stb124.append("    ,VALUE(T3.STAFFNAME, T1.REMARK10) AS REMARK10 ");
            stb124.append(" FROM CERTIF_SCHOOL_DAT T1 ");
            stb124.append(" LEFT JOIN STAFF_MST T2 ON T2.STAFFCD = T1.REMARK5 ");
            stb124.append(" LEFT JOIN STAFF_MST T3 ON T3.STAFFCD = T1.REMARK10 ");
            stb124.append(" WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '124' ");

            final Map row124 = KnjDbUtils.firstRow(KnjDbUtils.query(db2, stb124.toString()));
            _certifSchoolDat124Remark5 = getString(row124, "REMARK5");
            _certifSchoolDat124Remark10 = getString(row124, "REMARK10");

            final StringBuffer stb125 = new StringBuffer();
            stb125.append(" SELECT * ");
            stb125.append(" FROM CERTIF_SCHOOL_DAT T1 ");
            stb125.append(" WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '125' ");

            final Map row125 = KnjDbUtils.firstRow(KnjDbUtils.query(db2, stb125.toString()));

            _certifSchoolDat125SchoolName = getString(row125, "SCHOOL_NAME");
            _certifSchoolDat125JobName = getString(row125, "JOB_NAME");
            _certifSchoolDat125PrincipalName = getString(row125, "PRINCIPAL_NAME");
            _certifSchoolDat125Remark1 = getString(row125, "REMARK1");
            _certifSchoolDat125Remark2 = getString(row125, "REMARK2");
            _certifSchoolDat125Remark3 = getString(row125, "REMARK3");
            _certifSchoolDat125Remark4 = getString(row125, "REMARK4");
            _certifSchoolDat125Remark5 = getString(row125, "REMARK5");
            _certifSchoolDat125Remark6 = getString(row125, "REMARK6");
            _certifSchoolDat125Remark7 = getString(row125, "REMARK7");
            _staffname = StringUtils.defaultString(_certifSchoolDat125JobName) + StringUtils.defaultString(_certifSchoolDat125PrincipalName);
            _staffname2 = StringUtils.defaultString(_certifSchoolDat125PrincipalName);
            _staffnameKeta = Util.getMS932ByteLength(_staffname);
            _charge = StringUtils.defaultString(_certifSchoolDat125Remark7);
        }

        /**
         * コードが「その他」（名称マスタの予備2が'1'）ならtrue、それ以外はfalse
         */
        private boolean isSonota(
                final String nameCd1,
                final String nameCd2
        ) {
            if ("1".equals(getNameMst(nameCd1, nameCd2, "NAMESPARE2"))) {
                return true;
            }
            return false;
        }

        private String getDocumentMstTitle(final String docuemntcd) {
            return getString(KnjDbUtils.firstRow(Util.getMappedList(_documentMstMap, docuemntcd)), "TITLE");
        }
        private String getDocumentMstText(final String docuemntcd) {
            return getString(KnjDbUtils.firstRow(Util.getMappedList(_documentMstMap, docuemntcd)), "TEXT");
        }
        private Map getDocumentMstMap(final DB2UDB db2) {
            final String sql = " SELECT DOCUMENTCD, TITLE, TEXT FROM DOCUMENT_MST ";
            return getColumnGroupByMap(new String[] {"DOCUMENTCD"}, KnjDbUtils.query(db2, sql));
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
        // 学年から年齢を計算する
        private double getNenrei2(final Student student, final String year1, final String year2) throws NumberFormatException {
            return 15.0 + Integer.parseInt(student._grade) - (StringUtils.isNumeric(year1) && StringUtils.isNumeric(year2) ? Integer.parseInt(year1) - Integer.parseInt(year2) : 0);
        }
        // 生年月日と対象日付から年齢を計算する
        private double getNenrei(final Student student, final String date, final String year1, final String year2) throws NumberFormatException {
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
    }

    private static String getCertif124(final DB2UDB db2,final String year) {
    	final StringBuffer stb124 = new StringBuffer();
        stb124.append(" SELECT ");
        stb124.append("     VALUE(T2.STAFFNAME, T1.REMARK5) AS REMARK5 ");
        stb124.append(" FROM CERTIF_SCHOOL_DAT T1 ");
        stb124.append(" LEFT JOIN STAFF_MST T2 ON T2.STAFFCD = T1.REMARK5 ");
        stb124.append(" WHERE YEAR = '" + year + "' AND CERTIF_KINDCD = '124' ");

        final Map row124 = KnjDbUtils.firstRow(KnjDbUtils.query(db2, stb124.toString()));

        final String rstr = StringUtils.defaultString(getString(row124, "REMARK5"),"");
        return rstr;
    }
}//クラスの括り
