<?php

require_once('for_php7.php');

require_once('knjl376jModel.inc');
require_once('knjl376jQuery.inc');

class knjl376jController extends Controller {
    var $ModelClassName = "knjl376jModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl376j":
                    $sessionInstance->knjl376jModel();
                    $this->callView("knjl376jForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjl376jCtl = new knjl376jController;
var_dump($_REQUEST);
?>
