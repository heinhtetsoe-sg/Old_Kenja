<?php

require_once('for_php7.php');

require_once('knje431aModel.inc');
require_once('knje431aQuery.inc');

class knje431aController extends Controller {
    var $ModelClassName = "knje431aModel";
    var $ProgramID      = "KNJE431A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje431a":
                    $sessionInstance->knje431aModel();
                    $this->callView("knje431aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje431aCtl = new knje431aController;
?>
