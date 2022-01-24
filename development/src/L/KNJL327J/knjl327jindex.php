<?php

require_once('for_php7.php');

require_once('knjl327jModel.inc');
require_once('knjl327jQuery.inc');

class knjl327jController extends Controller {
    var $ModelClassName = "knjl327jModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl327j":
                    $sessionInstance->knjl327jModel();
                    $this->callView("knjl327jForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl327jCtl = new knjl327jController;
var_dump($_REQUEST);
?>
