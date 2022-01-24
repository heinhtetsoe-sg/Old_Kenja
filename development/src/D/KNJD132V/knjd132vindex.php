<?php

require_once('for_php7.php');

require_once('knjd132vModel.inc');
require_once('knjd132vQuery.inc');

class knjd132vController extends Controller
{
    public $ModelClassName = "knjd132vModel";
    public $ProgramID      = "KNJD132V";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                case "value_set":
                    $this->callView("knjd132vForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "teikei":
                    $this->callView("knjd132vSubForm1");
                    break 2;
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
$knjd132vCtl = new knjd132vController();
