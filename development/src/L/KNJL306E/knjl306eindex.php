<?php

require_once('for_php7.php');

require_once('knjl306eModel.inc');
require_once('knjl306eQuery.inc');

class knjl306eController extends Controller {
    var $ModelClassName = "knjl306eModel";
    var $ProgramID      = "KNJL306E";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl306e":
                    $this->callView("knjl306eForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl306eCtl = new knjl306eController;
?>
