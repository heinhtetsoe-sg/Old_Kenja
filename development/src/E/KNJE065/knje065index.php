<?php

require_once('for_php7.php');

require_once('knje065Model.inc');
require_once('knje065Query.inc');

class knje065Controller extends Controller {
    var $ModelClassName = "knje065Model";
    var $ProgramID      = "KNJE065";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "updMain":
                case "main":
                case "reset":
                    $this->callView("knje065Form1");
                   break 2;
                case "recalc":
                    $this->callView("knje065Form1");
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
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje065Ctl = new knje065Controller;
//var_dump($_REQUEST);
?>
