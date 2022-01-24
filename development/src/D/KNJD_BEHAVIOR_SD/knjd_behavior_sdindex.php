<?php

require_once('for_php7.php');

require_once('knjd_behavior_sdModel.inc');
require_once('knjd_behavior_sdQuery.inc');

class knjd_behavior_sdController extends Controller {
    var $ModelClassName = "knjd_behavior_sdModel";
    var $ProgramID      = "KNJD_BEHAVIOR_SD";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("updEdit2");
                    break 1;
                case "form1":
                case "edit":
                case "updEdit2":
                case "clear":
                    $this->callView("knjd_behavior_sdForm1");
                    break 2;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->knjd_behavior_sdModel();
                    $this->callView("knjd_behavior_sdForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd_behavior_sdCtl = new knjd_behavior_sdController;
?>
