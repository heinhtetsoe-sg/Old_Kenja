<?php

require_once('for_php7.php');

require_once('knjp805aModel.inc');
require_once('knjp805aQuery.inc');

class knjp805aController extends Controller
{
    public $ModelClassName = "knjp805aModel";
    public $ProgramID      = "KNJP805A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "cmdStart":
                case "edit":
                case "read":
                case "readTest":
                case "chengeDiv":
                    $this->callView("knjp805aForm1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("read");
                    break 1;
                case "updateTest":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("readTest");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->setCmd("cmdStart");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$KNJP805ACtl = new knjp805aController();
