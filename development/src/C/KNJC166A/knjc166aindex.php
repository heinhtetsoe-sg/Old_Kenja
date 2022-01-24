<?php

require_once('for_php7.php');

require_once('knjc166aModel.inc');
require_once('knjc166aQuery.inc');

class knjc166aController extends Controller
{
    public $ModelClassName = "knjc166aModel";
    public $ProgramID      = "KNJC166A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "execute":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getExecModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                case "main":
                case "main2":
                    $this->callView("knjc166aForm1");
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
$knjc166aCtl = new knjc166aController();
