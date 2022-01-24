<?php

require_once('for_php7.php');

require_once('knjl304eModel.inc');
require_once('knjl304eQuery.inc');

class knjl304eController extends Controller {
    var $ModelClassName = "knjl304eModel";
    var $ProgramID      = "KNJL304E";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl304e":
                    $this->callView("knjl304eForm1");
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
$knjl304eCtl = new knjl304eController;
?>
