<?php

require_once('for_php7.php');

require_once('knje433aModel.inc');
require_once('knje433aQuery.inc');

class knje433aController extends Controller {
    var $ModelClassName = "knje433aModel";
    var $ProgramID      = "KNJE433A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje433a":
                    $sessionInstance->knje433aModel();
                    $this->callView("knje433aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje433aCtl = new knje433aController;
?>
