<?php

require_once('for_php7.php');

require_once('knjl502gModel.inc');
require_once('knjl502gQuery.inc');

class knjl502gController extends Controller {
    var $ModelClassName = "knjl502gModel";
    var $ProgramID      = "KNJL502G";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl502g":
                    $sessionInstance->knjl502gModel();
                    $this->callView("knjl502gForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl502gCtl = new knjl502gController;
?>
