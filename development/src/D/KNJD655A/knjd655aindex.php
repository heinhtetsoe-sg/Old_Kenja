<?php

require_once('for_php7.php');

require_once('knjd655aModel.inc');
require_once('knjd655aQuery.inc');

class knjd655aController extends Controller {
    var $ModelClassName = "knjd655aModel";
    var $ProgramID      = "KNJD655A";
    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                    $this->callView("knjd655aForm2");
                    break 2;
                case "add":
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "reset":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":
                case "coursename":
                    $this->callView("knjd655aForm1");
                    break 2;
                case "delete":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjd655aindex.php?cmd=list";
                    $args["right_src"] = ""; //レスポンスのタイミング？でセッションの値があったりなかったりするので、右フレームは左フレームを読込んでから読込むようにする
                    $args["cols"] = "55%,45%";
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
$knjd655aCtl = new knjd655aController;
//var_dump($_REQUEST);
?>
