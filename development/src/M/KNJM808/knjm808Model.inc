<?php

require_once('for_php7.php');

class knjm808Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ

    function knjm808Model() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("YEAR"         =>  VARS::post("YEAR"),         //年度
                             "SEMESTER"     =>  VARS::post("SEMESTER"),     //学期
                             "OUTPUT"       =>  VARS::post("GRADE"),        //出力対象
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
