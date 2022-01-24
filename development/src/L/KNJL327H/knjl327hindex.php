<?php

require_once('for_php7.php');

require_once('knjl327hModel.inc');
require_once('knjl327hQuery.inc');

class knjl327hController extends Controller {
    var $ModelClassName = "knjl327hModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl327h":
                    $sessionInstance->knjl327hModel();
                    $this->callView("knjl327hForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl327hCtl = new knjl327hController;
var_dump($_REQUEST);
?>
