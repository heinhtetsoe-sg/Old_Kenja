<?php

require_once('for_php7.php');

require_once('knjz290a_2Model.inc');
require_once('knjz290a_2Query.inc');

class knjz290a_2Controller extends Controller
{
    public $ModelClassName = "knjz290a_2Model";
    public $ProgramID      = "KNJZ290A_2";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "new":
                case "search":
                case "edit":
                case "change":
                    $this->callView("knjz290a_2Form2");
                    break 2;
                case "add":
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "reset":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":
                case "chg_year":
                    $this->callView("knjz290a_2Form1");
                    break 2;
                case "delete":
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
                        $this->callView("knjz290a_2Form1");
                    }
                    break 2;
                case "subform1":
                case "subform1_clear":
                case "list2":
                    $this->callView("knjz290a_2SubForm1");
                    break 2;
                case "subform1_add":
                    $sessionInstance->getInsertSubformModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("subform1");
                    break 1;
                case "subform1_update":
                    $sessionInstance->getUpdateSubformModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("subform1");
                    break 1;
                case "subform1_delete":
                    $sessionInstance->getDeleteSubformModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("subform1");
                    break 1;
                case "copy":
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "execute":
                    $sessionInstance->setAccessLogDetail("E", $ProgramID);
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjz290a_2index.php?cmd=list";
                    $args["right_src"] = "knjz290a_2index.php?cmd=edit";
                    $args["cols"] = "25%,75%";
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
$knjz290a_2Ctl = new knjz290a_2Controller();
