<?php

require_once('for_php7.php');

require_once('knje017Model.inc');
require_once('knje017Query.inc');

class knje017Controller extends Controller {
    var $ModelClassName = "knje017Model";
    var $ProgramID      = "KNJE017";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje017":
                    $sessionInstance->knje017Model();
                    $this->callView("knje017Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje017Ctl = new knje017Controller;
?>
