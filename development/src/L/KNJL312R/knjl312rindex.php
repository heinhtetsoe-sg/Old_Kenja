<?php

require_once('for_php7.php');

require_once('knjl312rModel.inc');
require_once('knjl312rQuery.inc');

class knjl312rController extends Controller {
    var $ModelClassName = "knjl312rModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl312r":
                    $sessionInstance->knjl312rModel();
                    $this->callView("knjl312rForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl312rCtl = new knjl312rController;
var_dump($_REQUEST);
?>
