<?php

require_once('for_php7.php');

require_once('knjm434wModel.inc');
require_once('knjm434wQuery.inc');

class knjm434wController extends Controller {
    var $ModelClassName = "knjm434wModel";
    var $ProgramID      = "KNJM434W";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "copy":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("knjm434w");
                    break 1;
                case "clear":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getClearModel();
                    $sessionInstance->setCmd("knjm434w");
                    break 1;
                case "":
                case "knjm434w":
                case "semechg":
                    $sessionInstance->knjm434wModel();
                    $this->callView("knjm434wForm1");
                    exit;
                case "gakki":
                    $sessionInstance->knjm434wModel();
                    $this->callView("knjm434wForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm434wCtl = new knjm434wController;
var_dump($_REQUEST);
?>
