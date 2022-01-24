<?php

require_once('for_php7.php');

require_once('knjd185hModel.inc');
require_once('knjd185hQuery.inc');

class knjd185hController extends Controller {
    var $ModelClassName = "knjd185hModel";
    var $ProgramID      = "KNJD185H";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd185h":
                    $sessionInstance->knjd185hModel();
                    $this->callView("knjd185hForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd185hCtl = new knjd185hController;
?>
