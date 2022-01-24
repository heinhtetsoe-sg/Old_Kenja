<?php

require_once('for_php7.php');

require_once('knjl351bModel.inc');
require_once('knjl351bQuery.inc');

class knjl351bController extends Controller {
    var $ModelClassName = "knjl351bModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl351b":
                    $sessionInstance->knjl351bModel();
                    $this->callView("knjl351bForm1");
                    exit;
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl351bForm1");
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
$knjl351bCtl = new knjl351bController;
var_dump($_REQUEST);
?>
