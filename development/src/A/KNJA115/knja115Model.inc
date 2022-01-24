<?php

require_once('for_php7.php');

class knja115Model extends Model {
    var $semester;
    var $hrClass;
    var $cmd;
    var $control;        //コントロールマスタ

    function knja115Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("TAISYOU"   =>  VARS::post("TAISYOU")             //ラジオボタン
                             );

        $this->hrClass = VARS::post("GRADE_HR_CLASS");

        $this->cmd          = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
}
?>
