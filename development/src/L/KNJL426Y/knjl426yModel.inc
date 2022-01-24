<?php

require_once('for_php7.php');

class knjl426yModel extends Model {
    var $field      = array();
    var $ObjYear;
    var $cmd;
    var $control;   //コントロールマスタ
        
    function knjl426yModel()
    {
    }

    function init()
    {               
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->ObjYear      = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"      =>  VARS::post("TESTDIV"),          //入試区分
                             "NDATE"        =>  VARS::post("NDATE"),            //通知日付
                             "CHECK_INEI"   =>  VARS::post("CHECK_INEI"),       //印影有り
                             "FORM"         =>  VARS::post("FORM"),             //帳票出力
                             "OUTPUT1"      =>  VARS::post("OUTPUT1"),          //出力範囲１
                             "OUTPUT2"      =>  VARS::post("OUTPUT2"),          //出力範囲２
                             "SIME_DATE"    =>  VARS::post("SIME_DATE"),        //締め切り日
                             "EXAMNO1"      =>  VARS::post("EXAMNO1"),          //受験番号
                             "APP_HOLD"     =>  VARS::post("APP_HOLD"),         //入試制度
                             "DBNAME"       =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
    }
}
?>
