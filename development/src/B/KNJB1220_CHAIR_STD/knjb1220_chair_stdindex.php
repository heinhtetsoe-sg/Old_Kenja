<?php

require_once('for_php7.php');

require_once('knjb1220_chair_stdModel.inc');
require_once('knjb1220_chair_stdQuery.inc');

class knjb1220_chair_stdController extends Controller {
    var $ModelClassName = "knjb1220_chair_stdModel";
    var $ProgramID      = "KNJB1220_CHAIR_STD";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "subMain":
                case "clear";
                    $sessionInstance->knjb1220_chair_stdModel();
                    $this->callView("knjb1220_chair_stdForm1");
                    exit;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("subMain");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb1220_chair_stdCtl = new knjb1220_chair_stdController;
?>
