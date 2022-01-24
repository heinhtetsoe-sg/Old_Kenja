<?php

require_once('for_php7.php');

require_once('knjl311gModel.inc');
require_once('knjl311gQuery.inc');

class knjl311gController extends Controller {
    var $ModelClassName = "knjl311gModel";
    var $ProgramID      = "KNJL311G";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl311g":
                    $sessionInstance->knjl311gModel();
                    $this->callView("knjl311gForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl311gCtl = new knjl311gController;
?>
