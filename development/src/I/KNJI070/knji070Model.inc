<?php

require_once('for_php7.php');

class knji070Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;       //コントロールマスタ

    function knji070Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("DATE"         =>  VARS::post("DATE"),         //処理年月日
                             "OUTPUT"       =>  VARS::post("OUTPUT"),       //調査書種類
                             "SEKI"         =>  VARS::post("SEKI"),         //記載責任者
                             "FORM6"        =>  VARS::post("FORM6"),        //６年用フォーム選択
                             "DBNAME"       =>  VARS::post("DBNAME")        //DB変更に備えて
                            );


        $this->cmd = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        //初期値の設定
        $this->Knje080UseAForm = ($this->Properties["Knje080UseAForm"] == '1') ? $this->Properties["Knje080UseAForm"] : 0;
    }
}
?>
