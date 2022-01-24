<?php

require_once('for_php7.php');

require_once('knjl305rModel.inc');
require_once('knjl305rQuery.inc');

class knjl305rController extends Controller {
    var $ModelClassName = "knjl305rModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl305r":
                    $sessionInstance->knjl305rModel();
                    $this->callView("knjl305rForm1");
                    exit;
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl305rForm1");
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
$knjl305rCtl = new knjl305rController;
//var_dump($_REQUEST);
?>
