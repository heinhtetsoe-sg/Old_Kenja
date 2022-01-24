// kanji=漢字
/*
 * $Id: c33823a5f911f2ce0e22aa72e4d512f4925b84b9 $
 *
 * 作成日:
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJE;

import static servletpack.KNJZ.detail.KnjDbUtils.getString;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJG.KNJG010_1;
import servletpack.KNJZ.detail.KNJDefineCode;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KNJ_PersonalinfoSql;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.SvfField;
import servletpack.KNJZ.detail.SvfFieldAreaInfo;
import servletpack.KNJZ.detail.SvfForm;
import servletpack.KNJZ.detail.SvfForm.Field;
import servletpack.KNJZ.detail.SvfForm.ImageField;

/*
 *  学校教育システム 賢者 [進路情報管理]  中学成績証明書（和）
 */

public class KNJE080J_1 {

    private static final Log log = LogFactory.getLog(KNJE080J_1.class);

    private static String CERTIF_KINDCD = "033";

    private Vrw32alp _svf;   //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
    private DB2UDB _db2;                      //Databaseクラスを継承したクラス
    private Param _param;
    private PrintData _printData;
    public boolean nonedata;

    public KNJE080J_1(final DB2UDB db2, final Vrw32alp svf, final Map initParamap) {
        this._db2 = db2;
        this._svf = svf;
        nonedata = false;
        log.fatal("$Revision: 76807 $ $Date: 2020-09-12 00:59:39 +0900 (土, 12 9 2020) $"); // CVSキーワードの取り扱いに注意
        _param = new Param(db2, initParamap);
    }

    public void printSvf(
            final String year,
            final String semester,
            final String date,
            final String schregno,
            final Map<String, String> paramap,
            final String staffCd,
            final int paper,
            final String kanji,
            final String certifNumber) {
        _param.setDocumentroot((String) paramap.get("DOCUMENTROOT"));
        if (_param._isOutputDebug) {
            final List<String> keys = new ArrayList<String>(paramap.keySet());
            Collections.sort(keys);
            for (final String key : keys) {
                log.info(" paramap " + key + " = " + paramap.get(key));
            }
        }
        final PrintData printData = new PrintData(schregno, year, semester, date, paramap, staffCd, kanji, certifNumber, _param);
        printData.load(_db2, _param);
        final Form form = new Form(_svf, _param);
        form.print(_db2, printData);
        nonedata = true;
    }

    /**
     *  PrepareStatement作成
     */
    public void pre_stat(String hyotei) {
    }

    /**
     *  PrepareStatement close
     */
    public void pre_stat_f() {
        if (null != _param) {
            for (final PreparedStatement ps : _param._psMap.values()) {
                DbUtils.closeQuietly(ps);
            }
            _param.deleteFile();
        }
    }

    private static String defstr(final String a, final String b) {
        return StringUtils.defaultString(a, b);
    }

    private static class Form {
        final Vrw32alp _svf;
        final Param _param;
        PrintData _printData;
        private String _formname;
        private SvfForm _svfForm;

        Form(final Vrw32alp svf, final Param param) {
            _svf = svf;
            _param = param;
        }

        private void print(final DB2UDB _db2, final PrintData printData) {
            _printData = printData;
            String formname = null;
            if (_param._isKindai) {
                formname = "KNJE080_3KIN.frm";
            } else if (_param._isChiyodaKudan) {
                formname = "KNJE080_1JKUDAN.frm";
            } else if (_param._isOsakatoin) {
                formname = "KNJE080_1JTOIN.frm";
            } else if (_param._isMatsudo) {
                formname = "KNJE080_1JMATSUDO.frm";
            } else if (_param._isJogakkan) {
                formname = "KNJE080_1J_JOGAKKAN.frm";
            } else {
                formname = "KNJE080_1J.frm";
            }
            formname = modifyForm(_param, formname);
            log.info(" form = " + formname);
            setForm(formname);
            printSchoolInfo(_db2);  //学校名、校長名のセット
            printPersonalInfo(_db2);  //氏名等出力
            printAttend(_db2);  //出欠の出力
            printStudyrec(_db2);  //学習の記録出力
            _svf.VrEndPage();
        }

        private final String FLG_STAMP_MOVE = "FLG_STAMP_MOVE";
        private final String FLG_STAMP_SIZE = "FLG_STAMP_SIZE";
        private TreeMap<String, String> getModifyFlgMap(final SvfForm svfForm) {
            final TreeMap<String, String> modifyFlgMap = new TreeMap<String, String>();

            ImageField imageField = svfForm.getImageField("STAMP");
            if (null != imageField) {
                if (_param._isSakae) {
                    modifyFlgMap.put(FLG_STAMP_MOVE, "1");
                } else if (null != _printData._stampSizeMm) {
                    modifyFlgMap.put(FLG_STAMP_SIZE, "1");
                }
            }

            return modifyFlgMap;
        }
        private String modifyForm(final Param param, String formname) {
            final String path = _svf.getPath(formname);
            _svfForm = new SvfForm(new File(path));
            if (!_svfForm.readFile()) {
                return formname;
            }
            final TreeMap<String, String> modifyFlgMap = getModifyFlgMap(_svfForm);
            if (modifyFlgMap.isEmpty()) {
                return formname;
            }
            final String flg = formname + "::" + Util.mkString(modifyFlgMap, "|");
            if (!param._deleteFiles.containsKey(flg)) {
                try {
                    if (modifyFlgMap.containsKey(FLG_STAMP_MOVE)) {
                        ImageField imageField = _svfForm.getImageField("STAMP");
                        _svfForm.move(imageField, imageField.addX(180).addY(100));
                    }

                    File file = _svfForm.writeTempFile();
                    if (file.exists()) {
                        param._deleteFiles.put(flg, file);
                    }
                } catch (IOException e) {
                    log.error("exception!", e);
                }
                if (null != param._deleteFiles.get(flg)) {
                    formname = param._deleteFiles.get(flg).getName();
                }
            }
            return formname;
        }

        protected void resizeStampImage(final Param param, final PrintData printData, final SvfForm svfForm) {
            final SvfForm.ImageField image = svfForm.getImageField("STAMP");
            if (null == image) {
                return;
            }
            final int x = image._point._x;
            final int y = image._point._y;
            final int endX = image._endX;
            final int endY = y + image._height;
            final int centerX = (x + endX) / 2;
            final int centerY = (y + endY) / 2;
            final int l = Util.mmToDot(printData._stampSizeMm);
            final int newX = centerX - l / 2;
            final int newY = centerY - l / 2;
            final int newEndX = centerX + l / 2;
            final int newHeight = l;

            final SvfForm.ImageField newImage = image.setFieldname("STAMP").setX(newX).setY(newY).setEndX(newEndX).setHeight(newHeight);
            svfForm.removeImageField(image);
            svfForm.addImageField(newImage);
            if (param._isOutputDebug) {
                log.info("move stamp (" + x + ", " + y + ", len = " + image._height + ") to (" + newX + ", " + newY + ", len = " + l + ")");
            }
        }

        private double getFieldRepeatPitch(final String fieldname) {
            Field field = _svfForm.getField(fieldname);
            if (null != field && null != field._repeatConfig) {
                return SvfFieldAreaInfo.KNJSvfFieldModify.charPointToPixel("", field._repeatConfig._repeatPitch, 0);
            }
            return 0;
        }

        private void setForm(final String formname) {
            _svf.VrSetForm(formname, 1);  //成績証明書(和)
            _formname = formname;
            if (null != formname && null == _param._formInfo.get(formname)) {
                _param._formInfo.put(formname, SvfField.getSvfFormFieldInfoMapGroupByName(_svf));
            }
        }

        private void vrsOut(final String fieldname, final String data) {
            if (_param._isOutputDebugAll) {
                if (null == data) {
                    log.info(" null (\"" + fieldname + "\")");
                } else {
                    log.info("VrsOut(\"" + fieldname + "\", " + data + ")");
                    SvfField field = getField(fieldname);
                    if (null == field) {
                        log.info(" no such field : \"" + fieldname + "\"");
                    }
                }
            }
            if (null != data) {
                _svf.VrsOut(fieldname, data);
            }
        }

