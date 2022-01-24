<?php

require_once('for_php7.php');

require_once('knjl377jModel.inc');
require_once('knjl377jQuery.inc');

class knjl377jController extends Controller {
    var $ModelClassName = "knjl377jModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl377j":
                    $sessionInstance->knjl377jModel();
                    $this->callView("knjl377jForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjl377jCtl = new knjl377jController;
var_dump($_REQUEST);
?>
