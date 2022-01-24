<?php
require_once('knjd428mModel.inc');
require_once('knjd428mQuery.inc');

class knjd428mController extends Controller
{
    public $ModelClassName = "knjd428mModel";
    public $ProgramID      = "KNJD428M";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "edit2":
                case "reset":
                case "updEdit":
                    $this->callView("knjd428mForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("updEdit");
                    break 1;
                case "update_remark":
                    $sessionInstance->getUpdateRemarkModel();
                    $sessionInstance->setCmd("subform1");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "input":
                case "subform1":
                case "reset_remark":
                    $this->callView("knjd428mSubForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $search  = "?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/D/KNJD428M/knjd428mindex.php?cmd=edit") ."&button=1&SEND_AUTH={$sessionInstance->auth}&HANDICAP_FLG=1";
                    // no break
                case "back":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP_GHR/index.php" .$search;
                    $args["right_src"] = "knjd428mindex.php?cmd=edit2";
                    $args["cols"] = "20%,80%";
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
$knjd428mCtl = new knjd428mController();
