<?php

require_once('for_php7.php');

class knjm810Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ

    function knjm810Model() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("YEAR"         =>  VARS::post("YEAR"),         //年度
                             "SEMESTER"     =>  VARS::post("SEMESTER"),     //学期
                             "GRADE"        =>  VARS::post("GRADE"),        //学年（入学年度）
                             "DBNAME"       =>  VARS::post("DBNAME")        //DB変更に備えて
                            );

        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll();
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
