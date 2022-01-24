<?php

require_once('for_php7.php');

require_once('knjz431Model.inc');
require_once('knjz431Query.inc');

class knjz431Controller extends Controller {
    var $ModelClassName = "knjz431Model";
    var $ProgramID      = "KNJZ431";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "execute":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "edit":
                    $this->callView("knjz431Form");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz431Ctl = new knjz431Controller;
?>
