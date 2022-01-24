<?php

require_once('for_php7.php');

require_once('knja233Model.inc');
require_once('knja233Query.inc');

class knja233Controller extends Controller {
    var $ModelClassName = "knja233Model";
    var $ProgramID      = "KNJA233";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "toukei":
                case "":
                    $this->callView("knja233Form1");
                   break 2;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knja233Form1");
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
$knja233Ctl = new knja233Controller;
//var_dump($_REQUEST);
?>
