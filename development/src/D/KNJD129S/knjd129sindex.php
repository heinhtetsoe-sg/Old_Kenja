<?php

require_once('for_php7.php');


require_once('knjd129sModel.inc');
require_once('knjd129sQuery.inc');

class knjd129sController extends Controller
{
    public $ModelClassName = "knjd129sModel";
    public $ProgramID      = "KNJD129S";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                case "subclasscd":
                    $this->callView("knjd129sForm1");
                    break 2;
                case "chaircd":
                    $this->callView("knjd129sForm1");
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
                    $this->callView("knjd129sForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd129sCtl = new knjd129sController();
