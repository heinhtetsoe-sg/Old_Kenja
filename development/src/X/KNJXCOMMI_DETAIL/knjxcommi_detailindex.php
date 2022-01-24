<?php

require_once('for_php7.php');

require_once('knjxcommi_detailModel.inc');
require_once('knjxcommi_detailQuery.inc');

class knjxcommi_detailController extends Controller
{
    public $ModelClassName = "knjxcommi_detailModel";
    public $ProgramID      = "KNJXCOMMI_DETAIL";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                    $this->callView("knjxcommi_detailForm2");
                    break 2;
                case "add":
                case "update":
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "clear":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":
                    $this->callView("knjxcommi_detailForm1");
                    break 2;
                case "delete":
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjxcommi_detailindex.php?cmd=list";
                    $args["right_src"] = "knjxcommi_detailindex.php?cmd=edit";
                    $args["cols"] = "60%,40%";
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
$knjxcommi_detailCtl = new knjxcommi_detailController();
