<?php

require_once('for_php7.php');

require_once('knja195bModel.inc');
require_once('knja195bQuery.inc');

class knja195bController extends Controller {
    var $ModelClassName = "knja195bModel";
    var $ProgramID      = "KNJA195B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "search":
                case "knja195b":
                case "change_class":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knja195bModel();
                    $this->callView("knja195bForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja195bCtl = new knja195bController;
var_dump($_REQUEST);
?>
