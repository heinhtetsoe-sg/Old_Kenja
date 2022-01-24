<?php

require_once('for_php7.php');

require_once('knjz440Model.inc');
require_once('knjz440Query.inc');

class knjz440Controller extends Controller {
    var $ModelClassName = "knjz440Model";
    var $ProgramID      = "KNJZ440";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                    $this->callView("knjz440Form1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $this->callView("knjz440Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$KNJZ440Ctl = new knjz440Controller;
//var_dump($_REQUEST);
?>
