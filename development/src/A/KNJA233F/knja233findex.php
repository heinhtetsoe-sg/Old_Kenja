<?php

require_once('for_php7.php');

require_once('knja233fModel.inc');
require_once('knja233fQuery.inc');

class knja233fController extends Controller {
    var $ModelClassName = "knja233fModel";
    var $ProgramID      = "KNJA233F";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja233f":
                case "gakki":
                case "date":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knja233fModel();
                    $this->callView("knja233fForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knja233fForm1");
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
$knja233fCtl = new knja233fController;
//var_dump($_REQUEST);
?>
