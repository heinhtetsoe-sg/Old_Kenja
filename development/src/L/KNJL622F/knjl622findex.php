<?php

require_once('for_php7.php');

require_once('knjl622fModel.inc');
require_once('knjl622fQuery.inc');

class knjl622fController extends Controller {
    var $ModelClassName = "knjl622fModel";
    var $ProgramID      = "KNJL622F";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl622f":
                    $sessionInstance->knjl622fModel();
                    $this->callView("knjl622fForm1");
                    exit;
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl622fForm1");
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
$knjl622fCtl = new knjl622fController;
//var_dump($_REQUEST);
?>
