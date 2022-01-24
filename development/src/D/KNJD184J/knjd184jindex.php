<?php

require_once('for_php7.php');

require_once('knjd184jModel.inc');
require_once('knjd184jQuery.inc');

class knjd184jController extends Controller {
    var $ModelClassName = "knjd184jModel";
    var $ProgramID      = "KNJD184J";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd184j":
                    $sessionInstance->knjd184jModel();
                    $this->callView("knjd184jForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd184jCtl = new knjd184jController;
?>
