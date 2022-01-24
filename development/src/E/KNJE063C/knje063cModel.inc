<?php

require_once('for_php7.php');

class knje063cModel extends Model {
    var $field = array();
    var $cmd;
    var $selectdata;
    var $selectdataText;

    function knje063cModel() {}

    function init() {
        $this->field = array (
            "HENKAN_MOTO_YEAR"       =>  VARS::post("HENKAN_MOTO_YEAR"),       //変換対象 年度
            "HENKAN_MOTO_SUBCLASS"   =>  VARS::post("HENKAN_MOTO_SUBCLASS"),   //変換対象 変換元科目
            "HENKAN_JOKEN_1_YEAR"    =>  VARS::post("HENKAN_JOKEN_1_YEAR"),    //変換条件１ 年度
            "SYUTOKU_JOKEN_SUBCLASS" =>  VARS::post("SYUTOKU_JOKEN_SUBCLASS"), //変換条件１ 取得条件科目
            "ATTEND_DIV"             =>  VARS::post("ATTEND_DIV"),             //変換条件１ 受講、非受講
            "CREGIT_CNT"             =>  VARS::post("CREGIT_CNT"),             //変換条件２ 取得単位数
            "HENKAN_GO_SUBCLASS"     =>  VARS::post("HENKAN_GO_SUBCLASS"),     //変換語 変換後科目
        );

        $this->cmd          = VARS::request("cmd");
        $this->selectdata       = VARS::post("selectdata");
        $this->selectdataText   = VARS::post("selectdataText");

        //プロパティファイルの読み込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    function validateInput()
    {
        return true;
    }

    //更新
    function &getUpdateModel(){
        if ($this->validateInput()) {
            knje063cQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }
}
?>