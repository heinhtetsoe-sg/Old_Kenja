<?php

require_once('for_php7.php');

require_once('knjl322hModel.inc');
require_once('knjl322hQuery.inc');

class knjl322hController extends Controller {
    var $ModelClassName = "knjl322hModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl322h":
                    $sessionInstance->knjl322hModel();
                    $this->callView("knjl322hForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl322hCtl = new knjl322hController;
var_dump($_REQUEST);
?>
