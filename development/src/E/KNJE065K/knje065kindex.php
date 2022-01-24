<?php

require_once('for_php7.php');

require_once('knje065kModel.inc');
require_once('knje065kQuery.inc');

class knje065kController extends Controller {
    var $ModelClassName = "knje065kModel";
    var $ProgramID      = "KNJE065K";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "updMain":
                case "main":
                case "reset":
                    $this->callView("knje065kForm1");
                   break 2;
                case "recalc":
                    $this->callView("knje065kForm1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("updMain");
                    break 1;
                case "error":
                    $this->callView("error");
                case "read":
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                    $sessionInstance->setCmd("main");
                    break 1;
 //                   return;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje065kCtl = new knje065kController;
//var_dump($_REQUEST);
?>
