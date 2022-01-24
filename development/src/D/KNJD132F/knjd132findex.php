<?php

require_once('for_php7.php');

require_once('knjd132fModel.inc');
require_once('knjd132fQuery.inc');

class knjd132fController extends Controller {
    var $ModelClassName = "knjd132fModel";
    var $ProgramID      = "KNJD132F";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "updEdit":
                case "clear":
                    $this->callView("knjd132fForm1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("updEdit");
                    break 1;
                case "club":            //部活参照
                    $this->callView("knjd132fClub");
                    break 2;
                case "committee":       //委員会参照
                    $this->callView("knjd132fCommittee");
                    break 2;
                case "clubhdetail":    //大会記録備考参照参照
                    $this->callView("knjd132fClubHdetail");
                    break 2;
                case "qualified":       //検定参照
                    $this->callView("knjd132fQualified");
                    break 2;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&URL_SCHOOLCD={$sessionInstance->urlSchoolCd}&MN_ID={$sessionInstance->mnId}&PATH=" .urlencode("/D/KNJD132F/knjd132findex.php?cmd=edit") ."&button=1" ."&SES_FLG=2";
                    $args["right_src"] = "knjd132findex.php?cmd=edit";
                    $args["cols"] = "40%,*";
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
$knjd132fCtl = new knjd132fController;
?>
