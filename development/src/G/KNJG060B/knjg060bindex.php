<?php

require_once('for_php7.php');

require_once('knjg060bModel.inc');
require_once('knjg060bQuery.inc');

class knjg060bController extends Controller {
    var $ModelClassName = "knjg060bModel";
    var $ProgramID      = "KNJG060B";

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
                    $this->callView("knjg060bForm1");
                    break 2;
                case "readStation":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjg060bSubForm1");
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
                    $this->callView("knjg060bForm1");
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
$KNJg060Ctl = new knjg060bController;
//var_dump($_REQUEST);
?>
