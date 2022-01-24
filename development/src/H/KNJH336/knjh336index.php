<?php

require_once('for_php7.php');

require_once('knjh336Model.inc');
require_once('knjh336Query.inc');

class knjh336Controller extends Controller
{
    public $ModelClassName = "knjh336Model";
    public $ProgramID      = "KNJH336";

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
                case "exec":
                    if (!$sessionInstance->outputDataFile()) {
                        $this->callView("knjh336Form1");
                    }
                    break 2;
                case "output":
                    if (!$sessionInstance->outputTmpFile()) {
                        $this->callView("knjh336Form1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjh336Form1");
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
$knjh336Ctl = new knjh336Controller();
