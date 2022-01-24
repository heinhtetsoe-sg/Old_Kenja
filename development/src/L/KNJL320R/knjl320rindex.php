<?php

require_once('for_php7.php');

require_once('knjl320rModel.inc');
require_once('knjl320rQuery.inc');

class knjl320rController extends Controller {
    var $ModelClassName = "knjl320rModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                //CSV出力
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl320rForm1");
                    }
                    break 2;
                case "":
                case "knjl320r":
                    $sessionInstance->knjl320rModel();
                    $this->callView("knjl320rForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl320rCtl = new knjl320rController;
var_dump($_REQUEST);
?>
