<?php

require_once('for_php7.php');

require_once('knjl110qModel.inc');
require_once('knjl110qQuery.inc');

class knjl110qController extends Controller
{
    public $ModelClassName = "knjl110qModel";
    public $ProgramID      = "KNJL110Q";
    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                //CSV取込
                case "exec":
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                //CSV出力
                case "data":
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl110qForm1");
                    }
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "main":
                case "clear":
                    $this->callView("knjl110qForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl110qCtl = new knjl110qController();
