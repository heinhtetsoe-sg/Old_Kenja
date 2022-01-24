// kanji=漢字
/*
 * @version $Id: Facility.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2009/04/25 15:00:00 - JST
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */

package jp.co.alp.kenja.batch.otr.domain;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import nao_package.db.DB2UDB;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 使用施設
 * @version $Id: Facility.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class Facility {
    private static final Log log = LogFactory.getLog(Facility.class);
    /** 施設コード */
    public final String _faccd;
    /** ゲートNo. */
    public final List _gatenos;
    
    /**
     * コンストラクタ
     * @param faccd 学年
     * @param gateno HRクラス
     * @param name 略称
     */
    public Facility(final String faccd) {
        _faccd = faccd;
        _gatenos = new ArrayList();
    }

    public String getFaccd() {
        return _faccd;
    }

    public void addGateno(String gateno) {
        _gatenos.add(gateno);
    }
    
    public boolean contain(String gateno) {
        return _gatenos.contains(gateno);
    }
    
    private static String sql() {
        String sql = "select "
            + "faccd,"
            + "gateno "
            + "from FACILITY_GATE_DAT";
        return sql;
    }

    public static Map load(final DB2UDB db) throws SQLException {
        Map rtn = new TreeMap();
        db.query(sql());
        ResultSet rs = db.getResultSet();
        while (rs.next()) {
            final String faccd = rs.getString("faccd");
            final String gateno = rs.getString("gateno");
            
            Facility fac = (Facility) rtn.get(faccd);
            if (fac == null) {
                fac = new Facility(faccd);
                rtn.put(faccd, fac);
            }
            fac.addGateno(gateno);
        }
        return rtn;
    }

    private String gatenosStr() {
        final StringBuffer stb = new StringBuffer(50);
        stb.append("(");
        String comma = "";
        for (final Iterator it = _gatenos.iterator(); it.hasNext();) {
            String gateno = (String) it.next();
            stb.append(comma + gateno);
            comma = ",";
        }
        return stb.append(")").toString();
    }

    public String toString() {
        return "施設コード = " + _faccd + " ゲートNo = " + gatenosStr();
    }
}
