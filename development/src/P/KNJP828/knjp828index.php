<?php

require_once('for_php7.php');

require_once('knjp828Model.inc');
require_once('knjp828Query.inc');

class knjp828Controller extends Controller {
    var $ModelClassName = "knjp828Model";
    var $ProgramID      = "KNJP828";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "insertCsv":  //Insert＋CSV出力
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjp828Form1");
                    }
                    break 2;
                case "":
                case "main":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->getMainModel();
                    $this->callView("knjp828Form1");
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
$knjp828Ctl = new knjp828Controller;
?>
