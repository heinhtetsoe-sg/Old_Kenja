<?php

require_once('for_php7.php');

require_once('knjl381jModel.inc');
require_once('knjl381jQuery.inc');

class knjl381jController extends Controller {
    var $ModelClassName = "knjl381jModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl381j":
                    $sessionInstance->knjl381jModel();
                    $this->callView("knjl381jForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl381jCtl = new knjl381jController;
var_dump($_REQUEST);
?>
