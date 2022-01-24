<?php

require_once('for_php7.php');


require_once('knjm432kModel.inc');
require_once('knjm432kQuery.inc');

class knjm432kController extends Controller
{
    public $ModelClassName = "knjm432kModel";
    public $ProgramID      = "KNJM432K";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "subclasscd":
                case "reset":
                    $this->callView("knjm432kForm1");
                   break 2;
                case "chaircd":
                    $this->callView("knjm432kForm1");
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
$knjm432kCtl = new knjm432kController();
