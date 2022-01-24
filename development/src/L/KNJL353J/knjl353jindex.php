<?php

require_once('for_php7.php');

require_once('knjl353jModel.inc');
require_once('knjl353jQuery.inc');

class knjl353jController extends Controller {
    var $ModelClassName = "knjl353jModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl353j":
                    $sessionInstance->knjl353jModel();
                    $this->callView("knjl353jForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl353jCtl = new knjl353jController;
var_dump($_REQUEST);
?>
