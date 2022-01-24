<?php

require_once('for_php7.php');

require_once('knje400Model.inc');
require_once('knje400Query.inc');

class knje400Controller extends Controller {
    var $ModelClassName = "knje400Model";
    var $ProgramID      = "KNJE400";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "reset":
                case "knje400":
                case "list_set":
                    $sessionInstance->knje400Model();
                    $this->callView("knje400Form1");
                    exit;
                case "notify":
                case "add":
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "delete":
                    $sessionInstance->getDeleteModel();
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
$knje400Ctl = new knje400Controller;
?>
