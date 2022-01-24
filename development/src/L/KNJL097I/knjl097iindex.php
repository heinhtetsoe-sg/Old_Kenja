<?php
require_once('knjl097iModel.inc');
require_once('knjl097iQuery.inc');

class knjl097iController extends Controller {
    var $ModelClassName = "knjl097iModel";
    var $ProgramID      = "KNJL097I";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "judge":
                case "back":
                case "reset":
                    $this->callView("knjl097iForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
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
$knjl097iCtl = new knjl097iController;
?>