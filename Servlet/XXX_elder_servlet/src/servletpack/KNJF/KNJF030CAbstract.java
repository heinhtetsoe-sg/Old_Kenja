// kanji=漢字
/*
 * $Id: f8682e51a0bc60737cd992d07a9cc2fe444d3908 $
 *
 * 作成日: 2009/10/07 15:42:31 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJF;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

/**
 * <<クラスの説明>>。
 *
 * @author m-yama
 * @version $Id: f8682e51a0bc60737cd992d07a9cc2fe444d3908 $
 */
public abstract class KNJF030CAbstract {

    private static final Log log = LogFactory.getLog("KNJF030CAbstract.class");

    protected final KNJF030C.Param _param;

    protected final DB2UDB _db2;

    protected final Vrw32alp _svf;

    protected final String F_GANKA_KENSIN = "KNJF030C_4.frm";

    protected final String F_KEKKA_HA = "KNJF030C_5.frm";

    protected final String F_KEKKA_HA_A = "KNJF030C_5A.frm";

    protected final String F_TEIKI_KENSIN = "KNJF030C_6_2.frm";

    protected final String F_TEIKI_KENSIN_MUSASHI = "KNJF030C_6_3.frm";

    protected final String F_TEIKI_KENSIN_KUMA = "KNJF030C_6.frm";

    protected final String F_TEIKI_KENSIN_ITIRAN = "KNJF030C_10_2.frm";

    protected final String F_TEIKI_KENSIN_ITIRAN_KUMA = "KNJF030C_10.frm";

    protected final String F_KENKOU_SINDAN_IPPAN_A = "KNJF030A_1.frm";

    protected final String F_KENKOU_SINDAN_IPPAN_A_KUMA = "KNJF030A_1_3.frm";

    protected final String F_KENKOU_SINDAN_IPPAN_A_4 = "KNJF030A_1_4.frm";

    protected final String F_KENKOU_SINDAN_IPPAN_A_P = "KNJF030A_1P.frm";

    protected final String F_KENKOU_SINDAN_IPPAN_A_PKUMA = "KNJF030A_1P_3.frm";

    protected final String F_KENKOU_SINDAN_IPPAN_A_P4 = "KNJF030A_1P_4.frm";

    protected final String F_KEKKA_HA2 = "KNJF030C_11.frm";

    protected final String GANKA_KENSIN_DOC = "05";

    protected final String KEKKA_HA_DOC = "06";

    protected final String KEKKA_HA_A_DOC = "07";

    protected final String TEIKI_KENSIN_DOC = "08";

    /**
     * コンストラクタ。
     */
    public KNJF030CAbstract(final KNJF030C.Param param, final DB2UDB db2, final Vrw32alp svf) {
        _param = param;
        _db2 = db2;
        _svf = svf;
    }

    abstract protected boolean printMain(final List printStudents) throws SQLException;

    protected String getName(final String nameCd1, final String nameCd2, final String useFieldName) throws SQLException {
        final int checkNameCd2 = NumberUtils.isDigits(nameCd2) ? Integer.parseInt(nameCd2) : 0;
        final String sql = "SELECT VALUE(" + useFieldName + ", '') AS LABEL FROM NAME_MST WHERE NAMECD1 = '" + nameCd1 + "' AND NAMECD2 = '" + nameCd2 + "'";
        ResultSet rs = null;
        PreparedStatement ps = null;
        String label = "";
        try {
            ps = _db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                label = checkNameCd2 == 0 ? "" : rs.getString("LABEL");
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            _db2.commit();
        }
        return label;
    }

    protected String getNameMst(final String nameCd1, final String nameCd2, final String fieldname) throws SQLException {
        final String sql = "SELECT " + fieldname + " FROM NAME_MST WHERE NAMECD1 = '" + nameCd1 + "' AND NAMECD2 = '" + nameCd2 + "'";
        ResultSet rs = null;
        PreparedStatement ps = null;
        String rtn = null;
        try {
            ps = _db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                rtn = rs.getString(fieldname);
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            _db2.commit();
        }
        return rtn;
    }

protected String setName(
            final String fieldName,
            final String nameCd1,
            final String nameCd2,
            final String useFieldName,
            final int lineLength,
            final String plusString,
            final String fieldNameOver
    ) throws SQLException {
        final int checkNameCd2 = NumberUtils.isDigits(nameCd2) ? Integer.parseInt(nameCd2) : -1;
        final String sql = "SELECT VALUE(" + useFieldName + ", '') AS LABEL FROM NAME_MST WHERE NAMECD1 = '" + nameCd1 + "' AND NAMECD2 = '" + nameCd2 + "'";
        ResultSet rs = null;
        PreparedStatement ps = null;
        String retStr = "";
        try {
            ps = _db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String setName = checkNameCd2 == -1 ? "" + plusString : rs.getString("LABEL") + plusString;
                if (lineLength > 0 && setName.length() > lineLength) {
                    _svf.VrsOut(fieldName + fieldNameOver, setName);
                } else {
                    _svf.VrsOut(fieldName, setName);
                }
                retStr = setName;
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            _db2.commit();
        }
        return retStr;
    }    /** 文面マスタからタイトルと本文をセット 共通 */
    protected void printTitle(final String documentcd) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String documentSql = statementTitle(documentcd);
            ps = _db2.prepareStatement(documentSql);
            rs = ps.executeQuery();
            while (rs.next()) {
                if (rs.getString("TEXT") != null) {
                    StringTokenizer st = new StringTokenizer(rs.getString("TEXT"), "\n");
                    int j = 1;
                    while (st.hasMoreTokens()) {
                        _svf.VrsOut("TEXT" + j, st.nextToken());// 本文
                        j++;
                    }
                }
                _svf.VrsOut("TITLE", rs.getString("TITLE")); // タイトル
            }
            rs.close();
            ps.close();
            _db2.commit();
        } catch (Exception ex) {
            DbUtils.closeQuietly(null, ps, rs);
            _db2.commit();
        }
    }

    /**
     * 文面マスタ情報 文面マスタからタイトルと本文を取得
     */
    private String statementTitle(final String documentcd) {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT TITLE, TEXT FROM DOCUMENT_MST WHERE DOCUMENTCD = '" + documentcd + "'");
        } catch (Exception e) {
            log.warn("statementTitle error!", e);
        }
        return stb.toString();
    }
}

// eof
