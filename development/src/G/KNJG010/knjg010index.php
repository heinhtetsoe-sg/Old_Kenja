<?php

require_once('for_php7.php');


// kanji=漢字

require_once('knjg010Model.inc');
require_once('knjg010Query.inc');

class knjg010Controller extends Controller
{
    public $ModelClassName = "knjg010Model";
//    var $ProgramID      = "knjg010";
    public $ProgramID      = "KNJG010";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "search":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->schregChk($sessionInstance->field["SCHREGNO"]);
                case "edit":
                case "edit2":
                case "edit3":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjg010Form1");
                    exit;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    if ($sessionInstance->schregChk($sessionInstance->field["SCHREGNO"])) {
                        $sessionInstance->getInsertModel($sessionInstance->cmd);
                    }
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit2");
                    break 1;
                case "add":
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    if ($sessionInstance->schregChk($sessionInstance->field["SCHREGNO"])) {
                        $sessionInstance->getInsertModel($sessionInstance->cmd);
                    }
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit2");
                    break 1;
                case "cancel":
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $sessionInstance->getCancelModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit3");
                    break 1;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $sessionInstance->deleteIssueModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("list");
                    break 1;
                case "issue":
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $sessionInstance->getIssueModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("list3");
                    break 1;
                case "list1":   //通常・申請書登録時
                case "list":    //通常・申請書登録時
                case "list2":   //発行済み申請書表示
                case "list3":   //申請書発行時
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjg010Form2");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->knjg010Model();
                    //分割フレーム作成
                    $args["right_src"] = "knjg010index.php?cmd=list1";
                    $args["edit_src"] = "knjg010index.php?cmd=edit";
                    $args["rows"] = "50%,*%";
                    View::frame($args, "frame3.html");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjg010Ctl = new knjg010Controller();
//var_dump($_REQUEST);
?>
