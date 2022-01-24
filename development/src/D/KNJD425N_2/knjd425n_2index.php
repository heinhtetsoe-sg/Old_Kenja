<?php

require_once('for_php7.php');

require_once('knjd425n_2Model.inc');
require_once('knjd425n_2Query.inc');

class knjd425n_2Controller extends Controller {
    var $ModelClassName = "knjd425n_2Model";
    var $ProgramID      = "KNJD425N_2";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "subform":
                    $this->callView("knjd425n_2Form1");
                    break 2;
                case "zittai":
                    $this->callView("knjd425n_2Zittai");
                    break 2;
                case "ziritu":
                    $this->callView("knjd425n_2Ziritu");
                    break 2;
                case "nenkan":
                    $this->callView("knjd425n_2Nenkan");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knjd425n_2Form1", $sessionInstance->auth);
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
                    $this->callView("knjd425n_2Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd425n_2Ctl = new knjd425n_2Controller;
?>
