<?php

require_once('for_php7.php');

require_once('knjh140Model.inc');
require_once('knjh140Query.inc');

class knjh140Controller extends Controller
{
    public $ModelClassName = "knjh140Model";
    public $ProgramID      = "KNJH140";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "change_target":
                    $this->callView("knjh140Form1");
                    break 2;
                case "execute":
                    $sessionInstance->setAccessLogDetail("EI", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "csv":   //CSV出力
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjx050Form1");
                    }
                    break 2;
                case "output":
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->outputTmpFile()) {
                        $this->callView("knjh140Form1");
                    }
                    break 2;
                case "":
                case "main":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh140Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh140Ctl = new knjh140Controller();
