<?php

require_once('for_php7.php');


class knje370aModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $staffcd;        //職員コード
    var $selectdata;

    function knje370aModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //職員コード
    }

    function init() {
        $this->field = array("YEAR"         =>  VARS::post("YEAR"),             //年度
                             "SEMESTER"     =>  VARS::post("SEMESTER"),         //学期
                             "DATA_DIV"     =>  VARS::post("DATA_DIV"),         //データ区分
                             "GRADE_HR"     =>  VARS::post("GRADE_HR"),         //年組
                             "DBNAME"       =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->field["OUT_DIV"] = 1;//固定
        $this->cmd         = VARS::request("cmd");
        $this->selectdata  = VARS::post("selectdata");
    }
}
?>