        private void vrAttribute(final String fieldname, final String attribute) {
            if (_param._isOutputDebugAll) {
                if (null == attribute) {
                    log.info(" null (\"" + fieldname + "\")");
                } else {
                    log.info("VrAttribute(\"" + fieldname + "\", " + attribute + ")");
                    SvfField field = getField(fieldname);
                    if (null == field) {
                        log.info(" no such field : \"" + fieldname + "\"");
                    }
                }
            }
            if (null != attribute) {
                _svf.VrAttribute(fieldname, attribute);
            }
        }

        private void vrsOutSelect(final String[][] fieldLists, final String data) {
            final int datasize = KNJ_EditEdit.getMS932ByteLength(data);
            String[] fieldFound = null;
            boolean output = false;
            searchField:
            for (int i = 0; i < fieldLists.length; i++) {
                final String[] fieldnameList = fieldLists[i];
                int totalKeta = 0;
                int ketaMin = -1;
                for (int j = 0; j < fieldnameList.length; j++) {
                    final String fieldname = fieldnameList[j];
                    final SvfField svfField = getField(fieldname);
                    if (null == svfField) {
                        continue searchField;
                    }
                    totalKeta += svfField._fieldLength;
                    if (ketaMin == -1) {
                        ketaMin = svfField._fieldLength;
                    } else {
                        ketaMin = Math.min(ketaMin, svfField._fieldLength);
                    }
                    fieldFound = fieldnameList;
                }
                if (datasize <= totalKeta) {
                    final List<String> tokenList = KNJ_EditKinsoku.getTokenList(data, ketaMin); // fieldListの桁数はすべて同じ前提
                    if (tokenList.size() <= fieldnameList.length) {
                        for (int j = 0; j < Math.min(tokenList.size(), fieldnameList.length); j++) {
                            vrsOut(fieldnameList[j], tokenList.get(j));
                        }
                        output = true;
                        break searchField;
                    }
                }
            }
            if (!output && null != fieldFound) {
                final String[] fieldnameList = fieldFound;
                int ketaMin = -1;
                for (int j = 0; j < fieldnameList.length; j++) {
                    final String fieldname = fieldnameList[j];
                    final SvfField svfField = getField(fieldname);
                    if (ketaMin == -1) {
                        ketaMin = svfField._fieldLength;
                    } else {
                        ketaMin = Math.min(ketaMin, svfField._fieldLength);
                    }
                    fieldFound = fieldnameList;
                }
                final List<String> tokenList = KNJ_EditKinsoku.getTokenList(data, ketaMin);
                for (int j = 0; j < Math.min(tokenList.size(), fieldnameList.length); j++) {
                    vrsOut(fieldnameList[j], tokenList.get(j));
                }
                output = true;
            }
        }

        private void vrsOutn(final String fieldname, final int gyo, final String data) {
            if (_param._isOutputDebugAll) {
                if (null == data) {
                    log.info(" null (\"" + fieldname + "\", " + gyo + ")");
                } else {
                    log.info("VrsOutn(\"" + fieldname + "\", " + gyo + ", " + data + ")");
                    SvfField field = getField(fieldname);
                    if (null == field) {
                        log.info(" no such field : \"" + fieldname + "\", " + gyo);
                    }
                }
            }
            if (null != data) {
                _svf.VrsOutn(fieldname, gyo, data);
            }
        }

        private void vrAttributen(final String fieldname, final int gyo, final String attribute) {
            if (_param._isOutputDebugAll) {
                if (null == attribute) {
                    log.info(" null (\"" + fieldname + "\", " + gyo + ")");
                } else {
                    log.info("VrAttributen(\"" + fieldname + "\", " + gyo + ", " + attribute + ")");
                    SvfField field = getField(fieldname);
                    if (null == field) {
                        log.info(" no such field : \"" + fieldname + "\", " + gyo);
                    }
                }
            }
            if (null != attribute) {
                _svf.VrAttributen(fieldname, gyo, attribute);
            }
        }

        private SvfField getField(final String fieldname) {
            if (null == _formname) {
                log.warn("form not set!");
            }
            SvfField field = Util.getMappedMap(_param._formInfo, _formname).get(fieldname);
            if (_param._isOutputDebugAll) {
                log.info(" field " + fieldname + " = " + field);
            }
            return field;
        }

