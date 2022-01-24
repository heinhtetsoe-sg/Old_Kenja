<?php

require_once('for_php7.php');

class knjl309tModel extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード

    function knjc171aModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;  //職員コード
    }

    function init() {
        $this->field = array("DATE"         =>  VARS::post("DATE"),         //作成日付
                             "T_DATE"       =>  VARS::post("T_DATE"),       //通知日付
                             "TIME_DIV"     =>  VARS::post("TIME_DIV"),     //午前・午後
                             "S_DATE"       =>  VARS::post("S_DATE"),       //送付日付
                             "DBNAME"       =>  VARS::post("DBNAME")        //DB変更に備えて
                             );

        $this->test_year = CTRL_YEAR + 1;
        $this->cmd = VARS::request("cmd");
    }

    function clean() {}
}
?>
