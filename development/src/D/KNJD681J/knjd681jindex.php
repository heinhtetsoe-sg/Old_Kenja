<?php

require_once('for_php7.php');

require_once('knjd681jModel.inc');
require_once('knjd681jQuery.inc');

class knjd681jController extends Controller {
    var $ModelClassName = "knjd681jModel";
    var $ProgramID      = "KNJD681J";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd681j":
                    $sessionInstance->knjd681jModel();
                    $this->callView("knjd681jForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd681jCtl = new knjd681jController;
?>
