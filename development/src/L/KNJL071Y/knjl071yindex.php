<?php

require_once('for_php7.php');

require_once('knjl071yModel.inc');
require_once('knjl071yQuery.inc');

class knjl071yController extends Controller {
    var $ModelClassName = "knjl071yModel";
    var $ProgramID      = "KNJL071Y";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "app":
                case "main":
                case "read":
                case "back":
                case "next":
                case "reset":
                    $this->callView("knjl071yForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
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
$knjl071yCtl = new knjl071yController;
?>
