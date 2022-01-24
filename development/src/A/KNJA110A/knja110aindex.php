<?php

require_once('for_php7.php');

require_once('knja110aModel.inc');
require_once('knja110aQuery.inc');

class knja110aController extends Controller
{
    public $ModelClassName = "knja110aModel";
    public $ProgramID      = "KNJA110A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);

        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "clear":  //取消ボタン
                case "subForm":
                case "subEdit":
                    $this->callView("knja110aForm2");
                    break 2;
                case "nationality2": //記録備考参照
                    $this->callView("knja110aNationality2");
                    break 2;
                case "nationalityUpd":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getNationalityUpdateModel();
                    $sessionInstance->setCmd("subForm");
                    break 1;
                case "rireki":
                case "histEdit":
                case "changeCmb":
                    $this->callView("knja110aSubHist");
                    break 2;
                case "entGrdRireki":
                case "entGrdEdit":
                case "changeEntGrdCmb":
                    $this->callView("knja110aSubEntGrd");
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
                    $this->callView("knja110aSubForm1"); //一括更新画面
                    break 2;
                case "subForm2":
                case "subReplaceForm2":
                    $this->callView("knja110aSubForm2"); //履歴のチェックボックスの画面
                    break 2;
                case "replace_update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->replaceModel();
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
                    $search  = "?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/A/KNJA110A/knja110aindex.php?cmd=edit") ."&button=1";
                    if ($sessionInstance->Properties["useSpecial_Support_Hrclass"] != "1" && $sessionInstance->Properties["useFi_Hrclass"] != "1") {
                        $search .= "&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&URL_SCHOOLCD={$sessionInstance->urlSchoolCd}&MN_ID={$sessionInstance->mnId}";
                    }
                    // no break
                case "back":
                    //分割フレーム作成
                    if ($sessionInstance->Properties["useSpecial_Support_Hrclass"] == "1" || $sessionInstance->Properties["useFi_Hrclass"] == "1") {
                        $args["left_src"] = REQUESTROOT ."/X/KNJXEXP_GHR/index.php" .$search;
                    } else {
                        $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php" .$search;
                    }
                    $args["right_src"] = "knja110aindex.php?cmd=edit";
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
$knja110aCtl = new knja110aController();
