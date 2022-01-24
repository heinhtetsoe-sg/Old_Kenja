<?php

require_once('for_php7.php');

require_once('knjl302hModel.inc');
require_once('knjl302hQuery.inc');

class knjl302hController extends Controller {
    var $ModelClassName = "knjl302hModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl302h":
                    $sessionInstance->knjl302hModel();
                    $this->callView("knjl302hForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl302hCtl = new knjl302hController;
var_dump($_REQUEST);
?>