        /**
         *  学校情報
         */
        private void printSchoolInfo(final DB2UDB db2) {
            _printData.setCertifSchoolDat(db2, _param);
            final boolean notOutputPrincipalName = "2".equals(_printData._paramap.get("OUTPUT_PRINCIPAL"));

            final String psKey = "PS_SCHOOLINFO";
            if (!_param._psMap.containsKey(psKey)) {
                try {
                    //  学校データ
                    final String sql = getSchoolInfoSql();
                    if (_param._isOutputDebug) {
                        log.info(" schoolinfo sql = " + sql);
                    }
                    _param._psMap.put(psKey, db2.prepareStatement(sql));
                } catch (Exception e) {
                    log.error("[KNJE080J_1]pre_stat error! ", e);
                }
            }

            final String dateYear = (_printData._date != null) ? KNJG010_1.b_year(_printData._date): _printData._ctrlYear;
            final Object[] arg;
            if (_param._isKindai) {
                arg = new Object[] {dateYear, _printData._ctrlYear,};
            } else {
                arg = new Object[] {_printData._ctrlYear, };
            }

            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, _param._psMap.get(psKey), arg));
            if (_param._isOutputDebug) {
                log.info(" school info arg = " + ArrayUtils.toString(arg));
                log.info(" school info row = " + row);
            }
            if (!row.isEmpty()) {
                if (!_param._isKindai) {
                    vrsOut("SYOSYO_NAME", getString(row, "SYOSYO_NAME"));
                    vrsOut("SYOSYO_NAME2", getString(row, "SYOSYO_NAME2"));
                }

                vrsOut("SCHOOLNAME",  defstr(_printData._certifSchoolDatSchoolName, getString(row, "SCHOOLNAME1")));      //学校名
                if (null == getField("DATE") && null != getField("DATE_REP")) {
                    final List<Tuple<String, Integer>> dataAndYList = formatDateList(db2, _printData._date);
                    for (int i = 0; i < dataAndYList.size(); i++) {
                        final String data = dataAndYList.get(i)._first;
                        final Integer y = dataAndYList.get(i)._second;
                        vrsOutn("DATE_REP", i + 1, data);   //記載日
                        vrAttributen("DATE_REP", i + 1, attributeY("DATE_REP", i + 1, y));
                    }
                } else {
                    vrsOut("DATE", !StringUtils.isBlank(_printData._date) ? KNJ_EditDate.getAutoFormatDate(db2, _printData._date) : "　　年 　月 　日");   //記載日
                }
                if (_param._isSeijyo) {
                    vrsOut("SCHOOL_ADDR", getString(row, "REMARK1"));
                }
                if (_param._isChiyodaKudan) {
                    vrsOut("SCHOOL_ADDR", getString(row, "SCHOOLADDR1"));
                    vrsOut("SCHOOL_TELNO", getString(row, "SCHOOLTELNO"));
                }
                if (!_param._isKindai) {
                    vrsOut("JOBNAME", defstr(_printData._certifSchoolDatJobName, getString(row, "PRINCIPAL_JOBNAME")));    //役職名
                }
                if (!notOutputPrincipalName) {
                    vrsOut("STAFFNAME", defstr(_printData._certifSchoolDatPrincipalName, getString(row, "PRINCIPAL_NAME")));    //校長名
                }
                if (_param._isSakae) {
                    vrsOut("CORP_NAME", getString(row, "REMARK3"));    //役職名
                }
            }
            if (_printData._isPrintStamp) {
                vrsOut("STAMP", _param._certifSchoolstampImagePath); // 校長印影
            }
        }

        private String attributeY(final String fieldname, final int i, final Integer plusy) {
            final SvfField field = getField(fieldname);
            final int fieldY = field.y();
            final double pitch = getFieldRepeatPitch(fieldname) / 4;
            final String rtn = "Y=" + String.valueOf((int) (fieldY + pitch * i + plusy));
//            log.info(" " + fieldname + ", pitch = " + pitch + ", i = " + i + ", plusy = " + plusy + " = " + rtn);
            return rtn;
        }

        private String getSchoolInfoSql() {

            final StringBuffer sql = new StringBuffer();
            final String q = "?";

            sql.append(" SELECT ");
            sql.append("     T1.YEAR ");
            sql.append("     ,T1.SCHOOLZIPCD");
            sql.append("     ,T1.SCHOOLADDR1");
            sql.append("     ,T1.SCHOOLADDR2");
            sql.append("     ,T1.SCHOOLTELNO");
            sql.append("     ,T1.SCHOOLDIV ");
            if (_param._isKindai) {
                sql.append("     ,T1.SCHOOLNAME1");
                sql.append("     ,T2.STAFFCD AS PRINCIPAL_STAFFCD");
                sql.append("     ,T2.STAFFNAME AS PRINCIPAL_NAME");
                sql.append("     ,T2.JOBNAME AS PRINCIPAL_JOBNAME ");
                sql.append(" FROM ");
                sql.append("      SCHOOL_MST T1 "); //  学校
                //  校長
                sql.append(" LEFT JOIN (");
                sql.append("     SELECT ");
                sql.append("         W1.YEAR,");
                sql.append("         W1.STAFFCD,");
                sql.append("         STAFFNAME,");
                sql.append("         JOBNAME ");
                sql.append("     FROM ");
                sql.append("         STAFF_YDAT W1 ");
                sql.append("         INNER JOIN STAFF_MST W2 ON W2.STAFFCD = W1.STAFFCD ");
                sql.append("         LEFT JOIN JOB_MST W3 ON W3.JOBCD = W2.JOBCD ");
                sql.append("     WHERE ");
                sql.append("         W1.YEAR = " + q + " AND (W2.JOBCD = '0001' OR W2.JOBCD = '0005') ");
                sql.append(" ) T2 ON T2.YEAR = T1.YEAR ");
            } else {
                sql.append("    ,T2.SCHOOL_NAME AS SCHOOLNAME1");
                sql.append("    ,T2.SYOSYO_NAME ");
                sql.append("    ,T2.SYOSYO_NAME2 ");
                sql.append("    ,T2.CERTIF_NO ");
                sql.append("    ,T2.JOB_NAME AS PRINCIPAL_JOBNAME ");
                sql.append("    ,T2.PRINCIPAL_NAME ");
                sql.append("    ,T2.REMARK1");
                sql.append("    ,T2.REMARK2");
                sql.append("    ,T2.REMARK3");
                sql.append("    ,T2.REMARK4");
                sql.append("    ,T2.REMARK5");
                sql.append("    ,T2.REMARK6");
                sql.append("    ,T2.REMARK7");
                sql.append("    ,T2.REMARK8");
                sql.append("    ,T2.REMARK9");
                sql.append("    ,T2.REMARK10 ");
                sql.append(" FROM ");
                sql.append("     SCHOOL_MST T1 ");
                sql.append("     LEFT JOIN CERTIF_SCHOOL_DAT T2 ON T2.CERTIF_KINDCD = " + CERTIF_KINDCD + " AND T1.YEAR = T2.YEAR ");
            }
            sql.append(" WHERE ");
            sql.append("     T1.YEAR = " + q + " ");
            if (_param._hasSCHOOL_MST_SCHOOL_KIND) {
                sql.append(" AND T1.SCHOOL_KIND = '" + "J" + "' ");
            }

            return sql.toString();
        }

        /**
         *  個人情報
         */
        private void printPersonalInfo(final DB2UDB db2) {
            final String schoolkind = _printData.getSchoolKind(db2);

            if (_param._isKindai) {
                vrsOut("CERTIFNO", StringUtils.defaultString(_printData._certifNumber, "     "));     //証明書番号
            } else {
                vrsOut("CERTIF_NO", StringUtils.defaultString(_printData._certifNumber, "     "));     //証明書番号
            }

            final String psKey = "PS_PERSONAL";
            if (!_param._psMap.containsKey(psKey)) {
                try {
                    //  個人データ
                    final String sql = new KNJ_PersonalinfoSql().sql_info_reg("111100101000J");
                    _param._psMap.put(psKey, db2.prepareStatement(sql));
                } catch (Exception e) {
                    log.error("[KNJE080J_1]pre_stat error! ", e);
                }
            }

            final Map<String, String> rsmap = KnjDbUtils.firstRow(KnjDbUtils.query(db2, _param._psMap.get(psKey), new Object[] {_printData._schregno, _printData._year, _printData._semester, _printData._schregno, _printData._year}));
            if (null == rsmap || rsmap.isEmpty()) {
                return;
            }

            final String useRealName = getString(rsmap, "USE_REAL_NAME");
            final String name = "1".equals(useRealName) || "1".equals(_param._certifPrintRealName) ? getString(rsmap, "REAL_NAME") : getString(rsmap, "NAME");
            final String nameKana = "1".equals(useRealName) || "1".equals(_param._certifPrintRealName) ? getString(rsmap, "REAL_NAME_KANA") : getString(rsmap, "NAME_KANA");
            final String entDate = getString(rsmap, "ENT_DATE");
            final String grdDiv = getString(rsmap, "GRD_DIV");
            final String grdDate = getString(rsmap, "GRD_DATE");
            final String graduDate = getString(rsmap, "GRADU_DATE");
            final String birthday = getString(rsmap, "BIRTHDAY");
            if (name != null) {
                vrsOutSelect(new String[][] {{"NAME"}, {"NAME2"}, {"NAME3"}}, name);
            }
            if (null != nameKana) {
                vrsOutSelect(new String[][] {{"NAME_KANA"}, {"NAME_KANA2"}, {"NAME_KANA3"}}, nameKana);
            }
            if (entDate != null) {
                final String format;
                if (_param._isMatsudo) {
                    format = formatDate(db2, entDate);
                } else {
                    format = formatMonth(db2, entDate);
                }
                vrsOut("ENT_DATE", format + "入学");
            }
            if (grdDate != null) {
                final String format;
                if (_param._isMatsudo) {
                    format = formatDate(db2, grdDate);
                } else {
                    format = formatMonth(db2, grdDate);
                }
                vrsOut("GRAD_DATE", format + "卒業");
            } else if (graduDate != null) {
                if ("H".equals(schoolkind)) {
                    // 現在高校生の場合、表示しない
                } else {
                    vrsOut("GRAD_DATE", formatMonth(db2, graduDate) + "卒業予定");
                }
            }
            final String gradeCd = getString(rsmap, "GRADE_CD");
            if (NumberUtils.isDigits(gradeCd) && _param._isJogakkan) {
                final boolean hasRegdH = "1".equals(KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT DISTINCT 1 FROM SCHREG_REGD_DAT T1 INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE WHERE T1.SCHREGNO = ? AND T2.SCHOOL_KIND = 'H' ", new Object[] {_printData._schregno})));
                if (!hasRegdH && (null == grdDiv || "4".equals(grdDiv))) {
                    vrsOut("GRADE", "第" + StringUtils.defaultString(KNJ_EditEdit.convertKansuuji(String.valueOf(Integer.parseInt(gradeCd)))) + "学年");
                }
            }
            if (birthday != null) {
                if (null == getField("BIRTHDAY") && null != getField("BIRTHDAY_REP")) {
                    final List<Tuple<String, Integer>> array = formatBirthdayList(db2, _param._isSeireki || "1".equals(getString(rsmap, "BIRTHDAY_FLG")), birthday);
                    for (int i = 0; i < array.size(); i++) {
                        final String data = array.get(i)._first;
                        final Integer y = array.get(i)._second;
                        vrsOutn("BIRTHDAY_REP", i + 1, data);
                        vrAttributen("BIRTHDAY_REP", i + 1, attributeY("BIRTHDAY_REP", i + 1, y));
                    }
                } else {
                    vrsOut("BIRTHDAY", formatBirthday(db2, _param._isSeireki || "1".equals(getString(rsmap, "BIRTHDAY_FLG")), birthday));
                }
            }
            if (_param._isKindai) {
                final String enterName = getString(rsmap, "ENTER_NAME");
                if (enterName != null) {
                    if ( -1 < enterName.lastIndexOf("入学")) {
                        vrsOut("TRANSFER1", "入学");
                    } else {
                        vrsOut("TRANSFER1", enterName);
                    }
                }
            }
            vrsOut("SEX", getString(rsmap, "SEX"));
        }

        private List<Tuple<String, Integer>> formatDateList(final DB2UDB db2, final String date) {
            if (StringUtils.isBlank(date)) {
                final String data = "　　　　年 　　月　 　日";
                final List<Tuple<String, Integer>> rtn = new ArrayList<Tuple<String, Integer>>();
                for (int i = 0; i < data.length(); i++) {
                    rtn.add(Tuple.of(String.valueOf(data.charAt(i)), 0));
                }
                return rtn;
            }
            final List<Tuple<String, Integer>> rtn = new ArrayList<Tuple<String, Integer>>();
            if (_param._isSeireki) {
                final String s = KNJ_EditDate.h_format_SeirekiJP(date);
                if (null != s) {
                    for (int i = 0; i < s.length(); i++) {
                        rtn.add(Tuple.of(String.valueOf(s.charAt(i)), 0));
                    }
                }
            } else {
                final String[] arr = KNJ_EditDate.tate2_format(KNJ_EditDate.h_format_JP(db2, date));
                int py = 0;
                for (final String token : KNJ_EditKinsoku.getTokenList(arr[0], 2)) {
                    rtn.add(Tuple.of(token, py)); // 元号
                }
                py += 20;
                rtn.add(Tuple.of(hankaku1DigitToZenkakuDigit(arr[1]), py));
                rtn.add(Tuple.of(arr[2], py)); // "年"
                py += 20;
                rtn.add(Tuple.of(hankaku1DigitToZenkakuDigit(arr[3]), py));
                rtn.add(Tuple.of(arr[4], py)); // "月"
                py += 20;
                rtn.add(Tuple.of(hankaku1DigitToZenkakuDigit(arr[5]), py));
                rtn.add(Tuple.of(arr[6], py)); // "日"
            }
//            log.info(" array = " + rtn);
            return rtn;
        }

        private String hankaku1DigitToZenkakuDigit(final String digit) {
            if (null == digit) {
                return "";
            }
            if (digit.length() == 1) {
                final char c = digit.charAt(0);
                if ('0' <= c && c <= '9') {
                    return String.valueOf((char) (c - '0' + '０'));
                }
            }
            return digit;
        }

        private String formatDate(final DB2UDB db2, final String date) {
            if (_param._isSeireki) {
                return KNJ_EditDate.h_format_SeirekiJP(date);
            }
            return KNJ_EditDate.h_format_JP(db2, date);
        }

        private String formatMonth(final DB2UDB db2, final String date) {
            if (_param._isSeireki) {
                return KNJ_EditDate.getAutoFormatYM(db2, date);
            }
            return KNJ_EditDate.h_format_JP_M(db2, date);
        }

        private String formatBirthday(final DB2UDB db2, final boolean isSeireki, final String birthday) {
            if (null == birthday) {
                return "";
            }
            if (isSeireki) {
                final Calendar cal = Util.toCalendar(birthday);
                final SimpleDateFormat df = new SimpleDateFormat("yyyy年M月d日生");
                return df.format(cal.getTime());
            }
            return KNJ_EditDate.h_format_JP_Bth(db2, birthday);
        }

        private List<Tuple<String, Integer>> formatBirthdayList(final DB2UDB db2, final boolean isSeireki, final String birthday) {
            if (StringUtils.isBlank(birthday)) {
                return Arrays.asList(Tuple.of("", 0));
            }
            final List<Tuple<String, Integer>> rtn = new ArrayList<Tuple<String, Integer>>();
            if (isSeireki) {
                final Calendar cal = Util.toCalendar(birthday);
                final SimpleDateFormat df = new SimpleDateFormat("yyyy年M月d日生");
                final String s = df.format(cal.getTime());
                if (null != s) {
                    for (int i = 0; i < s.length(); i++) {
                        rtn.add(Tuple.of(String.valueOf(s.charAt(i)), 0));
                    }
                }
            } else {
                final String[] arr = KNJ_EditDate.tate2_format(KNJ_EditDate.h_format_JP(db2, birthday));
                int yp = 0;
                for (final String token : KNJ_EditKinsoku.getTokenList(arr[0], 2)) {
                    rtn.add(Tuple.of(token, yp)); // 元号
                }
                yp += 20;
                rtn.add(Tuple.of(hankaku1DigitToZenkakuDigit(arr[1]), yp));
                rtn.add(Tuple.of(arr[2], yp)); // "年"
                yp += 20;
                rtn.add(Tuple.of(hankaku1DigitToZenkakuDigit(arr[3]), yp));
                rtn.add(Tuple.of(arr[4], yp)); // "月"
                yp += 20;
                rtn.add(Tuple.of(hankaku1DigitToZenkakuDigit(arr[5]), yp));
                rtn.add(Tuple.of(arr[6], yp)); // "日"
                rtn.add(Tuple.of("生", yp));
            }
            log.info(" array = " + rtn); // TODO:
            return rtn;
        }

        /**
         *  出欠データ
         **/
        private void printAttend(final DB2UDB db2) {
            if (_param._isChiyodaKudan) {
                // 印字なし
                return;
            }
            final String psKey = "PS_ATTENDREC";
            if (!_param._psMap.containsKey(psKey)) {
                final String tname1 = "SCHREG_ATTENDREC_DAT";
                //  出欠記録データ
                final StringBuffer stb = new StringBuffer();
                stb.append( "SELECT DISTINCT ");
                stb.append(      "T1.YEAR,");
                stb.append(      "ANNUAL,");
                stb.append(      "VALUE(CLASSDAYS,0) AS CLASSDAYS,");                            //授業日数
                stb.append(      "CASE WHEN S1.SEM_OFFDAYS = '1' ");
                if (_param._isKindai) {
                    stb.append(         "THEN VALUE(CLASSDAYS,0) - VALUE(ABROAD,0) ");
                    stb.append(         "ELSE VALUE(CLASSDAYS,0) - VALUE(OFFDAYS,0) - VALUE(ABROAD,0) ");
                } else {
                    stb.append(         "THEN VALUE(CLASSDAYS,0) ");
                    stb.append(         "ELSE VALUE(CLASSDAYS,0) - VALUE(OFFDAYS,0) ");
                }
                stb.append(         "END AS ATTEND_1,"); //授業日数-休学日数:1
                stb.append(    "VALUE(SUSPEND,0) + VALUE(MOURNING,0) AS SUSP_MOUR,");          //出停・忌引
                stb.append(    "VALUE(SUSPEND,0) AS SUSPEND,");                                //出停:2
                stb.append(    "VALUE(MOURNING,0) AS MOURNING,");                              //忌引:3
                stb.append(    "VALUE(ABROAD,0) AS ABROAD,");                                  //留学:4
                stb.append(    "CASE WHEN S1.SEM_OFFDAYS = '1' ");
                stb.append(         "THEN VALUE(REQUIREPRESENT,0) + VALUE(OFFDAYS,0) ");
                stb.append(         "ELSE VALUE(REQUIREPRESENT,0) ");
                stb.append(         "END AS REQUIREPRESENT,"); //要出席日数:5
                stb.append(    "CASE WHEN S1.SEM_OFFDAYS = '1' ");
                stb.append(         "THEN VALUE(SICK,0) + VALUE(ACCIDENTNOTICE,0) + VALUE(NOACCIDENTNOTICE,0) + VALUE(OFFDAYS,0) ");
                stb.append(         "ELSE VALUE(SICK,0) + VALUE(ACCIDENTNOTICE,0) + VALUE(NOACCIDENTNOTICE,0) ");
                stb.append(         "END AS ATTEND_6,"); //病欠＋事故欠（届・無）:6
                stb.append(    "VALUE(PRESENT,0) AS PRESENT,");                                //出席日数:7
                stb.append(    "VALUE(MOURNING,0) + VALUE(SUSPEND,0) AS ATTEND_8 ");           //忌引＋出停:8
                stb.append("FROM ");
                stb.append(    "(");
                stb.append(        "SELECT ");
                stb.append(            "SCHREGNO,");
                stb.append(            "YEAR,");
                stb.append(            "ANNUAL,");
                stb.append(            "SUM(CLASSDAYS) AS CLASSDAYS,");
                stb.append(            "SUM(OFFDAYS) AS OFFDAYS,");
                stb.append(            "SUM(ABSENT) AS ABSENT,");
                stb.append(            "SUM(SUSPEND) AS SUSPEND,");
                stb.append(            "SUM(MOURNING) AS MOURNING,");
                stb.append(            "SUM(ABROAD) AS ABROAD,");
                stb.append(            "SUM(REQUIREPRESENT) AS REQUIREPRESENT,");
                stb.append(            "SUM(SICK) AS SICK,");
                stb.append(            "SUM(ACCIDENTNOTICE) AS ACCIDENTNOTICE,");
                stb.append(            "SUM(NOACCIDENTNOTICE) AS NOACCIDENTNOTICE,");
                stb.append(            "SUM(PRESENT) AS PRESENT ");
                stb.append(       " FROM ");
                stb.append(            tname1);
                stb.append(       " WHERE ");
                stb.append(                "SCHREGNO = ? ");
                stb.append(            "AND YEAR <= ? ");
//            		if ("on".equals(notPrintAnotherAttendrec)) {
//            		    stb.append(        "AND SCHOOLCD <> '1' ");
//            		}
                stb.append(        "GROUP BY ");
                stb.append(            "SCHREGNO,");
                stb.append(            "ANNUAL,");
                stb.append(            "YEAR ");
                stb.append(    ")T1 ");
                stb.append(    "LEFT JOIN SCHOOL_MST S1 ON S1.YEAR = T1.YEAR ");
                if (_param._hasSCHOOL_MST_SCHOOL_KIND) {
                    stb.append(    "    AND S1.SCHOOL_KIND = 'J' ");
                }
                stb.append(    "LEFT JOIN (SELECT REGD.YEAR, REGD.SCHREGNO ");
                stb.append(    "           FROM SCHREG_REGD_DAT REGD ");
                stb.append(    "           INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGD.YEAR ");
                stb.append(    "                                          AND  GDAT.GRADE = REGD.GRADE ");
                stb.append(    "                                          AND  GDAT.SCHOOL_KIND <> 'J' ");
                stb.append(    "           GROUP BY REGD.YEAR, REGD.SCHREGNO ");
                stb.append(    "           ) L2 ON L2.SCHREGNO = T1.SCHREGNO ");
                stb.append(    "               AND L2.YEAR = T1.YEAR ");
                stb.append(    " WHERE ");
                stb.append(    "    L2.YEAR IS NULL ");
                stb.append("ORDER BY ");
                stb.append(    "T1.ANNUAL");
                try {
                    _param._psMap.put(psKey, db2.prepareStatement(stb.toString()));
                } catch (Exception e) {
                    log.error("[KNJE080J_1]pre_stat error! ", e);
                }
            }

            for (final Map<String, String> row : KnjDbUtils.query(db2, _param._psMap.get(psKey), new Object[] {_printData._schregno, _printData._year})) {
                final String annual = KnjDbUtils.getString(row, "ANNUAL");
                final int i;
                final String gradeCd = _printData._gradeGradeCdMap.get(annual);
                if (NumberUtils.isDigits(gradeCd)) {
                    i = Integer.parseInt(gradeCd);
                } else {
                    i = Integer.parseInt(annual);
                }
                vrsOutn("ATTEND",  i,  KnjDbUtils.getString(row, "PRESENT"));  //出席
                vrsOutn("PRESENT", i,  KnjDbUtils.getString(row, "REQUIREPRESENT"));  //要出席
                vrsOutn("CLASSDAYS", i,  KnjDbUtils.getString(row, "CLASSDAYS"));  //授業日数 専大松戸
            }
        }

        /**
         *  学習の記録
         */
        private void printStudyrec(final DB2UDB db2) {
            final String psKey = "PS_STUDYREC";
            if (!_param._psMap.containsKey(psKey)) {
                //  学習記録データ
                final String sql = getStudyrecSql(_param);
                if (_param._isOutputDebug) {
                    log.info(" sqlStudyrec = " + sql);
                }
                try {
                    _param._psMap.put(psKey, db2.prepareStatement(sql));
                } catch (Exception e) {
                    log.error("[KNJE080J_1]pre_stat error! ", e);
                }
            }

            final Object[] arg = {
                    _printData._schregno,
                    _printData._year,
            };
            if (_param._isOutputDebug) {
                log.info(" studyrec arg = " + ArrayUtils.toString(arg));
            }
            final List<Map<String, String>> studyrecList = KnjDbUtils.query(db2, _param._psMap.get(psKey), arg);

            final int maxLine = _param._isMatsudo ? 40 : 10;
            String s_classcd = null;  //教科コード
            String s_subclasscd = null;  //教科コード
            int linex = 0;               //行数
            for (int i = 0; i < studyrecList.size(); i++) {
                final Map<String, String> m = studyrecList.get(i);
                final String classname = getString(m, "CLASSNAME");
                final String subclassname = getString(m, "SUBCLASSNAME");
                final String subclasscd = getString(m, "SUBCLASSCD");

                if (_param._isOutputDebugAll) {
                    log.info(" record " + m);
                }

                if (_param._isMatsudo) {
                    //科目コードの変わり目
                    final String classcd = getString(m, "CLASSCD");
                    if (s_subclasscd == null || !subclasscd.equals(s_subclasscd) || s_classcd == null || !classcd.equals(s_classcd)) {
                        linex++;
                        if (maxLine < linex) {
                            break;  //行のオーバーフロー
                        }
                        vrsOutn("SUBCLASSNAME", linex, subclassname);  //科目名
                        s_subclasscd = subclasscd;
                        if (null == s_classcd || !s_classcd.equals(classcd)) {
                            vrsOutn("CLASSNAME", linex, classname);  //教科名
                            s_classcd = classcd;
                        }
                    }
                } else {
                    //教科コードの変わり目
                    final String classcd = getString(m, "CLASSCD");
                    if (s_classcd == null || !classcd.equals(s_classcd)) {
                        linex++;
                        if (maxLine < linex) {
                            break;  //行のオーバーフロー
                        }
                        final String field;
                        if (_param._isMatsudo) {
                            field = "CLASSNAME";
                        } else {
                            field = "SUBCLASSNAME";
                        }
                        vrsOutn(field, linex, classname);  //教科名
                        s_classcd = classcd;
                    }
                }

                //学年ごとの出力
                final String hyotei;
                if (_printData._d065Name1List.contains(subclasscd)) {
                    hyotei = _param._d001Abbv1Map.get(getString(m, "GRADES"));
                } else {
                    hyotei = getString(m, "GRADES");
                }
                final String annual = getString(m, "ANNUAL");
                final int g;
                final String gradeCd = _printData._gradeGradeCdMap.get(annual);
                if (NumberUtils.isDigits(gradeCd)) {
                    g = Integer.parseInt(gradeCd);
                } else {
                    log.info(" gradeCd = " + gradeCd + " / annual = " + annual + ", " + _printData._gradeGradeCdMap);
                    g = Integer.parseInt(annual);
                }
                if (1 <= g && g <= 3 && hyotei != null) {
                    vrsOutn("GRADES" + g, linex, hyotei);  //評定
                }
            }
        }

        private String getStudyrecSql(final Param param) {
            final boolean useD065 = false;

//            String CONFIG_PRINT_GRD = "PRINT_GRD";
            final StringBuffer stb = new StringBuffer();

            final boolean notUseStudyrecProvFlgDat = false; // null != paramMap.get("notUseStudyrecProvFlgDat");
            if (param._isKindai) {
                stb.append("WITH STUDYREC AS(");
                stb.append("SELECT  T1.CLASSNAME, ");
                stb.append("        T1.SUBCLASSNAME, ");
                stb.append("        T1.SCHREGNO, ");
                stb.append("        T1.YEAR, ");
                stb.append("        T1.ANNUAL, ");
                stb.append("        T1.CLASSCD, ");
                stb.append("        T1.SCHOOL_KIND, ");
                stb.append("        T1.CURRICULUM_CD, ");
                stb.append("        VALUE(L2.SUBCLASSCD2, T1.SUBCLASSCD) AS SUBCLASSCD, ");
                stb.append("        T1.VALUATION AS GRADES ");
                stb.append("       ,CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) ELSE T1.GET_CREDIT END AS CREDIT ");
                if (useD065) {
                    stb.append("        ,NMD065.NAME1 AS D065FLG ");
                } else {
                    stb.append("        ,CAST(NULL AS VARCHAR(1)) AS D065FLG ");
                }
                stb.append("FROM   SCHREG_STUDYREC_DAT T1 ");
                stb.append("        LEFT JOIN SUBCLASS_MST L2 ON L2.CLASSCD = T1.CLASSCD AND L2.SCHOOL_KIND = T1.SCHOOL_KIND AND L2.CURRICULUM_CD = T1.CURRICULUM_CD AND L2.SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append("        LEFT JOIN STUDYREC_PROV_FLG_DAT TPROV ON TPROV.SCHOOLCD = T1.SCHOOLCD ");
                stb.append("            AND TPROV.YEAR = T1.YEAR ");
                stb.append("            AND TPROV.SCHREGNO = T1.SCHREGNO ");
                stb.append("            AND TPROV.CLASSCD = T1.CLASSCD ");
                stb.append("            AND TPROV.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("            AND TPROV.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("            AND TPROV.SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append("            AND TPROV.PROV_FLG = '1' ");
                if (useD065) {
                    stb.append("   LEFT JOIN V_NAME_MST NMD065 ON NMD065.YEAR = T1.YEAR AND NMD065.NAMECD1 = 'D065' AND NMD065.NAME1 = T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ");
                }
                stb.append("WHERE   T1.SCHREGNO = ? AND ");
                stb.append("        T1.YEAR <= ? AND ");
                stb.append("        (T1.CLASSCD BETWEEN '" + KNJDefineCode.subject_D + "' AND '" + KNJDefineCode.subject_U + "' OR T1.CLASSCD = '" + KNJDefineCode.subject_T + "') ");
                if (param._hasSCHOOL_MST_SCHOOL_KIND) {
                    stb.append("        AND T1.SCHOOL_KIND = 'J' ");
                }
                stb.append("         AND NOT EXISTS(SELECT  'X' ");
                stb.append("                   FROM    SUBCLASS_REPLACE_COMBINED_DAT T2 ");
                stb.append("                   WHERE   T2.YEAR = T1.YEAR ");
                stb.append("                       AND T2.ATTEND_CLASSCD = T1.CLASSCD ");
                stb.append("                       AND T2.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("                       AND T2.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("                       AND T2.ATTEND_SUBCLASSCD = T1.SUBCLASSCD) ");
                stb.append("         AND TPROV.SUBCLASSCD IS NULL ");
                stb.append("    )");

            } else {
                stb.append("WITH T_STUDYREC AS(");
                stb.append("SELECT  T1.CLASSNAME ");
                stb.append("      , T1.SUBCLASSNAME ");
                stb.append("      , T1.SCHREGNO ");
                stb.append("      , T1.YEAR ");
                stb.append("      , T1.ANNUAL ");
                stb.append("      , T1.CLASSCD ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("      , T1.SCHOOL_KIND ");
                    stb.append("      , T1.CURRICULUM_CD ");
                }
                stb.append("      , T1.SUBCLASSCD ");
                stb.append("      , L2.SUBCLASSCD2 ");
                stb.append("      , T1.VALUATION AS GRADES ");
                stb.append("      , CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) ELSE T1.GET_CREDIT END AS CREDIT ");
                stb.append("      , CAST(NULL AS VARCHAR(1)) AS D065FLG ");
                stb.append("FROM   SCHREG_STUDYREC_DAT T1 ");
                stb.append("        LEFT JOIN SUBCLASS_MST L2 ON L2.SUBCLASSCD = T1.SUBCLASSCD ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("         AND L2.CLASSCD = T1.CLASSCD ");
                    stb.append("         AND L2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                    stb.append("         AND L2.CURRICULUM_CD = T1.CURRICULUM_CD ");
                }
                if (notUseStudyrecProvFlgDat) {
                } else {
                    stb.append("        LEFT JOIN STUDYREC_PROV_FLG_DAT TPROV ON TPROV.SCHOOLCD = T1.SCHOOLCD ");
                    stb.append("            AND TPROV.YEAR = T1.YEAR ");
                    stb.append("            AND TPROV.SCHREGNO = T1.SCHREGNO ");
                    if ("1".equals(param._useCurriculumcd)) {
                        stb.append("            AND TPROV.CLASSCD = T1.CLASSCD ");
                        stb.append("            AND TPROV.SCHOOL_KIND = T1.SCHOOL_KIND ");
                        stb.append("            AND TPROV.CURRICULUM_CD = T1.CURRICULUM_CD ");
                    }
                    stb.append("            AND TPROV.SUBCLASSCD = T1.SUBCLASSCD ");
                    stb.append("            AND TPROV.PROV_FLG = '1' ");
                }
                stb.append("WHERE   T1.SCHREGNO = ? ");
                stb.append("    AND T1.YEAR <= ? ");
                stb.append("    AND (T1.CLASSCD BETWEEN '" + KNJDefineCode.subject_D + "' AND '" + KNJDefineCode.subject_U + "' OR T1.CLASSCD = '" + KNJDefineCode.subject_T + "') ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("        AND T1.SCHOOL_KIND = 'J' ");
                }

                if (notUseStudyrecProvFlgDat) {
                } else {
                    stb.append("            AND TPROV.SUBCLASSCD IS NULL ");
                }
                stb.append(") ");
                stb.append(" , STUDYREC0 AS( ");
                stb.append("    SELECT ");
                stb.append("        T1.CLASSNAME ");
                stb.append("      , T1.SUBCLASSNAME ");
                stb.append("      , T1.SCHREGNO ");
                stb.append("      , T1.YEAR ");
                stb.append("      , T1.ANNUAL ");
                stb.append("      , T1.CLASSCD ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("      , T1.SCHOOL_KIND ");
                    stb.append("      , T1.CURRICULUM_CD ");
                }
                stb.append("      , T1.SUBCLASSCD ");
                stb.append("      , T1.GRADES ");
                stb.append("      , T1.CREDIT ");
                stb.append("      , D065FLG ");
                stb.append("    FROM ");
                stb.append("        T_STUDYREC T1 ");
                stb.append("    WHERE ");
                stb.append("        T1.SUBCLASSCD2 IS NULL ");
                stb.append("    UNION ALL ");
                stb.append("    SELECT ");
                stb.append("        MAX(T1.CLASSNAME) AS CLASSNAME ");
                stb.append("      , MAX(T1.SUBCLASSNAME) AS SUBCLASSNAME ");
                stb.append("      , T1.SCHREGNO ");
                stb.append("      , T1.YEAR ");
                stb.append("      , MAX(T1.ANNUAL) AS ANNUAL ");
                stb.append("      , T1.CLASSCD ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("      , T1.SCHOOL_KIND ");
                    stb.append("      , T1.CURRICULUM_CD ");
                }
                stb.append("      , T1.SUBCLASSCD2 AS SUBCLASSCD ");
                stb.append("      , MAX(T1.GRADES) AS GRADES ");
                stb.append("      , SUM(T1.CREDIT) AS CREDIT ");
                stb.append("      , MAX(D065FLG) AS D065FLG ");
                stb.append("    FROM ");
                stb.append("        T_STUDYREC T1 ");
                stb.append("    WHERE ");
                stb.append("        T1.SUBCLASSCD2 IS NOT NULL ");
                stb.append("    GROUP BY ");
                stb.append("        T1.SCHREGNO ");
                stb.append("      , T1.YEAR ");
                stb.append("      , T1.CLASSCD ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("  ,     T1.SCHOOL_KIND ");
                    stb.append("  ,     T1.CURRICULUM_CD ");
                }
                stb.append("      , T1.SUBCLASSCD2 ");
                stb.append(") ");

                stb.append(" , STUDYREC AS( ");
                stb.append("    SELECT ");
                stb.append("        T1.CLASSNAME ");
                stb.append("      , T1.SUBCLASSNAME ");
                stb.append("      , T1.SCHREGNO ");
                stb.append("      , T1.YEAR ");
                stb.append("      , T1.ANNUAL ");
                stb.append("      , T1.CLASSCD ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("      , T1.SCHOOL_KIND ");
                    stb.append("      , T1.CURRICULUM_CD ");
                }
                stb.append("      , T1.SUBCLASSCD ");
                stb.append("      , D065FLG ");
                stb.append("      , MAX(T1.GRADES) AS GRADES ");
                stb.append("    FROM ");
                stb.append("        STUDYREC0 T1 ");
                stb.append("    GROUP BY ");
                stb.append("        T1.CLASSNAME ");
                stb.append("      , T1.SUBCLASSNAME ");
                stb.append("      , T1.SCHREGNO ");
                stb.append("      , T1.YEAR ");
                stb.append("      , T1.ANNUAL ");
                stb.append("      , T1.CLASSCD ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("      , T1.SCHOOL_KIND ");
                    stb.append("      , T1.CURRICULUM_CD ");
                }
                stb.append("      , T1.SUBCLASSCD ");
                stb.append("      , T1.D065FLG ");
                stb.append(") ");
            }

            stb.append(" SELECT ");
            stb.append("     VALUE(CLM.SPECIALDIV, '0') as SPECIALDIV ");
            stb.append("   , CLM.SHOWORDER2 as CLASS_ORDER ");
            stb.append("   , SUBM.SHOWORDER2 as SUBCLASS_ORDER ");
            stb.append("   , T1.ANNUAL ");
            stb.append("   , T1.YEAR ");
            stb.append("   , T1.CLASSCD ");
            stb.append("   , VALUE(T1.CLASSNAME, CLM.CLASSORDERNAME1, CLM.CLASSNAME) AS CLASSNAME ");
            stb.append("   , T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("   , VALUE(T1.SUBCLASSNAME, SUBM.SUBCLASSORDERNAME1, SUBM.SUBCLASSNAME) AS SUBCLASSNAME ");
            stb.append("   , T1.GRADES AS GRADES ");
            stb.append(" FROM ");
            stb.append("     STUDYREC T1 ");
            stb.append("     LEFT JOIN CLASS_MST CLM ON CLM.CLASSCD = T1.CLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND CLM.SCHOOL_KIND = T1.SCHOOL_KIND ");
            }
            stb.append("     LEFT JOIN SUBCLASS_MST SUBM ON SUBM.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND SUBM.CLASSCD = T1.CLASSCD ");
                stb.append("         AND SUBM.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("         AND SUBM.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append(" WHERE ");
            stb.append("     T1.CLASSCD BETWEEN '" + KNJDefineCode.subject_D + "' AND '" + KNJDefineCode.subject_U + "' ");

            stb.append(" ORDER BY ");
            stb.append("     VALUE(CLM.SPECIALDIV, '0'), CLM.SHOWORDER2, T1.CLASSCD, SUBM.SHOWORDER2, T1.SUBCLASSCD, T1.ANNUAL");
            return stb.toString();
        }
    }

    private static class PrintData {

        final String _schregno;
        final String _year;
        final String _semester;
        final String _date;
        final Map _paramap;
        final String _staffCd;
        final String _kanji;
        final String _certifNumber;
        final String _ctrlYear;
        Map<String, String> _gradeGradeCdMap;
        List<String> _d065Name1List;

        private String _certifSchoolDatSchoolName;
        private String _certifSchoolDatPrincipalName;
        private String _certifSchoolDatJobName;
        final String _stampSizeMm;
        final boolean _isPrintStamp;

        public PrintData(
                final String schregno,
                final String year,
                final String semester,
                final String date,
                final Map paramap,
                final String staffCd,
                final String kanji,
                final String certifNumber,
                final Param param) {
            _schregno = schregno;
            _year = year;
            _semester = semester;
            _date = null == date ? date : StringUtils.replace(date, "/", "-");
            _paramap = paramap;
            _staffCd = staffCd;
            _kanji = kanji;
            _certifNumber = certifNumber;
            _ctrlYear = (String) _paramap.get("CTRL_YEAR");
            _isPrintStamp = param._isOsakatoin || param._isSakae || param._isJogakkan || "1".equals(_paramap.get("PRINT_STAMP")) || "1".equals(param.property("KNJE080J_PRINT_STAMP"));
            if (param._isOutputDebug) {
                log.info(" isPrintStamp = " + _isPrintStamp);
            }
            _stampSizeMm = param.property("stampSizeMm");
        }

        public void load(final DB2UDB db2, final Param param) {

            if (_isPrintStamp) {
                param._certifSchoolstampImagePath = param.getImageFilePath("CERTIF_SCHOOLSTAMP_J.bmp");
                if (null == param._certifSchoolstampImagePath) {
                    param._certifSchoolstampImagePath = param.getImageFilePath("SCHOOLSTAMP_J.bmp");
                }
            }

            final String sql = " SELECT GRADE, GRADE_CD FROM SCHREG_REGD_GDAT WHERE YEAR IN (SELECT MIN(YEAR) FROM SCHREG_REGD_GDAT WHERE YEAR IN ('" + _year + "', '" + _ctrlYear + "')) ";
            _gradeGradeCdMap = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, sql), "GRADE", "GRADE_CD");

            _d065Name1List = KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, " SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D065' AND NAME1 IS NOT NULL "), "NAME1");
        }

        private void setCertifSchoolDat(final DB2UDB db2, final Param param) {
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, " SELECT JOB_NAME, PRINCIPAL_NAME, SCHOOL_NAME, REMARK3 FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _ctrlYear + "' AND CERTIF_KINDCD = '" + CERTIF_KINDCD + "' "));
            if (param._isOutputDebug) {
                log.info(" certifSchoolDat = " + row);
            }
            _certifSchoolDatJobName = KnjDbUtils.getString(row, "JOB_NAME");
            _certifSchoolDatPrincipalName = KnjDbUtils.getString(row, "PRINCIPAL_NAME");
            _certifSchoolDatSchoolName = KnjDbUtils.getString(row, "SCHOOL_NAME");
        }

        private String getSchoolKind(final DB2UDB db2) {
            String sql = "";
            sql += " SELECT T2.SCHOOL_KIND ";
            sql += " FROM (SELECT MIN(GRADE) AS GRADE FROM SCHREG_REGD_DAT WHERE YEAR = '" + _year + "' AND SCHREGNO = '" + _schregno + "') T1 ";
            sql += " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = '" + _year + "' AND T2.GRADE = T1.GRADE ";
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, sql, null));
        }
    }

    private static class Tuple<K, V> {
        final K _first;
        final V _second;
        private Tuple(final K first, final V second) {
            _first = first;
            _second = second;
        }
        public static <K, V> Tuple<K, V> of(final K first, final V second) {
            return new Tuple<K, V>(first, second);
        }
        public String toString() {
            return "(" + _first + ", " + _second + ")";
        }
    }

    private static class Util {

        public static int mmToDot(final String mm) {
            final BigDecimal dpi = new BigDecimal("400");
            final BigDecimal mmPerInch = new BigDecimal("25.4");
            final int dot = new BigDecimal(mm).multiply(dpi).divide(mmPerInch, 1, BigDecimal.ROUND_HALF_UP).intValue();
            return dot;
        }

        public static int toInt(final String s, final int def) {
            return NumberUtils.isNumber(s) ? ((int) Double.parseDouble(s)) : def;
        }

        public static double toDouble(final String s, final double def) {
            return NumberUtils.isNumber(s) ? Double.parseDouble(s) : def;
        }

        public static Calendar toCalendar(final String date) {
            final Calendar cal = Calendar.getInstance();
            cal.setTime(Date.valueOf(date));
            return cal;
        }

        public static String date0401(final String date) {
            final Calendar cal = toCalendar(date);
            final int nendo;
            if (cal.get(Calendar.MONTH) < Calendar.APRIL) {
                nendo = cal.get(Calendar.YEAR) - 1;
            } else {
                nendo = cal.get(Calendar.YEAR);
            }
            return String.valueOf(nendo) + "-04-01";
        }

        public static Integer enterYear(final String date) {
            if (null != date) {
                final Calendar cal = toCalendar(date);
                if (cal.get(Calendar.MONTH) < Calendar.APRIL) {
                    return new Integer(cal.get(Calendar.YEAR) - 1);
                } else {
                    return new Integer(cal.get(Calendar.YEAR));
                }
            }
            return null;
        }

        public static String h_format_Seireki(final String date) {
            String rtn = null;
            try {
                final SimpleDateFormat sdfy = new SimpleDateFormat("yyyy年M月d日");
                rtn = sdfy.format(Date.valueOf(date.replace('/', '-')));
            } catch (Exception e) {
                log.error("Exception! date = " + date, e);
                rtn = "";
            }
            return rtn;
        }

        public static <A, B, C> Map<B, C> getMappedHashMap(final Map<A, Map<B, C>> map, final A key1) {
            if (!map.containsKey(key1)) {
                map.put(key1, new HashMap<B, C>());
            }
            return map.get(key1);
        }

        public static <A, B, C> Map<B, C> getMappedMap(final Map<A, Map<B, C>> map, final A key1) {
            if (!map.containsKey(key1)) {
                map.put(key1, new TreeMap<B, C>());
            }
            return map.get(key1);
        }

        public static <A, B> List<B> getMappedList(final Map<A, List<B>> map, final A key1) {
            if (!map.containsKey(key1)) {
                map.put(key1, new ArrayList<B>());
            }
            return map.get(key1);
        }

        public static String mkString(final TreeMap<String, String> map, final String comma) {
            final List<String> list = new ArrayList<String>();
            for (final Map.Entry<String, String> e : map.entrySet()) {
                if (StringUtils.isEmpty(e.getKey()) || StringUtils.isEmpty(e.getValue())) {
                    continue;
                }
                list.add(e.getKey() + "=" + e.getValue());
            }
            return mkString(list, comma, "");
        }

        public static String mkString(final List<String> list, final String comma, final String last) {
            final StringBuffer stb = new StringBuffer();
            String comma0 = "";
            String nl = "";
            for (final String s : list) {
                if (null == s || s.length() == 0) {
                    continue;
                }
                stb.append(comma0).append(s);
                comma0 = comma;
                nl = last;
            }
            return stb.append(nl).toString();
        }

        public static String add(final String s1, final String s2) {
            if (!NumberUtils.isDigits(s1) && !NumberUtils.isDigits(s2)) {
                return null;
            }
            final int si1 = Integer.parseInt(NumberUtils.isDigits(s1) ? s1 : "0");
            final int si2 = Integer.parseInt(NumberUtils.isDigits(s2) ? s2 : "0");
            return String.valueOf(si1 + si2);
        }

        public static <T>  List<List<T>> splitByCount(final List<T> list, final int splitCount) {
            final List<List<T>> rtn = new ArrayList<List<T>>();
            List<T> current = null;
            int count = 0;
            for (final T item : list) {
                if (splitCount <= count || null == current) {
                    count = 0;
                    current = new ArrayList();
                    rtn.add(current);
                }
                current.add(item);
                count += 1;
            }
            return rtn;
        }

        private static <T> List<T> reverse(final Collection<T> col) {
            final LinkedList<T> rtn = new LinkedList<T>();
            for (final ListIterator<T> it = new ArrayList(col).listIterator(col.size()); it.hasPrevious();) {
                rtn.add(it.previous());
            }
            return rtn;
        }

        public static <T> void debugRecordList(final String debugText, final List<T> recordList) {
            for (final T t : recordList) {
                log.info(" " + debugText + " = " + t);
            }
        }
    }

    private static class Param {
        private final Map _initParamap;
        private final String _useCurriculumcd;
        private boolean _isKindai;
        private String _documentroot;
        final Map<String, String> _dbPrgInfoProperties;
        public Properties _prgInfoPropertiesFilePrperties;
        private String _certifSchoolstampImagePath;

        final boolean _hasSCHOOL_MST_SCHOOL_KIND;
        final boolean _isChiyodaKudan;
        final boolean _isSeijyo;
        final boolean _isOsakatoin;
        final boolean _isSakae;
        final boolean _isMatsudo;
        final boolean _isJogakkan;
        final String _z010Name1;
        final boolean _isSeireki; // 西暦表示するならtrue
        final String _certifPrintRealName;
        final String _imagepath;
        final Map<String, String> _d001Abbv1Map;
        final boolean _isOutputDebug;
        final boolean _isOutputDebugAll;
        final Map<String, Map<String, SvfField>> _formInfo = new TreeMap<String, Map<String, SvfField>>();
        final Map<String, File> _deleteFiles = new HashMap<String, File>();
        final Map<String, String> _gradeGradecdMap = new HashMap<String, String>();

        final Map<String, PreparedStatement> _psMap = new HashMap<String, PreparedStatement>();

        Param(final DB2UDB db2, final Map initParamap) {
            _initParamap = initParamap;
            _useCurriculumcd = KnjDbUtils.setTableColumnCheck(db2, "SUBCLASS_MST", "CURRICULUM_CD") ? "1" : null;
            _dbPrgInfoProperties = getDbPrginfoProperties(db2);

            _z010Name1 = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' "));
            log.info(" z010Name1 = " + _z010Name1);
            _isChiyodaKudan = "chiyoda".equals(_z010Name1);
            _isSeijyo = "seijyo".equals(_z010Name1);
            _isOsakatoin = "osakatoin".equals(_z010Name1);
            _isSakae = "sakae".equals(_z010Name1);
            _isMatsudo = "matsudo".equals(_z010Name1);
            _isJogakkan = "jogakkan".equals(_z010Name1);
            _isKindai = "KINDAI".equals(_z010Name1) || "KINJUNIOR".equals(_z010Name1);
            _isSeireki = KNJ_EditDate.isSeireki(db2);
            _hasSCHOOL_MST_SCHOOL_KIND = KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND");
            _certifPrintRealName = (String) initParamap.get("certifPrintRealName");
            _imagepath = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT IMAGEPATH FROM CONTROL_MST WHERE CTRL_NO = '01' "));
            _d001Abbv1Map = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, " SELECT NAMECD2, ABBV1 FROM NAME_MST WHERE NAMECD1 = 'D001' AND ABBV1 IS NOT NULL "), "NAMECD2", "ABBV1");

            final String[] outputDebug = StringUtils.split(_dbPrgInfoProperties.get("outputDebug"));
            _isOutputDebugAll = ArrayUtils.contains(outputDebug, "all");
            _isOutputDebug = _isOutputDebugAll || ArrayUtils.contains(outputDebug, "1");
        }

        public void setDocumentroot(String documentroot) {
            _documentroot = documentroot;
            _prgInfoPropertiesFilePrperties = loadPropertyFile("prgInfo.properties");
        }

        private static Map<String, String> getDbPrginfoProperties(final DB2UDB db2) {
            return KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, " SELECT NAME, VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJE080' "), "NAME", "VALUE");
        }

        public String property(final String name) {
            String val = null;
            if (null != _dbPrgInfoProperties) {
                if (_dbPrgInfoProperties.containsKey(name)) {
                    val = _dbPrgInfoProperties.get(name);
                    if (_isOutputDebug) {
                        log.info("property in db: " + name + " = " + val);
                    }
                    return val;
                }
            }
            if (_initParamap.containsKey(name)) {
                return (String) _initParamap.get(name);
            }
            if (null != _prgInfoPropertiesFilePrperties) {
                if (_prgInfoPropertiesFilePrperties.containsKey(name)) {
                    val = _prgInfoPropertiesFilePrperties.getProperty(name);
                    if (_isOutputDebug) {
                        log.info("property in file: " + name + " = " + val);
                    }
                } else {
                    if (_isOutputDebug) {
                        log.warn("property not exists in file: " + name);
                    }
                }
            }
            return val;
        }

        public Properties loadPropertyFile(final String filename) {
            File file = null;
            if (null != _documentroot) {
                file = new File(new File(_documentroot).getParentFile().getAbsolutePath() + "/config/" + filename);
                if (_isOutputDebug) {
                    log.info("check prop : " + file.getAbsolutePath() + ", exists? " + file.exists());
                }
                if (!file.exists()) {
                    file = null;
                }
            }
            if (null == file) {
                file = new File(_documentroot + "/" + filename);
            }
            if (!file.exists()) {
                if (_isOutputDebug) {
                    log.error("file not exists: " + file.getAbsolutePath());
                }
                return null;
            }
            if (_isOutputDebug) {
                log.error("file : " + file.getAbsolutePath() + ", " + file.length());
            }
            final Properties props = new Properties();
            FileReader r = null;
            try {
                r = new FileReader(file);
                props.load(r);
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                if (null != r) {
                    try {
                        r.close();
                    } catch (Exception _ignored) {
                    }
                }
            }
            return props;
        }

        public void deleteFile() {
            for (final Iterator<File> it = _deleteFiles.values().iterator(); it.hasNext();) {
                final File f = it.next();
                try {
                    log.info(" file " + f.getAbsolutePath() + " delete? " + f.delete());
                } catch (Exception e) {
                    log.error("exception!", e);
                }
                it.remove();
            }
        }

        public String getImageFilePath(final String filename) {
            String path = "";
            if (null != _documentroot) {
                path += _documentroot;
                if (!path.endsWith("/")) {
                    path += "/";
                }
            }
            if (null != _imagepath) {
                path += _imagepath;
                if (!path.endsWith("/")) {
                    path += "/";
                }
            }
            path += filename;
            final File file = new File(path);
            log.info(" file " + file.getPath() +" exists? = " + file.exists());
            if (!file.exists()) {
                return null;
            }
            return file.getPath();
        }
    }
}
