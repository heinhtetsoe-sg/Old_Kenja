<?php

require_once('for_php7.php');

class knjl335qModel extends Model {
    var $field = array();
    var $cmd;
    var $ObjYear;

    function knjl335qModel() {}

    function init() {
        $this->ObjYear = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"          =>  VARS::post("TESTDIV"),          //入試区分
                             "OUTPUT_SELECT"    =>  VARS::post("OUTPUT_SELECT"),    //出力選択
                             "TAISYOU"          =>  VARS::post("TAISYOU")           //出力対象
                     );

        $this->cmd = VARS::request("cmd");
    }
}
?>
