<?php

require_once('for_php7.php');

require_once('knja260oModel.inc');
require_once('knja260oQuery.inc');

class knja260oController extends Controller {
    var $ModelClassName = "knja260oModel";
    var $ProgramID      = "KNJA260O";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "init":
                case "knja260o":
                    $sessionInstance->knja260oModel();
                    $this->callView("knja260oForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knja260oForm1");
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
$knja260oCtl = new knja260oController;
//var_dump($_REQUEST);
?>
