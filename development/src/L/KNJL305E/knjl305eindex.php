<?php

require_once('for_php7.php');

require_once('knjl305eModel.inc');
require_once('knjl305eQuery.inc');

class knjl305eController extends Controller {
    var $ModelClassName = "knjl305eModel";
    var $ProgramID      = "KNJL305E";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl305e":
                    $this->callView("knjl305eForm1");
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
$knjl305eCtl = new knjl305eController;
?>
