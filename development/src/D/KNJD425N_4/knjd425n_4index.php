<?php

require_once('for_php7.php');

require_once('knjd425n_4Model.inc');
require_once('knjd425n_4Query.inc');

class knjd425n_4Controller extends Controller {
    var $ModelClassName = "knjd425n_4Model";
    var $ProgramID      = "KNJD425N_4";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "subform":
                    $this->callView("knjd425n_4Form1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knjd425n_4Form1", $sessionInstance->auth);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("subform");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("subform");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $this->callView("knjd425n_4Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd425n_4Ctl = new knjd425n_4Controller;
?>
