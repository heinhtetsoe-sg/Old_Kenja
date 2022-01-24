<?php

require_once('for_php7.php');

require_once('knjl372jModel.inc');
require_once('knjl372jQuery.inc');

class knjl372jController extends Controller {
    var $ModelClassName = "knjl372jModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl372j":
                    $sessionInstance->knjl372jModel();
                    $this->callView("knjl372jForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl372jCtl = new knjl372jController;
var_dump($_REQUEST);
?>
