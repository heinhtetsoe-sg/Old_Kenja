<?php

require_once('for_php7.php');

require_once('knjl307jModel.inc');
require_once('knjl307jQuery.inc');

class knjl307jController extends Controller {
    var $ModelClassName = "knjl307jModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl307j":
                    $sessionInstance->knjl307jModel();
                    $this->callView("knjl307jForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl307jCtl = new knjl307jController;
var_dump($_REQUEST);
?>
