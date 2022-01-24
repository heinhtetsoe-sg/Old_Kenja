<?php

require_once('for_php7.php');

require_once('knjd139tModel.inc');
require_once('knjd139tQuery.inc');

class knjd139tController extends Controller {
    var $ModelClassName = "knjd139tModel";
    var $ProgramID      = "KNJD139T";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "updEdit":
                case "clear":
                    $this->callView("knjd139tForm1");
                    break 2;
                case "committee": //委員会参照
                    $this->callView("knjd139tCommittee");
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
                    $args["left_src"]  = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/D/KNJD139T/knjd139tindex.php?cmd=edit") ."&button=1" ."&SES_FLG=1";
                    $args["left_src"] .= "&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&URL_SCHOOLCD={$sessionInstance->urlSchoolCd}&MN_ID={$sessionInstance->mnId}";
                    $args["right_src"] = "knjd139tindex.php?cmd=edit";
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
$knjd139tCtl = new knjd139tController;
?>
