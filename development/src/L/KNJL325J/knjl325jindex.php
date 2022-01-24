<?php

require_once('for_php7.php');

require_once('knjl325jModel.inc');
require_once('knjl325jQuery.inc');

class knjl325jController extends Controller {
    var $ModelClassName = "knjl325jModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl325j":
                    $sessionInstance->knjl325jModel();
                    $this->callView("knjl325jForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjl325jCtl = new knjl325jController;
var_dump($_REQUEST);
?>
