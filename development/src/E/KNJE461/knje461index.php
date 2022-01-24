<?php

require_once('for_php7.php');

require_once('knje461Model.inc');
require_once('knje461Query.inc');

class knje461Controller extends Controller {
    var $ModelClassName = "knje461Model";
    var $ProgramID      = "KNJE461";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change":
                case "changeHukusiki":
                case "main":
                case "seldate":
                case "clear";
                case "knje461";
                    $sessionInstance->knje461Model();
                    $this->callView("knje461Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje461Ctl = new knje461Controller;
?>
