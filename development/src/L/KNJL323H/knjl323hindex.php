<?php

require_once('for_php7.php');

require_once('knjl323hModel.inc');
require_once('knjl323hQuery.inc');

class knjl323hController extends Controller {
    var $ModelClassName = "knjl323hModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl323h":
                    $sessionInstance->knjl323hModel();
                    $this->callView("knjl323hForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl323hCtl = new knjl323hController;
var_dump($_REQUEST);
?>
