<?php

require_once('for_php7.php');

require_once('knjl300jModel.inc');
require_once('knjl300jQuery.inc');

class knjl300jController extends Controller {
    var $ModelClassName = "knjl300jModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl300j":
                    $sessionInstance->knjl300jModel();
                    $this->callView("knjl300jForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl300jCtl = new knjl300jController;
var_dump($_REQUEST);
?>
