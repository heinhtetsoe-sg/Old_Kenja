<?php

require_once('for_php7.php');

require_once('knjl221rModel.inc');
require_once('knjl221rQuery.inc');

class knjl221rController extends Controller {
    var $ModelClassName = "knjl221rModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl221r":
                    $sessionInstance->knjl221rModel();
                    $this->callView("knjl221rForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl221rCtl = new knjl221rController;
var_dump($_REQUEST);
?>
