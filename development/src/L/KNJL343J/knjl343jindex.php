<?php

require_once('for_php7.php');

require_once('knjl343jModel.inc');
require_once('knjl343jQuery.inc');

class knjl343jController extends Controller {
    var $ModelClassName = "knjl343jModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl343j":
                    $sessionInstance->knjl343jModel();
                    $this->callView("knjl343jForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjl343jCtl = new knjl343jController;
var_dump($_REQUEST);
?>
