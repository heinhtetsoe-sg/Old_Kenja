<?php

require_once('for_php7.php');

require_once('knja260kModel.inc');
require_once('knja260kQuery.inc');

class knja260kController extends Controller {
    var $ModelClassName = "knja260kModel";
    var $ProgramID      = "KNJA260K";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "init":
                case "knja260k":
                    $sessionInstance->knja260kModel();
                    $this->callView("knja260kForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knja260kForm1");
                    }
                    break 2;
                case "csv2":     //CSVダウンロード---2005.09.30
                    if (!$sessionInstance->getDownloadModel2()){
                        $this->callView("knja260kForm1");
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
$knja260kCtl = new knja260kController;
//var_dump($_REQUEST);
?>
