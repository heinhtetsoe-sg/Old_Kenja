<?php

require_once('for_php7.php');

require_once('knjl315bModel.inc');
require_once('knjl315bQuery.inc');

class knjl315bController extends Controller {
    var $ModelClassName = "knjl315bModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl315b":
                    $sessionInstance->knjl315bModel();
                    $this->callView("knjl315bForm1");
                    exit;
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl315bForm1");
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
$knjl315bCtl = new knjl315bController;
var_dump($_REQUEST);
?>
