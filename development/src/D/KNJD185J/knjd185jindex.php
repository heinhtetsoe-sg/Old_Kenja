<?php

require_once('for_php7.php');

require_once('knjd185jModel.inc');
require_once('knjd185jQuery.inc');

class knjd185jController extends Controller {
    var $ModelClassName = "knjd185jModel";
    var $ProgramID      = "KNJD185J";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd185j":
                    $sessionInstance->knjd185jModel();
                    $this->callView("knjd185jForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd185jCtl = new knjd185jController;
?>
