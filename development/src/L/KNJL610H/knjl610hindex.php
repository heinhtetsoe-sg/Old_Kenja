<?php
require_once('knjl610hModel.inc');
require_once('knjl610hQuery.inc');

class knjl610hController extends Controller
{
    public $ModelClassName = "knjl610hModel";
    public $ProgramID      = "KNJL610H";
    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "reset":
                case "search":
                case "new":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjl610hForm2");
                    break 2;
                case "headerCsv":     //CSV書出
                    $this->checkAuth(DEF_UPDATABLE);
                    if (!$sessionInstance->getHeaderCsvModel()) {
                        $this->callView("knjl610hForm2");
                    }
                    break 2;
                case "uploadCsv":       //CSV取込
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUploadCsvModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "downloadCsv":     //CSV書出
                    $this->checkAuth(DEF_UPDATABLE);
                    if (!$sessionInstance->getDownloadCsvModel()) {
                        $this->callView("knjl610hForm2");
                    }
                    break 2;
                case "add":
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":
                case "chagneSort":
                    $this->callView("knjl610hForm1");
                    break 2;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjl610hindex.php?cmd=list";
                    $args["right_src"] = "knjl610hindex.php?cmd=edit";
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
$knjl610hCtl = new knjl610hController();
//var_dump($_REQUEST);
