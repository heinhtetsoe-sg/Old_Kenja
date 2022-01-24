<?php

require_once('for_php7.php');

require_once('knjf345Model.inc');
require_once('knjf345Query.inc');

class knjf345Controller extends Controller
{
    public $ModelClassName = "knjf345Model";
    public $ProgramID      = "KNJF345";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                case "fixed":
                    $this->callView("knjf345Form1");
                    break 2;
                case "fixedLoad":
                    $this->callView("knjf345fixedForm1");
                    break 2;
                case "recalc":
                    $this->callView("knjf345Form1");
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
                        $this->callView("knjf345Form1");
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
                        $this->callView("knjf345Form1");
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
$knjf345Ctl = new knjf345Controller();
