<?php

require_once('for_php7.php');

require_once('knjh174Model.inc');
require_once('knjh174Query.inc');

class knjh174Controller extends Controller {
    var $ModelClassName = "knjh174Model";
    var $ProgramID      = "KNJH174";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                //CSV取込
                case "exec":
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                //CSV出力
                case "csv":
                case "head":
                case "error":
                case "data":
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjh174Form1");
                    }
                    break 2;
                //前年度コピー
                case "copy":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                case "main":
                    $this->callView("knjh174Form1");
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
$knjh174Ctl = new knjh174Controller;
?>
