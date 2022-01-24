<?php

require_once('for_php7.php');
require_once('knjd128vModel.inc');
require_once('knjd128vQuery.inc');

class knjd128vController extends Controller
{
    public $ModelClassName = "knjd128vModel";
    public $ProgramID      = "KNJD128V";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                case "subclasscd":
                    $this->callView("knjd128vForm1");
                    break 2;
                case "chaircd":
                    //$sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjd128vForm1");
                    break 2;
                case "calc":
                    //$sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjd128vForm1");
                    break 2;
                case "update":
                    //$sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "subform1":    //顔写真
                    $this->callView("knjd128vSubform1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $this->callView("knjd128vForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd128vCtl = new knjd128vController();
