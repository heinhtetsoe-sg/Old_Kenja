<?php

require_once('for_php7.php');

require_once('knjd_behavior_lmModel.inc');
require_once('knjd_behavior_lmQuery.inc');

class knjd_behavior_lmController extends Controller {
    var $ModelClassName = "knjd_behavior_lmModel";
    var $ProgramID      = "KNJD_BEHAVIOR_LM";

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
                    $this->callView("knjd_behavior_lmForm1");
                    break 2;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->knjd_behavior_lmModel();
                    $this->callView("knjd_behavior_lmForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd_behavior_lmCtl = new knjd_behavior_lmController;
?>
