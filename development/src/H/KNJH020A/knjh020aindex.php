<?php

require_once('for_php7.php');
require_once('knjh020aModel.inc');
require_once('knjh020aQuery.inc');

class knjh020aController extends Controller
{
    public $ModelClassName = "knjh020aModel";
    public $ProgramID      = "KNJH020A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "reset":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh020aForm1");
                    break 2;
                case "subForm2":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh020aSubForm2");
                    break 2;
                case "update":
                case "subUpdate":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    if (!$sessionInstance->auth) {
                        $this->checkAuth(DEF_UPDATE_RESTRICT);
                    }
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    if (!$sessionInstance->auth) {
                        $this->checkAuth(DEF_UPDATE_RESTRICT);
                    }
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "end":
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "rireki":
                case "histEdit":
                case "changeCmb":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh020aSubHist");
                    break 2;
                case "rireki2":
                case "histEdit2":
                case "changeCmb2":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh020aSubHistHosyouNin");
                    break 2;
                case "histAdd":
                case "histAdd2":
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    if (!$sessionInstance->auth) {
                        $this->checkAuth(DEF_UPDATE_RESTRICT);
                    }
                    $sessionInstance->getUpdateHistModel();
                    break 1;
                case "histUpd":
                case "histUpd2":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    if (!$sessionInstance->auth) {
                        $this->checkAuth(DEF_UPDATE_RESTRICT);
                    }
                    $sessionInstance->getUpdateHistModel();
                    break 1;
                case "histDel":
                case "histDel2":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    if (!$sessionInstance->auth) {
                        $this->checkAuth(DEF_UPDATE_RESTRICT);
                    }
                    $sessionInstance->getUpdateHistModel();
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "add":
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    if (!$sessionInstance->auth) {
                        $this->checkAuth(DEF_UPDATE_RESTRICT);
                    }
                    $sessionInstance->getAddingModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "":
                    //分割フレーム作成
                    $search = "?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/H/KNJH020A/knjh020aindex.php?cmd=edit") ."&button=1";
                    // no break
                case "back":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php" .$search;
                    $args["right_src"] = "knjh020aindex.php?cmd=edit";
                    $args["cols"] = "22%,*%";
                    View::frame($args);
                    return;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}で!す"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh020aCtl = new knjh020aController();
