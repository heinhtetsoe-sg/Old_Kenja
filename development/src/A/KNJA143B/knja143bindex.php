<?php

require_once('for_php7.php');

require_once('knja143bModel.inc');
require_once('knja143bQuery.inc');

class knja143bController extends Controller {
    var $ModelClassName = "knja143bModel";
    var $ProgramID      = "KNJA143B";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "clear";
                case "knja143b";
                case "search";
                    $sessionInstance->knja143bModel();
                    $this->callView("knja143bForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja143bCtl = new knja143bController;
?>
