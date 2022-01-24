<?php

require_once('for_php7.php');

require_once('knjl312hModel.inc');
require_once('knjl312hQuery.inc');

class knjl312hController extends Controller {
    var $ModelClassName = "knjl312hModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl312h":
                    $sessionInstance->knjl312hModel();
                    $this->callView("knjl312hForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl312hCtl = new knjl312hController;
var_dump($_REQUEST);
?>
