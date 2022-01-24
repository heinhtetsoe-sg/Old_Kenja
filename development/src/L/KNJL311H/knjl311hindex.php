<?php

require_once('for_php7.php');

require_once('knjl311hModel.inc');
require_once('knjl311hQuery.inc');

class knjl311hController extends Controller {
    var $ModelClassName = "knjl311hModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl311h":
                    $sessionInstance->knjl311hModel();
                    $this->callView("knjl311hForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl311hCtl = new knjl311hController;
var_dump($_REQUEST);
?>
