<?php

require_once('for_php7.php');

require_once('knjl509gModel.inc');
require_once('knjl509gQuery.inc');

class knjl509gController extends Controller {
    var $ModelClassName = "knjl509gModel";
    var $ProgramID      = "KNJL509G";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl509g":
                    $sessionInstance->knjl509gModel();
                    $this->callView("knjl509gForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl509gCtl = new knjl509gController;
?>
