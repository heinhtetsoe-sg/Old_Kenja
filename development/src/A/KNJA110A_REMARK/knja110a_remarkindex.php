<?php

require_once('for_php7.php');

require_once('knja110a_remarkModel.inc');
require_once('knja110a_remarkQuery.inc');

class knja110a_remarkController extends Controller {
    var $ModelClassName = "knja110a_remarkModel";
    var $ProgramID      = "KNJA110A_REMARK";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "subform3":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knja110a_remarkForm1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knja110a_remarkForm1", $sessionInstance->auth);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("subform3");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("subform3");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $this->callView("knja110a_remarkForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja110a_remarkCtl = new knja110a_remarkController;
?>
