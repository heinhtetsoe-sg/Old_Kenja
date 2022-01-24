<?php

require_once('for_php7.php');

require_once('knjz091a_2Model.inc');
require_once('knjz091a_2Query.inc');

class knjz091a_2Controller extends Controller
{
    public $ModelClassName = "knjz091a_2Model";
    public $ProgramID      = "KNJZ091A";
    
    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "chgPref":
                    $sessionInstance->setAccessLogDetail("S", "KNJZ091A_2");
                    $this->callView("knjz091a_2Form2");
                    break 2;
                case "add":
                    $sessionInstance->setAccessLogDetail("I", "KNJZ091A_2");
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", "KNJZ091A_2");
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "reset":
                    $sessionInstance->setAccessLogDetail("S", "KNJZ091A_2");
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":
                    $this->callView("knjz091a_2Form1");
                    break 2;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", "KNJZ091A_2");
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "uploadCsv":
                    $sessionInstance->getUploadCsvModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "downloadHead":
                case "downloadCsv":
                case "downloadError":
                    if (!$sessionInstance->getDownloadCsvModel()) {
                        $this->callView("knjz091a_2Form2");
                    }
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjz091a_2index.php?cmd=list";
                    $args["right_src"] = "knjz091a_2index.php?cmd=edit";
                    $args["cols"] = "30%,70%";
                    View::frame($args);
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz091a_2Ctl = new knjz091a_2Controller();
