<?php

require_once('for_php7.php');

require_once('knjh160aModel.inc');
require_once('knjh160aQuery.inc');

class knjh160aController extends Controller {
    var $ModelClassName = "knjh160aModel";
    var $ProgramID      = "KNJH160A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);

        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit": 
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh160aForm2");
                    break 2;
                case "replace":
                    $this->callView("knjh160aSubForm1");
                    break 2;
                case "replace_update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->ReplaceModel();
                    $sessionInstance->setCmd("replace");
                    break 1;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $search = "?PROGRAMID=" .$this->ProgramID ."&AUTH=".AUTHORITY."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&URL_SCHOOLCD={$sessionInstance->urlSchoolCd}&MN_ID={$sessionInstance->mnId}&TARGET=right_frame&PATH=" .urlencode("/H/KNJH160A/knjh160aindex.php?cmd=edit") ."&button=1";
                case "back":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP3/index.php" .$search;
                    $args["right_src"] = "knjh160aindex.php?cmd=edit";
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
$knjh160aCtl = new knjh160aController;
?>
