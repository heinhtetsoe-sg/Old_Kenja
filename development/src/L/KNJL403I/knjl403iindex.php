<?php
require_once('knjl403iModel.inc');
require_once('knjl403iQuery.inc');

class knjl403iController extends Controller {
    var $ModelClassName = "knjl403iModel";
    var $ProgramID      = "KNJL403I";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl403i":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjl403iModel();
                    $this->callView("knjl403iForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl403iCtl = new knjl403iController;
?>
