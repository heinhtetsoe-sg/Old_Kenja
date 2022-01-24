<?php

require_once('for_php7.php');

require_once('knje444aModel.inc');
require_once('knje444aQuery.inc');

class knje444aController extends Controller
{
    public $ModelClassName = "knje444aModel";
    public $ProgramID      = "KNJE444A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "edit":
                    $this->callView("knje444aForm1");
                    break 2;
                case "csv":
                    if (!$sessionInstance->getCsvModel()) {
                        //変更済みの場合は詳細画面に戻る
                        $sessionInstance->setCmd("edit");
                        break 1;
                    }
                    break 2;
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
$knje444aCtl = new knje444aController();
