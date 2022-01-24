<?php

require_once('for_php7.php');

require_once('knjl305fModel.inc');
require_once('knjl305fQuery.inc');

class knjl305fController extends Controller {
    var $ModelClassName = "knjl305fModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl305f":
                    $sessionInstance->knjl305fModel();
                    $this->callView("knjl305fForm1");
                    exit;
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl305fForm1");
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
$knjl305fCtl = new knjl305fController;
var_dump($_REQUEST);
?>
