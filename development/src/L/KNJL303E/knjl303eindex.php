<?php

require_once('for_php7.php');

require_once('knjl303eModel.inc');
require_once('knjl303eQuery.inc');

class knjl303eController extends Controller {
    var $ModelClassName = "knjl303eModel";
    var $ProgramID      = "KNJL303E";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl303e":
                    $this->callView("knjl303eForm1");
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
$knjl303eCtl = new knjl303eController;
?>
