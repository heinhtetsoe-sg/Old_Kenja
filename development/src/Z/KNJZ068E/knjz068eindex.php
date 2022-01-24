<?php

require_once('for_php7.php');

require_once('knjz068eModel.inc');
require_once('knjz068eQuery.inc');

class knjz068eController extends Controller {
    var $ModelClassName = "knjz068eModel";
    var $ProgramID      = "KNJZ068E";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "clear";
                case "knjz068e";
                    $sessionInstance->knjz068eModel();
                    $this->callView("knjz068eForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz068eCtl = new knjz068eController;
?>
