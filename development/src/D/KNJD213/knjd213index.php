<?php

require_once('for_php7.php');

require_once('knjd213Model.inc');
require_once('knjd213Query.inc');

class knjd213Controller extends Controller {
    var $ModelClassName = "knjd213Model";
    var $ProgramID      = "KNJD213";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "copy":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("knjd213");
                    break 1;
                case "clear":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getClearModel();
                    $sessionInstance->setCmd("knjd213");
                    break 1;
                case "":
                case "knjd213":
                case "semechg":
                    $sessionInstance->knjd213Model();
                    $this->callView("knjd213Form1");
                    exit;
                case "gakki":
                    $sessionInstance->knjd213Model();
                    $this->callView("knjd213Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd213Ctl = new knjd213Controller;
var_dump($_REQUEST);
?>
