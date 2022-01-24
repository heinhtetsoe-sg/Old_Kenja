<?php

require_once('for_php7.php');

require_once('knjl328hModel.inc');
require_once('knjl328hQuery.inc');

class knjl328hController extends Controller {
    var $ModelClassName = "knjl328hModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl328h":
                    $sessionInstance->knjl328hModel();
                    $this->callView("knjl328hForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl328hCtl = new knjl328hController;
var_dump($_REQUEST);
?>
