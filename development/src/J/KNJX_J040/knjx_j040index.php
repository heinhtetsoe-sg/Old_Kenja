<?php

require_once('for_php7.php');

require_once('knjx_j040Model.inc');
require_once('knjx_j040Query.inc');

class knjx_j040Controller extends Controller {
    var $ModelClassName = "knjx_j040Model";
    var $ProgramID      = "KNJX_J040";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //CSV取込
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csv":     //CSV出力
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjx_j040Form1");
                    }
                    break 2;
                case "":
                case "main":
                    $sessionInstance->knjx_j040Model();
                    $this->callView("knjx_j040Form1");
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
$knjx_j040Ctl = new knjx_j040Controller;
?>
