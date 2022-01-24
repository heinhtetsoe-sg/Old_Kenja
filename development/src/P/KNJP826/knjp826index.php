<?php

require_once('for_php7.php');

require_once('knjp826Model.inc');
require_once('knjp826Query.inc');

class knjp826Controller extends Controller {
    var $ModelClassName = "knjp826Model";
    var $ProgramID      = "KNJP826";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "insertCsv":  //Insert＋CSV出力
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjp826Form1");
                    }
                    break 2;
                case "csv":       //CSV出力
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjp826Form1");
                    }
                    break 2;
                case "":
                case "main":
                case "divChange":
                case "redisterChange":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->getMainModel();
                    $this->callView("knjp826Form1");
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
$knjp826Ctl = new knjp826Controller;
?>
