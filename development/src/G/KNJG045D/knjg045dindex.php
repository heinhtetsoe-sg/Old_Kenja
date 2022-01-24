<?php

require_once('for_php7.php');

require_once('knjg045dModel.inc');
require_once('knjg045dQuery.inc');

class knjg045dController extends Controller {
    var $ModelClassName = "knjg045dModel";
    var $ProgramID      = "KNJG045D";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjg045dForm1");
                    break 2;
                case "shutcho":
                case "shutcho-A":
                case "kyuka":
                case "kyuka-A":
                case "sub":
                    $this->callView("knjg045dSubForm1");
                    break 2;
                case "update":
                case "delete":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "update_detail":
                    $sessionInstance->getUpdateDetailSeqModel();
                    $sessionInstance->setCmd($sessionInstance->cmd);
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjg045dCtl = new knjg045dController;
//var_dump($_REQUEST);
?>
