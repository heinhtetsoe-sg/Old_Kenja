<?php

require_once('for_php7.php');
require_once('knjh720Model.inc');
require_once('knjh720Query.inc');

class knjh720Controller extends Controller
{
    public $ModelClassName = "knjh720Model";
    public $ProgramID      = "KNJH720";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "reset":
                case "testdiv":
                case "classcd":
                case "subclasscd":
                case "faccd":
                    $this->callView("knjh720Form1");
                    break 2;
                case "exec":
                    if ($sessionInstance->execCSV()) {
                        $this->callView("knjh720Form1");
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
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh720Ctl = new knjh720Controller();
