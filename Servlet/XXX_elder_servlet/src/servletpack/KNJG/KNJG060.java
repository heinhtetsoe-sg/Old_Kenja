// kanji=漢字
/*
 * $Id: ade224798e657a468553b02993de9c66f387a797 $
 *
 * 作成日: 2007/11/20 17:20:00 - JST
 * 作成者: nakada
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJG;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.SvfForm;

/**
 * 通学証明書
 * @author nakada
 * @version $Id: ade224798e657a468553b02993de9c66f387a797 $
 */
public class KNJG060 {
    /* pkg */static final Log log = LogFactory.getLog(KNJG060.class);

    private static final String SCHOOL_KIND_P = "P";
    private static final String SCHOOL_KIND_J = "J";
    private static final String SCHOOL_KIND_H = "H";

    /** 性別 */
    private static final String SEX_MAN = "1";
    private static final String SEX_WOMAN = "2";

    private Form _form;
    private Vrw32alp _svf = new Vrw32alp();

    private boolean _hasData;

    private Param _param;

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        _hasData = false;

        log.fatal("$Revision: 76806 $ $Date: 2020-09-12 00:49:44 +0900 (土, 12 9 2020) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);

        if (_svf.VrInit() < 0) {
            throw new IllegalStateException("svf初期化失敗");
        }
        _svf.VrSetSpoolFileStream(response.getOutputStream());
        response.setContentType("application/pdf");

        DB2UDB db2 = null;
        try {
            final String dbName = request.getParameter("DBNAME");
            db2 = new DB2UDB(dbName, "db2inst1", "db2inst1", DB2UDB.TYPE2);
            if (openDb(db2)) {
                return;
            }

            _param = new Param(request, db2);

            _form = new Form(_param, _svf);

            for (int i = 0; i < _param._certif.length; i++) {
                log.debug(">> 証明書連番 =" + _param._certif[i]);

                final Student student = Student.createStudent(db2, _param, _param._certif[i]);
                printMain(db2, student);
            }

        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (!_hasData) {
                _svf.VrSetForm("MES001.frm", 0);
                _svf.VrsOut("note", "note");
                _svf.VrEndPage();
            }
            
            if (null != _param) {
            	for (final Iterator<File> it = _param._flagFormfileMap.values().iterator(); it.hasNext();) {
            		final File file = it.next();
            		try {
            			file.delete();
            		} catch (Exception e) {
            			log.info("exception!", e);
            		}
            		it.remove();
            	}
            }

            final int ret = _svf.VrQuit();
            log.info("===> VrQuit():" + ret);
            closeDb(db2);
        }
    }

    private void printMain(final DB2UDB db2, final Student student) throws SQLException {
        /* № */
        _form._svf.VrsOut("CERTIF_NO1", student._certifIssueDat._certifNo);

        _form.printSchoolInfo(student);
        /* 氏名 */
        _form.printName(student);
        /* 年齢 */
        _form.printAge(_param._loginDate, student._birthday);
        /* 性別 */
        if (student._sex.equals(SEX_MAN)) {
            _form._svf.VrsOut("SEX", SEX_MAN);
        } else if (student._sex.equals(SEX_WOMAN)) {
            _form._svf.VrsOut("SEX", SEX_WOMAN);
        }

        /* 住所１ */
        _form.printAddr1(student);
        /* 住所２ */
        _form.printAddr2(student);
        /* 電話番号 */
        _form._svf.VrsOut("TELNO", student._schregAddressDat._telno);
        /* 課程 */
        String majorName = "";
        if(!StringUtils.isBlank(student._course._courseName)){
            majorName = student._course._courseName + "課程";
        }
        /* 学科 */
        if (null != student._major._majorName) {
            majorName = majorName + "　" + student._major._majorName;
        }

        if(!_param._freshmanFlg) {
            if (SCHOOL_KIND_J.equals(student._schregRegdDat._schoolKind) && (_param._isChiben || _param._isMatsudo)) {
                // 智辯の中学 または、専修松戸の中学は表示しない
                majorName = "";
            }
        }
        String majorNameField = 14 < majorName.length() ? "MAJORNAME1" : "MAJORNAME";
        _form._svf.VrsOut(majorNameField, majorName);
        /* 学年 */
        final String setAnnual = _param.isPrintGrade() ? student._schregRegdDat._grade : student._schregRegdDat._annual;
        _form._svf.VrsOut("ANNUAL", NumberUtils.isDigits(setAnnual) ?
                Integer.toString(Integer.parseInt(setAnnual)) : "");
        /* 証明書番号 */
        _form._svf.VrsOut("CERTIF_NO2", student._schregNo);

        if (!_param._dateBlankCheck){
            /* 有効期限 */
            _form._svf.VrsOut("MONTH_DIV", student._certifIssueDat._certifDetailEachtypeDat._remark6);
        }
        /* 発駅 */
        _form._svf.VrsOut("START_STATION", student._certifIssueDat._certifDetailEachtypeDat._remark1);
        /* 着駅 */
        _form._svf.VrsOut("END_STATION", student._certifIssueDat._certifDetailEachtypeDat._remark2);
        /* 経由駅 */
        _form._svf.VrsOut("BY_WAY_STATION", student._certifIssueDat._certifDetailEachtypeDat._remark3);
        String[] dates = null;
        if(!"".equals(student._certifIssueDat._certifDetailEachtypeDat._remark5)){
            dates = KNJ_EditDate.tate_format(getJDate(db2, student._certifIssueDat._certifDetailEachtypeDat._remark5));
            final String limitYear = (_param._isNagisa) ? student._certifIssueDat._certifDetailEachtypeDat._remark5.substring(0,4) : dates[1];
            if (!_param._dateBlankCheck){
                /* 通学証明書の有効期間 */
                _form._svf.VrsOut("LIMIT_YEAR", limitYear);
                _form._svf.VrsOut("LIMIT_MONTH", dates[2]);
                _form._svf.VrsOut("LIMIT_DAY", dates[3]);
            }
        }

        /* 証明欄・発行日付 */
        if(!"".equals(student._certifIssueDat._issueDate)){
            dates = KNJ_EditDate.tate_format(getJDate(db2, student._certifIssueDat._issueDate));
            final String issueYear = (_param._isNagisa) ? student._certifIssueDat._issueDate.substring(0,4) : dates[1];
            if(!_param._isNagisa) {
                _form._svf.VrsOut("ERA_NAME3", dates[0]);
            }
            _form._svf.VrsOut("ISSUE_YEAR", issueYear);
            _form._svf.VrsOut("ISSUE_MONTH", dates[2]);
            _form._svf.VrsOut("ISSUE_DAY", dates[3]);
        }

        /* 学校所在地 */
        if (null != student._certifSchoolDat._remark1) {
            if ("1".equals(_param._useAddrField2) && student._certifSchoolDat._remark1.length() > 50) {
                _form._svf.VrsOut("REMARK3", student._certifSchoolDat._remark1);
            } else if ("1".equals(_param._useAddrField2) && student._certifSchoolDat._remark1.length() > 30) {
                _form._svf.VrsOut("REMARK2", student._certifSchoolDat._remark1);
            } else {
                _form._svf.VrsOut("REMARK", student._certifSchoolDat._remark1);
            }
        }
        /* 学校名 */
        _form._svf.VrsOut("SCHOOLNAME", student._certifSchoolDat._schoolName);
        /* 職名 */
        _form._svf.VrsOut("JOBNAME", student._certifSchoolDat._jobName + "　");
        /* 校長名 */
        _form._svf.VrsOut("STAFFNAME", student._certifSchoolDat._principalName);
        if (_param._knjg060bPrintStamp){
            /* 学校印 */
            if (SCHOOL_KIND_J.equals(student._schregRegdDat._schoolKind)) {
                _form._svf.VrsOut("SCHOOL_STAMP", _param._stampFilePathJ);
            } else if (SCHOOL_KIND_H.equals(student._schregRegdDat._schoolKind)) {
                _form._svf.VrsOut("SCHOOL_STAMP", _param._stampFilePathH);
            }
        }

        if(!_param._isNagisa && !_param._dateBlankCheck) {
            List flist = new ArrayList();
            flist.add("ERA_NAME");
            flist.add("ERA_NAME2");
            putGengou2(db2, _svf, flist);
        }
        
        _form._svf.VrEndPage();
        _hasData = true;
    }

