<?php

require_once('for_php7.php');

require_once('knjl307rModel.inc');
require_once('knjl307rQuery.inc');

class knjl307rController extends Controller {
    var $ModelClassName = "knjl307rModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl307r":
                    $sessionInstance->knjl307rModel();
                    $this->callView("knjl307rForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl307rCtl = new knjl307rController;
var_dump($_REQUEST);
?>
