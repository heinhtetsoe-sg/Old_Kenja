<?php

require_once('for_php7.php');

require_once('knjl306jModel.inc');
require_once('knjl306jQuery.inc');

class knjl306jController extends Controller {
    var $ModelClassName = "knjl306jModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl306j":
                    $sessionInstance->knjl306jModel();
                    $this->callView("knjl306jForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl306jCtl = new knjl306jController;
var_dump($_REQUEST);
?>
