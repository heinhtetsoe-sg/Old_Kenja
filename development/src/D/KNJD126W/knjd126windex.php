<?php

require_once('for_php7.php');

require_once('knjd126wModel.inc');
require_once('knjd126wQuery.inc');

class knjd126wController extends Controller
{
    public $ModelClassName = "knjd126wModel";
    public $ProgramID      = "KNJD126W";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "form1":
                case "chgGrd1":
                case "chgSub1":
                case "select1":
                case "reset":
                case "back":
                    $this->callView("knjd126wForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("form1");
                    break 1;
                case "delete":
                    $sessionInstance->getDeleteModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("form1");
                    break 1;
                case "form2":
                case "chgGrd2":
                case "chgSub2":
                case "select2":
                case "form2_reset":
                    $this->callView("knjd126wForm2");
                    break 2;
                case "form2_update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("form2");
                    break 1;
                case "delete2":
                    $sessionInstance->getDeleteModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("form2");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->setCmd("form1");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd126wCtl = new knjd126wController();
