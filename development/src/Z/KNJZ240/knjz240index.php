<?php

require_once('for_php7.php');

require_once('knjz240Model.inc');
require_once('knjz240Query.inc');

class knjz240Controller extends Controller
{
    public $ModelClassName = "knjz240Model";
    public $ProgramID      = "KNJZ240";
    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "pattern":
                case "clear":
                case "edit":
                case "change":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz240Form1");
                    break 2;
                case "insert":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->setCmd("edit");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz240Ctl = new knjz240Controller;
//var_dump($_REQUEST);
