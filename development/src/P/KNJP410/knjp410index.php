<?php

require_once('for_php7.php');

require_once('knjp410Model.inc');
require_once('knjp410Query.inc');

class knjp410Controller extends Controller
{
    public $ModelClassName = "knjp410Model";

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
                case "knjp410":
                    $sessionInstance->knjp410Model();
                    $this->callView("knjp410Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp410Ctl = new knjp410Controller();
