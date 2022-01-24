<?php

require_once('for_php7.php');

require_once('knja263Model.inc');
require_once('knja263Query.inc');

class knja263Controller extends Controller {
    var $ModelClassName = "knja263Model";
    var $ProgramID      = "KNJA263";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //ＣＳＶダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knja263Form1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    break 2;
                case "":
                case "main":
                    $this->callView("knja263Form1");
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
$knja263Ctl = new knja263Controller;
?>
