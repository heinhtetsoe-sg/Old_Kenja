<?php

require_once('for_php7.php');

require_once('knjz030Model.inc');
require_once('knjz030Query.inc');

class knjz030Controller extends Controller
{
    public $ModelClassName = "knjz030Model";
    public $ProgramID      = "KNJZ030";
    
    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz030Form1");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                      $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz030Ctl = new knjz030Controller();
//var_dump($_REQUEST);
