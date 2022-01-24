<?php

require_once('for_php7.php');

require_once('knjz150Model.inc');
require_once('knjz150Query.inc');

class KNJZ150Controller extends Controller {
    var $ModelClassName = "knjz150Model";
    var $ProgramID      = "KNJZ150";

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
                    $this->callView("knjz150Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }

        }
    }
}
$KNJZ150Ctl = new KNJZ150Controller;
//var_dump($_REQUEST);
?>
