<?php

require_once('for_php7.php');

require_once('knjb3049Model.inc');
require_once('knjb3049Query.inc');

class knjb3049Controller extends Controller {
    var $ModelClassName = "knjb3049Model";
    var $ProgramID      = "KNJB3049";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                //データ取込
                case "exec":
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                //エラー出力
                case "error":
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjb3049Form1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjb3049Form1");
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
$knjb3049Ctl = new knjb3049Controller;
?>
