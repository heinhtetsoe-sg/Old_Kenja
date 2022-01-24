<?php
require_once('knja233dModel.inc');
require_once('knja233dQuery.inc');

class knja233dController extends Controller {
    var $ModelClassName = "knja233dModel";
    var $ProgramID      = "KNJA233D";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja233d":
                case "gakki":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knja233dModel();
                    $this->callView("knja233dForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knja233dForm1");
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
$knja233dCtl = new knja233dController;
//var_dump($_REQUEST);
?>
