<?php

require_once('for_php7.php');

require_once('knjl324gModel.inc');
require_once('knjl324gQuery.inc');

class knjl324gController extends Controller {
    var $ModelClassName = "knjl324gModel";
    var $ProgramID      = "KNJL324G";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl324g":
                    $sessionInstance->knjl324gModel();
                    $this->callView("knjl324gForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl324gCtl = new knjl324gController;
?>
