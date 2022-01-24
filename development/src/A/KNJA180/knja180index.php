<?php

require_once('for_php7.php');

require_once('knja180Model.inc');
require_once('knja180Query.inc');

class knja180Controller extends Controller {
    var $ModelClassName = "knja180Model";
    var $ProgramID      = "KNJA180";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja180":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knja180Model();       //コントロールマスタの呼び出し
                    $this->callView("knja180Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knja180Form1");
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
$knja180Ctl = new knja180Controller;
//var_dump($_REQUEST);
?>
