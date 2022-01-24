<?php

require_once('for_php7.php');


require_once('knjd129qModel.inc');
require_once('knjd129qQuery.inc');

class knjd129qController extends Controller
{
    public $ModelClassName = "knjd129qModel";
    public $ProgramID      = "KNJD129Q";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "subclasscd":
                case "reset":
                    $this->callView("knjd129qForm1");
                    break 2;
                case "chaircd":
                    //$sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjd129qForm1");
                    break 2;
                case "update":
                    //$sessionInstance->setAccessLogDetail("U", $ProgramID);
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
$knjd129qCtl = new knjd129qController();
