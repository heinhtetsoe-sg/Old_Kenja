<?php

require_once('for_php7.php');


require_once('knjd128fModel.inc');
require_once('knjd128fQuery.inc');

class knjd128fController extends Controller
{
    public $ModelClassName = "knjd128fModel";
    public $ProgramID      = "KNJD128F";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                case "subclasscd":
                    $this->callView("knjd128fForm1");
                    break 2;
                case "chaircd":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjd128fForm1");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
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
$knjd128fCtl = new knjd128fController();
