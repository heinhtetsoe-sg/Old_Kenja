<?php

require_once('for_php7.php');

require_once('knjl343gModel.inc');
require_once('knjl343gQuery.inc');

class knjl343gController extends Controller {
    var $ModelClassName = "knjl343gModel";
    var $ProgramID      = "KNJL343G";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl343g":
                    $sessionInstance->knjl343gModel();
                    $this->callView("knjl343gForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl343gCtl = new knjl343gController;
?>
