<?php

require_once('for_php7.php');

require_once('knjl501gModel.inc');
require_once('knjl501gQuery.inc');

class knjl501gController extends Controller {
    var $ModelClassName = "knjl501gModel";
    var $ProgramID      = "KNJL501G";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl501g":
                    $sessionInstance->knjl501gModel();
                    $this->callView("knjl501gForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl501gCtl = new knjl501gController;
?>
