<?php

require_once('for_php7.php');

require_once('knjl305dModel.inc');
require_once('knjl305dQuery.inc');

class knjl305dController extends Controller {
    var $ModelClassName = "knjl305dModel";
    var $ProgramID      = "KNJL305D";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl305d":
                    $this->callView("knjl305dForm1");
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
$knjl305dCtl = new knjl305dController;
?>
