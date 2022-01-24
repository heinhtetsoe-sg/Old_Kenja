<?php

require_once('for_php7.php');

require_once('knje102Model.inc');
require_once('knje102Query.inc');

class knje102Controller extends Controller
{
    public $ModelClassName = "knje102Model";
    public $ProgramID      = "KNJE102";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "change_target":
                    $this->callView("knje102Form1");
                    break 2;
                case "execute":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getExecModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "csv":   //CSV出力
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjx050Form1");
                    }
                    break 2;
                case "exec":
                    if (!$sessionInstance->outputDataFile()) {
                        $this->callView("knje102Form1");
                    }
                    break 2;
                case "output":
                    if (!$sessionInstance->outputTmpFile()) {
                        $this->callView("knje102Form1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knje102Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje102Ctl = new knje102Controller();
