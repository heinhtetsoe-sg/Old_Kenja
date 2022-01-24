<?php

require_once('for_php7.php');

require_once('knjl341uModel.inc');
require_once('knjl341uQuery.inc');

class knjl341uController extends Controller {
    var $ModelClassName = "knjl341uModel";
    var $ProgramID      = "KNJL341U";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl341u":
                    $this->callView("knjl341uForm1");
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
$knjl341uCtl = new knjl341uController;
?>
