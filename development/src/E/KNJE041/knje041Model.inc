<?php

require_once('for_php7.php');


// 定数定義
class knje041Model extends Model {
    var $sendAuth;
    var $auth;

    function knje041Model() {}
    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if(VARS::get("SEND_PRGID")){
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;
        $this->cmd = VARS::request("cmd");
    }
    //入力項目チェック
    function validateInput() {
        return true;
    }

    /**
     * 編集完了用のモデルを返します
    */
    function &getUpdateModel() {}

    /**
     * アップデート時の内部メソッド
     */
    function &update() {}

    /**
     * 変数をクリアします
     */
    function clean() {}
}
?>
