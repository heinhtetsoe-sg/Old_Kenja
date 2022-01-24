<?php

require_once('for_php7.php');

require_once('knjd679jModel.inc');
require_once('knjd679jQuery.inc');

class knjd679jController extends Controller {
    var $ModelClassName = "knjd679jModel";
    var $ProgramID      = "KNJD679J";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd679j":
                    $sessionInstance->knjd679jModel();
                    $this->callView("knjd679jForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd679jCtl = new knjd679jController;
?>
