<?php

require_once('for_php7.php');

require_once('knjm250eModel.inc');
require_once('knjm250eQuery.inc');

class knjm250eController extends Controller {
    var $ModelClassName = "knjm250eModel";
    var $ProgramID      = "KNJM250E";     //プログラムID

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "chg_subclass":
                case "reset":
                case "read":
                    $this->callView("knjm250eForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm250eCtl = new knjm250eController;
?>
