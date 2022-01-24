// kanji=漢字
/*
 * $Id: AccumulateOptions.java 75473 2020-07-16 07:19:19Z maeshiro $
 *
 * 作成日: 2006/09/22 14:57:43 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.accumulate.option;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import jp.co.alp.kenja.batch.KenjaBatchContext;
import jp.co.alp.kenja.batch.domain.Term;
import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.KenjaDateImpl;
import jp.co.alp.kenja.common.domain.Semester;
import jp.co.alp.kenja.common.util.KenjaCommandLineParameters;
import jp.co.alp.kenja.common.util.KenjaParameters;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 累積データ一括生成のオプション。
 * @author takaesu
 * @version $Id: AccumulateOptions.java 75473 2020-07-16 07:19:19Z maeshiro $
 */
public class AccumulateOptions implements KenjaBatchContext {
    /** 日付オプションのキーワード。 */
    public static final String DATE = "date";

    /** 基準日オプションのキーワード。 */
    public static final String BASE_DAY = "baseday";

    /** 処理単位オプションのキーワード。*/
    public static final String UNIT = "execute.unit";

    /** 校種オプションのキーワード。*/
    public static final String SCHOOL_KIND = "school_kind";

    /** デバッグトレースする生徒の学籍番号 */
    public static final String SCHREGNO = "trace.schregno";

    /** ATTEND_DAY_DATの更新REGISTERCD
     * 指定のREGISTERCD以外はDELETE・INSERTしない。
     */
    public static final String ATTEND_DAY_DAT_UPDATE_REGISTERCD = "attend_day_dat.update_registercd";

    /** 実行の有無 */
    public static final String DO_RUN = "doRun";

    /*pkg*/static final Log log = LogFactory.getLog(AccumulateOptions.class);

    private final KenjaParameters _kenjaParam;
    private final ExecuteUnit _unit;
    /** 対象校種 */
    private final String[] _schoolKinds;
    /** 更新対象校種 */
    private final String[] _enabledSchoolKinds;
    /** 更新対象REGISTERCD */
    private final String[] _attendDayDatUpdateRegisterCd;

    /** 実施日付 */
    private final KenjaDateImpl _date;

    /** 基準日 */
    private final BaseDay _baseDay;

    private final Tracer _tracer;
    private final boolean _doRun;

    private final Properties _properties;

    private ControlMaster _cm;

    /**
     * コンストラクタ。
     * @param args 引数
     * @param properties プロパティ
     */
    public AccumulateOptions(final String[] args, final Properties properties) {
        final KenjaParameters kenjaParam = new KenjaCommandLineParameters(args);
        kenjaParam.validate();
        log.debug("パラメータ=" + kenjaParam);

        _date = validateDate(kenjaParam.getParameter(DATE));
        _baseDay = validateBaseDay(kenjaParam.getParameter(BASE_DAY));
        _kenjaParam = kenjaParam;
        _properties = properties;
        log.debug("プロパティファイルの項目=" + properties);
        _unit = new ExecuteUnit(properties.getProperty(UNIT, "semester"));
        _schoolKinds = validateSchoolKinds(properties.getProperty(SCHOOL_KIND));
        _enabledSchoolKinds = validateEnabledSchoolKinds(_schoolKinds);
        _attendDayDatUpdateRegisterCd = validateAttendDayDatUpdateRegistercd(properties.getProperty(ATTEND_DAY_DAT_UPDATE_REGISTERCD));
        _tracer = new Tracer(properties.getProperty(SCHREGNO, null));
        _doRun = BooleanUtils.toBoolean(properties.getProperty(DO_RUN));

        final String registerCd = _kenjaParam.getStaffCd();
        log.info("登録者コード(REGISTERCD/STAFFCD)=" + registerCd + ", length=" + registerCd.length());
    }

    private String[] validateSchoolKinds(final String schoolKindCsv) {
        final String[] schoolKinds;
        if (null == schoolKindCsv) {
            schoolKinds = new String[] {null}; // dummy
        } else {
            schoolKinds = schoolKindCsv.split(",\\s*");
        }
        return schoolKinds;
    }

    private String[] validateEnabledSchoolKinds(final String[] schoolKinds) {
        final List<String> enabledSchoolKinds = new ArrayList<String>();
        for (final String schoolKind : schoolKinds) {
            if (null != schoolKind) {
                enabledSchoolKinds.add(schoolKind);
            }
        }
        return enabledSchoolKinds.toArray(new String[enabledSchoolKinds.size()]);
    }

