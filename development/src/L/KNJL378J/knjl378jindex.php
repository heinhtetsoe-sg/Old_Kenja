<?php

require_once('for_php7.php');

require_once('knjl378jModel.inc');
require_once('knjl378jQuery.inc');

class knjl378jController extends Controller {
    var $ModelClassName = "knjl378jModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl378j":
                    $sessionInstance->knjl378jModel();
                    $this->callView("knjl378jForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjl378jCtl = new knjl378jController;
var_dump($_REQUEST);
?>
