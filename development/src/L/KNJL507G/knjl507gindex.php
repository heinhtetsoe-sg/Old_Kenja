<?php

require_once('for_php7.php');

require_once('knjl507gModel.inc');
require_once('knjl507gQuery.inc');

class knjl507gController extends Controller {
    var $ModelClassName = "knjl507gModel";
    var $ProgramID      = "KNJL507G";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl507g":
                    $sessionInstance->knjl507gModel();
                    $this->callView("knjl507gForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl507gCtl = new knjl507gController;
?>
