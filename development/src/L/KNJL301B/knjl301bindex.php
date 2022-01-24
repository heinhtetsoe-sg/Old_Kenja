<?php

require_once('for_php7.php');

require_once('knjl301bModel.inc');
require_once('knjl301bQuery.inc');

class knjl301bController extends Controller {
    var $ModelClassName = "knjl301bModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl301b":
                    $sessionInstance->knjl301bModel();
                    $this->callView("knjl301bForm1");
                    exit;
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl301bForm1");
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
$knjl301bCtl = new knjl301bController;
var_dump($_REQUEST);
?>
