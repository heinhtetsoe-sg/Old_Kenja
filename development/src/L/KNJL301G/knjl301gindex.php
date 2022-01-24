<?php

require_once('for_php7.php');

require_once('knjl301gModel.inc');
require_once('knjl301gQuery.inc');

class knjl301gController extends Controller {
    var $ModelClassName = "knjl301gModel";
    var $ProgramID      = "KNJL301G";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl301g":
                    $sessionInstance->knjl301gModel();
                    $this->callView("knjl301gForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl301gCtl = new knjl301gController;
?>
