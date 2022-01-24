<?php

require_once('for_php7.php');

class knjh189Model extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード

    /**
     * メンバ変数の初期化
     */
    function knjh189Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;  //職員コード
    }

    function init() {
        $this->field = array(
                             "DATE"             =>  VARS::post("DATE"),
                             "DBNAME"           =>  VARS::post("DBNAME")
                             );

        $this->cmd = VARS::request("cmd");
    }
    /**
     * 変数をクリアします
     */
    function clean() {}
}
?>
