<?php

require_once('for_php7.php');

require_once('knjl072yModel.inc');
require_once('knjl072yQuery.inc');

class knjl072yController extends Controller {
    var $ModelClassName = "knjl072yModel";
    var $ProgramID      = "KNJL072Y";

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
                    $this->callView("knjl072yForm1");
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
$knjl072yCtl = new knjl072yController;
?>
