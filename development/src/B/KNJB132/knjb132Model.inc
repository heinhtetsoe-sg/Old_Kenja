<?php

require_once('for_php7.php');

class knjb132Model extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード

    /**
     * メンバ変数の初期化
     */
    function knjb132Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;  //職員コード
    }

    function init() {
        $this->field = array("DATE"   =>  VARS::post("DATE"),   //対象日
                             "DBNAME" =>  VARS::post("DBNAME")  //DB変更に備えて
                             );

        $this->cmd = VARS::request("cmd");
    }
    /**
     * 変数をクリアします
     */
    function clean() {}
}
?>