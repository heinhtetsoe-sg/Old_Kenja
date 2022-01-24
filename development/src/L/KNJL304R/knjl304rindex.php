<?php

require_once('for_php7.php');

require_once('knjl304rModel.inc');
require_once('knjl304rQuery.inc');

class knjl304rController extends Controller {
    var $ModelClassName = "knjl304rModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl304r":
                    $sessionInstance->knjl304rModel();
                    $this->callView("knjl304rForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl304rCtl = new knjl304rController;
var_dump($_REQUEST);
?>
