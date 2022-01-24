<?php

require_once('for_php7.php');

require_once('knje041Model.inc');
require_once('knje041Query.inc');

class knje041Controller extends Controller {
    var $ModelClassName = "knje041Model";
    var $ProgramID      = "KNJE041";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    //分割フレーム作成
                    $args["left_src"]  = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/X/KNJXATTEND3/index.php?cmd=detail") ."&button=3" ."&SEND_AUTH={$sessionInstance->auth}&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&URL_SCHOOLCD={$sessionInstance->urlSchoolCd}&MN_ID={$sessionInstance->mnId}";
                    $args["right_src"] = REQUESTROOT ."/X/KNJXATTEND3/index.php?MEMO=knje041&PRGID=KNJE041";
                    $args["cols"] = "50%,50%";
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
$knje041Ctl = new knje041Controller;
//var_dump($_REQUEST);
?>
