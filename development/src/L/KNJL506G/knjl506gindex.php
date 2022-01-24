<?php

require_once('for_php7.php');

require_once('knjl506gModel.inc');
require_once('knjl506gQuery.inc');

class knjl506gController extends Controller {
    var $ModelClassName = "knjl506gModel";
    var $ProgramID      = "KNJL506G";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl506g":
                    $sessionInstance->knjl506gModel();
                    $this->callView("knjl506gForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl506gCtl = new knjl506gController;
?>
