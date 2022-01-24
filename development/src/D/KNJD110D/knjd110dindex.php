<?php

require_once('for_php7.php');

require_once('knjd110dModel.inc');
require_once('knjd110dQuery.inc');

class knjd110dController extends Controller {
    var $ModelClassName = "knjd110dModel";
    var $ProgramID      = "KNJD110D";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "del":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("main");
                    break 1;
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
                    $this->callView("knjd110dForm1");
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
$knjd110dCtl = new knjd110dController;
//var_dump($_REQUEST);
?>
