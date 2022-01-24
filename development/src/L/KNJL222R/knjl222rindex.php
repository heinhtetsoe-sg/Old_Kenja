<?php

require_once('for_php7.php');

require_once('knjl222rModel.inc');
require_once('knjl222rQuery.inc');

class knjl222rController extends Controller {
    var $ModelClassName = "knjl222rModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl222r":
                    $sessionInstance->knjl222rModel();
                    $this->callView("knjl222rForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl222rCtl = new knjl222rController;
var_dump($_REQUEST);
?>
