<?php

require_once('for_php7.php');

require_once('knjd132hModel.inc');
require_once('knjd132hQuery.inc');

class knjd132hController extends Controller {
    var $ModelClassName = "knjd132hModel";
    var $ProgramID      = "KNJD132H";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "updEdit":
                case "clear":
                    $this->callView("knjd132hForm1");
                    break 2;
                case "club": //部活参照
                    $this->callView("knjd132hClub");
                    break 2;
                case "committee": //委員会参照
                    $this->callView("knjd132hCommittee");
                    break 2;
                case "qualified": //資格参照
                    $this->callView("knjd132hQualified");
                    break 2;
                case "club_record": //大会記録備考参照
                    $this->callView("knjd132hClubRecord");
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
                    $args["left_src"]  = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/D/KNJD132H/knjd132hindex.php?cmd=edit") ."&button=1" ."&SES_FLG=1";
                    $args["left_src"] .= "&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&URL_SCHOOLCD={$sessionInstance->urlSchoolCd}&MN_ID={$sessionInstance->mnId}";
                    $args["right_src"] = "knjd132hindex.php?cmd=edit";
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
$knjd132hCtl = new knjd132hController;
?>
