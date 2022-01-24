<?php

require_once('for_php7.php');

require_once('knjl344gModel.inc');
require_once('knjl344gQuery.inc');

class knjl344gController extends Controller {
    var $ModelClassName = "knjl344gModel";
    var $ProgramID      = "KNJL344G";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl344g":
                    $sessionInstance->knjl344gModel();
                    $this->callView("knjl344gForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl344gCtl = new knjl344gController;
?>
