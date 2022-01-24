<?php

require_once('for_php7.php');

require_once('knjd110kModel.inc');
require_once('knjd110kQuery.inc');

class knjd110kController extends Controller {
    var $ModelClassName = "knjd110kModel";
    var $ProgramID      = "KNJD110K";

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
                case "do_run":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateDoRun();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                case "main":
                case "chg_year":
                    $this->callView("knjd110kForm1");
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
$knjd110kCtl = new knjd110kController;
//var_dump($_REQUEST);
?>
