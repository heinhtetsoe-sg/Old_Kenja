<?php

require_once('for_php7.php');

require_once('knja226aModel.inc');
require_once('knja226aQuery.inc');

class knja226aController extends Controller {
    var $ModelClassName = "knja226aModel";
    var $ProgramID      = "KNJA226A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main";
                    $this->callView("knja226aForm1");
                    break 2;
                case "knja226a":                              //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja226aModel();        //コントロールマスタの呼び出し
                    $this->callView("knja226aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja226aCtl = new knja226aController;
?>
