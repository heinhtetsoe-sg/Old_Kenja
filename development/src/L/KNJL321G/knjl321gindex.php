<?php

require_once('for_php7.php');

require_once('knjl321gModel.inc');
require_once('knjl321gQuery.inc');

class knjl321gController extends Controller {
    var $ModelClassName = "knjl321gModel";
    var $ProgramID      = "KNJL321G";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl321g":
                    $sessionInstance->knjl321gModel();
                    $this->callView("knjl321gForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl321gCtl = new knjl321gController;
?>
