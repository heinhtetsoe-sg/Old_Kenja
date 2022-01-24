<?php
require_once('knjl122iModel.inc');
require_once('knjl122iQuery.inc');

class knjl122iController extends Controller {
    var $ModelClassName = "knjl122iModel";
    var $ProgramID      = "KNJL122I";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                case "end":
                case "back":
                    $this->callView("knjl122iForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "replace":
                    $this->callView("knjl122iSubForm1");
                    break 2;
                case "replace_update":
                    $sessionInstance->getReplaceModel();
                    $sessionInstance->setCmd("replace");
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
$knjl122iCtl = new knjl122iController;
?>
