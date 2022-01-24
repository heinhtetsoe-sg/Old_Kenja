<?php

require_once('for_php7.php');

require_once('knjd133tModel.inc');
require_once('knjd133tQuery.inc');

class knjd133tController extends Controller
{
    public $ModelClassName = "knjd133tModel";
    public $ProgramID      = "KNJD133T";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "csvInputMain":
                case "subclasscd":
                case "reset":
                    $this->callView("knjd133tForm1");
                    break 2;
                case "chaircd":
                    $this->callView("knjd133tForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csvInput":    //CSV取込
                    $sessionInstance->getCsvInputModel();
                    $sessionInstance->setCmd("csvInputMain");
                    break 1;
                case "csvOutput":    //CSV出力
                    if (!$sessionInstance->getCsvOutputModel()) {
                        $this->callView("knjd133tForm1");
                    }
                    break 2;
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
$knjd133tCtl = new knjd133tController();
