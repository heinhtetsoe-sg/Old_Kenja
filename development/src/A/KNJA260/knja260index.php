<?php

require_once('for_php7.php');

require_once('knja260Model.inc');
require_once('knja260Query.inc');

class knja260Controller extends Controller {
    var $ModelClassName = "knja260Model";
    var $ProgramID      = "KNJA260";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "init":
                case "knja260":
                    $sessionInstance->knja260Model();
                    $this->callView("knja260Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knja260Form1");
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
$knja260Ctl = new knja260Controller;
//var_dump($_REQUEST);
?>
