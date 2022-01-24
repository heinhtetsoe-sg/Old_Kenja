<?php

require_once('for_php7.php');

require_once('knja042Model.inc');
require_once('knja042Query.inc');

class knja042Controller extends Controller {
    var $ModelClassName = "knja042Model";
    var $ProgramID      = "KNJA042";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //ＣＳＶダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knja042Form1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID); 
                    break 2;
                case "":
                case "main":
                    $this->callView("knja042Form1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja042Ctl = new knja042Controller;
?>
