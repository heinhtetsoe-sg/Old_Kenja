<?php

require_once('for_php7.php');

require_once('knjl322bModel.inc');
require_once('knjl322bQuery.inc');

class knjl322bController extends Controller {
    var $ModelClassName = "knjl322bModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl322b":
                    $sessionInstance->knjl322bModel();
                    $this->callView("knjl322bForm1");
                    exit;
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl322bForm1");
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
$knjl322bCtl = new knjl322bController;
//var_dump($_REQUEST);
?>
