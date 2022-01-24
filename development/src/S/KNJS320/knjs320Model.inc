<?php

require_once('for_php7.php');

class knjs320Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ

    function knjs320Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("GRADE_HR_CLASS"   => VARS::post("GRADE_HR_CLASS"),    //対象クラス
                             "TARGET_MONTH"     => VARS::post("TARGET_MONTH"),      //対象月
                             "TARGET_FORM"      => VARS::post("TARGET_FORM"),       //帳票選択
                             "FORM2_REMARK"     => VARS::post("FORM2_REMARK"),      //備考選択
                             "FORM3_REMARK"     => VARS::post("FORM3_REMARK"),      //備考選択
                             "DBNAME"           => VARS::post("DBNAME")             //DB変更に備えて
                            );

        $this->cmd   = VARS::request("cmd");
        $this->getPropertiesAll();
    }

    //変数をクリアします
    function clean()
    {
    }
}
?>
