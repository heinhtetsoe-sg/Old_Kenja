<?php

require_once('for_php7.php');

require_once('knja233cModel.inc');
require_once('knja233cQuery.inc');

class knja233cController extends Controller {
    var $ModelClassName = "knja233cModel";
    var $ProgramID      = "KNJA233C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja233c":
                case "gakki":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knja233cModel();
                    $this->callView("knja233cForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knja233cForm1");
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
$knja233cCtl = new knja233cController;
//var_dump($_REQUEST);
?>
