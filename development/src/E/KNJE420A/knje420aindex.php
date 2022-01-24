<?php

require_once('for_php7.php');

require_once('knje420aModel.inc');
require_once('knje420aQuery.inc');

class knje420aController extends Controller {
    var $ModelClassName = "knje420aModel";
    var $ProgramID      = "KNJE420A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje420a":
                    $sessionInstance->knje420aModel();
                    $this->callView("knje420aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje420aCtl = new knje420aController;
?>
