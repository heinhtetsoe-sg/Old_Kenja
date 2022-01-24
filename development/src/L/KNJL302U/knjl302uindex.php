<?php

require_once('for_php7.php');

require_once('knjl302uModel.inc');
require_once('knjl302uQuery.inc');

class knjl302uController extends Controller {
    var $ModelClassName = "knjl302uModel";
    var $ProgramID      = "KNJL302U";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl302u":
                    $this->callView("knjl302uForm1");
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
$knjl302uCtl = new knjl302uController;
?>
