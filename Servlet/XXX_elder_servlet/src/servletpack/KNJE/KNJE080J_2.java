// kanji=漢字
/*
 * $Id: b618c6de2e115573ec8970a812048760562d5cce $
 *
 * 作成日:
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJE;

import static servletpack.KNJZ.detail.KNJ_EditEdit.getMS932ByteLength;

import java.io.File;
import java.sql.PreparedStatement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
import servletpack.KNJZ.detail.KNJ_PersonalinfoSql;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.SvfField;
import servletpack.KNJZ.detail.SvfForm;

/*
 *  学校教育システム 賢者 [進路情報管理]  中学成績証明書（英）
 */

public class KNJE080J_2 {

    private static final Log log = LogFactory.getLog(KNJE080J_2.class);

    private static String CERTIF_KINDCD = "034";

    private static String SCHOOL_KIND_J = "J";

    private Vrw32alp _svf;
    private DB2UDB _db2;
    private final Param _param;
    public boolean nonedata;

    public KNJE080J_2(final DB2UDB db2, final Vrw32alp svf, final Map initParamap) {
        _db2 = db2;
        _svf = svf;
        nonedata = false;
        log.fatal("$Revision: 74268 $ $Date: 2020-05-14 10:33:59 +0900 (木, 14 5 2020) $"); // CVSキーワードの取り扱いに注意
        _param = new Param(_db2, initParamap);
    }

    public void printSvf(final String year, final String semester, final String date, final String schregno, final Map paramap, final String staffCd, final int paper, final String kanji, final String certifNumber) {

        final PrintData printData = new PrintData(_db2, year, semester, date, schregno, paramap, staffCd, paper, kanji, certifNumber);
        log.info(" schregno = " + printData._schregno + " (isGrd = " + printData._useGrdTable + ")");
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
     *  PrepareStatement close <- KNJD070_1
     */
    public void pre_stat_f() {
        if (null != _param) {
            _param.closePs();
            _param.closeFile();
        }
    }

    private static class Util {

        private static String join(final Collection<String> list, final String comma) {
            final StringBuffer stb = new StringBuffer();
            String comma0 = "";
            for (final String s : list) {
                if (StringUtils.isEmpty(s)) {
                    continue;
                }
                stb.append(comma0).append(s);
                comma0 = comma;
            }
            return stb.toString();
        }

        private static String mkString(final TreeMap<String, String> map, final String comma) {
            final List<String> list = new ArrayList<String>();
            for (final Map.Entry<String, String> e : map.entrySet()) {
                if (StringUtils.isEmpty(e.getKey()) || StringUtils.isEmpty(e.getValue())) {
                    continue;
                }
                list.add(e.getKey() + "=" + e.getValue());
            }
            return mkString(list, comma);
        }

        private static String mkString(final Collection<String> list, final String comma) {
            return join(list, comma);
        }

        private static String engMonthName(final String date) {
            return h_format_US(date, "MMMM");
        }

        private static Calendar getCalendarOfDate(final String date) {
            try {
                final Calendar cal = Calendar.getInstance();
                cal.setTime(java.sql.Date.valueOf(StringUtils.replace(date, "/", "-")));
                return cal;
            } catch (Exception e) {
                log.error("exception! date = " + date, e);
            }
            return null;
        }

        private static <A, B> List<B> getMappedList(final Map<A, List<B>> map, final A key) {
            if (!map.containsKey(key)) {
                map.put(key, new ArrayList<B>());
            }
            return map.get(key);
        }

        private static <A, B, C> Map<B, C> getMappedMap(final Map<A, Map<B, C>> map, final A key1) {
            if (!map.containsKey(key1)) {
                map.put(key1, new TreeMap<B, C>());
            }
            return map.get(key1);
        }

        private static String h_format_US(final String strx, final String format) {
            String rtn = "";
            final Date date = Util.toDate(strx);
            if (null != date) {
                rtn = new SimpleDateFormat(format, new Locale("en", "US")).format(date);
            }
            return rtn;
        }

        private static Date toDate(final String strx) {
            SimpleDateFormat sdf = new SimpleDateFormat();
            Date date = new Date();
            try {
                sdf.applyPattern("yyyy-MM-dd");
                date = sdf.parse(strx);
            } catch (Exception e) {
                try {
                    sdf.applyPattern("yyyy/MM/dd");
                    date = sdf.parse(strx);
                } catch (Exception e2) {
                    date = null;
                }
            }
            return date;
        }

        private static String yearOfDate(String dateString) {
            Date date = Util.toDate(dateString);
            if (null != date) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                return String.valueOf(cal.get(Calendar.YEAR));
            }
            return null;
        }

        /**
         * 単語途中でなるべく区切らないように分割
         *
         * <pre>
         * splitBySizeWithSpace("abcde fghij klmnop", 13) = {"abcde fghij ", "klmnop"}
         * splitBySizeWithSpace("abcde fghij klmnop", 10) = {"abcde", "fghij", "klmnop"}
         * splitBySizeWithSpace("abcde fghij klmnop", 7) =  {"abcde", "fghij", "klmnop"}
         * </pre>
         *
         * @param s 文字列
         * @param keta 桁
         * @return 文字列を分割した配列
         */
        private static String[] splitBySizeWithSpace(final String s, final int[] ketas) {
            if (null == s) {
                return new String[] {};
            } else if (s.length() <= ketas[0]) {
                return new String[] {s};
            }
            final List<String> split = new ArrayList();
            int idx = ketas[0];
            int beforeidx = 0;
            while (true) {
                int idxSpace = -1;
                boolean isCheckSpace = false;
                if (s.charAt(idx) != ' ') { // 単語途中で区切りがスペースでなければ、前方探索
                    isCheckSpace = true;
                    for (int i = idx - 1; i > beforeidx; i--) {
                        if (s.charAt(i) == ' ') {
                            idxSpace = i;
                            break;
                        }
                    }
                    //log.info("  idxSpace = " + idxSpace + " at " + s + " ( " + idx + " = " + s.charAt(idx) + ")");
                }
                if (idxSpace != -1) {
                    split.add(s.substring(beforeidx, idxSpace));
                    beforeidx = idxSpace + 1;
                    idx = beforeidx + ketas[split.size() < ketas.length ? split.size() : ketas.length - 1];
                } else {
                    // スペースがなければ指定区切りまでを追加
                    if (isCheckSpace) {
                        //log.info(" no space-char in [" + beforeidx + ", " + idx + "] string [" + s.substring(beforeidx, idx) + "]  full [" + s + "]");
                    }
                    split.add(s.substring(beforeidx, idx));
                    beforeidx = idx;
                    idx = beforeidx + ketas[split.size() < ketas.length ? split.size() : ketas.length - 1];
                }
                if (s.length() <= idx) {
                    split.add(s.substring(beforeidx));
                    break;
                }
            }
            final String[] array = new String[split.size()];
            for (int i = 0; i < array.length; i++) {
                array[i] = split.get(i);
            }
            return array;
        }
    }

    private static class Form {

        final Vrw32alp _svf;
        final Param _param;
        private PrintData _printData;
        private String _formname;
        private String dateFormat_d_c_MMM_c_yyyy = "d,MMM,yyyy";
        private String dateFormat_MMMM_s_d_c_s_yyyy = "MMMM d, yyyy";
        private boolean hasData = false;

        Form(final Vrw32alp svf, final Param param) {
            _svf = svf;
            _param = param;
        }
        public void print(final DB2UDB db2, final PrintData printData) {
            _printData = printData;
            setForm();
            printSchool(db2);  //学校名、校長名のセット
            printPersonal(db2);  //氏名、住所等出力
            printAttend(db2);  //出欠の出力
            printStudyrec(db2);  //学習の記録出力
            _svf.VrEndPage();
        }

        private void setForm() {
            final String formname;
            int formFlg = 1;
            if (_param._isKindai) {
                formname = "KNJE080_4KIN.frm";
                _printData._usePrincipalSignatureImage = true;
            } else if (_param._isChiyodaKudan) {
                formname = "KNJE080_2JKUDAN.frm";
                formFlg = 4;
            } else if (_param._isKaijyo) {
                formname = "KNJE080_2JKAIJYO.frm";
                formFlg = 4;
            } else if (_param._isSakae) {
                formname = "KNJE080_2JSAKAE.frm";
                _printData._usePrincipalSignatureImage = true;
                _printData._isPrintStamp = true;
            } else if (_param._isMeikei) {
                formname = "KNJE080_2JMEIKEI.frm";
                _printData._usePrincipalSignatureImage = true;
            } else if (_param._isMatsudo) {
                formname = "KNJE080_2JMATSUDO.frm";
                _printData._isPrintStamp = "1".equals(_printData._paramap.get("PRINT_STAMP"));
            } else {
                formname = "KNJE080_2J.frm";
                _printData._isPrintStamp = true;
            }

            final String useFormname = configForm(formname);
            log.info(" form = " + useFormname);
            _svf.VrSetForm(useFormname, formFlg);  //成績証明書(和)
            _formname = useFormname;
            if (null != useFormname && null == _param._formInfo.get(useFormname)) {
                _param._formInfo.put(formname, SvfField.getSvfFormFieldInfoMapGroupByName(_svf));
            }
        }

