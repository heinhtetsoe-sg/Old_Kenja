<?php

require_once('for_php7.php');

require_once('knjp411Model.inc');
require_once('knjp411Query.inc');

class knjp411Controller extends Controller
{
    public $ModelClassName = "knjp411Model";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":    //CSV取込
                    if (!$sessionInstance->getDownloadModel()) {
                        //変更済みの場合は詳細画面に戻る
                        $sessionInstance->setCmd("main");
                        break 1;
                    }
                    break 2;
                case "":
                case "main":
                case "knjp411":
                    $sessionInstance->knjp411Model();
                    $this->callView("knjp411Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp411Ctl = new knjp411Controller();
