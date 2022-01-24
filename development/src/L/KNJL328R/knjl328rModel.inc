<?php

require_once('for_php7.php');

class knjl328rModel extends Model {
    var $field      = array();
    var $ObjYear;
    var $cmd;
    var $control;   //コントロールマスタ
    var $fixApplicantDiv;
        
    function knjl328rModel()
    {
    }

    function init()
    {               
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->ObjYear      = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"      =>  VARS::post("TESTDIV"),          //入試区分
                             "NOTICEDATE"   =>  VARS::post("NOTICEDATE"),       //通知日付
                             "FORM"         =>  VARS::post("FORM"),             //帳票出力
                             "OUTPUT1"      =>  VARS::post("OUTPUT1"),          //（通知）出力範囲１
                             "OUTPUT2"      =>  VARS::post("OUTPUT2"),          //（通知）出力範囲２
                             "RECEPTNO1"    =>  VARS::post("RECEPTNO1"),        //（通知）受験番号
                             "OUTPUT3"      =>  VARS::post("OUTPUT3"),          //（奨学生）出力範囲
                             "RECEPTNO2"    =>  VARS::post("RECEPTNO2"),        //（奨学生）受験番号
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
