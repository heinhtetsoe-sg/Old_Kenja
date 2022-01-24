<?php

require_once('for_php7.php');

require_once('knjd669jModel.inc');
require_once('knjd669jQuery.inc');

class knjd669jController extends Controller {
    var $ModelClassName = "knjd669jModel";
    var $ProgramID      = "KNJD669J";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd669jChg":
                case "knjd669j":
                    $sessionInstance->knjd669jModel();
                    $this->callView("knjd669jForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd669jCtl = new knjd669jController;
?>
