<?php

require_once('for_php7.php');

require_once('knjl304uModel.inc');
require_once('knjl304uQuery.inc');

class knjl304uController extends Controller {
    var $ModelClassName = "knjl304uModel";
    var $ProgramID      = "KNJL304U";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl304u":
                    $this->callView("knjl304uForm1");
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
$knjl304uCtl = new knjl304uController;
?>
