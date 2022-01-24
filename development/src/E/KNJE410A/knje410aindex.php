<?php

require_once('for_php7.php');

require_once('knje410aModel.inc');
require_once('knje410aQuery.inc');

class knje410aController extends Controller {
    var $ModelClassName = "knje410aModel";
    var $ProgramID      = "KNJE410A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje410a":
                    $sessionInstance->knje410aModel();
                    $this->callView("knje410aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje410aCtl = new knje410aController;
?>
