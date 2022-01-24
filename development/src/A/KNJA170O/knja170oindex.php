<?php

require_once('for_php7.php');

require_once('knja170oModel.inc');
require_once('knja170oQuery.inc');

class knja170oController extends Controller {
    var $ModelClassName = "knja170oModel";
    var $ProgramID      = "KNJA170O";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja170o":
                case "read":
                    $sessionInstance->knja170oModel();
                    $this->callView("knja170oForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knja170oForm1");
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
$knja170oCtl = new knja170oController;
//var_dump($_REQUEST);
?>
