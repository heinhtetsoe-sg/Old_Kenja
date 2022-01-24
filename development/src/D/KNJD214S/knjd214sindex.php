<?php

require_once('for_php7.php');

require_once('knjd214sModel.inc');
require_once('knjd214sQuery.inc');

class knjd214sController extends Controller {
    var $ModelClassName = "knjd214sModel";
    var $ProgramID      = "KNJD214S";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "copy":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID); 
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("knjd214s");
                    break 1;
                case "clear":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID); 
                    $sessionInstance->getClearModel();
                    $sessionInstance->setCmd("knjd214s");
                    break 1;
                case "del_rireki":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID); 
                    $sessionInstance->getDeleteRirekiModel();
                    $sessionInstance->setCmd("knjd214s");
                    break 1;
                case "":
                case "knjd214s":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd214sModel();
                    $this->callView("knjd214sForm1");
                    exit;
                case "semechg":
                    $sessionInstance->knjd214sModel();
                    $this->callView("knjd214sForm1");
                    exit;
                case "gakki":
                    $sessionInstance->knjd214sModel();
                    $this->callView("knjd214sForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd214sCtl = new knjd214sController;
var_dump($_REQUEST);
?>
