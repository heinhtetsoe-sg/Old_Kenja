<?php

require_once('for_php7.php');

require_once('knjl571aModel.inc');
require_once('knjl571aQuery.inc');

class knjl571aController extends Controller {
    var $ModelClassName = "knjl571aModel";
    var $ProgramID      = "KNJL571A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                case "end":
                case "search":
                case "reload":
                case "sort":
                    $this->callView("knjl571aForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("search");
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
$knjl571aCtl = new knjl571aController;
?>
