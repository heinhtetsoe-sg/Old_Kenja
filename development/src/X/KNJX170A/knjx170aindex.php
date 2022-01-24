<?php

require_once('for_php7.php');

require_once('knjx170aModel.inc');
require_once('knjx170aQuery.inc');

class knjx170aController extends Controller
{
    public $ModelClassName = "knjx170aModel";
    public $ProgramID      = "KNJX170A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "change_target":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjx170aForm1");
                    break 2;
                case "execute":
                    $sessionInstance->setAccessLogDetail("E", $ProgramID);
                    $sessionInstance->getExecModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "csv":   //CSV出力
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjx170aForm1");
                    }
                    break 2;
                case "exec":
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->outputDataFile()) {
                        $this->callView("knjx170aForm1");
                    }
                    break 2;
                case "output":
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->outputTmpFile()) {
                        $this->callView("knjx170aForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjx170aForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjx170aCtl = new knjx170aController();
