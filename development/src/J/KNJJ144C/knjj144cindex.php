<?php

require_once('for_php7.php');

require_once('knjj144cModel.inc');
require_once('knjj144cQuery.inc');

class knjj144cController extends Controller {
    var $ModelClassName = "knjj144cModel";
    var $ProgramID      = "KNJJ144C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "changeCmb":
                case "checkAttendCd":
                case "reset":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $this->callView("knjj144cForm1");
                   break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID); 
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                case "":
                    $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjj144cCtl = new knjj144cController;
//var_dump($_REQUEST);
?>
