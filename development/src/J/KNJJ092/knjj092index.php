<?php

require_once('for_php7.php');

require_once('knjj092Model.inc');
require_once('knjj092Query.inc');

class knjj092Controller extends Controller {
    var $ModelClassName = "knjj092Model";
    var $ProgramID      = "KNJJ092";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            $sessionInstance->knjj092Model();        //コントロールマスタの呼び出し
            switch (trim($sessionInstance->cmd)) {
                case "search":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->schreg_chk($sessionInstance->field["SCHREGNO"]);
                case "edit":
                case "reset":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjj092Form2");
                    break 2;
                case "add":
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":
                case "committeechange":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjj092Form1");
                    break 2;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "Ikkatsu":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjj092Ikkatsu");
                    break 2;
                case "ikkatsu_Insert":
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $sessionInstance->getSubInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("Ikkatsu");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjj092index.php?cmd=list";
                    $args["right_src"] = "knjj092index.php?cmd=edit";
                    $args["cols"] = "50%,50%";
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
$knjj092Ctl = new knjj092Controller;
//var_dump($_REQUEST);
?>
