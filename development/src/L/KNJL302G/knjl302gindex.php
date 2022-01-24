<?php

require_once('for_php7.php');

require_once('knjl302gModel.inc');
require_once('knjl302gQuery.inc');

class knjl302gController extends Controller {
    var $ModelClassName = "knjl302gModel";
    var $ProgramID      = "KNJL302G";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl302g":
                    $sessionInstance->knjl302gModel();
                    $this->callView("knjl302gForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl302gCtl = new knjl302gController;
?>
