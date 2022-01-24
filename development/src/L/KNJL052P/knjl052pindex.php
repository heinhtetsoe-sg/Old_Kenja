<?php

require_once('for_php7.php');

require_once('knjl052pModel.inc');
require_once('knjl052pQuery.inc');

class knjl052pController extends Controller {
    var $ModelClassName = "knjl052pModel";
    var $ProgramID      = "KNJL052P";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "end":
                case "main":
                case "read":
                case "back":
                case "next":
                case "reset":
                    $this->callView("knjl052pForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
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
$knjl052pCtl = new knjl052pController;
?>
