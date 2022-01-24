<?php

require_once('for_php7.php');

class knjl326nModel extends Model {
    var $field      = array();
    var $ObjYear;
    var $cmd;
    var $control;   //コントロールマスタ
        
    function knjl326nModel()
    {
    }

    function init()
    {               
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->ObjYear      = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"      =>  VARS::post("TESTDIV"),          //入試区分
                             "TSUCHI_DATE"  =>  VARS::post("TSUCHI_DATE"),      //通知日付
                             "OUTPUT_KIND"  =>  VARS::post("OUTPUT_KIND"),      //帳票種類
                             "OUTPUT"       =>  VARS::post("OUTPUT"),           //出力範囲
                             "SCHOOLCD"     =>  VARS::post("SCHOOLCD"),         //出身学校コード
                             "DBNAME"       =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
    }
}
?>
