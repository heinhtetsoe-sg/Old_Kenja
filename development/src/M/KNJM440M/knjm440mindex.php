<?php

require_once('for_php7.php');

require_once('knjm440mModel.inc');
require_once('knjm440mQuery.inc');

class knjm440mController extends Controller {
    var $ModelClassName = "knjm440mModel";
    var $ProgramID      = "KNJM440M";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                case "main":
                    $this->callView("knjm440mForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm440mCtl = new knjm440mController;
?>
