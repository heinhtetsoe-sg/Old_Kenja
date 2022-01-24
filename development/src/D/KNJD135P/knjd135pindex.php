<?php

require_once('for_php7.php');

require_once('knjd135pModel.inc');
require_once('knjd135pQuery.inc');

class knjd135pController extends Controller
{
    public $ModelClassName = "knjd135pModel";
    public $ProgramID      = "KNJD135P";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                case "value_set":
                    $this->callView("knjd135pForm1");
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
                    $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd135pCtl = new knjd135pController();
