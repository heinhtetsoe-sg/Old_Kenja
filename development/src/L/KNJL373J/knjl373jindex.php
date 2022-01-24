<?php

require_once('for_php7.php');

require_once('knjl373jModel.inc');
require_once('knjl373jQuery.inc');

class knjl373jController extends Controller {
    var $ModelClassName = "knjl373jModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl373j":
                    $sessionInstance->knjl373jModel();
                    $this->callView("knjl373jForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl373jCtl = new knjl373jController;
var_dump($_REQUEST);
?>
