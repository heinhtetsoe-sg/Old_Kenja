<?php

require_once('for_php7.php');

require_once('knjh711Model.inc');
require_once('knjh711Query.inc');

class knjh711Controller extends Controller
{
    public $ModelClassName = "knjh711Model";
    public $ProgramID      = "KNJH711";
    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "edit":
                case "clear":
                case "btn_def":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh711Form1");
                    break 2;
                case "change_test":
                case "change_period":
                case "change_subclass":
                case "change_facility":
                case "change_hrclass":
                    $sessionInstance->knjh711Model(); //コントロールマスタの呼び出し
                    $this->callView("knjh711Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh711Ctl = new knjh711Controller();
