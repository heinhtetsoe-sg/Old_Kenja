<?php

require_once('for_php7.php');

require_once('knjz031Model.inc');
require_once('knjz031Query.inc');

class knjz031Controller extends Controller
{
    public $ModelClassName = "knjz031Model";
    public $ProgramID      = "KNJZ031";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "reset":
                case "decision":
                case "":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz031Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz031Ctl = new knjz031Controller();
//var_dump($_REQUEST);
