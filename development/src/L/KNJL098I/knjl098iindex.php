<?php
require_once('knjl098iModel.inc');
require_once('knjl098iQuery.inc');

class knjl098iController extends Controller {
    var $ModelClassName = "knjl098iModel";
    var $ProgramID      = "KNJL098I";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "back":
                case "reset":
                    $this->callView("knjl098iForm1");
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
$knjl098iCtl = new knjl098iController;
?>