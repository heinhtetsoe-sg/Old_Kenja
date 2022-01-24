<?php

require_once('for_php7.php');

class knjf015Model extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード

    function knjf015Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("SEMESTER"    =>  VARS::post("SEMESTER"),    //学期
                             "MONTH"       =>  VARS::post("MONTH"),       //指定月
                             "DATE"        =>  VARS::post("DATE"),        //学籍処理日
                             "DBNAME"      =>  VARS::post("DBNAME"),      //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
    }

    function clean() {}
}
?>
