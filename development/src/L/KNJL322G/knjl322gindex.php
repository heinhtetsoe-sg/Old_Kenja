<?php

require_once('for_php7.php');

require_once('knjl322gModel.inc');
require_once('knjl322gQuery.inc');

class knjl322gController extends Controller {
    var $ModelClassName = "knjl322gModel";
    var $ProgramID      = "KNJL322G";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl322g":
                    $sessionInstance->knjl322gModel();
                    $this->callView("knjl322gForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl322gCtl = new knjl322gController;
?>