    private String[] validateAttendDayDatUpdateRegistercd(final String attendDayDatUpdateRegisterCdCsv) {
        final List<String> attendDayDatUpdateRegisterCdList = new ArrayList<String>();
        if (null != attendDayDatUpdateRegisterCdCsv) {
            for (final String v : attendDayDatUpdateRegisterCdCsv.split(",\\s*")) {
                if (!StringUtils.isBlank(v)) {
                    attendDayDatUpdateRegisterCdList.add(v);
                }
            }
        }
        if (!ArrayUtils.isEmpty(_attendDayDatUpdateRegisterCd)) {
            log.info(" attendDayDatUpdateRegisterCd = " + ArrayUtils.toString(_attendDayDatUpdateRegisterCd));
        }
        return attendDayDatUpdateRegisterCdList.toArray(new String[attendDayDatUpdateRegisterCdList.size()]);
    }

    private BaseDay validateBaseDay(final String baseDay) {
        if (!("2".equals(baseDay) || "1".equals(baseDay))) {
            log.error("パラメータ不正:" + baseDay);
            throw new IllegalArgumentException("パラメータ不正(baseDay):" + baseDay);
        }
        return BaseDay.getInstance(Integer.parseInt(baseDay));
    }

    private KenjaDateImpl validateDate(final String dateStr) {
        KenjaDateImpl date = null;
        try {
            date = KenjaDateImpl.getInstance(dateStr);
        } catch (final ParseException e) {
            log.error("パラメータ不正(date):" + dateStr, e);
            throw new IllegalArgumentException("パラメータ不正(date):" + dateStr);
        }
        return date;
    }

    /**
     * 実施日付を得る。
     * @return 実施日付
     */
    public KenjaDateImpl getDate() {
        return _date;
    }

    /**
     * パラメータを得る。
     * @return パラメータ
     */
    public KenjaParameters getKenjaParameters() {
        return _kenjaParam;
    }

    /**
     * Header のListを得る。
     * @param semester 学期
     * @param nendo 年度
     * @return Header のList
     */
    public List<Header> getHeaderList(final Semester semester, final int nendo) {
        _unit.init(_baseDay, getDate(), semester);
        log.info("処理単位=" + _unit);

        final List<Header> rtn = new ArrayList<Header>();
        for (final Term term : _unit.getTermList()) {
            log.debug(" レコードごとの集計範囲 :" + term);

            final Header header = new Header(term, nendo, getKenjaParameters());
            rtn.add(header);
        }
        return rtn;
    }
    /**
     * 職員コードを得る。
     * @return 職員コード
     */
    public String getStaffCd() {
        return _kenjaParam.getStaffCd();
    }

    /**
     * デバッグトレーサを得る。
     * @return デバッグトレーサ
     */
    public Tracer getTracer() {
        return _tracer;
    }

    /**
     * 基準日を得る。
     * @return 基準日
     */
    public BaseDay getBaseDay() {
        return _baseDay;
    }

    /**
     * 実行の有無を得る。
     * @return true なら処理を実行する
     */
    public boolean doRun() {
        return _doRun;
    }

    /**
     * 処理単位を得る。
     * @return 処理単位
     */
    public ExecuteUnit getUnit() {
        return _unit;
    }

    /**
     * 対象校種を得る。
     * @return 対象校種
     */
    public String[] getSchoolKind() {
        return _schoolKinds;
    }

    /**
     * 更新対象校種を得る。
     * @return 更新対象校種
     */
    public String[] getEnabledSchoolKind() {
        return _enabledSchoolKinds;
    }

    /**
     * プロパティーを得る。
     * @return プロパティー
     */
    public Properties getProperties() {
        return _properties;
    }

    /**
     * {@inheritDoc}
     */
    public void setControlMaster(final ControlMaster cm) {
        _cm = cm;
    }

    /**
     * {@inheritDoc}
     */
    public ControlMaster getControlMaster() {
        return _cm;
    }

    /**
     * ATTEND_DAY_DATの更新対象REGISTERCDを得る。
     * @return ATTEND_DAY_DATの更新対象REGISTERCD
     */
    public String[] getAttendDayDatUpdateRegisterCd() {
        return _attendDayDatUpdateRegisterCd;
    }
} // AccumulateOptions

// eof
