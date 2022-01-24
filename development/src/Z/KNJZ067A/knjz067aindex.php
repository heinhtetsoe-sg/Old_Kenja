<?php

require_once('for_php7.php');

require_once('knjz067aModel.inc');
require_once('knjz067aQuery.inc');

class knjz067aController extends Controller
{
    public $ModelClassName = "knjz067aModel";
    public $ProgramID      = "KNJZ067A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "edit2":
                case "edit3":
                case "reset":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz067aForm2");
                    break 2;
                case "list":
                case "combo":
                    $this->callView("knjz067aForm1");
                    break 2;
                case "copy":
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("list");
                    break 1;
                case "add":
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                /*case "uploadCsv":
                    $sessionInstance->getUploadCsvModel();
                    $sessionInstance->setCmd("edit");
                    break 1;*/
                case "downloadHead":
                case "downloadCsv":
                case "downloadError":
                    if (!$sessionInstance->getDownloadCsvModel()) {
                        $this->callView("knjz067aForm1");
                    }
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"]  = "knjz067aindex.php?cmd=list";
                    $args["right_src"] = "knjz067aindex.php?cmd=edit";
                    $args["cols"] = "50%, 50%";
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
$knjz067aCtl = new knjz067aController();
