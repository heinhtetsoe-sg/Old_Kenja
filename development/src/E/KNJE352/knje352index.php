<?php

require_once('for_php7.php');

require_once('knje352Model.inc');
require_once('knje352Query.inc');

class knje352Controller extends Controller {
    var $ModelClassName = "knje352Model";
    var $ProgramID      = "KNJE352";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje352":
                    $sessionInstance->knje352Model();
                    $this->callView("knje352Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje352Ctl = new knje352Controller;
?>
