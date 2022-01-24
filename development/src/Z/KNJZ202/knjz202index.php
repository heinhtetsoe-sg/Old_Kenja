<?php

require_once('for_php7.php');

require_once('knjz202Model.inc');
require_once('knjz202Query.inc');

class knjz202Controller extends Controller {
    var $ModelClassName = "knjz202Model";
    var $ProgramID      = "KNJZ202";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "execute":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                case "main":
                case "chg_year":
                    $this->callView("knjz202Form1");
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
$knjz202Ctl = new knjz202Controller;
//var_dump($_REQUEST);
?>
