<?php

require_once('for_php7.php');

require_once('knjl345gModel.inc');
require_once('knjl345gQuery.inc');

class knjl345gController extends Controller {
    var $ModelClassName = "knjl345gModel";
    var $ProgramID      = "KNJL345G";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl345g":
                    $sessionInstance->knjl345gModel();
                    $this->callView("knjl345gForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl345gCtl = new knjl345gController;
?>
