<?php

require_once('for_php7.php');

require_once('knje410Model.inc');
require_once('knje410Query.inc');

class knje410Controller extends Controller
{
    public $ModelClassName = "knje410Model";
    public $ProgramID      = "KNJE410";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                    $this->callView("knje410Form1");
                    break 2;
                case "recalc":
                    $this->callView("knje410Form1");
                    break 2;
                case "houkoku":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateEdboardModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "uploadCsv":
                    $sessionInstance->getUploadCsvModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "downloadError":
                case "downloadCsv":
                    if (!$sessionInstance->getDownloadCsvModel()) {
                        $this->callView("knje410Form1");
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
$knje410Ctl = new knje410Controller();
