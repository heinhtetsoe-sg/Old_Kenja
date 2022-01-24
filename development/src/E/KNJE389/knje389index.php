<?php

require_once('for_php7.php');

require_once('knje389Model.inc');
require_once('knje389Query.inc');

class knje389Controller extends Controller {
    var $ModelClassName = "knje389Model";
    var $ProgramID      = "KNJE389";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //CSV取込
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    // $this->checkAuth(DEF_UPDATE_RESTRICT, "knje389Form1", $sessionInstance->auth);
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csv":       //CSV出力
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knje389Form1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knje389Form1");
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
$knje389Ctl = new knje389Controller;
?>
