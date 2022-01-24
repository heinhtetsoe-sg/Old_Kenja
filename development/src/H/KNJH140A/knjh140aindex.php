<?php

require_once('for_php7.php');

require_once('knjh140aModel.inc');
require_once('knjh140aQuery.inc');

class knjh140aController extends Controller
{
    public $ModelClassName = "knjh140aModel";
    public $ProgramID      = "KNJH140A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "change_target":
                    $this->callView("knjh140aForm1");
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
                        $this->callView("knjh140aForm1");
                    }
                    break 2;
                case "output":
                    if (!$sessionInstance->outputTmpFile()) {
                        $this->callView("knjh140aForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjh140aForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh140aCtl = new knjh140aController();
