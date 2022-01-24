<?php

require_once('for_php7.php');

require_once('knjl307bModel.inc');
require_once('knjl307bQuery.inc');

class knjl307bController extends Controller {
    var $ModelClassName = "knjl307bModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl307b":
                    $sessionInstance->knjl307bModel();
                    $this->callView("knjl307bForm1");
                    exit;
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl307bForm1");
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
$knjl307bCtl = new knjl307bController;
var_dump($_REQUEST);
?>
