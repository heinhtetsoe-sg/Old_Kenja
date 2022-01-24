<?php

require_once('for_php7.php');

require_once('knjd420pModel.inc');
require_once('knjd420pQuery.inc');

class knjd420pController extends Controller {
    var $ModelClassName = "knjd420pModel";
    var $ProgramID      = "KNJD420P";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "gakki":
                case "edit":
                case "edit2":
                case "clear":
                    $this->callView("knjd420pForm1");
                    break 2;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "changeSemester":
                case "sort":
                case "updateEnd":
                    $sessionInstance->knjd420pModel();       //コントロールマスタの呼び出し
                    $this->callView("knjd420pForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("updateEnd");
                    break 1;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP_GHR/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/D/KNJD420P/knjd420pindex.php?cmd=edit") ."&button=1" ."&SEND_AUTH={$sessionInstance->auth}&HANDICAP_FLG=1:2";
                    $args["right_src"] = "knjd420pindex.php?cmd=edit";
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
$knjd420pCtl = new knjd420pController;
?>
