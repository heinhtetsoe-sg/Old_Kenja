<?php

require_once('for_php7.php');

require_once('knjl316rModel.inc');
require_once('knjl316rQuery.inc');

class knjl316rController extends Controller {
    var $ModelClassName = "knjl316rModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl316r":
                    $sessionInstance->knjl316rModel();
                    $this->callView("knjl316rForm1");
                    exit;
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl316rForm1");
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
$knjl316rCtl = new knjl316rController;
?>
