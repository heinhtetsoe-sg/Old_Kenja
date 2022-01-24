<?php

require_once('for_php7.php');

class knjl334qModel extends Model {
    var $field = array();
    var $cmd;
    var $ObjYear;

    function knjl334qModel() {}

    function init() {
        $this->ObjYear = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"          =>  VARS::post("TESTDIV"),          //入試区分
                             "TAISYOU"          =>  VARS::post("TAISYOU"),          //出力対象
                             "KOJIN_SHITEI"     =>  VARS::post("KOJIN_SHITEI"),     //出力対象(個人選択時)
                             "POROW"            =>  VARS::post("POROW"),            //行
                             "POCOL"            =>  VARS::post("POCOL")             //列
                     );

        $this->cmd = VARS::request("cmd");
    }
}
?>
