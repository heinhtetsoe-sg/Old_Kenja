<?php
require_once('knjl3020Model.inc');
require_once('knjl3020Query.inc');

class knjl3020Controller extends Controller
{
    var $ModelClassName = "knjl3020Model";
    var $ProgramID      = "KNJL3020";
    function main()
    {
        $sessionInstance = &Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("sel");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "sel":
                case "chgAppDiv":
                case "clear";
                    $this->callView("knjl3020Form1");
                    break 2;
                case "":
                    $sessionInstance->setCmd("sel");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl3020Ctl = new knjl3020Controller;
//var_dump($_REQUEST);
