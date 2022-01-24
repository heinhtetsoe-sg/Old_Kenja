<?php

require_once('for_php7.php');

require_once('knjd425_1Model.inc');
require_once('knjd425_1Query.inc');

class knjd425_1Controller extends Controller {
    var $ModelClassName = "knjd425_1Model";
    var $ProgramID      = "KNJD425_1";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "subform1":
                case "subform1A":
                case "subform1_clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjd425_1Form1");
                    break 2;
                case "update1":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knjd425_1Form1", $sessionInstance->auth);
                    $sessionInstance->getUpdateModel1();
                    $sessionInstance->setCmd("subform1");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("subform1");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $this->callView("knjd425_1Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd425_1Ctl = new knjd425_1Controller;
?>
