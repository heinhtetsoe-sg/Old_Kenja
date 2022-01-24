<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knje320Model.inc 56587 2017-10-22 12:54:51Z maeshiro $

class knje320Model extends Model {

    var $field      = array();
    var $cmd;
    var $control;   // コントロールマスタ
        
    function knje320Model() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("SCHOOL_SORT"       =>  VARS::post("SCHOOL_SORT"),   // 学校種別
                             "SENKOU_KAI"        =>  VARS::post("SENKOU_KAI"),    // 選考会
                             "YEAR"              =>  VARS::post("YEAR"),          // 年度
                             "DBNAME"            =>  VARS::post("DBNAME")         // DB変更に備えて
                            );

        $this->cmd         = VARS::request("cmd");
    }
}
?>
