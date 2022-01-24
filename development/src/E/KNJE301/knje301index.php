<?php

require_once('for_php7.php');

require_once('knje301Model.inc');
require_once('knje301Query.inc');

class knje301Controller extends Controller {
    var $ModelClassName = "knje301Model";
    var $ProgramID      = "KNJE301";
    
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "copy":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case 'main':
                case "":
                case "sel";
                    $this->callView("knje301Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knje301Ctl = new knje301Controller;
//var_dump($_REQUEST);
?>
