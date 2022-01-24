<?php

require_once('for_php7.php');

require_once('knjb1602Model.inc');
require_once('knjb1602Query.inc');

class knjb1602Controller extends Controller {
    var $ModelClassName = "knjb1602Model";
    var $ProgramID      = "KNJB1602";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":        //CSV取り込み
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csv":   //CSV出力
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjb1602Form1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjb1602Form1");
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
$knjb1602Ctl = new knjb1602Controller;
?>
