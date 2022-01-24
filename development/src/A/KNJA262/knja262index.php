<?php

require_once('for_php7.php');

require_once('knja262Model.inc');
require_once('knja262Query.inc');

class knja262Controller extends Controller {
    var $ModelClassName = "knja262Model";
    var $ProgramID      = "KNJA262";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //ＣＳＶダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knja262Form1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knja262Form1");
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
$knja262Ctl = new knja262Controller;
?>
