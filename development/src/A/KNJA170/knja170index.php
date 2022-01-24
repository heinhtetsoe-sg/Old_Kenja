<?php

require_once('for_php7.php');

require_once('knja170Model.inc');
require_once('knja170Query.inc');

class knja170Controller extends Controller {
    var $ModelClassName = "knja170Model";
    var $ProgramID      = "KNJA170";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja170":
                case "read":
                    $sessionInstance->knja170Model();
                    $this->callView("knja170Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knja170Form1");
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
$knja170Ctl = new knja170Controller;
//var_dump($_REQUEST);
?>
