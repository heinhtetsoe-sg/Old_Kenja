<?php

require_once('for_php7.php');

class knjc210Model extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ

    function knjc210Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("SDATE"   =>  VARS::post("SDATE"),  //印刷範囲(開始日)
                             "EDATE"   =>  VARS::post("EDATE"),  //印刷範囲(終了日)
                             "HIDUKE"  =>  VARS::post("HIDUKE"), //欠席・遅刻・早退・受付日
                             "DBNAME"  =>  VARS::post("DBNAME")  //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
    }

    function clean() {}
}
?>
