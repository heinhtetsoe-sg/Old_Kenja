<?php

require_once('for_php7.php');

require_once('knje400aModel.inc');
require_once('knje400aQuery.inc');

class knje400aController extends Controller {
    var $ModelClassName = "knje400aModel";
    var $ProgramID      = "KNJE400A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "reset":
                case "knje400a":
                case "list_set":
                    $sessionInstance->knje400aModel();
                    $this->callView("knje400aForm1");
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
$knje400aCtl = new knje400aController;
?>
