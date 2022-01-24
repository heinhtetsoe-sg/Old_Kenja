<?php

require_once('for_php7.php');

require_once('knjl371jModel.inc');
require_once('knjl371jQuery.inc');

class knjl371jController extends Controller {
    var $ModelClassName = "knjl371jModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl371j":
                    $sessionInstance->knjl371jModel();
                    $this->callView("knjl371jForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl371jCtl = new knjl371jController;
var_dump($_REQUEST);
?>
