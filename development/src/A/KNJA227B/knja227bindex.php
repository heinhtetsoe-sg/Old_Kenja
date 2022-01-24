<?php

require_once('for_php7.php');

require_once('knja227bModel.inc');
require_once('knja227bQuery.inc');

class knja227bController extends Controller {
    var $ModelClassName = "knja227bModel";
    var $ProgramID      = "KNJA227B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "search":
                case "knja227b":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja227bModel();       //コントロールマスタの呼び出し
                    $this->callView("knja227bForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knja227bCtl = new knja227bController;
?>
