<?php

require_once('for_php7.php');

require_once('knja270Model.inc');
require_once('knja270Query.inc');

class knja270Controller extends Controller {
    var $ModelClassName = "knja270Model";
    var $ProgramID      = "KNJA270";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "init":
                case "cmbchange":
                case "datechange":
                case "knja270":
                    $sessionInstance->knja270Model();
                    $this->callView("knja270Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knja270Form1");
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
$knja270Ctl = new knja270Controller;
//var_dump($_REQUEST);
?>
