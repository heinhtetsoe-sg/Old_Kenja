<?php

require_once('for_php7.php');

require_once('knjl337gModel.inc');
require_once('knjl337gQuery.inc');

class knjl337gController extends Controller {
    var $ModelClassName = "knjl337gModel";
    var $ProgramID      = "KNJL337G";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl337g":
                    $sessionInstance->knjl337gModel();
                    $this->callView("knjl337gForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl337gCtl = new knjl337gController;
?>
