<?php

require_once('for_php7.php');

require_once('knjl386qModel.inc');
require_once('knjl386qQuery.inc');

class knjl386qController extends Controller {
    var $ModelClassName = "knjl386qModel";
    var $ProgramID      = "KNJL386Q";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "edit2":
                case "edit3":
                case "reset":
                case "shori_change":
                    $this->callView("knjl386qForm2");
                    break 2;
                case "add":
                    if($sessionInstance->getInsertModel()){
                        //変更済みの場合は詳細画面に戻る
                        $sessionInstance->setCmd("edit2");
                        break 1;
                    }else{
                        $this->callView("knjl386qForm2");
                        break 2;
                    }
                case "update":
                    if($sessionInstance->getUpdateModel()){
                        //変更済みの場合は詳細画面に戻る
                        $sessionInstance->setCmd("edit2");
                        break 1;
                    }else{
                        $this->callView("knjl386qForm2");
                        break 2;
                    }
                case "list":
                    $this->callView("knjl386qForm1");
                    break 2;
                case "copy":
                    $sessionInstance->getCopyModel();
                    $this->callView("knjl386qForm1");
                    break 2;
                case "csv":
                    if(!$sessionInstance->getCsvModel()){
                        $this->callView("knjl386qForm1");
                    }
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
                    $args["left_src"] = "knjl386qindex.php?cmd=list";
                    $args["right_src"] = "knjl386qindex.php?cmd=edit";
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
$knjl386qCtl = new knjl386qController;
//var_dump($_REQUEST);
?>
