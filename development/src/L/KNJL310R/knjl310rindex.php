<?php

require_once('for_php7.php');

require_once('knjl310rModel.inc');
require_once('knjl310rQuery.inc');

class knjl310rController extends Controller {
    var $ModelClassName = "knjl310rModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl310r":
                    $sessionInstance->knjl310rModel();
                    $this->callView("knjl310rForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl310rCtl = new knjl310rController;
var_dump($_REQUEST);
?>
