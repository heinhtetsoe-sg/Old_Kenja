<?php

require_once('for_php7.php');

class knjl330rModel extends Model {
    var $field      = array();
    var $ObjYear;
    var $cmd;
    var $control;   //コントロールマスタ
    var $fixApplicantDiv;
        
    function knjl330rModel()
    {
    }

    function init()
    {               
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->ObjYear      = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "NOTICEDATE"   =>  VARS::post("NOTICEDATE"),       //通知日付
                             "OUTPUT"       =>  VARS::post("OUTPUT"),           //（通知）出力範囲１
                             "RECEPTNO1"    =>  VARS::post("RECEPTNO1"),        //（通知）受験番号
                             "DBNAME"       =>  VARS::post("DBNAME")            //DB変更に備えて
                            );
                            
        $this->fixApplicantDiv = '1';
        if (SCHOOLKIND == 'J') {
            $this->fixApplicantDiv = '2';
        }

        $this->cmd = VARS::request("cmd");
    }
}
?>
