<?php

require_once('for_php7.php');

require_once('knjl015gModel.inc');
require_once('knjl015gQuery.inc');

class knjl015gController extends Controller {
    var $ModelClassName = "knjl015gModel";
    var $ProgramID      = "KNJL015G";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                    $this->callView("knjl015gForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                    $this->callView("knjl015gForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl015gCtl = new knjl015gController;
?>
