<?php

require_once('for_php7.php');

require_once('knjl352bModel.inc');
require_once('knjl352bQuery.inc');

class knjl352bController extends Controller {
    var $ModelClassName = "knjl352bModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl352b":
                    $sessionInstance->knjl352bModel();
                    $this->callView("knjl352bForm1");
                    exit;
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl352bForm1");
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
$knjl352bCtl = new knjl352bController;
var_dump($_REQUEST);
?>
