<?php

require_once('for_php7.php');

require_once('knjd666jModel.inc');
require_once('knjd666jQuery.inc');

class knjd666jController extends Controller {
    var $ModelClassName = "knjd666jModel";
    var $ProgramID      = "KNJD666J";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd666j":
                    $sessionInstance->knjd666jModel();
                    $this->callView("knjd666jForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd666jCtl = new knjd666jController;
?>
