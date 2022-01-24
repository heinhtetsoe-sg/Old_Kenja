<?php

require_once('for_php7.php');

require_once('knjl370jModel.inc');
require_once('knjl370jQuery.inc');

class knjl370jController extends Controller {
    var $ModelClassName = "knjl370jModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl370j":
                    $sessionInstance->knjl370jModel();
                    $this->callView("knjl370jForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl370jCtl = new knjl370jController;
var_dump($_REQUEST);
?>
