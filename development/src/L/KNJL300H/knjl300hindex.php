<?php

require_once('for_php7.php');

require_once('knjl300hModel.inc');
require_once('knjl300hQuery.inc');

class knjl300hController extends Controller {
    var $ModelClassName = "knjl300hModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl300h":
                    $this->callView("knjl300hForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl300hCtl = new knjl300hController;
var_dump($_REQUEST);
?>
