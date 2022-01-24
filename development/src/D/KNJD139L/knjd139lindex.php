<?php

require_once('for_php7.php');
require_once('knjd139lModel.inc');
require_once('knjd139lQuery.inc');

class knjd139lController extends Controller {
    var $ModelClassName = "knjd139lModel";
    var $ProgramID      = "KNJD139L";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "updEdit":
                case "edit":
                case "clear":
                    $this->callView("knjd139lForm1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knjd139lForm1");
                    $sessionInstance->setAccessLogDetail("U", $ProgramID); 
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("updEdit");
                    break 1;
                case "subform1":    //通知表所見参照
                    $this->callView("knjd139lSubForm1");
                    break 2;
                // case "attendRemark": //出欠備考参照
                //     $this->callView("knjd139lAttendRemark");
                //     break 2;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP_GHR/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/D/KNJD139L/knjd139lindex.php?cmd=edit") ."&button=1&schoolKind=P";
                    $args["right_src"] = "knjd139lindex.php?cmd=edit";
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
$knjd139lCtl = new knjd139lController;
?>
