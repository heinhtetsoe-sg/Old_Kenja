<?php

require_once('for_php7.php');

class knjd624mModel extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード

    function knjd624mModel() {}

    function init() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;  //職員コード

        $this->field = array("GRADE"    =>  VARS::post("GRADE"),    //学年
                             "DATE"     =>  VARS::post("DATE"),     //対象日
                             "SEMESTER" =>  VARS::post("SEMESTER"), //対象日
                             "DBNAME"   =>  VARS::post("DBNAME")    //DB変更に備えて
                             );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
    }

    function clean() {}
}
?>
