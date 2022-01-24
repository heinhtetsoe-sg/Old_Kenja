<?php

require_once('for_php7.php');

require_once('knjl342hModel.inc');
require_once('knjl342hQuery.inc');

class knjl342hController extends Controller {
    var $ModelClassName = "knjl342hModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl342h":
                    $sessionInstance->knjl342hModel();
                    $this->callView("knjl342hForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl342hCtl = new knjl342hController;
var_dump($_REQUEST);
?>
