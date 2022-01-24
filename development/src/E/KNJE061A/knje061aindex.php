<?php
require_once('knje061aModel.inc');
require_once('knje061aQuery.inc');

class knje061aController extends Controller
{
    public $ModelClassName = "knje061aModel";
    public $ProgramID      = "KNJE061A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getExecModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "output":
                    if (!$sessionInstance->outputTmpFile()) {
                        $this->callView("knje061aForm1");
                    }
                    break 2;
                case "":
                case "main":
                case "coursecode":
                case "hr_class":
                case "annual":
                    $sessionInstance->getMainModel();
                    $this->callView("knje061aForm1");
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
$knje061aCtl = new knje061aController();
