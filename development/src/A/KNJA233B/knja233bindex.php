<?php

require_once('for_php7.php');

require_once('knja233bModel.inc');
require_once('knja233bQuery.inc');

class knja233bController extends Controller {
    var $ModelClassName = "knja233bModel";
    var $ProgramID      = "KNJA233B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja233b":
                case "gakki":
                case "date":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knja233bModel();
                    $this->callView("knja233bForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knja233bForm1");
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
$knja233bCtl = new knja233bController;
//var_dump($_REQUEST);
?>
