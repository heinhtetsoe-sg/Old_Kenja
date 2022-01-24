<?php

require_once('for_php7.php');

require_once('knjz093_finschool_reflectionModel.inc');
require_once('knjz093_finschool_reflectionQuery.inc');

class knjz093_finschool_reflectionController extends Controller
{
    public $ModelClassName = "knjz093_finschool_reflectionModel";
    public $ProgramID      = "KNJZ093A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
//                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("sel");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "sel":
                case "changeType":
//                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz093_finschool_reflectionForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz093_finschool_reflectionCtl = new knjz093_finschool_reflectionController();
