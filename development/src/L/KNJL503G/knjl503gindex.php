<?php

require_once('for_php7.php');

require_once('knjl503gModel.inc');
require_once('knjl503gQuery.inc');

class knjl503gController extends Controller {
    var $ModelClassName = "knjl503gModel";
    var $ProgramID      = "KNJL503G";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl503g":
                    $sessionInstance->knjl503gModel();
                    $this->callView("knjl503gForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl503gCtl = new knjl503gController;
?>
