<?php

require_once('for_php7.php');

require_once('knjp374Model.inc');
require_once('knjp374Query.inc');

class knjp374Controller extends Controller {
    var $ModelClassName = "knjp374Model";
    var $ProgramID      = "KNJP374";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "execute":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "edit":
                case "chngCmb":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjp374Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp374Ctl = new knjp374Controller;
?>
