<?php

require_once('for_php7.php');


require_once('knjd133Model.inc');
require_once('knjd133Query.inc');

class knjd133Controller extends Controller
{
    public $ModelClassName = "knjd133Model";
    public $ProgramID      = "KNJD133";

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
                    $this->callView("knjd133Form1");
                    break 2;
                case "chaircd":
                    //$sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjd133Form1");
                    break 2;
                case "update":
                    //$sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "replace":
                    $this->callView("knjd133SubForm1");
                    break 2;
                case "replace_update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    //$sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getReplaceModel();
                    $sessionInstance->setCmd("replace");
                    break 1;
                case "csvInput":    //CSV取込
                    //$sessionInstance->setAccessLogDetail("EI", $ProgramID);
                    $sessionInstance->getCsvInputModel();
                    $sessionInstance->setCmd("csvInputMain");
                    break 1;
                case "csvOutput":    //CSV出力
                    if (!$sessionInstance->getCsvOutputModel()) {
                        $this->callView("knjd133Form1");
                    }
                    //$sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    break 2;
                case "teikei":
                case "teikei2":
                    $this->callView("knjd133SubForm2");
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
$knjd133Ctl = new knjd133Controller();
