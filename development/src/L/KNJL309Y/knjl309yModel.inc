<?php

require_once('for_php7.php');

class knjl309yModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ

    function knjl309yModel()
    {
        common::GetControlMaster_Fnc($this->control);
        $this->ObjYear = CTRL_YEAR+1;
    }

    function init()
    {
        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"          =>  VARS::post("TESTDIV"),          //入試区分
                             "FORM"             =>  VARS::post("FORM"),             //フォーム出力
                             "APP_HOLD"         =>  VARS::post("APP_HOLD"),         //入試区分
                             "SHIFT_DESIRE_FLG" =>  VARS::post("SHIFT_DESIRE_FLG"), //特別判定希望者のみ出力
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
    }
}
?>
