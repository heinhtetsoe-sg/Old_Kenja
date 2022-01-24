<?php

require_once('for_php7.php');

require_once('knjd133jModel.inc');
require_once('knjd133jQuery.inc');

class knjd133jController extends Controller
{
    public $ModelClassName = "knjd133jModel";
    public $ProgramID      = "KNJD133J";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "csvInputMain":
                case "subclasscd":
                case "reset":
                case "back":
                case "value_set":
                    $this->callView("knjd133jForm1");
                    break 2;
                case "chaircd":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjd133jForm1");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "replace":
                    $this->callView("knjd133jSubForm1");
                    break 2;
                case "replace_update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getReplaceModel();
                    $sessionInstance->setCmd("replace");
                    break 1;
                case "csvInput":    //CSV取込
                    $sessionInstance->setAccessLogDetail("EI", $ProgramID);
                    $sessionInstance->getCsvInputModel();
                    $sessionInstance->setCmd("csvInputMain");
                    break 1;
                case "csvOutput":    //CSV出力
                    if (!$sessionInstance->getCsvOutputModel()) {
                        $this->callView("knjd133jForm1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    break 2;
                case "teikei":
                case "teikei2":
                case "teikei3":
                    $this->callView("knjd133jSubForm2");
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
$knjd133jCtl = new knjd133jController();
