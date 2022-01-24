// kanji=漢字
/*
 * $Id: e6342f351a103933ef2688c21a25602481b02ad0 $
 *
 * 作成日: 2009/04/23 14:27:31 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2004-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
import servletpack.KNJZ.detail.CsvUtils;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: e6342f351a103933ef2688c21a25602481b02ad0 $
 */
public class KNJD292 {

    private static final Log log = LogFactory.getLog("KNJD292.class");

    private static final int LINE_CNT = 25;

    private boolean _hasData;

    private static final String cmd_csv = "csv";

    private static final String ATTEND_OBJ_SEM = "9";

    private static final String TRANSFER = "TRANSFER";

    private static final String PRGID_KNJD292V = "KNJD292V";
    private static final String PRGID_KNJD292W = "KNJD292W";

    private static final String AMIKAKE_ATTRIBUTE = "Paint=(1,70,1),Bold=1";

    final String rishutyuMark = "△";
    final String mirishuMark = "×";

    private Param _param;

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        Vrw32alp svf = null;
        DB2UDB db2 = null;
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            if (cmd_csv.equals(_param._cmd)) {
                final List<List<String>> outputLines = new ArrayList<List<String>>();
                setOutputCsvLinesKNJD292W(db2, _param, outputLines);
                final Map dataMap = new HashMap();
                dataMap.put("TITLE", _param._title);
                dataMap.put("OUTPUT_LINES", outputLines);
                final Map csvParam = new HashMap();
                csvParam.put("HttpServletRequest", request);

                CsvUtils.outputJson(log, request, response, CsvUtils.toJson(dataMap), csvParam);
            } else {
                response.setContentType("application/pdf");

                svf = new Vrw32alp();

                svf.VrInit();
                svf.VrSetSpoolFileStream(response.getOutputStream());

                printMain(db2, svf, _param);
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {

            if (!cmd_csv.equals(_param._cmd)) {
                if (!_hasData) {
                    svf.VrSetForm("MES001.frm", 0);
                    svf.VrsOut("note", "note");
                    svf.VrEndPage();
                }
                svf.VrQuit();
            }

            if (null != db2) {
                db2.commit();
                db2.close();
            }
        }

    }

    private static <T> List<List<T>> getPageList(final List<T> studentList, final int max) {
        final List<List<T>> rtn = new ArrayList();
        List<T> current = null;
        for (final T t : studentList) {
            if (null == current || current.size() >= max) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(t);
        }
        return rtn;
    }

    private static String toString(final Object o, final String alt) {
        return null == o ? alt : o.toString();
    }

