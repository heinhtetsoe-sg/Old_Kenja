<?php

require_once('for_php7.php');

class knjl328pModel extends Model {
    var $field = array();
    var $cmd;
    var $ObjYear;

    function knjl328pModel() {}

    function init() {
        $this->ObjYear = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"          =>  VARS::post("TESTDIV"),          //入試区分
                             "TSUCHI"           =>  VARS::post("TSUCHI"),           //通知日付
                             "ALLFLG"           =>  VARS::post("ALLFLG"),           //出力対象(1:全校, 2:指定)
                             "JIZEN"            =>  VARS::post("JIZEN"),            //事前印刷checkbox
                             "FINSCHOOLCD"      =>  VARS::post("FINSCHOOLCD"),      //出身学校コード
                             "SHIMEKIRI"        =>  VARS::post("SHIMEKIRI"),        //締切日付
                             "SHUTSUGAN"        =>  VARS::post("SHUTSUGAN"),        //出願期間
                             "DOCUMENTNO"       =>  VARS::post("DOCUMENTNO")        //文書番号textbox
                             );

        $this->cmd = VARS::request("cmd");
    }
}
?>
