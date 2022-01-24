<?php

require_once('for_php7.php');

require_once('knjtx010Model.inc');
require_once('knjtx010Query.inc');

class knjtx010Controller extends Controller {
    var $ModelClassName = "knjtx010Model";
    var $ProgramID      = "KNJTX010";
    
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("sel");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "sel";
                    $this->callView("knjtx010Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjtx010Ctl = new knjtx010Controller;
//var_dump($_REQUEST);
?>
