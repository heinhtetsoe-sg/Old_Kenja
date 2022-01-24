<?php

require_once('for_php7.php');

require_once('knjg045eModel.inc');
require_once('knjg045eQuery.inc');

class knjg045eController extends Controller {
    var $ModelClassName = "knjg045eModel";
    var $ProgramID      = "KNJG045E";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjg045eForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
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
$knjg045eCtl = new knjg045eController;
//var_dump($_REQUEST);
?>
