<?php

require_once('for_php7.php');

require_once('knjl343wModel.inc');
require_once('knjl343wQuery.inc');

class knjl343wController extends Controller {
    var $ModelClassName = "knjl343wModel";
    var $ProgramID      = "KNJL340W";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                //CSV出力
                case "csv":
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl343wForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl343wForm1");
                    break 2;
                case "houkoku":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateEdboardModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl343wCtl = new knjl343wController;
?>
