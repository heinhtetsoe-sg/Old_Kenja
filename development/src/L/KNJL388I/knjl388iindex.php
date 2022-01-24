<?php
require_once('knjl388iModel.inc');
require_once('knjl388iQuery.inc');

class knjl388iController extends Controller {
    var $ModelClassName = "knjl388iModel";
    var $ProgramID      = "KNJL388I";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl388i":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjl388iModel();
                    $this->callView("knjl388iForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl388iCtl = new knjl388iController;
?>
