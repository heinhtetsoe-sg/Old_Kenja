<?php
require_once('knjj521Model.inc');
require_once('knjj521Query.inc');

class knjj521Controller extends Controller
{
    public $ModelClassName = "knjj521Model";
    public $ProgramID      = "KNJJ521";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "form1":
                case "reset":
                    $this->callView("knjj521Form1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("form1");
                    //変更済みの場合は詳細画面に戻る
                    break 1;
                case "calc":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("form1");
                    //変更済みの場合は詳細画面に戻る
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->setCmd("form1");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjj521Ctl = new knjj521Controller;
