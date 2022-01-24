<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjwschool_searchModel extends Model {
    var $search = array();
    var $cmd;
    var $control;       //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    function knjwschool_searchModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }
    function init() {
        if ($this->cmd == "") {}

        $this->cmd             = VARS::request("cmd");
        $this->targetname      = VARS::request("targetname");
        $this->FINSCHOOL_TYPE  = VARS::request("FINSCHOOL_TYPE");
        $this->NAME            = VARS::request("NAME");
        $this->ADDR            = VARS::request("ADDR");
        $this->ZIPCD           = VARS::request("ZIPCD");

        $this->search = array("FINSCHOOL_TYPE"  => VARS::post("FINSCHOOL_TYPE"),
                              "NAME"            => VARS::post("NAME"),
                              "ADDR"            => VARS::post("ADDR"),
                              "ZIPCD"           => VARS::post("ZIPCD")
                              );
    }
}
?>
