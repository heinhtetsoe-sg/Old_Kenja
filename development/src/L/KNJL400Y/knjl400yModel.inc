<?php

require_once('for_php7.php');

class knjl400yModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ

    function knjl400yModel()
    {
        common::GetControlMaster_Fnc($this->control);
        $this->ObjYear      = CTRL_YEAR+1;
    }

    function init()
    {
        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"          =>  VARS::post("TESTDIV"),          //入試区分
                             "LINE"             =>  VARS::post("LINE"),             //開始位置（行）
                             "ROW"              =>  VARS::post("ROW"),              //開始位置（列）
                             "APP_HOLD"         =>  VARS::post("APP_HOLD"),         //入試区分
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
    }
}
?>
