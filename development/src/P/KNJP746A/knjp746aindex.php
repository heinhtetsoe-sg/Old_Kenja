<?php

require_once('for_php7.php');

require_once('knjp746aModel.inc');
require_once('knjp746aQuery.inc');

class knjp746aController extends Controller
{
    public $ModelClassName = "knjp746aModel";
    public $ProgramID      = "KNJP746A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "resultMain": //処理実行後
                    $this->callView("knjp746aForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("resultMain");
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
$knjp746aCtl = new knjp746aController();
//var_dump($_REQUEST);
