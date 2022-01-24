<?php

require_once('for_php7.php');

require_once('knjd186jModel.inc');
require_once('knjd186jQuery.inc');

class knjd186jController extends Controller {
    var $ModelClassName = "knjd186jModel";
    var $ProgramID      = "KNJD186J";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "clear";
                case "knjd186j";
                    $sessionInstance->knjd186jModel();
                    $this->callView("knjd186jForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd186jCtl = new knjd186jController;
?>
