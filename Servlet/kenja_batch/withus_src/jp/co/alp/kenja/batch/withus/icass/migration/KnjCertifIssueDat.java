// kanji=漢字
/*
 * $Id: KnjCertifIssueDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/08/15 15:46:35 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * CERTIF_ISSUE_DAT を作る。
 * @author takaesu
 * @version $Id: KnjCertifIssueDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjCertifIssueDat extends AbstractKnj implements IKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjCertifIssueDat.class);

    /**
     * 申請No.(証明書連番)
     */
    public static int _certifIndex = 1;

    public KnjCertifIssueDat() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "証明書発行データ"; }

    void migrate() throws SQLException {
        final String sql;
        sql = " SELECT "
            + "     T1.*, "
            + "     CASE WHEN T2.GAKUSEKI_JOTAI_CODE = '3' "
            + "          THEN '1' "
            + "          WHEN T2.GAKUSEKI_JOTAI_CODE = '5' "
            + "          THEN '1' "
            + "          WHEN T2.GAKUSEKI_JOTAI_CODE = '6' "
            + "          THEN '1' "
            + "          WHEN T2.GAKUSEKI_JOTAI_CODE = '9' "
            + "          THEN '1' "
            + "          ELSE '0' "
            + "      END AS GRADUATE_FLG "
            + " FROM "
            + "     SEITO_HAKKO_BUNSHO T1 "
            + " LEFT JOIN SEITO_GAKUSEKI_IDO_RIREKI T2 ON "
            + "      T1.SHIGANSHA_RENBAN = T2.SHIGANSHA_RENBAN ";

            log.debug("sql=" + sql);

        final List result = _runner.mapListQuery(sql);
        log.debug("データ件数=" + result.size());

        _runner.listToKnj(result, "certif_issue_dat", this);
    }

    /** {@inheritDoc} */
    public Object[] mapToArray(Map map) {
        final String  _year;            //String
        final String  _certif_index;    //String
        final String  _schregno;        //String
        final String  _applicantno;     //String
        final String  _type;            //String
        final String  _certif_kindcd;   //String
        final String  _graduate_flg;    //String
        final Date    _applydate;       //Date
        final String  _issuername;      //String
        final String  _issuecd;         //String
        final Integer _certif_no;       //Integer
        final Date    _issuedate;       //Date
        final String  _charge;          //String
        final String  _printcd;         //String
        final String  _registercd;      //String

        final Integer seitoHakkoBunshoNo = new Integer(_certifIndex++);

        _year =          (String) map.get("NENDO_CODE");
        _certif_index =  seitoHakkoBunshoNo.toString();
        final String _shiganshaRenban = (String) map.get("SHIGANSHA_RENBAN");
        _schregno =      _param.getSchregno(_shiganshaRenban);
        _applicantno =   _param.getApplicantNo(_shiganshaRenban);
        _type =          null;
        final String bunsho_shubetsu_code = (String) map.get("BUNSHO_SHUBETSU_CODE");
        if (bunsho_shubetsu_code.equals("61")) {
            _certif_kindcd = "004";
        } else if (bunsho_shubetsu_code.equals("62")) {
            _certif_kindcd = "018";
        } else if (bunsho_shubetsu_code.equals("63")) {
            _certif_kindcd = "011";
        } else if (bunsho_shubetsu_code.equals("64")) {
            _certif_kindcd = "003";
        } else if (bunsho_shubetsu_code.equals("65")) {
            _certif_kindcd = "001";
        } else if (bunsho_shubetsu_code.equals("66")) {
            _certif_kindcd = "006";
        } else if (bunsho_shubetsu_code.equals("71")) {
            _certif_kindcd = "008";
        } else if (bunsho_shubetsu_code.equals("72")) {
            _certif_kindcd = "009";
        } else {
            _certif_kindcd = null;
        }
        _graduate_flg =  (String) map.get("GRADUATE_FLG");
        _applydate =     map.get("HAKKO_NENGAPPI") != null ? Date.valueOf((String) map.get("HAKKO_NENGAPPI")) : null;
        _issuername =    null;
        _issuecd =       "1";
        final Integer certif_no = new Integer(Integer.valueOf((String) map.get("SEITO_HAKKO_BUNSHO_NO")).intValue());
        _certif_no =     certif_no;
        _issuedate =     _applydate;
        _charge =        null;// 13. CHARGE; 証明書発行手数料支払
        _printcd =       null;
        _registercd =    (String) Param.REGISTERCD;

        final Object[] rtn = {
                _year,
                _certif_index,
                _schregno,
                _applicantno,
                _type,
                _certif_kindcd,
                _graduate_flg,
                _applydate,
                _issuername,
                _issuecd,
                _certif_no,
                _issuedate,
                _charge,
                _printcd,
                _registercd,
        };
        return rtn;
    }
}
// eof

