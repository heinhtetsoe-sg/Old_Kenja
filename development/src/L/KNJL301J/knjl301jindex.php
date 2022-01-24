<?php

require_once('for_php7.php');

require_once('knjl301jModel.inc');
require_once('knjl301jQuery.inc');

class knjl301jController extends Controller {
    var $ModelClassName = "knjl301jModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl301j":
                    $sessionInstance->knjl301jModel();
                    $this->callView("knjl301jForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl301jCtl = new knjl301jController;
var_dump($_REQUEST);
?>
