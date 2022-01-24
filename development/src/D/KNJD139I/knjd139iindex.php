<?php

require_once('for_php7.php');

require_once('knjd139iModel.inc');
require_once('knjd139iQuery.inc');

class knjd139iController extends Controller
{
    public $ModelClassName = "knjd139iModel";
    public $ProgramID      = "KNJD139I";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "updEdit":
                case "edit":
                case "clear":
                    $this->callView("knjd139iForm1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knjd139iForm1");
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("updEdit");
                    break 1;
                case "subform1":    //通知表所見参照
                    $this->callView("knjd139iSubForm1");
                    break 2;
                case "attendRemark": //出欠備考参照
                    $this->callView("knjd139iAttendRemark");
                    break 2;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP_GHR/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/D/KNJD139I/knjd139iindex.php?cmd=edit") ."&button=1&schoolKind=P";
                    $args["right_src"] = "knjd139iindex.php?cmd=edit";
                    $args["cols"] = "20%,80%";
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
$knjd139iCtl = new knjd139iController;
