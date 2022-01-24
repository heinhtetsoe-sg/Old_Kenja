<?php

require_once('for_php7.php');

require_once('knjl072cModel.inc');
require_once('knjl072cQuery.inc');

class knjl072cController extends Controller {
    var $ModelClassName = "knjl072cModel";
    var $ProgramID      = "KNJL072C";

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
                case "search":
                    $this->callView("knjl072cForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                case "":
                    $this->callView("knjl072cForm1");
                    exit;
//                    $sessionInstance->setCmd("main");
//                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl072cCtl = new knjl072cController;
?>
