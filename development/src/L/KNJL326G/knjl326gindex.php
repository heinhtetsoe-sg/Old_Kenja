<?php

require_once('for_php7.php');

require_once('knjl326gModel.inc');
require_once('knjl326gQuery.inc');

class knjl326gController extends Controller {
    var $ModelClassName = "knjl326gModel";
    var $ProgramID      = "KNJL326G";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl326g":
                    $sessionInstance->knjl326gModel();
                    $this->callView("knjl326gForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl326gCtl = new knjl326gController;
?>
