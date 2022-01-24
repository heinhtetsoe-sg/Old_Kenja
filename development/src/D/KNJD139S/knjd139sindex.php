<?php

require_once('for_php7.php');

require_once('knjd139sModel.inc');
require_once('knjd139sQuery.inc');

class knjd139sController extends Controller {
    var $ModelClassName = "knjd139sModel";
    var $ProgramID      = "KNJD139S";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "edit2":
                case "updEdit":
                case "reset":
                case "clear":
                case "attend":
                    $this->callView("knjd139sForm1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("updEdit");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/D/KNJD139S/knjd139sindex.php?cmd=edit") ."&button=1" ."&SES_FLG=1&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&URL_SCHOOLCD={$sessionInstance->urlSchoolCd}&MN_ID={$sessionInstance->mnId}";
                    $args["right_src"] = "knjd139sindex.php?cmd=edit";
                    $args["cols"] = "20%,80%";
                    View::frame($args);
                    exit;
                case "back":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php" .$search;
                    $args["right_src"] = "knjd139sindex.php?cmd=edit2";
                    $args["cols"] = "20%,80%";
                    View::frame($args);
                    return;
                case "ikkatsu":
                    $this->callView("knjd139sIkkatsu"); //一括更新画面
                    break 2;
                case "ikkatsu_update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->IkkatsuModel();
                    $sessionInstance->setCmd("ikkatsu");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd139sCtl = new knjd139sController;
?>
