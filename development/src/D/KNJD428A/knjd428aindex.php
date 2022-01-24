<?php

require_once('for_php7.php');

require_once('knjd428aModel.inc');
require_once('knjd428aQuery.inc');

class knjd428aController extends Controller {
    var $ModelClassName = "knjd428aModel";
    var $ProgramID      = "KNJD428A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "gakki":
                case "edit":
                case "clear":
                    $this->callView("knjd428aForm1");
                    break 2;
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
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP_GHR/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/D/KNJD428A/knjd428aindex.php?cmd=edit") ."&button=1" ."&SEND_AUTH={$sessionInstance->auth}&HANDICAP_FLG=N1";
                    $args["right_src"] = "knjd428aindex.php?cmd=edit";
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
$knjd428aCtl = new knjd428aController;
?>
