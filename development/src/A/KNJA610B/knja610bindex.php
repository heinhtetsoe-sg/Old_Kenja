<?php

require_once('for_php7.php');

require_once('knja610bModel.inc');
require_once('knja610bQuery.inc');

class knja610bController extends Controller {
    var $ModelClassName = "knja610bModel";
    var $ProgramID      = "KNJA610B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "init":
                case "knja610b":
                    $sessionInstance->knja610bModel();
                    $this->callView("knja610bForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knja610bForm1");
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
$knja610bCtl = new knja610bController;
//var_dump($_REQUEST);
?>
