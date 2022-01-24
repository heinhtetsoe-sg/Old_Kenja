/*
 * $Id: 7ad848bde9b3d0e815efd1ee0489672c79347d0f $
 *
 * 作成日: 2013/07/25
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;


import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 学校教育システム 賢者 [成績管理]  成績判定会議資料
 */

public class KNJD234U {

    private static final Log log = LogFactory.getLog(KNJD234U.class);

    private static final String SEMEALL = "9";
    private static final String SUBCLASSCD999999 = "999999";
    private static final String COURSEALL = "ALL";

    private static final String RECORD1 = "RECORD1";
    private static final String RECORD1_TAB1 = "RECORD1_TAB1";
    private static final String RECORD1_TAB1_LINE = "RECORD1_TAB1_LINE";
    private static final String RECORD1_TAB2 = "RECORD1_TAB2";
    private static final String RECORD1_TAB2_LINE = "RECORD1_TAB2_LINE";
    private static final String RECORD1_TAB3 = "RECORD1_TAB3";

    private final int DIV_2_I_SEISEKI_YURYOSYA = 21;
    private final int DIV_2_RO_6KANEN_KAIKIN = 22;
    private final int DIV_2_HA_3KANEN_KAIKIN = 23;
    private final int DIV_2_NI_1KANEN_KAIKIN = 24;

    private static final int DIV_3_I_KEKKSEKIGOKEI = 31;
    private static final int DIV_3_RO_NENKANHEIKIN_TOTAL = 32;
    private static final int DIV_3_HA_SHUTOKU_FUNINTEI = 33;
    private static final int DIV_3_NI_NENKANHEIKIN_KAMOKU = 34;
    private static final int DIV_3_HO_JOKI_1_AND_OTHER_2 = 35;
    private static final int DIV_3_HE_NENKANHEIKIN_TOTAL2 = 36;
    private static final int DIV_3_TO_SHUSSEKISUBEKI = 37;
    private static final int DIV_3_CHI_SHUSSEKISUBEKI2 = 38;
    private static final int DIV_3_SEIKATUSHIDOU = 39;

    private static final String FROM_TO_MARK = "\uFF5E";

    private final int[] DIV3_ARRAY = {
                DIV_3_I_KEKKSEKIGOKEI, DIV_3_RO_NENKANHEIKIN_TOTAL, DIV_3_HA_SHUTOKU_FUNINTEI, DIV_3_NI_NENKANHEIKIN_KAMOKU, DIV_3_HO_JOKI_1_AND_OTHER_2,
                DIV_3_HE_NENKANHEIKIN_TOTAL2, DIV_3_TO_SHUSSEKISUBEKI, DIV_3_CHI_SHUSSEKISUBEKI2, DIV_3_SEIKATUSHIDOU
            };

    private final int TABLE_DIV_SEITO_LIST = 0;
    private final int TABLE_DIV_SEISEKI_SHUKKETSU = 1;
    private final int TABLE_DIV_SHUKKETSU = 2;
    private final int TABLE_DIV_SHIDOU = 3;

    private boolean _hasData;

