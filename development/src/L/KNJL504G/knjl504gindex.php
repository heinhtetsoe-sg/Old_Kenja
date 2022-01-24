<?php

require_once('for_php7.php');

require_once('knjl504gModel.inc');
require_once('knjl504gQuery.inc');

class knjl504gController extends Controller {
    var $ModelClassName = "knjl504gModel";
    var $ProgramID      = "KNJL504G";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl504g":
                    $sessionInstance->knjl504gModel();
                    $this->callView("knjl504gForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl504gCtl = new knjl504gController;
?>
