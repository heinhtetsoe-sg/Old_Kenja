<?php

require_once('for_php7.php');

require_once('knjl226rModel.inc');
require_once('knjl226rQuery.inc');

class knjl226rController extends Controller {
    var $ModelClassName = "knjl226rModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl226r":
                    $sessionInstance->knjl226rModel();
                    $this->callView("knjl226rForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl226rCtl = new knjl226rController;
var_dump($_REQUEST);
?>
