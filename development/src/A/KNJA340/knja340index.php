<?php

require_once('for_php7.php');

require_once('knja340Model.inc');
require_once('knja340Query.inc');

class knja340Controller extends Controller {
    var $ModelClassName = "knja340Model";
    var $ProgramID      = "KNJA340";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja340":
                    $sessionInstance->knja340Model();
                    $this->callView("knja340Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja340Ctl = new knja340Controller;
?>
