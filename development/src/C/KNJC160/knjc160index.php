<?php

require_once('for_php7.php');

require_once('knjc160Model.inc');
require_once('knjc160Query.inc');

class knjc160Controller extends Controller {
    var $ModelClassName = "knjc160Model";
    var $ProgramID      = "knjc160";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "execute":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    //制御日付の変更
                    $sessionInstance->ChangeAttendDate();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    //$sessionInstance->setCmd("");
                    break 1;
                case "":
                case "main":
                case "chg_year":
                    $this->callView("knjc160Form1");
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
$knjc160Ctl = new knjc160Controller;
//var_dump($_REQUEST);
?>
