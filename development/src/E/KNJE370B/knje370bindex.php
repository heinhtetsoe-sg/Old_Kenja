<?php

require_once('for_php7.php');

require_once('knje370bModel.inc');
require_once('knje370bQuery.inc');

class knje370bController extends Controller {
    var $ModelClassName = "knje370bModel";
    var $ProgramID      = "KNJE370B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knje370bForm1");
                    break 2;
                case "":
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csv":
                    $sessionInstance->getCsv();
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje370bCtl = new knje370bController;
?>
