<?php

require_once('for_php7.php');

require_once('knjd183cModel.inc');
require_once('knjd183cQuery.inc');

class knjd183cController extends Controller
{
    public $ModelClassName = "knjd183cModel";
    public $ProgramID      = "KNJD183C";

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
                    $this->callView("knjd183cForm1");
                    break 2;
                case "chaircd":
                    $this->callView("knjd183cForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "replace":
                    $this->callView("knjd183cSubForm1");
                    break 2;
                case "replace_update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getReplaceModel();
                    $sessionInstance->setCmd("replace");
                    break 1;
                case "csvInput":    //CSV取込
                    $sessionInstance->getCsvInputModel();
                    $sessionInstance->setCmd("csvInputMain");
                    break 1;
                case "csvOutput":    //CSV出力
                    if (!$sessionInstance->getCsvOutputModel()) {
                        $this->callView("knjd183cForm1");
                    }
                    break 2;
                case "teikei":
                    $this->callView("knjd183cSubForm2");
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
$knjd183cCtl = new knjd183cController();
