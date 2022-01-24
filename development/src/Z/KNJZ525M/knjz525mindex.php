<?php

require_once('for_php7.php');

require_once('knjz525mModel.inc');
require_once('knjz525mQuery.inc');

class knjz525mController extends Controller
{
    public $ModelClassName = "knjz525mModel";
    public $ProgramID      = "KNJZ525M";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
               case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
               case "delete":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $sessionInstance->getDeleteModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
               case "copy":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getCopyModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
               case "error":
                    $this->callView("error");
                    break 2;
               case "kakutei":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz525mForm1");
                    break 2;
               case "clear":
               case "main":
               case "chgPtrn":
               case "":
                    $this->callView("knjz525mForm1");
                    break 2;
               default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz525mCtl = new knjz525mController();
