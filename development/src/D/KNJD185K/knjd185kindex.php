<?php

require_once('for_php7.php');

require_once('knjd185kModel.inc');
require_once('knjd185kQuery.inc');

class knjd185kController extends Controller {
    var $ModelClassName = "knjd185kModel";
    var $ProgramID      = "KNJD185K";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd185k":
                    $sessionInstance->knjd185kModel();
                    $this->callView("knjd185kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd185kCtl = new knjd185kController;
?>
