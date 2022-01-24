<?php

require_once('for_php7.php');

require_once('knjl323jModel.inc');
require_once('knjl323jQuery.inc');

class knjl323jController extends Controller {
    var $ModelClassName = "knjl323jModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl323j":
                    $sessionInstance->knjl323jModel();
                    $this->callView("knjl323jForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl323jCtl = new knjl323jController;
var_dump($_REQUEST);
?>
