<?php

require_once('for_php7.php');

class knjg022Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    function knjg022Model()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {               
        $this->field = array("YEAR"         =>  VARS::post("YEAR"),     //年度選択
                             "SORT"         =>  VARS::post("SORT"),     //出力順
                             "PAGE"         =>  VARS::post("PAGE"),     //印刷開始ページ
                             "DBNAME"       =>  VARS::post("DBNAME")    //DB変更に備えて
                            );

        $this->cmd   = VARS::request("cmd");
        $this->getPropertiesAll();
    }

    function clean()
    {
    }
}
?>
