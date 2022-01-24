<?php

require_once('for_php7.php');

require_once('knjl312jModel.inc');
require_once('knjl312jQuery.inc');

class knjl312jController extends Controller {
    var $ModelClassName = "knjl312jModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl312j":
                    $sessionInstance->knjl312jModel();
                    $this->callView("knjl312jForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl312jCtl = new knjl312jController;
var_dump($_REQUEST);
?>
