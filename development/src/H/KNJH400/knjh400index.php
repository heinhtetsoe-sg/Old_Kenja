<?php

require_once('for_php7.php');

require_once('knjh400Model.inc');
require_once('knjh400Query.inc');

class knjh400Controller extends Controller
{
    public $ModelClassName = "knjh400Model";
    public $ProgramID      = "KNJH400";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "radio":
                case "STAFFSORTCLICK":
                case "DATESORTCLICK":
                case "TITLESORTCLICK":
                case "subEnd":
                    $this->callView("knjh400Form1");
                    break 2;
                case "insert":
                case "upd":
                case "reset":
                    $this->callView("knjh400SubForm1");
                    break 2;
                case "updateSub":
                case "insertSub":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("reset");
                    break 1;
                case "delete":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXSEARCH5/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&URL_SCHOOLCD={$sessionInstance->urlSchoolCd}&MN_ID={$sessionInstance->mnId}&TARGET=right_frame&PATH=" .urlencode("/H/KNJH400/knjh400index.php?cmd=edit")."&button=1";
                    $args["right_src"] = REQUESTROOT ."/X/KNJXSEARCH5/index.php?cmd=right&PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/H/KNJH400/knjh400index.php?cmd=edit")."&button=1";
                    $args["cols"] = "23%,*";
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
$KNJh400Ctl = new knjh400Controller();
//var_dump($_REQUEST);
