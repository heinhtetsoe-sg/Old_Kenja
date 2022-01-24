<?php

require_once('for_php7.php');


require_once('knjd129dModel.inc');
require_once('knjd129dQuery.inc');

class knjd129dController extends Controller
{
    public $ModelClassName = "knjd129dModel";
    public $ProgramID      = "KNJD129D";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                case "subclasscd":
                    $this->callView("knjd129dForm1");
                    break 2;
                case "chaircd":
                    $this->callView("knjd129dForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $this->callView("knjd129dForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd129dCtl = new knjd129dController();
