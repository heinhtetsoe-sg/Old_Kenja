<?php

require_once('for_php7.php');

require_once('knjl314gModel.inc');
require_once('knjl314gQuery.inc');

class knjl314gController extends Controller {
    var $ModelClassName = "knjl314gModel";
    var $ProgramID      = "KNJL314G";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl314g":
                    $sessionInstance->knjl314gModel();
                    $this->callView("knjl314gForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl314gCtl = new knjl314gController;
?>
