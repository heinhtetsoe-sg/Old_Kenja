<?php

require_once('for_php7.php');

require_once('knjl505gModel.inc');
require_once('knjl505gQuery.inc');

class knjl505gController extends Controller {
    var $ModelClassName = "knjl505gModel";
    var $ProgramID      = "KNJL505G";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl505g":
                    $sessionInstance->knjl505gModel();
                    $this->callView("knjl505gForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl505gCtl = new knjl505gController;
?>
