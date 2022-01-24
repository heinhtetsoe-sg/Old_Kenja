<?php

require_once('for_php7.php');

require_once('knjl316Model.inc');
require_once('knjl316Query.inc');

class knjl316Controller extends Controller {
    var $ModelClassName = "knjl316Model";
    var $ProgramID      = "KNJL316";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl316":
                    $sessionInstance->knjl316Model();
                    $this->callView("knjl316Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl316Ctl = new knjl316Controller;
?>
