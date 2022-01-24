<?php

require_once('for_php7.php');

require_once('knjl051gModel.inc');
require_once('knjl051gQuery.inc');

class knjl051gController extends Controller {
    var $ModelClassName = "knjl051gModel";
    var $ProgramID      = "KNJL051G";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                case "end":
                    $this->callView("knjl051gForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
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
$knjl051gCtl = new knjl051gController;
?>
