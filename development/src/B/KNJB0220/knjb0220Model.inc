<?php

require_once('for_php7.php');

class knjb0220Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ

    function knjb0220Model() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("KUBUN"        =>  VARS::post("KUBUN"),        //履修設定区分
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
