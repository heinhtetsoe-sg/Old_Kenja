<?php

require_once('for_php7.php');

require_once('knjl375jModel.inc');
require_once('knjl375jQuery.inc');

class knjl375jController extends Controller {
    var $ModelClassName = "knjl375jModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl375j":
                    $sessionInstance->knjl375jModel();
                    $this->callView("knjl375jForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjl375jCtl = new knjl375jController;
var_dump($_REQUEST);
?>
