<?php

require_once('for_php7.php');

require_once('knjl320gModel.inc');
require_once('knjl320gQuery.inc');

class knjl320gController extends Controller {
    var $ModelClassName = "knjl320gModel";
    var $ProgramID      = "KNJL320G";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl320g":
                    $sessionInstance->knjl320gModel();
                    $this->callView("knjl320gForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl320gCtl = new knjl320gController;
?>
