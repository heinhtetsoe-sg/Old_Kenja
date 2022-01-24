<?php

require_once('for_php7.php');

class knjs331Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $weekArray = array();

    function knjs331Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("SCHOOL_KIND"  => VARS::post("SCHOOL_KIND"),   //校種
                             "GRADE"        => VARS::post("GRADE"),         //学年
                             "DBNAME"       => VARS::post("DBNAME")         //DB変更に備えて
                            );

        $this->cmd   = VARS::request("cmd");

        $this->getPropertiesAll();
        if (VARS::get("SEND_selectSchoolKind")) {
            $this->selectSchoolKind = VARS::get("SEND_selectSchoolKind");
        }

    }

    //変数をクリアします
    function clean()
    {
    }
}
?>