    protected static <T, K> List<T> getMappedList(final Map<K, List<T>> map, final K key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList<T>());
        }
        return map.get(key1);
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf, final Param param) throws Exception {
        String FORM_NAME;
        if (PRGID_KNJD292W.equals(param._prgid)) {
            FORM_NAME = "KNJD292W_2.frm";
        } else if ("1".equals(param._printShukei)) {
            FORM_NAME = "KNJD292_2.frm";
        } else {
            FORM_NAME = "KNJD292.frm";
        }
        log.info(" formname = " + FORM_NAME);
        final List<Student> allStudentList = Student.getStudentList(db2, param);
        final List<Subclass> subclassList = getSubclassList(db2, param, allStudentList);
        if (param._isOutputDebug) {
            log.info(" student list size = " + allStudentList.size() + ", subclassList size = " + subclassList.size());
        }

        for (final List<Student> studentList : getPageList(allStudentList, LINE_CNT)) {

            svf.VrSetForm(FORM_NAME, 4);
            svf.VrsOut("NENDO", param._title);
            svf.VrsOut("DATE", param._loginDateFormatted);
            svf.VrsOut("HR_NAME", param._hrClass._name);
            svf.VrsOut("GRAD_COMP_CREDITS", param._gradCompCredit);
            svf.VrsOut("GRAD_CREDITS", param._gradCredit);

            if ("1".equals(param._printShukei) || PRGID_KNJD292W.equals(param._prgid)) {
                svf.VrsOut("TOTAL_NOW_C_SUBJECT_NAME", "現在履修中科目数");
                svf.VrsOut("TOTAL_NOW_C_CREDIT_NAME", "現在履修中単位数");
                svf.VrsOut("TOTAL_YET_C_SUBJECT_NAME", "未履修科目数");
                svf.VrsOut("TOTAL_YET_C_CREDIT_NAME", "未履修単位数");
                svf.VrsOut("MARK1", rishutyuMark);
                svf.VrsOut("MARK2", mirishuMark);
            } else {
                svf.VrAttribute("MARK", "Paint=(1,70,1),Bold=1");
            }

            for (int i = 0; i < studentList.size(); i++) {
                final Student student = studentList.get(i);
                final int line = i + 1;
                svf.VrsOutn("ATTENDNO", line, student._attendNo);
                svf.VrsOutn("NAME" + (student._name.length() > 10 ? "2" : "1"), line, student._name);
                if ("1".equals(param._use_SchregNo_hyoji) || PRGID_KNJD292W.equals(_param._prgid)) {
                    svf.VrsOutn("SCHREGNO", line, student._schregno);
                }
                if ("1".equals(param._printShukei) || PRGID_KNJD292W.equals(param._prgid)) {
                    svf.VrsOutn("TOTAL_NOW_C_SUBJECT", line, student.rishutyuSubclassCount(param));
                    svf.VrsOutn("TOTAL_NOW_C_CREDIT", line, student.rishutyuSubclassCredit(param));
                    svf.VrsOutn("TOTAL_YET_C_SUBJECT", line, student.mirishuSubclassCount(param));
                    svf.VrsOutn("TOTAL_YET_C_CREDIT", line, student.mirishuSubclassCredit(param));

                    if (PRGID_KNJD292W.equals(param._prgid)) {
                        final String shukkouNissu = shukkouNissuSagaken(student._hrAttendDateList);
                        svf.VrsOutn("HR_ATTEND" + (shukkouNissu.length() > 3 ? "_2" : ""), line, shukkouNissu);
                        svf.VrsOutn("SP_ACT_ATTEND" + (StringUtils.defaultString(student._spActAttend).length() > 3 ? "_2" : ""), line, student._spActAttend);
                    }

                    svf.VrsOutn("TOTAL_C_SUBJECT", line, student.rishuSubclassCount(param));
                    svf.VrsOutn("TOTAL_C_CREDIT", line, student.rishuSubclassCredit(param));
                    svf.VrsOutn("TOTAL_G_SUBJECT", line, student.shutokuSubclassCount(param));
                    svf.VrsOutn("TOTAL_G_CREDIT", line, student.shutokuSubclassCredit(param));

                } else {
                    svf.VrsOutn("TOTAL_C_CREDIT", line, student.totalCompCredit());
                    svf.VrsOutn("TOTAL_G_CREDIT", line, student.totalCredit());
                }
            }

            int idx = 0;
            for (final Subclass subclass : subclassList) {
                //log.debug(" subClass year = " + subClass._year + ", subclasscd = " + subClass._subClassCd);

                svf.VrsOut("NENDO2", subclass.getNendo(db2));
                svf.VrsOut("ABSENCE_HIGH" + (subclass.getAbsenceHigh().length() > 2 ? "2" : "1"), subclass.getAbsenceHigh());
                svf.VrsOut("CREDIT" + (subclass.getCredit(param, allStudentList).length() > 2 ? "2" : "1"), subclass.getCredit(param, allStudentList));
                svf.VrsOut("CLASSCD", subclass._classcd);
                svf.VrsOut("CLASSNAME", subclass._classAbbv);
                if (StringUtils.defaultString(subclass._subclassName).length() > 7) {
                    svf.VrsOut("SUNCLASS2", subclass._subclassName);
                    if (PRGID_KNJD292W.equals(param._prgid)) {
                        svf.VrsOut("SUNCLASS1", String.valueOf(idx));
                        svf.VrAttribute("SUNCLASS1", "Y=10000");
                    }
                } else {
                    svf.VrsOut("SUNCLASS1", subclass._subclassName);
                    if (PRGID_KNJD292W.equals(param._prgid)) {
                        svf.VrsOut("SUNCLASS2", String.valueOf(idx));
                        svf.VrAttribute("SUNCLASS2", "Y=10000");
                    }
                }

                final String subKey = PrintData.key(subclass._schoolcd, subclass._year, subclass._classcd, subclass._subclasscd);
                for (int i = 0; i < studentList.size(); i++) {
                    final Student student = studentList.get(i);
                    final int line = i + 1;

                    final PrintData printData = student._printDataMap.get(subKey);
                    if (null == printData) {
                        continue;
                    }
                    svf.VrsOutn("AVG_VALUE", line, String.valueOf(student.getGradValueAvg(1, BigDecimal.ROUND_HALF_UP)));
                    boolean isKekkaAmikake = false;
                    if (PRGID_KNJD292V.equals(param._prgid)) {
                        if (!param._abroadSubclasscd.equals(printData._subclasscd)) {
                            svf.VrsOutn("VALUE", line, toString(printData._gradValue, ""));

                            if (null != printData._gradValue && 1 == printData._gradValue.intValue()) {
                                svf.VrAttributen("VALUE", line, AMIKAKE_ATTRIBUTE);
                            } else if (!printData._isFromAttend && null != printData._gradValue && 0 == printData._gradValue.intValue()) {
                                isKekkaAmikake = true;
                            }
                        }
                    } else if ("1".equals(param._printShukei) || PRGID_KNJD292W.equals(param._prgid)) {
                        if (printData.isMirishu(param)) {
                            svf.VrsOutn("VALUE", line, mirishuMark);
                        } else if (printData.isRishutyu(param)) {
                            svf.VrsOutn("VALUE", line, rishutyuMark);
                        } else {
                            if (!param._abroadSubclasscd.equals(printData._subclasscd)) {
                                svf.VrsOutn("VALUE", line, toString(printData._gradValue, ""));
                            }
                        }
                    } else {
                        if (!param._abroadSubclasscd.equals(printData._subclasscd)) {
                            svf.VrsOutn("VALUE", line, toString(printData._gradValue, ""));
                        }
                    }
                    svf.VrsOutn("G_CREDIT", line, toString(printData._credit, ""));
                    if (!param._abroadSubclasscd.equals(printData._subclasscd) && null != printData._kekka && printData._kekka.doubleValue() > 0.0) {
                        if (PRGID_KNJD292V.equals(param._prgid)) {
                            if (isKekkaAmikake) {
                                svf.VrAttributen("KEKKA", line, AMIKAKE_ATTRIBUTE);
                            }
                            svf.VrsOutn("KEKKA", line, String.valueOf(printData._kekka.intValue()));
                            if (isKekkaAmikake) {
                                svf.VrAttributen("KEKKA", line, "Paint=(0,0,0),Bold=0");
                            }
                        } else {
                            final RegdDat regdDat = student._regdMap.get(subclass._year);
                            if (null != regdDat) {
                                int absenceHigh = -1;
                                if (_param._knjSchoolMst.isJitu()) {
                                    final BigDecimal schregAbsenceHigh = student._subclassAbsenceHigh.get(subclass._subclasscd);
                                    if (null != schregAbsenceHigh) {
                                        absenceHigh = schregAbsenceHigh.intValue();
                                    }
                                } else if (_param._knjSchoolMst.isHoutei()) {
                                    final String key = regdDat._year + StringUtils.defaultString(regdDat._courseCd, "0") + StringUtils.defaultString(regdDat._majorCd, "000") + StringUtils.defaultString(regdDat._grade, "00") + StringUtils.defaultString(regdDat._courseCode, "0000");
                                    final CreditMst creditMst = subclass._creditMst.get(key);
                                    if (null != creditMst) {
                                        absenceHigh = creditMst._absenHigh;
                                    }
                                }
                                if (absenceHigh > 0) {
                                    boolean kekkaIsOver = printData._kekka.intValue() > absenceHigh;
                                    if (kekkaIsOver) {
                                        isKekkaAmikake = true;
                                        svf.VrAttributen("KEKKA", line, AMIKAKE_ATTRIBUTE);
                                    }
                                    //log.info(" " + student._schregno +" 欠課数上限=" + absenceHigh + " , over?" + kekkaIsOver + " (欠課数=" + printData._kekka + ")");
                                }
                            }
                            svf.VrsOutn("KEKKA", line, String.valueOf(printData._kekka.intValue()));
                            if (isKekkaAmikake) {
                                svf.VrAttributen("KEKKA", line, "Paint=(0,0,0),Bold=0");
                            }
                        }
                    }
                }
                idx += 1;

                svf.VrEndRecord();
            }
            _hasData = true;
        }
    }

    private void setOutputCsvLinesKNJD292W(final DB2UDB db2, final Param param, final List<List<String>> outputLines) throws SQLException {
        final List<Student> allStudentList = Student.getStudentList(db2, param);
        final List<Subclass> subclassList = getSubclassList(db2, param, allStudentList);
        if (param._isOutputDebug) {
            log.info(" student list size = " + allStudentList.size() + ", subclassList size = " + subclassList.size());
        }

        CsvUtils.newLine(outputLines).add(param._title);
        CsvUtils.newLine(outputLines).addAll(Arrays.asList("", "", "", "上段：評定", "", "", "", "", "", param._loginDateFormatted));
        CsvUtils.newLine(outputLines).addAll(Arrays.asList(param._hrClass._name, "", "", "下段：単位数"));
        CsvUtils.newLine(outputLines);
        final List<String> nendoHeader = CsvUtils.newLine(outputLines);
        nendoHeader.addAll(Arrays.asList("年度", "", ""));
//        final List<String> gendoHeader = CsvUtils.newLine(outputLines);
//        gendoHeader.addAll(Arrays.asList("限度", "", ""));
        final List<String> tanniHeader = CsvUtils.newLine(outputLines);
        tanniHeader.addAll(Arrays.asList("単位", "", ""));
        final List<String> classnameHeader = CsvUtils.newLine(outputLines);
        classnameHeader.addAll(Arrays.asList("", "教科・科目", ""));
        final List<String> subclassnameHeader = CsvUtils.newLine(outputLines);
        subclassnameHeader.addAll(Arrays.asList("学籍番号", "氏名", "平均評定"));

        final List<String> footer = new ArrayList<String>();
        footer.addAll(Arrays.asList("", "", rishutyuMark + ":履修中" + " " + mirishuMark + ":未履修"));

        String oldClassAbbv = "";
        for (final Subclass subclass : subclassList) {
            //log.debug(" subClass year = " + subClass._year + ", subclasscd = " + subClass._subClassCd);

            nendoHeader.add(subclass.getNendo(db2));
//            gendoHeader.add(subclass.getAbsenceHigh());
            tanniHeader.add(subclass.getCredit(param, allStudentList));
            classnameHeader.add(StringUtils.defaultString(subclass._classAbbv).equals(StringUtils.defaultString(oldClassAbbv)) ? "" : subclass._classAbbv);
            oldClassAbbv = StringUtils.defaultString(subclass._classAbbv);
            subclassnameHeader.add(subclass._subclassName);
        }
        final List<Integer> totalIdxList = new ArrayList<Integer>();
        final int GENZAI_RISHU_KAMOKU_SU = 1;
        final int GENZAI_RISHU_TANNI_SU = 2;
        final int SHUKKO_NISSU = 3;
        final int TOKUBETSU_KATSUDOU = 4;
        final int MIRISHU_KAMOKU_SU = 5;
        final int MIRISHU_TANNI_SU = 6;
        final int RISHU_KAMOKU_SU = 7;
        final int RISHU_TANNI_SU = 8;
        final int SHUTOKU_KAMOKU_SU = 9;
        final int SHUTOKU_TANNI_SU = 10;
        if (PRGID_KNJD292W.equals(param._prgid)) {
            totalIdxList.addAll(Arrays.asList(GENZAI_RISHU_KAMOKU_SU, GENZAI_RISHU_TANNI_SU, SHUKKO_NISSU, TOKUBETSU_KATSUDOU, MIRISHU_KAMOKU_SU, MIRISHU_TANNI_SU, RISHU_KAMOKU_SU, RISHU_TANNI_SU, SHUTOKU_KAMOKU_SU, SHUTOKU_TANNI_SU));
        }
        for (int totalIdx : totalIdxList) {

            if (PRGID_KNJD292W.equals(param._prgid) || "1".equals(param._printShukei)) {
                tanniHeader.add("");
            } else {
                if (totalIdx == RISHU_TANNI_SU) {
                    tanniHeader.add(param._gradCompCredit);
                } else if (totalIdx == SHUTOKU_TANNI_SU) {
                    tanniHeader.add(param._gradCredit);
                } else {
                    tanniHeader.add("");
                }
            }

            if (totalIdx == GENZAI_RISHU_KAMOKU_SU) {
                subclassnameHeader.add(rishutyuMark + "現在履修中科目数");
            } else if (totalIdx == GENZAI_RISHU_TANNI_SU) {
                subclassnameHeader.add(rishutyuMark + "現在履修中単位数");
            } else if (totalIdx == SHUKKO_NISSU) {
                subclassnameHeader.add("出校日数");
            } else if (totalIdx == TOKUBETSU_KATSUDOU) {
                subclassnameHeader.add("特別活動");
            } else if (totalIdx == MIRISHU_KAMOKU_SU) {
                subclassnameHeader.add(mirishuMark + "未履修科目数");
            } else if (totalIdx == MIRISHU_TANNI_SU) {
                subclassnameHeader.add(mirishuMark + "未履修単位数");
            } else if (totalIdx == RISHU_KAMOKU_SU) {
                subclassnameHeader.add("履修科目数");
            } else if (totalIdx == RISHU_TANNI_SU) {
                subclassnameHeader.add("履修単位数");
            } else if (totalIdx == SHUTOKU_KAMOKU_SU) {
                subclassnameHeader.add("修得科目数");
            } else if (totalIdx == SHUTOKU_TANNI_SU) {
                subclassnameHeader.add("修得単位数");
            }
        }


        for (final Student student : allStudentList) {

            final List<String> hyoteiLine = CsvUtils.newLine(outputLines);
            hyoteiLine.addAll(Arrays.asList(student._schregno, student._name, String.valueOf(student.getGradValueAvg(1, BigDecimal.ROUND_HALF_UP))));
            final List<String> tanniLine = CsvUtils.newLine(outputLines);
            tanniLine.addAll(Arrays.asList("", "", ""));
//            final List<String> kekkaLine = CsvUtils.newLine(outputLines);
//            kekkaLine.addAll(Arrays.asList("", "", ""));

            for (final Subclass subclass : subclassList) {
                //log.debug(" subClass year = " + subClass._year + ", subclasscd = " + subClass._subClassCd);

                final String subKey = PrintData.key(subclass._schoolcd, subclass._year, subclass._classcd, subclass._subclasscd);

                final PrintData printData = student._printDataMap.get(subKey);
                if (null == printData) {
                    hyoteiLine.add("");
                    tanniLine.add("");
//                    kekkaLine.add("");
                } else {
                    String hyotei = null;
                    if (PRGID_KNJD292V.equals(param._prgid)) {
                        if (!param._abroadSubclasscd.equals(printData._subclasscd)) {
                            hyotei = toString(printData._gradValue, "");
                        }
                    } else if ("1".equals(param._printShukei) || PRGID_KNJD292W.equals(param._prgid)) {
                        if (printData.isMirishu(param)) {
                            hyotei = mirishuMark;
                        } else if (printData.isRishutyu(param)) {
                            hyotei = rishutyuMark;
                        } else {
                            if (!param._abroadSubclasscd.equals(printData._subclasscd)) {
                                hyotei = toString(printData._gradValue, "");
                            }
                        }
                    } else {
                        if (!param._abroadSubclasscd.equals(printData._subclasscd)) {
                            hyotei = toString(printData._gradValue, "");
                        }
                    }
                    hyoteiLine.add(hyotei);
                    tanniLine.add(toString(printData._credit, ""));
//                  String kekka = "";
//                  if (!param._abroadSubclasscd.equals(printData._subclasscd) && null != printData._kekka && printData._kekka.doubleValue() > 0.0) {
//                      if (PRGID_KNJD292V.equals(param._prgid)) {
//                          kekka = String.valueOf(printData._kekka.intValue());
//                      } else {
//                          final RegdDat regdDat = student._regdMap.get(subclass._year);
//                          if (null != regdDat) {
//                              int absenceHigh = -1;
//                              if (param._knjSchoolMst.isJitu()) {
//                                  final BigDecimal schregAbsenceHigh = student._subclassAbsenceHigh.get(subclass._subclasscd);
//                                  if (null != schregAbsenceHigh) {
//                                      absenceHigh = schregAbsenceHigh.intValue();
//                                  }
//                              } else if (param._knjSchoolMst.isHoutei()) {
//                                  final String key = regdDat._year + StringUtils.defaultString(regdDat._courseCd, "0") + StringUtils.defaultString(regdDat._majorCd, "000") + StringUtils.defaultString(regdDat._grade, "00") + StringUtils.defaultString(regdDat._courseCode, "0000");
//                                  final CreditMst creditMst = subclass._creditMst.get(key);
//                                  if (null != creditMst) {
//                                      absenceHigh = creditMst._absenHigh;
//                                  }
//                              }
//                          }
//                          kekka = String.valueOf(printData._kekka.intValue());
//                      }
//                  }
//                    kekkaLine.add(kekka);
                }
            }

            for (int totalIdx : totalIdxList) {
                if (totalIdx == GENZAI_RISHU_KAMOKU_SU) {
                    hyoteiLine.add(student.rishutyuSubclassCount(param));
                } else if (totalIdx == GENZAI_RISHU_TANNI_SU) {
                    hyoteiLine.add(student.rishutyuSubclassCredit(param));
                } else if (totalIdx == SHUKKO_NISSU) {
                    hyoteiLine.add(shukkouNissuSagaken(student._hrAttendDateList));
                } else if (totalIdx == TOKUBETSU_KATSUDOU) {
                    hyoteiLine.add(student._spActAttend);
                } else if (totalIdx == MIRISHU_KAMOKU_SU) {
                    hyoteiLine.add(student.mirishuSubclassCount(param));
                } else if (totalIdx == MIRISHU_TANNI_SU) {
                    hyoteiLine.add(student.mirishuSubclassCredit(param));
                } else if (totalIdx == RISHU_KAMOKU_SU) {
                    hyoteiLine.add(student.rishuSubclassCount(param));
                } else if (totalIdx == RISHU_TANNI_SU) {
                    hyoteiLine.add(student.rishuSubclassCredit(param));
                } else if (totalIdx == SHUTOKU_KAMOKU_SU) {
                    hyoteiLine.add(student.shutokuSubclassCount(param));
                } else if (totalIdx == SHUTOKU_TANNI_SU) {
                    hyoteiLine.add(student.shutokuSubclassCredit(param));
                }
            }
        }
        outputLines.add(footer);
    }

    private List<Subclass> getSubclassList(final DB2UDB db2, final Param param, final List<Student> studentAllList) throws SQLException {
        final Map<String, Subclass> subclassMap = new HashMap();
        for (final Student student : studentAllList) {
            for (final PrintData printData : student._printDataMap.values()) {

                final String key = PrintData.key(printData._schoolcd, printData._year, printData._classcd, printData._subclasscd);

                if (!subclassMap.containsKey(key)) {
                    subclassMap.put(key, new Subclass(printData._schoolcd, printData._year, printData._classcd, printData._subclasscd));
                }
            }
        }
        if (param._isOutputDebug) {
            log.info(" subclassMap size = " + subclassMap.size());
        }
        Subclass.setSubclassInfo(db2, param, studentAllList, subclassMap);
        final List<Subclass> subclassList = new ArrayList<Subclass>(subclassMap.values());
        Collections.sort(subclassList);
        return subclassList;
    }

    public static String shukkouNissuSagaken(final List<Map<String, String>> rowList) {
        final Set<String> set = new TreeSet<String>();
        for (final Map<String, String> row : rowList) {

            if ("SPECIAL".equals(KnjDbUtils.getString(row, "TABLEDIV"))) {
                // 特別活動
                if (!"1".equals(KnjDbUtils.getString(row, "M026_NAMESPARE1")) && null != KnjDbUtils.getString(row, "M027_NAME1")) {
                    set.add(KnjDbUtils.getString(row, "EXECUTEDATE"));
                }
            }
        }
        return String.valueOf(set.size());
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 58635 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _year;
        final String _regdSemester;
        final String _semester;
        final String _gradeHrClass;
        final String _schoolKind;
        final String[] _classSelected; // 学籍番号
        final String _cmd;
        String _yearDiv; // 1:全年度 2:当年度のみ
        final String _prgid;
        final String _date;
        final String _loginDate;
        final String _printShukei; // 集計を印字する
        final HrClass _hrClass;
        final boolean _isGakunenMatu;
        private String _gradCredit;
        private String _gradCompCredit;
        /** 教育課程コードを使用するか */
        final String _useCurriculumcd;
        final String _useTestCountflg;
        final String _abroadSubclasscd;
        final String _nendo;
        final String _title;
        final String _loginDateFormatted;
        final boolean _hasANOTHER_CLASS_MST;
        final boolean _hasANOTHER_SUBCLASS_MST;

        /** 名称マスタ（学校等） */
        private String _z010Name1; //学校
        private String _z010NameSpare1; //record_score_dat使用フラグ
        private Map _attendParamMap;
        private KNJSchoolMst _knjSchoolMst;
        private boolean _isOutputDebugAll;
        private boolean _isOutputDebug;
        private boolean _isOutputDebugQuery;

        /** 氏名欄に学籍番号の表示/非表示 1:表示 それ以外:非表示 */
        final String _use_SchregNo_hyoji;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _isGakunenMatu = "9".equals(_semester) ? true : false;
            _regdSemester = _isGakunenMatu ? request.getParameter("CTRL_SEMESTER") : _semester;
            _prgid = request.getParameter("PRGID");
            _cmd = request.getParameter("cmd");
            boolean hasYearDiv = false;
            final Enumeration<String> enums = request.getParameterNames();
            while (enums.hasMoreElements()) {
                final String name = enums.nextElement();
                if ("YEAR_DIV".equals(name)) {
                    hasYearDiv = true;
                    break;
                }
            }
            _yearDiv = hasYearDiv ? request.getParameter("YEAR_DIV") : "1" ;
            _date = request.getParameter("DATE").replace('/', '-');

            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            _schoolKind = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _gradeHrClass.substring(0, 2) + "' "));

            if (cmd_csv.equals(_cmd)) {
                _classSelected = StringUtils.split(request.getParameter("CLASS_SELECTED"), ",");
            } else {
                _classSelected = request.getParameterValues("CLASS_SELECTED");
            }
            _loginDate = request.getParameter("LOGIN_DATE");
            _printShukei = request.getParameter("PRINT_SHUKEI");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useTestCountflg = request.getParameter("useTestCountflg");
            _use_SchregNo_hyoji = request.getParameter("use_SchregNo_hyoji");
            setSchoolMst(db2);
            _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_year)) + "年度";
            if (PRGID_KNJD292W.equals(_prgid)) {
                _title = _nendo + "　成績判定会議資料";
            } else if (PRGID_KNJD292V.equals(_prgid)) {
                _title = _nendo + "　成績判定会議資料（生徒別未履修／履修のみ科目）";
            } else if ("2".equals(_yearDiv)) {
                _title = _nendo + "　単位認定状況一覧";
            } else {
                _title = _nendo + "　卒業見込者判定会議資料（明細）";
            }
            _loginDateFormatted = KNJ_EditDate.h_format_JP(db2, _loginDate);

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);

            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }
            setNameMst(db2);
            _hrClass = getHrclass(db2);

            _abroadSubclasscd = "1".equals(_useCurriculumcd) ? TRANSFER + "-" + TRANSFER + "-" + TRANSFER + "-" + TRANSFER : TRANSFER;
            log.debug(" useRecordScoreDat = " + isUseRecordScoreDat() + ", useTestCountflg = " + _useTestCountflg);

            _hasANOTHER_CLASS_MST = KnjDbUtils.setTableColumnCheck(db2, "ANOTHER_CLASS_MST", null);
            _hasANOTHER_SUBCLASS_MST = KnjDbUtils.setTableColumnCheck(db2, "ANOTHER_SUBCLASS_MST", null);

            final String[] outputDebug = StringUtils.split(KnjDbUtils.getDbPrginfoProperties(db2, "KNJD292", "outputDebug"));
            _isOutputDebugAll = ArrayUtils.contains(outputDebug, "all");
            _isOutputDebug = _isOutputDebugAll || ArrayUtils.contains(outputDebug, "1");
            _isOutputDebugQuery = _isOutputDebugAll || ArrayUtils.contains(outputDebug, "query");
        }

        private void setSchoolMst(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final boolean setSchoolKind = KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND");
                final String sql = "SELECT GRAD_CREDITS, GRAD_COMP_CREDITS FROM SCHOOL_MST WHERE YEAR = '" + _year + "' " + (setSchoolKind ? " AND SCHOOL_KIND = '" + _schoolKind + "' " : "");

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _gradCredit = rs.getString("GRAD_CREDITS");
                    _gradCompCredit = rs.getString("GRAD_COMP_CREDITS");
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private void setNameMst(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sqlNameMst("Z010", "00"));
                rs = ps.executeQuery();
                if (rs.next()) {
                    _z010Name1 = rs.getString("NAME1");
                    _z010NameSpare1 = rs.getString("NAMESPARE1");
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.debug("学校=" + _z010Name1 + "、成績テーブル=" + getRecordTable());
        }

        private String sqlNameMst(final String namecd1, final String namecd2) {
            final String sql = " SELECT "
                + "     * "
                + " FROM "
                + "     NAME_MST "
                + " WHERE "
                + "         NAMECD1 = '" + namecd1 + "' "
                + "     AND NAMECD2 = '" + namecd2 + "'";
            return sql;
        }

        public String getRecordTable() {
            return isUseRecordScoreDat() ? "RECORD_SCORE_DAT" : "RECORD_DAT";
        }

        public boolean isUseRecordScoreDat() {
            return _z010NameSpare1 != null;
        }

        private HrClass getHrclass(
                final DB2UDB db2
        ) throws SQLException {
            HrClass hrClass = null;
            final String sql = " SELECT "
                             + "     * "
                             + " FROM "
                             + "     SCHREG_REGD_HDAT "
                             + " WHERE "
                             + "         YEAR = '" + _year + "' "
                             + "     AND SEMESTER = '" + _regdSemester + "'"
                             + "     AND GRADE || HR_CLASS = '" + _gradeHrClass + "'";

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String grade = rs.getString("GRADE");
                    final String hrClassCd = rs.getString("HR_CLASS");
                    final String hrName = rs.getString("HR_NAME");
                    final String hrAbbv = rs.getString("HR_NAMEABBV");
                    hrClass = new HrClass(grade, hrClassCd, hrName, hrAbbv);
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return hrClass;
        }

    }

    private static class HrClass {
        final String _grade;
        final String _hrClass;
        final String _name;
        final String _abbv;

        public HrClass(
                final String grade,
                final String hrclass,
                final String hrName,
                final String hrAbbv
        ) {
            _grade = grade;
            _hrClass = hrclass;
            _name = hrName;
            _abbv = hrAbbv;
        }

        public String toString() {
            return "学年：" + _grade +
                    " クラス：" + _hrClass +
                    " 名称：" + _name;
        }
    }

    private static class Student {
        final String _schregno;
        final String _attendNo;
        final String _name;
        Map<String, RegdDat> _regdMap = new HashMap();
        Map<String, PrintData> _printDataMap = new HashMap();
        Map<String, BigDecimal> _subclassAbsenceHigh = new HashMap();
        String _spActAttend;
        final List<Map<String, String>> _hrAttendDateList = new ArrayList<Map<String, String>>();

        public Student(
                final String schregNo,
                final String attendNo,
                final String name,
                final Map regdMap
        ) {
            _schregno = schregNo;
            _attendNo = attendNo;
            _name = name;
            _regdMap = regdMap;
        }

        public String rishutyuSubclassCount(final Param param) {
            return subclassCount(rishutyuSubclasses(param));
        }

        public String rishutyuSubclassCredit(final Param param) {
            return sumCreditMstCredit(rishutyuSubclasses(param));
        }

        public String mirishuSubclassCount(final Param param) {
            return subclassCount(mirishuSubclasses(param));
        }

        public String mirishuSubclassCredit(final Param param) {
            return sumCreditMstCredit(mirishuSubclasses(param));
        }

        public String rishuSubclassCount(Param param) {
            return subclassCount(rishuSubclasses(param));
        }

        public String rishuSubclassCredit(Param param) {
            return sumCompCredit(rishuSubclasses(param));
        }

        public String shutokuSubclassCount(Param param) {
            return subclassCount(shutokuSubclasses(param));
        }

        public String shutokuSubclassCredit(Param param) {
            return sumCredit(shutokuSubclasses(param));
        }

        private static String subclassCount(final Collection<PrintData> list) {
            final Set subclassCount = new HashSet();
            for (final PrintData printData : list) {
                if (null != printData._subclasscd) {
                    subclassCount.add(printData._subclasscd);
                }
            }
            return String.valueOf(subclassCount.size());
        }

        private static String sumCredit(final Collection<PrintData> list) {
            int sum = 0;
            for (final PrintData printData : list) {
                if (null != printData._credit && !printData._isCalFlg1AttendSubclass && !printData._isCalFlg2CombinedSubclass) {
                    sum += printData._credit.intValue();
                }
            }
            return String.valueOf(sum);
        }

        private static String sumCompCredit(final Collection<PrintData> list) {
            int sum = 0;
            for (final PrintData printData : list) {
                if (null != printData._compCredit && !printData._isCalFlg1AttendSubclass && !printData._isCalFlg2CombinedSubclass) {
                    sum += printData._compCredit.intValue();
                }
            }
            return String.valueOf(sum);
        }

        private static String sumCreditMstCredit(final Collection<PrintData> list) {
            int sum = 0;
            for (final PrintData printData : list) {
                if (null != printData._creditMstCredit && !printData._isCalFlg1AttendSubclass && !printData._isCalFlg2CombinedSubclass) {
                    sum += printData._creditMstCredit.intValue();
                }
            }
            return String.valueOf(sum);
        }

        private List<PrintData> rishuSubclasses(final Param param) {
            final List<PrintData> list = new ArrayList();
            for (final PrintData printData : _printDataMap.values()) {
                if (null != printData._compCredit && !printData._isCalFlg1AttendSubclass && !printData._isCalFlg2CombinedSubclass) {
                    list.add(printData);
                }
            }
            return list;
        }

        private List<PrintData> shutokuSubclasses(final Param param) {
            final List<PrintData> list = new ArrayList();
            for (final PrintData printData : _printDataMap.values()) {
                if (null != printData._credit && !printData._isCalFlg1AttendSubclass && !printData._isCalFlg2CombinedSubclass) {
                    list.add(printData);
                }
            }
            return list;
        }

        private List<PrintData> mirishuSubclasses(final Param param) {
            final List<PrintData> list = new ArrayList();
            for (final PrintData printData : _printDataMap.values()) {
                if (printData.isMirishu(param)) {
                    list.add(printData);
                }
            }
            return list;
        }

        private List<PrintData> rishutyuSubclasses(final Param param) {
            final List<PrintData> list = new ArrayList();
            for (final PrintData printData : _printDataMap.values()) {
                if (printData.isRishutyu(param)) {
                    list.add(printData);
                }
            }
            return list;
        }

        public String totalCompSubclassCount() {
            return subclassCount(_printDataMap.values());
        }

        public String totalCompCredit() {
            int sum = 0;
            for (final PrintData printData : _printDataMap.values()) {
                if (null != printData._compCredit && !printData._isCalFlg1AttendSubclass && !printData._isCalFlg2CombinedSubclass) {
                    sum += printData._compCredit.intValue();
                }
            }
            return String.valueOf(sum);
        }

        public String totalCredit() {
            int sum = 0;
            for (final PrintData printData : _printDataMap.values()) {
                if (null != printData._credit && !printData._isCalFlg1AttendSubclass && !printData._isCalFlg2CombinedSubclass) {
                    sum += printData._credit.intValue();
                }
            }
            return String.valueOf(sum);
        }

        public static void setPrintData(final DB2UDB db2, final Param param, final Collection<Student> studentList) throws SQLException {
            PreparedStatement ps = null;
            try {
                final String printSql = getPrintDataSql(param);
                if (param._isOutputDebug) {
                    log.info(" sql = " + printSql);
                }
                ps = db2.prepareStatement(printSql);

                for (final Student student : studentList) {

                    for (final Map<String, String> row : KnjDbUtils.query(db2, ps, new Object[] { student._schregno })) {

                        final String schoolcd = KnjDbUtils.getString(row, "SCHOOLCD");
                        final String year = KnjDbUtils.getString(row, "YEAR");
                        final String classCd = KnjDbUtils.getString(row, "CLASSCD");
                        final String subClassCd = KnjDbUtils.getString(row, "SUBCLASSCD");
                        final Integer credit = KnjDbUtils.getInt(row, "CREDIT", null);
                        final Integer gradValue = KnjDbUtils.getInt(row, "GRAD_VALUE", null);
                        final Integer compCredit = KnjDbUtils.getInt(row, "COMP_CREDIT", null);
                        final Integer creditMstCredit = KnjDbUtils.getInt(row, "CREDIT_MST_CREDIT", null);
                        final boolean isCalFlg1AttendSubclass = null != KnjDbUtils.getString(row, "CAL_FLG1_ATTEND_SUBCLASS");
                        final boolean isCalFlg2CombinedSubclass = null != KnjDbUtils.getString(row, "CAL_FLG2_COMBINED_SUBCLASS");

                        final PrintData printData = new PrintData(schoolcd, year, classCd, subClassCd, credit, gradValue, compCredit, creditMstCredit, new Double(0), isCalFlg1AttendSubclass, isCalFlg2CombinedSubclass);
                        student._printDataMap.put(PrintData.key(schoolcd, year, classCd, subClassCd), printData);

                        student._subclassAbsenceHigh.put(subClassCd, KnjDbUtils.getBigDecimal(row, "SCHREG_ABSENCE_HIGH", null));
                    }
                }
            } catch (SQLException e) {
                log.error("sql exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(ps);
            }
        }

        private static String getPrintDataSql(final Param param) {

            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCHREGNOS AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO ");
            stb.append(" FROM ");
            stb.append("     SCHREG_BASE_MST T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.SCHREGNO = ? ");
            stb.append(" ), SCH_SUB AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.YEAR, ");
            stb.append("     substr(L1.SUBCLASSCD, 1, 2) AS CLASSCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     L1.SCHOOL_KIND, ");
                stb.append("     L1.CURRICULUM_CD, ");
            }
            stb.append("     L1.SUBCLASSCD, ");
            stb.append("     T1.SCHREGNO ");
            stb.append(" FROM ");
            stb.append("     CHAIR_STD_DAT T1 ");
            stb.append("     INNER JOIN CHAIR_DAT L1 ON T1.YEAR = L1.YEAR ");
            stb.append("          AND T1.SEMESTER = L1.SEMESTER ");
            stb.append("          AND T1.CHAIRCD = L1.CHAIRCD ");
            stb.append("     INNER JOIN SCHREGNOS T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append(" GROUP BY ");
            stb.append("     T1.YEAR, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     L1.SCHOOL_KIND, ");
                stb.append("     L1.CURRICULUM_CD, ");
            }
            stb.append("     L1.SUBCLASSCD, ");
            stb.append("     T1.SCHREGNO ");
            stb.append(" ), RECORD AS ( ");
            stb.append(" SELECT ");
            stb.append("     '0' AS SCHOOLCD, "); // 本校
            stb.append("     T1.YEAR, ");
            stb.append("     T1.CLASSCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T1.SCHOOL_KIND, ");
                stb.append("     T1.CURRICULUM_CD, ");
            }
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     CASE WHEN L1.GET_CREDIT IS NULL AND L1.ADD_CREDIT IS NULL THEN CAST(NULL AS SMALLINT) ");
            stb.append("       ELSE VALUE(L1.GET_CREDIT, 0) + VALUE(L1.ADD_CREDIT,0) ");
            stb.append("     END AS CREDIT, ");
            stb.append("     L1.COMP_CREDIT AS COMP_CREDIT, ");
            if (PRGID_KNJD292W.equals(param._prgid)) {
                stb.append("     L1.VALUE AS GRAD_VALUE ");
            } else if (param.isUseRecordScoreDat()) {
                if ("TESTITEM_MST_COUNTFLG_NEW_SDIV".equals(param._useTestCountflg) || "TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV".equals(param._useTestCountflg)) {
                    stb.append("     L1.SCORE AS GRAD_VALUE ");
                } else {
                    stb.append("     L1.VALUE AS GRAD_VALUE ");
                }
            } else {
                if (param._isGakunenMatu) {
                    stb.append("     L1.GRAD_VALUE AS GRAD_VALUE ");
                } else {
                    stb.append("     L1.SEM" + param._regdSemester + "_VALUE AS GRAD_VALUE ");
                }
            }
            stb.append(" FROM ");
            stb.append("     SCH_SUB T1 ");
            stb.append("     INNER JOIN SCHREGNOS T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            if (PRGID_KNJD292W.equals(param._prgid)) {
                stb.append("     LEFT JOIN V_RECORD_SCORE_HIST_DAT L1 ON T1.YEAR = L1.YEAR ");
                stb.append("          AND L1.SEMESTER = '" + param._semester + "' ");
                stb.append("          AND L1.TESTKINDCD = '99' ");
                stb.append("          AND L1.TESTITEMCD = '00' ");
                stb.append("          AND L1.SCORE_DIV = '09' ");
            } else if (param.isUseRecordScoreDat()) {
                stb.append("     LEFT JOIN RECORD_SCORE_DAT L1 ON T1.YEAR = L1.YEAR ");
                stb.append("          AND L1.SEMESTER = '" + param._semester + "' ");
                stb.append("          AND L1.TESTKINDCD = '99' ");
                stb.append("          AND L1.TESTITEMCD = '00' ");
                if ("TESTITEM_MST_COUNTFLG_NEW_SDIV".equals(param._useTestCountflg) || "TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV".equals(param._useTestCountflg)) {
                    stb.append("          AND L1.SCORE_DIV = '09' ");
                } else {
                    stb.append("          AND L1.SCORE_DIV = '00' ");
                }
            } else {
                stb.append("     LEFT JOIN RECORD_DAT L1 ON T1.YEAR = L1.YEAR ");
            }
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("          AND T1.CLASSCD = L1.CLASSCD ");
                stb.append("          AND T1.SCHOOL_KIND = L1.SCHOOL_KIND ");
                stb.append("          AND T1.CURRICULUM_CD = L1.CURRICULUM_CD ");
            }
            stb.append("          AND T1.SUBCLASSCD = L1.SUBCLASSCD ");
            stb.append("          AND T1.SCHREGNO = L1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append(" ), STUDY_O AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHOOLCD, ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.CLASSCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T1.SCHOOL_KIND, ");
                stb.append("     T1.CURRICULUM_CD, ");
            }
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     CASE WHEN T1.GET_CREDIT IS NULL AND T1.ADD_CREDIT IS NULL THEN CAST(NULL AS SMALLINT) ");
            stb.append("       ELSE SUM(VALUE(T1.GET_CREDIT, 0)) + SUM(VALUE(T1.ADD_CREDIT,0)) ");
            stb.append("     END AS CREDIT, ");
            stb.append("     SUM(T1.COMP_CREDIT) AS COMP_CREDIT, ");
            stb.append("     SUM(T1.VALUATION) AS GRAD_VALUE ");
            stb.append(" FROM ");
            stb.append("     SCHREG_STUDYREC_DAT T1 ");
            stb.append("     INNER JOIN SCHREGNOS T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append(" WHERE ");
            if ("1".equals(param._yearDiv)) {
                stb.append("     T1.YEAR < '" + param._year + "' ");
            } else {
                stb.append("     T1.YEAR IS NULL ");
            }
            stb.append(" GROUP BY ");
            stb.append("     T1.SCHOOLCD, ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.CLASSCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T1.SCHOOL_KIND, ");
                stb.append("     T1.CURRICULUM_CD, ");
            }
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     T1.GET_CREDIT, ");
            stb.append("     T1.ADD_CREDIT, ");
            stb.append("     T1.SCHREGNO ");
            stb.append(" ), QUALIFIED AS ( ");
            stb.append(" SELECT ");
            stb.append("     '0' AS SCHOOLCD, "); // 本校
            stb.append("     T1.YEAR, ");
            stb.append("     substr(T1.SUBCLASSCD, 1, 2) AS CLASSCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T1.SCHOOL_KIND, ");
                stb.append("     T1.CURRICULUM_CD, ");
            }
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     SUM(T1.CREDITS) AS CREDIT, ");
            stb.append("     SUM(T1.CREDITS) AS COMP_CREDIT, ");
            stb.append("     0 AS GRAD_VALUE ");
            stb.append(" FROM ");
            stb.append("     SCHREG_QUALIFIED_DAT T1 ");
            stb.append("     INNER JOIN SCHREGNOS T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + param._year + "' ");
            stb.append("     AND T1.CONDITION_DIV IN ('1', '2') ");
            stb.append(" GROUP BY ");
            stb.append("     T1.YEAR, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T1.SCHOOL_KIND, ");
                stb.append("     T1.CURRICULUM_CD, ");
            }
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     T1.SCHREGNO ");
            stb.append(" ), TRANSFER_O AS ( ");
            stb.append(" SELECT ");
            stb.append("     '0' AS SCHOOLCD, "); // 本校
            stb.append("     FISCALYEAR(TRANSFER_SDATE) AS YEAR,");
            stb.append("     '" + TRANSFER + "' AS CLASSCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     '" + TRANSFER + "' AS SCHOOL_KIND, ");
                stb.append("     '" + TRANSFER + "' AS CURRICULUM_CD, ");
            }
            stb.append("     '" + TRANSFER + "' AS SUBCLASSCD, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     SUM(T1.ABROAD_CREDITS) AS CREDIT, ");
            stb.append("     SUM(T1.ABROAD_CREDITS) AS COMP_CREDIT, ");
            stb.append("     0 AS GRAD_VALUE ");
            stb.append(" FROM ");
            stb.append("     SCHREG_TRANSFER_DAT T1 ");
            stb.append("     INNER JOIN SCHREGNOS T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     T1.TRANSFERCD IN ('1') ");
            if ("1".equals(param._yearDiv)) {
                stb.append("     AND '" + param._year + "' >  ");
            } else {
                stb.append("     AND 'XXXX' = ");
            }
            stb.append("         FISCALYEAR(TRANSFER_SDATE) ");
            stb.append(" GROUP BY ");
            stb.append("     FISCALYEAR(TRANSFER_SDATE),");
            stb.append("     T1.SCHREGNO ");
            stb.append(" ), TRANSFER_N AS ( ");
            stb.append(" SELECT ");
            stb.append("     '0' AS SCHOOLCD, "); // 本校
            stb.append("     FISCALYEAR(TRANSFER_SDATE) AS YEAR,");
            stb.append("     '" + TRANSFER + "' AS CLASSCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     '" + TRANSFER + "' AS SCHOOL_KIND, ");
                stb.append("     '" + TRANSFER + "' AS CURRICULUM_CD, ");
            }
            stb.append("     '" + TRANSFER + "' AS SUBCLASSCD, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     SUM(T1.ABROAD_CREDITS) AS CREDIT, ");
            stb.append("     SUM(T1.ABROAD_CREDITS) AS COMP_CREDIT, ");
            stb.append("     0 AS GRAD_VALUE ");
            stb.append(" FROM ");
            stb.append("     SCHREG_TRANSFER_DAT T1 ");
            stb.append("     INNER JOIN SCHREGNOS T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     T1.TRANSFERCD IN ('1') ");
            stb.append("     AND '" + param._year + "' = FISCALYEAR(TRANSFER_SDATE) ");
            stb.append(" GROUP BY ");
            stb.append("     FISCALYEAR(TRANSFER_SDATE),");
            stb.append("     T1.SCHREGNO ");
            stb.append(" ), MAIN_T AS ( ");
            stb.append("           SELECT * FROM RECORD ");
            stb.append(" UNION ALL SELECT * FROM STUDY_O ");
            stb.append(" UNION ALL SELECT * FROM QUALIFIED ");
            stb.append(" UNION ALL SELECT * FROM TRANSFER_O ");
            stb.append(" UNION ALL SELECT * FROM TRANSFER_N ");
            stb.append(" ) ");

            stb.append(" , REP_COMBINED_SUBCLASS AS ( ");
            stb.append(" SELECT DISTINCT ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" COMBINED_CLASSCD, ");
                stb.append(" COMBINED_SCHOOL_KIND, ");
                stb.append(" COMBINED_CURRICULUM_CD, ");
            }
            stb.append(" COMBINED_SUBCLASSCD, ");
            stb.append(" MAX(CALCULATE_CREDIT_FLG) AS CALCULATE_CREDIT_FLG ");
            stb.append(" FROM SUBCLASS_REPLACE_COMBINED_DAT T1 ");
            stb.append(" WHERE T1.YEAR = '" + param._year + "' ");
            stb.append(" GROUP BY ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" COMBINED_CLASSCD, ");
                stb.append(" COMBINED_SCHOOL_KIND, ");
                stb.append(" COMBINED_CURRICULUM_CD, ");
            }
            stb.append(" COMBINED_SUBCLASSCD ");
            stb.append(" ) ");
            stb.append(" , REP_ATTEND_SUBCLASS AS ( ");
            stb.append(" SELECT DISTINCT ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" ATTEND_CLASSCD, ");
                stb.append(" ATTEND_SCHOOL_KIND, ");
                stb.append(" ATTEND_CURRICULUM_CD, ");
            }
            stb.append(" ATTEND_SUBCLASSCD, ");
            stb.append(" MAX(CALCULATE_CREDIT_FLG) AS CALCULATE_CREDIT_FLG ");
            stb.append(" FROM SUBCLASS_REPLACE_COMBINED_DAT T1 ");
            stb.append(" WHERE T1.YEAR = '" + param._year + "' ");
            stb.append(" GROUP BY ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" ATTEND_CLASSCD, ");
                stb.append(" ATTEND_SCHOOL_KIND, ");
                stb.append(" ATTEND_CURRICULUM_CD, ");
            }
            stb.append(" ATTEND_SUBCLASSCD ");
            stb.append(" ) ");

            stb.append(" SELECT ");
            stb.append("     T1.SCHOOLCD, ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.CLASSCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
            } else {
                stb.append("     T1.SUBCLASSCD, ");
            }
            stb.append("     T1.SCHREGNO, ");
            stb.append("     VALUE(T2.COMP_ABSENCE_HIGH, 99) AS SCHREG_ABSENCE_HIGH, ");
            stb.append("     RAS.CALCULATE_CREDIT_FLG AS CAL_FLG1_ATTEND_SUBCLASS, ");
            stb.append("     RCS.CALCULATE_CREDIT_FLG AS CAL_FLG2_COMBINED_SUBCLASS, ");
            stb.append("     SUM(T1.CREDIT) AS CREDIT, ");
            stb.append("     SUM(T1.COMP_CREDIT) AS COMP_CREDIT, ");
            stb.append("     SUM(T1.GRAD_VALUE) AS GRAD_VALUE, ");
            stb.append("     SUM(T5.CREDITS) AS CREDIT_MST_CREDIT ");
            stb.append(" FROM ");
            stb.append("     MAIN_T T1");
            stb.append("     LEFT JOIN SCHREG_ABSENCE_HIGH_DAT T2 ON ");
            stb.append("         T1.YEAR = T2.YEAR ");
            stb.append("         AND T1.SCHREGNO = T2.SCHREGNO ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T1.CLASSCD = T2.CLASSCD ");
                stb.append("         AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ");
                stb.append("         AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ");
            }
            stb.append("         AND T1.SUBCLASSCD = T2.SUBCLASSCD ");
            stb.append("         AND T2.DIV = '1' ");
            stb.append("     LEFT JOIN (SELECT SCHREGNO, YEAR, MAX(SEMESTER) AS SEMESTER FROM SCHREG_REGD_DAT WHERE YEAR <= '" + param._year +"' GROUP BY SCHREGNO, YEAR ");
            stb.append("               ) T3 ON T3.SCHREGNO = T1.SCHREGNO AND T3.YEAR = T1.YEAR ");
            stb.append("     LEFT JOIN SCHREG_REGD_DAT T4 ON T4.SCHREGNO = T3.SCHREGNO AND T4.YEAR = T3.YEAR AND T4.SEMESTER = T3.SEMESTER ");
            stb.append("     LEFT JOIN CREDIT_MST T5 ON T5.YEAR = T4.YEAR ");
            stb.append("         AND T5.COURSECD = T4.COURSECD ");
            stb.append("         AND T5.MAJORCD = T4.MAJORCD ");
            stb.append("         AND T5.GRADE = T4.GRADE ");
            stb.append("         AND T5.COURSECODE = T4.COURSECODE ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T5.CLASSCD = T1.CLASSCD ");
                stb.append("         AND T5.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("         AND T5.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("         AND T5.SUBCLASSCD = T1.SUBCLASSCD ");

            stb.append("     LEFT JOIN REP_ATTEND_SUBCLASS RAS ON RAS.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND RAS.ATTEND_CLASSCD = T1.CLASSCD ");
                stb.append("         AND RAS.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("         AND RAS.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("         AND RAS.CALCULATE_CREDIT_FLG = '1' ");
            stb.append("     LEFT JOIN REP_COMBINED_SUBCLASS RCS ON RCS.COMBINED_SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND RCS.COMBINED_CLASSCD = T1.CLASSCD ");
                stb.append("         AND RCS.COMBINED_SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("         AND RCS.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("         AND RCS.CALCULATE_CREDIT_FLG = '2' ");

            stb.append(" GROUP BY ");
            stb.append("     T1.SCHOOLCD, ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.CLASSCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD, ");
            } else {
                stb.append("     T1.SUBCLASSCD, ");
            }
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T2.COMP_ABSENCE_HIGH, ");
            stb.append("     RAS.CALCULATE_CREDIT_FLG, ");
            stb.append("     RCS.CALCULATE_CREDIT_FLG ");
            stb.append(" ORDER BY ");
            stb.append("     YEAR, ");
            stb.append("     CLASSCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ");
            } else {
                stb.append("     T1.SUBCLASSCD ");
            }

            return stb.toString();
        }

        public double getGradValueAvg(int scale, int roundingMode) {
            final String creditAvg = String.valueOf(getGradValueAvg());
            final BigDecimal wrk = new BigDecimal(creditAvg).setScale(scale, roundingMode);
            return wrk.doubleValue();
        }

        /**
         * 評定平均を得る。
         * @return 評定平均
         */
        public double getGradValueAvg() {
            int sum = 0;
            int count = 0;
            for (final PrintData printData : _printDataMap.values()) {
                if (null == printData._gradValue || printData._gradValue.intValue() <= 0) {
                    continue;
                }
                sum += printData._gradValue.intValue();
                count++;
            }
            if (0 == count) {
                return 0;
            }
            return sum / (double) count;
        }

        public static void setAttendSubclass(final DB2UDB db2, final Param param, final Map<String, Student> studentMap) throws SQLException {

            if (PRGID_KNJD292W.equals(param._prgid)) {
                final String hrAttendSql = getHrAttendSql(param);
                if (param._isOutputDebug) {
                    log.info(" hrAttendSql = " + hrAttendSql);
                }
                for (final Map row : KnjDbUtils.query(db2, hrAttendSql)) {
                    final Student student = studentMap.get(KnjDbUtils.getString(row, "SCHREGNO"));
                    if (null == student) {
                        continue;
                    }
                    student._hrAttendDateList.add(row);
                }

                final String spActAttendSql = getSpecialAttendSql(param);
                if (param._isOutputDebug) {
                    log.info(" spActAttendSql = " + spActAttendSql);
                }
                for (final Map row : KnjDbUtils.query(db2, spActAttendSql)) {
                    final Student student = studentMap.get(KnjDbUtils.getString(row, "SCHREGNO"));
                    if (null == student) {
                        continue;
                    }
                    student._spActAttend = KnjDbUtils.getString(row, "SPECIALJISUU");
                }
            } else {
                ResultSet rs = null;
                final Map<String, PreparedStatement> psMap = new HashMap();
                final Integer zero = new Integer(0);

                try {
                    for (final Student student : studentMap.values()) {
                        //log.debug(" set attendance student = " + student._schregno);

                        for (final RegdDat regdDat : student._regdMap.values()) {

                            final String year = regdDat._year;
                            final String semester = regdDat._semester;
                            final String date = year.equals(param._year) ? param._date : regdDat._eDate;

                            final String psKey = "PSKEY" + year + date;
                            if (null == psMap.get(psKey)) {
                                final Map paramMap = new HashMap(param._attendParamMap);
                                paramMap.put("schregno", "?");
                                final String attendSql = AttendAccumulate.getAttendSubclassAbsenceSql(
                                        year,
                                        semester,
                                        null,
                                        date,
                                        paramMap);

                                psMap.put(psKey, db2.prepareStatement(attendSql));
                            }

                            PreparedStatement ps = psMap.get(psKey);
                            ps.setString(1, student._schregno);
                            rs = ps.executeQuery();
                            while (rs.next()) {
                                final String subclassCd = rs.getString("SUBCLASSCD");
                                final String classCd = "1".equals(param._useCurriculumcd) ? subclassCd.substring(7, 9) : subclassCd.substring(0, 2);
                                final String key = PrintData.key(PrintData.SCHOOLCD0, year, classCd, subclassCd);

                                if (!student._printDataMap.containsKey(key)) {
                                    final PrintData printData = new PrintData(PrintData.SCHOOLCD0, year, classCd, subclassCd, zero, zero, zero, zero, new Double(0), false, false);
                                    printData._isFromAttend = true;
                                    student._printDataMap.put(key, printData);
                                }

                                if (ATTEND_OBJ_SEM.equals(rs.getString("SEMESTER"))) {
                                    final PrintData printData = student._printDataMap.get(key);
                                    printData._kekka = new Double(rs.getInt("ABSENT_SEM"));
                                }
                            }
                        }
                    }
                } finally {
                    db2.commit();
                    DbUtils.closeQuietly(rs);
                }

                for (final PreparedStatement ps : psMap.values()) {
                    DbUtils.closeQuietly(ps);
                }
            }
        }

        private static List<Student> getStudentList(final DB2UDB db2, final Param param) throws SQLException {
            final List<Student> rtn = new ArrayList();
            final Map<String, Student> studentMap = new HashMap<String, Student>();
            PreparedStatement ps = null;
            try {
                final String studentSql = getStudentSql(param);
                ps = db2.prepareStatement(studentSql);

                for (int i = 0; i < param._classSelected.length; i++) {
                    final String schregno = param._classSelected[i];

                    String attendNo = "";
                    String name = "";
                    final Map regdMap = new HashMap();
                    for (final Map<String, String> row : KnjDbUtils.query(db2, ps, new Object[] { schregno })) {

                        attendNo = KnjDbUtils.getString(row, "ATTENDNO");
                        name = KnjDbUtils.getString(row, "NAME");

                        final String regdYear = KnjDbUtils.getString(row, "YEAR");
                        final String regdSeme = KnjDbUtils.getString(row, "SEMESTER");
                        final String grade = KnjDbUtils.getString(row, "GRADE");
                        final String hrClass = KnjDbUtils.getString(row, "HR_CLASS");
                        final String courseCd = KnjDbUtils.getString(row, "COURSECD");
                        final String majorCd = KnjDbUtils.getString(row, "MAJORCD");
                        final String courseCode = KnjDbUtils.getString(row, "COURSECODE");
                        final String sDate = KnjDbUtils.getString(row, "SDATE");
                        final String eDate = KnjDbUtils.getString(row, "EDATE");

                        final RegdDat regdDat = new RegdDat(regdYear, regdSeme, grade, hrClass, attendNo, courseCd, majorCd, courseCode, sDate, eDate);
                        regdMap.put(regdYear, regdDat);
                    }

                    Student student = new Student(schregno, attendNo, name, regdMap);
                    rtn.add(student);

                    studentMap.put(schregno, student);
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(ps);
            }

            Student.setPrintData(db2, param, studentMap.values());
            Student.setAttendSubclass(db2, param, studentMap);
            return rtn;
        }

        private static String getStudentSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCHREGNOS (SCHREGNO) AS ( VALUES CAST(? AS VARCHAR(8)) ");
            stb.append(" ), REGD_T AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.SEMESTER ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1 ");
            stb.append("     INNER JOIN SCHREGNOS T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + param._regdSemester + "' ");
            if (!"2".equals(param._yearDiv)) {
                stb.append(" UNION ");
                stb.append(" SELECT ");
                stb.append("     T1.YEAR, ");
                stb.append("     T1.SCHREGNO, ");
                stb.append("     MAX(T1.SEMESTER) AS SEMESTER ");
                stb.append(" FROM ");
                stb.append("     SCHREG_REGD_DAT T1 ");
                stb.append("     INNER JOIN SCHREGNOS T2 ON T2.SCHREGNO = T1.SCHREGNO ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR < '" + param._year + "' ");
                stb.append(" GROUP BY ");
                stb.append("     T1.YEAR, ");
                stb.append("     T1.SCHREGNO ");
            }
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.ATTENDNO, ");
            stb.append("     T1.COURSECD, ");
            stb.append("     T1.MAJORCD, ");
            stb.append("     T1.COURSECODE, ");
            stb.append("     L1.NAME, ");
            stb.append("     L2.SDATE, ");
            stb.append("     L2.EDATE ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1 ");
            stb.append("     LEFT JOIN SCHREG_BASE_MST L1 ON T1.SCHREGNO = L1.SCHREGNO ");
            stb.append("     LEFT JOIN V_SEMESTER_GRADE_MST L2 ON T1.YEAR = L2.YEAR ");
            stb.append("          AND T1.SEMESTER = L2.SEMESTER ");
            stb.append("          AND T1.GRADE = L2.GRADE ");
            stb.append(" WHERE ");
            stb.append("     EXISTS( ");
            stb.append("        SELECT ");
            stb.append("            'x' ");
            stb.append("        FROM ");
            stb.append("            REGD_T E1 ");
            stb.append("        WHERE ");
            stb.append("            T1.YEAR = E1.YEAR ");
            stb.append("            AND T1.SEMESTER = E1.SEMESTER ");
            stb.append("            AND T1.SCHREGNO = E1.SCHREGNO ");
            stb.append("     ) ");
            stb.append(" ORDER BY ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.ATTENDNO ");

            return stb.toString();
        }

        private static String getSpecialAttendSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SPECIAL_ATTEND AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.SCHREGNO,  ");
            stb.append("         SUM(CAST(T1.CREDIT_TIME AS DECIMAL(5, 1))) AS SPECIALJISUU ");
            stb.append("     FROM ");
            stb.append("         SPECIALACT_ATTEND_DAT T1 ");
            stb.append("     INNER JOIN V_NAME_MST M027 ");
            stb.append("          ON M027.NAME1   = T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ");
            stb.append("         AND M027.NAMECD1 = 'M027' ");
            stb.append("         AND M027.YEAR = T1.YEAR ");
            stb.append("     WHERE ");
            stb.append("         (T1.YEAR = '" + param._year + "' AND T1.ATTENDDATE <= '" + param._date + "' ");
            if ("1".equals(param._yearDiv)) {
                stb.append("         OR T1.YEAR < '" + param._year + "' ");
            }
            stb.append("         ) ");
            stb.append("          AND NOT EXISTS(SELECT 'X' FROM V_NAME_MST M026 ");
            stb.append("                         WHERE M026.NAME1      = T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ");
            stb.append("                           AND M026.YEAR       = T1.YEAR ");
            stb.append("                           AND M026.NAMECD1    = 'M026' ");
            stb.append("                           AND M026.NAMESPARE2 = '1' ) ");
            stb.append("     GROUP BY ");
            stb.append("         T1.SCHREGNO ");
            stb.append(" ) ");
            stb.append("     SELECT ");
            stb.append("         BASE.SCHREGNO, ");
            stb.append("         SPAT0.SPECIALJISUU ");
            stb.append("     FROM ");
            stb.append("         SCHREG_BASE_MST BASE ");
            stb.append("     INNER JOIN SPECIAL_ATTEND SPAT0 ON SPAT0.SCHREGNO = BASE.SCHREGNO ");
            stb.append("     WHERE ");
            stb.append("         BASE.SCHREGNO IN " + SQLUtils.whereIn(true, param._classSelected) + " ");
            stb.append(" ORDER BY ");
            stb.append("     BASE.SCHREGNO ");
            return stb.toString();
        }

        private static String getHrAttendSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCHREGNOS (SCHREGNO) AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.SCHREGNO ");
            stb.append("     FROM SCHREG_BASE_MST T1 ");
            stb.append("     WHERE T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._classSelected) + " ");
            stb.append(" ) ");
//            stb.append(" SELECT ");
//            stb.append("     BASE.SCHREGNO, ");
//            stb.append("     CASE WHEN T2.SCHREGNO IS NOT NULL THEN 'SCHOOLING' END AS TABLEDIV, ");
//            stb.append("     T2.YEAR, ");
//            stb.append("     T2.EXECUTEDATE, ");
//            stb.append("     T5.SEMESTER ");
//            stb.append("   , CHAIR.CLASSCD ");
//            stb.append("   , CHAIR.CLASSCD || '-' || CHAIR.SCHOOL_KIND || '-' || CHAIR.CURRICULUM_CD || '-' || CHAIR.SUBCLASSCD AS SUBCLASSCD ");
//            stb.append("   , SUBM.SUBCLASSNAME ");
//            stb.append("   , T2.CREDIT_TIME ");
//            stb.append("   , T9.NAMESPARE1 ");
//            stb.append("   , NM_M026.NAMESPARE1 AS M026_NAMESPARE1 ");
//            stb.append("   , NM_M026.NAMESPARE2 AS M026_NAMESPARE2 ");
//            stb.append("   , NM_M027.NAME1 AS M027_NAME1");
//            stb.append(" FROM SCHREG_BASE_MST BASE ");
//            stb.append(" INNER JOIN SCHREGNOS S1 ON S1.SCHREGNO = BASE.SCHREGNO ");
//            stb.append(" LEFT JOIN SCH_ATTEND_DAT T2 ON T2.SCHREGNO = BASE.SCHREGNO ");
//            stb.append(" LEFT JOIN SEMESTER_MST T5 ON T5.YEAR = T2.YEAR ");
//            stb.append("     AND T5.SEMESTER <> '9' ");
//            stb.append("     AND T2.EXECUTEDATE BETWEEN T5.SDATE AND T5.EDATE ");
//            stb.append(" LEFT JOIN CHAIR_DAT CHAIR ON CHAIR.CHAIRCD = T2.CHAIRCD ");
//            stb.append("     AND CHAIR.YEAR = T2.YEAR ");
//            stb.append("     AND CHAIR.SEMESTER = T5.SEMESTER ");
//            stb.append(" LEFT JOIN SUBCLASS_MST SUBM ON SUBM.CLASSCD = CHAIR.CLASSCD ");
//            stb.append("     AND SUBM.SCHOOL_KIND = CHAIR.SCHOOL_KIND ");
//            stb.append("     AND SUBM.CURRICULUM_CD = CHAIR.CURRICULUM_CD ");
//            stb.append("     AND SUBM.SUBCLASSCD = CHAIR.SUBCLASSCD ");
//            stb.append(" LEFT JOIN NAME_MST T9 ON T9.NAMECD1 = 'M001' ");
//            stb.append("     AND T9.NAMECD2 = T2.SCHOOLINGKINDCD ");
//            stb.append(" LEFT JOIN V_NAME_MST NM_M026 ON NM_M026.YEAR = T2.YEAR ");
//            stb.append("     AND NM_M026.NAMECD1 = 'M026' ");
//            stb.append("     AND NM_M026.NAME1 = CHAIR.CLASSCD || '-' || CHAIR.SCHOOL_KIND || '-' || CHAIR.CURRICULUM_CD || '-' || CHAIR.SUBCLASSCD ");
//            stb.append(" LEFT JOIN V_NAME_MST NM_M027 ON NM_M027.YEAR = T2.YEAR ");
//            stb.append("     AND NM_M027.NAMECD1 = 'M027' ");
//            stb.append("     AND NM_M027.NAME1 = CHAIR.CLASSCD || '-' || CHAIR.SCHOOL_KIND || '-' || CHAIR.CURRICULUM_CD || '-' || CHAIR.SUBCLASSCD ");
//            stb.append(" WHERE ");
//            stb.append("     T2.EXECUTEDATE <= '" + param._date + "' ");
//            if ("2".equals(param._yearDiv)) {
//                stb.append("     AND T2.YEAR = '" + param._year + "' ");
//            }
//            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     BASE.SCHREGNO, ");
            stb.append("     CASE WHEN T2.SCHREGNO IS NOT NULL THEN 'SPECIAL' END AS TABLEDIV, ");
            stb.append("     T2.YEAR, ");
            stb.append("     T2.ATTENDDATE AS EXECUTEDATE, ");
            stb.append("     T2.SEMESTER ");
            stb.append("   , T2.CLASSCD ");
            stb.append("   , CHAIR.CLASSCD || '-' || CHAIR.SCHOOL_KIND || '-' || CHAIR.CURRICULUM_CD || '-' || CHAIR.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("   , SUBM.SUBCLASSNAME ");
            stb.append("   , T2.CREDIT_TIME ");
            stb.append("   , CAST(NULL AS VARCHAR(1)) AS NAMESPARE1 ");
            stb.append("   , NM_M026.NAMESPARE1 AS M026_NAMESPARE1 ");
            stb.append("   , NM_M026.NAMESPARE2 AS M026_NAMESPARE2 ");
            stb.append("   , NM_M027.NAME1 AS M027_NAME1");
            stb.append(" FROM SCHREG_BASE_MST BASE ");
            stb.append(" INNER JOIN SCHREGNOS S1 ON S1.SCHREGNO = BASE.SCHREGNO ");
            stb.append(" LEFT JOIN SPECIALACT_ATTEND_DAT T2 ON T2.SCHREGNO = BASE.SCHREGNO ");
            stb.append(" LEFT JOIN CHAIR_DAT CHAIR ON CHAIR.CHAIRCD = T2.CHAIRCD ");
            stb.append("     AND CHAIR.YEAR = T2.YEAR ");
            stb.append("     AND CHAIR.SEMESTER = T2.SEMESTER ");
            stb.append(" LEFT JOIN SUBCLASS_MST SUBM ON SUBM.CLASSCD = CHAIR.CLASSCD ");
            stb.append("     AND SUBM.SCHOOL_KIND = CHAIR.SCHOOL_KIND ");
            stb.append("     AND SUBM.CURRICULUM_CD = CHAIR.CURRICULUM_CD ");
            stb.append("     AND SUBM.SUBCLASSCD = CHAIR.SUBCLASSCD ");
            stb.append(" LEFT JOIN V_NAME_MST NM_M026 ON NM_M026.YEAR = T2.YEAR ");
            stb.append("     AND NM_M026.NAMECD1 = 'M026' ");
            stb.append("     AND NM_M026.NAME1 = T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD ");
            stb.append(" LEFT JOIN V_NAME_MST NM_M027 ON NM_M027.YEAR = T2.YEAR ");
            stb.append("     AND NM_M027.NAMECD1 = 'M027' ");
            stb.append("     AND NM_M027.NAME1 = T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     T2.ATTENDDATE <= '" + param._date + "' ");
            if ("2".equals(param._yearDiv)) {
                stb.append("     AND T2.YEAR = '" + param._year + "' ");
            }
//            stb.append(" UNION ALL ");
//            stb.append(" SELECT ");
//            stb.append("     BASE.SCHREGNO, ");
//            stb.append("     CASE WHEN T2.SCHREGNO IS NOT NULL THEN 'TEST' END AS TABLEDIV, ");
//            stb.append("     T2.YEAR, ");
//            stb.append("     T2.INPUT_DATE AS EXECUTEDATE, ");
//            stb.append("     T2.SEMESTER ");
//            stb.append("   , T2.CLASSCD ");
//            stb.append("   , T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD AS SUBCLASSCD ");
//            stb.append("   , SUBM.SUBCLASSNAME ");
//            stb.append("   , CAST(NULL AS DECIMAL(5, 1)) AS CREDIT_TIME ");
//            stb.append("   , CAST(NULL AS VARCHAR(1)) AS NAMESPARE1 ");
//            stb.append("   , NM_M026.NAMESPARE1 AS M026_NAMESPARE1 ");
//            stb.append("   , CAST(NULL AS VARCHAR(1)) AS M026_NAMESPARE2 ");
//            stb.append("   , CAST(NULL AS VARCHAR(1)) AS M027_NAME1");
//            stb.append(" FROM SCHREG_BASE_MST BASE ");
//            stb.append(" INNER JOIN SCHREGNOS S1 ON S1.SCHREGNO = BASE.SCHREGNO ");
//            stb.append(" LEFT JOIN TEST_ATTEND_DAT T2 ON T2.SCHREGNO = BASE.SCHREGNO ");
//            stb.append(" LEFT JOIN SUBCLASS_MST SUBM ON SUBM.CLASSCD = T2.CLASSCD ");
//            stb.append("     AND SUBM.SCHOOL_KIND = T2.SCHOOL_KIND ");
//            stb.append("     AND SUBM.CURRICULUM_CD = T2.CURRICULUM_CD ");
//            stb.append("     AND SUBM.SUBCLASSCD = T2.SUBCLASSCD ");
//            stb.append(" LEFT JOIN V_NAME_MST NM_M026 ON NM_M026.YEAR = T2.YEAR ");
//            stb.append("     AND NM_M026.NAMECD1 = 'M026' ");
//            stb.append("     AND NM_M026.NAME1 = T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD ");
//            stb.append(" WHERE ");
//            stb.append("     T2.INPUT_DATE <= '" + param._date + "' ");
//            if ("2".equals(param._yearDiv)) {
//                stb.append("     AND T2.YEAR = '" + param._year + "' ");
//            }
            return stb.toString();
        }

        public String toString() {
            return _attendNo + "番：" + _name;
        }
    }

    private static class RegdDat {
        final String _year;
        final String _semester;
        final String _grade;
        final String _hrClass;
        final String _attendNo;
        final String _courseCd;
        final String _majorCd;
        final String _courseCode;
        final String _sDate;
        final String _eDate;

        public RegdDat(
                final String year,
                final String semester,
                final String grade,
                final String hrClass,
                final String attendNo,
                final String courseCd,
                final String majorCd,
                final String courseCode,
                final String sDate,
                final String eDate
        ) {
            _year = year;
            _semester = semester;
            _grade = grade;
            _hrClass = hrClass;
            _attendNo = attendNo;
            _courseCd = courseCd;
            _majorCd = majorCd;
            _courseCode = courseCode;
            _sDate = sDate;
            _eDate = eDate;
        }

        public String toString() {
            return "年度：" + _year +
                    " クラス：" + _grade + "年 " + _hrClass + "組" + _attendNo + "番";
        }
    }

    private static class PrintData {
        private static String SCHOOLCD0 = "0";
        private static String SCHOOLCD1 = "1";
        /**
         * SCHREG_STUDYREC_DAT.SCHOOLCD 学校区分 0:本校、1:前籍校、2:大検
         */
        final String _schoolcd;
        final String _year;
        final String _classcd;
        final String _subclasscd;
        final Integer _credit;
        final Integer _gradValue;
        final Integer _compCredit;
        final Integer _creditMstCredit;
        final boolean _isCalFlg1AttendSubclass;
        final boolean _isCalFlg2CombinedSubclass;
        Double _kekka;
        boolean _isFromAttend;

        public PrintData(
                final String schoolcd,
                final String year,
                final String classcd,
                final String subclasscd,
                final Integer credit,
                final Integer gradValue,
                final Integer compCredit,
                final Integer creditMstCredit,
                final Double kekka,
                final boolean isCalFlg1AttendSubclass,
                final boolean isCalFlg2CombinedSubclass
        ) {
            _schoolcd = schoolcd;
            _year = year;
            _classcd = classcd;
            _subclasscd = subclasscd;
            _credit = credit;
            _gradValue = gradValue;
            _compCredit = compCredit;
            _creditMstCredit = creditMstCredit;
            _kekka = kekka;
            _isCalFlg1AttendSubclass = isCalFlg1AttendSubclass;
            _isCalFlg2CombinedSubclass = isCalFlg2CombinedSubclass;
        }

        public boolean isRishutyu(final Param param) {
            return !param._abroadSubclasscd.equals(_subclasscd) && param._year.equals(_year) && !(PRGID_KNJD292W.equals(param._prgid) && null != _gradValue);
        }

        boolean isMirishu(final Param param) {
            return !param._abroadSubclasscd.equals(_subclasscd) && !param._year.equals(_year) && (null == _compCredit || _compCredit.intValue() == 0) && !_isFromAttend;
        }

        /**
         * @param schoolcd 学校区分 0:本校、1:前籍校、2:大検
         * @param year
         * @param classcd
         * @param subclasscd
         * @return
         */
        static String key(final String schoolcd, final String year, final String classcd, final String subclasscd) {
            return schoolcd + year + classcd + subclasscd;
        }

        public String toString() {
            return "{ year = " + _year + ", subclassCd = " + _subclasscd + ", credit = " + _credit + ", compCredit = " + _compCredit + ", creditMstCredit = " + _creditMstCredit + "}";
        }
    }

    private static class Subclass implements Comparable<Subclass> {
        /**
         * SCHREG_STUDYREC_DAT.SCHOOLCD 学校区分 0:本校、1:前籍校、2:大検
         */
        final String _schoolcd;
        final String _year;
        final String _classcd;
        final String _subclasscd;
        String _className;
        String _classAbbv;
        String _subclassName;
        String _subclassAbbv;
        TreeSet<Integer> _creditSet = new TreeSet();
        TreeSet<Integer> _abHighSet = new TreeSet();
        Map<String, CreditMst> _creditMst = new HashMap();

        public Subclass(
                final String schoolcd,
                final String year,
                final String classcd,
                final String subclasscd) {
            _schoolcd = schoolcd;
            _year = year;
            _classcd = classcd;
            _subclasscd = subclasscd;
        }

        private static void setSubclassInfo(final DB2UDB db2, final Param param, final List<Student> studentAllList, final Map<String, Subclass> subclassMap) throws SQLException {

            final Map<String, String> subclassNameMap = new HashMap();
            final Map<String, String> subclassAbbvMap = new HashMap();
            //科目マスタ
            final String subclassSql = "SELECT * FROM SUBCLASS_MST ";
            for (final Map<String, String> row : KnjDbUtils.query(db2, subclassSql)) {
                final String subclasscd = "1".equals(param._useCurriculumcd) ? KnjDbUtils.getString(row, "CLASSCD") + "-" + KnjDbUtils.getString(row, "SCHOOL_KIND") + "-" + KnjDbUtils.getString(row, "CURRICULUM_CD") + "-" + KnjDbUtils.getString(row, "SUBCLASSCD") : KnjDbUtils.getString(row, "SUBCLASSCD");
                subclassNameMap.put(subclasscd, KnjDbUtils.getString(row, "SUBCLASSNAME"));
                subclassAbbvMap.put(subclasscd, KnjDbUtils.getString(row, "SUBCLASSABBV"));
            }

            final Map<String, String> classNameMap = new HashMap();
            final Map<String, String> classAbbvMap = new HashMap();
            //教科マスタ
            final String classSql = "SELECT * FROM CLASS_MST ";
            for (final Map<String, String> row : KnjDbUtils.query(db2, classSql)) {
                final String classcd = "1".equals(param._useCurriculumcd) ? KnjDbUtils.getString(row, "CLASSCD") + "-" + KnjDbUtils.getString(row, "SCHOOL_KIND") : KnjDbUtils.getString(row, "CLASSCD");
                classNameMap.put(classcd, KnjDbUtils.getString(row, "CLASSNAME"));
                classAbbvMap.put(classcd, KnjDbUtils.getString(row, "CLASSABBV"));
            }

            for (final Subclass subclass : subclassMap.values()) {
                //留学は固定
                if (subclass._subclasscd.equals(param._abroadSubclasscd)) {
                    subclass._subclassName = "留学";
                    subclass._subclassAbbv = "留学";
                    subclass._className = "留学";
                    subclass._classAbbv = "留学";
                    continue;
                }

                subclass._subclassName = subclassNameMap.get(subclass._subclasscd);
                subclass._subclassAbbv = subclassAbbvMap.get(subclass._subclasscd);
                final String classKey = "1".equals(param._useCurriculumcd) ? subclass._classcd + subclass._subclasscd.substring(2, 4) : subclass._classcd;
                subclass._className = classNameMap.get(classKey);
                subclass._classAbbv = classAbbvMap.get(classKey);
            }

            final Set<String> schoolcd1SubclassSet = new TreeSet<String>();
            for (final Subclass subclass : subclassMap.values()) {
                if (PrintData.SCHOOLCD1.equals(subclass._schoolcd)) {
                    schoolcd1SubclassSet.add(subclass._subclasscd);
                }
            }
            if (!schoolcd1SubclassSet.isEmpty()) {
                final Map<String, String> anClassNameMap = new HashMap();
                final Map<String, String> anClassAbbvMap = new HashMap();
                if (param._hasANOTHER_CLASS_MST) {
                    for (final Subclass subclass : subclassMap.values()) {
                        if (PrintData.SCHOOLCD1.equals(subclass._schoolcd)) {
                            final String[] split = StringUtils.split(subclass._subclasscd, "-");
                            final String classKey = split[0] + "-" + split[1];
                            if (!anClassNameMap.containsKey(classKey) && param._hasANOTHER_CLASS_MST) {
                                final Map<String, String> row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, " SELECT CLASSNAME, CLASSABBV FROM ANOTHER_CLASS_MST WHERE CLASSCD = ? AND SCHOOL_KIND = ? ", new Object[] {split[0], split[1]}));
                                anClassNameMap.put(classKey, KnjDbUtils.getString(row, "CLASSNAME"));
                                anClassAbbvMap.put(classKey, KnjDbUtils.getString(row, "CLASSABBV"));
                            }
                            if (null != anClassNameMap.get(classKey)) {
                                subclass._className = anClassNameMap.get(classKey);
                            }
                            if (null != anClassAbbvMap.get(classKey)) {
                                subclass._classAbbv = anClassAbbvMap.get(classKey);
                            }
                        }
                    }
                }
                if (param._hasANOTHER_SUBCLASS_MST) {
                    final Map<String, String> anSubclassNameMap = new HashMap();
                    final Map<String, String> anSubclassAbbvMap = new HashMap();
                    for (final Subclass subclass : subclassMap.values()) {
                        if (PrintData.SCHOOLCD1.equals(subclass._schoolcd)) {
                            final String[] split = StringUtils.split(subclass._subclasscd, "-");
                            final String subclassKey = split[0] + "-" + split[1] + "-" + split[2] + "-" + split[3];
                            if (!anSubclassNameMap.containsKey(subclassKey) && param._hasANOTHER_SUBCLASS_MST) {
                                final Map<String, String> row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, " SELECT SUBCLASSNAME, SUBCLASSABBV FROM ANOTHER_SUBCLASS_MST WHERE CLASSCD = ? AND SCHOOL_KIND = ? AND CURRICULUM_CD = ? AND SUBCLASSCD = ? ", split));
                                anSubclassNameMap.put(subclassKey, KnjDbUtils.getString(row, "SUBCLASSNAME"));
                                anSubclassAbbvMap.put(subclassKey, KnjDbUtils.getString(row, "SUBCLASSABBV"));
                            }
                            if (null != anSubclassNameMap.get(subclassKey)) {
                                subclass._subclassName = anSubclassNameMap.get(subclassKey);
                            }
                            if (null != anSubclassAbbvMap.get(subclassKey)) {
                                subclass._subclassAbbv = anSubclassAbbvMap.get(subclassKey);
                            }
                        }
                    }
                }
            }

            PreparedStatement ps = null;
            try {
                final Set<String> yearSet = new TreeSet<String>();
                for (final Student student : studentAllList) {
                    for (final RegdDat regdDat : student._regdMap.values()) {
                        yearSet.add(regdDat._year);
                    }
                }
                final String creditSql = getCreditMstSql(param);
                ps = db2.prepareStatement(creditSql);

                //単位マスタ
                for (final String year : yearSet) {

                    for (final Map<String, String> row : KnjDbUtils.query(db2, ps, new Object[] {year})) {
                        final String classcd = KnjDbUtils.getString(row, "CLASSCD");
                        final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                        final String subclasskey = PrintData.key(PrintData.SCHOOLCD0, year, classcd, subclasscd);
                        final Subclass subclass = subclassMap.get(subclasskey);
                        if (null == subclass) {
                            continue;
                        }

                        final String key = KnjDbUtils.getString(row, "KEY");
                        if (!subclass._creditMst.containsKey(key)) {
                            final Integer credit = KnjDbUtils.getInt(row, "CREDITS", 0);
                            final Integer absenHigh = KnjDbUtils.getInt(row, "ABSENCE_HIGH", 0);
                            subclass._creditSet.add(credit);
                            subclass._abHighSet.add(absenHigh);
                            subclass._creditMst.put(key, new CreditMst(credit, absenHigh));
                        }
                    }
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(ps);
            }
        }

        public String getNendo(final DB2UDB db2) {
            if (!NumberUtils.isDigits(_year)) {
                return "";
            }
            final String dummyNendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_year)) + "年12月31日";
            final String nendo[] = KNJ_EditDate.tate2_format(dummyNendo);
            return nendo[1];
        }

        public String getAbsenceHigh() {
            final TreeSet<Integer> s = _abHighSet;
            if (s.isEmpty()) {
                return "";
            }
            if (s.size() == 1) {
                return s.last().toString();
            } else {
                return s.first().toString() + "\uFF5E" + s.last().toString();
            }
        }

        public String getCredit(final Param param, final List<Student> studentList) {
            final TreeSet<Integer> s = _creditSet;
            final boolean isCheckStudentCredit = PRGID_KNJD292W.equals(param._prgid);
            if (s.isEmpty()) {
                if (isCheckStudentCredit) {
                    final String subKey = PrintData.key(_schoolcd, _year, _classcd, _subclasscd);
                    for (final Student student : studentList) {
                        final PrintData printData = student._printDataMap.get(subKey);
                        if (null != printData && null != printData._credit) {
                            s.add(printData._credit);
                        }
                    }
                    if (s.isEmpty()) {
                        return "";
                    }
                    if (s.size() > 1) {
                        log.info(" 生徒の単位 : " + s);
                    }
                } else {
                    return "";
                }
            }
            if (s.size() == 1) {
                return s.last().toString();
            } else {
                return s.first().toString() + "\uFF5E" + s.last().toString();
            }
        }

        private static String getCreditMstSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     YEAR || COURSECD || MAJORCD || GRADE || COURSECODE AS KEY ");
            stb.append("     , CLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     , CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD ");
            } else {
                stb.append("     , SUBCLASSCD ");
            }
            stb.append("     , VALUE(CREDITS, 0) AS CREDITS ");
            stb.append("     , VALUE(ABSENCE_HIGH, 0) AS ABSENCE_HIGH ");
            stb.append(" FROM ");
            stb.append("     CREDIT_MST ");
            stb.append(" WHERE ");
            stb.append("     YEAR = ? ");

            return stb.toString();
        }

        public int compareTo(final Subclass that) {
            int cmp = 0;
            if (cmp == 0) {
                cmp = this._subclasscd.compareTo(that._subclasscd);
            }
            if (cmp == 0) {
                cmp = this._year.compareTo(that._year);
            }
            return cmp;
        }

        public String toString() {
            return _subclasscd + " 科目名：" + _subclassName + " 単位：" + _creditSet
                    + " 欠時：" + _abHighSet
                    + " 単位マスタ：" + _creditMst.size();
        }
    }

    private static class CreditMst {
        final int _credit;
        final int _absenHigh;

        public CreditMst(
                final int credit,
                final int absenHigh
        ) {
            _credit = credit;
            _absenHigh = absenHigh;
        }
    }

}
 // KNJD292

// eof
