<?php

require_once('for_php7.php');

require_once('knjl020yModel.inc');
require_once('knjl020yQuery.inc');

class knjl020yController extends Controller {
    var $ModelClassName = "knjl020yModel";
    var $ProgramID      = "KNJL020Y";     //プログラムID

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "read":
                case "back":
                case "next":
                case "reset":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl020yForm1");
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
$knjl020yCtl = new knjl020yController;
?>
