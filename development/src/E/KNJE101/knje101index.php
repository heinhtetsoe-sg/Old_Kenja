<?php

require_once('for_php7.php');

require_once('knje101Model.inc');
require_once('knje101Query.inc');

class knje101Controller extends Controller
{
    public $ModelClassName = "knje101Model";
    public $ProgramID      = "KNJE101";

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
                        $this->callView("knje101Form1");
                    }
                    break 2;
                case "output":
                    if (!$sessionInstance->outputTmpFile()) {
                        $this->callView("knje101Form1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knje101Form1");
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
$knje101Ctl = new knje101Controller();
