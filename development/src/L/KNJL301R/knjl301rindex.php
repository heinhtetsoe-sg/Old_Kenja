<?php

require_once('for_php7.php');

require_once('knjl301rModel.inc');
require_once('knjl301rQuery.inc');

class knjl301rController extends Controller {
    var $ModelClassName = "knjl301rModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl301r":
                    $sessionInstance->knjl301rModel();
                    $this->callView("knjl301rForm1");
                    exit;
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl301rForm1");
                    }
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl301rCtl = new knjl301rController;
//var_dump($_REQUEST);
?>
