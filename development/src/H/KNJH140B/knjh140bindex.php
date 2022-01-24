<?php

require_once('for_php7.php');

require_once('knjh140bModel.inc');
require_once('knjh140bQuery.inc');

class knjh140bController extends Controller
{
    public $ModelClassName = "knjh140bModel";
    public $ProgramID      = "KNJH140B";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "change_target":
                    $this->callView("knjh140bForm1");
                    break 2;
                case "execute":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "csv":   //CSV出力
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjh140bForm1");
                    }
                    break 2;
                case "output":
                    if (!$sessionInstance->outputTmpFile()) {
                        $this->callView("knjh140bForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjh140bForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh140bCtl = new knjh140bController();
