<?php

require_once('for_php7.php');

require_once('knja062s1Model.inc');
require_once('knja062s1Query.inc');

class knja062s1Controller extends Controller {
    var $ModelClassName = "knja062s1Model";
    var $ProgramID      = "KNJA062S1";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "reset":
                    $this->callView("knja062s1Form2");
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
                case "list":
                case "list_edit":
                case "updlist":
                    $this->callView("knja062s1Form1");
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
                    $args["left_src"] = "knja062s1index.php?cmd=list";
                    $args["right_src"] = "knja062s1index.php?cmd=edit";
                    $args["cols"] = "40%,60%";
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
$knja062s1Ctl = new knja062s1Controller;
//var_dump($_REQUEST);
?>
