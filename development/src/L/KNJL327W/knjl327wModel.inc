<?php

require_once('for_php7.php');

class knjl327wModel extends Model {
    var $cmd;

    //メンバ変数の初期化
    function knjl327wModel() {}

    function init() {
        $this->cmd          = VARS::request("cmd");
        $this->ObjYear      = CTRL_YEAR + 1;
        $this->applicantdiv = VARS::post("APPLICANTDIV");
        $this->testdiv      = VARS::post("TESTDIV");
        $this->style        = VARS::post("STYLE");          //様式ラジオボタン
        $this->kisai_date   = VARS::post("KISAI_DATE");
        $this->suc_date     = VARS::post("SUC_DATE");
        $this->suc_am_pm    = VARS::post("SUC_AM_PM");
        $this->suc_hour     = VARS::post("SUC_HOUR");
        $this->suc_minute   = VARS::post("SUC_MINUTE");
        $this->comment      = VARS::post("COMMENT");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }
}
?>
