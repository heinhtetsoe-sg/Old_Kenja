<?php

require_once('for_php7.php');

require_once('knjx170Model.inc');
require_once('knjx170Query.inc');

class knjx170Controller extends Controller
{
    public $ModelClassName = "knjx170Model";
    public $ProgramID      = "KNJX170";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "change_target":
                    $this->callView("knjx170Form1");
                    break 2;
                case "execute":
                    $sessionInstance->getExecModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "csv":   //CSV出力
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjx170Form1");
                    }
                    break 2;
                case "exec":
                    if (!$sessionInstance->outputDataFile()) {
                        $this->callView("knjx170Form1");
                    }
                    break 2;
                case "output":
                    if (!$sessionInstance->outputTmpFile()) {
                        $this->callView("knjx170Form1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjx170Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjx170Ctl = new knjx170Controller();
