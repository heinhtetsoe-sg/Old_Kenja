<?php

require_once('for_php7.php');

require_once('knje372hModel.inc');
require_once('knje372hQuery.inc');

class knje372hController extends Controller {
    var $ModelClassName = "knje372hModel";
    var $ProgramID      = "KNJE372H";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje372h":
                case "gakki":
                    $sessionInstance->knje372hModel();
                    $this->callView("knje372hForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje372hCtl = new knje372hController;
var_dump($_REQUEST);
?>
