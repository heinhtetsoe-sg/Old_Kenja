<?php

require_once('for_php7.php');

require_once('knjd133hModel.inc');
require_once('knjd133hQuery.inc');

class knjd133hController extends Controller
{
    public $ModelClassName = "knjd133hModel";
    public $ProgramID      = "KNJD133H";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "csvInputMain":
                case "subclasscd":
                case "reset":
                case "value_set":
                case "back":
                    $this->callView("knjd133hForm1");
                    break 2;
                case "chaircd":
                    //$sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjd133hForm1");
                    break 2;
                case "update":
                    //$sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csvInput":    //CSV取込
                    $sessionInstance->setAccessLogDetail("EI", $ProgramID);
                    $sessionInstance->getCsvInputModel();
                    $sessionInstance->setCmd("csvInputMain");
                    break 1;
                case "csvOutput":    //CSV出力
                    if (!$sessionInstance->getCsvOutputModel()) {
                        $this->callView("knjd133hForm1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    break 2;
                case "replace1":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjd133hSubForm1");
                    break 2;
                case "replace_update1":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->replaceModel();
                    $sessionInstance->setCmd("replace1");
                    break 1;
                case "teikei":
                case "teikei2":
                    $this->callView("knjd133hSubForm2");
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
$knjd133hCtl = new knjd133hController();