    private static String getJDate(final DB2UDB db2, String date) {
        try {
            final Calendar cal = KNJServletUtils.parseDate(date);
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH) + 1;
            int dom = cal.get(Calendar.DAY_OF_MONTH);


            return KNJ_EditDate.gengou(db2, year, month, dom);

        } catch (final Exception e) {
            return null;
        }
    }

    private static int mmToDot(final String mm) {
    	final BigDecimal dpi = new BigDecimal("400");
    	final BigDecimal mmPerInch = new BigDecimal("25.4");
    	final int dot = new BigDecimal(mm).multiply(dpi).divide(mmPerInch, 1, BigDecimal.ROUND_HALF_UP).intValue();
    	return dot;
    }

    private static BigDecimal dotToMm(final String dot) {
    	final BigDecimal dpi = new BigDecimal("400");
    	final BigDecimal mmPerInch = new BigDecimal("25.4");
    	final BigDecimal mm = new BigDecimal(dot).multiply(mmPerInch).divide(dpi, 1, BigDecimal.ROUND_HALF_UP);
    	return mm;
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
    
    private static String mkString(final List<String> list, final String comma) {
 	   final String last = "";
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

    private static int toInt(final String s, final int def) {
        return NumberUtils.isNumber(s) ? ((int) Double.parseDouble(s)) : def;
    }

    // ======================================================================
    private static class Param {
        private final String _year;
        private final String _semester;
        private final String _programId;
        private final String _dbName;
        private final String _loginDate;
        private final String[] _certif;            // 通学証明書番号
        private final String _type;                 // 証明書種別コード
        private final String _exeYear;              // 指示画面検索年度 (賢者では指示画面検索は無いのでyearを使用する)
        private final String _useAddrField2;
        private String _z010Yobi2;
        private boolean _isChiben;
        private boolean _isRisshi;
        private boolean _isNagisa;
        private boolean _isMatsudo;
        private final String _ticketRadio;

        private final boolean _freshmanFlg;  //新入生フラグ TRUE:新入生 FALSE:在籍

        private Map _prefMap;                       // 都道府県

        private final boolean _dateBlankCheck;  //有効期限を空欄出力する
        private final boolean _knjg060bPrintStamp; //通学証明書に印影を出力する

        private final String _documentroot;
        private final String _imagepath;
        private final String _stampFilePathJ;
        private final String _stampFilePathH;
        private String _stampSizeMm; // イメージサイズ(mm)
        private String _stampPositionXmm;
        private String _stampPositionYmm;

        private Map<String, File> _flagFormfileMap = new HashMap<String, File>();

        final KNJSchoolMst _schoolMst;

        public Param(final HttpServletRequest request, final DB2UDB db2)throws SQLException {
            final String year = request.getParameter("YEAR");
            //final String exeYear = request.getParameter("EXE_YEAR");

            _year = year;
            _semester = request.getParameter("SEMESTER");
            _programId = request.getParameter("PRGID");
            _dbName = request.getParameter("DBNAME");
            _loginDate = KNJ_EditDate.h_format_thi(request.getParameter("LOGIN_DATE"),0);
            _certif = request.getParameterValues("TUGAKU");
            _type = request.getParameter("TYPE");
            _exeYear = year;
            _useAddrField2 = request.getParameter("useAddrField2");

            final String schName = getZ010(db2, "NAME1");
            _isChiben = "CHIBEN".equals(schName);
            _isRisshi = "1".equals(StringUtils.defaultString(request.getParameter("isRisshi"), ""));
            _isNagisa = "nagisa".equals(schName);
            _isMatsudo = "matsudo".equals(schName);

            //立志社の時だけチェック。立志以外/NULLならこれまで通りの指定("1")となる。
            if (_isRisshi) {
                _ticketRadio = "".equals(StringUtils.defaultString(request.getParameter("TICKET_RADIO"), "")) ? "1" : request.getParameter("TICKET_RADIO");
            } else {
                _ticketRadio = "1";
            }

            _freshmanFlg = "1".equals(request.getParameter("STUDENT_RADIO")) ? true : false;
            _dateBlankCheck = "1".equals(StringUtils.defaultString(request.getParameter("DATE_BLANK_CHECK"), "")) ? true : false;
            _knjg060bPrintStamp = "1".equals(StringUtils.defaultString(request.getParameter("knjg060bPrintStamp"), "")) ? true : false;

            _documentroot = request.getParameter("DOCUMENTROOT");
            KNJ_Control imagepath_extension = new KNJ_Control();                //取得クラスのインスタンス作成
            KNJ_Control.ReturnVal returnval = imagepath_extension.Control(db2);
            _imagepath = returnval.val4;                                        //写真データ格納フォルダ
            _stampFilePathJ = getImageFilePath("SCHOOLSTAMP_J.bmp"); //高校印
            _stampFilePathH = getImageFilePath("SCHOOLSTAMP_H.bmp"); //中学印
            _stampSizeMm = request.getParameter("knjg060StampSizeMm");
            if (!NumberUtils.isNumber(_stampSizeMm)) {
            	_stampSizeMm = request.getParameter("stampSizeMm");
            }
        	_stampPositionXmm = request.getParameter("knjg060StampPositionXmm");
        	_stampPositionYmm = request.getParameter("knjg060StampPositionYmm");

            _prefMap = getPrefMst(db2);
            _z010Yobi2 = getZ010(db2, "NAMESPARE2");
            
            _schoolMst = new KNJSchoolMst(db2, _year);
        }

        public boolean isPrintGrade() {
            return !StringUtils.isEmpty(_z010Yobi2) || "0".equals(_schoolMst._schoolDiv);
        }

        public String _prefMapString(String pref) {
            return (String) _prefMap.get(pref) != null ? (String) _prefMap.get(pref) : "";
        }

        private Map getPrefMst(final DB2UDB db2) throws SQLException {
            final String sql = sqlPrefMst();
            final Map rtn = new HashMap();

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String code = rs.getString("code");
                    final String name = rs.getString("name");
                    rtn.put(code, name);
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            return rtn;
        }

        private String sqlPrefMst() {
            return " select"
            + "    PREF_CD as code,"
            + "    PREF_NAME as name"
            + " from"
            + "    PREF_MST"
            + " order by PREF_CD";
        }

        private String getZ010(final DB2UDB db2, final String field) throws SQLException {
            final String sql = sqlGetZ010yobi2();
            String rtn = "";

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = StringUtils.defaultString(rs.getString(field));
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            return rtn;
        }

        private String sqlGetZ010yobi2() {
            return " select"
            + "    NAME1, NAMESPARE2"
            + " from"
            + "    NAME_MST"
            + " where"
            + "    NAMECD1 = 'Z010'"
            + "    AND NAMECD2 = '00'";
        }

        /**
         * 写真データファイルの取得
         */
        private String getImageFilePath(final String filename) {
            if (null == _documentroot || null == _imagepath || null == filename) {
                return null;
            } // DOCUMENTROOT
            final StringBuffer path = new StringBuffer();
            path.append(_documentroot).append("/").append(_imagepath).append("/").append(filename);
            final File file = new File(path.toString());
            if (!file.exists()) {
                log.warn("画像ファイル無し:" + path);
                return null;
            } // 写真データ存在チェック用
            return path.toString();
        }
    }

    // ======================================================================

    private static class Form {
    	private Param _param;
        private Vrw32alp _svf;

        public Form(final Param param, final Vrw32alp svf) throws IOException {
        	_param = param;
        	
            String formname = null;
            if ("2".equals(param._ticketRadio)) {
            	formname = "KNJG060_2.frm";
            } else {
            	formname = "KNJG060.frm";
            }

            _svf = svf;
            formname = setConfigForm(param, formname);

            _svf.VrSetForm(formname, 1);
        }
        
		public void printSchoolInfo(final Student student) {
            String schoolType = "";
            String schoolDiv = "";
            if (SCHOOL_KIND_P.equals(student._schregRegdDat._schoolKind)) {
                schoolType = "小学校";
                schoolDiv = "義務課程";
            } else if (SCHOOL_KIND_J.equals(student._schregRegdDat._schoolKind)) {
                schoolType = "中学校";
                schoolDiv = "義務課程";
            } else if (SCHOOL_KIND_H.equals(student._schregRegdDat._schoolKind)) {
                schoolType = "高等学校";
                schoolDiv = "高等課程";
            }
            _svf.VrsOut("SCHOOL_TYPE", schoolType);
            _svf.VrsOut("SCHOOL_DIV", schoolDiv);
        }

        public void printAge(String date, String birthday) {
            _svf.VrsOut("AGE", Integer.toString(getAge(date, birthday)));
        }

        public int getAge(String date, String birthday) {
            final Calendar nowCal;
            try {
                final String dateStr = date;
				nowCal = parseDate(dateStr, "yyyy/MM/dd");
                int nowYear = nowCal.get(Calendar.YEAR);
                int nowMonth = nowCal.get(Calendar.MONTH) + 1;
                int nowDay = nowCal.get(Calendar.DATE);

                if (birthday == null) {
                    return 0;
                }

                final Calendar cal = KNJServletUtils.parseDate(birthday);
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH) + 1;
                int day = cal.get(Calendar.DATE);

                 return Integer.parseInt(""+(nowYear-year -
                  ((month > nowMonth || (month == nowMonth &&
                          day > nowDay)) ? 1:0)));
            } catch (ParseException e) {
                return 0;
            }
        }

        private Calendar parseDate(final String dateStr, final String pattern) throws ParseException {
            // 文字列を Date型に
            final SimpleDateFormat format = new SimpleDateFormat(pattern);
            final Date date = format.parse(dateStr);

            // Date型を Calendar に
            final Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            return cal;
        }

        public void printName(Student student) {
        	final int NAME1_LENG = 34;
            String name = student._name;

            if (name != null) {
                final String label;
                if (name.length() <= NAME1_LENG) {
                    label = "NAME1";
                } else {
                    label = "NAME2";
                }
                _svf.VrsOut(label, name);
            }
        }

        public void printAddr1(Student student) {
            /** 住所１ */
            final int ADD1_LENG = 50;

            String addr1 = student._schregAddressDat._addr1;
            String addr2 = student._schregAddressDat._addr2;

            log.debug(" strictSize1 = " + strictSize(addr1) + " (" + (addr1 == null ? 0 : addr1.length() * 2) + ")");
            log.debug(" strictSize2 = " + strictSize(addr2) + " (" + (addr2 == null ? 0 : addr2.length() * 2) + ")");

            String addr = (addr1 == null ? "" : addr1) + (addr2 == null ? "" : addr2);

            if (strictSize(addr1) + strictSize(addr2) < ADD1_LENG) {
                // 住所1＋住所2＝が25文字以内なら、中央表示
                _svf.VrsOut("ADDRESS1_1", addr);
            } else if (addr2 == null) {
                // 住所2がnullのとき
                if (strictSize(addr1) <= ADD1_LENG) {
                    // 住所1が25文字以内で中央表示
                    _svf.VrsOut("ADDRESS1_1", addr1 == null ? "" : addr1);
                } else {
                    // 住所1が25文字超えるとき、住所1＋住所2で、上下で表示
                    int pos = limitoverIndex(addr, ADD1_LENG);
                    String a1 = addr.substring(0, pos);
                    String a2 = addr.substring(pos);

                    _svf.VrsOut("ADDRESS1_2", a1);
                    _svf.VrsOut("ADDRESS1_3", a2);
                }
            } else {
                // 上記以外のときは、 住所1は上段、住所2は下段（リンクではない）
                if ("1".equals(_param._useAddrField2) &&
                        (strictSize(addr1) > 50 || strictSize(addr2) > 50)
                        ) {
                    _svf.VrsOut("ADDRESS3_1", addr1 == null ? "" : addr1);
                    _svf.VrsOut("ADDRESS3_2", addr2 == null ? "" : addr2);
                } else {
                    _svf.VrsOut("ADDRESS1_2", addr1 == null ? "" : addr1);
                    _svf.VrsOut("ADDRESS1_3", addr2 == null ? "" : addr2);
                }
            }
        }

        /**
         * 文字のバイト数を得る
         */
        private int getCharByte(char c) {
        	return KNJ_EditEdit.getMS932ByteLength(String.valueOf(c));
        }

        /**
         * 文字列のサイズを得る
         */
        private int strictSize(String str) {
        	return KNJ_EditEdit.getMS932ByteLength(str);
        }

        /**
         * リミットを越えるインデクスを得る
         */
        private int limitoverIndex(String str, int limit) {
            if (str == null) {
                return 0;
            }

            int size = 0;
            for (int i = 0; i < str.length(); i++) {
                size += getCharByte(str.charAt(i));
                if (size > limit) {
                    return i;
                }
            }
            return str.length() - 1;
        }

        public void printAddr2(Student student) {
            _svf.VrsOut("ADDRESS2", "");
        }
        
        // フォーム修正
		public String setConfigForm(final Param param, String formname) {
        	final File formFile = new File(_svf.getPath(formname));
        	if (!formFile.exists()) {
        		log.warn("no file : " + formname);
        		return formname;
        	}
        	
        	final TreeMap<String, String> flags = getFlagMap(param);
        	if (flags.isEmpty()) {
        		return formname;
        	}
        	final String flag = formname + ":" + mkString(flags, "|");
        	log.info(" form modify flag = " + flag);
        	if (!param._flagFormfileMap.containsKey(flag)) {
        		final SvfForm svfForm = new SvfForm(formFile);
        		if (svfForm.readFile()) {
        			
        			File file = null;
        			try {
            			modifyForm(param, flags, svfForm);
        				file = svfForm.writeTempFile();
        			} catch (Exception e) {
        				log.error("exception!", e);
        			}
        			param._flagFormfileMap.put(flag, file);
        		}
        	}
        	final File file = param._flagFormfileMap.get(flag);
        	if (null != file) {
        		formname = file.getName();
        	}
        	return formname;
        }

        private static final String FLG_RESIZE_STAMP = "FLG_RESIZE_STAMP";
		private TreeMap<String, String> getFlagMap(final Param param) {
			final TreeMap<String, String> flags = new TreeMap<String, String>();
			if (NumberUtils.isNumber(param._stampSizeMm) || NumberUtils.isNumber(param._stampPositionXmm) || NumberUtils.isNumber(param._stampPositionYmm)) {
				flags.put(FLG_RESIZE_STAMP, "1");
			}
			return flags;
		}

        private void modifyForm(final Param param, final Map<String, String> flags, final SvfForm svfForm) {
        	if (flags.containsKey(FLG_RESIZE_STAMP)) {
        		resizeStampImage(param, svfForm);
        	}
        }

		private void resizeStampImage(final Param param, final SvfForm svfForm) {
			final String fieldname = "SCHOOL_STAMP";
			final SvfForm.ImageField image = svfForm.getImageField(fieldname);
			if (null == image) {
				log.info(" no image : " + fieldname);
				return;
			}
			
			final int x = image._point._x;
			final int y = image._point._y;
			final int endX = image._endX;
			final int endY = y + image._height;
			final int l = NumberUtils.isNumber(param._stampSizeMm) ? mmToDot(param._stampSizeMm) : image._height;
			final int newX;
			final int newEndX;
			if (NumberUtils.isNumber(param._stampPositionXmm)) {
				newX = mmToDot(param._stampPositionXmm);
				newEndX = newX + l;
			} else {
				final int centerX = (x + endX) / 2;
				newX = centerX - l / 2;
				newEndX = centerX + l / 2;
			}
			final int newY;
			if (NumberUtils.isNumber(param._stampPositionYmm)) {
				newY = mmToDot(param._stampPositionYmm);
			} else {
				final int centerY = (y + endY) / 2;
				newY = centerY - l / 2;
			}
			final int newHeight = l;
			
			final SvfForm.ImageField newImage = image.setFieldname(fieldname).setX(newX).setY(newY).setEndX(newEndX).setHeight(newHeight);
			svfForm.removeImageField(image);
			svfForm.addImageField(newImage);
			final BigDecimal xmm = dotToMm(String.valueOf(x));
			final BigDecimal ymm = dotToMm(String.valueOf(y));
			final BigDecimal hmm = dotToMm(String.valueOf(image._height));
			final BigDecimal newXmm = dotToMm(String.valueOf(newX));
			final BigDecimal newYmm = dotToMm(String.valueOf(newY));
			final BigDecimal newHmm = dotToMm(String.valueOf(l));
			log.info("move stamp (x=" + x + "(" + xmm + "mm), y=" + y + "(" + ymm + "mm), len = " + image._height + "(" + hmm + "mm)) ");
			log.info("        to (x=" + newX + "(" + newXmm + "mm), y=" + newY + "(" + newYmm + "mm), len = " + l + "(" + newHmm + "mm))");
		}
    }

    // ======================================================================
    /**
     * 学籍。学籍基礎マスタ。
     */
    private static class Student {
        private final String _schregNo;         // 学籍番号
        private final String _name;             // 氏名
        private final String _birthday;         // 生年月日
        private final String _sex;              // 性別

        private SchregRegdDat _schregRegdDat;    // 生徒。学籍在籍データ
        private Course _course;                  // 課程データ
        private Major _major;                    // 学科データ
        private SchregAddressDat _schregAddressDat; // 生徒。学籍住所データ
        private CertifSchoolDat _certifSchoolDat;   // 証明書学校データ
        private CertifIssueDat _certifIssueDat;

        Student(final String schregNo,
                final String name,
                final String birthday,
                final String sex
        ) {
            _schregNo = schregNo;
            _name = name;
            _birthday = birthday;
            _sex = sex;
        }

        public void load(DB2UDB db2, final Param param, CertifIssueDat cid) throws SQLException, Exception {
            _schregRegdDat = SchregRegdDat.createSourseCodeDat(db2, param, param._exeYear, param._semester, _schregNo);
            _major = Major.createMajorDat(db2, _schregRegdDat._courseCd, _schregRegdDat._majorCd);
            _course = Course.createCourseDat(db2, _schregRegdDat._courseCd);
            _schregAddressDat = SchregAddressDat.createSchregAddressDat(db2, param, _schregNo);
            _certifIssueDat = cid;
            _certifSchoolDat = CertifSchoolDat.createSchoolDat(db2, param._exeYear, _certifIssueDat._certifKindCd);
        }
        
        private static Student createStudent(final DB2UDB db2, final Param param, final String certifIndex)
        		throws SQLException, Exception {
        	
        	PreparedStatement ps = null;
        	ResultSet rs = null;
        	
        	try {
        		CertifIssueDat cid = CertifIssueDat.createCertifIssueDat(db2, param, certifIndex);

        		final String sql;
        		if(param._freshmanFlg) {
        			sql = sqlFreshmanStudents(cid._schregNo);
        		}else {
        			sql = sqlStudents(cid._schregNo);
        		}
        		ps = db2.prepareStatement(sql);
        		rs = ps.executeQuery();
        		while (rs.next()) {
        			final String schregNo = rs.getString("schregNo");
        			final String name = param._freshmanFlg ? rs.getString("name") :"1".equals(rs.getString("use_real_name")) ? rs.getString("real_name") : rs.getString("name");
        			final String birthday = rs.getString("birthday");
        			final String sex = rs.getString("sex");
        			
        			final Student studentDat = new Student(
        					schregNo,
        					name,
        					birthday,
        					sex
        					);
        			
        			studentDat.load(db2, param, cid);
        			return studentDat;
        		}
        	} finally {
        		db2.commit();
        		DbUtils.closeQuietly(null, ps, rs);
        	}
        	
        	log.debug(">>>SCHREG_BASE_MST に該当するものがありません。");
        	throw new Exception();
        }
        
        private static String sqlStudents(String schregno) {
        	return " select"
        			+ "    T1.SCHREGNO as schregNo,"
        			+ "    T1.NAME as name,"
        			+ "    T1.REAL_NAME as real_name,"
        			+ "    (CASE WHEN L1.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END) AS use_real_name,"
        			+ "    T1.BIRTHDAY as birthday,"
        			+ "    T1.SEX as sex"
        			+ " from"
        			+ "    SCHREG_BASE_MST T1"
        			+ " left join "
        			+ "    SCHREG_NAME_SETUP_DAT L1 ON L1.SCHREGNO = T1.SCHREGNO AND L1.DIV = '01' "
        			+ " where"
        			+ "    T1.SCHREGNO = '" + schregno + "'";
        }
        
        private static String sqlFreshmanStudents(String schregno) {
        	return " select"
        			+ "    T1.SCHREGNO as schregNo,"
        			+ "    T1.NAME as name,"
        			+ "    T1.BIRTHDAY as birthday,"
        			+ "    T1.SEX as sex"
        			+ " from"
        			+ "    FRESHMAN_DAT T1"
        			+ " where"
        			+ "    T1.SCHREGNO = '" + schregno + "'";
        }
    }

    // ======================================================================
    /**
     * 生徒。学籍在籍データ。
     */
    private static class SchregRegdDat {
        private final String _grade;        // 年次
        private final String _schoolKind;        // 年次
        private final String _annual;        // 年次
        private final String _courseCd;      // 課程コード
        private final String _majorCd;       // 学科コード

        SchregRegdDat(
                final String grade,
                final String schoolKind,
                final String annual,
                final String courseCd,
                final String majorCd
        ) {
            _grade = grade;
            _schoolKind = schoolKind;
            _annual = annual;
            _courseCd = courseCd;
            _majorCd = majorCd;
        }
        
        public static SchregRegdDat createSourseCodeDat(DB2UDB db2, final Param param, String YEAR, String SEMESTER, String SCHREGNO) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            if(param._freshmanFlg) {
                ps = db2.prepareStatement(sqlFreshmanSchregRegdDat(YEAR, SCHREGNO));
            }else {
                ps = db2.prepareStatement(sqlSchregRegdDat(YEAR, SEMESTER, SCHREGNO));
            }
            rs = ps.executeQuery();
            while (rs.next()) {
                final String grade = rs.getString("GRADE");
                final String schoolKind = rs.getString("SCHOOL_KIND");
                final String annual = rs.getString("annual");
                final String courseCd = rs.getString("courseCd");
                final String majorCd = rs.getString("majorCd");

                final SchregRegdDat schregRegdDat = new SchregRegdDat(
                        grade,
                        schoolKind,
                        annual,
                        courseCd,
                        majorCd
                );
                return schregRegdDat;
            }
            return new SchregRegdDat("", "", "", "", "");
        }

        private static String sqlSchregRegdDat(String year, String semester, String schregNo) {
            return " select"
                    + "    L1.GRADE_CD AS GRADE,"
                    + "    L1.SCHOOL_KIND,"
                    + "    T1.ANNUAL as annual,"
                    + "    T1.COURSECD as courseCd,"
                    + "    T1.MAJORCD as majorCd"
                    + " from"
                    + "    SCHREG_REGD_DAT T1 "
                    + "    LEFT JOIN SCHREG_REGD_GDAT L1 ON L1.YEAR = T1.YEAR "
                    + "         AND L1.GRADE = T1.GRADE "
                    + " where"
                    + "    T1.SCHREGNO = '" + schregNo + "' and"
                    + "    T1.YEAR = '" + year + "' and"
                    + "    T1.SEMESTER = '" + semester + "'"
                    ;
        }

        private static String sqlFreshmanSchregRegdDat(String year, String schregNo) {
            return " select"
            		+ "    L1.GRADE_CD AS GRADE,"
            		+ "    L1.SCHOOL_KIND,"
                    + "    T1.GRADE as annual,"
                    + "    T1.COURSECD as courseCd,"
                    + "    T1.MAJORCD as majorCd"
                    + " from"
                    + "    FRESHMAN_DAT T1 "
                    + "    LEFT JOIN SCHREG_REGD_GDAT L1 ON L1.YEAR = T1.ENTERYEAR "
                    + "         AND L1.GRADE = T1.GRADE "
                    + " where"
                    + "    T1.SCHREGNO = '" + schregNo + "' and"
                    + "    T1.ENTERYEAR = '" + year + "' "
                    ;
        }
    }

    // ======================================================================
    /**
     * 生徒。学籍住所データ。
     */
    private static class SchregAddressDat {
        private final String _zipcd; // 郵便番号
        private final String _addr1; // 住所１
        private final String _addr2; // 住所２
        private final String _telno; // 電話番号

        SchregAddressDat(
                final String zipcd,
                final String addr1,
                final String addr2,
                final String telno
        ) {
            _zipcd = zipcd;
            _addr1 = addr1;
            _addr2 = addr2;
            _telno = telno;
        }
        
        private static SchregAddressDat createSchregAddressDat(DB2UDB db2, final Param param, String schregno)
        		throws SQLException {
        	
        	PreparedStatement ps = null;
        	ResultSet rs = null;
        	if(param._freshmanFlg) {
        		ps = db2.prepareStatement(sqlFreshmanSchregAddressDat(schregno));
        	}else {
        		ps = db2.prepareStatement(sqlSchregAddressDat(schregno));
        	}
        	
        	rs = ps.executeQuery();
        	while (rs.next()) {
        		final String zipcd = rs.getString("zipcd");
        		final String addr1 = rs.getString("addr1");
        		final String addr2 = rs.getString("addr2");
        		final String telno = rs.getString("telno");
        		
        		final SchregAddressDat studentSchregAddressDat = new SchregAddressDat(
        				zipcd,
        				addr1,
        				addr2,
        				telno
        				);
        		return studentSchregAddressDat;
        	}
        	return new SchregAddressDat("", "", "", "");
        }
        
        private static String sqlSchregAddressDat(String schregno) {
        	return " select"
        			+ "    ZIPCD as zipcd,"
        			+ "    ADDR1 as addr1,"
        			+ "    ADDR2 as addr2,"
        			+ "    TELNO as telno"
        			+ " from"
        			+ "    SCHREG_ADDRESS_DAT"
        			+ " where"
        			+ "    SCHREGNO = '" + schregno + "'"
        			+ " order by ISSUEDATE DESC";
        }
        
        private static String sqlFreshmanSchregAddressDat(String schregno) {
        	return " select"
        			+ "    ZIPCD as zipcd,"
        			+ "    ADDR1 as addr1,"
        			+ "    ADDR2 as addr2,"
        			+ "    TELNO as telno"
        			+ " from"
        			+ "    FRESHMAN_DAT"
        			+ " where"
        			+ "    SCHREGNO = '" + schregno + "'"
        			+ " order by ISSUEDATE DESC";
        }
    }

    // ======================================================================
    /**
     * 証明書学校データ。
     */
    private static class CertifSchoolDat {
        private final String _remark1;      // 学校所在地
        private final String _schoolName;       // 学校名
        private final String _jobName;          // 役職名
        private final String _principalName;    // 校長名

        CertifSchoolDat(
                final String remark1,
                final String schoolName,
                final String jobName,
                final String principalName
        ) {
            _remark1 = remark1;
            _schoolName = schoolName;
            _jobName = jobName;
            _principalName = principalName;
        }
        
        private static CertifSchoolDat createSchoolDat(DB2UDB db2, String year, String certifKind)
        		throws SQLException {
        	
        	PreparedStatement ps = null;
        	ResultSet rs = null;
        	final String sql = sqlCertifSchoolDat(year, certifKind);
        	ps = db2.prepareStatement(sql);
        	rs = ps.executeQuery();
        	while (rs.next()) {
        		final String remark1 = StringUtils.defaultString(rs.getString("remark1"));
        		final String schoolName = StringUtils.defaultString(rs.getString("schoolName"));
        		final String jobName = StringUtils.defaultString(rs.getString("jobName"));
        		final String principalName = StringUtils.defaultString(rs.getString("principalName"));
        		
        		final CertifSchoolDat certifSchoolDat = new CertifSchoolDat(
        				remark1,
        				schoolName,
        				jobName,
        				principalName
        				);
        		return certifSchoolDat;
        	}
        	return new CertifSchoolDat("", "", "", "");
        }
        
        private static String sqlCertifSchoolDat(String year, String certifKind) {
        	return " select"
        			+ "    REMARK1 as remark1,"
        			+ "    SCHOOL_NAME as schoolName,"
        			+ "    JOB_NAME as jobName,"
        			+ "    PRINCIPAL_NAME as principalName"
        			+ " from"
        			+ "    CERTIF_SCHOOL_DAT"
        			+ " where"
        			+ "    YEAR = '" + year + "' and"
        			+ "    CERTIF_KINDCD = '" + certifKind + "'"
        			;
        }
    }

    // ======================================================================
    /**
     * 課程データ。
     */
    private static class Course {
        private final String _courseName;

        Course(
                final String courseName
        ) {
            _courseName = courseName;
        }
        
        public static Course createCourseDat(DB2UDB db2, String courseCd)
        		throws SQLException {
        	PreparedStatement ps = null;
        	ResultSet rs = null;
        	
        	ps = db2.prepareStatement(sqlCourseDat(courseCd));
        	rs = ps.executeQuery();
        	while (rs.next()) {
        		final String name = StringUtils.defaultString(rs.getString("name"));
        		
        		final Course course = new Course(
        				name
        				);
        		return course;
        	}
        	return new Course("");
        }
        
        private static String sqlCourseDat(String coursecd) {
        	return " select"
        			+ "    COURSENAME as name"
        			+ " from"
        			+ "    COURSE_MST"
        			+ " where"
        			+ "    COURSECD = '" + coursecd + "' "
        			;
        }
    }


    /**
     * 学科データ。
     */
    private static class Major {
        private final String _majorName;

        Major(
                final String majorName
        ) {
            _majorName = majorName;
        }
        
        public static Major createMajorDat(DB2UDB db2, String courseCd, String majorCd)
        		throws SQLException {
        	PreparedStatement ps = null;
        	ResultSet rs = null;
        	
        	ps = db2.prepareStatement(sqlMajorDat(courseCd, majorCd));
        	rs = ps.executeQuery();
        	while (rs.next()) {
        		final String name = rs.getString("name");
        		
        		final Major major = new Major(
        				name
        				);
        		return major;
        	}
        	return new Major("");
        }
        
        private static String sqlMajorDat(String coursecd, String majorCd) {
        	return " select"
        			+ "    MAJORNAME as name"
        			+ " from"
        			+ "    MAJOR_MST"
        			+ " where"
        			+ "    COURSECD = '" + coursecd + "' and"
        			+ "    MAJORCD = '" + majorCd + "'"
        			;
        }
    }

    private boolean openDb(final DB2UDB db2) {
        try {
            db2.open();
        } catch (final Exception ex) {
            log.error("db open error!", ex);
            return true;
        }
        return false;
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    // ======================================================================
    /**
     * 証明書種別別発行データ。
     */
    private static class CertifIssueDat {
        private final String _schregNo;     // 学籍番号
        private final String _certifKindCd; // 証明書種類コード
        private final String _issueDate;    // 証明書発行日付
        private final String _certifNo;     // 証明書番号

        private CertifDetailEachtypeDat _certifDetailEachtypeDat;

        CertifIssueDat(
                final String schregNo,
                final String certifKindCd,
                final String issueDate,
                final String certifNo
        ) {
            _schregNo = schregNo;
            _certifKindCd = certifKindCd;
            _issueDate = issueDate;
            _certifNo = certifNo;
        }
        
        private static CertifIssueDat createCertifIssueDat(DB2UDB db2, final Param param, String pCertifIndex)
        		throws SQLException, Exception {
        	
        	final String sql = sqlCertifIssueDat(param, pCertifIndex);
        	
        	PreparedStatement ps = null;
        	ResultSet rs = null;
        	
        	ps = db2.prepareStatement(sql);
        	rs = ps.executeQuery();
        	while (rs.next()) {
        		final String schregNo = StringUtils.defaultString(rs.getString("schregNo"));
        		final String certifKindCd = StringUtils.defaultString(rs.getString("certifKindCd"));
        		final String issueDate = StringUtils.defaultString(rs.getString("issueDate"));
        		final String certifNo = StringUtils.defaultString(rs.getString("certifNo"));
        		
        		final CertifIssueDat certifIssueDat = new CertifIssueDat(
        				schregNo,
        				certifKindCd,
        				issueDate,
        				certifNo
        				);
        		certifIssueDat._certifDetailEachtypeDat = CertifDetailEachtypeDat.createCertifDetailEachtypeDat(db2, param, pCertifIndex);
        		return certifIssueDat;
        	}
        	
        	log.debug(">>>CERTIF_ISSUE_DAT に該当するものがありません。");
        	throw new Exception();
        }
        
        private static String sqlCertifIssueDat(final Param param, String certifIndex) {
        	return " select"
        			+ "    SCHREGNO as schregNo,"
        			+ "    CERTIF_KINDCD as certifKindCd,"
        			+ "    ISSUEDATE as issueDate,"
        			+ "    CERTIF_NO as certifNo"
        			+ " from"
        			+ "    CERTIF_ISSUE_DAT"
        			+ " where"
        			+ "    YEAR = '" + param._exeYear + "' and"
        			+ "    CERTIF_INDEX = '" + certifIndex + "'"
        			;
        }
    }

    // ======================================================================
    /**
     * 証明書種別別詳細データ。
     */
    private static class CertifDetailEachtypeDat {
        private final String _schregNo;     // 学籍番号
        private final String _type;         // 証明書種別
        private final String _remark1;      // 備考1
        private final String _remark2;      // 備考2
        private final String _remark3;      // 備考3
        private final String _remark4;      // 備考4
        private final String _remark5;      // 備考5
        private final String _remark6;      // 備考6
        private final String _remark7;      // 備考7

        CertifDetailEachtypeDat(
                final String schregNo,
                final String type,
                final String remark1,
                final String remark2,
                final String remark3,
                final String remark4,
                final String remark5,
                final String remark6,
                final String remark7
        ) {
            _schregNo = schregNo;
            _type = type;
            _remark1 = remark1;
            _remark2 = remark2;
            _remark3 = remark3;
            _remark4 = remark4;
            _remark5 = remark5;
            _remark6 = remark6;
            _remark7 = remark7;
        }
        
        private static CertifDetailEachtypeDat createCertifDetailEachtypeDat(DB2UDB db2, final Param _param, String pCertifIndex)
        		throws SQLException, Exception {
        	
        	final String sql = sqlCertifDetailEachtypeDat(_param, pCertifIndex);
        	
        	PreparedStatement ps = null;
        	ResultSet rs = null;
        	
        	ps = db2.prepareStatement(sql);
        	rs = ps.executeQuery();
        	while (rs.next()) {
        		final String schregNo = rs.getString("schregNo");
        		final String type = rs.getString("type");
        		final String remark1 = StringUtils.defaultString(rs.getString("remark1"));
        		final String remark2 = StringUtils.defaultString(rs.getString("remark2"));
        		final String remark3 = StringUtils.defaultString(rs.getString("remark3"));
        		final String remark4 = StringUtils.defaultString(rs.getString("remark4"));
        		final String remark5 = StringUtils.defaultString(rs.getString("remark5"));
        		final String remark6 = StringUtils.defaultString(rs.getString("remark6"));
        		final String remark7 = StringUtils.defaultString(rs.getString("remark7"));
        		
        		final CertifDetailEachtypeDat certifDetailEachtypeDat = new CertifDetailEachtypeDat(
        				schregNo,
        				type,
        				remark1,
        				remark2,
        				remark3,
        				remark4,
        				remark5,
        				remark6,
        				remark7
        				);
        		return certifDetailEachtypeDat;
        	}
        	
        	log.debug(">>>CERTIF_DETAIL_EACHTYPE_DAT に該当するものがありません。");
        	throw new Exception();
        }
        
        private static String sqlCertifDetailEachtypeDat(final Param _param, String certifIndex) {
        	return " select"
        			+ "    SCHREGNO as schregNo,"
        			+ "    TYPE as type,"
        			+ "    REMARK1 as remark1,"
        			+ "    REMARK2 as remark2,"
        			+ "    REMARK3 as remark3,"
        			+ "    REMARK4 as remark4,"
        			+ "    REMARK5 as remark5,"
        			+ "    REMARK6 as remark6,"
        			+ "    REMARK7 as remark7"
        			+ " from"
        			+ "    CERTIF_DETAIL_EACHTYPE_DAT"
        			+ " where"
        			+ "    YEAR = '" + _param._exeYear + "' and"
        			+ "    CERTIF_INDEX = '" + certifIndex + "'"
        			;
        }
    }

    private void putGengou2(final DB2UDB db2, final Vrw32alp svf, final List fieldList) {
        //元号(記入項目用)
        String[] dwk;
        if (_param._loginDate.indexOf('/') >= 0) {
            dwk = StringUtils.split(_param._loginDate, '/');
        } else if (_param._loginDate.indexOf('-') >= 0) {
            dwk = StringUtils.split(_param._loginDate, '-');
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

} // KNJG060

// eof
