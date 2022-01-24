<?php

require_once('for_php7.php');

require_once('knjtx009Model.inc');
require_once('knjtx009Query.inc');

class knjtx009Controller extends Controller {
    var $ModelClassName = "knjtx009Model";
    var $ProgramID      = "KNJTX009";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "execute":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getExecuteModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjtx009Form1");
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
$knjtx009Ctl = new knjtx009Controller;
?>
