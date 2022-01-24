<?php

require_once('for_php7.php');

require_once('knjd133cModel.inc');
require_once('knjd133cQuery.inc');

class knjd133cController extends Controller
{
    public $ModelClassName = "knjd133cModel";
    public $ProgramID      = "KNJD133C";

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
                    $this->callView("knjd133cForm1");
                    break 2;
                case "chaircd":
                    $this->callView("knjd133cForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "replace":
                    $this->callView("knjd133cSubForm1");
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
                        $this->callView("knjd133cForm1");
                    }
                    break 2;
                case "teikei":
                    $this->callView("knjd133cSubForm2");
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
$knjd133cCtl = new knjd133cController();
