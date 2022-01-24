<?php

require_once('for_php7.php');

class knjl401yModel extends Model {
    var $field      = array();
    var $ObjYear;
    var $cmd;
    var $control;   //コントロールマスタ
        
    function knjl401yModel()
    {
    }

    function init()
    {               
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->ObjYear      = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"      =>  VARS::post("TESTDIV"),          //入試区分
                             "APP_HOLD"     =>  VARS::post("APP_HOLD"),         //入試区分
                             "DBNAME"       =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd         = VARS::request("cmd");

    }
}
?>