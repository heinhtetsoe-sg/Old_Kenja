<?php

require_once('for_php7.php');

require_once('knjl322nModel.inc');
require_once('knjl322nQuery.inc');

class knjl322nController extends Controller {
    var $ModelClassName = "knjl322nModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl322n":
                    $sessionInstance->knjl322nModel();
                    $this->callView("knjl322nForm1");
                    exit;
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl322nForm1");
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
$knjl322nCtl = new knjl322nController;
//var_dump($_REQUEST);
?>
