<?php

require_once('for_php7.php');

require_once('knjl015dModel.inc');
require_once('knjl015dQuery.inc');

class knjl015dController extends Controller {
    var $ModelClassName = "knjl015dModel";
    var $ProgramID      = "KNJL015D";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "read":
                case "reset":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl015dForm1");
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
$knjl015dCtl = new knjl015dController;
?>
