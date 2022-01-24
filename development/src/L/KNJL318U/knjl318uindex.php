<?php

require_once('for_php7.php');

require_once('knjl318uModel.inc');
require_once('knjl318uQuery.inc');

class knjl318uController extends Controller {
    var $ModelClassName = "knjl318uModel";
    var $ProgramID      = "KNJL318U";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl318u":
                    $this->callView("knjl318uForm1");
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
$knjl318uCtl = new knjl318uController;
?>
