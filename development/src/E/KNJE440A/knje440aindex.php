<?php

require_once('for_php7.php');

require_once('knje440aModel.inc');
require_once('knje440aQuery.inc');

class knje440aController extends Controller {
    var $ModelClassName = "knje440aModel";
    var $ProgramID      = "KNJE440A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje440a":
                    $sessionInstance->knje440aModel();
                    $this->callView("knje440aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje440aCtl = new knje440aController;
?>
