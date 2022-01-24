<?php

require_once('for_php7.php');

class knjb0051Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ

    function knjb0051Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("JIKANWARI_SYUBETU"  =>  VARS::post("JIKANWARI_SYUBETU"),  //時間割種別
                             "TITLE"              =>  VARS::post("TITLE"),              //基本時間割
                             "SDATE"              =>  VARS::post("SDATE"),              //開始日付(通常時間割)
                             "EDATE"              =>  VARS::post("EDATE"),              //終了日付(通常時間割)
                             "DATE_WEEK"          =>  VARS::post("DATE_WEEK"),          //対象範囲(週数)
                             "DBNAME"             =>  VARS::post("DBNAME"),             //DB変更に備えて
                     );

        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll();
    }

    function clean() {}
}
?>
