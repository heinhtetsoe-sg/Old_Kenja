<?php

require_once('for_php7.php');

require_once('knjz068fModel.inc');
require_once('knjz068fQuery.inc');

class knjz068fController extends Controller {
    var $ModelClassName = "knjz068fModel";
    var $ProgramID      = "KNJZ068F";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "clear";
                case "knjz068f";
                    $sessionInstance->knjz068fModel();
                    $this->callView("knjz068fForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz068fCtl = new knjz068fController;
?>
