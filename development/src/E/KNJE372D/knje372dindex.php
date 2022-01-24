<?php

require_once('for_php7.php');

require_once('knje372dModel.inc');
require_once('knje372dQuery.inc');

class knje372dController extends Controller
{
    public $ModelClassName = "knje372dModel";
    public $ProgramID      = "KNJE372D";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knje372dForm1");
                    break 2;
                case "execute":
                    $sessionInstance->setAccessLogDetail("E", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getExecModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
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
$knje372dCtl = new knje372dController();
