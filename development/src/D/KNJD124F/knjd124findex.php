<?php

require_once('for_php7.php');


require_once('knjd124fModel.inc');
require_once('knjd124fQuery.inc');

class knjd124fController extends Controller
{
    public $ModelClassName = "knjd124fModel";
    public $ProgramID      = "KNJD124F";

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
                    $this->callView("knjd124fForm1");
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
$knjd124fCtl = new knjd124fController();
