<?php

require_once('for_php7.php');

require_once('knjd110aModel.inc');
require_once('knjd110aQuery.inc');

class knjd110aController extends Controller {
    var $ModelClassName = "knjd110aModel";
    var $ProgramID      = "KNJD110A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "execute":
                case "execute2":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    //制御日付の変更
                    $sessionInstance->ChangeAttendDate();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    //$sessionInstance->setCmd("");
                    break 1;
                case "do_run":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateDoRun();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                case "main":
                case "chg_year":
                    $this->callView("knjd110aForm1");
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
$knjd110aCtl = new knjd110aController;
//var_dump($_REQUEST);
?>
