<?php

require_once('for_php7.php');

class knjl326rModel extends Model {
    var $field      = array();
    var $ObjYear;
    var $cmd;
    var $control;   //コントロールマスタ
    var $fixApplicantDiv;
        
    function knjl326rModel()
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
                             "OUTPUT"       =>  VARS::post("OUTPUT"),           //出力範囲
                             "SCHOOLCD"     =>  VARS::post("SCHOOLCD"),         //出身学校コード
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
