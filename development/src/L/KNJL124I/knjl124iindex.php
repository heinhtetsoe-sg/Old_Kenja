<?php
require_once('knjl124iModel.inc');
require_once('knjl124iQuery.inc');

class knjl124iController extends Controller {
    var $ModelClassName = "knjl124iModel";
    var $ProgramID      = "KNJL124I";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "edit":
                case "back":
                case "reset":
                    $this->callView("knjl124iForm1");
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
$knjl124iCtl = new knjl124iController;
?>