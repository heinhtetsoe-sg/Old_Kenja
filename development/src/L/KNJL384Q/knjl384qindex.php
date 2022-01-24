<?php

require_once('for_php7.php');

require_once('knjl384qModel.inc');
require_once('knjl384qQuery.inc');

class knjl384qController extends Controller {
    var $ModelClassName = "knjl384qModel";
    var $ProgramID      = "KNJl384q";

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
                    $this->callView("knjl384qForm2");
                    break 2;
                case "add":
                    if($sessionInstance->getInsertModel()){
                        //変更済みの場合は詳細画面に戻る
                        $sessionInstance->setCmd("edit2");
                        break 1;
                    }else{
                        $this->callView("knjl384qForm2");
                        break 2;
                    }
                case "update":
                    if($sessionInstance->getUpdateModel()){
                        //変更済みの場合は詳細画面に戻る
                        $sessionInstance->setCmd("edit2");
                        break 1;
                    }else{
                        $this->callView("knjl384qForm2");
                        break 2;
                    }
                case "list":
                    $this->callView("knjl384qForm1");
                    break 2;
                case "copy":
                    $sessionInstance->getCopyModel();
                    $this->callView("knjl384qForm1");
                    break 2;
                case "csv":
                    if(!$sessionInstance->getCsvModel()){
                        $this->callView("knjl384qForm1");
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
                    $args["left_src"] = "knjl384qindex.php?cmd=list";
                    $args["right_src"] = "knjl384qindex.php?cmd=edit";
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
$knjl384qCtl = new knjl384qController;
//var_dump($_REQUEST);
?>
