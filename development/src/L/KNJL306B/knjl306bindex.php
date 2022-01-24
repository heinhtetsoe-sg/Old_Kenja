<?php

require_once('for_php7.php');

require_once('knjl306bModel.inc');
require_once('knjl306bQuery.inc');

class knjl306bController extends Controller {
    var $ModelClassName = "knjl306bModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl306b":
                    $sessionInstance->knjl306bModel();
                    $this->callView("knjl306bForm1");
                    exit;
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl306bForm1");
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
$knjl306bCtl = new knjl306bController;
var_dump($_REQUEST);
?>
