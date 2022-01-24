<?php

require_once('for_php7.php');

require_once('knjl370qModel.inc');
require_once('knjl370qQuery.inc');

class knjl370qController extends Controller {
    var $ModelClassName = "knjl370qModel";
    var $ProgramID      = "KNJL370Q";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "change":
                case "change1":
                case "change2":
                case "edit2":
                case "edit3":
                case "reset":
                case "shori_change":
                    $this->callView("knjl370qForm2");
                    break 2;
                case "add":
                    if($sessionInstance->getInsertModel()){
                        //変更済みの場合は詳細画面に戻る
                        $sessionInstance->setCmd("edit2");
                        break 1;
                    }else{
                        $this->callView("knjl370qForm2");
                        break 2;
                    }
                case "update":
                    if($sessionInstance->getUpdateModel()){
                        //変更済みの場合は詳細画面に戻る
                        $sessionInstance->setCmd("edit2");
                        break 1;
                    }else{
                        $this->callView("knjl370qForm2");
                        break 2;
                    }
                case "list":
                case "search":
                    $this->callView("knjl370qForm1");
                    break 2;
                case "delete":
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit3");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjl370qindex.php?cmd=list";
                    $args["right_src"] = "knjl370qindex.php?cmd=edit";
                    $args["cols"] = "35%,65%";
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
$knjl370qCtl = new knjl370qController;
//var_dump($_REQUEST);
?>
