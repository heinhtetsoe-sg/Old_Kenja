<?php

require_once('for_php7.php');

require_once('knjl321eModel.inc');
require_once('knjl321eQuery.inc');

class knjl321eController extends Controller {
    var $ModelClassName = "knjl321eModel";
    var $ProgramID      = "KNJL321E";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl321e":
                    $this->callView("knjl321eForm1");
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
$knjl321eCtl = new knjl321eController;
?>
