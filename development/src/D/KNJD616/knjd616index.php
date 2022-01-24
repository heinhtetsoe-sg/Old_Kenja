<?php

require_once('for_php7.php');

require_once('knjd616Model.inc');
require_once('knjd616Query.inc');

class knjd616Controller extends Controller {
    var $ModelClassName = "knjd616Model";
    var $ProgramID      = "KNJD616";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd616":
                case "semechg":
                case "gakki":
                case "grade":
                    $sessionInstance->knjd616Model();
                    $this->callView("knjd616Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd616Ctl = new knjd616Controller;
var_dump($_REQUEST);
?>
