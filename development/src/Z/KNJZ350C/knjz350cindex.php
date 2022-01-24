<?php

require_once('for_php7.php');

require_once('knjz350cModel.inc');
require_once('knjz350cQuery.inc');

class knjz350cController extends Controller
{
    public $ModelClassName = "knjz350cModel";
    public $ProgramID      = "KNJZ350C";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "copy":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("sel");
                    break 1;
                case "update1":         //出欠表示項目
                case "update2":         //出欠入力可項目
                case "update3":         //累積表示項目
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("sel");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "sel":
                case "change":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz350cForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz350cCtl = new knjz350cController();
