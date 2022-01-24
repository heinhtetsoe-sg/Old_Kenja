<?php

require_once('for_php7.php');

require_once('knjd156vModel.inc');
require_once('knjd156vQuery.inc');

class knjd156vController extends Controller {
    var $ModelClassName = "knjd156vModel";
    var $ProgramID      = "KNJD156V";
    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                    $this->callView("knjd156vForm2");
                    break 2;
                case "add":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->setAccessLogDetail("I", $ProgramID); 
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID); 
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "reset":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":
                    $this->callView("knjd156vForm1");
                    break 2;
                case "coursename":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $this->callView("knjd156vForm1");
                    break 2;
                case "delete":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->setAccessLogDetail("D", $ProgramID); 
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "copy":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID); 
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("list");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjd156vindex.php?cmd=list";
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
$knjd156vCtl = new knjd156vController;
//var_dump($_REQUEST);
?>
