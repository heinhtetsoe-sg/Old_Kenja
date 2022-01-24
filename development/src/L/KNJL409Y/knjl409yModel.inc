<?php

require_once('for_php7.php');

class knjl409yModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ

    function knjl409yModel()
    {
        common::GetControlMaster_Fnc($this->control);
        $this->ObjYear      = CTRL_YEAR+1;
    }

    function init()
    {
        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"          =>  VARS::post("TESTDIV"),          //入試区分
                             "RECEPTNO_FROM"    =>  VARS::post("RECEPTNO_FROM"),    //受付開始番号（開始）
                             "RECEPTNO_TO"      =>  VARS::post("RECEPTNO_TO"),      //受付開始番号（終了）
                             "APP_HOLD"         =>  VARS::post("APP_HOLD"),         //入試区分
                             "OUTPUT"           =>  VARS::post("OUTPUT"),           //本人/保護者
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
    }
}
?>
