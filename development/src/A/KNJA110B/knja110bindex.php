<?php

require_once('for_php7.php');
require_once('knja110bModel.inc');
require_once('knja110bQuery.inc');

class knja110bController extends Controller {
    var $ModelClassName = "knja110bModel";
    var $ProgramID      = "KNJA110B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);

        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit": 
                case "clear":  //取消ボタン
                case "subForm": 
                case "subEdit": 
                    $this->callView("knja110bForm2");
                    break 2;
                case "rireki":
                case "histEdit":
                case "changeCmb":
                    $this->callView("knja110bSubHist");
                    break 2;
                case "entGrdRireki":
                case "entGrdEdit":
                case "changeEntGrdCmb":
                    $this->callView("knja110bSubEntGrd");
                    break 2;
                case "execute":
                    $sessionInstance->setAccessLogDetail("E", $ProgramID);
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "histAdd":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $sessionInstance->getUpdateHistModel();
                    break 1;
                case "histUpd":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateHistModel();
                    break 1;
                case "histDel":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $sessionInstance->getUpdateHistModel();
                    break 1;
                case "entGrdHistAdd":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $sessionInstance->getUpdateEntGrdHistModel();
                    break 1;
                case "entGrdHistUpd":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateEntGrdHistModel();
                    break 1;
                case "entGrdHistDel":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $sessionInstance->getUpdateEntGrdHistModel();
                    break 1;
                case "replace":
                case "subReplace":
                    $this->callView("knja110bSubForm1"); //一括更新画面
                    break 2;
                case "subForm2":
                case "subReplaceForm2":
                    $this->callView("knja110bSubForm2"); //履歴のチェックボックスの画面
                    break 2;
                case "replace_update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->ReplaceModel();
                    $sessionInstance->setCmd("replace");
                    break 1;
                case "subReplace_update":/***********************************************/
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->subReplaceModel();
                    $sessionInstance->setCmd("replace");
                    break 1;
                case "add":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $sessionInstance->getAddingModel();
                    $sessionInstance->setCmd("subEdit");
                    break 1;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
//                    $sessionInstance->setCmd("subEdit");
                    break 1;
                case "subUpdate":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getSubUpdateModel();
                    $sessionInstance->setCmd("subEdit");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $search = "?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/A/KNJA110B/knja110bindex.php?cmd=edit") ."&button=1";
                case "back":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP_GHR/index.php" .$search;
                    $args["right_src"] = "knja110bindex.php?cmd=edit";
                    $args["cols"] = "25%,*%";
                    View::frame($args);
                    return;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja110bCtl = new knja110bController;
?>
