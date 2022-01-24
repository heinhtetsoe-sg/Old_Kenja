<?php
require_once('knjl050yModel.inc');
require_once('knjl050yQuery.inc');

class knjl050yController extends Controller {
    var $ModelClassName = "knjl050yModel";
    var $ProgramID      = "KNJL050Y";

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
                    $this->callView("knjl050yForm1");
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
$knjl050yCtl = new knjl050yController;
?>
