<?php

require_once('for_php7.php');


require_once('knjd124hModel.inc');
require_once('knjd124hQuery.inc');

class knjd124hController extends Controller
{
    public $ModelClassName = "knjd124hModel";
    public $ProgramID      = "KNJD124H";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "subclasscd":
                case "chaircd":
                case "reset":
                case "":
                case "sort1":
                case "sort2":
                case "sort9":
                case "sort10101":
                case "sort10201":
                case "sort20101":
                case "sort20201":
                case "sort30201":
                    $this->callView("knjd124hForm1");
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
$knjd124hCtl = new knjd124hController();
