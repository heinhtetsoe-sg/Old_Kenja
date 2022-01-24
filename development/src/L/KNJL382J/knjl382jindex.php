<?php

require_once('for_php7.php');

require_once('knjl382jModel.inc');
require_once('knjl382jQuery.inc');

class knjl382jController extends Controller {
    var $ModelClassName = "knjl382jModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl382j":
                    $sessionInstance->knjl382jModel();
                    $this->callView("knjl382jForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl382jCtl = new knjl382jController;
var_dump($_REQUEST);
?>
