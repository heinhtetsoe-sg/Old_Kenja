<?php

require_once('for_php7.php');

require_once('knja610cModel.inc');
require_once('knja610cQuery.inc');

class knja610cController extends Controller {
    var $ModelClassName = "knja610cModel";
    var $ProgramID      = "KNJA610C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "init":
                case "knja610c":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knja610cModel();
                    $this->callView("knja610cForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knja610cForm1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja610cCtl = new knja610cController;
//var_dump($_REQUEST);
?>
