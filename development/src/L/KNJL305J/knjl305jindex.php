<?php

require_once('for_php7.php');

require_once('knjl305jModel.inc');
require_once('knjl305jQuery.inc');

class knjl305jController extends Controller {
    var $ModelClassName = "knjl305jModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl305j":
                    $sessionInstance->knjl305jModel();
                    $this->callView("knjl305jForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl305jCtl = new knjl305jController;
var_dump($_REQUEST);
?>
