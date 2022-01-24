<?php

require_once('for_php7.php');

require_once('knjl627fModel.inc');
require_once('knjl627fQuery.inc');

class knjl627fController extends Controller {
    var $ModelClassName = "knjl627fModel";
    var $ProgramID      = "KNJL627F";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl627f":
                    $sessionInstance->knjl627fModel();
                    $this->callView("knjl627fForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl627fCtl = new knjl627fController;
?>
