<?php

require_once('for_php7.php');

require_once('knjd425l_3Model.inc');
require_once('knjd425l_3Query.inc');

class knjd425l_3Controller extends Controller {
    var $ModelClassName = "knjd425l_3Model";
    var $ProgramID      = "KNJD425L_3";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "edit":
                case "check":
                    $this->callView("knjd425l_3Form1");
                    break 2;
                case "zittai":
                    $this->callView("knjd425l_3Zittai");
                    break 2;
                case "nenkan":
                    $this->callView("knjd425l_3Nenkan");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knjd425l_3Form1", $sessionInstance->auth);
                    $sessionInstance->getUpdateModel();
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
$knjd425l_3Ctl = new knjd425l_3Controller;
?>
