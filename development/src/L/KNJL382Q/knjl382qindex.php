<?php

require_once('for_php7.php');

require_once('knjl382qModel.inc');
require_once('knjl382qQuery.inc');

class knjl382qController extends Controller {
    var $ModelClassName = "knjl382qModel";
    var $ProgramID      = "KNJL382Q";

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
                    $this->callView("knjl382qForm2");
                    break 2;
                case "add":
                    if($sessionInstance->getInsertModel()){
                        //変更済みの場合は詳細画面に戻る
                        $sessionInstance->setCmd("edit2");
                        break 1;
                    }else{
                        $this->callView("knjl382qForm2");
                        break 2;
                    }
                case "update":
                    if($sessionInstance->getUpdateModel()){
                        //変更済みの場合は詳細画面に戻る
                        $sessionInstance->setCmd("edit2");
                        break 1;
                    }else{
                        $this->callView("knjl382qForm2");
                        break 2;
                    }
                case "list":
                case "change":
                    $this->callView("knjl382qForm1");
                    break 2;
                case "copy":
                    $sessionInstance->getCopyModel();
                    $this->callView("knjl382qForm1");
                    break 2;
                case "csv":
                    if(!$sessionInstance->getCsvModel()){
                        $this->callView("knjl382qForm1");
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
                    $args["left_src"] = "knjl382qindex.php?cmd=list";
                    $args["right_src"] = "knjl382qindex.php?cmd=edit";
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
$knjl382qCtl = new knjl382qController;
//var_dump($_REQUEST);
?>
