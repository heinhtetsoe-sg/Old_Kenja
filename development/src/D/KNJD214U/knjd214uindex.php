<?php

require_once('for_php7.php');

require_once('knjd214uModel.inc');
require_once('knjd214uQuery.inc');

class knjd214uController extends Controller {
    var $ModelClassName = "knjd214uModel";
    var $ProgramID      = "KNJD214U";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "copy":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID); 
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("knjd214u");
                    break 1;
                case "clear":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID); 
                    $sessionInstance->getClearModel();
                    $sessionInstance->setCmd("knjd214u");
                    break 1;
                case "del_rireki":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID); 
                    $sessionInstance->getDeleteRirekiModel();
                    $sessionInstance->setCmd("knjd214u");
                    break 1;
                case "":
                case "knjd214u":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd214uModel();
                    $this->callView("knjd214uForm1");
                    exit;
                case "semechg":
                    $sessionInstance->knjd214uModel();
                    $this->callView("knjd214uForm1");
                    exit;
                case "gakki":
                    $sessionInstance->knjd214uModel();
                    $this->callView("knjd214uForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd214uCtl = new knjd214uController;
var_dump($_REQUEST);
?>