        private String configForm(final String formname0) {
            final TreeMap<String, String> modifyFlgMap = getModifyFlgMap();
            String modifyFlg = Util.mkString(modifyFlgMap, "|");
            if (!StringUtils.isBlank(modifyFlg)) {
                modifyFlg = formname0 + ":" + modifyFlg;
            }
            if (_param._isOutputDebug) {
                log.info(" config form key = " + modifyFlg);
            }
            if (modifyFlgMap.isEmpty()) {
                return formname0;
            }

            if (!_param._createdFormFiles.containsKey(modifyFlg)) {
                final String path = _svf.getPath(formname0);
                final File formFile = new File(path);
                final SvfForm svfForm = new SvfForm(formFile);

                if (svfForm.readFile()) {
                    modifyForm(modifyFlgMap, svfForm);
                    try {
                        final File newFormFile = svfForm.writeTempFile();
                        _param._createdFormFiles.put(modifyFlg, newFormFile.exists() ? newFormFile : null);
                    } catch (Exception e) {
                        log.error("exception!", e);
                    }
                }
            }
            final File newFormFile = _param._createdFormFiles.get(modifyFlg);
            String newformname = formname0;
            if (null != newFormFile) {
                newformname = newFormFile.getName();
            }
            return newformname;
        }

        final String FLG_REITAKU = "FLG_REITAKU";
        private TreeMap<String, String> getModifyFlgMap() {
            final TreeMap<String, String> modifyFlgMap = new TreeMap<String, String>();
            if (_param._isReitaku) {
                modifyFlgMap.put(FLG_REITAKU, "1");
            }
            return modifyFlgMap;
        }

        private void modifyForm(final Map<String, String> modifyFlgMap, final SvfForm svfForm) {
            if (modifyFlgMap.containsKey(FLG_REITAKU)) {
                for (final SvfForm.KoteiMoji moji : svfForm.getElementList(SvfForm.KoteiMoji.class)) {
                    if (moji._moji.contains("Address of School : ") || moji._moji.contains("Course : ")) {
                        svfForm.removeKoteiMoji(moji);
                    }
                    if (moji._moji.contains("Name of Student : ") || moji._moji.contains("Sex : ")) {
                        svfForm.move(moji, moji.addY(-50));
                    }
                }
                for (final SvfForm.Field field : svfForm.getElementList(SvfForm.Field.class)) {
                    if (Arrays.asList("SCHOOL_ADDRESS1", "SCHOOL_ADDRESS1", "SCHOOL_ADDRESS1", "MAJOR").contains(field._fieldname)) {
                        svfForm.removeField(field);
                    }
                    if (Arrays.asList("NAME", "SEX").contains(field._fieldname)) {
                        svfForm.removeField(field);
                        svfForm.addField(field.addY(-50));
                    }
                    if (Arrays.asList("GRADE1", "GRADE2", "GRADE3").contains(field._fieldname)) {
                        svfForm.removeField(field);
                        svfForm.addField(field.setHenshuShiki(""));
                    }
                }
                final SvfForm.Field STAFFNAME = svfForm.getField("STAFFNAME");
                final SvfForm.Field SCHOOLNAME2 = svfForm.getField("SCHOOLNAME2");
                svfForm.addKoteiMoji(new SvfForm.KoteiMoji("PRINCIPAL", SCHOOLNAME2.getPoint().addY(-3), STAFFNAME._charPoint10));
                svfForm.removeField(SCHOOLNAME2);
                svfForm.addField(SCHOOLNAME2.addY(90).setHenshuShiki(""));
            }
        }

