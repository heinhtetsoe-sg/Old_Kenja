<?php

require_once('for_php7.php');

require_once('knjl350jModel.inc');
require_once('knjl350jQuery.inc');

class knjl350jController extends Controller {
    var $ModelClassName = "knjl350jModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl350j":
                    $sessionInstance->knjl350jModel();
                    $this->callView("knjl350jForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjl350jCtl = new knjl350jController;
var_dump($_REQUEST);
?>