    private Param _param;

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        Vrw32alp svf = null;
        DB2UDB db2 = null;
        try {
            response.setContentType("application/pdf");

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _hasData = false;

            _param = createParam(db2, request);

            final List studentList = getStudentList(db2, _param);
            final List hrClassList = HrClass.getHrClassList(studentList);
            setData(db2, _param, studentList, hrClassList);

            svf = new Vrw32alp();
            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            printMain(db2, svf, studentList, hrClassList);

        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
            svf.VrQuit();

            if (null != db2) {
                db2.commit();
                db2.close();
            }
        }
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf, final List studentList, final List hrClassList) {
        final Map mappedLineList = getMappedLineList(db2, studentList, hrClassList);

        final List pageList = new ArrayList();

        for (final Iterator it = mappedLineList.keySet().iterator(); it.hasNext();) {
            final Object printDiv = it.next();
            final List lineList = getMappedList(mappedLineList, printDiv);

            pageList.addAll(getPrintLinePageList(lineList));
        }

        final int totalPage = pageList.size();

        final String form = "KNJD234U.frm";

        final String title = _param._nendo + "　" + defstr(_param._a023Abbv1) + Integer.parseInt(_param._gradeCd) + "年　進級判定会議資料";

        for (int pi = 0; pi < pageList.size(); pi++) {
            final List lineList = (List) pageList.get(pi);

            svf.VrSetForm(form, 4);

            svf.VrsOut("PAGE1", String.valueOf(pi + 1)); //
            svf.VrsOut("PAGE2", String.valueOf(totalPage)); //

            svf.VrsOut("TITLE", title); // タイトル
            svf.VrsOut("DATE", _param._printDate); //

            for (int li = 0; li < lineList.size(); li++) {
                final Object o = lineList.get(li);
//                log.info(" " + li + " " + o);
                if (o instanceof Map) {
                    printRecord(svf, (Map) o);
                } else if (o instanceof Table) {
                    Table t = (Table) o;
                    final List recordList = t.toRecordList(true);
                    for (int i = 0; i < recordList.size(); i++) {
                        printRecord(svf, (Map) recordList.get(i));
                    }
                }
            }
            _hasData = true;
        }
    }

    private Map newRecord(final String recordName, final List recordList) {
        final Map record = new HashMap();
        record.put("RECORD", recordName);
        recordList.add(record);
        return record;
    }

    private void printRecord(final Vrw32alp svf, final Map m) {
        for (final Iterator it = m.entrySet().iterator(); it.hasNext();) {
            final Map.Entry e = (Map.Entry) it.next();
            final String field = (String) e.getKey();
            final String value = (String) e.getValue();
            if (null != field) {
                final String[] split = StringUtils.split(field, ",");
                if (split.length > 1) {
                    final String fieldn = split[0];
                    final int line = Integer.parseInt(split[1]);
                    svf.VrsOutn(fieldn, line, value);
                } else {
                    svf.VrsOut(field, value);
                }
            }
        }
        svf.VrEndRecord();
    }

    private class Table {

        final int _tableDiv;
        final String _header;
        final List _studentList;
        final boolean _isAddRow;
        final String _footer;
        String _canncelComment;
        final int _dataDiv;
        Table(final int tableDiv, final String header, final List studentList, final String footer, final boolean isAddRow, final int dataDiv) {
            _tableDiv = tableDiv;
            _header = header;
            _studentList = studentList;
            _isAddRow = isAddRow;
            _footer = footer;
            _dataDiv = dataDiv;
        }

        private List getStudentSubclassList() {
            final Set subclasscdSet = new TreeSet();
            for (final Iterator it = _studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                subclasscdSet.addAll(student._subclassScoreMap.keySet());
            }
            subclasscdSet.remove(SUBCLASSCD999999);
            final List subclasscdList = new ArrayList(subclasscdSet);
            return subclasscdList;
        }

        public List toRecordList(final boolean isLast) {
            final List recordList = new ArrayList();

            newRecord(RECORD1, recordList).put("TEXT1", this._header); // テキスト
            log.info(" dataDiv = " + _dataDiv + " / tableDiv = " + _tableDiv + " / student size = " + _studentList.size());
            switch (_tableDiv) {
            case TABLE_DIV_SEITO_LIST:
                if (null != _canncelComment) {
                    newRecord(RECORD1, recordList).put("TEXT1", _canncelComment); // テキスト
                } else {
                    final Map hrClassStudentListMap = new TreeMap();
                    final Map hrClassHrAbbvMap = new TreeMap();
                    int hrAbbvKetaMax = 0;
                    for (final Iterator it = _studentList.iterator(); it.hasNext();) {
                        final Student student = (Student) it.next();
                        if (null != student._hrClass) {
                            getMappedList(hrClassStudentListMap, student._hrClass).add(student);
                            hrClassHrAbbvMap.put(student._hrClass, student._hrNameabbv);
                            hrAbbvKetaMax = Math.max(hrAbbvKetaMax, getMS932ByteLength(student._hrNameabbv));
                        }
                    }
                    hrAbbvKetaMax += 2;
                    final String header = "　　";
                    for (final Iterator it = hrClassStudentListMap.keySet().iterator(); it.hasNext();) {
                        final String hrClass = (String) it.next();

                        final List hrClassStudentList = getMappedList(hrClassStudentListMap, hrClass);
                        final List lineList = getPageList(hrClassStudentList, 5); // 5人ごと
                        for (int li = 0; li < lineList.size(); li++) {

                            final List lineStudentList = (List) lineList.get(li);
                            final StringBuffer stb = new StringBuffer();
                            for (int j = 0; j < lineStudentList.size(); j++) {
                                final Student student = (Student) lineStudentList.get(j);
                                stb.append(rightPadding(student._name, 24)); // 最小12文字
                            }
                            if (li == 0) {
                                stb.insert(0, header + rightPadding((String) hrClassHrAbbvMap.get(hrClass), hrAbbvKetaMax));
                            } else {
                                stb.insert(0, header + rightPadding("", hrAbbvKetaMax));
                            }
                            newRecord(RECORD1, recordList).put("TEXT1", stb.toString()); // テキスト
                        }
                        newRecord(RECORD1, recordList).put("TEXT1", header + rightPadding("", hrAbbvKetaMax) + "計; " + String.valueOf(hrClassStudentList.size() + "名")); // テキスト
                    }
                }
                break;
            case TABLE_DIV_SEISEKI_SHUKKETSU:
                final Map headerRecord1 = newRecord(RECORD1_TAB1, recordList);
                headerRecord1.put("HANTEI_1", "判定"); // 判定
                headerRecord1.put("HR_CLASS_NAME1_1", "組"); // 組
                headerRecord1.put("NAME_1", "氏名"); // 生徒氏名
                headerRecord1.put("AVG_1", "平均点"); // 平均点
                final List subclasscdList = this.getStudentSubclassList();
                final int maxSubclass = 20;
                for (int j = 0; j < Math.min(subclasscdList.size(), maxSubclass); j++) {
                    final int line = j + 1;
                    final String subclasscd = (String) subclasscdList.get(j);
                    final Subclass subclass = (Subclass) _param._subclassMap.get(subclasscd);
                    if (null != subclass) {
                        headerRecord1.put("SUBCLASS_1," + line, subclass._subclassabbv); // 科目
                    }
                }
                headerRecord1.put("SUBCLASS_1," + 21, "欠席"); // 出欠
                headerRecord1.put("SUBCLASS_1," + 22, "途欠"); // 出欠
                headerRecord1.put("SUBCLASS_1," + 23, "遅刻"); // 出欠
                headerRecord1.put("SUBCLASS_1," + 24, "早退"); // 出欠
                headerRecord1.put("SUBCLASS_1," + 25, "合計"); // 出欠

                for (final Iterator it = _studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    final Map record = newRecord(RECORD1_TAB1, recordList);
                    record.put("HANTEI_1", student.getHantei(_dataDiv)); // 判定
                    record.put("HR_CLASS_NAME1_1", student._hrNameabbv); // 組
                    record.put("NAME_1", student._name); // 生徒氏名
                    record.put("AVG_1", student.getSubclassAvg(SUBCLASSCD999999)); // 平均点

                    for (int j = 0; j < Math.min(subclasscdList.size(), maxSubclass); j++) {
                        final int line = j + 1;
                        final String subclasscd = (String) subclasscdList.get(j);
                        final String avg = student.getSubclassAvg(subclasscd);
                        record.put("SUBCLASS_1," + line, avg); // 科目
                    }
                    if (null != student._attendance) {
                        final Attendance att = student._attendance;
                        record.put("SUBCLASS_1," + 21, String.valueOf(att._absence)); // 出欠
                        record.put("SUBCLASS_1," + 22, String.valueOf(att._tochuKekka)); // 出欠
                        record.put("SUBCLASS_1," + 23, String.valueOf(att._late)); // 出欠
                        record.put("SUBCLASS_1," + 24, String.valueOf(att._early)); // 出欠
                        record.put("SUBCLASS_1," + 25, String.valueOf(att.kekkaGokei())); // 出欠
                    }
                }

                if (_isAddRow) {
                    newRecord(RECORD1_TAB1_LINE, recordList).put("TAB_ROW_DUMMY1", "1"); // テーブルブランク行ダミー
                }

                newRecord(RECORD1_TAB1_LINE, recordList).put("TAB_LINE_DUMMY1", "1"); // テーブル下線ダミー

                newRecord(RECORD1, recordList).put("TEXT1", "　　　　　　　　合計数＝ " + String.valueOf(_studentList.size()) + " 名" + defstr(_footer)); // テキスト
                break;
            case TABLE_DIV_SHUKKETSU:
                final Map headerRecord2 = newRecord(RECORD1_TAB2, recordList);
                headerRecord2.put("HANTEI_3", "判定"); // 判定
                headerRecord2.put("HR_CLASS_NAME1_3", "組"); // 組
                headerRecord2.put("NAME_3", "氏名"); // 生徒氏名
                headerRecord2.put("AVG_3", "平均点"); // 平均点

                headerRecord2.put("SUBCLASS_3," + 1, "欠席"); // 出欠
                headerRecord2.put("SUBCLASS_3," + 2, "途欠"); // 出欠
                headerRecord2.put("SUBCLASS_3," + 3, "遅刻"); // 出欠
                headerRecord2.put("SUBCLASS_3," + 4, "早退"); // 出欠
                headerRecord2.put("SUBCLASS_3," + 5, "合計"); // 出欠
                headerRecord2.put("SUBCLASS_3_TEXT", "主な事由"); // 出欠

                for (final Iterator it = _studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    final Map record = newRecord(RECORD1_TAB2, recordList);
                    record.put("HANTEI_3", student.getHantei(_dataDiv)); // 判定
                    record.put("HR_CLASS_NAME1_3", student._hrNameabbv); // 組
                    record.put("NAME_3", student._name); // 生徒氏名
                    record.put("AVG_3", student.getSubclassAvg(SUBCLASSCD999999)); // 平均点

                    record.put("SUBCLASS_3_TEXT", mkString(student._attendSemesRemarkList, " ")); // 出欠
                    if (null != student._attendance) {
                        final Attendance att = student._attendance;
                        record.put("SUBCLASS_3," + 1, String.valueOf(att._absence)); // 出欠
                        record.put("SUBCLASS_3," + 2, String.valueOf(att._tochuKekka)); // 出欠
                        record.put("SUBCLASS_3," + 3, String.valueOf(att._late)); // 出欠
                        record.put("SUBCLASS_3," + 4, String.valueOf(att._early)); // 出欠
                        record.put("SUBCLASS_3," + 5, String.valueOf(att.kekkaGokei())); // 出欠
                    }
                }

                if (_isAddRow) {
                    newRecord(RECORD1_TAB3, recordList).put("TAB_ROW_DUMMY3", "1"); // テーブルブランク行ダミー
                }

                newRecord(RECORD1_TAB1_LINE, recordList).put("TAB_LINE_DUMMY1", "1"); // テーブル下線ダミー

                newRecord(RECORD1, recordList).put("TEXT1", "　　　　　　　　合計数＝ " + String.valueOf(_studentList.size()) + " 名"); // テキスト
                break;
            case TABLE_DIV_SHIDOU:
                final Map headerRecord3 = newRecord(RECORD1_TAB3, recordList);
                headerRecord3.put("HR_CLASS_NAME1_2", "組"); // 組
                headerRecord3.put("NAME_2", "氏名"); // 生徒氏名
                headerRecord3.put("AVG_2", "平均点"); // 平均点

                headerRecord3.put("SIDOU_COMMENT2", "生活指導内容"); // 指導コメント

                headerRecord3.put("SUBCLASS_2," + 1, "欠席"); // 出欠
                headerRecord3.put("SUBCLASS_2," + 2, "途欠"); // 出欠
                headerRecord3.put("SUBCLASS_2," + 3, "遅刻"); // 出欠
                headerRecord3.put("SUBCLASS_2," + 4, "早退"); // 出欠
                headerRecord3.put("SUBCLASS_2," + 5, "合計"); // 出欠

                for (final Iterator it = _studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    final Map record = newRecord(RECORD1_TAB3, recordList);
                    record.put("HR_CLASS_NAME1_2", student._hrNameabbv); // 組
                    record.put("NAME_2", student._name); // 生徒氏名
                    record.put("AVG_2", student.getSubclassAvg(SUBCLASSCD999999)); // 平均点

                    record.put("SIDOU_COMMENT2", mkString(student._detailHistDatList, " ")); // 指導コメント

                    if (null != student._attendance) {
                        final Attendance att = student._attendance;
                        record.put("SUBCLASS_2," + 1, String.valueOf(att._absence)); // 出欠
                        record.put("SUBCLASS_2," + 2, String.valueOf(att._tochuKekka)); // 出欠
                        record.put("SUBCLASS_2," + 3, String.valueOf(att._late)); // 出欠
                        record.put("SUBCLASS_2," + 4, String.valueOf(att._early)); // 出欠
                        record.put("SUBCLASS_2," + 5, String.valueOf(att.kekkaGokei())); // 出欠
                    }
                }
                newRecord(RECORD1_TAB2_LINE, recordList).put("TAB_LINE_DUMMY2", "1"); // テーブル下線ダミー
                break;
            }
            if (!isLast) {
            	newRecord(RECORD1, recordList).put("BLANK_TEXT", "1"); // テキスト
            }
//            log.info(" recordList = " + recordList);
            return recordList;
        }
        public String toString() {
            return "Table(" + _header + ")";
        }
    }

    private static class Ratio implements Comparable {
        final int _bunshi;
        final int _bunbo;
        Ratio(final int bunshi, final int bunbo) {
            _bunshi = bunshi;
            _bunbo = bunbo;
        }
        BigDecimal multiplyAttendLimit(final int num) {
        	final int scale = 0;
        	final int roundingMode = BigDecimal.ROUND_UP;
        	if (_bunshi == 1 && _bunbo > 0 && num % _bunbo == 0) {
        		BigDecimal rtn = new BigDecimal(num / _bunbo + 1); // 割り切れる場合は割った値+1
        		//log.info(" num / _bunbo = " + (num / _bunbo) + " , rtn = " + rtn);
				return rtn;
        	}
            return multiply(num, scale, roundingMode);
        }
        BigDecimal multiply(final int num, final int scale, final int roundingMode) {
            return new BigDecimal(num).multiply(new BigDecimal(_bunshi)).divide(new BigDecimal(_bunbo), scale, roundingMode);
        }
        public String str() {
            return String.valueOf(_bunshi) + "/" + String.valueOf(_bunbo);
        }
        public int compareTo(final Object o) {
            final Ratio r = (Ratio) o;
            return r._bunbo * _bunshi - _bunbo * r._bunshi;
        }
    }

    private List getFormatNenkumiTextList(final List list, final String elemTail, final int maxKeta) {
        final List rtn = new ArrayList();
        StringBuffer current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final String s = (String) it.next();

            if (null == current || getMS932ByteLength(current.toString() + elemTail + s) > maxKeta) {
                current = new StringBuffer();
                rtn.add(current);
            }
            current.append(elemTail + s);
        }
        for (int i = 0; i < rtn.size(); i++) {
            rtn.set(i, rtn.get(i).toString());
        }
        return rtn;
    }

    private static String defstr(final String s) {
        return StringUtils.defaultString(s);
    }

    private static String defstr(final String s, final String alt) {
        return StringUtils.defaultString(s, alt);
    }

    private String formatDate(final DB2UDB db2, final String date) {
        if (null == date) {
            return "";
        }
        final Calendar cal = Calendar.getInstance();
        cal.setTime(Date.valueOf(date));
        final int year = cal.get(Calendar.YEAR);
        final int month = cal.get(Calendar.MONTH) + 1;
        final int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        return KNJ_EditDate.gengou(db2, year) + "年" + (month < 10 ? " " : "") + String.valueOf(month) + "月" + (dayOfMonth < 10 ? " " : "") + String.valueOf(dayOfMonth) + "日";
    }

    private static String toStr(final Object o) {
        return null == o ? null : o.toString();
    }

    private class TargetArg {
        final List _toushoCountList = new ArrayList();
        final List _toushoLineList = new ArrayList();
        final List _genzaiCountList = new ArrayList();
        final List _genzaiLineList = new ArrayList();
        final List _shingiCountList = new ArrayList();
        final List _shingiLineList = new ArrayList();
        final List _shingiAllStudentList = new ArrayList();

        int _shussekisubekiNissu;
        int _3_to_kessekiNissu;
        int _3_chi_kessekiNissu;

        public String getHantei(final Student student, final int dataDiv) {
            final int idx = ArrayUtils.indexOf(DIV3_ARRAY, dataDiv);
            if (-1 == idx) {
                return null;
            }
            for (int i = idx + 1; i < DIV3_ARRAY.length; i++) {
                if (isTargetStudent(DIV3_ARRAY[i], this, student)) {
                    return "後出";
                }
            }
            return null;
        }
    }

    private Map getMappedLineList(final DB2UDB db2, final List studentList, final List hrClassList) {

        final TargetArg arg = new TargetArg();
        for (int hri = 0; hri < hrClassList.size(); hri++) {
            final HrClass hrClass = (HrClass) hrClassList.get(hri);

            final int countTousho = HrClass.getZaisekiList(hrClass._studentList, null, _param, _param._yearSdate).size();
            arg._toushoCountList.add(new BigDecimal(countTousho));

            final int countGenzai = HrClass.getZaisekiList(hrClass._studentList, null, _param, _param._edate).size();
            arg._genzaiCountList.add(new BigDecimal(countGenzai));

            final List shingiStudentList = new ArrayList();
            shingiStudentList.addAll(HrClass.getZaisekiList(hrClass._studentList, null, _param, _param._edate));
            shingiStudentList.removeAll(HrClass.getStudentCountRyugakuKyugakuSex(hrClass._studentList, 2, null));
            final int countShingi = shingiStudentList.size();
            arg._shingiCountList.add(new BigDecimal(countShingi));
            arg._shingiAllStudentList.addAll(shingiStudentList);

            arg._toushoLineList.add(defstr(hrClass._hrNameabbv) + "　" + String.valueOf(countTousho) + "名");
            arg._genzaiLineList.add(defstr(hrClass._hrNameabbv) + "　" + String.valueOf(countGenzai) + "名");
            arg._shingiLineList.add(defstr(hrClass._hrNameabbv) + "　" + String.valueOf(countShingi) + "名");
        }

        arg._shussekisubekiNissu = getShussekisubekiNissu(arg._shingiAllStudentList);
        arg._3_to_kessekiNissu = _param._3_to_shussekisubeki_ratio.multiplyAttendLimit(arg._shussekisubekiNissu).intValue();
        arg._3_chi_kessekiNissu = _param._3_chi_shussekisubeki_ratio.multiplyAttendLimit(arg._shussekisubekiNissu).intValue();

        Student studentKaikin6Daihyou = null;
        Student studentKaikin3Daihyou = null;
        Student studentKaikin1Daihyou = null;
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();

            if (student._schregno.equals(_param._schregnoKaikin6)) {
                studentKaikin6Daihyou = student;
            }
            if (student._schregno.equals(_param._schregnoKaikin3)) {
                studentKaikin3Daihyou = student;
            }
            if (student._schregno.equals(_param._schregnoKaikin1)) {
                studentKaikin1Daihyou = student;
            }
        }


        final Map rtn = new TreeMap();
        final List itemList1 = getMappedList(rtn, new Integer(1)); // 1ページ目
        newRecord(RECORD1, itemList1).put("TEXT1", ("１．在籍に関する事項")); // テキスト
        {
            newRecord(RECORD1, itemList1).put("TEXT1", "（イ）　　本年度当初の在籍者数 ------- " + defstr(getSum(arg._toushoCountList)) + "名"); // テキスト
            for (final Iterator it = getFormatNenkumiTextList(arg._toushoLineList, "　　　", 120).iterator(); it.hasNext();) {
                newRecord(RECORD1, itemList1).put("TEXT1", (String) it.next()); // テキスト
            }
            newRecord(RECORD1, itemList1).put("BLANK_TEXT", "1"); // テキスト
        }
        {
            newRecord(RECORD1, itemList1).put("TEXT1", "（ロ）　　現在の在籍者数 ------- " + defstr(getSum(arg._genzaiCountList)) + "名"); // テキスト
            for (final Iterator it = getFormatNenkumiTextList(arg._genzaiLineList, "　　　", 120).iterator(); it.hasNext();) {
                newRecord(RECORD1, itemList1).put("TEXT1", (String) it.next()); // テキスト
            }
            newRecord(RECORD1, itemList1).put("BLANK_TEXT", "1"); // テキスト
        }
        {
            newRecord(RECORD1, itemList1).put("TEXT1", "（ハ）　　審議の在籍者数 ------- " + String.valueOf(arg._shingiAllStudentList.size()) + "名"); // テキスト
            for (final Iterator it = getFormatNenkumiTextList(arg._shingiLineList, "　　　", 120).iterator(); it.hasNext();) {
                newRecord(RECORD1, itemList1).put("TEXT1", (String) it.next()); // テキスト
            }
            newRecord(RECORD1, itemList1).put("BLANK_TEXT", "1"); // テキスト
        }
        {
            final List tengakuTaigaku = HrClass.getTengakuTaigaku(studentList, _param, _param._edate);
            newRecord(RECORD1, itemList1).put("TEXT1", "（ニ）　　退学者・転学者 ------- " + String.valueOf(tengakuTaigaku.size()) + "名"); // テキスト
            for (final Iterator it = tengakuTaigaku.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                newRecord(RECORD1, itemList1).put("TEXT1", "　　　" + defstr(student._hrNameabbv) + "　" + rightPadding(defstr(student._name), 24) + "　　　(" + formatDate(db2, student._grddate) + " " + defstr(student._grddivName) + " " + defstr(student._grdText) + ")"); // テキスト
            }
            newRecord(RECORD1, itemList1).put("BLANK_TEXT", "1"); // テキスト
        }
        {
            final List tennyu = HrClass.getTennyu(studentList, _param, _param._edate);
            newRecord(RECORD1, itemList1).put("TEXT1", "（ホ）　　転入学者 ------- " + String.valueOf(tennyu.size()) + "名"); // テキスト
            for (final Iterator it = tennyu.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                newRecord(RECORD1, itemList1).put("TEXT1", "　　　" + defstr(student._hrNameabbv) + "　" + rightPadding(defstr(student._name), 24) + "　　　(" + formatDate(db2, student._entdate) + " " + defstr(student._entdivName) + " " + defstr(student._entText) + ")"); // テキスト
            }
            newRecord(RECORD1, itemList1).put("BLANK_TEXT", "1"); // テキスト
        }
        {
            final List fukugaku = HrClass.getStudentFukugaku(studentList, _param._edate);
            newRecord(RECORD1, itemList1).put("TEXT1", "（ヘ）　　復学者数 ------- " + String.valueOf(fukugaku.size()) + "名"); // テキスト
            for (final Iterator it = fukugaku.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                newRecord(RECORD1, itemList1).put("TEXT1", "　　　" + defstr(student._hrNameabbv) + "　" + rightPadding(defstr(student._name), 24) + "　　　(" + formatDate(db2, student._entdate) + " " + defstr(student._entdivName) + " " + defstr(student._entText) + ")"); // テキスト
            }
            newRecord(RECORD1, itemList1).put("BLANK_TEXT", "1"); // テキスト
        }
        {
            final List kyugaku = HrClass.getStudentCountKyugakuSex(studentList, 2, null);
            newRecord(RECORD1, itemList1).put("TEXT1", "（ト）　　休学者数 ------- " + String.valueOf(kyugaku.size()) + "名"); // テキスト
            for (final Iterator it = kyugaku.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                newRecord(RECORD1, itemList1).put("TEXT1", "　　　" + defstr(student._hrNameabbv) + "　" + rightPadding(defstr(student._name), 24) + "　　　(" + formatDate(db2, student._transferSdate2) + FROM_TO_MARK + formatDate(db2, student._transferEdate2) + ")"); // テキスト
            }
            newRecord(RECORD1, itemList1).put("BLANK_TEXT", "1"); // テキスト
        }

        final List itemList2 = getMappedList(rtn, new Integer(2)); // 2ページ目
        newRecord(RECORD1, itemList2).put("TEXT1", "２．表彰に関する事項"); // テキスト

        {
            final String zaisekiTotal = defstr(getSum(arg._genzaiCountList));
            final BigDecimal percentage = new BigDecimal("5");
            String countXpercent = "";
            if (NumberUtils.isNumber(zaisekiTotal)) {
                countXpercent = new BigDecimal(zaisekiTotal).multiply(percentage).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP).toString();
            }
            final int dataDiv = DIV_2_I_SEISEKI_YURYOSYA;
            final List std21 = getTargetStudentList(dataDiv, arg);
            Collections.sort(std21, new Student.ComparatorByAvg());
            final Table table = new Table(TABLE_DIV_SEISEKI_SHUKKETSU, "（イ）　　成績優良者（年間平均" + _param._2_i_avg + "点以上、欠席・途中欠課・遅刻・早退の合計が" + _param._2_i_kessekiGokeiMax + "未満）", std21, "　（在籍者数" + zaisekiTotal + "名　" + percentage.toString() + "％　" + countXpercent + "名）", false, dataDiv);
            itemList2.add(table);
        }
        {
            final int dataDiv = DIV_2_RO_6KANEN_KAIKIN;
            final List std2ro = !_param._isPrintKaikin6 ? new ArrayList() : getTargetStudentList(dataDiv, arg);
            final Table table = new Table(TABLE_DIV_SEITO_LIST, rightPadding("（ロ）　　6ヵ年皆勤者 ------- " + String.valueOf(std2ro.size()) + "名", 80) + daihyou(studentKaikin6Daihyou), std2ro, "", false, dataDiv);
            itemList2.add(table);
            if (!_param._isPrintKaikin6) {
                table._canncelComment = "　　　　卒業学年ではないので対象外です";
            }
        }
        {
            final int dataDiv = DIV_2_HA_3KANEN_KAIKIN;
            final List std2ha = !_param._isPrintKaikin3 ? new ArrayList() : getTargetStudentList(dataDiv, arg);
            final Table table = new Table(TABLE_DIV_SEITO_LIST, rightPadding("（ハ）　　3ヵ年皆勤者 ------- " + String.valueOf(std2ha.size()) + "名", 80) + daihyou(studentKaikin3Daihyou), std2ha, "", false, dataDiv);
            itemList2.add(table);
            if (!_param._isPrintKaikin3) {
                table._canncelComment = "　　　　卒業学年ではないので対象外です";
            }
        }
        {
            final int dataDiv = DIV_2_NI_1KANEN_KAIKIN;
            final List std2ni = getTargetStudentList(dataDiv, arg);
            final Table table = new Table(TABLE_DIV_SEITO_LIST, rightPadding("（ニ）　　1ヵ年皆勤者 ------- " + String.valueOf(std2ni.size()) + "名", 80) + daihyou(studentKaikin1Daihyou), std2ni, "", false, dataDiv);
            itemList2.add(table);
        }

        final List itemList3 = getMappedList(rtn, new Integer(3)); // 3ページ目

        newRecord(RECORD1, itemList3).put("TEXT1", "３．卒業及び進級に関する事項"); // テキスト
        newRecord(RECORD1, itemList3).put("BLANK_TEXT", "1"); // テキスト
        {
            final int dataDiv = DIV_3_I_KEKKSEKIGOKEI;
            final Table table = new Table(TABLE_DIV_SHUKKETSU, "（イ）　　欠席・途中欠課・遅刻・早退の合計が" + _param._3_i_kessekiGokeiMin + "回以上の者", getTargetStudentList(dataDiv, arg), "", false, dataDiv);
            itemList3.add(table);
        }
        {
            final int dataDiv = DIV_3_RO_NENKANHEIKIN_TOTAL;
            final Table table = new Table(TABLE_DIV_SEISEKI_SHUKKETSU, "（ロ）　　定期考査成績の年間総平均点が" + _param._3_ro_total_avg + "点未満である者", getTargetStudentList(dataDiv, arg), "", false, dataDiv);
            itemList3.add(table);
        }
        {
            final int dataDiv = DIV_3_HA_SHUTOKU_FUNINTEI;
            final Table table = new Table(TABLE_DIV_SEISEKI_SHUKKETSU, "（ハ）　　教科担当者が単位を認めない者", getTargetStudentList(dataDiv, arg), "", false, dataDiv);
            itemList3.add(table);
            if ("J".equals(_param._schoolKind)) {
                table._canncelComment = "　　　　※中学は対象外";
            }
        }
        {
            final int dataDiv = DIV_3_NI_NENKANHEIKIN_KAMOKU;
            final Table table = new Table(TABLE_DIV_SEISEKI_SHUKKETSU, "（ニ）　　年間平均" + _param._3_ni_kamoku_avg + "点未満の科目が全教科の" + _param._3_ni_seiseki_ratio.str() + "以上にわたる者", getTargetStudentList(dataDiv, arg), "", false, dataDiv);
            itemList3.add(table);
        }
        {
            final int dataDiv = DIV_3_HO_JOKI_1_AND_OTHER_2;
            final Table table = new Table(TABLE_DIV_SEISEKI_SHUKKETSU, "（ホ）　　上記（イ）と他の２項目にわたって該当する者", getTargetStudentList(dataDiv, arg), "", false, dataDiv);
            itemList3.add(table);
        }
        {
            final int dataDiv = DIV_3_HE_NENKANHEIKIN_TOTAL2;
            final Table table = new Table(TABLE_DIV_SEISEKI_SHUKKETSU, "（ヘ）　　定期考査成績の年間総平均点が" + _param._3_he_total_avg + "点未満である者", getTargetStudentList(dataDiv, arg), "", false, dataDiv);
            itemList3.add(table);
        }

        {
            final int dataDiv = DIV_3_TO_SHUSSEKISUBEKI;
            final Table table = new Table(TABLE_DIV_SEISEKI_SHUKKETSU, insertBlank(150, "（ト）　　欠席日数が出席すべき日数の" + _param._3_to_shussekisubeki_ratio.str() + "を超える者", "出席すべき日数" + arg._shussekisubekiNissu + "日　" + arg._3_to_kessekiNissu + "日以上"), getTargetStudentList(dataDiv, arg), "", false, dataDiv);
            itemList3.add(table);
        }
        {
            final int dataDiv = DIV_3_CHI_SHUSSEKISUBEKI2;
            final Table table = new Table(TABLE_DIV_SEISEKI_SHUKKETSU, insertBlank(150, "（チ）　　欠席日数が出席すべき日数の" + _param._3_chi_shussekisubeki_ratio.str() + "を超える者", "出席すべき日数" + arg._shussekisubekiNissu + "日　" + arg._3_chi_kessekiNissu + "日以上"), getTargetStudentList(dataDiv, arg), "", false, dataDiv);
            itemList3.add(table);
        }
        {
            final int dataDiv = DIV_3_SEIKATUSHIDOU;
            final Table table = new Table(TABLE_DIV_SHIDOU, "　【生活指導の記録】", getTargetStudentList(dataDiv, arg), "", false, dataDiv);
            itemList3.add(table);
        }

        return rtn;
    }

    private String daihyou(final Student student) {
        String hrnameAbbv = null;
        String name = null;
        if (null != student) {
            hrnameAbbv = student._hrNameabbv;
            name = student._name;
        }
        return "【代表　" + rightPadding(hrnameAbbv, 10) + "  " + rightPadding(name, 24) + "】";
    }

    private String insertBlank(final int keta, final String s1, final String s2) {
        final int blankKeta = keta - getMS932ByteLength(s1) - getMS932ByteLength(s2);
        return defstr(s1) + StringUtils.replace(StringUtils.repeat(" ", blankKeta), "  ", "　") + defstr(s2);
    }

    private List getTargetStudentList(final int dataDiv, final TargetArg arg) {
        final List targetStudentList = new ArrayList();

        for (final Iterator it = arg._shingiAllStudentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            final boolean isTarget = isTargetStudent(dataDiv, arg, student);
            if (isTarget) {
                targetStudentList.add(student);
                student.setHantei(dataDiv, arg.getHantei(student, dataDiv));
            }
        }
        Collections.sort(targetStudentList, new Student.ComparatorByDataDiv(dataDiv));
        return targetStudentList;
    }

    private boolean isTargetStudent(final int dataDiv, final TargetArg arg, final Student student) {
        final String avg9 = student.getSubclassAvg(SUBCLASSCD999999);

        boolean isTarget = false;
        switch (dataDiv) {
        case DIV_2_I_SEISEKI_YURYOSYA:
            // 成績
            boolean isTargetSeiseki = false;
            if (NumberUtils.isNumber(avg9)) {
                if (new BigDecimal(avg9).compareTo(_param._2_i_avg) >= 0) {
                    isTargetSeiseki = true;
                }
            }
            // 出欠
            boolean isTargetShukketsu = false;
            if (null != student._attendance) {
                Attendance att = student._attendance;
                if (att.kekkaGokei() < _param._2_i_kessekiGokeiMax) {
                    isTargetShukketsu = true;
                }
            }
            isTarget = isTargetSeiseki && isTargetShukketsu;
            break;

        case DIV_2_RO_6KANEN_KAIKIN:
        case DIV_2_HA_3KANEN_KAIKIN:
        case DIV_2_NI_1KANEN_KAIKIN:
            boolean _6nen = false;
            if (_param._isPrintKaikin6) {
            	if (student._regdYearCount.intValue() < 6) {
            		_6nen = false;
            	} else {
            		_6nen = true;
            		boolean hasData = false;
            		final Set notFoundAttendanceYear6 = new TreeSet();
            		for (final Iterator it = _param.beforeYearList(6).iterator(); it.hasNext();) {
            			final String year = (String) it.next();
            			final Attendance att = (Attendance) student._yearAttendanceMap.get(year);
            			hasData = hasData || null != att;
            			if (null == att) {
            				notFoundAttendanceYear6.add(year);
            			} else if (!Attendance.isKaikin(att)) {
            				_6nen = false;
            				break;
            			}
            		}
            		if (!hasData) {
            			_6nen = false;
            		}
            		if (!notFoundAttendanceYear6.isEmpty()) {
            			log.warn(" 出欠データなし: " + student._schregno + ", year = " + notFoundAttendanceYear6 + " / data year = " + student._yearAttendanceMap.keySet());
            		}
            	}
            }
            boolean _3nen = false;
            if (_param._isPrintKaikin3) {
            	if (student._regdYearCount.intValue() < 3) {
            		_3nen = false;
            	} else {
            		_3nen = true;
            		boolean hasData = false;
            		final Set notFoundAttendanceYear3 = new TreeSet();
            		for (final Iterator it = _param.beforeYearList(3).iterator(); it.hasNext();) {
            			final String year = (String) it.next();
            			final Attendance att = (Attendance) student._yearAttendanceMap.get(year);
            			hasData = hasData || null != att;
            			if (null == att) {
            				notFoundAttendanceYear3.add(year);
            			} else if (!Attendance.isKaikin(att)) {
            				_3nen = false;
            				break;
            			}
            		}
            		if (!hasData) {
            			_3nen = false;
            		}
            		if (!notFoundAttendanceYear3.isEmpty()) {
            			log.warn(" 出欠データなし: " + student._schregno + ", year = " + notFoundAttendanceYear3 + " / data year = " + student._yearAttendanceMap.keySet());
            		}
            	}
            }
            if (_param._isPrintKaikin6 && dataDiv == DIV_2_RO_6KANEN_KAIKIN) {
            	isTarget = _6nen;
            } else if (_param._isPrintKaikin3 && dataDiv == DIV_2_HA_3KANEN_KAIKIN) {
            	isTarget = _3nen && !_6nen;
            } else if (dataDiv == DIV_2_NI_1KANEN_KAIKIN) {
            	isTarget = !_6nen && !_3nen && null != student._attendance && Attendance.isKaikin(student._attendance);
            }
            break;

        case DIV_3_I_KEKKSEKIGOKEI:
            if (null != student._attendance) {
                isTarget = student._attendance.kekkaGokei() >= _param._3_i_kessekiGokeiMin;
            }
            break;

        case DIV_3_RO_NENKANHEIKIN_TOTAL:
            if (NumberUtils.isNumber(avg9)) {
                if (new BigDecimal(avg9).compareTo(_param._3_ro_total_avg) < 0) {
                    isTarget = true;
                }
            } else {
                isTarget = true;
            }
            break;

        case DIV_3_HA_SHUTOKU_FUNINTEI:
            isTarget = student.hasFunintei();
            break;

        case DIV_3_NI_NENKANHEIKIN_KAMOKU:
            final Map kakukamoku = new HashMap(student._subclassBdAvgMap);
            kakukamoku.remove(SUBCLASSCD999999);
            int mimanCount = 0;
            for (final Iterator it = kakukamoku.keySet().iterator(); it.hasNext();) {
                final String subclasscd = (String) it.next();
                final BigDecimal bdAvg = (BigDecimal) kakukamoku.get(subclasscd);
                if (bdAvg.compareTo(_param._3_ni_kamoku_avg) < 0) {
                    student._subclassMarkMap.put(subclasscd, "*");
                    mimanCount += 1;
                }
            }
            isTarget = new Ratio(mimanCount, kakukamoku.size()).compareTo(_param._3_ni_seiseki_ratio) >= 0;
            break;

        case DIV_3_HO_JOKI_1_AND_OTHER_2:
            if (isTargetStudent(DIV_3_I_KEKKSEKIGOKEI, arg, student)) {
                int count = 0;
                final int[] divArray = {DIV_3_RO_NENKANHEIKIN_TOTAL, DIV_3_HA_SHUTOKU_FUNINTEI, DIV_3_NI_NENKANHEIKIN_KAMOKU};
                for (int i = 0; i < divArray.length; i++) {
                    if (isTargetStudent(divArray[i], arg, student)) {
                        count += 1;
                    }
                }
                isTarget = count >= 2;
            }
            break;

        case DIV_3_HE_NENKANHEIKIN_TOTAL2:
            if (NumberUtils.isNumber(avg9)) {
                if (new BigDecimal(avg9).compareTo(_param._3_he_total_avg) < 0) {
                    isTarget = true;
                }
            }
            break;

        case DIV_3_TO_SHUSSEKISUBEKI:
            if (null != student._attendance) {
                isTarget = student._attendance._absence >= arg._3_to_kessekiNissu;
            }
            break;

        case DIV_3_CHI_SHUSSEKISUBEKI2:
            if (null != student._attendance) {
                isTarget = student._attendance._absence >= arg._3_chi_kessekiNissu;
            }
            break;
        case DIV_3_SEIKATUSHIDOU:
            isTarget = !student._detailHistDatList.isEmpty();
            break;
        default:
        }
        return isTarget;
    }

    private int getShussekisubekiNissu(final List studentAllList) {
        int shussekiSubekiNissuMax = 0;
        for (final Iterator it = studentAllList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            if (null != student._attendance) {
                shussekiSubekiNissuMax = Math.max(shussekiSubekiNissuMax, student._attendance._mlesson);
            }
        }
        return shussekiSubekiNissuMax;
    }

    private static String rightPadding(final String s, final int keta) {
        return StringUtils.defaultString(s) + StringUtils.replace(StringUtils.repeat(" ", keta - getMS932ByteLength(s)), "  ", "　");
    }

    private static String mkString(final List remarkList, final String comma) {
        final StringBuffer stb = new StringBuffer();
        String cm = "";
        for (final Iterator it = remarkList.iterator(); it.hasNext();) {
            stb.append(cm).append(it.next());
            cm = comma;
        }
        return stb.toString();
    }

    private static List getMappedList(final Map map, final Object key) {
        if (null == map.get(key)) {
            map.put(key, new ArrayList());
        }
        return (List) map.get(key);
    }

    private static Set getMappedSet(final Map map, final Object key) {
        if (null == map.get(key)) {
            map.put(key, new TreeSet());
        }
        return (Set) map.get(key);
    }

    private static Map getMappedMap(final Map map, final Object key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap());
        }
        return (Map) map.get(key1);
    }

    private static void setData(final DB2UDB db2, final Param param, final List studentList, final List hrClassList) {
        log.debug(" setData ");
        PreparedStatement ps = null;

        Map studentMap = new HashMap();
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            studentMap.put(student._schregno, student);
        }

        // １日出欠
        try {
            param._attendParamMap.put("grade", "?");
            param._attendParamMap.put("hrClass", "?");

            String sql = AttendAccumulate.getAttendSemesSql(
                    param._year,
                    param._semester,
                    param._sdate,
                    param._edate,
                    param._attendParamMap
            );
            ps = db2.prepareStatement(sql);
            final Integer _0 = new Integer(0);
            for (final Iterator hIt = hrClassList.iterator(); hIt.hasNext();) {
                final HrClass hrClass = (HrClass) hIt.next();
                log.debug(" set Attendance " + hrClass);

                //log.debug(" attend semes sql = " + sql);
                for (final Iterator it = KnjDbUtils.query(db2, ps, new Object[] {hrClass._grade, hrClass._hrClass}).iterator(); it.hasNext();) {
                    final Map row = (Map) it.next();
                    final Student student = (Student) studentMap.get(KnjDbUtils.getString(row, "SCHREGNO"));
                    if (student == null || !"9".equals(KnjDbUtils.getString(row, ("SEMESTER")))) {
                        continue;
                    }
                    final int lesson = KnjDbUtils.getInt(row, "LESSON", _0).intValue();
                    final int mourning = KnjDbUtils.getInt(row, "MOURNING", _0).intValue();
                    final int suspend = KnjDbUtils.getInt(row, "SUSPEND", _0).intValue() + KnjDbUtils.getInt(row, "VIRUS", _0).intValue() + KnjDbUtils.getInt(row, "KOUDOME", _0).intValue();
                    final int abroad = KnjDbUtils.getInt(row, "TRANSFER_DATE", _0).intValue();
                    final int mlesson = KnjDbUtils.getInt(row, "MLESSON", _0).intValue();
                    final int absence = KnjDbUtils.getInt(row, "SICK", _0).intValue();
                    final int attend = KnjDbUtils.getInt(row, "PRESENT", _0).intValue();
                    final int late = KnjDbUtils.getInt(row, "LATE", _0).intValue();
                    final int early = KnjDbUtils.getInt(row, "EARLY", _0).intValue();
                    final int tochuKekka = KnjDbUtils.getInt(row, "TOCHU_KEKKA", _0).intValue();

                    final Attendance attendance = new Attendance(student._schregno, lesson, mourning, suspend, abroad, mlesson, absence, attend, late, early, tochuKekka);
                    //log.info("   schregno = " + student._schregno + " , attendance = " + attendance + " => " + Attendance.isKaikin(attendance));
                    student._attendance = attendance;
                }
            }

        } catch (Exception e) {
            log.error("sql exception!", e);
        } finally {
            DbUtils.closeQuietly(ps);
        }

        // １日出欠
        try {
            final List yearList;
            if (param._isPrintKaikin6) {
                yearList = param.beforeYearList(6);
            } else if (param._isPrintKaikin3) {
                yearList = param.beforeYearList(3);
            } else {
                yearList = Collections.EMPTY_LIST;
            }

            final Map attendParamMapInit = new HashMap(param._attendParamMap);
            attendParamMapInit.remove("hasuuMap");
            attendParamMapInit.remove("attendSemesInState");
            attendParamMapInit.remove("attendSemesMap");
            attendParamMapInit.remove("periodInState");
            attendParamMapInit.remove("knjDefineSchool");
            attendParamMapInit.remove("knjSchoolMst");
            attendParamMapInit.remove("sdate");
            attendParamMapInit.remove("grade");
            attendParamMapInit.remove("hrClass");

            final DecimalFormat df = new DecimalFormat("00");

            for (final Iterator yit = yearList.iterator(); yit.hasNext();) {
                final String year = (String) yit.next();

                if (year.equals(param._year)) {
                    for (final Iterator it = studentList.iterator(); it.hasNext();) {
                        final Student student = (Student) it.next();
                        student._yearAttendanceMap.put(year, student._attendance);
                    }
                    continue;
                }

                final String grade = df.format(Integer.parseInt(param._grade) - (Integer.parseInt(param._year) - Integer.parseInt(year)));

                final Map attendparamMap = new HashMap(attendParamMapInit);
                attendparamMap.put("grade", grade);

                final Map semesterMap = param.getSemesterMap(db2, year);

                final TreeSet semestercdSet = new TreeSet(semesterMap.keySet());
                semestercdSet.remove(SEMEALL);
                if (semestercdSet.isEmpty()) {
                    log.warn(" 学期無し: year = " + year);
                    continue;
                }
                final String lastSemester = (String) semestercdSet.last();

                String sql = AttendAccumulate.getAttendSemesSql(
                        year,
                        lastSemester,
                        (String) getMappedMap(semesterMap, "1").get("SDATE"),
                        (String) getMappedMap(semesterMap, "9").get("EDATE"),
                        attendparamMap
                );
                //log.debug(" attend semes sql (" + year + ")= " + sql);
                final Integer _0 = new Integer(0);
                for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
                    final Map row = (Map) it.next();
                    final Student student = (Student) studentMap.get(KnjDbUtils.getString(row, "SCHREGNO"));
                    if (student == null || !"9".equals(KnjDbUtils.getString(row, ("SEMESTER")))) {
                        continue;
                    }
                    final int lesson = KnjDbUtils.getInt(row, "LESSON", _0).intValue();
                    final int mourning = KnjDbUtils.getInt(row, "MOURNING", _0).intValue();
                    final int suspend = KnjDbUtils.getInt(row, "SUSPEND", _0).intValue() + KnjDbUtils.getInt(row, "VIRUS", _0).intValue() + KnjDbUtils.getInt(row, "KOUDOME", _0).intValue();
                    final int abroad = KnjDbUtils.getInt(row, "TRANSFER_DATE", _0).intValue();
                    final int mlesson = KnjDbUtils.getInt(row, "MLESSON", _0).intValue();
                    final int absence = KnjDbUtils.getInt(row, "SICK", _0).intValue();
                    final int attend = KnjDbUtils.getInt(row, "PRESENT", _0).intValue();
                    final int late = KnjDbUtils.getInt(row, "LATE", _0).intValue();
                    final int early = KnjDbUtils.getInt(row, "EARLY", _0).intValue();
                    final int tochuKekka = KnjDbUtils.getInt(row, "TOCHU_KEKKA", _0).intValue();

                    final Attendance attendance = new Attendance(student._schregno, lesson, mourning, suspend, abroad, mlesson, absence, attend, late, early, tochuKekka);
                    //log.info("   schregno = " + student._schregno + " , year = " + year + ", attendance = " + attendance + " => " + Attendance.isKaikin(attendance));
                    student._yearAttendanceMap.put(year, attendance);
                }
            }

        } catch (Exception e) {
            log.error("sql exception!", e);
        }

        // １日出欠備考
        try {
            final Map hasuuMap = AttendAccumulate.getHasuuMap(
                    db2,
                    param._year,
                    param._sdate,
                    param._edate
            );
            final List attendSemesInStateList = getMappedList(getMappedMap(hasuuMap, "GRADE:" + param._grade), "attendSemesInStateList");
            log.info(" attendSemesInStateList = " + attendSemesInStateList);

            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT ");
            sql.append("     T1.SCHREGNO, T1.REMARK1 ");
            sql.append(" FROM ATTEND_SEMES_REMARK_DAT T1 ");
            sql.append(" INNER JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = T1.SCHREGNO ");
            sql.append("     AND REGD.YEAR = T1.YEAR ");
            sql.append("     AND REGD.SEMESTER = T1.SEMESTER ");
            sql.append(" WHERE ");
            sql.append("  T1.YEAR = '" + param._year + "' ");
            sql.append("  AND T1.SEMESTER || T1.MONTH = ? ");
            sql.append("  AND T1.REMARK1 IS NOT NULL ");

            ps = db2.prepareStatement(sql.toString());

            for (int i = 0; i < attendSemesInStateList.size(); i++) {
                final String semeMonth = (String) attendSemesInStateList.get(i);

                for (final Iterator it = KnjDbUtils.query(db2, ps, new Object[] {semeMonth}).iterator(); it.hasNext();) {
                    final Map row = (Map) it.next();
                    final Student student = (Student) studentMap.get(KnjDbUtils.getString(row, "SCHREGNO"));
                    if (null == student) {
                        continue;
                    }

                    student._attendSemesRemarkList.add(KnjDbUtils.getString(row, "REMARK1"));
                }
            }

        } catch (Exception e) {
            log.error("sql exception!", e);
        } finally {
            DbUtils.closeQuietly(ps);
        }

        final String attsubSql = SubclassScore.getSubclassScoreSql(param);
        log.debug(" setRecord  sql = " + attsubSql);
        for (final Iterator it = KnjDbUtils.query(db2, attsubSql).iterator(); it.hasNext();) {
            final Map row = (Map) it.next();
            final String schregno = KnjDbUtils.getString(row, "SCHREGNO");
            final Student student = (Student) studentMap.get(schregno);
            if (null == student) {
                continue;
            }

            final String testcd = KnjDbUtils.getString(row, "TESTCD");
            if (null == testcd) {
                continue;
            }
            final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
            final String classabbv = KnjDbUtils.getString(row, "CLASSABBV");
            final String subclassname = KnjDbUtils.getString(row, "SUBCLASSNAME");
            final String subclassabbv = KnjDbUtils.getString(row, "SUBCLASSABBV");
            final String score = KnjDbUtils.getString(row, "SCORE");
            final BigDecimal avg = KnjDbUtils.getBigDecimal(row, "AVG", null);

            if (!param._subclassMap.containsKey(subclasscd)) {
                param._subclassMap.put(subclasscd, new Subclass(subclasscd, classabbv, subclassname, subclassabbv, false, false));
            }
            final Subclass subclass = (Subclass) param._subclassMap.get(subclasscd);
            subclass._subclassScoreAllNull = KnjDbUtils.getString(row, "SCORE_ALL_NULL");
            if (subclass._isMoto) {
            	continue;
            }

            final SubclassScore subclassscore = new SubclassScore(student, subclass, score, avg);

            getMappedMap(student._subclassScoreMap, subclasscd).put(testcd, subclassscore);
        }

        // 復学
        try {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT T1.SCHREGNO, MIN(COMEBACK_DATE) AS COMEBACK_DATE ");
            sql.append(" FROM SCHREG_ENT_GRD_HIST_COMEBACK_DAT T1 ");
            sql.append(" INNER JOIN SCHREG_REGD_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            sql.append("     AND T2.YEAR = '" + param._year + "' ");
            sql.append("     AND T2.SEMESTER = '" + param._semester + "' ");
            sql.append("     AND T2.GRADE = '" + param._grade + "' ");
            sql.append(" WHERE ");
            sql.append("     T1.SCHOOL_KIND = '" + param._schoolKind + "' ");
            sql.append(" GROUP BY ");
            sql.append("     T1.SCHREGNO ");

            for (final Iterator it = KnjDbUtils.query(db2, sql.toString()).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                final String schregno = KnjDbUtils.getString(row, "SCHREGNO");
                final Student student = (Student) studentMap.get(schregno);
                if (null == student) {
                    continue;
                }
                student._entGrdHistComebackDatComebackDateMin = KnjDbUtils.getString(row, "COMEBACK_DATE");
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        }

        if ("H".equals(param._schoolKind)) {
        	// 修得単位
        	try {
        		final StringBuffer sql = new StringBuffer();
        		sql.append(" SELECT T1.SCHREGNO, T1.CLASSCD, T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, T1.SCORE, T1.GET_CREDIT, D008.NAMECD2 AS D008_NAMECD2 ");
        		sql.append(" FROM RECORD_SCORE_DAT T1 ");
        		sql.append(" INNER JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = T1.SCHREGNO ");
        		sql.append("     AND REGD.YEAR = T1.YEAR ");
        		sql.append("     AND REGD.SEMESTER = '" + param._semester + "' ");
        		sql.append("     AND REGD.GRADE = '" + param._grade + "' ");
        		sql.append(" LEFT JOIN NAME_MST D008 ON NAMECD1 = '" + param._d008Namecd1 + "' AND NAMECD2 = T1.CLASSCD ");
        		sql.append(" WHERE ");
        		sql.append("     T1.YEAR = '" + param._year + "' ");
        		sql.append("     AND T1.SEMESTER = '9' ");
        		sql.append("     AND T1.TESTKINDCD = '99' ");
        		sql.append("     AND T1.TESTITEMCD = '00' ");
        		sql.append("     AND T1.SCORE_DIV = '09' ");

        		for (final Iterator it = KnjDbUtils.query(db2, sql.toString()).iterator(); it.hasNext();) {
        			final Map row = (Map) it.next();
        			final String schregno = KnjDbUtils.getString(row, "SCHREGNO");
        			final Student student = (Student) studentMap.get(schregno);
        			if (null == student) {
        				continue;
        			}
        			final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
					student._subclasscdGetCreditMap.put(subclasscd, KnjDbUtils.getString(row, "GET_CREDIT"));
					final boolean hyoteiNoInput = "90".equals(KnjDbUtils.getString(row, "CLASSCD")) || null != KnjDbUtils.getString(row, "D008_NAMECD2");
					if (!hyoteiNoInput) {
						student._subclasscdHyoteiMap.put(subclasscd, KnjDbUtils.getString(row, "SCORE"));
					}
        		}
        	} catch (Exception ex) {
        		log.fatal("exception!", ex);
        	}
        }

        // 罰
        try {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT T1.SCHREGNO, T1.DETAILCD, NMH304.NAME1 AS DETAILCD_NAME, T1.CONTENT ");
            sql.append(" FROM SCHREG_DETAILHIST_DAT T1 ");
            sql.append(" LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = T1.SCHREGNO ");
            sql.append("     AND REGD.YEAR = T1.YEAR ");
            sql.append("     AND REGD.SEMESTER = '" + param._semester + "' ");
            sql.append("     AND REGD.GRADE = '" + param._grade + "' ");
            sql.append(" LEFT JOIN NAME_MST NMH304 ON NMH304.NAMECD1 = 'H304' AND NMH304.NAMECD2 = T1.DETAILCD ");
            sql.append(" WHERE ");
            sql.append("     T1.YEAR = '" + param._year + "' ");
            sql.append("     AND T1.DETAIL_DIV = '2' ");
            sql.append(" ORDER BY ");
            sql.append("     T1.SCHREGNO ");
            sql.append("   , T1.DETAIL_SDATE ");

            for (final Iterator it = KnjDbUtils.query(db2, sql.toString()).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                final String schregno = KnjDbUtils.getString(row, "SCHREGNO");
                final Student student = (Student) studentMap.get(schregno);
                if (null == student || null == KnjDbUtils.getString(row, "DETAILCD_NAME")) {
                    continue;
                }
                student._detailHistDatList.add(StringUtils.replace(StringUtils.replace(KnjDbUtils.getString(row, "DETAILCD_NAME") + kakko(KnjDbUtils.getString(row, "CONTENT")), "\r\n", "\n"), "\n", ""));
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        }

        final TreeSet entDateSet = new TreeSet();
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            if (null != student._entdate) {
                entDateSet.add(student._entdate);
            }
        }
        if (!entDateSet.isEmpty()) {
            final String entDateMin = (String) entDateSet.first();
            if (param._yearSdate.compareTo(entDateMin) < 1) {
                param._yearSdate = entDateMin;
            }
        }

        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();

            for (final Iterator subit = student._subclassScoreMap.entrySet().iterator(); subit.hasNext();) {
                final Map.Entry e = (Map.Entry) subit.next();
                final String subclasscd = (String) e.getKey();
                final Map testcdSubclassScoreMap = (Map) e.getValue();
                final Map testcdScoreMap = new HashMap();
                for (final Iterator scit = testcdSubclassScoreMap.entrySet().iterator(); scit.hasNext();) {
                    final Map.Entry se = (Map.Entry) scit.next();
                    final String testcd = (String) se.getKey();
                    final SubclassScore subscore = (SubclassScore) se.getValue();
                    if (NumberUtils.isNumber(subscore._score)) {
                        testcdScoreMap.put(testcd, new BigDecimal(subscore._score));
                    }
                }
                getMappedMap(student._subclassAvgTargetMap, subclasscd).putAll(testcdScoreMap);
            }

            for (final Iterator subit = student._subclassAvgTargetMap.entrySet().iterator(); subit.hasNext();) {
                final Map.Entry e = (Map.Entry) subit.next();
                final String subclasscd = (String) e.getKey();

                if (SUBCLASSCD999999.equals(subclasscd)) {
                    continue;
                } else {
                    final Map testcdScoreMap = getMappedMap(student._subclassAvgTargetMap, subclasscd);
                    final String avg = getAvg(testcdScoreMap.values(), 0);
                    if (NumberUtils.isNumber(avg)) {
                        student._subclassBdAvgMap.put(subclasscd, new BigDecimal(avg));
                    }
                }
            }

//            log.info(" " + student._schregno + " | kakukamoku avg = " + student._subclassBdAvgMap);
            student._subclassBdAvgMap.put(SUBCLASSCD999999, getAvg(student._subclassBdAvgMap.values(), 1));
        }
    }

    private static String kakko(final String s) {
        return StringUtils.isEmpty(s) ? "" : "(" + s + ")";
    }

    private static List getStudentList(final DB2UDB db2, final Param param) {
        final List studentList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCHREG_TRANSFER1 AS ( ");
            stb.append("   SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     MAX(T1.TRANSFER_SDATE) AS TRANSFER_SDATE ");
            stb.append("   FROM SCHREG_TRANSFER_DAT T1 ");
            stb.append("   LEFT JOIN V_SEMESTER_GRADE_MST T2 ON T2.YEAR = '" + param._year + "' AND T2.SEMESTER = '9' AND GRADE = '" + param._grade + "' ");
            stb.append("   WHERE ");
            stb.append("     T2.SDATE BETWEEN T1.TRANSFER_SDATE AND VALUE(T1.TRANSFER_EDATE, '9999-12-31') ");
            stb.append("   GROUP BY T1.SCHREGNO ");
            stb.append(" ), SCHREG_TRANSFER2 AS ( ");
            stb.append("   SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     MAX(T1.TRANSFER_SDATE) AS TRANSFER_SDATE ");
            stb.append("   FROM SCHREG_TRANSFER_DAT T1 ");
            stb.append("   WHERE ");
            stb.append("     '" + param._edate + "' BETWEEN T1.TRANSFER_SDATE AND VALUE(T1.TRANSFER_EDATE, '9999-12-31') ");
            stb.append("   GROUP BY T1.SCHREGNO ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     REGD.GRADE, ");
            stb.append("     REGD.HR_CLASS, ");
            stb.append("     REGD.ATTENDNO, ");
            stb.append("     REGD.SCHREGNO, ");
            stb.append("     REGDH.HR_NAME, ");
            stb.append("     REGDH.HR_NAMEABBV, ");
            stb.append("     BASE.NAME, ");
            stb.append("     BASE.SEX, ");
            stb.append("     NMZ002.NAME2 AS SEX_NAME, ");
            stb.append("     ENTGRD.ENT_DATE, ");
            stb.append("     ENTGRD.ENT_DIV, ");
            stb.append("     ENTGRD.ENT_REASON, ");
            stb.append("     ENTGRD.ENT_SCHOOL, ");
            stb.append("     ENTGRD.ENT_ADDR, ");
            stb.append("     ENTGRD.ENT_ADDR2, ");
            stb.append("     NMA002.NAME1 AS ENT_DIV_NAME, ");
            stb.append("     ENTGRD.GRD_DATE, ");
            stb.append("     ENTGRD.GRD_DIV, ");
            stb.append("     ENTGRD.GRD_REASON, ");
            stb.append("     ENTGRD.GRD_SCHOOL, ");
            stb.append("     ENTGRD.GRD_ADDR, ");
            stb.append("     ENTGRD.GRD_ADDR2, ");
            stb.append("     NMA003.NAME1 AS GRD_DIV_NAME, ");
            stb.append("     T5.TRANSFERCD AS TRANSFERCD1, ");
            stb.append("     NMA004_1.NAME1 AS TRANSFER_NAME1, ");
            stb.append("     T5.TRANSFERREASON AS TRANSFERREASON1, ");
            stb.append("     T5.TRANSFER_SDATE AS TRANSFER_SDATE1, ");
            stb.append("     T5.TRANSFER_EDATE AS TRANSFER_EDATE1, ");
            stb.append("     T7.TRANSFERCD AS TRANSFERCD2, ");
            stb.append("     NMA004_2.NAME1 AS TRANSFER_NAME2, ");
            stb.append("     T7.TRANSFERREASON AS TRANSFERREASON2, ");
            stb.append("     T7.TRANSFER_SDATE AS TRANSFER_SDATE2, ");
            stb.append("     T7.TRANSFER_EDATE AS TRANSFER_EDATE2, ");
            stb.append("     REGD.COURSECD, ");
            stb.append("     REGD.MAJORCD, ");
            stb.append("     T9.MAJORNAME, ");
            stb.append("     REGD.COURSECODE, ");
            stb.append("     T8.COURSECODENAME, ");
            stb.append("     YC.YEAR_COUNT ");
            stb.append(" FROM SCHREG_REGD_DAT REGD ");
            stb.append(" LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR ");
            stb.append("     AND REGDH.SEMESTER = REGD.SEMESTER ");
            stb.append("     AND REGDH.GRADE = REGD.GRADE ");
            stb.append("     AND REGDH.HR_CLASS = REGD.HR_CLASS ");
            stb.append(" LEFT JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = REGD.YEAR ");
            stb.append("     AND REGDG.GRADE = REGD.GRADE ");
            stb.append(" LEFT JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
            stb.append(" LEFT JOIN SCHREG_ENT_GRD_HIST_DAT ENTGRD ON ENTGRD.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     AND ENTGRD.SCHOOL_KIND = REGDG.SCHOOL_KIND ");
            stb.append(" LEFT JOIN SCHREG_TRANSFER1 T4 ON T4.SCHREGNO = REGD.SCHREGNO ");
            stb.append(" LEFT JOIN SCHREG_TRANSFER_DAT T5 ON T5.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     AND T5.TRANSFER_SDATE = T4.TRANSFER_SDATE ");
            stb.append(" LEFT JOIN SCHREG_TRANSFER2 T6 ON T6.SCHREGNO = REGD.SCHREGNO ");
            stb.append(" LEFT JOIN SCHREG_TRANSFER_DAT T7 ON T7.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     AND T7.TRANSFER_SDATE = T6.TRANSFER_SDATE ");
            stb.append(" LEFT JOIN COURSECODE_MST T8 ON T8.COURSECODE = REGD.COURSECODE ");
            stb.append(" LEFT JOIN MAJOR_MST T9 ON T9.COURSECD = REGD.COURSECD AND T9.MAJORCD = REGD.MAJORCD ");
            stb.append(" LEFT JOIN NAME_MST NMA004_1 ON NMA004_1.NAMECD1 = 'A004' AND NMA004_1.NAMECD2 = T5.TRANSFERCD ");
            stb.append(" LEFT JOIN NAME_MST NMA004_2 ON NMA004_2.NAMECD1 = 'A004' AND NMA004_2.NAMECD2 = T7.TRANSFERCD ");
            stb.append(" LEFT JOIN NAME_MST NMZ002 ON NMZ002.NAMECD1 = 'Z002' AND NMZ002.NAMECD2 = BASE.SEX ");
            stb.append(" LEFT JOIN NAME_MST NMA002 ON NMA002.NAMECD1 = 'A002' AND NMA002.NAMECD2 = ENTGRD.ENT_DIV ");
            stb.append(" LEFT JOIN NAME_MST NMA003 ON NMA003.NAMECD1 = 'A003' AND NMA003.NAMECD2 = ENTGRD.GRD_DIV ");
            stb.append(" LEFT JOIN (SELECT SCHREGNO, COUNT(DISTINCT YEAR) AS YEAR_COUNT FROM SCHREG_REGD_DAT WHERE YEAR <= '" + param._year + "' GROUP BY SCHREGNO) YC ON YC.SCHREGNO = REGD.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append(" REGD.YEAR = '" + param._year + "' ");
            stb.append(" AND REGD.SEMESTER = '" + (SEMEALL.equals(param._semester) ? param._ctrlSemester : param._semester) + "' ");
            stb.append(" AND REGD.GRADE = '" + param._grade + "' ");
            if (!"".equals(param._courseCode) && !COURSEALL.equals(param._courseCode)) {
                stb.append(" AND REGD.COURSECODE = '" + param._courseCode + "' ");
            }
            stb.append(" ORDER BY ");
            stb.append("     REGD.GRADE, ");
            stb.append("     REGD.HR_CLASS, ");
            stb.append("     REGD.ATTENDNO ");

//            log.info(" regd sql = " + stb.toString());
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                final String grade = rs.getString("GRADE");
                final String hrClass = rs.getString("HR_CLASS");
                final String hrNameabbv = rs.getString("HR_NAMEABBV");
                final String attendno = rs.getString("ATTENDNO");
                final String schregno = rs.getString("SCHREGNO");
                final String hrName = rs.getString("HR_NAME");
                final String name = rs.getString("NAME");
                final String sex = rs.getString("SEX");
                final String sexName = rs.getString("SEX_NAME");
                final String entdiv = rs.getString("ENT_DIV");
                final String entdivName = rs.getString("ENT_DIV_NAME");
                final String entdate = rs.getString("ENT_DATE");
                final String grddiv = rs.getString("GRD_DIV");
                final String grddivName = rs.getString("GRD_DIV_NAME");
                final String grddate = rs.getString("GRD_DATE");
                final String transfercd1 = rs.getString("TRANSFERCD1");
                final String transferName1 = rs.getString("TRANSFER_NAME1");
                final String transferreason1 = rs.getString("TRANSFERREASON1");
                final String transferSdate1 = rs.getString("TRANSFER_SDATE1");
                final String transferEdate1 = rs.getString("TRANSFER_EDATE1");
                final String transfercd2 = rs.getString("TRANSFERCD2");
                final String transferName2 = rs.getString("TRANSFER_NAME2");
                final String transferreason2 = rs.getString("TRANSFERREASON2");
                final String transferSdate2 = rs.getString("TRANSFER_SDATE2");
                final String transferEdate2 = rs.getString("TRANSFER_EDATE2");
                final String coursecd = rs.getString("COURSECD");
                final String majorcd = rs.getString("MAJORCD");
                final String majorname = rs.getString("MAJORNAME");
                final String coursecode = rs.getString("COURSECODE");
                final String coursecodename = rs.getString("COURSECODENAME");
                final Integer regdYearCount = NumberUtils.isDigits(rs.getString("YEAR_COUNT")) ? Integer.valueOf(rs.getString("YEAR_COUNT")) : new Integer(0);

                final String entText = defstr(rs.getString("ENT_REASON")) + defstr(rs.getString("ENT_SCHOOL")); // + defstr(rs.getString("ENT_ADDR")) + defstr(rs.getString("ENT_ADDR2"));
                final String grdText = defstr(rs.getString("GRD_REASON")) + defstr(rs.getString("GRD_SCHOOL")); // + defstr(rs.getString("GRD_ADDR")) + defstr(rs.getString("GRD_ADDR2"));
                final Student student = new Student(grade, hrClass, hrNameabbv, attendno, schregno, hrName, name, sex, sexName, entdiv, entdivName, entdate, grddiv, grddivName, grddate,
                        transfercd1, transferName1, transferreason1 ,transferSdate1, transferEdate1,
                        transfercd2, transferName2, transferreason2, transferSdate2, transferEdate2,
                        coursecd, majorcd, majorname, coursecode, coursecodename, regdYearCount);
                student._entText = entText;
                student._grdText = grdText;
                studentList.add(student);
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
            ps = null;
            rs = null;
        }
        return studentList;
    }

    private static class Student {

        private static DecimalFormat attendnodf = new DecimalFormat("00");

        final String _grade;
        final String _hrClass;
        final String _hrNameabbv;
        final String _attendno;
        final String _schregno;
        final String _hrName;
        final String _name;
        final String _sex;
        final String _sexName;
        final String _entdiv;
        final String _entdivName;
        final String _entdate;
        final String _grddiv;
        final String _grddivName;
        final String _grddate;
        // 年度開始日時点の異動データ
        final String _transfercd1;
        final String _transfername1;
        final String _transferreason1;
        final String _transferSdate1;
        final String _transferEdate1;
        // パラメータ指定日付時点の異動データ
        final String _transfercd2;
        final String _transfername2;
        final String _transferreason2;
        final String _transferSdate2;
        final String _transferEdate2;
        final String _coursecd;
        final String _majorcd;
        final String _majorname;
        final String _coursecode;
        final String _coursecodename;
        final Integer _regdYearCount;
        final Map _subclassScoreMap;
        final Map _subclassMarkMap;
        final Map _subclassAvgTargetMap; // Map<String(subclasscd), Map<String(testcd), BigDecimal(score)>>科目ごとのテストごとの得点
        final Map _subclassBdAvgMap; // Map<String(subclasscd), String(avg)>科目ごとの平均点
//        final Map _subclassAttendance;
        final Map _hanteiMap;
        List _attendSemesRemarkList = new ArrayList();
        List _detailHistDatList = new ArrayList();
        final Map _subclasscdGetCreditMap = new HashMap();
        final Map _subclasscdHyoteiMap = new HashMap();

        private String _entGrdHistComebackDatComebackDateMin;
        private String _entText;
        private String _grdText;
        private Attendance _attendance;
        private Map _yearAttendanceMap = new HashMap();

        Student(
            final String grade,
            final String hrClass,
            final String hrNameabbv,
            final String attendno,
            final String schregno,
            final String hrName,
            final String name,
            final String sex,
            final String sexName,
            final String entdiv,
            final String entdivName,
            final String entdate,
            final String grddiv,
            final String grddivName,
            final String grddate,
            final String transfercd1,
            final String transfername1,
            final String transferreason1,
            final String transferSdate1,
            final String transferEdate1,
            final String transfercd2,
            final String transfername2,
            final String transferreason2,
            final String transferSdate2,
            final String transferEdate2,
            final String coursecd,
            final String majorcd,
            final String majorname,
            final String coursecode,
            final String coursecodename,
            final Integer regdYearCount 
        ) {
            _grade = grade;
            _hrClass = hrClass;
            _hrNameabbv = hrNameabbv;
            _attendno = attendno;
            _schregno = schregno;
            _hrName = hrName;
            _name = name;
            _sex = sex;
            _sexName = sexName;
            _entdiv = entdiv;
            _entdivName = entdivName;
            _entdate = entdate;
            _grddiv = grddiv;
            _grddivName = grddivName;
            _grddate = grddate;
            _transfercd1 = transfercd1;
            _transfername1 = transfername1;
            _transferreason1 = transferreason1;
            _transferSdate1 = transferSdate1;
            _transferEdate1 = transferEdate1;
            _transfercd2 = transfercd2;
            _transfername2 = transfername2;
            _transferreason2 = transferreason2;
            _transferSdate2 = transferSdate2;
            _transferEdate2 = transferEdate2;
            _coursecd = coursecd;
            _majorcd = majorcd;
            _majorname = majorname;
            _coursecode = coursecode;
            _coursecodename = coursecodename;
            _regdYearCount = regdYearCount;
            _subclassScoreMap = new TreeMap();
            _subclassMarkMap = new TreeMap();
            _subclassAvgTargetMap = new TreeMap();
            _subclassBdAvgMap = new TreeMap();
//            _subclassAttendance = new TreeMap();
            _hanteiMap = new HashMap();
        }

        public boolean hasFunintei() {
            final Set funintei = new TreeSet();
            final Set hyotei1 = new TreeSet();
            for (final Iterator it = _subclasscdGetCreditMap.entrySet().iterator() ;it.hasNext();) {
                final Map.Entry e = (Map.Entry) it.next();
                final String subclasscd = (String) e.getKey();
                final String getCredit = (String) e.getValue();
                if (null == getCredit || NumberUtils.isDigits(getCredit) && Integer.parseInt(getCredit) == 0) {
                    funintei.add(subclasscd);
                }
                if (_subclasscdHyoteiMap.containsKey(subclasscd)) {
                	final String hyotei = (String) _subclasscdHyoteiMap.get(subclasscd);
                    if (null == hyotei || NumberUtils.isDigits(hyotei) && Integer.parseInt(hyotei) <= 1) {
                    	hyotei1.add(subclasscd);
                    }
                }
            }
            if (!funintei.isEmpty() || !hyotei1.isEmpty()) {
                log.info(" " + _schregno + " funintei :" + funintei + ", hyotei1 = " + hyotei1);
                return true;
            }
            return false;
        }

        public String coursecdMajorcd() {
            return _coursecd + _majorcd;
        }

        public String course() {
            return _coursecd + _majorcd + _coursecode;
        }

        private String getHrNameabbvAttendnoCd(final Param param) {
            return
            defstr(_hrNameabbv) + "-" +
            defstr((NumberUtils.isDigits(_attendno)) ? attendnodf.format(Integer.parseInt(_attendno)) : _attendno);
        }

        private String getHrclassAttendnoCd() {
            return
            defstr((NumberUtils.isDigits(_hrClass)) ? String.valueOf(Integer.parseInt(_hrClass)) : _hrClass) +
            defstr((NumberUtils.isDigits(_attendno)) ? attendnodf.format(Integer.parseInt(_attendno)) : _attendno);
        }

        public String getSubclassAvg(final String subclasscd) {
            final String rtn;
            rtn = toStr(_subclassBdAvgMap.get(subclasscd));
            return rtn;
        }

        public void setHantei(final int dataDiv, final String hantei) {
            _hanteiMap.put(new Integer(dataDiv), hantei);
        }

        public String getHantei(final int dataDiv) {
            return (String) _hanteiMap.get(new Integer(dataDiv));
        }

        /**
         *
         * @param flg 1:学年開始日 2:指定日付 0:どちらか
         * @return
         */
        public boolean isTennyu(final String date) {
            final String cd1 = "4"; // 転入
            final String cd2 = "5"; // 編入
            return (cd1.equals(_entdiv) || cd2.equals(_entdiv)) && null != date && date.compareTo(_entdate) >= 0;
        }

        /**
         *
         * @param flg 1:学年開始日 2:指定日付 0:どちらか
         * @return
         */
        public boolean isRyugaku(final int flg) {
            final String cd = "1"; // 留学
            return flg == 1 && cd.equals(_transfercd1)  || flg == 2 && cd.equals(_transfercd2) || flg == 0 && (cd.equals(_transfercd1) || cd.equals(_transfercd2));
        }

        /**
         *
         * @param flg 1:学年開始日 2:指定日付 0:どちらか
         * @return
         */
        public boolean isKyugaku(final int flg) {
            final String cd = "2"; // 休学
            return flg == 1 && cd.equals(_transfercd1)  || flg == 2 && cd.equals(_transfercd2) || flg == 0 && (cd.equals(_transfercd1) || cd.equals(_transfercd2));
        }

        /**
         *
         * @param flg 1:学年開始日 2:指定日付 0:どちらか
         * @return
         */
        public boolean isRyugakuKyugaku(final int flg) {
            return isRyugaku(flg) || isKyugaku(flg);
        }

        public boolean isTenhennyuugaku(final Param param) {
            final boolean isTenhennyuugaku = ("4".equals(_entdiv) || "5".equals(_entdiv)) && (null == _entdate || param._yearSdate.compareTo(_entdate) <= 0);
//            if (null != _entdate) {
//                log.info(" " + toString() + " Tenhennyuugaku " + _entdate + " ( " + param._yearSdate + ")");
//            }
            return isTenhennyuugaku;
        }

        public boolean isFukugaku(final String date) {
            if (null != _entGrdHistComebackDatComebackDateMin && null != date) {
                return _entGrdHistComebackDatComebackDateMin.compareTo(date) <= 0;
            }
            return false;
        }

        public boolean isNyugakuZumiAtDate(final Param param, final String date) {
            final boolean isNyugakuaZumiAtDate = null != _entdate && (null == date || _entdate.compareTo(date) <= 0);
            if (!isNyugakuaZumiAtDate) {
            	log.info(" not nyugaku " + _entdate + " at " + date);
            }
            return isNyugakuaZumiAtDate;
        }

        public boolean isJoseki(final Param param, final String date) {
            final boolean isJoseki = null != _grddiv && !"4".equals(_grddiv) && null != _grddate && ((param._yearSdate.compareTo(_grddate) <= 0 && (null == date || _grddate.compareTo(date) <= 0)));
//            if (isJoseki) {
//                log.debug(" " + toString() + " joseki = " + isJoseki + " : " + _grddiv + " / "   + _grddate + " ( " + param._yearSdate + ", " + date + ")");
//            }
            return isJoseki;
        }

        public String toString() {
            return "Student(" + _schregno + ")";
        }

        public static List filterCourses(final Param param, final List studentList, final Collection courses) {
            final List list = new ArrayList();
            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if ("1".equals(param._kyugaku) && student.isKyugaku(2)) {
                    continue;
                }
                if (courses.contains(student.course())) {
                    list.add(student);
                }
            }
            return list;
        }

        public static class ComparatorByAvg implements Comparator {
            public int compare(final Object o1, final Object o2) {
                final Student s1 = (Student) o1;
                final Student s2 = (Student) o2;
                final String avg1 = s1.getSubclassAvg(SUBCLASSCD999999);
                final String avg2 = s2.getSubclassAvg(SUBCLASSCD999999);
                int cmp = 0;
                if (null != avg1 || null != avg2) {
                    if (null == avg1) {
                        cmp = 1;
                    } else if (null == avg2) {
                        cmp = -1;
                    } else {
                        cmp = - new BigDecimal(avg1).compareTo(new BigDecimal(avg2));
                    }
                }
                if (0 != cmp) {
                    return cmp;
                }
                cmp = (s1._grade + s1._hrClass + s1._attendno).compareTo(s2._grade + s2._hrClass + s2._attendno);
                return cmp;
            }
        }

        public static class ComparatorByDataDiv implements Comparator {
        	final int _dataDiv;
            public ComparatorByDataDiv(final int dataDiv) {
            	_dataDiv = dataDiv;
            }
            public int compare(final Object o1, final Object o2) {
                final Student s1 = (Student) o1;
                final Student s2 = (Student) o2;
                int cmp = 0;
            	switch (_dataDiv) {
            	case DIV_3_I_KEKKSEKIGOKEI:
            		int kekka1 = 0;
            		if (null != s1._attendance) {
            			kekka1 = s1._attendance.kekkaGokei();
            		}
            		int kekka2 = 0;
            		if (null != s2._attendance) {
            			kekka2 = s2._attendance.kekkaGokei();
            		}
            		cmp = new Integer(kekka1).compareTo(new Integer(kekka2)); // 欠課数合計の少ない昇順
            		break;
            	case DIV_3_RO_NENKANHEIKIN_TOTAL:
            	case DIV_3_HA_SHUTOKU_FUNINTEI:
            	case DIV_3_NI_NENKANHEIKIN_KAMOKU:
            	case DIV_3_HO_JOKI_1_AND_OTHER_2:
            	case DIV_3_HE_NENKANHEIKIN_TOTAL2:
            		final String avg1 = (String) s1._subclassBdAvgMap.get(SUBCLASSCD999999);
            		final String avg2 = (String) s2._subclassBdAvgMap.get(SUBCLASSCD999999);
            		if (NumberUtils.isNumber(avg1) || NumberUtils.isNumber(avg2)) {
            			if (!NumberUtils.isNumber(avg1)) {
            				return 1;
            			} else if (!NumberUtils.isNumber(avg2)) {
            				return -1;
            			}
            			return - new BigDecimal(avg1).compareTo(new BigDecimal(avg2)); // 平均点の高い順
            		}
            		break;
            	case DIV_3_TO_SHUSSEKISUBEKI:
            	case DIV_3_CHI_SHUSSEKISUBEKI2:
            		int absence1 = 0;
            		if (null != s1._attendance) {
            			absence1 = s1._attendance._absence;
            		}
            		int absence2 = 0;
            		if (null != s2._attendance) {
            			absence2 = s2._attendance._absence;
            		}
            		cmp = new Integer(absence1).compareTo(new Integer(absence2)); // 欠席の少ない順
            		break;
            	}
                if (0 != cmp) {
                    return cmp;
                }
                cmp = (s1._grade + s1._hrClass + s1._attendno).compareTo(s2._grade + s2._hrClass + s2._attendno);
                return cmp;
            }
        }
    }

    /**
     * 1日出欠データ
     */
    private static class Attendance {
    	final String _schregno;

        final int _lesson;
        /** 忌引 */
        final int _mourning;
        /** 出停 */
        final int _suspend;
        /** 留学 */
        final int _abroad;
        /** 出席すべき日数 */
        final int _mlesson;
        /** 欠席 */
        final int _absence;
        final int _attend;
        /** 遅刻 */
        final int _late;
        /** 早退 */
        final int _early;

        final int _tochuKekka;

        public Attendance(
        		final String schregno,
                final int lesson,
                final int mourning,
                final int suspend,
                final int abroad,
                final int mlesson,
                final int absence,
                final int attend,
                final int late,
                final int early,
                final int tochuKekka
        ) {
        	_schregno = schregno;
            _lesson = lesson;
            _mourning = mourning;
            _suspend = suspend;
            _abroad = abroad;
            _mlesson = mlesson;
            _absence = absence;
            _attend = attend;
            _late = late;
            _early = early;
            _tochuKekka = tochuKekka;
        }

        public int kekkaGokei() {
            return _absence + _tochuKekka + _late + _early;
        }

        public String toString() {
            return "[lesson=" + _lesson +
            ",mlesson=" + _mlesson +
            ",mourning=" + _mourning +
            ",suspend=" + _suspend +
            ",abroad=" + _abroad +
            ",absence=" + _absence +
            ",attend=" + _attend +
            ",late=" + _late +
            ",leave=" + _early;
        }

        public static boolean isKaikin(final Attendance att) {
            if (null == att) {
                return false;
            }
            return att._absence == 0 && att._late == 0 && att._early == 0;
        }

    }

    private static class HrClass {
        final String _grade;
        final String _hrClass;
        final String _hrNameabbv;
        final List _studentList;
//        String _avgAvg;

        HrClass(
            final String grade,
            final String hrClass,
            final String hrNameabbv
        ) {
            _grade = grade;
            _hrClass = hrClass;
            _hrNameabbv = hrNameabbv;
            _studentList = new ArrayList();
        }

        public String getCode() {
            return _grade + _hrClass;
        }

        public static List getZaisekiList(final List studentList, final String sex, final Param param, final String date) {
            final List list = new ArrayList();
            for (final Iterator it =studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if ((null == sex || sex.equals(student._sex)) && !student.isJoseki(param, date) && student.isNyugakuZumiAtDate(param, date)) {
                    list.add(student);
                }
            }
            return list;
        }

        public static List getStudentCountRyugakuSex(final List studentList, final int flg, final String sex) {
            final List list = new ArrayList();
            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if (null == sex || sex.equals(student._sex)) {
                    if (student.isRyugaku(flg)) {
                        list.add(student);
                    }
                }
            }
            return list;
        }

        public static List getStudentCountKyugakuSex(final List studentList, final int flg, final String sex) {
            final List list = new ArrayList();
            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if (null == sex || sex.equals(student._sex)) {
                    if (student.isKyugaku(flg)) {
                        list.add(student);
                    }
                }
            }
            return list;
        }

        public static List getStudentCountRyugakuKyugakuSex(final List studentList, final int flg, final String sex) {
            final List list = new ArrayList();
            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if (null == sex || sex.equals(student._sex)) {
                    if (student.isRyugakuKyugaku(flg)) {
                        list.add(student);
                    }
                }
            }
            return list;
        }

        public static List getStudentFukugaku(final List studentList, final String date) {
            final List list = new ArrayList();
            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if (student.isFukugaku(date)) {
                    list.add(student);
                }
            }
            return list;
        }

        public static List getTennyu(final List studentList, final Param param, final String date) {
            final List list = new ArrayList();
            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if (student.isTennyu(date)) {
                    list.add(student);
                }
            }
            return list;
        }

        public static List getTengakuTaigaku(final List studentList, final Param param, final String date) {
            final List list = new ArrayList();
            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if (student.isJoseki(param, date) && !"1".equals(student._grddiv)) {
                    list.add(student);
                }
            }
            return list;
        }

        public static HrClass getHrClass(final String grade, final String hrClass, final List hrClassList) {
            for (final Iterator it = hrClassList.iterator(); it.hasNext();) {
                HrClass hrclass = (HrClass) it.next();
                if (hrclass._grade.equals(grade) && hrclass._hrClass.equals(hrClass)) {
                    return hrclass;
                }
            }
            return null;
        }

        public static List getHrClassList(final List studentList) {
            final List list = new ArrayList();
            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if (null == getHrClass(student._grade, student._hrClass, list)) {
                    list.add(new HrClass(student._grade, student._hrClass, student._hrNameabbv));
                }
                final HrClass hrclass = getHrClass(student._grade, student._hrClass, list);
                hrclass._studentList.add(student);
            }
            return list;
        }

        public String toString() {
            return "HrClass(" + _grade + _hrClass + ":" + _hrNameabbv + ")";
        }
    }

    private static class Subclass implements Comparable {
        final String _subclasscd;
        final String _classabbv;
        final String _subclassname;
        final String _subclassabbv;
        final boolean _isSaki;
        final boolean _isMoto;
        final HashMap _courseCreditsMap; // Map<String (COURSECD + MAJORCD + COURSECODE), Integer (CREDIT)>
        final HashMap _creditsCourseMap; // Map<Integer (CREDIT), Set<String (COURSECD + MAJORCD + COURSECODE)>>
        String _subclassScoreAllNull;
        Subclass(
            final String subclasscd,
            final String classabbv,
            final String subclassname,
            final String subclassabbv,
            final boolean isSaki,
            final boolean isMoto
        ) {
            _subclasscd = subclasscd;
            _classabbv = classabbv;
            _subclassname = subclassname;
            _subclassabbv = subclassabbv;
            _courseCreditsMap = new HashMap();
            _creditsCourseMap = new HashMap();
            _isSaki = isSaki;
            _isMoto = isMoto;
        }

        public int compareTo(final Object o) {
            if (!(o instanceof Subclass)) return -1;
            final Subclass s = (Subclass) o;
            return _subclasscd.compareTo(s._subclasscd);
        }

        public String toString() {
            return "Subclass(" + _subclasscd + ":" + _subclassname + ")";
        }
    }

    private static String sishaGonyu(final BigDecimal avg) {
        return null == avg ? null : avg.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    /**
     * 生徒の科目の得点
     */
    private static class SubclassScore {
        final Student _student;
        final Subclass _subclass;
        final String _score;
        final BigDecimal _avg;

        SubclassScore(
            final Student student,
            final Subclass subclass,
            final String score,
            final BigDecimal avg
        ) {
            _student = student;
            _subclass = subclass;
            _score = score;
            _avg = avg;
        }

        public static String getSubclassScoreSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append("   WITH REL_COUNT AS (");
            stb.append("   SELECT SUBCLASSCD");
            stb.append("     , CLASSCD ");
            stb.append("     , SCHOOL_KIND ");
            stb.append("     , CURRICULUM_CD ");
            stb.append("     , COUNT(*) AS COUNT ");
            stb.append("          FROM RELATIVEASSESS_MST ");
            stb.append("          WHERE GRADE = '" + param._grade + "' AND ASSESSCD = '3' ");
            stb.append("   GROUP BY SUBCLASSCD");
            stb.append("     , CLASSCD ");
            stb.append("     , SCHOOL_KIND ");
            stb.append("     , CURRICULUM_CD ");
            stb.append("   ) ");

            stb.append("   , SCORE_ALL_NULL_SUBCLASS AS (");
            stb.append("   SELECT T_SCORE.SUBCLASSCD");
            stb.append("     , T_SCORE.SEMESTER, T_SCORE.TESTKINDCD, T_SCORE.TESTITEMCD, T_SCORE.SCORE_DIV ");
            stb.append("     , T_SCORE.CLASSCD ");
            stb.append("     , T_SCORE.SCHOOL_KIND ");
            stb.append("     , T_SCORE.CURRICULUM_CD ");
            stb.append("    FROM RECORD_SCORE_DAT T_SCORE ");
//            if ("9".equals(param._semester) && HYOTEI_TESTCD.equals(param._testcd)) {
//                stb.append("     LEFT JOIN RECORD_PROV_FLG_DAT PROV ON PROV.YEAR = T_SCORE.YEAR ");
//                stb.append("         AND PROV.CLASSCD = T_SCORE.CLASSCD ");
//                stb.append("         AND PROV.SCHOOL_KIND = T_SCORE.SCHOOL_KIND ");
//                stb.append("         AND PROV.CURRICULUM_CD = T_SCORE.CURRICULUM_CD ");
//                stb.append("         AND PROV.SUBCLASSCD = T_SCORE.SUBCLASSCD ");
//                stb.append("         AND PROV.SCHREGNO = T_SCORE.SCHREGNO ");
//            }
            stb.append("    WHERE T_SCORE.YEAR = '" + param._year + "' ");
//            stb.append("        AND T_SCORE.SEMESTER = '" + param._semester + "' ");
//            stb.append("        AND T_SCORE.TESTKINDCD || T_SCORE.TESTITEMCD || T_SCORE.SCORE_DIV = '" + param._testcd + "' ");
            stb.append("        AND T_SCORE.TESTKINDCD <> '99' ");
            stb.append("        AND T_SCORE.SCORE_DIV <> '09' ");
            stb.append("   GROUP BY T_SCORE.SUBCLASSCD");
            stb.append("     , T_SCORE.SEMESTER, T_SCORE.TESTKINDCD, T_SCORE.TESTITEMCD, T_SCORE.SCORE_DIV ");
            stb.append("     , T_SCORE.CLASSCD ");
            stb.append("     , T_SCORE.SCHOOL_KIND ");
            stb.append("     , T_SCORE.CURRICULUM_CD ");
            stb.append("    HAVING MAX(T_SCORE.SCORE) IS NULL ");
//            if ("9".equals(param._semester) && HYOTEI_TESTCD.equals(param._testcd)) {
//                if ("1".equals(param._kariHyotei)) {
//                    stb.append("    AND MIN(PROV.PROV_FLG) IS NOT NULL ");
//                } else {
//                    stb.append("    OR MIN(PROV.PROV_FLG) IS NOT NULL ");
//                }
//            }
            stb.append("   ) ");
//            if ("9".equals(param._semester) && HYOTEI_TESTCD.equals(param._testcd)) {
//                getHyoteiDataSql(false, param, stb);
//            }
            stb.append(" SELECT ");
            stb.append("     0 AS TESTFLG, ");
            stb.append("     REGD.SCHREGNO, ");
            stb.append("     T_RANK.SEMESTER || T_RANK.TESTKINDCD || T_RANK.TESTITEMCD || T_RANK.SCORE_DIV AS TESTCD, ");
            stb.append("     T_SCORE.CLASSCD || '-' || T_SCORE.SCHOOL_KIND || '-' || T_SCORE.CURRICULUM_CD || '-' || T_SCORE.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     CLM.CLASSABBV, ");
            stb.append("     SCLM.SUBCLASSNAME, ");
            stb.append("     SCLM.SUBCLASSABBV, ");
            stb.append("     T_RANK.SCORE, ");
            stb.append("     T_RANK.AVG, ");
            stb.append("     T_RANK.GRADE_RANK, ");
            stb.append("     T_RANK.GRADE_AVG_RANK, ");
            stb.append("     T_RANK.CLASS_RANK, ");
            stb.append("     T_RANK.CLASS_AVG_RANK, ");
            stb.append("     T_RANK.COURSE_RANK, ");
            stb.append("     T_RANK.COURSE_AVG_RANK, ");
            stb.append("     T_RANK.MAJOR_RANK, ");
            stb.append("     T_RANK.MAJOR_AVG_RANK, ");
            stb.append("     CASE WHEN T9.SUBCLASSCD IS NOT NULL THEN '1' END AS SCORE_ALL_NULL ");
            stb.append(" FROM SCHREG_REGD_DAT REGD ");
            stb.append(" INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     AND NOT (VALUE(BASE.ENT_DIV, '') IN ('4', '5') AND VALUE(BASE.ENT_DATE, '1900-01-01') > '" + param._edate + "') ");
            stb.append("     AND NOT (VALUE(BASE.GRD_DIV, '') IN ('1', '2', '3') AND VALUE(BASE.GRD_DATE, '9999-12-31') < '" + param._edate + "') ");
            stb.append(" INNER JOIN RECORD_SCORE_DAT T_SCORE ON T_SCORE.YEAR = REGD.YEAR ");
//            stb.append("     AND T_SCORE.SEMESTER = '" + param._semester + "' ");
//            stb.append("     AND T_SCORE.TESTKINDCD || T_SCORE.TESTITEMCD || T_SCORE.SCORE_DIV = '" + param._testcd + "' ");
            stb.append("     AND T_SCORE.TESTKINDCD <> '99' ");
            stb.append("     AND T_SCORE.SCORE_DIV <> '09' ");
            stb.append("     AND T_SCORE.SCHREGNO = REGD.SCHREGNO ");
//            if ("9".equals(param._semester) && HYOTEI_TESTCD.equals(param._testcd)) {
//                stb.append(" LEFT JOIN HYOTEI_DATA T_RANK ON T_RANK.SUBCLASSCD = T_SCORE.CLASSCD || '-' || T_SCORE.SCHOOL_KIND || '-' || T_SCORE.CURRICULUM_CD || '-' || T_SCORE.SUBCLASSCD ");
//                stb.append("     AND T_RANK.SCHREGNO = T_SCORE.SCHREGNO ");
//            } else {
                stb.append(" LEFT JOIN RECORD_RANK_SDIV_DAT T_RANK ON T_RANK.YEAR = T_SCORE.YEAR ");
                stb.append("     AND T_RANK.SEMESTER = T_SCORE.SEMESTER ");
                stb.append("     AND T_RANK.TESTKINDCD = T_SCORE.TESTKINDCD ");
                stb.append("     AND T_RANK.TESTITEMCD = T_SCORE.TESTITEMCD ");
                stb.append("     AND T_RANK.SCORE_DIV = T_SCORE.SCORE_DIV ");
                stb.append("     AND T_RANK.CLASSCD = T_SCORE.CLASSCD ");
                stb.append("     AND T_RANK.SCHOOL_KIND = T_SCORE.SCHOOL_KIND ");
                stb.append("     AND T_RANK.CURRICULUM_CD = T_SCORE.CURRICULUM_CD ");
                stb.append("     AND T_RANK.SUBCLASSCD = T_SCORE.SUBCLASSCD ");
                stb.append("     AND T_RANK.SCHREGNO = T_SCORE.SCHREGNO ");
//            }
            stb.append(" LEFT JOIN SUBCLASS_MST SCLM ON SCLM.SUBCLASSCD = T_SCORE.SUBCLASSCD ");
            stb.append("     AND SCLM.SCHOOL_KIND = T_SCORE.SCHOOL_KIND ");
            stb.append("     AND SCLM.CURRICULUM_CD = T_SCORE.CURRICULUM_CD ");
            stb.append("     AND SCLM.CLASSCD = T_SCORE.CLASSCD ");
//            stb.append(" LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV T4 ON T4.YEAR = T_SCORE.YEAR ");
//            stb.append("     AND T4.SEMESTER = T_SCORE.SEMESTER ");
//            stb.append("     AND T4.TESTKINDCD = T_SCORE.TESTKINDCD ");
//            stb.append("     AND T4.TESTITEMCD = T_SCORE.TESTITEMCD ");
//            stb.append("     AND T4.SCORE_DIV = T_SCORE.SCORE_DIV ");
            stb.append("  LEFT JOIN CLASS_MST CLM ON CLM.CLASSCD = T_SCORE.CLASSCD ");
            stb.append("     AND CLM.SCHOOL_KIND = T_SCORE.SCHOOL_KIND ");
            stb.append("  LEFT JOIN SCORE_ALL_NULL_SUBCLASS T9 ON T9.SUBCLASSCD = T_SCORE.SUBCLASSCD ");
            stb.append("     AND T9.SEMESTER = T_SCORE.SEMESTER ");
            stb.append("     AND T9.TESTKINDCD = T_SCORE.TESTKINDCD ");
            stb.append("     AND T9.TESTITEMCD = T_SCORE.TESTITEMCD ");
            stb.append("     AND T9.SCORE_DIV = T_SCORE.SCORE_DIV ");
            stb.append("     AND T9.CLASSCD = T_SCORE.CLASSCD ");
            stb.append("     AND T9.SCHOOL_KIND = T_SCORE.SCHOOL_KIND ");
            stb.append("     AND T9.CURRICULUM_CD = T_SCORE.CURRICULUM_CD ");
            stb.append(" WHERE ");
            stb.append(" REGD.YEAR = '" + param._year + "' ");
            stb.append(" AND REGD.SEMESTER = '" + (SEMEALL.equals(param._semester) ? param._ctrlSemester : param._semester) + "' ");
            stb.append(" AND REGD.GRADE = '" + param._grade + "' ");
            stb.append(" AND T_SCORE.CLASSCD <= '90' ");
//            stb.append(" UNION ALL ");
//            stb.append(" SELECT ");
//            stb.append("     0 AS TESTFLG, ");
//            stb.append("     REGD.SCHREGNO, ");
//            stb.append("     T_RANK.SEMESTER || T_RANK.TESTKINDCD || T_RANK.TESTITEMCD || T_RANK.SCORE_DIV AS TESTCD, ");
//            stb.append("     T_RANK.SUBCLASSCD AS SUBCLASSCD, ");
//            stb.append("     CAST(NULL AS VARCHAR(1)) AS CLASSABBV, ");
//            stb.append("     CAST(NULL AS VARCHAR(1)) AS SUBCLASSNAME, ");
//            stb.append("     CAST(NULL AS VARCHAR(1)) AS SUBCLASSABBV, ");
//            stb.append("     T_RANK.SCORE, ");
//            stb.append("     T_RANK.AVG, ");
//            stb.append("     T_RANK.GRADE_RANK, ");
//            stb.append("     T_RANK.GRADE_AVG_RANK, ");
//            stb.append("     T_RANK.CLASS_RANK, ");
//            stb.append("     T_RANK.CLASS_AVG_RANK, ");
//            stb.append("     T_RANK.COURSE_RANK, ");
//            stb.append("     T_RANK.COURSE_AVG_RANK, ");
//            stb.append("     T_RANK.MAJOR_RANK, ");
//            stb.append("     T_RANK.MAJOR_AVG_RANK, ");
////            stb.append("     CAST(NULL AS SMALLINT) AS SLUMP_SCORE, ");
////            stb.append("     CAST(NULL AS VARCHAR(1)) AS SLUMP_MARK_CD, ");
////            stb.append("     CAST(NULL AS VARCHAR(1)) AS SLUMP_MARK, ");
//            stb.append("     CAST(NULL AS VARCHAR(1)) AS SCORE_ALL_NULL ");
//            stb.append(" FROM SCHREG_REGD_DAT REGD ");
//            stb.append(" INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
//            stb.append("     AND NOT (VALUE(BASE.ENT_DIV, '') IN ('4', '5') AND VALUE(BASE.ENT_DATE, '1900-01-01') > '" + param._edate + "') ");
//            stb.append("     AND NOT (VALUE(BASE.GRD_DIV, '') IN ('1', '2', '3') AND VALUE(BASE.GRD_DATE, '9999-12-31') < '" + param._edate + "') ");
////            if ("9".equals(param._semester) && HYOTEI_TESTCD.equals(param._testcd)) {
////                stb.append(" INNER JOIN HYOTEI_DATA T_RANK ON T_RANK.SCHREGNO = REGD.SCHREGNO ");
////            } else {
//                stb.append(" INNER JOIN RECORD_RANK_SDIV_DAT T_RANK ON T_RANK.YEAR = REGD.YEAR ");
////                stb.append("     AND T_RANK.SEMESTER = '" + param._semester + "' ");
////                stb.append("     AND T_RANK.TESTKINDCD || T_RANK.TESTITEMCD || T_RANK.SCORE_DIV = '" + param._testcd + "' ");
//                stb.append("     AND T_RANK.TESTKINDCD <> '99' ");
//                stb.append("     AND T_RANK.SCORE_DIV <> '09' ");
//                stb.append("     AND T_RANK.SCHREGNO = REGD.SCHREGNO ");
////            }
//            stb.append(" WHERE ");
//            stb.append(" REGD.YEAR = '" + param._year + "' ");
//            stb.append(" AND REGD.SEMESTER = '" + (SEMEALL.equals(param._semester) ? param._ctrlSemester : param._semester) + "' ");
//            stb.append(" AND REGD.GRADE = '" + param._grade + "' ");
//            stb.append(" AND T_RANK.SUBCLASSCD = '" + SUBCLASSCD999999 + "' ");
            return stb.toString();
        }

        public String toString() {
            return "SubclassScore(" + _subclass + ", " + _score + ", " + _avg + ")";
        }
    }

    public static int recordHeight(final Map record) {
        final int recordHeight;
        final String recordName = (String) record.get("RECORD");
        if (ArrayUtils.contains(new String[] {RECORD1_TAB1_LINE, RECORD1_TAB2_LINE}, recordName)) {
            recordHeight = 18;
        } else if (ArrayUtils.contains(new String[] {RECORD1}, recordName)) {
            recordHeight = 80;
        } else {
            recordHeight = 70;
        }
        return recordHeight;
    }

    private static List getPrintLinePageList(final List list) {
        final List rtn = new ArrayList();
        final int subformHeight = 3079 - 504;
        int currentHeight = 0;
        List current = null;
//        for (final Iterator it = list.iterator(); it.hasNext();) {
//            final Object o = it.next();
//
//            int recordHeight = 0;
//            if (o instanceof Map) {
//                recordHeight = recordHeight((Map) o);
//            } else if (o instanceof Table) {
//                Table t = (Table) o;
//                for (final Iterator rit = t.toRecordList().iterator(); rit.hasNext();) {
//                    final Map record = (Map) rit.next();
//                    recordHeight += recordHeight(record);
//                }
//            } else {
//                throw new IllegalArgumentException("unknown object : " + o);
//            }
//
//            if (current == null || currentHeight > 0 && currentHeight + recordHeight > subformHeight) {
//                if (null != current) {
//                    log.info(" page (" + rtn.size() + ") : current = " + currentHeight + ", recordHeight = " + recordHeight + " / subformHeight = " + subformHeight);
//                }
//                current = new ArrayList();
//                rtn.add(current);
//                currentHeight = 0;
//            }
//            current.add(o);
//            currentHeight += recordHeight;
//        }
//
//        return rtn;

        final List concatList = new ArrayList();
        for (int i = 0; i < list.size(); i++) {
        	final boolean isLast = i == list.size() - 1;
            final Object o = list.get(i);
            if (o instanceof Map) {
                concatList.add(o);
            } else if (o instanceof Table) {
                Table t = (Table) o;
                concatList.addAll(t.toRecordList(isLast));
            }
        }

        for (final Iterator it = concatList.iterator(); it.hasNext();) {
            final Map record = (Map) it.next();
            int recordHeight = recordHeight(record);

            if (current == null || currentHeight > 0 && currentHeight + recordHeight > subformHeight) {
//                if (null != current) {
//                    log.info(" page (" + rtn.size() + ") : current = " + currentHeight + ", recordHeight = " + recordHeight + " / subformHeight = " + subformHeight);
//                }
                current = new ArrayList();
                rtn.add(current);
                currentHeight = 0;
            }
            current.add(record);
            currentHeight += recordHeight;
        }

        return rtn;
    }


    private static List getPageList(final List list, final int count) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= count) {
                current = new ArrayList();
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

    private static String getText(final List list, final String comma) {
        String cm = "";
        final StringBuffer stb = new StringBuffer();
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null != o && !StringUtils.isBlank(o.toString())) {
                stb.append(cm).append(o.toString());
                cm = comma;
            }
        }
        return stb.toString();
    }

    private static String getSum(final Collection elems) {
        if (0 == elems.size()) return null;
        BigDecimal sum = new BigDecimal(0);
        for (final Iterator it = elems.iterator(); it.hasNext();) {
            final BigDecimal e = (BigDecimal) it.next();
            sum = sum.add(e);
        }
        return String.valueOf(sum);
    }

    private static String getAvg(final Collection elems, final int scale) {
        if (0 == elems.size()) return null;
        log.debug(" elems = " + elems);
        String sum = getSum(elems);
        if (null == sum) return null;
        return new BigDecimal(sum).divide(new BigDecimal(elems.size()), scale, BigDecimal.ROUND_HALF_UP).toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 74345 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _year;
        final String _semester;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _printDate;
        final String _grade;
        final String _courseCode;
//        final String _testcd;
        final String _sdate;
        final String _edate;
        final int _yuryo;
        final int _fushin;
        final String _kariHyotei; // 990009:学年評定の場合
//        final String _outputKijun; // 1:総計 2:平均点
        final String _useCurriculumcd;
//        final TestItem _testItem;
        private String _yearSdate;
        final String _cmd;
        final String _setSchoolKind;
        final String _dosubunpuCourse;
        final String _kyugaku;
        final String _gradeName2;
        final String _schoolKind;
        final String _gradeCd;
        final String _a023Abbv1;
        final String _d008Namecd1;
        final String _schregnoKaikin6;
        final String _schregnoKaikin3;
        final String _schregnoKaikin1;
        final boolean _isPrintKaikin3;
        final boolean _isPrintKaikin6;
        final boolean _isSeireki;
        final String _nendo;

        final BigDecimal _2_i_avg = new BigDecimal(80);
        final int _2_i_kessekiGokeiMax = 10;
        final int _3_i_kessekiGokeiMin = 30;
        final BigDecimal _3_ro_total_avg = new BigDecimal(40);
        final BigDecimal _3_ni_kamoku_avg = new BigDecimal(30);
        final Ratio _3_ni_seiseki_ratio = new Ratio(1, 4);
        final BigDecimal _3_he_total_avg = new BigDecimal(30);
        final Ratio _3_to_shussekisubeki_ratio = new Ratio(1, 4);
        final Ratio _3_chi_shussekisubeki_ratio = new Ratio(1, 3);

        private Map _subclassMap;

//        private Map _averageDatMap = Collections.EMPTY_MAP;
        final Map _attendParamMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _ctrlDate = StringUtils.replace(request.getParameter("CTRL_DATE"), "/", "-");
            _semester = request.getParameter("SEMESTER");
            _grade = request.getParameter("GRADE");
            _courseCode = request.getParameter("COURSE");

//            _testcd = request.getParameter("TEST_CD");
            _edate = request.getParameter("EDATE").replace('/', '-');
            _sdate = request.getParameter("SDATE").replace('/', '-');
            _kariHyotei = request.getParameter("KARI_HYOTEI");
//            _outputKijun = request.getParameter("OUTPUT_KIJUN");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _yearSdate = getYearSdate(db2);
            _cmd = request.getParameter("cmd");
            _subclassMap = getSubclassMap(db2);
            _yuryo = toInt(request.getParameter("YURYO"), 0);
            _fushin = toInt(request.getParameter("FUSHIN"), 0);
//            _dateDiv = request.getParameter("DATE_DIV");
//            _date = request.getParameter("DATE").replace('/', '-');
//            _ctrlYear = request.getParameter("CTRL_YEAR");
//            _ctrlDate = request.getParameter("CTRL_DATE");
            _setSchoolKind = request.getParameter("setSchoolKind");
            _dosubunpuCourse = request.getParameter("DOSUBUPU_COURSE");
            _kyugaku = request.getParameter("KYUGAKU");

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");

//            _testItem = getTestKindItem(db2);
            _gradeName2 = getGdat(db2, "GRADE_NAME2");
            _schoolKind = getGdat(db2, "SCHOOL_KIND");
            _gradeCd = getGdat(db2, "GRADE_CD");
            _a023Abbv1 = getA023Abbv1(db2, _schoolKind);
            _schregnoKaikin6 = request.getParameter("SCHREGNO_KAIKIN6");
            _schregnoKaikin3 = request.getParameter("SCHREGNO_KAIKIN3");
            _schregnoKaikin1 = request.getParameter("SCHREGNO_KAIKIN1");
            _isPrintKaikin6 = "H".equals(_schoolKind) && "03".equals(_gradeCd);
            _isPrintKaikin3 = "03".equals(_gradeCd);
            _isSeireki = getSeireki(db2);
            _nendo = _isSeireki ? _year + "年度" : KNJ_EditDate.gengou(db2, Integer.parseInt(_year)) + "年度";

            final String tmpD008Cd = "D" + _schoolKind + "08";
            String d008Namecd2CntStr = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT COUNT(*) FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = '" + tmpD008Cd + "' "));
            int d008Namecd2Cnt = Integer.parseInt(StringUtils.defaultIfEmpty(d008Namecd2CntStr, "0"));
            _d008Namecd1 = d008Namecd2Cnt > 0 ? tmpD008Cd : "D008";

            
            if (_isSeireki) {
                final Calendar cal = Calendar.getInstance();
                final int year = cal.get(Calendar.YEAR);
                _printDate = year + "年度" + KNJ_EditDate.h_format_JP_MD(_ctrlDate);
            } else {
                _printDate = KNJ_EditDate.h_format_JP(db2, _ctrlDate);
            }
        }

        public Map getSemesterMap(final DB2UDB db2, final String year) {

            final String sql = " SELECT * FROM SEMESTER_MST WHERE YEAR = '" + year + "' ";

            return KnjDbUtils.getKeyMap(KnjDbUtils.query(db2, sql), "SEMESTER");
        }

        public List beforeYearList(final int count) {
            final List yearList = new ArrayList();
            for (int i = 0; i < count; i++) {
                final String year = String.valueOf(Integer.parseInt(_year) - i);
                yearList.add(year);
            }
            return yearList;
        }

        private static int toInt(final String s, final int defaultInt) {
            return NumberUtils.isNumber(s) ? Integer.parseInt(s) : defaultInt;
        }

        /**
         * 名称マスタ NAMECD1='Z010' NAMECD2='00'読込
         */
        private String setZ010Name1(DB2UDB db2) {
            String name1 = null;
            name1 = KnjDbUtils.getOne(KnjDbUtils.query(db2,  "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' "));
            return name1;
        }

        private boolean getSeireki(final DB2UDB db2) {
            final String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z012' AND NAMECD2 = '00'";
            boolean isSeireki = "2".equals(KnjDbUtils.getOne(KnjDbUtils.query(db2, sql)));
            return isSeireki;
        }

        private String getA023Abbv1(final DB2UDB db2, final String schoolKind) {

            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     ABBV1 ");
            stb.append(" FROM NAME_MST T1 ");
            stb.append(" WHERE T1.NAMECD1 = 'A023' ");
            stb.append("   AND T1.NAME1 = '" + schoolKind + "' ");

            String rtn = KnjDbUtils.getOne(KnjDbUtils.query(db2, stb.toString()));

            return rtn;
        }

        private String getGdat(final DB2UDB db2, final String field) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     " + field + " ");
            stb.append(" FROM SCHREG_REGD_GDAT T1 ");
            stb.append(" WHERE T1.YEAR = '" + _year + "' ");
            stb.append("   AND T1.GRADE = '" + _grade + "' ");

            String rtn = KnjDbUtils.getOne(KnjDbUtils.query(db2, stb.toString()));

            return rtn;
        }

        private String getYearSdate(DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.SDATE ");
            stb.append(" FROM V_SEMESTER_GRADE_MST T1 ");
            stb.append(" WHERE T1.YEAR = '" + _year + "' ");
            stb.append("   AND T1.SEMESTER = '9' ");
            stb.append("   AND T1.GRADE = '" + _grade + "' ");

            String yearSdate = KnjDbUtils.getOne(KnjDbUtils.query(db2, stb.toString()));

            if (null == yearSdate) {
                yearSdate = _year + "-04-01";
            }
            return yearSdate;
        }

        private Map getSubclassMap(DB2UDB db2) {
            Map map = new HashMap();
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" WITH REPL AS ( ");
                stb.append(" SELECT DISTINCT '1' AS DIV, COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD AS SUBCLASSCD FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _year + "' ");
                stb.append(" UNION ");
                stb.append(" SELECT DISTINCT '2' AS DIV, ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD AS SUBCLASSCD FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _year + "' ");
                stb.append(" ) ");
                stb.append(" SELECT ");
                stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("     T2.CLASSABBV, ");
                stb.append("     T1.SUBCLASSNAME, ");
                stb.append("     T1.SUBCLASSABBV, ");
                stb.append("     CASE WHEN L1.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_SAKI, ");
                stb.append("     CASE WHEN L2.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_MOTO ");
                stb.append(" FROM SUBCLASS_MST T1 ");
                stb.append(" LEFT JOIN REPL L1 ON L1.DIV = '1' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L1.SUBCLASSCD ");
                stb.append(" LEFT JOIN REPL L2 ON L2.DIV = '2' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L2.SUBCLASSCD ");
                stb.append(" LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD ");
                stb.append("   AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");

                log.debug(" subclass sql ="  + stb.toString());

                for (final Iterator it = KnjDbUtils.query(db2, stb.toString()).iterator(); it.hasNext();) {
                    final Map rs = (Map) it.next();
                    final String subclasscd = KnjDbUtils.getString(rs, "SUBCLASSCD");
                    final String classabbv = KnjDbUtils.getString(rs, "CLASSABBV");
                    final String subclassname = KnjDbUtils.getString(rs, "SUBCLASSNAME");
                    final String subclassabbv = KnjDbUtils.getString(rs, "SUBCLASSABBV");
                    final boolean isSaki = "1".equals(KnjDbUtils.getString(rs, "IS_SAKI"));
                    final boolean isMoto = "1".equals(KnjDbUtils.getString(rs, "IS_MOTO"));
                    map.put(subclasscd, new Subclass(subclasscd, classabbv, subclassname, subclassabbv, isSaki, isMoto));
                }

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            }
            return map;
        }
    }
}

// eof

