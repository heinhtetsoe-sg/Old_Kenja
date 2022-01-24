<?php

require_once('for_php7.php');

require_once('knjl303dModel.inc');
require_once('knjl303dQuery.inc');

class knjl303dController extends Controller {
    var $ModelClassName = "knjl303dModel";
    var $ProgramID      = "KNJL303D";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl303d":
                    $this->callView("knjl303dForm1");
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
$knjl303dCtl = new knjl303dController;
?>
