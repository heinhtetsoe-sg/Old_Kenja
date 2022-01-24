<?php

require_once('for_php7.php');

require_once('knjl315rModel.inc');
require_once('knjl315rQuery.inc');

class knjl315rController extends Controller {
    var $ModelClassName = "knjl315rModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl315r":
                    $sessionInstance->knjl315rModel();
                    $this->callView("knjl315rForm1");
                    exit;
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl315rForm1");
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
$knjl315rCtl = new knjl315rController;
//var_dump($_REQUEST);
?>
