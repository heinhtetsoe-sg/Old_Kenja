<?php

require_once('for_php7.php');

require_once('knjl305bModel.inc');
require_once('knjl305bQuery.inc');

class knjl305bController extends Controller {
    var $ModelClassName = "knjl305bModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl305b":
                    $sessionInstance->knjl305bModel();
                    $this->callView("knjl305bForm1");
                    exit;
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl305bForm1");
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
$knjl305bCtl = new knjl305bController;
var_dump($_REQUEST);
?>
