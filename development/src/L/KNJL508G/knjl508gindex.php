<?php

require_once('for_php7.php');

require_once('knjl508gModel.inc');
require_once('knjl508gQuery.inc');

class knjl508gController extends Controller {
    var $ModelClassName = "knjl508gModel";
    var $ProgramID      = "KNJL508G";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl508g":
                    $sessionInstance->knjl508gModel();
                    $this->callView("knjl508gForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl508gCtl = new knjl508gController;
?>
