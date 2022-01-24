<?php

require_once('for_php7.php');

require_once('knjl223rModel.inc');
require_once('knjl223rQuery.inc');

class knjl223rController extends Controller {
    var $ModelClassName = "knjl223rModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl223r":
                    $sessionInstance->knjl223rModel();
                    $this->callView("knjl223rForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl223rCtl = new knjl223rController;
var_dump($_REQUEST);
?>
