<?php

require_once('for_php7.php');

require_once('knjl621aModel.inc');
require_once('knjl621aQuery.inc');

class knjl621aController extends Controller {
    var $ModelClassName = "knjl621aModel";
    var $ProgramID      = "KNJL621A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "insert":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "delete":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "edit":
                case "ajaxGetName":
                case "ajaxGetSeatno":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjl621aForm");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl621aCtl = new knjl621aController;
?>
