<?php

require_once('for_php7.php');

require_once('knjd110eModel.inc');
require_once('knjd110eQuery.inc');

class knjd110eController extends Controller
{
    public $ModelClassName = "knjd110eModel";
    public $ProgramID      = "KNJD110E";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "del":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "execute":
                case "execute2":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    //制御日付の変更
                    $sessionInstance->changeAttendDate();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    //$sessionInstance->setCmd("");
                    break 1;
                case "":
                case "main":
                    $this->callView("knjd110eForm1");
                    break 2;
                case "chg_year":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjd110eForm1");
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
$knjd110eCtl = new knjd110eController();
//var_dump($_REQUEST);
