<?php

require_once('for_php7.php');

require_once('knjl327gModel.inc');
require_once('knjl327gQuery.inc');

class knjl327gController extends Controller {
    var $ModelClassName = "knjl327gModel";
    var $ProgramID      = "KNJL327G";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl327g":
                    $sessionInstance->knjl327gModel();
                    $this->callView("knjl327gForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl327gCtl = new knjl327gController;
?>
