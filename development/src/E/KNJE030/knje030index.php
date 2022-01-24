<?php

require_once('for_php7.php');

require_once('knje030Model.inc');
require_once('knje030Query.inc');

class knje030Controller extends Controller {
    var $ModelClassName = "knje030Model";
    var $ProgramID      = "KNJE030";
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "class":
                    $this->callView("knje030Form2");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID); 
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $this->callView("knje030Form1");
                    break 2;
                case "delete":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("D", $ProgramID); 
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
               case "reset":
                   $sessionInstance->setCmd("edit");
                   break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knje030index.php?cmd=list";
                    $args["right_src"] = "knje030index.php?cmd=edit";
                    $args["cols"] = "39%,61%";
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
$knje030Ctl = new knje030Controller;
//var_dump($_REQUEST);
?>
