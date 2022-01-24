<?php

require_once('for_php7.php');

require_once('knjl328nModel.inc');
require_once('knjl328nQuery.inc');

class knjl328nController extends Controller {
    var $ModelClassName = "knjl328nModel";
    var $ProgramID      = "KNJL328N";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl328n":
                    $sessionInstance->knjl328nModel();
                    $this->callView("knjl328nForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl328nCtl = new knjl328nController;
?>
