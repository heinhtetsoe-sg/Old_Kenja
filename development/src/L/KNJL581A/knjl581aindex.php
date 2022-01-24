<?php

require_once('for_php7.php');

require_once('knjl581aModel.inc');
require_once('knjl581aQuery.inc');

class knjl581aController extends Controller {
    var $ModelClassName = "knjl581aModel";
    var $ProgramID      = "KNJL581A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "end":
                    $this->callView("knjl581aForm1");
                    break 2;
                case "":
                    $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl581aCtl = new knjl581aController;
?>
