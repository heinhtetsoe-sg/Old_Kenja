<?php

require_once('for_php7.php');

require_once('knjm434mModel.inc');
require_once('knjm434mQuery.inc');

class knjm434mController extends Controller {
    var $ModelClassName = "knjm434mModel";
    var $ProgramID      = "KNJM434M";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "copy":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("knjm434m");
                    break 1;
                case "clear":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getClearModel();
                    $sessionInstance->setCmd("knjm434m");
                    break 1;
                case "":
                case "knjm434m":
                case "semechg":
                    $sessionInstance->knjm434mModel();
                    $this->callView("knjm434mForm1");
                    exit;
                case "gakki":
                    $sessionInstance->knjm434mModel();
                    $this->callView("knjm434mForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm434mCtl = new knjm434mController;
var_dump($_REQUEST);
?>
