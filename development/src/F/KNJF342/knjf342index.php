<?php

require_once('for_php7.php');

require_once('knjf342Model.inc');
require_once('knjf342Query.inc');

class knjf342Controller extends Controller
{
    public $ModelClassName = "knjf342Model";
    public $ProgramID      = "KNJF342";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                case "fixed":
                    $this->callView("knjf342Form1");
                    break 2;
                case "fixedLoad":
                    $this->callView("knjf342fixedForm1");
                    break 2;
                case "recalc":
                    $this->callView("knjf342Form1");
                    break 2;
                case "houkoku":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateEdboardModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    if (!$sessionInstance->getUpdateModel()) {
                        //更新すべきデータなし
                        $this->callView("knjf342Form1");
                        break 2;
                    } else {
                        //更新成功時は確定処理のポップアップを表示
                        $sessionInstance->setCmd("fixed");
                        break 1;
                    }
                    // no break
                case "fixedUpd":
                    //確定処理＞確定ボタン押下時
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getFixedUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csv":
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjf342Form1");
                    }
                    break 2;
                case "error":
                    $this->callView("error");
                    // no break
                case "read":
                    $sessionInstance->setCmd("main");
                    break 1;
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
$knjf342Ctl = new knjf342Controller();
