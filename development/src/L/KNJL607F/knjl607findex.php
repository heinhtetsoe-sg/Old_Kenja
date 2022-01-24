<?php

require_once('for_php7.php');

require_once('knjl607fModel.inc');
require_once('knjl607fQuery.inc');

class knjl607fController extends Controller {
    var $ModelClassName = "knjl607fModel";
    var $ProgramID      = "KNJL607F";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl607f":
                    $sessionInstance->knjl607fModel();
                    $this->callView("knjl607fForm1");
                    exit;
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl607fForm1");
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
$knjl607fCtl = new knjl607fController;
//var_dump($_REQUEST);
?>