        private void vrsOut(final String fieldname, final String data) {
            if (_param._isOutputDebugVrsout) {
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

        private void vrImageOut(final String fieldname, final String filepath) {
            if (null != filepath) {
                vrsOut(fieldname, filepath);
            }
        }

        private void vrsOutn(final String fieldname, final int gyo, final String data) {
            if (_param._isOutputDebugVrsout) {
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

        private SvfField getField(final String fieldname) {
            if (null == _formname) {
                log.warn("form not set!");
            }
            SvfField field = (SvfField) Util.getMappedMap(_param._formInfo, _formname).get(fieldname);
            if (_param._isOutputDebugVrsout) {
                log.info(" field " + fieldname + " = " + field);
            }
            return field;
        }

        private String getFieldForDataKeta(final String[] fields, final String data) {
            final int keta = getMS932ByteLength(data);
            String existMax = null; // どのフィールドにも収まらない場合最大桁数のフィールドを使用する
            for (int i = 0; i < fields.length; i++) {
                final SvfField field = getField(fields[i]);
                if (null == field) {
                    continue;
                }
                existMax = fields[i];
                if (keta <= field._fieldLength) {
                    return fields[i];
                }
            }
            if (_param._isOutputDebug) {
                log.info(" use existMax \"" + existMax + "\" / fields = " + ArrayUtils.toString(fields) + ", data = \"" + data + "\"");
            }
            return existMax;
        }

        private void vrsOutFieldSelect(final String[] fields, final String data) {
            vrsOut(getFieldForDataKeta(fields, data), data);
        }

        private String dateFormat() {
            if (_param._isKaijyo) {
                return dateFormat_MMMM_s_d_c_s_yyyy;
            }
            return dateFormat_d_c_MMM_c_yyyy;
        }

        /**
         *  学校情報
         */
        private void printSchool(final DB2UDB db2) {
            //  学校データ
            final String psKeySchoolMst = "PS_SCHOOL_MST";
            if (null == _param.getPs(psKeySchoolMst)) {
                final String sql = getSchoolMstSql();
                if (_param._isOutputDebugQuery) {
                    log.info(" school_mst sql = " + sql);
                }
                _param.setPs(db2, psKeySchoolMst, sql);
            }

            // 証明書学校データ
            final String psKeySchool = "PS_SCHOOLINFO";
            if (null == _param.getPs(psKeySchool)) {
                final String sql = getCertifSchoolDatSql();
                if (_param._isOutputDebugQuery) {
                    log.info(" schoolinfo sql = " + sql);
                }
                _param.setPs(db2, psKeySchool, sql);
            }

            // 職員
            final String psKeyStaff = "PS_STAFFINFO";
            if (null == _param.getPs(psKeyStaff)) {
                final String sql = getStaffInfoSql();
                if (_param._isOutputDebugQuery) {
                    log.info(" staffinfo sql = " + sql);
                }
                _param.setPs(db2, psKeyStaff, sql);
            }

            final boolean notOutputPrincipalName = "2".equals(_printData._paramap.get("OUTPUT_PRINCIPAL"));

            final String dateYear = _printData._date != null ? KNJG010_1.b_year(_printData._date) : _printData._ctrlYear;

            final Object[] argSchoolMst = new Object[] { dateYear};
            final Object[] argSchool = new Object[] { dateYear};
            final Object[] argStaff = new Object[] { _printData._ctrlYear};

            if (_param._isOutputDebugQuery) {
                log.info(" schoolinfo arg = " + ArrayUtils.toString(argSchool) + " / " + ArrayUtils.toString(argStaff));
            }

            final Map schoolMstInfo = KnjDbUtils.firstRow(KnjDbUtils.query(db2, _param.getPs(psKeySchoolMst), argSchoolMst));
            final String schoolMstSchoolnameEng = KnjDbUtils.getString(schoolMstInfo, "SCHOOLNAME_ENG");
            final boolean printAddr2 = !_param._isMatsudo;
            String addr1 = null;
            String addr2 = null;
            addr1 = KnjDbUtils.getString(schoolMstInfo, "SCHOOLADDR1_ENG");
            addr2 = !printAddr2 ? "" : StringUtils.defaultString(KnjDbUtils.getString(schoolMstInfo, "SCHOOLADDR2_ENG"));

            String staffJobname = null;
            String certifSchoolDatRemark3 = null;
            if (!_param._isKindai) {
                final Map schoolInfo = KnjDbUtils.firstRow(KnjDbUtils.query(db2, _param.getPs(psKeySchool), argSchool));
                staffJobname = KnjDbUtils.getString(schoolInfo, "JOB_NAME");
                certifSchoolDatRemark3 = KnjDbUtils.getString(schoolInfo, "REMARK3");
                final String remark5 = KnjDbUtils.getString(schoolInfo, "REMARK5");
                final String remark6 = KnjDbUtils.getString(schoolInfo, "REMARK6");
                // 備考5か備考6のデータがあれば住所1、住所2とする
                if (!StringUtils.isEmpty(remark5) || !StringUtils.isEmpty(remark6)) {
                    addr1 = remark5;
                    addr2 = StringUtils.defaultString(remark6);
                }
            }

            String staffMstPricipalNameEng = null;
            {
                final Map staffInfo = KnjDbUtils.firstRow(KnjDbUtils.query(db2, _param.getPs(psKeyStaff), argStaff));
                staffMstPricipalNameEng = KnjDbUtils.getString(staffInfo, "PRINCIPAL_NAME_ENG");
                if (_param._isKindai) {
                    staffJobname = KnjDbUtils.getString(staffInfo, "PRINCIPAL_JOBNAME");
                }
            }

            if (!schoolMstInfo.isEmpty()) {
                // 学校名
                final String schoolName = StringUtils.defaultString(_printData._certifSchoolDatSchoolName, schoolMstSchoolnameEng);
                if (schoolName != null) {
                    vrsOut("SCHOOLNAME1",  schoolName);
                    vrsOut("SCHOOLNAME2",  schoolName);
                }

                // 学校住所
                if (addr1 != null) {
                    String space = "";
                    if (!StringUtils.isBlank(addr1) && !Character.isWhitespace(addr1.charAt(addr1.length() - 1)) && !StringUtils.isBlank(addr2) && !Character.isWhitespace(addr2.charAt(0))) {
                        space = " ";
                    }
                    addr1 = addr1 + space + addr2;

                    vrsOutFieldSelect(new String[] {"SCHOOL_ADDRESS1", "SCHOOL_ADDRESS2", "SCHOOL_ADDRESS3", "SCHOOL_ADDRESS4"}, addr1);  //住所
                }

                // 記載日
                if (_param._isMatsudo) {
                    vrsOutDateMonthYear("DATE_MONTH", "DATE_DAY", "DATE_YEAR", _printData._date);
                } else if (_param._isChiyodaKudan) {
                    if (null != _printData._date) {
                        vrsOut("DATE", KNJ_EditDate.h_format_UK(_printData._date, "MMMM"));
                    }
                } else {
                    if (null != _printData._date) {
                        final String formatDate = Util.h_format_US(_printData._date, dateFormat());
                        vrsOut("DATE", formatDate);
                        if (_param._isKaijyo) {
                            _svf.VrAttribute("DATE", "UnderLine=(0,1,1),Keta=" + String.valueOf(KNJ_EditEdit.getMS932ByteLength(formatDate)));
                        }
                    } else {
                        vrsOut("DATE", "　　,");
                    }
                }

                // 校長名
                if (notOutputPrincipalName) {
                } else {
                    vrsOut("JOBNAME", staffJobname);
                    vrsOut("STAFFNAME", StringUtils.defaultString(_printData._certifSchoolDatPrincipalName, staffMstPricipalNameEng));
                }
                if (_param._isKaijyo) {
                    final String[] splitted = Util.splitBySizeWithSpace(_printData._certifSchoolDatRemark4, new int[] {70, 78});
                    for (int i = 0; i < splitted.length; i++) {
                        vrsOut("REMARK4_" + String.valueOf(i + 1), splitted[i]);
                    }
                }
                if (_param._isSakae) {
                    vrsOut("CORP_NAME", certifSchoolDatRemark3);
                }
                if (_param._isMatsudo) {
                    final String[] splitSchoolname = Util.splitBySizeWithSpace(schoolName, new int[] {20, 100});
                    if (splitSchoolname.length > 0) {
                        vrsOut("SCHOOLNAME2_1", splitSchoolname[0]);
                    }
                    if (splitSchoolname.length > 1) {
                        vrsOut("SCHOOLNAME2_2", splitSchoolname[1]);
                    }
                }
            }

            if (_printData._usePrincipalSignatureImage) {
                vrImageOut("SIGNATURE", _param.getImagePath(_printData._documentroot, "PRINCIPAL_SIGNATURE_J.jpg"));
            }
            if (_printData._isPrintStamp) {
                String stampPath = _param.getImagePath(_printData._documentroot, "CERTIF_SCHOOLSTAMP_J.bmp");
                if (null == stampPath) {
                    stampPath = _param.getImagePath(_printData._documentroot, "SCHOOLSTAMP_J.bmp");
                }
                vrImageOut("STAMP", stampPath);
            }
        }

        private String getSchoolMstSql() {

            final StringBuffer sql = new StringBuffer();

            final String q = "?";
            sql.append(" SELECT ");
            sql.append("     T1.YEAR ");
            sql.append("     ,T1.SCHOOLNAME_ENG");
            sql.append("     ,T1.SCHOOLADDR1_ENG");
            sql.append("     ,T1.SCHOOLADDR2_ENG");
            sql.append(" FROM ");
            sql.append("     SCHOOL_MST T1 ");
            sql.append(" WHERE ");
            sql.append("     T1.YEAR = " + q + " ");
            if (_param._hasSCHOOL_MST_SCHOOL_KIND) {
                sql.append(" AND T1.SCHOOL_KIND = '" + SCHOOL_KIND_J + "' ");
            }

            return sql.toString();
        }

        private String getCertifSchoolDatSql() {

            final StringBuffer sql = new StringBuffer();

            final String q = "?";
            sql.append(" SELECT ");
            sql.append("      T1.YEAR ");
            sql.append("     ,T1.JOB_NAME ");
            sql.append("     ,T1.SYOSYO_NAME ");
            sql.append("     ,T1.SYOSYO_NAME2 ");
            sql.append("     ,T1.CERTIF_NO ");
            sql.append("     ,T1.REMARK1");
            sql.append("     ,T1.REMARK2");
            sql.append("     ,T1.REMARK3");
            sql.append("     ,T1.REMARK4");
            sql.append("     ,T1.REMARK5");
            sql.append("     ,T1.REMARK6");
            sql.append("     ,T1.REMARK7");
            sql.append("     ,T1.REMARK8");
            sql.append("     ,T1.REMARK9");
            sql.append("     ,T1.REMARK10 ");
            sql.append(" FROM ");
            sql.append("     CERTIF_SCHOOL_DAT T1 ");
            sql.append(" WHERE ");
            sql.append("     T1.YEAR = " + q + " ");
            sql.append(" AND T1.CERTIF_KINDCD = '" + CERTIF_KINDCD + "' ");

            return sql.toString();
        }

        private String getStaffInfoSql() {

            final StringBuffer sql = new StringBuffer();

            final String q = "?";
            sql.append("     SELECT ");
            sql.append("           W1.STAFFCD ");
            sql.append("         , W2.STAFFNAME ");
            sql.append("         , W2.STAFFNAME_ENG AS PRINCIPAL_NAME_ENG ");
            sql.append("         , W3.JOBNAME AS PRINCIPAL_JOBNAME ");
            sql.append("     FROM ");
            sql.append("         STAFF_YDAT W1 ");
            sql.append("         INNER JOIN STAFF_MST W2 ON W2.STAFFCD = W1.STAFFCD ");
            sql.append("         LEFT JOIN JOB_MST W3 ON W3.JOBCD = W2.JOBCD ");
            sql.append("     WHERE ");
            sql.append("         W1.YEAR = " + q + " ");
            if (_param._isKindai) {
                sql.append("         AND (W2.JOBCD = '0001' OR W2.JOBCD = '0005') ");
            } else {
                sql.append("         AND W2.JOBCD = '0001' ");
            }
            return sql.toString();
        }

        /**
         *  個人情報
         */
        private void printPersonal(final DB2UDB db2) {
            // 証明書番号
            if (_printData._certifNumber != null) {
                vrsOut("CERTIFNO",  _printData._certifNumber);
            } else {
                vrsOut("CERTIFNO",  "     ");
            }
            final Map personalInfo = _printData._personalInfo;

            if (!personalInfo.isEmpty()) {

                // 氏名
                final String nameEng = KnjDbUtils.getString(personalInfo, "NAME_ENG");
                final String setName;
                if (_param._isMeikei) {
                    setName = StringUtils.replaceOnce(nameEng, " ", ",");
                } else {
                    setName = nameEng;
                }
                vrsOut("NAME", setName);

                // 性別
                if (_param._isChiyodaKudan) {
                    final String sexEng2 = KnjDbUtils.getString(personalInfo, "SEX_ENG2");
                    if ("Female".equalsIgnoreCase(sexEng2)) {
                        vrsOut("FEMALE", "○");
                    } else if ("Male".equalsIgnoreCase(sexEng2)) {
                        vrsOut("MALE", "○");
                    }
                } else if (_param._isKaijyo) {
                    vrsOut("SEX", KnjDbUtils.getString(personalInfo, "SEX_ENG2"));
                } else {
                    vrsOut("SEX", StringUtils.defaultString(KnjDbUtils.getString(personalInfo, "SEX_ENG2"), KnjDbUtils.getString(personalInfo, "SEX_ENG")));
                }

                // 入学日付
                final String entDate = KnjDbUtils.getString(personalInfo, "ENT_DATE");
                if (entDate != null) {
                    if (_param._isMatsudo) {
                        vrsOutDateMonthYear("ENTDATE_MONTH", "ENTDATE_DAY", "ENTDATE_YEAR", entDate);
                    } else {
                        vrsOut("DATE_E", Util.h_format_US(entDate, dateFormat()));
                    }
                }

                // 卒業日付
                String printGrdDate = null;
                final String grdDate = KnjDbUtils.getString(personalInfo, "GRD_DATE");
                final String graduDate = KnjDbUtils.getString(personalInfo, "GRADU_DATE");
                final boolean isPrintGraduDate = !_param._isMatsudo;
                if (grdDate != null) {
                    printGrdDate = grdDate;
                } else if (graduDate != null && isPrintGraduDate) {
                    if ("H".equals(_printData._schoolkind)) {
                        // 現在高校生の場合、表示しない
                    } else {
                        printGrdDate = graduDate;
                    }
                }
                if (null != printGrdDate) {
                    if (_param._isMatsudo) {
                        vrsOutDateMonthYear("GRDDATE_MONTH", "GRDDATE_DAY", "GRDDATE_YEAR", printGrdDate);
                    } else {
                        vrsOut("DATE_C", Util.h_format_US(printGrdDate, dateFormat()));
                    }
                }

                if (_param._isKaijyo) {
                    vrsOut("CURRENT_YEAR", Util.yearOfDate(_printData._date));
                }

                // 生年月日
                final String birthday = KnjDbUtils.getString(personalInfo, "BIRTHDAY");
                if (birthday != null) {
                    if (_param._isChiyodaKudan) {
                        try {
                            final Calendar cal = Util.getCalendarOfDate(birthday);
                            final int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
                            final String dayOfMonthStr = (dayOfMonth < 10 ? " " : "") + String.valueOf(dayOfMonth);
                            final int month = cal.get(Calendar.MONTH) + 1;
                            final String monthStr = (month < 10 ? " " : "") + String.valueOf(month);
                            vrsOut("BIRTHDAY", dayOfMonthStr + "  ／  " + monthStr + "  ／  " + String.valueOf(cal.get(Calendar.YEAR)));

                        } catch (Exception e) {
                            log.error("exception!", e);
                        }
                    } else if (_param._isMatsudo) {
                        vrsOutDateMonthYear("BIRTHDAY_MONTH", "BIRTHDAY_DAY", "BIRTHDAY_YEAR", birthday);
                    } else {
                        vrsOut("BIRTHDAY", Util.h_format_US(birthday, dateFormat()));
                    }
                }

                // 学科
                vrsOut("MAJOR", KnjDbUtils.getString(personalInfo, "MAJORENG"));

                if (_param._isKaijyo) {
                    // 生徒住所
                    final String addr1 = KnjDbUtils.getString(personalInfo, "ADDR1_ENG");
                    final String addr2 = KnjDbUtils.getString(personalInfo, "ADDR2_ENG");
                    if (KNJ_EditEdit.getMS932ByteLength(addr1) > 60 || KNJ_EditEdit.getMS932ByteLength(addr2) > 60) {
                        vrsOut("ADDRESS2",  addr1);
                        vrsOut("ADDRESS2_2",  addr2);
                    } else {
                        vrsOut("ADDRESS1",  addr1);
                        vrsOut("ADDRESS1_2",  addr2);
                    }
                }
            }
        }

        private void vrsOutDateMonthYear(final String monthField, final String dayOfMonthField, final String yearField, final String date) {
            if (null == date) {
                return;
            }
            final Calendar cal = Util.getCalendarOfDate(date);
            final int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
            final String dayOfMonthStr = (dayOfMonth < 10 ? " " : "") + String.valueOf(dayOfMonth);
            final int month = cal.get(Calendar.MONTH) + 1;
            final String monthStr = (month < 10 ? " " : "") + String.valueOf(month);
            final String yearStr = String.valueOf(cal.get(Calendar.YEAR));
            vrsOut(monthField, monthStr);
            vrsOut(dayOfMonthField, dayOfMonthStr);
            vrsOut(yearField, yearStr);
        }
        /**
         *  出欠データ
         **/
        private void printAttend(final DB2UDB db2) {
            if (_param._isKaijyo) {
                return;
            }
            final String psKey = "PS_ATTENDREC(isGrd=" + _printData._useGrdTable + ")";
            if (null == _param.getPs(psKey)) {
                //  出欠記録データ
                final String tname1;
                if (_printData._useGrdTable) {
                    tname1 = "GRD_ATTENDREC_DAT";
                } else {
                    tname1 = "SCHREG_ATTENDREC_DAT";
                }
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
                stb.append(         "END AS LESSON,"); //授業日数-休学日数
                stb.append(    "VALUE(SUSPEND,0) + VALUE(MOURNING,0) AS SUSP_MOUR,");          //出停・忌引
                stb.append(    "VALUE(SUSPEND,0) AS SUSPEND,");                                //出停
                stb.append(    "VALUE(MOURNING,0) AS MOURNING,");                              //忌引
                stb.append(    "VALUE(ABROAD,0) AS ABROAD,");                                  //留学
                stb.append(    "CASE WHEN S1.SEM_OFFDAYS = '1' ");
                stb.append(         "THEN VALUE(REQUIREPRESENT,0) + VALUE(OFFDAYS,0) ");
                stb.append(         "ELSE VALUE(REQUIREPRESENT,0) ");
                stb.append(         "END AS REQUIREPRESENT,"); //要出席日数
                stb.append(    "CASE WHEN S1.SEM_OFFDAYS = '1' ");
                stb.append(         "THEN VALUE(SICK,0) + VALUE(ACCIDENTNOTICE,0) + VALUE(NOACCIDENTNOTICE,0) + VALUE(OFFDAYS,0) ");
                stb.append(         "ELSE VALUE(SICK,0) + VALUE(ACCIDENTNOTICE,0) + VALUE(NOACCIDENTNOTICE,0) ");
                stb.append(         "END AS SICK,"); //病欠＋事故欠（届・無）
                stb.append(    "VALUE(PRESENT,0) AS PRESENT ");                                //出席日数
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
                stb.append("ORDER BY ");
                stb.append(    "T1.ANNUAL");
                final String attendrecSql = stb.toString();
                if (_param._isOutputDebugQuery) {
                    log.info(" attendrecSql sql = " + attendrecSql);
                }
                _param.setPs(db2, psKey, attendrecSql);
            }
            final Object[] arg = {_printData._schregno, _printData._year};
            if (_param._isOutputDebugQuery) {
                log.info(" attendrecSql arg = " + ArrayUtils.toString(arg));
            }

            for (final Map row : KnjDbUtils.query(db2, _param.getPs(psKey), arg)) {
                final String annual = KnjDbUtils.getString(row, "ANNUAL");
                final int i;
                final String gradeCd = _printData._gradeGradeCdMap.get(annual);
                if (NumberUtils.isDigits(gradeCd)) {
                    i = Integer.parseInt(gradeCd);
                } else {
                    i = Integer.parseInt(annual);
                }
                final String classDays = KnjDbUtils.getString(row, "CLASSDAYS");
                final String jugyoNissu = KnjDbUtils.getString(row, "LESSON");
                final String shussekiNissu = KnjDbUtils.getString(row, "PRESENT");
                final String kessekiNissu = KnjDbUtils.getString(row, "SICK");
                final String shutteiKibikiNissu = KnjDbUtils.getString(row, "SUSP_MOUR");
                if (_param._isChiyodaKudan) {
                    vrsOutn("ATTEND", i, StringUtils.defaultString(shussekiNissu) + " ／ " + StringUtils.defaultString(jugyoNissu));  //出席
                } else {
                    vrsOutn("DAYS", i, jugyoNissu);  //授業日数
                    vrsOutn("MUST", i, KnjDbUtils.getString(row, "REQUIREPRESENT"));  //出席すべき日数
                    vrsOutn("ATTEND", i, shussekiNissu);  //出席
                    vrsOutn("SICK", i, kessekiNissu);  //欠席
                    vrsOutn("SUSP_MOUR", i, shutteiKibikiNissu);  //欠席
                    vrsOutn("LESSON", i, classDays);
                }
            }
        }

        /**
         *  学習の記録
         */
        private void printStudyrec(final DB2UDB db2) {
            final String sogo = "sogo";
            final String abroad = "abroad";
            final String total = "total";

            final String psKey = "PS_STUDYREC(isGrd=" + _printData._useGrdTable + ")";
            if (null == _param.getPs(psKey)) {
                //  学習記録データ
                final String studyrecSql = getStudyrecSql(_printData, _param);
                if (_param._isOutputDebugQuery) {
                    log.info(" studyrec sql = " + studyrecSql);
                }
                _param.setPs(db2, psKey, studyrecSql);
            }
            final Object[] arg = {_printData._schregno, _printData._year};
            if (_param._isOutputDebugQuery) {
                log.info(" studyrec arg = " + ArrayUtils.toString(arg));
            }
            final List<Map<String, String>> recordList = KnjDbUtils.query(db2, _param.getPs(psKey), arg);
            for (final Iterator<Map<String, String>> it = recordList.iterator(); it.hasNext();) {
                final Map row = it.next();
                final String classname = KnjDbUtils.getString(row, "CLASSNAME");
                if (Arrays.asList(sogo, abroad, total).contains(classname)) {
                    it.remove();
                }
                final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                if (_printData._d065Name1List.contains(subclasscd)) {
                    row.put("GRADES", _param._d001Abbv1Map.get(KnjDbUtils.getString(row, "GRADES")));
                }
            }

            int linex = 0;               //行数
            if (_param._isMatsudo) {
                for (int g = 1; g <= 3; g++) {
                    // year時点の学年より上もしくは入学学年未満は表示しない
                    if (_printData._gradeCdInYear < g || g < _printData._entGradeCd) {
                        continue;
                    }
                    String startMonth = "";
                    String endMonth = "";
                    if (_printData._entGradeCd == g) {
                        startMonth = Util.engMonthName(_printData._entDate);
                    } else if (-1 == _printData._entGradeCd || _printData._entGradeCd < g) {
                        startMonth = "April";
                    }
                    if (_printData._grdGradeCd == g) {
                        endMonth = Util.engMonthName(_printData._grdDate);
                    } else if (-1 == _printData._grdGradeCd || g < _printData._grdGradeCd) {
                        endMonth = "March";
                    }
                    vrsOut("YEAR_STARTMONTH" + String.valueOf(g),  startMonth);
                    vrsOut("YEAR_ENDMONTH" + String.valueOf(g),  endMonth);
                }
            } else if (_param._isReitaku) {
                vrsOut("GRADE1",  "1st");
                vrsOut("GRADE2",  "2nd");
                vrsOut("GRADE3",  "3rd");
            } else if (!_param._isKaijyo) {
                vrsOut("GRADE1",  "7");
                vrsOut("GRADE2",  "8");
                vrsOut("GRADE3",  "9");
            }
            if (_param._isChiyodaKudan) {
                final DecimalFormat df = new DecimalFormat("00");
                for (int gradeCdi = 1; gradeCdi <= 3; gradeCdi++) {
                    final String gradeCd = df.format(gradeCdi);
                    final String year = _printData._gradeCdYearMap.get(gradeCd);
                    if (NumberUtils.isDigits(year)) {
                        final int iYear = Integer.parseInt(year);
                        vrsOut("YEAR_FROM_TO" + String.valueOf(gradeCdi), String.valueOf(iYear) + "～" + String.valueOf(iYear + 1));
                    }
                }

                final int maxClazz = 9;
                List<Map<String, String>> clazzList = new ArrayList();
                for (int i = 0; i < recordList.size(); i++) {
                    final Map row = recordList.get(i);

                    final String classcd = KnjDbUtils.getString(row, "CLASSCD");
                    if (null == classcd) {
                        continue;
                    }
                    Map clazz = null;
                    for (final Map c : clazzList) {
                        if (classcd.equals(c.get("CLASSCD"))) {
                            clazz = c;
                            break;
                        }
                    }
                    if (null == clazz) {
                        clazz = new HashMap();
                        clazzList.add(clazz);
                        clazz.put("CLASSCD", classcd);
                        Util.getMappedList(clazz, "ROW_LIST").add(row);
                    }
                    final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                    final List subclasscdList = Util.getMappedList(clazz, "SUBCLASSCD_LIST");
                    if (!subclasscdList.contains(subclasscd)) {
                        subclasscdList.add(subclasscd);
                    }
                    Util.getMappedList(clazz, "SUBCLASS" + subclasscd + "_LIST").add(row);
                }
                if (_param._isOutputDebug) {
                    for (final Map clazz : clazzList) {

                        linex++;
                        final List subclasscdList = Util.getMappedList(clazz, "SUBCLASSCD_LIST");
                        log.info(" class = " + clazz.get("CLASSCD") + ", subclass = " + subclasscdList);
                    }
                }
                clazzList = clazzList.subList(0, Math.min(clazzList.size(), maxClazz));
                for (final Map clazz : clazzList) {

                    linex++;
                    final List subclasscdList = Util.getMappedList(clazz, "SUBCLASSCD_LIST");
                    final boolean has2Subclass = subclasscdList.size() > 1;
                    for (int subi = 0; subi < subclasscdList.size(); subi++) {
                        final String subclasscd = (String) subclasscdList.get(subi);
                        final String ssub = String.valueOf(subi + 1);

                        final List rowList = Util.getMappedList(clazz, "SUBCLASS" + subclasscd + "_LIST");
                        for (int k = 0; k < rowList.size(); k++) {
                            final Map row = (Map) rowList.get(k);
                            final String classname = StringUtils.defaultString(KnjDbUtils.getString(row, "CLASSNAME"));
                            final int classnameketa = classname.length();

                            final String[] classnameSplit;
                            final String[] classnameField;
                            if (has2Subclass) {
                                final int classnameFieldKeta = 12;
                                final int subclassnameFieldKeta = 12;
                                if (classnameketa <= classnameFieldKeta) {
                                    classnameSplit = new String[] {classname};
                                    classnameField = new String[] {"SUBJECT2"};
                                } else {
                                    classnameSplit = Util.splitBySizeWithSpace(classname, new int[] {classnameFieldKeta});
                                    classnameField = new String[] {"SUBJECT2_2_1", "SUBJECT2_2_2"};
                                }
                                final String subclassname = StringUtils.defaultString(KnjDbUtils.getString(row, "SUBCLASSNAME"));
                                final int subclassnameketa = subclassname.length();
                                final String[] subclassnameSplit;
                                final String[] subclassnameField;
                                if (subclassnameketa <= subclassnameFieldKeta) {
                                    subclassnameSplit = new String[] {subclassname};
                                    subclassnameField = new String[] {"SUBJECT2_SUB" + ssub};
                                } else {
                                    subclassnameSplit = Util.splitBySizeWithSpace(subclassname, new int[] {subclassnameFieldKeta});
                                    subclassnameField = new String[] {"SUBJECT2_SUB" + ssub + "_2_1", "SUBJECT2_SUB" + ssub + "_2_2"};
                                }
                                for (int si = 0; si < Math.min(subclassnameField.length, subclassnameSplit.length); si++) {
                                    vrsOut(subclassnameField[si], subclassnameSplit[si]);  //科目名
                                }
                            } else {
                                final int classnameFieldKeta = 24;
                                if (classnameketa <= classnameFieldKeta) {
                                    classnameSplit = new String[] {classname};
                                    classnameField = new String[] {"SUBJECT"};
                                } else {
                                    classnameSplit = Util.splitBySizeWithSpace(classname, new int[] {classnameFieldKeta});
                                    classnameField = new String[] {"SUBJECT_2_1", "SUBJECT_2_2"};
                                }
                            }
                            for (int si = 0; si < Math.min(classnameField.length, classnameSplit.length); si++) {
                                vrsOut(classnameField[si], classnameSplit[si]);  //教科名
                            }
                            //学年ごとの出力 評定
                            final int g = Integer.parseInt(KnjDbUtils.getString(row, "ANNUAL"));
                            final String grades = KnjDbUtils.getString(row, "GRADES");
                            if (1 <= g && g <= 3 && grades != null) {
                                if (has2Subclass) {
                                    vrsOut("GRADING" + g + "_SUB" + ssub, grades);
                                } else {
                                    vrsOut("GRADING" + g, grades);
                                }
                            }
                        }
                    }
                    _svf.VrEndRecord();
                }
                for (int i = clazzList.size(); i < maxClazz; i++) {
                    vrsOut("SUBJECT", String.valueOf(i));
                    _svf.VrAttribute("SUBJECT", "X=10000");
                    _svf.VrEndRecord();
                }
                hasData = true;
            } else if (_param._isKaijyo) {

                final int maxClazz = 9;
                List<Map<String, String>> clazzList = new ArrayList();
                for (int i = 0; i < recordList.size(); i++) {
                    final Map row = recordList.get(i);

                    final String classcd = KnjDbUtils.getString(row, "CLASSCD");
                    if (null == classcd) {
                        continue;
                    }
                    Map clazz = null;
                    for (final Map c : clazzList) {
                        if (classcd.equals(c.get("CLASSCD"))) {
                            clazz = c;
                            break;
                        }
                    }
                    if (null == clazz) {
                        clazz = new HashMap();
                        clazzList.add(clazz);
                        clazz.put("CLASSCD", classcd);
                        Util.getMappedList(clazz, "ROW_LIST").add(row);
                    }
                    final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                    final List subclasscdList = Util.getMappedList(clazz, "SUBCLASSCD_LIST");
                    if (!subclasscdList.contains(subclasscd)) {
                        subclasscdList.add(subclasscd);
                    }
                    Util.getMappedList(clazz, "SUBCLASS" + subclasscd + "_LIST").add(row);
                }
                if (_param._isOutputDebug) {
                    for (int i = 0; i < clazzList.size(); i++) {
                        final Map clazz = clazzList.get(i);

                        linex++;
                        final List subclasscdList = Util.getMappedList(clazz, "SUBCLASSCD_LIST");
                        log.info(" class = " + clazz.get("CLASSCD") + ", subclass = " + subclasscdList);
                    }
                }
                clazzList = clazzList.subList(0, Math.min(clazzList.size(), maxClazz));
                for (int i = 0; i < clazzList.size(); i++) {
                    final Map clazz = clazzList.get(i);

                    linex++;
                    final List subclasscdList = Util.getMappedList(clazz, "SUBCLASSCD_LIST");
                    for (int subi = 0; subi < subclasscdList.size(); subi++) {
                        final String subclasscd = (String) subclasscdList.get(subi);

                        final List rowList = Util.getMappedList(clazz, "SUBCLASS" + subclasscd + "_LIST");
                        for (int k = 0; k < rowList.size(); k++) {
                            final Map row = (Map) rowList.get(k);
                            vrsOut("SUBJECT", StringUtils.defaultString(KnjDbUtils.getString(row, "CLASSNAME")));  //教科名
                            //学年ごとの出力
                            final int g = Integer.parseInt(KnjDbUtils.getString(row, "ANNUAL"));
                            final String grades = KnjDbUtils.getString(row, "GRADES");
                            if (1 <= g && g <= 3 && grades != null) {
                                vrsOut("GRADING" + g, grades);  //評定
                            }
                        }
                    }
                    _svf.VrEndRecord();
                }
                for (int i = clazzList.size(); i < maxClazz; i++) {
                    vrsOut("SUBJECT", String.valueOf(i));
                    _svf.VrAttribute("SUBJECT", "X=10000");
                    _svf.VrEndRecord();
                }
                hasData = true;
            } else {
                final int maxLine = 10;
                String s_classcd = null;  //科目コード
                for (final Map row : recordList) {

                    final String classname = KnjDbUtils.getString(row, "CLASSNAME");
                    //教科コードの変わり目
                    if (s_classcd == null || !KnjDbUtils.getString(row, "CLASSCD").equals(s_classcd)) {
                        linex++;
                        if (maxLine < linex) {
                            break;  //行のオーバーフロー
                        }
                        if (_param._isMeikei) {
                            vrsOutn("SUBJECT", linex, StringUtils.defaultString(KnjDbUtils.getString(row, "SUBCLASSNAME")));  //教科名
                        } else {
                            vrsOutn("SUBJECT", linex, classname);  //教科名

                        }
                        s_classcd = KnjDbUtils.getString(row, "CLASSCD");
                        final String credit = KnjDbUtils.getString(row, "CREDIT");
                        if (credit != null && 0 < Integer.parseInt(credit)) {
                            vrsOutn("CREDIT", linex, credit);  //単位数の合計
                        }
                        if (_param._isMatsudo) {
                            for (int g = 1; g <= 3; g++) {
                                if (_printData._gradeCdInYear < g) {
                                    continue;
                                }
                                vrsOutn("GRADING" + g, linex, "NE");  //評定
                            }
                        }
                    }
                    //学年ごとの出力
                    final String annual = KnjDbUtils.getString(row, "ANNUAL");
                    final int g;
                    final String gradeCd = _printData._gradeGradeCdMap.get(annual);
                    if (NumberUtils.isDigits(gradeCd)) {
                        g = Integer.parseInt(gradeCd);
                    } else {
                        g = Integer.parseInt(annual);
                    }
                    final String hyotei = KnjDbUtils.getString(row, "GRADES");
                    if (1 <= g && g <= 3 && hyotei != null) {
                        vrsOutn("GRADING" + g, linex, hyotei);  //評定
                    }
                    hasData = true;
                }
            }
        }

        private String getStudyrecSql(final PrintData printData, final Param param) {

            String tname1 = null;        // SCHREG_STUDYREC_DAT
            String tname2 = null;        // SCHREG_TRANSFER_DAT
            String tname3 = null;        // SCHREG_REGD_DAT

             if (printData._useGrdTable) {
                 tname1 = "GRD_STUDYREC_DAT";
                 tname2 = "GRD_TRANSFER_DAT";
                 tname3 = "GRD_REGD_DAT";
             } else {
                 tname1 = "SCHREG_STUDYREC_DAT";
                 tname2 = "SCHREG_TRANSFER_DAT";
                 tname3 = "SCHREG_REGD_DAT";
             }
             log.debug("table1->"+tname1+"   tname2->"+tname2+"   tname3->"+tname3);
             final StringBuffer stb = new StringBuffer();

             final String targetSchoolKind = "1".equals(param._useCurriculumcd) && param._hasSCHOOL_MST_SCHOOL_KIND ? SCHOOL_KIND_J : null;
             final boolean notUseStudyrecProvFlgDat = false;
             if (param._isKindai) {
                 stb.append("WITH STUDYREC AS(");
                 stb.append("SELECT  T1.CLASSNAME, ");
                 stb.append(        "T1.SUBCLASSNAME, ");
                 stb.append(        "T1.SCHREGNO, ");
                 stb.append(        "T1.YEAR, ");
                 stb.append(        "T1.ANNUAL, ");
                 stb.append(        "T1.CLASSCD, ");
                 if ("1".equals(param._useCurriculumcd)) {
                     stb.append(        "T1.CLASSCD || '-' || T1.SCHOOL_KIND AS CLASS_SCHK, ");
                 }
                 if ("1".equals(param._useCurriculumcd)) {
                     stb.append(        "T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                 }
                 stb.append(        "VALUE(L2.SUBCLASSCD2, T1.SUBCLASSCD) AS SUBCLASSCD, ");
                 stb.append(        "T1.VALUATION AS GRADES ");
                 stb.append(       ",CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) ELSE T1.GET_CREDIT END AS CREDIT ");
                 stb.append("FROM   " + tname1 + " T1 ");
                 stb.append(        "LEFT JOIN SUBCLASS_MST L2 ON ");
                 if ("1".equals(param._useCurriculumcd)) {
                     stb.append(        "L2.CLASSCD || '-' || L2.SCHOOL_KIND || '-' || L2.CURRICULUM_CD || '-' || ");
                 }
                 stb.append(            "L2.SUBCLASSCD=");
                 if ("1".equals(param._useCurriculumcd)) {
                     stb.append(        "T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                 }
                 stb.append(            "T1.SUBCLASSCD ");
                 if (notUseStudyrecProvFlgDat) {
                 } else {
                     stb.append(        "LEFT JOIN STUDYREC_PROV_FLG_DAT TPROV ON TPROV.SCHOOLCD = T1.SCHOOLCD ");
                     stb.append(        "    AND TPROV.YEAR = T1.YEAR ");
                     stb.append(        "    AND TPROV.SCHREGNO = T1.SCHREGNO ");
                     if ("1".equals(param._useCurriculumcd)) {
                         stb.append(        "    AND TPROV.CLASSCD = T1.CLASSCD ");
                         stb.append(        "    AND TPROV.SCHOOL_KIND = T1.SCHOOL_KIND ");
                         stb.append(        "    AND TPROV.CURRICULUM_CD = T1.CURRICULUM_CD ");
                     }
                     stb.append(        "    AND TPROV.SUBCLASSCD = T1.SUBCLASSCD ");
                     stb.append(        "    AND TPROV.PROV_FLG = '1' ");
                 }
                 stb.append("WHERE   T1.SCHREGNO = ? AND ");
                 stb.append(        "T1.YEAR <= ? AND ");
                 stb.append(        "(T1.CLASSCD BETWEEN '" + KNJDefineCode.subject_D + "' AND '" + KNJDefineCode.subject_U + "' OR T1.CLASSCD = '" + KNJDefineCode.subject_T + "') ");
                 if (null != targetSchoolKind) {
                     stb.append(        "AND T1.SCHOOL_KIND = '" + targetSchoolKind + "' ");
                 }
                 stb.append(        " AND NOT EXISTS(SELECT  'X' ");
                 stb.append(                   "FROM    SUBCLASS_REPLACE_COMBINED_DAT T2 ");
                 stb.append(                   "WHERE   T2.YEAR = T1.YEAR AND ");
                 if ("1".equals(param._useCurriculumcd)) {
                     stb.append(        "T2.ATTEND_CLASSCD || '-' || T2.ATTEND_SCHOOL_KIND || '-' || T2.ATTEND_CURRICULUM_CD || '-' || ");
                 }
                 stb.append(                           " T2.ATTEND_SUBCLASSCD = ");
                 if ("1".equals(param._useCurriculumcd)) {
                     stb.append(        "T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                 }
                 stb.append(                           " T1.SUBCLASSCD) ");
                 if (notUseStudyrecProvFlgDat) {
                 } else {
                     stb.append(        "    AND TPROV.SUBCLASSCD IS NULL ");
                 }
                 stb.append(    ")");

             } else {
                 stb.append("WITH T_STUDYREC AS(");
                 stb.append("SELECT  T1.CLASSNAME, ");
                 stb.append(        "T1.SUBCLASSNAME, ");
                 stb.append(        "T1.SCHREGNO, ");
                 stb.append(        "T1.YEAR, ");
                 stb.append(        "T1.ANNUAL, ");
                 if ("1".equals(param._useCurriculumcd)) {
                     stb.append(        "T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                 }
                 stb.append(        "T1.SUBCLASSCD AS SUBCLASSCD, ");
                 if ("1".equals(param._useCurriculumcd)) {
                     stb.append(        "T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                 }
                 stb.append(        "L2.SUBCLASSCD2 AS SUBCLASSCD2, ");
                 stb.append(        "T1.VALUATION AS GRADES ");
                 stb.append(   ",T1.CLASSCD ");
                 if ("1".equals(param._useCurriculumcd)) {
                     stb.append(   ",T1.CLASSCD || '-' || T1.SCHOOL_KIND AS CLASS_SCHK ");
                 }
                 stb.append(       ",CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) ELSE T1.GET_CREDIT END AS CREDIT ");
                 stb.append("FROM   " + tname1 + " T1 ");
                 stb.append(        "LEFT JOIN SUBCLASS_MST L2 ON ");
                 if ("1".equals(param._useCurriculumcd)) {
                     stb.append(        "L2.CLASSCD || '-' || L2.SCHOOL_KIND || '-' || L2.CURRICULUM_CD || '-' || ");
                 }
                 stb.append(            "L2.SUBCLASSCD=");
                 if ("1".equals(param._useCurriculumcd)) {
                     stb.append(        "T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                 }
                 stb.append(            "T1.SUBCLASSCD ");
                 if (notUseStudyrecProvFlgDat) {
                 } else {
                     stb.append(        "LEFT JOIN STUDYREC_PROV_FLG_DAT TPROV ON TPROV.SCHOOLCD = T1.SCHOOLCD ");
                     stb.append(        "    AND TPROV.YEAR = T1.YEAR ");
                     stb.append(        "    AND TPROV.SCHREGNO = T1.SCHREGNO ");
                     if ("1".equals(param._useCurriculumcd)) {
                         stb.append(        "    AND TPROV.CLASSCD = T1.CLASSCD ");
                         stb.append(        "    AND TPROV.SCHOOL_KIND = T1.SCHOOL_KIND ");
                         stb.append(        "    AND TPROV.CURRICULUM_CD = T1.CURRICULUM_CD ");
                     }
                     stb.append(        "    AND TPROV.SUBCLASSCD = T1.SUBCLASSCD ");
                     stb.append(        "    AND TPROV.PROV_FLG = '1' ");
                 }
                 stb.append("WHERE   T1.SCHREGNO = ? AND T1.YEAR <= ? AND ");
                 stb.append(        "(T1.CLASSCD BETWEEN '" + KNJDefineCode.subject_D + "' AND '" + KNJDefineCode.subject_U + "' OR T1.CLASSCD = '" + KNJDefineCode.subject_T + "') ");
                 if (null != targetSchoolKind) {
                     stb.append(        "AND T1.SCHOOL_KIND = '" + targetSchoolKind + "' ");
                 }
//                 if (isNotPrintMirishu(_config)) {
//                     stb.append(        "AND VALUE(T1.COMP_CREDIT, 0) <> 0 ");
//                 }
                 if (notUseStudyrecProvFlgDat) {
                 } else {
                     stb.append(        "    AND TPROV.SUBCLASSCD IS NULL ");
                 }
                 stb.append(") , STUDYREC0 AS( ");
                 stb.append(    "SELECT ");
                 stb.append(        "T1.CLASSNAME, ");
                 stb.append(        "T1.SUBCLASSNAME, ");
                 stb.append(        "T1.SCHREGNO, ");
                 stb.append(        "T1.YEAR, ");
                 stb.append(        "T1.ANNUAL, ");
                 stb.append(        "T1.CLASSCD , ");
                 if ("1".equals(param._useCurriculumcd)) {
                     stb.append(        "T1.CLASS_SCHK , ");
                 }
                 stb.append(        "T1.SUBCLASSCD, ");
                 stb.append(        "T1.GRADES, ");
                 stb.append(        "T1.CREDIT ");
                 stb.append(    "FROM ");
                 stb.append(        "T_STUDYREC T1 ");
                 stb.append(    "WHERE ");
                 stb.append(        "T1.SUBCLASSCD2 IS NULL ");
                 stb.append(    "UNION ALL ");
                 stb.append(    "SELECT ");
                 stb.append(        "MAX(T1.CLASSNAME) AS CLASSNAME, ");
                 stb.append(        "MAX(T1.SUBCLASSNAME) AS SUBCLASSNAME, ");
                 stb.append(        "T1.SCHREGNO, ");
                 stb.append(        "T1.YEAR, ");
                 stb.append(        "MAX(T1.ANNUAL) AS ANNUAL, ");
                 stb.append(        "T1.CLASSCD, ");
                 if ("1".equals(param._useCurriculumcd)) {
                     stb.append(        "MAX(T1.CLASS_SCHK) AS CLASS_SCHK, ");
                 }
                 stb.append(        "T1.SUBCLASSCD2 AS SUBCLASSCD, ");
                 stb.append(        "MAX(T1.GRADES) AS GRADES, ");
                 stb.append(        "SUM(T1.CREDIT) AS CREDIT ");
                 stb.append(    "FROM ");
                 stb.append(        "T_STUDYREC T1 ");
                 stb.append(    "WHERE ");
                 stb.append(        "T1.SUBCLASSCD2 IS NOT NULL ");
                 stb.append(    "GROUP BY ");
                 stb.append(        "T1.SCHREGNO, ");
                 stb.append(        "T1.YEAR, ");
                 stb.append(        "T1.CLASSCD, ");
                 stb.append(        "T1.SUBCLASSCD2 ");
                 stb.append(") , STUDYREC AS( ");
                 stb.append(    "SELECT ");
                 stb.append(        "T1.CLASSNAME, ");
                 stb.append(        "T1.SUBCLASSNAME, ");
                 stb.append(        "T1.SCHREGNO, ");
                 stb.append(        "T1.YEAR, ");
                 stb.append(        "T1.ANNUAL, ");
                 stb.append(        "T1.CLASSCD , ");
                 if ("1".equals(param._useCurriculumcd)) {
                     stb.append(        "T1.CLASS_SCHK , ");
                 }
                 stb.append(        "T1.SUBCLASSCD, ");
                 stb.append(        "MAX(T1.GRADES) AS GRADES, ");
                 stb.append(        "SUM(T1.CREDIT) AS CREDIT ");
                 stb.append(    "FROM ");
                 stb.append(        "STUDYREC0 T1 ");
                 stb.append(    "GROUP BY ");
                 stb.append(        "T1.CLASSNAME, ");
                 stb.append(        "T1.SUBCLASSNAME, ");
                 stb.append(        "T1.SCHREGNO, ");
                 stb.append(        "T1.YEAR, ");
                 stb.append(        "T1.ANNUAL, ");
                 stb.append(        "T1.CLASSCD , ");
                 if ("1".equals(param._useCurriculumcd)) {
                     stb.append(        "T1.CLASS_SCHK , ");
                 }
                 stb.append(        "T1.SUBCLASSCD ");
                 stb.append(") ");
             }

             //該当生徒の科目評定、修得単位及び教科評定平均
             stb.append( "SELECT ");
             stb.append(     "VALUE(T2.SPECIALDIV, '0') as SPECIALDIV ");
             stb.append(     ", T2.SHOWORDER2 as CLASS_ORDER ");
             stb.append(     ", T3.SHOWORDER2 as SUBCLASS_ORDER ");
             stb.append(     ", T1.ANNUAL ");
             stb.append(     ", T1.YEAR ");
             stb.append(     ", T1.CLASSCD ");
             stb.append(     ", T2.CLASSNAME_ENG AS CLASSNAME ");
             stb.append(     ", T1.SUBCLASSCD ");
             stb.append(     ", T3.SUBCLASSNAME_ENG AS SUBCLASSNAME ");
             stb.append(     ", T1.GRADES AS GRADES ");
             stb.append(     ", T4.CREDIT ");
             stb.append( "FROM ");
             stb.append(     "STUDYREC T1 ");
             stb.append(     "LEFT JOIN CLASS_MST T2 ON ");
             if ("1".equals(param._useCurriculumcd)) {
                 stb.append(        " T2.CLASSCD || '-' || T2.SCHOOL_KIND = T1.CLASS_SCHK ");
             } else {
                 stb.append(        " T2.CLASSCD = T1.CLASSCD ");
             }
             stb.append(     "LEFT JOIN SUBCLASS_MST T3 ON ");
             if ("1".equals(param._useCurriculumcd)) {
                 stb.append(        " T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || ");
             }
             stb.append(            " T3.SUBCLASSCD = T1.SUBCLASSCD ");
             //  修得単位数の計
             stb.append(     "INNER JOIN(SELECT ");
             stb.append(             "CLASSCD,SUBCLASSCD,SUM(T1.CREDIT) AS CREDIT ");
             stb.append(         "FROM ");
             stb.append(             "STUDYREC T1 ");
             stb.append(         "WHERE ");
             stb.append(             "CLASSCD BETWEEN '" + KNJDefineCode.subject_D + "' AND '" + KNJDefineCode.subject_U + "' ");
             stb.append(         "GROUP BY ");
             stb.append(             "CLASSCD,SUBCLASSCD ");
             stb.append(     ")T4 ON T4.SUBCLASSCD = T1.SUBCLASSCD ");
             stb.append( "WHERE ");
             stb.append(     "T1.CLASSCD BETWEEN '" + KNJDefineCode.subject_D + "' AND '" + KNJDefineCode.subject_U + "' ");

             stb.append( "ORDER BY SPECIALDIV, CLASS_ORDER, CLASSCD, SUBCLASS_ORDER, SUBCLASSCD, ANNUAL");
             return stb.toString();
        }
    }

    private static class PrintData {
        final String _year;
        final String _semester;
        final String _date;
        final String _schregno;
        final Map _paramap;
        final String _staffCd;
//        final int _paper;
//        final String _kanji;
        final String _certifNumber;
        final boolean _useGrdTable;
        Map<String, String> _gradeGradeCdMap;
        final String _ctrlYear;
        final String _documentroot;
        int _entGradeCd = -1;
        int _grdGradeCd = -1;

        private String _certifSchoolDatSchoolName;
        private String _certifSchoolDatPrincipalName;
        private String _certifSchoolDatRemark4;
        private String _schoolkind;
        private Map<String, String> _gradeCdYearMap;
        boolean _usePrincipalSignatureImage;
        boolean _isPrintStamp;
        private int _gradeCdInYear;
        private Map<String, String> _personalInfo;
        private String _entDate;
        private String _grdDate;
        List<String> _d065Name1List;

        PrintData(
                final DB2UDB db2,
                final String year,
                final String semester,
                final String date,
                final String schregno,
                final Map paramap,
                final String staffCd,
                final int paper,
                final String kanji,
                final String certifNumber
                ) {
            _year = year;
            _semester = semester;
            _date = date;
            _schregno = schregno;
            _paramap = paramap;
            _staffCd = staffCd;
//            _paper = paper;
//            _kanji = kanji;
            _certifNumber = certifNumber;
            _ctrlYear = (String) _paramap.get("CTRL_YEAR");
            _documentroot = (String) _paramap.get("DOCUMENTROOT");

            _useGrdTable = null != KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SCHREGNO FROM GRD_BASE_MST WHERE SCHREGNO = '" + _schregno + "'"));  //卒・在判定
            _d065Name1List = KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, " SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D065' AND NAME1 IS NOT NULL "), "NAME1");
        }

        public void load(final DB2UDB db2, final Param param) {
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, " SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _ctrlYear + "' AND CERTIF_KINDCD = '" + CERTIF_KINDCD + "' "));
            if (param._isOutputDebug) {
                log.info(" certifSchoolDat ( year = '" + _ctrlYear + "', certif_kindcd = '" + CERTIF_KINDCD + "') = " + row);
            }

            _certifSchoolDatPrincipalName = KnjDbUtils.getString(row, "PRINCIPAL_NAME");
            _certifSchoolDatSchoolName = KnjDbUtils.getString(row, "SCHOOL_NAME");
            _certifSchoolDatRemark4 = KnjDbUtils.getString(row, "REMARK4");

            _schoolkind = getSchoolKind(db2, _year);
            _gradeCdYearMap = getGradeCdYearMap(db2);

            final String gradeCdInYearSql = " SELECT GRADE_CD FROM SCHREG_REGD_GDAT WHERE (YEAR, GRADE) IN (SELECT YEAR, GRADE FROM SCHREG_REGD_DAT WHERE SCHREGNO = '" + _schregno + "' AND YEAR = '" + _year + "') ";
            _gradeCdInYear = KnjDbUtils.getInt(KnjDbUtils.firstRow(KnjDbUtils.query(db2, gradeCdInYearSql)), "GRADE_CD", -1);

            final String sql = " SELECT GRADE, GRADE_CD FROM SCHREG_REGD_GDAT WHERE YEAR IN (SELECT MIN(YEAR) FROM SCHREG_REGD_GDAT WHERE YEAR IN ('" + _year + "', '" + _ctrlYear + "')) ";
            _gradeGradeCdMap = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, sql), "GRADE", "GRADE_CD");

            _personalInfo = getPersonalInfo(db2, param);
            _entDate = KnjDbUtils.getString(_personalInfo, "ENT_DATE");
            _grdDate = KnjDbUtils.getString(_personalInfo, "GRD_DATE");

            final StringBuffer entGrdSql = new StringBuffer();
            entGrdSql.append(" WITH ENTGRD AS ( ");
            entGrdSql.append("     SELECT ");
            entGrdSql.append("         T1.* ");
            entGrdSql.append("       , FISCALYEAR(T1.ENT_DATE) AS ENT_YEAR ");
            entGrdSql.append("       , FISCALYEAR(T1.GRD_DATE) AS GRD_YEAR ");
            entGrdSql.append("     FROM SCHREG_ENT_GRD_HIST_DAT T1 ");
            entGrdSql.append("     WHERE T1.SCHREGNO = '" + _schregno + "' AND T1.SCHOOL_KIND = '" + SCHOOL_KIND_J + "'");
            entGrdSql.append(" ) ");
            entGrdSql.append(" , REGD AS ( ");
            entGrdSql.append("     SELECT DISTINCT ");
            entGrdSql.append("         T1.SCHREGNO ");
            entGrdSql.append("       , T1.YEAR ");
            entGrdSql.append("       , T1.GRADE ");
            entGrdSql.append("       , T2.GRADE_CD ");
            entGrdSql.append("     FROM SCHREG_REGD_DAT T1 ");
            entGrdSql.append("     LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ");
            entGrdSql.append("     WHERE T1.SCHREGNO = '" + _schregno + "' AND T1.YEAR <= '" + _year + "'");
            entGrdSql.append(" ) ");
            entGrdSql.append(" SELECT ");
            entGrdSql.append("     T1.* ");
            entGrdSql.append("   , T2.GRADE_CD AS ENT_GRADE_CD ");
            entGrdSql.append("   , T3.GRADE_CD AS GRD_GRADE_CD ");
            entGrdSql.append(" FROM ENTGRD T1 ");
            entGrdSql.append(" LEFT JOIN REGD T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.YEAR = T1.ENT_YEAR ");
            entGrdSql.append(" LEFT JOIN REGD T3 ON T3.SCHREGNO = T1.SCHREGNO AND T3.YEAR = T1.GRD_YEAR ");
            final Map entGrdRow = KnjDbUtils.firstRow(KnjDbUtils.query(db2, entGrdSql.toString()));
            _entGradeCd = KnjDbUtils.getInt(entGrdRow, "ENT_GRADE_CD", -1);
            _grdGradeCd = KnjDbUtils.getInt(entGrdRow, "GRD_GRADE_CD", -1);
        }

        private Map getPersonalInfo(final DB2UDB db2, final Param param) {
            final String psKey = "PS_PERSONAL(isGrd=" + _useGrdTable + ")";
            //  個人データ
            if (null == param.getPs(psKey)) {
                final KNJ_PersonalinfoSql o = new KNJ_PersonalinfoSql();
                final Map paramMap = new HashMap();
                paramMap.put("PRINT_GRD", _useGrdTable ? "1" : "0");
                paramMap.put("SCHOOL_MST_SCHOOL_KIND", param._hasSCHOOL_MST_SCHOOL_KIND ? SCHOOL_KIND_J : null);
                final String sql = o.sql_info_reg("111110110000J", paramMap);
                if (param._isOutputDebugQuery) {
                    log.info(" personalinfo sql = " + sql);
                }
                param.setPs(db2, psKey, sql);
            }
            final Object[] arg = {_schregno, _year, _semester, _schregno, _year};
            if (param._isOutputDebugQuery) {
                log.info(" personalinfo arg = " + ArrayUtils.toString(arg));
            }

            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, param.getPs(psKey), arg));
            return row;
        }

        private String getSchoolKind(final DB2UDB db2, final String year) {
            String sql = "";
            sql += " SELECT T2.SCHOOL_KIND ";
            sql += " FROM (SELECT MIN(GRADE) AS GRADE FROM SCHREG_REGD_DAT WHERE YEAR = '" + year + "' AND SCHREGNO = '" + _schregno + "') T1 ";
            sql += " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = '" + year + "' AND T2.GRADE = T1.GRADE ";

            String schoolKind = KnjDbUtils.getOne(KnjDbUtils.query(db2, sql));
            return schoolKind;
        }

        private Map<String, String> getGradeCdYearMap(final DB2UDB db2) {
            Map<String, String> rtn = new HashMap();
            String sql1 = "";
            sql1 += " SELECT T2.GRADE_CD, MAX(T1.YEAR) AS YEAR ";
            sql1 += " FROM SCHREG_REGD_DAT T1 ";
            sql1 += " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR ";
            sql1 += "     AND T2.GRADE = T1.GRADE ";
            sql1 += "     AND T2.SCHOOL_KIND = 'J' ";
            sql1 += " WHERE T1.SCHREGNO = '" + _schregno + "' ";
            sql1 += " GROUP BY T2.GRADE_CD ";

            rtn = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, sql1), "GRADE_CD", "YEAR");

            String sql2 = "";
            sql2 += " SELECT T2.GRADE_CD, T1.ANNUAL, MAX(T1.YEAR) AS YEAR ";
            sql2 += " FROM SCHREG_STUDYREC_DAT T1 ";
            sql2 += " LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR ";
            sql2 += "     AND T2.GRADE = T1.ANNUAL ";
            sql2 += "     AND T2.SCHOOL_KIND = 'J' ";
            sql2 += " WHERE T1.SCHREGNO = '" + _schregno + "' ";
            sql2 += " GROUP BY T2.GRADE_CD, T1.ANNUAL ";

            for (final Map row : KnjDbUtils.query(db2, sql2)) {
//                log.info(" " + KnjDbUtils.resultSetToRowMap(meta, rs));
                final String gradeCd = KnjDbUtils.getString(row, "GRADE_CD");
                if (null != rtn.get(gradeCd)) {
                    continue;
                }
                rtn.put(gradeCd, KnjDbUtils.getString(row, "YEAR"));
            }
            return rtn;

        }
    }

    private static class Param {
        final Map _initParamap;
        final String _useCurriculumcd;
        final boolean _isKindai;
        final String _z010Name1;
        final boolean _hasSCHOOL_MST_SCHOOL_KIND;
        final boolean _isChiyodaKudan;
        final boolean _isKaijyo;
        final boolean _isOsakatoin;
        final boolean _isSakae;
        final boolean _isMeikei;
        final boolean _isMatsudo;
        final boolean _isReitaku;
        final Map<String, String> _d001Abbv1Map;
        final boolean _isOutputDebugAll;
        final boolean _isOutputDebug;
        final boolean _isOutputDebugQuery;
        final boolean _isOutputDebugVrsout;
        final Map _formInfo = new HashMap();
        final Map _controlMstMap;
        private Map<String, PreparedStatement> _psMap = new HashMap();
        private HashSet<String> _logOnce = new HashSet();
        private final Map<String, File> _createdFormFiles = new TreeMap<String, File>();

        Param(final DB2UDB db2, final Map initParamap) {
            _initParamap = initParamap;
            _useCurriculumcd = (String) initParamap.get("useCurriculumcd");
            _z010Name1 = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00'"));
            log.info(" z010 = " + _z010Name1);
            _isKindai = "KINDAI".equals(_z010Name1) || "KINJUNIOR".equals(_z010Name1);
            _isChiyodaKudan = "chiyoda".equals(_z010Name1);
            _isKaijyo = "kaijyo".equals(_z010Name1);
            _isOsakatoin = "osakatoin".equals(_z010Name1);
            _isSakae = "sakae".equals(_z010Name1);
            _isMeikei = "meikei".equals(_z010Name1);
            _isMatsudo = "matsudo".equals(_z010Name1);
            _isReitaku = "reitaku".equals(_z010Name1);
            _hasSCHOOL_MST_SCHOOL_KIND = KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND");
            _d001Abbv1Map = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, " SELECT NAMECD2, ABBV1 FROM NAME_MST WHERE NAMECD1 = 'D001' AND ABBV1 IS NOT NULL "), "NAMECD2", "ABBV1");
            final String[] outputDebug = StringUtils.split(getDbPrginfoProperties(db2, "outputDebug"), " ");
            _isOutputDebugAll = ArrayUtils.contains(outputDebug, "all");
            _isOutputDebug = ArrayUtils.contains(outputDebug, "1");
            _isOutputDebugQuery = ArrayUtils.contains(outputDebug, "query");
            _isOutputDebugVrsout = ArrayUtils.contains(outputDebug, "vrsout");
            _controlMstMap = KnjDbUtils.firstRow(KnjDbUtils.query(db2, "SELECT CTRL_YEAR, CTRL_SEMESTER, CTRL_DATE, IMAGEPATH, EXTENSION FROM CONTROL_MST WHERE CTRL_NO = '01' "));
        }

        public void closePs() {
            for (final PreparedStatement ps : _psMap.values()) {
                DbUtils.closeQuietly(ps);
            }
        }

        public void closeFile() {
            for (final PreparedStatement ps : _psMap.values()) {
                DbUtils.closeQuietly(ps);
            }
            for (final File file : _createdFormFiles.values()) {
                try {
                    file.delete();
                } catch (Exception e) {
                    log.error("exception!", e);
                }
            }
        }

        private void setPs(final DB2UDB db2, final String psKey, final String sql) {
            try {
                _psMap.put(psKey, db2.prepareStatement(sql));
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }

        private PreparedStatement getPs(final String psKey) {
            return _psMap.get(psKey);
        }

        private void logOnce(final String s) {
            if (_logOnce.contains(s)) {
                return;
            }
            log.info(s);
            _logOnce.add(s);
        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJE080' AND NAME = '" + propName + "' "));
        }

        private String getImagePath(final String documentroot, final String filename) {
            final String imageDir = KnjDbUtils.getString(_controlMstMap, "IMAGEPATH");
            if (null == documentroot) {
                logOnce(" documentroot null.");
                return null;
            } // DOCUMENTROOT
            if (null == imageDir) {
                logOnce(" imageDir null.");
                return null;
            }
            if (null == filename) {
                logOnce(" filename null.");
                return null;
            }
            final StringBuffer stb = new StringBuffer();
            stb.append(documentroot);
            stb.append("/");
            stb.append(imageDir);
            stb.append("/");
            stb.append(filename);
            final File file = new File(stb.toString());
            logOnce("image file:" + file.getAbsolutePath() + " exists? " + file.exists());
            if (!file.exists()) {
                return null;
            }
            return stb.toString();
        }
    }
}
