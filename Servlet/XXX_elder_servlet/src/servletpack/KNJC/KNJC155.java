/*
 * $Id: 99bedb5d3a226df05deedd8a19a3c8686a5d68a3 $
 *
 * 作成日: 2018/05/09
 * 作成者: tawada
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJC;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.CsvUtils;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJC155 {

    private static final Log log = LogFactory.getLog(KNJC155.class);

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

        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            if (!"csv".equals(_param._cmd)) {
                response.setContentType("application/pdf");

                svf.VrInit();
                svf.VrSetSpoolFileStream(response.getOutputStream());
            }

            _hasData = false;

            if ("csv".equals(_param._cmd)) {
                final String filename = "授業出席状況表" + ".csv";
                CsvUtils.outputLines(log, response, filename, getCsvOutputLine(db2));
            } else {
                printMain(db2, svf);
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (!"csv".equals(_param._cmd)) {
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

    private static String add(final String num1, final String num2) {
        if (!NumberUtils.isNumber(num1)) return num2;
        if (!NumberUtils.isNumber(num2)) return num1;
        return String.valueOf((int) Double.parseDouble(num1) + Double.parseDouble(num2));
    }

    private static String intString(final String num1) {
        if (!NumberUtils.isNumber(num1)) {
            return num1;
        }
        return String.valueOf((int) Double.parseDouble(num1));
    }

    private List getCsvOutputLine(final DB2UDB db2) {

        final List printList = schregData.getSchList(db2, _param);
        final Set executeDateSet = new TreeSet();

        final List m001Name1List = KnjDbUtils.getColumnDataList(_param._m001List, "NAME1");

        for (final Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final schregData schData = (schregData) iterator.next();

            schData.load(db2, _param);
        }

        for (final Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final schregData schData = (schregData) iterator.next();
            executeDateSet.addAll(schData._executeDateList);
        }
        final List lines = new ArrayList();
        final List header = CsvUtils.newLine(lines);
        header.addAll(Arrays.asList(new String[] {"クラス", "番号", "学籍番号", "氏名", "かな", "科目コード", "科目名", "必要時間数", "合計出席時間数"}));
        header.addAll(formatDateList(executeDateSet));
        header.addAll(m001Name1List);

        for (final Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final schregData schData = (schregData) iterator.next();
            for (final Iterator it2 = schData._subclassList.iterator(); it2.hasNext();) {
                SubClassData subClassData = (SubClassData) it2.next();

                String setTotalAttend = null;
                for (final Iterator it = schData._totalAttendMap.keySet().iterator(); it.hasNext();) {
                    final String chairSubclassCd = (String) it.next();
                    if (chairSubclassCd.endsWith(subClassData._subClassCd)) {
                        setTotalAttend = add((String) schData._totalAttendMap.get(chairSubclassCd), setTotalAttend);
                    }
                }

                final List lineData = new ArrayList(Arrays.asList(new String[] {schData._hrNameAbbv, schData._attendno, schData._schregNo, schData._name, schData._nameKana, subClassData._subClassCd, subClassData._subClassName, subClassData._schSeqAll, StringUtils.defaultString(intString(setTotalAttend), "0")}));
                for (final Iterator dateIt = executeDateSet.iterator(); dateIt.hasNext();) {
                    final String excuteDate = (String) dateIt.next();

                    String setAttendTime = null;
                    for (Iterator it4 = schData._schedAttendMap.keySet().iterator(); it4.hasNext();) {
                        final String chairSubclassExcute = (String) it4.next();
                        //final String chairCd         = StringUtils.split(chairSubclassExcute ,"_")[0];
                        final String subclassCd      = StringUtils.split(chairSubclassExcute ,"_")[0];
                        final String keyExcute       = StringUtils.split(chairSubclassExcute ,"_")[1];
                        final String key = subclassCd;
                        if (!excuteDate.equals(keyExcute) || !key.endsWith(subClassData._subClassCd)) {
                            continue;
                        }

                        setAttendTime = add((String) schData._schedAttendMap.get(key + "_" + excuteDate), setAttendTime);
                    }
                    lineData.add(intString(setAttendTime));

                }

                for (final Iterator it = _param._m001List.iterator(); it.hasNext();) {
                    final Map row = (Map) it.next();
                    final String namecd2 = KnjDbUtils.getString(row, "NAMECD2");

                    String setAttendTime = null;
                    for (Iterator it4 = schData._schedAttendMap.keySet().iterator(); it4.hasNext();) {
                        final String chairSubclassExcute = (String) it4.next();
                        //final String chairCd         = StringUtils.split(chairSubclassExcute ,"_SCHOOLINGKINDCD")[0];
                        final String subclassCd      = StringUtils.split(chairSubclassExcute ,"_SCHOOLINGKINDCD")[0];
                        final String schoolingkindcd = StringUtils.split(chairSubclassExcute ,"_SCHOOLINGKINDCD")[1];
                        final String key = subclassCd;
                        if (!namecd2.equals(schoolingkindcd) || !key.endsWith(subClassData._subClassCd)) {
                            continue;
                        }

                        setAttendTime = add((String) schData._schedAttendMap.get(key + "_SCHOOLINGKINDCD" + namecd2), setAttendTime);
                    }
                    lineData.add(intString(setAttendTime));
                }

                CsvUtils.newLine(lines).addAll(lineData);
            }

        }
        return lines;
    }

    private Collection formatDateList(Collection executeDateSet) {
        final List rtn = new ArrayList();
        for (final Iterator it = executeDateSet.iterator(); it.hasNext();) {
            final String executeDate = (String) it.next();
            rtn.add(KNJ_EditDate.h_format_JP_MD(executeDate));
        }
        return rtn;
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final int maxCol = 25;
        final List printList = schregData.getSchList(db2, _param);
        for (final Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final schregData schData = (schregData) iterator.next();

            svf.VrSetForm("KNJC155.frm", 4);

            svf.VrsOut("TITLE", "授業出席状況表");      // タイトル
            svf.VrsOut("HR_NAME", schData._hrNameAbbv); // クラス
            svf.VrsOut("SCHREGNO", schData._schregNo);  // 学籍番号
            svf.VrsOut("NAME", schData._name);          // 氏名
            final String setMonth = KNJ_EditDate.h_format_JP_M(db2, _param._ctrlDate);
            svf.VrsOut("MONTH", setMonth);              // 日付
            svf.VrsOut("TR_NAME", schData._staffName);  // 担当教員
            svf.VrsOut("TELNO", (String) _param._schoolTelMap.get(schData._schoolKind)); // 連絡先

            schData.load(db2, _param);

            int colNo  = 0;

            for (final Iterator it2 = schData._subclassList.iterator(); it2.hasNext();) {
                final SubClassData subClassData = (SubClassData) it2.next();

                //final String key = subClassData._chairCd + subClassData._subClassCd;
                final String key = subClassData._subClassCd;
                final int rowNo = Integer.parseInt((String) schData._subclasscdRownoMap.get(key));

                final String subNameField = KNJ_EditEdit.getMS932ByteLength(subClassData._subClassName) > 18 ? "2": "1";
                svf.VrsOutn("SUBCLASS_NAME" + subNameField, rowNo, subClassData._subClassName); // 科目
                svf.VrsOutn("MUST_TIME", rowNo, subClassData._schSeqAll);                       // 必要時間数

                final String setTotalAttend = (String) schData._totalAttendMap.get(key);
                svf.VrsOutn("TOTAL_ATTEND_TIME", rowNo, StringUtils.defaultString(intString(setTotalAttend), "0")); // 合計出席時間
            }

            for (final Iterator it3 = schData._executeDateList.iterator(); it3.hasNext();) {
                final String excuteDate = (String) it3.next();

                if (colNo >= maxCol) {
                    colNo  = 0;
                }

                final String dateMonth = KNJ_EditDate.h_format_S(excuteDate, "M");
                final String dateDay   = KNJ_EditDate.h_format_S(excuteDate, "d");
                svf.VrsOut("DATE2_1", dateMonth + "月"); // 日付（月）
                svf.VrsOut("DATE2_2", dateDay   + "日"); // 日付（日）

                for (Iterator it4 = schData._schedAttendMap.keySet().iterator(); it4.hasNext();) {
                    final String chairSubclassExcute = (String) it4.next();
                    //final String chairCd         = StringUtils.split(chairSubclassExcute ,"_")[0];
                    final String subclassCd      = StringUtils.split(chairSubclassExcute ,"_")[0];
                    final String keyExcute       = StringUtils.split(chairSubclassExcute ,"_")[1];
                    //final String key             = chairSubclassCd;
                    final String key             = subclassCd;
                    if (!excuteDate.equals(keyExcute) || null == schData._subclasscdRownoMap.get(key)) continue;

                    final int setRowNo = Integer.parseInt((String) schData._subclasscdRownoMap.get(key));
                    final String setAttendTime = (String) schData._schedAttendMap.get(key + "_" + excuteDate);
                    svf.VrsOutn("ATTEND_TIME", setRowNo, intString(setAttendTime)); // 出席時間
                }

                svf.VrEndRecord();
                colNo += 1;
            }
            for (int nmi = 0; nmi < _param._m001List.size(); nmi++) {
                final Map row = (Map) _param._m001List.get(nmi);
                final String namecd2 = KnjDbUtils.getString(row, "NAMECD2");

                if (colNo >= maxCol) {
                    colNo  = 0;
                }

                final String name1 = KnjDbUtils.getString(row, "NAME1");
                final String[] tokens;
                final String[] fields;
                if (KNJ_EditEdit.getMS932ByteLength(name1) > 8) {
                    fields = new String[] {"DATE3_1", "DATE3_2", "DATE3_3"};
                    tokens = KNJ_EditEdit.get_token(name1, 6, 3);
                } else {
                    fields = new String[] {"DATE2_1", "DATE2_2"};
                    tokens = KNJ_EditEdit.get_token(name1, 4, 2);
                }
                if (null != tokens) {
                    for (int i = 0; i < Math.min(tokens.length, fields.length); i++) {
                        svf.VrsOut(fields[i], tokens[i]);
                    }
                }

                for (final Iterator it4 = schData._schedAttendMap.keySet().iterator(); it4.hasNext();) {
                    final String chairSubclassExcute = (String) it4.next();
                    //final String chairCd         = StringUtils.split(chairSubclassExcute ,"_SCHOOLINGKINDCD")[0];
                    final String subclassCd      = StringUtils.split(chairSubclassExcute ,"_SCHOOLINGKINDCD")[0];
                    final String keyExcute       = StringUtils.split(chairSubclassExcute ,"_SCHOOLINGKINDCD")[1];
                    //final String key             = chairSubclassCd;
                    final String key             = subclassCd;
                    if (!namecd2.equals(keyExcute) || null == schData._subclasscdRownoMap.get(key)) continue;

                    final int setRowNo = Integer.parseInt((String) schData._subclasscdRownoMap.get(key));
                    final String setAttendTime = (String) schData._schedAttendMap.get(key + "_SCHOOLINGKINDCD" + namecd2);
                    svf.VrsOutn("ATTEND_TIME", setRowNo, intString(setAttendTime)); // 出席時間
                }

                svf.VrEndRecord();
                colNo += 1;
            }
            for (int i = (colNo > 0 && colNo % maxCol == 0 ? maxCol : colNo <= maxCol ? colNo : colNo % maxCol); i < maxCol; i++) {
                svf.VrEndRecord();
            }
            _hasData = true;
        }
    }

    private static class schregData {
        final String _schregNo;
        final String _schoolKind;
        final String _name;
        final String _nameKana;
        final String _hrNameAbbv;
        final String _staffName;
        final String _attendno;
        Map _subclasscdRownoMap;
        List _subclassList;
        Map _totalAttendMap;
        Map _schedAttendMap;
        List _executeDateList;
        public schregData(
                final String schregNo,
                final String schoolKind,
                final String name,
                final String nameKana,
                final String hrNameAbbv,
                final String staffName,
                final String attendno
        ) {
            _schregNo     = schregNo;
            _schoolKind     = schoolKind;
            _name         = name;
            _nameKana     = nameKana;
            _hrNameAbbv   = hrNameAbbv;
            _staffName    = staffName;
            _attendno     = attendno;
        }

        private void load(final DB2UDB db2, final Param param) {
            int rowNo = 1;
            _subclasscdRownoMap = new TreeMap();
            _subclassList = SubClassData.getSubclassList(db2, _schregNo, param);
            for (Iterator it2 = _subclassList.iterator(); it2.hasNext();) {
                SubClassData subClassData = (SubClassData) it2.next();

                //_setSubCdMap.put(subClassData._chairCd + subClassData._subClassCd, String.valueOf(rowNo));
                _subclasscdRownoMap.put(subClassData._subClassCd, String.valueOf(rowNo));
                rowNo++;
            }

            _totalAttendMap = new TreeMap();
            _schedAttendMap  = getSchAttendDataMap(db2, _schregNo, param);
            _executeDateList = getExecuteDateList(db2, _schregNo, param);

            for (final Iterator it3 = _executeDateList.iterator(); it3.hasNext();) {
                final String excuteDate = (String) it3.next();

                for (final Iterator it4 = _schedAttendMap.keySet().iterator(); it4.hasNext();) {
                    final String chairSubclassExcute = (String) it4.next();
                    //final String chairCd   = StringUtils.split(chairSubclassExcute ,"_")[0];
                    final String subclassCd = StringUtils.split(chairSubclassExcute ,"_")[0];
                    final String keyExcute  = StringUtils.split(chairSubclassExcute ,"_")[1];
                    final String key = subclassCd;
                    if (!excuteDate.equals(keyExcute) || null == _subclasscdRownoMap.get(key)) continue;

                    final String setAttendTime = (String) _schedAttendMap.get(key + "_" + excuteDate);

                    //合計出席時間をセット
                    _totalAttendMap.put(key, add((String) _totalAttendMap.get(key), setAttendTime));
                }
            }

            for (final Iterator it3 = param._m001List.iterator(); it3.hasNext();) {
                final Map row = (Map) it3.next();
                final String namecd2 = KnjDbUtils.getString(row, "NAMECD2");

                for (final Iterator it4 = _schedAttendMap.keySet().iterator(); it4.hasNext();) {
                    final String chairSubclassSchoolingkindcd = (String) it4.next();
                    //final String chairCd    = StringUtils.split(chairSubclassSchoolingkindcd ,"_SCHOOLINGKINDCD")[0];
                    final String subclassCd = StringUtils.split(chairSubclassSchoolingkindcd ,"_SCHOOLINGKINDCD")[0];
                    final String keyExcute  = StringUtils.split(chairSubclassSchoolingkindcd ,"_SCHOOLINGKINDCD")[1];
                    if (!namecd2.equals(keyExcute) || null == _subclasscdRownoMap.get(subclassCd)) continue;

                    final String schoolingAttData = (String) _schedAttendMap.get(subclassCd + "_SCHOOLINGKINDCD" + namecd2);
                    if (NumberUtils.isNumber(schoolingAttData)) {
                        final String creditTime = String.valueOf((int) Double.parseDouble(schoolingAttData));

                        //合計出席時間をセット
                        _totalAttendMap.put(subclassCd, add((String) _totalAttendMap.get(subclassCd), creditTime));
                    }
                }
            }
        }

        /**
         * 生徒ごとの授業実施日付
         * @param db2
         * @param schregNo
         * @return
         */
        private static List getExecuteDateList(final DB2UDB db2, final String schregNo, final Param param) {

            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCHEDULE AS ( ");
            stb.append(" SELECT ");
            stb.append("     REG_D.YEAR, REG_D.SEMESTER, SCCHR.CHAIRCD, STD.APPENDDATE, SCCHR.EXECUTEDATE ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT REG_D ");
            stb.append("     INNER JOIN CHAIR_STD_DAT STD ON REG_D.YEAR     = STD.YEAR ");
            stb.append("                                  AND REG_D.SEMESTER = STD.SEMESTER ");
            stb.append("                                  AND REG_D.SCHREGNO = STD.SCHREGNO ");
            stb.append("     INNER JOIN SCH_CHR_DAT SCCHR ON STD.CHAIRCD = SCCHR.CHAIRCD ");
            stb.append("                                 AND REG_D.YEAR    = SCCHR.YEAR ");
            stb.append("                                 AND SCCHR.EXECUTEDATE BETWEEN STD.APPDATE AND STD.APPENDDATE ");
            stb.append("     INNER JOIN CHAIR_DAT CHAIR ON STD.YEAR     = CHAIR.YEAR ");
            stb.append("                               AND STD.SEMESTER = CHAIR.SEMESTER ");
            stb.append("                               AND STD.CHAIRCD  = CHAIR.CHAIRCD ");
            stb.append(" WHERE ");
            stb.append("         REG_D.YEAR     = '"+ param._ctrlYear +"' ");
            stb.append("     AND REG_D.SEMESTER <= '"+ param._ctrlSemester +"' ");
            stb.append("     AND REG_D.SCHREGNO = '"+ schregNo +"' ");
            stb.append("     AND CHAIR.CLASSCD <= '90' ");
            if ("1".equals(param._notPrintZenki)) {
                stb.append("     AND VALUE(CHAIR.TAKESEMES, '0') <> '1' ");
            }
            stb.append(" GROUP BY ");
            stb.append("     REG_D.YEAR, REG_D.SEMESTER, SCCHR.CHAIRCD, STD.APPENDDATE, SCCHR.EXECUTEDATE ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.EXECUTEDATE ");
            stb.append(" FROM ");
            stb.append("     SCHEDULE T1 ");
            if ("1".equals(param._printSubclassLastChairStd)) {
                stb.append("     INNER JOIN SEMESTER_MST SEME ON T1.YEAR       = SEME.YEAR ");
                stb.append("                                 AND T1.SEMESTER   = SEME.SEMESTER ");
                stb.append("                                 AND T1.APPENDDATE = SEME.EDATE ");
                stb.append("     INNER JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = '" + schregNo + "' ");
                stb.append("                                 AND T1.YEAR = REGD.YEAR ");
                stb.append("                                 AND T1.SEMESTER = REGD.SEMESTER ");
                stb.append("     INNER JOIN SCHREG_REGD_DAT REGD_S ON REGD_S.SCHREGNO = REGD.SCHREGNO "); // ログインした学期と同じ学科（土曜コース、平日コース）の時間割のみを表示する。（コース変更した場合、変更前は対象外とする）
                stb.append("                                 AND REGD_S.YEAR = REGD.YEAR ");
                stb.append("                                 AND REGD_S.SEMESTER = '" + param._ctrlSemester + "' ");
                stb.append("                                 AND REGD_S.COURSECD = REGD.COURSECD ");
                stb.append("                                 AND REGD_S.MAJORCD = REGD.MAJORCD ");
            }
            stb.append(" GROUP BY ");
            stb.append("     T1.EXECUTEDATE ");
            stb.append(" ORDER BY ");
            stb.append("     T1.EXECUTEDATE ");

            final String sql = stb.toString();
            if (param._isOutputDebug) {
                log.info(" sql =" + sql);
            }

            return KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, sql), "EXECUTEDATE");
        }

        /**
         * 生徒ごとの出席時間情報
         * @param db2
         * @param schregNo
         * @return
         */
        private static Map getSchAttendDataMap(final DB2UDB db2, final String schregNo, final Param param) {
            final Map retMap = new TreeMap();
            final String sql = getSchAtenndSql(schregNo, param);
            log.debug(" sql =" + sql);

            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                //final String chairCd     = KnjDbUtils.getString(row, "CHAIRCD");
                final String subClassCd  = KnjDbUtils.getString(row, "SUBCLASSCD");
                final String executeDate = KnjDbUtils.getString(row, "EXECUTEDATE");
                final String attendTime  = KnjDbUtils.getString(row, "ATTEND_TIME");

                //retMap.put(chairCd + "_" + subClassCd + "_" + executeDate, attendTime);
                retMap.put(subClassCd + "_" + executeDate, add((String) retMap.get(subClassCd + "_" + executeDate), attendTime));
            }

            final String sql2 = getSchAttendDatSql(schregNo, param);
            for (final Iterator it = KnjDbUtils.query(db2, sql2).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                //final String chairCd         = KnjDbUtils.getString(row, "CHAIRCD");
                final String subClassCd      = KnjDbUtils.getString(row, "SUBCLASSCD");
                final String schoolingkindcd = KnjDbUtils.getString(row, "SCHOOLINGKINDCD");
                final String creditTime      = KnjDbUtils.getString(row, "CREDIT_TIME");

                //retMap.put(chairCd + "_" + subClassCd + "_SCHOOLINGKINDCD" + schoolingkindcd, creditTime);
                retMap.put(subClassCd + "_SCHOOLINGKINDCD" + schoolingkindcd, add((String) retMap.get(subClassCd + "_SCHOOLINGKINDCD" + schoolingkindcd), creditTime));
            }
            return retMap;
        }

        private static String getSchAtenndSql(final String schregNo, final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH T_EXECUTED AS ( ");
            stb.append("   SELECT ");
            stb.append("       EXECUTEDATE, PERIODCD, CHAIRCD, EXECUTED ");
            stb.append("   FROM ");
            stb.append("       SCH_CHR_DAT SCH ");
            stb.append("       INNER JOIN SEMESTER_MST SEME ON SEME.YEAR = '" + param._ctrlYear + "' ");
            stb.append("           AND SEME.SEMESTER <> '9' ");
            stb.append("           AND SCH.EXECUTEDATE BETWEEN SEME.SDATE AND SEME.EDATE ");
            stb.append("   UNION ");
            stb.append("   SELECT ");
            stb.append("       EXECUTEDATE, PERIODCD, CHAIRCD, EXECUTED ");
            stb.append("   FROM ");
            stb.append("       SCH_CHR_HRATE_DAT SCH ");
            stb.append("       INNER JOIN SEMESTER_MST SEME ON SEME.YEAR = '" + param._ctrlYear + "' ");
            stb.append("           AND SEME.SEMESTER <> '9' ");
            stb.append("           AND SCH.EXECUTEDATE BETWEEN SEME.SDATE AND SEME.EDATE ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     STD_D.SCHREGNO, ");
            stb.append("     STD_D.CHAIRCD, ");
            stb.append("     SUB_M.SUBCLASSCD, ");
            stb.append("     HRATE.EXECUTEDATE, ");
            stb.append("     sum(case when ATEND.DI_CD is null then 1 ");
            stb.append("              when ATEND.DI_CD = '0'   then 1 ");
            stb.append("              else 0 ");
            stb.append("         end) AS ATTEND_TIME ");
            stb.append(" FROM ");
            stb.append("     CHAIR_STD_DAT STD_D ");
            stb.append("     LEFT JOIN CHAIR_DAT CHAIR ON STD_D.YEAR     = CHAIR.YEAR ");
            stb.append("                              AND STD_D.SEMESTER = CHAIR.SEMESTER ");
            stb.append("                              AND STD_D.CHAIRCD  = CHAIR.CHAIRCD ");
            stb.append("     LEFT JOIN V_SUBCLASS_MST SUB_M ON CHAIR.YEAR          = SUB_M.YEAR ");
            stb.append("                                   AND CHAIR.CLASSCD       = SUB_M.CLASSCD ");
            stb.append("                                   AND CHAIR.SCHOOL_KIND   = SUB_M.SCHOOL_KIND ");
            stb.append("                                   AND CHAIR.CURRICULUM_CD = SUB_M.CURRICULUM_CD ");
            stb.append("                                   AND CHAIR.SUBCLASSCD    = SUB_M.SUBCLASSCD ");
            stb.append("     LEFT JOIN SCHREG_REGD_DAT REG_D ON STD_D.YEAR     = REG_D.YEAR ");
            stb.append("                                    AND STD_D.SEMESTER = REG_D.SEMESTER ");
            stb.append("                                    AND STD_D.SCHREGNO = REG_D.SCHREGNO ");
            stb.append("     LEFT JOIN T_EXECUTED HRATE ON STD_D.CHAIRCD  = HRATE.CHAIRCD ");
            stb.append("                                    AND HRATE.EXECUTEDATE BETWEEN STD_D.APPDATE AND STD_D.APPENDDATE ");
            stb.append("     LEFT JOIN ATTEND_DAT ATEND ON ATEND.SCHREGNO   = STD_D.SCHREGNO ");
            stb.append("                               AND ATEND.ATTENDDATE = HRATE.EXECUTEDATE ");
            stb.append("                               AND ATEND.PERIODCD   = HRATE.PERIODCD ");
            stb.append(" WHERE ");
            stb.append("         STD_D.YEAR     = '"+ param._ctrlYear +"' ");
            stb.append("     AND STD_D.SEMESTER <= '"+ param._ctrlSemester +"' ");
            stb.append("     AND STD_D.SCHREGNO = '"+ schregNo +"' ");
            stb.append("     AND HRATE.EXECUTED = '1' ");
            stb.append("     AND CHAIR.CLASSCD <= '90' ");
            stb.append("     AND NOT EXISTS( ");
            stb.append("         SELECT ");
            stb.append("             'X' ");
            stb.append("         FROM ");
            stb.append("             SCHREG_BASE_MST BASE ");
            stb.append("         WHERE ");
            stb.append("             BASE.SCHREGNO = STD_D.SCHREGNO ");
            stb.append("             AND BASE.GRD_DIV IS NOT NULL ");
            stb.append("             AND BASE.GRD_DIV NOT IN ('4','5') ");
            stb.append("             AND BASE.GRD_DATE IS NOT NULL ");
            stb.append("             AND BASE.GRD_DATE < HRATE.EXECUTEDATE ");
            stb.append("     ) ");
            stb.append(" GROUP BY ");
            stb.append("     STD_D.SCHREGNO, ");
            stb.append("     STD_D.CHAIRCD, ");
            stb.append("     SUB_M.SUBCLASSCD, ");
            stb.append("     HRATE.EXECUTEDATE ");
            stb.append(" ORDER BY ");
            stb.append("     STD_D.SCHREGNO, ");
            stb.append("     SUB_M.SUBCLASSCD ");

            return stb.toString();
        }

        private static String getSchAttendDatSql(final String schregNo, final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("       T1.SCHREGNO ");
            stb.append("     , T1.CHAIRCD ");
            stb.append("     , CHR.SUBCLASSCD ");
            stb.append("     , T1.SCHOOLINGKINDCD ");
            stb.append("     , SUM(T1.CREDIT_TIME) AS CREDIT_TIME ");
            stb.append(" FROM SCH_ATTEND_DAT T1 ");
            stb.append(" INNER JOIN SEMESTER_MST SEME ON SEME.YEAR = T1.YEAR ");
            stb.append("                             AND SEME.SEMESTER <> '9' ");
            stb.append("                             AND T1.EXECUTEDATE BETWEEN SEME.SDATE AND SEME.EDATE ");
            stb.append(" INNER JOIN CHAIR_DAT CHR ON CHR.YEAR = T1.YEAR ");
            stb.append("                         AND CHR.SEMESTER = SEME.SEMESTER ");
            stb.append("                         AND CHR.CHAIRCD = T1.CHAIRCD ");
            stb.append(" INNER JOIN SUBCLASS_MST SUB_M ON SUB_M.CLASSCD = CHR.CLASSCD ");
            stb.append("                              AND SUB_M.SCHOOL_KIND = CHR.SCHOOL_KIND ");
            stb.append("                              AND SUB_M.CURRICULUM_CD = CHR.CURRICULUM_CD ");
            stb.append("                              AND SUB_M.SUBCLASSCD = CHR.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._ctrlYear + "' ");
            stb.append("     AND SEME.SEMESTER <= '" + param._ctrlSemester + "' ");
            stb.append("     AND CHR.CLASSCD <= '90' ");
            stb.append("     AND T1.SCHREGNO = '" + schregNo + "' ");
            stb.append(" GROUP BY ");
            stb.append("       T1.SCHREGNO ");
            stb.append("     , T1.CHAIRCD ");
            stb.append("     , CHR.SUBCLASSCD ");
            stb.append("     , T1.SCHOOLINGKINDCD ");
            stb.append(" HAVING ");
            stb.append("     SUM(T1.CREDIT_TIME) IS NOT NULL ");
            stb.append(" ORDER BY ");
            stb.append("       T1.SCHOOLINGKINDCD ");
            stb.append("     , CHR.SUBCLASSCD ");

            return stb.toString();
        }

        private static List getSchList(final DB2UDB db2, final Param param) {
            final List retList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getSchregSql(param);
                if (param._isOutputDebug) {
                    log.info(" student sql =" + sql);
                }
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String schregNo   = rs.getString("SCHREGNO");
                    final String name       = rs.getString("NAME");
                    final String nameKana   = rs.getString("NAME_KANA");
                    final String hrNameAbbv = rs.getString("HR_NAMEABBV");
                    final String staffName  = rs.getString("STAFFNAME");
                    final String schoolKind = rs.getString("SCHOOL_KIND");
                    final String attendno = NumberUtils.isDigits(rs.getString("ATTENDNO")) ? String.valueOf(Integer.parseInt(rs.getString("ATTENDNO"))) : rs.getString("ATTENDNO");

                    final schregData schData = new schregData(schregNo, schoolKind, name, nameKana, hrNameAbbv, staffName, attendno);
                    retList.add(schData);
                }

            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retList;
        }

        private static String getSchregSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     REGD.SCHREGNO, ");
            stb.append("     GDAT.SCHOOL_KIND, ");
            stb.append("     BASE.NAME, ");
            stb.append("     BASE.NAME_KANA, ");
            stb.append("     HDAT.HR_NAMEABBV, ");
            stb.append("     STFF.STAFFNAME, ");
            stb.append("     REGD.ATTENDNO ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT REGD ");
            stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ");
            stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON REGD.YEAR  = GDAT.YEAR ");
            stb.append("                                    AND REGD.GRADE = GDAT.GRADE ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT HDAT ON REGD.YEAR     = HDAT.YEAR ");
            stb.append("                                    AND REGD.SEMESTER = HDAT.SEMESTER ");
            stb.append("                                    AND REGD.GRADE    = HDAT.GRADE ");
            stb.append("                                    AND REGD.HR_CLASS = HDAT.HR_CLASS ");
            stb.append("     LEFT JOIN STAFF_MST STFF ON HDAT.TR_CD1 = STFF.STAFFCD ");
            stb.append(" WHERE ");
            stb.append("         REGD.YEAR     = '" + param._ctrlYear + "' ");
            stb.append("     AND REGD.SEMESTER = '" + param._ctrlSemester + "' ");
            stb.append("     AND REGD.SCHREGNO IN " + param._schNoSelectedIn + " ");
            stb.append(" ORDER BY ");
            stb.append("     REGD.ATTENDNO ");

            return stb.toString();
        }
    }

    private static class SubClassData {
        //final String _chairCd;
        final String _subClassCd;
        final String _subClassName;
        String _schSeqAll;
        public SubClassData(
                final String chairCd,
                final String subClassCd,
                final String subClassName
        ) {
            //_chairCd      = chairCd;
            _subClassCd   = subClassCd;
            _subClassName = subClassName;
        }


        /**
         * 生徒ごとの科目（講座）リスト
         * @param db2
         * @param schregNo
         * @return
         */
        private static List getSubclassList(final DB2UDB db2, final String schregNo, final Param param) {
            final List subclassList = new ArrayList();
            final Map subclassCdMap = new HashMap();
            final String sql = getSubClassSql(schregNo, param);
            if (param._isOutputDebug) {
                log.info(" subclass sql =" + sql);
            }

            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                //final String chairCd      = KnjDbUtils.getString(row, "CHAIRCD");
                final String subClassCd   = KnjDbUtils.getString(row, "SUBCLASSCD");
                final String subClassName = KnjDbUtils.getString(row, "SUBCLASSNAME");
                final String schSeqAll    = KnjDbUtils.getString(row, "SCH_SEQ_ALL");

                final String key = subClassCd;
                if (!subclassCdMap.containsKey(key)) {
                    final SubClassData subClassData = new SubClassData(null, subClassCd, subClassName);
                    subclassCdMap.put(key, subClassData);
                    subclassList.add(subClassData);
                }
                final SubClassData subClassData = (SubClassData) subclassCdMap.get(key);
                if (null == subClassData._schSeqAll || NumberUtils.isDigits(subClassData._schSeqAll) && NumberUtils.isDigits(schSeqAll) && Integer.parseInt(schSeqAll) > Integer.parseInt(subClassData._schSeqAll)) {
                    subClassData._schSeqAll = schSeqAll;
                }
            }
            return subclassList;
        }

        private static String getSubClassSql(final String schregNo, final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     STD_D.SCHREGNO, ");
//            stb.append("     STD_D.CHAIRCD, ");
            stb.append("     SUB_M.SUBCLASSCD, ");
            stb.append("     SUB_M.SUBCLASSNAME, ");
            stb.append("     MAX(CORES.SCH_SEQ_ALL) AS SCH_SEQ_ALL ");
            stb.append(" FROM ");
            stb.append("     CHAIR_STD_DAT STD_D ");
            stb.append("     INNER JOIN CHAIR_DAT CHAIR ON STD_D.YEAR     = CHAIR.YEAR ");
            stb.append("                              AND STD_D.SEMESTER = CHAIR.SEMESTER ");
            stb.append("                              AND STD_D.CHAIRCD  = CHAIR.CHAIRCD ");
            stb.append("     INNER JOIN V_SUBCLASS_MST SUB_M ON CHAIR.YEAR          = SUB_M.YEAR ");
            stb.append("                                   AND CHAIR.CLASSCD       = SUB_M.CLASSCD ");
            stb.append("                                   AND CHAIR.SCHOOL_KIND   = SUB_M.SCHOOL_KIND ");
            stb.append("                                   AND CHAIR.CURRICULUM_CD = SUB_M.CURRICULUM_CD ");
            stb.append("                                   AND CHAIR.SUBCLASSCD    = SUB_M.SUBCLASSCD ");
            stb.append("     LEFT JOIN CHAIR_CORRES_DAT CORES ON CHAIR.YEAR          = CORES.YEAR ");
            stb.append("                                     AND CHAIR.CHAIRCD       = CORES.CHAIRCD ");
            stb.append("                                     AND CHAIR.CLASSCD       = CORES.CLASSCD ");
            stb.append("                                     AND CHAIR.SCHOOL_KIND   = CORES.SCHOOL_KIND ");
            stb.append("                                     AND CHAIR.CURRICULUM_CD = CORES.CURRICULUM_CD ");
            stb.append("                                     AND CHAIR.SUBCLASSCD    = CORES.SUBCLASSCD ");
            if ("1".equals(param._printSubclassLastChairStd)) {
                stb.append("     INNER JOIN SEMESTER_MST SEME ON STD_D.YEAR       = SEME.YEAR ");
                stb.append("                                 AND STD_D.SEMESTER   = SEME.SEMESTER ");
                stb.append("                                 AND STD_D.APPENDDATE = SEME.EDATE ");
                stb.append("     INNER JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = STD_D.SCHREGNO ");
                stb.append("                                 AND STD_D.YEAR = REGD.YEAR ");
                stb.append("                                 AND STD_D.SEMESTER = REGD.SEMESTER ");
                stb.append("     INNER JOIN SCHREG_REGD_DAT REGD_S ON REGD_S.SCHREGNO = REGD.SCHREGNO "); // ログインした学期と同じ学科（土曜コース、平日コース）の時間割のみを表示する。（コース変更した場合、変更前は対象外とする）
                stb.append("                                 AND REGD_S.YEAR = REGD.YEAR ");
                stb.append("                                 AND REGD_S.SEMESTER = '" + param._ctrlSemester + "' ");
                stb.append("                                 AND REGD_S.COURSECD = REGD.COURSECD ");
                stb.append("                                 AND REGD_S.MAJORCD = REGD.MAJORCD ");
            }
            stb.append(" WHERE ");
            stb.append("         STD_D.YEAR     = '"+ param._ctrlYear +"' ");
            stb.append("     AND STD_D.SEMESTER <= '"+ param._ctrlSemester +"' ");
            stb.append("     AND STD_D.SCHREGNO = '"+ schregNo +"' ");
            stb.append("     AND CHAIR.CLASSCD <= '90' ");
            if ("1".equals(param._notPrintZenki)) {
                stb.append("     AND VALUE(CHAIR.TAKESEMES, '0') <> '1' ");
            }
            stb.append(" GROUP BY ");
            stb.append("     STD_D.SCHREGNO, ");
//            stb.append("     STD_D.CHAIRCD, ");
            stb.append("     SUB_M.SUBCLASSCD, ");
            stb.append("     SUB_M.SUBCLASSNAME ");
            stb.append(" UNION ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
//            stb.append("     T1.CHAIRCD, ");
            stb.append("     SUB_M.SUBCLASSCD, ");
            stb.append("     SUB_M.SUBCLASSNAME, ");
            stb.append("     CAST(NULL AS SMALLINT) AS SCH_SEQ_ALL ");
            stb.append(" FROM SCH_ATTEND_DAT T1 ");
            stb.append(" INNER JOIN SEMESTER_MST L1 ON ");
            stb.append("        L1.YEAR = T1.YEAR ");
            stb.append("    AND L1.SEMESTER <> '9' ");
            stb.append("    AND T1.EXECUTEDATE BETWEEN L1.SDATE AND L1.EDATE ");
            stb.append(" INNER JOIN CHAIR_DAT CHAIR ON ");
            stb.append("        CHAIR.YEAR = T1.YEAR ");
            stb.append("    AND CHAIR.SEMESTER = L1.SEMESTER ");
            stb.append("    AND CHAIR.CHAIRCD = T1.CHAIRCD ");
            stb.append(" INNER JOIN SUBCLASS_MST SUB_M ON ");
            stb.append("        SUB_M.CLASSCD = CHAIR.CLASSCD ");
            stb.append("    AND SUB_M.SCHOOL_KIND = CHAIR.SCHOOL_KIND ");
            stb.append("    AND SUB_M.CURRICULUM_CD = CHAIR.CURRICULUM_CD ");
            stb.append("    AND SUB_M.SUBCLASSCD = CHAIR.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("         T1.YEAR = '"+ param._ctrlYear +"' ");
            stb.append("     AND L1.SEMESTER <= '"+ param._ctrlSemester +"' ");
            stb.append("     AND T1.SCHREGNO = '"+ schregNo +"' ");
            stb.append("     AND CHAIR.CLASSCD <= '90' ");
            if ("1".equals(param._notPrintZenki)) {
                stb.append("     AND VALUE(CHAIR.TAKESEMES, '0') <> '1' ");
            }
            stb.append(" ORDER BY ");
            stb.append("     SCHREGNO, ");
//            stb.append("     CHAIRCD, ");
            stb.append("     SUBCLASSCD ");

            return stb.toString();
        }
    }

    /** パラメータ取得処理 */
    private static Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 70477 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _schoolCd;
        private final String _schNoSelectedIn;
        private final String _cmd;
        private final String _notPrintZenki;
        private final String _printSubclassLastChairStd;
        private final Map _schoolTelMap;
        private final List _m001List;
        private final boolean _isOutputDebug;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear           = request.getParameter("CTRL_YEAR");
            _ctrlSemester       = request.getParameter("CTRL_SEMESTER");
            _ctrlDate           = request.getParameter("CTRL_DATE");
            _schoolCd           = request.getParameter("SCHOOLCD");
            _cmd                = request.getParameter("cmd");
            _schoolTelMap       = getSchoolTelNo(db2, _ctrlYear, _schoolCd);
            _notPrintZenki      = request.getParameter("NOT_PRINT_ZENKI");
            _printSubclassLastChairStd = request.getParameter("printSubclassLastChairStd");

            final String[] schNoSelected = request.getParameterValues("CATEGORY_SELECTED");
            _schNoSelectedIn = getSchregNoIn(schNoSelected);

            _m001List = KnjDbUtils.query(db2, " SELECT NAMECD2, NAME1 FROM V_NAME_MST WHERE YEAR = '" + _ctrlYear + "' AND NAMECD1 = 'M001' AND NAMECD2 IS NOT NULL ORDER BY INT(NAMECD2) ");

            _isOutputDebug = "1".equals(KnjDbUtils.getDbPrginfoProperties(db2, "KNJC155", "outputDebug"));
        }

        private String getSchregNoIn(final String[] schNoSelected) {
            StringBuffer stb = new StringBuffer();
            stb.append("(");
            for (int i = 0; i < schNoSelected.length; i++) {
                if (0 < i) stb.append(",");
                stb.append("'" + schNoSelected[i] + "'");
            }
            stb.append(")");
            return stb.toString();
        }

        private Map getSchoolTelNo(final DB2UDB db2, final String year, final String schoolCd) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("    SCHOOL_KIND, ");
            stb.append("    SCHOOLTELNO ");
            stb.append(" FROM ");
            stb.append("    SCHOOL_MST ");
            stb.append(" WHERE ");
            stb.append("        YEAR     = '" + year + "' ");
            stb.append("    AND SCHOOLCD = '"+ schoolCd +"'");

            return KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, stb.toString()), "SCHOOL_KIND", "SCHOOLTELNO");
        }
    }
}

// eof
