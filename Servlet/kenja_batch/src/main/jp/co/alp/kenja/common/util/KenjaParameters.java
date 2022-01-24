// kanji=漢字
/*
 * $Id: KenjaParameters.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2004/07/20 18:44:06 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.util;

import java.net.URL;

import java.awt.Image;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.KenjaProgramInfo;
import jp.co.alp.kenja.common.domain.KenjaPermission;

/**
 * パラメータ（アプレットまたはコマンドライン）の抽象クラス。
 * @author tamura
 * @version $Id: KenjaParameters.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public abstract class KenjaParameters {
    /** DBホスト名 */
    public static final String PARAM_DBHOST = "dbhost";

    /** データベース名 */
    public static final String PARAM_DBNAME = "dbname";

    /** 職員コード */
    public static final String PARAM_STAFFCD = "staffcd";

    /** 教育課程コードを使用するか */
    public static final String PARAM_USE_CURRICULUMCD = "useCurriculumcd";

    /** 職員コードを表示するか */
    public static final String PARAM_IS_SHOW_STAFFCD = "isShowStaffcd";

    /** 時間割実施区分を使用するか */
    public static final String PARAM_USE_SCH_CHR_DAT_EXECUTEDIV = "useSchChrDatExecutediv";

    private static final Log log = LogFactory.getLog(KenjaParameters.class);

    /** メニュー校種 */
    public static final String PARAM_SCHOOLKIND = "SCHOOLKIND";

    /** メニュー学校コード */
    public static final String PARAM_SCHOOLCD = "SCHOOLCD";

    protected String _dbhost;
    protected String _dbname;
    protected String _staffcd;
    protected boolean _useCurriculumcd;
    protected boolean _isShowStaffcd;
    protected String _menuSCHOOLKIND;
    protected String _menuSCHOOLCD;

    private KenjaProgramInfo _programInfo;
    private KenjaPermission _permission;
    private boolean _isAdministratorGroup;

    /**
     * コンストラクタ。
     */
    protected KenjaParameters() {
        super();
    }

    /**
     * プログラム情報を設定する。
     * @param programInfo プログラム情報
     */
    public synchronized void setProgramInfo(final KenjaProgramInfo programInfo) {
        if (null == _programInfo) {
            _programInfo = programInfo;
        } else {
            log.warn("プログラム情報はすでに設定済み:" + programInfo + "," + _programInfo);
        }
    }

    /**
     * 権限を設定する。
     * @param permission 権限
     */
    public synchronized void setPermission(final KenjaPermission permission) {
        if (null == _permission) {
            _permission = permission;
        } else {
            log.warn("権限はすでに設定済み:" + permission + "," + _permission);
        }
    }

    /**
     * 管理者グループかを設定する。
     * @param isAdministratorGroup 管理者グループか
     */
    public void setAdministratorGroup(final boolean isAdministratorGroup) {
        _isAdministratorGroup = isAdministratorGroup;
    }

    /**
     *プログラム情報を得る。
     * @return プログラム情報
     */
    public KenjaProgramInfo getProgramInfo() {
        return _programInfo;
    }

    /**
     * 権限を得る。
     * @return 権限
     */
    public synchronized KenjaPermission getPermission() {
        if (null == _permission) {
            return KenjaPermission.DENIED;
        }
        return _permission;
    }

    /**
     * 管理者グループかを得る。
     * @return 管理者グループか
     */
    public boolean isAdministratorGroup() {
        return _isAdministratorGroup;
    }

    /**
     * パラメータを得る。
     * @param key キー
     * @return 値
     */
    public abstract String getParameter(final String key);

    /**
     * リソースファイルのURLを得る。
     * @param filename リソースファイルのファイル名
     * @return 得られたURL
     */
    public abstract URL getResourceURL(final String filename);

    /**
     * DBホスト名を得る。
     * @return DBホスト名
     */
    public String getDbHost() { return _dbhost; }

    /**
     * DB名を得る。
     * @return DB名
     */
    public String getDbName() { return _dbname; }

    /**
     * 職員コードを得る。
     * @return 職員コード
     */
    public String getStaffCd() {
        if (null == _staffcd) {
            return "99999999"; // varchar(8)
        }
        return _staffcd;
    }

    /**
     * 教育課程コードを使用するか。
     * @return 教育課程コードを使用するならtrue、そうでなければfalse
     */
    public boolean useCurriculumcd() { return _useCurriculumcd; }

    /**
     * 職員コードを表示するか。
     * @return 職員コードを表示するならtrue、そうでなければfalse
     */
    public boolean isShowStaffcd() { return _isShowStaffcd; }

    /**
     * メニュー校種を得る。
     * @return メニュー校種
     */
    public String getMenuSCHOOLKIND() { return _menuSCHOOLKIND; }

    /**
     * メニュー学校コードを得る。
     * @return メニュー学校コード
     */
    public String getMenuSCHOOLCD() { return _menuSCHOOLCD; }

    /**
     * デバッグモードか否か判定する。
     * @return デバッグモードなら<code>true</code>
     */
    public boolean isDebug() {
        if ("debug".equals(getParameter("debug"))) {
            log.fatal("デバッグモード.debug mode.");
            return true;
        }
        return false;
    }

    /**
     * パラメータの内容の妥当性を検査する。
     * @return 妥当なら<code>null</code>を返し、否なら例外のインスタンス。
     */
    public final Object validate() {
        if (null == getDbHost()) {
            return new IllegalArgumentException("パラメータ不正:" + PARAM_DBHOST);
        }

        if (null == getDbName()) {
            return new IllegalArgumentException("パラメータ不正:" + PARAM_DBNAME);
        }

        return null;
    }

} // KenjaParameters

// eof
