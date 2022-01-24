<?php

require_once('for_php7.php');

require_once('knjl335uModel.inc');
require_once('knjl335uQuery.inc');

class knjl335uController extends Controller {
    var $ModelClassName = "knjl335uModel";
    var $ProgramID      = "KNJL335U";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl335u":
                    $this->callView("knjl335uForm1");
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
$knjl335uCtl = new knjl335uController;
?>
