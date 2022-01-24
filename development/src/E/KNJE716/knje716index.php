<?php

require_once('for_php7.php');

require_once('knje716Model.inc');
require_once('knje716Query.inc');

class knje716Controller extends Controller
{
    public $ModelClassName = "knje716Model";
    public $ProgramID      = "KNJE716";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "reset":
                case "change_header":
                    $this->callView("knje716Form1");
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
$knje716Ctl = new knje716Controller();
