<?php

require_once('for_php7.php');

require_once('knja610vModel.inc');
require_once('knja610vQuery.inc');

class knja610vController extends Controller {
    var $ModelClassName = "knja610vModel";
    var $ProgramID      = "KNJA610V";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "init":
                case "knja610v":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knja610vModel();
                    $this->callView("knja610vForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knja610vForm1");
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
$knja610vCtl = new knja610vController;
//var_dump($_REQUEST);
?>
