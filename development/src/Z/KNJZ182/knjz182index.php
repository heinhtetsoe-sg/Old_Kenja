<?php

require_once('for_php7.php');

require_once('knjz182Model.inc');
require_once('knjz182Query.inc');

class knjz182Controller extends Controller {
    var $ModelClassName = "knjz182Model";
    var $ProgramID      = "KNJZ182";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
               case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
               case "copy":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getCopyModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
               case "error":
                    $this->callView("error");
                    break 2;
               case "level":
                    $this->callView("knjz182Form1");
                    break 2;
               case "inquiry":
                    $this->callView("knjz182Form2");
                    break 2;
               case "clear":
               case "change":
               case "main":
               case "default":
               case "":
                    $this->callView("knjz182Form1");
                    break 2;
               default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz182Ctl = new knjz182Controller;
//var_dump($_REQUEST);
?>
