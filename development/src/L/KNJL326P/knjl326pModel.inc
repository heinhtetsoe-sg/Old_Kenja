<?php

require_once('for_php7.php');

class knjl326pModel extends Model {
    var $field = array();
    var $cmd;
    var $ObjYear;

    function knjl326pModel() {}

    function init() {
        $this->ObjYear = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"          =>  VARS::post("TESTDIV"),          //入試区分
                             "TSUCHI"           =>  VARS::post("TSUCHI"),           //通知日付
                             "RESALT"           =>  VARS::post("RESALT"),           //帳票種類
                             "JIZEN"            =>  VARS::post("JIZEN"),            //事前印刷checkbox
                             "JIZEN_UNPASS"     =>  VARS::post("JIZEN_UNPASS"),     //事前印刷checkbox不合格通知書
                             "ORIDATE"          =>  VARS::post("ORIDATE"),          //オリエンテーション日付
                             "ORIHOUR"          =>  VARS::post("ORIHOUR"),          //時
                             "ORIMINUTE"        =>  VARS::post("ORIMINUTE"),        //分
                             "ALLFLG"           =>  VARS::post("ALLFLG"),           //出力対象
                             "ORDER"            =>  VARS::post("ORDER"),            //出力順
                             "TEXTEXAMNO"       =>  VARS::post("TEXTEXAMNO")        //受験番号指定textbox
                             );

        $this->cmd = VARS::request("cmd");
    }
}
?>
