<?php

require_once('for_php7.php');

require_once('knjp733Model.inc');
require_once('knjp733Query.inc');

class knjp733Controller extends Controller {
    var $ModelClassName = "knjp733Model";
    var $ProgramID      = "KNJP733";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getExecModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("updMain");
                    break 1;
                case "output":
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->OutputTmpFile()) {
                        $this->callView("knjp733Form1");
                    }
                    break 2;
                case "":
                case "updMain":
                case "main":
                case "changeVal":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->getMainModel();
                    $this->callView("knjp733Form1");
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
$knjp733Ctl = new knjp733Controller;
?>
