<?php

require_once('for_php7.php');

require_once('knje061Model.inc');
require_once('knje061Query.inc');

class knje061Controller extends Controller {
    var $ModelClassName = "knje061Model";
    var $ProgramID      = "KNJE061";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getExecModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "output":
                    if (!$sessionInstance->OutputTmpFile()) {
                        $this->callView("knje061Form1");
                    }
                    break 2;
                case "":
                case "main":
                case "coursecode":
                case "hr_class":
                case "annual":
                    $sessionInstance->getMainModel();
                    $this->callView("knje061Form1");
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
$knje061Ctl = new knje061Controller;
?>
