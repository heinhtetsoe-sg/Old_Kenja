<?php

require_once('for_php7.php');


require_once('knje353Model.inc');
require_once('knje353Query.inc');

class knje353Controller extends Controller {
    var $ModelClassName = "knje353Model";
    var $ProgramID      = "KNJE353";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje353":
                    $sessionInstance->knje353Model();
                    $this->callView("knje353Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje353Ctl = new knje353Controller;
?>
