<?php

require_once('for_php7.php');

require_once('knjl314rModel.inc');
require_once('knjl314rQuery.inc');

class knjl314rController extends Controller {
    var $ModelClassName = "knjl314rModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl314r":
                    $sessionInstance->knjl314rModel();
                    $this->callView("knjl314rForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl314rCtl = new knjl314rController;
var_dump($_REQUEST);
?>
