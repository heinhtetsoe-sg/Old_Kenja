<?php

require_once('for_php7.php');

require_once('knjm435dModel.inc');
require_once('knjm435dQuery.inc');

class knjm435dController extends Controller {
    var $ModelClassName = "knjm435dModel";
    var $ProgramID      = "KNJM435D";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "copy":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("knjm435d");
                    break 1;
                case "clear":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getClearModel();
                    $sessionInstance->setCmd("knjm435d");
                    break 1;
                case "":
                case "knjm435d":
                case "semechg":
                    $sessionInstance->knjm435dModel();
                    $this->callView("knjm435dForm1");
                    exit;
                case "gakki":
                    $sessionInstance->knjm435dModel();
                    $this->callView("knjm435dForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm435dCtl = new knjm435dController;
var_dump($_REQUEST);
?>
