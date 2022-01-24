<?php

require_once('for_php7.php');

require_once('knje070fModel.inc');
require_once('knje070fQuery.inc');

class knje070fController extends Controller {
    var $ModelClassName = "knje070fModel";
    var $ProgramID      = "KNJE070F";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje070f":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knje070fModel();       //コントロールマスタの呼び出し
                    $this->callView("knje070fForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje070fCtl = new knje070fController;
//var_dump($_REQUEST);
?>
