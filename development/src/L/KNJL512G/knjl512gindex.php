<?php

require_once('for_php7.php');

require_once('knjl512gModel.inc');
require_once('knjl512gQuery.inc');

class knjl512gController extends Controller {
    var $ModelClassName = "knjl512gModel";
    var $ProgramID      = "KNJL512G";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "read":
                case "end":
                    $this->callView("knjl512gForm1");
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
$knjl512gCtl = new knjl512gController;
?>
