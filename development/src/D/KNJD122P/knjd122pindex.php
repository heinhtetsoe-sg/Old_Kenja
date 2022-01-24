<?php

require_once('for_php7.php');

require_once('knjd122pModel.inc');
require_once('knjd122pQuery.inc');

class knjd122pController extends Controller {
    var $ModelClassName = "knjd122pModel";
    var $ProgramID      = "KNJD122P";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd122p":
                    $sessionInstance->knjd122pModel();
                    $this->callView("knjd122pForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd122pCtl = new knjd122pController;
?>
