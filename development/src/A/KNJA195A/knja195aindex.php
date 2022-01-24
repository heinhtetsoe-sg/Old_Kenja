<?php

require_once('for_php7.php');

require_once('knja195aModel.inc');
require_once('knja195aQuery.inc');

class knja195aController extends Controller {
    var $ModelClassName = "knja195aModel";
    var $ProgramID      = "KNJA195A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "search":
                case "knja195a":
                case "change_class":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knja195aModel();
                    $this->callView("knja195aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja195aCtl = new knja195aController;
var_dump($_REQUEST);
?>
