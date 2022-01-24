// kanji=漢字
/*
 * $Id: AccumulateOptions.java 75473 2020-07-16 07:19:19Z maeshiro $
 *
 * 作成日: 2006/09/22 14:57:43 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.ui;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import jp.co.alp.kenja.batch.KenjaBatchContext;
import jp.co.alp.kenja.batch.accumulate.option.BaseDay;
import jp.co.alp.kenja.batch.accumulate.option.Header;
import jp.co.alp.kenja.batch.accumulate.option.Tracer;
import jp.co.alp.kenja.batch.domain.Term;
import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.KenjaDateImpl;
import jp.co.alp.kenja.common.domain.Semester;
import jp.co.alp.kenja.common.util.KenjaCommandLineParameters;
import jp.co.alp.kenja.common.util.KenjaParameters;

import org.apache.commons.lang.BooleanUtils;
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

    /** 実行の有無 */
    public static final String DO_RUN = "doRun";

    /*pkg*/static final Log log = LogFactory.getLog(AccumulateOptions.class);

    private final KenjaParameters _kenjaParam;
    private final ExecuteUnit _unit;
    /** 対象校種 */
    private final String[] _schoolKinds;

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

        final String dateStr = kenjaParam.getParameter(DATE);
        try {
            _date = KenjaDateImpl.getInstance(dateStr);
        } catch (final ParseException e) {
            log.error("パラメータ不正(date):" + dateStr, e);
            throw new IllegalArgumentException("パラメータ不正(date):" + dateStr);
        }

        final String baseDay = kenjaParam.getParameter(BASE_DAY);
        if (!("2".equals(baseDay) || "1".equals(baseDay))) {
            log.error("パラメータ不正:" + baseDay);
            throw new IllegalArgumentException("パラメータ不正(date):" + dateStr);
        }
        _baseDay = BaseDay.getInstance(Integer.parseInt(baseDay));

        _kenjaParam = kenjaParam;
        _properties = properties;
        log.debug("プロパティファイルの項目=" + properties);

        final String unitStr = properties.getProperty(UNIT, "semester");
        _unit = new ExecuteUnit(unitStr);
        
        final String schoolKindCsv = _properties.getProperty(SCHOOL_KIND);
        if (null == schoolKindCsv) {
        	_schoolKinds = new String[] {null};
        } else {
        	_schoolKinds = schoolKindCsv.split(",\\s*");
        }

        final String traceStudent = properties.getProperty(SCHREGNO, null);
        _tracer = new Tracer(traceStudent);
        _doRun = BooleanUtils.toBoolean(properties.getProperty(DO_RUN));

        final String registerCd = _kenjaParam.getStaffCd();
        log.info("登録者コード(REGISTERCD/STAFFCD)=" + registerCd + ", length=" + registerCd.length());
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

            final Header header = new Header(term, nendo, getKenjaParameters().getStaffCd());
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
} // AccumulateOptions

// eof
