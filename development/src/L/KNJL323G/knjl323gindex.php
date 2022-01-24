<?php

require_once('for_php7.php');

require_once('knjl323gModel.inc');
require_once('knjl323gQuery.inc');

class knjl323gController extends Controller {
    var $ModelClassName = "knjl323gModel";
    var $ProgramID      = "KNJL323G";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl323g":
                    $sessionInstance->knjl323gModel();
                    $this->callView("knjl323gForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl323gCtl = new knjl323gController;
?>
