<?php

require_once('for_php7.php');

require_once('knje991tModel.inc');
require_once('knje991tQuery.inc');

class knje991tController extends Controller {
    var $ModelClassName = "knje991tModel";
    var $ProgramID      = "KNJE991T";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje991t":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knje991tModel();       //コントロールマスタの呼び出し
                    $this->callView("knje991tForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje991tCtl = new knje991tController;
//var_dump($_REQUEST);
?>
