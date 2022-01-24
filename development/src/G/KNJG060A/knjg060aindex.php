<?php

require_once('for_php7.php');

require_once('knjg060aModel.inc');
require_once('knjg060aQuery.inc');

class knjg060aController extends Controller {
    var $ModelClassName = "knjg060aModel";
    var $ProgramID      = "KNJG060A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "change":
                case "edit":
                case "search":
                case "read":
                case "subEnd":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjg060aForm1");
                    break 2;
                case "readStation":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjg060aSubForm1");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("search");
                    break 1;
                //上覧から下欄へ
                case "read2":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjg060aForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->setCmd("edit");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$KNJg060aCtl = new knjg060aController;
//var_dump($_REQUEST);
?>
