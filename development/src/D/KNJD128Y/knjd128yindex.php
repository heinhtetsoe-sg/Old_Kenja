<?php

require_once('for_php7.php');


require_once('knjd128yModel.inc');
require_once('knjd128yQuery.inc');

class knjd128yController extends Controller
{
    public $ModelClassName = "knjd128yModel";
    public $ProgramID      = "KNJD128Y";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                case "subclasscd":
                    $this->callView("knjd128yForm1");
                    break 2;
                case "chaircd":
                case "month":
                    //$sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjd128yForm1");
                    break 2;
                case "calc":
                    //$sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjd128yForm1");
                    break 2;
                case "update":
                    //$sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "subform1":    //顔写真
                    $this->callView("knjd128ySubform1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $this->callView("knjd128yForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd128yCtl = new knjd128yController();
