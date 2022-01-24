<?php

require_once('for_php7.php');

require_once('knjd428dModel.inc');
require_once('knjd428dQuery.inc');

class knjd428dController extends Controller {
    var $ModelClassName = "knjd428dModel";
    var $ProgramID      = "KNJD428D";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "gakki":
                case "edit":
                case "clear":
                    $this->callView("knjd428dForm1");
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
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP_GHR/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/D/KNJD428D/knjd428dindex.php?cmd=edit") ."&button=1" ."&SEND_AUTH={$sessionInstance->auth}&HANDICAP_FLG=N1";
                    $args["right_src"] = "knjd428dindex.php?cmd=edit";
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
$knjd428dCtl = new knjd428dController;
?>
