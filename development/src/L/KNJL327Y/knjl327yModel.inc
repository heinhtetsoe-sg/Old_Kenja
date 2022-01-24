<?php

require_once('for_php7.php');

class knjl327yModel extends Model {
    var $field      = array();
    var $ObjYear;
    var $cmd;
    var $control;   //コントロールマスタ
        
    function knjl327yModel()
    {
    }

    function init()
    {               
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->ObjYear      = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"      =>  VARS::post("TESTDIV"),          //入試区分
                             "DESIREDIV"    =>  VARS::post("DESIREDIV"),        //志望区分
                             "NDATE"        =>  VARS::post("NDATE"),            //通知日付
                             "FORM"         =>  VARS::post("FORM"),             //帳票出力
                             "SCHOOL_SORT1" =>  VARS::post("SCHOOL_SORT1"),     //学校順1
                             "SCHOOL_SORT2" =>  VARS::post("SCHOOL_SORT2"),     //学校順2
                             "OUTPUT1"      =>  VARS::post("OUTPUT1"),          //（通知）出力範囲１
                             "OUTPUT2"      =>  VARS::post("OUTPUT2"),          //（通知）出力範囲２
                             "EXAMNO1"      =>  VARS::post("EXAMNO1"),          //（通知）受験番号
                             "OUTPUT3"      =>  VARS::post("OUTPUT3"),          //（奨学生）出力範囲
                             "EXAMNO2"      =>  VARS::post("EXAMNO2"),          //（奨学生）受験番号
                             "DBNAME"       =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
    }
}
?>
