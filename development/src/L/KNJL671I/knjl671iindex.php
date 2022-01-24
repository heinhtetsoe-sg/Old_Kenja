<?php
require_once('for_php7.php');

require_once('knjl671iModel.inc');
require_once('knjl671iQuery.inc');

class knjl671iController extends Controller
{
    public $ModelClassName = "knjl671iModel";
    public $ProgramID      = "KNJL671I";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "reset":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjl671iForm2");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":
                case "chagneSort":
                case "listSearch":
                    $this->callView("knjl671iForm1");
                    break 2;
                case "headerCsv":     //ヘッダーダウンロード
                    $this->checkAuth(DEF_UPDATABLE);
                    if (!$sessionInstance->getHeaderCsvModel()) {
                        $this->callView("knjl671iForm2");
                    }
                    break 2;
                case "downloadCsv":     //CSVダウンロード
                    $this->checkAuth(DEF_UPDATABLE);
                    if (!$sessionInstance->getDownloadCsvModel()) {
                        $this->callView("knjl671iForm2");
                    }
                    break 2;
                case "uploadCsv":       //CSV取込
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUploadCsvModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "downloadErr":     //CSVエラーダウンロード
                    $this->checkAuth(DEF_UPDATABLE);
                    if (!$sessionInstance->getErrorCsvDownloadModel()) {
                        $this->callView("knjl671iForm2");
                    }
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
                    $args["left_src"] = "knjl671iindex.php?cmd=list";
                    $args["right_src"] = "knjl671iindex.php?cmd=edit";
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
$knjl671iCtl = new knjl671iController();
//var_dump($_REQUEST);
